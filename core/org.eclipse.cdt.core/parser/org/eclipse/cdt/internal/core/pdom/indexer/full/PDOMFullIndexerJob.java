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
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMFile;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.jobs.Job;

/**
 * @author Doug Schaefer
 *
 */
public abstract class PDOMFullIndexerJob extends Job {

	protected final PDOM pdom;
	
	// Error count, bail when it gets too high
	protected int errorCount;
	protected final int MAX_ERRORS = 10;
	
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
			addSymbols(tu.getLanguage(), ast);
		} finally {
			pdom.releaseWriteLock();
		}
	}
	
	public void addSymbols(ILanguage language, IASTTranslationUnit ast) throws CoreException {
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
				
			PDOMFile sourceFile = pdom.addFile(sourcePath);
			String destPath = include.getPath();
			PDOMFile destFile = pdom.addFile(destPath);
			sourceFile.addIncludeTo(destFile);
		}
	
		// Add in the macros
		IASTPreprocessorMacroDefinition[] macros = ast.getMacroDefinitions();
		for (int i = 0; i < macros.length; ++i) {
			IASTPreprocessorMacroDefinition macro = macros[i];
			
			IASTFileLocation sourceLoc = macro.getFileLocation();
			if (sourceLoc == null)
				continue; // skip built-ins and command line macros
			
			PDOMFile sourceFile = pdom.getFile(sourceLoc.getFileName());
			if (sourceFile != null) // not sure why this would be null
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
					IASTFileLocation fileloc = name.getFileLocation();
					if (fileloc != null) {
						PDOMFile file = pdom.addFile(fileloc.getFileName());
						linkage.addName(name, file);
					}
					return PROCESS_CONTINUE;
				} catch (Throwable e) {
					CCorePlugin.log(e);
					return ++errorCount > MAX_ERRORS ? PROCESS_ABORT : PROCESS_CONTINUE;
				}
			};
		});;
	
		// Tell the world
		pdom.fireChange();
	}
	

}
