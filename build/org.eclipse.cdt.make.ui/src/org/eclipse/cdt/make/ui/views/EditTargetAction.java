/*******************************************************************************
 * Copyright (c) 2000, 2013 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.ui.views;

import java.util.List;

import org.eclipse.cdt.make.core.IMakeTarget;
import org.eclipse.cdt.make.internal.ui.MakeUIImages;
import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.cdt.make.ui.dialogs.MakeTargetDialog;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.SelectionListenerAction;

/**
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class EditTargetAction extends SelectionListenerAction {
	private final Shell shell;

	public EditTargetAction(Shell shell) {
		super(MakeUIPlugin.getResourceString("EditTargetAction.label")); //$NON-NLS-1$
		this.shell = shell;

		setToolTipText(MakeUIPlugin.getResourceString("EditTargetAction.tooltip")); //$NON-NLS-1$
		setDisabledImageDescriptor(MakeUIImages.getImageDescriptor(MakeUIImages.IMG_DTOOL_TARGET_EDIT));
		setImageDescriptor(MakeUIImages.getImageDescriptor(MakeUIImages.IMG_ETOOL_TARGET_EDIT));
	}

	@Override
	public void run() {
		if (canRename()) {
			MakeTargetDialog dialog;
			try {
				dialog = new MakeTargetDialog(shell, (IMakeTarget) getStructuredSelection().getFirstElement());
				dialog.open();
			} catch (CoreException e) {
				MakeUIPlugin.errorDialog(shell,
						MakeUIPlugin.getResourceString("EditTargetAction.exception.internalError"), //$NON-NLS-1$
						MakeUIPlugin.getResourceString("EditTargetAction.exception.errorEditingTarget"), e); //$NON-NLS-1$
			}
		}
	}

	@Override
	protected boolean updateSelection(IStructuredSelection selection) {
		return super.updateSelection(selection) && canRename();
	}

	private boolean canRename() {
		List<?> elements = getStructuredSelection().toList();
		if (elements.size() == 1 && (elements.get(0) instanceof IMakeTarget)) {
			return true;
		}
		return false;
	}
}
