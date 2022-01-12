/*******************************************************************************
 * Copyright (c) 2007 Symbian Software Limited and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
