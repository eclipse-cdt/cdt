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

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;

import org.eclipse.cdt.core.parser.IParser;
import org.eclipse.cdt.core.parser.IProblem;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.ParserFactory;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ast.*;
import org.eclipse.cdt.core.search.ICSearchPattern;
import org.eclipse.cdt.core.search.ICSearchResultCollector;
import org.eclipse.cdt.core.search.ICSearchScope;
import org.eclipse.cdt.internal.core.model.IWorkingCopy;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
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
		IResource resource = null;
		
		if( workspaceRoot != null ){
			resource = workspaceRoot.findMember( path, true );
			if( resource == null ){
				IFile file = workspaceRoot.getFile( path );
				try{
					file.createLink( path, 0, null );
				} catch ( CoreException e ){
					file = null;
				}
				resource = file;
			}
		}
		
		resourceStack.addFirst( ( currentResource != null ) ? (Object)currentResource : (Object)currentPath );
		
		currentResource = resource;
		currentPath = ( resource == null ) ? path : null;
	}

	public void exitInclusion(IASTInclusion inclusion) {
		Object obj = resourceStack.removeFirst();
		if( obj instanceof IResource ){
			currentResource = (IResource)obj;
			currentPath = null;
		} else {
			currentPath = (IPath) obj;
			currentResource = null;
		}
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
		workspaceRoot = (workspace != null) ? workspace.getRoot() : null;
		
		HashMap wcPaths = new HashMap();
		int wcLength = (workingCopies == null) ? 0 : workingCopies.length;
		if( wcLength > 0 ){
			String [] newPaths = new String[ wcLength ];
			
			for( int i = 0; i < wcLength; i++ ){
				IWorkingCopy workingCopy = workingCopies[ i ];
				String path = workingCopy.getOriginalElement().getPath().toString();
				wcPaths.put( path, workingCopy );
				newPaths[ i ] = path;
			}
			
			int len = paths.length;
			String [] tempArray = new String[ len + wcLength ];
			System.arraycopy( paths, 0, tempArray, 0, len );
			System.arraycopy( newPaths, 0, tempArray, len, wcLength );
			paths = tempArray;
		}
		
		Arrays.sort( paths );
		
		int length = paths.length;
		if( progressMonitor != null ){
			progressMonitor.beginTask( "", length );
		}
		
		for( int i = 0; i < length; i++ ){
			if( progressMonitor != null &&  progressMonitor.isCanceled() ){
				throw new OperationCanceledException();
			}
			
			String pathString = paths[ i ];
			
			//skip duplicates
			if( i > 0 && pathString.equals( paths[ i - 1 ] ) ) continue;
			
			Reader reader = null;
			if( workspaceRoot != null ){
				IWorkingCopy workingCopy = (IWorkingCopy)wcPaths.get( pathString );
				
				if( workingCopy != null ){
					currentResource = workingCopy.getOriginalElement().getResource();
				} else {
					currentResource = workspaceRoot.findMember( pathString, true );
				}
			
				try{
					if( currentResource == null ){
						IPath path = new Path( pathString );
						IFile file = workspaceRoot.getFile( path );
						file.createLink( path, 0, null );
					}
					if( currentResource != null && currentResource instanceof IFile ){
						IFile file = (IFile) currentResource;
						reader = new InputStreamReader( file.getContents() );
					} else continue;
				} catch ( CoreException e ){
					continue;
				}
			} else {
				IPath path = new Path( pathString );
				try {
					currentPath = path;
					reader = new FileReader( path.toFile() );
				} catch (FileNotFoundException e) {
					continue;
				}
			}
			
			IScanner scanner = ParserFactory.createScanner( reader, pathString, null, null, ParserMode.QUICK_PARSE );
			IParser  parser  = ParserFactory.createParser( scanner, null, ParserMode.QUICK_PARSE );
			parser.setRequestor( this );
			
			parser.parse();
		}
	}
	
	protected void report( IASTOffsetableNamedElement node, int accuracyLevel ){
		try {
			if( currentResource != null ){
				resultCollector.accept( currentResource, 
								  node.getElementNameOffset(), 
								  node.getElementNameOffset() + node.getName().length(), 
								  null, 
								  accuracyLevel );
			} else if( currentPath != null ){
				resultCollector.accept( currentPath, 
										node.getElementStartingOffset(), 
										node.getElementEndingOffset(), 
										null, 
										accuracyLevel );				
			}
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private ICSearchPattern 		searchPattern;
	private ICSearchResultCollector resultCollector;
	private IProgressMonitor 		progressMonitor;
	private IResource 				currentResource = null;
	private IPath					currentPath 	= null;
	private ICSearchScope 			searchScope;		
	private LinkedList 				resourceStack = new LinkedList();
	private IWorkspaceRoot 			workspaceRoot;
}
