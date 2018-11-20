/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Rational Software - initial implementation
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

import org.eclipse.cdt.core.dom.ast.c.ICASTArrayDesignator;
import org.eclipse.cdt.core.dom.ast.c.ICASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTDesignatedInitializer;
import org.eclipse.cdt.core.dom.ast.c.ICASTDesignator;
import org.eclipse.cdt.core.dom.ast.c.ICASTFieldDesignator;
import org.eclipse.cdt.core.dom.ast.c.ICASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTTypeIdInitializerExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCastExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeleteExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTPackExpansionExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTReferenceOperator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeConstructorExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTypeIdExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.gnu.IGNUASTCompoundStatementExpression;
import org.eclipse.cdt.core.dom.ast.gnu.IGNUASTTypeIdExpression;
import org.eclipse.cdt.core.dom.ast.gnu.c.ICASTKnRFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.gnu.c.IGCCASTArrayRangeDesignator;
import org.eclipse.cdt.core.parser.GCCKeywords;
import org.eclipse.cdt.core.parser.Keywords;
import org.eclipse.cdt.internal.core.dom.parser.ASTProblem;
import org.eclipse.cdt.internal.core.model.ASTStringUtil;

/**
 * This is a utility class to help convert AST elements to Strings corresponding to the AST
 * element's signature.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @deprecated The class is provided for testing purposes, only. It should not be used by clients.
 * Within CDT it is recommended to use {@link ASTStringUtil}, instead.
 */
@Deprecated
public class ASTSignatureUtil {
	private static final String COMMA_SPACE = ", "; //$NON-NLS-1$
	private static final String EMPTY_STRING = ""; //$NON-NLS-1$
	private static final String SPACE = " "; //$NON-NLS-1$
	private static final String[] EMPTY_STRING_ARRAY = {};

	/**
	 * Return's the String representation of a node's type (if available). This is currently only
	 * being used for testing.
	 *
	 * @param node
	 */
	public static String getNodeSignature(IASTNode node) {
		if (node instanceof IASTDeclarator)
			return getSignature((IASTDeclarator) node);
		if (node instanceof IASTDeclSpecifier)
			return getSignature((IASTDeclSpecifier) node);
		if (node instanceof IASTTypeId)
			return getSignature((IASTTypeId) node);
		if (node instanceof IASTSimpleDeclaration) {
			IASTSimpleDeclaration decl = (IASTSimpleDeclaration) node;
			StringBuilder buffer = new StringBuilder(getSignature(decl.getDeclSpecifier()));

			IASTDeclarator[] declarators = decl.getDeclarators();
			for (int i = 0; i < declarators.length; ++i) {
				buffer.append(SPACE);
				buffer.append(getSignature(declarators[i]));
				if (declarators[i].getInitializer() != null
						&& declarators[i].getInitializer() instanceof ICPPASTConstructorInitializer) {
					buffer.append(getInitializerString(declarators[i].getInitializer()));
				}
			}
			buffer.append(";"); //$NON-NLS-1$
			return buffer.toString();
		}
		if (node instanceof IASTExpression) {
			return getExpressionString((IASTExpression) node);
		}

		return EMPTY_STRING;
	}

	/**
	 * Returns the parameter signature for an IASTDeclarator as a comma separated list wrapped in parenthesis.
	 *
	 * This method uses ASTSignatureUtil#getParametersSignatureArray(IASTArray) to build a comma separated
	 * list of the parameter's signatures and then wraps them in parenthesis.
	 *
	 * @param decltor
	 * @return the parameter signature for an IASTDeclarator as a comma separated list wrapped in parenthesis
	 */
	// if only function declarator's have parameters then change this to function declarator... should check
	// before starting.. make my life easier!
	public static String getParameterSignature(IASTDeclarator decltor) {
		// should only be working with decltor that has parms
		if (!(decltor instanceof IASTStandardFunctionDeclarator || decltor instanceof ICASTKnRFunctionDeclarator))
			return EMPTY_STRING;

		StringBuilder result = new StringBuilder();

		String[] parms = getParameterSignatureArray(decltor);

		result.append(Keywords.cpLPAREN);
		for (int i = 0; i < parms.length; i++) {
			if (parms[i] != null) {
				result.append(parms[i]);
				if (i < parms.length - 1)
					result.append(COMMA_SPACE);
			}
		}
		result.append(Keywords.cpRPAREN);

		return result.toString();
	}

	/**
	 * Returns a String[] corresponding to the signatures of individual parameters for an IASTDeclarator.
	 *
	 * @param decltor
	 * @return a String[] corresponding to the signatures of individual parameters for an IASTDeclarator
	 */
	public static String[] getParameterSignatureArray(IASTDeclarator decltor) {
		// should only be working with decltor that has parms
		if (!(decltor instanceof IASTStandardFunctionDeclarator || decltor instanceof ICASTKnRFunctionDeclarator))
			return EMPTY_STRING_ARRAY;

		String[] result = EMPTY_STRING_ARRAY;

		if (decltor instanceof IASTStandardFunctionDeclarator) {
			IASTParameterDeclaration[] parms = null;
			parms = ((IASTStandardFunctionDeclarator) decltor).getParameters();

			if (((IASTStandardFunctionDeclarator) decltor).takesVarArgs()) {
				result = new String[parms.length + 1];
				result[parms.length] = "..."; //$NON-NLS-1$
			} else {
				result = new String[parms.length];
			}

			for (int i = 0; i < parms.length; i++) {
				if (parms[i] != null) {
					result[i] = getSignature(parms[i].getDeclarator());
				}
			}
		} else if (decltor instanceof ICASTKnRFunctionDeclarator) {
			IASTName[] names = null;
			names = ((ICASTKnRFunctionDeclarator) decltor).getParameterNames(); // required to get the order
																				// the parameters are used

			result = new String[names.length];

			for (int i = 0; i < names.length; i++) {
				if (names[i] != null) {
					final IASTDeclarator declaratorForParameterName = ((ICASTKnRFunctionDeclarator) decltor)
							.getDeclaratorForParameterName(names[i]);
					if (declaratorForParameterName != null)
						result[i] = getSignature(declaratorForParameterName);
				}
			}
		}

		return result;
	}

	private static String getDeclaratorSpecificSignature(IASTDeclarator declarator) {
		StringBuilder result = new StringBuilder();
		IASTPointerOperator[] ops = declarator.getPointerOperators();
		boolean needSpace = false;

		if (ops != null && ops.length > 0) {
			for (IASTPointerOperator op : ops) {
				if (op != null) {
					if (needSpace) {
						result.append(SPACE);
					}
					if (op instanceof IASTPointer) {
						final IASTPointer ptr = (IASTPointer) op;
						result.append(Keywords.cpSTAR); // want to have this before keywords on the pointer
						needSpace = true;
						if (ptr.isConst()) {
							if (needSpace) {
								result.append(SPACE);
							}
							result.append(Keywords.CONST);
							needSpace = true;
						}
						if (ptr.isVolatile()) {
							if (needSpace) {
								result.append(SPACE);
							}
							result.append(Keywords.VOLATILE);
							needSpace = true;
						}
						if (ptr.isRestrict()) {
							if (needSpace) {
								result.append(SPACE);
							}
							result.append(Keywords.RESTRICT);
							needSpace = true;
						}
					}

					if (op instanceof ICPPASTReferenceOperator) {
						if (needSpace) {
							result.append(SPACE);
						}
						result.append(Keywords.cpAMPER);
						needSpace = true;
					}
				}
			}
		}

		if (declarator instanceof IASTArrayDeclarator) {
			IASTArrayModifier[] mods = ((IASTArrayDeclarator) declarator).getArrayModifiers();

			for (IASTArrayModifier mod : mods) {
				if (mod != null) {
					if (needSpace) {
						result.append(SPACE);
						needSpace = false;
					}
					result.append(Keywords.cpLBRACKET);
					if (mod instanceof ICASTArrayModifier) {
						if (((ICASTArrayModifier) mod).isConst()) {
							if (needSpace) {
								result.append(SPACE);
								needSpace = false;
							}
							result.append(Keywords.CONST);
							needSpace = true;
						}
						if (((ICASTArrayModifier) mod).isRestrict()) {
							if (needSpace) {
								result.append(SPACE);
								needSpace = false;
							}
							result.append(Keywords.RESTRICT);
							needSpace = true;
						}
						if (((ICASTArrayModifier) mod).isStatic()) {
							if (needSpace) {
								result.append(SPACE);
								needSpace = false;
							}
							result.append(Keywords.STATIC);
							needSpace = true;
						}
						if (((ICASTArrayModifier) mod).isVolatile()) {
							if (needSpace) {
								result.append(SPACE);
								needSpace = false;
							}
							result.append(Keywords.VOLATILE);
						}
					}
					result.append(Keywords.cpRBRACKET);
				}
			}
		}

		return result.toString();
	}

	private static String getDeclaratorSignature(IASTDeclarator declarator) {
		if (declarator == null)
			return EMPTY_STRING;

		StringBuilder result = new StringBuilder();

		result.append(getDeclaratorSpecificSignature(declarator));

		if (declarator.getNestedDeclarator() != null) {
			result.append(SPACE);
			result.append(Keywords.cpLPAREN);
			result.append(getDeclaratorSignature(declarator.getNestedDeclarator()));
			result.append(Keywords.cpRPAREN);
		}

		// append the parameter's signatures
		result.append(getParameterSignature(declarator));

		return result.toString();
	}

	/**
	 * This function is used to return the signature of an IASTInitializer.
	 *
	 * @param init an initializer
	 * @return the signature of an IASTInitializer
	 */
	public static String getInitializerString(IASTInitializer init) {
		StringBuilder result = new StringBuilder();

		if (init instanceof IASTEqualsInitializer) {
			result.append(Keywords.cpASSIGN);
			result.append(getInitializerClauseString(((IASTEqualsInitializer) init).getInitializerClause()));
		} else if (init instanceof IASTInitializerList) {
			result.append(Keywords.cpLBRACE);
			appendExpressionList(result, ((IASTInitializerList) init).getClauses());
			result.append(Keywords.cpRBRACE);
		} else if (init instanceof ICASTDesignatedInitializer) {
			ICASTDesignator[] designators = ((ICASTDesignatedInitializer) init).getDesignators();
			for (int i = 0; i < designators.length; i++) {
				result.append(getDesignatorSignature(designators[i]));
				if (i < designators.length - 1)
					result.append(COMMA_SPACE);
			}
			result.append(Keywords.cpASSIGN);
			result.append(getInitializerClauseString(((ICASTDesignatedInitializer) init).getOperand()));
		} else if (init instanceof ICPPASTConstructorInitializer) {
			result.append("("); //$NON-NLS-1$
			appendExpressionList(result, ((ICPPASTConstructorInitializer) init).getArguments());
			result.append(")"); //$NON-NLS-1$
		}

		return result.toString();
	}

	private static void appendExpressionList(StringBuilder result, IASTInitializerClause[] inits) {
		for (int i = 0; i < inits.length; i++) {
			result.append(getInitializerClauseString(inits[i]));
			if (i < inits.length - 1)
				result.append(COMMA_SPACE);
		}
	}

	private static String getInitializerClauseString(IASTInitializerClause initializerClause) {
		if (initializerClause instanceof IASTExpression) {
			return getExpressionString((IASTExpression) initializerClause);
		}
		if (initializerClause instanceof IASTInitializer) {
			return getInitializerString((IASTInitializer) initializerClause);
		}
		return ""; //$NON-NLS-1$
	}

	private static String getDesignatorSignature(ICASTDesignator designator) {
		StringBuilder result = new StringBuilder();

		if (designator instanceof ICASTArrayDesignator) {
			result.append(Keywords.cpLBRACKET);
			result.append(getExpressionString(((ICASTArrayDesignator) designator).getSubscriptExpression()));
			result.append(Keywords.cpRBRACKET);
		} else if (designator instanceof ICASTFieldDesignator) {
			result.append(Keywords.cpDOT);
			result.append(((ICASTFieldDesignator) designator).getName().toString());
		} else if (designator instanceof IGCCASTArrayRangeDesignator) {
			result.append(Keywords.cpLBRACKET);
			result.append(getExpressionString(((IGCCASTArrayRangeDesignator) designator).getRangeFloor()));
			result.append(SPACE);
			result.append(Keywords.cpELLIPSIS);
			result.append(SPACE);
			result.append(getExpressionString(((IGCCASTArrayRangeDesignator) designator).getRangeCeiling()));
			result.append(Keywords.cpRBRACKET);
		}

		return result.toString();
	}

	/**
	 * Returns the String signature corresponding to an IASTDeclarator. This includes the signature
	 * of the parameters which is built via ASTSignatureUtil#getParameterSignature(IASTDeclarator)
	 * if the declarator is for a function.
	 *
	 * @param declarator
	 * @return the String signature corresponding to an IASTDeclarator
	 */
	public static String getSignature(IASTDeclarator declarator) {
		StringBuilder result = new StringBuilder();

		// get the declSpec
		IASTDeclSpecifier declSpec = null;

		IASTNode node = declarator.getParent();
		while (node instanceof IASTDeclarator) {
			declarator = (IASTDeclarator) node;
			node = node.getParent();
		}

		if (node instanceof IASTParameterDeclaration)
			declSpec = ((IASTParameterDeclaration) node).getDeclSpecifier();
		else if (node instanceof IASTSimpleDeclaration)
			declSpec = ((IASTSimpleDeclaration) node).getDeclSpecifier();
		else if (node instanceof IASTFunctionDefinition)
			declSpec = ((IASTFunctionDefinition) node).getDeclSpecifier();
		else if (node instanceof IASTTypeId)
			declSpec = ((IASTTypeId) node).getDeclSpecifier();

		// append the declSpec's signature to the signature
		String specString = getSignature(declSpec);
		if (specString != null && !specString.equals(EMPTY_STRING))
			result.append(specString);

		// append the declarator's signature (without specifier)
		String decltorString = getDeclaratorSignature(declarator);
		if (specString != null && specString.length() > 0 && decltorString != null && decltorString.length() > 0) {
			result.append(SPACE);
		}
		result.append(decltorString);

		return result.toString();
	}

	/**
	 * Returns the String representation of the signature for the IASTDeclSpecifier.
	 *
	 * @param declSpec
	 * @return the String representation of the signature for the IASTDeclSpecifier
	 */
	public static String getSignature(IASTDeclSpecifier declSpec) {
		if (declSpec == null)
			return EMPTY_STRING;
		boolean needSpace = false;

		StringBuilder result = new StringBuilder();

		if (declSpec.getStorageClass() == IASTDeclSpecifier.sc_mutable) {
			result.append(Keywords.MUTABLE);
			needSpace = true;
		}
		if (declSpec.getStorageClass() == IASTDeclSpecifier.sc_auto) {
			if (needSpace) {
				result.append(SPACE);
				needSpace = false;
			}
			result.append(Keywords.AUTO);
			needSpace = true;
		}
		if (declSpec.getStorageClass() == IASTDeclSpecifier.sc_extern) {
			if (needSpace) {
				result.append(SPACE);
				needSpace = false;
			}
			result.append(Keywords.EXTERN);
			needSpace = true;
		}
		if (declSpec.getStorageClass() == IASTDeclSpecifier.sc_register) {
			if (needSpace) {
				result.append(SPACE);
				needSpace = false;
			}
			result.append(Keywords.REGISTER);
			needSpace = true;
		}
		if (declSpec.getStorageClass() == IASTDeclSpecifier.sc_static) {
			if (needSpace) {
				result.append(SPACE);
				needSpace = false;
			}
			result.append(Keywords.STATIC);
			needSpace = true;
		}
		if (declSpec.getStorageClass() == IASTDeclSpecifier.sc_typedef) {
			if (needSpace) {
				result.append(SPACE);
				needSpace = false;
			}
			result.append(Keywords.TYPEDEF);
			needSpace = true;
		}

		if (declSpec.isConst()) {
			if (needSpace) {
				result.append(SPACE);
				needSpace = false;
			}
			result.append(Keywords.CONST);
			needSpace = true;
		}
		if (declSpec.isInline()) {
			if (needSpace) {
				result.append(SPACE);
				needSpace = false;
			}
			result.append(Keywords.INLINE);
			needSpace = true;
		}
		if (declSpec.isRestrict()) {
			if (needSpace) {
				result.append(SPACE);
				needSpace = false;
			}
			result.append(Keywords.RESTRICT);
			needSpace = true;
		}
		if (declSpec.isVolatile()) {
			if (needSpace) {
				result.append(SPACE);
				needSpace = false;
			}
			result.append(Keywords.VOLATILE);
			needSpace = true;
		}

		if (declSpec instanceof ICPPASTDeclSpecifier) {
			ICPPASTDeclSpecifier cppDeclSpec = (ICPPASTDeclSpecifier) declSpec;
			if (cppDeclSpec.isThreadLocal()) {
				if (needSpace) {
					result.append(SPACE);
					needSpace = false;
				}
				result.append(Keywords.THREAD_LOCAL);
				needSpace = true;
			}
			if (cppDeclSpec.isConstexpr()) {
				if (needSpace) {
					result.append(SPACE);
					needSpace = false;
				}
				result.append(Keywords.CONSTEXPR);
				needSpace = true;
			}
			if (cppDeclSpec.isExplicit()) {
				if (needSpace) {
					result.append(SPACE);
					needSpace = false;
				}
				result.append(Keywords.EXPLICIT);
				needSpace = true;
			}
			if (cppDeclSpec.isFriend()) {
				if (needSpace) {
					result.append(SPACE);
					needSpace = false;
				}
				result.append(Keywords.FRIEND);
				needSpace = true;
			}
			if (cppDeclSpec.isVirtual()) {
				if (needSpace) {
					result.append(SPACE);
					needSpace = false;
				}
				result.append(Keywords.VIRTUAL);
				needSpace = true;
			}
		}

		// handle complex cases
		if (declSpec instanceof IASTCompositeTypeSpecifier) {
			// 101114 fix, do not display class, and for consistency don't display struct/union as well
			// if (declSpec instanceof ICPPASTCompositeTypeSpecifier) {
			// switch(((ICPPASTCompositeTypeSpecifier)declSpec).getKey()) {
			// case ICPPASTCompositeTypeSpecifier.k_class:
			// if (needSpace) { result.append(SPACE); needSpace=false; } result.append(Keywords.CLASS);
			// needSpace=true;
			// break;
			// case IASTCompositeTypeSpecifier.k_struct:
			// if (needSpace) { result.append(SPACE); needSpace=false; } result.append(Keywords.STRUCT);
			// needSpace=true;
			// break;
			// case IASTCompositeTypeSpecifier.k_union:
			// if (needSpace) { result.append(SPACE); needSpace=false; } result.append(Keywords.UNION);
			// needSpace=true;
			// break;
			// }
			// } else if (declSpec instanceof ICASTCompositeTypeSpecifier) {
			// switch(((ICASTCompositeTypeSpecifier)declSpec).getKey()) {
			// case IASTCompositeTypeSpecifier.k_struct:
			// if (needSpace) { result.append(SPACE); needSpace=false; } result.append(Keywords.STRUCT);
			// needSpace=true;
			// break;
			// case IASTCompositeTypeSpecifier.k_union:
			// if (needSpace) { result.append(SPACE); needSpace=false; } result.append(Keywords.UNION);
			// needSpace=true;
			// break;
			// }
			// }

			result.append(((IASTCompositeTypeSpecifier) declSpec).getName());
		} else if (declSpec instanceof IASTElaboratedTypeSpecifier) {
			// 101114 fix, do not display class, and for consistency don't display struct/union as well
			// switch(((IASTElaboratedTypeSpecifier)declSpec).getKind()) {
			// case ICPPASTElaboratedTypeSpecifier.k_class:
			// if (needSpace) { result.append(SPACE); needSpace=false; } result.append(Keywords.CLASS);
			// needSpace=true;
			// break;
			// case IASTElaboratedTypeSpecifier.k_enum:
			// if (needSpace) { result.append(SPACE); needSpace=false; } result.append(Keywords.ENUM);
			// needSpace=true;
			// break;
			// case IASTElaboratedTypeSpecifier.k_struct:
			// if (needSpace) { result.append(SPACE); needSpace=false; } result.append(Keywords.STRUCT);
			// needSpace=true;
			// break;
			// case IASTElaboratedTypeSpecifier.k_union:
			// if (needSpace) { result.append(SPACE); needSpace=false; } result.append(Keywords.UNION);
			// needSpace=true;
			// break;
			// }
			if (needSpace) {
				result.append(SPACE);
				needSpace = false;
			}
			result.append(((IASTElaboratedTypeSpecifier) declSpec).getName());
		} else if (declSpec instanceof IASTEnumerationSpecifier) {
			if (needSpace) {
				result.append(SPACE);
				needSpace = false;
			}
			result.append(Keywords.ENUM);
			needSpace = true;
		} else if (declSpec instanceof IASTNamedTypeSpecifier) {
			if (needSpace) {
				result.append(SPACE);
				needSpace = false;
			}
			result.append(((IASTNamedTypeSpecifier) declSpec).getName().toString());
			needSpace = true;
		} else if (declSpec instanceof IASTSimpleDeclSpecifier) {
			final IASTSimpleDeclSpecifier sds = (IASTSimpleDeclSpecifier) declSpec;
			if (sds.isLongLong()) {
				if (needSpace) {
					result.append(SPACE);
					needSpace = false;
				}
				result.append(Keywords.LONG_LONG);
				needSpace = true;
			}
			if (sds.isComplex()) {
				if (needSpace) {
					result.append(SPACE);
					needSpace = false;
				}
				result.append(Keywords.c_COMPLEX);
				needSpace = true;
			}
			if (sds.isImaginary()) {
				if (needSpace) {
					result.append(SPACE);
					needSpace = false;
				}
				result.append(Keywords.c_IMAGINARY);
				needSpace = true;
			}
			if (sds.isLong()) {
				if (needSpace) {
					result.append(SPACE);
					needSpace = false;
				}
				result.append(Keywords.LONG);
				needSpace = true;
			}
			if (sds.isShort()) {
				if (needSpace) {
					result.append(SPACE);
					needSpace = false;
				}
				result.append(Keywords.SHORT);
				needSpace = true;
			}
			if (sds.isSigned()) {
				if (needSpace) {
					result.append(SPACE);
					needSpace = false;
				}
				result.append(Keywords.SIGNED);
				needSpace = true;
			}
			if (sds.isUnsigned()) {
				if (needSpace) {
					result.append(SPACE);
					needSpace = false;
				}
				result.append(Keywords.UNSIGNED);
				needSpace = true;
			}

			switch (sds.getType()) {
			case IASTSimpleDeclSpecifier.t_typeof:
				if (needSpace) {
					result.append(SPACE);
					needSpace = false;
				}
				result.append(Keywords.TYPEOF);
				needSpace = true;
				break;
			case IASTSimpleDeclSpecifier.t_decltype:
				if (needSpace) {
					result.append(SPACE);
					needSpace = false;
				}
				result.append(Keywords.cDECLTYPE);
				needSpace = true;
				break;
			case IASTSimpleDeclSpecifier.t_auto:
				if (needSpace) {
					result.append(SPACE);
					needSpace = false;
				}
				result.append(Keywords.cAUTO);
				needSpace = true;
				break;
			case IASTSimpleDeclSpecifier.t_bool:
				if (needSpace) {
					result.append(SPACE);
					needSpace = false;
				}
				if (declSpec instanceof ICASTSimpleDeclSpecifier) {
					result.append(Keywords.c_BOOL);
				} else {
					result.append(Keywords.BOOL);
				}
				needSpace = true;
				break;
			case IASTSimpleDeclSpecifier.t_char:
				if (needSpace) {
					result.append(SPACE);
					needSpace = false;
				}
				result.append(Keywords.CHAR);
				needSpace = true;
				break;
			case IASTSimpleDeclSpecifier.t_wchar_t:
				if (needSpace) {
					result.append(SPACE);
					needSpace = false;
				}
				result.append(Keywords.WCHAR_T);
				needSpace = true;
				break;
			case IASTSimpleDeclSpecifier.t_char16_t:
				if (needSpace) {
					result.append(SPACE);
					needSpace = false;
				}
				result.append(Keywords.CHAR16_T);
				needSpace = true;
				break;
			case IASTSimpleDeclSpecifier.t_char32_t:
				if (needSpace) {
					result.append(SPACE);
					needSpace = false;
				}
				result.append(Keywords.CHAR32_T);
				needSpace = true;
				break;
			case IASTSimpleDeclSpecifier.t_double:
				if (needSpace) {
					result.append(SPACE);
					needSpace = false;
				}
				result.append(Keywords.DOUBLE);
				needSpace = true;
				break;
			case IASTSimpleDeclSpecifier.t_float:
				if (needSpace) {
					result.append(SPACE);
					needSpace = false;
				}
				result.append(Keywords.FLOAT);
				needSpace = true;
				break;
			case IASTSimpleDeclSpecifier.t_int:
				if (needSpace) {
					result.append(SPACE);
					needSpace = false;
				}
				result.append(Keywords.INT);
				needSpace = true;
				break;
			case IASTSimpleDeclSpecifier.t_int128:
				if (needSpace) {
					result.append(SPACE);
					needSpace = false;
				}
				result.append(GCCKeywords.__INT128);
				needSpace = true;
				break;
			case IASTSimpleDeclSpecifier.t_float128:
				if (needSpace) {
					result.append(SPACE);
					needSpace = false;
				}
				result.append(GCCKeywords.__FLOAT128);
				needSpace = true;
				break;
			case IASTSimpleDeclSpecifier.t_decimal32:
				if (needSpace) {
					result.append(SPACE);
					needSpace = false;
				}
				result.append(GCCKeywords._DECIMAL32);
				needSpace = true;
				break;
			case IASTSimpleDeclSpecifier.t_decimal64:
				if (needSpace) {
					result.append(SPACE);
					needSpace = false;
				}
				result.append(GCCKeywords._DECIMAL64);
				needSpace = true;
				break;
			case IASTSimpleDeclSpecifier.t_decimal128:
				if (needSpace) {
					result.append(SPACE);
					needSpace = false;
				}
				result.append(GCCKeywords._DECIMAL128);
				needSpace = true;
				break;
			case IASTSimpleDeclSpecifier.t_void:
				if (needSpace) {
					result.append(SPACE);
					needSpace = false;
				}
				result.append(Keywords.VOID);
				needSpace = true;
				break;
			}
		}

		return result.toString();
	}

	/**
	 * Returns the String representation of the signature for the IASTTypeId.
	 *
	 * @param typeId
	 * @return the String representation of the signature for the IASTTypeId
	 */
	public static String getSignature(IASTTypeId typeId) {
		return getSignature(typeId.getAbstractDeclarator());
	}

	/**
	 * Return a string representation for the given IASTExpression. Expressions having an extension kind
	 * should provide their own toString method which will be called by this.
	 *
	 * @param expression
	 * @return a string representation for the given IASTExpression
	 */
	public static String getExpressionString(IASTExpression expression) {
		if (expression instanceof IASTArraySubscriptExpression)
			return getArraySubscriptExpression((IASTArraySubscriptExpression) expression);
		else if (expression instanceof IASTBinaryExpression)
			return getBinaryExpression((IASTBinaryExpression) expression);
		else if (expression instanceof IASTCastExpression)
			return getCastExpression((IASTCastExpression) expression);
		else if (expression instanceof IASTConditionalExpression)
			return getConditionalExpression((IASTConditionalExpression) expression);
		else if (expression instanceof IASTExpressionList)
			return getExpressionList((IASTExpressionList) expression);
		else if (expression instanceof IASTFieldReference)
			return getFieldReference((IASTFieldReference) expression);
		else if (expression instanceof IASTFunctionCallExpression)
			return getFunctionCallExpression((IASTFunctionCallExpression) expression);
		else if (expression instanceof IASTIdExpression)
			return getIdExpression((IASTIdExpression) expression);
		else if (expression instanceof IASTLiteralExpression)
			return getLiteralExpression((IASTLiteralExpression) expression);
		else if (expression instanceof IASTTypeIdExpression)
			return getTypeIdExpression((IASTTypeIdExpression) expression);
		else if (expression instanceof IASTUnaryExpression)
			return getUnaryExpression((IASTUnaryExpression) expression);
		else if (expression instanceof ICASTTypeIdInitializerExpression)
			return getTypeIdInitializerExpression((ICASTTypeIdInitializerExpression) expression);
		else if (expression instanceof ICPPASTDeleteExpression)
			return getDeleteExpression((ICPPASTDeleteExpression) expression);
		else if (expression instanceof ICPPASTNewExpression)
			return getNewExpression((ICPPASTNewExpression) expression);
		else if (expression instanceof ICPPASTSimpleTypeConstructorExpression)
			return getSimpleTypeConstructorExpression((ICPPASTSimpleTypeConstructorExpression) expression);
		else if (expression instanceof IGNUASTCompoundStatementExpression)
			return getCompoundStatementExpression((IGNUASTCompoundStatementExpression) expression);
		else if (expression instanceof ICPPASTPackExpansionExpression)
			return getPackExpansionExpression((ICPPASTPackExpansionExpression) expression);

		return getEmptyExpression(expression);
	}

	private static String getArraySubscriptExpression(IASTArraySubscriptExpression expression) {
		StringBuilder result = new StringBuilder();
		result.append(getExpressionString(expression.getArrayExpression()));
		result.append(Keywords.cpLBRACKET);
		result.append(getInitializerClauseString(expression.getArgument()));
		result.append(Keywords.cpRBRACKET);
		return result.toString();
	}

	private static String getCastExpression(IASTCastExpression expression) {
		StringBuilder result = new StringBuilder();
		boolean normalCast = false;

		if (expression.getOperator() == IASTCastExpression.op_cast)
			normalCast = true;

		if (normalCast) {
			result.append(Keywords.cpLPAREN);
			result.append(getSignature(expression.getTypeId()));
			result.append(Keywords.cpRPAREN);
			result.append(getExpressionString(expression.getOperand()));
		} else {
			result.append(getCastOperatorString(expression));
			result.append(Keywords.cpLT);
			result.append(getSignature(expression.getTypeId()));
			result.append(Keywords.cpGT);
			result.append(Keywords.cpLPAREN);
			result.append(getExpressionString(expression.getOperand()));
			result.append(Keywords.cpRPAREN);
		}

		return result.toString();
	}

	private static String getFieldReference(IASTFieldReference expression) {
		StringBuilder result = new StringBuilder();
		result.append(getExpressionString(expression.getFieldOwner()));
		if (expression.isPointerDereference())
			result.append(Keywords.cpARROW);
		else
			result.append(Keywords.cpDOT);

		result.append(expression.getFieldName().toString());
		return result.toString();
	}

	private static String getFunctionCallExpression(IASTFunctionCallExpression expression) {
		StringBuilder result = new StringBuilder();
		result.append(getExpressionString(expression.getFunctionNameExpression()));
		result.append(Keywords.cpLPAREN);
		IASTInitializerClause[] clauses = expression.getArguments();
		for (int i = 0; i < clauses.length; i++) {
			if (i > 0) {
				result.append(COMMA_SPACE);
			}
			result.append(getInitializerClauseString(clauses[i]));
		}
		result.append(Keywords.cpRPAREN);
		return result.toString();
	}

	private static String getTypeIdInitializerExpression(ICASTTypeIdInitializerExpression expression) {
		StringBuilder result = new StringBuilder();
		result.append(Keywords.cpLPAREN);
		result.append(getSignature(expression.getTypeId()));
		result.append(Keywords.cpRPAREN);
		result.append(getInitializerString(expression.getInitializer()));
		return result.toString();
	}

	private static String getDeleteExpression(ICPPASTDeleteExpression expression) {
		StringBuilder result = new StringBuilder();
		result.append(Keywords.DELETE);
		result.append(SPACE);
		if (expression.getOperand() != null)
			result.append(getExpressionString(expression.getOperand()));
		return result.toString();
	}

	private static String getSimpleTypeConstructorExpression(ICPPASTSimpleTypeConstructorExpression expression) {
		StringBuilder result = new StringBuilder();
		result.append(getSignature(expression.getDeclSpecifier()));
		result.append(getInitializerString(expression.getInitializer()));
		return result.toString();
	}

	private static String getCompoundStatementExpression(IGNUASTCompoundStatementExpression expression) {
		return String.valueOf(Keywords.cpELLIPSIS); // TODO might need to getSignature(IASTStatement) in the
													// future
	}

	private static String getTypeIdExpression(IASTTypeIdExpression expression) {
		StringBuilder result = new StringBuilder();
		String operator = getTypeIdExpressionOperator(expression);
		if (operator != null && !operator.equals(EMPTY_STRING))
			result.append(operator);

		if (operator != null && !operator.equals(EMPTY_STRING)) {
			result.append(SPACE);
			result.append(Keywords.cpLPAREN);
		}
		result.append(getSignature(expression.getTypeId()));
		if (operator != null && !operator.equals(EMPTY_STRING))
			result.append(Keywords.cpRPAREN);
		return result.toString();
	}

	private static String getExpressionList(IASTExpressionList expression) {
		StringBuilder result = new StringBuilder();
		IASTExpression[] exps = expression.getExpressions();
		if (exps != null && exps.length > 0) {
			for (int i = 0; i < exps.length; i++) {
				result.append(getExpressionString(exps[i]));
				if (i < exps.length - 1) {
					result.append(COMMA_SPACE);
				}
			}
		}
		return result.toString();
	}

	private static String getEmptyExpression(IASTExpression expression) {
		return EMPTY_STRING;
	}

	private static String getLiteralExpression(IASTLiteralExpression expression) {
		return expression.toString();
	}

	private static String getIdExpression(IASTIdExpression expression) {
		return expression.getName().toString();
	}

	private static String getConditionalExpression(IASTConditionalExpression expression) {
		StringBuilder result = new StringBuilder();
		result.append(getExpressionString(expression.getLogicalConditionExpression()));
		result.append(SPACE);
		result.append(Keywords.cpQUESTION);
		result.append(SPACE);
		final IASTExpression positiveExpression = expression.getPositiveResultExpression();
		if (positiveExpression != null) {
			result.append(getExpressionString(positiveExpression));
			result.append(SPACE);
		}
		result.append(Keywords.cpCOLON);
		result.append(SPACE);
		result.append(getExpressionString(expression.getNegativeResultExpression()));
		return result.toString();
	}

	private static String getNewExpression(ICPPASTNewExpression expression) {
		StringBuilder result = new StringBuilder();
		result.append(Keywords.NEW);
		result.append(SPACE);
		final IASTInitializerClause[] args = expression.getPlacementArguments();
		if (args != null) {
			result.append("("); //$NON-NLS-1$
			appendExpressionList(result, args);
			result.append(")"); //$NON-NLS-1$
		}
		result.append(getSignature(expression.getTypeId()));
		final IASTInitializer initializer = expression.getInitializer();
		if (initializer != null)
			result.append(getInitializerString(initializer));
		return result.toString();
	}

	private static String getBinaryExpression(IASTBinaryExpression expression) {
		StringBuilder buffer = new StringBuilder();
		buffer.append(getExpressionString(expression.getOperand1()));
		buffer.append(SPACE);
		buffer.append(getBinaryOperatorString(expression));
		buffer.append(SPACE);
		buffer.append(getExpressionString(expression.getOperand2()));
		return buffer.toString();
	}

	private static String getUnaryExpression(IASTUnaryExpression expression) {
		StringBuilder buffer = new StringBuilder();
		boolean postOperator = false;
		boolean primaryBracketed = false;

		switch (expression.getOperator()) {
		case IASTUnaryExpression.op_postFixDecr:
		case IASTUnaryExpression.op_postFixIncr:
			postOperator = true;
			break;
		case IASTUnaryExpression.op_bracketedPrimary:
			primaryBracketed = true;
			break;
		default:
			postOperator = false;
			break;
		}

		if (!postOperator && !primaryBracketed)
			buffer.append(getUnaryOperatorString(expression));

		// Need to add a space to the unary expression if it is a specific operator
		switch (expression.getOperator()) {
		case IASTUnaryExpression.op_sizeof:
		case ICPPASTUnaryExpression.op_noexcept:
		case ICPPASTUnaryExpression.op_throw:
		case ICPPASTUnaryExpression.op_typeid:
			buffer.append(SPACE);
			break;
		}

		if (primaryBracketed)
			buffer.append(Keywords.cpLPAREN);
		buffer.append(getExpressionString(expression.getOperand()));
		if (primaryBracketed)
			buffer.append(Keywords.cpRPAREN);
		if (postOperator && !primaryBracketed)
			buffer.append(getUnaryOperatorString(expression));

		return buffer.toString();
	}

	/**
	 * Returns the String representation of the IASTCastExpression's operator.
	 *
	 * @param expression
	 * @return the String representation of the IASTCastExpression's operator
	 */
	public static String getCastOperatorString(IASTCastExpression expression) {
		int op = expression.getOperator();
		String opString = EMPTY_STRING;

		if (expression instanceof ICPPASTCastExpression) {
			switch (op) {
			case ICPPASTCastExpression.op_const_cast:
				opString = Keywords.CONST_CAST;
				break;
			case ICPPASTCastExpression.op_dynamic_cast:
				opString = Keywords.DYNAMIC_CAST;
				break;
			case ICPPASTCastExpression.op_reinterpret_cast:
				opString = Keywords.REINTERPRET_CAST;
				break;
			case ICPPASTCastExpression.op_static_cast:
				opString = Keywords.STATIC_CAST;
				break;
			default:
				break;
			}
		}

		if (!opString.equals(EMPTY_STRING))
			return opString;

		switch (op) {
		case IASTCastExpression.op_cast:
			opString = Keywords.CAST;
			break;
		}

		return opString;
	}

	/**
	 * Returns the String representation of the IASTUnaryExpression's operator.
	 *
	 * @param ue
	 * @return the String representation of the IASTUnaryExpression's operator
	 */
	public static String getUnaryOperatorString(IASTUnaryExpression ue) {
		int op = ue.getOperator();
		String opString = EMPTY_STRING;

		if (ue instanceof ICPPASTUnaryExpression) {
			switch (op) {
			case ICPPASTUnaryExpression.op_noexcept:
				opString = Keywords.NOEXCEPT;
				break;
			case ICPPASTUnaryExpression.op_throw:
				opString = Keywords.THROW;
				break;
			case ICPPASTUnaryExpression.op_typeid:
				opString = Keywords.TYPEID;
				break;
			}
		}
		if (!opString.equals(EMPTY_STRING))
			return opString;

		switch (op) {
		case IASTUnaryExpression.op_alignOf:
			opString = Keywords.ALIGNOF;
			break;
		case IASTUnaryExpression.op_amper:
			opString = String.valueOf(Keywords.cpAMPER);
			break;
		case IASTUnaryExpression.op_minus:
			opString = String.valueOf(Keywords.cpMINUS);
			break;
		case IASTUnaryExpression.op_not:
			opString = String.valueOf(Keywords.cpNOT);
			break;
		case IASTUnaryExpression.op_plus:
			opString = String.valueOf(Keywords.cpPLUS);
			break;
		case IASTUnaryExpression.op_postFixDecr:
			opString = String.valueOf(Keywords.cpDECR);
			break;
		case IASTUnaryExpression.op_postFixIncr:
			opString = String.valueOf(Keywords.cpINCR);
			break;
		case IASTUnaryExpression.op_prefixDecr:
			opString = String.valueOf(Keywords.cpDECR);
			break;
		case IASTUnaryExpression.op_prefixIncr:
			opString = String.valueOf(Keywords.cpINCR);
			break;
		case IASTUnaryExpression.op_sizeof:
			opString = Keywords.SIZEOF;
			break;
		case IASTUnaryExpression.op_sizeofParameterPack:
			opString = Keywords.SIZEOF + new String(Keywords.cpELLIPSIS);
			break;
		case IASTUnaryExpression.op_star:
			opString = String.valueOf(Keywords.cpSTAR);
			break;
		case IASTUnaryExpression.op_tilde:
			opString = String.valueOf(Keywords.cpCOMPL);
			break;
		}

		return opString;
	}

	/**
	 * Returns the String representation of the IASTBinaryExpression's operator.
	 *
	 * @param be
	 * @return the String representation of the IASTBinaryExpression's operator
	 */
	public static String getBinaryOperatorString(IASTBinaryExpression be) {
		int op = be.getOperator();
		String opString = EMPTY_STRING;
		switch (op) {
		case IASTBinaryExpression.op_multiply:
			opString = String.valueOf(Keywords.cpSTAR);
			break;
		case IASTBinaryExpression.op_divide:
			opString = String.valueOf(Keywords.cpDIV);
			break;
		case IASTBinaryExpression.op_modulo:
			opString = String.valueOf(Keywords.cpMOD);
			break;
		case IASTBinaryExpression.op_plus:
			opString = String.valueOf(Keywords.cpPLUS);
			break;
		case IASTBinaryExpression.op_minus:
			opString = String.valueOf(Keywords.cpMINUS);
			break;
		case IASTBinaryExpression.op_shiftLeft:
			opString = String.valueOf(Keywords.cpSHIFTL);
			break;
		case IASTBinaryExpression.op_shiftRight:
			opString = String.valueOf(Keywords.cpSHIFTR);
			break;
		case IASTBinaryExpression.op_lessThan:
			opString = String.valueOf(Keywords.cpLT);
			break;
		case IASTBinaryExpression.op_greaterThan:
			opString = String.valueOf(Keywords.cpGT);
			break;
		case IASTBinaryExpression.op_lessEqual:
			opString = String.valueOf(Keywords.cpLTEQUAL);
			break;
		case IASTBinaryExpression.op_greaterEqual:
			opString = String.valueOf(Keywords.cpGTEQUAL);
			break;
		case IASTBinaryExpression.op_binaryAnd:
			opString = String.valueOf(Keywords.cpAMPER);
			break;
		case IASTBinaryExpression.op_binaryXor:
			opString = String.valueOf(Keywords.cpXOR);
			break;
		case IASTBinaryExpression.op_binaryOr:
			opString = String.valueOf(Keywords.cpBITOR);
			break;
		case IASTBinaryExpression.op_logicalAnd:
			opString = String.valueOf(Keywords.cpAND);
			break;
		case IASTBinaryExpression.op_logicalOr:
			opString = String.valueOf(Keywords.cpOR);
			break;
		case IASTBinaryExpression.op_assign:
			opString = String.valueOf(Keywords.cpASSIGN);
			break;
		case IASTBinaryExpression.op_multiplyAssign:
			opString = String.valueOf(Keywords.cpSTARASSIGN);
			break;
		case IASTBinaryExpression.op_divideAssign:
			opString = String.valueOf(Keywords.cpDIVASSIGN);
			break;
		case IASTBinaryExpression.op_moduloAssign:
			opString = String.valueOf(Keywords.cpMODASSIGN);
			break;
		case IASTBinaryExpression.op_plusAssign:
			opString = String.valueOf(Keywords.cpPLUSASSIGN);
			break;
		case IASTBinaryExpression.op_minusAssign:
			opString = String.valueOf(Keywords.cpMINUSASSIGN);
			break;
		case IASTBinaryExpression.op_shiftLeftAssign:
			opString = String.valueOf(Keywords.cpSHIFTLASSIGN);
			break;
		case IASTBinaryExpression.op_shiftRightAssign:
			opString = String.valueOf(Keywords.cpSHIFTRASSIGN);
			break;
		case IASTBinaryExpression.op_binaryAndAssign:
			opString = String.valueOf(Keywords.cpAMPERASSIGN);
			break;
		case IASTBinaryExpression.op_binaryXorAssign:
			opString = String.valueOf(Keywords.cpXORASSIGN);
			break;
		case IASTBinaryExpression.op_binaryOrAssign:
			opString = String.valueOf(Keywords.cpBITORASSIGN);
			break;
		case IASTBinaryExpression.op_equals:
			opString = String.valueOf(Keywords.cpEQUAL);
			break;
		case IASTBinaryExpression.op_notequals:
			opString = String.valueOf(Keywords.cpNOTEQUAL);
			break;
		case IASTBinaryExpression.op_max:
			opString = String.valueOf(Keywords.cpMAX);
			break;
		case IASTBinaryExpression.op_min:
			opString = String.valueOf(Keywords.cpMIN);
			break;
		case IASTBinaryExpression.op_pmarrow:
			opString = String.valueOf(Keywords.cpARROW);
			break;
		case IASTBinaryExpression.op_pmdot:
			opString = String.valueOf(Keywords.cpDOT);
			break;
		}

		return opString;
	}

	/**
	 * Returns the String representation of the IASTTypeIdExpression's operator.
	 *
	 * @param expression
	 * @return the String representation of the IASTTypeIdExpression's operator
	 */
	private static String getTypeIdExpressionOperator(IASTTypeIdExpression expression) {
		String result = EMPTY_STRING;

		if (expression instanceof IGNUASTTypeIdExpression) {
			switch (expression.getOperator()) {
			case IGNUASTTypeIdExpression.op_alignof:
				result = Keywords.ALIGNOF;
				break;
			case IGNUASTTypeIdExpression.op_typeof:
				result = Keywords.TYPEOF;
				break;
			}
		}

		if (expression instanceof ICPPASTTypeIdExpression) {
			switch (expression.getOperator()) {
			case ICPPASTTypeIdExpression.op_typeid:
				result = Keywords.TYPEID;
				break;
			}
		}

		if (expression.getOperator() == IASTTypeIdExpression.op_sizeof)
			result = Keywords.SIZEOF;

		return result;
	}

	/**
	 * Returns the String representation of the pack expansion expression.
	 */
	private static String getPackExpansionExpression(ICPPASTPackExpansionExpression expression) {
		return new StringBuilder().append(getExpressionString(expression.getPattern())).append(Keywords.cpELLIPSIS)
				.toString();
	}

	public static String getProblemMessage(int problemID, String detail) {
		return ASTProblem.getMessage(problemID, detail);
	}
}
