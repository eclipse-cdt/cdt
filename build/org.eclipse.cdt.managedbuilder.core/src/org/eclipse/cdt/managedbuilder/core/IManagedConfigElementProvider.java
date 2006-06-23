/*******************************************************************************
 * Copyright (c) 2004, 2006 TimeSys Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * TimeSys Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.core;
/**
 * Clients may implement this interface to dynamically provided the config
 * information that is otherwise specified in the ManagedBuidInfo extension
 * point.  It corresponds to the <code>configProvider</code> sub-element of 
 * the ManagedBuildInfo extension point.
 */
public interface IManagedConfigElementProvider {
	
	String ELEMENT_NAME = "dynamicElementProvider";	//$NON-NLS-1$
	String CLASS_ATTRIBUTE = "class";	//$NON-NLS-1$
	
	/**
	 * Each configuration element returned from this method is treated as if
	 * it were a direct sub-child of a ManagedBuildInfo extension.  As such
	 * it should conform to ManagedBuildTools.exsd.  The only exception is it
	 * should not contain nested <code>configProvider</code> elements.
	 */
	IManagedConfigElement[] getConfigElements();
}
