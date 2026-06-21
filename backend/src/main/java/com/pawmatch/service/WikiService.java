package com.pawmatch.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.pawmatch.entity.WikiEntry;
import com.pawmatch.entity.WikiCategory;

import java.util.List;
import java.util.Map;

public interface WikiService extends IService<WikiEntry> {
    List<Map<String, Object>> getCategoryTree();
    WikiEntry getEntryDetail(Long entryId);
    IPage<Map<String, Object>> getEntryList(Integer pageNum, Integer pageSize, Long categoryId, String keyword, String sortBy);
    Long createEntry(String title, String summary, String content, Long categoryId, Long authorId, Integer userType);
    void editEntry(Long entryId, String title, String summary, String content, Long categoryId, Long userId, Integer userType, String editSummary);
    IPage<Map<String, Object>> getReviewList(Integer pageNum, Integer pageSize);
    void reviewEntry(Long entryId, boolean approved);
    Map<String, Object> markHelpful(Long entryId, Long userId);
    boolean checkHelpfulStatus(Long entryId, Long userId);
    List<Map<String, Object>> getRevisions(Long entryId);
    IPage<Map<String, Object>> getMyEntries(Long userId, Integer pageNum, Integer pageSize);
    void delistEntry(Long entryId, Long userId, Integer userType);
}