/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.model;

import org.eclipse.cdt.core.dom.ICodeReaderFactory;
import org.eclipse.cdt.core.dom.ast.IASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.AbstractLanguage;
import org.eclipse.cdt.core.model.IContributedModelBuilder;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.core.runtime.CoreException;

/**
 * Built-in language for assembly files.
 *
 * @since 4.0
 */
public class AssemblyLanguage extends AbstractLanguage {

	private static final String ID= "org.eclipse.cdt.core.assembly"; //$NON-NLS-1$

	/*
	 * @see org.eclipse.cdt.core.model.ILanguage#createModelBuilder(org.eclipse.cdt.core.model.ITranslationUnit)
	 */
	public IContributedModelBuilder createModelBuilder(ITranslationUnit tu) {
		return null;
	}

	/*
	 * @see org.eclipse.cdt.core.model.ILanguage#getASTTranslationUnit(org.eclipse.cdt.core.parser.CodeReader, org.eclipse.cdt.core.parser.IScannerInfo, org.eclipse.cdt.core.dom.ICodeReaderFactory, org.eclipse.cdt.core.index.IIndex, org.eclipse.cdt.core.parser.IParserLogService)
	 */
	public IASTTranslationUnit getASTTranslationUnit(CodeReader reader, IScannerInfo scanInfo,
			ICodeReaderFactory fileCreator, IIndex index, IParserLogService log) throws CoreException {
		return null;
	}

	/*
	 * @see org.eclipse.cdt.core.model.ILanguage#getCompletionNode(org.eclipse.cdt.core.parser.CodeReader, org.eclipse.cdt.core.parser.IScannerInfo, org.eclipse.cdt.core.dom.ICodeReaderFactory, org.eclipse.cdt.core.index.IIndex, org.eclipse.cdt.core.parser.IParserLogService, int)
	 */
	public IASTCompletionNode getCompletionNode(CodeReader reader, IScannerInfo scanInfo,
			ICodeReaderFactory fileCreator, IIndex index, IParserLogService log, int offset) throws CoreException {
		return null;
	}

	/*
	 * @see org.eclipse.cdt.core.model.ILanguage#getId()
	 */
	public String getId() {
		return ID;
	}

	/*
	 * @see org.eclipse.cdt.core.model.ILanguage#getSelectedNames(org.eclipse.cdt.core.dom.ast.IASTTranslationUnit, int, int)
	 */
	public IASTName[] getSelectedNames(IASTTranslationUnit ast, int start, int length) {
		return null;
	}

}
