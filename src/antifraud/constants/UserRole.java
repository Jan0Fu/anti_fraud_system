package antifraud.constants;

import org.springframework.security.core.GrantedAuthority;

public enum UserRole implements GrantedAuthority {
    ADMINISTRATOR, MERCHANT, SUPPORT;

    @Override
    public String getAuthority() {
        return name();
    }
}
