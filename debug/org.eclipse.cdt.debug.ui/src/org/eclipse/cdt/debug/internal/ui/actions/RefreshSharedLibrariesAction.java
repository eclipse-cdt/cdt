/**********************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/ 
package org.eclipse.cdt.debug.internal.ui.actions; 

import org.eclipse.cdt.debug.core.ICSharedLibraryManager;
import org.eclipse.cdt.debug.core.ICUpdateManager;
import org.eclipse.debug.core.model.IDebugElement;
 
/**
 * A delegate for the "Refresh" action of the Shared Libraries view.
 */
public class RefreshSharedLibrariesAction extends AbstractRefreshActionDelegate {

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.actions.RefreshActionDelegate#getUpdateManager(java.lang.Object)
	 */
	protected ICUpdateManager getUpdateManager( Object element ) {
		if ( element instanceof IDebugElement ) {
			return (ICUpdateManager)((IDebugElement)element).getDebugTarget().getAdapter( ICSharedLibraryManager.class );
		}
		return null;
	}
}
