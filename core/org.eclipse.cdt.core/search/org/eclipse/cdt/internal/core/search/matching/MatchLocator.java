/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 *******************************************************************************/
/*
 * Created on Jun 13, 2003
 */
package org.eclipse.cdt.internal.core.search.matching;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.IParser;
import org.eclipse.cdt.core.parser.IProblem;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfoProvider;
import org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate;
import org.eclipse.cdt.core.parser.ParserFactory;
import org.eclipse.cdt.core.parser.ParserFactoryError;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ParserUtil;
import org.eclipse.cdt.core.parser.ScannerInfo;
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
import org.eclipse.cdt.core.parser.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTParameterReference;
import org.eclipse.cdt.core.parser.ast.IASTReference;
import org.eclipse.cdt.core.parser.ast.IASTScope;
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
import org.eclipse.cdt.core.search.ICSearchPattern;
import org.eclipse.cdt.core.search.ICSearchResultCollector;
import org.eclipse.cdt.core.search.ICSearchScope;
import org.eclipse.cdt.core.search.IMatch;
import org.eclipse.cdt.core.search.IMatchLocator;
import org.eclipse.cdt.internal.core.parser.scanner2.ObjectSet;
import org.eclipse.cdt.internal.core.search.AcceptMatchOperation;
import org.eclipse.cdt.internal.core.search.indexing.IndexProblemHandler;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
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
public class MatchLocator implements IMatchLocator{

	
	ArrayList matchStorage;
	
	protected ObjectSet encounteredHeaders;
	protected ObjectSet tempHeaderSet;
	
	public static boolean VERBOSE = false;
	
	private boolean checkForMatch = true;
	/**
	 * 
	 */
	public MatchLocator( ICSearchPattern pattern, ICSearchResultCollector collector, ICSearchScope scope) {
		super();
		searchPattern = pattern;
		resultCollector = collector;
		searchScope = scope;	
	}

	public boolean acceptProblem(IProblem problem) 								{ return IndexProblemHandler.ruleOnProblem(problem, ParserMode.COMPLETE_PARSE );	}
	public void acceptUsingDirective(IASTUsingDirective usageDirective) 		{	}
	public void acceptUsingDeclaration(IASTUsingDeclaration usageDeclaration) 	{	}
	public void acceptASMDefinition(IASTASMDefinition asmDefinition) 			{	}
	public void acceptAbstractTypeSpecDeclaration(IASTAbstractTypeSpecifierDeclaration abstractDeclaration) {}

	public void enterTemplateDeclaration(IASTTemplateDeclaration declaration) 	{	}
	public void enterTemplateSpecialization(IASTTemplateSpecialization specialization) 		{	}
	public void enterTemplateInstantiation(IASTTemplateInstantiation instantiation) {	}

	public void exitTemplateDeclaration(IASTTemplateDeclaration declaration) 	{}
	public void exitTemplateSpecialization(IASTTemplateSpecialization specialization) 		{	}
	public void exitTemplateExplicitInstantiation(IASTTemplateInstantiation instantiation) 	{	}
	
	public void enterCodeBlock(IASTCodeScope scope) {	}
	public void exitCodeBlock(IASTCodeScope scope) 	{	}
	
	public void enterLinkageSpecification(IASTLinkageSpecification linkageSpec){
		pushScope( linkageSpec );	
	}

	public void exitLinkageSpecification(IASTLinkageSpecification linkageSpec){
		popScope();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptParameterReference(org.eclipse.cdt.internal.core.parser.ast.complete.ASTParameterReference)
	 */
	public void acceptParameterReference(IASTParameterReference reference)
	{	
		if (checkForMatch)
			check( REFERENCES, reference );        
	}
	

	public void acceptTemplateParameterReference(IASTTemplateParameterReference reference) 
	{
		if (checkForMatch)
			check( REFERENCES, reference );	
	}
	
	public void acceptTypedefDeclaration(IASTTypedefDeclaration typedef){
		lastDeclaration = typedef;
		
		if (checkForMatch)
			check( DECLARATIONS, typedef );
	}
	
	public void acceptTypedefReference( IASTTypedefReference reference ){
		if (checkForMatch)
			check( REFERENCES, reference );
	}
	
	public void acceptEnumeratorReference(IASTEnumeratorReference reference){
		if (checkForMatch)
			check( REFERENCES, reference );	
	}
		
	public void acceptMacro(IASTMacro macro){
		if (checkForMatch)
			check( DECLARATIONS, macro );	
	}
	
	public void acceptVariable(IASTVariable variable){
		lastDeclaration = variable;
		
		if (checkForMatch){
			check( DECLARATIONS, variable );
			
			//A declaration is a definition unless...:
			//it contains the extern specifier or a linkage-spec and no initializer
			if( variable.getInitializerClause() != null ||
			    ( !variable.isExtern() && !(currentScope instanceof IASTLinkageSpecification) ) ){
				check( DEFINITIONS, variable );
			}
		}
	}
	
	public void acceptField(IASTField field){
		lastDeclaration = field;
		
		if (checkForMatch){
			if( currentScope instanceof IASTClassSpecifier ){
				check( DECLARATIONS, field ); 	   
				if( !field.isStatic() ){
					check( DEFINITIONS, field ); 
				}
			} else {
				check( DEFINITIONS, field );
			}
		}
	}
	
	public void acceptEnumerationSpecifier(IASTEnumerationSpecifier enumeration){
		lastDeclaration = enumeration; 
		
		if (checkForMatch){		
			check( DECLARATIONS, enumeration );
			Iterator iter = enumeration.getEnumerators();
			while( iter.hasNext() ){
				IASTEnumerator enumerator = (IASTEnumerator) iter.next();
				lastDeclaration = enumerator;
				check ( DECLARATIONS, enumerator );
			}  
		}
	}
		
	public void acceptFunctionDeclaration(IASTFunction function){
		lastDeclaration = function;
		
		if (checkForMatch)
			check( DECLARATIONS, function );
	}
	
	public void acceptMethodDeclaration(IASTMethod method){
		lastDeclaration = method;
		
		if (checkForMatch)
			check( DECLARATIONS, method );
	}
		
	public void acceptClassReference(IASTClassReference reference) {
		if (checkForMatch)
			check( REFERENCES, reference );
	}
	
	public void acceptNamespaceReference( IASTNamespaceReference reference ){
		if (checkForMatch)
			check( REFERENCES, reference );
	}
	
	public void acceptVariableReference( IASTVariableReference reference ){
		if (checkForMatch)
			check( REFERENCES, reference );		
	}
	
	public void acceptFieldReference( IASTFieldReference reference ){
		if (checkForMatch)
			check( REFERENCES, reference );
	}
	
	public void acceptEnumerationReference( IASTEnumerationReference reference ){
		if (checkForMatch)
			check( REFERENCES, reference );
	}
	
	public void acceptFunctionReference( IASTFunctionReference reference ){
		if (checkForMatch)
			check( REFERENCES, reference );
	}
	
	public void acceptMethodReference( IASTMethodReference reference ){
		if (checkForMatch)
			check( REFERENCES, reference );	
	}
	
	public void enterFunctionBody(IASTFunction function){
		lastDeclaration = function;
		
		if (checkForMatch)
		{
			if( !function.previouslyDeclared() )
				check( DECLARATIONS, function );
				
			check( DEFINITIONS, function );
			
			Iterator parms =function.getParameters();
			while (parms.hasNext()){
				Object tempParm = parms.next();
				if (tempParm instanceof IASTParameterDeclaration){
					check( DECLARATIONS, ((IASTParameterDeclaration)tempParm));
				}
			}
		}
		
		pushScope( function );
	}
	
	public void enterMethodBody(IASTMethod method) {
		lastDeclaration = method;
		
		if (checkForMatch){
			if( !method.previouslyDeclared() )
				check( DECLARATIONS, method );
				
			check( DEFINITIONS, method );
			
			
			Iterator parms =method.getParameters();
			while (parms.hasNext()){
				Object tempParm = parms.next();
				if (tempParm instanceof IASTParameterDeclaration){
					check( DECLARATIONS, ((IASTParameterDeclaration)tempParm));
				}
			}
		}

		pushScope( method );
	}
	
	public void enterCompilationUnit(IASTCompilationUnit compilationUnit) {
		pushScope( compilationUnit );
	}
	
	public void enterNamespaceDefinition(IASTNamespaceDefinition namespaceDefinition) {
		lastDeclaration = namespaceDefinition;
		
		if (checkForMatch){
			check( DECLARATIONS, namespaceDefinition );
			check( DEFINITIONS, namespaceDefinition );
		}
		
		pushScope( namespaceDefinition );			
	}

	public void enterClassSpecifier(IASTClassSpecifier classSpecification) {
		lastDeclaration = classSpecification;
		
		if (checkForMatch){
			check( DECLARATIONS, classSpecification );
		}
		
		pushScope( classSpecification );		
	}
	
	public void exitFunctionBody(IASTFunction function) {
		popScope();	
	}

	public void exitMethodBody(IASTMethod method) {
		popScope();	
	}

	public void exitClassSpecifier(IASTClassSpecifier classSpecification) {
		if (checkForMatch){
			check(DECLARATIONS, classSpecification);
		}
		
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
		if (!encounteredHeaders.containsKey(includePath)){
			//this header has not been seen before
			searchStack.addFirst(new Boolean(checkForMatch));
			checkForMatch = true;
			if (!tempHeaderSet.containsKey(includePath)){
				tempHeaderSet.put(includePath);
			}
		}
		else{
			//this header has been seen before; don't bother processing it
			searchStack.addFirst(new Boolean(checkForMatch));
			checkForMatch = false;
		}
			
		if( workspaceRoot != null ){
			resource = workspaceRoot.getFileForLocation( path );
//			if( resource == null ){
//				//TODO:What to do if the file is not in the workspace?				
			//				IFile file = currentResource.getProject().getFile(
			// inclusion.getName() );
//				try{
//					file.createLink( path, 0, null );
//				} catch ( CoreException e ){
//					file = null;
//				}
//				resource = file;
//			}
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
		
		//set match for current level
		Boolean check= (Boolean) searchStack.removeFirst();
		checkForMatch = check.booleanValue();
	}
		
   
	public void locateMatches( String [] paths, IWorkspace workspace, IWorkingCopy[] workingCopies ) throws InterruptedException{
		
		matchStorage = new ArrayList();
		encounteredHeaders= new ObjectSet(32);
		tempHeaderSet = new ObjectSet(32);
		
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
			progressMonitor.beginTask( "", length ); //$NON-NLS-1$
		}
		
		for( int i = 0; i < length; i++ ){
			if( progressMonitor != null ) {
				if( progressMonitor.isCanceled() ){
					throw new InterruptedException();
				} else {
					progressMonitor.worked( 1 );
				}
			}
			
			String pathString = paths[ i ];
			
			//skip duplicates
			if( i > 0 && pathString.equals( paths[ i - 1 ] ) ) continue;
			
			if  (!searchScope.encloses(pathString)) continue;
			
			IFile tempFile=workspaceRoot.getFile(new Path(pathString));
			IPath tempLocation =tempFile.getLocation();
			if ((tempLocation != null) && (encounteredHeaders.containsKey(tempLocation.toOSString()))) continue;
			
			CodeReader reader = null;
			
			realPath = null; 
			IProject project = null;
			
			if( workspaceRoot != null ){
				IWorkingCopy workingCopy = (IWorkingCopy)wcPaths.get( pathString );
				
				if( workingCopy != null ){
					currentResource = workingCopy.getResource();
					if ( currentResource != null && currentResource.isAccessible() ) {
						reader = new CodeReader(currentResource.getLocation().toOSString(), workingCopy.getContents()); 
						realPath = currentResource.getLocation();
						project = currentResource.getProject();
					} else {
						continue;
					}
				} else {
					currentResource = workspaceRoot.findMember( pathString, true );

					InputStream contents = null;
					try{
						if( currentResource != null ){
							if (currentResource.isAccessible() && currentResource instanceof IFile) {
								IFile file = (IFile) currentResource;
								contents = file.getContents();
								reader = new CodeReader(currentResource.getLocation().toOSString(), contents);
								realPath = currentResource.getLocation();
								project = file.getProject();
							} else {
								continue;
							}
						}
					} catch ( CoreException e ){
						continue;
					} catch ( IOException e ) {
						continue;
					} finally {
						if (contents != null) {
							try {
								contents.close();
							} catch (IOException io) {
								// ignore.
							}
						}
					}
				}
			}
			if( currentResource == null ) {
				try {
					IPath path = new Path( pathString );
					currentPath = path;
					reader = new CodeReader(pathString);
					realPath = currentPath; 
				} catch (IOException e) {
					continue;
				}
			}
			
			//Set checkForMatch to true
			checkForMatch = true;
			
			//Get the scanner info
			IScannerInfo scanInfo = new ScannerInfo();
			IScannerInfoProvider provider = CCorePlugin.getDefault().getScannerInfoProvider(project);
			if (provider != null){
				IScannerInfo buildScanInfo = provider.getScannerInformation(currentResource != null ? currentResource : project);
				if( buildScanInfo != null )
					scanInfo = new ScannerInfo(buildScanInfo.getDefinedSymbols(), buildScanInfo.getIncludePaths());
			}
			
			ParserLanguage language = null;
			if( project != null ){
				language = CoreModel.hasCCNature( project ) ? ParserLanguage.CPP : ParserLanguage.C;
			} else {
				//TODO no project, what language do we use?
				language = ParserLanguage.CPP;
			}
			
			IParser parser = null;
			try
			{
				IScanner scanner = ParserFactory.createScanner( reader, scanInfo, ParserMode.COMPLETE_PARSE, language, this, ParserUtil.getScannerLogService(), null );
				parser  = ParserFactory.createParser( scanner, this, ParserMode.COMPLETE_PARSE, language, ParserUtil.getParserLogService() );
			}
			catch( ParserFactoryError pfe )
			{
				
			}
			
			if (VERBOSE)
			  MatchLocator.verbose("*** New Search for path: " + pathString); //$NON-NLS-1$
			  
			
			try{ 
				parser.parse();
			}
			catch(Exception ex){
				if (VERBOSE){
					ex.printStackTrace();
				}
			}
			catch(VirtualMachineError vmErr){
				if (VERBOSE){
					MatchLocator.verbose("MatchLocator VM Error: "); //$NON-NLS-1$
					vmErr.printStackTrace();
				}
			} finally { 
				encounteredHeaders.addAll(tempHeaderSet);
				tempHeaderSet.clear();
				scopeStack.clear();
				resourceStack.clear();
				searchStack.clear();
				lastDeclaration = null;
				currentScope = null;
				parser = null;
			}
			
			if( matchStorage.size() > 0 ){
				AcceptMatchOperation acceptMatchOp = new AcceptMatchOperation( resultCollector, matchStorage );
				try {
					CCorePlugin.getWorkspace().run(acceptMatchOp,null);
				} catch (CoreException e) {}
				
				matchStorage.clear();
			}
		}
	}
	
	protected void report( ISourceElementCallbackDelegate node, int accuracyLevel ){
		try {
			if( currentResource != null && !searchScope.encloses(currentResource.getFullPath().toOSString() ) ){
				return;
			}
			
			int offset = 0;
			int end = 0;
			
			if( node instanceof IASTReference ){
				IASTReference reference = (IASTReference) node;
				offset = reference.getOffset();
				end = offset + reference.getName().length();
				if (VERBOSE)
					MatchLocator.verbose("Report Match: " + reference.getName()); //$NON-NLS-1$
			} else if( node instanceof IASTOffsetableNamedElement ){
				IASTOffsetableNamedElement offsetableElement = (IASTOffsetableNamedElement) node;
				offset = offsetableElement.getNameOffset() != 0 ? offsetableElement.getNameOffset() 
															    : offsetableElement.getStartingOffset();
				end = offsetableElement.getNameEndOffset();
				if( end == 0 ){
					end = offset + offsetableElement.getName().length();
				}
																						  
				if (VERBOSE)
					MatchLocator.verbose("Report Match: " + offsetableElement.getName()); //$NON-NLS-1$
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
					//local declaration, only report if not being filtered
					if( shouldExcludeLocalDeclarations ){
						return;
					}
					
					object = (ISourceElementCallbackDelegate) currentScope;
				} else {
					object = node;
				}
			}
			
			if( currentResource != null ){
				match = resultCollector.createMatch( currentResource, offset, end, object, null );
			} else if( currentPath != null ){
				match = resultCollector.createMatch( currentPath, offset, end, object, realPath );
			}
			if( match != null ){
				//Save till later
				//resultCollector.acceptMatch( match );
				matchStorage.add(match);
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
	
	public void setShouldExcludeLocalDeclarations( boolean exclude ){
		shouldExcludeLocalDeclarations = exclude;
	}
	
	private boolean shouldExcludeLocalDeclarations = false;
	
	private ISourceElementCallbackDelegate lastDeclaration;
	
	private ICSearchPattern 		searchPattern;
	private ICSearchResultCollector resultCollector;
	private IProgressMonitor 		progressMonitor;
	private IPath					currentPath 	= null;
	private ICSearchScope 			searchScope;
	private IWorkspaceRoot 			workspaceRoot;
	private IPath 					realPath; 
	
	private IResource 				currentResource = null;
	private LinkedList 				resourceStack = new LinkedList();
	
	private IASTScope				currentScope = null;
	private LinkedList				scopeStack = new LinkedList();
	
	private LinkedList				searchStack = new LinkedList();

	/* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptElaboratedForewardDeclaration(org.eclipse.cdt.core.parser.ast.IASTElaboratedTypeSpecifier)
     */
    public void acceptElaboratedForewardDeclaration(IASTElaboratedTypeSpecifier elaboratedType){
		check( DECLARATIONS, elaboratedType );	
    }

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptFriendDeclaration(org.eclipse.cdt.core.parser.ast.IASTDeclaration)
	 */
	public void acceptFriendDeclaration(IASTDeclaration declaration) {
		// TODO Auto-generated method stub
		
	}

	public static void verbose(String log) {
	  System.out.println("(" + Thread.currentThread() + ") " + log);  //$NON-NLS-1$ //$NON-NLS-2$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#createReader(java.lang.String)
	 */
	public CodeReader createReader(String finalPath, Iterator workingCopies) {
		return ParserUtil.createReader(finalPath,workingCopies);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.search.IMatchLocator#setProgressMonitor(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void setProgressMonitor(IProgressMonitor progressMonitor) {
		this.progressMonitor = progressMonitor;
	}
}
