package org.eclipse.cdt.make.ui.views;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

import org.eclipse.cdt.make.internal.ui.MakeUIImages;
import org.eclipse.core.resources.IResource;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.SelectionListenerAction;


public class AddTargetAction extends SelectionListenerAction  {

	Shell shell;
	IResource resource;

	public AddTargetAction (Shell shell) {
		super("Add Build Target");
		this.shell = shell;
	
		setToolTipText("Add Build Target");
		MakeUIImages.setImageDescriptors(this, "tool16", MakeUIImages.IMG_TOOLS_MAKE_TARGET_ADD);
	}

	public void run() {
		
	}
}
