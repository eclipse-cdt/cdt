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
		super("Delete Build Target");
		this.shell = shell;

		setToolTipText("Delete Build Target");
		MakeUIImages.setImageDescriptors(this, "tool16", MakeUIImages.IMG_TOOLS_MAKE_TARGET_DELETE);
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
			title = "Confirm Target Deletion";
			IMakeTarget target = (IMakeTarget) targets.get(0);
			msg = MessageFormat.format("Are you sure you want to delete  ''{0}''?", new Object[] { target.getName()});
		} else {
			title = "Confirm Multiple Target Deletion";
			msg =
				MessageFormat.format(
					"Are you sure you want to delete these {0} targets?",
					new Object[] { new Integer(targets.size())});
		}
		return MessageDialog.openQuestion(shell, title, msg);
	}

	public void run() {
		if (canDelete() && confirmDelete() == false)
			return;
		List targets = getTargetsToDelete();
		IMakeTargetManager manager = MakeCorePlugin.getDefault().getTargetManager();
		Iterator iter = targets.iterator();
		try {
			while (iter.hasNext()) {
				manager.removeTarget((IMakeTarget) iter.next());
			}
		} catch (CoreException e) {
			MakeUIPlugin.errorDialog(shell, "Target Remove Error", "Error deleting build target", e);
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
