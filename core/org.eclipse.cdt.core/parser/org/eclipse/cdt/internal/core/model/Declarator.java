package org.eclipse.cdt.internal.core.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.internal.core.parser.util.Name;

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
	private boolean isConst = false;
	private boolean isVolatile = false;
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

	private List pointerOperators = new ArrayList(); 
	
	/**
	 * @return List
	 */
	public List getPointerOperators() {
		return pointerOperators;
	}
	
	public void addPointerOperator( PointerOperator po )
	{
		pointerOperators.add( po );
	}

	/**
	 * @return boolean
	 */
	public boolean isConst() {
		return isConst;
	}

	/**
	 * Sets the isConst.
	 * @param isConst The isConst to set
	 */
	public void setConst(boolean isConst) {
		this.isConst = isConst;
	}

	/**
	 * @return boolean
	 */
	public boolean isVolatile() {
		return isVolatile;
	}

	/**
	 * Sets the isVolatile.
	 * @param isVolatile The isVolatile to set
	 */
	public void setVolatile(boolean isVolatile) {
		this.isVolatile = isVolatile;
	}

}
