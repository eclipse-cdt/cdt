package org.eclipse.cdt.internal.core.dom;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.internal.core.parser.util.DeclSpecifier;


/**
 * @author jcamelon
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class ParameterDeclaration extends Declaration implements DeclSpecifier.Container {

	DeclSpecifier declSpec = null; 

	/**
	 * @see org.eclipse.cdt.internal.core.dom.DeclarationSpecifier.CElementWrapper#getDeclSpecifier()
	 */
	public DeclSpecifier getDeclSpecifier() {
		if( declSpec == null )
			declSpec = new DeclSpecifier(); 
			
		return declSpec; 
	}

	/**
	 * @see org.eclipse.cdt.internal.core.dom.DeclarationSpecifier.CElementWrapper#setDeclSpecifier(org.eclipse.cdt.internal.core.dom.DeclarationSpecifier)
	 */
	public void setDeclSpecifier(DeclSpecifier in) {
		declSpec = in; 
	}
	private List declarators = new LinkedList();
	
	public void addDeclarator(Object declarator) {
		declarators.add(declarator);
	}

	public List getDeclarators() {
		return Collections.unmodifiableList( declarators );
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.util.DeclarationSpecifier.Container#removeDeclarator(java.lang.Object)
	 */
	public void removeDeclarator(Object declarator) {
		declarators.remove( declarator );
	}
}
