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
package org.eclipse.cdt.internal.core.parser.ast.full;

import java.util.Iterator;

import org.eclipse.cdt.core.parser.ast.AccessVisibility;
import org.eclipse.cdt.core.parser.ast.ClassKind;
import org.eclipse.cdt.core.parser.ast.ClassNameType;
import org.eclipse.cdt.core.parser.ast.IASTTemplateDeclaration;
import org.eclipse.cdt.internal.core.parser.pst.IDerivableContainerSymbol;
import org.eclipse.cdt.internal.core.parser.pst.ISymbol;
import org.eclipse.cdt.internal.core.parser.pst.ParserSymbolTable;

/**
 * @author jcamelon
 *
 */
public class ASTClassSpecifier implements IASTFClassSpecifier, IPSTSymbolExtension {

	private final IDerivableContainerSymbol symbol;
	private final ClassKind classKind;
	private final ClassNameType type;
	private final String name;   

	public ASTClassSpecifier( IDerivableContainerSymbol symbol, String name, ClassNameType type, ClassKind kind )
	{
		this.name = name; 
		this.symbol = symbol;
		this.classKind = kind;
		this.type = type; 
		symbol.setASTNode( this );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTScope#getDeclarations()
	 */
	public Iterator getDeclarations() {
		return new ScopeIterator( symbol.getContainedSymbols());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTClassSpecifier#getClassNameType()
	 */
	public ClassNameType getClassNameType() {
		return type;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTClassSpecifier#getClassKind()
	 */
	public ClassKind getClassKind() {
		return classKind;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTClassSpecifier#getBaseClauses()
	 */
	public Iterator getBaseClauses() {
		return new BaseIterator( symbol.getParents() );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement#getName()
	 */
	public String getName() {
		return name;
	}

	private int nameOffset = 0, startingOffset = 0, endingOffset = 0; 
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement#getElementNameOffset()
	 */
	public int getElementNameOffset() {
		return nameOffset;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement#setNameOffset(int)
	 */
	public void setNameOffset(int o) {
		nameOffset = o;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTTemplatedDeclaration#getOwnerTemplateDeclaration()
	 */
	public IASTTemplateDeclaration getOwnerTemplateDeclaration() {
		if( getSymbol().getContainingSymbol().getType() == ParserSymbolTable.TypeInfo.t_template )
			return (IASTTemplateDeclaration)getSymbol().getContainingSymbol().getASTNode();
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#setStartingOffset(int)
	 */
	public void setStartingOffset(int o) {
		startingOffset = o;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#setEndingOffset(int)
	 */
	public void setEndingOffset(int o) {
		endingOffset = o;

	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#getElementStartingOffset()
	 */
	public int getElementStartingOffset() {
		return startingOffset;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#getElementEndingOffset()
	 */
	public int getElementEndingOffset() {
		return endingOffset;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.ast.IPSTSymbolExtension#getSymbol()
	 */
	public ISymbol getSymbol() {
		return symbol;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTClassSpecifier#getCurrentVisiblity()
	 */
	public AccessVisibility getCurrentVisibilityMode() {
		// TODO Auto-generated method stub
		return null;
	}

}
