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

import org.eclipse.cdt.core.parser.ast.IASTAbstractDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTExpression;
import org.eclipse.cdt.core.parser.ast.IASTInitializerClause;
import org.eclipse.cdt.core.parser.ast.IASTScope;
import org.eclipse.cdt.core.parser.ast.IASTVariable;
import org.eclipse.cdt.internal.core.parser.ast.NamedOffsets;

/**
 * @author jcamelon
 *
 */
public class ASTVariable extends ASTDeclaration implements IASTVariable
{
    private final boolean isAuto;
    private final IASTInitializerClause initializerClause;
    private final IASTExpression bitfieldExpression;
    private final IASTAbstractDeclaration abstractDeclaration;
    private final boolean isMutable;
    private final boolean isExtern;
    private final boolean isRegister;
    private final boolean isStatic;
    private final String name;
    private NamedOffsets offsets = new NamedOffsets(); 
    /**
     * @param scope
     */
    public ASTVariable(IASTScope scope, String name, boolean isAuto, IASTInitializerClause initializerClause, IASTExpression bitfieldExpression, 
    	IASTAbstractDeclaration abstractDeclaration, boolean isMutable, boolean isExtern, boolean isRegister, boolean isStatic, int startingOffset, int nameOffset )
    {
        super(scope);
		this.isAuto = isAuto;
		this.initializerClause = initializerClause;
		this.bitfieldExpression = bitfieldExpression;
		this.abstractDeclaration = abstractDeclaration;
		this.isMutable= isMutable;
		this.isExtern = isExtern;
		this.isRegister = isRegister;
		this.isStatic = isStatic;
		this.name = name;

    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTVariable#isAuto()
     */
    public boolean isAuto()
    {
        return isAuto;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTVariable#isRegister()
     */
    public boolean isRegister()
    {
        return isRegister;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTVariable#isStatic()
     */
    public boolean isStatic()
    {
        return isStatic;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTVariable#isExtern()
     */
    public boolean isExtern()
    {
        return isExtern;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTVariable#isMutable()
     */
    public boolean isMutable()
    {
        return isMutable;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTVariable#getAbstractDeclaration()
     */
    public IASTAbstractDeclaration getAbstractDeclaration()
    {
        return abstractDeclaration;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTVariable#getName()
     */
    public String getName()
    {
        return name;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTVariable#getInitializerClause()
     */
    public IASTInitializerClause getInitializerClause()
    {
        return initializerClause;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTVariable#isBitfield()
     */
    public boolean isBitfield()
    {
        return ( bitfieldExpression != null );
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTVariable#getBitfieldExpression()
     */
    public IASTExpression getBitfieldExpression()
    {
        // TODO Auto-generated method stub
        return bitfieldExpression;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#setStartingOffset(int)
     */
    public void setStartingOffset(int o)
    {
        // TODO Auto-generated method stub
        
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#setEndingOffset(int)
     */
    public void setEndingOffset(int o)
    {
        // TODO Auto-generated method stub
        
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#getElementStartingOffset()
     */
    public int getElementStartingOffset()
    {
        // TODO Auto-generated method stub
        return 0;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#getElementEndingOffset()
     */
    public int getElementEndingOffset()
    {
        // TODO Auto-generated method stub
        return 0;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement#getElementNameOffset()
     */
    public int getElementNameOffset()
    {
        // TODO Auto-generated method stub
        return 0;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement#setNameOffset(int)
     */
    public void setNameOffset(int o)
    {
        // TODO Auto-generated method stub
        
    }
 
}
