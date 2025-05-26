package com.eyxpoliba.emotion_recognition.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Entity
@Getter
@Setter
@AllArgsConstructor
@Table(name = "user_reactions")
public class ReactionsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private UserEntity userId;

    private String image;

    private String imageDescription;

    private String imageReaction;

    private String aiComment;

    public ReactionsEntity() {

    }
}
