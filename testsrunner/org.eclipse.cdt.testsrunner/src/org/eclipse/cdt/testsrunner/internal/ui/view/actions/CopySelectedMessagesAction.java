/*******************************************************************************
 * Copyright (c) 2011 Anton Gorenkov 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Gorenkov - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.testsrunner.internal.ui.view.actions;


import java.util.Iterator;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.ui.IWorkbenchCommandConstants;

/**
 * Copies the selected test messages to the clipboard.
 */
public class CopySelectedMessagesAction extends Action {

	private Clipboard clipboard;
	private TableViewer tableViewer;


	public CopySelectedMessagesAction(TableViewer tableViewer, Clipboard clipboard) {
		super(ActionsMessages.CopySelectedMessagesAction_text);
		setToolTipText(ActionsMessages.CopySelectedMessagesAction_tooltip);
		setActionDefinitionId(IWorkbenchCommandConstants.EDIT_COPY);
		this.tableViewer = tableViewer;
		this.clipboard = clipboard;
	}

	@Override
	public void run() {
		ITableLabelProvider labelProvider = (ITableLabelProvider)tableViewer.getLabelProvider();
		IStructuredSelection selection = (IStructuredSelection)tableViewer.getSelection();
		if (!selection.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			boolean needEOL = false;
			for (Iterator<?> it = selection.iterator(); it.hasNext();) {
				Object item = it.next();
				if (needEOL) {
					sb.append(System.getProperty("line.separator")); //$NON-NLS-1$
				} else {
					needEOL = true;
				}
				sb.append(labelProvider.getColumnText(item, 0));
			}
			clipboard.setContents(
					new String[]{ sb.toString() },
					new Transfer[]{ TextTransfer.getInstance() });
		}
	}
	
}

