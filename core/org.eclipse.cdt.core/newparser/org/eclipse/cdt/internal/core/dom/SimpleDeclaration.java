package org.eclipse.cdt.internal.core.dom;

import java.util.LinkedList;
import java.util.List;

/**
 */
public class SimpleDeclaration extends Declaration {

	private DeclSpecifierSeq declSpecifierSeq;
	
	public void setDeclSpecifierSequence(DeclSpecifierSeq declSpecifierSeq) {
		this.declSpecifierSeq = declSpecifierSeq;
	}
	
	public DeclSpecifierSeq getDeclSpecifierSeq() {
		return declSpecifierSeq;
	}
	
	private List declarators = new LinkedList();
	
	public void addDeclarator(Declarator declarator) {
		declarators.add(declarator);
	}

	public List getDeclarators() {
		return declarators;
	}
	
}
