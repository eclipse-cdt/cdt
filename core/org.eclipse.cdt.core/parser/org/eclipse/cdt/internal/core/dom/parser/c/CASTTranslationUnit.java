/**********************************************************************
 * Copyright (c) 2002-2004 IBM Canada and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation */
package org.eclipse.cdt.internal.core.dom.parser.c;


import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.internal.core.dom.parser.IRequiresLocationInformation;
import org.eclipse.cdt.internal.core.parser.scanner2.ILocationResolver;

/**
 * @author jcamelon
 */
public class CASTTranslationUnit extends CASTNode implements IASTTranslationUnit, IRequiresLocationInformation {


    private IASTDeclaration [] decls = null;
    private static final int DEFAULT_CHILDREN_LIST_SIZE = 8;
    private int currentIndex = 0;
    
    //Binding
    private CScope compilationUnit = null;
    private ILocationResolver resolver;
    private static final IASTPreprocessorStatement[] EMPTY_PREPROCESSOR_STATEMENT_ARRAY = new IASTPreprocessorStatement[0];
    private static final IASTNodeLocation[] EMPTY_PREPROCESSOR_LOCATION_ARRAY = new IASTNodeLocation[0];
    private static final IASTPreprocessorMacroDefinition[] EMPTY_PREPROCESSOR_MACRODEF_ARRAY = new IASTPreprocessorMacroDefinition[0];
    private static final IASTPreprocessorIncludeStatement[] EMPTY_PREPROCESSOR_INCLUSION_ARRAY = new IASTPreprocessorIncludeStatement[0];
    private static final IASTProblem[] EMPTY_PROBLEM_ARRAY = new IASTProblem[0];
    private static final String EMPTY_STRING = ""; //$NON-NLS-1$
    
    public void addDeclaration( IASTDeclaration d )
    {
        if( decls == null )
        {
            decls = new IASTDeclaration[ DEFAULT_CHILDREN_LIST_SIZE ];
            currentIndex = 0;
        }
        if( decls.length == currentIndex )
        {
            IASTDeclaration [] old = decls;
            decls = new IASTDeclaration[ old.length * 2 ];
            for( int i = 0; i < old.length; ++i )
                decls[i] = old[i];
        }
        decls[ currentIndex++ ] = d;
    }

    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTTranslationUnit#getDeclarations()
     */
    public IASTDeclaration[] getDeclarations() {
        if( decls == null ) return IASTDeclaration.EMPTY_DECLARATION_ARRAY;
        removeNullDeclarations();
        return decls;
    }

    /**
     * @param decls2
     */
    private void removeNullDeclarations() {
        int nullCount = 0; 
        for( int i = 0; i < decls.length; ++i )
            if( decls[i] == null )
                ++nullCount;
        if( nullCount == 0 ) return;
        IASTDeclaration [] old = decls;
        int newSize = old.length - nullCount;
        decls = new IASTDeclaration[ newSize ];
        for( int i = 0; i < newSize; ++i )
            decls[i] = old[i];
        currentIndex = newSize;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTTranslationUnit#getScope()
     */
    public IScope getScope() {
    	if( compilationUnit == null )
    		compilationUnit = new CScope( this );
        return compilationUnit;
    }
    
    /* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IASTTranslationUnit#getDeclarations(org.eclipse.cdt.core.dom.ast.IBinding)
	 */
	public IASTName[] getDeclarations(IBinding binding) {
	    //TODO if binding is macro, circumvent the visitor
		return CVisitor.getDeclarations(this, binding);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IASTTranslationUnit#getReferences(org.eclipse.cdt.core.dom.ast.IBinding)
	 */
	public IASTName[] getReferences(IBinding binding) {
	    //TODO if binding is macro, circumvent the visitor
		return CVisitor.getReferences( this, binding );
	}



    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTTranslationUnit#getLocationInfo(int, int)
     */
    public IASTNodeLocation[] getLocationInfo(int offset, int length) {
        if( resolver == null ) return EMPTY_PREPROCESSOR_LOCATION_ARRAY;
        return resolver.getLocations(offset,length);
    }


    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTTranslationUnit#getNodeForLocation(org.eclipse.cdt.core.dom.ast.IASTNodeLocation)
     */
    public IASTNode[] selectNodesForLocation(String path, int offset, int length) {
        // TODO Auto-generated method stub
        return null;
    }


    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTTranslationUnit#getMacroDefinitions()
     */
    public IASTPreprocessorMacroDefinition[] getMacroDefinitions() {
        if( resolver == null ) return EMPTY_PREPROCESSOR_MACRODEF_ARRAY;
        IASTPreprocessorMacroDefinition [] result = resolver.getMacroDefinitions();
        setParentRelationship( result, IASTTranslationUnit.PREPROCESSOR_STATEMENT );
        return result;
    }


    /* (non-Javadoc)
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


   /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTTranslationUnit#getAllPreprocessorStatements()
     */
    public IASTPreprocessorStatement[] getAllPreprocessorStatements() {
        if( resolver == null ) return EMPTY_PREPROCESSOR_STATEMENT_ARRAY;
        IASTPreprocessorStatement [] result = resolver.getAllPreprocessorStatements();
        setParentRelationship( result, IASTTranslationUnit.PREPROCESSOR_STATEMENT );
        return result;
    }


    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser2.IRequiresLocationInformation#setLocationResolver(org.eclipse.cdt.internal.core.parser.scanner2.ILocationResolver)
     */
    public void setLocationResolver(ILocationResolver resolver) {
        this.resolver = resolver;
    }


    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTTranslationUnit#selectNodesForLocation(int, int)
     */
    public IASTNode[] selectNodesForLocation(int offset, int length) {
        return selectNodesForLocation( "", offset, length ); //$NON-NLS-1$
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#finalize()
     */
    protected void finalize() throws Throwable {
        if( resolver != null ) resolver.cleanup();
        super.finalize();
    }


    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTTranslationUnit#getPreprocesorProblems()
     */
    public IASTProblem[] getPreprocesorProblems() {
        if( resolver == null ) return EMPTY_PROBLEM_ARRAY;
        IASTProblem [] result = resolver.getScannerProblems();
        for( int i = 0; i < result.length; ++i )
        {
            IASTProblem p = result[i];
            p.setParent( this );
            p.setPropertyInParent( IASTTranslationUnit.SCANNER_PROBLEM );
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
