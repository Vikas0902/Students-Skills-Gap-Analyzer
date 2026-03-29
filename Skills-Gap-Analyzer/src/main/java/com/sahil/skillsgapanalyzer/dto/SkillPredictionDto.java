package com.sahil.skillsgapanalyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SkillPredictionDto {
    private String skill;
    private Double masteryScore;
    private String status;
    private String trend;
    private Double avgTime;
    private Integer totalHints;
    private Double Confidence;
}
