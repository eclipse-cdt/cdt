/*******************************************************************************
 * Copyright (c) 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.internal.ui.commands;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.debug.core.model.ICVariable;
import org.eclipse.cdt.debug.internal.ui.views.memory.AddMemoryBlocks;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.memory.IMemoryRenderingSite;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Handler for viewing variable in memory view command, based on 
 * org.eclipse.cdt.debug.internal.ui.actions.ViewMemoryActionDelegate
 *
 */
public class ViewMemoryHandler extends AbstractHandler {

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection instanceof IStructuredSelection) {
			List<Object> list = new ArrayList<Object>();
			Iterator<?> iter = ((IStructuredSelection)selection).iterator();
            while (iter.hasNext()) {
            	Object obj = iter.next();
            	if (obj instanceof ICVariable) {
            		list.add(obj);
            	}
            }
            ICVariable[] variables = list.toArray(new ICVariable[list.size()]);
            showInMemoryView(variables);
		} 
		return null;
	}

	private void showInMemoryView(ICVariable[] variables) {
		try {
			IWorkbenchPage page = CDebugUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
			IViewPart newView = page.showView(IDebugUIConstants.ID_MEMORY_VIEW, null, IWorkbenchPage.VIEW_ACTIVATE);			
			IMemoryRenderingSite memSite = (IMemoryRenderingSite) newView;
			new AddMemoryBlocks().addMemoryBlocksForVariables(variables, memSite);
		} catch (ClassCastException e) {
			CDebugUIPlugin.log(e);
		} catch (PartInitException e) {
			CDebugUIPlugin.log(e);
		} catch (DebugException e) {
			CDebugUIPlugin.log(e);
		}
	}
}
