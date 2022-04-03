package io.rocketbase.commons.auth.model;

import io.rocketbase.commons.dto.authentication.PasswordChangeRequest;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PasswordChangeRequestWithRepeat extends PasswordChangeRequest {
    private String passwordRepeat;

    public PasswordChangeRequestWithRepeat(PasswordChangeRequest request) {
        super(request.getCurrentPassword(), request.getNewPassword());
    }
}
