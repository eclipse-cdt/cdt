package org.eclipse.cdt.make.ui.views;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.make.core.IMakeTarget;
import org.eclipse.cdt.make.core.IMakeTargetManager;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.internal.ui.MakeUIImages;
import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.SelectionListenerAction;

public class DeleteTargetAction extends SelectionListenerAction {

	Shell shell;
	IResource resource;

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
		List targets = getTargetsToDelete();
		String title;
		String msg;
		if (targets.size() == 1) {
			title = MakeUIPlugin.getResourceString("DeleteTargetAction.title.confirmDeletion"); //$NON-NLS-1$
			IMakeTarget target = (IMakeTarget) targets.get(0);
			msg = MessageFormat.format(MakeUIPlugin.getResourceString("DeleteTargetAction.message.confirmDeleteion"), new Object[] { target.getName()}); //$NON-NLS-1$
		} else {
			title = MakeUIPlugin.getResourceString("DeleteTargetAction.title.confirmMultipleDeletion"); //$NON-NLS-1$
			msg =
				MessageFormat.format(
					MakeUIPlugin.getResourceString("DeleteTargetAction.message.confirmMultipleDeletion"), //$NON-NLS-1$
					new Object[] { new Integer(targets.size())});
		}
		return MessageDialog.openQuestion(shell, title, msg);
	}

	public void run() {
		if (!canDelete() || confirmDelete() == false)
			return;
		List targets = getTargetsToDelete();
		IMakeTargetManager manager = MakeCorePlugin.getDefault().getTargetManager();
		Iterator iter = targets.iterator();
		try {
			while (iter.hasNext()) {
				manager.removeTarget((IMakeTarget) iter.next());
			}
		} catch (CoreException e) {
			MakeUIPlugin.errorDialog(shell, MakeUIPlugin.getResourceString("DeleteTargetAction.exception.removeError"), MakeUIPlugin.getResourceString("DeleteTargetAction.exception.errorDeletingBuildTarget"), e); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	protected boolean updateSelection(IStructuredSelection selection) {
		return super.updateSelection(selection) && canDelete();
	}

	/**
	 * @return
	 */
	private List getTargetsToDelete() {
		return getStructuredSelection().toList();
	}

	/**
		 * @return
		 */
	private boolean canDelete() {
		List elements = getStructuredSelection().toList();
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

}
