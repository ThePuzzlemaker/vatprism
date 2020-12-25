package net.marvk.fs.vatsim.map.configuration;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import net.marvk.fs.vatsim.api.*;
import net.marvk.fs.vatsim.map.data.*;
import org.geotools.data.shapefile.files.ShpFiles;
import org.geotools.data.shapefile.shp.ShapefileReader;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPolygon;

import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class AppModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(VatsimApiUrlProvider.class).to(UrlProviderV1.class).in(Singleton.class);
        bind(VatsimApiDataSource.class).to(HttpDataSource.class).in(Singleton.class);
        bind(AirportRepository.class).in(Singleton.class);
        bind(ClientRepository.class).in(Singleton.class);
        bind(FlightInformationRegionRepository.class).in(Singleton.class);
        bind(FlightInformationRegionBoundaryRepository.class).in(Singleton.class);
        bind(UpperInformationRegionRepository.class).in(Singleton.class);
        bind(InternationalDateLineRepository.class).in(Singleton.class);
    }

    @Provides
    @Singleton
    public VatsimApi vatsimApi(final VatsimApiDataSource dataSource) {
        final SimpleVatsimApi api = new SimpleVatsimApi(dataSource);
        final CachedVatsimApi cached = new CachedVatsimApi(api, Duration.ofSeconds(3));
        return cached;
    }

    @Provides
    @Named("worldShapefileUrl")
    public List<String> worldShapefileUrls() {
        return Collections.singletonList("ne_50m_land");
    }

    @Provides
    @Named("lakesShapefileUrl")
    public List<String> lakesShapefileUrl() {
        return List.of(
//                "ne_10m_lakes_north_america",
//                "ne_10m_lakes_europe",
                "ne_10m_lakes"
        );
    }

    @Provides
    @Singleton
    @Named("world")
    public List<Polygon> world(@Named("worldShapefileUrl") final List<String> shapefileUrls) throws IOException {
        return loadPolygons(shapefileUrls);
    }

    @Provides
    @Singleton
    @Named("lakes")
    public List<Polygon> lakes(@Named("lakesShapefileUrl") final List<String> shapefileUrls) throws IOException {
        return loadPolygons(shapefileUrls);
    }

    private List<Polygon> loadPolygons(final Collection<String> names) throws IOException {
        final Collection<List<Polygon>> result = new ArrayList<>();

        for (final String name : names) {
            result.add(loadPolygons(name));
        }

        return result.stream().flatMap(Collection::stream).collect(Collectors.toUnmodifiableList());
    }

    private List<Polygon> loadPolygons(final String name) throws IOException {
        final ShapefileReader shapefileReader = new ShapefileReader(
                new ShpFiles(shpUrl(name)),
                false,
                false,
                new GeometryFactory()
        );

//        final DataStore dataStore = DataStoreFinder.getDataStore(Map.of("url", shpUrl(name)));
//
//        final SimpleFeatureSource featureSource = dataStore.getFeatureSource(dataStore.getTypeNames()[0]);
//
//        final SimpleFeatureIterator features = featureSource.getFeatures().features();

        try {
            final List<Polygon> result = new ArrayList<>();

            while (shapefileReader.hasNext()) {
                final ShapefileReader.Record record = shapefileReader.nextRecord();

                final Object shape = record.shape();

                if (shape instanceof MultiLineString) {
                    final MultiLineString mls = (MultiLineString) shape;

                    for (int i = 0; i < mls.getNumGeometries(); i++) {
                        result.add(new Polygon(mls.getGeometryN(i)));
                    }
                } else if (shape instanceof MultiPolygon) {
                    final MultiPolygon mp = (MultiPolygon) shape;

                    for (int i = 0; i < mp.getNumGeometries(); i++) {
                        final Geometry geometryN = mp.getGeometryN(i);
                        final org.locationtech.jts.geom.Polygon polygon = (org.locationtech.jts.geom.Polygon) geometryN;

                        result.add(new Polygon(polygon));
                    }
                }
            }

            return result;
        } finally {
            shapefileReader.close();
        }
    }

    private static URL shpUrl(final String name) {
        return url(name, "shp");
    }

    private static URL url(final String name, final String extension) {
        return AppModule.class.getResource("/net/marvk/fs/vatsim/map/world/%s/%s.%s".formatted(name, name, extension));
    }
}
