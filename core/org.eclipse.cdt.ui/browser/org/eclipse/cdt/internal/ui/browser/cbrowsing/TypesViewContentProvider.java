/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.browser.cbrowsing;

import org.eclipse.cdt.core.browser.ITypeInfo;
import org.eclipse.cdt.core.model.ICElement;

class TypesViewContentProvider extends CBrowsingContentProvider {

	TypesViewContentProvider(CBrowsingPart browsingPart) {
		super(browsingPart);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
	 */
	public boolean hasChildren(Object element) {
		if (element == null || (element instanceof ITypeInfo && !((ITypeInfo)element).exists())) {
			return false;
		}

		try {
			startReadInDisplayThread();
		
			if (element instanceof ITypeInfo) {
				ITypeInfo info = (ITypeInfo)element;
				return info.hasEnclosedTypes();
			}

			return false;
//		} catch (CModelException e) {
//			return false;
		} finally {
			finishedReadInDisplayThread();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object element) {
		if (element == null || (element instanceof ITypeInfo && !((ITypeInfo)element).exists())) {
			return INVALID_INPUT;
		}
		
		try {
			startReadInDisplayThread();
			
			if (element instanceof ITypeInfo) {
				ITypeInfo info = (ITypeInfo)element;
				final int kinds[] = { ICElement.C_CLASS, ICElement.C_STRUCT,
						ICElement.C_UNION, ICElement.C_ENUMERATION,
						ICElement.C_TYPEDEF};
				//TODO this should be a prefs option
				ITypeInfo[] children = info.getEnclosedTypes(kinds);
				if (children != null && children.length > 0) {
				    return children;
				}
				return EMPTY_CHILDREN;
			}

			return INVALID_INPUT;
//		} catch (CModelException e) {
//			return NO_CHILDREN;
		} finally {
			finishedReadInDisplayThread();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
	 */
	public Object getParent(Object element) {
		if (element == null || (element instanceof ITypeInfo && !((ITypeInfo)element).exists())) {
			return null;
		}

		try {
			startReadInDisplayThread();
		
			if (element instanceof ITypeInfo) {
				ITypeInfo info = (ITypeInfo)element;
				if (info.isEnclosedType()) {
					return info.getEnclosingType();
				} else {
//					return info.getEnclosingProject();
					return null;
				}
			}

			return null;
//		} catch (CModelException e) {
//			return false;
		} finally {
			finishedReadInDisplayThread();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}
}
