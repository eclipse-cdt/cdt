/*******************************************************************************
 * Copyright (c) 2011 Anton Gorenkov 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Gorenkov - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.testsrunner.model;

/**
 * Describes the location of the test object.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ITestLocation {

	/**
	 * Returns the file name in which testing object is located.
	 * 
	 * @return file name
	 */
	public String getFile();

	/**
	 * Returns the line number on which testing object is located.
	 * 
	 * @return line number
	 */
	public int getLine();

}
