package com.pawmatch.service;

import com.pawmatch.entity.AdoptionFollowup;
import java.util.List;

public interface AdoptionFollowupService {
    AdoptionFollowup create(AdoptionFollowup followup);
    List<AdoptionFollowup> getByAdoptionId(Long adoptionId);
    List<AdoptionFollowup> getByShelterId(Long shelterId);
}
