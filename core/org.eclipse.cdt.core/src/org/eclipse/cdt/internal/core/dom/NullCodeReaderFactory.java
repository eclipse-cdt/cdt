/*******************************************************************************
 * Copyright (c) 2007, 2011 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom;

import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.ICodeReaderCache;

/**
 * A <code>ICodeReaderFactory</code> which creates dummy <code>CodeReader</code>s without content.
 *
 * @since 4.0
 */
@Deprecated
public class NullCodeReaderFactory extends AbstractCodeReaderFactory {

	private static final char[] EMPTY_CHARS = new char[0];
	private static final NullCodeReaderFactory INSTANCE = new NullCodeReaderFactory();

	public static NullCodeReaderFactory getInstance() {
		return INSTANCE;
	}

	private NullCodeReaderFactory() {
		super(null);
	}

	/*
	 * @see org.eclipse.cdt.core.dom.ICodeReaderFactory#createCodeReaderForInclusion(java.lang.String)
	 */
	@Override
	public CodeReader createCodeReaderForInclusion(String path) {
		return new CodeReader(path, EMPTY_CHARS);
	}

	@Override
	public CodeReader createCodeReaderForInclusion(IIndexFileLocation ifl, String astPath) {
		return new CodeReader(astPath, EMPTY_CHARS);
	}

	/*
	 * @see org.eclipse.cdt.core.dom.ICodeReaderFactory#createCodeReaderForTranslationUnit(java.lang.String)
	 */
	@Override
	public CodeReader createCodeReaderForTranslationUnit(String path) {
		return new CodeReader(path, EMPTY_CHARS);
	}

	/*
	 * @see org.eclipse.cdt.core.dom.ICodeReaderFactory#getCodeReaderCache()
	 */
	@Override
	public ICodeReaderCache getCodeReaderCache() {
		return null;
	}

	/*
	 * @see org.eclipse.cdt.core.dom.ICodeReaderFactory#getUniqueIdentifier()
	 */
	@Override
	public int getUniqueIdentifier() {
		// is this used somewhere?
		return 7;
	}

}
