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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICLogConstants;
import org.eclipse.cdt.core.dom.CDOM;
import org.eclipse.cdt.core.dom.IASTServiceProvider.UnsupportedDialectException;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit.IDependencyTree;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit.IDependencyTree.IASTInclusionNode;
import org.eclipse.cdt.core.index.IIndexDelta;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.cdt.core.parser.IExtendedScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfoProvider;
import org.eclipse.cdt.core.parser.ParseError;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.internal.core.index.IIndex;
import org.eclipse.cdt.internal.core.index.impl.IndexDelta;
import org.eclipse.cdt.internal.core.index.sourceindexer.AbstractIndexer;
import org.eclipse.cdt.internal.core.index.sourceindexer.SourceIndexer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * A DOMSourceIndexerRunner indexes source files using the DOM AST.
 * 
 * @author vhirsl
 */
public class DOMSourceIndexerRunner extends AbstractIndexer {

    private SourceIndexer indexer;
    // timing & errors
    static int totalParseTime = 0;
    static int totalVisitTime = 0;
    static int errorCount = 0;
    static Map errors = new HashMap();

    public DOMSourceIndexerRunner(IFile resource, SourceIndexer indexer) {
        this.resourceFile = resource;
        this.indexer = indexer;
    }

    public void setFileTypes(String[] fileTypes) {
        // TODO Auto-generated method stub
    }

    protected void indexFile(IFile file) throws IOException {
        int problems = indexer.indexProblemsEnabled(resourceFile.getProject());
        // enable inclusion problem markers
        problems |= SourceIndexer.INCLUSION_PROBLEMS_BIT;
        setProblemMarkersEnabled(problems);
        requestRemoveMarkers(resourceFile, null);
        
        // do not index the file if there is no scanner info
        if (isScannerInfoEmpty(resourceFile)) {
            // generate info marker - file is not indexed
            addInfoMarker(resourceFile, CCorePlugin.getResourceString("DOMIndexerMarker.EmptyScannerInfo")); //$NON-NLS-1$
            if (areProblemMarkersEnabled()) {
                reportProblems();
            }
            return;
        }
        
        // Add the name of the file to the index
        output.addIndexedFile(file.getFullPath().toString());
      
        //C or CPP?
        ParserLanguage language = CoreModel.hasCCNature(resourceFile.getProject()) ? 
                ParserLanguage.CPP : ParserLanguage.C;
        IASTTranslationUnit tu = null;
		long startTime = 0, parseTime = 0, endTime = 0;
		String error = null;
        try {
            if (AbstractIndexer.TIMING)
                startTime = System.currentTimeMillis();
            
            tu = CDOM.getInstance().getASTService().getTranslationUnit(resourceFile,
                    CDOM.getInstance().getCodeReaderFactory(CDOM.PARSE_SAVED_RESOURCES));
            
            if (AbstractIndexer.TIMING)
                parseTime = System.currentTimeMillis();
            
            processIncludeDirectives(tu.getDependencyTree());
            processMacroDefinitions(tu.getMacroDefinitions());
            processPreprocessorProblems(tu.getPreprocessorProblems());
            
            ASTVisitor visitor = null;
            if (language == ParserLanguage.CPP) {
                visitor = new CPPGenerateIndexVisitor(this);
            } else {
                visitor = new CGenerateIndexVisitor(this);
            }
           
            tu.accept(visitor);   
 
        }
        catch (VirtualMachineError vmErr) {
			error = vmErr.toString();
            if (vmErr instanceof OutOfMemoryError) {
                org.eclipse.cdt.internal.core.model.Util.log(null, "Out Of Memory error: " + vmErr.getMessage() + " on File: " + resourceFile.getName(), ICLogConstants.CDT); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
        catch (ParseError e) {
			error = e.toString();
            org.eclipse.cdt.internal.core.model.Util.log(null, "Parser Timeout on File: " + resourceFile.getName(), ICLogConstants.CDT); //$NON-NLS-1$
        }
        catch (UnsupportedDialectException e) {
			error = e.toString();
            org.eclipse.cdt.internal.core.model.Util.log(null, "Unsupported C/C++ dialect on File: " + resourceFile.getName(), ICLogConstants.CDT); //$NON-NLS-1$
        }
        catch (Exception ex) {
			error = ex.toString();
            if (ex instanceof IOException)
                throw (IOException) ex;
        }
        finally {
           if (AbstractIndexer.TIMING) {
                endTime = System.currentTimeMillis();
				if (error != null) {
					errorCount++;
					System.out.print(error + ':' + resourceFile.getName() + ':');
                    if (!errors.containsKey(error)) {
                        errors.put(error, new Integer(0));
                    }
                    errors.put(error, new Integer(((Integer) errors.get(error)).intValue()+1)); 
				}
                System.out.println("DOM Indexer - Total Parse Time for " + resourceFile.getName()  + ": " + (parseTime - startTime)); //$NON-NLS-1$ //$NON-NLS-2$
                System.out.println("DOM Indexer - Total Visit Time for " + resourceFile.getName()  + ": " + (endTime - parseTime)); //$NON-NLS-1$  //$NON-NLS-2$
                totalParseTime += parseTime - startTime;
                totalVisitTime += endTime - parseTime;
                long currentTime = endTime - startTime;
                System.out.println("DOM Indexer - Total Index Time for " + resourceFile.getName()  + ": " + currentTime); //$NON-NLS-1$  //$NON-NLS-2$
                long tempTotaltime = indexer.getTotalIndexTime() + currentTime;
	            indexer.setTotalIndexTime(tempTotaltime);
                System.out.println("DOM Indexer - Overall Index Time: " + tempTotaltime + "(" + totalParseTime + ", " + totalVisitTime + ") " + errorCount + " errors"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                System.out.flush();
            }
            if (AbstractIndexer.VERBOSE){
                AbstractIndexer.verbose("DOM AST TRAVERSAL FINISHED " + resourceFile.getName().toString()); //$NON-NLS-1$
            }   
            // if the user disable problem reporting since we last checked, don't report the collected problems
            if (areProblemMarkersEnabled()) {
                reportProblems();
            }
            
            // Report events
            if (tu != null) {
                List filesTrav = Arrays.asList(tu.getIncludeDirectives());
                IndexDelta indexDelta = new IndexDelta(resourceFile.getProject(),filesTrav, IIndexDelta.INDEX_FINISHED_DELTA);
                indexer.notifyListeners(indexDelta);
            }
            // Release all resources
        }
    }

    /**
     * @param IFile
     * @return boolean
     */
    private boolean isScannerInfoEmpty(IFile file) {
        boolean rc = true;
        IScannerInfoProvider provider = CCorePlugin.getDefault().getScannerInfoProvider(file.getProject());
        if (provider != null){
            IScannerInfo scanInfo = provider.getScannerInformation(file);
            if (scanInfo != null) {
                if (!scanInfo.getDefinedSymbols().isEmpty() ||
                        scanInfo.getIncludePaths().length > 0) {
                    rc = false;
                }
                if (scanInfo instanceof IExtendedScannerInfo) {
                    IExtendedScannerInfo extScanInfo = (IExtendedScannerInfo) scanInfo;
                    if (extScanInfo.getLocalIncludePath().length > 0)
                        rc = false;
                    if (extScanInfo.getIncludeFiles().length > 0) {
                        rc = false;
                        for (int i = 0; i < extScanInfo.getIncludeFiles().length; i++) {
                            String includeFile = extScanInfo.getIncludeFiles()[i];
                            /* See if this file has been encountered before */
                            indexer.haveEncounteredHeader(resourceFile.getProject().getFullPath(), new Path(includeFile));
                        }
                    }
                    if (extScanInfo.getMacroFiles().length > 0) {
                        rc = false;
                        for (int i = 0; i < extScanInfo.getIncludeFiles().length; i++) {
                            String macrosFile = extScanInfo.getMacroFiles()[i];
                            /* See if this file has been encountered before */
                            indexer.haveEncounteredHeader(resourceFile.getProject().getFullPath(), new Path(macrosFile));
                        }
                    }
                }
            }
        }
        return rc;
    }

    /**
     * @param tree
     */
    private void processIncludeDirectives(IDependencyTree tree) {
        int fileNumber = getOutput().getIndexedFile(
                getResourceFile().getFullPath().toString()).getFileID();
        
        processNestedInclusions(fileNumber, tree.getInclusions(), null);
    }

    /**
     * @param fileNumber
     * @param inclusions 
     * @param parent
     */
    private void processNestedInclusions(int fileNumber, IASTInclusionNode[] inclusions, IASTInclusionNode parent) {
        for (int i = 0; i < inclusions.length; i++) {
            IASTInclusionNode inclusion = inclusions[i];
            String include = inclusion.getIncludeDirective().getPath();

            if (areProblemMarkersEnabled()) {
                IPath newPath = new Path(include);
                IFile tempFile = CCorePlugin.getWorkspace().getRoot().getFileForLocation(newPath);
                if (tempFile != null) {
                    //File is in the workspace
                    requestRemoveMarkers(tempFile, resourceFile);
                }
            }

            getOutput().addIncludeRef(fileNumber, include);
            getOutput().addRelatives(fileNumber, include, 
                    (parent != null) ? parent.getIncludeDirective().getPath() : null);
			
            IndexerOutputWrapper.addNameRef(getOutput(),
                    new char[][] {include.toCharArray()}, 
					IndexerOutputWrapper.INCLUDE,
					fileNumber,
					1,
					1,
					IIndex.OFFSET);
            
            /* See if this file has been encountered before */
            indexer.haveEncounteredHeader(resourceFile.getProject().getFullPath(), new Path(include));

            // recurse
            processNestedInclusions(fileNumber, inclusion.getNestedInclusions(), inclusion);
        }
    }

    /**
     * @param macroDefinitions
     */
    private void processMacroDefinitions(IASTPreprocessorMacroDefinition[] macroDefinitions) {
        for (int i = 0; i < macroDefinitions.length; i++) {
            IASTName macro = macroDefinitions[i].getName();
            // Get the location
            IASTFileLocation loc = IndexEncoderUtil.getFileLocation(macro);
            int fileNumber = IndexEncoderUtil.calculateIndexFlags(this, loc);
            IndexerOutputWrapper.addNameDecl(getOutput(),
                    new char[][] {macro.toCharArray()},
					IndexerOutputWrapper.MACRO,
					fileNumber,
					loc.getNodeOffset(),
                    loc.getNodeLength(),
                    IIndex.OFFSET);
        }
        
    }

    /**
     * @param preprocessorProblems
     */
    private void processPreprocessorProblems(IASTProblem[] preprocessorProblems) {
        for (int i = 0; i < preprocessorProblems.length; i++) {
            IASTProblem problem = preprocessorProblems[i];

            if (areProblemMarkersEnabled() && shouldRecordProblem(problem)) {
                // Get the location
                IASTFileLocation loc = IndexEncoderUtil.getFileLocation(problem);
                processProblem(problem.getMessage(), loc);
            }
        }
    }

    /**
     * @param name
     */
    public void processProblem(String message, IASTFileLocation loc) {
        IFile tempFile = resourceFile;
        //If we are in an include file, get the include file
        if (loc != null) {
            String fileName = loc.getFileName();
            tempFile = CCorePlugin.getWorkspace().getRoot().getFileForLocation(new Path(fileName));
            if (tempFile != null) {
                generateMarkerProblem(tempFile, resourceFile, message, loc);
            }
        }
    }

	private void generateMarkerProblem(IFile tempFile, IFile originator, String message, IASTFileLocation loc) {
        Problem tempProblem = new AddMarkerProblem(tempFile, originator, message, loc);
        if (getProblemsMap().containsKey(tempFile)) {
            List list = (List) getProblemsMap().get(tempFile);
            list.add(tempProblem);
        } else {
            List list = new ArrayList();
            list.add(new RemoveMarkerProblem(tempFile, resourceFile));  //remove existing markers
            list.add(tempProblem);
			getProblemsMap().put(tempFile, list);
        }
	}

	class AddMarkerProblem extends Problem {
		private String message;
        private IASTFileLocation location;
		public AddMarkerProblem(IResource resource, IResource originator, String message, IASTFileLocation location) {
			super(resource, originator);
			this.message = message;
			this.location = location;
		}

		public void run() {
	        if (location != null) {
	            try {
	                //we only ever add index markers on the file, so DEPTH_ZERO is far enough
	                IMarker[] markers = resource.findMarkers(ICModelMarker.INDEXER_MARKER, true,IResource.DEPTH_ZERO);
	                
	                boolean newProblem = true;
	                
	                if (markers.length > 0) {
	                    IMarker tempMarker = null;
	                    int nameStart = -1; 
	                    int nameLen = -1;
	                    String tempMsgString = null;
	                    
	                    for (int i=0; i<markers.length; i++) {
	                        tempMarker = markers[i];
	                        nameStart = ((Integer) tempMarker.getAttribute(IMarker.CHAR_START)).intValue();
	                        nameLen = ((Integer) tempMarker.getAttribute(IMarker.CHAR_END)).intValue() - nameStart;
	                        tempMsgString = (String) tempMarker.getAttribute(IMarker.MESSAGE);
	                        if (nameStart != -1 && 
	                                nameStart == location.getNodeOffset() &&
	                                nameLen == location.getNodeLength() &&
	                                tempMsgString.equalsIgnoreCase(INDEXER_MARKER_PREFIX + message)) {
	                            newProblem = false;
	                            break;
	                        }
	                    }
	                }
	                if (newProblem) {
	                    IMarker marker = resource.createMarker(ICModelMarker.INDEXER_MARKER);
	                    int start = location.getNodeOffset();
	                    int end = start + location.getNodeLength();
	                    marker.setAttribute(IMarker.LOCATION, location.getStartingLineNumber());
	                    marker.setAttribute(IMarker.MESSAGE, INDEXER_MARKER_PREFIX + message);
	                    marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
	                    marker.setAttribute(IMarker.LINE_NUMBER, location.getStartingLineNumber());
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
        
        if (problem.checkCategory(IASTProblem.PREPROCESSOR_INCLUSION_NOT_FOUND)) {
            return true;
        }
        else if (problem.checkCategory(IASTProblem.PREPROCESSOR_RELATED) || 
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

    /**
     * 
     */
    public static void printErrors() {
        if (AbstractIndexer.TIMING) {
            totalParseTime = 0;
            totalVisitTime = 0;
            System.out.println("Errors during indexing"); //$NON-NLS-1$
            for (Iterator i = errors.keySet().iterator(); i.hasNext(); ) {
                String error = (String) i.next();;
                System.out.println(error + " : " + ((Integer) errors.get(error)).toString()); //$NON-NLS-1$
            }
        }
    }

}
