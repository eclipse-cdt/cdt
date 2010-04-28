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

import org.eclipse.cdt.codan.internal.core.CodanBuilder;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author Alena
 * 
 */
public class CxxCodanReconciler {
	private CodanBuilder builder = new CodanBuilder();

	public void reconciledAst(IASTTranslationUnit ast, IResource resource,
			IProgressMonitor monitor) {
		if (ast == null)
			return;
		builder.runInEditor(ast, resource, monitor);
	}
}
