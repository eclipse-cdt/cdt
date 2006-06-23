/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.corext.util;

import org.eclipse.cdt.core.model.ICContainer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ISourceRoot;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.internal.ui.util.EditorUtility;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

public class CModelUtil {
	/**
	 * Returns the working copy CU of the given CU. If the CU is already a
	 * working copy or the CU has no working copy the input CU is returned.
	 */	
	public static ITranslationUnit toWorkingCopy(ITranslationUnit unit) {
		if (!unit.isWorkingCopy()) {
			ITranslationUnit workingCopy= EditorUtility.getWorkingCopy(unit);
			if (workingCopy != null) {
				return workingCopy;
			}
		}
		return unit;
	}
	
	public static ITranslationUnit toOriginal(ITranslationUnit unit){
		if (unit.isWorkingCopy()) {
			return (((IWorkingCopy)unit).getOriginalElement());
		}
		return unit;
	}

	/**
	 * Returns the source root of <code>ICElement</code>. If the given
	 * element is already a source root, the element itself is returned.
	 */
	public static ISourceRoot getSourceRoot(ICElement element) {
		ICElement root = element;
		while (root != null) {
			if (root instanceof ISourceRoot)
				return (ISourceRoot)root;
			ICElement parent = root.getAncestor(ICElement.C_CCONTAINER);
			if (parent == root)
				return null;
			root = parent;
		}
		return null;
	}

	/**
	 * Returns the source folder of <code>ICElement</code>. If the given
	 * element is already a source folder, the element itself is returned.
	 */
	public static ICContainer getSourceFolder(ICElement element) {
		ICContainer folder = null;
	    if (element != null) {
			boolean foundSourceRoot = false;
			ICElement curr = element;
			while (curr != null && !foundSourceRoot) {
				if (curr instanceof ICContainer && folder == null) {
				    folder = (ICContainer)curr;
				}
				foundSourceRoot = (curr instanceof ISourceRoot);
				curr = curr.getParent();
			}
			if (folder == null) {
			    ICProject cproject = element.getCProject();
				folder = cproject.findSourceRoot(cproject.getProject());
			}
	    }
		return folder;
	}
	
	/**
	 * Returns <code>true</code> if the given source root is
	 * referenced. This means it is own by a different project but is referenced
	 * by the root's parent. Returns <code>false</code> if the given root
	 * doesn't have an underlying resource.
	 */
	public static boolean isReferenced(ISourceRoot root) {
		IResource resource= root.getResource();
		if (resource != null) {
			IProject project= resource.getProject();
			IProject container= root.getCProject().getProject();
			return !container.equals(project);
		}
		return false;
	}
}
