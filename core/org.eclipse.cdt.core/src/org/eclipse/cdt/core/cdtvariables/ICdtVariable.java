/*******************************************************************************
 * Copyright (c) 2005, 2009 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.cdtvariables;

/**
 * This interface represents the given build macro
 * Clients may implement or extend this interface.

 * @since 3.0
 */
public interface ICdtVariable {
	/**
	 * can hold any text string
	 */
	public static final int VALUE_TEXT = 1;

	/**
	 * can hold the array of text string values
	 */
	public static final int VALUE_TEXT_LIST = 2;

	/**
	 * can hold file path
	 */
	public static final int VALUE_PATH_FILE = 3;

	/**
	 * can hold the array of file path values
	 */
	public static final int VALUE_PATH_FILE_LIST = 4;

	/**
	 * can hold dir path
	 */
	public static final int VALUE_PATH_DIR = 5;

	/**
	 * can hold the array of dir path values
	 */
	public static final int VALUE_PATH_DIR_LIST = 6;

	/**
	 * can hold both file and dir path
	 */
	public static final int VALUE_PATH_ANY = 7;

	/**
	 * can hold the array of  PATH_ANY values
	 */
	public static final int VALUE_PATH_ANY_LIST = 8;

	/**
	 * Returns the macro name
	 */
	String getName();

	/**
	 * @return IBuildMacro.VALUE_xxx
	 */
	int getValueType();

	/**
	 * @throws CdtVariableException if macro holds StringList-type value
	 */
	String getStringValue() throws CdtVariableException;

	/**
	 * @throws CdtVariableException if macro holds single String-type value
	 */
	String[] getStringListValue() throws CdtVariableException;
}
