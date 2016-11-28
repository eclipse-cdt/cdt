/*******************************************************************************
 * Copyright (c) 2016, 2017 Kichwa Coders Ltd (https://kichwacoders.com/) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.linkerscript.validation;

import java.util.Map;
import java.util.Optional;

import org.eclipse.cdt.linkerscript.linkerScript.LExpression;
import org.eclipse.cdt.linkerscript.linkerScript.LNumberLiteral;

import com.google.inject.ImplementedBy;

@ImplementedBy(LExpressionReducerImpl.class)
public interface LExpressionReducer {

	/**
	 * Return modifiable map of known memory origins
	 */
	Map<String, Long> getMemoryOriginMap();

	/**
	 * Return modifiable map of known memory sizes
	 */
	Map<String, Long> getMemoryLengthMap();

	/**
	 * Return modifiable map of known variable values
	 */
	Map<String, Long> getVariableValuesMap();

	/**
	 * Reduce input to a simpler version. If result is constant the returned
	 * type will be of {@link LNumberLiteral}
	 *
	 * @param exp
	 *            expression to reduce
	 * @return reduced
	 */
	LExpression reduce(LExpression exp);

	/**
	 * Reduce input to a constant.
	 *
	 * @param exp
	 *            expression to reduce
	 * @return reduced constant if present
	 */
	Optional<Long> reduceToLong(LExpression exp);
}
