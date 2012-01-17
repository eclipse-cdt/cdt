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

import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IMemoryBlockExtension;
import org.eclipse.debug.ui.memory.IMemoryRendering;
import org.eclipse.debug.ui.memory.IMemoryRenderingSite;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

public class FindAction implements IViewActionDelegate {

	private static String FIND_NEXT_ID	= "org.eclipse.cdt.debug.ui.memory.search.FindNextAction"; //$NON-NLS-1$
	
	private IMemoryRenderingSite fView;
	
	private static Properties fSearchDialogProperties = new Properties();

	public static Properties getProperties() { return fSearchDialogProperties; }
	
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
			
			Shell shell = CDebugUIPlugin.getActiveWorkbenchShell();
			FindReplaceDialog dialog = new FindReplaceDialog(shell, (IMemoryBlockExtension) memBlock, 
				fView, fSearchDialogProperties, fAction);
			if(action.getId().equals(FIND_NEXT_ID))
			{
				if(Boolean.valueOf(fSearchDialogProperties.getProperty(FindReplaceDialog.SEARCH_ENABLE_FIND_NEXT, Boolean.FALSE.toString())))
				{
					dialog.performFindNext();
				}
				return;
			}
			else
			{
				dialog.open();

				// TODO: finish feature?
				//Object results[] = dialog.getResult();
			}
		}

	}

	private static IAction fAction = null;
	
	public void selectionChanged(IAction action, ISelection selection) {
		
		if(action.getId().equals(FIND_NEXT_ID))
		{
			fAction = action;
			action.setEnabled(Boolean.valueOf(fSearchDialogProperties.getProperty(FindReplaceDialog.SEARCH_ENABLE_FIND_NEXT, Boolean.FALSE.toString())));
		}
	}

}
