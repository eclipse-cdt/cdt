/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.utils;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.mapping.IResourceChangeDescriptionFactory;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.ResourceChangeChecker;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ITranslationUnit;

import org.eclipse.cdt.internal.corext.util.CModelUtil;
import org.eclipse.cdt.internal.corext.util.Resources;

/**
 * This class defines a set of reusable static checks methods.
 */
public class Checks {

	/*
	 * no instances
	 */
	private Checks(){
	}

	public static boolean startsWithUpperCase(String s) {
		if (s == null) {
			return false;
		} else if ("".equals(s)) { //$NON-NLS-1$
			return false;
		} else {
			// Workaround for JDK bug (see 26529)
			return s.charAt(0) == Character.toUpperCase(s.charAt(0));
		}
	}

	public static boolean startsWithLowerCase(String s){
		if (s == null) {
			return false;
		} else if ("".equals(s)) { //$NON-NLS-1$
			return false;
	    } else {
			// Workaround for JDK bug (see 26529)
			return s.charAt(0) == Character.toLowerCase(s.charAt(0));
		}
	}

	public static boolean resourceExists(IPath resourcePath){
		return ResourcesPlugin.getWorkspace().getRoot().findMember(resourcePath) != null;
	}

	public static boolean isReadOnly(Object element) throws CModelException {
		if (element instanceof IResource)
			return isReadOnly((IResource) element);

		if (element instanceof ICElement) {
			return isReadOnly(((ICElement) element).getResource());
		}

		Assert.isTrue(false, "Not expected to get here"); //$NON-NLS-1$
		return false;
	}

	public static boolean isReadOnly(IResource res) throws CModelException {
		ResourceAttributes attributes= res.getResourceAttributes();
		if (attributes != null && attributes.isReadOnly())
			return true;

		if (! (res instanceof IContainer))
			return false;

		IContainer container= (IContainer)res;
		IResource[] children;
		try {
			children = container.members();
			for (int i= 0; i < children.length; i++) {
				if (isReadOnly(children[i]))
					return true;
			}
		} catch (CModelException e){
			throw e;
		} catch (CoreException e) {
			throw new CModelException(e);
		}
		return false;
	}

	//-------- validateEdit checks ----

	public static RefactoringStatus validateModifiesFiles(IFile[] filesToModify, Object context) {
		RefactoringStatus result= new RefactoringStatus();
		IStatus status= Resources.checkInSync(filesToModify);
		if (!status.isOK())
			result.merge(RefactoringStatus.create(status));
		status= Resources.makeCommittable(filesToModify, context);
		if (!status.isOK()) {
			result.merge(RefactoringStatus.create(status));
			if (!result.hasFatalError()) {
				result.addFatalError(Messages.Checks_validateEdit);
			}
		}
		return result;
	}

	public static void addModifiedFilesToChecker(IFile[] filesToModify, CheckConditionsContext context) {
		ResourceChangeChecker checker= (ResourceChangeChecker) context.getChecker(ResourceChangeChecker.class);
		IResourceChangeDescriptionFactory deltaFactory= checker.getDeltaFactory();

		for (int i= 0; i < filesToModify.length; i++) {
			deltaFactory.change(filesToModify[i]);
		}
	}

	public static RefactoringStatus validateEdit(ITranslationUnit tu, Object context) {
		IResource resource= CModelUtil.toOriginal(tu).getResource();
		RefactoringStatus result= new RefactoringStatus();
		if (resource == null)
			return result;
		IStatus status= Resources.checkInSync(resource);
		if (!status.isOK())
			result.merge(RefactoringStatus.create(status));
		status= Resources.makeCommittable(resource, context);
		if (!status.isOK()) {
			result.merge(RefactoringStatus.create(status));
			if (!result.hasFatalError()) {
				result.addFatalError(Messages.Checks_validateEdit);
			}
		}
		return result;
	}
}
