package io.rocketbase.commons.vaadin.component;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexLayout.FlexDirection;
import org.springframework.util.StringUtils;
import org.vaadin.firitin.components.button.VButton;
import org.vaadin.firitin.components.html.VLabel;
import org.vaadin.firitin.components.html.VParagaph;
import org.vaadin.firitin.components.notification.VNotification;
import org.vaadin.firitin.components.orderedlayout.VFlexLayout;

public abstract class Notifications {

    private static final int DEFAULT_DURATION = 5000;

    public static VNotification saved() {
        VNotification notification = new VNotification(UI.getCurrent().getTranslation("successfullySaved"), DEFAULT_DURATION);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        notification.open();
        return notification;
    }

    public static VNotification deleted() {
        VNotification notification = new VNotification(UI.getCurrent().getTranslation("successfullyDeleted"), DEFAULT_DURATION);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        notification.open();
        return notification;
    }

    public static VNotification internalError() {
        return internalError(null);
    }

    public static VNotification internalError(String message) {
        return internalError(message, null);
    }

    public static VNotification internalError(String message, String details) {
        VNotification notification = new VNotification();
        notification.setDuration(0);
        notification.setPosition(Notification.Position.MIDDLE);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);

        Label label = new VLabel(message != null ? message : UI.getCurrent().getTranslation("internalError"))
                .withClassName("h4")
                .withWidth("300px")
                .withStyle("text-align", "center");
        VFlexLayout layout = new VFlexLayout(label);

        if (!StringUtils.isEmpty(details)) {
            layout.add(new VParagaph(details).withWidth("300px"));
        }
        layout.add(new VButton("Ok", e -> notification.close())
                .withThemeVariants(ButtonVariant.LUMO_PRIMARY));
        notification.add(layout);
        notification.open();
        return notification;
    }

    public static VNotification validationError() {
        VNotification notification = new VNotification(UI.getCurrent().getTranslation("validationExceptionsCheckForm"), DEFAULT_DURATION, Notification.Position.BOTTOM_START);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        notification.open();
        return notification;
    }
}
