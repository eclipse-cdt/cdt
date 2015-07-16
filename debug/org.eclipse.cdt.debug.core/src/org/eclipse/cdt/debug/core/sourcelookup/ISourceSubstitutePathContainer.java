/*******************************************************************************
 * Copyright (c) 2015 Kichwa Coders and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Jonah Graham (Kichwa Coders) - initial API and implementation to Add support for gdb's "set substitute-path" (Bug 472765)
 *******************************************************************************/
package org.eclipse.cdt.debug.core.sourcelookup;

import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;

/**
 * A source container that maps some file system locations to other files ystem
 * locations, using GDB's "set substitute-path" to perform the mapping.
 * 
 * @since 7.8
 */
public interface ISourceSubstitutePathContainer extends ISourceContainer {

	/**
	 * Returns path to the file as it appears in the debug information generated
	 * by the C/C++ compiler.
	 * 
	 * When looking at "-file-list-exec-source-files" in GDB, this is the path
	 * that appears in the file= field, or ideally some prefix of it.
	 * 
	 * This is the value that will be passed to set substitute-path's from
	 * argument.
	 * 
	 * @return path to the file as it appears in the debug information generated
	 *         by the compiler.
	 */
	public IPath getBackendPath();

	/**
	 * Returns path to the file as GDB can find it on disk.
	 * 
	 * When looking at "-file-list-exec-source-files" in GDB, this is the path
	 * that appears in the fullname= field, or ideally some prefix of it.
	 * 
	 * This is the value that will be passed to the substitute-path's to
	 * argument.
	 * 
	 * @return path to the file as it appears where GDB can find it on disk.
	 */
	public IPath getLocalPath();
}
