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

import org.eclipse.cdt.core.ICLogConstants;
import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
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
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.c.CASTVisitor;
import org.eclipse.cdt.internal.core.index.FunctionEntry;
import org.eclipse.cdt.internal.core.index.IIndex;
import org.eclipse.cdt.internal.core.index.NamedEntry;
import org.eclipse.cdt.internal.core.index.TypeEntry;
import org.eclipse.cdt.internal.core.model.Util;

public class CGenerateIndexVisitor extends CASTVisitor {
    private DOMSourceIndexerRunner indexer; 
    {
        shouldVisitNames          = true;
        shouldVisitDeclarations   = true;
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
     * @see org.eclipse.cdt.core.dom.ast.ASTVisitor#visit(org.eclipse.cdt.core.dom.ast.IASTDeclaration)
     */
    public int visit(IASTDeclaration declaration) {
        if (IndexEncoderUtil.nodeInVisitedExternalHeader(declaration, indexer.getIndexer())) 
            return PROCESS_SKIP;
        return PROCESS_CONTINUE;
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
            // TODO remove printStackTrace
            e.printStackTrace();
            Util.log(e, e.getProblem().getMessage(), ICLogConstants.CDT);
        }
        catch (Exception e) {
            // TODO remove printStackTrace
            e.printStackTrace();
            Util.log(e, e.toString(), ICLogConstants.CDT);
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
            indexer.processProblem(problem.getMessage(), loc);
        }
        return super.visit(problem);
    }

    /**
     * @param name
     * @throws DOMException
     */
    private void processName(IASTName name) throws DOMException {
        // Quick check to see if the name is in an already indexed external header file
		if (IndexEncoderUtil.nodeInVisitedExternalHeader(name, indexer.getIndexer())) 
			return;

		IBinding binding = name.resolveBinding();
        if (binding == null) return;

        // check for IProblemBinding
        if (binding instanceof IProblemBinding) {
            IProblemBinding problem = (IProblemBinding) binding;
            if (indexer.areProblemMarkersEnabled() && indexer.shouldRecordProblem(problem)){
                // Get the location
                IASTFileLocation loc = IndexEncoderUtil.getFileLocation(name);
                indexer.processProblem(problem.getMessage(), loc);
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
        char[][] qualifiedName = getFullyQualifiedName(name);
        if (qualifiedName == null) 
            return;
        // determine entryKind
        int entryKind = IIndex.UNKNOWN;
        if (name.isDefinition()){
            entryKind = IIndex.DEFINITION;
        }
        else if (name.isDeclaration()) {
            entryKind = IIndex.DECLARATION;
        }
        else if (name.isReference()) {
            entryKind = IIndex.REFERENCE;
        }
        else return;
        
        IASTFileLocation fileLoc = IndexEncoderUtil.getFileLocation(name);
        
        // determine type
        if (binding instanceof ICompositeType) {
            int iEntryType = 0;
            int compositeKey = ((ICompositeType) binding).getKey();
            ASTNodeProperty prop = name.getPropertyInParent();
            switch (compositeKey) {
                case ICompositeType.k_struct:
                    iEntryType = IIndex.TYPE_STRUCT;
                    if (name.isDeclaration() && prop == IASTElaboratedTypeSpecifier.TYPE_NAME)
                        iEntryType = IIndex.TYPE_STRUCT;
                    break;
                case ICompositeType.k_union:
                    iEntryType = IIndex.TYPE_UNION;
                    if (name.isDeclaration() && prop == IASTElaboratedTypeSpecifier.TYPE_NAME)
                        iEntryType = IIndex.TYPE_UNION;
                    break;
            }
            int modifiers = 0;
            if (entryKind != IIndex.REFERENCE) {
                modifiers = IndexVisitorUtil.getModifiers(name, binding);
            }
            // guard against cpp entities in c project
            if (iEntryType != 0) {
                TypeEntry indexEntry = new TypeEntry(iEntryType, entryKind, qualifiedName, modifiers, fileNumber);
                indexEntry.setNameOffset(fileLoc.getNodeOffset(), fileLoc.getNodeLength(), IIndex.OFFSET);
    
                indexEntry.serialize(indexer.getOutput());
            }
        }
        else if (binding instanceof IEnumeration){
            int modifiers = 0;
            if (entryKind != IIndex.REFERENCE) {
                modifiers = IndexVisitorUtil.getModifiers(name, binding);
            }
            TypeEntry indexEntry = new TypeEntry(IIndex.TYPE_ENUM, entryKind, qualifiedName, modifiers, fileNumber);
            indexEntry.setNameOffset(fileLoc.getNodeOffset(), fileLoc.getNodeLength(), IIndex.OFFSET);

            indexEntry.serialize(indexer.getOutput());
        }
        else if (binding instanceof ITypedef) {
            TypeEntry indexEntry = new TypeEntry(IIndex.TYPE_TYPEDEF, entryKind, qualifiedName, 0, fileNumber);
            indexEntry.setNameOffset(fileLoc.getNodeOffset(), fileLoc.getNodeLength(), IIndex.OFFSET);
    
            indexEntry.serialize(indexer.getOutput());
        }
        else if (binding instanceof IEnumerator) {
            NamedEntry indexEntry = new NamedEntry(IIndex.ENUMTOR, entryKind, qualifiedName, 0, fileNumber);
            indexEntry.setNameOffset(fileLoc.getNodeOffset(), fileLoc.getNodeLength(), IIndex.OFFSET);

            indexEntry.serialize(indexer.getOutput());
        }
        else if (binding instanceof IField) { 
            int modifiers = 0;
            if (entryKind != IIndex.REFERENCE) {
                modifiers = IndexVisitorUtil.getModifiers(name, binding);
            }
            NamedEntry indexEntry = new NamedEntry(IIndex.FIELD, entryKind, qualifiedName, modifiers, fileNumber);
            indexEntry.setNameOffset(fileLoc.getNodeOffset(), fileLoc.getNodeLength(), IIndex.OFFSET);

            indexEntry.serialize(indexer.getOutput());
        }
        else if (binding instanceof IVariable &&
                !(binding instanceof IParameter)) { 
            // exclude local variables
            IScope definingScope = binding.getScope();
            if (definingScope == name.getTranslationUnit().getScope()) {
                int modifiers = 0;
                if (entryKind != IIndex.REFERENCE) {
                    modifiers = IndexVisitorUtil.getModifiers(name, binding);
                }
                NamedEntry indexEntry = new NamedEntry(IIndex.VAR, entryKind, qualifiedName, modifiers, fileNumber);
                indexEntry.setNameOffset(fileLoc.getNodeOffset(), fileLoc.getNodeLength(), IIndex.OFFSET);
    
                indexEntry.serialize(indexer.getOutput());
            }
        }
        else if (binding instanceof IFunction) {
            int modifiers = 0;
            if (entryKind != IIndex.REFERENCE) {
                modifiers = IndexVisitorUtil.getModifiers(name, binding);
            }
            FunctionEntry indexEntry = new FunctionEntry(IIndex.FUNCTION, entryKind, qualifiedName, modifiers, fileNumber);
            indexEntry.setNameOffset(fileLoc.getNodeOffset(), fileLoc.getNodeLength(), IIndex.OFFSET);
            indexEntry.setSignature(IndexVisitorUtil.getParameters(name));
            indexEntry.setReturnType(IndexVisitorUtil.getReturnType((IFunction) binding));

            indexEntry.serialize(indexer.getOutput());
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
