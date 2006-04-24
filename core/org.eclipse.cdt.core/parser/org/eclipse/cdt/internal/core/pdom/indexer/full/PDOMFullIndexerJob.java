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

package org.eclipse.cdt.internal.core.pdom.indexer.full;

import org.eclipse.cdt.core.CCorePlugin;
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
public abstract class PDOMFullIndexerJob extends Job {

	protected final PDOM pdom;
	
	public PDOMFullIndexerJob(PDOM pdom) {
		super("Full Indexer: " + pdom.getProject().getElementName());
		this.pdom = pdom;
		setRule(CCorePlugin.getPDOMManager().getIndexerSchedulingRule());
	}

	protected IASTTranslationUnit parse(ITranslationUnit tu) throws CoreException {
		ILanguage language = tu.getLanguage();
		if (language == null)
			return null;
		
		// get the AST in the "Full" way, i.e. don't skip anything.
		return language.getASTTranslationUnit(tu, ILanguage.AST_SKIP_IF_NO_BUILD_INFO);
	}

	protected void addTU(ITranslationUnit tu) throws InterruptedException, CoreException {
		IASTTranslationUnit ast = parse(tu);
		if (ast == null)
			return;
		
		pdom.acquireWriteLock();
		try {
			pdom.addSymbols(tu.getLanguage(), ast);
		} finally {
			pdom.releaseWriteLock();
		}
	}
	
}
