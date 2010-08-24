/*******************************************************************************
 * Copyright (c) 2006, 2010 Wind River Systems, Inc. and others.
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
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.CopyFilesAndFoldersOperation;
import org.eclipse.ui.actions.MoveFilesAndFoldersOperation;
import org.eclipse.ui.actions.ReadOnlyStateChecker;
import org.eclipse.ui.ide.dialogs.ImportTypeDialog;
import org.eclipse.ui.navigator.CommonDropAdapter;
import org.eclipse.ui.navigator.CommonDropAdapterAssistant;
import org.eclipse.ui.part.ResourceTransfer;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICContainer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.ITranslationUnit;

import org.eclipse.cdt.internal.ui.cview.CViewMessages;
import org.eclipse.cdt.internal.ui.util.ExceptionHandler;

/**
 * A Common Navigator drop adapter assistant handling dropping of <code>ICElement</code>s.
 * 
 * @see org.eclipse.cdt.internal.ui.cview.SelectionTransferDropAdapter
 */
public class CNavigatorDropAdapterAssistant extends CommonDropAdapterAssistant {

	private static final IResource[] NO_RESOURCES = new IResource[0];

	/*
	 * @see org.eclipse.ui.navigator.CommonDropAdapterAssistant#isSupportedType(org.eclipse.swt.dnd.TransferData)
	 */
	@Override
	public boolean isSupportedType(TransferData transferType) {
		return super.isSupportedType(transferType)
				|| ResourceTransfer.getInstance().isSupportedType(transferType)
				|| FileTransfer.getInstance().isSupportedType(transferType);
	}

	/*
	 * @see org.eclipse.ui.navigator.CommonDropAdapterAssistant#handleDrop(org.eclipse.ui.navigator.CommonDropAdapter, org.eclipse.swt.dnd.DropTargetEvent, java.lang.Object)
	 */
	@Override
	public IStatus handleDrop(CommonDropAdapter dropAdapter,
			DropTargetEvent event, Object target) {

		try {
			// drop in folder
			if (target instanceof ICContainer || 
					target instanceof ICProject || 
					target instanceof IContainer ||
					(dropAdapter.getCurrentOperation() == DND.DROP_COPY && (
							target instanceof IFile ||
							target instanceof ITranslationUnit))) {
	
				final Object data= event.data;
				if (data == null) {
					return Status.CANCEL_STATUS;
				}
				final IContainer destination= getDestination(target);
				if (destination == null) {
					return Status.CANCEL_STATUS;
				}
				IResource[] resources = null;
				TransferData currentTransfer = dropAdapter.getCurrentTransfer();
				final int dropOperation = dropAdapter.getCurrentOperation();
				if (LocalSelectionTransfer.getTransfer().isSupportedType(
						currentTransfer)) {
					resources = getSelectedResources();
					if (target instanceof ITranslationUnit) {
						if (handleDropCopy(target, event).isOK()) {
							// drop inside translation unit - we are done
							return Status.OK_STATUS;
						}
					}
				} else if (ResourceTransfer.getInstance().isSupportedType(
						currentTransfer)) {
					resources = (IResource[]) event.data;
				}
				if (FileTransfer.getInstance().isSupportedType(currentTransfer)) {
					final String[] names = (String[]) data;
					// Run the import operation asynchronously. 
					// Otherwise the drag source (e.g., Windows Explorer) will be blocked 
					// while the operation executes. Fixes bug 35796.
					Display.getCurrent().asyncExec(new Runnable() {
						public void run() {
							getShell().forceActive();
							CopyFilesAndFoldersOperation op= new CopyFilesAndFoldersOperation(getShell());
							op.copyOrLinkFiles(names, destination, dropOperation);
						}
					});
				} else if (event.detail == DND.DROP_COPY || event.detail == DND.DROP_LINK) {
					return performResourceCopy(dropAdapter, getShell(), resources);
				} else {
					ReadOnlyStateChecker checker = new ReadOnlyStateChecker(
						getShell(), 
						"Move Resource Action",	//$NON-NLS-1$
						"Move Resource Action");//$NON-NLS-1$	
					resources = checker.checkReadOnlyResources(resources);
					MoveFilesAndFoldersOperation operation = new MoveFilesAndFoldersOperation(getShell());
					operation.copyResources(resources, destination);
				}
				return Status.OK_STATUS;
			}
		
			switch(event.detail) {
				case DND.DROP_MOVE:
					return handleDropMove(target, event);
				case DND.DROP_COPY:
					return handleDropCopy(target, event);
			}
		} catch (CModelException e){
			ExceptionHandler.handle(e, CViewMessages.SelectionTransferDropAdapter_error_title, CViewMessages.SelectionTransferDropAdapter_error_message); 
			return e.getStatus();
		} catch(InvocationTargetException e) {
			ExceptionHandler.handle(e, CViewMessages.SelectionTransferDropAdapter_error_title, CViewMessages.SelectionTransferDropAdapter_error_exception); 
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
	@Override
	public IStatus validateDrop(Object target, int operation,
			TransferData transferType) {

		// drop in folder
		if (target instanceof ICContainer || 
				target instanceof ICProject || 
				target instanceof IContainer ||
				(operation == DND.DROP_COPY && (
						target instanceof IFile ||
						target instanceof ITranslationUnit))) {
			IContainer destination= getDestination(target);
			if (LocalSelectionTransfer.getTransfer().isSupportedType(transferType)) {
				IResource[] selectedResources= getSelectedResources();
				if (selectedResources.length > 0) {
					for (IResource res : selectedResources) {
						if(res instanceof IProject) {
							// drop of projects not supported on other IResources
							// "Path for project must have only one segment."
							return Status.CANCEL_STATUS;
						}
					}
					if (operation == DND.DROP_COPY || operation == DND.DROP_LINK) {
						CopyFilesAndFoldersOperation op = new CopyFilesAndFoldersOperation(getShell());
						if (op.validateDestination(destination, selectedResources) == null) {
							return Status.OK_STATUS;
						}
					} else {
						MoveFilesAndFoldersOperation op = new MoveFilesAndFoldersOperation(getShell());
						if (op.validateDestination(destination, selectedResources) == null) {
							return Status.OK_STATUS;
						}
					}
				}
			} else if (FileTransfer.getInstance().isSupportedType(transferType)) {
				String[] sourceNames = (String[]) FileTransfer.getInstance().nativeToJava(transferType);
				if (sourceNames == null) {
					// source names will be null on Linux. Use empty names to do
					// destination validation.
					// Fixes bug 29778
					sourceNames = new String[0];
				}
				CopyFilesAndFoldersOperation copyOperation = new CopyFilesAndFoldersOperation(
						getShell());
				if (null != copyOperation.validateImportDestination(destination,
						sourceNames)) {
					return Status.CANCEL_STATUS;
				}
				return Status.OK_STATUS;
			}
		}

		if (LocalSelectionTransfer.getTransfer().isSupportedType(transferType)) {
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
				ExceptionHandler.handle(e, CViewMessages.SelectionTransferDropAdapter_error_title, CViewMessages.SelectionTransferDropAdapter_error_message); 
			}
		}
		return Status.CANCEL_STATUS;
	}

	private IStatus handleValidateCopy(Object target) throws CModelException{
		if (target != null) {

			ISelection selection = LocalSelectionTransfer.getTransfer().getSelection();
			ICElement[] cElements= getCElements(selection);
			
			if (cElements == null || cElements.length == 0) {
				return Status.CANCEL_STATUS;	
			}
			if (!canCopyElements(cElements))
				return Status.CANCEL_STATUS;	
	
			if (target instanceof ISourceReference) {
				for (ICElement cElement : cElements) {
					if (cElement.getElementType() <= ICElement.C_UNIT) {
						return Status.CANCEL_STATUS;
					}
				}
				return Status.OK_STATUS;
			}
		
		}
		return Status.CANCEL_STATUS;
	}

	private IStatus handleValidateMove(Object target) throws CModelException {
		if (target != null) {

			ISelection selection = LocalSelectionTransfer.getTransfer().getSelection();
			ICElement[] cElements= getCElements(selection);

			if (cElements == null || cElements.length == 0) {
				return Status.CANCEL_STATUS;	
			}
			if (Arrays.asList(cElements).contains(target)) {
				return Status.CANCEL_STATUS;	
			}
			if (!canMoveElements(cElements)) {
				return Status.CANCEL_STATUS;	
			}
			if (target instanceof ISourceReference) {
				for (ICElement cElement : cElements) {
					if (cElement.getElementType() <= ICElement.C_UNIT) {
						return Status.CANCEL_STATUS;
					}
				}
				return Status.OK_STATUS;
			}
		
		}
		return Status.CANCEL_STATUS;
	}

	/**
	 * Performs a resource copy.
	 * Cloned from ResourceDropAdapterAssistant to support linked resources (bug 319405).
	 */
	private IStatus performResourceCopy(CommonDropAdapter dropAdapter,
			Shell shell, IResource[] sources) {
		IContainer target = getDestination(dropAdapter.getCurrentTarget());
		if (target == null) {
			return Status.CANCEL_STATUS;
		}
		
		boolean shouldLinkAutomatically = false;
		if (target.isVirtual()) {
			shouldLinkAutomatically = true;
			for (int i = 0; i < sources.length; i++) {
				if ((sources[i].getType() != IResource.FILE) && (sources[i].getLocation() != null)) {
					// If the source is a folder, but the location is null (a
					// broken link, for example),
					// we still generate a link automatically (the best option).
					shouldLinkAutomatically = false;
					break;
				}
			}
		}

		CopyFilesAndFoldersOperation operation = new CopyFilesAndFoldersOperation(shell);
		// if the target is a virtual folder and all sources are files, then
		// automatically create links
		if (shouldLinkAutomatically) {
			operation.setCreateLinks(true);
			operation.copyResources(sources, target);
		} else {
			boolean allSourceAreLinksOrVirtualFolders = true;
			for (int i = 0; i < sources.length; i++) {
				if (!sources[i].isVirtual() && !sources[i].isLinked()) {
					allSourceAreLinksOrVirtualFolders = false;
					break;
				}
			}
			// if all sources are either links or groups, copy then normally,
			// don't show the dialog
			if (!allSourceAreLinksOrVirtualFolders) {
				ImportTypeDialog dialog = new ImportTypeDialog(getShell(), dropAdapter.getCurrentOperation(), sources, target);
				dialog.setResource(target);
				if (dialog.open() == Window.OK) {
					if (dialog.getSelection() == ImportTypeDialog.IMPORT_VIRTUAL_FOLDERS_AND_LINKS)
						operation.setVirtualFolders(true);
					if (dialog.getSelection() == ImportTypeDialog.IMPORT_LINK)
						operation.setCreateLinks(true);
					if (dialog.getVariable() != null)
						operation.setRelativeVariable(dialog.getVariable());
					operation.copyResources(sources, target);
				} else
					return Status.CANCEL_STATUS;
			} else
				operation.copyResources(sources, target);
		}

		return Status.OK_STATUS;
	}

	private IStatus handleDropCopy(final Object target, DropTargetEvent event) throws CModelException, InvocationTargetException, InterruptedException{
		ISelection selection = LocalSelectionTransfer.getTransfer().getSelection();
		final ICElement[] cElements= getCElements(selection);

		if (target instanceof ICElement && cElements.length > 0) {
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
		List<?> elements = ((IStructuredSelection)selection).toList();
		List<Object> resources= new ArrayList<Object>(elements.size());
		for (Object element : elements) {
			if (element instanceof ITranslationUnit) {
				continue;
			}
			if (element instanceof ICElement)
				resources.add(element);
		}
		return resources.toArray(new ICElement[resources.size()]);
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
				if (parent == null) {
					if (p!= null) 
						return false;
				} 
				else if (!parent.equals(p))
					return false;
			}
		}
		return true;
	}

	private IContainer getDestination(Object dropTarget) {
		if (dropTarget instanceof IContainer) {
			return (IContainer)dropTarget;
		} else if (dropTarget instanceof ICElement) {
			return getDestination(((ICElement)dropTarget).getResource());
		} else if (dropTarget instanceof IFile) {
			return ((IFile)dropTarget).getParent();
		}
		return null;
	}

	/**
	 * Returns the resource selection from the LocalSelectionTransfer.
	 * 
	 * @return the resource selection from the LocalSelectionTransfer
	 */
	private IResource[] getSelectedResources() {

		ISelection selection = LocalSelectionTransfer.getTransfer()
				.getSelection();
		if (selection instanceof IStructuredSelection) {
			return getSelectedResources((IStructuredSelection)selection);
		} 
		return NO_RESOURCES;
	}

	/**
	 * Returns the resource selection from the LocalSelectionTransfer.
	 * 
	 * @return the resource selection from the LocalSelectionTransfer
	 */
	private IResource[] getSelectedResources(IStructuredSelection selection) {
		ArrayList<Object> selectedResources = new ArrayList<Object>();

		for (Iterator<?> i = selection.iterator(); i.hasNext();) {
			Object o = i.next();
			if (o instanceof IResource) {
				selectedResources.add(o);
			} else if (o instanceof IAdaptable) {
				IAdaptable a = (IAdaptable) o;
				IResource r = (IResource) a.getAdapter(IResource.class);
				if (r != null) {
					selectedResources.add(r);
				}
			}
		}
		return selectedResources
				.toArray(new IResource[selectedResources.size()]);
	}

}
