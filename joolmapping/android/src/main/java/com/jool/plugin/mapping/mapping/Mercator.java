package com.jool.plugin.mapping.mapping;


/**
 Implements the Mercator projection.
 
 
 <p>This map projection introduced in 1569 by Gerardus Mercator. It is often described as a cylindrical projection,
 but it must be derived mathematically. The meridians are equally spaced, parallel vertical lines, and the
 parallels of latitude are parallel, horizontal straight lines, spaced farther and farther apart as their distance
 from the Equator increases. This projection is widely used for navigation charts, because any straight line
 on a Mercator-projection map is a line of constant true bearing that enables a navigator to plot a straight-line
 course. It is less practical for world maps because the scale is distorted; areas farther away from the equator
 appear disproportionately large. On a Mercator projection, for example, the landmass of Greenland appears to be
 greater than that of the continent of South America; in actual area, Greenland is smaller than the Arabian Peninsula.
 </p>
 
*/
//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
//#if HAS_SYSTEM_SERIALIZABLEATTRIBUTE

//#endif
public class Mercator extends MapProjection
{
	//double lon_center;		//Center longitude (projection center)
	//double lat_origin;		//center latitude
	//double e,e2;			//eccentricity constants
	private final double _k0; //small value m

	/** 
	 Initializes the MercatorProjection object with the specified parameters to project points. 
	 
	 @param parameters ParameterList with the required parameters.
	 
	 
	*/
	public Mercator(Iterable<ProjectionParameter> parameters)
	{
		this(parameters, null);
	}

	/** 
	 Initializes the MercatorProjection object with the specified parameters.
	 
	 @param parameters List of parameters to initialize the projection.
	 
	 <p>The parameters this projection expects are listed below.</p>
	 <list type="table">
	 <listheader><term>Items</term><description>Descriptions</description></listheader>
	 <item><term>central_meridian</term><description>The longitude of the point from which the values of both the geographical coordinates on the ellipsoid and the grid coordinates on the projection are deemed to increment or decrement for computational purposes. Alternatively it may be considered as the longitude of the point which in the absence of application of false coordinates has grid coordinates of (0,0).</description></item>
	 <item><term>latitude_of_origin</term><description>The latitude of the point from which the values of both the geographical coordinates on the ellipsoid and the grid coordinates on the projection are deemed to increment or decrement for computational purposes. Alternatively it may be considered as the latitude of the point which in the absence of application of false coordinates has grid coordinates of (0,0).</description></item>
	 <item><term>scale_factor</term><description>The factor by which the map grid is reduced or enlarged during the projection process, defined by its value at the natural origin.</description></item>
	 <item><term>false_easting</term><description>Since the natural origin may be at or near the centre of the projection and under normal coordinate circumstances would thus give rise to negative coordinates over parts of the mapped area, this origin is usually given false coordinates which are large enough to avoid this inconvenience. The False Easting, FE, is the easting value assigned to the abscissa (east).</description></item>
	 <item><term>false_northing</term><description>Since the natural origin may be at or near the centre of the projection and under normal coordinate circumstances would thus give rise to negative coordinates over parts of the mapped area, this origin is usually given false coordinates which are large enough to avoid this inconvenience. The False Northing, FN, is the northing value assigned to the ordinate.</description></item>
	 </list>
	 
	*/
	protected Mercator(Iterable<ProjectionParameter> parameters, Mercator inverse)
	{
		super(parameters, inverse);
		setAuthority("EPSG");
		ProjectionParameter scale_factor = GetParameter("scale_factor");

		if (scale_factor == null) //This is a two standard parallel Mercator projection (2SP)
		{
			_k0 = Math.cos(Utils.clone(lat_origin)) / Math.sqrt(1.0 - Utils.clone(_es) * Math.sin(lat_origin) * Math.sin(lat_origin));
			setAuthorityCode(9805);
			setName("Mercator_2SP");
		}
		else //This is a one standard parallel Mercator projection (1SP)
		{
			_k0 = scale_factor.getValue();
			setName("Mercator_1SP");
		}
	}

	/** 
	 Converts coordinates in decimal degrees to projected meters.
	 
	 
	 <p>The parameters this projection expects are listed below.</p>
	 <list type="table">
	 <listheader><term>Items</term><description>Descriptions</description></listheader>
	 <item><term>longitude_of_natural_origin</term><description>The longitude of the point from which the values of both the geographical coordinates on the ellipsoid and the grid coordinates on the projection are deemed to increment or decrement for computational purposes. Alternatively it may be considered as the longitude of the point which in the absence of application of false coordinates has grid coordinates of (0,0).  Sometimes known as ""central meridian""."</description></item>
	 <item><term>latitude_of_natural_origin</term><description>The latitude of the point from which the values of both the geographical coordinates on the ellipsoid and the grid coordinates on the projection are deemed to increment or decrement for computational purposes. Alternatively it may be considered as the latitude of the point which in the absence of application of false coordinates has grid coordinates of (0,0).</description></item>
	 <item><term>scale_factor_at_natural_origin</term><description>The factor by which the map grid is reduced or enlarged during the projection process, defined by its value at the natural origin.</description></item>
	 <item><term>false_easting</term><description>Since the natural origin may be at or near the centre of the projection and under normal coordinate circumstances would thus give rise to negative coordinates over parts of the mapped area, this origin is usually given false coordinates which are large enough to avoid this inconvenience. The False Easting, FE, is the easting value assigned to the abscissa (east).</description></item>
	 <item><term>false_northing</term><description>Since the natural origin may be at or near the centre of the projection and under normal coordinate circumstances would thus give rise to negative coordinates over parts of the mapped area, this origin is usually given false coordinates which are large enough to avoid this inconvenience. The False Northing, FN, is the northing value assigned to the ordinate .</description></item>
	 </list>
	 
	 @param lonlat The point in decimal degrees.
	 @return Point in projected meters
	*/
	@Override
	protected double[] RadiansToMeters(double[] lonlat)
	{
		if (Double.isNaN(lonlat[0]) || Double.isNaN(lonlat[1]))
		{
			return new double[] {Double.NaN, Double.NaN};
		}

		double dLongitude = lonlat[0];
		double dLatitude = lonlat[1];

		/* Forward equations */
		if (Math.abs(Math.abs(dLatitude) - HALF_PI) <= EPSLN)
		{
			throw new IllegalArgumentException("Transformation cannot be computed at the poles.");
		}

		double esinphi = Utils.clone(_e) * Math.sin(dLatitude);
		double x = Utils.clone(_semiMajor) * Utils.clone(_k0) * (dLongitude - central_meridian);
		double y = Utils.clone(_semiMajor) * Utils.clone(_k0) * Math.log(Math.tan(PI * 0.25 + dLatitude * 0.5) * Math.pow((1 - esinphi) / (1 + esinphi), Utils.clone(_e) * 0.5));
		return lonlat.length < 3 ? new double[] {x, y} : new double[] {x, y, lonlat[2]};
	}

	/** 
	 Converts coordinates in projected meters to decimal degrees.
	 
	 @param p Point in meters
	 @return Transformed point in decimal degrees
	*/
	@Override
	protected double[] MetersToRadians(double[] p)
	{
		double dLongitude = Double.NaN;
		double dLatitude = Double.NaN;

		/* Inverse equations
		  -----------------*/
		double dX = p[0]; //* _metersPerUnit - this._falseEasting;
		double dY = p[1]; // * _metersPerUnit - this._falseNorthing;
		double ts = Math.exp(-dY / (Utils.clone(this._semiMajor) * Utils.clone(_k0))); //t

		double chi = HALF_PI - 2 * Math.atan(ts);
		double e4 = Math.pow(Utils.clone(_e), 4);
		double e6 = Math.pow(Utils.clone(_e), 6);
		double e8 = Math.pow(Utils.clone(_e), 8);

		dLatitude = chi + (Utils.clone(_es) * 0.5 + 5 * e4 / 24 + e6 / 12 + 13 * e8 / 360) * Math.sin(2 * chi) + (7 * e4 / 48 + 29 * e6 / 240 + 811 * e8 / 11520) * Math.sin(4 * chi) + + (7 * e6 / 120 + 81 * e8 / 1120) * Math.sin(6 * chi) + + (4279 * e8 / 161280) * Math.sin(8 * chi);

		dLongitude = dX / (Utils.clone(_semiMajor) * Utils.clone(_k0)) + central_meridian;
		return p.length < 3 ? new double[] {dLongitude, dLatitude} : new double[] {dLongitude, dLatitude, p[2]};
	}

	/** 
	 Returns the inverse of this projection.
	 
	 @return IMathTransform that is the reverse of the current projection.
	*/
	@Override
	public IMathTransform Inverse()
	{
		if (_inverse == null)
		{
			_inverse = new Mercator(_Parameters.ToProjectionParameter(), this);
		}
		return _inverse;
	}
}