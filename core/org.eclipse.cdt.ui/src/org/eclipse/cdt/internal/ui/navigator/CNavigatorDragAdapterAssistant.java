/*******************************************************************************
 * Copyright (c) 2006, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.navigator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.ui.navigator.CommonDragAdapterAssistant;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.ITranslationUnit;

/**
 * A Common Navigator drag assistant supporting <code>LocalSelectionTransfer</code> of 
 * <code>ICElement</code>s being also <code>ISourceReference</code>s and 
 * <code>FileTransfer</code> for external translation units.
 * 
 * @see org.eclipse.cdt.internal.ui.cview.SelectionTransferDragAdapter
 */
public class CNavigatorDragAdapterAssistant extends CommonDragAdapterAssistant {

	private static final Transfer[] TRANSFERS = new Transfer[] {
		LocalSelectionTransfer.getTransfer(),
		FileTransfer.getInstance()
	};

	/*
	 * @see org.eclipse.ui.navigator.CommonDragAdapterAssistant#getSupportedTransferTypes()
	 */
	@Override
	public Transfer[] getSupportedTransferTypes() {
		return TRANSFERS;
	}

	/*
	 * @see org.eclipse.ui.navigator.CommonDragAdapterAssistant#setDragData(org.eclipse.swt.dnd.DragSourceEvent, org.eclipse.jface.viewers.IStructuredSelection)
	 */
	@Override
	public boolean setDragData(DragSourceEvent event, IStructuredSelection selection) {
		if (selection != null) {
			if (LocalSelectionTransfer.getTransfer().isSupportedType(event.dataType)) {
				boolean applicable= false;
				for (Iterator<?> iter= (selection).iterator(); iter.hasNext();) {
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
			} else if (FileTransfer.getInstance().isSupportedType(event.dataType)) {
				List<String> files= new ArrayList<String>();
				for (Iterator<?> iter= (selection).iterator(); iter.hasNext();) {
					Object element= iter.next();
					if (element instanceof ITranslationUnit) {
						ITranslationUnit tu= (ITranslationUnit) element;
						IPath location= tu.getLocation();
						if (location != null) {
							files.add(location.toOSString());
						}
					}
				}
				if (!files.isEmpty()) {
					event.data = files.toArray(new String[files.size()]);
					return true;
				}
			}
		}
		return false;
	}
}
