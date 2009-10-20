/*******************************************************************************
 * Copyright (c) 2006-2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ted R Williams (Wind River Systems, Inc.) - initial implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.ui.memory.transport.actions;

import org.eclipse.cdt.debug.ui.memory.transport.ImportMemoryDialog;
import org.eclipse.cdt.debug.ui.memory.transport.MemoryTransportPlugin;
import org.eclipse.debug.ui.memory.IMemoryRenderingSite;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

/**
 * Action for downloading memory.
 */
public class ImportMemoryAction implements IViewActionDelegate {


	private IMemoryRenderingSite fView;

	public void init(IViewPart view) {
		if (view instanceof IMemoryRenderingSite)
			fView = (IMemoryRenderingSite) view;
	}
	
	public void run(IAction action) {

		ISelection selection = fView.getSite().getSelectionProvider()
				.getSelection();
		
		// use utility function in export code
		ExportMemoryAction.BlockAndAddress blockAndAddr = ExportMemoryAction.getMemoryBlockAndInitialStartAddress(selection);
		if(blockAndAddr.block == null)
			return;
			
		ImportMemoryDialog dialog = new ImportMemoryDialog(MemoryTransportPlugin.getShell(), blockAndAddr.block, blockAndAddr.addr, fView);
		dialog.open();
		
		dialog.getResult();
	}
	
	public void selectionChanged(IAction action, ISelection selection) {
		// use utility function in export code		
		action.setEnabled(ExportMemoryAction.getMemoryBlockAndInitialStartAddress(selection).block != null);
	}

}
