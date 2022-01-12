/*******************************************************************************
 * Copyright (c) 2009, 2011 Wind River Systems, Inc. and others.
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

import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.parser.FileContent;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.parser.scanner.InternalFileContent;
import org.eclipse.cdt.internal.core.parser.scanner.InternalFileContentProvider;

public class EmptyFilesProvider extends InternalFileContentProvider {
	final private static EmptyFilesProvider INSTANCE = new EmptyFilesProvider();

	public static InternalFileContentProvider getInstance() {
		return INSTANCE;
	}

	private EmptyFilesProvider() {
	}

	@Override
	public InternalFileContent getContentForInclusion(String path, IMacroDictionary macroDictionary) {
		if (!getInclusionExists(path))
			return null;

		return (InternalFileContent) FileContent.create(path, CharArrayUtils.EMPTY);
	}

	@Override
	public InternalFileContent getContentForInclusion(IIndexFileLocation ifl, String astPath) {
		return (InternalFileContent) FileContent.create(astPath, CharArrayUtils.EMPTY);
	}
}
