package com.atm.interceptor;

import com.atm.model.ATMSession;
import com.atm.model.enums.SessionState;
import com.atm.repository.ATMSessionRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * ============================================================
 * DESIGN PATTERN 4 — INTERCEPTOR PATTERN
 * ============================================================
 * CustomerSessionInterceptor implements the INTERCEPTOR pattern
 * (also called Chain of Responsibility in some contexts).
 *
 * WHAT IT DOES:
 * Every HTTP request to /atm/session/** is intercepted by this
 * class BEFORE it reaches any controller. If the session is
 * invalid, the request is blocked and redirected — the
 * controller never even runs.
 *
 * WHY IT'S A PATTERN:
 * Instead of copy-pasting session validation at the top of
 * every single controller method (which would violate SRP),
 * the validation logic is written ONCE here and applied
 * uniformly as a cross-cutting concern.
 *
 * HOW IT FITS INTO SPRING:
 *   Request → [CustomerSessionInterceptor.preHandle()]
 *               ↓ (if returns true)
 *             [Controller method]
 *               ↓
 *             [Response]
 *
 * This is registered in WebConfig.java:
 *   registry.addInterceptor(customerSessionInterceptor)
 *            .addPathPatterns("/atm/session/**");
 *
 * ANALOGY:
 * Like a security guard at a building entrance — every visitor
 * is checked before being allowed upstairs. The offices (controllers)
 * don't each have their own guard; one guard covers all.
 */
@Component
public class CustomerSessionInterceptor implements HandlerInterceptor {

    @Autowired
    private ATMSessionRepository sessionRepository;

    /**
     * INTERCEPTOR preHandle() — runs BEFORE the target controller.
     * Returns true  → request continues to the controller.
     * Returns false → request is blocked; redirect is sent.
     *
     * Validation steps:
     *   1. HTTP session must exist.
     *   2. ATM_SESSION_ID attribute must be present.
     *   3. The ATMSession in the DB must be authenticated and not ended/retained.
     *
     * If all checks pass, the ATMSession is attached to the
     * request as "ATM_SESSION" for controllers to use directly.
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {

        // Step 1: Ensure an HTTP session exists at all
        HttpSession httpSession = request.getSession(false);
        if (httpSession == null) {
            response.sendRedirect("/atm/insert-card");
            return false; // BLOCK — no session
        }

        // Step 2: Ensure the ATM session ID is stored in the HTTP session
        String sessionId = (String) httpSession.getAttribute("ATM_SESSION_ID");
        if (sessionId == null) {
            response.sendRedirect("/atm/insert-card");
            return false; // BLOCK — card not inserted
        }

        // Step 3: Validate the ATMSession in the database
        ATMSession atmSession = sessionRepository.findById(sessionId).orElse(null);
        if (atmSession == null || !atmSession.isAuthenticated()
                || atmSession.getState() == SessionState.ENDED
                || atmSession.getState() == SessionState.CARD_RETAINED) {
            httpSession.removeAttribute("ATM_SESSION_ID");
            response.sendRedirect("/atm/insert-card");
            return false; // BLOCK — invalid or expired session
        }

        // All checks passed — pass the session to the controller via request attribute
        request.setAttribute("ATM_SESSION", atmSession);
        return true; // ALLOW — continue to controller
    }
}
