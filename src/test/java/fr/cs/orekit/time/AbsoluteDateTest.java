package fr.cs.orekit.time;

import java.text.ParseException;
import java.util.Date;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import fr.cs.orekit.errors.OrekitException;
import fr.cs.orekit.iers.IERSDirectoryCrawler;

public class AbsoluteDateTest
extends TestCase {

    public AbsoluteDateTest(String name) {
        super(name);
    }

    public void testStandardEpoch() {
        TimeScale tai = TAIScale.getInstance();
        TimeScale tt  = TTScale.getInstance();
        assertEquals(-210866760000000l, AbsoluteDate.JULIAN_EPOCH.toDate(tt).getTime());
        assertEquals(-3506716800000l,   AbsoluteDate.MODIFIED_JULIAN_EPOCH.toDate(tt).getTime());
        assertEquals(-631152000000l,    AbsoluteDate.FIFTIES_EPOCH.toDate(tt).getTime());
        assertEquals(315964819000l,     AbsoluteDate.GPS_EPOCH.toDate(tai).getTime());
        assertEquals(946728000000l,     AbsoluteDate.J2000_EPOCH.toDate(tt).getTime());
    }

    public void testOutput() {
        TimeScale tt = TTScale.getInstance();
        assertEquals("1950-01-01T01:01:01.000",
                     new AbsoluteDate(AbsoluteDate.FIFTIES_EPOCH, 3661.0).toString(tt));
        assertEquals("2000-01-01T13:01:01.000",
                     new AbsoluteDate(AbsoluteDate.J2000_EPOCH, 3661.0).toString(tt));
    }

    public void testJ2000() {
        assertEquals("2000-01-01T12:00:00.000",
                     AbsoluteDate.J2000_EPOCH.toString(TTScale.getInstance()));
        assertEquals("2000-01-01T11:59:27.816",
                     AbsoluteDate.J2000_EPOCH.toString(TAIScale.getInstance()));
        assertEquals("2000-01-01T11:58:55.816",
                     AbsoluteDate.J2000_EPOCH.toString(utc));
    }

    public void testFraction() {
        AbsoluteDate d =
            new AbsoluteDate(new ChunkedDate(2000, 01, 01), new ChunkedTime(11, 59, 27.816),
                             TAIScale.getInstance());
        assertEquals(0, d.minus(AbsoluteDate.J2000_EPOCH), 1.0e-10);
    }

    public void testScalesOffset() {
        AbsoluteDate date = new AbsoluteDate(new ChunkedDate(2006, 02, 24),
                                             new ChunkedTime(15, 38, 00),
                                             utc);
        assertEquals(33,
                     date.timeScalesOffset(TAIScale.getInstance(), utc),
                     1.0e-10);
    }

    public void testUTC() {
        AbsoluteDate date = new AbsoluteDate(new ChunkedDate(2002, 01, 01),
                                             new ChunkedTime(00, 00, 01),
                                             utc);
        assertEquals("2002-01-01T00:00:01.000", date.toString());
    }

    public void test1970() {
        AbsoluteDate date = new AbsoluteDate(new Date(0l), utc);
        assertEquals("1970-01-01T00:00:00.000", date.toString());
    }

    public void testUtcGpsOffset() {
        AbsoluteDate date1   = new AbsoluteDate(new ChunkedDate(2005, 8, 9),
                                                new ChunkedTime(16, 31, 17),
                                                utc);
        AbsoluteDate date2   = new AbsoluteDate(new ChunkedDate(2006, 8, 9),
                                                new ChunkedTime(16, 31, 17),
                                                utc);
        AbsoluteDate dateRef = new AbsoluteDate(new ChunkedDate(1980, 1, 6),
                                                ChunkedTime.H00,
                                                utc);

        // 13 seconds offset between GPS time and UTC in 2005
        long noLeapGap = ((9347 * 24 + 16) * 60 + 31) * 60 + 17;
        long realGap   = (long) date1.minus(dateRef);
        assertEquals(13, realGap - noLeapGap);

        // 14 seconds offset between GPS time and UTC in 2006
        noLeapGap = ((9712 * 24 + 16) * 60 + 31) * 60 + 17;
        realGap   = (long) date2.minus(dateRef);
        assertEquals(14, realGap - noLeapGap);

    }

    public void testGpsDate() {
        AbsoluteDate date = AbsoluteDate.createGPSDate(1387, 318677000.0);
        AbsoluteDate ref  = new AbsoluteDate(new ChunkedDate(2006, 8, 9),
                                             new ChunkedTime(16, 31, 03),
                                             utc);
        assertEquals(0, date.minus(ref), 1.0e-12);
    }

    public void testEquals() throws ParseException {
        AbsoluteDate d1 =
            new AbsoluteDate(new ChunkedDate(2006, 2, 25),
                             new ChunkedTime(17, 10, 34),
                             utc);
        AbsoluteDate d2 =
            new AbsoluteDate(new AbsoluteDate(new ChunkedDate(2006, 2, 25),
                                              new ChunkedTime(17, 10, 0),
                                              utc),
                             34);
        assertTrue(d1.equals(d2));
        assertFalse(d1.equals(this));
    }

    public void testHashcode() throws ParseException {
        AbsoluteDate d1 =
            new AbsoluteDate(new ChunkedDate(2006, 2, 25),
                             new ChunkedTime(17, 10, 34),
                             utc);
        AbsoluteDate d2 =
            new AbsoluteDate(new AbsoluteDate(new ChunkedDate(2006, 2, 25),
                                              new ChunkedTime(17, 10, 0),
                                              utc),
                             34);
        assertEquals(d1.hashCode(), d2.hashCode());
        assertTrue(d1.hashCode() != new AbsoluteDate(d1, 1.0e-3).hashCode());
    }

    public void setUp() throws OrekitException {
        System.setProperty(IERSDirectoryCrawler.IERS_ROOT_DIRECTORY, "regular-data");
        utc = UTCScale.getInstance();
    }

    public static Test suite() {
        return new TestSuite(AbsoluteDateTest.class);
    }

    private TimeScale utc;

}
