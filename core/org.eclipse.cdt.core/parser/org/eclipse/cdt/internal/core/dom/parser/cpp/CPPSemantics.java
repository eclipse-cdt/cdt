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

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTCastExpression;
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
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStandardFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IQualifierType;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;
import org.eclipse.cdt.core.dom.ast.c.ICASTFieldDesignator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFieldReference;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceAlias;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDirective;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBasicType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPCompositeBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPPointerToMemberType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTNamespaceDefinition;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.CharArrayObjectMap;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.core.parser.util.ObjectMap;
import org.eclipse.cdt.core.parser.util.ObjectSet;
import org.eclipse.cdt.core.parser.util.ArrayUtil.ArrayWrapper;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;

/**
 * @author aniefer
 */
public class CPPSemantics {

	public static final char[] EMPTY_NAME_ARRAY = new char[0];
	public static final String EMPTY_NAME = ""; //$NON-NLS-1$
	public static final char[] OPERATOR_ = new char[] {'o','p','e','r','a','t','o','r',' '};  //$NON-NLS-1$
	public static final IType VOID_TYPE = new CPPBasicType( IBasicType.t_void, 0 );
	
	static protected class LookupData
	{
		protected IASTName astName;
		public char[] name;
		public ObjectMap usingDirectives = ObjectMap.EMPTY_MAP; 
		public ObjectSet visited = ObjectSet.EMPTY_SET;	//used to ensure we don't visit things more than once
		public ObjectSet inheritanceChain;	//used to detect circular inheritance
		public ObjectSet associated = ObjectSet.EMPTY_SET;
		
		public boolean ignoreUsingDirectives = false;
		public boolean usingDirectivesOnly = false;
		public boolean forceQualified = false;
		public boolean forUserDefinedConversion = false;
		public boolean forAssociatedScopes = false;
		public boolean prefixLookup = false;
		
		public Object foundItems = null;
		public Object [] functionParameters;
		public ProblemBinding problem;
        
		
		public LookupData( IASTName n ){
			astName = n;
			this.name = n.toCharArray();
		}
		public LookupData( char [] n ){
			astName = null;
			this.name = n;
		}
		public boolean includeBlockItem( IASTNode item ){
		    if( ( astName != null && astName.getParent() instanceof IASTIdExpression ) || 
		        item instanceof IASTNamespaceDefinition  ||
	           (item instanceof IASTSimpleDeclaration && ((IASTSimpleDeclaration)item).getDeclSpecifier() instanceof IASTCompositeTypeSpecifier ) )
		    {
		        return true;
		    }
		    return false;
		}
		public boolean typesOnly(){
			if( astName == null ) return false;
			IASTNode parent = astName.getParent();
			if( parent instanceof ICPPASTBaseSpecifier || parent instanceof ICPPASTElaboratedTypeSpecifier )
			    return true;
			if( parent instanceof ICPPASTQualifiedName ){
			    IASTName [] ns = ((ICPPASTQualifiedName)parent).getNames();
			    return ( astName != ns[ ns.length -1 ] );
			}
			return false;
		}
		public boolean forUsingDeclaration(){
			if( astName == null ) return false;
			IASTNode p1 = astName.getParent();
			IASTNode p2 = p1.getParent();
			return ( ( p1 instanceof ICPPASTUsingDeclaration ) ||
				     ( p1 instanceof ICPPASTQualifiedName && p2 instanceof ICPPASTUsingDeclaration ) );
		}
		public boolean forDefinition(){
			if( astName == null ) return false;
			IASTNode p1 = astName.getParent();
			IASTNode p2 = p1.getParent();
			
			return ( ( p1 instanceof ICPPASTQualifiedName && p2 instanceof IASTDeclarator ) ||
				     ( p1 instanceof IASTDeclarator && p2 instanceof IASTSimpleDeclaration) );
		}
		public boolean considerConstructors(){
			if( astName == null ) return false;
			IASTNode p1 = astName.getParent();
			IASTNode p2 = p1.getParent();
			
			if( p1 instanceof ICPPASTNamedTypeSpecifier && p2 instanceof IASTTypeId )
				return p2.getParent() instanceof ICPPASTNewExpression;
			else if( p1 instanceof ICPPASTQualifiedName && p2 instanceof ICPPASTFunctionDeclarator ){
				IASTName[] names = ((ICPPASTQualifiedName)p1).getNames();
				if( names.length >= 2 && names[ names.length - 1 ] == astName )
				    return CPPVisitor.isConstructor( names[ names.length - 2 ], (IASTDeclarator) p2 );
			} else if( p1 instanceof ICPPASTQualifiedName && p2 instanceof ICPPASTNamedTypeSpecifier ){
				IASTNode p3 = p2.getParent();
				return p3 instanceof IASTTypeId && p3.getParent() instanceof ICPPASTNewExpression;
			}
			return false;
		}
		public boolean qualified(){
		    if( forceQualified ) return true;
			if( astName == null ) return false;
			IASTNode p1 = astName.getParent();
			if( p1 instanceof ICPPASTQualifiedName ){
				return ((ICPPASTQualifiedName)p1).getNames()[0] != astName;
			}
			return p1 instanceof ICPPASTFieldReference;
		}
		public boolean functionCall(){
		    if( astName == null ) return false;
		    IASTNode p1 = astName.getParent();
		    if( p1 instanceof ICPPASTQualifiedName )
		        p1 = p1.getParent();
		    return ( p1 instanceof IASTIdExpression && p1.getPropertyInParent() == IASTFunctionCallExpression.FUNCTION_NAME );
		}
        public boolean checkWholeClassScope() {
            if( astName == null ) return false;
            ASTNodeProperty prop = astName.getPropertyInParent();
            if( prop == IASTIdExpression.ID_NAME || 
				prop == IASTFieldReference.FIELD_NAME || 
				prop == ICASTFieldDesignator.FIELD_NAME ||
				prop == ICPPASTUsingDirective.QUALIFIED_NAME ||
				prop == ICPPASTUsingDeclaration.NAME ||
				prop == IASTFunctionCallExpression.FUNCTION_NAME ||
				prop == ICPPASTUsingDeclaration.NAME ||
				prop == IASTNamedTypeSpecifier.NAME )
            {
                return true;
            }
            return false;
        }
        public boolean hasResults(){
            if( foundItems == null )
                return false;
            if( foundItems instanceof Object [] )
                return ((Object[])foundItems).length != 0;
            if( foundItems instanceof CharArrayObjectMap )
                return ((CharArrayObjectMap)foundItems).size() != 0;
            return false;
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

		public int compare( Cost cost ) throws DOMException{
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
                                try {
                                    t1 = ((ITypedef)t1).getType();
                                } catch ( DOMException e ) {
                                    t1 = e.getProblem();
                                }
                            else {
								if( t1 instanceof IPointerType )		
									op1 = (IPointerType) t1;	
								break;
							}
						}
						while( true ){
							if( t2 instanceof ITypedef )
                                try {
                                    t2 = ((ITypedef)t2).getType();
                                } catch ( DOMException e ) {
                                    t2 = e.getProblem();
                                }
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
//	    if( name instanceof ICPPASTQualifiedName && ((ICPPASTQualifiedName)name).isFullyQualified() ) 
//	       return ((ICPPASTTranslationUnit)name.getTranslationUnit()).resolveBinding();
	       
		//1: get some context info off of the name to figure out what kind of lookup we want
		LookupData data = createLookupData( name, true );
		
		try {
            //2: lookup
            lookup( data, name );
        } catch ( DOMException e1 ) {
            data.problem = (ProblemBinding) e1.getProblem();
        }
		
		if( data.problem != null )
		    return data.problem;
		
		//3: resolve ambiguities
		IBinding binding;
        try {
            binding = resolveAmbiguities( data, name );
        } catch ( DOMException e2 ) {
            binding = e2.getProblem();
        }
        //4: post processing
		binding = postResolution( binding, data );
		return binding;
	}

	/**
     * @param binding
     * @param data
     * @param name
     * @return
     */
    private static IBinding postResolution( IBinding binding, LookupData data ) {
        if( !(data.astName instanceof ICPPASTQualifiedName) && data.functionCall() ){
            //3.4.2 argument dependent name lookup, aka Koenig lookup
            try {
                IScope scope = (binding != null ) ? binding.getScope() : null;
                if( data.associated.size() > 0 && ( scope == null|| !(scope instanceof ICPPClassScope) ) ){
                    Object [] assoc = new Object[ data.associated.size() ];
                    System.arraycopy( data.associated.keyArray(), 0, assoc, 0, assoc.length );
                    data.ignoreUsingDirectives = true;
                    data.forceQualified = true;
                    for( int i = 0; i < assoc.length; i++ ){
                        if( data.associated.containsKey( assoc[i] ) )
                            lookup( data, assoc[i] );
                    }
                    
                    binding = resolveAmbiguities( data, data.astName );
                }
            } catch ( DOMException e ) {
                binding = e.getProblem();
            }
        }
            
        if( binding instanceof ICPPClassType && data.considerConstructors() ){
		    ICPPClassType cls = (ICPPClassType) binding;
		    try {
                //force resolution of constructor bindings
                cls.getConstructors();
                //then use the class scope to resolve which one.
    		    binding = ((ICPPClassScope)cls.getCompositeScope()).getBinding( data.astName );
            } catch ( DOMException e ) {
                binding = e.getProblem();
            }
		    
		}        
        if( data.astName.getPropertyInParent() == IASTNamedTypeSpecifier.NAME && !( binding instanceof IType || binding instanceof ICPPConstructor) ){
            binding = new ProblemBinding( IProblemBinding.SEMANTIC_INVALID_TYPE, data.name );
        }
        
		if( binding != null && data.forDefinition() && !( binding instanceof IProblemBinding ) ){
			addDefinition( binding, data.astName );
		}
		if( binding == null )
			binding = new ProblemBinding(IProblemBinding.SEMANTIC_NAME_NOT_FOUND, data.name );
        return binding;
    }

    static private CPPSemantics.LookupData createLookupData( IASTName name, boolean considerAssociatedScopes ){
		CPPSemantics.LookupData data = new CPPSemantics.LookupData( name );
		IASTNode parent = name.getParent();
		if( parent instanceof ICPPASTQualifiedName ){
			parent = parent.getParent();
		}
		
		if( parent instanceof ICPPASTFunctionDeclarator ){
			data.functionParameters = ((ICPPASTFunctionDeclarator)parent).getParameters();
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
		} else if( parent instanceof ICPPASTFieldReference && parent.getParent() instanceof IASTFunctionCallExpression ){
		    IASTExpression exp = ((IASTFunctionCallExpression)parent.getParent()).getParameterExpression();
			if( exp instanceof IASTExpressionList )
				data.functionParameters = ((IASTExpressionList) exp ).getExpressions();
			else if( exp != null )
				data.functionParameters = new IASTExpression [] { exp };
			else
				data.functionParameters = IASTExpression.EMPTY_EXPRESSION_ARRAY;
		} else if( parent instanceof ICPPASTNamedTypeSpecifier && parent.getParent() instanceof IASTTypeId ){
	        IASTTypeId typeId = (IASTTypeId) parent.getParent();
	        if( typeId.getParent() instanceof ICPPASTNewExpression ){
	            ICPPASTNewExpression newExp = (ICPPASTNewExpression) typeId.getParent();
	            IASTExpression init = newExp.getNewInitializer();
	            if( init instanceof IASTExpressionList )
					data.functionParameters = ((IASTExpressionList) init ).getExpressions();
				else if( init != null )
					data.functionParameters = new IASTExpression [] { init };
				else
					data.functionParameters = IASTExpression.EMPTY_EXPRESSION_ARRAY;
	        }
		}
		
		if( considerAssociatedScopes && !(name.getParent() instanceof ICPPASTQualifiedName) && data.functionCall() ){
		    data.associated = getAssociatedScopes( data );
		}
		
		return data;
	}
    
    static private ObjectSet getAssociatedScopes( LookupData data ) {
        Object [] ps = data.functionParameters;
        ObjectSet namespaces = new ObjectSet(2);
        ObjectSet classes = new ObjectSet(2);
        for( int i = 0; i < ps.length; i++ ){
            IType p = getSourceParameterType( ps, i );
            p = getUltimateType( p, true );
            try {
                getAssociatedScopes( p, namespaces, classes );
            } catch ( DOMException e ) {
            }
        }
        return namespaces;
    }

    static private void getAssociatedScopes( IType t, ObjectSet namespaces, ObjectSet classes ) throws DOMException{
        //3.4.2-2 
		if( t instanceof ICPPClassType ){
		    if( !classes.containsKey( t ) ){
		        classes.put( t );
		        namespaces.put( getContainingNamespaceScope( (IBinding) t ) );

			    ICPPClassType cls = (ICPPClassType) t;
			    ICPPBase[] bases = cls.getBases();
			    for( int i = 0; i < bases.length; i++ ){
			        getAssociatedScopes( bases[i].getBaseClass(), namespaces, classes );
			    }
		    }
		} else if( t instanceof IEnumeration ){
		    namespaces.put( getContainingNamespaceScope( (IBinding) t ) );
		} else if( t instanceof IFunctionType ){
		    IFunctionType ft = (IFunctionType) t;
		    
		    getAssociatedScopes( getUltimateType( ft.getReturnType(), true ), namespaces, classes );
		    IType [] ps = ft.getParameterTypes();
		    for( int i = 0; i < ps.length; i++ ){
		        getAssociatedScopes( getUltimateType( ps[i], true ), namespaces, classes );
		    }
		} else if( t instanceof ICPPPointerToMemberType ){
		    IBinding binding = ((ICPPPointerToMemberType)t).getMemberOfClass();
		    if( binding instanceof IType )
		        getAssociatedScopes( (IType)binding, namespaces, classes );
		    getAssociatedScopes( getUltimateType( ((ICPPPointerToMemberType)t).getType(), true ), namespaces, classes );
		}
		return;
    }
    
    static private ICPPNamespaceScope getContainingNamespaceScope( IBinding binding ) throws DOMException{
        if( binding == null ) return null;
        IScope scope = binding.getScope();
        while( scope != null && !(scope instanceof ICPPNamespaceScope) ){
            scope = scope.getParent();
        }
        return (ICPPNamespaceScope) scope;
    }
    
	static private ICPPScope getLookupScope( IASTName name ) throws DOMException{
	    IASTNode parent = name.getParent();
	    
    	if( parent instanceof ICPPASTBaseSpecifier ) {
	        IASTNode n = CPPVisitor.getContainingBlockItem( parent );
	        return (ICPPScope) CPPVisitor.getContainingScope( n );
	    } else if( parent instanceof ICPPASTConstructorChainInitializer ){
	    	ICPPASTConstructorChainInitializer initializer = (ICPPASTConstructorChainInitializer) parent;
	    	IASTFunctionDeclarator dtor = (IASTFunctionDeclarator) initializer.getParent();
	    	return (ICPPScope) dtor.getName().resolveBinding().getScope();
	    	
	    }
	    return (ICPPScope) CPPVisitor.getContainingScope( name );
	}
	private static void mergeResults( LookupData data, Object results, boolean scoped ){
	    if( !data.prefixLookup ){
	        if( results instanceof IBinding ){
	            data.foundItems = ArrayUtil.append( Object.class, (Object[]) data.foundItems, results );
	        } else if( results instanceof Object[] ){
	            data.foundItems = ArrayUtil.addAll( Object.class, (Object[])data.foundItems, (Object[])results );
	        }
	    } else {
	        Object [] objs = (Object[]) results;
	        CharArrayObjectMap resultMap = (CharArrayObjectMap) data.foundItems;
	        if( objs != null ) {
	    		for( int i = 0; i < objs.length && objs[i] != null; i++ ){
	    		    char [] n = null;
	    		    if( objs[i] instanceof IBinding )
	    		        n = ((IBinding)objs[i]).getNameCharArray();
	    		    else
	    		        n = ((IASTName)objs[i]).toCharArray();
    		        if( !resultMap.containsKey( n ) ){
	    				resultMap.put( n, objs[i] );
	    			} else if( !scoped ) {
	    			    Object obj = resultMap.get( n );
	    			    if( obj instanceof Object [] ) {
	    			        if( objs[i] instanceof IBinding )
	    			            obj = ArrayUtil.append( Object.class, (Object[]) obj, objs[i] );
	    			        else
	    			            obj = ArrayUtil.addAll( Object.class, (Object[])obj, (Object[]) objs[i] );
	    			    } else {
	    			        if( objs[i] instanceof IBinding )
	    			            obj = new Object [] { obj, objs[i] };
	    			        else {
	    			            Object [] temp = new Object [ ((Object[])objs[i]).length + 1 ];
	    			            temp[0] = obj;
	    			            obj = ArrayUtil.addAll( Object.class, temp, (Object[]) objs[i] );
	    			        }
	    			    } 
	    			}
	    		}
	        }
	    }
	}
	static private void lookup( CPPSemantics.LookupData data, Object start ) throws DOMException{
		IASTNode node = data.astName;

		ICPPScope scope = null;
		if( start instanceof ICPPScope )
		    scope = (ICPPScope) start;
		else
		    scope = getLookupScope( (IASTName) start );
		
		while( scope != null ){
			IASTNode blockItem = CPPVisitor.getContainingBlockItem( node );
			if( scope.getPhysicalNode() != blockItem.getParent() && !(scope instanceof ICPPNamespaceScope) )
				blockItem = node;
			
			ArrayWrapper directives = null;
			if( !data.usingDirectivesOnly ){
				IBinding binding = data.prefixLookup ? null : scope.getBinding( data.astName );
				if( binding == null ){
				    directives = new ArrayWrapper();
				    mergeResults( data, lookupInScope( data, scope, blockItem, directives ), true );
				} else {
				    mergeResults( data, binding, true );
				}
			}
				
			if( !data.ignoreUsingDirectives ) {
				data.visited.clear();
				if( data.prefixLookup || !data.hasResults() ){
					Object[] transitives = lookupInNominated( data, scope, null );
					
					processDirectives( data, scope, transitives );
					if( directives != null && directives.array != null && directives.array.length != 0 )
						processDirectives( data, scope, directives.array );
					
					while( !data.usingDirectives.isEmpty() && data.usingDirectives.get( scope ) != null ){
						transitives = lookupInNominated( data, scope, transitives );
		
						if( !data.qualified() || ( data.prefixLookup || !data.hasResults()) ){
							processDirectives( data, scope, transitives );
						}
					}
				}
			}
			
			if( !data.prefixLookup && (data.problem != null || data.hasResults()) )
				return;
			
			if( !data.usingDirectivesOnly && scope instanceof ICPPClassScope ){
				mergeResults( data, lookupInParents( data, (ICPPClassScope) scope ), true );
			}
			
			if( !data.prefixLookup && (data.problem != null || data.hasResults()) )
				return;
			
			//if still not found, loop and check our containing scope
			if( data.qualified() && !data.usingDirectives.isEmpty() )
				data.usingDirectivesOnly = true;
			
			if( blockItem != null )
				node = blockItem;
			scope = (ICPPScope) scope.getParent();
		}
	}

	private static IASTName[] lookupInParents( CPPSemantics.LookupData data, ICPPClassScope lookIn ) throws DOMException{
		ICPPASTCompositeTypeSpecifier compositeTypeSpec = (ICPPASTCompositeTypeSpecifier) lookIn.getPhysicalNode();
		ICPPASTBaseSpecifier [] bases = compositeTypeSpec.getBaseSpecifiers();
	
		IASTName[] inherited = null;
		IASTName[] result = null;
		
		if( bases.length == 0 )
			return null;
				
		//use data to detect circular inheritance
		if( data.inheritanceChain == null )
			data.inheritanceChain = new ObjectSet( 2 );
		
		data.inheritanceChain.put( lookIn );
			
		int size = bases.length;
		for( int i = 0; i < size; i++ )
		{
			ICPPClassType cls = null;
			IBinding binding = bases[i].getName().resolveBinding();
			while( binding instanceof ITypedef && ((ITypedef)binding).getType() instanceof IBinding ){
				binding = (IBinding) ((ITypedef)binding).getType();
			}
			if( binding instanceof ICPPClassType )
				cls = (ICPPClassType) binding;
			else 
				continue;
			ICPPClassScope parent = (ICPPClassScope) cls.getCompositeScope();
			
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
					
					if( inherited == null || inherited.length == 0 ){
						inherited = lookupInParents( data, parent );
					}
				} else {
				    data.problem = new ProblemBinding( IProblemBinding.SEMANTIC_CIRCULAR_INHERITANCE, bases[i].getName().toCharArray() );
				    return null;
				}
			}	
			
			if( inherited != null && inherited.length != 0 ){
				if( result == null || result.length == 0 ){
					result = inherited;
				} else if ( inherited != null && inherited.length != 0 ) {
					for( int j = 0; j < result.length && result[j] != null; j++ ) {
						IASTName n = result[j];
						if( checkAmbiguity( n, inherited ) ){
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

	private static boolean checkAmbiguity( IASTName n, IASTName []names ) throws DOMException{
	    names = (IASTName[]) ArrayUtil.trim( IASTName.class, names );
	    if( names.length == 0 )
	        return false;

	    //it is not ambiguous if they are the same thing and it is static or an enumerator
	    IBinding binding = n.resolveBinding();
	    for( int i = 0; i < names.length && names[i] != null; i++ ){
	        
	        IBinding b = names[i].resolveBinding();
	        
	        if( binding != b )
	            return true;
	        if( binding instanceof IEnumerator )
	            continue;
	        else if( (binding instanceof IFunction && ((IFunction)binding).isStatic()) ||
	                 (binding instanceof IVariable && ((IVariable)binding).isStatic()) )
	            continue;
	        
	        return true;
	    }
		return false;
	}

	static private void processDirectives( CPPSemantics.LookupData data, IScope scope, Object[] directives ) throws DOMException{
		if( directives == null || directives.length == 0 )
			return;
		
		ICPPScope enclosing = null;
		IScope temp = null;
		
		int size = directives.length;
		for( int i = 0; i < size && directives[i] != null; i++ ){
			Object d = directives[i];
			IBinding binding = null;
			if( d instanceof ICPPASTUsingDirective ){
				binding = ((ICPPASTUsingDirective)d).getQualifiedName().resolveBinding();
			} else if( d instanceof ICPPASTNamespaceDefinition ){
				binding = ((ICPPASTNamespaceDefinition)d).getName().resolveBinding();
			}
			if( binding instanceof ICPPNamespace ){
				temp = ((ICPPNamespace)binding).getNamespaceScope();
			} else
				continue;
				
			//namespace are searched at most once
			if( !data.visited.containsKey( temp ) ){
				enclosing = getClosestEnclosingScope( scope, temp );
				
				//data.usingDirectives is a map from enclosing scope to a IScope[]
				//of namespaces to consider when we reach that enclosing scope
				IScope [] scopes = (IScope[]) ( data.usingDirectives.isEmpty() ? null : data.usingDirectives.get( enclosing ) );
				scopes = (IScope[]) ArrayUtil.append( IScope.class, scopes, temp );
				if( data.usingDirectives == ObjectMap.EMPTY_MAP ){
					data.usingDirectives = new ObjectMap(2);
				}
				data.usingDirectives.put( enclosing, scopes );
			}
		}
		
	}

	static private ICPPScope getClosestEnclosingScope( IScope scope1, IScope scope2 ) throws DOMException{
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
	 * @throws DOMException
	 */
	static private IASTName[] lookupInScope( CPPSemantics.LookupData data, ICPPScope scope, IASTNode blockItem, ArrayWrapper usingDirectives ) throws DOMException {
		IASTName possible = null;
		IASTNode [] nodes = null;
		IASTNode parent = scope.getPhysicalNode();
		
		IASTName [] namespaceDefs = null;
		int namespaceIdx = -1;
		
		if( data.associated.containsKey( scope ) ){
			//we are looking in scope, remove it from the associated scopes list
			data.associated.remove( scope );
		}
		
		IASTName[] found = null;
		
		if( parent instanceof IASTCompoundStatement ){
		    if( parent.getParent() instanceof IASTFunctionDefinition ){
		        ICPPASTFunctionDeclarator dtor = (ICPPASTFunctionDeclarator) ((IASTFunctionDefinition)parent.getParent()).getDeclarator();
		        nodes = dtor.getParameters();
		    } 
		    if( nodes == null || nodes.length == 0 ){
				IASTCompoundStatement compound = (IASTCompoundStatement) parent;
				nodes = compound.getStatements();
		    }
		} else if ( parent instanceof IASTTranslationUnit ){
			IASTTranslationUnit translation = (IASTTranslationUnit) parent;
			nodes = translation.getDeclarations();
		} else if ( parent instanceof ICPPASTCompositeTypeSpecifier ){
			ICPPASTCompositeTypeSpecifier comp = (ICPPASTCompositeTypeSpecifier) parent;
			nodes = comp.getMembers();
		} else if ( parent instanceof ICPPASTNamespaceDefinition ){
		    //need binding because namespaces can be split
		    CPPNamespace namespace = (CPPNamespace) ((ICPPASTNamespaceDefinition)parent).getName().resolveBinding();
		    namespaceDefs = namespace.getNamespaceDefinitions();
		    
			nodes = ((ICPPASTNamespaceDefinition)namespaceDefs[0].getParent()).getDeclarations();
			namespaceIdx = 0;
		} else if( parent instanceof ICPPASTFunctionDeclarator ){
		    ICPPASTFunctionDeclarator dtor = (ICPPASTFunctionDeclarator) parent;
	        nodes = dtor.getParameters();
		}
		
		int idx = -1;
		boolean checkWholeClassScope = ( scope instanceof ICPPClassScope ) && data.checkWholeClassScope();
		IASTNode item = ( nodes != null ? (nodes.length > 0 ? nodes[++idx] : null ) : parent );
	
		while( item != null ) {
			if( !checkWholeClassScope && blockItem != null && ((ASTNode)item).getOffset() > ((ASTNode) blockItem).getOffset() )
				break;
			
			if( item != blockItem || data.includeBlockItem( item ) ){
				if( !data.ignoreUsingDirectives &&
					( item instanceof ICPPASTUsingDirective  ||
					  (item instanceof ICPPASTNamespaceDefinition &&
					   ((ICPPASTNamespaceDefinition)item).getName().toCharArray().length == 0) ) )
				{
					if( usingDirectives != null )
						usingDirectives.array = ArrayUtil.append( usingDirectives.array, item );
				} else {
					possible = collectResult( data, scope, item, (item == parent)  );
					if( possible != null ){
					    found = (IASTName[]) ArrayUtil.append( IASTName.class, found, possible );
					}
				}
			}
			if( item == blockItem && !checkWholeClassScope )
				break;
			if( idx > -1 && ++idx < nodes.length ){
				item = nodes[idx];
			} else {
			    item = null;
			
			    if( namespaceIdx > -1 ) {
			        //check all definitions of this namespace
				    while( namespaceIdx > -1 && namespaceDefs.length > ++namespaceIdx ){
				        nodes = ((ICPPASTNamespaceDefinition)namespaceDefs[namespaceIdx].getParent()).getDeclarations();
					    if( nodes.length > 0 ){
					        idx = 0;
					        item = nodes[0];
					        break;
					    }     
				    }
			    } else if( parent instanceof IASTCompoundStatement && nodes instanceof IASTParameterDeclaration [] ){
			    	//function body, we were looking at parameters, now check the body itself
			        IASTCompoundStatement compound = (IASTCompoundStatement) parent;
					nodes = compound.getStatements(); 
					if( nodes.length > 0 ){
				        idx = 0;
				        item = nodes[0];
				        break;
				    }  
			    }
			}
		}
		return found;
	}

	static private Object[] lookupInNominated( CPPSemantics.LookupData data, ICPPScope scope, Object[] transitives ) throws DOMException{
		if( data.usingDirectives.isEmpty() )
			return transitives;
		
		ICPPScope temp = null;
		
		IScope [] directives = (IScope[]) data.usingDirectives.remove( scope );
		if( directives == null || directives.length == 0 ) {
			return transitives;
		}
		for( int i = 0; i < directives.length && directives[i] != null; i++ ){
			temp = (ICPPScope) directives[i];
			if( !data.visited.containsKey( temp ) ){
				if( data.visited == ObjectSet.EMPTY_SET ) {
					data.visited = new ObjectSet(2);
				}
				data.visited.put( temp );
				ArrayWrapper usings = new ArrayWrapper();
				IASTName[] found = lookupInScope( data, temp, null, usings );
				mergeResults( data, found, false );
				
				//only consider the transitive using directives if we are an unqualified
				//lookup, or we didn't find the name in decl
				if( usings.array != null && usings.array.length > 0 && (!data.qualified() || found == null ) ){
				    transitives = ArrayUtil.addAll( Object.class, transitives, usings.array );
				}
			}
		}
		return transitives;
	}

	static private IASTName collectResult( CPPSemantics.LookupData data, ICPPScope scope, IASTNode node, boolean checkAux ){
	    IASTDeclaration declaration = null;
	    if( node instanceof IASTDeclaration ) 
	        declaration = (IASTDeclaration) node;
		if( node instanceof IASTDeclarationStatement )
			declaration = ((IASTDeclarationStatement)node).getDeclaration();
		else if( node instanceof IASTForStatement )
			declaration = ((IASTForStatement)node).getInitDeclaration();
		else if( node instanceof IASTParameterDeclaration && !data.typesOnly() ){
		    IASTParameterDeclaration parameterDeclaration = (IASTParameterDeclaration) node;
		    IASTDeclarator dtor = parameterDeclaration.getDeclarator();
		    while( dtor.getNestedDeclarator() != null )
		    	dtor = dtor.getNestedDeclarator();
			IASTName declName = dtor.getName();
			if( nameMatches( data, declName.toCharArray() ) ) {
		        return declName;
		    }
		}
		if( declaration == null )
			return null;
		
		if( declaration instanceof IASTSimpleDeclaration ){
			IASTSimpleDeclaration simpleDeclaration = (IASTSimpleDeclaration) declaration;
			if( !data.typesOnly() ) { 
				IASTDeclarator [] declarators = simpleDeclaration.getDeclarators();
				for( int i = 0; i < declarators.length; i++ ){
					IASTDeclarator declarator = declarators[i];
					while( declarator.getNestedDeclarator() != null )
						declarator = declarator.getNestedDeclarator();
					if( data.considerConstructors() || !CPPVisitor.isConstructor( scope, declarator ) ){
						IASTName declaratorName = declarator.getName();
						if( nameMatches( data, declaratorName.toCharArray() ) ) {
							return declaratorName;
						}
					}
				}
			}
	
			//decl spec 
			IASTDeclSpecifier declSpec = simpleDeclaration.getDeclSpecifier();
			if( declSpec instanceof IASTElaboratedTypeSpecifier ){
				IASTName elabName = ((IASTElaboratedTypeSpecifier)declSpec).getName();
				if( nameMatches( data, elabName.toCharArray() ) ) {
					return elabName;
				}
			} else if( declSpec instanceof ICPPASTCompositeTypeSpecifier ){
				IASTName compName = ((IASTCompositeTypeSpecifier)declSpec).getName();
				if( nameMatches( data, compName.toCharArray() ) ) {
					return compName;
				}
			} else if( declSpec instanceof IASTEnumerationSpecifier ){
			    IASTEnumerationSpecifier enumeration = (IASTEnumerationSpecifier) declSpec;
			    IASTName eName = enumeration.getName();
			    if( nameMatches( data, eName.toCharArray() ) ) {
					return eName;
				}
			    if( !data.typesOnly() ) {
				    //check enumerators too
				    IASTEnumerator [] list = enumeration.getEnumerators();
				    for( int i = 0; i < list.length; i++ ) {
				        IASTEnumerator enumerator = list[i];
				        if( enumerator == null ) break;
				        eName = enumerator.getName();
				        if( nameMatches( data, eName.toCharArray() ) ) {
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
			if( nameMatches( data, name.toCharArray() ) ) {
				return name;
			}
		} else if( declaration instanceof ICPPASTNamespaceDefinition ){
			IASTName namespaceName = ((ICPPASTNamespaceDefinition) declaration).getName();
			if( nameMatches( data, namespaceName.toCharArray() ) )
				return namespaceName;
		} else if( declaration instanceof ICPPASTNamespaceAlias ){
			IASTName alias = ((ICPPASTNamespaceAlias) declaration).getAlias();
			if( nameMatches( data, alias.toCharArray() ) )
				return alias;
		}
		
		if( data.typesOnly() )
		    return null;
		
		if( declaration instanceof IASTFunctionDefinition ){
			IASTFunctionDefinition functionDef = (IASTFunctionDefinition) declaration;
			IASTFunctionDeclarator declarator = functionDef.getDeclarator();
			
			//check the function itself
			IASTName declName = declarator.getName();
			if( data.considerConstructors() || !CPPVisitor.isConstructor( scope, declarator ) ){
			    if( nameMatches( data, declName.toCharArray() ) ) {
					return declName;
				}
			}
			if( checkAux ) {
				//check the parameters
				if ( declarator instanceof IASTStandardFunctionDeclarator ) {
					IASTParameterDeclaration []  parameters = ((IASTStandardFunctionDeclarator)declarator).getParameters();
					for( int i = 0; i < parameters.length; i++ ){
						IASTParameterDeclaration parameterDeclaration = parameters[i];
						if( parameterDeclaration == null ) break;
						IASTDeclarator dtor = parameterDeclaration.getDeclarator();
						while( dtor.getNestedDeclarator() != null )
							dtor = dtor.getNestedDeclarator();
						declName = dtor.getName();
						if( nameMatches( data, declName.toCharArray() ) ) {
							return declName;
						}
					}
				}
			}
		} 
		
		return null;
	}

	private static final boolean nameMatches( LookupData data, char[] potential ){
	    return ( (data.prefixLookup && CharArrayUtils.equals( potential, 0, data.name.length, data.name )) || 
			     (!data.prefixLookup && CharArrayUtils.equals( potential, data.name )) );
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

	static protected IBinding resolveAmbiguities( IASTName name, IBinding[] bindings ){
	    bindings = (IBinding[]) ArrayUtil.trim( IBinding.class, bindings );
	    if( bindings == null || bindings.length == 0 )
	        return null;
	    else if( bindings.length == 1 )
	        return bindings[ 0 ];
	    
	    LookupData data = createLookupData( name, false );
	    data.foundItems = bindings;
	    try {
            return resolveAmbiguities( data, name );
        } catch ( DOMException e ) {
            return e.getProblem();
        }
	}
	
	static private boolean declaredBefore( IBinding binding, IASTNode node ){
	    if( binding instanceof ICPPBinding ){
	        ICPPBinding cpp = (ICPPBinding) binding;
	        IASTNode[] n = cpp.getDeclarations();
	        if( n != null && n.length > 0 )
	            return (((ASTNode) n[0]).getOffset() <= ((ASTNode)node).getOffset() );
	        else if( cpp.getDefinition() != null )
	            return (((ASTNode) cpp.getDefinition()).getOffset() <= ((ASTNode)node).getOffset() );
	        else 
	            return true;
	    }
	    return false;
	}
	
	static private IBinding resolveAmbiguities( CPPSemantics.LookupData data, IASTName name ) throws DOMException {
	    if( !data.hasResults() || data.prefixLookup )
	        return null;
	      
	    IBinding type = null;
	    IBinding obj  = null;
	    IBinding temp = null;
	    IFunction[] fns = null;
	    
	    Object [] items = (Object[]) data.foundItems;
	    for( int i = 0; i < items.length && items[i] != null; i++ ){
	        Object o = items[i];
	        if( o instanceof IASTName )
	            temp = ((IASTName) o).resolveBinding();
	        else if( o instanceof IBinding ){
	            temp = (IBinding) o;
	            if( !declaredBefore( temp, name ) )
	                continue;
	        } else
	            continue;

	        if( temp instanceof ICPPCompositeBinding ){
	        	IBinding [] bindings = ((ICPPCompositeBinding) temp).getBindings();
	        	//data.foundItems = ArrayUtil.addAll( Object.class, data.foundItems, bindings );
	        	mergeResults( data, bindings, false );
	        	continue;
	        } else if( temp instanceof IType ){
	        	if( type == null ){
	                type = temp;
	            } else {
	                return new ProblemBinding( IProblemBinding.SEMANTIC_AMBIGUOUS_LOOKUP, data.name );
	            }
	        } else if( temp instanceof IFunction ){
	        	fns = (IFunction[]) ArrayUtil.append( IFunction.class, fns, temp );
	        } else {
	        	if( obj == null )
	        		obj = temp;
	        	else if( obj != temp ){
	        	    return new ProblemBinding( IProblemBinding.SEMANTIC_AMBIGUOUS_LOOKUP, data.name );
	        	}
	        }
	    }
	    
	    if( type != null ) {
	    	if( data.typesOnly() || (obj == null && fns == null) )
	    		return type;
	    	IScope typeScope = type.getScope();
	    	if( obj != null && obj.getScope() != typeScope ){
	    	    return new ProblemBinding( IProblemBinding.SEMANTIC_AMBIGUOUS_LOOKUP, data.name );
	    	} else if( fns != null ){
	    		for( int i = 0; i < fns.length && fns[i] != null; i++ ){
	    			if( ((IBinding)fns[i]).getScope() != typeScope )
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
	
	static private boolean functionHasParameters( IFunction function, IASTParameterDeclaration [] params ) throws DOMException{
		IFunctionType ftype = function.getType();
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
	
	static private void reduceToViable( LookupData data, IBinding[] functions ) throws DOMException{
	    if( functions == null || functions.length == 0 )
	        return;
	    
		Object [] fParams = data.functionParameters;
		int numParameters = ( fParams != null ) ? fParams.length : 0;		
		int num;	
			
		//Trim the list down to the set of viable functions
		IFunction fName;
		ICPPASTFunctionDeclarator function = null;
		int size = functions.length;
		for( int i = 0; i < size && functions[i] != null; i++ ){
			fName = (IFunction) functions[i];
			function = (ICPPASTFunctionDeclarator) ((ICPPBinding)fName).getDefinition();
			if( function == null ){
			    IASTNode [] nodes = ((ICPPBinding) fName).getDeclarations();
			    if( nodes != null && nodes.length > 0 )
			    	function = (ICPPASTFunctionDeclarator) nodes[0];
			}

			if( function == null ){
			    //implicit member function, for now, not supporting default values or var args
			    num = fName.getParameters().length;
			} else { 
			    num = function.getParameters().length;
			}
		
			//if there are m arguments in the list, all candidate functions having m parameters
			//are viable	 
			if( num == numParameters ){
				if( data.forDefinition() && !functionHasParameters( fName, (IASTParameterDeclaration[]) data.functionParameters ) ){
					functions[i] = null;
				}
				continue;
			} else if( function == null ){
			    functions[i] = null;
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
				functions[i] = null;
			} 
			//a candidate function having more than m parameters is viable only if the (m+1)-st
			//parameter has a default argument
			else {
				IASTParameterDeclaration [] params = function.getParameters();
				for( int j = num - 1; j > ( numParameters - num); j-- ){
					if( params[j].getDeclarator().getInitializer() == null ){
					    functions[i] = null;
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
	static private IBinding resolveFunction( CPPSemantics.LookupData data, IBinding[] fns ) throws DOMException{
	    fns = (IBinding[]) ArrayUtil.trim( IBinding.class, fns );
	    if( fns == null || fns.length == 0 )
	        return null;
	    
		if( data.forUsingDeclaration() ){
			if( fns.length == 1 )
				return fns[ 0 ];
			return new CPPCompositeBinding( fns );
		}
		
		//we don't have any arguments with which to resolve the function
		if( data.functionParameters == null ){
		    return resolveTargetedFunction( data, fns );
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
		IParameter [] targetBindings = null;
		int targetLength = 0;
		
		int numFns = fns.length;
		int numSourceParams = ( data.functionParameters != null ) ? data.functionParameters.length : 0;
		if( data.functionParameters != null && numSourceParams == 0 )
			numSourceParams = 1;
		sourceParameters = data.functionParameters;
		
		for( int fnIdx = 0; fnIdx < numFns; fnIdx++ ){
			currFn = (IFunction) fns[fnIdx];
			
			if( currFn == null || bestFn == currFn )
				continue;
			
			ICPPASTFunctionDeclarator currDtor = (ICPPASTFunctionDeclarator) ((ICPPBinding)currFn).getDefinition();
			if( currDtor == null ){
			    IASTNode[] nodes = ((ICPPBinding) currFn).getDeclarations();
			    if( nodes != null && nodes.length > 0 )
			        currDtor = (ICPPASTFunctionDeclarator) nodes[0];
			}
			targetParameters = ( currDtor != null ) ? currDtor.getParameters() : null;

			if( targetParameters == null ){
			    targetBindings = currFn.getParameters();
			    targetLength = targetBindings.length;
			} else {
			    targetLength = targetParameters.length;
			}
			int numTargetParams = ( targetLength == 0 ) ? 1 : targetLength;
			
			if( currFnCost == null ){
				currFnCost = new Cost [ (numSourceParams == 0) ? 1 : numSourceParams ];	
			}
			
			comparison = 0;
			boolean varArgs = false;
			
			for( int j = 0; j < numSourceParams || j == 0; j++ ){
				source = getSourceParameterType( sourceParameters, j );
				if( source instanceof IProblemBinding )
					return (IBinding) source;
				
				if( j < numTargetParams ){
					if( targetLength == 0  && j == 0 ){
						target = VOID_TYPE;
					} else if( targetParameters != null ) {
						IParameter param = (IParameter) targetParameters[j].getDeclarator().getName().resolveBinding(); 
						target = param.getType();
					} else {
					    target = targetBindings[j].getType();
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
			for( int j = 0; j < numSourceParams || j == 0; j++ ){ 
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
	
	/**
	 * 13.4-1 A use of an overloaded function without arguments is resolved in certain contexts to a function
     * @param data
     * @param fns
     * @return
     */
    private static IBinding resolveTargetedFunction( LookupData data, IBinding[] fns ) {
        if( fns.length == 1 )
            return fns[0];
        
        if( data.forAssociatedScopes ){
            return new CPPCompositeBinding( fns );
        }
        
        IBinding result = null;
        
        Object o = getTargetType( data );
        IType type, types[] = null;
        int idx = -1;
        if( o instanceof IType [] ){
            types = (IType[]) o;
            type = types[ ++idx ];
        } else
            type = (IType) o;
        
        while( type != null ){
            type = (type != null) ? getUltimateType( type, false ) : null;
            if( type == null || !( type instanceof IFunctionType ) )
                return new ProblemBinding( IProblemBinding.SEMANTIC_AMBIGUOUS_LOOKUP, data.name );

            for( int i = 0; i < fns.length; i++ ){
                IFunction fn = (IFunction) fns[i];
                IType ft = null;
                try {
                     ft = fn.getType();
                } catch ( DOMException e ) {
                    ft = e.getProblem();
                }
                if( type.equals( ft ) ){
                    if( result == null )
                        result = fn;
                    else
                        return new ProblemBinding( IProblemBinding.SEMANTIC_AMBIGUOUS_LOOKUP, data.name );
                }
            }

            if( idx > 0 && ++idx < types.length  ){
                type = types[idx];
            } else {
                type = null;
            }
        }
                
        return ( result != null ) ? result : new ProblemBinding( IProblemBinding.SEMANTIC_AMBIGUOUS_LOOKUP, data.name ); 
    }

    private static Object getTargetType( LookupData data ){
        IASTName name = data.astName;
        
        if( name.getPropertyInParent() == ICPPASTQualifiedName.SEGMENT_NAME )
            name = (IASTName) name.getParent();
        
        if( name.getPropertyInParent() != IASTIdExpression.ID_NAME )
            return null;
        
        IASTIdExpression idExp = (IASTIdExpression) name.getParent();
        IASTNode node = idExp.getParent();
        ASTNodeProperty prop = null;
        while( node != null ){
            prop = node.getPropertyInParent();
            //target is an object or reference being initialized
            if( prop == IASTInitializerExpression.INITIALIZER_EXPRESSION ){
                IASTInitializerExpression initExp = (IASTInitializerExpression) node.getParent();
                IASTDeclarator dtor = (IASTDeclarator) initExp.getParent();
                return CPPVisitor.createType( dtor );
            }
            //target is the left side of an assignment
            else if( prop == IASTBinaryExpression.OPERAND_TWO && 
                     ((IASTBinaryExpression)node.getParent()).getOperator() == IASTBinaryExpression.op_assign )
            {
                IASTBinaryExpression binaryExp = (IASTBinaryExpression) node.getParent();
                IASTExpression exp = binaryExp.getOperand1();
                return CPPVisitor.getExpressionType( exp );
            }
            //target is a parameter of a function
            else if( prop == IASTFunctionCallExpression.PARAMETERS ||
                     (prop == IASTExpressionList.NESTED_EXPRESSION && node.getParent().getPropertyInParent() == IASTFunctionCallExpression.PARAMETERS ) )
            {
                //if this function call refers to an overloaded function, there is more than one possiblity
                //for the target type
                IASTFunctionCallExpression fnCall = null;
                int idx = -1;
                if( prop == IASTFunctionCallExpression.PARAMETERS ){
                    fnCall = (IASTFunctionCallExpression) node.getParent();
                    idx = 0;
                } else {
                    IASTExpressionList list = (IASTExpressionList) node.getParent();
                    fnCall = (IASTFunctionCallExpression) list.getParent();
                    IASTExpression [] exps = list.getExpressions();
                    for( int i = 0; i < exps.length; i++ ){
                        if( exps[i] == node ){
                            idx = i;
                            break;
                        }
                    }
                }
                IFunctionType [] types = getPossibleFunctions( fnCall );
                if( types == null ) return null;
                IType [] result = null;
                for( int i = 0; i < types.length && types[i] != null; i++ ){
                    IType [] pts = null;
                    try {
                        pts = types[i].getParameterTypes();
                    } catch ( DOMException e ) {
                        continue;
                    }
                    if( pts.length > idx )
                        result = (IType[]) ArrayUtil.append( IType.class, result, pts[idx] );
                }
                return result;
            }
            //target is an explicit type conversion
//            else if( prop == ICPPASTSimpleTypeConstructorExpression.INITIALIZER_VALUE )
//            {
//                
//            }
            //target is an explicit type conversion
            else if( prop == IASTCastExpression.OPERAND )
            {
            	IASTCastExpression cast = (IASTCastExpression) node.getParent();
            	return CPPVisitor.createType( cast.getTypeId().getAbstractDeclarator() );
            }
            //target is the return value of a function, operator or conversion
            else if( prop == IASTReturnStatement.RETURNVALUE )
            {
            	while( !( node instanceof IASTFunctionDefinition ) ){
            		node = node.getParent();
            	}
            	IASTDeclarator dtor = ((IASTFunctionDefinition)node).getDeclarator();
            	while( dtor.getNestedDeclarator() != null )
            		dtor = dtor.getNestedDeclarator();
            	IBinding binding = dtor.getName().resolveBinding();
            	if( binding instanceof IFunction ){
            		try {
	            		IFunctionType ft = ((IFunction)binding).getType();
	            		return ft.getReturnType();
            		} catch ( DOMException e ) {
            		}
            	}
            }
            
            else if( prop == IASTUnaryExpression.OPERAND ){
                IASTUnaryExpression parent = (IASTUnaryExpression) node.getParent();
                if( parent.getOperator() == IASTUnaryExpression.op_bracketedPrimary ||
                    parent.getOperator() == IASTUnaryExpression.op_amper)
                {
                    node = parent;
                	continue;
                }
            }
            break;
        }
        return null;
    }
    
    static private IFunctionType [] getPossibleFunctions( IASTFunctionCallExpression call ){
        IFunctionType [] result = null;
        
        IASTExpression exp = call.getFunctionNameExpression();
        if( exp instanceof IASTIdExpression ){
            IASTIdExpression idExp = (IASTIdExpression) exp;
            IASTName name = idExp.getName();
	        LookupData data = createLookupData( name, false );
			try {
	            lookup( data, name );
	        } catch ( DOMException e1 ) {
	            return null;
	        }
	        if( data.hasResults() ){
	            Object [] items = (Object[]) data.foundItems;
	            IBinding temp = null;
	            for( int i = 0; i < items.length; i++ ){
	                Object o = items[i];
	                if( o == null ) break;
	                if( o instanceof IASTName )
	    	            temp = ((IASTName) o).resolveBinding();
	    	        else if( o instanceof IBinding ){
	    	            temp = (IBinding) o;
	    	            if( !declaredBefore( temp, name ) )
	    	                continue;
	    	        } else
	    	            continue;
	                
	                try {
		                if( temp instanceof IFunction ){
		                    result = (IFunctionType[]) ArrayUtil.append( IFunctionType.class, result, ((IFunction)temp).getType() );
		                } else if( temp instanceof IVariable ){
                            IType type = getUltimateType( ((IVariable) temp).getType(), false );
                            if( type instanceof IFunctionType )
                                result = (IFunctionType[]) ArrayUtil.append( IFunctionType.class, result, type );
		                }
	                } catch( DOMException e ){
	                }
	            }
	        }
        } else {
            IType type = CPPVisitor.getExpressionType( exp );
            type = getUltimateType( type, false );
            if( type instanceof IFunctionType ){
                result = new IFunctionType[] { (IFunctionType) type };
            }
        }
        return result;
    }
    
    static private Cost checkStandardConversionSequence( IType source, IType target ) throws DOMException {
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
		IType s = getUltimateType( cost.source, true );
		IType t = getUltimateType( cost.target, true );
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
	
	static private Cost checkUserDefinedConversionSequence( IType source, IType target ) throws DOMException {
		Cost cost = null;
		Cost constructorCost = null;
		Cost conversionCost = null;

		IType s = getUltimateType( source, true );
		IType t = getUltimateType( target, true );

		ICPPConstructor constructor = null;
		ICPPMethod conversion = null;
		
		//constructors
		if( t instanceof ICPPClassType ){
			LookupData data = new LookupData( EMPTY_NAME_ARRAY );
			data.forUserDefinedConversion = true;
			data.functionParameters = new Object [] { source };
			ICPPConstructor [] constructors = ((ICPPClassType)t).getConstructors();
			
			if( constructors.length > 0 ){
			    //the list out of Arrays.asList does not support remove, which we need
				constructor = (ICPPConstructor) resolveFunction( data, constructors );
			}
			if( constructor != null && constructor.isExplicit() ){
				constructor = null;
			}
		}
		
		//conversion operators
		if( s instanceof ICPPClassType ){
			char[] name = EMPTY_NAME_ARRAY;//TODO target.toCharArray();
			
			if( !CharArrayUtils.equals( name, EMPTY_NAME_ARRAY) ){
				LookupData data = new LookupData( CharArrayUtils.concat( OPERATOR_, name ));
				data.functionParameters = IASTExpression.EMPTY_EXPRESSION_ARRAY;
				data.forUserDefinedConversion = true;
				
				ICPPScope scope = (ICPPScope) ((ICPPClassType) s).getCompositeScope();
				data.foundItems = lookupInScope( data, scope, null, null );
				IBinding [] fns = (IBinding[]) ArrayUtil.append( IBinding.class, null, data.foundItems );
				conversion = (ICPPMethod) ( (data.foundItems != null ) ? resolveFunction( data, fns ) : null );	
			}
		}
		
		if( constructor != null ){
			constructorCost = checkStandardConversionSequence( t, target );
		}
		if( conversion != null ){
			conversionCost = checkStandardConversionSequence( conversion.getType().getReturnType(), target );
		}
		
		//if both are valid, then the conversion is ambiguous
		if( constructorCost != null && constructorCost.rank != Cost.NO_MATCH_RANK && 
			conversionCost != null && conversionCost.rank != Cost.NO_MATCH_RANK )
		{
			cost = constructorCost;
			cost.userDefined = Cost.AMBIGUOUS_USERDEFINED_CONVERSION;	
			cost.rank = Cost.USERDEFINED_CONVERSION_RANK;
		} else {
			if( constructorCost != null && constructorCost.rank != Cost.NO_MATCH_RANK ){
				cost = constructorCost;
				cost.userDefined = constructor.hashCode();
				cost.rank = Cost.USERDEFINED_CONVERSION_RANK;
			} else if( conversionCost != null && conversionCost.rank != Cost.NO_MATCH_RANK ){
				cost = conversionCost;
				cost.userDefined = conversion.hashCode();
				cost.rank = Cost.USERDEFINED_CONVERSION_RANK;
			} 			
		}
		return cost;
	}

	static protected IType getUltimateType( IType type, boolean stopAtPointerToMember ){
	    try {
	        while( true ){
				if( type instanceof ITypedef )
				    type = ((ITypedef)type).getType();
	            else if( type instanceof IQualifierType )
					type = ((IQualifierType)type).getType();
	            else if( stopAtPointerToMember && type instanceof ICPPPointerToMemberType )
	                return type;
				else if( type instanceof IPointerType )
					type = ((IPointerType) type).getType();
				else if( type instanceof ICPPReferenceType )
					type = ((ICPPReferenceType)type).getType();
				else 
					return type;
			}
        } catch ( DOMException e ) {
            return e.getProblem();
        }
	}
	
	static private boolean isCompleteType( IType type ){
		type = getUltimateType( type, false );
		if( type instanceof ICPPClassType && ((ICPPBinding)type).getDefinition() == null ){
			return false;
		}
		return true;
	}
	static private Cost lvalue_to_rvalue( IType source, IType target ) throws DOMException{
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
	
	static private void qualificationConversion( Cost cost ) throws DOMException{
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
			 			op2 = (IPointerType) t;	
			 		break;
			 	}
			 }
			 if( op1 == null && op2 == null )
			 	break;
			 else if( op1 == null ^ op2 == null) {
			 	canConvert = false; 
			 	break;
			 } else if( op1 instanceof ICPPPointerToMemberType ^ op2 instanceof ICPPPointerToMemberType ){
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
			s = op1.getType();
			t = op2.getType();
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
	 * @throws DOMException
	 */
	static private void promotion( Cost cost ) throws DOMException{
		IType src = getUltimateType( cost.source, true );
		IType trg = getUltimateType( cost.target, true );
		 
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
	static private void conversion( Cost cost ) throws DOMException{
		IType src = cost.source;
		IType trg = cost.target;
		
		int temp = -1;
		
		cost.conversion = 0;
		cost.detail = 0;

		IType s = getUltimateType( src, true );
		IType t = getUltimateType( trg, true );
				
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
		} else if( s instanceof ICPPPointerToMemberType && t instanceof ICPPPointerToMemberType ){
		    //4.11-2 An rvalue of type "pointer to member of B of type cv T", where B is a class type, 
			//can be converted to an rvalue of type "pointer to member of D of type cv T" where D is a
			//derived class of B
		    ICPPPointerToMemberType spm = (ICPPPointerToMemberType) s;
		    ICPPPointerToMemberType tpm = (ICPPPointerToMemberType) t;
		    if( spm.getType().equals( tpm.getType() ) ){
		        temp = hasBaseClass( tpm.getMemberOfClass(), spm.getMemberOfClass(), false );
		        cost.rank = ( temp > -1 ) ? Cost.CONVERSION_RANK : Cost.NO_MATCH_RANK;
				cost.conversion = ( temp > -1 ) ? temp : 0;
				cost.detail = 1;
		    }
		}		
	}
	
	static private void derivedToBaseConversion( Cost cost ) throws DOMException {
		IType s = getUltimateType( cost.source, true );
		IType t = getUltimateType( cost.target, true );
		
		if( cost.targetHadReference && s instanceof ICPPClassType && t instanceof ICPPClassType ){
			int temp = hasBaseClass( (ICPPClassType) s, (ICPPClassType) t, true );
			
			if( temp > -1 ){
				cost.rank = Cost.DERIVED_TO_BASE_CONVERSION;
				cost.conversion = temp;
			}	
		}
	}

	static private int hasBaseClass( IBinding symbol, IBinding base, boolean needVisibility ) throws DOMException {
		if( symbol == base ){
			return 0;
		}
		ICPPClassType clsSymbol = null;
		ICPPClassType clsBase = null;
		IType temp = null;
		while( symbol instanceof ITypedef ){
		    temp = ((ITypedef)symbol).getType();
		    if( temp instanceof IBinding )
		        symbol = (IBinding) temp;
		    else return -1;
		}
		if( symbol instanceof ICPPClassType )
		    clsSymbol = (ICPPClassType) symbol;
		else return -1;
		
		while( base instanceof ITypedef ){
		    temp = ((ITypedef)base).getType();
		    if( temp instanceof IBinding )
		        base= (IBinding) temp;
		    else return -1;
		}
		if( base instanceof ICPPClassType )
		    clsBase = (ICPPClassType) base;
		else return -1;
		
		
		ICPPClassType parent = null;
		ICPPBase [] bases = clsSymbol.getBases();
		
		for( int i = 0; i < bases.length; i ++ ){
			ICPPBase wrapper = bases[i];	
			parent = bases[i].getBaseClass();
			boolean isVisible = ( wrapper.getVisibility() == ICPPBase.v_public);

			if( parent == clsBase ){
				if( needVisibility && !isVisible )
					return -1;
				return 1;
			} 
			int n = hasBaseClass( parent, clsBase, needVisibility );
			if( n > 0 )
				return n + 1;
		}
		return -1;
	}
	
	/**
	 * Find the binding for the type for the given name, if the given name is not a type, or can not 
	 * be resolved, null is returned.
	 * @param mostRelevantScope
	 * @param name
	 * @return
	 */
	public static IBinding findTypeBinding( IASTNode mostRelevantScope, IASTName name ){
		IScope scope = null;
		if( mostRelevantScope instanceof IASTCompoundStatement )
			scope = ((IASTCompoundStatement) mostRelevantScope).getScope();
		else if ( mostRelevantScope instanceof IASTTranslationUnit )
			scope = ((IASTTranslationUnit) mostRelevantScope).getScope();
		else if ( mostRelevantScope instanceof ICPPASTNamespaceDefinition )
			scope = ((ICPPASTNamespaceDefinition) mostRelevantScope).getScope();
		else if( mostRelevantScope instanceof ICPPASTCompositeTypeSpecifier )
			scope = ((ICPPASTCompositeTypeSpecifier) mostRelevantScope).getScope();
		
		if( scope == null )
			return null;
		
		LookupData data = new LookupData( name ){
			public boolean typesOnly(){ return true; }
			public boolean forUsingDeclaration(){ return false; }
			public boolean forDefinition(){ return false; }
			public boolean considerConstructors(){ return false; }
			public boolean functionCall(){ return false; }
			public boolean qualified(){ 
				IASTNode p1 = astName.getParent();
				if( p1 instanceof ICPPASTQualifiedName ){
					return ((ICPPASTQualifiedName)p1).getNames()[0] != astName;
				}
				return false;
			}
		};
		
		try {
			lookup( data, scope );
		} catch (DOMException e) {
			return null;
		}
		IBinding binding = null;
        try {
            binding = resolveAmbiguities( data, name );
        } catch ( DOMException e2 ) {
        }
        
		return binding;
	}
	
    public static IBinding [] prefixLookup( IASTName name ){
        LookupData data = createLookupData( name, true );
        data.prefixLookup = true;
        data.foundItems = new CharArrayObjectMap( 2 );
        
        try {
            lookup( data, name );
        } catch ( DOMException e ) {
        }
        CharArrayObjectMap map = (CharArrayObjectMap) data.foundItems;
        IBinding [] result = null;
        if( !map.isEmpty() ){
            char [] key = null;
            Object obj = null;
            int size = map.size(); 
            for( int i = 0; i < size; i++ ) {
                key = map.keyAt( i );
                obj = map.get( key );
                if( obj instanceof IBinding )
                    result = (IBinding[]) ArrayUtil.append( IBinding.class, result, obj );
                else {
                    Object item = null;
                    if( obj instanceof Object[] ){
                        Object[] objs = (Object[]) obj;
                        if( objs.length > 1 && objs[1] != null )
                            continue;
                        item = objs[0];
                    } else {
                        item = obj;
                    }
                    
                    if( item instanceof IBinding )
                        result = (IBinding[]) ArrayUtil.append( IBinding.class, result, item );
                    else {
                        IBinding binding = ((IASTName) item).resolveBinding();
                        if( binding != null && !(binding instanceof IProblemBinding))
                            result = (IBinding[]) ArrayUtil.append( IBinding.class, result, binding );
                    }
                        
                }
            }
        }
        return (IBinding[]) ArrayUtil.trim( IBinding.class, result );
    }

}
