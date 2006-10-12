/*******************************************************************************
 * Copyright (c) 2005, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 * Markus Schorn (Wind River Systems)
 *******************************************************************************/

package org.eclipse.cdt.core.model;

import org.eclipse.cdt.core.dom.ICodeReaderFactory;
import org.eclipse.cdt.core.dom.ast.ASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;

/**
 * Models differences between languages. The interace is not supposed to be implemented directly.
 * Rather than that clients may subclass {@link AbstractLanguage}.
 * @author Doug Schaefer
 */
public interface ILanguage extends IAdaptable {

	//public static final QualifiedName KEY = new QualifiedName(CCorePlugin.PLUGIN_ID, "language"); //$NON-NLS-1$
	public static final String KEY = "language"; //$NON-NLS-1$

	/**
	 * @deprecated has no effect.
	 */
	public static final int AST_USE_INDEX = 1;

	/**
	 * @deprecated use {@link ITranslationUnit#AST_SKIP_ALL_HEADERS}
	 */
	public static final int AST_SKIP_ALL_HEADERS = ITranslationUnit.AST_SKIP_ALL_HEADERS;

	/**
	 * @deprecated use {@link ITranslationUnit#AST_SKIP_INDEXED_HEADERS}
	 */
	public static final int AST_SKIP_INDEXED_HEADERS = ITranslationUnit.AST_SKIP_INDEXED_HEADERS;

	/**
	 * @deprecated use {@link ITranslationUnit#AST_SKIP_IF_NO_BUILD_INFO}
	 */
	public static final int AST_SKIP_IF_NO_BUILD_INFO = ITranslationUnit.AST_SKIP_IF_NO_BUILD_INFO;
	
	/**
	 * Return the language id for this language.
	 * This is to differentiate languages from eachother.
	 * 
	 * @return language id
	 */
	public String getId();

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
	 * Return the AST Completion Node for the given working copy at the given
	 * offset.
	 * 
	 * @param workingCopy
	 * @param offset
	 * @return
	 */
	public ASTCompletionNode getCompletionNode(IWorkingCopy workingCopy, int offset) throws CoreException;

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

	// mstodo javadoc
	public IASTTranslationUnit getASTTranslationUnit(CodeReader reader, IScannerInfo scanInfo, ICodeReaderFactory fileCreator, IIndex index) throws CoreException;
}
