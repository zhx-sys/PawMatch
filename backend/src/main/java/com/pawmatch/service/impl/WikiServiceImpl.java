package com.pawmatch.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pawmatch.entity.*;
import com.pawmatch.mapper.*;
import com.pawmatch.service.WikiService;
import com.pawmatch.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class WikiServiceImpl extends ServiceImpl<WikiEntryMapper, WikiEntry> implements WikiService {

    private final WikiCategoryMapper wikiCategoryMapper;
    private final WikiRevisionMapper wikiRevisionMapper;
    private final WikiContributionMapper wikiContributionMapper;
    private final UserMapper userMapper;
    private final WikiHelpfulRecordMapper wikiHelpfulRecordMapper;
    private final GrowthServiceImpl growthService;

    public WikiServiceImpl(WikiCategoryMapper wikiCategoryMapper, WikiRevisionMapper wikiRevisionMapper,
                           WikiContributionMapper wikiContributionMapper, UserMapper userMapper,
                           WikiHelpfulRecordMapper wikiHelpfulRecordMapper, GrowthServiceImpl growthService) {
        this.wikiCategoryMapper = wikiCategoryMapper;
        this.wikiRevisionMapper = wikiRevisionMapper;
        this.wikiContributionMapper = wikiContributionMapper;
        this.userMapper = userMapper;
        this.wikiHelpfulRecordMapper = wikiHelpfulRecordMapper;
        this.growthService = growthService;
    }

    @Override
    public List<Map<String, Object>> getCategoryTree() {
        List<WikiCategory> all = wikiCategoryMapper.selectList(
                Wrappers.lambdaQuery(WikiCategory.class).orderByAsc(WikiCategory::getSortOrder));
        Map<Long, List<WikiCategory>> childrenMap = new HashMap<>();
        List<WikiCategory> roots = new ArrayList<>();
        for (WikiCategory cat : all) {
            if (cat.getParentId() == null) {
                roots.add(cat);
            } else {
                childrenMap.computeIfAbsent(cat.getParentId(), k -> new ArrayList<>()).add(cat);
            }
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (WikiCategory root : roots) {
            Map<String, Object> node = new LinkedHashMap<>();
            node.put("id", root.getId());
            node.put("name", root.getName());
            node.put("sortOrder", root.getSortOrder());
            List<WikiCategory> children = childrenMap.getOrDefault(root.getId(), Collections.emptyList());
            List<Map<String, Object>> childNodes = children.stream().map(c -> {
                Map<String, Object> cn = new LinkedHashMap<>();
                cn.put("id", c.getId());
                cn.put("name", c.getName());
                cn.put("parentId", c.getParentId());
                cn.put("sortOrder", c.getSortOrder());
                return cn;
            }).collect(Collectors.toList());
            node.put("children", childNodes);
            result.add(node);
        }
        return result;
    }

    @Override
    public WikiEntry getEntryDetail(Long entryId) {
        WikiEntry entry = getById(entryId);
        if (entry == null || entry.getStatus() == 0) {
            throw new BusinessException(404, "词条不存在");
        }
        entry.setViewCount((entry.getViewCount() != null ? entry.getViewCount() : 0) + 1);
        updateById(entry);
        return entry;
    }

    @Override
    public IPage<Map<String, Object>> getEntryList(Integer pageNum, Integer pageSize, Long categoryId, String keyword, String sortBy) {
        Page<WikiEntry> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<WikiEntry> wrapper = Wrappers.lambdaQuery(WikiEntry.class)
                .eq(WikiEntry::getStatus, 1)
                .eq(categoryId != null, WikiEntry::getCategoryId, categoryId)
                .like(keyword != null && !keyword.isEmpty(), WikiEntry::getTitle, keyword);

        if ("helpful".equals(sortBy)) {
            wrapper.orderByDesc(WikiEntry::getHelpfulCount);
        } else {
            wrapper.orderByDesc(WikiEntry::getCreateTime);
        }
        IPage<WikiEntry> result = page(page, wrapper);

        Set<Long> userIds = result.getRecords().stream().map(WikiEntry::getAuthorId).collect(Collectors.toSet());
        Map<Long, String> nameMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            userMapper.selectList(Wrappers.lambdaQuery(User.class).in(User::getId, userIds))
                    .forEach(u -> nameMap.put(u.getId(), u.getNickname()));
        }
        // Also load category names
        Set<Long> catIds = result.getRecords().stream().map(WikiEntry::getCategoryId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> catNameMap = new HashMap<>();
        if (!catIds.isEmpty()) {
            wikiCategoryMapper.selectList(Wrappers.lambdaQuery(WikiCategory.class).in(WikiCategory::getId, catIds))
                    .forEach(c -> catNameMap.put(c.getId(), c.getName()));
        }

        IPage<Map<String, Object>> respPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        respPage.setRecords(result.getRecords().stream().map(e -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", e.getId());
            m.put("title", e.getTitle());
            m.put("summary", e.getSummary());
            m.put("categoryId", e.getCategoryId());
            m.put("categoryName", catNameMap.get(e.getCategoryId()));
            m.put("authorId", e.getAuthorId());
            m.put("authorName", nameMap.get(e.getAuthorId()));
            m.put("viewCount", e.getViewCount());
            m.put("helpfulCount", e.getHelpfulCount());
            m.put("createTime", e.getCreateTime());
            return m;
        }).collect(Collectors.toList()));
        return respPage;
    }

    @Override
    @Transactional
    public Long createEntry(String title, String summary, String content, Long categoryId, Long authorId, Integer userType) {
        WikiEntry entry = new WikiEntry();
        entry.setTitle(title);
        entry.setSummary(summary != null ? summary : "");
        entry.setContent(content);
        entry.setCategoryId(categoryId);
        entry.setAuthorId(authorId);
        entry.setViewCount(0);
        entry.setHelpfulCount(0);
        entry.setCreateTime(LocalDateTime.now());
        entry.setUpdateTime(LocalDateTime.now());
        // 救助站(userType=1)直接发布，普通用户(userType=0)待审核
        entry.setStatus(userType != null && userType == 1 ? 1 : 2);
        save(entry);

        // 记录贡献
        WikiContribution contrib = new WikiContribution();
        contrib.setUserId(authorId);
        contrib.setEntryId(entry.getId());
        contrib.setContribType("create");
        contrib.setPointsAwarded(0);
        contrib.setCreateTime(LocalDateTime.now());
        wikiContributionMapper.insert(contrib);

        return entry.getId();
    }

    @Override
    @Transactional
    public void editEntry(Long entryId, String title, String summary, String content, Long categoryId,
                          Long userId, Integer userType, String editSummary) {
        WikiEntry entry = getById(entryId);
        if (entry == null || entry.getStatus() == 0) {
            throw new BusinessException(404, "词条不存在");
        }
        // 记录编辑历史
        WikiRevision revision = new WikiRevision();
        revision.setEntryId(entryId);
        revision.setUserId(userId);
        revision.setContentBefore(entry.getContent());
        revision.setSummary(editSummary != null ? editSummary : "");
        revision.setCreateTime(LocalDateTime.now());

        // 非救助站编辑需要再次审核
        boolean isShelter = userType != null && userType == 1;
        if (!isShelter) {
            entry.setStatus(2);
        }
        entry.setTitle(title != null ? title : entry.getTitle());
        entry.setSummary(summary != null ? summary : entry.getSummary());
        entry.setContent(content != null ? content : entry.getContent());
        entry.setCategoryId(categoryId != null ? categoryId : entry.getCategoryId());
        entry.setUpdateTime(LocalDateTime.now());

        revision.setContentAfter(entry.getContent());
        wikiRevisionMapper.insert(revision);
        updateById(entry);

        // 记录贡献
        WikiContribution contrib = new WikiContribution();
        contrib.setUserId(userId);
        contrib.setEntryId(entryId);
        contrib.setContribType("edit");
        contrib.setPointsAwarded(0);
        contrib.setCreateTime(LocalDateTime.now());
        wikiContributionMapper.insert(contrib);

        // 积分：编辑百科词条+5
        growthService.awardPoints(userId, 5, "WIKI_EDIT", "编辑百科词条");
    }

    @Override
    public IPage<Map<String, Object>> getReviewList(Integer pageNum, Integer pageSize) {
        Page<WikiEntry> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<WikiEntry> wrapper = Wrappers.lambdaQuery(WikiEntry.class)
                .eq(WikiEntry::getStatus, 2)
                .orderByDesc(WikiEntry::getCreateTime);
        IPage<WikiEntry> result = page(page, wrapper);

        Set<Long> userIds = result.getRecords().stream().map(WikiEntry::getAuthorId).collect(Collectors.toSet());
        Map<Long, String> nameMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            userMapper.selectList(Wrappers.lambdaQuery(User.class).in(User::getId, userIds))
                    .forEach(u -> nameMap.put(u.getId(), u.getNickname()));
        }
        Set<Long> catIds = result.getRecords().stream().map(WikiEntry::getCategoryId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> catNameMap = new HashMap<>();
        if (!catIds.isEmpty()) {
            wikiCategoryMapper.selectList(Wrappers.lambdaQuery(WikiCategory.class).in(WikiCategory::getId, catIds))
                    .forEach(c -> catNameMap.put(c.getId(), c.getName()));
        }

        IPage<Map<String, Object>> respPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        respPage.setRecords(result.getRecords().stream().map(e -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", e.getId());
            m.put("title", e.getTitle());
            m.put("summary", e.getSummary());
            m.put("categoryName", catNameMap.get(e.getCategoryId()));
            m.put("authorName", nameMap.get(e.getAuthorId()));
            m.put("createTime", e.getCreateTime());
            return m;
        }).collect(Collectors.toList()));
        return respPage;
    }

    @Override
    @Transactional
    public void reviewEntry(Long entryId, boolean approved) {
        WikiEntry entry = getById(entryId);
        if (entry == null) {
            throw new BusinessException(404, "词条不存在");
        }
        if (entry.getStatus() != 2) {
            throw new BusinessException(400, "该词条不是待审核状态");
        }
        entry.setStatus(approved ? 1 : 0);
        entry.setUpdateTime(LocalDateTime.now());
        updateById(entry);

        // 积分：词条审核通过，作者+10
        if (approved) {
            growthService.awardPoints(entry.getAuthorId(), 10, "WIKI_APPROVED", "词条审核通过");
        }
    }

    @Override
    @Transactional
    public Map<String, Object> markHelpful(Long entryId, Long userId) {
        WikiEntry entry = getById(entryId);
        if (entry == null || entry.getStatus() != 1) {
            throw new BusinessException(404, "词条不存在");
        }

        WikiHelpfulRecord existing = wikiHelpfulRecordMapper.selectOne(
                Wrappers.lambdaQuery(WikiHelpfulRecord.class)
                        .eq(WikiHelpfulRecord::getEntryId, entryId)
                        .eq(WikiHelpfulRecord::getUserId, userId));

        Map<String, Object> result = new LinkedHashMap<>();
        if (existing != null) {
            wikiHelpfulRecordMapper.deleteById(existing.getId());
            entry.setHelpfulCount(Math.max(0, (entry.getHelpfulCount() != null ? entry.getHelpfulCount() : 0) - 1));
            updateById(entry);
            result.put("helpful", false);
            result.put("helpfulCount", entry.getHelpfulCount());
        } else {
            WikiHelpfulRecord record = new WikiHelpfulRecord();
            record.setEntryId(entryId);
            record.setUserId(userId);
            record.setCreateTime(LocalDateTime.now());
            wikiHelpfulRecordMapper.insert(record);

            entry.setHelpfulCount((entry.getHelpfulCount() != null ? entry.getHelpfulCount() : 0) + 1);
            updateById(entry);

            if (!entry.getAuthorId().equals(userId)) {
                growthService.awardPoints(entry.getAuthorId(), 2, "WIKI_HELPFUL", "词条被标记有帮助");
            }
            result.put("helpful", true);
            result.put("helpfulCount", entry.getHelpfulCount());
        }
        return result;
    }

    @Override
    public boolean checkHelpfulStatus(Long entryId, Long userId) {
        return wikiHelpfulRecordMapper.selectCount(
                Wrappers.lambdaQuery(WikiHelpfulRecord.class)
                        .eq(WikiHelpfulRecord::getEntryId, entryId)
                        .eq(WikiHelpfulRecord::getUserId, userId)) > 0;
    }

    @Override
    public List<Map<String, Object>> getRevisions(Long entryId) {
        List<WikiRevision> revisions = wikiRevisionMapper.selectList(
                Wrappers.lambdaQuery(WikiRevision.class)
                        .eq(WikiRevision::getEntryId, entryId)
                        .orderByDesc(WikiRevision::getCreateTime));

        Set<Long> userIds = revisions.stream().map(WikiRevision::getUserId).collect(Collectors.toSet());
        Map<Long, String> nameMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            userMapper.selectList(Wrappers.lambdaQuery(User.class).in(User::getId, userIds))
                    .forEach(u -> nameMap.put(u.getId(), u.getNickname()));
        }

        return revisions.stream().map(r -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", r.getId());
            m.put("userId", r.getUserId());
            m.put("userName", nameMap.get(r.getUserId()));
            m.put("summary", r.getSummary());
            m.put("createTime", r.getCreateTime());
            return m;
        }).collect(Collectors.toList());
    }

    @Override
    public IPage<Map<String, Object>> getMyEntries(Long userId, Integer pageNum, Integer pageSize) {
        Page<WikiEntry> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<WikiEntry> wrapper = Wrappers.lambdaQuery(WikiEntry.class)
                .eq(WikiEntry::getAuthorId, userId)
                .eq(WikiEntry::getStatus, 1)
                .orderByDesc(WikiEntry::getCreateTime);
        IPage<WikiEntry> result = page(page, wrapper);

        Set<Long> catIds = result.getRecords().stream().map(WikiEntry::getCategoryId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> catNameMap = new HashMap<>();
        if (!catIds.isEmpty()) {
            wikiCategoryMapper.selectList(Wrappers.lambdaQuery(WikiCategory.class).in(WikiCategory::getId, catIds))
                    .forEach(c -> catNameMap.put(c.getId(), c.getName()));
        }

        IPage<Map<String, Object>> respPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        respPage.setRecords(result.getRecords().stream().map(e -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", e.getId());
            m.put("title", e.getTitle());
            m.put("summary", e.getSummary());
            m.put("categoryName", catNameMap.get(e.getCategoryId()));
            m.put("viewCount", e.getViewCount());
            m.put("helpfulCount", e.getHelpfulCount());
            m.put("createTime", e.getCreateTime());
            return m;
        }).collect(Collectors.toList()));
        return respPage;
    }

    @Override
    @Transactional
    public void delistEntry(Long entryId, Long userId, Integer userType) {
        WikiEntry entry = getById(entryId);
        if (entry == null || entry.getStatus() == 0) {
            throw new BusinessException(404, "词条不存在");
        }
        // 救助站(userType=1)可下架任意词条，普通用户只能下架自己的词条
        if (!(userType != null && userType == 1) && !entry.getAuthorId().equals(userId)) {
            throw new BusinessException(403, "无权操作该词条");
        }
        if (entry.getStatus() != 1) {
            throw new BusinessException(400, "只能下架已发布的词条");
        }
        entry.setStatus(0);
        entry.setUpdateTime(LocalDateTime.now());
        updateById(entry);
    }
}