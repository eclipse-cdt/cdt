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
import java.util.HashSet;
import java.util.Iterator;
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
	private List/*<ConditionalProcessGroup>*/ conditionalProcessGroupList;

	public TemplateProcessHandler(TemplateCore template) {
		this.template = template;
		initialize();
	}

	/**
	 * initializes the template descriptor and Root Elements.
	 *
	 */
	private void initialize() {
		TemplateDescriptor desc = template.getTemplateDescriptor();
		Element root = desc.getRootElement();
		conditionalProcessGroupList =  new ArrayList/*<ConditionalProcessGroup>*/();
		List/*<Element>*/ nodeList = TemplateEngine.getChildrenOfElementByTag(root, TemplateDescriptor.IF);
		for (int j = 0, l = nodeList.size(); j < l; j++) {
			conditionalProcessGroupList.add(new ConditionalProcessGroup(template, (Element) nodeList.get(j), j + 1));
		}
		//Collect all free-hanging processes in one ConditionalProcessGroup object with condition true.
		nodeList = TemplateEngine.getChildrenOfElementByTag(root, TemplateDescriptor.PROCESS);
		conditionalProcessGroupList.add(new ConditionalProcessGroup(template, (Element[]) nodeList.toArray(new Element[nodeList.size()])));
	}
	
	/**
	 * 
	 * @param monitor 
	 * @return IStatus, as an array of status info
	 * @throws ProcessFailureException
	 */
	public IStatus[] processAll(IProgressMonitor monitor) throws ProcessFailureException {
		List/*<IStatus>*/ allStatuses = new ArrayList/*<IStatus>*/();
		for (Iterator i = conditionalProcessGroupList.iterator(); i.hasNext();) {
			try {
				allStatuses.addAll(((ConditionalProcessGroup)i.next()).process(monitor));
			} catch (ProcessFailureException e) {
				throw new ProcessFailureException(e.getMessage(), e, allStatuses);
			}
		}
		return (IStatus[]) allStatuses.toArray(new IStatus[allStatuses.size()]);
	}

	/**
	 * Returns all macros
	 * @return
	 */
	public Set/*<String>*/ getAllMacros() {
		Set/*<String>*/ set = new HashSet/*<String>*/();
		for (Iterator i = conditionalProcessGroupList.iterator(); i.hasNext();) {
			Set/*<String>*/ subSet = ((ConditionalProcessGroup)i.next()).getAllMacros();
			if (subSet != null) {
				set.addAll(subSet);
			}
		}
		return set;
	}
}
