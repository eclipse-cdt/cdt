/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.remote.core;

/**
 * Preference constants that can be used to check the default remote service provider preference.
 *
 * @since 6.0
 */
public interface IRemotePreferenceConstants {
	/**
	 * Preference setting for the default connection type. Clients can check this preference to see if a default provider
	 * has been set, and if so, what the provider ID is.
	 *
	 * @since 2.0
	 */
	public static final String PREF_CONNECTION_TYPE_ID = "connectionTypeId"; //$NON-NLS-1$
}
