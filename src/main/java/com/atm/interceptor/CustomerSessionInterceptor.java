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

@Component
public class CustomerSessionInterceptor implements HandlerInterceptor {

    @Autowired
    private ATMSessionRepository sessionRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        HttpSession httpSession = request.getSession(false);
        if (httpSession == null) {
            response.sendRedirect("/atm/insert-card");
            return false;
        }

        String sessionId = (String) httpSession.getAttribute("ATM_SESSION_ID");
        if (sessionId == null) {
            response.sendRedirect("/atm/insert-card");
            return false;
        }

        ATMSession atmSession = sessionRepository.findById(sessionId).orElse(null);
        if (atmSession == null || !atmSession.isAuthenticated()
                || atmSession.getState() == SessionState.ENDED
                || atmSession.getState() == SessionState.CARD_RETAINED) {
            httpSession.removeAttribute("ATM_SESSION_ID");
            response.sendRedirect("/atm/insert-card");
            return false;
        }

        request.setAttribute("ATM_SESSION", atmSession);
        return true;
    }
}
