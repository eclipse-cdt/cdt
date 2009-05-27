/*********************************************************************************
 * Copyright (c) 2008, 2009 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * David Dykstal (IBM) - [224671] [api] org.eclipse.rse.core API leaks non-API types
 * David Dykstal (IBM) - [226561] Add API markup to RSE javadocs for extend / implement
 * David Dykstal (IBM) - [261486][api] add noextend to interfaces that require it
 *********************************************************************************/

package org.eclipse.rse.core.model;

/**
 * A modifiable container allows its contents to be set directly.
 * 
 * @noimplement This interface is not intended to be implemented by clients. The
 *              standard implementations are included in the framework.
 * @noextend This interface is not intended to be extended by clients.
 *
 * @since org.eclipse.rse.core 3.0
 */
public interface ISystemModifiableContainer extends ISystemContainer {

	/**
	 * Cache contents of a certain type.
	 * @param type the contents type.
	 * @param cachedContents the contents to cache.
	 */
	public void setContents(ISystemContentsType type, Object[] cachedContents);

}
