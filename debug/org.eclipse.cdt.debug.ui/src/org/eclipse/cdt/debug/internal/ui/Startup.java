/*******************************************************************************
 * Copyright (c) 2007 ARM and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * ARM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.ui.IStartup;

/**
 * Forces the org.eclipse.cdt.debug.ui plugin to be loaded. The Modules view requires 
 * CElementAdapterFactory to be registered to display the labels and images of ICElement's. 
 */
public class Startup implements IStartup {

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IStartup#earlyStartup()
	 */
	public void earlyStartup() {
		CUIPlugin.getDefault();
	}
}
