/*******************************************************************************
 * Copyright (c) 2004, 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   QNX Software Systems - Initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.core.sourcelookup;

import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.internal.core.ICDebugInternalConstants;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;

/**
 * Director of the common source containers.
 */
public class CommonSourceLookupDirector extends CSourceLookupDirector {
	@Override
	public void setSourceContainers(ISourceContainer[] containers) {
		try {
			super.setSourceContainers(containers);
			CDebugCorePlugin.getDefault().getPluginPreferences()
					.setValue(ICDebugInternalConstants.PREF_DEFAULT_SOURCE_CONTAINERS, getMemento());
			CDebugCorePlugin.getDefault().getPluginPreferences()
					.setValue(ICDebugInternalConstants.PREF_COMMON_SOURCE_CONTAINERS, ""); //$NON-NLS-1$
			CDebugCorePlugin.getDefault().savePluginPreferences();
		} catch (CoreException e) {
			CDebugCorePlugin.log(e.getStatus());
		}
	}
}
