/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.ui.views;


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
		super(MakeUIPlugin.getResourceString("AddTargetAction.label")); //$NON-NLS-1$
		this.shell = shell;

		setToolTipText(MakeUIPlugin.getResourceString("AddTargetAction.tooltip")); //$NON-NLS-1$
		MakeUIImages.setImageDescriptors(this, "tool16", MakeUIImages.IMG_TOOLS_MAKE_TARGET_ADD); //$NON-NLS-1$
	}

	public void run() {
		if (canAdd()) {
			try {
				MakeTargetDialog dialog = new MakeTargetDialog(shell, (IContainer) getStructuredSelection().getFirstElement());
				dialog.open();
			} catch (CoreException e) {
				MakeUIPlugin.errorDialog(shell, MakeUIPlugin.getResourceString("AddTargetAction.exception.internalError"), e.toString(), e); //$NON-NLS-1$
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
