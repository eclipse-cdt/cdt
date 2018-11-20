/*******************************************************************************
 * Copyright (c) 2007, 2008 Symbian Software Limited and others.
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.core.templateengine.TemplateCore;
import org.eclipse.cdt.core.templateengine.TemplateDescriptor;
import org.eclipse.cdt.core.templateengine.TemplateEngine;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.w3c.dom.Element;

/**
 * Class handles the Template processes
 */
public class TemplateProcessHandler {

	private TemplateCore template;
	private List<ConditionalProcessGroup> conditionalProcessGroupList;

	public TemplateProcessHandler(TemplateCore template) {
		this.template = template;
		initialize();
	}

	/**
	 * Initialises the template descriptor and Root Elements.
	 */
	private void initialize() {
		TemplateDescriptor desc = template.getTemplateDescriptor();
		Element root = desc.getRootElement();
		conditionalProcessGroupList = new ArrayList<>();
		List<Element> nodeList = TemplateEngine.getChildrenOfElementByTag(root, TemplateDescriptor.IF);
		for (int j = 0, l = nodeList.size(); j < l; j++) {
			conditionalProcessGroupList.add(new ConditionalProcessGroup(template, nodeList.get(j), j + 1));
		}
		//Collect all free-hanging processes in one ConditionalProcessGroup object with condition true.
		nodeList = TemplateEngine.getChildrenOfElementByTag(root, TemplateDescriptor.PROCESS);
		conditionalProcessGroupList
				.add(new ConditionalProcessGroup(template, nodeList.toArray(new Element[nodeList.size()])));
	}

	/**
	 *
	 * @param monitor
	 * @return IStatus, as an array of status info
	 * @throws ProcessFailureException
	 */
	public IStatus[] processAll(IProgressMonitor monitor) throws ProcessFailureException {
		List<IStatus> allStatuses = new ArrayList<>();
		for (ConditionalProcessGroup cpg : conditionalProcessGroupList) {
			try {
				allStatuses.addAll(cpg.process(monitor));
			} catch (ProcessFailureException e) {
				throw new ProcessFailureException(e.getMessage(), e, allStatuses);
			}
		}
		return allStatuses.toArray(new IStatus[allStatuses.size()]);
	}

	/**
	 * @return the union of all macros used in this template's process groups
	 */
	public Set<String> getAllMacros() {
		Set<String> set = new HashSet<>();
		for (ConditionalProcessGroup cpg : conditionalProcessGroupList) {
			Set<String> subSet = cpg.getAllMacros();
			if (subSet != null) {
				set.addAll(subSet);
			}
		}
		return set;
	}
}
