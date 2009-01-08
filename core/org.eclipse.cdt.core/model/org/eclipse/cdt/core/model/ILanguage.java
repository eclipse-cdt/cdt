/*******************************************************************************
 * Copyright (c) 2005, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Doug Schaefer (QNX) - Initial API and implementation
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
 * 
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface ILanguage extends IAdaptable {

	/**
	 * Option for {@link #getASTTranslationUnit(CodeReader, IScannerInfo, ICodeReaderFactory, IIndex, int, IParserLogService)}
	 * Instructs the parser to skip function and method bodies.
	 */
	public final static int OPTION_SKIP_FUNCTION_BODIES= 0x1;

	/**
	 * @deprecated, has no effect.
	 */
	@Deprecated
	public final static int OPTION_ADD_COMMENTS= 0x2;

	/**
	 * Option for {@link #getASTTranslationUnit(CodeReader, IScannerInfo, ICodeReaderFactory, IIndex, int, IParserLogService)}
	 * Performance optimization, instructs the parser not to create image-locations. 
	 * When using this option {@link IASTName#getImageLocation()} will always return <code>null</code>.
	 */
	public final static int OPTION_NO_IMAGE_LOCATIONS= 0x4;

	/**
	 * Option for {@link #getASTTranslationUnit(CodeReader, IScannerInfo, ICodeReaderFactory, IIndex, int, IParserLogService)}
	 * Marks the ast as being based on a source-file rather than a header-file. This makes a difference
	 * when bindings from the AST are used for searching the index, e.g. for static variables. 
	 */
	public final static int OPTION_IS_SOURCE_UNIT= 0x8;

	/**
	 * Option for {@link #getASTTranslationUnit(CodeReader, IScannerInfo, ICodeReaderFactory, IIndex, int, IParserLogService)}
	 * Instructs the parser not to create ast nodes for expressions within aggregate initializers
	 * when they do not contain names.
	 * @since 5.1
	 */
	public final static int OPTION_SKIP_TRIVIAL_EXPRESSIONS_IN_AGGREGATE_INITIALIZERS= 0x10;

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
	 * Return the AST completion node for the given offset.
	 * @throws CoreException
	 */
	public IASTCompletionNode getCompletionNode(CodeReader reader, IScannerInfo scanInfo, ICodeReaderFactory fileCreator, IIndex index, IParserLogService log, int offset) throws CoreException;


	/**
	 * Gather the list of IASTNames that appear the selection with the given start offset
	 * and length in the given ITranslationUnit.
	 * @deprecated use {@link IASTTranslationUnit#getNodeSelector(String)}, instead.
	 */
	@Deprecated
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
	 * Fully equivalent to 
	 * <code> getASTTranslationUnit(reader, scanInfo, fileCreator, index, 0, log) </code>
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
	public IASTTranslationUnit getASTTranslationUnit(CodeReader reader, IScannerInfo scanInfo, 
			ICodeReaderFactory fileCreator, IIndex index, IParserLogService log) 
			throws CoreException;
	
	/**
	 * Construct an AST for the source code provided by <code>reader</code>.
	 * As an option you can supply 
	 * @param reader source code to be parsed.
	 * @param scanInfo provides include paths and defined symbols.
	 * @param fileCreator factory that provides CodeReaders for files included
	 *                    by the source code being parsed.
	 * @param index (optional) index to use to provide support for ambiguity
	 *              resolution.
	 * @param options A combination of 
	 * {@link #OPTION_SKIP_FUNCTION_BODIES},
	 * {@link #OPTION_NO_IMAGE_LOCATIONS}, {@link #OPTION_IS_SOURCE_UNIT},
	 *  or <code>0</code>.
	 * @param log logger
	 * @return an AST for the source code provided by reader.
	 * @throws CoreException
	 */
	public IASTTranslationUnit getASTTranslationUnit(CodeReader reader, IScannerInfo scanInfo,
			ICodeReaderFactory fileCreator, IIndex index, int options, IParserLogService log)
			throws CoreException;

}
