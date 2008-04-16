/********************************************************************************
 * Copyright (c) 2008 MontaVista Software, Inc.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Yu-Fen Kuo      (MontaVista) - initial API and implementation
 * Anna Dushistova (MontaVista) - initial API and implementation
 ********************************************************************************/
package org.eclipse.rse.internal.terminals.ui.views;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.rse.core.subsystems.ISystemDragDropAdapter;
import org.eclipse.rse.subsystems.terminals.core.elements.TerminalElement;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.rse.ui.view.ISystemRemoteElementAdapter;
import org.eclipse.rse.ui.view.ISystemViewElementAdapter;
import org.eclipse.ui.IActionFilter;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.views.properties.IPropertySource;

public class TerminalViewElementsAdapterFactory implements IAdapterFactory {

    private TerminalViewElementAdapter elementAdapter = new TerminalViewElementAdapter();

    public Object getAdapter(Object adaptableObject, Class adapterType) {
        Object adapter = null;
        if (adaptableObject instanceof TerminalElement)
            adapter = elementAdapter;

        if ((adapter != null) && (adapterType == IPropertySource.class)) {
            ((ISystemViewElementAdapter) adapter)
                    .setPropertySourceInput(adaptableObject);
        } else if (adapter == null) {
            SystemBasePlugin
                    .logWarning("No adapter found for object of type: " + adaptableObject.getClass().getName()); //$NON-NLS-1$
        }
        return adapter;
    }

    public Class[] getAdapterList() {
        return new Class[] { ISystemViewElementAdapter.class,
                ISystemDragDropAdapter.class,
                ISystemRemoteElementAdapter.class, IPropertySource.class,
                IWorkbenchAdapter.class, IActionFilter.class };
    }

}
