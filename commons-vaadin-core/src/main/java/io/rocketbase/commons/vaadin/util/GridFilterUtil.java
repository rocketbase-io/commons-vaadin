package io.rocketbase.commons.vaadin.util;

import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.function.ValueProvider;
import io.rocketbase.commons.util.Nulls;
import lombok.Getter;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.vaadin.firitin.components.button.VButton;
import org.vaadin.firitin.components.combobox.VComboBox;
import org.vaadin.firitin.components.textfield.VNumberField;
import org.vaadin.firitin.components.textfield.VTextField;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GridFilterUtil<T> {

    @Getter
    protected final Grid<T> grid;

    @Getter
    protected HeaderRow filterRow;
    protected Set<HasValue> filterComponents;
    protected List<Consumer<Collection<T>>> itemsConsumers;
    protected Map<Grid.Column<T>, Pair<ValueProvider<T, ?>, SerializablePredicate<?>>> columnFilters;


    static final class Pair<S, T> {

        private final S first;
        private final T second;

        private Pair(S first, T second) {

            Assert.notNull(first, "First must not be null!");
            Assert.notNull(second, "Second must not be null!");

            this.first = first;
            this.second = second;
        }

        /**
         * Creates a new {@link Pair} for the given elements.
         *
         * @param first  must not be {@literal null}.
         * @param second must not be {@literal null}.
         * @return
         */
        public static <S, T> Pair<S, T> of(S first, T second) {
            return new Pair<>(first, second);
        }

        /**
         * Returns the first element of the {@link Pair}.
         *
         * @return
         */
        public S getFirst() {
            return first;
        }

        /**
         * Returns the second element of the {@link Pair}.
         *
         * @return
         */
        public T getSecond() {
            return second;
        }

        /**
         * A collector to create a {@link Map} from a {@link Stream} of {@link Pair}s.
         *
         * @return
         */
        public static <S, T> Collector<Pair<S, T>, ?, Map<S, T>> toMap() {
            return Collectors.toMap(Pair::getFirst, Pair::getSecond);
        }

        /*
         * (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(@Nullable Object o) {

            if (this == o) {
                return true;
            }

            if (!(o instanceof Pair)) {
                return false;
            }

            Pair<?, ?> pair = (Pair<?, ?>) o;

            if (!ObjectUtils.nullSafeEquals(first, pair.first)) {
                return false;
            }

            return ObjectUtils.nullSafeEquals(second, pair.second);
        }

        /*
         * (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            int result = ObjectUtils.nullSafeHashCode(first);
            result = 31 * result + ObjectUtils.nullSafeHashCode(second);
            return result;
        }

        /*
         * (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return String.format("%s->%s", this.first, this.second);
        }
    }


    public enum NumberEquals {
        GTE("≥"),
        LTE("≤"),
        EQ("=");

        @Getter
        private String caption;

        NumberEquals(String caption) {
            this.caption = caption;
        }
    }

    public enum StringEquals {
        CONTAINS("*"),
        BEGINS("begins"),
        ENDS("ends"),
        EXACT("=");


        @Getter
        private String caption;

        StringEquals(String caption) {
            this.caption = caption;
        }
    }

    public GridFilterUtil(Grid<T> grid) {
        this.grid = grid;
        if (!(grid.getDataProvider() instanceof ListDataProvider)) {
            throw new IllegalArgumentException("only ListDataProvider is supported");
        }

        itemsConsumers = new ArrayList<>();
        columnFilters = new HashMap<>();
        filterComponents = new HashSet<>();
    }

    public Grid.Column<T> addTextFilter(Grid.Column<T> column, ValueProvider<T, String> valueProvider) {
        return this.addTextFilter(column, valueProvider, null);
    }

    public Grid.Column<T> addTextFilter(Grid.Column<T> column, ValueProvider<T, String> valueProvider, StringEquals... comparators) {
        checkInitFilterRow();

        Set<StringEquals> comparatorOptions = new LinkedHashSet<>();
        if (comparators != null && comparators.length > 0) {
            for (StringEquals c : comparators) {
                comparatorOptions.add(c);
            }
        }
        AtomicReference<StringEquals> reference = new AtomicReference<>(comparatorOptions.isEmpty() ? StringEquals.CONTAINS : comparatorOptions.iterator().next());

        Consumer<String> filter = (value) -> {
            StringEquals comparator = reference.get();
            if (StringEquals.EXACT.equals(comparator)) {
                columnFilters.put(column, Pair.of(valueProvider, (SerializablePredicate<String>) s -> Nulls.notNull(s).equalsIgnoreCase(Nulls.notNull(value))));
            }
            if (!StringUtils.isEmpty(value)) {
                String loweredValue = value.toLowerCase();
                switch (comparator) {
                    case CONTAINS:
                        columnFilters.put(column, Pair.of(valueProvider, (SerializablePredicate<String>) s -> s != null && s.toLowerCase().contains(loweredValue)));
                        break;
                    case BEGINS:
                        columnFilters.put(column, Pair.of(valueProvider, (SerializablePredicate<String>) s -> s != null && s.toLowerCase().startsWith(loweredValue)));
                        break;
                    case ENDS:
                        columnFilters.put(column, Pair.of(valueProvider, (SerializablePredicate<String>) s -> s != null && s.toLowerCase().endsWith(loweredValue)));
                        break;
                }
            } else {
                columnFilters.remove(column);
            }
            updateFilter();
        };

        VTextField textField = new VTextField().withClearButtonVisible(true)
                .withFullWidth()
                .withThemeVariants(TextFieldVariant.LUMO_SMALL)
                .withValueChangeMode(ValueChangeMode.LAZY)
                .withValueChangeTimeout(200)
                .withValueChangeListener(e -> {
                    filter.accept(e.getValue());
                });
        filterComponents.add(textField);

        if (comparatorOptions.size() > 1) {
            ComboBox<StringEquals> comparatorSelect = new VComboBox<StringEquals>(null, comparatorOptions)
                    .withSizeUndefined()
                    .withThemeSmall()
                    .withItemLabelGenerator(StringEquals::getCaption)
                    .withValue(reference.get())
                    .withAllowCustomValue(false)
                    .withValueChangeListener(e -> {
                        reference.set(e.getValue());
                        filter.accept(textField.getValue());
                    })
                    .withWidth("90px");
            textField.setSuffixComponent(comparatorSelect);
        }

        filterRow.getCell(column)
                .setComponent(textField);
        return column;
    }

    public Grid.Column<T> addNumberFilter(Grid.Column<T> column, ValueProvider<T, ? extends Number> valueProvider) {
        return this.addNumberFilter(column, valueProvider, NumberEquals.GTE);
    }

    public Grid.Column<T> addNumberFilter(Grid.Column<T> column, ValueProvider<T, ? extends Number> valueProvider, NumberEquals initalState) {
        checkInitFilterRow();

        AtomicInteger currentIndex = new AtomicInteger(initalState.ordinal());

        Consumer<Double> filter = (value) -> {
            if (!StringUtils.isEmpty(value)) {
                columnFilters.put(column, Pair.of(valueProvider, (SerializablePredicate<? extends Number>) s -> {
                    if (s == null) {
                        return false;
                    }
                    switch (currentIndex.get()) {
                        case 0:
                            return s.doubleValue() >= value.doubleValue();
                        case 1:
                            return s.doubleValue() <= value.doubleValue();
                        default:
                            return s.doubleValue() == value.doubleValue();
                    }
                }));
            } else {
                columnFilters.remove(column);
            }
            updateFilter();
        };

        VNumberField numberField = new VNumberField()
                .withClearButtonVisible(true)
                .withFullWidth()
                .withValueChangeMode(ValueChangeMode.LAZY);

        Button compareTypeButton = new VButton(NumberEquals.values()[currentIndex.get()].getCaption())
                .withThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        compareTypeButton.addClickListener(e -> {
            currentIndex.set((currentIndex.get() + 1) % NumberEquals.values().length);
            filter.accept(numberField.getValue());
            compareTypeButton.setText(NumberEquals.values()[currentIndex.get()].getCaption());
        });

        numberField.addValueChangeListener(e -> filter.accept((e.getValue())));
        numberField.setPrefixComponent(compareTypeButton);

        numberField.addThemeVariants(TextFieldVariant.LUMO_SMALL, TextFieldVariant.LUMO_ALIGN_RIGHT);
        numberField.setValueChangeTimeout(200);
        filterComponents.add(numberField);

        filterRow.getCell(column)
                .setComponent(numberField);
        return column;
    }

    public Grid.Column<T> addEqualFilter(Grid.Column<T> column, ValueProvider<T, ?> valueProvider) {
        return this.addEqualFilter(column, valueProvider, null);
    }

    public Grid.Column<T> addEqualFilter(Grid.Column<T> column, ValueProvider<T, ?> valueProvider, ItemLabelGenerator itemLabelGenerator) {
        checkInitFilterRow();

        VComboBox comboBox = new VComboBox<>()
                .withClearButtonVisible(true)
                .withFullWidth()
                .withThemeSmall();
        if (itemLabelGenerator != null) {
            comboBox.setItemLabelGenerator(itemLabelGenerator);
        }

        comboBox.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                columnFilters.put(column, Pair.of(valueProvider, (SerializablePredicate) v -> e.getValue().equals(v)));
            } else {
                columnFilters.remove(column);
            }
            updateFilter();
        });

        Consumer<Collection<T>> consumer = (items) -> {
            Set options = new TreeSet();
            for (T i : items) {
                Object value = valueProvider.apply(i);
                if (value != null) {
                    options.add(value);
                }
            }
            comboBox.setItems(options);
            comboBox.clear();
        };
        itemsConsumers.add(consumer);
        // initialize
        consumer.accept(((ListDataProvider<T>) grid.getDataProvider()).getItems());

        filterComponents.add(comboBox);

        filterRow.getCell(column)
                .setComponent(comboBox);
        return column;
    }

    /**
     * needs to get triggered after first column attach otherwise headerrow will be just one without caption
     */
    protected void checkInitFilterRow() {
        if (filterRow == null) {
            filterRow = grid.appendHeaderRow();
        }
    }

    protected void updateFilter() {
        ListDataProvider<T> dataProvider = (ListDataProvider<T>) grid.getDataProvider();
        dataProvider.clearFilters();
        for (Pair<ValueProvider<T, ?>, SerializablePredicate<?>> filterPair : columnFilters.values()) {
            dataProvider.addFilter((ValueProvider) filterPair.getFirst(), (SerializablePredicate) filterPair.getSecond());
        }
        GridUtil.setTotal(grid, dataProvider.size(new Query<>(dataProvider.getFilter())));
    }

    public void clearFilters() {
        filterComponents.forEach(HasValue::clear);
    }

    public void reset() {
        clearFilters();
        updateFilter();

        if (!itemsConsumers.isEmpty()) {
            ListDataProvider<T> dataProvider = (ListDataProvider<T>) grid.getDataProvider();
            for (Consumer<Collection<T>> c : itemsConsumers) {
                c.accept(dataProvider.getItems());
            }
        }
    }

    public void setItems(Collection<T> items) {
        grid.setItems(items);
        reset();
    }

    public void deselectAll() {
        grid.deselectAll();
    }
}
