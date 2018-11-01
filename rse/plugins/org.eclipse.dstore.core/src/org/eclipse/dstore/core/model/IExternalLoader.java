/********************************************************************************
 * Copyright (c) 2007 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License 2.0 which accompanies this distribution, and is 
 * available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/
package org.eclipse.dstore.core.model;

public interface IExternalLoader {

	
	/**
	 * Indicates whether this external loader can load a particular class
	 * @param source a qualified classname
	 * @return true if it can load the clas
	 */
	public boolean canLoad(String source);
	
	/**
	 * Loads the specified class
	 * @param source a qualified classname
	 * @return the loaded class
	 * @throws ClassNotFoundException
	 */
	public Class loadClass(String source) throws ClassNotFoundException;

}
