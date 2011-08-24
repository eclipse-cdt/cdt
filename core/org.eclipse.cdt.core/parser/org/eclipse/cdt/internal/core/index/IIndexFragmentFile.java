/*******************************************************************************
 * Copyright (c) 2006, 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Andrew Ferguson (Symbian)
 *     Sergey Prigogin (Google)
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.index;

import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.core.runtime.CoreException;

import java.util.Map;

public interface IIndexFragmentFile extends IIndexFile {
	IIndexFragmentFile[] EMPTY_ARRAY = {};

	/**
	 * Returns the fragment that owns this file.
	 */
	IIndexFragment getIndexFragment();

	/**
	 * Sets the timestamp of the file
	 */
	void setTimestamp(long timestamp) throws CoreException;

	/**
	 * Sets the hash of the file content. 
	 */
	void setContentsHash(long hash) throws CoreException;

	/**
	 * Sets the hash-code of the scanner configuration.
	 * @param hashcode a hash-code or <code>0</code> if it is unknown.
	 * @throws CoreException 
	 */
	void setScannerConfigurationHashcode(int hashcode) throws CoreException;

	/**
	 * Sets the hash-code of the file encoding.
	 * @param hashcode a hash-code or <code>0</code> if it is unknown.
	 */
	void setEncodingHashcode(int hashcode) throws CoreException;

	/**
	 * Sets the names and definitions of the macros that affect the preprocessed contents of
	 * the file or the files it includes. Undefined macros have <code>null</code> values in the map.
	 */
	void setSignificantMacros(Map<String, String> macros) throws CoreException;

	/**
	 * Sets the name of the macro that, if defined, makes the preprocessed contents of the file
	 * not contain any code irrespectively of other macros. Returns <code>null</code> if the file
	 * does not have the include guard macro.
	 */
	void setPragmaOnceSemantics(boolean value) throws CoreException;

	/**
	 * Returns whether this file contains content in its
	 * associated fragment. Files without content are inserted to track includes.
	 */
	boolean hasContent() throws CoreException;

	/**
	 * Returns the id of the linkage this file belongs to.
	 */
	int getLinkageID() throws CoreException;
}
