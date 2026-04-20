package com.atm.model;

import com.atm.model.enums.UserRole;
import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "system_administrators")
@DiscriminatorValue("ADMIN")
public class SystemAdministrator extends ATMUser implements UserDetails {

    @Column(name = "admin_id", unique = true, nullable = false)
    private String adminId;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(name = "firmware_version")
    private String firmwareVersion = "1.0.0";

    public SystemAdministrator() {
        setRole(UserRole.ADMIN);
    }

    @Override
    public boolean login() { return true; }

    @Override
    public void logout() {}

    public void runRemoteDiagnostics() {}

    public boolean updateSystemFirmware(String version) {
        if (version != null && version.matches("\\d+\\.\\d+\\.\\d+")) {
            this.firmwareVersion = version;
            return true;
        }
        return false;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    @Override
    public boolean isAccountNonExpired() { return true; }
    @Override
    public boolean isAccountNonLocked() { return true; }
    @Override
    public boolean isCredentialsNonExpired() { return true; }
    @Override
    public boolean isEnabled() { return true; }

    public String getAdminId() { return adminId; }
    public void setAdminId(String adminId) { this.adminId = adminId; }
    @Override
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    @Override
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getFirmwareVersion() { return firmwareVersion; }
    public void setFirmwareVersion(String firmwareVersion) { this.firmwareVersion = firmwareVersion; }
}
