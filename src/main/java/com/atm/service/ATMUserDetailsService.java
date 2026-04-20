package com.atm.service;

import com.atm.repository.BankManagerRepository;
import com.atm.repository.SystemAdministratorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class ATMUserDetailsService implements UserDetailsService {

    @Autowired
    private BankManagerRepository bankManagerRepository;

    @Autowired
    private SystemAdministratorRepository adminRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return bankManagerRepository.findByUsername(username)
                .map(m -> (UserDetails) m)
                .orElseGet(() -> adminRepository.findByUsername(username)
                        .map(a -> (UserDetails) a)
                        .orElseThrow(() -> new UsernameNotFoundException("Staff user not found: " + username)));
    }
}
