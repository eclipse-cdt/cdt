/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.browser.cbrowsing;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;

import org.eclipse.cdt.core.browser.AllTypesCache;
import org.eclipse.cdt.core.browser.ITypeInfo;
import org.eclipse.cdt.core.browser.ITypeReference;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IParent;
import org.eclipse.cdt.internal.ui.browser.opentype.OpenTypeMessages;
import org.eclipse.cdt.internal.ui.util.ExceptionHandler;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

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
			return NO_CHILDREN;
		}
		
		try {
			startReadInDisplayThread();
			
			if (element instanceof IStructuredSelection) {
				Assert.isLegal(false);
				Object[] result= new Object[0];
				Class clazz= null;
				Iterator iter= ((IStructuredSelection)element).iterator();
				while (iter.hasNext()) {
					Object item=  iter.next();
					if (clazz == null)
						clazz= item.getClass();
					if (clazz == item.getClass())
						result= concatenate(result, getChildren(item));
					else
						return NO_CHILDREN;
				}
				return result;
			}
			
			if (element instanceof ITypeInfo) {
				ITypeInfo info = (ITypeInfo) element;
				if (info.getCElementType() == ICElement.C_NAMESPACE) {
					return NO_CHILDREN;		// shouldn't get here...
				}
				ICElement elem = getCElement(info);
				if (elem != null && elem instanceof IParent) {
					return ((IParent)elem).getChildren();
				}
				return NO_CHILDREN;
			}
			
			if (element instanceof IParent) {
				return ((IParent)element).getChildren();
			}

			return NO_CHILDREN;
		} catch (CModelException e) {
			return NO_CHILDREN;
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

	private ICElement getCElement(ITypeInfo info) {
		ITypeReference location = info.getResolvedReference();
		if (location == null) {
			final ITypeInfo[] typesToResolve = new ITypeInfo[] { info };
			IRunnableWithProgress runnable = new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					AllTypesCache.resolveTypeLocation(typesToResolve[0], monitor);
					if (monitor.isCanceled()) {
						throw new InterruptedException();
					}
				}
			};

			IProgressService service = PlatformUI.getWorkbench().getProgressService();
			try {
				service.busyCursorWhile(runnable);
			} catch (InvocationTargetException e) {
				String title = OpenTypeMessages.getString("OpenTypeAction.exception.title"); //$NON-NLS-1$
				String message = OpenTypeMessages.getString("OpenTypeAction.exception.message"); //$NON-NLS-1$
				ExceptionHandler.handle(e, title, message);
				return null;
			} catch (InterruptedException e) {
				// cancelled by user
				return null;
			}
	
			location = info.getResolvedReference();
		}
	
		ICElement elem = null;
		if (location != null)
			elem = info.getCElement();

		if (location == null) {
			// could not resolve location
			String title = OpenTypeMessages.getString("OpenTypeAction.errorTitle"); //$NON-NLS-1$
			String message = OpenTypeMessages.getFormattedString("OpenTypeAction.errorTypeNotFound", info.getQualifiedTypeName().toString()); //$NON-NLS-1$
			MessageDialog.openError(getShell(), title, message);
		}
		return elem;
	}
	
	protected Shell getShell() {
		return CUIPlugin.getActiveWorkbenchShell();
	}
}
