/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.navigator;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.ui.cview.CViewMessages;
import org.eclipse.cdt.internal.ui.util.ExceptionHandler;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.ui.navigator.CommonDropAdapter;
import org.eclipse.ui.navigator.CommonDropAdapterAssistant;

/**
 * A Common Navigator drop adapter assistant handling dropping of <code>ICElement</code>s.
 * 
 * @see org.eclipse.cdt.internal.ui.cview.SelectionTransferDropAdapter
 * @see org.eclipse.cdt.internal.ui.cview.CView#initDrop()
 */
public class CNavigatorDropAdapterAssistant extends CommonDropAdapterAssistant {

	/*
	 * @see org.eclipse.ui.navigator.CommonDropAdapterAssistant#handleDrop(org.eclipse.ui.navigator.CommonDropAdapter, org.eclipse.swt.dnd.DropTargetEvent, java.lang.Object)
	 */
	public IStatus handleDrop(CommonDropAdapter dropAdapter,
			DropTargetEvent event, Object target) {
		try {
			switch(event.detail) {
				case DND.DROP_MOVE:
					return handleDropMove(target, event);
				case DND.DROP_COPY:
					return handleDropCopy(target, event);
			}
		} catch (CModelException e){
			ExceptionHandler.handle(e, CViewMessages.getString("SelectionTransferDropAdapter.error.title"), CViewMessages.getString("SelectionTransferDropAdapter.error.message")); //$NON-NLS-1$ //$NON-NLS-2$
			return e.getStatus();
		} catch(InvocationTargetException e) {
			ExceptionHandler.handle(e, CViewMessages.getString("SelectionTransferDropAdapter.error.title"), CViewMessages.getString("SelectionTransferDropAdapter.error.exception")); //$NON-NLS-1$ //$NON-NLS-2$
			return Status.CANCEL_STATUS;
		} catch (InterruptedException e) {
			//ok
		} finally {
			// The drag source listener must not perform any operation
			// since this drop adapter did the remove of the source even
			// if we moved something.
			event.detail= DND.DROP_NONE;
		}
		return Status.CANCEL_STATUS;
	}

	/*
	 * @see org.eclipse.ui.navigator.CommonDropAdapterAssistant#validateDrop(java.lang.Object, int, org.eclipse.swt.dnd.TransferData)
	 */
	public IStatus validateDrop(Object target, int operation,
			TransferData transferType) {
		try {
			switch(operation) {
				case DND.DROP_DEFAULT:
					return handleValidateMove(target); 
				case DND.DROP_COPY:
					return handleValidateCopy(target);
				case DND.DROP_MOVE:
					return handleValidateMove(target);
			}
		} catch (CModelException e){
			ExceptionHandler.handle(e, CViewMessages.getString("SelectionTransferDropAdapter.error.title"), CViewMessages.getString("SelectionTransferDropAdapter.error.message")); //$NON-NLS-1$ //$NON-NLS-2$
		}	
		return Status.CANCEL_STATUS;
	}

	private IStatus handleValidateCopy(Object target) throws CModelException{
		if (target != null) {

			ISelection selection = LocalSelectionTransfer.getTransfer().getSelection();
			ICElement[] cElements= getCElements(selection);
			
			if (!canCopyElements(cElements))
				return Status.CANCEL_STATUS;	
	
			if (target instanceof ISourceReference) {
				return Status.OK_STATUS;
			}
		
		}
		return Status.CANCEL_STATUS;
	}

	private IStatus handleValidateMove(Object target) throws CModelException {
		if (target != null) {

			ISelection selection = LocalSelectionTransfer.getTransfer().getSelection();
			ICElement[] cElements= getCElements(selection);
			
			if (!canMoveElements(cElements))
				return Status.CANCEL_STATUS;	
	
			if (target instanceof ISourceReference) {
				return Status.OK_STATUS;
			}
		
		}
		return Status.CANCEL_STATUS;
	}

	private IStatus handleDropCopy(final Object target, DropTargetEvent event) throws CModelException, InvocationTargetException, InterruptedException{
		ISelection selection = LocalSelectionTransfer.getTransfer().getSelection();
		final ICElement[] cElements= getCElements(selection);

		if (target instanceof ICElement) {
			ICElement cTarget = (ICElement)target;
			ICElement parent = cTarget;
			boolean isTargetTranslationUnit = cTarget instanceof ITranslationUnit;
			if (!isTargetTranslationUnit) {
				parent = cTarget.getParent();
			}
			final ICElement[] containers = new ICElement[cElements.length];
			for (int i = 0; i < containers.length; ++i) {
				containers[i] = parent;
			}
			ICElement[] neighbours = null;
			if (!isTargetTranslationUnit) {
				neighbours = new ICElement[cElements.length];
				for (int i = 0; i < neighbours.length; ++i) {
					neighbours[i] = cTarget;
				}
			}
			final ICElement[] siblings = neighbours;
			IRunnableWithProgress runnable = new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						CoreModel.getDefault().getCModel().copy(cElements, containers, siblings, null, false, monitor);
					} catch (CModelException e) {
						throw new InvocationTargetException(e);
					}
				}
			};
			run(runnable);
			return Status.OK_STATUS;
		}
		return Status.CANCEL_STATUS;
	}

	private IStatus handleDropMove(final Object target, DropTargetEvent event) throws CModelException, InvocationTargetException, InterruptedException{
		ISelection selection = LocalSelectionTransfer.getTransfer().getSelection();
		final ICElement[] cElements= getCElements(selection);
		
		if (target instanceof ICElement) {
			ICElement cTarget = (ICElement)target;
			ICElement parent = cTarget;
			boolean isTargetTranslationUnit = cTarget instanceof ITranslationUnit;
			if (!isTargetTranslationUnit) {
				parent = cTarget.getParent();
			}
			final ICElement[] containers = new ICElement[cElements.length];
			for (int i = 0; i < containers.length; ++i) {
				containers[i] = parent;
			}
			ICElement[] neighbours = null;
			if (!isTargetTranslationUnit) {
				neighbours = new ICElement[cElements.length];
				for (int i = 0; i < neighbours.length; ++i) {
					neighbours[i] = cTarget;
				}
			}
			final ICElement[] siblings = neighbours;
			IRunnableWithProgress runnable = new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						CoreModel.getDefault().getCModel().move(cElements, containers, siblings, null, false, monitor);
					} catch (CModelException e) {
						throw new InvocationTargetException(e);
					}
				}
			};
			run(runnable);
			return Status.OK_STATUS;
		}
		return Status.CANCEL_STATUS;
	}

	public void run(IRunnableWithProgress runnable) throws InterruptedException, InvocationTargetException {
		IRunnableContext context= new ProgressMonitorDialog(getShell());
		context.run(true, true, runnable);
	}

	public static ICElement[] getCElements(ISelection selection) {
		if (!(selection instanceof IStructuredSelection)) {
			return null;
		}
		List elements = ((IStructuredSelection)selection).toList();
		List resources= new ArrayList(elements.size());
		for (Iterator iter= elements.iterator(); iter.hasNext();) {
			Object element= iter.next();
			if (element instanceof ITranslationUnit) {
				continue;
			}
			if (element instanceof ICElement)
				resources.add(element);
		}
		return (ICElement[]) resources.toArray(new ICElement[resources.size()]);
	}

	private static boolean canCopyElements(ICElement[] cElements) {
		if (cElements != null) {
			return hasCommonParent(cElements);
		}
		return false;
	}		
	
	private static boolean canMoveElements(ICElement[] cElements) {
		if (cElements != null) {
			return hasCommonParent(cElements);
		}
		return false;
	}		
	
	private static boolean hasCommonParent(ICElement[] elements) {
		if (elements.length > 1) {
			ICElement parent = elements[0];
			for (int i = 0; i < elements.length; ++i) {
				ICElement p = elements[i].getParent();
				if (parent == null && p!= null) {
					return false;
				} else if (!parent.equals(p)){
					return false;
				}
			}
		}
		return true;
	}

}
