package io.rocketbase.commons.vaadin.renderer;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.function.ValueProvider;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BooleanRenderer<ITEM> extends ComponentRenderer<Component, ITEM> {

    private final ValueProvider<ITEM, Boolean> valueProvider;

    @Override
    public Component createComponent(ITEM item) {
        Boolean value = (item == null ? null : valueProvider.apply(item));
        if (value == null) {
            return new Div();
        }
        return value ? VaadinIcon.CHECK_SQUARE.create() : VaadinIcon.THIN_SQUARE.create();
    }


}
