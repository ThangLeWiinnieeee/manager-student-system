package vn.edu.fit.topicmanagement.major;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MajorRepository extends JpaRepository<Major, Long> {
    List<Major> findAllByOrderByCodeAsc();
    List<Major> findByEnabledTrueOrderByCodeAsc();
    boolean existsByCodeIgnoreCase(String code);
    boolean existsByNameIgnoreCase(String name);
    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
