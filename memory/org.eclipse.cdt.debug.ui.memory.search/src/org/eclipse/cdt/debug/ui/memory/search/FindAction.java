/*******************************************************************************
 * Copyright (c) 2007-2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ted R Williams (Wind River Systems, Inc.) - initial implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.ui.memory.search;

import java.util.Properties;

import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IMemoryBlockExtension;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.views.memory.MemoryView;
import org.eclipse.debug.ui.memory.IMemoryRendering;
import org.eclipse.debug.ui.memory.IMemoryRenderingSite;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

public class FindAction implements IViewActionDelegate {

	private IMemoryRenderingSite fView;
	
	private static Properties fSearchDialogProperties = new Properties();

	public void init(IViewPart view) {
		if (view instanceof IMemoryRenderingSite)
			fView = (IMemoryRenderingSite) view;
	}

	public void run(IAction action) {
		ISelection selection = fView.getSite().getSelectionProvider()
			.getSelection();
		
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection strucSel = (IStructuredSelection) selection;
		
			// return if current selection is empty
			if (strucSel.isEmpty())
				return;
		
			Object obj = strucSel.getFirstElement();
		
			if (obj == null)
				return;
		
			IMemoryBlock memBlock = null;
		
			if (obj instanceof IMemoryRendering) {
				memBlock = ((IMemoryRendering) obj).getMemoryBlock();
			} else if (obj instanceof IMemoryBlock) {
				memBlock = (IMemoryBlock) obj;
			}
			
			Shell shell = DebugUIPlugin.getShell();
			FindReplaceDialog dialog = new FindReplaceDialog(shell, (IMemoryBlockExtension) memBlock, 
				fView, (Properties) fSearchDialogProperties, fAction);
			if(action.getText().equalsIgnoreCase("Find Next"))
			{
				if(fSearchDialogProperties.getProperty(FindReplaceDialog.SEARCH_ENABLE_FIND_NEXT, "false").equals("true"))
				{
					dialog.performFindNext();
				}
				return;
			}
			else
			{
				dialog.open();
				
				Object results[] = dialog.getResult();
			}
		}

	}

	private static IAction fAction = null;
	
	public void selectionChanged(IAction action, ISelection selection) {
		if(action.getText().equalsIgnoreCase("Find Next"))
		{
			fAction = action;
			action.setEnabled(fSearchDialogProperties.getProperty(FindReplaceDialog.SEARCH_ENABLE_FIND_NEXT, "false")
				.equals("true"));
		}
	}

}
