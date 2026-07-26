package com.topaz.encurtador.model;
import lombok.*;
import java.time.LocalDateTime;
import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(of = "id")
@Entity
@Table(
        name = "TB_SHORT_URL",
        indexes = {
                @Index(name = "IDX_SHORT_CODE", columnList = "SHORT_CODE")
        }
)
public class ShortUrl {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "ORIGINAL_URL", nullable = false, length = 2048)
    private String originalUrl;

    @Column(name = "SHORT_CODE", nullable = false, unique = true, length = 100)
    private String shortCode;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public ShortUrl(String originalUrl, String shortCode) {
        this.originalUrl = originalUrl;
        this.shortCode = shortCode;
    }
}