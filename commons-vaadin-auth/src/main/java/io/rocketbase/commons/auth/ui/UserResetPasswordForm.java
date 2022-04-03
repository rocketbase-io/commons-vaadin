package io.rocketbase.commons.auth.ui;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.validator.StringLengthValidator;
import io.rocketbase.commons.api.ValidationApi;
import io.rocketbase.commons.auth.model.AppUserResetPasswordWithRepeat;
import io.rocketbase.commons.dto.appuser.AppUserResetPassword;
import io.rocketbase.commons.dto.validation.PasswordErrorCodes;
import io.rocketbase.commons.dto.validation.ValidationResponse;
import io.rocketbase.commons.util.Nulls;
import io.rocketbase.commons.vaadin.component.Notifications;
import org.vaadin.firitin.components.button.VButton;
import org.vaadin.firitin.components.formlayout.VFormLayout;
import org.vaadin.firitin.components.orderedlayout.VVerticalLayout;
import org.vaadin.firitin.components.textfield.VPasswordField;

import java.util.function.Consumer;

public class UserResetPasswordForm extends Composite<VerticalLayout> {

    private final ValidationApi validationApi;
    private final Consumer<AppUserResetPassword> requestConsumer;

    private Binder<AppUserResetPasswordWithRepeat> binder;

    private PasswordField password, passwordRepeat;

    /**
     * @param validationApi   is needed for validation
     * @param requestConsumer used to perform create user after form validation
     */
    public UserResetPasswordForm(ValidationApi validationApi, Consumer<AppUserResetPassword> requestConsumer) {
        this.validationApi = validationApi;
        this.requestConsumer = requestConsumer;

        initForms();
        bind();
    }

    private void initForms() {
        password = new VPasswordField().withFullWidth();
        passwordRepeat = new VPasswordField().withFullWidth();
    }

    private void bind() {
        binder = new Binder<>();

        binder.forField(password)
                .asRequired()
                .withValidator(new StringLengthValidator(getTranslation("atLeastAndMax", 3, 100), 3, 100))
                .withValidator((Validator<String>) (value, context) -> {
                    ValidationResponse<PasswordErrorCodes> response = validationApi.validatePassword(value);
                    if (!response.isValid()) {
                        return ValidationResult.error(response.getMessage("; "));
                    }
                    return ValidationResult.ok();
                })
                .bind(AppUserResetPasswordWithRepeat::getResetPassword, AppUserResetPasswordWithRepeat::setResetPassword);

        binder.forField(passwordRepeat)
                .asRequired()
                .withValidator(new StringLengthValidator(getTranslation("atLeastAndMax", 3, 100), 3, 100))
                .withValidator((Validator<String>) (value, context) -> Nulls.notNull(value).equals(password.getValue()) ?
                        ValidationResult.ok() :
                        ValidationResult.error(getTranslation("notTheSamePassword")))
                .bind(AppUserResetPasswordWithRepeat::getPasswordRepeat, AppUserResetPasswordWithRepeat::setPasswordRepeat);
    }

    @Override
    protected VerticalLayout initContent() {
        VFormLayout form = new VFormLayout()
                .withResponsiveStepsTwoCols(FormLayout.ResponsiveStep.LabelsPosition.TOP, "21em")
                .withFormItem(password, getTranslation("user.password"), 1)
                .withFormItem(passwordRepeat, getTranslation("user.passwordRepeat"), 1);
        return new VVerticalLayout()
                .withComponent(form)
                .withComponent(new VButton(getTranslation("resetPassword"), e -> {
                    AppUserResetPasswordWithRepeat request = new AppUserResetPasswordWithRepeat();
                    try {
                        binder.writeBean(request);
                        requestConsumer.accept(request);
                    } catch (ValidationException ve) {
                        Notifications.validationError();
                    }
                }).withThemeVariants(ButtonVariant.LUMO_PRIMARY), FlexComponent.Alignment.END);
    }

}
