package stylus.history;

import org.junit.Before;
import org.junit.Test;
import stylus.Ted;

import java.util.Set;

import static stylus.checker.Checker.check;

public class HistoryBankTest extends Ted {
    private HistoryBank history;

    @Before
    public void setup() {
        history = new HistoryBank();
    }

    @Test
    public void testCreation() {
        test("testCreation");
        check(history.getIncidents().isEmpty()).eq(true);
        check(history.getEntityMap().isEmpty()).eq(true);
    }

    @Test
    public void testAddGetEntity() {
        test("testAddGetEntity");
        var joe = new Entity("joe", "Joe", "person", true);
        history.addEntity(joe);
        check(history.getEntityMap().size()).eq(1);
        check(history.getEntity("joe").orElse(null)).eq(joe);
        check(history.getEntity("bob").orElse(null)).eq(null);
    }

    @Test
    public void testRemoveEntity() {
        test("testRemoveEntity");
        var joe = new Entity("joe", "Joe", "person", true);
        history.addEntity(joe);
        check(history.getEntityMap().isEmpty()).eq(false);

        check(history.removeEntity("joe").orElse(null)).eq(joe);
        check(history.getEntityMap().isEmpty()).eq(true);

        check(history.removeEntity("bob").orElse(null)).eq(null);
    }

    @Test
    public void testGetTimeFrame_all() {
        test("testGetTimeFrame_all");
        populateHistory();
        var frame = history.getTimeFrame();
        check(frame).eq(new TimeFrame(10, 90));
    }

    @Test
    public void testGetTimeFrame_filtered() {
        test("testGetTimeFrame_filtered");
        populateHistory();
        var frame = history.getTimeFrame(i -> i.concerns("bob"));
        check(frame).eq(new TimeFrame(15, 85));
    }

    @Test
    public void testGetPeriod_full() {
        test("testGetPeriod_full");
        populateHistory();
        var joe = history.getEntity("joe").orElseThrow();
        var joePeriod = history.getPeriod("joe").orElse(null);
        check(joePeriod).eq(new Period(joe, 10, 90, Cap.HARD, Cap.HARD));

        var bob = history.getEntity("bob").orElseThrow();
        var bobPeriod = history.getPeriod("bob").orElse(null);
        check(bobPeriod).eq(new Period(bob, 15, 85, Cap.SOFT, Cap.SOFT));
    }

    @Test
    public void testGetPeriod_mid() {
        test("testGetPeriod_mid");
        populateHistory();
        var frame = new TimeFrame(15, 85);

        var joe = history.getEntity("joe").orElseThrow();
        var joePeriod = history.getPeriod("joe", frame).orElse(null);
        check(joePeriod).eq(new Period(joe, 15, 85, Cap.SOFT, Cap.SOFT));

        var bob = history.getEntity("bob").orElseThrow();
        var bobPeriod = history.getPeriod("bob", frame).orElse(null);
        check(bobPeriod).eq(new Period(bob, 15, 85, Cap.SOFT, Cap.SOFT));
    }

    @Test
    public void testGetPeriod_little() {
        test("testGetPeriod_little");
        populateHistory();
        var frame = new TimeFrame(20, 80);

        var joe = history.getEntity("joe").orElseThrow();
        var joePeriod = history.getPeriod("joe", frame).orElse(null);
        check(joePeriod).eq(new Period(joe, 20, 80, Cap.SOFT, Cap.SOFT));

        var bob = history.getEntity("bob").orElseThrow();
        var bobPeriod = history.getPeriod("bob", frame).orElse(null);
        check(bobPeriod).eq(new Period(bob, 20, 80, Cap.SOFT, Cap.SOFT));
    }

    private void populateHistory() {
        history.addEntity(new Entity("joe", "JoeP", "person", true));
        history.addEntity(new Entity("bob", "BobC", "person", true));
        history.getIncidents()
            .add(new Incident.Start(10, "Joe is born", "joe"));
        history.getIncidents()
            .add(new Incident.Normal(15, "Bob enters", Set.of("bob")));
        history.getIncidents()
            .add(new Incident.Normal(50, "Joe and Bob talk", Set.of("joe", "bob")));
        history.getIncidents()
            .add(new Incident.Normal(85, "Bob leaves", Set.of("bob")));
        history.getIncidents()
            .add(new Incident.End(90, "Joe dies", "joe"));
    }

}
