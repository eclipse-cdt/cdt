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
	/**
	 * Get the offset of this IString record in the PDOM
	 * @return
	 */
	public int getRecord();
	
	// strcmp equivalents
	/**
	 * Compare this IString record and the specified IString record
	 * @param chars
	 * @return <ul><li> -1 if this &lt; string
	 * <li> 0 if this == string
	 * <li> 1 if this &gt; string
	 * </ul>
	 * @throws CoreException
	 */
	public int compare(IString string) throws CoreException;
	
	/**
	 * Compare this IString record and the specified String object
	 * @param chars
	 * @return <ul><li> -1 if this &lt; string
	 * <li> 0 if this == string
	 * <li> 1 if this &gt; string
	 * </ul>
	 * @throws CoreException
	 */
	public int compare(String string) throws CoreException;
	
	/**
	 * Compare this IString record and the specified character array
	 * @param chars
	 * @return <ul><li> -1 if this &lt; chars
	 * <li> 0 if this == chars
	 * <li> 1 if this &gt; chars
	 * </ul>
	 * @throws CoreException
	 */
	public int compare(char[] chars) throws CoreException;

	
	/**
	 * Compare this IString record and the specified character array
	 * @param chars
	 * @return <ul><li> -1 if this &lt; chars
	 * <li> 0 if this has a prefix chars
	 * <li> 1 if this &gt; chars and does not have the prefix
	 * </ul>
	 * @throws CoreException
	 */
	public int comparePrefix(char[] name) throws CoreException;

	/**
	 * Get an equivalent character array to this IString record<p>
	 * <b>N.B. This method can be expensive: compare and equals can be used for
	 * efficient comparisons</b>
	 * @return an equivalent character array to this IString record
	 * @throws CoreException
	 */
	public char[] getChars() throws CoreException;
	
	/**
	 * Get an equivalent String object to this IString record<p>
	 * <b>N.B. This method can be expensive: compare and equals can be used for
	 * efficient comparisons</b>
	 * @return an equivalent String object to this IString record
	 * @throws CoreException
	 */
	public String getString() throws CoreException;
	
	/**
	 * Free the associated record in the PDOM
	 * @throws CoreException
	 */
	public void delete() throws CoreException;
}
