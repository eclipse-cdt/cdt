package org.eclipse.cdt.internal.core.dom;

import java.util.List;


/**
 * A scope contains a set of declarations that are defined in that 
scope.  */ public interface IScope {
	
	public void addDeclaration(Declaration declaration);
	public List getDeclarations();

}
