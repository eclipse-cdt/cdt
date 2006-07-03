/*******************************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.search;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ISourceRange;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;

/**
 * @author Doug Schaefer
 *
 */
public class PDOMSearchElementQuery extends PDOMSearchQuery {

	private ISourceReference element;
	
	public PDOMSearchElementQuery(ICElement[] scope, ISourceReference element, int flags) {
		super(scope, flags);
		this.element = element;
	}

	public IStatus run(IProgressMonitor monitor) throws OperationCanceledException {
		try {
			ISourceRange range = element.getSourceRange();
			ITranslationUnit tu = element.getTranslationUnit();
			ILanguage language = tu.getLanguage();
			IASTTranslationUnit ast = language.getASTTranslationUnit(tu, ILanguage.AST_SKIP_ALL_HEADERS | ILanguage.AST_USE_INDEX);
			IASTName[] names = language.getSelectedNames(ast, range.getIdStartPos(), range.getIdLength());
			
			for (int i = 0; i < names.length; ++i) {
				IBinding binding = names[i].resolveBinding();
				createMatches(language, binding);
			}
			return Status.OK_STATUS;
		} catch (CoreException e) {
			return e.getStatus();
		}
	}

	public String getLabel() {
		if (element instanceof ICElement)
			return super.getLabel() + " " + ((ICElement)element).getElementName(); //$NON-NLS-1$
		else
			return super.getLabel() + " something.";
	}
}
