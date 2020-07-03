/* Copyright 2002-2020 CS Group
 * Licensed to CS Group (CS) under one or more
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
package org.orekit.models.earth.atmosphere;

import org.hipparchus.util.Decimal64;
import org.hipparchus.RealFieldElement;
import org.hipparchus.geometry.euclidean.threed.FieldVector3D;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.hipparchus.util.FastMath;
import org.hipparchus.analysis.interpolation.TrivariateGridInterpolator;
import org.hipparchus.analysis.interpolation.TricubicInterpolator;
import org.hipparchus.analysis.TrivariateFunction;
import org.orekit.bodies.BodyShape;
import org.orekit.bodies.FieldGeodeticPoint;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.frames.Frame;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.FieldAbsoluteDate;


public class CustomAtmosphere implements Atmosphere {

    /** Serializable UID.*/
    private static final long serialVersionUID = 2772347498196369601L;

    /** Earth shape model. */
    private BodyShape    shape;

    /** Density array. */
    private double[] rho;

    /** Latitudes. */
    private double[] lat;
    
    /** Longitudes. */
    private double[] lon;
    
    /** Altitudes. */
    private double[] alt;
    
    /** Density grid. */
    private double[][][] rho3D;

    /** Interpolated density function. */
    private TrivariateFunction p;

    public CustomAtmosphere(final BodyShape shape, final double[] rho, 
		    			final double[] lat, final double[] lon, final double[] alt) {

	this.shape = shape;
	this.lat = lat;
	this.lon = lon;
	this.alt = alt;
	this.rho = rho;
    	this.rho3D  = makeRhoGrid(rho, lat, lon, alt);

	TrivariateGridInterpolator interpolator = new TricubicInterpolator();
	this.p = interpolator.interpolate(lat, lon, alt, rho3D);
    }

    public double[][][] makeRhoGrid(final double[] rho_, final double[] lat, 
		    		final double[] lon, final double[] alt) {
        
	final double[][][] rho = new double[lat.length][lon.length][alt.length];
        for (int i = 0; i < lat.length; i++) {
	    for (int j = 0; j < lon.length; j++) {
		for (int k = 0; k < alt.length; k++) {
		    rho[i][j][k] = rho_[(i * lon.length * alt.length) + (alt.length * j) + k];
		}
	    }
        }
        
	return rho;
    }

    /** {@inheritDoc} */
    public Frame getFrame() {
        return shape.getBodyFrame();
    }

    /** Returns the density grid */
    public double[][][] getRhoGrid() {
        return rho3D;
    }

    /** {@inheritDoc} */
    public double getDensity(final AbsoluteDate date, final Vector3D position, final Frame frame) {
        final GeodeticPoint gp = shape.transform(position, frame, date);
	return p.value(gp.getLatitude()*(180./FastMath.PI),gp.getLongitude()*(180./FastMath.PI),gp.getAltitude());
    }

    @Override
    public <T extends RealFieldElement<T>> T
    	getDensity(final FieldAbsoluteDate<T> date, final FieldVector3D<T> position,
                   final Frame frame) {
	final FieldGeodeticPoint<T> gp = shape.transform(position, frame, date);
	final T zero = position.getX().getField().getZero();
	final double lat_ = gp.getLatitude().getReal()*(180./FastMath.PI);
        final double lon_ = gp.getLongitude().getReal()*(180./FastMath.PI);
        final double alt_ = gp.getAltitude().getReal();
	final double rho_ = p.value(lat_,lon_,alt_);
	final T rho = zero.add(rho_);
	return rho;
    }

}
