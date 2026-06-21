package com.pawmatch.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.pawmatch.dto.response.ReportResponse;
import com.pawmatch.entity.Comment;
import com.pawmatch.entity.Post;
import com.pawmatch.entity.Report;
import com.pawmatch.entity.User;
import com.pawmatch.mapper.CommentMapper;
import com.pawmatch.mapper.PostMapper;
import com.pawmatch.mapper.ReportMapper;
import com.pawmatch.mapper.UserMapper;
import com.pawmatch.service.ReportService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReportServiceImpl implements ReportService {

    private final ReportMapper reportMapper;
    private final PostMapper postMapper;
    private final CommentMapper commentMapper;
    private final UserMapper userMapper;
    private final CreditService creditService;

    public ReportServiceImpl(ReportMapper reportMapper, PostMapper postMapper,
                             CommentMapper commentMapper, UserMapper userMapper,
                             CreditService creditService) {
        this.reportMapper = reportMapper;
        this.postMapper = postMapper;
        this.commentMapper = commentMapper;
        this.userMapper = userMapper;
        this.creditService = creditService;
    }

    @Override
    public Report create(Report report) {
        report.setStatus(0);
        report.setCreateTime(LocalDateTime.now());
        reportMapper.insert(report);
        return report;
    }

    @Override
    public List<ReportResponse> getPending() {
        LambdaQueryWrapper<Report> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Report::getStatus, 0).orderByDesc(Report::getCreateTime);
        List<Report> reports = reportMapper.selectList(wrapper);

        List<ReportResponse> result = new ArrayList<>();
        for (Report r : reports) {
            ReportResponse resp = new ReportResponse();
            resp.setId(r.getId());
            resp.setReporterId(r.getReporterId());
            resp.setTargetType(r.getTargetType());
            resp.setTargetId(r.getTargetId());
            resp.setReason(r.getReason());
            resp.setStatus(r.getStatus());
            resp.setCreateTime(r.getCreateTime());

            // 查询举报人昵称
            if (r.getReporterId() != null) {
                User reporter = userMapper.selectById(r.getReporterId());
                if (reporter != null) {
                    resp.setReporterName(reporter.getNickname());
                }
            }

            // 查询被举报内容
            if ("POST".equalsIgnoreCase(r.getTargetType())) {
                Post post = postMapper.selectById(r.getTargetId());
                if (post != null) {
                    resp.setTargetTitle(post.getTitle());
                    resp.setTargetContent(post.getContent());
                }
            } else if ("COMMENT".equalsIgnoreCase(r.getTargetType())) {
                Comment comment = commentMapper.selectById(r.getTargetId());
                if (comment != null) {
                    resp.setTargetContent(comment.getContent());
                }
            }
            result.add(resp);
        }
        return result;
    }

    @Override
    public void review(Long reportId, Integer status) {
        Report report = reportMapper.selectById(reportId);
        if (report == null) {
            throw new com.pawmatch.exception.BusinessException(404, "举报不存在");
        }

        LambdaUpdateWrapper<Report> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Report::getId, reportId).set(Report::getStatus, status);
        reportMapper.update(null, wrapper);

        // 举报通过时扣被举报人信用分
        if (status == 1) {
            Long targetUserId = null;
            if ("POST".equalsIgnoreCase(report.getTargetType())) {
                Post post = postMapper.selectById(report.getTargetId());
                if (post != null) {
                    targetUserId = post.getUserId();
                }
            } else if ("COMMENT".equalsIgnoreCase(report.getTargetType())) {
                Comment comment = commentMapper.selectById(report.getTargetId());
                if (comment != null) {
                    targetUserId = comment.getUserId();
                }
            }
            if (targetUserId != null) {
                String detail = "POST".equalsIgnoreCase(report.getTargetType())
                        ? "被举报内容核实通过" : "被举报评论核实通过";
                creditService.changeCredit(targetUserId, 0, -20, "REPORTED", detail, reportId);
            }
        }
    }
}
