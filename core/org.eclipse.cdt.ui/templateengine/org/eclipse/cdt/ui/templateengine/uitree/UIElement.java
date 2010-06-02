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

import java.util.Map;

import org.eclipse.cdt.ui.templateengine.SimpleElementException;
import org.eclipse.cdt.ui.templateengine.uitree.uiwidgets.UIComposite;


/**
 * UIElement describes the abstract behavior expected from GenericUIElementGroup and 
 * InputUIElement. Some of the methods are meaningful to group Element. They will throw
 * SimpleElementException when invoked on InputUIElement.
 */
public abstract class UIElement {
	public static final String ID = "id"; //$NON-NLS-1$
	public static final String TYPE = "type"; //$NON-NLS-1$
	public static final String DESCRIPTION = "description"; //$NON-NLS-1$
	public static final String TITLE = "label"; //$NON-NLS-1$
	public static final String IMAGELOCATION = "image"; //$NON-NLS-1$

	/**
	 * Parent of this UIElement
	 */
	private UIElement parent;

	/**
	 * Attributes of this UIElement
	 */
	protected UIAttributes uiAttributes;
	
	public UIElement(UIAttributes uiAttributes) {
		this.uiAttributes = uiAttributes;
	}

	/**
	 * set the Parent of this UIElement
	 */
	public void setParent(UIElement parent) {
		this.parent = parent;
	}

	/**
	 * get the Parent of this UIElement
	 */
	public UIElement getParent() {
		return parent;
	}

	/**
	 * get the attributes of this UIElement
	 */
	public UIAttributes getAttributes() {
		return uiAttributes;
	}

	/**
	 * set the Values of UIElements from the given HashMap. This method is called recursively on all the children, if the UIElement instance on which this mehtod called is a GenericUIElementGroup. return void.
	 */
	public abstract void setValues(Map<String, String> valueMap);

	/**
	 * get The values as a HashMap. This method is called recursively on all the children, if the UIElement instance on which this mehtod called is a GenericUIElementGroup.
	 * @return  HashMap.
	 */
	public abstract Map<String, String> getValues();

	/**
	 * This method adds UIWidets to UIComposite. This method is called
	 * recursively on all the children, if the UIElement instance on which this
	 * method called is a GenericUIElementGroup.
	 * 
	 * @param uiComposite
	 */
	public abstract void createWidgets(UIComposite uiComposite);

	/**
	 * disposes the widget. This method is called recursively on all the
	 * children, if the UIElement instance on which this method is called, is a
	 * GenericUIElementGroup.
	 * 
	 */
	public abstract void disposeWidget();

	/**
	 * getThe child UIElement at the given index. This method throws
	 * SimpleElementException, if invoked on a InputUIElement.
	 * 
	 * @param index
	 * @return The child UIElement
	 * @throws SimpleElementException
	 */
	public abstract UIElement getChild(int index) throws SimpleElementException;

	/**
	 * add the given UIElement to the childList. This method throws
	 * SimpleElementException, if invoked on a InputUIElement.
	 * 
	 * @param uiElement
	 * @throws SimpleElementException
	 */
	public abstract void addToChildList(UIElement uiElement) throws SimpleElementException;

	/**
	 * returns the child count of UIElement. This method throws
	 * SimpleElementException, if invoked on a InputUIElement.
	 * 
	 * @return the child count of UIElement
	 * @throws SimpleElementException
	 */
	public abstract int getChildCount() throws SimpleElementException;

	/**
	 * The return value depends on the state of the UIElement. This information
	 * is used by UIPage to enable or disable the UIPage.
	 * 
	 * @return boolean.
	 */
	public abstract boolean isValid();
}
