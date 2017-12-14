/* Copyright 2002-2017 CS Systèmes d'Information
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
package org.orekit.gnss.attitude;

import org.hipparchus.analysis.differentiation.DerivativeStructure;
import org.hipparchus.util.FastMath;
import org.orekit.errors.OrekitException;
import org.orekit.time.AbsoluteDate;
import org.orekit.utils.FieldPVCoordinates;
import org.orekit.utils.PVCoordinatesProvider;
import org.orekit.utils.TimeStampedAngularCoordinates;
import org.orekit.utils.TimeStampedPVCoordinates;

/**
 * Attitude providers for Beidou Medium Earth Orbit navigation satellites.
 * @author Luc Maisonobe Java translation
 * @since 9.2
 */
public class BeidouMeo extends AbstractGNSSAttitudeProvider {

    /** Serializable UID. */
    private static final long serialVersionUID = 20171114L;

    /** Constant for Beidou turns. */
    private static final double BETA_0 = FastMath.toRadians(2.0);

    /** Simple constructor.
     * @param validityStart start of validity for this provider
     * @param validityEnd end of validity for this provider
     * @param sun provider for Sun position
     */
    public BeidouMeo(final AbsoluteDate validityStart, final AbsoluteDate validityEnd,
                     final PVCoordinatesProvider sun) {
        super(validityStart, validityEnd, sun);
    }

    /** {@inheritDoc} */
    @Override
    protected TimeStampedAngularCoordinates correctYaw(final TimeStampedPVCoordinates pv,
                                                       final FieldPVCoordinates<DerivativeStructure> pvDS,
                                                       final DerivativeStructure beta,
                                                       final DerivativeStructure svbCos,
                                                       final TimeStampedAngularCoordinates nominalYaw)
        throws OrekitException {

        if (FastMath.abs(beta.getValue()) < 2 * BETA_0) {
            // when Sun is close to orbital plane, attitude is in Orbit Normal (ON) yaw
            return orbitNormalYaw(pv);
        }

        // in nominal yaw mode
        return nominalYaw;

    }

}
