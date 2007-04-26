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
 * This interface has methods which returns a UIElement, given an Element.
 * Method to return a List of Elements, given an Element.
 * 
 * @since 4.0
 */

public interface IUIElementTreeBuilderHelper {
	/**
	 * Returns the UIElement.
	 * @param element
	 * @return UIElement
     * 
     * @since 4.0
	 */
	public UIElement getUIElement(Element element);
}
