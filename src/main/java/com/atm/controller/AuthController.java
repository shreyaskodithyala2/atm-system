package com.atm.controller;

import com.atm.model.ATMSession;
import com.atm.service.AuthenticationService;
import com.atm.service.AuthenticationService.CardInsertResult;
import com.atm.service.AuthenticationService.PinResult;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    @Autowired
    private AuthenticationService authService;

    @GetMapping("/")
    public String index(HttpSession session) {
        if (session.getAttribute("ATM_SESSION_ID") != null) {
            return "redirect:/atm/session/dashboard";
        }
        return "redirect:/atm/insert-card";
    }

    @GetMapping("/atm/insert-card")
    public String showInsertCard(HttpSession session, Model model) {
        session.removeAttribute("ATM_SESSION_ID");
        model.addAttribute("cardNumber", "");
        return "auth/card-login";
    }

    @PostMapping("/atm/insert-card")
    public String processCardInsert(@RequestParam String cardNumber,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {
        String cleanCard = cardNumber.replaceAll("\\s|-", "");
        CardInsertResult result = authService.insertCard(cleanCard);

        switch (result) {
            case SUCCESS -> {
                ATMSession atmSession = authService.createSession(cleanCard);
                session.setAttribute("ATM_SESSION_ID", atmSession.getSessionId());
                session.setAttribute("CARD_NUMBER", cleanCard);
                return "redirect:/atm/pin-entry";
            }
            case CARD_RETAINED -> {
                redirectAttributes.addFlashAttribute("error",
                    "This card has been retained due to security reasons. Please contact your bank.");
                return "redirect:/atm/card-retained";
            }
            case CARD_EXPIRED -> {
                redirectAttributes.addFlashAttribute("error",
                    "This card has expired. Please visit your branch for a replacement.");
                return "redirect:/atm/insert-card";
            }
            default -> {
                redirectAttributes.addFlashAttribute("error",
                    "Card not recognized. Please check your card number.");
                return "redirect:/atm/insert-card";
            }
        }
    }

    @GetMapping("/atm/pin-entry")
    public String showPinEntry(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        String sessionId = (String) session.getAttribute("ATM_SESSION_ID");
        if (sessionId == null) return "redirect:/atm/insert-card";
        model.addAttribute("attemptsRemaining", authService.getRemainingAttempts(sessionId));
        return "auth/pin-entry";
    }

    @PostMapping("/atm/pin-entry")
    public String processPin(@RequestParam String pin,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        String sessionId = (String) session.getAttribute("ATM_SESSION_ID");
        if (sessionId == null) return "redirect:/atm/insert-card";

        PinResult result = authService.authenticatePin(sessionId, pin);

        switch (result) {
            case SUCCESS -> {
                return "redirect:/atm/session/dashboard";
            }
            case CARD_RETAINED -> {
                session.removeAttribute("ATM_SESSION_ID");
                return "redirect:/atm/card-retained";
            }
            default -> {
                int remaining = authService.getRemainingAttempts(sessionId);
                redirectAttributes.addFlashAttribute("error",
                    "Incorrect PIN. " + remaining + " attempt(s) remaining.");
                return "redirect:/atm/pin-entry";
            }
        }
    }

    @GetMapping("/atm/card-retained")
    public String showCardRetained() {
        return "auth/card-retained";
    }

    @GetMapping("/atm/logout")
    public String logout(HttpSession session) {
        String sessionId = (String) session.getAttribute("ATM_SESSION_ID");
        if (sessionId != null) {
            authService.endSession(sessionId);
            session.removeAttribute("ATM_SESSION_ID");
        }
        session.invalidate();
        return "redirect:/atm/insert-card";
    }

    @GetMapping("/staff/login")
    public String staffLogin(@RequestParam(required = false) String error,
                             @RequestParam(required = false) String logout,
                             Model model) {
        if (error != null) model.addAttribute("error", "Invalid credentials. Please try again.");
        if (logout != null) model.addAttribute("message", "You have been logged out successfully.");
        return "auth/staff-login";
    }
}
