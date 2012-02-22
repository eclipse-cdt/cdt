/*******************************************************************************
 * Copyright (c) 2007, 2008 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nokia - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.actions.breakpoints;


import java.math.BigInteger;

import org.eclipse.cdt.debug.internal.core.model.CMemoryBlockExtension;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IMemoryBlockExtension;
import org.eclipse.debug.core.model.MemoryByte;
import org.eclipse.debug.ui.memory.IRepositionableMemoryRendering;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;


public class AddWatchpointOnMemoryActionDelegate extends AddWatchpointActionDelegate {

	/**
	 * Constructor for Action1.
	 */
	public AddWatchpointOnMemoryActionDelegate() {
		super();
	}

	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	@Override
	public void run(IAction action) {
		IStructuredSelection selection = getSelection();
		
		if (selection == null || selection.isEmpty()) {
			return;
		}
		
		Object obj = selection.getFirstElement();
		if (obj != null) {
			String memorySpace = null;
			String address = ""; //$NON-NLS-1$
			String range = "1"; //$NON-NLS-1$
			
			if (obj instanceof IAdaptable) {
				IRepositionableMemoryRendering rendering = (IRepositionableMemoryRendering) ((IAdaptable)obj).getAdapter(IRepositionableMemoryRendering.class);
				if (rendering != null) {
					int addressableSize = 1;
					IMemoryBlock memblock = rendering.getMemoryBlock();
					if (memblock instanceof IMemoryBlockExtension) {
						try {
							addressableSize = ((IMemoryBlockExtension)memblock).getAddressableSize();
						} catch (DebugException e) {
							CDebugUIPlugin.log(e);
						}
					}
					
					memorySpace = getMemorySpace(rendering.getMemoryBlock(), memorySpace);
					address = getSelectedAddress(rendering.getSelectedAddress(), address);
					range = getRange(rendering.getSelectedAsBytes(), addressableSize, range);
				}
			}
			
			AddWatchpointDialog dlg = new AddWatchpointDialog(CDebugUIPlugin.getActiveWorkbenchShell(), 
					getMemorySpaceManagement());
			dlg.initializeMemorySpace(memorySpace);
			dlg.setExpression(address);
			dlg.initializeRange(true, range);
			 
			if (dlg.open() == Window.OK) {
				addWatchpoint(dlg.getWriteAccess(), dlg.getReadAccess(), dlg.getExpression(), dlg.getMemorySpace(), dlg.getRange());
			}
		}
	}

	private String getMemorySpace(IMemoryBlock memBlock, String def) {
		if (memBlock != null && memBlock instanceof CMemoryBlockExtension) {
			return ((CMemoryBlockExtension)memBlock).getMemorySpaceID();
		}
		return def;
	}

	private String getSelectedAddress(BigInteger selectedAddress, String def) {
		if (selectedAddress != null) {
			return "0x" + selectedAddress.toString(16); //$NON-NLS-1$
		}
		return def;
	}

	private String getRange(MemoryByte[] selectedBytes, int addressableSize, String def) {
		if (selectedBytes != null && selectedBytes.length > 0) {
			return Integer.toString(selectedBytes.length / addressableSize);
		}
		return def;
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void selectionChanged(IAction action, ISelection selection) {}

	private IStructuredSelection getSelection() {
		return (IStructuredSelection)getView().getViewSite().getSelectionProvider().getSelection();
	}
}
