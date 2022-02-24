package keeper.project.homepage.dto.library;

import java.time.LocalDateTime;
import java.util.Date;
import keeper.project.homepage.entity.library.BookEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookResult {

  private Long id;
  private String title;
  private String author;
  private String information;
  private String department;
  private Long total;
  private Long borrow;
  private Long enable;
  private Date registerDate;
  private Long thumbnailId;

  public void initWithEntity(BookEntity bookEntity) {
    this.id = bookEntity.getId();
    this.title = bookEntity.getTitle();
    this.author = bookEntity.getAuthor();
    this.information = bookEntity.getInformation();
    if (bookEntity.getDepartment() != null) {
      this.department = bookEntity.getDepartment().getName();
    }
    this.total = bookEntity.getTotal();
    this.borrow = bookEntity.getBorrow();
    this.enable = bookEntity.getEnable();
    this.registerDate = bookEntity.getRegisterDate();
    if (bookEntity.getThumbnailId() != null) {
      this.thumbnailId = bookEntity.getThumbnailId().getId();
    }

  }
}
