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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.cdt.core.model.ICContainer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

public class DeltaAnalyzer {
	private List fAdded= new ArrayList();
	private List fChanged= new ArrayList();
	private List fRemoved= new ArrayList();
	
	public DeltaAnalyzer() {
	}
	
	public void analyzeDelta(ICElementDelta delta) throws CoreException {
		processDelta(delta);
	}
	
	private void processDelta(ICElementDelta delta) throws CoreException {
		int flags = delta.getFlags();

		if ((flags & ICElementDelta.F_CHILDREN) != 0) {
			ICElementDelta[] children = delta.getAffectedChildren();
			for (int i = 0; i < children.length; ++i) {
				processDelta(children[i]);
			}
		}

		ICElement element = delta.getElement();
		switch (element.getElementType()) {
		case ICElement.C_UNIT:
			ITranslationUnit tu = (ITranslationUnit)element;
			if (!tu.isWorkingCopy()) {
				switch (delta.getKind()) {
				case ICElementDelta.CHANGED:
					if ((flags & ICElementDelta.F_CONTENT) != 0) {
						fChanged.add(tu);
					}
					break;
				case ICElementDelta.ADDED:
					fAdded.add(tu);
					break;
				case ICElementDelta.REMOVED:
					fRemoved.add(tu);
					break;
				}
			}
			break;
		case ICElement.C_CCONTAINER:
			ICContainer folder= (ICContainer) element;
			if (delta.getKind() == ICElementDelta.ADDED) {
				collectSources(folder, fAdded);
			}
			break;
		}
	}

	private void collectSources(ICContainer container, Collection sources) throws CoreException {
		container.accept(new TranslationUnitCollector(sources, sources, true, new NullProgressMonitor()));
	}

	public ITranslationUnit[] getAddedTUs() {
		return (ITranslationUnit[]) fAdded.toArray(new ITranslationUnit[fAdded.size()]);
	}

	public ITranslationUnit[] getChangedTUs() {
		return (ITranslationUnit[]) fChanged.toArray(new ITranslationUnit[fChanged.size()]);
	}

	public ITranslationUnit[] getRemovedTUs() {
		return (ITranslationUnit[]) fRemoved.toArray(new ITranslationUnit[fRemoved.size()]);
	}
}
