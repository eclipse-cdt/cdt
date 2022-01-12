/*******************************************************************************
 * Copyright (c) 2010, 2011 Axel Mueller and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Axel Mueller - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.ui.actions;

import org.eclipse.cdt.make.core.IMakeTarget;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.cdt.make.internal.ui.preferences.MakePreferencePage;
import org.eclipse.cdt.make.ui.TargetBuild;
import org.eclipse.cdt.make.ui.dialogs.BuildTargetDialog;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.window.Window;

/**
 * Rebuild last target of selected resource or project.
 * Search is done non-recursively.
 * If no valid last target is found, show the build target dialog.
 *
 * @since 7.0
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class BuildLastTargetAction extends AbstractTargetAction {

	@Override
	public void run(IAction action) {
		IContainer container = getSelectedContainer();
		if (container != null) {
			String name = null;
			if (MakePreferencePage.useProjectLastMakeTarget()) {
				try {
					name = (String) container.getProject().getSessionProperty(
							new QualifiedName(MakeUIPlugin.getUniqueIdentifier(), TargetBuild.LAST_TARGET_CONTAINER));
					if (name != null) {
						IContainer lastTargetContainer;
						if (name.length() == 0)
							lastTargetContainer = container.getProject();
						else
							lastTargetContainer = container.getProject().getFolder(new Path(name));
						if (lastTargetContainer.exists())
							container = lastTargetContainer;
						name = (String) container.getSessionProperty(
								new QualifiedName(MakeUIPlugin.getUniqueIdentifier(), TargetBuild.LAST_TARGET));
					}
				} catch (CoreException e) {
				}
			} else {
				if (MakePreferencePage.useProjectRootForLastMakeTarget()) {
					container = container.getProject();
				}
				try {
					name = (String) container.getSessionProperty(
							new QualifiedName(MakeUIPlugin.getUniqueIdentifier(), TargetBuild.LAST_TARGET));
				} catch (CoreException e) {
				}
			}
			try {
				boolean showDialog = true;
				if (name != null) {
					IMakeTarget target = MakeCorePlugin.getDefault().getTargetManager().findTarget(container, name);
					if (target != null) {
						TargetBuild.buildTargets(getShell(), new IMakeTarget[] { target });
						showDialog = false;
						IPath path = container.getProjectRelativePath();
						container.getProject().setSessionProperty(new QualifiedName(MakeUIPlugin.getUniqueIdentifier(),
								TargetBuild.LAST_TARGET_CONTAINER), path.toString());
					}
				}

				// no last target found, let the user decide
				if (showDialog) {
					boolean recursive = MakePreferencePage.useProjectLastMakeTarget();
					BuildTargetDialog dialog = new BuildTargetDialog(getShell(), container, recursive);
					if (dialog.open() == Window.OK) {
						IMakeTarget target = dialog.getTarget();
						if (target != null) {
							container.setSessionProperty(
									new QualifiedName(MakeUIPlugin.getUniqueIdentifier(), TargetBuild.LAST_TARGET),
									target.getName());
							IPath path = target.getContainer().getProjectRelativePath();
							container.getProject()
									.setSessionProperty(new QualifiedName(MakeUIPlugin.getUniqueIdentifier(),
											TargetBuild.LAST_TARGET_CONTAINER), path.toString());
						}
					}
				}
			} catch (CoreException e) {
			}
		}
	}

	@Override
	public boolean isEnabled() {
		return super.isEnabled();
	}
}
