package org.eclipse.cdt.internal.ui.dnd;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.ui.part.PluginDropAdapter;
import org.eclipse.ui.part.PluginTransfer;

import org.eclipse.cdt.core.model.ICElement;


public class PluginTransferDropAdapter extends PluginDropAdapter implements
		TransferDropTargetListener {

	public PluginTransferDropAdapter (StructuredViewer viewer) {
		super(viewer);
	}
	
	public Transfer getTransfer() {
		return PluginTransfer.getInstance();
	}

	public boolean isEnabled(DropTargetEvent event) {
		Object target = event.item != null ? event.item.getData() : null;
		if (target == null) {
			return false;
		}
		return target instanceof ICElement || target instanceof IResource;
	}

}

