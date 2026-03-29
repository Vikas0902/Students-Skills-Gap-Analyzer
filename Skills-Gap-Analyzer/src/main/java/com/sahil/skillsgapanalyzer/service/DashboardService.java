package com.sahil.skillsgapanalyzer.service;

import com.sahil.skillsgapanalyzer.dto.DashboardDataResponse;
import com.sahil.skillsgapanalyzer.dto.MLPredictionResponse;
import com.sahil.skillsgapanalyzer.dto.MlPredictionRequest;
import com.sahil.skillsgapanalyzer.dto.SkillPredictionDto;
import com.sahil.skillsgapanalyzer.entity.Student;
import com.sahil.skillsgapanalyzer.entity.StudentAttempt;
import com.sahil.skillsgapanalyzer.integration.MlPredictionClient;
import com.sahil.skillsgapanalyzer.repository.StudentAttemptRepository;
import com.sahil.skillsgapanalyzer.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final StudentRepository studentRepository;
    private final StudentAttemptRepository studentAttemptRepository;
    private final MlPredictionClient mlPredictionClient;

    //    Constructor Injection
    public DashboardService(StudentRepository studentRepository,
                            StudentAttemptRepository studentAttemptRepository,
                            MlPredictionClient mlPredictionClient){
        this.studentRepository = studentRepository;
        this.studentAttemptRepository = studentAttemptRepository;
        this.mlPredictionClient = mlPredictionClient;
    }

    public DashboardDataResponse getStudentDashboard(Long studentId){
//        Fetching student and their attempt history:
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student with id: "+studentId+" not found!"));

        List<StudentAttempt> history = studentAttemptRepository.findByStudent_IdOrderByActionNumAsc(studentId);

        if(history.isEmpty()){
            throw new RuntimeException("No attempt history found for this student");
        }

//    --Calculate Overall Metrics
        int totalAttempts = history.size();
        long totalCorrect = history.stream().filter(a -> a.getCorrect() == 1).count();
        double overallAccuracy = (double) totalCorrect/totalAttempts;

//    --Calculate recent accuracy (last 3 attempts)
        int recentCount  = Math.min(3 , totalAttempts);
        List<StudentAttempt> recentAttempts = history.subList(totalAttempts - recentCount , totalAttempts);
        long recentCorrect = recentAttempts.stream().filter(a -> a.getCorrect() == 1).count();
        double recentAccuracy = (double) recentCorrect/recentCount;

//     --Group by skill to calculate specific mastery
        Map<String , List<StudentAttempt>> attemptsBySkill = history.stream().collect(Collectors.groupingBy(StudentAttempt::getSkill));

        List<SkillPredictionDto> skillPredictions = new ArrayList<>();
        List<String> weakSkills = new ArrayList<>();
        List<String> strongSkills = new ArrayList<>();
        double totalMasteryScore = 0.0;
        double totalConfidence = 0.0;

//      --Ask the ML model to predict the *next* attempt for each skill
        for(Map.Entry<String , List<StudentAttempt>> entry: attemptsBySkill.entrySet()){
            String skill = entry.getKey();
            List<StudentAttempt> skillHistory = entry.getValue();

            int skillAttempts = skillHistory.size();
            long skillCorrect = skillHistory.stream().filter(a -> a.getCorrect() == 1).count();
            double skillAccuracy = (double) skillCorrect/skillAttempts;
            double avgTime = skillHistory.stream().mapToDouble(StudentAttempt::getTimeTaken).average().orElse(0.0);
            int totalHints = skillHistory.stream().mapToInt(StudentAttempt::getHintCount).sum();

//      --build payload for flask api:
            MlPredictionRequest mlRequest = new MlPredictionRequest();
            mlRequest.skill_attempts = skillAttempts + 1; // Simulating the NEXT attempt
            mlRequest.skill_accuracy = skillAccuracy;
            mlRequest.total_attempts = totalAttempts + 1;
            mlRequest.overall_accuracy = overallAccuracy;
            mlRequest.recent_accuracy = recentAccuracy;
            mlRequest.hintCount = totalHints; // Using historical hints as context
            mlRequest.timeTaken = avgTime;    // Using historical avg time as context

//      --Make network call to flask:
            MLPredictionResponse mlResponse = mlPredictionClient.getPrediction(mlRequest);

//      --Map the ML response to frontend dto
            SkillPredictionDto dto = new SkillPredictionDto();
            dto.setSkill(skill);
            dto.setMasteryScore(mlResponse.mastery_score_percentage);
            dto.setConfidence(mlResponse.model_confidence);
            dto.setAvgTime((double) (Math.round(avgTime * 10.0)/10));
            dto.setTotalHints(totalHints);

//      --determine status based on probability
            double probab = mlResponse.predicted_probability;
            if(probab<0.60){
                dto.setStatus("Critical Weakness");
                weakSkills.add(skill);
            } else if (probab < 0.75){
                dto.setStatus("Needs Practice");
            } else if (probab < 0.85){
                dto.setStatus("Proficient");
            } else{
                dto.setStatus("Mastered");
                strongSkills.add(skill);
            }

//      --Determine Trend:
            if(recentAccuracy > overallAccuracy + 0.1) dto.setTrend("Improving 📈");
            else if (recentAccuracy < overallAccuracy - 0.1) dto.setTrend("Declining📉");
            else dto.setTrend("Stable");

            skillPredictions.add(dto);
            totalMasteryScore += mlResponse.mastery_score_percentage;
            totalConfidence += mlResponse.model_confidence;
        }

//      --Sort skills: lowest mastery first (prioritize weakness)
        skillPredictions.sort(Comparator.comparingDouble(SkillPredictionDto::getMasteryScore));

//      --Assemble the final dashboard response:
        DashboardDataResponse finalResponse = new DashboardDataResponse();

        DashboardDataResponse.StudentProfile profile = new DashboardDataResponse.StudentProfile();
        profile.totalAttempts = totalAttempts;
        profile.avgAccuracy = String.format("%.1f%%" , overallAccuracy*100);
        profile.recentAccuracy = String.format("%.1f%%" , recentAccuracy*100);
        finalResponse.setProfile(profile);

        DashboardDataResponse.OverviewMetrics overview = new DashboardDataResponse.OverviewMetrics();
        overview.skillsGap = weakSkills.size();
        overview.strongSkills = strongSkills.size();
        overview.predictedScore = String.format("%.1f%%" , totalMasteryScore / attemptsBySkill.size());
        overview.modelConfidence = String.format("%.1f%%" , totalConfidence / attemptsBySkill.size() *100);
        finalResponse.setOverview(overview);

        DashboardDataResponse.ActionableInsights insights = new DashboardDataResponse.ActionableInsights();
        insights.weakSkillsList = weakSkills;
        insights.strongSkillsList = strongSkills;

        if(!weakSkills.isEmpty()){
            insights.learningPathRecommendation = "Focus immediately on: " + weakSkills.get(0) + " .";
        } else{
            insights.learningPathRecommendation = "Great job! Proceed to new topics.";
        }

        insights.performancePrediction = recentAccuracy >= overallAccuracy ?
                "Student is performing Steadily or improving" : "Student has struggled recently.";
        finalResponse.setInsights(insights);
        finalResponse.setSkillsBreakdown(skillPredictions);

        return finalResponse;
    }
}
