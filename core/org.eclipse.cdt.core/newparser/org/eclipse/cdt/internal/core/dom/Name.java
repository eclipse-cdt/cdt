package org.eclipse.cdt.internal.core.dom;

import java.util.LinkedList;
import java.util.List;

/**
 * @author dschaefe
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class Name {

	private List names = new LinkedList();
	
	public void addName(String name) {
		names.add(name);
	}
	
	public List getNames() {
		return names;
	}
	
}
