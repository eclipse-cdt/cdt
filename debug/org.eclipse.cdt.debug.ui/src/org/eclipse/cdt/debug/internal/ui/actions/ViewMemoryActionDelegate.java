/*******************************************************************************
 * Copyright (c) 2008 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ken Ryall (Nokia) - initial API and implementation (207231)
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.debug.core.model.ICVariable;
import org.eclipse.cdt.debug.internal.ui.CDebugUIUtils;
import org.eclipse.cdt.debug.internal.ui.views.memory.AddMemoryBlocks;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.memory.IMemoryRenderingSite;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;

public class ViewMemoryActionDelegate implements IObjectActionDelegate {

	private ICVariable[] variables;

	public ViewMemoryActionDelegate() {}

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}
	
	public void run(IAction action) {
		
		IWorkbenchPage page = CDebugUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IViewPart newView;
		try {
			newView = page.showView(IDebugUIConstants.ID_MEMORY_VIEW, null, IWorkbenchPage.VIEW_ACTIVATE);			
			IMemoryRenderingSite memSite = (IMemoryRenderingSite) newView;
			new AddMemoryBlocks().addMemoryBlocksForVariables(variables, memSite);
		} catch (ClassCastException e) {
			CDebugUIUtils.openError(ActionMessages.getString("ViewMemoryActionDelegate.ErrorTitle"), ActionMessages.getString("ViewMemoryActionDelegate.CantOpenMemoryView"), e); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (PartInitException e) {
			CDebugUIUtils.openError(ActionMessages.getString("ViewMemoryActionDelegate.ErrorTitle"), ActionMessages.getString("ViewMemoryActionDelegate.CantOpenMemoryView"), e); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (DebugException e) {
			CDebugUIUtils.openError(ActionMessages.getString("ViewMemoryActionDelegate.ErrorTitle"), ActionMessages.getString("ViewMemoryActionDelegate.CantViewMemory"), e); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	@SuppressWarnings("unchecked")
	public void selectionChanged(IAction action, ISelection selection) {
		if ( selection instanceof IStructuredSelection ) {
			List<Object> list = new ArrayList<Object>();
			IStructuredSelection ssel = (IStructuredSelection)selection;
			Iterator<Object> i = ssel.iterator();
			while( i.hasNext() ) {
				Object o = i.next();
				if ( o instanceof ICVariable ) {
					action.setEnabled( true );
					list.add( o );
				}
			}
			setVariables( list.toArray( new ICVariable[list.size()] ) );
		}
		else {
			action.setChecked( false );
			action.setEnabled( false );
		}
	}

	protected ICVariable[] getVariables() {
		return variables;
	}

	private void setVariables( ICVariable[] variables ) {
		this.variables = variables;
	}

}
