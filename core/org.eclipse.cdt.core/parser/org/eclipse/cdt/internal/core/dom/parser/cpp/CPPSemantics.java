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
/*
 * Created on Dec 8, 2004
 */
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionList;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IQualifierType;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFieldReference;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDirective;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBasicType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPCompositeBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTNamespaceDefinition;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.core.parser.util.ObjectMap;
import org.eclipse.cdt.core.parser.util.ObjectSet;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;
import org.eclipse.cdt.internal.core.parser.pst.ISymbol;
import org.eclipse.cdt.internal.core.parser.pst.ITypeInfo;

/**
 * @author aniefer
 */
public class CPPSemantics {

	public static final char[] EMPTY_NAME_ARRAY = new char[0];
	public static final IType VOID_TYPE = new CPPBasicType( IBasicType.t_void, 0 );
	
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
		public boolean typesOnly = false;
		public List foundItems = null;
		public Object [] functionParameters;
		public boolean forUserDefinedConversion;
		public boolean forUsingDeclaration;
        public ProblemBinding problem;
		
		public LookupData( char[] n ){
			name = n;
		}
	}

	static protected class Cost
	{
		public Cost( IType s, IType t ){
			source = s;
			target = t;
		}
		
		public IType source;
		public IType target;
		
		public boolean targetHadReference = false;
		
		public int lvalue;
		public int promotion;
		public int conversion;
		public int qualification;
		public int userDefined;
		public int rank = -1;
		public int detail;
		
		//Some constants to help clarify things
		public static final int AMBIGUOUS_USERDEFINED_CONVERSION = 1;
		
		public static final int NO_MATCH_RANK = -1;
		public static final int IDENTITY_RANK = 0;
		public static final int LVALUE_OR_QUALIFICATION_RANK = 0;
		public static final int PROMOTION_RANK = 1;
		public static final int CONVERSION_RANK = 2;
		public static final int DERIVED_TO_BASE_CONVERSION = 3;
		public static final int USERDEFINED_CONVERSION_RANK = 4;
		public static final int ELLIPSIS_CONVERSION = 5;

		public int compare( Cost cost ){
			int result = 0;
			
			if( rank != cost.rank ){
				return cost.rank - rank;
			}
			
			if( userDefined != 0 || cost.userDefined != 0 ){
				if( userDefined == 0 || cost.userDefined == 0 ){
					return cost.userDefined - userDefined;
				} 
				if( (userDefined == AMBIGUOUS_USERDEFINED_CONVERSION || cost.userDefined == AMBIGUOUS_USERDEFINED_CONVERSION) ||
					(userDefined != cost.userDefined ) )
						return 0;
		 
					// else they are the same constructor/conversion operator and are ranked
					//on the standard conversion sequence
		
			}
			
			if( promotion > 0 || cost.promotion > 0 ){
				result = cost.promotion - promotion;
			}
			if( conversion > 0 || cost.conversion > 0 ){
				if( detail == cost.detail ){
					result = cost.conversion - conversion;
				} else {
					result = cost.detail - detail;
				}
			}
			
			if( result == 0 ){
				if( cost.qualification != qualification ){
					return cost.qualification - qualification;
				} else if( (cost.qualification == qualification) && qualification == 0 ){
					return 0;
				} else {
					IPointerType op1, op2;
					IType t1 = cost.target, t2 = target;
					int subOrSuper = 0;
					while( true ){
						op1 = null;
						op2 = null;
						while( true ){
							if( t1 instanceof ITypedef )	
								t1 = ((ITypedef)t1).getType();
							else {
								if( t1 instanceof IPointerType )		
									op1 = (IPointerType) t1;	
								break;
							}
						}
						while( true ){
							if( t2 instanceof ITypedef )	
								t2 = ((ITypedef)t2).getType();
							else {
								if( t2 instanceof IPointerType )		
									op1 = (IPointerType) t2;	
								break;
							}
						}
						if( op1 == null || op2 == null )
							break;
						
						int cmp = ( op1.isConst() ? 1 : 0 ) + ( op1.isVolatile() ? 1 : 0 ) -
						          ( op2.isConst() ? 1 : 0 ) + ( op2.isVolatile() ? 1 : 0 );
 
						if( subOrSuper == 0 )
							subOrSuper = cmp;
						else if( subOrSuper > 0 ^ cmp > 0) {
							result = -1;
							break;
						}

					}
					if( result == -1 ){
						result = 0;
					} else {
						if( op1 == op2 ){
							result = subOrSuper;
						} else {
							result = op1 != null ? 1 : -1; 
						}
					}
				}
			}
			 
			return result;
		}
	}
	static protected IBinding resolveBinding( IASTName name ){
	    if( name.toCharArray().length == 0 ){
	    	IASTNode node = name.getParent();
	    	if( node instanceof ICPPASTQualifiedName ){
	    		ICPPASTQualifiedName qname = (ICPPASTQualifiedName) node;
	    		if( qname.getNames()[0] == name ){
	    			//translation unit
	    			while( ! (node instanceof ICPPASTTranslationUnit ) ){
	    				node = node.getParent();
	    			}
	    			return ((ICPPASTTranslationUnit) node).resolveBinding();
	    		}
	    	}
	    	return null;	    	
	    }

		//1: get some context info off of the name to figure out what kind of lookup we want
		LookupData data = createLookupData( name );
		
		//2: lookup
		lookup( data, name );
		
		if( data.problem != null )
		    return data.problem;
		
		//3: resolve ambiguities
		IBinding binding = resolveAmbiguities( data, name );
		if( binding != null && data.forDefinition && !( binding instanceof IProblemBinding ) ){
			addDefinition( binding, name );
		}
		return binding;
	}

	static private CPPSemantics.LookupData createLookupData( IASTName name ){
		CPPSemantics.LookupData data = new CPPSemantics.LookupData( name.toCharArray() );
		IASTNode parent = name.getParent();
		if( parent instanceof ICPPASTQualifiedName ){
			data.qualified = ((ICPPASTQualifiedName)parent).getNames()[0] != name;
			parent = parent.getParent();
			if( parent instanceof IASTDeclarator ){
				data.forDefinition = true;
				if( parent instanceof ICPPASTFunctionDeclarator ){
					data.functionParameters = ((ICPPASTFunctionDeclarator)parent).getParameters();
				}
			} else if( parent instanceof IASTIdExpression ){
				parent = parent.getParent();
				if( parent instanceof IASTFunctionCallExpression ){
					IASTExpression exp = ((IASTFunctionCallExpression)parent).getParameterExpression();
					if( exp instanceof IASTExpressionList )
						data.functionParameters = ((IASTExpressionList) exp ).getExpressions();
					else if( exp != null )
						data.functionParameters = new IASTExpression [] { exp };
					else 
						data.functionParameters = IASTExpression.EMPTY_EXPRESSION_ARRAY;
				}
			} else if( parent instanceof ICPPASTUsingDeclaration ){
				data.forUsingDeclaration = true;
			}
		} else if( parent instanceof IASTDeclarator ){
		    if( parent.getParent() instanceof IASTSimpleDeclaration )
		        data.forDefinition = true;
		} else if ( parent instanceof ICPPASTBaseSpecifier ||
		        	parent instanceof ICPPASTElaboratedTypeSpecifier) 
		{
			data.typesOnly = true;
		} else if( parent instanceof IASTIdExpression ){
			parent = parent.getParent();
			if( parent instanceof IASTFunctionCallExpression ){
				IASTExpression exp = ((IASTFunctionCallExpression)parent).getParameterExpression();
				if( exp instanceof IASTExpressionList )
					data.functionParameters = ((IASTExpressionList) exp ).getExpressions();
				else if( exp != null )
					data.functionParameters = new IASTExpression [] { exp };
				else
					data.functionParameters = IASTExpression.EMPTY_EXPRESSION_ARRAY;
			}
		} else if( parent instanceof ICPPASTFieldReference ){
			data.qualified = true;
			
			if( parent.getParent() instanceof IASTFunctionCallExpression ){
			    IASTExpression exp = ((IASTFunctionCallExpression)parent.getParent()).getParameterExpression();
				if( exp instanceof IASTExpressionList )
					data.functionParameters = ((IASTExpressionList) exp ).getExpressions();
				else if( exp != null )
					data.functionParameters = new IASTExpression [] { exp };
				else
					data.functionParameters = IASTExpression.EMPTY_EXPRESSION_ARRAY;
			}
		} else if( parent instanceof ICPPASTUsingDeclaration ){
			data.forUsingDeclaration = true;
		}
		return data;
	}

	static private ICPPScope getLookupScope( IASTName name ){
	    IASTNode parent = name.getParent();
	    
	    if( parent instanceof IASTDeclSpecifier ){
	        IASTNode node = parent.getParent();
	        if( node instanceof IASTParameterDeclaration ){
	            IASTNode n = CPPVisitor.getContainingBlockItem( parent );
		        return (ICPPScope) CPPVisitor.getContainingScope( n );
	        }
	    } else if( parent instanceof ICPPASTBaseSpecifier ) {
	        IASTNode n = CPPVisitor.getContainingBlockItem( parent );
	        return (ICPPScope) CPPVisitor.getContainingScope( n );
	    } 
	    return (ICPPScope) CPPVisitor.getContainingScope( name );
	}
	
	static private void lookup( CPPSemantics.LookupData data, IASTName name ){
		IASTNode node = name; 
		
		ICPPScope scope = getLookupScope( name );		
		while( scope != null ){
			IASTNode blockItem = CPPVisitor.getContainingBlockItem( node );
			if( scope.getPhysicalNode() != blockItem.getParent() )
				blockItem = node;
			
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
			
			if( data.problem != null || data.foundItems != null && !data.foundItems.isEmpty() )
				return;
			
			if( !data.usingDirectivesOnly && scope instanceof ICPPClassScope ){
				data.foundItems = lookupInParents( data, (ICPPClassScope) scope );
			}
			
			if( data.problem != null || data.foundItems != null && !data.foundItems.isEmpty() )
				return;
			
			//if still not found, loop and check our containing scope
			if( data.qualified && !data.usingDirectives.isEmpty() )
				data.usingDirectivesOnly = true;
			
			if( blockItem != null )
				node = blockItem;
			scope = (ICPPScope) scope.getParent();
		}
	}

	private static List lookupInParents( CPPSemantics.LookupData data, ICPPClassScope lookIn ){
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
				    data.problem = new ProblemBinding( IProblemBinding.SEMANTIC_CIRCULAR_INHERITANCE, bases[i].getName().toCharArray() );
				    return null;
				}
			}	
			
			if( inherited != null && !inherited.isEmpty() ){
				if( result == null || result.isEmpty() ){
					result = inherited;
				} else if ( inherited != null && !inherited.isEmpty() ) {
					for( int j = 0; j < result.size(); j++ ) {
						IASTName n = (IASTName) result.get(j);
						if( !checkAmbiguity( n, inherited ) ){
						    data.problem = new ProblemBinding( IProblemBinding.SEMANTIC_AMBIGUOUS_LOOKUP, n.toCharArray() ); 
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

	static private void processDirectives( CPPSemantics.LookupData data, IScope scope, List directives ){
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
	static private List lookupInScope( CPPSemantics.LookupData data, ICPPScope scope, IASTNode blockItem, List usingDirectives ) {
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
			if( item == null || ( blockItem != null && ((ASTNode)item).getOffset() > ((ASTNode) blockItem).getOffset() ))
				break;
			if( item == blockItem ){
			    if( !(item instanceof IASTNamespaceDefinition)  &&
			        !(item instanceof IASTSimpleDeclaration && 
			           ((IASTSimpleDeclaration)item).getDeclSpecifier() instanceof IASTCompositeTypeSpecifier) )
			    {
			        break;
			    }
			}
			
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

	static private List lookupInNominated( CPPSemantics.LookupData data, ICPPScope scope, List transitives ){
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

	static private IASTName collectResult( CPPSemantics.LookupData data, IASTNode node, boolean checkAux ){
	    IASTDeclaration declaration = null;
	    if( node instanceof IASTDeclaration ) 
	        declaration = (IASTDeclaration) node;
		if( node instanceof IASTDeclarationStatement )
			declaration = ((IASTDeclarationStatement)node).getDeclaration();
		else if( node instanceof IASTForStatement )
			declaration = ((IASTForStatement)node).getInitDeclaration();
		
		if( declaration == null )
			return null;
		
		if( declaration instanceof IASTSimpleDeclaration ){
			IASTSimpleDeclaration simpleDeclaration = (IASTSimpleDeclaration) declaration;
			if( !data.typesOnly ) { 
				IASTDeclarator [] declarators = simpleDeclaration.getDeclarators();
				for( int i = 0; i < declarators.length; i++ ){
					IASTDeclarator declarator = declarators[i];
					IASTName declaratorName = declarator.getName();
					if( CharArrayUtils.equals( declaratorName.toCharArray(), data.name ) ){
						return declaratorName;
					}
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
			    if( !data.typesOnly ) {
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
			}
		} else if( declaration instanceof ICPPASTUsingDeclaration ){
			ICPPASTUsingDeclaration using = (ICPPASTUsingDeclaration) declaration;
			IASTName name = using.getName();
			if( name instanceof ICPPASTQualifiedName ){
				IASTName [] ns = ((ICPPASTQualifiedName)name).getNames();
				name = ns[ ns.length - 1 ];
			}
			if( CharArrayUtils.equals( name.toCharArray(), data.name ) ){
				return name;
			}
			
		}
		if( data.typesOnly )
		    return null;
		
		if( declaration instanceof IASTFunctionDefinition ){
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

	private static void addDefinition( IBinding binding, IASTName name ){
		if( binding instanceof IFunction ){
			IASTNode node =  name.getParent();
			if( node instanceof ICPPASTQualifiedName )
				node = node.getParent();
			if( node instanceof ICPPASTFunctionDeclarator ){
				((CPPFunction)binding).addDefinition( (ICPPASTFunctionDeclarator) node );
			}
		}
	}

	static protected IBinding resolveAmbiguities( IASTName name, List bindings ){
	    if( bindings == null || bindings.size() == 0 )
	        return null;
	    else if( bindings.size() == 1 )
	        return (IBinding) bindings.get( 0 );
	    
	    LookupData data = createLookupData( name );
	    data.foundItems = bindings;
	    return resolveAmbiguities( data, name );
	}
	
	static private IBinding resolveAmbiguities( CPPSemantics.LookupData data, IASTName name ) {
	    if( data.foundItems == null || data.foundItems.size() == 0 )
	        return null;
	      
	    IBinding type = null;
	    IBinding obj  = null;
	    IBinding temp = null;
	    List fns = null;
	    
	    for( int i = 0; i < data.foundItems.size(); i++ ){
	        IASTName n = (IASTName) data.foundItems.get( i );
	        
	        temp = n.resolveBinding();
	        if( temp instanceof ICPPCompositeBinding ){
	        	IBinding [] bindings = ((ICPPCompositeBinding) temp).getBindings();
	        	for( int j = 0; j < bindings.length; j++ )
	        		data.foundItems.add( bindings[j] );
	        	continue;
	        } else if( temp instanceof IType ){
	        	if( type == null ){
	                type = temp;
	            } else {
	                return new ProblemBinding( IProblemBinding.SEMANTIC_AMBIGUOUS_LOOKUP, data.name );
	            }
	        } else if( temp instanceof IFunction ){
	        	if( fns == null )
	        		fns = new ArrayList(2);
	        	fns.add( temp );
	        } else {
	        	if( obj == null )
	        		obj = temp;
	        	else {
	        	    return new ProblemBinding( IProblemBinding.SEMANTIC_AMBIGUOUS_LOOKUP, data.name );
	        	}
	        }
	    }
	    
	    if( type != null ) {
	    	if( obj == null && fns == null )
	    		return type;
	    	IScope typeScope = type.getScope();
	    	if( obj != null && obj.getScope() != typeScope ){
	    	    return new ProblemBinding( IProblemBinding.SEMANTIC_AMBIGUOUS_LOOKUP, data.name );
	    	} else if( fns != null ){
	    		for( int i = 0; i < fns.size(); i++ ){
	    			if( ((IBinding)fns.get(i)).getScope() != typeScope )
	    			    return new ProblemBinding( IProblemBinding.SEMANTIC_AMBIGUOUS_LOOKUP, data.name );
	    		}
	    	}
	    }
	    if( fns != null){
	    	if( obj != null )
	    		return new ProblemBinding( IProblemBinding.SEMANTIC_AMBIGUOUS_LOOKUP, data.name );
	    	return resolveFunction( data, fns );
	    }
	    
	    return obj;
	}
	
	static private boolean functionHasParameters( ICPPASTFunctionDeclarator function, IASTParameterDeclaration [] params ){
		IFunctionType ftype = (IFunctionType) CPPVisitor.createType( function );
		if( params.length == 0 ){
			return ftype.getParameterTypes().length == 0;
		}
		
		IASTNode node = params[0].getParent();
		if( node instanceof ICPPASTFunctionDeclarator ){
			IFunctionType t2 = (IFunctionType) CPPVisitor.createType( (ICPPASTFunctionDeclarator) node );
			return ftype.equals( t2 );
		}
	 	return false;
	}
	
	static private void reduceToViable( LookupData data, List functions ){
		Object [] fParams = data.functionParameters;
		int numParameters = ( fParams != null ) ? fParams.length : 0;		
		int num;	
			
		//Trim the list down to the set of viable functions
		IFunction fName;
		ICPPASTFunctionDeclarator function = null;
		int size = functions.size();
		for( int i = 0; i < size; i++ ){
			fName = (IFunction) functions.get(i);
			function = (ICPPASTFunctionDeclarator) fName.getPhysicalNode();
			num = function.getParameters().length;
		
			//if there are m arguments in the list, all candidate functions having m parameters
			//are viable	 
			if( num == numParameters ){
				if( data.forDefinition && !functionHasParameters( function, (IASTParameterDeclaration[]) data.functionParameters ) ){
					functions.remove( i-- );
					size--;
				}
				continue;
			} 
			//check for void
			else if( numParameters == 0 && num == 1 ){
				IASTParameterDeclaration param = function.getParameters()[0];
				IASTDeclSpecifier declSpec = param.getDeclSpecifier();
				if( declSpec instanceof IASTSimpleDeclSpecifier ){
					if( ((IASTSimpleDeclSpecifier)declSpec).getType() == IASTSimpleDeclSpecifier.t_void &&
						param.getDeclarator().getPointerOperators().length == 0 )
					{
						continue;
					}
				}
			}
			
			//A candidate function having fewer than m parameters is viable only if it has an 
			//ellipsis in its parameter list.
			if( num < numParameters ){
				if( function.takesVarArgs() ) {
					continue;
				} 
				//not enough parameters, remove it
				functions.remove( i-- );
				size--;
			} 
			//a candidate function having more than m parameters is viable only if the (m+1)-st
			//parameter has a default argument
			else {
				IASTParameterDeclaration [] params = function.getParameters();
				for( int j = num - 1; j > ( numParameters - num); j-- ){
					if( params[j].getDeclarator().getInitializer() == null ){
						functions.remove( i-- );
						size--;
						break;
					}
				}
			}
		}
	}

	static private IType getSourceParameterType( Object [] params, int idx ){
		if( params instanceof IASTExpression [] ){
			IASTExpression [] exps = (IASTExpression[]) params;
			if( idx < exps.length)
				return CPPVisitor.getExpressionType( exps[ idx ] );
			return ( idx == 0 ) ? VOID_TYPE : null;
		} else if( params instanceof IASTParameterDeclaration[] ){
			IASTParameterDeclaration [] decls = (IASTParameterDeclaration[]) params;
			if( idx < decls.length)
				return CPPVisitor.createType( decls[idx].getDeclarator() );
			return ( idx == 0 ) ? VOID_TYPE : null;
		} else if( params == null && idx == 0 )
			return VOID_TYPE;
		return null;
	}
	static private IBinding resolveFunction( CPPSemantics.LookupData data, List fns ){
		if( data.forUsingDeclaration ){
			if( fns.size() == 1 )
				return (IBinding) fns.get( 0 );
			return new CPPCompositeBinding( fns );
		}
		//reduce our set of candidate functions to only those who have the right number of parameters
		reduceToViable( data, fns );
		
		IFunction bestFn = null;					//the best function
		IFunction currFn = null;					//the function currently under consideration
		Cost [] bestFnCost = null;				//the cost of the best function
		Cost [] currFnCost = null;				//the cost for the current function
				
		IType source = null;					//parameter we are called with
		IType target = null;					//function's parameter
//		ITypeInfo voidInfo = null;				//used to compare f() and f(void)
		
		int comparison;
		Cost cost = null;						//the cost of converting source to target
		Cost temp = null;						//the cost of using a user defined conversion to convert source to target
				 
		boolean hasWorse = false;				//currFn has a worse parameter fit than bestFn
		boolean hasBetter = false;				//currFn has a better parameter fit than bestFn
		boolean ambiguous = false;				//ambiguity, 2 functions are equally good
		boolean currHasAmbiguousParam = false;	//currFn has an ambiguous parameter conversion (ok if not bestFn)
		boolean bestHasAmbiguousParam = false;  //bestFn has an ambiguous parameter conversion (not ok, ambiguous)

		Object [] sourceParameters = null;			//the parameters the function is being called with
		IASTParameterDeclaration [] targetParameters = null;			//the current function's parameters
		
		int numFns = fns.size();
		int numSourceParams = ( data.functionParameters != null ) ? data.functionParameters.length : 0;
		if( data.functionParameters != null && numSourceParams == 0 )
			numSourceParams = 1;
		sourceParameters = data.functionParameters;
		
		for( int fnIdx = 0; fnIdx < numFns; fnIdx++ ){
			currFn = (IFunction) fns.get( fnIdx );
			
			if( bestFn != null ){
				if( bestFn == currFn )
					continue;
			}
			
			ICPPASTFunctionDeclarator currDtor = (ICPPASTFunctionDeclarator) currFn.getPhysicalNode();
			targetParameters = currDtor.getParameters();

			int numTargetParams = ( targetParameters.length == 0 ) ? 1 : targetParameters.length;
			if( currFnCost == null ){
				currFnCost = new Cost [ (numSourceParams == 0) ? 1 : numSourceParams ];	
			}
			
			comparison = 0;
			boolean varArgs = false;
			
			for( int j = 0; j < numSourceParams || j == 0; j++ ){
				source = getSourceParameterType( sourceParameters, j );
				
				if( j < numTargetParams ){
					if( targetParameters.length == 0  && j == 0 ){
						target = VOID_TYPE;
					} else {
					IParameter param = (IParameter) targetParameters[j].getDeclarator().getName().resolveBinding(); 
					target = param.getType();
					}
				} else 
					varArgs = true;
				
				if( varArgs ){
					cost = new Cost( source, null );
					cost.rank = Cost.ELLIPSIS_CONVERSION;
				} /*else if ( target.getHasDefault() && source.isType( ITypeInfo.t_void ) && !source.hasPtrOperators() ){
					//source is just void, ie no parameter, if target had a default, then use that
					cost = new Cost( source, target );
					cost.rank = Cost.IDENTITY_RANK;
				}*/ else if( source.equals( target ) ){
					cost = new Cost( source, target );
					cost.rank = Cost.IDENTITY_RANK;	//exact match, no cost
				} else {
					cost = checkStandardConversionSequence( source, target );
					
					//12.3-4 At most one user-defined conversion is implicitly applied to
					//a single value.  (also prevents infinite loop)				
					if( cost.rank == Cost.NO_MATCH_RANK && !data.forUserDefinedConversion ){
						temp = checkUserDefinedConversionSequence( source, target );
						if( temp != null ){
							cost = temp;
						}
					}
				}
				
				currFnCost[ j ] = cost;
			}
			
			
			hasWorse = false;
			hasBetter = false;
			//In order for this function to be better than the previous best, it must
			//have at least one parameter match that is better that the corresponding
			//match for the other function, and none that are worse.
			for( int j = 0; j < numSourceParams; j++ ){ 
				if( currFnCost[ j ].rank < 0 ){
					hasWorse = true;
					hasBetter = false;
					break;
				}
				
				//an ambiguity in the user defined conversion sequence is only a problem
				//if this function turns out to be the best.
				currHasAmbiguousParam = ( currFnCost[ j ].userDefined == 1 );
				
				if( bestFnCost != null ){
					comparison = currFnCost[ j ].compare( bestFnCost[ j ] );
					hasWorse |= ( comparison < 0 );
					hasBetter |= ( comparison > 0 );
				} else {
					hasBetter = true;
				}
			}
			
			//If function has a parameter match that is better than the current best,
			//and another that is worse (or everything was just as good, neither better nor worse).
			//then this is an ambiguity (unless we find something better than both later)	
			ambiguous |= ( hasWorse && hasBetter ) || ( !hasWorse && !hasBetter );
			
			if( !hasWorse ){
				if( hasBetter ){
					//the new best function.
					ambiguous = false;
					bestFnCost = currFnCost;
					bestHasAmbiguousParam = currHasAmbiguousParam;
					currFnCost = null;
					bestFn = currFn;
				} 
			}
		}

		
		if( ambiguous || bestHasAmbiguousParam ){
			return new ProblemBinding( IProblemBinding.SEMANTIC_AMBIGUOUS_LOOKUP, data.name );
		}
						
		return bestFn;
	}
	
	static private Cost checkStandardConversionSequence( IType source, IType target ) {
		Cost cost = lvalue_to_rvalue( source, target );
		
		if( cost.source == null || cost.target == null ){
			return cost;
		}
			
		if( cost.source.equals( cost.target ) ){
			cost.rank = Cost.IDENTITY_RANK;
			return cost;
		}
	
		qualificationConversion( cost );
		
		//if we can't convert the qualifications, then we can't do anything
		if( cost.qualification == 0 ){
			return cost;
		}
		
		//was the qualification conversion enough?
		IType s = getUltimateType( cost.source );
		IType t = getUltimateType( cost.target );
		if( s.equals( t ) ){
			return cost;
		}
		
		promotion( cost );
		if( cost.promotion > 0 || cost.rank > -1 ){
			return cost;
		}
		
		conversion( cost );
		
		if( cost.rank > -1 )
			return cost;
		
		derivedToBaseConversion( cost );
		
		return cost;	
	}
	
	static private Cost checkUserDefinedConversionSequence( IType source, IType target ) {
		Cost cost = null;
//		Cost constructorCost = null;
//		Cost conversionCost = null;

//		IType s = getUltimateType( source );
//		IType t = getUltimateType( target );
//		//ISymbol sourceDecl = null;
//		IASTName constructor = null;
//		IASTName conversion = null;
//		
//		//constructors
//		if( t instanceof ICPPClassType ){
//			LookupData data = new LookupData( EMPTY_NAME_ARRAY );
//			data.forUserDefinedConversion = true;
//			data.functionParameters = new Object [] { source };
//			
//			if( !container.getConstructors().isEmpty() ){
//				ArrayList constructors = new ArrayList( container.getConstructors() );
//				constructor = resolveFunction( data, constructors );
//			}
//			if( constructor != null && constructor.getTypeInfo().checkBit( ITypeInfo.isExplicit ) ){
//				constructor = null;
//			}
//		}
		
		//conversion operators
//		if( source.getType() == ITypeInfo.t_type ){
//			source = getFlatTypeInfo( source, provider );
//			sourceDecl = ( source != null ) ? source.getTypeSymbol() : null;
//			
//			if( sourceDecl != null && (sourceDecl instanceof IContainerSymbol) ){
//				char[] name = target.toCharArray();
//				
//				if( !CharArrayUtils.equals( name, EMPTY_NAME_ARRAY) ){
//				    
//					LookupData data = new LookupData( CharArrayUtils.concat( OPERATOR_, name )){ //$NON-NLS-1$
//						public List getParameters() { return Collections.EMPTY_LIST; }
//						public TypeFilter getFilter() { return FUNCTION_FILTER; }
//					};
//					data.forUserDefinedConversion = true;
//					data.foundItems = lookupInContained( data, (IContainerSymbol) sourceDecl );
//					conversion = (data.foundItems != null ) ? (IParameterizedSymbol)resolveAmbiguities( data ) : null;	
//				}
//			}
//		}
//		
//		if( constructor != null ){
//			IType info = provider.getTypeInfo( ITypeInfo.t_type );
//			info.setTypeSymbol( constructor.getContainingSymbol() );
//			constructorCost = checkStandardConversionSequence( info, target );
//		}
//		if( conversion != null ){
//			IType info = provider.getTypeInfo( target.getType() );
//			info.setTypeSymbol( target.getTypeSymbol() );
//			conversionCost = checkStandardConversionSequence( info, target );
//		}
		
//		//if both are valid, then the conversion is ambiguous
//		if( constructorCost != null && constructorCost.rank != Cost.NO_MATCH_RANK && 
//			conversionCost != null && conversionCost.rank != Cost.NO_MATCH_RANK )
//		{
//			cost = constructorCost;
//			cost.userDefined = Cost.AMBIGUOUS_USERDEFINED_CONVERSION;	
//			cost.rank = Cost.USERDEFINED_CONVERSION_RANK;
//		} else {
//			if( constructorCost != null && constructorCost.rank != Cost.NO_MATCH_RANK ){
//				cost = constructorCost;
//				cost.userDefined = constructor.hashCode();
//				cost.rank = Cost.USERDEFINED_CONVERSION_RANK;
//			} else if( conversionCost != null && conversionCost.rank != Cost.NO_MATCH_RANK ){
//				cost = conversionCost;
//				cost.userDefined = conversion.hashCode();
//				cost.rank = Cost.USERDEFINED_CONVERSION_RANK;
//			} 			
//		}
		return cost;
	}

	static protected IType getUltimateType( IType type ){
		while( true ){
			if( type instanceof ITypedef )
				type = ((ITypedef)type).getType();
			else if( type instanceof IQualifierType )
				type = ((IQualifierType)type).getType();
			else if( type instanceof IPointerType )
				type = ((IPointerType) type).getType();
			else if( type instanceof ICPPReferenceType )
				type = ((ICPPReferenceType)type).getType();
			else 
				return type;
		}
	}
	
	static private boolean isCompleteType( IType type ){
		type = getUltimateType( type );
		if( type instanceof ICPPClassType ){
			if( ((ICPPClassType) type).getPhysicalNode() instanceof ICPPASTElaboratedTypeSpecifier )
				return false;
		}
		return true;
	}
	static private Cost lvalue_to_rvalue( IType source, IType target ){
		Cost cost = new Cost( source, target );
		
		if( ! isCompleteType( source ) ){
			cost.rank = Cost.NO_MATCH_RANK;
			return cost;
		}
		
		if( source instanceof ICPPReferenceType ){
			source = ((ICPPReferenceType) source).getType();
		}
		if( target instanceof ICPPReferenceType ){
			target = ((ICPPReferenceType) target).getType();
			cost.targetHadReference = true;
		}
			
		cost.source = source;
		cost.target = target;
		
		return cost;
	}
	
	static private void qualificationConversion( Cost cost ){
		boolean canConvert = true;

		IPointerType op1, op2;
		IType s = cost.source, t = cost.target;
		boolean constInEveryCV2k = true;
		while( true ){
			 op1 = null;
			 op2 = null;
			 while( true ){
			 	if( s instanceof ITypedef )	
			 		s = ((ITypedef)s).getType();
			 	else {
			 		if( s instanceof IPointerType )		
			 			op1 = (IPointerType) s;	
			 		break;
			 	}
			 }
			 while( true ){
			 	if( t instanceof ITypedef )	
			 		t = ((ITypedef)t).getType();
			 	else {
			 		if( t instanceof IPointerType )		
			 			op1 = (IPointerType) t;	
			 		break;
			 	}
			 }
			 if( op1 == null && op2 == null )
			 	break;
			 else if( op1 == null ^ op2 == null) {
			 	canConvert = false; 
			 	break;
			 }
			 
			 //if const is in cv1,j then const is in cv2,j.  Similary for volatile
			if( ( op1.isConst() && !op2.isConst() ) || ( op1.isVolatile() && !op2.isVolatile() ) ) {
				canConvert = false;
				break;
			}
			//if cv1,j and cv2,j are different then const is in every cv2,k for 0<k<j
			if( !constInEveryCV2k && ( op1.isConst() != op2.isConst() ||
									   op1.isVolatile() != op2.isVolatile() ) )
			{
				canConvert = false;
				break; 
			}
			constInEveryCV2k &= op2.isConst();
		}
		
		if( s instanceof IQualifierType ^ t instanceof IQualifierType ){
			canConvert = false;
		} else if( s instanceof IQualifierType && t instanceof IQualifierType ){
			IQualifierType qs = (IQualifierType) s, qt = (IQualifierType) t;
			if( qs.isConst() && !qt.isConst() || qs.isVolatile() && !qt.isVolatile() )
				canConvert = false;
		}

		if( canConvert == true ){
			cost.qualification = 1;
			cost.rank = Cost.LVALUE_OR_QUALIFICATION_RANK;
		} else {
			cost.qualification = 0;
		}
	}
	
	/**
	 * 
	 * @param source
	 * @param target
	 * @return int
	 * 
	 * 4.5-1 char, signed char, unsigned char, short int or unsigned short int
	 * can be converted to int if int can represent all the values of the source
	 * type, otherwise they can be converted to unsigned int.
	 * 4.5-2 wchar_t or an enumeration can be converted to the first of the
	 * following that can hold it: int, unsigned int, long unsigned long.
	 * 4.5-4 bool can be promoted to int 
	 * 4.6 float can be promoted to double
	 */
	static private void promotion( Cost cost ){
		IType src = getUltimateType( cost.source );
		IType trg = getUltimateType( cost.target );
		 
		if( src.equals( trg ) )
			return;
		
		if( src instanceof IBasicType && trg instanceof IBasicType ){
			int sType = ((IBasicType)src).getType();
			int tType = ((IBasicType)trg).getType();
			if( ( tType == IBasicType.t_int && ( sType == IBasicType.t_char    || 
												 sType == ICPPBasicType.t_bool || 
												 sType == ICPPBasicType.t_wchar_t ) ) ||
				( tType == IBasicType.t_double && sType == IBasicType.t_float ) )
			{
				cost.promotion = 1; 
			}
		} else if( src instanceof IEnumeration && trg instanceof IBasicType &&
				   ((IBasicType) trg).getType() == IBasicType.t_int )
		{
			cost.promotion = 1; 
		}
		
		cost.rank = (cost.promotion > 0 ) ? Cost.PROMOTION_RANK : Cost.NO_MATCH_RANK;
	}
	static private void conversion( Cost cost ){
		IType src = cost.source;
		IType trg = cost.target;
		
		int temp = -1;
		
		cost.conversion = 0;
		cost.detail = 0;
		
//		if( !src.hasSamePtrs( trg ) ){
//			return;
//		} 
//		
		IType s = getUltimateType( src );
		IType t = getUltimateType( trg );
		
		
		if( s instanceof ICPPClassType ){
			//4.10-2 an rvalue of type "pointer to cv T", where T is an object type can be
			//converted to an rvalue of type "pointer to cv void"
			if( t instanceof IBasicType && ((IBasicType)t).getType() == IBasicType.t_void ){
				cost.rank = Cost.CONVERSION_RANK;
				cost.conversion = 1;
				cost.detail = 2;
				return;
			}
			//4.10-3 An rvalue of type "pointer to cv D", where D is a class type can be converted
			//to an rvalue of type "pointer to cv B", where B is a base class of D.
			else if( t instanceof ICPPClassType ){
				temp = hasBaseClass( (ICPPClassType)s, (ICPPClassType) t, true );
				cost.rank = ( temp > -1 ) ? Cost.CONVERSION_RANK : Cost.NO_MATCH_RANK;
				cost.conversion = ( temp > -1 ) ? temp : 0;
				cost.detail = 1;
				return;
			}
		} else if( t instanceof IBasicType && s instanceof IBasicType || s instanceof IEnumeration ){
			//4.7 An rvalue of an integer type can be converted to an rvalue of another integer type.  
			//An rvalue of an enumeration type can be converted to an rvalue of an integer type.
			cost.rank = Cost.CONVERSION_RANK;
			cost.conversion = 1;	
		}
		
		//TODO
//		else if( ptr.getType() == ITypeInfo.PtrOp.t_memberPointer ){
//			//4.11-2 An rvalue of type "pointer to member of B of type cv T", where B is a class type, 
//			//can be converted to an rvalue of type "pointer to member of D of type cv T" where D is a
//			//derived class of B
//			ITypeInfo.PtrOp srcPtr =  trg.hasPtrOperators() ? (ITypeInfo.PtrOp)trg.getPtrOperators().get(0) : null;
//			if( trgDecl.isType( srcDecl.getType() ) && srcPtr != null && srcPtr.getType() == ITypeInfo.PtrOp.t_memberPointer ){
//				try {
//					temp = hasBaseClass( ptr.getMemberOf(), srcPtr.getMemberOf() );
//				} catch (ParserSymbolTableException e) {
//					//not going to happen since we didn't ask for the visibility exception
//				}
//				cost.rank = ( temp > -1 ) ? Cost.CONVERSION_RANK : Cost.NO_MATCH_RANK;
//				cost.detail = 1;
//				cost.conversion = ( temp > -1 ) ? temp : 0;
//				return; 
//			}
//		}
		
	}
	
	static private void derivedToBaseConversion( Cost cost ) {
		IType s = getUltimateType( cost.source );
		IType t = getUltimateType( cost.target );
		
		if( cost.targetHadReference && s instanceof ICPPClassType && t instanceof ICPPClassType ){
			int temp = hasBaseClass( (ICPPClassType) s, (ICPPClassType) t, true );
			
			if( temp > -1 ){
				cost.rank = Cost.DERIVED_TO_BASE_CONVERSION;
				cost.conversion = temp;
			}	
		}
	}

	static private int hasBaseClass( ICPPClassType symbol, ICPPClassType base, boolean needVisibility ) {
		if( symbol == base ){
			return 0;
		}
		ICPPClassType parent = null;
		ICPPBase [] bases = symbol.getBases();
		
		for( int i = 0; i < bases.length; i ++ ){
			ICPPBase wrapper = bases[i];	
			parent = bases[i].getBaseClass();
			boolean isVisible = ( wrapper.getVisibility() == ICPPBase.v_public);

			if( parent == base ){
				if( needVisibility && !isVisible )
					return -1;
				return 1;
			} 
			int n = hasBaseClass( parent, base, needVisibility );
			if( n > 0 )
				return n + 1;
		}
		return -1;
	}
}
