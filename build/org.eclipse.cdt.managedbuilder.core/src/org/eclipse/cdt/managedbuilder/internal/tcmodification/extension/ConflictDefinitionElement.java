/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.tcmodification.extension;

import org.eclipse.core.runtime.IConfigurationElement;

public class ConflictDefinitionElement extends MatchObjectList {
	public static final String ELEMENT_NAME = "conflictDefinition"; //$NON-NLS-1$

	public ConflictDefinitionElement(IConfigurationElement element) throws IllegalArgumentException {
		super(element);
	}

}
