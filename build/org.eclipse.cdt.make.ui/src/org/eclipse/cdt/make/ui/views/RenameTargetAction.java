package org.eclipse.cdt.make.ui.views;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

import org.eclipse.cdt.make.internal.ui.MakeUIImages;
import org.eclipse.core.resources.IResource;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.SelectionListenerAction;


public class RenameTargetAction extends SelectionListenerAction  {

	Shell shell;
	IResource resource;

	public RenameTargetAction (Shell shell) {
		super("Rename Build Target");
		this.shell = shell;
	
		setToolTipText("Rename Build Target");
		MakeUIImages.setImageDescriptors(this, "tool16", MakeUIImages.IMG_TOOLS_MAKE_TARGET_RENAME);
	}

	public void run() {
		
	}
}
