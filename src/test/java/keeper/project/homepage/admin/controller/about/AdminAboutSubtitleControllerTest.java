package keeper.project.homepage.admin.controller.about;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.partWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.restdocs.request.RequestDocumentation.requestParts;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;
import java.io.FileInputStream;
import keeper.project.homepage.ApiControllerTestHelper;
import keeper.project.homepage.common.FileConversion;
import keeper.project.homepage.entity.FileEntity;
import keeper.project.homepage.entity.ThumbnailEntity;
import keeper.project.homepage.entity.etc.StaticWriteContentEntity;
import keeper.project.homepage.entity.etc.StaticWriteSubtitleImageEntity;
import keeper.project.homepage.entity.etc.StaticWriteTitleEntity;
import keeper.project.homepage.entity.member.MemberEntity;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.operation.preprocess.OperationRequestPreprocessor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Transactional
@Log4j2
public class AdminAboutSubtitleControllerTest extends ApiControllerTestHelper {

  private MemberEntity generalMember;
  private String generalToken;
  private MemberEntity adminMember;
  private String adminToken;

  private ThumbnailEntity thumbnailEntity;
  private FileEntity fileEntity;
  private StaticWriteTitleEntity staticWriteTitleEntity;
  private StaticWriteSubtitleImageEntity staticWriteSubtitleImageEntity;
  private StaticWriteContentEntity staticWriteContentEntity;

  private final String ipAddress1 = "127.0.0.1";

  @BeforeAll
  public static void createFile() {
    final String keeperFilesDirectoryPath = System.getProperty("user.dir") + File.separator
        + "keeper_files";
    final String thumbnailDirectoryPath = System.getProperty("user.dir") + File.separator
        + "keeper_files" + File.separator + "thumbnail";
    final String befUpdateImage = keeperFilesDirectoryPath + File.separator + "bef.jpg";
    final String befUpdateThumbnail = thumbnailDirectoryPath + File.separator + "thumb_bef.jpg";
    final String aftUpdateImage = keeperFilesDirectoryPath + File.separator + "aft.jpg";

    File keeperFilesDir = new File(keeperFilesDirectoryPath);
    File thumbnailDir = new File(thumbnailDirectoryPath);

    if (!keeperFilesDir.exists()) {
      keeperFilesDir.mkdir();
    }

    if (!thumbnailDir.exists()) {
      thumbnailDir.mkdir();
    }

    createImageForTest(befUpdateImage);
    createImageForTest(befUpdateThumbnail);
    createImageForTest(aftUpdateImage);
  }

  private static void createImageForTest(String filePath) {
    FileConversion fileConversion = new FileConversion();
    fileConversion.makeSampleJPEGImage(filePath);
  }

  @BeforeEach
  public void setUp() throws Exception {
    generalMember = generateMemberEntity(MemberJobName.회원, MemberTypeName.정회원, MemberRankName.일반회원);
    adminMember = generateMemberEntity(MemberJobName.회장, MemberTypeName.정회원, MemberRankName.우수회원);
    generalToken = generateJWTToken(generalMember.getLoginId(), memberPassword);
    adminToken = generateJWTToken(adminMember.getLoginId(), memberPassword);

    fileEntity = FileEntity.builder()
        .fileName("bef.jpg")
        .filePath("keeper_files" + File.separator + "bef.jpg")
        .fileSize(0L)
        .ipAddress(ipAddress1)
        .build();
    fileRepository.save(fileEntity);

    thumbnailEntity = ThumbnailEntity.builder()
        .path("keeper_files" + File.separator + "thumbnail" + File.separator + "thumb_bef.jpg")
        .file(fileEntity).build();
    thumbnailRepository.save(thumbnailEntity);

    staticWriteTitleEntity = generateTestTitle(1);
    staticWriteSubtitleImageEntity = generateTestSubtitle(1);
    staticWriteContentEntity = generateTestContent(1);

  }

  private String getFileName(String filePath) {
    File file = new File(filePath);
    return file.getName();
  }

  public StaticWriteTitleEntity generateTestTitle(Integer index) {
    StaticWriteTitleEntity staticWriteTitleEntity = StaticWriteTitleEntity.builder()
        .title("테스트 타이틀" + index)
        .type("테스트 타입" + index)
        .build();
    return staticWriteTitleRepository.save(staticWriteTitleEntity);
  }

  public StaticWriteSubtitleImageEntity generateTestSubtitle(Integer index) {
    StaticWriteSubtitleImageEntity staticWriteSubtitleImageEntity = StaticWriteSubtitleImageEntity.builder()
        .subtitle("테스트 서브 타이틀" + index)
        .displayOrder(index)
        .staticWriteTitle(staticWriteTitleEntity)
        .thumbnail(thumbnailEntity)
        .build();
    return staticWriteSubtitleImageRepository.save(staticWriteSubtitleImageEntity);
  }

  public StaticWriteContentEntity generateTestContent(Integer index) {
    StaticWriteContentEntity staticWriteContentEntity = StaticWriteContentEntity.builder()
        .content("테스트 컨텐츠" + index)
        .displayOrder(index)
        .staticWriteSubtitleImage(staticWriteSubtitleImageEntity)
        .build();
    return staticWriteContentRepository.save(staticWriteContentEntity);
  }

  @Test
  @DisplayName("페이지 블럭 서브 타이틀 생성 - 성공")
  public void createSubTitleSuccess() throws Exception {
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    MockMultipartFile image = new MockMultipartFile("thumbnail", "aft.jpg", "image/jpg",
        new FileInputStream(new File(
            System.getProperty("user.dir") + File.separator + "keeper_files" + File.separator
                + "aft.jpg")));

    params.add("subtitle", "테스트 서브 타이틀");
    params.add("staticWriteTitleId", "1");
    params.add("displayOrder", "1");
    params.add("ipAddress", "192.111.222");

    mockMvc.perform(multipart("/v1/admin/about/sub-title/create")
            .file(image)
            .params(params)
            .header("Authorization", adminToken)
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .with(request -> {
              request.setMethod("POST");
              return request;
            }))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andDo(document("aboutSubtitle-create",
            requestParameters(
                parameterWithName("subtitle").description("생성하고자 하는 페이지 블럭 서브 타이틀의 부제목"),
                parameterWithName("staticWriteTitleId").description("생성하고자 하는 페이지 블럭 서브 타이틀과 연결되는 페이지 블럭 타이틀의 ID"),
                parameterWithName("displayOrder").description("생성하고자 하는 페이지 블럭 서브 타이틀이 보여지는 순서"),
                parameterWithName("ipAddress").description("IP 주소")
            ),
            requestParts(
                partWithName("thumbnail").description(
                    "썸네일 용 이미지 (form-data 에서 thumbnail= parameter 부분)")
            ),
            responseFields(
                fieldWithPath("success").description("성공: true +\n실패: false"),
                fieldWithPath("code").description("성공 시 0을 반환"),
                fieldWithPath("msg").description("성공: 성공하였습니다 +\n실패: 에러 메세지 반환"),
                fieldWithPath("data.id").description("생성에 성공한 페이지 블럭 서브 타이틀의 ID"),
                fieldWithPath("data.subtitle").description("생성에 성공한 페이지 블럭 서브 타이틀의 부제목"),
                fieldWithPath("data.staticWriteTitleId").description("생성에 성공한 페이지 블럭 서브 타이틀과 연결된 페이지 블럭 타이틀의 ID"),
                subsectionWithPath("data.thumbnail").description("생성에 성공한 페이지 블럭 서브 타이틀과 연결된 썸네일 데이터"),
                fieldWithPath("data.displayOrder").description("생성에 성공한 페이지 블럭 서브 타이틀이 보여지는 순서"),
                subsectionWithPath("data.staticWriteContentResults[]").description("생성에 성공한 페이지 블럭 서브 타이틀과 연결된 페이지 블럭 컨텐츠 데이터 리스트")
            )));
  }

  @Test
  @DisplayName("페이지 블럭 서브 타이틀 생성 - 실패(권한 오류)")
  public void createSubTitleFailByAuth() throws Exception {
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    MockMultipartFile image = new MockMultipartFile("thumbnail", "aft.jpg", "image/jpg",
        new FileInputStream(new File(
            System.getProperty("user.dir") + File.separator + "keeper_files" + File.separator
                + "aft.jpg")));

    params.add("subtitle", "테스트 서브 타이틀");
    params.add("staticWriteTitleId", "1");
    params.add("displayOrder", "1");
    params.add("ipAddress", "192.111.222");

    mockMvc.perform(multipart("/v1/admin/about/sub-title/create")
            .file(image)
            .params(params)
            .header("Authorization", generalToken)
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .with(request -> {
              request.setMethod("POST");
              return request;
            }))
        .andDo(print())
        .andExpect(status().is4xxClientError())
        .andExpect(jsonPath("$.success").value(false));
  }

  @Test
  @DisplayName("페이지 블럭 서브 타이틀 생성 - 실패(존재하지 않는 타이틀)")
  public void createSubTitleFailByTitle() throws Exception {
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    MockMultipartFile image = new MockMultipartFile("thumbnail", "aft.jpg", "image/jpg",
        new FileInputStream(new File(
            System.getProperty("user.dir") + File.separator + "keeper_files" + File.separator
                + "aft.jpg")));

    params.add("subtitle", "테스트 서브 타이틀");
    params.add("staticWriteTitleId", "7");
    params.add("displayOrder", "1");
    params.add("ipAddress", "192.111.222");

    mockMvc.perform(multipart("/v1/admin/about/sub-title/create")
            .file(image)
            .params(params)
            .header("Authorization", adminToken)
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .with(request -> {
              request.setMethod("POST");
              return request;
            }))
        .andDo(print())
        .andExpect(status().is5xxServerError())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.msg").value("존재하지 않는 타이틀 ID 입니다."));
  }

  @Test
  @DisplayName("페이지 블럭 서브 타이틀 수정 - 성공")
  public void modifySubtitleByIdSuccess() throws Exception {
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    MockMultipartFile image = new MockMultipartFile("thumbnail", "aft.jpg", "image/jpg",
        new FileInputStream(new File(
            System.getProperty("user.dir") + File.separator + "keeper_files" + File.separator
                + "aft.jpg")));

    params.add("subtitle", "수정된 서브 타이틀");
    params.add("staticWriteTitleId", "1");
    params.add("displayOrder", "7");
    params.add("ipAddress", "192.111.777");

    mockMvc.perform(
            multipart("/v1/admin/about/sub-title/modify/{id}", staticWriteSubtitleImageEntity.getId())
                .file(image)
                .params(params)
                .header("Authorization", adminToken)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .with(request -> {
                  request.setMethod("PUT");
                  return request;
                }))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andDo(document("aboutSubtitle-modify",
            requestParameters(
                parameterWithName("subtitle").description("수정하고자 하는 페이지 블럭 서브 타이틀의 부제목(변하지 않을 시 기존값)"),
                parameterWithName("staticWriteTitleId").description("수정하고자 하는 페이지 블럭 서브 타이틀과 연결되는 페이지 블럭 타이틀의 ID(변하지 않을 시 기존값)"),
                parameterWithName("displayOrder").description("수정하고자 하는 페이지 블럭 서브 타이틀이 보여지는 순서(변하지 않을 시 기존값)"),
                parameterWithName("ipAddress").description("IP 주소(변하지 않을 시 기존값)")
            ),
            requestParts(
                partWithName("thumbnail").description(
                    "썸네일 용 이미지 (form-data 에서 thumbnail= parameter 부분) (변하지 않을 시 기존값)")
            ),
            responseFields(
                fieldWithPath("success").description("성공: true +\n실패: false"),
                fieldWithPath("code").description("성공 시 0을 반환"),
                fieldWithPath("msg").description("성공: 성공하였습니다 +\n실패: 에러 메세지 반환"),
                fieldWithPath("data.id").description("수정에 성공한 페이지 블럭 서브 타이틀의 ID"),
                fieldWithPath("data.subtitle").description("수정에 성공한 페이지 블럭 서브 타이틀의 부제목"),
                fieldWithPath("data.staticWriteTitleId").description("수정에 성공한 페이지 블럭 서브 타이틀과 연결된 페이지 블럭 타이틀의 ID"),
                subsectionWithPath("data.thumbnail").description("수정에 성공한 페이지 블럭 서브 타이틀과 연결된 썸네일 데이터"),
                fieldWithPath("data.displayOrder").description("수정에 성공한 페이지 블럭 서브 타이틀이 보여지는 순서"),
                subsectionWithPath("data.staticWriteContentResults[]").description("수정에 성공한 페이지 블럭 서브 타이틀과 연결된 페이지 블럭 컨텐츠 데이터 리스트")
            )));
  }

  @Test
  @DisplayName("페이지 블럭 서브 타이틀 수정 - 실패(존재하지 않는 서브 타이틀 ID)")
  public void modifySubtitleByIdFailById() throws Exception {
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    MockMultipartFile image = new MockMultipartFile("thumbnail", "aft.jpg", "image/jpg",
        new FileInputStream(new File(
            System.getProperty("user.dir") + File.separator + "keeper_files" + File.separator
                + "aft.jpg")));

    params.add("subtitle", "수정된 서브 타이틀");
    params.add("staticWriteTitleId", "1");
    params.add("displayOrder", "7");
    params.add("ipAddress", "192.111.777");

    mockMvc.perform(multipart("/v1/admin/about/sub-title/modify/{id}", 1234)
            .file(image)
            .params(params)
            .header("Authorization", adminToken)
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .with(request -> {
              request.setMethod("PUT");
              return request;
            }))
        .andDo(print())
        .andExpect(status().is5xxServerError())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.msg").value("존재하지 않는 서브 타이틀 ID 입니다."));
  }

  @Test
  @DisplayName("페이지 블럭 서브 타이틀 삭제 - 성공")
  public void deleteSubTitleByIdSuccess() throws Exception {
    mockMvc.perform(
            delete("/v1/admin/about/sub-title/delete/{id}", staticWriteSubtitleImageEntity.getId())
                .header("Authorization", adminToken))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andDo(document("aboutSubtitle-delete",
            pathParameters(
              parameterWithName("id").description("삭제하고자 하는 페이지 블럭 서브 타이틀의 ID")
            ),
            responseFields(
                fieldWithPath("success").description("성공: true +\n실패: false"),
                fieldWithPath("code").description("성공 시 0을 반환"),
                fieldWithPath("msg").description("성공: 성공하였습니다 +\n실패: 에러 메세지 반환"),
                fieldWithPath("data.id").description("삭제에 성공한 페이지 블럭 서브 타이틀의 ID"),
                fieldWithPath("data.subtitle").description("삭제에 성공한 페이지 블럭 서브 타이틀의 부제목"),
                fieldWithPath("data.staticWriteTitleId").description("삭제에 성공한 페이지 블럭 서브 타이틀과 연결된 페이지 블럭 타이틀의 ID"),
                subsectionWithPath("data.thumbnail").description("삭제에 성공한 페이지 블럭 서브 타이틀과 연결된 썸네일 데이터"),
                fieldWithPath("data.displayOrder").description("삭제에 성공한 페이지 블럭 서브 타이틀이 보여지는 순서"),
                subsectionWithPath("data.staticWriteContentResults[]").description("삭제에 성공한 페이지 블럭 서브 타이틀과 연결된 페이지 블럭 컨텐츠 데이터 리스트")
            )));
  }

  @Test
  @DisplayName("페이지 블럭 서브 타이틀 삭제 - 실패(존재하지 않는 서브 타이틀 ID)")
  public void deleteSubTitleByIdFailById() throws Exception {
    mockMvc.perform(
            delete("/v1/admin/about/sub-title/delete/{id}", 1234)
                .header("Authorization", adminToken))
        .andDo(print())
        .andExpect(status().is5xxServerError())
        .andExpect(jsonPath("$.success").value(false));
  }

}