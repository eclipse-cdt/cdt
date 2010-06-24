/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems, Inc. and others.
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.core.model.ICContainer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

public class DeltaAnalyzer {
	private final List<ITranslationUnit> fForce= new ArrayList<ITranslationUnit>();
	private final List<ITranslationUnit> fChanged= new ArrayList<ITranslationUnit>();
	private final List<ITranslationUnit> fRemoved= new ArrayList<ITranslationUnit>();
	// For testing purposes, only.
	public static boolean sSuppressPotentialTUs= false;
	
	public DeltaAnalyzer() {
	}
	
	public void analyzeDelta(ICElementDelta delta) throws CoreException {
		processDelta(delta, new HashSet<IResource>());
	}
	
	private void processDelta(ICElementDelta delta, Set<IResource> handled) throws CoreException {
		final int flags = delta.getFlags();

		final boolean hasChildren = (flags & ICElementDelta.F_CHILDREN) != 0;
		if (hasChildren) {
			for (ICElementDelta child : delta.getAffectedChildren()) {
				processDelta(child, handled);
			}
		}

		final ICElement element = delta.getElement();
		handled.add(element.getResource());
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
					fChanged.add(tu);
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
				collectSources(folder, fChanged);
			}
			break;
		}
		
		if (!sSuppressPotentialTUs) {
			// If the cmodel delta does not have children, also look at the children of the
			// resource delta.
			final boolean checkChildren = !hasChildren;
			final IResourceDelta[] rDeltas= delta.getResourceDeltas();
			processResourceDelta(rDeltas, element, handled, checkChildren);
		}
	}

	public void processResourceDelta(final IResourceDelta[] rDeltas, final ICElement element,
			Set<IResource> handled, boolean checkChildren) {
		if (rDeltas != null) {
			for (IResourceDelta rd: rDeltas) {
				final int rdkind = rd.getKind();
				if (rdkind != IResourceDelta.ADDED) {
					IResource res= rd.getResource();
					if (res instanceof IFile && handled.add(res)) {
						switch (rdkind) {
						case IResourceDelta.CHANGED:
							if ((rd.getFlags() & IResourceDelta.CONTENT) != 0) {
								fChanged.add(new PotentialTranslationUnit(element, (IFile) res));
							}
							break;
						case IResourceDelta.REMOVED:
							fRemoved.add(new PotentialTranslationUnit(element, (IFile) res));
							break;
						}
					}
				}
				if (rdkind == IResourceDelta.CHANGED && checkChildren) {
					processResourceDelta(rd.getAffectedChildren(), element, handled, checkChildren);
				}
			}
		}
	}

	private void collectSources(ICContainer container, Collection<ITranslationUnit> sources) throws CoreException {
		container.accept(new TranslationUnitCollector(sources, sources, new NullProgressMonitor()));
	}

	public ITranslationUnit[] getForcedTUs() {
		return fForce.toArray(new ITranslationUnit[fForce.size()]);
	}

	public ITranslationUnit[] getChangedTUs() {
		return fChanged.toArray(new ITranslationUnit[fChanged.size()]);
	}

	public ITranslationUnit[] getRemovedTUs() {
		return fRemoved.toArray(new ITranslationUnit[fRemoved.size()]);
	}

	public List<ITranslationUnit> getForcedList() {
		return fForce;
	}

	public List<ITranslationUnit> getChangedList() {
		return fChanged;
	}
}
