/*******************************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.ui;

import java.util.Comparator;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.ui.model.IWorkbenchAdapter;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IArchiveContainer;
import org.eclipse.cdt.core.model.IBinaryContainer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IIncludeReference;
import org.eclipse.cdt.core.model.ILibraryReference;
import org.eclipse.cdt.core.model.IMember;
import org.eclipse.cdt.core.model.IMethodDeclaration;
import org.eclipse.cdt.core.model.ISourceRoot;

/**
 *	A sorter to sort the file and the folders in the C viewer in the following order:
 * 	1 Project
 * 	2 BinaryContainer
 *  3 ArchiveContainer
 *  4 LibraryContainer
 *  5 IncludeContainer
 *  6 Source roots
 *  5 C Elements
 *  6 non C Elements
 *  
 *  @noextend This class is not intended to be subclassed by clients.
 */
public class CElementSorter extends ViewerSorter { 

    protected static final int CMODEL = 0;
	protected static final int PROJECTS = 10;
	protected static final int BINARYCONTAINER = 12;
	protected static final int ARCHIVECONTAINER = 13;
	protected static final int INCLUDEREFCONTAINER = 14;
	protected static final int LIBRARYREFCONTAINER = 15;
	protected static final int SOURCEROOTS = 16;
	protected static final int CCONTAINERS = 17;
	protected static final int LIBRARYREFERENCES = 18;
	protected static final int INCLUDEREFERENCES = 19;
	protected static final int TRANSLATIONUNIT_HEADERS = 20;
	protected static final int TRANSLATIONUNIT_SOURCE = 21;
	protected static final int TRANSLATIONUNITS = 22;
	protected static final int BINARIES = 23;
	protected static final int ARCHIVES = 24;
	
	protected static final int INCLUDES = 28;
	protected static final int MACROS = 29;
	protected static final int USINGS = 30;
	protected static final int NAMESPACES = 32;
	protected static final int NAMESPACES_RESERVED = 33;
	protected static final int NAMESPACES_SYSTEM = 34;
	/**
	 * @since 5.1
	 */
	protected static final int TYPES = 35;
	protected static final int VARIABLEDECLARATIONS = 36;
	protected static final int FUNCTIONDECLARATIONS = 37;
	protected static final int VARIABLES = 38;
	protected static final int VARIABLES_RESERVED = 39;
	protected static final int VARIABLES_SYSTEM = 40;
	protected static final int FUNCTIONS = 41;
	protected static final int FUNCTIONS_RESERVED = 42;
	protected static final int FUNCTIONS_SYSTEM = 43;
	protected static final int METHODDECLARATIONS = 44;

	protected static final int CELEMENTS = 100;
	protected static final int CELEMENTS_RESERVED = 101;
	protected static final int CELEMENTS_SYSTEM = 102;

	protected static final int RESOURCEFOLDERS= 200;
	protected static final int RESOURCES= 201;
	protected static final int STORAGE= 202;
	protected static final int OTHERS= 500;


	/*
	 * Constants added for names starting with '_' or '__'
	 */
	private static final int NORMAL = 0;
	private static final int RESERVED = 1;
	private static final int SYSTEM = 2;

	/*
	 * Constants for ordering different member kinds.
	 */
	private static final int STATIC_MEMBER = 0;
	private static final int CONSTRUCTOR = 1;
	private static final int DESTRUCTOR = 2;
	private static final int MEMBER = 3;

	/**
	 * Flag indicating whether header files and source files should be separated.
	 * If <code>true</code>, header files will be sorted before source files,
	 * otherwise header and source files will be sorted by name.
	 */
	private boolean fSeparateHeaderAndSource;

	/**
	 * Default constructor for use as executable extension.
	 */
	public CElementSorter() {
		final IPreferenceStore store= CUIPlugin.getDefault().getPreferenceStore();
		fSeparateHeaderAndSource= store.getBoolean(PreferenceConstants.CVIEW_SEPARATE_HEADER_AND_SOURCE);
	}
	
	@Override
	public int category (Object element) {
		if (element instanceof ICElement) {
			ICElement cElement = (ICElement) element;
			switch (cElement.getElementType()) {
			case ICElement.C_MODEL:
				return CMODEL;
			case ICElement.C_PROJECT:
				return PROJECTS;
			case ICElement.C_CCONTAINER:
				if (element instanceof ISourceRoot) {
					return SOURCEROOTS;
				}
				return CCONTAINERS;
			case ICElement.C_VCONTAINER:
				if (element instanceof IBinaryContainer) {
					return BINARYCONTAINER;
				} else if (element instanceof IArchiveContainer) {
					return ARCHIVECONTAINER;
				} else if (element instanceof ILibraryReference) {
					return LIBRARYREFERENCES;
				} else if (element instanceof IIncludeReference) {
					return INCLUDEREFERENCES;
				}
				return CCONTAINERS;
			case ICElement.C_UNIT:
				if (fSeparateHeaderAndSource) {
					if (CoreModel.isValidHeaderUnitName(cElement.getCProject().getProject(), cElement.getElementName())) {
						return TRANSLATIONUNIT_HEADERS;
					}
					if (CoreModel.isValidSourceUnitName(cElement.getCProject().getProject(), cElement.getElementName())) {
						return TRANSLATIONUNIT_SOURCE;
					}
				}
				return TRANSLATIONUNITS;
			case ICElement.C_INCLUDE:
				return INCLUDES;
			case ICElement.C_MACRO:
				return MACROS;
			case ICElement.C_NAMESPACE:
				return NAMESPACES + getNameKind(cElement.getElementName());
			case ICElement.C_USING:
				return USINGS;
			case ICElement.C_TYPEDEF:
			case ICElement.C_CLASS: 
			case ICElement.C_CLASS_DECLARATION:
			case ICElement.C_TEMPLATE_CLASS:
			case ICElement.C_TEMPLATE_CLASS_DECLARATION:
			case ICElement.C_STRUCT:
			case ICElement.C_STRUCT_DECLARATION:
			case ICElement.C_TEMPLATE_STRUCT:
			case ICElement.C_TEMPLATE_STRUCT_DECLARATION:
			case ICElement.C_UNION:
			case ICElement.C_UNION_DECLARATION:
			case ICElement.C_TEMPLATE_UNION:
			case ICElement.C_TEMPLATE_UNION_DECLARATION:
			case ICElement.C_ENUMERATION:
				return TYPES;
			case ICElement.C_FUNCTION_DECLARATION:
			case ICElement.C_TEMPLATE_FUNCTION_DECLARATION:
				return FUNCTIONDECLARATIONS;
			case ICElement.C_METHOD_DECLARATION:
			case ICElement.C_TEMPLATE_METHOD_DECLARATION:
				return METHODDECLARATIONS;
			case ICElement.C_VARIABLE_DECLARATION:
				return VARIABLEDECLARATIONS;
			case ICElement.C_VARIABLE:
			case ICElement.C_TEMPLATE_VARIABLE:
			case ICElement.C_FIELD:
				return VARIABLES + getNameKind(cElement.getElementName());
			case ICElement.C_FUNCTION:
			case ICElement.C_TEMPLATE_FUNCTION:
			case ICElement.C_METHOD:
			case ICElement.C_TEMPLATE_METHOD:
				return FUNCTIONS + getNameKind(cElement.getElementName());
			case ICElement.C_ARCHIVE:
				return ARCHIVES;
			case ICElement.C_BINARY:
				return BINARIES;
			default:
				return CELEMENTS + getNameKind(cElement.getElementName());
			}
		} else if (element instanceof IResource) {
			IResource resource = (IResource) element;
			switch (resource.getType()) {
			case IResource.PROJECT:
				return PROJECTS;
			case IResource.FOLDER:
				return RESOURCEFOLDERS;
			default:
				return RESOURCES;
			}
		} else if (element instanceof IStorage) {
			return STORAGE;
		} else if (element instanceof CElementGrouping) {
			int type = ((CElementGrouping)element).getType();
			switch (type) {
			case CElementGrouping.INCLUDES_GROUPING:
				return INCLUDES;
			case CElementGrouping.MACROS_GROUPING:
				return MACROS;
			case CElementGrouping.CLASS_GROUPING:
				return TYPES;
			case CElementGrouping.NAMESPACE_GROUPING:
				return NAMESPACES;
			case CElementGrouping.LIBRARY_REF_CONTAINER:
				return LIBRARYREFCONTAINER;
			case CElementGrouping.INCLUDE_REF_CONTAINER:
				return INCLUDEREFCONTAINER;
			}
		}
		return OTHERS;
	}

	private int getNameKind(String name) {
		int length = name.length();
		if (length > 0 && name.charAt(0) == '_') {
			if (length > 1 && name.charAt(1) == '_') {
				return SYSTEM;
			}
			return RESERVED;
		}
		return NORMAL;
	}

	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		int cat1 = category(e1);
		int cat2 = category(e2);

		if (cat1 != cat2)
			return cat1 - cat2;

		// cat1 == cat2

		@SuppressWarnings("unchecked")
		final Comparator<Object> comparator = getComparator();
		if (cat1 == PROJECTS) {
			IWorkbenchAdapter a1= (IWorkbenchAdapter)((IAdaptable)e1).getAdapter(IWorkbenchAdapter.class);
			IWorkbenchAdapter a2= (IWorkbenchAdapter)((IAdaptable)e2).getAdapter(IWorkbenchAdapter.class);
			return comparator.compare(a1.getLabel(e1), a2.getLabel(e2));
		}

		if (cat1 == SOURCEROOTS) {
			ISourceRoot root1= getSourceRoot(e1);
			ISourceRoot root2= getSourceRoot(e2);
			if (root1 == null) {
				if (root2 == null) {
					return 0;
				}
				return 1;
			} else if (root2 == null) {
				return -1;
			}			
			if (!root1.getPath().equals(root2.getPath())) {
				int p1= getPathEntryIndex(root1);
				int p2= getPathEntryIndex(root2);
				if (p1 != p2) {
					return p1 - p2;
				}
			}
		}

		// non - c resources are sorted using the label from the viewers label provider
		if (cat1 == RESOURCES || cat1 == RESOURCEFOLDERS || cat1 == STORAGE || cat1 == OTHERS) {
			return compareWithLabelProvider(viewer, e1, e2);
		}
		
		String ns1 = ""; //$NON-NLS-1$
		String ns2 = ns1;
		
		String name1;
		String name2;

		if (e1 instanceof ICElement) {
			name1 = ((ICElement)e1).getElementName();
			int idx = name1.lastIndexOf("::"); //$NON-NLS-1$
			if (idx >= 0) {
				ns1 = name1.substring(0, idx);
				name1 = name1.substring(idx + 2);
			}
		    if (name1.length() > 0 && name1.charAt(0) == '~') {
		    	name1 = name1.substring(1);
		    }
		} else {
			name1 = e1.toString();
		}
		if (e2 instanceof ICElement) {
			name2 = ((ICElement)e2).getElementName();
			int idx = name2.lastIndexOf("::"); //$NON-NLS-1$
			if (idx >= 0) {
				ns2 = name2.substring(0, idx);
				name2 = name2.substring(idx + 2);
			}
		    if (name2.length() > 0 && name2.charAt(0) == '~') {
		    	name2 = name2.substring(1);
		    }
		} else {
			name2 = e2.toString();
		}
		
		// compare namespace
		int result = comparator.compare(ns1, ns2);
		if (result != 0) {
			return result;
		}
		
		// compare method/member kind
		if (e1 instanceof IMethodDeclaration && e2 instanceof IMethodDeclaration) {
			result = getMethodKind((IMethodDeclaration) e1) - getMethodKind((IMethodDeclaration) e2);
		} else if (e1 instanceof IMember && e2 instanceof IMember) {
			result = getMemberKind((IMember) e1) - getMemberKind((IMember) e2);
		}
		if (result != 0) {
			return result;
		}

		// compare simple name
		result = comparator.compare(name1, name2);
		if (result != 0) {
			return result;
		}
		return result;
	}

	private int getMethodKind(IMethodDeclaration method) {
		try {
			if (method.isStatic()) {
				return STATIC_MEMBER;
			}
			if (method.isConstructor()) {
				return CONSTRUCTOR;
			}
			if (method.isDestructor()) {
				return DESTRUCTOR;
			}
		} catch (CModelException exc) {
			// ignore
		}
		return MEMBER;
	}

	private int getMemberKind(IMember member) {
		try {
			if (member.isStatic()) {
				return STATIC_MEMBER;
			}
		} catch (CModelException exc) {
			// ignore
		}
		return MEMBER;
	}

	private ISourceRoot getSourceRoot(Object element) {
		ICElement celement = (ICElement)element;
		while (! (celement instanceof ISourceRoot) && celement != null) {
			celement = celement.getParent();
		}
		return (ISourceRoot)celement;
	}

	private int compareWithLabelProvider(Viewer viewer, Object e1, Object e2) {
		if (viewer instanceof ContentViewer) {
			IBaseLabelProvider prov = ((ContentViewer) viewer).getLabelProvider();
			if (prov instanceof ILabelProvider) {
				ILabelProvider lprov= (ILabelProvider) prov;
				String name1 = lprov.getText(e1);
				String name2 = lprov.getText(e2);
				if (name1 != null && name2 != null) {
					@SuppressWarnings("unchecked")
					final Comparator<Object> comparator = getComparator();
					return comparator.compare(name1, name2);
				}
			}
		}
		return 0; // can't compare
	}

	private int getPathEntryIndex(ISourceRoot root) {
		try {
			IPath rootPath= root.getPath();
			ISourceRoot[] roots= root.getCProject().getSourceRoots();
			for (int i= 0; i < roots.length; i++) {
				if (roots[i].getPath().equals(rootPath)) {
					return i;
				}
			}
		} catch (CModelException e) {
		}

		return Integer.MAX_VALUE;
	}

}
