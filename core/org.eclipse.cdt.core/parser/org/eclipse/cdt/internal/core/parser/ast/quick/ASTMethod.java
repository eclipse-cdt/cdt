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

import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;
import org.eclipse.cdt.core.parser.ast.IASTAbstractDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTExceptionSpecification;
import org.eclipse.cdt.core.parser.ast.IASTMethod;
import org.eclipse.cdt.core.parser.ast.IASTScope;
import org.eclipse.cdt.core.parser.ast.IASTTemplate;
/**
 * @author jcamelon
 *
 */
public class ASTMethod extends ASTFunction implements IASTMethod
{
    private final boolean isConst;
    private final boolean isDestructor;
    private final boolean isConstructor;
    private final boolean isExplicit;
    private final boolean isPureVirtual;
    private final boolean isVirtual;
    private final boolean isVolatile;
    private final ASTAccessVisibility visibility;
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
    public ASTMethod(
        IASTScope scope,
        String name,
        List parameters,
        IASTAbstractDeclaration returnType,
        IASTExceptionSpecification exception,
        boolean isInline,
        boolean isFriend,
        boolean isStatic,
        int startOffset,
        int nameOffset,
        IASTTemplate ownerTemplate,
        boolean isConst,
        boolean isVolatile,
        boolean isConstructor,
        boolean isDestructor,
        boolean isVirtual,
        boolean isExplicit,
        boolean isPureVirtual, ASTAccessVisibility visibility)
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
        this.isVirtual = isVirtual;
        this.isPureVirtual = isPureVirtual;
        this.isConstructor = isConstructor;
        this.isDestructor = isDestructor;
        this.isExplicit = isExplicit; 
        this.isConst = isConst;
        this.isVolatile = isVolatile;
        this.visibility = visibility;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTMethod#isVirtual()
     */
    public boolean isVirtual()
    {
        return isVirtual;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTMethod#isExplicit()
     */
    public boolean isExplicit()
    {
        // TODO Auto-generated method stub
        return isExplicit;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTMethod#isConstructor()
     */
    public boolean isConstructor()
    {
        return isConstructor;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTMethod#isDestructor()
     */
    public boolean isDestructor()
    {
        return isDestructor;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTMethod#isConst()
     */
    public boolean isConst()
    {
        return isConst;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTMethod#isVolatile()
     */
    public boolean isVolatile()
    {
        return isVolatile;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTMethod#isPureVirtual()
     */
    public boolean isPureVirtual()
    {
        return isPureVirtual;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTMember#getVisiblity()
     */
    public ASTAccessVisibility getVisiblity()
    {
        return visibility;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTMember#getOwnerClassSpecifier()
     */
}
