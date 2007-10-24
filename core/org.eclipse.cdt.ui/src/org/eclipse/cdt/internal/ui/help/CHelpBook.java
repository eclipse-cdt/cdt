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

	private int type;
	private String title;
	private TreeMap entries; 
	
	public CHelpBook(Element e) {
		entries = new TreeMap();

		if (e.hasAttribute(ATTR_TITLE))
			title = e.getAttribute(ATTR_TITLE).trim();
		if (e.hasAttribute(ATTR_BTYPE)) {
			try {
				type = Integer.parseInt(e.getAttribute(ATTR_TITLE));
			} catch (NumberFormatException ee) {}
		}
		NodeList list = e.getChildNodes();
		for(int i = 0; i < list.getLength(); i++){
			Node node = list.item(i);
			if(node.getNodeType() != Node.ELEMENT_NODE)	continue;
			if(NODE_ENTRY.equals(node.getNodeName())) {
				CHelpEntry he = new CHelpEntry((Element)node);
				if (he.isValid()) 
					add(he.getKeyword(), he);
			}
		}
	}
	
	public int getCHelpType() {
		return type;
	}
	
	public String getTitle() {
		return title;
	}
	
	private void add(String keyword, Object entry) {
		entries.put(keyword, entry);
	}
	
	public IFunctionSummary getFunctionInfo(
			ICHelpInvocationContext context,
			String name) {
		
		if (entries.containsKey(name)) {
			CHelpEntry he = (CHelpEntry)entries.get(name);
			IFunctionSummary[] fs = he.getFunctionSummary();
			if (fs != null && fs.length > 0)
				return fs[0]; 
			// TODO: function summary selection
		}
		return null;
	}
	
	/**
	 * Temporary implementation with slow search
	 * @param context
	 * @param prefix
	 * @return matching functions
	 */
	public List getMatchingFunctions(
			ICHelpInvocationContext context,
			String prefix) {
		
		Collection col = null;
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
			   } else
				   i--;
			}
			SortedMap sm = (i>-1) ?
				entries.subMap(pr1, new String(bs)) :
				entries.tailMap(pr1);
			col = sm.values();	
		}
		
		if (col.size() > 0)
			return new ArrayList(col);
		else
			return null;
	}

	public ICHelpResourceDescriptor getHelpResources(
			ICHelpInvocationContext context, String name) {
		if (entries.containsKey(name)) {
			CHelpEntry he = (CHelpEntry)entries.get(name);
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
		public ICHelpBook getCHelpBook() {
			return book;
		}
		public IHelpResource[] getHelpResources() {
			return res;
		}
	}

	public String toString() {
		return "<helpBook title=\"" +title +"\" type=\"" + type + "\">"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
	
}
