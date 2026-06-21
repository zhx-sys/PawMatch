package com.pawmatch.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pawmatch.dto.response.ApiResponse;
import com.pawmatch.entity.WikiEntry;
import com.pawmatch.security.PawMatchPrincipal;
import com.pawmatch.service.WikiService;
import com.pawmatch.exception.BusinessException;
import com.pawmatch.exception.ErrorCode;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wiki")
public class WikiController {

    private final WikiService wikiService;

    public WikiController(WikiService wikiService) {
        this.wikiService = wikiService;
    }

    @GetMapping("/categories")
    public ApiResponse<List<Map<String, Object>>> getCategories() {
        return ApiResponse.success(wikiService.getCategoryTree());
    }

    @GetMapping("/entry/{id}")
    public ApiResponse<WikiEntry> getEntryDetail(@PathVariable Long id) {
        WikiEntry entry = wikiService.getEntryDetail(id);
        return ApiResponse.success(entry);
    }

    @GetMapping("/entry/list")
    public ApiResponse<IPage<Map<String, Object>>> getEntryList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "newest") String sortBy) {
        return ApiResponse.success(wikiService.getEntryList(pageNum, pageSize, categoryId, keyword, sortBy));
    }

    @PostMapping("/entry")
    public ApiResponse<Long> createEntry(@RequestBody Map<String, Object> body) {
        PawMatchPrincipal principal = (PawMatchPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        String title = (String) body.get("title");
        String summary = (String) body.get("summary");
        String content = (String) body.get("content");
        Object catIdObj = body.get("categoryId");
        Long categoryId = catIdObj != null ? ((Number) catIdObj).longValue() : null;

        Long id = wikiService.createEntry(title, summary, content, categoryId, principal.getUserId(), principal.getUserType());
        String msg = principal.getUserType() == 1 ? "词条创建成功" : "词条已提交，等待审核";
        return ApiResponse.success(msg, id);
    }

    @PutMapping("/entry/{id}")
    public ApiResponse<Void> editEntry(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        PawMatchPrincipal principal = (PawMatchPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        String title = (String) body.get("title");
        String summary = (String) body.get("summary");
        String content = (String) body.get("content");
        Object catIdObj = body.get("categoryId");
        Long categoryId = catIdObj != null ? ((Number) catIdObj).longValue() : null;
        String editSummary = (String) body.get("editSummary");

        wikiService.editEntry(id, title, summary, content, categoryId, principal.getUserId(), principal.getUserType(), editSummary);
        String msg = principal.getUserType() == 1 ? "编辑成功" : "编辑已提交，等待审核";
        return ApiResponse.success(msg, null);
    }

    @GetMapping("/entry/review/list")
    public ApiResponse<IPage<Map<String, Object>>> getReviewList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PawMatchPrincipal principal = (PawMatchPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        if (principal.getUserType() != 1) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return ApiResponse.success(wikiService.getReviewList(pageNum, pageSize));
    }

    @PutMapping("/entry/{id}/review")
    public ApiResponse<Void> reviewEntry(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        PawMatchPrincipal principal = (PawMatchPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        if (principal.getUserType() != 1) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        Boolean approved = body.getOrDefault("approved", true);
        wikiService.reviewEntry(id, approved);
        return ApiResponse.success(approved ? "审核通过" : "已拒绝", null);
    }

    @PutMapping("/entry/{id}/helpful")
    public ApiResponse<Map<String, Object>> markHelpful(@PathVariable Long id) {
        PawMatchPrincipal principal = (PawMatchPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        Map<String, Object> result = wikiService.markHelpful(id, principal.getUserId());
        return ApiResponse.success(result);
    }

    @GetMapping("/entry/{id}/helpful-status")
    public ApiResponse<Boolean> checkHelpfulStatus(@PathVariable Long id) {
        PawMatchPrincipal principal = (PawMatchPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return ApiResponse.success(wikiService.checkHelpfulStatus(id, principal.getUserId()));
    }

@GetMapping("/entry/{id}/revisions")
    public ApiResponse<List<Map<String, Object>>> getRevisions(@PathVariable Long id) {
        return ApiResponse.success(wikiService.getRevisions(id));
    }

    @GetMapping("/entry/my/list")
    public ApiResponse<IPage<Map<String, Object>>> getMyEntries(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PawMatchPrincipal principal = (PawMatchPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return ApiResponse.success(wikiService.getMyEntries(principal.getUserId(), pageNum, pageSize));
    }

    @PutMapping("/entry/{id}/delist")
    public ApiResponse<Void> delistEntry(@PathVariable Long id) {
        PawMatchPrincipal principal = (PawMatchPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        wikiService.delistEntry(id, principal.getUserId(), principal.getUserType());
        return ApiResponse.success("词条已下架", null);
    }
}