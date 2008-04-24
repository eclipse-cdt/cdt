/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir,
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson,
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 *
 * Contributors:
 * David Dykstal (IBM) - added javadoc, minor changes
 * David Dykstal (IBM) - [226561] Add API markup to RSE javadocs for extend / implement
 *******************************************************************************/

package org.eclipse.rse.core.model;

/**
 * The standard implementation of {@link IPropertyType}. Use the static factory
 * methods to return instances.
 * 
 * @noextend This class is not intended to be subclassed by clients.
 */
public class PropertyType implements IPropertyType {

	private int _type = 0;
	private String[] _enumValues;

	private static final String ENUMERATION_STR = "enumeration:"; //$NON-NLS-1$

	private static IPropertyType _booleanPropertyType = new PropertyType(TYPE_BOOLEAN);
	private static IPropertyType _integerPropertyType = new PropertyType(TYPE_INTEGER);
	private static IPropertyType _stringPropertyType = new PropertyType(TYPE_STRING);

	private PropertyType(int type) {
		_type = type;
	}

	/**
	 * Returns an instance of boolean property type.
	 * @return IPropertyType
	 */
	public static IPropertyType getBooleanPropertyType() {
		return _booleanPropertyType;
	}

	/**
	 * Returns an instance of integer property type.
	 * @return IPropertyType
	 */
	public static IPropertyType getIntegerPropertyType() {
		return _integerPropertyType;
	}

	/**
	 * Returns an instance of string property type.
	 * @return IPropertyType
	 */
	public static IPropertyType getStringPropertyType() {
		return _stringPropertyType;
	}

	/**
	 * Returns an instance of enum property type.
	 * @param values String[] array of allowed enumerator values.
	 * @return IPropertyType
	 */
	public static IPropertyType getEnumPropertyType(String[] values) {
		PropertyType type = new PropertyType(TYPE_ENUM);
		type._enumValues = values;
		return type;
	}

	/**
	 * Returns an instance of property type based on the String specification.
	 * This is the reverse of PropertyType.toString().
	 * @return IPropertyType instance based on String specification.
	 */
	public static IPropertyType fromString(String typeStr) {
		if (typeStr.equals(String.class.toString())) {
			return getStringPropertyType();
		} else if (typeStr.equals(Integer.class.toString())) {
			return getIntegerPropertyType();
		} else if (typeStr.startsWith(ENUMERATION_STR)) {
			String subString = typeStr.substring(ENUMERATION_STR.length());
			String[] enumValues = subString.split(","); //$NON-NLS-1$
			return getEnumPropertyType(enumValues);
		} else if (typeStr.equals(Boolean.class.toString())) {
			return getBooleanPropertyType();
		} else {
			return getStringPropertyType();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.model.IPropertyType#getType()
	 */
	public int getType() {
		return _type;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.model.IPropertyType#isString()
	 */
	public boolean isString() {
		return _type == TYPE_STRING;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.model.IPropertyType#isInteger()
	 */
	public boolean isInteger() {
		return _type == TYPE_INTEGER;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.model.IPropertyType#isEnum()
	 */
	public boolean isEnum() {
		return _type == TYPE_ENUM;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.model.IPropertyType#isBoolean()
	 */
	public boolean isBoolean() {
		return _type == TYPE_BOOLEAN;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.model.IPropertyType#getEnumValues()
	 */
	public String[] getEnumValues() {
		return _enumValues;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		if (isString()) {
			return String.class.getName();
		} else if (isInteger()) {
			return Integer.class.getName();
		} else if (isEnum()) {
			StringBuffer buf = new StringBuffer();
			buf.append(ENUMERATION_STR);
			String[] enumValues = getEnumValues();
			for (int i = 0; i < enumValues.length; i++) {
				buf.append(enumValues[i]);
				if (i + 1 < enumValues.length) {
					buf.append(","); //$NON-NLS-1$
				}
			}
			return buf.toString();
		} else if (isBoolean()) {
			return Boolean.class.getName();
		}
		return super.toString();
	}

}
