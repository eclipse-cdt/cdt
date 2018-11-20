/*******************************************************************************
 * Copyright (c) 2009, 2013 Wind River Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Ericsson                    - Updated hover to use the new Details format
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui;

import org.eclipse.cdt.dsf.debug.ui.AbstractDsfDebugTextHover;
import org.eclipse.cdt.dsf.gdb.IGdbDebugPreferenceConstants;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunchDelegate;
import org.eclipse.cdt.dsf.mi.service.MIExpressions;
import org.eclipse.core.runtime.Platform;

/**
 * Debug editor text hover for GDB.
 *
 * @since 2.1
 */
public class GdbDebugTextHover extends AbstractDsfDebugTextHover {

	@Override
	protected String getModelId() {
		return GdbLaunchDelegate.GDB_DEBUG_MODEL_ID;
	}

	@Override
	protected String getHoverFormat() {
		return MIExpressions.DETAILS_FORMAT;
	}

	@Override
	protected boolean useExpressionExplorer() {
		// The preference is part of the GdbPlugin preference store
		// Bug 414622
		if (Platform.getPreferencesService().getBoolean(GdbPlugin.PLUGIN_ID,
				IGdbDebugPreferenceConstants.PREF_USE_INSPECTOR_HOVER, true, null)) {
			return true;
		}
		return false;
	}
}
