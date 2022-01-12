/*******************************************************************************
 * Copyright (c) 2007, 2008 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.help;

import java.util.ArrayList;

import org.eclipse.cdt.ui.IFunctionSummary;
import org.eclipse.help.IHelpResource;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class CHelpEntry {
	private static final String ATTR_KEYWD = "keyword"; //$NON-NLS-1$
	private static final String NODE_TOPIC = "topic"; //$NON-NLS-1$
	private static final String NODE_FSUMM = "functionSummary"; //$NON-NLS-1$

	private String keyword = null;
	private boolean isValid = true;
	private CHelpTopic[] hts = null;
	private CFunctionSummary[] fss = null;

	public CHelpEntry(Element e) {
		keyword = e.getAttribute(ATTR_KEYWD).trim();
		ArrayList<CFunctionSummary> obs1 = new ArrayList<>();
		ArrayList<CHelpTopic> obs2 = new ArrayList<>();
		NodeList list = e.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			Node node = list.item(i);
			if (node.getNodeType() != Node.ELEMENT_NODE)
				continue;
			if (NODE_FSUMM.equals(node.getNodeName())) {
				obs1.add(new CFunctionSummary((Element) node, keyword));
			} else if (NODE_TOPIC.equals(node.getNodeName())) {
				obs2.add(new CHelpTopic((Element) node, keyword));
			}
		}
		fss = obs1.toArray(new CFunctionSummary[obs1.size()]);
		hts = obs2.toArray(new CHelpTopic[obs2.size()]);

		if (fss.length == 0 && hts.length == 0)
			isValid = false; // nothing to display
	}

	/**
	 * Returns true if help entry is correct
	 * Returns false if entry is empty or when
	 *   subsequent processing failed somehow.
	 * @return entry state
	 */
	public boolean isValid() {
		return isValid;
	}

	public String getKeyword() {
		return keyword;
	}

	public IFunctionSummary[] getFunctionSummary() {
		return fss;
	}

	public IHelpResource[] getHelpResource() {
		return hts;
	}

	@Override
	public String toString() {
		return "<entry keyword=\"" + keyword + "\">"; //$NON-NLS-1$ //$NON-NLS-2$
	}
}
