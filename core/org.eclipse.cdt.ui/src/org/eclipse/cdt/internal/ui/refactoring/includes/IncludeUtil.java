/*******************************************************************************
 * Copyright (c) 2012, 2013 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.includes;

import static org.eclipse.cdt.internal.core.dom.parser.ASTTranslationUnit.getNodeEndOffset;
import static org.eclipse.cdt.internal.core.dom.parser.ASTTranslationUnit.getNodeOffset;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.text.IRegion;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IndexLocationFactory;

public class IncludeUtil {
	/** Not instantiatable. All methods are static. */
	private IncludeUtil() {}

	/**
	 * Checks if a file is a source file (.c, .cpp, .cc, etc). Header files are not considered
	 * source files.
	 * @return Returns {@code true} if the the file is a source file.
	 */
	public static boolean isSource(IIndexFile file, IProject project) throws CoreException {
		return isSource(getPath(file), project);
	}

	/**
	 * Checks if a file is a source file (.c, .cpp, .cc, etc). Header files are not considered
	 * source files.
	 * @return Returns {@code true} if the the file is a source file.
	 */
	public static boolean isSource(String filename, IProject project) {
		IContentType ct= CCorePlugin.getContentType(project, filename);
		if (ct != null) {
			String id = ct.getId();
			if (CCorePlugin.CONTENT_TYPE_CSOURCE.equals(id) || CCorePlugin.CONTENT_TYPE_CXXSOURCE.equals(id)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the path of the given index file.
	 * @param file The index file.
	 * @return The path.
	 */
	public static String getPath(IIndexFile file) throws CoreException {
		return getPath(file.getLocation());
	}

	/**
	 * Returns the path of the given index file.
	 * @param fileLocation The index file location.
	 * @return The path.
	 */
	public static String getPath(IIndexFileLocation fileLocation) {
		return IndexLocationFactory.getAbsolutePath(fileLocation).toOSString();
	}

	public static boolean isContainedInRegion(IASTNode node, IRegion region) {
		return getNodeOffset(node) >= region.getOffset()
				&& getNodeEndOffset(node) <= region.getOffset() + region.getLength();
	}
}
