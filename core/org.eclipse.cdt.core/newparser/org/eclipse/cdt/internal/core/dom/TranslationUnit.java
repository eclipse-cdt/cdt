package org.eclipse.cdt.internal.core.dom;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.core.dom.IScope;

/**
 */
public class TranslationUnit implements IScope {

	private List declarations = new LinkedList();
	
	public void addDeclaration(Declaration declaration) {
		declarations.add(declaration);
	}

	public List getDeclarations() {
		return declarations;
	}
}
