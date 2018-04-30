package io.apicollab.server.domain;

import lombok.*;

import javax.persistence.*;
import java.util.Collection;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"id"}, callSuper = true)
public class Application extends BaseEntity {

    private static final long serialVersionUID = -9029098147791424149L;

    @Column(nullable = false, unique = true)
    private String name;
    @Column(nullable = false)
    private String email;
    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Collection<Api> apis;

    @Builder
    private Application(String id, String name, String email) {
        super(id);
        this.name = name;
        this.email = email;
    }
}
