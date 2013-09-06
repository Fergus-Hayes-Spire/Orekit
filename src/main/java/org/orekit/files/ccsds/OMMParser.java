/* Copyright 2002-2013 CS Systèmes d'Information
 * Licensed to CS Systèmes d'Information (CS) under one or more
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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.math3.exception.util.DummyLocalizable;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.util.FastMath;
import org.orekit.errors.OrekitException;
import org.orekit.errors.OrekitMessages;
import org.orekit.files.general.OrbitFile;
import org.orekit.files.general.OrbitFileParser;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.DateTimeComponents;
import org.orekit.time.TimeScalesFactory;
import org.orekit.utils.Constants;
import org.orekit.utils.IERSConventions;

/** A parser for the CCSDS OMM (Orbiter Mean-Elements Message).
 * @author sports
 * @since 6.1
 */
public class OMMParser
    implements OrbitFileParser {

    /** Initial Date for MET or MRT time systems. Has to be configured by the user prior parsing. */
    private AbsoluteDate initialDate;

    /** Gravitational coefficient. Has to be configured by the user prior parsing. */
    private double mu;

    /** IERS Conventions to use. */
    private IERSConventions conventions;

    /** Launch Year. Used for the OMMFile generateTLE method.
     * Has to be configured by the user prior parsing. */
    private int launchYear;

    /** Launch number. Used for the OMMFile generateTLE method.
     * Has to be configured by the user prior parsing. */
    private int launchNumber;

    /** Piece of launch (from "A" to "ZZZ"). Used for the OMMFile generateTLE method.
     * Has to be configured by the user prior parsing. */
    private String launchPiece;

    /** Simple constructor.
     * <p>
     * The initial date for Mission Elapsed Time and Mission Relative Time time systems is not set here.
     * If such time systems are used, it must be initialized before parsing by calling {@link
     * #setInitialDate(AbsoluteDate)}.
     * </p>
     * <p>
     * The gravitational coefficient is not set here. If it is needed in order
     * to parse Cartesian orbits where the value is not set in the CCSDS file, it must
     * be initialized before parsing by calling {@link #setMu(double)}.
     * </p>
     * <p>
     * The IERS conventions to use is not set here. If it is needed in order to
     * parse some reference frames or UT1 time scale, it must be initialized before
     * parsing by calling {@link #setConventions(IERSConventions)}.
     * </p>
     */
    public OMMParser() {
        initialDate = AbsoluteDate.FUTURE_INFINITY;
        mu = Double.NaN;
        conventions = null;
        launchYear = 0;
        launchNumber = 0;
        launchPiece = null;
    }

    /** Set initial date.
     * @param initialDate date to be set
     */
    public void setInitialDate(final AbsoluteDate initialDate) {
        this.initialDate = initialDate;
    }

    /** Set gravitational coefficient.
     * @param mu gravitational coefficient to be set
     */
    public void setMu(final double mu) {
        this.mu = mu;
    }

    /** Set IERS conventions.
     * @param conventions IERS conventions to be set
     */
    public void setConventions(final IERSConventions conventions) {
        this.conventions = conventions;
    }

    /** Set launch year.
     * @param launchYear year to be set
     */
    public void setLaunchYear(final int launchYear) {
        this.launchYear = launchYear;
    }

    /** Set launch number.
     * @param launchNumber number to be set
     */
    public void setLaunchNumber(final int launchNumber) {
        this.launchNumber = launchNumber;
    }

    /** Set launch piece.
     * @param launchPiece name of the piece to be set
     */
    public void setLaunchPiece(final String launchPiece) {
        this.launchPiece = launchPiece;
    }

    /** {@inheritDoc} */
    public OMMFile parse(final String fileName)
        throws OrekitException {

        InputStream stream = null;

        try {
            stream = new FileInputStream(fileName);
            return parse(stream);
        } catch (FileNotFoundException e) {
            throw new OrekitException(OrekitMessages.UNABLE_TO_FIND_FILE,
                                      fileName);
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (IOException e) {
                // ignore
            }
        }
    }

    /** {@inheritDoc} */
    public OMMFile parse(final InputStream stream)
        throws OrekitException {

        try {
            return parseInternal(stream);
        } catch (IOException e) {
            throw new OrekitException(e, new DummyLocalizable(e.getMessage()));
        }
    }

    /**
     * Parse the OMM file from the given {@link InputStream} and return a
     * {@link OMMFile} object.
     * @param stream the stream to be parsed
     * @return the {@link OMMFile}
     * @throws OrekitException if the file could not be parsed successfully
     * @throws IOException if an error occurs while reading from the stream
     */
    private OMMFile parseInternal(final InputStream stream)
        throws OrekitException, IOException {

        final BufferedReader reader = new BufferedReader(
                                                         new InputStreamReader(
                                                                               stream,
                                                                               "UTF-8"));
        // initialize internal data structures
        final ParseInfo pi = new ParseInfo();
        final OMMFile file = pi.file;
        final String BLANKS = " +";
        // set the additional data that has been configured prior the parsing by the user.

        if (!initialDate.equals(AbsoluteDate.FUTURE_INFINITY)) {
            pi.file.setInitialDate(initialDate);
        }

        if (!Double.isNaN(mu)) {
            pi.file.setMuSet(mu);
        }

        if (launchYear != 0) {
            pi.file.setLaunchYear(launchYear);
        }

        if (launchNumber != 0) {
            pi.file.setLaunchNumber(launchNumber);
        }

        if (launchPiece != null) {
            pi.file.setLaunchPiece(launchPiece);
        }

        for (String line = reader.readLine(); line != null; line = reader
            .readLine()) {
            if (line.trim().length() == 0)
                continue;
            final Scanner sc = new Scanner(line);
            pi.keywordTmp = sc.next();
            if (pi.keywordTmp.matches("USER_DEFINED_.*")) {
                pi.userDefinedKeyword = pi.keywordTmp;
                pi.keyword = Keyword.USER_DEFINED_X;
            } else
                pi.keyword = Keyword.valueOf(pi.keywordTmp);

            if (pi.keyword != Keyword.COMMENT) {
                sc.next(); // skip "="
            }
            pi.keyValue = sc.next();

            switch (pi.keyword) {
            case CCSDS_OMM_VERS: {
                file.setFormatVersion(pi.keyValue);
            }
                break;

            case COMMENT: {
                pi.commentTmp.add(line.split(BLANKS, 2)[1]);
            }
                break;

            case CREATION_DATE: {
                checkSetComment(pi, file, ODMBlock.HEADER);
                file.setCreationDate(new AbsoluteDate(pi.keyValue,
                                                            TimeScalesFactory
                                                                .getUTC()));
            }
                break;

            case ORIGINATOR: {
                file.setOriginator(pi.keyValue);
            }
                break;

            case OBJECT_NAME: {
                checkSetComment(pi, file, ODMBlock.METADATA);
                file.setObjectName(line.split(BLANKS, 3)[2]);
            }
                break;

            case OBJECT_ID: {
                file.setObjectID(pi.keyValue);
            }
                break;

            case CENTER_NAME: {
                file.setCenterName(pi.keyValue);
                if (pi.keyValue.matches("SOLAR SYSTEM BARYCENTER") ||
                    pi.keyValue.matches("SSB")) {
                    pi.keyValue = "SOLAR_SYSTEM_BARYCENTER";
                }
                if (pi.keyValue.matches("EARTH MOON BARYCENTER") ||
                    pi.keyValue.matches("EARTH-MOON BARYCENTER") ||
                    pi.keyValue.matches("EARTH BARYCENTER") ||
                    pi.keyValue.matches("EMB")) {
                    pi.keyValue = "EARTH_MOON";
                }
                for (final CenterName c : CenterName.values()) {
                    if (c.name().equals(pi.keyValue)) {
                        file.setHasCreatableBody(true);
                        file.setCenterBody(c.getCelestialBody());
                        file.setMuCreated(c.getCelestialBody().getGM());
                    }
                }
            }
                break;

            case REF_FRAME: {
                file.setRefFrame(CCSDSFrame.valueOf(pi.keyValue.replaceAll("-", "")).getFrame(conventions));
            }
                break;

            case REF_FRAME_EPOCH: {
                pi.hasRefFrameEpoch = true;
                pi.epochTmp = pi.keyValue;
            }
                break;
            case TIME_SYSTEM: {
                file.setTimeSystem(OrbitFile.TimeSystem.valueOf(pi.keyValue));
                switch (file.getTimeSystem()) {
                case GMST:
                    file.setTimeScale(TimeScalesFactory.getGMST());
                    break;
                case GPS:
                    file.setTimeScale(TimeScalesFactory.getGPS());
                    break;
                case TAI:
                    file.setTimeScale(TimeScalesFactory.getTAI());
                    break;
                case TCB:
                    file.setTimeScale(TimeScalesFactory.getTCB());
                    break;
                case TDB:
                    file.setTimeScale(TimeScalesFactory.getTDB());
                    break;

                case TCG:
                    file.setTimeScale(TimeScalesFactory.getTCG());
                    break;

                case TT:
                    file.setTimeScale(TimeScalesFactory.getTT());
                    break;

                case UT1:
                    file.setTimeScale(TimeScalesFactory.getUT1(conventions));
                    break;
                case UTC:
                    file.setTimeScale(TimeScalesFactory.getUTC());
                    break;

                default:
                }
                if (pi.hasRefFrameEpoch) {
                    if (file.getTimeSystem().equals(OrbitFile.TimeSystem.MET) ||
                        file.getTimeSystem().equals(OrbitFile.TimeSystem.MRT)) {
                        final DateTimeComponents clock = DateTimeComponents.parseDateTime(pi.epochTmp);
                        final double offset = clock.getDate().getYear() * Constants.JULIAN_YEAR +
                                              clock.getDate().getDayOfYear() * Constants.JULIAN_DAY +
                                              clock.getTime().getSecondsInDay();
                        file.setFrameEpoch(offset);
                    }
                    else {
                        file.setFrameEpoch(new AbsoluteDate(pi.epochTmp, file
                        .getTimeScale()));
                    }
                }
            }
                break;

            case MEAN_ELEMENT_THEORY: {
                file.setMeanElementTheory(pi.keyValue);
            }
                break;

            case EPOCH: {
                checkSetComment(pi, file, ODMBlock.DATA_MEAN_KEPLERIAN_ELEMENTS);
                if (file.getTimeSystem().equals(OrbitFile.TimeSystem.MET) ||
                    file.getTimeSystem().equals(OrbitFile.TimeSystem.MRT)) {
                    final DateTimeComponents clock = DateTimeComponents.parseDateTime(pi.keyValue);
                    final double offset = clock.getDate().getYear() * Constants.JULIAN_YEAR +
                                          clock.getDate().getDayOfYear() * Constants.JULIAN_DAY +
                                          clock.getTime().getSecondsInDay();
                    file.setEpoch(offset);
                }
                else {
                    file.setEpoch(new AbsoluteDate(pi.keyValue, file.getTimeScale()));
                }
            }
                break;

            case SEMI_MAJOR_AXIS: {
                file.setA(Double.parseDouble(pi.keyValue) * 1000);
            }
                break;

            case MEAN_MOTION: {
                file.setMeanMotion(Double.parseDouble(pi.keyValue) * FastMath.PI / 43200.0);
            }
                break;

            case ECCENTRICITY: {
                file.setE(Double.parseDouble(pi.keyValue));
            }
                break;

            case INCLINATION: {
                file.setI(FastMath.toRadians(Double.parseDouble(pi.keyValue)));
            }
                break;

            case RA_OF_ASC_NODE: {
                file.setRaan(FastMath.toRadians(Double.parseDouble(pi.keyValue)));
            }
                break;

            case ARG_OF_PERICENTER: {
                file.setPa(FastMath.toRadians(Double.parseDouble(pi.keyValue)));
            }
                break;

            case MEAN_ANOMALY: {
                file.setAnomaly(FastMath.toRadians(Double.parseDouble(pi.keyValue)));
            }
                break;

            case GM: {
                file.setMuParsed(Double.parseDouble(pi.keyValue) * 1e9);
            }
                break;

            case MASS: {
                file.setMass(Double.parseDouble(pi.keyValue));
                checkSetComment(pi, file, ODMBlock.DATA_SPACECRAFT);
            }
                break;

            case SOLAR_RAD_AREA: {
                file.setSolarRadArea(Double.parseDouble(pi.keyValue));
                checkSetComment(pi, file, ODMBlock.DATA_SPACECRAFT);
            }
                break;

            case SOLAR_RAD_COEFF: {
                file.setSolarRadCoeff(Double.parseDouble(pi.keyValue));
                checkSetComment(pi, file, ODMBlock.DATA_SPACECRAFT);
            }
                break;

            case DRAG_AREA: {
                file.setDragArea(Double.parseDouble(pi.keyValue));
                checkSetComment(pi, file, ODMBlock.DATA_SPACECRAFT);
            }
                break;

            case DRAG_COEFF: {
                file.setDragCoeff(Double.parseDouble(pi.keyValue));
                checkSetComment(pi, file, ODMBlock.DATA_SPACECRAFT);
            }
                break;

            case EPHEMERIS_TYPE: {
                file.setEphemerisType(Integer.parseInt(pi.keyValue));
                checkSetComment(pi, file, ODMBlock.DATA_TLE_RELATED_PARAMETERS);
            }
                break;

            case CLASSIFICATION_TYPE: {
                file.setClassificationType(pi.keyValue.charAt(0));
            }
                break;

            case NORAD_CAT_ID: {
                file.setNoradID(Integer.parseInt(pi.keyValue));
            }
                break;

            case ELEMENT_SET_NO: {
                file.setElementSetNo(pi.keyValue);
            }
                break;

            case REV_AT_EPOCH: {
                file.setRevAtEpoch(Integer.parseInt(pi.keyValue));
            }
                break;

            case BSTAR: {
                file.setbStar(Double.parseDouble(pi.keyValue));
            }
                break;

            case MEAN_MOTION_DOT: {
                file.setMeanMotionDot(Double.parseDouble(pi.keyValue) * FastMath.PI / 1.86624e9);
            }
                break;

            case MEAN_MOTION_DDOT: {
                file.setMeanMotionDotDot(Double.parseDouble(pi.keyValue) *
                                         FastMath.PI / 5.3747712e13);
            }
                break;

            case COV_REF_FRAME: {
                checkSetComment(pi, file, ODMBlock.DATA_COVARIANCE);
                final CCSDSFrame frame = CCSDSFrame.valueOf(pi.keyValue.replaceAll("-", ""));
                if (frame.isLof()) {
                    file.setCovRefLofType(frame.getLofType());
                } else {
                    file.setCovRefFrame(frame.getFrame(conventions));
                }
            }
                break;

            case CX_X: {
                pi.covMatrix.addToEntry(0, 0, Double.parseDouble(pi.keyValue));
                file.setHasCovarianceMatrix(true);
            }
                break;

            case CY_X: {
                pi.covMatrix.addToEntry(1, 0, Double.parseDouble(pi.keyValue));
                pi.covMatrix.addToEntry(0, 1, Double.parseDouble(pi.keyValue));
            }
                break;

            case CY_Y: {
                pi.covMatrix.addToEntry(1, 1, Double.parseDouble(pi.keyValue));
            }
                break;

            case CZ_X: {
                pi.covMatrix.addToEntry(2, 0, Double.parseDouble(pi.keyValue));
                pi.covMatrix.addToEntry(0, 2, Double.parseDouble(pi.keyValue));
            }
                break;

            case CZ_Y: {
                pi.covMatrix.addToEntry(2, 1, Double.parseDouble(pi.keyValue));
                pi.covMatrix.addToEntry(1, 2, Double.parseDouble(pi.keyValue));
            }
                break;

            case CZ_Z: {
                pi.covMatrix.addToEntry(2, 2, Double.parseDouble(pi.keyValue));
            }
                break;

            case CX_DOT_X: {
                pi.covMatrix.addToEntry(3, 0, Double.parseDouble(pi.keyValue));
                pi.covMatrix.addToEntry(0, 3, Double.parseDouble(pi.keyValue));
            }
                break;

            case CX_DOT_Y: {
                pi.covMatrix.addToEntry(3, 1, Double.parseDouble(pi.keyValue));
                pi.covMatrix.addToEntry(1, 3, Double.parseDouble(pi.keyValue));
            }
                break;

            case CX_DOT_Z: {
                pi.covMatrix.addToEntry(3, 2, Double.parseDouble(pi.keyValue));
                pi.covMatrix.addToEntry(2, 3, Double.parseDouble(pi.keyValue));
            }
                break;

            case CX_DOT_X_DOT: {
                pi.covMatrix.addToEntry(3, 3, Double.parseDouble(pi.keyValue));
            }
                break;

            case CY_DOT_X: {
                pi.covMatrix.addToEntry(4, 0, Double.parseDouble(pi.keyValue));
                pi.covMatrix.addToEntry(0, 4, Double.parseDouble(pi.keyValue));
            }
                break;

            case CY_DOT_Y: {
                pi.covMatrix.addToEntry(4, 1, Double.parseDouble(pi.keyValue));
                pi.covMatrix.addToEntry(1, 4, Double.parseDouble(pi.keyValue));
            }
                break;

            case CY_DOT_Z: {
                pi.covMatrix.addToEntry(4, 2, Double.parseDouble(pi.keyValue));
                pi.covMatrix.addToEntry(2, 4, Double.parseDouble(pi.keyValue));
            }
                break;

            case CY_DOT_X_DOT: {
                pi.covMatrix.addToEntry(4, 3, Double.parseDouble(pi.keyValue));
                pi.covMatrix.addToEntry(3, 4, Double.parseDouble(pi.keyValue));
            }
                break;

            case CY_DOT_Y_DOT: {
                pi.covMatrix.addToEntry(4, 4, Double.parseDouble(pi.keyValue));
            }
                break;

            case CZ_DOT_X: {
                pi.covMatrix.addToEntry(5, 0, Double.parseDouble(pi.keyValue));
                pi.covMatrix.addToEntry(0, 5, Double.parseDouble(pi.keyValue));
            }
                break;

            case CZ_DOT_Y: {
                pi.covMatrix.addToEntry(5, 1, Double.parseDouble(pi.keyValue));
                pi.covMatrix.addToEntry(1, 5, Double.parseDouble(pi.keyValue));
            }
                break;

            case CZ_DOT_Z: {
                pi.covMatrix.addToEntry(5, 2, Double.parseDouble(pi.keyValue));
                pi.covMatrix.addToEntry(2, 5, Double.parseDouble(pi.keyValue));
            }
                break;

            case CZ_DOT_X_DOT: {
                pi.covMatrix.addToEntry(5, 3, Double.parseDouble(pi.keyValue));
                pi.covMatrix.addToEntry(3, 5, Double.parseDouble(pi.keyValue));
            }
                break;

            case CZ_DOT_Y_DOT: {
                pi.covMatrix.addToEntry(5, 4, Double.parseDouble(pi.keyValue));
                pi.covMatrix.addToEntry(4, 5, Double.parseDouble(pi.keyValue));
            }
                break;

            case CZ_DOT_Z_DOT: {
                pi.covMatrix.addToEntry(5, 5, Double.parseDouble(pi.keyValue));
                file.setCovarianceMatrix(pi.covMatrix);
            }
                break;

            case USER_DEFINED_X: {
                file.setUserDefinedParameters(pi.userDefinedKeyword,
                                              pi.keyValue);
            }
                break;

            default:
            }
        }
        reader.close();
        return file;
    }

    /** This method is called after a potential comment parsing. If there has been a comment parsing,
     * it sets the comment to the associated ODM block.
     * @param pi the parsing info
     * @param file the OMM file to be set
     * @param block comment's block
     * @throws OrekitException if the ODM block is DATA_MANEUVER
     */
    public void checkSetComment(final ParseInfo pi, final OMMFile file, final ODMBlock block)
        throws OrekitException {
        if (!pi.commentTmp.isEmpty()) {
            file.setComment(block, pi.commentTmp);
            pi.commentTmp.clear();
        }
    }

    /** Private class used to stock OMM parsing info.
     * @author sports
     */
    private static class ParseInfo {

        /** OMM file being read. */
        private OMMFile file;

        /** Keyword of the line being read. */
        private Keyword keyword;

        /** Key value of the line being read. */
        private String keyValue;

        /** Stored epoch. */
        private String epochTmp;

        /** Stored keyword. */
        private String keywordTmp;

        /** Position/Velocity covariance matrix. */
        private RealMatrix covMatrix;

        /** Stored comments. */
        private List<String> commentTmp;

        /** Boolean testing whether the reference frame has an associated epoch. */
        private boolean hasRefFrameEpoch;

        /** User defined keyword. */
        private String userDefinedKeyword;

        /** Create a new {@link ParseInfo} object. */
        protected ParseInfo() {
            covMatrix = new Array2DRowRealMatrix(6, 6);
            file = new OMMFile();
            commentTmp = new ArrayList<String>();
        }
    }
}
