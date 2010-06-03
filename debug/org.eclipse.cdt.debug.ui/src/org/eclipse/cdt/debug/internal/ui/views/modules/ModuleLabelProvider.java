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

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.debug.core.model.ICModule;
import org.eclipse.cdt.debug.internal.ui.CDebugImages;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.internal.ui.model.elements.ElementLabelProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.views.DebugModelPresentationContext;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * org.eclipse.cdt.debug.internal.ui.views.modules.CElementLabelProvider: 
 * //TODO Add description.
 */
public class ModuleLabelProvider extends ElementLabelProvider {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.elements.ElementLabelProvider#getLabel(org.eclipse.jface.viewers.TreePath, org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext, java.lang.String)
	 */
	protected String getLabel( TreePath elementPath, IPresentationContext presentationContext, String columnId ) throws CoreException {
		Object element = elementPath.getLastSegment();
		if ( element instanceof ICModule && presentationContext instanceof DebugModelPresentationContext ) {
			IDebugModelPresentation presentation = ((DebugModelPresentationContext)presentationContext).getModelPresentation();
			return presentation.getText( element );
		}
		if ( element instanceof IAdaptable ) {
			IWorkbenchAdapter adapter = (IWorkbenchAdapter)(((IAdaptable)element).getAdapter( IWorkbenchAdapter.class ));
			if ( adapter != null )
				return adapter.getLabel( element );
		}
		return ""; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.elements.ElementLabelProvider#getImageDescriptor(org.eclipse.jface.viewers.TreePath, org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext, java.lang.String)
	 */
	protected ImageDescriptor getImageDescriptor( TreePath elementPath, IPresentationContext presentationContext, String columnId ) throws CoreException {
		Object element = elementPath.getLastSegment();
		if ( element instanceof ICModule ) {
			ICModule module = (ICModule)element;
			switch( module.getType() ) {
				case ICModule.EXECUTABLE:
					if ( module.areSymbolsLoaded() ) {
						return CDebugImages.DESC_OBJS_EXECUTABLE_WITH_SYMBOLS;
					}
					return CDebugImages.DESC_OBJS_EXECUTABLE;
				case ICModule.SHARED_LIBRARY:
					if ( module.areSymbolsLoaded() ) {
						return CDebugImages.DESC_OBJS_SHARED_LIBRARY_WITH_SYMBOLS;
					}
					return CDebugImages.DESC_OBJS_SHARED_LIBRARY;
			}
		}
		if ( element instanceof ICElement ) {
			IWorkbenchAdapter adapter = (IWorkbenchAdapter)(((IAdaptable)element).getAdapter( IWorkbenchAdapter.class ));
			if ( adapter != null )
				return adapter.getImageDescriptor( element );
		}
		return super.getImageDescriptor( elementPath, presentationContext, columnId );
	}
}
