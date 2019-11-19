/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.launchbar.ui.target;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.ui.wizards.IWizardDescriptor;

/**
 * A manager for the launch target UI.
 */
public interface ILaunchTargetUIManager {

	/**
	 * Return a label provider that gives the test and image for the target.
	 *
	 * @param target
	 *            the launch target
	 * @return the label provider for the launch target
	 */
	ILabelProvider getLabelProvider(ILaunchTarget target);

	/**
	 * @deprecated this should never have been in the interface, now returns null
	 * @return null
	 */
	@Deprecated
	public IWizardDescriptor[] getLaunchTargetWizards();

	/**
	 * Open a dialog to edit the specified launch target.
	 * 
	 * @param target
	 *            launch target to edit
	 */
	void editLaunchTarget(ILaunchTarget target);

}
