package com.ecom.config;

import com.ecom.model.UserDtls;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
public class CustomUser implements UserDetails {

    private UserDtls user;


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        SimpleGrantedAuthority authority= new SimpleGrantedAuthority(user.getRole());

        return Arrays.asList(authority);
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {

        return  user.getEmail();
    }
    @Override
    public boolean isAccountNonExpired() {
        return user.getAccountNonLocked();
    }
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isEnabled(){
        return true;
    }
}
