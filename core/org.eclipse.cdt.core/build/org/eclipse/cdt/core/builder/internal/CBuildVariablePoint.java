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

import org.eclipse.cdt.core.builder.ICBuildVariablePoint;
import org.eclipse.cdt.core.builder.model.ICBuildVariableProvider;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;

/**
 * Simple wrapper for the data associated with an instance of
 * a CBuildVariable extension point.
 */

public class CBuildVariablePoint
	extends ACExtensionPoint
	implements ICBuildVariablePoint {

	/**
	 * Constructor.
	 * 
	 * @param element configuration element for the build variable provider.
	 */
	public CBuildVariablePoint(IConfigurationElement element) {
		super(element);
	}

	/**
	 * @see org.eclipse.cdt.core.builder.ICBuildVariablePoint#getId()
	 */
	public String getId() {
		return getField(FIELD_ID);
	}

	/**
	 * @see org.eclipse.cdt.core.builder.ICBuildVariablePoint#getNatures()
	 */
	public String[] getNatures() {
		return parseField(getField(FIELD_NATURES, "*"), ";"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * @see org.eclipse.cdt.core.builder.ICBuildVariablePoint#getProviderClassName()
	 */
	public String getProviderClassName() {
		return getField(FIELD_CLASS);
	}

	/**
	 * @see org.eclipse.cdt.core.builder.ICBuildVariablePoint#getProvider()
	 */
	public ICBuildVariableProvider getProvider() throws CoreException {
		return (ICBuildVariableProvider) getClassInstance(FIELD_CLASS);
	}
}
