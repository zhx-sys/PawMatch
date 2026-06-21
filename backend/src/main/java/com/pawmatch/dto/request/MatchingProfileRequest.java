package com.pawmatch.dto.request;

import lombok.Data;

@Data
public class MatchingProfileRequest {

    private String livingSpace;
    private Boolean hasChildren;
    private Boolean hasOtherPets;
    private String petExperience;
    private String dailyRoutine;
    private String budgetRange;
    private String petPreference;
}