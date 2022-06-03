package keeper.project.homepage.repository.member;

import java.util.List;
import keeper.project.homepage.entity.member.MemberHasMemberJobEntity;
import keeper.project.homepage.entity.member.MemberJobEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberHasMemberJobRepository extends
    JpaRepository<MemberHasMemberJobEntity, Long> {

  List<MemberHasMemberJobEntity> findAllByMemberEntity_Id(Long id);

  List<MemberHasMemberJobEntity> findAllByMemberEntity_IdAndAndMemberJobEntity_Id(Long memberId,
      Long jobId);

  MemberHasMemberJobEntity findFirstByMemberJobEntityOrderByIdDesc(MemberJobEntity memberJobEntity);
}
