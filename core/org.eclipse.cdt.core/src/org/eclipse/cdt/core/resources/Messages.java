/*******************************************************************************
 * Copyright (c) 2011, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.resources;

import org.eclipse.osgi.util.NLS;

class Messages extends NLS {
	public static String RefreshExclusion_0;
	public static String RefreshScopeManager_0;
	public static String RefreshScopeManager_1;
	public static String RefreshScopeManager_2;
	public static String RefreshScopeManager_3;
	public static String RefreshScopeManager_4;

	static {
		// Initialize resource bundle.
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}

	private Messages() {
	}
}
