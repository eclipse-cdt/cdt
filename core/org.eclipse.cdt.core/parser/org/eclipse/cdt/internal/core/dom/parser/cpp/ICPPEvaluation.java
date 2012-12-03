/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.internal.core.dom.parser.ISerializableEvaluation;

/**
 * Assists in evaluating expressions.
 */
public interface ICPPEvaluation extends ISerializableEvaluation {
	boolean isInitializerList();
	boolean isFunctionSet();

	/**
	 * Returns {@code true} if the type of the expression depends on template parameters.
	 */
	boolean isTypeDependent();

	/**
	 * Returns {@code true} if the value of the expression depends on template parameters.
	 */
	boolean isValueDependent();

	/**
	 * TODO Add description
	 *
	 * @param point determines the scope for name lookups
	 */
	IType getTypeOrFunctionSet(IASTNode point);

	/**
	 * TODO Add description
	 *
	 * @param point determines the scope for name lookups
	 */
	IValue getValue(IASTNode point);

	/**
	 * TODO Add description
	 *
	 * @param point determines the scope for name lookups
	 */
	ValueCategory getValueCategory(IASTNode point);

	/**
	 * Returns a signature uniquely identifying the evaluation. Two evaluations with identical
	 * signatures are guaranteed to produce the same results.
	 */
	char[] getSignature();

	/**
	 * Instantiates the evaluation with the provided template parameter map and pack offset.
	 * The context is used to replace templates with their specialization, where appropriate.
	 * @return a fully or partially instantiated evaluation, or the original evaluation
	 */
	ICPPEvaluation instantiate(ICPPTemplateParameterMap tpMap, int packOffset,
			ICPPClassSpecialization within, int maxdepth, IASTNode point);

	/**
	 * Determines size of the template parameter pack.
	 *
	 * @noreference This method is not intended to be referenced by clients. 
	 */
	int determinePackSize(ICPPTemplateParameterMap tpMap);

	/**
	 * Checks if the evaluation references a template parameter either directly or though nested
	 * evaluations. 
	 */
	boolean referencesTemplateParameter();
}
