/*******************************************************************************
 * Copyright (c) 2003, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Camelon (IBM Corporation) - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.ParserFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * Utility for creating code readers
 */
public class InternalParserUtil extends ParserFactory {

	/**
	 * Normalizes the path by using the location of the file, if possible.
	 */
	public static String normalizePath(String path, IFile file) {
		IPath loc= file.getLocation();
		if (loc != null) {
			path= loc.toOSString();
		}
		return path;
	}

	/**
	 * Creates a code reader for an external location, normalizing path to 
	 * canonical path.
	 */
	public static CodeReader createExternalFileReader(String externalLocation) throws IOException {
		File includeFile = new File(externalLocation);
		if (includeFile.isFile()) {
			    //use the canonical path so that in case of non-case-sensitive OSs
			    //the CodeReader always has the same name as the file on disk with
			    //no differences in case.
			return new CodeReader(includeFile.getCanonicalPath());
		}
		return null;
	}
	
	/**
	 * Creates a code reader for an external location, normalizing path to 
	 * canonical path.
	 */
	public static CodeReader createWorkspaceFileReader(String path, IFile file) throws CoreException, IOException{
		path = normalizePath(path, file);
		InputStream in= file.getContents();
		try {
			return new CodeReader(path, file.getCharset(), in);
		} finally {
			try {
				in.close();
			} catch (IOException e) {
			}
		}
	}

	public static CodeReader createCodeReader(IIndexFileLocation ifl) throws CoreException, IOException {
		String fullPath= ifl.getFullPath();
		if (fullPath != null) {
			IResource res= ResourcesPlugin.getWorkspace().getRoot().findMember(new Path(fullPath));
			if (res instanceof IFile)
				return createWorkspaceFileReader(ifl.getURI().getPath(), (IFile) res);
		}
		return createExternalFileReader(ifl.getURI().getPath());
	}
}
