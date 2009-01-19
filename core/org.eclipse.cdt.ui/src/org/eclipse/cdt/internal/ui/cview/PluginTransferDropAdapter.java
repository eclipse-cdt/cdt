package org.eclipse.cdt.internal.ui.cview;

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.ui.part.PluginDropAdapter;
import org.eclipse.ui.part.PluginTransfer;

import org.eclipse.cdt.internal.ui.dnd.TransferDropTargetListener;

public class PluginTransferDropAdapter extends PluginDropAdapter implements
		TransferDropTargetListener {

	public PluginTransferDropAdapter (StructuredViewer viewer) {
		super(viewer);
	}
	
	public Transfer getTransfer() {
		return PluginTransfer.getInstance();
	}

	public boolean isEnabled(DropTargetEvent event) {
		return true;
	}

}

