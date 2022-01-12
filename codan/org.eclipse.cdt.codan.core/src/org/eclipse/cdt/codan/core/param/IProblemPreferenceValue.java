/*******************************************************************************
 * Copyright (c) 2009,2010 QNX Software Systems
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    QNX Software Systems (Alena Laskavaia)  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.param;

/**
 * Value of the problem preference. If more than one it can be composite, i.e.
 * map.Extend {@link AbstractProblemPreference} class
 * to implement this interface.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IProblemPreferenceValue extends Cloneable {
	/**
	 * Get value of preference.
	 *
	 * @return object that represents the value. Limited number of object types
	 *         are allowed.
	 * @see IProblemPreferenceDescriptor.PreferenceType
	 */
	Object getValue();

	/**
	 * Set value of preference represented by this object.
	 *
	 * @param value
	 */
	void setValue(Object value);

	/**
	 * Export value in string representation required for storing in eclipse
	 * preferences.
	 *
	 * @return string representation of the value
	 */
	String exportValue();

	/**
	 * Import value from string into internal object state.
	 *
	 * @param str
	 *        - string from preferences, previously exported by exportValue
	 *        method.
	 */
	void importValue(String str);
}
