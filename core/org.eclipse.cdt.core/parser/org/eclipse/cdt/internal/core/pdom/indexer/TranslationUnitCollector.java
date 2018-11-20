/*******************************************************************************
 * Copyright (c) 2007, 2012 Wind River Systems, Inc. and others.
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
package org.eclipse.cdt.internal.core.pdom.indexer;

import java.util.Collection;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementVisitor;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

final public class TranslationUnitCollector implements ICElementVisitor {
	private final Collection<ITranslationUnit> fSources;
	private final Collection<ITranslationUnit> fHeaders;
	private final IProgressMonitor fProgressMonitor;

	public TranslationUnitCollector(Collection<ITranslationUnit> sources, Collection<ITranslationUnit> headers,
			IProgressMonitor pm) {
		fSources = sources;
		fHeaders = headers;
		fProgressMonitor = pm;
	}

	@Override
	public boolean visit(ICElement element) throws CoreException {
		if (fProgressMonitor.isCanceled()) {
			return false;
		}
		switch (element.getElementType()) {
		case ICElement.C_UNIT:
			ITranslationUnit tu = (ITranslationUnit) element;
			if (tu.isSourceUnit()) {
				fSources.add(tu);
			} else if (fHeaders != null && tu.isHeaderUnit()) {
				fHeaders.add(tu);
			}
			return false;
		case ICElement.C_CCONTAINER:
		case ICElement.C_PROJECT:
			return true;
		}
		return false;
	}
}