/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
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


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.cdt.core.parser.CodeReader;
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
import org.eclipse.cdt.core.parser.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTParameterReference;
import org.eclipse.cdt.core.parser.ast.IASTTemplateDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTTemplateInstantiation;
import org.eclipse.cdt.core.parser.ast.IASTTemplateParameterReference;
import org.eclipse.cdt.core.parser.ast.IASTTemplateSpecialization;
import org.eclipse.cdt.core.parser.ast.IASTTypeSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTTypedefDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTTypedefReference;
import org.eclipse.cdt.core.parser.ast.IASTUsingDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTUsingDirective;
import org.eclipse.cdt.core.parser.ast.IASTVariable;
import org.eclipse.cdt.core.parser.ast.IASTVariableReference;
import org.eclipse.cdt.internal.core.Util;
import org.eclipse.cdt.utils.TimeOut;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

/**
 * @author bgheorgh
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class SourceIndexerRequestor implements ISourceElementRequestor, IIndexConstants {
	
	SourceIndexer indexer;
	IFile resourceFile;

	char[] packageName;
	char[][] enclosingTypeNames = new char[5][];
	int depth = 0;
	int methodDepth = 0;
	
	private IASTInclusion currentInclude = null;
	private LinkedList includeStack = new LinkedList();
	
	private int problemMarkersEnabled = 0;
	private Map problemsMap = null;
	
	private IProgressMonitor pm = new NullProgressMonitor();
	private  TimeOut timeoutThread = null;
	
	private static final String INDEXER_MARKER_ORIGINATOR =  ICModelMarker.INDEXER_MARKER + ".originator";  //$NON-NLS-1$
	private static final String INDEXER_MARKER_PREFIX = Util.bind("indexerMarker.prefix" ) + " "; //$NON-NLS-1$ //$NON-NLS-2$
	private static final String INDEXER_MARKER_PROCESSING = Util.bind( "indexerMarker.processing" ); //$NON-NLS-1$
	
	private ArrayList filesTraversed = null;
	
	public SourceIndexerRequestor(SourceIndexer indexer, IFile resourceFile, TimeOut timeOut) {
		super();
		this.indexer = indexer;
		this.resourceFile = resourceFile;
		this.timeoutThread =  timeOut;
		this.filesTraversed = new ArrayList(15);
		this.filesTraversed.add(resourceFile.getLocation().toOSString());
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptProblem(org.eclipse.cdt.core.parser.IProblem)
	 */
	public boolean acceptProblem(IProblem problem) {
		if( areProblemMarkersEnabled() && shouldRecordProblem( problem ) ){
			IASTInclusion include = peekInclude();
			IFile tempFile = resourceFile;
		  
			//If we are in an include file, get the include file
			if (include != null){
				IPath newPath = new Path(include.getFullFileName());
		 		tempFile = CCorePlugin.getWorkspace().getRoot().getFileForLocation(newPath);
			}
			
			if( tempFile != null ){
				Problem tempProblem = new AddMarkerProblem(tempFile, resourceFile, problem );
				if( problemsMap.containsKey( tempFile ) ){
					List list = (List) problemsMap.get( tempFile );
					list.add( tempProblem );
				} else {
					List list = new ArrayList();
					list.add( new RemoveMarkerProblem( tempFile, resourceFile ) );  //remove existing markers
					list.add( tempProblem );
					problemsMap.put( tempFile, list );
				}
			}
		}
		
		return IndexProblemHandler.ruleOnProblem( problem, ParserMode.COMPLETE_PARSE );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptMacro(org.eclipse.cdt.core.parser.ast.IASTMacro)
	 */
	public void acceptMacro(IASTMacro macro) {
		// TODO Auto-generated method stub
		indexer.addMacro(macro);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptVariable(org.eclipse.cdt.core.parser.ast.IASTVariable)
	 */
	public void acceptVariable(IASTVariable variable) {
		// TODO Auto-generated method stub
		//System.out.println("acceptVariable");
		indexer.addVariable(variable);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptFunctionDeclaration(org.eclipse.cdt.core.parser.ast.IASTFunction)
	 */
	public void acceptFunctionDeclaration(IASTFunction function) {
		// TODO Auto-generated method stub
		//System.out.println("acceptFunctionDeclaration");
		indexer.addFunctionDeclaration(function);
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
	public void acceptTypedefDeclaration(IASTTypedefDeclaration typedef) {
		// TODO Auto-generated method stub
		indexer.addTypedefDeclaration(typedef);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptEnumerationSpecifier(org.eclipse.cdt.core.parser.ast.IASTEnumerationSpecifier)
	 */
	public void acceptEnumerationSpecifier(IASTEnumerationSpecifier enumeration) {
		// TODO Auto-generated method stub
		//System.out.println("acceptEnumSpecifier");
		indexer.addEnumerationSpecifier(enumeration);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterFunctionBody(org.eclipse.cdt.core.parser.ast.IASTFunction)
	 */
	public void enterFunctionBody(IASTFunction function) {
		// TODO Auto-generated method stub
		indexer.addFunctionDeclaration(function);
		//indexer.addFunctionDefinition();
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
		if( areProblemMarkersEnabled() ){
			IPath newPath = new Path(inclusion.getFullFileName());
			IFile tempFile = CCorePlugin.getWorkspace().getRoot().getFileForLocation(newPath);
			if (tempFile !=null){
				requestRemoveMarkers(tempFile, resourceFile);
			} else{
			 //File is out of workspace
			}
		}
		
		IASTInclusion parent = peekInclude();
		indexer.addInclude(inclusion, parent);
		//Push on stack
		pushInclude(inclusion);
		//Add to traversed files
		this.filesTraversed.add(inclusion.getFullFileName());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterNamespaceDefinition(org.eclipse.cdt.core.parser.ast.IASTNamespaceDefinition)
	 */
	public void enterNamespaceDefinition(IASTNamespaceDefinition namespaceDefinition) {
		// TODO Auto-generated method stub
		//System.out.println("enterNamespaceDefinition");
		indexer.addNamespaceDefinition(namespaceDefinition);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterClassSpecifier(org.eclipse.cdt.core.parser.ast.IASTClassSpecifier)
	 */
	public void enterClassSpecifier(IASTClassSpecifier classSpecification) {
		// TODO Auto-generated method stub
		
		//System.out.println("New class spec: " + classSpecification.getName());
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
	public void enterTemplateInstantiation(IASTTemplateInstantiation instantiation) {
		// TODO Auto-generated method stub
		//System.out.println("enterTemplateExplicitInstantiation");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptMethodDeclaration(org.eclipse.cdt.core.parser.ast.IASTMethod)
	 */
	public void acceptMethodDeclaration(IASTMethod method) {
		// TODO Auto-generated method stub
		//System.out.println("acceptMethodDeclaration");
		indexer.addMethodDeclaration(method);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterMethodBody(org.eclipse.cdt.core.parser.ast.IASTMethod)
	 */
	public void enterMethodBody(IASTMethod method) {
		// TODO Auto-generated method stub
		//System.out.println("enterMethodBody " + method.getName());
		indexer.addMethodDeclaration(method);
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
	  // System.out.println("acceptField");
	   indexer.addFieldDeclaration(field);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptClassReference(org.eclipse.cdt.core.parser.ast.IASTClassSpecifier, int)
	 */
	public void acceptClassReference(IASTClassReference reference) {
		// TODO Auto-generated method stub
		//System.out.println("acceptClassReference");
		if (reference.getReferencedElement() instanceof IASTClassSpecifier)
			indexer.addClassReference((IASTClassSpecifier)reference.getReferencedElement());
		else if (reference.getReferencedElement() instanceof IASTElaboratedTypeSpecifier)
		{
		    indexer.addClassReference((IASTTypeSpecifier) reference.getReferencedElement());
		}
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
		indexer.addClassSpecifier(classSpecification);
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
		popInclude();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitCompilationUnit(org.eclipse.cdt.core.parser.ast.IASTCompilationUnit)
	 */
	public void exitCompilationUnit(IASTCompilationUnit compilationUnit) {
		// TODO Auto-generated method stub
		//System.out.println("exitCompilationUnit");

}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptAbstractTypeSpecDeclaration(org.eclipse.cdt.core.parser.ast.IASTAbstractTypeSpecifierDeclaration)
	 */
	public void acceptAbstractTypeSpecDeclaration(IASTAbstractTypeSpecifierDeclaration abstractDeclaration) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptTypedefReference(org.eclipse.cdt.core.parser.ast.IASTTypedefReference)
	 */
	public void acceptTypedefReference(IASTTypedefReference reference) {
		// TODO Auto-generated method stub
		if( reference.getReferencedElement() instanceof IASTTypedefDeclaration )
			indexer.addTypedefReference( (IASTTypedefDeclaration) reference.getReferencedElement() );
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptNamespaceReference(org.eclipse.cdt.core.parser.ast.IASTNamespaceReference)
	 */
	public void acceptNamespaceReference(IASTNamespaceReference reference) {
		// TODO Auto-generated method stub
		if (reference.getReferencedElement() instanceof IASTNamespaceDefinition)
		indexer.addNamespaceReference((IASTNamespaceDefinition)reference.getReferencedElement());	
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptEnumerationReference(org.eclipse.cdt.core.parser.ast.IASTEnumerationReference)
	 */
	public void acceptEnumerationReference(IASTEnumerationReference reference) {
		// TODO Auto-generated method stub
		if (reference.getReferencedElement() instanceof IASTEnumerationSpecifier)
		  indexer.addEnumerationReference((IASTEnumerationSpecifier) reference.getReferencedElement());
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptVariableReference(org.eclipse.cdt.core.parser.ast.IASTVariableReference)
	 */
	public void acceptVariableReference(IASTVariableReference reference) {
		// TODO Auto-generated method stub
		if (reference.getReferencedElement() instanceof IASTVariable)
			indexer.addVariableReference((IASTVariable)reference.getReferencedElement());
	
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptFunctionReference(org.eclipse.cdt.core.parser.ast.IASTFunctionReference)
	 */
	public void acceptFunctionReference(IASTFunctionReference reference) {
		if (reference.getReferencedElement() instanceof IASTFunction)
			indexer.addFunctionReference((IASTFunction) reference.getReferencedElement());
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptFieldReference(org.eclipse.cdt.core.parser.ast.IASTFieldReference)
	 */
	public void acceptFieldReference(IASTFieldReference reference) {
		if (reference.getReferencedElement() instanceof IASTField)
		  indexer.addFieldReference((IASTField) reference.getReferencedElement());
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptMethodReference(org.eclipse.cdt.core.parser.ast.IASTMethodReference)
	 */
	public void acceptMethodReference(IASTMethodReference reference) {
		if (reference.getReferencedElement() instanceof IASTMethod)
		 indexer.addMethodReference((IASTMethod) reference.getReferencedElement());
	}
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptElaboratedForewardDeclaration(org.eclipse.cdt.core.parser.ast.IASTElaboratedTypeSpecifier)
     */
    public void acceptElaboratedForewardDeclaration(IASTElaboratedTypeSpecifier elaboratedType){
        indexer.addElaboratedForwardDeclaration(elaboratedType);       
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
     	if( reference.getReferencedElement() instanceof IASTEnumerator )
     		indexer.addEnumeratorReference( (IASTEnumerator)reference.getReferencedElement() );
        
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptParameterReference(org.eclipse.cdt.internal.core.parser.ast.complete.ASTParameterReference)
     */
    public void acceptParameterReference(IASTParameterReference reference)
    {
        if( reference.getReferencedElement() instanceof IASTParameterDeclaration )
        	indexer.addParameterReference( (IASTParameterDeclaration) reference.getReferencedElement() );
        
    }
    
    public void acceptTemplateParameterReference( IASTTemplateParameterReference reference ){
    	if( reference.getReferencedElement() instanceof IASTTemplateParameterReference ){
    		//TODO
    	}
    }
    
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptFriendDeclaration(org.eclipse.cdt.core.parser.ast.IASTDeclaration)
	 */
	public void acceptFriendDeclaration(IASTDeclaration declaration) {
		// TODO Auto-generated method stub
		
	}
	
	private void pushInclude( IASTInclusion inclusion ){
		includeStack.addFirst( currentInclude );
		currentInclude = inclusion;
	}
	
	private IASTInclusion popInclude(){
		IASTInclusion oldInclude = currentInclude;
		currentInclude = (includeStack.size() > 0 ) ? (IASTInclusion) includeStack.removeFirst() : null;
		return oldInclude;
	}
	
	private IASTInclusion peekInclude(){
		return currentInclude;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#createReader(java.lang.String)
	 */
	public CodeReader createReader(String finalPath, Iterator workingCopies) {
		return ParserUtil.createReader(finalPath,workingCopies);
	}

	protected void processMarkers( List problemsList ){
		Iterator i = problemsList.iterator();
		while( i.hasNext() ){
			Problem prob = (Problem) i.next();
			if( prob.isAddProblem() ){
				addMarkers( prob.file, prob.originator, prob.getIProblem() );
			} else {
				removeMarkers( prob.file, prob.originator );
			}
		}
	}
	/**
	 * 
	 */
	public void removeMarkers(IFile resource, IFile originator) {
		if( originator == null ){
			//remove all markers
			try {
				resource.deleteMarkers( ICModelMarker.INDEXER_MARKER, true, IResource.DEPTH_INFINITE );
			} catch (CoreException e) {
			}
			return;
		}
		// else remove only those markers with matching originator
		IMarker[] markers;
		try {
			markers = resource.findMarkers(ICModelMarker.INDEXER_MARKER, true, IResource.DEPTH_INFINITE);
		} catch (CoreException e1) {
			return;
		}
		String origPath = originator.getFullPath().toString();
		IMarker mark = null;
		String orig = null;
		for( int i = 0; i < markers.length; i++ ){
			mark = markers[ i ];
			try {
				orig = (String) mark.getAttribute( INDEXER_MARKER_ORIGINATOR );
				if( orig != null && orig.equals( origPath ) ){
					mark.delete();
				}
			} catch (CoreException e) {
			}
		}
	}
	
	private void addMarkers(IFile tempFile, IFile originator, IProblem problem){
		 try {
		 	//we only ever add index markers on the file, so DEPTH_ZERO is far enough
	      	IMarker[] markers = tempFile.findMarkers(ICModelMarker.INDEXER_MARKER, true,IResource.DEPTH_ZERO);
	      	
	      	boolean newProblem = true;
	      	
	      	if (markers.length > 0){
	      		IMarker tempMarker = null;
	      		Integer tempInt = null;	
	      		String tempMsgString = null;
	      		
	      		for (int i=0; i<markers.length; i++){
	      			tempMarker = markers[i];
	      			tempInt = (Integer) tempMarker.getAttribute(IMarker.LINE_NUMBER);
	      			tempMsgString = (String) tempMarker.getAttribute(IMarker.MESSAGE);
	      			if (tempInt.intValue()==problem.getSourceLineNumber() &&
	      				tempMsgString.equals( INDEXER_MARKER_PREFIX + problem.getMessage()))
	      			{
	      				newProblem = false;
	      				break;
	      			}
	      		}
	      	}
	      	
	      	if (newProblem){
		        IMarker marker = tempFile.createMarker(ICModelMarker.INDEXER_MARKER);
		 		int start = problem.getSourceStart();
		 		int end = problem.getSourceEnd();
		 		if( end <= start )
		 			end = start + 1;
				marker.setAttribute(IMarker.LOCATION, problem.getSourceLineNumber());
				marker.setAttribute(IMarker.MESSAGE, INDEXER_MARKER_PREFIX + problem.getMessage());
				marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
				marker.setAttribute(IMarker.LINE_NUMBER, problem.getSourceLineNumber());
				marker.setAttribute(IMarker.CHAR_START, start);
				marker.setAttribute(IMarker.CHAR_END, end);	
				marker.setAttribute(INDEXER_MARKER_ORIGINATOR, originator.getFullPath().toString() );
	      	}
			
	      } catch (CoreException e) {
	         // You need to handle the cases where attribute value is rejected
	      }
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.ui.text.contentassist.ITimeoutThreadOwner#setTimeout(int)
	 */
	public void setTimeout(int timeout) {
		timeoutThread.setTimeout(timeout);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.ui.text.contentassist.ITimeoutThreadOwner#startTimer()
	 */
	public void startTimer() {
		createProgressMonitor();
		while (!timeoutThread.isReadyToRun()){
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		timeoutThread.startTimer();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.ui.text.contentassist.ITimeoutThreadOwner#stopTimer()
	 */
	public void stopTimer() {
		timeoutThread.stopTimer();
		pm.setCanceled(false);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#parserTimeout()
	 */
	public boolean parserTimeout() {
		if ((pm != null) && (pm.isCanceled()))
			return true;
		return false;
	}
	/*
	 * Creates a new progress monitor with each start timer
	 */
	private void createProgressMonitor() {
		pm.setCanceled(false);
		timeoutThread.setProgressMonitor(pm);
	}
	
	
	public boolean areProblemMarkersEnabled(){
		return problemMarkersEnabled != 0;
	}
	
	public void setProblemMarkersEnabled( int value ){
		if( value != 0 ){
			problemsMap = new HashMap();
		}
		this.problemMarkersEnabled = value;
	}
	
	public void reportProblems(){
		if( !areProblemMarkersEnabled() )
			return;
		
		Iterator i = problemsMap.keySet().iterator();
		
		while (i.hasNext()){
			IFile resource = (IFile) i.next();
			List problemList = (List) problemsMap.get( resource );

			//only bother scheduling a job if we have problems to add or remove
			if( problemList.size() <= 1 ){
				IMarker [] marker;
				try {
					marker = resource.findMarkers( ICModelMarker.INDEXER_MARKER, true, IResource.DEPTH_ZERO);
				} catch (CoreException e) {
					continue;
				}
				if( marker.length == 0 )
					continue;
			}
			String jobName = INDEXER_MARKER_PROCESSING;
			jobName += " ("; //$NON-NLS-1$
			jobName += resource.getFullPath();
			jobName += ')';
			
			ProcessMarkersJob job = new ProcessMarkersJob(  resource, problemList, jobName );
			
			IndexManager indexManager = CCorePlugin.getDefault().getCoreModel().getIndexManager();
			IProgressMonitor group = indexManager.getIndexJobProgressGroup();
			
			job.setRule( resource );
			if( group != null )
				job.setProgressGroup( group, 0 );
			job.setPriority( Job.DECORATE );
			job.schedule();
		}
	}
	
	public boolean shouldRecordProblem( IProblem problem ){
		if( problem.getSourceLineNumber() == -1  )
			return false;
		
		boolean preprocessor = ( problemMarkersEnabled & IndexManager.PREPROCESSOR_PROBLEMS_BIT ) != 0;
		boolean semantics = ( problemMarkersEnabled & IndexManager.SEMANTIC_PROBLEMS_BIT ) != 0;
		boolean syntax = ( problemMarkersEnabled & IndexManager.SYNTACTIC_PROBLEMS_BIT ) != 0;
		
		if( problem.checkCategory( IProblem.PREPROCESSOR_RELATED ) )
			return preprocessor && problem.getID() != IProblem.PREPROCESSOR_CIRCULAR_INCLUSION;
		else if( problem.checkCategory( IProblem.SEMANTICS_RELATED ) )
			return semantics;
		else if( problem.checkCategory( IProblem.SYNTAX_RELATED ) )
			return syntax;
		
		return false;
	}

	public void requestRemoveMarkers(IFile resource, IFile originator ){
		if( !areProblemMarkersEnabled() )
			return;
		
		Problem prob = new RemoveMarkerProblem( resource, originator );
		
		//a remove request will erase any previous requests for this resource
		if( problemsMap.containsKey( resource ) ){
			List list = (List) problemsMap.get( resource );
			list.clear();
			list.add( prob );
		} else {
			List list = new ArrayList();
			list.add( prob );
			problemsMap.put( resource, list );
		}
		
	}
	private class ProcessMarkersJob extends Job{
		protected final List problems;
		private final IFile resource;
		public ProcessMarkersJob( IFile resource, List problems, String name ){
			super( name );
			this.problems = problems;
			this.resource = resource;
		}

		protected IStatus run(IProgressMonitor monitor) {
			IWorkspaceRunnable job = new IWorkspaceRunnable( ){
				public void run(IProgressMonitor monitor){
					processMarkers( problems );
				}
			};
			try {
				CCorePlugin.getWorkspace().run(job, resource, 0, null);
			} catch (CoreException e) {
			}
			return Status.OK_STATUS;
		}
	}
	
	abstract private class Problem {
		public IFile file;
		public IFile originator;
		public Problem( IFile file, IFile orig ){
			this.file = file;
			this.originator = orig;
		}
		
		abstract public boolean isAddProblem();
		abstract public IProblem getIProblem();
	}
	private class AddMarkerProblem extends Problem {
		private IProblem problem;
		public AddMarkerProblem(IFile file, IFile orig, IProblem problem) {
			super( file, orig );
			this.problem = problem;
		}
		public boolean isAddProblem(){
			return true;
		}
		public IProblem getIProblem(){
			return problem;
		}
	}
	private class RemoveMarkerProblem extends Problem {
		public RemoveMarkerProblem(IFile file, IFile orig) {
			super(file, orig);
		}
		public boolean isAddProblem() {
			return false;
		}
		public IProblem getIProblem() {
			return null;
		}
	}
	/**
	 * @return Returns the filesTraversed.
	 */
	public ArrayList getFilesTraversed() {
		return filesTraversed;
	}
}
