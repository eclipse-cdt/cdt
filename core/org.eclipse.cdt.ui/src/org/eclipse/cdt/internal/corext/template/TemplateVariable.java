package org.eclipse.cdt.internal.corext.template;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * A template variable.
 */
public abstract class TemplateVariable {

	/** name of the variable */
	private final String fName;

	/** description of the variable */
	private final String fDescription;
	
	/**
	 * Creates an instance of <code>TemplateVariable</code>.
	 * 
	 * @param name the name of the variable
	 * @param description the description for the variable
	 */
	protected TemplateVariable(String name, String description) {
	 	fName= name;
	 	fDescription= description;   
	}
	
	/**
	 * Returns the name of the variable.
	 */
	public String getName() {
		return fName;
	}

	/**
	 * Returns the description for the variable.
	 */
	public String getDescription() {
		return fDescription;   
	}

	/**
	 * Tests if this variable can be evaluated.
	 */
//	public abstract boolean canEvaluate(TemplateContext context);
	
	/**
	 * Evaluates this variable
	 * 
	 * @return the evaluated string, or <code>null</code> if not evaluatable.
	 */
	public abstract String evaluate(TemplateContext context);

	/**
	 * Returns whether this variable is resolved.
	 * By default, the variable is not resolved.
	 * Clients can overwrite this method to force resolution of the variable.
	 */
	public boolean isResolved(TemplateContext context) {
		return false;
	}

}
