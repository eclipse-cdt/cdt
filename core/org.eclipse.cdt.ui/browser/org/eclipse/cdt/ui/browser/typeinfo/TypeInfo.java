/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.browser.typeinfo;

import java.util.ArrayList;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IParent;
import org.eclipse.cdt.core.search.BasicSearchMatch;
import org.eclipse.cdt.core.search.ICSearchScope;
import org.eclipse.cdt.internal.core.index.StringMatcher;
import org.eclipse.cdt.internal.ui.browser.util.ArrayUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;


/**
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class TypeInfo extends BasicSearchMatch implements ITypeInfo
{
	private final static int[] cElementTypes= {
		ICElement.C_NAMESPACE,
		ICElement.C_CLASS,
		ICElement.C_TEMPLATE_CLASS,
		ICElement.C_STRUCT,
		ICElement.C_TEMPLATE_STRUCT,
		ICElement.C_UNION,
		ICElement.C_TEMPLATE_UNION,
		ICElement.C_ENUMERATION,
		ICElement.C_TYPEDEF
	};

	public static int[] getAllCElementTypes() {
		return cElementTypes;
	}

	public static boolean isValidCElementType(int type) {
		return ArrayUtil.contains(cElementTypes, type);
	}

	private final static String scopeResolutionOperator= "::"; //$NON-NLS-1$
	private final static String fileScopeSeparator= " : "; //$NON-NLS-1$
	private static final StringMatcher fSystemTypeMatcher= new StringMatcher("_*", true, false); //$NON-NLS-1$
	
	public TypeInfo() {
		super();
	}
	
	public boolean isEnclosed(ICSearchScope scope) {
		return scope.encloses(getFilePath());
	}
	
	public boolean isSystemType() {
		// recognized low-level system types eg __FILE
		String[] names= getEnclosingNames();
		if (names != null) {
			for (int i= 0; i < names.length; ++i) {
				if (fSystemTypeMatcher.match(names[i]))
					return true;
			}
		}
		return fSystemTypeMatcher.match(getName());
	}

	public String getFileName() {
		if (resource != null)
			return resource.getName();
		else if (path != null)
			return path.lastSegment();
		else
			return null;
	}

	public String getFilePath() {
		if (resource != null)
			return resource.getFullPath().toString();
		else if (path != null)
			return path.toString();
		else
			return null;
	}

	public String getFileExtension() {
		if (resource != null)
			return resource.getFileExtension();
		else if (path != null)
			return path.getFileExtension();
		else
			return null;
	}

	public String getQualifiedParentName() {
		StringBuffer buf= new StringBuffer();
		String fileName = getFileName();
		if (fileName != null && fileName.length() > 0)
			buf.append(fileName);
		String parentName = getParentName();
		if (parentName != null && parentName.length() > 0) {
			buf.append(fileScopeSeparator); //$NON-NLS-1$
			buf.append(parentName);
		}
		return buf.toString();
	}
	
	public String getFullyQualifiedName() {
		StringBuffer buf= new StringBuffer();
		String fileName = getFileName();
		if (fileName != null && fileName.length() > 0)
			buf.append(fileName);
		String parentName = getParentName();
		if (parentName != null && parentName.length() > 0) {
			buf.append(fileScopeSeparator); //$NON-NLS-1$
			buf.append(parentName);
			buf.append(scopeResolutionOperator); //$NON-NLS-1$
		}
		String name = getName();
		if (name != null && name.length() > 0)
			buf.append(name);
		return buf.toString();
	}
	
	public String getQualifiedName() {
		StringBuffer buf= new StringBuffer();
		String parentName = getParentName();
		if (parentName != null && parentName.length() > 0) {
			buf.append(parentName);
			buf.append(scopeResolutionOperator); //$NON-NLS-1$
		}
		String name = getName();
		if (name != null && name.length() > 0)
			buf.append(name);
		return buf.toString();
	}
	
	public String[] getEnclosingNames() {
		//TODO pull up this method into BasicSearchMatch
		//since it already has access to this info
		String parentName= getParentName();
		if (parentName == null)
			return null;

		ArrayList names= new ArrayList(5);
		int lastIndex= 0;
		String nextName;
		int qualifierIndex= parentName.indexOf(scopeResolutionOperator, 0);
		while (qualifierIndex >= 0) {
			nextName= parentName.substring(lastIndex, qualifierIndex);
			lastIndex= qualifierIndex + scopeResolutionOperator.length();
			names.add(nextName);
			qualifierIndex= parentName.indexOf(scopeResolutionOperator, lastIndex);
		}
		nextName= parentName.substring(lastIndex);
		names.add(nextName);

		return (String[]) names.toArray(new String[names.size()]);
	}	

	public String toString() {
		return getFullyQualifiedName();
	}
	
	private boolean matchesCType(ICElement celement, String name) {
		if (isValidCElementType(celement.getElementType()))
			return celement.getElementName().equals(name);
		return false;
	}
	
	private ICElement findCElement(ICElement celement, String name) {
		if (matchesCType(celement, name))
			return celement;
		else if (celement instanceof IParent) {
			ICElement[] children = ((IParent)celement).getChildren();
			for (int i = 0; i < children.length; i++) {
				if (matchesCType(children[i], name))
					return children[i];
			}
		}
		return null;
	}
	
	public ICElement getCElement() {
		if (resource != null && resource.getType() == IResource.FILE) {
			ICElement parentElement= CoreModel.getDefault().create((IFile)resource);
			if (parentElement instanceof IParent) {
				String[] names= getEnclosingNames();
				if (names != null) {
					for (int i= 0; i < names.length; ++i) {
						parentElement= findCElement(parentElement, names[i]);
						if (parentElement == null)
							break;
					}
				}
				if (parentElement != null)
					return findCElement(parentElement, getName());
			}
		}
		return null;
	}
}
