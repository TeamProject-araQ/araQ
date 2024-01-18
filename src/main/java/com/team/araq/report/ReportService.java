package com.team.araq.report;

import com.team.araq.user.SiteUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;

    public void createReport(String reason, String detailReason, SiteUser reportingUser, SiteUser reportedUser, String code) {
        Report report = new Report();
        report.setReason(reason);
        report.setDetailReason(detailReason);
        report.setReportingUser(reportingUser);
        report.setReportedUser(reportedUser);
        report.setReportDate(LocalDateTime.now());
        report.setStatus("처리 대기");
        report.setCode(code);
        this.reportRepository.save(report);
    }

    public Page<Report> getList(int page) {
        List<Sort.Order> sort = new ArrayList<>();
        sort.add(Sort.Order.desc("reportDate"));
        Pageable pageable = PageRequest.of(page, 10, Sort.by(sort));
        return this.reportRepository.findAll(pageable);
    }

    public Report getReport(Integer id) {
        Optional<Report> report = this.reportRepository.findById(id);
        if (report.isPresent()) return report.get();
        else throw new RuntimeException("그런 신고 없습니다.");
    }

    public void deleteReport(Report report) {
        this.reportRepository.delete(report);
    }

    public void updateStatus(Report report) {
        report.setStatus("처리 완료");
        this.reportRepository.save(report);
    }
}
