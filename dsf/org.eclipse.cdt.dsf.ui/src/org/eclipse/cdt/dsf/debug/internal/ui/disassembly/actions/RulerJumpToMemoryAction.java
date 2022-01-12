/*******************************************************************************
 * Copyright (c) 2021 Intel Corporation.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.disassembly.actions;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.DisassemblyMessages;
import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.provisional.DisassemblySelection;
import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.provisional.IDisassemblyPart;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IMemoryBlockExtension;
import org.eclipse.debug.core.model.IMemoryBlockRetrievalExtension;
import org.eclipse.debug.internal.ui.views.memory.MemoryViewUtil;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.memory.IMemoryRendering;
import org.eclipse.debug.ui.memory.IMemoryRenderingContainer;
import org.eclipse.debug.ui.memory.IMemoryRenderingSite;
import org.eclipse.debug.ui.memory.IMemoryRenderingType;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;

/**
 * Ruler action to jump to a memory location using the address of the last row selection.
 */
@SuppressWarnings("restriction")
public final class RulerJumpToMemoryAction extends AbstractDisassemblyRulerAction {
	private static final String MEMORY_VIEW_ID = "org.eclipse.debug.ui.MemoryView"; //$NON-NLS-1$

	public RulerJumpToMemoryAction(IDisassemblyPart disassemblyPart, IVerticalRulerInfo rulerInfo) {
		super(disassemblyPart, rulerInfo);
		setText(DisassemblyMessages.Disassembly_action_JumpToMemory_label);
		setToolTipText(DisassemblyMessages.Disassembly_action_JumpToMemory_tooltip);
	}

	@Override
	public void run() {
		if (getRulerInfo() == null || getDisassemblyPart() == null) {
			reportException(new Exception("The current selection is invalid.")); //$NON-NLS-1$
			return;
		}

		IAddress address = getAddress();
		if (address == null) {
			reportException(new Exception("Failed to retrieve memory address.")); //$NON-NLS-1$
			return;
		}

		IWorkbenchPartSite site = getDisassemblyPart().getSite();
		if (site == null) {
			reportException(new Exception("No workbench site available. Disassembly view not initialized?")); //$NON-NLS-1$
			return;
		}
		IWorkbenchPage page = site.getPage();
		if (page == null) {
			reportException(new Exception("No workbench page available. Disassembly view not initialized?")); //$NON-NLS-1$
			return;
		}
		IViewPart viewPart = page.findView(MEMORY_VIEW_ID);
		if (viewPart != null) {
			page.activate(viewPart);
		} else {
			try {
				viewPart = page.showView(MEMORY_VIEW_ID);
			} catch (PartInitException e) {
				reportException(e);
				return;
			}
		}

		if (!(viewPart instanceof IMemoryRenderingSite)) {
			reportException(new Exception("Failed to open memory view.")); //$NON-NLS-1$
			return;
		}

		addMemoryBlock((IMemoryRenderingSite) viewPart, address.toHexAddressString());
	}

	/**
	 * Add memory block for the given address.
	 *
	 * @param memoryView the memory view
	 * @param address the memory address to add the block for
	 */
	private void addMemoryBlock(IMemoryRenderingSite memoryView, String address) {
		try {
			IAdaptable debugContext = DebugUITools.getPartDebugContext(getDisassemblyPart().getSite());

			IMemoryBlockRetrievalExtension memRetrieval = (IMemoryBlockRetrievalExtension) MemoryViewUtil
					.getMemoryBlockRetrieval(debugContext);

			// get extended memory block with the expression entered
			IMemoryBlockExtension memBlock = memRetrieval.getExtendedMemoryBlock(address, debugContext);

			// add block to memory block manager
			if (memBlock == null) {
				throw new Exception("Failed to retrieve memory block."); //$NON-NLS-1$
			}
			IMemoryBlock[] memArray = new IMemoryBlock[] { memBlock };

			DebugPlugin.getDefault().getMemoryBlockManager().addMemoryBlocks(memArray);

			IMemoryRenderingType renderingType = DebugUITools.getMemoryRenderingManager()
					.getPrimaryRenderingType(memBlock);

			IMemoryRendering rendering = renderingType.createRendering();

			IMemoryRenderingContainer container = memoryView.getContainer(IDebugUIConstants.ID_RENDERING_VIEW_PANE_1);

			rendering.init(container, memBlock);
			container.addMemoryRendering(rendering);
		} catch (Exception e) {
			reportException(e);
		}
	}

	/**
	 * @return the address from the current selection, or <code>null</code>
	 */
	private IAddress getAddress() {
		int lastLine = getRulerInfo().getLineOfLastMouseButtonActivity();
		if (lastLine < 0) {
			return null;
		}
		ISelectionProvider provider = getDisassemblyPart().getSite().getSelectionProvider();
		if (provider == null) {
			return null;
		}
		IDocument document = getDisassemblyPart().getTextViewer().getDocument();
		if (document == null) {
			return null;
		}

		IRegion region;
		try {
			region = document.getLineInformation(lastLine);
		} catch (BadLocationException e) {
			return null;
		}
		ITextSelection textSelection = new TextSelection(document, region.getOffset(), 0);
		DisassemblySelection selection = new DisassemblySelection(textSelection, getDisassemblyPart());
		return selection.getStartAddress();
	}

	/**
	* Report an error to the user.
	*
	* @param e underlying exception
	*/
	private void reportException(Exception e) {
		IStatus status = new Status(IStatus.ERROR, CDebugUIPlugin.PLUGIN_ID, "Error on jump to memory: ", e); //$NON-NLS-1$
		ErrorDialog.openError(getDisassemblyPart().getSite().getShell(),
				DisassemblyMessages.Disassembly_action_JumpToMemory_errorTitle,
				DisassemblyMessages.Disassembly_action_JumpToMemory_errorMessage, status);
		CDebugUIPlugin.log(status);
	}
}
