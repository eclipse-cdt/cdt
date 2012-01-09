/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Niefer (IBM Corporation) - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.TDEF;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.getNestedType;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStandardFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.ISemanticProblem;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBlockScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.Linkage;
import org.eclipse.cdt.internal.core.dom.parser.ASTInternal;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;
import org.eclipse.cdt.internal.core.dom.parser.ProblemFunctionType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;
import org.eclipse.core.runtime.PlatformObject;

/**
 * Binding for c++ function
 */
public class CPPFunction extends PlatformObject implements ICPPFunction, ICPPInternalFunction {

	protected IASTDeclarator[] declarations;
	protected ICPPASTFunctionDeclarator definition;
	protected ICPPFunctionType type = null;
	
	private static final int FULLY_RESOLVED         = 1;
	private static final int RESOLUTION_IN_PROGRESS = 1 << 1;
	private int bits = 0;
	
	public CPPFunction(IASTDeclarator declarator) {
	    if (declarator != null) {
			IASTNode parent = ASTQueries.findOutermostDeclarator(declarator).getParent();
			if (parent instanceof IASTFunctionDefinition) {
				if (declarator instanceof ICPPASTFunctionDeclarator) {
					definition = (ICPPASTFunctionDeclarator) declarator;
				}
			} else {
				declarations = new IASTDeclarator[] { declarator };
			}
	    
		    IASTName name= getASTName();
		    name.setBinding(this);
	    }
	}
	
	private void resolveAllDeclarations() {
	    if ((bits & (FULLY_RESOLVED | RESOLUTION_IN_PROGRESS)) == 0) {
	        bits |= RESOLUTION_IN_PROGRESS;
		    IASTTranslationUnit tu = null;
	        if (definition != null) {
	            tu = definition.getTranslationUnit();
	        } else if (declarations != null) {
	            tu = declarations[0].getTranslationUnit();
	        } else {
	            //implicit binding
	            IScope scope = getScope();
                IASTNode node = ASTInternal.getPhysicalNodeOfScope(scope);
				if (node != null) {
					tu = node.getTranslationUnit();
				}
	        }
	        if (tu != null) {
	            CPPVisitor.getDeclarations(tu, this);
	        }
	        declarations = ArrayUtil.trim(IASTDeclarator.class, declarations);
	        bits |= FULLY_RESOLVED;
	        bits &= ~RESOLUTION_IN_PROGRESS;
	    }
	}
	
    @Override
	public IASTDeclarator[] getDeclarations() {
        return declarations;
    }

    @Override
	public ICPPASTFunctionDeclarator getDefinition() {
        return definition;
    }
    
	@Override
	public final void addDefinition(IASTNode node) {
		IASTDeclarator dtor = extractRelevantDtor(node);
		if (dtor instanceof ICPPASTFunctionDeclarator) {
			ICPPASTFunctionDeclarator fdtor= (ICPPASTFunctionDeclarator) dtor;
			updateFunctionParameterBindings(fdtor);
			definition = fdtor;
		}
	}
	
	@Override
	public final void addDeclaration(IASTNode node) {
		IASTDeclarator dtor = extractRelevantDtor(node);
		if (dtor == null) {
			return;
		}
		
		// function could be declared via a typedef
		if (dtor instanceof ICPPASTFunctionDeclarator) {
			updateFunctionParameterBindings((ICPPASTFunctionDeclarator) dtor);
		}

		if (declarations == null || declarations.length == 0) {
			declarations = new IASTDeclarator[] { dtor };
		} else {
			// Keep the lowest offset declaration in [0]
			if (((ASTNode)node).getOffset() < ((ASTNode)declarations[0]).getOffset()) {
				declarations = ArrayUtil.prepend(declarations, dtor);
			} else {
				declarations = ArrayUtil.append(declarations, dtor);
			}
		}
	}

	private IASTDeclarator extractRelevantDtor(IASTNode node) {
		while (node instanceof IASTName)
			node = node.getParent();
		if (!(node instanceof IASTDeclarator))
			return null;
		return ASTQueries.findTypeRelevantDeclarator((IASTDeclarator) node);
	}

	@Override
	public ICPPParameter[] getParameters() {
	    IASTStandardFunctionDeclarator dtor = getPreferredDtor();
	    if (dtor == null) {
			return CPPBuiltinParameter.createParameterList(getType());
	    }
		IASTParameterDeclaration[] params = dtor.getParameters();
		int size = params.length;
		ICPPParameter[] result = new ICPPParameter[size];
		if (size > 0) {
			for (int i = 0; i < size; i++) {
				IASTParameterDeclaration p = params[i];
				final IASTName name = getParamName(p);
				final IBinding binding= name.resolveBinding();
				if (binding instanceof ICPPParameter) {
					result[i]= (ICPPParameter) binding;
				} else {
					result[i] = new CPPParameter.CPPParameterProblem(p, IProblemBinding.SEMANTIC_INVALID_TYPE,
							name.toCharArray());
				}
			}
		}
		return result;
	}

	@Override
	public IScope getFunctionScope() {
	    resolveAllDeclarations();
	    if (definition != null) {
			return definition.getFunctionScope();
	    } 
	        
	    for (IASTDeclarator dtor : declarations) {
			if (dtor instanceof ICPPASTFunctionDeclarator) {
				return ((ICPPASTFunctionDeclarator) dtor).getFunctionScope();
			}
		}
	    
	    // function declaration via typedef
	    return null;
	}

	@Override
	public String getName() {
	    return getASTName().toString();
	}

	@Override
	public char[] getNameCharArray() {
		return getASTName().getSimpleID();
	}
	
	protected IASTName getASTName() {
		IASTDeclarator dtor = (definition != null) ? definition : declarations[0];
		dtor= ASTQueries.findInnermostDeclarator(dtor);
	    IASTName name= dtor.getName();
	    if (name instanceof ICPPASTQualifiedName) {
	        IASTName[] ns = ((ICPPASTQualifiedName)name).getNames();
	        name = ns[ns.length - 1];
	    }
	    return name;
	}

	@Override
	public IScope getScope() {
	    IASTName n = getASTName();
	    IScope scope = CPPVisitor.getContainingScope(n);
	    if (scope instanceof ICPPClassScope) {
	    	ICPPASTDeclSpecifier declSpec = null;
		    if (definition != null) {
		    	IASTNode node = ASTQueries.findOutermostDeclarator(definition).getParent();
		        IASTFunctionDefinition def = (IASTFunctionDefinition) node;
			    declSpec = (ICPPASTDeclSpecifier) def.getDeclSpecifier();    
		    } else {
		    	IASTNode node = ASTQueries.findOutermostDeclarator(declarations[0]).getParent();
		        IASTSimpleDeclaration decl = (IASTSimpleDeclaration)node; 
		        declSpec = (ICPPASTDeclSpecifier) decl.getDeclSpecifier();
		    }
		    if (declSpec.isFriend()) {
		        try {
	                while (scope instanceof ICPPClassScope) {
		                scope = scope.getParent();
	                }
		        } catch (DOMException e) {
	            }
		    }
	    }
		return scope;
	}

    @Override
	public ICPPFunctionType getType() {
        if (type == null) {
			final IType t = getNestedType(CPPVisitor.createType((definition != null) ? definition : declarations[0]), TDEF);
			if (t instanceof ICPPFunctionType) {
				type = (ICPPFunctionType) t;
			} else if (t instanceof ISemanticProblem){
				type= new ProblemFunctionType(((ISemanticProblem) t).getID());
			} else {
				// This case is unexpected
				type = new ProblemFunctionType(ISemanticProblem.TYPE_UNRESOLVED_NAME);
			}
		}
        return type;
    }

    @Override
	public IBinding resolveParameter(CPPParameter param) {
		int pos= param.getParameterPosition();
		
    	int tdeclLen= declarations == null ? 0 : declarations.length;
    	for (int i= -1; i < tdeclLen; i++) {
    		ICPPASTFunctionDeclarator tdecl;
    		if (i == -1) {
    			tdecl= definition;
    			if (tdecl == null)
    				continue;
    		} else {
    			final IASTDeclarator dtor= declarations[i];
    			if (!(dtor instanceof ICPPASTFunctionDeclarator)) {
        			if (dtor == null) {
        				break;
        			}
    				continue;
    			}
    			tdecl= (ICPPASTFunctionDeclarator) dtor;
    		}
    		
    		IASTParameterDeclaration[] params = tdecl.getParameters();
    		if (pos < params.length) {
				final IASTName oName = getParamName(params[pos]);
    			return oName.resolvePreBinding();
    		}
    	}
    	return param;
    }

	private IASTName getParamName(final IASTParameterDeclaration paramDecl) {
		return ASTQueries.findInnermostDeclarator(paramDecl.getDeclarator()).getName();
	}
    
    protected final void updateFunctionParameterBindings(ICPPASTFunctionDeclarator fdtor) {
		IASTParameterDeclaration[] updateParams = fdtor.getParameters();

    	int k= 0;
    	int tdeclLen= declarations == null ? 0 : declarations.length;
    	for (int i= -1; i < tdeclLen && k < updateParams.length; i++) {
    		ICPPASTFunctionDeclarator tdecl;
    		if (i == -1) {
    			tdecl= definition;
    			if (tdecl == null)
    				continue;
    		} else {
    			final IASTDeclarator dtor= declarations[i];
    			if (!(dtor instanceof ICPPASTFunctionDeclarator)) {
        			if (dtor == null) {
        				break;
        			}
    				continue;
    			}
    			tdecl= (ICPPASTFunctionDeclarator) dtor;
    		}
    		
    		IASTParameterDeclaration[] params = tdecl.getParameters();
    		int end= Math.min(params.length, updateParams.length);
    		for (; k < end; k++) {
    			final IASTName oName = getParamName(params[k]);
    			IBinding b= oName.resolvePreBinding();
    			IASTName n = getParamName(updateParams[k]);
    			n.setBinding(b);
    			ASTInternal.addDeclaration(b, n);
    		}
    	}
    }

    @Override
	public final boolean isStatic() {
        return isStatic(true);
    }

    @Override
	public boolean isStatic(boolean resolveAll) {
        if (resolveAll && (bits & FULLY_RESOLVED) == 0) {
            resolveAllDeclarations();
        }
		return hasStorageClass(this, IASTDeclSpecifier.sc_static);
    }

//	static public boolean isStatic
//        //2 state bits, most significant = whether or not we've figure this out yet
//        //least significant = whether or not we are static
//        int state = (bits & IS_STATIC) >> 2;
//        if (state > 1) return (state % 2 != 0);
//        
//        IASTDeclSpecifier declSpec = null;
//        IASTFunctionDeclarator dtor = (IASTFunctionDeclarator) getDefinition();
//        if (dtor != null) {
//	        declSpec = ((IASTFunctionDefinition)dtor.getParent()).getDeclSpecifier();
//	        if (declSpec.getStorageClass() == IASTDeclSpecifier.sc_static) {
//	            bits |= 3 << 2;
//	            return true;
//	        }
//        }
//        
//        IASTFunctionDeclarator[] dtors = (IASTFunctionDeclarator[]) getDeclarations();
//        if (dtors != null) {
//	        for (int i = 0; i < dtors.length; i++) {
//	            IASTNode parent = dtors[i].getParent();
//	            declSpec = ((IASTSimpleDeclaration)parent).getDeclSpecifier();
//	            if (declSpec.getStorageClass() == IASTDeclSpecifier.sc_static) {
//	                bits |= 3 << 2;
//	                return true;
//	            }
//	        }
//        }
//        bits |= 2 << 2;
//        return false;
//    }
    
    @Override
	public String[] getQualifiedName() {
        return CPPVisitor.getQualifiedName(this);
    }

    @Override
	public char[][] getQualifiedNameCharArray() {
        return CPPVisitor.getQualifiedNameCharArray(this);
    }

    @Override
	public boolean isGloballyQualified() throws DOMException {
        IScope scope = getScope();
        while (scope != null) {
            if (scope instanceof ICPPBlockScope)
                return false;
            scope = scope.getParent();
        }
        return true;
    }

	static public boolean hasStorageClass(ICPPInternalFunction function, int storage) {
	    IASTDeclarator dtor = (IASTDeclarator) function.getDefinition();
	    IASTNode[] ds = function.getDeclarations();

        int i = -1;
        do {
            if (dtor != null) {
                IASTNode parent = dtor.getParent();
	            while (!(parent instanceof IASTDeclaration))
	                parent = parent.getParent();
	            
	            IASTDeclSpecifier declSpec = null;
	            if (parent instanceof IASTSimpleDeclaration) {
	                declSpec = ((IASTSimpleDeclaration)parent).getDeclSpecifier();
	            } else if (parent instanceof IASTFunctionDefinition) {
	                declSpec = ((IASTFunctionDefinition)parent).getDeclSpecifier();
	            }
	            if (declSpec != null && declSpec.getStorageClass() == storage) {
	            	return true;
	            }
            }
            if (ds != null && ++i < ds.length) {
            	dtor = (IASTDeclarator) ds[i];
            } else {
                break;
            }
        } while (dtor != null);
        return false;
	}
	
	@Override
	public boolean isDeleted() {
		return isDeletedDefinition(getDefinition());
	}

	public static boolean isDeletedDefinition(IASTNode def) {
		while (def != null && !(def instanceof IASTDeclaration)) {
			def= def.getParent();
		}
		if (def instanceof ICPPASTFunctionDefinition) {
			return ((ICPPASTFunctionDefinition) def).isDeleted();
		}
		return false;
	}

	@Override
	public boolean isMutable() {
        return false;
    }

    @Override
	public boolean isInline() {
    	IASTDeclarator dtor = getDefinition();
        IASTDeclarator[] ds = getDeclarations();
        int i = -1;
        do {
            if (dtor != null) {
                IASTNode parent = dtor.getParent();
	            while (!(parent instanceof IASTDeclaration))
	                parent = parent.getParent();
	            
	            IASTDeclSpecifier declSpec = null;
	            if (parent instanceof IASTSimpleDeclaration)
	                declSpec = ((IASTSimpleDeclaration)parent).getDeclSpecifier();
	            else if (parent instanceof IASTFunctionDefinition)
	                declSpec = ((IASTFunctionDefinition)parent).getDeclSpecifier();
	            
	            if (declSpec != null && declSpec.isInline())
                    return true;
            }
            if (ds != null && ++i < ds.length)
                dtor = ds[i];
            else
                break;
        } while (dtor != null);
        return false;
    }

    @Override
	public boolean isExternC() {
	    if (CPPVisitor.isExternC(getDefinition())) {
	    	return true;
	    }
        IASTNode[] ds= getDeclarations();
        if (ds != null) {
        	for (IASTNode element : ds) {
        		if (CPPVisitor.isExternC(element)) {
        			return true;
        		}
			}
        }
        return false;
    }

    @Override
	public boolean isExtern() {
        return hasStorageClass(this, IASTDeclSpecifier.sc_extern);
    }

    @Override
	public boolean isAuto() {
        return hasStorageClass(this, IASTDeclSpecifier.sc_auto);
    }

    @Override
	public boolean isRegister() {
        return hasStorageClass(this, IASTDeclSpecifier.sc_register);
    }

    @Override
	public boolean takesVarArgs() {
        ICPPASTFunctionDeclarator dtor= getPreferredDtor();
        return dtor != null ? dtor.takesVarArgs() : false;
    }
    
	@Override
	public ILinkage getLinkage() {
		return Linkage.CPP_LINKAGE;
	}
	
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(getName());
		IFunctionType t = getType();
		result.append(t != null ? ASTTypeUtil.getParameterTypeString(t) : "()"); //$NON-NLS-1$
		return result.toString();
	}
	
	@Override
	public IBinding getOwner() {
		return CPPVisitor.findNameOwner(getASTName(), false);
	}

	@Override
	public IType[] getExceptionSpecification() {
		ICPPASTFunctionDeclarator declarator = getPreferredDtor();
		if (declarator != null) {
			IASTTypeId[] astTypeIds= declarator.getExceptionSpecification();
			if (astTypeIds.equals(ICPPASTFunctionDeclarator.NO_EXCEPTION_SPECIFICATION)) 
				return null;
			
			if (astTypeIds.equals(IASTTypeId.EMPTY_TYPEID_ARRAY)) 
				return IType.EMPTY_TYPE_ARRAY;
			
			IType[] typeIds = new IType[astTypeIds.length];
			for (int i= 0; i < astTypeIds.length; ++i) {
				typeIds[i] = CPPVisitor.createType(astTypeIds[i]);
			}
			return typeIds;
		}
		return null;
	}
	
	protected ICPPASTFunctionDeclarator getPreferredDtor() {
        ICPPASTFunctionDeclarator dtor = getDefinition();
        if (dtor != null) 
        	return dtor;

        IASTDeclarator[] dtors = getDeclarations();
        if (dtors != null) {
        	for (IASTDeclarator declarator : dtors) {
        		if (declarator instanceof ICPPASTFunctionDeclarator) 
        			return (ICPPASTFunctionDeclarator) declarator;
        	}
        }
        return null;
	}

	@Override
	public int getRequiredArgumentCount() {
		return getRequiredArgumentCount(getParameters());
	}

	public static int getRequiredArgumentCount(ICPPParameter[] pars) {
		int result= pars.length;
		while(result > 0) {
			final ICPPParameter p = pars[result-1];
			if (p.hasDefaultValue() || p.isParameterPack()) {
				result--;
			} else {
				if (pars.length == 1 && SemanticUtil.isVoidType(p.getType())) {
					return 0;
				}
				return result;
			}
		}
		return 0;
	}

	@Override
	public boolean hasParameterPack() {
		ICPPParameter[] pars= getParameters();
		return pars.length > 0 && pars[pars.length-1].isParameterPack();
	}
}
