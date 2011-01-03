/*******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
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
 * Xuan Chen        (IBM)    - [225617] [useraction][api] Remove Team view support inside user action.
 * Kevin Doyle (IBM)   - [222828] Icons for some Actions Missing
 * Kevin Doyle (IBM)   - [239908] Need to set the connection in compile manager for Work with Compile Commands Action
 *******************************************************************************/
package org.eclipse.rse.internal.useractions.api.ui.compile;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.internal.useractions.Activator;
import org.eclipse.rse.internal.useractions.IUserActionsImageIds;
import org.eclipse.rse.internal.useractions.api.files.compile.ISystemCompileManagerAdapter;
import org.eclipse.rse.internal.useractions.files.compile.UniversalCompileManager;
import org.eclipse.rse.internal.useractions.ui.compile.SystemCompileManager;
import org.eclipse.rse.internal.useractions.ui.compile.SystemCompileProfile;
import org.eclipse.rse.internal.useractions.ui.compile.SystemWorkWithCompileCommandsDialog;
import org.eclipse.rse.internal.useractions.ui.uda.SystemUDAResources;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.actions.SystemBaseDialogAction;
import org.eclipse.rse.ui.view.ISystemRemoteElementAdapter;
import org.eclipse.rse.ui.view.SystemAdapterHelpers;
import org.eclipse.swt.widgets.Shell;

/**
 * The action that displays the Work With -> Compile Commands menu item
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part
 * of a work in progress. There is no guarantee that this API will work or that
 * it will remain the same. Please do not use this API without consulting with
 * the <a href="http://www.eclipse.org/tm/">Target Management</a> team.
 * </p>
 */
public class SystemWorkWithCompileCommandsAction extends SystemBaseDialogAction {

	private ISubSystem subsystem = null;
	private ISubSystemConfiguration subsystemFactory = null;
	private ISystemProfile profile = null;
	private SystemCompileManager compileManager = null;

	/**
	 * Constructor
	 * @param shell The Shell of the parent UI for this dialog
	 * @param fromCascadingCompileAction true to get "Work with Compile Commands" label, false to get "Compile Commands" label.
	 */
	public SystemWorkWithCompileCommandsAction(Shell shell, boolean fromCascadingCompileAction) {
		super(fromCascadingCompileAction ? SystemUDAResources.ACTION_WORKWITH_WWCOMPILE_CMDS_LABEL : SystemUDAResources.ACTION_WORKWITH_COMPILE_CMDS_LABEL,
				fromCascadingCompileAction ? SystemUDAResources.ACTION_WORKWITH_WWCOMPILE_CMDS_TOOLTIP : SystemUDAResources.ACTION_WORKWITH_COMPILE_CMDS_TOOLTIP, Activator.getDefault()
						.getImageDescriptor(IUserActionsImageIds.WORK_WITH_COMPILE_COMMANDS_1), shell);
		allowOnMultipleSelection(false);
		if (!fromCascadingCompileAction)
			setContextMenuGroup(ISystemContextMenuConstants.GROUP_WORKWITH);
		else
			setContextMenuGroup(ISystemContextMenuConstants.GROUP_REORGANIZE);
		setHelp(RSEUIPlugin.HELPPREFIX + "actnwwcc"); //$NON-NLS-1$
		setAvailableOffline(true);
	}

	public SystemWorkWithCompileCommandsAction(Shell shell, boolean fromCascadingCompileAction, ISubSystem subSystem, SystemCompileManager compileManager)
	{
		this(shell, fromCascadingCompileAction);
		this.subsystem = subSystem;
		this.compileManager = compileManager;
		if (null != subSystem)
		{
			subsystemFactory = subsystem.getSubSystemConfiguration();
			profile = subSystem.getSystemProfile();
		}

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
		return true;
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
		if (null == subsystem)
		{
			if (inputObject instanceof ISubSystem)
				subsystem = (ISubSystem) inputObject;
			//Don't think we need to support invoking this diaglog from Team view.
			/*
			else if (inputObject instanceof SystemTeamViewCompileTypeNode) {
				SystemTeamViewCompileTypeNode typeNode = ((SystemTeamViewCompileTypeNode) inputObject);
				subsystemFactory = typeNode.getParentSubSystemFactory().getSubSystemConfiguration();
				profile = typeNode.getProfile();
			} else if (inputObject instanceof SystemTeamViewSubSystemConfigurationNode) {
				subsystemFactory = ((SystemTeamViewSubSystemConfigurationNode) inputObject).getSubSystemConfiguration();
				profile = ((SystemTeamViewSubSystemConfigurationNode) inputObject).getProfile();
			}
			*/
			else {
				ISystemRemoteElementAdapter rmtAdapter = SystemAdapterHelpers.getRemoteAdapter(inputObject);
				if (rmtAdapter != null) subsystem = rmtAdapter.getSubSystem(inputObject);
			}
		}
		SystemCompileProfile currProfile = null;
		SystemCompileProfile[] currProfiles = null;

		if (subsystem != null) {
			if (subsystemFactory == null) subsystemFactory = subsystem.getSubSystemConfiguration();
			if (profile == null) profile = subsystem.getSystemProfile();
		}

		if (null == compileManager)
		{
			 if (inputObject instanceof IAdaptable) {
				 ISystemCompileManagerAdapter	adapter = (ISystemCompileManagerAdapter)((IAdaptable)inputObject).getAdapter(ISystemCompileManagerAdapter.class);
				 if (null != adapter)
				 {
					 compileManager = adapter.getSystemCompileManager(subsystemFactory);
				 }
			 }
		}

		if (null == compileManager)
		{
			 compileManager = new UniversalCompileManager();
			 compileManager.setSubSystemFactory(subsystemFactory);
		}

		if (null != compileManager)
		{
				 if (profile != null)
				 {
					 currProfile = compileManager.getCompileProfile(profile);
					 currProfiles = compileManager.getAllCompileProfiles();
				 }

				 if (subsystem != null) {
					 compileManager.setSystemConnection(subsystem.getHost());
				 }
				 
				caseSensitive = subsystemFactory.isCaseSensitive();

				SystemWorkWithCompileCommandsDialog dlg = new SystemWorkWithCompileCommandsDialog(shell, compileManager, currProfile);
				/* FIXME - currProfiles cannot be null since above stuff was commented out
				 if (currProfiles != null) {
				 dlg.setProfiles(currProfiles);
				 }
				 */
				dlg.setProfiles(currProfiles);
				dlg.setCaseSensitive(caseSensitive);

				return dlg;
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
