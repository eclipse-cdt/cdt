/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.internal.ui.callhierarchy;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.util.ExceptionHandler;

public class CallHierarchyUI {

	public static CHViewPart open(ICElement input, IWorkbenchWindow window) {
        if (input != null) {
        	return openInViewPart(window, input);
        }
        return null;
    }

    private static CHViewPart openInViewPart(IWorkbenchWindow window, ICElement input) {
        IWorkbenchPage page= window.getActivePage();
        try {
            CHViewPart result= (CHViewPart)page.showView(CUIPlugin.ID_CALL_HIERARCHY);
            result.setInput(input);
            return result;
        } catch (CoreException e) {
            ExceptionHandler.handle(e, window.getShell(), CHMessages.OpenCallHierarchyAction_label, null); 
        }
        return null;        
    }

}
