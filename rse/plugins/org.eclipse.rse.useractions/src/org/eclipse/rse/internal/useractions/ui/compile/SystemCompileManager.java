/*******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * IBM Corporation - initial API and implementation
 * Martin Oberhuber (Wind River) - [168975] Move RSE Events API to Core
 * Martin Oberhuber (Wind River) - [186773] split ISystemRegistryUI from ISystemRegistry
 * David Dykstal (IBM) - [186589] move user actions API out of org.eclipse.rse.ui   
 * Martin Oberhuber (Wind River) - [cleanup] Avoid using SystemStartHere in production code
 * Xuan Chen     (IBM) - [223126] [api][breaking] Remove API related to User Actions in RSE Core/UI
 *******************************************************************************/

package org.eclipse.rse.internal.useractions.ui.compile;

import java.util.Hashtable;

import org.eclipse.core.resources.IFolder;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.internal.useractions.UserActionsPersistenceUtil;
import org.eclipse.rse.internal.useractions.api.ui.compile.SystemCascadingCompileAction;
import org.eclipse.rse.internal.useractions.ui.SystemCmdSubstVarList;
import org.eclipse.rse.internal.useractions.ui.uda.SystemUDAResources;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.subsystems.files.core.subsystems.IVirtualRemoteFile;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.rse.ui.SystemMenuManager;
import org.eclipse.rse.ui.messages.SystemMessageDialog;
import org.eclipse.rse.ui.view.ISystemRemoteElementAdapter;
import org.eclipse.rse.ui.view.SystemAdapterHelpers;
import org.eclipse.swt.widgets.Shell;

/**
 * This class manages the compile framework for a particular instantiation.
 * It is typically associated with a subsystem factory, but it is designed
 * to be used in other contexts as well. 
 * <p>
 * Here is the model for the compile framework:
 * <ul>
 *  <li>This compile manager manages a list of SystemCompileProfiles. For subsystem
 *         factories, there will be one of these per system profile. In other contexts,
 *         there may well be only one SystemCompileProfile per manager.
 *  <li>A SystemCompileProfile manages a list of a SystemCompileTypes, which are persisted.
 *      There is one compile type per compilable source type, like ".cpp" for unix file
 *      systems. 
 *  <li>Each SystemCompileType manages a list of SystemCompileCommands. Each command 
 *      represents a remote command for compiling source objects of this type. Each
 *      command has a label shown in the Compile popup menu and a compile string that
 *      is the command.
 *  <li>There is a Work With Compile Commmands dialog that shows all existing types,
 *       and for each type the list of existing compile commands. Users can use this
 *       to define new compile commands per type, and new types.
 *  <li>The compile profile will optionally pre-fill its list if the persistent file is
 *       not found (ie, on first touch). That pre-filled list is what the user gets by
 *       default until they define their own compile commands. The pre-filled compile
 *       commands can be edited by the user (except the label) and can be restored to 
 *       their shipped values.
 *  <li>When the user selects a compile command to run, an instance of SystemCompilableSource
 *       is created, containing a reference to the selected source object and the 
 *       selected SystemCompileCommand. A method in that object is called to actually
 *       run the compile command.
 *  <li>Compile commands can have substitution variables. The list of supported substitution
 *       variables is supplied by this SystemCompileManager. The work-with dialog allows the
 *       user to easily insert these into his command string, and the SystemCompilableSource
 *       object is responsible for making the substitutions at runtime, given the compile
 *       command and selected source object.
 * </ul>
 * @see SystemCompileProfile
 * @see SystemCompileType
 * @see SystemCompileCommand
 * @see SystemWorkWithCompileCommandsDialog
 */
public abstract class SystemCompileManager {
	private Hashtable compileProfilesPerProfile = new Hashtable();
	private Hashtable compileSubstitutorsPerConnection = new Hashtable();
	protected IHost systemConnection;
	protected String osType = "default";  //$NON-NLS-1$
	protected ISubSystemConfiguration subsystemFactory;
	/**
	 * As last set by calling setCurrentCompileCommand. Sometimes needed by subclasses.
	 */
	protected SystemCompileCommand currentCompileCommand;

	/**
	 * Constructor for SystemCompileManager
	 */
	public SystemCompileManager() {
		super();
	}

	/**
	 * Sets the subsystemconfiguration which instantiated this. Not called if using this
	 *  framework outside of the world of subsystem factories.
	 */
	public void setSubSystemFactory(ISubSystemConfiguration ssFactory) {
		this.subsystemFactory = ssFactory;
	}

	/**
	 * Return the subsystem factory which instantiated this instance, or as set via {@link #setSubSystemFactory(ISubSystemConfiguration)}.
	 */
	public ISubSystemConfiguration getSubSystemFactory() {
		return subsystemFactory;
	}

	/**
	 * Set the current system connection. This is set in the work with dialog, and 
	 *  used by the edit pane and other downstream classes.
	 */
	public void setSystemConnection(IHost systemConnection) {
		this.systemConnection = systemConnection;
	}

	/**
	 * Return the system connection with which this manager instance is associated.
	 */
	public IHost getSystemConnection() {
		return systemConnection;
	}

	/**
	 * Sets the current compile command. Called by the framework when running a compile command, prior to calling
	 *  commands that may be dependent on values in the compile command being processed.
	 */
	public void setCurrentCompileCommand(SystemCompileCommand compileCmd) { // defect 47808
		this.currentCompileCommand = compileCmd;
	}

	/**
	 * Return the current compile cmd as set by {@link #setCurrentCompileCommand(SystemCompileCommand)}.
	 */
	public SystemCompileCommand getCurrentCompileCommand() { // defect 47808
		return currentCompileCommand;
	}

	/**
	 * Return true (default) if multiple-select is supported for the compile action
	 */
	public boolean isMultiSelectSupported(SystemCompileCommand compileCmd) {
		return true;
	}

	/**
	 * Get the singleton compile profile given a SystemProfile.
	 * <p>
	 * Called in the Work With Compile Commands and the Compile cascading actions.
	 * <p>
	 * Do not override this, as the implementation is complete. However,
	 *  you must override createCompileProfile.
	 * <p>
	 * If you are using this outside of the subsystem framework, this method will not be called.
	 * 
	 * @see #createCompileProfile(ISystemProfile)
	 */
	public SystemCompileProfile getCompileProfile(ISystemProfile profile) {
		if (compileProfilesPerProfile == null) compileProfilesPerProfile = new Hashtable();
		SystemCompileProfile cprofile = (SystemCompileProfile) compileProfilesPerProfile.get(profile);
		if (cprofile == null) {
			cprofile = createCompileProfile(profile);
			if (cprofile != null) compileProfilesPerProfile.put(profile, cprofile);
		}
		return cprofile;
	}

	/**
	 * Return a list of all SystemCompileProfile objects.
	 * By default, returns one per active system profile. If not using the subsystem framework,
	 *  this must be overridden.
	 */
	public SystemCompileProfile[] getAllCompileProfiles() {
		ISystemProfile[] systemProfiles = RSECorePlugin.getTheSystemProfileManager().getActiveSystemProfiles();
		SystemCompileProfile[] compProfiles = null;
		if ((systemProfiles != null) && (systemProfiles.length > 0)) {
			compProfiles = new SystemCompileProfile[systemProfiles.length];
			for (int idx = 0; idx < systemProfiles.length; idx++)
				compProfiles[idx] = getCompileProfile(systemProfiles[idx]);
		}
		return compProfiles;
	}

	/**
	 * Overridable method to instantiate your SystemCompileProfile subclass for the 
	 * given system profile. 
	 * <p>It is important you pass the SystemProfile's name to the ctor of SystemCompileProfile.
	 */
	protected abstract SystemCompileProfile createCompileProfile(ISystemProfile profile);

	/**
	 * Callback method from SystemCompileProfile to get the folder into which the
	 *  xml file for this compile profile will be stored. By default uses the 
	 *  given subsystem factory. If you are using this framework outside of the
	 *  subsystem factory world, then override this method.
	 */
	public IFolder getCompileProfileFolder(SystemCompileProfile compProfile) {
		ISystemProfile systemProfile = getSystemProfile(compProfile);
		if (systemProfile == null) {
			SystemMessage msg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_GENERIC_E);
			msg.makeSubstitution(SystemUDAResources.SystemCompileManager_0);
			SystemBasePlugin
					.logError("In SystemCompileManager#getCompileProfileFolder, and we have gotten a null for the system profile named " + compProfile.getProfileName() + ". That should never happen!"); //$NON-NLS-1$ //$NON-NLS-2$
			SystemMessageDialog.displayErrorMessage(SystemBasePlugin.getActiveWorkbenchShell(), msg);
			//return null; BETTER TO LET IT CRASH... BETTER RECOVERY
		}
		//System.out.println("systemProfile = "    + systemProfile);
		//System.out.println("subsystemFactory = " + subsystemFactory);
		IFolder folder = UserActionsPersistenceUtil.getCompileCommandsFolder(systemProfile, subsystemFactory);
		return folder;
	}

	/**
	 * Callback from SystemProfile to decide, when no xml file is found, if we want
	 *  to prime the new xml file with defaults.
	 * <p>
	 * By default, returns true if the SystemProfile the given compile profile is 
	 *  associated with is a user-private profile. If not using the compile framework,
	 *  override this to use your own criteria.
	 */
	public boolean wantToPrimeWithDefaults(SystemCompileProfile profile) {
		ISystemProfile systemProfile = getSystemProfile(profile);
		//System.out.println("Inside wantToPrimeWithDefaults("+systemProfile.getName()+") and result is " + systemProfile.isDefaultPrivate());
		return systemProfile.isDefaultPrivate();
	}

	/**
	 * Return the default (supplied) compile commands to prime the compile commands with.
	 */
	public abstract SystemDefaultCompileCommands getDefaultCompileCommands();

	/**
	 * If the command is an default supplied command, returns its SystemDefaultCompileCommand object.
	 * Returns null if not a default supplied command.
	 * @param commandName - the name of the command, minus the parameters. This is not the label!
	 */
	public SystemDefaultCompileCommand getDefaultSuppliedCommand(String commandName) {
		SystemDefaultCompileCommands dftCmds = getDefaultCompileCommands();
		if (dftCmds == null) return null;
		return dftCmds.getCommand(commandName);
	}

	/**
	 * Given a SystemCompileProfile, return the SystemProfile it is associated with
	 */
	private ISystemProfile getSystemProfile(SystemCompileProfile compProfile) {
		//return currentProfile;
		//SystemProfile[] systemProfiles = RSECorePlugin.getTheSystemProfileManager().getActiveSystemProfiles(); THIS WAS THE BUG!!
		ISystemProfile[] systemProfiles = RSECorePlugin.getTheSystemProfileManager().getSystemProfiles();
		String profileName = compProfile.getProfileName();
		ISystemProfile currentProfile = null;
		for (int idx = 0; (currentProfile == null) && (idx < systemProfiles.length); idx++) {
			if (systemProfiles[idx].getName().equals(profileName)) currentProfile = systemProfiles[idx];
		}
		return currentProfile;
		/*
		 System.out.println("Searching for match on = "    + compProfile.getProfileName());
		 Enumeration keys = compileProfilesPerProfile.keys();
		 System.out.println(":Keys exist? " + keys.hasMoreElements());
		 SystemProfile match = null;
		 while ((match==null) && keys.hasMoreElements())
		 {
		 SystemProfile key = (SystemProfile)keys.nextElement();
		 SystemCompileProfile value = (SystemCompileProfile)compileProfilesPerProfile.get(key);
		 System.out.println("...key    = "    + key.getName());
		 System.out.println("...value  = "    + value.getProfileName());
		 if (value == compProfile)
		 match = key;
		 }
		 return match;
		 */
	}

	/**
	 * The compile manager and related classes is impacted by a profile rename, as we have
	 * some in-memory places to be updated. This method is called by the subsystem factory
	 * on a profile rename operation so we can update ourselves.
	 */
	public void profileRenamed(ISystemProfile profile, String oldName) {
		if (compileProfilesPerProfile == null) return;
		SystemCompileProfile cprofile = (SystemCompileProfile) compileProfilesPerProfile.get(profile);
		if (cprofile != null) cprofile.setProfileName(profile.getName());
	}

	/**
	 * Return true if the given remote object is potentially compilable. This decides
	 *  the existence of the Compile menu item. It is possible to enable/disable this if
	 *  there is no current compile command... this is a more course grained decision.
	 * <p>
	 * Our default implementation is to query the source type of the input object,
	 *  and return true only if there is a source type defined for it in any of the
	 *  currently active system profiles.
	 */
	public boolean isCompilable(Object selection) {
		/* MJB: Hack to disable compilation on virtual files for now */
		if (selection instanceof IVirtualRemoteFile) return false;
		ISystemRemoteElementAdapter rmtAdapter = SystemAdapterHelpers.getRemoteAdapter(selection);
		if (rmtAdapter == null) return false;
		String srcType = rmtAdapter.getRemoteSourceType(selection);
		if (srcType == null) return false;
		boolean compilable = false;
		ISystemProfile[] activeProfiles = RSECorePlugin.getTheSystemRegistry().getActiveSystemProfiles();
		for (int idx = 0; !compilable && (idx < activeProfiles.length); idx++) {
			SystemCompileProfile compProfile = getCompileProfile(activeProfiles[idx]);
			compProfile.addContributions(selection);
			compilable = (compProfile.getCompileType(srcType) != null);
		}
		return compilable;
	}

	/**
	 * Populate main context menu with a menu item for compile.
	 * Allows subclasses the opportunity to add compile actions for single and multiple selections. 
	 * <p>
	 * This is called by the addCommonRemoteObjectsActions method, if this subsystem
	 *  supports compiles.
	 */
	public void addCompileActions(Shell shell, IStructuredSelection selection, SystemMenuManager menu, String menuGroup) {
		if ((selection == null) || (selection.getFirstElement() == null)) {
			return;
		}
		int size = selection.size();
		if (size == 1) {
			addSingleSelectionCompileActions(shell, selection, menu, menuGroup);
		} else if (size > 1) {
			addMultipleSelectionCompileActions(shell, selection, menu, menuGroup);
		}
	}

	/**
	 * Adds compile actions for single selections. 
	 * Populates main context menu with a "Compile->" submenu cascade,
	 *  which will only be populated when the submenu is selected.
	 * <p>
	 * This is called by the addCompileActions method for single selections.
	 * Subclasses may override.
	 */
	public void addSingleSelectionCompileActions(Shell shell, IStructuredSelection selection, SystemMenuManager menu, String menuGroup) {
		SystemCascadingCompileAction promptAction = new SystemCascadingCompileAction(shell, true);
		SystemCascadingCompileAction noPromptAction = new SystemCascadingCompileAction(shell, false);
		menu.add(menuGroup, noPromptAction);
		menu.add(menuGroup, promptAction);
	}

	/**
	 * Adds compile actions for multiple selections. 
	 * By default, does nothing.
	 * This is called by the addCompileActions method for multiple selections.
	 * Subclasses may override.
	 */
	public void addMultipleSelectionCompileActions(Shell shell, IStructuredSelection selection, SystemMenuManager menu, String menuGroup) {
		SystemCompileMultipleSelectAction multiAction = new SystemCompileMultipleSelectAction(shell);
		menu.add(menuGroup, multiAction);
	}

	/**
	 * For support of the Work With Compile Commands dialog.
	 * <p>
	 * Return the substitution variables supported by compile commands managed by this manager.
	 */
	public abstract SystemCmdSubstVarList getSubstitutionVariableList();

	/**
	 * Return the substitutor for doing variable substitution. 
	 * <p>
	 * Override to return a class that implements ISystemCompileCommandSubstitutor, that knows how to 
	 *  substitute the variables found in getSubstitutionVariableList().
	 */
	protected ISystemCompileCommandSubstitutor getSubstitutor() {
		ISystemCompileCommandSubstitutor substor = (ISystemCompileCommandSubstitutor) compileSubstitutorsPerConnection.get(systemConnection);
		if (substor == null) {
			substor = createSubstitutor(systemConnection);
			compileSubstitutorsPerConnection.put(systemConnection, substor);
		}
		return substor;
	}

	/**
	 * Return the substitutor for doing variable substitution. 
	 * <p>
	 * Override to return a class that implements ISystemCompileCommandSubstitutor, that knows how to 
	 *  substitute the variables found in getSubstitutionVariableList().
	 */
	protected abstract ISystemCompileCommandSubstitutor createSubstitutor(IHost connection);

	/**
	 * For support of the Work With Compile Commands dialog.
	 * <p>
	 * Return our edit pane. Overriding this is an alternative to calling setEditPane.
	 * This is called in createContents
	 */
	public SystemCompileCommandEditPane getCompileCommandEditPane(Shell shell, ISystemCompileCommandEditPaneHoster hoster, boolean caseSensitive) {
		return new SystemCompileCommandEditPane(this, shell, hoster, caseSensitive);
	}

	/**
	 * For support of the Work With Compile Commands dialog.
	 * <p>
	 * Return the dialog used to prompt for a new source type when "Add..." is pressed beside the
	 *  source type combo. This returns an instance of the default SystemNewCompileSrcTypeDialog.
	 * <p>
	 * One strategy for subclasses is to call super on this method, then configure the results via 
	 *  the setters in the default dialog. Another is to subclass that dialog and return an instance
	 *  of the subclass.
	 */
	protected SystemNewCompileSrcTypeDialog getNewSrcTypeDialog(Shell shell, boolean caseSensitive) {
		//System.out.println("test 1" + caseSensitive);
		return new SystemNewCompileSrcTypeDialog(shell, this, caseSensitive);
	}

	public String getSourceTypePromptMRILabel() {
		return SystemUDAResources.RESID_WWCOMPCMDS_TYPES_LABEL;
	}

	public String getSourceTypePromptMRITooltip() {
		return SystemUDAResources.RESID_WWCOMPCMDS_TYPES_TOOLTIP;
	}
	
	public String getOSType()
	{
		return osType;
	}
}
