package org.eclipse.cdt.internal.core.dom;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


/**
 * @author jcamelon
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class ParameterDeclarationClause implements IScope {

	/**
	 * @see org.eclipse.cdt.core.dom.IScope#addDeclaration(org.eclipse.cdt.internal.core.dom.Declaration)
	 */
	public void addDeclaration( Declaration declaration) {
		declarations.add( declaration ); 
	}

	/**
	 * @see org.eclipse.cdt.core.dom.IScope#getDeclarations()
	 */
	public List getDeclarations() {
		return Collections.unmodifiableList( declarations );
	}

	private List declarations = new LinkedList();
	private Declarator owner; 
	
	ParameterDeclarationClause( Declarator owner )
	{
		this.owner = owner; 
		this.owner.addParms( this ); 
	}
}
