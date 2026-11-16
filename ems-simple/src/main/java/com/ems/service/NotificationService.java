package com.ems.service;

import com.ems.dao.ReportDao;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class NotificationService {

    private final JavaMailSender mailSender;
    private final ReportDao reportDao;

    public NotificationService(JavaMailSender mailSender, ReportDao reportDao) {
        this.mailSender = mailSender;
        this.reportDao = reportDao;
    }

    public void sendStatusUpdateNotification(long reportId, String newStatus, String comment, String referenceNo) {
        // 1. Get teacher details
        Map<String, String> teacherInfo = reportDao.getTeacherInfoForReport(reportId);
        String email = teacherInfo.get("email");
        String name = teacherInfo.get("name");
        long teacherId = reportDao.getTeacherIdForReport(reportId);

        // 2. Create In-App Notification
        String inAppMessage = "Your report (" + referenceNo + ") status changed to " + newStatus + ". Admin note: " + comment;
        reportDao.createNotification(teacherId, reportId, inAppMessage);

        // 3. Send Email
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("RepairHub Update: Report " + referenceNo);
            message.setText("Hello " + name + ",\n\n"
                    + "The status of your maintenance report (" + referenceNo + ") has been updated to: " + newStatus + ".\n\n"
                    + "Administrator Comment: " + comment + "\n\n"
                    + "You can log in to RepairHub to view the full details.\n\n"
                    + "Regards,\nRepairHub Team");

            mailSender.send(message);
            System.out.println("Email sent successfully to " + email);
        } catch (Exception e) {
            System.err.println("Failed to send email to " + email + ": " + e.getMessage());
        }
    }
}