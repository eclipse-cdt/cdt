/*******************************************************************************
 * Copyright (c) 2016 Ericsson AB and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Alvaro Sanchez-Leon (Ericsson AB) - First Implementation and API
 *******************************************************************************/

package org.eclipse.cdt.debug.ui.memory.traditional;

import org.eclipse.cdt.debug.ui.internal.MemorySpacePreferencesHelper;

/**
 * @since 1.4
 */
public class TraditionalMemoryRenderingFactory {
	private static IMemorySpacePreferencesHelper fMemSpaceHelper = null;
	private final static Object fLock = new Object();

	public static IMemorySpacePreferencesHelper getMemorySpacesPreferencesHelper() {
		synchronized (fLock) {
			if (fMemSpaceHelper == null) {
				fMemSpaceHelper = new MemorySpacePreferencesHelper();
			}
		}

		return fMemSpaceHelper;
	}

	public static void setMemorySpacesPreferencesHelper(IMemorySpacePreferencesHelper helper) {
		synchronized (fLock) {
			fMemSpaceHelper = helper;
		}
	}
}
