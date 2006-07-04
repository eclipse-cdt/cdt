/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.internal.ui.missingapi;

import java.util.ArrayList;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;

import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMFile;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMInclude;

public class CIndexQueries {
	public static class IPDOMInclude {
		private PDOMInclude fInclude;
		
		public IPDOMInclude(PDOMInclude include) {
			fInclude= include;
		}
		public boolean isSystemInclude() {
			return false;
		}
		public boolean isActiveCode() {
			return true;
		}
		public IPath getIncludedBy() throws CoreException {
			return Path.fromOSString(fInclude.getIncludedBy().getFileName().getString());
		}
		public IPath getIncludes() throws CoreException {
			return Path.fromOSString(fInclude.getIncludes().getFileName().getString());
		}
		public String getName() throws CoreException {
			return Path.fromOSString(fInclude.getIncludes().getFileName().getString()).lastSegment();
		}
		public int getOffset() {
			return 9;
		}
		public long getTimestamp() {
			return 0;
		}
	}

	private static final IPDOMInclude[] EMPTY_INCLUDES = new IPDOMInclude[0];
    private static final CIndexQueries sInstance= new CIndexQueries();
	
	public static CIndexQueries getInstance() {
		return sInstance;
	}
	
	public static ITranslationUnit toTranslationUnit(ICProject cproject, PDOMFile includedBy) throws CoreException {
		String name= includedBy.getFileName().getString();
		IPath path= Path.fromOSString(name);
		ICElement e= cproject.findElement(path);
		if (e instanceof ITranslationUnit) {
			return (ITranslationUnit) e;
		}
		return null;
	}

	private CIndexQueries() {
	}

	public IPDOMInclude[] findIncludedBy(ITranslationUnit tu, IProgressMonitor pm) throws CoreException {
		ICProject cproject= tu.getCProject();
		if (cproject != null) {
			PDOM pdom= (PDOM) CCorePlugin.getPDOMManager().getPDOM(cproject);
			if (pdom != null) {
				PDOMFile fileTarget= pdom.getFile(locationForTU(tu));
				if (fileTarget != null) {
					ArrayList result= new ArrayList();
					PDOMInclude include= fileTarget.getFirstIncludedBy();
					while (include != null) {
						result.add(new IPDOMInclude(include));
						include= include.getNextInIncludedBy();
					}
					return (IPDOMInclude[]) result.toArray(new IPDOMInclude[result.size()]);
				}
			}	
		}		
		return EMPTY_INCLUDES;
	}

	private IPath locationForTU(ITranslationUnit tu) {
		IResource r= tu.getResource();
		if (r != null) {
			return r.getLocation();
		}
		return tu.getPath();
	}
	
	public IPDOMInclude[] findIncludesTo(ITranslationUnit tu, IProgressMonitor pm) throws CoreException {
		ICProject cproject= tu.getCProject();
		if (cproject != null) {
			PDOM pdom= (PDOM) CCorePlugin.getPDOMManager().getPDOM(cproject);
			if (pdom != null) {
				PDOMFile fileTarget= pdom.getFile(locationForTU(tu));
				if (fileTarget != null) {
					ArrayList result= new ArrayList();
					PDOMInclude include= fileTarget.getFirstInclude();
					while (include != null) {
						result.add(new IPDOMInclude(include));
						include= include.getNextInIncludes();
					}
					return (IPDOMInclude[]) result.toArray(new IPDOMInclude[result.size()]);
				}
			}	
		}		
		return EMPTY_INCLUDES;
	}
}
