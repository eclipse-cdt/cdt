/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser;


import java.util.Iterator;

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
import org.eclipse.cdt.internal.core.parser.InternalParserUtil;


public class NullSourceElementRequestor implements ISourceElementRequestor 
{
    private ParserMode mode = ParserMode.COMPLETE_PARSE;

	public NullSourceElementRequestor()
	{
	}

	public NullSourceElementRequestor( ParserMode mode )
	{
		this.mode = mode;
	}
	/* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptProblem(org.eclipse.cdt.core.parser.IProblem)
     */
    public boolean acceptProblem(IProblem problem)
    {
		return DefaultProblemHandler.ruleOnProblem( problem, mode );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptMacro(org.eclipse.cdt.core.parser.ast.IASTMacro)
     */
    public void acceptMacro(IASTMacro macro)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptVariable(org.eclipse.cdt.core.parser.ast.IASTVariable)
     */
    public void acceptVariable(IASTVariable variable)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptFunctionDeclaration(org.eclipse.cdt.core.parser.ast.IASTFunction)
     */
    public void acceptFunctionDeclaration(IASTFunction function)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptUsingDirective(org.eclipse.cdt.core.parser.ast.IASTUsingDirective)
     */
    public void acceptUsingDirective(IASTUsingDirective usageDirective)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptUsingDeclaration(org.eclipse.cdt.core.parser.ast.IASTUsingDeclaration)
     */
    public void acceptUsingDeclaration(IASTUsingDeclaration usageDeclaration)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptASMDefinition(org.eclipse.cdt.core.parser.ast.IASTASMDefinition)
     */
    public void acceptASMDefinition(IASTASMDefinition asmDefinition)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptTypedef(org.eclipse.cdt.core.parser.ast.IASTTypedef)
     */
    public void acceptTypedefDeclaration(IASTTypedefDeclaration typedef)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptEnumerationSpecifier(org.eclipse.cdt.core.parser.ast.IASTEnumerationSpecifier)
     */
    public void acceptEnumerationSpecifier(IASTEnumerationSpecifier enumeration)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterFunctionBody(org.eclipse.cdt.core.parser.ast.IASTFunction)
     */
    public void enterFunctionBody(IASTFunction function)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitFunctionBody(org.eclipse.cdt.core.parser.ast.IASTFunction)
     */
    public void exitFunctionBody(IASTFunction function)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterCompilationUnit(org.eclipse.cdt.core.parser.ast.IASTCompilationUnit)
     */
    public void enterCompilationUnit(IASTCompilationUnit compilationUnit)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterInclusion(org.eclipse.cdt.core.parser.ast.IASTInclusion)
     */
    public void enterInclusion(IASTInclusion inclusion)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterNamespaceDefinition(org.eclipse.cdt.core.parser.ast.IASTNamespaceDefinition)
     */
    public void enterNamespaceDefinition(IASTNamespaceDefinition namespaceDefinition)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterClassSpecifier(org.eclipse.cdt.core.parser.ast.IASTClassSpecifier)
     */
    public void enterClassSpecifier(IASTClassSpecifier classSpecification)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterLinkageSpecification(org.eclipse.cdt.core.parser.ast.IASTLinkageSpecification)
     */
    public void enterLinkageSpecification(IASTLinkageSpecification linkageSpec)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterTemplateDeclaration(org.eclipse.cdt.core.parser.ast.IASTTemplateDeclaration)
     */
    public void enterTemplateDeclaration(IASTTemplateDeclaration declaration)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterTemplateSpecialization(org.eclipse.cdt.core.parser.ast.IASTTemplateSpecialization)
     */
    public void enterTemplateSpecialization(IASTTemplateSpecialization specialization)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterTemplateExplicitInstantiation(org.eclipse.cdt.core.parser.ast.IASTTemplateInstantiation)
     */
    public void enterTemplateInstantiation(IASTTemplateInstantiation instantiation)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptMethodDeclaration(org.eclipse.cdt.core.parser.ast.IASTMethod)
     */
    public void acceptMethodDeclaration(IASTMethod method)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterMethodBody(org.eclipse.cdt.core.parser.ast.IASTMethod)
     */
    public void enterMethodBody(IASTMethod method)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitMethodBody(org.eclipse.cdt.core.parser.ast.IASTMethod)
     */
    public void exitMethodBody(IASTMethod method)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptField(org.eclipse.cdt.core.parser.ast.IASTField)
     */
    public void acceptField(IASTField field)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptClassReference(org.eclipse.cdt.core.parser.ast.IASTClassReference)
     */
    public void acceptClassReference(IASTClassReference reference)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitTemplateDeclaration(org.eclipse.cdt.core.parser.ast.IASTTemplateDeclaration)
     */
    public void exitTemplateDeclaration(IASTTemplateDeclaration declaration)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitTemplateSpecialization(org.eclipse.cdt.core.parser.ast.IASTTemplateSpecialization)
     */
    public void exitTemplateSpecialization(IASTTemplateSpecialization specialization)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitTemplateExplicitInstantiation(org.eclipse.cdt.core.parser.ast.IASTTemplateInstantiation)
     */
    public void exitTemplateExplicitInstantiation(IASTTemplateInstantiation instantiation)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitLinkageSpecification(org.eclipse.cdt.core.parser.ast.IASTLinkageSpecification)
     */
    public void exitLinkageSpecification(IASTLinkageSpecification linkageSpec)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitClassSpecifier(org.eclipse.cdt.core.parser.ast.IASTClassSpecifier)
     */
    public void exitClassSpecifier(IASTClassSpecifier classSpecification)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitNamespaceDefinition(org.eclipse.cdt.core.parser.ast.IASTNamespaceDefinition)
     */
    public void exitNamespaceDefinition(IASTNamespaceDefinition namespaceDefinition)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitInclusion(org.eclipse.cdt.core.parser.ast.IASTInclusion)
     */
    public void exitInclusion(IASTInclusion inclusion)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitCompilationUnit(org.eclipse.cdt.core.parser.ast.IASTCompilationUnit)
     */
    public void exitCompilationUnit(IASTCompilationUnit compilationUnit)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptAbstractTypeSpecDeclaration(org.eclipse.cdt.core.parser.ast.IASTAbstractTypeSpecifierDeclaration)
     */
    public void acceptAbstractTypeSpecDeclaration(IASTAbstractTypeSpecifierDeclaration abstractDeclaration)
    {
        // TODO Auto-generated method stub
        
    }




    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptTypedefReference(org.eclipse.cdt.core.parser.ast.IASTTypedefReference)
     */
    public void acceptTypedefReference(IASTTypedefReference reference)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptNamespaceReference(org.eclipse.cdt.core.parser.ast.IASTNamespaceReference)
     */
    public void acceptNamespaceReference(IASTNamespaceReference reference)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptEnumerationReference(org.eclipse.cdt.core.parser.ast.IASTEnumerationReference)
     */
    public void acceptEnumerationReference(IASTEnumerationReference reference)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptVariableReference(org.eclipse.cdt.core.parser.ast.IASTVariableReference)
     */
    public void acceptVariableReference(IASTVariableReference reference)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptFunctionReference(org.eclipse.cdt.core.parser.ast.IASTFunctionReference)
     */
    public void acceptFunctionReference(IASTFunctionReference reference)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptFieldReference(org.eclipse.cdt.core.parser.ast.IASTFieldReference)
     */
    public void acceptFieldReference(IASTFieldReference reference)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptMethodReference(org.eclipse.cdt.core.parser.ast.IASTMethodReference)
     */
    public void acceptMethodReference(IASTMethodReference reference)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptElaboratedForewardDeclaration(org.eclipse.cdt.core.parser.ast.IASTElaboratedTypeSpecifier)
     */
    public void acceptElaboratedForewardDeclaration(IASTElaboratedTypeSpecifier elaboratedType)
    {
        // TODO Auto-generated method stub
        
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
		return InternalParserUtil.createFileReader( finalPath );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptTemplateParameterReference(org.eclipse.cdt.core.parser.ast.IASTTemplateParameterReference)
	 */
	public void acceptTemplateParameterReference(IASTTemplateParameterReference reference) 
	{
		// TODO Auto-generated method stub
		
	} 
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptFriendDeclaration(org.eclipse.cdt.core.parser.ast.IASTDeclaration)
	 */
	public void acceptFriendDeclaration(IASTDeclaration declaration) {
		// TODO Auto-generated method stub
		
	}
}
