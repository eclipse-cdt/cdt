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

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
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
import org.eclipse.cdt.internal.core.index.IIndex;
import org.eclipse.cdt.internal.core.index.domsourceindexer.IndexerOutputWrapper.EntryType;

public class CGenerateIndexVisitor extends CASTVisitor {
    private DOMSourceIndexerRunner indexer; 
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

    public CGenerateIndexVisitor(DOMSourceIndexerRunner indexer) {
        super();
        this.indexer = indexer;
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
            indexer.processProblem(problem, loc);
        }
        return super.visit(problem);
    }

    /**
     * @param name
     * @throws DOMException
     */
    private void processName(IASTName name) throws DOMException {
        IBinding binding = name.resolveBinding();
        // check for IProblemBinding
        if (binding instanceof IProblemBinding) {
            IProblemBinding problem = (IProblemBinding) binding;
            if (indexer.areProblemMarkersEnabled() && indexer.shouldRecordProblem(problem)){
                // Get the location
                IASTFileLocation loc = IndexEncoderUtil.getFileLocation(name);
                indexer.processProblem(name, loc);
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
     * @param binding
     * @param loc 
     * @param indexFlag
     * @throws DOMException 
     */
    private void processNameBinding(IASTName name, IBinding binding, IASTFileLocation loc, int fileNumber) throws DOMException {
        // determine type
        EntryType entryType = null;
        if (binding instanceof ICompositeType) {
            int compositeKey = ((ICompositeType) binding).getKey();
            ASTNodeProperty prop = name.getPropertyInParent();
            switch (compositeKey) {
                case ICompositeType.k_struct:
                    entryType = IndexerOutputWrapper.STRUCT;
                    if (name.isDeclaration() && prop == IASTElaboratedTypeSpecifier.TYPE_NAME)
                        entryType = IndexerOutputWrapper.FWD_STRUCT;
                    break;
                case ICompositeType.k_union:
                    entryType = IndexerOutputWrapper.UNION;
                    if (name.isDeclaration() && prop == IASTElaboratedTypeSpecifier.TYPE_NAME)
                        entryType = IndexerOutputWrapper.FWD_UNION;
                    break;
            }
        }
        else if (binding instanceof IEnumeration)
            entryType = IndexerOutputWrapper.ENUM;
        else if (binding instanceof ITypedef)
            entryType = IndexerOutputWrapper.TYPEDEF;
        else if (binding instanceof IEnumerator)
            entryType = IndexerOutputWrapper.ENUMERATOR;
        else if (binding instanceof IField) 
            entryType = IndexerOutputWrapper.FIELD;
        else if (binding instanceof IParameter ||
                 binding instanceof IVariable) 
            entryType = IndexerOutputWrapper.VAR;
        else if (binding instanceof IFunction)
            entryType = IndexerOutputWrapper.FUNCTION;
        
        if (entryType != null) {
            if (name.isDeclaration()) {
                IndexerOutputWrapper.addNameDecl(indexer.getOutput(),
                        getFullyQualifiedName(name),
                        entryType,
						fileNumber, 
                        loc.getNodeOffset(),
                        loc.getNodeLength(),
                        IIndex.OFFSET);
            }                   
            else if (name.isReference()) {
                IndexerOutputWrapper.addNameRef(indexer.getOutput(),
                        getFullyQualifiedName(name),
                        entryType,
						fileNumber, 
                        loc.getNodeOffset(),
                        loc.getNodeLength(),
                        IIndex.OFFSET);
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
