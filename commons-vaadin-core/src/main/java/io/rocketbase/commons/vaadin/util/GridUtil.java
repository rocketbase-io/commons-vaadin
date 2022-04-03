package io.rocketbase.commons.vaadin.util;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.FooterRow;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.function.ValueProvider;
import io.rocketbase.commons.vaadin.renderer.InstantRenderer;
import org.vaadin.firitin.components.html.VLabel;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;

public abstract class GridUtil<T> {

    private static DateTimeFormatter DATE_TIME_SHORT_FORMAT = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
            .withZone(ZoneId.systemDefault());

    public static <T> Grid<T> addModified(Grid<T> grid, ValueProvider<T, Instant> modified, ValueProvider<T, String> modifiedBy) {
        grid.addColumn(new InstantRenderer<>(modified, DATE_TIME_SHORT_FORMAT))
                .setHeader(UI.getCurrent().getTranslation("modified"))
                .setAutoWidth(false)
                .setFlexGrow(0)
                .setSortable(true)
                .setComparator(modifiedBy)
                .setWidth("160px");
        grid.addColumn(modifiedBy)
                .setHeader(UI.getCurrent().getTranslation("modifiedBy"))
                .setAutoWidth(false)
                .setFlexGrow(0)
                .setSortable(true)
                .setWidth("140px");
        return grid;
    }

    public static <T> Grid.Column<T> addId(Grid<T> grid, ValueProvider<T, Object> id) {
        Grid.Column<T> column = grid.addColumn(id)
                .setHeader(UI.getCurrent().getTranslation("id"))
                .setAutoWidth(false)
                .setFlexGrow(0)
                .setSortable(true)
                .setWidth("100px")
                .setTextAlign(ColumnTextAlign.END)
                .setKey("id");

        column.setVisible(false);

        GridContextMenu<T> contextMenu = new GridContextMenu<>(grid);
        contextMenu.addItem("toggleId", e -> grid.getColumnByKey("id").setVisible(!grid.getColumnByKey("id").isVisible()));
        contextMenu.setDynamicContentHandler(selection -> {
            if (selection == null) {
                return true;
            }
            return false;
        });
        return column;
    }

    public static <T> Grid<T> setTotal(GridFilterUtil<T> gridFilter, int total) {
        return setTotal(gridFilter.getGrid(), total);
    }

    public static <T> Grid<T> setTotal(Grid<T> grid, int total) {
        List<FooterRow> footerRows = grid.getFooterRows();
        FooterRow footerRow;
        if (footerRows.isEmpty()) {
            grid.addClassName("total-count");
            footerRow = grid.appendFooterRow();
        } else {
            footerRow = footerRows.get(0);
        }

        VLabel totalCount = new VLabel(UI.getCurrent().getTranslation("totalCount", total)).withClassName("total-count");
        Grid.Column<T> column = grid.getColumns().get(0).isVisible() ? grid.getColumns().get(0) : grid.getColumns().get(1);
        footerRow.getCell(column)
                .setComponent(totalCount);
        return grid;
    }

    public static <T> Grid.Column<T> configureIconColumn(Grid.Column<T> column) {
        Integer size = column.getGrid().hasThemeName(GridVariant.LUMO_COMPACT.getVariantName()) ? 52 : 70;
        if (column.getGrid().hasThemeName(GridVariant.LUMO_COLUMN_BORDERS.getVariantName())) {
            size += 6;
        }
        return column.setFlexGrow(0)
                .setWidth(String.format("%dpx", size))
                .setAutoWidth(false);
    }

    public static void fixColumnSize(String width, ColumnTextAlign align, Grid.Column... columns) {
        for (Grid.Column c : columns) {
            c.setSortable(true);
            c.setWidth(width);
            c.setFlexGrow(0);
            c.setAutoWidth(false);
            c.setTextAlign(align);
            c.setResizable(true);
        }
    }
}