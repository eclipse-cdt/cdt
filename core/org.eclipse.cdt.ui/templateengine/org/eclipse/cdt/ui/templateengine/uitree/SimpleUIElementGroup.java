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

import org.eclipse.cdt.ui.templateengine.uitree.uiwidgets.UIComposite;


/**
 * This is a PAGES_ONLY implementation for UIElement group. It uses the method
 * implementation for UIElement provided by GenericUIElementGroup.
 */

public class SimpleUIElementGroup extends GenericUIElementGroup {

	public SimpleUIElementGroup(UIAttributes attribute) {
		super(UIGroupTypeEnum.PAGES_ONLY, attribute);
	}

	/**
	 * @see UIElement
	 */
	@Override
	public void setValues(Map<String, String> valueMap) {
		super.setValues(valueMap);
	}

	/**
	 * @see UIElement
	 */
	@Override
	public Map<String, String> getValues() {
		return super.getValues();
	}

	/**
	 * @see UIElement
	 */
	@Override
	public void createWidgets(UIComposite uiComposite) {
		super.createWidgets(uiComposite);
	}

	/**
	 * dispose the Widget, releasing any resources occupied by this widget. The
	 * same is called on the child list.
	 * 
	 * @see UIElement
	 */
	@Override
	public void disposeWidget() {
		super.disposeWidget();
	}

	// @see UIElement
	@Override
	public boolean isValid() {
		return super.isValid();
	}
}
