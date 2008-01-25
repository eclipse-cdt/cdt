/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTArrayDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTComment;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IMacroBinding;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;
import org.eclipse.cdt.core.dom.ast.c.ICASTDesignator;
import org.eclipse.cdt.core.dom.ast.cpp.CPPASTVisitor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCatchHandler;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionTryBlockDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTOperatorName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBasicType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexFileSet;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.ASTInternal;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;
import org.eclipse.cdt.internal.core.dom.parser.GCCBuiltinSymbolProvider.CPPBuiltinParameter;
import org.eclipse.cdt.internal.core.parser.scanner.ILocationResolver;
import org.eclipse.core.runtime.CoreException;

/**
 * @author jcamelon
 */
public class CPPASTTranslationUnit extends CPPASTNode implements ICPPASTTranslationUnit, IASTAmbiguityParent {
    
    private static final IASTPreprocessorStatement[] EMPTY_PREPROCESSOR_STATEMENT_ARRAY = new IASTPreprocessorStatement[0];
    private static final IASTPreprocessorMacroDefinition[] EMPTY_PREPROCESSOR_MACRODEF_ARRAY = new IASTPreprocessorMacroDefinition[0];
    private static final IASTPreprocessorIncludeStatement[] EMPTY_PREPROCESSOR_INCLUSION_ARRAY = new IASTPreprocessorIncludeStatement[0];
    private static final String EMPTY_STRING = ""; //$NON-NLS-1$
    private static final IASTProblem[] EMPTY_PROBLEM_ARRAY = new IASTProblem[0];
    private static final IASTName[] EMPTY_NAME_ARRAY = new IASTName[0];

    private IASTDeclaration[] decls = new IASTDeclaration[32];
    private ICPPNamespace binding = null;
    private ICPPScope scope = null;
    private ILocationResolver resolver;
    private IIndex index;
    private IIndexFileSet fIndexFileSet;
	private boolean fIsHeader;
    
    public IASTTranslationUnit getTranslationUnit() {
    	return this;
    }
    
    public void addDeclaration(IASTDeclaration d) {
        decls = (IASTDeclaration [])ArrayUtil.append( IASTDeclaration.class, decls, d );
        if (d != null) {
			d.setParent(this);
			d.setPropertyInParent(OWNED_DECLARATION);
		}
    }


    public IASTDeclaration[] getDeclarations() {
        if (decls == null)
            return IASTDeclaration.EMPTY_DECLARATION_ARRAY;
        return (IASTDeclaration[]) ArrayUtil.trim( IASTDeclaration.class, decls );
    }

    public IScope getScope() {
        if (scope == null) {
            scope = new CPPNamespaceScope(this);
			addBuiltinOperators(scope);
        }
		
        return scope;
    }
	
	private void addBuiltinOperators(IScope theScope) {
        // void
        IType cpp_void = new CPPBasicType(IBasicType.t_void, 0);
        // void *
        IType cpp_void_p = new GPPPointerType(new CPPQualifierType(new CPPBasicType(IBasicType.t_void, 0), false, false), new GPPASTPointer());
        // size_t // assumed: unsigned long int
        IType cpp_size_t = new CPPBasicType(IBasicType.t_int, ICPPBasicType.IS_LONG & ICPPBasicType.IS_UNSIGNED);

		// void * operator new (std::size_t);
        IBinding temp = null;
        IType[] newParms = new IType[1];
        newParms[0] = cpp_size_t;
        IFunctionType newFunctionType = new CPPFunctionType(cpp_void_p, newParms);
        IParameter[] newTheParms = new IParameter[1];
        newTheParms[0] = new CPPBuiltinParameter(newParms[0]);
        temp = new CPPImplicitFunction(ICPPASTOperatorName.OPERATOR_NEW, theScope, newFunctionType, newTheParms, false);
        try {
        	ASTInternal.addBinding(theScope, temp);
        } catch (DOMException de) {}
		
		// void * operator new[] (std::size_t);
		temp = null;
        temp = new CPPImplicitFunction(ICPPASTOperatorName.OPERATOR_NEW_ARRAY, theScope, newFunctionType, newTheParms, false);
        try {
        	ASTInternal.addBinding(theScope, temp);
        } catch (DOMException de) {}
		
		// void operator delete(void*);
        temp = null;
        IType[] deleteParms = new IType[1];
        deleteParms[0] = cpp_void_p;
        IFunctionType deleteFunctionType = new CPPFunctionType(cpp_void, deleteParms);
        IParameter[] deleteTheParms = new IParameter[1];
        deleteTheParms[0] = new CPPBuiltinParameter(deleteParms[0]);
        temp = new CPPImplicitFunction(ICPPASTOperatorName.OPERATOR_DELETE, theScope, deleteFunctionType, deleteTheParms, false);
        try {
        	ASTInternal.addBinding(theScope, temp);
        } catch (DOMException de) {}
		
		// void operator delete[](void*);
		temp = null;
        temp = new CPPImplicitFunction(ICPPASTOperatorName.OPERATOR_DELETE_ARRAY, theScope, deleteFunctionType, deleteTheParms, false);
        try {
        	ASTInternal.addBinding(theScope, temp);
        } catch (DOMException de) {}
	}
	
    public IASTName[] getDeclarationsInAST(IBinding b) {
        if( b instanceof IMacroBinding )
        {
            if( resolver == null )
                return EMPTY_NAME_ARRAY;
            return resolver.getDeclarations( (IMacroBinding)b );
        }
        return CPPVisitor.getDeclarations( this, b );
    }

    public IName[] getDeclarations(IBinding b) {
        IName[] names = getDeclarationsInAST(b);
        if (names.length == 0 && index != null) {
        	try {
        		names = index.findDeclarations(b);
        	} catch (CoreException e) {
        		CCorePlugin.log(e);
        		return names;
        	}
        }
        
        return names;
    }

    public IASTName[] getDefinitionsInAST(IBinding binding) {
    	if (binding instanceof IMacroBinding) {
    		if( resolver == null )
    			return EMPTY_NAME_ARRAY;
    		return resolver.getDeclarations((IMacroBinding)binding);
        }
        
    	IASTName[] names = CPPVisitor.getDeclarations(this, binding);
        for (int i = 0; i < names.length; i++) {
            if (!names[i].isDefinition())
                names[i] = null;
        }
    	// nulls can be anywhere, don't use trim()
        return (IASTName[])ArrayUtil.removeNulls(IASTName.class, names);
    }

    public IName[] getDefinitions(IBinding binding) {
    	IName[] names = getDefinitionsInAST(binding);
        if (names.length == 0 && index != null) {
        	try {
        		names = index.findDefinitions(binding);
        	} catch (CoreException e) {
        		CCorePlugin.log(e);
        		return names;
        	}
        }
        
        return names;
    }


    public IASTName[] getReferences(IBinding b) {
        if( b instanceof IMacroBinding )
        {
            if( resolver == null )
        		  return EMPTY_NAME_ARRAY;
            return resolver.getReferences( (IMacroBinding)b );
        }
        return CPPVisitor.getReferences(this, b);
    }
    
    private class CPPFindNodeForOffsetAction extends CPPASTVisitor {
    	{
    		shouldVisitNames          = true;
    		shouldVisitDeclarations   = true;
    		shouldVisitInitializers   = true;
    		shouldVisitParameterDeclarations = true;
    		shouldVisitDeclarators    = true;
    		shouldVisitDeclSpecifiers = true;
    		shouldVisitExpressions    = true;
    		shouldVisitStatements     = true;
    		shouldVisitTypeIds        = true;
    		shouldVisitEnumerators    = true;
    		shouldVisitBaseSpecifiers = true;
    		shouldVisitNamespaces     = true;
    	}
    	
    	IASTNode foundNode = null;
    	int offset = 0;
    	int length = 0;
    	
    	/**
		 * 
		 */
		public CPPFindNodeForOffsetAction(int offset, int length) {
			this.offset = offset;
			this.length = length;
		}
    	
    	public int processNode(IASTNode node) {
    		if (foundNode != null)
    			return PROCESS_ABORT;
    		
    		if (node instanceof ASTNode &&
    				((ASTNode)node).getOffset() == offset &&
    				((ASTNode)node).getLength() == length) {
    			foundNode = node;
    			return PROCESS_ABORT;
    		}
    		
    		// skip the rest of this node if the selection is outside of its bounds
    		if (node instanceof ASTNode &&
    				offset > ((ASTNode)node).getOffset() + ((ASTNode)node).getLength())
    			return PROCESS_SKIP;
    		
    		return PROCESS_CONTINUE;
    	}
    	
 
    	public int visit(IASTDeclaration declaration) {
    		// use declarations to determine if the search has gone past the offset (i.e. don't know the order the visitor visits the nodes)
    		if (declaration instanceof ASTNode && ((ASTNode)declaration).getOffset() > offset)
    			return PROCESS_ABORT;
    		
    		return processNode(declaration);
    	}
    	

    	public int visit(IASTDeclarator declarator) {
    		int ret = processNode(declarator);
    		
    		IASTPointerOperator[] ops = declarator.getPointerOperators();
    		for(int i=0; i<ops.length; i++)
    			processNode(ops[i]);
    		
    		if (declarator instanceof IASTArrayDeclarator) {
    			IASTArrayModifier[] mods = ((IASTArrayDeclarator)declarator).getArrayModifiers();
    			for(int i=0; i<mods.length; i++)
    				processNode(mods[i]);
    		}
    		
    		if (declarator instanceof ICPPASTFunctionDeclarator) {
    			ICPPASTConstructorChainInitializer[] chainInit = ((ICPPASTFunctionDeclarator)declarator).getConstructorChain();
    			for(int i=0; i<chainInit.length; i++) {
    				processNode(chainInit[i]);
    			}
    			
    			if( declarator instanceof ICPPASTFunctionTryBlockDeclarator ){
    				ICPPASTCatchHandler [] catchHandlers = ((ICPPASTFunctionTryBlockDeclarator)declarator).getCatchHandlers();
    				for( int i = 0; i < catchHandlers.length; i++ ){
    					processNode(catchHandlers[i]);
    				}
    			}	
    		}
    		
    		return ret;
    	}
    	
 
    	public int processDesignator(ICASTDesignator designator) {
    		return processNode(designator);
    	}

    	public int visit(IASTDeclSpecifier declSpec) {
    		return processNode(declSpec);
    	}

    	public int visit(IASTEnumerator enumerator) {
    		return processNode(enumerator);
    	}

    	public int visit(IASTExpression expression) {
    		return processNode(expression);
    	}
 
    	public int visit(IASTInitializer initializer) {
    		return processNode(initializer);
    	}
    	
    	public int visit(IASTName name) {
    		if ( name.toString() != null )
    			return processNode(name);
    		return PROCESS_CONTINUE;
    	}
    	
    	public int visit(
    			IASTParameterDeclaration parameterDeclaration) {
    		return processNode(parameterDeclaration);
    	}
    	
    	public int visit(IASTStatement statement) {
    		return processNode(statement);
    	}
    	
    	public int visit(IASTTypeId typeId) {
    		return processNode(typeId);
    	}

    	public IASTNode getNode() {
    		return foundNode;
    	}
    }
    

    public IASTNode selectNodeForLocation(String path, int realOffset, int realLength) {
    	IASTNode result= null;
		if (resolver != null) {
	    	int start= resolver.getSequenceNumberForFileOffset(path, realOffset);
	    	if (start >= 0) {
	    		int length= realLength < 1 ? 0 : 
	    			resolver.getSequenceNumberForFileOffset(path, realOffset+realLength-1) + 1 - start;
	    		result= resolver.findSurroundingPreprocessorNode(start, length);
	    		if (result == null) {
	    			CPPFindNodeForOffsetAction nodeFinder = new CPPFindNodeForOffsetAction(start, length);
	    			accept(nodeFinder);
	    			result = nodeFinder.getNode();
	    		}
	    	}    	
		}
		return result;
    }


    public IASTPreprocessorMacroDefinition[] getMacroDefinitions() {
       if( resolver == null ) return EMPTY_PREPROCESSOR_MACRODEF_ARRAY;
       IASTPreprocessorMacroDefinition [] result = resolver.getMacroDefinitions();
       return result;
    }

    public IASTPreprocessorMacroDefinition[] getBuiltinMacroDefinitions() {
       if( resolver == null ) return EMPTY_PREPROCESSOR_MACRODEF_ARRAY;
       IASTPreprocessorMacroDefinition [] result = resolver.getBuiltinMacroDefinitions();
       return result;
    }

    public IASTPreprocessorIncludeStatement[] getIncludeDirectives() {
       if( resolver == null ) return EMPTY_PREPROCESSOR_INCLUSION_ARRAY;
       IASTPreprocessorIncludeStatement [] result = resolver.getIncludeDirectives();
       return result;
    }

    public IASTPreprocessorStatement[] getAllPreprocessorStatements() {
        if (resolver == null)
            return EMPTY_PREPROCESSOR_STATEMENT_ARRAY;
        IASTPreprocessorStatement [] result = resolver.getAllPreprocessorStatements();
        return result;
    }

    public void setLocationResolver(ILocationResolver resolver) {
        this.resolver = resolver;
        resolver.setRootNode( this );
    }

    public IBinding resolveBinding() {
        if (binding == null)
            binding = new CPPNamespace(this);
        return binding;
    }

    public IASTProblem[] getPreprocessorProblems() {
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

	public String getFilePath() {
		if (resolver == null)
			return EMPTY_STRING;
		return new String(resolver.getTranslationUnitPath());
	}
	
    public boolean accept( ASTVisitor action ){
        if( action.shouldVisitTranslationUnit){
		    switch( action.visit( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        IASTDeclaration [] ds = getDeclarations();
        for( int i = 0; i < ds.length; i++ ){
            if( !ds[i].accept( action ) ) return false;
        }
        
        if( action.shouldVisitTranslationUnit){
		    switch( action.leave( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        return true;
    }
    
    public IASTFileLocation flattenLocationsToFile(IASTNodeLocation[] nodeLocations) {
        if( resolver == null )
            return null;
        return resolver.flattenLocations( nodeLocations );
    }

    public IDependencyTree getDependencyTree() {
        if( resolver == null )
            return null;
        return resolver.getDependencyTree();
    }

	public String getContainingFilename(int offset) {
		if( resolver == null )
			return EMPTY_STRING;
		return resolver.getContainingFilePath( offset );
	}
    
    public void replace(IASTNode child, IASTNode other) {
        if( decls == null ) return;
        for( int i = 0; i < decls.length; ++i )
        {
           if( decls[i] == null ) break;
           if( decls[i] == child )
           {
               other.setParent( child.getParent() );
               other.setPropertyInParent( child.getPropertyInParent() );
               decls[i] = (IASTDeclaration) other;
           }
        }
    }

    public ParserLanguage getParserLanguage() {
        return ParserLanguage.CPP;
    }
    
    public IIndex getIndex() {
    	return index;
    }
    
    public void setIndex(IIndex pdom) {
    	this.index = pdom;
    	if (index != null) {
    		fIndexFileSet= index.createFileSet();
    	}
    }

	public IASTComment[] getComments() {
		if (resolver != null) {
			return resolver.getComments();
		}
		return new IASTComment[0];
	}

	@SuppressWarnings("unchecked")
	public Object getAdapter(Class adapter) {
		if (adapter.isAssignableFrom(resolver.getClass())) {
			return resolver;
		}
		if (adapter.isAssignableFrom(IIndexFileSet.class)) {
			return fIndexFileSet;
		}
		return null;
	}

	public boolean isHeaderUnit() {
		return fIsHeader;
	}

	public void setIsHeaderUnit(boolean headerUnit) {
		fIsHeader= headerUnit;
	}
}
