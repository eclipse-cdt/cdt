/*******************************************************************************
 * Copyright (c) 2008, 2010 Nokia and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Nokia (Ed Swartz) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.make.core.makefile;

import java.io.IOException;
import java.io.Reader;
import java.net.URI;

/**
 * Provide an abstraction to loading the contents of a makefile
 * @author eswartz
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IMakefileReaderProvider {
	/**
	 * Get a reader for the contents of the file at filename.
	 * @param fileURI the file to read.  It's up to the implementation how to read
	 * it, but usually EFS.getFileStore(fileURI).getInputStream(...) is the best bet.
	 * @return Reader a reader for the contents of the existing file
	 * @throws IOException if the file cannot be found according to the implementation
	 */
	Reader getReader(URI fileURI) throws IOException;
}
