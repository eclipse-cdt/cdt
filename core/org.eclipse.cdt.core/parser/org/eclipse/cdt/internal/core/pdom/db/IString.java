/*******************************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.pdom.db;

import org.eclipse.core.runtime.CoreException;

/**
 * Interface for strings stored in the database. There is more than one string
 * format. This interface hides that fact. 
 * 
 * @author Doug Schaefer
 */
public interface IString {

	public int getRecord();
	
	// strcmp equivalents
	public int compare(IString string) throws CoreException;
	public int compare(String string) throws CoreException;
	public int compare(char[] chars) throws CoreException;

	// use sparingly, these can be expensive
	public char[] getChars() throws CoreException;
	public String getString() throws CoreException;
	
	public void delete() throws CoreException;
}
