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

import org.eclipse.cdt.core.ICLogConstants;
import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
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
import org.eclipse.cdt.core.dom.ast.cpp.CPPASTVisitor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBlockScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPDelegate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.internal.core.index.FunctionEntry;
import org.eclipse.cdt.internal.core.index.IIndex;
import org.eclipse.cdt.internal.core.index.IIndexEntry;
import org.eclipse.cdt.internal.core.index.NamedEntry;
import org.eclipse.cdt.internal.core.index.TypeEntry;
import org.eclipse.cdt.internal.core.model.Util;

public class CPPGenerateIndexVisitor extends CPPASTVisitor {
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

//        shouldVisitBaseSpecifiers = false;
        shouldVisitNamespaces     = true;
//        shouldVisitTemplateParameters = false;
    }
    
    public CPPGenerateIndexVisitor(DOMSourceIndexerRunner indexer) {
        super();
        this.indexer = indexer;
    }

/* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.CPPASTVisitor#visit(org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition)
     */
    public int visit(ICPPASTNamespaceDefinition namespace) {
        if (IndexEncoderUtil.nodeInVisitedExternalHeader(namespace, indexer.getIndexer())) 
            return PROCESS_SKIP;
        return PROCESS_CONTINUE;
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
        if (name instanceof ICPPASTQualifiedName) return PROCESS_CONTINUE;
        
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
    
            processNameBinding(name, binding, loc, indexFlag, IIndex.UNKNOWN); // function will determine limitTo
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
    private void processNameBinding(IASTName name, IBinding binding, IASTFileLocation loc, int fileNumber, int entryKind) throws DOMException {
        char[][] qualifiedName = getFullyQualifiedName(binding);
        if (qualifiedName == null) 
            return;
        // determine entryKind
        if (entryKind == IIndex.UNKNOWN) {
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
        }
        
        IASTFileLocation fileLoc = IndexEncoderUtil.getFileLocation(name);
        
        // determine type
        if (binding instanceof ICompositeType) {
            int iEntryType = 0;
            ICompositeType compBinding = (ICompositeType) binding;
            int compositeKey = compBinding.getKey();
            ASTNodeProperty prop = name.getPropertyInParent();
            switch (compositeKey) {
                case ICPPClassType.k_class:
                    iEntryType = IIndex.TYPE_CLASS;
                    if (name.isDeclaration() && prop == IASTElaboratedTypeSpecifier.TYPE_NAME) {
                        iEntryType = IIndex.TYPE_CLASS;
                    }
                    break;
                case ICompositeType.k_struct:
                    iEntryType = IIndex.TYPE_STRUCT;
                    if (name.isDeclaration() && prop == IASTElaboratedTypeSpecifier.TYPE_NAME) {
                        iEntryType = IIndex.TYPE_STRUCT;
                    }
                    break;
                case ICompositeType.k_union:
                    iEntryType = IIndex.TYPE_UNION;
                    if (name.isDeclaration() && prop == IASTElaboratedTypeSpecifier.TYPE_NAME) {
                        iEntryType = IIndex.TYPE_UNION;
                    }
                    break;
            }
            int modifiers = 0;
            if (entryKind != IIndex.REFERENCE) {
                modifiers = IndexVisitorUtil.getModifiers(name, binding);
            }
            TypeEntry indexEntry = new TypeEntry(iEntryType, entryKind, qualifiedName, modifiers, fileNumber);
            indexEntry.setNameOffset(fileLoc.getNodeOffset(), fileLoc.getNodeLength(), IIndex.OFFSET);
            if (entryKind == IIndex.DEFINITION && binding instanceof ICPPClassType) {
                addDerivedDeclarations(name, (ICPPClassType)binding, indexEntry, fileNumber);
                addFriendDeclarations(name, (ICPPClassType)binding, indexEntry, fileNumber);
            }
            
            serialize(indexEntry);
        }
        else if (binding instanceof IEnumeration) {
            int modifiers = 0;
            if (entryKind != IIndex.REFERENCE) {
                modifiers = IndexVisitorUtil.getModifiers(name, binding);
            }
            TypeEntry indexEntry = new TypeEntry(IIndex.TYPE_ENUM, entryKind, qualifiedName, modifiers, fileNumber);
            indexEntry.setNameOffset(fileLoc.getNodeOffset(), fileLoc.getNodeLength(), IIndex.OFFSET);

            serialize(indexEntry);
        }
        else if (binding instanceof ITypedef) {
            TypeEntry indexEntry = new TypeEntry(IIndex.TYPE_TYPEDEF, entryKind, qualifiedName, 0, fileNumber);
            indexEntry.setNameOffset(fileLoc.getNodeOffset(), fileLoc.getNodeLength(), IIndex.OFFSET);

            serialize(indexEntry);
        }
        else if (binding instanceof ICPPNamespace) {
            NamedEntry indexEntry = new NamedEntry(IIndex.NAMESPACE, entryKind, qualifiedName, 0, fileNumber);
            indexEntry.setNameOffset(fileLoc.getNodeOffset(), fileLoc.getNodeLength(), IIndex.OFFSET);

            serialize(indexEntry);
        }
        else if (binding instanceof IEnumerator) {
            NamedEntry indexEntry = new NamedEntry(IIndex.ENUMTOR, entryKind, qualifiedName, 0, fileNumber);
            indexEntry.setNameOffset(fileLoc.getNodeOffset(), fileLoc.getNodeLength(), IIndex.OFFSET);

            serialize(indexEntry);
        }
        else if (binding instanceof IField) {
            int modifiers = 0;
            if (entryKind != IIndex.REFERENCE) {
                modifiers = IndexVisitorUtil.getModifiers(name, binding);
            }
            NamedEntry indexEntry = new NamedEntry(IIndex.FIELD, entryKind, qualifiedName, modifiers, fileNumber);
            indexEntry.setNameOffset(fileLoc.getNodeOffset(), fileLoc.getNodeLength(), IIndex.OFFSET);

            serialize(indexEntry);
        }
        else if (binding instanceof IVariable &&
                !(binding instanceof IParameter)) { 
            // exclude local variables
            IScope definingScope = binding.getScope();
            if (!(definingScope instanceof ICPPBlockScope)) {
                int modifiers = 0;
                if (entryKind != IIndex.REFERENCE) {
                    modifiers = IndexVisitorUtil.getModifiers(name, binding);
                }
                TypeEntry indexEntry = new TypeEntry(IIndex.TYPE_VAR, entryKind, qualifiedName, modifiers, fileNumber);
                indexEntry.setNameOffset(fileLoc.getNodeOffset(), fileLoc.getNodeLength(), IIndex.OFFSET);
    
                serialize(indexEntry);
            }
        }
        else if (binding instanceof ICPPMethod) {
            int modifiers = 0;
            if (entryKind != IIndex.REFERENCE) {
                modifiers = IndexVisitorUtil.getModifiers(name, binding);
            }
            FunctionEntry indexEntry = new FunctionEntry(IIndex.METHOD, entryKind, qualifiedName, modifiers, fileNumber);
            indexEntry.setNameOffset(fileLoc.getNodeOffset(), fileLoc.getNodeLength(), IIndex.OFFSET);
            indexEntry.setSignature(IndexVisitorUtil.getParameters((IFunction) binding));
            indexEntry.setReturnType(IndexVisitorUtil.getReturnType((IFunction) binding));

            serialize(indexEntry);
            // TODO In case we want to add friend method declarations to index
//          if (isFriendDeclaration(name, binding)) {
//			    entryType = IndexerOutputWrapper.FRIEND; 
//          }
        }
        else if (binding instanceof IFunction) {
            int modifiers = 0;
            if (entryKind != IIndex.REFERENCE) {
                modifiers = IndexVisitorUtil.getModifiers(name, binding);
            }
            FunctionEntry indexEntry = new FunctionEntry(IIndex.FUNCTION, entryKind, qualifiedName, modifiers, fileNumber);
            indexEntry.setNameOffset(fileLoc.getNodeOffset(), fileLoc.getNodeLength(), IIndex.OFFSET);
            indexEntry.setSignature(IndexVisitorUtil.getParameters((IFunction) binding));
            indexEntry.setReturnType(IndexVisitorUtil.getReturnType((IFunction) binding));

            serialize(indexEntry);
            // TODO In case we want to add friend function declarations to index
//	        if (isFriendDeclaration(name, binding)) {
//				entryType = IndexerOutputWrapper.FRIEND; 
//	        }
        }
        else if (binding instanceof ICPPUsingDeclaration) {
            ICPPDelegate[] delegates = ((ICPPUsingDeclaration)binding).getDelegates();
            for (int i = 0; i < delegates.length; i++) {
                IBinding orig = delegates[i].getBinding();
                processNameBinding(name, orig, loc, fileNumber, IIndex.REFERENCE); // reference to the original binding
                processNameBinding(name, delegates[i], loc, fileNumber, IIndex.DECLARATION); // declaration of the new name
            }
            return;
        }
        
    }

    /**
     * @param indexEntry
     */
    private void serialize(IIndexEntry indexEntry) {
        indexEntry.serialize(indexer.getOutput());
    }

    /**
     * @param name
     * @param cppClassBinding
     * @param typeEntry
     * @param fileNumber
     * @throws DOMException 
     */
    private void addDerivedDeclarations(IASTName name, ICPPClassType cppClassBinding, TypeEntry typeEntry, int fileNumber) throws DOMException {
        List baseEntries = new ArrayList();
        ICPPBase[] baseClasses = cppClassBinding.getBases();
        for (int i = 0; i < baseClasses.length; i++) {
            ICPPBase base = baseClasses[i];
            ICPPClassType baseClass = baseClasses[i].getBaseClass();
            // skip problem bindings
            if (baseClass instanceof IProblemBinding) 
                continue;
            int modifiers = 0;
            int vis = base.getVisibility();
            if (vis == ICPPBase.v_public) {
                modifiers |= IIndex.publicAccessSpecifier;
            }
            else if (vis == ICPPBase.v_protected) {
                modifiers |= IIndex.protectedAccessSpecifier;
            }
            else if (vis == ICPPBase.v_private) {
                modifiers |= IIndex.privateAccessSpecifier;
            }
            if (base.isVirtual()) {
                modifiers |= IIndex.virtualSpecifier;
            }
            NamedEntry namedEntry = new NamedEntry(IIndex.TYPE_DERIVED, IIndex.DECLARATION, 
                    baseClass.getQualifiedNameCharArray(), modifiers, fileNumber);
            // tricky part - get the location of base specifier name
            IASTNode parent = name.getParent();
            if (parent instanceof ICPPASTCompositeTypeSpecifier) {
                ICPPASTCompositeTypeSpecifier typeParent = (ICPPASTCompositeTypeSpecifier) parent;
                ICPPASTBaseSpecifier[] baseSpecs = typeParent.getBaseSpecifiers();
                IASTFileLocation baseLoc = null;
                for (int j = 0; j < baseSpecs.length; j++) {
                    ICPPASTBaseSpecifier baseSpec = baseSpecs[j];
                    IASTName baseName = baseSpec.getName();
                    if (baseName.resolveBinding().equals(baseClass)) {
                        baseLoc = IndexEncoderUtil.getFileLocation(baseName);
                        break;
                    }
                }
                if (baseLoc != null) {
                    namedEntry.setNameOffset(baseLoc.getNodeOffset(), baseLoc.getNodeLength(), IIndex.OFFSET);
                    baseEntries.add(namedEntry);
                }
            }
        }
        if (baseEntries.size() > 0) {
            typeEntry.setBaseTypes((IIndexEntry[]) baseEntries.toArray(new IIndexEntry[baseEntries.size()]));
        }
    }

    /**
     * @param name
     * @param cppClassBinding
     * @param typeEntry
     * @param fileNumber
     */
    private void addFriendDeclarations(IASTName name, ICPPClassType cppClassBinding, TypeEntry typeEntry, int fileNumber) {
        IASTNode parent = name.getParent();
        if (parent instanceof ICPPASTCompositeTypeSpecifier) {
            ICPPASTCompositeTypeSpecifier parentClass = (ICPPASTCompositeTypeSpecifier) parent;
            List friendEntries = new ArrayList();
            IASTDeclaration[] members = parentClass.getMembers();
            for (int j = 0; j < members.length; j++) {
                IASTDeclaration decl = members[j];
                if (decl instanceof IASTSimpleDeclaration) {
                    IASTSimpleDeclaration simplDecl = (IASTSimpleDeclaration) decl;
                    IASTDeclSpecifier declSpec = simplDecl.getDeclSpecifier();
                    if (declSpec instanceof ICPPASTElaboratedTypeSpecifier) {
                        ICPPASTElaboratedTypeSpecifier elabTypeSpec = (ICPPASTElaboratedTypeSpecifier) declSpec;
                        if (elabTypeSpec.isFriend()) {
                            IASTName friendName = elabTypeSpec.getName();
                            
                            IBinding friend = friendName.resolveBinding();
                            if (friend != null && !(friend instanceof IProblemBinding)) {
                                int modifiers = IndexVisitorUtil.getModifiers(friendName, friend);
                                
                                NamedEntry namedEntry = new NamedEntry(IIndex.TYPE_FRIEND, IIndex.DECLARATION,
                                        getFullyQualifiedName(friend), modifiers, fileNumber);
                                IASTFileLocation fileLoc = IndexEncoderUtil.getFileLocation(friendName);
                                namedEntry.setNameOffset(fileLoc.getNodeOffset(), fileLoc.getNodeLength(), IIndex.OFFSET);
                                friendEntries.add(namedEntry);
                            }
                        }
                    }
                }
            }
            if (friendEntries.size() > 0) {
                typeEntry.setFriends((IIndexEntry[]) friendEntries.toArray(new IIndexEntry[friendEntries.size()]));
            }
        }
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
