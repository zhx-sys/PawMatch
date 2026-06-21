package com.pawmatch.dto.response;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class MatchedPetResponse extends PetResponse {

    private Integer matchScore;
    private Map<String, Integer> matchDetails;
}