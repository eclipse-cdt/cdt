/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core;

import org.eclipse.cdt.core.ICExtensionDescriptor;
import org.eclipse.core.runtime.IConfigurationElement;

public class CExtensionDescriptor implements ICExtensionDescriptor {
	private IConfigurationElement fElement;
	protected static final String ATTRIBUTE_ID = "id"; //$NON-NLS-1$
	protected static final String ATTRIBUTE_NAME = "name"; //$NON-NLS-1$

	public CExtensionDescriptor(IConfigurationElement el){
		fElement = el;
	}

	@Override
	public String getId() {
		return fElement.getAttribute(ATTRIBUTE_ID);
	}

	@Override
	public String getName() {
		return fElement.getAttribute(ATTRIBUTE_NAME);
	}

	public IConfigurationElement getConfigurationElement(){
		return fElement;
	}
}
