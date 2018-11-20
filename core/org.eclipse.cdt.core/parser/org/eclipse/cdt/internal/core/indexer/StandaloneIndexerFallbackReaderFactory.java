/*******************************************************************************
 *  Copyright (c) 2007, 2012 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *    IBM - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.indexer;

import java.io.File;
import java.io.IOException;

import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.ICodeReaderCache;
import org.eclipse.cdt.internal.core.dom.AbstractCodeReaderFactory;
import org.eclipse.core.runtime.CoreException;

/**
 * A factory for CodeReaders construction.
 *
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the CDT team.
 * </p>
 *
 * @since 4.0
 */
@Deprecated
public class StandaloneIndexerFallbackReaderFactory extends AbstractCodeReaderFactory {

	public StandaloneIndexerFallbackReaderFactory() {
		super(null);
	}

	@Override
	public CodeReader createCodeReaderForInclusion(String path) {
		try {
			if (!new File(path).isFile())
				return null;
			return new CodeReader(path);
		} catch (IOException e) {
			return null;
		}
	}

	@Override
	public CodeReader createCodeReaderForTranslationUnit(String path) {
		try {
			if (!new File(path).isFile())
				return null;
			return new CodeReader(path);
		} catch (IOException e) {
			return null;
		}
	}

	@Override
	public ICodeReaderCache getCodeReaderCache() {
		return null;
	}

	@Override
	public int getUniqueIdentifier() {
		return 0;
	}

	@Override
	public CodeReader createCodeReaderForInclusion(IIndexFileLocation ifl, String astPath)
			throws CoreException, IOException {
		return createCodeReaderForInclusion(astPath);
	}
}
