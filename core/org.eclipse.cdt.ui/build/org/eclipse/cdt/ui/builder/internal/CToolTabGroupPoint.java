/**********************************************************************
 * Copyright (c) 2002,2003 Timesys Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * Timesys - Initial API and implementation
 **********************************************************************/

package org.eclipse.cdt.ui.builder.internal;

import org.eclipse.cdt.core.builder.internal.ACExtensionPoint;
import org.eclipse.cdt.ui.builder.ICToolTabGroup;
import org.eclipse.cdt.ui.builder.ICToolTabGroupPoint;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;

/**
 * @author sam.robb
 */
public class CToolTabGroupPoint
	extends ACExtensionPoint
	implements ICToolTabGroupPoint {

	/**
	 * Constructor.
	 * 
	 * @param element configuration element for the build configuration provider.
	 */
	public CToolTabGroupPoint(IConfigurationElement element) {
		super(element);
	}

	/**
	 * @see org.eclipse.cdt.ui.builder.ICToolTabGroupPoint#getId()
	 */
	public String getId() {
		return getField(FIELD_ID);
	}

	/**
	 * @see org.eclipse.cdt.ui.builder.ICToolTabGroupPoint#getProviderClassName()
	 */
	public String getProviderClassName() {
		return getField(FIELD_CLASS);
	}

	/**
	 * @see org.eclipse.cdt.ui.builder.ICToolTabGroupPoint#getProvider()
	 */
	public ICToolTabGroup getProvider() throws CoreException {
		return (ICToolTabGroup) getClassInstance(FIELD_CLASS);
	}

}
