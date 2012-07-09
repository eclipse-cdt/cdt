/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.internal.core.dom.parser.ISerializableEvaluation;

/**
 * Assists in evaluating expressions.
 */
public interface ICPPEvaluation extends ISerializableEvaluation {
	boolean isInitializerList();
	boolean isFunctionSet();

	boolean isTypeDependent();
	boolean isValueDependent();
	
	IType getTypeOrFunctionSet(IASTNode point);
	IValue getValue(IASTNode point);
	ValueCategory getValueCategory(IASTNode point);
}
