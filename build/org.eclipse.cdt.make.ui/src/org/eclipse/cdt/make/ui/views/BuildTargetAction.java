package org.eclipse.cdt.make.ui.views;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.make.core.IMakeTarget;
import org.eclipse.cdt.make.internal.ui.MakeUIImages;
import org.eclipse.cdt.make.ui.actions.TargetBuild;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.SelectionListenerAction;

public class BuildTargetAction extends SelectionListenerAction {

	Shell shell;
	IResource resource;

	public BuildTargetAction(Shell shell) {
		super("Build Target");
		this.shell = shell;

		setToolTipText("Build Target");
		MakeUIImages.setImageDescriptors(this, "tool16", MakeUIImages.IMG_TOOLS_MAKE_TARGET_BUILD);
	}

	public void run() {
		if (canBuild()) {
			IMakeTarget[] targets = (IMakeTarget[]) getSelectedElements().toArray(new IMakeTarget[0]);
			TargetBuild.runWithProgressDialog(shell, targets);
		}
	}

	protected boolean updateSelection(IStructuredSelection selection) {
		return super.updateSelection(selection) && canBuild();
	}

	private boolean canBuild() {
		List elements = getSelectedElements();
		if (elements.size() > 0) {
			Iterator iterator = elements.iterator();
			while (iterator.hasNext()) {
				if (!(iterator.next() instanceof IMakeTarget)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	private List getSelectedElements() {
		return getStructuredSelection().toList();
	}
}
