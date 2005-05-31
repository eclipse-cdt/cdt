/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICLogConstants;
import org.eclipse.cdt.core.index.IIndexDelta;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.IParser;
import org.eclipse.cdt.core.parser.IProblem;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfoProvider;
import org.eclipse.cdt.core.parser.ParserFactory;
import org.eclipse.cdt.core.parser.ParserFactoryError;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ParserUtil;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.core.parser.ast.ASTClassKind;
import org.eclipse.cdt.core.parser.ast.ASTNotImplementedException;
import org.eclipse.cdt.core.parser.ast.IASTBaseSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTClassReference;
import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier;
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
import org.eclipse.cdt.core.parser.ast.IASTMacro;
import org.eclipse.cdt.core.parser.ast.IASTMethod;
import org.eclipse.cdt.core.parser.ast.IASTMethodReference;
import org.eclipse.cdt.core.parser.ast.IASTNamespaceDefinition;
import org.eclipse.cdt.core.parser.ast.IASTNamespaceReference;
import org.eclipse.cdt.core.parser.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTParameterReference;
import org.eclipse.cdt.core.parser.ast.IASTTypeSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTTypedefDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTTypedefReference;
import org.eclipse.cdt.core.parser.ast.IASTVariable;
import org.eclipse.cdt.core.parser.ast.IASTVariableReference;
import org.eclipse.cdt.internal.core.index.FunctionEntry;
import org.eclipse.cdt.internal.core.index.IIndex;
import org.eclipse.cdt.internal.core.index.NamedEntry;
import org.eclipse.cdt.internal.core.index.TypeEntry;
import org.eclipse.cdt.internal.core.index.cindexstorage.IndexedFileEntry;
import org.eclipse.cdt.internal.core.index.impl.IndexDelta;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * A SourceIndexer indexes source files using the parser. The following items are indexed:
 * Declarations:
 * - Classes
 * - Structs
 * - Unions
 * References:
 * - Classes
 * - Structs
 * - Unions
 */
public class SourceIndexerRunner extends AbstractIndexer {
	private SourceIndexer indexer;
	
	/**
	 * @param resource
	 * @param out
	 */
	public SourceIndexerRunner(IFile resource, SourceIndexer indexer) {
		this.indexer = indexer;
		this.resourceFile = resource;
	}
	
	protected void indexFile(IFile file) throws IOException {
		// Add the name of the file to the index
		IndexedFileEntry indFile =output.addIndexedFile(file.getFullPath().toString());
        
		// Create a new Parser 
		SourceIndexerRequestor requestor = new SourceIndexerRequestor(this, resourceFile);
		
		int problems = indexer.indexProblemsEnabled( resourceFile.getProject() );
		setProblemMarkersEnabled( problems );
		requestRemoveMarkers( resourceFile, null );
		
		//Get the scanner info
		IProject currentProject = resourceFile.getProject();
		IScannerInfo scanInfo = new ScannerInfo();
		IScannerInfoProvider provider = CCorePlugin.getDefault().getScannerInfoProvider(currentProject);
		if (provider != null){
		  IScannerInfo buildScanInfo = provider.getScannerInformation(resourceFile);
		  if (buildScanInfo != null){
			scanInfo = new ScannerInfo(buildScanInfo.getDefinedSymbols(), buildScanInfo.getIncludePaths());
		  }
		}
		
		//C or CPP?
		ParserLanguage language = CoreModel.hasCCNature(currentProject) ? ParserLanguage.CPP : ParserLanguage.C;
		
		IParser parser = null;

		InputStream contents = null;
		try {
			contents = resourceFile.getContents();
			CodeReader reader = new CodeReader(resourceFile.getLocation().toOSString(), resourceFile.getCharset(), contents);
			parser = ParserFactory.createParser( 
							ParserFactory.createScanner(reader, scanInfo, ParserMode.COMPLETE_PARSE, language, requestor, ParserUtil.getScannerLogService(), null ), 
							requestor, ParserMode.COMPLETE_PARSE, language, ParserUtil.getParserLogService() );
		} catch( ParserFactoryError pfe ){
		} catch (CoreException e) {
		} finally {
			if (contents != null) {
				contents.close();
			}
		}
		
		try{
		    long startTime = 0;
            
            if (AbstractIndexer.TIMING)
                startTime = System.currentTimeMillis();
            
			boolean retVal = parser.parse();
			
	        if (AbstractIndexer.TIMING){
	            long currentTime = System.currentTimeMillis() - startTime;
	            System.out.println("Source Indexer - Index Time for " + resourceFile.getName() + ": " + currentTime + " ms"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	            long tempTotaltime = indexer.getTotalIndexTime() + currentTime;
	            indexer.setTotalIndexTime(tempTotaltime);
	            System.out.println("Source Indexer - Total Index Time: " + tempTotaltime + " ms"); //$NON-NLS-1$ //$NON-NLS-2$
	            
	        }
	        
			if (AbstractIndexer.VERBOSE){
				if (!retVal)
					AbstractIndexer.verbose("PARSE FAILED " + resourceFile.getName().toString()); //$NON-NLS-1$
				else
					AbstractIndexer.verbose("PARSE SUCCEEDED " + resourceFile.getName().toString());			 //$NON-NLS-1$
			}	
		}
		catch ( VirtualMachineError vmErr){
			if (vmErr instanceof OutOfMemoryError){
				org.eclipse.cdt.internal.core.model.Util.log(null, "Out Of Memory error: " + vmErr.getMessage() + " on File: " + resourceFile.getName(), ICLogConstants.CDT); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		catch ( Exception ex ){
			if (ex instanceof IOException)
				throw (IOException) ex;
		}
		finally{
			//if the user disable problem reporting since we last checked, don't report the collected problems
			if( indexer.indexProblemsEnabled( resourceFile.getProject() ) != 0 )
				reportProblems();
			
			//Report events
			ArrayList filesTrav = requestor.getFilesTraversed();
			IndexDelta indexDelta = new IndexDelta(resourceFile.getProject(),filesTrav, IIndexDelta.INDEX_FINISHED_DELTA);
			indexer.notifyListeners(indexDelta);
			//Release all resources
			parser=null;
			currentProject = null;
			requestor = null;
			provider = null;
			scanInfo=null;
		}
	}

	/**
	 * @param fullPath
	 * @param path
	 */
	public boolean haveEncounteredHeader(IPath fullPath, Path path) {
		return indexer.haveEncounteredHeader(fullPath, path, true);
	}

	protected class AddMarkerProblem extends Problem {
        private IProblem problem;
        public AddMarkerProblem(IResource file, IResource orig, IProblem problem) {
            super(file, orig);
            this.problem = problem;
        }

		public void run() {
            try {
               //we only ever add index markers on the file, so DEPTH_ZERO is far enough
               IMarker[] markers = resource.findMarkers(ICModelMarker.INDEXER_MARKER, true,IResource.DEPTH_ZERO);
               
               boolean newProblem = true;
               
               if (markers.length > 0) {
                   IMarker tempMarker = null;
                   Integer tempInt = null; 
                   String tempMsgString = null;
                   
                   for (int i=0; i<markers.length; i++) {
                       tempMarker = markers[i];
                       tempInt = (Integer) tempMarker.getAttribute(IMarker.LINE_NUMBER);
                       tempMsgString = (String) tempMarker.getAttribute(IMarker.MESSAGE);
                       if (tempInt != null && tempInt.intValue() == problem.getSourceLineNumber() &&
                           tempMsgString.equalsIgnoreCase( INDEXER_MARKER_PREFIX + problem.getMessage())) 
                       {
                           newProblem = false;
                           break;
                       }
                   }
               }
               
               if (newProblem) {
                   IMarker marker = resource.createMarker(ICModelMarker.INDEXER_MARKER);
                   int start = problem.getSourceStart();
                   int end = problem.getSourceEnd();
                   if (end <= start)
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
    }

    public void addClassSpecifier(IASTClassSpecifier classSpecification, int fileNumber){

        if (classSpecification.getClassKind().equals(ASTClassKind.CLASS))
        {
            //Get base clauses
            Iterator baseClauses = classSpecification.getBaseClauses();
            while (baseClauses.hasNext()){
                IASTBaseSpecifier baseSpec = (IASTBaseSpecifier) baseClauses.next();
                try {
                    IASTTypeSpecifier typeSpec =  baseSpec.getParentClassSpecifier();
                    if (typeSpec instanceof IASTClassSpecifier){
                        IASTClassSpecifier baseClassSpec = (IASTClassSpecifier) typeSpec;
                        char[][] baseFullyQualifiedName = baseClassSpec.getFullyQualifiedNameCharArrays();
                        int offset = baseClassSpec.getNameOffset();
                        int offsetLength = baseClassSpec.getNameEndOffset() - offset;
                      
                        TypeEntry typeEntry = new TypeEntry(IIndex.TYPE_DERIVED ,IIndex.DECLARATION, baseFullyQualifiedName, 0 /*getModifiers()*/, fileNumber);
        				typeEntry.setNameOffset(offset, offsetLength, IIndex.OFFSET);
        				typeEntry.serialize(output);
                    }
                } catch (ASTNotImplementedException e) {}
            }
            
            //Get friends
            Iterator friends = classSpecification.getFriends();
            while (friends.hasNext()){
                Object decl = friends.next();
                if (decl instanceof IASTClassSpecifier){
                    IASTClassSpecifier friendClassSpec = (IASTClassSpecifier) decl;
                    char[][] baseFullyQualifiedName = friendClassSpec.getFullyQualifiedNameCharArrays();
                    int offset = friendClassSpec.getNameOffset();
                    int offsetLength = friendClassSpec.getNameEndOffset() - offset;
                    
                    TypeEntry typeEntry = new TypeEntry(IIndex.TYPE_FRIEND ,IIndex.DECLARATION, baseFullyQualifiedName, 0 /*getModifiers()*/, fileNumber);
    				typeEntry.setNameOffset(offset,offsetLength, IIndex.OFFSET);
    				typeEntry.serialize(output);
                }
                else if (decl instanceof IASTElaboratedTypeSpecifier){
                    IASTElaboratedTypeSpecifier friendClassSpec = (IASTElaboratedTypeSpecifier) decl;
                    char[][] baseFullyQualifiedName = friendClassSpec.getFullyQualifiedNameCharArrays();
                    int offset = friendClassSpec.getNameOffset();
                    int offsetLength = friendClassSpec.getNameEndOffset() - offset;
                   
                    TypeEntry typeEntry = new TypeEntry(IIndex.TYPE_FRIEND ,IIndex.DECLARATION, baseFullyQualifiedName, 0 /*getModifiers()*/, fileNumber);
    				typeEntry.setNameOffset(offset,offsetLength, IIndex.OFFSET);
    				typeEntry.serialize(output);
                }
                else if (decl instanceof IASTFunction){
                    
                }
                else if (decl instanceof IASTMethod){
                    //
                }
                
            }
            
            int offset = classSpecification.getNameOffset();
            int offsetLength = classSpecification.getNameEndOffset() - offset;
        
            TypeEntry typeEntry = new TypeEntry(IIndex.TYPE_CLASS,IIndex.DECLARATION, classSpecification.getFullyQualifiedNameCharArrays(), 0 /*getModifiers()*/, fileNumber);
			typeEntry.setNameOffset(offset, offsetLength,  IIndex.OFFSET);
			//typeEntry.setBaseTypes(getInherits());
			typeEntry.serialize(output);
        }       
        else if (classSpecification.getClassKind().equals(ASTClassKind.STRUCT))
        {
            //Get base clauses
            Iterator i = classSpecification.getBaseClauses();
            while (i.hasNext()){
                IASTBaseSpecifier baseSpec = (IASTBaseSpecifier) i.next();
                try {
                    IASTTypeSpecifier typeSpec =  baseSpec.getParentClassSpecifier();
                    if (typeSpec instanceof IASTClassSpecifier){
                        IASTClassSpecifier baseClassSpec = (IASTClassSpecifier) typeSpec;
                        char[][] baseFullyQualifiedName = baseClassSpec.getFullyQualifiedNameCharArrays();
                        int offset = baseClassSpec.getNameOffset();
                        int offsetLength = baseClassSpec.getNameEndOffset() - offset;
                  
                        TypeEntry typeEntry = new TypeEntry(IIndex.TYPE_DERIVED ,IIndex.DECLARATION, baseFullyQualifiedName, 0 /*getModifiers()*/, fileNumber);
        				typeEntry.setNameOffset(offset, offsetLength, IIndex.OFFSET);
        				typeEntry.serialize(output);
                    }
                } catch (ASTNotImplementedException e) {}
            }
            
//          Get friends
            Iterator friends = classSpecification.getFriends();
            while (friends.hasNext()){
                Object decl = friends.next();
                if (decl instanceof IASTClassSpecifier){
                    IASTClassSpecifier friendClassSpec = (IASTClassSpecifier) decl;
                    char[][] baseFullyQualifiedName = friendClassSpec.getFullyQualifiedNameCharArrays();
                    int offset = friendClassSpec.getNameOffset();
                    int offsetLength = friendClassSpec.getNameEndOffset() - offset;
                
                    TypeEntry typeEntry = new TypeEntry(IIndex.TYPE_FRIEND ,IIndex.DECLARATION, baseFullyQualifiedName, 0 /*getModifiers()*/, fileNumber);
    				typeEntry.setNameOffset(offset,offsetLength, IIndex.OFFSET);
    				typeEntry.serialize(output);
                }
                else if (decl instanceof IASTElaboratedTypeSpecifier){
                    IASTElaboratedTypeSpecifier friendClassSpec = (IASTElaboratedTypeSpecifier) decl;
                    char[][] baseFullyQualifiedName = friendClassSpec.getFullyQualifiedNameCharArrays();
                    int offset = friendClassSpec.getNameOffset();
                    int offsetLength = friendClassSpec.getNameEndOffset() - offset;
                   
                    TypeEntry typeEntry = new TypeEntry(IIndex.TYPE_FRIEND ,IIndex.DECLARATION, baseFullyQualifiedName, 0 /*getModifiers()*/, fileNumber);
    				typeEntry.setNameOffset(offset,offsetLength, IIndex.OFFSET);
    				typeEntry.serialize(output);
                }
                else if (decl instanceof IASTFunction){
                    
                }
                else if (decl instanceof IASTMethod){
                    //
                }
            }
            
            int offset = classSpecification.getNameOffset();
            int offsetLength = classSpecification.getNameEndOffset() - offset;
            
            TypeEntry typeEntry = new TypeEntry(IIndex.TYPE_STRUCT,IIndex.DECLARATION, classSpecification.getFullyQualifiedNameCharArrays(), 0 /*getModifiers()*/, fileNumber);
			typeEntry.setNameOffset(offset, offsetLength,  IIndex.OFFSET);
			//typeEntry.setBaseTypes(getInherits());
			typeEntry.serialize(output);
        }
        else if (classSpecification.getClassKind().equals(ASTClassKind.UNION))
        {   
            int offset = classSpecification.getNameOffset();
            int offsetLength = classSpecification.getNameEndOffset() - offset;
          
        	TypeEntry typeEntry = new TypeEntry(IIndex.TYPE_UNION,IIndex.DECLARATION, classSpecification.getFullyQualifiedNameCharArrays(), 0 /*getModifiers()*/, fileNumber);
			typeEntry.setNameOffset(offset,offsetLength, IIndex.OFFSET);
			//typeEntry.setBaseTypes(getInherits());
			typeEntry.serialize(output);
        }
    }
    
    public void addEnumerationSpecifier(IASTEnumerationSpecifier enumeration, int fileNumber) {
        
        int offset = enumeration.getNameOffset();
        int offsetLength = enumeration.getNameEndOffset() - offset;
      
        TypeEntry typeEntry = new TypeEntry(IIndex.TYPE_ENUM ,IIndex.DECLARATION, enumeration.getFullyQualifiedNameCharArrays(), 0 /*getModifiers()*/, fileNumber);
		typeEntry.setNameOffset(offset, offsetLength, IIndex.OFFSET);
		typeEntry.serialize(output);
	
        Iterator i = enumeration.getEnumerators();
        while (i.hasNext())
        {
            IASTEnumerator en = (IASTEnumerator) i.next();  
            char[][] enumeratorFullName =
                createEnumeratorFullyQualifiedName(en);
            
            offset = en.getNameOffset();
            offsetLength = en.getNameEndOffset() - offset;
         
            NamedEntry namedEntry = new NamedEntry(IIndex.ENUMTOR, IIndex.DECLARATION, enumeratorFullName, 0 /*getModifiers()*/, fileNumber);
   		 	namedEntry.setNameOffset(offset, offsetLength, IIndex.OFFSET);
   		 	namedEntry.serialize(output);
        }
    }

    protected char[][] createEnumeratorFullyQualifiedName(IASTEnumerator en) {
        char[] name = en.getNameCharArray();
        IASTEnumerationSpecifier parent = en.getOwnerEnumerationSpecifier();
        char[][] parentName = parent.getFullyQualifiedNameCharArrays();
        
        //See spec 7.2-10, the the scope of the enumerator is the same level as the enumeration
        char[][] enumeratorFullName = new char[parentName.length][];
        
        System.arraycopy( parentName, 0, enumeratorFullName, 0, parentName.length);
        enumeratorFullName[ parentName.length - 1 ] = name;
        return enumeratorFullName;
    }

    public void addEnumeratorReference(IASTEnumeratorReference reference, int fileNumber) {
		IASTEnumerator enumerator = (IASTEnumerator)reference.getReferencedElement();
        int offset = reference.getOffset();
        int offsetLength = enumerator.getNameEndOffset() - enumerator.getNameOffset();
  
        NamedEntry namedEntry = new NamedEntry(IIndex.ENUMTOR, IIndex.REFERENCE, createEnumeratorFullyQualifiedName(enumerator), 0 /*getModifiers()*/, fileNumber);
		namedEntry.setNameOffset(offset, offsetLength, IIndex.OFFSET);
		namedEntry.serialize(output);
    }
        
    public void addMacro(IASTMacro macro, int fileNumber) {
        char[][] macroName = new char[][] { macro.getNameCharArray() };
        int offset = macro.getNameOffset();
        int offsetLength = macro.getNameEndOffset() - offset;
     
		NamedEntry namedEntry = new NamedEntry(IIndex.MACRO, IIndex.DECLARATION, macroName, 0 /*getModifiers()*/, fileNumber);
		namedEntry.setNameOffset(offset, offsetLength, IIndex.OFFSET);
		namedEntry.serialize(output);
    }
        
    public void addEnumerationReference(IASTEnumerationReference reference, int fileNumber) {
		IASTEnumerationSpecifier enumeration = (IASTEnumerationSpecifier) reference.getReferencedElement();
		int offset = reference.getOffset();
        int offsetLength = enumeration.getNameEndOffset() - enumeration.getNameOffset();
      
        TypeEntry typeEntry = new TypeEntry(IIndex.TYPE_ENUM ,IIndex.REFERENCE, enumeration.getFullyQualifiedNameCharArrays(), 0 /*getModifiers()*/, fileNumber);
		typeEntry.setNameOffset(offset, offsetLength, IIndex.OFFSET);
		typeEntry.serialize(output);
    }
    public void addVariable(IASTVariable variable, int fileNumber) {
        int offset = variable.getNameOffset();
        int offsetLength = variable.getNameEndOffset() - offset;
    
		TypeEntry typeEntry = new TypeEntry(IIndex.TYPE_VAR ,IIndex.DECLARATION, variable.getFullyQualifiedNameCharArrays(), 0 /*getModifiers()*/, fileNumber);
		typeEntry.setNameOffset(offset, offsetLength, IIndex.OFFSET);
		typeEntry.serialize(output);
    }
    
    public void addVariableReference(IASTVariableReference reference, int fileNumber) {
		IASTVariable variable = (IASTVariable)reference.getReferencedElement();
        int offset = reference.getOffset();
        int offsetLength = variable.getNameEndOffset() - variable.getNameOffset();

    	TypeEntry typeEntry = new TypeEntry(IIndex.TYPE_VAR ,IIndex.REFERENCE, variable.getFullyQualifiedNameCharArrays(), 0 /*getModifiers()*/, fileNumber);
		typeEntry.setNameOffset(offset, offsetLength, IIndex.OFFSET);
		typeEntry.serialize(output);
    }   
    
    public void addParameterReference( IASTParameterReference reference, int fileNumber ){
		IASTParameterDeclaration parameter = (IASTParameterDeclaration) reference.getReferencedElement();
		int offset = reference.getOffset();
        int offsetLength = parameter.getNameEndOffset() - parameter.getNameOffset();
    
    	TypeEntry typeEntry = new TypeEntry(IIndex.TYPE_VAR ,IIndex.REFERENCE, new char[][]{parameter.getNameCharArray()}, 0 /*getModifiers()*/, fileNumber);
		typeEntry.setNameOffset(offset, offsetLength, IIndex.OFFSET);
		typeEntry.serialize(output);
    }
    
    public void addTypedefDeclaration(IASTTypedefDeclaration typedef, int fileNumber) {
        int offset = typedef.getNameOffset();
        int offsetLength = typedef.getNameEndOffset() - offset;
        
    	TypeEntry typeEntry = new TypeEntry(IIndex.TYPE_TYPEDEF, IIndex.DECLARATION, typedef.getFullyQualifiedNameCharArrays(), 0 /*getModifiers()*/, fileNumber);
		typeEntry.setNameOffset(offset, offsetLength, IIndex.OFFSET);
		typeEntry.serialize(output);
    }
    
    public void addFieldDeclaration(IASTField field, int fileNumber) {
        int offset = field.getNameOffset();
        int offsetLength = field.getNameEndOffset() - offset;
       
		NamedEntry namedEntry = new NamedEntry(IIndex.FIELD, IIndex.DECLARATION, field.getFullyQualifiedNameCharArrays(), 0 /*getModifiers()*/, fileNumber);
		namedEntry.setNameOffset(offset, offsetLength, IIndex.OFFSET);
		namedEntry.serialize(output);
    }
    
    public void addFieldReference(IASTFieldReference reference, int fileNumber) {
		IASTField field=(IASTField) reference.getReferencedElement();
        int offset = reference.getOffset();
        int offsetLength = field.getNameEndOffset() - field.getNameOffset();
      
        NamedEntry namedEntry = new NamedEntry(IIndex.FIELD, IIndex.REFERENCE, field.getFullyQualifiedNameCharArrays(), 0 /*getModifiers()*/, fileNumber);
		namedEntry.setNameOffset(offset, offsetLength, IIndex.OFFSET);
		namedEntry.serialize(output);
		
    }
    
    public void addMethodDeclaration(IASTMethod method, int fileNumber) {
        int offset = method.getNameOffset();
        int offsetLength = method.getNameEndOffset() - offset;
      
		FunctionEntry functionEntry = new FunctionEntry(IIndex.METHOD, IIndex.DECLARATION, method.getFullyQualifiedNameCharArrays(),0 /*getModifiers()*/, fileNumber);
		//funEntry.setSignature(getFunctionSignature());
		functionEntry.setNameOffset(offset, offsetLength, IIndex.OFFSET);
		functionEntry.serialize(output);
		
        Iterator i=method.getParameters();
        while (i.hasNext()){
            Object parm = i.next();
            if (parm instanceof IASTParameterDeclaration){
                IASTParameterDeclaration parmDecl = (IASTParameterDeclaration) parm;
                offset = parmDecl.getNameOffset();
                offsetLength = parmDecl.getNameEndOffset() - offset;
          
                TypeEntry typeEntry = new TypeEntry(IIndex.TYPE_VAR ,IIndex.DECLARATION, new char[][]{parmDecl.getNameCharArray()}, 0 /*getModifiers()*/, fileNumber);
				typeEntry.setNameOffset(offset, offsetLength, IIndex.OFFSET);
				typeEntry.serialize(output);
            }
        }
    }
    
    public void addMethodReference(IASTMethodReference reference, int fileNumber) {
		IASTMethod method = (IASTMethod) reference.getReferencedElement();
        int offset = reference.getOffset();
        int offsetLength = method.getNameEndOffset() - method.getNameOffset();
       
        FunctionEntry functionEntry = new FunctionEntry(IIndex.METHOD, IIndex.REFERENCE, method.getFullyQualifiedNameCharArrays(),0 /*getModifiers()*/, fileNumber);
		//funEntry.setSignature(getFunctionSignature());
		functionEntry.setNameOffset(offset, offsetLength, IIndex.OFFSET);
		functionEntry.serialize(output);
    }

    public void addElaboratedForwardDeclaration(IASTElaboratedTypeSpecifier elaboratedType, int fileNumber) {
        int offset = elaboratedType.getNameOffset();
        int offsetLength = elaboratedType.getNameEndOffset() - offset;
        
        if (elaboratedType.getClassKind().equals(ASTClassKind.CLASS))
        {
        	TypeEntry typeEntry = new TypeEntry(IIndex.TYPE_FWD_CLASS ,IIndex.DECLARATION, elaboratedType.getFullyQualifiedNameCharArrays(), 0 /*getModifiers()*/, fileNumber);
			typeEntry.setNameOffset(offset, offsetLength, IIndex.OFFSET);
			typeEntry.serialize(output);
        }       
        else if (elaboratedType.getClassKind().equals(ASTClassKind.STRUCT))
        {
            TypeEntry typeEntry = new TypeEntry(IIndex.TYPE_FWD_STRUCT ,IIndex.DECLARATION, elaboratedType.getFullyQualifiedNameCharArrays(), 0 /*getModifiers()*/, fileNumber);
			typeEntry.setNameOffset(offset, offsetLength, IIndex.OFFSET);
			typeEntry.serialize(output);
        }
        else if (elaboratedType.getClassKind().equals(ASTClassKind.UNION))
        {
            TypeEntry typeEntry = new TypeEntry(IIndex.TYPE_FWD_UNION,IIndex.DECLARATION, elaboratedType.getFullyQualifiedNameCharArrays(), 0 /*getModifiers()*/, fileNumber);
			typeEntry.setNameOffset(offset, offsetLength, IIndex.OFFSET);
			typeEntry.serialize(output);
        }
    }
    
    public void addConstructorDeclaration(){
        
    }
    public void addConstructorReference(){

    }
    
    public void addMemberDeclaration(){
    
    }
    public void addMemberReference(){
    
    }

    public void addFunctionDeclaration(IASTFunction function, int fileNumber){
        int offset = function.getNameOffset();
        int offsetLength = function.getNameEndOffset() - offset;
        
        FunctionEntry functionEntry = new FunctionEntry(IIndex.FUNCTION, IIndex.DECLARATION, function.getFullyQualifiedNameCharArrays(),0 /*getModifiers()*/, fileNumber);
		//funEntry.setSignature(getFunctionSignature());
		functionEntry.setNameOffset(offset, offsetLength, IIndex.OFFSET);
		functionEntry.serialize(output);
		
        Iterator i=function.getParameters();
        while (i.hasNext()){
            Object parm = i.next();
            if (parm instanceof IASTParameterDeclaration){
                IASTParameterDeclaration parmDecl = (IASTParameterDeclaration) parm;
                offset = parmDecl.getNameOffset();
                offsetLength = parmDecl.getNameEndOffset() - offset;
                
        		TypeEntry typeEntry = new TypeEntry(IIndex.TYPE_VAR ,IIndex.DECLARATION, new char[][]{parmDecl.getNameCharArray()}, 0 /*getModifiers()*/, fileNumber);
        		typeEntry.setNameOffset(offset, offsetLength, IIndex.OFFSET);
        		typeEntry.serialize(output);
            }
        }
    }
    
    public void addFunctionReference(IASTFunctionReference reference, int fileNumber){
		IASTFunction function=(IASTFunction) reference.getReferencedElement();
		int offset = reference.getOffset();
        int offsetLength = function.getNameEndOffset() - function.getNameOffset();

        FunctionEntry functionEntry = new FunctionEntry(IIndex.FUNCTION, IIndex.REFERENCE, function.getFullyQualifiedNameCharArrays(),0 /*getModifiers()*/, fileNumber);
		//funEntry.setSignature(getFunctionSignature());
		functionEntry.setNameOffset(offset, offsetLength, IIndex.OFFSET);
		functionEntry.serialize(output);
    }
    
    public void addNameReference(){
        
    }
    
    public void addNamespaceDefinition(IASTNamespaceDefinition namespace, int fileNumber){
        int offset = namespace.getNameOffset();
        int offsetLength = namespace.getNameEndOffset() - offset;
      
        NamedEntry namedEntry = new NamedEntry(IIndex.NAMESPACE, IIndex.DECLARATION, namespace.getFullyQualifiedNameCharArrays(), 0 /*getModifiers()*/, fileNumber);
		namedEntry.setNameOffset(offset, offsetLength, IIndex.OFFSET);
		namedEntry.serialize(output);
    }
    
    public void addNamespaceReference(IASTNamespaceReference reference, int fileNumber) {
		IASTNamespaceDefinition namespace = (IASTNamespaceDefinition)reference.getReferencedElement();
        int offset = reference.getOffset();
        int offsetLength = namespace.getNameEndOffset() -  namespace.getNameOffset();
             
        NamedEntry namedEntry = new NamedEntry(IIndex.NAMESPACE, IIndex.REFERENCE, namespace.getFullyQualifiedNameCharArrays(), 0 /*getModifiers()*/, fileNumber);
		namedEntry.setNameOffset(offset, offsetLength, IIndex.OFFSET);
		namedEntry.serialize(output);
    }
    
    public void addTypedefReference( IASTTypedefReference reference, int fileNumber ){
		IASTTypedefDeclaration typedef = (IASTTypedefDeclaration) reference.getReferencedElement();
        int offset = reference.getOffset();
        int offsetLength = typedef.getNameEndOffset() - typedef.getNameOffset();
        
        TypeEntry typeEntry = new TypeEntry(IIndex.TYPE_TYPEDEF, IIndex.REFERENCE, typedef.getFullyQualifiedNameCharArrays(), 0 /*getModifiers()*/, fileNumber);
		typeEntry.setNameOffset(offset,  offsetLength, IIndex.OFFSET);
		typeEntry.serialize(output);
    }

    public void addClassReference(IASTClassReference reference, int fileNumber){
        char[][] fullyQualifiedName = null;
        ASTClassKind classKind = null;
        int offset=0;
        int offsetLength=1;
        Object referenceObject = reference.getReferencedElement();
        if (referenceObject  instanceof IASTClassSpecifier){
          IASTClassSpecifier classRef = (IASTClassSpecifier) referenceObject;
          fullyQualifiedName = classRef.getFullyQualifiedNameCharArrays();
          classKind = classRef.getClassKind();
          offset=reference.getOffset();
		  offsetLength=classRef.getNameEndOffset()-classRef.getNameOffset();
        }
        else if (referenceObject instanceof IASTElaboratedTypeSpecifier){
          IASTElaboratedTypeSpecifier typeRef = (IASTElaboratedTypeSpecifier) reference;
          fullyQualifiedName = typeRef.getFullyQualifiedNameCharArrays();
          classKind = typeRef.getClassKind();
          offset=reference.getOffset();
          offsetLength=typeRef.getNameEndOffset()-typeRef.getNameOffset();
        }
    
        if (classKind.equals(ASTClassKind.CLASS))
        {  
            TypeEntry typeEntry = new TypeEntry(IIndex.TYPE_CLASS,IIndex.REFERENCE, fullyQualifiedName, 0 /*getModifiers()*/, fileNumber);
			typeEntry.setNameOffset(offset, offsetLength,  IIndex.OFFSET);
			//typeEntry.setBaseTypes(getInherits());
			typeEntry.serialize(output);
        }       
        else if (classKind.equals(ASTClassKind.STRUCT))
        {
            TypeEntry typeEntry = new TypeEntry(IIndex.TYPE_STRUCT,IIndex.REFERENCE, fullyQualifiedName, 0 /*getModifiers()*/, fileNumber);
			typeEntry.setNameOffset(offset, offsetLength,  IIndex.OFFSET);
			//typeEntry.setBaseTypes(getInherits());
			typeEntry.serialize(output);
        }
        else if (classKind.equals(ASTClassKind.UNION))
        { 
            TypeEntry typeEntry = new TypeEntry(IIndex.TYPE_UNION,IIndex.REFERENCE, fullyQualifiedName, 0 /*getModifiers()*/, fileNumber);
			typeEntry.setNameOffset(offset, offsetLength,  IIndex.OFFSET);
			//typeEntry.setBaseTypes(getInherits());
			typeEntry.serialize(output);
        }
    }
    
    public void addForwardClassReference(IASTClassReference reference, int fileNumber){
        char[][] fullyQualifiedName = null;
        ASTClassKind classKind = null;
        int offset=0;
        int offsetLength=1;
		Object referencedObject = reference.getReferencedElement();
        if (referencedObject instanceof IASTElaboratedTypeSpecifier){
          IASTElaboratedTypeSpecifier typeRef = (IASTElaboratedTypeSpecifier) referencedObject;
          fullyQualifiedName = typeRef.getFullyQualifiedNameCharArrays();
          classKind = typeRef.getClassKind();
		  offset=reference.getOffset();
	      offsetLength=typeRef.getNameEndOffset()-typeRef.getNameOffset();
        }
    
        if (classKind == null)
            return;
        
        if (classKind.equals(ASTClassKind.CLASS))
        {  
            TypeEntry typeEntry = new TypeEntry(IIndex.TYPE_FWD_CLASS ,IIndex.REFERENCE, fullyQualifiedName, 0 /*getModifiers()*/, fileNumber);
			typeEntry.setNameOffset(offset, offsetLength, IIndex.OFFSET);
			typeEntry.serialize(output);
        }       
        else if (classKind.equals(ASTClassKind.STRUCT))
        {
            TypeEntry typeEntry = new TypeEntry(IIndex.TYPE_FWD_STRUCT ,IIndex.REFERENCE, fullyQualifiedName, 0 /*getModifiers()*/, fileNumber);
 			typeEntry.setNameOffset(offset, offsetLength, IIndex.OFFSET);
 			typeEntry.serialize(output);
        }
        else if (classKind.equals(ASTClassKind.UNION))
        {
        	TypeEntry typeEntry = new TypeEntry(IIndex.TYPE_FWD_UNION ,IIndex.REFERENCE, fullyQualifiedName, 0 /*getModifiers()*/, fileNumber);
  			typeEntry.setNameOffset(offset, offsetLength, IIndex.OFFSET);
  			typeEntry.serialize(output);
        }
    }

    public void addInclude(IASTInclusion inclusion, IASTInclusion parent, int fileNumber){
        this.output.addIncludeRef(fileNumber, inclusion.getFullFileName());
        this.output.addRelatives(fileNumber, inclusion.getFullFileName(),(parent != null ) ? parent.getFullFileName() : null);
        
        //Add Dep Table entry
        char[][] incName = new char[1][];
        incName[0] = inclusion.getFullFileName().toCharArray();
        //TODO: Kludge! Get rid of BOGUS entry - need to restructure Dep Tree to use reference indexes
        int BOGUS_ENTRY = 1;
        this.output.addIncludeRef(fileNumber, incName,1,1, IIndex.OFFSET);
    }

	public void generateMarkerProblem(IFile tempFile, IFile originator, IProblem problem) {
        Problem tempProblem = new AddMarkerProblem(tempFile, originator, problem);
        if (getProblemsMap().containsKey(tempFile)) {
            List list = (List) getProblemsMap().get(tempFile);
            list.add(tempProblem);
        } else {
            List list = new ArrayList();
            list.add(new RemoveMarkerProblem(tempFile, originator));  //remove existing markers
            list.add(tempProblem);
			getProblemsMap().put(tempFile, list);
        }
	}
}
