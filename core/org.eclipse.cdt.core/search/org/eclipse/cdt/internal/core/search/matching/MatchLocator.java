/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 ******************************************************************************/
/*
 * Created on Jun 13, 2003
 */
package org.eclipse.cdt.internal.core.search.matching;

import org.eclipse.cdt.core.parser.IProblem;
import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.ast.*;

/**
 * @author aniefer
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class MatchLocator implements ISourceElementRequestor {

	/**
	 * 
	 */
	public MatchLocator() {
		super();
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptProblem(org.eclipse.cdt.core.parser.IProblem)
	 */
	public void acceptProblem(IProblem problem) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptMacro(org.eclipse.cdt.core.parser.ast.IASTMacro)
	 */
	public void acceptMacro(IASTMacro macro) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptVariable(org.eclipse.cdt.core.parser.ast.IASTVariable)
	 */
	public void acceptVariable(IASTVariable variable) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptFunctionDeclaration(org.eclipse.cdt.core.parser.ast.IASTFunction)
	 */
	public void acceptFunctionDeclaration(IASTFunction function) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptUsingDirective(org.eclipse.cdt.core.parser.ast.IASTUsingDirective)
	 */
	public void acceptUsingDirective(IASTUsingDirective usageDirective) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptUsingDeclaration(org.eclipse.cdt.core.parser.ast.IASTUsingDeclaration)
	 */
	public void acceptUsingDeclaration(IASTUsingDeclaration usageDeclaration) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptASMDefinition(org.eclipse.cdt.core.parser.ast.IASTASMDefinition)
	 */
	public void acceptASMDefinition(IASTASMDefinition asmDefinition) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptTypedef(org.eclipse.cdt.core.parser.ast.IASTTypedef)
	 */
	public void acceptTypedef(IASTTypedef typedef) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterEnumSpecifier(org.eclipse.cdt.core.parser.ast.IASTEnumSpecifier)
	 */
	//public void enterEnumSpecifier(IASTEnumSpecifier enumSpec) {
		// TODO Auto-generated method stub

	//}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptEnumerator(org.eclipse.cdt.core.parser.ast.IASTEnumerator)
	 */
	public void acceptEnumerator(IASTEnumerator enumerator) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitEnumSpecifier(org.eclipse.cdt.core.parser.ast.IASTEnumSpecifier)
	 */
	//public void exitEnumSpecifier(IASTEnumSpecifier enumSpec) {
		// TODO Auto-generated method stub

	//}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterFunctionBody(org.eclipse.cdt.core.parser.ast.IASTFunction)
	 */
	public void enterFunctionBody(IASTFunction function) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitFunctionBody(org.eclipse.cdt.core.parser.ast.IASTFunction)
	 */
	public void exitFunctionBody(IASTFunction function) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterCompilationUnit(org.eclipse.cdt.core.parser.ast.IASTCompilationUnit)
	 */
	public void enterCompilationUnit(IASTCompilationUnit compilationUnit) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterInclusion(org.eclipse.cdt.core.parser.ast.IASTInclusion)
	 */
	public void enterInclusion(IASTInclusion inclusion) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterNamespaceDefinition(org.eclipse.cdt.core.parser.ast.IASTNamespaceDefinition)
	 */
	public void enterNamespaceDefinition(IASTNamespaceDefinition namespaceDefinition) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterClassSpecifier(org.eclipse.cdt.core.parser.ast.IASTClassSpecifier)
	 */
	public void enterClassSpecifier(IASTClassSpecifier classSpecification) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterLinkageSpecification(org.eclipse.cdt.core.parser.ast.IASTLinkageSpecification)
	 */
	public void enterLinkageSpecification(IASTLinkageSpecification linkageSpec) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterTemplateDeclaration(org.eclipse.cdt.core.parser.ast.IASTTemplateDeclaration)
	 */
	public void enterTemplateDeclaration(IASTTemplateDeclaration declaration) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterTemplateSpecialization(org.eclipse.cdt.core.parser.ast.IASTTemplateSpecialization)
	 */
	public void enterTemplateSpecialization(IASTTemplateSpecialization specialization) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterTemplateExplicitInstantiation(org.eclipse.cdt.core.parser.ast.IASTTemplateInstantiation)
	 */
	public void enterTemplateExplicitInstantiation(IASTTemplateInstantiation instantiation) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptMethodDeclaration(org.eclipse.cdt.core.parser.ast.IASTMethod)
	 */
	public void acceptMethodDeclaration(IASTMethod method) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterMethodBody(org.eclipse.cdt.core.parser.ast.IASTMethod)
	 */
	public void enterMethodBody(IASTMethod method) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitMethodBody(org.eclipse.cdt.core.parser.ast.IASTMethod)
	 */
	public void exitMethodBody(IASTMethod method) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptField(org.eclipse.cdt.core.parser.ast.IASTField)
	 */
	public void acceptField(IASTField field) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptConstructor(org.eclipse.cdt.core.parser.ast.IASTConstructor)
	 */
	public void acceptConstructor(IASTConstructor constructor) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitTemplateDeclaration(org.eclipse.cdt.core.parser.ast.IASTTemplateDeclaration)
	 */
	public void exitTemplateDeclaration(IASTTemplateDeclaration declaration) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitTemplateSpecialization(org.eclipse.cdt.core.parser.ast.IASTTemplateSpecialization)
	 */
	public void exitTemplateSpecialization(IASTTemplateSpecialization specialization) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitTemplateExplicitInstantiation(org.eclipse.cdt.core.parser.ast.IASTTemplateInstantiation)
	 */
	public void exitTemplateExplicitInstantiation(IASTTemplateInstantiation instantiation) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitLinkageSpecification(org.eclipse.cdt.core.parser.ast.IASTLinkageSpecification)
	 */
	public void exitLinkageSpecification(IASTLinkageSpecification linkageSpec) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitClassSpecifier(org.eclipse.cdt.core.parser.ast.IASTClassSpecifier)
	 */
	public void exitClassSpecifier(IASTClassSpecifier classSpecification) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitNamespaceDefinition(org.eclipse.cdt.core.parser.ast.IASTNamespaceDefinition)
	 */
	public void exitNamespaceDefinition(IASTNamespaceDefinition namespaceDefinition) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitInclusion(org.eclipse.cdt.core.parser.ast.IASTInclusion)
	 */
	public void exitInclusion(IASTInclusion inclusion) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitCompilationUnit(org.eclipse.cdt.core.parser.ast.IASTCompilationUnit)
	 */
	public void exitCompilationUnit(IASTCompilationUnit compilationUnit) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptEnumerationSpecifier(org.eclipse.cdt.core.parser.ast.IASTEnumerationSpecifier)
	 */
	public void acceptEnumerationSpecifier(IASTEnumerationSpecifier enumeration) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptClassReference(org.eclipse.cdt.core.parser.ast.IASTClassSpecifier, int)
	 */
	public void acceptClassReference(IASTClassSpecifier classSpecifier, int referenceOffset) {
		// TODO Auto-generated method stub
		
	}

}
