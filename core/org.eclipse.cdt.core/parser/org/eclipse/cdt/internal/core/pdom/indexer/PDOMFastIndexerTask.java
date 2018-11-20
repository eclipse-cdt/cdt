/*******************************************************************************
 * Copyright (c) 2006, 2012 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.indexer;

import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.IncludeFileContentProvider;
import org.eclipse.cdt.internal.core.dom.IIncludeFileResolutionHeuristics;

/**
 * Configures the abstract indexer to return tasks suitable for fast indexing.
 */
class PDOMFastIndexerTask extends PDOMIndexerTask {

	public PDOMFastIndexerTask(PDOMFastIndexer indexer, ITranslationUnit[] added, ITranslationUnit[] changed,
			ITranslationUnit[] removed) {
		super(added, changed, removed, indexer, true);
	}

	@Override
	protected IncludeFileContentProvider createReaderFactory() {
		return IncludeFileContentProvider.getSavedFilesProvider();
	}

	@Override
	protected IIncludeFileResolutionHeuristics createIncludeHeuristics() {
		return new ProjectIndexerIncludeResolutionHeuristics(getCProject().getProject(), getInputAdapter());
	}
}
