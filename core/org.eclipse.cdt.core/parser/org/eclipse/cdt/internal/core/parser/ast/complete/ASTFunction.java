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
import org.eclipse.cdt.internal.core.parser.ast.ASTQualifiedNamedElement;
import org.eclipse.cdt.internal.core.parser.ast.NamedOffsets;
import org.eclipse.cdt.internal.core.parser.pst.IParameterizedSymbol;
import org.eclipse.cdt.internal.core.parser.pst.TypeInfo;

/**
 * @author jcamelon
 *
 */
public class ASTFunction extends ASTScope implements IASTFunction
{
	private final boolean previouslyDeclared;
    private boolean hasFunctionBody = false;
    private final IASTTemplate ownerTemplate;
    private final IASTAbstractDeclaration returnType;
    private final IASTExceptionSpecification exception;
    private NamedOffsets offsets = new NamedOffsets(); 
	private final ASTQualifiedNamedElement qualifiedName;
	private final List parameters;
	protected final ASTReferenceStore references;
	private List declarations = new ArrayList();	
    /**
     * @param symbol
     * @param parameters
     * @param returnType
     * @param exception
     * @param startOffset
     * @param nameOffset
     * @param ownerTemplate
     * @param references
     */
    public ASTFunction(IParameterizedSymbol symbol, int nameEndOffset, List parameters, IASTAbstractDeclaration returnType, IASTExceptionSpecification exception, int startOffset, int startingLine, int nameOffset, int nameLine, IASTTemplate ownerTemplate, List references, boolean previouslyDeclared, boolean hasFunctionTryBlock )
    {
    	super( symbol );
    	this.parameters = parameters;
    	this.returnType = returnType; 
    	this.exception = exception; 
    	setStartingOffsetAndLineNumber(startOffset, startingLine);
    	setNameOffset(nameOffset);
    	setNameEndOffsetAndLineNumber(nameEndOffset, nameLine);
    	this.ownerTemplate = ownerTemplate;
    	this.references = new ASTReferenceStore( references );
    	qualifiedName = new ASTQualifiedNamedElement( getOwnerScope(), symbol.getName() );
    	this.previouslyDeclared =previouslyDeclared;
    	this.hasFunctionTryBlock = hasFunctionTryBlock;
    }


    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFunction#isInline()
     */
    public boolean isInline()
    {
        return symbol.getTypeInfo().checkBit( TypeInfo.isInline );
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFunction#isFriend()
     */
    public boolean isFriend()
    {
        return symbol.getTypeInfo().checkBit( TypeInfo.isFriend );
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFunction#isStatic()
     */
    public boolean isStatic()
    {
		return symbol.getTypeInfo().checkBit( TypeInfo.isStatic );
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement#getName()
     */
    public String getName()
    {
        return symbol.getName();
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
        hasFunctionBody = true;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFunction#hasFunctionBody()
     */
    public boolean hasFunctionBody()
    {
        return hasFunctionBody;
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
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#setStartingOffset(int)
     */
    public void setStartingOffsetAndLineNumber(int offset, int lineNumber)
    {
        offsets.setStartingOffsetAndLineNumber(offset, lineNumber);
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#setEndingOffset(int)
     */
    public void setEndingOffsetAndLineNumber(int offset, int lineNumber)
    {
        offsets.setEndingOffsetAndLineNumber(offset, lineNumber);
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
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#acceptElement(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void acceptElement(ISourceElementRequestor requestor)
    {
        try
        {
            requestor.acceptFunctionDeclaration(this);
        }
        catch (Exception e)
        {
            /* do nothing */
        }
        functionCallbacks(requestor);
    }
    
    protected  void functionCallbacks(ISourceElementRequestor requestor)
    {
        references.processReferences(requestor);
        processParameterInitializersAndArrayMods(requestor);
        if( getReturnType() != null )
        	getReturnType().acceptElement(requestor);
        if( getExceptionSpec() != null )
        	getExceptionSpec().acceptElement(requestor);
    }
    /**
     * @param requestor
     */
    protected void processParameterInitializersAndArrayMods(ISourceElementRequestor requestor)
    {
        Iterator i = parameters.iterator();
        while( i.hasNext() )
        {
        	IASTParameterDeclaration parm = (IASTParameterDeclaration)i.next();
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
	public void setNameEndOffsetAndLineNumber(int offset, int lineNumber)
	{
		offsets.setNameEndOffsetAndLineNumber(offset, lineNumber);
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
    	return declarations.iterator();
    }
    public void addDeclaration(IASTDeclaration declaration)
    {
    	declarations.add(declaration);
    }


	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTFunction#takesVarArgs()
	 */
	public boolean takesVarArgs() {
		return ((IParameterizedSymbol)getSymbol()).hasVariableArgs();
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
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement#getNameLineNumber()
	 */
	public int getNameLineNumber() {
		return offsets.getNameLineNumber();
	}
}
