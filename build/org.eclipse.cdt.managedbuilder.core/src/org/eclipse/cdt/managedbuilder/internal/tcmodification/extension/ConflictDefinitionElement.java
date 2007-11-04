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
package org.eclipse.cdt.managedbuilder.internal.tcmodification.extension;

import org.eclipse.core.runtime.IConfigurationElement;

public class ConflictDefinitionElement extends MatchObjectList {
	public static final String ELEMENT_NAME = "conflictDefinition"; //$NON-NLS-1$

	public ConflictDefinitionElement(IConfigurationElement element)
			throws IllegalArgumentException {
		super(element);
	}

}
