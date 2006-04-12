/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
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
 * {Name} (company) - description of contribution.
 ********************************************************************************/
package org.eclipse.rse.ui.internal;

import org.eclipse.rse.ui.IRSEUIRegistry;

public class RSEUIRegistry implements IRSEUIRegistry {

	// the singleton instance
	private static RSEUIRegistry instance;

	/**
	 * Constructor.
	 */
	public RSEUIRegistry() {
		super();
		init();
	}

	/**
	 * Initializes the registry. This should only be called from the constructor.
	 */
	private void init() {
	}

	/**
	 * Returns the singleton instance of the registry.
	 * @return the singleton instance
	 */
	public static final RSEUIRegistry getDefault() {

		if (instance == null) {
			instance = new RSEUIRegistry();
		}

		return instance;
	}
}