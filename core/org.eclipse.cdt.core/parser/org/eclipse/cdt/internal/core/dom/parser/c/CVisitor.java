/*******************************************************************************
 * Copyright (c) 2005, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Niefer (IBM Rational Software) - Initial API and implementation 
 *     Markus Schorn (Wind River Systems)
 *     Bryan Wilkinson (QNX)
 *     Andrew Ferguson (Symbian)
 *     Jens Elmenthaler - http://bugs.eclipse.org/173458 (camel case completion)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTArrayDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTGotoStatement;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTLabelStatement;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTProblemHolder;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStandardFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IArrayType;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBasicType.Kind;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.ILabel;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.ISemanticProblem;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.c.ICASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTFieldDesignator;
import org.eclipse.cdt.core.dom.ast.c.ICASTPointer;
import org.eclipse.cdt.core.dom.ast.c.ICASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTTypedefNameSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICArrayType;
import org.eclipse.cdt.core.dom.ast.c.ICCompositeTypeScope;
import org.eclipse.cdt.core.dom.ast.c.ICFunctionScope;
import org.eclipse.cdt.core.dom.ast.gnu.c.ICASTKnRFunctionDeclarator;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexFileSet;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.CharArraySet;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.core.parser.util.IContentAssistMatcher;
import org.eclipse.cdt.internal.core.dom.parser.ASTInternal;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;
import org.eclipse.cdt.internal.core.dom.parser.IASTInternalScope;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;
import org.eclipse.cdt.internal.core.dom.parser.ProblemType;
import org.eclipse.cdt.internal.core.parser.util.ContentAssistMatcherFactory;

/**
 * Collection of methods to find information in an AST.
 */
public class CVisitor extends ASTQueries {
	/**
	 * 
	 */
	private static final CBasicType UNSIGNED_LONG_INT = new CBasicType(Kind.eInt, IBasicType.IS_LONG | IBasicType.IS_UNSIGNED);

	public static class CollectProblemsAction extends ASTVisitor {
		{
			shouldVisitDeclarations = true;
			shouldVisitExpressions = true;
			shouldVisitStatements = true;
			shouldVisitTypeIds = true;
		}
		
		private static final int DEFAULT_CHILDREN_LIST_SIZE = 8;
		private IASTProblem[] problems = null;
		int numFound = 0;

		public CollectProblemsAction() {
			problems = new IASTProblem[DEFAULT_CHILDREN_LIST_SIZE];
		}
		
		private void addProblem(IASTProblem problem) {
			if (problems.length == numFound) { // if the found array is full, then double the array
	            IASTProblem[] old = problems;
	            problems = new IASTProblem[old.length * 2];
	            for (int j = 0; j < old.length; ++j)
	                problems[j] = old[j];
	        }
			problems[numFound++] = problem;
		}
		
	    private IASTProblem[] removeNullFromProblems() {
	    	if (problems[problems.length - 1] != null) { // if the last element in the list is not null then return the list
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
		@Override
		public int visit(IASTDeclaration declaration) {
			if (declaration instanceof IASTProblemHolder)
				addProblem(((IASTProblemHolder) declaration).getProblem());

			return PROCESS_CONTINUE;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.cdt.internal.core.dom.parser.c.CVisitor.CBaseVisitorAction#processExpression(org.eclipse.cdt.core.dom.ast.IASTExpression)
		 */
		@Override
		public int visit(IASTExpression expression) {
			if (expression instanceof IASTProblemHolder)
				addProblem(((IASTProblemHolder) expression).getProblem());

			return PROCESS_CONTINUE;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.cdt.internal.core.dom.parser.c.CVisitor.CBaseVisitorAction#processStatement(org.eclipse.cdt.core.dom.ast.IASTStatement)
		 */
		@Override
		public int visit(IASTStatement statement) {
			if (statement instanceof IASTProblemHolder)
				addProblem(((IASTProblemHolder) statement).getProblem());

			return PROCESS_CONTINUE;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.cdt.internal.core.dom.parser.c.CVisitor.CBaseVisitorAction#processTypeId(org.eclipse.cdt.core.dom.ast.IASTTypeId)
		 */
		@Override
		public int visit(IASTTypeId typeId) {
			if (typeId instanceof IASTProblemHolder)
				addProblem(((IASTProblemHolder) typeId).getProblem());

			return PROCESS_CONTINUE;
		}
	}

	public static class CollectDeclarationsAction extends ASTVisitor {
		{
			shouldVisitDeclarators = true;
			shouldVisitDeclSpecifiers = true;
			shouldVisitEnumerators = true;
			shouldVisitStatements = true;
		}
		
		private static final int DEFAULT_CHILDREN_LIST_SIZE = 8;
		private IASTName[] declsFound = null;
		int numFound = 0;
		IBinding binding = null;
		boolean compositeTypeDeclared = false;
		
		private void addName(IASTName name) {
			if (declsFound.length == numFound) { // if the found array is full, then double the array
	            IASTName[] old = declsFound;
	            declsFound = new IASTName[old.length * 2];
	            for (int j = 0; j < old.length; ++j)
	                declsFound[j] = old[j];
	        }
			declsFound[numFound++] = name;
		}
		
	    private IASTName[] removeNullFromNames() {
	    	if (declsFound[declsFound.length - 1] != null) { // if the last element in the list is not null then return the list
				return declsFound;			
			} else if (declsFound[0] == null) { // if the first element in the list is null, then return empty list
				return new IASTName[0];
			}
			
			IASTName[] results = new IASTName[numFound];
			for (int i= 0; i < results.length; i++)
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
		@Override
		public int visit(IASTDeclarator declarator) {
			//GCC allows declarations in expressions, so we have to continue from the 
			//declarator in case there is something in the initializer expression
			if (declarator == null || declarator.getName() == null || declarator.getName().toCharArray().length == 0) return PROCESS_CONTINUE;
			
			//if the binding is something not declared in a declarator, continue
			if (binding instanceof ICompositeType)
				return PROCESS_CONTINUE;
			if (binding instanceof IEnumeration)
				return PROCESS_CONTINUE;
			
			IASTNode parent = declarator.getParent();
			while (parent != null && !(parent instanceof IASTDeclaration || parent instanceof IASTParameterDeclaration))
				parent = parent.getParent();

			if (parent instanceof IASTDeclaration) {
				if (parent instanceof IASTFunctionDefinition) {
					if (declarator.getName() != null && declarator.getName().resolveBinding() == binding) {
						addName(declarator.getName());
					}
				} else if (parent instanceof IASTSimpleDeclaration) {
					// prototype parameter with no identifier isn't a declaration of the K&R C parameter 
//					if (binding instanceof CKnRParameter && declarator.getName().toCharArray().length == 0)
//						return PROCESS_CONTINUE;
					
					if ((declarator.getName() != null && declarator.getName().resolveBinding() == binding)) {
						addName(declarator.getName());
					}
				} 
			} else if (parent instanceof IASTParameterDeclaration) {
				if (declarator.getName() != null && declarator.getName().resolveBinding() == binding) {
					addName(declarator.getName());
				}
			}
			
			return PROCESS_CONTINUE;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.internal.core.dom.parser.c.CVisitor.CBaseVisitorAction#processDeclSpecifier(org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier)
		 */
		@Override
		public int visit(IASTDeclSpecifier declSpec) {
			if (compositeTypeDeclared && declSpec instanceof ICASTTypedefNameSpecifier)  
				return PROCESS_CONTINUE;
			
			//if the binding isn't declared in a decl spec, skip it
			if (!(binding instanceof ICompositeType) &&	!(binding instanceof IEnumeration))
				return PROCESS_CONTINUE;
			
			if (binding instanceof ICompositeType && declSpec instanceof IASTCompositeTypeSpecifier) {
			    if (((IASTCompositeTypeSpecifier) declSpec).getName().resolveBinding() == binding) { 
					compositeTypeDeclared = true;
					addName(((IASTCompositeTypeSpecifier) declSpec).getName());
				}
			} else if (binding instanceof IEnumeration && declSpec instanceof IASTEnumerationSpecifier) {
				if (((IASTEnumerationSpecifier) declSpec).getName().resolveBinding() == binding) {
					compositeTypeDeclared = true;
					addName(((IASTEnumerationSpecifier) declSpec).getName());
				}
			} else if (declSpec instanceof IASTElaboratedTypeSpecifier) {
			    if (compositeTypeDeclared) {
			        IASTNode parent = declSpec.getParent();
			        if (!(parent instanceof IASTSimpleDeclaration) || ((IASTSimpleDeclaration) parent).getDeclarators().length > 0) {
			            return PROCESS_CONTINUE;
			        }
			    }
				if (((IASTElaboratedTypeSpecifier) declSpec).getName().resolveBinding() == binding) { 
					compositeTypeDeclared = true;
					addName(((IASTElaboratedTypeSpecifier) declSpec).getName());
				}
			}
			
			return PROCESS_CONTINUE;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.cdt.internal.core.dom.parser.c.CVisitor.CBaseVisitorAction#processEnumerator(org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator)
		 */
		@Override
		public int visit(IASTEnumerator enumerator) {
			if (binding instanceof IEnumerator && enumerator.getName().resolveBinding() == binding) {
				addName(enumerator.getName());
			}
			
			return PROCESS_CONTINUE;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.cdt.internal.core.dom.parser.c.CVisitor.CBaseVisitorAction#processStatement(org.eclipse.cdt.core.dom.ast.IASTStatement)
		 */
		@Override
		public int visit(IASTStatement statement) {
			if (statement instanceof IASTLabelStatement && binding instanceof ILabel) {
				if (((IASTLabelStatement) statement).getName().resolveBinding() == binding) 
					addName(((IASTLabelStatement) statement).getName());
				return PROCESS_SKIP;
			}

			return PROCESS_CONTINUE;
		}
	}

	public static class CollectReferencesAction extends ASTVisitor {
		private static final int DEFAULT_LIST_SIZE = 8;
		private IASTName[] refs;
		private IBinding binding;
		private int idx = 0;
		private int kind;
		
		private static final int KIND_LABEL  = 1;
		private static final int KIND_OBJ_FN = 2;
		private static final int KIND_TYPE   = 3;
		
		public CollectReferencesAction(IBinding binding) {
			this.binding = binding;
			this.refs = new IASTName[DEFAULT_LIST_SIZE];
			
			shouldVisitNames = true;
			if (binding instanceof ILabel) {
				kind = KIND_LABEL;
			} else if (binding instanceof ICompositeType || 
					 binding instanceof ITypedef || 
					 binding instanceof IEnumeration) {
				kind = KIND_TYPE;
			} else {
				kind = KIND_OBJ_FN;
			}
		}
		
		@Override
		public int visit(IASTName name) {
			ASTNodeProperty prop = name.getPropertyInParent();
			switch (kind) {
			case KIND_LABEL:
				if (prop == IASTGotoStatement.NAME)
					break;
				return PROCESS_CONTINUE;
			case KIND_TYPE:
				if (prop == IASTNamedTypeSpecifier.NAME) {
					break;
				} else if (prop == IASTElaboratedTypeSpecifier.TYPE_NAME) {
					IASTNode p = name.getParent().getParent();
					if (!(p instanceof IASTSimpleDeclaration) ||
							((IASTSimpleDeclaration) p).getDeclarators().length > 0) {
						break;
					}
				}
				return PROCESS_CONTINUE;
			case KIND_OBJ_FN:
				if (prop == IASTIdExpression.ID_NAME || 
						prop == IASTFieldReference.FIELD_NAME || 
						prop == ICASTFieldDesignator.FIELD_NAME) {
					break;
				}
				return PROCESS_CONTINUE;
			}
			
			if (CharArrayUtils.equals(name.toCharArray(), binding.getNameCharArray())) {
				if (sameBinding(name.resolveBinding(), binding)) {
					if (refs.length == idx) {
						IASTName[] temp = new IASTName[refs.length * 2];
						System.arraycopy(refs, 0, temp, 0, refs.length);
						refs = temp;
					}
					refs[idx++] = name;
				}
			}
			return PROCESS_CONTINUE;
		}
		
		private boolean sameBinding(IBinding binding1, IBinding binding2) {
			if (binding1 == binding2)
				return true;
			if (binding1 != null && binding1.equals(binding2))
				return true;
			return false;
		}

		public IASTName[] getReferences() {
			if (idx < refs.length) {
				IASTName[] temp = new IASTName[idx];
				System.arraycopy(refs, 0, temp, 0, idx);
				refs = temp;
			}
			return refs;
		}
	}
	
	protected static final ASTNodeProperty STRING_LOOKUP_PROPERTY = new ASTNodeProperty("CVisitor.STRING_LOOKUP_PROPERTY - STRING_LOOKUP"); //$NON-NLS-1$
	protected static final ASTNodeProperty STRING_LOOKUP_TAGS_PROPERTY = new ASTNodeProperty("CVisitor.STRING_LOOKUP_TAGS_PROPERTY - STRING_LOOKUP"); //$NON-NLS-1$
	private static final String SIZE_T = "size_t"; //$NON-NLS-1$
	private static final String PTRDIFF_T = "ptrdiff_t"; //$NON-NLS-1$
	public static final String EMPTY_STRING = ""; //$NON-NLS-1$
	public static final char[] EMPTY_CHAR_ARRAY = "".toCharArray(); //$NON-NLS-1$
	
	// definition lookup start location
	protected static final int AT_BEGINNING = 1;
	protected static final int AT_NEXT = 2; 

	static protected void createBinding(IASTName name) {
		IBinding binding = null;
		IASTNode parent = name.getParent();
		
		if (parent instanceof CASTIdExpression) {
			binding = resolveBinding(parent);
		} else if (parent instanceof ICASTTypedefNameSpecifier) {
			binding = resolveBinding(parent);
		} else if (parent instanceof IASTFieldReference) {
			binding = (IBinding) findBinding((IASTFieldReference) parent, false);
			if (binding == null) {
				binding = new ProblemBinding(name, IProblemBinding.SEMANTIC_NAME_NOT_FOUND, name.toCharArray());
			}
		} else if (parent instanceof IASTDeclarator) {
			binding = createBinding((IASTDeclarator) parent, name);
		} else if (parent instanceof ICASTCompositeTypeSpecifier) {
			binding = createBinding((ICASTCompositeTypeSpecifier) parent);
		} else if (parent instanceof ICASTElaboratedTypeSpecifier) {
			binding = createBinding((ICASTElaboratedTypeSpecifier) parent);
		} else if (parent instanceof IASTStatement) {
		    binding = createBinding ((IASTStatement) parent);
		} else if (parent instanceof ICASTEnumerationSpecifier) {
		    binding = createBinding((ICASTEnumerationSpecifier) parent);
		} else if (parent instanceof IASTEnumerator) {
		    binding = createBinding((IASTEnumerator) parent);
		} else if (parent instanceof ICASTFieldDesignator) {
			binding = resolveBinding(parent);
		}
		name.setBinding(binding);
	}

	private static IBinding createBinding(ICASTEnumerationSpecifier enumeration) {
	    IASTName name = enumeration.getName();
	    IScope scope =  getContainingScope(enumeration);
	    IBinding binding= null;
	    if (scope != null) {
	    	binding = scope.getBinding(name, false);
	    }
        if (binding != null && !(binding instanceof IIndexBinding) && name.isActive()) {
        	if (binding instanceof IEnumeration) {
            	if (binding instanceof CEnumeration) {
            	    ((CEnumeration) binding).addDefinition(name);
            	}
        	} else {
        		return new ProblemBinding(name, IProblemBinding.SEMANTIC_INVALID_OVERLOAD, name.toCharArray());	
        	}
	    } else {
	        binding = new CEnumeration(name);
	        ASTInternal.addName(scope, name);
	    } 
        return binding; 
	}

	private static IBinding createBinding(IASTEnumerator enumerator) {
	    IEnumerator binding = new CEnumerator(enumerator); 
	    try {
	    	ASTInternal.addName(binding.getScope(), enumerator.getName());
        } catch (DOMException e) {
        }
	    return binding;
	}

	private static IBinding createBinding(IASTStatement statement) {
	    if (statement instanceof IASTGotoStatement) {
	        char[] gotoName = ((IASTGotoStatement) statement).getName().toCharArray();
	        IScope scope = getContainingScope(statement);
	        if (scope != null && scope instanceof ICFunctionScope) {
	            CFunctionScope functionScope = (CFunctionScope) scope;
	            ILabel[] labels = functionScope.getLabels();
	            for (ILabel label : labels) {
	                if (CharArrayUtils.equals(label.getNameCharArray(), gotoName)) {
	                    return label;
	                }
	            }
	            //label not found
	            return new ProblemBinding(((IASTGotoStatement) statement).getName(), IProblemBinding.SEMANTIC_LABEL_STATEMENT_NOT_FOUND, gotoName);
	        }
	    } else if (statement instanceof IASTLabelStatement) {
	        IASTName name = ((IASTLabelStatement) statement).getName();
	        IBinding binding = new CLabel(name);
	        try {
	        	IScope scope = binding.getScope();
	        	if (scope instanceof ICFunctionScope)
	        		ASTInternal.addName(binding.getScope(), name);
            } catch (DOMException e) {
            }
	        return binding;
	    }
	    return null;
	}

	private static IBinding createBinding(ICASTElaboratedTypeSpecifier elabTypeSpec) {
		IASTNode parent = elabTypeSpec.getParent();
		IASTName name = elabTypeSpec.getName();
		if (parent instanceof IASTDeclaration) {
			IBinding binding= null;
			IScope insertIntoScope= null;
			if (parent instanceof IASTSimpleDeclaration 
					&& ((IASTSimpleDeclaration) parent).getDeclarators().length == 0) {
				IScope scope= getContainingScope(elabTypeSpec);
				try {
					while (scope instanceof ICCompositeTypeScope)
						scope= scope.getParent();

					binding= scope.getBinding(name, false);
				} catch (DOMException e) {
				}
				if (binding != null && name.isActive()) {
					if (binding instanceof CEnumeration) {
				        ((CEnumeration) binding).addDeclaration(name);
				    } else if (binding instanceof CStructure) {
				    	((CStructure) binding).addDeclaration(name);
				    }
				}
			} else {
				binding= resolveBinding(elabTypeSpec);
				if (binding == null) {
					insertIntoScope= elabTypeSpec.getTranslationUnit().getScope();
					binding= insertIntoScope.getBinding(name, false);
					if (binding != null && name.isActive()) {
						if (binding instanceof CEnumeration) {
					        ((CEnumeration) binding).addDeclaration(name);
					    } else if (binding instanceof CStructure) {
					    	((CStructure) binding).addDeclaration(name);
					    }
					}
				}
			}
			if (binding == null) {
				if (elabTypeSpec.getKind() == IASTElaboratedTypeSpecifier.k_enum) {
			        binding = new CEnumeration(name);
			    } else {
			        binding = new CStructure(name);    
			    }
				if (insertIntoScope != null) {
					ASTInternal.addName(insertIntoScope, name);
				}
			}
			return binding;
		} else if (parent instanceof IASTTypeId || parent instanceof IASTParameterDeclaration) {
			return resolveBinding(elabTypeSpec);
		}
		return null;
	}
	
	/**
	 * if prefix == false, return an IBinding or null
	 * if prefix == true, return an IBinding[] or null
	 * @param fieldReference
	 * @param prefix
	 * @return
	 */
	private static Object findBinding(IASTFieldReference fieldReference, boolean prefix) {
		IASTExpression fieldOwner = fieldReference.getFieldOwner();
		if (fieldOwner == null)
			return null;
		
		IType type = fieldOwner.getExpressionType();
	    while (type != null && type instanceof ITypeContainer) {
    		type = ((ITypeContainer) type).getType();
	    }
		
		if (type != null && type instanceof ICompositeType) {
			ICompositeType ct = (ICompositeType) type;
			if (ct instanceof IIndexBinding) {
				ct= ((CASTTranslationUnit) fieldReference.getTranslationUnit()).mapToASTType(ct);
			}
		    if (prefix) {
		        char[] p = fieldReference.getFieldName().toCharArray();
		        return findFieldsByPrefix(ct, p);
		    } 
			return ct.findField(fieldReference.getFieldName().toString());
		}
		return null;
	}

	public static IBinding[] findFieldsByPrefix(final ICompositeType ct, char[] p) {
		IBinding[] result = null;
		IContentAssistMatcher matcher = ContentAssistMatcherFactory.getInstance().createMatcher(p);
		IField[] fields = ct.getFields();
		for (IField field : fields) {
			if (matcher.match(field.getNameCharArray())) {
		        result = ArrayUtil.append(IBinding.class, result, field);
		    }
		}
		return ArrayUtil.trim(IBinding.class, result);
	}
	
	static IType getPtrDiffType(IASTBinaryExpression expr) {
		IScope scope = getContainingScope(expr);
		IBinding[] bs = scope.find(PTRDIFF_T);
		for (IBinding b : bs) {
			if (b instanceof IType) {
				if (!(b instanceof ICInternalBinding) || 
						CVisitor.declaredBefore(((ICInternalBinding) b).getPhysicalNode(), expr)) {
					return (IType) b;
				}
			}
		}

		return new CBasicType(Kind.eInt, 0, expr);
	}

	static IType get_SIZE_T(IASTExpression expr) {
		IASTTranslationUnit tu= expr.getTranslationUnit();
		if (tu != null) {
			IBinding[] bs = tu.getScope().find(SIZE_T);
			for (IBinding b : bs) {
				if (b instanceof IType) {
					if (!(b instanceof ICInternalBinding) || 
							CVisitor.declaredBefore(((ICInternalBinding) b).getPhysicalNode(), expr)) {
						return (IType) b;
					}
				}
			}
		}
		return UNSIGNED_LONG_INT;
	}

	static IType unwrapTypedefs(IType type) {
		while (type instanceof ITypedef) {
			type= ((ITypedef) type).getType();
		}
		return type;
	}

	/**
	 * @param parent
	 * @return
	 */
	private static IBinding createBinding(IASTDeclarator declarator, IASTName name) {
		IBinding binding = null;
		if (declarator instanceof ICASTKnRFunctionDeclarator) {
			if (CharArrayUtils.equals(declarator.getName().toCharArray(), name.toCharArray())) {
				IScope scope= CVisitor.getContainingScope(declarator);
				binding = scope.getBinding(name, false);
				if (binding != null && !(binding instanceof IIndexBinding) && name.isActive()) {
				    if (binding instanceof ICInternalFunction) {
				        ((ICInternalFunction) binding).addDeclarator(declarator);
				    } else {
				        binding = new ProblemBinding(name, IProblemBinding.SEMANTIC_INVALID_OVERLOAD, name.toCharArray());
				    }
				} else { 
					binding = createBinding(declarator);
				}
			} else { // createBinding for one of the ICASTKnRFunctionDeclarator's parameterNames
			    IBinding f = declarator.getName().resolveBinding();
			    if (f instanceof CFunction) {
			        binding = ((CFunction) f).resolveParameter(name);
			    }
			}
		} else {
		    binding = createBinding(declarator);
		}
		return binding;
	}

	private static IBinding createBinding(IASTDeclarator declarator) {
		IASTNode parent = ASTQueries.findOutermostDeclarator(declarator).getParent();
		declarator= ASTQueries.findInnermostDeclarator(declarator);		
		IASTDeclarator typeRelevant= ASTQueries.findTypeRelevantDeclarator(declarator);
		
		IScope scope= getContainingScope(parent);
		ASTNodeProperty prop = parent.getPropertyInParent();
		if (prop == IASTDeclarationStatement.DECLARATION) {
		    //implicit scope, see 6.8.4-3
		    prop = parent.getParent().getPropertyInParent();
		    if (prop != IASTCompoundStatement.NESTED_STATEMENT)
		    	scope = null;
		}
		
		IASTName name = declarator.getName();
		
		IBinding binding = (scope != null) ? scope.getBinding(name, false) : null;  
        
        boolean isFunction= false;
        if (parent instanceof IASTParameterDeclaration || parent.getPropertyInParent() == ICASTKnRFunctionDeclarator.FUNCTION_PARAMETER) {
        	IASTDeclarator fdtor = (IASTDeclarator) parent.getParent();
        	if (ASTQueries.findTypeRelevantDeclarator(fdtor) instanceof IASTFunctionDeclarator) {
        		IASTName n= ASTQueries.findInnermostDeclarator(fdtor).getName();
        		IBinding temp = n.resolveBinding();
        		if (temp != null && temp instanceof CFunction) {
        			binding = ((CFunction) temp).resolveParameter(name);
        		} else if (temp instanceof IFunction) {
        			//problems with the function, still create binding for the parameter
        			binding = new CParameter(name);
        		}
        		return binding;
        	}
		} else if (parent instanceof IASTFunctionDefinition) {
			isFunction= true;
		} else if (parent instanceof IASTSimpleDeclaration) {
			IASTSimpleDeclaration simpleDecl = (IASTSimpleDeclaration) parent;			
			if (simpleDecl.getDeclSpecifier().getStorageClass() == IASTDeclSpecifier.sc_typedef) {
				binding = new CTypedef(name);
			} else {
				isFunction= typeRelevant instanceof IASTFunctionDeclarator;
				if (!isFunction) { 
					IType t1 = createType(declarator), t2 = null;
					if (CVisitor.unwrapTypedefs(t1) instanceof IFunctionType) {
						isFunction= true;
					} else {
						if (binding != null && !(binding instanceof IIndexBinding) && name.isActive()) {
							if (binding instanceof IParameter) {
								return new ProblemBinding(name, IProblemBinding.SEMANTIC_INVALID_REDECLARATION, name.toCharArray());
							} else if (binding instanceof IVariable) {
								t2 = ((IVariable) binding).getType();
								if (t1 != null && t2 != null && (
										t1.isSameType(t2) || isCompatibleArray(t1, t2) != null)) {
									if (binding instanceof CVariable)
										((CVariable) binding).addDeclaration(name);
								} else {
									return new ProblemBinding(name, IProblemBinding.SEMANTIC_INVALID_REDECLARATION, name.toCharArray());
								}
							}
						} else if (simpleDecl.getParent() instanceof ICASTCompositeTypeSpecifier) {
							binding = new CField(name);
						} else {
							binding = new CVariable(name);
						}
					}
				}
			}
		}
		if (isFunction) {
			if (binding != null && !(binding instanceof IIndexBinding) && name.isActive()) {
				if (binding instanceof IFunction) {
					IFunction function = (IFunction) binding;
					if (function instanceof CFunction) {
						((CFunction) function).addDeclarator(typeRelevant);
					}
					return function;
				}
				binding = new ProblemBinding(name, IProblemBinding.SEMANTIC_INVALID_OVERLOAD, name.toCharArray());
			} else {
				binding = new CFunction(typeRelevant);
			}

		}
		return binding;
	}

	private static IBinding createBinding(ICASTCompositeTypeSpecifier compositeTypeSpec) {
		IScope scope = null;
		IBinding binding = null;
		IASTName name = compositeTypeSpec.getName();
		try {
			scope =  getContainingScope(compositeTypeSpec);
			while (scope instanceof ICCompositeTypeScope)
				scope =  scope.getParent();
				
			if (scope != null) {
				binding = scope.getBinding(name, false);
				if (binding != null && !(binding instanceof IIndexBinding) && name.isActive()) {
					if (binding instanceof CStructure)
						((CStructure) binding).addDefinition(compositeTypeSpec);
					return binding;
				}
			}
		} catch (DOMException e) {
		}
	    return new CStructure(name);
	}
	
	protected static IBinding resolveBinding(IASTNode node) {
		if (node instanceof IASTFunctionDefinition) {
			IASTFunctionDefinition functionDef = (IASTFunctionDefinition) node;
			IASTFunctionDeclarator functionDeclartor = functionDef.getDeclarator();
			IASTName name = findInnermostDeclarator(functionDeclartor).getName();
			IScope scope = getContainingScope(node);
			return lookup(scope, name);
		} else if (node instanceof IASTIdExpression) {
			IScope scope = getContainingScope(node);
			IBinding binding = lookup(scope, ((IASTIdExpression) node).getName());
			if (binding instanceof IType && !(binding instanceof IProblemBinding) ) {
				return new ProblemBinding(node, IProblemBinding.SEMANTIC_INVALID_TYPE,
						binding.getNameCharArray(), new IBinding[] { binding });
			}
			return binding;
		} else if (node instanceof ICASTTypedefNameSpecifier) {
			IScope scope = getContainingScope(node);
			IASTName name= ((ICASTTypedefNameSpecifier) node).getName();
			IBinding binding = lookup(scope, name);
			if (binding == null)
				return new ProblemBinding(node, IProblemBinding.SEMANTIC_NAME_NOT_FOUND, name.toCharArray());
			if (binding instanceof IType)
				return binding;
			return new ProblemBinding(node, IProblemBinding.SEMANTIC_INVALID_TYPE, binding.getNameCharArray(),
					new IBinding[] { binding });
		} else if (node instanceof ICASTElaboratedTypeSpecifier) {
			IScope scope = getContainingScope(node);
			return lookup(scope, ((ICASTElaboratedTypeSpecifier) node).getName());
		} else if (node instanceof ICASTCompositeTypeSpecifier) {
			IScope scope = getContainingScope(node);
			return lookup(scope, ((ICASTCompositeTypeSpecifier) node).getName());
		} else if (node instanceof IASTTypeId) {
			IASTTypeId typeId = (IASTTypeId) node;
			IASTDeclSpecifier declSpec = typeId.getDeclSpecifier();
			IASTName name = null;
			if (declSpec instanceof ICASTElaboratedTypeSpecifier) {
				name = ((ICASTElaboratedTypeSpecifier) declSpec).getName();
			} else if (declSpec instanceof ICASTCompositeTypeSpecifier) {
				name = ((ICASTCompositeTypeSpecifier) declSpec).getName();
			} else if (declSpec instanceof ICASTTypedefNameSpecifier) {
				name = ((ICASTTypedefNameSpecifier) declSpec).getName();
			}
			if (name != null) {
				IBinding binding = name.resolveBinding();
				if (binding instanceof IType) {
					return binding;
				} else if (binding != null) {
					return new ProblemBinding(node, IProblemBinding.SEMANTIC_INVALID_TYPE,
							binding.getNameCharArray(), new IBinding[] { binding });
                }
				return null;
			}
		} else if (node instanceof ICASTFieldDesignator) {
			IASTNode blockItem = getContainingBlockItem(node);
			
			if ((blockItem instanceof IASTSimpleDeclaration ||
					(blockItem instanceof IASTDeclarationStatement && ((IASTDeclarationStatement) blockItem).getDeclaration() instanceof IASTSimpleDeclaration))) {
				IASTSimpleDeclaration simpleDecl = null;
				if (blockItem instanceof IASTDeclarationStatement &&
						((IASTDeclarationStatement) blockItem).getDeclaration() instanceof IASTSimpleDeclaration) {
					simpleDecl = (IASTSimpleDeclaration)((IASTDeclarationStatement) blockItem).getDeclaration();
				} else if (blockItem instanceof IASTSimpleDeclaration) {
					simpleDecl = (IASTSimpleDeclaration) blockItem;
				}
		
				if (simpleDecl != null) {
					IBinding struct = null;
					if (simpleDecl.getDeclSpecifier() instanceof IASTNamedTypeSpecifier)
						struct = ((IASTNamedTypeSpecifier) simpleDecl.getDeclSpecifier()).getName().resolveBinding();
					else if (simpleDecl.getDeclSpecifier() instanceof IASTElaboratedTypeSpecifier)
						struct = ((IASTElaboratedTypeSpecifier) simpleDecl.getDeclSpecifier()).getName().resolveBinding();
					else if (simpleDecl.getDeclSpecifier() instanceof IASTCompositeTypeSpecifier)
						struct = ((IASTCompositeTypeSpecifier) simpleDecl.getDeclSpecifier()).getName().resolveBinding();
					
					if (struct instanceof ICompositeType) {
						return ((ICompositeType) struct).findField(((ICASTFieldDesignator) node).getName().toString());
					} else if (struct instanceof ITypeContainer) {
						IType type;
                        type = ((ITypeContainer) struct).getType();
						while (type instanceof ITypeContainer && !(type instanceof CStructure)) {
							type = ((ITypeContainer) type).getType();
						}

						if (type instanceof CStructure)
							return ((CStructure) type).findField(((ICASTFieldDesignator) node).getName().toString());
					}
				}
			}
		}
		return null;
	}
	
	/**
	 * May return <code>null</code>, e.g. for parameter names in function-prototypes.
	 */
	public static IScope getContainingScope(IASTNode node) {
	    if (node == null)
			return null;
		while (node != null) {
		    if (node instanceof IASTDeclaration) {
				IASTNode parent = node.getParent();
				if (parent instanceof IASTTranslationUnit) {
					return ((IASTTranslationUnit) parent).getScope();
				} else if (parent instanceof IASTDeclarationStatement) {
					return getContainingScope((IASTStatement) parent);
				} else if (parent instanceof IASTForStatement) {
				    return ((IASTForStatement) parent).getScope();
				} else if (parent instanceof IASTCompositeTypeSpecifier) {
				    return ((IASTCompositeTypeSpecifier) parent).getScope();
				} else if (parent instanceof ICASTKnRFunctionDeclarator) {
					parent = ((IASTDeclarator) parent).getParent();
					if (parent instanceof IASTFunctionDefinition) {
						return ((IASTCompoundStatement)((IASTFunctionDefinition) parent).getBody()).getScope();
					}
				}
		    } else if (node instanceof IASTStatement) {
		        return getContainingScope((IASTStatement) node);
		    } else if (node instanceof IASTExpression) {
				IASTNode parent = node.getParent();
				if (parent instanceof IASTForStatement) {
				    return ((IASTForStatement) parent).getScope();
				} 
		    } else if (node instanceof IASTParameterDeclaration) {
				IASTNode parent = node.getParent();
				if (parent instanceof IASTStandardFunctionDeclarator) {					
					IASTStandardFunctionDeclarator dtor = (IASTStandardFunctionDeclarator) parent;
					if (ASTQueries.findTypeRelevantDeclarator(dtor) == dtor) {
						parent= ASTQueries.findOutermostDeclarator(dtor);
						ASTNodeProperty prop = parent.getPropertyInParent();
						if (prop == IASTSimpleDeclaration.DECLARATOR)
						    return dtor.getFunctionScope();
						else if (prop == IASTFunctionDefinition.DECLARATOR)
						    return ((IASTCompoundStatement) ((IASTFunctionDefinition) parent.getParent()).getBody()).getScope();
					}
				}
		    } else if (node instanceof IASTEnumerator) {
		        //put the enumerators in the same scope as the enumeration
		        node = node.getParent();
		    } else if (node instanceof IASTName) {
		    	ASTNodeProperty prop = node.getPropertyInParent();
		    	if (prop == IASTLabelStatement.NAME) {
		    		IScope scope= getContainingScope(node.getParent());
				    //labels have function scope
				    while (scope != null && !(scope instanceof ICFunctionScope)) {
				        try {
		                    scope = scope.getParent();
		                } catch (DOMException e) {
		                    scope = e.getProblem();
		                    break;
		                }
				    }
				    return scope;
		    	}
		    }
		    
		    node = node.getParent();
		}
	    return null;
	}
	
	public static IScope getContainingScope(IASTStatement statement) {
		IASTNode parent = statement.getParent();
		IScope scope = null;
		if (parent instanceof IASTCompoundStatement) {
		    IASTCompoundStatement compound = (IASTCompoundStatement) parent;
		    scope = compound.getScope();
		} else if (parent instanceof IASTStatement) {
			if (parent instanceof IASTForStatement) {
				scope= ((IASTForStatement) parent).getScope();
			} else {
				scope = getContainingScope((IASTStatement) parent);
			}
		} else if (parent instanceof IASTFunctionDefinition) {
			return ((IASTFunctionDefinition) parent).getScope();
		} else {
			return getContainingScope(parent);
		}
		
		if (statement instanceof IASTGotoStatement) {
		    // labels have function scope
		    while (scope != null && !(scope instanceof ICFunctionScope)) {
		        try {
                    scope = scope.getParent();
                } catch (DOMException e) {
                    scope = e.getProblem();
                    break;
                }
		    }
		}
		
		return scope;
	}
	
	private static IASTNode getContainingBlockItem(IASTNode node) {
		IASTNode parent = node.getParent();
		if (parent instanceof IASTDeclaration) {
			IASTNode p = parent.getParent();
			if (p instanceof IASTDeclarationStatement)
				return p;
			return parent;
		} else if (parent instanceof IASTCompoundStatement || // parent is something that can contain a declaration 
				parent instanceof IASTTranslationUnit   ||
				parent instanceof IASTForStatement  ||
				parent instanceof IASTFunctionDeclarator) {
			return node;
		}
		
		return getContainingBlockItem(parent);
	}
	
	/**
	 * Lookup for a name starting from the given scope.
	 */
	protected static IBinding lookup(IScope scope, IASTName name) {
		if (scope == null)
			return null;
		
		IIndexFileSet fileSet= IIndexFileSet.EMPTY;
		IASTTranslationUnit tu= name.getTranslationUnit();
		if (tu == null && scope instanceof IASTInternalScope) {
			tu= ((IASTInternalScope) scope).getPhysicalNode().getTranslationUnit();
		}
		if (tu != null) {
			final IIndexFileSet fs= (IIndexFileSet) tu.getAdapter(IIndexFileSet.class);
			if (fs != null) {
				fileSet= fs;
			}
		}
		
		while (scope != null) {
			if (!(scope instanceof ICCompositeTypeScope)) {
				IBinding binding = scope.getBinding(name, true, fileSet);
				if (binding != null)
					return binding;
			}
			try {
				scope= scope.getParent();
			} catch (DOMException e) {
				scope= null;
			}
		}
		
		return externalBinding(tu, name);
	}
	
	/**
	 * if (bits & PREFIX_LOOKUP) then returns IBinding[]
	 * otherwise returns IBinding
	 */
	protected static IBinding[] lookupPrefix(IScope scope, IASTName name) throws DOMException{
		if (scope == null)
			return null;
		
		IIndexFileSet fileSet= IIndexFileSet.EMPTY;
		IASTTranslationUnit tu= name.getTranslationUnit();
		if (tu == null && scope instanceof IASTInternalScope) {
			tu= ((IASTInternalScope) scope).getPhysicalNode().getTranslationUnit();
		}
		if (tu != null) {
			final IIndexFileSet fs= (IIndexFileSet) tu.getAdapter(IIndexFileSet.class);
			if (fs != null) {
				fileSet= fs;
			}
		}
		
		IBinding[] result = null;
		CharArraySet handled= new CharArraySet(1);
		while (scope != null) {
			if (!(scope instanceof ICCompositeTypeScope)) {
				IBinding[] bindings= scope.getBindings(name, true, true, fileSet);
				for (IBinding b : bindings) {
					final char[] n= b.getNameCharArray();
					// consider binding only if no binding with the same name was found in another scope.
					if (!handled.containsKey(n)) {
						result= ArrayUtil.append(IBinding.class, result, b);
					}
				}
				// store names of bindings
				for (IBinding b : bindings) {
					final char[] n= b.getNameCharArray();
					handled.put(n);
				}
			}
			scope= scope.getParent();
		}
		
		return ArrayUtil.trim(IBinding.class, result);
	}

	private static IBinding externalBinding(IASTTranslationUnit tu, IASTName name) {
	    IASTNode parent = name.getParent();
	    IBinding external = null;
	    if (parent instanceof IASTIdExpression) {
	        if (parent.getPropertyInParent() == IASTFunctionCallExpression.FUNCTION_NAME) {
	            //external function
	            external = new CExternalFunction(tu, name);
	            ASTInternal.addName(tu.getScope(), name);
	        } else {
	            //external variable
	            //external = new CExternalVariable(tu, name);
       	        //((CScope) tu.getScope()).addName(name);
	        	external = new ProblemBinding(name, IProblemBinding.SEMANTIC_NAME_NOT_FOUND, name.toCharArray());
	        }
	    }
	    return external;
	}
	
	protected static IASTDeclarator findDefinition(IASTDeclarator declarator, int beginAtLoc) {
	    return (IASTDeclarator) findDefinition(declarator, declarator.getName().toCharArray(), beginAtLoc);
	}

	protected static IASTFunctionDeclarator findDefinition(IASTFunctionDeclarator declarator) {
		return (IASTFunctionDeclarator) findDefinition(declarator, declarator.getName().toCharArray(), AT_NEXT);
	}

	protected static IASTDeclSpecifier findDefinition(ICASTElaboratedTypeSpecifier declSpec) {
		return (IASTDeclSpecifier) findDefinition(declSpec, declSpec.getName().toCharArray(), AT_BEGINNING);
	}

	private static IASTNode findDefinition(IASTNode decl, char[] declName, int beginAtLoc) {
		IASTNode blockItem = getContainingBlockItem(decl);
		IASTNode parent = blockItem.getParent();
		IASTNode[] list = null;
		if (parent instanceof IASTCompoundStatement) {
			IASTCompoundStatement compound = (IASTCompoundStatement) parent;
			list = compound.getStatements();
		} else if (parent instanceof IASTTranslationUnit) {
			IASTTranslationUnit translation = (IASTTranslationUnit) parent;
			list = translation.getDeclarations();
		}
		boolean begun = (beginAtLoc == AT_BEGINNING);
		if (list != null) {
			for (IASTNode node : list) {
				if (node == blockItem) {
				    begun = true;
					continue;
				}
				
				if (begun) {
					if (node instanceof IASTDeclarationStatement) {
						node = ((IASTDeclarationStatement) node).getDeclaration();
					}
					
					if (node instanceof IASTFunctionDefinition && decl instanceof IASTFunctionDeclarator) {
						IASTFunctionDeclarator dtor = ((IASTFunctionDefinition) node).getDeclarator();
						IASTName name = ASTQueries.findInnermostDeclarator(dtor).getName();
						if (name.toString().equals(declName)) {
							return dtor;
						}
					} else if (node instanceof IASTSimpleDeclaration && decl instanceof ICASTElaboratedTypeSpecifier) {
						IASTSimpleDeclaration simpleDecl = (IASTSimpleDeclaration) node;
						IASTDeclSpecifier declSpec = simpleDecl.getDeclSpecifier();
						IASTName name = null;
						
						if (declSpec instanceof ICASTCompositeTypeSpecifier) {
						    name = ((ICASTCompositeTypeSpecifier) declSpec).getName();
						} else if (declSpec instanceof ICASTEnumerationSpecifier) {
						    name = ((ICASTEnumerationSpecifier) declSpec).getName();
						}
						if (name !=  null) {
						    if (CharArrayUtils.equals(name.toCharArray(), declName)) {
								return declSpec;
							}
						}
					} else if (node instanceof IASTSimpleDeclaration && decl instanceof IASTDeclarator) {
					    IASTSimpleDeclaration simpleDecl = (IASTSimpleDeclaration) node;
					    IASTDeclarator[] dtors = simpleDecl.getDeclarators();
					    for (int j = 0; dtors != null && j < dtors.length; j++) {
					        if (CharArrayUtils.equals(dtors[j].getName().toCharArray(), declName)) {
					            return dtors[j];
					        }
					    }
					}
				}
			}
		}
		return null;
	}
	
	/**
	 * Create an IType for an IASTDeclarator.
	 * 
	 * @param declarator the IASTDeclarator whose IType will be created
	 * @return the IType of the IASTDeclarator parameter
	 */
	public static IType createType(IASTDeclarator declarator) {
	    IASTDeclSpecifier declSpec = null;
		
		IASTNode node = declarator.getParent();
		while (node instanceof IASTDeclarator) {
			declarator = (IASTDeclarator) node;
			node = node.getParent();
		}
		
		if (node instanceof IASTSimpleDeclaration) {
			declSpec = ((IASTSimpleDeclaration) node).getDeclSpecifier();
		} else if (node instanceof IASTParameterDeclaration) {
			declSpec = ((IASTParameterDeclaration) node).getDeclSpecifier();
		} else if (node instanceof IASTFunctionDefinition) {
			declSpec = ((IASTFunctionDefinition) node).getDeclSpecifier();
		} else if (node instanceof IASTTypeId) {
		    declSpec = ((IASTTypeId) node).getDeclSpecifier();
		}
	
		boolean isParameter = (node instanceof IASTParameterDeclaration || node.getParent() instanceof ICASTKnRFunctionDeclarator); 
		
		IType type = createType((ICASTDeclSpecifier) declSpec);
		type = createType(type, declarator);

        if (isParameter) {
        	IType paramType = type;
        	// Remove typedefs ready for subsequent processing.
        	while (paramType instanceof ITypedef) {
        		paramType = ((ITypedef) paramType).getType();
        	}
        	        	
            //C99: 6.7.5.3-7 a declaration of a parameter as "array of type" shall be adjusted to "qualified pointer to type", where the
    		//type qualifiers (if any) are those specified within the[and] of the array type derivation
            if (paramType instanceof IArrayType) { // the index does not yet return ICArrayTypes
	            IArrayType at = (IArrayType) paramType;
				int q= 0;
				if (at instanceof ICArrayType) {
					ICArrayType cat= (ICArrayType) at;
					if (cat.isConst()) q |= CPointerType.IS_CONST;
					if (cat.isVolatile()) q |= CPointerType.IS_VOLATILE;
					if (cat.isRestrict()) q |= CPointerType.IS_RESTRICT;
				}
				type = new CPointerType(at.getType(), q);
	        } else if (paramType instanceof IFunctionType) {
	            //-8 A declaration of a parameter as "function returning type" shall be adjusted to "pointer to function returning type"
	            type = new CPointerType(paramType, 0);
	        }
        }
        
		return type;
	}
	
	public static IType createType(IType baseType, IASTDeclarator declarator) {
	    if (declarator instanceof IASTFunctionDeclarator)
	        return createType(baseType, (IASTFunctionDeclarator) declarator);
		
		IType type = baseType;
		type = setupPointerChain(declarator.getPointerOperators(), type);
		type = setupArrayChain(declarator, type);
		
	    IASTDeclarator nested = declarator.getNestedDeclarator();
	    if (nested != null) {
	    	return createType(type, nested);
	    }
	    return type;
	}
	
	public static IType createType(IType returnType, IASTFunctionDeclarator declarator) {
	    IType[] pTypes = getParmTypes(declarator);
	    returnType = setupPointerChain(declarator.getPointerOperators(), returnType);
	    
	    IType type = new CFunctionType(returnType, pTypes);
	    
	    IASTDeclarator nested = declarator.getNestedDeclarator();
	    if (nested != null) {
	    	return createType(type, nested);
	    }
	    return type;
	}

	/**
	 * This is used to create a base IType corresponding to an IASTDeclarator and
	 * the IASTDeclSpecifier.  This method doesn't have any recursive behavior and is used as
	 * the foundation of the ITypes being created. The parameter isParm is used to specify whether
	 * the declarator is a parameter or not.  
	 * 
	 * @param declSpec the IASTDeclSpecifier used to determine if the base type is a CQualifierType
	 * 		or not
	 * @return the base IType
	 */
	public static IType createBaseType(IASTDeclSpecifier declSpec) {
		if (declSpec instanceof ICASTSimpleDeclSpecifier) {
			final ICASTSimpleDeclSpecifier sds = (ICASTSimpleDeclSpecifier) declSpec;
			IASTExpression exp = sds.getDeclTypeExpression();
			if (exp != null)
				return exp.getExpressionType();
			return new CBasicType(sds);
		} 
		IBinding binding = null;
		IASTName name = null;
		if (declSpec instanceof ICASTTypedefNameSpecifier) {
			name = ((ICASTTypedefNameSpecifier) declSpec).getName();
		} else if (declSpec instanceof IASTElaboratedTypeSpecifier) {
			name = ((IASTElaboratedTypeSpecifier) declSpec).getName();
		} else if (declSpec instanceof IASTCompositeTypeSpecifier) {
			name = ((IASTCompositeTypeSpecifier) declSpec).getName();		
		} else if (declSpec instanceof IASTEnumerationSpecifier) {
			name = ((IASTEnumerationSpecifier) declSpec).getName();
		} else {
			throw new IllegalArgumentException();
		}
		
		if (name == null)
			return new ProblemType(ISemanticProblem.TYPE_NO_NAME);
		
		binding = name.resolveBinding();
		if (binding instanceof IType && !(binding instanceof IProblemBinding))
		    return (IType) binding;
		
		return new ProblemType(ISemanticProblem.TYPE_UNRESOLVED_NAME);
	}

	public static IType createType(ICASTDeclSpecifier declSpec) {
	    if (declSpec.isConst() || declSpec.isVolatile() || declSpec.isRestrict()) {
			return new CQualifierType(declSpec);
		}
	    
	    return createBaseType(declSpec);
	}

	/**
	 * Returns an IType[] corresponding to the parameter types of the IASTFunctionDeclarator parameter.
	 * 
	 * @param decltor the IASTFunctionDeclarator to create an IType[] for its parameters
	 * @return IType[] corresponding to the IASTFunctionDeclarator parameters
	 */
	private static IType[] getParmTypes(IASTFunctionDeclarator decltor) {
		if (decltor instanceof IASTStandardFunctionDeclarator) {
			IASTParameterDeclaration parms[] = ((IASTStandardFunctionDeclarator) decltor).getParameters();
			IType parmTypes[] = new IType[parms.length];
			
		    for (int i = 0; i < parms.length; i++) {
		    	parmTypes[i] = createType(parms[i].getDeclarator());
		    }
		    return parmTypes;
		} else if (decltor instanceof ICASTKnRFunctionDeclarator) {
			IASTName parms[] = ((ICASTKnRFunctionDeclarator) decltor).getParameterNames();
			IType parmTypes[] = new IType[parms.length];
			
		    for (int i = 0; i < parms.length; i++) {
		        IASTDeclarator dtor = getKnRParameterDeclarator((ICASTKnRFunctionDeclarator) decltor, parms[i]);
                if (dtor != null)
                    parmTypes[i] = createType(dtor);
		    }
		    return parmTypes;
		} else {
			throw new IllegalArgumentException();
		}
	}
	
    protected static IASTDeclarator getKnRParameterDeclarator(ICASTKnRFunctionDeclarator fKnRDtor, IASTName name) {
        IASTDeclaration[] decls = fKnRDtor.getParameterDeclarations();
        char[] n = name.toCharArray();
        for (int i = 0; i < decls.length; i++) {
            if (!(decls[i] instanceof IASTSimpleDeclaration))
                continue;
            
            IASTDeclarator[] dtors = ((IASTSimpleDeclaration) decls[i]).getDeclarators();
            for (IASTDeclarator dtor : dtors) {
                if (CharArrayUtils.equals(dtor.getName().toCharArray(), n)) {
                    return dtor; 
                }
            }
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
			IASTArrayModifier[] mods = ((IASTArrayDeclarator) decl).getArrayModifiers();
			for (int i = mods.length - 1; i >= 0; i--) {
				CArrayType arrayType = new CArrayType(lastType);
				if (mods[i] instanceof ICASTArrayModifier) {
					arrayType.setModifier((ICASTArrayModifier) mods[i]);
				}
				lastType= arrayType;
			}
		}
		
		return lastType;
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
		
		if (ptrs != null && ptrs.length > 0) {
			pointerType = new CPointerType();
											
			if (ptrs.length == 1) {
				pointerType.setType(lastType);
				pointerType.setQualifiers(
						(((ICASTPointer) ptrs[0]).isConst() ? CPointerType.IS_CONST : 0) |
						(((ICASTPointer) ptrs[0]).isRestrict() ? CPointerType.IS_RESTRICT : 0) |
						(((ICASTPointer) ptrs[0]).isVolatile() ? CPointerType.IS_VOLATILE : 0));				
			} else {
				CPointerType tempType = new CPointerType();
				pointerType.setType(tempType);
				pointerType.setQualifiers(
						(((ICASTPointer) ptrs[ptrs.length - 1]).isConst() ? CPointerType.IS_CONST : 0) |
						(((ICASTPointer) ptrs[ptrs.length - 1]).isRestrict() ? CPointerType.IS_RESTRICT : 0) |
						(((ICASTPointer) ptrs[ptrs.length - 1]).isVolatile() ? CPointerType.IS_VOLATILE : 0));
				int i = ptrs.length - 2;
				for (; i > 0; i--) {
					tempType.setType(new CPointerType());
					tempType.setQualifiers(
							(((ICASTPointer) ptrs[i]).isConst() ? CPointerType.IS_CONST : 0) |
							(((ICASTPointer) ptrs[i]).isRestrict() ? CPointerType.IS_RESTRICT : 0) |
							(((ICASTPointer) ptrs[i]).isVolatile() ? CPointerType.IS_VOLATILE : 0));
					tempType = (CPointerType) tempType.getType();
				}					
				tempType.setType(lastType);
				tempType.setQualifiers(
						(((ICASTPointer) ptrs[i]).isConst() ? CPointerType.IS_CONST : 0) |
						(((ICASTPointer) ptrs[i]).isRestrict() ? CPointerType.IS_RESTRICT : 0) |
						(((ICASTPointer) ptrs[i]).isVolatile() ? CPointerType.IS_VOLATILE : 0));
			}
			
			return pointerType;
		}
		
		return lastType;
	}
	
	public static IASTProblem[] getProblems(IASTTranslationUnit tu) {
		CollectProblemsAction action = new CollectProblemsAction();
		tu.accept(action);
		
		return action.getProblems();
	}
	
	public static IASTName[] getDeclarations(IASTTranslationUnit tu, IBinding binding) {
		CollectDeclarationsAction action = new CollectDeclarationsAction(binding);
		tu.accept(action);

		return action.getDeclarationNames();
	}

	public static IASTName[] getReferences(IASTTranslationUnit tu, IBinding binding) {
		CollectReferencesAction action = new CollectReferencesAction(binding);
		tu.accept(action);
		return action.getReferences();
	}

    public static IBinding[] findBindingsForContentAssist(IASTName name, boolean isPrefix) {
        ASTNodeProperty prop = name.getPropertyInParent();
        
        IBinding[] result = null; 
        
        if (prop == IASTFieldReference.FIELD_NAME) {
            result = (IBinding[]) findBinding((IASTFieldReference) name.getParent(), isPrefix);
        } else if (prop == ICASTFieldDesignator.FIELD_NAME) {
            result = findBindingForContentAssist((ICASTFieldDesignator) name.getParent(), isPrefix);
        } else {
	        IScope scope= getContainingScope(name);
			try {
				if (isPrefix) {
					result = lookupPrefix(scope, name);
				} else {
					result = new IBinding[] { lookup(scope, name) };
				}
			} catch (DOMException e) {
	        }
        }
        return ArrayUtil.trim(IBinding.class, result);
    }
    
	private static IBinding[] findBindingForContentAssist(ICASTFieldDesignator fd, boolean isPrefix) {
		IASTNode blockItem = getContainingBlockItem(fd);
		
		IASTNode parent= blockItem;
		while (parent != null && !(parent instanceof IASTSimpleDeclaration))
			parent= parent.getParent();
		
		if (parent instanceof IASTSimpleDeclaration) {
			IASTSimpleDeclaration simpleDecl = (IASTSimpleDeclaration) parent;
			IBinding struct= null;
			if (simpleDecl.getDeclSpecifier() instanceof IASTNamedTypeSpecifier) {
				struct = ((IASTNamedTypeSpecifier) simpleDecl.getDeclSpecifier()).getName().resolveBinding();
			} else if (simpleDecl.getDeclSpecifier() instanceof IASTElaboratedTypeSpecifier) {
				struct = ((IASTElaboratedTypeSpecifier) simpleDecl.getDeclSpecifier()).getName().resolveBinding();
			} else if (simpleDecl.getDeclSpecifier() instanceof IASTCompositeTypeSpecifier) {
				struct = ((IASTCompositeTypeSpecifier) simpleDecl.getDeclSpecifier()).getName().resolveBinding();
			}
			if (struct instanceof IType) {
				IType t= unwrapTypedefs((IType) struct);
			
				if (t instanceof ICompositeType) {
					return findFieldsByPrefix((ICompositeType) t, fd.getName().toCharArray());
				}
			}
		}
		return null;
	}
	
	public static IBinding[] findBindings(IScope scope, String name) {
        CASTName astName = new CASTName(name.toCharArray());
	    
	    // normal names
	    astName.setPropertyInParent(STRING_LOOKUP_PROPERTY);
	    Object o1 = lookup(scope, astName);
        
	    IBinding[] b1 = null;
	    if (o1 instanceof IBinding) {
	    	b1 = new IBinding[] { (IBinding) o1 };
	    } else {
	    	b1 = (IBinding[]) o1;
	    }
	    
	    // structure names
        astName.setPropertyInParent(STRING_LOOKUP_TAGS_PROPERTY);
        Object o2 = lookup(scope, astName);

	    IBinding[] b2 = null;
	    if (o2 instanceof IBinding) {
	    	b2 = new IBinding[] { (IBinding) o2 };
	    } else {
	    	b2 = (IBinding[]) o2;
	    }
        
        // label names
        List<ILabel> b3 = new ArrayList<ILabel>();
        do {
            char[] n = name.toCharArray();
            if (scope instanceof ICFunctionScope) {
                ILabel[] labels = ((CFunctionScope) scope).getLabels();
                for (ILabel label : labels) {
                	if (CharArrayUtils.equals(label.getNameCharArray(), n)) {
                		b3.add(label);
                		break;
                	}
	            }
                break;
            }
            try {
				scope = scope.getParent();
			} catch (DOMException e) {
				scope= null;
			}
        } while (scope != null);
        
        int c = (b1 == null ? 0 : b1.length) + (b2 == null ? 0 : b2.length) + b3.size();

        IBinding[] result = new IBinding[c];
        
        if (b1 != null)
        	ArrayUtil.addAll(IBinding.class, result, b1);
        
        if (b2 != null)
        	ArrayUtil.addAll(IBinding.class, result, b2);
       
        ArrayUtil.addAll(IBinding.class, result, b3.toArray(new IBinding[b3.size()]));
        
        return result;
    }
    
	static public boolean declaredBefore(IASTNode nodeA, IASTNode nodeB) {
	    if (nodeB == null) return true;
	    if (nodeB.getPropertyInParent() == STRING_LOOKUP_PROPERTY) return true;
	    
	    if (nodeA instanceof ASTNode) {
	    	ASTNode nd= (ASTNode) nodeA;
	        int pointOfDecl = 0;
	        
            ASTNodeProperty prop = nd.getPropertyInParent();
            // point of declaration for a name is immediately after its complete declarator and before its initializer
            if (prop == IASTDeclarator.DECLARATOR_NAME || nd instanceof IASTDeclarator) {
                IASTDeclarator dtor = (IASTDeclarator)((nd instanceof IASTDeclarator) ? nd : nd.getParent());
                while (dtor.getParent() instanceof IASTDeclarator)
                    dtor = (IASTDeclarator) dtor.getParent();
                IASTInitializer init = dtor.getInitializer();
                if (init != null)
                    pointOfDecl = ((ASTNode) init).getOffset() - 1;
                else
                    pointOfDecl = ((ASTNode) dtor).getOffset() + ((ASTNode) dtor).getLength();
            } 
            // point of declaration for an enumerator is immediately after it enumerator-definition
            else if (prop == IASTEnumerator.ENUMERATOR_NAME) {
                IASTEnumerator enumtor = (IASTEnumerator) nd.getParent();
                if (enumtor.getValue() != null) {
                    ASTNode exp = (ASTNode) enumtor.getValue();
                    pointOfDecl = exp.getOffset() + exp.getLength();
                } else {
                    pointOfDecl = nd.getOffset() + nd.getLength();
                }
            } else {
                pointOfDecl = nd.getOffset() + nd.getLength();
            }
            
            return pointOfDecl < ((ASTNode) nodeB).getOffset();
	    }
	    
	    return true; 
	}

	/**
	 * Searches for the function enclosing the given node. May return <code>null</code>.
	 */
	public static IBinding findEnclosingFunction(IASTNode node) {
		while (node != null && !(node instanceof IASTFunctionDefinition)) {
			node= node.getParent();
		}
		if (node == null)
			return null;
		
		IASTDeclarator dtor= findInnermostDeclarator(((IASTFunctionDefinition) node).getDeclarator());
		if (dtor != null) {
			IASTName name= dtor.getName();
			if (name != null) {
				return name.resolveBinding();
			}
		}
		return null;
	}

	/**
	 * Searches for the first function, struct or union enclosing the declaration the provided
	 * node belongs to and returns the binding for it. Returns <code>null</code>, if the declaration is not
	 * enclosed by any of the above constructs.
	 */
	public static IBinding findDeclarationOwner(IASTNode node, boolean allowFunction) {
		// search for declaration
		while (!(node instanceof IASTDeclaration)) {
			if (node == null)
				return null;
			
			node= node.getParent();
		}
				
		// search for enclosing binding
		IASTName name= null;
		node= node.getParent();
		for (; node != null; node= node.getParent()) {
			if (node instanceof IASTFunctionDefinition) {
				if (!allowFunction) 
					continue;

				IASTDeclarator dtor= findInnermostDeclarator(((IASTFunctionDefinition) node).getDeclarator());
				if (dtor != null) {
					name= dtor.getName();
				}
				break;
			} 
			if (node instanceof IASTCompositeTypeSpecifier) {
				name= ((IASTCompositeTypeSpecifier) node).getName();
				break;
			}
		}
		if (name == null) 
			return null;
		
		return name.resolveBinding();
	}
}
