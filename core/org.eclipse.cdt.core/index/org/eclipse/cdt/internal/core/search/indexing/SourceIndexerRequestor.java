/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.search.indexing;

/**
* @author bgheorgh
*/

import org.eclipse.cdt.core.parser.IParser;
import org.eclipse.cdt.core.parser.IParserCallback;
import org.eclipse.cdt.core.parser.IProblem;
import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.ast.IASTASMDefinition;
import org.eclipse.cdt.core.parser.ast.IASTClassReference;
import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTCompilationUnit;
import org.eclipse.cdt.core.parser.ast.IASTElaboratedTypeSpecifier;
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
import org.eclipse.cdt.internal.core.index.IDocument;

/**
 * @author bgheorgh
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class SourceIndexerRequestor implements IParserCallback,ISourceElementRequestor, IIndexConstants {
	
	SourceIndexer indexer;
	IDocument document;

	char[] packageName;
	char[][] enclosingTypeNames = new char[5][];
	int depth = 0;
	int methodDepth = 0;
	
	public SourceIndexerRequestor(SourceIndexer indexer, IDocument document) {
		super();
		this.indexer = indexer;
		this.document= document;
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
		//System.out.println("acceptMacro");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptVariable(org.eclipse.cdt.core.parser.ast.IASTVariable)
	 */
	public void acceptVariable(IASTVariable variable) {
		// TODO Auto-generated method stub
		//System.out.println("acceptVariable");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptFunctionDeclaration(org.eclipse.cdt.core.parser.ast.IASTFunction)
	 */
	public void acceptFunctionDeclaration(IASTFunction function) {
		// TODO Auto-generated method stub
		//System.out.println("acceptFunctionDeclaration");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptUsingDirective(org.eclipse.cdt.core.parser.ast.IASTUsingDirective)
	 */
	public void acceptUsingDirective(IASTUsingDirective usageDirective) {
		// TODO Auto-generated method stub
		//System.out.println("acceptUsingDirective");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptUsingDeclaration(org.eclipse.cdt.core.parser.ast.IASTUsingDeclaration)
	 */
	public void acceptUsingDeclaration(IASTUsingDeclaration usageDeclaration) {
		// TODO Auto-generated method stub
		//System.out.println("acceptUsingDeclaration");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptASMDefinition(org.eclipse.cdt.core.parser.ast.IASTASMDefinition)
	 */
	public void acceptASMDefinition(IASTASMDefinition asmDefinition) {
		// TODO Auto-generated method stub
		//System.out.println("acceptASMDefinition");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptTypedef(org.eclipse.cdt.core.parser.ast.IASTTypedef)
	 */
	public void acceptTypedef(IASTTypedef typedef) {
		// TODO Auto-generated method stub
		//System.out.println("acceptTypedef");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptEnumerationSpecifier(org.eclipse.cdt.core.parser.ast.IASTEnumerationSpecifier)
	 */
	public void acceptEnumerationSpecifier(IASTEnumerationSpecifier enumeration) {
		// TODO Auto-generated method stub
		//System.out.println("acceptEnumSpecifier");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterFunctionBody(org.eclipse.cdt.core.parser.ast.IASTFunction)
	 */
	public void enterFunctionBody(IASTFunction function) {
		// TODO Auto-generated method stub
		//System.out.println("enterFunctionBody");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitFunctionBody(org.eclipse.cdt.core.parser.ast.IASTFunction)
	 */
	public void exitFunctionBody(IASTFunction function) {
		// TODO Auto-generated method stub
		//System.out.println("exitFunctionBody");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterCompilationUnit(org.eclipse.cdt.core.parser.ast.IASTCompilationUnit)
	 */
	public void enterCompilationUnit(IASTCompilationUnit compilationUnit) {
		// TODO Auto-generated method stub
		//System.out.println("enterCompilationUnit");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterInclusion(org.eclipse.cdt.core.parser.ast.IASTInclusion)
	 */
	public void enterInclusion(IASTInclusion inclusion) {
		// TODO Auto-generated method stub
		
		//System.out.println("NEW INCLUSION \nInclusion short name: " + inclusion.getName()); 
		//System.out.println("Inclusion Long Name: " + inclusion.getFullFileName());
		//System.out.println("enterInclusion");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterNamespaceDefinition(org.eclipse.cdt.core.parser.ast.IASTNamespaceDefinition)
	 */
	public void enterNamespaceDefinition(IASTNamespaceDefinition namespaceDefinition) {
		// TODO Auto-generated method stub
		//System.out.println("enterNamespaceDefinition");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterClassSpecifier(org.eclipse.cdt.core.parser.ast.IASTClassSpecifier)
	 */
	public void enterClassSpecifier(IASTClassSpecifier classSpecification) {
		// TODO Auto-generated method stub
		
		//System.out.println("New class spec: " + classSpecification.getName());
		indexer.addClassSpecifier(classSpecification);
		//System.out.println("enterClassSpecifier");

	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterLinkageSpecification(org.eclipse.cdt.core.parser.ast.IASTLinkageSpecification)
	 */
	public void enterLinkageSpecification(IASTLinkageSpecification linkageSpec) {
		// TODO Auto-generated method stub
		//System.out.println("enterLinkageSpecification");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterTemplateDeclaration(org.eclipse.cdt.core.parser.ast.IASTTemplateDeclaration)
	 */
	public void enterTemplateDeclaration(IASTTemplateDeclaration declaration) {
		// TODO Auto-generated method stub
		//System.out.println("enterTemplateDeclaration");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterTemplateSpecialization(org.eclipse.cdt.core.parser.ast.IASTTemplateSpecialization)
	 */
	public void enterTemplateSpecialization(IASTTemplateSpecialization specialization) {
		// TODO Auto-generated method stub
		//System.out.println("enterTemplateSpecialization");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterTemplateExplicitInstantiation(org.eclipse.cdt.core.parser.ast.IASTTemplateInstantiation)
	 */
	public void enterTemplateExplicitInstantiation(IASTTemplateInstantiation instantiation) {
		// TODO Auto-generated method stub
		//System.out.println("enterTemplateExplicitInstantiation");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptMethodDeclaration(org.eclipse.cdt.core.parser.ast.IASTMethod)
	 */
	public void acceptMethodDeclaration(IASTMethod method) {
		// TODO Auto-generated method stub
		//System.out.println("acceptMethodDeclaration");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterMethodBody(org.eclipse.cdt.core.parser.ast.IASTMethod)
	 */
	public void enterMethodBody(IASTMethod method) {
		// TODO Auto-generated method stub
		//System.out.println("enterMethodBody");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitMethodBody(org.eclipse.cdt.core.parser.ast.IASTMethod)
	 */
	public void exitMethodBody(IASTMethod method) {
		// TODO Auto-generated method stub
		//System.out.println("exitMethodBody");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptField(org.eclipse.cdt.core.parser.ast.IASTField)
	 */
	public void acceptField(IASTField field) {
		// TODO Auto-generated method stub
		//System.out.println("acceptField");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptClassReference(org.eclipse.cdt.core.parser.ast.IASTClassSpecifier, int)
	 */
	public void acceptClassReference(IASTClassReference reference) {
		// TODO Auto-generated method stub
		//System.out.println("acceptClassReference");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitTemplateDeclaration(org.eclipse.cdt.core.parser.ast.IASTTemplateDeclaration)
	 */
	public void exitTemplateDeclaration(IASTTemplateDeclaration declaration) {
		// TODO Auto-generated method stub
		//System.out.println("exitTemplateDeclaration");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitTemplateSpecialization(org.eclipse.cdt.core.parser.ast.IASTTemplateSpecialization)
	 */
	public void exitTemplateSpecialization(IASTTemplateSpecialization specialization) {
		// TODO Auto-generated method stub
		//System.out.println("exitTemplateSpecialization");

	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitTemplateExplicitInstantiation(org.eclipse.cdt.core.parser.ast.IASTTemplateInstantiation)
	 */
	public void exitTemplateExplicitInstantiation(IASTTemplateInstantiation instantiation) {
		// TODO Auto-generated method stub
		//System.out.println("exitTemplateExplicitInstantiation");

	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitLinkageSpecification(org.eclipse.cdt.core.parser.ast.IASTLinkageSpecification)
	 */
	public void exitLinkageSpecification(IASTLinkageSpecification linkageSpec) {
		// TODO Auto-generated method stub
		//System.out.println("exitLinkageSpecification");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitClassSpecifier(org.eclipse.cdt.core.parser.ast.IASTClassSpecifier)
	 */
	public void exitClassSpecifier(IASTClassSpecifier classSpecification) {
		// TODO Auto-generated method stub
		//System.out.println("exitClassSpecifier");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitNamespaceDefinition(org.eclipse.cdt.core.parser.ast.IASTNamespaceDefinition)
	 */
	public void exitNamespaceDefinition(IASTNamespaceDefinition namespaceDefinition) {
		// TODO Auto-generated method stub
		//System.out.println("exitNamespaceDefinition");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitInclusion(org.eclipse.cdt.core.parser.ast.IASTInclusion)
	 */
	public void exitInclusion(IASTInclusion inclusion) {
		// TODO Auto-generated method stub
		//System.out.println("exitInclusion");

	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitCompilationUnit(org.eclipse.cdt.core.parser.ast.IASTCompilationUnit)
	 */
	public void exitCompilationUnit(IASTCompilationUnit compilationUnit) {
		// TODO Auto-generated method stub
		//System.out.println("exitCompilationUnit");

	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#setParser(org.eclipse.cdt.core.parser.IParser)
	 */
	public void setParser(IParser parser) {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#translationUnitBegin()
	 */
	public Object translationUnitBegin() {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#translationUnitEnd(java.lang.Object)
	 */
	public void translationUnitEnd(Object unit) {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#inclusionBegin(java.lang.String, int, int, boolean)
	 */
	public Object inclusionBegin(String includeFile, int nameBeginOffset, int inclusionBeginOffset, boolean local) {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#inclusionEnd(java.lang.Object)
	 */
	public void inclusionEnd(Object inclusion) {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#macro(java.lang.String, int, int, int)
	 */
	public Object macro(String macroName, int macroNameOffset, int macroBeginOffset, int macroEndOffset) {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#simpleDeclarationBegin(java.lang.Object, org.eclipse.cdt.core.parser.IToken)
	 */
	public Object simpleDeclarationBegin(Object Container, IToken firstToken) {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#simpleDeclSpecifier(java.lang.Object, org.eclipse.cdt.core.parser.IToken)
	 */
	public void simpleDeclSpecifier(Object Container, IToken specifier) {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#simpleDeclSpecifierName(java.lang.Object)
	 */
	public void simpleDeclSpecifierName(Object declaration) {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#simpleDeclSpecifierType(java.lang.Object, java.lang.Object)
	 */
	public void simpleDeclSpecifierType(Object declaration, Object type) {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#simpleDeclarationEnd(java.lang.Object, org.eclipse.cdt.core.parser.IToken)
	 */
	public void simpleDeclarationEnd(Object declaration, IToken lastToken) {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#parameterDeclarationBegin(java.lang.Object)
	 */
	public Object parameterDeclarationBegin(Object Container) {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#parameterDeclarationEnd(java.lang.Object)
	 */
	public void parameterDeclarationEnd(Object declaration) {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#nameBegin(org.eclipse.cdt.core.parser.IToken)
	 */
	public void nameBegin(IToken firstToken) {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#nameEnd(org.eclipse.cdt.core.parser.IToken)
	 */
	public void nameEnd(IToken lastToken) {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#declaratorBegin(java.lang.Object)
	 */
	public Object declaratorBegin(Object container) {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#declaratorId(java.lang.Object)
	 */
	public void declaratorId(Object declarator) {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#declaratorAbort(java.lang.Object)
	 */
	public void declaratorAbort(Object declarator) {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#declaratorPureVirtual(java.lang.Object)
	 */
	public void declaratorPureVirtual(Object declarator) {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#declaratorCVModifier(java.lang.Object, org.eclipse.cdt.core.parser.IToken)
	 */
	public void declaratorCVModifier(Object declarator, IToken modifier) {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#declaratorThrowsException(java.lang.Object)
	 */
	public void declaratorThrowsException(Object declarator) {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#declaratorThrowExceptionName(java.lang.Object)
	 */
	public void declaratorThrowExceptionName(Object declarator) {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#declaratorEnd(java.lang.Object)
	 */
	public void declaratorEnd(Object declarator) {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#arrayDeclaratorBegin(java.lang.Object)
	 */
	public Object arrayDeclaratorBegin(Object declarator) {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#arrayDeclaratorEnd(java.lang.Object)
	 */
	public void arrayDeclaratorEnd(Object arrayQualifier) {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#pointerOperatorBegin(java.lang.Object)
	 */
	public Object pointerOperatorBegin(Object container) {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#pointerOperatorType(java.lang.Object, org.eclipse.cdt.core.parser.IToken)
	 */
	public void pointerOperatorType(Object ptrOperator, IToken type) {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#pointerOperatorName(java.lang.Object)
	 */
	public void pointerOperatorName(Object ptrOperator) {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#pointerOperatorCVModifier(java.lang.Object, org.eclipse.cdt.core.parser.IToken)
	 */
	public void pointerOperatorCVModifier(Object ptrOperator, IToken modifier) {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#pointerOperatorAbort(java.lang.Object)
	 */
	public void pointerOperatorAbort(Object ptrOperator) {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#pointerOperatorEnd(java.lang.Object)
	 */
	public void pointerOperatorEnd(Object ptrOperator) {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#argumentsBegin(java.lang.Object)
	 */
	public Object argumentsBegin(Object declarator) {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#argumentsEnd(java.lang.Object)
	 */
	public void argumentsEnd(Object parameterDeclarationClause) {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#functionBodyBegin(java.lang.Object)
	 */
	public Object functionBodyBegin(Object declaration) {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#functionBodyEnd(java.lang.Object)
	 */
	public void functionBodyEnd(Object functionBody) {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#classSpecifierBegin(java.lang.Object, org.eclipse.cdt.core.parser.IToken)
	 */
	public Object classSpecifierBegin(Object container, IToken classKey) {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#classSpecifierName(java.lang.Object)
	 */
	public void classSpecifierName(Object classSpecifier) {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#classSpecifierAbort(java.lang.Object)
	 */
	public void classSpecifierAbort(Object classSpecifier) {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#classMemberVisibility(java.lang.Object, org.eclipse.cdt.core.parser.IToken)
	 */
	public void classMemberVisibility(Object classSpecifier, IToken visibility) {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#classSpecifierEnd(java.lang.Object, org.eclipse.cdt.core.parser.IToken)
	 */
	public void classSpecifierEnd(Object classSpecifier, IToken closingBrace) {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#baseSpecifierBegin(java.lang.Object)
	 */
	public Object baseSpecifierBegin(Object containingClassSpec) {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#baseSpecifierName(java.lang.Object)
	 */
	public void baseSpecifierName(Object baseSpecifier) {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#baseSpecifierVisibility(java.lang.Object, org.eclipse.cdt.core.parser.IToken)
	 */
	public void baseSpecifierVisibility(Object baseSpecifier, IToken visibility) {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#baseSpecifierVirtual(java.lang.Object, boolean)
	 */
	public void baseSpecifierVirtual(Object baseSpecifier, boolean virtual) {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#baseSpecifierEnd(java.lang.Object)
	 */
	public void baseSpecifierEnd(Object baseSpecifier) {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#expressionBegin(java.lang.Object)
	 */
	public Object expressionBegin(Object container) {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#expressionOperator(java.lang.Object, org.eclipse.cdt.core.parser.IToken)
	 */
	public void expressionOperator(Object expression, IToken operator) {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#expressionTerminal(java.lang.Object, org.eclipse.cdt.core.parser.IToken)
	 */
	public void expressionTerminal(Object expression, IToken terminal) {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#expressionName(java.lang.Object)
	 */
	public void expressionName(Object expression) {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#expressionAbort(java.lang.Object)
	 */
	public void expressionAbort(Object expression) {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#expressionEnd(java.lang.Object)
	 */
	public void expressionEnd(Object expression) {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#elaboratedTypeSpecifierBegin(java.lang.Object, org.eclipse.cdt.core.parser.IToken)
	 */
	public Object elaboratedTypeSpecifierBegin(Object container, IToken classKey) {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#elaboratedTypeSpecifierName(java.lang.Object)
	 */
	public void elaboratedTypeSpecifierName(Object elab) {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#elaboratedTypeSpecifierEnd(java.lang.Object)
	 */
	public void elaboratedTypeSpecifierEnd(Object elab) {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#namespaceDefinitionBegin(java.lang.Object, org.eclipse.cdt.core.parser.IToken)
	 */
	public Object namespaceDefinitionBegin(Object container, IToken namespace) {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#namespaceDefinitionId(java.lang.Object)
	 */
	public void namespaceDefinitionId(Object namespace) {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#namespaceDefinitionAbort(java.lang.Object)
	 */
	public void namespaceDefinitionAbort(Object namespace) {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#namespaceDefinitionEnd(java.lang.Object, org.eclipse.cdt.core.parser.IToken)
	 */
	public void namespaceDefinitionEnd(Object namespace, IToken closingBrace) {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#linkageSpecificationBegin(java.lang.Object, java.lang.String)
	 */
	public Object linkageSpecificationBegin(Object container, String literal) {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#linkageSpecificationEnd(java.lang.Object)
	 */
	public void linkageSpecificationEnd(Object linkageSpec) {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#usingDirectiveBegin(java.lang.Object)
	 */
	public Object usingDirectiveBegin(Object container) {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#usingDirectiveNamespaceId(java.lang.Object)
	 */
	public void usingDirectiveNamespaceId(Object directive) {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#usingDirectiveAbort(java.lang.Object)
	 */
	public void usingDirectiveAbort(Object directive) {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#usingDirectiveEnd(java.lang.Object)
	 */
	public void usingDirectiveEnd(Object directive) {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#usingDeclarationBegin(java.lang.Object)
	 */
	public Object usingDeclarationBegin(Object container) {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#usingDeclarationMapping(java.lang.Object, boolean)
	 */
	public void usingDeclarationMapping(Object declaration, boolean isTypeName) {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#usingDeclarationAbort(java.lang.Object)
	 */
	public void usingDeclarationAbort(Object declaration) {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#usingDeclarationEnd(java.lang.Object)
	 */
	public void usingDeclarationEnd(Object declaration) {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#enumSpecifierBegin(java.lang.Object, org.eclipse.cdt.core.parser.IToken)
	 */
	public Object enumSpecifierBegin(Object container, IToken enumKey) {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#enumSpecifierId(java.lang.Object)
	 */
	public void enumSpecifierId(Object enumSpec) {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#enumSpecifierAbort(java.lang.Object)
	 */
	public void enumSpecifierAbort(Object enumSpec) {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#enumSpecifierEnd(java.lang.Object, org.eclipse.cdt.core.parser.IToken)
	 */
	public void enumSpecifierEnd(Object enumSpec, IToken closingBrace) {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#enumeratorBegin(java.lang.Object)
	 */
	public Object enumeratorBegin(Object enumSpec) {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#enumeratorId(java.lang.Object)
	 */
	public void enumeratorId(Object enumDefn) {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#enumeratorEnd(java.lang.Object, org.eclipse.cdt.core.parser.IToken)
	 */
	public void enumeratorEnd(Object enumDefn, IToken lastToken) {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#asmDefinition(java.lang.Object, java.lang.String)
	 */
	public void asmDefinition(Object container, String assemblyCode) {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#constructorChainBegin(java.lang.Object)
	 */
	public Object constructorChainBegin(Object declarator) {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#constructorChainAbort(java.lang.Object)
	 */
	public void constructorChainAbort(Object ctor) {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#constructorChainEnd(java.lang.Object)
	 */
	public void constructorChainEnd(Object ctor) {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#constructorChainElementBegin(java.lang.Object)
	 */
	public Object constructorChainElementBegin(Object ctor) {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#constructorChainElementId(java.lang.Object)
	 */
	public void constructorChainElementId(Object element) {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#constructorChainElementEnd(java.lang.Object)
	 */
	public void constructorChainElementEnd(Object element) {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#constructorChainElementExpressionListElementBegin(java.lang.Object)
	 */
	public Object constructorChainElementExpressionListElementBegin(Object element) {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#constructorChainElementExpressionListElementEnd(java.lang.Object)
	 */
	public void constructorChainElementExpressionListElementEnd(Object expression) {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#explicitInstantiationBegin(java.lang.Object)
	 */
	public Object explicitInstantiationBegin(Object container) {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#explicitInstantiationEnd(java.lang.Object)
	 */
	public void explicitInstantiationEnd(Object instantiation) {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#explicitSpecializationBegin(java.lang.Object)
	 */
	public Object explicitSpecializationBegin(Object container) {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#explicitSpecializationEnd(java.lang.Object)
	 */
	public void explicitSpecializationEnd(Object instantiation) {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#templateDeclarationBegin(java.lang.Object, org.eclipse.cdt.core.parser.IToken)
	 */
	public Object templateDeclarationBegin(Object container, IToken firstToken) {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#templateDeclarationAbort(java.lang.Object)
	 */
	public void templateDeclarationAbort(Object templateDecl) {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#templateDeclarationEnd(java.lang.Object, org.eclipse.cdt.core.parser.IToken)
	 */
	public void templateDeclarationEnd(Object templateDecl, IToken lastToken) {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#templateParameterListBegin(java.lang.Object)
	 */
	public Object templateParameterListBegin(Object declaration) {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#templateParameterListEnd(java.lang.Object)
	 */
	public void templateParameterListEnd(Object parameterList) {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#templateTypeParameterBegin(java.lang.Object, org.eclipse.cdt.core.parser.IToken)
	 */
	public Object templateTypeParameterBegin(Object templDecl, IToken kind) {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#templateTypeParameterName(java.lang.Object)
	 */
	public void templateTypeParameterName(Object typeParm) {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#templateTypeParameterAbort(java.lang.Object)
	 */
	public void templateTypeParameterAbort(Object typeParm) {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#templateTypeParameterInitialTypeId(java.lang.Object)
	 */
	public void templateTypeParameterInitialTypeId(Object typeParm) {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#templateTypeParameterEnd(java.lang.Object)
	 */
	public void templateTypeParameterEnd(Object typeParm) {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#startBitfield(java.lang.Object)
	 */
	public Object startBitfield(Object declarator) {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#endBitfield(java.lang.Object)
	 */
	public void endBitfield(Object bitfield) {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#oldKRParametersBegin(java.lang.Object)
	 */
	public Object oldKRParametersBegin(Object parameterDeclarationClause) {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserCallback#oldKRParametersEnd(java.lang.Object)
	 */
	public void oldKRParametersEnd(Object oldKRParameterDeclarationClause) {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptElaboratedTypeSpecifier(org.eclipse.cdt.core.parser.ast.IASTElaboratedTypeSpecifier)
	 */
	public void acceptElaboratedTypeSpecifier(IASTElaboratedTypeSpecifier elaboratedTypeSpec) {
		// TODO Auto-generated method stub
		
	}

//TODO: Get rid of these IParserCallbacks once the parser cleans up

}
