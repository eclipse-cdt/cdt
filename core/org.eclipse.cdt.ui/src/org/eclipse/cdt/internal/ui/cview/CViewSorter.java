package org.eclipse.cdt.internal.ui.cview;

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
import org.eclipse.jface.viewers.ViewerSorter;

/**
 *	A sorter to sort the file and the folders in the C viewer in the following order:
 * 	1st Project
 * 	2nd BinaryContainer
 *  3nd ArchiveContainer
 *  4  Folder
 *  5  C File
 *  6 the reset
 */
public class CViewSorter extends ViewerSorter { 
	
	public int category (Object element) {
		if (element instanceof ICModel) {
			return 0;
		} else if (element instanceof ICProject) {
			return 10;
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
		return 200;
	}
}
