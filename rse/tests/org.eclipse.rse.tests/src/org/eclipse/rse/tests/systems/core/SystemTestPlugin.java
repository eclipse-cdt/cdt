/* *******************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Don Yantzi (IBM) - initial contribution.
 * David Dykstal (IBM) - initial contribution.
 * *******************************************************************************/
package org.eclipse.rse.tests.systems.core;

import org.eclipse.ui.plugin.AbstractUIPlugin;

public class SystemTestPlugin extends AbstractUIPlugin {
	
	private static SystemTestPlugin singleton = null;
	
	/**
	 * Constructs a new plugin.
	 * The plugin should be constructed by the workbench exactly once, 
	 * so it is saved as a singleton.
	 */
	public SystemTestPlugin() {
		super();
		singleton = this;
	}
	
	/**
	 * @return the singleton plugin.
	 */
	public static SystemTestPlugin getDefault() {
		return singleton;
	}
	
}
