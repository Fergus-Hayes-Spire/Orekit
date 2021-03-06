/* Contributed in the public domain.
 * Licensed to CS GROUP (CS) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * CS licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.orekit.files.ccsds;

import org.orekit.annotation.DefaultDataContext;
import org.orekit.data.DataContext;
import org.orekit.errors.OrekitException;
import org.orekit.errors.OrekitMessages;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.DateTimeComponents;
import org.orekit.time.TimeScale;
import org.orekit.time.TimeScales;
import org.orekit.utils.Constants;
import org.orekit.utils.IERSConventions;

/**
 * The set of time scales defined in Annex A of the ODM CCSDS standard 502.0-B-2.
 *
 * @author Evan Ward
 */
public enum CcsdsTimeScale {

    /** Greenwich Mean Sidereal Time. */
    GMST {
        @Override
        public TimeScale getTimeScale(final IERSConventions conventions, final TimeScales timeScales) {
            return timeScales.getGMST(conventions, false);
        }
    },
    /** Global Positioning System. */
    GPS {
        @Override
        public TimeScale getTimeScale(final IERSConventions conventions, final TimeScales timeScales) {
            return timeScales.getGPS();
        }
    },
    /** Mission Elapsed Time. */
    MET {
        @Override
        public AbsoluteDate parseDate(final String date,
                                      final IERSConventions conventions,
                                      final AbsoluteDate missionReferenceDate,
                                      final TimeScales timeScales) {
            final DateTimeComponents clock = DateTimeComponents.parseDateTime(date);
            final double offset = clock.getDate().getYear() * Constants.JULIAN_YEAR +
                    clock.getDate().getDayOfYear() * Constants.JULIAN_DAY +
                    clock.getTime().getSecondsInUTCDay();
            return missionReferenceDate.shiftedBy(offset);
        }

        @Override
        public TimeScale getTimeScale(final IERSConventions conventions, final TimeScales timeScales) {
            throw new OrekitException(
                    OrekitMessages.CCSDS_NO_CORRESPONDING_TIME_SCALE,
                    "MET");
        }
    },
    /** Mission Relative Time. */
    MRT {
        @Override
        public AbsoluteDate parseDate(final String date,
                                      final IERSConventions conventions,
                                      final AbsoluteDate missionReferenceDate,
                                      final TimeScales timeScales) {
            final DateTimeComponents clock = DateTimeComponents.parseDateTime(date);
            final double offset = clock.getDate().getYear() * Constants.JULIAN_YEAR +
                    clock.getDate().getDayOfYear() * Constants.JULIAN_DAY +
                    clock.getTime().getSecondsInUTCDay();
            return missionReferenceDate.shiftedBy(offset);
        }

        @Override
        public TimeScale getTimeScale(final IERSConventions conventions, final TimeScales timeScales) {
            throw new OrekitException(
                    OrekitMessages.CCSDS_NO_CORRESPONDING_TIME_SCALE,
                    "MRT");
        }
    },
    /** Spacecraft Clock. Not currently Implemented. */
    SCLK {
        @Override
        public AbsoluteDate parseDate(final String date,
                                      final IERSConventions conventions,
                                      final AbsoluteDate missionReferenceDate,
                                      final TimeScales timeScales) {
            throw new OrekitException(
                    OrekitMessages.CCSDS_TIME_SYSTEM_NOT_IMPLEMENTED,
                    this.name());
        }

        @Override
        public TimeScale getTimeScale(final IERSConventions conventions, final TimeScales timeScales) {
            throw new OrekitException(
                    OrekitMessages.CCSDS_NO_CORRESPONDING_TIME_SCALE,
                    this.name());
        }
    },
    /** International Atomic Time. */
    TAI {
        @Override
        public TimeScale getTimeScale(final IERSConventions conventions, final TimeScales timeScales) {
            return timeScales.getTAI();
        }
    },
    /** Barycentric Coordinate Time. */
    TCB {
        @Override
        public TimeScale getTimeScale(final IERSConventions conventions, final TimeScales timeScales) {
            return timeScales.getTCB();
        }
    },
    /** Barycentric Dynamical Time. */
    TDB {
        @Override
        public TimeScale getTimeScale(final IERSConventions conventions, final TimeScales timeScales) {
            return timeScales.getTDB();
        }
    },
    /** Geocentric Coordinate Time. */
    TCG {
        @Override
        public TimeScale getTimeScale(final IERSConventions conventions, final TimeScales timeScales) {
            return timeScales.getTCG();
        }
    },
    /** Terrestrial Time. */
    TT {
        @Override
        public TimeScale getTimeScale(final IERSConventions conventions, final TimeScales timeScales) {
            return timeScales.getTT();
        }
    },
    /** Universal Time. */
    UT1 {
        @Override
        public TimeScale getTimeScale(final IERSConventions conventions, final TimeScales timeScales) {
            return timeScales.getUT1(conventions, false);
        }
    },
    /** Universal Coordinated Time. */
    UTC {
        @Override
        public TimeScale getTimeScale(final IERSConventions conventions, final TimeScales timeScales) {
            return timeScales.getUTC();
        }
    };

    /**
     * Parse a date in this time scale.
     *
     * <p>This method uses the {@link DataContext#getDefault() default data context}.
     *
     * @param date                 a CCSDS date string.
     * @param conventions          IERS conventions for {@link #UT1} and {@link #GMST}.
     * @param missionReferenceDate epoch for {@link #MET} and {@link #MRT}.
     * @return parsed {@code date}.
     * @see #parseDate(String, IERSConventions, AbsoluteDate, TimeScales)
     */
    @DefaultDataContext
    public AbsoluteDate parseDate(final String date,
                                  final IERSConventions conventions,
                                  final AbsoluteDate missionReferenceDate) {
        return parseDate(date, conventions, missionReferenceDate,
                DataContext.getDefault().getTimeScales());
    }

    /**
     * Parse a date in this time scale.
     *
     * @param date                 a CCSDS date string.
     * @param conventions          IERS conventions for {@link #UT1} and {@link #GMST}.
     * @param missionReferenceDate epoch for {@link #MET} and {@link #MRT}.
     * @param timeScales the set of time scales to use.
     * @return parsed {@code date}.
     * @since 10.1
     */
    public AbsoluteDate parseDate(final String date,
                                  final IERSConventions conventions,
                                  final AbsoluteDate missionReferenceDate,
                                  final TimeScales timeScales) {
        return new AbsoluteDate(date, this.getTimeScale(conventions, timeScales));
    }

    /**
     * Get the corresponding {@link TimeScale}.
     *
     * <p>This method uses the {@link DataContext#getDefault() default data context}.
     *
     * @param conventions IERS Conventions for the {@link #GMST} and {@link #UT1} scales.
     * @return the time scale.
     */
    @DefaultDataContext
    public TimeScale getTimeScale(final IERSConventions conventions) {
        return getTimeScale(conventions, DataContext.getDefault().getTimeScales());
    }

    /**
     * Get the corresponding {@link TimeScale}.
     *
     * @param conventions IERS Conventions for the {@link #GMST} and {@link #UT1} scales.
     * @param timeScales the set of time scales to use.
     * @return the time scale.
     */
    public abstract TimeScale getTimeScale(IERSConventions conventions,
                                           TimeScales timeScales);

    /**
     * Check if {@code timeScale} is one of the values supported by this enum.
     *
     * @param timeScale specifier.
     * @return {@code true} if {@link #valueOf(String)} will not throw an exception with
     * the same string.
     */
    public static boolean contains(final String timeScale) {
        for (final CcsdsTimeScale scale : values()) {
            if (scale.name().equals(timeScale)) {
                return true;
            }
        }
        return false;
    }

}
