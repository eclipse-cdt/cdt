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

import org.eclipse.cdt.core.builder.ICToolchainPoint;
import org.eclipse.cdt.core.builder.model.ICToolchainProvider;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;

/**
 * Simple wrapper for the data associated with an instance of
 * a CToolchain extension point.
 */

public class CToolchainPoint
	extends ACExtensionPoint
	implements ICToolchainPoint {

	/**
	 * Constructor.
	 * 
	 * @param element configuration element for the toolchain provider.
	 */
	public CToolchainPoint(IConfigurationElement element) {
		super(element);
	}

	/**
	 * @see org.eclipse.cdt.core.builder.ICToolchainPoint#getId()
	 */
	public String getId() {
		return getField(FIELD_ID);
	}

	/**
	 * @see org.eclipse.cdt.core.builder.ICToolchainPoint#getNatures()
	 */
	public String[] getNatures() {
		return parseField(getField(FIELD_NATURES, "*"), ";"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * @see org.eclipse.cdt.core.builder.ICToolchainPoint#getProviderClassName()
	 */
	public String getProviderClassName() {
		return getField(FIELD_CLASS);
	}

	/**
	 * @see org.eclipse.cdt.core.builder.ICToolchainPoint#getProvider()
	 */
	public ICToolchainProvider getProvider() throws CoreException {
		return (ICToolchainProvider) getClassInstance(FIELD_CLASS);
	}
}
