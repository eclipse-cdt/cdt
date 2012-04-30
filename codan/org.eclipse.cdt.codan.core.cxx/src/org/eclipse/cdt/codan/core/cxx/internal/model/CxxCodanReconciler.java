/*******************************************************************************
 * Copyright (c) 2009, 2012 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alena Laskavaia  - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.codan.core.cxx.internal.model;

import org.eclipse.cdt.codan.internal.core.CodanRunner;
import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author Alena
 */
public class CxxCodanReconciler {

	public void reconciledAst(IASTTranslationUnit ast, IResource resource, IProgressMonitor monitor) {
		if (ast == null)
			return;
		IProject project = resource.getProject();
		if (project == null)
			return;
		try {
			if (project.hasNature(CProjectNature.C_NATURE_ID) || project.hasNature(CCProjectNature.CC_NATURE_ID)) {
				CodanRunner.runInEditor(ast, resource, monitor);
			}
		} catch (CoreException e) {
			// ignore
		}
	}
}
