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

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTArrayDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
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
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBasicType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceScope;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.Linkage;
import org.eclipse.cdt.internal.core.dom.parser.ASTInternal;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.ASTTranslationUnit;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;
import org.eclipse.cdt.internal.core.dom.parser.GCCBuiltinSymbolProvider.CPPBuiltinParameter;
import org.eclipse.cdt.internal.core.index.IIndexScope;
import org.eclipse.cdt.internal.core.parser.scanner.IncludeFileContent;

/**
 * C++-specific implementation of a translation-unit.
 */
public class CPPASTTranslationUnit extends ASTTranslationUnit implements ICPPASTTranslationUnit, IASTAmbiguityParent {
    private CPPNamespaceScope fScope = null;
    private ICPPNamespace fBinding = null;
	private CPPScopeMapper fScopeMapper= new CPPScopeMapper(this);
	
	public CPPASTTranslationUnit() {
	}
	
    public CPPNamespaceScope getScope() {
        if (fScope == null) {
            fScope = new CPPNamespaceScope(this);
			addBuiltinOperators(fScope);
        }
        return fScope;
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
        temp = new CPPImplicitFunction(OverloadableOperator.NEW.toCharArray(), theScope, newFunctionType, newTheParms, false);
        try {
        	ASTInternal.addBinding(theScope, temp);
        } catch (DOMException de) {}
		
		// void * operator new[] (std::size_t);
		temp = null;
        temp = new CPPImplicitFunction(OverloadableOperator.NEW_ARRAY.toCharArray(), theScope, newFunctionType, newTheParms, false);
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
        temp = new CPPImplicitFunction(OverloadableOperator.DELETE.toCharArray(), theScope, deleteFunctionType, deleteTheParms, false);
        try {
        	ASTInternal.addBinding(theScope, temp);
        } catch (DOMException de) {}
		
		// void operator delete[](void*);
		temp = null;
        temp = new CPPImplicitFunction(OverloadableOperator.DELETE_ARRAY.toCharArray(), theScope, deleteFunctionType, deleteTheParms, false);
        try {
        	ASTInternal.addBinding(theScope, temp);
        } catch (DOMException de) {}
	}
	
    public IASTName[] getDeclarationsInAST(IBinding binding) {
        if (binding instanceof IMacroBinding) {
        	return getMacroDefinitionsInAST((IMacroBinding) binding);
        }
        return CPPVisitor.getDeclarations(this, binding);
    }

    public IASTName[] getDefinitionsInAST(IBinding binding) {
        if (binding instanceof IMacroBinding) {
        	return getMacroDefinitionsInAST((IMacroBinding) binding);
        }
    	IASTName[] names = CPPVisitor.getDeclarations(this, binding);
        for (int i = 0; i < names.length; i++) {
            if (!names[i].isDefinition())
                names[i] = null;
        }
    	// nulls can be anywhere, don't use trim()
        return (IASTName[])ArrayUtil.removeNulls(IASTName.class, names);
    }

    public IASTName[] getReferences(IBinding binding) {
        if (binding instanceof IMacroBinding) {
            return getMacroReferencesInAST((IMacroBinding) binding);
        }
        return CPPVisitor.getReferences(this, binding);
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
    	
 
    	@Override
		public int visit(IASTDeclaration declaration) {
    		// use declarations to determine if the search has gone past the offset (i.e. don't know the order the visitor visits the nodes)
    		if (declaration instanceof ASTNode && ((ASTNode)declaration).getOffset() > offset)
    			return PROCESS_ABORT;
    		
    		return processNode(declaration);
    	}
    	

    	@Override
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

    	@Override
		public int visit(IASTDeclSpecifier declSpec) {
    		return processNode(declSpec);
    	}

    	@Override
		public int visit(IASTEnumerator enumerator) {
    		return processNode(enumerator);
    	}

    	@Override
		public int visit(IASTExpression expression) {
    		return processNode(expression);
    	}
 
    	@Override
		public int visit(IASTInitializer initializer) {
    		return processNode(initializer);
    	}
    	
    	@Override
		public int visit(IASTName name) {
    		if ( name.toString() != null )
    			return processNode(name);
    		return PROCESS_CONTINUE;
    	}
    	
    	@Override
		public int visit(
    			IASTParameterDeclaration parameterDeclaration) {
    		return processNode(parameterDeclaration);
    	}
    	
    	@Override
		public int visit(IASTStatement statement) {
    		return processNode(statement);
    	}
    	
    	@Override
		public int visit(IASTTypeId typeId) {
    		return processNode(typeId);
    	}

    	public IASTNode getNode() {
    		return foundNode;
    	}
    }
    

    public IASTNode selectNodeForLocation(String path, int realOffset, int realLength) {
    	IASTNode result= null;
		if (fLocationResolver != null) {
	    	int start= fLocationResolver.getSequenceNumberForFileOffset(path, realOffset);
	    	if (start >= 0) {
	    		int length= realLength < 1 ? 0 : 
	    			fLocationResolver.getSequenceNumberForFileOffset(path, realOffset+realLength-1) + 1 - start;
	    		result= fLocationResolver.findSurroundingPreprocessorNode(start, length);
	    		if (result == null) {
	    			CPPFindNodeForOffsetAction nodeFinder = new CPPFindNodeForOffsetAction(start, length);
	    			accept(nodeFinder);
	    			result = nodeFinder.getNode();
	    		}
	    	}    	
		}
		return result;
    }

    public IBinding resolveBinding() {
        if (fBinding == null)
            fBinding = new CPPNamespace(this);
        return fBinding;
    }
	
    public void replace(IASTNode child, IASTNode other) {
        if (fDeclarations == null) return;
        for(int i=0; i < fDeclarations.length; ++i) {
           if (fDeclarations[i] == null) break;
           if (fDeclarations[i] == child) {
               other.setParent(child.getParent());
               other.setPropertyInParent(child.getPropertyInParent());
               fDeclarations[i] = (IASTDeclaration) other;
           }
        }
    }

    public ParserLanguage getParserLanguage() {
        return ParserLanguage.CPP;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IASTTranslationUnit#getLinkage()
	 */
	public ILinkage getLinkage() {
		return Linkage.CPP_LINKAGE;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.scanner.ISkippedIndexedFilesListener#skippedFile(org.eclipse.cdt.internal.core.parser.scanner.IncludeFileContent)
	 */
	public void skippedFile(int offset, IncludeFileContent fileContent) {
		super.skippedFile(offset, fileContent);
		fScopeMapper.registerAdditionalDirectives(offset, fileContent.getUsingDirectives());
	}	
	
	// bug 217102: namespace scopes from the index have to be mapped back to the AST.
	IScope mapToASTScope(IIndexScope scope) {
		return fScopeMapper.mapToASTScope(scope);
	}

	/**
	 * Stores directives from the index into this scope.
	 */
	void handleAdditionalDirectives(ICPPNamespaceScope scope) {
		fScopeMapper.handleAdditionalDirectives(scope);
	}
}
