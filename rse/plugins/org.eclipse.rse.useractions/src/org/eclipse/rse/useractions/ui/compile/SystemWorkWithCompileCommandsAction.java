/*******************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * Martin Oberhuber (Wind River) - [168870] refactor org.eclipse.rse.core package of the UI plugin
 * David Dykstal (IBM) - [186589] move user types, user actions, and compile commands
 *                                API to the user actions plugin
 *******************************************************************************/
package org.eclipse.rse.useractions.ui.compile;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.internal.ui.view.team.SystemTeamViewSubSystemConfigurationNode;
import org.eclipse.rse.internal.useractions.IUserActionsImageIds;
import org.eclipse.rse.internal.useractions.ui.compile.SystemCompileManager;
import org.eclipse.rse.internal.useractions.ui.compile.SystemCompileProfile;
import org.eclipse.rse.internal.useractions.ui.compile.SystemWorkWithCompileCommandsDialog;
import org.eclipse.rse.internal.useractions.ui.compile.teamview.SystemTeamViewCompileTypeNode;
import org.eclipse.rse.internal.useractions.ui.uda.SystemUDAResources;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.actions.SystemBaseDialogAction;
import org.eclipse.rse.ui.view.ISystemRemoteElementAdapter;
import org.eclipse.rse.ui.view.SystemAdapterHelpers;
import org.eclipse.rse.useractions.files.compile.ISystemCompileManagerAdapter;
import org.eclipse.swt.widgets.Shell;

/**
 * The action that displays the Work With -> Compile Commands menu item
 */
public class SystemWorkWithCompileCommandsAction extends SystemBaseDialogAction {
	/**
	 * Constructor
	 * @param shell The Shell of the parent UI for this dialog
	 * @param fromCascadingCompileAction true to get "Work with Compile Commands" label, false to get "Compile Commands" label.
	 */
	public SystemWorkWithCompileCommandsAction(Shell shell, boolean fromCascadingCompileAction) {
		super(fromCascadingCompileAction ? SystemUDAResources.ACTION_WORKWITH_WWCOMPILE_CMDS_LABEL : SystemUDAResources.ACTION_WORKWITH_COMPILE_CMDS_LABEL,
				fromCascadingCompileAction ? SystemUDAResources.ACTION_WORKWITH_WWCOMPILE_CMDS_TOOLTIP : SystemUDAResources.ACTION_WORKWITH_COMPILE_CMDS_TOOLTIP, RSEUIPlugin.getDefault()
						.getImageDescriptor(IUserActionsImageIds.WORK_WITH_COMPILE_COMMANDS_1), shell);
		allowOnMultipleSelection(false);
		if (!fromCascadingCompileAction)
			setContextMenuGroup(ISystemContextMenuConstants.GROUP_WORKWITH);
		else
			setContextMenuGroup(ISystemContextMenuConstants.GROUP_REORGANIZE);
		setHelp(RSEUIPlugin.HELPPREFIX + "actnwwcc"); //$NON-NLS-1$
		setAvailableOffline(true);
	}

	/**
	 * Reset between runs
	 */
	public void reset() {
		// nothing to do as we have no instance vars.
	}

	/**
	 * Called by SystemBaseAction when selection is set.
	 * Our opportunity to verify we are allowed for this selected type.
	 */
	public boolean updateSelection(IStructuredSelection selection) {
		boolean enable = true;
		Object inputObject = selection.getFirstElement();
		if (inputObject instanceof SystemTeamViewCompileTypeNode) {
			SystemTeamViewCompileTypeNode typeNode = ((SystemTeamViewCompileTypeNode) inputObject);
			ISystemProfile currSystemProfile = typeNode.getProfile();
			enable = currSystemProfile.isActive();
		} else if (inputObject instanceof SystemTeamViewSubSystemConfigurationNode) {
			ISystemProfile currSystemProfile = ((SystemTeamViewSubSystemConfigurationNode) inputObject).getProfile();
			enable = currSystemProfile.isActive();
		}
		return enable;
	}

	/**
	 * If you decide to use the supplied run method as is,
	 *  then you must override this method to create and return
	 *  the dialog that is displayed by the default run method
	 *  implementation.
	 * <p>
	 * If you override run with your own, then
	 *  simply implement this to return null as it won't be used.
	 * @see #run()
	 */
	protected Dialog createDialog(Shell shell) {
		Object inputObject = getFirstSelection();
		boolean caseSensitive = false;
		ISubSystem subsystem = null;
		ISubSystemConfiguration ssf = null;
		ISystemProfile currSystemProfile = null;
		if (inputObject instanceof ISubSystem)
			subsystem = (ISubSystem) inputObject;
		else if (inputObject instanceof SystemTeamViewCompileTypeNode) {
			SystemTeamViewCompileTypeNode typeNode = ((SystemTeamViewCompileTypeNode) inputObject);
			ssf = typeNode.getParentSubSystemFactory().getSubSystemConfiguration();
			currSystemProfile = typeNode.getProfile();
		} else if (inputObject instanceof SystemTeamViewSubSystemConfigurationNode) {
			ssf = ((SystemTeamViewSubSystemConfigurationNode) inputObject).getSubSystemConfiguration();
			currSystemProfile = ((SystemTeamViewSubSystemConfigurationNode) inputObject).getProfile();
		} else {
			ISystemRemoteElementAdapter rmtAdapter = SystemAdapterHelpers.getRemoteAdapter(inputObject);
			if (rmtAdapter != null) subsystem = rmtAdapter.getSubSystem(inputObject);
		}
		SystemCompileProfile currProfile = null;
		SystemCompileProfile[] currProfiles = null;
		SystemCompileManager compileManager = null;
		if (subsystem != null) {
			if (ssf == null) ssf = subsystem.getSubSystemConfiguration();
			if (currSystemProfile == null) currSystemProfile = subsystem.getSystemProfile();
		}
		if (ssf != null) {
			
			 ISubSystemConfiguration ssc = subsystem.getSubSystemConfiguration();
			 
			 
			 
			 if (inputObject instanceof IAdaptable) {
				 ISystemCompileManagerAdapter	adapter = (ISystemCompileManagerAdapter)((IAdaptable)inputObject).getAdapter(ISystemCompileManagerAdapter.class);
				 if (null != adapter)
				 {
					 compileManager = adapter.getSystemCompileManager(ssc);
				 }
			 }
			 
			 if (null != compileManager)
			 {
				 if (currSystemProfile != null)
				 {
					 currProfile = compileManager.getCompileProfile(currSystemProfile);
					 currProfiles = compileManager.getAllCompileProfiles();
				 }
			
				caseSensitive = ssf.isCaseSensitive();
		
				SystemWorkWithCompileCommandsDialog dlg = new SystemWorkWithCompileCommandsDialog(shell, compileManager, currProfile);
				/* FIXME - currProfiles cannot be null since above stuff was commented out
				 if (currProfiles != null) {
				 dlg.setProfiles(currProfiles);
				 }
				 */
				dlg.setProfiles(currProfiles);
				dlg.setCaseSensitive(caseSensitive);
				if (inputObject instanceof SystemTeamViewCompileTypeNode) {
					SystemTeamViewCompileTypeNode node = (SystemTeamViewCompileTypeNode) inputObject;
					dlg.setCompileType(node.getCompileType());
					dlg.setSupportsAddSrcTypeButton(false);
				}
				return dlg;
			 }
		}
		return null;
	}

	/**
	 * Required by parent. We use it to return the new name.
	 * In our case, we don't need it, so we return null.
	 */
	protected Object getDialogValue(Dialog dlg) {
		return null;
	}
}
