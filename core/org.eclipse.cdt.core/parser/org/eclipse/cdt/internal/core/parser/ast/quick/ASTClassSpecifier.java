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

import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;
import org.eclipse.cdt.core.parser.ast.ASTClassKind;
import org.eclipse.cdt.core.parser.ast.IASTBaseSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTScope;
import org.eclipse.cdt.core.parser.ast.IASTTemplate;
import org.eclipse.cdt.internal.core.parser.ast.NamedOffsets;
/**
 * @author jcamelon
 *
 */
public class ASTClassSpecifier extends ASTScopedTypeSpecifier implements IASTQClassSpecifier, IASTQScope
{
    
    private final IASTScope scope;
    public ASTClassSpecifier(
        IASTScope scope,
        String name,
        ASTClassKind kind,
        ClassNameType type,
        ASTAccessVisibility access,
        IASTTemplate ownerTemplate)
    {
    	super( scope, name );
        this.scope = scope; 
        qualifiedNameElement = new ASTQualifiedNamedElement( scope, name );
        classNameType = type;
        classKind = kind;
        this.access = access;
        this.name = name;
    }
    
    private final ASTQualifiedNamedElement qualifiedNameElement;
    private final String name;
    private List declarations = new ArrayList();
    private List baseClauses = new ArrayList();
    private ASTAccessVisibility access;
    private NamedOffsets offsets = new NamedOffsets();
    private final ClassNameType classNameType;
    private final ASTClassKind classKind;
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTClassSpecifier#getClassNameType()
     */
    public ClassNameType getClassNameType()
    {
        return classNameType;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTClassSpecifier#getClassKind()
     */
    public ASTClassKind getClassKind()
    {
        return classKind;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTClassSpecifier#getBaseClauses()
     */
    public Iterator getBaseClauses()
    {
        return baseClauses.iterator();
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTClassSpecifier#getCurrentVisiblity()
     */
    public ASTAccessVisibility getCurrentVisibilityMode()
    {
        return access;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTScope#getDeclarations()
     */
    public Iterator getDeclarations()
    {
        return declarations.iterator();
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement#getName()
     */
    public String getName()
    {
        return name;
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
        offsets.setNameOffset(o);
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
     * @see org.eclipse.cdt.internal.core.parser.ast.quick.IASTQScope#addDeclaration(org.eclipse.cdt.core.parser.ast.IASTDeclaration)
     */
    public void addDeclaration(IASTDeclaration declaration)
    {
        declarations.add(declaration);
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser.ast.quick.IASTQClassSpecifier#addBaseClass(org.eclipse.cdt.core.parser.ast.IASTBaseSpecifier)
     */
    public void addBaseClass(IASTBaseSpecifier baseSpecifier)
    {
        baseClauses.add(baseSpecifier);
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTClassSpecifier#setCurrentVisibility(org.eclipse.cdt.core.parser.ast.ASTAccessVisibility)
     */
    public void setCurrentVisibility(ASTAccessVisibility visibility)
    {
    	this.access = visibility;
    }

}
