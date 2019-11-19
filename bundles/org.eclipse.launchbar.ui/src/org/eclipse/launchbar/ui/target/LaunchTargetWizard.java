/*******************************************************************************
 * Copyright (c) 2017 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.launchbar.ui.target;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.launchbar.core.target.ILaunchTarget;

public abstract class LaunchTargetWizard extends Wizard {

	protected ILaunchTarget launchTarget;

	public void setLaunchTarget(ILaunchTarget launchTarget) {
		this.launchTarget = launchTarget;
	}

	public ILaunchTarget getLaunchTarget() {
		return launchTarget;
	}

	public boolean canDelete() {
		return false;
	}

	public void performDelete() {
		// do nothing by default
	}

}
