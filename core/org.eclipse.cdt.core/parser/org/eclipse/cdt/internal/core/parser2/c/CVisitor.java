
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

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;

/**
 * Created on Nov 5, 2004
 * @author aniefer
 */
public class CVisitor {
	static protected void createBinding( CASTName name ){
		IBinding binding = null;
		IASTNode parent = name.getParent();
		
		if( parent instanceof CASTIdExpression ){
			binding = createBinding( (CASTIdExpression) parent );
		} else if( parent instanceof CASTDeclarator ){
			binding = createBinding( (CASTDeclarator) parent );
		} 
		name.setBinding( binding );
	}

	/**
	 * @param parent
	 * @return
	 */
	private static IBinding createBinding(CASTIdExpression idExpression) {
		IASTNode blockItem = getContainingBlockItem( idExpression );
		
		IBinding binding = findBinding( blockItem, (CASTName) idExpression.getName() );
		return binding;
	}

	/**
	 * @param parent
	 * @return
	 */
	private static IBinding createBinding(CASTDeclarator declarator) {
		IASTNode parent = declarator.getParent();
		
		if( parent instanceof CASTSimpleDeclaration ){
			return createBinding( (CASTSimpleDeclaration) parent );
		} else if( parent instanceof CASTFunctionDefinition ){
			return createBinding( (CASTFunctionDefinition) parent );
		} else if( parent instanceof CASTParameterDeclaration ){
			return createBinding( (CASTParameterDeclaration ) parent );
		}
		
		return null;
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
	private static IBinding createBinding(CASTSimpleDeclaration simpleDeclaration) {
		CVariable variable = new CVariable( simpleDeclaration );
		return variable;
	}

	private static IBinding createBinding( CASTParameterDeclaration parameterDeclaration ){
		CParameter parameter = new CParameter( parameterDeclaration );
		return parameter;
	}
	
	private static IBinding resolveBinding( IASTNode node ){
		if( node instanceof IASTFunctionDefinition ){
			IASTFunctionDefinition functionDef = (IASTFunctionDefinition) node;
			IASTFunctionDeclarator functionDeclartor = functionDef.getDeclarator();
			IASTName name = functionDeclartor.getName();
			return name.resolveBinding();
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
			else
				return parent;
		} else if ( parent instanceof CASTCompoundStatement || parent instanceof CASTTranslationUnit )
			return node;
		
		return getContainingBlockItem( parent );
	}
	
	private static IASTNode getPreviousItem( IASTNode blockItem ){
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
			int idx = list.indexOf( blockItem );
			if( idx <= 0 )
				return null;
			return (IASTNode) list.get( idx - 1);
		}
		return null;
	}
	
	protected static IBinding findBinding( IASTNode blockItem, CASTName name ){
		IASTNode previous = blockItem;
		IBinding binding = null;
		while( previous != null ){
			IASTNode temp = getPreviousItem( previous );
			
			if( temp != null ){
				previous = temp;
				if( previous instanceof CASTDeclarationStatement ){
					CASTDeclarationStatement declStatement = (CASTDeclarationStatement) previous;
					binding = findBinding( declStatement.getDeclaration(), name );
				} else if( previous instanceof IASTDeclaration ){
					binding = findBinding( (IASTDeclaration) previous, name );
				}	
			} else {
				//check the parent
				IASTNode parent = previous.getParent();
				if( parent instanceof IASTDeclaration ){
					binding = findBinding( (IASTDeclaration) parent, name );
				}
				previous = parent;
			}
			if( binding != null )
				return binding;
		}
		
		return null;
	}
	
	private static IBinding findBinding( IASTDeclaration declaration, CASTName name ){
		if( declaration instanceof CASTSimpleDeclaration ){
			CASTSimpleDeclaration simpleDeclaration = (CASTSimpleDeclaration) declaration;
			List declarators = simpleDeclaration.getDeclarators();
			for( int i = 0; i < declarators.size(); i++ ){
				CASTDeclarator declarator = (CASTDeclarator) declarators.get(i);
				CASTName declaratorName = (CASTName) declarator.getName();
				if( CharArrayUtils.equals( declaratorName.toCharArray(), name.toCharArray() ) ){
					return declaratorName.resolveBinding();
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
	
}
