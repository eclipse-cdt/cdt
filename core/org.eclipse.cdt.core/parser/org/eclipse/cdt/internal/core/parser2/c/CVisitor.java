
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

import org.eclipse.cdt.core.dom.ast.IASTArraySubscriptExpression;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTCaseStatement;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTConditionalExpression;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTDoStatement;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionList;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTGotoStatement;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTLabelStatement;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTSwitchStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTUnaryTypeIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTWhileStatement;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.c.ICASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTTypeIdInitializerExpression;
import org.eclipse.cdt.core.dom.ast.c.ICASTTypedefNameSpecifier;
import org.eclipse.cdt.core.dom.ast.gnu.IGNUASTCompoundStatementExpression;
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
		IBinding binding = resolveBinding( parameterDeclaration, CURRENT_SCOPE );
		if( binding == null )
			binding = new CParameter( parameterDeclaration );
		return binding;
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
		} else if( node instanceof IASTParameterDeclaration ){
			IASTParameterDeclaration param = (IASTParameterDeclaration) node;
			IASTFunctionDeclarator fDtor = (IASTFunctionDeclarator) param.getParent();
			if( fDtor.getParent() instanceof IASTFunctionDefinition ){
				return null;
			}
			IASTFunctionDeclarator fdef = findDefinition( fDtor );
			if( fdef != null ){
				int index = fDtor.getParameters().indexOf( param );
				if( index >= 0 && index < fdef.getParameters().size() ){
					IASTParameterDeclaration pdef = (IASTParameterDeclaration) fdef.getParameters().get( index );
					return pdef.getDeclarator().getName().resolveBinding();
				}
			}
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
	
	public static void clearBindings( IASTTranslationUnit tu ){
		List decls = tu.getDeclarations();
		for( int i = 0; i < decls.size(); i++ ){
			clearBindings( (IASTDeclaration) decls.get(i) );
		}
	}
	private static void clearBindings( IASTDeclaration declaration ){
		if( declaration instanceof IASTSimpleDeclaration ){
			IASTSimpleDeclaration simpleDecl = (IASTSimpleDeclaration) declaration;
			clearBindings( simpleDecl.getDeclSpecifier() );
			List list = simpleDecl.getDeclarators();
			for( int i = 0; i < list.size(); i++ ){
				clearBindings( (IASTDeclarator) list.get(i) );
			}
		} else if( declaration instanceof IASTFunctionDefinition ){
			IASTFunctionDefinition fnDef = (IASTFunctionDefinition) declaration;
			clearBindings( fnDef.getDeclSpecifier() );
			clearBindings( fnDef.getDeclarator() );
			clearBindings( fnDef.getBody() );
		}
	}
	private static void clearBindings( IASTDeclarator declarator ){
		((CASTName)declarator.getName()).setBinding( null );
		
		if( declarator.getNestedDeclarator() != null )
			clearBindings( declarator.getNestedDeclarator() );
		
		//TODO: if( declarator.getInitializer() != null )
		
		if( declarator instanceof IASTFunctionDeclarator ){
			List list = ((IASTFunctionDeclarator)declarator).getParameters();
			for( int i = 0; i < list.size(); i++ ){
				IASTParameterDeclaration param = (IASTParameterDeclaration) list.get(i);
				clearBindings( param.getDeclarator() );
			}
		}
	}
	private static void clearBindings( IASTDeclSpecifier declSpec ){
		if( declSpec instanceof ICASTCompositeTypeSpecifier ){
			ICASTCompositeTypeSpecifier compTypeSpec = (ICASTCompositeTypeSpecifier) declSpec;
			((CASTName) compTypeSpec.getName()).setBinding( null );
			
			List list = compTypeSpec.getMembers();
			for( int i = 0; i < list.size(); i++ ){
				clearBindings( (IASTDeclaration) list.get(i) );
			}
		}
	}
	private static void clearBindings( IASTStatement statement ){
		if( statement instanceof IASTCompoundStatement ){
			List list = ((IASTCompoundStatement) statement).getStatements();
			for( int i = 0; i < list.size(); i++ ){
				clearBindings( (IASTStatement) list.get(i) );
			}
		} else if( statement instanceof IASTDeclarationStatement ){
			clearBindings( ((IASTDeclarationStatement)statement).getDeclaration() );
		} else if( statement instanceof IASTExpressionStatement ){
			clearBindings( ((IASTExpressionStatement)statement).getExpression() );
		} else if( statement instanceof IASTCaseStatement ){
			clearBindings( ((IASTCaseStatement)statement).getExpression() );
		} else if( statement instanceof IASTDoStatement ){
			clearBindings( ((IASTDoStatement)statement).getBody() );
		} else if( statement instanceof IASTGotoStatement ){
			((CASTName) ((IASTGotoStatement)statement).getName()).setBinding( null );
		} else if( statement instanceof IASTIfStatement ){
			clearBindings( ((IASTIfStatement) statement ).getCondition() );
			clearBindings( ((IASTIfStatement) statement ).getThenClause() );
			clearBindings( ((IASTIfStatement) statement ).getElseClause() );
		} else if( statement instanceof IASTLabelStatement ){
			((CASTName) ((IASTLabelStatement)statement).getName()).setBinding( null );
		} else if( statement instanceof IASTReturnStatement ){
			clearBindings( ((IASTReturnStatement) statement ).getReturnValue() );
		} else if( statement instanceof IASTSwitchStatement ){
			clearBindings( ((IASTSwitchStatement) statement ).getController() );
			clearBindings( ((IASTSwitchStatement) statement ).getBody() );
		} else if( statement instanceof IASTWhileStatement ){
			clearBindings( ((IASTWhileStatement) statement ).getCondition() );
			clearBindings( ((IASTWhileStatement) statement ).getBody() );
		}
	}
	private static void clearBindings( IASTTypeId typeId ){
		clearBindings( typeId.getAbstractDeclarator() );
		clearBindings( typeId.getDeclSpecifier() );		
	}
	private static void clearBindings( IASTExpression expression ){
		if( expression instanceof IASTArraySubscriptExpression ){
			clearBindings( ((IASTArraySubscriptExpression)expression).getArrayExpression() );
			clearBindings( ((IASTArraySubscriptExpression)expression).getSubscriptExpression() );
		} else if( expression instanceof IASTBinaryExpression ){
			clearBindings( ((IASTBinaryExpression)expression).getOperand1() );
			clearBindings( ((IASTBinaryExpression)expression).getOperand2() );
		} else if( expression instanceof IASTConditionalExpression){
			clearBindings( ((IASTConditionalExpression)expression).getLogicalConditionExpression() );
			clearBindings( ((IASTConditionalExpression)expression).getNegativeResultExpression() );
			clearBindings( ((IASTConditionalExpression)expression).getPositiveResultExpression() );
		} else if( expression instanceof IASTExpressionList ){
			List list = ((IASTExpressionList)expression).getExpressions();
			for( int i = 0; i < list.size(); i++){
				clearBindings( (IASTExpression) list.get(i) );
			}
		} else if( expression instanceof IASTFieldReference ){
			clearBindings( ((IASTFieldReference)expression).getFieldOwner() );
			((CASTName) ((IASTFieldReference)expression).getFieldName()).setBinding( null );
		} else if( expression instanceof IASTFunctionCallExpression ){
			clearBindings( ((IASTFunctionCallExpression)expression).getFunctionNameExpression() );
			clearBindings( ((IASTFunctionCallExpression)expression).getParameterExpression() );
		} else if( expression instanceof IASTIdExpression ){
			((CASTName) ((IASTIdExpression)expression).getName()).setBinding( null );
		} else if( expression instanceof IASTTypeIdExpression ){
			clearBindings( ((IASTTypeIdExpression)expression).getTypeId() );
		} else if( expression instanceof IASTUnaryExpression ){
			clearBindings( ((IASTUnaryExpression)expression).getOperand() );
		} else if( expression instanceof IASTUnaryTypeIdExpression ){
			clearBindings( ((IASTUnaryTypeIdExpression)expression).getOperand() );
			clearBindings( ((IASTUnaryTypeIdExpression)expression).getTypeId() );
		} else if( expression instanceof ICASTTypeIdInitializerExpression ){
			clearBindings( ((ICASTTypeIdInitializerExpression)expression).getTypeId() );
			//TODO: ((ICASTTypeIdInitializerExpression)expression).getInitializer();
		} else if( expression instanceof IGNUASTCompoundStatementExpression ){
			clearBindings( ((IGNUASTCompoundStatementExpression)expression).getCompoundStatement() );
		}
	}
	
}
