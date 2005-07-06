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
package org.eclipse.cdt.internal.core.index.domsourceindexer;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTSignatureUtil;
import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStandardFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMember;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;
import org.eclipse.cdt.internal.core.dom.parser.c.ICInternalFunction;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalFunction;
import org.eclipse.cdt.internal.core.index.IIndex;

/**
 * Utility methods used by both C and C++ visitors 
 * 
 * @author vhirsl
 */
public class IndexVisitorUtil {
    private IndexVisitorUtil() {}
    
    /**
     * @param name 
     * @param functionBinding
     * @return
     */
    static int getModifiers(IASTName name, IBinding binding) {
        int modifiers = 0;
        try {
            if (binding instanceof ICPPMember) {
                ICPPMember member = (ICPPMember) binding;
                int vis = member.getVisibility();
                if (vis == ICPPMember.v_public) {
                    modifiers |= IIndex.publicAccessSpecifier;
                }
                else if (vis == ICPPMember.v_private) {
                    modifiers |= IIndex.privateAccessSpecifier;
                }
                else if (vis == ICPPMember.v_protected) {
                    modifiers |= IIndex.protectedAccessSpecifier;
                }
            }
            if (binding instanceof ICompositeType ||
                    binding instanceof IEnumeration) {
                IASTNode parent = name.getParent();
                if (parent instanceof IASTDeclSpecifier) {
                    IASTDeclSpecifier declSpec = (IASTDeclSpecifier) parent;
                    
                    if (declSpec.isConst()) {
                        modifiers |= IIndex.constQualifier;
                    }
                    else if (declSpec.isVolatile()) {
                        modifiers |= IIndex.volatileQualifier;
                    }
                    if (declSpec.isInline()) {
                        modifiers |= IIndex.inlineSpecifier;
                    }
                    if (declSpec instanceof ICPPASTDeclSpecifier) {
                        ICPPASTDeclSpecifier cppDeclSpec = (ICPPASTDeclSpecifier) declSpec;
                        if (cppDeclSpec.isExplicit()) {
                            modifiers |= IIndex.explicitSpecifier;
                        }
                        if (cppDeclSpec.isVirtual()) {
                            modifiers |= IIndex.virtualSpecifier;
                        }
                    }
                }
            }
            else if (binding instanceof IVariable) {
                IVariable variableBinding = (IVariable) binding;

                if (variableBinding.isAuto()) {
                    modifiers |= IIndex.autoSpecifier;
                }
                else if (variableBinding.isExtern()) {
                    modifiers |= IIndex.externSpecifier;
                }
                else if (variableBinding.isRegister()) {
                    modifiers |= IIndex.registerSpecifier;
                }
                else if (variableBinding.isStatic()) {
                    modifiers |= IIndex.staticSpecifier;
                }
                if (variableBinding instanceof ICPPVariable) {
                    ICPPVariable cppVariable = (ICPPVariable) variableBinding;
                    if (cppVariable.isMutable()) {
                        modifiers |= IIndex.mutableSpecifier;
                    }
                }
            }
            else if (binding instanceof IFunction) {
            	if( binding instanceof ICInternalFunction ){
            		//performance improvement for indexing, we know we have already resolved all the previous 
            		//declarations of this function.
            		((ICInternalFunction)binding).setFullyResolved( true );
            	}
            	
                IFunction functionBinding = (IFunction) binding;

                if (functionBinding.isAuto()) {
                    modifiers |= IIndex.autoSpecifier;
                }
                else if (functionBinding.isExtern()) {
                    modifiers |= IIndex.externSpecifier;
                }
                else if (functionBinding.isRegister()) {
                    modifiers |= IIndex.registerSpecifier;
                }
                //For performance reasons, use internal interface if possible, since we know the 
                //index is resolving bindings in order.
                else if ((binding instanceof ICPPInternalFunction) ? ((ICPPInternalFunction)functionBinding).isStatic(false) 
                												   : functionBinding.isStatic()) {
                    modifiers |= IIndex.staticSpecifier;
                }
                else if (functionBinding.isInline()) {
                    modifiers |= IIndex.inlineSpecifier;
                }
                if (functionBinding instanceof ICPPFunction) {
                    ICPPFunction cppFunction = (ICPPFunction) functionBinding;
                    if (cppFunction.isMutable()) {
                        modifiers |= IIndex.mutableSpecifier;
                    }
                    if (cppFunction instanceof ICPPMethod) {
                        ICPPMethod cppMethod = (ICPPMethod) cppFunction;
                        if (cppMethod.isVirtual()) {
                            modifiers |= IIndex.virtualSpecifier;
                        }
                        if (cppMethod instanceof ICPPConstructor) {
                            ICPPConstructor constructor = (ICPPConstructor) cppMethod;
                            if (constructor.isExplicit()) {
                                modifiers |= IIndex.explicitSpecifier;
                            }
                        }
                    }
                }
                // check parent node for const, pure virtual and volatile
                IASTNode parent = name.getParent();
                if (parent instanceof ICPPASTFunctionDeclarator) {
                    ICPPASTFunctionDeclarator cppFunDecl = (ICPPASTFunctionDeclarator) parent;
                    if (cppFunDecl.isConst()) {
                        modifiers |= IIndex.constQualifier;
                    }
                    else if (cppFunDecl.isVolatile()) {
                        modifiers |= IIndex.volatileQualifier;
                    }
                    if (cppFunDecl.isPureVirtual()) {
                        modifiers |= IIndex.pureVirtualSpecifier;
                    }
                }
            } // IFunction
        }
        catch (DOMException e) {
        }
        return modifiers;
    }

    /**
     * @param functionName
     * @return
     */
    public static char[][] getParameters(IASTName functionName) {
        IASTNode parent = functionName.getParent();
        if (parent instanceof ICPPASTQualifiedName) {
        	parent = parent.getParent();
        }
        if (parent instanceof IASTFunctionDeclarator) {
        	IASTFunctionDeclarator fDecl = (IASTFunctionDeclarator) parent;
            String[] parameters = ASTSignatureUtil.getParameterSignatureArray(fDecl);
            char[][] rv;
            if (fDecl instanceof IASTStandardFunctionDeclarator &&
	            	((IASTStandardFunctionDeclarator) fDecl).takesVarArgs()) {
           		rv = new char[parameters.length + 1][];
           		rv[parameters.length] = "...".toCharArray(); //$NON-NLS-1$
            }
            else {
                rv = new char[parameters.length][];
            }
            if (rv.length == 0) {
                return new char[][] { "void".toCharArray() }; //$NON-NLS-1$
            }
            for (int k = 0; k < parameters.length; k++) {
                rv[k] = parameters[k].toCharArray();
            }
            return rv;
        }
        return new char[0][];
    }
    
    /**
     * @param function
     * @return
     */
    public static char[][] getParameters(IFunction function) {
        List parameterList = new ArrayList();
        try {
            IParameter[] parameters = function.getParameters();
            for (int i = 0; i < parameters.length; i++) {
                IType paramType = parameters[i].getType();
                parameterList.add(ASTTypeUtil.getType(paramType).toCharArray());
            }
            if (function.takesVarArgs()) {
                parameterList.add("...".toCharArray()); //$NON-NLS-1$
            }
            if (parameterList.isEmpty()) {
            	parameterList.add("void".toCharArray()); //$NON-NLS-1$
            }
        }
        catch (DOMException e) {
        }
        return (char[][]) parameterList.toArray(new char[parameterList.size()][]);
    }            
    /**
     * @param function
     * @return
     */
    public static char[] getReturnType(IFunction function) {
        try {
            IFunctionType functionType = function.getType();
            IType returnType = functionType.getReturnType();
            return ASTTypeUtil.getType(returnType).toCharArray();
        }
        catch (DOMException e) {
        }
        return new char[0];
    }
    
}
