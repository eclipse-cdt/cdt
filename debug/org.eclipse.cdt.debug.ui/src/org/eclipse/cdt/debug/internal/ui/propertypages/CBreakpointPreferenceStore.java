/*******************************************************************************
 * Copyright (c) 2000, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     QNX Software Systems - Refactored to use platform implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.propertypages;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceStore;

/**
 * A preference store that presents the state of the properties of a C/C++ breakpoint. 
 */
public class CBreakpointPreferenceStore extends PreferenceStore implements IPreferenceStore {

	protected final static String ENABLED = "ENABLED"; //$NON-NLS-1$

	protected final static String CONDITION = "CONDITION"; //$NON-NLS-1$

	protected final static String IGNORE_COUNT = "IGNORE_COUNT"; //$NON-NLS-1$

	protected final static String LINE = "LINE"; //$NON-NLS-1$

	/**
	 * Constructor for CBreakpointPreferenceStore.
	 */
	public CBreakpointPreferenceStore() {

	}

	/**
	 * Override to not save. 
	 * This store used for temporary breakpoint setting in dialogs 
	 * and does not require permanent storage.
	 */
	@Override
	public boolean needsSaving() {
		return false;
	}
}
