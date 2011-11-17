/*******************************************************************************
 * Copyright (c) 2007, 2008 Symbian Software Limited and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Bala Torati (Symbian) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.templateengine.uitree;

import java.util.List;

import org.w3c.dom.Element;

import org.eclipse.cdt.core.templateengine.TemplateEngine;
import org.eclipse.cdt.core.templateengine.TemplateEngineUtil;
import org.eclipse.cdt.ui.templateengine.SimpleElementException;


/**
 *
 * call setgetUIElementTreeRootNull(), before createUIElementTree(...).
 * UIElementTreeBuilderManager builds a UIElementTree.
 *
 * --------------UITree creation algorithm----------------------------
 * createUIElementTree(UITreeRoot, RootPropertyGroupElement) UITreeRoot is
 * initially null. createUIElementTree(UIElement parent, XML_Element element)
 * Step 1. if( parent == null ) parent =
 * UIElementTreeBuilderHelper.getUIElement(element).
 * Step 2. else {
 * 		Step 3. List
 * 		childList = getChildList(element);
 * 		Step 4. Iterator I =
 * 		getIterator(childList);
 * 		Step 5. for every element belonging to childList
 * 		{
 * 			Step 6. uiElement = getUIElement (element from childList); advance I to next
 * 			Element. uiElement .setParent(parent); parent.put(uiElement );
 *  		createUIElementTree(uiElement, element from childList);
 *  	}
 * }
 * ---------------------------------------------------------------------
 *
 */

public class UIElementTreeBuilderManager implements IUIElementTreeBuilderManager {
	/**
	 * reference to iUIElementTreeBuilderHelper, which returns UIElement for Element.
	 */
	private UIElementTreeBuilderHelper uiElementTreeBuilderHelper;

	/**
	 * The root of the UIElementTree.
	 */
	private UIElement uiTreeRoot = null;

	/**
	 *
	 * @param uiElementTreeBuilderHelper
	 */
	public UIElementTreeBuilderManager(UIElementTreeBuilderHelper uiElementTreeBuilderHelper) {
		this.uiElementTreeBuilderHelper = uiElementTreeBuilderHelper;
	}

	/**
	 * This method create the UIElementTree, by following the algorithm given
	 * above.
	 */
	@Override
	public void createUIElementTree(UIElement uiParent, Element element) {
		if (uiParent == null) {
			uiTreeRoot = uiElementTreeBuilderHelper.getUIElement(element);
			uiParent = uiTreeRoot;
		}

		if ((uiParent != null) && (uiParent instanceof GenericUIElementGroup)) {
			List<Element> childList = TemplateEngine.getChildrenOfElement(element);
			for (int listIndex = 0, l = childList.size(); listIndex < l; listIndex++) {
				UIElement uiElement = uiElementTreeBuilderHelper.getUIElement(childList.get(listIndex));
				if (uiElement != null) {
					uiElement.setParent(uiParent);
				} else {
					continue;
				}
				try {
					uiParent.addToChildList(uiElement);
				} catch (SimpleElementException exp) {
					TemplateEngineUtil.log(exp);
				}
				createUIElementTree(uiElement, childList.get(listIndex));
			}
		}
	}

	/**
	 *
	 * @return UIElement, root UIElement.
	 */
	public UIElement getUIElementTreeRoot() {
		return uiTreeRoot;
	}

	/**
	 * sets the UIElementTree root element to null. This method is invoked
	 * before creating UIElementTree.
	 */
	public void setUIElementTreeRootNull() {
		uiTreeRoot = null;
	}

}
