/*******************************************************************************
 * Copyright (c) 2000, 2008 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.model;

import org.eclipse.cdt.core.model.IOutputEntry;
import org.eclipse.core.runtime.IPath;

/**
 * OutputEntry
 */
public class OutputEntry extends APathEntry implements IOutputEntry {

	public OutputEntry(IPath path, IPath[] exclusionPatterns, boolean isExported) {
		super(CDT_OUTPUT, null, null, path, exclusionPatterns, isExported);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof IOutputEntry) {
			IOutputEntry otherEntry = (IOutputEntry) obj;
			if (!super.equals(otherEntry)) {
				return false;
			}
			if (path == null) {
				if (otherEntry.getPath() != null) {
					return false;
				}
			} else {
				if (!path.toString().equals(otherEntry.getPath().toString())) {
					return false;
				}
			}
			return true;
		}
		return super.equals(obj);
	}
}
