/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.corext.refactoring;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IMember;
import org.eclipse.cdt.core.model.IOpenable;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;

public class ResourceUtil {
	
	private ResourceUtil(){
	}
	
	public static IFile[] getFiles(ITranslationUnit[] cus) {
		List files= new ArrayList(cus.length);
		for (int i= 0; i < cus.length; i++) {
			IResource resource= ResourceUtil.getResource(cus[i]);
			if (resource.getType() == IResource.FILE)
				files.add(resource);
		}
		return (IFile[]) files.toArray(new IFile[files.size()]);
	}

	public static IFile getFile(ITranslationUnit cu) {
		IResource resource= ResourceUtil.getResource(cu);
		if (resource.getType() == IResource.FILE)
			return (IFile)resource;
		else
			return null;
	}

	//----- other ------------------------------
			
	/**
	 * Finds an <code>IResource</code> for a given <code>ITranslationUnit</code>.
	 * If the parameter is a working copy then the <code>IResource</code> for
	 * the original element is returned.
	 */
	public static IResource getResource(ITranslationUnit cu) {
		return cu.getResource();
	}


	/**
	 * Returns the <code>IResource</code> that the given <code>IMember</code> is defined in.
	 * @see #getResource
	 */
	public static IResource getResource(IMember member) {
		//Assert.isTrue(!member.isBinary());
		return getResource(member.getTranslationUnit());
	}

	public static IResource getResource(Object o){
		if (o instanceof IResource)
			return (IResource)o;
		if (o instanceof ICElement)
			return getResource((ICElement)o);
		return null;	
	}

	private static IResource getResource(ICElement element){
		if (element.getElementType() == ICElement.C_UNIT) 
			return getResource((ITranslationUnit) element);
		else if (element instanceof IOpenable) 
			return element.getResource();
		else	
			return null;	
	}
}
