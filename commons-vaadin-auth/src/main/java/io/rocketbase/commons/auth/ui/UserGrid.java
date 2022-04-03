package io.rocketbase.commons.auth.ui;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import io.rocketbase.commons.api.AppUserApi;
import io.rocketbase.commons.api.ValidationApi;
import io.rocketbase.commons.dto.appuser.AppUserRead;
import io.rocketbase.commons.dto.appuser.AppUserResetPassword;
import io.rocketbase.commons.dto.appuser.AppUserUpdate;
import io.rocketbase.commons.dto.appuser.QueryAppUser;
import io.rocketbase.commons.model.AppUserToken;
import io.rocketbase.commons.vaadin.component.ButtonBar;
import io.rocketbase.commons.vaadin.component.Buttons;
import io.rocketbase.commons.vaadin.component.Notifications;
import io.rocketbase.commons.vaadin.data.PageableDataProvider;
import io.rocketbase.commons.vaadin.renderer.BooleanRenderer;
import io.rocketbase.commons.vaadin.renderer.InstantRenderer;
import lombok.Setter;
import org.springframework.data.util.Pair;
import org.vaadin.firitin.components.button.VButton;
import org.vaadin.firitin.components.checkbox.VCheckBox;
import org.vaadin.firitin.components.html.VImage;
import org.vaadin.firitin.components.html.VLabel;
import org.vaadin.firitin.components.textfield.VTextField;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class UserGrid extends Grid<AppUserRead> {

    private static DateTimeFormatter DATE_TIME_SHORT_FORMAT = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withZone(ZoneId.systemDefault());

    private final AppUserApi api;
    private final ValidationApi validationApi;

    private TextField firstName = new VTextField().withFullWidth();
    private TextField lastName = new VTextField().withFullWidth();
    private Checkbox enabled = new VCheckBox();

    @Setter
    private Supplier<QueryAppUser> filterSupplier;

    private Consumer<Pair<AppUserToken, AppUserResetPassword>> resetPasswordConsumer;

    public UserGrid(AppUserApi api) {
        this(api, null, null, null);
    }

    public UserGrid(AppUserApi api, ValidationApi validationApi, Consumer<Pair<AppUserToken, AppUserResetPassword>> resetPasswordConsumer, Consumer<Pair<AppUserToken, AppUserUpdate>> updateUserConsumer) {
        this.api = api;
        this.validationApi = validationApi;
        this.resetPasswordConsumer = resetPasswordConsumer;

        addColumn(new ComponentRenderer<>(v -> new VImage(v.getAvatar(), "avatar")
                .withClassName("app-bar__avatar")))
                .setHeader(getTranslation("user.avatar"))
                .setTextAlign(ColumnTextAlign.CENTER)
                .setFlexGrow(0)
                .setWidth("80px")
                .setAutoWidth(false);

        Column<AppUserRead> username = addColumn(AppUserRead::getUsername)
                .setHeader(getTranslation("user.username"))
                .setSortProperty("username")
                .setFlexGrow(0)
                .setWidth("200px")
                .setAutoWidth(false);

        addColumn(AppUserRead::getFirstName)
                .setHeader(getTranslation("user.firstName"))
                .setEditorComponent(firstName)
                .setSortProperty("firstName")
                .setAutoWidth(true);

        addColumn(AppUserRead::getLastName)
                .setHeader(getTranslation("user.lastName"))
                .setEditorComponent(lastName)
                .setSortProperty("lastName")
                .setAutoWidth(true);

        addColumn(AppUserRead::getEmail)
                .setHeader(getTranslation("user.email"))
                .setSortProperty("email")
                .setAutoWidth(true);

        addColumn(new InstantRenderer<>(AppUserRead::getCreated, DATE_TIME_SHORT_FORMAT))
                .setHeader(getTranslation("user.created"))
                .setSortProperty("created")
                .setWidth("150px")
                .setFlexGrow(0)
                .setAutoWidth(false);

        addColumn(new InstantRenderer<>(AppUserRead::getLastLogin, DATE_TIME_SHORT_FORMAT))
                .setHeader(getTranslation("user.lastLogin"))
                .setSortProperty("lastLogin")
                .setWidth("150px")
                .setFlexGrow(0)
                .setAutoWidth(false);

        addColumn(new BooleanRenderer<>(AppUserRead::isEnabled))
                .setHeader(getTranslation("user.enabled"))
                .setEditorComponent(enabled)
                .setSortProperty("enabled")
                .setTextAlign(ColumnTextAlign.CENTER)
                .setWidth("100px")
                .setFlexGrow(0)
                .setAutoWidth(false);

        boolean resetPasswordEnabled = validationApi != null && resetPasswordConsumer != null;
        if (updateUserConsumer != null || resetPasswordEnabled) {
            Editor<AppUserRead> editor = getEditor();
            Binder<AppUserRead> binder = new Binder<>();
            editor.setBinder(binder);
            editor.setBuffered(true);
            editor.addSaveListener(e -> {
                updateUserConsumer.accept(Pair.of(e.getItem(), AppUserUpdate.builder()
                        .firstName(e.getItem().getFirstName())
                        .lastName(e.getItem().getLastName())
                        .enabled(enabled.getOptionalValue().isPresent() ? enabled.getValue() : false)
                        .build()));
                Notifications.saved();
            });

            binder.forField(firstName)
                    .bind(AppUserRead::getFirstName, AppUserRead::setFirstName);
            binder.forField(lastName)
                    .bind(AppUserRead::getLastName, AppUserRead::setLastName);
            binder.forField(enabled)
                    .bind(AppUserRead::isEnabled, AppUserRead::setEnabled);


            addColumn(new ComponentRenderer<>(v -> {
                ButtonBar buttonBar = new ButtonBar();
                if (resetPasswordEnabled) {
                    buttonBar.addButton(new VButton(VaadinIcon.KEY.create(), e -> {
                        if (editor.isOpen()) {
                            editor.cancel();
                        }
                        resetPasswordDialog(v);
                    }));
                }
                if (updateUserConsumer != null) {
                    buttonBar.addButton(new VButton(VaadinIcon.PENCIL.create(), e -> {
                        if (editor.isOpen()) {
                            editor.cancel();
                        }
                        editor.editItem(v);
                        firstName.focus();
                    }));
                }
                return buttonBar;
            }))
                    .setEditorComponent(new ButtonBar()
                            .withButton(Buttons.save(true, () -> editor.save()))
                            .withButton(Buttons.cancel(true, () -> editor.cancel()))
                    ).setWidth(resetPasswordEnabled && updateUserConsumer != null ? "150px" : "100px")
                    .setAutoWidth(false)
                    .setFlexGrow(0);
        }
        sort(GridSortOrder.asc(username).build());
    }

    private void resetPasswordDialog(AppUserRead user) {
        Dialog dialog = new Dialog(new VLabel(getTranslation("resetUserPassword", user.getDisplayName())).withClassName("font-size-xxl"));

        Consumer<AppUserResetPassword> consumer = (r) -> {
            resetPasswordConsumer.accept(Pair.of(user, r));
            dialog.close();
            Notifications.saved();
            reload();
        };

        dialog.add(new UserResetPasswordForm(validationApi, consumer));
        dialog.setWidth("700px");
        dialog.open();
    }

    public void reload() {
        setItems(PageableDataProvider.fetch((query, pageable) -> api.find(filterSupplier != null ? filterSupplier.get() : null, pageable).toPage()),
                PageableDataProvider.count((query, pageable) -> api.find(filterSupplier != null ? filterSupplier.get() : null, pageable).toPage()));
    }
}
