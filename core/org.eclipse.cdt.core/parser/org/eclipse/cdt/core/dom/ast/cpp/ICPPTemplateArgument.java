/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;

/**
 * Models the value of a template parameter or for the argument of a template-id. 
 * Such a value can either be a type-value, or an integral value.
 * 
 * @since 5.1
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients. 
 */
public interface ICPPTemplateArgument {
	ICPPTemplateArgument[] EMPTY_ARGUMENTS = {};

	/**
	 * Returns whether this is an integral value, suitable for a template non-type parameter. 
	 */
	boolean isNonTypeValue();

	/**
	 * Returns whether this is a type value, suitable for either a template type or a 
	 * template template parameter. 
	 */
	boolean isTypeValue();

	/**
	 * If this is a type value (suitable for a template type and template template parameters), 
	 * the type used as a value is returned. 
	 * For non-type values, <code>null</code> is returned. 
	 */
	IType getTypeValue();
	
	/**
	 * If this is a non-type value (suitable for a template non-type parameters), 
	 * the evaluation object is returned. 
	 * For type values, <code>null</code> is returned. 
	 * @noreference This method is not intended to be referenced by clients.
	 */
	ICPPEvaluation getNonTypeEvaluation();
	
	/**
	 * If this is a non-type value (suitable for a template non-type parameters), 
	 * the value is returned. 
	 * For type values, <code>null</code> is returned. 
	 */
	IValue getNonTypeValue();

	/**
	 * If this is a non-type value (suitable for a template non-type parameter),
	 * the type of the value is returned.
	 * For type values, <code>null</code> is returned. 
	 */
	IType getTypeOfNonTypeValue();

	/**
	 * Checks whether two arguments denote the same value.
	 */
	boolean isSameValue(ICPPTemplateArgument arg);
	
	/**
	 * Returns whether this template argument is a pack expansion or not.
	 * @since 5.2
	 */
	boolean isPackExpansion();

	/**
	 * Returns the expansion pattern, if this is a pack expansion, or <code>null</code> otherwise.
	 * @since 5.2
	 */
	ICPPTemplateArgument getExpansionPattern();
}
