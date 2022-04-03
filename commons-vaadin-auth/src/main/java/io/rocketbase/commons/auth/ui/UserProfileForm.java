package io.rocketbase.commons.auth.ui;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.validator.StringLengthValidator;
import io.rocketbase.commons.api.ValidationApi;
import io.rocketbase.commons.auth.model.PasswordChangeRequestWithRepeat;
import io.rocketbase.commons.dto.authentication.PasswordChangeRequest;
import io.rocketbase.commons.dto.authentication.UpdateProfileRequest;
import io.rocketbase.commons.dto.validation.PasswordErrorCodes;
import io.rocketbase.commons.dto.validation.ValidationResponse;
import io.rocketbase.commons.model.AppUserToken;
import io.rocketbase.commons.util.Nulls;
import io.rocketbase.commons.vaadin.component.Notifications;
import org.vaadin.firitin.components.button.VButton;
import org.vaadin.firitin.components.formlayout.VFormLayout;
import org.vaadin.firitin.components.orderedlayout.VVerticalLayout;
import org.vaadin.firitin.components.textfield.VPasswordField;
import org.vaadin.firitin.components.textfield.VTextField;

import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class UserProfileForm extends Composite<Accordion> {

    private final ValidationApi validationApi;
    private final Supplier<AppUserToken> currentUser;
    private final Consumer<UpdateProfileRequest> updateConsumer;
    private final Consumer<PasswordChangeRequestWithRepeat> passwordChangeConsumer;

    private Binder<UpdateProfileRequest> updateProfileBinder;
    private Binder<PasswordChangeRequestWithRepeat> passwordChangeBinder;

    private TextField firstName, lastName;

    private PasswordField currentPassword, newPassword, newPasswordRepeat;

    /**
     * @param validationApi      is needed for validation
     * @param currentUser            current user best would be appUserService.getByUsername(CommonsPrincipal.getCurrent().getUsername())
     * @param updateConsumer         used to perform update profile with form value
     * @param passwordChangeConsumer used to perform change password
     */
    public UserProfileForm(ValidationApi validationApi, Supplier<AppUserToken> currentUser, Consumer<UpdateProfileRequest> updateConsumer, Consumer<PasswordChangeRequestWithRepeat> passwordChangeConsumer) {
        this.validationApi = validationApi;
        this.currentUser = currentUser;
        this.updateConsumer = updateConsumer;
        this.passwordChangeConsumer = passwordChangeConsumer;

        initForms();
        bind();
    }

    private void initForms() {
        firstName = new VTextField().withFullWidth();
        lastName = new VTextField().withFullWidth();

        currentPassword = new VPasswordField().withFullWidth();
        newPassword = new VPasswordField().withFullWidth();
        newPasswordRepeat = new VPasswordField().withFullWidth();
    }

    private void bind() {
        updateProfileBinder = new Binder<>();

        updateProfileBinder.forField(firstName)
                .bind(UpdateProfileRequest::getFirstName, UpdateProfileRequest::setFirstName);
        updateProfileBinder.forField(lastName)
                .bind(UpdateProfileRequest::getLastName, UpdateProfileRequest::setLastName);

        passwordChangeBinder = new Binder<>();
        passwordChangeBinder.forField(currentPassword)
                .asRequired()
                .withValidator(new StringLengthValidator(getTranslation("atLeastAndMax", 1, 100), 1, 100))
                .bind(PasswordChangeRequest::getCurrentPassword, PasswordChangeRequest::setCurrentPassword);
        passwordChangeBinder.forField(newPassword)
                .asRequired()
                .withValidator(new StringLengthValidator(getTranslation("atLeastAndMax", 3, 100), 3, 100))
                .withValidator((Validator<String>) (value, context) -> {
                    ValidationResponse<PasswordErrorCodes> response = validationApi.validatePassword(value);
                    if (!response.isValid()) {
                        return ValidationResult.error(response.getMessage("; "));
                    }
                    return ValidationResult.ok();
                })
                .bind(PasswordChangeRequest::getNewPassword, PasswordChangeRequest::setNewPassword);
        passwordChangeBinder.forField(newPasswordRepeat)
                .asRequired()
                .withValidator(new StringLengthValidator(getTranslation("atLeastAndMax", 3, 100), 3, 100))
                .withValidator((Validator<String>) (value, context) -> Nulls.notNull(value).equals(newPassword.getValue()) ?
                        ValidationResult.ok() :
                        ValidationResult.error(getTranslation("notTheSamePassword")))
                .bind(PasswordChangeRequestWithRepeat::getPasswordRepeat, PasswordChangeRequestWithRepeat::setPasswordRepeat);
    }

    @Override
    protected Accordion initContent() {
        Accordion accordion = new Accordion();
        FormLayout profileForm = new VFormLayout()
                .withResponsiveSteps(new FormLayout.ResponsiveStep("0", 1, FormLayout.ResponsiveStep.LabelsPosition.ASIDE))
                .withFormItem(firstName, getTranslation("user.firstName"), 1)
                .withFormItem(lastName, getTranslation("user.lastName"), 1);

        updateProfileBinder.readBean(UpdateProfileRequest.builder()
                .firstName(currentUser.get().getFirstName())
                .lastName(currentUser.get().getLastName())
                .build());

        accordion.add(getTranslation("user.updateProfile"), new VVerticalLayout()
                .withComponent(profileForm)
                .withComponent(new VButton(getTranslation("user.updateProfile"), VaadinIcon.USER_CARD.create(), e -> {
                    UpdateProfileRequest request = UpdateProfileRequest.builder()
                            .avatar(currentUser.get().getAvatar())
                            .keyValues(new HashMap<>())
                            .build();
                    try {
                        updateProfileBinder.writeBean(request);
                        updateConsumer.accept(request);
                    } catch (ValidationException ve) {
                        Notifications.validationError();
                    }
                }).withThemeVariants(ButtonVariant.LUMO_PRIMARY), FlexComponent.Alignment.END)
        );

        FormLayout passwordForm = new VFormLayout()
                .withResponsiveSteps(new FormLayout.ResponsiveStep("0", 1, FormLayout.ResponsiveStep.LabelsPosition.ASIDE))
                .withFormItem(currentPassword, getTranslation("user.currentPassword"), 1)
                .withFormItem(newPassword, getTranslation("user.newPassword"), 1)
                .withFormItem(newPasswordRepeat, getTranslation("user.passwordRepeat"), 1);

        accordion.add(getTranslation("user.changePassword"), new VVerticalLayout()
                .withComponent(passwordForm)
                .withComponent(new VButton(getTranslation("user.changePassword"), VaadinIcon.KEY.create(), e -> {
                    PasswordChangeRequestWithRepeat request = new PasswordChangeRequestWithRepeat();
                    try {
                        passwordChangeBinder.writeBean(request);
                        passwordChangeConsumer.accept(request);
                    } catch (ValidationException ve) {
                        Notifications.validationError();
                    }
                }).withThemeVariants(ButtonVariant.LUMO_ERROR), FlexComponent.Alignment.END));
        accordion.open(0);
        return accordion;
    }

}
