/**********************************************************************
 * Copyright (c) 2002,2003, 2004 Rational Software Corporation and others.
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
import org.eclipse.cdt.core.parser.ast.IASTNamespaceDefinition;
import org.eclipse.cdt.core.parser.ast.IASTUsingDirective;
import org.eclipse.cdt.core.parser.ast.IReferenceManager;
import org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol;
import org.eclipse.cdt.internal.core.parser.pst.IUsingDirectiveSymbol;

/**
 * @author jcamelon
 *
 */
public class ASTUsingDirective extends ASTAnonymousDeclaration implements IASTUsingDirective
{
	private final IUsingDirectiveSymbol using;
	private List references;
    /**
     * @param namespaceDefinition
     * @param startingOffset
     * @param endingOffset
     */
    //public ASTUsingDirective(IContainerSymbol ownerSymbol, IASTNamespaceDefinition namespaceDefinition, int startingOffset, int endingOffset, List references )
	public ASTUsingDirective(IContainerSymbol ownerSymbol, IUsingDirectiveSymbol usingDirective, int startingOffset, int startingLine, int endingOffset, int endingLine, List references )
    {
    	super( ownerSymbol );
        //namespace = namespaceDefinition;
        using = usingDirective; 
        setStartingOffsetAndLineNumber(startingOffset, startingLine);
        setEndingOffsetAndLineNumber(endingOffset, endingLine);
        this.references = references;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTUsingDirective#getNamespaceName()
     */
    public String getNamespaceName()
    {
    	IASTNamespaceDefinition namespace = getNamespaceDefinition();
		String [] fqn = namespace.getFullyQualifiedName();
        StringBuffer buffer = new StringBuffer(); 
        for( int i = 0; i < fqn.length; ++i )
        {
        	buffer.append( fqn[ i ] );
        	if( i + 1 != fqn.length ) 
        		buffer.append( "::"); //$NON-NLS-1$
        }
        return buffer.toString();
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#acceptElement(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void acceptElement(ISourceElementRequestor requestor, IReferenceManager manager)
    {
        try
        {
            requestor.acceptUsingDirective( this );
        }
        catch (Exception e)
        {
            /* do nothing */
        }
        manager.processReferences(references, requestor);
        references = null;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#enterScope(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void enterScope(ISourceElementRequestor requestor, IReferenceManager manager)
    {
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#exitScope(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void exitScope(ISourceElementRequestor requestor, IReferenceManager manager)
    {
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTUsingDirective#getNamespaceDefinition()
     */
    public IASTNamespaceDefinition getNamespaceDefinition()
    {
    	IContainerSymbol namespaceSymbol = using.getNamespace();
    	
        return (IASTNamespaceDefinition)namespaceSymbol.getASTExtension().getPrimaryDeclaration();
    }
    
	private int startingLineNumber, startingOffset, endingLineNumber, endingOffset;
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#getStartingLine()
     */
    public final int getStartingLine() {
    	return startingLineNumber;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#getEndingLine()
     */
    public final int getEndingLine() {
    	return endingLineNumber;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#setStartingOffset(int)
     */
    public final void setStartingOffsetAndLineNumber(int offset, int lineNumber)
    {
    	startingOffset = offset;
    	startingLineNumber = lineNumber;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#setEndingOffset(int)
     */
    public final void setEndingOffsetAndLineNumber(int offset, int lineNumber)
    {
    	endingOffset = offset;
    	endingLineNumber = lineNumber;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#getStartingOffset()
     */
    public final int getStartingOffset()
    {
        return startingOffset;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#getEndingOffset()
     */
    public final int getEndingOffset()
    {
        return endingOffset;
    }

}
