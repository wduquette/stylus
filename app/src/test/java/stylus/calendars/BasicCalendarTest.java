package stylus.calendars;

import org.junit.Test;

import static stylus.checker.Checker.check;

public class BasicCalendarTest {
    private static final Calendar AF = Armorican.AF;
    private static final Calendar ME = Armorican.ME;

    // A calendar with 10 day "years"
    private static final BasicCalendar AE = new BasicCalendar.Builder()
        .epochOffset(0)
        .month(StandardMonth.JANUARY, 31)
        .month(StandardMonth.FEBRUARY, y -> isLeapYear(y) ? 29 : 28)
        .month(StandardMonth.MARCH, 31)
        .month(StandardMonth.APRIL, 30)
        .month(StandardMonth.MAY, 31)
        .month(StandardMonth.JUNE, 30)
        .month(StandardMonth.JULY, 31)
        .month(StandardMonth.AUGUST, 31)
        .month(StandardMonth.SEPTEMBER, 30)
        .month(StandardMonth.OCTOBER, 31)
        .month(StandardMonth.NOVEMBER, 30)
        .month(StandardMonth.DECEMBER, 31)
        .build();

    private static boolean isLeapYear(int year) {
        if (year % 400 == 0) {
            return true;
        } else if (year % 100 == 0) {
            return false;
        } else {
            return year % 4 == 0;
        }
    }

    @Test
    public void testDaysInYear() {
        // Year 0 would be a leap year if we had a year 0; but year -1 is the
        // equivalent year.
        check(AE.daysInYear(-5)).eq(366);
        check(AE.daysInYear(-4)).eq(365);
        check(AE.daysInYear(-3)).eq(365);
        check(AE.daysInYear(-2)).eq(365);
        check(AE.daysInYear(-1)).eq(366);

        check(AE.daysInYear(1)).eq(365);
        check(AE.daysInYear(2)).eq(365);
        check(AE.daysInYear(3)).eq(365);
        check(AE.daysInYear(4)).eq(366);
        check(AE.daysInYear(100)).eq(365);  // 100 year rule
        check(AE.daysInYear(400)).eq(366);  // 400 year rule
    }

    @Test
    public void testDaysInMonth() {
        check(AE.daysInMonth(  1, 1)).eq(31);

        check(AE.daysInMonth(  1, 2)).eq(28);
        check(AE.daysInMonth(  4, 2)).eq(29);
        check(AE.daysInMonth(100, 2)).eq(28);
        check(AE.daysInMonth(400, 2)).eq(29);
    }

    @Test
    public void testValidate() {
        // FIRST, check assignment.
        check(AE.date(1, 2, 3).year()).eq(1);
        check(AE.date(1, 2, 3).monthOfYear()).eq(2);
        check(AE.date(1, 2, 3).dayOfMonth()).eq(3);

        // NEXT, the following dates should be valid.
        AE.date(-1,2,29);
        AE.date(4,2,29);
        AE.date(400,2,29);
        AE.date(2024,2,29);
    }

    @Test
    public void testDay2Date() {
        // Positive Days
        check(AE.day2date(   0)).eq(AE.date(1,  1,  1));
        check(AE.day2date(  30)).eq(AE.date(1,  1, 31));
        check(AE.day2date(  31)).eq(AE.date(1,  2,  1));
        check(AE.day2date(  58)).eq(AE.date(1,  2, 28));
        check(AE.day2date(  59)).eq(AE.date(1,  3,  1));
        check(AE.day2date( 364)).eq(AE.date(1, 12, 31));
        check(AE.day2date( 365)).eq(AE.date(2,  1,  1));
        check(AE.day2date( 730)).eq(AE.date(3,  1,  1));
        check(AE.day2date(1095)).eq(AE.date(4,  1,  1));
        check(AE.day2date(1126)).eq(AE.date(4,  2,  1));
        check(AE.day2date(1154)).eq(AE.date(4,  2, 29));
        check(AE.day2date(1155)).eq(AE.date(4,  3,  1));

        // Negative days
        check(AE.day2date(-366)).eq(AE.date(-1,  1,   1));
        check(AE.day2date(-336)).eq(AE.date(-1,  1,  31));
        check(AE.day2date(-335)).eq(AE.date(-1,  2,   1));
        check(AE.day2date(-307)).eq(AE.date(-1,  2,  29));
        check(AE.day2date(-306)).eq(AE.date(-1,  3,   1));
        check(AE.day2date(  -1)).eq(AE.date(-1,  12, 31));
    }

    @Test
    public void testDate2Day() {
        check(AE.date2day(AE.date(-1,  1,  31))).eq(-336);

        // Positive Days
        check(AE.date2day(AE.date(1,  1,  1))).eq(0);
        check(AE.date2day(AE.date(1,  1, 31))).eq(30);
        check(AE.date2day(AE.date(1,  2,  1))).eq(31);
        check(AE.date2day(AE.date(1,  2, 28))).eq(58);
        check(AE.date2day(AE.date(1,  3,  1))).eq(59);
        check(AE.date2day(AE.date(1, 12, 31))).eq(364);
        check(AE.date2day(AE.date(2,  1,  1))).eq(365);
        check(AE.date2day(AE.date(3,  1,  1))).eq(730);
        check(AE.date2day(AE.date(4,  1,  1))).eq(1095);
        check(AE.date2day(AE.date(4,  2,  1))).eq(1126);
        check(AE.date2day(AE.date(4,  2, 29))).eq(1154);
        check(AE.date2day(AE.date(4,  3,  1))).eq(1155);

        // Negative days
        check(AE.date2day(AE.date(-1,  1,   1))).eq(-366);
        check(AE.date2day(AE.date(-1,  1,  31))).eq(-336);
        check(AE.date2day(AE.date(-1,  2,   1))).eq(-335);
        check(AE.date2day(AE.date(-1,  2,  29))).eq(-307);
        check(AE.date2day(AE.date(-1,  3,   1))).eq(-306);
        check(AE.date2day(AE.date(-1,  12, 31))).eq(-1);
    }

    @Test public void testArmoricanSpotChecks() {
        var meDate = ME.yearDay(1011,1);
        var meDay = ME.yearDay2day(meDate);
        var afDate = AF.yearDay(33,1);
        var afDay = AF.yearDay2day(afDate);

        check(meDay).eq(afDay);
        check(ME.day2yearDay(meDay)).eq(meDate);
        check(AF.day2yearDay(afDay)).eq(afDate);
    }

    @Test
    public void testFormatParse() {
        var date = ME.date(1018, 2, 4);

        check(ME.format(date)).eq("ME-1018-2-4");
        check(ME.parse("ME-1018-2-4")).eq(ME.date2day(date));
    }
}
