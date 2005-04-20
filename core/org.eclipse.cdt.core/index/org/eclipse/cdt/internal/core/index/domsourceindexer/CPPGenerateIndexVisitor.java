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
import org.eclipse.cdt.internal.core.index.cindexstorage.ICIndexStorageConstants;
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
    
            processNameBinding(name, binding, loc, indexFlag); // function will determine Ref or Decl
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
    
    private void processNameDeclBinding(IASTName name, IBinding binding, IASTFileLocation loc, int fileNumber) throws DOMException {
    	if (binding instanceof ICompositeType) {
            ICompositeType compBinding = (ICompositeType) binding;
            int compositeKey = compBinding.getKey();
            ASTNodeProperty prop = name.getPropertyInParent();
            switch (compositeKey) {
                case ICPPClassType.k_class:                 
                    if (name.isDeclaration() && prop == IASTElaboratedTypeSpecifier.TYPE_NAME)
                        indexer.getOutput().addFwd_ClassDecl(fileNumber, getFullyQualifiedName(binding), loc.getNodeOffset(), loc.getNodeLength(),ICIndexStorageConstants.OFFSET);
                    else
                        indexer.getOutput().addClassDecl(fileNumber, getFullyQualifiedName(binding), loc.getNodeOffset(), loc.getNodeLength(),ICIndexStorageConstants.OFFSET);
                    break;
                case ICompositeType.k_struct:                   
                    if (name.isDeclaration() && prop == IASTElaboratedTypeSpecifier.TYPE_NAME)
                        indexer.getOutput().addFwd_StructDecl(fileNumber, getFullyQualifiedName(binding), loc.getNodeOffset(), loc.getNodeLength(),ICIndexStorageConstants.OFFSET);
                    else
                        indexer.getOutput().addStructDecl(fileNumber, getFullyQualifiedName(binding), loc.getNodeOffset(), loc.getNodeLength(),ICIndexStorageConstants.OFFSET);
                    break;
                case ICompositeType.k_union:                   
                    if (name.isDeclaration() && prop == IASTElaboratedTypeSpecifier.TYPE_NAME)
                        indexer.getOutput().addFwd_UnionDecl(fileNumber, getFullyQualifiedName(binding), loc.getNodeOffset(), loc.getNodeLength(),ICIndexStorageConstants.OFFSET);
                    else
                        indexer.getOutput().addUnionDecl(fileNumber, getFullyQualifiedName(binding), loc.getNodeOffset(), loc.getNodeLength(),ICIndexStorageConstants.OFFSET);
                    break;
            }
            addDerivedAndFriendDeclaration(name, compBinding, loc, fileNumber);
        }
        else if (binding instanceof IEnumeration)
        	 indexer.getOutput().addEnumDecl(fileNumber, getFullyQualifiedName(binding), loc.getNodeOffset(), loc.getNodeLength(),ICIndexStorageConstants.OFFSET);
        else if (binding instanceof ITypedef)
        	 indexer.getOutput().addTypedefDecl(fileNumber, getFullyQualifiedName(binding), loc.getNodeOffset(), loc.getNodeLength(),ICIndexStorageConstants.OFFSET);
        else if (binding instanceof ICPPNamespace)
        	 indexer.getOutput().addNamespaceDecl(fileNumber, getFullyQualifiedName(binding), loc.getNodeOffset(), loc.getNodeLength(),ICIndexStorageConstants.OFFSET);
        else if (binding instanceof IEnumerator)
        	 indexer.getOutput().addEnumtorDecl(fileNumber, getFullyQualifiedName(binding), loc.getNodeOffset(), loc.getNodeLength(),ICIndexStorageConstants.OFFSET);
        else if (binding instanceof IField) 
        	 indexer.getOutput().addFieldDecl(fileNumber, getFullyQualifiedName(binding), loc.getNodeOffset(), loc.getNodeLength(),ICIndexStorageConstants.OFFSET);
        else if (binding instanceof IParameter ||
                 binding instanceof IVariable) 
        	 indexer.getOutput().addVarDecl(fileNumber, getFullyQualifiedName(binding), loc.getNodeOffset(), loc.getNodeLength(),ICIndexStorageConstants.OFFSET);
        else if (binding instanceof ICPPMethod)
        	 indexer.getOutput().addMethodDecl(fileNumber, getFullyQualifiedName(binding), loc.getNodeOffset(), loc.getNodeLength(),ICIndexStorageConstants.OFFSET);
        else if (binding instanceof IFunction) {
        	 indexer.getOutput().addFunctionDecl(fileNumber, getFullyQualifiedName(binding), loc.getNodeOffset(), loc.getNodeLength(),ICIndexStorageConstants.OFFSET);
            // TODO In case we want to add friend function declarations to index
            // addDerivedAndFriendDeclaration(name, binding, loc, fileNumber);
        }
        else if (binding instanceof ICPPUsingDeclaration) {
            ICPPDelegate[] delegates = ((ICPPUsingDeclaration)binding).getDelegates();
            for (int i = 0; i < delegates.length; i++) {
                IBinding orig = delegates[i].getBinding();
                processNameRefBinding(name, orig, loc, fileNumber); // reference to the original binding
                processNameDeclBinding(name, delegates[i], loc, fileNumber); // declaration of the new name
            }
            return;
        }
    }
    
    private void processNameRefBinding(IASTName name, IBinding binding, IASTFileLocation loc, int fileNumber) throws DOMException {
    	if (binding instanceof ICompositeType) {
            ICompositeType compBinding = (ICompositeType) binding;
            int compositeKey = compBinding.getKey();
            ASTNodeProperty prop = name.getPropertyInParent();
            switch (compositeKey) {
                case ICPPClassType.k_class:                 
                    if (name.isDeclaration() && prop == IASTElaboratedTypeSpecifier.TYPE_NAME)
                        indexer.getOutput().addFwd_ClassRef(fileNumber, getFullyQualifiedName(binding), loc.getNodeOffset(), loc.getNodeLength(),ICIndexStorageConstants.OFFSET);
                    else
                        indexer.getOutput().addClassRef(fileNumber, getFullyQualifiedName(binding), loc.getNodeOffset(), loc.getNodeLength(),ICIndexStorageConstants.OFFSET);
                    break;
                case ICompositeType.k_struct:                   
                    if (name.isDeclaration() && prop == IASTElaboratedTypeSpecifier.TYPE_NAME)
                        indexer.getOutput().addFwd_StructRef(fileNumber, getFullyQualifiedName(binding), loc.getNodeOffset(), loc.getNodeLength(),ICIndexStorageConstants.OFFSET);
                    else
                        indexer.getOutput().addStructRef(fileNumber, getFullyQualifiedName(binding), loc.getNodeOffset(), loc.getNodeLength(),ICIndexStorageConstants.OFFSET);
                    break;
                case ICompositeType.k_union:                   
                    if (name.isDeclaration() && prop == IASTElaboratedTypeSpecifier.TYPE_NAME)
                        indexer.getOutput().addFwd_UnionRef(fileNumber, getFullyQualifiedName(binding), loc.getNodeOffset(), loc.getNodeLength(),ICIndexStorageConstants.OFFSET);
                    else
                        indexer.getOutput().addUnionRef(fileNumber, getFullyQualifiedName(binding), loc.getNodeOffset(), loc.getNodeLength(),ICIndexStorageConstants.OFFSET);
                    break;
            }
            addDerivedAndFriendDeclaration(name, compBinding, loc, fileNumber);
        }
        else if (binding instanceof IEnumeration)
        	 indexer.getOutput().addEnumRef(fileNumber, getFullyQualifiedName(binding), loc.getNodeOffset(), loc.getNodeLength(),ICIndexStorageConstants.OFFSET);
        else if (binding instanceof ITypedef)
        	 indexer.getOutput().addTypedefRef(fileNumber, getFullyQualifiedName(binding), loc.getNodeOffset(), loc.getNodeLength(),ICIndexStorageConstants.OFFSET);
        else if (binding instanceof ICPPNamespace)
        	 indexer.getOutput().addNamespaceRef(fileNumber, getFullyQualifiedName(binding), loc.getNodeOffset(), loc.getNodeLength(),ICIndexStorageConstants.OFFSET);
        else if (binding instanceof IEnumerator)
        	 indexer.getOutput().addEnumtorRef(fileNumber, getFullyQualifiedName(binding), loc.getNodeOffset(), loc.getNodeLength(),ICIndexStorageConstants.OFFSET);
        else if (binding instanceof IField) 
        	 indexer.getOutput().addFieldRef(fileNumber, getFullyQualifiedName(binding), loc.getNodeOffset(), loc.getNodeLength(),ICIndexStorageConstants.OFFSET);
        else if (binding instanceof IParameter ||
                 binding instanceof IVariable) 
        	 indexer.getOutput().addVarRef(fileNumber, getFullyQualifiedName(binding), loc.getNodeOffset(), loc.getNodeLength(),ICIndexStorageConstants.OFFSET);
        else if (binding instanceof ICPPMethod)
        	 indexer.getOutput().addMethodRef(fileNumber, getFullyQualifiedName(binding), loc.getNodeOffset(), loc.getNodeLength(),ICIndexStorageConstants.OFFSET);
        else if (binding instanceof IFunction) {
        	 indexer.getOutput().addFunctionRef(fileNumber, getFullyQualifiedName(binding), loc.getNodeOffset(), loc.getNodeLength(),ICIndexStorageConstants.OFFSET);
            // TODO In case we want to add friend function declarations to index
            // addDerivedAndFriendDeclaration(name, binding, loc, fileNumber);
        }
        else if (binding instanceof ICPPUsingDeclaration) {
            ICPPDelegate[] delegates = ((ICPPUsingDeclaration)binding).getDelegates();
            for (int i = 0; i < delegates.length; i++) {
                IBinding orig = delegates[i].getBinding();
                processNameRefBinding(name, orig, loc, fileNumber); // reference to the original binding
                processNameDeclBinding(name, delegates[i], loc, fileNumber); // declaration of the new name
            }
            return;
        }
    }
    private void processNameBinding(IASTName name, IBinding binding, IASTFileLocation loc, int fileNumber) throws DOMException {
            if (name.isDeclaration()) {
            	processNameDeclBinding(name, binding, loc, fileNumber);
            }
            else if (name.isReference()) {
            	processNameRefBinding(name, binding, loc, fileNumber);
            }           
//            else 
//            	ICSearchConstants.UNKNOWN_LIMIT_TO;
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
                    indexer.getOutput().addDerivedDecl(fileNumber, 
                            getFullyQualifiedName(binding),
                            loc.getNodeOffset(),
                            loc.getNodeLength(),
                            ICIndexStorageConstants.OFFSET);
                }
                else if (prop == IASTElaboratedTypeSpecifier.TYPE_NAME) {
                    // friend 
                    indexer.getOutput().addFriendDecl(fileNumber, 
                            getFullyQualifiedName(binding),
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
//                                        ICIndexStorageConstants.FRIEND,
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
