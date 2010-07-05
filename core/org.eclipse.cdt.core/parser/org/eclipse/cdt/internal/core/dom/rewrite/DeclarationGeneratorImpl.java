/*******************************************************************************
 * Copyright (c) 2010 Tomasz Wesolowski
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Tomasz Wesolowski - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.dom.rewrite;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTArrayDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTPointer;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IArrayType;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBasicType.Kind;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.INodeFactory;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IQualifierType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTReferenceOperator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNodeFactory;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTArrayDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;

/**
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part
 * of a work in progress. There is no guarantee that this API will work or that
 * it will remain the same.
 * 
 * I'll be public API in org.eclipse.cdt.core 5.3
 * 
 * @author Tomasz Wesolowski
 *
 */
public class DeclarationGeneratorImpl{

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.rewrite.IDeclarationGenerator#createDeclSpecFromType(org.eclipse.cdt.core.dom.ast.IType)
	 */
	
	private INodeFactory factory;
	
	/**
	 * Creates a new generator using the given factory.
	 * @param factory a factory to use. If a C++ type is requested, it has to be an instance of {@link ICPPNodeFactory}. 
	 */
	public DeclarationGeneratorImpl(INodeFactory factory) {
		this.factory = factory;
	}
	

	public IASTDeclSpecifier createDeclSpecFromType(IType type) {
		
		IASTDeclSpecifier returnedDeclSpec = null;
		
		if (type instanceof IPointerType) {
			IType inner = ((IPointerType)type).getType();
			returnedDeclSpec = createDeclSpecFromType(inner);
		} else if (type instanceof ICPPReferenceType) {
			IType inner = ((ICPPReferenceType)type).getType();
			returnedDeclSpec = createDeclSpecFromType(inner);
		} else if (type instanceof IArrayType) {
			IType inner = ((IArrayType)type).getType();
			returnedDeclSpec = createDeclSpecFromType(inner);
		} else if (type instanceof IFunctionType) {
			IType inner = ((IFunctionType)type).getReturnType();
			returnedDeclSpec = createDeclSpecFromType(inner);
		} else if (type instanceof IQualifierType) {
			IType inner = ((IQualifierType)type).getType();
			returnedDeclSpec = createDeclSpecFromType(inner);
			if (((IQualifierType) type).isConst()) {
				returnedDeclSpec.setConst(true);
			}
			if (((IQualifierType) type).isVolatile()) {
				returnedDeclSpec.setVolatile(true);
			}
		} else if (type instanceof IBasicType) {
			Kind kind = ((IBasicType)type).getKind();
			IASTSimpleDeclSpecifier declSpec = factory.newSimpleDeclSpecifier();
			declSpec.setType(kind);
			returnedDeclSpec = declSpec;
		} else if (type instanceof IBinding) { /* ITypedef, ICompositeType... */ 
			// BTW - we need to distinguish (and fail explicitly) on literal composites like:
			// struct {  } aSingleInstance;
			returnedDeclSpec = getDeclSpecForBinding((IBinding) type);
		}
		
		// Fallback...
		if (returnedDeclSpec == null) {
			IASTSimpleDeclSpecifier specifier = factory.newSimpleDeclSpecifier();
			specifier.setType(Kind.eVoid);
			returnedDeclSpec = specifier;
		}
		return returnedDeclSpec;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.rewrite.IDeclarationGenerator#createDeclaratorFromType(org.eclipse.cdt.core.dom.ast.IType, char[])
	 */
	public IASTDeclarator createDeclaratorFromType(IType type, char[] name) {
		
		IASTDeclarator returnedDeclarator = null;
	
		try {
	
			if (type instanceof IPointerType || type instanceof IArrayType || type instanceof ICPPReferenceType) {
				
				IASTDeclarator declarator = createPointerOrArrayDeclarator(type, name);
				returnedDeclarator = declarator;
						
			} else if (typeIrrelevantForDeclarator(type)) {
				IASTDeclarator declarator = factory.newDeclarator(null);
				IASTName astName = factory.newName(name);
				declarator.setName(astName);
				
				returnedDeclarator = declarator;
				
			} else if (type instanceof IFunctionType) {
				// TODO function pointers
				
				
			}
		} catch (DOMException e) {
			e.printStackTrace();
		}
		
		// Fallback
		if (returnedDeclarator == null) {
			returnedDeclarator = factory.newDeclarator(factory.newName(name));
		}
		
		return returnedDeclarator;
	}

	/**
	 * Checks if a given type isn't described by the {@link IASTDeclarator} (only by the {@link IASTDeclSpecifier}).
	 * @param type the type to check
	 * @return true if irrelevant
	 */
	private boolean typeIrrelevantForDeclarator(IType type) {
		return type instanceof IBasicType || type instanceof ICompositeType || type instanceof IQualifierType || type instanceof ITypedef;
	}

	/**
	 * Handles the creation of declarators of pointers, references, arrays and arrays of pointers/references.
	 * @param type the topmost type, expected {@link IPointerType}, {@link ICPPReferenceType} or {@link IArrayType}
	 * @param name the declarator name
	 * @return Either a {@link CPPASTDeclarator} or a {@link CPPASTArrayDeclarator}, depending on what's needed
	 * @throws DOMException
	 */
	private IASTDeclarator createPointerOrArrayDeclarator(IType type, char[] name) throws DOMException {
		List<IASTPointerOperator> ptrs = new ArrayList<IASTPointerOperator>();
		List<IASTExpression> arrs = new ArrayList<IASTExpression>();
		while (type instanceof IPointerType || type instanceof ICPPReferenceType) {
			IASTPointerOperator astPtr;
			if (type instanceof IPointerType) {
				IASTPointer cppastPointer = factory.newPointer();
				IPointerType ptrType = (IPointerType) type;
				cppastPointer.setConst(ptrType.isConst());
				cppastPointer.setVolatile(ptrType.isVolatile());
				astPtr = cppastPointer;
				type = ptrType.getType();
			} else {
				ICPPASTReferenceOperator cppastReferenceOperator = ((ICPPNodeFactory)factory).newReferenceOperator(false);
				ICPPReferenceType refType = (ICPPReferenceType) type;
				astPtr = cppastReferenceOperator;
				type = refType.getType();
	
			}
			ptrs.add(astPtr);
		}
		while (type instanceof IArrayType) {
			IArrayType arrType = (IArrayType) type;
			IASTExpression arraySizeExpression = arrType.getArraySizeExpression();
			arrs.add(arraySizeExpression);
			type = arrType.getType();
		}
	
		IASTDeclarator decl;
		if (!arrs.isEmpty()) {
			IASTArrayDeclarator arrayDeclarator = factory.newArrayDeclarator(null);
			for (IASTExpression exp : arrs) {
				IASTArrayModifier arrayModifier = factory.newArrayModifier((exp == null) ? exp : exp.copy());
				arrayDeclarator.addArrayModifier(arrayModifier);
			}
			decl = arrayDeclarator;
		} else {
			decl = factory.newDeclarator(null);
		}
		for (IASTPointerOperator ptr : ptrs) {
			decl.addPointerOperator(ptr);
		}
	
		createDeclaratorContent(decl, type, name);
		
		return decl;
	}

	/**
	 * Fills a given declarator with either an ASTName or a proper nested declarator
	 * @param decl The declarator to fill
	 * @param type the content type of declarator
	 * @param name the desired name of declarator tree
	 * @throws DOMException 
	 */
	private void createDeclaratorContent(IASTDeclarator decl, IType type, char[] name) throws DOMException {
		if (type instanceof IPointerType || type instanceof IArrayType || type instanceof ICPPReferenceType) {
			IASTDeclarator nested = createPointerOrArrayDeclarator(type, name);
			decl.setNestedDeclarator(nested);
		} else if (typeIrrelevantForDeclarator(type)) {
			IASTName astName = factory.newName(name);
			decl.setName(astName);
		}
	}

	private IASTNamedTypeSpecifier getDeclSpecForBinding(IBinding binding) {
	
		char[][] qualifiedNameCharArray = CPPVisitor.getQualifiedNameCharArray(binding);
		if (qualifiedNameCharArray.length > 1) {
			
			ICPPASTQualifiedName name = ((ICPPNodeFactory)factory).newQualifiedName();
			for (char[] cs : qualifiedNameCharArray) {
				name.addName(factory.newName(cs));
			}
			return factory.newTypedefNameSpecifier(name);
			
		} else if (qualifiedNameCharArray.length == 1) {
			IASTName name = factory.newName(qualifiedNameCharArray[0]);
			return factory.newTypedefNameSpecifier(name);
		} else {
			IASTName name = factory.newName(binding.getName().toCharArray());
			return factory.newTypedefNameSpecifier(name);
		}
	}

}