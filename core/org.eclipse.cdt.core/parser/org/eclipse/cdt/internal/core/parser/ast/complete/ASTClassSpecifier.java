/*******************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser.ast.complete;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;
import org.eclipse.cdt.core.parser.ast.ASTClassKind;
import org.eclipse.cdt.core.parser.ast.IASTBaseSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTReference;
import org.eclipse.cdt.core.parser.ast.IASTScope;
import org.eclipse.cdt.internal.core.parser.Parser;
import org.eclipse.cdt.internal.core.parser.ast.ASTQualifiedNamedElement;
import org.eclipse.cdt.internal.core.parser.ast.SymbolIterator;
import org.eclipse.cdt.internal.core.parser.pst.IDerivableContainerSymbol;
import org.eclipse.cdt.internal.core.parser.pst.ISymbol;
import org.eclipse.cdt.internal.core.parser.pst.IDerivableContainerSymbol.IParentSymbol;

/**
 * @author jcamelon
 *
 */
public class ASTClassSpecifier extends ASTScope implements IASTClassSpecifier
{
	private List declarations = null;	
	public class BaseIterator implements Iterator
	{

		private final Iterator parents; 
		/**
		 * @param symbol
		 */
		public BaseIterator(IDerivableContainerSymbol symbol)
		{
			if( symbol.getParents() != null )
				parents = symbol.getParents().iterator();
			else
				parents = null;  
		}
		/* (non-Javadoc)
		 * @see java.util.Iterator#hasNext()
		 */
		public boolean hasNext()
		{
			if( parents == null )
				return false;
			return parents.hasNext();
		}
		/* (non-Javadoc)
		 * @see java.util.Iterator#next()
		 */
		public Object next()
		{
			if( ! hasNext() )
				throw new NoSuchElementException();
    		
			IParentSymbol pw = (IParentSymbol)parents.next();
        
			return new ASTBaseSpecifier( pw.getParent(), pw.isVirtual(), pw.getAccess(), pw.getOffset(), pw.getReferences() );
         
		}
		/* (non-Javadoc)
		 * @see java.util.Iterator#remove()
		 */
		public void remove()
		{
			throw new UnsupportedOperationException();
		}
	}

	
	
	private final ClassNameType classNameType; 
	private final ASTClassKind  classKind;
	private ASTAccessVisibility currentVisibility;
	private final ASTQualifiedNamedElement qualifiedName;
	private List references;

    /**
     * @param symbol
     */
    public ASTClassSpecifier(ISymbol symbol, ASTClassKind kind, ClassNameType type, ASTAccessVisibility access, int startingOffset, int startingLine, int nameOffset, int nameEndOffset, int nameLine, List references, char [] filename )
    {
        super(symbol);
        classKind = kind;
        classNameType = type; 
        currentVisibility = access;
        setStartingOffsetAndLineNumber(startingOffset, startingLine);
        setNameOffset(nameOffset);
        setNameEndOffsetAndLineNumber(nameEndOffset, nameLine);
		qualifiedName = new ASTQualifiedNamedElement( getOwnerScope(), symbol.getName() );
		this.references = references;
		fn = filename;
    }
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
        return new BaseIterator( (IDerivableContainerSymbol)getSymbol() );
    }
    
    private List getBaseClausesList(){
    	List clauses = ((IDerivableContainerSymbol)getSymbol()).getParents();
    	return (clauses != null) ? clauses : Collections.EMPTY_LIST;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTClassSpecifier#getCurrentVisibilityMode()
     */
    public ASTAccessVisibility getCurrentVisibilityMode()
    {
        return currentVisibility;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTClassSpecifier#setCurrentVisibility(org.eclipse.cdt.core.parser.ast.ASTAccessVisibility)
     */
    public void setCurrentVisibility(ASTAccessVisibility visibility)
    {
        currentVisibility = visibility;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement#getName()
     */
    public String getName()
    {
        return String.valueOf(symbol.getName());
    }
    public char[] getNameCharArray(){
        return symbol.getName();
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#acceptElement(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void acceptElement(ISourceElementRequestor requestor)
    {
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#enterScope(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void enterScope(ISourceElementRequestor requestor)
    {
    	Parser.processReferences( references, requestor );
    	references = null;
        try
        {
            requestor.enterClassSpecifier(this);
        }
        catch (Exception e)
        {
            /* do nothing */
        } 
        List bases = getBaseClausesList();
        int size = bases.size();
        for( int i = 0; i < size; i++ )
        {
        	IParentSymbol pw = (IParentSymbol)bases.get(i); 
    		IASTBaseSpecifier baseSpec = new ASTBaseSpecifier( pw.getParent(), pw.isVirtual(), pw.getAccess(), pw.getOffset(), pw.getReferences() );
        	baseSpec.acceptElement(requestor);
        }
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#exitScope(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void exitScope(ISourceElementRequestor requestor)
    {
    	Parser.processReferences( this.resolvedCrossReferences, requestor );
        try
        {
            requestor.exitClassSpecifier(this);
        }
        catch (Exception e)
        {
            /* do nothing */
        }
        
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTQualifiedNameElement#getFullyQualifiedName()
     */
    public String[] getFullyQualifiedName()
    {
        return qualifiedName.getFullyQualifiedName();
    }
    public char[][] getFullyQualifiedNameCharArrays(){
        return qualifiedName.getFullyQualifiedNameCharArrays();
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTScopedElement#getOwnerScope()
     */
    public IASTScope getOwnerScope()
    {
		return (IASTScope)symbol.getContainingSymbol().getASTExtension().getPrimaryDeclaration();
    }

    
    public Iterator getDeclarations()
    {
    	//If a callback (ie StructuralParseCallback) populates the declarations list
    	//then return that iterator, otherwise use the ASTScope implementation which
    	//gets one from the symbol table.
    	if( declarations != null ){
    		return declarations.iterator();	
    	}
    	return super.getDeclarations();
    }
    
    public void addDeclaration(IASTDeclaration declaration)
    {
    	declarations.add(declaration);
    }

    public void initDeclarations()
	{
    	declarations = new ArrayList(0);
	}
    
    private List unresolvedCrossReferences = Collections.EMPTY_LIST;
    private boolean processingUnresolvedReferences = false;
    public void addUnresolvedReference( UnresolvedReferenceDuple duple)
	{
    	//avoid a ConcurrentModificationException by not adding more references when we are 
    	//in the middle of processing them
    	if( !processingUnresolvedReferences ){
    	    if( unresolvedCrossReferences == Collections.EMPTY_LIST )
    	        unresolvedCrossReferences = new ArrayList();
    		unresolvedCrossReferences.add( duple );
    	}
    }
    
    public List getUnresolvedReferences()
	{
    	return unresolvedCrossReferences;
    }
    
    public void setProcessingUnresolvedReferences( boolean processing ){
    	processingUnresolvedReferences = processing;
    }
    
    private List resolvedCrossReferences = Collections.EMPTY_LIST;
	/**
	 * @param references2
	 */
	public void setExtraReferences(List references ) {
		if( references != null && !references.isEmpty())
		{
			for( int i = 0; i < references.size(); ++i )
			{
				IASTReference r = (IASTReference)references.get(i);
				if( resolvedCrossReferences == Collections.EMPTY_LIST )
				    resolvedCrossReferences = new ArrayList( references.size() );
				resolvedCrossReferences.add( r );	
			}
		}
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTClassSpecifier#getFriends()
	 */
	public Iterator getFriends() {
		IDerivableContainerSymbol s = (IDerivableContainerSymbol) getSymbol();
		return new SymbolIterator( s.getFriends().iterator() );
	}
	
	
	private int startingLineNumber, startingOffset, endingLineNumber, endingOffset, nameStartOffset, nameEndOffset, nameLineNumber;
	private final char[] fn;
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#getStartingLine()
     */
    public int getStartingLine() {
    	return startingLineNumber;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#getEndingLine()
     */
    public int getEndingLine() {
    	return endingLineNumber;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement#getNameLineNumber()
     */
    public int getNameLineNumber() {
    	return nameLineNumber;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#setStartingOffset(int)
     */
    public void setStartingOffsetAndLineNumber(int offset, int lineNumber)
    {
    	startingOffset = offset;
    	startingLineNumber = lineNumber;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#setEndingOffset(int)
     */
    public void setEndingOffsetAndLineNumber(int offset, int lineNumber)
    {
    	endingOffset = offset;
    	endingLineNumber = lineNumber;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#getStartingOffset()
     */
    public int getStartingOffset()
    {
        return startingOffset;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#getEndingOffset()
     */
    public int getEndingOffset()
    {
        return endingOffset;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement#getNameOffset()
     */
    public int getNameOffset()
    {
    	return nameStartOffset;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement#setNameOffset(int)
     */
    public void setNameOffset(int o)
    {
        nameStartOffset = o;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement#getNameEndOffset()
     */
    public int getNameEndOffset()
    {
        return nameEndOffset;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement#setNameEndOffset(int)
     */
    public void setNameEndOffsetAndLineNumber(int offset, int lineNumber)
    {
    	nameEndOffset = offset;
    	nameLineNumber = lineNumber;
    }
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#getFilename()
	 */
	public char[] getFilename() {
		return fn;
	}
}
