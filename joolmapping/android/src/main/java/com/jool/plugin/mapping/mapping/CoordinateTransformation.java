package com.jool.plugin.mapping.mapping;



/**
 Describes a coordinate transformation. This class only describes a 
 coordinate transformation, it does not actually perform the transform 
 operation on points. To transform points you must use a <see cref="MathTransform"/>.
 */
//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
//#if HAS_SYSTEM_SERIALIZABLEATTRIBUTE

//#endif
public class CoordinateTransformation implements ICoordinateTransformation
{
	/**
	 Initializes an instance of a CoordinateTransformation

	 @param sourceCS Source coordinate system
	 @param targetCS Target coordinate system
	 @param transformType Transformation type
	 @param mathTransform Math transform
	 @param name Name of transform
	 @param authority Authority
	 @param authorityCode Authority code
	 @param areaOfUse Area of use
	 @param remarks Remarks
	 */
	public CoordinateTransformation(ICoordinateSystem sourceCS, ICoordinateSystem targetCS, TransformType transformType, IMathTransform mathTransform, String name, String authority, long authorityCode, String areaOfUse, String remarks)
	{
		super();
		_TargetCS = targetCS;
		_SourceCS = sourceCS;
		_TransformType = transformType;
		_MathTransform = mathTransform;
		_Name = name;
		_Authority = authority;
		_AuthorityCode = authorityCode;
		_AreaOfUse = areaOfUse;
		_Remarks = remarks;
	}



//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
	///#region ICoordinateTransformation Members

	private String _AreaOfUse;
	/**
	 Human readable description of domain in source coordinate system.
	 */
	public final String getAreaOfUse()
	{
		return _AreaOfUse;
	}

	private String _Authority;
	/**
	 Authority which defined transformation and parameter values.


	 An Authority is an organization that maintains definitions of Authority Codes. For example the European Petroleum Survey Group (EPSG) maintains a database of coordinate systems, and other spatial referencing objects, where each object has a code number ID. For example, the EPSG code for a WGS84 Lat/Lon coordinate system is �4326�

	 */
	public final String getAuthority()
	{
		return _Authority;
	}

	private long _AuthorityCode;
	/**
	 Code used by authority to identify transformation. An empty string is used for no code.

	 The AuthorityCode is a compact string defined by an Authority to reference a particular spatial reference object. For example, the European Survey Group (EPSG) authority uses 32 bit integers to reference coordinate systems, so all their code strings will consist of a few digits. The EPSG code for WGS84 Lat/Lon is �4326�.
	 */
	public final long getAuthorityCode()
	{
		return _AuthorityCode;
	}

	private IMathTransform _MathTransform;
	/**
	 Gets math transform.
	 */
	public final IMathTransform getMathTransform()
	{
		return _MathTransform;
	}

	private String _Name;
	/**
	 Name of transformation.
	 */
	public final String getName()
	{
		return _Name;
	}

	private String _Remarks;
	/**
	 Gets the provider-supplied remarks.
	 */
	public final String getRemarks()
	{
		return _Remarks;
	}

	private ICoordinateSystem _SourceCS;
	/**
	 Source coordinate system.
	 */
	public final ICoordinateSystem getSourceCS()
	{
		return _SourceCS;
	}

	private ICoordinateSystem _TargetCS;
	/**
	 Target coordinate system.
	 */
	public final ICoordinateSystem getTargetCS()
	{
		return _TargetCS;
	}

	private TransformType _TransformType;
	/**
	 Semantic type of transform. For example, a datum transformation or a coordinate conversion.
	 */
	public final TransformType getTransformType()
	{
		return _TransformType;
	}

//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
	///#endregion
}