package org.eclipse.cdt.internal.core.dom;

import java.util.HashMap;
import java.util.Map;

/**
 */
public class DeclarativeRegion {

	private Map declarations = new HashMap();
	
	public void addDeclaration(String name, Declaration decl) {
		declarations.put(name, decl);
	}

	// This is the general get method
	public Declaration getDeclaration(String name) {
		Declaration decl = null;
		
		if ((decl = (Declaration)declarations.get(name)) != null)
			return decl;

		return null;	
	}
}
