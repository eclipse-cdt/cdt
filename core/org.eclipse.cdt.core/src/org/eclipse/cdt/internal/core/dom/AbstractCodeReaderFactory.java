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
package org.eclipse.cdt.internal.core.dom;

import java.io.IOException;

import org.eclipse.cdt.core.dom.ICodeReaderFactory;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;

/**
 * Base implementation for all code reader factories.
 */
@Deprecated
public abstract class AbstractCodeReaderFactory implements ICodeReaderFactory, IAdaptable {

	private final IIncludeFileResolutionHeuristics fHeuristics;

	public AbstractCodeReaderFactory(IIncludeFileResolutionHeuristics heuristics) {
		fHeuristics = heuristics;
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object getAdapter(Class adapter) {
		if (adapter.isAssignableFrom(IIncludeFileResolutionHeuristics.class)) {
			return fHeuristics;
		}
		return null;
	}

	public abstract CodeReader createCodeReaderForInclusion(IIndexFileLocation ifl, String astPath)
			throws CoreException, IOException;
}
