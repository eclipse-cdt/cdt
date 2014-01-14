/*
 * Copyright (c) 2014 BlackBerry Limited and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.cdt.core;

import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionConverter;
import org.eclipse.core.expressions.ExpressionTagNames;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;

/**
 * The purpose of ProblemMarkerFilterDesc is to manage information about 
 * one instance ProblemMarkerFilter extension point.
 */
class ProblemMarkerFilterDesc  {

	/**
	 * XML attribute for name of class that implements this extension point
	 */
	private static final String ATTR_CLASS = "class"; //$NON-NLS-1$

	/**
	 * Configuration element for this extension point
	 */
	private final IConfigurationElement element;

	/**
	 * Expression that allows conditionally enable/disable extension point
	 */
	private final Expression enablementExpression;

	/**
	 * Status of this extension point.
	 * False = disabled
	 * True = enabled
	 * null = not decided yet
	 */
	private Boolean fStatus = null;

	/**
	 * Unique id of this extension point
	 */
	private String id;

	/**
	 * 
	 */
	private IProblemMarkerFilter filter = NULL_FILTER;

	/**
	 * Filter that accept any marker.
	 * 
	 */
	private static final IProblemMarkerFilter NULL_FILTER = new IProblemMarkerFilter() {

		@Override
		public boolean acceptMarker(ProblemMarkerInfo markerInfo) {
			return true;
		}

	};

	/**
	 * Constructor
	 * 
	 * @param element configuration element with optional enablementExpression
	 */
	ProblemMarkerFilterDesc(IConfigurationElement element) {
		this.element = element;

		Expression expr = null;
		IConfigurationElement[] children = element.getChildren(ExpressionTagNames.ENABLEMENT);
		switch (children.length) {
		case 0:
			fStatus = Boolean.TRUE;
			break;
		case 1:
			try {
				ExpressionConverter parser = ExpressionConverter.getDefault();
				expr = parser.perform(children[0]);
			} catch (CoreException e) {
				CCorePlugin.log("Error in enablement expression of " + id, e); //$NON-NLS-1$
			}
			break;
		default:
			CCorePlugin.log("Too many enablement expressions for " + id); //$NON-NLS-1$
			fStatus = Boolean.FALSE;
			break;
		}
		enablementExpression = expr;
	}

	/**
	 * Evaluate enablement expression and set fStatus member
	 * @return value of enablement expression
	 */
	private boolean matches() {
		// If the enablement expression is missing or structurally invalid, then return immediately
		if (fStatus != null)
			return fStatus.booleanValue();

		if (enablementExpression != null)
			try {
				EvaluationContext evalContext = new EvaluationContext(null, null);
				return enablementExpression.evaluate(evalContext) == EvaluationResult.TRUE;
			} catch (CoreException e) {
				CCorePlugin.log("Error while evaluating enablement expression for " + id, e); //$NON-NLS-1$
			}

		fStatus = Boolean.FALSE;
		return false;
	}

	/**
	 * Create instance of extension point class and set @link{ProblemMarkerFilterDesc#filter} member
	 * @return new filter instance or NULL_FILER if filter could not be instantiated
	 */
	private IProblemMarkerFilter createFilter() {
		if (filter == null)
			synchronized (this) {
				if (filter == null) {
					try {
						filter = (IProblemMarkerFilter) element.createExecutableExtension(ATTR_CLASS);
					} catch (CoreException e) {
						String id = element.getDeclaringExtension().getNamespaceIdentifier() + '.'
								+ element.getDeclaringExtension().getSimpleIdentifier();
						CCorePlugin.log("Error in class attribute of " + id, e); //$NON-NLS-1$

						// mark the filter with an empty implementation to prevent future load attempts
						filter = NULL_FILTER;
					}
				}
			}

		return filter;
	}

	/**
	 * Return filter interface
	 * @return Filter interface or NULL_FILER if filter could not be created
	 */
	IProblemMarkerFilter getFilter() {
		return (matches() ? createFilter() : NULL_FILTER);
	}

}
