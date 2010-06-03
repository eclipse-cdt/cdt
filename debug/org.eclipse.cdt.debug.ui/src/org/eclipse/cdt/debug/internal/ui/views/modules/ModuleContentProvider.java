/*******************************************************************************
 * Copyright (c) 2007, 2008 ARM and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * ARM - Initial API and implementation
 * Wind River Systems - adapted to work with platform Modules view (bug 210558)
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.views.modules; 

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.IParent;
import org.eclipse.cdt.debug.core.model.ICModule;
import org.eclipse.cdt.debug.core.model.ICStackFrame;
import org.eclipse.cdt.debug.core.model.ICThread;
import org.eclipse.cdt.debug.core.model.IModuleRetrieval;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.internal.ui.model.elements.ElementContentProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;
import org.eclipse.debug.ui.IDebugUIConstants;

public class ModuleContentProvider extends ElementContentProvider {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.elements.ElementContentProvider#getChildCount(java.lang.Object, org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext, org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate)
	 */
	protected int getChildCount( Object element, IPresentationContext context, IViewerUpdate monitor ) throws CoreException {
		return getAllChildren( element, context ).length;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.elements.ElementContentProvider#getChildren(java.lang.Object, int, int, org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext, org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate)
	 */
	protected Object[] getChildren( Object parent, int index, int length, IPresentationContext context, IViewerUpdate monitor ) throws CoreException {
		return getElements( getAllChildren( parent, context ), index, length );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.elements.ElementContentProvider#supportsContextId(java.lang.String)
	 */
	protected boolean supportsContextId( String id ) {
		return IDebugUIConstants.ID_MODULE_VIEW.equals( id );
	}
	
	protected Object[] getAllChildren( Object parent, IPresentationContext context ) throws CoreException {			
		if ( parent instanceof IModuleRetrieval ) {
			return ((IModuleRetrieval)parent).getModules();
		}
		else if ( parent instanceof ICThread || parent instanceof ICStackFrame ) {
			IModuleRetrieval mr = (IModuleRetrieval)((IAdaptable)parent).getAdapter( IModuleRetrieval.class );
			if ( mr != null ) {
				return mr.getModules();
			}
		}
		else if ( parent instanceof ICModule ) {
			IBinary binary = (IBinary)((ICModule)parent).getAdapter( IBinary.class );
			if ( binary != null ) {
				try {
					return binary.getChildren();
				}
				catch( CModelException e ) {
				}
			}
		}
		else if ( parent instanceof IParent ) {
			try {
				return ((IParent)parent).getChildren();
			}
			catch( CModelException e ) {
			}
		}
		return EMPTY;
	}
}
