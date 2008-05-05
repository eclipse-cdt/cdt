/*******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir,
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson,
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 *
 * Contributors:
 * David Dykstal (IBM) - [168976][api] move ISystemNewConnectionWizardPage from core to UI
 * David Dykstal (IBM) - [226561] Add API markup to RSE javadocs for extend / implement
 *******************************************************************************/

package org.eclipse.rse.core.model;

import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;

/**
 * An interface used to drive properties into a subsystem during host creation.
 *
 * This is an internal interface for use in the framework. Potential clients
 * should extend one of the implementations or implement one of the extensions.
 *
 * @since org.eclipse.rse.core 3.0
 */
public interface ISubSystemConfigurator {

	/**
	 * Return the subsystem configuration associated with these properties.
	 */
	public ISubSystemConfiguration getSubSystemConfiguration();

	/**
	 * Apply the values herein to a subsystem.
	 * @param ss the subystem to be affected.
	 * @return true if the values were applied.
	 */
	public boolean applyValues(ISubSystem ss);

}
