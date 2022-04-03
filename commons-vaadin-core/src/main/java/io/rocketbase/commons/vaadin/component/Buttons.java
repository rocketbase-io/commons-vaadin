package io.rocketbase.commons.vaadin.component;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.IconFactory;
import com.vaadin.flow.component.icon.VaadinIcon;
import lombok.Getter;
import org.springframework.lang.Nullable;
import org.vaadin.firitin.components.button.VButton;

import static com.vaadin.flow.component.button.ButtonVariant.*;

public abstract class Buttons {

    public static ButtonBuilder builder(ButtonType type) {
        return new ButtonBuilder(type);
    }

    public static VButton cancel(boolean iconOnly, Runnable onClick) {
        return builder(ButtonType.CANCEL).iconOnly(iconOnly, onClick);
    }

    public static VButton save(boolean iconOnly, Runnable onClick) {
        return builder(ButtonType.SAVE).iconOnly(iconOnly, onClick);
    }

    public static VButton add(boolean iconOnly, Runnable onClick) {
        return builder(ButtonType.ADD).iconOnly(iconOnly, onClick);
    }

    public static VButton refresh(boolean iconOnly, Runnable onClick) {
        return builder(ButtonType.REFRESH).iconOnly(iconOnly, onClick);
    }

    public static VButton edit(boolean iconOnly, Runnable onClick) {
        return builder(ButtonType.EDIT).iconOnly(iconOnly, onClick);
    }

    public static VButton delete(boolean iconOnly, Runnable onClick) {
        return builder(ButtonType.DELETE).iconOnly(iconOnly, onClick);
    }

    public static VButton deleteConfirm(boolean iconOnly, Runnable onClick) {
        return builder(ButtonType.DELETE_CONFIRM).iconOnly(iconOnly, onClick);
    }

    public static VButton openDetails(boolean iconOnly, Runnable onClick) {
        return builder(ButtonType.OPEN_DETAILS).iconOnly(iconOnly, onClick);
    }

    public static VButton resetFilter(boolean iconOnly, Runnable onClick) {
        return builder(ButtonType.RESET_FILTER).iconOnly(iconOnly, onClick);
    }

    public static VButton createButton(ButtonType type, Runnable onClick) {
        return builder(type)
                .icon()
                .caption()
                .tooltip()
                .onClick(onClick)
                .build();
    }

    public static VButton createButton(@Nullable IconFactory icon, @Nullable String text, @Nullable String tooltip, @Nullable Runnable onClick) {
        VButton button = new VButton();
        handleButton(button, icon, text, tooltip, onClick);
        return button;
    }

    public static VButton createButton(@Nullable IconFactory icon, @Nullable Runnable onClick) {
        VButton button = new VButton();
        handleButton(button, icon, null, null, onClick);
        return button;
    }

    public static VButton createButton(ButtonType buttonType, boolean icon, boolean caption, boolean tooltip,
                                       Runnable onClick) {
        UI ui = UI.getCurrent();
        String translation = ui.getTranslation(buttonType.getCaption());


        VButton button = new VButton();

        Runnable effectiveOnClick;

        if (buttonType.isConfirm()) {
            effectiveOnClick = () -> Dialogs.openConfirmDelete(ui.getTranslation(buttonType.getConfirmYes()),
                    ui.getTranslation(buttonType.getConfirmNo()),
                    ui.getTranslation(buttonType.getConfirmHeadline()),
                    ui.getTranslation(buttonType.getConfirmMessage()),
                    onClick);
        } else {
            effectiveOnClick = onClick;
        }

        if (buttonType.getVariants().length > 0) {
            button.addThemeVariants(buttonType.getVariants());
        }

        String tooltipText = null;
        if (tooltip) {
            // when tooltip is given use tooltip
            // when no tooltip is given but caption
            // is hidden, use the caption as tooltip
            // otherwise the caption would be
            // displayed twice
            if (buttonType.getTooltip() != null) {
                tooltipText = ui.getTranslation(buttonType.getTooltip());
            } else if (!caption) {
                tooltipText = translation;
            }
        }

        return handleButton(button,
                icon ? buttonType.getIcon() : null,
                caption ? translation : null,
                tooltipText,
                effectiveOnClick);
    }

    private static VButton handleButton(VButton button, @Nullable IconFactory icon, @Nullable String text, @Nullable String tooltip,
                                        @Nullable Runnable onClick) {
        if (icon != null) {
            button.setIcon(icon.create());
        }
        if (text != null) {
            button.setText(text);
        }
        if (tooltip != null) {
            button.getElement().setAttribute("title", tooltip);
        }
        if (onClick != null) {
            button.addClickListener(e -> onClick.run());
        }
        return button;
    }

    @Getter
    public static class ButtonType {
        public static final ButtonType CANCEL = custom(VaadinIcon.CLOSE_SMALL, "buttonCancel", false, LUMO_TERTIARY);

        public static final ButtonType SAVE = custom(VaadinIcon.HARDDRIVE_O, "buttonSave", true);

        public static final ButtonType ADD = custom(VaadinIcon.PLUS, "buttonAdd", true);

        public static final ButtonType REFRESH = custom(VaadinIcon.REFRESH, "buttonRefresh", true);

        public static final ButtonType EDIT = custom(VaadinIcon.PENCIL, "buttonEdit", true);

        public static final ButtonType DELETE = custom(VaadinIcon.TRASH, "buttonDelete", true, LUMO_ERROR);

        public static final ButtonType DELETE_CONFIRM = customConfirm(VaadinIcon.TRASH, "buttonDelete", true, null, "confirmDelete", "cancelDelete", "deleteDataset", "areYouSure", LUMO_ERROR);

        public static final ButtonType OPEN_DETAILS = custom(VaadinIcon.SEARCH, "buttonOpenDetails", true, LUMO_CONTRAST);

        public static final ButtonType RESET_FILTER = custom(VaadinIcon.ARROW_BACKWARD, "buttonResetFilter", false, LUMO_TERTIARY);

        public static final ButtonType CLOSE = custom(VaadinIcon.CLOSE_SMALL, "buttonClose", true, LUMO_TERTIARY);

        public static final ButtonType RELOAD = custom(VaadinIcon.REFRESH, "buttonReload", true);

        public static final ButtonType YES = custom(VaadinIcon.CHECK, "buttonYes", true);

        public static final ButtonType NO = custom(VaadinIcon.CLOSE, "buttonNo", true, LUMO_ERROR);

        private final IconFactory icon;

        private final String caption;

        private final ButtonVariant[] variants;

        private final boolean defaultWithIcon;

        private final String tooltip;

        private final boolean confirm;

        private final String confirmYes;

        private final String confirmNo;

        private final String confirmHeadline;

        private final String confirmMessage;

        private ButtonType(IconFactory icon, String caption, boolean defaultWithIcon, String tooltip,
                           ButtonVariant... variants) {
            this.icon = icon;
            this.caption = caption;
            this.defaultWithIcon = defaultWithIcon;
            this.tooltip = tooltip;
            this.confirm = false;
            this.confirmYes = null;
            this.confirmNo = null;
            this.confirmHeadline = null;
            this.confirmMessage = null;
            this.variants = variants;
        }

        public ButtonType(IconFactory icon, String caption, boolean defaultWithIcon, String tooltip, String confirmYes,
                          String confirmNo, String confirmHeadline, String confirmMessage, ButtonVariant... variants) {
            this.icon = icon;
            this.caption = caption;
            this.variants = variants;
            this.defaultWithIcon = defaultWithIcon;
            this.tooltip = tooltip;
            this.confirm = true;
            this.confirmYes = confirmYes;
            this.confirmNo = confirmNo;
            this.confirmHeadline = confirmHeadline;
            this.confirmMessage = confirmMessage;
        }

        public static ButtonType custom(IconFactory icon, String caption, boolean defaultWithIcon, ButtonVariant... variants) {
            return custom(icon, caption, defaultWithIcon, null, variants);
        }

        public static ButtonType custom(IconFactory icon, String caption, boolean defaultWithIcon, String tooltip,
                                        ButtonVariant... variants) {
            return new ButtonType(icon, caption, defaultWithIcon, tooltip, variants);
        }

        public static ButtonType customConfirm(IconFactory icon, String baseI18N, boolean defaultWithIcon,
                                               ButtonVariant... variants) {
            return customConfirm(icon,
                    baseI18N + ".caption",
                    defaultWithIcon,
                    baseI18N + ".tooltip",
                    baseI18N + ".confirm",
                    baseI18N + ".cancel",
                    baseI18N + ".headline",
                    baseI18N + ".message",
                    variants);
        }

        public static ButtonType customConfirm(IconFactory icon, String caption, boolean defaultWithIcon,
                                               String tooltip, String confirmYes, String confirmNo,
                                               String confirmHeadline, String confirmMessage,
                                               ButtonVariant... variants) {
            return new ButtonType(icon,
                    caption,
                    defaultWithIcon,
                    tooltip,
                    confirmYes,
                    confirmNo,
                    confirmHeadline,
                    confirmMessage,
                    variants);
        }
    }

    public static class ButtonBuilder {
        private final ButtonType type;

        private boolean icon, caption, tooltip;

        private Runnable onClick;

        private ButtonBuilder(ButtonType type) {
            this.type = type;
        }

        public ButtonBuilder icon() {
            this.icon = true;
            return this;
        }

        public ButtonBuilder caption() {
            this.caption = true;
            return this;
        }

        public ButtonBuilder tooltip() {
            this.tooltip = true;
            return this;
        }

        public ButtonBuilder onClick(Runnable onClick) {
            this.onClick = onClick;
            return this;
        }

        public VButton build() {
            return createButton(type, icon, caption, tooltip, onClick);
        }

        VButton iconOnly(boolean iconOnly, Runnable onClick) {
            if (iconOnly) {
                return iconWithTooltip(onClick);
            }
            return createButton(type, type.isDefaultWithIcon(), true, false, onClick);
        }

        VButton justButton(boolean iconOnly) {
            if (iconOnly) {
                return iconWithTooltip(onClick);
            }
            return createButton(type, type.isDefaultWithIcon(), true, false, onClick);
        }

        public VButton iconWithTooltip(Runnable onClick) {
            return createButton(type, true, false, true, onClick);
        }

        public VButton captionOnly(Runnable onClick) {
            return createButton(type, false, true, false, onClick);
        }
    }
}
