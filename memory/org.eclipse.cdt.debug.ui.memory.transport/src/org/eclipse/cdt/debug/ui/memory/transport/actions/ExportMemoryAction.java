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

import java.math.BigInteger;

import org.eclipse.cdt.debug.ui.memory.transport.ExportMemoryDialog;
import org.eclipse.cdt.debug.ui.memory.transport.MemoryTransportPlugin;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IMemoryBlockExtension;
import org.eclipse.debug.ui.memory.IMemoryRendering;
import org.eclipse.debug.ui.memory.IMemoryRenderingSite;
import org.eclipse.debug.ui.memory.IRepositionableMemoryRendering;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

/**
 * Action for exporting memory.
 */
public class ExportMemoryAction implements IViewActionDelegate {

	private IMemoryRenderingSite fView;

	public void init(IViewPart view) {
		if (view instanceof IMemoryRenderingSite)
			fView = (IMemoryRenderingSite) view;
	}


	/**
	 * Utility PODS to return a memory block and an address from a method
	 */
	static class BlockAndAddress {
		
		static public final BlockAndAddress EMPTY = new BlockAndAddress(null, BigInteger.valueOf(0)); 

		public BlockAndAddress(IMemoryBlock block, BigInteger addr) {
			this.block = block;
			this.addr = addr;
		}

		public IMemoryBlock block;
		public BigInteger addr;
	}

	/**
	 * Returns the memory block and initial base address for the export
	 * operation.
	 * 
	 * @return a result object; null is never returned
	 */
	static BlockAndAddress getMemoryBlockAndInitialStartAddress(ISelection selection)
	{

		IMemoryBlock memBlock = null;
		BigInteger initialStartAddr = null;
		
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection strucSel = (IStructuredSelection) selection;

			// return if current selection is empty
			if (strucSel.isEmpty())
				return BlockAndAddress.EMPTY;

			Object obj = strucSel.getFirstElement();

			if (obj == null)
				return BlockAndAddress.EMPTY;

			// Get the initial start address for the operation. 
			if (obj instanceof IMemoryRendering) {
				memBlock = ((IMemoryRendering) obj).getMemoryBlock();
				if (obj instanceof IRepositionableMemoryRendering) {
					initialStartAddr = ((IRepositionableMemoryRendering)obj).getSelectedAddress();
				}
			} else if (obj instanceof IMemoryBlock) {
				memBlock = (IMemoryBlock) obj;
			}
			
			if (initialStartAddr == null) {
				if (memBlock instanceof IMemoryBlockExtension) {
					try {
						initialStartAddr = ((IMemoryBlockExtension)memBlock).getBigBaseAddress();
					} catch (DebugException e) {
						initialStartAddr = BigInteger.valueOf(memBlock.getStartAddress());
					}
				}
				else {
					if (memBlock != null) {
						initialStartAddr = BigInteger.valueOf(memBlock.getStartAddress());
					}
				}
			}
		}
		
		return new BlockAndAddress(memBlock, initialStartAddr); 
	}
	
	public void run(IAction action) {

		ISelection selection = fView.getSite().getSelectionProvider()
			.getSelection();
		BlockAndAddress blockAndAddr = getMemoryBlockAndInitialStartAddress(selection);
		if(blockAndAddr.block == null)
			return;
		ExportMemoryDialog dialog = new ExportMemoryDialog(MemoryTransportPlugin.getShell(), blockAndAddr.block, blockAndAddr.addr);
		dialog.open();
		
		dialog.getResult();
	}
	
	public void selectionChanged(IAction action, ISelection selection) {
		action.setEnabled(getMemoryBlockAndInitialStartAddress(selection).block != null);
	}

}
