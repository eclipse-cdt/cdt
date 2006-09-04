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
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IFunctionDeclaration;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.IParent;
import org.eclipse.cdt.core.model.ISourceRange;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMFile;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMInclude;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMName;
import org.eclipse.cdt.internal.corext.util.CModelUtil;

/**
 * Access to high level queries in the index.
 * @since 4.0
 */
public class CIndexQueries {
    private static final ICElement[] EMPTY_ELEMENTS = new ICElement[0];
    private static final CIndexIncludeRelation[] EMPTY_INCLUDES = new CIndexIncludeRelation[0];
    private static final CIndexQueries sInstance= new CIndexQueries();
	
	public static CIndexQueries getInstance() {
		return sInstance;
	}
	
	/**
	 * Searches for all include-relations that include the given translation unit.
	 * @param scope the projects to be searched.
	 * @param tu a translation unit
	 * @param pm a monitor for reporting progress.
	 * @return an array of include relations.
	 * @since 4.0
	 */
	public CIndexIncludeRelation[] findIncludedBy(ICProject[] scope, ITranslationUnit tu, IProgressMonitor pm) {
		HashMap result= new HashMap();
		ICProject projectOfTU= tu.getCProject();

		// mstodo progress monitor
		for (int i = scope.length-1; i >= 0; i--) {
			ICProject cproject = scope[i];
			// prefer the project of the translation unit.
			if (i != 0) {
				if (cproject.equals(projectOfTU)) {
					System.arraycopy(scope, 0, scope, 1, i);
					scope[0]= projectOfTU;
					cproject= scope[i];
				}
			}
			CIndexIncludeRelation[] includes= findIncludedBy(cproject, tu);	
			for (int j = 0; j < includes.length; j++) {
				CIndexIncludeRelation include = includes[j];
				result.put(include.getIncludedBy(), include);
			}
		}
		Collection includes= result.values();
		return (CIndexIncludeRelation[]) includes.toArray(new CIndexIncludeRelation[includes.size()]);
	}

	/**
	 * Searches for all include-relations in a project that include the given translation unit.
	 * @param project the project to be searched.
	 * @param tu a translation unit
	 * @return an array of include relations.
	 * @since 4.0
	 */
	public CIndexIncludeRelation[] findIncludedBy(ICProject project, ITranslationUnit tu) {
		try {
			PDOM pdom= (PDOM) CCorePlugin.getPDOMManager().getPDOM(project);
			if (pdom != null) {
				pdom.acquireReadLock();
				try {
					PDOMFile fileTarget= pdom.getFile(locationForTU(tu));
					if (fileTarget != null) {
						ArrayList result= new ArrayList();
						PDOMInclude include= fileTarget.getFirstIncludedBy();
						while (include != null) {
							try {
								result.add(new CIndexIncludeRelation(include));
							} catch (CoreException e) {
								CUIPlugin.getDefault().log(e);
							}
							include= include.getNextInIncludedBy();
						}
						return (CIndexIncludeRelation[]) result.toArray(new CIndexIncludeRelation[result.size()]);
					}
				}
				finally {
					pdom.releaseReadLock();
				}
			}
		} catch (CoreException e) {
			CUIPlugin.getDefault().log(e);
		} catch (InterruptedException e) {
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
	
	/**
	 * Searches for all include-relations defined in the given translation unit.
	 * @param tu a translation unit.
	 * @param pm a monitor to report progress.
	 * @return an array of include relations.
	 * @since 4.0
	 */
	public CIndexIncludeRelation[] findIncludesTo(ITranslationUnit tu, IProgressMonitor pm) {
		ICProject cproject= tu.getCProject();
		if (cproject != null) {
			try {
				PDOM pdom= (PDOM) CCorePlugin.getPDOMManager().getPDOM(cproject);
				if (pdom != null) {
					pdom.acquireReadLock();
					try {
						PDOMFile fileTarget= pdom.getFile(locationForTU(tu));
						if (fileTarget != null) {
							ArrayList result= new ArrayList();
							PDOMInclude include= fileTarget.getFirstInclude();
							while (include != null) {
								try {
									result.add(new CIndexIncludeRelation(include));
								} catch (CoreException e) {
									CUIPlugin.getDefault().log(e);
								}
								include= include.getNextInIncludes();
							}
							return (CIndexIncludeRelation[]) result.toArray(new CIndexIncludeRelation[result.size()]);
						}
					}
					finally {
						pdom.releaseReadLock();
					}
				}
			} catch (CoreException e) {
				CUIPlugin.getDefault().log(e);
			} catch (InterruptedException e) {
			}	
		}		
		return EMPTY_INCLUDES;
	}

	/**
	 * Searches for functions and methods that call a given element.
	 * @param scope the projects to be searched
	 * @param callee a function, method, constant, variable or enumerator.
	 * @param pm a monitor to report progress
	 * @return a result object.
	 * @since 4.0
	 */
	public CalledByResult findCalledBy(ICProject[] scope, ICElement callee, IProgressMonitor pm) {
		IASTName name= astNameForElement(callee);
		
		CalledByResult result= new CalledByResult();
		if (name != null) {
			// resolve the binding.
			name.resolveBinding();

			for (int i = 0; i < scope.length; i++) {
				ICProject project = scope[i];
				findCalledBy(name, project, result);					
			}
		}
		return result;
	}

	private IASTName astNameForElement(ICElement elem) {
		try {
			if (elem instanceof ISourceReference) {
				ISourceReference sf= (ISourceReference) elem;
				ISourceRange range= sf.getSourceRange();
				ITranslationUnit tu= sf.getTranslationUnit();
				IASTTranslationUnit ast= astUnitForTU(tu);
				if (ast != null) {
					ILanguage language = tu.getLanguage();
					IASTName[] names = language.getSelectedNames(ast, range.getIdStartPos(), range.getIdLength());
					if (names.length > 0) {
						return names[names.length-1];
					}
					return null;
				}
			}
		} catch (CoreException e) {
			CUIPlugin.getDefault().log(e);
		}
		return null;
	}

	private IASTTranslationUnit astUnitForTU(ITranslationUnit tu) {
		try {
			if (tu != null) {
				ILanguage language = tu.getLanguage();
				return language.getASTTranslationUnit(tu, ILanguage.AST_SKIP_ALL_HEADERS | ILanguage.AST_USE_INDEX);
			}
		} catch (CoreException e) {
			CUIPlugin.getDefault().log(e);
		}
		return null;
	}

	private void findCalledBy(IASTName name, ICProject project, CalledByResult result) {
		try {
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
							ICElement elem = findCalledBy(ref);
							if (elem != null) {
								result.add(elem, ref);
							} 
						}
					}
				}
				finally {
					pdom.releaseReadLock();
				}
			}
		} catch (CoreException e) {
			CUIPlugin.getDefault().log(e);
		} catch (InterruptedException e) {
		}
	}
	
	private ICElement findCalledBy(CIndexReference reference) {
		ITranslationUnit tu= reference.getTranslationUnit();
		long timestamp= reference.getTimestamp();
		IPositionConverter pc= CCorePlugin.getPositionTrackerManager().findPositionConverter(tu.getPath(), timestamp);
		int offset= reference.getOffset();
		if (pc != null) {
			offset= pc.historicToActual(new Region(offset, 0)).getOffset();
		}
		return findEnclosingFunction(tu, offset);
	}

	private ICElement findEnclosingFunction(ICElement element, int offset) {
		if (element == null || (element instanceof IFunctionDeclaration)) {
			return element;
		}
		try {
			if (element instanceof IParent) {
				ICElement[] children= ((IParent) element).getChildren();
				for (int i = 0; i < children.length; i++) {
					ICElement child = children[i];
					if (child instanceof ISourceReference) {
						ISourceRange sr= ((ISourceReference) child).getSourceRange();
						int startPos= sr.getStartPos();
						if (startPos <= offset && offset < startPos + sr.getLength()) {
							return findEnclosingFunction(child, offset);
						}
					}
				}
			}
		} catch (CModelException e) {
			CUIPlugin.getDefault().log(e);
		}
		return null;
	}

	private ICElement findCElementForDeclaration(ICElement element, int offset, int length) {
		int endoffset= offset+length;
		if (element == null) {
			return null;
		}
		try {
			if (element instanceof IParent) {
				ICElement[] children= ((IParent) element).getChildren();
				for (int i = 0; i < children.length; i++) {
					ICElement child = children[i];
					if (child instanceof ISourceReference) {
						ISourceRange sr= ((ISourceReference) child).getSourceRange();
						int offset2= sr.getIdStartPos();
						int endoffset2= offset2+sr.getIdLength();
						if (offset <= offset2 && endoffset2 <= endoffset) {
							return child;
						}
						offset2= sr.getStartPos();
						endoffset2= offset2+sr.getLength();
						if (offset2 <= offset && endoffset <= endoffset2) {
							ICElement result= findCElementForDeclaration(child, offset, length);
							if (result != null) {
								return result;
							}
						}
					}
				}
			}
		} catch (CModelException e) {
			CUIPlugin.getDefault().log(e);
		}
		return null;
	}

	private ITranslationUnit toTranslationUnit(ICProject cproject, IASTName name) {
		IPath path= Path.fromOSString(name.getFileLocation().getFileName());
		try {
			return CModelUtil.findTranslationUnitForLocation(path, cproject);
		} catch (CModelException e) {
			CUIPlugin.getDefault().log(e);
			return null;
		}
	}

	/**
	 * Searches for all calls that are made within a given range.
	 * @param scope the scope in which the references can be resolved to elements
	 * @param tu the translation unit where the calls are made
	 * @param range the range in the translation unit where the calls are made
	 * @param pm a monitor to report progress
	 * @return a result object.
	 * @since 4.0
	 */
	public CallsToResult findCallsInRange(ICProject[] scope, ITranslationUnit tu, IRegion range, IProgressMonitor pm) {
		CallsToResult result= new CallsToResult();
		IASTTranslationUnit astTU= astUnitForTU(tu);
		if (astTU != null) {
			ReferenceVisitor refVisitor= new ReferenceVisitor(astTU.getFilePath(), range.getOffset(), range.getLength());
			astTU.accept(refVisitor);

			IASTName[] refs = refVisitor.getReferences();
			for (int i = 0; i < refs.length; i++) {
				IASTName name = refs[i];
				ICElement[] defs = findAllDefinitions(scope, name);
				if (defs.length == 0) {
					ICElement elem = findAnyDeclaration(scope, name);
					if (elem != null) {
						defs = new ICElement[] { elem };
					}
				}
				if (defs != null && defs.length > 0) {
					CIndexReference ref = new CIndexReference(tu, name);
					result.add(defs, ref);
				}
			}
		}
		return result;
	}
	
	public ICElement[] findAllDefinitions(ICProject[] projectsToSearch, IASTName name) {
		name.resolveBinding();
		ArrayList result= new ArrayList();
		for (int i = 0; i < projectsToSearch.length; i++) {
			ICProject project = projectsToSearch[i];
			ICElement[] definitions= findAllDefinitions(project, name);
			if (definitions != null && definitions.length > 0) {
				result.addAll(Arrays.asList(definitions));
			}
		}
		return (ICElement[]) result.toArray(new ICElement[result.size()]);
	}
			
	public ICElement[] findAllDefinitions(ICProject project, IASTName name) {
		PDOM pdom;
		try {
			pdom = (PDOM) CCorePlugin.getPDOMManager().getPDOM(project);
			if (pdom != null) {
				pdom.acquireReadLock();
				try {
					IBinding binding= pdom.resolveBinding(name);
					if (binding != null) {
						return allEnclosingElements(project, pdom.getDefinitions(binding));
					}
				}
				finally {
					pdom.releaseReadLock();
				}
			}
		} catch (CoreException e) {
			CUIPlugin.getDefault().log(e);
		} catch (InterruptedException e) {
		}

		return EMPTY_ELEMENTS;
	}

	private ICElement[] allEnclosingElements(ICProject project, IASTName[] defs) {
		if (defs != null && defs.length > 0) {
			ArrayList result= new ArrayList(defs.length);
			for (int i = 0; i < defs.length; i++) {
				IASTName defName = defs[i];
				ICElement elem= findCElementForDeclaration(project, defName);
				if (elem != null) {
					result.add(elem);
				}
			}
			return (ICElement[]) result.toArray(new ICElement[result.size()]);
		}
		return EMPTY_ELEMENTS;
	}

	private ICElement findCElementForDeclaration(ICProject project, IASTName declName) {
		ITranslationUnit tu= toTranslationUnit(project, declName);
		if (tu != null) {
			IRegion region= null;
			if (declName instanceof PDOMName) {
				PDOMName pname= (PDOMName) declName;
				region= new Region(pname.getNodeOffset(), pname.getNodeLength());
				//	mstodo use correct timestamp			
				//	PDOMFile file= pname.getFile();
				long timestamp= tu.getPath().toFile().lastModified();
				IPositionConverter pc= CCorePlugin.getPositionTrackerManager().findPositionConverter(tu.getPath(), timestamp);
				if (pc != null) {
					region= pc.historicToActual(region);
				}
			}
			else {
				IASTFileLocation loc= declName.getFileLocation();
				region= new Region(loc.getNodeOffset(), loc.getNodeLength());
			}
			return findCElementForDeclaration(tu, region.getOffset(), region.getLength());
		}
		return null;
	}

	public ICElement findDefinition(ICProject[] projectsToSearch, IASTName name) {
		name.resolveBinding();
		
		for (int i = 0; i < projectsToSearch.length; i++) {
			ICProject project = projectsToSearch[i];
			ICElement definition= findDefinition(project, name);
			if (definition != null) {
				return definition;
			}
		}
		return null;
	}
			
	public ICElement findDefinition(ICProject project, IASTName name) {
		if (name.isDefinition()) {
			return findCElementForDeclaration(project, name);
		}
		
		PDOM pdom;
		try {
			pdom = (PDOM) CCorePlugin.getPDOMManager().getPDOM(project);
			if (pdom != null) {
				pdom.acquireReadLock();
				try {
					IBinding binding= pdom.resolveBinding(name);
					if (binding != null) {
						ICElement elem= firstEnclosingElement(project, pdom.getDefinitions(binding));
						if (elem != null) {
							return elem;
						}
					}
				}
				finally {
					pdom.releaseReadLock();
				}
			}
		} catch (CoreException e) {
			CUIPlugin.getDefault().log(e);
		} catch (InterruptedException e) {
		}

		return null;
	}

	private ICElement firstEnclosingElement(ICProject project, IASTName[] defs) {
		if (defs != null) {
			for (int i = 0; i < defs.length; i++) {
				IASTName defName = defs[i];
				ICElement elem= findCElementForDeclaration(project, defName);
				if (elem != null) {
					return elem;
				}
			}
		}
		return null;
	}

	public ICElement findAnyDeclaration(ICProject[] projectsToSearch, IASTName name) {
		name.resolveBinding();
		
		for (int i = 0; i < projectsToSearch.length; i++) {
			ICProject project = projectsToSearch[i];
			ICElement declaration= findAnyDeclaration(project, name);
			if (declaration != null) {
				return declaration;
			}
		}
		return null;
	}
			
	public ICElement findAnyDeclaration(ICProject project, IASTName name) {
		PDOM pdom;
		try {
			pdom = (PDOM) CCorePlugin.getPDOMManager().getPDOM(project);
			if (pdom != null) {
				pdom.acquireReadLock();
				try {
					IBinding binding= pdom.resolveBinding(name);
					if (binding != null) {
						ICElement elem= firstEnclosingElement(project, pdom.getDefinitions(binding));
						if (elem != null) {
							return elem;
						}
						elem= firstEnclosingElement(project, pdom.getDeclarations(binding));
						if (elem != null) {
							return elem;
						}
					}
				}
				finally {
					pdom.releaseReadLock();
				}
			}
		} catch (CoreException e) {
			CUIPlugin.getDefault().log(e);
		} catch (InterruptedException e) {
		}

		return null;
	}
}
