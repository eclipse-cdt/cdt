package org.eclipse.cdt.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IArchive;
import org.eclipse.cdt.core.model.IArchiveContainer;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.IBinaryContainer;
import org.eclipse.cdt.core.model.ICContainer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IFunction;
import org.eclipse.cdt.core.model.IFunctionDeclaration;
import org.eclipse.cdt.core.model.IInclude;
import org.eclipse.cdt.core.model.IIncludeReference;
import org.eclipse.cdt.core.model.ILibraryReference;
import org.eclipse.cdt.core.model.IMacro;
import org.eclipse.cdt.core.model.IMethod;
import org.eclipse.cdt.core.model.IMethodDeclaration;
import org.eclipse.cdt.core.model.INamespace;
import org.eclipse.cdt.core.model.ISourceRoot;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IUsing;
import org.eclipse.cdt.core.model.IVariable;
import org.eclipse.cdt.core.model.IVariableDeclaration;
import org.eclipse.cdt.internal.ui.cview.IncludeRefContainer;
import org.eclipse.cdt.internal.ui.cview.LibraryRefContainer;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.ui.model.IWorkbenchAdapter;

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
 */
public class CElementSorter extends ViewerSorter { 

	private static final int CMODEL = 0;
	private static final int PROJECTS = 10;
	//private static final int OUTPUTREFCONTAINER = 11;
	private static final int BINARYCONTAINER = 12;
	private static final int ARCHIVECONTAINER = 13;
	private static final int INCLUDEREFCONTAINER = 14;
	private static final int LIBRARYREFCONTAINER = 15;
	private static final int SOURCEROOTS = 16;
	private static final int CCONTAINERS = 17;
	private static final int LIBRARYREFERENCES = 18;
	private static final int INCLUDEREFERENCES = 19;
	private static final int TRANSLATIONUNIT_HEADERS = 20;
	private static final int TRANSLATIONUNIT_SOURCE = 21;
	private static final int TRANSLATIONUNITS = 22;
	private static final int BINARIES = 23;
	private static final int ARCHIVES = 24;
	
	private static final int INCLUDES = 30;
	private static final int MACROS = 31;
	private static final int NAMESPACES = 32;
	private static final int USINGS = 33;
	private static final int VARIABLEDECLARATIONS = 34;
	private static final int FUNCTIONDECLARATIONS = 35;
	private static final int VARIABLES = 36;
	private static final int VARIABLES_RESERVED = 37;
	private static final int VARIABLES_SYSTEM = 38;
	private static final int FUNCTIONS = 39;
	private static final int FUNCTIONS_RESERVED = 40;
	private static final int FUNCTIONS_SYSTEM = 41;
	private static final int METHODDECLARATIONS = 42;

	private static final int CELEMENTS = 100;
	private static final int CELEMENTS_RESERVED = 101;
	private static final int CELEMENTS_SYSTEM = 102;

	private static final int RESOURCEFOLDERS= 200;
	private static final int RESOURCES= 201;
	private static final int STORAGE= 202;
	private static final int OTHERS= 500;

	public int category (Object element) {
		if (element instanceof ICModel) {
			return CMODEL;
		} else if (element instanceof ICProject) {
			return PROJECTS;
		} else if (element instanceof ISourceRoot) {
			return SOURCEROOTS;
		} else if (element instanceof IBinaryContainer) {
			return BINARYCONTAINER;
		} else if (element instanceof IArchiveContainer) {
			return ARCHIVECONTAINER;
		} else if (element instanceof ICContainer) {
			return CCONTAINERS;
		} else if (element instanceof ITranslationUnit) {
			ITranslationUnit tu = (ITranslationUnit)element;
			if (CoreModel.isValidHeaderUnitName(tu.getCProject().getProject(), tu.getElementName())) {
				return TRANSLATIONUNIT_HEADERS;
			}
			if (CoreModel.isValidSourceUnitName(tu.getCProject().getProject(), tu.getElementName())) {
				return TRANSLATIONUNIT_SOURCE;
			}
			return TRANSLATIONUNITS;
		} else if (element instanceof IInclude) {
			return INCLUDES;
		} else if (element instanceof IMacro) {
			return MACROS;
		} else if (element instanceof INamespace) {
			return NAMESPACES;
		} else if (element instanceof IUsing) {
			return USINGS;
		} else if (element instanceof IFunctionDeclaration && ! (element instanceof IFunction)) {
			return FUNCTIONDECLARATIONS;
		} else if (element instanceof IMethodDeclaration && !(element instanceof IMethod)) {
			return METHODDECLARATIONS;
		} else if (element instanceof IVariableDeclaration) {
			return VARIABLEDECLARATIONS;
		} else if (element instanceof IVariable) {
			String name = ((ICElement)element).getElementName();
			if (name.startsWith("__")) { //$NON-NLS-1$
				return VARIABLES_SYSTEM;
			}
			if (name.charAt(0) == '_') {
				return VARIABLES_RESERVED;
			}
			return VARIABLES;
		} else if (element instanceof IFunction) {
			String name = ((ICElement)element).getElementName();
			if (name.startsWith("__")) { //$NON-NLS-1$
				return FUNCTIONS_SYSTEM;
			}
			if (name.charAt(0) == '_') {
				return FUNCTIONS_RESERVED;
			}
			return FUNCTIONS;
		} else if (element instanceof IArchive) {
			return ARCHIVES;
		} else if (element instanceof IBinary) {
			return BINARIES;
		} else if (element instanceof ILibraryReference) {
			return LIBRARYREFERENCES;
		} else if (element instanceof IIncludeReference) {
			return INCLUDEREFERENCES;
		} else if (element instanceof ICElement) {
			String name = ((ICElement)element).getElementName();
			if (name.startsWith("__")) { //$NON-NLS-1$
				return CELEMENTS_SYSTEM;
			}
			if (name.charAt(0) == '_') {
				return CELEMENTS_RESERVED;
			}
			return CELEMENTS;
		} else if (element instanceof IFile) {
			return RESOURCES;
		} else if (element instanceof IProject) {
			return PROJECTS;
		} else if (element instanceof IContainer) {
			return RESOURCEFOLDERS;
		} else if (element instanceof IStorage) {
			return STORAGE;
		} else if (element instanceof LibraryRefContainer) {
			return LIBRARYREFCONTAINER;
		} else if (element instanceof IncludeRefContainer) {
			return INCLUDEREFCONTAINER;
		}
		return OTHERS;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerSorter#compare(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public int compare(Viewer viewer, Object e1, Object e2) {
		int cat1 = category(e1);
		int cat2 = category(e2);

		if (cat1 != cat2)
			return cat1 - cat2;

		// cat1 == cat2

		if (cat1 == PROJECTS) {
			IWorkbenchAdapter a1= (IWorkbenchAdapter)((IAdaptable)e1).getAdapter(IWorkbenchAdapter.class);
			IWorkbenchAdapter a2= (IWorkbenchAdapter)((IAdaptable)e2).getAdapter(IWorkbenchAdapter.class);
			return getCollator().compare(a1.getLabel(e1), a2.getLabel(e2));
		}

		if (cat1 == SOURCEROOTS) {
			ISourceRoot root1= getSourceRoot(e1);
			ISourceRoot root2= getSourceRoot(e2);
			if (root1 == null) {
				if (root2 == null) {
					return 0;
				} else {
					return 1;
				}
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
		
		String name1;
		boolean e1destructor = false;
		String name2;
		boolean e2destructor = false;

		if (e1 instanceof ICElement) {
			name1 = ((ICElement)e1).getElementName();
		    if (e1 instanceof IMethodDeclaration) {
		        IMethodDeclaration method = (IMethodDeclaration)e1;
		        try {
			        if (method.isDestructor()) {
						name1 = ((ICElement)e1).getElementName().substring(1);
						e1destructor = true;
			        }
		        } catch (CModelException e) {
		        }
		    }
		} else {
			name1 = e1.toString();
		}
		if (e2 instanceof ICElement) {
			name2 = ((ICElement)e2).getElementName();
		    if (e2 instanceof IMethodDeclaration) {
		        IMethodDeclaration method = (IMethodDeclaration)e2;
		        try {
			        if (method.isDestructor()) {
						name2 = ((ICElement)e2).getElementName().substring(1);
						e2destructor = true;
			        }
		        } catch (CModelException e) {
		        }
		    }
		} else {
			name2 = e2.toString();
		}
		int result = getCollator().compare(name1, name2);
		if (result == 0 && (e1destructor != e2destructor)) {
		    result = e1destructor ? 1 : -1;
		}
		return result;
	}

	private ISourceRoot getSourceRoot(Object element) {
		ICElement celement = (ICElement)element;
		while (! (celement instanceof ISourceRoot) && celement != null) {
			celement = celement.getParent();
		}
		return (ISourceRoot)celement;
	}

	private int compareWithLabelProvider(Viewer viewer, Object e1, Object e2) {
		if (viewer == null || !(viewer instanceof ContentViewer)) {
			IBaseLabelProvider prov = ((ContentViewer) viewer).getLabelProvider();
			if (prov instanceof ILabelProvider) {
				ILabelProvider lprov= (ILabelProvider) prov;
				String name1 = lprov.getText(e1);
				String name2 = lprov.getText(e2);
				if (name1 != null && name2 != null) {
					return getCollator().compare(name1, name2);
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
