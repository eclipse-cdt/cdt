/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom;

import org.eclipse.cdt.core.dom.ast.IASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopyProvider;
import org.eclipse.cdt.internal.core.dom.InternalASTServiceProvider;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IStorage;

/**
 * @author jcamelon
 *
 * @deprecated This class does not take into account language mappings.  Use
 * {@link org.eclipse.cdt.core.model.ILanguage} instead.
 * @noreference This class is not intended to be referenced by clients.
 */
@Deprecated
public class CDOM implements IASTServiceProvider {
	/**
	 * Singleton - Constructor is private.
	 */
	private CDOM() {
	}

	/**
	 * <code>instance</code> is the singleton.
	 */
	private static CDOM instance = new CDOM();

	/**
	 * accessor for singleton instance
	 * @return instance
	 */
	public static CDOM getInstance() {
		return instance;
	}

	/**
	 * Currently, only one AST Service is provided.
	 */
	private IASTServiceProvider defaultService = new InternalASTServiceProvider();

	/**
	 * @return IASTServiceProvider, the mechanism for obtaining an AST
	 */
	public IASTServiceProvider getASTService() {
		//CDOM itself is not so much "the" AST service as it acts as a proxy
		//to different AST services
		//Should we see the need to provide an extension point for this
		//rather than purely proxying the calls to IASTServiceProvider#*
		//we would have to do some discovery and co-ordination on behalf of the
		//client
		return this;
	}

	/**
	 * Constant <code>PARSE_SAVED_RESOURCES</code> - Parse saved resources in the workspace
	 */
	public static final int PARSE_SAVED_RESOURCES = 0;
	/**
	 * Constant <code>PARSE_WORKING_COPY_WITH_SAVED_INCLUSIONS</code> - Parse working copy for
	 * translation unit, saved resources for all header files.
	 */
	public static final int PARSE_WORKING_COPY_WITH_SAVED_INCLUSIONS = 1;
	/**
	 * Constant <code>PARSE_WORKING_COPY_WHENEVER_POSSIBLE</code> - Parse working copy whenever possible for both
	 * header files and the file in question as a translation unit.
	 */
	public static final int PARSE_WORKING_COPY_WHENEVER_POSSIBLE = 2;

	/**
	 * This method always returns <code>null</code>.
	 */
	public ICodeReaderFactory getCodeReaderFactory(int key) {
		return null;
	}

	@Override
	public IASTTranslationUnit getTranslationUnit(IFile fileToParse) throws UnsupportedDialectException {
		//TODO - At this time, we purely delegate blindly
		//In the future, we may need to delegate based upon context provided
		return defaultService.getTranslationUnit(fileToParse);
	}

	@Override
	public IASTTranslationUnit getTranslationUnit(IFile fileToParse, ICodeReaderFactory fileCreator)
			throws UnsupportedDialectException {
		//TODO - At this time, we purely delegate blindly
		//In the future, we may need to delegate based upon context provided
		return defaultService.getTranslationUnit(fileToParse, fileCreator);
	}

	@Override
	public IASTTranslationUnit getTranslationUnit(IFile fileToParse, ICodeReaderFactory fileCreator,
			IParserConfiguration configuration) throws UnsupportedDialectException {
		//TODO - At this time, we purely delegate blindly
		//In the future, we may need to delegate based upon context provided
		return defaultService.getTranslationUnit(fileToParse, fileCreator, configuration);
	}

	@Override
	public IASTCompletionNode getCompletionNode(IFile fileToParse, int offset, ICodeReaderFactory fileCreator)
			throws UnsupportedDialectException {
		//TODO - At this time, we purely delegate blindly
		//In the future, we may need to delegate based upon context provided
		return defaultService.getCompletionNode(fileToParse, offset, fileCreator);
	}

	@Override
	public IASTCompletionNode getCompletionNode(IStorage fileToParse, IProject project, int offset,
			ICodeReaderFactory fileCreator) throws UnsupportedDialectException {
		//TODO - At this time, we purely delegate blindly
		//In the future, we may need to delegate based upon context provided
		return defaultService.getCompletionNode(fileToParse, project, offset, fileCreator);
	}

	/**
	 * This method has no effect.
	 */
	public void setWorkingCopyProvider(IWorkingCopyProvider workingCopyProvider) {
	}

	@Override
	public IASTTranslationUnit getTranslationUnit(IStorage fileToParse, IProject project,
			ICodeReaderFactory fileCreator) throws UnsupportedDialectException {
		return defaultService.getTranslationUnit(fileToParse, project, fileCreator);
	}

	@Override
	public IASTTranslationUnit getTranslationUnit(IStorage fileToParse, IProject project)
			throws UnsupportedDialectException {
		return defaultService.getTranslationUnit(fileToParse, project);
	}

	@Override
	public IASTTranslationUnit getTranslationUnit(IFile fileToParse, boolean parseComments)
			throws UnsupportedDialectException {
		return defaultService.getTranslationUnit(fileToParse, parseComments);
	}

	@Override
	public IASTTranslationUnit getTranslationUnit(IFile fileToParse, ICodeReaderFactory fileCreator,
			boolean parseComments) throws UnsupportedDialectException {
		return defaultService.getTranslationUnit(fileToParse, fileCreator, parseComments);
	}
}
