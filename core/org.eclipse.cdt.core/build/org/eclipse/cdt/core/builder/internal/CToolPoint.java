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

package org.eclipse.cdt.core.builder.internal;

import org.eclipse.cdt.core.builder.ICToolPoint;
import org.eclipse.cdt.core.builder.model.ICTool;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;

/**
 * Simple wrapper for the data associated with an instance of
 * a CTool extension point.
 */

public class CToolPoint extends ACExtensionPoint implements ICToolPoint {

	/**
	 * Constructor.
	 * 
	 * @param element configuration element for the tool type.
	 */
	public CToolPoint(IConfigurationElement element) {
		super(element);
	}

	/**
	 * @see org.eclipse.cdt.core.builder.ICToolPoint#getId()
	 */
	public String getId() {
		return getField(FIELD_ID);
	}

	/**
	 * @see org.eclipse.cdt.core.builder.ICToolPoint#getName()
	 */
	public String getName() {
		return getField(FIELD_NAME);
	}

	/**
	 * @see org.eclipse.cdt.core.builder.ICToolPoint#getType()
	 */
	public String getType() {
		return getField(FIELD_TYPE);
	}

	/**
	 * @see org.eclipse.cdt.core.builder.ICToolPoint#getProviderClassName()
	 */
	public String getProviderClassName() {
		return getField(FIELD_CLASS);
	}

	/**
	 * @see org.eclipse.cdt.core.builder.ICToolPoint#getProvider()
	 */
	public ICTool getProvider() throws CoreException {
		return (ICTool) getClassInstance(FIELD_CLASS);
	}
}
