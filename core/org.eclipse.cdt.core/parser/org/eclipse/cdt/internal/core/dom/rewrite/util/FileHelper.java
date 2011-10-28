/*******************************************************************************
 * Copyright (c) 2008, 2011 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.rewrite.util;

import java.io.IOException;

import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.internal.core.resources.ResourceLookup;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.text.TextUtilities;

public class FileHelper {
	private static final String DEFAULT_LINE_DELIMITTER = "\n"; //$NON-NLS-1$

	public static IFile getFileFromNode(IASTNode node) {
		IPath implPath = new Path(node.getContainingFilename());
		return ResourceLookup.selectFileForLocation(implPath, null);
	}

	public static boolean isFirstWithinSecondLocation(IASTFileLocation loc1, IASTFileLocation loc2) {
		boolean isEquals = true;

		isEquals &= loc1.getFileName().equals(loc2.getFileName());
		isEquals &= loc1.getNodeOffset() >= loc2.getNodeOffset();
		isEquals &= loc1.getNodeOffset() + loc1.getNodeLength() <= loc2.getNodeOffset()
				+ loc2.getNodeLength();

		return isEquals;
	}

	public static String determineLineDelimiter(IFile file) {
		String fileContent = ""; //$NON-NLS-1$

		try {
			fileContent = FileContentHelper.getContent(file, 0);
		} catch (CoreException e) {
		} catch (IOException e) {
		}

		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject();
		IScopeContext[] scopeContext;
		if (project != null) {
			scopeContext = new IScopeContext[] { new ProjectScope(project) };
		} else {
			scopeContext = new IScopeContext[] { InstanceScope.INSTANCE };
		}
		String platformDefaultLineDelimiter = System.getProperty("line.separator", DEFAULT_LINE_DELIMITTER); //$NON-NLS-1$
		String defaultLineDelimiter = Platform.getPreferencesService().getString(Platform.PI_RUNTIME,
				Platform.PREF_LINE_SEPARATOR, platformDefaultLineDelimiter, scopeContext);
		return TextUtilities.determineLineDelimiter(fileContent.toString(), defaultLineDelimiter);
	}
}
