package com.smartims.service.impl;

import com.smartims.entity.Issue;
import com.smartims.enums.IssueStatus;
import com.smartims.repository.SlaPolicyRepository;
import com.smartims.service.SlaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class SlaServiceImpl implements SlaService {

    private final SlaPolicyRepository slaPolicyRepository;

    @Override
    public void applySla(Issue issue) {
        issue.setSlaBreached(false);
    }

    @Override
    public void checkAndMarkBreach(Issue issue) {

        System.out.println("SLA check for issue " + issue.getId());

        if (issue.getCreatedAt() == null) {
            System.out.println("createdAt is NULL");
            return;
        }

        if (issue.getPriorityLevel() == null) {
            System.out.println("PriorityLevel is NULL");
            return;
        }

        if (issue.isSlaBreached()) {
            System.out.println("Already breached");
            return;
        }

        slaPolicyRepository.findByPriorityLevel(issue.getPriorityLevel())
                .ifPresentOrElse(policy -> {

                    long elapsedMinutes = Duration.between(
                            issue.getCreatedAt(),
                            LocalDateTime.now()
                    ).toMinutes();

                    System.out.println("⏱ Elapsed = " + elapsedMinutes);
                    System.out.println("⏱ SLA limit = " + policy.getResolutionTimeMinutes());

                    if (elapsedMinutes > policy.getResolutionTimeMinutes()) {
                        issue.setSlaBreached(true);
                        System.out.println("SLA BREACHED for issue " + issue.getId());
                    }
                }, () -> {
                    System.out.println("NO SLA POLICY FOUND");
                });
    }

//    @Override
//    public void checkAndMarkBreach(Issue issue) {
//
//        if (issue == null ||
//                issue.getCreatedAt() == null ||
//                issue.getPriorityLevel() == null ||
//                issue.isSlaBreached() ||
//                issue.getStatus() == IssueStatus.CLOSED) {
//            return;
//        }
//
//        slaPolicyRepository.findByPriorityLevel(issue.getPriorityLevel())
//                .ifPresent(policy -> {
//
//                    long elapsedMinutes = Duration.between(
//                            issue.getCreatedAt(),
//                            LocalDateTime.now()
//                    ).toMinutes();
//
//                    if (elapsedMinutes > policy.getResolutionTimeMinutes()) {
//                        issue.setSlaBreached(true);
//                    }
//                });
//    }
}
