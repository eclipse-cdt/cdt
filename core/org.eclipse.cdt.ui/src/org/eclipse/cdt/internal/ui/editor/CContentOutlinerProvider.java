/**********************************************************************
 * Copyright (c) 2002,2003,2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.internal.ui.editor;

import java.util.ArrayList;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ElementChangedEvent;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.IElementChangedListener;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.ui.BaseCElementContentProvider;
import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.model.WorkbenchAdapter;

/*
 * CContentOutlinerProvider 
 */
public class CContentOutlinerProvider extends BaseCElementContentProvider {

	CContentOutlinePage fOutliner;
	ITranslationUnit root;
	private ElementChangedListener fListener;

	/**
	 * The element change listener of the java outline viewer.
	 * @see IElementChangedListener
	 */
	class ElementChangedListener implements IElementChangedListener {
		
		public void elementChanged(final ElementChangedEvent e) {
			
			ICElementDelta delta= findElement(root, e.getDelta());
			if (delta != null && fOutliner != null) {
				fOutliner.contentUpdated();
				return;
			}
			// TODO: We should be able to be smarter then a dum refresh
//			ICElementDelta delta= findElement(base, e.getDelta());
//			if (delta != null && fOutlineViewer != null) {
//				fOutlineViewer.reconcile(delta);
//			}
		}
		
		private boolean isPossibleStructuralChange(ICElementDelta cuDelta) {
			if (cuDelta.getKind() != ICElementDelta.CHANGED) {
				return true; // add or remove
			}
			int flags= cuDelta.getFlags();
			if ((flags & ICElementDelta.F_CHILDREN) != 0) {
				return true;
			}
			return (flags & (ICElementDelta.F_CONTENT | ICElementDelta.F_FINE_GRAINED)) == ICElementDelta.F_CONTENT;
		}
		
		protected ICElementDelta findElement(ICElement unit, ICElementDelta delta) {
			
			if (delta == null || unit == null)
				return null;
			
			ICElement element= delta.getElement();
			
			if (unit.equals(element)) {
				if (isPossibleStructuralChange(delta)) {
					return delta;
				}
				return null;
			}
	
			if (element.getElementType() > ICElement.C_UNIT)
				return null;
				
			ICElementDelta[] children= delta.getAffectedChildren();
			if (children == null || children.length == 0)
				return null;
				
			for (int i= 0; i < children.length; i++) {
				ICElementDelta d= findElement(unit, children[i]);
				if (d != null)
					return d;
			}
			
			return null;
		}
	}

	/**
	 * VirtualGrouping
	 */
	public class IncludesContainer extends WorkbenchAdapter implements IAdaptable {
		ITranslationUnit tu;

		public IncludesContainer(ITranslationUnit unit) {
			tu = unit;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.model.IWorkbenchAdapter#getChildren(java.lang.Object)
		 */
		public Object[] getChildren(Object object) {
			try {
				return tu.getChildrenOfType(ICElement.C_INCLUDE).toArray();
			} catch (CModelException e) {
			}
			return NO_CHILDREN;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.model.IWorkbenchAdapter#getLabel(java.lang.Object)
		 */
		public String getLabel(Object object) {
			return "include statements";
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.model.IWorkbenchAdapter#getImageDescriptor(java.lang.Object)
		 */
		public ImageDescriptor getImageDescriptor(Object object) {
			return CPluginImages.DESC_OBJS_INCCONT;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.model.IWorkbenchAdapter#getParent(java.lang.Object)
		 */
		public Object getParent(Object object) {
			return tu;
		}

		/*
		 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(Class)
		 */
		public Object getAdapter(Class clas) {
			if (clas == IWorkbenchAdapter.class)
				return this;
			return null;
		}

	}

	/**
	 * 
	 */
	public CContentOutlinerProvider(CContentOutlinePage outliner) {
		super(true, true);
		fOutliner = outliner;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object element) {
		if (element instanceof ITranslationUnit) {
			ITranslationUnit unit = (ITranslationUnit)element;
			try {
				if (fOutliner != null && fOutliner.isIncludesGroupingEnabled()) {
					boolean hasInclude = false;
					ICElement[] children = unit.getChildren();
					ArrayList list = new ArrayList(children.length);
					for (int i = 0; i < children.length; i++) {
						if (children[i].getElementType() != ICElement.C_INCLUDE) {
							list.add(children[i]);
						} else {
							hasInclude = true;
						}
					}
					if (hasInclude) {
						list.add (0, new IncludesContainer(unit));
					}
					return list.toArray();
				}
			} catch (CModelException e) {
				return NO_CHILDREN;
			}
			
		} else if (element instanceof IncludesContainer) {
			IncludesContainer includes = (IncludesContainer)element;
			return includes.getChildren(element);
		}
		return super.getChildren(element);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {
		super.dispose();
		if (fListener != null) {
			CoreModel.getDefault().removeElementChangedListener(fListener);
			fListener= null;
		}		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		boolean isTU= (newInput instanceof ITranslationUnit);

		if (isTU && fListener == null) {
			fListener= new ElementChangedListener();
			CoreModel.getDefault().addElementChangedListener(fListener);
			root = (ITranslationUnit)newInput;
		} else if (!isTU && fListener != null) {
			CoreModel.getDefault().removeElementChangedListener(fListener);
			fListener= null;
			root = null;
		}
	}

}
