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

import org.eclipse.cdt.core.parser.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTEnumerator;
import org.eclipse.cdt.core.parser.ast.IASTExpression;
import org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement;
import org.eclipse.cdt.internal.core.parser.ast.NamedOffsets;

/**
 * @author jcamelon
 *
 */
public class ASTEnumerator
    implements IASTEnumerator, IASTOffsetableNamedElement
{
	private final IASTExpression initialValue;
    private final String name; 
	private final IASTEnumerationSpecifier enumeration;
	private final NamedOffsets offsets = new NamedOffsets(); 
    /**
     * @param enumeration
     * @param string
     * @param startingOffset
     * @param endingOffset
     */
    public ASTEnumerator(IASTEnumerationSpecifier enumeration, String string, int startingOffset, int endingOffset, IASTExpression initialValue)
    {
    	this.enumeration = enumeration; 
        name = string;
        offsets.setStartingOffset( startingOffset );
		offsets.setNameOffset( startingOffset );
		offsets.setEndingOffset( endingOffset );
		enumeration.addEnumerator(this);
		this.initialValue = initialValue;
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
     * @see org.eclipse.cdt.core.parser.ast.IASTEnumerator#getOwnerEnumerationSpecifier()
     */
    public IASTEnumerationSpecifier getOwnerEnumerationSpecifier()
    {
        return enumeration;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTEnumerator#getInitialValue()
     */
    public IASTExpression getInitialValue()
    {
        return initialValue;
    }
}
