/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.model;

import org.eclipse.cdt.core.dom.ast.ASTSignatureUtil;
import org.eclipse.cdt.core.dom.ast.IASTArrayDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionList;
import org.eclipse.cdt.core.dom.ast.IASTFieldDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerList;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTPointer;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStandardFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.c.ICASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTDesignatedInitializer;
import org.eclipse.cdt.core.dom.ast.c.ICASTPointer;
import org.eclipse.cdt.core.dom.ast.c.ICASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionTryBlockDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTPointerToMember;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTReferenceOperator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplatedTypeTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTypenameExpression;
import org.eclipse.cdt.core.dom.ast.gnu.c.ICASTKnRFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPASTPointer;
import org.eclipse.cdt.core.parser.Keywords;


/**
 * This is a utility class to help convert AST elements to Strings.
 * 
 * @see org.eclipse.cdt.core.dom.ast.ASTSignatureUtil
 * @see org.eclipse.cdt.core.dom.ast.ASTTypeUtil
 */

public class ASTStringUtil {
	
	private static final String COMMA_SPACE= ", "; //$NON-NLS-1$
	private static final String[] EMPTY_STRING_ARRAY= new String[0];

	/**
	 * Return the qualified name if the given <code>IASTName</code> 
	 * is an <code>ICPPASTQualifiedName</code>, otherwise a simple name is returned.
	 * 
	 * @param name
	 * @return a (possibly) qualified name
	 */
	public static String getQualifiedName(IASTName name) {
		return appendQualifiedNameString(new StringBuffer(), name).toString();
	}

	/**
	 * Return the non-qualified name.
	 * 
	 * @param name
	 * @return a non-qualified name
	 */
	public static String getSimpleName(IASTName name) {
		return appendSimpleNameString(new StringBuffer(), name).toString();
	}

	/**
	 * Compute a signature string with parameters, without initializers.
	 * 
	 * @param declarator
	 * @return the type string
	 * @see ASTSignatureUtil#getSignature(IASTDeclarator)
	 */
	public static String getSignatureString(IASTDeclarator declarator) {
		return trimRight(appendSignatureString(new StringBuffer(), declarator)).toString();
	}

	/**
	 * Compute a signature string including parameters, but without initializers.
	 * 
	 * @param declSpecifier
	 * @param declarator
	 * @return the signature string
	 */
	public static String getSignatureString(IASTDeclSpecifier declSpecifier, IASTDeclarator declarator) {
		final StringBuffer buffer= new StringBuffer();
		appendDeclarationString(buffer, declSpecifier, declarator, true);
		return trimRight(buffer).toString();
	}

	/**
	 * Compute a (return-)type string without parameters and initializers.
	 * 
	 * @param declSpecifier
	 * @param declarator
	 * @return the type string
	 */
	public static String getTypeString(IASTDeclSpecifier declSpecifier, IASTDeclarator declarator) {
		final StringBuffer buffer= new StringBuffer();
		appendDeclarationString(buffer, declSpecifier, declarator, false);
		return trimRight(buffer).toString();
	}

	/**
	 * Get the signatures of the function parameter declarations.
	 * 
	 * @param functionDeclarator
	 * @return the parameter signature array
	 * 
	 * @see ASTSignatureUtil#getParameterSignatureArray(IASTDeclarator)
	 */
	public static String[] getParameterSignatureArray(IASTFunctionDeclarator functionDeclarator) {
		if (functionDeclarator instanceof IASTStandardFunctionDeclarator) {
			final IASTStandardFunctionDeclarator standardFunctionDecl= (IASTStandardFunctionDeclarator)functionDeclarator;
			final IASTParameterDeclaration[] parameters= standardFunctionDecl.getParameters();
			final boolean takesVarArgs= standardFunctionDecl.takesVarArgs();
			final String[] parameterStrings= new String[parameters.length + (takesVarArgs ? 1 : 0)];
			int i;
			for (i=0; i < parameters.length; ++i) {
				parameterStrings[i]= getParameterSignatureString(parameters[i]);
			}
			if (takesVarArgs) {
				parameterStrings[i]= new String(Keywords.cpELLIPSIS);
			}
			return parameterStrings;
		} else if (functionDeclarator instanceof ICASTKnRFunctionDeclarator) {
			final ICASTKnRFunctionDeclarator knrDeclarator= (ICASTKnRFunctionDeclarator)functionDeclarator;
			final IASTName[] names= knrDeclarator.getParameterNames();
			final String[] result= new String[names.length];
			for(int i=0; i<names.length; i++) {
				if (names[i] != null) {
					final IASTDeclarator declaratorForParameterName= knrDeclarator.getDeclaratorForParameterName(names[i]);
                    if( declaratorForParameterName != null )
                        result[i]= getSignatureString(declaratorForParameterName);
				}
			}
			return result;
		}
		return EMPTY_STRING_ARRAY;
	}

	/**
	 * Convert the given template parameters into a string array.
	 * 
	 * @param templateParams
	 * @return a string array of template parameters
	 */
	public static String[] getTemplateParameterArray(ICPPASTTemplateParameter[] templateParams){
		final String[] parameterTypes= new String[templateParams.length];
		for (int i= 0; i < templateParams.length; i++) {
			final StringBuffer paramType= new StringBuffer();
			final ICPPASTTemplateParameter parameter= templateParams[i];
			appendTemplateParameterString(paramType, parameter);
			parameterTypes[i]= trimRight(paramType).toString();
		}
		return parameterTypes;		
	}

	private static String getParameterSignatureString(IASTParameterDeclaration parameterDeclaration) {
		return trimRight(appendParameterDeclarationString(new StringBuffer(), parameterDeclaration)).toString();
	}

	private static StringBuffer appendSignatureString(StringBuffer buffer, IASTDeclarator declarator) {
		// get the declaration node
		IASTNode node= declarator.getParent();
		while(node instanceof IASTDeclarator ){
			declarator= (IASTDeclarator)node;
			node= node.getParent();
		}
		
		// get the declSpec
		final IASTDeclSpecifier declSpec;
		if(node instanceof IASTParameterDeclaration)
			declSpec= ((IASTParameterDeclaration) node).getDeclSpecifier();
		else if(node instanceof IASTSimpleDeclaration)
			declSpec= ((IASTSimpleDeclaration)node).getDeclSpecifier();
		else if(node instanceof IASTFunctionDefinition)
			declSpec= ((IASTFunctionDefinition)node).getDeclSpecifier();
		else if(node instanceof IASTTypeId)
		    declSpec= ((IASTTypeId)node).getDeclSpecifier();
		else
			declSpec= null;

		return appendDeclarationString(buffer, declSpec, declarator, true);
	}

	private static StringBuffer appendDeclarationString(StringBuffer buffer, IASTDeclSpecifier declSpecifier,  IASTDeclarator declarator, boolean addParams) {
		if (declSpecifier != null) {
			appendDeclSpecifierString(buffer, declSpecifier);
			trimRight(buffer);
		}
		appendDeclaratorString(buffer, declarator, addParams);
		return buffer;
	}

	private static StringBuffer appendDeclaratorString(StringBuffer buffer, IASTDeclarator declarator, boolean addParams) {
		if (declarator == null) {
			return buffer;
		}
		appendPointerOperatorsString(buffer, declarator.getPointerOperators());
		if (declarator instanceof IASTArrayDeclarator) {
			appendArrayQualifiersString(buffer, (IASTArrayDeclarator)declarator);
		}
		final IASTDeclarator nestedDeclarator= declarator.getNestedDeclarator();
		if (nestedDeclarator != null) {
			StringBuffer tmp= new StringBuffer();
			appendDeclaratorString(tmp, nestedDeclarator, addParams);
			trimRight(tmp);
			final int tmpLength= tmp.length();
			if (tmpLength > 0) {
				if (tmp.charAt(0) != Keywords.cpLPAREN[0] || tmp.charAt(tmpLength-1) != Keywords.cpRPAREN[0]) {
					buffer.append(Keywords.cpLPAREN);
					buffer.append(tmp);
					buffer.append(Keywords.cpRPAREN);
				}
				else {
					buffer.append(tmp);
				}
			}
		}
		if (!addParams) {
			return buffer;
		}
		if (declarator instanceof IASTFunctionDeclarator) {
			final IASTFunctionDeclarator functionDecl= (IASTFunctionDeclarator)declarator;
			appendParameterSignatureString(buffer, functionDecl);
			if (declarator instanceof ICPPASTFunctionDeclarator) {
				final ICPPASTFunctionDeclarator cppFunctionDecl= (ICPPASTFunctionDeclarator)declarator;
				if (cppFunctionDecl.isConst()) {
					buffer.append(Keywords.CONST).append(' ');
				}
				if (cppFunctionDecl.isVolatile()) {
					buffer.append(Keywords.VOLATILE).append(' ');
				}
				if (cppFunctionDecl.isPureVirtual()) {
					buffer.append("=0 "); //$NON-NLS-1$
				}
				final IASTTypeId[] exceptionTypeIds= cppFunctionDecl.getExceptionSpecification();
				if (exceptionTypeIds.length > 0) {
					buffer.append(Keywords.THROW).append(' ');
					for (int i= 0; i < exceptionTypeIds.length; i++) {
						if (i > 0) {
							buffer.append(COMMA_SPACE);
						}
						appendTypeIdString(buffer, exceptionTypeIds[i]);
					}
				}
				if (declarator instanceof ICPPASTFunctionTryBlockDeclarator) {
					assert false : "TODO: handle "+ declarator.getClass().getName(); //$NON-NLS-1$
				}
			}
		} else if (declarator instanceof IASTFieldDeclarator) {
			final IASTFieldDeclarator fieldDeclarator= (IASTFieldDeclarator)declarator;
			final IASTExpression bitFieldSize= fieldDeclarator.getBitFieldSize();
			if (bitFieldSize != null) {
				buffer.append(Keywords.cpCOLON);
				appendExpressionString(buffer, bitFieldSize);
			}
		} else {
//			assert false : "TODO: handle "+ declarator.getClass().getName(); //$NON-NLS-1$
		}
		return buffer;
	}

	private static StringBuffer appendInitializerString(StringBuffer buffer, IASTInitializer initializer) {
		if (initializer instanceof IASTInitializerExpression) {
			final IASTInitializerExpression initializerExpression= (IASTInitializerExpression)initializer;
			buffer.append(Keywords.cpASSIGN);
			appendExpressionString(buffer, initializerExpression.getExpression());
		} else if (initializer instanceof IASTInitializerList) {
			final IASTInitializerList initializerList= (IASTInitializerList)initializer;
			final IASTInitializer[] initializers= initializerList.getInitializers();
			buffer.append(Keywords.cpASSIGN);
			buffer.append(Keywords.cpLBRACE);
			for (int i= 0; i < initializers.length; i++) {
				if (i > 0) {
					buffer.append(COMMA_SPACE);
				}
				appendInitializerString(buffer, initializers[i]);
			}
			trimRight(buffer);
			buffer.append(Keywords.cpRBRACE);
		} else if (initializer instanceof ICASTDesignatedInitializer) {
			//TODO handle ICASTDesignatedInitializer?
//			final ICASTDesignatedInitializer designatedInitializer= (ICASTDesignatedInitializer)initializer;
//			final ICASTDesignator[] designator= designatedInitializer.getDesignators();
		} else if (initializer instanceof ICPPASTConstructorInitializer) {
			final ICPPASTConstructorInitializer constructorInitializer= (ICPPASTConstructorInitializer)initializer;
			final IASTExpression expression= constructorInitializer.getExpression();
			buffer.append(Keywords.cpLPAREN);
			appendExpressionString(buffer, expression);
			trimRight(buffer);
			buffer.append(Keywords.cpRPAREN);
		} else if (initializer != null) {
			assert false : "TODO: handle "+ initializer.getClass().getName(); //$NON-NLS-1$
		}
		return buffer;
	}

	private static StringBuffer appendTypeIdString(StringBuffer buffer, IASTTypeId typeId) {
		appendDeclSpecifierString(buffer, typeId.getDeclSpecifier());
		appendDeclaratorString(buffer, typeId.getAbstractDeclarator(), true);
		return buffer;
	}

	private static StringBuffer trimRight(StringBuffer buffer) {
		int length= buffer.length();
		while (length > 0 && buffer.charAt(length - 1) == ' ') {
			--length;
		}
		buffer.setLength(length);
		return buffer;
	}

	private static StringBuffer appendArrayQualifiersString(StringBuffer buffer, IASTArrayDeclarator declarator) {
		final IASTArrayModifier[] modifiers= declarator.getArrayModifiers();
		for (int i= 0; i < modifiers.length; i++) {
			buffer.append(Keywords.cpLBRACKET).append(Keywords.cpRBRACKET);
		}
		return buffer;
	}

	private static StringBuffer appendPointerOperatorsString(StringBuffer buffer, IASTPointerOperator[] pointerOperators) {
		for (int i= 0; i < pointerOperators.length; i++) {
			final IASTPointerOperator pointerOperator= pointerOperators[i];
			if (pointerOperator instanceof IASTPointer) {
				final IASTPointer pointer= (IASTPointer)pointerOperator;
				if (pointer instanceof ICPPASTPointerToMember) {
					final ICPPASTPointerToMember pointerToMember= (ICPPASTPointerToMember)pointer;
					appendQualifiedNameString(buffer, pointerToMember.getName());
				}
				buffer.append(Keywords.cpSTAR);
				if (pointer.isConst()) {
					buffer.append(' ').append(Keywords.CONST);
				}
				if (pointer.isVolatile()) {
					buffer.append(' ').append(Keywords.VOLATILE);
				}
				if (pointerOperator instanceof ICASTPointer) {
					final ICASTPointer cPointer= (ICASTPointer)pointerOperator;
					if (cPointer.isRestrict()) {
						buffer.append(' ').append(Keywords.RESTRICT);
					}
				} else if (pointerOperator instanceof IGPPASTPointer) {
					final IGPPASTPointer gppPointer= (IGPPASTPointer)pointerOperator;
					if (gppPointer.isRestrict()) {
						buffer.append(' ').append(Keywords.RESTRICT);
					}
				}
			} else if (pointerOperator instanceof ICPPASTReferenceOperator) {
				buffer.append(Keywords.cpAMPER);
			}
		} 
		return buffer;
	}

	private static StringBuffer appendParameterSignatureString(StringBuffer buffer, IASTFunctionDeclarator functionDeclarator) {
		if (functionDeclarator instanceof IASTStandardFunctionDeclarator) {
			final IASTStandardFunctionDeclarator standardFunctionDecl= (IASTStandardFunctionDeclarator)functionDeclarator;
			final IASTParameterDeclaration[] parameters= standardFunctionDecl.getParameters();
			final boolean takesVarArgs= standardFunctionDecl.takesVarArgs();
			buffer.append(Keywords.cpLPAREN);
			for (int i= 0; i < parameters.length; i++) {
				if (i > 0) {
					buffer.append(COMMA_SPACE);
				}
				appendParameterDeclarationString(buffer, parameters[i]);
			}
			if (takesVarArgs) {
				if (parameters.length > 0) {
					buffer.append(COMMA_SPACE);
				}
				buffer.append(Keywords.cpELLIPSIS);
			}
			trimRight(buffer);
			buffer.append(Keywords.cpRPAREN);
		} else if (functionDeclarator instanceof ICASTKnRFunctionDeclarator) {
			final ICASTKnRFunctionDeclarator knrDeclarator= (ICASTKnRFunctionDeclarator)functionDeclarator;
			final IASTName[] names= knrDeclarator.getParameterNames();
			for(int i=0; i<names.length; i++) {
				if (i > 0) {
					buffer.append(COMMA_SPACE);
				}
				if (names[i] != null) {
					final IASTDeclarator declaratorForParameterName= knrDeclarator.getDeclaratorForParameterName(names[i]);
                    if(declaratorForParameterName != null) {
                        appendSignatureString(buffer, declaratorForParameterName);
                    }
				}
			}
		}
		return buffer;
	}

	private static StringBuffer appendParameterDeclarationString(StringBuffer buffer, IASTParameterDeclaration parameter) {
		final IASTDeclSpecifier declSpecifier= parameter.getDeclSpecifier();
		if (declSpecifier != null) {
			appendDeclSpecifierString(buffer, declSpecifier);
			trimRight(buffer);
		}
		final IASTDeclarator declarator= parameter.getDeclarator();
		if (declarator != null) {
			appendDeclaratorString(buffer, declarator, true);
			appendInitializerString(buffer, declarator.getInitializer());
		}
		return buffer;
	}

	private static StringBuffer appendDeclSpecifierString(StringBuffer buffer, IASTDeclSpecifier declSpecifier) {
		if (declSpecifier.isConst()) {
			buffer.append(Keywords.CONST).append(' ');
		}
		if (declSpecifier.isVolatile()) {
			buffer.append(Keywords.VOLATILE).append(' ');
		}
//		if (declSpecifier.isInline()) {
//			buffer.append(Keywords.INLINE).append(' ');
//		}
		if (declSpecifier instanceof ICASTDeclSpecifier) {
			final ICASTDeclSpecifier cDeclSpec= (ICASTDeclSpecifier)declSpecifier;
			if (cDeclSpec.isRestrict()) {
				buffer.append(Keywords.RESTRICT).append(' ');
			}
		} else if (declSpecifier instanceof ICPPASTDeclSpecifier) {
			final ICPPASTDeclSpecifier cppDeclSpec= (ICPPASTDeclSpecifier)declSpecifier;
			if (cppDeclSpec.isFriend()) {
				buffer.append(Keywords.FRIEND).append(' ');
			}
			if (cppDeclSpec.isVirtual()) {
				buffer.append(Keywords.VIRTUAL).append(' ');
			}
			if (cppDeclSpec.isExplicit()) {
				buffer.append(Keywords.EXPLICIT).append(' ');
			}
			if (declSpecifier instanceof IGPPASTDeclSpecifier) {
				final IGPPASTDeclSpecifier gppDeclSpec= (IGPPASTDeclSpecifier)declSpecifier;
				if (gppDeclSpec.isRestrict()) {
					buffer.append(Keywords.RESTRICT).append(' ');
				}
			}
		}
		// storage class
//		final int storageClass= declSpecifier.getStorageClass();
//		switch (storageClass) {
//		case IASTDeclSpecifier.sc_typedef:
//			buffer.append(Keywords.TYPEDEF).append(' ');
//			break;
//		case IASTDeclSpecifier.sc_extern:
//			buffer.append(Keywords.EXTERN).append(' ');
//			break;
//		case IASTDeclSpecifier.sc_static:
//			buffer.append(Keywords.STATIC).append(' ');
//			break;
//		case IASTDeclSpecifier.sc_auto:
//			buffer.append(Keywords.AUTO).append(' ');
//			break;
//		case IASTDeclSpecifier.sc_register:
//			buffer.append(Keywords.REGISTER).append(' ');
//			break;
//		case ICPPASTDeclSpecifier.sc_mutable:
//			buffer.append(Keywords.MUTABLE).append(' ');
//			break;
//		}
		if (declSpecifier instanceof IASTCompositeTypeSpecifier) {
			final IASTCompositeTypeSpecifier compositeTypeSpec= (IASTCompositeTypeSpecifier)declSpecifier;
			final int key= compositeTypeSpec.getKey();
			switch (key) {
			case IASTCompositeTypeSpecifier.k_struct:
				buffer.append(Keywords.STRUCT).append(' ');
				break;
			case IASTCompositeTypeSpecifier.k_union:
				buffer.append(Keywords.UNION).append(' ');
				break;
			case ICPPASTCompositeTypeSpecifier.k_class:
				buffer.append(Keywords.CLASS).append(' ');
				break;
			default:
			}
			appendQualifiedNameString(buffer, compositeTypeSpec.getName());
		} else if (declSpecifier instanceof IASTElaboratedTypeSpecifier) {
			final IASTElaboratedTypeSpecifier elaboratedTypeSpec= (IASTElaboratedTypeSpecifier)declSpecifier;
			switch (elaboratedTypeSpec.getKind()) {
			case IASTElaboratedTypeSpecifier.k_enum:
				buffer.append(Keywords.ENUM).append(' ');
				break;
			case IASTElaboratedTypeSpecifier.k_struct:
				buffer.append(Keywords.STRUCT).append(' ');
				break;
			case IASTElaboratedTypeSpecifier.k_union:
				buffer.append(Keywords.UNION).append(' ');
				break;
			case ICPPASTElaboratedTypeSpecifier.k_class:
				buffer.append(Keywords.CLASS).append(' ');
				break;
			default:
				assert false;
			}
			appendQualifiedNameString(buffer, elaboratedTypeSpec.getName());
		} else if (declSpecifier instanceof IASTEnumerationSpecifier) {
			final IASTEnumerationSpecifier enumerationSpec= (IASTEnumerationSpecifier)declSpecifier;
			buffer.append(Keywords.ENUM).append(' ');
			appendQualifiedNameString(buffer, enumerationSpec.getName());
		} else if (declSpecifier instanceof IASTSimpleDeclSpecifier) {
			final IASTSimpleDeclSpecifier simpleDeclSpec= (IASTSimpleDeclSpecifier)declSpecifier;
			if (simpleDeclSpec.isSigned()) {
				buffer.append(Keywords.SIGNED).append(' ');
			}
			if (simpleDeclSpec.isUnsigned()) {
				buffer.append(Keywords.UNSIGNED).append(' ');
			}
			if (simpleDeclSpec.isShort()) {
				buffer.append(Keywords.SHORT).append(' ');
			}
			if (simpleDeclSpec.isLong()) {
				buffer.append(Keywords.LONG).append(' ');
			}
			if (simpleDeclSpec instanceof ICASTSimpleDeclSpecifier) {
				final ICASTSimpleDeclSpecifier cSimpleDeclSpec= (ICASTSimpleDeclSpecifier)simpleDeclSpec;
				if (cSimpleDeclSpec.isLongLong()) {
					buffer.append(Keywords.LONG_LONG).append(' ');
				}
				if (cSimpleDeclSpec.isComplex()) {
					buffer.append(Keywords._COMPLEX).append(' ');
				}
				if (cSimpleDeclSpec.isImaginary()) {
					buffer.append(Keywords._IMAGINARY).append(' ');
				}
				switch (simpleDeclSpec.getType()) {
				case ICASTSimpleDeclSpecifier.t_Bool:
					buffer.append(Keywords._BOOL).append(' ');
					break;
				}
			}
			switch (simpleDeclSpec.getType()) {
			case IASTSimpleDeclSpecifier.t_void:
				buffer.append(Keywords.VOID).append(' ');
				break;
			case IASTSimpleDeclSpecifier.t_char:
				buffer.append(Keywords.CHAR).append(' ');
				break;
			case IASTSimpleDeclSpecifier.t_int:
				buffer.append(Keywords.INT).append(' ');
				break;
			case IASTSimpleDeclSpecifier.t_float:
				buffer.append(Keywords.FLOAT).append(' ');
				break;
			case IASTSimpleDeclSpecifier.t_double:
				buffer.append(Keywords.DOUBLE).append(' ');
				break;
			case ICPPASTSimpleDeclSpecifier.t_bool:
				buffer.append(Keywords.BOOL).append(' ');
				break;
			case ICPPASTSimpleDeclSpecifier.t_wchar_t:
				buffer.append(Keywords.WCHAR_T).append(' ');
				break;
			default:
			}
		} else if (declSpecifier instanceof IASTNamedTypeSpecifier) {
			final IASTNamedTypeSpecifier namedTypeSpec= (IASTNamedTypeSpecifier)declSpecifier;
			appendQualifiedNameString(buffer, namedTypeSpec.getName());
		}
		return buffer;
	}

	private static StringBuffer appendQualifiedNameString(StringBuffer buffer, IASTName name) {
		return appendNameString(buffer, name, true);
	}

	private static StringBuffer appendSimpleNameString(StringBuffer buffer, IASTName name) {
		return appendNameString(buffer, name, false);
	}

	private static StringBuffer appendNameString(StringBuffer buffer, IASTName name, boolean qualified) {
		if (name instanceof ICPPASTQualifiedName) {
			final ICPPASTQualifiedName qualifiedName= (ICPPASTQualifiedName)name;
			if (qualified) {
				final IASTName[] names= qualifiedName.getNames();
				for (int i= 0; i < names.length; i++) {
					if (i > 0) {
						buffer.append(Keywords.cpCOLONCOLON);
					}
					appendQualifiedNameString(buffer, names[i]);
				}
			} else {
				buffer.append(qualifiedName.getLastName());
			}
		} else if (name instanceof ICPPASTTemplateId) {
			final ICPPASTTemplateId templateId= (ICPPASTTemplateId)name;
			appendQualifiedNameString(buffer, templateId.getTemplateName());
			final IASTNode[] templateArguments= templateId.getTemplateArguments();
			buffer.append(Keywords.cpLT);
			for (int i= 0; i < templateArguments.length; i++) {
				if (i > 0) {
					buffer.append(Keywords.cpCOMMA);
				}
				final IASTNode argument= templateArguments[i];
				if (argument instanceof IASTTypeId) {
					appendTypeIdString(buffer, (IASTTypeId)argument);
				} else if (argument instanceof IASTExpression) {
					final IASTExpression expression= (IASTExpression)argument;
					appendExpressionString(buffer, expression);
				}
				trimRight(buffer);
			}
			buffer.append(Keywords.cpGT);
		} else if (name != null) {
			buffer.append(name.toCharArray());
		}
		return buffer;
	}

	private static StringBuffer appendExpressionString(StringBuffer buffer, IASTExpression expression) {
		if (expression instanceof IASTIdExpression) {
			final IASTIdExpression idExpression= (IASTIdExpression)expression;
			appendQualifiedNameString(buffer, idExpression.getName());
		} else if (expression instanceof IASTExpressionList) {
			final IASTExpressionList expressionList= (IASTExpressionList)expression;
			final IASTExpression[] expressions= expressionList.getExpressions();
			for (int i= 0; i < expressions.length; i++) {
				if (i > 0) {
					buffer.append(COMMA_SPACE);
				}
				appendExpressionString(buffer, expressions[i]);
			}
		} else if (expression instanceof ICPPASTTypenameExpression) {
			final ICPPASTTypenameExpression typenameExpression= (ICPPASTTypenameExpression)expression;
			buffer.append(Keywords.TYPENAME).append(' ');
			appendQualifiedNameString(buffer, typenameExpression.getName());
			final IASTExpression initialValue= typenameExpression.getInitialValue();
			if (initialValue != null) {
				buffer.append(Keywords.cpASSIGN);
				appendExpressionString(buffer, initialValue);
			}
		} else if (expression instanceof IASTLiteralExpression) {
			buffer.append(ASTSignatureUtil.getExpressionString(expression));
		} else if (expression != null) {
			buffer.append(ASTSignatureUtil.getExpressionString(expression));
		}
		return buffer;
	}

	private static StringBuffer appendTemplateParameterString(StringBuffer buffer, ICPPASTTemplateParameter parameter){
		if (parameter instanceof ICPPASTParameterDeclaration) {
			appendParameterDeclarationString(buffer, (ICPPASTParameterDeclaration)parameter);
		} else if (parameter instanceof ICPPASTSimpleTypeTemplateParameter) {
			final ICPPASTSimpleTypeTemplateParameter simpletypeParameter= (ICPPASTSimpleTypeTemplateParameter)parameter;
			final IASTName name= simpletypeParameter.getName();
			if (name != null) {
				appendSimpleNameString(buffer, name);
			} else {
				final int type= simpletypeParameter.getParameterType();
				switch (type) {
				case ICPPASTSimpleTypeTemplateParameter.st_class:
					buffer.append(Keywords.CLASS);
					break;
				case ICPPASTSimpleTypeTemplateParameter.st_typename:
					buffer.append(Keywords.TYPENAME);
					break;
				}
			}
		} else if (parameter instanceof ICPPASTTemplatedTypeTemplateParameter) {
			final ICPPASTTemplatedTypeTemplateParameter templatedTypeParameter= (ICPPASTTemplatedTypeTemplateParameter)parameter;
			final ICPPASTTemplateParameter[] subParameters= templatedTypeParameter.getTemplateParameters();
			buffer.append(Keywords.TEMPLATE).append(Keywords.cpLT);
			for (int i= 0; i < subParameters.length; i++) {
				final ICPPASTTemplateParameter templateParameter= subParameters[i];
				if (i > 0) {
					buffer.append(COMMA_SPACE);
				}
				appendTemplateParameterString(buffer, templateParameter);
			}
			trimRight(buffer);
			buffer.append(Keywords.cpGT);
		}
		return buffer;
	}	
	
}
