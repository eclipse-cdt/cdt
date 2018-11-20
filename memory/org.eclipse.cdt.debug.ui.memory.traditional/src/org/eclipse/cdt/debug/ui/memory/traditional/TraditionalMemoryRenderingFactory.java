/*******************************************************************************
 * Copyright (c) 2016 Ericsson AB and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
