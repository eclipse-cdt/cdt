package org.eclipse.cdt.internal.core.dom;

import java.util.HashMap;
import java.util.Map;

/**
 */
public class DeclarativeRegion {

	private Map declarations = new HashMap();
	
	public void addDeclaration(String name, Object decl) {
		declarations.put(name, decl);
	}

	// This is the general get method
	public Object getDeclaration(String name) {
		Object decl = null;
		
		if ((decl = declarations.get(name)) != null)
			return decl;

		return null;	
	}
}
