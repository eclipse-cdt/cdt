package org.eclipse.cdt.internal.corext.template;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * A simple template variable, which always evaluates to a defined string.
 */
public class SimpleTemplateVariable extends TemplateVariable {

	/** The string to which this variable evaluates. */
	private String fEvaluationString;
	/** A flag indicating if this variable can be resolved. */
	private boolean fResolved;

	/*
	 * @see TemplateVariable#TemplateVariable(String, String)
	 */
	protected SimpleTemplateVariable(String name, String description) {
		super(name, description);
	}

	/**
	 * Sets the string to which this variable evaluates.
	 * 
	 * @param evaluationString the evaluation string, may be <code>null</code>.
	 */
	public final void setEvaluationString(String evaluationString) {
		fEvaluationString= evaluationString;	
	}

	/*
	 * @see TemplateVariable#evaluate(TemplateContext)
	 */
	public String evaluate(TemplateContext context) {
		return fEvaluationString;
	}

	/**
	 * Sets the resolved flag.
	 */
	public final void setResolved(boolean resolved) {
		fResolved= resolved;
	}

	/*
	 * @see TemplateVariable#isResolved(TemplateContext)
	 */
	public boolean isResolved(TemplateContext context) {
		return fResolved;
	}

}
