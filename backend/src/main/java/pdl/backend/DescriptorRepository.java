package pdl.backend;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DescriptorRepository extends JpaRepository<Descriptor, Long> {
    List<Descriptor> findByImageId(Long imageId);
    List<Descriptor> findByType(String type);
    List<Descriptor> findByImageIdAndType(Long imageId, String type);
}