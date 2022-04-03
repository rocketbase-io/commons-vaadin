package io.rocketbase.commons.auth.ui;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.data.validator.StringLengthValidator;
import io.rocketbase.commons.api.ValidationApi;
import io.rocketbase.commons.dto.appuser.AppUserCreate;
import io.rocketbase.commons.dto.validation.EmailErrorCodes;
import io.rocketbase.commons.dto.validation.PasswordErrorCodes;
import io.rocketbase.commons.dto.validation.UsernameErrorCodes;
import io.rocketbase.commons.dto.validation.ValidationResponse;
import io.rocketbase.commons.util.Nulls;
import io.rocketbase.commons.vaadin.component.Notifications;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.vaadin.firitin.components.button.VButton;
import org.vaadin.firitin.components.checkbox.VCheckBox;
import org.vaadin.firitin.components.formlayout.VFormLayout;
import org.vaadin.firitin.components.orderedlayout.VVerticalLayout;
import org.vaadin.firitin.components.textfield.VEmailField;
import org.vaadin.firitin.components.textfield.VPasswordField;
import org.vaadin.firitin.components.textfield.VTextField;

import java.util.function.Consumer;

public class UserCreateForm extends Composite<VerticalLayout> {

    private final ValidationApi validationApi;
    private final Consumer<AppUserCreate> requestConsumer;

    private Binder<AppUserCreateWithRepeat> binder;

    private TextField username, firstName, lastName;
    private EmailField email;
    private PasswordField password, passwordRepeat;
    private Checkbox admin;
    private Checkbox enabled;

    /**
     * @param validationApi   is needed for validation
     * @param requestConsumer used to perform create user after form validation
     */
    public UserCreateForm(ValidationApi validationApi, Consumer<AppUserCreate> requestConsumer) {
        this.validationApi = validationApi;
        this.requestConsumer = requestConsumer;

        initForms();
        bind();
    }

    private void initForms() {
        username = new VTextField().withFullWidth();
        firstName = new VTextField().withFullWidth();
        lastName = new VTextField().withFullWidth();
        email = new VEmailField().withFullWidth();
        password = new VPasswordField().withFullWidth();
        passwordRepeat = new VPasswordField().withFullWidth();
        admin = new VCheckBox(false);
        enabled = new VCheckBox(true);
    }

    private void bind() {
        binder = new Binder<>();

        binder.forField(username)
                .asRequired()
                .withValidator(new StringLengthValidator(getTranslation("atLeastAndMax", 3, 255), 3, 255))
                .withValidator((Validator<String>) (value, context) -> {
                    ValidationResponse<UsernameErrorCodes> response = validationApi.validateUsername(value);
                    if (!response.isValid()) {
                        return ValidationResult.error(response.getMessage("; "));
                    }
                    return ValidationResult.ok();
                })
                .bind(AppUserCreateWithRepeat::getUsername, AppUserCreateWithRepeat::setUsername);
        binder.forField(firstName)
                .bind(AppUserCreateWithRepeat::getFirstName, AppUserCreateWithRepeat::setFirstName);
        binder.forField(lastName)
                .bind(AppUserCreateWithRepeat::getLastName, AppUserCreateWithRepeat::setLastName);
        binder.forField(email)
                .asRequired()
                .withValidator(new EmailValidator(getTranslation("invalidEmail")))
                .withValidator(new StringLengthValidator(getTranslation("atLeastAndMax", 3, 255), 3, 255))
                .withValidator((Validator<String>) (value, context) -> {
                    ValidationResponse<EmailErrorCodes> response = validationApi.validateEmail(value);
                    if (!response.getErrorCodes().isEmpty()) {
                        return ValidationResult.error(response.getMessage("; "));
                    }
                    return ValidationResult.ok();
                })
                .bind(AppUserCreateWithRepeat::getEmail, AppUserCreateWithRepeat::setEmail);
        binder.forField(password)
                .asRequired()
                .withValidator(new StringLengthValidator(getTranslation("atLeastAndMax", 5, 100), 5, 100))
                .withValidator((Validator<String>) (value, context) -> {
                    ValidationResponse<PasswordErrorCodes> response = validationApi.validatePassword(value);
                    if (!response.getErrorCodes().isEmpty()) {
                        return ValidationResult.error(response.getMessage("; "));
                    }
                    return ValidationResult.ok();
                })
                .bind(AppUserCreateWithRepeat::getPassword, AppUserCreateWithRepeat::setPassword);

        binder.forField(passwordRepeat)
                .asRequired()
                .withValidator(new StringLengthValidator(getTranslation("atLeastAndMax", 5, 100), 5, 100))
                .withValidator((Validator<String>) (value, context) -> Nulls.notNull(value).equals(password.getValue()) ?
                        ValidationResult.ok() :
                        ValidationResult.error(getTranslation("notTheSamePassword")))
                .bind(AppUserCreateWithRepeat::getPasswordRepeat, AppUserCreateWithRepeat::setPasswordRepeat);

        binder.forField(admin)
                .bind(AppUserCreateWithRepeat::getAdmin, AppUserCreateWithRepeat::setAdmin);

        binder.forField(enabled)
                .bind(AppUserCreateWithRepeat::isEnabled, AppUserCreateWithRepeat::setEnabled);
    }

    @Override
    protected VerticalLayout initContent() {
        VFormLayout form = new VFormLayout()
                .withResponsiveStepsTwoCols(FormLayout.ResponsiveStep.LabelsPosition.TOP, "21em")
                .withFormItem(username, getTranslation("user.username"), 2)
                .withFormItem(firstName, getTranslation("user.firstName"), 1)
                .withFormItem(lastName, getTranslation("user.lastName"), 1)
                .withFormItem(email, getTranslation("user.email"), 2)
                .withFormItem(password, getTranslation("user.password"), 1)
                .withFormItem(passwordRepeat, getTranslation("user.passwordRepeat"), 1)
                .withFormItem(admin, getTranslation("user.adminRole"), 1)
                .withFormItem(enabled, getTranslation("user.enabled"), 1);
        return new VVerticalLayout()
                .withComponent(form)
                .withComponent(new VButton(getTranslation("createUser"), e -> {
                    AppUserCreateWithRepeat request = new AppUserCreateWithRepeat();
                    try {
                        binder.writeBean(request);
                        requestConsumer.accept(request);
                    } catch (ValidationException ve) {
                        Notifications.validationError();
                    }
                }).withThemeVariants(ButtonVariant.LUMO_PRIMARY), FlexComponent.Alignment.END);
    }

    @Data
    @NoArgsConstructor
    public static class AppUserCreateWithRepeat extends AppUserCreate {
        private String passwordRepeat;

        public AppUserCreateWithRepeat(AppUserCreate request) {
            super(request.getUsername(), request.getPassword(), request.getFirstName(), request.getLastName(), request.getEmail(), request.getAvatar(), request.getKeyValues(), request.getAdmin(), request.getRoles(), request.isEnabled());
        }
    }

}
