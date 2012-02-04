/*******************************************************************************
 * Copyright (c) 2008, 2012 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 *     Institute for Software - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.extractfunction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;

import org.eclipse.cdt.internal.ui.refactoring.MethodContext;
import org.eclipse.cdt.internal.ui.refactoring.NameInformation;
import org.eclipse.cdt.internal.ui.refactoring.utils.VisibilityEnum;

public class ExtractFunctionInformation {
	private VisibilityEnum visibility = VisibilityEnum.v_private;
	private String methodName;
	private boolean replaceDuplicates;
	private List<NameInformation> parameters;
	private NameInformation mandatoryReturnVariable; 
	private ICPPASTFunctionDeclarator declarator;
	private MethodContext context;
	private boolean isExtractExpression;
	private boolean virtual;

	/**
	 * Returns the function declarator of the method / function from were the statements
	 * are extacted from.
	 * @return the function declarator or null
	 */
	public ICPPASTFunctionDeclarator getDeclarator() {
		return declarator;
	}

	public void setDeclarator(ICPPASTFunctionDeclarator declarator) {
		this.declarator = declarator;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public boolean isReplaceDuplicates() {
		return replaceDuplicates;
	}

	public void setReplaceDuplicates(boolean replaceDuplicates) {
		this.replaceDuplicates = replaceDuplicates;
	}

	public NameInformation getReturnVariable() {
		if (mandatoryReturnVariable != null)
			return mandatoryReturnVariable;
		for (NameInformation param : parameters) {
			if (param.isReturnValue())
				return param;
		}
		return null;
	}

	public NameInformation getMandatoryReturnVariable() {
		return mandatoryReturnVariable;
	}

	public void setMandatoryReturnVariable(NameInformation variable) {
		this.mandatoryReturnVariable = variable;
	}

	public List<NameInformation> getParameters() {
		return parameters;
	}

	public void setParameters(List<NameInformation> parameters) {
		this.parameters = new ArrayList<NameInformation>(parameters);
	}

	public VisibilityEnum getVisibility() {
		return visibility;
	}

	public void setVisibility(VisibilityEnum visibility) {
		this.visibility = visibility;
	}

	public MethodContext getMethodContext() {
		return context;
	}

	public void setMethodContext(MethodContext context) {
		this.context = context;
	}

	public boolean isExtractExpression() {
		return isExtractExpression;
	}

	public void setExtractExpression(boolean isExtractExpression) {
		this.isExtractExpression = isExtractExpression;
	}

	public boolean isVirtual() {
		return virtual;
	}

	public void setVirtual(boolean isVirtual) {
		this.virtual = isVirtual;
	}

	public void sortParameters(final boolean outFirst) {
		Collections.sort(parameters, new Comparator<NameInformation>() {
			@Override
			public int compare(NameInformation p1, NameInformation p2) {
				boolean out1 = p1.isOutputParameter() || hasNonConstPointerOrReference(p1);
				boolean out2 = p2.isOutputParameter() || hasNonConstPointerOrReference(p2);
				return out1 == out2 ? 0 : out1 == outFirst ? -1 : 1;
			}
		});
	}

	public static boolean hasNonConstPointerOrReference(NameInformation param) {
		IASTDeclarator declarator = param.getDeclarator();
		IASTPointerOperator[] operators = declarator.getPointerOperators();
		if (operators.length != 0) {
			IASTDeclSpecifier declSpecifier = param.getDeclSpecifier();
			return declSpecifier == null || !declSpecifier.isConst();
		}
		return false;
	}
}
