package org.eclipse.cdt.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
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
import org.eclipse.cdt.core.model.ILibraryReference;
import org.eclipse.cdt.core.model.IMacro;
import org.eclipse.cdt.core.model.IMethod;
import org.eclipse.cdt.core.model.IMethodDeclaration;
import org.eclipse.cdt.core.model.INamespace;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IUsing;
import org.eclipse.cdt.core.model.IVariable;
import org.eclipse.cdt.core.model.IVariableDeclaration;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 *	A sorter to sort the file and the folders in the C viewer in the following order:
 * 	1st Project
 * 	2nd BinaryContainer
 *  3nd ArchiveContainer
 *  4  Folder
 *  5  C File
 *  6 the reset
 */
public class CElementSorter extends ViewerSorter { 

	private static final int CMODEL = 0;
	private static final int PROJECT = 10;
	private static final int RESOURCE = 200;

	public int category (Object element) {
		if (element instanceof ICModel) {
			return CMODEL;
		} else if (element instanceof ICProject) {
			return PROJECT;
		} else if (element instanceof IBinaryContainer) {
			return 20;
		} else if (element instanceof IArchiveContainer) {
			return 30;
		} else if (element instanceof ICContainer) {
			return 40;
		} else if (element instanceof ITranslationUnit) {
			IResource res = null;
			res = ((ITranslationUnit)element).getUnderlyingResource();
			if (res != null) {
				String ext = res.getFileExtension();
				if (ext != null) {
					String[] headers = CoreModel.getDefault().getHeaderExtensions();
					for (int i = 0; i < headers.length; i++) {
						if (ext.equals(headers[i])) {
							return 42;
						}
					}
					String[] sources = CoreModel.getDefault().getSourceExtensions();
					for (int i = 0; i < sources.length; i++) {
						if (ext.equals(sources[i])) {
							return 47;
						}
					}					
					return 48;
				}
				return 49;
			}
			return 50;
		} else if (element instanceof IInclude) {
			return 60;
		} else if (element instanceof IMacro) {
			return 70;
		} else if (element instanceof INamespace) {
			return 80;
		} else if (element instanceof IUsing) {
			return 90;
		} else if (element instanceof IFunctionDeclaration && ! (element instanceof IFunction)) {
			return 100;
		} else if (element instanceof IMethodDeclaration && !(element instanceof IMethod)) {
			return 110;
		} else if (element instanceof IVariableDeclaration) {
			return 120;
		} else if (element instanceof IVariable) {
			String name = ((ICElement)element).getElementName();
			if (name.startsWith("__")) {
				return 122;
			}
			if (name.charAt(0) == '_') {
				return 124;
			}
			return 130;
		} else if (element instanceof IFunction) {
			String name = ((ICElement)element).getElementName();
			if (name.startsWith("__")) {
				return 132;
			}
			if (name.charAt(0) == '_') {
				return 134;
			}
			return 140;
		} else if (element instanceof IArchive) {
			return 150;
		} else if (element instanceof IBinary) {
			return 160;
		} else if (element instanceof ILibraryReference) {
			return 170;
		} else if (element instanceof ICElement) {
			String name = ((ICElement)element).getElementName();
			if (name.startsWith("__")) {
				return 172;
			}
			if (name.charAt(0) == '_') {
				return 174;
			}
			return 180;
		}
		return RESOURCE;
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

		if (cat1 == PROJECT) {
			IWorkbenchAdapter a1= (IWorkbenchAdapter)((IAdaptable)e1).getAdapter(IWorkbenchAdapter.class);
			IWorkbenchAdapter a2= (IWorkbenchAdapter)((IAdaptable)e2).getAdapter(IWorkbenchAdapter.class);
			return getCollator().compare(a1.getLabel(e1), a2.getLabel(e2));
		}

		// non - c resources are sorted using the label from the viewers label provider
		if (cat1 == RESOURCE) {
			return compareWithLabelProvider(viewer, e1, e2);
		}
		
		String name1;
		String name2;

		if (e1 instanceof ICElement) {
			name1 = ((ICElement)e1).getElementName();
		} else {
			name1 = e1.toString();
		}
		if (e2 instanceof ICElement) {
			name2 = ((ICElement)e2).getElementName();
		} else {
			name2 = e2.toString();
		}
		return getCollator().compare(name1, name2);		
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

}
