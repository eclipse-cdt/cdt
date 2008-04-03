/*********************************************************************************
 * Copyright (c) 2008 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * David Dykstal (IBM) - [224671] [api] org.eclipse.rse.core API leaks non-API types
 *********************************************************************************/

package org.eclipse.rse.core.filters;

import org.eclipse.rse.internal.core.filters.SystemFilterSimple;

/**
 * Utilities to be used in the construction and manipulation of filters.
 * @noextend
 */
public class SystemFilterUtil {
	
	public static ISystemFilter makeSimpleFilter(String name) {
		return new SystemFilterSimple(name);
	}

}
