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
package org.eclipse.cdt.core.settings.model;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IPath;

public final class CSourceEntry extends ACExclusionFilterEntry implements ICSourceEntry {

	public CSourceEntry(IPath path, IPath exclusionPatterns[], int flags) {
		super(path, exclusionPatterns, flags | VALUE_WORKSPACE_PATH);
	}

	public CSourceEntry(IFolder rc, IPath exclusionPatterns[], int flags) {
		super(rc, exclusionPatterns,  flags | VALUE_WORKSPACE_PATH);
	}

	public CSourceEntry(String value, IPath exclusionPatterns[], int flags) {
		super(value, exclusionPatterns, flags | VALUE_WORKSPACE_PATH);
	}

	@Override
	public final int getKind() {
		return SOURCE_PATH;
	}
}
