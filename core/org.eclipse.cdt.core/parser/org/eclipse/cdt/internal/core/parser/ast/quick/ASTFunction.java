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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.parser.ast.IASTAbstractDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTExceptionSpecification;
import org.eclipse.cdt.core.parser.ast.IASTFunction;
import org.eclipse.cdt.core.parser.ast.IASTQualifiedNameElement;
import org.eclipse.cdt.core.parser.ast.IASTScope;
import org.eclipse.cdt.core.parser.ast.IASTTemplate;
import org.eclipse.cdt.internal.core.parser.ast.NamedOffsets;

/**
 * @author jcamelon
 *
 */
public class ASTFunction extends ASTDeclaration implements IASTFunction
{
	/**
     * @param scope
     */
    public ASTFunction(IASTScope scope, String name, List parameters, IASTAbstractDeclaration returnType, IASTExceptionSpecification exception, 
    			boolean isInline, boolean isFriend, boolean isStatic, int startOffset, int nameOffset, IASTTemplate ownerTemplate )
    {
        super(ownerTemplate != null ? null : scope  );
        this.name = name; 
        this.parms = parameters;
        this.returnType = returnType;
        this.exceptionSpec = exception; 
        this.isInline = isInline;
        this.isFriend = isFriend;
        this.isStatic = isStatic;
        this.ownerTemplateDeclaration = ownerTemplate;
		if( ownerTemplate != null )
			ownerTemplate.setOwnedDeclaration( this );
        offsets.setStartingOffset( startOffset );
        offsets.setNameOffset( nameOffset );
        qualifiedName = new ASTQualifiedNamedElement( scope, name );
    }
    
    private final IASTQualifiedNameElement qualifiedName; 
    private final IASTTemplate ownerTemplateDeclaration;
    private NamedOffsets offsets = new NamedOffsets();
    private List  declarations = new ArrayList(); 
    private final IASTExceptionSpecification exceptionSpec;
    private final String name;
    private final List parms;
    private final IASTAbstractDeclaration returnType;
    private final boolean isInline, isFriend, isStatic; 
	
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFunction#isInline()
     */
    public boolean isInline()
    {
        return isInline;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFunction#isFriend()
     */
    public boolean isFriend()
    {
        return isFriend;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFunction#isStatic()
     */
    public boolean isStatic()
    {
        return isStatic;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement#getName()
     */
    public String getName()
    {
        return name;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFunction#getReturnType()
     */
    public IASTAbstractDeclaration getReturnType()
    {
        return returnType;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFunction#getParameters()
     */
    public Iterator getParameters()
    {
        return parms.iterator();
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFunction#getExceptionSpec()
     */
    public IASTExceptionSpecification getExceptionSpec()
    {
        return exceptionSpec;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement#getElementNameOffset()
     */
    public int getElementNameOffset()
    {
        return offsets.getElementNameOffset();
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement#setNameOffset(int)
     */
    public void setElementNameOffset(int o)
    {
        offsets.setNameOffset( o );
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTTemplatedDeclaration#getOwnerTemplateDeclaration()
     */
    public IASTTemplate getOwnerTemplateDeclaration()
    {
        return ownerTemplateDeclaration;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#setStartingOffset(int)
     */
    public void setStartingOffset(int o)
    {
        offsets.setStartingOffset(o);
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#setEndingOffset(int)
     */
    public void setEndingOffset(int o)
    {
        offsets.setEndingOffset(o);
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#getElementStartingOffset()
     */
    public int getElementStartingOffset()
    {
        return offsets.getElementStartingOffset();
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#getElementEndingOffset()
     */
    public int getElementEndingOffset()
    {
        return offsets.getElementEndingOffset();
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTScope#getDeclarations()
     */
    public Iterator getDeclarations()
    {
        return declarations.iterator();
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTQualifiedNameElement#getFullyQualifiedName()
     */
    public String[] getFullyQualifiedName()
    {
        return qualifiedName.getFullyQualifiedName();
    }
}
