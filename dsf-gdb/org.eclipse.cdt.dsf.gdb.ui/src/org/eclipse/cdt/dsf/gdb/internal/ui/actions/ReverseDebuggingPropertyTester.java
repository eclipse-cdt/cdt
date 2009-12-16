/*******************************************************************************
 * Copyright (c) 2009 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.actions;

import org.eclipse.cdt.debug.core.model.IReverseToggleHandler;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.contexts.IDebugContextService;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Property tester for reverse debugging information available through the given 
 * object.  The object being tested could either be a {@link IWorkbenchPart} or
 * a {@link IDMVMContext}.
 * <p>
 * One property is supported:
 * <ul>
 * <li> "isReverseDebuggingEnabled" - Checks whether reverse debugging is currently 
 * enabled given the receiver.</li>
 * </ul>
 * </p>
 */
public class ReverseDebuggingPropertyTester extends PropertyTester {

    private static final String ENABLED = "isReverseDebuggingEnabled"; //$NON-NLS-1$

    public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
    	if (ENABLED.equals(property)) {
    		if (receiver instanceof IWorkbenchPart) {
    			Object selection = getContextSelectionForPart((IWorkbenchPart)receiver);
    			if (selection instanceof IDMVMContext) {
    				return test((IDMVMContext)selection);
    			}
    		} else if (receiver instanceof IDMVMContext) {
				return test((IDMVMContext)receiver);
    		}
    	}
    	return false;
    }   
    
    private boolean test(IDMVMContext context) {
    	boolean result = false;
		ICommandControlDMContext controlDmc = DMContexts.getAncestorOfType(context.getDMContext(), ICommandControlDMContext.class);
		if (controlDmc != null) {
			IReverseToggleHandler toggle = (IReverseToggleHandler)(controlDmc.getAdapter(IReverseToggleHandler.class));
			if (toggle != null) {
				result = toggle.isReverseToggled(controlDmc);
			}
		}
		return result;
    }

    private static Object getContextSelectionForPart(IWorkbenchPart part) {
    	IDebugContextService contextService = 
    		DebugUITools.getDebugContextManager().getContextService(part.getSite().getWorkbenchWindow());

    	ISelection debugContext = contextService.getActiveContext(getPartId(part));
    	if (debugContext == null) {
    		debugContext = contextService.getActiveContext();
    	}

    	if (debugContext instanceof IStructuredSelection) {
    		return ((IStructuredSelection)debugContext).getFirstElement();
    	}
    	
    	return null;
    }
    private static String getPartId(IWorkbenchPart part) {
        if (part instanceof IViewPart) {
            IViewSite site = (IViewSite)part.getSite();
            return site.getId() + (site.getSecondaryId() != null ? (":" + site.getSecondaryId()) : "");  //$NON-NLS-1$ //$NON-NLS-2$
        } else {
            return part.getSite().getId();
        }
    }

}
