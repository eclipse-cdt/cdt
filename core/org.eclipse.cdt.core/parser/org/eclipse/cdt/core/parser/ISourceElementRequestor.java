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
package org.eclipse.cdt.core.parser;

import org.eclipse.cdt.core.parser.ast.IASTASMDefinition;
import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTCompilationUnit;
import org.eclipse.cdt.core.parser.ast.IASTConstructor;
import org.eclipse.cdt.core.parser.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTField;
import org.eclipse.cdt.core.parser.ast.IASTFunction;
import org.eclipse.cdt.core.parser.ast.IASTInclusion;
import org.eclipse.cdt.core.parser.ast.IASTLinkageSpecification;
import org.eclipse.cdt.core.parser.ast.IASTMacro;
import org.eclipse.cdt.core.parser.ast.IASTMethod;
import org.eclipse.cdt.core.parser.ast.IASTNamespaceDefinition;
import org.eclipse.cdt.core.parser.ast.IASTTemplateDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTTemplateInstantiation;
import org.eclipse.cdt.core.parser.ast.IASTTemplateSpecialization;
import org.eclipse.cdt.core.parser.ast.IASTTypedef;
import org.eclipse.cdt.core.parser.ast.IASTUsingDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTUsingDirective;
import org.eclipse.cdt.core.parser.ast.IASTVariable;

/**
 * @author jcamelon
 *
 */
public interface ISourceElementRequestor {
	
	public void acceptProblem( IProblem problem );

	public void acceptMacro( IASTMacro macro );
	public void acceptVariable( IASTVariable variable );
	public void acceptFunctionDeclaration( IASTFunction function );
	public void acceptUsingDirective( IASTUsingDirective usageDirective );
	public void acceptUsingDeclaration( IASTUsingDeclaration usageDeclaration );
	public void acceptASMDefinition( IASTASMDefinition asmDefinition );
	public void acceptTypedef( IASTTypedef typedef );
	public void acceptEnumerationSpecifier( IASTEnumerationSpecifier enumeration );

	public void enterFunctionBody( IASTFunction function );
	public void exitFunctionBody( IASTFunction function );

	public void enterCompilationUnit( IASTCompilationUnit compilationUnit ); 
	public void enterInclusion( IASTInclusion inclusion ); 
	public void enterNamespaceDefinition( IASTNamespaceDefinition namespaceDefinition );
	public void enterClassSpecifier( IASTClassSpecifier classSpecification );
	public void enterLinkageSpecification( IASTLinkageSpecification linkageSpec );
	
	public void enterTemplateDeclaration( IASTTemplateDeclaration declaration );
	public void enterTemplateSpecialization( IASTTemplateSpecialization specialization );
	public void enterTemplateExplicitInstantiation( IASTTemplateInstantiation instantiation );
	
	public void acceptMethodDeclaration( IASTMethod method );
	public void enterMethodBody( IASTMethod method );
	public void exitMethodBody( IASTMethod method );
	public void acceptField( IASTField field );
	public void acceptConstructor( IASTConstructor constructor );

	public void acceptClassReference( IASTClassSpecifier classSpecifier, int referenceOffset );
	
	public void exitTemplateDeclaration( IASTTemplateDeclaration declaration );
	public void exitTemplateSpecialization( IASTTemplateSpecialization specialization );
	public void exitTemplateExplicitInstantiation( IASTTemplateInstantiation instantiation );
	
	public void exitLinkageSpecification( IASTLinkageSpecification linkageSpec );
	public void exitClassSpecifier( IASTClassSpecifier classSpecification ); 	 
	public void exitNamespaceDefinition( IASTNamespaceDefinition namespaceDefinition ); 
	public void exitInclusion( IASTInclusion inclusion ); 
	public void exitCompilationUnit( IASTCompilationUnit compilationUnit );
}
