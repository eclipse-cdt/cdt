/*******************************************************************************
 * Copyright (c) 2002, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser;
import java.util.List;

import org.eclipse.cdt.core.parser.ITokenDuple;
import org.eclipse.cdt.core.parser.ast.ASTPointerOperator;
import org.eclipse.cdt.core.parser.ast.IASTArrayModifier;
import org.eclipse.cdt.core.parser.ast.IASTScope;
/**
 * @author jcamelon
 *
 */
public interface IDeclarator
{
	public IASTScope getScope();
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
