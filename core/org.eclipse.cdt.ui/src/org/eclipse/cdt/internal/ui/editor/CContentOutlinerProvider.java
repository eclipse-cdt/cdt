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

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ElementChangedEvent;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.IElementChangedListener;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.ui.BaseCElementContentProvider;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.Viewer;

/*
 * CContentOutlinerProvider 
 */
public class CContentOutlinerProvider extends BaseCElementContentProvider {

	CContentOutlinePage fOutliner;
	ITranslationUnit root;
	private ElementChangedListener fListener;
	private IPropertyChangeListener fPropertyListener;

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

	class PropertyListener implements IPropertyChangeListener {

		/* (non-Javadoc)
		 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
		 */
		public void propertyChange(PropertyChangeEvent event) {
			String prop = event.getProperty();
//			if (prop.equals(PreferenceConstants.OUTLINE_GROUP_INCLUDES)) {
//				Object newValue = event.getNewValue();
//				if (newValue instanceof Boolean) {
//					boolean value = ((Boolean)newValue).booleanValue();
//					if (areIncludesGroup() != value) {
//						setIncludesGrouping(value);
//						if (fOutliner != null) {
//							fOutliner.contentUpdated();
//						}
//					}
//				}
//			}
		}

	}

	/**
	 * 
	 */
	public CContentOutlinerProvider(CContentOutlinePage outliner) {
		super(true, true);
		fOutliner = outliner;
		//setIncludesGrouping(PreferenceConstants.getPreferenceStore().getBoolean(PreferenceConstants.OUTLINE_GROUP_INCLUDES));
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
		if (fPropertyListener != null) {
			PreferenceConstants.getPreferenceStore().removePropertyChangeListener(fPropertyListener);
			fPropertyListener = null;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		boolean isTU= (newInput instanceof ITranslationUnit);

		if (isTU && fListener == null) {
			root = (ITranslationUnit)newInput;
			fListener= new ElementChangedListener();
			CoreModel.getDefault().addElementChangedListener(fListener);
			fPropertyListener = new PropertyListener();
			PreferenceConstants.getPreferenceStore().addPropertyChangeListener(fPropertyListener);
		} else if (!isTU && fListener != null) {
			CoreModel.getDefault().removeElementChangedListener(fListener);
			fListener= null;
			root = null;
		}
	}

}
