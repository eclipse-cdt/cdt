/*******************************************************************************
 * Copyright (c) 2007, 2014 Symbian Software Limited and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Bala Torati (Symbian) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.templateengine.process;

import org.eclipse.cdt.core.templateengine.TemplateCore;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

import com.ibm.icu.text.MessageFormat;

/**
 * Abstract ProcessRunner class provides the methods to implement for processes.
 */
public abstract class ProcessRunner {
	private ProcessParameter[] params = new ProcessParameter[0];

	void setProcessParameters(ProcessParameter[] params) {
		this.params = params == null ? new ProcessParameter[0] : params;
	}

	/**
	 * Returns the Process Parameters.
	 */
	public ProcessParameter[] getProcessParameters() {
		return params;
	}

	/**
	 * @since 5.6
	 */
	protected ProcessFailureException missingArgException(String processId, String varname) {
		String msg = MessageFormat.format(Messages.ProcessRunner_missingArg, varname);
		return new ProcessFailureException(getProcessMessage(processId, IStatus.ERROR, msg));
	}

	/**
	 * Checks the whether the arguments are matching the required parameters.
	 */
	protected final boolean areArgumentsMatchingRequiredParameters(ProcessArgument[] args) {
		if ((params == null && args != null) || (params != null && args == null)) {
			return false;
		}
		if (params == null && args == null) {
			return true;
		}
		if (params.length != args.length) {
			return false;
		}
		for (int i = 0; i < params.length; i++) {
			if (!args[i].isOfParameterType(params[i])) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Return the String containing the mismatching message
	 * if the arguments are not matching the required parameters.
	 */
	public String getArgumentsMismatchMessage(ProcessArgument[] args) {
		if (params == null && args != null) {
			return Messages.getString("ProcessRunner.unexpectedArguments"); //$NON-NLS-1$
		}
		if (params != null && args == null) {
			return Messages.getString("ProcessRunner.missingArguments"); //$NON-NLS-1$
		}
		if (params == null && args == null) {
			return null;
		}
		if (params.length != args.length) {
			return Messages.getString("ProcessRunner.missingArguments"); //$NON-NLS-1$
		}
		for (int i = 0; i < params.length; i++) {
			ProcessParameter param = params[i];
			ProcessArgument arg = args[i];
			if (!arg.isOfParameterType(param)) {
				return Messages.getString("ProcessRunner.argumentsMismatch") + arg.getName(); //$NON-NLS-1$
			}
		}
		return null;
	}

	/**
	 * Returns the process message based on the parameters.
	 */
	protected final String getProcessMessage(String processId, int code, String msg) {
		switch (code) {
		case IStatus.ERROR:
			return processId + Messages.getString("ProcessRunner.error") + msg; //$NON-NLS-1$
		case IStatus.OK:
			return processId + Messages.getString("ProcessRunner.success") + msg; //$NON-NLS-1$
		default:
			return processId + Messages.getString("ProcessRunner.info") + msg; //$NON-NLS-1$
		}
	}

	/**
	 * @param template
	 * @param args
	 * @param processId
	 * @throws ProcessFailureException
	 */
	public abstract void process(TemplateCore template, ProcessArgument[] args, String processId,
			IProgressMonitor monitor) throws ProcessFailureException;
}
