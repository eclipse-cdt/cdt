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

import org.eclipse.cdt.ui.templateengine.SimpleElementException;

/**
 * InputUIElement, an abstract class extends UIElement. Provides implementation
 * to some of the methods. It provides definitions to those methods which
 * doesn't apply to InuputUIElement's. SimpleElementException is thrown from
 * these methods.
 * 
 */
public abstract class InputUIElement extends UIElement {
	public static final String INPUTTYPE= "input"; //$NON-NLS-1$
	public static final String MULTILINETYPE= "multiline"; //$NON-NLS-1$
	public static final String SELECTTYPE= "select"; //$NON-NLS-1$
	public static final String BOOLEANTYPE= "boolean"; //$NON-NLS-1$
	public static final String BROWSETYPE= "browse"; //$NON-NLS-1$
	public static final String BROWSEDIRTYPE= "browsedir"; //$NON-NLS-1$
	public static final String STRINGLISTTYPE= "stringlist"; //$NON-NLS-1$
	public static final String SPECIALLISTTYPE= "speciallist"; //$NON-NLS-1$
	public static final String MANDATORY= "mandatory"; //$NON-NLS-1$
	public static final String INPUTPATTERN="pattern"; //$NON-NLS-1$
	public static final String DEFAULT= "default"; //$NON-NLS-1$
	public static final String WIDGETLABEL= "label"; //$NON-NLS-1$
	public static final String BROWSELABEL= "    Browse..   "; //$NON-NLS-1$
	public static final String CONTENTS= " contents"; //$NON-NLS-1$
	public static final String ISINVALID= " is Invalid. "; //$NON-NLS-1$
	public static final String CHECKPROJECT= "checkproject"; //$NON-NLS-1$
	public static final String NULL= "null"; //$NON-NLS-1$
	public static final String SIZE= "size"; //$NON-NLS-1$
	public static final String HIDDEN= "hidden"; //$NON-NLS-1$
	
	/**
	 * The string appearing in the Combo box
	 */
	public static final String COMBOITEM_LABEL= "label"; //$NON-NLS-1$
	
	/**
	 * Alternative attribute name for the value stored when the corresponding Combo item is selected.
	 * See <a href="https://bugs.eclipse.org/222954">Bugzilla 222954</a>.
	 */
	public static final String COMBOITEM_NAME= "name"; //$NON-NLS-1$
	
	/**
	 * Preferred attribute name for the value stored when the corresponding Combo item is selected.
	 */
	public static final String COMBOITEM_VALUE= "value"; //$NON-NLS-1$

	protected InputUIElement(UIAttributes uiAttribute) {
		super(uiAttribute);
	}

	/**
	 * Overloaded from UIElement, It does not apply to InputUIElement
	 * 
	 * @see UIElement
	 * @param uiElement
	 * @throws SimpleElementException
	 */
	@Override
	public void addToChildList(UIElement uiElement) throws SimpleElementException {
		throw new SimpleElementException();
	}

	/**
	 * Overloaded from UIElement, It does not apply to InputUIElement
	 * 
	 * @see UIElement
	 */
	@Override
	public int getChildCount() throws SimpleElementException {
		throw new SimpleElementException();
	}

	/**
	 * Overloaded from UIElement, It does not apply to InputUIElement
	 * 
	 * @see UIElement
	 */
	@Override
	public UIElement getChild(int index) throws SimpleElementException {
		throw new SimpleElementException();
	}
}
