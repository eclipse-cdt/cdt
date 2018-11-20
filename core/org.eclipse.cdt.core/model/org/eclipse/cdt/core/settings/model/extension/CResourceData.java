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
package org.eclipse.cdt.core.settings.model.extension;

import org.eclipse.core.runtime.IPath;

public abstract class CResourceData extends CDataObject {

	CResourceData() {
	}

	public abstract IPath getPath();

	//	public abstract boolean isExcluded();

	public abstract void setPath(IPath path);

	//	public abstract void setExcluded(boolean excluded);

	public abstract boolean hasCustomSettings();

	/**
	 * Intended for debugging purpose only.
	 */
	@Override
	@SuppressWarnings("nls")
	public String toString() {
		return "path=" + getPath() + ", " + super.toString();
	}
}
