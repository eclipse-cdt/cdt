/*******************************************************************************
 * Copyright (c) 2006, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *    Anton Leherbauer (Wind River Systems)
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
	/*
	 * @see org.eclipse.cdt.core.model.ILanguage#getName()
	 */
	public String getName() {
		ILanguageDescriptor languageDescriptor= LanguageManager.getInstance().getLanguageDescriptor(getId());
		if (languageDescriptor != null) {
			return languageDescriptor.getName();
		}
		return getId();
	}

	public IASTTranslationUnit getASTTranslationUnit(CodeReader reader, IScannerInfo scanInfo, ICodeReaderFactory fileCreator, IIndex index, int options, IParserLogService log) 
			throws CoreException {
		// for backwards compatibility
		return getASTTranslationUnit(reader, scanInfo, fileCreator, index, log);
	}
}
