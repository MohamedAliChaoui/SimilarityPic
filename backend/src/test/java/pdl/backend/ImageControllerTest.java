package pdl.backend;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ImageController.class)
public class ImageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ImageRepository imageRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private DescriptorRepository descriptorRepository;

    @MockBean
    private ImageProcessor imageProcessor;

    private Image testImage;

    @BeforeEach
    void setUp() {
        // Créer une image de test avec un ID
        testImage = Mockito.mock(Image.class);
        when(testImage.getId()).thenReturn(1L);
        when(testImage.getName()).thenReturn("test.jpg");
        when(testImage.getFormat()).thenReturn("jpeg");
        when(testImage.getSize()).thenReturn(3L);
        when(testImage.isPublished()).thenReturn(true);
        when(testImage.isFavorite()).thenReturn(false);
    }

    @Test
    void testGetImageList() throws Exception {
        // Configuration du mock
        when(imageRepository.findAll()).thenReturn(Arrays.asList(testImage));

        // Test de l'endpoint
        mockMvc.perform(get("/images")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("test.jpg"))
                .andExpect(jsonPath("$[0].format").value("jpeg"));
    }

    @Test
    void testGetImage() throws Exception {
        // Configuration du mock
        when(imageRepository.findById(1L)).thenReturn(Optional.of(testImage));

        // Test de l'endpoint
        mockMvc.perform(get("/images/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testGetImageNotFound() throws Exception {
        // Configuration du mock
        when(imageRepository.findById(999L)).thenReturn(Optional.empty());

        // Test de l'endpoint
        mockMvc.perform(get("/images/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetFeed() throws Exception {
        // Configuration du mock
        when(imageRepository.findByPublishedTrue()).thenReturn(Arrays.asList(testImage));

        // Test de l'endpoint
        mockMvc.perform(get("/images/feed")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("test.jpg"))
                .andExpect(jsonPath("$[0].isPublished").value(true));
    }

    @Test
    void testTogglePublish() throws Exception {
        // Configuration du mock
        when(imageRepository.findById(1L)).thenReturn(Optional.of(testImage));
        when(imageRepository.save(any(Image.class))).thenReturn(testImage);

        // Test de l'endpoint
        mockMvc.perform(put("/images/1/publish")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testToggleFavorite() throws Exception {
        // Configuration du mock
        when(imageRepository.findById(1L)).thenReturn(Optional.of(testImage));
        when(imageRepository.save(any(Image.class))).thenReturn(testImage);

        // Test de l'endpoint
        mockMvc.perform(put("/images/1/favorite")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
} 