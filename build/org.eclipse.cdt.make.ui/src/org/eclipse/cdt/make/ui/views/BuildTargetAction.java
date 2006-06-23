/*******************************************************************************
 * Copyright (c) 2000, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.ui.views;


import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.make.core.IMakeTarget;
import org.eclipse.cdt.make.internal.ui.MakeUIImages;
import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.cdt.make.ui.TargetBuild;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.SelectionListenerAction;

public class BuildTargetAction extends SelectionListenerAction {

	Shell shell;
	IResource resource;

	public BuildTargetAction(Shell shell) {
		super(MakeUIPlugin.getResourceString("BuildTargetAction.label")); //$NON-NLS-1$
		this.shell = shell;

		setToolTipText(MakeUIPlugin.getResourceString("BuildTargetAction.tooltip")); //$NON-NLS-1$
		MakeUIImages.setImageDescriptors(this, "tool16", MakeUIImages.IMG_TOOLS_MAKE_TARGET_BUILD); //$NON-NLS-1$
	}

	public void run() {
		if (canBuild()) {
			IMakeTarget[] targets = (IMakeTarget[]) getSelectedElements().toArray(new IMakeTarget[0]);
			TargetBuild.buildTargets(shell, targets);
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
