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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.c.CASTVisitor;
import org.eclipse.cdt.core.search.ICSearchConstants;
import org.eclipse.cdt.internal.core.search.indexing.IIndexEncodingConstants;
import org.eclipse.cdt.internal.core.search.indexing.IIndexEncodingConstants.EntryType;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;

public class CGenerateIndexVisitor extends CASTVisitor {
    private DOMSourceIndexerRunner indexer; 
    private IFile resourceFile;
    private List problems;
    {
        shouldVisitNames          = true;
//        shouldVisitDeclarations   = false;
//        shouldVisitInitializers   = false;
//        shouldVisitParameterDeclarations = false;
//        shouldVisitDeclarators    = false;
//        shouldVisitDeclSpecifiers = false;
//        shouldVisitExpressions    = false;
//        shouldVisitStatements     = false;
//        shouldVisitTypeIds        = false;
//        shouldVisitEnumerators    = false;
//        shouldVisitTranslationUnit = false;
        shouldVisitProblems       = true;

//      shouldVisitDesignators    = false
    }

    public CGenerateIndexVisitor(DOMSourceIndexerRunner indexer, IFile resourceFile) {
        super();
        this.indexer = indexer;
        this.resourceFile = resourceFile;
        problems = new ArrayList();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.ASTVisitor#visit(org.eclipse.cdt.core.dom.ast.IASTName)
     */
    public int visit(IASTName name) {
        //Check to see if this reference actually occurs in the file being indexed
        //or if it occurs in another file
        int indexFlag = IndexEncoderUtil.calculateIndexFlags(indexer, name);

        // qualified names are going to be handled segment by segment 
//        if (name instanceof ICPPASTQualifiedName) return PROCESS_CONTINUE;
        
        try {
            processName(name, indexFlag);
        }
        catch (DOMException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return PROCESS_CONTINUE;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.ASTVisitor#visit(org.eclipse.cdt.core.dom.ast.IASTProblem)
     */
    public int visit(IASTProblem problem) {
        if (indexer.areProblemMarkersEnabled() && indexer.shouldRecordProblem(problem)){
            IFile tempFile = resourceFile;
          
            //If we are in an include file, get the include file
            IASTNodeLocation[] locs = problem.getNodeLocations();
            if (locs[0] instanceof IASTFileLocation) {
                IASTFileLocation fileLoc = (IASTFileLocation) locs[0];
                String fileName = fileLoc.getFileName();
                tempFile = CCorePlugin.getWorkspace().getRoot().getFileForLocation(new Path(fileName));
            }
            
            if( tempFile != null ){
                  indexer.generateMarkerProblem(tempFile, resourceFile, problem);
            }
        }
        return super.visit(problem);
    }

    /**
     * @param name
     * @param indexFlag
     * @throws DOMException
     */
    private void processName(IASTName name, int indexFlag) throws DOMException {
        IBinding binding = name.resolveBinding();
        // check for IProblemBinding
        if (binding instanceof IProblemBinding) {
            IProblemBinding problem = (IProblemBinding) binding;
            if (indexer.areProblemMarkersEnabled() && indexer.shouldRecordProblem(problem)){
                IFile tempFile = resourceFile;
              
                //If we are in an include file, get the include file
                IASTNodeLocation[] locs = name.getNodeLocations();
                if (locs[0] instanceof IASTFileLocation) {
                    IASTFileLocation fileLoc = (IASTFileLocation) locs[0];
                    String fileName = fileLoc.getFileName();
                    tempFile = CCorePlugin.getWorkspace().getRoot().getFileForLocation(new Path(fileName));
                }
                
                if( tempFile != null ){
                      indexer.generateMarkerProblem(tempFile, resourceFile, problem);
                }
            }
            return;
        }
        processNameBinding(name, binding, indexFlag);
    }        

    /**
     * @param name
     * @param binding
     * @param indexFlag
     * @throws DOMException 
     */
    private void processNameBinding(IASTName name, IBinding binding, int fileNumber) throws DOMException {
        // determine type
        EntryType entryType = null;
        if (binding instanceof ICompositeType) {
            int compositeKey = ((ICompositeType) binding).getKey();
            ASTNodeProperty prop = name.getPropertyInParent();
            switch (compositeKey) {
                case ICompositeType.k_struct:
                    entryType = IIndexEncodingConstants.STRUCT;
                    if (name.isDeclaration() && prop == IASTElaboratedTypeSpecifier.TYPE_NAME)
                        entryType = IIndexEncodingConstants.FWD_STRUCT;
                    break;
                case ICompositeType.k_union:
                    entryType = IIndexEncodingConstants.UNION;
                    if (name.isDeclaration() && prop == IASTElaboratedTypeSpecifier.TYPE_NAME)
                        entryType = IIndexEncodingConstants.FWD_UNION;
                    break;
            }
        }
        else if (binding instanceof IEnumeration)
            entryType = IIndexEncodingConstants.ENUM;
        else if (binding instanceof ITypedef)
            entryType = IIndexEncodingConstants.TYPEDEF;
        else if (binding instanceof IEnumerator)
            entryType = IIndexEncodingConstants.ENUMERATOR;
        else if (binding instanceof IField) 
            entryType = IIndexEncodingConstants.FIELD;
        else if (binding instanceof IParameter ||
                 binding instanceof IVariable) 
            entryType = IIndexEncodingConstants.VAR;
        else if (binding instanceof IFunction)
            entryType = IIndexEncodingConstants.FUNCTION;
        
        if (entryType != null) {
            if (name.isDeclaration()) {
                indexer.getOutput().addRef(fileNumber,IndexEncoderUtil.encodeEntry(
                            getFullyQualifiedName(name),
                            entryType,
                            ICSearchConstants.DECLARATIONS));
            }                   
            else if (name.isReference()) {
                indexer.getOutput().addRef(fileNumber, IndexEncoderUtil.encodeEntry(
                            getFullyQualifiedName(name),
                            entryType,
                            ICSearchConstants.REFERENCES));
            }
        }
    }

    /**
     * @param name
     * @return
     */
    private char[][] getFullyQualifiedName(IASTName name) {
        IBinding binding = name.resolveBinding();
        if (!(binding instanceof IField))
            return new char[][] {name.toCharArray()};
        // special case for fields
        IASTName parent = null;
        try {
            parent = binding.getScope().getScopeName();
        }
        catch (DOMException e) {
        }
        if (parent != null)
            return new char[][] {parent.toCharArray(), name.toCharArray()};
        return new char[][] {name.toCharArray()};
    }

}
