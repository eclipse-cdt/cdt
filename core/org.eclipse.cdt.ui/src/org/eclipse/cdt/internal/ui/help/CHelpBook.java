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
import java.util.Collection;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.help.IHelpResource;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.eclipse.cdt.ui.ICHelpBook;
import org.eclipse.cdt.ui.ICHelpResourceDescriptor;
import org.eclipse.cdt.ui.IFunctionSummary;
import org.eclipse.cdt.ui.text.ICHelpInvocationContext;

public class CHelpBook implements ICHelpBook {
	private static final String ATTR_TITLE = "title"; //$NON-NLS-1$
	private static final String ATTR_BTYPE = "type";  //$NON-NLS-1$
	private static final String NODE_ENTRY = "entry"; //$NON-NLS-1$
	private static final String TYPE_C   = "HELP_TYPE_C"; //$NON-NLS-1$
	private static final String TYPE_CPP = "HELP_TYPE_CPP"; //$NON-NLS-1$
	private static final String TYPE_ASM = "HELP_TYPE_ASM"; //$NON-NLS-1$
	private static final int DEFAULT_VAL  = ICHelpBook.HELP_TYPE_C;

	private int type;
	private String title;
	private TreeMap<String, CHelpEntry> entries; 
	
	public CHelpBook(Element e) {
		entries = new TreeMap<String, CHelpEntry>();

		if (e.hasAttribute(ATTR_TITLE))
			title = e.getAttribute(ATTR_TITLE).trim();
		if (e.hasAttribute(ATTR_BTYPE)) {
			String s = e.getAttribute(ATTR_BTYPE);
			try {
				type = Integer.parseInt(s);
				if (type < DEFAULT_VAL ||
					type > ICHelpBook.HELP_TYPE_ASM)
					type = DEFAULT_VAL;
			} catch (NumberFormatException ee) {
				if (TYPE_C.equalsIgnoreCase(s))
					type = ICHelpBook.HELP_TYPE_C;
				else if (TYPE_CPP.equalsIgnoreCase(s))
					type = ICHelpBook.HELP_TYPE_CPP;
				else if (TYPE_ASM.equalsIgnoreCase(s))
					type = ICHelpBook.HELP_TYPE_ASM;
				else
					type = DEFAULT_VAL;
			}
		}
		NodeList list = e.getChildNodes();
		for(int i = 0; i < list.getLength(); i++){
			Node node = list.item(i);
			if(node.getNodeType() != Node.ELEMENT_NODE)	continue;
			if(NODE_ENTRY.equals(node.getNodeName())) {
				CHelpEntry he = new CHelpEntry((Element)node);
				if (he.isValid()) 
					entries.put(he.getKeyword(), he);
			}
		}
	}
	
	@Override
	public int getCHelpType() {
		return type;
	}
	
	@Override
	public String getTitle() {
		return title;
	}
	
	public IFunctionSummary getFunctionInfo(
			ICHelpInvocationContext context,
			String name) {
		
		if (entries.containsKey(name)) {
			CHelpEntry he = entries.get(name);
			IFunctionSummary[] fs = he.getFunctionSummary();
			if (fs != null && fs.length > 0)
				return fs[0]; 
		}
		return null;
	}
	
	/**
	 * Temporary implementation with slow search
	 * @param context
	 * @param prefix
	 * @return matching functions
	 */
	public List<IFunctionSummary> getMatchingFunctions(
			ICHelpInvocationContext context,
			String prefix) {
		
		Collection<CHelpEntry> col = null;
		if (prefix == null || prefix.trim().length() == 0) {
			// return whole data
			col = entries.values();
		} else {
			String pr1 = prefix.trim();
			byte[] bs = pr1.getBytes();
			int i = bs.length - 1;
			while (i >= 0) {
			   byte b = bs[i];
			   if (++b > bs[i]) { // no overflow
				   bs[i] = b;
				   break;
			   }
			   i--;
			}
			SortedMap<String, CHelpEntry> sm = (i>-1) ?
				entries.subMap(pr1, new String(bs)) :
				entries.tailMap(pr1);
			col = sm.values();	
		}
				
		if (col.size() > 0) {
			ArrayList<IFunctionSummary> out = new ArrayList<IFunctionSummary>(col.size());
			for (CHelpEntry he: col) 
				for (IFunctionSummary fs : he.getFunctionSummary())
					out.add(fs);
			return out;
		}
		return null;
	}

	public ICHelpResourceDescriptor getHelpResources(
			ICHelpInvocationContext context, String name) {
		if (entries.containsKey(name)) {
			CHelpEntry he = entries.get(name);
			IHelpResource[] hr = he.getHelpResource();
			if (hr != null && hr.length > 0)
				return new HRDescriptor(this, hr); 
		}
		return null;
	}
	
	private class HRDescriptor implements ICHelpResourceDescriptor {
		private ICHelpBook book;
		private IHelpResource[] res;
		HRDescriptor(ICHelpBook _book, IHelpResource[] _res) {
			book = _book;
			res  = _res;
		}
		@Override
		public ICHelpBook getCHelpBook() {
			return book;
		}
		@Override
		public IHelpResource[] getHelpResources() {
			return res;
		}
	}

	@Override
	public String toString() {
		return "<helpBook title=\"" +title +"\" type=\"" + type + "\">"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
	
}
