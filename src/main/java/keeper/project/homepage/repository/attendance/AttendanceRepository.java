package keeper.project.homepage.repository.attendance;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import keeper.project.homepage.entity.attendance.AttendanceEntity;
import keeper.project.homepage.entity.member.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceRepository extends JpaRepository<AttendanceEntity, Long> {

  Optional<AttendanceEntity> findTopByMemberIdOrderByIdDesc(MemberEntity memberEntity);

  List<AttendanceEntity> findAllByMemberId(MemberEntity memberEntity);

  List<AttendanceEntity> findAllByTimeBetween(Date date1, Date date2);

  List<AttendanceEntity> findByMemberIdAndTimeBetween(
      MemberEntity memberId, Date time, Date time2);

}
