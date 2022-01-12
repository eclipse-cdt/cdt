/*******************************************************************************
 * Copyright (c) 2010 Axel Mueller and others.
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
package org.eclipse.cdt.make.ui.views;

import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.cdt.make.ui.actions.BuildLastTargetAction;
import org.eclipse.ui.actions.SelectionListenerAction;

/**
 * Rebuild last target of selected resource or project.
 *
 * @since 7.0
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class RebuildLastTargetAction extends SelectionListenerAction {

	public RebuildLastTargetAction() {
		super(MakeUIPlugin.getResourceString("BuildLastTargetAction.label")); //$NON-NLS-1$

		setToolTipText(MakeUIPlugin.getResourceString("BuildLastTargetAction.tooltip")); //$NON-NLS-1$
		setEnabled(false);
	}

	@Override
	public void run() {
		final BuildLastTargetAction buildAction = new BuildLastTargetAction();
		buildAction.selectionChanged(null, this.getStructuredSelection());

		if (buildAction.isEnabled()) {
			buildAction.run(null);
		}
	}
}
