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
import java.util.Iterator;
import java.util.LinkedList;

import org.eclipse.cdt.core.parser.IParser;
import org.eclipse.cdt.core.parser.IProblem;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate;
import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.ParserFactory;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ast.*;
import org.eclipse.cdt.core.search.ICSearchConstants;
import org.eclipse.cdt.core.search.ICSearchPattern;
import org.eclipse.cdt.core.search.ICSearchResultCollector;
import org.eclipse.cdt.core.search.ICSearchScope;
import org.eclipse.cdt.core.search.IMatch;
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
	public void acceptUsingDirective(IASTUsingDirective usageDirective) 		{	}
	public void acceptUsingDeclaration(IASTUsingDeclaration usageDeclaration) 	{	}
	public void acceptASMDefinition(IASTASMDefinition asmDefinition) 			{	}
	public void acceptTypedefDeclaration(IASTTypedefDeclaration typedef) 		{	}
	
	public void acceptAbstractTypeSpecDeclaration(IASTAbstractTypeSpecifierDeclaration abstractDeclaration) {}
	public void acceptTypedefReference( IASTTypedefReference reference )        {	}
	
	public void enterLinkageSpecification(IASTLinkageSpecification linkageSpec) {	}
	public void enterTemplateDeclaration(IASTTemplateDeclaration declaration) 	{	}
	public void enterTemplateSpecialization(IASTTemplateSpecialization specialization) 		{	}
	public void enterTemplateInstantiation(IASTTemplateInstantiation instantiation) {	}

	public void exitTemplateDeclaration(IASTTemplateDeclaration declaration) 	{}
	public void exitTemplateSpecialization(IASTTemplateSpecialization specialization) 		{	}
	public void exitTemplateExplicitInstantiation(IASTTemplateInstantiation instantiation) 	{	}
	public void exitLinkageSpecification(IASTLinkageSpecification linkageSpec) 	{	}
	
	public void acceptVariable(IASTVariable variable){
		check( DECLARATIONS, VariableDeclarationPattern.class, variable );   
	}
	
	public void acceptField(IASTField field){ 
		check( DECLARATIONS, FieldDeclarationPattern.class, field ); 	   
	}
	
	public void acceptEnumerationSpecifier(IASTEnumerationSpecifier enumeration){ 
		check( DECLARATIONS, ClassDeclarationPattern.class, enumeration );
		Iterator iter = enumeration.getEnumerators();
		while( iter.hasNext() ){
			check ( DECLARATIONS, FieldDeclarationPattern.class, (ISourceElementCallbackDelegate) iter.next() );
		}  
	}
		
	public void acceptFunctionDeclaration(IASTFunction function){
		check( DECLARATIONS, FunctionDeclarationPattern.class, function );
	}
	
	public void acceptMethodDeclaration(IASTMethod method){
		check( DECLARATIONS, MethodDeclarationPattern.class, method );
	}
		
	public void acceptClassReference(IASTClassReference reference) {
		check( REFERENCES, ClassDeclarationPattern.class, reference );
	}
	
	public void acceptNamespaceReference( IASTNamespaceReference reference ){
		check( REFERENCES, NamespaceDeclarationPattern.class, reference );
	}
	
	public void acceptVariableReference( IASTVariableReference reference ){
		check( REFERENCES, VariableDeclarationPattern.class, reference );		
	}
	
	public void acceptFieldReference( IASTFieldReference reference ){
		check( REFERENCES, FieldDeclarationPattern.class, reference );
	}
	
	public void acceptEnumerationReference( IASTEnumerationReference reference ){
		check( REFERENCES, ClassDeclarationPattern.class, reference );
	}
	
	public void acceptFunctionReference( IASTFunctionReference reference ){
		check( REFERENCES, FunctionDeclarationPattern.class,  reference );
	}
	
	public void acceptMethodReference( IASTMethodReference reference ){
		check( REFERENCES, MethodDeclarationPattern.class, reference );	
	}
	
	public void enterFunctionBody(IASTFunction function){
		check( DEFINITIONS, FunctionDeclarationPattern.class, function );
		pushScope( function );
	}
	
	public void enterMethodBody(IASTMethod method) {
		check( DEFINITIONS, MethodDeclarationPattern.class, method );
		pushScope( method );
	}
	
	public void enterCompilationUnit(IASTCompilationUnit compilationUnit) {
		pushScope( compilationUnit );
	}
	
	public void enterNamespaceDefinition(IASTNamespaceDefinition namespaceDefinition) {
		check( DECLARATIONS, NamespaceDeclarationPattern.class, namespaceDefinition );			
		pushScope( namespaceDefinition );
	}

	public void enterClassSpecifier(IASTClassSpecifier classSpecification) {
		check( DECLARATIONS, ClassDeclarationPattern.class, classSpecification );		
		pushScope( classSpecification );
	}
	
	public void exitFunctionBody(IASTFunction function) {
		popScope();	
	}

	public void exitMethodBody(IASTMethod method) {
		popScope();	
	}

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
			if( progressMonitor != null ) {
				if( progressMonitor.isCanceled() ){
					throw new OperationCanceledException();
				} else {
					progressMonitor.worked( 1 );
				}
			}
			
			String pathString = paths[ i ];
			
			//skip duplicates
			if( i > 0 && pathString.equals( paths[ i - 1 ] ) ) continue;
			
			Reader reader = null;
			
			IPath realPath = null; 
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
						realPath = currentResource.getLocation();
					} else continue;
				} catch ( CoreException e ){
					continue;
				}
			} else {
				IPath path = new Path( pathString );
				try {
					currentPath = path;
					reader = new FileReader( path.toFile() );
					realPath = currentPath; 
				} catch (FileNotFoundException e) {
					continue;
				}
			}
			
			IScanner scanner = ParserFactory.createScanner( reader, realPath.toOSString(), new ScannerInfo(), ParserMode.COMPLETE_PARSE, this );
			IParser  parser  = ParserFactory.createParser( scanner, this, ParserMode.COMPLETE_PARSE );
			
			parser.parse();
		}
	}
	
	protected void report( ISourceElementCallbackDelegate node, int accuracyLevel ){
		try {
			int offset = 0;
			int length = 0;
			
			if( node instanceof IASTReference ){
				IASTReference reference = (IASTReference) node;
				offset = reference.getOffset();
				length = reference.getName().length();
			} else if( node instanceof IASTOffsetableNamedElement ){
				IASTOffsetableNamedElement offsetableElement = (IASTOffsetableNamedElement) node;
				offset = offsetableElement.getNameOffset() != 0 ? offsetableElement.getNameOffset() 
															    : offsetableElement.getStartingOffset();
				length = offsetableElement.getName().length();															  
			}
				
			IMatch match = null;		
			if( currentResource != null ){
				match = resultCollector.createMatch( currentResource, offset, offset + length, node, currentScope );
			} else if( currentPath != null ){
				match = resultCollector.createMatch( currentPath, offset, offset + length, node, currentScope );
			}
			if( match != null ){
				resultCollector.acceptMatch( match );
			}
		
		} catch (CoreException e) {
		}
	}

	private void check( LimitTo limit, Class patternClass, ISourceElementCallbackDelegate node ){
		if( searchPattern.getLimitTo() != limit && searchPattern.getLimitTo() != ALL_OCCURRENCES )
			return;
			
		if( searchPattern.getClass() == patternClass ){
			int level = ICSearchPattern.IMPOSSIBLE_MATCH;
			
			if( node instanceof IASTReference ){
				level = searchPattern.matchLevel( ((IASTReference)node).getReferencedElement() );
			} else  {
				level = searchPattern.matchLevel(  node );
			} 
			
			if( level != ICSearchPattern.IMPOSSIBLE_MATCH )
			{
				report( node, level );
			}
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
