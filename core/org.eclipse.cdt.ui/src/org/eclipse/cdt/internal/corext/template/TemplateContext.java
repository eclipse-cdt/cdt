package org.eclipse.cdt.internal.corext.template;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.CoreException;

/**
 * A template context. A template context is associated with a context type.
 */
public abstract class TemplateContext {

	/** context type of this context */
	private final ContextType fContextType;

	/**
	 * Creates a template context of a particular context type.
	 */
	protected TemplateContext(ContextType contextType) {
		fContextType= contextType;
	}

	/**
	 * Returns the context type of this context.
	 */
	public ContextType getContextType() {
	 	return fContextType;   
	}

	/**
	 * Evaluates the template and returns a template buffer.
	 */
	public abstract TemplateBuffer evaluate(Template template) throws CoreException;
	
	/**
	 * Tests if the specified template can be evaluated in this context.
	 */
	public abstract boolean canEvaluate(Template template);
	
}
