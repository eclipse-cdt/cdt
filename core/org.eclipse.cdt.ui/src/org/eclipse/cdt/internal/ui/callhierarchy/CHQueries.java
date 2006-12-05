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

package org.eclipse.cdt.internal.ui.callhierarchy;

import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IPositionConverter;
import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.index.IndexLocationFactory;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ISourceRange;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.core.model.ext.CElementHandleFactory;
import org.eclipse.cdt.internal.corext.util.CModelUtil;


/**
 * Access to high level queries in the index.
 * @since 4.0
 */
public class CHQueries {
	private static final ICElement[] EMPTY_ELEMENTS = new ICElement[0];
	private static final CHNode[] EMPTY_NODES= new CHNode[0];
	
    private CHQueries() {}
    
	/**
	 * Searches for functions and methods that call a given element.
	 * @throws DOMException 
	 */
	public static CHNode[] findCalledBy(CHContentProvider cp, CHNode node, 
			IIndex index, IProgressMonitor pm) throws CoreException, DOMException {
		CalledByResult result= new CalledByResult();
		ICElement callee= node.getRepresentedDeclaration();
		if (! (callee instanceof ISourceReference)) {
			return EMPTY_NODES;
		}
		IBinding calleeBinding= elementToBinding(index, callee);
		findCalledBy(index, calleeBinding, callee.getCProject(), result);	
		
		return cp.createNodes(node, result);
	}

	private static IIndexBinding elementToBinding(IIndex index, ICElement element) throws CoreException {
		IIndexName name= elementToName(index, element);
		if (name != null) {
			return index.findBinding(name);
		}
		return null;
	}

	private static IIndexName elementToName(IIndex index, ICElement element) throws CoreException {
		if (element instanceof ISourceReference) {
			ISourceReference sf = ((ISourceReference)element);
			ITranslationUnit tu= sf.getTranslationUnit();
			if (tu != null) {
				IIndexFileLocation location= IndexLocationFactory.getIFL(tu);
				if (location != null) {
					IIndexFile file= index.getFile(location);
					if (file != null) {
						String elementName= element.getElementName();
						int idx= elementName.lastIndexOf(":")+1; //$NON-NLS-1$
						ISourceRange pos= sf.getSourceRange();
						IRegion region= new Region(pos.getIdStartPos()+idx, pos.getIdLength());
						IPositionConverter converter= CCorePlugin.getPositionTrackerManager().findPositionConverter(tu, file.getTimestamp());
						if (converter != null) {
							region= converter.actualToHistoric(region);
						}
						IIndexName[] names= file.findNames(region.getOffset(), region.getLength());
						for (int i = 0; i < names.length; i++) {
							IIndexName name = names[i];
							if (!name.isReference() && elementName.endsWith(new String(name.toCharArray()))) {
								return name;
							}
						}
					}
				}
			}
		}
		return null;
	}

	private static void findCalledBy(IIndex index, IBinding callee, ICProject project, CalledByResult result) throws CoreException, DOMException {
		if (callee != null) {
			IIndexName[] names= index.findReferences(callee);
			for (int i = 0; i < names.length; i++) {
				IIndexName rname = names[i];
				IIndexName caller= rname.getEnclosingDefinition();
				if (caller != null) {
					ICElement elem= getCElementForName(project, index, caller);
					if (elem != null) {
						result.add(elem, rname);
					} 
				}
			}
		}
	}

	private static ITranslationUnit getTranslationUnit(ICProject cproject, IName name) {
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
	 * @throws DOMException 
	 */
	public static CHNode[] findCalls(CHContentProvider cp, CHNode node, IIndex index, IProgressMonitor pm) throws CoreException, DOMException {
		ICElement caller= node.getRepresentedDeclaration();
		CallsToResult result= new CallsToResult();
		IIndexName callerName= elementToName(index, caller);
		if (callerName != null) {
			IIndexName[] refs= callerName.getEnclosedNames();
			for (int i = 0; i < refs.length; i++) {
				IIndexName name = refs[i];
				IBinding binding= index.findBinding(name);
				if (CallHierarchyUI.isRelevantForCallHierarchy(binding)) {
					ICElement[] defs = findAllDefinitions(index, binding);
					if (defs.length == 0) {
						ICElement elem = findAnyDeclaration(index, null, binding);
						if (elem != null) {
							defs = new ICElement[] { elem };
						}
					}
					if (defs != null && defs.length > 0) {
						result.add(defs, name);
					}
				}
			}
		}
		return cp.createNodes(node, result);
	}
	
	public static ICElement[] findAllDefinitions(IIndex index, IBinding binding) throws CoreException, DOMException {
		if (binding != null) {
			IIndexName[] defs= index.findDefinitions(binding);

			ArrayList result= new ArrayList();
			for (int i = 0; i < defs.length; i++) {
				IIndexName in = defs[i];
				ICElement definition= getCElementForName(null, index, in);
				if (definition != null) {
					result.add(definition);
				}
				
			}
			return (ICElement[]) result.toArray(new ICElement[result.size()]);
		}
		return EMPTY_ELEMENTS;
	}
			
	static ICElement getCElementForName(ICProject preferProject, IIndex index, IASTName declName) throws CoreException, DOMException {
		assert !declName.isReference();
		IBinding binding= declName.resolveBinding();
		if (binding != null) {
			ITranslationUnit tu= getTranslationUnit(preferProject, declName);
			if (tu != null) {
				IFile file= (IFile) tu.getResource();
				long timestamp= file.getLocalTimeStamp();
				IASTFileLocation loc= declName.getFileLocation();
				IRegion region= new Region(loc.getNodeOffset(), loc.getNodeLength());
				IPositionConverter converter= CCorePlugin.getPositionTrackerManager().findPositionConverter(file, timestamp);
				if (converter != null) {
					region= converter.actualToHistoric(region);
				}
				return CElementHandleFactory.create(tu, binding, region, timestamp);
			}
		}
		return null;
	}

	static ICElement getCElementForName(ICProject preferProject, IIndex index, IIndexName declName) throws CoreException, DOMException {
		assert !declName.isReference();
		ITranslationUnit tu= getTranslationUnit(preferProject, declName);
		if (tu != null) {
			IRegion region= new Region(declName.getNodeOffset(), declName.getNodeLength());
			long timestamp= declName.getFile().getTimestamp();
			return CElementHandleFactory.create(tu, index.findBinding(declName), region, timestamp);
		}
		return null;
	}

	public static ICElement findAnyDeclaration(IIndex index, ICProject preferProject, IBinding binding) throws CoreException, DOMException {
		if (binding != null) {
			IIndexName[] names= index.findNames(binding, IIndex.FIND_DECLARATIONS);
			for (int i = 0; i < names.length; i++) {
				ICElement elem= getCElementForName(preferProject, index, names[i]);
				if (elem != null) {
					return elem;
				}
			}
		}
		return null;
	}
}
