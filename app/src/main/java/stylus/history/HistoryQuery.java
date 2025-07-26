package stylus.history;

import stylus.calendars.Calendar;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class HistoryQuery {
    //-------------------------------------------------------------------------
    // Instance Variables

    // The query terms; they will be executed in order by execute().
    private final List<Term> terms = new ArrayList<>();

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Creates an empty query.
     */
    public HistoryQuery() {
    }

    /**
     * Copies another query.
     * @param other The other query.
     */
    @SuppressWarnings("unused")
    public HistoryQuery(HistoryQuery other) {
        this.terms.addAll(other.terms);
    }

    //-------------------------------------------------------------------------
    // Term Definitions

    // The query terms. See the javadoc for the term methods for semantics.
    private interface Term {
        /**
         * Filters incidents according to the given predicate.
         * @param filter The predicate
         */
        record IncidentFilter(Predicate<Incident> filter) implements Term {}

        /**
         * Expands recurring incidents as anniversaries throughout the current
         * range.
         * @param calendar The calendar, which must have months.
         * @param finalYear The final calendar year.  If null, defaults to
         *                  last year in data.
         */
        record ExpandRecurring(Calendar calendar, Integer finalYear) implements Term {}

        /**
         * Includes the given entities from the output.  Resets
         * the included entities list on first inclusion of entities or types.
         * @param entityIds The entity IDs to include.
         */
        record Includes(List<String> entityIds) implements Term {}

        /**
         * Excludes the given entities from the output.
         * @param entityIds The entity IDs to exclude.
         */
        record Excludes(List<String> entityIds) implements Term {}

        /**
         * Includes entities of the given types in the output.  Resets
         * the included entities list on first inclusion of entities or types.
         * @param types The entity types
         */
        record IncludesTypes(List<String> types) implements Term {}

        /**
         * Excludes entities of the given types from the output
         * @param types The entity types
         */
        record ExcludesTypes(List<String> types) implements Term {}

        /**
         * Limits the displayed time frame to that of the listed entities.
         * If the list is null, limits it to the set of included entities.
         * @param entityIds
         */
        record BoundBy(List<String> entityIds) implements Term {}

        /**
         * Groups entities in the output by "primes": first, a group of
         * the prime entities, then a group for each of the prime types,
         * then a group for any remaining types in alphabetical order.
         * Note: entities are included in only one group.  If the lists
         * are empty, the query uses the primes as defined in the history
         * data.
         * @param entities The prime entities.
         * @param types The prime types.
         */
        record GroupByPrimes(List<String> entities, List<String> types)
            implements Term {}

        record GroupBySource() implements Term {}
    }

    //-------------------------------------------------------------------------
    // Terms

    /**
     * Clears all query terms.  The query will return the entire history.
     * @return The query
     */
    public HistoryQuery clear() {
        terms.clear();
        return this;
    }

    /**
     * Filters out all incidents prior to the given moment.
     * @param moment The moment
     * @return The query
     */
    public HistoryQuery noEarlierThan(int moment) {
        terms.add(new Term.IncidentFilter(in -> in.moment() >= moment));
        return this;
    }

    /**
     * Filters out all incidents following the given moment.
     * @param moment The moment
     * @return The query
     */
    public HistoryQuery noLaterThan(int moment) {
        terms.add(new Term.IncidentFilter(in -> in.moment() <= moment));
        return this;
    }

    /**
     * A general filter for incidents.  Only incidents for which the
     * predicate is true will be included.
     * @param predicate The predicate
     * @return The query
     */
    public HistoryQuery filter(Predicate<Incident> predicate) {
        terms.add(new Term.IncidentFilter(predicate));
        return this;
    }

    /**
     * Recurring incidents will be expanded as anniversaries through the final
     * year of the current range of incidents, IF the calendar has months.
     * @param calendar the calendar
     * @return The query
     */
    public HistoryQuery expandAnniversaries(Calendar calendar) {
        terms.add(new Term.ExpandRecurring(calendar, null));
        return this;
    }

    /**
     * Recurring incidents will be expanded as anniversaries through the given
     * final year, IF the calendar has months.
     * @param calendar the calendar
     * @param finalYear The final year
     * @return The query
     */
    public HistoryQuery expandAnniversaries(Calendar calendar, int finalYear) {
        terms.add(new Term.ExpandRecurring(calendar, finalYear));
        return this;
    }

    /**
     * The query includes all entities by default. If no inclusions or
     * exclusions have been done, this limits the set of entities to
     * those given.  Otherwise, these entities are added to the set.
     * @param entityIds The entity IDs
     * @return The query
     */
    public HistoryQuery includes(String... entityIds) {
        return includes(List.of(entityIds));
    }

    /**
     * The query includes all entities by default. If no inclusions or
     * exclusions have been done, this limits the set of entities to
     * those given.  Otherwise, these entities are added to the set.
     * @param entityIds The entity IDs
     * @return The query
     */
    public HistoryQuery includes(List<String> entityIds) {
        terms.add(new Term.Includes(new ArrayList<>(entityIds)));
        return this;
    }

    /**
     * The query includes all entities by default.  If this is found, the
     * named entities are removed from the set of included entities.
     * @param entityIds The entity IDs
     * @return The query
     */
    public HistoryQuery excludes(String... entityIds) {
        return excludes(List.of(entityIds));
    }

    /**
     * The query includes all entities by default.  If this is found, the
     * named entities are removed from the set of included entities.
     * @param entityIds The entity IDs
     * @return The query
     */
    public HistoryQuery excludes(List<String> entityIds) {
        terms.add(new Term.Excludes(new ArrayList<>(entityIds)));
        return this;
    }

    /**
     * The query includes all entities by default. If no inclusions or
     * exclusions have been done, this limits the set of entities to
     * those having the types given. Otherwise, entities of these types are
     * added to the set.
     * @param types The entity types
     * @return The query
     */
    public HistoryQuery includeTypes(String... types) {
        return includeTypes(List.of(types));
    }

    /**
     * The query includes all entities by default. If no inclusions or
     * exclusions have been done, this limits the set of entities to
     * those having the types given. Otherwise, entities of these types are
     * added to the set.
     * @param types The entity types
     * @return The query
     */
    public HistoryQuery includeTypes(List<String> types) {
        terms.add(new Term.IncludesTypes(types));
        return this;
    }

    /**
     * The query includes all entities by default.  If this is found, entities
     * having the named types are removed from the set of included entities.
     * @param types The types to exclude
     * @return The query
     */
    public HistoryQuery excludeTypes(String... types) {
        return excludeTypes(List.of(types));
    }

    /**
     * The query includes all entities by default.  If this is found, entities
     * having the named types are removed from the set of included entities.
     * @param types The types to exclude
     * @return The query
     */
    public HistoryQuery excludeTypes(List<String> types) {
        terms.add(new Term.ExcludesTypes(types));
        return this;
    }

    /**
     * This term limits the time range to the incidents that concern the
     * named entities.  If no entities are listed here, the time range is
     * limited to the incidents that concern all included entities.
     * @param entityIds The entities of interest
     * @return The query
     */
    public HistoryQuery boundByEntities(String... entityIds) {
        return boundByEntities(List.of(entityIds));
    }

    /**
     * This term limits the time range to the incidents that concern the
     * named entities.  If no entities are listed here, the time range is
     * limited to the incidents that concern all included entities.
     * @param entityIds The entities of interest
     * @return The query
     */
    public HistoryQuery boundByEntities(List<String> entityIds) {
        terms.add(new Term.BoundBy(entityIds));
        return this;
    }

    /**
     * Groups entities by prime entities and types.  If entities is non-empty,
     * any listed entities will go in the Prime group, before any other
     * entities.  This will be followed by a group for each prime type (if any)
     * followed by any remaining types in alphabetical order. Entities will
     * appear in only one group; groups that are empty will be discarded.
     * @param entityIds List of entity IDs, or null.
     * @param types List of types, or null.
     * @return The query
     */
    public HistoryQuery groupByPrimes(
        List<String> entityIds,
        List<String> types
    ) {
        terms.add(new Term.GroupByPrimes(
            entityIds != null ? entityIds : List.of(),
            types != null ? types : List.of()));
        return this;
    }

    /**
     * Group entities by the prime entities and types defined in the history
     * data.
     * @return The query.
     */
    @SuppressWarnings("unused")
    public HistoryQuery groupByPrimes() {
        terms.add(new Term.GroupByPrimes(List.of(), List.of()));
        return this;
    }

    /**
     * Entities are listed in the order of definition in the source data.
     * @return The query.
     */
    @SuppressWarnings("unused")
    public HistoryQuery groupBySource() {
        terms.add(new Term.GroupBySource());
        return this;
    }

    //------------------------------------------------------------------------
    // Query

    /**
     * Executes the query by executing the query terms in order for the
     * source history, and returns a history that reflects the executed query.
     * Each query time might:
     *
     * <ul>
     * <li>Modify the set of incidents to include</li>
     * <li>Modify the set of entities to include</li>
     * </ul>
     * @param source The source history
     * @return The resulting history
     */
    public History execute(History source) {
        return new Query(source).execute();
    }

    //-------------------------------------------------------------------------
    // Helper Types

    // Retains transient state while executing a query
    private class Query {
        //---------------------------------------------------------------------
        // Instance Variables

        final History source;
        final Map<String,Period> periods;

        Set<String> entities;
        List<Incident> incidents;
        boolean entitySetModified = false;
        Term groupingTerm;
        LinkedHashMap<String, List<Period>> periodGroups =
            new LinkedHashMap<>();

        //---------------------------------------------------------------------
        // Constructor

        Query(History source) {
            this.source = source;
            this.entities = new HashSet<>(source.getEntityMap().keySet());
            this.incidents = source.getIncidents().stream()
                .sorted(Comparator.comparing(Incident::moment))
                .toList();
            this.periods = source.getPeriods();
            this.groupingTerm = new Term.GroupByPrimes(List.of(), List.of());
        }

        //---------------------------------------------------------------------
        // Execution

        HistoryView execute() {
            // FIRST, do the filtering
            for (var term : terms) {
                switch (term) {
                    case Term.IncidentFilter t -> doFilterIncidents(t);
                    case Term.ExpandRecurring t -> doExpandRecurring(t);
                    case Term.Includes t -> doIncludeEntities(t);
                    case Term.IncludesTypes t -> doIncludeTypes(t);
                    case Term.Excludes t -> doExcludeEntities(t);
                    case Term.ExcludesTypes t -> doExcludeTypes(t);
                    case Term.BoundBy t -> doBoundByEntities(t);
                    case Term.GroupByPrimes t -> groupingTerm = t;
                    case Term.GroupBySource t -> groupingTerm = t;
                    default ->
                        throw new IllegalStateException(
                            "Unknown term:" + term);
                }
            }

            // NEXT, apply the entity filter to the incidents table.
            incidents = incidents.stream()
                .filter(this::includesQueriedEntity)
                .toList();

            // NEXT, compute the period groups
            switch (groupingTerm) {
                case Term.GroupByPrimes t -> doGroupByPrimes(t);
                case Term.GroupBySource ignored -> doGroupBySource();
                default -> throw new IllegalStateException(
                    "Unsupported 'groupBy' term: " + groupingTerm);
            }

            // NEXT, compute the entity map
            Map<String,Entity> map = new LinkedHashMap<>();
            for (var id : entities) {
                var period = periods.get(id);
                if (period != null) {
                    map.put(id, period.entity());
                }
            }

            var result = new HistoryView(
                source.getTypeMap(),
                map,
                incidents,
                periodGroups);
            result.setMomentFormatter(source.getMomentFormatter());

            return result;
        }

        void doFilterIncidents(Term.IncidentFilter t) {
            incidents = incidents.stream().filter(t.filter).toList();
        }

        void doExpandRecurring(Term.ExpandRecurring t) {
            var cal = t.calendar();
            if (!cal.hasMonths()) {
                return;
            }

            // FIRST, get the recurring incidents, and also the final year
            var recurring = incidents.stream()
                .filter(Incident::isRecurring)
                .toList();

            if (recurring.isEmpty()) {
                return;
            }

            int finalYear;

            if (t.finalYear() != null) {
                finalYear = t.finalYear();
            } else {
                // There are incidents; there will be a final year.
                finalYear = incidents.stream()
                    .mapToInt(i -> cal.day2date(i.moment()).year())
                    .max()
                    .orElseThrow();
            }

            // NEXT, add the anniversary for each recurring incident.
            var result = new ArrayList<>(incidents);
            for (var incident : recurring) {
                var date = cal.day2date(incident.moment());

                for (var y = date.year() + 1; y <= finalYear; y++) {
                    var newDate = cal.date(y, date.monthOfYear(), date.dayOfMonth());
                    var moment = cal.date2day(newDate);
                    var age = y - date.year();
                    result.add(new Incident.Anniversary(moment, age, incident));
                }
            }

            incidents = result;
        }

        void doIncludeEntities(Term.Includes t) {
            if (!entitySetModified) {
                entities.clear();
            }
            entitySetModified = true;
            t.entityIds.forEach(id -> {
                // NOTE: the client might reasonably reference an entity that
                // was filtered out by a previous query.  Such a reference is
                // therefore not an error, but we cannot pass it along
                // anyway.
                if (source.getEntityMap().containsKey(id)) {
                    entities.add(id);
                }
            });
        }

        void doExcludeEntities(Term.Excludes t) {
            entitySetModified = true;
            t.entityIds().forEach(entities::remove);
        }

        void doIncludeTypes(Term.IncludesTypes t) {
            if (!entitySetModified) {
                entities.clear();
            }
            entitySetModified = true;
            var toInclude = periods.values().stream()
                .map(Period::entity)
                .filter(e -> t.types().contains(e.type()))
                .map(Entity::id)
                .toList();
            entities.addAll(toInclude);
        }

        void doExcludeTypes(Term.ExcludesTypes t) {
            entitySetModified = true;
            var toRetain = periods.values().stream()
                .map(Period::entity)
                .filter(e -> !t.types().contains(e.type()))
                .map(Entity::name)
                .toList();
            entities = new HashSet<>(toRetain);
        }

        boolean includesQueriedEntity(Incident incident) {
            var concerns = new HashSet<>(incident.entityIds());
            concerns.retainAll(entities);
            return !concerns.isEmpty();
        }

        void doBoundByEntities(Term.BoundBy t) {
            var list = new ArrayList<Period>();

            var ids = !t.entityIds.isEmpty() ? t.entityIds : entities;

            for (var id : ids) {
                if (periods.containsKey(id)) {
                    list.add(periods.get(id));
                }
            }

            var start = list.stream()
                .mapToInt(Period::start)
                .min().orElse(Integer.MIN_VALUE);
            var end = list.stream()
                .mapToInt(Period::end)
                .min().orElse(Integer.MAX_VALUE);
            incidents = incidents.stream()
                .filter(in -> in.moment() >= start && in.moment() <= end)
                .toList();
        }

        // Get the source's period groups, but filter out the excluded
        // entities.
        void doGroupBySource() {
            for (var grp : source.getPeriodGroups().entrySet()) {
                var list = new ArrayList<Period>();

                for (var period : grp.getValue()) {
                    if (entities.contains(period.entity().id())) {
                        list.add(period);
                    }
                }

                if (!list.isEmpty()) {
                    periodGroups.put(grp.getKey(), list);
                }
            }
        }

        // Create period groups by primes entities and types.
        void doGroupByPrimes(Term.GroupByPrimes t) {
            // FIRST, get the prime entities and the prime types.
            var primeEntities = !t.entities().isEmpty()
                ? t.entities()
                : getPrimeEntities(source);
            var primeTypes = !t.types().isEmpty()
                ? t.types()
                : getPrimeTypes(source);

            // NEXT, get the entities and types remaining to be grouped.
            var remainingEntities = new HashSet<>(entities);
            var remainingTypes = entities.stream()
                .map(id -> source.getEntityMap().get(id).type())
                .collect(Collectors.toSet());

            // NEXT, get the prime group
            var primes = new ArrayList<Period>();
            for (var id : primeEntities) {
                var period = periods.get(id);
                if (remainingEntities.contains(id) && period != null) {
                    primes.add(period);
                    remainingEntities.remove(id);
                }
            }

            if (!primes.isEmpty()) {
                periodGroups.put("prime", primes);
            }

            // NEXT, add each prime type
            for (var type : primeTypes) {
                var group = periods.values().stream()
                    .filter(p -> p.entity().type().equals(type))
                    .filter(p -> remainingEntities.contains(p.entity().id()))
                    .sorted(Comparator.comparing(Period::start))
                    .toList();

                if (!group.isEmpty()) {
                    periodGroups.put(type, group);
                    remainingTypes.remove(type);
                }
            }

            // NEXT, add each other type
            var others = periods.values().stream()
                .map(Period::entity)
                .map(Entity::type)
                .filter(remainingTypes::contains)
                .sorted()
                .toList();

            for (var type : others) {
                var group = periods.values().stream()
                    .filter(p -> p.entity().type().equals(type))
                    .filter(p -> remainingEntities.contains(p.entity().id()))
                    .sorted(Comparator.comparing(Period::start))
                    .toList();

                if (!group.isEmpty()) {
                    periodGroups.put(type, group);
                }
            }
        }
    }

    private List<String> getPrimeEntities(History source) {
        return source.getEntityMap().values().stream()
            .filter(Entity::prime)
            .map(Entity::id)
            .toList();
    }

    private List<String> getPrimeTypes(History source) {
        return source.getTypeMap().values().stream()
            .filter(EntityType::prime)
            .map(EntityType::id)
            .toList();
    }
}
