/*******************************************************************************
 * 
 * Copyright (c) 2002, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - initial implementation base on code from rational/IBM
 ******************************************************************************/

package org.eclipse.cdt.internal.ui.compare;

import java.util.Iterator;

import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.DefaultProblemHandler;
import org.eclipse.cdt.core.parser.IProblem;
import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ParserUtil;
import org.eclipse.cdt.core.parser.ast.IASTASMDefinition;
import org.eclipse.cdt.core.parser.ast.IASTAbstractTypeSpecifierDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTClassReference;
import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTCodeScope;
import org.eclipse.cdt.core.parser.ast.IASTCompilationUnit;
import org.eclipse.cdt.core.parser.ast.IASTDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTEnumerationReference;
import org.eclipse.cdt.core.parser.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTEnumeratorReference;
import org.eclipse.cdt.core.parser.ast.IASTField;
import org.eclipse.cdt.core.parser.ast.IASTFieldReference;
import org.eclipse.cdt.core.parser.ast.IASTFunction;
import org.eclipse.cdt.core.parser.ast.IASTFunctionReference;
import org.eclipse.cdt.core.parser.ast.IASTInclusion;
import org.eclipse.cdt.core.parser.ast.IASTLinkageSpecification;
import org.eclipse.cdt.core.parser.ast.IASTMacro;
import org.eclipse.cdt.core.parser.ast.IASTMethod;
import org.eclipse.cdt.core.parser.ast.IASTMethodReference;
import org.eclipse.cdt.core.parser.ast.IASTNamespaceDefinition;
import org.eclipse.cdt.core.parser.ast.IASTNamespaceReference;
import org.eclipse.cdt.core.parser.ast.IASTParameterReference;
import org.eclipse.cdt.core.parser.ast.IASTTemplateDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTTemplateInstantiation;
import org.eclipse.cdt.core.parser.ast.IASTTemplateParameterReference;
import org.eclipse.cdt.core.parser.ast.IASTTemplateSpecialization;
import org.eclipse.cdt.core.parser.ast.IASTTypedefDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTTypedefReference;
import org.eclipse.cdt.core.parser.ast.IASTUsingDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTUsingDirective;
import org.eclipse.cdt.core.parser.ast.IASTVariable;
import org.eclipse.cdt.core.parser.ast.IASTVariableReference;

/**
 *
 */
public class SourceElementRequestorAdapter implements ISourceElementRequestor {
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptAbstractTypeSpecDeclaration(org.eclipse.cdt.core.parser.ast.IASTAbstractTypeSpecifierDeclaration)
	 */
	public void acceptAbstractTypeSpecDeclaration(IASTAbstractTypeSpecifierDeclaration abstractDeclaration) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptASMDefinition(org.eclipse.cdt.core.parser.ast.IASTASMDefinition)
	 */
	public void acceptASMDefinition(IASTASMDefinition asmDefinition) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptClassReference(org.eclipse.cdt.core.parser.ast.IASTClassReference)
	 */
	public void acceptClassReference(IASTClassReference reference) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptElaboratedForewardDeclaration(org.eclipse.cdt.core.parser.ast.IASTElaboratedTypeSpecifier)
	 */
	public void acceptElaboratedForewardDeclaration(IASTElaboratedTypeSpecifier elaboratedType) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptEnumerationReference(org.eclipse.cdt.core.parser.ast.IASTEnumerationReference)
	 */
	public void acceptEnumerationReference(IASTEnumerationReference reference) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptEnumerationSpecifier(org.eclipse.cdt.core.parser.ast.IASTEnumerationSpecifier)
	 */
	public void acceptEnumerationSpecifier(IASTEnumerationSpecifier enumeration) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptField(org.eclipse.cdt.core.parser.ast.IASTField)
	 */
	public void acceptField(IASTField field) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptFieldReference(org.eclipse.cdt.core.parser.ast.IASTFieldReference)
	 */
	public void acceptFieldReference(IASTFieldReference reference) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptFunctionDeclaration(org.eclipse.cdt.core.parser.ast.IASTFunction)
	 */
	public void acceptFunctionDeclaration(IASTFunction function) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptFunctionReference(org.eclipse.cdt.core.parser.ast.IASTFunctionReference)
	 */
	public void acceptFunctionReference(IASTFunctionReference reference) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptMacro(org.eclipse.cdt.core.parser.ast.IASTMacro)
	 */
	public void acceptMacro(IASTMacro macro) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptMethodDeclaration(org.eclipse.cdt.core.parser.ast.IASTMethod)
	 */
	public void acceptMethodDeclaration(IASTMethod method) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptMethodReference(org.eclipse.cdt.core.parser.ast.IASTMethodReference)
	 */
	public void acceptMethodReference(IASTMethodReference reference) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptNamespaceReference(org.eclipse.cdt.core.parser.ast.IASTNamespaceReference)
	 */
	public void acceptNamespaceReference(IASTNamespaceReference reference) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptProblem(org.eclipse.cdt.core.parser.IProblem)
	 */
	public boolean acceptProblem(IProblem problem) {
		return DefaultProblemHandler.ruleOnProblem( problem, ParserMode.QUICK_PARSE );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptTypedefDeclaration(org.eclipse.cdt.core.parser.ast.IASTTypedefDeclaration)
	 */
	public void acceptTypedefDeclaration(IASTTypedefDeclaration typedef) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptTypedefReference(org.eclipse.cdt.core.parser.ast.IASTTypedefReference)
	 */
	public void acceptTypedefReference(IASTTypedefReference reference) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptUsingDeclaration(org.eclipse.cdt.core.parser.ast.IASTUsingDeclaration)
	 */
	public void acceptUsingDeclaration(IASTUsingDeclaration usageDeclaration) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptUsingDirective(org.eclipse.cdt.core.parser.ast.IASTUsingDirective)
	 */
	public void acceptUsingDirective(IASTUsingDirective usageDirective) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptVariable(org.eclipse.cdt.core.parser.ast.IASTVariable)
	 */
	public void acceptVariable(IASTVariable variable) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptVariableReference(org.eclipse.cdt.core.parser.ast.IASTVariableReference)
	 */
	public void acceptVariableReference(IASTVariableReference reference) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterClassSpecifier(org.eclipse.cdt.core.parser.ast.IASTClassSpecifier)
	 */
	public void enterClassSpecifier(IASTClassSpecifier classSpecification) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterCompilationUnit(org.eclipse.cdt.core.parser.ast.IASTCompilationUnit)
	 */
	public void enterCompilationUnit(IASTCompilationUnit compilationUnit) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterFunctionBody(org.eclipse.cdt.core.parser.ast.IASTFunction)
	 */
	public void enterFunctionBody(IASTFunction function) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterInclusion(org.eclipse.cdt.core.parser.ast.IASTInclusion)
	 */
	public void enterInclusion(IASTInclusion inclusion) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterLinkageSpecification(org.eclipse.cdt.core.parser.ast.IASTLinkageSpecification)
	 */
	public void enterLinkageSpecification(IASTLinkageSpecification linkageSpec) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterMethodBody(org.eclipse.cdt.core.parser.ast.IASTMethod)
	 */
	public void enterMethodBody(IASTMethod method) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterNamespaceDefinition(org.eclipse.cdt.core.parser.ast.IASTNamespaceDefinition)
	 */
	public void enterNamespaceDefinition(IASTNamespaceDefinition namespaceDefinition) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterTemplateDeclaration(org.eclipse.cdt.core.parser.ast.IASTTemplateDeclaration)
	 */
	public void enterTemplateDeclaration(IASTTemplateDeclaration declaration) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterTemplateInstantiation(org.eclipse.cdt.core.parser.ast.IASTTemplateInstantiation)
	 */
	public void enterTemplateInstantiation(IASTTemplateInstantiation instantiation) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterTemplateSpecialization(org.eclipse.cdt.core.parser.ast.IASTTemplateSpecialization)
	 */
	public void enterTemplateSpecialization(IASTTemplateSpecialization specialization) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitClassSpecifier(org.eclipse.cdt.core.parser.ast.IASTClassSpecifier)
	 */
	public void exitClassSpecifier(IASTClassSpecifier classSpecification) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitCompilationUnit(org.eclipse.cdt.core.parser.ast.IASTCompilationUnit)
	 */
	public void exitCompilationUnit(IASTCompilationUnit compilationUnit) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitFunctionBody(org.eclipse.cdt.core.parser.ast.IASTFunction)
	 */
	public void exitFunctionBody(IASTFunction function) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitInclusion(org.eclipse.cdt.core.parser.ast.IASTInclusion)
	 */
	public void exitInclusion(IASTInclusion inclusion) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitLinkageSpecification(org.eclipse.cdt.core.parser.ast.IASTLinkageSpecification)
	 */
	public void exitLinkageSpecification(IASTLinkageSpecification linkageSpec) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitMethodBody(org.eclipse.cdt.core.parser.ast.IASTMethod)
	 */
	public void exitMethodBody(IASTMethod method) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitNamespaceDefinition(org.eclipse.cdt.core.parser.ast.IASTNamespaceDefinition)
	 */
	public void exitNamespaceDefinition(IASTNamespaceDefinition namespaceDefinition) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitTemplateDeclaration(org.eclipse.cdt.core.parser.ast.IASTTemplateDeclaration)
	 */
	public void exitTemplateDeclaration(IASTTemplateDeclaration declaration) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitTemplateExplicitInstantiation(org.eclipse.cdt.core.parser.ast.IASTTemplateInstantiation)
	 */
	public void exitTemplateExplicitInstantiation(IASTTemplateInstantiation instantiation) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitTemplateSpecialization(org.eclipse.cdt.core.parser.ast.IASTTemplateSpecialization)
	 */
	public void exitTemplateSpecialization(IASTTemplateSpecialization specialization) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterCodeBlock(org.eclipse.cdt.core.parser.ast.IASTScope)
	 */
	public void enterCodeBlock(IASTCodeScope scope) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitCodeBlock(org.eclipse.cdt.core.parser.ast.IASTScope)
	 */
	public void exitCodeBlock(IASTCodeScope scope) {
		// TODO Auto-generated method stub
		
	}

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptEnumeratorReference(org.eclipse.cdt.core.parser.ast.IASTEnumerationReference)
     */
    public void acceptEnumeratorReference(IASTEnumeratorReference reference)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptParameterReference(org.eclipse.cdt.internal.core.parser.ast.complete.ASTParameterReference)
     */
    public void acceptParameterReference(IASTParameterReference reference)
    {
        // TODO Auto-generated method stub        
    }

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#createReader(java.lang.String)
	 */
	public CodeReader createReader(String finalPath, Iterator workingCopies) {
		return ParserUtil.createReader(finalPath, workingCopies	);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptTemplateParameterReference(org.eclipse.cdt.core.parser.ast.IASTTemplateParameterReference)
	 */
	public void acceptTemplateParameterReference(IASTTemplateParameterReference reference) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptFriendDeclaration(org.eclipse.cdt.core.parser.ast.IASTDeclaration)
	 */
	public void acceptFriendDeclaration(IASTDeclaration declaration) {
		// TODO Auto-generated method stub
		
	}
}
