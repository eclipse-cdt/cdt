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
import org.eclipse.cdt.core.dom.ast.cpp.CPPASTVisitor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
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
import org.eclipse.cdt.internal.core.index.IIndex;
import org.eclipse.cdt.internal.core.index.domsourceindexer.IndexerOutputWrapper.EntryType;

public class CPPGenerateIndexVisitor extends CPPASTVisitor {
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

//        shouldVisitBaseSpecifiers = false;
//        shouldVisitNamespaces     = false;
//        shouldVisitTemplateParameters = false;
    }
    
    public CPPGenerateIndexVisitor(DOMSourceIndexerRunner indexer) {
        super();
        this.indexer = indexer;
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
    
            processNameBinding(name, binding, loc, indexFlag, null); // function will determine limitTo
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
                    entryType = IndexerOutputWrapper.CLASS;
                    if (name.isDeclaration() && prop == IASTElaboratedTypeSpecifier.TYPE_NAME)
                        entryType = IndexerOutputWrapper.FWD_CLASS;
                    break;
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
			addDerivedDeclaratiion(name, compBinding, loc, fileNumber);
	        if (isFriendDeclaration(name, binding)) {
				entryType = IndexerOutputWrapper.FRIEND; 
	        }
        }
        else if (binding instanceof IEnumeration)
            entryType = IndexerOutputWrapper.ENUM;
        else if (binding instanceof ITypedef)
            entryType = IndexerOutputWrapper.TYPEDEF;
        else if (binding instanceof ICPPNamespace)
            entryType = IndexerOutputWrapper.NAMESPACE;
        else if (binding instanceof IEnumerator)
            entryType = IndexerOutputWrapper.ENUMERATOR;
        else if (binding instanceof IField) 
            entryType = IndexerOutputWrapper.FIELD;
        else if (binding instanceof IParameter ||
                 binding instanceof IVariable) 
            entryType = IndexerOutputWrapper.VAR;
        else if (binding instanceof ICPPMethod) {
            entryType = IndexerOutputWrapper.METHOD;
            // TODO In case we want to add friend method declarations to index
//          if (isFriendDeclaration(name, binding)) {
//			    entryType = IndexerOutputWrapper.FRIEND; 
//          }
        }
        else if (binding instanceof IFunction) {
            entryType = IndexerOutputWrapper.FUNCTION;
            // TODO In case we want to add friend function declarations to index
//	        if (isFriendDeclaration(name, binding)) {
//				entryType = IndexerOutputWrapper.FRIEND; 
//	        }
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
        
        if (entryType != null) {
			if (limitTo == ICSearchConstants.DECLARATIONS) {
	            IndexerOutputWrapper.addNameDecl(indexer.getOutput(),
						getFullyQualifiedName(binding),
						entryType,
	                    fileNumber, 
	                    loc.getNodeOffset(),
	                    loc.getNodeLength(),
	                    IIndex.OFFSET);
			}
			else if (limitTo == ICSearchConstants.REFERENCES) {
	            IndexerOutputWrapper.addNameRef(indexer.getOutput(),
						getFullyQualifiedName(binding),
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
     * @param compBinding
     * @param loc
     * @param fileNumber
     * @throws DOMException
     */
	private void addDerivedDeclaratiion(IASTName name, ICompositeType compBinding, IASTFileLocation loc, int fileNumber) throws DOMException {
        ASTNodeProperty prop = name.getPropertyInParent();
        int compositeKey = compBinding.getKey();
        if (compositeKey == ICPPClassType.k_class || compositeKey == ICompositeType.k_struct) {
            if (prop == ICPPASTBaseSpecifier.NAME) {
                // base class
	            IndexerOutputWrapper.addNameDecl(indexer.getOutput(), getFullyQualifiedName(compBinding),
						IndexerOutputWrapper.DERIVED,
	                    fileNumber, 
	                    loc.getNodeOffset(),
	                    loc.getNodeLength(),
	                    IIndex.OFFSET);
            }
        }
	}

	/**
     * @param name
     * @param fileNumber 
     * @param loc 
     * @param binding
     * @throws DOMException 
     */
    private boolean isFriendDeclaration(IASTName name, IBinding binding) throws DOMException {
		boolean rc = false;
		if (!name.isDeclaration())
			return rc;
        ASTNodeProperty prop = name.getPropertyInParent();
        if (binding instanceof ICompositeType) {
            ICompositeType compBinding = (ICompositeType) binding;
            int compositeKey = compBinding.getKey();
            if (compositeKey == ICPPClassType.k_class || compositeKey == ICompositeType.k_struct) {
                if (prop == IASTElaboratedTypeSpecifier.TYPE_NAME) {
					IASTElaboratedTypeSpecifier elaboratedTypeSpec = (IASTElaboratedTypeSpecifier) name.getParent();
					if (elaboratedTypeSpec instanceof ICPPASTDeclSpecifier) {
						ICPPASTDeclSpecifier cppDeclSpec = (ICPPASTDeclSpecifier) elaboratedTypeSpec;
						rc = cppDeclSpec.isFriend();
					}
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
//                        rc = fDeclSpec.isFriend();
//                    }
//                }
//            }
//        }
		return rc;
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
