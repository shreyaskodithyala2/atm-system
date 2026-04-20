package com.atm.service;

import com.atm.dto.DiagnosticsReport;
import com.atm.model.ATMCard;
import com.atm.model.SystemAdministrator;
import com.atm.model.enums.TransactionStatus;
import com.atm.repository.ATMCardRepository;
import com.atm.repository.ATMSessionRepository;
import com.atm.repository.SystemAdministratorRepository;
import com.atm.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminService {

    @Autowired private SystemAdministratorRepository adminRepository;
    @Autowired private ATMSessionRepository sessionRepository;
    @Autowired private TransactionRepository transactionRepository;
    @Autowired private ATMCardRepository cardRepository;

    public DiagnosticsReport runRemoteDiagnostics(String adminUsername) {
        SystemAdministrator admin = adminRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(23, 59, 59);

        long retainedCount = cardRepository.findAll().stream()
                .filter(ATMCard::isRetained).count();

        DiagnosticsReport report = new DiagnosticsReport();
        report.setRunAt(LocalDateTime.now());
        report.setSystemStatus("OPERATIONAL");
        report.setDatabaseStatus("CONNECTED");
        report.setNetworkStatus("ONLINE");
        report.setCashDispenserStatus("FUNCTIONAL");
        report.setCardReaderStatus("FUNCTIONAL");
        report.setActiveSessionCount(sessionRepository.count());
        report.setTodayTransactionCount(transactionRepository.countByTimestampBetween(todayStart, todayEnd));
        report.setRetainedCardCount(retainedCount);
        report.setFirmwareVersion(admin.getFirmwareVersion());
        return report;
    }

    @Transactional
    public boolean updateSystemFirmware(String adminUsername, String version) {
        SystemAdministrator admin = adminRepository.findByUsername(adminUsername)
                .orElse(null);
        if (admin == null) return false;
        boolean updated = admin.updateSystemFirmware(version);
        if (updated) adminRepository.save(admin);
        return updated;
    }

    public List<ATMCard> getRetainedCards() {
        return cardRepository.findAll().stream()
                .filter(ATMCard::isRetained).toList();
    }

    @Transactional
    public void releaseCard(Long cardId) {
        cardRepository.findById(cardId).ifPresent(card -> {
            card.release();
            cardRepository.save(card);
        });
    }
}
