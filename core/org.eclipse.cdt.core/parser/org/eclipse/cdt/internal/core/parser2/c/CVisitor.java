
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

import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.c.ICASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTTypedefNameSpecifier;
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
		} else if( parent instanceof ICASTTypedefNameSpecifier ){
			binding = resolveBinding( parent );
		} else if( parent instanceof IASTFieldReference ){
			binding = findBinding( (IASTFieldReference) parent );
		} else if( parent instanceof CASTDeclarator ){
			binding = createBinding( (CASTDeclarator) parent, name );
		} else if( parent instanceof CASTCompositeTypeSpecifier ){
			binding = createBinding( (CASTCompositeTypeSpecifier) parent );
		} else if( parent instanceof ICASTElaboratedTypeSpecifier ){
			binding = createBinding( (ICASTElaboratedTypeSpecifier) parent );
		}
		name.setBinding( binding );
	}

	private static IBinding createBinding( ICASTElaboratedTypeSpecifier elabTypeSpec ){
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
		if( fieldOwner instanceof IASTIdExpression ){
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
	private static IBinding createBinding(IASTDeclarator declarator, CASTName name) {
		IBinding binding = null;
		IASTNode parent = declarator.getParent();
		if( declarator instanceof IASTFunctionDeclarator ){
			binding = resolveBinding( parent, CURRENT_SCOPE );
			if( binding == null )
				binding = new CFunction( (IASTFunctionDeclarator) declarator );
		} else if( parent instanceof IASTSimpleDeclaration ){
			binding = createBinding( (IASTSimpleDeclaration) parent, name );
		} else if( parent instanceof IASTParameterDeclaration ){
			binding = createBinding( (IASTParameterDeclaration ) parent );
		}
		
		return binding;
	}

	private static IBinding createBinding( ICASTCompositeTypeSpecifier compositeTypeSpec ){
		IBinding binding = resolveBinding( compositeTypeSpec, CURRENT_SCOPE );
		if( binding == null )
			binding = new CStructure( compositeTypeSpec );
		return binding;
	}

	/**
	 * @param parent
	 * @return
	 */
	private static IBinding createBinding(IASTSimpleDeclaration simpleDeclaration, IASTName name) {
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

	private static IBinding createBinding( IASTParameterDeclaration parameterDeclaration ){
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
			IASTNode blockItem = getContainingBlockItem( node );
			//return name.resolveBinding();
			return findBinding( blockItem, (CASTName) name, scopeDepth );
		} else if( node instanceof IASTIdExpression ){
			IASTNode blockItem = getContainingBlockItem( node );
			return findBinding( blockItem, (CASTName) ((IASTIdExpression)node).getName(), scopeDepth );
		} else if( node instanceof ICASTTypedefNameSpecifier ){
			IASTNode blockItem = getContainingBlockItem( node );
			return findBinding( blockItem, (CASTName) ((ICASTTypedefNameSpecifier)node).getName(), scopeDepth );
		} else if( node instanceof ICASTElaboratedTypeSpecifier ){
			IASTNode blockItem = getContainingBlockItem( node );
			return findBinding( blockItem, (CASTName) ((ICASTElaboratedTypeSpecifier)node).getName(), scopeDepth );
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
		if( parent instanceof IASTCompoundStatement ){
			return getContainingScope( (IASTStatement)parent );
		} else if( parent instanceof IASTFunctionDefinition ){
			IASTFunctionDeclarator fnDeclarator = ((IASTFunctionDefinition) parent ).getDeclarator();
			IFunction function = (IFunction) fnDeclarator.getName().resolveBinding();
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
		if( parent instanceof IASTFunctionDeclarator ){
			IASTFunctionDeclarator functionDeclarator = (IASTFunctionDeclarator) parent;
			IASTName fnName = functionDeclarator.getName();
			IFunction function = (IFunction) fnName.resolveBinding();
			return function.getFunctionScope();
		}
		
		return null;
	}
	
	private static IASTNode getContainingBlockItem( IASTNode node ){
		IASTNode parent = node.getParent();
		if( parent instanceof IASTDeclaration ){
			IASTNode p = parent.getParent();
			if( p instanceof IASTDeclarationStatement )
				return p;
			return parent;
		}
		//if parent is something that can contain a declaration
		else if ( parent instanceof IASTCompoundStatement || 
				  parent instanceof IASTTranslationUnit   ||
				  parent instanceof IASTForStatement )
		{
			return node;
		}
		
		return getContainingBlockItem( parent );
	}
	
	protected static IBinding findBinding( IASTNode blockItem, CASTName name, int scopeDepth ){
		IBinding binding = null;
		while( blockItem != null ){
			
			IASTNode parent = blockItem.getParent();
			List list = null;
			if( parent instanceof IASTCompoundStatement ){
				IASTCompoundStatement compound = (IASTCompoundStatement) parent;
				list = compound.getStatements();
			} else if ( parent instanceof IASTTranslationUnit ){
				IASTTranslationUnit translation = (IASTTranslationUnit) parent;
				list = translation.getDeclarations();
			}
			
			if( list != null ){
				for( int i = 0; i < list.size(); i++ ){
					IASTNode node = (IASTNode) list.get(i);
					if( node == blockItem )
						break;
					if( node instanceof IASTDeclarationStatement ){
						IASTDeclarationStatement declStatement = (IASTDeclarationStatement) node;
						binding = checkForBinding( declStatement.getDeclaration(), name );
					} else if( node instanceof IASTDeclaration ){
						binding = checkForBinding( (IASTDeclaration) node, name );
					}
					if( binding != null )
						return binding;
				}
			} else {
				//check the parent
				if( parent instanceof IASTDeclaration ){
					binding = checkForBinding( (IASTDeclaration) parent, name );
					if( binding != null )
						return binding;
				} else if( parent instanceof IASTStatement ){
					binding = checkForBinding( (IASTStatement) parent, name );
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
	
	private static IBinding checkForBinding( IASTDeclaration declaration, CASTName name ){
		if( declaration instanceof IASTSimpleDeclaration ){
			IASTSimpleDeclaration simpleDeclaration = (IASTSimpleDeclaration) declaration;
			List declarators = simpleDeclaration.getDeclarators();
			int size = declarators.size();
			if( size > 0 ){
				for( int i = 0; i < declarators.size(); i++ ){
					IASTDeclarator declarator = (IASTDeclarator) declarators.get(i);
					CASTName declaratorName = (CASTName) declarator.getName();
					if( CharArrayUtils.equals( declaratorName.toCharArray(), name.toCharArray() ) ){
						return declaratorName.resolveBinding();
					}
				}
			} else {
				//forward declaration
				IASTDeclSpecifier declSpec = simpleDeclaration.getDeclSpecifier();
				if( declSpec instanceof ICASTElaboratedTypeSpecifier ){
					CASTName elabName = (CASTName) ((ICASTElaboratedTypeSpecifier)declSpec).getName();
					if( CharArrayUtils.equals( elabName.toCharArray(), name.toCharArray() ) ){
						return elabName.resolveBinding();
					}
				}
			}
		} else if( declaration instanceof IASTFunctionDefinition ){
			IASTFunctionDefinition functionDef = (IASTFunctionDefinition) declaration;
			IASTFunctionDeclarator declarator = functionDef.getDeclarator();
			
			//check the function itself
			CASTName declName = (CASTName) declarator.getName();
			if( CharArrayUtils.equals( declName.toCharArray(), name.toCharArray() ) ){
				return declName.resolveBinding();
			}
			//check the parameters
			List parameters = declarator.getParameters();
			for( int i = 0; i < parameters.size(); i++ ){
				IASTParameterDeclaration parameterDeclaration = (IASTParameterDeclaration) parameters.get(i);
				declName = (CASTName) parameterDeclaration.getDeclarator().getName();
				if( CharArrayUtils.equals( declName.toCharArray(), name.toCharArray() ) ){
					return declName.resolveBinding();
				}
			}
		}
		return null;
	}
	
	private static IBinding checkForBinding( IASTStatement statement, CASTName name ){
		if( statement instanceof IASTForStatement ){
			IASTForStatement forStatement = (IASTForStatement) statement;
			if( forStatement.getInitDeclaration() != null ){
				return checkForBinding( forStatement.getInitDeclaration(), name );
			}
		}
		
		return null;
	}
	
	protected static IASTFunctionDeclarator findDefinition( IASTFunctionDeclarator declarator ){
		return (IASTFunctionDeclarator) findDefinition( declarator, declarator.getName().toString() );
	}
	protected static IASTDeclSpecifier findDefinition( ICASTElaboratedTypeSpecifier declSpec ){
		String elabName = declSpec.getName().toString();
		return (IASTDeclSpecifier) findDefinition(declSpec, elabName);
	}

	private static IASTNode findDefinition(IASTNode decl, String declName) {
		IASTNode blockItem = getContainingBlockItem( decl );
		IASTNode parent = blockItem.getParent();
		List list = null;
		if( parent instanceof IASTCompoundStatement ){
			IASTCompoundStatement compound = (IASTCompoundStatement) parent;
			list = compound.getStatements();
		} else if ( parent instanceof IASTTranslationUnit ){
			IASTTranslationUnit translation = (IASTTranslationUnit) parent;
			list = translation.getDeclarations();
		}
		if( list != null ){
			for( int i = 0; i < list.size(); i++ ){
				IASTNode node = (IASTNode) list.get(i);
				if( node == blockItem )
					continue;
				if( node instanceof IASTDeclarationStatement ){
					node = ((IASTDeclarationStatement) node).getDeclaration();
				}
				
				if( node instanceof IASTFunctionDefinition && decl instanceof IASTFunctionDeclarator ){
					IASTFunctionDeclarator dtor = ((IASTFunctionDefinition) node).getDeclarator();
					IASTName name = dtor.getName();
					if( name.toString().equals( declName )){
						return dtor;
					}
				} else if( node instanceof IASTSimpleDeclaration && decl instanceof ICASTElaboratedTypeSpecifier){
					IASTSimpleDeclaration simpleDecl = (IASTSimpleDeclaration) node;
					if( simpleDecl.getDeclSpecifier() instanceof ICASTCompositeTypeSpecifier ){
						ICASTCompositeTypeSpecifier compTypeSpec = (ICASTCompositeTypeSpecifier) simpleDecl.getDeclSpecifier();
						IASTName name = compTypeSpec.getName();
						if( name.toString().equals( declName ) ){
							return compTypeSpec;
						}
					}
				}
			}
		}
		return null;
	}
}
