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
 *******************************************************************************/
package org.eclipse.rse.internal.useractions.ui.compile;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.internal.useractions.ui.SystemCmdSubstVarList;
import org.eclipse.rse.shells.ui.RemoteCommandHelpers;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCmdSubSystem;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.rse.ui.view.ISystemRemoteElementAdapter;
import org.eclipse.rse.ui.view.SystemAdapterHelpers;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * When a user selects a remote compilable source member, and then one of the compile commands
 *  from the cascading compile popup menu action, an instance of this class is created to manage
 *  the actual running of the compile command against the selected source.
 */
public class SystemCompilableSource implements Runnable {
	protected Object firstSelection;
	protected boolean isPrompt;
	protected SystemCompileCommand compileCmd;
	protected ISystemRemoteElementAdapter rmtAdapter;
	protected Shell shell;
	protected Viewer viewer;

	/**
	 * Constructor for SystemCompilableSource.
	 * Instantiated by SystemCompileAction.
	 * @param shell - the current shell, in case we need it for the prompt dialog or error messages.
	 * @param firstSelection - the selected compilable source member
	 * @param compileCmd - the Compile Command that is to be run against the selected compilable source member
	 * @param isPrompt - true if the user choose the flavor of the action to prompt the compile command
	 * @param viewer - the viewer that originated the compile action
	 */
	public SystemCompilableSource(Shell shell, Object firstSelection, SystemCompileCommand compileCmd, boolean isPrompt, Viewer viewer) {
		super();
		this.shell = shell;
		this.firstSelection = firstSelection;
		this.compileCmd = compileCmd;
		this.isPrompt = isPrompt;
		this.rmtAdapter = SystemAdapterHelpers.getRemoteAdapter(firstSelection);
		this.viewer = viewer;
	}

	/**
	 * Return the shell as set in the constructor.
	 * If this is null, we attempt to get the active shell
	 */
	public Shell getShell() {
		if (shell != null)
			return shell;
		else {
			shell = SystemBasePlugin.getActiveWorkbenchShell();
			if (shell == null) {
				shell = Display.getCurrent().getActiveShell();
				if (shell == null) {
					Shell[] shells = Display.getCurrent().getShells();
					for (int i = 0; (i < shells.length) && (shell == null); i++)
						if (!shells[i].isDisposed() && shells[i].isEnabled()) shell = shells[i];
				}
			}
			return shell;
		}
	}

	/**
	 * Return the selected compilable remote source object we are to compile
	 */
	protected Object getSelectedObject() {
		return firstSelection;
	}

	/**
	 * Return the compile command to compile the selected source object with.
	 */
	protected SystemCompileCommand getCompileCommand() {
		return compileCmd;
	}

	/**
	 * Return if the compile command is to be prompted or not
	 */
	protected boolean isPrompt() {
		return isPrompt;
	}

	/**
	 * Return the remote adapter for the currently selected compilable remote source object
	 */
	protected ISystemRemoteElementAdapter getRemoteAdapter() {
		return rmtAdapter;
	}

	/**
	 * Return the source type of this remote object
	 */
	public String getSourceType() {
		return rmtAdapter.getRemoteSourceType(firstSelection);
	}

	/**
	 * Return the system connection from the which the selected object came from
	 */
	public IHost getSystemConnection() {
		return getSubSystem().getHost();
	}

	/**
	 * Return the subsystem which is responsible for producing the remote object.
	 */
	protected ISubSystem getSubSystem() {
		return rmtAdapter.getSubSystem(firstSelection);
	}

	/**
	 * Return the command subsystem for the remote connection. Typically needed to actually run the
	 *  compile command.
	 */
	protected IRemoteCmdSubSystem getCommandSubSystem() {
		return RemoteCommandHelpers.getCmdSubSystem(getSubSystem().getHost());
	}

	/**
	 * Return the substitution variable list. Called by runCompileCommand default implementation.
	 * By default, returns it from the SystemCompileManager, but you can override if you have your
	 * own list.
	 */
	protected SystemCmdSubstVarList getSubstitutionVariableList() {
		SystemCompileManager mgr = compileCmd.getParentType().getParentProfile().getParentManager();
		mgr.setCurrentCompileCommand(compileCmd); // defect 47808
		SystemCmdSubstVarList varlist = mgr.getSubstitutionVariableList();
		mgr.setCurrentCompileCommand(null); // defect 47808
		return varlist;
	}

	/**
	 * Return the substitutor for doing variable substitution. Called by runCompileCommand default implementation.
	 * By default, returns it from the SystemCompileManager, but you can override if you have your
	 * own list.
	 */
	protected ISystemCompileCommandSubstitutor getSubstitutor() {
		SystemCompileManager mgr = compileCmd.getParentType().getParentProfile().getParentManager();
		mgr.setCurrentCompileCommand(compileCmd); // defect 47808
		// if not called by the compile action, system connection is not set, so set it here
		if (mgr.getSystemConnection() == null) mgr.setSystemConnection(getSystemConnection());
		ISystemCompileCommandSubstitutor substitutor = compileCmd.getParentType().getParentProfile().getParentManager().getSubstitutor();
		mgr.setCurrentCompileCommand(null); // defect 47808
		return substitutor;
	}

	/**
	 * Run the compile command against the selected source.
	 * Do not override this directly, as it tries to handle the prompting first.
	 * Rather, override internalPromptCompileCommand(String) and internalRunCompileCommand(String)
	 */
	public boolean runCompileCommand() {
		//String originalString = compileCmd.getCurrentString();		
		String substitutedString = getSubstitutedString(compileCmd, firstSelection, getSubstitutor());
		if (isPrompt()) {
			substitutedString = internalPromptCompileCommand(substitutedString);
			if (substitutedString == null || substitutedString.trim().equals("")) //$NON-NLS-1$
				return false;
		}
		//System.out.println("Running compile command...");
		//System.out.println("...original cmd: '" + originalString + "'");
		//System.out.println("...final cmd...: '" + substitutedString + "'");
		return internalRunCompileCommand(substitutedString);
	}

	/**
	 * Given the compile command, the selected source object, do the variable substitution.
	 * This can be overridden if needed. The default implementation here is:
	 * <pre><code>
	 *	return compileCmd.doVariableSubstitution(firstSelection, substitutor);
	 * </code></pre>
	 */
	protected String getSubstitutedString(SystemCompileCommand compileCmd, Object firstSelection, ISystemCompileCommandSubstitutor substitutor) {
		return compileCmd.doVariableSubstitution(firstSelection, substitutor);
	}

	/**
	 * After the substituting and the prompting, it is now time to the remote running of the 
	 *  fully resolved compile command. Do that here.
	 * <p>
	 * Must be overridden.
	 * @return true if all is well, false if something went wrong. This prevents the next compile from running
	 */
	protected boolean internalRunCompileCommand(String compileCmd) {
		return true;
	}

	/**
	 * When running a compile command from the prompt menu, we prompt the command. This is the 
	 *  method that does this prompt. Override if appropriate, else a simple dialog is presented
	 *  to the user showing the substituted compile command and allowing them to change it.
	 * <p>
	 * By default, this uses the SystemPromptCompileCommandDialog dialog to prompt the user to change
	 *  the compile command.
	 */
	protected String internalPromptCompileCommand(String substitutedCompileCommand) {
		String promptedCmd = substitutedCompileCommand;
		SystemPromptCompileCommandDialog promptDlg = new SystemPromptCompileCommandDialog(shell, substitutedCompileCommand);
		promptDlg.open();
		if (!promptDlg.wasCancelled())
			promptedCmd = promptDlg.getCommand();
		else
			promptedCmd = null;
		return promptedCmd;
	}

	/**
	 * The run() method for running code in a thread. This is empty by default, but we include it 
	 *  for your convenience to save adding "implements Runnable" in your subclass. Override if
	 *  using threads or asynchExec.
	 */
	public void run() {
	}
}
