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
package org.eclipse.cdt.core.settings.model.util;

import java.util.List;

public class PathEntryResolveInfo {
	private PathEntryResolveInfoElement[] fElements;

	public PathEntryResolveInfo(List<PathEntryResolveInfoElement> list) {
		if (list != null) {
			fElements = list.toArray(new PathEntryResolveInfoElement[list.size()]);
		} else {
			fElements = new PathEntryResolveInfoElement[0];
		}
	}

	public PathEntryResolveInfoElement[] getElements() {
		return fElements.clone();
	}
}
