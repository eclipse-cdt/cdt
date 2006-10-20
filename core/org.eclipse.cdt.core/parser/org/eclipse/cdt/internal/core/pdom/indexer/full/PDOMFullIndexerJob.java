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

package org.eclipse.cdt.internal.core.pdom.indexer.full;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMIndexer;
import org.eclipse.cdt.core.dom.IPDOMIndexerTask;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.index.IIndexFragmentFile;
import org.eclipse.cdt.internal.core.index.IWritableIndex;
import org.eclipse.cdt.internal.core.index.IWritableIndexManager;
import org.eclipse.cdt.internal.core.pdom.indexer.PDOMIndexerTask;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * @author Doug Schaefer
 *
 */
abstract class PDOMFullIndexerJob extends PDOMIndexerTask implements IPDOMIndexerTask {

	protected final PDOMFullIndexer indexer;
	protected final IWritableIndex index;
		
	public PDOMFullIndexerJob(PDOMFullIndexer indexer) throws CoreException {
		this.indexer = indexer;
		this.index = ((IWritableIndexManager) CCorePlugin.getIndexManager()).getWritableIndex(indexer.getProject());
	}

	public IPDOMIndexer getIndexer() {
		return indexer;
	}
	
	protected void setupIndexAndReaderFactory() {
		// mstodo delay setting up index to here.
	}
	
	protected void doChangeTU(ITranslationUnit tu) throws CoreException, InterruptedException {
		IPath path = tu.getLocation();
		if (path == null) {
			return;
		}
		IASTTranslationUnit ast= tu.getAST(null, ITranslationUnit.AST_SKIP_IF_NO_BUILD_INFO);
		if (ast == null)
			return;
		
		index.acquireWriteLock(0);
		
		try {
			// Remove the old symbols in the tu
			IIndexFragmentFile file = (IIndexFragmentFile) index.getFile(path);
			if (file != null)
				index.clearFile(file);

			// Clear out the symbols in the includes
			IASTPreprocessorIncludeStatement[] includes = ast.getIncludeDirectives();
			for (int i = 0; i < includes.length; ++i) {
				String incname = includes[i].getPath();
				IIndexFragmentFile incfile = (IIndexFragmentFile) index.getFile(new Path(incname));
				if (incfile != null) {
					index.clearFile(incfile);
				}
			}
			
			addSymbols(ast);
		} finally {
			index.releaseWriteLock(0);
		}
	}
	
	protected void addSymbols(IASTTranslationUnit ast) throws CoreException {
		// Add in the includes
		IASTPreprocessorIncludeStatement[] includes = ast.getIncludeDirectives();
		for (int i = 0; i < includes.length; ++i) {
			IASTPreprocessorIncludeStatement include = includes[i];
			
			IASTFileLocation sourceLoc = include.getFileLocation();
			String sourcePath
				= sourceLoc != null
				? sourceLoc.getFileName()
				: ast.getFilePath(); // command-line includes
				
			IIndexFragmentFile sourceFile = index.addFile(sourcePath);
			String destPath = include.getPath();
			IIndexFragmentFile destFile = index.addFile(destPath);
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
			IIndexFragmentFile sourceFile = index.addFile(filename);
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
						index.addName(index.addFile(nameLoc.getFileName()), name);
					return PROCESS_CONTINUE;
				} catch (Throwable e) {
					CCorePlugin.log(e);
					return ++fErrorCount > MAX_ERRORS ? PROCESS_ABORT : PROCESS_CONTINUE;
				}
			}
		});
	}
}
