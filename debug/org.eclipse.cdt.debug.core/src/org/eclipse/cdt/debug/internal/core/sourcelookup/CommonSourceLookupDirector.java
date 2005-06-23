/*******************************************************************************
 * Copyright (c) 2004, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
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

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.sourcelookup.AbstractSourceLookupDirector#setSourceContainers(org.eclipse.debug.core.sourcelookup.ISourceContainer[])
	 */
	public void setSourceContainers( ISourceContainer[] containers ) {
		try {
			super.setSourceContainers( containers );
			CDebugCorePlugin.getDefault().getPluginPreferences().setValue( ICDebugInternalConstants.PREF_COMMON_SOURCE_CONTAINERS, getMemento() );
			CDebugCorePlugin.getDefault().savePluginPreferences();
		}
		catch( CoreException e ) {
			CDebugCorePlugin.log( e.getStatus() );
		}
	}
}
