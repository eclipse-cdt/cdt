/*******************************************************************************
 * Copyright (c) 2007, 2015 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.typehierarchy;

import java.util.Iterator;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;

public class THDropTargetListener implements DropTargetListener {
	private ICElement fInput;
	private boolean fEnabled = true;
	private IWorkbenchWindow fWindow;

	public THDropTargetListener(THViewPart view) {
		fWindow = view.getSite().getWorkbenchWindow();
	}

	public void setEnabled(boolean val) {
		fEnabled = val;
	}

	@Override
	public void dragEnter(DropTargetEvent event) {
		fInput = null;
		checkOperation(event);
		if (event.detail != DND.DROP_NONE) {
			if (LocalSelectionTransfer.getTransfer().isSupportedType(event.currentDataType)) {
				fInput = checkLocalSelection();
				if (!TypeHierarchyUI.isValidInput(fInput)) {
					event.detail = DND.DROP_NONE;
					fInput = null;
				}
			}
		}
	}

	private ICElement checkLocalSelection() {
		ISelection sel = LocalSelectionTransfer.getTransfer().getSelection();
		if (sel instanceof IStructuredSelection) {
			for (Iterator<?> iter = ((IStructuredSelection) sel).iterator(); iter.hasNext();) {
				Object element = iter.next();
				if (element instanceof ICElement) {
					return (ICElement) element;
				}
				if (element instanceof IAdaptable) {
					ICElement adapter = ((IAdaptable) element).getAdapter(ICElement.class);
					if (adapter != null) {
						return adapter;
					}
				}
			}
		}
		return null;
	}

	@Override
	public void dragLeave(DropTargetEvent event) {
	}

	@Override
	public void dragOperationChanged(DropTargetEvent event) {
		checkOperation(event);
	}

	@Override
	public void dragOver(DropTargetEvent event) {
	}

	@Override
	public void drop(DropTargetEvent event) {
		if (fInput == null) {
			Display.getCurrent().beep();
		} else {
			TypeHierarchyUI.open(fInput, fWindow);
		}
	}

	@Override
	public void dropAccept(DropTargetEvent event) {
	}

	private void checkOperation(DropTargetEvent event) {
		if (fEnabled && (event.operations & DND.DROP_COPY) != 0) {
			event.detail = DND.DROP_COPY;
		} else {
			event.detail = DND.DROP_NONE;
		}
	}
}
