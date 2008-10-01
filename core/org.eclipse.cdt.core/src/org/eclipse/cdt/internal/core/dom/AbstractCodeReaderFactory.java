/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.dom;

import org.eclipse.cdt.core.dom.ICodeReaderFactory;
import org.eclipse.core.runtime.IAdaptable;

/**
 * Base implementation for all code reader factories.
 */
public abstract class AbstractCodeReaderFactory implements ICodeReaderFactory, IAdaptable {

	private final IIncludeFileResolutionHeuristics fHeuristics;

	public AbstractCodeReaderFactory(IIncludeFileResolutionHeuristics heuristics) {
		fHeuristics= heuristics;
	}
	
	@SuppressWarnings("unchecked")
	public Object getAdapter(Class adapter) {
		if (adapter.isAssignableFrom(IIncludeFileResolutionHeuristics.class)) {
			return fHeuristics;
		}
		return null;
	}
}
