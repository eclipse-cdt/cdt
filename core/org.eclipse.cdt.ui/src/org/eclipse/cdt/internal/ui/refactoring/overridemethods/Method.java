/******************************************************************************* 
 * Copyright (c) 2017 Pavel Marek 
 * All rights reserved. This program and the accompanying materials  
 * are made available under the terms of the Eclipse Public License v1.0  
 * which accompanies this distribution, and is available at  
 * http://www.eclipse.org/legal/epl-v10.html   
 *  
 * Contributors:  
 *      Pavel Marek - initial API and implementation 
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.overridemethods;

import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;

/**
 * Wrapper for ICPPMethod. Provides converting to String functionality.
 * @author Pavel Marek 
 *
 */
public class Method {
	private ICPPMethod fMethod;

	/**
	 * Accepts only methods declared as virtual.
	 * @param method
	 */
	public Method(ICPPMethod method) {
		this.fMethod= method;
	}
	
	/**
	 * Two methods are considered equal if they have same signature ie. name
	 * and types of parameters in same order.
	 */
	@Override
	public int hashCode() {
		StringBuilder stringBuilder= new StringBuilder();
		
		stringBuilder.append(fMethod.getName());
		for (ICPPParameter parameter : fMethod.getParameters()) {
			stringBuilder.append(parameter.getType());

		}
		return stringBuilder.toString().hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		return this.hashCode() == o.hashCode();
	}
	
	public ICPPMethod getMethod() {
		return fMethod;
	}

	/**
	 * Accepts only methods declared as virtual.
	 * @param fMethod
	 */
	public void setMethod(ICPPMethod fMethod) {
		this.fMethod = fMethod;
	}

	private String printType(IType type) {
		if (type instanceof ITypedef) {
			return ((ITypedef) type).getName();
		}
		else {
			return type.toString();
		}
	}
	
	/**
	 * Build StringBuilder containing method signature, conforming to this
	 * format: "\<virtual\> RET NAME(PARAMS) \<const\>" ie. without trailing
	 * pure virtual specifier or semicolon.
	 * 
	 * Note: StringBuilder returned by this method is supposed to be appended.
	 * @return 
	 */
	private StringBuilder methodSignature() {
		ICPPFunctionType functionType= fMethod.getDeclaredType();
		ICPPParameter[] parameters= fMethod.getParameters();
		StringBuilder stringBuilder= new StringBuilder();
		
		// fMethod is always virtual
		// TODO: check preferences.
		stringBuilder.append("virtual "); //$NON-NLS-1$
		stringBuilder.append(printType(functionType.getReturnType())); 
		stringBuilder.append(" "); //$NON-NLS-1$
		stringBuilder.append(fMethod.getName() + "("); //$NON-NLS-1$
		// Print parameters.
		for (int i = 0; i < parameters.length; ++i) {
			stringBuilder.append(printType(parameters[i].getType()) + " "); //$NON-NLS-1$
			if (i == parameters.length - 1) {
				// Last parameter - do not print white space.
				stringBuilder.append(parameters[i].getName());
			}
			else {
				stringBuilder.append(parameters[i].getName() + " "); //$NON-NLS-1$
			}
		}
		stringBuilder.append(")"); //$NON-NLS-1$
		
		// Insert const keyword if necessary.
		if (functionType.isConst()) {
			stringBuilder.append(" const"); //$NON-NLS-1$
		}

		// Do not insert trailing semicolon nor pure virtual specifier.
		
		return stringBuilder;
	}
	
	/**
	 * Prints this method as overridden into code.
	 */
	public String print() {
		StringBuilder stringBuilder= methodSignature();
		
		// TODO: check preferences
		if (true) {
			stringBuilder.append(" override"); //$NON-NLS-1$
		}
		stringBuilder.append(";"); //$NON-NLS-1$
		
		return stringBuilder.toString();
	}
	
	/**
	 * TODO: This method should be implemented by getting IASTNode for fMethod
	 * and printing this IASTNode - if it is possible.
	 */
	@Override
	public String toString() {
		StringBuilder stringBuilder= methodSignature();
		if (fMethod.isPureVirtual()) {
			stringBuilder.append("=0"); //$NON-NLS-1$
		}
		stringBuilder.append(";"); //$NON-NLS-1$
		
		return stringBuilder.toString();
		
	}
}
