package com.eyxpoliba.emotion_recognition.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "images")
@Getter
@Setter
public class ImageEntity {
    @Id
    private Long id;
    private String name;
    private String description;
    private String source;
}