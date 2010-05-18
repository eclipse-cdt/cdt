/*******************************************************************************
 * Copyright (c) 2009,2010 QNX Software Systems
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    QNX Software Systems (Alena Laskavaia)  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.param;

/**
 * Value of the problem preference. If more than one it can be composite, i.e.
 * map
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
	 * @return
	 */
	String exportValue();

	/**
	 * Import value from string into internal object state.
	 * 
	 * @param str
	 *            - string from preferences, previously exported by exportValue
	 *            method.
	 */
	void importValue(String str);
}
