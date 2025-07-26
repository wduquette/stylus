package stylus.history;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class AbstractHistory implements History {
    //-------------------------------------------------------------------------
    // Instance Variables

    // The calendar information
    private Function<Integer, String> momentFormatter;

    // The entity types in this history.
    private final SequencedMap<String,EntityType> typeMap = new LinkedHashMap<>();

    // The entities in this history.  Use a LinkedHashMap to preserve
    private final SequencedMap<String, Entity> entityMap =
        new LinkedHashMap<>();

    // The incidents
    private final List<Incident> incidents = new ArrayList<>();

    //-------------------------------------------------------------------------
    // Constructors

    public AbstractHistory() {
        // Nothing to do
    }

    //-------------------------------------------------------------------------
    // Configuration

    public final Function<Integer, String> getMomentFormatter() {
        return momentFormatter;
    }

    public void setMomentFormatter(Function<Integer, String> formatter) {
        this.momentFormatter = formatter;
    }

    //-------------------------------------------------------------------------
    // Protected Members, for use by subclasses.
    //
    // Subclasses are free to edit and view the history as they like.

    protected final SequencedMap<String, EntityType> typeMap() {
        return typeMap;
    }

    protected final void setTypeMap(Map<String, EntityType> map) {
        typeMap.clear();
        typeMap.putAll(map);
    }

    protected final SequencedMap<String, Entity> entityMap() {
        return entityMap;
    }

    protected final void setEntityMap(Map<String, Entity> map) {
        entityMap.clear();
        entityMap.putAll(map);
    }

    protected final List<Incident> incidents() {
        return incidents;
    }

    protected final void setIncidents(List<Incident> list) {
        incidents.clear();
        incidents.addAll(list);
    }

    //-------------------------------------------------------------------------
    // Standard Queries.
    //
    // These queries are available to all subclasses.

    /**
     * Returns the time frame given all incidents.
     * @return the time frame.
     */
    public TimeFrame getTimeFrame() {
        return getTimeFrame(incident -> true);
    }

    /**
     * Returns the TimeFrame for all incidents that pass the filter.
     * @param filter The filter predicate
     * @return the TimeFrame.
     */
    public TimeFrame getTimeFrame(Predicate<Incident> filter) {
        var filtered = incidents().stream()
            .filter(filter)
            .toList();

        var start = filtered.stream()
            .mapToInt(Incident::moment)
            .min().orElse(0);
        var end = filtered.stream()
            .mapToInt(Incident::moment)
            .max().orElse(0);

        return new TimeFrame(start, end);
    }

    public Optional<Period> getPeriod(String entityId) {
        return getPeriod(entityId, getTimeFrame());
    }

    public Optional<Period> getPeriod(String entityId, TimeFrame frame) {
        // FIRST, get the entity
        var entity = entityMap().get(entityId);
        if (entity == null) {
            throw new IllegalArgumentException("No such entity: \"" + entityId + "\"");
        }

        // NEXT, get the sorted earliest and last incidents concerning this
        // entity
        var all = getIncidents(entityId);

        if (all.isEmpty()) {
            return Optional.empty();
        }

        var first = all.getFirst();
        var last = all.getLast();

        int startMoment;
        int endMoment;
        Cap startCap;
        Cap endCap;

        if (first.moment() > frame.end()) {
            return Optional.empty();
        } else if (first.moment() < frame.start()) {
            startMoment = frame.start();
            startCap = Cap.SOFT;
        } else {
            startMoment = first.moment();
            startCap = first.cap();
        }

        if (last.moment() < frame.start()) {
            return Optional.empty();
        } else if (last.moment() > frame.end()) {
            endMoment = frame.end();
            endCap = Cap.SOFT;
        } else {
            endMoment = last.moment();
            endCap = last.cap();
        }

        return Optional.of(new Period(
            entity,
            startMoment,
            endMoment,
            startCap,
            endCap
        ));
    }

    public Map<String,Period> getPeriods(TimeFrame frame) {
        var map = new HashMap<String,Period>();

        for (var id : entityMap().keySet()) {
            getPeriod(id, frame).ifPresent(period -> map.put(id, period));
        }

        return map;
    }

    public Map<String,Period> getPeriods() {
        return getPeriods(getTimeFrame());
    }

    // Computes a list of entities by group name.  For now, groups
    // will be defined by type, in alphabetical order; within each
    // group, entities will be sorted by period, earliest start time
    // first.
    @Override
    public LinkedHashMap<String,List<Period>> getPeriodGroups() {
        // FIRST, get the periods
        var periods = getPeriods();

        // NEXT, initialize the result map to contain a list for each
        // entity type, in alphabetical order
        var map = new LinkedHashMap<String,List<Period>>();
        periods.values().stream()
            .map(p -> p.entity().type())
            .sorted()
            .forEach(type -> map.put(type, new ArrayList<>()));

        // NEXT, add the periods for each type.
        for (var id : periods.keySet()) {
            var period = periods.get(id);
            var type = period.entity().type();
            map.get(type).add(period);
        }

        // NEXT, sort each group list
        for (var group : map.keySet()) {
            var sorted = map.get(group).stream()
                .sorted(Comparator.comparing(Period::start))
                .toList();
            map.put(group, sorted);
        }

        return map;
    }

    public List<Incident> getIncidents(String entityId) {
        return incidents().stream()
            .filter(i -> i.concerns(entityId))
            .sorted(Comparator.comparing(Incident::moment))
            .toList();
    }

    public String toTimelineChart() {
        return new TextTimelineChart(this).toString();
    }
}
