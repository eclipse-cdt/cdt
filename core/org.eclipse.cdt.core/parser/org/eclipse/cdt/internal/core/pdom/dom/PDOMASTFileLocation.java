/*******************************************************************************
 * Copyright (c) 2017 Nathan Ridge.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom;

import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;

/**
 * Implementation of IASTFileLocation for use by PDOM types.
 * This implementation just stores the fields which need to be computed by the caller
 * at constructor time.
 */
public class PDOMASTFileLocation implements IASTFileLocation {
	private String fFilename;
	private int fNodeOffset;
	private int fNodeLength;

	public PDOMASTFileLocation(String filename, int nodeOffset, int nodeLength) {
		fFilename = filename;
		fNodeOffset = nodeOffset;
		fNodeLength = nodeLength;
	}

	@Override
	public IASTFileLocation asFileLocation() {
		return this;
	}

	@Override
	public String getFileName() {
		return fFilename;
	}

	@Override
	public int getNodeOffset() {
		return fNodeOffset;
	}

	@Override
	public int getNodeLength() {
		return fNodeLength;
	}

	@Override
	public int getStartingLineNumber() {
		return 0;
	}

	@Override
	public int getEndingLineNumber() {
		return 0;
	}

	@Override
	public IASTPreprocessorIncludeStatement getContextInclusionStatement() {
		return null;
	}
}
