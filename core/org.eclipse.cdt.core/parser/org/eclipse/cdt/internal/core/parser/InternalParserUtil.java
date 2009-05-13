/*******************************************************************************
 * Copyright (c) 2003, 2009 IBM Corporation and others.
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
import org.eclipse.core.resources.IResourceStatus;
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
	 * canonical path. The cache is consulted after the path has been normalized.
	 */
	public static CodeReader createExternalFileReader(String externalLocation, CodeReaderLRUCache cache) throws IOException {
		File includeFile = new File(externalLocation);
		if (includeFile.isFile()) {
			    //use the canonical path so that in case of non-case-sensitive OSs
			    //the CodeReader always has the same name as the file on disk with
			    //no differences in case.
			final String path = includeFile.getCanonicalPath();
			if (cache != null) {
				CodeReader result= cache.get(path);
				if (result != null)
					return result;
			}

			return new CodeReader(path);
		}
		return null;
	}
	
	/**
	 * Creates a code reader for an external location, normalizing path to 
	 * canonical path.
	 */
	public static CodeReader createWorkspaceFileReader(String path, IFile file, CodeReaderLRUCache cache) throws CoreException, IOException{
		path = normalizePath(path, file);
		if (cache != null) {
			CodeReader result= cache.get(path);
			if (result != null)
				return result;
		}
		
		InputStream in;
		try {
			in= file.getContents(true);
		} catch (CoreException e) {
			switch (e.getStatus().getCode()) {
			case IResourceStatus.NOT_FOUND_LOCAL:
			case IResourceStatus.NO_LOCATION_LOCAL:
			case IResourceStatus.FAILED_READ_LOCAL:
			case IResourceStatus.RESOURCE_NOT_LOCAL:
				return null;
			}
			throw e;
		}
		try {
			return new CodeReader(path, file.getCharset(), in);
		} finally {
			try {
				in.close();
			} catch (IOException e) {
			}
		}
	}

	public static CodeReader createCodeReader(IIndexFileLocation ifl, CodeReaderLRUCache cache) throws CoreException, IOException {
		String fullPath= ifl.getFullPath();
		if (fullPath != null) {
			IResource res= ResourcesPlugin.getWorkspace().getRoot().findMember(new Path(fullPath));
			if (res instanceof IFile)
				return createWorkspaceFileReader(ifl.getURI().getPath(), (IFile) res, cache);
		}
		return createExternalFileReader(ifl.getURI().getPath(), cache);
	}
}
