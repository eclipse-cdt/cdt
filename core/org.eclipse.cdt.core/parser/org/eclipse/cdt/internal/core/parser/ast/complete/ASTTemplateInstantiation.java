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

import java.util.Iterator;

import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.ast.ASTNotImplementedException;
import org.eclipse.cdt.core.parser.ast.IASTDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTScope;
import org.eclipse.cdt.core.parser.ast.IASTTemplateDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTTemplateInstantiation;
import org.eclipse.cdt.internal.core.parser.ast.NamedOffsets;
import org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol;
import org.eclipse.cdt.internal.core.parser.pst.ISymbol;
import org.eclipse.cdt.internal.core.parser.pst.ITemplateFactory;
import org.eclipse.cdt.internal.core.parser.pst.ITemplateSymbol;
import org.eclipse.cdt.internal.core.parser.pst.StandardSymbolExtension;

/**
 * @author jcamelon
 *
 */
public class ASTTemplateInstantiation extends ASTSymbol implements IASTTemplateInstantiation
{
	private ITemplateFactory factory;
	private IASTScope ownerScope;
	private IASTTemplateDeclaration instantiatedTemplate;
	private ISymbol instance;
	
	private NamedOffsets offsets = new NamedOffsets();
	
    public ASTTemplateInstantiation( IASTScope scope ){
    	super( null );
    	IContainerSymbol container = ((ASTScope)scope).getContainerSymbol();
    	
    	factory = container.getSymbolTable().newTemplateFactory();
    	factory.setContainingSymbol( container );
    	
    	factory.setASTExtension( new StandardSymbolExtension( factory, this ) );
    	
    	factory.pushTemplate( null );
    	ownerScope = scope;
    }
    
    public IASTTemplateDeclaration getInstantiatedTemplate(){
    	return instantiatedTemplate;
    }
    
    public void releaseFactory(){
    	factory = null;
    }
    
    public void setInstanceSymbol( ISymbol sym ){
    	instance = sym;
    	ITemplateSymbol template = (ITemplateSymbol) instance.getInstantiatedSymbol().getContainingSymbol();
    	instantiatedTemplate = (IASTTemplateDeclaration) template.getASTExtension().getPrimaryDeclaration();
    	setSymbol( instance.getInstantiatedSymbol().getContainingSymbol() );
    }
    
    public ISymbol getInstanceSymbol(){
    	return instance;
    }
    
    public IContainerSymbol getContainerSymbol()
	{
       	return factory != null ? (IContainerSymbol) factory : ((ASTTemplateDeclaration)getInstantiatedTemplate()).getContainerSymbol();
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTTemplate#getOwnedDeclaration()
     */
    public IASTDeclaration getOwnedDeclaration()
    {
        // TODO Auto-generated method stub
        return null;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTTemplate#setOwnedDeclaration(org.eclipse.cdt.core.parser.ast.IASTDeclaration)
     */
    public void setOwnedDeclaration(IASTDeclaration declaration)
    {
        // TODO Auto-generated method stub
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#setStartingOffset(int)
     */
    public void setStartingOffsetAndLineNumber(int offset, int lineNumber)
    {
    	offsets.setStartingOffsetAndLineNumber( offset, lineNumber );
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#setEndingOffset(int)
     */
    public void setEndingOffsetAndLineNumber(int offset, int lineNumber)
    {
    	offsets.setEndingOffsetAndLineNumber( offset, lineNumber );
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
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#getStartingLine()
	 */
	public int getStartingLine() {
		return offsets.getStartingLine();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#getEndingLine()
	 */
	public int getEndingLine() {
		return offsets.getEndingLine();
	}
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTScopedElement#getOwnerScope()
     */
    public IASTScope getOwnerScope()
    {
        return ownerScope;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#acceptElement(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void acceptElement(ISourceElementRequestor requestor)
    {
        // TODO Auto-generated method stub
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#enterScope(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void enterScope(ISourceElementRequestor requestor)
    {
    	try
        {
            requestor.enterTemplateInstantiation(this);
        }
        catch (Exception e)
        {
            /* do nothing */
        }
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#exitScope(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void exitScope(ISourceElementRequestor requestor)
    {
    	try
        {
            requestor.exitTemplateExplicitInstantiation(this);
        }
        catch (Exception e)
        {
            /* do nothing */
        }
    }

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTScope#getDeclarations()
	 */
	public Iterator getDeclarations() throws ASTNotImplementedException {
		// TODO Auto-generated method stub
		return null;
	}
}
