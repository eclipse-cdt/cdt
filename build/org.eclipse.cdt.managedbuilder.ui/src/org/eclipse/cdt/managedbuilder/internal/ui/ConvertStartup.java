/*******************************************************************************
 * Copyright (c) 2005 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.managedbuilder.internal.ui;

import org.eclipse.cdt.managedbuilder.ui.actions.ConvertTargetAction;
import org.eclipse.ui.IStartup;


public class ConvertStartup implements IStartup {

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IStartup#earlyStartup()
	 */
	/* 
	 * This code is needed incase we want to add cascading menu for project converters in UI.
	 */
	public void earlyStartup() {
		ConvertTargetAction.initStartup();
	}
}
