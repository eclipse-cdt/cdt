/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
/*
 * Created on Dec 8, 2004
 */
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.IPDOM;
import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTArraySubscriptExpression;
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
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerExpression;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IArrayType;
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
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCatchHandler;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExplicitTemplateInstantiation;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFieldReference;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTForStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTIfStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLinkageSpecification;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceAlias;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTOperatorName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSwitchStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDirective;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTWhileStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBasicType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPDelegate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMember;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPPointerToMemberType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateNonTypeParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTypeParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.CharArrayObjectMap;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.core.parser.util.ObjectMap;
import org.eclipse.cdt.core.parser.util.ObjectSet;
import org.eclipse.cdt.core.parser.util.ArrayUtil.ArrayWrapper;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;

/**
 * @author aniefer
 */
public class CPPSemantics {

    protected static final ASTNodeProperty STRING_LOOKUP_PROPERTY = new ASTNodeProperty("CPPSemantics.STRING_LOOKUP_PROPERTY - STRING_LOOKUP"); //$NON-NLS-1$
	public static final char[] EMPTY_NAME_ARRAY = new char[0];
	public static final String EMPTY_NAME = ""; //$NON-NLS-1$
	public static final char[] OPERATOR_ = new char[] {'o','p','e','r','a','t','o','r',' '};  //$NON-NLS-1$
	public static final IType VOID_TYPE = new CPPBasicType( IBasicType.t_void, 0 );
	
	static protected class LookupData
	{
		protected IASTName astName;
		public ObjectMap usingDirectives = ObjectMap.EMPTY_MAP; 
		public ObjectSet visited = ObjectSet.EMPTY_SET;	//used to ensure we don't visit things more than once
		public ObjectSet inheritanceChain;	//used to detect circular inheritance
		public ObjectSet associated = ObjectSet.EMPTY_SET;
		
		public boolean checkWholeClassScope = false;
		public boolean ignoreUsingDirectives = false;
		public boolean usingDirectivesOnly = false;
		public boolean forceQualified = false;
		public boolean forUserDefinedConversion = false;
		public boolean forAssociatedScopes = false;
		public boolean prefixLookup = false;
		public boolean typesOnly = false;
		public boolean considerConstructors = false;
		public Object foundItems = null;
		public Object [] functionParameters;
		public IASTNode [] templateArguments;
		public ProblemBinding problem;
        
		
		public LookupData( IASTName n ){
			astName = n;
			typesOnly = typesOnly();
			considerConstructors = considerConstructors();
			checkWholeClassScope = checkWholeClassScope();
		}
		public LookupData(){
			astName = null;
		}
		public final char [] name () {
			if( astName != null )
				return astName.toCharArray();
			return EMPTY_NAME_ARRAY;
		}
		public boolean includeBlockItem( IASTNode item ){
		    if( astName.getPropertyInParent() == STRING_LOOKUP_PROPERTY ) return true;
		    if( ( astName != null && astName.getParent() instanceof IASTIdExpression ) || 
		        item instanceof ICPPASTNamespaceDefinition  ||
	           (item instanceof IASTSimpleDeclaration && ((IASTSimpleDeclaration)item).getDeclSpecifier() instanceof IASTCompositeTypeSpecifier ) ||
			    item instanceof ICPPASTTemplateDeclaration )
		    {
		        return true;
		    }
		    return false;
		}
		private boolean typesOnly(){
			if( astName == null ) return false;
			if( astName.getPropertyInParent() == STRING_LOOKUP_PROPERTY ) return false;
			IASTNode parent = astName.getParent();
			if( parent instanceof ICPPASTBaseSpecifier || parent instanceof ICPPASTElaboratedTypeSpecifier || 
			    parent instanceof ICPPASTCompositeTypeSpecifier )
			    return true;
			if( parent instanceof ICPPASTQualifiedName ){
			    IASTName [] ns = ((ICPPASTQualifiedName)parent).getNames();
			    return ( astName != ns[ ns.length -1 ] );
			}
			return false;
		}
		public boolean forUsingDeclaration(){
			if( astName == null ) return false;
			if( astName.getPropertyInParent() == STRING_LOOKUP_PROPERTY ) return false;
			IASTNode p1 = astName.getParent();
			if( p1 instanceof ICPPASTUsingDeclaration )
			    return true;
			
			if( p1 instanceof ICPPASTQualifiedName ){
			    IASTNode p2 = p1.getParent();
			    if( p2 instanceof ICPPASTUsingDeclaration ){
			        IASTName [] ns = ((ICPPASTQualifiedName) p1 ).getNames();
			        return (ns[ ns.length - 1 ] == astName);
			    }
			}
			return false;
		}
		public boolean forDefinition(){
			if( astName == null ) return false;
			if( astName.getPropertyInParent() == STRING_LOOKUP_PROPERTY ) return false;
			
			IASTName n = astName;
			if( n.getParent() instanceof ICPPASTTemplateId )
			    n = (IASTName) n.getParent();
			IASTNode p1 = n.getParent();
			if( p1 instanceof ICPPASTQualifiedName ){
			    IASTName [] ns = ((ICPPASTQualifiedName)p1).getNames();
			    if( ns[ns.length - 1] != n )
			        return false;
			    p1 = p1.getParent();
			}			
			IASTNode p2 = p1.getParent();
			if( p1 instanceof IASTDeclarator && p2 instanceof IASTSimpleDeclaration ){
				return !( p2.getParent() instanceof ICPPASTExplicitTemplateInstantiation );
			}
			return ( p1 instanceof IASTDeclarator && p2 instanceof IASTFunctionDefinition);
		}
		
		public boolean forExplicitInstantiation(){
			if( astName == null ) return false;
			if( astName.getPropertyInParent() == STRING_LOOKUP_PROPERTY ) return false;
			
			IASTName n = astName;
			if( n.getParent() instanceof ICPPASTTemplateId )
			    n = (IASTName) n.getParent();
			IASTNode p1 = n.getParent();
			if( p1 instanceof ICPPASTQualifiedName ){
			    IASTName [] ns = ((ICPPASTQualifiedName)p1).getNames();
			    if( ns[ns.length - 1] != n )
			        return false;
			    p1 = p1.getParent();
			}			
			IASTNode p2 = p1.getParent();
			if( p1 instanceof IASTDeclarator && p2 instanceof IASTSimpleDeclaration ){
				return ( p2.getParent() instanceof ICPPASTExplicitTemplateInstantiation );
			}
			return false;
		}
		
		private boolean considerConstructors(){
			if( astName == null ) return false;
			if( astName.getPropertyInParent() == STRING_LOOKUP_PROPERTY ) return false;
			IASTNode p1 = astName.getParent();
			IASTNode p2 = p1.getParent();
			
			if( p1 instanceof ICPPASTConstructorChainInitializer )
				return true;
			if( p1 instanceof ICPPASTNamedTypeSpecifier && p2 instanceof IASTTypeId )
				return p2.getParent() instanceof ICPPASTNewExpression;
			else if( p1 instanceof ICPPASTQualifiedName ){
				if( p2 instanceof ICPPASTFunctionDeclarator ){
					IASTName[] names = ((ICPPASTQualifiedName)p1).getNames();
					if( names.length >= 2 && names[ names.length - 1 ] == astName )
					    return CPPVisitor.isConstructor( names[ names.length - 2 ], (IASTDeclarator) p2 );
				} else if( p2 instanceof ICPPASTNamedTypeSpecifier ){
					IASTNode p3 = p2.getParent();
					return p3 instanceof IASTTypeId && p3.getParent() instanceof ICPPASTNewExpression;
				} else if( p2 instanceof IASTIdExpression ){
					return p2.getParent() instanceof IASTFunctionCallExpression;
				}
			} else if( p1 instanceof IASTFunctionCallExpression || p2 instanceof IASTFunctionCallExpression )
				return true;
			return false;
		}
		public boolean qualified(){
		    if( forceQualified ) return true;
			if( astName == null ) return false;
			if( astName.getPropertyInParent() == STRING_LOOKUP_PROPERTY ) return false;
			IASTNode p1 = astName.getParent();
			if( p1 instanceof ICPPASTQualifiedName ){
				return ((ICPPASTQualifiedName)p1).getNames()[0] != astName;
			}
			return p1 instanceof ICPPASTFieldReference;
		}
		public boolean functionCall(){
		    if( astName == null ) return false;
		    if( astName.getPropertyInParent() == STRING_LOOKUP_PROPERTY ) return false;
		    IASTNode p1 = astName.getParent();
		    if( p1 instanceof ICPPASTQualifiedName )
		        p1 = p1.getParent();
		    return ( p1 instanceof IASTIdExpression && p1.getPropertyInParent() == IASTFunctionCallExpression.FUNCTION_NAME );
		}
        private boolean checkWholeClassScope() {
            if( astName == null ) return false;
            if( astName.getPropertyInParent() == STRING_LOOKUP_PROPERTY ) return true;
            
            IASTNode parent = astName.getParent();
            while( parent != null && !(parent instanceof IASTFunctionDefinition) ){
                parent = parent.getParent();
            }
            if( parent instanceof IASTFunctionDefinition ){
            	while( parent.getParent() instanceof ICPPASTTemplateDeclaration )
            		parent = parent.getParent();
                if( parent.getPropertyInParent() != IASTCompositeTypeSpecifier.MEMBER_DECLARATION )
                    return false;
                
                ASTNodeProperty prop = astName.getPropertyInParent();
                if( prop == ICPPASTQualifiedName.SEGMENT_NAME )
                    prop = astName.getParent().getPropertyInParent();
                if( prop == IASTIdExpression.ID_NAME || 
    				prop == IASTFieldReference.FIELD_NAME || 
    				prop == ICASTFieldDesignator.FIELD_NAME ||
    				prop == ICPPASTUsingDirective.QUALIFIED_NAME ||
    				prop == ICPPASTUsingDeclaration.NAME ||
    				prop == IASTFunctionCallExpression.FUNCTION_NAME ||
    				prop == IASTNamedTypeSpecifier.NAME ||
    				prop == ICPPASTConstructorChainInitializer.MEMBER_ID )
                {
                    return true;
                }
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
        /**
         * an IType[] of function arguments, inluding the implied object argument
         * @return
         */
        public IType getImpliedObjectArgument() {
            IType implied = null;
            
            if( astName != null ){
                IASTName tempName = astName;
                while( tempName.getParent() instanceof IASTName )
                    tempName = (IASTName) tempName.getParent();
                
                ASTNodeProperty prop = tempName.getPropertyInParent();
                if( prop == IASTFieldReference.FIELD_NAME || 
                	(prop == STRING_LOOKUP_PROPERTY && tempName.getParent() instanceof ICPPASTFieldReference) )
                {
                    ICPPASTFieldReference fieldRef = (ICPPASTFieldReference) tempName.getParent();
                    implied = CPPVisitor.getExpressionType( fieldRef.getFieldOwner() );
                    if( fieldRef.isPointerDereference() && implied instanceof IPointerType ){
                        try {
                            implied = ((IPointerType)implied).getType();
                        } catch ( DOMException e ) {
                            implied = e.getProblem();
                        }
                    }
                } else if( prop == IASTIdExpression.ID_NAME ){
                    IScope scope = CPPVisitor.getContainingScope( tempName );
                    if( scope instanceof ICPPClassScope ){
                        implied = ((ICPPClassScope)scope).getClassType();
                    } else {
	                    implied = CPPVisitor.getThisType( scope );
	                    if( implied instanceof IPointerType ){
	                        try {
	                            implied = ((IPointerType)implied).getType();
	                        } catch ( DOMException e ) {
	                            implied = e.getProblem();
	                        }
	                    }
                    }
                } else if( prop == STRING_LOOKUP_PROPERTY && tempName.getParent() instanceof IASTArraySubscriptExpression ){
            		IASTExpression exp = ((IASTArraySubscriptExpression)tempName.getParent()).getArrayExpression();
            		implied = CPPVisitor.getExpressionType( exp );
                }
            }
            return implied;
        }
		public boolean forFriendship() {
			if( astName == null )
				return false;
			IASTNode node = astName.getParent();
			while( node instanceof IASTName )
				node = node.getParent();
			
			IASTDeclaration decl = null;
			IASTDeclarator dtor = null;
			if( node instanceof ICPPASTDeclSpecifier && node.getParent() instanceof IASTDeclaration ){
				decl = (IASTDeclaration) node.getParent();
			} else if( node instanceof IASTDeclarator ) {
				dtor = (IASTDeclarator) node;
				while( dtor.getParent() instanceof IASTDeclarator )
					dtor = (IASTDeclarator) dtor.getParent();
				if( !(dtor.getParent() instanceof IASTDeclaration) )
					return false;
				decl = (IASTDeclaration) dtor.getParent();
			} else {
				return false;
			}
			if( decl instanceof IASTSimpleDeclaration ){
				IASTSimpleDeclaration simple = (IASTSimpleDeclaration) decl;
				if( ! ((ICPPASTDeclSpecifier)simple.getDeclSpecifier()).isFriend() )
					return false;
				if( dtor != null )
					return true;
				return simple.getDeclarators().length == 0;
			} else if( decl instanceof IASTFunctionDefinition ){
				IASTFunctionDefinition fnDef = (IASTFunctionDefinition) decl;
				if( ! ((ICPPASTDeclSpecifier)fnDef.getDeclSpecifier()).isFriend() )
					return false;
				return ( dtor != null );
			}
			return false;
		}
		
		public boolean checkAssociatedScopes() {
			if( astName == null || astName instanceof ICPPASTQualifiedName )
				return false;
			IASTNode parent = astName.getParent();
			if( parent instanceof ICPPASTQualifiedName ){
				IASTName [] ns = ((ICPPASTQualifiedName)parent).getNames();
				if( ns[ ns.length - 1] != astName )
					return false;
			}
			return functionCall() && (associated.size() > 0);
		}
		public boolean checkClassContainingFriend() {
			if( astName == null || astName instanceof ICPPASTQualifiedName )
				return false;
			
			IASTNode p = astName.getParent();
			ASTNodeProperty prop = null;
			while( p != null ){
				prop = p.getPropertyInParent();
				if( prop == ICPPASTTemplateId.TEMPLATE_ID_ARGUMENT || prop == IASTDeclarator.DECLARATOR_NAME )
					return false;
				if( p instanceof IASTDeclarator && !(((IASTDeclarator)p).getName() instanceof ICPPASTQualifiedName) )
					return false;
				if( p instanceof IASTDeclaration ){
					if( prop == IASTCompositeTypeSpecifier.MEMBER_DECLARATION ){
						if( p instanceof IASTSimpleDeclaration ){
							ICPPASTDeclSpecifier declSpec = (ICPPASTDeclSpecifier) ((IASTSimpleDeclaration)p).getDeclSpecifier();
							return declSpec.isFriend();
						} else if( p instanceof IASTFunctionDefinition ){
							ICPPASTDeclSpecifier declSpec = (ICPPASTDeclSpecifier) ((IASTFunctionDefinition)p).getDeclSpecifier();
							return declSpec.isFriend();
						}
					} else {
						return false;
					}
				}
				p = p.getParent();
			}
			return false;
		}
		public boolean preferTemplateFunctions() {
			if( astName == null )
				return false;
			return (astName instanceof ICPPASTTemplateId || astName.getPropertyInParent() == ICPPASTTemplateId.TEMPLATE_NAME );
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
		public static final int FUZZY_TEMPLATE_PARAMETERS = 6;

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
        if( data.checkAssociatedScopes() ){
            //3.4.2 argument dependent name lookup, aka Koenig lookup
            try {
                IScope scope = (binding != null ) ? binding.getScope() : null;
                if( scope == null || !(scope instanceof ICPPClassScope) ){
                    data.ignoreUsingDirectives = true;
                    data.forceQualified = true;
                    for( int i = 0; i < data.associated.size(); i++ ){
                    	lookup( data, data.associated.keyAt(i) );
                    }
                    binding = resolveAmbiguities( data, data.astName );
                }
            } catch ( DOMException e ) {
                binding = e.getProblem();
            }
        }
        if( binding == null && data.checkClassContainingFriend() ){
        	//3.4.1-10 if we don't find a name used in a friend declaration in the member declaration's class
        	//we should look in the class granting friendship
        	IASTNode parent = data.astName.getParent();
        	while( parent != null && !(parent instanceof ICPPASTCompositeTypeSpecifier) )
        		parent = parent.getParent();
        	if( parent instanceof ICPPASTCompositeTypeSpecifier ){
        		IScope scope = ((ICPPASTCompositeTypeSpecifier)parent).getScope();
        		try {
		    		lookup( data, scope );
		    		binding = resolveAmbiguities( data, data.astName );
        		} catch( DOMException e ){
        			binding = e.getProblem();
        		}
        	}
        }
		if( binding instanceof ICPPClassTemplate ){
			ASTNodeProperty prop = data.astName.getPropertyInParent();
			if( prop != ICPPASTQualifiedName.SEGMENT_NAME && prop != ICPPASTTemplateId.TEMPLATE_NAME ){
				try {
					IASTNode def = ((ICPPInternalBinding)binding).getDefinition();
					if( def != null ){
						def = def.getParent();
						IASTNode parent = data.astName.getParent();
						while( parent != null ){
							if( parent == def ){
								binding = CPPTemplates.instantiateWithinClassTemplate( (ICPPClassTemplate) binding );
								break;
							}
							if( parent instanceof ICPPASTNamespaceDefinition )
								break;
							parent = parent.getParent();
						}
					}
				} catch( DOMException e ) {
				}
			}
		}
        if( binding instanceof ICPPClassType && data.considerConstructors ){
        	ICPPClassType cls = (ICPPClassType) binding;
        	if( data.astName instanceof ICPPASTTemplateId && cls instanceof ICPPInternalTemplate ){
        		ICPPASTTemplateId id = (ICPPASTTemplateId) data.astName;
        		IType [] args = CPPTemplates.createTypeArray( id.getTemplateArguments() );
        		cls = (ICPPClassType) ((ICPPInternalTemplate)cls).instantiate( args );
        	}
		    if( cls != null ){
			    try {
	                //force resolution of constructor bindings
	                IBinding [] ctors = cls.getConstructors();
	                if( ctors.length > 0 && !(ctors[0] instanceof IProblemBinding) ){
		                //then use the class scope to resolve which one.
		    		    binding = ((ICPPClassScope)cls.getCompositeScope()).getBinding( data.astName, true );
	                }
	            } catch ( DOMException e ) {
	                binding = e.getProblem();
	            }
		    }
		    
		}
		IASTName name = data.astName;
		if( name.getParent() instanceof ICPPASTTemplateId ){
			if( binding instanceof ICPPTemplateInstance ){
				IBinding b = binding;
				binding = ((ICPPTemplateInstance)binding).getSpecializedBinding();
				name.setBinding( binding );
				name = (IASTName) name.getParent();
				name.setBinding( b );
			} else {
				name = (IASTName) name.getParent();
			}
		}
		if( name.getParent() instanceof ICPPASTQualifiedName ){
			IASTName [] ns = ((ICPPASTQualifiedName)name.getParent()).getNames();
			if( name == ns [ ns.length - 1] )
				name = (IASTName) name.getParent();
		}
		
        if( binding != null ) {
	        if( name.getPropertyInParent() == IASTNamedTypeSpecifier.NAME && !( binding instanceof IType || binding instanceof ICPPConstructor) ){
	        	IASTNode parent = name.getParent().getParent();
	        	if( parent instanceof IASTTypeId && parent.getPropertyInParent() == ICPPASTTemplateId.TEMPLATE_ID_ARGUMENT ){
	        		//don't do a problem here
	        	} else {
	        		binding = new ProblemBinding( data.astName, IProblemBinding.SEMANTIC_INVALID_TYPE, data.name() );
	        	}
	        }
        }
        
		if( binding != null && !( binding instanceof IProblemBinding ) ){
		    if( data.forDefinition() ){
		        addDefinition( binding, data.astName );
		    } 
		}
		if( binding == null ){
			// Let's try the pdom
			IPDOM pdom = name.getTranslationUnit().getIndex();
			if (pdom != null)
				binding = pdom.resolveBinding(name);

			// If we're still null...
			if (binding == null) {
			    if( name instanceof ICPPASTQualifiedName && data.forDefinition() )
			        binding = new ProblemBinding( data.astName, IProblemBinding.SEMANTIC_MEMBER_DECLARATION_NOT_FOUND, data.name() );
			    else
			        binding = new ProblemBinding( data.astName, IProblemBinding.SEMANTIC_NAME_NOT_FOUND, data.name() );
			}
		}
        return binding;
    }

    static private CPPSemantics.LookupData createLookupData( IASTName name, boolean considerAssociatedScopes ){
		CPPSemantics.LookupData data = new CPPSemantics.LookupData( name );
		IASTNode parent = name.getParent();
		
		if( name instanceof ICPPASTTemplateId ){
			data.templateArguments = ((ICPPASTTemplateId)name).getTemplateArguments();
		}
		
		if( parent instanceof ICPPASTTemplateId )
			parent = parent.getParent();
		if( parent instanceof ICPPASTQualifiedName )
			parent = parent.getParent();
		
		if( parent instanceof IASTDeclarator && parent.getPropertyInParent() == IASTSimpleDeclaration.DECLARATOR ){
		    IASTSimpleDeclaration simple = (IASTSimpleDeclaration) parent.getParent();
		    if( simple.getDeclSpecifier().getStorageClass() == IASTDeclSpecifier.sc_typedef )
		        data.forceQualified = true;
		}
		
		if( parent instanceof ICPPASTFunctionDeclarator ){
			data.functionParameters = ((ICPPASTFunctionDeclarator)parent).getParameters();
		} else if( parent instanceof IASTIdExpression ){
		    ASTNodeProperty prop = parent.getPropertyInParent();
		    if( prop == IASTFunctionCallExpression.FUNCTION_NAME ){
		        parent = parent.getParent();
				IASTExpression exp = ((IASTFunctionCallExpression)parent).getParameterExpression();
				if( exp instanceof IASTExpressionList )
					data.functionParameters = ((IASTExpressionList) exp ).getExpressions();
				else if( exp != null )
					data.functionParameters = new IASTExpression [] { exp };
				else
					data.functionParameters = IASTExpression.EMPTY_EXPRESSION_ARRAY;
			}
		} else if( parent instanceof ICPPASTFieldReference && parent.getPropertyInParent() == IASTFunctionCallExpression.FUNCTION_NAME ){
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
		} else if( parent instanceof ICPPASTConstructorChainInitializer ){
			ICPPASTConstructorChainInitializer ctorinit = (ICPPASTConstructorChainInitializer) parent;
			IASTExpression val = ctorinit.getInitializerValue();
			if( val instanceof IASTExpressionList )
				data.functionParameters = ((IASTExpressionList) val ).getExpressions();
			else if( val != null )
				data.functionParameters = new IASTExpression [] { val };
			else
				data.functionParameters = IASTExpression.EMPTY_EXPRESSION_ARRAY;
		}
		
		if( considerAssociatedScopes && !(name.getParent() instanceof ICPPASTQualifiedName) && data.functionCall() ){
		    data.associated = getAssociatedScopes( data );
		}
		
		return data;
	}
    
    static private ObjectSet getAssociatedScopes( LookupData data ) {
        IType [] ps = getSourceParameterTypes( data.functionParameters );
        ObjectSet namespaces = new ObjectSet(2);
        ObjectSet classes = new ObjectSet(2);
        for( int i = 0; i < ps.length; i++ ){
            IType p = ps[i];
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
				IScope scope = getContainingNamespaceScope( (IBinding) t );
				if( scope != null )
					namespaces.put( scope );

			    ICPPClassType cls = (ICPPClassType) t;
			    ICPPBase[] bases = cls.getBases();
			    for( int i = 0; i < bases.length; i++ ){
			        if( bases[i] instanceof IProblemBinding )
			            continue;
			        IBinding b = bases[i].getBaseClass();
			        if( b instanceof IType )
			        	getAssociatedScopes( (IType) b, namespaces, classes );
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
	    IScope scope = null;
    	if( parent instanceof ICPPASTBaseSpecifier ) {
    	    ICPPASTCompositeTypeSpecifier compSpec = (ICPPASTCompositeTypeSpecifier) parent.getParent();
    	    IASTName n = compSpec.getName();
    	    if( n instanceof ICPPASTQualifiedName ){
    	        IASTName [] ns = ((ICPPASTQualifiedName)n).getNames();
    	        n = ns[ ns.length - 1 ];
    	    }
    	    
	        scope = CPPVisitor.getContainingScope( n );
	    } else if( parent instanceof ICPPASTConstructorChainInitializer ){
	    	ICPPASTConstructorChainInitializer initializer = (ICPPASTConstructorChainInitializer) parent;
	    	IASTFunctionDeclarator dtor = (IASTFunctionDeclarator) initializer.getParent();
	    	IBinding binding = dtor.getName().resolveBinding();
	    	if( !(binding instanceof IProblemBinding) )
	    		scope = binding.getScope();
	    } else {
	    	scope = CPPVisitor.getContainingScope( name );
	    }
    	if( scope instanceof ICPPScope )
    		return (ICPPScope)scope;
    	else if( scope instanceof IProblemBinding )
    		return new CPPScope.CPPScopeProblem( ((IProblemBinding)scope).getASTNode(), IProblemBinding.SEMANTIC_BAD_SCOPE, ((IProblemBinding)scope).getNameCharArray() );
    	return new CPPScope.CPPScopeProblem( name, IProblemBinding.SEMANTIC_BAD_SCOPE, name.toCharArray() );
	}
	private static void mergeResults( LookupData data, Object results, boolean scoped ){
	    if( !data.prefixLookup ){
	        if( results instanceof IBinding ){
	            data.foundItems = ArrayUtil.append( Object.class, (Object[]) data.foundItems, results );
	        } else if( results instanceof Object[] ){
	            data.foundItems = ArrayUtil.addAll( Object.class, (Object[])data.foundItems, (Object[])results );
	        }
	    } else {
	        data.foundItems = mergePrefixResults( (CharArrayObjectMap) data.foundItems, results, scoped );
	    }
	}
	
	/**
	 * @param dest
	 * @param source : either Object[] or CharArrayObjectMap
	 * @param scoped
	 * @return
	 */
	private static Object mergePrefixResults( CharArrayObjectMap dest, Object source, boolean scoped ){
		if( source == null ) return dest; 
        CharArrayObjectMap resultMap = ( dest != null ) ? dest : new CharArrayObjectMap(2);
        
        CharArrayObjectMap map = null;
        Object [] objs = null;
        if( source instanceof CharArrayObjectMap )
        	map = (CharArrayObjectMap) source;
        else{
			if (source instanceof Object[])
				objs = ArrayUtil.trim( Object.class, (Object[]) source );
			else 
				objs = new Object[]{ source };
		} 
        
        int size = map != null ? map.size() : objs.length;
		int resultInitialSize = resultMap.size();
        for( int i = 0; i < size; i ++ ) {
        	char [] key = ( map != null ) ? map.keyAt(i) 
        								  : ( objs[i] instanceof IBinding) ? ((IBinding)objs[i]).getNameCharArray() 
        								  								   : ((IASTName)objs[i]).toCharArray();
        	int idx = resultMap.lookup( key );
        	if( idx == -1 ){
				resultMap.put( key, (map != null ) ? map.get( key ) : objs[i] );
			} else if( !scoped || idx >= resultInitialSize ) {
			    Object obj = resultMap.get( key );
			    Object so = ( map != null ) ? map.get(key) : objs[i];
			    if( obj instanceof Object [] ) {
			        if( so instanceof IBinding || so instanceof IASTName )
			            obj = ArrayUtil.append( Object.class, (Object[]) obj, so );
			        else
			            obj = ArrayUtil.addAll( Object.class, (Object[])obj, (Object[]) so );
			    } else {
			        if( so instanceof IBinding || so instanceof IASTName )
			            obj = new Object [] { obj, so };
			        else {
			            Object [] temp = new Object [ ((Object[])so).length + 1 ];
			            temp[0] = obj;
			            obj = ArrayUtil.addAll( Object.class, temp, (Object[]) so );
			        }
			    } 
				resultMap.put( key, obj );
			}
        }

        return resultMap;
	}
	static protected void lookup( CPPSemantics.LookupData data, Object start ) throws DOMException{
		IASTNode node = data.astName;

		ICPPScope scope = null;
		if( start instanceof ICPPScope )
		    scope = (ICPPScope) start;
		else if( start instanceof IASTName )
		    scope = getLookupScope( (IASTName) start );
		else 
			return;
		
		boolean friendInLocalClass = false;
		if( scope instanceof ICPPClassScope && data.forFriendship() ){
			try {
				ICPPClassType cls = ((ICPPClassScope)scope).getClassType();
				friendInLocalClass = !cls.isGloballyQualified();
			} catch ( DOMException e ){
			}
		}
		
		while( scope != null ){
			IASTNode blockItem = CPPVisitor.getContainingBlockItem( node );
			
			ArrayWrapper directives = null;
			if( !data.usingDirectivesOnly ){
				if( scope.isFullyCached() && !data.prefixLookup && data.astName != null ){
					IBinding binding = data.prefixLookup ? null : scope.getBinding( data.astName, true );
					if( binding != null && 
						( CPPSemantics.declaredBefore( binding, data.astName ) || 
						  (scope instanceof ICPPClassScope && data.checkWholeClassScope) ) )
					{
						mergeResults( data, binding, true );	
					}
				} else {
					if (!data.prefixLookup && data.astName != null ) {
						IBinding b = scope.getBinding( data.astName, false );
						if (b instanceof CPPImplicitFunction || b instanceof CPPImplicitTypedef)
							mergeResults( data, b, true );
					}
				    mergeResults( data, lookupInScope( data, scope, blockItem ), true );
				}

				if( (!data.hasResults() || data.prefixLookup) && scope instanceof ICPPNamespaceScope ){
					directives = new ArrayWrapper();
					directives.array = ((ICPPNamespaceScope) scope).getUsingDirectives();
					if( directives.array != null ){
						for( int i = 0; i < directives.array.length; i++ ){
							if( !CPPSemantics.declaredBefore( directives.array[i], blockItem ) ){
								directives.array[i] = null;
								directives.array = ArrayUtil.trim( IASTNode.class, directives.array );
							}
						}
					}
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
			
			if( (!data.prefixLookup && (data.problem != null || data.hasResults())) ||
				( friendInLocalClass && !(scope instanceof ICPPClassScope) ) )
			{
				return;
			}
			
			if( !data.usingDirectivesOnly && scope instanceof ICPPClassScope ){
				mergeResults( data, lookupInParents( data, scope ), true );
			}
			
			if( !data.prefixLookup && (data.problem != null || data.hasResults()) )
				return;
			
			//if still not found, loop and check our containing scope
			if( data.qualified() && !(scope instanceof ICPPTemplateScope) ) {
				if( !data.usingDirectives.isEmpty() )
					data.usingDirectivesOnly = true;
				else
					break;
			}
			
			if( blockItem != null )
				node = blockItem;
			
			ICPPScope parentScope = (ICPPScope) scope.getParent();
			if( parentScope instanceof ICPPTemplateScope ){
			    IASTNode parent = node.getParent();
			    while( parent != null && !(parent instanceof ICPPASTTemplateDeclaration) ){
			        node = parent;
			        parent = parent.getParent();
			    }
			    if( parent != null ){
			        ICPPASTTemplateDeclaration templateDecl = (ICPPASTTemplateDeclaration) parent;
			        ICPPTemplateScope templateScope = templateDecl.getScope();
			        if( templateScope.getTemplateDefinition() == ((ICPPTemplateScope)parentScope).getTemplateDefinition() ){
			            parentScope = templateScope;
			        }
			    }
			}
			scope = parentScope;
		}
	}

	private static Object lookupInParents( CPPSemantics.LookupData data, ICPPScope lookIn ) throws DOMException{
		ICPPBase [] bases = null;
		if( lookIn instanceof ICPPClassScope ){
			ICPPClassType c  = ((ICPPClassScope)lookIn).getClassType();
			if (c != null)
				bases = c.getBases();
		}
	
		Object inherited = null;
		Object result = null;
		
		if( bases == null || bases.length == 0 )
			return null;
				
		//use data to detect circular inheritance
		if( data.inheritanceChain == null )
			data.inheritanceChain = new ObjectSet( 2 );
		
		data.inheritanceChain.put( lookIn );
			
		int size = bases.length;
		for( int i = 0; i < size; i++ )
		{
			inherited = null;
			ICPPClassType cls = null;
			IBinding b = bases[i].getBaseClass();
			if( b instanceof ICPPClassType )
				cls = (ICPPClassType) b;
			else 
				continue;
			ICPPScope parent = (ICPPScope) cls.getCompositeScope();
			
			if( parent == null || parent instanceof CPPUnknownScope )
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
					if( data.astName != null && !data.prefixLookup && parent.isFullyCached() )
						inherited = parent.getBinding( data.astName, true );
					else 
						inherited = lookupInScope( data, parent, null );
					
					if( inherited == null || data.prefixLookup ){
						Object temp = lookupInParents( data, parent );
						if( inherited != null ){
							inherited = mergePrefixResults( null, inherited, true );
							inherited = mergePrefixResults( (CharArrayObjectMap)inherited, (CharArrayObjectMap)temp, true );
						} else {
							inherited = temp;
						}
					} else {
					    visitVirtualBaseClasses( data, cls );
					}
				} else {
				    data.problem = new ProblemBinding( null, IProblemBinding.SEMANTIC_CIRCULAR_INHERITANCE, cls.getNameCharArray() );
				    return null;
				}
			}	
			
			if( inherited != null  ){
				if( result == null ){
					result = inherited;
				} else if ( inherited != null ) {
					if( !data.prefixLookup ) {
						if( result instanceof Object [] ){
							Object [] r = (Object[]) result;
							for( int j = 0; j < r.length && r[j] != null; j++ ) {
								if( checkForAmbiguity( data, r[j], inherited ) ){
								    data.problem = new ProblemBinding( data.astName, IProblemBinding.SEMANTIC_AMBIGUOUS_LOOKUP, data.name() ); 
								    return null;
								}
							}
						} else {
							if( checkForAmbiguity( data, result, inherited ) ){
							    data.problem = new ProblemBinding( data.astName, IProblemBinding.SEMANTIC_AMBIGUOUS_LOOKUP, data.name() ); 
							    return null;
							}
						}
					} else {
						CharArrayObjectMap temp = (CharArrayObjectMap) inherited;
						CharArrayObjectMap r = (CharArrayObjectMap) result;
						char[] key = null;
						int tempSize = temp.size();
						for( int ii = 0; ii < tempSize; ii++ ){
						    key = temp.keyAt( ii );
							if( !r.containsKey( key ) ){
								r.put( key, temp.get(key) );
							} else {
								//TODO: prefixLookup ambiguity checking
							}
						}
					}
				}
			}
		}
	
		data.inheritanceChain.remove( lookIn );
	
		return result;	
	}

	public static void visitVirtualBaseClasses( LookupData data, ICPPClassType cls ){
	    ICPPBase [] bases = null;
	    try {
            bases = cls.getBases();
        } catch ( DOMException e ) {
            return;
        }
        for( int i = 0; i < bases.length; i++ ){
            try {
                if( bases[i].isVirtual() ){
                    if( data.visited == ObjectSet.EMPTY_SET )
				        data.visited = new ObjectSet(2);
                    IBinding b = bases[i].getBaseClass();
                    if( b instanceof ICPPClassType )
                    	data.visited.put( ((ICPPClassType)b).getCompositeScope() );
                } else {
                	IBinding b = bases[i].getBaseClass();
                    if( b instanceof ICPPClassType )
                    	visitVirtualBaseClasses( data, (ICPPClassType) b );
                }
            } catch ( DOMException e1 ) {
            }
        }
	}
	private static boolean checkForAmbiguity( LookupData data, Object n, Object names ) throws DOMException{
		if( names instanceof Object[] ) {
		    names = ArrayUtil.trim( Object.class, (Object[]) names );
		    if( ((Object[])names).length == 0 )
		        return false;
		}

	    IBinding binding =  ( n instanceof IBinding) ? (IBinding)n : ((IASTName)n).resolveBinding();
	    while( binding instanceof ICPPDelegate ){
	    	binding = ((ICPPDelegate)binding).getBinding();
	    }
	    Object [] objs = ( names instanceof Object[] ) ? (Object[])names : null;
	    int idx = ( objs != null && objs.length > 0 ) ? 0 : -1;
	    Object o = ( idx != -1 ) ? objs[idx++] : names;
	    while( o != null ) {       
	        IBinding b = ( o instanceof IBinding ) ? (IBinding) o : ((IASTName)o).resolveBinding();
	        
	        if( b instanceof ICPPUsingDeclaration ){
	        	objs = ArrayUtil.append( Object.class, objs, ((ICPPUsingDeclaration)b).getDelegates() );
	        } else {
	        	while( b instanceof ICPPDelegate ){
	    	    	b = ((ICPPDelegate)b).getBinding();
	    	    }
		        if( binding != b )
		            return true;
				
				boolean ok = false;
				//3.4.5-4 if the id-expression  in a class member access is a qualified id... the result 
				//is not required to be a unique base class...
				if( binding instanceof ICPPClassType ){
					IASTNode parent = data.astName.getParent();
					if( parent instanceof ICPPASTQualifiedName && 
						parent.getPropertyInParent() == IASTFieldReference.FIELD_NAME )
					{
						ok = true;
					}
				}
			    //it is not ambiguous if they are the same thing and it is static or an enumerator
		        if( binding instanceof IEnumerator ||
		           (binding instanceof IFunction && ((ICPPInternalFunction)binding).isStatic( false )) ||
			       (binding instanceof IVariable && ((IVariable)binding).isStatic()) ) 
		        {
		        	ok = true;
		        }
		        if( !ok )
					return true;
	        }
	        if( idx > -1 && idx < objs.length )
	        	o = objs[idx++];
	        else
	        	o = null;
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
	static protected IASTName[] lookupInScope( CPPSemantics.LookupData data, ICPPScope scope, IASTNode blockItem ) throws DOMException {
		Object possible = null;
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
			IASTNode p = parent.getParent();
		    if( p instanceof IASTFunctionDefinition ){
		        ICPPASTFunctionDeclarator dtor = (ICPPASTFunctionDeclarator) ((IASTFunctionDefinition)p).getDeclarator();
		        nodes = dtor.getParameters();
		    } 
		    if( p instanceof ICPPASTCatchHandler ){
		    	parent = p;
		    } else if( nodes == null || nodes.length == 0 ){
				IASTCompoundStatement compound = (IASTCompoundStatement) parent;
				nodes = compound.getStatements();
		    }
		} else if ( parent instanceof IASTTranslationUnit ){
			IASTTranslationUnit translation = (IASTTranslationUnit) parent;
			nodes = translation.getDeclarations();
		} else if ( parent instanceof ICPPASTCompositeTypeSpecifier ){
			ICPPASTCompositeTypeSpecifier comp = (ICPPASTCompositeTypeSpecifier) parent;
			nodes = comp.getMembers();
			
			//9-2 a class name is also inserted into the scope of the class itself
			IASTName n = comp.getName();
			if( nameMatches( data, n ) ) {
				found = (IASTName[]) ArrayUtil.append( IASTName.class, found, n );
		    }
		} else if ( parent instanceof ICPPASTNamespaceDefinition ){
		    //need binding because namespaces can be split
		    CPPNamespace namespace = (CPPNamespace) ((ICPPASTNamespaceDefinition)parent).getName().resolveBinding();
		    namespaceDefs = namespace.getNamespaceDefinitions();
		    
			nodes = ((ICPPASTNamespaceDefinition)namespaceDefs[0].getParent()).getDeclarations();
			namespaceIdx = 0;
		} else if( parent instanceof ICPPASTFunctionDeclarator ){
		    ICPPASTFunctionDeclarator dtor = (ICPPASTFunctionDeclarator) parent;
	        nodes = dtor.getParameters();
		} else if( parent instanceof ICPPASTTemplateDeclaration ){
			ICPPASTTemplateDeclaration template = (ICPPASTTemplateDeclaration) parent;
			nodes = template.getTemplateParameters();
		}
		
		int idx = -1;
		boolean checkWholeClassScope = ( scope instanceof ICPPClassScope ) && data.checkWholeClassScope;
		IASTNode item = ( nodes != null ? (nodes.length > 0 ? nodes[++idx] : null ) : parent );
		IASTNode [][] nodeStack = null;
		int [] nodeIdxStack = null;
		int nodeStackPos = -1;
		while( item != null ) {
		    if( item instanceof ICPPASTLinkageSpecification ){
		        IASTDeclaration [] decls = ((ICPPASTLinkageSpecification)item).getDeclarations();
		        if( decls != null && decls.length > 0 ){
			        nodeStack = (IASTNode[][]) ArrayUtil.append( IASTNode[].class, nodeStack, nodes );
			        nodeIdxStack = ArrayUtil.setInt( nodeIdxStack, ++nodeStackPos, idx );
			        nodes = ((ICPPASTLinkageSpecification)item).getDeclarations();
			        idx = 0;
				    item = nodes[idx];
		        }
			}

		    if( item instanceof IASTDeclarationStatement )
		        item = ((IASTDeclarationStatement)item).getDeclaration();
		    if( item instanceof ICPPASTUsingDirective  ||
				  (item instanceof ICPPASTNamespaceDefinition &&
				   ((ICPPASTNamespaceDefinition)item).getName().toCharArray().length == 0) ) 
			{
				if( scope instanceof ICPPNamespaceScope )
					((ICPPNamespaceScope)scope).addUsingDirective( item );
			} else {
			    //possible is IASTName or IASTName[]
				possible = collectResult( data, scope, item, (item == parent)  );
				if( possible != null ) {
				    int jdx = -1;
				    IASTName temp;
				    if( possible instanceof IASTName )
				        temp = (IASTName) possible;
				    else
				        temp = ((IASTName[])possible)[++jdx];
				    while( temp != null ) {
					
						if(	(checkWholeClassScope || declaredBefore( temp, data.astName )) &&
						    (item != blockItem || data.includeBlockItem( item )) )
							
						{
							if( data.considerConstructors || 
								!( temp.getParent() instanceof IASTDeclarator &&
								   CPPVisitor.isConstructor( scope, (IASTDeclarator) temp.getParent() ) ) )
							{
								found = (IASTName[]) ArrayUtil.append( IASTName.class, found, temp );
							}
						}
						if( ++jdx > 0 && jdx < ((IASTName[])possible).length )
						    temp = ((IASTName[])possible)[jdx];
						else 
						    temp = null;
				    }
				}
			}
		    
			if( idx > -1 && ++idx < nodes.length ){
				item = nodes[idx];
			} else {
			    item = null;
			    nullItem: while( item == null ){
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
				    } else if( parent instanceof ICPPASTCatchHandler ){
				    	parent = ((ICPPASTCatchHandler)parent).getCatchBody();
				    	if( parent instanceof IASTCompoundStatement ){
				    		nodes = ((IASTCompoundStatement)parent).getStatements();
				    	}
				    	if( nodes.length > 0 ){
					        idx = 0;
					        item = nodes[0];
					        break;
					    }  
				    }
				    if( item == null && nodeStackPos >= 0 ){
				        nodes = nodeStack[nodeStackPos];
				        nodeStack[nodeStackPos] = null;
				        idx = nodeIdxStack[nodeStackPos--];
				        if( ++idx >= nodes.length )
				            continue;
				        
			            item = nodes[idx];
				    }
				    break;
			    }
			}
		}
		

		scope.setFullyCached( true );
		
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
				
				boolean found = false;
				if( temp.isFullyCached() && !data.prefixLookup ){
					IBinding binding = temp.getBinding( data.astName, true );
					if( binding != null && 
						( CPPSemantics.declaredBefore( binding, data.astName ) || 
						  (scope instanceof ICPPClassScope && data.checkWholeClassScope) ) )
					{
						mergeResults( data, binding, true );
						found = true;
					}
				} else {
					IASTName [] f = lookupInScope( data, temp, null );
					if( f != null ) {
						mergeResults( data, f, true );
						found = true;
					}
				}

				if( !found && temp instanceof ICPPNamespaceScope ){
					usings.array = ((ICPPNamespaceScope) temp).getUsingDirectives();
				}
							
				//only consider the transitive using directives if we are an unqualified
				//lookup, or we didn't find the name in decl
				if( usings.array != null && usings.array.length > 0 && (!data.qualified() || !found ) ){
				    transitives = ArrayUtil.addAll( Object.class, transitives, usings.array );
				}
			}
		}
		return transitives;
	}

	static private Object collectResult( CPPSemantics.LookupData data, ICPPScope scope, IASTNode node, boolean checkAux ) throws DOMException{
	    IASTName resultName = null;
	    IASTName [] resultArray = null;
	    
	    IASTDeclaration declaration = null;
	    if( node instanceof ICPPASTTemplateDeclaration )
			declaration = ((ICPPASTTemplateDeclaration)node).getDeclaration();
	    else if( node instanceof IASTDeclaration ) 
	        declaration = (IASTDeclaration) node;
		else if( node instanceof IASTDeclarationStatement )
			declaration = ((IASTDeclarationStatement)node).getDeclaration();
		else if( node instanceof ICPPASTCatchHandler )
			declaration = ((ICPPASTCatchHandler)node).getDeclaration();
		else if( node instanceof ICPPASTForStatement && checkAux )
        {
			ICPPASTForStatement forStatement = (ICPPASTForStatement) node;
			if( forStatement.getConditionDeclaration() == null ){
				if( forStatement.getInitializerStatement() instanceof IASTDeclarationStatement )
					declaration = ((IASTDeclarationStatement)forStatement.getInitializerStatement()).getDeclaration();
			} else {
				if( forStatement.getInitializerStatement() instanceof IASTDeclarationStatement ){
					Object o = collectResult( data, scope, forStatement.getInitializerStatement(), checkAux );
					if( o instanceof IASTName )
						resultName = (IASTName) o;
					else if( o instanceof IASTName[] )
						resultArray = (IASTName[]) o;
				}
				declaration = forStatement.getConditionDeclaration();
			}
        } else if( node instanceof ICPPASTSwitchStatement ){
        	declaration = ((ICPPASTSwitchStatement)node).getControllerDeclaration();
        } else if( node instanceof ICPPASTIfStatement ) {
        	declaration = ((ICPPASTIfStatement)node).getConditionDeclaration();
	    } else if( node instanceof ICPPASTWhileStatement ){
	    	declaration = ((ICPPASTWhileStatement)node).getConditionDeclaration();
	    } else if( node instanceof IASTParameterDeclaration ){
		    IASTParameterDeclaration parameterDeclaration = (IASTParameterDeclaration) node;
		    IASTDeclarator dtor = parameterDeclaration.getDeclarator();
            if (dtor != null) { // could be null when content assist in the declSpec
    		    while( dtor.getNestedDeclarator() != null )
    		    	dtor = dtor.getNestedDeclarator();
    			IASTName declName = dtor.getName();
    			scope.addName( declName );
    			if( !data.typesOnly && nameMatches( data, declName ) ) {
    			    return declName;
    		    }
            }
		} else if( node instanceof ICPPASTTemplateParameter ){
			IASTName name = CPPTemplates.getTemplateParameterName( (ICPPASTTemplateParameter) node );
			scope.addName( name );
			if( nameMatches( data, name ) ) {
		        return name;
		    }
		}
		if( declaration == null )
			return null;
		
		if( declaration instanceof IASTSimpleDeclaration ){
			IASTSimpleDeclaration simpleDeclaration = (IASTSimpleDeclaration) declaration;
			ICPPASTDeclSpecifier declSpec = (ICPPASTDeclSpecifier) simpleDeclaration.getDeclSpecifier();
			IASTDeclarator [] declarators = simpleDeclaration.getDeclarators();
			if( !declSpec.isFriend() ) {
				for( int i = 0; i < declarators.length; i++ ){
					IASTDeclarator declarator = declarators[i];
					while( declarator.getNestedDeclarator() != null )
						declarator = declarator.getNestedDeclarator();
					IASTName declaratorName = declarator.getName();
					scope.addName( declaratorName );
					if( !data.typesOnly || simpleDeclaration.getDeclSpecifier().getStorageClass() == IASTDeclSpecifier.sc_typedef ) {
						if( nameMatches( data, declaratorName ) ) {
							if( resultName == null )
							    resultName = declaratorName;
							else if( resultArray == null )
							    resultArray = new IASTName[] { resultName, declaratorName };
							else
							    resultArray = (IASTName[]) ArrayUtil.append( IASTName.class, resultArray, declaratorName );
						}
					}
				}
			}
	
			//decl spec 
			
			IASTName specName = null;
			if( declarators.length == 0 && declSpec instanceof IASTElaboratedTypeSpecifier ){
				specName = ((IASTElaboratedTypeSpecifier)declSpec).getName();
			} else if( declSpec instanceof ICPPASTCompositeTypeSpecifier ){
			    ICPPASTCompositeTypeSpecifier compSpec = (ICPPASTCompositeTypeSpecifier) declSpec;
				specName = compSpec.getName();
				
				//anonymous union?             //GCC supports anonymous structs too
				if( declarators.length == 0 && /*compSpec.getKey() == IASTCompositeTypeSpecifier.k_union &&*/
				    specName.toCharArray().length == 0 )
				{
				    Object o = null;
				    IASTDeclaration [] decls = compSpec.getMembers();
				    for ( int i = 0; i < decls.length; i++ ) {
                        o = collectResult( data, scope, decls[i], checkAux );
                        if( o instanceof IASTName ){
                            if( resultName == null )
    						    resultName = (IASTName) o;
    						else if( resultArray == null )
    						    resultArray = new IASTName[] { resultName, (IASTName) o };
    						else
    						    resultArray = (IASTName[]) ArrayUtil.append( IASTName.class, resultArray, o );
                        } else if( o instanceof IASTName [] ){
                            IASTName [] oa = (IASTName[]) o;
                            if( resultName == null ){
    						    resultName = oa[0];
    						    resultArray = oa;
                            } else if( resultArray == null ){
    						    resultArray = new IASTName[ 1 + oa.length ];
    						    resultArray[0] = resultName;
    						    resultArray = (IASTName[]) ArrayUtil.addAll( IASTName.class, resultArray, oa );
                            } else {
                                resultArray = (IASTName[]) ArrayUtil.addAll( IASTName.class, resultArray, oa );
                            }
                        }
                    }
				}
			} else if( declSpec instanceof IASTEnumerationSpecifier ){
			    IASTEnumerationSpecifier enumeration = (IASTEnumerationSpecifier) declSpec;
			    specName = enumeration.getName();

			    //check enumerators too
			    IASTEnumerator [] list = enumeration.getEnumerators();
			    IASTName tempName;
			    for( int i = 0; i < list.length; i++ ) {
			        IASTEnumerator enumerator = list[i];
			        if( enumerator == null ) break;
			        tempName = enumerator.getName();
			        scope.addName( tempName );
			        if( !data.typesOnly && nameMatches( data, tempName ) ) {
			            if( resultName == null )
						    resultName = tempName;
						else if( resultArray == null )
						    resultArray = new IASTName[] { resultName, tempName };
						else
						    resultArray = (IASTName[]) ArrayUtil.append( IASTName.class, resultArray, tempName );
					}
			    }
			}
			if( specName != null ) {
			    scope.addName( specName );
			    if( nameMatches( data, specName ) ) {
				    if( resultName == null )
					    resultName = specName;
					else if( resultArray == null )
					    resultArray = new IASTName[] { resultName, specName };
					else
					    resultArray = (IASTName[]) ArrayUtil.append( IASTName.class, resultArray, specName );
			    }
			}
		} else if( declaration instanceof ICPPASTUsingDeclaration ){
			ICPPASTUsingDeclaration using = (ICPPASTUsingDeclaration) declaration;
			IASTName name = using.getName();
			if( name instanceof ICPPASTQualifiedName ){
				IASTName [] ns = ((ICPPASTQualifiedName)name).getNames();
				name = ns[ ns.length - 1 ];
			}
			scope.addName( name );
			if( nameMatches( data, name ) ) {
				return name;
			}
		} else if( declaration instanceof ICPPASTNamespaceDefinition ){
			IASTName namespaceName = ((ICPPASTNamespaceDefinition) declaration).getName();
			scope.addName( namespaceName );
			if( nameMatches( data, namespaceName ) )
				return namespaceName;
		} else if( declaration instanceof ICPPASTNamespaceAlias ){
			IASTName alias = ((ICPPASTNamespaceAlias) declaration).getAlias();
			scope.addName( alias );
			if( nameMatches( data, alias ) )
				return alias;
		} else if( declaration instanceof IASTFunctionDefinition ){
			IASTFunctionDefinition functionDef = (IASTFunctionDefinition) declaration;
			if( ! ((ICPPASTDeclSpecifier) functionDef.getDeclSpecifier()).isFriend() ){
				IASTFunctionDeclarator declarator = functionDef.getDeclarator();
				
				//check the function itself
				IASTName declName = declarator.getName();
				scope.addName( declName );
	
			    if( !data.typesOnly && nameMatches( data, declName ) ) {
					return declName;
				}
			}
		}
		
		if( resultArray != null )
		    return resultArray;
		return resultName;
	}

	private static final boolean nameMatches( LookupData data, IASTName potential ){
	    if( potential instanceof ICPPASTQualifiedName ){
	        //A qualified name implies the name actually belongs to a different scope, and should
	        //not be considered here.
	        return false;
	    }
	    char[] c = potential.toCharArray();
	    char [] n = data.name();
	    return ( (data.prefixLookup && CharArrayUtils.equals( c, 0, n.length, n )) || 
			     (!data.prefixLookup && CharArrayUtils.equals( c, n )) );
	}
	
	private static void addDefinition( IBinding binding, IASTName name ){
		if( binding instanceof IFunction ){
			IASTNode node =  name.getParent();
			if( node instanceof ICPPASTQualifiedName )
				node = node.getParent();
			if( node instanceof ICPPASTFunctionDeclarator && node.getParent() instanceof IASTFunctionDefinition ){
				if( binding instanceof ICPPInternalBinding )
				((ICPPInternalBinding)binding).addDefinition( node );
			}
		}
	}

	static protected IBinding resolveAmbiguities( IASTName name, Object[] bindings ){
	    bindings = ArrayUtil.trim( Object.class, bindings );
	    if( bindings == null || bindings.length == 0 )
	        return null;
	    else if( bindings.length == 1 ){
	        if( bindings[0] instanceof IBinding )
	    	    return (IBinding) bindings[0];
	    	else if( bindings[0] instanceof IASTName && ((IASTName) bindings[0]).getBinding() != null )
	    	    return ((IASTName) bindings[ 0 ]).getBinding();

	    }
	    
	    if( name.getPropertyInParent() != STRING_LOOKUP_PROPERTY ) {
		    LookupData data = createLookupData( name, false );
		    data.foundItems = bindings;
		    try {
	            return resolveAmbiguities( data, name );
	        } catch ( DOMException e ) {
	            return e.getProblem();
	        }
	    }
	    
        IBinding [] result = null;
        for ( int i = 0; i < bindings.length; i++ ) {
            if( bindings[i] instanceof IASTName )
                result = (IBinding[]) ArrayUtil.append( IBinding.class, result, ((IASTName)bindings[i]).resolveBinding() );
            else if( bindings[i] instanceof IBinding )
                result = (IBinding[]) ArrayUtil.append( IBinding.class, result, bindings[i] );
        }
        return new CPPCompositeBinding( result );
	}
	
	static public boolean declaredBefore( Object obj, IASTNode node ){
	    if( node == null ) return true;
	    if( node.getPropertyInParent() == STRING_LOOKUP_PROPERTY ) return true;
	    
	    ASTNode nd = null;
	    if( obj instanceof ICPPSpecialization ){
	        obj = ((ICPPSpecialization)obj).getSpecializedBinding();
	    }
	    
	    if( obj instanceof ICPPInternalBinding ){
	        ICPPInternalBinding cpp = (ICPPInternalBinding) obj;
	        IASTNode[] n = cpp.getDeclarations();
	        if( n != null && n.length > 0 ) {
	        	nd = (ASTNode) n[0];
	        }
	        ASTNode def = (ASTNode) cpp.getDefinition();
	        if( def != null ){
	        	if( nd == null || def.getOffset() < nd.getOffset() )
	        		nd = def;
	        }
	        if( nd == null ) 
	            return true;
	    } else if( obj instanceof ASTNode ){
	        nd = (ASTNode) obj;
	    }
	    
	    if( nd != null ){
	        int pointOfDecl = 0;
            ASTNodeProperty prop = nd.getPropertyInParent();
            //point of declaration for a name is immediately after its complete declarator and before its initializer
            if( prop == IASTDeclarator.DECLARATOR_NAME || nd instanceof IASTDeclarator ){
                IASTDeclarator dtor = (IASTDeclarator)((nd instanceof IASTDeclarator) ? nd : nd.getParent());
                while( dtor.getParent() instanceof IASTDeclarator )
                    dtor = (IASTDeclarator) dtor.getParent();
                IASTInitializer init = dtor.getInitializer();
                if( init != null )
                    pointOfDecl = ((ASTNode)init).getOffset() - 1;
                else
                    pointOfDecl = ((ASTNode)dtor).getOffset() + ((ASTNode)dtor).getLength();
            } 
            //point of declaration for an enumerator is immediately after it enumerator-definition
            else if( prop == IASTEnumerator.ENUMERATOR_NAME) {
                IASTEnumerator enumtor = (IASTEnumerator) nd.getParent();
                if( enumtor.getValue() != null ){
                    ASTNode exp = (ASTNode) enumtor.getValue();
                    pointOfDecl = exp.getOffset() + exp.getLength();
                } else {
                    pointOfDecl = nd.getOffset() + nd.getLength();
                }
            } else if( prop == ICPPASTUsingDeclaration.NAME ){
                nd = (ASTNode) nd.getParent();
            	pointOfDecl = nd.getOffset();
            } else if( prop == ICPPASTNamespaceAlias.ALIAS_NAME ){
            	nd = (ASTNode) nd.getParent();
            	pointOfDecl = nd.getOffset() + nd.getLength();
            } else 
                pointOfDecl = nd.getOffset() + nd.getLength();
            
            return ( pointOfDecl < ((ASTNode)node).getOffset() );
	        
	    }
	    return true; // TODO - I changed this to true 
	}
	
	static private IBinding resolveAmbiguities( CPPSemantics.LookupData data, IASTName name ) throws DOMException {
	    if( !data.hasResults() || data.prefixLookup )
	        return null;
	      
	    IBinding type = null;
	    IBinding obj  = null;
	    IBinding temp = null;
	    ObjectSet fns = ObjectSet.EMPTY_SET;
	    ObjectSet templateFns = ObjectSet.EMPTY_SET;
	    
	    Object [] items = (Object[]) data.foundItems;
	    for( int i = 0; i < items.length && items[i] != null; i++ ){
	        Object o = items[i];
	        boolean declaredBefore = declaredBefore( o, name );
	        if( !data.checkWholeClassScope && !declaredBefore )
	        	continue;
	        if( o instanceof IASTName ){
	            temp = ((IASTName) o).resolveBinding();
	            if( temp == null )
	                continue;
	        } else if( o instanceof IBinding ){
	            temp = (IBinding) o;
	            
	        } else
	            continue;

	        if( !( temp instanceof ICPPMember ) && !declaredBefore )
                continue;
	        if( temp instanceof ICPPUsingDeclaration ){
	        	IBinding [] bindings = ((ICPPUsingDeclaration) temp).getDelegates();
	        	mergeResults( data, bindings, false );
	        	items = (Object[]) data.foundItems;
	        	continue;
	        } else if( temp instanceof CPPCompositeBinding ){
	        	IBinding [] bindings = ((CPPCompositeBinding)temp).getBindings();
	        	mergeResults( data, bindings, false );
	        	items = (Object[]) data.foundItems;
	        	continue;
	        } else if( temp instanceof IType ){
	        	if( type == null ){
	                type = temp;
	        	} else if( (temp instanceof ICPPDelegate && ((ICPPDelegate)temp).getBinding() == type) ||
		        	       (type instanceof ICPPDelegate && ((ICPPDelegate)type).getBinding() == temp) ||
		        	       (type instanceof ICPPDelegate && temp instanceof ICPPDelegate && 
		        	    	((ICPPDelegate)type).getBinding() == ((ICPPDelegate)temp).getBinding()) )
	        	{
	        	    //ok, delegates are synonyms
	        	} else if( type instanceof ICPPClassTemplate && temp instanceof ICPPSpecialization &&
						   ((ICPPSpecialization)temp).getSpecializedBinding() == type )
				{
					//ok, stay with the template, the specialization, if applicable, will come out during instantiation
				} else if( type != temp && !((IType)type).isSameType( (IType) temp )) {
	                return new ProblemBinding( data.astName, IProblemBinding.SEMANTIC_AMBIGUOUS_LOOKUP, data.name() );
	            }
	        } else if( temp instanceof IFunction ){
	        	if( temp instanceof ICPPTemplateDefinition ){
	        		if( templateFns == ObjectSet.EMPTY_SET )
	        			templateFns = new ObjectSet(2);
	        		templateFns.put( temp );
	        	} else { 
	        		if( fns == ObjectSet.EMPTY_SET )
	        			fns = new ObjectSet(2);
	        		fns.put( temp );
	        	}
	        } else {
	        	if( obj == null )
	        		obj = temp;
	        	else if( (temp instanceof ICPPDelegate && ((ICPPDelegate)temp).getBinding() == obj) ||
	        	         (obj instanceof ICPPDelegate && ((ICPPDelegate)obj).getBinding() == temp) )
	        	{
	        	    //ok, delegates are synonyms
	        	} else if( obj != temp ){
	        	    return new ProblemBinding( data.astName, IProblemBinding.SEMANTIC_AMBIGUOUS_LOOKUP, data.name() );
	        	}
	        }
	    }
	    if( data.forUsingDeclaration() ){
	        IBinding [] bindings = null;
	        if( obj != null ){
	            if( fns.size() > 0 ) return new ProblemBinding( data.astName, IProblemBinding.SEMANTIC_AMBIGUOUS_LOOKUP, data.name() );
	//            if( type == null ) return obj;
	            bindings = (IBinding[]) ArrayUtil.append( IBinding.class, bindings, obj );
	            bindings = (IBinding[]) ArrayUtil.append( IBinding.class, bindings, type );
	        } else {
//	            if( fns == null ) return type;
	            bindings = (IBinding[]) ArrayUtil.append( IBinding.class, bindings, type );
	            bindings = (IBinding[]) ArrayUtil.addAll( IBinding.class, bindings, fns.keyArray() );
	        }
	        bindings = (IBinding[]) ArrayUtil.trim( IBinding.class, bindings );
	        ICPPUsingDeclaration composite = new CPPUsingDeclaration( data.astName, bindings );
	        return composite;	
	    }
	        
	    int numTemplateFns = templateFns.size();
		if( numTemplateFns > 0 ){
			if( data.functionParameters != null && !data.forDefinition() ){
				IFunction [] fs  = CPPTemplates.selectTemplateFunctions( templateFns, data.functionParameters, data.astName );
				if( fs != null && fs.length > 0){
				    if( fns == ObjectSet.EMPTY_SET )
				        fns = new ObjectSet( fs.length );
					fns.addAll( fs );
				}
			} else {
				if( fns == ObjectSet.EMPTY_SET )
					fns = templateFns;
				else
					fns.addAll( templateFns );
			}
		}
		int numFns = fns.size();
	    if( type != null ) {
	    	if( data.typesOnly || (obj == null && numFns == 0 ) )
	    		return type;
	    }
	   
	    if( numFns > 0 ){
	    	if( obj != null )
	    		return new ProblemBinding( data.astName, IProblemBinding.SEMANTIC_AMBIGUOUS_LOOKUP, data.name() );
	    	return resolveFunction( data, (IBinding[]) fns.keyArray( IBinding.class ) );
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
			return isSameFunction( function, (IASTDeclarator) node );
		}
	 	return false;
	}
	
	static private void reduceToViable( LookupData data, IBinding[] functions ) throws DOMException{
	    if( functions == null || functions.length == 0 )
	        return;
	    
		Object [] fParams = data.functionParameters;
		int numParameters = ( fParams != null ) ? fParams.length : 0;		
		int num;	
		boolean def = data.forDefinition();	
		//Trim the list down to the set of viable functions
		IFunction function = null;
		int size = functions.length;
		for( int i = 0; i < size && functions[i] != null; i++ ){
			function = (IFunction) functions[i];
			num = function.getParameters().length;
		
			//if there are m arguments in the list, all candidate functions having m parameters
			//are viable	 
			if( num == numParameters ){
				if( def && !isMatchingFunctionDeclaration( function, data ) ){
					functions[i] = null;
				}
				continue;
			}
			//check for void
			else if( numParameters == 0 && num == 1 ){
			    IParameter param = function.getParameters()[0];
			    IType t = param.getType();
			    if( t instanceof IBasicType && ((IBasicType)t).getType() == IBasicType.t_void )
			        continue;
			}
			
			if( def ){
				//if this is for a definition, we had to match the number of parameters.
				functions[i] = null;
				continue;
			}
			
			//A candidate function having fewer than m parameters is viable only if it has an 
			//ellipsis in its parameter list.
			if( num < numParameters ){
			    if( function.takesVarArgs() )
			        continue;
				//not enough parameters, remove it
				functions[i] = null;
			} 
			//a candidate function having more than m parameters is viable only if the (m+1)-st
			//parameter has a default argument
			else {
			    IParameter [] params = function.getParameters();
			    for( int j = num - 1; j >= numParameters; j-- ){
					if( ((ICPPParameter)params[j]).getDefaultValue() == null ){
					    functions[i] = null;
						break;
					}
				}
			}
		}
	}
	static private boolean isMatchingFunctionDeclaration( IFunction candidate, LookupData data ){
		IASTName name = data.astName;
		ICPPASTTemplateDeclaration decl = CPPTemplates.getTemplateDeclaration( name );
		if( decl != null && !(candidate instanceof ICPPTemplateDefinition) ) 
		    return false;
		
		if( candidate instanceof ICPPTemplateDefinition && decl instanceof ICPPASTTemplateSpecialization ){
			ICPPFunctionTemplate fn = CPPTemplates.resolveTemplateFunctions( new Object [] { candidate }, data.astName );
			return ( fn != null && !(fn instanceof IProblemBinding ) );
		} 

		try {
		    IASTNode node = data.astName.getParent();
		    while( node instanceof IASTName )
		        node = node.getParent();
		    if( !(node instanceof ICPPASTFunctionDeclarator) )
		        return false;
		    ICPPASTFunctionDeclarator dtor = (ICPPASTFunctionDeclarator) node;
		    ICPPFunctionType ftype = (ICPPFunctionType) candidate.getType();
		    if( dtor.isConst() != ftype.isConst() || dtor.isVolatile() != ftype.isVolatile() )
		        return false;
			return functionHasParameters( candidate, (IASTParameterDeclaration[]) data.functionParameters );
		} catch (DOMException e) {
		} 
		return false;
	}
	
	static private IType[] getSourceParameterTypes( Object [] params  ){
	    if( params instanceof IType[] ){
	        return (IType[]) params;
	    } 
	    
	    if( params == null || params.length == 0 )
	        return new IType[] { VOID_TYPE };
	    
	    if( params instanceof IASTExpression [] ){
			IASTExpression [] exps = (IASTExpression[]) params;
			IType [] result = new IType[ exps.length ];
			for ( int i = 0; i < exps.length; i++ ) {
			    result[i] = CPPVisitor.getExpressionType( exps[i] );
            }
			return result;
		} else if( params instanceof IASTParameterDeclaration[] ){
		    IASTParameterDeclaration [] decls = (IASTParameterDeclaration[]) params;
		    IType [] result = new IType[ decls.length ];
			for ( int i = 0; i < params.length; i++ ) {
			    result[i] = CPPVisitor.createType( decls[i].getDeclarator() );
            }
			return result;
		}
		return null;
	}
	
	static private IType [] getTargetParameterTypes( IFunction fn ) throws DOMException{
	    IParameter [] params = fn.getParameters();

	    boolean useImplicit = ( fn instanceof ICPPMethod && !(fn instanceof ICPPConstructor) );
	    IType [] result = new IType[ useImplicit ? params.length + 1 : params.length ];
	    
	    if( useImplicit ){
		    ICPPFunctionType ftype = (ICPPFunctionType) ((ICPPFunction)fn).getType();
			IScope scope = fn.getScope();
			if( scope instanceof ICPPTemplateScope )
				scope = scope.getParent();
			ICPPClassType cls = null;
			if( scope instanceof ICPPClassScope ){
				cls = ((ICPPClassScope)scope).getClassType();
			} else {
				cls = new CPPClassType.CPPClassTypeProblem( scope.getPhysicalNode(), IProblemBinding.SEMANTIC_BAD_SCOPE, fn.getNameCharArray() );
			}
			if( cls instanceof ICPPClassTemplate ){
			    cls = (ICPPClassType) CPPTemplates.instantiateWithinClassTemplate( (ICPPClassTemplate) cls );
			}
			IType implicitType = cls;
			if( ftype.isConst() || ftype.isVolatile() ){
				implicitType = new CPPQualifierType( implicitType, ftype.isConst(), ftype.isVolatile() );
			}
			implicitType = new CPPReferenceType( implicitType );

			result[0] = implicitType;
	    }
	    for( int i = 0; i < params.length; i++ )
	        result = (IType[]) ArrayUtil.append( IType.class, result, params[i].getType() );
		
	    return result;
	}
	
	static private IBinding resolveFunction( CPPSemantics.LookupData data, IBinding[] fns ) throws DOMException{
	    fns = (IBinding[]) ArrayUtil.trim( IBinding.class, fns );
	    if( fns == null || fns.length == 0 )
	        return null;
	    
		if( data.forUsingDeclaration() ){
			return new CPPUsingDeclaration( data.astName, fns );
		}
		
		//we don't have any arguments with which to resolve the function
		if( data.functionParameters == null ){
		    return resolveTargetedFunction( data, fns );
		}
		//reduce our set of candidate functions to only those who have the right number of parameters
		reduceToViable( data, fns );
		
		if( data.forDefinition() || data.forExplicitInstantiation() ){
			for (int i = 0; i < fns.length; i++) {
				if( fns[i] != null ){
					return fns[i];
				}
			}
			return null;
		}
		
		IFunction bestFn = null;					//the best function
		IFunction currFn = null;					//the function currently under consideration
		Cost [] bestFnCost = null;				//the cost of the best function
		Cost [] currFnCost = null;				//the cost for the current function
				
		IType source = null;					//parameter we are called with
		IType target = null;					//function's parameter
		
		int comparison;
		Cost cost = null;						//the cost of converting source to target
		Cost temp = null;						//the cost of using a user defined conversion to convert source to target
				 
		boolean hasWorse = false;				//currFn has a worse parameter fit than bestFn
		boolean hasBetter = false;				//currFn has a better parameter fit than bestFn
		boolean ambiguous = false;				//ambiguity, 2 functions are equally good
		boolean currHasAmbiguousParam = false;	//currFn has an ambiguous parameter conversion (ok if not bestFn)
		boolean bestHasAmbiguousParam = false;  //bestFn has an ambiguous parameter conversion (not ok, ambiguous)

		IType [] sourceParameters = getSourceParameterTypes( data.functionParameters ); //the parameters the function is being called with
		IType [] targetParameters = null;
		boolean sourceVoid = ( data.functionParameters == null || data.functionParameters.length == 0 );
		int numSourceParams = 0;
		int targetLength = 0;
		int numFns = fns.length;
		
		IType impliedObjectType = data.getImpliedObjectArgument();
		
		outer: for( int fnIdx = 0; fnIdx < numFns; fnIdx++ ){
			currFn = (IFunction) fns[fnIdx];
			
			if( currFn == null || bestFn == currFn ||
			    ( bestFn instanceof ICPPDelegate && ((ICPPDelegate)bestFn).getBinding() == currFn ) ||
			    ( currFn instanceof ICPPDelegate && ((ICPPDelegate)currFn).getBinding() == bestFn ) )
			{
				continue;
			}
	
			targetParameters = getTargetParameterTypes( currFn );

			targetLength = targetParameters.length;
			boolean useImplicitObj = ( currFn instanceof ICPPMethod && !(currFn instanceof ICPPConstructor) );
			numSourceParams = ( useImplicitObj ) ? sourceParameters.length + 1 : sourceParameters.length;
			int numTargetParams = 0;
			
			if( currFnCost == null || currFnCost.length != ((numSourceParams == 0) ? 1 : numSourceParams) ){
				currFnCost = new Cost [ (numSourceParams == 0) ? 1 : numSourceParams ];	
			}
			
			comparison = 0;
			boolean varArgs = false;
			
			for( int j = 0; j < numSourceParams || j == 0; j++ ){
			    if( useImplicitObj ) {
			        source = ( j == 0 ) ? impliedObjectType : sourceParameters[j - 1];
			    	numTargetParams = ( targetLength == 1 ) ? 2 : targetLength;
			    } else { 
			        source = sourceParameters[j];
			        numTargetParams = ( targetLength == 0 ) ? 1 : targetLength;
			    }
		    
				if( j < numTargetParams ){
				    if( (useImplicitObj && targetLength == 1 && j == 1) ||
				        (!useImplicitObj && targetLength == 0 && j == 0) )
				    {
				        target = VOID_TYPE;
				    } else {
					    target = targetParameters[j];
					}
				} else 
					varArgs = true;
				
				if( useImplicitObj && j == 0 && ((ICPPInternalFunction)currFn).isStatic( false ) ) {
				    //13.3.1-4 for static member functions, the implicit object parameter is considered to match any object
				    cost = new Cost( source, target );
					cost.rank = Cost.IDENTITY_RANK;	//exact match, no cost
				} else if( source == null ){
				    continue outer;
				} else if( varArgs ){
					cost = new Cost( source, null );
					cost.rank = Cost.ELLIPSIS_CONVERSION;
				} else if( source.isSameType( target )  ||
				   	      ( sourceVoid && ((useImplicitObj && j == 1)||(!useImplicitObj && j == 0)) ) )
				{
					cost = new Cost( source, target );
					cost.rank = Cost.IDENTITY_RANK;	//exact match, no cost
				} else {
					cost = checkStandardConversionSequence( source, target );
					//12.3-4 At most one user-defined conversion is implicitly applied to
					//a single value.  (also prevents infinite loop)				
					if( (cost.rank == Cost.NO_MATCH_RANK || cost.rank == Cost.FUZZY_TEMPLATE_PARAMETERS ) && 
						!data.forUserDefinedConversion )
					{
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
			int len = ( bestFnCost == null || currFnCost.length < bestFnCost.length ) ? currFnCost.length : bestFnCost.length;
			for( int j = 1; j <= len; j++ ){
				Cost currCost = currFnCost[ currFnCost.length - j ];
				if( currCost.rank < 0 ){
					hasWorse = true;
					hasBetter = false;
					break;
				}
				
				//an ambiguity in the user defined conversion sequence is only a problem
				//if this function turns out to be the best.
				currHasAmbiguousParam = ( currCost.userDefined == 1 );
				if( bestFnCost != null ){
					comparison = currCost.compare( bestFnCost[ bestFnCost.length - j ] );
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
				//if they are both template functions, we can order them that way
				boolean bestIsTemplate = (bestFn instanceof ICPPSpecialization && 
										 ((ICPPSpecialization)bestFn).getSpecializedBinding() instanceof ICPPFunctionTemplate);
				boolean currIsTemplate = (currFn instanceof ICPPSpecialization && 
						 				((ICPPSpecialization)currFn).getSpecializedBinding() instanceof ICPPFunctionTemplate);
				if( bestIsTemplate && currIsTemplate )
				{
						ICPPFunctionTemplate t1 = (ICPPFunctionTemplate) ((ICPPSpecialization)bestFn).getSpecializedBinding();
						ICPPFunctionTemplate t2 = (ICPPFunctionTemplate) ((ICPPSpecialization)currFn).getSpecializedBinding();
						int order = CPPTemplates.orderTemplateFunctions( t1, t2);
						if ( order < 0 ){
							hasBetter = true;	 				
						} else if( order > 0 ){
							ambiguous = false;
						}
				}
				//we prefer normal functions over template functions, unless we specified template arguments
				else if( bestIsTemplate && !currIsTemplate ){
					if( data.preferTemplateFunctions() )
						ambiguous = false;
					else
						hasBetter = true;
				} else if( !bestIsTemplate && currIsTemplate ){
					if( data.preferTemplateFunctions() )
						hasBetter = true;
					else
						ambiguous = false;
						
				} 
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
			return new ProblemBinding( data.astName, IProblemBinding.SEMANTIC_AMBIGUOUS_LOOKUP, data.name() );
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
                return new ProblemBinding( data.astName, IProblemBinding.SEMANTIC_AMBIGUOUS_LOOKUP, data.name() );

            for( int i = 0; i < fns.length; i++ ){
                IFunction fn = (IFunction) fns[i];
                IType ft = null;
                try {
                     ft = fn.getType();
                } catch ( DOMException e ) {
                    ft = e.getProblem();
                }
                if( type.isSameType( ft ) ){
                    if( result == null )
                        result = fn;
                    else
                        return new ProblemBinding( data.astName, IProblemBinding.SEMANTIC_AMBIGUOUS_LOOKUP, data.name() );
                }
            }

            if( idx > 0 && ++idx < types.length  ){
                type = types[idx];
            } else {
                type = null;
            }
        }
                
        return ( result != null ) ? result : new ProblemBinding( data.astName, IProblemBinding.SEMANTIC_AMBIGUOUS_LOOKUP, data.name() ); 
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
			if( prop == IASTDeclarator.INITIALIZER ){
				IASTDeclarator dtor = (IASTDeclarator) node.getParent();
				return CPPVisitor.createType( dtor );
			} else if( prop == IASTInitializerExpression.INITIALIZER_EXPRESSION ){
                IASTInitializerExpression initExp = (IASTInitializerExpression) node.getParent();
                if( initExp.getParent() instanceof IASTDeclarator ){
	                IASTDeclarator dtor = (IASTDeclarator) initExp.getParent();
	                return CPPVisitor.createType( dtor );
                }
                return null;
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
            else if( prop == IASTCastExpression.OPERAND )
            {
            	IASTCastExpression cast = (IASTCastExpression) node.getParent();
            	return CPPVisitor.createType( cast.getTypeId().getAbstractDeclarator() );
            }
            //target is a template non-type parameter (14.3.2-5)
            else if( prop == ICPPASTTemplateId.TEMPLATE_ID_ARGUMENT ){
                ICPPASTTemplateId id = (ICPPASTTemplateId) node.getParent();
                IASTNode [] args = id.getTemplateArguments();
                int i = 0;
                for ( ; i < args.length; i++ ) {
                    if( args[i] == node ){
                        break;
                    }
                }
                ICPPTemplateDefinition template = (ICPPTemplateDefinition) id.getTemplateName().resolveBinding();
                if( template != null ){
                    try {
                        ICPPTemplateParameter [] ps = template.getTemplateParameters();
                        if( i < args.length && i < ps.length && ps[i] instanceof ICPPTemplateNonTypeParameter ){
                            return ((ICPPTemplateNonTypeParameter)ps[i]).getType();
                        }
                    } catch ( DOMException e ) {
                        return null;
                    }
                }
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
    
    static protected Cost checkStandardConversionSequence( IType source, IType target ) throws DOMException {
		Cost cost = lvalue_to_rvalue( source, target );
		
		if( cost.source == null || cost.target == null ){
			return cost;
		}
			
		if( cost.source.isSameType( cost.target ) ){
			cost.rank = Cost.IDENTITY_RANK;
			return cost;
		}
	
		qualificationConversion( cost );
		
		//if we can't convert the qualifications, then we can't do anything
		if( cost.qualification == Cost.NO_MATCH_RANK ){
			return cost;
		}
		
		//was the qualification conversion enough?
		IType s = getUltimateType( cost.source, true );
		IType t = getUltimateType( cost.target, true );
		
		if( s == null || t == null ){
		    cost.rank = Cost.NO_MATCH_RANK;
			return cost;
		}
		
		if( s.isSameType( t ) ){
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
		
		if( cost.rank == -1 ){
			relaxTemplateParameters( cost );
		}
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
			ICPPConstructor [] constructors = ((ICPPClassType)t).getConstructors();
			if( constructors.length > 0 ){
			    if( constructors.length == 1 && constructors[0] instanceof IProblemBinding )
			        constructor = null;
			    else {
					LookupData data = new LookupData();
					data.forUserDefinedConversion = true;
					data.functionParameters = new IType [] { source };
			    	IBinding binding = resolveFunction( data, constructors );
			    	if( binding instanceof ICPPConstructor )
			    		constructor = (ICPPConstructor) binding;
			    }
			}
			if( constructor != null && !constructor.isExplicit() ){
				constructorCost = checkStandardConversionSequence( t, target );
			}
		}
		
		//conversion operators
		if( s instanceof ICPPInternalClassType ){
			ICPPMethod [] ops = ((ICPPInternalClassType)s).getConversionOperators();
			if( ops.length > 0 && !(ops[0] instanceof IProblemBinding) ){
				Cost [] costs = null;
				for (int i = 0; i < ops.length; i++) {
					cost = checkStandardConversionSequence( ops[i].getType().getReturnType(), target );
					if( cost.rank != Cost.NO_MATCH_RANK )
						costs = (Cost[]) ArrayUtil.append( Cost.class, costs, cost );
				}
				if( costs != null ){
					Cost best = costs[0];
					boolean bestIsBest = true;
					int bestIdx = 0;
					for (int i = 1; i < costs.length && costs[i] != null; i++) {
						int comp = best.compare( costs[i] );
						if( comp == 0 )
							bestIsBest = false;
						else if( comp > 0 ){
							best = costs[ bestIdx = i ];
							bestIsBest = true;
						}
					}
					if( bestIsBest ){
						conversion = ops[ bestIdx ]; 
						conversionCost = best;
					}
				}
			}
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
		if( type instanceof ICPPClassType && type instanceof ICPPInternalBinding )
			return (((ICPPInternalBinding)type).getDefinition() != null );
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
			
		//4.3 function to pointer conversion
		if( target instanceof IPointerType && ((IPointerType)target).getType() instanceof IFunctionType &&
		    source instanceof IFunctionType )
	    {
		    source = new CPPPointerType( source );
	    }
	    //4.2 Array-To-Pointer conversion
		else if( target instanceof IPointerType && source instanceof IArrayType ){
			source = new CPPPointerType( ((IArrayType)source).getType() );
		}
		
		//4.1 if T is a non-class type, the type of the rvalue is the cv-unqualified version of T
		if( source instanceof IQualifierType ){
			IType t = ((IQualifierType)source).getType();
			while( t instanceof ITypedef )
				t = ((ITypedef)t).getType();
			if( !(t instanceof ICPPClassType) ){
				source = t;
			}
		} else if( source instanceof IPointerType && 
				   ( ((IPointerType)source).isConst() || ((IPointerType)source).isVolatile() ) )
		{
			IType t = ((IPointerType)source).getType();
			while( t instanceof ITypedef )
				t = ((ITypedef)t).getType();
			if( !(t instanceof ICPPClassType) ){
				source = new CPPPointerType( t );
			}
		}
		
		cost.source = source;
		cost.target = target;
		
		return cost;
	}
	
	static private void qualificationConversion( Cost cost ) throws DOMException{
		boolean canConvert = true;
		int requiredConversion = Cost.IDENTITY_RANK;  

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
				requiredConversion = Cost.NO_MATCH_RANK;
				break;
			}
			//if cv1,j and cv2,j are different then const is in every cv2,k for 0<k<j
			if( !constInEveryCV2k && ( op1.isConst() != op2.isConst() ||
									   op1.isVolatile() != op2.isVolatile() ) )
			{
				canConvert = false;
				requiredConversion = Cost.NO_MATCH_RANK;
				break; 
			}
			constInEveryCV2k &= op2.isConst();
			s = op1.getType();
			t = op2.getType();
		}
		
		if( s instanceof IQualifierType ^ t instanceof IQualifierType ){
		    if( t instanceof IQualifierType ){
		        canConvert = true;
		        requiredConversion = Cost.CONVERSION_RANK;
		    } else {
		    	//4.2-2 a string literal can be converted to pointer to char
		    	if( t instanceof IBasicType && ((IBasicType)t).getType() == IBasicType.t_char &&
		    		s instanceof IQualifierType )
		    	{
		    		IType qt = ((IQualifierType)s).getType();
		    		if( qt instanceof IBasicType ){
		    			IASTExpression val = ((IBasicType)qt).getValue();
		    			canConvert = (val != null && 
		    						  val instanceof IASTLiteralExpression && 
									  ((IASTLiteralExpression)val).getKind() == IASTLiteralExpression.lk_string_literal );
		    		} else {
		    			canConvert = false;
		    			requiredConversion = Cost.NO_MATCH_RANK;
		    		}
		    	} else {
		    		canConvert = false;
		    		requiredConversion = Cost.NO_MATCH_RANK;
		    	}
		    }
		} else if( s instanceof IQualifierType && t instanceof IQualifierType ){
			IQualifierType qs = (IQualifierType) s, qt = (IQualifierType) t;
			if( qs.isConst() && !qt.isConst() || qs.isVolatile() && !qt.isVolatile() )
				requiredConversion = Cost.NO_MATCH_RANK;
			else if( qs.isConst() == qt.isConst() && qs.isVolatile() == qt.isVolatile() )
				requiredConversion = Cost.IDENTITY_RANK;
			else
				requiredConversion = Cost.CONVERSION_RANK;
		} else if( constInEveryCV2k && !canConvert ){
			canConvert = true;
			requiredConversion = Cost.CONVERSION_RANK;
			int i = 1;
			for( IType type = s; canConvert == true && i == 1; type = t, i++  ){
				while( type instanceof ITypeContainer ){
					if( type instanceof IQualifierType )
						canConvert = false;
					else if( type instanceof IPointerType ){
						canConvert = !((IPointerType)type).isConst() && !((IPointerType)type).isVolatile();
					}
					if( !canConvert ){
						requiredConversion = Cost.NO_MATCH_RANK;
						break;
					}
					type = ((ITypeContainer)type).getType();
				}
			}
		}

		cost.qualification = requiredConversion;
		if( canConvert == true ){
			cost.rank = Cost.LVALUE_OR_QUALIFICATION_RANK;
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
		 
		if( src.isSameType( trg ) )
			return;
		
		if( src instanceof IBasicType && trg instanceof IBasicType ){
			int sType = ((IBasicType)src).getType();
			int tType = ((IBasicType)trg).getType();
			if( ( tType == IBasicType.t_int && ( sType == IBasicType.t_int ||   //short, long , unsigned etc
												 sType == IBasicType.t_char    || 
												 sType == ICPPBasicType.t_bool || 
												 sType == ICPPBasicType.t_wchar_t ||
												 sType == IBasicType.t_unspecified ) ) || //treat unspecified as int
				( tType == IBasicType.t_double && sType == IBasicType.t_float ) )
			{
				cost.promotion = 1; 
			}
		} else if( src instanceof IEnumeration && trg instanceof IBasicType &&
				   ( ((IBasicType)trg).getType() == IBasicType.t_int || 
				     ((IBasicType)trg).getType() == IBasicType.t_unspecified ) )
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
			
		IType sPrev = src;
		while( sPrev instanceof ITypeContainer ){
			IType next = ((ITypeContainer)sPrev).getType();
			while( next instanceof IQualifierType || next instanceof ITypedef ){
				next = ((ITypeContainer)next).getType();
			}
			if( next == s )
				break;
			sPrev = next;
		}

		if( src instanceof IBasicType && trg instanceof IPointerType ){
			//4.10-1 an integral constant expression of integer type that evaluates to 0 can be converted to a pointer type
			IASTExpression exp = ((IBasicType)src).getValue();
			if( exp instanceof IASTLiteralExpression && 
			    ((IASTLiteralExpression)exp).getKind() == IASTLiteralExpression.lk_integer_constant  )
			{
				try { 
					String val = exp.toString().toLowerCase().replace('u', '0');
					val.replace( 'l', '0' );
					if( Integer.decode( val ).intValue() == 0 ){
						cost.rank = Cost.CONVERSION_RANK;
						cost.conversion = 1;
					}
				} catch( NumberFormatException e ) {
				}
			}
		} else if( sPrev instanceof IPointerType ){
			IType tPrev = trg;
			while( tPrev instanceof ITypeContainer ){
				IType next = ((ITypeContainer)tPrev).getType();
				while( next instanceof IQualifierType || next instanceof ITypedef ){
					next = ((ITypeContainer)next).getType();
				}
				if( next == t )
					break;
				tPrev = next;
			}
			
			//4.10-2 an rvalue of type "pointer to cv T", where T is an object type can be
			//converted to an rvalue of type "pointer to cv void"
			if( tPrev instanceof IPointerType && t instanceof IBasicType && ((IBasicType)t).getType() == IBasicType.t_void ){
				cost.rank = Cost.CONVERSION_RANK;
				cost.conversion = 1;
				cost.detail = 2;
				return;
			}
			//4.10-3 An rvalue of type "pointer to cv D", where D is a class type can be converted
			//to an rvalue of type "pointer to cv B", where B is a base class of D.
			else if( s instanceof ICPPClassType && tPrev instanceof IPointerType && t instanceof ICPPClassType ){
				temp = hasBaseClass( (ICPPClassType)s, (ICPPClassType) t, false );
				cost.rank = ( temp > -1 ) ? Cost.CONVERSION_RANK : Cost.NO_MATCH_RANK;
				cost.conversion = ( temp > -1 ) ? temp : 0;
				cost.detail = 1;
				return;
			}
		} 
		if( t instanceof IBasicType && s instanceof IBasicType || s instanceof IEnumeration ){
			//4.7 An rvalue of an integer type can be converted to an rvalue of another integer type.  
			//An rvalue of an enumeration type can be converted to an rvalue of an integer type.
			cost.rank = Cost.CONVERSION_RANK;
			cost.conversion = 1;	
		} else if( t instanceof IBasicType && ((IBasicType)t).getType() == ICPPBasicType.t_bool && s instanceof IPointerType ){
			//4.12 pointer or pointer to member type can be converted to an rvalue of type bool
			cost.rank = Cost.CONVERSION_RANK;
			cost.conversion = 1;
		} else if( s instanceof ICPPPointerToMemberType && t instanceof ICPPPointerToMemberType ){
		    //4.11-2 An rvalue of type "pointer to member of B of type cv T", where B is a class type, 
			//can be converted to an rvalue of type "pointer to member of D of type cv T" where D is a
			//derived class of B
		    ICPPPointerToMemberType spm = (ICPPPointerToMemberType) s;
		    ICPPPointerToMemberType tpm = (ICPPPointerToMemberType) t;
		    if( spm.getType().isSameType( tpm.getType() ) ){
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
			int temp = hasBaseClass( (ICPPClassType) s, (ICPPClassType) t, false );
			
			if( temp > -1 ){
				cost.rank = Cost.DERIVED_TO_BASE_CONVERSION;
				cost.conversion = temp;
			}	
		}
	}
	static private void relaxTemplateParameters( Cost cost ){
		IType s = getUltimateType( cost.source, false );
		IType t = getUltimateType( cost.target, false );
		
		if( (s instanceof ICPPTemplateTypeParameter && t instanceof ICPPTemplateTypeParameter) ||
			(s instanceof ICPPTemplateTemplateParameter && t instanceof ICPPTemplateTemplateParameter ) )
		{
			cost.rank = Cost.FUZZY_TEMPLATE_PARAMETERS;
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
		
		
		IBinding parent = null;
		ICPPBase [] bases = clsSymbol.getBases();
		
		for( int i = 0; i < bases.length; i ++ ){
			ICPPBase wrapper = bases[i];	
			parent = bases[i].getBaseClass();
			boolean isVisible = ( wrapper.getVisibility() == ICPPBase.v_public);
			
			if( parent instanceof IType && 
				 ( ((IType)parent).isSameType( clsBase ) || 
				   ( clsBase instanceof ICPPSpecialization &&  //allow some flexibility with templates 
				     ((IType)((ICPPSpecialization)clsBase).getSpecializedBinding()).isSameType( (IType) parent ) ) ) )
			{
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
	
	public static ICPPFunction findOperator( IASTExpression exp, ICPPClassType cls ){
		IScope scope = null;
		try {
			scope = cls.getCompositeScope();
		} catch (DOMException e1) {
			return null;
		}
		if( scope == null )
			return null;
		
		CPPASTName astName = new CPPASTName();
		astName.setParent( exp );
	    astName.setPropertyInParent( STRING_LOOKUP_PROPERTY );
	    LookupData data = null;
	    
		if( exp instanceof IASTArraySubscriptExpression ){
		    astName.setName( ICPPASTOperatorName.OPERATOR_BRACKET );
		    data = new LookupData( astName );
		    data.forceQualified = true;
		    data.functionParameters = new IASTExpression [] { ((IASTArraySubscriptExpression)exp).getSubscriptExpression() };
		} else if( exp instanceof IASTFieldReference ){
			astName.setName( ICPPASTOperatorName.OPERATOR_ARROW );
			data = new LookupData( astName );
			data.forceQualified = true;
			data.functionParameters = IASTExpression.EMPTY_EXPRESSION_ARRAY;
		} else {
			return null;
		}
		
		try {
		    lookup( data, scope );
		    IBinding binding = resolveAmbiguities( data, astName );
		    if( binding instanceof ICPPFunction )
		    	return (ICPPFunction) binding;
		} catch( DOMException e ){
		}
		return null;
	}
	
	public static IBinding[] findBindings( IScope scope, String name, boolean qualified ) throws DOMException{
		return findBindings( scope, name.toCharArray(), qualified );
	}
	public static IBinding[] findBindings( IScope scope, char []name, boolean qualified ) throws DOMException{
	    CPPASTName astName = new CPPASTName();
	    astName.setName( name );
	    astName.setParent( scope.getPhysicalNode() );
	    astName.setPropertyInParent( STRING_LOOKUP_PROPERTY );
	    
	    LookupData data = new LookupData( astName );
	    data.forceQualified = qualified;
	            
	    try {
			lookup( data, scope );
		} catch (DOMException e) {
			return new IBinding [] { e.getProblem() };
		}
		
		Object [] items = (Object[]) data.foundItems;
		if( items == null )
		    return new IBinding[0];
		
		ObjectSet set = new ObjectSet( items.length );
		IBinding binding = null;
		for( int i = 0; i < items.length; i++ ){
		    if( items[i] instanceof IASTName )
		        binding = ((IASTName) items[i]).resolveBinding();
		    else if( items[i] instanceof IBinding )
		        binding = (IBinding) items[i];
		    else
		        binding = null;
		    
		    if( binding != null )
		    	if( binding instanceof ICPPUsingDeclaration ){
		    		set.addAll( ((ICPPUsingDeclaration)binding).getDelegates() );
		    	} else if( binding instanceof CPPCompositeBinding ){
                    set.addAll( ((CPPCompositeBinding)binding).getBindings() );
			    } else {
			        set.put( binding );
			    }
		}
		
	    return (IBinding[]) set.keyArray( IBinding.class );
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
                else if( obj instanceof IASTName ) {
					IBinding binding = ((IASTName) obj).resolveBinding();
                    if( binding != null && !(binding instanceof IProblemBinding))
                        result = (IBinding[]) ArrayUtil.append( IBinding.class, result, binding );
                } else if( obj instanceof Object [] ) {
					Object[] objs = (Object[]) obj;
					for (int j = 0; j < objs.length && objs[j] != null; j++) {
						Object item = objs[j];
						if( item instanceof IBinding )
		                    result = (IBinding[]) ArrayUtil.append( IBinding.class, result, item );
		                else if( item instanceof IASTName ) {
							IBinding binding = ((IASTName) item).resolveBinding();
		                    if( binding != null && !(binding instanceof IProblemBinding))
		                        result = (IBinding[]) ArrayUtil.append( IBinding.class, result, binding );
		                }
					}                        
                }
            }
        }
        
        IASTTranslationUnit tu = name.getTranslationUnit();
        if (tu != null) {
        	IPDOM pdom = tu.getIndex(); 
        	if (pdom != null)
        		result = (IBinding[])ArrayUtil.addAll(IBinding.class, result, pdom.resolvePrefix(name));
        }

        return (IBinding[]) ArrayUtil.trim( IBinding.class, result );
    }

	public static boolean isSameFunction(IFunction function, IASTDeclarator declarator) {
		IASTName name = declarator.getName();
		ICPPASTTemplateDeclaration templateDecl = CPPTemplates.getTemplateDeclaration( name );

		boolean fnIsTemplate = ( function instanceof ICPPFunctionTemplate );
		boolean dtorIsTemplate = ( templateDecl != null ); 
		if( fnIsTemplate && dtorIsTemplate ){
			return CPPTemplates.isSameTemplate( (ICPPTemplateDefinition)function, name );
		} else if( fnIsTemplate ^ dtorIsTemplate ){
			return false;
		} 
		IType type = null;
		try {
			type = function.getType();
			return type.isSameType( CPPVisitor.createType( declarator ) );
		} catch (DOMException e) {
		}
		return false;
	}

}
