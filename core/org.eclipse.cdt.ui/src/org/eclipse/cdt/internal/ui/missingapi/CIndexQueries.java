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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
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
    
    public static class IPDOMReference {
		private IASTName fName;
		private ICProject fProject;
		
		public IPDOMReference(ICProject cproject, IASTName name) {
			fProject= cproject;
			fName= name;
		}
		public ITranslationUnit getTranslationUnit() throws CoreException {
			return toTranslationUnit(fProject, fName);
		}
		public int getOffset() {
			return fName.getFileLocation().getNodeOffset();
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
		return toTranslationUnit(cproject, name);
	}

	public static ITranslationUnit toTranslationUnit(ICProject cproject, IASTName name) throws CoreException {
		return toTranslationUnit(cproject, name.getFileLocation().getFileName());
	}

	private static ITranslationUnit toTranslationUnit(ICProject cproject, String pathStr) throws CModelException {
		IPath path= Path.fromOSString(pathStr);
		ICElement e= cproject.findElement(path);
		if (e instanceof ITranslationUnit) {
			return (ITranslationUnit) e;
		}
		return null;
	}


	public IPDOMInclude[] findIncludedBy(ITranslationUnit tu, IProgressMonitor pm) throws CoreException, InterruptedException {
		HashMap result= new HashMap();
		LinkedList projects= new LinkedList(Arrays.asList(CoreModel.getDefault().getCModel().getCProjects()));
		ICProject cproject= tu.getCProject();
		if (cproject != null) {
			projects.remove(cproject);
			projects.addFirst(cproject);
		}
		
		for (Iterator iter = projects.iterator(); iter.hasNext();) {
			cproject = (ICProject) iter.next();
			PDOM pdom= (PDOM) CCorePlugin.getPDOMManager().getPDOM(cproject);
			if (pdom != null) {
				pdom.acquireReadLock();
				try {
					PDOMFile fileTarget= pdom.getFile(locationForTU(tu));
					if (fileTarget != null) {
						PDOMInclude include= fileTarget.getFirstIncludedBy();
						while (include != null) {
							PDOMFile file= include.getIncludedBy();
							String path= file.getFileName().getString();
							result.put(path, new IPDOMInclude(include));
							include= include.getNextInIncludedBy();
						}
					}
				}
				finally {
					pdom.releaseReadLock();
				}
			}	
		}
		Collection includes= result.values();
		return (IPDOMInclude[]) includes.toArray(new IPDOMInclude[includes.size()]);
	}

	private IPath locationForTU(ITranslationUnit tu) {
		IResource r= tu.getResource();
		if (r != null) {
			return r.getLocation();
		}
		return tu.getPath();
	}
	
	public IPDOMInclude[] findIncludesTo(ITranslationUnit tu, IProgressMonitor pm) throws CoreException, InterruptedException {
		ICProject cproject= tu.getCProject();
		if (cproject != null) {
			PDOM pdom= (PDOM) CCorePlugin.getPDOMManager().getPDOM(cproject);
			if (pdom != null) {
				pdom.acquireReadLock();
				try {
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
				finally {
					pdom.releaseReadLock();
				}
			}	
		}		
		return EMPTY_INCLUDES;
	}

	public IPDOMReference[] findReferences(ITranslationUnit tu, IASTName name, IProgressMonitor pm) throws CoreException, InterruptedException {
		ArrayList result= new ArrayList();
		LinkedList projects= new LinkedList(Arrays.asList(CoreModel.getDefault().getCModel().getCProjects()));
		ICProject cproject= tu.getCProject();
		if (cproject != null) {
			projects.remove(cproject);
			projects.addFirst(cproject);
		}
		
		name.resolveBinding();
		for (Iterator iter = projects.iterator(); iter.hasNext();) {
			cproject = (ICProject) iter.next();
			PDOM pdom= (PDOM) CCorePlugin.getPDOMManager().getPDOM(cproject);
			if (pdom != null) {
				pdom.acquireReadLock();
				try {
					IBinding binding= pdom.resolveBinding(name);
					if (binding != null) {
						IASTName[] names= pdom.getReferences(binding);
						for (int i = 0; i < names.length; i++) {
							IASTName rname = names[i];
							if (tu != null) {
								result.add(new IPDOMReference(cproject, rname));
							}
						}
					}
				}
				finally {
					pdom.releaseReadLock();
				}
			}	
		}
		return (IPDOMReference[]) result.toArray(new IPDOMReference[result.size()]);
	}
}
