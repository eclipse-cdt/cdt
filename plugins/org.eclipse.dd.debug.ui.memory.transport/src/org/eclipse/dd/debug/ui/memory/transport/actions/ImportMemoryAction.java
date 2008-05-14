/*******************************************************************************
 * Copyright (c) 2006-2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ted R Williams (Wind River Systems, Inc.) - initial implementation
 *******************************************************************************/

package org.eclipse.dd.debug.ui.memory.transport.actions;

import org.eclipse.dd.debug.ui.memory.transport.ImportMemoryDialog;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.views.memory.MemoryView;
import org.eclipse.debug.internal.ui.views.memory.MemoryViewIdRegistry;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.memory.IMemoryRendering;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

/**
 * Action for downloading memory.
 */
public class ImportMemoryAction implements IViewActionDelegate {


	private MemoryView fView;

	public void init(IViewPart view) {
		if (view instanceof MemoryView)
			fView = (MemoryView) view;
	}

	public void run(IAction action) {

		ISelection selection = fView.getSite().getSelectionProvider()
				.getSelection();
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection strucSel = (IStructuredSelection) selection;

			// return if current selection is empty
			if (strucSel.isEmpty())
				return;

			Object obj = strucSel.getFirstElement();

			if (obj == null)
				return;

			IMemoryBlock memBlock = null;

			if (obj instanceof IMemoryRendering) {
				memBlock = ((IMemoryRendering) obj).getMemoryBlock();
			} else if (obj instanceof IMemoryBlock) {
				memBlock = (IMemoryBlock) obj;
			}
			if(memBlock == null)
				return;
			
			ImportMemoryDialog dialog = new ImportMemoryDialog(DebugUIPlugin.getShell(), memBlock);
			dialog.open();
			
			dialog.getResult();
		}

	}
	
	public void selectionChanged(IAction action, ISelection selection) {
		
	}

}
