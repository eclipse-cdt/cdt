/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.internal.core.pdom.indexer;

import java.util.Collection;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementVisitor;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

final class TranslationUnitCollector implements ICElementVisitor {
	private final Collection fTUs;
	private final boolean fAllFiles;
	private final IProgressMonitor fProgressMonitor;

	public TranslationUnitCollector(Collection tus, boolean allFiles, IProgressMonitor pm) {
		fTUs = tus;
		fAllFiles = allFiles;
		fProgressMonitor= pm;
	}

	public boolean visit(ICElement element) throws CoreException {
		if (fProgressMonitor.isCanceled()) {
			return false;
		}
		switch (element.getElementType()) {
		case ICElement.C_UNIT:
			ITranslationUnit tu = (ITranslationUnit)element;
			if (tu.isSourceUnit()) {
				if (fAllFiles || !CoreModel.isScannerInformationEmpty(tu.getResource())) {
					fTUs.add(tu);
				}
			}
			else if (tu.isHeaderUnit()) {
				fTUs.add(tu);
			}
			return false;
		case ICElement.C_CCONTAINER:
		case ICElement.C_PROJECT:
			return true;
		}
		return false;
	}
}