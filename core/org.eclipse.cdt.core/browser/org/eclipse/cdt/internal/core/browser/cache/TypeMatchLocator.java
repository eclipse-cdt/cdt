/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 *     QNX Software Systems - adapted for type search
 *******************************************************************************/
package org.eclipse.cdt.internal.core.browser.cache;

import java.io.CharArrayReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.core.parser.DefaultProblemHandler;
import org.eclipse.cdt.core.parser.IParser;
import org.eclipse.cdt.core.parser.IProblem;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfoProvider;
import org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate;
import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.ParserFactory;
import org.eclipse.cdt.core.parser.ParserFactoryError;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ParserUtil;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.core.parser.ast.ASTClassKind;
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
import org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement;
import org.eclipse.cdt.core.parser.ast.IASTParameterReference;
import org.eclipse.cdt.core.parser.ast.IASTQualifiedNameElement;
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
import org.eclipse.cdt.core.search.ICSearchConstants;
import org.eclipse.cdt.core.search.ICSearchPattern;
import org.eclipse.cdt.internal.core.browser.util.PathUtil;
import org.eclipse.cdt.internal.core.browser.util.SimpleStack;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;

public class TypeMatchLocator implements ISourceElementRequestor, ICSearchConstants {
	private ISourceElementCallbackDelegate lastDeclaration;
	private ICSearchPattern searchPattern;
	private TypeMatchCollector resultCollector;
	private IProgressMonitor progressMonitor;
	private IWorkspaceRoot workspaceRoot= null;
	private SimpleStack scopeStack= new SimpleStack();
	private SimpleStack resourceStack= new SimpleStack();
	private static boolean VERBOSE= false;

	public TypeMatchLocator(TypeMatchCollector collector) {
		super();
		searchPattern= new TypeSearchPattern();
		resultCollector= collector;
		progressMonitor= collector.getProgressMonitor();
		if (progressMonitor == null)
			progressMonitor= new NullProgressMonitor();
	}

	public boolean acceptProblem(IProblem problem) {
		return DefaultProblemHandler.ruleOnProblem(problem, ParserMode.COMPLETE_PARSE );
	}
	public void acceptUsingDirective(IASTUsingDirective usageDirective) { }
	public void acceptUsingDeclaration(IASTUsingDeclaration usageDeclaration) {	}
	public void acceptASMDefinition(IASTASMDefinition asmDefinition) { }
	public void acceptAbstractTypeSpecDeclaration(IASTAbstractTypeSpecifierDeclaration abstractDeclaration) { }
	public void enterTemplateDeclaration(IASTTemplateDeclaration declaration) { }
	public void enterTemplateSpecialization(IASTTemplateSpecialization specialization) { }
	public void enterTemplateInstantiation(IASTTemplateInstantiation instantiation) { }
	public void exitTemplateDeclaration(IASTTemplateDeclaration declaration) { }
	public void exitTemplateSpecialization(IASTTemplateSpecialization specialization) { }
	public void exitTemplateExplicitInstantiation(IASTTemplateInstantiation instantiation) { }
	public void acceptParameterReference(IASTParameterReference reference) { }
	public void acceptTemplateParameterReference(IASTTemplateParameterReference reference) { }
	public void acceptTypedefReference( IASTTypedefReference reference ) { }
	public void acceptEnumeratorReference(IASTEnumeratorReference reference) { }
	public void acceptClassReference(IASTClassReference reference) { }
	public void acceptNamespaceReference( IASTNamespaceReference reference ) { }
	public void acceptVariableReference( IASTVariableReference reference ) { }
	public void acceptFieldReference( IASTFieldReference reference ) { }
	public void acceptEnumerationReference( IASTEnumerationReference reference ) { }
	public void acceptFunctionReference( IASTFunctionReference reference ) { }
	public void acceptMethodReference( IASTMethodReference reference ) { }
	public void acceptField(IASTField field) { }
	public void acceptMacro(IASTMacro macro) { }
	public void acceptVariable(IASTVariable variable) { }
	public void acceptFunctionDeclaration(IASTFunction function) { }
	public void acceptMethodDeclaration(IASTMethod method) { }
	public void enterCodeBlock(IASTCodeScope scope) { }
	public void exitCodeBlock(IASTCodeScope scope) { }
	
	public void acceptTypedefDeclaration(IASTTypedefDeclaration typedef){
		lastDeclaration = typedef;
		check( DECLARATIONS, typedef );
	}
	
	public void acceptEnumerationSpecifier(IASTEnumerationSpecifier enumeration){
		lastDeclaration = enumeration; 
		check( DECLARATIONS, enumeration );
	}
		
    public void acceptElaboratedForewardDeclaration(IASTElaboratedTypeSpecifier elaboratedType){
		check( DECLARATIONS, elaboratedType );	
    }

	public void enterLinkageSpecification(IASTLinkageSpecification linkageSpec){
		pushScope( linkageSpec );	
	}
	public void exitLinkageSpecification(IASTLinkageSpecification linkageSpec){
		popScope();
	}

	public void enterCompilationUnit(IASTCompilationUnit compilationUnit) {
		pushScope( compilationUnit );
	}
	public void exitCompilationUnit(IASTCompilationUnit compilationUnit){
		popScope();
	}
	
	public void enterFunctionBody(IASTFunction function){
		pushScope( function );
	}
	public void exitFunctionBody(IASTFunction function) {
		popScope();	
	}

	public void enterMethodBody(IASTMethod method) {
		pushScope( method );
	}
	public void exitMethodBody(IASTMethod method) {
		popScope();	
	}
	
	public void enterNamespaceDefinition(IASTNamespaceDefinition namespaceDefinition) {
		lastDeclaration = namespaceDefinition;
		check( DECLARATIONS, namespaceDefinition );
		check( DEFINITIONS, namespaceDefinition );
		pushScope( namespaceDefinition );			
	}
	public void exitNamespaceDefinition(IASTNamespaceDefinition namespaceDefinition) {
		popScope();
	}

	public void enterClassSpecifier(IASTClassSpecifier classSpecification) {
		lastDeclaration = classSpecification;
		check( DECLARATIONS, classSpecification );
		pushScope( classSpecification );		
	}
	public void exitClassSpecifier(IASTClassSpecifier classSpecification) {
		popScope();
	}

	public void enterInclusion(IASTInclusion inclusion) {
		String includePath = inclusion.getFullFileName();

		IPath path = new Path( includePath );
		IResource resource = null;
		
		if (workspaceRoot != null)
			resource = workspaceRoot.getFileForLocation( path );
		
		if (resource == null) {
			// we need to standardize the paths for external headers
			path = PathUtil.getCanonicalPath(includePath);
		}
		
		if (resource != null)
			resourceStack.push(resource);
		else
			resourceStack.push(path);

		if (progressMonitor.isCanceled())
			throw new OperationCanceledException();
	}

	public void exitInclusion(IASTInclusion inclusion) {
		resourceStack.pop();

		if (progressMonitor.isCanceled())
			throw new OperationCanceledException();
	}
		
	protected void report( ISourceElementCallbackDelegate node, int accuracyLevel ){
		int offset = 0;
		int end = 0;
		IASTOffsetableNamedElement offsetable = null;
		String name = null;
		int type = 0;

		if( node instanceof IASTReference ){
			IASTReference reference = (IASTReference) node;
			offset = reference.getOffset();
			end = offset + reference.getName().length();
			if (VERBOSE)
				verbose("Report Match: " + reference.getName()); //$NON-NLS-1$
		} else if( node instanceof IASTOffsetableNamedElement ){
			offsetable= (IASTOffsetableNamedElement) node;
			offset = offsetable.getNameOffset() != 0 ? offsetable.getNameOffset() 
														    : offsetable.getStartingOffset();
			end = offsetable.getNameEndOffset();
			if( end == 0 ){
				end = offset + offsetable.getName().length();
			}
																					  
			if (VERBOSE)
				verbose("Report Match: " + offsetable.getName()); //$NON-NLS-1$
		}

		if (node instanceof IASTReference)
			node = lastDeclaration;
	
		if (node instanceof IASTReference) {
			offsetable = (IASTOffsetableNamedElement) ((IASTReference)node).getReferencedElement();
			name = ((IASTReference)node).getName();
		} else if (node instanceof IASTOffsetableNamedElement) {
			offsetable = (IASTOffsetableNamedElement)node;
			name = offsetable.getName();
		} else {
			return;
		}
		
		// skip unnamed structs
		if (name == null || name.length() == 0)
			return;

		// skip unused types
		type= getElementType(offsetable);
		if (type == 0) {
			return;
		}
		
		// collect enclosing names
		String[] enclosingNames = null;
		if (offsetable instanceof IASTQualifiedNameElement) {
			String[] names = ((IASTQualifiedNameElement) offsetable).getFullyQualifiedName();
			if (names != null && names.length > 1) {
				enclosingNames = new String[names.length-1];
				System.arraycopy(names, 0, enclosingNames, 0, names.length-1);
			}
		}

//		// collect enclosing files
//		IPath[] enclosingPaths= null;
//		Object[] sourceRefs= resourceStack.toArray();
//		// assert(sourceRefs.length > 0)
//
//		// walk through resource stack and
//		// collect enclosing paths
//		enclosingPaths= new IPath[sourceRefs.length-1];
//		for (int i= 0; i < sourceRefs.length-1; ++i) {
//			Object obj= sourceRefs[i];
//			IPath sourcePath= null;
//			if (obj instanceof IResource) {
//				IResource res= (IResource) obj;
//				enclosingPaths[i]= res.getFullPath();
//			} else {
//				enclosingPaths[i]= (IPath) obj;
//			}
//		}
		
		IResource resource= null;
		IPath path= null;
		Object obj= resourceStack.top();
		if (obj instanceof IResource)
			resource= (IResource) obj;
		else
			path= (IPath) obj;
		
		resultCollector.acceptType(name, type, enclosingNames, resource, path, offset, end);
	}

	private void check( LimitTo limit, ISourceElementCallbackDelegate node ){

		if (progressMonitor.isCanceled())
			throw new OperationCanceledException();
		
		// skip local declarations
		IASTScope currentScope= (IASTScope)scopeStack.top();
		if (currentScope instanceof IASTFunction || currentScope instanceof IASTMethod) {
			return;
		}

		// always limit == DECLARATIONS
//		
//		if( !searchPattern.canAccept( limit ) )
//			return;
			
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
		scopeStack.push(scope);

		if (progressMonitor.isCanceled())
			throw new OperationCanceledException();
	}
	
	private IASTScope popScope(){
		IASTScope oldScope= (IASTScope) scopeStack.pop();

		if (progressMonitor.isCanceled())
			throw new OperationCanceledException();

		return oldScope;
	}
 
	private static void verbose(String log) {
	  System.out.println("(" + Thread.currentThread() + ") " + log);  //$NON-NLS-1$ //$NON-NLS-2$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#createReader(java.lang.String)
	 */
	public Reader createReader(String finalPath, Iterator workingCopies) {
		return ParserUtil.createReader(finalPath, workingCopies);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#parserTimeout()
	 */
	public boolean parserTimeout() {
		return false;
	}


	public void locateMatches(Set searchPaths, IWorkspace workspace, IWorkingCopy[] workingCopies) throws InterruptedException {
		workspaceRoot= (workspace != null) ? workspace.getRoot() : null;
		
		Set searchablePaths= new HashSet(searchPaths);
		Map workingCopyMap= null;

		if (workingCopies != null && workingCopies.length > 0) {
			workingCopyMap= new HashMap(workingCopies.length);
			for (int i= 0; i < workingCopies.length; ++i) {
				IWorkingCopy workingCopy= workingCopies[i];
				IPath wcPath= workingCopy.getOriginalElement().getPath();
				workingCopyMap.put(wcPath, workingCopy);
				searchablePaths.add(wcPath);
			}
		}

		progressMonitor.beginTask("", searchablePaths.size()); //$NON-NLS-1$
		
		for (Iterator i= searchablePaths.iterator(); i.hasNext(); ) {

			if (progressMonitor.isCanceled())
				throw new InterruptedException();

			IPath path= (IPath) i.next();

			if (!resultCollector.beginParsing(path))
				continue;
			
			Reader reader= null;
			IPath realPath= null; 
			IProject project= null;
			IResource currentResource= null;
			IPath currentPath= null;
			
			progressMonitor.worked(1);
			
			if (workspaceRoot != null) {
				IWorkingCopy workingCopy= null;
				if (workingCopyMap != null)
					workingCopy= (IWorkingCopy) workingCopyMap.get(path);
				if (workingCopy != null) {
					currentResource= workingCopy.getResource();
					reader= new CharArrayReader(workingCopy.getContents());
				} else {
					currentResource= workspaceRoot.findMember(path, true);
					if (currentResource != null && currentResource instanceof IFile) {
						IFile file= (IFile) currentResource;
						try {
							reader= new InputStreamReader(file.getContents());
						} catch (CoreException ex) {
							continue;
						}
					}
				}
			}

			if (currentResource == null) {
				try {
					reader= new FileReader(path.toFile());
				} catch (FileNotFoundException ex) {
					continue;
				}
				currentPath= path;
				realPath= currentPath;
				project= null;
			} else {
				currentPath= null;
				realPath= currentResource.getLocation();
				project= currentResource.getProject();
			}
			
			if (currentResource != null)
				resourceStack.push(currentResource);
			else
				resourceStack.push(currentPath);
				
			parseMatches(path, reader, realPath, project);
			
			resourceStack.pop();

			resultCollector.doneParsing(path);

			if (progressMonitor.isCanceled())
				throw new InterruptedException();
		}
		
		progressMonitor.done();
	}
	
	private void parseMatches(IPath path, Reader reader, IPath realPath, IProject project) throws InterruptedException {
		//Get the scanner info
		IScannerInfo scanInfo = new ScannerInfo();
		IScannerInfoProvider provider = CCorePlugin.getDefault().getScannerInfoProvider(project);
		if (provider != null){
			IScannerInfo buildScanInfo = provider.getScannerInformation(project);
			if( buildScanInfo != null )
				scanInfo = new ScannerInfo(buildScanInfo.getDefinedSymbols(), buildScanInfo.getIncludePaths());
		}
		
		ParserLanguage language = null;
		if( project != null ){
			language = CoreModel.getDefault().hasCCNature( project ) ? ParserLanguage.CPP : ParserLanguage.C;
		} else {
			//TODO no project, what language do we use?
			language = ParserLanguage.CPP;
		}
		
		IParser parser = null;
		try
		{
			IScanner scanner = ParserFactory.createScanner( reader, realPath.toOSString(), scanInfo, ParserMode.COMPLETE_PARSE, language, this, ParserUtil.getScannerLogService(), null );
			parser  = ParserFactory.createParser( scanner, this, ParserMode.STRUCTURAL_PARSE, language, ParserUtil.getParserLogService() );
		}
		catch( ParserFactoryError pfe )
		{
			
		}
		
		if (VERBOSE)
			verbose("*** New Search for path: " + path); //$NON-NLS-1$
		  
		try {
			parser.parse();
		}
		catch(OperationCanceledException ex) {
			throw new InterruptedException();
		}
		catch(Exception ex) {
			if (VERBOSE){
				ex.printStackTrace();
			}
		}
		catch(VirtualMachineError vmErr){
			if (VERBOSE){
				verbose("TypeMatchLocator VM Error: "); //$NON-NLS-1$
				vmErr.printStackTrace();
			}
		}
	}
	
	private int getElementType(IASTOffsetableNamedElement offsetable) {
		if (offsetable instanceof IASTClassSpecifier ||
			offsetable instanceof IASTElaboratedTypeSpecifier) {
				
			ASTClassKind kind = null;
			if (offsetable instanceof IASTClassSpecifier) {
				kind= ((IASTClassSpecifier)offsetable).getClassKind();
			} else {
				kind= ((IASTElaboratedTypeSpecifier)offsetable).getClassKind();
			}
			
			if (kind == ASTClassKind.CLASS) {
				return ICElement.C_CLASS;
			} else if (kind == ASTClassKind.STRUCT) {
				return ICElement.C_STRUCT;
			} else if (kind == ASTClassKind.UNION) {
				return ICElement.C_UNION;
			}
		} else if ( offsetable instanceof IASTNamespaceDefinition ){
			return ICElement.C_NAMESPACE;
		} else if ( offsetable instanceof IASTEnumerationSpecifier ){
			return ICElement.C_ENUMERATION;
		} else if ( offsetable instanceof IASTTypedefDeclaration ){
			return ICElement.C_TYPEDEF;
		}
		
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptFriendDeclaration(org.eclipse.cdt.core.parser.ast.IASTDeclaration)
	 */
	public void acceptFriendDeclaration(IASTDeclaration declaration) {
		// TODO Auto-generated method stub
		
	}
	
}
