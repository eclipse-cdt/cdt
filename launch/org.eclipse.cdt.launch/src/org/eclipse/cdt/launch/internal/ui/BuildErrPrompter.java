/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation, Freescale Semiconductor and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Freescale Semiconductor - customized for use in CDT
 *******************************************************************************/

package org.eclipse.cdt.launch.internal.ui;

import java.text.MessageFormat;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationsMessages;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Shell;

/**
 * This class is a customization of the platform's
 * CompileErrorProjectPromptStatusHandler. We use it to put up a more
 * CDT-centric message when building before a launch and there is an error in
 * the project. We want to let the user know what specific build configuration
 * is having a build error when the configuration is not the active one.
 * See https://bugs.eclipse.org/bugs/show_bug.cgi?id=309126#c11
 */
public class BuildErrPrompter implements IStatusHandler {

	/**
	 * Status code indicating there was an error in the main project. Linked to
	 * BuildErrPrompter via our statusHandlers extension (see plugin.xml)
	 */
	public static final int STATUS_CODE_ERR_IN_MAIN_PROJ = 1002;

	/**
	 * Status code indicating there was an error in a project referenced by the
	 * main project. Linked to BuildErrPrompter via our statusHandlers extension
	 * (see plugin.xml)
	 */
	public static final int STATUS_CODE_ERR_IN_REFERENCED_PROJS = 1003;

	/**
	 * Source is an array of three things, in the following order
	 * <ul>
	 * <li>launch configuration that was invoked
	 * <li>the name of the project the launch first attempted to build
	 * <li>the name of the build configuration that was built, or empty string
	 * if it was the active one. This argument should be non-empty ONLY if a
	 * not-active configuration was built.
	 * <ul>
	 * 
	 * @see org.eclipse.debug.core.IStatusHandler#handleStatus(org.eclipse.core.runtime.IStatus,
	 *      java.lang.Object)
	 */
	@Override
	public Object handleStatus(IStatus status, Object source) throws CoreException {

		if (!(source instanceof Object[])) {
			assert false : "status handler not given expected arguments"; //$NON-NLS-1$
			return Boolean.TRUE;
		}

		Object[] args = (Object[])source;
		if (args.length != 3 ||
			!(args[0] instanceof ILaunchConfiguration) ||
			!(args[1] instanceof String) ||
			!(args[2] instanceof String)) {
			assert false : "status handler not given expected arguments"; //$NON-NLS-1$
			return Boolean.TRUE;
		}

		final ILaunchConfiguration launchConfig = (ILaunchConfiguration)args[0];
		final String projectName = (String)args[1];
		final String buildConfigName = (String)args[2];

		// The platform does this check; we should, too
		if (DebugUITools.isPrivate(launchConfig)) {
			return Boolean.TRUE;
		}
	
		Shell shell = DebugUIPlugin.getShell();
		String title =  LaunchConfigurationsMessages.CompileErrorPromptStatusHandler_0; 
		String message;
		if (status.getCode() == STATUS_CODE_ERR_IN_MAIN_PROJ) {
			if (buildConfigName.length() > 0) {
				message = MessageFormat.format(
						LaunchMessages.BuildErrPrompter_error_in_specific_config, projectName, buildConfigName);  
			}
			else {
				message = MessageFormat.format(
						LaunchMessages.BuildErrPrompter_error_in_active_config, projectName);  
			}
		}
		else if (status.getCode() == STATUS_CODE_ERR_IN_REFERENCED_PROJS) {
			if (buildConfigName.length() > 0) {
				message = MessageFormat.format(
						LaunchMessages.BuildErrPrompter_error_in_referenced_project_specific,  
						projectName, buildConfigName);  
			}
			else {
				message = MessageFormat.format(
						LaunchMessages.BuildErrPrompter_error_in_referenced_project_active, 						
						projectName);  
			}
		}
		else {
			assert false : "this prompter was called for an unexpected status"; //$NON-NLS-1$
			return Boolean.TRUE;
		}
		
		// The rest is monkey-see, monkey-do (copied from
		// CompileErrorProjectPromptStatusHandler)
		
		IPreferenceStore store = DebugUIPlugin.getDefault().getPreferenceStore(); 
		String pref = store.getString(IInternalDebugUIConstants.PREF_CONTINUE_WITH_COMPILE_ERROR);
		if (pref != null) {
			if (pref.equals(MessageDialogWithToggle.ALWAYS)) {
				return Boolean.TRUE;
			}
		}
		MessageDialogWithToggle dialog = new MessageDialogWithToggle(shell, 
				title, 
				null, 
				message, 
				MessageDialog.QUESTION,
				new String[] {IDialogConstants.PROCEED_LABEL, IDialogConstants.CANCEL_LABEL}, 
				0,
				LaunchConfigurationsMessages.CompileErrorProjectPromptStatusHandler_1,
				false);
        int open = dialog.open();
		if (open == IDialogConstants.PROCEED_ID) {
        	if(dialog.getToggleState()) {
        		store.setValue(IInternalDebugUIConstants.PREF_CONTINUE_WITH_COMPILE_ERROR, MessageDialogWithToggle.ALWAYS);
        	}
            return Boolean.TRUE;
        } 
        else {
            return Boolean.FALSE;
        }
	}
}
