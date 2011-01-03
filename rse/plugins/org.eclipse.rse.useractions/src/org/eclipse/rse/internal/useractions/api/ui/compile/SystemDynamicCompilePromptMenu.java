/*********************************************************************************
 * Copyright (c) 2008 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Xuan Chen (IBM) - [222470] initial contribution.
 * Kevin Doyle (IBM) - [239700] Compile Commands are available on items it shouldn't -- Modified SystemDynamicCompileMenu
 * Kevin Doyle (IBM) - [253037] ClassCastException in SystemDynamicUserActionMenu, SystemDynamicCompileMenu
 *********************************************************************************/
package org.eclipse.rse.internal.useractions.api.ui.compile;

import java.util.ArrayList;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.internal.useractions.api.files.compile.ISystemCompileManagerAdapter;
import org.eclipse.rse.internal.useractions.files.compile.UniversalCompileManager;
import org.eclipse.rse.internal.useractions.ui.compile.SystemCompileCommand;
import org.eclipse.rse.internal.useractions.ui.compile.SystemCompileManager;
import org.eclipse.rse.internal.useractions.ui.compile.SystemCompileProfile;
import org.eclipse.rse.internal.useractions.ui.compile.SystemCompileType;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.rse.ui.view.ISystemRemoteElementAdapter;
import org.eclipse.rse.ui.view.SystemAdapterHelpers;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.CompoundContributionItem;

/**
 * Dynamic Compile Menu.
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part
 * of a work in progress. There is no guarantee that this API will work or that
 * it will remain the same. Please do not use this API without consulting with
 * the <a href="http://www.eclipse.org/tm/">Target Management</a> team.
 * </p>
 */
public class SystemDynamicCompilePromptMenu extends CompoundContributionItem
{

	protected IContributionItem[] getContributionItems() {

		ArrayList returnedItemList = new ArrayList();

		ISystemProfile[] activeProfiles = RSECorePlugin.getTheSystemRegistry().getActiveSystemProfiles();
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		ISelection selection = window.getSelectionService().getSelection();
		Object firstSelection = null;
		if (selection instanceof IStructuredSelection)
			firstSelection = ((IStructuredSelection) selection).getFirstElement();
		if (firstSelection == null)
		{
			return new IContributionItem[0];
		}
		Shell shell = SystemBasePlugin.getActiveWorkbenchShell();

		ISystemRemoteElementAdapter rmtAdapter = SystemAdapterHelpers.getRemoteAdapter(firstSelection);
		ISubSystem subsystem = rmtAdapter.getSubSystem(firstSelection);
		ISubSystemConfiguration ssc = subsystem.getSubSystemConfiguration();

		SystemCompileManager compileManager = null;

		 if (firstSelection instanceof IAdaptable) {
			 ISystemCompileManagerAdapter	adapter = (ISystemCompileManagerAdapter)((IAdaptable)firstSelection).getAdapter(ISystemCompileManagerAdapter.class);
			 if (null != adapter)
			 {
				 compileManager = adapter.getSystemCompileManager(ssc);
			 }
		 }

		 if (null == compileManager)
		 {
			 compileManager = new UniversalCompileManager();
			 compileManager.setSubSystemFactory(ssc);
		 }

		for (int idx = 0; idx < activeProfiles.length; idx++)
		{
			String srcType = null;
			if (rmtAdapter != null) {
				srcType = rmtAdapter.getRemoteSourceType(firstSelection);
				if (srcType == null)
					srcType = "null"; //$NON-NLS-1$
				else if (srcType.equals("")) //$NON-NLS-1$
					srcType = "blank"; //$NON-NLS-1$
			}

			if (null != compileManager)
			{
				 SystemCompileManager thisCompileManager = compileManager;
				 SystemCompileProfile compileProfile = thisCompileManager.getCompileProfile(activeProfiles[idx]);
				 // compileProfile.addContributions(firstSelection);
				 SystemCompileType compileType = compileProfile.getCompileType(srcType);

				 if (compileType != null)
				 {
					 SystemCompileCommand[] cmds = compileType.getCompileCommandsArray();
					 for (int idx2=0; idx2<cmds.length; idx2++)
					 {
						 SystemCompileAction action = new SystemCompileAction(shell, cmds[idx2], true);
						 action.setSelection(selection);
						 ActionContributionItem testContribution = new ActionContributionItem(action);
						 returnedItemList.add(testContribution);
						 //ourSubMenu.add(action);
					 }
				 }
			 }

		}

		// add a separator before Work With Compile Commands... menu item
		//ourSubMenu.add(new Separator());
		// add Work With Commands... action
		if (returnedItemList.size() > 0)
			returnedItemList.add(new Separator());

		   // Here's where you would dynamically generate your list
		SystemWorkWithCompileCommandsAction workWithCompileCommandAction = new SystemWorkWithCompileCommandsAction(shell, true, subsystem, compileManager);
		workWithCompileCommandAction.setSelection(selection);
		ActionContributionItem testContribution = new ActionContributionItem(workWithCompileCommandAction);
		returnedItemList.add(testContribution);
        IContributionItem[] list = (IContributionItem[])returnedItemList.toArray(new IContributionItem[]{});
		//String[] array = (String[])arrayList.toArray(new String[]{});
        //SystemCascadingCompileAction promptAction = new SystemCascadingCompileAction(null, true);
        //list[0] = new TestContribution(/*promptAction*/);

        return list;
	}

}
