/*******************************************************************************
 * Copyright (c) 2005, 2011 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *******************************************************************************/
package org.eclipse.cdt.internal.ui;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.jface.viewers.IBasicPropertyConstants;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;

public class CElementPropertySource implements IPropertySource {

	private final static String LABEL = "CElementProperties.name"; //$NON-NLS-1$

	private ICElement fCElement;

	// Property Descriptors
	static private IPropertyDescriptor[] fgPropertyDescriptors;

	static {
		// resource name
		String displayName = CUIPlugin.getResourceString(LABEL);
		PropertyDescriptor descriptor = new PropertyDescriptor(IBasicPropertyConstants.P_TEXT, displayName);
		descriptor.setAlwaysIncompatible(true);

		fgPropertyDescriptors = new IPropertyDescriptor[] { descriptor };
	}

	public CElementPropertySource(ICElement elem) {
		fCElement = elem;
	}

	/**
	 * @see IPropertySource#getPropertyDescriptors
	 */
	@Override
	public IPropertyDescriptor[] getPropertyDescriptors() {
		return fgPropertyDescriptors;
	}

	/**
	 * @see IPropertySource#getPropertyValue
	 */
	@Override
	public Object getPropertyValue(Object name) {
		if (name.equals(IBasicPropertyConstants.P_TEXT)) {
			return fCElement.getElementName();
		}
		return null;
	}

	/**
	 * @see IPropertySource#setPropertyValue
	 */
	@Override
	public void setPropertyValue(Object name, Object value) {
	}

	/**
	 * @see IPropertySource#getEditableValue
	 */
	@Override
	public Object getEditableValue() {
		return null;
	}

	/**
	 * @see IPropertySource#isPropertySet
	 */
	@Override
	public boolean isPropertySet(Object property) {
		return false;
	}

	/**
	 * @see IPropertySource#resetPropertyValue
	 */
	@Override
	public void resetPropertyValue(Object property) {
	}
}
