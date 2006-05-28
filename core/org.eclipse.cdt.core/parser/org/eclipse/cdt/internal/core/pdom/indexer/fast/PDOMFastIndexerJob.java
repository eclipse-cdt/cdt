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

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMIndexer;
import org.eclipse.cdt.core.dom.IPDOMIndexerTask;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.PDOMCodeReaderFactory;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMFile;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 *
 */
public abstract class PDOMFastIndexerJob implements IPDOMIndexerTask {

	protected final PDOMFastIndexer indexer;
	protected final PDOM pdom;
	protected final PDOMCodeReaderFactory codeReaderFactory;
	
	// Error counter. If we too many errors we bail
	protected int errorCount;
	protected final int MAX_ERRORS = 10;

	public PDOMFastIndexerJob(PDOMFastIndexer indexer) throws CoreException {
		this.indexer = indexer;
		this.pdom = (PDOM)CCorePlugin.getPDOMManager().getPDOM(indexer.getProject());
		this.codeReaderFactory = new PDOMCodeReaderFactory(pdom);
	}

	public IPDOMIndexer getIndexer() {
		return indexer;
	}
	
	protected void addTU(ITranslationUnit tu) throws InterruptedException, CoreException {
		ILanguage language = tu.getLanguage();
		if (language == null)
			return;
	
		// get the AST in a "Fast" way
		IASTTranslationUnit ast = language.getASTTranslationUnit(tu,
				codeReaderFactory,
				ILanguage.AST_USE_INDEX	| ILanguage.AST_SKIP_IF_NO_BUILD_INFO);
		if (ast == null)
			return;
		
		// Clear the macros
		codeReaderFactory.clearMacros();

		pdom.acquireWriteLock();
		try {
			addSymbols(language, ast);
		} finally {
			pdom.releaseWriteLock();
		}
	}

	protected void addSymbols(ILanguage language, IASTTranslationUnit ast) throws InterruptedException, CoreException {
		final PDOMLinkage linkage = pdom.getLinkage(language);
		if (linkage == null)
			return;
			
		// Add in the includes
		IASTPreprocessorIncludeStatement[] includes = ast.getIncludeDirectives();
		for (int i = 0; i < includes.length; ++i) {
			IASTPreprocessorIncludeStatement include = includes[i];
			
			IASTFileLocation sourceLoc = include.getFileLocation();
			String sourcePath
				= sourceLoc != null
				? sourceLoc.getFileName()
				: ast.getFilePath(); // command-line includes
				
			PDOMFile sourceFile = codeReaderFactory.getCachedFile(sourcePath);
			String destPath = include.getPath();
			PDOMFile destFile = codeReaderFactory.getCachedFile(destPath);
			sourceFile.addIncludeTo(destFile);
		}
	
		// Add in the macros
		IASTPreprocessorMacroDefinition[] macros = ast.getMacroDefinitions();
		for (int i = 0; i < macros.length; ++i) {
			IASTPreprocessorMacroDefinition macro = macros[i];
			
			IASTFileLocation sourceLoc = macro.getFileLocation();
			if (sourceLoc == null)
				continue; // skip built-ins and command line macros
				
			String filename = sourceLoc.getFileName();
			PDOMFile sourceFile = codeReaderFactory.getCachedFile(filename);
			sourceFile.addMacro(macro);
		}
			
		// Add in the names
		ast.accept(new ASTVisitor() {
			{
				shouldVisitNames = true;
				shouldVisitDeclarations = true;
			}
			public int visit(IASTName name) {
				try {
					IASTFileLocation nameLoc = name.getFileLocation();
					if (nameLoc != null)
						linkage.addName(name, codeReaderFactory.getCachedFile(nameLoc.getFileName()));
					return PROCESS_CONTINUE;
				} catch (Throwable e) {
					CCorePlugin.log(e);
					return ++errorCount > MAX_ERRORS ? PROCESS_ABORT : PROCESS_CONTINUE;
				}
			}
		});
	
	}		
}
