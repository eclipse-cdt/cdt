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
package org.eclipse.cdt.ui.templateengine.pages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.templateengine.TemplateEngineUtil;
import org.eclipse.cdt.ui.templateengine.SimpleElementException;
import org.eclipse.cdt.ui.templateengine.uitree.InputUIElement;
import org.eclipse.cdt.ui.templateengine.uitree.SimpleUIElementGroup;
import org.eclipse.cdt.ui.templateengine.uitree.UIElement;


/**
 * The UIPagesProvider creates a Map of UIPages. The Map will have ID as key,
 * UIPage as value. The sequence of call to get Map of UIPages. 1.
 * clearOrderVector() for all PropertyGroup Elements. 2. getUIPages(...)
 * 
 */
public class UIPagesProvider {

	/**
	 * maintains the Page display order.
	 */
	private List<String> orderVector;


	public UIPagesProvider() {
		orderVector = new ArrayList<String>();
	}
	
	/**
	 * after getting this clear the Vector.
	 * 
	 * @return Vector
	 */
	public List<String> getOrderVector() {
		return orderVector;
	}

	/**
	 * re-initialize the Vector.
	 */
	public void clearOrderVector() {
		orderVector = new ArrayList<String>();
	}

	/**
	 * This class has methods to return an HashMap of UIPages. The UIPages will
	 * correspond to UIElement group passed as parameter to this method. For a
	 * group UIElement, the children count is taken. An array of UIPage for the
	 * count is created. The same is initialized with UIPages.
	 * 
	 * @param uiElement
	 *            UIElement group root element. Which can be converted to a
	 *            UIPage.
	 * @param valueStore
	 * @return HashMap, UIPages corresponding to param aUIElement.
	 */
	public Map<String, UIWizardPage> getWizardUIPages(UIElement uiElement, Map<String, String> valueStore) {
		int childCount = 0;

		try {
			childCount = uiElement.getChildCount();
		} catch (SimpleElementException e) {
			TemplateEngineUtil.log(e);
		}

		// HashMap of UIPages
		HashMap<String, UIWizardPage> pageMap = new HashMap<String, UIWizardPage>();

		// If uiElement contains other group elements as children.
		if (hasChildUIGroupElement(uiElement)) {

			for (int i = 0; i < childCount; i++) {
				try {
					pageMap.putAll(getWizardUIPages(uiElement.getChild(i), valueStore)); // recursion
				} catch (SimpleElementException e) {
					TemplateEngineUtil.log(e);
				}
			}
		}
		else {
			if ((hasChildUIElement(uiElement))) {
				String label = uiElement.getAttributes().get(UIElement.TITLE);
				String description = (uiElement.getAttributes()).get(UIElement.DESCRIPTION);
				UIWizardPage uiPage = new UIWizardPage(label, description, uiElement, valueStore);

				pageMap.put((uiElement.getAttributes()).get(UIElement.ID), uiPage);
				addToOrderVector((uiElement.getAttributes()).get(UIElement.ID));
			}
		}
		return pageMap;
	}

	/**
	 * whether the given (node in UIElementTree) UIElement contains children of
	 * group type.
	 * 
	 * @param parent
	 * @return boolean, true if it does, false otherwise.
	 */
	public boolean hasChildUIGroupElement(UIElement parent) {
		boolean retVal = false;
		try {
			if (parent.getChildCount() > 0) {
				for (int i = 0; i < parent.getChildCount(); i++) {
					if (parent.getChild(i) instanceof SimpleUIElementGroup) {
						retVal = true;
						break;
					}
				}
			}
		} catch (SimpleElementException see) {
			retVal = false;
		}
		return retVal;
	}

	/**
	 * whether the given (node in UIElementTree) UIElement contains children of
	 * UIElement type.
	 * 
	 * @param parent
	 * @return boolean, true if it does, false otherwise.
	 */
	public boolean hasChildUIElement(UIElement parent) {
		boolean retVal = false;
		try {
			if (parent.getChildCount() > 0) {
				for (int i = 0; i < parent.getChildCount(); i++) {
					if (parent.getChild(i) instanceof InputUIElement) {
						retVal = true;
						break;
					}
				}
			}
		} catch (SimpleElementException see) {
			retVal = false;
		}
		return retVal;
	}

	/**
	 * If the order vector contains the page id return, do not add it to order
	 * vector. HashMap will not allow duplicate keys.
	 * 
	 * @param pageId
	 */
	private void addToOrderVector(String pageId) {
		for(String id : orderVector) {
			if (id.equalsIgnoreCase(pageId))
				return;
		}
		orderVector.add(pageId);
	}
}
