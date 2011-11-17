/*******************************************************************************
 * Copyright (c) 2000, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Axel Mueller - Rebuild last target
 *******************************************************************************/
package org.eclipse.cdt.make.ui.actions;

import org.eclipse.cdt.make.core.IMakeTarget;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.cdt.make.ui.TargetBuild;
import org.eclipse.cdt.make.ui.dialogs.BuildTargetDialog;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.bindings.BindingManagerEvent;
import org.eclipse.jface.bindings.IBindingManagerListener;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.keys.IBindingService;

/**
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class BuildTargetAction extends AbstractTargetAction {

	private static final String TARGET_BUILD_COMMAND = "org.eclipse.cdt.make.ui.targetBuildCommand"; //$NON-NLS-1$
	private IBindingService bindingService;
	private IAction InitAction;

	public BuildTargetAction(){
		bindingService = null;
		InitAction = null;
	}

	@Override
	public void init(IAction action) {
		bindingService = (IBindingService) PlatformUI.getWorkbench().getService(IBindingService.class);
		if (bindingService != null) {
			bindingService.addBindingManagerListener(bindingManagerListener);
			String keyBinding = bindingService.getBestActiveBindingFormattedFor(TARGET_BUILD_COMMAND);
			if (keyBinding != null)
				action.setText(MakeUIPlugin.getResourceString("ActionMakeBuildTarget.label")+"\t"+ keyBinding); //$NON-NLS-1$ //$NON-NLS-2$
		}
		InitAction = action;
    }

	@Override
	public void run(IAction action) {
		IContainer container = getSelectedContainer();
		if (container != null) {
			BuildTargetDialog dialog = new BuildTargetDialog(getShell(), container, true);
			String name = null;
			try {
				name = (String) container.getSessionProperty(new QualifiedName(MakeUIPlugin.getUniqueIdentifier(),
						TargetBuild.LAST_TARGET));
			} catch (CoreException e) {
			}
			try {
				if (name != null) {
					IMakeTarget target = MakeCorePlugin.getDefault().getTargetManager().findTarget(container, name);
					if (target != null)
						dialog.setTarget(target);
				}
				if (dialog.open() == Window.OK) {
					IMakeTarget target = dialog.getTarget();
					if (target != null) {
						container.setSessionProperty(new QualifiedName(MakeUIPlugin.getUniqueIdentifier(),
								TargetBuild.LAST_TARGET), target.getName());
						IPath path = target.getContainer().getProjectRelativePath();
						container.getProject().setSessionProperty(
								new QualifiedName(MakeUIPlugin.getUniqueIdentifier(),
										TargetBuild.LAST_TARGET_CONTAINER), path.toString());
					}
				}
			} catch (CoreException e) {
			}
		}
	}

	@Override
	public void dispose() {
		if (bindingService != null) {
			bindingService.removeBindingManagerListener(bindingManagerListener);
			bindingService = null;
		}

		super.dispose();
	}

	private IBindingManagerListener bindingManagerListener = new IBindingManagerListener() {

		@Override
		public void bindingManagerChanged(BindingManagerEvent event) {

			if (event.isActiveBindingsChanged()) {
				String keyBinding = bindingService.getBestActiveBindingFormattedFor(TARGET_BUILD_COMMAND);
				if (keyBinding != null) InitAction.setText(
						MakeUIPlugin.getResourceString("ActionMakeBuildTarget.label")+"\t"+ keyBinding); //$NON-NLS-1$ //$NON-NLS-2$

			}
		}
	};

}
