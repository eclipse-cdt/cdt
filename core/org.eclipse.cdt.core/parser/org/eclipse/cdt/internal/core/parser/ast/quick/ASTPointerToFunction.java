/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.internal.core.parser.ast.quick;

import java.util.List;

import org.eclipse.cdt.core.parser.ast.ASTPointerOperator;
import org.eclipse.cdt.core.parser.ast.IASTAbstractDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTExceptionSpecification;
import org.eclipse.cdt.core.parser.ast.IASTPointerToFunction;
import org.eclipse.cdt.core.parser.ast.IASTScope;
import org.eclipse.cdt.core.parser.ast.IASTTemplate;

/**
 * @author jcamelon
 *
 */
public class ASTPointerToFunction
    extends ASTFunction
    implements IASTPointerToFunction
{

    private final ASTPointerOperator pointerOperator;

    /**
     * @param scope
     * @param name
     * @param parameters
     * @param returnType
     * @param exception
     * @param isInline
     * @param isFriend
     * @param isStatic
     * @param startOffset
     * @param nameOffset
     * @param ownerTemplate
     */
    public ASTPointerToFunction(IASTScope scope, String name, List parameters, IASTAbstractDeclaration returnType, IASTExceptionSpecification exception, boolean isInline, boolean isFriend, boolean isStatic, int startOffset, int nameOffset, IASTTemplate ownerTemplate, ASTPointerOperator pointerOperator)
    {
        super(
            scope,
            name,
            parameters,
            returnType,
            exception,
            isInline,
            isFriend,
            isStatic,
            startOffset,
            nameOffset,
            ownerTemplate);
        this.pointerOperator = pointerOperator;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTPointerOperatorOwner#getPointerOperator()
     */
    public ASTPointerOperator getPointerOperator()
    {
        return pointerOperator;
    }


}
