package com.atm.controller;

import com.atm.dto.DiagnosticsReport;
import com.atm.model.ATMCard;
import com.atm.model.SystemAdministrator;
import com.atm.repository.SystemAdministratorRepository;
import com.atm.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/staff/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired private AdminService adminService;
    @Autowired private SystemAdministratorRepository adminRepository;

    private SystemAdministrator getAdmin(UserDetails userDetails) {
        return adminRepository.findByUsername(userDetails.getUsername()).orElseThrow();
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        model.addAttribute("admin", getAdmin(userDetails));
        return "admin/dashboard";
    }

    @PostMapping("/diagnostics")
    public String runDiagnostics(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        DiagnosticsReport report = adminService.runRemoteDiagnostics(userDetails.getUsername());
        model.addAttribute("report", report);
        model.addAttribute("admin", getAdmin(userDetails));
        return "admin/diagnostics";
    }

    @GetMapping("/firmware")
    public String firmwareForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        SystemAdministrator admin = getAdmin(userDetails);
        model.addAttribute("admin", admin);
        model.addAttribute("currentVersion", admin.getFirmwareVersion());
        return "admin/firmware";
    }

    @PostMapping("/firmware")
    public String updateFirmware(@RequestParam String version,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 RedirectAttributes ra) {
        boolean success = adminService.updateSystemFirmware(userDetails.getUsername(), version);
        if (success) {
            ra.addFlashAttribute("message", "Firmware updated to version " + version + " successfully.");
        } else {
            ra.addFlashAttribute("error", "Invalid version format. Use semantic versioning (e.g., 2.5.0).");
        }
        return "redirect:/staff/admin/firmware";
    }

    @GetMapping("/cards")
    public String retainedCards(Model model) {
        List<ATMCard> retainedCards = adminService.getRetainedCards();
        model.addAttribute("retainedCards", retainedCards);
        return "admin/retained-cards";
    }

    @PostMapping("/cards/release/{id}")
    public String releaseCard(@PathVariable Long id, RedirectAttributes ra) {
        adminService.releaseCard(id);
        ra.addFlashAttribute("message", "Card released successfully.");
        return "redirect:/staff/admin/cards";
    }
}
