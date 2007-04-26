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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.templateengine.TemplateCore;
import org.eclipse.cdt.core.templateengine.TemplateEngine;
import org.eclipse.cdt.core.templateengine.TemplateEngineMessages;
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
	 * @param template
	 * @param element
	 */
	private void buildArgs(TemplateCore template, Element element) {
		List/*<Element>*/ children = TemplateEngine.getChildrenOfElement(element);
		ProcessParameter[] params = processRunner.getProcessParameters();
		List/*<ProcessArgument>*/ list = new ArrayList/*<ProcessArgument>*/(params.length);
		int childIndex = 0;
		for(int i=0; i<params.length; i++) {
			ProcessParameter param = params[i];
			boolean childrenRemain = childIndex < children.size();
			Element child = (Element) (childrenRemain ? children.get(childIndex) : null);
			if (param.isExternal() && (!childrenRemain || !param.getName().equals(child.getAttribute(ProcessArgument.ELEM_NAME)))) {
				list.add(new ProcessArgument(template, param));
			} else if (childrenRemain) {
				list.add(new ProcessArgument(template, child));
				childIndex++;
			}
		}
		while (childIndex < children.size()) {
			list.add(new ProcessArgument(template, (Element) children.get(childIndex++)));
		}
		args = (ProcessArgument[]) list.toArray(new ProcessArgument[list.size()]);
	}

	/**
	 * 
	 * @return boolean, true if the Process is Ready.
	 */
	public boolean isReadyToProcess() {
		if (processRunner == null || !processRunner.areArgumentsMatchingRequiredParameters(args) || !areAllMacrosExpandable()) {
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
			for(int i=0; i<args.length; i++) {
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
			for(int i=0; i<args.length; i++) {
				ProcessArgument arg = args[i];
				if ((macro = arg.getFirstNonExpandableMacro()) != null) {
					return TemplateEngineMessages.getString("Process.argument") + arg.getName() + TemplateEngineMessages.getString("Process.expandableMacro") + macro; //$NON-NLS-1$ //$NON-NLS-2$
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
				return id + TemplateEngineMessages.getString("Process.error") + msg; //$NON-NLS-1$
			case IStatus.OK:
				return id + TemplateEngineMessages.getString("Process.success") + msg; //$NON-NLS-1$
			default:
				return id + TemplateEngineMessages.getString("Process.info") + msg; //$NON-NLS-1$
		}
	}
	
	/**
	 * Constructor 
	 * @param monitor 
	 * @return
	 * @throws ProcessFailureException
	 */
	public IStatus process(IProgressMonitor monitor) throws ProcessFailureException {
		if (processRunner == null) {
			throw new ProcessFailureException(TemplateEngineMessages.getString("Process.unknownProcess") + processType); //$NON-NLS-1$
		}
		if (!processRunner.areArgumentsMatchingRequiredParameters(args)) {
			throw new ProcessFailureException(processRunner.getArgumentsMismatchMessage(args));
		}
		if (!areAllMacrosExpandable()) {
			throw new ProcessFailureException(getProcessMessage(IStatus.ERROR, getFirstNonExpandableMacroMessage(args)));
		}
		resolve();
		processRunner.process(template, args, id, monitor);
		return new Status(IStatus.INFO, CCorePlugin.PLUGIN_ID, IStatus.OK, getProcessMessage(IStatus.OK, TemplateEngineMessages.getString("Process.executedSuccessfully") + Arrays.asList(args)), null); //$NON-NLS-1$
	}

	private void resolve() {
		if (args != null) {
			for(int i=0; i<args.length; i++) {
				ProcessArgument arg = args[i];
				if (!arg.isResolved()) {
					arg.resolve();
				}
			}
		}
	}

	/**
	 * Returns the Macros.
	 * @return
	 */
	public Set/*<String>*/ getMacros() {
		Set/*<String>*/ set = null;
		if (args != null) {
			for(int i=0; i<args.length; i++) {
				ProcessArgument arg = args[i];
				Set/*<String>*/ subSet = arg.getMacros();
				if (subSet != null) {
					if (set == null) {
						set = new HashSet/*<String>*/();
					}
					set.addAll(subSet);
				}
			}
		}
		return set;
	}
	
	public String toString() {
		return id;
	}
}
