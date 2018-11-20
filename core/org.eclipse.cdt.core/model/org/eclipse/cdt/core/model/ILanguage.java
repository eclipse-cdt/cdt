/*******************************************************************************
 * Copyright (c) 2005, 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Doug Schaefer (QNX) - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *    IBM Corporation
 *******************************************************************************/
package org.eclipse.cdt.core.model;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationListOwner;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.parser.FileContent;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IncludeFileContentProvider;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;

/**
 * Models differences between languages. The interface is not supposed to be implemented directly.
 * Rather than that clients may subclass {@link AbstractLanguage}.
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface ILanguage extends IAdaptable {
	/**
	 * Option for {@link #getASTTranslationUnit(FileContent, IScannerInfo, IncludeFileContentProvider, IIndex, int, IParserLogService)}
	 * Instructs the parser to skip function and method bodies.
	 */
	public final static int OPTION_SKIP_FUNCTION_BODIES = 0x1;

	/**
	 * @deprecated, Has no effect.
	 * @noreference This field is not intended to be referenced by clients.
	 */
	@Deprecated
	public final static int OPTION_ADD_COMMENTS = 0x2;

	/**
	 * Option for {@link #getASTTranslationUnit(FileContent, IScannerInfo, IncludeFileContentProvider, IIndex, int, IParserLogService)}
	 * Performance optimization, allows the parser not to create image-locations.
	 * When using this option {@link IASTName#getImageLocation()} will always return <code>null</code>.
	 */
	public final static int OPTION_NO_IMAGE_LOCATIONS = 0x4;

	/**
	 * @deprecated, Has no effect.
	 */
	@Deprecated
	public final static int OPTION_IS_SOURCE_UNIT = 0x8;

	/**
	 * Option for {@link #getASTTranslationUnit(FileContent, IScannerInfo, IncludeFileContentProvider, IIndex, int, IParserLogService)}
	 * Allows the parser not to create ast nodes for expressions within aggregate initializers
	 * when they do not contain names.
	 * @since 5.1
	 */
	public final static int OPTION_SKIP_TRIVIAL_EXPRESSIONS_IN_AGGREGATE_INITIALIZERS = 0x10;

	/**
	 * Option for {@link #getASTTranslationUnit(FileContent, IScannerInfo, IncludeFileContentProvider, IIndex, int, IParserLogService)}
	 * Instructs the parser to create ast nodes for inactive code branches, if possible. The parser
	 * makes its best effort to create ast for the inactive code branches but may decide to skip parts
	 * of the inactive code (e.g. function bodies, entire code branches, etc.).
	 * <p>
	 * The inactive nodes can be accessed via {@link IASTDeclarationListOwner#getDeclarations(boolean)} or
	 * by using a visitor with {@link ASTVisitor#includeInactiveNodes} set to <code>true</code>.
	 *
	 * @since 5.1
	 */
	public final static int OPTION_PARSE_INACTIVE_CODE = 0x20;

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
	 * Returns the human readable name corresponding to this language, suitable for display.
	 * @since 4.0
	 */
	public String getName();

	/**
	 * Constructs an AST for the source code provided by <code>reader</code>.
	 *
	 * @param content source code to be parsed.
	 * @param scanInfo provides include paths and defined symbols.
	 * @param fileCreator factory that provides file content for files included
	 * @param index (optional) index to use to lookup symbols external to the translation unit.
	 * @param options A combination of {@link #OPTION_SKIP_FUNCTION_BODIES},
	 *     {@link #OPTION_NO_IMAGE_LOCATIONS}, or <code>0</code>.
	 * @param log logger
	 * @return an AST for the source code provided by reader.
	 * @throws CoreException
	 * @since 5.2
	 */
	public IASTTranslationUnit getASTTranslationUnit(FileContent content, IScannerInfo scanInfo,
			IncludeFileContentProvider fileCreator, IIndex index, int options, IParserLogService log)
			throws CoreException;

	/**
	 * Returns the AST completion node for the given offset.
	 * @since 5.2
	 */
	public IASTCompletionNode getCompletionNode(FileContent reader, IScannerInfo scanInfo,
			IncludeFileContentProvider fileCreator, IIndex index, IParserLogService log, int offset)
			throws CoreException;

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
	 * @deprecated replaced by {@link IASTTranslationUnit#getNodeSelector(String)}.
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Deprecated
	IASTName[] getSelectedNames(IASTTranslationUnit ast, int start, int length);

	/**
	 * @deprecated replaced by {@link #getASTTranslationUnit(FileContent, IScannerInfo,
	 * IncludeFileContentProvider, IIndex, int, IParserLogService)}
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Deprecated
	IASTTranslationUnit getASTTranslationUnit(org.eclipse.cdt.core.parser.CodeReader reader, IScannerInfo scanInfo,
			org.eclipse.cdt.core.dom.ICodeReaderFactory fileCreator, IIndex index, IParserLogService log)
			throws CoreException;

	/**
	 * @deprecated replaced by {@link #getASTTranslationUnit(FileContent, IScannerInfo,
	 * IncludeFileContentProvider, IIndex, int, IParserLogService)}
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Deprecated
	IASTTranslationUnit getASTTranslationUnit(org.eclipse.cdt.core.parser.CodeReader reader, IScannerInfo scanInfo,
			org.eclipse.cdt.core.dom.ICodeReaderFactory fileCreator, IIndex index, int options, IParserLogService log)
			throws CoreException;

	/**
	 * @deprecated replaced by {@link #getCompletionNode(FileContent, IScannerInfo,
	 * IncludeFileContentProvider, IIndex, IParserLogService, int)}.
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Deprecated
	IASTCompletionNode getCompletionNode(org.eclipse.cdt.core.parser.CodeReader reader, IScannerInfo scanInfo,
			org.eclipse.cdt.core.dom.ICodeReaderFactory fileCreator, IIndex index, IParserLogService log, int offset)
			throws CoreException;
}
