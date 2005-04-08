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
import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTEnumerator;
import org.eclipse.cdt.core.parser.ast.IASTField;
import org.eclipse.cdt.core.parser.ast.IASTFunction;
import org.eclipse.cdt.core.parser.ast.IASTInclusion;
import org.eclipse.cdt.core.parser.ast.IASTMacro;
import org.eclipse.cdt.core.parser.ast.IASTMethod;
import org.eclipse.cdt.core.parser.ast.IASTNamespaceDefinition;
import org.eclipse.cdt.core.parser.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTTypeSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTTypedefDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTVariable;
import org.eclipse.cdt.core.search.ICSearchConstants;
import org.eclipse.cdt.internal.core.index.cindexstorage.ICIndexStorageConstants;
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
	 
    final static int CLASS = 1;
    final static int STRUCT = 2;
    final static int UNION = 3;
    final static int ENUM = 4;
    final static int VAR = 5;
    final static int TYPEDEF = 6;
    final static int DERIVED = 7;
    final static int FRIEND = 8;
    final static int FWD_CLASS = 9;
    final static int FWD_STRUCT = 10;
    final static int FWD_UNION = 11;
    
	IFile resourceFile;
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

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.search.indexing.AbstractIndexer#getResourceFile()
	 */
	public IFile getResourceFile() {
		return resourceFile;
	}

	/**
	 * @param fullPath
	 * @param path
	 */
	public boolean haveEncounteredHeader(IPath fullPath, Path path) {
		return indexer.haveEncounteredHeader(fullPath, path);
	}

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.index.sourceindexer.AbstractIndexer#addMarkers(org.eclipse.core.resources.IFile, org.eclipse.core.resources.IFile, java.lang.Object, java.lang.Object)
     */
    protected void addMarkers(IFile tempFile, IFile originator, Object problem, Object location) {
        if (problem instanceof IProblem) {
            IProblem iProblem = (IProblem) problem;
            
            try {
               //we only ever add index markers on the file, so DEPTH_ZERO is far enough
               IMarker[] markers = tempFile.findMarkers(ICModelMarker.INDEXER_MARKER, true,IResource.DEPTH_ZERO);
               
               boolean newProblem = true;
               
               if (markers.length > 0) {
                   IMarker tempMarker = null;
                   Integer tempInt = null; 
                   String tempMsgString = null;
                   
                   for (int i=0; i<markers.length; i++) {
                       tempMarker = markers[i];
                       tempInt = (Integer) tempMarker.getAttribute(IMarker.LINE_NUMBER);
                       tempMsgString = (String) tempMarker.getAttribute(IMarker.MESSAGE);
                       if (tempInt != null && tempInt.intValue()==iProblem.getSourceLineNumber() &&
                           tempMsgString.equalsIgnoreCase( INDEXER_MARKER_PREFIX + iProblem.getMessage())) 
                       {
                           newProblem = false;
                           break;
                       }
                   }
               }
               
               if (newProblem) {
                   IMarker marker = tempFile.createMarker(ICModelMarker.INDEXER_MARKER);
                   int start = iProblem.getSourceStart();
                   int end = iProblem.getSourceEnd();
                   if (end <= start)
                       end = start + 1;
                   marker.setAttribute(IMarker.LOCATION, iProblem.getSourceLineNumber());
                   marker.setAttribute(IMarker.MESSAGE, INDEXER_MARKER_PREFIX + iProblem.getMessage());
                   marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
                   marker.setAttribute(IMarker.LINE_NUMBER, iProblem.getSourceLineNumber());
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
                        output.addRef(fileNumber, encodeTypeEntry(baseFullyQualifiedName,DERIVED,ICSearchConstants.DECLARATIONS), offset,offsetLength, ICIndexStorageConstants.OFFSET);
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
                    output.addRef(fileNumber, encodeTypeEntry(baseFullyQualifiedName,FRIEND,ICSearchConstants.DECLARATIONS),offset, offsetLength, ICIndexStorageConstants.OFFSET);
                }
                else if (decl instanceof IASTElaboratedTypeSpecifier){
                    IASTElaboratedTypeSpecifier friendClassSpec = (IASTElaboratedTypeSpecifier) decl;
                    char[][] baseFullyQualifiedName = friendClassSpec.getFullyQualifiedNameCharArrays();
                    int offset = friendClassSpec.getNameOffset();
                    int offsetLength = friendClassSpec.getNameEndOffset() - offset;
                    output.addRef(fileNumber, encodeTypeEntry(baseFullyQualifiedName,FRIEND,ICSearchConstants.DECLARATIONS), offset, offsetLength, ICIndexStorageConstants.OFFSET);
                }
                else if (decl instanceof IASTFunction){
                    
                }
                else if (decl instanceof IASTMethod){
                    //
                }
                
            }
            
            int offset = classSpecification.getNameOffset();
            int offsetLength = classSpecification.getNameEndOffset() - offset;
            output.addRef(fileNumber, encodeTypeEntry(classSpecification.getFullyQualifiedNameCharArrays(),CLASS, ICSearchConstants.DECLARATIONS),offset, offsetLength, ICIndexStorageConstants.OFFSET);
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
                        output.addRef(fileNumber, encodeTypeEntry(baseFullyQualifiedName,DERIVED,ICSearchConstants.DECLARATIONS),offset, offsetLength, ICIndexStorageConstants.OFFSET);
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
                    output.addRef(fileNumber, encodeTypeEntry(baseFullyQualifiedName,FRIEND,ICSearchConstants.DECLARATIONS),offset,offsetLength, ICIndexStorageConstants.OFFSET);
                }
                else if (decl instanceof IASTElaboratedTypeSpecifier){
                    IASTElaboratedTypeSpecifier friendClassSpec = (IASTElaboratedTypeSpecifier) decl;
                    char[][] baseFullyQualifiedName = friendClassSpec.getFullyQualifiedNameCharArrays();
                    int offset = friendClassSpec.getNameOffset();
                    int offsetLength = friendClassSpec.getNameEndOffset() - offset;
                    output.addRef(fileNumber, encodeTypeEntry(baseFullyQualifiedName,FRIEND,ICSearchConstants.DECLARATIONS),offset,offsetLength, ICIndexStorageConstants.OFFSET);
                }
                else if (decl instanceof IASTFunction){
                    
                }
                else if (decl instanceof IASTMethod){
                    //
                }
            }
            
            int offset = classSpecification.getNameOffset();
            int offsetLength = classSpecification.getNameEndOffset() - offset;
            output.addRef(fileNumber, encodeTypeEntry(classSpecification.getFullyQualifiedNameCharArrays(),STRUCT, ICSearchConstants.DECLARATIONS),offset,offsetLength, ICIndexStorageConstants.OFFSET);
        }
        else if (classSpecification.getClassKind().equals(ASTClassKind.UNION))
        {   
            int offset = classSpecification.getNameOffset();
            int offsetLength = classSpecification.getNameEndOffset() - offset;
            output.addRef(fileNumber, encodeTypeEntry(classSpecification.getFullyQualifiedNameCharArrays(),UNION, ICSearchConstants.DECLARATIONS),offset,offsetLength, ICIndexStorageConstants.OFFSET);            
        }
    }
    
    public void addEnumerationSpecifier(IASTEnumerationSpecifier enumeration, int fileNumber) {
        
        int offset = enumeration.getNameOffset();
        int offsetLength = enumeration.getNameEndOffset() - offset;
        output.addRef(fileNumber, encodeTypeEntry(enumeration.getFullyQualifiedNameCharArrays(), ENUM, ICSearchConstants.DECLARATIONS),offset,offsetLength, ICIndexStorageConstants.OFFSET);
        
        Iterator i = enumeration.getEnumerators();
        while (i.hasNext())
        {
            IASTEnumerator en = (IASTEnumerator) i.next();  
            char[][] enumeratorFullName =
                createEnumeratorFullyQualifiedName(en);
            
            offset = en.getNameOffset();
            offsetLength = en.getNameEndOffset() - offset;
            output.addRef(fileNumber, encodeEntry( enumeratorFullName, ENUMTOR_DECL, ENUMTOR_DECL_LENGTH ),offset,offsetLength, ICIndexStorageConstants.OFFSET);

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

    public void addEnumeratorReference(IASTEnumerator enumerator, int fileNumber) {
        
        int offset = enumerator.getNameOffset();
        int offsetLength = enumerator.getNameEndOffset() - offset;
        output.addRef(fileNumber, encodeEntry(createEnumeratorFullyQualifiedName(enumerator),ENUMTOR_REF,ENUMTOR_REF_LENGTH),offset,offsetLength, ICIndexStorageConstants.OFFSET); 
    }
        
    public void addMacro(IASTMacro macro, int fileNumber) {
        char[][] macroName = new char[][] { macro.getNameCharArray() };
        int offset = macro.getNameOffset();
        int offsetLength = macro.getNameEndOffset() - offset;
        output.addRef(fileNumber, encodeEntry(macroName,MACRO_DECL,MACRO_DECL_LENGTH), offset,offsetLength, ICIndexStorageConstants.OFFSET);
    }
        
    public void addEnumerationReference(IASTEnumerationSpecifier enumeration, int fileNumber) {
        int offset = enumeration.getNameOffset();
        int offsetLength = enumeration.getNameEndOffset() - offset;
        output.addRef(fileNumber, encodeTypeEntry(enumeration.getFullyQualifiedNameCharArrays(), ENUM, ICSearchConstants.REFERENCES), offset,offsetLength, ICIndexStorageConstants.OFFSET);
    }
    public void addVariable(IASTVariable variable, int fileNumber) {
        int offset = variable.getNameOffset();
        int offsetLength = variable.getNameEndOffset() - offset;
        output.addRef(fileNumber, encodeTypeEntry(variable.getFullyQualifiedNameCharArrays(), VAR, ICSearchConstants.DECLARATIONS), offset,offsetLength, ICIndexStorageConstants.OFFSET);
    }
    
    public void addVariableReference(IASTVariable variable, int fileNumber) {
        int offset = variable.getNameOffset();
        int offsetLength = variable.getNameEndOffset() - offset;
        output.addRef(fileNumber, encodeTypeEntry(variable.getFullyQualifiedNameCharArrays(), VAR, ICSearchConstants.REFERENCES),offset, offsetLength, ICIndexStorageConstants.OFFSET);
    }   
    
    public void addParameterReference( IASTParameterDeclaration parameter, int fileNumber ){
        int offset = parameter.getNameOffset();
        int offsetLength = parameter.getNameEndOffset() - offset;
        output.addRef(fileNumber,encodeTypeEntry( new char[][] { parameter.getNameCharArray() }, VAR, ICSearchConstants.REFERENCES), offset, offsetLength, ICIndexStorageConstants.OFFSET);
    }
    
    public void addTypedefDeclaration(IASTTypedefDeclaration typedef, int fileNumber) {
        int offset = typedef.getNameOffset();
        int offsetLength = typedef.getNameEndOffset() - offset;
        output.addRef(fileNumber,encodeEntry(typedef.getFullyQualifiedNameCharArrays(), TYPEDEF_DECL, TYPEDEF_DECL_LENGTH),offset,offsetLength, ICIndexStorageConstants.OFFSET);
    }
    
    public void addFieldDeclaration(IASTField field, int fileNumber) {
        int offset = field.getNameOffset();
        int offsetLength = field.getNameEndOffset() - offset;
        output.addRef(fileNumber, encodeEntry(field.getFullyQualifiedNameCharArrays(),FIELD_DECL,FIELD_DECL_LENGTH),offset,offsetLength, ICIndexStorageConstants.OFFSET);
    }
    
    public void addFieldReference(IASTField field, int fileNumber) {
        int offset = field.getNameOffset();
        int offsetLength = field.getNameEndOffset() - offset;
        output.addRef(fileNumber, encodeEntry(field.getFullyQualifiedNameCharArrays(),FIELD_REF,FIELD_REF_LENGTH),offset,offsetLength, ICIndexStorageConstants.OFFSET);
    }
    
    public void addMethodDeclaration(IASTMethod method, int fileNumber) {
        int offset = method.getNameOffset();
        int offsetLength = method.getNameEndOffset() - offset;
        output.addRef(fileNumber, encodeEntry(method.getFullyQualifiedNameCharArrays(),METHOD_DECL,METHOD_DECL_LENGTH),offset,offsetLength, ICIndexStorageConstants.OFFSET);
    
        Iterator i=method.getParameters();
        while (i.hasNext()){
            Object parm = i.next();
            if (parm instanceof IASTParameterDeclaration){
                IASTParameterDeclaration parmDecl = (IASTParameterDeclaration) parm;
                offset = parmDecl.getNameOffset();
                offsetLength = parmDecl.getNameEndOffset() - offset;
                output.addRef(fileNumber, encodeTypeEntry(new char[][]{parmDecl.getNameCharArray()}, VAR, ICSearchConstants.DECLARATIONS),offset,offsetLength, ICIndexStorageConstants.OFFSET);
            }
        }
    }
    
    public void addMethodReference(IASTMethod method, int fileNumber) {
        int offset = method.getNameOffset();
        int offsetLength = method.getNameEndOffset() - offset;
        output.addRef(fileNumber, encodeEntry(method.getFullyQualifiedNameCharArrays(),METHOD_REF,METHOD_REF_LENGTH),offset,offsetLength, ICIndexStorageConstants.OFFSET);
    }

    public void addElaboratedForwardDeclaration(IASTElaboratedTypeSpecifier elaboratedType, int fileNumber) {
        int offset = elaboratedType.getNameOffset();
        int offsetLength = elaboratedType.getNameEndOffset() - offset;
        
        if (elaboratedType.getClassKind().equals(ASTClassKind.CLASS))
        {
            output.addRef(fileNumber,encodeTypeEntry(elaboratedType.getFullyQualifiedNameCharArrays(),FWD_CLASS, ICSearchConstants.DECLARATIONS),offset,offsetLength, ICIndexStorageConstants.OFFSET);
        }       
        else if (elaboratedType.getClassKind().equals(ASTClassKind.STRUCT))
        {
            output.addRef(fileNumber,encodeTypeEntry(elaboratedType.getFullyQualifiedNameCharArrays(),FWD_STRUCT, ICSearchConstants.DECLARATIONS),offset,offsetLength, ICIndexStorageConstants.OFFSET);
        }
        else if (elaboratedType.getClassKind().equals(ASTClassKind.UNION))
        {
            output.addRef(fileNumber,encodeTypeEntry(elaboratedType.getFullyQualifiedNameCharArrays(),FWD_UNION, ICSearchConstants.DECLARATIONS),offset,offsetLength, ICIndexStorageConstants.OFFSET);         
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
        
        output.addRef(fileNumber, encodeEntry(function.getFullyQualifiedNameCharArrays(),FUNCTION_DECL,FUNCTION_DECL_LENGTH),offset,offsetLength, ICIndexStorageConstants.OFFSET);
        
        Iterator i=function.getParameters();
        while (i.hasNext()){
            Object parm = i.next();
            if (parm instanceof IASTParameterDeclaration){
                IASTParameterDeclaration parmDecl = (IASTParameterDeclaration) parm;
                offset = parmDecl.getNameOffset();
                offsetLength = parmDecl.getNameEndOffset() - offset;
                output.addRef(fileNumber, encodeTypeEntry(new char[][]{parmDecl.getNameCharArray()}, VAR, ICSearchConstants.DECLARATIONS),offset,offsetLength, ICIndexStorageConstants.OFFSET);
            }
        }
    }
    
    public void addFunctionReference(IASTFunction function, int fileNumber){
        int offset = function.getNameOffset();
        int offsetLength = function.getNameEndOffset() - offset;
        output.addRef(fileNumber, encodeEntry(function.getFullyQualifiedNameCharArrays(),FUNCTION_REF,FUNCTION_REF_LENGTH),offset,offsetLength, ICIndexStorageConstants.OFFSET);
    }
    
    public void addNameReference(){
        
    }
    
    public void addNamespaceDefinition(IASTNamespaceDefinition namespace, int fileNumber){
        int offset = namespace.getNameOffset();
        int offsetLength = namespace.getNameEndOffset() - offset;
        output.addRef(fileNumber, encodeEntry(namespace.getFullyQualifiedNameCharArrays(),NAMESPACE_DECL,NAMESPACE_DECL_LENGTH),offset,offsetLength, ICIndexStorageConstants.OFFSET);
    }
    
    public void addNamespaceReference(IASTNamespaceDefinition namespace, int fileNumber) {
        int offset = namespace.getNameOffset();
        int offsetLength = namespace.getNameEndOffset() - offset;
        output.addRef(fileNumber, encodeEntry(namespace.getFullyQualifiedNameCharArrays(),NAMESPACE_REF,NAMESPACE_REF_LENGTH),offset,offsetLength, ICIndexStorageConstants.OFFSET);
    }
    
    public void addTypedefReference( IASTTypedefDeclaration typedef, int fileNumber ){
        int offset = typedef.getNameOffset();
        int offsetLength = typedef.getNameEndOffset() - offset;
        output.addRef(fileNumber,encodeTypeEntry( typedef.getFullyQualifiedNameCharArrays(), TYPEDEF, ICSearchConstants.REFERENCES),offset, offsetLength, ICIndexStorageConstants.OFFSET);
    }

    private void addSuperTypeReference(int modifiers, char[] packageName, char[] typeName, char[][] enclosingTypeNames, char classOrInterface, char[] superTypeName, char superClassOrInterface){

    }
    
    public void addTypeReference(char[] typeName){
        //output.addRef(CharOperation.concat(TYPE_REF, CharOperation.lastSegment(typeName, '.')));
    }
    
    public void addClassReference(IASTTypeSpecifier reference, int fileNumber){
        char[][] fullyQualifiedName = null;
        ASTClassKind classKind = null;
        int offset=0;
        int offsetLength=1;
        
        if (reference instanceof IASTClassSpecifier){
          IASTClassSpecifier classRef = (IASTClassSpecifier) reference;
          fullyQualifiedName = classRef.getFullyQualifiedNameCharArrays();
          classKind = classRef.getClassKind();
          offset=classRef.getNameOffset();
        }
        else if (reference instanceof IASTElaboratedTypeSpecifier){
          IASTElaboratedTypeSpecifier typeRef = (IASTElaboratedTypeSpecifier) reference;
          fullyQualifiedName = typeRef.getFullyQualifiedNameCharArrays();
          classKind = typeRef.getClassKind();
          offset=typeRef.getNameOffset();
          offsetLength=typeRef.getNameEndOffset()-offset;
        }
    
        if (classKind.equals(ASTClassKind.CLASS))
        {  
            output.addRef(fileNumber, encodeTypeEntry(fullyQualifiedName,CLASS, ICSearchConstants.REFERENCES),offset,offsetLength, ICIndexStorageConstants.OFFSET);
        }       
        else if (classKind.equals(ASTClassKind.STRUCT))
        {
            output.addRef(fileNumber, encodeTypeEntry(fullyQualifiedName,STRUCT,ICSearchConstants.REFERENCES),offset,offsetLength, ICIndexStorageConstants.OFFSET);
        }
        else if (classKind.equals(ASTClassKind.UNION))
        {
            output.addRef(fileNumber, encodeTypeEntry(fullyQualifiedName,UNION,ICSearchConstants.REFERENCES),offset,offsetLength, ICIndexStorageConstants.OFFSET);         
        }
    }
    
    public void addForwardClassReference(IASTTypeSpecifier reference, int fileNumber){
        char[][] fullyQualifiedName = null;
        ASTClassKind classKind = null;
        int offset=0;
        int offsetLength=1;
        if (reference instanceof IASTElaboratedTypeSpecifier){
          IASTElaboratedTypeSpecifier typeRef = (IASTElaboratedTypeSpecifier) reference;
          fullyQualifiedName = typeRef.getFullyQualifiedNameCharArrays();
          classKind = typeRef.getClassKind();
          offset=typeRef.getNameOffset();
          offsetLength=typeRef.getNameEndOffset() - offset;
        }
    
        if (classKind == null)
            return;
        
        if (classKind.equals(ASTClassKind.CLASS))
        {  
            output.addRef(fileNumber, encodeTypeEntry(fullyQualifiedName,FWD_CLASS, ICSearchConstants.REFERENCES),offset,offsetLength, ICIndexStorageConstants.OFFSET);
        }       
        else if (classKind.equals(ASTClassKind.STRUCT))
        {
            output.addRef(fileNumber, encodeTypeEntry(fullyQualifiedName,FWD_STRUCT,ICSearchConstants.REFERENCES),offset,offsetLength, ICIndexStorageConstants.OFFSET);
        }
        else if (classKind.equals(ASTClassKind.UNION))
        {
            output.addRef(fileNumber, encodeTypeEntry(fullyQualifiedName,FWD_UNION,ICSearchConstants.REFERENCES),offset,offsetLength, ICIndexStorageConstants.OFFSET);         
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
        this.output.addRef(fileNumber, encodeEntry(incName, INCLUDE_REF, INCLUDE_REF_LENGTH),1,1, ICIndexStorageConstants.LINE);
    }
    
    /**
     * Type entries are encoded as follow: 'typeDecl/' ('C' | 'S' | 'U' | 'E' ) '/'  TypeName ['/' Qualifier]* 
     */
     protected static final char[] encodeTypeEntry( char[][] fullTypeName, int typeType, LimitTo encodeType){ 

        int pos = 0, nameLength = 0;
        for (int i=0; i<fullTypeName.length; i++){
            char[] namePart = fullTypeName[i];
            nameLength+= namePart.length;
        }
        
        char [] result = null;
        if( encodeType == REFERENCES ){
            //char[] has to be of size - [type decl length + length of the name + separators + letter]
            result = new char[TYPE_REF_LENGTH + nameLength + fullTypeName.length + 1 ];
            System.arraycopy(TYPE_REF, 0, result, 0, pos = TYPE_REF_LENGTH);
        
        } else if( encodeType == DECLARATIONS ){
            //char[] has to be of size - [type decl length + length of the name + separators + letter]
            result = new char[TYPE_DECL_LENGTH + nameLength + fullTypeName.length + 1 ];
            System.arraycopy(TYPE_DECL, 0, result, 0, pos = TYPE_DECL_LENGTH);
        }
        switch (typeType)
        {
            case(CLASS):
            result[pos++] = CLASS_SUFFIX;
            break;
            
            case(STRUCT):
            result[pos++] = STRUCT_SUFFIX;
            break;
            
            case(UNION):
            result[pos++] = UNION_SUFFIX;
            break;
            
            case(ENUM):
            result[pos++] = ENUM_SUFFIX;
            break;
            
            case (VAR):
            result[pos++] = VAR_SUFFIX;
            break;
            
            case (TYPEDEF):
            result[pos++] = TYPEDEF_SUFFIX;
            break;
            
            case(DERIVED):
            result[pos++]= DERIVED_SUFFIX;
            break;
            
            case(FRIEND):
            result[pos++]=FRIEND_SUFFIX;
            break;
            
            case(FWD_CLASS):
            result[pos++]=FWD_CLASS_SUFFIX;
            break;
            
            case (FWD_STRUCT):
            result[pos++]=FWD_STRUCT_SUFFIX;
            break;
            
            case (FWD_UNION):
            result[pos++]=FWD_UNION_SUFFIX;
            break;
        }
        result[pos++] = SEPARATOR;
        //Encode in the following manner
        //  [typeDecl info]/[typeName]/[qualifiers]
        if (fullTypeName.length > 0){
        //Extract the name first
            char [] tempName = fullTypeName[fullTypeName.length-1];
            System.arraycopy(tempName, 0, result, pos, tempName.length);
            pos += tempName.length;
        }
        //Extract the qualifiers
        for (int i=fullTypeName.length - 2; i >= 0; i--){
            result[pos++] = SEPARATOR;
            char [] tempName = fullTypeName[i];
            System.arraycopy(tempName, 0, result, pos, tempName.length);
            pos+=tempName.length;               
        }
        
        if (AbstractIndexer.VERBOSE)
            AbstractIndexer.verbose(new String(result));
            
        return result;
    }
    /**
     * Namespace entries are encoded as follow: '[prefix]/' TypeName ['/' Qualifier]*
     */
    protected static final char[] encodeEntry(char[][] elementName, char[] prefix, int prefixSize){ 
        int pos, nameLength = 0;
        for (int i=0; i<elementName.length; i++){
            char[] namePart = elementName[i];
            nameLength+= namePart.length;
        }
        //char[] has to be of size - [type length + length of the name (including qualifiers) + 
        //separators (need one less than fully qualified name length)
        char[] result = new char[prefixSize + nameLength + elementName.length - 1 ];
        System.arraycopy(prefix, 0, result, 0, pos = prefix.length);
        if (elementName.length > 0){
        //Extract the name first
            char [] tempName = elementName[elementName.length-1];
            System.arraycopy(tempName, 0, result, pos, tempName.length);
            pos += tempName.length;
        }
        //Extract the qualifiers
        for (int i=elementName.length - 2; i>=0; i--){
            result[pos++] = SEPARATOR;
            char [] tempName = elementName[i];
            System.arraycopy(tempName, 0, result, pos, tempName.length);
            pos+=tempName.length;               
        }
        
        if (AbstractIndexer.VERBOSE)
            AbstractIndexer.verbose(new String(result));
            
        return result;
    }
    
}
