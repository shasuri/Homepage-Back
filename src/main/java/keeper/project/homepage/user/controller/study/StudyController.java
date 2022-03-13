package keeper.project.homepage.user.controller.study;

import keeper.project.homepage.common.dto.result.ListResult;
import keeper.project.homepage.common.dto.result.SingleResult;
import keeper.project.homepage.common.service.ResponseService;
import keeper.project.homepage.user.dto.study.CreateStudyRequestDto;
import keeper.project.homepage.user.dto.study.StudyDto;
import keeper.project.homepage.user.dto.study.StudyYearSeasonDto;
import keeper.project.homepage.user.service.study.StudyService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Secured("ROLE_회원")
@RequestMapping("v1/study")
public class StudyController {

  private final StudyService studyService;
  private final ResponseService responseService;

  @GetMapping("/years")
  public ListResult<StudyYearSeasonDto> getYearsAndSeasons() {

    return responseService.getSuccessListResult(
        studyService.getAllStudyYearsAndSeasons());
  }

  @GetMapping("/list")
  public ListResult<StudyDto> getStudyList(
      @RequestParam Integer year,
      @RequestParam Integer season
  ) {

    return responseService.getSuccessListResult(
        studyService.getAllStudyList(year, season));
  }

  @PostMapping(value = "", consumes = "multipart/form-data")
  public SingleResult<StudyDto> createStudy(
      @ModelAttribute CreateStudyRequestDto createStudyRequest) {

    for (Long memberId : createStudyRequest.getMemberIdList()) {
      System.out.println(memberId);
    }
    return responseService.getSuccessSingleResult(
        studyService.createStudy(
            createStudyRequest.getStudyDto(),
            createStudyRequest.getThumbnail(),
            createStudyRequest.getMemberIdList()));
  }
}
