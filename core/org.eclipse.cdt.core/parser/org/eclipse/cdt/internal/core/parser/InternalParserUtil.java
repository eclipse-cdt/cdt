/*******************************************************************************
 * Copyright (c) 2003, 2011 IBM Corporation and others.
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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.parser.FileContent;
import org.eclipse.cdt.core.parser.ParserFactory;
import org.eclipse.cdt.internal.core.parser.scanner.AbstractCharArray;
import org.eclipse.cdt.internal.core.parser.scanner.FileCharArray;
import org.eclipse.cdt.internal.core.parser.scanner.InternalFileContent;
import org.eclipse.cdt.internal.core.resources.PathCanonicalizationStrategy;
import org.eclipse.cdt.utils.UNCPathConverter;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
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
	public static final String SYSTEM_DEFAULT_ENCODING = System.getProperty("file.encoding"); //$NON-NLS-1$

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
	 * @deprecated, use {@link FileContent}, instead.
	 */
	@Deprecated
	public static org.eclipse.cdt.core.parser.CodeReader createExternalFileReader(String externalLocation, CodeReaderLRUCache cache) throws IOException {
		File includeFile = new File(externalLocation);
		if (includeFile.isFile()) {
		    // Use the canonical path so that in case of non-case-sensitive OSs
		    // the CodeReader always has the same name as the file on disk with
		    // no differences in case.
			final String path = PathCanonicalizationStrategy.getCanonicalPath(includeFile);
			if (cache != null) {
				org.eclipse.cdt.core.parser.CodeReader result= cache.get(path);
				if (result != null)
					return result;
			}

			return new org.eclipse.cdt.core.parser.CodeReader(path);
		}
		return null;
	}
	
	/**
	 * Creates a code reader for an external location, normalizing path to 
	 * canonical path.
	 * @deprecated, use {@link FileContent}, instead.
	 */
	@Deprecated
	public static org.eclipse.cdt.core.parser.CodeReader createWorkspaceFileReader(String path, IFile file, CodeReaderLRUCache cache) throws CoreException, IOException{
		path = normalizePath(path, file);
		if (cache != null) {
			org.eclipse.cdt.core.parser.CodeReader result= cache.get(path);
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
			return new org.eclipse.cdt.core.parser.CodeReader(path, file.getCharset(), in);
		} finally {
			try {
				in.close();
			} catch (IOException e) {
			}
		}
	}



	/**
	 * @deprecated, use {@link FileContent}, instead.
	 */
	@Deprecated
	public static org.eclipse.cdt.core.parser.CodeReader createCodeReader(IIndexFileLocation ifl, CodeReaderLRUCache cache) throws CoreException, IOException {
		String fullPath= ifl.getFullPath();
		if (fullPath != null) {
			IResource res= ResourcesPlugin.getWorkspace().getRoot().findMember(new Path(fullPath));
			if (res instanceof IFile)
				return createWorkspaceFileReader(ifl.getURI().getPath(), (IFile) res, cache);
		}
		return createExternalFileReader(ifl.getURI().getPath(), cache);
	}

	public static InternalFileContent createFileContent(IIndexFileLocation ifl) {
		String fullPath= ifl.getFullPath();
		if (fullPath != null) {
			IResource res= ResourcesPlugin.getWorkspace().getRoot().findMember(new Path(fullPath));
			if (res instanceof IFile)
				return createWorkspaceFileContent((IFile) res);
		} 
		/*
		 * If URI refers to a remote resource, use the full URI to retrieve the file content
		 * Otherwise, assume it is in the local filesystem somewhere
		 */
		String scheme = ifl.getURI().getScheme();
		if (!scheme.equals(EFS.SCHEME_FILE)) {
			return createExternalFileContent(UNCPathConverter.toPath(ifl.getURI()).toString(), SYSTEM_DEFAULT_ENCODING);
		}
		return createExternalFileContent(ifl.getURI().getPath(), SYSTEM_DEFAULT_ENCODING);
	}
	
	public static InternalFileContent createWorkspaceFileContent(IFile file) {
		String path= file.getLocationURI().getPath();
		path= normalizePath(path, file);

		InputStream input;
		try {
			long fileReadTime = System.currentTimeMillis();
			IFileStore store = EFS.getStore(file.getLocationURI());
			IFileInfo fileInfo = store.fetchInfo();
			input= file.getContents(true);
			if (!(input instanceof FileInputStream)) {
				/*
				 * In general, non-local file-systems will not use FileInputStream.
				 * Instead make a cached copy of the file and open an input stream to that.
				 */
				File fileCache = store.toLocalFile(EFS.CACHE, null);
				try {
					input = new FileInputStream(fileCache);
				} catch (FileNotFoundException e) {
					CCorePlugin.log(e);
					return null;
				}
			}
			try {
				return createFileContent(path, file.getCharset(), input,
						fileInfo.getLastModified(), fileInfo.getLength(), fileReadTime);
			} finally {
				try {
					input.close();
				} catch (IOException e) {
				}
			}
		} catch (CoreException e) {
			switch (e.getStatus().getCode()) {
			case IResourceStatus.NOT_FOUND_LOCAL:
			case IResourceStatus.NO_LOCATION_LOCAL:
			case IResourceStatus.FAILED_READ_LOCAL:
			case IResourceStatus.RESOURCE_NOT_LOCAL:
			case IResourceStatus.RESOURCE_NOT_FOUND:
				break;
			default:
				CCorePlugin.log(e);
				break;
			}
			return null;
		}
	}

	/**
	 * Creates a code reader for an external location, normalizing path to 
	 * canonical path. 
	 */
	public static InternalFileContent createExternalFileContent(String externalLocation, String encoding) {
		long fileReadTime = System.currentTimeMillis();
		File includeFile = null;
		String path = null;
		if (!UNCPathConverter.isUNC(externalLocation)) {
			includeFile = new File(externalLocation);
		    // Use the canonical path so that in case of non-case-sensitive OSs
		    // the CodeReader always has the same name as the file on disk with
		    // no differences in case.
			path = PathCanonicalizationStrategy.getCanonicalPath(includeFile);
		} else {
			try {
				IFileStore store = EFS.getStore(UNCPathConverter.getInstance().toURI(externalLocation));
				includeFile = store.toLocalFile(EFS.CACHE, null);
				path = externalLocation;
			} catch (CoreException e) {
			}
		}
		if (includeFile != null && includeFile.isFile()) {
			long timestamp = includeFile.lastModified();
			long fileSize = includeFile.length();
			FileInputStream in;
			try {
				in = new FileInputStream(includeFile);
			} catch (IOException e) {
				CCorePlugin.log(e);
				return null;
			}
			try {
				return createFileContent(path, encoding, in, timestamp, fileSize, fileReadTime);
			} finally {
				try {
					in.close();
				} catch (IOException e) {
				}
			}
		}
		return null;
	}

	private static InternalFileContent createFileContent(String path, String charset, InputStream in,
			long fileTimestamp, long fileSize, long fileReadTime) {
		try {
			AbstractCharArray chars= FileCharArray.create(path, charset, in);
			if (chars == null)
				return null;
			
			return new InternalFileContent(path, chars, fileTimestamp, fileSize, fileReadTime);
		} catch (IOException e) {
			CCorePlugin.log(e);
		}
		return null;
	}
}
