/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Doug Schaefer (IBM) - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * Represents a parameter to a function. The scope of the parameter is
 * the function that declared this parameter.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IParameter extends IVariable {
	public static final IParameter [] EMPTY_PARAMETER_ARRAY = new IParameter[0];
	
	/**
	 * Inherited from {@link IVariable}, always returns <code>null</code>.
	 * @since 5.1
	 */
	@Override
	IValue getInitialValue();
}
