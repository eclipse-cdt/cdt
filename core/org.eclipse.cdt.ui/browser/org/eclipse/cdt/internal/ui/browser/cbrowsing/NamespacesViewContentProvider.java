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

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;

import org.eclipse.cdt.core.browser.AllTypesCache;
import org.eclipse.cdt.core.browser.ITypeInfo;
import org.eclipse.cdt.core.browser.ITypeSearchScope;
import org.eclipse.cdt.core.browser.TypeSearchScope;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ISourceRoot;
import org.eclipse.cdt.internal.ui.browser.opentype.OpenTypeMessages;
import org.eclipse.cdt.internal.ui.util.ExceptionHandler;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

class NamespacesViewContentProvider extends CBrowsingContentProvider {

	NamespacesViewContentProvider(CBrowsingPart browsingPart) {
		super(browsingPart);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
	 */
	public boolean hasChildren(Object element) {
		if (element == null || (element instanceof ICElement && !((ICElement)element).exists())) {
			return false;
		}

		try {
			startReadInDisplayThread();
		
			if (element instanceof ICProject) {
				return true;
//				TypeSearchScope scope = new TypeSearchScope();
//				scope.add((ICProject)element);
//				return AllTypesCache.getNamespaces(scope, true);
			}

			if (element instanceof ISourceRoot) {
				return true;
//				TypeSearchScope scope = new TypeSearchScope();
//				scope.add((ISourceRoot)element);
//				return AllTypesCache.getNamespaces(scope, true);
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
			
			if (element instanceof ICProject) {
				TypeSearchScope scope = new TypeSearchScope();
				scope.add((ICProject)element);
				return getNamespaces(scope);
			}

			if (element instanceof ISourceRoot) {
				TypeSearchScope scope = new TypeSearchScope();
				scope.add((ISourceRoot)element);
				return getNamespaces(scope);
			}

			return NO_CHILDREN;
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

	private Object[] getNamespaces(final ITypeSearchScope scope) {
		if (!AllTypesCache.isCacheUpToDate(scope)) {
			IRunnableWithProgress runnable = new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					AllTypesCache.updateCache(scope, monitor);
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
				return NO_CHILDREN;
			} catch (InterruptedException e) {
				// cancelled by user
				return NO_CHILDREN;
			}
		}
		return AllTypesCache.getNamespaces(scope, true);
	}
	
	protected Shell getShell() {
		return CUIPlugin.getActiveWorkbenchShell();
	}
}
