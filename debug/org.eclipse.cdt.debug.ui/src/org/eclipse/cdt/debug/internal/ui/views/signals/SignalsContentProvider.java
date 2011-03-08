/*******************************************************************************
 * Copyright (c) 2004, 2011 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Wind River Systems - flexible hierarchy Signals view (bug 338908)
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.views.signals; 

import org.eclipse.cdt.debug.core.model.ICDebugTarget;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.cdt.debug.ui.ICDebugUIConstants;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.internal.ui.model.elements.ElementContentProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;

public class SignalsContentProvider extends ElementContentProvider {

	@Override
	protected int getChildCount( Object element, IPresentationContext context, IViewerUpdate monitor ) throws CoreException {
		return getAllChildren( element, context ).length;
	}

	@Override
	protected Object[] getChildren( Object parent, int index, int length, IPresentationContext context, IViewerUpdate monitor ) throws CoreException {
		return getElements( getAllChildren( parent, context ), index, length );
	}

	@Override
	protected boolean supportsContextId( String id ) {
		return ICDebugUIConstants.ID_SIGNALS_VIEW.equals( id );
	}
	
	protected Object[] getAllChildren(Object parent, IPresentationContext context) throws CoreException {
		if (parent instanceof ICDebugTarget) {
			ICDebugTarget target = (ICDebugTarget) parent;
			try {
				Object[] signals = target.getSignals();
				if (signals != null)
					return signals;
			} catch (DebugException e) {
				CDebugUIPlugin.log(e);
			}
		}
		return EMPTY;
	}
}
