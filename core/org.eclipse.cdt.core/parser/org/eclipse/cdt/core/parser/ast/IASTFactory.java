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
package org.eclipse.cdt.core.parser.ast;

import org.eclipse.cdt.internal.core.parser.TokenDuple;
import org.eclipse.cdt.internal.core.parser.Parser.Backtrack;

/**
 * @author jcamelon
 *
 */
public interface IASTFactory {
	
	public IASTMacro createMacro( String name, int startingOffset, int endingOffset, int nameOffset );
	public IASTInclusion createInclusion( String name, String fileName, boolean local, int startingOffset, int endingOffset, int nameOffset );

	public IASTUsingDirective createUsingDirective(
		IASTScope scope,
		TokenDuple duple)
		throws Backtrack;
		
	public IASTUsingDeclaration createUsingDeclaration( 
		IASTScope scope, 
		boolean isTypeName, 
		TokenDuple name );
		
		
	public IASTASMDefinition createASMDefinition(
		IASTScope scope,
		String assembly,
		int first,
		int last);
	
	public IASTNamespaceDefinition createNamespaceDefinition(
		IASTScope scope,
		String identifier,
		int startingOffset, int nameOffset);
	
	public IASTCompilationUnit createCompilationUnit();
	
	public IASTLinkageSpecification createLinkageSpecification(IASTScope scope, String spec);
	
	public IASTClassSpecifier createClassSpecifier( IASTScope scope,
		String name,  
		ClassKind kind, 
		ClassNameType type, 
		AccessVisibility access, 
		IASTTemplateDeclaration ownerTemplateDeclaration, int startingOffset, int nameOffset );

	/**
	 * @param astClassSpec
	 * @param isVirtual
	 * @param visibility
	 * @param string
	 */
	public void addBaseSpecifier(IASTClassSpecifier astClassSpec, boolean isVirtual, AccessVisibility visibility, String string);

    public IASTElaboratedTypeSpecifier createElaboratedTypeSpecifier(ClassKind elaboratedClassKind, String typeName, int startingOffset, int endOffset );
    public IASTEnumerationSpecifier createEnumerationSpecifier(String name, int startingOffset, int nameOffset );
    public void addEnumerator(IASTEnumerationSpecifier enumeration, String string, int startingOffset, int endingOffset);
	
}