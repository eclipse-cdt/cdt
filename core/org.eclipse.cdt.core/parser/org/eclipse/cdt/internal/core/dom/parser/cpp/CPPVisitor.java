/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
/*
 * Created on Nov 29, 2004
 */
package org.eclipse.cdt.internal.core.dom.parser.cpp;


import java.util.ArrayList;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTArrayDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTArraySubscriptExpression;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTCaseStatement;
import org.eclipse.cdt.core.dom.ast.IASTCastExpression;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTConditionalExpression;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTDoStatement;
import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionList;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTFieldDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTGotoStatement;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerList;
import org.eclipse.cdt.core.dom.ast.IASTLabelStatement;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTPointer;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTProblemHolder;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStandardFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTSwitchStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTWhileStatement;
import org.eclipse.cdt.core.dom.ast.IArrayType;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IQualifierType;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCatchHandler;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeleteExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFieldReference;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionTryBlockDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLinkageSpecification;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceAlias;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTReferenceOperator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeConstructorExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTryBlockStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTypenameExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDirective;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBasicType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.dom.ast.gnu.IGNUASTCompoundStatementExpression;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;

/**
 * @author aniefer
 */
public class CPPVisitor {

	/**
	 * @param name
	 */
	public static IBinding createBinding(IASTName name) {
		IASTNode parent = name.getParent();
		IBinding binding = null;
		if( parent instanceof IASTNamedTypeSpecifier  ||
		    parent instanceof ICPPASTQualifiedName    ||
			parent instanceof ICPPASTBaseSpecifier 	  ||
			parent instanceof ICPPASTConstructorChainInitializer ) 
		{
			binding = CPPSemantics.resolveBinding( name ); 
			if( binding instanceof IProblemBinding && parent instanceof ICPPASTQualifiedName ){
				if( ((IProblemBinding)binding).getID() == IProblemBinding.SEMANTIC_NAME_NOT_FOUND ){
					parent = parent.getParent();
				}
			} else {
				return binding;
			}
		} 
		if( parent instanceof IASTIdExpression ){
			return resolveBinding( parent );
		} else if( parent instanceof ICPPASTFieldReference ){
			return resolveBinding( parent );
		} else if( parent instanceof ICPPASTCompositeTypeSpecifier ){
			return createBinding( (ICPPASTCompositeTypeSpecifier) parent );
		} else if( parent instanceof IASTDeclarator ){
			return createBinding( (IASTDeclarator) parent );
		} else if( parent instanceof ICPPASTElaboratedTypeSpecifier ){
			return createBinding( (ICPPASTElaboratedTypeSpecifier) parent );
		} else if( parent instanceof IASTDeclaration ){
			return createBinding( (IASTDeclaration) parent );
		} else if( parent instanceof IASTEnumerationSpecifier ){
		    return createBinding( (IASTEnumerationSpecifier) parent );
		} else if( parent instanceof IASTEnumerator ){
		    return createBinding( (IASTEnumerator) parent );
		} else if( parent instanceof IASTGotoStatement ){
		    return createBinding( (IASTGotoStatement) parent );
		} else if( parent instanceof IASTLabelStatement ){
		    return createBinding( (IASTLabelStatement) parent );
		}
		
		return null;
	}
	
	private static IBinding createBinding( IASTGotoStatement gotoStatement ) {
	    ICPPFunctionScope functionScope = (ICPPFunctionScope) getContainingScope( gotoStatement );
	    IASTName name = gotoStatement.getName();
	    IBinding binding;
        try {
            binding = functionScope.getBinding( name );
            if( binding == null ){
    	        binding = new CPPLabel( gotoStatement );
    	        functionScope.addBinding( binding );
    	    }
        } catch ( DOMException e ) {
            binding = e.getProblem();
        }
        
	    return binding;
	}
	
	private static IBinding createBinding( IASTLabelStatement labelStatement ) {
	    ICPPFunctionScope functionScope = (ICPPFunctionScope) getContainingScope( labelStatement );
	    IASTName name = labelStatement.getName();
	    IBinding binding;
        try {
            binding = functionScope.getBinding( name );
            if( binding == null ){
    	        binding = new CPPLabel( labelStatement );
    	        functionScope.addBinding( binding );
    	    } else {
    	        ((CPPLabel)binding).setLabelStatement( labelStatement );
    	    }
        } catch ( DOMException e ) {
            binding = e.getProblem();
        }
        
	    return binding;
	}
	
    private static IBinding createBinding( IASTEnumerator enumerator ) {
        ICPPScope scope = (ICPPScope) getContainingScope( enumerator );
        IBinding enumtor;
        try {
            enumtor = scope.getBinding( enumerator.getName() );
            if( enumtor == null ){
                enumtor = new CPPEnumerator( enumerator );
                scope.addBinding( enumtor );
            }
        } catch ( DOMException e ) {
            enumtor = e.getProblem();
        }
        
        return enumtor;
    }


    private static IBinding createBinding( IASTEnumerationSpecifier specifier ) {
        ICPPScope scope = (ICPPScope) getContainingScope( specifier );
        IBinding enumeration;
        try {
            enumeration = scope.getBinding( specifier.getName() );
            if( enumeration == null ){
                enumeration = new CPPEnumeration( specifier );
                scope.addBinding( enumeration );
            }
        } catch ( DOMException e ) {
            enumeration = e.getProblem();
        }
        
        return enumeration;
    }

    private static IBinding createBinding( ICPPASTElaboratedTypeSpecifier elabType ){
	    IASTNode parent = elabType.getParent();
	    IBinding binding = null;
	    boolean mustBeSimple = true;
	    if( parent instanceof IASTSimpleDeclaration ){
	        IASTDeclarator [] dtors = ((IASTSimpleDeclaration)parent).getDeclarators();
	        if( dtors.length > 0 ){
	        	binding = CPPSemantics.resolveBinding( elabType.getName() );
	        } else {
	        	mustBeSimple = false;
	        }
	    } else if( parent instanceof IASTParameterDeclaration || 
	    		   parent instanceof IASTDeclaration ||
				   parent instanceof IASTTypeId )
	    {
	    	binding = CPPSemantics.resolveBinding( elabType.getName() );
	    }
	    
		if( binding != null && 
		    (!(binding instanceof IProblemBinding) ||((IProblemBinding)binding).getID() != IProblemBinding.SEMANTIC_NAME_NOT_FOUND) )
		{
			return binding;
    	}
		
		//7.1.5.3-2 ... If name lookup does not find a declaration for the name, the elaborated-type-specifier is ill-formed
		//unless it is of the simple form class-key identifier
	    if( mustBeSimple && elabType.getName() instanceof ICPPASTQualifiedName )
	    	return binding;
	    
		ICPPScope scope = (ICPPScope) getContainingScope( elabType );
		
		if( mustBeSimple ){
			//3.3.1-5 ... the identifier is declared in the smallest non-class non-function-prototype scope that contains
			//the declaration
			while( scope instanceof ICPPClassScope || scope instanceof ICPPFunctionScope ){
				try {
					scope = (ICPPScope) scope.getParent();
				} catch (DOMException e1) {
				}
			}
		}
        try {
            binding = scope.getBinding( elabType.getName() );
            if( binding == null ){
    			if( elabType.getKind() != IASTElaboratedTypeSpecifier.k_enum )
    				binding = new CPPClassType( elabType );
    			scope.addBinding( binding );
    		} else {
    			((CPPClassType)binding).addDeclaration( elabType );
    		}
        } catch ( DOMException e ) {
            binding = e.getProblem();
        }
        
		return binding;
	}
	private static IBinding createBinding( ICPPASTCompositeTypeSpecifier compType ){
		IASTName name = compType.getName();
		if( name instanceof ICPPASTQualifiedName ){
			IASTName [] ns = ((ICPPASTQualifiedName)name).getNames();
			name = ns[ ns.length - 1 ];
		}
		ICPPScope scope = (ICPPScope) getContainingScope( name );
		IBinding binding;
        try {
            binding = scope.getBinding( compType.getName() );
            if( binding == null || !(binding instanceof ICPPClassType) ){
    			binding = new CPPClassType( compType );
    			scope.addBinding( binding );
    		} else {
    			((CPPClassType)binding).addDefinition( compType );
    		}
        } catch ( DOMException e ) {
            binding = e.getProblem();
        }
        
		return binding;
	}
	private static IBinding createBinding( IASTDeclaration declaration ){
		if( declaration instanceof ICPPASTNamespaceDefinition ){
			ICPPASTNamespaceDefinition namespaceDef = (ICPPASTNamespaceDefinition) declaration;
			ICPPScope scope = (ICPPScope) getContainingScope( namespaceDef );
			IBinding binding;
            try {
                binding = scope.getBinding( namespaceDef.getName() );
                if( binding == null ){
    				binding = new CPPNamespace( namespaceDef.getName() );
    				scope.addBinding( binding );
    			}
            } catch ( DOMException e ) {
                binding = e.getProblem();
            }
			return binding;
		} else if( declaration instanceof ICPPASTUsingDirective ){
			return CPPSemantics.resolveBinding( ((ICPPASTUsingDirective) declaration).getQualifiedName() );
		} else if( declaration instanceof ICPPASTNamespaceAlias ) {
			ICPPASTNamespaceAlias alias = (ICPPASTNamespaceAlias) declaration;
			return CPPSemantics.resolveBinding( alias.getQualifiedName() );
		}
		
			
		return null;
	}
	private static IBinding createBinding( IASTDeclarator declarator ){
		IASTNode parent = declarator.getParent();

		if( parent instanceof IASTTypeId )
		    return CPPSemantics.resolveBinding( declarator.getName() );
		    
		if( declarator.getNestedDeclarator() != null )
			return createBinding( declarator.getNestedDeclarator() );

		
		while( parent instanceof IASTDeclarator ){
			parent = parent.getParent();
		}
		
		ICPPScope scope = (ICPPScope) getContainingScope( parent );
		IBinding binding;
        try {
            binding = ( scope != null ) ? scope.getBinding( declarator.getName() ) : null;
        } catch ( DOMException e ) {
            binding = null;
        }
        
        if( declarator instanceof ICPPASTFunctionDeclarator ){
			if( binding != null && binding instanceof IFunction ){
			    IFunction function = (IFunction) binding;
			    IFunctionType ftype;
                try {
                    ftype = function.getType();
                    IType type = createType( declarator );
    			    if( ftype.equals( type ) ){
    			        if( parent instanceof IASTSimpleDeclaration )
    			            ((CPPFunction)function).addDeclaration( (ICPPASTFunctionDeclarator) declarator );
    			        else 
    			            ((CPPFunction)function).addDefinition( (ICPPASTFunctionDeclarator) declarator );
    			        
    			        return function;
    			    }
                } catch ( DOMException e1 ) {
                }
			} 
			if( scope instanceof ICPPClassScope ){
				if( isConstructor( scope, declarator) )
					binding = new CPPConstructor( (ICPPASTFunctionDeclarator) declarator );
				else 
					binding = new CPPMethod( (ICPPASTFunctionDeclarator) declarator );
			} else {
				binding = new CPPFunction( (ICPPASTFunctionDeclarator) declarator );
			}
		} else if( parent instanceof IASTParameterDeclaration ){
			IASTParameterDeclaration param = (IASTParameterDeclaration) parent;
			IASTStandardFunctionDeclarator fDtor = (IASTStandardFunctionDeclarator) param.getParent();
			IBinding temp = fDtor.getName().resolveBinding();
			if( temp instanceof IFunction ){
				CPPFunction function = (CPPFunction) temp;
				binding = function.resolveParameter( param );
			}
		} else if( parent instanceof IASTSimpleDeclaration ){
			IASTSimpleDeclaration simpleDecl = (IASTSimpleDeclaration) parent;			
			if( simpleDecl.getDeclSpecifier().getStorageClass() == IASTDeclSpecifier.sc_typedef ){
				binding = new CPPTypedef( declarator );
			} else if( simpleDecl.getParent() instanceof ICPPASTCompositeTypeSpecifier ){
				binding = new CPPField( declarator );
			} else {
				binding = new CPPVariable( declarator );
			}
		} 

		if( scope != null && binding != null ){
            try {
                scope.addBinding( binding );
            } catch ( DOMException e1 ) {
            }
		}
		
		return binding;
	}

	public static boolean isConstructor( IScope containingScope, IASTDeclarator declarator ){
	    if( containingScope == null || !(containingScope instanceof ICPPClassScope) )
	        return false;
	    
	    ICPPASTCompositeTypeSpecifier clsTypeSpec;
        try {
            clsTypeSpec = (ICPPASTCompositeTypeSpecifier) ((ICPPClassScope)containingScope).getPhysicalNode();
        } catch ( DOMException e ) {
            return false;
        }
        return isConstructor( clsTypeSpec.getName(), declarator );
	}
	public static boolean isConstructor( IASTName parentName, IASTDeclarator declarator ){
	    if( declarator == null      || !(declarator instanceof IASTFunctionDeclarator) )
	        return false;
        
	    IASTName name = declarator.getName();
	    if( name instanceof ICPPASTQualifiedName ){
	        IASTName [] names = ((ICPPASTQualifiedName)name).getNames(); 
	        name = names[ names.length - 1 ];
	    }
	    if( !CharArrayUtils.equals( name.toCharArray(), parentName.toCharArray() ) )
	        return false;
	    
	    IASTDeclSpecifier declSpec = null;
	    IASTNode parent = declarator.getParent();
	    if( parent instanceof IASTSimpleDeclaration ){
	        declSpec = ((IASTSimpleDeclaration)parent).getDeclSpecifier();
	    } else if( parent instanceof IASTFunctionDefinition ){
	        declSpec = ((IASTFunctionDefinition)parent).getDeclSpecifier();
	    }
	    if( declSpec != null && declSpec instanceof IASTSimpleDeclSpecifier ){
	        return ( ((IASTSimpleDeclSpecifier)declSpec).getType() == IASTSimpleDeclSpecifier.t_unspecified ); 
	    }
	    
	    return false;
	    
	}
	
	public static IScope getContainingScope( IASTNode node ){
		if( node == null )
			return null;
		if( node instanceof IASTName )
			return getContainingScope( (IASTName) node );
		else if( node instanceof IASTDeclaration )
	        return getContainingScope( (IASTDeclaration) node );
	    else if( node instanceof IASTStatement )
	        return getContainingScope( (IASTStatement) node );
	    else if( node instanceof IASTDeclSpecifier )
	        return getContainingScope( (IASTDeclSpecifier) node );
	    else if( node instanceof IASTParameterDeclaration )
	        return getContainingScope( (IASTParameterDeclaration) node );
	    else if( node instanceof IASTExpression )
	        return getContainingScope( (IASTExpression) node );
	    else if( node instanceof IASTEnumerator ){
	        //put the enumerators in the same scope as the enumeration
	        return getContainingScope( (IASTEnumerationSpecifier) node.getParent() );
	    }
	    
	    return getContainingScope( node.getParent() );
	}
	
	public static IScope getContainingScope( IASTExpression expression ){
	    IASTNode parent = expression.getParent();
	    if( parent instanceof IASTForStatement ){
	        return ((IASTForStatement)parent).getScope();
	    } else if( parent instanceof IASTCompoundStatement ){
	        return ((IASTCompoundStatement)parent).getScope();
	    } else if( parent instanceof ICPPASTConstructorChainInitializer ){
	    	IASTNode node = getContainingBlockItem( parent );
	    	if( node instanceof IASTFunctionDefinition ){
	    		IASTCompoundStatement body = (IASTCompoundStatement) ((IASTFunctionDefinition)node).getBody();
	    		return body.getScope();
	    	}
	    }
	    return getContainingScope( parent );
	}
	public static IScope getContainingScope( IASTName name ){
		IASTNode parent = name.getParent();
		try {
			if( parent instanceof ICPPASTQualifiedName ){
				IASTName [] names = ((ICPPASTQualifiedName) parent).getNames();
				int i = 0;
				for( ; i < names.length; i++ ){
					if( names[i] == name ) break;
				}
				if( i > 0 ){
					IBinding binding = names[i - 1].resolveBinding();
					if( binding instanceof ICPPClassType ){
						return ((ICPPClassType)binding).getCompositeScope();
					} else if( binding instanceof ICPPNamespace ){
						return ((ICPPNamespace)binding).getNamespaceScope();
					}
				}
			} else if( parent instanceof ICPPASTFieldReference ){
				IASTExpression owner = ((ICPPASTFieldReference)parent).getFieldOwner();
				IType type = CPPSemantics.getUltimateType( getExpressionType( owner ) );
				if( type instanceof ICPPClassType ){
					return ((ICPPClassType) type).getCompositeScope();
				}
			}
		} catch( DOMException e ){
		    return e.getProblem();
		}
		return getContainingScope( parent );
	}
	/**
	 * @param declaration
	 * @return
	 */
	public static IScope getContainingScope(IASTDeclaration declaration) {
		IASTNode parent = declaration.getParent();
		if( parent instanceof IASTTranslationUnit ){
			return ((IASTTranslationUnit)parent).getScope();
		} else if( parent instanceof IASTDeclarationStatement ){
			return getContainingScope( (IASTStatement) parent );
		} else if( parent instanceof IASTForStatement ){
		    return ((IASTForStatement)parent).getScope();
		} else if( parent instanceof IASTCompositeTypeSpecifier ){
		    return ((IASTCompositeTypeSpecifier)parent).getScope();
		} else if( parent instanceof ICPPASTNamespaceDefinition ) {
			return ((ICPPASTNamespaceDefinition)parent).getScope();
		}
		
		return null;
	}
	
	public static IScope getContainingScope( IASTStatement statement ){
		IASTNode parent = statement.getParent();
		IScope scope = null;
		if( parent instanceof IASTCompoundStatement ){
		    IASTCompoundStatement compound = (IASTCompoundStatement) parent;
		    scope = compound.getScope();
		} else if( parent instanceof IASTStatement ){
			scope = getContainingScope( (IASTStatement)parent );
		} else if( parent instanceof IASTFunctionDefinition ){
			IASTFunctionDeclarator fnDeclarator = ((IASTFunctionDefinition) parent ).getDeclarator();
			IFunction function = (IFunction) fnDeclarator.getName().resolveBinding();
			try {
                scope = function.getFunctionScope();
            } catch ( DOMException e ) {
                return e.getProblem();
            }
		}
		
		if( statement instanceof IASTGotoStatement || statement instanceof IASTLabelStatement ){
		    //labels have function scope
		    while( scope != null && !(scope instanceof ICPPFunctionScope) ){
		        try {
                    scope = scope.getParent();
                } catch ( DOMException e ) {
                    return e.getProblem();
                }
		    }
		}
		
		return scope;
	}
	
	public static IScope getContainingScope( IASTDeclSpecifier typeSpec ){
//		if( typeSpec instanceof ICPPASTCompositeTypeSpecifier ){
//			ICPPASTCompositeTypeSpecifier compTypeSpec = (ICPPASTCompositeTypeSpecifier) typeSpec;
//			IASTName name = compTypeSpec.getName();
//			if( name instanceof ICPPASTQualifiedName ){
//				IASTName [] names = ((ICPPASTQualifiedName)name).getNames();
//				if( names.length > 1 ){
//					IBinding binding = names[ names.length - 2 ].resolveBinding();
//					if( binding instanceof ICPPClassType )
//						return ((ICPPClassType)binding).getCompositeScope();
//					else if( binding instanceof ICPPNamespace )
//						return ((ICPPNamespace)binding).getNamespaceScope();
//				}
//			}
//		}
	    IASTNode parent = typeSpec.getParent();
	    if( parent instanceof IASTSimpleDeclaration )
	        return getContainingScope( (IASTSimpleDeclaration) parent );
	    else if( parent instanceof IASTTypeId )
	        return getContainingScope( parent.getParent() );
	    return getContainingScope( parent );
	}

	/**
	 * @param parameterDeclaration
	 * @return
	 */
	public static IScope getContainingScope(IASTParameterDeclaration parameterDeclaration) {
		ICPPASTFunctionDeclarator dtor = (ICPPASTFunctionDeclarator) parameterDeclaration.getParent();
		IASTName name = dtor.getName();
		if( name instanceof ICPPASTQualifiedName ) {
			IASTName[] ns = ((ICPPASTQualifiedName) name).getNames();
			if( ns.length > 0 )
				return getContainingScope( ns [ ns.length - 1 ] );
		} 
		return getContainingScope( dtor );
	}
	
	public static IASTNode getContainingBlockItem( IASTNode node ){
		IASTNode parent = node.getParent();
		if( parent == null )
			return null;
		
		if( parent instanceof IASTDeclaration ){
			IASTNode p = parent.getParent();
			if( p instanceof IASTDeclarationStatement )
				return p;
			return parent;
		} else if( parent instanceof IASTExpression ){
			IASTNode p = parent.getParent();
			if( p instanceof IASTStatement )
				return p;
		} else if ( parent instanceof IASTStatement || parent instanceof IASTTranslationUnit ) {
			return parent;
		}
		
		return getContainingBlockItem( parent );
	}
	
	static private IBinding resolveBinding( IASTNode node ){
		if( node instanceof IASTIdExpression ){
			return CPPSemantics.resolveBinding( ((IASTIdExpression)node).getName() );
		} else if( node instanceof ICPPASTFieldReference ){
			return CPPSemantics.resolveBinding( ((ICPPASTFieldReference)node).getFieldName() );
		} else if( node instanceof IASTFunctionCallExpression ){
		    return resolveBinding( ((IASTFunctionCallExpression)node).getFunctionNameExpression() );
		}
		return null;
	}
	
	public static abstract class CPPBaseVisitorAction {
		public boolean processNames          = false;
		public boolean processDeclarations   = false;
		public boolean processInitializers   = false;
		public boolean processParameterDeclarations = false;
		public boolean processDeclarators    = false;
		public boolean processDeclSpecifiers = false;
		public boolean processExpressions    = false;
		public boolean processStatements     = false;
		public boolean processTypeIds        = false;
		public boolean processEnumerators    = false;
		public boolean processBaseSpecifiers = false;
		public boolean processNamespaces     = false;
		
		/**
		 * @return true to continue visiting, return false to stop
		 */
		public final static int PROCESS_SKIP     = 1;
		public final static int PROCESS_ABORT    = 2;
		public final static int PROCESS_CONTINUE = 3;
		
		public int processName( IASTName name ) 					{ return PROCESS_CONTINUE; }
		public int processDeclaration( IASTDeclaration declaration ){ return PROCESS_CONTINUE; }
		public int processInitializer( IASTInitializer initializer ){ return PROCESS_CONTINUE; }
		public int processParameterDeclaration( IASTParameterDeclaration parameterDeclaration ) { return PROCESS_CONTINUE; }
		public int processDeclarator( IASTDeclarator declarator )   { return PROCESS_CONTINUE; }
		public int processDeclSpecifier( IASTDeclSpecifier declSpec ){return PROCESS_CONTINUE; }
		public int processExpression( IASTExpression expression )   { return PROCESS_CONTINUE; }
		public int processStatement( IASTStatement statement )      { return PROCESS_CONTINUE; }
		public int processTypeId( IASTTypeId typeId )               { return PROCESS_CONTINUE; }
		public int processEnumerator( IASTEnumerator enumerator )   { return PROCESS_CONTINUE; }
		public int processBaseSpecifier(ICPPASTBaseSpecifier specifier) { return PROCESS_CONTINUE; }
		public int processNamespace( ICPPASTNamespaceDefinition namespace) { return PROCESS_CONTINUE; }
	}
	
	public static class CollectProblemsAction extends CPPBaseVisitorAction {
		{
			processDeclarations = true;
			processExpressions = true;
			processStatements = true;
			processTypeIds = true;
		}
		
		private static final int DEFAULT_CHILDREN_LIST_SIZE = 8;
		private IASTProblem[] problems = null;
		int numFound = 0;

		public CollectProblemsAction() {
			problems = new IASTProblem[DEFAULT_CHILDREN_LIST_SIZE];
		}
		
		private void addProblem(IASTProblem problem) {
			if( problems.length == numFound ) // if the found array is full, then double the array
	        {
	            IASTProblem [] old = problems;
	            problems = new IASTProblem[ old.length * 2 ];
	            for( int j = 0; j < old.length; ++j )
	                problems[j] = old[j];
	        }
			problems[numFound++] = problem;
		}
		
	    private IASTProblem[] removeNullFromProblems() {
	    	if (problems[problems.length-1] != null) { // if the last element in the list is not null then return the list
				return problems;			
			} else if (problems[0] == null) { // if the first element in the list is null, then return empty list
				return new IASTProblem[0];
			}
			
			IASTProblem[] results = new IASTProblem[numFound];
			for (int i=0; i<results.length; i++)
				results[i] = problems[i];
				
			return results;
	    }
		
		public IASTProblem[] getProblems() {
			return removeNullFromProblems();
		}
	    
		/* (non-Javadoc)
		 * @see org.eclipse.cdt.internal.core.dom.parser.c.CVisitor.CBaseVisitorAction#processDeclaration(org.eclipse.cdt.core.dom.ast.IASTDeclaration)
		 */
		public int processDeclaration(IASTDeclaration declaration) {
			if ( declaration instanceof IASTProblemHolder )
				addProblem(((IASTProblemHolder)declaration).getProblem());

			return PROCESS_CONTINUE;
		}
		/* (non-Javadoc)
		 * @see org.eclipse.cdt.internal.core.dom.parser.c.CVisitor.CBaseVisitorAction#processExpression(org.eclipse.cdt.core.dom.ast.IASTExpression)
		 */
		public int processExpression(IASTExpression expression) {
			if ( expression instanceof IASTProblemHolder )
				addProblem(((IASTProblemHolder)expression).getProblem());

			return PROCESS_CONTINUE;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.cdt.internal.core.dom.parser.c.CVisitor.CBaseVisitorAction#processStatement(org.eclipse.cdt.core.dom.ast.IASTStatement)
		 */
		public int processStatement(IASTStatement statement) {
			if ( statement instanceof IASTProblemHolder )
				addProblem(((IASTProblemHolder)statement).getProblem());

			return PROCESS_CONTINUE;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.cdt.internal.core.dom.parser.c.CVisitor.CBaseVisitorAction#processTypeId(org.eclipse.cdt.core.dom.ast.IASTTypeId)
		 */
		public int processTypeId(IASTTypeId typeId) {
			if ( typeId instanceof IASTProblemHolder )
				addProblem(((IASTProblemHolder)typeId).getProblem());

			return PROCESS_CONTINUE;
		}
	}
	
	public static void visitTranslationUnit( IASTTranslationUnit tu, CPPBaseVisitorAction action ){
		IASTDeclaration [] decls = tu.getDeclarations();
		for( int i = 0; i < decls.length; i++ ){
			if( !visitDeclaration( decls[i], action ) ) return;
		}
	}

	public static boolean visitNamespaceDefinition( ICPPASTNamespaceDefinition namespace, CPPBaseVisitorAction action ){
	    if( action.processNamespaces ){
	        switch( action.processNamespace( namespace ) ){
	            case CPPBaseVisitorAction.PROCESS_ABORT : return false;
	            case CPPBaseVisitorAction.PROCESS_SKIP  : return true;
	            default : break;
	        }
	    }
	        
		if( !visitName( namespace.getName(), action ) ) return false;
		IASTDeclaration [] decls = namespace.getDeclarations();
		for( int i = 0; i < decls.length; i++ ){
			if( !visitDeclaration( decls[i], action ) ) return false;
		}
	   return true;
	}
	/**
	 * @param declaration
	 * @param action
	 * @return
	 */
	public static boolean visitDeclaration(IASTDeclaration declaration, CPPBaseVisitorAction action) {
	    if( declaration instanceof ICPPASTNamespaceDefinition )
	        return visitNamespaceDefinition( (ICPPASTNamespaceDefinition) declaration, action );
	    
		if( action.processDeclarations ) {
		    switch( action.processDeclaration( declaration ) ){
	            case CPPBaseVisitorAction.PROCESS_ABORT : return false;
	            case CPPBaseVisitorAction.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
		
		if( declaration instanceof IASTSimpleDeclaration ){
			IASTSimpleDeclaration simple = (IASTSimpleDeclaration) declaration;
			if( !visitDeclSpecifier( simple.getDeclSpecifier(), action ) ) return false;
			IASTDeclarator [] dtors = simple.getDeclarators();
			for( int i = 0; i < dtors.length; i++ ){
				if( !visitDeclarator( dtors[i], action) ) return false;
			}
		} else if( declaration instanceof IASTFunctionDefinition ){
			IASTFunctionDefinition function = (IASTFunctionDefinition) declaration;
			if( !visitDeclSpecifier( function.getDeclSpecifier(), action ) ) return false;
			if( !visitDeclarator( function.getDeclarator(), action ) ) return false;
			if( !visitStatement( function.getBody(), action ) ) return false;
		} else if( declaration instanceof ICPPASTUsingDeclaration ){
			if( !visitName( ((ICPPASTUsingDeclaration)declaration).getName(), action ) ) return false;
		} else if( declaration instanceof ICPPASTUsingDirective ){
			if( !visitName( ((ICPPASTUsingDirective)declaration).getQualifiedName(), action ) ) return false;
		} else if( declaration instanceof ICPPASTNamespaceAlias ){
			ICPPASTNamespaceAlias alias = (ICPPASTNamespaceAlias) declaration;
			if( !visitName( alias.getQualifiedName(), action ) ) return false;
			if( !visitName( alias.getAlias(), action ) ) return false;
		} else if( declaration instanceof ICPPASTLinkageSpecification ){
			IASTDeclaration [] decls = ((ICPPASTLinkageSpecification) declaration).getDeclarations();
			for( int i = 0; i < decls.length; i++ ){
				if( !visitDeclaration( decls[i], action ) ) return false;
			}
		} else if( declaration instanceof ICPPASTTemplateDeclaration ){
			ICPPASTTemplateDeclaration template = (ICPPASTTemplateDeclaration) declaration;
			ICPPASTTemplateParameter [] params = template.getTemplateParameters();
			for( int i = 0; i < params.length; i++ ){
				if( !visitTemplateParameter( params[i], action ) ) return false;
			}
			if( !visitDeclaration( template.getDeclaration(), action ) ) return false;
		} else if( declaration instanceof ICPPASTTemplateSpecialization ){
			if( !visitDeclaration( ((ICPPASTTemplateSpecialization) declaration).getDeclaration(), action ) ) return false;
		}
		return true;
	}

	/**
	 * @param name
	 * @param action
	 * @return
	 */
	public static boolean visitName(IASTName name, CPPBaseVisitorAction action) {
		if( action.processNames ){
		    switch( action.processName( name ) ){
	            case CPPBaseVisitorAction.PROCESS_ABORT : return false;
	            case CPPBaseVisitorAction.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
			
		if( name instanceof ICPPASTQualifiedName ){
			IASTName [] names = ((ICPPASTQualifiedName)name).getNames();
			for( int i = 0; i < names.length; i++ ){
				if( !visitName( names[i], action ) ) return false;
			}
		}
		return true;
	}

	/**
	 * @param declSpecifier
	 * @param action
	 * @return
	 */
	public static boolean visitDeclSpecifier(IASTDeclSpecifier declSpecifier, CPPBaseVisitorAction action) {
		if( action.processDeclSpecifiers ){
		    switch( action.processDeclSpecifier( declSpecifier ) ){
	            case CPPBaseVisitorAction.PROCESS_ABORT : return false;
	            case CPPBaseVisitorAction.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
			
		if( declSpecifier instanceof ICPPASTCompositeTypeSpecifier ){
			ICPPASTCompositeTypeSpecifier composite = (ICPPASTCompositeTypeSpecifier) declSpecifier;
			if( !visitName( composite.getName(), action ) ) return false;
			ICPPASTBaseSpecifier [] bases = composite.getBaseSpecifiers();
			for( int i = 0; i < bases.length; i++ ) {
				if( !visitBaseSpecifier( bases[i], action ) ) return false;
			}
			IASTDeclaration [] decls = composite.getMembers();
			for( int i = 0; i < decls.length; i++ ){
				if( !visitDeclaration( decls[i], action ) ) return false;
			}
		} else if( declSpecifier instanceof ICPPASTElaboratedTypeSpecifier ){
			if( !visitName( ((ICPPASTElaboratedTypeSpecifier) declSpecifier).getName(), action ) ) return false;
		} else if( declSpecifier instanceof IASTEnumerationSpecifier ){
			IASTEnumerationSpecifier enumeration = (IASTEnumerationSpecifier) declSpecifier;
			if( !visitName( enumeration.getName(), action ) ) return false;
			IASTEnumerator [] etors = enumeration.getEnumerators();
			for( int i = 0; i < etors.length; i++ ){
				if( !visitEnumerator( etors[i], action ) ) return false;
			}
		} else if( declSpecifier instanceof ICPPASTNamedTypeSpecifier ){
			if( !visitName( ((ICPPASTNamedTypeSpecifier)declSpecifier).getName(), action ) ) return false;
		} else if( declSpecifier instanceof IGPPASTSimpleDeclSpecifier ) {
			IASTExpression typeOf = ((IGPPASTSimpleDeclSpecifier)declSpecifier).getTypeofExpression();
			if( typeOf != null )
				if( !visitExpression( typeOf, action ) ) return false;
		}
		return true;
	}

	/**
	 * @param declarator
	 * @param action
	 * @return
	 */
	public static boolean visitDeclarator(IASTDeclarator declarator, CPPBaseVisitorAction action) {
		if( action.processDeclarators ){
		    switch( action.processDeclarator( declarator ) ){
	            case CPPBaseVisitorAction.PROCESS_ABORT : return false;
	            case CPPBaseVisitorAction.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
			
		if( declarator.getPropertyInParent() != IASTTypeId.ABSTRACT_DECLARATOR &&
			declarator.getNestedDeclarator() == null )
		{
			if( !visitName( declarator.getName(), action ) ) return false;
		}
		
		if( declarator.getNestedDeclarator() != null )
			if( !visitDeclarator( declarator.getNestedDeclarator(), action ) ) return false;
		
		if( declarator instanceof ICPPASTFunctionDeclarator ){
			ICPPASTFunctionDeclarator fdtor = (ICPPASTFunctionDeclarator) declarator;
		    IASTParameterDeclaration [] list = fdtor.getParameters();
			for( int i = 0; i < list.length; i++ ){
			    if( !visitParameterDeclaration( list[i], action ) ) return false;
			}
			ICPPASTConstructorChainInitializer [] ctorChain = fdtor.getConstructorChain();
			for( int i = 0; i < ctorChain.length; i++ ){
				if( !visitName( ctorChain[i].getMemberInitializerId(), action ) ) return false;
				if( !visitExpression( ctorChain[i].getInitializerValue(), action ) ) return false;
			}
			IASTTypeId [] typeIds = fdtor.getExceptionSpecification();
			for( int i = 0; i < typeIds.length; i++ ){
				if( !visitTypeId( typeIds[i], action ) ) return false;
			}
			
			if( declarator instanceof ICPPASTFunctionTryBlockDeclarator ){
				ICPPASTCatchHandler [] catchHandlers = ((ICPPASTFunctionTryBlockDeclarator)declarator).getCatchHandlers();
				for( int i = 0; i < catchHandlers.length; i++ ){
					if( !visitStatement( catchHandlers[i], action ) ) return false;
				}
			}
			
		}
		if( declarator instanceof IASTArrayDeclarator ){
			IASTArrayModifier [] mods = ((IASTArrayDeclarator) declarator).getArrayModifiers();
			for( int i = 0; i < mods.length; i++ ){
				if( mods[i].getConstantExpression() != null && !visitExpression( mods[i].getConstantExpression(), action ) ) return false;
			}
		}
		
		if( declarator.getInitializer() != null )
		    if( !visitInitializer( declarator.getInitializer(), action ) ) return false;
		    
		if( declarator instanceof IASTFieldDeclarator )
			if( ! visitExpression( ((IASTFieldDeclarator) declarator).getBitFieldSize(), action ) ) return false;
			
		return true;
	}
	
	private static boolean visitIfStatement( IASTIfStatement ifStatement, CPPBaseVisitorAction action ){
		while( ifStatement != null ){
			if( action.processStatements ){
			    switch( action.processStatement( ifStatement ) ){
			        case CPPBaseVisitorAction.PROCESS_ABORT : return false;
			        case CPPBaseVisitorAction.PROCESS_SKIP  : return true;
			        default : break;
			    }
		    }	
		    if( !visitExpression( ifStatement.getCondition(), action ) ) return false;
		    if( !visitStatement( ifStatement.getThenClause(), action ) ) return false;
		    if( ifStatement.getElseClause() != null ){
		    	IASTStatement statement = ifStatement.getElseClause();
		       	if( statement instanceof IASTIfStatement ){
		       		ifStatement = (IASTIfStatement) statement;
		       		continue;
		       	} 
		       	if( !visitStatement( statement, action ) ) return false;
		    }
		    ifStatement = null;
		}
		return true;
	}
	/**
	 * @param body
	 * @param action
	 * @return
	 */
	public static boolean visitStatement(IASTStatement statement, CPPBaseVisitorAction action) {
		//handle if's in a non-recursive manner to avoid stack overflows in case of huge number of elses
		if( statement instanceof IASTIfStatement )
			return visitIfStatement( (IASTIfStatement) statement, action );
		
		if( action.processStatements ){
		    switch( action.processStatement( statement ) ){
	            case CPPBaseVisitorAction.PROCESS_ABORT : return false;
	            case CPPBaseVisitorAction.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
			
		if( statement instanceof IASTCompoundStatement ){
			IASTStatement [] list = ((IASTCompoundStatement) statement).getStatements();
			for( int i = 0; i < list.length; i++ ){
			    if( list[i] == null ) break;
				if( !visitStatement( list[i], action ) ) return false;
			}
		} else if( statement instanceof IASTDeclarationStatement ){
			if( !visitDeclaration( ((IASTDeclarationStatement)statement).getDeclaration(), action ) ) return false;
		} else if( statement instanceof IASTExpressionStatement ){
		    if( !visitExpression( ((IASTExpressionStatement)statement).getExpression(), action ) ) return false;
		} else if( statement instanceof IASTCaseStatement ){
		    if( !visitExpression( ((IASTCaseStatement)statement).getExpression(), action ) ) return false;
		} else if( statement instanceof IASTDoStatement ){
		    if( !visitStatement( ((IASTDoStatement)statement).getBody(), action ) ) return false;
		    if( !visitExpression( ((IASTDoStatement)statement).getCondition(), action ) ) return false;
		} else if( statement instanceof IASTGotoStatement ){
		    if( !visitName( ((IASTGotoStatement)statement).getName(), action ) ) return false;
		} else if( statement instanceof IASTLabelStatement ){
		    if( !visitName( ((IASTLabelStatement)statement).getName(), action ) ) return false;
		} else if( statement instanceof IASTReturnStatement ){
		    if( !visitExpression( ((IASTReturnStatement) statement ).getReturnValue(), action ) ) return false;
		} else if( statement instanceof IASTSwitchStatement ){
		    if( !visitExpression( ((IASTSwitchStatement) statement ).getController(), action ) ) return false;
		    if( !visitStatement( ((IASTSwitchStatement) statement ).getBody(), action ) ) return false;
		} else if( statement instanceof IASTWhileStatement ){
		    if( !visitExpression( ((IASTWhileStatement) statement ).getCondition(), action ) ) return false;
		    if( !visitStatement( ((IASTWhileStatement) statement ).getBody(), action ) ) return false;
		} else if( statement instanceof IASTForStatement ){
		    IASTForStatement s = (IASTForStatement) statement;
		    if( s.getInitDeclaration() != null )
		        if( !visitDeclaration( s.getInitDeclaration(), action ) ) return false;
		    if( s.getInitExpression() != null )
		        if( !visitExpression( s.getInitExpression(), action ) ) return false;
		    if( !visitExpression( s.getCondition(), action ) ) return false;
		    if( !visitExpression( s.getIterationExpression(), action ) ) return false;
		    if( !visitStatement( s.getBody(), action ) ) return false;
		} else if( statement instanceof ICPPASTCatchHandler ){
			if( !visitDeclaration( ((ICPPASTCatchHandler) statement).getDeclaration(), action ) ) return false;
			if( !visitStatement( ((ICPPASTCatchHandler) statement).getCatchBody(), action ) ) return false;
		} else if( statement instanceof ICPPASTTryBlockStatement ){
			if( !visitStatement( ((ICPPASTTryBlockStatement)statement).getTryBody(), action ) ) return false;
			ICPPASTCatchHandler [] handlers = ((ICPPASTTryBlockStatement)statement).getCatchHandlers();
			for( int i = 0; i < handlers.length; i++ ){
				if( !visitStatement( handlers[i], action ) ) return false;
			}
		}
		
		return true;
	}

	/**
	 * @param typeOf
	 * @param action
	 * @return
	 */
	public static boolean visitExpression(IASTExpression expression, CPPBaseVisitorAction action) {
		if( action.processExpressions ){
		    switch( action.processExpression( expression ) ){
	            case CPPBaseVisitorAction.PROCESS_ABORT : return false;
	            case CPPBaseVisitorAction.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
			
		if( expression instanceof IASTArraySubscriptExpression ){
		    if( !visitExpression( ((IASTArraySubscriptExpression)expression).getArrayExpression(), action ) ) return false;
		    if( !visitExpression( ((IASTArraySubscriptExpression)expression).getSubscriptExpression(), action ) ) return false;
		} else if( expression instanceof IASTBinaryExpression ){
		    if( !visitExpression( ((IASTBinaryExpression)expression).getOperand1(), action ) ) return false;
		    if( !visitExpression( ((IASTBinaryExpression)expression).getOperand2(), action ) ) return false;
		} else if( expression instanceof IASTConditionalExpression){
		    if( !visitExpression( ((IASTConditionalExpression)expression).getLogicalConditionExpression(), action ) ) return false;
		    if( !visitExpression( ((IASTConditionalExpression)expression).getNegativeResultExpression(), action ) ) return false;
		    if( !visitExpression( ((IASTConditionalExpression)expression).getPositiveResultExpression(), action ) ) return false;
		} else if( expression instanceof IASTExpressionList ){
			IASTExpression[] list = ((IASTExpressionList)expression).getExpressions();
			for( int i = 0; i < list.length; i++){
			    if( list[i] == null ) break;
			    if( !visitExpression( list[i], action ) ) return false;
			}
		} else if( expression instanceof IASTFieldReference ){
		    if( !visitExpression( ((IASTFieldReference)expression).getFieldOwner(), action ) ) return false;
		    if( !visitName( ((IASTFieldReference)expression).getFieldName(), action ) ) return false;
		} else if( expression instanceof IASTFunctionCallExpression ){
		    if( !visitExpression( ((IASTFunctionCallExpression)expression).getFunctionNameExpression(), action ) ) return false;
		    if( ((IASTFunctionCallExpression)expression).getParameterExpression() != null )
		    	if( !visitExpression( ((IASTFunctionCallExpression)expression).getParameterExpression(), action ) ) return false;
		} else if( expression instanceof IASTIdExpression ){
		    if( !visitName( ((IASTIdExpression)expression).getName(), action ) ) return false;
		} else if( expression instanceof IASTTypeIdExpression ){
		    if( !visitTypeId( ((IASTTypeIdExpression)expression).getTypeId(), action ) ) return false;
		} else if( expression instanceof IASTCastExpression ){
		    if( !visitTypeId( ((IASTCastExpression)expression).getTypeId(), action ) ) return false;
		    if( !visitExpression( ((IASTCastExpression)expression).getOperand(), action ) ) return false;
		} else if( expression instanceof IASTUnaryExpression ){
		    if( !visitExpression( ((IASTUnaryExpression)expression).getOperand(), action ) ) return false;
		} else if( expression instanceof IGNUASTCompoundStatementExpression ){
		    if( !visitStatement( ((IGNUASTCompoundStatementExpression)expression).getCompoundStatement(), action ) ) return false;
		} else if( expression instanceof ICPPASTDeleteExpression ){
			if( !visitExpression( ((ICPPASTDeleteExpression)expression).getOperand(), action ) )  return false;
		} else if( expression instanceof ICPPASTNewExpression ) {
			ICPPASTNewExpression newExp = (ICPPASTNewExpression) expression;
			if( newExp.getNewPlacement() != null )
				if( !visitExpression( newExp.getNewPlacement(), action ) ) return false;
			if( newExp.getTypeId() != null )
				if( !visitTypeId( newExp.getTypeId(), action ) ) return false;
			IASTExpression [] exps = newExp.getNewTypeIdArrayExpressions();
			for( int i = 0; i < exps.length; i++ ){
				if( !visitExpression( exps[i], action ) ) return false;
			}
			if( newExp.getNewInitializer() != null )
				if( !visitExpression( newExp.getNewInitializer(), action ) ) return false;
		} else if( expression instanceof ICPPASTSimpleTypeConstructorExpression ){
			if( !visitExpression( ((ICPPASTSimpleTypeConstructorExpression)expression).getInitialValue(), action ) ) return false;
		} else if( expression instanceof ICPPASTTypenameExpression ){
			if( !visitName( ((ICPPASTTypenameExpression)expression).getName(), action ) ) return false;
			if( !visitExpression( ((ICPPASTTypenameExpression)expression).getInitialValue(), action ) )  return false;
		}
		return true;	
	}
	
	/**
	 * @param typeId
	 * @param action
	 * @return
	 */
	public static boolean visitTypeId(IASTTypeId typeId, CPPBaseVisitorAction action) {
		if( action.processTypeIds ){
		    switch( action.processTypeId( typeId ) ){
	            case CPPBaseVisitorAction.PROCESS_ABORT : return false;
	            case CPPBaseVisitorAction.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
		if( !visitDeclSpecifier( typeId.getDeclSpecifier(), action ) ) return false;
		if( !visitDeclarator( typeId.getAbstractDeclarator(), action ) ) return false;
		return true;
	}

	/**
	 * @param initializer
	 * @param action
	 * @return
	 */
	public static boolean visitInitializer(IASTInitializer initializer, CPPBaseVisitorAction action) {
		if( action.processInitializers ){
		    switch( action.processInitializer( initializer ) ){
	            case CPPBaseVisitorAction.PROCESS_ABORT : return false;
	            case CPPBaseVisitorAction.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
			
	    if( initializer instanceof IASTInitializerExpression ){
	        if( !visitExpression( ((IASTInitializerExpression) initializer).getExpression(), action ) ) return false;
	    } else if( initializer instanceof IASTInitializerList ){
	        IASTInitializer [] list = ((IASTInitializerList) initializer).getInitializers();
	        for( int i = 0; i < list.length; i++ ){
	            if( !visitInitializer( list[i], action ) ) return false;
	        }
	    } else if( initializer instanceof ICPPASTConstructorInitializer ){
	    	if( !visitExpression( ((ICPPASTConstructorInitializer) initializer).getExpression(), action ) ) return false;
	    }
	    return true;
	}

	/**
	 * @param enumerator
	 * @param action
	 * @return
	 */
	public static boolean visitEnumerator(IASTEnumerator enumerator, CPPBaseVisitorAction action) {
		if( action.processEnumerators ){
		    switch( action.processEnumerator( enumerator ) ){
	            case CPPBaseVisitorAction.PROCESS_ABORT : return false;
	            case CPPBaseVisitorAction.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
			
	    if( !visitName( enumerator.getName(), action ) ) return false;
	    if( enumerator.getValue() != null )
	        if( !visitExpression( enumerator.getValue(), action ) ) return false;
	    return true;
	}

	/**
	 * @param specifier
	 * @param action
	 * @return
	 */
	public static boolean visitBaseSpecifier(ICPPASTBaseSpecifier specifier, CPPBaseVisitorAction action) {
		if( action.processBaseSpecifiers ){
		    switch( action.processBaseSpecifier( specifier ) ){
	            case CPPBaseVisitorAction.PROCESS_ABORT : return false;
	            case CPPBaseVisitorAction.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
			
	    if( !visitName( specifier.getName(), action ) ) return false;
	    return true;
	}

	public static boolean visitParameterDeclaration( IASTParameterDeclaration parameterDeclaration, CPPBaseVisitorAction action ){
	    if( action.processParameterDeclarations ){
	    	switch( action.processParameterDeclaration( parameterDeclaration ) ){
		        case CPPBaseVisitorAction.PROCESS_ABORT : return false;
		        case CPPBaseVisitorAction.PROCESS_SKIP  : return true;
		        default : break;
		    }
	    }
	    
	    if( !visitDeclSpecifier( parameterDeclaration.getDeclSpecifier(), action ) ) return false;
	    if( !visitDeclarator( parameterDeclaration.getDeclarator(), action ) ) return false;
	    return true;
	}
	
	/**
	 * @param parameter
	 * @param action
	 * @return
	 */
	public static boolean visitTemplateParameter(ICPPASTTemplateParameter parameter, CPPBaseVisitorAction action) {
		return true;
	}

	/**
	 * Generate a function type for an implicit function.
	 * NOTE: This does not currectly handle parameters with typedef types.
	 * @param returnType
	 * @param parameters
	 * @return
	 */
	
	public static IFunctionType createImplicitFunctionType( IType returnType, IParameter [] parameters ){
	    IType [] pTypes = new IType[ parameters.length ];
	    IType pt = null;
	    
	    ArrayList temp = new ArrayList();
	    for( int i = 0; i < parameters.length; i++ ){
	        try {
                pt = parameters[i].getType();
            } catch ( DOMException e ) {
                pt = e.getProblem();
            }
	        
	        temp.add( pt.clone() );
	        while( pt instanceof ITypeContainer){
	            try {
                    pt = ((ITypeContainer)pt).getType();
                } catch ( DOMException e1 ) {
                    pt = e1.getProblem();
                }
	            if( pt instanceof ITypeContainer && !(pt instanceof ITypedef) ){
		            IType t = (IType) pt.clone();
		            ((ITypeContainer) temp.get( temp.size() - 1 )).setType( t );
		            temp.add( t );
	            } else {
	                temp.add( pt );
	                break;
	            }
	        }
	        int lastIdx = temp.size() - 1;
	        if( lastIdx > 0 && temp.get( lastIdx - 1 ) instanceof IQualifierType ){
	            temp.remove( --lastIdx );
	            if( lastIdx > 0 ){
	                ITypeContainer cont = (ITypeContainer) temp.get( lastIdx - 1 );
	                cont.setType( (IType) temp.get( lastIdx ) );
	            }
	        }
	        
	        IType lastType = (IType) temp.get( lastIdx );
	        if( lastType instanceof IArrayType ){
	            try {
                    lastType = new CPPPointerType( ((IArrayType) lastType).getType() );
                } catch ( DOMException e1 ) {
                    lastType = e1.getProblem();
                }
	        } else if( lastType instanceof IFunctionType ){
	            lastType = new CPPPointerType( lastType );
	        }
	        
	        pTypes[i] = (IType) temp.get( 0 ); 
	        
	    }
	    
	    return new CPPFunctionType( returnType, pTypes );
	}
	private static IType createType( IType returnType, ICPPASTFunctionDeclarator fnDtor ){
	    IASTParameterDeclaration [] params = fnDtor.getParameters();
	    IType [] pTypes = new IType [ params.length ];
	    IType pt = null;
	    
	    for( int i = 0; i < params.length; i++ ){
	        IASTDeclSpecifier pDeclSpec = params[i].getDeclSpecifier();
	        IASTDeclarator pDtor = params[i].getDeclarator();
	        //8.3.5-3 
	        //Any cv-qualifier modifying a parameter type is deleted.
	        //so only create the base type from the declspec and not the qualifiers
	        pt = getBaseType( pDeclSpec );
	        
	        pt = createType( pt, pDtor );
	        
	        //any parameter of type array of T is adjusted to be pointer to T
	        if( pt instanceof IArrayType ){
	            IArrayType at = (IArrayType) pt;
	            try {
                    pt = new CPPPointerType( at.getType() );
                } catch ( DOMException e ) {
                    pt = e.getProblem();
                }
	        }
	        
	        //any parameter to type function returning T is adjusted to be pointer to function
	        if( pt instanceof IFunctionType ){
	            pt = new CPPPointerType( pt );
	        }
	        
	        pTypes[i] = pt;
	    }
	    
	    returnType = getPointerTypes( returnType, fnDtor );
	    
	    IType type = new CPPFunctionType( returnType, pTypes );
	    IASTDeclarator nested = fnDtor.getNestedDeclarator();
	    if( nested != null ) {
	    	return createType( type, nested );
	    }
	    return type;
	}
	
	/**
	 * @param declarator
	 * @return
	 */
	private static IType createType(IType baseType, IASTDeclarator declarator) {
	    if( declarator instanceof ICPPASTFunctionDeclarator )
	        return createType( baseType, (ICPPASTFunctionDeclarator)declarator );
		
		IType type = baseType;
		type = getPointerTypes( type, declarator );
		if( declarator instanceof IASTArrayDeclarator )
		    type = getArrayTypes( type, (IASTArrayDeclarator) declarator );

	    IASTDeclarator nested = declarator.getNestedDeclarator();
	    if( nested != null ) {
	    	return createType( type, nested );
	    }
	    return type;
	}

	private static IType getPointerTypes( IType type, IASTDeclarator declarator ){
	    IASTPointerOperator [] ptrOps = declarator.getPointerOperators();
		for( int i = ptrOps.length - 1; i >= 0; i-- ){
		    if( ptrOps[i] instanceof IASTPointer )
		        type = new CPPPointerType( type, (IASTPointer) ptrOps[i] );
		    else if( ptrOps[i] instanceof ICPPASTReferenceOperator )
		        type = new CPPReferenceType( type );
		}
		return type;
	}
	private static IType getArrayTypes( IType type, IASTArrayDeclarator declarator ){
	    IASTArrayModifier [] mods = declarator.getArrayModifiers();
	    for( int i = 0; i < mods.length; i++ ){
	        type = new CPPArrayType( type );
	    }
	    return type;
	}
	
	/**
	 * @param declarator
	 * @return
	 */
	public static IType createType(IASTDeclarator declarator) {
		IASTDeclSpecifier declSpec = null;
		
		IASTNode node = declarator.getParent();
		while( node instanceof IASTDeclarator ){
			declarator = (IASTDeclarator) node;
			node = node.getParent();
		}
		
		if( node instanceof IASTParameterDeclaration )
			declSpec = ((IASTParameterDeclaration) node).getDeclSpecifier();
		else if( node instanceof IASTSimpleDeclaration )
			declSpec = ((IASTSimpleDeclaration)node).getDeclSpecifier();
		else if( node instanceof IASTFunctionDefinition )
			declSpec = ((IASTFunctionDefinition)node).getDeclSpecifier();
	
		IType type = createType( declSpec );
		type = createType( type, declarator );
		return type;
	}
	/**
	 * @param declSpec
	 * @return
	 */
	public static IType createType(IASTDeclSpecifier declSpec ) {
	    IType type = getBaseType( declSpec );
		
		if( type != null && ( declSpec.isConst() || declSpec.isVolatile() ) ){
		    type = new CPPQualifierType( type, declSpec.isConst(), declSpec.isVolatile() );
		}
		return type;
	}

	private static IType getBaseType( IASTDeclSpecifier declSpec ){
	    IType type = null;
	    if( declSpec instanceof ICPPASTCompositeTypeSpecifier ){
			IBinding binding = ((ICPPASTCompositeTypeSpecifier) declSpec).getName().resolveBinding();
			if( binding instanceof IType) 
				type = (IType) binding;
	    } else if( declSpec instanceof ICPPASTNamedTypeSpecifier ){
	    	IBinding binding = ((ICPPASTNamedTypeSpecifier)declSpec).getName().resolveBinding();
	    	if( binding instanceof IType )
	    		type = (IType) binding;
		} else if( declSpec instanceof ICPPASTElaboratedTypeSpecifier ){
			IBinding binding = ((ICPPASTElaboratedTypeSpecifier)declSpec).getName().resolveBinding();
			if( binding instanceof IType )
				type = (IType) binding;
		} else if( declSpec instanceof IASTEnumerationSpecifier ){
			IBinding binding = ((IASTEnumerationSpecifier)declSpec).getName().resolveBinding();
			if( binding instanceof IType )
				type = (IType) binding;
		} else if( declSpec instanceof ICPPASTSimpleDeclSpecifier ){
			ICPPASTSimpleDeclSpecifier spec = (ICPPASTSimpleDeclSpecifier) declSpec;
			int bits = ( spec.isLong()     ? CPPBasicType.IS_LONG  : 0 ) &
					   ( spec.isShort()    ? CPPBasicType.IS_SHORT : 0 ) &
					   ( spec.isSigned()   ? CPPBasicType.IS_SIGNED: 0 ) &
					   ( spec.isUnsigned() ? CPPBasicType.IS_SHORT : 0 );
			if( spec instanceof IGPPASTSimpleDeclSpecifier ){
				IGPPASTSimpleDeclSpecifier gspec = (IGPPASTSimpleDeclSpecifier) spec;
				bits &= ( gspec.isLongLong() ? GPPBasicType.IS_LONGLONG : 0 );
				type = new GPPBasicType( spec.getType(), bits, getExpressionType(gspec.getTypeofExpression()) );
			} else {
			    type = new CPPBasicType( spec.getType(), bits );
			}
		}
		return type;
	}
	/**
	 * @param expression
	 * @return
	 */
	public static IType getExpressionType(IASTExpression expression) {
		if( expression == null )
			return null;
	    if( expression instanceof IASTIdExpression ){
	        IBinding binding = resolveBinding( expression );
			if( binding instanceof IVariable ){
				try {
                    return ((IVariable)binding).getType();
                } catch ( DOMException e ) {
                    return e.getProblem();
                }
			}
	    } else if( expression instanceof IASTCastExpression ){
	        IASTTypeId id = ((IASTCastExpression)expression).getTypeId();
	        IType type = createType( id.getDeclSpecifier() );
	        return createType( type, id.getAbstractDeclarator() );
	    } else if( expression instanceof ICPPASTLiteralExpression ){
	    	switch( ((ICPPASTLiteralExpression) expression).getKind() ){
	    		case ICPPASTLiteralExpression.lk_this : break;
	    		case ICPPASTLiteralExpression.lk_true :
	    		case ICPPASTLiteralExpression.lk_false:
	    			return new CPPBasicType( ICPPBasicType.t_bool, 0 );
	    		case IASTLiteralExpression.lk_char_constant:
	    			return new CPPBasicType( IBasicType.t_char, 0 );
	    		case IASTLiteralExpression.lk_float_constant:
	    			return new CPPBasicType( IBasicType.t_float, 0 );
	    		case IASTLiteralExpression.lk_integer_constant:
	    			return new CPPBasicType( IBasicType.t_int, 0 );
	    		case IASTLiteralExpression.lk_string_literal:
	    			IType type = new CPPBasicType( IBasicType.t_char, 0 );
	    			return new CPPPointerType( type );
	    	}
	    	
	    } else if( expression instanceof IASTFunctionCallExpression ){
	        IBinding binding = resolveBinding( expression );
	        if( binding instanceof IFunction ){
	            IFunctionType fType;
                try {
                    fType = ((IFunction)binding).getType();
                    if( fType != null )
    	                return fType.getReturnType();
                } catch ( DOMException e ) {
                    return e.getProblem();
                }
                
	        }
	    }
	    else if( expression instanceof IASTUnaryExpression )
	    {
	       if( ((IASTUnaryExpression)expression).getOperator() == IASTUnaryExpression.op_bracketedPrimary )
	           return getExpressionType(((IASTUnaryExpression)expression).getOperand() );
	    }
	    return null;
	}
	
	public static IASTProblem[] getProblems(IASTTranslationUnit tu) {
		CollectProblemsAction action = new CollectProblemsAction();
		visitTranslationUnit(tu, action);
		
		return action.getProblems();
	}

}
