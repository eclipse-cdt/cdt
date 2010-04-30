/*******************************************************************************
 * Copyright (c) 2000, 2010 QNX Software Systems and others.
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

import org.eclipse.cdt.make.core.IMakeTarget;
import org.eclipse.cdt.make.core.IMakeTargetManager;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.SelectionListenerAction;

import com.ibm.icu.text.MessageFormat;

/**
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class DeleteTargetAction extends SelectionListenerAction {
	private final Shell shell;

	public DeleteTargetAction(Shell shell) {
		super(MakeUIPlugin.getResourceString("DeleteTargetAction.label")); //$NON-NLS-1$
		this.shell = shell;

		setToolTipText(MakeUIPlugin.getResourceString("DeleteTargetAction.tooltip")); //$NON-NLS-1$

		ISharedImages images = PlatformUI.getWorkbench().getSharedImages();
		setImageDescriptor(images.getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));
		setDisabledImageDescriptor(images.getImageDescriptor(ISharedImages.IMG_TOOL_DELETE_DISABLED));
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
					// if necessary remove last target property 
					String lastTargetName = null;
					IContainer container = ((IMakeTarget) target).getContainer();
					try {
						lastTargetName = (String)container.getSessionProperty(new QualifiedName(MakeUIPlugin.getUniqueIdentifier(), "lastTarget")); //$NON-NLS-1$
					} catch (CoreException e) {
					}
					if (lastTargetName != null && lastTargetName.equals(((IMakeTarget) target).getName())) {
						try {
							container.setSessionProperty(new QualifiedName(MakeUIPlugin.getUniqueIdentifier(),
									"lastTarget"), null); //$NON-NLS-1$
						} catch (CoreException e) {
						}
					}
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

	private List<?> getSelectedElements() {
		return getStructuredSelection().toList();
	}

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
