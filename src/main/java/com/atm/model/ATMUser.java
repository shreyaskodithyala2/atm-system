package com.atm.model;

import com.atm.model.enums.UserRole;
import jakarta.persistence.*;

/**
 * ============================================================
 * SOLID PRINCIPLE 2 — OPEN/CLOSED PRINCIPLE (OCP)
 * ============================================================
 * ATMUser is CLOSED for modification — its core structure
 * (id, userId, name, role) never changes.
 * It is OPEN for extension — new user types (Customer,
 * BankManager, SystemAdministrator) are added by subclassing,
 * NOT by editing this class.
 *
 * The abstract methods login() and logout() define a CONTRACT
 * that every user type must fulfil in its own way.
 *
 * ============================================================
 * SOLID PRINCIPLE 3 — LISKOV SUBSTITUTION PRINCIPLE (LSP)
 * ============================================================
 * Any subclass of ATMUser (Customer, BankManager,
 * SystemAdministrator) can be used wherever an ATMUser is
 * expected, without breaking the program.
 *
 * Example: ManagerService accepts ATMUser references and works
 * correctly whether the underlying object is a BankManager or
 * any future ATMUser subtype.
 */
@Entity
@Table(name = "atm_users")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "dtype", discriminatorType = DiscriminatorType.STRING)
public abstract class ATMUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ---- Common fields shared by every user subtype ----
    @Column(name = "user_id", unique = true, nullable = false)
    private String userId;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    /**
     * OCP: Abstract methods form the extension point.
     * Each subclass provides its own login/logout implementation
     * without this base class needing to change.
     *
     * LSP: Any subclass is guaranteed to have these behaviours,
     * making substitution safe.
     */
    public abstract boolean login();
    public abstract void logout();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }
}
