package net.marvk.fs.vatsim.map.data;

import javafx.beans.property.*;
import net.marvk.fs.vatsim.api.data.VatsimFlightInformationRegion;

import java.util.Objects;

public class FlightInformationRegion implements Settable<VatsimFlightInformationRegion>, Data {
    private final StringProperty icao = new SimpleStringProperty();
    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty prefixPosition = new SimpleStringProperty();
    private final StringProperty unknown1 = new SimpleStringProperty();

    private final ReadOnlyObjectWrapper<FlightInformationRegionBoundary> boundary =
            RelationshipReadOnlyObjectWrapper.withOtherList(this, FlightInformationRegionBoundary::getFlightInformationRegionsWritable);

    private final ReadOnlyListWrapper<Controller> controllers =
            RelationshipReadOnlyListWrapper.withOtherProperty(this, Controller::workingFlightInformationRegionPropertyWritable);

    @Override
    public void setFromModel(final VatsimFlightInformationRegion model) {
        Objects.requireNonNull(model);

        icao.set(model.getIcao());
        name.set(model.getName());
        prefixPosition.set(model.getPrefixPosition());
        unknown1.set(model.getUnknown1());
    }

    public String getIcao() {
        return icao.get();
    }

    public ReadOnlyStringProperty icaoProperty() {
        return icao;
    }

    public String getName() {
        return name.get();
    }

    public ReadOnlyStringProperty nameProperty() {
        return name;
    }

    public String getPrefixPosition() {
        return prefixPosition.get();
    }

    public ReadOnlyStringProperty prefixPositionProperty() {
        return prefixPosition;
    }

    public String getUnknown1() {
        return unknown1.get();
    }

    public ReadOnlyStringProperty unknown1Property() {
        return unknown1;
    }

    public FlightInformationRegionBoundary getBoundary() {
        return boundary.get();
    }

    public ObjectProperty<FlightInformationRegionBoundary> boundaryPropertyWritable() {
        return boundary;
    }

    public ReadOnlyObjectProperty<FlightInformationRegionBoundary> boundaryProperty() {
        return boundary.getReadOnlyProperty();
    }

    SimpleListProperty<Controller> getControllersWritable() {
        return controllers;
    }

    public ReadOnlyListProperty<Controller> getControllers() {
        return controllers.getReadOnlyProperty();
    }

    @Override
    public <R> R visit(final DataVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
