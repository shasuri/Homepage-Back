package keeper.project.homepage.admin.crawler;

import java.util.List;

public class CrawlerSettings {

  // NOTE: 좋은 설정파일 방식 있으면 바꾸기
  public static final List<String> TITLES = List.of("보안뉴스", "ItWorld", "콘텐스트 코리아",
      "WEVITY (공모전 대외활동)", "ItFind");
  public static final List<String> ELEMENT_QUERIES = List.of(
      "body > dl > dd > section > ul:not(:nth-child(1)) > li > a", "div.node-list > div h5 > a",
      "#frm > div > div.list_style_2 > ul > li",
      ".list > :not(:nth-child(1))", "tbody > tr > td > a");
  public static final List<String> TITLE_QUERIES = List.of("", "", "span.txt", "a", "");
  public static final List<String> LINK_QUERIES = List.of("", "", "a", "a", "");
  public static final List<String> URLS = List.of(
      "https://m.boannews.com/html/", "https://www.itworld.co.kr/t/36/%EB%B3%B4%EC%95%88",
      "https://www.contestkorea.com/sub/list.php?int_gbn=1&Txt_bcode=030510001",
      "https://www.wevity.com/?c=find&s=1&gub=1&cidx=21&sp=&sw=&gbn=list&mode=ing",
      "https://www.itfind.or.kr/data/seminar/list.do?pageSize=10&boardParam1=&searchTarget2=&pageIndex=0");
  public static final List<Integer> URL_ENDS = List.of(URLS.get(0).length(), 25, 33, 23, 24);
}
