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
package org.eclipse.cdt.core.browser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IParent;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfoProvider;
import org.eclipse.cdt.core.search.ICSearchScope;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;


/**
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class TypeInfo implements ITypeInfo, Comparable
{
	protected final static String scopeResolutionOperator= "::"; //$NON-NLS-1$
	protected final static String fileScopeSeparator= " : "; //$NON-NLS-1$
	private String hashString= null;
	private int hashCode= 0;
	private String name= null;
	private int type= 0;
	private String[] enclosingNames= null;
	private IResource resource= null;
	private IPath path= null;
	private int startOffset= 0;
	private int endOffset= 0;
	private ICElement cElement= null;
	
	public TypeInfo(String name, int type, String[] enclosingNames, IResource resource, IPath path, int startOffset, int endOffset) {
		init(name, type, enclosingNames, resource, path, startOffset, endOffset);
	}

	public TypeInfo(String fullName, int type, IPath path, int startOffset, int endOffset) {
		String name= fullName;
		String parentName= null;
		int qualifierIndex= fullName.lastIndexOf(scopeResolutionOperator);
		if (qualifierIndex >= 0) {
			parentName= fullName.substring(0, qualifierIndex);
			name= fullName.substring(qualifierIndex+2);
		}
		String[] enclosingNames= null;
		if (parentName != null)
			enclosingNames= parseScopedName(parentName);

		init(name, type, enclosingNames, null, path, startOffset, endOffset);
	}

	public TypeInfo(TypeInfo info) {
		init(info.name, info.type, info.enclosingNames, info.resource, info.path, info.startOffset, info.endOffset);
	}

	private void init(String name, int type, String[] enclosingNames, IResource resource, IPath path, int startOffset, int endOffset) {
		this.name= name;
		this.type= type;
		if (enclosingNames != null) {
			this.enclosingNames= new String[enclosingNames.length];
			System.arraycopy(enclosingNames, 0, this.enclosingNames, 0, enclosingNames.length);
		}
		this.resource= resource;
		if (path == null && resource != null)
			path= resource.getFullPath();
		this.path= path;
		this.startOffset= startOffset;
		this.endOffset= endOffset;
	}
	
	public String getName() {
		return name;
	}
	
	public int getType() {
		return type;
	}

	public String[] getEnclosingNames() {
		return enclosingNames;
	}

	public IResource getResource() {
		return resource;
	}
	
	public IPath getPath() {
		if (resource != null)
			return resource.getFullPath();
		else
			return path;
	}

	public IPath getLocation() {
		if (resource != null)
			return resource.getLocation();
		else
			return path;
	}

	public int getStartOffset() {
		return startOffset;
	}

	public int getEndOffset() {
		return endOffset;
	}

	public String getFileName() {
		if (resource != null)
			return resource.getName();
		else if (path != null)
			return path.lastSegment();
		else
			return null;
	}

	public String getParentName() {
		if (enclosingNames != null) {
			StringBuffer buf= new StringBuffer();
			for (int i= 0; i < enclosingNames.length; ++i) {
				if (i > 0)
					buf.append(scopeResolutionOperator);
				buf.append(enclosingNames[i]);
			}
			return buf.toString();
		}
		return null;
	}

	public String getQualifiedParentName() {
		StringBuffer buf= new StringBuffer();
		String fileName = getFileName();
		if (fileName != null)
			buf.append(fileName);
		String parentName = getParentName();
		if (parentName != null) {
			if (fileName != null)
				buf.append(fileScopeSeparator);
			buf.append(parentName);
		}
		return buf.toString();
	}
	
	public String getQualifiedName() {
		StringBuffer buf= new StringBuffer();
		String parentName = getParentName();
		if (parentName != null)
			buf.append(parentName);
		String name = getName();
		if (name != null) {
			if (parentName != null)
				buf.append(scopeResolutionOperator);
			buf.append(name);
		}
		return buf.toString();
	}
	
	public String getFullyQualifiedName() {
		StringBuffer buf= new StringBuffer();
		String fileName = getFileName();
		if (fileName != null)
			buf.append(fileName);
		String parentName = getParentName();
		if (parentName != null) {
			if (fileName != null)
				buf.append(fileScopeSeparator);
			buf.append(parentName);
		}
		String name = getName();
		if (name != null)
			if (parentName != null)
				buf.append(scopeResolutionOperator);
			else if (fileName != null)
				buf.append(fileScopeSeparator);
			buf.append(name);
		return buf.toString();
	}
	
	public String toString() {
		return getFullyQualifiedName();
	}
	
	public ICElement getCElement() {
		if (cElement == null)
			cElement= resolveCElement();
		return cElement;
	}

	private ICElement resolveCElement() {
		if (resource != null && resource.getType() == IResource.FILE) {
			ICElement parentElement= CoreModel.getDefault().create((IFile)resource);
			if (parentElement instanceof IParent) {
				if (enclosingNames != null) {
					for (int i= 0; i < enclosingNames.length; ++i) {
						parentElement= findCElement(parentElement, enclosingNames[i]);
						if (parentElement == null)
							break;
					}
				}
				if (parentElement != null)
					return findCElement(parentElement, name);
			}
		}
		return null;
	}
	
	private ICElement findCElement(ICElement celement, String name) {
		if (isValidType(celement.getElementType()) && celement.getElementName().equals(name))
			return celement;
		
		if (celement instanceof IParent) {
			ICElement[] children = ((IParent)celement).getChildren();
			for (int i = 0; i < children.length; i++) {
				ICElement child= children[i];
				if (isValidType(child.getElementType()) && child.getElementName().equals(name))
					return child;
			}
		}
		return null;
	}

	public IPath resolveIncludePath(ICProject cProject) {
		IPath fullPath= getLocation();
		if (cProject == null || fullPath == null)
			return null;
		IProject project= cProject.getProject();
		IScannerInfoProvider provider= CCorePlugin.getDefault().getScannerInfoProvider(project);
		if (provider != null) {
			IScannerInfo info= provider.getScannerInformation(project);
			if (info != null) {
				String[] includePaths= info.getIncludePaths();
				IPath relativePath= null;
				int mostSegments= 0;
				for (int i= 0; i < includePaths.length; ++i) {
					IPath includePath= new Path(includePaths[i]);
					if (includePath.isPrefixOf(fullPath)) {
						int segments= includePath.matchingFirstSegments(fullPath);
						if (segments > mostSegments) {
							relativePath= fullPath.removeFirstSegments(segments).setDevice(null);
							mostSegments= segments;
						}
					}
				}
				return relativePath;
			}
		}
		return null;
	}
	
	public boolean isEnclosed(ICSearchScope scope) {
		if (scope == null)
			return false;

		// check if path is in scope
		IPath path= getPath();
		if (path != null && scope.encloses(path.toString()))
			return true;
		
		// check include paths of enclosing projects
		IPath[] projectPaths= scope.enclosingProjects();
		if (projectPaths != null) {
			for (int i= 0; i < projectPaths.length; ++i) {
				IPath projPath= projectPaths[i];
				ICElement elem= CoreModel.getDefault().create(projPath);
				if (elem != null && elem instanceof ICProject) {
					ICProject proj= (ICProject) elem;
					if (resolveIncludePath(proj) != null)
						return true;
					// TODO search referenced projects too?
					// IProject[] refs= proj.getProject().getReferencedProjects();
				}
			}
		}
		return false;
	}
	
	public int hashCode() {
		if (hashString == null) {
			hashCode= getHashString().hashCode();
		}
		return hashCode;
	}
	
	private String getHashString() {
		if (hashString == null) {
			StringBuffer buf= new StringBuffer(64);

			IPath path= getLocation();
			if (path != null)
				buf.append(path.toString());

			buf.append(" ["); //$NON-NLS-1$
			buf.append(startOffset);
			buf.append("-"); //$NON-NLS-1$
			buf.append(endOffset);
			buf.append("] "); //$NON-NLS-1$
			
			String parentName= getParentName();
			if (parentName != null && parentName.length() > 0) {
				buf.append(parentName);
				buf.append(scopeResolutionOperator);
			}
			String name= getName();
			if (name != null && name.length() > 0)
				buf.append(name);

			buf.append(":"); //$NON-NLS-1$
			buf.append(type);

			hashString= buf.toString();
		}
		return hashString;
	}
	
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof TypeInfo)) {
			return false;
		}
		TypeInfo info= (TypeInfo)obj;
		if (hashCode() != info.hashCode())
			return false;
		return getHashString().equals(info.getHashString());
	}
	
	public int compareTo(Object obj) {
		if (obj == this) {
			return 0;
		}
		if( !(obj instanceof TypeInfo)) {
			throw new ClassCastException();
		}
		TypeInfo info= (TypeInfo)obj;
		return getHashString().compareTo(info.getHashString());
	}

	public static boolean isValidType(int type) {
		switch (type) {
			case ICElement.C_NAMESPACE:
			case ICElement.C_CLASS:
			case ICElement.C_STRUCT:
			case ICElement.C_UNION:
			case ICElement.C_ENUMERATION:
			case ICElement.C_TYPEDEF:
//			case ICElement.C_TEMPLATE_CLASS:
//			case ICElement.C_TEMPLATE_STRUCT:
//			case ICElement.C_TEMPLATE_UNION:
				return true;
			
			default:
				return false;
		}
	}
	
	public static String[] parseScopedName(String scopedName) {
		ArrayList names= new ArrayList(5);
		int lastIndex= 0;
		String nextName;
		int qualifierIndex= scopedName.indexOf(scopeResolutionOperator, 0);
		while (qualifierIndex >= 0) {
			nextName= scopedName.substring(lastIndex, qualifierIndex);
			lastIndex= qualifierIndex + scopeResolutionOperator.length();
			names.add(nextName);
			qualifierIndex= scopedName.indexOf(scopeResolutionOperator, lastIndex);
		}
		nextName= scopedName.substring(lastIndex);
		names.add(nextName);
		return (String[]) names.toArray(new String[names.size()]);
	}
	
	final static private Comparator TYPE_NAME_COMPARATOR= new Comparator() {
		public int compare(Object o1, Object o2) {
			return ((ITypeInfo)o1).getName().compareTo(((ITypeInfo)o2).getName());
		}
	};

	public static ITypeInfo findType(String name, IPath path, ITypeInfo[] elements) {
		if (elements == null)
			return null;

		ITypeInfo key= new TypeInfo(name, 0, path, 0, 0);

		int index= Arrays.binarySearch(elements, key, TYPE_NAME_COMPARATOR);
		if (index >= 0 && index < elements.length) {
			for (int i= index - 1; i >= 0; i--) {
				ITypeInfo curr= elements[i];
				if (key.getName().equals(curr.getName())) {
					if (key.getQualifiedName().equals(curr.getQualifiedName())) {
						return curr;
					}
				} else {
					break;
				}
			}
			for (int i= index; i < elements.length; i++) {
				ITypeInfo curr= elements[i];
				if (key.getName().equals(curr.getName())) {
					if (key.getQualifiedName().equals(curr.getQualifiedName())) {
						return curr;
					}
				} else {
					break;
				}
			}
		}
		return null;
	}

}
