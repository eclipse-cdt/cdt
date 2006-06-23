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
package org.eclipse.cdt.internal.core.parser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.eclipse.cdt.core.parser.*;
import org.eclipse.cdt.core.parser.IQuickParseCallback;
import org.eclipse.cdt.core.parser.ast.ASTNotImplementedException;
import org.eclipse.cdt.core.parser.ast.IASTCompilationUnit;
import org.eclipse.cdt.core.parser.ast.IASTFunction;
import org.eclipse.cdt.core.parser.ast.IASTInclusion;
import org.eclipse.cdt.core.parser.ast.IASTMacro;
import org.eclipse.cdt.core.parser.ast.IASTMethod;
import org.eclipse.cdt.core.parser.ast.IASTOffsetableElement;


public class QuickParseCallback extends NullSourceElementRequestor implements IQuickParseCallback
{
	protected IASTCompilationUnit compUnit = null;
	protected List inclusions = new ArrayList(); 
	protected List macros = new ArrayList(); 
	protected boolean hasNoProblems = true;
	
	public Iterator getInclusions()
	{
		return inclusions.iterator(); 
	}
	
	public Iterator getMacros()
	{
		return macros.iterator();
	}
	
	public Iterator getDeclarations(){
		try{
			return compUnit.getDeclarations();
		}
		catch (ASTNotImplementedException ne )
		{
			return null;
		}
	}
		
	public void exitMethodBody( IASTMethod method )
	{
		method.setHasFunctionBody( true );
	}

	
	public void exitFunctionBody( IASTFunction function )
	{
		function.setHasFunctionBody( true );
	}
	
	
	
	public void exitCompilationUnit( IASTCompilationUnit compilationUnit )
	{
		this.compUnit = compilationUnit;
	}
	
	public void exitInclusion( IASTInclusion inclusion )
	{
		inclusions.add( inclusion );
	}
	
	public void acceptMacro( IASTMacro macro )
	{
		macros.add( macro );
	}
	
    /**
     * @return
     */
    public IASTCompilationUnit getCompilationUnit()
    {
        return compUnit;
    }

	public class OffsetableIterator implements Iterator 
	{		
		private Iterator declarationIter; 
		private final Iterator inclusionIter; 
		private final Iterator macroIter; 
	
		private IASTOffsetableElement currentMacro = null, currentInclusion= null, currentDeclaration= null; 
	
		public OffsetableIterator()
		{
            declarationIter = getDeclarations();
			inclusionIter = getInclusions();
			macroIter = getMacros();		
			updateInclusionIterator(); 
			updateMacroIterator(); 
			updateDeclarationIterator();
		}
	
		private Object updateDeclarationIterator()
		{
			Object offsetable = currentDeclaration;
			if(declarationIter != null)
				currentDeclaration = ( declarationIter.hasNext() ) ? (IASTOffsetableElement)declarationIter.next() : null; 
			return offsetable; 
		}

		private Object updateMacroIterator()
		{
			Object offsetable = currentMacro; 
			currentMacro = ( macroIter.hasNext() ) ? (IASTOffsetableElement)macroIter.next() : null; 
			return offsetable;
		}
	
		private Object updateInclusionIterator()
		{
			Object offsetable = currentInclusion;
			currentInclusion = ( inclusionIter.hasNext() ) ? (IASTOffsetableElement)inclusionIter.next() : null;
			return offsetable;
		}
		/* (non-Javadoc)
		 * @see java.util.Iterator#hasNext()
		 */
		public boolean hasNext() {
			return (( currentMacro == null && currentInclusion == null && currentDeclaration == null ) ? 
				false : true);
		}

		/* (non-Javadoc)
		 * @see java.util.Iterator#next()
		 */
		public Object next() {
			// case 1: all are null 
			if( ! hasNext() ) 
				throw new NoSuchElementException();
			
			// case 2: two of three are null 
			if( currentMacro == null && currentInclusion == null )
				return updateDeclarationIterator();
			if( currentDeclaration == null && currentInclusion == null )
				return updateMacroIterator();
			if( currentMacro == null && currentDeclaration == null )
				return updateInclusionIterator();
		
			// case 3: 1 is null
			if( currentMacro == null )
			{
				if( currentDeclaration.getStartingOffset() < currentInclusion.getStartingOffset() )
					return updateDeclarationIterator(); 
				return updateInclusionIterator();
			}

			if( currentInclusion == null )
			{
				if( currentDeclaration.getStartingOffset() < currentMacro.getStartingOffset() )
					return updateDeclarationIterator(); 
				return updateMacroIterator();
			}

			if( currentDeclaration == null )
			{
				if( currentInclusion.getStartingOffset() < currentMacro.getStartingOffset() )
					return updateInclusionIterator(); 
				return updateMacroIterator(); 
			}
		
			// case 4: none are null 
			if( currentInclusion.getStartingOffset() < currentMacro.getStartingOffset() && 
				currentInclusion.getStartingOffset() < currentDeclaration.getStartingOffset() ) 
				return updateInclusionIterator(); 
			
			if( currentMacro.getStartingOffset() < currentInclusion.getStartingOffset() && 
				currentMacro.getStartingOffset() < currentDeclaration.getStartingOffset() ) 
				return updateMacroIterator();
			// only remaining case
			return updateDeclarationIterator();
		}

		/* (non-Javadoc)
		 * @see java.util.Iterator#remove()
		 */
		public void remove() {
			throw new UnsupportedOperationException( ParserMessages.getString("QuickParseCallback.exception.constIterator"));  //$NON-NLS-1$
		}
	}

	public Iterator iterateOffsetableElements()
	{
		return new OffsetableIterator();
	}	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptProblem(org.eclipse.cdt.core.parser.IProblem)
	 */
	public boolean acceptProblem(IProblem problem) {
		setHasNoProblems(false);
		return super.acceptProblem(problem);
	}
	/**
	 * @return Returns the hasProblems.
	 */
	public boolean hasNoProblems() {
		return hasNoProblems;
	}
	/**
	 * @param hasProblems The hasProblems to set.
	 */
	public void setHasNoProblems(boolean hasProblems) {
		this.hasNoProblems = hasProblems;
	}
}
