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
package org.eclipse.cdt.managedbuilder.internal.tcmodification;

public class ObjectSetList {
	private IObjectSet[] fObjectSets;

	public ObjectSetList(IObjectSet[] objects) {
		this(objects, true);
	}

	ObjectSetList(IObjectSet[] objects, boolean copy) {
		if (copy)
			objects = objects.clone();
		fObjectSets = objects;
	}

	public IObjectSet[] getObjectSets() {
		return fObjectSets.clone();
	}
}
