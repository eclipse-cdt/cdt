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
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.ast.IASTAbstractDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTArrayModifier;
import org.eclipse.cdt.core.parser.ast.IASTCodeScope;
import org.eclipse.cdt.core.parser.ast.IASTDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTExceptionSpecification;
import org.eclipse.cdt.core.parser.ast.IASTFunction;
import org.eclipse.cdt.core.parser.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTTemplate;
import org.eclipse.cdt.internal.core.parser.Parser;
import org.eclipse.cdt.internal.core.parser.ast.ASTQualifiedNamedElement;
import org.eclipse.cdt.internal.core.parser.pst.IParameterizedSymbol;
import org.eclipse.cdt.internal.core.parser.pst.ITypeInfo;

/**
 * @author jcamelon
 *
 */
public class ASTFunction extends ASTScope implements IASTFunction
{
	private final boolean previouslyDeclared;
    private boolean hasFunctionBody = false;
    private final boolean isFriendDeclaration;
    private final IASTTemplate ownerTemplate;
    private final IASTAbstractDeclaration returnType;
    private final IASTExceptionSpecification exception;
	private final ASTQualifiedNamedElement qualifiedName;
	private final List parameters;
	protected List references;
	private List declarations = null;	
    private final char [] fn;
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#getFilename()
	 */
	public char[] getFilename() {
		return fn;
	}

    /**
     * @param symbol
     * @param parameters
     * @param returnType
     * @param exception
     * @param startOffset
     * @param nameOffset
     * @param ownerTemplate
     * @param references
     * @param filename
     */
    public ASTFunction(IParameterizedSymbol symbol, int nameEndOffset, List parameters, IASTAbstractDeclaration returnType, IASTExceptionSpecification exception, int startOffset, int startingLine, int nameOffset, int nameLine, IASTTemplate ownerTemplate, List references, boolean previouslyDeclared, boolean hasFunctionTryBlock, boolean isFriend, char[] filename )
    {
    	super( symbol );
    	this.parameters = parameters;
    	this.returnType = returnType; 
    	this.exception = exception; 
    	setStartingOffsetAndLineNumber(startOffset, startingLine);
    	setNameOffset(nameOffset);
    	setNameEndOffsetAndLineNumber(nameEndOffset, nameLine);
    	this.ownerTemplate = ownerTemplate;
    	this.references = references;
    	qualifiedName = new ASTQualifiedNamedElement( getOwnerScope(), symbol.getName() );
    	this.previouslyDeclared =previouslyDeclared;
    	this.hasFunctionTryBlock = hasFunctionTryBlock;
    	this.isFriendDeclaration = isFriend;
    	fn = filename;
    }


    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFunction#isInline()
     */
    public boolean isInline()
    {
        return symbol.getTypeInfo().checkBit( ITypeInfo.isInline );
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFunction#isFriend()
     */
    public boolean isFriend()
    {
    	return isFriendDeclaration;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFunction#isStatic()
     */
    public boolean isStatic()
    {
		return symbol.getTypeInfo().checkBit( ITypeInfo.isStatic );
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement#getName()
     */
    public String getName()
    {
        return String.valueOf(symbol.getName());
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
        return parameters.iterator();
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFunction#getExceptionSpec()
     */
    public IASTExceptionSpecification getExceptionSpec()
    {
        return exception;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFunction#setHasFunctionBody(boolean)
     */
    public void setHasFunctionBody(boolean b)
    {
        hasFunctionBody = b;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFunction#hasFunctionBody()
     */
    public boolean hasFunctionBody()
    {
        return hasFunctionBody;
    }
 
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTTemplatedDeclaration#getOwnerTemplateDeclaration()
     */
    public IASTTemplate getOwnerTemplateDeclaration()
    {
        return ownerTemplate;
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
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#acceptElement(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void acceptElement(ISourceElementRequestor requestor)
    {
        try
        {
        	if( isFriend() )
        		requestor.acceptFriendDeclaration(this);
        	else
        		requestor.acceptFunctionDeclaration(this);
        }
        catch (Exception e)
        {
            /* do nothing */
        }
        functionCallbacks(requestor);
    }
    
    protected  void functionCallbacks(ISourceElementRequestor requestor )
    {
        Parser.processReferences(references, requestor);
        references = null;
        processParameterInitializersAndArrayMods(requestor);
        if( getReturnType() != null )
        	getReturnType().acceptElement(requestor);
    }
    /**
     * @param requestor
     * @param manager
     */
    protected void processParameterInitializersAndArrayMods(ISourceElementRequestor requestor )
    {
        int size = parameters.size();
        for( int i = 0; i < size; i++ )
        {
        	IASTParameterDeclaration parm = (IASTParameterDeclaration)parameters.get(i);
        	if( parm.getDefaultValue() != null )
        		parm.getDefaultValue().acceptElement(requestor);
        	Iterator arrays = parm.getArrayModifiers();
        	while( arrays.hasNext() )
        	{
        		((IASTArrayModifier)arrays.next()).acceptElement(requestor);
        	}
        }
    }


    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#enterScope(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void enterScope(ISourceElementRequestor requestor)
    {
		try
        {
            requestor.enterFunctionBody( this );
        }
        catch (Exception e)
        {
            /* do nothing */
        }
		functionCallbacks( requestor );
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#exitScope(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void exitScope(ISourceElementRequestor requestor)
    {
        try
        {
            requestor.exitFunctionBody( this );
        }
        catch (Exception e)
        {
            /* do nothing */
        }
    }


    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTCodeScope#getOwnerCodeScope()
     */
    public IASTCodeScope getOwnerCodeScope()
    {
        return ( getSymbol().getContainingSymbol().getASTExtension().getPrimaryDeclaration() ) instanceof IASTCodeScope ? 
			(IASTCodeScope) getSymbol().getContainingSymbol().getASTExtension().getPrimaryDeclaration()  : null;
    }


    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFunction#previouslyDeclared()
     */
    public boolean previouslyDeclared()
    {
        return previouslyDeclared;
    }

	private boolean hasFunctionTryBlock = false;
	
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFunction#setHasFunctionTryBlock(boolean)
     */
    public void setHasFunctionTryBlock(boolean b)
    {
		hasFunctionTryBlock = b;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFunction#hasFunctionTryBlock()
     */
    public boolean hasFunctionTryBlock()
    {
        return hasFunctionTryBlock;
    }

    public Iterator getDeclarations()
    {
    	if(declarations != null)
    		return declarations.iterator();
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


	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTFunction#takesVarArgs()
	 */
	public boolean takesVarArgs() {
		return ((IParameterizedSymbol)getSymbol()).hasVariableArgs();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTCodeScope#getContainingFunction()
	 */
	public IASTFunction getContainingFunction() {
		return this;
	}
	
	private int startingLineNumber, startingOffset, endingLineNumber, endingOffset, nameStartOffset, nameEndOffset, nameLineNumber;
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
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement#getNameCharArray()
     */
    public char[] getNameCharArray() {
        return symbol.getName();
    }
}
