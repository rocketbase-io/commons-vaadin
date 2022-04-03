package io.rocketbase.commons.auth.model;

import io.rocketbase.commons.dto.appuser.AppUserResetPassword;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AppUserResetPasswordWithRepeat extends AppUserResetPassword {
    private String passwordRepeat;

    public AppUserResetPasswordWithRepeat(AppUserResetPassword request) {
        super(request.getResetPassword());
    }
}
