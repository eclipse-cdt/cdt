/*******************************************************************************
 * Copyright (c) 2012 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alex Ruiz  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.cxx.util;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;

/**
 * Utility methods related to C++ file types.
 *
 * @author alruiz@google.com (Alex Ruiz)
 */
public final class FileTypes {
	private static final String[] CPP_FILE_EXTENSIONS = {
		"cc",   //$NON-NLS-1$
		"cpp",  //$NON-NLS-1$
		"cxx"   //$NON-NLS-1$
	};
	private static final String[] HEADER_FILE_EXTENSIONS = { "h" }; //$NON-NLS-1$

	/**
	 * Indicates whether the given <code>{@link IResource}</code> is a C++ file.
	 * @param resource the {@code IResource} to check.
	 * @return {@code true} if the given {@code IResource} is a C++ file; {@code false} otherwise.
	 */
	public static boolean isCppFile(IResource resource) {
		return isFileWithExtension(resource, CPP_FILE_EXTENSIONS);
	}

	/**
	 * Indicates whether the given <code>{@link IResource}</code> is a header file.
	 * @param resource the {@code IResource} to check.
	 * @return {@code true} if the given {@code IResource} is a header file; {@code false}
	 *         otherwise.
	 */
	public static boolean isHeaderFile(IResource resource) {
		return isFileWithExtension(resource, HEADER_FILE_EXTENSIONS);
	}

	private static boolean isFileWithExtension(IResource resource, String[] validExtensions) {
		if (!(resource instanceof IFile)) {
			return false;
		}
		IPath path = resource.getFullPath();
		return doesPathHaveExtension(path, validExtensions);
	}

	private static boolean doesPathHaveExtension(IPath path, String[] validExtensions) {
		String fileExtension = path.getFileExtension();
		for (String extension : validExtensions) {
			if (extension.equalsIgnoreCase(fileExtension)) {
				return true;
			}
		}
		return false;
	}

	private FileTypes() {}
}
