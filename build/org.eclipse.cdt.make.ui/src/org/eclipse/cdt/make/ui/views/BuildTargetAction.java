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
import org.eclipse.cdt.make.ui.TargetBuild;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.SelectionListenerAction;

/**
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class BuildTargetAction extends SelectionListenerAction {
	private final Shell shell;

	public BuildTargetAction(Shell shell) {
		super(MakeUIPlugin.getResourceString("BuildTargetAction.label")); //$NON-NLS-1$
		this.shell = shell;

		setToolTipText(MakeUIPlugin.getResourceString("BuildTargetAction.tooltip")); //$NON-NLS-1$
		setDisabledImageDescriptor(MakeUIImages.getImageDescriptor(MakeUIImages.IMG_DTOOL_TARGET_BUILD));
		setImageDescriptor(MakeUIImages.getImageDescriptor(MakeUIImages.IMG_ETOOL_TARGET_BUILD));
		setEnabled(false);
	}

	@Override
	public void run() {
		if (canBuild()) {
			IMakeTarget[] targets = getSelectedElements().toArray(new IMakeTarget[0]);
			TargetBuild.buildTargets(shell, targets);
			// set last target property for last element
			IContainer container = targets[targets.length - 1].getContainer();
			try {
				container.setSessionProperty(
						new QualifiedName(MakeUIPlugin.getUniqueIdentifier(), TargetBuild.LAST_TARGET),
						targets[targets.length - 1].getName());
			} catch (CoreException e) {
			}
			// store container of last target, too
			try {
				IPath path = container.getProjectRelativePath();
				container.getProject().setSessionProperty(
						new QualifiedName(MakeUIPlugin.getUniqueIdentifier(), TargetBuild.LAST_TARGET_CONTAINER),
						path.toString());
			} catch (CoreException e) {
			}
		}
	}

	@Override
	protected boolean updateSelection(IStructuredSelection selection) {
		return super.updateSelection(selection) && canBuild();
	}

	private boolean canBuild() {
		List<?> elements = getSelectedElements();
		for (Object element : elements) {
			if (!(element instanceof IMakeTarget)) {
				return false;
			}
		}
		return elements.size() > 0;
	}

	private List<?> getSelectedElements() {
		return getStructuredSelection().toList();
	}
}
