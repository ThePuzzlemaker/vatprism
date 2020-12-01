package net.marvk.fs.vatsim.map.data;

import com.google.inject.Inject;
import com.google.inject.Provider;
import javafx.geometry.Point2D;
import lombok.extern.slf4j.Slf4j;
import net.marvk.fs.vatsim.api.VatsimApi;
import net.marvk.fs.vatsim.api.VatsimApiException;
import net.marvk.fs.vatsim.api.data.VatsimAirport;
import net.marvk.fs.vatsim.map.GeomUtil;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
public class AirportRepository extends ProviderRepository<Airport, AirportRepository.VatsimAirportWrapper> {
    private final Lookup<Airport> icaoLookup = Lookup.fromProperty(Airport::getIcao);
    private final Lookup<Airport> iataLookup = Lookup.fromCollection(Airport::getIatas);

    @Inject
    public AirportRepository(final VatsimApi vatsimApi, final Provider<Airport> provider) {
        super(vatsimApi, provider);
    }

    @Override
    protected String keyFromModel(final VatsimAirportWrapper vatsimAirport) {
        return vatsimAirport.getIcao();
    }

    @Override
    protected String keyFromViewModel(final Airport airport) {
        return airport.getIcao();
    }

    @Override
    protected Collection<VatsimAirportWrapper> extractModelList(final VatsimApi api) throws VatsimApiException {
        return api
                .vatSpy()
                .getAirports()
                .stream()
                .filter(e -> !e.getPseudo())
                .collect(Collectors.groupingBy(VatsimAirport::getIcao))
                .values()
                .stream()
                .map(VatsimAirportWrapper::new)
                .collect(Collectors.toList());
    }

    @Override
    protected void onAdd(final Airport toAdd, final VatsimAirportWrapper vatsimAirport) {
        icaoLookup.put(toAdd);
        iataLookup.put(toAdd);
    }

    public List<Airport> getByIcao(final String icao) {
        return icaoLookup.get(icao);
    }

    public List<Airport> getByIata(final String iata) {
        return iataLookup.get(iata);
    }

    public static final class VatsimAirportWrapper {
        private final String icao;
        private final List<String> names;
        private final Point2D position;
        private final List<String> iatas;
        private final String fir;
        private final boolean pseudo;

        public VatsimAirportWrapper(final List<VatsimAirport> airports) {
            this.icao = icao(airports);
            this.names = names(airports);
            this.position = position(airports);
            this.iatas = iatas(airports);
            this.fir = firs(airports);
            this.pseudo = pseudo(airports);
        }

        public String getIcao() {
            return icao;
        }

        public List<String> getNames() {
            return names;
        }

        public Point2D getPosition() {
            return position;
        }

        public List<String> getIatas() {
            return iatas;
        }

        public String getFir() {
            return fir;
        }

        public boolean isPseudo() {
            return pseudo;
        }

        private boolean pseudo(final List<VatsimAirport> airports) {
            final List<Boolean> pseudos = airports
                    .stream()
                    .map(VatsimAirport::getPseudo)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());

            if (pseudos.size() != 1) {
                log.warn("Airports with same ICAO (" + icao + ") have differing pseudos");
            }

            return pseudos.get(0);
        }

        private String firs(final List<VatsimAirport> airports) {
            final List<String> firs = airports
                    .stream()
                    .map(VatsimAirport::getFlightInformationRegion)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());

            if (firs.size() != 1) {
                log.warn("Airports with same ICAO (" + icao + ") have differing firs");
            }

            return firs.get(0);
        }

        private List<String> iatas(final List<VatsimAirport> airports) {
            return airports
                    .stream()
                    .map(VatsimAirport::getIataLid)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
        }

        private String icao(final List<VatsimAirport> airports) {
            final List<String> icaos = airports
                    .stream()
                    .map(VatsimAirport::getIcao)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());

            if (icaos.size() != 1) {
                log.warn("Airports passed to VatsimAirportWrapper have differing icaos");
            }

            return icaos.get(0);
        }

        private List<String> names(final List<VatsimAirport> airports) {
            return airports.stream().map(VatsimAirport::getName).collect(Collectors.toList());
        }

        private Point2D position(final List<VatsimAirport> airports) {
            final List<Point2D> points = airports
                    .stream()
                    .map(e -> GeomUtil.parsePoint(e.getPosition()))
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());

            if (points.size() != 1) {
                log.warn("Airports with same ICAO (" + icao + ") have differing positions");
            }

            return points.get(0);
        }
    }
}
