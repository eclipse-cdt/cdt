package org.eclipse.rse.internal.useractions.ui.uda.actions;

/*******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * David Dykstal (IBM) - [186589] move user types, user actions, and compile commands
 *                                API to the user actions plugin
 * Xuan Chen     (IBM) - [225617] [useraction][api] Remove Team view support inside user action.
 * Kevin Doyle (IBM)   - [222828] Icons for some Actions Missing
 * Kevin Doyle	 (IBM) - [252296] Work With User Actions action is not available in offline mode
 *******************************************************************************/
//import java.util.Iterator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.internal.useractions.Activator;
import org.eclipse.rse.internal.useractions.IUserActionsImageIds;
import org.eclipse.rse.internal.useractions.ui.uda.SystemUDAResources;
import org.eclipse.rse.internal.useractions.ui.uda.SystemUDActionSubsystem;
import org.eclipse.rse.internal.useractions.ui.uda.SystemWorkWithUDAsDialog;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.actions.SystemBaseDialogAction;
import org.eclipse.swt.widgets.Shell;

/**
 * The action that displays the Work With User-Defined Actions GUI
 */
public class SystemWorkWithUDAsAction extends SystemBaseDialogAction {
	private ISubSystem subsystem = null;
	private ISubSystemConfiguration subsystemFactory = null;
	private ISystemProfile profile;
	private SystemUDActionSubsystem udaActionSubsystem = null;

	/**
	 * Constructor when starting with a subsystem (such as in RS view)
	 * @param parent The Shell of the parent UI for this dialog
	 * @param subSystem The subsystem we are launching this from/for
	 */
	public SystemWorkWithUDAsAction(Shell parent, ISubSystem subSystem, SystemUDActionSubsystem udaActionSubsystem) {
		this(parent);
		setSubSystem(subSystem);
		setActionSubsystem(udaActionSubsystem);
		setAvailableOffline(true);
	}

	/**
	 * Constructor when starting with a subsystem factory (such as in Team view)
	 * @param parent The Shell of the parent UI for this dialog
	 * @param subSystemFactory The subsystem factory we are launching this from/for
	 */
	public SystemWorkWithUDAsAction(Shell parent, ISubSystemConfiguration subSystemFactory, ISystemProfile profile, SystemUDActionSubsystem udaActionSubsystem) {
		this(parent);
		setSubSystemFactory(subsystemFactory, profile);
		setActionSubsystem(udaActionSubsystem);
	}

	/**
	 * Constructor when we don't have anything. 
	 * At run time, the input is deduced from the first selected object.
	 * @param parent The Shell of the parent UI for this dialog
	 */
	public SystemWorkWithUDAsAction(Shell parent) {
		super(SystemUDAResources.ACTION_WORKWITH_UDAS_LABEL, SystemUDAResources.ACTION_WORKWITH_UDAS_TOOLTIP, Activator.getDefault().getImageDescriptor(
				IUserActionsImageIds.WORK_WITH_USER_ACTIONS_1), parent);
		allowOnMultipleSelection(false);
		setContextMenuGroup(ISystemContextMenuConstants.GROUP_WORKWITH);
		setHelp(RSEUIPlugin.HELPPREFIX + "actn0045"); //$NON-NLS-1$
	}

	/**
	 * Constructor when we don't have anything, and we want to choose between "Work with->User Actions" and "Work With User Actions".
	 * At run time, the input is deduced from the first selected object.
	 * @param parent The Shell of the parent UI for this dialog
	 * @param fromCascadingCompileAction true to get "Work with Compile Commands" label, false to get "Compile Commands" label.  
	 */
	public SystemWorkWithUDAsAction(Shell parent, boolean fromCascadingCompileAction) {
		super(fromCascadingCompileAction ? SystemUDAResources.ACTION_WORKWITH_WWUDAS_LABEL : SystemUDAResources.ACTION_WORKWITH_UDAS_LABEL,
				fromCascadingCompileAction ? SystemUDAResources.ACTION_WORKWITH_WWUDAS_TOOLTIP : SystemUDAResources.ACTION_WORKWITH_UDAS_TOOLTIP, Activator.getDefault().getImageDescriptor(
						IUserActionsImageIds.WORK_WITH_USER_ACTIONS_1), parent);
		allowOnMultipleSelection(false);
		if (!fromCascadingCompileAction)
			setContextMenuGroup(ISystemContextMenuConstants.GROUP_WORKWITH);
		else
			setContextMenuGroup(ISystemContextMenuConstants.GROUP_REORGANIZE);
		setHelp(RSEUIPlugin.HELPPREFIX + "actn0045"); //$NON-NLS-1$
		setAvailableOffline(true);
	}

	/**
	 * Reset between runs
	 */
	public void reset() {
		subsystem = null;
		subsystemFactory = null;
		profile = null;
	}

	/**
	 * Set the subsystem which is the input to this action. You either call this,
	 *  or setSubSystemFactory, or the input is deduced from the selection when the action is run.
	 */
	public void setSubSystem(ISubSystem subsystem) {
		this.subsystem = subsystem;
		if (subsystem != null) {
			this.subsystemFactory = subsystem.getSubSystemConfiguration();
			this.profile = subsystem.getSystemProfile();
		}
	}

	/**
	 * Set the subsystem factory and profile, which are the input to this action. You either call this,
	 *  or setSubSystem, or the input is deduced from the selection when the action is run.
	 */
	public void setSubSystemFactory(ISubSystemConfiguration subsystemFactory, ISystemProfile profile) {
		this.subsystemFactory = subsystemFactory;
		this.profile = profile;
	}

	public void setActionSubsystem(SystemUDActionSubsystem udaActionSubsystem) {
		this.udaActionSubsystem = udaActionSubsystem;
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
	protected Dialog createDialog(Shell parent) {
		Object element = getFirstSelection();
		//System.out.println("First selection: "+element);
		if ((subsystem == null) && (element instanceof ISubSystem)) {
			subsystem = (ISubSystem) element;
		} 
		//Don't think we need to support invoking Work With User Action dialog from Team view
		/*
		else if ((subsystemFactory == null) && (element instanceof SystemTeamViewSubSystemConfigurationNode)) {
			SystemTeamViewSubSystemConfigurationNode ssfNode = (SystemTeamViewSubSystemConfigurationNode) element;
			subsystemFactory = ssfNode.getSubSystemConfiguration();
			profile = ssfNode.getProfile();
			//System.out.println("Profile is: "+profile);
		}
		*/
		SystemWorkWithUDAsDialog dlg = null;
		if (subsystem != null)
			dlg = new SystemWorkWithUDAsDialog(parent, subsystem, udaActionSubsystem);
		else
			dlg = new SystemWorkWithUDAsDialog(parent, subsystemFactory, profile, udaActionSubsystem);
		return dlg;
	}

	/**
	 * Required by parent. We use it to return the new name.
	 * In our case, we don't need it, so we return null.
	 */
	protected Object getDialogValue(Dialog dlg) {
		return null;
	}
}
