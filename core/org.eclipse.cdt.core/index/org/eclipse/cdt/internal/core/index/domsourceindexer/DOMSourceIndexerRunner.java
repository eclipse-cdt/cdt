/***********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
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
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.parser.ParseError;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.search.ICSearchConstants;
import org.eclipse.cdt.internal.core.index.IDocument;
import org.eclipse.cdt.internal.core.index.IIndexerOutput;
import org.eclipse.cdt.internal.core.index.sourceindexer.AbstractIndexer;
import org.eclipse.cdt.internal.core.index.sourceindexer.SourceIndexer;
import org.eclipse.cdt.internal.core.search.indexing.IIndexEncodingConstants;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Path;

/**
 * A DOMSourceIndexerRunner indexes source files using the DOMAST. The following items are indexed:
 * Declarations:
 * - Classes
 * - Structs
 * - Unions
 * References:
 * - Classes
 * - Structs
 * - Unions
 * 
 * @author vhirsl
 */
public class DOMSourceIndexerRunner extends AbstractIndexer {

    private IFile resourceFile;
    private SourceIndexer indexer;
    private boolean problemReportingEnabled = false;

    public DOMSourceIndexerRunner(IFile resource, SourceIndexer indexer) {
        this.resourceFile = resource;
        this.indexer = indexer;
    }

    public IIndexerOutput getOutput() {
        return output;
    }
    
    public IFile getResourceFile() {
        return resourceFile;
    }

    /**
     * @return Returns the problemReportingEnabled.
     */
    public boolean isProblemReportingEnabled() {
        return problemReportingEnabled;
    }
    
    public void setFileTypes(String[] fileTypes) {
        // TODO Auto-generated method stub

    }

    protected void indexFile(IDocument document) throws IOException {
        // Add the name of the file to the index
        output.addDocument(document);
        problemReportingEnabled = indexer.indexProblemsEnabled(resourceFile.getProject()) != 0;
        
        //C or CPP?
        ParserLanguage language = CoreModel.hasCCNature(resourceFile.getProject()) ? 
                ParserLanguage.CPP : ParserLanguage.C;
        
        try {
            IASTTranslationUnit tu = CDOM.getInstance().getASTService().getTranslationUnit(
                    resourceFile,
                    CDOM.getInstance().getCodeReaderFactory(CDOM.PARSE_SAVED_RESOURCES));
            
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
    
            if (AbstractIndexer.VERBOSE){
                AbstractIndexer.verbose("DOM AST TRAVERSAL FINISHED " + resourceFile.getName().toString());             //$NON-NLS-1$
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
            //if the user disable problem reporting since we last checked, don't report the collected problems
//            if( manager.indexProblemsEnabled( resourceFile.getProject() ) != 0 )
//                requestor.reportProblems();
//            
//            //Report events
//            ArrayList filesTrav = requestor.getFilesTraversed();
//            IndexDelta indexDelta = new IndexDelta(resourceFile.getProject(),filesTrav, IIndexDelta.INDEX_FINISHED_DELTA);
//            CCorePlugin.getDefault().getCoreModel().getIndexManager().notifyListeners(indexDelta);
            //Release all resources
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
            int indexFlag = getOutput().getIndexedFile(
                    getResourceFile().getFullPath().toString()).getFileNumber();
            getOutput().addRef(IndexEncoderUtil.encodeEntry(
                        new char[][] {include.toCharArray()}, 
                        IIndexEncodingConstants.INCLUDE,
                        ICSearchConstants.REFERENCES),
                    indexFlag);

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
            int indexFlag = IndexEncoderUtil.calculateIndexFlags(this, macro);
            getOutput().addRef(IndexEncoderUtil.encodeEntry(
                        new char[][] {macro.toCharArray()},
                        IIndexEncodingConstants.MACRO,
                        ICSearchConstants.DECLARATIONS),
                    indexFlag);
        }
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.index.sourceindexer.AbstractIndexer#addMarkers(org.eclipse.core.resources.IFile, org.eclipse.core.resources.IFile, java.lang.Object)
     */
    protected void addMarkers(IFile tempFile, IFile originator, Object problem) {
        // TODO Auto-generated method stub
        
    }

}
