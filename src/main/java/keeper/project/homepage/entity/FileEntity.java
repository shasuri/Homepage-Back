package keeper.project.homepage.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "file")
public class FileEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column
  private String fileName;
  @Column
  private String filePath;
  @Column
  private Long fileSize;
  @Column
  private Date uploadTime;
  @Column
  private String ipAddress;
  @ManyToOne(targetEntity = PostingEntity.class, fetch = FetchType.LAZY)
  @JoinColumn(name = "posting_id")
  @JsonIgnore
  private PostingEntity postingId;
}