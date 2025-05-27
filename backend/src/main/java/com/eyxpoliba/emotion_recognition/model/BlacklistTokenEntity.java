package com.eyxpoliba.emotion_recognition.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;


@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "blacklist_tokens")
public class BlacklistTokenEntity {
    @Id
    private String jwt;

    private LocalDate expirationDate;
}
