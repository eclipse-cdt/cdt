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

import java.util.Iterator;

import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.ui.navigator.CommonDragAdapterAssistant;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.ITranslationUnit;

/**
 * A Common Navigator drag assistant for <code>ICElement</code>s being also
 * <code>ISourceReference</code>s.
 * 
 * @see org.eclipse.cdt.internal.ui.cview.SelectionTransferDragAdapter
 */
public class CNavigatorDragAdapterAssistant extends CommonDragAdapterAssistant {

	/*
	 * @see org.eclipse.ui.navigator.CommonDragAdapterAssistant#getSupportedTransferTypes()
	 */
	public Transfer[] getSupportedTransferTypes() {
		Transfer[] transfers= new Transfer[] {
				LocalSelectionTransfer.getTransfer()
		};
		return transfers;
	}

	/*
	 * @see org.eclipse.ui.navigator.CommonDragAdapterAssistant#setDragData(org.eclipse.swt.dnd.DragSourceEvent, org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public boolean setDragData(DragSourceEvent event,
			IStructuredSelection selection) {
		if (selection != null) {
			boolean applicable= false;
			for (Iterator iter= (selection).iterator(); iter.hasNext();) {
				Object element= iter.next();
				if (element instanceof ICElement) {
					if (element instanceof ITranslationUnit) {
						continue;
					}
					if (!(element instanceof ISourceReference)) {
						return false;
					}
					applicable= true;
				}
			}
			if (applicable) {
				event.data = selection;
				return true;
			}
		}
		return false;
	}

}
