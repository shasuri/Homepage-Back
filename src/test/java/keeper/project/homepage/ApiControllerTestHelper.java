package keeper.project.homepage;

import static keeper.project.homepage.service.sign.SignUpService.HALF_GENERATION_MONTH;
import static keeper.project.homepage.service.sign.SignUpService.KEEPER_FOUNDING_YEAR;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import keeper.project.homepage.ApiControllerTestSetUp;
import keeper.project.homepage.dto.result.SingleResult;
import keeper.project.homepage.dto.sign.SignInDto;
import keeper.project.homepage.entity.member.MemberEntity;
import keeper.project.homepage.entity.member.MemberHasMemberJobEntity;
import keeper.project.homepage.entity.member.MemberJobEntity;
import keeper.project.homepage.entity.member.MemberRankEntity;
import keeper.project.homepage.entity.member.MemberTypeEntity;
import keeper.project.homepage.entity.posting.CategoryEntity;
import keeper.project.homepage.entity.posting.CommentEntity;
import keeper.project.homepage.entity.posting.PostingEntity;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;
import org.springframework.test.web.servlet.MvcResult;

public class ApiControllerTestHelper extends ApiControllerTestSetUp {

  final public String memberPassword = "memberPassword";
  final public String postingPassword = "postingPassword";

  public enum MemberJobName {
    회장("ROLE_회장"),
    부회장("ROLE_부회장"),
    대외부장("ROLE_대외부장"),
    학술부장("ROLE_학술부장"),
    전산관리자("ROLE_전산관리자"),
    서기("ROLE_서기"),
    총무("ROLE_총무"),
    사서("ROLE_사서"),
    회원("ROLE_회원");

    private String jobName;

    MemberJobName(String jobName) {
      this.jobName = jobName;
    }

    public String getJobName() {
      return jobName;
    }
  }

  public enum MemberRankName {
    일반회원("일반회원"),
    우수회원("우수회원");

    private String rankName;

    MemberRankName(String rankName) {
      this.rankName = rankName;
    }

    public String getRankName() {
      return rankName;
    }
  }

  public enum MemberTypeName {
    비회원("비회원"), 정회원("정회원"), 휴면회원("휴면회원"), 졸업("졸업"), 탈퇴("탈퇴");

    private String typeName;

    MemberTypeName(String typeName) {
      this.typeName = typeName;
    }

    public String getTypeName() {
      return typeName;
    }

  }

  public enum ResponseType {
    SINGLE("data"),
    LIST("list[]");

    private final String reponseFieldPrefix;

    ResponseType(String reponseFieldPrefix) {
      this.reponseFieldPrefix = reponseFieldPrefix;
    }

    public String getReponseFieldPrefix() {
      return reponseFieldPrefix;
    }
  }

  public Float getMemberGeneration() {
    LocalDate date = LocalDate.now();
    Float generation = (float) (date.getYear() - KEEPER_FOUNDING_YEAR);
    if (date.getMonthValue() >= HALF_GENERATION_MONTH) {
      generation += 0.5F;
    }
    return generation;
  }

  public MemberEntity generateMemberEntity(MemberJobName jobName, MemberTypeName typeName,
      MemberRankName rankName) {
    final String epochTime = Long.toHexString(System.nanoTime());
    MemberJobEntity memberJob = memberJobRepository.findByName(jobName.getJobName()).get();
    MemberHasMemberJobEntity hasMemberJobEntity = MemberHasMemberJobEntity.builder()
        .memberJobEntity(memberJob)
        .build();
    MemberTypeEntity memberType = memberTypeRepository.findByName(typeName.getTypeName()).get();
    MemberRankEntity memberRank = memberRankRepository.findByName(rankName.getRankName()).get();
    return memberRepository.saveAndFlush(MemberEntity.builder()
        .loginId("LoginId" + epochTime)
        .password(passwordEncoder.encode(memberPassword))
        .realName("RealName")
        .nickName("NickName")
        .emailAddress("member" + epochTime + "@k33p3r.com")
        .studentId(epochTime)
        .generation(getMemberGeneration())
        .memberJobs(new ArrayList<>(List.of(hasMemberJobEntity)))
        .memberType(memberType)
        .memberRank(memberRank)
        .build());
  }

  public String generateJWTToken(String loginId, String password) throws Exception {
    String content = "{\n"
        + "    \"loginId\": \"" + loginId + "\",\n"
        + "    \"password\": \"" + password + "\"\n"
        + "}";
    MvcResult result = mockMvc.perform(post("/v1/signin")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(content))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.code").value(0))
        .andExpect(jsonPath("$.msg").exists())
        .andExpect(jsonPath("$.data").exists())
        .andReturn();

    String resultString = result.getResponse().getContentAsString();
    ObjectMapper mapper = new ObjectMapper();
    SingleResult<SignInDto> sign = mapper.readValue(resultString, new TypeReference<>() {
    });
    return sign.getData().getToken();
  }

  public CategoryEntity generateCategoryEntity() {
    final String epochTime = Long.toHexString(System.nanoTime());
    return categoryRepository.save(
        CategoryEntity.builder().name("testCategory" + epochTime).build());
  }

  public PostingEntity generatePostingEntity(MemberEntity writer, CategoryEntity category,
      Integer isNotice, Integer isSecret, Integer isTemp) {
    final String epochTime = Long.toHexString(System.nanoTime());
    final LocalDateTime now = LocalDateTime.now();
    return postingRepository.save(PostingEntity.builder()
        .title("posting 제목 " + epochTime)
        .content("posting 내용 " + epochTime)
        .categoryId(category)
        .ipAddress("192.111.222.333")
        .allowComment(0)
        .isNotice(isNotice)
        .isSecret(isSecret)
        .isTemp(isTemp)
        .likeCount(0)
        .dislikeCount(0)
        .commentCount(0)
        .visitCount(0)
        .registerTime(now)
        .updateTime(now)
        .password(postingPassword)
        .memberId(writer)
        .build());
  }

  public CommentEntity generateCommentEntity(PostingEntity posting, MemberEntity writer,
      Long parentId) {
    final String epochTime = Long.toHexString(System.nanoTime());
    final LocalDateTime now = LocalDateTime.now();
    final String content = (parentId == 0L ? "댓글 내용 " : parentId + "의 대댓글 내용 ") + epochTime;
    return commentRepository.save(CommentEntity.builder()
        .content(content)
        .registerTime(now)
        .updateTime(now)
        .ipAddress("111.111.111.111")
        .likeCount(0)
        .dislikeCount(0)
        .parentId(parentId)
        .member(writer)
        .postingId(posting)
        .build());
  }

  public List<FieldDescriptor> generateCommonResponseFields(String docSuccess, String docCode,
      String docMsg) {
    List<FieldDescriptor> commonFields = new ArrayList<>();
    commonFields.addAll(Arrays.asList(
        fieldWithPath("success").description(docSuccess),
        fieldWithPath("code").description(docCode),
        fieldWithPath("msg").description(docMsg)));
    return commonFields;
  }

  public List<FieldDescriptor> generateMemberCommonResponseFields(ResponseType type,
      String success, String code, String msg, FieldDescriptor... addDescriptors) {
    String prefix = type.getReponseFieldPrefix();
    List<FieldDescriptor> commonFields = new ArrayList<>();
    commonFields.addAll(generateCommonResponseFields(success, code, msg));
    commonFields.addAll(Arrays.asList(
        fieldWithPath(prefix + ".id").description("아이디"),
        fieldWithPath(prefix + ".emailAddress").description("이메일 주소"),
        fieldWithPath(prefix + ".nickName").description("닉네임"),
        fieldWithPath(prefix + ".birthday").description("생일").type(Date.class).optional(),
        fieldWithPath(prefix + ".registerDate").description("가입 날짜"),
        fieldWithPath(prefix + ".point").description("포인트 점수"),
        fieldWithPath(prefix + ".level").description("레벨"),
        fieldWithPath(prefix + ".merit").description("상점"),
        fieldWithPath(prefix + ".demerit").description("벌점"),
        fieldWithPath(prefix + ".generation").description("기수 (7월 이후는 N.5기)"),
        fieldWithPath(prefix + ".thumbnailId").description("회원의 썸네일 이미지 아이디"),
        fieldWithPath(prefix + ".rank").description("회원 등급: null, 우수회원, 일반회원"),
        fieldWithPath(prefix + ".type").description("회원 상태: null, 비회원, 정회원, 휴면회원, 졸업회원, 탈퇴"),
        fieldWithPath(prefix + ".jobs").description(
            "동아리 직책: null, ROLE_회장, ROLE_부회장, ROLE_대외부장, ROLE_학술부장, ROLE_전산관리자, ROLE_서기, ROLE_총무, ROLE_사서"))
    );
    if (addDescriptors.length > 0) {
      commonFields.addAll(Arrays.asList(addDescriptors));
    }
    return commonFields;
  }

  public List<FieldDescriptor> generatePostingResponseFields(ResponseType type, String success,
      String code, String msg, FieldDescriptor... addDescriptors) {
    String prefix = type.getReponseFieldPrefix();
    List<FieldDescriptor> commonFields = new ArrayList<>();
    commonFields.addAll(generateCommonResponseFields(success, code, msg));
    commonFields.addAll(Arrays.asList(
        // FIXME : PostingDto에 id와 writer가 없음
//        fieldWithPath(prefix + ".id").description("게시물 ID"),
        fieldWithPath(prefix + ".title").description("게시물 제목"),
        fieldWithPath(prefix + ".content").description("게시물 내용"),
//        fieldWithPath(prefix + ".writer").description("작성자  (비밀 게시글일 경우 익명)"),
        fieldWithPath(prefix + ".visitCount").description("조회 수"),
        fieldWithPath(prefix + ".likeCount").description("좋아요 수"),
        fieldWithPath(prefix + ".dislikeCount").description("싫어요 수"),
        fieldWithPath(prefix + ".commentCount").description("댓글 수"),
        fieldWithPath(prefix + ".registerTime").description("작성 시간"),
        fieldWithPath(prefix + ".updateTime").description("수정 시간"),
        fieldWithPath(prefix + ".ipAddress").description("IP 주소"),
        fieldWithPath(prefix + ".allowComment").description("댓글 허용?"),
        fieldWithPath(prefix + ".isNotice").description("공지글?"),
        fieldWithPath(prefix + ".isSecret").description("비밀글?"),
        fieldWithPath(prefix + ".isTemp").description("임시저장?"),
        fieldWithPath(prefix + ".password").description("비밀번호").optional(),
        fieldWithPath(prefix + ".memberId").description("작성자 아이디"),
        fieldWithPath(prefix + ".categoryId").description("카테고리 아이디"),
        fieldWithPath(prefix + ".thumbnailId").description("게시글 썸네일 아이디")
    ));
    if (addDescriptors.length > 0) {
      commonFields.addAll(Arrays.asList(addDescriptors));
    }
    return commonFields;
  }

  public List<FieldDescriptor> generateCommonCommentResponse(ResponseType type, String docSuccess,
      String docCode, String docMsg, FieldDescriptor... descriptors) {
    String prefix = type.getReponseFieldPrefix();
    List<FieldDescriptor> commonFields = new ArrayList<>();
    commonFields.addAll(generateCommonResponseFields(docSuccess, docCode, docMsg));
    commonFields.addAll(Arrays.asList(
        fieldWithPath(prefix + ".id").description("댓글 id"),
        fieldWithPath(prefix + ".content").description("댓글 내용"),
        fieldWithPath(prefix + ".registerTime").description("댓글이 처음 등록된 시간"),
        fieldWithPath(prefix + ".updateTime").description("댓글이 수정된 시간"),
        fieldWithPath(prefix + ".ipAddress").description("댓글 작성자의 ip address"),
        fieldWithPath(prefix + ".likeCount").description("좋아요 개수"),
        fieldWithPath(prefix + ".dislikeCount").description("싫어요 개수"),
        fieldWithPath(prefix + ".parentId").description("대댓글인 경우, 부모 댓글의 id"),
        fieldWithPath(prefix + ".writer").optional().description("작성자 (탈퇴한 작성자일 경우 null)"),
        fieldWithPath(prefix + ".writerId").optional().description("작성자 (탈퇴한 작성자일 경우 null)"),
        fieldWithPath(prefix + ".writerThumbnailId").optional().type(Long.TYPE)
            .description("작성자 (탈퇴했을 경우 / 썸네일을 등록하지 않았을 경우 null)")));
    if (descriptors.length > 0) {
      commonFields.addAll(Arrays.asList(descriptors));
    }
    return commonFields;
  }
}