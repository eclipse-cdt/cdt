package org.eclipse.cdt.make.ui.views;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;


public class MakeTargetAction extends Action  {

	Shell shell;
	IResource resource;

	public MakeTargetAction (Shell shell) {
		super("Add Make Targets");
		this.shell = shell;
	
		setToolTipText("BuildAction");
		setImageDescriptor(CPluginImages.DESC_BUILD_MENU);
	}

	public void run() {
		InputDialog dialog = new InputDialog(shell, "Target Dialog: ", "Enter Target(s): ", null, null);
		dialog.open();
		//String value = dialog.getValue();
//		if (value != null && value.length() > 0) {
//			if (resource != null)
//				MakeUtil.addPersistentTarget(resource, value);
//		}
	}

	public void selectionChanged(IStructuredSelection selection) {
		Object obj = (IAdaptable)selection.getFirstElement();
		if (obj instanceof IAdaptable) {
			IAdaptable element = (IAdaptable)obj;
			resource = (IResource)element.getAdapter(IResource.class);
		}
	}
}
