/*******************************************************************************
 * Copyright (c) 2012, 2013 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Sergey Prigogin (Google)
 *     Nathan Ridge
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.internal.core.dom.parser.ISerializableEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPFunctionParameterMap;

/**
 * Assists in evaluating expressions.
 */
public interface ICPPEvaluation extends ISerializableEvaluation {
	public static final ICPPEvaluation[] EMPTY_ARRAY = {};

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
	 * Returns the type of the expression, or a {@code FunctionSetType} if the expression evaluates
	 * to a function set.
	 *
	 * @param point the point of instantiation, determines the scope for name lookups
	 */
	IType getTypeOrFunctionSet(IASTNode point);

	/**
	 * Returns the value of the expression.
	 *
	 * @param point the point of instantiation, determines the scope for name lookups
	 */
	IValue getValue(IASTNode point);

	/**
	 * Returns the category of the expression value.
	 * @see ValueCategory
	 *
	 * @param point the point of instantiation, determines the scope for name lookups
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
	 * Computes the evaluation produced by substituting function parameters by their values.
	 * 
	 * @param parameterMap maps function parameters to their values
	 * @param maxdepth allowed recursion depth 
	 * @param point the point of instantiation, determines the scope for name lookups
	 * @return the computed evaluation
	 */
	ICPPEvaluation computeForFunctionCall(CPPFunctionParameterMap parameterMap, int maxdepth,
			IASTNode point);

	/**
	 * Searches the evaluation for a usage of a template parameter which is a parameter pack,
	 * and returns the number of arguments bound to that parameter pack in the given
	 * template parameter map.
	 *
	 * Can also return one of the special values CPPTemplates.PACK_SIZE_DEFER,
	 * CPPTemplates.PACK_SIZE_FAIL, and CPPTemplates.PACK_SIZE_NOT_FOUND. See their
	 * declarations for their meanings.
	 *
	 * See also {@code CPPTemplates.determinePackSize()}.
	 */
	int determinePackSize(ICPPTemplateParameterMap tpMap);

	/**
	 * Checks if the evaluation references a template parameter either directly or though nested
	 * evaluations. 
	 */
	boolean referencesTemplateParameter();
	
	/**
	 * If the evaluation is dependent (or instantiated from a dependent
	 * evaluation), returns the template definition in which the
	 * evaluation occurs. Otherwise returns null. 
	 */
	IBinding getTemplateDefinition();
}
