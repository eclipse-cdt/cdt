package org.eclipse.cdt.internal.core.dom;

import java.util.LinkedList;
import java.util.List;

/**
 */
public class TranslationUnit {

	private List declarations = new LinkedList();
	
	public void addDeclaration(Declaration declaration) {
		declarations.add(declaration);
	}

}
