package org.eclipse.cdt.internal.ui.cview;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.jface.viewers.ViewerSorter;

import org.eclipse.core.resources.IResource;

import org.eclipse.cdt.core.model.ICRoot;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ICFolder;
import org.eclipse.cdt.core.model.IArchive;
import org.eclipse.cdt.core.model.IBinaryContainer;
import org.eclipse.cdt.core.model.IArchiveContainer;
import org.eclipse.cdt.core.model.ICFile;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.CModelException;

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
		if (element instanceof ICRoot) {
			return 0;
		} else if (element instanceof ICProject) {
			return 10;
		} else if (element instanceof IBinaryContainer) {
			return 20;
		} else if (element instanceof IArchiveContainer) {
			return 30;
		} else if (element instanceof ICFolder) {
			return 40;
		} else if (element instanceof ICFile) {
			IResource res = null;
			try {
				res = ((ICFile)element).getUnderlyingResource();
			} catch (CModelException e) {
			}
			if (res != null) {
				String ext = res.getFileExtension();
				if (ext != null) {
					if (ext.equals("h") || ext.equals("hh")) {
						return 50;
					}
					if (ext.equals("c") || ext.equals("C") || ext.equals("cc") || ext.equals("cpp")) {
						return 51;
					}
					return 52;
				}
				return 53;
			}
			return 54;
		} else if (element instanceof ICElement) {
			String name = ((ICElement)element).getElementName();
			if (name.startsWith("__")) {
				return 68;
			}
			if (name.charAt(0) == '_') {
				return 67;
			}
			return 60;
		} else if (element instanceof IArchive) {
			return 70;
		}
		return 80;
	}
}
