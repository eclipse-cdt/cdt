
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
package org.eclipse.cdt.internal.core.dom.parser.c;

import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
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
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.ILabel;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;
import org.eclipse.cdt.core.dom.ast.c.ICASTArrayDesignator;
import org.eclipse.cdt.core.dom.ast.c.ICASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTDesignatedInitializer;
import org.eclipse.cdt.core.dom.ast.c.ICASTDesignator;
import org.eclipse.cdt.core.dom.ast.c.ICASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTFieldDesignator;
import org.eclipse.cdt.core.dom.ast.c.ICASTPointer;
import org.eclipse.cdt.core.dom.ast.c.ICASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTTypeIdInitializerExpression;
import org.eclipse.cdt.core.dom.ast.c.ICASTTypedefNameSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICFunctionScope;
import org.eclipse.cdt.core.dom.ast.c.ICScope;
import org.eclipse.cdt.core.dom.ast.gnu.IGNUASTCompoundStatementExpression;
import org.eclipse.cdt.core.dom.ast.gnu.c.ICASTKnRFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.gnu.c.IGCCASTArrayRangeDesignator;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPVisitor.CPPBaseVisitorAction;

/**
 * Created on Nov 5, 2004
 * @author aniefer
 */
public class CVisitor {
	public static abstract class CBaseVisitorAction {
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
		public boolean processDesignators    = false;
		/**
		 * @return true to continue visiting, abort to stop, skip to not descend into this node. 
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
        public int processDesignator( ICASTDesignator designator )  { return PROCESS_CONTINUE; }
	}
	
	public static class ClearBindingAction extends CBaseVisitorAction {
		{
			processNames = true;
		}
		public int processName(IASTName name) {
			if ( ((CASTName)name).hasBinding() ) {
				 ICScope scope;
                try {
                    scope = (ICScope)name.resolveBinding().getScope();
                    if ( scope != null )
    				 	scope.removeBinding(name.resolveBinding());
                } catch ( DOMException e ) {
                }
				 ((CASTName) name ).setBinding( null );
			}
			
			return PROCESS_CONTINUE;
		}
	}
	
	public static class CollectProblemsAction extends CBaseVisitorAction {
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

	public static class CollectDeclarationsAction extends CBaseVisitorAction {
		{
			processDeclarators = true;
			processDeclSpecifiers = true;
			processEnumerators = true;
			processStatements = true;
		}
		
		private static final int DEFAULT_CHILDREN_LIST_SIZE = 8;
		private IASTName[] declsFound = null;
		int numFound = 0;
		IBinding binding = null;
		boolean compositeTypeDeclared = false;
		
		private void addName(IASTName name) {
			if( declsFound.length == numFound ) // if the found array is full, then double the array
	        {
	            IASTName [] old = declsFound;
	            declsFound = new IASTName[ old.length * 2 ];
	            for( int j = 0; j < old.length; ++j )
	                declsFound[j] = old[j];
	        }
			declsFound[numFound++] = name;
		}
		
	    private IASTName[] removeNullFromNames() {
	    	if (declsFound[declsFound.length-1] != null) { // if the last element in the list is not null then return the list
				return declsFound;			
			} else if (declsFound[0] == null) { // if the first element in the list is null, then return empty list
				return new IASTName[0];
			}
			
			IASTName[] results = new IASTName[numFound];
			for (int i=0; i<results.length; i++)
				results[i] = declsFound[i];
				
			return results;
	    }
		
		public IASTName[] getDeclarationNames() {
			return removeNullFromNames();
		}
		
		public CollectDeclarationsAction(IBinding binding) {
			declsFound = new IASTName[DEFAULT_CHILDREN_LIST_SIZE];
			this.binding = binding;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.cdt.internal.core.dom.parser.c.CVisitor.CBaseVisitorAction#processDeclarator(org.eclipse.cdt.core.dom.ast.IASTDeclarator)
		 */
		public int processDeclarator(IASTDeclarator declarator) {
			//GCC allows declarations in expressions, so we have to continue from the 
			//declarator in case there is something in the initializer expression
			if ( declarator == null ) return PROCESS_CONTINUE;
			
			//if the binding is something not declared in a declarator, continue
			if( binding instanceof ICompositeType ) return PROCESS_CONTINUE;
			if( binding instanceof IEnumeration ) return PROCESS_CONTINUE;
			
			IASTNode parent = declarator.getParent();
			while (parent != null && !(parent instanceof IASTDeclaration))
				parent = parent.getParent();

			if ( parent instanceof IASTDeclaration ) {
				if ( parent != null && parent instanceof IASTFunctionDefinition ) {
					if ( declarator.getName() != null && declarator.getName().resolveBinding() == binding ) {
						addName(declarator.getName());
					}
				} else if ( parent instanceof IASTSimpleDeclaration ) {
					// prototype parameter with no identifier isn't a declaration of the K&R C parameter 
					if ( binding instanceof CKnRParameter && declarator.getName().toCharArray().length == 0 )
						return PROCESS_CONTINUE;
					
					if ( (declarator.getName() != null && declarator.getName().resolveBinding() == binding) ) {
						addName(declarator.getName());
					}
				} 
			} else if ( parent instanceof IASTParameterDeclaration && binding instanceof IParameter ) {
				if ( declarator.getName() != null && declarator.getName().resolveBinding() == binding ) {
					addName(declarator.getName());
				}
			}
			
			return PROCESS_CONTINUE;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.internal.core.dom.parser.c.CVisitor.CBaseVisitorAction#processDeclSpecifier(org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier)
		 */
		public int processDeclSpecifier(IASTDeclSpecifier declSpec) {
			if ( declSpec instanceof IASTSimpleDeclSpecifier ||	declSpec instanceof ICASTTypedefNameSpecifier ) 
				return PROCESS_CONTINUE;
			
			//if the binding isn't declared in a decl spec, skip it
			if( !(binding instanceof ICompositeType) &&	!(binding instanceof IEnumeration) )
				return PROCESS_CONTINUE;
			
			if ( !compositeTypeDeclared && declSpec != null && declSpec instanceof IASTCompositeTypeSpecifier ) {
				if (((IASTCompositeTypeSpecifier)declSpec).getName().resolveBinding() == binding) { 
					compositeTypeDeclared = true;
					addName(((IASTCompositeTypeSpecifier)declSpec).getName());
				}
			} else if (!compositeTypeDeclared && declSpec instanceof IASTElaboratedTypeSpecifier ) {
				if (((IASTElaboratedTypeSpecifier)declSpec).getName().resolveBinding() == binding) { 
					compositeTypeDeclared = true;
					addName(((IASTElaboratedTypeSpecifier)declSpec).getName());
				}
			} else if (!compositeTypeDeclared && declSpec instanceof IASTEnumerationSpecifier ) {
				if (((IASTEnumerationSpecifier)declSpec).getName().resolveBinding() == binding) {
					compositeTypeDeclared = true;
					addName(((IASTEnumerationSpecifier)declSpec).getName());
				}
			}
			
			return PROCESS_CONTINUE;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.cdt.internal.core.dom.parser.c.CVisitor.CBaseVisitorAction#processEnumerator(org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator)
		 */
		public int processEnumerator(IASTEnumerator enumerator) {
			if( binding instanceof IEnumerator && enumerator.getName().resolveBinding() == binding ){
				addName( enumerator.getName() );
			}
			
			return PROCESS_CONTINUE;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.cdt.internal.core.dom.parser.c.CVisitor.CBaseVisitorAction#processStatement(org.eclipse.cdt.core.dom.ast.IASTStatement)
		 */
		public int processStatement(IASTStatement statement) {
			if ( statement instanceof IASTLabelStatement && binding instanceof ILabel ){
				if ( ((IASTLabelStatement)statement).getName().resolveBinding() == binding ) 
					addName(((IASTLabelStatement)statement).getName());
				return PROCESS_SKIP;
			}

			return PROCESS_CONTINUE;
		}
	}

	public static class CollectReferencesAction extends CBaseVisitorAction {
		private static final int DEFAULT_LIST_SIZE = 8;
		private IASTName [] refs;
		private IBinding binding;
		private int idx = 0;
		private int kind;
		
		private static final int KIND_LABEL  = 1;
		private static final int KIND_OBJ_FN = 2;
		private static final int KIND_TYPE   = 3;
		
		
		public CollectReferencesAction( IBinding binding ){
			this.binding = binding;
			this.refs = new IASTName[ DEFAULT_LIST_SIZE ];
			
			processNames = true;
			if( binding instanceof ILabel )
				kind = KIND_LABEL;
			else if( binding instanceof ICompositeType || 
					 binding instanceof ITypedef || 
					 binding instanceof IEnumeration )
			{
				kind = KIND_TYPE;
			} else 
				kind = KIND_OBJ_FN;
		}
		
		public int processName( IASTName name ){
			ASTNodeProperty prop = name.getPropertyInParent();
			switch( kind ){
				case KIND_LABEL:
					if( prop == IASTGotoStatement.NAME )
						break;
					return PROCESS_CONTINUE;
				case KIND_TYPE:
					if( prop == IASTNamedTypeSpecifier.NAME )
						break;
					else if( prop == IASTElaboratedTypeSpecifier.TYPE_NAME ){
						IASTNode p = name.getParent().getParent();
						if( !(p instanceof IASTSimpleDeclaration) ||
							((IASTSimpleDeclaration)p).getDeclarators().length > 0 )
						{
							break;
						}
					}
					return PROCESS_CONTINUE;
				case KIND_OBJ_FN:
					if( prop == IASTIdExpression.ID_NAME || 
						prop == IASTFieldReference.FIELD_NAME || 
						prop == ICASTFieldDesignator.FIELD_NAME )
					{
						break;
					}
					return PROCESS_CONTINUE;
			}
			
			if( name.resolveBinding() == binding ){
				if( refs.length == idx ){
					IASTName [] temp = new IASTName[ refs.length * 2 ];
					System.arraycopy( refs, 0, temp, 0, refs.length );
					refs = temp;
				}
				refs[idx++] = name;
			}
			return PROCESS_CONTINUE;
		}
		public IASTName[] getReferences(){
			if( idx < refs.length ){
				IASTName [] temp = new IASTName[ idx ];
				System.arraycopy( refs, 0, temp, 0, idx );
				refs = temp;
			}
			return refs;
		}
	}
	
	//lookup bits
	private static final int COMPLETE = 0;		
	private static final int CURRENT_SCOPE = 1;
	private static final int TAGS = 2;
	
	//definition lookup start loc
	protected static final int AT_BEGINNING = 1;
	protected static final int AT_NEXT = 2; 

	static protected void createBinding( IASTName name ){
		IBinding binding = null;
		IASTNode parent = name.getParent();
		
		if( parent instanceof CASTIdExpression ){
			binding = resolveBinding( parent );
		} else if( parent instanceof ICASTTypedefNameSpecifier ){
			binding = resolveBinding( parent );
		} else if( parent instanceof IASTFieldReference ){
			binding = findBinding( (IASTFieldReference) parent );
		} else if( parent instanceof IASTDeclarator ){
			binding = createBinding( (IASTDeclarator) parent, name );
		} else if( parent instanceof ICASTCompositeTypeSpecifier ){
			binding = createBinding( (ICASTCompositeTypeSpecifier) parent );
		} else if( parent instanceof ICASTElaboratedTypeSpecifier ){
			binding = createBinding( (ICASTElaboratedTypeSpecifier) parent );
		} else if( parent instanceof IASTStatement ){
		    binding = createBinding ( (IASTStatement) parent );
		} else if( parent instanceof ICASTEnumerationSpecifier ){
		    binding = createBinding( (ICASTEnumerationSpecifier) parent );
		} else if( parent instanceof IASTEnumerator ) {
		    binding = createBinding( (IASTEnumerator) parent );
		} else if( parent instanceof ICASTFieldDesignator ) {
			binding = resolveBinding( parent );
		}
		((CASTName)name).setBinding( binding );
	}

	private static IBinding createBinding( ICASTEnumerationSpecifier enumeration ){
	    IEnumeration binding = new CEnumeration( enumeration );
	    try {
            ((ICScope)binding.getScope()).addBinding( binding );
        } catch ( DOMException e ) {
        }
        return binding; 
	}
	private static IBinding createBinding( IASTEnumerator enumerator ){
	    IEnumerator binding = new CEnumerator( enumerator ); 
	    try {
            ((ICScope)binding.getScope()).addBinding( binding );
        } catch ( DOMException e ) {
        }
	    return binding;
	}
	private static IBinding createBinding( IASTStatement statement ){
	    if( statement instanceof IASTGotoStatement ){
	        char [] gotoName = ((IASTGotoStatement)statement).getName().toCharArray();
	        IScope scope = getContainingScope( statement );
	        if( scope != null && scope instanceof ICFunctionScope ){
	            CFunctionScope functionScope = (CFunctionScope) scope;
	            List labels = functionScope.getLabels();
	            for( int i = 0; i < labels.size(); i++ ){
	                ILabel label = (ILabel) labels.get(i);
	                if( CharArrayUtils.equals( label.getNameCharArray(), gotoName) ){
	                    return label;
	                }
	            }
	            //label not found
	            return new CLabel.CLabelProblem( IProblemBinding.SEMANTIC_LABEL_STATEMENT_NOT_FOUND, gotoName );
	        }
	    } else if( statement instanceof IASTLabelStatement ){
	        IBinding binding = new CLabel( (IASTLabelStatement) statement );
	        try {
                ((ICFunctionScope) binding.getScope()).addBinding( binding );
            } catch ( DOMException e ) {
            }
	        return binding;
	    }
	    return null;
	}
	private static IBinding createBinding( ICASTElaboratedTypeSpecifier elabTypeSpec ){
		IASTNode parent = elabTypeSpec.getParent();
		if( parent instanceof IASTSimpleDeclaration ){
			IASTSimpleDeclaration declaration = (IASTSimpleDeclaration) parent;
			if( declaration.getDeclarators().length == 0 ){
				//forward declaration
				IBinding binding = resolveBinding( elabTypeSpec, CURRENT_SCOPE | TAGS );
				if( binding == null ){
					binding = new CStructure( elabTypeSpec );
					try {
                        ((ICScope) binding.getScope()).addBinding( binding );
                    } catch ( DOMException e ) {
                    }
				}
				return binding;
			} 
			return resolveBinding( elabTypeSpec, COMPLETE | TAGS );
		} else if( parent instanceof IASTTypeId || parent instanceof IASTParameterDeclaration ){
			IASTNode blockItem = getContainingBlockItem( parent );
			return findBinding( blockItem, elabTypeSpec.getName(), COMPLETE | TAGS );
		}
		return null;
	}
	private static IBinding findBinding( IASTFieldReference fieldReference ){
		IASTExpression fieldOwner = fieldReference.getFieldOwner();
		IType type = null;
		if( fieldOwner instanceof IASTArraySubscriptExpression ){
		    type = getExpressionType( ((IASTArraySubscriptExpression) fieldOwner).getArrayExpression() );
		} else {
		    type = getExpressionType( fieldOwner );
		}
	    while( type != null && type instanceof ITypeContainer) {
    		try {
                type = ((ITypeContainer)type).getType();
            } catch ( DOMException e ) {
                return e.getProblem();
            }
	    }
		
		if( type != null && type instanceof ICompositeType ){
			try {
                return ((ICompositeType) type).findField( fieldReference.getFieldName().toString() );
            } catch ( DOMException e ) {
                return e.getProblem();
            }
		}
		return null;
	}
	
	private static IType getExpressionType( IASTExpression expression ) {
	    try{ 
		    if( expression instanceof IASTIdExpression ){
		        IBinding binding = resolveBinding( expression );
				if( binding instanceof IVariable ){
					return ((IVariable)binding).getType();
				}
		    } else if( expression instanceof IASTCastExpression ){
		        IASTTypeId id = ((IASTCastExpression)expression).getTypeId();
				IBinding binding = resolveBinding( id );
				if( binding != null && binding instanceof IType ){
					return (IType) binding;
				}
		    } else if( expression instanceof IASTFieldReference ){ 
		        IBinding binding = ((IASTFieldReference)expression).getFieldName().resolveBinding();
			        
				if( binding instanceof IVariable ){
					return ((IVariable)binding).getType();
				}
		    }
		    else if( expression instanceof IASTUnaryExpression )
		    {
		       if( ((IASTUnaryExpression)expression).getOperator() == IASTUnaryExpression.op_bracketedPrimary )
		           return getExpressionType(((IASTUnaryExpression)expression).getOperand() );
		    }
	    } catch( DOMException e ){
	        return e.getProblem();
	    }
	    return null;
	}
	/**
	 * @param parent
	 * @return
	 */
	private static IBinding createBinding(IASTDeclarator declarator, IASTName name) {
		IBinding binding = null;
		IASTNode parent = declarator.getParent();
		if( declarator instanceof IASTStandardFunctionDeclarator ){
			binding = resolveBinding( parent, CURRENT_SCOPE );
			if( binding != null ) {
			    ((CFunction)binding).addDeclarator( (IASTStandardFunctionDeclarator) declarator );
			} else {
				binding = createBinding(declarator);
			}
		} else if( declarator instanceof ICASTKnRFunctionDeclarator ){
			if ( CharArrayUtils.equals(declarator.getName().toCharArray(), name.toCharArray()) ){
				binding = resolveBinding( parent, CURRENT_SCOPE );
				if( binding != null ) {
				    ((CFunction)binding).addDeclarator( (ICASTKnRFunctionDeclarator) declarator );
				} else { 
					binding = createBinding(declarator);
				}
			} else { // createBinding for one of the ICASTKnRFunctionDeclarator's parameterNames
			    IBinding f = declarator.getName().resolveBinding();
			    if( f instanceof CFunction ){
			        binding = ((CFunction) f).resolveParameter( name );
			    }

				if ( declarator.getParent() instanceof IASTFunctionDefinition ) {
					ICScope scope = (ICScope) ((IASTCompoundStatement)((IASTFunctionDefinition)declarator.getParent()).getBody()).getScope();
					if ( scope != null && binding != null )
                        try {
                            scope.addBinding(binding);
                        } catch ( DOMException e ) {
                        }
				}
			}
		} else if( parent instanceof IASTSimpleDeclaration ){
			binding = createBinding( (IASTSimpleDeclaration) parent, name );
		} else if( parent instanceof IASTParameterDeclaration ){
		    IASTParameterDeclaration param = (IASTParameterDeclaration) parent;
		    IASTFunctionDeclarator fDtor = (IASTFunctionDeclarator) param.getParent();
		    IBinding b = fDtor.getName().resolveBinding();
		    if( b instanceof IFunction ){
		        binding = ((CFunction)b).resolveParameter( name );
		        parent = fDtor.getParent();
		        if( parent instanceof IASTFunctionDefinition ){
		            ICScope scope = (ICScope) ((IASTCompoundStatement)((IASTFunctionDefinition)parent).getBody()).getScope();
					if ( scope != null && binding != null )
                        try {
                            scope.addBinding(binding);
                        } catch ( DOMException e ) {
                        }
				}
		    }
		} else if ( parent instanceof IASTFunctionDeclarator ) {
			binding = createBinding(declarator);
		}
		
		return binding;
	}

	private static IBinding createBinding( IASTDeclarator declarator ){
		IASTNode parent = declarator.getParent();

		if( declarator.getNestedDeclarator() != null )
			return createBinding( declarator.getNestedDeclarator() );
		
		while( parent instanceof IASTDeclarator ){
			parent = parent.getParent();
		}
		
		ICScope scope = (ICScope) getContainingScope( parent );
		IBinding binding = null;
		try {
            binding = ( scope != null ) ? scope.getBinding( ICScope.NAMESPACE_TYPE_OTHER, declarator.getName().toCharArray() ) : null;
        } catch ( DOMException e1 ) {
            binding = null;
        }  
		
		if( declarator instanceof IASTFunctionDeclarator ){
			if( binding != null && binding instanceof IFunction ){
			    try{ 
			        IFunction function = (IFunction) binding;
				    IFunctionType ftype = function.getType();
				    IType type = createType( declarator.getName() );
				    if( ftype.equals( type ) ){
				        if( parent instanceof IASTSimpleDeclaration )
				            ((CFunction)function).addDeclarator( (IASTFunctionDeclarator) declarator );
				        
				        return function;
				    }
			    } catch( DOMException e ){
			    }
			} 

			if( parent instanceof IASTSimpleDeclaration && ((IASTSimpleDeclaration) parent).getDeclSpecifier().getStorageClass() == IASTDeclSpecifier.sc_typedef)
				binding = new CTypeDef( declarator.getName() );
			else
				binding = new CFunction( (IASTFunctionDeclarator) declarator );
		} else if( parent instanceof IASTSimpleDeclaration ){
			IASTSimpleDeclaration simpleDecl = (IASTSimpleDeclaration) parent;			
			if( simpleDecl.getDeclSpecifier().getStorageClass() == IASTDeclSpecifier.sc_typedef ){
				binding = new CTypeDef( declarator.getName() );
			} else if( simpleDecl.getParent() instanceof ICASTCompositeTypeSpecifier ){
				binding = new CField( declarator.getName() );
			} else {
				binding = new CVariable( declarator.getName() );
			}
		} 

		if( scope != null && binding != null )
            try {
                scope.addBinding( binding );
            } catch ( DOMException e ) {
            }
		return binding;
	}

	
	private static IBinding createBinding( ICASTCompositeTypeSpecifier compositeTypeSpec ){
	    ICompositeType binding = new CStructure( compositeTypeSpec );
	    ICScope scope;
        try {
            scope = (ICScope) binding.getScope();
            scope.addBinding( binding );
        } catch ( DOMException e ) {
        }
        
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
			try {
                ((ICScope) binding.getScope()).addBinding( binding );
            } catch ( DOMException e ) {
            }
		} else if( simpleDeclaration.getParent() instanceof ICASTCompositeTypeSpecifier ){
			binding = new CField( name );
			try {
                ((ICScope) binding.getScope()).addBinding( binding );
            } catch ( DOMException e ) {
            }
		} else {
		    CScope scope = (CScope) CVisitor.getContainingScope( simpleDeclaration );
		    binding = scope.getBinding( ICScope.NAMESPACE_TYPE_OTHER, name.toCharArray() );
		    if( binding == null ){
		    	// if the simpleDeclaration is part of a KRC function declarator, then the binding is to a KRC parameter
		    	if ( simpleDeclaration.getParent() instanceof IASTFunctionDeclarator ) {
		    	    IASTFunctionDeclarator fdtor = (IASTFunctionDeclarator) simpleDeclaration.getParent();
		    	    IBinding fn = fdtor.getName().resolveBinding();
		    	    if( fn instanceof CFunction ){
		    	        binding = ((CFunction)fn).resolveParameter( name );
		    	    }
		    	} else {
			        binding = new CVariable( name );		    		
		    	}
		    	
		        scope.addBinding( binding );
		    }
		}
 
		return binding;
	}

	protected static IBinding resolveBinding( IASTNode node ){
		return resolveBinding( node, COMPLETE );
	}
	protected static IBinding resolveBinding( IASTNode node, int bits ){
		if( node instanceof IASTFunctionDefinition ){
			IASTFunctionDefinition functionDef = (IASTFunctionDefinition) node;
			IASTFunctionDeclarator functionDeclartor = functionDef.getDeclarator();
			IASTName name = functionDeclartor.getName();
			IASTNode blockItem = getContainingBlockItem( node );
			return findBinding( blockItem, name, bits );
		} else if( node instanceof IASTIdExpression ){
			IASTNode blockItem = getContainingBlockItem( node );
			return findBinding( blockItem, ((IASTIdExpression)node).getName(), bits );
		} else if( node instanceof ICASTTypedefNameSpecifier ){
			IASTNode blockItem = getContainingBlockItem( node );
			return findBinding( blockItem, ((ICASTTypedefNameSpecifier)node).getName(), bits );
		} else if( node instanceof ICASTElaboratedTypeSpecifier ){
			IASTNode blockItem = getContainingBlockItem( node );
			return findBinding( blockItem, ((ICASTElaboratedTypeSpecifier)node).getName(), bits );
		} else if( node instanceof ICASTCompositeTypeSpecifier ){
			IASTNode blockItem = getContainingBlockItem( node );
			return findBinding( blockItem, ((ICASTCompositeTypeSpecifier)node).getName(), bits );
		} else if( node instanceof IASTTypeId ){
			IASTTypeId typeId = (IASTTypeId) node;
			IASTDeclSpecifier declSpec = typeId.getDeclSpecifier();
			IASTName name = null;
			if( declSpec instanceof ICASTElaboratedTypeSpecifier ){
				name = ((ICASTElaboratedTypeSpecifier)declSpec).getName();
			} else if( declSpec instanceof ICASTCompositeTypeSpecifier ){
				name = ((ICASTCompositeTypeSpecifier)declSpec).getName();
			}
			if( name != null ){
				return name.resolveBinding();
			}
		} else if( node instanceof ICASTFieldDesignator ) {
			IASTNode blockItem = getContainingBlockItem( node );
			
			if ( (blockItem instanceof IASTSimpleDeclaration ||
					(blockItem instanceof IASTDeclarationStatement && ((IASTDeclarationStatement)blockItem).getDeclaration() instanceof IASTSimpleDeclaration)) ) {
				
				IASTSimpleDeclaration simpleDecl = null;
				if (blockItem instanceof IASTDeclarationStatement &&
					((IASTDeclarationStatement)blockItem).getDeclaration() instanceof IASTSimpleDeclaration )
					simpleDecl = (IASTSimpleDeclaration)((IASTDeclarationStatement)blockItem).getDeclaration();
				else if ( blockItem instanceof IASTSimpleDeclaration )
					simpleDecl = (IASTSimpleDeclaration)blockItem;
		
				if ( simpleDecl != null ) {
					IBinding struct = null;
					if ( simpleDecl.getDeclSpecifier() instanceof IASTNamedTypeSpecifier )
						struct = ((IASTNamedTypeSpecifier)simpleDecl.getDeclSpecifier()).getName().resolveBinding();
					else if ( simpleDecl.getDeclSpecifier() instanceof IASTElaboratedTypeSpecifier )
						struct = ((IASTElaboratedTypeSpecifier)simpleDecl.getDeclSpecifier()).getName().resolveBinding();
					else if ( simpleDecl.getDeclSpecifier() instanceof IASTCompositeTypeSpecifier )
						struct = ((IASTCompositeTypeSpecifier)simpleDecl.getDeclSpecifier()).getName().resolveBinding();
					
					if ( struct instanceof CStructure ) {
						return ((CStructure)struct).findField(((ICASTFieldDesignator)node).getName().toString());
					} else if ( struct instanceof ITypeContainer ) {
						IType type;
                        try {
                            type = ((ITypeContainer)struct).getType();
                            while ( type instanceof ITypeContainer && !(type instanceof CStructure) ) {
    							type = ((ITypeContainer)type).getType();
    						}
                        } catch ( DOMException e ) {
                            return e.getProblem();
                        }
                        
						
						if ( type instanceof CStructure )
							return ((CStructure)type).findField(((ICASTFieldDesignator)node).getName().toString());
					}
				}
			}
		}
		return null;
	}
	
	public static IScope getContainingScope( IASTNode node ){
	    if( node instanceof IASTDeclaration )
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
		} else if( parent instanceof IASTForStatement ){
		    return ((IASTForStatement)parent).getScope();
		} else if( parent instanceof IASTCompositeTypeSpecifier ){
		    return ((IASTCompositeTypeSpecifier)parent).getScope();
		} else if( parent instanceof ICASTKnRFunctionDeclarator ){
			parent = declaration.getParent();
			if( parent instanceof ICASTKnRFunctionDeclarator ){
				parent = ((IASTDeclarator)parent).getParent();
				if ( parent instanceof IASTFunctionDefinition ) {
					return ((IASTCompoundStatement)((IASTFunctionDefinition)parent).getBody()).getScope();
				}
			}
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
		    while( scope != null && !(scope instanceof ICFunctionScope) ){
		        try {
                    scope = scope.getParent();
                } catch ( DOMException e ) {
                    scope = e.getProblem();
                }
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
		if( parent instanceof IASTStandardFunctionDeclarator ){
			parent = ((IASTDeclarator)parent).getParent();
			if ( parent instanceof IASTFunctionDefinition ) {
				return ((IASTCompoundStatement)((IASTFunctionDefinition)parent).getBody()).getScope();
			}
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
	
	protected static IBinding findBinding( IASTNode blockItem, IASTName name, int bits ){
		IBinding binding = null;
		while( blockItem != null ){
			
			IASTNode parent = blockItem.getParent();
			IASTNode [] nodes = null;
			ICScope scope = null;
			if( parent instanceof IASTCompoundStatement ){
				IASTCompoundStatement compound = (IASTCompoundStatement) parent;
				nodes = compound.getStatements();
				scope = (ICScope) compound.getScope();
			} else if ( parent instanceof IASTTranslationUnit ){
				IASTTranslationUnit translation = (IASTTranslationUnit) parent;
				nodes = translation.getDeclarations();
				scope = (ICScope) translation.getScope();
			}
			
			boolean typesOnly = (bits & TAGS) != 0;
			
			if( scope != null ){
			    int namespaceType = typesOnly ? ICScope.NAMESPACE_TYPE_TAG : ICScope.NAMESPACE_TYPE_OTHER;
			    try {
                    binding = scope.getBinding( namespaceType, name.toCharArray() );
                } catch ( DOMException e ) {
                    binding = null;
                }
			    if( binding != null )
			        return binding;
			}
			
			if( nodes != null ){
				for( int i = 0; i < nodes.length; i++ ){
					IASTNode node = nodes[i];
					if( node == null || node == blockItem )
						break;
					if( node instanceof IASTDeclarationStatement ){
						IASTDeclarationStatement declStatement = (IASTDeclarationStatement) node;
						binding = checkForBinding( declStatement.getDeclaration(), name, typesOnly );
					} else if( node instanceof IASTDeclaration ){
						binding = checkForBinding( (IASTDeclaration) node, name, typesOnly );
					}
					if( binding != null ){
				        return binding;
					}
				}
			} else {
				//check the parent
				if( parent instanceof IASTDeclaration ){
					binding = checkForBinding( (IASTDeclaration) parent, name, typesOnly );
				} else if( parent instanceof IASTStatement ){
					binding = checkForBinding( (IASTStatement) parent, name, typesOnly );
				}
				if( binding != null ){
				    return binding;
				}
			}
			if( (bits & CURRENT_SCOPE) == 0 )
				blockItem = parent;
			else 
				blockItem = null;
			
			if( blockItem instanceof IASTTranslationUnit )
			    break;
		}
		
		if( blockItem != null)
		    return externalBinding( (IASTTranslationUnit) blockItem, name );
		
		return null;
	}
	
	private static IBinding externalBinding( IASTTranslationUnit tu, IASTName name ){
	    IASTNode parent = name.getParent();
	    IBinding external = null;
	    if( parent instanceof IASTIdExpression ){
	        if( parent.getPropertyInParent() == IASTFunctionCallExpression.FUNCTION_NAME ){
	            //external function
	            external = new CExternalFunction( tu, name );
	        } else {
	            //external variable
	            external = new CExternalVariable( tu, name );
	        }
	        ((CScope)tu.getScope()).addBinding( external );
	    }
	    return external;
	}
	
	private static IBinding checkForBinding( IASTDeclSpecifier declSpec, IASTName name, boolean typesOnly ){
		IASTName tempName = null;
		if( typesOnly ){
			if( declSpec instanceof ICASTElaboratedTypeSpecifier ){
				tempName = ((ICASTElaboratedTypeSpecifier)declSpec).getName();
				if( CharArrayUtils.equals( tempName.toCharArray(), name.toCharArray() ) ){
					return tempName.resolveBinding();
				}
			} else if( declSpec instanceof ICASTCompositeTypeSpecifier ){
				tempName = ((ICASTCompositeTypeSpecifier)declSpec).getName();
				if( CharArrayUtils.equals( tempName.toCharArray(), name.toCharArray() ) ){
					return tempName.resolveBinding();
				}
				//also have to check for any nested structs
				IASTDeclaration [] nested = ((ICASTCompositeTypeSpecifier)declSpec).getMembers();
				for( int i = 0; i < nested.length; i++ ){
					if( nested[i] instanceof IASTSimpleDeclaration ){
						IASTDeclSpecifier d = ((IASTSimpleDeclaration)nested[i]).getDeclSpecifier();
						if( d instanceof ICASTCompositeTypeSpecifier ) {
							IBinding temp = checkForBinding( d, name, typesOnly );
							if( temp != null )
								return temp;
						}
					}
				}
			} else if( declSpec instanceof ICASTEnumerationSpecifier ){
			    ICASTEnumerationSpecifier enumeration = (ICASTEnumerationSpecifier) declSpec;
			    tempName = enumeration.getName();
			    if( CharArrayUtils.equals( tempName.toCharArray(), name.toCharArray() ) ){
					return tempName.resolveBinding();
				}
			}
		} else {
			if( declSpec instanceof ICASTEnumerationSpecifier ) {
			    //check enumerators
			    IASTEnumerator [] list = ((ICASTEnumerationSpecifier) declSpec).getEnumerators();
			    for( int i = 0; i < list.length; i++ ) {
			        IASTEnumerator enumerator = list[i];
			        if( enumerator == null ) break;
			        tempName = enumerator.getName();
			        if( CharArrayUtils.equals( tempName.toCharArray(), name.toCharArray() ) ){
						return tempName.resolveBinding();
					}
			    }
			}
		}
		return null;
	}
	private static IBinding checkForBinding( IASTDeclaration declaration, IASTName name, boolean typesOnly ){
		IASTName tempName = null;
		if( declaration instanceof IASTSimpleDeclaration ){
			IASTSimpleDeclaration simpleDeclaration = (IASTSimpleDeclaration) declaration;
			
			if( !typesOnly ){
				IASTDeclarator [] declarators = simpleDeclaration.getDeclarators();
				for( int i = 0; i < declarators.length; i++ ){
					IASTDeclarator declarator = declarators[i];
					tempName = declarator.getName();
					if( CharArrayUtils.equals( tempName.toCharArray(), name.toCharArray() ) ){
						return tempName.resolveBinding();
					}
				}
			}
			return checkForBinding( simpleDeclaration.getDeclSpecifier(), name, typesOnly );
		} else if( !typesOnly && declaration instanceof IASTFunctionDefinition ){
			IASTFunctionDefinition functionDef = (IASTFunctionDefinition) declaration;
			
			if (functionDef.getDeclarator() instanceof IASTStandardFunctionDeclarator) {
				CASTFunctionDeclarator declarator = (CASTFunctionDeclarator) functionDef.getDeclarator();
				
				//check the function itself
				IASTName declName = declarator.getName();
				if( CharArrayUtils.equals( declName.toCharArray(), name.toCharArray() ) ){
					return declName.resolveBinding();
				}
				//check the parameters
				IASTParameterDeclaration []  parameters = declarator.getParameters();
				for( int i = 0; i < parameters.length; i++ ){
					IASTParameterDeclaration parameterDeclaration = parameters[i];
					if( parameterDeclaration == null ) break;
					declName = parameterDeclaration.getDeclarator().getName();
					if( CharArrayUtils.equals( declName.toCharArray(), name.toCharArray() ) ){
						return declName.resolveBinding();
					}
				}
			} else if (functionDef.getDeclarator() instanceof ICASTKnRFunctionDeclarator) {
				CASTKnRFunctionDeclarator declarator = (CASTKnRFunctionDeclarator) functionDef.getDeclarator();
				
				//check the function itself
				IASTName declName = declarator.getName();
				if( CharArrayUtils.equals( declName.toCharArray(), name.toCharArray() ) ){
					return declName.resolveBinding();
				}
				//check the parameters
				IASTDeclaration []  parameters = declarator.getParameterDeclarations();
				for( int i = 0; i < parameters.length; i++ ){
					IASTDeclaration parameterDeclaration = parameters[i];
					if( parameterDeclaration == null || !(parameters[i] instanceof IASTSimpleDeclaration)) break;
					IASTDeclarator[] parmDecltors = ((IASTSimpleDeclaration)parameters[i]).getDeclarators();
					for (int j=0; j<parmDecltors.length; j++) {
						declName = parmDecltors[j].getName();
						if( CharArrayUtils.equals( declName.toCharArray(), name.toCharArray() ) ){
							return declName.resolveBinding();
						}						
					}
				}
			}
		}
		return null;
	}
	
	private static IBinding checkForBinding( IASTStatement statement, IASTName name, boolean typesOnly ){
		if( statement instanceof IASTDeclarationStatement ){
			return checkForBinding( ((IASTDeclarationStatement)statement).getDeclaration(), name, typesOnly );
		} else if( statement instanceof IASTForStatement ){
			IASTForStatement forStatement = (IASTForStatement) statement;
			if( forStatement.getInitDeclaration() != null ){
				return checkForBinding( forStatement.getInitDeclaration(), name, typesOnly );
			}
		}
		return null;
	}
	
	protected static IASTDeclarator findDefinition( IASTDeclarator declarator, int beginAtLoc ){
	    return (IASTDeclarator) findDefinition( declarator, declarator.getName().toCharArray(), beginAtLoc );
	}
	protected static IASTFunctionDeclarator findDefinition( IASTFunctionDeclarator declarator ){
		return (IASTFunctionDeclarator) findDefinition( declarator, declarator.getName().toCharArray(), AT_NEXT );
	}
	protected static IASTDeclSpecifier findDefinition( ICASTElaboratedTypeSpecifier declSpec ){
		return (IASTDeclSpecifier) findDefinition(declSpec, declSpec.getName().toCharArray(), AT_BEGINNING);
	}

	private static IASTNode findDefinition(IASTNode decl, char [] declName, int beginAtLoc) {
		IASTNode blockItem = getContainingBlockItem( decl );
		IASTNode parent = blockItem.getParent();
		IASTNode [] list = null;
		if( parent instanceof IASTCompoundStatement ){
			IASTCompoundStatement compound = (IASTCompoundStatement) parent;
			list = compound.getStatements();
		} else if ( parent instanceof IASTTranslationUnit ){
			IASTTranslationUnit translation = (IASTTranslationUnit) parent;
			list = translation.getDeclarations();
		}
		boolean begun = ( beginAtLoc == AT_BEGINNING );
		if( list != null ){
			for( int i = 0; i < list.length; i++ ){
				IASTNode node = list[i];
				if( node == blockItem ){
				    begun = true;
					continue;
				}
				
				if( begun ) {
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
							if( CharArrayUtils.equals( compTypeSpec.getName().toCharArray(), declName ) ){
								return compTypeSpec;
							}
						}
					} else if( node instanceof IASTSimpleDeclaration && decl instanceof IASTDeclarator ){
					    IASTSimpleDeclaration simpleDecl = (IASTSimpleDeclaration) node;
					    IASTDeclarator [] dtors = simpleDecl.getDeclarators();
					    for( int j = 0; dtors != null && j < dtors.length; j++ ){
					        if( CharArrayUtils.equals( dtors[j].getName().toCharArray(), declName ) ){
					            return dtors[j];
					        }
					    }
					}
				}
			}
		}
		return null;
	}
	
	public static void clearBindings( IASTTranslationUnit tu ){
		visitTranslationUnit( tu, new ClearBindingAction() );
	}
	
	public static void visitTranslationUnit( IASTTranslationUnit tu, CBaseVisitorAction action ){
		IASTDeclaration[] decls = tu.getDeclarations();
		for( int i = 0; i < decls.length; i++ ){
			if( !visitDeclaration( decls[i], action ) ) return;
		}
	}
	
	public static boolean visitName( IASTName name, CBaseVisitorAction action ){
		if( action.processNames ) {
		    switch( action.processName( name ) ){
	            case CPPBaseVisitorAction.PROCESS_ABORT : return false;
	            case CPPBaseVisitorAction.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
		return true;
	}
	
	public static boolean visitDeclaration( IASTDeclaration declaration, CBaseVisitorAction action ){
		if( action.processDeclarations ) {
		    switch( action.processDeclaration( declaration ) ){
	            case CPPBaseVisitorAction.PROCESS_ABORT : return false;
	            case CPPBaseVisitorAction.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
		
		if( declaration instanceof IASTSimpleDeclaration ){
			IASTSimpleDeclaration simpleDecl = (IASTSimpleDeclaration) declaration;
			if( !visitDeclSpecifier( simpleDecl.getDeclSpecifier(), action ) ) return false;
			IASTDeclarator [] list = simpleDecl.getDeclarators();
			for( int i = 0; list != null && i < list.length; i++ ){
				if( !visitDeclarator( list[i], action ) ) return false;
			}
		} else if( declaration instanceof IASTFunctionDefinition ){
			IASTFunctionDefinition fnDef = (IASTFunctionDefinition) declaration;
			if( !visitDeclSpecifier( fnDef.getDeclSpecifier(), action ) ) return false;
			if( !visitDeclarator( fnDef.getDeclarator(), action ) ) return false;
			if( !visitStatement( fnDef.getBody(), action ) ) return false;
		}
		return true;
	}
	public static boolean visitDeclarator( IASTDeclarator declarator, CBaseVisitorAction action ){
		if( action.processDeclarators ){
			switch( action.processDeclarator( declarator ) ){
	            case CPPBaseVisitorAction.PROCESS_ABORT : return false;
	            case CPPBaseVisitorAction.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
		
		if( declarator.getPropertyInParent() != IASTTypeId.ABSTRACT_DECLARATOR ){
			if( !visitName( declarator.getName(), action ) ) return false;
		}
		
		if( declarator.getNestedDeclarator() != null )
			if( !visitDeclarator( declarator.getNestedDeclarator(), action ) ) return false;
		
		if( declarator.getInitializer() != null )
		    if( !visitInitializer( declarator.getInitializer(), action ) ) return false;
		
		if( declarator instanceof IASTStandardFunctionDeclarator ){
		    IASTParameterDeclaration [] list = ((IASTStandardFunctionDeclarator)declarator).getParameters();
			for( int i = 0; i < list.length; i++ ){
				if( !visitParameterDeclaration( list[i], action ) ) return false;
			}
		} else if ( declarator instanceof ICASTKnRFunctionDeclarator ) {
			ICASTKnRFunctionDeclarator knr = (ICASTKnRFunctionDeclarator) declarator;
			IASTName [] names = knr.getParameterNames();
			for( int i = 0; i < names.length; i++ ){
				if( !visitName( names[i], action ) ) return false;
			}
			
			IASTDeclaration[] parmDeclarations = knr.getParameterDeclarations();
			for( int i = 0; i < parmDeclarations.length; i++ ){
				if( !visitDeclaration( parmDeclarations[i], action ) ) return false;
			}
		}
		return true;
	}
	
	public static boolean visitInitializer( IASTInitializer initializer, CBaseVisitorAction action ){
	    if( initializer == null )
	        return true;
	    
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
	    } else if( initializer instanceof ICASTDesignatedInitializer ){
	        ICASTDesignatedInitializer dinit = (ICASTDesignatedInitializer) initializer;
	        ICASTDesignator [] ds = dinit.getDesignators();
	        for( int i = 0; i < ds.length; i++ ){
	            if( !visitDesignator( ds[i], action ) ) return false;
	        }
	        if( !visitInitializer( dinit.getOperandInitializer(), action ) ) return false;
	    }
	    return true;
	}
	public static boolean visitDesignator( ICASTDesignator designator, CBaseVisitorAction action ){
	    if( action.processDesignators ){
	    	switch( action.processDesignator( designator ) ){
		        case CPPBaseVisitorAction.PROCESS_ABORT : return false;
		        case CPPBaseVisitorAction.PROCESS_SKIP  : return true;
		        default : break;
		    }
	    }
	    if( designator instanceof ICASTFieldDesignator ){
	        if( !visitName( ((ICASTFieldDesignator)designator).getName(), action ) ) return false;
	    } else if( designator instanceof ICASTArrayDesignator ){
	        if( !visitExpression( ((ICASTArrayDesignator)designator).getSubscriptExpression(), action ) ) return false;
	    } else if( designator instanceof IGCCASTArrayRangeDesignator ){
	        if( !visitExpression( ((IGCCASTArrayRangeDesignator)designator).getRangeFloor(), action ) ) return false;
	        if( !visitExpression( ((IGCCASTArrayRangeDesignator)designator).getRangeCeiling(), action ) ) return false;
	    }
	    return true;
	}
	public static boolean visitParameterDeclaration( IASTParameterDeclaration parameterDeclaration, CBaseVisitorAction action ){
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
	
	public static boolean visitDeclSpecifier( IASTDeclSpecifier declSpec, CBaseVisitorAction action ){
		if( action.processDeclSpecifiers ){
	    	switch( action.processDeclSpecifier( declSpec ) ){
		        case CPPBaseVisitorAction.PROCESS_ABORT : return false;
		        case CPPBaseVisitorAction.PROCESS_SKIP  : return true;
		        default : break;
		    }
	    }
		
		if( declSpec instanceof ICASTCompositeTypeSpecifier ){
			ICASTCompositeTypeSpecifier compTypeSpec = (ICASTCompositeTypeSpecifier) declSpec;
			if( !visitName( compTypeSpec.getName(), action ) ) return false;
			
			IASTDeclaration [] list = compTypeSpec.getMembers();
			for( int i = 0; i < list.length; i++ ){
				if( !visitDeclaration( list[i], action ) ) return false;
			}
		} else if( declSpec instanceof ICASTElaboratedTypeSpecifier ){
			if( !visitName( ((ICASTElaboratedTypeSpecifier) declSpec).getName(), action ) ) return false;
		} else if( declSpec instanceof ICASTTypedefNameSpecifier ){
			if( !visitName( ((ICASTTypedefNameSpecifier) declSpec).getName(), action ) ) return false;
		} else if( declSpec instanceof ICASTEnumerationSpecifier ){
		    ICASTEnumerationSpecifier enumSpec = (ICASTEnumerationSpecifier) declSpec;
		    if( !visitName( enumSpec.getName(), action ) ) return false;
		    IASTEnumerator [] list = enumSpec.getEnumerators();
		    for( int i = 0; i < list.length; i++ ){
		        if( !visitEnumerator( list[i], action ) ) return false;
		    }
		}
		return true;
	}
	public static boolean visitEnumerator( IASTEnumerator enumerator, CBaseVisitorAction action ){
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
	
	public static boolean visitStatement( IASTStatement statement, CBaseVisitorAction action ){
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
		    if( ((IASTExpressionStatement)statement).getExpression() != null && !visitExpression( ((IASTExpressionStatement)statement).getExpression(), action ) ) return false;
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
		    if( ((IASTIfStatement) statement ).getElseClause() != null )
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
		}
		return true;
	}
	public static boolean visitTypeId( IASTTypeId typeId, CBaseVisitorAction action ){
		if( action.processTypeIds ){
		    switch( action.processTypeId( typeId ) ){
		        case CPPBaseVisitorAction.PROCESS_ABORT : return false;
		        case CPPBaseVisitorAction.PROCESS_SKIP  : return true;
		        default : break;
		    }
	    }
		
		if( !visitDeclarator( typeId.getAbstractDeclarator(), action ) ) return false;
		if( !visitDeclSpecifier( typeId.getDeclSpecifier(), action ) ) return false;
		return true;
	}
	public static boolean visitExpression( IASTExpression expression, CBaseVisitorAction action ){
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
		    if( ((IASTFunctionCallExpression)expression).getParameterExpression() != null && !visitExpression( ((IASTFunctionCallExpression)expression).getParameterExpression(), action ) ) return false;
		} else if( expression instanceof IASTIdExpression ){
		    if( !visitName( ((IASTIdExpression)expression).getName(), action ) ) return false;
		} else if( expression instanceof IASTTypeIdExpression ){
		    if( !visitTypeId( ((IASTTypeIdExpression)expression).getTypeId(), action ) ) return false;
		} else if( expression instanceof IASTCastExpression ){
		    if( !visitTypeId( ((IASTCastExpression)expression).getTypeId(), action ) ) return false;
		    if( !visitExpression( ((IASTCastExpression)expression).getOperand(), action ) ) return false;
		} else if( expression instanceof IASTUnaryExpression ){
		    if( !visitExpression( ((IASTUnaryExpression)expression).getOperand(), action ) ) return false;
		} else if( expression instanceof ICASTTypeIdInitializerExpression ){
		    if( !visitTypeId( ((ICASTTypeIdInitializerExpression)expression).getTypeId(), action ) ) return false;
		    if( !visitInitializer( ((ICASTTypeIdInitializerExpression)expression).getInitializer(), action ) ) return false;
		} else if( expression instanceof IGNUASTCompoundStatementExpression ){
		    if( !visitStatement( ((IGNUASTCompoundStatementExpression)expression).getCompoundStatement(), action ) ) return false;
		}
		return true;
	}
	
	/**
	 * Create an IType for an IASTName.
	 * 
	 * @param name the IASTName whose IType will be created
	 * @return the IType of the IASTName parameter
	 */
	public static IType createType(IASTName name) {
		return createType(name, false);
	}
	
	/**
	 * This method is used to create the structure of ITypes based on the structure of the IASTDeclarator for the IASTName pased in.
	 * 
	 * This method has recursive behaviour if the IASTDeclarator's parent is found to be an IASTFunctionDeclarator.
	 * Recursion is used to get the IType of the IASTDeclarator's parent.
	 * 
	 * If the IASTDeclarator's parent is an IASTSimpleDeclaration, an IASTFunctionDefinition, or an IASTParameterDeclaration then
	 * any recurisve behaviour to that point is stopped and the proper IType is returned at that point.  
	 * 
	 * @param name the IASTName to create an IType for
	 * @param isParm whether the IASTName is a parameter in a declaration or not
	 * @return the IType corresponding to the IASTName
	 */
	public static IType createType(IASTName name, boolean isParm) {
		// if it's not a declarator then return null, otherwise work with its parent
		if (!(name.getParent() instanceof IASTDeclarator)) return null;
		IASTDeclarator declarator = (IASTDeclarator) name.getParent();
		
		// if it's a simple declaration then create a base type and a function type if necessary and return it
		if (declarator.getParent() instanceof IASTSimpleDeclaration) {
			IType lastType = createBaseType( declarator, ((IASTSimpleDeclaration)declarator.getParent()).getDeclSpecifier(), isParm );
			
			if (declarator instanceof IASTFunctionDeclarator)
				lastType = new CFunctionType(lastType, getParmTypes((IASTFunctionDeclarator)declarator));
			
			return lastType;
			
		// if it's a function declarator then use recursion to get the parent's type
		} else if (declarator.getParent() instanceof IASTFunctionDeclarator) {
			IASTDeclarator origDecltor = (IASTDeclarator)declarator.getParent();
			IType lastType = createType(origDecltor.getName(), isParm); // use recursion to get the type of the IASTDeclarator's parent
			
			// if it was a function declarator and its parent is a function definition then do cleanup from the recursion here (setup pointers/arrays/and get functiontype)
			if (declarator.getParent().getParent() instanceof IASTFunctionDefinition) {
				if (declarator.getPointerOperators() != IASTDeclarator.EMPTY_DECLARATOR_ARRAY) 
					lastType = setupPointerChain(declarator.getPointerOperators(), lastType);
					
				IType arrayChain = setupArrayChain(declarator, lastType);
				if (arrayChain != null) lastType = arrayChain;
				
				lastType = new CFunctionType(lastType, getParmTypes((IASTFunctionDeclarator)declarator));
				
			// if it was a function declarator and its parent is not a function definition then do cleanup from the recursion here (setup pointers/arrays/ and check if need function type)
			} else {
				if (declarator.getPointerOperators() != IASTDeclarator.EMPTY_DECLARATOR_ARRAY) 
					lastType = setupPointerChain(declarator.getPointerOperators(), lastType);
					
				IType arrayChain = setupArrayChain(declarator, lastType);
				if (arrayChain != null) lastType = arrayChain;
				
				if (declarator instanceof IASTFunctionDeclarator)
					lastType = new CFunctionType(lastType, getParmTypes((IASTFunctionDeclarator)declarator));
			}
				
			return lastType;
			
		// if it's a function definition then create the base type and setup a function type if necessary 
		} else if (declarator.getParent() instanceof IASTFunctionDefinition) {
			IASTFunctionDefinition functionDef = (IASTFunctionDefinition)declarator.getParent();

			IType lastType = createBaseType(functionDef.getDeclarator(), functionDef.getDeclSpecifier(), isParm);
			
			if (declarator instanceof IASTFunctionDeclarator)
				lastType = new CFunctionType(lastType, getParmTypes((IASTFunctionDeclarator)declarator));
			
			return lastType;
			
		// if it's a parameter declaration then setup the base type, if necessary setup a function type and change it's return type to pointer to function returning T 
		} else if (declarator.getParent() instanceof IASTParameterDeclaration) {
			if (declarator instanceof IASTFunctionDeclarator) {		
				IType returnType = createBaseType(declarator, ((IASTParameterDeclaration)declarator.getParent()).getDeclSpecifier(), isParm);
				IType lastType = new CFunctionType(returnType, getParmTypes((IASTFunctionDeclarator)declarator));
					
				if (declarator.getPointerOperators() != IASTPointerOperator.EMPTY_ARRAY) 
					lastType = setupPointerChain(declarator.getPointerOperators(), lastType);
					
				IType arrayChain = setupArrayChain(declarator, lastType);
				if (arrayChain != null) lastType = arrayChain;

				// any parameter to type function returning T is adjusted to be pointer to function returning T
	            lastType = new CPointerType( lastType );
				
				return lastType;
			}
			
			return createBaseType( declarator, ((IASTParameterDeclaration)(declarator).getParent()).getDeclSpecifier(), isParm );
		}

		return null; // if anything else isn't supported yet return null for now
	}

	/**
	 * This is used to create a base IType corresponding to an IASTDeclarator and the IASTDeclSpecifier.  This method doesn't have any recursive
	 * behaviour and is used as the foundation of the ITypes being created.  
	 * The parameter isParm is used to specify whether the declarator is a parameter or not.  
	 * 
	 * @param declarator the IASTDeclarator whose base IType will be created 
	 * @param declSpec the IASTDeclSpecifier used to determine if the base type is a CQualifierType or not
	 * @param isParm is used to specify whether the IASTDeclarator is a parameter of a declaration
	 * @return the base IType
	 */
	public static IType createBaseType(IASTDeclarator declarator, IASTDeclSpecifier declSpec, boolean isParm) {
		IType lastType = null;
		
		if( declSpec instanceof ICASTTypedefNameSpecifier ){
			if (declSpec.isConst() || declSpec.isVolatile() || (declSpec instanceof ICASTDeclSpecifier && ((ICASTDeclSpecifier)declSpec).isRestrict()))
				return new CQualifierType(declSpec);
			
			ICASTTypedefNameSpecifier nameSpec = (ICASTTypedefNameSpecifier) declSpec;
			lastType = (IType) nameSpec.getName().resolveBinding();			
			
			IType pointerChain = setupPointerChain(declarator.getPointerOperators(), lastType);
			if (pointerChain != null) lastType = pointerChain;
			
			IType arrayChain = null;
			if (isParm)
				arrayChain = setupArrayParmChain(declarator, lastType);
			else
				arrayChain = setupArrayChain(declarator, lastType);
			if (arrayChain != null) lastType = arrayChain;
			
		} else if( declSpec instanceof IASTElaboratedTypeSpecifier ){
			if (declSpec.isConst() || declSpec.isVolatile() || (declSpec instanceof ICASTDeclSpecifier && ((ICASTDeclSpecifier)declSpec).isRestrict()))
				return new CQualifierType(declSpec);
			
			IASTElaboratedTypeSpecifier elabTypeSpec = (IASTElaboratedTypeSpecifier) declSpec;
			lastType = (IType) elabTypeSpec.getName().resolveBinding();
			
			IType pointerChain = setupPointerChain(declarator.getPointerOperators(), lastType);
			if (pointerChain != null) lastType = pointerChain;
			
			IType arrayChain = null;
			if (isParm)
				arrayChain = setupArrayParmChain(declarator, lastType);
			else
				arrayChain = setupArrayChain(declarator, lastType);
			if (arrayChain != null) lastType = arrayChain;
			
		} else if( declSpec instanceof IASTCompositeTypeSpecifier ){
			if (declSpec.isConst() || declSpec.isVolatile() || (declSpec instanceof ICASTDeclSpecifier && ((ICASTDeclSpecifier)declSpec).isRestrict()))
				return new CQualifierType(declSpec);
			
			IASTCompositeTypeSpecifier compTypeSpec = (IASTCompositeTypeSpecifier) declSpec;
			lastType = (IType) compTypeSpec.getName().resolveBinding();
			
			IType pointerChain = setupPointerChain(declarator.getPointerOperators(), lastType);
			if (pointerChain != null) lastType = pointerChain;
			
			IType arrayChain = null;
			if (isParm)
				arrayChain = setupArrayParmChain(declarator, lastType);
			else
				arrayChain = setupArrayChain(declarator, lastType);
			if (arrayChain != null) lastType = arrayChain;
			
		} else if (declSpec instanceof ICASTSimpleDeclSpecifier) {
			if (declSpec.isConst() || declSpec.isVolatile() || (declSpec instanceof ICASTDeclSpecifier && ((ICASTDeclSpecifier)declSpec).isRestrict()))
				lastType = new CQualifierType(declSpec);
			else						
				lastType = new CBasicType((ICASTSimpleDeclSpecifier)declSpec);
			
			IType pointerChain = setupPointerChain(declarator.getPointerOperators(), lastType);
			if (pointerChain != null) lastType = pointerChain;
			
			IType arrayChain = null;
			if (isParm)
				arrayChain = setupArrayParmChain(declarator, lastType);
			else
				arrayChain = setupArrayChain(declarator, lastType);
			if (arrayChain != null) lastType = arrayChain;
			
		}

		return lastType;
	}

	/**
	 * Returns an IType[] corresponding to the parameter types of the IASTFunctionDeclarator parameter.
	 * 
	 * @param decltor the IASTFunctionDeclarator to create an IType[] for its parameters
	 * @return IType[] corresponding to the IASTFunctionDeclarator parameters
	 */
	private static IType[] getParmTypes( IASTFunctionDeclarator decltor ){
		if ( decltor instanceof IASTStandardFunctionDeclarator ) {
			IASTParameterDeclaration parms[] = ((IASTStandardFunctionDeclarator)decltor).getParameters();
			IType parmTypes[] = new IType[parms.length];
			
		    for( int i = 0; i < parms.length; i++ ){
		    	parmTypes[i] = createType(parms[i].getDeclarator().getName(), true);
		    }
		    return parmTypes;
		} else if ( decltor instanceof ICASTKnRFunctionDeclarator ) {
			IASTDeclaration parms[] = ((ICASTKnRFunctionDeclarator)decltor).getParameterDeclarations();
			IType parmTypes[] = new IType[parms.length];
			
		    for( int i = 0; i < parms.length; i++ ){
		    	if ( parms[i] instanceof IASTSimpleDeclaration ) {
		    		IASTDeclarator[] decltors = ((IASTSimpleDeclaration)parms[i]).getDeclarators();
			    	for ( int j = 0; j < decltors.length; j++ ) {
				    	parmTypes[i] = createType(decltors[j].getName(), true);		    		
			    	}
		    	}
		    }
		    return parmTypes;
		} else {
			return null;
		}
	}

	/**
	 * Setup a chain of CQualifiedPointerType for the IASTArrayModifier[] of an IASTArrayDeclarator.
	 * The CQualifiedType is an IPointerType that is qualified with the IASTArrayModifier.
	 * i.e. the modifiers within the [ and ] of the array type
	 * 
	 * @param decl the IASTDeclarator that is a parameter and has the IASTArrayModifier[]
	 * @param lastType the IType that the end of the CQualifiedPointerType chain points to
	 * @return the starting CQualifiedPointerType at the beginning of the CQualifiedPointerType chain
	 */
	private static IType setupArrayParmChain(IASTDeclarator decl, IType lastType) {
		if (decl instanceof IASTArrayDeclarator) {
			int i=0;
			IASTArrayModifier[] mods = ((IASTArrayDeclarator)decl).getArrayModifiers();

			// C99: 6.7.5.3-7 "array of type" shall be adjusted to "qualified pointer to type", where the type qualifiers (if any)
			// are those specified within the [ and ] of the array type derivation
			IType pType = new CQualifiedPointerType(lastType, mods[i++]); 
			for (; i < ((IASTArrayDeclarator)decl).getArrayModifiers().length - 1; i++) {
				pType = new CQualifiedPointerType(lastType, mods[i]);
			}
			return pType;
		}
		
		return null;
	}
	
	/**
	 * Traverse through an array of IASTArrayModifier[] corresponding to the IASTDeclarator decl parameter.
	 * For each IASTArrayModifier in the array, create a corresponding CArrayType object and 
	 * link it in a chain.  The returned IType is the start of the CArrayType chain that represents
	 * the types of the IASTArrayModifier objects in the declarator.
	 * 
	 * @param decl the IASTDeclarator containing the IASTArrayModifier[] array to create a CArrayType chain for
	 * @param lastType the IType that the end of the CArrayType chain points to 
	 * @return the starting CArrayType at the beginning of the CArrayType chain
	 */
	private static IType setupArrayChain(IASTDeclarator decl, IType lastType) {
		if (decl instanceof IASTArrayDeclarator) {
			int i=0;
			IASTArrayModifier[] mods = ((IASTArrayDeclarator)decl).getArrayModifiers();
			
			CArrayType arrayType = new CArrayType(lastType); 
			if (mods[i] instanceof ICASTArrayModifier) {
				arrayType.setModifiedArrayModifier((ICASTArrayModifier)mods[i++]);
			}
			for (; i < ((IASTArrayDeclarator)decl).getArrayModifiers().length - 1; i++) {
				arrayType = new CArrayType(arrayType);
				if (mods[i] instanceof ICASTArrayModifier) {
					arrayType.setModifiedArrayModifier((ICASTArrayModifier)mods[i]);
				}
			}
			return arrayType;
		}
		
		return null;
	}

	/**
	 * Traverse through an array of IASTPointerOperator[] pointers and set up a pointer chain 
	 * corresponding to the types of the IASTPointerOperator[].
	 * 
	 * @param ptrs an array of IASTPointerOperator[] used to setup the pointer chain
	 * @param lastType the IType that the end of the CPointerType chain points to
	 * @return the starting CPointerType at the beginning of the CPointerType chain
	 */
	private static IType setupPointerChain(IASTPointerOperator[] ptrs, IType lastType) {
		CPointerType pointerType = null;
		
		if ( ptrs != null && ptrs.length > 0 ) {
			pointerType = new CPointerType();
											
			if (ptrs.length == 1) {
				pointerType.setType(lastType);
				pointerType.setPointer((ICASTPointer)ptrs[0]);
			} else {
				CPointerType tempType = new CPointerType();
				pointerType.setType(tempType);
				pointerType.setPointer((ICASTPointer)ptrs[ptrs.length - 1]);
				int i = ptrs.length - 2;
				for (; i > 0; i--) {
					tempType.setType(new CPointerType());
					tempType.setPointer((ICASTPointer)ptrs[i]);
					tempType = (CPointerType)tempType.getType();
				}					
				tempType.setType(lastType);
				tempType.setPointer((ICASTPointer)ptrs[i]);
			}
			
			return pointerType;
		}
		
		return null;
	}
	
	public static IASTProblem[] getProblems(IASTTranslationUnit tu) {
		CollectProblemsAction action = new CollectProblemsAction();
		visitTranslationUnit(tu, action);
		
		return action.getProblems();
	}
	
	public static IASTName[] getDeclarations(IASTTranslationUnit tu, IBinding binding) {
		CollectDeclarationsAction action = new CollectDeclarationsAction(binding);
		visitTranslationUnit(tu, action);

		return action.getDeclarationNames();
	}

	/**
	 * @param unit
	 * @param binding
	 * @return
	 */
	public static IASTName[] getReferences(IASTTranslationUnit tu, IBinding binding) {
		CollectReferencesAction action = new CollectReferencesAction( binding );
		visitTranslationUnit( tu, action );
		return action.getReferences();
	}
}
