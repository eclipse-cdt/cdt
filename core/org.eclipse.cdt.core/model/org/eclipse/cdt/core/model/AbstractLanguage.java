/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.PlatformObject;

/**
 * Models the differences between various languages.
 * @since 4.0
 */
public abstract class AbstractLanguage extends PlatformObject implements ILanguage {
	
	final public IASTTranslationUnit getASTTranslationUnit(ITranslationUnit file, int style) throws CoreException {
		throw new UnsupportedOperationException();
	}

	final public IASTTranslationUnit getASTTranslationUnit(ITranslationUnit file, ICodeReaderFactory codeReaderFactory,
			int style) throws CoreException {
		throw new UnsupportedOperationException();
	}
}
