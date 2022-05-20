package keeper.project.homepage.repository.ctf;

import keeper.project.homepage.entity.ctf.CtfChallengeCategoryEntity;
import keeper.project.homepage.entity.ctf.CtfSubmitLogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CtfSubmitLogRepository extends
    JpaRepository<CtfSubmitLogEntity, Long> {

  Page<CtfSubmitLogEntity> findAllByIdIsNot(Long id, Pageable pageable);
}