/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

	public abstract void setPath(IPath path) ;
	
//	public abstract void setExcluded(boolean excluded);
	
	public abstract boolean hasCustomSettings();
	
	/**
	 * Intended for debugging purpose only.
	 */
	@Override
	@SuppressWarnings("nls")
	public String toString() {
		return "path="+getPath() + ", " + super.toString();
	}
}
