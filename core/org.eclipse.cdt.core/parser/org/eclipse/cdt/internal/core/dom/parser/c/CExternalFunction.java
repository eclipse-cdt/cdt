/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

/*
 * Created on Jan 26, 2005
 */
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.c.ICExternalBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPFunctionType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;

/**
 * @author aniefer
 */
public class CExternalFunction extends CFunction implements ICExternalBinding {
    private IASTName name = null;
    private IASTTranslationUnit tu = null;
    
    public CExternalFunction( IASTTranslationUnit tu, IASTName name ) {
    	super( null );
        this.name = name;
        this.tu = tu;
    }


    @Override
	public IFunctionType getType() {
		if (type == null) {
			// Bug 321856: Prevent recursions
			type = new CPPFunctionType(CPPSemantics.VOID_TYPE, IType.EMPTY_TYPE_ARRAY);
			IFunctionType computedType = createType();
			if (computedType != null) {
				type = computedType;
			}
		}
		return type;
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
