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

import java.util.LinkedList;

import org.eclipse.cdt.core.parser.IProblem;
import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.ast.*;
import org.eclipse.cdt.core.search.ICSearchPattern;
import org.eclipse.cdt.core.search.ICSearchResultCollector;
import org.eclipse.cdt.core.search.ICSearchScope;
import org.eclipse.cdt.internal.core.model.IWorkingCopy;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;


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
	public MatchLocator( ICSearchPattern pattern, ICSearchResultCollector collector, ICSearchScope scope, IProgressMonitor monitor) {
		super();
		searchPattern = pattern;
		resultCollector = collector;
		searchScope = scope;
		progressMonitor = monitor;		
	}

	public void acceptProblem(IProblem problem) 								{	}
	public void acceptMacro(IASTMacro macro) 									{	}
	public void acceptVariable(IASTVariable variable) 							{	}
	public void acceptFunctionDeclaration(IASTFunction function) 				{	}
	public void acceptUsingDirective(IASTUsingDirective usageDirective) 		{	}
	public void acceptUsingDeclaration(IASTUsingDeclaration usageDeclaration) 	{	}
	public void acceptASMDefinition(IASTASMDefinition asmDefinition) 			{	}
	public void acceptTypedef(IASTTypedef typedef) 								{	}
	public void acceptEnumerator(IASTEnumerator enumerator) 					{	}
	public void acceptEnumerationSpecifier(IASTEnumerationSpecifier enumeration){	}
	public void acceptClassReference(IASTClassSpecifier classSpecifier, int referenceOffset) {	}
	public void acceptElaboratedTypeSpecifier(IASTElaboratedTypeSpecifier elaboratedTypeSpec){  }
	public void acceptMethodDeclaration(IASTMethod method) 						{	}
	public void acceptField(IASTField field) 									{	}
	public void acceptConstructor(IASTConstructor constructor) 					{	}	
	public void enterFunctionBody(IASTFunction function) 						{	}
	public void enterCompilationUnit(IASTCompilationUnit compilationUnit) 		{	}
	public void enterNamespaceDefinition(IASTNamespaceDefinition namespaceDefinition) 		{	}
	public void enterLinkageSpecification(IASTLinkageSpecification linkageSpec) {	}
	public void enterTemplateDeclaration(IASTTemplateDeclaration declaration) 	{	}
	public void enterTemplateSpecialization(IASTTemplateSpecialization specialization) 		{	}
	public void enterTemplateExplicitInstantiation(IASTTemplateInstantiation instantiation) {	}
	public void enterMethodBody(IASTMethod method) 								{	}
	public void exitFunctionBody(IASTFunction function) 						{	}
	public void exitMethodBody(IASTMethod method) 								{	}
	public void exitTemplateDeclaration(IASTTemplateDeclaration declaration) 	{	}
	public void exitTemplateSpecialization(IASTTemplateSpecialization specialization) 		{	}
	public void exitTemplateExplicitInstantiation(IASTTemplateInstantiation instantiation) 	{	}
	public void exitLinkageSpecification(IASTLinkageSpecification linkageSpec) 	{	}
	public void exitClassSpecifier(IASTClassSpecifier classSpecification) 		{	}
	public void exitNamespaceDefinition(IASTNamespaceDefinition namespaceDefinition) 		{	}
	public void exitCompilationUnit(IASTCompilationUnit compilationUnit)		{	}

	public void enterInclusion(IASTInclusion inclusion) {
		String includePath = inclusion.getFullFileName();

		IPath path = new Path( includePath );
		IResource resource = workspaceRoot.findMember( path, true );
		if( resource != null ){
			resourceStack.addFirst( currentResource );
			currentResource = resource;
		}
	}

	public void exitInclusion(IASTInclusion inclusion) {
		currentResource = (IResource) resourceStack.removeFirst();
	}
		
	public void enterClassSpecifier(IASTClassSpecifier classSpecification) {
		if( searchPattern instanceof ClassDeclarationPattern ){
			int level = searchPattern.matchLevel( classSpecification ); 
			if(  level != ICSearchPattern.IMPOSSIBLE_MATCH ){
				report( classSpecification, level );				
			}
		}
	}

	public void locateMatches( String [] paths, IWorkspace workspace, IWorkingCopy[] workingCopies ){
		workspaceRoot = workspace.getRoot();
	}
	
	protected void report( IASTOffsetableElement node, int accuracyLevel ){
		try {
			resultCollector.accept( currentResource, 
							  node.getElementStartingOffset(), 
							  node.getElementEndingOffset(), 
							  null, 
							  accuracyLevel );
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private ICSearchPattern 		searchPattern;
	private ICSearchResultCollector resultCollector;
	private IProgressMonitor 		progressMonitor;
	private IResource 				currentResource;
	private ICSearchScope 			searchScope;		
	private LinkedList 				resourceStack;
	private IWorkspaceRoot 			workspaceRoot;
}
