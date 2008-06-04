/********************************************************************************
 * Copyright (c) 2008 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight.
 *
 * Contributors:
 * David McKnight  (IBM)   [220123][dstore] Configurable timeout on irresponsiveness
 ********************************************************************************/
package org.eclipse.dstore.core.model;

/**
 * Classes that implement this and add themselves to the DataStore preference
 * listeners get called each time a preference is changed.
 * 
 * @since 3.0
 */
public interface IDataStorePreferenceListener {

	/**
	 * A DataStore preference has changed
	 * @param property the property that has changed
	 * @param value the value of the property
	 */
	public void preferenceChanged(String property, String value);

}
