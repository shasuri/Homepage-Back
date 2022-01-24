package keeper.project.homepage.service.sign;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import javax.transaction.Transactional;
import keeper.project.homepage.config.security.JwtTokenProvider;
import keeper.project.homepage.dto.EmailAuthDto;
import keeper.project.homepage.entity.member.MemberEntity;
import keeper.project.homepage.exception.CustomLoginIdSigninFailedException;
import keeper.project.homepage.repository.member.MemberRepository;
import keeper.project.homepage.service.mail.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Log4j2
public class SignInService {

  private static final int NEW_TEMPORARY_PASSWORD_LENGTH = 12;
  
  private final MemberRepository memberRepository;
  private final PasswordEncoder passwordEncoder;
  private final CustomPasswordService customPasswordService;
  private final JwtTokenProvider jwtTokenProvider;
  private final MailService mailService;

  public MemberEntity login(String loginId, String password) {
    MemberEntity memberEntity = memberRepository.findByLoginId(loginId)
        .orElseThrow(CustomLoginIdSigninFailedException::new);
    String hashedPassword = memberEntity.getPassword();
    if (!passwordMatches(password, hashedPassword)) {
      throw new CustomLoginIdSigninFailedException();
    }
    return memberEntity;
  }

  public String createJwtToken(MemberEntity memberEntity) {
    return "Bearer " + jwtTokenProvider.createToken(String.valueOf(memberEntity.getId()),
        memberEntity.getRoles());
  }

  public void findIdWithEmail(EmailAuthDto emailAuthDto) {
    Optional<MemberEntity> member = memberRepository.findByEmailAddress(
        emailAuthDto.getEmailAddress());
    if (member.isEmpty()) {
      throw new CustomLoginIdSigninFailedException("해당 이메일을 가진 유저가 존재하지 않습니다");
    }

    List<String> toUserList = new ArrayList<>(List.of(emailAuthDto.getEmailAddress()));
    String subject = "회원님의 KEEPER 아이디 입니다.";
    String text = "회원님의 KEEPER 아이디는 " + member.get().getLoginId() + " 입니다.";
    mailService.sendMail(toUserList, subject, text);
  }

  public void findPasswordWithEmail(EmailAuthDto emailAuthDto) {
    Optional<MemberEntity> member = memberRepository.findByEmailAddress(
        emailAuthDto.getEmailAddress());
    if (member.isEmpty()) {
      throw new CustomLoginIdSigninFailedException("해당 이메일을 가진 유저가 존재하지 않습니다");
    }

    String generatedAuthCode = generateRandomAuthCode(NEW_TEMPORARY_PASSWORD_LENGTH);
    member.get().changePassword(passwordEncoder.encode(generatedAuthCode));
    memberRepository.save(member.get());

    List<String> toUserList = new ArrayList<>(List.of(emailAuthDto.getEmailAddress()));
    String subject = "KEEPER 임시 비밀번호 발송 메일입니다.";
    String text = "KEEPER 임시 비밀번호를 발송해 드렸습니다.\n" +
        "KEEPER 임시 비밀번호는 " + generatedAuthCode + " 입니다.\n" +
        "반드시 비밀번호를 변경하시길 바랍니다.";
    mailService.sendMail(toUserList, subject, text);
  }

  @Transactional
  public void changePassword(String newPassword) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    Integer id = getIdFromAuth(authentication);
    MemberEntity memberEntity = memberRepository.findById(id)
        .orElseThrow(CustomLoginIdSigninFailedException::new);
    memberEntity.changePassword(passwordEncoder.encode(newPassword));
    memberRepository.save(memberEntity);
  }

  private boolean passwordMatches(String password, String hashedPassword) {
    return passwordEncoder.matches(password, hashedPassword)
        || customPasswordService.checkPasswordWithPBKDF2SHA256(password, hashedPassword)
        || customPasswordService.checkPasswordWithMD5(password, hashedPassword);
  }

  private Integer getIdFromAuth(Authentication authentication) {
    int id;
    try {
      id = Integer.parseInt(authentication.getName());
    } catch (NumberFormatException e) {
      throw new CustomLoginIdSigninFailedException("잘못된 JWT 토큰입니다.");
    }
    return id;
  }

  private String generateRandomAuthCode(int targetStringLength) {
    int leftLimit = 48; // numeral '0'
    int rightLimit = 122; // letter 'z'
    Random random = new Random();

    return random.ints(leftLimit, rightLimit + 1)
        .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
        .limit(targetStringLength)
        .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
        .toString();
    // 출처: https://www.baeldung.com/java-random-string
  }
}
