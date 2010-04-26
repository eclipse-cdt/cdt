/*******************************************************************************
 * Copyright (c) 2006 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nokia - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * A reader that's able to decipher debug symbol formats.
 * 
 * This initial version only returns a list of source files.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ISymbolReader {

	String[] getSourceFiles();
	
	/**
	 * Gets the source files from this symbol reader.
	 *
	 * @param monitor a progress monitor since this may be a lengthly operation
	 * @return an array of path names to the source files
	 * @since 5.2
	 */
	String[] getSourceFiles(IProgressMonitor monitor);
}
