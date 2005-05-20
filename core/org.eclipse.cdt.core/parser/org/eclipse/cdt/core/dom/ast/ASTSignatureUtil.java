/*******************************************************************************
 * Copyright (c) 2005 Rational Software Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     Rational Software - initial implementation
 ******************************************************************************/

package org.eclipse.cdt.core.dom.ast;

import org.eclipse.cdt.core.dom.ast.c.ICASTArrayDesignator;
import org.eclipse.cdt.core.dom.ast.c.ICASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTDesignatedInitializer;
import org.eclipse.cdt.core.dom.ast.c.ICASTDesignator;
import org.eclipse.cdt.core.dom.ast.c.ICASTFieldDesignator;
import org.eclipse.cdt.core.dom.ast.c.ICASTPointer;
import org.eclipse.cdt.core.dom.ast.c.ICASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTTypeIdInitializerExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCastExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeleteExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTReferenceOperator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeConstructorExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTypeIdExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTypenameExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.gnu.IGNUASTCompoundStatementExpression;
import org.eclipse.cdt.core.dom.ast.gnu.IGNUASTTypeIdExpression;
import org.eclipse.cdt.core.dom.ast.gnu.IGNUASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.gnu.c.ICASTKnRFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.gnu.c.IGCCASTArrayRangeDesignator;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPASTPointer;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.parser.GCCKeywords;
import org.eclipse.cdt.core.parser.Keywords;

/**
 * This is a utility class to help convert AST elements to Strings corresponding
 * to the AST element's signature.
 * 
 * @author dsteffle
 */

public class ASTSignatureUtil {
	
	private static final String COMMA_SPACE = ", "; //$NON-NLS-1$
	private static final String EMPTY_STRING = ""; //$NON-NLS-1$
	private static final String SPACE = " "; //$NON-NLS-1$
	private static final String[] EMPTY_STRING_ARRAY = new String[0];
	
	/**
	 * Return's the String representation of a node's type (if available).  This is
	 * currently only being used for testing.
	 * 
	 * TODO Remove this function when done testing if it is no longer needed
	 * 
	 * @param node
	 * @return
	 */
	public static String getNodeSignature(IASTNode node) {
		if (node instanceof IASTDeclarator)
			return getSignature((IASTDeclarator)node);
		if (node instanceof IASTDeclSpecifier)
			return getSignature((IASTDeclSpecifier)node);
		if (node instanceof IASTTypeId)
			return getSignature((IASTTypeId)node);
        if( node instanceof IASTSimpleDeclaration )
        {
            IASTSimpleDeclaration decl = (IASTSimpleDeclaration) node;
            StringBuffer buffer = new StringBuffer( getSignature( decl.getDeclSpecifier()));
            
            IASTDeclarator [] declarators = decl.getDeclarators();
            for( int i = 0; i < declarators.length; ++i )
            {
                buffer.append( SPACE );
                buffer.append( getSignature( declarators[i] ));
                if( declarators[i].getInitializer() != null && declarators[i].getInitializer() instanceof ICPPASTConstructorInitializer )
                    buffer.append( getInitializerString( declarators[i].getInitializer() ));
            }
            buffer.append( ";"); //$NON-NLS-1$
            return buffer.toString();
        }
        if( node instanceof IASTExpression )
        {
            return getExpressionString( (IASTExpression) node );
        }
		
		return EMPTY_STRING;
	}
	
	/**
	 * Returns the parameter signature for an IASTDeclarator as a comma separated list wrapped in parenthesis.
	 * 
	 * This method uses ASTSignatureUtil#getParametersSignatureArray(IASTArray) to build a comma separated list of the 
	 * parameter's signatures and then wraps them in parenthesis.
	 * 
	 * @param decltor
	 * @return the parameter signature for an IASTDeclarator as a comma separated list wrapped in parenthesis
	 */
	// if only function declarator's have parameters then change this to function declarator... should check before starting.. make my life easier!
	public static String getParameterSignature(IASTDeclarator decltor) {
		// should only be working with decltor that has parms
		if (!(decltor instanceof IASTStandardFunctionDeclarator || decltor instanceof ICASTKnRFunctionDeclarator)) return EMPTY_STRING;

		StringBuffer result = new StringBuffer();
		
		String[] parms = getParameterSignatureArray(decltor);
		
		result.append(Keywords.cpLPAREN);
		for(int i=0; i<parms.length; i++) {
			if (parms[i] != null) {
				result.append(parms[i]);
				if (i<parms.length-1) result.append(COMMA_SPACE);
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
//		 should only be working with decltor that has parms
		if (!(decltor instanceof IASTStandardFunctionDeclarator || decltor instanceof ICASTKnRFunctionDeclarator)) return EMPTY_STRING_ARRAY;
		
		String[] result = EMPTY_STRING_ARRAY;
		
		if (decltor instanceof IASTStandardFunctionDeclarator) {
			IASTParameterDeclaration[] parms = null;
			parms = ((IASTStandardFunctionDeclarator)decltor).getParameters();
			
			result = new String[parms.length];
			
			for(int i=0; i<parms.length; i++) {
				if (parms[i] != null) {
					result[i] = getSignature(parms[i].getDeclarator());
				}
			}
		} else if (decltor instanceof ICASTKnRFunctionDeclarator) {
			IASTName[] names = null;
			names = ((ICASTKnRFunctionDeclarator)decltor).getParameterNames(); // required to get the order the parameters are used
			
			result = new String[names.length];
			
			for(int i=0; i<names.length; i++) {
				if (names[i] != null) {
					final IASTDeclarator declaratorForParameterName = ((ICASTKnRFunctionDeclarator)decltor).getDeclaratorForParameterName(names[i]);
                    if( declaratorForParameterName != null )
                        result[i] = getSignature(declaratorForParameterName);
				}
			}
		}
		
		return result;
	}
	
	private static String getDeclaratorSpecificSignature(IASTDeclarator declarator) {
		StringBuffer result = new StringBuffer();
		IASTPointerOperator[] ops = declarator.getPointerOperators();
		boolean needSpace=false;
		
		if (ops != null && ops.length > 0) {
			for(int i=0; i<ops.length; i++) {
				if (ops[i] != null) {
					if (ops[i] instanceof IASTPointer) {
						result.append(Keywords.cpSTAR); // want to have this before keywords on the pointer
						needSpace=true;
					}
					
					if (ops[i] instanceof IGPPASTPointer) {
						if (((IGPPASTPointer)ops[i]).isRestrict()) { if (needSpace) { result.append(SPACE); needSpace=false; } result.append(Keywords.RESTRICT); needSpace=true; }
					}
					
					if (ops[i] instanceof ICASTPointer) {
						if (((ICASTPointer)ops[i]).isRestrict()) { if (needSpace) { result.append(SPACE); needSpace=false; } result.append(Keywords.RESTRICT); needSpace=true; }
					}
					
					if (ops[i] instanceof IASTPointer) {
						if (((IASTPointer)ops[i]).isConst()) { if (needSpace) { result.append(SPACE); needSpace=false; } result.append(Keywords.CONST); needSpace=true; }
						if (((IASTPointer)ops[i]).isVolatile()) { if (needSpace) { result.append(SPACE); needSpace=false; } result.append(Keywords.VOLATILE); needSpace=true; }
					}
					
					if (ops[i] instanceof ICPPASTReferenceOperator) {
						if (needSpace) { result.append(SPACE); needSpace=false; } result.append(Keywords.cpAMPER); needSpace=true;
					}
				}
			}
		}
		
		if (declarator instanceof IASTArrayDeclarator) {
			IASTArrayModifier[] mods = ((IASTArrayDeclarator)declarator).getArrayModifiers();
			
			for(int i=0; i<mods.length; i++) {
				if (mods[i] != null) {
					if (needSpace) { result.append(SPACE); needSpace=false; } result.append(Keywords.cpLBRACKET);
					if (mods[i] instanceof ICASTArrayModifier) {
						if (((ICASTArrayModifier)mods[i]).isConst()) { if (needSpace) { result.append(SPACE); needSpace=false; } result.append(Keywords.CONST); needSpace=true; }
						if (((ICASTArrayModifier)mods[i]).isRestrict()) { if (needSpace) { result.append(SPACE); needSpace=false; } result.append(Keywords.RESTRICT); needSpace=true; }
						if (((ICASTArrayModifier)mods[i]).isStatic()) { if (needSpace) { result.append(SPACE); needSpace=false; } result.append(Keywords.STATIC); needSpace=true; }
						if (((ICASTArrayModifier)mods[i]).isVolatile()) { if (needSpace) { result.append(SPACE); needSpace=false; } result.append(Keywords.VOLATILE); }
					}
					result.append(Keywords.cpRBRACKET);
				}
			}
		}
	
		return result.toString();
	}
	
	private static String getDeclaratorSignature(IASTDeclarator declarator) {
		if (declarator == null) return EMPTY_STRING;
		
		StringBuffer result = new StringBuffer();
		
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
	 * TODO this function is used for testing and probably should not public once 
	 * this Utility class has been finalized as it will likely never be used publicly except for testing
	 * 
	 * @param init
	 * @return the signature of an IASTInitializer
	 */
	public static String getInitializerString(IASTInitializer init) {
		StringBuffer result = new StringBuffer();
		
		if (init instanceof IASTInitializerExpression){
			result.append(getExpressionString(((IASTInitializerExpression)init).getExpression()));
		} else if (init instanceof IASTInitializerList) {
			result.append(Keywords.cpLBRACE);
			IASTInitializer[] inits = ((IASTInitializerList)init).getInitializers();
			for(int i=0; i<inits.length; i++) {
				result.append(getInitializerString(inits[i]));
				if (i<inits.length-1) result.append(COMMA_SPACE);
			}
			result.append(Keywords.cpRBRACE);
		} else if (init instanceof ICASTDesignatedInitializer) {
			ICASTDesignator[] designators = ((ICASTDesignatedInitializer)init).getDesignators();
			for(int i=0; i<designators.length; i++) {
				result.append(getDesignatorSignature(designators[i]));
				if (i<designators.length-1) result.append(COMMA_SPACE);
			}
			result.append(Keywords.cpASSIGN);
			result.append(getInitializerString(((ICASTDesignatedInitializer)init).getOperandInitializer()));
		} else if (init instanceof ICPPASTConstructorInitializer) {
			result.append( "("); //$NON-NLS-1$
            result.append( getExpressionString( ((ICPPASTConstructorInitializer)init).getExpression() ));
            result.append( ")"); //$NON-NLS-1$
		}
		
		return result.toString();
	}
	
	private static String getDesignatorSignature(ICASTDesignator designator) {
		StringBuffer result = new StringBuffer();
		
		if (designator instanceof ICASTArrayDesignator) {
			result.append(Keywords.cpLBRACKET);
			result.append(getExpressionString(((ICASTArrayDesignator)designator).getSubscriptExpression()));
			result.append(Keywords.cpRBRACKET);
		} else if (designator instanceof ICASTFieldDesignator) {
			result.append(Keywords.cpDOT);
			result.append(((ICASTFieldDesignator)designator).getName().toString());
		} else if (designator instanceof IGCCASTArrayRangeDesignator) {
			result.append(Keywords.cpLBRACKET);
			result.append(getExpressionString(((IGCCASTArrayRangeDesignator)designator).getRangeFloor()));
			result.append(SPACE);
			result.append(Keywords.cpELLIPSIS);
			result.append(SPACE);
			result.append(getExpressionString(((IGCCASTArrayRangeDesignator)designator).getRangeCeiling()));
			result.append(Keywords.cpRBRACKET);
		}
		
		return result.toString();
	}
	
	/**
	 * Returns the String signature corresponding to an IASTDeclarator.  This includes the signature
	 * of the parameters which is built via ASTSignatureUtil#getParameterSignature(IASTDeclarator)
	 * if the declarator is for a function.
	 * 
	 * @param declarator
	 * @return the String signature corresponding to an IASTDeclarator
	 */
	public static String getSignature(IASTDeclarator declarator) {
		StringBuffer result = new StringBuffer();
		
		// get the declSpec
		IASTDeclSpecifier declSpec = null;
		
		IASTNode node = declarator.getParent();
		while( node instanceof IASTDeclarator ){
			declarator = (IASTDeclarator) node;
			node = node.getParent();
		}
		
		if( node instanceof IASTParameterDeclaration )
			declSpec = ((IASTParameterDeclaration) node).getDeclSpecifier();
		else if( node instanceof IASTSimpleDeclaration )
			declSpec = ((IASTSimpleDeclaration)node).getDeclSpecifier();
		else if( node instanceof IASTFunctionDefinition )
			declSpec = ((IASTFunctionDefinition)node).getDeclSpecifier();
		else if( node instanceof IASTTypeId )
		    declSpec = ((IASTTypeId)node).getDeclSpecifier();
		
		// append the declSpec's signature to the signature
		String specString = getSignature(declSpec);
		if (specString != null && !specString.equals(EMPTY_STRING))
			result.append(specString);
		
		// append the declarator's signature (without specifier)
		String decltorString = getDeclaratorSignature(declarator);
		if (specString != null && specString.length() > 0 && 
				decltorString != null && decltorString.length() > 0) {
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
		if (declSpec == null) return EMPTY_STRING;
		boolean needSpace=false;
		
		StringBuffer result = new StringBuffer();
		
		if (declSpec.getStorageClass() == ICPPASTDeclSpecifier.sc_mutable) { result.append(Keywords.MUTABLE); needSpace=true; }
		if (declSpec.getStorageClass() == IASTDeclSpecifier.sc_auto) { if (needSpace) { result.append(SPACE); needSpace=false; } result.append(Keywords.AUTO); needSpace=true; }
		if (declSpec.getStorageClass() == IASTDeclSpecifier.sc_extern) { if (needSpace) { result.append(SPACE); needSpace=false; } result.append(Keywords.EXTERN); needSpace=true; }
		if (declSpec.getStorageClass() == IASTDeclSpecifier.sc_register) { if (needSpace) { result.append(SPACE); needSpace=false; } result.append(Keywords.REGISTER); needSpace=true; }
		if (declSpec.getStorageClass() == IASTDeclSpecifier.sc_static) { if (needSpace) { result.append(SPACE); needSpace=false; } result.append(Keywords.STATIC); needSpace=true; }
		if (declSpec.getStorageClass() == IASTDeclSpecifier.sc_typedef) { if (needSpace) { result.append(SPACE); needSpace=false; } result.append(Keywords.TYPEDEF); needSpace=true; }
		
		if (declSpec.isConst()) { if (needSpace) { result.append(SPACE); needSpace=false; } result.append(Keywords.CONST); needSpace=true; }
		if (declSpec.isInline()) { if (needSpace) { result.append(SPACE); needSpace=false; } result.append(Keywords.INLINE); needSpace=true; }
		if (declSpec.isVolatile()) { if (needSpace) { result.append(SPACE); needSpace=false; } result.append(Keywords.VOLATILE); needSpace=true; }
		
		if (declSpec instanceof ICASTDeclSpecifier ) {
			if (((ICASTDeclSpecifier)declSpec).isRestrict()) { if (needSpace) { result.append(SPACE); needSpace=false; } result.append(Keywords.RESTRICT); needSpace=true; }
		} else if (declSpec instanceof ICPPASTDeclSpecifier) {
			if (declSpec.getStorageClass() == ICPPASTDeclSpecifier.sc_mutable) { if (needSpace) { result.append(SPACE); needSpace=false; } result.append(Keywords.MUTABLE); needSpace=true; }
			if (((ICPPASTDeclSpecifier)declSpec).isExplicit()) { if (needSpace) { result.append(SPACE); needSpace=false; } result.append(Keywords.EXPLICIT); needSpace=true; }
			if (((ICPPASTDeclSpecifier)declSpec).isFriend()) { if (needSpace) { result.append(SPACE); needSpace=false; } result.append(Keywords.FRIEND); needSpace=true; }
			if (((ICPPASTDeclSpecifier)declSpec).isVirtual()) { if (needSpace) { result.append(SPACE); needSpace=false; } result.append(Keywords.VIRTUAL); needSpace=true; }
		} else if (declSpec instanceof IGPPASTDeclSpecifier) {
			if (((IGPPASTDeclSpecifier)declSpec).isRestrict()) { if (needSpace) { result.append(SPACE); needSpace=false; } result.append(Keywords.RESTRICT); needSpace=true; }
		}
		
		// handle complex cases 
		if (declSpec instanceof IASTCompositeTypeSpecifier) {
			if (declSpec instanceof ICPPASTCompositeTypeSpecifier) {
				switch(((ICPPASTCompositeTypeSpecifier)declSpec).getKey()) {
					case ICPPASTCompositeTypeSpecifier.k_class:
						if (needSpace) { result.append(SPACE); needSpace=false; } result.append(Keywords.CLASS); needSpace=true;
						break;
					case IASTCompositeTypeSpecifier.k_struct:
						if (needSpace) { result.append(SPACE); needSpace=false; } result.append(Keywords.STRUCT); needSpace=true;
						break;
					case IASTCompositeTypeSpecifier.k_union:
						if (needSpace) { result.append(SPACE); needSpace=false; } result.append(Keywords.UNION); needSpace=true;
						break;
				}
			} else if (declSpec instanceof ICASTCompositeTypeSpecifier) {
				switch(((ICASTCompositeTypeSpecifier)declSpec).getKey()) {
					case IASTCompositeTypeSpecifier.k_struct:
						if (needSpace) { result.append(SPACE); needSpace=false; } result.append(Keywords.STRUCT); needSpace=true;
						break;
					case IASTCompositeTypeSpecifier.k_union:
						if (needSpace) { result.append(SPACE); needSpace=false; } result.append(Keywords.UNION); needSpace=true;
						break;
				}
			}
		} else if (declSpec instanceof IASTElaboratedTypeSpecifier) {
			switch(((IASTElaboratedTypeSpecifier)declSpec).getKind()) {
				case ICPPASTElaboratedTypeSpecifier.k_class:
					if (needSpace) { result.append(SPACE); needSpace=false; } result.append(Keywords.CLASS); needSpace=true;
					break;
				case IASTElaboratedTypeSpecifier.k_enum:
					if (needSpace) { result.append(SPACE); needSpace=false; } result.append(Keywords.ENUM); needSpace=true;
					break;
				case IASTElaboratedTypeSpecifier.k_struct:
					if (needSpace) { result.append(SPACE); needSpace=false; } result.append(Keywords.STRUCT); needSpace=true;
					break;
				case IASTElaboratedTypeSpecifier.k_union:
					if (needSpace) { result.append(SPACE); needSpace=false; } result.append(Keywords.UNION); needSpace=true;
					break;
			}
		} else if (declSpec instanceof IASTEnumerationSpecifier) {
			if (needSpace) { result.append(SPACE); needSpace=false; } result.append(Keywords.ENUM); needSpace=true;
		} else if (declSpec instanceof IASTNamedTypeSpecifier) {
			if (needSpace) { result.append(SPACE); needSpace=false; } result.append(((IASTNamedTypeSpecifier)declSpec).getName().toString()); needSpace=true;
		} else if (declSpec instanceof IASTSimpleDeclSpecifier) {
			// handle complex cases
			if (declSpec instanceof IGPPASTSimpleDeclSpecifier) {
				if (((IGPPASTSimpleDeclSpecifier)declSpec).isLongLong()) result.append(Keywords.LONG_LONG);
				
				switch(((IGPPASTSimpleDeclSpecifier)declSpec).getType()) {
					case IGPPASTSimpleDeclSpecifier.t_typeof:
						if (needSpace) { result.append(SPACE); needSpace=false; } result.append(GCCKeywords.TYPEOF); needSpace=true;
						break;
					case IGPPASTSimpleDeclSpecifier.t_Complex:
						if (needSpace) { result.append(SPACE); needSpace=false; } result.append(Keywords.c_COMPLEX); needSpace=true;
						break;
					case IGPPASTSimpleDeclSpecifier.t_Imaginary:
						if (needSpace) { result.append(SPACE); needSpace=false; } result.append(Keywords.c_IMAGINARY); needSpace=true;
						break;
				}
			}
			
			if (declSpec instanceof ICPPASTSimpleDeclSpecifier) {
				switch(((ICPPASTSimpleDeclSpecifier)declSpec).getType()) {
					case ICPPASTSimpleDeclSpecifier.t_bool:
						if (needSpace) { result.append(SPACE); needSpace=false; } result.append(Keywords.BOOL); needSpace=true;
						break;
					case ICPPASTSimpleDeclSpecifier.t_wchar_t:
						if (needSpace) { result.append(SPACE); needSpace=false; } result.append(Keywords.WCHAR_T); needSpace=true;
						break;
				}
			}
			
			if (declSpec instanceof ICASTSimpleDeclSpecifier) {
				if (((ICASTSimpleDeclSpecifier)declSpec).isLongLong()) { if (needSpace) { result.append(SPACE); needSpace=false; } result.append(Keywords.LONG_LONG); needSpace=true; }
				
				switch(((ICASTSimpleDeclSpecifier)declSpec).getType()) {
					case ICASTSimpleDeclSpecifier.t_Bool:
						if (needSpace) { result.append(SPACE); needSpace=false; } result.append(Keywords.c_BOOL); needSpace=true;
						break;
					case ICASTSimpleDeclSpecifier.t_Complex:
						if (needSpace) { result.append(SPACE); needSpace=false; } result.append(Keywords.c_COMPLEX); needSpace=true;
						break;
					case ICASTSimpleDeclSpecifier.t_Imaginary:
						if (needSpace) { result.append(SPACE); needSpace=false; } result.append(Keywords.c_IMAGINARY); needSpace=true;
						break;
				}
			}
			
			
			// handle simple cases
			if (((IASTSimpleDeclSpecifier)declSpec).isLong()) { if (needSpace) { result.append(SPACE); needSpace=false; } result.append(Keywords.LONG); needSpace=true; }
			if (((IASTSimpleDeclSpecifier)declSpec).isShort()) { if (needSpace) { result.append(SPACE); needSpace=false; } result.append(Keywords.SHORT); needSpace=true; }
			if (((IASTSimpleDeclSpecifier)declSpec).isSigned()) { if (needSpace) { result.append(SPACE); needSpace=false; } result.append(Keywords.SIGNED); needSpace=true; }
			if (((IASTSimpleDeclSpecifier)declSpec).isUnsigned()) { if (needSpace) { result.append(SPACE); needSpace=false; } result.append(Keywords.UNSIGNED); needSpace=true; }
			
			switch(((IASTSimpleDeclSpecifier)declSpec).getType()) {
				case IASTSimpleDeclSpecifier.t_char:
					if (needSpace) { result.append(SPACE); needSpace=false; } result.append(Keywords.CHAR); needSpace=true;
					break;
				case IASTSimpleDeclSpecifier.t_double:
					if (needSpace) { result.append(SPACE); needSpace=false; } result.append(Keywords.DOUBLE); needSpace=true;
					break;
				case IASTSimpleDeclSpecifier.t_float:
					if (needSpace) { result.append(SPACE); needSpace=false; } result.append(Keywords.FLOAT); needSpace=true;
					break;
				case IASTSimpleDeclSpecifier.t_int:
					if (needSpace) { result.append(SPACE); needSpace=false; } result.append(Keywords.INT); needSpace=true;
					break;
				case IASTSimpleDeclSpecifier.t_void:
					if (needSpace) { result.append(SPACE); needSpace=false; } result.append(Keywords.VOID); needSpace=true;
					break;
			}
		}
		
		return result.toString();
	}
	
	/**
	 * Returns the String representation of the signature for the IASTTypeId.
	 * @param typeId
	 * @return the String representation of the signature for the IASTTypeId
	 */
	public static String getSignature(IASTTypeId typeId) {
		return getSignature(typeId.getAbstractDeclarator());
	}
		
	/**
	 * Return a string representation for the given IASTExpression.  Expressions having an extension kind should
	 * provide their own toString method which will be called by this.
	 * 
	 * @param expression
	 * @return a string representation for the given IASTExpression
	 */
	public static String getExpressionString( IASTExpression expression ){
		if (expression instanceof IASTArraySubscriptExpression)
			return getArraySubscriptExpression((IASTArraySubscriptExpression)expression);
		else if (expression instanceof IASTBinaryExpression)
			return getBinaryExpression( (IASTBinaryExpression)expression );
		else if (expression instanceof IASTCastExpression)
			return getCastExpression((IASTCastExpression)expression);
		else if (expression instanceof IASTConditionalExpression)
			return getConditionalExpression((IASTConditionalExpression)expression);
		else if (expression instanceof IASTExpressionList)
			return getExpressionList((IASTExpressionList)expression);
		else if (expression instanceof IASTFieldReference)
			return getFieldReference((IASTFieldReference)expression);
		else if (expression instanceof IASTFunctionCallExpression)
			return getFunctionCallExpression((IASTFunctionCallExpression)expression);
		else if (expression instanceof IASTIdExpression)
			return getIdExpression((IASTIdExpression)expression);
		else if (expression instanceof IASTLiteralExpression)
			return getLiteralExpression((IASTLiteralExpression)expression);
		else if (expression instanceof IASTTypeIdExpression)
			return getTypeIdExpression( (IASTTypeIdExpression)expression );
		else if (expression instanceof IASTUnaryExpression)
			return getUnaryExpression( (IASTUnaryExpression)expression );
		else if (expression instanceof ICASTTypeIdInitializerExpression)
			return getTypeIdInitializerExpression((ICASTTypeIdInitializerExpression)expression);
		else if (expression instanceof ICPPASTDeleteExpression)
			return getDeleteExpression((ICPPASTDeleteExpression)expression);
		else if (expression instanceof ICPPASTNewExpression)
			return getNewExpression((ICPPASTNewExpression)expression);
		else if (expression instanceof ICPPASTSimpleTypeConstructorExpression)
			return getSimpleTypeConstructorExpression((ICPPASTSimpleTypeConstructorExpression)expression);
		else if (expression instanceof ICPPASTTypenameExpression)
			return getTypenameExpression((ICPPASTTypenameExpression)expression);			
		else if (expression instanceof IGNUASTCompoundStatementExpression)
			return getCompoundStatementExpression((IGNUASTCompoundStatementExpression)expression);

		return getEmptyExpression( expression );
	}
	
	private static String getArraySubscriptExpression(IASTArraySubscriptExpression expression) {
		StringBuffer result = new StringBuffer();
		result.append(getExpressionString(expression.getArrayExpression()));
		result.append(Keywords.cpLBRACKET);
		result.append(getExpressionString(expression.getSubscriptExpression()));
		result.append(Keywords.cpRBRACKET);
		return result.toString();
	}
	
	private static String getCastExpression(IASTCastExpression expression) {
		StringBuffer result = new StringBuffer();
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
		StringBuffer result = new StringBuffer();
		result.append(getExpressionString(expression.getFieldOwner()));
		if (expression.isPointerDereference())
			result.append(Keywords.cpARROW);
		else
			result.append(Keywords.cpDOT);
		
		result.append(expression.getFieldName().toString());
		return result.toString();
	}
	
	private static String getFunctionCallExpression(IASTFunctionCallExpression expression) {
		StringBuffer result = new StringBuffer();
		result.append(getExpressionString(expression.getFunctionNameExpression()));
		result.append(Keywords.cpLPAREN);
		result.append(getExpressionString(expression.getParameterExpression()));
		result.append(Keywords.cpRPAREN);
		return result.toString();
	}
	
	private static String getTypeIdInitializerExpression(ICASTTypeIdInitializerExpression expression) {
		StringBuffer result = new StringBuffer();
		result.append(Keywords.cpLPAREN);
		result.append(getSignature(expression.getTypeId()));
		result.append(Keywords.cpRPAREN);
		result.append(getInitializerString(expression.getInitializer()));
		return result.toString();
	}
	
	private static String getDeleteExpression(ICPPASTDeleteExpression expression) {
		StringBuffer result = new StringBuffer();
		result.append(Keywords.DELETE);
		result.append(SPACE);
		if (expression.getOperand() != null)
			result.append(getExpressionString(expression.getOperand()));
		return result.toString();
	}
	
	private static String getSimpleTypeConstructorExpression(ICPPASTSimpleTypeConstructorExpression expression) {
		StringBuffer result = new StringBuffer();
		switch (expression.getSimpleType()) {
			case ICPPASTSimpleTypeConstructorExpression.t_bool:
				result.append(Keywords.BOOL);
				break;
			case ICPPASTSimpleTypeConstructorExpression.t_char:
				result.append(Keywords.CHAR);
				break;
			case ICPPASTSimpleTypeConstructorExpression.t_double:
				result.append(Keywords.DOUBLE);
				break;
			case ICPPASTSimpleTypeConstructorExpression.t_float:
				result.append(Keywords.FLOAT);
				break;
			case ICPPASTSimpleTypeConstructorExpression.t_int:
				result.append(Keywords.INT);
				break;
			case ICPPASTSimpleTypeConstructorExpression.t_long:
				result.append(Keywords.LONG);
				break;
			case ICPPASTSimpleTypeConstructorExpression.t_short:
				result.append(Keywords.SHORT);
				break;
			case ICPPASTSimpleTypeConstructorExpression.t_signed:
				result.append(Keywords.SIGNED);
				break;
			case ICPPASTSimpleTypeConstructorExpression.t_unsigned:
				result.append(Keywords.UNSIGNED);
				break;
			case ICPPASTSimpleTypeConstructorExpression.t_void:
				result.append(Keywords.VOID);
				break;
			case ICPPASTSimpleTypeConstructorExpression.t_wchar_t:
				result.append(Keywords.WCHAR_T);
				break;
		}
		result.append(Keywords.cpLPAREN);
		result.append(expression.getInitialValue());
		result.append(Keywords.cpRPAREN);
		return result.toString();
	}
	
	private static String getTypenameExpression(ICPPASTTypenameExpression expression) {
		StringBuffer result = new StringBuffer();
		result.append(Keywords.TYPENAME);
		result.append(SPACE);
		result.append(expression.getName().toString());
		IASTExpression initValue = expression.getInitialValue();
		result.append(Keywords.cpLPAREN);
		if (initValue != null) {
			result.append(getExpressionString(initValue));
		}
		result.append(Keywords.cpRPAREN);
		return result.toString();
	}
	
	private static String getCompoundStatementExpression(IGNUASTCompoundStatementExpression expression) {
		return String.valueOf(Keywords.cpELLIPSIS); // TODO might need to getSignature(IASTStatement) in the future
	}
	
	private static String getTypeIdExpression(IASTTypeIdExpression expression) {
		StringBuffer result = new StringBuffer();
		String operator = getTypeIdExpressionOperator(expression); 
		if (operator != null && !operator.equals(EMPTY_STRING)) result.append(operator);
		
		if (operator != null && !operator.equals(EMPTY_STRING)) { result.append(SPACE); result.append(Keywords.cpLPAREN); }
		result.append(getSignature(expression.getTypeId()));
		if (operator != null && !operator.equals(EMPTY_STRING)) result.append(Keywords.cpRPAREN);
		return result.toString();
	}
	
	private static String getExpressionList(IASTExpressionList expression) {
		StringBuffer result = new StringBuffer();
		IASTExpression[] exps = expression.getExpressions();
		if (exps != null && exps.length>0) {
			for(int i=0; i<exps.length; i++) {
				result.append(getExpressionString(exps[i]));
				if (i < exps.length-1) {
					result.append(COMMA_SPACE);
				}
			}
		}
		return result.toString();
	}
	
	private static String getEmptyExpression( IASTExpression expression ){
		return EMPTY_STRING;
	}
	
	private static String getLiteralExpression( IASTLiteralExpression expression ){
		StringBuffer result = new StringBuffer();
		if (expression.getKind() == IASTLiteralExpression.lk_string_literal) result.append("\""); //$NON-NLS-1$
		result.append(expression.toString());
		if (expression.getKind() == IASTLiteralExpression.lk_string_literal) result.append("\""); //$NON-NLS-1$		
		return result.toString();
	}
	
	private static String getIdExpression( IASTIdExpression expression ){
		return expression.getName().toString();
	}
	private static String getConditionalExpression( IASTConditionalExpression expression ){
		StringBuffer result = new StringBuffer();
		result.append(getExpressionString(expression.getLogicalConditionExpression()));
		result.append(SPACE);
		result.append(Keywords.cpQUESTION);
		result.append(SPACE);
		result.append(getExpressionString(expression.getPositiveResultExpression()));
		result.append(SPACE);
		result.append(Keywords.cpCOLON);
		result.append(SPACE);
		result.append(getExpressionString(expression.getNegativeResultExpression()));
		return result.toString();
	}
	private static String getNewExpression( ICPPASTNewExpression expression ){
		StringBuffer result = new StringBuffer();
		result.append(Keywords.NEW);
		result.append(SPACE);
		if (expression.getNewPlacement() != null) {
			result.append(getExpressionString(expression.getNewPlacement()));
		}
		result.append(getSignature(expression.getTypeId()));
		result.append(Keywords.cpLPAREN);
		result.append(getExpressionString(expression.getNewInitializer()));
		result.append(Keywords.cpRPAREN);
		return result.toString();
	}
	private static String getBinaryExpression( IASTBinaryExpression expression ){
		StringBuffer buffer = new StringBuffer();
		buffer.append( getExpressionString( expression.getOperand1() ) );
		buffer.append(SPACE);
		buffer.append( getBinaryOperatorString( expression ));
		buffer.append(SPACE);
		buffer.append( getExpressionString( expression.getOperand2() ) );
		return buffer.toString();
	}

	private static String getUnaryExpression( IASTUnaryExpression expression ){
		StringBuffer buffer = new StringBuffer();
		boolean postOperator=false;
		boolean primaryBracketed=false;
		
		switch(expression.getOperator()) {
			case IASTUnaryExpression.op_postFixDecr:
			case IASTUnaryExpression.op_postFixIncr:
				postOperator=true;
				break;
			case IASTUnaryExpression.op_bracketedPrimary:
				primaryBracketed=true;
				break;
			default:
				postOperator=false;
				break;
		}
		
		if (!postOperator && !primaryBracketed) buffer.append(getUnaryOperatorString(expression));
		
		// need to add a space to the unary expression if it is a specific operator
		switch(expression.getOperator()) {
			case IASTUnaryExpression.op_sizeof:
			case ICPPASTUnaryExpression.op_throw:
			case ICPPASTUnaryExpression.op_typeid:
				buffer.append(SPACE);
				break;
		}
		
		if (primaryBracketed) buffer.append(Keywords.cpLPAREN);
		buffer.append(getExpressionString(expression.getOperand()));
		if (primaryBracketed) buffer.append(Keywords.cpRPAREN);
		if (postOperator && !primaryBracketed) buffer.append(getUnaryOperatorString(expression));
		
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
		
		if (!opString.equals(EMPTY_STRING)) return opString;
		
		switch(op) {
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
			switch(op) {
				case ICPPASTUnaryExpression.op_throw:
					opString = Keywords.THROW;
					break;
				case ICPPASTUnaryExpression.op_typeid:
					opString = Keywords.TYPEID;
					break;
			}
		} else if (ue instanceof IGNUASTUnaryExpression) {
			switch(op) {
				case IGNUASTUnaryExpression.op_alignOf:
					opString = Keywords.ALIGNOF;
					break;
				case IGNUASTUnaryExpression.op_typeof:
					opString = Keywords.TYPEOF;
					break;
			}
		}
		
		if (!opString.equals(EMPTY_STRING)) return opString;
		
		switch(op) {
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
		
		if (be instanceof ICPPASTBinaryExpression) {
			switch(op) {
				case ICPPASTBinaryExpression.op_pmarrow:
					opString = String.valueOf(Keywords.cpARROW);
					break;
				case ICPPASTBinaryExpression.op_pmdot:
					opString = String.valueOf(Keywords.cpDOT);
					break;
			}
		} else if (be instanceof IGPPASTBinaryExpression) {
			switch(op) {
				case IGPPASTBinaryExpression.op_max:
					opString = String.valueOf(Keywords.cpMAX);
					break;
				case IGPPASTBinaryExpression.op_min:
					opString = String.valueOf(Keywords.cpMIN);
					break;
			}
		}
		
		if (!opString.equals(EMPTY_STRING)) return opString;
		
		switch(op) {
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
            case IGPPASTBinaryExpression.op_max:
                opString = String.valueOf(Keywords.cpMAX);
                break;
            case IGPPASTBinaryExpression.op_min:
                opString = String.valueOf(Keywords.cpMIN);
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
			switch(expression.getOperator()) {
				case ICPPASTTypeIdExpression.op_typeid:
					result = Keywords.TYPEID;
					break;
			}
		}
		
		if (expression.getOperator() == IASTTypeIdExpression.op_sizeof)
			result = Keywords.SIZEOF;
		
		return result;
	}

}
