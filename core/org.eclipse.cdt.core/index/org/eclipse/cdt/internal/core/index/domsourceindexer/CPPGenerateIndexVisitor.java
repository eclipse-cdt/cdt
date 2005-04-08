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
import org.eclipse.cdt.core.dom.ast.cpp.CPPASTVisitor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPDelegate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.search.ICSearchConstants;
import org.eclipse.cdt.core.search.ICSearchConstants.LimitTo;
import org.eclipse.cdt.internal.core.index.cindexstorage.ICIndexStorageConstants;
import org.eclipse.cdt.internal.core.search.indexing.IIndexEncodingConstants;
import org.eclipse.cdt.internal.core.search.indexing.IIndexEncodingConstants.EntryType;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;

public class CPPGenerateIndexVisitor extends CPPASTVisitor {
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

//        shouldVisitBaseSpecifiers = false;
//        shouldVisitNamespaces     = false;
//        shouldVisitTemplateParameters = false;
    }
    
    public CPPGenerateIndexVisitor(DOMSourceIndexerRunner indexer, IFile resourceFile) {
        super();
        this.indexer = indexer;
        this.resourceFile = resourceFile;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.ASTVisitor#visit(org.eclipse.cdt.core.dom.ast.IASTName)
     */
    public int visit(IASTName name) {
        // qualified names are going to be handled segment by segment 
        if (name instanceof ICPPASTQualifiedName) return PROCESS_CONTINUE;
        
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
        if (indexer.areProblemMarkersEnabled() && indexer.shouldRecordProblem(problem)) {
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
    
            processNameBinding(name, binding, loc, indexFlag, null); // function will determine limitTo
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
     * @param limitTo 
     * @throws DOMException
     */
    private void processNameBinding(IASTName name, IBinding binding, IASTFileLocation loc, int fileNumber, LimitTo limitTo) throws DOMException {
        // determine LimitTo
        if (limitTo == null) {
            if (name.isDeclaration()) {
                limitTo = ICSearchConstants.DECLARATIONS;
            }
            else if (name.isReference()) {
                limitTo = ICSearchConstants.REFERENCES;
            }
            else {
                limitTo = ICSearchConstants.UNKNOWN_LIMIT_TO;
            }
        }
        
        // determine type
        EntryType entryType = null;
        if (binding instanceof ICompositeType) {
            ICompositeType compBinding = (ICompositeType) binding;
            int compositeKey = compBinding.getKey();
            ASTNodeProperty prop = name.getPropertyInParent();
            switch (compositeKey) {
                case ICPPClassType.k_class:
                    entryType = IIndexEncodingConstants.CLASS;
                    if (name.isDeclaration() && prop == IASTElaboratedTypeSpecifier.TYPE_NAME)
                        entryType = IIndexEncodingConstants.FWD_CLASS;
                    break;
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
            addDerivedAndFriendDeclaration(name, compBinding, loc, fileNumber);
        }
        else if (binding instanceof IEnumeration)
            entryType = IIndexEncodingConstants.ENUM;
        else if (binding instanceof ITypedef)
            entryType = IIndexEncodingConstants.TYPEDEF;
        else if (binding instanceof ICPPNamespace)
            entryType = IIndexEncodingConstants.NAMESPACE;
        else if (binding instanceof IEnumerator)
            entryType = IIndexEncodingConstants.ENUMERATOR;
        else if (binding instanceof IField) 
            entryType = IIndexEncodingConstants.FIELD;
        else if (binding instanceof IParameter ||
                 binding instanceof IVariable) 
            entryType = IIndexEncodingConstants.VAR;
        else if (binding instanceof ICPPMethod)
            entryType = IIndexEncodingConstants.METHOD;
        else if (binding instanceof IFunction) {
            entryType = IIndexEncodingConstants.FUNCTION;
            // TODO In case we want to add friend function declarations to index
            // addDerivedAndFriendDeclaration(name, binding, loc, fileNumber);
        }
        else if (binding instanceof ICPPUsingDeclaration) {
            ICPPDelegate[] delegates = ((ICPPUsingDeclaration)binding).getDelegates();
            for (int i = 0; i < delegates.length; i++) {
                IBinding orig = delegates[i].getBinding();
                processNameBinding(name, orig, loc, fileNumber, ICSearchConstants.REFERENCES); // reference to the original binding
                processNameBinding(name, delegates[i], loc, fileNumber, ICSearchConstants.DECLARATIONS); // declaration of the new name
            }
            return;
        }
        
        if (entryType != null && limitTo != null) {
            indexer.getOutput().addRef(fileNumber, IndexEncoderUtil.encodeEntry(
                        getFullyQualifiedName(binding),
                        entryType,
                        limitTo),
                    loc.getNodeOffset(),
                    loc.getNodeLength(),
                    ICIndexStorageConstants.OFFSET);
        }
    }

    /**
     * @param name
     * @param fileNumber 
     * @param loc 
     * @param compBinding
     * @throws DOMException 
     */
    private void addDerivedAndFriendDeclaration(IASTName name, IBinding binding, IASTFileLocation loc, int fileNumber) throws DOMException {
        ASTNodeProperty prop = name.getPropertyInParent();
        if (binding instanceof ICompositeType) {
            ICompositeType compBinding = (ICompositeType) binding;
            int compositeKey = compBinding.getKey();
            if (compositeKey == ICPPClassType.k_class ||
                    compositeKey == ICompositeType.k_struct) {
                if (prop == ICPPASTBaseSpecifier.NAME) {
                    // base class
                    indexer.getOutput().addRef(fileNumber, IndexEncoderUtil.encodeEntry(
                                getFullyQualifiedName(binding),
                                IIndexEncodingConstants.DERIVED,
                                ICSearchConstants.DECLARATIONS),
                            loc.getNodeOffset(),
                            loc.getNodeLength(),
                            ICIndexStorageConstants.OFFSET);
                }
                else if (prop == IASTElaboratedTypeSpecifier.TYPE_NAME) {
                    // friend 
                    indexer.getOutput().addRef(fileNumber, IndexEncoderUtil.encodeEntry(
                                getFullyQualifiedName(binding),
                                IIndexEncodingConstants.FRIEND,
                                ICSearchConstants.DECLARATIONS),
                            loc.getNodeOffset(),
                            loc.getNodeLength(),
                            ICIndexStorageConstants.OFFSET);
                }
            }
        }
        // TODO In case we want to add friend function declarations to index
//        else if (binding instanceof IFunction) {
//            IFunction funBinding = (IFunction) binding;
//            if (prop == IASTFunctionDeclarator.DECLARATOR_NAME) {
//                IASTFunctionDeclarator fDecl = (IASTFunctionDeclarator) name.getParent();
//                IASTNode fDeclParent = fDecl.getParent();
//                if (fDeclParent instanceof IASTSimpleDeclaration) {
//                    IASTSimpleDeclaration sDecl = (IASTSimpleDeclaration) fDeclParent;
//                    IASTDeclSpecifier declSpec = sDecl.getDeclSpecifier();
//                    if (declSpec instanceof ICPPASTSimpleDeclSpecifier) {
//                        ICPPASTSimpleDeclSpecifier fDeclSpec = (ICPPASTSimpleDeclSpecifier) declSpec;
//                        if (fDeclSpec.isFriend()) {
//                            // friend 
//                            indexer.getOutput().addRef(fileNumber, IndexEncoderUtil.encodeEntry(
//                                        getFullyQualifiedName(binding),
//                                        IIndexEncodingConstants.FRIEND,
//                                        ICSearchConstants.DECLARATIONS));
//                        }
//                    }
//                }
//            }
//        }
    }

    /**
     * @param binding
     * @return
     */
    private char[][] getFullyQualifiedName(IBinding binding) {
        try {
            if (binding instanceof ICPPBinding) {
                return ((ICPPBinding) binding).getQualifiedNameCharArray();
            }
        }
        catch (DOMException e) {
        }
        return new char[][] {binding.getNameCharArray()};
    }

}
