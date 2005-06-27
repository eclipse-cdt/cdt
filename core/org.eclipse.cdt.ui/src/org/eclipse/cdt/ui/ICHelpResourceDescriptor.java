/**********************************************************************
 * Copyright (c) 2004, 2005 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *     Intel Corporation - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.ui;

import org.eclipse.help.IHelpResource;

/**
 * This interface represents Help Resources found in the certain Help Book 
 * provided by certain CHelpProvider
 * @see ICHelpProvider
 * @since 2.1
 */
public interface ICHelpResourceDescriptor {
	/**
	 * represents the Help Book, where help resources represented by getHelpResources()
	 * method were found
	 * @return ICHelpBook interface representing the help book where help was found 
	 */
	ICHelpBook getCHelpBook();
	
	/**
	 * gets the array of help resources found in the HelpBook represented
	 * by getCHelpBook() method
	 * @return the IHelpResource[] array
	 */
	IHelpResource[] getHelpResources();
}

