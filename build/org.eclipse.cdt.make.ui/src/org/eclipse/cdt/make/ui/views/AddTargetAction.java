package org.eclipse.cdt.make.ui.views;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

import java.util.List;

import org.eclipse.cdt.make.internal.ui.MakeUIImages;
import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.cdt.make.ui.dialogs.MakeTargetDialog;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.SelectionListenerAction;

public class AddTargetAction extends SelectionListenerAction {

	Shell shell;
	IResource resource;

	public AddTargetAction(Shell shell) {
		super("Add Build Target");
		this.shell = shell;

		setToolTipText("Add Build Target");
		MakeUIImages.setImageDescriptors(this, "tool16", MakeUIImages.IMG_TOOLS_MAKE_TARGET_ADD); //$NON-NLS-1$
	}

	public void run() {
		if (canAdd()) {
			try {
				MakeTargetDialog dialog = new MakeTargetDialog(shell, (IContainer) getStructuredSelection().getFirstElement());
				dialog.open();
			} catch (CoreException e) {
				MakeUIPlugin.errorDialog(shell, "Internal Error", "Internal Error", e);
			}
		}

	}

	protected boolean updateSelection(IStructuredSelection selection) {
		return super.updateSelection(selection) && canAdd();
	}

	private boolean canAdd() {
		List elements = getStructuredSelection().toList();
		if (elements.size() > 1 || elements.size() < 1) {
			return false;
		}
		if (elements.get(0) instanceof IContainer) {
			return true;
		}
		return false;
	}

}
