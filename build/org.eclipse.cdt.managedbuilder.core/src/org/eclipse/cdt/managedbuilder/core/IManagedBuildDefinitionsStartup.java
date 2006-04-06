/*******************************************************************************
 * Copyright (c) 2006 Nokia Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.core;

/**
 * Clients that need to augment configuration attributes may need to insure
 * that those modification are picked up when configruation elements are loaded.
 * Implementing this interface will insure that a client's plugin is loaded
 * before all available configurations are available to the first project that
 * is loaded in the workbench.
 * 
 * An example of this use is when a client creates unique build configuration IDs,
 * derived from default configruations, and all existing projects need to know about
 * all possible build configurations at eclipse startup.
 */
public interface IManagedBuildDefinitionsStartup {
	
	String BUILD_DEFINITION_STARTUP = "buildDefinitionStartup"; //$NON-NLS-1$
	String CLASS_ATTRIBUTE = "class";			//$NON-NLS-1$
	
	/**
	 * Any work you want to do on build definitions after they have been loaded but before they have been resolved.
	 */
	void buildDefsLoaded();
	
	/**
	 * Any work you want to do on build definitions after they have been resolved.
	 */
	void buildDefsResolved();
}

