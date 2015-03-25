/*******************************************************************************
 * Copyright (c) 2004, 2014 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * Alvaro Sanchez-Leon (Ericsson) - Support Register Groups (Bug 235747)
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.commands;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.cdt.debug.core.model.ICDebugTarget;
import org.eclipse.cdt.debug.core.model.IPersistableRegisterGroup;
import org.eclipse.cdt.debug.core.model.IRegisterDescriptor;
import org.eclipse.cdt.debug.internal.core.model.CDebugTarget;
import org.eclipse.cdt.debug.internal.ui.actions.ActionMessages;
import org.eclipse.cdt.debug.internal.ui.actions.RegisterGroupDialog;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IRegisterGroup;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;

public abstract class AbstractRegisterGroupCommandHandler extends AbstractHandler {

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.actions.IRegisterGroupActionsTarget#addRegisterGroup(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void addRegisterGroup(IStructuredSelection selection) {
		ICDebugTarget t = getDebugTarget(selection);
		if (t != null) {
			// Using Debug model
			try {
				IRegisterDescriptor[] registers = t.getRegisterDescriptors();
				RegisterGroupDialog dialog = new RegisterGroupDialog(Display.getCurrent().getActiveShell(), registers);
				if (dialog.open() == Window.OK) {
					t.addRegisterGroup(dialog.getName(), dialog.getDescriptors());
				}
			} catch (DebugException e) {
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.actions.IRegisterGroupActionsTarget#canAddRegisterGroup(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public boolean canAddRegisterGroup(IStructuredSelection selection) {
		ICDebugTarget target = getDebugTarget(selection);
		return (target != null) ? target.isSuspended() : false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.actions.IRegisterGroupActionsTarget#editRegisterGroup(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void editRegisterGroup(IStructuredSelection selection) {
		IPersistableRegisterGroup group = getRegisterGroup(selection);
		if (group == null) {
			return;
		}

		IRegisterDescriptor[] all;
		try {
			all = ((CDebugTarget) group.getDebugTarget()).getRegisterDescriptors();
			RegisterGroupDialog dialog = new RegisterGroupDialog(Display.getCurrent().getActiveShell(),
					group.getName(), all, group.getRegisterDescriptors());
			if (dialog.open() == Window.OK) {
				IDebugTarget target = group.getDebugTarget();
				if (target instanceof ICDebugTarget) {
					((ICDebugTarget) target).modifyRegisterGroup(group, dialog.getDescriptors());
				}
			}
		} catch (DebugException e) {
			CDebugUIPlugin.errorDialog(ActionMessages.getString("EditRegisterGroupActionDelegate.0"), e.getStatus()); //$NON-NLS-1$
		}

	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.actions.IRegisterGroupActionsTarget#canEditRegisterGroup(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public boolean canEditRegisterGroup(IStructuredSelection selection) {
		IPersistableRegisterGroup group = getRegisterGroup(selection);
		return (group != null);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.actions.IRegisterGroupActionsTarget#removeRegisterGroups(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void removeRegisterGroups(IStructuredSelection selection) {
		IRegisterGroup[] groups = getRegisterGroups(selection);
		if (groups.length > 0) {
			IDebugTarget target = groups[0].getDebugTarget();
			if (target instanceof ICDebugTarget) {
				((ICDebugTarget) target).removeRegisterGroups(groups);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.actions.IRegisterGroupActionsTarget#canRemoveRegisterGroups(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public boolean canRemoveRegisterGroups(IStructuredSelection selection) {
		IRegisterGroup[] groups = getRegisterGroups(selection);
		if (groups.length > 0) {
			return true;
		}

		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.actions.IRegisterGroupActionsTarget#restoreDefaultGroups(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void restoreDefaultGroups(IStructuredSelection selection) {
		getDebugTarget(selection).restoreDefaultRegisterGroups();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.actions.IRegisterGroupActionsTarget#canRestoreDefaultGroups(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public boolean canRestoreDefaultGroups(IStructuredSelection selection) {
		ICDebugTarget target = getDebugTarget(selection);
		return (target != null) ? target.isSuspended() : false;
	}

	private ICDebugTarget getDebugTarget(IStructuredSelection selection) {

		Object element = selection.getFirstElement();

		if (element instanceof IDebugElement) {
			return ((IDebugElement) element).getDebugTarget().getAdapter(ICDebugTarget.class);
		}
		return null;
	}

	private IPersistableRegisterGroup getRegisterGroup(IStructuredSelection ss) {
		IPersistableRegisterGroup selectedGroup = null;
		if (!ss.isEmpty()) {
			Object s = ss.getFirstElement();
			if (s instanceof IPersistableRegisterGroup) {
				selectedGroup = (IPersistableRegisterGroup) s;
			}
		}
	
		return selectedGroup;
	}

	private IRegisterGroup[] getRegisterGroups(IStructuredSelection ss) {
		ArrayList<IRegisterGroup> list = new ArrayList<IRegisterGroup>();
		for (Iterator<?> iterator = ss.iterator(); iterator.hasNext();) {
			Object selection = iterator.next();
			if (selection instanceof IRegisterGroup) {
				list.add((IRegisterGroup) selection);
			}
		}
	
		return list.toArray(new IRegisterGroup[list.size()]);
	}

}
