/*******************************************************************************
 * Copyright (c) 2006, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX - Initial API and implementation
 *     Andrew Ferguson (Symbian)
 *     Markus Schorn (Wind River Systems)
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
	 */
	public long getRecord();
	
	// strcmp equivalents
	/**
	 * Compare this IString record and the specified IString record
	 * @param string
	 * @param caseSensitive whether to compare in a case-sensitive way
	 * @return <ul><li> -1 if this &lt; string
	 * <li> 0 if this == string
	 * <li> 1 if this &gt; string
	 * </ul>
	 * @throws CoreException
	 */
	public int compare(IString string, boolean caseSensitive) throws CoreException;
	
	/**
	 * Compare this IString record and the specified String object
	 * @param string
	 * @param caseSensitive whether to compare in a case-sensitive way
	 * @return <ul><li> -1 if this &lt; string
	 * <li> 0 if this == string
	 * <li> 1 if this &gt; string
	 * </ul>
	 * @throws CoreException
	 */
	public int compare(String string, boolean caseSensitive) throws CoreException;
	
	/**
	 * Compare this IString record and the specified character array
	 * @param chars
	 * @param caseSensitive whether to compare in a case-sensitive way
	 * @return <ul><li> -1 if this &lt; chars
	 * <li> 0 if this == chars
	 * <li> 1 if this &gt; chars
	 * </ul>
	 * @throws CoreException
	 */
	public int compare(char[] chars, boolean caseSensitive) throws CoreException;

	/**
	 * Compare this IString record and the specified IString record in a case sensitive manner
	 * such that it is compatible with case insensitive comparison.
	 * @param string
	 * @return <ul><li> -1 if this &lt; string
	 * <li> 0 if this == string
	 * <li> 1 if this &gt; string
	 * </ul>
	 * @throws CoreException
	 */
	public int compareCompatibleWithIgnoreCase(IString string) throws CoreException;

	/**
	 * Compare this IString record and the specified char array in a case sensitive manner
	 * such that it is compatible with case insensitive comparison.
	 * @param chars
	 * @return <ul><li> -1 if this &lt; string
	 * <li> 0 if this == string
	 * <li> 1 if this &gt; string
	 * </ul>
	 * @throws CoreException
	 */
	public int compareCompatibleWithIgnoreCase(char[] chars) throws CoreException;
	
	/**
	 * Compare this IString record and the specified character array
	 * @param name the name to compare to
	 * @param caseSensitive whether to compare in a case-sensitive way
	 * @return <ul><li> -1 if this &lt; chars
	 * <li> 0 if this has a prefix chars
	 * <li> 1 if this &gt; chars and does not have the prefix
	 * </ul>
	 * @throws CoreException
	 */
	public int comparePrefix(char[] name, boolean caseSensitive) throws CoreException;

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
