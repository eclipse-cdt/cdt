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

import org.eclipse.cdt.managedbuilder.internal.tcmodification.ObjectSetList;

public abstract class ObjectSetListBasedDefinition {
	public static final int CONFLICT = 1;
	private ObjectSetList fList;

	protected ObjectSetListBasedDefinition(ObjectSetList list) {
		fList = list;
	}

	public ObjectSetList getObjectSetList() {
		return fList;
	}

	public abstract int getType();

}
