/*******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * Martin Oberhuber (Wind River) - [168975] Move RSE Events API to Core
 * Martin Oberhuber (Wind River) - [186128] Move IProgressMonitor last in all API
 * Martin Oberhuber (Wind River) - [186773] split ISystemRegistryUI from ISystemRegistry
 * David McKnight   (IBM)        - [225506] [api][breaking] RSE UI leaks non-API types
 * Kevin Doyle		(IBM)		 - [241015] Add getActionSubstVarList(SystemUDActionElement)
 * Kevin Doyle		(IBM)		 - [241866] Refresh After doesn't work for User Actions
 *******************************************************************************/

package org.eclipse.rse.internal.useractions.ui.uda;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.events.ISystemResourceChangeEvents;
import org.eclipse.rse.core.events.ISystemResourceChangeListener;
import org.eclipse.rse.core.events.SystemResourceChangeEvent;
import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.internal.useractions.UserActionsIcon;
import org.eclipse.rse.internal.useractions.ui.ISystemSubstitutor;
import org.eclipse.rse.internal.useractions.ui.SystemCmdSubstVarList;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.shells.ui.RemoteCommandHelpers;
import org.eclipse.rse.subsystems.files.core.model.RemoteFileUtility;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCmdSubSystem;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.rse.ui.messages.SystemMessageDialog;
import org.eclipse.rse.ui.view.ISystemEditableRemoteObject;
import org.eclipse.rse.ui.view.ISystemRemoteElementAdapter;
import org.eclipse.rse.ui.view.SystemTableViewProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.eclipse.ui.model.AdaptableList;
import org.eclipse.ui.model.WorkbenchContentProvider;

/** 
 * Provide the interface to customize and implement the user-defined actions support
 * for the subsystems which implement it.   Subsystems are expected to override
 * these methods as required.
 * <p>
 * For some subsystem factories, actions and types can be partitioned/scoped by
 *  "domain". Eg, for iSeries actions are scoped by object and member, and each
 *  has unique lists of actions and types.
 * This base class offers all the support for supporting domains, but it is triggered
 * by the method supportsDomains() which is overridden by child classes appropriately.
 * <p>
 * Some subsystems will support named types by which actions can be scoped. This
 * support is triggered by supportsTypes(), which returns true by default but can
 * be overridden by childclasses.
 */
public abstract class SystemUDActionSubsystem implements ISystemSubstitutor {
	public final static int DOMAIN_NONE = -1;
	protected ISubSystem _subsystem; // May be null for an import action
	protected ISubSystemConfiguration subsystemFactory; // for use in Team view where we show user actions per SSF.
	protected SystemUDActionManager udActionManager;
	protected SystemUDTypeManager udTypeManager;
	protected SystemUDAResolvedTypes udaResolvedTypes;
	protected SystemUDActionElement currentAction; // current action being processed
	protected boolean testAction; // is current action the test action?
	protected String osType = "default"; //$NON-NLS-1$

	/**
	 * Constructor
	 */
	public SystemUDActionSubsystem() {
		super();
		//this._subsystem = subsys;
	}

	/**
	 * Overridable method for child classes to do migration of their actions.
	 * This is called on first load of a document, which has a release stamp other than
	 * the current release
	 * @return true if any migration was done
	 */
	protected abstract boolean doActionsMigration(ISystemProfile profile, String oldRelease);

	/**
	 * Overridable method for child classes to do migration of their types.
	 * This is called on first load of a document, which has a release stamp other than
	 * the current release
	 * @return true if any migration was done
	 */
	protected abstract boolean doTypesMigration(ISystemProfile profile, String oldRelease);

	/**
	 * Overridable method for child classes to supply the label to display in the 
	 *  "New" node for actions. Typically only overridden if domains are not supported, 
	 *  as otherwise the child nodes of "New" have the specific labels.<br>
	 * If not overridden, then "New" is used.  
	 * @return translated label
	 */
	protected String getNewNodeActionLabel() {
		return SystemUDAResources.ACTION_CASCADING_NEW_LABEL;
	}

	/**
	 * Overridable method for child classes to supply the label to display in the 
	 *  "New" node for type. Typically only overridden if domains are not supported, 
	 *  as otherwise the child nodes of "New" have the specific labels.<br>
	 * If not overridden, then "New" is used.  
	 * @return translated label
	 */
	protected String getNewNodeTypeLabel() {
		return SystemUDAResources.ACTION_CASCADING_NEW_LABEL;
	}

	/**
	 * Get the singleton manager of user-defined actions for this subsystem factory
	 */
	public SystemUDActionManager getUDActionManager() {
		if (udActionManager == null) udActionManager = new SystemUDActionManager(this);
		return udActionManager;
	}

	/**
	 * Get the singleton manager of named file types for this subsystem factory
	 */
	public SystemUDTypeManager getUDTypeManager() {
		if ((udTypeManager == null) && supportsTypes()) udTypeManager = new SystemUDTypeManager(this);
		return udTypeManager;
	}

	/**
	 * Return the list of substitution variables for the given domain type.
	 * Called from edit pane in work with dialog.
	 * This must be overridden!
	 */
	public abstract SystemCmdSubstVarList getActionSubstVarList(int actionDomainType);

	 /**
	  * Return the list of substitution variables for the given UDA action.
	  */
	public SystemCmdSubstVarList getActionSubstVarList(SystemUDActionElement action)
	{
		int actionDomainType = action.getDomain();
		return getActionSubstVarList(actionDomainType);
	}
	
	/**
	 * Retrieve current subsystem
	 */
	public ISubSystem getSubsystem() {
		return _subsystem;
	}

	/**
	 * Set current subsystem
	 */
	public void setSubsystem(ISubSystem ss) {
		_subsystem = ss;
		if (ss != null) setSubSystemFactory(ss.getSubSystemConfiguration());
	}

	/**
	 * Retrieve current subsystem factory. Useful when we don't have a subsystem
	 */
	public ISubSystemConfiguration getSubSystemFactory() {
		return subsystemFactory;
	}

	/**
	 * Set current subsystem factory. Useful when we don't have a subsystem
	 */
	public void setSubSystemFactory(ISubSystemConfiguration ssf) {
		subsystemFactory = ssf;
	}

	/**
	 * Return true if actions can be scoped by file types
	 * Default is true
	 */
	public boolean supportsTypes() {
		return true;
	}

	/**
	 * Return true if actions can be scoped by file types for the given domain.
	 * Default is supportsTypes()
	 */
	public boolean supportsTypes(int domain) {
		return supportsTypes();
	}

	/**
	 * Return true if the action/type manager supports domains.
	 * Default is false
	 */
	public boolean supportsDomains() {
		return false;
	}

	/**
	 * In some cases, we supports domains in general, but only want to expose
	 *  one of those domains to the user. For example, for file subsystems,
	 *  we support folder and file domains, but for named types we really only
	 *  support the file domain.
	 * <p>
	 * Default is -1
	 */
	public int getSingleDomain(SystemUDBaseManager docManager) {
		return -1;
	}

	// **************************************************************
	// User Interface:  Adding Menu Actions, etc.
	// **************************************************************
	/** 
	 * Return the action's edit pane.
	 * Subclasses should override if they want to return their own edit pane.
	 * @param ss - the subsystem if you have it. If you don't have it, pass null.
	 * @param ssFactory - the subsystem factory, if you don't have the subsystem.
	 * @param profile - the subsystem factory, if you don't have the subsystem.
	 * @param parent - the hosting dialog/property page
	 * @param tv - the tree view if the parent is a dialog.
	 *	 */
	public SystemUDActionEditPane getCustomUDActionEditPane(ISubSystem ss, ISubSystemConfiguration ssFactory, ISystemProfile profile, ISystemUDAEditPaneHoster parent, ISystemUDTreeView tv) {
		return new SystemUDActionEditPane(this, parent, tv);
	}

	/** 
	 * Historical.
	 * Now replaced with {@link #getCustomUDActionEditPane(ISubSystem, ISubSystemConfiguration, ISystemProfile, ISystemUDAEditPaneHoster, ISystemUDTreeView)} 
	 */
	protected final SystemUDActionEditPane getCustomUDActionEditPane(ISubSystem ss, ISystemUDAEditPaneHoster parent, ISystemUDTreeView tv) {
		return getCustomUDActionEditPane(ss, null, null, parent, tv);
	}

	/** 
	 * Historical.
	 * Now replaced with {@link #getCustomUDActionEditPane(ISubSystem, ISubSystemConfiguration, ISystemProfile, ISystemUDAEditPaneHoster, ISystemUDTreeView)}  
	 */
	protected final SystemUDActionEditPane getCustomUDActionEditPane(ISubSystemConfiguration ssFactory, ISystemProfile profile, ISystemUDAEditPaneHoster parent, ISystemUDTreeView tv) {
		return getCustomUDActionEditPane(null, ssFactory, profile, parent, tv);
	}

	/** 
	 * Subclasses may override to provide a custom type edit pane subclass. 
	 * Subclasses should override if they want to return their own types pane.
	 * @param parent - the hosting dialog/property page
	 * @param tv - the tree view if the parent is a dialog.
	 */
	public SystemUDTypeEditPane getCustomUDTypeEditPane(ISystemUDAEditPaneHoster parent, ISystemUDTreeView tv) {
		return new SystemUDTypeEditPane(this, parent, tv);
	}

	// **************************************************************
	// Accessing UDA/UDT's in memory/storage:
	// **************************************************************
	/**
	 * Prime the user data with the default types.
	 */
	public abstract SystemUDTypeElement[] primeDefaultTypes(SystemUDTypeManager udtd);

	/**
	 * Prime the user data with the default actions.  Subsystem and profile specific
	 */
	public abstract SystemUDActionElement[] primeDefaultActions(SystemUDActionManager udad, ISystemProfile profile);

	/**
	 * Given this IBM-supplied user action, restore it to its IBM-supplied state.
	 * @return true if all went well, false if it wasn't restore for some reason
	 */
	public boolean restoreDefaultAction(SystemUDActionElement element, int domain, String actionName) {
		return false;
	}

	/**
	 * Given this IBM-supplied named type, restore it to its IBM-supplied state
	 * @return true if all went well, false if it wasn't restore for some reason
	 */
	public boolean restoreDefaultType(SystemUDTypeElement element, int domain, String typeName) {
		return false;
	}

	// **************************************************************
	// Running commands:
	// **************************************************************
	/**
	 * Return the command name that tells us this is an action for testing substitution variables.
	 * <p>
	 * Returns "ibm test action"
	 */
	public String getTestActionName() {
		return "ibm test action"; //$NON-NLS-1$
	}

	/**
	 * Return the default name of the test file in test mode.
	 * The test file is generated at action run-time when the action name is {@link #getTestActionName()}.
	 * <p>
	 * Returns "TESTUSERACTION.TXT"
	 */
	public String getTestFileName() {
		return "TESTUSERACTION.TXT"; //$NON-NLS-1$
	}

	/**
	 * Return the default path of the test file in test mode
	 * The test file is generated at action run-time when the action name is {@link #getTestActionName()}.
	 * <p>
	 * Returns "c:\\Test_RSE_User_Actions"
	 */
	public String getTestFilePath() {
		return "c:\\Test_RSE_User_Actions"; //$NON-NLS-1$
	}

	/**
	 * Overriddable method for printing out information about the collected names
	 *  for "invoke once" actions, when in test mode.
	 */
	protected void printTestActionInvokeOnceInformation(Shell shell, PrintWriter writer) {
	}

	/**
	 * When the user selects one or more objects in the RSE, then right clicks
	 *  and selects a user action, this method is called (by the SystemUDAsBaseAction
	 *  class).
	 * <p>
	 * For each selected object, the action's command is resolved (variable substitution done)
	 *   by calling doCommandSubstitution, and then run by calling runCommand(...).
	 * @param shell - the shell to use for display the prompt, if appropriate
	 * @param action - the user action to run
	 * @param selection - the currently selected objects
	 * @param viewer - the viewer we are running this from. Used to do the refresh if requested in this action. Can be null.
	 */
	public void run(Shell shell, SystemUDActionElement action, IStructuredSelection selection, ISystemResourceChangeListener viewer) {
		//Assert.isLegal(shell != null, "shell argument is null"); //$NON-NLS-1$
		processingSelection(true);
		Iterator elements = selection.iterator();
		this.currentAction = action;
		IRemoteCmdSubSystem cmdSubSystem = null;
		boolean runOnce = action.getCollect();
		boolean actionRunEvenOnce = false;
		boolean cancelled = false;
		/*
		 * DKM - I've taken the linebreak stripping out.  Now we intend to support
		 * batches of commands.  The interpreting of line breaks is now
		 * delegated to implementors of runCommand()
		 */
		String command = action.getCommand();
		// what is test action? For testing purposes, creating an action with this name means
		//  generating a local file to prove the variable substitution works...
		testAction = action.getName().toLowerCase().startsWith(getTestActionName());
		File testFile = null;
		PrintWriter testWriter = null;
		if (testAction) {
			this.currentAction = action;
			try {
				String testFileDir = action.getComment();
				if ((testFileDir == null) || (testFileDir.trim().length() == 0)) testFileDir = getTestFilePath();
				File testDir = new File(testFileDir);
				if (!testDir.exists()) {
					testDir.mkdir();
				}
				String testFileName = command;
				if ((testFileName == null) || (testFileName.trim().length() == 0)) testFileName = getTestFileName();
				testFile = new File(testDir, testFileName);
				String message = "In test action mode. Output file is: {0}"; //$NON-NLS-1$
				message = MessageFormat.format(message, new Object[] { testFile.getAbsolutePath() });
				SystemBasePlugin.logInfo(message);
				testWriter = new PrintWriter(new FileOutputStream(testFile));
				getActionSubstVarList(action.getDomain()).printDisplayStrings(testWriter);
			} catch (Exception exc) {
				if (testFile != null) {
					String message = "Error creating test file {0} for user actions:"; //$NON-NLS-1$
					message = MessageFormat.format(message, new Object[] { testFile.getAbsolutePath() });
					SystemBasePlugin.logError(message, exc);
				}
				return;
			}
		}
		// ------------------------------------------------------------
		// THIS ACTION IS TO BE RUN ONCE PER SELECTED OBJECT
		// ------------------------------------------------------------
		try {
			if (checkDirtyEditors(selection)) {
				if (!runOnce) {
					Object selectedObject = null;
					while (!cancelled && elements.hasNext()) {
						selectedObject = elements.next();
						//cmdSubSystemContext = selectedObject;
						if (cmdSubSystem == null) {
							cmdSubSystem = getCommandSubSystem(selectedObject);
						}
						if (testAction) {
							SystemCmdSubstVarList supportedVariables = getActionSubstVarList(action.getDomain());
							String[] substitutedVariables = supportedVariables.doAllSubstitutions(selectedObject, this);
							if (testWriter != null) {
								testWriter.println("Selected Object: " + getRemoteAdapter(selectedObject).getAbsoluteName(selectedObject)); //$NON-NLS-1$
								for (int idx = 0; idx < substitutedVariables.length; idx++) {
									testWriter.println("....." + substitutedVariables[idx]); //$NON-NLS-1$
								}
							}
						} else {
							String cmd = doCommandSubstitutions(action, command, selectedObject);
							// Prompt support
							if (action.getPrompt()) {
								// Prompt user and allow to edit the command. Honor their request to cancel
								cmd = promptCommand(shell, cmd);
								if (cmd == null) cancelled = true;
							}
							if (!cancelled) cancelled = !runCommand(shell, action, cmd, cmdSubSystem, selectedObject, (Viewer) viewer);
							if (!cancelled && !actionRunEvenOnce) actionRunEvenOnce = true;
						} // end else !testAction
					} // end while loop
				} // end if !runOnce
				// ------------------------------------------------------------
				// THIS ACTION IS TO BE RUN ONCE ONLY, FOR ALL SELECTED OBJECTS
				// ------------------------------------------------------------
				else {
					StringBuffer collectedNames = new StringBuffer();
					Object firstSelectedObject = collectNames(shell, elements, collectedNames);
					if (firstSelectedObject == null) // happens when something goes wrong. Msg already shown to user
						return;
					String nameVar = getAllNamesSubstitutionVariable();
					String cmd = command;
					if (nameVar != null) {
						if (testAction) cmd = nameVar;
						int nameVarIdx = cmd.indexOf(nameVar);
						if (nameVarIdx >= 0) {
							cmd = cmd.substring(0, nameVarIdx) + collectedNames.toString() + cmd.substring(nameVarIdx + nameVar.length());
						}
					}
					if (testAction && testWriter != null) {
						SystemCmdSubstVarList supportedVariables = getActionSubstVarList(action.getDomain());
						String[] substitutedVariables = supportedVariables.doAllSubstitutions(firstSelectedObject, this);
						testWriter.println("First Selected Object: " + getRemoteAdapter(firstSelectedObject).getAbsoluteName(firstSelectedObject)); //$NON-NLS-1$
						if (nameVar != null) testWriter.println("....." + nameVar + " = " + cmd); //$NON-NLS-1$ //$NON-NLS-2$
						for (int idx = 0; idx < substitutedVariables.length; idx++)
							testWriter.println("....." + substitutedVariables[idx]); //$NON-NLS-1$
						printTestActionInvokeOnceInformation(shell, testWriter);
					} else {
						cmd = doCommandSubstitutions(action, cmd, firstSelectedObject);
						// Prompt support
						if (action.getPrompt()) {
							// Prompt user and allow to edit the command. Honor their request to cancel
							cmd = promptCommand(shell, cmd);
							if (cmd == null) cancelled = true;
						}
						if (!cancelled) {
							cmdSubSystem = getCommandSubSystem(firstSelectedObject);
							cancelled = !runCommand(shell, action, cmd, cmdSubSystem, firstSelectedObject, (Viewer) viewer);
						}
						if (!cancelled) actionRunEvenOnce = true;
					} // end else !testAction   
				} // end else runOnce
				// ANYTHING GO WRONG??
			}
		} catch (Exception exc) {
			SystemMessageDialog.displayExceptionMessage(shell, exc);
			System.out.println("Error running user action " + command + ": " + exc.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}
		// ------------------------------------------------------------
		// REFRESH VIEW IF REQUESTED IN ACTION         
		// ------------------------------------------------------------
		if (actionRunEvenOnce && action.getRefresh()) {
			ISystemRegistry sr = RSECorePlugin.getTheSystemRegistry();
			try {
				Thread.sleep(500L);
			} catch (Exception exc) {
			} // defect 46380: give action's command time to run? I don't know, but this works!
			if (viewer != null) {
				sr.fireEvent(viewer, new SystemResourceChangeEvent(selection.toArray(), ISystemResourceChangeEvents.EVENT_REFRESH_REMOTE, null));
			} else {
				sr.fireEvent(new SystemResourceChangeEvent(selection.toArray(), ISystemResourceChangeEvents.EVENT_REFRESH_REMOTE, null));
			}
			// todo! verify we are sending the right event! ok, done... its the right one.
		}
		if (testWriter != null && testFile != null) {
			testWriter.flush();
			testWriter.close();
			SystemMessage msg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_GENERIC_I_HELP);
			msg.makeSubstitution("Test file " + testFile.getName() + " generated successfully", "The file was generated in directory " + testFile.getParent()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			SystemMessageDialog dlg = new SystemMessageDialog(shell, msg);
			dlg.openWithDetails();
		}
		processingSelection(false);
	} // end method     

	/**
	 * After an action's command has been resolved (vars substituted) this method
	 * is called to actually do the remote command execution
	 * @param shell - the shell to use if need to prompt for password or show msg dialog
	 * @param action - the action being processed, in case attributes of it need to be queried
	 * @param cmdString - the resolved command
	 * @param cmdSubSystem - this connection's command subsystem, which will run the command
	 * @param context - any context information the subsystem's runCommand might need
	 * @return true if we should continue, false if something went wrong
	 */
	protected boolean runCommand(Shell shell, SystemUDActionElement action, String cmdString, IRemoteCmdSubSystem cmdSubSystem, Object context, Viewer viewer) {
		boolean ok = false;
		if (cmdSubSystem != null) {
			ok = true;
			try {
				cmdSubSystem.runCommand(cmdString, context, new NullProgressMonitor());
			} catch (Exception e) {
				SystemBasePlugin.logError("RunUserAction", e); //$NON-NLS-1$
				SystemMessageDialog.displayExceptionMessage(shell, e);
				ok = false;
			}
		} // end if
		return ok;
	} // end method

	/**
	 * Called when user selects a user action to run, from the base user
	 * action class. Called by our run(...) method
	 */
	protected String doCommandSubstitutions(SystemUDActionElement action, String cmd, Object selectedObject) {
		this.currentAction = action;
		SystemCmdSubstVarList supportedVariables = getActionSubstVarList(action);
		return supportedVariables.doSubstitutions(cmd, selectedObject, this);
	} // end method

	/**
	 * When processing an action that has elected to be invoked only once, versus
	 *  once per selected object, we call this method to collect the names of the
	 *  selected objects into a single string buffer.
	 * <p>
	 * This can be overridden if need be. The default behaviour is to concatenate
	 * the quoted absolute name of each selected object.
	 * 
	 * @return first selected object, or null if something went wrong (msg will have been issued)
	 */
	protected Object collectNames(Shell shell, Iterator elements, StringBuffer collectedNames) {
		return collectNamesDefaultMethod(shell, elements, collectedNames);
	} // end method

	/**
	 * Allows subclasses to call it even if their immediate parent overrides
	 */
	protected Object collectNamesDefaultMethod(Shell shell, Iterator elements, StringBuffer collectedNames) {
		Object firstSelectedObject = null;
		while (elements.hasNext()) {
			Object selectedObject = elements.next();
			if (firstSelectedObject == null)
				firstSelectedObject = selectedObject;
			else
				collectedNames.append(" "); //$NON-NLS-1$
			collectedNames.append(getNameDelimiter());
			collectedNames.append(getRemoteAdapter(selectedObject).getAbsoluteName(selectedObject));
			collectedNames.append(getNameDelimiter());
		} // end while loop 
		return firstSelectedObject;
	} // end method

	/**
	 * When processing an action that has elected to be invoked only once, versus
	 *  once per selected object, we call this method to get the "all names"
	 *  substitution variable so that we can substitute it with the collection
	 *  of names of all selected objects. 
	 * <p>
	 * The default is "${resource_name}", but can be overridden. 
	 */
	protected String getAllNamesSubstitutionVariable() {
		return "${resource_name}"; //$NON-NLS-1$
	}

	/**
	 * When processing an action that has elected to be invoked only once, versus
	 *  once per selected object, we call this method to get the delimiter
	 *  character to surround each name in. 
	 * <p>
	 * The default is a double quote, but can be overridden. For example, for iSeries
	 *  native file systems, this is overridden with a single quote.
	 */
	protected char getNameDelimiter() {
		return '\"';
	}

	/**
	 * Called when processing user action that has the "prompt" attribute set.
	 * By default, puts up dialog allowing user to see and edit the fully resolved command.
	 * @param shell - the shell to host the modal dialog
	 * @param command - the fully resolved (variables substituted) command
	 * @return the edited command string, or null if the user pressed cancel
	 */
	protected String promptCommand(Shell shell, String command) {
		return promptCommandDefault(shell, command);
	}

	/**
	 * This allows child classes to call this directly
	 */
	protected String promptCommandDefault(Shell shell, String command) {
		SystemPromptUDADialog dialog = new SystemPromptUDADialog(shell, command);
		dialog.open();
		if (!dialog.wasCancelled())
			return dialog.getCommand();
		else
			return null;
	}

	/**
	 * Get the command subsystem associated the given remote object
	 */
	protected static IRemoteCmdSubSystem getCommandSubSystem(Object selectedObject) {
		return RemoteCommandHelpers.getCmdSubSystem(getRemoteAdapter(selectedObject).getSubSystem(selectedObject).getHost());
	}

	/**
	 * Get the first file subsystem associated the given remote object.
	 * May return null!
	 */
	protected static IRemoteFileSubSystem getFileSubSystem(Object selectedObject) {
		IRemoteFileSubSystem[] rfsss = RemoteFileUtility.getFileSubSystems(getCommandSubSystem(selectedObject).getHost());
		if ((rfsss != null) && (rfsss.length > 0))
			return rfsss[0];
		else
			return null;
	}

	/**
	 * Returns the implementation of ISystemRemoteElement for the given
	 * object.  Returns null if this object does not adaptable to this.
	 */
	protected static ISystemRemoteElementAdapter getRemoteAdapter(Object o) {
		if (!(o instanceof IAdaptable))
			return (ISystemRemoteElementAdapter) Platform.getAdapterManager().getAdapter(o, ISystemRemoteElementAdapter.class);
		else
			return (ISystemRemoteElementAdapter) ((IAdaptable) o).getAdapter(ISystemRemoteElementAdapter.class);
	}

	/**
	 * From the interface ISystemSubstitutor.
	 * <p>
	 * Return the string to substitute for the given substitution
	 *  variable, given the current context object. This object will
	 *  be passed whatever was passed into the doSubstitution method.
	 * <p>It is VERY IMPORTANT to return null if you can't do the 
	 * substitution for some reason! This is a clue to the algorithm
	 * that no change was made and increases performance.
	 * <p>
	 * We try to handle common substitutions here in the base class, and
	 * pass on any other requests to the child classes via a call to
	 * internalGetSubstitutionValue(String var, Object context)
	 */
	public String getSubstitutionValue(String subvar, Object context) {
		return getCommonSubstitutionValues(subvar, context);
	}

	/**
	 * This abstraction allows child subclasses to override getSubstitutionValues, yet 
	 *  grandchild subclasses to still call this common class if needed.
	 */
	public String getCommonSubstitutionValues(String subvar, Object context) {
		// ${action_name} = This user defined action name		
		if (subvar.equals("${action_name}")) //$NON-NLS-1$
			return currentAction.toString();
		// ${connection_name} = The connection in which this action is launched
		if (subvar.equals("${connection_name}")) //$NON-NLS-1$
			return getCommandSubSystem(context).getHost().getAliasName();
		// ${user_id} = The user ID that was used to signon with
		else if (subvar.equals("${user_id}")) //$NON-NLS-1$
			return getCommandSubSystem(context).getConnectorService().getUserId();
		// ${system_tempdir} = The fully qualified temp directory in remote system
		else if (subvar.equals("${system_tempdir}")) //$NON-NLS-1$
			return getCommandSubSystem(context).getConnectorService().getTempDirectory();
		// ${system_homedir} = The fully qualified home directory in remote system, for current user
		else if (subvar.equals("${system_homedir}")) //$NON-NLS-1$
			return getCommandSubSystem(context).getConnectorService().getHomeDirectory();
		// ${system_pathsep} = The path separator. ';' on Windows, ':' on Unix and Linux
		else if (subvar.equals("${system_pathsep}")) //$NON-NLS-1$
		{
			IRemoteFileSubSystem rfss = getFileSubSystem(context);
			if (rfss != null)
				return rfss.getParentRemoteFileSubSystemConfiguration().getPathSeparator();
			else
				return "system_pathsep not available"; // hopefully will never happen //$NON-NLS-1$
		}
		// ${system_filesep} = The file separator. '\\' on Windows, '/' on Unix and Linux
		else if (subvar.equals("${system_filesep}")) //$NON-NLS-1$
		{
			IRemoteFileSubSystem rfss = getFileSubSystem(context);
			if (rfss != null)
				return rfss.getParentRemoteFileSubSystemConfiguration().getSeparator();
			else
				return "system_filesep not available"; // hopefully will never happen //$NON-NLS-1$
		}
		// ${system_hostname} = The host name of the remote system
		else if (subvar.equals("${system_hostname}")) //$NON-NLS-1$
			return getCommandSubSystem(context).getHost().getHostName();
		// ${local_hostname} = The host name of the local system
		else if (subvar.equals("${local_hostname}")) //$NON-NLS-1$
			return RSECorePlugin.getLocalMachineName();
		// ${local_ip} = The ip address of the local system
		else if (subvar.equals("${local_ip}")) //$NON-NLS-1$
			return RSECorePlugin.getLocalMachineIPAddress();
		// ----------------------------------------------------------------------
		// We leave it to each subsystem plugin to define the following, as they
		//  will each define their own mri for the display text. However, we can
		//  do the substitutions right here as it generic code...
		// ----------------------------------------------------------------------
		// ${resource_name} = The name of the selected object
		else if (subvar.equals("${resource_name}")) //$NON-NLS-1$
			return getRemoteAdapter(context).getName(context);
		// ${resource_path} = The fully qualified name of the selected resource
		else if (subvar.equals("${resource_path}")) //$NON-NLS-1$
			return getRemoteAdapter(context).getAbsoluteName(context);
		else
			return internalGetSubstitutionValue(currentAction, subvar, context);
	}

	/**
	 * Overridable extension point for child class to do variable substitution for variables unique to them.
	 * 
	 */
	public abstract String internalGetSubstitutionValue(SystemUDActionElement currentAction, String substitutionVariable, Object context);

	/**
	 * 
	 */
	public boolean hasUnsupportedSubstitutionVars(Object action, int domain) {
		return false;
	}

	/**
	 * Check to see it any actions will apply to this selection.
	 * Stop checking as soon as 1 action is found.
	 * This method is an optimized, find-1-only, version of addUserActions() below.
	 * May be overriden for subsystem specific filtering of actions
	 * <b>CURRENTLY WE JUST RETURN TRUE</b>
	 */
	public boolean eligibleUserActionsForSelection(IStructuredSelection selection, ISystemProfile profile) {
		return true; // todo. Maybe ... doesn't seem worth it! 
	}

	/**
	 * Populate context menu ("User Actions->" cascading action) with user
	 *  actions that meet their type-scoping criteria for given selection.
	 * <p>
	 * If given a profile, the list is scoped to that, else it includes actions
	 *  for all active profiles.
	 */
	public Action[] addUserActions(IMenuManager menu, IStructuredSelection selection, ISystemProfile profile, Shell shell) {
		// access UDA tree for this subsystem
		SystemUDActionManager actMgr = getUDActionManager();
		// Go through each profile for this subsystem's factory
		ISystemProfile[] profiles = null;
		if (profile == null)
			profiles = getActiveSystemProfiles();
		else
			profiles = new ISystemProfile[] { profile };
		int domain = -1;
		if (supportsDomains()) {
			domain = getDomainFromSelection(selection);
			if (domain == -1) 
				return new Action[0];
		}
		boolean multiSelection = (selection.size() != 1);
		ArrayList actionList = new ArrayList();
		for (int idx = 0; idx < profiles.length; idx++) {
			profile = profiles[idx];
			SystemUDActionElement[] actionElements = actMgr.getActions(null, profile, domain);
			// Scan UDA's for matching types and add to menu.
			// if any match, then create the initial UDA submenu cascade item
			for (int i = 0; i < actionElements.length; i++) {
				SystemUDActionElement actionElement = actionElements[i];
				if (!actionElement.getShow()) continue;
				if (multiSelection && actionElement.getSingleSelection()) continue;
				if (supportsDomains() && (domain != actionElement.getDomain())) continue; // newly added... we were getting file actions on folders
				if (!supportsTypes() || meetsSelection(actionElement, selection, domain)) {
					SystemUDAsBaseAction uda = new SystemUDAsBaseAction(actionElement, shell, this);
					uda.setSelection(selection);
					uda.setShell(shell);
					uda.setEnabled(!getWorkingOfflineMode());
					actionList.add(uda);
					if (null != menu)
					{
						menu.add(uda);
					}
					
				}
			} // end for-loop
		} // end for all profiles loop
		Action[] list = (Action[])actionList.toArray(new Action[]{});
		
		return list;
		
	}

	/**
	 * We disable user defined actions if we are in work-offline mode.
	 * Currently, how we determine this is dependent on the subsystem factory.
	 */
	public boolean getWorkingOfflineMode() {
		return false;
	}

	/**
	 * Determine domain, given the selection.
	 * Eg subsystem that supports domains has to do this via overriding this method.
	 * If domains not supported, return -1.
	 */
	protected abstract int getDomainFromSelection(IStructuredSelection selection);

	/**
	 * Given an action, and the currently selected remote objects, and the domain of those,
	 *  return true if ALL of the selected remote objects matches any of the type criteria 
	 *  for this action
	 */
	protected boolean meetsSelection(SystemUDActionElement action, IStructuredSelection selection, int domainType) {
		String unresolvedActionTypes[] = action.getFileTypes();
		// fastpath for "ALL"!
		if ((unresolvedActionTypes == null) || (unresolvedActionTypes.length == 0))
			return true; // what else to do?
		else if (unresolvedActionTypes[0].equals("ALL")) //$NON-NLS-1$
			return true;
		Object actionTypes[] = resolveTypes(unresolvedActionTypes, domainType);
		Iterator elements = selection.iterator();
		Object element = null;
		while (elements.hasNext()) {
			element = elements.next();
			// OK if matches any one of the file types for an action
			boolean foundMatch = false;
			for (int j = 0; !foundMatch && (j < actionTypes.length); j++) {
				// compare current unnamed type to current selected object
				if (isMatch(actionTypes[j], element, domainType)) {
					foundMatch = true;
					break;
				}
			} // for j
			if (!foundMatch) return false;
		}
		return true;
	}

	/**
	 * Given a list of names that represent named types, 
	 *  resolve that into a concatenated list of all types for
	 *  the given type names.
	 * <p>
	 * Basically, this concatenates all the subtypes together.
	 * However, it also weeds out any redundancies
	 */
	protected String[] resolveTypes(String[] p_types, int domainType) {
		Vector types = new Vector();
		for (int i = 0; i < p_types.length; i++) {
			String fileTypes = getFileTypesForTypeName(p_types[i], domainType);
			if (fileTypes != null) {
				StringTokenizer st = new StringTokenizer(fileTypes, getTypesDelimiter());
				int n = st.countTokens();
				for (int j = 0; j < n; j++) {
					String token = st.nextToken().trim();
					if (types.indexOf(token) < 0) types.addElement(token);
				}
			}
		}
		String[] allTypes = new String[types.size()];
		for (int idx = 0; idx < allTypes.length; idx++)
			allTypes[idx] = (String) types.elementAt(idx);
		return allTypes;
	}

	/**
	 * Given a named-type name and a domain, find that type element and
	 *  return the types for that named type.
	 */
	private String getFileTypesForTypeName(String name, int domainType) {
		if (udaResolvedTypes == null) udaResolvedTypes = getResolvedTypesHelper();
		return udaResolvedTypes.getFileTypesForTypeName(name, domainType, getUDTypeManager());
	}

	/**
	 * Compares a particular file type (not named, but actual scalar/generic type)
	 *  to a specific user-selected remote object.
	 * Returns true if the object's information matches that of the given type
	 * <p>
	 * Must be overridden, but only called if supportsTypes() returns true. 
	 * Else, just return true!
	 * @param actionType - an unnamed file type, as in "*.cpp"
	 * @param selectedObject - one of the currently selected remote objects
	 * @param domainType - integer representation of current domain
	 */
	protected abstract boolean isMatch(Object actionType, Object selectedObject, int domainType);

	/**
	 * Get the delimiter used to delimiter the types in a type string.
	 * Default is " "
	 */
	protected String getTypesDelimiter() {
		return " "; //$NON-NLS-1$
	}

	/**
	 * 
	 */
	public void resetResolvedTypes() {
		udaResolvedTypes = null;
	}

	/**
	 * 
	 */
	public SystemUDAResolvedTypes getResolvedTypesHelper() {
		return new SystemUDAResolvedTypes();
	}

	// ---------------------------------------------------------------------------
	// NEW METHODS MOVED DOWN TO ABSTRACT-OUT/ENCAPSULATE THE NOTION OF DOMAINS...
	// DONE BY PHIL IN RELEASE 2.
	// PREVIOUSLY DOMAINS ONLY INTRODUCED IN THE ISERIES SUBCLASS FOR NATIVE FILES
	// XML SYNTAX FOR A DOMAIN:
	//  <Domain Name="XlatedDomainName" Type="DomainName"
	// ---------------------------------------------------------------------------
	/**
	 * For efficiency reasons, internally we use an integer to represent a domain.
	 * However, that has to be mapped to a name which is actually what is stored as the
	 *  "name" attribute for the xml domain node.
	 * This returns the maximum integer number supported by this action/type manager.
	 * Returns -1 by default.
	 * Needs to be overridden by children that support domains
	 */
	public int getMaximumDomain() {
		return -1;
	}

	/**
	 * For efficiency reasons, internally we use an integer to represent a domain.
	 * However, that has to be mapped to a name which is actually what is stored as the
	 *  "type" attribute for the xml domain node.
	 * <p>XML ATTRIBUTE SYNTAX FOR A DOMAIN:
	 * <code>
	 *  <Domain Name="XlatedDomainName" Type="DomainName"/>
	 * </code>
	 * This maps the given integer to its domain name.
	 * Returns null by default.
	 * Needs to be overridden by children that support domains
	 */
	public String mapDomainName(int domainInteger) {
		if ((domainInteger >= 0) && (domainInteger <= getMaximumDomain()))
			return getDomainNames()[domainInteger];
		else
			return null;
	}

	/**
	 * Map a given untranslated domain name to its integer value
	 */
	public int mapDomainName(String domainName) {
		String[] domainNames = getDomainNames();
		int match = -1;
		if ((domainNames != null) && (domainNames.length > 0)) {
			for (int idx = 0; (match == -1) && (idx < domainNames.length); idx++)
				if (domainNames[idx].equals(domainName)) match = idx;
		}
		return match;
	}

	/**
	 * For efficiency reasons, internally we use an integer to represent a domain.
	 * However, that has to be mapped to a translated name occasionally for the UI,
	 *  and indeed the translated name is stored in the XML in the "Name" attribute.
	 * <p>XML ATTRIBUTE SYNTAX FOR A DOMAIN:
	 * <code>
	 *  <Domain Name="XlatedDomainName" Type="DomainName"/>
	 * </code>
	 * This maps the given integer to its translated domain name.
	 * Returns null by default.
	 * Needs to be overridden by children that support domains
	 */
	public String mapDomainXlatedName(int domainInteger) {
		if ((domainInteger >= 0) && (domainInteger <= getMaximumDomain()))
			return getXlatedDomainNames()[domainInteger];
		else
			return null;
	}

	/**
	 * Same as above but specifically for what is shown in the work with user actions dialog for the new element
	 */
	public String mapDomainXlatedNewName(int domainInteger) {
		if ((domainInteger >= 0) && (domainInteger <= getMaximumDomain()))
			return getXlatedDomainNewNames()[domainInteger];
		else
			return null;
	}

	/**
	 * Same as above but specifically for what is shown in the work with named types dialog for the new element
	 */
	public String mapDomainXlatedNewTypeName(int domainInteger) {
		if ((domainInteger >= 0) && (domainInteger <= getMaximumDomain()))
			return getXlatedDomainNewTypeNames()[domainInteger];
		else
			return null;
	}

	/**
	 * Get the list of untranslated domain names
	 */
	public String[] getDomainNames() {
		return null;
	}

	/**
	 * Get the list of translated domain names
	 */
	public String[] getXlatedDomainNames() {
		return null;
	}

	/**
	 * Get the list of translated domain names for use in the tree view, for the "New" nodes,
	 *  in the Work With User Actions dialog.
	 */
	public String[] getXlatedDomainNewNames() {
		return null;
	}

	/**
	 * Get the list of translated domain names for use in the tree view, for the "New" nodes,
	 *  in the Work With User Types dialog.
	 */
	public String[] getXlatedDomainNewTypeNames() {
		return null;
	}

	/**
	 * Get the domain icon to show in the tree views
	 */
	public Image getDomainImage(int domain) {
		return null;
	}

	/**
	 * Get the domain icon to show in the tree views, for the new item for this domain
	 */
	public Image getDomainNewImage(int domain) {
		return UserActionsIcon.USERACTION_NEW.getImage();
	}

	/**
	 * Get the domain icon to show in the named type tree view, for the new item for this domain
	 */
	public Image getDomainNewTypeImage(int domain) {
		return UserActionsIcon.USERTYPE_NEW.getImage();
	}

	/**
	 * Get the active system profiles
	 */
	protected ISystemProfile[] getActiveSystemProfiles() {
		return RSECorePlugin.getTheSystemRegistry().getActiveSystemProfiles();
	}

	protected List getDirtyEditors(IStructuredSelection sel) {
		List dirtyEditors = new ArrayList();
		List selection = sel.toList();
		for (int i = 0; i < selection.size(); i++) {
			Object selected = selection.get(i);
			if (selected instanceof IAdaptable) {
				ISystemEditableRemoteObject editable = getEditableFor((IAdaptable) selected);
				if (editable != null) {
					try {
						// is the file being edited?
						if (editable.checkOpenInEditor() == 0) {
							// reference the editing editor
							editable.openEditor();
							// file is open in editor - prompt for save
							if (editable.isDirty()) {
								dirtyEditors.add(editable);
							}
						}
					} catch (Exception e) {
					}
				}
			}
		}
		return dirtyEditors;
	}

	protected ISystemEditableRemoteObject getEditableFor(IAdaptable selected) {
		ISystemRemoteElementAdapter adapter = (ISystemRemoteElementAdapter) selected.getAdapter(ISystemRemoteElementAdapter.class);
		if (adapter.canEdit(selected)) {
			ISystemEditableRemoteObject editable = adapter.getEditableRemoteObject(selected);
			try {
				editable.setLocalResourceProperties();
			} catch (Exception e) {
			}
			return editable;
		}
		return null;
	}

	protected boolean checkDirtyEditors(IStructuredSelection selection) {
		List dirtyEditors = getDirtyEditors(selection);
		if (dirtyEditors.size() > 0) {
			AdaptableList input = new AdaptableList();
			for (int i = 0; i < dirtyEditors.size(); i++) {
				ISystemEditableRemoteObject rmtObj = (ISystemEditableRemoteObject) dirtyEditors.get(i);
				input.add(rmtObj.getRemoteObject());
			}
			WorkbenchContentProvider cprovider = new WorkbenchContentProvider();
			SystemTableViewProvider lprovider = new SystemTableViewProvider();
			ListSelectionDialog dlg = new ListSelectionDialog(SystemBasePlugin.getActiveWorkbenchShell(), input, cprovider, lprovider, SystemUDAResources.EditorManager_saveResourcesMessage);
			dlg.setInitialSelections(input.getChildren());
			dlg.setTitle(SystemUDAResources.EditorManager_saveResourcesTitle);
			int result = dlg.open();
			//Just return false to prevent the operation continuing
			if (result == IDialogConstants.CANCEL_ID) return false;
			Object[] filesToSave = dlg.getResult();
			for (int s = 0; s < filesToSave.length; s++) {
				IAdaptable rmtObj = (IAdaptable) filesToSave[s];
				ISystemEditableRemoteObject editable = getEditableFor(rmtObj);
				editable.doImmediateSaveAndUpload();
			}
		}
		return true;
	}

	/**
	 * Method called at the start and end of running user actions
	 * This allows children a chance to perform some action before and after
	 * the actions are run by overriding this method.
	 * @param processingSelection true before proecssing, false after processing	 
	 */
	protected void processingSelection(boolean processingSelection) {
	}
	
	public String getOSType()
	{
		return osType;
	}
}
