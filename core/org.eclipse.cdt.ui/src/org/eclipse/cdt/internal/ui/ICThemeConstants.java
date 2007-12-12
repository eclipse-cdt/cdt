/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;

/**
 * Defines the constants used in the <code>org.eclipse.ui.themes</code>
 * extension contributed by this plug-in.
 */
public interface ICThemeConstants {
	String ID_PREFIX= CUIPlugin.PLUGIN_ID + "."; //$NON-NLS-1$

	/**
	 * A theme constant that holds the background color used in the code assist selection dialog.
	 */
	public final String CODEASSIST_PROPOSALS_BACKGROUND= ID_PREFIX + PreferenceConstants.CODEASSIST_PROPOSALS_BACKGROUND;

	/**
	 * A theme constant that holds the foreground color used in the code assist selection dialog.
	 */
	public final String CODEASSIST_PROPOSALS_FOREGROUND= ID_PREFIX + PreferenceConstants.CODEASSIST_PROPOSALS_FOREGROUND;

	/**
	 * A theme constant that holds the background color used for parameter hints.
	 */
	public final String CODEASSIST_PARAMETERS_BACKGROUND= ID_PREFIX + PreferenceConstants.CODEASSIST_PARAMETERS_BACKGROUND;

	/**
	 * A theme constant that holds the foreground color used for parameter hints.
	 */
	public final String CODEASSIST_PARAMETERS_FOREGROUND= ID_PREFIX + PreferenceConstants.CODEASSIST_PARAMETERS_FOREGROUND;

}
