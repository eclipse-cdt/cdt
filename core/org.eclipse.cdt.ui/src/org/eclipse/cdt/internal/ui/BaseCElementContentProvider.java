package org.eclipse.cdt.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IArchive;
import org.eclipse.cdt.core.model.IArchiveContainer;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.IBinaryContainer;
import org.eclipse.cdt.core.model.ICContainer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IParent;
import org.eclipse.cdt.core.model.ISourceRoot;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
 
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
	
	public BaseCElementContentProvider() {
	}
	
	public BaseCElementContentProvider(boolean provideMembers, boolean provideWorkingCopy) {
		fProvideMembers= provideMembers;
		fProvideWorkingCopy= provideWorkingCopy;
	}
	
	/**
	 * Returns whether the members are provided when asking
	 * for a TU's or ClassFile's children.
	 */
	public boolean getProvideMembers() {
		return fProvideMembers;
	}

	/**
	 * Returns whether the members are provided when asking
	 * for a TU's or ClassFile's children.
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

	/* (non-Cdoc)
	 * Method declared on IStructuredContentProvider.
	 */
	public Object[] getElements(Object parent) {
		return getChildren(parent);
	}
	
	/* (non-Cdoc)
	 * Method declared on IContentProvider.
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	/* (non-Cdoc)
	 * Method declared on IContentProvider.
	 */
	public void dispose() {
	}

	/* (non-Cdoc)
	 * Method declared on ITreeContentProvider.
	 */
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
			} else if (element instanceof IBinaryContainer) {
				return NO_CHILDREN; // we deal with this in the CVIewContentProvider
			} else if (element instanceof IArchiveContainer) {
				return NO_CHILDREN; // we deal with this in the CViewContentProvider
			} else if (element instanceof ITranslationUnit) {
				// if we want to get the chidren of a translation unit
				if (fProvideMembers) {
					// if we want to use the working copy of it
					if(fProvideWorkingCopy){
						// if it is not already a working copy
						if(!(element instanceof IWorkingCopy)){
							// if it has a valid working copy
							ITranslationUnit tu = (ITranslationUnit)element;
							IWorkingCopy copy = tu.findSharedWorkingCopy(CUIPlugin.getDefault().getBufferFactory());
							if(copy != null) {
								return ((IParent)copy).getChildren();
							}
						}
					}
					return ((IParent)element).getChildren();
				}
			} else if (element instanceof IParent) {
				return ((IParent)element).getChildren();
			} else if (element instanceof IProject) {
				return getResources((IProject)element);
			} else if (element instanceof IFolder) {
				return getResources((IFolder)element);
			}
		} catch (CModelException e) {
			//CUIPlugin.getDefault().log(e);
			return NO_CHILDREN;
		}
		return NO_CHILDREN;
	}

	/* (non-Cdoc)
	 *
	 * @see ITreeContentProvider
	 */
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

		if (element instanceof IBinaryContainer) {
			try {
				IBinaryContainer cont = (IBinaryContainer)element;
				IBinary[] bins = getExecutables(cont);
				return (bins != null) && bins.length > 0;
			} catch (CModelException e) {
				return false;
			}
		}

		if (element instanceof IParent) {
			// when we have C children return true, else we fetch all the children
			if (((IParent)element).hasChildren()) {
				return true;
			}
		}
		Object[] children= getChildren(element);
		return (children != null) && children.length > 0;
	}
	 
	/* (non-Cdoc)
	 * Method declared on ITreeContentProvider.
	 */
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
			parent = ((ICElement)element).getParent();			
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
			
		List list= new ArrayList();
		ISourceRoot[] roots = cproject.getSourceRoots();
		// filter out source roots that correspond to projects and
		// replace them with the package fragments directly
		for (int i= 0; i < roots.length; i++) {
			ISourceRoot root= roots[i];
			if (isProjectSourceRoot(root)) {
				Object[] children= root.getChildren();
				for (int k= 0; k < children.length; k++) { 
					list.add(children[k]);
				}
			} else {
				list.add(root);
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

	protected Object[] getCResources(ICContainer container) throws CModelException {
		Object[] objects = null;
		Object[] children = container.getChildren();
		try {
			objects = container.getNonCResources();
			if (objects.length > 0) {
				objects = filterNonCResources(objects, container.getCProject());
			}
		} catch (CModelException e) {
		}
		if (objects == null || objects.length == 0) {
			return children;
		}
		return concatenate(children, objects);
	}

	private Object[] getResources(IProject project) {
		try {
			return project.members();
		} catch (CoreException e) {
		}
		return NO_CHILDREN;
	}

	private Object[] getResources(IFolder folder) throws CModelException {
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
		List nonCResources = new ArrayList(objects.length);
		for (int i= 0; i < objects.length; i++) {
			Object o= objects[i];
			// A folder can also be a source root in the following case
			// Project
			//  + src <- source folder
			//    + excluded <- excluded from class path
			//      + included  <- a new source folder.
			// Included is a member of excluded, but since it is rendered as a source
			// folder we have to exclude it as a normal child.
			if (o instanceof IFolder) {
				IFolder folder = (IFolder)o;
				boolean found = false;
				for (int j = 0; j < roots.length; j++) {
					if (roots[j].getPath().equals(folder.getFullPath())) {
						found = true;
						break;
					}
				}
				// it is a sourceRoot skip it.
				if (found) {
					continue;
				}
			} else if (o instanceof IFile){
				boolean found = false;
				for (int j = 0; j < binaries.length; j++) {
					IResource res = binaries[j].getResource();
					if (o.equals(res)) {
						o = binaries[j];
						found = true;
						break;
					}
				}
				if (!found) {
					for (int j = 0; j < archives.length; j++) {
						IResource res = archives[j].getResource();
						if (o.equals(res)) {
							o = archives[j];
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

	protected IBinary[] getExecutables(ICProject cproject) throws CModelException {
		IBinaryContainer container = cproject.getBinaryContainer();
		return getExecutables(container);
	}
	
	protected IBinary[] getExecutables(IBinaryContainer container) throws CModelException {
		ICElement[] celements = container.getChildren();
		ArrayList list = new ArrayList(celements.length);
		for (int i = 0; i < celements.length; i++) {
			if (celements[i] instanceof IBinary) {
				IBinary bin = (IBinary)celements[i];
				if (bin.isExecutable() || bin.isSharedLib()) {
					list.add(bin);
				}
			}
		}
		IBinary[] bins = new IBinary[list.size()];
		list.toArray(bins);
		return bins;
	}

	protected IBinary[] getBinaries(ICProject cproject) throws CModelException {
		IBinaryContainer container = cproject.getBinaryContainer();
		return getBinaries(container);
	}

	protected IBinary[] getBinaries(IBinaryContainer container) throws CModelException {
		ICElement[] celements = container.getChildren();
		ArrayList list = new ArrayList(celements.length);
		for (int i = 0; i < celements.length; i++) {
			if (celements[i] instanceof IBinary) {
				IBinary bin = (IBinary)celements[i];
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
		ArrayList list = new ArrayList(celements.length);
		for (int i = 0; i < celements.length; i++) {
			if (celements[i] instanceof IArchive) {
				IArchive ar = (IArchive)celements[i];
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
