/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.terminals.interfaces;

/**
 * Terminals plug-in preference key definitions.
 */
public interface IPreferenceKeys {
	/**
	 * Preference keys family prefix.
	 */
	public final String PREF_TERMINAL = "terminals"; //$NON-NLS-1$

	/**
	 * Preference key: Remove terminated terminals when a new terminal is created.
	 */
	public final String PREF_REMOVE_TERMINATED_TERMINALS = PREF_TERMINAL + ".removeTerminatedTerminals"; //$NON-NLS-1$
}
