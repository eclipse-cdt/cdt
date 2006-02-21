/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.views.modules; 

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.IParent;
import org.eclipse.cdt.debug.core.model.ICModule;
import org.eclipse.cdt.debug.core.model.IModuleRetrieval;
import org.eclipse.cdt.debug.ui.ICDebugUIConstants;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.internal.ui.viewers.provisional.AsynchronousContentAdapter;
import org.eclipse.debug.internal.ui.viewers.provisional.IPresentationContext;
 
/**
 * Comment for .
 */
public class ModuleContentAdapter extends AsynchronousContentAdapter {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.provisional.AsynchronousContentAdapter#getChildren(java.lang.Object, org.eclipse.debug.internal.ui.viewers.provisional.IPresentationContext)
	 */
	protected Object[] getChildren( Object parent, IPresentationContext context ) throws CoreException {
		if ( parent instanceof IModuleRetrieval ) {
			return ((IModuleRetrieval)parent).getModules();
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

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.AsynchronousTreeContentAdapter#hasChildren(java.lang.Object, org.eclipse.debug.internal.ui.viewers.IPresentationContext)
	 */
	protected boolean hasChildren( Object element, IPresentationContext context ) throws CoreException {
		if ( element instanceof IModuleRetrieval ) {
			return ((IModuleRetrieval)element).hasModules();
		}
		else if ( element instanceof ICModule ) {
			IBinary binary = (IBinary)((ICModule)element).getAdapter( IBinary.class );
			if ( binary != null ) {
				return binary.hasChildren();
			}
		}
		else if ( element instanceof IParent ) {
			return ((IParent)element).hasChildren();
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.AsynchronousTreeContentAdapter#supportsPartId(java.lang.String)
	 */
	protected boolean supportsPartId( String id ) {
		return ICDebugUIConstants.ID_MODULES_VIEW.equals( id );
	}
}
