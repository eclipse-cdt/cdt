/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.internal.core.dom.parser.IRequiresLocationInformation;
import org.eclipse.cdt.internal.core.parser.scanner2.ILocationResolver;

/**
 * @author jcamelon
 */
public class CPPASTTranslationUnit extends CPPASTNode implements
        ICPPASTTranslationUnit, IRequiresLocationInformation {
    private IASTDeclaration[] decls = null;

    private ICPPNamespace binding = null;

    private ICPPScope scope = null;

    private static final int DEFAULT_CHILDREN_LIST_SIZE = 8;

    private int currentIndex = 0;

    private ILocationResolver resolver;

    private static final IASTNode[] EMPTY_NODE_ARRAY = new IASTNode[0];

    private static final IASTPreprocessorStatement[] EMPTY_PREPROCESSOR_STATEMENT_ARRAY = new IASTPreprocessorStatement[0];

    private static final IASTNodeLocation[] EMPTY_PREPROCESSOR_LOCATION_ARRAY = new IASTNodeLocation[0];

    private static final IASTPreprocessorMacroDefinition[] EMPTY_PREPROCESSOR_MACRODEF_ARRAY = new IASTPreprocessorMacroDefinition[0];

    private static final IASTPreprocessorIncludeStatement[] EMPTY_PREPROCESSOR_INCLUSION_ARRAY = new IASTPreprocessorIncludeStatement[0];
    private static final String EMPTY_STRING = ""; //$NON-NLS-1$
    private static final IASTProblem[] EMPTY_PROBLEM_ARRAY = new IASTProblem[0];

    public void addDeclaration(IASTDeclaration d) {
        if (decls == null) {
            decls = new IASTDeclaration[DEFAULT_CHILDREN_LIST_SIZE];
            currentIndex = 0;
        }
        if (decls.length == currentIndex) {
            IASTDeclaration[] old = decls;
            decls = new IASTDeclaration[old.length * 2];
            for (int i = 0; i < old.length; ++i)
                decls[i] = old[i];
        }
        decls[currentIndex++] = d;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.core.dom.ast.IASTTranslationUnit#getDeclarations()
     */
    public IASTDeclaration[] getDeclarations() {
        if (decls == null)
            return IASTDeclaration.EMPTY_DECLARATION_ARRAY;
        removeNullDeclarations();
        return decls;
    }

    /**
     * @param decls2
     */
    private void removeNullDeclarations() {
        int nullCount = 0;
        for (int i = 0; i < decls.length; ++i)
            if (decls[i] == null)
                ++nullCount;
        if (nullCount == 0)
            return;
        IASTDeclaration[] old = decls;
        int newSize = old.length - nullCount;
        decls = new IASTDeclaration[newSize];
        for (int i = 0; i < newSize; ++i)
            decls[i] = old[i];
        currentIndex = newSize;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.core.dom.ast.IASTTranslationUnit#getScope()
     */
    public IScope getScope() {
        if (scope == null)
            scope = new CPPNamespaceScope(this);
        return scope;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.core.dom.ast.IASTTranslationUnit#getDeclarations(org.eclipse.cdt.core.dom.ast.IBinding)
     */
    public IASTName[] getDeclarations(IBinding b) {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.core.dom.ast.IASTTranslationUnit#getReferences(org.eclipse.cdt.core.dom.ast.IBinding)
     */
    public IASTName[] getReferences(IBinding b) {
    	return CPPVisitor.getReferences(this, b);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.core.dom.ast.IASTTranslationUnit#getLocationInfo(int,
     *      int)
     */
    public IASTNodeLocation[] getLocationInfo(int offset, int length) {
        if (resolver == null)
            return EMPTY_PREPROCESSOR_LOCATION_ARRAY;
        return resolver.getLocations(offset, length);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.core.dom.ast.IASTTranslationUnit#getNodeForLocation(org.eclipse.cdt.core.dom.ast.IASTNodeLocation)
     */
    public IASTNode[] selectNodesForLocation(String path, int offset, int length) {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.core.dom.ast.IASTTranslationUnit#getMacroDefinitions()
     */
    public IASTPreprocessorMacroDefinition[] getMacroDefinitions() {
       if( resolver == null ) return EMPTY_PREPROCESSOR_MACRODEF_ARRAY;
       IASTPreprocessorMacroDefinition [] result = resolver.getMacroDefinitions();
       setParentRelationship( result, IASTTranslationUnit.PREPROCESSOR_STATEMENT );
       return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.core.dom.ast.IASTTranslationUnit#getIncludeDirectives()
     */
    public IASTPreprocessorIncludeStatement[] getIncludeDirectives() {
       if( resolver == null ) return EMPTY_PREPROCESSOR_INCLUSION_ARRAY;
       IASTPreprocessorIncludeStatement [] result = resolver.getIncludeDirectives();
       setParentRelationship( result, IASTTranslationUnit.PREPROCESSOR_STATEMENT );
       return result;
    }

    /**
     * @param result
     * @param preprocessor_statement
     */
    protected void setParentRelationship(IASTNode[] result, ASTNodeProperty property ) {
       for( int i = 0; i < result.length; ++i )
       {
          result[i].setParent( this );
          result[i].setPropertyInParent( property );
       }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.core.dom.ast.IASTTranslationUnit#getAllPreprocessorStatements()
     */
    public IASTPreprocessorStatement[] getAllPreprocessorStatements() {
        if (resolver == null)
            return EMPTY_PREPROCESSOR_STATEMENT_ARRAY;
        return resolver.getAllPreprocessorStatements();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.internal.core.parser2.IRequiresLocationInformation#setLocationResolver(org.eclipse.cdt.internal.core.parser.scanner2.ILocationResolver)
     */
    public void setLocationResolver(ILocationResolver resolver) {
        this.resolver = resolver;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.core.dom.ast.IASTTranslationUnit#selectNodesForLocation(int,
     *      int)
     */
    public IASTNode[] selectNodesForLocation(int offset, int length) {
        if (resolver == null)
            return EMPTY_NODE_ARRAY;
        return selectNodesForLocation(resolver.getTranslationUnitPath(),
                offset, length); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTranslationUnit#resolveBinding()
     */
    public IBinding resolveBinding() {
        if (binding == null)
            binding = new CPPNamespace(this);
        return binding;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.core.dom.ast.IASTTranslationUnit#getPreprocesorProblems()
     */
    public IASTProblem[] getPreprocesorProblems() {
        if (resolver == null)
            return EMPTY_PROBLEM_ARRAY;
        IASTProblem[] result = resolver.getScannerProblems();
        for (int i = 0; i < result.length; ++i) {
            IASTProblem p = result[i];
            p.setParent(this);
            p.setPropertyInParent(IASTTranslationUnit.SCANNER_PROBLEM);
        }
        return result;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTTranslationUnit#getUnpreprocessedSignature(org.eclipse.cdt.core.dom.ast.IASTNodeLocation[])
     */
    public String getUnpreprocessedSignature(IASTNodeLocation[] locations) {
       if( resolver == null ) return EMPTY_STRING;
       return new String( resolver.getUnpreprocessedSignature(locations) );
    }
}
