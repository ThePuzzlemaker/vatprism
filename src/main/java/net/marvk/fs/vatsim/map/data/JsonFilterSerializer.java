package net.marvk.fs.vatsim.map.data;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import javafx.scene.paint.Color;

import java.lang.reflect.Type;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class JsonFilterSerializer implements Serializer<Filter> {
    private final Gson gson;

    public JsonFilterSerializer() {
        gson = new GsonBuilder()
                .registerTypeAdapter(Filter.class, new FilterAdapter())
                .registerTypeAdapter(Color.class, new ColorAdapter())
                .registerTypeAdapter(Filter.StringPredicate.class, new StringPredicateAdapter())
                .setPrettyPrinting()
                .serializeNulls()
                .create();
    }

    @Override
    public String serialize(final Filter filter) {
        return gson.toJson(filter);
    }

    @Override
    public Filter deserialize(final String s) {
        return gson.fromJson(s, Filter.class);
    }

    private interface Adapter<T> extends JsonSerializer<T>, JsonDeserializer<T> {
    }

    private static class FilterAdapter implements Adapter<Filter> {
        @Override
        public JsonElement serialize(final Filter src, final Type typeOfSrc, final JsonSerializationContext context) {
            final JsonObject result = new JsonObject();
            result.add("uuid", context.serialize(src.getUuid()));
            result.add("name", context.serialize(src.getName()));
            result.add("textColor", context.serialize(src.getTextColor()));
            result.add("backgroundColor", context.serialize(src.getBackgroundColor()));
            result.add("type", context.serialize(src.getType()));
            result.add("callsignPredicates", context.serialize(src.getCallsignPredicates()));
            result.add("callsignCidOperator", context.serialize(src.getCallsignsCidsOperator()));
            result.add("cidPredicates", context.serialize(src.getCidPredicates()));
            result.add("departurePredicates", context.serialize(src.getDepartureAirportPredicates()));
            result.add("departureArrivalOperator", context.serialize(src.getDeparturesArrivalsOperator()));
            result.add("arrivalPredicates", context.serialize(src.getArrivalAirportPredicates()));
            result.add("pilotRatings", context.serialize(src.getPilotRatings()));
            result.add("controllerRatings", context.serialize(src.getControllerRatings()));
            result.add("flightStatuses", context.serialize(src.getFlightStatuses()));
            result.add("controllerTypes", context.serialize(src.getControllerTypes()));
            result.add("flightTypes", context.serialize(src.getFlightTypes()));
            result.add("flightRules", context.serialize(src.getFlightRules()));
            result.add("flightPlanRequired", context.serialize(src.isFlightPlanRequired()));

            return result;
        }

        @Override
        public Filter deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext c) throws JsonParseException {
            final JsonObject o = json.getAsJsonObject();

            final Color textColor = deserializeToClass(o.get("textColor"), c, Color.class);
            final Color backgroundColor = deserializeToClass(o.get("backgroundColor"), c, Color.class);
            final Filter.Type type = deserializeToClass(o.get("type"), c, Filter.Type.class);
            final List<Filter.StringPredicate> callsignPredicates = deserializeToList(o.get("callsignPredicates"), c, Filter.StringPredicate.class);
            final Filter.Operator callsignCidOperator = deserializeToClass(o.get("callsignCidOperator"), c, Filter.Operator.class);
            final List<Filter.StringPredicate> cidPredicates = deserializeToList(o.get("cidPredicates"), c, Filter.StringPredicate.class);
            final List<Filter.StringPredicate> departurePredicates = deserializeToList(o.get("departurePredicates"), c, Filter.StringPredicate.class);
            final Filter.Operator departureArrivalOperator = deserializeToClass(o.get("departureArrivalOperator"), c, Filter.Operator.class);
            final List<Filter.StringPredicate> arrivalPredicates = deserializeToList(o.get("arrivalPredicates"), c, Filter.StringPredicate.class);

            final List<PilotRating> pilotRatings = deserializeToList(o.get("pilotRatings"), c, PilotRating.class);
            final List<ControllerRating> controllerRatings = deserializeToList(o.get("controllerRatings"), c, ControllerRating.class);

            final List<Filter.FlightStatus> flightStatuses = deserializeToList(o.get("flightStatuses"), c, Filter.FlightStatus.class);
            final List<ControllerType> controllerTypes = deserializeToList(o.get("controllerTypes"), c, ControllerType.class);
            final List<Filter.FlightType> flightTypes = deserializeToList(o.get("flightTypes"), c, Filter.FlightType.class);
            final List<FlightRule> flightRules = deserializeToList(o.get("flightRules"), c, FlightRule.class);

            return new Filter(
                    UUID.fromString(o.get("uuid").getAsString()),
                    o.get("name").getAsString(),
                    textColor,
                    backgroundColor,
                    type,
                    callsignPredicates,
                    callsignCidOperator,
                    cidPredicates,
                    departurePredicates,
                    departureArrivalOperator,
                    arrivalPredicates,
                    pilotRatings,
                    controllerRatings,
                    flightStatuses,
                    controllerTypes,
                    flightTypes,
                    flightRules,
                    o.get("flightPlanRequired").getAsBoolean()
            );
        }
    }

    private static class StringPredicateAdapter implements Adapter<Filter.StringPredicate> {
        @Override
        public Filter.StringPredicate deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
            final JsonObject o = json.getAsJsonObject();
            return Filter.StringPredicate
                    .tryCreate(o.get("content").getAsString(), o.get("regex").getAsBoolean()).get();
        }

        @Override
        public JsonElement serialize(final Filter.StringPredicate src, final Type typeOfSrc, final JsonSerializationContext context) {
            final JsonObject result = new JsonObject();
            result.addProperty("content", src.getContent());
            result.addProperty("regex", src.isRegex());
            return result;
        }
    }

    private static class ColorAdapter implements Adapter<Color> {
        @Override
        public Color deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
            return Color.valueOf(json.getAsString());
        }

        @Override
        public JsonElement serialize(final Color src, final Type typeOfSrc, final JsonSerializationContext context) {
            return new JsonPrimitive(src.toString());
        }
    }

    private static <T> List<T> deserializeToList(final JsonElement jsonElement, final JsonDeserializationContext context, final Class<T> clazz) {
        return StreamSupport.stream(jsonElement.getAsJsonArray().spliterator(), false)
                            .map(e -> deserializeToClass(e, context, clazz))
                            .collect(Collectors.toList());
    }

    private static <T> T deserializeToClass(final JsonElement jsonElement, final JsonDeserializationContext context, final Class<T> clazz) {
        return context.deserialize(jsonElement, TypeToken.get(clazz).getType());
    }
}