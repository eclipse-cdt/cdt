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
package org.eclipse.cdt.internal.core.parser2.cpp;

import java.util.ArrayList;
import java.util.List;

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
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
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
import org.eclipse.cdt.core.dom.ast.IASTWhileStatement;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;
import org.eclipse.cdt.core.dom.ast.c.ICFunctionScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCatchHandler;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeleteExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionTryBlockDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLinkageSpecification;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceAlias;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeConstructorExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTryBlockStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTypenameExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDirective;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.dom.ast.gnu.IGNUASTCompoundStatementExpression;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.core.parser.util.ObjectMap;
import org.eclipse.cdt.core.parser.util.ObjectSet;
import org.eclipse.cdt.internal.core.parser.pst.ISymbol;
import org.eclipse.cdt.internal.core.parser.pst.ITypeInfo;

/**
 * @author aniefer
 */
public class CPPVisitor {

	/**
	 * @param name
	 */
	public static IBinding createBinding(IASTName name) {
		IASTNode parent = name.getParent();
		if( parent instanceof IASTNamedTypeSpecifier  ||
		    parent instanceof ICPPASTQualifiedName    ||
			parent instanceof ICPPASTBaseSpecifier ) 
		{
			return resolveBinding( name );
		} else if( parent instanceof IASTIdExpression ){
			return resolveBinding( parent );
		} else if( parent instanceof ICPPASTCompositeTypeSpecifier ){
			return createBinding( (ICPPASTCompositeTypeSpecifier) parent );
		} else if( parent instanceof IASTDeclarator ){
			return createBinding( (IASTDeclarator) parent );
		} else if( parent instanceof ICPPASTElaboratedTypeSpecifier ){
			return createBinding( (ICPPASTElaboratedTypeSpecifier) parent );
		} else if( parent instanceof IASTDeclaration )
			return createBinding( (IASTDeclaration) parent );
		return null;
	}
	
	private static IBinding createBinding( ICPPASTElaboratedTypeSpecifier elabType ){
		ICPPScope scope = (ICPPScope) getContainingScope( elabType );
		CPPClassType binding = (CPPClassType) scope.getBinding( 0, elabType.getName().toCharArray() );
		if( binding == null ){
			if( elabType.getKind() != IASTElaboratedTypeSpecifier.k_enum )
				binding = new CPPClassType( elabType );
			scope.addBinding( binding );
		} else {
			binding.addDeclaration( elabType );
		}
		return binding;
	}
	private static IBinding createBinding( ICPPASTCompositeTypeSpecifier compType ){
		ICPPScope scope = (ICPPScope) getContainingScope( compType );
		CPPClassType binding = (CPPClassType) scope.getBinding( 0, compType.getName().toCharArray() );
		if( binding == null ){
			binding = new CPPClassType( compType );
			scope.addBinding( binding );
		} else {
			binding.addDefinition( compType );
		}
		return binding;
	}
	private static IBinding createBinding( IASTDeclaration declaration ){
		if( declaration instanceof ICPPASTNamespaceDefinition ){
			ICPPASTNamespaceDefinition namespaceDef = (ICPPASTNamespaceDefinition) declaration;
			ICPPScope scope = (ICPPScope) getContainingScope( namespaceDef );
			CPPNamespace binding = (CPPNamespace) scope.getBinding( 0, namespaceDef.getName().toCharArray() );
			if( binding == null )
				binding = new CPPNamespace( namespaceDef );
			else
				binding.addDefinition( namespaceDef );
			return binding;
		} else if( declaration instanceof ICPPASTUsingDirective ){
			return resolveBinding( ((ICPPASTUsingDirective) declaration).getQualifiedName() );
		}
		
			
		return null;
	}
	private static IBinding createBinding( IASTDeclarator declarator ){
		IBinding binding = null;
		IASTNode parent = declarator.getParent();
		if( declarator instanceof ICPPASTFunctionDeclarator ){
			IScope scope = getContainingScope( parent );
			if( scope instanceof ICPPClassScope )
				binding = new CPPMethod( (ICPPASTFunctionDeclarator) declarator );
			else
				binding = new CPPFunction( (ICPPASTFunctionDeclarator) declarator );
		} else {
			if( parent instanceof IASTSimpleDeclaration ){
				IASTSimpleDeclaration simpleDecl = (IASTSimpleDeclaration) parent;
				if( simpleDecl.getParent() instanceof ICPPASTCompositeTypeSpecifier ){
					binding = new CPPField( declarator );
				} else {
					binding = new CPPVariable( declarator );
				}
			} else if( parent instanceof IASTParameterDeclaration ){
				
			} else if( parent instanceof IASTFunctionDefinition ){
				
			}
		}
		return binding;
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
	    else if( node instanceof IASTEnumerator ){
	        //put the enumerators in the same scope as the enumeration
	        return getContainingScope( (IASTEnumerationSpecifier) node.getParent() );
	    }
	    
	    return getContainingScope( node.getParent() );
	}
	
	public static IScope getContainingScope( IASTName name ){
		IASTNode parent = name.getParent();
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
			scope = function.getFunctionScope();
		}
		
		if( statement instanceof IASTGotoStatement || statement instanceof IASTLabelStatement ){
		    //labels have function scope
		    while( scope != null && !(scope instanceof ICFunctionScope) ){
		        scope = scope.getParent();
		    }
		}
		
		return scope;
	}
	
	public static IScope getContainingScope( IASTDeclSpecifier compTypeSpec ){
	    IASTNode parent = compTypeSpec.getParent();
	    return getContainingScope( (IASTSimpleDeclaration) parent );
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
		if( parent == null )
			return null;
		
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
	
	static private IBinding resolveBinding( IASTNode node ){
		if( node instanceof IASTIdExpression ){
			return resolveBinding( ((IASTIdExpression)node).getName() );
		}
		return null;
	}
	
	static protected class LookupData
	{
		public char[] name;
		public ObjectMap usingDirectives = ObjectMap.EMPTY_MAP; 
		public ObjectSet visited = ObjectSet.EMPTY_SET;	//used to ensure we don't visit things more than once
		public ObjectSet inheritanceChain;	//used to detect circular inheritance
		
		public boolean qualified = false;
		public boolean ignoreUsingDirectives = false;
		public boolean usingDirectivesOnly = false;
		public boolean forDefinition = false;
		
		public List foundItems = null;
		
		public LookupData( char[] n ){
			name = n;
		}
	}
	
	static private IBinding resolveBinding( IASTName name ){
		//1: get some context info off of the name to figure out what kind of lookup we want
		LookupData data = createLookupData( name );
		
		//2: lookup
		lookup( data, name );
		
		//3: resolve ambiguities
		//TODO
		if( data.foundItems != null && data.foundItems.size() == 1 ){
			IASTName found = (IASTName) data.foundItems.get(0);
			IBinding binding = found.resolveBinding();
			if( data.forDefinition ){
				addDefinition( binding, name );
			}
			return binding;
		}
		return null;
	}
	private static void addDefinition( IBinding binding, IASTName name ){
		if( binding instanceof IFunction ){
			IASTNode node =  name.getParent();
			if( node instanceof ICPPASTQualifiedName )
				node = node.getParent();
			if( node instanceof IASTFunctionDeclarator ){
				((CPPFunction)binding).addDefinition( (IASTFunctionDeclarator) node );
			}
		}
	}
	static private LookupData createLookupData( IASTName name ){
		LookupData data = new LookupData( name.toCharArray() );
		IASTNode parent = name.getParent();
		if( parent instanceof ICPPASTQualifiedName ){
			data.qualified = ((ICPPASTQualifiedName)parent).getNames()[0] != name;
			parent = parent.getParent();
			if( parent instanceof IASTDeclarator ){
				data.forDefinition = true;
			}
		} else if( parent instanceof IASTDeclarator ){
			data.forDefinition = true;
		} else if ( parent instanceof ICPPASTBaseSpecifier ) {
			//filter out non-type names
		}
		return data;
	}
	
	static private IASTName collectResult( LookupData data, IASTNode declaration, boolean checkAux ){
		if( declaration instanceof IASTDeclarationStatement )
			declaration = ((IASTDeclarationStatement)declaration).getDeclaration();
		else if( declaration instanceof IASTForStatement )
			declaration = ((IASTForStatement)declaration).getInitDeclaration();
		
		if( declaration == null )
			return null;
		
		if( declaration instanceof IASTSimpleDeclaration ){
			IASTSimpleDeclaration simpleDeclaration = (IASTSimpleDeclaration) declaration;
			IASTDeclarator [] declarators = simpleDeclaration.getDeclarators();
			for( int i = 0; i < declarators.length; i++ ){
				IASTDeclarator declarator = declarators[i];
				IASTName declaratorName = declarator.getName();
				if( CharArrayUtils.equals( declaratorName.toCharArray(), data.name ) ){
					return declaratorName;
				}
			}

			//decl spec 
			IASTDeclSpecifier declSpec = simpleDeclaration.getDeclSpecifier();
			if( declSpec instanceof IASTElaboratedTypeSpecifier ){
				IASTName elabName = ((IASTElaboratedTypeSpecifier)declSpec).getName();
				if( CharArrayUtils.equals( elabName.toCharArray(), data.name ) ){
					return elabName;
				}
			} else if( declSpec instanceof ICPPASTCompositeTypeSpecifier ){
				IASTName compName = ((IASTCompositeTypeSpecifier)declSpec).getName();
				if( CharArrayUtils.equals( compName.toCharArray(), data.name ) ){
					return compName;
				}
			} else if( declSpec instanceof IASTEnumerationSpecifier ){
			    IASTEnumerationSpecifier enumeration = (IASTEnumerationSpecifier) declSpec;
			    IASTName eName = enumeration.getName();
			    if( CharArrayUtils.equals( eName.toCharArray(), data.name ) ){
					return eName;
				}
			    //check enumerators too
			    IASTEnumerator [] list = enumeration.getEnumerators();
			    for( int i = 0; i < list.length; i++ ) {
			        IASTEnumerator enumerator = list[i];
			        if( enumerator == null ) break;
			        eName = enumerator.getName();
			        if( CharArrayUtils.equals( eName.toCharArray(), data.name ) ){
						return eName;
					}
			    }
			    
			}
		} else if( declaration instanceof IASTFunctionDefinition ){
			IASTFunctionDefinition functionDef = (IASTFunctionDefinition) declaration;
			IASTFunctionDeclarator declarator = functionDef.getDeclarator();
			
			//check the function itself
			IASTName declName = declarator.getName();
			if( declName instanceof ICPPASTQualifiedName ){
				IASTName [] names = ((ICPPASTQualifiedName)declName).getNames(); 
				declName = names[ names.length - 1 ];
			}
			if( CharArrayUtils.equals( declName.toCharArray(), data.name ) ){
				return declName;
			}
			if( checkAux ) {
				//check the parameters
				IASTParameterDeclaration []  parameters = declarator.getParameters();
				for( int i = 0; i < parameters.length; i++ ){
					IASTParameterDeclaration parameterDeclaration = parameters[i];
					if( parameterDeclaration == null ) break;
					declName = parameterDeclaration.getDeclarator().getName();
					if( CharArrayUtils.equals( declName.toCharArray(), data.name ) ){
						return declName;
					}
				}
			}
		} else if( declaration instanceof ICPPASTNamespaceDefinition ){
			IASTName namespaceName = ((ICPPASTNamespaceDefinition) declaration).getName();
			if( CharArrayUtils.equals( namespaceName.toCharArray(), data.name ) )
				return namespaceName;
		}
		return null;
	}
	static private void lookup( LookupData data, IASTName name ){
		IASTNode node = name; 
		
		ICPPScope scope = (ICPPScope) getContainingScope( node );		
		while( scope != null ){
			IASTNode blockItem = getContainingBlockItem( node );
			List directives = null;
			if( !data.usingDirectivesOnly ){
				directives = new ArrayList(2);
				data.foundItems = lookupInScope( data, scope, blockItem, directives );
			}
				
			
			if( !data.ignoreUsingDirectives ) {
				data.visited.clear();
				if( data.foundItems == null || data.foundItems.isEmpty() ){
					List transitives = lookupInNominated( data, scope, null );
					
					processDirectives( data, scope, transitives );
					if( directives != null && directives.size() != 0 )
						processDirectives( data, scope, directives );
					
					while( !data.usingDirectives.isEmpty() && data.usingDirectives.get( scope ) != null ){
						if( transitives != null )
							transitives.clear();
						transitives = lookupInNominated( data, scope, transitives );
		
						if( !data.qualified || data.foundItems == null ){
							processDirectives( data, scope, transitives );
						}
					}
				}
			}
			
			if( data.foundItems != null && !data.foundItems.isEmpty() )
				return;
			
			if( !data.usingDirectivesOnly && scope instanceof ICPPClassScope ){
				data.foundItems = lookupInParents( data, (ICPPClassScope) scope );
			}
			
			if( data.foundItems != null && !data.foundItems.isEmpty() )
				return;
			
			//if still not found, loop and check our containing scope
			if( data.qualified && !data.usingDirectives.isEmpty() )
				data.usingDirectivesOnly = true;
			
			if( blockItem != null )
				node = blockItem.getParent();
			scope = (ICPPScope) scope.getParent();
		}
	}
	
	private static List lookupInParents( LookupData data, ICPPClassScope lookIn ){
		ICPPASTCompositeTypeSpecifier compositeTypeSpec = (ICPPASTCompositeTypeSpecifier) lookIn.getPhysicalNode();
		ICPPASTBaseSpecifier [] bases = compositeTypeSpec.getBaseSpecifiers();

		List inherited = null;
		List result = null;
		
		if( bases.length == 0 )
			return null;
				
		//use data to detect circular inheritance
		if( data.inheritanceChain == null )
			data.inheritanceChain = new ObjectSet( 2 );
		
		data.inheritanceChain.put( lookIn );
			
		int size = bases.length;
		for( int i = 0; i < size; i++ )
		{
			ICPPClassType binding = (ICPPClassType) bases[i].getName().resolveBinding();
			ICPPClassScope parent = (ICPPClassScope) binding.getCompositeScope();
			
			if( parent == null )
				continue;

			if( !bases[i].isVirtual() || !data.visited.containsKey( parent ) ){
				if( bases[i].isVirtual() ){
				    if( data.visited == ObjectSet.EMPTY_SET )
				        data.visited = new ObjectSet(2);
					data.visited.put( parent );
				}

				//if the inheritanceChain already contains the parent, then that 
				//is circular inheritance
				if( ! data.inheritanceChain.containsKey( parent ) ){
					//is this name define in this scope?
					inherited = lookupInScope( data, parent, null, null );
					
					if( inherited == null || inherited.isEmpty() ){
						inherited = lookupInParents( data, parent );
					}
				} else {
					//throw new ParserSymbolTableException( ParserSymbolTableException.r_CircularInheritance );
				}
			}	
			
			if( inherited != null && !inherited.isEmpty() ){
				if( result == null || result.isEmpty() ){
					result = inherited;
				} else if ( inherited != null && !inherited.isEmpty() ) {
					for( int j = 0; j < result.size(); j++ ) {
						IASTName n = (IASTName) result.get(j);
						if( !checkAmbiguity( n, inherited ) ){
							//throw new ParserSymbolTableException( ParserSymbolTableException.r_Ambiguous );
							return null;
						}
					}
				}
			} else {
				inherited = null;	//reset temp for next iteration
			}
		}
	
		data.inheritanceChain.remove( lookIn );

		return result;	
	}
	
	private static boolean checkAmbiguity( Object obj1, Object obj2 ){
		//it is not ambiguous if they are the same thing and it is static or an enumerator
		if( obj1 == obj2 ){
			List objList = ( obj1 instanceof List ) ? (List) obj1 : null;
			int objListSize = ( objList != null ) ? objList.size() : 0;
			ISymbol symbol = ( objList != null ) ? (ISymbol) objList.get(0) : ( ISymbol )obj1;
			int idx = 1;
			while( symbol != null ) {
				ITypeInfo type = ((ISymbol)obj1).getTypeInfo();
				if( !type.checkBit( ITypeInfo.isStatic ) && !type.isType( ITypeInfo.t_enumerator ) ){
					return false;
				}
				
				if( objList != null && idx < objListSize ){
					symbol = (ISymbol) objList.get( idx++ );
				} else {
					symbol = null;
				}
			}
			return true;
		} 
		return false;
	}
	static private void processDirectives( LookupData data, IScope scope, List directives ){
		if( directives == null || directives.size() == 0 )
			return;
		
		ICPPScope enclosing = null;
		IScope temp = null;
		
		int size = directives.size();
		for( int i = 0; i < size; i++ ){
			IASTName qualName = ((ICPPASTUsingDirective)directives.get(i)).getQualifiedName();
			IBinding binding = qualName.resolveBinding();
			if( binding instanceof ICPPNamespace ){
				temp = ((ICPPNamespace)binding).getNamespaceScope();
			} else
				continue;
				
			//namespace are searched at most once
			if( !data.visited.containsKey( temp ) ){
				enclosing = getClosestEnclosingScope( scope, temp );
				
				//data.usingDirectives is a map from enclosing scope to a list
				//of namespaces to consider when we reach that enclosing scope
				List list = data.usingDirectives.isEmpty() ? null : (List) data.usingDirectives.get( enclosing );
				if( list == null ){
					list = new ArrayList();
					
					if( data.usingDirectives == ObjectMap.EMPTY_MAP ){
						data.usingDirectives = new ObjectMap(2);
					}
					data.usingDirectives.put( enclosing, list );
				} 
				list.add( temp );
			}
		}
		
	}
	static private ICPPScope getClosestEnclosingScope( IScope scope1, IScope scope2 ){
		ObjectSet set = new ObjectSet( 2 );
		IScope parent = scope1;
		while( parent != null ){
			set.put( parent );
			parent = parent.getParent();
		}
		parent = scope2;
		while( parent != null && !set.containsKey( parent ) ){
			parent = parent.getParent();
		}
		return (ICPPScope) parent;
	}
	/**
	 * 
	 * @param scope
	 * @return List of encountered using directives
	 */
	static private List lookupInScope( LookupData data, ICPPScope scope, IASTNode blockItem, List usingDirectives ) {
		IASTName possible = null;
		IASTNode [] nodes = null;
		IASTNode parent = scope.getPhysicalNode();

		List found = null;
		
		if( parent instanceof IASTCompoundStatement ){
			IASTCompoundStatement compound = (IASTCompoundStatement) parent;
			nodes = compound.getStatements();
		} else if ( parent instanceof IASTTranslationUnit ){
			IASTTranslationUnit translation = (IASTTranslationUnit) parent;
			nodes = translation.getDeclarations();
		} else if ( parent instanceof ICPPASTCompositeTypeSpecifier ){
			ICPPASTCompositeTypeSpecifier comp = (ICPPASTCompositeTypeSpecifier) parent;
			nodes = comp.getMembers();
		} else if ( parent instanceof ICPPASTNamespaceDefinition ){
			nodes = ((ICPPASTNamespaceDefinition)parent).getDeclarations();
		}

		int idx = -1;
		IASTNode item = ( nodes != null ? (nodes.length > 0 ? nodes[++idx] : null ) : parent );

		while( item != null ) {
			if( item == null || item == blockItem )
				break;
			
			if( item instanceof ICPPASTUsingDirective && !data.ignoreUsingDirectives ) {
				if( usingDirectives != null )
					usingDirectives.add( item );
			} else {
				possible = collectResult( data, item, (item == parent)  );
				if( possible != null ){
					if( found == null )
						found = new ArrayList(2);
					found.add( possible );
				}
			}
			if( idx > -1 && ++idx < nodes.length ){
				item = nodes[idx];
			} else {
				item = null;
			}
		}
		return found;
	}
	
	static private List lookupInNominated( LookupData data, ICPPScope scope, List transitives ){
		if( data.usingDirectives.isEmpty() )
			return transitives;
		
		List directives = null;
		ICPPScope temp = null;
		
		directives = (List) data.usingDirectives.remove( scope );
		if( directives == null || directives.size() == 0 ) {
			return transitives;
		}
		for( int i = 0; i < directives.size(); i++ ){
			temp = (ICPPScope) directives.get(i);
			if( !data.visited.containsKey( temp ) ){
				if( data.visited == ObjectSet.EMPTY_SET ) {
					data.visited = new ObjectSet(2);
				}
				data.visited.put( temp );
				List usings = new ArrayList(2);
				List found = lookupInScope( data, temp, null, usings );
				if( data.foundItems == null )
					data.foundItems = found;
				else if( found != null )
					data.foundItems.addAll( found );
				
				
				//only consider the transitive using directives if we are an unqualified
				//lookup, or we didn't find the name in decl
				if( usings != null && usings.size() > 0 && (!data.qualified || found == null ) ){
					if( transitives == null )
						transitives = new ArrayList(2);
					transitives.addAll( usings );
				}
			}
		}
		return transitives;
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
		public boolean processBaseSpecifiers;
		
		/**
		 * @return true to continue visiting, return false to stop
		 */
		public boolean processName( IASTName name ) 					{ return true; }
		public boolean processDeclaration( IASTDeclaration declaration ){ return true; }
		public boolean processInitializer( IASTInitializer initializer ){ return true; }
		public boolean processParameterDeclaration( IASTParameterDeclaration parameterDeclaration ) { return true; }
		public boolean processDeclarator( IASTDeclarator declarator )   { return true; }
		public boolean processDeclSpecifier( IASTDeclSpecifier declSpec ){return true; }
		public boolean processExpression( IASTExpression expression )   { return true; }
		public boolean processStatement( IASTStatement statement )      { return true; }
		public boolean processTypeId( IASTTypeId typeId )               { return true; }
		public boolean processEnumerator( IASTEnumerator enumerator )   { return true; }
		public boolean processBaseSpecifier(ICPPASTBaseSpecifier specifier) { return true; }
	}
	
	public static void visitTranslationUnit( IASTTranslationUnit tu, CPPBaseVisitorAction action ){
		IASTDeclaration [] decls = tu.getDeclarations();
		for( int i = 0; i < decls.length; i++ ){
			if( !visitDeclaration( decls[i], action ) ) return;
		}
	}

	/**
	 * @param declaration
	 * @param action
	 * @return
	 */
	public static boolean visitDeclaration(IASTDeclaration declaration, CPPBaseVisitorAction action) {
		if( action.processDeclarations ) 
			if( !action.processDeclaration( declaration ) ) return false;
		
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
		} else if( declaration instanceof ICPPASTNamespaceDefinition ){
			ICPPASTNamespaceDefinition namespace = (ICPPASTNamespaceDefinition) declaration;
			if( !visitName( namespace.getName(), action ) ) return false;
			IASTDeclaration [] decls = namespace.getDeclarations();
			for( int i = 0; i < decls.length; i++ ){
				if( !visitDeclaration( decls[i], action ) ) return false;
			}
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
		if( action.processNames )
			if( !action.processName( name ) ) return false;
			
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
		if( action.processDeclSpecifiers )
			if( !action.processDeclSpecifier( declSpecifier ) ) return false;
			
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
			IASTEnumerationSpecifier enum = (IASTEnumerationSpecifier) declSpecifier;
			if( !visitName( enum.getName(), action ) ) return false;
			IASTEnumerator [] etors = enum.getEnumerators();
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
		if( action.processDeclarators )
			if( !action.processDeclarator( declarator ) ) return false;
			
		if( !visitName( declarator.getName(), action ) ) return false;
		
		if( declarator.getNestedDeclarator() != null )
			if( !visitDeclarator( declarator.getNestedDeclarator(), action ) ) return false;
		
		if( declarator instanceof ICPPASTFunctionDeclarator ){
			ICPPASTFunctionDeclarator fdtor = (ICPPASTFunctionDeclarator) declarator;
		    IASTParameterDeclaration [] list = fdtor.getParameters();
			for( int i = 0; i < list.length; i++ ){
				IASTParameterDeclaration param = list[i];
				if( !visitDeclSpecifier( param.getDeclSpecifier(), action ) ) return false;
				if( !visitDeclarator( param.getDeclarator(), action ) ) return false;
			}
			ICPPASTConstructorChainInitializer [] ctorChain = fdtor.getConstructorChain();
			for( int i = 0; i < list.length; i++ ){
				if( !visitName( ctorChain[i].getMemberInitializerId(), action ) ) return false;
				if( !visitExpression( ctorChain[i].getInitializerValue(), action ) ) return false;
			}
			IASTTypeId [] typeIds = fdtor.getExceptionSpecification();
			for( int i = 0; i < list.length; i++ ){
				if( !visitTypeId( typeIds[i], action ) ) return false;
			}
			
			if( declarator instanceof ICPPASTFunctionTryBlockDeclarator ){
				ICPPASTCatchHandler [] catchHandlers = ((ICPPASTFunctionTryBlockDeclarator)declarator).getCatchHandlers();
				for( int i = 0; i < catchHandlers.length; i++ ){
					if( !visitStatement( catchHandlers[i], action ) ) return false;
				}
			}
			
		}
		
		if( declarator.getInitializer() != null )
		    if( !visitInitializer( declarator.getInitializer(), action ) ) return false;
		    
		return true;
	}
	
	/**
	 * @param body
	 * @param action
	 * @return
	 */
	public static boolean visitStatement(IASTStatement statement, CPPBaseVisitorAction action) {
		if( action.processStatements )
			if( !action.processStatement( statement ) ) return false;
			
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
		} else if( statement instanceof IASTIfStatement ){
		    if( !visitExpression( ((IASTIfStatement) statement ).getCondition(), action ) ) return false;
		    if( !visitStatement( ((IASTIfStatement) statement ).getThenClause(), action ) ) return false;
		    if( !visitStatement( ((IASTIfStatement) statement ).getElseClause(), action ) ) return false;
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
		if( action.processExpressions )
			if( !action.processExpression( expression ) ) return false;
			
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
		if( action.processTypeIds )
			if( !action.processTypeId( typeId ) ) return false;
		if( !visitDeclarator( typeId.getAbstractDeclarator(), action ) ) return false;
		if( !visitDeclSpecifier( typeId.getDeclSpecifier(), action ) ) return false;
		return true;
	}

	/**
	 * @param initializer
	 * @param action
	 * @return
	 */
	public static boolean visitInitializer(IASTInitializer initializer, CPPBaseVisitorAction action) {
		if( action.processInitializers )
			if( !action.processInitializer( initializer ) ) return false;
			
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
		if( action.processEnumerators )
			if( !action.processEnumerator( enumerator ) ) return false;
			
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
		if( action.processBaseSpecifiers )
			if( !action.processBaseSpecifier( specifier ) ) return false;
			
	    if( !visitName( specifier.getName(), action ) ) return false;
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
}
