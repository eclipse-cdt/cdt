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
 *******************************************************************************/
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.internal.useractions.Activator;
import org.eclipse.rse.internal.useractions.IUserActionsImageIds;
import org.eclipse.rse.internal.useractions.ui.uda.SystemUDAResources;
import org.eclipse.rse.internal.useractions.ui.uda.SystemUDActionSubsystem;
import org.eclipse.rse.internal.useractions.ui.uda.SystemWorkWithUDTypeDialog;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.actions.SystemBaseDialogAction;
import org.eclipse.swt.widgets.Shell;

/**
 * The action that displays the Work With File Types GUI
 */
public class SystemWorkWithFileTypesAction extends SystemBaseDialogAction {
	private ISubSystem subsystem = null;
	private ISubSystemConfiguration subsystemFactory = null;
	private SystemUDActionSubsystem udaActionSubsystem;
	private ISystemProfile profile;
	private SystemWorkWithUDTypeDialog ourDlg = null;
	private String typeToPreSelect = null;
	private int preSelectTypeDomain;
	private String outputSelectedType;
	private int outputSelectedDomain = -1;

	/**
	 * Constructor when we have a subsystem
	 * @param parent The Shell of the parent UI for this dialog
	 * @param udaActionSubsystem The User Define Action subsystem we are launching this from/for
	 */
	public SystemWorkWithFileTypesAction(Shell parent, SystemUDActionSubsystem udaActionSubsystem) {
		this(parent);
		this.udaActionSubsystem = udaActionSubsystem;
		this.subsystem = udaActionSubsystem.getSubsystem();
		if (subsystem != null) {
			this.subsystemFactory = subsystem.getSubSystemConfiguration();
			this.profile = subsystem.getSystemProfile();
		}
		setAvailableOffline(true);
	}

	/**
	 * Constructor when we have a subsystem factory and profile
	 * @param parent The Shell of the parent UI for this dialog
	 * @param subSystemFactory The subsystem factory we are launching this from/for
	 * @param profile The profile we are launching this from/for
	 */
	public SystemWorkWithFileTypesAction(Shell parent, ISubSystemConfiguration subSystemFactory, ISystemProfile profile) {
		this(parent);
		this.subsystemFactory = subSystemFactory;
		this.profile = profile;
	}

	/**
	 * Constructor when we don't have anything
	 * At run time, the input is deduced from the first selected object.
	 * @param parent The Shell of the parent UI for this dialog
	 */
	public SystemWorkWithFileTypesAction(Shell parent) {
		super(SystemUDAResources.ACTION_WORKWITH_NAMEDTYPES_LABEL, SystemUDAResources.ACTION_WORKWITH_NAMEDTYPES_TOOLTIP, Activator.getDefault().getImageDescriptor(
				IUserActionsImageIds.WORK_WITH_NAMED_TYPES_1), parent);
		allowOnMultipleSelection(false);
		setContextMenuGroup(ISystemContextMenuConstants.GROUP_WORKWITH);
		setHelp(RSEUIPlugin.HELPPREFIX + "actn0046"); //$NON-NLS-1$
	}

	/**
	 * Set a type to preselect in the dialog.
	 * If domains are supported, specify the domain number, else
	 *  pass -1.
	 */
	public void preSelectType(int domain, String type) {
		this.typeToPreSelect = type;
		this.preSelectTypeDomain = domain;
	}

	/**
	 * Called by SystemBaseAction when selection is set.
	 * Our opportunity to verify we are allowed for this selected type.
	 */
	public boolean checkObjectType(Object selectedObject) {
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
		if ((subsystem == null) && (getFirstSelection() instanceof ISubSystem))
			subsystem = (ISubSystem) getFirstSelection();
		if (subsystem != null)
			ourDlg = new SystemWorkWithUDTypeDialog(parent, subsystem, udaActionSubsystem);
		else
			ourDlg = new SystemWorkWithUDTypeDialog(parent, subsystemFactory, profile);
		if (typeToPreSelect != null) ourDlg.preSelectType(preSelectTypeDomain, typeToPreSelect);
		return ourDlg;
	}

	/**
	 * Required by parent. After the dialog closes, its output
	 *  object contains the type that was selected at the time
	 *  of the close. This might be of interest to someone, so
	 *  we scoop it out here, and return it in the 
	 *  getSelectedTypeName() method.
	 */
	protected Object getDialogValue(Dialog dlg) {
		outputSelectedType = ((SystemWorkWithUDTypeDialog) dlg).getSelectedTypeName();
		if ((outputSelectedType != null) && (outputSelectedType.length() == 0)) outputSelectedType = null;
		return outputSelectedType;
	}

	/**
	 * Return the name of the type that was selected at the 
	 *  time of exiting the dialog. Might be null!
	 */
	public String getSelectedTypeName() {
		outputSelectedType = ourDlg.getSelectedTypeName();
		//System.out.println("outputSelectedType = " + outputSelectedType);
		if ((outputSelectedType != null) && (outputSelectedType.length() == 0)) outputSelectedType = null;
		return outputSelectedType;
	}

	/**
	 * Return the domain of the type that was selected at the 
	 *  time of exiting the dialog. Might be -1!
	 */
	public int getSelectedTypeDomain() {
		outputSelectedDomain = ourDlg.getSelectedTypeDomain();
		//System.out.println("outputSelectedDomain = " + outputSelectedDomain);
		return outputSelectedDomain;
	}
}
