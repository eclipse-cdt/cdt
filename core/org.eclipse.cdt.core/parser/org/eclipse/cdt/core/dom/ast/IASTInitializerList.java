/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.core.dom.ast;

import java.util.List;

/**
 * This is an an initializer that is a list of initializers.
 * 
 * @author Doug Schaefer
 */
public interface IASTInitializerList extends IASTInitializer {

	/**
	 * Get the list of initializers.
	 * 
	 * @return
	 */
	public List getInitializers();
}
