/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

public class Util {
	
	private static final int DEFAULT_READING_SIZE = 8192;

	
	/**
	 * Returns the contents of the given file as a byte array.
	 * @throws IOException if a problem occured reading the file.
	 */
	public static byte[] getFileByteContent(File file) throws IOException {
		InputStream stream = null;
		try {
			stream = new BufferedInputStream(new FileInputStream(file));
			return getInputStreamAsByteArray(stream, (int) file.length());
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
				}
			}
		}
	}
	/**
	 * Returns the contents of the given file as a char array.
	 * When encoding is null, then the platform default one is used
	 * @throws IOException if a problem occured reading the file.
	 */
	public static char[] getFileCharContent(File file, String encoding) throws IOException {
		InputStream stream = null;
		try {
			stream = new BufferedInputStream(new FileInputStream(file));
			return Util.getInputStreamAsCharArray(stream, (int) file.length(), encoding);
		}
		catch (OutOfMemoryError er){
			return null;
		}
		 finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
				}
			}
		}
	}
	
	/**
	 * Returns the given input stream's contents as a byte array.
	 * If a length is specified (ie. if length != -1), only length bytes
	 * are returned. Otherwise all bytes in the stream are returned.
	 * Note this doesn't close the stream.
	 * @throws IOException if a problem occured reading the stream.
	 */
	public static byte[] getInputStreamAsByteArray(InputStream stream, int length)
		throws IOException {
		byte[] contents;
		if (length == -1) {
			contents = new byte[0];
			int contentsLength = 0;
			int amountRead = -1;
			do {
				int amountRequested = Math.max(stream.available(), DEFAULT_READING_SIZE);  // read at least 8K
				
				// resize contents if needed
				if (contentsLength + amountRequested > contents.length) {
					System.arraycopy(
						contents,
						0,
						contents = new byte[contentsLength + amountRequested],
						0,
						contentsLength);
				}

				// read as many bytes as possible
				amountRead = stream.read(contents, contentsLength, amountRequested);

				if (amountRead > 0) {
					// remember length of contents
					contentsLength += amountRead;
				}
			} while (amountRead != -1); 

			// resize contents if necessary
			if (contentsLength < contents.length) {
				System.arraycopy(
					contents,
					0,
					contents = new byte[contentsLength],
					0,
					contentsLength);
			}
		} else {
			contents = new byte[length];
			int len = 0;
			int readSize = 0;
			while ((readSize != -1) && (len != length)) {
				// See PR 1FMS89U
				// We record first the read size. In this case len is the actual read size.
				len += readSize;
				readSize = stream.read(contents, len, length - len);
			}
		}

		return contents;
	}

	/**
	 * Returns the given input stream's contents as a character array.
	 * If a length is specified (ie. if length != -1), only length chars
	 * are returned. Otherwise all chars in the stream are returned.
	 * Note this doesn't close the stream.
	 * @throws IOException if a problem occured reading the stream.
	 */
	public static char[] getInputStreamAsCharArray(InputStream stream, int length, String encoding)
		throws IOException {
		InputStreamReader reader = null;
		reader = encoding == null
					? new InputStreamReader(stream)
					: new InputStreamReader(stream, encoding);
		char[] contents;
		if (length == -1) {
			contents = CharOperation.NO_CHAR;
			int contentsLength = 0;
			int amountRead = -1;
			do {
				int amountRequested = Math.max(stream.available(), DEFAULT_READING_SIZE);  // read at least 8K

				// resize contents if needed
				if (contentsLength + amountRequested > contents.length) {
					System.arraycopy(
						contents,
						0,
						contents = new char[contentsLength + amountRequested],
						0,
						contentsLength);
				}

				// read as many chars as possible
				amountRead = reader.read(contents, contentsLength, amountRequested);

				if (amountRead > 0) {
					// remember length of contents
					contentsLength += amountRead;
				}
			} while (amountRead != -1);

			// resize contents if necessary
			if (contentsLength < contents.length) {
				System.arraycopy(
					contents,
					0,
					contents = new char[contentsLength],
					0,
					contentsLength);
			}
		} else {
			contents = new char[length];
			int len = 0;
			int readSize = 0;
			while ((readSize != -1) && (len != length)) {
				// See PR 1FMS89U
				// We record first the read size. In this case len is the actual read size.
				len += readSize;
				readSize = reader.read(contents, len, length - len);
			}
			// See PR 1FMS89U
			// Now we need to resize in case the default encoding used more than one byte for each
			// character
			if (len != length)
				System.arraycopy(contents, 0, (contents = new char[len]), 0, len);
		}

		return contents;
	}
	

	/**
	 * Helper method - returns the targeted item (IResource if internal or java.io.File if external), 
	 * or null if unbound
	 * Internal items must be referred to using container relative paths.
	 */
	public static Object getTarget(IContainer container, IPath path, boolean checkResourceExistence) {

		if (path == null) return null;
	
		// lookup - inside the container
		if (path.getDevice() == null) { // container relative paths should not contain a device 
													// (see http://dev.eclipse.org/bugs/show_bug.cgi?id=18684)
													// (case of a workspace rooted at d:\ )
			IResource resource = container.findMember(path);
			if (resource != null){
				if (!checkResourceExistence ||resource.exists()) return resource;
				return null;
			}
		}
	
		// if path is relative, it cannot be an external path
		// (see http://dev.eclipse.org/bugs/show_bug.cgi?id=22517)
		if (!path.isAbsolute()) return null; 

		// lookup - outside the container
		File externalFile = new File(path.toOSString());
		if (!checkResourceExistence) {
			return externalFile;
		} else if (existingExternalFiles.contains(externalFile)) {
			return externalFile;
		} else {
			if (externalFile.exists()) {
				// cache external file
				existingExternalFiles.add(externalFile);
				return externalFile;
			}
		}
		
		return null;	
	}
		/**
		 * A set of java.io.Files used as a cache of external jars that 
		 * are known to be existing.
		 * Note this cache is kept for the whole session.
		 */ 
		public static HashSet<File> existingExternalFiles = new HashSet<File>();
		
	/*
	 * Returns whether the given resource matches one of the exclusion patterns.
	 * 
	 * @see IClasspathEntry#getExclusionPatterns
	 */
	public final static boolean isExcluded(IResource resource, char[][] exclusionPatterns) {
		IPath path = resource.getFullPath();
		// ensure that folders are only excluded if all of their children are excluded
		if (resource.getType() == IResource.FOLDER)
			path = path.append("*"); //$NON-NLS-1$
		return isExcluded(path, exclusionPatterns);
	}
	
	/*
	 * Returns whether the given resource path matches one of the exclusion
	 * patterns.
	 * 
	 * @see IClasspathEntry#getExclusionPatterns
	 */
	public final static boolean isExcluded(IPath resourcePath, char[][] exclusionPatterns) {
		if (exclusionPatterns == null) return false;
		char[] path = resourcePath.toString().toCharArray();
		for (char[] exclusionPattern : exclusionPatterns)
			if (CharOperation.pathMatch(exclusionPattern, path, true, '/'))
				return true;
		return false;
	}
	
	/**
	 * Returns an IStatus object with severity IStatus.ERROR based on the
	 * given Throwable.
	 * @param t the Throwable that caused the error.
	 * @return an IStatus object based on the given Throwable.
	 */
	public static IStatus createStatus(Throwable t) {
		String msg= t.getMessage();
		if (msg == null) {
			msg= Messages.Util_unexpectedError; 
		}
		return new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, 0, msg, t);
	}
	
	/**
	 * Determines if [filename] is an absolute path specification on the host OS. For example, "c:\some\file"
	 * will return true on Windows, but false on UNIX. Conversely, "/some/file" will return false on Windows,
	 * true on Linux. "somefile.txt", "some/file", "./some/file", and "../some/file" will all return false on
	 * all hosts.
	 * 
	 * <p>
	 * UNC paths ("\\some\dir") are recognized as native on Windows.
	 * 
	 * @param filename
	 *            a file specification. Slashes do not need to be in native format or consistent, except for a
	 *            UNC path, where both prefix slashes must be either forward or backwards.
	 */
	public static boolean isNativeAbsolutePath(String filename) {
		if (Platform.getOS().equals(Platform.OS_WIN32)) {
			if (filename.length() > 2) {
				// "c:\some\dir"
				if (filename.charAt(1) == ':') {
					return filename.length() > 3 && isSlash(filename.charAt(2));
				}
				else {
					return filename.startsWith("\\\\") || // UNC //$NON-NLS-1$
							filename.startsWith("//"); // UNC converted to forward slashes //$NON-NLS-1$
				}
			}
			return false;
		}
		else {
			// So much simpler on Linux/UNIX (and MacOS now?)  
			return filename.length() > 1 && isSlash(filename.charAt(0));
		}
	}
	
	private static boolean isSlash(Character c) {
		return c == '\\' || c == '/';
	}
}


