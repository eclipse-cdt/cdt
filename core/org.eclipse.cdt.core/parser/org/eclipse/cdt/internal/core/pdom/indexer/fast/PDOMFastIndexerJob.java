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

package org.eclipse.cdt.internal.core.pdom.indexer.fast;

import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.jobs.Job;

/**
 * @author Doug Schaefer
 *
 */
public abstract class PDOMFastIndexerJob extends Job {

	protected final PDOM pdom;
	
	public PDOMFastIndexerJob(String name, PDOM pdom) {
		super(name);
		this.pdom = pdom;
	}

	protected void addTU(ITranslationUnit tu) throws InterruptedException, CoreException {
		ILanguage language = tu.getLanguage();
		if (language == null)
			return;
	
		// get the AST in a "Fast" way
		IASTTranslationUnit ast = language.getASTTranslationUnit(tu,
				ILanguage.AST_USE_INDEX
						| ILanguage.AST_SKIP_INDEXED_HEADERS
						| ILanguage.AST_SKIP_IF_NO_BUILD_INFO);
		if (ast == null)
			return;
		
		pdom.acquireWriteLock();
		try {
			pdom.addSymbols(language, ast);
		} finally {
			pdom.releaseWriteLock();
		}
	}

}
