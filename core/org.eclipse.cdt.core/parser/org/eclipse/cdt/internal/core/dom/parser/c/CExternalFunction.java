/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Niefer (IBM Corporation) - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IBasicType.Kind;
import org.eclipse.cdt.core.dom.ast.c.ICExternalBinding;

/**
 * Models functions used without declarations.
 */
public class CExternalFunction extends CFunction implements ICExternalBinding {
    private static final IType VOID_TYPE = 	new CBasicType(Kind.eVoid, 0);

	private IASTName name = null;
    private IASTTranslationUnit tu = null;
    
    public CExternalFunction( IASTTranslationUnit tu, IASTName name ) {
    	super( null );
        this.name = name;
        this.tu = tu;
    }


    @Override
	public IFunctionType getType() {
		IFunctionType t = super.getType();
		if (t == null) {
			type = new CFunctionType(VOID_TYPE, IType.EMPTY_TYPE_ARRAY);
		}
		return type;
	}
    
    @Override
	public IParameter[] getParameters() {
    	return IParameter.EMPTY_PARAMETER_ARRAY;
	}

	@Override
	protected IASTTranslationUnit getTranslationUnit() {
		return tu;
    }

    @Override
	public String getName() {
        return name.toString();
    }

    @Override
	public char[] getNameCharArray() {
        return name.toCharArray();
    }

    @Override
	public IScope getScope() {
        return tu.getScope();
    }

    @Override
	public boolean isExtern() {
        return true;
    }
}
