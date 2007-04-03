/*******************************************************************************
 * Copyright (c) 2006, 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.core.model;

import org.eclipse.cdt.core.dom.ICodeReaderFactory;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.PlatformObject;

/**
 * Models the differences between various languages.
 * @since 4.0
 */
public abstract class AbstractLanguage extends PlatformObject implements ILanguage {
	/**
	 * Option for {@link #getASTTranslationUnit(CodeReader, IScannerInfo, ICodeReaderFactory, IIndex, int, IParserLogService)}
	 * Instructs the parser to skip function and method bodies.
	 */
	public final static int OPTION_SKIP_FUNCTION_BODIES= 1;
	
	/** 
	 * @deprecated, throws an UnsupportedOperationException
	 */
	final public IASTTranslationUnit getASTTranslationUnit(ITranslationUnit file, int style) throws CoreException {
		throw new UnsupportedOperationException();
	}

	/** 
	 * @deprecated, throws an UnsupportedOperationException
	 */
	final public IASTTranslationUnit getASTTranslationUnit(ITranslationUnit file, ICodeReaderFactory codeReaderFactory,
			int style) throws CoreException {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Construct an AST for the source code provided by <code>reader</code>.
	 * As an option you can supply 
	 * @param reader source code to be parsed.
	 * @param scanInfo provides include paths and defined symbols.
	 * @param fileCreator factory that provides CodeReaders for files included
	 *                    by the source code being parsed.
	 * @param index (optional) index to use to provide support for ambiguity
	 *              resolution.
	 * @param options {@link #OPTION_SKIP_FUNCTION_BODIES} or <code>0</code>.
	 * @param log logger
	 * @return an AST for the source code provided by reader.
	 * @throws CoreException
	 */
	public IASTTranslationUnit getASTTranslationUnit(CodeReader reader, IScannerInfo scanInfo, ICodeReaderFactory fileCreator, IIndex index, int options, IParserLogService log) 
			throws CoreException {
		// for backwards compatibility
		return getASTTranslationUnit(reader, scanInfo, fileCreator, index, log);
	}
}
