package org.eclipse.cdt.internal.core.model;

import java.util.List;

import org.eclipse.cdt.internal.core.newparser.util.Name;

/**
 * @author jcamelon
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class Declarator {
	
	private Name name;
	
	/**
	 * Returns the name.
	 * @return Name
	 */
	public Name getName() {
		return name;
	}

	/**
	 * Sets the name.
	 * @param name The name to set
	 */
	public void setName(Name name) {
		this.name = name;
	}
	
	private List parameterDeclarationClause = null; 

	/**
	 * Returns the parameterDeclarationClause.
	 * @return List
	 */
	public List getParameterDeclarationClause() {
		return parameterDeclarationClause;
	}

	/**
	 * Sets the parameterDeclarationClause.
	 * @param parameterDeclarationClause The parameterDeclarationClause to set
	 */
	public void setParameterDeclarationClause(List parameterDeclarationClause) {
		this.parameterDeclarationClause = parameterDeclarationClause;
	}
}
