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

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
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
import org.eclipse.cdt.internal.core.index.cindexstorage.ICIndexStorageConstants;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;

public class CGenerateIndexVisitor extends CASTVisitor {
    private DOMSourceIndexerRunner indexer; 
    private IFile resourceFile;
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
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.ASTVisitor#visit(org.eclipse.cdt.core.dom.ast.IASTName)
     */
    public int visit(IASTName name) {
        // qualified names are going to be handled segment by segment 
//        if (name instanceof ICPPASTQualifiedName) return PROCESS_CONTINUE;
        
        try {
            processName(name);
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
            // Get the location
            IASTFileLocation loc = IndexEncoderUtil.getFileLocation(problem);
            processProblem(problem, loc);
        }
        return super.visit(problem);
    }

    /**
     * @param name
     * @throws DOMException
     */
    private void processName(IASTName name) throws DOMException {
        // Quick check to see if the name is a reference in an external header file
		//if (IndexEncoderUtil.nodeInExternalHeader(name) && name.isReference())
		if (IndexEncoderUtil.nodeInExternalHeader(name))
			return;

		IBinding binding = name.resolveBinding();
        // check for IProblemBinding
        if (binding instanceof IProblemBinding) {
            IProblemBinding problem = (IProblemBinding) binding;
            if (indexer.areProblemMarkersEnabled() && indexer.shouldRecordProblem(problem)){
                // Get the location
                IASTFileLocation loc = IndexEncoderUtil.getFileLocation(name);
                processProblem(name, loc);
            }
            return;
        }

        // Get the location
        IASTFileLocation loc = IndexEncoderUtil.getFileLocation(name);
        if (loc != null) {
            //Check to see if this reference actually occurs in the file being indexed
            //or if it occurs in another file
            int indexFlag = IndexEncoderUtil.calculateIndexFlags(indexer, loc);
    
            processNameBinding(name, binding, loc, indexFlag);
        }
    }        

    /**
     * @param name
     */
    private void processProblem(IASTNode node, IASTFileLocation loc) {
        IFile tempFile = resourceFile;
        //If we are in an include file, get the include file
        if (loc != null) {
            String fileName = loc.getFileName();
            tempFile = CCorePlugin.getWorkspace().getRoot().getFileForLocation(new Path(fileName));
            if (tempFile != null) {
                indexer.generateMarkerProblem(tempFile, resourceFile, node, loc);
            }
        }
    }

    /**
     * @param name
     * @param binding
     * @param loc 
     * @param indexFlag
     * @throws DOMException 
     */
    
    private void processNameBinding(IASTName name, IBinding binding, IASTFileLocation loc, int fileNumber) throws DOMException {
        // determine type
        if (binding instanceof ICompositeType) {
            int compositeKey = ((ICompositeType) binding).getKey();
            ASTNodeProperty prop = name.getPropertyInParent();
            switch (compositeKey) {
                case ICompositeType.k_struct:
                    if (name.isDeclaration() && prop == IASTElaboratedTypeSpecifier.TYPE_NAME)
                        if (name.isDeclaration()) {
                            indexer.getOutput().addFwd_StructDecl(fileNumber, getFullyQualifiedName(name),loc.getNodeOffset(),loc.getNodeLength(),ICIndexStorageConstants.OFFSET);
                        }                   
                        else if (name.isReference()) {
                            indexer.getOutput().addFwd_StructRef(fileNumber, getFullyQualifiedName(name),loc.getNodeOffset(),loc.getNodeLength(),ICIndexStorageConstants.OFFSET);
                        }
                    else
                        if (name.isDeclaration()) {
                            indexer.getOutput().addStructDecl(fileNumber, getFullyQualifiedName(name),loc.getNodeOffset(),loc.getNodeLength(),ICIndexStorageConstants.OFFSET);
                        }                   
                        else if (name.isReference()) {
                            indexer.getOutput().addStructRef(fileNumber, getFullyQualifiedName(name),loc.getNodeOffset(),loc.getNodeLength(),ICIndexStorageConstants.OFFSET);
                        }
                    break;
                case ICompositeType.k_union:     
                    if (name.isDeclaration() && prop == IASTElaboratedTypeSpecifier.TYPE_NAME)
                        if (name.isDeclaration()) {
                            indexer.getOutput().addFwd_UnionDecl(fileNumber, getFullyQualifiedName(name),loc.getNodeOffset(),loc.getNodeLength(),ICIndexStorageConstants.OFFSET);
                        }                   
                        else if (name.isReference()) {
                            indexer.getOutput().addFwd_UnionRef(fileNumber, getFullyQualifiedName(name),loc.getNodeOffset(),loc.getNodeLength(),ICIndexStorageConstants.OFFSET);
                        }
                    else
                        if (name.isDeclaration()) {
                            indexer.getOutput().addUnionDecl(fileNumber, getFullyQualifiedName(name),loc.getNodeOffset(),loc.getNodeLength(),ICIndexStorageConstants.OFFSET);
                        }                   
                        else if (name.isReference()) {
                            indexer.getOutput().addUnionRef(fileNumber, getFullyQualifiedName(name),loc.getNodeOffset(),loc.getNodeLength(),ICIndexStorageConstants.OFFSET);
                        }
                    break;
            }
        }
        else if (binding instanceof IEnumeration)
            if (name.isDeclaration()) {
                indexer.getOutput().addEnumDecl(fileNumber, getFullyQualifiedName(name),loc.getNodeOffset(),loc.getNodeLength(),ICIndexStorageConstants.OFFSET);
            }                   
            else if (name.isReference()) {
                indexer.getOutput().addEnumRef(fileNumber, getFullyQualifiedName(name),loc.getNodeOffset(),loc.getNodeLength(),ICIndexStorageConstants.OFFSET);
            }
        else if (binding instanceof ITypedef)
            if (name.isDeclaration()) {
                indexer.getOutput().addTypedefDecl(fileNumber, getFullyQualifiedName(name),loc.getNodeOffset(),loc.getNodeLength(),ICIndexStorageConstants.OFFSET);
            }                   
            else if (name.isReference()) {
                indexer.getOutput().addTypedefRef(fileNumber, getFullyQualifiedName(name),loc.getNodeOffset(),loc.getNodeLength(),ICIndexStorageConstants.OFFSET);
            }
        else if (binding instanceof IEnumerator)
            if (name.isDeclaration()) {
                indexer.getOutput().addEnumtorDecl(fileNumber, getFullyQualifiedName(name),loc.getNodeOffset(),loc.getNodeLength(),ICIndexStorageConstants.OFFSET);
            }                   
            else if (name.isReference()) {
                indexer.getOutput().addEnumtorRef(fileNumber, getFullyQualifiedName(name),loc.getNodeOffset(),loc.getNodeLength(),ICIndexStorageConstants.OFFSET);
            }
        else if (binding instanceof IField) 
            if (name.isDeclaration()) {
                indexer.getOutput().addFieldDecl(fileNumber, getFullyQualifiedName(name),loc.getNodeOffset(),loc.getNodeLength(),ICIndexStorageConstants.OFFSET);
            }                   
            else if (name.isReference()) {
                indexer.getOutput().addFieldRef(fileNumber, getFullyQualifiedName(name),loc.getNodeOffset(),loc.getNodeLength(),ICIndexStorageConstants.OFFSET);
            }
        else if (binding instanceof IParameter ||
                 binding instanceof IVariable) 
            if (name.isDeclaration()) {
                indexer.getOutput().addVarDecl(fileNumber, getFullyQualifiedName(name),loc.getNodeOffset(),loc.getNodeLength(),ICIndexStorageConstants.OFFSET);
            }                   
            else if (name.isReference()) {
                indexer.getOutput().addVarRef(fileNumber, getFullyQualifiedName(name),loc.getNodeOffset(),loc.getNodeLength(),ICIndexStorageConstants.OFFSET);
            }
        else if (binding instanceof IFunction)
            if (name.isDeclaration()) {
                indexer.getOutput().addFunctionDecl(fileNumber, getFullyQualifiedName(name),loc.getNodeOffset(),loc.getNodeLength(),ICIndexStorageConstants.OFFSET);
            }                   
            else if (name.isReference()) {
                indexer.getOutput().addFunctionRef(fileNumber, getFullyQualifiedName(name),loc.getNodeOffset(),loc.getNodeLength(),ICIndexStorageConstants.OFFSET);
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
