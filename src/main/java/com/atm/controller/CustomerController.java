package com.atm.controller;

import com.atm.model.*;
import com.atm.model.enums.TransactionStatus;
import com.atm.repository.TransactionRepository;
import com.atm.service.TransactionService;
import com.atm.service.TransactionService.TransactionResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/atm/session")
public class CustomerController {

    @Autowired private TransactionService transactionService;
    @Autowired private TransactionRepository transactionRepository;

    private ATMSession getSession(HttpServletRequest request) {
        return (ATMSession) request.getAttribute("ATM_SESSION");
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpServletRequest request, Model model) {
        ATMSession session = getSession(request);
        model.addAttribute("customer", session.getCustomer());
        model.addAttribute("account", session.getCustomer().getAccount());
        return "customer/dashboard";
    }

    @GetMapping("/balance")
    public String balance(HttpServletRequest request, Model model) {
        ATMSession session = getSession(request);
        model.addAttribute("customer", session.getCustomer());
        model.addAttribute("account", session.getCustomer().getAccount());
        return "customer/balance";
    }

    @GetMapping("/withdraw")
    public String showWithdraw(HttpServletRequest request, Model model) {
        ATMSession session = getSession(request);
        model.addAttribute("account", session.getCustomer().getAccount());
        return "customer/withdraw";
    }

    @PostMapping("/withdraw")
    public String processWithdraw(@RequestParam double amount,
                                  HttpServletRequest request,
                                  RedirectAttributes ra) {
        ATMSession session = getSession(request);
        TransactionResult result = transactionService.processWithdrawal(session.getSessionId(), amount);
        return handleTransactionResult(result, session.getSessionId(), ra, "withdraw");
    }

    @GetMapping("/deposit")
    public String showDeposit(HttpServletRequest request, Model model) {
        ATMSession session = getSession(request);
        model.addAttribute("account", session.getCustomer().getAccount());
        return "customer/deposit";
    }

    @PostMapping("/deposit")
    public String processDeposit(@RequestParam double amount,
                                 @RequestParam(defaultValue = "CASH") String depositType,
                                 HttpServletRequest request,
                                 RedirectAttributes ra) {
        ATMSession session = getSession(request);
        TransactionResult result = transactionService.processDeposit(session.getSessionId(), amount, depositType);
        return handleTransactionResult(result, session.getSessionId(), ra, "deposit");
    }

    @GetMapping("/transfer")
    public String showTransfer(HttpServletRequest request, Model model) {
        ATMSession session = getSession(request);
        model.addAttribute("account", session.getCustomer().getAccount());
        return "customer/transfer";
    }

    @PostMapping("/transfer")
    public String processTransfer(@RequestParam String targetAccount,
                                  @RequestParam double amount,
                                  HttpServletRequest request,
                                  RedirectAttributes ra) {
        ATMSession session = getSession(request);
        if (targetAccount.equals(session.getCustomer().getAccount().getAccountNumber())) {
            ra.addFlashAttribute("error", "Cannot transfer to your own account.");
            return "redirect:/atm/session/transfer";
        }
        TransactionResult result = transactionService.processTransfer(session.getSessionId(), targetAccount, amount);
        return handleTransactionResult(result, session.getSessionId(), ra, "transfer");
    }

    @GetMapping("/bills")
    public String showBills(HttpServletRequest request, Model model) {
        ATMSession session = getSession(request);
        model.addAttribute("account", session.getCustomer().getAccount());
        return "customer/bills";
    }

    @PostMapping("/bills")
    public String processBill(@RequestParam String billerId,
                              @RequestParam String billerName,
                              @RequestParam double amount,
                              HttpServletRequest request,
                              RedirectAttributes ra) {
        ATMSession session = getSession(request);
        TransactionResult result = transactionService.processBillPayment(
                session.getSessionId(), billerId, billerName, amount);
        return handleTransactionResult(result, session.getSessionId(), ra, "bills");
    }

    @GetMapping("/receipt")
    public String receipt(HttpServletRequest request, Model model) {
        ATMSession session = getSession(request);
        Transaction tx = transactionService.getLastCompletedTransaction(session.getSessionId());
        if (tx == null) tx = transactionService.getLastTransaction(session.getSessionId());
        if (tx == null) return "redirect:/atm/session/dashboard";
        model.addAttribute("transaction", tx);
        model.addAttribute("customer", session.getCustomer());
        model.addAttribute("account", session.getCustomer().getAccount());
        return "customer/receipt";
    }

    @GetMapping("/history")
    public String history(HttpServletRequest request, Model model) {
        ATMSession session = getSession(request);
        List<Transaction> history = transactionRepository
                .findBySession_Customer_IdOrderByTimestampDesc(session.getCustomer().getId());
        model.addAttribute("transactions", history);
        model.addAttribute("customer", session.getCustomer());
        return "customer/history";
    }

    @GetMapping("/awaiting")
    public String awaiting(HttpServletRequest request, Model model) {
        ATMSession session = getSession(request);
        Transaction tx = transactionService.getLastPendingTransaction(session.getSessionId());
        model.addAttribute("transaction", tx);
        model.addAttribute("customer", session.getCustomer());
        return "customer/awaiting-approval";
    }

    @GetMapping("/transaction-status/{txId}")
    @ResponseBody
    public ResponseEntity<Map<String, String>> transactionStatus(@PathVariable String txId) {
        return transactionRepository.findById(txId)
                .map(tx -> ResponseEntity.ok(Map.of("status", tx.getStatus().name())))
                .orElse(ResponseEntity.notFound().build());
    }

    private String handleTransactionResult(TransactionResult result, String sessionId,
                                           RedirectAttributes ra, String backPage) {
        switch (result) {
            case SUCCESS -> { return "redirect:/atm/session/receipt"; }
            case AWAITING_APPROVAL -> { return "redirect:/atm/session/awaiting"; }
            case INSUFFICIENT_FUNDS -> {
                ra.addFlashAttribute("error", "Insufficient funds in your account.");
                return "redirect:/atm/session/" + backPage;
            }
            case INVALID_ACCOUNT -> {
                ra.addFlashAttribute("error", "Target account not found. Please verify the account number.");
                return "redirect:/atm/session/" + backPage;
            }
            default -> {
                ra.addFlashAttribute("error", "Transaction failed. Please try again.");
                return "redirect:/atm/session/" + backPage;
            }
        }
    }
}
