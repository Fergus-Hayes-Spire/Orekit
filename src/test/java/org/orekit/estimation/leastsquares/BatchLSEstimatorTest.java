/* Copyright 2002-2016 CS Systèmes d'Information
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
package org.orekit.estimation.leastsquares;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;
import org.junit.Test;
import org.orekit.errors.OrekitException;
import org.orekit.estimation.Context;
import org.orekit.estimation.EstimationTestUtils;
import org.orekit.estimation.measurements.Measurement;
import org.orekit.estimation.measurements.PVMeasurementCreator;
import org.orekit.estimation.measurements.RangeMeasurementCreator;
import org.orekit.estimation.measurements.RangeRateMeasurementCreator;
import org.orekit.orbits.OrbitType;
import org.orekit.orbits.PositionAngle;
import org.orekit.propagation.Propagator;
import org.orekit.propagation.conversion.NumericalPropagatorBuilder;

public class BatchLSEstimatorTest {

    @Test
    public void testKeplerPV() throws OrekitException {

        Context context = EstimationTestUtils.eccentricContext();

        final NumericalPropagatorBuilder propagatorBuilder =
                        context.createBuilder(OrbitType.KEPLERIAN, PositionAngle.TRUE,
                                              1.0e-6, 60.0, 0.001);

        // create perfect PV measurements
        final Propagator propagator = EstimationTestUtils.createPropagator(context.initialOrbit,
                                                                           propagatorBuilder);
        final List<Measurement<?>> measurements =
                        EstimationTestUtils.createMeasurements(propagator,
                                                               new PVMeasurementCreator(),
                                                               0.0, 1.0, 300.0);

        // create orbit estimator
        final BatchLSEstimator estimator = new BatchLSEstimator(propagatorBuilder,
                                                                new LevenbergMarquardtOptimizer());
        for (final Measurement<?> measurement : measurements) {
            estimator.addMeasurement(measurement);
        }
        estimator.setConvergenceThreshold(1.0e-14, 1.0e-12);
        estimator.setMaxIterations(10);
        estimator.setMaxEvaluations(20);

        EstimationTestUtils.checkFit(context, estimator, 3, 4,
                                     0.0, 1.1e-8,
                                     0.0, 6.7e-8,
                                     0.0, 3.0e-9,
                                     0.0, 3.1e-12);

    }

    @Test
    public void testKeplerRange() throws OrekitException {

        Context context = EstimationTestUtils.eccentricContext();

        final NumericalPropagatorBuilder propagatorBuilder =
                        context.createBuilder(OrbitType.KEPLERIAN, PositionAngle.TRUE,
                                              1.0e-6, 60.0, 0.001);

        // create perfect range measurements
        final Propagator propagator = EstimationTestUtils.createPropagator(context.initialOrbit,
                                                                           propagatorBuilder);
        final List<Measurement<?>> measurements =
                        EstimationTestUtils.createMeasurements(propagator,
                                                               new RangeMeasurementCreator(context),
                                                               1.0, 3.0, 300.0);

        // create orbit estimator
        final BatchLSEstimator estimator = new BatchLSEstimator(propagatorBuilder,
                                                                new LevenbergMarquardtOptimizer());
        for (final Measurement<?> range : measurements) {
            estimator.addMeasurement(range);
        }
        estimator.setConvergenceThreshold(1.0e-14, 1.0e-12);
        estimator.setMaxIterations(10);
        estimator.setMaxEvaluations(20);

        EstimationTestUtils.checkFit(context, estimator, 3, 4,
                                     0.0, 1.0e-6,
                                     0.0, 2.0e-6,
                                     0.0, 3.8e-7,
                                     0.0, 1.5e-10);

    }

    @Test
    public void testKeplerRangeRate() throws OrekitException {

        Context context = EstimationTestUtils.eccentricContext();

        final NumericalPropagatorBuilder propagatorBuilder =
                        context.createBuilder(OrbitType.KEPLERIAN, PositionAngle.TRUE,
                                              1.0e-6, 60.0, 0.001);

        // create perfect range measurements
        final Propagator propagator = EstimationTestUtils.createPropagator(context.initialOrbit,
                                                                           propagatorBuilder);
        final List<Measurement<?>> measurements1 =
                        EstimationTestUtils.createMeasurements(propagator,
                                                               new RangeRateMeasurementCreator(context),
                                                               1.0, 3.0, 300.0);

        //final List<Measurement> measurements2 =
        //        EstimationTestUtils.createMeasurements(propagator,
        //                                               new RangeMeasurementCreator(context),
        //                                               1.0, 3.0, 300.0);
        
        final List<Measurement<?>> measurements = new ArrayList<Measurement<?>>();
        measurements.addAll(measurements1);
        //measurements.addAll(measurements2);
        
        // create orbit estimator
        final BatchLSEstimator estimator = new BatchLSEstimator(propagatorBuilder,
                                                                new LevenbergMarquardtOptimizer());
        for (final Measurement<?> rangerate : measurements) {
            estimator.addMeasurement(rangerate);
        }
        estimator.setConvergenceThreshold(1.0e-14, 1.0e-12);
        estimator.setMaxIterations(10);
        estimator.setMaxEvaluations(20);

        EstimationTestUtils.checkFit(context, estimator, 4, 5,
                                     0.0, 2e-3,
                                     0.0, 4e-3,
                                     0.0, 100,  // we only have range rate...
                                     0.0, 7e-3);
    }

    @Test
    public void testKeplerRangeAndRangeRate() throws OrekitException {

        Context context = EstimationTestUtils.eccentricContext();

        final NumericalPropagatorBuilder propagatorBuilder =
                        context.createBuilder(OrbitType.KEPLERIAN, PositionAngle.TRUE,
                                              1.0e-6, 60.0, 0.001);

        // create perfect range measurements
        final Propagator propagator = EstimationTestUtils.createPropagator(context.initialOrbit,
                                                                           propagatorBuilder);

        final List<Measurement<?>> measurementsRange =
                        EstimationTestUtils.createMeasurements(propagator,
                                                               new RangeMeasurementCreator(context),
                                                               1.0, 3.0, 300.0);
        final List<Measurement<?>> measurementsRangeRate =
                        EstimationTestUtils.createMeasurements(propagator,
                                                               new RangeRateMeasurementCreator(context),
                                                               1.0, 3.0, 300.0);

        // concat measurements
        final List<Measurement<?>> measurements = new ArrayList<Measurement<?>>();
        measurements.addAll(measurementsRange);
        measurements.addAll(measurementsRangeRate);
        
        // create orbit estimator
        final BatchLSEstimator estimator = new BatchLSEstimator(propagatorBuilder,
                                                                new LevenbergMarquardtOptimizer());
        for (final Measurement<?> meas : measurements) {
            estimator.addMeasurement(meas);
        }
        estimator.setConvergenceThreshold(1.0e-14, 1.0e-12);
        estimator.setMaxIterations(10);
        estimator.setMaxEvaluations(20);

        // we have low correlation between the two types of measurement. We can expect a good estimate.
        EstimationTestUtils.checkFit(context, estimator, 3, 4,
                                     0.0, 1,
                                     0.0, 1,
                                     0.0, 2e-4,
                                     0.0, 7e-8);
    }

}

