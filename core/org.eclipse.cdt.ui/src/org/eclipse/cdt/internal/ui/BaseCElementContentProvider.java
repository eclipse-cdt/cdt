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
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.resources.IContainer;
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
C model (<code>ICModel</code>)
   C project (<code>ICProject</code>)
      C Container(folders) (<code>ICContainer</code>)
      Translation unit (<code>ITranslationUnit</code>)
      Binary file (<code>IBinary</code>)
      Archive file (<code>IArchive</code>)
      Non C Resource file (<code>Object</code>)

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
		//fProvideWorkingCopy= provideWorkingCopy;
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
		//fProvideWorkingCopy= b;
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
		if (element instanceof ICElement) {
			ICElement celement = (ICElement)element;		
			if (celement instanceof ICModel) {
				return  getCProjects((ICModel)celement);
			} else if  (celement instanceof ICProject ) {
				return getCProjectResources((ICProject)celement);
			} else if (celement instanceof ICContainer) {
				return getCResources((ICContainer)celement);
			} else if (celement.getElementType() == ICElement.C_UNIT) {
				if (fProvideMembers) {
					return ((IParent)element).getChildren();
				}
			} else if (celement instanceof IParent) {
				return (Object[])((IParent)celement).getChildren();
			}
		}
		return getResources(element);
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
			if (element instanceof ITranslationUnit || element instanceof IBinary || element instanceof IArchive) {
				return false;
			}
		}
			
		if (element instanceof ICProject) {
			ICProject cp= (ICProject)element;
			if (!cp.getProject().isOpen()) {
				return false;
			} else {
				return true;	
			}
		}
 
		if (element instanceof ICContainer) {
			return true;
		}
		
		if (element instanceof IParent) {
			// when we have C children return true, else we fetch all the children
			return ((IParent)element).hasChildren();
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
		if (element instanceof ICElement) {
			return ((ICElement)element).getParent();			
		}
		if (element instanceof IResource) {
			IResource parent= ((IResource)element).getParent();
			ICElement cParent= CoreModel.getDefault().create(parent);
			if (cParent != null && cParent.exists()) {
				return cParent;
			}
			return parent;
		}
		return null;
	}
	
	protected Object[] getCProjects(ICModel cModel) {
		return cModel.getCProjects();
	}

	protected Object[] getCProjectResources(ICProject cproject) {
		Object[] objects = getCResources((ICContainer)cproject);
		IArchiveContainer archives = cproject.getArchiveContainer(); 
		if (archives.hasChildren()) {
			objects = concatenate(objects, new Object[] {archives});
		}
		IBinaryContainer bins = cproject.getBinaryContainer(); 
		if (bins.hasChildren()) {
			objects = concatenate(objects, new Object[] {bins});
		}
		return objects;
	}

	protected Object[] getCResources(ICContainer container) {
		Object[] objects = null;
		Object[] children = container.getChildren();
		try {
			objects = container.getNonCResources();
		} catch (CModelException e) {
		}
		if (objects == null) {
			return children;
		}
		return concatenate(children, objects);
	}

	private Object[] getResources(Object resource) {
		try {
			if (resource instanceof IContainer) {
				Object[] members= ((IContainer)resource).members();
				List nonCResources= new ArrayList();
				for (int i= 0; i < members.length; i++) {
					Object o= members[i];
					nonCResources.add(o);
				}
				return nonCResources.toArray();
			}
		} catch(CoreException e) {
		}
		return NO_CHILDREN;
	}
/*	
	protected boolean isBuildPathChange(ICElementDelta delta) {
		int flags= delta.getFlags();
		return (delta.getKind() == ICElementDelta.CHANGED && 
			((flags & ICElementDelta.F_ADDED_TO_CLASSPATH) != 0) ||
			 ((flags & ICElementDelta.F_REMOVED_FROM_CLASSPATH) != 0) ||
			 ((flags & ICElementDelta.F_CLASSPATH_REORDER) != 0));
	}
*/
	
	
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
