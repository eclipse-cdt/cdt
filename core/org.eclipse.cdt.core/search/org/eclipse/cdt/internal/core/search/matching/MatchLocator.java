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
import org.eclipse.cdt.core.parser.ast.ASTClassKind;
import org.eclipse.cdt.core.parser.ast.IASTASMDefinition;
import org.eclipse.cdt.core.parser.ast.IASTAbstractTypeSpecifierDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTClassReference;
import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTCompilationUnit;
import org.eclipse.cdt.core.parser.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTEnumerator;
import org.eclipse.cdt.core.parser.ast.IASTField;
import org.eclipse.cdt.core.parser.ast.IASTFunction;
import org.eclipse.cdt.core.parser.ast.IASTInclusion;
import org.eclipse.cdt.core.parser.ast.IASTLinkageSpecification;
import org.eclipse.cdt.core.parser.ast.IASTMacro;
import org.eclipse.cdt.core.parser.ast.IASTMethod;
import org.eclipse.cdt.core.parser.ast.IASTNamespaceDefinition;
import org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement;
import org.eclipse.cdt.core.parser.ast.IASTPointerToFunction;
import org.eclipse.cdt.core.parser.ast.IASTPointerToMethod;
import org.eclipse.cdt.core.parser.ast.IASTScope;
import org.eclipse.cdt.core.parser.ast.IASTTemplateDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTTemplateInstantiation;
import org.eclipse.cdt.core.parser.ast.IASTTemplateSpecialization;
import org.eclipse.cdt.core.parser.ast.IASTTypedefDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTUsingDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTUsingDirective;
import org.eclipse.cdt.core.parser.ast.IASTVariable;
import org.eclipse.cdt.core.search.ICSearchConstants;
import org.eclipse.cdt.core.search.ICSearchPattern;
import org.eclipse.cdt.core.search.ICSearchResultCollector;
import org.eclipse.cdt.core.search.ICSearchScope;
import org.eclipse.cdt.internal.core.model.IWorkingCopy;
import org.eclipse.cdt.internal.core.parser.ScannerInfo;
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
public class MatchLocator implements ISourceElementRequestor, ICSearchConstants {

	
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
	public void acceptTypedef(IASTTypedefDeclaration typedef) 								{	}
	public void acceptEnumerator(IASTEnumerator enumerator) 					{	}
	public void acceptAbstractTypeSpecDeclaration(IASTAbstractTypeSpecifierDeclaration abstractDeclaration) {}
	public void acceptPointerToFunction(IASTPointerToFunction function) {}
	public void acceptPointerToMethod(IASTPointerToMethod method)   { }
	
	public void acceptEnumerationSpecifier(IASTEnumerationSpecifier enumeration){
		if( searchPattern.getLimitTo() == DECLARATIONS || searchPattern.getLimitTo() == ALL_OCCURRENCES ){
			if( searchPattern instanceof ClassDeclarationPattern ){
				ClassDeclarationPattern classPattern = (ClassDeclarationPattern)searchPattern;
				if( classPattern.getKind() == null || classPattern.getKind() == ASTClassKind.ENUM ){
					int level = searchPattern.matchLevel( enumeration ); 
					if(  level != ICSearchPattern.IMPOSSIBLE_MATCH ){
						report( enumeration, level );				
					}
				}
			}
		}
	}	
	
	public void acceptClassReference(IASTClassReference reference) {	}
	public void acceptElaboratedTypeSpecifier(IASTElaboratedTypeSpecifier elaboratedTypeSpec){  }
	public void acceptMethodDeclaration(IASTMethod method) 						{	}
	public void acceptField(IASTField field) 									{	}
	
	public void enterFunctionBody(IASTFunction function){
		pushScope( function );
	}
	
	public void enterCompilationUnit(IASTCompilationUnit compilationUnit) {
		pushScope( compilationUnit );
	}
	
	public void enterNamespaceDefinition(IASTNamespaceDefinition namespaceDefinition) {
		if( searchPattern.getLimitTo() == DECLARATIONS || searchPattern.getLimitTo() == ALL_OCCURRENCES ){
			if( searchPattern instanceof NamespaceDeclarationPattern ){
				int level = searchPattern.matchLevel( namespaceDefinition ); 
				if(  level != ICSearchPattern.IMPOSSIBLE_MATCH ){
					report( namespaceDefinition, level );				
				}
			}
		}			
				
		pushScope( namespaceDefinition );
	}
	
	public void enterLinkageSpecification(IASTLinkageSpecification linkageSpec) {	}
	public void enterTemplateDeclaration(IASTTemplateDeclaration declaration) 	{	}
	public void enterTemplateSpecialization(IASTTemplateSpecialization specialization) 		{	}
	public void enterTemplateExplicitInstantiation(IASTTemplateInstantiation instantiation) {	}
	
	public void enterMethodBody(IASTMethod method) {
		pushScope( method );
	}
	
	public void exitFunctionBody(IASTFunction function) {
		popScope();	
	}
	public void exitMethodBody(IASTMethod method) {
		popScope();	
	}
	
	public void exitTemplateDeclaration(IASTTemplateDeclaration declaration) 	{}
	public void exitTemplateSpecialization(IASTTemplateSpecialization specialization) 		{	}
	public void exitTemplateExplicitInstantiation(IASTTemplateInstantiation instantiation) 	{	}
	public void exitLinkageSpecification(IASTLinkageSpecification linkageSpec) 	{	}
	
	public void exitClassSpecifier(IASTClassSpecifier classSpecification) {
		popScope();
	}
	
	public void exitNamespaceDefinition(IASTNamespaceDefinition namespaceDefinition) {
		popScope();
	}
	
	public void exitCompilationUnit(IASTCompilationUnit compilationUnit){
		popScope();
	}

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
		if( searchPattern.getLimitTo() == DECLARATIONS || searchPattern.getLimitTo() == ALL_OCCURRENCES ){
			if( searchPattern instanceof ClassDeclarationPattern ){
				int level = searchPattern.matchLevel( classSpecification ); 
				if(  level != ICSearchPattern.IMPOSSIBLE_MATCH ){
					report( classSpecification, level );				
				}
			}
		}			
		pushScope( classSpecification );
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
			
			IScanner scanner = ParserFactory.createScanner( reader, pathString, new ScannerInfo(), ParserMode.QUICK_PARSE );
			IParser  parser  = ParserFactory.createParser( scanner, null, ParserMode.QUICK_PARSE );
			parser.setRequestor( this );
			
			parser.parse();
		}
	}
	
	protected void report( IASTOffsetableNamedElement node, int accuracyLevel ){
		try {
			if( progressMonitor != null ) {
				if( progressMonitor.isCanceled() ) {
					throw new OperationCanceledException();
				} else {
					progressMonitor.worked( 1 );
				}
			}
			
			int offset = node.getElementNameOffset();
			if( offset == 0 )
				offset = node.getElementStartingOffset();
				
			if( currentResource != null ){
				
				resultCollector.accept( currentResource, 
								  offset, 
								  offset + node.getName().length(), 
								  resultCollector.createMatch( node, currentScope ), 
								  accuracyLevel );
			} else if( currentPath != null ){
				resultCollector.accept( currentPath, 
										offset, 
										offset + node.getName().length(), 
										resultCollector.createMatch( node, currentScope ), 
										accuracyLevel );				
			}
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void pushScope( IASTScope scope ){
		scopeStack.addFirst( currentScope );
		currentScope = scope;
	}
	
	private IASTScope popScope(){
		IASTScope oldScope = currentScope;
		currentScope = (scopeStack.size() > 0 ) ? (IASTScope) scopeStack.removeFirst() : null;
		return oldScope;
	}
	
	private ICSearchPattern 		searchPattern;
	private ICSearchResultCollector resultCollector;
	private IProgressMonitor 		progressMonitor;
	private IPath					currentPath 	= null;
	private ICSearchScope 			searchScope;
	private IWorkspaceRoot 			workspaceRoot;
	
	private IResource 				currentResource = null;
	private LinkedList 				resourceStack = new LinkedList();
	
	private IASTScope				currentScope = null;
	private LinkedList				scopeStack = new LinkedList();

}
