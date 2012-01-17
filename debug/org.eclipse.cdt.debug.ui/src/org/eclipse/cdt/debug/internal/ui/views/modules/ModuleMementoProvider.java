/*******************************************************************************
 * Copyright (c) 2007 ARM and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * ARM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.views.modules;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.debug.core.CDIDebugModel;
import org.eclipse.cdt.debug.core.model.ICDebugTarget;
import org.eclipse.cdt.debug.core.model.ICModule;
import org.eclipse.cdt.debug.core.model.ICStackFrame;
import org.eclipse.cdt.debug.core.model.ICThread;
import org.eclipse.cdt.debug.core.model.IModuleRetrieval;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.internal.ui.model.elements.ElementMementoProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.ui.IMemento;

/**
 * org.eclipse.cdt.debug.internal.ui.views.modules.ModuleMementoProvider: 
 * //TODO Add description.
 */
public class ModuleMementoProvider extends ElementMementoProvider {

	/**
	 * memento attribute
	 */
	private static final String ELEMENT_NAME = "ELEMENT_NAME"; //$NON-NLS-1$

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.elements.ElementMementoProvider#encodeElement(java.lang.Object, org.eclipse.ui.IMemento, org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext)
	 */
	@Override
	protected boolean encodeElement( Object element, IMemento memento, IPresentationContext context ) throws CoreException {
		if ( element instanceof ICDebugTarget || element instanceof ICThread || element instanceof ICStackFrame ) {
			IModuleRetrieval mr = (IModuleRetrieval)((IAdaptable)element).getAdapter( IModuleRetrieval.class );
			if ( mr != null ) {
				memento.putString( ELEMENT_NAME, mr.toString() );
			}
			else {
				// shouldn't happen
				memento.putString( ELEMENT_NAME, CDIDebugModel.getPluginIdentifier() );
			}
		}
		else if ( element instanceof ICModule ) {
			memento.putString( ELEMENT_NAME, ((ICModule)element).getName() );
		}
		else if ( element instanceof ICElement ) {
			memento.putString( ELEMENT_NAME, ((ICElement)element).getElementName() );
		}
		else {
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.elements.ElementMementoProvider#isEqual(java.lang.Object, org.eclipse.ui.IMemento, org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext)
	 */
	@Override
	protected boolean isEqual( Object element, IMemento memento, IPresentationContext context ) throws CoreException {
		String mementoName = memento.getString( ELEMENT_NAME );
		if ( mementoName != null ) {
			String elementName = null;
			if ( element instanceof ICDebugTarget || element instanceof ICThread || element instanceof ICStackFrame ) {
				IModuleRetrieval mr = (IModuleRetrieval)((IAdaptable)element).getAdapter( IModuleRetrieval.class );
				elementName = ( mr != null ) ? mr.toString() : CDIDebugModel.getPluginIdentifier();
			}
			else if ( element instanceof ICModule ) {
				elementName = ((ICModule)element).getName();
			}
			else if ( element instanceof ICElement ) {
				elementName = ((ICElement)element).getElementName();
			}
			if ( elementName != null ) {
				return elementName.equals( mementoName );
			}
		}
		return false;
	}
}
