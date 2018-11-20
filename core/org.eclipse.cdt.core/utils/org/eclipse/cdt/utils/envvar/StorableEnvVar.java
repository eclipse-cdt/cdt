/*******************************************************************************
 * Copyright (c) 2005, 2011 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Intel Corporation - Initial API and implementation
 *    James Blackburn (Broadcom Corp.)
 *    IBM Corporation
 *******************************************************************************/
package org.eclipse.cdt.utils.envvar;

import org.eclipse.cdt.core.envvar.EnvironmentVariable;
import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.cdt.internal.core.SafeStringInterner;
import org.osgi.service.prefs.Preferences;

/**
 * This class represents the Environment variable that could be loaded
 * and stored in XML
 *
 * @since 3.0
 */
public class StorableEnvVar extends EnvironmentVariable {
	public static final String VARIABLE_ELEMENT_NAME = "variable"; //$NON-NLS-1$
	public static final String NAME = "name"; //$NON-NLS-1$
	public static final String VALUE = "value"; //$NON-NLS-1$
	public static final String OPERATION = "operation"; //$NON-NLS-1$
	public static final String DELIMITER = "delimiter"; //$NON-NLS-1$

	public static final String REPLACE = "replace"; //$NON-NLS-1$
	public static final String REMOVE = "remove"; //$NON-NLS-1$
	public static final String APPEND = "append"; //$NON-NLS-1$
	public static final String PREPEND = "prepend"; //$NON-NLS-1$

	public StorableEnvVar(String name, String value, int op, String delimiter) {
		super(name, value, op, delimiter);
	}

	public StorableEnvVar(String name) {
		this(name, null, ENVVAR_REPLACE, null);
	}

	public StorableEnvVar(String name, String value) {
		this(name, value, ENVVAR_REPLACE, null);
	}

	public StorableEnvVar(String name, String value, String delimiter) {
		this(name, value, ENVVAR_REPLACE, delimiter);
	}

	/**
	 * Load the environment variable from the ICStorageElement
	 * @param element
	 */
	public StorableEnvVar(ICStorageElement element) {
		fName = SafeStringInterner.safeIntern(element.getAttribute(NAME));

		fValue = SafeStringInterner.safeIntern(element.getAttribute(VALUE));

		fOperation = opStringToInt(element.getAttribute(OPERATION));

		fDelimiter = element.getAttribute(DELIMITER);
		if ("".equals(fDelimiter)) //$NON-NLS-1$
			fDelimiter = null;
	}

	/**
	 * Load the Environment Variable directly from a Preference element
	 * @param name
	 * @param element
	 * @since 5.2
	 */
	public StorableEnvVar(String name, Preferences element) {
		fName = SafeStringInterner.safeIntern(name);
		fValue = SafeStringInterner.safeIntern(element.get(VALUE, null));
		fOperation = opStringToInt(element.get(OPERATION, null));
		fDelimiter = element.get(DELIMITER, null);
	}

	private int opStringToInt(String op) {
		int operation;

		if (REMOVE.equals(op))
			operation = ENVVAR_REMOVE;
		else if (APPEND.equals(op))
			operation = ENVVAR_APPEND;
		else if (PREPEND.equals(op))
			operation = ENVVAR_PREPEND;
		else
			operation = ENVVAR_REPLACE;

		return operation;
	}

	private String opIntToString(int op) {
		String operation;

		if (ENVVAR_REMOVE == op)
			operation = REMOVE;
		else if (ENVVAR_APPEND == op)
			operation = APPEND;
		else if (ENVVAR_PREPEND == op)
			operation = PREPEND;
		else
			operation = REPLACE;

		return operation;
	}

	public void serialize(ICStorageElement element) {
		if (fName != null)
			element.setAttribute(NAME, fName);

		if (fValue != null)
			element.setAttribute(VALUE, fValue);

		element.setAttribute(OPERATION, opIntToString(fOperation));

		if (fDelimiter != null)
			element.setAttribute(DELIMITER, fDelimiter);
	}

	/**
	 * Serialize this Preference straight into the Preferences element.
	 * It's assumed that the Preference node represents this StorableEnvVar's name
	 * @param element
	 * @since 5.2
	 */
	public void serialize(Preferences element) {
		if (fValue != null)
			element.put(VALUE, fValue);

		element.put(OPERATION, opIntToString(fOperation));

		if (fDelimiter != null)
			element.put(DELIMITER, fDelimiter);
	}
}
