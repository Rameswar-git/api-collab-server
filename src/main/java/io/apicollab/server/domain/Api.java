package io.apicollab.server.domain;

import io.apicollab.server.constant.ApiStatus;
import io.apicollab.server.mapper.ApiTagsConverter;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.bridge.builtin.EnumBridge;

import javax.persistence.*;
import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"id"})
@ToString(of = {"id", "name"})
@Indexed
public class Api extends BaseEntity {

    private static final long serialVersionUID = 8281554038825109184L;

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid2")
    protected String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String version;

    @Column(nullable = false, length = 255)
    private String description;

    @Column
    @Convert(converter = ApiTagsConverter.class)
    private List<String> tags;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Field(bridge=@FieldBridge(impl=EnumBridge.class))
    private ApiStatus status;

    @Column(nullable = false, length = 2000000) // 2 MB max
    @Basic(fetch = FetchType.LAZY)
    @Lob
    @Type(type="org.hibernate.type.StringType")
    @Field()
    private String swaggerDefinition;

    @ManyToOne
    private Application application;

    @PrePersist
    @PreUpdate
    void truncateDescription() {
        if(description.length() > 255) {
            description = description.substring(0, 255);
        }
    }
}
