/*******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Rational Software - Initial API and implementation
 * Markus Schorn (Wind River Systems)
 * Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.model;


import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IMethodDeclaration;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;

public class MethodDeclaration extends FunctionDeclaration implements IMethodDeclaration{

	boolean isConst = false;
	boolean isConstructor = false;
	boolean isDestructor = false;

	public MethodDeclaration(ICElement parent, String name){
		super(parent, name, ICElement.C_METHOD_DECLARATION);
	}

	public MethodDeclaration(ICElement parent, String name, int type){
		super(parent, name, type);
	}

	@Override
	public boolean isConstructor(){
		return isConstructor;
	}

	@Override
	public boolean isDestructor() {
		return isDestructor;
	}

	public void setConstructor(boolean isConstructor) {
		this.isConstructor = isConstructor;
	}

	public void setDestructor(boolean isDestructor) {
		this.isDestructor = isDestructor;
	}

	@Override
	public boolean isOperator(){
		return getElementName().startsWith("operator"); //$NON-NLS-1$
	}

	@Override
	public boolean isPureVirtual() throws CModelException{
		return getMethodInfo().isPureVirtual();
	}

	public void setPureVirtual(boolean isPureVirtual) throws CModelException{
		getMethodInfo().setPureVirtual(isPureVirtual);
	}

	@Override
	public boolean isInline() throws CModelException{
		return getMethodInfo().isInline();
	}

	public void setInline(boolean isInline) throws CModelException{
		getMethodInfo().setInline(isInline);
	}

	@Override
	public boolean isVirtual() throws CModelException{
		return getMethodInfo().isVirtual();
	}

	public void setVirtual(boolean isVirtual) throws CModelException{
		getMethodInfo().setVirtual(isVirtual);
	}

	@Override
	public boolean isFriend() throws CModelException{
		return getMethodInfo().isFriend();
	}

	public void setFriend(boolean isFriend) throws CModelException{
		getMethodInfo().setFriend(isFriend);
	}

	@Override
	public boolean isConst(){
		return isConst;
	}

	@Override
	public void setConst(boolean isConst) throws CModelException{
		this.isConst = isConst;
	}

	@Override
	public ASTAccessVisibility getVisibility() throws CModelException{
		return getMethodInfo().getVisibility();
	}

	public void setVisibility(ASTAccessVisibility visibility) throws CModelException{
		getMethodInfo().setVisibility(visibility);
	}

	@Override
	protected CElementInfo createElementInfo () {
		return new MethodInfo(this);
	}

	protected MethodInfo getMethodInfo() throws CModelException{
		return (MethodInfo) getElementInfo();
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof IMethodDeclaration) {
			return equals(this, (IMethodDeclaration) other);
		}
		return false;
	}

	public static boolean equals(IMethodDeclaration lhs, IMethodDeclaration rhs) {
		try {
			return lhs.isConst() == rhs.isConst() &&
					FunctionDeclaration.equals(lhs, rhs);
		} catch (CModelException e) {
			return false;
		}
	}


}
