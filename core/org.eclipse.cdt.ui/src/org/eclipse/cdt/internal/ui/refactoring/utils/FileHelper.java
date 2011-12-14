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
package org.eclipse.cdt.internal.ui.refactoring.utils;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import org.eclipse.cdt.core.dom.ast.IASTNode;

/**
 * Helper class concerning files.
 * 
 * @author Lukas Felber
 */
public class FileHelper {

	public static IFile getFileFromNode(IASTNode node) {
		IPath implPath = new Path(node.getContainingFilename());
		return ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(implPath);
	}
}
