/*******************************************************************************
 * Copyright (c) 2007, 2014 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.settings.model;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IPath;

public final class CSourceEntry extends ACExclusionFilterEntry implements ICSourceEntry {

	public CSourceEntry(IPath path, IPath exclusionPatterns[], int flags) {
		super(path, exclusionPatterns, flags | VALUE_WORKSPACE_PATH);
	}

	public CSourceEntry(IFolder rc, IPath exclusionPatterns[], int flags) {
		super(rc, exclusionPatterns, flags | VALUE_WORKSPACE_PATH);
	}

	public CSourceEntry(String value, IPath exclusionPatterns[], int flags) {
		super(value, exclusionPatterns, flags | VALUE_WORKSPACE_PATH);
	}

	@Override
	public final int getKind() {
		return SOURCE_PATH;
	}
}
