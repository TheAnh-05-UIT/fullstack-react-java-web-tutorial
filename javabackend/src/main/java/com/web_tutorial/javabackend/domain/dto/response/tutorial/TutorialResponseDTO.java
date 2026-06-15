package com.web_tutorial.javabackend.domain.dto.response.tutorial;

import com.web_tutorial.javabackend.domain.tutorial.TutorialStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TutorialResponseDTO {
    private Long id;
    private String title;
    private String slug;
    private String description;
    private String thumbnail;
    private TutorialStatus status;
    private Long viewCount;
    private Instant createdAt;
    private String createBy;
}
