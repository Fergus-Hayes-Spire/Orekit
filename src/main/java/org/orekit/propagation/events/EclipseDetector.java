/* Copyright 2002-2019 CS Systèmes d'Information
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
package org.orekit.propagation.events;

import org.hipparchus.RealFieldElement;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.hipparchus.ode.events.Action;
import org.hipparchus.util.FastMath;
import org.orekit.bodies.OneAxisEllipsoid;
import org.orekit.errors.OrekitInternalError;
import org.orekit.frames.FieldTransform;
import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;
import org.orekit.frames.Transform;
import org.orekit.frames.TransformProvider;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.events.handlers.EventHandler;
import org.orekit.propagation.events.handlers.StopOnIncreasing;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.FieldAbsoluteDate;
import org.orekit.utils.PVCoordinatesProvider;

/** Finder for satellite eclipse related events.
 * <p>This class finds eclipse events, i.e. satellite within umbra (total
 * eclipse) or penumbra (partial eclipse).</p>
 * <p>The default implementation behavior is to {@link Action#CONTINUE continue}
 * propagation when entering the eclipse and to {@link Action#STOP stop} propagation
 * when exiting the eclipse. This can be changed by calling {@link
 * #withHandler(EventHandler)} after construction.</p>
 * @see org.orekit.propagation.Propagator#addEventDetector(EventDetector)
 * @author Pascal Parraud
 */
public class EclipseDetector extends AbstractDetector<EclipseDetector> {

    /** Occulting body. */
    private final OneAxisEllipsoid occulting;

    /** Occulted body. */
    private final PVCoordinatesProvider occulted;

    /** Occulted body radius (m). */
    private final double occultedRadius;

    /** Umbra, if true, or penumbra, if false, detection flag. */
    private final boolean totalEclipse;

    /** Build a new eclipse detector.
     * <p>The new instance is a total eclipse (umbra) detector with default
     * values for maximal checking interval ({@link #DEFAULT_MAXCHECK})
     * and convergence threshold ({@link #DEFAULT_THRESHOLD}).</p>
     * @param occulted the body to be occulted
     * @param occultedRadius the radius of the body to be occulted (m)
     * @param occulting the occulting body
     * @since 10.0
     */
    public EclipseDetector(final PVCoordinatesProvider occulted,  final double occultedRadius,
                           final OneAxisEllipsoid occulting) {
        this(DEFAULT_MAXCHECK, DEFAULT_THRESHOLD, DEFAULT_MAX_ITER,
             new StopOnIncreasing<EclipseDetector>(),
             occulted, occultedRadius, occulting, true);
    }

    /** Build a new eclipse detector.
     * <p>The new instance is a total eclipse (umbra) detector with default
     * values for maximal checking interval ({@link #DEFAULT_MAXCHECK})
     * and convergence threshold ({@link #DEFAULT_THRESHOLD}).</p>
     * @param occulted the body to be occulted
     * @param occultedRadius the radius of the body to be occulted (m)
     * @param occulting the occulting body
     * @param occultingRadius the occulting body radius (m)
     */
    public EclipseDetector(final PVCoordinatesProvider occulted,  final double occultedRadius,
                           final PVCoordinatesProvider occulting, final double occultingRadius) {
        this(DEFAULT_MAXCHECK, DEFAULT_THRESHOLD,
             occulted, occultedRadius, occulting, occultingRadius);
    }

    /** Build a new eclipse detector.
     * <p>The new instance is a total eclipse (umbra) detector with default
     * value for convergence threshold ({@link #DEFAULT_THRESHOLD}).</p>
     * <p>The maximal interval between eclipse checks should be smaller than
     * the half duration of the minimal pass to handle, otherwise some short
     * passes could be missed.</p>
     * @param maxCheck maximal checking interval (s)
     * @param occulted the body to be occulted
     * @param occultedRadius the radius of the body to be occulted in meters
     * @param occulting the occulting body
     * @param occultingRadius the occulting body radius in meters
     */
    public EclipseDetector(final double maxCheck,
                           final PVCoordinatesProvider occulted,  final double occultedRadius,
                           final PVCoordinatesProvider occulting, final double occultingRadius) {
        this(maxCheck, DEFAULT_THRESHOLD,
             occulted, occultedRadius, occulting, occultingRadius);
    }

    /** Build a new eclipse detector.
     * <p>The new instance is a total eclipse (umbra) detector.</p>
     * <p>The maximal interval between eclipse checks should be smaller than
     * the half duration of the minimal pass to handle, otherwise some short
     * passes could be missed.</p>
     * @param maxCheck maximal checking interval (s)
     * @param threshold convergence threshold (s)
     * @param occulted the body to be occulted
     * @param occultedRadius the radius of the body to be occulted in meters
     * @param occulting the occulting body
     * @param occultingRadius the occulting body radius in meters
     */
    public EclipseDetector(final double maxCheck, final double threshold,
                           final PVCoordinatesProvider occulted,  final double occultedRadius,
                           final PVCoordinatesProvider occulting, final double occultingRadius) {
        this(maxCheck, threshold, DEFAULT_MAX_ITER, new StopOnIncreasing<EclipseDetector>(),
             occulted, occultedRadius, new SphericalOccultingBody(occulting, occultingRadius), true);
    }

    /** Private constructor with full parameters.
     * <p>
     * This constructor is private as users are expected to use the builder
     * API with the various {@code withXxx()} methods to set up the instance
     * in a readable manner without using a huge amount of parameters.
     * </p>
     * @param maxCheck maximum checking interval (s)
     * @param threshold convergence threshold (s)
     * @param maxIter maximum number of iterations in the event time search
     * @param handler event handler to call at event occurrences
     * @param occulted the body to be occulted
     * @param occultedRadius the radius of the body to be occulted in meters
     * @param occulting the occulting body
     * @param totalEclipse umbra (true) or penumbra (false) detection flag
     * @since 10.0
     */
    private EclipseDetector(final double maxCheck, final double threshold,
                            final int maxIter, final EventHandler<? super EclipseDetector> handler,
                            final PVCoordinatesProvider occulted,  final double occultedRadius,
                            final OneAxisEllipsoid occulting, final boolean totalEclipse) {
        super(maxCheck, threshold, maxIter, handler);
        this.occulted       = occulted;
        this.occultedRadius = FastMath.abs(occultedRadius);
        this.occulting      = occulting;
        this.totalEclipse   = totalEclipse;
    }

    /** {@inheritDoc} */
    @Override
    protected EclipseDetector create(final double newMaxCheck, final double newThreshold,
                                     final int nawMaxIter, final EventHandler<? super EclipseDetector> newHandler) {
        return new EclipseDetector(newMaxCheck, newThreshold, nawMaxIter, newHandler,
                                   occulted, occultedRadius, occulting, totalEclipse);
    }

    /**
     * Setup the detector to full umbra detection.
     * <p>
     * This will override a penumbra/umbra flag if it has been configured previously.
     * </p>
     * @return a new detector with updated configuration (the instance is not changed)
     * @see #withPenumbra()
     * @since 6.1
     */
    public EclipseDetector withUmbra() {
        return new EclipseDetector(getMaxCheckInterval(), getThreshold(), getMaxIterationCount(), getHandler(),
                                   occulted, occultedRadius, occulting, true);
    }

    /**
     * Setup the detector to penumbra detection.
     * <p>
     * This will override a penumbra/umbra flag if it has been configured previously.
     * </p>
     * @return a new detector with updated configuration (the instance is not changed)
     * @see #withUmbra()
     * @since 6.1
     */
    public EclipseDetector withPenumbra() {
        return new EclipseDetector(getMaxCheckInterval(), getThreshold(), getMaxIterationCount(), getHandler(),
                                   occulted, occultedRadius, occulting, false);
    }

    /** Get the total eclipse detection flag.
     * @return the total eclipse detection flag (true for umbra events detection,
     * false for penumbra events detection)
     */
    public boolean getTotalEclipse() {
        return totalEclipse;
    }

    /** Compute the value of the switching function.
     * This function becomes negative when entering the region of shadow
     * and positive when exiting.
     * @param s the current state information: date, kinematics, attitude
     * @return value of the switching function
     */
    public double g(final SpacecraftState s) {
        final Vector3D pted = occulted.getPVCoordinates(s.getDate(), occulting.getBodyFrame()).getPosition();
        final Vector3D psat = s.getPVCoordinates(occulting.getBodyFrame()).getPosition();
        final Vector3D ps   = psat.subtract(pted);
        final double angle  = Vector3D.angle(ps, psat);
        final double rs     = FastMath.asin(occultedRadius / ps.getNorm());
        if (Double.isNaN(rs)) {
            return FastMath.PI;
        }
        final double ro     = FastMath.asin(occulting.getEquatorialRadius() / psat.getNorm());
        if (Double.isNaN(ro)) {
            return -FastMath.PI;
        }
        return totalEclipse ? (angle - ro + rs) : (angle - ro - rs);
    }

    /** Temporary dummy ellipsoid for spherical occulting bodies.
     * @deprecated as of 10.0, this class is a temporary intermediate for deprecated constructors
     */
    @Deprecated
    private static class SphericalOccultingBody extends OneAxisEllipsoid {

        /** Serializable UID. */
        private static final long serialVersionUID = 20190403L;

        /** Simple constructor.
         * @param occulting the occulting body
         * @param occultingRadius the occulting body radius in meters
         */
        SphericalOccultingBody(final PVCoordinatesProvider occulting, final double occultingRadius) {
            super(occultingRadius, 0.0,
                  new Frame(FramesFactory.getEME2000(),
                            new TransformProvider() {

                            /** Serializable UID. */
                            private static final long serialVersionUID = 20190403L;

                            /** {@inheritDoc} */
                            @Override
                            public Transform getTransform(final AbsoluteDate date) {
                                return new Transform(date, occulting.getPVCoordinates(date, FramesFactory.getEME2000()));
                            }

                            /** {@inheritDoc} */
                            @Override
                            public <T extends RealFieldElement<T>> FieldTransform<T> getTransform(final FieldAbsoluteDate<T> date) {
                                // never called
                                throw new OrekitInternalError(null);
                            }
                    }, "dummy"));
        }

    }

}
