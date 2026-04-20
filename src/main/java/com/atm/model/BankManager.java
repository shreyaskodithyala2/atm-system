package com.atm.model;

import com.atm.model.enums.UserRole;
import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "bank_managers")
@DiscriminatorValue("BANK_MANAGER")
public class BankManager extends ATMUser implements UserDetails {

    @Column(name = "manager_id", unique = true, nullable = false)
    private String managerId;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    public BankManager() {
        setRole(UserRole.BANK_MANAGER);
    }

    @Override
    public boolean login() { return true; }

    @Override
    public void logout() {}

    public boolean approveLargeTransaction(String transactionId) {
        return true;
    }

    public void auditDailyTransactions(Date date) {}

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_BANK_MANAGER"));
    }

    @Override
    public boolean isAccountNonExpired() { return true; }
    @Override
    public boolean isAccountNonLocked() { return true; }
    @Override
    public boolean isCredentialsNonExpired() { return true; }
    @Override
    public boolean isEnabled() { return true; }

    public String getManagerId() { return managerId; }
    public void setManagerId(String managerId) { this.managerId = managerId; }
    @Override
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    @Override
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
