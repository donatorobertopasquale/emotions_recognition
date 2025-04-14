package com.eyxpoliba.emotion_recognition.model;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "results")
public class ResultEntity {
    @Id
    private Long id;
    private Long userId; // FK verso UserEntity
    private String imageIdList;
    private String listDescriptionImages;
    private String listReactions;
    private String commentIA;
}