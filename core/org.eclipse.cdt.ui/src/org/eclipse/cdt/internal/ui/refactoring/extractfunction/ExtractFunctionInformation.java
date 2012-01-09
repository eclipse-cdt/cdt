/*******************************************************************************
 * Copyright (c) 2008, 2009 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 *     Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.extractfunction;

import java.util.ArrayList;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;

import org.eclipse.cdt.internal.ui.refactoring.MethodContext;
import org.eclipse.cdt.internal.ui.refactoring.NodeContainer.NameInformation;
import org.eclipse.cdt.internal.ui.refactoring.utils.VisibilityEnum;

public class ExtractFunctionInformation {
	public final int VISIBILITY_PRIVATE = 1;
	public final int VISIBILITY_PROTECTED = 3;
	public final int VISIBILITY_PUBLIC = 2;
	
	private VisibilityEnum visibility = VisibilityEnum.v_private;
	private String methodName;
	private boolean replaceDuplicates;
	private ArrayList<NameInformation> allAfterUsedNames;
	private ArrayList<NameInformation> allUsedNames;
	private NameInformation inScopeDeclaredVariable; 
	private NameInformation returnVariable;
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

	public ArrayList<NameInformation> getAllAfterUsedNames() {
		if (allAfterUsedNames == null) {
			allAfterUsedNames = new ArrayList<NameInformation>();
			for (NameInformation name : getAllUsedNames()) {
				if (name.isReference()||name.isReturnValue()) {
					allAfterUsedNames.add(name);
				}
			}
		}
		
		return allAfterUsedNames;
	}

	public void setAllAfterUsedNames(ArrayList<NameInformation> allAfterUsedNames) {
		this.allAfterUsedNames = allAfterUsedNames;
	}

	public NameInformation getReturnVariable() {
		return returnVariable;
	}

	public void setReturnVariable(NameInformation returnVariable) {
		if (returnVariable != null) {
			returnVariable.setUserSetIsReturnValue(true);
		}
		this.returnVariable = returnVariable;
	}

	public NameInformation getInScopeDeclaredVariable() {
		return inScopeDeclaredVariable;
	}

	public void setInScopeDeclaredVariable(NameInformation inScopeDeclaredVariable) {
		this.inScopeDeclaredVariable = inScopeDeclaredVariable;
	}

	public ArrayList<NameInformation> getAllUsedNames() {
		return allUsedNames;
	}

	public void setAllUsedNames(ArrayList<NameInformation> allUsedNames) {
		this.allUsedNames = allUsedNames;
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
}
