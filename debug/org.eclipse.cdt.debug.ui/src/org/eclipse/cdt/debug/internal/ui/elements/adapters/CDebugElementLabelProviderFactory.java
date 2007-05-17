/*******************************************************************************
 * Copyright (c) 2007 ARM Limited and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     ARM Limited - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.elements.adapters;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.debug.core.model.ICModule;
import org.eclipse.cdt.debug.core.model.ICVariable;
import org.eclipse.cdt.debug.internal.ui.views.modules.ModuleLabelProvider;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementLabelProvider;

/**
 * Factory for all (non-default) label providers
 */
public class CDebugElementLabelProviderFactory implements IAdapterFactory {

	private static IElementLabelProvider fgModuleLabelProvider = new ModuleLabelProvider();
	private static IElementLabelProvider fgVariableLabelProvider = new CVariableLabelProvider();

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
	 */
	public Object getAdapter( Object adaptableObject, Class adapterType ) {
		if ( adapterType.equals( IElementLabelProvider.class ) ) {
			if ( adaptableObject instanceof ICModule ) {
				return fgModuleLabelProvider;
			}
			if ( adaptableObject instanceof ICElement ) {
				return fgModuleLabelProvider;
			}
			if ( adaptableObject instanceof ICVariable ) {
				return fgVariableLabelProvider;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
	 */
	public Class[] getAdapterList() {
		return new Class[] {
				IElementLabelProvider.class,
			};
	}
}
