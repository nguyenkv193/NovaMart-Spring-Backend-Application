package com.novamart.security.userdetails;

import com.novamart.modules.users.constants.UserMessageConstants;
import com.novamart.modules.users.entity.User;
import com.novamart.modules.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        final User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        String.format(
                                UserMessageConstants.EMAIL_NOT_FOUND,
                                email
                        )
                ));

        return new UserDetail(user);
    }
}
