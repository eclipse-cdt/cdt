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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
import org.eclipse.cdt.internal.core.pdom.PDOMCodeReaderFactory;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMFile;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.jobs.Job;

/**
 * @author Doug Schaefer
 *
 */
public abstract class PDOMFastIndexerJob extends Job {

	protected final Map fileMap = new HashMap();
	protected final PDOM pdom;
	
	public PDOMFastIndexerJob(PDOM pdom) {
		super("Fast Indexer: " + pdom.getProject().getElementName());
		this.pdom = pdom;
		setRule(CCorePlugin.getPDOMManager().getIndexerSchedulingRule());
	}

	protected PDOMFile getCachedFile(String filename) throws CoreException {
		PDOMFile file = (PDOMFile)fileMap.get(filename);
		if (file == null) {
			file = pdom.addFile(filename);
			fileMap.put(filename, file);
		}
		return file;
	}
	
	protected void addTU(ITranslationUnit tu) throws InterruptedException, CoreException {
		ILanguage language = tu.getLanguage();
		if (language == null)
			return;
	
		PDOMCodeReaderFactory codeReaderFactory = new PDOMCodeReaderFactory(pdom);
		
		// get the AST in a "Fast" way
		IASTTranslationUnit ast = language.getASTTranslationUnit(tu,
				codeReaderFactory,
				ILanguage.AST_USE_INDEX	| ILanguage.AST_SKIP_IF_NO_BUILD_INFO);
		if (ast == null)
			return;

		pdom.acquireWriteLock();
		try {
			addSymbols(language, ast, codeReaderFactory.getSkippedHeaders());
		} finally {
			pdom.releaseWriteLock();
		}
		
		// Tell the world
		pdom.fireChange();
	}

	protected void addSymbols(ILanguage language, IASTTranslationUnit ast, Set skippedHeaders) throws InterruptedException, CoreException {
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
				
			PDOMFile sourceFile = getCachedFile(sourcePath);
			String destPath = include.getPath();
			PDOMFile destFile = getCachedFile(destPath);
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
			if (skippedHeaders.contains(filename))
				continue;

			PDOMFile sourceFile = getCachedFile(filename);
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
						linkage.addName(name, getCachedFile(nameLoc.getFileName()));
					return PROCESS_CONTINUE;
				} catch (CoreException e) {
					CCorePlugin.log(e);
					return PROCESS_ABORT;
				}
			};
		});;
	
	}		
}
