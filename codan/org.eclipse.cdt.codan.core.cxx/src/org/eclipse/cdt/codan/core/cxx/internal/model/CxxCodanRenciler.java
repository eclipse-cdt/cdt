/*******************************************************************************
 * Copyright (c) 2009 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.cxx.internal.model;

import java.io.File;
import java.net.URI;

import org.eclipse.cdt.codan.internal.core.CodanBuilder;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author Alena
 * 
 */

public class CxxCodanRenciler  {
	private CodanBuilder builder = new CodanBuilder();

	public void reconciledAst(IASTTranslationUnit ast, IProgressMonitor monitor) {
		if (ast == null)
			return;
		String filePath = ast.getFilePath();
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		IFile[] resources;
		URI uri = new File(filePath).toURI();
		resources = root.findFilesForLocationURI(uri);
		if (resources != null && resources.length > 0) {
			IFile resource = resources[0];
			builder.runInEditor(ast, resource, monitor);
		}
	}
}
