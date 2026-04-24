package com.atm.service;

import com.atm.dto.DiagnosticsReport;
import com.atm.hardware.ATMHardwareController;
import com.atm.model.ATMCard;
import com.atm.model.Customer;
import com.atm.model.SystemAdministrator;
import com.atm.model.enums.TransactionStatus;
import com.atm.repository.ATMCardRepository;
import com.atm.repository.ATMSessionRepository;
import com.atm.repository.CustomerRepository;
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
    @Autowired private CustomerRepository customerRepository;

    public DiagnosticsReport runRemoteDiagnostics(String adminUsername) {
        SystemAdministrator admin = adminRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(23, 59, 59);

        long retainedCount = cardRepository.findAll().stream()
                .filter(ATMCard::isRetained).count();

        // SINGLETON: query the one hardware instance for live hardware status
        ATMHardwareController hardware = ATMHardwareController.getInstance();

        // BUILDER: construct DiagnosticsReport with a fluent chain instead of 10 setter calls.
        // Each line names exactly what it sets — far more readable than scattered setters.
        DiagnosticsReport report = new DiagnosticsReport.Builder()
                .runAt(LocalDateTime.now())
                .systemStatus("OPERATIONAL")
                .databaseStatus("CONNECTED")
                .networkStatus("ONLINE")
                .cashDispenserStatus(hardware.getCashDispenserStatus()) // live from Singleton
                .cardReaderStatus(hardware.getCardReaderStatus())       // live from Singleton
                .activeSessionCount(sessionRepository.count())
                .todayTransactionCount(transactionRepository.countByTimestampBetween(todayStart, todayEnd))
                .retainedCardCount(retainedCount)
                .firmwareVersion(admin.getFirmwareVersion())
                .build(); // BUILDER: terminal call — creates the final immutable DiagnosticsReport
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

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    @Transactional
    public void releaseCard(Long cardId) {
        cardRepository.findById(cardId).ifPresent(card -> {
            card.release();
            cardRepository.save(card);
        });
    }

    @Transactional
    public void retainCard(Long cardId) {
        cardRepository.findById(cardId).ifPresent(card -> {
            card.retain();
            cardRepository.save(card);
        });
    }
}
