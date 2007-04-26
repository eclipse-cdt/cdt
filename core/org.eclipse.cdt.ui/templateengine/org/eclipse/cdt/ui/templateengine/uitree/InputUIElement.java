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

import org.eclipse.cdt.ui.templateengine.SimpleElementException;

/**
 * InputUIElement, an abstract class extends UIElement. Provides implementation
 * to some of the methods. It provides definitions to those methods which
 * doesn't apply to InuputUIElement's. SimpleElementException is thrown from
 * these methods.
 * 
 */
public abstract class InputUIElement extends UIElement {

	public static final String INPUTTYPE = new String("input"); //$NON-NLS-1$
	public static final String MULTILINETYPE = new String("multiline"); //$NON-NLS-1$
	public static final String SELECTTYPE = new String("select"); //$NON-NLS-1$
	public static final String BOOLEANTYPE = new String("boolean"); //$NON-NLS-1$
	public static final String BROWSETYPE = new String("browse"); //$NON-NLS-1$
	public static final String STRINGLISTTYPE = new String("stringlist"); //$NON-NLS-1$
	public static final String SPECIALLISTTYPE = new String("speciallist"); //$NON-NLS-1$
	public static final String MANDATORY = new String("mandatory"); //$NON-NLS-1$
	public static final String INPUTPATTERN = new String("pattern"); //$NON-NLS-1$
	public static final String DEFAULT = new String("default"); //$NON-NLS-1$
	public static final String WIDGETLABEL = new String("label"); //$NON-NLS-1$
	public static final String BROWSELABEL = new String("    Browse..   "); //$NON-NLS-1$
	public static final String CONTENTS = new String(" contents"); //$NON-NLS-1$
	public static final String ISINVALID = new String(" is Invalid. "); //$NON-NLS-1$
	public static final String CHECKPROJECT = new String("checkproject"); //$NON-NLS-1$
	public static final String NULL = new String("null"); //$NON-NLS-1$
	public static final String SIZE = new String("size"); //$NON-NLS-1$
	public static final String HIDDEN = new String("hidden"); //$NON-NLS-1$
	public static final String NAME = new String("name"); //$NON-NLS-1$
	public static final String SELECTED = new String("selected"); //$NON-NLS-1$


	protected InputUIElement(UIAttributes/*<String, String>*/ uiAttribute) {
		super(uiAttribute);
	}

	/**
	 * Overloaded from UIElement, It does not apply to InputUIElement
	 * 
	 * @see UIElement
	 * @param uiElement
	 * @throws SimpleElementException
	 */
	public void addToChildList(UIElement uiElement) throws SimpleElementException {
		throw new SimpleElementException();
	}

	/**
	 * Overloaded from UIElement, It does not apply to InputUIElement
	 * 
	 * @see UIElement
	 */
	public int getChildCount() throws SimpleElementException {
		throw new SimpleElementException();
	}

	/**
	 * Overloaded from UIElement, It does not apply to InputUIElement
	 * 
	 * @see UIElement
	 */
	public UIElement getChild(int index) throws SimpleElementException {
		throw new SimpleElementException();
	}
}
