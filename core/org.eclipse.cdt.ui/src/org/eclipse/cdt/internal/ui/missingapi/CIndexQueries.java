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
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IPositionConverter;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IFunctionDeclaration;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.IParent;
import org.eclipse.cdt.core.model.ISourceRange;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IVariableDeclaration;

import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMFile;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMInclude;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMName;

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
			projects.addLast(cproject);
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

	public CalledByResult findCalledBy(ICElement callee, IProgressMonitor pm) throws CoreException, InterruptedException {
		LinkedList projects= new LinkedList(Arrays.asList(CoreModel.getDefault().getCModel().getCProjects()));
		IASTName name= toASTName(callee);
		
		CalledByResult result= new CalledByResult();
		if (name != null) {
			// resolve the binding.
			name.resolveBinding();

			for (Iterator iter = projects.iterator(); iter.hasNext();) {
				findCalledBy(name, (ICProject) iter.next(), result);	
			}
		}
		return result;
	}

	private IASTName toASTName(ICElement elem) throws CoreException {
		if (elem instanceof ISourceReference) {
			ISourceReference sf= (ISourceReference) elem;
			ISourceRange range = sf.getSourceRange();
			ITranslationUnit tu = sf.getTranslationUnit();
			if (tu != null) {
				ILanguage language = tu.getLanguage();
				IASTTranslationUnit ast = language.getASTTranslationUnit(tu, ILanguage.AST_SKIP_ALL_HEADERS | ILanguage.AST_USE_INDEX);
				return getASTName(language, ast, range);
			}
		}
		return null;
	}

	private static IASTName getASTName(ILanguage language, IASTTranslationUnit ast, ISourceRange range) {
		IASTName[] names = language.getSelectedNames(ast, range.getIdStartPos(), range.getIdLength());
		if (names.length > 0) {
			return names[names.length-1];
		}
		return null;
	}

	private void findCalledBy(IASTName name, ICProject project, CalledByResult result) throws InterruptedException, CoreException {
		PDOM pdom= (PDOM) CCorePlugin.getPDOMManager().getPDOM(project);
		if (pdom != null) {
			pdom.acquireReadLock();
			try {
				IBinding binding= pdom.resolveBinding(name);
				if (binding != null) {
					IASTName[] names= pdom.getReferences(binding);
					for (int i = 0; i < names.length; i++) {
						IASTName rname = names[i];
						ITranslationUnit tu= toTranslationUnit(project, rname);
						CIndexReference ref= new CIndexReference(tu, rname);
						ICElement elem= findCaller(ref);
						result.add(elem, ref);
					}
				}
			}
			finally {
				pdom.releaseReadLock();
			}
		}
	}

	public CallsToResult findCallsToInRange(ITranslationUnit tu, IRegion range, IProgressMonitor pm) throws CoreException, InterruptedException {
		CallsToResult result= new CallsToResult();
		ICProject project = tu.getCProject();
		PDOM pdom= (PDOM) CCorePlugin.getPDOMManager().getPDOM(project);
		if (pdom != null) {
			pdom.acquireReadLock();
			try {
				IPath location= locationForTU(tu);
				PDOMFile file= pdom.getFile(location);
				if (file != null) {
					// mstodo use correct timestamp
					long timestamp= location.toFile().lastModified();
					IPositionConverter pc= CCorePlugin.getPositionTrackerManager().findPositionConverter(tu.getPath(), timestamp);
					if (pc != null) {
						range= pc.historicToActual(range);
					}
					PDOMName name= file.getFirstName();
					while (name != null) {
						if (name.isReference()) {
							IASTFileLocation loc= name.getFileLocation();
							if (encloses(range, loc.getNodeOffset(), loc.getNodeLength())) {
								ICElement[] defs= findDefinitions(project, name);
								if (defs != null && defs.length > 0) {
									CIndexReference ref= new CIndexReference(tu, name);
									result.add(defs, ref);
								}
							}
						}
						name= name.getNextInFile();
					}
				}
			}
			finally {
				pdom.releaseReadLock();
			}
		}
		return result;
	}

	private ICElement[] findDefinitions(ICProject project, PDOMName name) throws CoreException {
		ArrayList defs= new ArrayList();
		PDOMBinding binding= name.getPDOMBinding();
		if (binding != null) {
			PDOMName declName= binding.getFirstDefinition();
			while (declName != null) {
				ICElement elem= findEnclosingElement(project, declName);
				if (elem != null) {
					defs.add(elem);
				}
				declName= declName.getNextInBinding();
			}
			if (defs.isEmpty()) {
				declName= binding.getFirstDeclaration();
				while (declName != null) {
					ICElement elem= findEnclosingElement(project, declName);
					if (elem != null) {
						defs.add(elem);
					}
					declName= declName.getNextInBinding();
				}
			}		
		}
		return (ICElement[]) defs.toArray(new ICElement[defs.size()]);
	}

	private boolean encloses(IRegion range, int nodeOffset, int nodeLength) {
		int d1= nodeOffset - range.getOffset();
		int d2= range.getLength() - nodeLength - d1;
		return d1 >= 0 && d2 >= 0;
	}
	
	private ICElement findEnclosingElement(ICProject project, PDOMName declName) throws CoreException {
		ITranslationUnit tu= toTranslationUnit(project, declName);
		if (tu != null) {
			//	mstodo use correct timestamp			
			//	PDOMFile file= declName.getFile();
			long timestamp= tu.getPath().toFile().lastModified();
			IPositionConverter pc= CCorePlugin.getPositionTrackerManager().findPositionConverter(tu.getPath(), timestamp);
			int offset= declName.getNodeOffset();
			if (pc != null) {
				offset= pc.historicToActual(new Region(offset, 0)).getOffset();
			}
			return findElement(tu, offset, true);
		}
		return null;
	}

	private ICElement findCaller(CIndexReference reference) throws CoreException {
		ITranslationUnit tu= reference.getTranslationUnit();
		long timestamp= reference.getTimestamp();
		IPositionConverter pc= CCorePlugin.getPositionTrackerManager().findPositionConverter(tu.getPath(), timestamp);
		int offset= reference.getOffset();
		if (pc != null) {
			offset= pc.historicToActual(new Region(offset, 0)).getOffset();
		}
		return findElement(tu, offset, false);
	}

	private ICElement findElement(ICElement element, int offset, boolean allowVars) throws CModelException {
		if (element == null || (element instanceof IFunctionDeclaration) || 
				(allowVars && element instanceof IVariableDeclaration)) {
			return element;
		}
		if (element instanceof IParent) {
			ICElement[] children= ((IParent) element).getChildren();
			for (int i = 0; i < children.length; i++) {
				ICElement child = children[i];
				if (child instanceof ISourceReference) {
					ISourceRange sr= ((ISourceReference) child).getSourceRange();
					int startPos= sr.getStartPos();
					if (startPos <= offset && offset < startPos + sr.getLength()) {
						return findElement(child, offset, allowVars);
					}
				}
			}
		}
		return null;
	}
}
