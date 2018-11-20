/*******************************************************************************
 * Copyright (c) 2007, 2016 Symbian Software Limited and others.
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
 * Christian Walther (walther@indel.ch) [333537] - Macro expansion in conditional process groups
 *******************************************************************************/
package org.eclipse.cdt.core.templateengine.process;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.templateengine.TemplateCore;
import org.eclipse.cdt.core.templateengine.TemplateDescriptor;
import org.eclipse.cdt.core.templateengine.TemplateEngine;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.w3c.dom.Element;

/**
 * ConditionalProcess encloses an &lt;if condition="..."&gt;&lt;/if&gt; block of the template.
 * The currently supported conditions are equals and not equals operations performed on two
 * Strings. The respective operators are == and !=. Any spaces will be treated as part of the
 * operands. The two operands will be evaluated for simple String equals and not equals after
 * performing a single pass  replace of any replace markers with their values in the template's
 * value store.
 */
public class ConditionalProcessGroup {

	private TemplateCore template;
	private Set<String> macros;
	private String conditionString;
	private String lValue;
	private String rValue;
	private Operator operator;
	private List<Process> processes;
	private String id;

	/**
	 * @author   BalaT
	 */
	private static class Operator {
		final static Operator EQUALS = new Operator("="); //$NON-NLS-1$
		final static Operator NOT_EQUALS = new Operator("!="); //$NON-NLS-1$

		String id;

		Operator(String id) {
			this.id = id;
		}

		@Override
		public boolean equals(Object arg0) {
			if (arg0 instanceof Operator) {
				return id.equals(((Operator) arg0).id);
			}
			return false;
		}
	}

	/**
	 * Constructs a ConditionalProcess element from the supplied conditionElement (&lt;if&gt;) while building Process
	 * objects out of each of the element's &lt;process&gt; children.
	 */
	public ConditionalProcessGroup(TemplateCore template, Element conditionElement, int id) {
		this.id = "Condition " + id; //$NON-NLS-1$
		conditionString = conditionElement.getAttribute(ProcessHelper.CONDITION);
		if (conditionString != null) {
			if (conditionString.trim().isEmpty()) {
				conditionString = null;
			} else {
				int op = conditionString.indexOf(ProcessHelper.EQUALS);
				if (op != -1) {
					this.operator = Operator.EQUALS;
					lValue = conditionString.substring(0, op);
					rValue = conditionString.substring(op + ProcessHelper.EQUALS.length());
				} else {
					op = conditionString.indexOf(ProcessHelper.NOT_EQUALS);
					if (op != -1) {
						this.operator = Operator.NOT_EQUALS;
						lValue = conditionString.substring(0, op);
						rValue = conditionString.substring(op + ProcessHelper.NOT_EQUALS.length());
					} //else an unsupported operation where this condition is ignored.
				}
				collectMacros(lValue);
				collectMacros(rValue);
			}
		}
		createProcessObjects(template,
				TemplateEngine.getChildrenOfElementByTag(conditionElement, TemplateDescriptor.PROCESS));
	}

	/**
	 * Adds values passed as parameter to the macros object
	 * @param value
	 */
	private void collectMacros(String value) {
		if (value != null) {
			if (macros == null) {
				macros = new HashSet<>();
			}
			macros.addAll(ProcessHelper.getReplaceKeys(value));
		}
	}

	/**
	 * Constructs a ConditionalProcess element from the supplied process elements while building Process
	 * objects out of each of the supplied process elements (&lt;process&gt;). The condition in this case is evaluated to true.
	 *
	 * This Constructor is expected to be used to evaluate all those process elements that are children of the template root element.
	 */
	public ConditionalProcessGroup(TemplateCore template, Element[] processElements) {
		id = "No Condition"; //$NON-NLS-1$
		createProcessObjects(template, Arrays.asList(processElements));
	}

	/**
	 * Creates the Process from the process Elements.
	 * @param templateCore
	 * @param processElements
	 */
	private void createProcessObjects(TemplateCore templateCore, List<Element> processElements) {
		this.template = templateCore;
		this.processes = new ArrayList<>(processElements.size());
		for (int j = 0, l = processElements.size(); j < l; j++) {
			Element processElem = processElements.get(j);
			if (processElem.getNodeName().equals(TemplateDescriptor.PROCESS)) {
				String processId = id + "--> Process " + (j + 1) + " (" + processElem.getAttribute(Process.ELEM_TYPE) //$NON-NLS-1$//$NON-NLS-2$
						+ ")"; //$NON-NLS-1$
				processes.add(new Process(templateCore, processElem, processId));
			}
		}
	}

	/**
	 * Checks if this conditional process group is completely ready to be processed.
	 */
	public boolean isReadyToProcess() {
		return areMacrosForConditionEvaluationExpandable() && isConditionValueTrue() && areProcessesReady();
	}

	/**
	 *
	 * @return boolean, as true if the Processes are ready to process
	 */
	private boolean areProcessesReady() {
		for (Process process : processes) {
			if (!process.isReadyToProcess()) {
				return false;
			}
		}
		return true;
	}

	/**
	 *
	 * @return boolean, true if Macros For Condition Evaluation Expandable.
	 */
	private boolean areMacrosForConditionEvaluationExpandable() {
		if (macros != null) {
			Map<String, String> valueStore = template.getValueStore();
			for (String value : macros) {
				if (valueStore.get(value) == null) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 *
	 * @return boolean, true if Condition Value is True.
	 */
	public boolean isConditionValueTrue() {
		if (conditionString == null) {
			return true;
		}
		if (!areMacrosForConditionEvaluationExpandable()) {
			return false;
		}
		Map<String, String> valueStore = template.getValueStore();
		String processedLValue = ProcessHelper.getValueAfterExpandingMacros(lValue, macros, valueStore);
		String processedRValue = ProcessHelper.getValueAfterExpandingMacros(rValue, macros, valueStore);
		if (operator.equals(Operator.EQUALS)) {
			return processedLValue.equals(processedRValue);
		} else if (operator.equals(Operator.NOT_EQUALS)) {
			return !processedLValue.equals(processedRValue);
		} else {
			return false;
		}
	}

	/**
	 * Process and Returns the Status of the prosses as a List.
	 * @param monitor
	 * @return List contains the IStatus.
	 * @throws ProcessFailureException
	 */
	public List<IStatus> process(IProgressMonitor monitor) throws ProcessFailureException {
		if (!areMacrosForConditionEvaluationExpandable()) {
			throw new ProcessFailureException(getUnexpandableMacroMessage());
		}
		if (!isConditionValueTrue()) {
			List<IStatus> statuses = new ArrayList<>(1);
			statuses.add(new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, IStatus.INFO,
					Messages.getString("ConditionalProcessGroup.notExecuting") + id, null)); //$NON-NLS-1$
			return statuses;
		}
		List<IStatus> statuses = new ArrayList<>(processes.size());
		for (Process process : processes) {
			try {
				statuses.add(process.process(monitor));
			} catch (ProcessFailureException e) {
				throw new ProcessFailureException(e.getMessage(), e, statuses);
			}
		}
		return statuses;
	}

	/**
	 * Return the Unexpandable Macro Message
	 * @return
	 */
	private String getUnexpandableMacroMessage() {
		if (macros != null) {
			Map<String, String> valueStore = template.getValueStore();
			for (String value : macros) {
				if (valueStore.get(value) == null) {
					return Messages.getString("ConditionalProcessGroup.unexpandableMacro") + value; //$NON-NLS-1$
				}
			}
		}
		return null;
	}

	/**
	 * Returns the Macros as a Set.
	 * @return   Set, contains macros
	 */
	public Set<String> getMacros() {
		return macros;
	}

	/**
	 * @return the union of all macros used in the child processes
	 */
	public Set<String> getAllMacros() {
		Set<String> set = null;
		if (macros != null) {
			set = new HashSet<>();
			set.addAll(macros);
		}
		for (Process process : processes) {
			Set<String> subSet = process.getMacros();
			if (subSet != null) {
				if (set == null) {
					set = new HashSet<>();
				}
				set.addAll(subSet);
			}
		}
		return set;
	}
}
