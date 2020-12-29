package net.marvk.fs.vatsim.map.view.datatable;

import javafx.beans.value.ObservableStringValue;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.Pane;
import javafx.scene.text.TextFlow;
import net.marvk.fs.vatsim.map.data.Data;
import net.marvk.fs.vatsim.map.view.TextFlowHighlighter;

import java.util.Comparator;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class ColumnBuilderFactory<Model extends Data> {
    private final SimpleTableViewModel<Model> viewModel;
    private final TextFlowHighlighter textFlowHighlighter;
    private final Consumer<TableColumn<Model, ?>> columnConsumer;

    public ColumnBuilderFactory(
            final SimpleTableViewModel<Model> viewModel,
            final TextFlowHighlighter textFlowHighlighter,
            final Consumer<TableColumn<Model, ?>> columnConsumer
    ) {
        this.viewModel = viewModel;
        this.textFlowHighlighter = textFlowHighlighter;
        this.columnConsumer = columnConsumer;
    }

    public <CellValue> TitleStep<Model, CellValue> newBuilder() {
        return new Builder<>();
    }

    public final class Builder<CellValue> implements
            TitleStep<Model, CellValue>,
            ComparableStep<Model, CellValue>,
            StringMapperStep<Model, CellValue>,
            ValueStep<Model, CellValue> {

        private final TableColumn<Model, CellValue> result;
        private boolean mono;
        private BiFunction<CellValue, String, String> stringMapper;
        private BiFunction<Model, ObservableStringValue, ObservableValue<CellValue>> valueFactory;
        private boolean valueNullable;

        private Builder() {
            this.result = new TableColumn<>();
            this.result.setSortable(false);
        }

        @Override
        public ValueStep<Model, CellValue> title(final String title) {
            this.result.setText(title);
            return this;
        }

        @Override
        public StringMapperStep<Model, CellValue> objectObservableValueFactory(final BiFunction<Model, ObservableStringValue, ObservableValue<CellValue>> valueFactory) {
            this.valueFactory = valueFactory;
            return this;
        }

        @Override
        public ComparableStep<Model, CellValue> toStringMapper(final BiFunction<CellValue, String, String> stringMapper, final boolean nullable) {
            this.valueNullable = nullable;
            this.stringMapper = stringMapper;
            return this;
        }

        @SuppressWarnings("unchecked")
        @Override
        public ComparableStep<Model, String> stringObservableValueFactory(final BiFunction<Model, ObservableStringValue, ObservableStringValue> valueFactory) {
            this.valueFactory = (model, query) -> (ObservableValue<CellValue>) valueFactory.apply(model, query);
            return (ComparableStep<Model, String>) toStringMapper(e -> (String) e);
        }

        @Override
        public MonoStep<Model, CellValue> sortable() {
            this.result.setSortable(true);
            return this;
        }

        @Override
        public MonoStep<Model, CellValue> sortable(final Comparator<CellValue> comparator) {
            this.result.setComparator(comparator);
            this.result.setSortable(true);
            return this;
        }

        @Override
        public BuildStep mono(final boolean mono) {
            this.mono = mono;
            return this;
        }

        @Override
        public void build() {
            result.setCellValueFactory(param -> valueFactory.apply(param.getValue(), viewModel.queryProperty()));
            result.setCellFactory(param -> new DataTableCell<>((cellValue, query) -> textFlow(stringMapper.apply(cellValue, query), mono), valueNullable));
            result.setReorderable(false);
            columnConsumer.accept(result);
        }

        private TextFlow textFlow(final String cellValue, final boolean mono) {
            if (viewModel.getQuery() == null || viewModel.getQuery().isBlank()) {
                return new TextFlow(textFlowHighlighter.createSimpleTextFlow(cellValue, mono));
            }

            return new TextFlow(textFlowHighlighter.createHighlightedTextFlow(cellValue, viewModel.getPattern(), mono));
        }
    }

    public interface TitleStep<Model extends Data, CellValue> {
        ValueStep<Model, CellValue> title(final String s);
    }

    public interface ComparableStep<Model extends Data, CellValue> extends MonoStep<Model, CellValue>, BuildStep {
        MonoStep<Model, CellValue> sortable();

        MonoStep<Model, CellValue> sortable(final Comparator<CellValue> comparator);
    }

    public interface MonoStep<Model extends Data, CellValue> extends BuildStep {
        BuildStep mono(final boolean mono);
    }

    public interface BuildStep {
        void build();
    }

    public interface StringMapperStep<Model extends Data, CellValue> {
        default ComparableStep<Model, CellValue> toStringMapper(final Function<CellValue, String> stringMapper) {
            return toStringMapper((cellValue, ignored) -> stringMapper.apply(cellValue));
        }

        default ComparableStep<Model, CellValue> toStringMapper(final Function<CellValue, String> stringMapper, final boolean nullable) {
            return toStringMapper((cellValue, ignored) -> stringMapper.apply(cellValue), nullable);
        }

        default ComparableStep<Model, CellValue> toStringMapper(final BiFunction<CellValue, String, String> stringMapper) {
            return toStringMapper(stringMapper, false);
        }

        ComparableStep<Model, CellValue> toStringMapper(final BiFunction<CellValue, String, String> stringMapper, final boolean nullable);
    }

    public interface ValueStep<Model extends Data, CellValue> {
//        default StringMapperStep<Model, CellValue> objectValueFactory(final Function<Model, CellValue> valueFactory) {
//            return objectValueFactory((model, s) -> valueFactory.apply(model));
//        }
//
//        default StringMapperStep<Model, CellValue> objectValueFactory(final BiFunction<Model, ObservableStringValue, CellValue> valueFactory) {
//            return objectObservableValueFactory((model, query) -> new SimpleObjectProperty<>(valueFactory.apply(model, query)));
//        }

        default StringMapperStep<Model, CellValue> objectObservableValueFactory(final Function<Model, ObservableValue<CellValue>> valueFactory) {
            return objectObservableValueFactory((model, ignored) -> valueFactory.apply(model));
        }

        StringMapperStep<Model, CellValue> objectObservableValueFactory(final BiFunction<Model, ObservableStringValue, ObservableValue<CellValue>> valueFactory);

//        default ComparableStep<Model, String> stringValueFactory(final Function<Model, String> valueFactory) {
//
//            return stringValueFactory((model, s) -> valueFactory.apply(model));
//        }
//
//        default ComparableStep<Model, String> stringValueFactory(final BiFunction<Model, ObservableStringValue, String> valueFactory) {
//            return stringObservableValueFactory((model, query) -> new SimpleStringProperty(valueFactory.apply(model, query)));
//        }

        default ComparableStep<Model, String> stringObservableValueFactory(final Function<Model, ObservableStringValue> valueFactory) {
            return stringObservableValueFactory((model, ignored) -> valueFactory.apply(model));
        }

        ComparableStep<Model, String> stringObservableValueFactory(final BiFunction<Model, ObservableStringValue, ObservableStringValue> valueFactory);
    }

    private class DataTableCell<CellValue> extends TableCell<Model, CellValue> {
        private final BiFunction<CellValue, String, Pane> paneSupplier;
        private final boolean valueNullable;

        public DataTableCell(final BiFunction<CellValue, String, Pane> paneSupplier, final boolean valueNullable) {
            this.paneSupplier = paneSupplier;
            this.valueNullable = valueNullable;
        }

        @Override
        protected void updateItem(final CellValue item, final boolean empty) {
            super.updateItem(item, empty);

            if (empty || (item == null && !valueNullable)) {
                setText(null);
                setGraphic(null);
            } else {
                setGraphic(paneSupplier.apply(item, viewModel.getQuery()));
            }
        }
    }
}
