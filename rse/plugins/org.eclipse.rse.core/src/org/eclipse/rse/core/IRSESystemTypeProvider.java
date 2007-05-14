/********************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors:
 * Uwe Stieber (Wind River) - initial API and implementation.
 ********************************************************************************/
package org.eclipse.rse.core;

import org.eclipse.rse.internal.core.RSECoreRegistry;

/**
 * Dynamic RSE system types provider interface.
 * 
 * See also extension point <code>org.eclipse.rse.core.systemTypeProviders</code>
 *  
 * Clients may implement this interface.
 * 
 * @since RSE 2.0
 */
public interface IRSESystemTypeProvider {

	/**
	 * Returns a list of possible RSE system types to register
	 * at initialization of the RSE core system. The method will
	 * be called only once for each provider from {@link RSECoreRegistry}.
	 * The list of the returned RSE system types will be checked
	 * for duplicates (via the system type id). Duplicates will
	 * be dropped.
	 * 
	 * Returned system types should be subclasses of {@link AbstractRSESystemType}.
	 * 
	 * @return The list of RSE system types to register or <code>null</code>.
	 */
	public IRSESystemType[] getSystemTypesForRegistration();
}
