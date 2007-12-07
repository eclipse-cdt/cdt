/*******************************************************************************
 * Copyright (c) 2005, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    QNX - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *    IBM Corporation
 *******************************************************************************/

package org.eclipse.cdt.core.model;

import org.eclipse.cdt.core.dom.ICodeReaderFactory;
import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.IASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;

/**
 * Models differences between languages. The interface is not supposed to be implemented directly.
 * Rather than that clients may subclass {@link AbstractLanguage}.
 * @author Doug Schaefer
 */
public interface ILanguage extends IAdaptable {

	//public static final QualifiedName KEY = new QualifiedName(CCorePlugin.PLUGIN_ID, "language"); //$NON-NLS-1$
	public static final String KEY = "language"; //$NON-NLS-1$

	/**
	 * Return the language id for this language.
	 * This is to differentiate languages from each other.
	 */
	public String getId();

	/**
	 * Return the id of the linkage this language contributes to. This is especially important
	 * for languages that write to the index.
	 * @see ILinkage
	 * @since 5.0
	 */
	public int getLinkageID();
	
	/**
	 * @return the human readable name corresponding to this language, suitable for display.
	 * @since 4.0
	 */
	public String getName();
	
	/**
	 * @deprecated use {@link ITranslationUnit#getAST()}.
	 */
	public IASTTranslationUnit getASTTranslationUnit(
			ITranslationUnit file,
			int style) throws CoreException;

	/**
	 * @deprecated use {@link ITranslationUnit#getAST(...)}.
	 */
	public IASTTranslationUnit getASTTranslationUnit(
			ITranslationUnit file,
			ICodeReaderFactory codeReaderFactory,
			int style) throws CoreException;

	/**
	 * Return the AST completion node for the given offset.
	 * 
	 * @param reader
	 * @param scanInfo
	 * @param fileCreator
	 * @param index
	 * @param log
	 * @param offset
	 * @return
	 * @throws CoreException
	 */
	public IASTCompletionNode getCompletionNode(CodeReader reader, IScannerInfo scanInfo, ICodeReaderFactory fileCreator, IIndex index, IParserLogService log, int offset) throws CoreException;


	/**
	 * Gather the list of IASTNames that appear the selection with the given start offset
	 * and length in the given ITranslationUnit.
	 * 
	 * @param tu
	 * @param start
	 * @param length
	 * @param style
	 * @return
	 */
	public IASTName[] getSelectedNames(IASTTranslationUnit ast, int start, int length);
	
	/**
	 * Used to override the default model building behavior for a translation unit.
	 * 
	 * @param  tu  the <code>ITranslationUnit</code> to be parsed (non-<code>null</code>)
	 * @return an <code>IModelBuilder</code>, which parses the given translation unit and
	 *         returns the <code>ICElement</code>s of its model, or <code>null</code>
	 *         to parse using the default CDT model builder
	 */
	public IContributedModelBuilder createModelBuilder(ITranslationUnit tu);

	/**
	 * Construct an AST for the source code provided by <code>reader</code>.
	 * @param reader source code to be parsed.
	 * @param scanInfo provides include paths and defined symbols.
	 * @param fileCreator factory that provides CodeReaders for files included
	 *                    by the source code being parsed.
	 * @param index (optional) index to use to provide support for ambiguity
	 *              resolution.
	 * @param log logger
	 * @return an AST for the source code provided by reader.
	 * @throws CoreException
	 */
	public IASTTranslationUnit getASTTranslationUnit(CodeReader reader, IScannerInfo scanInfo, ICodeReaderFactory fileCreator, IIndex index, IParserLogService log) throws CoreException;
}
