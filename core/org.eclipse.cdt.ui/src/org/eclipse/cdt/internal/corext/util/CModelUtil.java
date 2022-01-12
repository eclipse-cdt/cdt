/*******************************************************************************
 * Copyright (c) 2004, 2013 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Rational Software - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.corext.util;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICContainer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICModelStatus;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ISourceRoot;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.internal.ui.util.EditorUtility;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

public class CModelUtil {
	/**
	 * Returns the working copy TU of the given TU. If the TU is already a
	 * working copy or the TU has no working copy the input TU is returned.
	 */
	public static ITranslationUnit toWorkingCopy(ITranslationUnit unit) {
		if (!unit.isWorkingCopy()) {
			ITranslationUnit workingCopy = EditorUtility.getWorkingCopy(unit);
			if (workingCopy != null) {
				return workingCopy;
			}
		}
		return unit;
	}

	public static ITranslationUnit toOriginal(ITranslationUnit unit) {
		if (unit.isWorkingCopy()) {
			return (((IWorkingCopy) unit).getOriginalElement());
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
				return (ISourceRoot) root;
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
			ICElement curr = element;
			while (curr != null && !(curr instanceof ISourceRoot)) {
				curr = curr.getParent();
			}
			folder = (ISourceRoot) curr;
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
		IResource resource = root.getResource();
		if (resource != null) {
			IProject project = resource.getProject();
			IProject container = root.getCProject().getProject();
			return !container.equals(project);
		}
		return false;
	}

	/**
	 * Returns the translation unit the element belongs to or <code>null</code> if it does not.
	 */
	public static ITranslationUnit getTranslationUnit(ICElement elem) {
		while (elem != null) {
			if (elem instanceof ITranslationUnit) {
				return (ITranslationUnit) elem;
			}
			elem = elem.getParent();
		}
		return null;
	}

	/*
	 * Don't log not-exists exceptions
	 */
	public static boolean isExceptionToBeLogged(CoreException exception) {
		if (!(exception instanceof CModelException))
			return true;
		CModelException ce = (CModelException) exception;
		ICModelStatus status = ce.getCModelStatus();
		if (status == null || !status.doesNotExist())
			return true;
		return false;
	}
}
