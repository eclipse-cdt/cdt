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

import org.eclipse.core.runtime.IPath;

public interface ICResourceDescription extends ICSettingContainer, ICSettingObject{
	IPath getPath();
	
//	IPath getLocation();
	
	boolean isExcluded();
	
	void setExcluded(boolean excluded) throws WriteAccessException ;
	
	void setPath(IPath path) throws WriteAccessException ;
	
	ICFolderDescription getParentFolderDescription();
	
	boolean canExclude(boolean exclude);
}
