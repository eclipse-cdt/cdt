/********************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir,
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson,
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 *
 * Contributors:
 * David Dykstal (IBM) - added javadoc
 * David Dykstal (IBM) - [226561] Add API markup to RSE javadocs for extend / implement
 * Martin Oberhuber (Wind River) - [261486][api][cleanup] Mark @noimplement interfaces as @noextend
 ********************************************************************************/

package org.eclipse.rse.core.model;

/**
 * Property types are used to type instances of {@link IProperty}.
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients. Use
 *           {@link PropertyType} directly.
 */
public interface IPropertyType {

	public static final int TYPE_STRING = 0;
	public static final int TYPE_INTEGER = 1;
	public static final int TYPE_ENUM = 2;
	public static final int TYPE_BOOLEAN = 3;

	/**
	 * @return true if the property is of TYPE_STRING
	 */
	public boolean isString();

	/**
	 * @return true if the property is of TYPE_INTEGER
	 */
	public boolean isInteger();

	/**
	 * @return true if the property is of TYPE_ENUM
	 */
	public boolean isEnum();

	/**
	 * @return true if the property is of TYPE_BOOLEAN
	 */
	public boolean isBoolean();

	/**
	 * @return the integer value of the property type
	 */
	public int getType();

	/**
	 * @return the array of values that comprise the enumeration
	 */
	public String[] getEnumValues();

}