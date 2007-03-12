/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *    Ed Swartz (Nokia)
 *******************************************************************************/ 

package org.eclipse.cdt.internal.ui.viewsupport;

import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Region;
import org.eclipse.ui.IEditorInput;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IPositionConverter;
import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.index.IndexLocationFactory;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ISourceRange;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.core.model.ext.CElementHandleFactory;
import org.eclipse.cdt.internal.core.model.ext.ICElementHandle;
import org.eclipse.cdt.internal.corext.util.CModelUtil;

public class IndexUI {
	private static final ICElementHandle[] EMPTY_ELEMENTS = new ICElementHandle[0];

	public static IIndexBinding elementToBinding(IIndex index, ICElement element) throws CoreException, DOMException {
		if (element instanceof ISourceReference) {
			ISourceReference sf = ((ISourceReference)element);
			ISourceRange range= sf.getSourceRange();
			if (range.getIdLength() != 0) {
				IIndexName name= elementToName(index, element);
				if (name != null) {
					return index.findBinding(name);
				}
			}
			else {
				String name= element.getElementName();
				name= name.substring(name.lastIndexOf(':')+1);
				IIndexBinding[] bindings= index.findBindings(name.toCharArray(), IndexFilter.ALL, new NullProgressMonitor());
				for (int i = 0; i < bindings.length; i++) {
					IIndexBinding binding = bindings[i];
					if (checkBinding(binding, element)) {
						return binding;
					}
				}
			}
		}
		return null;
	}

	private static boolean checkBinding(IIndexBinding binding, ICElement element) throws DOMException {
		switch(element.getElementType()) {
		case ICElement.C_ENUMERATION:
			return binding instanceof IEnumeration;
		case ICElement.C_NAMESPACE:
			return binding instanceof ICPPNamespace;
		case ICElement.C_STRUCT:
			return binding instanceof ICompositeType && 
				((ICompositeType) binding).getKey() == ICompositeType.k_struct;
		case ICElement.C_CLASS:
			return binding instanceof ICPPClassType && 
				((ICompositeType) binding).getKey() == ICPPClassType.k_class;
		case ICElement.C_UNION:
			return binding instanceof ICompositeType && 
				((ICompositeType) binding).getKey() == ICompositeType.k_union;
		case ICElement.C_TYPEDEF:
			return binding instanceof ITypedef;
		}
		return false;
	}

	public static IIndexName elementToName(IIndex index, ICElement element) throws CoreException {
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
						IRegion region= new Region(pos.getIdStartPos()+idx, pos.getIdLength()-idx);
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

	public static ICElementHandle[] findRepresentative(IIndex index, IBinding binding)
		throws CoreException, DOMException {
		ICElementHandle[] defs = IndexUI.findAllDefinitions(index, binding);
		if (defs.length == 0) {
			ICElementHandle elem = IndexUI.findAnyDeclaration(index, null, binding);
			if (elem != null) {
				defs = new ICElementHandle[] { elem };
			}
		}
		return defs;
	}

	public static ICElementHandle[] findAllDefinitions(IIndex index, IBinding binding) throws CoreException, DOMException {
		if (binding != null) {
			IIndexName[] defs= index.findDefinitions(binding);

			ArrayList result= new ArrayList();
			for (int i = 0; i < defs.length; i++) {
				IIndexName in = defs[i];
				ICElementHandle definition= getCElementForName(null, index, in);
				if (definition != null) {
					result.add(definition);
				}
				
			}
			return (ICElementHandle[]) result.toArray(new ICElementHandle[result.size()]);
		}
		return EMPTY_ELEMENTS;
	}
			
	public static ICElementHandle getCElementForName(ICProject preferProject, IIndex index, IASTName declName) throws CoreException, DOMException {
		assert !declName.isReference();
		IBinding binding= declName.resolveBinding();
		if (binding != null) {
			ITranslationUnit tu= getTranslationUnit(preferProject, declName);
			if (tu != null) {
				IFile file= (IFile) tu.getResource();
				long timestamp= file != null ? file.getLocalTimeStamp() : 0;
				IASTFileLocation loc= declName.getFileLocation();
				IRegion region= new Region(loc.getNodeOffset(), loc.getNodeLength());
				IPositionConverter converter= CCorePlugin.getPositionTrackerManager().findPositionConverter(tu, timestamp);
				if (converter != null) {
					region= converter.actualToHistoric(region);
				}
				return CElementHandleFactory.create(tu, binding, region, timestamp);
			}
		}
		return null;
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

	public static ICElementHandle getCElementForName(ICProject preferProject, IIndex index, IIndexName declName) throws CoreException, DOMException {
		assert !declName.isReference();
		ITranslationUnit tu= getTranslationUnit(preferProject, declName);
		if (tu != null) {
			IRegion region= new Region(declName.getNodeOffset(), declName.getNodeLength());
			long timestamp= declName.getFile().getTimestamp();
			return CElementHandleFactory.create(tu, index.findBinding(declName), region, timestamp);
		}
		return null;
	}

	public static ICElementHandle findAnyDeclaration(IIndex index, ICProject preferProject, IBinding binding) throws CoreException, DOMException {
		if (binding != null) {
			IIndexName[] names= index.findNames(binding, IIndex.FIND_DECLARATIONS);
			for (int i = 0; i < names.length; i++) {
				ICElementHandle elem= getCElementForName(preferProject, index, names[i]);
				if (elem != null) {
					return elem;
				}
			}
		}
		return null;
	}

	public static IASTName getSelectedName(IIndex index, IEditorInput editorInput, ITextSelection selection) throws CoreException {
		int selectionStart = selection.getOffset();
		int selectionLength = selection.getLength();

		IWorkingCopy workingCopy = CUIPlugin.getDefault().getWorkingCopyManager().getWorkingCopy(editorInput);
		if (workingCopy == null)
			return null;
		
		int options= ITranslationUnit.AST_SKIP_INDEXED_HEADERS;
		IASTTranslationUnit ast = workingCopy.getAST(index, options);
		FindNameForSelectionVisitor finder= new FindNameForSelectionVisitor(ast.getFilePath(), selectionStart, selectionLength);
		ast.accept(finder);
		return finder.getSelectedName();
	}
}
