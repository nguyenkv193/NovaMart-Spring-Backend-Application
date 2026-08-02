package com.novamart.security.userdetails;

import com.novamart.modules.users.entity.User;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
public class UserDetail implements UserDetails {
    private final User user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();

        if(user.getRole() != null) {
            authorities.add(
                    new SimpleGrantedAuthority(
                            "ROLE_" + user.getRole().name()
                    )
            );
        }

        if(user.getPermission() != null) {
            authorities.add(
                    new SimpleGrantedAuthority(
                            user.getPermission().name()
                    )
            );
        }

        return authorities;
    }

    @Override
    public @Nullable String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }
}
