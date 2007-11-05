package org.eclipse.cdt.debug.internal.ui.actions;


import java.math.BigInteger;

import org.eclipse.cdt.debug.internal.core.model.CMemoryBlockExtension;
import org.eclipse.cdt.debug.internal.ui.actions.AddWatchpointDialog;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.cdt.debug.ui.ICMemorySelection;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.MemoryByte;
import org.eclipse.debug.internal.ui.memory.provisional.AbstractAsyncTableRendering;
import org.eclipse.debug.ui.memory.AbstractMemoryRendering;
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
			
			if (obj instanceof AbstractAsyncTableRendering) {
				AbstractAsyncTableRendering r = (AbstractAsyncTableRendering) obj;
				memorySpace = getMemorySpace(r.getMemoryBlock(), memorySpace);
				address = getSelectedAddress(r.getSelectedAddress(), address);
				range = getRange(r.getSelectedAsBytes(), range);
			} else if (obj instanceof ICMemorySelection) {
				ICMemorySelection sel = (ICMemorySelection) obj;
				memorySpace = getMemorySpace(sel.getContainingBlock(), memorySpace);
				address = getSelectedAddress(sel.getAddress(), address);
				range = sel.getUnits().toString();
			} else if (obj instanceof AbstractMemoryRendering) {
				AbstractMemoryRendering r = (AbstractMemoryRendering) obj;
				address = getSelectedAddress(BigInteger.valueOf(r.getMemoryBlock().getStartAddress()), address);
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

	private String getRange(MemoryByte[] selectedBytes, String def) {
		if (selectedBytes != null && selectedBytes.length > 0) {
			return Integer.toString(selectedBytes.length);
		}
		return def;
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {}

	private IStructuredSelection getSelection() {
		return (IStructuredSelection)getView().getViewSite().getSelectionProvider().getSelection();
	}
}
