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

import org.eclipse.cdt.core.browser.AllTypesCache;
import org.eclipse.cdt.core.browser.ITypeInfo;
import org.eclipse.cdt.core.browser.TypeUtil;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.INamespace;
import org.eclipse.cdt.core.model.IParent;
import org.eclipse.cdt.core.model.ISourceRoot;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.swt.widgets.Shell;

class MembersViewContentProvider extends CBrowsingContentProvider {

	MembersViewContentProvider(CBrowsingPart browsingPart) {
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
				ITypeInfo info = (ITypeInfo) element;
				return (info.getCElementType() != ICElement.C_TYPEDEF);
			}
			
			if (element instanceof IParent) {
				return ((IParent)element).hasChildren();
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
		if (element == null || (element instanceof ICElement && !((ICElement)element).exists())) {
			return INVALID_INPUT;
		}
		
		try {
			startReadInDisplayThread();
			
			if (element instanceof ITypeInfo) {
				ITypeInfo info = (ITypeInfo) element;
				if (info.getCElementType() == ICElement.C_NAMESPACE) {
					return INVALID_INPUT;		// shouldn't get here...
				}
				if (info.getCElementType() == ICElement.C_TYPEDEF) {
					return EMPTY_CHILDREN;
				}
				ICElement elem = AllTypesCache.getElementForType(info, true, true, null);
				if (elem == null) {
				    return ERROR_NO_CHILDREN;
				}
				if (elem instanceof IParent) {
					ICElement[] children = ((IParent)elem).getChildren();
					if (children != null && children.length > 0)
						return children;
				}
				return EMPTY_CHILDREN;
			}
			
			if (element instanceof IParent) {
				ICElement[] children = ((IParent)element).getChildren();
				if (children != null && children.length > 0)
				    return children;
				return EMPTY_CHILDREN;
			}
			
			return INVALID_INPUT;
		} catch (CModelException e) {
			return ERROR_CANCELLED;
		} finally {
			finishedReadInDisplayThread();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
	 */
	public Object getParent(Object element) {
		if (element instanceof ICModel || element instanceof ICProject || element instanceof ISourceRoot) {
			return null;
		}
		
		if (element instanceof ITypeInfo) {
		    return null;
		}
	    
		try {
			startReadInDisplayThread();
		
			if (element instanceof ICElement) {
			    ICElement celem = (ICElement)element;
			    if (TypeUtil.isMemberType(celem)) {
					ICElement parent = TypeUtil.getDeclaringType(celem);
					if (parent == null || parent instanceof INamespace) {
				        ITypeInfo info = AllTypesCache.getTypeForElement(celem, true, true, null);
				        if (info != null)
				            return info.getEnclosingType();
					}
					return parent;
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

	protected Shell getShell() {
		return CUIPlugin.getActiveWorkbenchShell();
	}
}
