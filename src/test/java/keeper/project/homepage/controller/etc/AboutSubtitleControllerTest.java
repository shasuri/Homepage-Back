package keeper.project.homepage.controller.etc;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import keeper.project.homepage.ApiControllerTestSetUp;
import keeper.project.homepage.dto.result.SingleResult;
import keeper.project.homepage.dto.sign.SignInDto;
import keeper.project.homepage.entity.FileEntity;
import keeper.project.homepage.entity.ThumbnailEntity;
import keeper.project.homepage.entity.etc.StaticWriteSubtitleImageEntity;
import keeper.project.homepage.entity.etc.StaticWriteTitleEntity;
import keeper.project.homepage.entity.member.MemberEntity;
import keeper.project.homepage.entity.member.MemberHasMemberJobEntity;
import keeper.project.homepage.entity.member.MemberJobEntity;
import keeper.project.homepage.entity.member.MemberRankEntity;
import keeper.project.homepage.entity.member.MemberTypeEntity;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Log4j2
public class AboutSubtitleControllerTest extends ApiControllerTestSetUp {

  private String adminToken;

  final private String ipAddress1 = "127.0.0.1";

  final private String adminLoginId = "hyeonmoAdmin";
  final private String adminPassword = "keeper2";
  final private String adminRealName = "JeongHyeonMo2";
  final private String adminNickName = "JeongHyeonMo2";
  final private String adminEmailAddress = "test2@k33p3r.com";
  final private String adminStudentId = "201724580";
  final private String adminPhoneNumber = "0100100101";
  final private int adminPoint = 50;

  private ThumbnailEntity thumbnailEntity;
  private FileEntity imageEntity;
  private StaticWriteSubtitleImageEntity staticWriteSubtitleImageEntity;

  @BeforeEach
  public void setUp() throws Exception {
    imageEntity = FileEntity.builder()
        .fileName("bef.jpg")
        .filePath("keeper_files" + File.separator + "bef.jpg")
        .fileSize(0L)
        .ipAddress(ipAddress1)
        .build();
    fileRepository.save(imageEntity);

    thumbnailEntity = ThumbnailEntity.builder()
        .path("keeper_files" + File.separator + "thumbnail" + File.separator + "thumb_bef.jpg")
        .file(imageEntity).build();
    thumbnailRepository.save(thumbnailEntity);

    MemberJobEntity memberAdminJobEntity = memberJobRepository.findByName("ROLE_회장").get();
    MemberTypeEntity memberTypeEntity = memberTypeRepository.findByName("정회원").get();
    MemberRankEntity memberRankEntity = memberRankRepository.findByName("일반회원").get();
    MemberHasMemberJobEntity hasMemberAdminJobEntity = MemberHasMemberJobEntity.builder()
        .memberJobEntity(memberAdminJobEntity)
        .build();
    MemberEntity memberAdmin = MemberEntity.builder()
        .loginId(adminLoginId)
        .password(passwordEncoder.encode(adminPassword))
        .realName(adminRealName)
        .nickName(adminNickName)
        .emailAddress(adminEmailAddress)
        .studentId(adminStudentId)
        .point(adminPoint)
        .memberType(memberTypeEntity)
        .memberRank(memberRankEntity)
        .thumbnail(thumbnailEntity)
        .memberJobs(new ArrayList<>(List.of(hasMemberAdminJobEntity)))
        .build();
    memberRepository.save(memberAdmin);

    String adminContent = "{\n"
        + "    \"loginId\": \"" + adminLoginId + "\",\n"
        + "    \"password\": \"" + adminPassword + "\"\n"
        + "}";
    MvcResult adminResult = mockMvc.perform(post("/v1/signin")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(adminContent))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.code").value(0))
        .andExpect(jsonPath("$.msg").exists())
        .andExpect(jsonPath("$.data").exists())
        .andReturn();

    ObjectMapper mapper = new ObjectMapper();
    String adminResultString = adminResult.getResponse().getContentAsString();
    SingleResult<SignInDto> adminSign = mapper.readValue(adminResultString, new TypeReference<>() {
    });
    adminToken = adminSign.getData().getToken();

    StaticWriteTitleEntity staticWriteTitleEntity = StaticWriteTitleEntity.builder()
        .title("동아리 지원")
        .type("activity")
        .build();

    staticWriteTitleRepository.save(staticWriteTitleEntity);

    staticWriteSubtitleImageEntity = StaticWriteSubtitleImageEntity.builder()
        .subtitle("서브 타이틀 테스트")
        .staticWriteTitle(staticWriteTitleEntity)
        .thumbnail(thumbnailEntity)
        .displayOrder(2)
        .build();

    staticWriteSubtitleImageRepository.save(staticWriteSubtitleImageEntity);
  }

  @Test
  @DisplayName("페이지 블럭 서브 타이틀 생성하기")
  public void createSubtitle() throws Exception {
    String content = "{\n"
        + "\"subtitle\":\"" + "세미나" + "\",\n"
        + "\"staticWriteTitleId\":\"" + 1 + "\",\n"
        + "\"thumbnailId\":\"" + thumbnailEntity.getId() + "\",\n"
        + "\"displayOrder\":\"" + 10 + "\"\n"
        + "}";

    mockMvc.perform(RestDocumentationRequestBuilders.post("/v1/about/sub-tittle/new")
            .header("Authorization", adminToken)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(content)
            .accept(MediaType.APPLICATION_JSON_VALUE))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.subtitle").value("세미나"))
        .andExpect(jsonPath("$.data.staticWriteTitleId").value(1))
        .andExpect(jsonPath("$.data.displayOrder").value(10))
        .andDo(document("aboutSubtitle-create",
            requestFields(
                fieldWithPath("subtitle").description("생성하고자 하는 페이지 블럭 서브타이틀의 제목"),
                fieldWithPath("staticWriteTitleId").description("생성하고자 하는 페이지 블럭 서브타이틀과 연결되는 페이지 블럭 타이틀의 ID"),
                fieldWithPath("thumbnailId").description("생성하고자 하는 페이지 블럭 서브타이틀과 연결되는 썸네일의 ID"),
                fieldWithPath("displayOrder").description("생성하고자 하는 페이지 블럭 서브타이틀이 보여지는 순서")
            ),
            responseFields(
                fieldWithPath("success").description("성공: true +\n실패: false"),
                fieldWithPath("code").description("성공 시 0을 반환"),
                fieldWithPath("msg").description("성공: 성공하였습니다 +\n실패: 에러 메세지 반환"),
                fieldWithPath("data.id").description("생성에 성공한 페이지 블럭 서브타이틀의 ID"),
                fieldWithPath("data.subtitle").description("생성에 성공한 페이지 블럭 서브타이틀의 제목"),
                fieldWithPath("data.staticWriteTitleId").description("생성에 성공한 페이지 블럭 서브타이틀과 연결되는 페이지 블럭 타이틀의 ID"),
                fieldWithPath("data.thumbnail.id").description("생성에 성공한 페이지 블럭 서브타이틀과 연결되는 썸네일의 ID"),
                fieldWithPath("data.displayOrder").description("생성에 성공한 페이지 블럭 서브타이틀이 보여지는 순서"),
                subsectionWithPath("data.staticWriteContentResults[]").description("생성에 성공한 페이지 블럭 서브타이틀과 연결된 페이지 블럭 컨텐츠의 리스트 데이터")
            )));

  }

  @Test
  @DisplayName("페이지 블럭 서브 타이틀 삭제 - 성공")
  public void deleteSubtitleSuccessById() throws Exception {
    mockMvc.perform(RestDocumentationRequestBuilders.delete("/v1/about/sub-tittle/delete/{id}",
                staticWriteSubtitleImageEntity.getId())
            .header("Authorization", adminToken))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andDo(document("aboutSubtitle-delete",
            pathParameters(
                parameterWithName("id").description("삭제하고자 하는 페이지 블럭 서브타이틀의 ID")
            ),
            responseFields(
                fieldWithPath("success").description("성공: true +\n실패: false"),
                fieldWithPath("code").description("성공 시 0을 반환"),
                fieldWithPath("msg").description("성공: 성공하였습니다 +\n실패: 에러 메세지 반환"),
                fieldWithPath("data.id").description("삭제에 성공한 페이지 블럭 서브타이틀의 ID"),
                fieldWithPath("data.subtitle").description("삭제에 성공한 페이지 블럭 서브타이틀의 제목"),
                fieldWithPath("data.staticWriteTitleId").description("삭제에 성공한 페이지 블럭 서브타이틀과 연결되는 페이지 블럭 타이틀의 ID"),
                fieldWithPath("data.thumbnail.id").description("삭제에 성공한 페이지 블럭 서브타이틀과 연결되는 썸네일의 ID"),
                fieldWithPath("data.displayOrder").description("삭제에 성공한 페이지 블럭 서브타이틀이 보여지던 순서"),
                subsectionWithPath("data.staticWriteContentResults[]").description("삭제에 성공한 페이지 블럭 서브타이틀과 연결된 페이지 블럭 컨텐츠의 리스트 데이터")
            )));
  }

  @Test
  @DisplayName("페이지 블럭 서브 타이틀 삭제 - 실패(존재하지 않는 ID)")
  public void deleteSubtitleFailById() throws Exception {
    mockMvc.perform(RestDocumentationRequestBuilders.delete("/v1/about/sub-tittle/delete/{id}",
                1234)
            .header("Authorization", adminToken))
        .andDo(print())
        .andExpect(status().is5xxServerError())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.msg").value("존재하지 않는 서브 타이틀 ID입니다."));
  }

  @Test
  @DisplayName("페이지 블럭 서브 타이틀 수정 - 성공")
  public void modifySubtitleSuccessById() throws Exception {
    String content = "{\n"
        + "\"subtitle\":\"" + "수정된 서브 타이틀" + "\",\n"
        + "\"staticWriteTitleId\":\"" + 2 + "\",\n"
        + "\"thumbnailId\":\"" + thumbnailEntity.getId() + "\",\n"
        + "\"displayOrder\":\"" + 5 + "\"\n"
        + "}";

    mockMvc.perform(RestDocumentationRequestBuilders.put("/v1/about/sub-tittle/modify/{id}",
                staticWriteSubtitleImageEntity.getId())
            .header("Authorization", adminToken)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(content)
            .accept(MediaType.APPLICATION_JSON_VALUE))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.staticWriteTitleId").value(2))
        .andExpect(jsonPath("$.data.thumbnail.id").value(thumbnailEntity.getId()))
        .andExpect(jsonPath("$.data.displayOrder").value(5))
        .andDo(document("aboutSubtitle-modify",
            pathParameters(
                parameterWithName("id").description("수정하고자 하는 페이지 블럭 서브타이틀의 ID")
            ),
            requestFields(
                fieldWithPath("subtitle").description("수정하고자 하는 페이지 블럭 서브타이틀의 제목(변하지 않을 시 기존 제목)"),
                fieldWithPath("staticWriteTitleId").description("수정하고자 하는 페이지 블럭 서브타이틀과 연결되는 페이지 블럭 타이틀의 ID(변하지 않을 시 기존 타이틀 ID)"),
                fieldWithPath("thumbnailId").description("수정하고자 하는 페이지 블럭 서브타이틀과 연결되는 썸네일의 ID(변하지 않을 시 기존 썸네일 ID)"),
                fieldWithPath("displayOrder").description("수정하고자 하는 페이지 블럭 서브타이틀이 보여지는 순서(변하지 않을 시 기존 보여지는 순서)")
            ),
            responseFields(
                fieldWithPath("success").description("성공: true +\n실패: false"),
                fieldWithPath("code").description("성공 시 0을 반환"),
                fieldWithPath("msg").description("성공: 성공하였습니다 +\n실패: 에러 메세지 반환"),
                fieldWithPath("data.id").description("수정에 성공한 페이지 블럭 서브타이틀의 ID"),
                fieldWithPath("data.subtitle").description("수정에 성공한 페이지 블럭 서브타이틀의 제목"),
                fieldWithPath("data.staticWriteTitleId").description("수정에 성공한 페이지 블럭 서브타이틀과 연결되는 페이지 블럭 타이틀의 ID"),
                fieldWithPath("data.thumbnail.id").description("수정에 성공한 페이지 블럭 서브타이틀과 연결되는 썸네일의 ID"),
                fieldWithPath("data.displayOrder").description("수정에 성공한 페이지 블럭 서브타이틀이 보여지는 순서"),
                subsectionWithPath("data.staticWriteContentResults[]").description("수정에 성공한 페이지 블럭 서브타이틀과 연결된 페이지 블럭 컨텐츠의 리스트 데이터")
            )));
  }

  @Test
  @DisplayName("페이지 블럭 서브 타이틀 수정 - 실패(존재하지 않는 서브 타이틀 ID)")
  public void modifySubtitleFailOneById() throws Exception {
    String content = "{\n"
        + "\"subtitle\":\"" + "수정된 서브 타이틀" + "\",\n"
        + "\"staticWriteTitleId\":\"" + 2 + "\",\n"
        + "\"thumbnailId\":\"" + thumbnailEntity.getId() + "\",\n"
        + "\"displayOrder\":\"" + 5 + "\"\n"
        + "}";

    mockMvc.perform(RestDocumentationRequestBuilders.put("/v1/about/sub-tittle/modify/{id}",
                1234)
            .header("Authorization", adminToken)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(content)
            .accept(MediaType.APPLICATION_JSON_VALUE))
        .andDo(print())
        .andExpect(status().is5xxServerError())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.msg").value("존재하지 않는 ID입니다."));
  }

  @Test
  @DisplayName("페이지 블럭 서브 타이틀 수정 - 실패(존재하지 않는 타이틀 ID)")
  public void modifySubtitleFailTwoById() throws Exception {
    String content = "{\n"
        + "\"subtitle\":\"" + "수정된 서브 타이틀" + "\",\n"
        + "\"staticWriteTitleId\":\"" + 1234 + "\",\n"
        + "\"thumbnailId\":\"" + thumbnailEntity.getId() + "\",\n"
        + "\"displayOrder\":\"" + 5 + "\"\n"
        + "}";

    mockMvc.perform(RestDocumentationRequestBuilders.put("/v1/about/sub-tittle/modify/{id}",
                staticWriteSubtitleImageEntity.getId())
            .header("Authorization", adminToken)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(content)
            .accept(MediaType.APPLICATION_JSON_VALUE))
        .andDo(print())
        .andExpect(status().is5xxServerError())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.msg").value("존재하지 않는 타이틀 ID입니다."));
  }

  @Test
  @DisplayName("페이지 블럭 서브 타이틀 수정 - 실패(존재하지 않는 썸네일 ID)")
  public void modifySubtitleFailThreeById() throws Exception {
    String content = "{\n"
        + "\"subtitle\":\"" + "수정된 서브 타이틀" + "\",\n"
        + "\"staticWriteTitleId\":\"" + 1 + "\",\n"
        + "\"thumbnailId\":\"" + 1234 + "\",\n"
        + "\"displayOrder\":\"" + 5 + "\"\n"
        + "}";

    mockMvc.perform(RestDocumentationRequestBuilders.put("/v1/about/sub-tittle/modify/{id}",
                staticWriteSubtitleImageEntity.getId())
            .header("Authorization", adminToken)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(content)
            .accept(MediaType.APPLICATION_JSON_VALUE))
        .andDo(print())
        .andExpect(status().is5xxServerError())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.msg").value("존재하지 않는 썸네일 ID입니다."));
  }
}
