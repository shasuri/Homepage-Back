package keeper.project.homepage.admin.controller.ctf;

import java.nio.file.AccessDeniedException;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import keeper.project.homepage.admin.dto.ctf.CtfChallengeAdminDto;
import keeper.project.homepage.admin.dto.ctf.CtfContestDto;
import keeper.project.homepage.admin.dto.ctf.CtfProbMakerDto;
import keeper.project.homepage.admin.dto.ctf.CtfSubmitLogDto;
import keeper.project.homepage.admin.service.ctf.CtfAdminService;
import keeper.project.homepage.common.dto.result.ListResult;
import keeper.project.homepage.common.dto.result.PageResult;
import keeper.project.homepage.common.dto.result.SingleResult;
import keeper.project.homepage.common.service.ResponseService;
import keeper.project.homepage.util.dto.FileDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/admin/ctf")
public class CtfAdminController {

  private final ResponseService responseService;
  private final CtfAdminService ctfAdminService;

  @Secured("ROLE_회장")
  @PostMapping("/contest")
  public SingleResult<CtfContestDto> createContest(@RequestBody CtfContestDto contestDto) {
    return responseService.getSuccessSingleResult(ctfAdminService.createContest(contestDto));
  }

  @Secured("ROLE_회장")
  @GetMapping("/contests")
  public ListResult<CtfContestDto> getContests() {
    List<CtfContestDto> contestList = ctfAdminService.getContests();
    return responseService.getSuccessListResult(contestList);
  }

  @Secured("ROLE_회장")
  @PatchMapping("/contest/{cid}/open")
  public SingleResult<CtfContestDto> openContest(@PathVariable("cid") Long challengeId) {
    return responseService.getSuccessSingleResult(ctfAdminService.openContest(challengeId));
  }

  @Secured("ROLE_회장")
  @PatchMapping("/contest/{cid}/close")
  public SingleResult<CtfContestDto> closeContest(@PathVariable("cid") Long challengeId) {
    return responseService.getSuccessSingleResult(ctfAdminService.closeContest(challengeId));
  }

  @Secured("ROLE_회장")
  @PostMapping("/prob/maker")
  public SingleResult<CtfProbMakerDto> designateProbMaker(
      @RequestBody CtfProbMakerDto probMakerDto) {
    return responseService.getSuccessSingleResult(ctfAdminService.designateProbMaker(probMakerDto));
  }

  @Secured({"ROLE_회장", "ROLE_출제자"})
  @PostMapping(value = "/prob")
  public SingleResult<CtfChallengeAdminDto> createProblem(
      @RequestBody CtfChallengeAdminDto challengeAdminDto) {
    return responseService.getSuccessSingleResult(
        ctfAdminService.createProblem(challengeAdminDto));
  }

  @Secured({"ROLE_회장", "ROLE_출제자"})
  @PostMapping(value = "/prob/file", consumes = "multipart/form-data")
  public SingleResult<FileDto> fileRegistrationInProblem(
      @RequestParam("file") MultipartFile file,
      @RequestParam("challengeId") Long challengeId, HttpServletRequest request) {
    return responseService.getSuccessSingleResult(
        ctfAdminService.fileRegistrationInProblem(challengeId, request, file));
  }

  @Secured({"ROLE_회장", "ROLE_출제자"})
  @PatchMapping("/prob/{pid}/open")
  public SingleResult<CtfChallengeAdminDto> openProblem(@PathVariable("pid") Long problemId) {
    return responseService.getSuccessSingleResult(ctfAdminService.openProblem(problemId));
  }

  @Secured({"ROLE_회장", "ROLE_출제자"})
  @PatchMapping("/prob/{pid}/close")
  public SingleResult<CtfChallengeAdminDto> closeProblem(@PathVariable("pid") Long problemId) {
    return responseService.getSuccessSingleResult(ctfAdminService.closeProblem(problemId));
  }

  @Secured({"ROLE_회장", "ROLE_출제자"})
  @DeleteMapping("/prob/{pid}")
  public SingleResult<CtfChallengeAdminDto> deleteProblem(@PathVariable("pid") Long problemId)
      throws AccessDeniedException {
    return responseService.getSuccessSingleResult(ctfAdminService.deleteProblem(problemId));
  }

  @Secured({"ROLE_회장", "ROLE_출제자"})
  @GetMapping("/prob")
  public ListResult<CtfChallengeAdminDto> getProblemList(@RequestParam Long ctfId) {
    return responseService.getSuccessListResult(ctfAdminService.getProblemList(ctfId));
  }

  @Secured({"ROLE_회장", "ROLE_출제자"})
  @GetMapping("/submit-log")
  public PageResult<CtfSubmitLogDto> getSubmitLogList(
      @PageableDefault(page = 0, size = 10) Pageable pageable) {
    return responseService.getSuccessPageResult(ctfAdminService.getSubmitLogList(pageable));
  }
}
