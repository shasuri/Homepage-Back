package keeper.project.homepage.controller.library;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.partWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.restdocs.request.RequestDocumentation.requestParts;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.transaction.Transactional;
import keeper.project.homepage.ApiControllerTestHelper;
import keeper.project.homepage.ApiControllerTestSetUp;
import keeper.project.homepage.entity.posting.CommentEntity;
import keeper.project.homepage.entity.posting.PostingEntity;
import keeper.project.homepage.util.FileConversion;
import keeper.project.homepage.common.dto.result.SingleResult;
import keeper.project.homepage.common.dto.sign.SignInDto;
import keeper.project.homepage.entity.FileEntity;
import keeper.project.homepage.entity.ThumbnailEntity;
import keeper.project.homepage.entity.library.BookBorrowEntity;
import keeper.project.homepage.entity.library.BookEntity;
import keeper.project.homepage.entity.member.MemberEntity;
import keeper.project.homepage.entity.member.MemberHasMemberJobEntity;
import keeper.project.homepage.entity.member.MemberJobEntity;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

sealed class BookManageTestHelper extends ApiControllerTestHelper
    permits BookManageControllerTest {

  public String bookTitle1 = "점프 투 파이썬";
  public String bookAuthor1 = "박응용";

  public String bookTitle2 = "일반물리학";
  public String bookAuthor2 = "멋진타우렌";

  public String bookInformation = "우웩 우우웩";
  public Long bookDepartment = 0L;
  public Long epochTime = LocalDateTime.now().atZone(ZoneId.systemDefault()).toEpochSecond();
  public String userDirectory = System.getProperty("user.dir");
  public String createTestImage = "keeper_files" + File.separator + "createTest.jpg";

  public BookEntity generateBookEntity(String title, String author, String information, Long total,
      Long borrow,
      ThumbnailEntity thumbnailEntity) {
    Date registerDate = new Date();

    return bookRepository.save(
        BookEntity.builder()
            .title(title)
            .author(author)
            .information(information)
            .total(total)
            .borrow(borrow)
            .enable(total - borrow)
            .registerDate(registerDate)
            .thumbnailId(thumbnailEntity)
            .build());
  }

  public BookBorrowEntity generateBookBorrowEntity(MemberEntity memberId, BookEntity bookId,
      Long quantity,
      Date borrowDate, Date expireDate) {

    return bookBorrowRepository.save(
        BookBorrowEntity.builder()
            .member(memberId)
            .book(bookId)
            .quantity(quantity)
            .borrowDate(borrowDate)
            .expireDate(expireDate)
            .build());
  }

  public String getFileName(String filePath) {
    File file = new File(filePath);
    return file.getName();
  }
}

@Transactional
public non-sealed class BookManageControllerTest extends BookManageTestHelper {

  private String userToken;
  private String adminToken;

  private MemberEntity userEntity;
  private MemberEntity adminEntity;


  @BeforeEach
  public void setUp() throws Exception {
    userEntity = generateMemberEntity(MemberJobName.회원, MemberTypeName.정회원, MemberRankName.일반회원);
    userToken = generateJWTToken(userEntity);
    adminEntity = generateMemberEntity(MemberJobName.회장, MemberTypeName.정회원, MemberRankName.우수회원);
    adminToken = generateJWTToken(adminEntity);

    ThumbnailEntity thumbnailEntity = generateThumbnailEntity();

    BookEntity bookEntity1 = generateBookEntity("점프 투 파이썬", "박응용", "파이썬이 잘 설명된 책이다", 2L, 0L,
        thumbnailEntity);
    BookEntity bookEntity2 = generateBookEntity("일반물리학", "Randall D. Knight", "", 3L, 1L,
        thumbnailEntity);
    BookEntity bookEntity3 = generateBookEntity("일반물리학", "박재열", "내가 쓴 물리학책", 1L, 0L,
        thumbnailEntity);

    BookBorrowEntity bookBorrowEntity1 = generateBookBorrowEntity(userEntity, bookEntity1, 1L,
        java.sql.Date.valueOf(getDate(-17)), java.sql.Date.valueOf(getDate(-3)));

    BookBorrowEntity bookBorrowEntity2 = generateBookBorrowEntity(adminEntity, bookEntity1, 1L,
        java.sql.Date.valueOf(getDate(-8)), java.sql.Date.valueOf(getDate(1)));
  }

  private String getDate(Integer date) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(new Date());
    calendar.add(Calendar.DATE, date);

    return bookManageService.transferFormat(calendar.getTime());
  }

  //--------------------------도서 등록------------------------------------
  @Test
  @DisplayName("책 등록 성공(기존 책)")
  public void addBook() throws Exception {
    Long bookQuantity1 = 1L;
    bookDepartment = 1L;

    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    MockMultipartFile thumbnail = new MockMultipartFile("thumbnail", getFileName(createTestImage),
        "image/jpg", new FileInputStream(userDirectory + File.separator + createTestImage));

    params.add("title", bookTitle1);
    params.add("author", bookAuthor1);
    params.add("information", bookInformation);
    params.add("department", String.valueOf(bookDepartment));
    params.add("quantity", String.valueOf(bookQuantity1));

    mockMvc.perform(multipart("/v1/admin/addbook")
            .file(thumbnail)
            .header("Authorization", adminToken)
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .params(params)
            .with(request -> {
              request.setMethod("POST");
              return request;
            }))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.code").value(0))
        .andExpect(jsonPath("$.msg").exists())
        .andDo(document("add-book",
            requestParameters(
                parameterWithName("title").description("책 제목"),
                parameterWithName("author").description("저자"),
                parameterWithName("information").description("한줄평(없어도 됨)"),
                parameterWithName("department").description("도서 분류 코드"),
                parameterWithName("quantity").description("추가 할 수량")
            ),
            requestParts(
                partWithName("thumbnail").description(
                    "썸네일 용 이미지 (form-data 에서 thumbnail= parameter 부분)")
            ),
            responseFields(
                fieldWithPath("success").description("책 추가 완료 시 true, 실패 시 false 값을 보냅니다."),
                fieldWithPath("code").description("책 추가 완료 시 0, 수량 초과로 실패 시 -1 코드를 보냅니다."),
                fieldWithPath("msg").description("책 추가 실패가 수량 초과 일 때만 발생하므로 수량 초과 메시지를 발생시킵니다.")
            )));
  }

  @Test
  @DisplayName("수량 초과 책 등록 실패(기존 책)")
  public void addBookFailedOverMax() throws Exception {
    Long bookQuantity1 = 3L;
    bookDepartment = 1L;

    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("title", bookTitle1);
    params.add("author", bookAuthor1);
    params.add("department", String.valueOf(bookDepartment));
    params.add("quantity", String.valueOf(bookQuantity1));

    mockMvc.perform(post("/v1/admin/addbook")
            .params(params)
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .header("Authorization", adminToken))
        .andDo(print())
        .andExpect(status().is5xxServerError())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.code").value(-1))
        .andExpect(jsonPath("$.msg").exists());
  }

  @Test
  @DisplayName("새 책 등록 성공")
  public void addNewBook() throws Exception {
    Long bookQuantity2 = 4L;
    String newTitle = "일반물리학2";
    bookDepartment = 3L;

    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("title", newTitle);
    params.add("author", bookAuthor1);
    params.add("department", String.valueOf(bookDepartment));
    params.add("quantity", String.valueOf(bookQuantity2));

    mockMvc.perform(post("/v1/admin/addbook")
            .params(params)
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .header("Authorization", adminToken))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.code").value(0))
        .andExpect(jsonPath("$.msg").exists());
  }

  @Test
  @DisplayName("제목 같고 작가 다른 책 등록 성공")
  public void addNewBookSameTitle() throws Exception {
    Long bookQuantity2 = 4L;
    String newTitle = "Do it! 점프 투 파이썬";
    String newAuthor = "박재열";
    bookDepartment = 1L;

    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("title", newTitle);
    params.add("author", newAuthor);
    params.add("department", String.valueOf(bookDepartment));
    params.add("quantity", String.valueOf(bookQuantity2));

    mockMvc.perform(post("/v1/admin/addbook")
            .params(params)
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .header("Authorization", adminToken))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.code").value(0))
        .andExpect(jsonPath("$.msg").exists());
  }

  @Test
  @DisplayName("수량 초과 새 책 등록 실패")
  public void addNewBookFailedOverMax() throws Exception {
    Long bookQuantity3 = 5L;
    bookDepartment = 1L;

    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("title", bookTitle1 + epochTime);
    params.add("author", bookAuthor1);
    params.add("department", String.valueOf(bookDepartment));
    params.add("quantity", String.valueOf(bookQuantity3));

    mockMvc.perform(post("/v1/admin/addbook")
            .params(params)
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .header("Authorization", adminToken))
        .andDo(print())
        .andExpect(status().is5xxServerError())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.code").value(-1))
        .andExpect(jsonPath("$.msg").exists());
  }

  //--------------------------도서 삭제------------------------------------
  @Test
  @DisplayName("책 삭제 성공(일부 삭제)")
  public void deleteBook() throws Exception {
    Long bookQuantity1 = 1L;

    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("title", bookTitle1);
    params.add("author", bookAuthor1);
    params.add("quantity", String.valueOf(bookQuantity1));

    mockMvc.perform(post("/v1/admin/deletebook")
            .params(params)
            .header("Authorization", adminToken))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.code").value(0))
        .andExpect(jsonPath("$.msg").exists())
        .andDo(document("delete-book",
            requestParameters(
                parameterWithName("title").description("책 제목"),
                parameterWithName("author").description("책 저자"),
                parameterWithName("quantity").description("삭제 할 수량")
            ),
            responseFields(
                fieldWithPath("success").description("책 삭제 완료 시 true, 실패 시 false 값을 보냅니다."),
                fieldWithPath("code").description(
                    "책 삭제 완료 시 0, 최대 수량 초과로 실패 시 -1, 없는 책으로 실패 시 -2 코드를 보냅니다."),
                fieldWithPath("msg").description(
                    "책 삭제 실패가 수량 초과 일 때 수량 초과 메시지를, 없는 책일 때 책이 없다는 메시지를 발생시킵니다.")
            )));
  }

  @Test
  @DisplayName("책 삭제 성공(전체 삭제)")
  public void deleteBookMax() throws Exception {
    Long bookQuantity3 = 3L;

    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("title", bookTitle1);
    params.add("author", bookAuthor1);
    params.add("quantity", String.valueOf(bookQuantity3));

    mockMvc.perform(post("/v1/admin/deletebook")
            .params(params)
            .header("Authorization", adminToken))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.code").value(0))
        .andExpect(jsonPath("$.msg").exists());
  }

  @Test
  @DisplayName("책 삭제 실패(없는 책)")
  public void deleteBookFailedNoExist() throws Exception {
    Long bookQuantity3 = 1L;

    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("title", bookTitle1 + epochTime);
    params.add("author", bookAuthor1 + epochTime);
    params.add("quantity", String.valueOf(bookQuantity3));

    mockMvc.perform(post("/v1/admin/deletebook")
            .params(params)
            .header("Authorization", adminToken))
        .andDo(print())
        .andExpect(status().is5xxServerError())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.code").value(-2))
        .andExpect(jsonPath("$.msg").exists());
  }

  @Test
  @DisplayName("책 삭제 실패(기존보다 많은 수량-total기준)")
  public void deleteBookFailedOverMax1() throws Exception {
    Long bookQuantity3 = 5L;

    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("title", bookTitle1);
    params.add("author", bookAuthor1);
    params.add("quantity", String.valueOf(bookQuantity3));

    mockMvc.perform(post("/v1/admin/deletebook")
            .params(params)
            .header("Authorization", adminToken))
        .andDo(print())
        .andExpect(status().is5xxServerError())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.code").value(-1))
        .andExpect(jsonPath("$.msg").exists());
  }

  @Test
  @DisplayName("책 삭제 실패(기존보다 많은 수량-enable기준)")
  public void deleteBookFailedOverMax2() throws Exception {
    Long bookQuantity3 = 2L;

    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("title", bookTitle2);
    params.add("author", bookAuthor2);
    params.add("quantity", String.valueOf(bookQuantity3));

    mockMvc.perform(post("/v1/admin/deletebook")
            .params(params)
            .header("Authorization", adminToken))
        .andDo(print())
        .andExpect(status().is5xxServerError())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.code").value(-1))
        .andExpect(jsonPath("$.msg").exists());
  }

  //--------------------------도서 대여------------------------------------
  @Test
  @DisplayName("책 대여 성공")
  public void borrowBook() throws Exception {
    Long borrowQuantity = 2L;

    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("title", bookTitle1);
    params.add("author", bookAuthor1);
    params.add("quantity", String.valueOf(borrowQuantity));

    mockMvc.perform(post("/v1/admin/borrowbook").params(params).header("Authorization", userToken))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.code").value(0))
        .andExpect(jsonPath("$.msg").exists())
        .andDo(document("borrow-book",
            requestParameters(
                parameterWithName("title").description("책 제목"),
                parameterWithName("author").description("저자"),
                parameterWithName("quantity").description("대여 할 수량")
            ),
            responseFields(
                fieldWithPath("success").description("책 대여 완료 시 true, 실패 시 false 값을 보냅니다."),
                fieldWithPath("code").description(
                    "책 대여 완료 시 0, 수량 초과로 실패 시 -1, 존재하지 않을 시 -2 코드를 보냅니다."),
                fieldWithPath("msg").description(
                    "책 대여 실패가 수량 초과 일 때 수량 초과 메시지를, 없는 책일 때 책이 없다는 메시지를 발생시킵니다.")
            )));
  }

  @Test
  @DisplayName("책 대여 실패(수량 초과)")
  public void borrowBookFailedOverMax() throws Exception {
    Long borrowQuantity = 2L;

    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("title", bookTitle2);
    params.add("author", bookAuthor2);
    params.add("quantity", String.valueOf(borrowQuantity));

    mockMvc.perform(post("/v1/admin/borrowbook").params(params).header("Authorization", userToken))
        .andDo(print())
        .andExpect(status().is5xxServerError())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.code").value(-1))
        .andExpect(jsonPath("$.msg").exists());
  }

  @Test
  @DisplayName("책 대여 실패(없는 책)")
  public void borrowBookFailedNotExist() throws Exception {
    Long borrowQuantity = 1L;

    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("title", bookTitle2 + epochTime);
    params.add("author", bookAuthor2);
    params.add("quantity", String.valueOf(borrowQuantity));

    mockMvc.perform(post("/v1/admin/borrowbook").params(params).header("Authorization", userToken))
        .andDo(print())
        .andExpect(status().is5xxServerError())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.code").value(-2))
        .andExpect(jsonPath("$.msg").exists());
  }

  //--------------------------도서 반납------------------------------------
  @Test
  @DisplayName("책 반납 성공(전부 반납)")
  public void returnBookAll() throws Exception {
    Long returnQuantity = 1L;

    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("title", bookTitle1);
    params.add("author", bookAuthor1);
    params.add("quantity", String.valueOf(returnQuantity));

    mockMvc.perform(post("/v1/admin/returnbook")
            .params(params)
            .header("Authorization", userToken))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.code").value(0))
        .andExpect(jsonPath("$.msg").exists())
        .andDo(document("return-book",
            requestParameters(
                parameterWithName("title").description("책 제목"),
                parameterWithName("author").description("저자"),
                parameterWithName("quantity").description("반납 할 수량")
            ),
            responseFields(
                fieldWithPath("success").description("책 반납 완료 시 true, 실패 시 false 값을 보냅니다."),
                fieldWithPath("code").description(
                    "책 반납 완료 시 0, 수량 초과로 실패 시 -1, 존재하지 않을 시 -2 코드를 보냅니다."),
                fieldWithPath("msg").description(
                    "책 반납 실패가 수량 초과 일 때 수량 초과 메시지를, 없는 책일 때 책이 없다는 메시지를 발생시킵니다.")
            )));
  }

  @Test
  @DisplayName("책 반납 성공(일부 반납)")
  public void returnBookPart() throws Exception {
    Long returnQuantity = 1L;

    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("title", bookTitle1);
    params.add("author", bookAuthor1);
    params.add("quantity", String.valueOf(returnQuantity));

    mockMvc.perform(post("/v1/admin/returnbook")
            .params(params)
            .header("Authorization", userToken))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.code").value(0))
        .andExpect(jsonPath("$.msg").exists());
  }

  @Test
  @DisplayName("책 반납 실패(수량 초과)")
  public void returnBookFailedOverMax() throws Exception {
    Long borrowQuantity = 3L;

    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("title", bookTitle1);
    params.add("author", bookAuthor1);
    params.add("quantity", String.valueOf(borrowQuantity));

    mockMvc.perform(post("/v1/admin/returnbook")
            .params(params)
            .header("Authorization", userToken))
        .andDo(print())
        .andExpect(status().is5xxServerError())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.code").value(-1))
        .andExpect(jsonPath("$.msg").exists());
  }

  @Test
  @DisplayName("책 반납 실패(없는 책)")
  public void returnBookFailedNotExist() throws Exception {
    Long borrowQuantity = 1L;

    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("title", bookTitle2 + epochTime);
    params.add("author", bookAuthor2);
    params.add("quantity", String.valueOf(borrowQuantity));

    mockMvc.perform(post("/v1/admin/returnbook")
            .params(params)
            .header("Authorization", userToken))
        .andDo(print())
        .andExpect(status().is5xxServerError())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.code").value(-2))
        .andExpect(jsonPath("$.msg").exists());
  }

  @Test
  @DisplayName("책 반납 실패(대출 안 한 책)")
  public void returnBookFailedNotBorrowExist() throws Exception {
    Long borrowQuantity = 1L;

    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("title", bookTitle2);
    params.add("author", bookAuthor2);
    params.add("quantity", String.valueOf(borrowQuantity));

    mockMvc.perform(post("/v1/admin/returnbook")
            .params(params)
            .header("Authorization", userToken))
        .andDo(print())
        .andExpect(status().is5xxServerError())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.code").value(-3))
        .andExpect(jsonPath("$.msg").exists());
  }

  //--------------------------연체 도서 표시------------------------------------
  @Test
  @DisplayName("연체 도서 표시(연체, 3일전)")
  public void sendOverdueBooks() throws Exception {
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

    mockMvc.perform(get("/v1/admin/overduebooks")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", adminToken))
        .andDo(print())
        .andExpect(status().isOk())
        .andDo(document("overdue-books",
            requestParameters(
                parameterWithName("page").optional().description("페이지 번호(default = 0)"),
                parameterWithName("size").optional().description("한 페이지당 출력 수(default = 10)")
            ),
            responseFields(
                fieldWithPath("success").description("연체 도서 전달 시 true, 실패 시 false 값을 보냅니다."),
                fieldWithPath("code").description(
                    "연체 도서 전달 성공 시 0, 실패 시 -11 코드를 보냅니다."),
                fieldWithPath("msg").description(
                    "연체 도서 전달 실패 시 연체 도서가 없다는 메시지를 전달합니다.")
            ).andWithPrefix("list.", fieldWithPath("[].id").description("대여정보 ID"),
                subsectionWithPath("[].member").description("대여자 ID"),
                subsectionWithPath("[].book").description("책 ID"),
                fieldWithPath("[].quantity").description("대여 수량"),
                fieldWithPath("[].borrowDate").description("대여일"),
                fieldWithPath("[].expireDate").description("만기일"))));
  }
}
