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

import java.io.CharArrayReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.parser.IParser;
import org.eclipse.cdt.core.parser.IProblem;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfoProvider;
import org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate;
import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserFactory;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ast.IASTASMDefinition;
import org.eclipse.cdt.core.parser.ast.IASTAbstractTypeSpecifierDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTClassReference;
import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTCodeScope;
import org.eclipse.cdt.core.parser.ast.IASTCompilationUnit;
import org.eclipse.cdt.core.parser.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTEnumerationReference;
import org.eclipse.cdt.core.parser.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTEnumerator;
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
import org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement;
import org.eclipse.cdt.core.parser.ast.IASTReference;
import org.eclipse.cdt.core.parser.ast.IASTScope;
import org.eclipse.cdt.core.parser.ast.IASTTemplateDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTTemplateInstantiation;
import org.eclipse.cdt.core.parser.ast.IASTTemplateSpecialization;
import org.eclipse.cdt.core.parser.ast.IASTTypedefDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTTypedefReference;
import org.eclipse.cdt.core.parser.ast.IASTUsingDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTUsingDirective;
import org.eclipse.cdt.core.parser.ast.IASTVariable;
import org.eclipse.cdt.core.parser.ast.IASTVariableReference;
import org.eclipse.cdt.core.search.ICSearchConstants;
import org.eclipse.cdt.core.search.ICSearchPattern;
import org.eclipse.cdt.core.search.ICSearchResultCollector;
import org.eclipse.cdt.core.search.ICSearchScope;
import org.eclipse.cdt.core.search.IMatch;
import org.eclipse.cdt.internal.core.model.IWorkingCopy;
import org.eclipse.cdt.internal.core.parser.ScannerInfo;
import org.eclipse.cdt.internal.core.parser.ast.complete.ASTParameterReference;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
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

	
	public static boolean VERBOSE = false;
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
	public void acceptUsingDirective(IASTUsingDirective usageDirective) 		{	}
	public void acceptUsingDeclaration(IASTUsingDeclaration usageDeclaration) 	{	}
	public void acceptASMDefinition(IASTASMDefinition asmDefinition) 			{	}
	public void acceptAbstractTypeSpecDeclaration(IASTAbstractTypeSpecifierDeclaration abstractDeclaration) {}

	public void enterLinkageSpecification(IASTLinkageSpecification linkageSpec) {	}
	public void enterTemplateDeclaration(IASTTemplateDeclaration declaration) 	{	}
	public void enterTemplateSpecialization(IASTTemplateSpecialization specialization) 		{	}
	public void enterTemplateInstantiation(IASTTemplateInstantiation instantiation) {	}

	public void exitTemplateDeclaration(IASTTemplateDeclaration declaration) 	{}
	public void exitTemplateSpecialization(IASTTemplateSpecialization specialization) 		{	}
	public void exitTemplateExplicitInstantiation(IASTTemplateInstantiation instantiation) 	{	}
	public void exitLinkageSpecification(IASTLinkageSpecification linkageSpec) 	{	}

	public void enterCodeBlock(IASTCodeScope scope) {	}
	public void exitCodeBlock(IASTCodeScope scope) 	{	}
	
	
	public void acceptTypedefDeclaration(IASTTypedefDeclaration typedef){
		lastDeclaration = typedef;
		check( DECLARATIONS, typedef );
	}
	
	public void acceptTypedefReference( IASTTypedefReference reference ){
		check( REFERENCES, reference );
	}
	
	public void acceptEnumeratorReference(IASTEnumeratorReference reference){
		check( REFERENCES, reference );	
	}
		
	public void acceptMacro(IASTMacro macro){
		check( DECLARATIONS, macro );	
	}
	
	public void acceptVariable(IASTVariable variable){
		lastDeclaration = variable;
		check( DECLARATIONS, variable );   
	}
	
	public void acceptField(IASTField field){
		lastDeclaration = field; 
		check( DECLARATIONS, field ); 	   
	}
	
	public void acceptEnumerationSpecifier(IASTEnumerationSpecifier enumeration){
		lastDeclaration = enumeration; 
		check( DECLARATIONS, enumeration );
		Iterator iter = enumeration.getEnumerators();
		while( iter.hasNext() ){
			IASTEnumerator enumerator = (IASTEnumerator) iter.next();
			lastDeclaration = enumerator;
			check ( DECLARATIONS, enumerator );
		}  
	}
		
	public void acceptFunctionDeclaration(IASTFunction function){
		lastDeclaration = function;
		check( DECLARATIONS, function );
	}
	
	public void acceptMethodDeclaration(IASTMethod method){
		lastDeclaration = method;
		check( DECLARATIONS, method );
	}
		
	public void acceptClassReference(IASTClassReference reference) {
		check( REFERENCES, reference );
	}
	
	public void acceptNamespaceReference( IASTNamespaceReference reference ){
		check( REFERENCES, reference );
	}
	
	public void acceptVariableReference( IASTVariableReference reference ){
		check( REFERENCES, reference );		
	}
	
	public void acceptFieldReference( IASTFieldReference reference ){
		check( REFERENCES, reference );
	}
	
	public void acceptEnumerationReference( IASTEnumerationReference reference ){
		check( REFERENCES, reference );
	}
	
	public void acceptFunctionReference( IASTFunctionReference reference ){
		check( REFERENCES, reference );
	}
	
	public void acceptMethodReference( IASTMethodReference reference ){
		check( REFERENCES, reference );	
	}
	
	public void enterFunctionBody(IASTFunction function){
		lastDeclaration = function;
		check( DECLARATIONS, function );
		check( DEFINITIONS, function );
		pushScope( function );
	}
	
	public void enterMethodBody(IASTMethod method) {
		lastDeclaration = method;
		check( DEFINITIONS, method );
		pushScope( method );
	}
	
	public void enterCompilationUnit(IASTCompilationUnit compilationUnit) {
		pushScope( compilationUnit );
	}
	
	public void enterNamespaceDefinition(IASTNamespaceDefinition namespaceDefinition) {
		lastDeclaration = namespaceDefinition;
		check( DECLARATIONS, namespaceDefinition );
		pushScope( namespaceDefinition );			
	}

	public void enterClassSpecifier(IASTClassSpecifier classSpecification) {
		lastDeclaration = classSpecification;
		check( DECLARATIONS, classSpecification );
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
			resource = workspaceRoot.getFileForLocation( path );
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
			
			if  (!searchScope.encloses(pathString)) continue;
			
			Reader reader = null;
			
			IPath realPath = null; 
			IProject project = null;
			
			if( workspaceRoot != null ){
				IWorkingCopy workingCopy = (IWorkingCopy)wcPaths.get( pathString );
				
				if( workingCopy != null ){
					reader = new CharArrayReader( workingCopy.getContents() );
					currentResource = workingCopy.getResource();
					realPath = currentResource.getLocation();
					project = currentResource.getProject();
				} else {
					currentResource = workspaceRoot.findMember( pathString, true );
					
					try{
						if( currentResource == null ){
							IPath path = new Path( pathString );
							IFile file = workspaceRoot.getFile( path );
							file.createLink( path, 0, null );
							project = file.getProject();
						}
						if( currentResource != null && currentResource instanceof IFile ){
							IFile file = (IFile) currentResource;
							reader = new InputStreamReader( file.getContents() );
							realPath = currentResource.getLocation();
							project = file.getProject();
						} else continue;
					} catch ( CoreException e ){
						continue;
					}
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
			
			//Get the scanner info
			IScannerInfo scanInfo = new ScannerInfo();
			IScannerInfoProvider provider = CCorePlugin.getDefault().getScannerInfoProvider(project);
			if (provider != null){
				IScannerInfo buildScanInfo = provider.getScannerInformation(project);
				scanInfo = new ScannerInfo(buildScanInfo.getDefinedSymbols(), buildScanInfo.getIncludePaths());
			}
			
			ParserLanguage language = null;
			if( project != null ){
				language = CoreModel.getDefault().hasCCNature( project ) ? ParserLanguage.CPP : ParserLanguage.C;
			} else {
				//TODO no probject, what language do we use?
				language = ParserLanguage.CPP;
			}
			IScanner scanner = ParserFactory.createScanner( reader, realPath.toOSString(), scanInfo, ParserMode.COMPLETE_PARSE, language, this );
			IParser  parser  = ParserFactory.createParser( scanner, this, ParserMode.COMPLETE_PARSE, language );
			
			if (VERBOSE)
			  MatchLocator.verbose("*** New Search for path: " + pathString);
			  
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
				if (VERBOSE)
					MatchLocator.verbose("Report Match: " + reference.getName());
			} else if( node instanceof IASTOffsetableNamedElement ){
				IASTOffsetableNamedElement offsetableElement = (IASTOffsetableNamedElement) node;
				offset = offsetableElement.getNameOffset() != 0 ? offsetableElement.getNameOffset() 
															    : offsetableElement.getStartingOffset();
				length = offsetableElement.getName().length();															  
				if (VERBOSE)
					MatchLocator.verbose("Report Match: " + offsetableElement.getName());
			}
		
			IMatch match = null;
			ISourceElementCallbackDelegate object = null;
			
			if( node instanceof IASTReference ){
				if( currentScope instanceof IASTFunction || currentScope instanceof IASTMethod ){
					object = (ISourceElementCallbackDelegate) currentScope;
				} else {
					object = lastDeclaration;
				}
			} else {
				if( currentScope instanceof IASTFunction || currentScope instanceof IASTMethod ){
					object = (ISourceElementCallbackDelegate) currentScope;
				} else {
					object = node;
				}
			}
		
			if( currentResource != null ){
				match = resultCollector.createMatch( currentResource, offset, offset + length, object );
			} else if( currentPath != null ){
				match = resultCollector.createMatch( currentPath, offset, offset + length, object );
			}
			if( match != null ){
				resultCollector.acceptMatch( match );
			}
		
		} catch (CoreException e) {
		}
	}

	private void check( LimitTo limit, ISourceElementCallbackDelegate node ){
		if( !searchPattern.canAccept( limit ) )
			return;
			
		int level = ICSearchPattern.IMPOSSIBLE_MATCH;
		
		if( node instanceof IASTReference ){
			level = searchPattern.matchLevel( ((IASTReference)node).getReferencedElement(), limit );
		} else  {
			level = searchPattern.matchLevel(  node, limit );
		} 
		
		if( level != ICSearchPattern.IMPOSSIBLE_MATCH )
		{
			report( node, level );
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
	
	private ISourceElementCallbackDelegate lastDeclaration;
	
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
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptElaboratedForewardDeclaration(org.eclipse.cdt.core.parser.ast.IASTElaboratedTypeSpecifier)
     */
    public void acceptElaboratedForewardDeclaration(IASTElaboratedTypeSpecifier elaboratedType){
		check( DECLARATIONS, elaboratedType );	
    }

	public static void verbose(String log) {
	  System.out.println("(" + Thread.currentThread() + ") " + log); 
	}

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptParameterReference(org.eclipse.cdt.internal.core.parser.ast.complete.ASTParameterReference)
     */
    public void acceptParameterReference(ASTParameterReference reference)
    {
        // TODO Auto-generated method stub
        
    }


}
