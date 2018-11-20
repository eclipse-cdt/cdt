/*******************************************************************************
 * Copyright (c) 2006, 2010 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    QNX - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *    IBM Corporation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.indexer;

import java.util.List;

import org.eclipse.cdt.core.parser.IncludeFileContentProvider;
import org.eclipse.cdt.internal.core.dom.IIncludeFileResolutionHeuristics;

/**
 * A task for index updates.
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
public class StandaloneFastIndexerTask extends StandaloneIndexerTask {
	public StandaloneFastIndexerTask(StandaloneFastIndexer indexer, List<String> added, List<String> changed,
			List<String> removed) {
		super(indexer, added, changed, removed, true);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected IncludeFileContentProvider createReaderFactory() {
		return IncludeFileContentProvider.adapt(new StandaloneIndexerFallbackReaderFactory());
	}

	@Override
	protected IIncludeFileResolutionHeuristics createIncludeHeuristics() {
		return null;
	}
}
