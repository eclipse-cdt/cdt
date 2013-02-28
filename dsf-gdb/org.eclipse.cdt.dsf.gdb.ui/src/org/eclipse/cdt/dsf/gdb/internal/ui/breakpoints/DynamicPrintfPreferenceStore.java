/*******************************************************************************
 * Copyright (c) 2010 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.breakpoints;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceStore;

/**
 * A preference store that presents the state of the properties of a C/C++ DynamicPrintf. 
 */
public class DynamicPrintfPreferenceStore extends PreferenceStore implements IPreferenceStore {

	protected final static String ENABLED = "ENABLED"; //$NON-NLS-1$

	protected final static String CONDITION = "CONDITION"; //$NON-NLS-1$

	protected final static String IGNORE_COUNT = "IGNORE_COUNT"; //$NON-NLS-1$

	protected final static String MESSAGE = "MESSAGE"; //$NON-NLS-1$

	protected final static String LINE = "LINE"; //$NON-NLS-1$

	public DynamicPrintfPreferenceStore() {

	}

	/**
	 * Override to not save. 
	 * This store used for temporary runtimePrint setting in dialogs 
	 * and does not require permanent storage.
	 */
	@Override
	public boolean needsSaving() {
		return false;
	}
}
