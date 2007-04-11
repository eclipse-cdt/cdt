/*******************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.rse.internal.useractions.ui;

import org.eclipse.swt.widgets.Composite;

/**
 * Interface that is to be implemented by anyone interested in 
 *  supplying additional gui, beyond the default, to the command text widget.
 */
public interface ISystemCommandTextAdditionalGUIProvider {
	/**
	 * Overridable entry point for subclasses that wish to put something to the right of the "Command:" label
	 * @return true if something entered to take up the available columns, false otherwise (will be padded)
	 */
	public boolean createCommandLabelLineControls(Composite parent, int availableColumns);

	/**
	 * Create additional buttons, to go under command prompt.
	 * Overridable.
	 * @return true if something entered to take up the available columns, false otherwise (will be padded)
	 */
	public boolean createExtraButtons(Composite parent, int availableColumns);
}
