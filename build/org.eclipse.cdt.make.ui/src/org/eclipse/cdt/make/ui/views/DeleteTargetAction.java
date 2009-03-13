/*******************************************************************************
 * Copyright (c) 2000, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.ui.views;


import com.ibm.icu.text.MessageFormat;
import java.util.List;

import org.eclipse.cdt.make.core.IMakeTarget;
import org.eclipse.cdt.make.core.IMakeTargetManager;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.internal.ui.MakeUIImages;
import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.SelectionListenerAction;

public class DeleteTargetAction extends SelectionListenerAction {
	private final Shell shell;

	public DeleteTargetAction(Shell shell) {
		super(MakeUIPlugin.getResourceString("DeleteTargetAction.label")); //$NON-NLS-1$
		this.shell = shell;

		setToolTipText(MakeUIPlugin.getResourceString("DeleteTargetAction.tooltip")); //$NON-NLS-1$
		MakeUIImages.setImageDescriptors(this, "tool16", MakeUIImages.IMG_TOOLS_MAKE_TARGET_DELETE); //$NON-NLS-1$
	}

	/**
	 * Asks the user to confirm a delete operation.
	 *
	 * @return <code>true</code> if the user says to go ahead, and <code>false</code>
	 *  if the deletion should be abandoned
	 */
	boolean confirmDelete() {
		List<?> targets = getSelectedElements();
		String title;
		String msg;
		if (targets.size() == 1) {
			title = MakeUIPlugin.getResourceString("DeleteTargetAction.title.confirmDeletion"); //$NON-NLS-1$
			IMakeTarget target = (IMakeTarget) targets.get(0);
			msg = MessageFormat.format(MakeUIPlugin.getResourceString("DeleteTargetAction.message.confirmDeleteion"), //$NON-NLS-1$
				new Object[] { target.getName()});
		} else {
			title = MakeUIPlugin.getResourceString("DeleteTargetAction.title.confirmMultipleDeletion"); //$NON-NLS-1$
			msg = MessageFormat.format(MakeUIPlugin.getResourceString("DeleteTargetAction.message.confirmMultipleDeletion"), //$NON-NLS-1$
				new Object[] { new Integer(targets.size())});
		}
		return MessageDialog.openQuestion(shell, title, msg);
	}

	@Override
	public void run() {
		if (!canDelete() || confirmDelete() == false) {
			return;
		}
		IMakeTargetManager manager = MakeCorePlugin.getDefault().getTargetManager();
		try {
			for (Object target : getSelectedElements()) {
				if (target instanceof IMakeTarget) {
						manager.removeTarget((IMakeTarget) target);
				}
			}
		} catch (CoreException e) {
			MakeUIPlugin.errorDialog(shell, MakeUIPlugin.getResourceString("DeleteTargetAction.exception.removeError"), //$NON-NLS-1$
				MakeUIPlugin.getResourceString("DeleteTargetAction.exception.errorDeletingBuildTarget"), e);  //$NON-NLS-1$
		}
	}

	@Override
	protected boolean updateSelection(IStructuredSelection selection) {
		return super.updateSelection(selection) && canDelete();
	}

	/**
	 * @return
	 */
	private List<?> getSelectedElements() {
		return getStructuredSelection().toList();
	}

	/**
		 * @return
		 */
	private boolean canDelete() {
		List<?> elements = getSelectedElements();
		for (Object element : elements) {
			if (! (element instanceof IMakeTarget)) {
				return false;
			}
		}
		return elements.size()>0;
	}

}
