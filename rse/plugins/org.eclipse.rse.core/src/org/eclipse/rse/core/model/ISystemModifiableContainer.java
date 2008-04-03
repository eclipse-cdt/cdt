/*********************************************************************************
 * Copyright (c) 2008 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * David Dykstal (IBM) - [224671] [api] org.eclipse.rse.core API leaks non-API types
 *********************************************************************************/

package org.eclipse.rse.core.model;

/**
 * A modifiable container allows its contents to be set directly.
 */
public interface ISystemModifiableContainer extends ISystemContainer {
	
	/**
	 * Cache contents of a certain type.
	 * @param type the contents type.
	 * @param cachedContents the contents to cache.
	 */
	public void setContents(ISystemContentsType type, Object[] cachedContents);

}
