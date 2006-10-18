/*******************************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 * Markus Schorn (Wind River Systems)
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
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.internal.core.index.IIndexFragmentFile;
import org.eclipse.cdt.internal.core.index.IWritableIndex;
import org.eclipse.cdt.internal.core.index.IWritableIndexManager;
import org.eclipse.cdt.internal.core.index.IndexBasedCodeReaderFactory;
import org.eclipse.cdt.internal.core.pdom.indexer.PDOMIndexerTask;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

/**
 * @author Doug Schaefer
 *
 */
abstract class PDOMFastIndexerJob extends PDOMIndexerTask implements IPDOMIndexerTask {

	protected final PDOMFastIndexer indexer;
	protected IWritableIndex index;
	protected IndexBasedCodeReaderFactory codeReaderFactory;
	
	public PDOMFastIndexerJob(PDOMFastIndexer indexer) throws CoreException {
		this.indexer = indexer;
	}
	
	protected void setupIndexAndReaderFactory() throws CoreException {
		this.index= ((IWritableIndexManager) CCorePlugin.getIndexManager()).getWritableIndex(indexer.getProject());
		this.codeReaderFactory = new IndexBasedCodeReaderFactory(index);
	}		

	public IPDOMIndexer getIndexer() {
		return indexer;
	}
	
	protected void doChangeTU(ITranslationUnit tu) throws CoreException, InterruptedException {
		IPath path = tu.getLocation();
		if (path == null) {
			return;
		}
		ILanguage language = tu.getLanguage();
		if (language == null)
			return;
	
		// skip if no scanner info
		IScannerInfo scanner= tu.getScannerInfo(false);
		CodeReader codeReader = tu.getCodeReader();
		if (scanner == null || codeReader == null) {
			return;
		}

		index.acquireReadLock();
		try {
			// get the AST in a "Fast" way
			IASTTranslationUnit ast= language.getASTTranslationUnit(codeReader, scanner, codeReaderFactory, index);

			index.acquireWriteLock(1);
			try {
				// Clear the macros
				codeReaderFactory.clearMacros();
				
				// Remove the old symbols in the tu
				IIndexFragmentFile file= (IIndexFragmentFile) index.getFile(path);
				if (file != null)
					index.clearFile(file);

				// Add the new symbols
				addSymbols(ast);
			} finally {
				index.releaseWriteLock(1);
			}
		}
		finally {
			index.releaseReadLock();
		}
	}

	protected void addSymbols(IASTTranslationUnit ast) throws InterruptedException, CoreException {
		// Add in the includes
		IASTPreprocessorIncludeStatement[] includes = ast.getIncludeDirectives();
		for (int i = 0; i < includes.length; ++i) {
			IASTPreprocessorIncludeStatement include = includes[i];
			
			IASTFileLocation sourceLoc = include.getFileLocation();
			String sourcePath
				= sourceLoc != null
				? sourceLoc.getFileName()
				: ast.getFilePath(); // command-line includes
				
			IIndexFragmentFile sourceFile = codeReaderFactory.createCachedFile(index, sourcePath);
			String destPath = include.getPath();
			IIndexFragmentFile destFile = codeReaderFactory.createCachedFile(index, destPath);
			index.addInclude(sourceFile, destFile);
		}
	
		// Add in the macros
		IASTPreprocessorMacroDefinition[] macros = ast.getMacroDefinitions();
		for (int i = 0; i < macros.length; ++i) {
			IASTPreprocessorMacroDefinition macro = macros[i];
			
			IASTFileLocation sourceLoc = macro.getFileLocation();
			if (sourceLoc == null)
				continue; // skip built-ins and command line macros
				
			String filename = sourceLoc.getFileName();
			IIndexFragmentFile sourceFile = codeReaderFactory.createCachedFile(index, filename);
			index.addMacro(sourceFile, macro);
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
						index.addName(codeReaderFactory.createCachedFile(index, nameLoc.getFileName()), name);
					return PROCESS_CONTINUE;
				} catch (Throwable e) {
					CCorePlugin.log(e);
					return ++fErrorCount > MAX_ERRORS ? PROCESS_ABORT : PROCESS_CONTINUE;
				}
			}
		});
	
	}		
}
