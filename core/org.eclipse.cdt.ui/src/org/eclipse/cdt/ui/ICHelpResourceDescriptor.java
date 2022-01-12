/**********************************************************************
 * Copyright (c) 2004, 2008 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Intel Corporation - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.ui;

import org.eclipse.help.IHelpResource;

/**
 * This interface represents Help Resources found in the certain Help Book
 * provided by certain CHelpProvider
 * <p>
 * Clients may implement this interface.
 * </p>
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
