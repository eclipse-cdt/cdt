/*******************************************************************************
 * Copyright (c) 2005, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *******************************************************************************/
package org.eclipse.cdt.internal.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.model.IWorkbenchAdapter;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IArchive;
import org.eclipse.cdt.core.model.IArchiveContainer;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.IBinaryContainer;
import org.eclipse.cdt.core.model.IBinaryModule;
import org.eclipse.cdt.core.model.ICContainer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IInclude;
import org.eclipse.cdt.core.model.IMacro;
import org.eclipse.cdt.core.model.IMember;
import org.eclipse.cdt.core.model.INamespace;
import org.eclipse.cdt.core.model.IParent;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.ISourceRoot;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.ui.CDTUITools;
import org.eclipse.cdt.ui.CElementGrouping;
import org.eclipse.cdt.ui.IncludesGrouping;
import org.eclipse.cdt.ui.NamespacesGrouping;

/**
 * A base content provider for C elements. It provides access to the
 * C element hierarchy without listening to changes in the C model.
 * Use this class when you want to present the C elements
 * in a modal dialog or wizard.
 * <p>
 * The following C element hierarchy is surfaced by this content provider:
 * <p>
 * <pre>
C model (<code>ICModel</code>)<br>
   C project (<code>ICProject</code>)<br>
      Source root (<code>ISourceRoot</code>)<br>
      C Container(folders) (<code>ICContainer</code>)<br>
      Translation unit (<code>ITranslationUnit</code>)<br>
      Binary file (<code>IBinary</code>)<br>
      Archive file (<code>IArchive</code>)<br>
      Non C Resource file (<code>Object</code>)<br>

 * </pre>
 */
public class BaseCElementContentProvider implements ITreeContentProvider {

	protected static final Object[] NO_CHILDREN= new Object[0];

	protected boolean fProvideMembers= false;
	protected boolean fProvideWorkingCopy= false;
	protected boolean fIncludesGrouping= false;
	protected boolean fNamespacesGrouping= false;
	protected boolean fMemberGrouping= false;
	protected boolean fMacroGrouping= false;

	public BaseCElementContentProvider() {
		this(false, false);
	}

	public BaseCElementContentProvider(boolean provideMembers, boolean provideWorkingCopy) {
	    fProvideMembers= provideMembers;
		fProvideWorkingCopy= provideWorkingCopy;
	}

	/**
	 * Returns whether the members are provided when asking
	 * for a TU's children.
	 */
	public boolean getProvideMembers() {
		return fProvideMembers;
	}

	/**
	 * Returns whether the members are provided when asking
	 * for a TU's children.
	 */
	public void setProvideMembers(boolean b) {
		fProvideMembers= b;
	}

	/**
	 * Sets whether the members are provided from
	 * a working copy of a compilation unit
	 */
	public void setProvideWorkingCopy(boolean b) {
		fProvideWorkingCopy= b;
	}

	/**
	 * Returns whether the members are provided
	 * from a working copy a compilation unit.
	 */
	public boolean getProvideWorkingCopy() {
		return fProvideWorkingCopy;
	}

	/**
	 * Can elements be group.
	 */
	public boolean areIncludesGroup() {
	    return fIncludesGrouping;
	}

	/**
	 * Allow Elements to be group.
	 * @param b
	 */
	public void setIncludesGrouping(boolean b) {
	    fIncludesGrouping = b;
	}

	/**
	 * Can elements be group.
	 */
	public boolean areNamespacesGroup() {
	    return fNamespacesGrouping;
	}

	/**
	 * Allow Elements to be group.
	 * @param b
	 */
	public void setNamespacesGrouping(boolean b) {
	    fNamespacesGrouping = b;
	}

	/**
	 * @return whether grouping of members is enabled
	 */
	public boolean isMemberGroupingEnabled() {
		return fMemberGrouping;
	}

	/**
	 * Enable/disable member grouping by common namespace.
	 * @param enable
	 */
	public void setMemberGrouping(boolean enable) {
		fMemberGrouping = enable;
	}

	/**
	 * @return whether grouping of macros is enabled
	 */
	public boolean isMacroGroupingEnabled() {
		return fMacroGrouping;
	}

	/**
	 * Enable/disable marco grouping
	 * @param enable
	 */
	public void setMacroGrouping(boolean enable) {
		fMacroGrouping = enable;
	}

	/* (non-Cdoc)
	 * Method declared on IContentProvider.
	 */
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	/* (non-Cdoc)
	 * Method declared on IContentProvider.
	 */
	@Override
	public void dispose() {
	}

	/* (non-Cdoc)
	 * Method declared on IStructuredContentProvider.
	 */
	@Override
	public Object[] getElements(Object parent) {
		return getChildren(parent);
	}

	/* (non-Cdoc)
	 * Method declared on ITreeContentProvider.
	 */
	@Override
	public Object[] getChildren(Object element) {
		if (!exists(element))
			return NO_CHILDREN;

		try {
			if (element instanceof ICModel) {
				return  getCProjects((ICModel)element);
			} else if  (element instanceof ICProject ) {
				return getSourceRoots((ICProject)element);
			} else if (element instanceof ICContainer) {
				return getCResources((ICContainer)element);
			} else if (element instanceof ITranslationUnit) {
				// if we want to get the children of a translation unit
				if (fProvideMembers) {
					// if we want to use the working copy of it
					ITranslationUnit tu = (ITranslationUnit)element;
					if (fProvideWorkingCopy){
						// if it is not already a working copy
						if (!(element instanceof IWorkingCopy)){
							// if it has a valid working copy
							IWorkingCopy copy = CDTUITools.getWorkingCopyManager().findSharedWorkingCopy(tu);
							if (copy != null) {
								tu = copy;
							}
						}
					}
					return getTranslationUnitChildren(tu);
				}
			} else if (element instanceof IBinary) {
				return ((IBinary)element).getChildren();
			} else if (element instanceof IArchive) {
				return ((IArchive)element).getChildren();
			} else if (element instanceof IBinaryModule) {
				return ((IBinaryModule)element).getChildren();
			} else if (element instanceof INamespace) {
				return getNamespaceChildren((INamespace) element);
			} else if (element instanceof ISourceReference  && element instanceof IParent) {
				return ((IParent)element).getChildren();
			} else if (element instanceof IProject) {
				return getResources((IProject)element);
			} else if (element instanceof IFolder) {
				return getResources((IFolder)element);
			} else if (element instanceof CElementGrouping) {
				return ((CElementGrouping)element).getChildren(element);
			}
		} catch (CModelException e) {
			//CUIPlugin.log(e);
			return NO_CHILDREN;
		}
		return NO_CHILDREN;
	}

	/* (non-Cdoc)
	 *
	 * @see ITreeContentProvider
	 */
	@Override
	public boolean hasChildren(Object element) {
		if (fProvideMembers) {
			// assume TUs and binary files are never empty
			if (element instanceof IBinary || element instanceof ITranslationUnit || element instanceof IArchive) {
				return true;
			}
		} else {
			// don't allow to drill down into a compilation unit or class file
			if (element instanceof ITranslationUnit || element instanceof IBinary || element instanceof IArchive
					|| element instanceof IFile) {
				return false;
			}
		}

		if (element instanceof ICProject) {
			ICProject cp= (ICProject)element;
			if (!cp.getProject().isOpen()) {
				return false;
			}
			return true;
		}

		if (element instanceof ICContainer) {
			ICContainer container= (ICContainer)element;
			IResource resource= container.getResource();
			if (resource instanceof IContainer) {
				try {
					return ((IContainer)resource).members().length > 0;
				} catch (CoreException exc) {
					return false;
				}
			}
		}

		if (element instanceof IParent) {
			// when we have C children return true, else we fetch all the children
			if (((IParent)element).hasChildren()) {
				return true;
			}
		}

		if (element instanceof CElementGrouping) {
			return true;
		}

		Object[] children= getChildren(element);
		return (children != null) && children.length > 0;
	}

	/* (non-Cdoc)
	 * Method declared on ITreeContentProvider.
	 */
	@Override
	public Object getParent(Object element) {
		if (!exists(element)) {
			return null;
		}
		return internalGetParent(element);
	}

	public Object internalGetParent(Object element) {
		if (element instanceof IResource) {
			IResource parent= ((IResource)element).getParent();
			if (parent != null && parent.isAccessible()) {
				ICElement cParent= CoreModel.getDefault().create(parent);
				if (cParent != null && cParent.exists()) {
					return cParent;
				}
			}
			return parent;
		}
		Object parent = null;
		if (element instanceof ICElement) {
			if (element instanceof ISourceRoot && !CCorePlugin.showSourceRootsAtTopOfProject()) {
				parent = ((ICElement) element).getResource().getParent();
				if (parent instanceof IProject) {
					return ((ICElement) element).getCProject();
				}
			}
			else
				parent = ((ICElement)element).getParent();
			// translate working copy parent to original TU,
			// because working copies are never returned by getChildren
			// this is necessary for proper show-in-target support
			if (parent instanceof IWorkingCopy) {
				parent= ((IWorkingCopy)parent).getOriginalElement();
			}
		} else if (element instanceof IWorkbenchAdapter) {
			parent = ((IWorkbenchAdapter)element).getParent(element);
		}

		// if the parent is the default ISourceRoot == ICProject  return the project
		if (parent instanceof ISourceRoot) {
			if (isProjectSourceRoot((ISourceRoot)parent)) {
				parent = ((ISourceRoot)parent).getCProject();
			}
		} else if (parent instanceof IBinaryContainer || parent instanceof IArchiveContainer) {
			// If the virtual container is the parent we must find the legitimate parent.
			if (element instanceof ICElement) {
				IResource res = ((ICElement)element).getResource();
				if (res != null) {
					parent = internalGetParent(res);
				}
			}
		}
		if (parent instanceof INamespace && fNamespacesGrouping) {
			final INamespace namespace = (INamespace)parent;
			final NamespacesGrouping grouping = new NamespacesGrouping(namespace.getTranslationUnit(), namespace, fMemberGrouping);
			if (grouping.getNamespaces().length > 2) {
				parent = grouping;
			}
		}
		if (parent instanceof IMember && fMemberGrouping) {
			final IMember member = (IMember)parent;
			final String ns = getElementNamespace(member);
			if (ns != null) {
				Object parentParent = member.getParent();
				if (parentParent instanceof INamespace && fNamespacesGrouping) {
					final INamespace namespace = (INamespace)parent;
					final NamespacesGrouping grouping = new NamespacesGrouping(namespace.getTranslationUnit(), namespace);
					if (grouping.getNamespaces().length > 2) {
						parentParent = grouping;
					}
				}
				return new MembersGrouping(parentParent, ns);
			}
		}
		// if we are doing grouping for the includes return the grouping container.
		if (element instanceof IInclude && fIncludesGrouping) {
			parent = new IncludesGrouping(((IInclude)element).getTranslationUnit());
		}
		if (element instanceof IMacro && fMacroGrouping) {
			parent = new MacrosGrouping(((IMacro)element).getTranslationUnit());
		}
		return parent;
	}

	protected Object[] getCProjects(ICModel cModel) throws CModelException {
		Object[] objects = cModel.getCProjects();
		try {
			Object[] nonC = cModel.getNonCResources();
			if (nonC.length > 0) {
				objects = concatenate(objects, nonC);
			}
		} catch (CModelException e) {
			//
		}
		return objects;
	}

	protected Object[] getSourceRoots(ICProject cproject) throws CModelException {
		if (!cproject.getProject().isOpen())
			return NO_CHILDREN;

		List<ICElement> list= new ArrayList<ICElement>();
		ICElement[] children = cproject.getChildren();
		for (ICElement child : children) {
			if (child instanceof ISourceRoot && child.getResource().getType() == IResource.PROJECT) {
				// Was a source root at the project, get the children of this element
				ICElement[] c2 = ((ISourceRoot)child).getChildren();
				for (int k = 0; k < c2.length; ++k)
					list.add(c2[k]);
			} else if (CCorePlugin.showSourceRootsAtTopOfProject()) {
				list.add(child);
			} else if (child instanceof ISourceRoot &&
						child.getResource().getParent().equals(cproject.getProject())) {
				list.add(child);
		}
		}

		Object[] objects = list.toArray();
		Object[] nonC = cproject.getNonCResources();
		if (nonC != null && nonC.length > 0) {
			nonC = filterNonCResources(nonC, cproject);
			objects = concatenate(objects, nonC);
		}

		return objects;
	}

	protected Object[] getTranslationUnitChildren(ITranslationUnit unit) throws CModelException {
		Object[] children = unit.getChildren();
		if (fIncludesGrouping) {
			boolean hasInclude = false;
			ArrayList<Object> list = new ArrayList<Object>(children.length);
			for (int i = 0; i < children.length; i++) {
				if (!(children[i] instanceof IInclude)) {
					list.add(children[i]);
				} else {
					hasInclude = true;
				}
			}
			if (hasInclude) {
				list.add (0, new IncludesGrouping(unit));
			}
			children = list.toArray();
		}
		Map<String, NamespacesGrouping> nsmap = new HashMap<String, NamespacesGrouping>();
		if (fNamespacesGrouping) {
			// check if there is another namespace with the same name for the same parent
			List<Object> list = new ArrayList<Object>(children.length);
			for (int i = 0; i < children.length; ++i) {
				if (children[i] instanceof INamespace) {
					INamespace n1 = (INamespace)children[i];
					NamespacesGrouping namespacesGrouping = nsmap.get(n1.getElementName());
					if (namespacesGrouping == null) {
						namespacesGrouping = new NamespacesGrouping(unit, n1, fMemberGrouping);
						if (namespacesGrouping.getNamespaces().length > 1) {
							nsmap.put(n1.getElementName(), namespacesGrouping);
							list.add(namespacesGrouping);
						} else {
							list.add(children[i]);
						}
					}
				} else {
					list.add(children[i]);
				}
			}
			children = list.toArray();
		}
		if (fMemberGrouping) {
			// check if there is another member with the same namespace for the same parent
			List<Object> list = new ArrayList<Object>(children.length);
			Map<String, MembersGrouping> map = new HashMap<String, MembersGrouping>();
			for (int i = 0; i < children.length; ++i) {
				if (children[i] instanceof IMember) {
					final ICElement member = (ICElement)children[i];
					String namespace = getElementNamespace(member);
					MembersGrouping memberGrouping = map.get(namespace);
					if (memberGrouping == null) {
						memberGrouping = new MembersGrouping(unit, namespace);
						map.put(namespace, memberGrouping);
						list.add(memberGrouping);
					}
				} else if (fNamespacesGrouping && children[i] instanceof INamespace) {
					if (!nsmap.containsKey(((INamespace) children[i]).getElementName())) {
						list.add(children[i]);
					}
				} else {
					list.add(children[i]);
				}
			}
			children = list.toArray();
		}
		if (fMacroGrouping) {
			ArrayList<Object> list = new ArrayList<Object>(children.length);
			boolean hasMacros = false;
			for (int i = 0; i < children.length; i++) {
				if (!(children[i] instanceof IMacro))
					list.add(children[i]);
				else
					hasMacros = true;
			}
			if (hasMacros) {
				//Check if include gouping is there. If so, put macros after
				if(!list.isEmpty()){
					if(list.get(0) instanceof IncludesGrouping)
						list.add (1, new MacrosGrouping(unit));
					else
						list.add (0, new MacrosGrouping(unit));
				}
				else
					list.add (0, new MacrosGrouping(unit));
			}
			children = list.toArray();
		}
		return children;
	}

	protected Object[] getNamespaceChildren(IParent element) throws CModelException {
		Object[] children = element.getChildren();
		if (fMemberGrouping) {
			// check if there is another member with the same namespace for the same parent
			List<Object> list = new ArrayList<Object>(children.length);
			Map<String, MembersGrouping> map = new HashMap<String, MembersGrouping>();
			for (int i = 0; i < children.length; ++i) {
				if (children[i] instanceof IMember) {
					final ICElement member = (ICElement)children[i];
					String namespace = getElementNamespace(member);
					MembersGrouping memberGrouping = map.get(namespace);
					if (memberGrouping == null) {
						memberGrouping = new MembersGrouping(element, namespace);
						map.put(namespace, memberGrouping);
						list.add(memberGrouping);
					}
				} else {
					list.add(children[i]);
				}
			}
			children = list.toArray();
		}
		return children;
	}

	private static String getElementNamespace(ICElement member) {
		String name = member.getElementName();
		int idx = name.lastIndexOf("::"); //$NON-NLS-1$
		if (idx < 0) {
			return null;
		}
		return name.substring(0, idx);
	}

	protected Object[] getCResources(ICContainer container) throws CModelException {
		Object[] objects = null;
		ICElement[] children = container.getChildren();
		List<ICElement> missingElements = Collections.emptyList();
		if (!CCorePlugin.showSourceRootsAtTopOfProject()) {
			missingElements = getMissingElements(container, children);
		}
		try {
			objects = container.getNonCResources();
			if (objects.length > 0) {
				objects = filterNonCResources(objects, container.getCProject());
			}
		} catch (CModelException e) {
		}

		Object[] result = children;
		if (missingElements.size() > 0) {
            result = concatenate(result, missingElements.toArray());
		}

		if (objects != null && objects.length > 0) {
			result = concatenate(result, objects);
		}

		return result;
	}

	private List<ICElement> getMissingElements(ICContainer container, ICElement[] elements) {
		// nested source roots may be filtered out below the project root,
		// we need to find them to add them back in
		List<ICElement> missingElements = new ArrayList<ICElement>();
		try {
			List<IResource> missingContainers = new ArrayList<IResource>();
			IResource[] allChildren = ((IContainer) container.getResource()).members();
			for (IResource child : allChildren) {
				if (!(child instanceof IContainer))
					continue;
				boolean found = false;
				for (ICElement element : elements) {
					if (element.getResource().equals(child)) {
						found = true;
						break;
					}
				}
				if (!found)
					missingContainers.add(child);
			}
			for (IResource resource : missingContainers) {
				ICElement element = container.getCProject().findElement(resource.getFullPath());
				if (element != null)
					missingElements.add(element);
			}
		} catch (CoreException e1) {
		}
		return missingElements;
	}

	protected Object[] getResources(IProject project) {
		try {
			return project.members();
		} catch (CoreException e) {
		}
		return NO_CHILDREN;
	}

	protected Object[] getResources(IFolder folder) throws CModelException {
		ICProject cproject = CoreModel.getDefault().create(folder.getProject());
		Object[] members = null;
		try {
			members = folder.members();
		} catch (CoreException e) {
			//
		}
		if (members == null || members.length == 0) {
			return NO_CHILDREN;
		}
		return filterNonCResources(members, cproject);
	}

	private Object[] filterNonCResources(Object[] objects, ICProject cproject) throws CModelException {
		ICElement[] binaries = null;
		ICElement[] archives = null;
		try {
			binaries = getBinaries(cproject);
			archives = getArchives(cproject);
		} catch (CModelException e) {
			archives = binaries = new ICElement[0];
		}
		ISourceRoot[] roots = null;
		try {
			roots = cproject.getSourceRoots();
		} catch (CModelException e) {
			roots = new ISourceRoot[0];
		}
		List<Object> nonCResources = new ArrayList<Object>(objects.length);
		for (Object object : objects) {
			Object o= object;
			// A folder can also be a source root in the following case
			// Project
			//  + src <- source folder
			//    + excluded <- excluded from class path
			//      + included  <- a new source folder.
			// Included is a member of excluded, but since it is rendered as a source
			// folder we have to exclude it as a normal child.
			if (o instanceof IFolder) {
				IFolder folder = (IFolder)o;
				ISourceRoot root = null;
				for (int j = 0; j < roots.length; j++) {
					if (roots[j].getPath().equals(folder.getFullPath())) {
						root = roots[j];
						break;
					}
				}
				// it is a sourceRoot skip it.
				if (root != null) {
					if (CCorePlugin.showSourceRootsAtTopOfProject())
					continue;
					else
						o = root;
				}
			} else if (o instanceof IFile){
				boolean found = false;
				for (ICElement binarie : binaries) {
					IResource res = binarie.getResource();
					if (o.equals(res)) {
						o = binarie;
						found = true;
						break;
					}
				}
				if (!found) {
					for (ICElement archive : archives) {
						IResource res = archive.getResource();
						if (o.equals(res)) {
							o = archive;
							break;
						}
					}
				}
			}
			nonCResources.add(o);
		}
		return nonCResources.toArray();
	}

	/**
	 * Note: This method is for internal use only. Clients should not call this method.
	 */
	protected boolean isProjectSourceRoot(ISourceRoot root) {
		IResource resource= root.getResource();
		return (resource instanceof IProject);
	}

	protected boolean exists(Object element) {
		if (element == null) {
			return false;
		}
		if (element instanceof IResource) {
			return ((IResource)element).exists();
		}
		if (element instanceof ICElement) {
			return ((ICElement)element).exists();
		}
		return true;
	}

	protected IBinary[] getBinaries(ICProject cproject) throws CModelException {
		IBinaryContainer container = cproject.getBinaryContainer();
		return getBinaries(container);
	}

	protected IBinary[] getBinaries(IBinaryContainer container) throws CModelException {
		ICElement[] celements = container.getChildren();
		ArrayList<IBinary> list = new ArrayList<IBinary>(celements.length);
		for (ICElement celement : celements) {
			if (celement instanceof IBinary) {
				IBinary bin = (IBinary)celement;
				list.add(bin);
			}
		}
		IBinary[] bins = new IBinary[list.size()];
		list.toArray(bins);
		return bins;
	}

	protected IArchive[] getArchives(ICProject cproject) throws CModelException {
		IArchiveContainer container = cproject.getArchiveContainer();
		return getArchives(container);
	}

	protected IArchive[] getArchives(IArchiveContainer container) throws CModelException {
		ICElement[] celements = container.getChildren();
		ArrayList<IArchive> list = new ArrayList<IArchive>(celements.length);
		for (ICElement celement : celements) {
			if (celement instanceof IArchive) {
				IArchive ar = (IArchive)celement;
				list.add(ar);
			}
		}
		IArchive[] ars = new IArchive[list.size()];
		list.toArray(ars);
		return ars;
	}

	/**
	 * Note: This method is for internal use only. Clients should not call this method.
	 */
	protected static Object[] concatenate(Object[] a1, Object[] a2) {
		int a1Len = a1.length;
		int a2Len = a2.length;
		Object[] res = new Object[a1Len + a2Len];
		System.arraycopy(a1, 0, res, 0, a1Len);
		System.arraycopy(a2, 0, res, a1Len, a2Len);
		return res;
	}

}
