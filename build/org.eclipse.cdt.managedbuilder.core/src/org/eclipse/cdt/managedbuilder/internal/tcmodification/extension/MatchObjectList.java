/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
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

public class MatchObjectList {
	private MatchObjectElement[] fObjects;

	public MatchObjectList(IConfigurationElement element) throws IllegalArgumentException {
		IConfigurationElement[] objectEls = element.getChildren(MatchObjectElement.ELEMENT_NAME);
		MatchObjectElement[] objects = new MatchObjectElement[objectEls.length];
		for (int i = 0; i < objectEls.length; i++) {
			objects[i] = new MatchObjectElement(objectEls[i]);
		}
		fObjects = objects;
	}

	public MatchObjectElement[] getMatchObjects() {
		return fObjects.clone();
	}

}
