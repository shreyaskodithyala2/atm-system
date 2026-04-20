package com.atm.controller;

import com.atm.model.BankManager;
import com.atm.model.Transaction;
import com.atm.repository.BankManagerRepository;
import com.atm.service.ManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/staff/manager")
@PreAuthorize("hasRole('BANK_MANAGER')")
public class ManagerController {

    @Autowired private ManagerService managerService;
    @Autowired private BankManagerRepository managerRepository;

    private BankManager getManager(UserDetails userDetails) {
        return managerRepository.findByUsername(userDetails.getUsername()).orElseThrow();
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        model.addAttribute("manager", getManager(userDetails));
        model.addAttribute("pendingCount", managerService.getPendingCount());
        model.addAttribute("todayCount", managerService.getTodayTransactionCount());
        model.addAttribute("pendingTransactions", managerService.getPendingApprovals());
        return "manager/dashboard";
    }

    @GetMapping("/approvals")
    public String approvals(Model model) {
        model.addAttribute("pendingTransactions", managerService.getPendingApprovals());
        return "manager/approvals";
    }

    @GetMapping("/approval/{id}")
    public String approvalDetail(@PathVariable String id, Model model) {
        List<Transaction> pending = managerService.getPendingApprovals();
        Transaction tx = pending.stream()
                .filter(t -> t.getTransactionId().equals(id))
                .findFirst().orElse(null);
        if (tx == null) return "redirect:/staff/manager/approvals";
        model.addAttribute("transaction", tx);
        return "manager/approval-detail";
    }

    @PostMapping("/approve/{id}")
    public String approve(@PathVariable String id, RedirectAttributes ra) {
        boolean success = managerService.approveLargeTransaction(id);
        if (success) {
            ra.addFlashAttribute("message", "Transaction approved and processed successfully.");
        } else {
            ra.addFlashAttribute("error", "Approval failed — insufficient funds or invalid transaction.");
        }
        return "redirect:/staff/manager/approvals";
    }

    @PostMapping("/decline/{id}")
    public String decline(@PathVariable String id, RedirectAttributes ra) {
        managerService.declineLargeTransaction(id);
        ra.addFlashAttribute("message", "Transaction has been declined.");
        return "redirect:/staff/manager/approvals";
    }

    @GetMapping("/audit")
    public String auditForm(Model model) {
        model.addAttribute("date", LocalDate.now());
        model.addAttribute("transactions", managerService.auditDailyTransactions(LocalDate.now()));
        return "manager/audit";
    }

    @PostMapping("/audit")
    public String auditSubmit(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                              Model model) {
        List<Transaction> transactions = managerService.auditDailyTransactions(date);
        double totalAmount = transactions.stream().mapToDouble(Transaction::getAmount).sum();
        long pendingInAudit = transactions.stream()
                .filter(t -> t.getStatus().name().equals("AWAITING_APPROVAL")).count();
        model.addAttribute("date", date);
        model.addAttribute("transactions", transactions);
        model.addAttribute("totalAmount", totalAmount);
        model.addAttribute("totalCount", transactions.size());
        model.addAttribute("pendingInAudit", pendingInAudit);
        return "manager/audit";
    }
}
