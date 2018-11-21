/*******************************************************************************
 * Copyright (c) 2006, 2011 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.model;

import org.eclipse.cdt.core.dom.ast.IASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.parser.FileContent;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IncludeFileContentProvider;
import org.eclipse.cdt.internal.core.parser.CodeReaderAdapter;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.PlatformObject;

/**
 * Models the differences between various languages.
 * @since 4.0
 */
public abstract class AbstractLanguage extends PlatformObject implements ILanguage {
	@Override
	public String getName() {
		ILanguageDescriptor languageDescriptor = LanguageManager.getInstance().getLanguageDescriptor(getId());
		if (languageDescriptor != null) {
			return languageDescriptor.getName();
		}
		return getId();
	}

	/**
	 * @deprecated replaced by {@link #getASTTranslationUnit(FileContent, IScannerInfo,
	 * IncludeFileContentProvider, IIndex, int, IParserLogService)}
	 */
	@Deprecated
	@Override
	public IASTTranslationUnit getASTTranslationUnit(org.eclipse.cdt.core.parser.CodeReader reader,
			IScannerInfo scanInfo, org.eclipse.cdt.core.dom.ICodeReaderFactory fileCreator, IIndex index, int options,
			IParserLogService log) throws CoreException {
		// For backwards compatibility, should be overridden.
		return getASTTranslationUnit(reader, scanInfo, fileCreator, index, log);
	}

	/**
	 * @since 5.2
	 */
	@Override
	@SuppressWarnings("deprecation")
	public IASTTranslationUnit getASTTranslationUnit(FileContent content, IScannerInfo scanInfo,
			IncludeFileContentProvider fileCreator, IIndex index, int options, IParserLogService log)
			throws CoreException {
		// For backwards compatibility, should be overridden.
		return getASTTranslationUnit(CodeReaderAdapter.adapt(content), scanInfo,
				org.eclipse.cdt.internal.core.parser.CodeReaderFactoryAdapter.adapt(fileCreator), index, options, log);
	}

	/**
	 * @since 5.2
	 */
	@Override
	@SuppressWarnings("deprecation")
	public IASTCompletionNode getCompletionNode(FileContent reader, IScannerInfo scanInfo,
			IncludeFileContentProvider fileCreator, IIndex index, IParserLogService log, int offset)
			throws CoreException {
		// For backwards compatibility, should be overridden.
		return getCompletionNode(CodeReaderAdapter.adapt(reader), scanInfo,
				org.eclipse.cdt.internal.core.parser.CodeReaderFactoryAdapter.adapt(fileCreator), index, log, offset);
	}
}
