/***********************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 ***********************************************************************/
package org.eclipse.cdt.internal.core.index.domsourceindexer;

import java.io.IOException;

import org.eclipse.cdt.core.ICLogConstants;
import org.eclipse.cdt.core.dom.CDOM;
import org.eclipse.cdt.core.dom.IASTServiceProvider.UnsupportedDialectException;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.cdt.core.parser.ParseError;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.search.ICSearchConstants;
import org.eclipse.cdt.internal.core.index.impl.IndexedFile;
import org.eclipse.cdt.internal.core.index.sourceindexer.AbstractIndexer;
import org.eclipse.cdt.internal.core.index.sourceindexer.SourceIndexer;
import org.eclipse.cdt.internal.core.search.indexing.IIndexEncodingConstants;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;

/**
 * A DOMSourceIndexerRunner indexes source files using the DOM AST.
 * 
 * @author vhirsl
 */
public class DOMSourceIndexerRunner extends AbstractIndexer {

    private IFile resourceFile;
    private SourceIndexer indexer;

    public DOMSourceIndexerRunner(IFile resource, SourceIndexer indexer) {
        this.resourceFile = resource;
        this.indexer = indexer;
    }

    public IFile getResourceFile() {
        return resourceFile;
    }

    public void setFileTypes(String[] fileTypes) {
        // TODO Auto-generated method stub

    }

    protected void indexFile(IFile file) throws IOException {
        // Add the name of the file to the index
        IndexedFile indFile =output.addIndexedFile(file.getFullPath().toString());
      
        int problems = indexer.indexProblemsEnabled(resourceFile.getProject());
        setProblemMarkersEnabled(problems);
        requestRemoveMarkers(resourceFile, null);
        
        //C or CPP?
        ParserLanguage language = CoreModel.hasCCNature(resourceFile.getProject()) ? 
                ParserLanguage.CPP : ParserLanguage.C;
        
        try {
            long startTime = 0, parseTime = 0, endTime = 0;
            
            if (AbstractIndexer.TIMING)
                startTime = System.currentTimeMillis();
            
            IASTTranslationUnit tu = CDOM.getInstance().getASTService().getTranslationUnit(
                    resourceFile,
                    CDOM.getInstance().getCodeReaderFactory(CDOM.PARSE_SAVED_RESOURCES));
            
            if (AbstractIndexer.TIMING)
                parseTime = System.currentTimeMillis();
            
            // TODO Use new method to get ordered include directives instead of
            // IASTTranslationUnit.getIncludeDirectives
            processIncludeDirectives(tu.getIncludeDirectives());
            processMacroDefinitions(tu.getMacroDefinitions());
            
            ASTVisitor visitor = null;
            if (language == ParserLanguage.CPP) {
                visitor = new CPPGenerateIndexVisitor(this, resourceFile);
            } else {
                visitor = new CGenerateIndexVisitor(this, resourceFile);
            }
           
            tu.accept(visitor);
    
            if (AbstractIndexer.TIMING){
                endTime = System.currentTimeMillis();
                System.out.println("DOM Indexer - Total Parse Time for " + resourceFile.getName()  + ": " + (parseTime - startTime)); //$NON-NLS-1$ //$NON-NLS-2$
                System.out.println("DOM Indexer - Total Visit Time for " + resourceFile.getName()  + ": " + (endTime - parseTime)); //$NON-NLS-1$  //$NON-NLS-2$
                System.out.println("DOM Indexer - Total Index Time for " + resourceFile.getName()  + ": " + (endTime - startTime)); //$NON-NLS-1$  //$NON-NLS-2$
            }
            if (AbstractIndexer.VERBOSE){
                AbstractIndexer.verbose("DOM AST TRAVERSAL FINISHED " + resourceFile.getName().toString()); //$NON-NLS-1$
            }   
        }
        catch ( VirtualMachineError vmErr){
            if (vmErr instanceof OutOfMemoryError){
                org.eclipse.cdt.internal.core.model.Util.log(null, "Out Of Memory error: " + vmErr.getMessage() + " on File: " + resourceFile.getName(), ICLogConstants.CDT); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
        catch (ParseError e){
            org.eclipse.cdt.internal.core.model.Util.log(null, "Parser Timeout on File: " + resourceFile.getName(), ICLogConstants.CDT); //$NON-NLS-1$
        }
        catch (UnsupportedDialectException e) {
            org.eclipse.cdt.internal.core.model.Util.log(null, "Unsupported C/C++ dialect on File: " + resourceFile.getName(), ICLogConstants.CDT); //$NON-NLS-1$
        }
        catch (Exception ex) {
            if (ex instanceof IOException)
                throw (IOException) ex;
        }
        finally{
            // if the user disable problem reporting since we last checked, don't report the collected problems
            if (areProblemMarkersEnabled()) {
                reportProblems();
            }
            
            // Report events
//            ArrayList filesTrav = requestor.getFilesTraversed();
//            IndexDelta indexDelta = new IndexDelta(resourceFile.getProject(),filesTrav, IIndexDelta.INDEX_FINISHED_DELTA);
//            CCorePlugin.getDefault().getCoreModel().getIndexManager().notifyListeners(indexDelta);
            // Release all resources
        }
    }

    /**
     * @param includeDirectives
     */
    private void processIncludeDirectives(IASTPreprocessorIncludeStatement[] includeDirectives) {
        IProject resourceProject = resourceFile.getProject();
        for (int i = 0; i < includeDirectives.length; i++) {
            String include = includeDirectives[i].getPath();
            // TODO reimplement when ordered collection becomes available
//            getOutput().addIncludeRef(include);
//            // where is this header file included
//            IASTNodeLocation[] locations = includeDirectives[i].getNodeLocations();
//            for (int j = 0; j < locations.length; j++) {
//                if (locations[j] instanceof IASTFileLocation) {
//                    IASTFileLocation fileLocation = (IASTFileLocation) locations[j];
//                    String parent = fileLocation.getFileName();
//                    /* Check to see if this is a header file */
//                    ICFileType type = CCorePlugin.getDefault().getFileType(resourceProject, parent);
//    
//                    if (type.isHeader()) {
//                        getOutput().addRelatives(include, parent);
//                    }
//                }
//            }
            int fileNumber = getOutput().getIndexedFile(
                    getResourceFile().getFullPath().toString()).getFileNumber();
            getOutput().addRef(fileNumber,IndexEncoderUtil.encodeEntry(
                        new char[][] {include.toCharArray()}, 
                        IIndexEncodingConstants.INCLUDE,
                        ICSearchConstants.REFERENCES));

            /* See if this file has been encountered before */
            indexer.haveEncounteredHeader(resourceProject.getFullPath(),new Path(include));
        }
    }

    /**
     * @param macroDefinitions
     */
    private void processMacroDefinitions(IASTPreprocessorMacroDefinition[] macroDefinitions) {
        for (int i = 0; i < macroDefinitions.length; i++) {
            IASTName macro = macroDefinitions[i].getName();
            int fileNumber = IndexEncoderUtil.calculateIndexFlags(this, macro);
            getOutput().addRef(fileNumber, IndexEncoderUtil.encodeEntry(
                        new char[][] {macro.toCharArray()},
                        IIndexEncodingConstants.MACRO,
                        ICSearchConstants.DECLARATIONS));
        }
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.index.sourceindexer.AbstractIndexer#addMarkers(org.eclipse.core.resources.IFile, org.eclipse.core.resources.IFile, java.lang.Object)
     */
    protected void addMarkers(IFile tempFile, IFile originator, Object problem) {
        String fileName;
        int sourceLineNumber = -1;
        String errorMessage = ""; //$NON-NLS-1$
        IASTNodeLocation location = null;
       
        if (problem instanceof IASTProblem) {
            IASTProblem astProblem = (IASTProblem) problem;
            errorMessage = astProblem.getMessage();
            location = astProblem.getNodeLocations()[0];
        }
        else if (problem instanceof IASTName) { // semantic error specified in IProblemBinding
            IASTName name = (IASTName) problem;
            if (name.resolveBinding() instanceof IProblemBinding) {
                IProblemBinding problemBinding = (IProblemBinding) name.resolveBinding(); 
                errorMessage = problemBinding.getMessage();
                location = name.getNodeLocations()[0];
                IASTNode node = problemBinding.getASTNode();
//                if (node != null && !name.equals(node)) {
//                    // TODO may require further processing - looking at the IProblemBinding id
//                    location = node.getNodeLocations()[0];
//                }
            }
        }
        if (location != null) {
            if (location instanceof IASTFileLocation) {
                IASTFileLocation fileLoc = (IASTFileLocation) location;
                fileName = fileLoc.getFileName(); 
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
                            if (tempInt != null && tempInt.intValue()== sourceLineNumber &&
                                    tempMsgString.equalsIgnoreCase(INDEXER_MARKER_PREFIX + errorMessage)) {
                                newProblem = false;
                                break;
                            }
                        }
                    }
                    if (newProblem) {
                        IMarker marker = tempFile.createMarker(ICModelMarker.INDEXER_MARKER);
                        int start = fileLoc.getNodeOffset();
                        int end = start + fileLoc.getNodeLength();
        //                marker.setAttribute(IMarker.LOCATION, iProblem.getSourceLineNumber());
                        marker.setAttribute(IMarker.LOCATION, 1);
                        marker.setAttribute(IMarker.MESSAGE, INDEXER_MARKER_PREFIX + errorMessage);
                        marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
        //                marker.setAttribute(IMarker.LINE_NUMBER, iProblem.getSourceLineNumber());
                        marker.setAttribute(IMarker.LINE_NUMBER, 1);
                        marker.setAttribute(IMarker.CHAR_START, start);
                        marker.setAttribute(IMarker.CHAR_END, end); 
                        marker.setAttribute(INDEXER_MARKER_ORIGINATOR, originator.getFullPath().toString());
                    }
                    
                 } catch (CoreException e) {
                     // You need to handle the cases where attribute value is rejected
                 }
            }
        }
    }

    public boolean shouldRecordProblem(IASTProblem problem) {
        boolean preprocessor = (getProblemMarkersEnabled() & SourceIndexer.PREPROCESSOR_PROBLEMS_BIT ) != 0;
        boolean semantics = (getProblemMarkersEnabled() & SourceIndexer.SEMANTIC_PROBLEMS_BIT ) != 0;
        boolean syntax = (getProblemMarkersEnabled() & SourceIndexer.SYNTACTIC_PROBLEMS_BIT ) != 0;
        
        if (problem.checkCategory(IASTProblem.PREPROCESSOR_RELATED) || 
                problem.checkCategory(IASTProblem.SCANNER_RELATED))
            return preprocessor && problem.getID() != IASTProblem.PREPROCESSOR_CIRCULAR_INCLUSION;
        else if (problem.checkCategory(IASTProblem.SEMANTICS_RELATED))
            return semantics;
        else if (problem.checkCategory(IASTProblem.SYNTAX_RELATED))
            return syntax;
        
        return false;
    }

    /**
     * @param binding
     * @return
     */
    public boolean shouldRecordProblem(IProblemBinding problem) {
        return (getProblemMarkersEnabled() & SourceIndexer.SEMANTIC_PROBLEMS_BIT) != 0;
    }

}
