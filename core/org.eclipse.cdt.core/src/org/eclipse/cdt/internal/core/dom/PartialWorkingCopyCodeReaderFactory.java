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
 *    IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;

import org.eclipse.cdt.core.dom.CDOM;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.core.model.IWorkingCopyProvider;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.ICodeReaderCache;
import org.eclipse.cdt.core.parser.ParserUtil;
import org.eclipse.cdt.internal.core.parser.EmptyIterator;
import org.eclipse.core.runtime.CoreException;

/**
 * @author jcamelon
 */
@Deprecated
public class PartialWorkingCopyCodeReaderFactory extends AbstractCodeReaderFactory {

	private final IWorkingCopyProvider provider;
	private ICodeReaderCache cache = null;

	/**
	 * @param provider
	 */
	public PartialWorkingCopyCodeReaderFactory(IWorkingCopyProvider provider,
			IIncludeFileResolutionHeuristics heuristics) {
		super(heuristics);
		this.provider = provider;
		cache = SavedCodeReaderFactory.getInstance().getCodeReaderCache();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ICodeReaderFactory#getUniqueIdentifier()
	 */
	@Override
	public int getUniqueIdentifier() {
		return CDOM.PARSE_WORKING_COPY_WITH_SAVED_INCLUSIONS;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ICodeReaderFactory#createCodeReaderForTranslationUnit(java.lang.String)
	 */
	@Override
	public CodeReader createCodeReaderForTranslationUnit(String path) {
		return checkWorkingCopyThenCache(path);
	}

	public CodeReader createCodeReaderForTranslationUnit(ITranslationUnit tu) {
		return new CodeReader(tu.getPath().toOSString(), tu.getContents());
	}

	protected CodeReader checkWorkingCopyThenCache(String path) {
		char[] buffer = ParserUtil.findWorkingCopyBuffer(path, createWorkingCopyIterator());
		if (buffer != null)
			return new CodeReader(path, buffer);
		return cache.get(path);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ICodeReaderFactory#createCodeReaderForInclusion(java.lang.String)
	 */
	@Override
	public CodeReader createCodeReaderForInclusion(String path) {
		return cache.get(path);
	}

	@Override
	public CodeReader createCodeReaderForInclusion(IIndexFileLocation ifl, String astPath)
			throws CoreException, IOException {
		return cache.get(astPath, ifl);
	}

	protected Iterator<IWorkingCopy> createWorkingCopyIterator() {
		if (provider == null)
			return EmptyIterator.empty();
		return Arrays.asList(provider.getWorkingCopies()).iterator();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ICodeReaderFactory#getCodeReaderCache()
	 */
	@Override
	public ICodeReaderCache getCodeReaderCache() {
		return cache;
	}

}
