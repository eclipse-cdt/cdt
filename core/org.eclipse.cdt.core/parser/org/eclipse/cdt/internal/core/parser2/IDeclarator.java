/**********************************************************************
 * Copyright (c) 2002-2004 IBM Canada and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation */
package org.eclipse.cdt.internal.core.parser2;

import java.util.List;

import org.eclipse.cdt.core.parser.ITokenDuple;
import org.eclipse.cdt.core.parser.ast.ASTPointerOperator;
import org.eclipse.cdt.core.parser.ast.IASTArrayModifier;

/**
 * @author jcamelon
 */
public interface IDeclarator
{
	public Object getScope();
    /**
     * @return
     */
    public abstract List getPointerOperators();
    public abstract void addPointerOperator(ASTPointerOperator ptrOp);
    /**
     * @param arrayMod
     */
    public abstract void addArrayModifier(IASTArrayModifier arrayMod);
    /**
     * @return
     */
    public abstract List getArrayModifiers();
    
	/**
	 * @param nameDuple
	 */
	public void setPointerOperatorName(ITokenDuple nameDuple);

	public ITokenDuple getPointerOperatorNameDuple();

}