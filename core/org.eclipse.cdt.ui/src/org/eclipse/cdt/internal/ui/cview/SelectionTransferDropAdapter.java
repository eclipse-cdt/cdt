/*******************************************************************************
 * Copyright (c) 2002, 2012 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * Anton Leherbauer (Wind River Systems) - Fixed bug 141484
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.cview;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.ui.dnd.CDTViewerDropAdapter;
import org.eclipse.cdt.internal.ui.dnd.TransferDropTargetListener;
import org.eclipse.cdt.internal.ui.util.ExceptionHandler;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Shell;

public class SelectionTransferDropAdapter extends CDTViewerDropAdapter implements TransferDropTargetListener {

	private List<?> fElements;
	private ICElement[] fMoveData;
	private ICElement[] fCopyData;

	private static final long DROP_TIME_DIFF_TRESHOLD = 150;

	public SelectionTransferDropAdapter(StructuredViewer viewer) {
		super(viewer, DND.FEEDBACK_SCROLL | DND.FEEDBACK_EXPAND);
	}

	//---- TransferDropTargetListener interface ---------------------------------------

	@Override
	public Transfer getTransfer() {
		return LocalSelectionTransfer.getTransfer();
	}

	@Override
	public boolean isEnabled(DropTargetEvent event) {
		Object target = event.item != null ? event.item.getData() : null;
		if (target == null) {
			return false;
		}
		return target instanceof ISourceReference;
	}

	//---- Actual DND -----------------------------------------------------------------

	@Override
	public void dragEnter(DropTargetEvent event) {
		clear();
		super.dragEnter(event);
	}

	@Override
	public void dragLeave(DropTargetEvent event) {
		clear();
		super.dragLeave(event);
	}

	private void clear() {
		fElements = null;
		fMoveData = null;
		fCopyData = null;
	}

	@Override
	public void validateDrop(Object target, DropTargetEvent event, int operation) {
		event.detail = DND.DROP_NONE;

		if (tooFast(event)) {
			return;
		}

		initializeSelection();

		try {
			switch (operation) {
			case DND.DROP_DEFAULT:
				event.detail = handleValidateDefault(target, event);
				break;
			case DND.DROP_COPY:
				event.detail = handleValidateCopy(target, event);
				break;
			case DND.DROP_MOVE:
				event.detail = handleValidateMove(target, event);
				break;
			case DND.DROP_LINK:
				event.detail = handleValidateLink(target, event);
				break;
			}
		} catch (CModelException e) {
			ExceptionHandler.handle(e, CViewMessages.SelectionTransferDropAdapter_error_title,
					CViewMessages.SelectionTransferDropAdapter_error_message);
			event.detail = DND.DROP_NONE;
		}
	}

	protected void initializeSelection() {
		if (fElements != null) {
			return;
		}
		ISelection s = LocalSelectionTransfer.getTransfer().getSelection();
		if (!(s instanceof IStructuredSelection)) {
			return;
		}
		fElements = ((IStructuredSelection) s).toList();
	}

	private boolean tooFast(DropTargetEvent event) {
		return Math.abs(LocalSelectionTransfer.getTransfer().getSelectionSetTime()
				- (event.time & 0xFFFFFFFFL)) < DROP_TIME_DIFF_TRESHOLD;
	}

	@Override
	public void drop(Object target, DropTargetEvent event) {
		try {
			switch (event.detail) {
			case DND.DROP_MOVE:
				handleDropMove(target, event);
				break;
			case DND.DROP_COPY:
				handleDropCopy(target, event);
				break;
			case DND.DROP_LINK:
				handleDropLink(target, event);
				break;
			}
		} catch (CModelException e) {
			ExceptionHandler.handle(e, CViewMessages.SelectionTransferDropAdapter_error_title,
					CViewMessages.SelectionTransferDropAdapter_error_message);
		} catch (InvocationTargetException e) {
			ExceptionHandler.handle(e, CViewMessages.SelectionTransferDropAdapter_error_title,
					CViewMessages.SelectionTransferDropAdapter_error_exception);
		} catch (InterruptedException e) {
			//ok
		} finally {
			// The drag source listener must not perform any operation
			// since this drop adapter did the remove of the source even
			// if we moved something.
			event.detail = DND.DROP_NONE;
		}
	}

	private int handleValidateDefault(Object target, DropTargetEvent event) throws CModelException {
		if (target == null) {
			return DND.DROP_NONE;
		}
		return handleValidateMove(target, event);
	}

	private int handleValidateMove(Object target, DropTargetEvent event) throws CModelException {
		if (target == null || fElements.contains(target)) {
			return DND.DROP_NONE;
		}
		if (fMoveData == null) {
			ICElement[] cElements = getCElements(fElements);
			if (cElements != null && cElements.length > 0) {
				fMoveData = cElements;
			}
		}

		if (!canMoveElements()) {
			return DND.DROP_NONE;
		}

		if (target instanceof ISourceReference) {
			return DND.DROP_MOVE;
		}
		return DND.DROP_NONE;
	}

	private boolean canMoveElements() {
		if (fMoveData != null) {
			return hasCommonParent(fMoveData);
		}
		return false;
	}

	private boolean hasCommonParent(ICElement[] elements) {
		if (elements.length > 1) {
			ICElement parent = elements[0];
			for (int i = 0; i < elements.length; ++i) {
				ICElement p = elements[i].getParent();
				if (parent == null) {
					if (p != null) {
						return false;
					}
				} else if (!parent.equals(p)) {
					return false;
				}
			}
		}
		return true;
	}

	private void handleDropLink(Object target, DropTargetEvent event) {
	}

	private int handleValidateLink(Object target, DropTargetEvent event) {
		return DND.DROP_NONE;
	}

	private void handleDropMove(final Object target, DropTargetEvent event)
			throws CModelException, InvocationTargetException, InterruptedException {
		final ICElement[] cElements = getCElements(fElements);

		if (target instanceof ICElement) {
			ICElement cTarget = (ICElement) target;
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
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						CoreModel.getDefault().getCModel().move(cElements, containers, siblings, null, false, monitor);
					} catch (CModelException e) {
						throw new InvocationTargetException(e);
					}
				}
			};
			run(runnable);
		}
	}

	private int handleValidateCopy(Object target, DropTargetEvent event) throws CModelException {
		if (target == null) {
			return DND.DROP_NONE;
		}

		if (fCopyData == null) {
			ICElement[] cElements = getCElements(fElements);
			if (cElements != null && cElements.length > 0) {
				fCopyData = cElements;
			}
		}

		if (!canCopyElements())
			return DND.DROP_NONE;

		if (target instanceof ISourceReference) {
			return DND.DROP_COPY;
		}
		return DND.DROP_NONE;
	}

	private boolean canCopyElements() {
		if (fCopyData != null) {
			return hasCommonParent(fCopyData);
		}
		return false;
	}

	private void handleDropCopy(final Object target, DropTargetEvent event)
			throws CModelException, InvocationTargetException, InterruptedException {
		final ICElement[] cElements = getCElements(fElements);

		if (target instanceof ICElement) {
			ICElement cTarget = (ICElement) target;
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
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						CoreModel.getDefault().getCModel().copy(cElements, containers, siblings, null, false, monitor);
					} catch (CModelException e) {
						throw new InvocationTargetException(e);
					}
				}
			};
			run(runnable);
		}
	}

	private Shell getShell() {
		return getViewer().getControl().getShell();
	}

	public void run(IRunnableWithProgress runnable) throws InterruptedException, InvocationTargetException {
		IRunnableContext context = new ProgressMonitorDialog(getShell());
		context.run(true, true, runnable);
	}

	public static ICElement[] getCElements(List<?> elements) {
		List<ICElement> resources = new ArrayList<>(elements.size());
		for (Iterator<?> iter = elements.iterator(); iter.hasNext();) {
			Object element = iter.next();
			if (element instanceof ITranslationUnit) {
				continue;
			}
			if (element instanceof ICElement)
				resources.add((ICElement) element);
		}
		return resources.toArray(new ICElement[resources.size()]);
	}

}
