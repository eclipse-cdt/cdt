/*******************************************************************************
 * Copyright (c) 2007, 2008 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.help;

import java.util.ArrayList;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.eclipse.cdt.ui.FunctionPrototypeSummary;
import org.eclipse.cdt.ui.IFunctionSummary;
import org.eclipse.cdt.ui.IRequiredInclude;

public class CFunctionSummary implements IFunctionSummary {
	private static final String ATTR_STD = "standard"; //$NON-NLS-1$
	private static final String NODE_NAME = "name"; //$NON-NLS-1$
	private static final String NODE_DESC = "description"; //$NON-NLS-1$
	private static final String NODE_INCL = "include"; //$NON-NLS-1$
	private static final String NODE_TYPE = "returnType"; //$NON-NLS-1$
	private static final String NODE_ARGS = "arguments"; //$NON-NLS-1$

	private static final String SP = " "; //$NON-NLS-1$
	private static final String LB = "("; //$NON-NLS-1$
	private static final String RB = ")"; //$NON-NLS-1$
	
	private String name = null;
	private String desc = null;
	private IRequiredInclude[] incs = null;
	IFunctionPrototypeSummary fps = null;
	
	public CFunctionSummary(Element e, String defName) {
		name = defName; // if there's no "name" tag, keyword used instead
		String args = null;
		String type = null;
		NodeList list = e.getChildNodes();
		ArrayList<IRequiredInclude> incList = new ArrayList<IRequiredInclude>();
		for(int j = 0; j < list.getLength(); j++){
			Node node = list.item(j);
			if(node.getNodeType() != Node.ELEMENT_NODE)	
				continue;
			String s = node.getNodeName().trim();
			String t = node.getFirstChild().getNodeValue().trim();
			if(NODE_NAME.equals(s)){
				name = t;
			} else if(NODE_DESC.equals(s)){
				desc = t;
			} else if(NODE_ARGS.equals(s)){
				args = t;
			} else if(NODE_TYPE.equals(s)){
				type = t;
			} else if(NODE_INCL.equals(s)){
				boolean std = true;
				if (((Element)node).hasAttribute(ATTR_STD)) {
					String st = ((Element)node).getAttribute(ATTR_STD);
					std = (st == null  || st.equalsIgnoreCase("true") //$NON-NLS-1$
							|| st.equalsIgnoreCase("yes"));           //$NON-NLS-1$
				}
				incList.add(new RequiredInclude(t, std));
			}
		}
		if (incList.size() > 0) 
			incs = incList.toArray(new IRequiredInclude[incList.size()]);
		fps = new FunctionPrototypeSummary(type + SP + name + LB + args + RB);	
	}
	
	@Override
	public String getDescription() {
		return desc;
	}
	@Override
	public IRequiredInclude[] getIncludes() { 
		return incs; 
	}
	@Override
	public String getName() {
		return name;
	}
	@Override
	public String getNamespace() {
		return null;
	}
	@Override
	public IFunctionPrototypeSummary getPrototype() {
		return fps;
	}
	
	/**
	 * This class implements IRequiredInclude interface 
	 */
	private class RequiredInclude implements IRequiredInclude {
		private String iname;
		private boolean std;
		
		private RequiredInclude(String s, boolean b) {
			iname = s;
			std = b;
		}
		@Override
		public String getIncludeName() {
			return iname;
		}
		@Override
		public boolean isStandard() {
			return std;
		}
		@Override
		public String toString() {
			if (std) 
				return "#include <" + iname + ">";   //$NON-NLS-1$ //$NON-NLS-2$
			return "#include \"" + iname + "\""; //$NON-NLS-1$ //$NON-NLS-2$
		}		
	}
	@Override
	public String toString() {
		return "<functionSummary> : " + getPrototype().getPrototypeString(false); //$NON-NLS-1$
	}

}
