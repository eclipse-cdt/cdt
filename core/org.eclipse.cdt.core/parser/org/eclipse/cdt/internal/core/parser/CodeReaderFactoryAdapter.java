/*******************************************************************************
 * Copyright (c) 2009, 2012 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser;

import java.io.IOException;

import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.parser.IncludeFileContentProvider;
import org.eclipse.cdt.internal.core.dom.AbstractCodeReaderFactory;
import org.eclipse.cdt.internal.core.parser.scanner.InternalFileContentProvider;
import org.eclipse.core.runtime.CoreException;

@Deprecated
public class CodeReaderFactoryAdapter extends AbstractCodeReaderFactory {

	/**
	 * @deprecated avoid using the adapter, its for backwards compatibility, only.
	 */
	@Deprecated
	public static org.eclipse.cdt.core.dom.ICodeReaderFactory adapt(IncludeFileContentProvider fileCreator) {
		if (fileCreator == null)
			return null;

		if (!(fileCreator instanceof InternalFileContentProvider))
			throw new IllegalArgumentException("Invalid file content provider"); //$NON-NLS-1$

		if (fileCreator instanceof FileContentProviderAdapter) {
			return ((FileContentProviderAdapter) fileCreator).getCodeReaderFactory();
		}
		return new CodeReaderFactoryAdapter((InternalFileContentProvider) fileCreator);
	}

	private InternalFileContentProvider fDelegate;

	private CodeReaderFactoryAdapter(InternalFileContentProvider fcp) {
		super(fcp.getIncludeHeuristics());
		fDelegate = fcp;
	}

	@Override
	public org.eclipse.cdt.core.parser.CodeReader createCodeReaderForInclusion(String path) {
		return CodeReaderAdapter.adapt(fDelegate.getContentForInclusion(path, null));
	}

	@Override
	public org.eclipse.cdt.core.parser.CodeReader createCodeReaderForInclusion(IIndexFileLocation ifl, String astPath)
			throws CoreException, IOException {
		return CodeReaderAdapter.adapt(fDelegate.getContentForInclusion(ifl, astPath));
	}

	@Override
	public org.eclipse.cdt.core.parser.CodeReader createCodeReaderForTranslationUnit(String path) {
		return CodeReaderAdapter.adapt(fDelegate.getContentForInclusion(path, null));
	}

	@Override
	@Deprecated
	public org.eclipse.cdt.core.parser.ICodeReaderCache getCodeReaderCache() {
		return null;
	}

	@Override
	public int getUniqueIdentifier() {
		return 0;
	}

	public InternalFileContentProvider getFileContentProvider() {
		return fDelegate;
	}
}
