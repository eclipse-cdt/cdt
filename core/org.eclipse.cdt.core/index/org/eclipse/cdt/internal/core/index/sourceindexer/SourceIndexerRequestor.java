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

package org.eclipse.cdt.internal.core.index.sourceindexer;

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
import org.eclipse.cdt.core.filetype.ICFileType;
import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.IParser;
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
import org.eclipse.cdt.internal.core.index.impl.IFileDocument;
import org.eclipse.cdt.internal.core.index.impl.IndexedFile;
import org.eclipse.cdt.internal.core.search.indexing.IIndexConstants;
import org.eclipse.cdt.internal.core.search.indexing.IndexManager;
import org.eclipse.cdt.internal.core.search.indexing.IndexProblemHandler;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
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
	
	SourceIndexerRunner indexer;
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
	
	private static final String INDEXER_MARKER_ORIGINATOR =  ICModelMarker.INDEXER_MARKER + ".originator";  //$NON-NLS-1$
	private static final String INDEXER_MARKER_PREFIX = Util.bind("indexerMarker.prefix" ) + " "; //$NON-NLS-1$ //$NON-NLS-2$
	private static final String INDEXER_MARKER_PROCESSING = Util.bind( "indexerMarker.processing" ); //$NON-NLS-1$
	
	private ArrayList filesTraversed = null;
	private IParser parser;
	
	public SourceIndexerRequestor(SourceIndexerRunner indexer, IFile resourceFile) {
		super();
		this.indexer = indexer;
		this.resourceFile = resourceFile;
		this.filesTraversed = new ArrayList(15);
		this.filesTraversed.add(resourceFile.getLocation().toOSString());
	}
	
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

	public void acceptMacro(IASTMacro macro) {
		//Check to see if this reference actually occurs in the file being indexed
		//or if it occurs in another file
		int indexFlag = calculateIndexFlags();
		
		indexer.addMacro(macro, indexFlag);
	}

	public void acceptVariable(IASTVariable variable) {
		//Check to see if this reference actually occurs in the file being indexed
		//or if it occurs in another file
		int indexFlag = calculateIndexFlags();
		
		indexer.addVariable(variable, indexFlag);
	}

	public void acceptFunctionDeclaration(IASTFunction function) {
		//Check to see if this reference actually occurs in the file being indexed
		//or if it occurs in another file
		int indexFlag = calculateIndexFlags();

		indexer.addFunctionDeclaration(function, indexFlag);
	}

	public void acceptUsingDirective(IASTUsingDirective usageDirective) {}
	public void acceptUsingDeclaration(IASTUsingDeclaration usageDeclaration) {}
	public void acceptASMDefinition(IASTASMDefinition asmDefinition) {}

	public void acceptTypedefDeclaration(IASTTypedefDeclaration typedef) {
		//Check to see if this reference actually occurs in the file being indexed
		//or if it occurs in another file
		int indexFlag = calculateIndexFlags();
		
		indexer.addTypedefDeclaration(typedef,indexFlag);
	}

	public void acceptEnumerationSpecifier(IASTEnumerationSpecifier enumeration) {
		//Check to see if this reference actually occurs in the file being indexed
		//or if it occurs in another file
		int indexFlag = calculateIndexFlags();
		
		indexer.addEnumerationSpecifier(enumeration,indexFlag);
	}

	public void enterFunctionBody(IASTFunction function) {
		//Check to see if this reference actually occurs in the file being indexed
		//or if it occurs in another file
		int indexFlag = calculateIndexFlags();
		
		indexer.addFunctionDeclaration(function,indexFlag);
		
	}

	public void exitFunctionBody(IASTFunction function) {}
	public void enterCompilationUnit(IASTCompilationUnit compilationUnit) {}
	
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
		indexer.addInclude(inclusion, parent,indexer.output.getIndexedFile(resourceFile.getFullPath().toString()).getFileNumber());
		//Push on stack
		pushInclude(inclusion);
		//Add to traversed files
		this.filesTraversed.add(inclusion.getFullFileName());
		
		IProject resourceProject = resourceFile.getProject();
		/* Check to see if this is a header file */
		ICFileType type = CCorePlugin.getDefault().getFileType(resourceProject,
				inclusion.getFullFileName());

		/* See if this file has been encountered before */
		if (type.isHeader())
			indexer.haveEncounteredHeader(resourceProject.getFullPath(),new Path(inclusion.getFullFileName()));
		
	}

	public void enterNamespaceDefinition(IASTNamespaceDefinition namespaceDefinition) {
		//Check to see if this reference actually occurs in the file being indexed
		//or if it occurs in another file
		int indexFlag = calculateIndexFlags();
		
		indexer.addNamespaceDefinition(namespaceDefinition, indexFlag);
	}

	public void enterClassSpecifier(IASTClassSpecifier classSpecification) {}
	public void enterLinkageSpecification(IASTLinkageSpecification linkageSpec) {}
	public void enterTemplateDeclaration(IASTTemplateDeclaration declaration) {}
	public void enterTemplateSpecialization(IASTTemplateSpecialization specialization) {}
	public void enterTemplateInstantiation(IASTTemplateInstantiation instantiation) {}

	public void acceptMethodDeclaration(IASTMethod method) {
		//Check to see if this reference actually occurs in the file being indexed
		//or if it occurs in another file
		int indexFlag = calculateIndexFlags();
		
		indexer.addMethodDeclaration(method, indexFlag);
	}

	public void enterMethodBody(IASTMethod method) {
		//Check to see if this reference actually occurs in the file being indexed
		//or if it occurs in another file
		int indexFlag = calculateIndexFlags();
		
		indexer.addMethodDeclaration(method, indexFlag);
	}

	public void exitMethodBody(IASTMethod method) {}
	
	public void acceptField(IASTField field) {
		//Check to see if this reference actually occurs in the file being indexed
		//or if it occurs in another file
		int indexFlag = calculateIndexFlags();
		
	    indexer.addFieldDeclaration(field, indexFlag);
	}

	public void acceptClassReference(IASTClassReference reference) {
		//Check to see if this reference actually occurs in the file being indexed
		//or if it occurs in another file
		int indexFlag = calculateIndexFlags();
		
		if (reference.getReferencedElement() instanceof IASTClassSpecifier)
			indexer.addClassReference((IASTClassSpecifier)reference.getReferencedElement(), indexFlag);
		else if (reference.getReferencedElement() instanceof IASTElaboratedTypeSpecifier)
		{
		    indexer.addForwardClassReference((IASTTypeSpecifier) reference.getReferencedElement(), indexFlag);
		} 
	}

	/**
	 * @return
	 */
	private int calculateIndexFlags() {
		int fileNum= 0;
		
		//Initialize the file number to be the file number for the file that triggerd
		//the indexing. Note that we should always be able to get a number for this as
		//the first step in the Source Indexer is to add the file being indexed to the index
		//which actually creates an entry for the file in the index.
		
		IndexedFile mainIndexFile = indexer.output.getIndexedFile(resourceFile.getFullPath().toString());
		if (mainIndexFile != null)
			fileNum = mainIndexFile.getFileNumber();
		
		IASTInclusion include = peekInclude();
		if (include != null){
			//We are not in the file that has triggered the index. Thus, we need to find the
			//file number for the current file (if it has one). If the current file does not
			//have a file number, we need to add it to the index.
			IFile tempFile = CCorePlugin.getWorkspace().getRoot().getFileForLocation(new Path(include.getFullFileName()));   
			String filePath = ""; //$NON-NLS-1$
			if (tempFile != null){
				//File is local to workspace
				filePath = tempFile.getFullPath().toString();
			}
			else{
				//File is external to workspace
				filePath = include.getFullFileName();
			}
			
			IndexedFile indFile = indexer.output.getIndexedFile(filePath);
			if (indFile != null){
				fileNum = indFile.getFileNumber();
			}
			else {
				//Need to add file to index
				if (tempFile != null){
				indFile = indexer.output.addSecondaryIndexedFile(new IFileDocument(tempFile));
				if (indFile != null)
					fileNum = indFile.getFileNumber();
				}
				else {
					indFile = indexer.output.addSecondaryExternalIndexedFile(include.getFullFileName());
					if (indFile != null)
						fileNum = indFile.getFileNumber();
				}
			}
			
		}
		
		return fileNum;
	}
	
	public void exitTemplateDeclaration(IASTTemplateDeclaration declaration) {}	
	public void exitTemplateSpecialization(IASTTemplateSpecialization specialization) {}
	public void exitTemplateExplicitInstantiation(IASTTemplateInstantiation instantiation) {}
	public void exitLinkageSpecification(IASTLinkageSpecification linkageSpec) {}

	public void exitClassSpecifier(IASTClassSpecifier classSpecification) {
		//Check to see if this reference actually occurs in the file being indexed
		//or if it occurs in another file
		int indexFlag = calculateIndexFlags();
	
		indexer.addClassSpecifier(classSpecification, indexFlag);
	}

	public void exitNamespaceDefinition(IASTNamespaceDefinition namespaceDefinition) {}

	public void exitInclusion(IASTInclusion inclusion) {
		// TODO Auto-generated method stub
		popInclude();
	}

	public void exitCompilationUnit(IASTCompilationUnit compilationUnit) {}
	
	public void acceptAbstractTypeSpecDeclaration(IASTAbstractTypeSpecifierDeclaration abstractDeclaration) {}

	public void acceptTypedefReference(IASTTypedefReference reference) {
		//Check to see if this reference actually occurs in the file being indexed
		//or if it occurs in another file
		int indexFlag = calculateIndexFlags();
		
		if( reference.getReferencedElement() instanceof IASTTypedefDeclaration )
			indexer.addTypedefReference( (IASTTypedefDeclaration) reference.getReferencedElement(),indexFlag);
	}
	
	public void acceptNamespaceReference(IASTNamespaceReference reference) {
		//Check to see if this reference actually occurs in the file being indexed
		//or if it occurs in another file
		int indexFlag = calculateIndexFlags();
		
		if (reference.getReferencedElement() instanceof IASTNamespaceDefinition)
		indexer.addNamespaceReference((IASTNamespaceDefinition)reference.getReferencedElement(),indexFlag);	
	}

	public void acceptEnumerationReference(IASTEnumerationReference reference) {
		//Check to see if this reference actually occurs in the file being indexed
		//or if it occurs in another file
		int indexFlag = calculateIndexFlags();
		
		if (reference.getReferencedElement() instanceof IASTEnumerationSpecifier)
		  indexer.addEnumerationReference((IASTEnumerationSpecifier) reference.getReferencedElement(),indexFlag);
	}
	
	public void acceptVariableReference(IASTVariableReference reference) {
		//Check to see if this reference actually occurs in the file being indexed
		//or if it occurs in another file
		int indexFlag = calculateIndexFlags();
		
		if (reference.getReferencedElement() instanceof IASTVariable)
			indexer.addVariableReference((IASTVariable)reference.getReferencedElement(),indexFlag);
	}
	
	public void acceptFunctionReference(IASTFunctionReference reference) {
		//Check to see if this reference actually occurs in the file being indexed
		//or if it occurs in another file
		int indexFlag = calculateIndexFlags();
		
		if (reference.getReferencedElement() instanceof IASTFunction)
			indexer.addFunctionReference((IASTFunction) reference.getReferencedElement(), indexFlag);
	}
	
	public void acceptFieldReference(IASTFieldReference reference) {
		//Check to see if this reference actually occurs in the file being indexed
		//or if it occurs in another file
		int indexFlag = calculateIndexFlags();
		
		if (reference.getReferencedElement() instanceof IASTField)
		  indexer.addFieldReference((IASTField) reference.getReferencedElement(),indexFlag);
	}
	
	public void acceptMethodReference(IASTMethodReference reference) {
		//Check to see if this reference actually occurs in the file being indexed
		//or if it occurs in another file
		int indexFlag = calculateIndexFlags();
		
		if (reference.getReferencedElement() instanceof IASTMethod)
		 indexer.addMethodReference((IASTMethod) reference.getReferencedElement(),indexFlag);
	}
    
    public void acceptElaboratedForewardDeclaration(IASTElaboratedTypeSpecifier elaboratedType){
    	//Check to see if this reference actually occurs in the file being indexed
		//or if it occurs in another file
		int indexFlag = calculateIndexFlags();
		
    	indexer.addElaboratedForwardDeclaration(elaboratedType, indexFlag);       
    }
	
    public void enterCodeBlock(IASTCodeScope scope) {}
	public void exitCodeBlock(IASTCodeScope scope) {}
    public void acceptEnumeratorReference(IASTEnumeratorReference reference){
    	//Check to see if this reference actually occurs in the file being indexed
		//or if it occurs in another file
		int indexFlag = calculateIndexFlags();
		
     	if( reference.getReferencedElement() instanceof IASTEnumerator )
     		indexer.addEnumeratorReference( (IASTEnumerator)reference.getReferencedElement(), indexFlag);
        
    }
    
    public void acceptParameterReference(IASTParameterReference reference){
    	//Check to see if this reference actually occurs in the file being indexed
		//or if it occurs in another file
		int indexFlag = calculateIndexFlags();
		
        if( reference.getReferencedElement() instanceof IASTParameterDeclaration )
        	indexer.addParameterReference( (IASTParameterDeclaration) reference.getReferencedElement(), indexFlag);
        
    }
    
    public void acceptTemplateParameterReference( IASTTemplateParameterReference reference ){}
    public void acceptFriendDeclaration(IASTDeclaration declaration) {}
	
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
	      			if (tempInt != null && tempInt.intValue()==problem.getSourceLineNumber() &&
	      				tempMsgString.equalsIgnoreCase( INDEXER_MARKER_PREFIX + problem.getMessage())) 
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
		
		boolean preprocessor = ( problemMarkersEnabled & SourceIndexer.PREPROCESSOR_PROBLEMS_BIT ) != 0;
		boolean semantics = ( problemMarkersEnabled & SourceIndexer.SEMANTIC_PROBLEMS_BIT ) != 0;
		boolean syntax = ( problemMarkersEnabled & SourceIndexer.SYNTACTIC_PROBLEMS_BIT ) != 0;
		
		if( problem.checkCategory( IProblem.PREPROCESSOR_RELATED ) || problem.checkCategory( IProblem.SCANNER_RELATED ) )
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
