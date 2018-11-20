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
 *     Bala Torati (Symbian) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.templateengine.process;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.templateengine.TemplateCore;
import org.eclipse.cdt.core.templateengine.TemplateEngine;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.w3c.dom.Element;

/**
 * This class contains methods to get first process block element, next process
 * block element and checks for next process block element.
 */
public class Process {
	public static final String ELEM_TYPE = "type"; //$NON-NLS-1$

	private ProcessRunner processRunner;
	private ProcessArgument[] args;
	private TemplateCore template;
	private String id;
	private String processType;

	/**
	 * Constructor to create a process.
	 * @param template
	 * @param element
	 * @param id
	 */
	public Process(TemplateCore template, Element element, String id) {
		this.template = template;
		this.id = id;
		processType = element.getAttribute(ELEM_TYPE);
		processRunner = ProcessRunnerFactory.getDefault().getProcessRunner(processType);
		if (processRunner != null) {
			buildArgs(template, element);
		}
	}

	/**
	 * This method build the necessary Arguments for the process
	 * @param templateCore
	 * @param element
	 */
	private void buildArgs(TemplateCore templateCore, Element element) {
		List<Element> children = TemplateEngine.getChildrenOfElement(element);
		ProcessParameter[] params = processRunner.getProcessParameters();
		List<ProcessArgument> list = new ArrayList<>(params.length);
		int childIndex = 0;
		for (int i = 0; i < params.length; i++) {
			ProcessParameter param = params[i];
			boolean childrenRemain = childIndex < children.size();
			Element child = (childrenRemain ? children.get(childIndex) : null);
			if (param.isExternal()
					&& (child == null || !param.getName().equals(child.getAttribute(ProcessArgument.ELEM_NAME)))) {
				list.add(new ProcessArgument(templateCore, param));
			} else if (childrenRemain) {
				list.add(new ProcessArgument(templateCore, child));
				childIndex++;
			}
		}
		while (childIndex < children.size()) {
			list.add(new ProcessArgument(templateCore, children.get(childIndex++)));
		}
		args = list.toArray(new ProcessArgument[list.size()]);
	}

	/**
	 *
	 * @return boolean, true if the Process is Ready.
	 */
	public boolean isReadyToProcess() {
		if (processRunner == null || !processRunner.areArgumentsMatchingRequiredParameters(args)
				|| !areAllMacrosExpandable()) {
			return false;
		}
		return true;
	}

	/**
	 *
	 * @return boolean, true if Macros are Exapandable.
	 */
	private boolean areAllMacrosExpandable() {
		if (args != null) {
			for (int i = 0; i < args.length; i++) {
				ProcessArgument arg = args[i];
				if (!arg.areAllMacrosExpandable()) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Returns First NonExpandable Macro Message
	 */
	private String getFirstNonExpandableMacroMessage(ProcessArgument[] args2) {
		if (args != null) {
			String macro;
			for (int i = 0; i < args.length; i++) {
				ProcessArgument arg = args[i];
				if ((macro = arg.getFirstNonExpandableMacro()) != null) {
					return Messages.getString("Process.argument") + arg.getName() + //$NON-NLS-1$
							Messages.getString("Process.expandableMacro") + macro; //$NON-NLS-1$
				}
			}
		}
		return null;
	}

	/**
	 * Returns the Process Message depending on the parameters.
	 * @param code
	 * @param msg
	 * @return
	 */
	private String getProcessMessage(int code, String msg) {
		switch (code) {
		case IStatus.ERROR:
			return id + Messages.getString("Process.error") + msg; //$NON-NLS-1$
		case IStatus.OK:
			return id + Messages.getString("Process.success") + msg; //$NON-NLS-1$
		default:
			return id + Messages.getString("Process.info") + msg; //$NON-NLS-1$
		}
	}

	/**
	 * Executes this process
	 * @param monitor
	 * @return the result of executing this process
	 * @throws ProcessFailureException
	 */
	public IStatus process(IProgressMonitor monitor) throws ProcessFailureException {
		if (processRunner == null) {
			throw new ProcessFailureException(Messages.getString("Process.unknownProcess") + processType); //$NON-NLS-1$
		}
		if (!processRunner.areArgumentsMatchingRequiredParameters(args)) {
			throw new ProcessFailureException(processRunner.getArgumentsMismatchMessage(args));
		}
		if (!areAllMacrosExpandable()) {
			throw new ProcessFailureException(
					getProcessMessage(IStatus.ERROR, getFirstNonExpandableMacroMessage(args)));
		}
		resolve();
		processRunner.process(template, args, id, monitor);
		return new Status(IStatus.INFO, CCorePlugin.PLUGIN_ID, IStatus.OK,
				getProcessMessage(IStatus.OK, Messages.getString("Process.executedSuccessfully") + Arrays.asList(args)), //$NON-NLS-1$
				null);
	}

	private void resolve() {
		if (args != null) {
			for (int i = 0; i < args.length; i++) {
				ProcessArgument arg = args[i];
				if (!arg.isResolved()) {
					arg.resolve();
				}
			}
		}
	}

	/**
	 * @return the macros defined in the context of this process
	 */
	public Set<String> getMacros() {
		Set<String> set = null;
		if (args != null) {
			for (int i = 0; i < args.length; i++) {
				ProcessArgument arg = args[i];
				Set<String> subSet = arg.getMacros();
				if (subSet != null) {
					if (set == null) {
						set = new HashSet<>();
					}
					set.addAll(subSet);
				}
			}
		}
		return set;
	}

	@Override
	public String toString() {
		return id;
	}
}
