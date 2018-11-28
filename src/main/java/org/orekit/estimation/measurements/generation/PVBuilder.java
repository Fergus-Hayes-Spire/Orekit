/* Copyright 2002-2018 CS Systèmes d'Information
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
package org.orekit.estimation.measurements.generation;

import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.hipparchus.random.CorrelatedRandomVectorGenerator;
import org.orekit.estimation.measurements.PV;
import org.orekit.propagation.SpacecraftState;


/** Builder for {@link PV} measurements.
 * @author Luc Maisonobe
 * @since 9.3
 */
public class PVBuilder extends AbstractMeasurementBuilder<PV> {

    /** Simple constructor.
     * @param noiseSource noise source, may be null for generating perfect measurements
     * @param sigmaPosition theoretical standard deviation on position components
     * @param sigmaVelocity theoretical standard deviation on velocity components
     * @param baseWeight base weight
     * @param propagatorIndex index of the propagator related to this measurement
     */
    public PVBuilder(final CorrelatedRandomVectorGenerator noiseSource,
                     final double sigmaPosition, final double sigmaVelocity,
                     final double baseWeight, final int propagatorIndex) {
        super(noiseSource,
              new double[] {
                  sigmaPosition, sigmaVelocity
              }, new double[] {
                  baseWeight
              }, propagatorIndex);
    }

    /** {@inheritDoc} */
    @Override
    public PV build(final SpacecraftState... states) {

        final int propagatorIndex   = getPropagatorsIndices()[0];
        final double[] sigma        = getTheoreticalStandardDeviation();
        final double baseWeight     = getBaseWeight()[0];
        final SpacecraftState state = states[propagatorIndex];

        // create a dummy measurement
        final PV dummy = new PV(state.getDate(), Vector3D.NaN, Vector3D.NaN,
                                sigma[0], sigma[1], baseWeight, propagatorIndex);

        // estimate the perfect value of the measurement
        final double[] pv = dummy.estimate(0, 0, states).getEstimatedValue();

        // add the noise
        final double[] noise = getNoise();
        if (noise != null) {
            pv[0] += noise[0];
            pv[1] += noise[1];
            pv[2] += noise[2];
            pv[3] += noise[3];
            pv[4] += noise[4];
            pv[5] += noise[5];
        }

        // generate measurement
        return new PV(state.getDate(),
                      new Vector3D(pv[0], pv[1], pv[2]), new Vector3D(pv[3], pv[4], pv[5]),
                      sigma[0], sigma[1], baseWeight, propagatorIndex);

    }

}
