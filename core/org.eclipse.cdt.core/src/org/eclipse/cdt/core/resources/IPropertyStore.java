package org.eclipse.cdt.core.resources;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

public interface IPropertyStore {
	public static final boolean BOOLEAN_DEFAULT_DEFAULT = false;
	public static final double DOUBLE_DEFAULT_DEFAULT = 0.0;
	public static final float FLOAT_DEFAULT_DEFAULT = 0.0f;
	public static final int INT_DEFAULT_DEFAULT = 0;
	public static final long LONG_DEFAULT_DEFAULT = 0L;
	public static final String STRING_DEFAULT_DEFAULT = new String();
	
	public static final String TRUE = "true";
	public static final String FALSE = "false";
	
	String getString(String name);
	void setDefault(String name, String def);
	void putValue(String name, String value);

}

