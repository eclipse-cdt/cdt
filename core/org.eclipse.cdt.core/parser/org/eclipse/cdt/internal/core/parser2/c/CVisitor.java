
/**********************************************************************
 * Copyright (c) 2002-2004 IBM Canada and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation 
 **********************************************************************/
package org.eclipse.cdt.internal.core.parser2.c;

import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypedefNameSpecifier;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.c.ICASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;

/**
 * Created on Nov 5, 2004
 * @author aniefer
 */
public class CVisitor {
	
	private static final int COMPLETE = 1;
	private static final int CURRENT_SCOPE = 2;
	
	static protected void createBinding( CASTName name ){
		IBinding binding = null;
		IASTNode parent = name.getParent();
		
		if( parent instanceof CASTIdExpression ){
			binding = resolveBinding( parent );
		} else if( parent instanceof IASTTypedefNameSpecifier ){
			binding = resolveBinding( parent );
		} else if( parent instanceof IASTFieldReference ){
			binding = findBinding( (IASTFieldReference) parent );
		} else if( parent instanceof CASTDeclarator ){
			binding = createBinding( (CASTDeclarator) parent, name );
		} else if( parent instanceof CASTCompositeTypeSpecifier ){
			binding = createBinding( (CASTCompositeTypeSpecifier) parent );
		} else if( parent instanceof IASTElaboratedTypeSpecifier ){
			binding = createBinding( (IASTElaboratedTypeSpecifier) parent );
		}
		name.setBinding( binding );
	}

	private static IBinding createBinding( IASTElaboratedTypeSpecifier elabTypeSpec ){
		IASTNode parent = elabTypeSpec.getParent();
		if( parent instanceof IASTSimpleDeclaration ){
			IASTSimpleDeclaration declaration = (IASTSimpleDeclaration) parent;
			if( declaration.getDeclarators().size() == 0 ){
				//forward declaration
				IBinding binding = resolveBinding( elabTypeSpec, CURRENT_SCOPE );
				if( binding == null )
					binding = new CStructure( elabTypeSpec );
				return binding;
			} 
			return resolveBinding( elabTypeSpec );
		}
		return null;
	}
	private static IBinding findBinding( IASTFieldReference fieldReference ){
		IASTExpression fieldOwner = fieldReference.getFieldOwner();
		ICompositeType compositeType = null;
		if( fieldOwner instanceof CASTIdExpression ){
			IBinding binding = resolveBinding( fieldOwner );
			if( binding instanceof IVariable ){
				binding = ((IVariable)binding).getType();
				while( binding != null && binding instanceof ITypedef )
					binding = ((ITypedef)binding).getType();
			}
			if( binding instanceof ICompositeType )
				compositeType = (ICompositeType) binding;
		}

		IBinding binding = null;
		if( compositeType != null ){
			binding = compositeType.findField( fieldReference.getFieldName().toString() );
		}
		return binding;
	}
	
	/**
	 * @param parent
	 * @return
	 */
	private static IBinding createBinding(CASTDeclarator declarator, CASTName name) {
		IASTNode parent = declarator.getParent();
		
		if( parent instanceof CASTSimpleDeclaration ){
			return createBinding( (CASTSimpleDeclaration) parent, name );
		} else if( parent instanceof CASTFunctionDefinition ){
			return createBinding( (CASTFunctionDefinition) parent );
		} else if( parent instanceof CASTParameterDeclaration ){
			return createBinding( (CASTParameterDeclaration ) parent );
		}
		
		return null;
	}

	private static IBinding createBinding( CASTCompositeTypeSpecifier compositeTypeSpec ){
		IBinding binding = resolveBinding( compositeTypeSpec, CURRENT_SCOPE );
		if( binding == null )
			binding = new CStructure( compositeTypeSpec );
		return binding;
	}
	/**
	 * @param definition
	 * @return
	 */
	private static IBinding createBinding(CASTFunctionDefinition functionDefinition) {
		//TODO need to check for forward declaration first
		CFunction function = new CFunction( functionDefinition );
		return function;
	}

	/**
	 * @param parent
	 * @return
	 */
	private static IBinding createBinding(CASTSimpleDeclaration simpleDeclaration, CASTName name) {
		IBinding binding = null;
		if( simpleDeclaration.getDeclSpecifier().getStorageClass() == IASTDeclSpecifier.sc_typedef ){
			binding = new CTypeDef( name );
		} else if( simpleDeclaration.getParent() instanceof ICASTCompositeTypeSpecifier ){
			binding = new CField( name );
		} else {
			binding = new CVariable( name );
		}
 
		return binding;
	}

	private static IBinding createBinding( CASTParameterDeclaration parameterDeclaration ){
		CParameter parameter = new CParameter( parameterDeclaration );
		return parameter;
	}
	
	protected static IBinding resolveBinding( IASTNode node ){
		return resolveBinding( node, COMPLETE );
	}
	protected static IBinding resolveBinding( IASTNode node, int scopeDepth ){
		if( node instanceof IASTFunctionDefinition ){
			IASTFunctionDefinition functionDef = (IASTFunctionDefinition) node;
			IASTFunctionDeclarator functionDeclartor = functionDef.getDeclarator();
			IASTName name = functionDeclartor.getName();
			return name.resolveBinding();
		} else if( node instanceof CASTIdExpression ){
			IASTNode blockItem = getContainingBlockItem( node );
			return findBinding( blockItem, (CASTName) ((CASTIdExpression)node).getName(), scopeDepth );
		} else if( node instanceof IASTTypedefNameSpecifier ){
			IASTNode blockItem = getContainingBlockItem( node );
			return findBinding( blockItem, (CASTName) ((IASTTypedefNameSpecifier)node).getName(), scopeDepth );
		} else if( node instanceof IASTElaboratedTypeSpecifier ){
			IASTNode blockItem = getContainingBlockItem( node );
			return findBinding( blockItem, (CASTName) ((IASTElaboratedTypeSpecifier)node).getName(), scopeDepth );
		} else if( node instanceof ICASTCompositeTypeSpecifier ){
			IASTNode blockItem = getContainingBlockItem( node );
			return findBinding( blockItem, (CASTName) ((ICASTCompositeTypeSpecifier)node).getName(), scopeDepth );
		}
		return null;
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
		}
		
		return null;
	}
	
	public static IScope getContainingScope( IASTStatement statement ){
		IASTNode parent = statement.getParent();
		if( parent instanceof CASTCompoundStatement ){
			return getContainingScope( (IASTStatement)parent );
		} else if( parent instanceof CASTFunctionDefinition ){
			IFunction function = (IFunction) resolveBinding( parent );
			return function.getFunctionScope();
		}
		
		return null;
	}
	
	public static IScope getContainingScope( IASTDeclSpecifier compTypeSpec ){
		return null;
	}

	/**
	 * @param parameterDeclaration
	 * @return
	 */
	public static IScope getContainingScope(IASTParameterDeclaration parameterDeclaration) {
		IASTNode parent = parameterDeclaration.getParent();
		if( parent instanceof CASTFunctionDeclarator ){
			CASTFunctionDeclarator functionDeclarator = (CASTFunctionDeclarator) parent;
			parent = functionDeclarator.getParent(); 
			IFunction function = (IFunction) resolveBinding( parent );
			return function.getFunctionScope();
		}
		
		return null;
	}
	
	private static IASTNode getContainingBlockItem( IASTNode node ){
		IASTNode parent = node.getParent();
		if( parent instanceof IASTDeclaration ){
			IASTNode p = parent.getParent();
			if( p instanceof CASTDeclarationStatement )
				return p;
			return parent;
		} else if ( parent instanceof CASTCompoundStatement || parent instanceof CASTTranslationUnit )
			return node;
		
		return getContainingBlockItem( parent );
	}
	
	protected static IBinding findBinding( IASTNode blockItem, CASTName name, int scopeDepth ){
		IBinding binding = null;
		while( blockItem != null ){
			
			IASTNode parent = blockItem.getParent();
			List list = null;
			if( parent instanceof CASTCompoundStatement ){
				CASTCompoundStatement compound = (CASTCompoundStatement) parent;
				list = compound.getStatements();
			} else if ( parent instanceof CASTTranslationUnit ){
				CASTTranslationUnit translation = (CASTTranslationUnit) parent;
				list = translation.getDeclarations();
			}
			
			if( list != null ){
				for( int i = 0; i < list.size(); i++ ){
					IASTNode node = (IASTNode) list.get(i);
					if( node == blockItem )
						break;
					if( node instanceof CASTDeclarationStatement ){
						CASTDeclarationStatement declStatement = (CASTDeclarationStatement) node;
						binding = findBinding( declStatement.getDeclaration(), name );
					} else if( node instanceof IASTDeclaration ){
						binding = findBinding( (IASTDeclaration) node, name );
					}
					if( binding != null )
						return binding;
				}
			} else {
				//check the parent
				if( parent instanceof IASTDeclaration ){
					binding = findBinding( (IASTDeclaration) parent, name );
					if( binding != null )
						return binding;
				}
			}
			if( scopeDepth == COMPLETE )
				blockItem = parent;
			else 
				blockItem = null;
		}
		
		return null;
	}
	
	private static IBinding findBinding( IASTDeclaration declaration, CASTName name ){
		if( declaration instanceof CASTSimpleDeclaration ){
			CASTSimpleDeclaration simpleDeclaration = (CASTSimpleDeclaration) declaration;
			List declarators = simpleDeclaration.getDeclarators();
			int size = declarators.size();
			if( size > 0 ){
				for( int i = 0; i < declarators.size(); i++ ){
					CASTDeclarator declarator = (CASTDeclarator) declarators.get(i);
					CASTName declaratorName = (CASTName) declarator.getName();
					if( CharArrayUtils.equals( declaratorName.toCharArray(), name.toCharArray() ) ){
						return declaratorName.resolveBinding();
					}
				}
			} else {
				//forward declaration
				IASTDeclSpecifier declSpec = simpleDeclaration.getDeclSpecifier();
				if( declSpec instanceof IASTElaboratedTypeSpecifier ){
					CASTName elabName = (CASTName) ((IASTElaboratedTypeSpecifier)declSpec).getName();
					if( CharArrayUtils.equals( elabName.toCharArray(), name.toCharArray() ) ){
						return elabName.resolveBinding();
					}
				}
			}
		} else if( declaration instanceof CASTFunctionDefinition ){
			CASTFunctionDefinition functionDef = (CASTFunctionDefinition) declaration;
			CASTFunctionDeclarator declarator = (CASTFunctionDeclarator) functionDef.getDeclarator();
			
			//check the function itself
			CASTName declName = (CASTName) declarator.getName();
			if( CharArrayUtils.equals( declName.toCharArray(), name.toCharArray() ) ){
				return declName.resolveBinding();
			}
			//check the parameters
			List parameters = declarator.getParameters();
			for( int i = 0; i < parameters.size(); i++ ){
				CASTParameterDeclaration parameterDeclaration = (CASTParameterDeclaration) parameters.get(i);
				declName = (CASTName) parameterDeclaration.getDeclarator().getName();
				if( CharArrayUtils.equals( declName.toCharArray(), name.toCharArray() ) ){
					return declName.resolveBinding();
				}
			}
		}
		return null;
	}
	
	protected static IASTDeclSpecifier findDefinition( IASTElaboratedTypeSpecifier declSpec ){
		String elabName = declSpec.getName().toString();
		IASTNode blockItem = getContainingBlockItem( declSpec );
		IASTNode parent = blockItem.getParent();
		List list = null;
		if( parent instanceof CASTCompoundStatement ){
			CASTCompoundStatement compound = (CASTCompoundStatement) parent;
			list = compound.getStatements();
		} else if ( parent instanceof CASTTranslationUnit ){
			CASTTranslationUnit translation = (CASTTranslationUnit) parent;
			list = translation.getDeclarations();
		}
		if( list != null ){
			for( int i = 0; i < list.size(); i++ ){
				IASTNode node = (IASTNode) list.get(i);
				if( node == blockItem )
					continue;
				if( node instanceof CASTDeclarationStatement ){
					node = ((CASTDeclarationStatement) node).getDeclaration();
				}
				
				if( node instanceof IASTSimpleDeclaration ){
					IASTSimpleDeclaration simpleDecl = (IASTSimpleDeclaration) node;
					if( simpleDecl.getDeclSpecifier() instanceof ICASTCompositeTypeSpecifier ){
						ICASTCompositeTypeSpecifier compTypeSpec = (ICASTCompositeTypeSpecifier) simpleDecl.getDeclSpecifier();
						IASTName name = compTypeSpec.getName();
						if( name.toString().equals( elabName ) ){
							return compTypeSpec;
						}
					}
				}
			}
		}
		return null;
	}
}
