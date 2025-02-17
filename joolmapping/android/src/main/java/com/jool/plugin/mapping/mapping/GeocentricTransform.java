package com.jool.plugin.mapping.mapping;


import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 
 
 
 <p>Latitude, Longitude and ellipsoidal height in terms of a 3-dimensional geographic system
 may by expressed in terms of a geocentric (earth centered) Cartesian coordinate reference system
 X, Y, Z with the Z axis corresponding to the earth's rotation axis positive northwards, the X
 axis through the intersection of the prime meridian and equator, and the Y axis through
 the intersection of the equator with longitude 90 degrees east. The geographic and geocentric
 systems are based on the same geodetic datum.</p>
 <p>Geocentric coordinate reference systems are conventionally taken to be defined with the X
 axis through the intersection of the Greenwich meridian and equator. This requires that the equivalent
 geographic coordinate reference systems based on a non-Greenwich prime meridian should first be
 transformed to their Greenwich equivalent. Geocentric coordinates X, Y and Z take their units from
 the units of the ellipsoid axes (a and b). As it is conventional for X, Y and Z to be in metres,
 if the ellipsoid axis dimensions are given in another linear unit they should first be converted
 to metres.</p>
 
*/
//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
//#if HAS_SYSTEM_SERIALIZABLEATTRIBUTE

//#endif
public class GeocentricTransform extends MathTransform
{
	private static final double COS_67P5 = 0.38268343236508977; // cosine of 67.5 degrees
	private static final double AD_C = 1.0026000; // Toms region 1 constant

	/** 
	 
	*/
	protected boolean _isInverse = false;

	private double es; // Eccentricity squared : (a^2 - b^2)/a^2
	private double semiMajor; // major axis
	private double semiMinor; // minor axis
	private double ab; // Semi_major / semi_minor
	private double ba; // Semi_minor / semi_major
	private double ses; // Second eccentricity squared : (a^2 - b^2)/b^2

	/** 
	 
	*/
	protected CopyOnWriteArrayList<ProjectionParameter> _Parameters;

	/** 
	 
	*/
	protected MathTransform _inverse;

	/** 
	 Initializes a geocentric projection object
	 
	 @param parameters List of parameters to initialize the projection.
	 @param isInverse Indicates whether the projection forward (meters to degrees or degrees to meters).
	*/
	public GeocentricTransform(CopyOnWriteArrayList<ProjectionParameter> parameters, boolean isInverse)
	{
		this(parameters);
		_isInverse = isInverse;
	}

	/** 
	 Initializes a geocentric projection object
	 
	 @param parameters List of parameters to initialize the projection.
	*/
	public GeocentricTransform(CopyOnWriteArrayList<ProjectionParameter> parameters)
	{
		_Parameters = parameters;
		semiMajor = ListHelper.find(_Parameters, (ProjectionParameter par) ->
		{
				// Do not remove the following lines containing "_Parameters = _Parameters;"
				// There is an issue deploying code with anonymous delegates to
				// SQLCLR because they're compiled using a writable static field
				// (which is not allowed in SQLCLR SAFE mode).
				// To workaround this, we will use a harmless reference to the
				// _Parameters field inside the anonymous delegate code making
				// the compiler generates a private nested class with a function
				// that is used as the delegate.
				// For details, see http://www.hedgate.net/articles/2006/01/27/troubles-with-shared-state-and-anonymous-delegates-in-sqlclr
///#pragma warning disable 1717
				_Parameters = _Parameters;
///#pragma warning restore 1717

				return par.getName().equalsIgnoreCase("semi_major");
		}).getValue();

		semiMinor = ListHelper.find(_Parameters, (ProjectionParameter par) ->
		{
///#pragma warning disable 1717
				_Parameters = _Parameters; // See explanation above.
///#pragma warning restore 1717
				return par.getName().equalsIgnoreCase("semi_minor");
		}).getValue();

		es = 1.0 - (semiMinor * semiMinor) / (semiMajor * semiMajor); //e^2
		ses = (Math.pow(semiMajor, 2) - Math.pow(semiMinor, 2)) / Math.pow(semiMinor, 2);
		ba = semiMinor / semiMajor;
		ab = semiMajor / semiMinor;
	}

	@Override
	public int getDimSource()
	{
		return 3;
	}

	@Override
	public int getDimTarget()
	{
		return 3;
	}

	/** 
	 Returns the inverse of this conversion.
	 
	 @return IMathTransform that is the reverse of the current conversion.
	*/
	@Override
	public IMathTransform Inverse()
	{
		if (_inverse == null)
		{
			_inverse = new GeocentricTransform(this._Parameters, !_isInverse);
		}
		return _inverse;
	}

	/** 
	 Converts coordinates in decimal degrees to projected meters.
	 
	 @param lonlat The point in decimal degrees.
	 @return Point in projected meters
	*/
	private double[] DegreesToMeters(double[] lonlat)
	{
		double lon = Degrees2Radians(lonlat[0]);
		double lat = Degrees2Radians(lonlat[1]);
		double h = lonlat.length < 3 ? 0 : (new Double(lonlat[2])).equals(Double.NaN) ? 0 : lonlat[2];
		double v = semiMajor / Math.sqrt(1 - es * Math.pow(Math.sin(lat), 2));
		double x = (v + h) * Math.cos(lat) * Math.cos(lon);
		double y = (v + h) * Math.cos(lat) * Math.sin(lon);
		double z = ((1 - es) * v + h) * Math.sin(lat);
		return new double[] {x, y, z};
	}

	/** 
	 Converts coordinates in projected meters to decimal degrees.
	 
	 @param pnt Point in meters
	 @return Transformed point in decimal degrees		
	*/
	private double[] MetersToDegrees(double[] pnt)
	{
		boolean At_Pole = false; // indicates whether location is in polar region */
		double Z = pnt.length < 3 ? 0 : (new Double(pnt[2])).equals(Double.NaN) ? 0 : pnt[2];

		double lon = 0;
		double lat = 0;
		double Height = 0;
		if (pnt[0] != 0.0)
		{
			lon = Math.atan2(pnt[1], pnt[0]);
		}
		else
		{
			if (pnt[1] > 0)
			{
				lon = Math.PI / 2;
			}
			else if (pnt[1] < 0)
			{
				lon = -Math.PI * 0.5;
			}
			else
			{
				At_Pole = true;
				lon = 0.0;
				if (Z > 0.0)
				{ // north pole
					lat = Math.PI * 0.5;
				}
				else if (Z < 0.0)
				{ // south pole
					lat = -Math.PI * 0.5;
				}
				else
				{ // center of earth
					return new double[] {Radians2Degrees(lon), Radians2Degrees(Math.PI * 0.5), -semiMinor};
				}
			}
		}
		double W2 = pnt[0] * pnt[0] + pnt[1] * pnt[1]; // Square of distance from Z axis
		double W = Math.sqrt(W2); // distance from Z axis
		double T0 = Z * AD_C; // initial estimate of vertical component
		double S0 = Math.sqrt(T0 * T0 + W2); //initial estimate of horizontal component
		double Sin_B0 = T0 / S0; //sin(B0), B0 is estimate of Bowring aux variable
		double Cos_B0 = W / S0; //cos(B0)
		double Sin3_B0 = Math.pow(Sin_B0, 3);
		double T1 = Z + semiMinor * ses * Sin3_B0; //corrected estimate of vertical component
		double Sum = W - semiMajor * es * Cos_B0 * Cos_B0 * Cos_B0; //numerator of cos(phi1)
		double S1 = Math.sqrt(T1 * T1 + Sum * Sum); //corrected estimate of horizontal component
		double Sin_p1 = T1 / S1; //sin(phi1), phi1 is estimated latitude
		double Cos_p1 = Sum / S1; //cos(phi1)
		double Rn = semiMajor / Math.sqrt(1.0 - es * Sin_p1 * Sin_p1); //Earth radius at location
		if (Cos_p1 >= COS_67P5)
		{
			Height = W / Cos_p1 - Rn;
		}
		else if (Cos_p1 <= -COS_67P5)
		{
			 Height = W / -Cos_p1 - Rn;
		}
		else
		{
			Height = Z / Sin_p1 + Rn * (es - 1.0);
		}
		if (!At_Pole)
		{
			lat = Math.atan(Sin_p1 / Cos_p1);
		}
		return new double[] {Radians2Degrees(lon), Radians2Degrees(lat), Height};
	}

	/** 
	 Transforms a coordinate point. The passed parameter point should not be modified.
	 
	 @param point
	 @return 
	*/
	@Override
	public double[] Transform(double[] point)
	{
		if (!_isInverse)
		{
			 return DegreesToMeters(point);
		}
		return MetersToDegrees(point);
	}

	/** 
	 Transforms a list of coordinate point ordinal values.
	 
	 @param points
	 @return 
	 
	 This method is provided for efficiently transforming many points. The supplied array
	 of ordinal values will contain packed ordinal values. For example, if the source
	 dimension is 3, then the ordinals will be packed in this order (x0,y0,z0,x1,y1,z1 ...).
	 The size of the passed array must be an integer multiple of DimSource. The returned
	 ordinal values are packed in a similar way. In some DCPs. the ordinals may be
	 transformed in-place, and the returned array may be the same as the passed array.
	 So any client code should not attempt to reuse the passed ordinal values (although
	 they can certainly reuse the passed array). If there is any problem then the server
	 implementation will throw an exception. If this happens then the client should not
	 make any assumptions about the state of the ordinal values.
	 
	*/
	@Override
	public List<double[]> TransformList(List<double[]> points)
	{
		CopyOnWriteArrayList<double[]> result = new CopyOnWriteArrayList<double[]>();
		for (int i = 0; i < points.size(); i++)
		{
			double[] point = points.get(i);
			result.add(Transform(point));
		}
		return result;
	}

//
//	@Override
//	public List<Coordinate> TransformList(List<Coordinate> points)
//	{
//		ArrayList<Coordinate> result = new ArrayList<Coordinate>(points.size());
//		for (Coordinate coordinate : points)
//		{
//			result.add(Transform(coordinate));
//		}
//		return result;
//	}

	/** 
	 Reverses the transformation
	*/
	@Override
	public void Invert()
	{
		_isInverse = !_isInverse;
	}

	/** 
	 Gets a Well-Known text representation of this object.
	 
	 <value></value>
	*/
	@Override
	public String getWKT()
	{
		throw new UnsupportedOperationException("The method or operation is not implemented.");
	}
	/** 
	 Gets an XML representation of this object.
	 
	 <value></value>
	*/
	@Override
	public String getXML()
	{
		throw new UnsupportedOperationException("The method or operation is not implemented.");
	}
}