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

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;
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
import org.eclipse.cdt.core.search.ICSearchConstants;
import org.eclipse.cdt.internal.core.search.indexing.IIndexEncodingConstants;
import org.eclipse.cdt.internal.core.search.indexing.IIndexEncodingConstants.EntryType;
import org.eclipse.core.resources.IFile;

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
        problems.add(problem);
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
            problems.add(problem);
            if (indexer.isProblemReportingEnabled()) {
                // TODO report problem
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
    private void processNameBinding(IASTName name, IBinding binding, int indexFlag) throws DOMException {
        // determine type
        EntryType entryType = null;
        if (binding instanceof ICompositeType) {
            int compositeKey = ((ICompositeType) binding).getKey();
            ASTNodeProperty prop = name.getPropertyInParent();
            switch (compositeKey) {
                case ICompositeType.k_struct:
                    if (prop == IASTElaboratedTypeSpecifier.TYPE_NAME)
                        entryType = IIndexEncodingConstants.FWD_STRUCT;
                    else if (prop == IASTCompositeTypeSpecifier.TYPE_NAME)
                        entryType = IIndexEncodingConstants.STRUCT;
                    break;
                case ICompositeType.k_union:
                    if (prop == IASTElaboratedTypeSpecifier.TYPE_NAME)
                        entryType = IIndexEncodingConstants.FWD_UNION;
                    else if (prop == IASTCompositeTypeSpecifier.TYPE_NAME)
                        entryType = IIndexEncodingConstants.UNION;
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
                indexer.getOutput().addRef(IndexEncoderUtil.encodeEntry(
                            getFullyQualifiedName(name),
                            entryType,
                            ICSearchConstants.DECLARATIONS),
                        indexFlag);
            }                   
            else if (name.isReference()) {
                indexer.getOutput().addRef(IndexEncoderUtil.encodeEntry(
                            getFullyQualifiedName(name),
                            entryType,
                            ICSearchConstants.REFERENCES),
                        indexFlag);
            }
        }
    }

    /**
     * @param name
     * @return
     */
    private char[][] getFullyQualifiedName(IASTName name) {
        return new char[][] {name.toCharArray()};
    }

}
