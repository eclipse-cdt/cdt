/*******************************************************************************
 * Copyright (c) 2008, 2011 Wind River Systems, Inc. and others.
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
package org.eclipse.cdt.internal.core.parser.scanner;

import org.eclipse.cdt.internal.core.dom.parser.ASTNode;

/**
 * Interface to listen for information about files skipped by the preprocessor,
 * because they are found in the index
 */
public interface ISkippedIndexedFilesListener {

	/**
	 * Notifies the listeners that an include file has been skipped.
	 * @param offset offset at which the file is included (see {@link ASTNode#getOffset()}
	 * @param fileContent information about the skipped file.
	 */
	void skippedFile(int offset, InternalFileContent fileContent);

	/**
	 * Notifies the listeners that a file is being parsed.
	 */
	void parsingFile(InternalFileContentProvider fileContentProvider, InternalFileContent fileContent);
}
