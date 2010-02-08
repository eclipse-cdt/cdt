/*******************************************************************************
 * Copyright (c) 2010 Axel Mueller and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Axel Mueller - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.ui.actions;

import org.eclipse.cdt.make.core.IMakeTarget;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.cdt.make.ui.TargetBuild;
import org.eclipse.cdt.make.ui.dialogs.BuildTargetDialog;
import org.eclipse.cdt.make.internal.ui.preferences.MakePreferencePage;
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
 */
public class BuildLastTargetAction extends AbstractTargetAction {
	
	@Override
	public void run(IAction action) {
		IContainer container = getSelectedContainer();
		if (container != null) {
			String name = null;
			if (MakePreferencePage.useProjectForLastMakeTarget()) {
				container = container.getProject();
			}
			try {
				name = (String)container.getSessionProperty(new QualifiedName(MakeUIPlugin.getUniqueIdentifier(), "lastTarget")); //$NON-NLS-1$
			} catch (CoreException e) {
			}
			try {
				boolean showDialog = true;
				if (name != null) {
					IPath path = new Path(name);
					if (path.segmentCount() <= 1) {// do not look recursively for last target
						IMakeTarget target = MakeCorePlugin.getDefault().getTargetManager().findTarget(container, name);
						if (target != null) {
							TargetBuild.buildTargets(getShell(), new IMakeTarget[] { target });
							showDialog = false;
						} 
					} 
				} 
				
				// no last target found, let the user decide
				if (showDialog) {
					BuildTargetDialog dialog = new BuildTargetDialog(getShell(), container, false/*Recursive*/);
					if (dialog.open() == Window.OK) {
						IMakeTarget target = dialog.getTarget();
						if (target != null) {
							IPath path =
								target.getContainer().getProjectRelativePath().removeFirstSegments(
										container.getProjectRelativePath().segmentCount());
							path = path.append(target.getName());
							container.setSessionProperty(new QualifiedName(MakeUIPlugin.getUniqueIdentifier(), "lastTarget"), //$NON-NLS-1$
									path.toString());
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
