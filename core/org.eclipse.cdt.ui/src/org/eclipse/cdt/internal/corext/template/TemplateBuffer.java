package org.eclipse.cdt.internal.corext.template;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.util.Assert;


/**
 * A template buffer is a container for a string and variables.
 */
public final class TemplateBuffer {
	
	/** The string of the template buffer */
	private String fString;
	/** The variable positions of the template buffer */
	private TemplatePosition[] fVariables;
	
	/**
	 * Creates a template buffer.
	 * 
	 * @param string the string
	 * @param variables the variable positions
	 * @throws CoreException for illegal variable positions
	 */
    public TemplateBuffer(String string, TemplatePosition[] variables) throws CoreException {
		setContent(string, variables);
    }

	/**
	 * Sets the content of the template buffer.
	 * 
	 * @param string the string
	 * @param variables the variable positions
	 * @throws CoreException for illegal variable positions
	 */
	public final void setContent(String string, TemplatePosition[] variables) throws CoreException {
		Assert.isNotNull(string);
		Assert.isNotNull(variables);

		// XXX assert non-overlapping variable properties

		fString= string;
		fVariables= variables;
	}

	/**
	 * Returns the string of the template buffer.
	 */
	public final String getString() {
		return fString;
	}
	
	/**
	 * Returns the variable positions of the template buffer.
	 */
	public final TemplatePosition[] getVariables() {
		return fVariables;
	}

}
