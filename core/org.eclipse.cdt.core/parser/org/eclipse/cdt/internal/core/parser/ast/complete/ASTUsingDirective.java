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
package org.eclipse.cdt.internal.core.parser.ast.complete;

import java.util.List;

import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.ast.ASTNotImplementedException;
import org.eclipse.cdt.core.parser.ast.IASTNamespaceDefinition;
import org.eclipse.cdt.core.parser.ast.IASTUsingDirective;
import org.eclipse.cdt.internal.core.parser.ast.Offsets;
import org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol;

/**
 * @author jcamelon
 *
 */
public class ASTUsingDirective extends ASTAnonymousDeclaration implements IASTUsingDirective
{
	private final IASTNamespaceDefinition namespace; 
	private Offsets offsets = new Offsets();
	private final ASTReferenceStore referenceDelegate;
    /**
     * @param namespaceDefinition
     * @param startingOffset
     * @param endingOffset
     */
    public ASTUsingDirective(IContainerSymbol ownerSymbol, IASTNamespaceDefinition namespaceDefinition, int startingOffset, int endingOffset, List references )
    {
    	super( ownerSymbol );
        namespace = namespaceDefinition;
        setStartingOffset(startingOffset);
        setEndingOffset(endingOffset);
        referenceDelegate = new ASTReferenceStore( references );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTUsingDirective#getNamespaceName()
     */
    public String getNamespaceName()
    {
        String [] fqn = namespace.getFullyQualifiedName();
        StringBuffer buffer = new StringBuffer(); 
        for( int i = 0; i < fqn.length; ++i )
        {
        	buffer.append( fqn[ i ] );
        	if( i + 1 != fqn.length ) 
        		buffer.append( "::");
        }
        return buffer.toString();
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
        offsets.setEndingOffset( o );
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#getElementStartingOffset()
     */
    public int getStartingOffset()
    {
        return offsets.getStartingOffset();
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#getElementEndingOffset()
     */
    public int getEndingOffset()
    {
        return offsets.getEndingOffset();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#acceptElement(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void acceptElement(ISourceElementRequestor requestor)
    {
        try
        {
            requestor.acceptUsingDirective( this );
        }
        catch (Exception e)
        {
            /* do nothing */
        }
        referenceDelegate.processReferences(requestor);
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#enterScope(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void enterScope(ISourceElementRequestor requestor)
    {
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#exitScope(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void exitScope(ISourceElementRequestor requestor)
    {
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTUsingDirective#getNamespaceDefinition()
     */
    public IASTNamespaceDefinition getNamespaceDefinition() throws ASTNotImplementedException
    {
        return namespace;
    }
}
