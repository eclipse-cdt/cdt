/**********************************************************************
 * Copyright (c) 2002,2003 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.make.internal.ui.editor;

import java.util.List;

import org.eclipse.cdt.make.core.IMakeTarget;
import org.eclipse.cdt.make.core.IMakeTargetManager;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.core.makefile.ITargetRule;
import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.cdt.make.ui.dialogs.MakeTargetDialog;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IFileEditorInput;

/**
 */
public class AddBuildTargetAction extends Action {

	MakefileContentOutlinePage fOutliner;
	
	public AddBuildTargetAction(MakefileContentOutlinePage outliner) {
		super("Add To Build Target");
		setDescription("Add To Build Target");
		setToolTipText("Add To Build Target");                 
		fOutliner = outliner;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		IMakeTargetManager manager = MakeCorePlugin.getDefault().getTargetManager();
		IFile file = getFile();
		ITargetRule rule = getTargetRule(fOutliner.getSelection());
		Shell shell = fOutliner.getControl().getShell();
		if (file != null && rule != null && shell != null) {
			String name = rule.getTarget().toString().trim();
			IMakeTarget target = manager.findTarget(file.getParent(), name);
			if (target != null) {
				MakeTargetDialog dialog;
				try {
					dialog = new MakeTargetDialog(shell, target);
					dialog.open();
				} catch (CoreException e) {
					MakeUIPlugin.errorDialog(shell, "Internal Error", "", e);
				}
			} else {
				try {
					String[] ids = manager.getTargetBuilders(file.getProject());
					if (ids.length > 0) {
						target = manager.createTarget(file.getProject(), name, ids[0]);
						target.setContainer(file.getParent());
						target.setBuildTarget(name);
						manager.addTarget(file.getParent(), target);
					}
				} catch (CoreException e) {
					MakeUIPlugin.errorDialog(shell, "Internal Error", "", e);
				}
			}
		}
	}

	public boolean canActionBeAdded(ISelection selection) {
		ITargetRule rule = getTargetRule(selection);
		if (rule != null) {
			IFile file = getFile();
			if (file != null) {
				return MakeCorePlugin.getDefault().getTargetManager().hasTargetBuilder(file.getProject());
			}
		}
		return false;
	}

	
	private IFile getFile() {
		Object input = fOutliner.getInput();
		if (input instanceof IFileEditorInput) {
			return ((IFileEditorInput)input).getFile();
		}
		return null;
	}

	private ITargetRule getTargetRule(ISelection sel) {
		if (!sel.isEmpty() && sel instanceof IStructuredSelection) {
			List list= ((IStructuredSelection)sel).toList();
			if (list.size() == 1) {
				Object element= list.get(0);
				if (element instanceof ITargetRule) {
					return (ITargetRule)element;
				}
			}
		}
		return null;
	}

}
