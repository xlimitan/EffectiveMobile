package com.example.entity;

import com.example.validation.ValidBirthdayYear;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Setter
@Getter
@NoArgsConstructor
@NamedEntityGraph(
        name = "user_with_all_cards",
        attributeNodes = {
                @NamedAttributeNode("cards")
        }
)
@Table(name = "\"user\"")
public class User {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    @EqualsAndHashCode.Include
    private UUID id;

    @NotBlank(message = "Фамилия не может быть пустой")
    @Size(min = 1, max = 30, message = "Размер фамилии должен быть в диапазоне от 1 до 30 символов")
    private String surname;

    @NotBlank(message = "Имя не может быть пустым")
    @Size(min = 2, max = 15, message = "Размер имени должен быть в диапазоне от 2 до 15 символов")
    private String name;

    private String patronymic;

    @ValidBirthdayYear
    private int birthdayYear;

    @NotBlank(message = "Логин не может быть пустым")
    @Size(min = 1, max = 30, message = "Размер логина должен быть в диапазоне от 1 до 30 символов")
    @Column(unique = true, nullable = false)
    private String username;

    @NotBlank(message = "Пароль не может быть пустым")
    @Size(min = 1, max = 100, message = "Размер пароля должен быть в диапазоне от 1 до 100 символов")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String password;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @ManyToMany
    @JoinTable(
            name = "user_role",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Card> cards = new ArrayList<>();

    public User(String surname, String name, String patronymic, int birthdayYear, String username, String password) {
        this.surname = surname;
        this.name = name;
        this.patronymic = patronymic;
        this.birthdayYear = birthdayYear;
        this.username = username;
        this.password = password;
    }

    public void addRole(Role role){
        roles.add(role);
    }

    public void addCard(Card card){
        cards.add(card);
        card.setOwner(this);
    }
}
