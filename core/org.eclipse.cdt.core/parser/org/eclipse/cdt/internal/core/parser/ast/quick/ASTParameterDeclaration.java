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
import org.eclipse.cdt.core.parser.ast.IASTInitializerClause;
import org.eclipse.cdt.core.parser.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTTypeSpecifier;
import org.eclipse.cdt.internal.core.parser.ast.ASTAbstractDeclaration;
import org.eclipse.cdt.internal.core.parser.ast.NamedOffsets;

/**
 * @author jcamelon
 *
 */
public class ASTParameterDeclaration extends ASTAbstractDeclaration implements IASTParameterDeclaration
{
    private final NamedOffsets offsets = new NamedOffsets();
	private final String parameterName; 
	private final IASTInitializerClause initializerClause;
    /**
     * @param isConst
     * @param typeSpecifier
     * @param pointerOperators
     * @param arrayModifiers
     * @param parameterName
     * @param initializerClause
     */
    public ASTParameterDeclaration(boolean isConst, boolean isVolatile, IASTTypeSpecifier typeSpecifier, List pointerOperators, List arrayModifiers, List parameters, ASTPointerOperator pointerOp, String parameterName, IASTInitializerClause initializerClause, int startingOffset, int endingOffset, int nameOffset, int nameEndOffset )
    {
    	super( isConst, isVolatile, typeSpecifier, pointerOperators, arrayModifiers, parameters, pointerOp );
		this.parameterName = parameterName;
		this.initializerClause = initializerClause;
		setStartingOffset( startingOffset );
		setEndingOffset( endingOffset );
		setNameOffset( nameOffset );
		setNameEndOffset(nameEndOffset);
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTParameterDeclaration#getName()
     */
    public String getName()
    {
        return parameterName;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTParameterDeclaration#getDefaultValue()
     */
    public IASTInitializerClause getDefaultValue()
    {
        return initializerClause;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement#getNameOffset()
	 */
	public int getNameOffset()
	{
		return offsets.getNameOffset();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement#setNameOffset(int)
	 */
	public void setNameOffset(int o)
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
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#getStartingOffset()
	 */
	public int getStartingOffset()
	{
		return offsets.getStartingOffset();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#getEndingOffset()
	 */
	public int getEndingOffset()
	{
		return offsets.getEndingOffset();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement#getNameEndOffset()
	 */
	public int getNameEndOffset()
	{
		return offsets.getNameEndOffset();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement#setNameEndOffset(int)
	 */
	public void setNameEndOffset(int o)
	{
		offsets.setNameEndOffset(o);
	}
}
