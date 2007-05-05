/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.dsf.debug.ui.viewmodel;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.dd.dsf.debug.service.IFormattedValues;
import org.eclipse.dd.dsf.ui.viewmodel.IVMAdapter;
import org.eclipse.dd.dsf.ui.viewmodel.IVMContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementContentProvider;
import org.eclipse.debug.ui.AbstractDebugView;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

/**
 * 
 */
public class SetDefaultFormatHex implements IViewActionDelegate {

    private IFormattedValueVMContext fFormattedValueVMC; 
    
    public void init(IViewPart view) {
    }

    public void run(IAction action) {
        if (fFormattedValueVMC != null) {
            fFormattedValueVMC.getPreferenceStore().setDefaultFormatId(IFormattedValues.HEX_FORMAT);
        }
    }

    public void selectionChanged(IAction action, ISelection selection) {
        fFormattedValueVMC = null;
        if (selection instanceof IStructuredSelection) {
            Object element = ((IStructuredSelection)selection).getFirstElement();
            if (element instanceof IFormattedValueVMContext) {
                fFormattedValueVMC = ((IFormattedValueVMContext)element);
            }
        }
        action.setEnabled(fFormattedValueVMC != null);
    }

}
