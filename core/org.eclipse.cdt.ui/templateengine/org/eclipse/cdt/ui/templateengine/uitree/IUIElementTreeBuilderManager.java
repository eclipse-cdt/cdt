/*******************************************************************************
 * Copyright (c) 2007 Symbian Software Limited and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Bala Torati (Symbian) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.templateengine.uitree;

import org.w3c.dom.Element;

/**
 * This interface provides methods, which will be implemented by Tree builder
 * class.
 * 
 * @since 4.0
 */

public interface IUIElementTreeBuilderManager {
	/**
	 * Creates the UIElement Tree
	 * @param ui
	 * @param parent
     * 
     * @since 4.0
	 */
	public void createUIElementTree(UIElement ui, Element parent);

}
