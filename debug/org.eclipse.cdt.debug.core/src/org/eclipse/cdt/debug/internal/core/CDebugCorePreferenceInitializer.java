/*******************************************************************************
 * Copyright (c) 2004, 2012 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * Ken Ryall (Nokia) - 207675
 * Mathias Kunter - Using adequate default charsets (bug 370462)
*******************************************************************************/
package org.eclipse.cdt.debug.internal.core; 

import java.nio.charset.Charset;

import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.ICDebugConstants;
import org.eclipse.cdt.debug.core.cdi.ICDIFormat;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
 
/**
 * Default preference value initializer for <code>CDebugCorePlugin</code>.
 */
public class CDebugCorePreferenceInitializer extends AbstractPreferenceInitializer {

	/** 
	 * Constructor for CDebugCorePreferenceInitializer. 
	 */
	public CDebugCorePreferenceInitializer() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	@Override
	public void initializeDefaultPreferences() {
		IEclipsePreferences defaultPreferences = DefaultScope.INSTANCE.getNode(CDebugCorePlugin.PLUGIN_ID);
		
		defaultPreferences.putInt(ICDebugConstants.PREF_MAX_NUMBER_OF_INSTRUCTIONS, ICDebugConstants.DEF_NUMBER_OF_INSTRUCTIONS);
		defaultPreferences.putInt(ICDebugConstants.PREF_DEFAULT_VARIABLE_FORMAT, ICDIFormat.NATURAL);
		defaultPreferences.putInt(ICDebugConstants.PREF_DEFAULT_EXPRESSION_FORMAT, ICDIFormat.NATURAL);
		defaultPreferences.putInt(ICDebugConstants.PREF_DEFAULT_REGISTER_FORMAT, ICDIFormat.NATURAL);
		defaultPreferences.put(ICDebugConstants.PREF_DEBUG_CHARSET, Charset.defaultCharset().name());
		if (Platform.getOS().equals(Platform.OS_WIN32)) {
			defaultPreferences.put(ICDebugConstants.PREF_DEBUG_WIDE_CHARSET, "UTF-16"); //$NON-NLS-1$
		} else {
			defaultPreferences.put(ICDebugConstants.PREF_DEBUG_WIDE_CHARSET, "UTF-32"); //$NON-NLS-1$
		}
		defaultPreferences.putBoolean(ICDebugConstants.PREF_INSTRUCTION_STEP_MODE_ON, false);
	}
}
