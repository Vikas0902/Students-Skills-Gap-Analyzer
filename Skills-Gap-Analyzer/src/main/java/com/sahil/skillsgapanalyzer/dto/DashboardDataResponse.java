package com.sahil.skillsgapanalyzer.dto;

import com.sahil.skillsgapanalyzer.entity.Student;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DashboardDataResponse {
    private StudentProfile profile;
    private OverviewMetrics overview;
    private List<SkillPredictionDto> skillsBreakdown;
    private ActionableInsights insights;

    public static class StudentProfile{
        public Integer totalAttempts;
        public String avgAccuracy;
        public String recentAccuracy;
    }

    public static class OverviewMetrics{
        public String predictedScore;
        public String modelConfidence;
        public Integer skillsGap;
        public Integer strongSkills;
    }

    public static class ActionableInsights{
        public List<String> workSkillsList;
        public List<String> stringSkillsList;
        public String learningPathRecommendation;
        public String performancePrediction;
    }

}
