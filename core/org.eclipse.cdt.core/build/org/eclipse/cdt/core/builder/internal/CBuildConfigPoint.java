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

import org.eclipse.cdt.core.builder.ICBuildConfigPoint;
import org.eclipse.cdt.core.builder.model.ICBuildConfigProvider;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;

/**
 * Simple wrapper for the data associated with an instance of
 * a CBuildConfig extension point.
 */
public class CBuildConfigPoint
	extends ACExtensionPoint
	implements ICBuildConfigPoint {

	/**
	 * Constructor.
	 * 
	 * @param element configuration element for the build configuration provider.
	 */
	public CBuildConfigPoint(IConfigurationElement element) {
		super(element);
	}

	/**
	 * @see org.eclipse.cdt.core.builder.ICBuildConfigPoint#getId()
	 */
	public String getId() {
		return getField(FIELD_ID);
	}

	/**
	 * @see org.eclipse.cdt.core.builder.ICBuildConfigPoint#getName()
	 */
	public String getName() {
		return getField(FIELD_NAME);
	}

	/**
	 * @see org.eclipse.cdt.core.builder.ICBuildConfigPoint#getNatures()
	 */
	public String[] getNatures() {
		return parseField(getField(FIELD_NATURES, "*"), ";"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * @see org.eclipse.cdt.core.builder.ICBuildConfigPoint#getProviderClassName()
	 */
	public String getProviderClassName() {
		return getField(FIELD_CLASS);
	}

	/**
	 * @see org.eclipse.cdt.core.builder.ICBuildConfigPoint#getProvider()
	 */
	public ICBuildConfigProvider getProvider() throws CoreException {
		return (ICBuildConfigProvider) getClassInstance(FIELD_CLASS);
	}
}
