package pdl.backend;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ImageControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	private Long createdImageId;
	private static final String TEST_AUTH_TOKEN = "Bearer test-token";
	private static final String TEST_TOKEN = "test-token";

	@BeforeEach
	public void setup() throws Exception {
		// Créer un utilisateur de test
		User testUser = new User("testuser", "testpass");
		testUser = userRepository.save(testUser);

		// Accéder à la map des sessions actives via réflexion
		Field activeSessionsField = AuthController.class.getDeclaredField("activeSessions");
		activeSessionsField.setAccessible(true);
		@SuppressWarnings("unchecked")
		Map<String, Long> activeSessions = (Map<String, Long>) activeSessionsField.get(null);
		if (activeSessions == null) {
			activeSessions = new HashMap<>();
			activeSessionsField.set(null, activeSessions);
		}
		activeSessions.put(TEST_TOKEN, testUser.getId());

		// Créer une image test avec des histogrammes
		final ClassPathResource imgFile = new ClassPathResource("test.jpg");
		MockMultipartFile multipartFile = new MockMultipartFile("file",
				"test_" + System.currentTimeMillis() + ".jpg",
				"image/jpeg",
				imgFile.getInputStream());

		String response = this.mockMvc.perform(MockMvcRequestBuilders.multipart("/images")
				.file(multipartFile)
				.header("Authorization", TEST_AUTH_TOKEN))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();

		createdImageId = Long.parseLong(response);
	}

	@Test
	@Order(1)
	public void testUploadImageJPEG() throws Exception {
		final ClassPathResource imgFile = new ClassPathResource("test.jpg");
		MockMultipartFile multipartFile = new MockMultipartFile("file",
				"test_jpeg.jpg",
				"image/jpeg",
				imgFile.getInputStream());

		this.mockMvc.perform(MockMvcRequestBuilders.multipart("/images")
				.file(multipartFile)
				.header("Authorization", TEST_AUTH_TOKEN))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(content().string(matchesPattern("\\d+")));
	}

	@Test
	@Order(2)
	public void testUploadImagePNG() throws Exception {
		final ClassPathResource imgFile = new ClassPathResource("test.png");
		MockMultipartFile multipartFile = new MockMultipartFile("file",
				"test_png.png",
				"image/png",
				imgFile.getInputStream());

		this.mockMvc.perform(MockMvcRequestBuilders.multipart("/images")
				.file(multipartFile)
				.header("Authorization", TEST_AUTH_TOKEN))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(content().string(matchesPattern("\\d+")));
	}

	@Test
	@Order(3)
	public void testUploadInvalidImageFormat() throws Exception {
		MockMultipartFile multipartFile = new MockMultipartFile("file",
				"test.gif",
				"image/gif",
				"test data".getBytes());

		this.mockMvc.perform(MockMvcRequestBuilders.multipart("/images")
				.file(multipartFile)
				.header("Authorization", TEST_AUTH_TOKEN))
				.andDo(print())
				.andExpect(status().isUnsupportedMediaType());
	}

	@Test
	@Order(4)
	public void testGetImageList() throws Exception {
		this.mockMvc.perform(get("/images")
				.header("Authorization", TEST_AUTH_TOKEN))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
				.andExpect(jsonPath("$[0].id", notNullValue()))
				.andExpect(jsonPath("$[0].name", notNullValue()))
				.andExpect(jsonPath("$[0].size", notNullValue()))
				.andExpect(jsonPath("$[0].format", notNullValue()))
				.andExpect(jsonPath("$[0].hasDescriptors", notNullValue()));
	}

	@Test
	@Order(5)
	public void testGetImageById() throws Exception {
		this.mockMvc.perform(get("/images/" + createdImageId)
				.header("Authorization", TEST_AUTH_TOKEN))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.IMAGE_JPEG_VALUE));
	}

	@Test
	@Order(6)
	public void testGetImageDescriptors() throws Exception {
		this.mockMvc.perform(get("/images/" + createdImageId + "/descriptors")
				.header("Authorization", TEST_AUTH_TOKEN))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$", hasSize(2))) // Vérifie qu'il y a 2 descripteurs (RGB3D et HS_HISTOGRAM)
				.andExpect(jsonPath("$[*].type", hasItems("RGB3D", "HS_HISTOGRAM")));
	}

	@Test
	@Order(7)
	public void testGetSimilarImages() throws Exception {
		this.mockMvc.perform(get("/images/" + createdImageId + "/similar")
				.param("number", "1")
				.param("descriptor", "RGB3D")
				.header("Authorization", TEST_AUTH_TOKEN))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$[*].similarity", everyItem(lessThanOrEqualTo(1.0))))
				.andExpect(jsonPath("$[*].similarity", everyItem(greaterThanOrEqualTo(0.0))));
	}

	@Test
	@Order(8)
	public void testDeleteImage() throws Exception {
		this.mockMvc.perform(delete("/images/" + createdImageId)
				.header("Authorization", TEST_AUTH_TOKEN))
				.andDo(print())
				.andExpect(status().isOk());

		// Vérifier que l'image a bien été supprimée
		this.mockMvc.perform(get("/images/" + createdImageId)
				.header("Authorization", TEST_AUTH_TOKEN))
				.andExpect(status().isNotFound());
	}

	@Test
	@Order(9)
	public void testGetSimilarImagesWithInvalidNumber() throws Exception {
		this.mockMvc.perform(get("/images/" + createdImageId + "/similar")
				.param("number", "-1")
				.param("descriptor", "RGB3D")
				.header("Authorization", TEST_AUTH_TOKEN))
				.andDo(print())
				.andExpect(status().isBadRequest());
	}

	@Test
	@Order(10)
	public void testGetSimilarImagesWithInvalidDescriptor() throws Exception {
		this.mockMvc.perform(get("/images/" + createdImageId + "/similar")
				.param("number", "1")
				.param("descriptor", "INVALID")
				.header("Authorization", TEST_AUTH_TOKEN))
				.andDo(print())
				.andExpect(status().isBadRequest());
	}

	@Test
	@Order(11)
	public void testUploadImageWithoutAuth() throws Exception {
		final ClassPathResource imgFile = new ClassPathResource("test.jpg");
		MockMultipartFile multipartFile = new MockMultipartFile("file",
				"test_jpeg.jpg",
				"image/jpeg",
				imgFile.getInputStream());

		this.mockMvc.perform(MockMvcRequestBuilders.multipart("/images")
				.file(multipartFile))
				.andDo(print())
				.andExpect(status().isUnauthorized());
	}

	@Test
	@Order(12)
	public void testUploadImageWithInvalidAuth() throws Exception {
		final ClassPathResource imgFile = new ClassPathResource("test.jpg");
		MockMultipartFile multipartFile = new MockMultipartFile("file",
				"test_jpeg.jpg",
				"image/jpeg",
				imgFile.getInputStream());

		this.mockMvc.perform(MockMvcRequestBuilders.multipart("/images")
				.file(multipartFile)
				.header("Authorization", "Bearer invalid-token"))
				.andDo(print())
				.andExpect(status().isUnauthorized());
	}
}
