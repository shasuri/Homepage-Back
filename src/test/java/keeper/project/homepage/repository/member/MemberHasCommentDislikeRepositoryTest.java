package keeper.project.homepage.repository.member;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import keeper.project.homepage.entity.posting.CategoryEntity;
import keeper.project.homepage.entity.posting.CommentEntity;
import keeper.project.homepage.entity.member.MemberEntity;
import keeper.project.homepage.entity.member.MemberHasCommentDislikeEntity;
import keeper.project.homepage.entity.member.MemberHasCommentEntityPK;
import keeper.project.homepage.entity.posting.PostingEntity;
import keeper.project.homepage.repository.member.MemberHasCommentDislikeRepository;
import keeper.project.homepage.repository.member.MemberRepository;
import keeper.project.homepage.repository.posting.CategoryRepository;
import keeper.project.homepage.repository.posting.CommentRepository;
import keeper.project.homepage.repository.posting.PostingRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class MemberHasCommentDislikeRepositoryTest {

  private Logger LOGGER = LogManager.getLogger();

  @Autowired
  private MemberHasCommentDislikeRepository memberHasCommentDislikeRepository;

  @Autowired
  private CommentRepository commentRepository;

  @Autowired
  private CategoryRepository categoryRepository;

  @Autowired
  private PostingRepository postingRepository;

  @Autowired
  private MemberRepository memberRepository;

  private CommentEntity commentEntity;
  private CommentEntity parentComment;
  private MemberEntity memberEntity;
  private MemberHasCommentDislikeEntity memberHasCommentDislikeEntity;

  private LocalDate registerTime = LocalDate.now();
  private LocalDate updateTime = LocalDate.now();
  private String ipAddress = "127.0.0.1";
  private Integer likeCount = 0;
  private Integer dislikeCount = 0;

  @BeforeEach
  public void setup() {
    memberEntity = memberRepository.save(MemberEntity.builder()
        .loginId("로그인")
        .password("비밀번호")
        .realName("이름")
        .nickName("닉네임")
        .emailAddress("이메일")
        .studentId("학번")
        .roles(Collections.singletonList("ROLE_USER")).build());
    CategoryEntity categoryEntity = categoryRepository.save(
        CategoryEntity.builder().name("test category").build());

    PostingEntity posting = postingRepository.save(PostingEntity.builder()
        .title("posting 제목")
        .content("posting 내용")
        .categoryId(categoryEntity)
        .ipAddress("192.111.222.333")
        .allowComment(0)
        .isNotice(0)
        .isSecret(1)
        .likeCount(10)
        .dislikeCount(1)
        .commentCount(0)
        .visitCount(0)
        .registerTime(new Date())
        .updateTime(new Date())
        .memberId(memberEntity)
        .password("asdsdf")
        .build());

    parentComment = commentRepository.save(CommentEntity.builder()
        .content("부모 댓글 내용")
        .registerTime(registerTime)
        .updateTime(updateTime)
        .ipAddress(ipAddress)
        .likeCount(likeCount)
        .dislikeCount(dislikeCount)
        .parentId(0L)
        .memberId(memberEntity)
        .postingId(posting)
        .build());

    commentEntity = commentRepository.save(CommentEntity.builder()
        .content("댓글 내용")
        .registerTime(registerTime)
        .updateTime(updateTime)
        .ipAddress(ipAddress)
        .likeCount(likeCount)
        .dislikeCount(dislikeCount)
        .parentId(parentComment.getId())
        .memberId(memberEntity)
        .postingId(posting)
        .build());

    memberHasCommentDislikeEntity = memberHasCommentDislikeRepository.save(
        MemberHasCommentDislikeEntity.builder().memberHasCommentEntityPK(
            new MemberHasCommentEntityPK(memberEntity, commentEntity)).build());
  }

  @Test
  @DisplayName("\"멤버-댓글 싫어요\" 생성")
  public void createTest() {
    LOGGER.info("comment id : " + commentEntity.getId());
    LOGGER.info("member id : " + memberEntity.getId());
    MemberHasCommentDislikeEntity newMHCD = memberHasCommentDislikeRepository.save(
        MemberHasCommentDislikeEntity.builder().memberHasCommentEntityPK(
            new MemberHasCommentEntityPK(memberEntity, parentComment)).build());
    LOGGER.info(String.format("create한 member_xx_dislike: (comment : %d, member : %d)",
        newMHCD.getMemberHasCommentEntityPK().getCommentEntity().getId(),
        newMHCD.getMemberHasCommentEntityPK().getMemberEntity().getId()));

  }

  @Test
  @DisplayName("댓글 id로 \"멤버-댓글 싫어요\" 조회")
  public void findByCommentIdTest() {
    List<MemberHasCommentDislikeEntity> findMHCDs = memberHasCommentDislikeRepository.findByMemberHasCommentEntityPK_CommentEntity(
        commentEntity);
    findMHCDs.forEach(
        mhcd -> LOGGER.info(String.format("found member_xx_dislike: (comment : %d, member : %d)",
            mhcd.getMemberHasCommentEntityPK().getCommentEntity().getId(),
            mhcd.getMemberHasCommentEntityPK().getMemberEntity().getId())));
    Assertions.assertTrue(!findMHCDs.isEmpty());
  }

  @Test
  @DisplayName("멤버 id로 \"멤버-댓글 싫어요\" 조회")
  public void findByMemberIdTest() {
    List<MemberHasCommentDislikeEntity> findMHCLs = memberHasCommentDislikeRepository.findByMemberHasCommentEntityPK_MemberEntity(
        memberEntity);
    findMHCLs.forEach(
        mhcd -> LOGGER.info(String.format("found member_xx_dislike: (comment : %d, member : %d)",
            mhcd.getMemberHasCommentEntityPK().getCommentEntity().getId(),
            mhcd.getMemberHasCommentEntityPK().getMemberEntity().getId())));
    Assertions.assertTrue(!findMHCLs.isEmpty());
  }

  @Test
  @DisplayName("\"멤버-댓글 싫어요\" 전체 리스트 조회")
  public void findAllTest() {
    List<MemberHasCommentDislikeEntity> findMHCDs = memberHasCommentDislikeRepository.findAll();
    findMHCDs.forEach(
        mhcd -> LOGGER.info(String.format("found member_xx_dislike: (comment : %d, member : %d)",
            mhcd.getMemberHasCommentEntityPK().getCommentEntity().getId(),
            mhcd.getMemberHasCommentEntityPK().getMemberEntity().getId())));
    Assertions.assertTrue(!findMHCDs.isEmpty());
  }

  @Test
  @DisplayName("멤버 id와 댓글 id로 \"멤버-댓글 싫어요\" 조회")
  public void findByMemberHasCommentLikeIdTest() {
    Optional<MemberHasCommentDislikeEntity> mhcd = memberHasCommentDislikeRepository.findById(
        new MemberHasCommentEntityPK(memberEntity, commentEntity));
    Assertions.assertTrue(mhcd.isPresent());
    LOGGER.info(String.format("found member_xx_dislike: (comment : %d, member : %d)",
        mhcd.get().getMemberHasCommentEntityPK().getCommentEntity().getId(),
        mhcd.get().getMemberHasCommentEntityPK().getMemberEntity().getId()));
  }

  @Test
  @DisplayName("멤버 id와 댓글 id로 \"멤버-댓글 싫어요\" 삭제 - 해당 멤버가 댓글의 좋아요를 취소한 경우")
  public void deleteByIdTest() {
    memberHasCommentDislikeRepository.deleteById(
        new MemberHasCommentEntityPK(memberEntity, commentEntity));

    Optional<MemberHasCommentDislikeEntity> findMHCLs = memberHasCommentDislikeRepository.findById(
        new MemberHasCommentEntityPK(memberEntity, commentEntity));
    Assertions.assertTrue(findMHCLs.isEmpty());
  }

  @Test
  @DisplayName("댓글 id로 \"멤버-댓글 싫어요\" 삭제 - 해당 댓글이 삭제된 경우")
  public void deleteByCommentIdTest() {
    memberHasCommentDislikeRepository.deleteByMemberHasCommentEntityPK_CommentEntity(
        commentEntity);

    List<MemberHasCommentDislikeEntity> findMHCDs = memberHasCommentDislikeRepository.findByMemberHasCommentEntityPK_CommentEntity(
        commentEntity);
    Assertions.assertTrue(findMHCDs.isEmpty());
  }

  @Test
  @DisplayName("멤버 id로 \"멤버-댓글 싫어요\" 삭제 - 해당 멤버 정보가 삭제된 경우")
  public void deleteByMemberIdTest() {
    LOGGER.info(String.format("delete member_id = %d", memberEntity.getId()));
    memberHasCommentDislikeRepository.deleteByMemberHasCommentEntityPK_MemberEntity(memberEntity);

    List<MemberHasCommentDislikeEntity> findMHCLs = memberHasCommentDislikeRepository.findByMemberHasCommentEntityPK_MemberEntity(
        memberEntity);
    Assertions.assertTrue(findMHCLs.isEmpty());
  }
}