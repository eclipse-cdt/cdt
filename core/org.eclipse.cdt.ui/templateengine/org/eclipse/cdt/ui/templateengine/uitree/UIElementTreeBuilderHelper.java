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

import java.util.HashMap;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import org.eclipse.cdt.core.templateengine.TemplateDescriptor;
import org.eclipse.cdt.core.templateengine.TemplateEngine;
import org.eclipse.cdt.core.templateengine.TemplateEngineHelper;
import org.eclipse.cdt.core.templateengine.TemplateInfo;
import org.eclipse.cdt.ui.templateengine.uitree.uiwidgets.UIBooleanWidget;
import org.eclipse.cdt.ui.templateengine.uitree.uiwidgets.UIBrowseWidget;
import org.eclipse.cdt.ui.templateengine.uitree.uiwidgets.UISelectWidget;
import org.eclipse.cdt.ui.templateengine.uitree.uiwidgets.UISpecialListWidget;
import org.eclipse.cdt.ui.templateengine.uitree.uiwidgets.UIStringListWidget;
import org.eclipse.cdt.ui.templateengine.uitree.uiwidgets.UITextWidget;


/**
 * UIElementTreeBuilderHelper provides methods to convert an Element (XML) into
 * UIElement. The UIElement can be a simple UI Widget or a group.
 */

public class UIElementTreeBuilderHelper implements IUIElementTreeBuilderHelper {

	/**
	 * TemplateDescriptor representing the TemplaeDescriptor XML.
	 */
	private TemplateDescriptor templateDescriptor = null;
	private TemplateInfo templateInfo;

	private Element element;

	/**
	 * Constructor, takes an TemplateDescriptor instance as parameter.
	 * 
	 * @param templateDescriptor
	 */
	public UIElementTreeBuilderHelper(TemplateDescriptor templateDescriptor, TemplateInfo templateInfo) {
		this.templateDescriptor = templateDescriptor;
		this.templateInfo = templateInfo;
	}

	/**
	 * 
	 * @return List of child Elements for the given
	 */
	public List getPropertyGroupList() {
		return templateDescriptor.getPropertyGroupList();
	}

	/**
	 * Given an XML Element, representing a PropertyElement. A UIElement for the
	 * same is returned. The Type attribute is verified, based on Type
	 * approprioate UIWidget is instantiated. This calss the getUIWidget private
	 * method.
	 * 
	 * @param element
	 * @return UIElement.
	 */
	public UIElement getUIElement(Element element) {

		this.element = element;
		UIElement retUIElement = null;
		UIAttributes/*<String, String>*/ uiAttributes = new UIAttributes/*<String, String>*/(templateInfo);

		NamedNodeMap list = element.getAttributes();
		for (int i = 0, s = list.getLength(); i < s; i++) {
			Node attribute = list.item(i);
			uiAttributes.put(attribute.getNodeName(), attribute.getNodeValue());
		}

		retUIElement = getUIWidget(uiAttributes);

		return retUIElement;
	}

	/**
	 * Given an XML Element, representing a PropertyElement. A UIElement for the
	 * same is returned. The Type attribute is verified, based on Type
	 * approprioate UIWidget is instantiated.
	 * 
	 * @param uiAttributes
	 * @return UIElement.
	 */
	private UIElement getUIWidget(UIAttributes/*<String, String>*/ uiAttributes) {

		String type = (String) uiAttributes.get(InputUIElement.TYPE);

		UIElement widgetElement = null;
		String itemName = null;
		String itemValue = null;
		String itemSelected = null;
		String nameStr = null;
		String valueStr = null;
		String selected = null;

		if (new Boolean((String)uiAttributes.get(InputUIElement.HIDDEN)).booleanValue())
			return null;
		if (type.equalsIgnoreCase("") || type == null) //$NON-NLS-1$
			return null;

		// UIWidgets
		if (type.equalsIgnoreCase(InputUIElement.INPUTTYPE)) {
			widgetElement = new UITextWidget(uiAttributes);
		}

		if (type.equalsIgnoreCase(InputUIElement.MULTILINETYPE)) {
			widgetElement = new UITextWidget(uiAttributes);
		}

		if (type.equalsIgnoreCase(InputUIElement.SELECTTYPE)) {

			HashMap/*<String, String>*/ itemMap = new HashMap/*<String, String>*/();
			List/*<Element>*/ itemList = TemplateEngine.getChildrenOfElement(element);

			for (int i = 0, l = itemList.size(); i < l; i++) {
				Element itemElement = (Element) itemList.get(i);
				NamedNodeMap itemAttrList = itemElement.getAttributes();

				for (int j = 0, l1 = itemAttrList.getLength(); j < l1; j++) {
					Node itemAttr = itemAttrList.item(j);
					itemName = itemAttr.getNodeName();
					itemValue = itemAttr.getNodeValue();

					if (itemName.equals(InputUIElement.TITLE)) {

						selected = itemValue;
						nameStr = new String(itemValue);
					} else if (itemName.equals(InputUIElement.NAME))
						valueStr = new String(itemValue);

					if (itemName.equals(InputUIElement.SELECTED))
						if (itemValue.equals(TemplateEngineHelper.BOOLTRUE))
							itemSelected = selected;

					itemMap.put(nameStr, valueStr);
				}
			}

			widgetElement = new UISelectWidget(uiAttributes, itemMap, itemSelected);
		}

		if (type.equalsIgnoreCase(InputUIElement.BOOLEANTYPE)) {
			widgetElement = new UIBooleanWidget(uiAttributes);
		}

		if (type.equalsIgnoreCase(InputUIElement.BROWSETYPE)) {
			widgetElement = new UIBrowseWidget(uiAttributes);
		}

		if (type.equalsIgnoreCase(InputUIElement.STRINGLISTTYPE)) {
			widgetElement = new UIStringListWidget(uiAttributes);
		}
		
		if (type.equalsIgnoreCase(InputUIElement.SPECIALLISTTYPE)) {
			widgetElement = new UISpecialListWidget(uiAttributes);
		}

		// PAGES(Groups).

		if (type.equalsIgnoreCase(GenericUIElementGroup.PAGES_ONLY)) {
			widgetElement = new SimpleUIElementGroup(uiAttributes);
		}

		if (type.equalsIgnoreCase(GenericUIElementGroup.PAGES_TAB)) {
			// Note: This is not implemented now as we haven't found a use case
			// for generating UI pages as TABS in a single page. 
		}

		return widgetElement;
	}
}
