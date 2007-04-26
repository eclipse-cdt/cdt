/*******************************************************************************
 * Copyright (c) 2007 Symbian Software Limited and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Bala Torati (Symbian) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.templateengine.process;

import org.eclipse.cdt.core.templateengine.TemplateCore;
import org.eclipse.cdt.core.templateengine.TemplateEngineMessages;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;


/**
 * Abstract ProcessRunner class provides the methods to implement for processes.
 */
public abstract class ProcessRunner {
	
	private ProcessParameter[] params;
	
	void setProcessParameters(ProcessParameter[] params) {
		this.params = params;
	}
	
	/**
	 * Returns the Process Parameters.
	 * @return
	 */
	public ProcessParameter[] getProcessParameters() {
		return params;
	}
	
	/**
	 * Checks the whether the arguments are matching to Requied Parameters.
	 * @param args
	 * @return
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
	 * if the arguments are not matching to Requied Parameters.
	 * @param args
	 * @return
	 */
	public String getArgumentsMismatchMessage(ProcessArgument[] args) {
		if (params == null && args != null) {
			return TemplateEngineMessages.getString("ProcessRunner.unexpectedArguments"); //$NON-NLS-1$
		}
		if (params != null && args == null) {
			return TemplateEngineMessages.getString("ProcessRunner.missingArguments"); //$NON-NLS-1$
		}
		if (params == null && args == null) {
			return null;
		}
		if (params.length != args.length) {
			return TemplateEngineMessages.getString("ProcessRunner.missingArguments"); //$NON-NLS-1$
		}
		for (int i = 0; i < params.length; i++) {
			ProcessParameter param = params[i];
			ProcessArgument arg = args[i];
			if (!arg.isOfParameterType(param)) {
				return TemplateEngineMessages.getString("ProcessRunner.argumentsMismatch") + arg.getName(); //$NON-NLS-1$
			}
		}
		return null;
	}

	/**
	 * Returns the process message based on the pameters.
	 * @param processId
	 * @param code
	 * @param msg
	 * @return
	 */
	protected final String getProcessMessage(String processId, int code, String msg) {
		switch (code) {
			case IStatus.ERROR:
				return processId + TemplateEngineMessages.getString("ProcessRunner.error") + msg; //$NON-NLS-1$
			case IStatus.OK:
				return processId + TemplateEngineMessages.getString("ProcessRunner.success") + msg; //$NON-NLS-1$
			default:
				return processId + TemplateEngineMessages.getString("ProcessRunner.info") + msg; //$NON-NLS-1$
		}
	}
	
	/**
	 * @param template
	 * @param args
	 * @param processId
	 * @throws ProcessFailureException
	 */
	public abstract void process(TemplateCore template, ProcessArgument[] args, String processId, IProgressMonitor monitor) throws ProcessFailureException;
}
