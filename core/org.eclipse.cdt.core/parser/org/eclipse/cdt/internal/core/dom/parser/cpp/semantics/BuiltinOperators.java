/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.dom.parser.cpp.semantics;

import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBasicType.Kind;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPPointerToMemberType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPArithmeticConversion;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPBasicType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPBuiltinParameter;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPFunctionType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPImplicitFunction;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPReferenceType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.OverloadableOperator;

/**
 * Generates built-in operators according to 13.6
 */
class BuiltinOperators {
	private static final ICPPFunction[] EMPTY = {};
	private static final int FIRST = 0;
	private static final int SECOND = 1;
	private static final IType BOOL = new CPPBasicType(Kind.eBoolean, 0);
	private static final IType PTR_DIFF = new CPPBasicType(Kind.eInt, 0);

	public static ICPPFunction[] create(OverloadableOperator operator, IASTInitializerClause[] args, IASTTranslationUnit tu, Object[] globCandidates) {
		if (operator == null || args == null || args.length == 0)
			return EMPTY;
		
		return new BuiltinOperators(operator, args, tu.getScope(), globCandidates).create();
	}

	private final OverloadableOperator fOperator;
	private final boolean fUnary;
	private IType fType1;
	private IType fType2;
	private IType[][] fClassConversionTypes= {null, null};
	private boolean[] fIsClass= {false,false};
	private IScope fFileScope;
	private List<ICPPFunction> fResult;
	private Set<String> fSignatures;
	private Object[] fGlobalCandidates;

	BuiltinOperators(OverloadableOperator operator, IASTInitializerClause[] args, IScope fileScope, Object[] globCandidates) {
		fFileScope= fileScope;
		fOperator= operator;
		fUnary= args.length<2;
		fGlobalCandidates= globCandidates;
		if (args.length > 0 && args[0] instanceof IASTExpression) {
			IType type= ((IASTExpression) args[0]).getExpressionType();
			if (!(type instanceof IProblemBinding)) 
				fType1= type;
			
		}
		if (args.length > 1 && args[1] instanceof IASTExpression) {
			IType type= ((IASTExpression) args[1]).getExpressionType();
			if (!(type instanceof IProblemBinding))
				fType2= type;
		}
	}


	private ICPPFunction[] create() {
		switch(fOperator) {
		case ARROW:
		case COMMA:
		case DELETE:
		case DELETE_ARRAY:
		case NEW:
		case NEW_ARRAY:
		case PAREN:
			return EMPTY;
			
		case INCR:
		case DECR:
			opIncOrDec();
			break;

		case STAR:
			if (fUnary) {
				opDeref();
			} else {
				binaryPromotedArithmetic(true, ReturnType.CONVERT);
			}
			break;

		case DIV:
			binaryPromotedArithmetic(true, ReturnType.CONVERT);
			break;

		case PLUS:
			if (fUnary) {
				unaryPointer();
				unaryPromotedArithmetic(true);
			} else {
				binaryPromotedArithmetic(true, ReturnType.CONVERT);
				pointerArithmetic(false, false);
			}				
			break;

		case BRACKET:
			pointerArithmetic(true, false);
			break;

		case MINUS:
			if (fUnary) {
				unaryPromotedArithmetic(true);
			} else {
				binaryPromotedArithmetic(true, ReturnType.CONVERT);
				pointerArithmetic(false, true);
			}				
			break;

		case BITCOMPLEMENT:
			unaryPromotedArithmetic(false);
			break;

		case ARROWSTAR:
			opArrowStar();
			break;

		case EQUAL:
		case NOTEQUAL:
			binaryPromotedArithmetic(true, ReturnType.USE_BOOL);
			comparison(true);
			break;

		case GT:
		case GTEQUAL:
		case LT:
		case LTEQUAL:
			binaryPromotedArithmetic(true, ReturnType.USE_BOOL);
			comparison(false);
			break;

		case AMPER:
			if (fUnary) 
				return EMPTY;
			
			binaryPromotedArithmetic(false, ReturnType.CONVERT);
			break;

		case BITOR:
		case XOR:
		case MOD:
			binaryPromotedArithmetic(false, ReturnType.CONVERT);
			break;

		case SHIFTL:
		case SHIFTR:
			binaryPromotedArithmetic(false, ReturnType.USE_FIRST);
			break;

		case ASSIGN:
			arithmeticAssignement(true, Assignment.WITHOUT_OPERATION);
			break;

		case MINUSASSIGN:
		case PLUSASSIGN:
			arithmeticAssignement(true, Assignment.WITH_POINTER_OPERATION);
			break;
			
		case DIVASSIGN:
		case STARASSIGN:
			arithmeticAssignement(true, Assignment.WITH_OPERATION);
			break;

		case AMPERASSIGN:
		case BITORASSIGN:
		case MODASSIGN:
		case SHIFTLASSIGN:
		case SHIFTRASSIGN:
		case XORASSIGN:
			arithmeticAssignement(false, Assignment.WITH_OPERATION);
			break;

		case AND:
		case OR:
			addFunction(BOOL, BOOL, BOOL);
			break;
			
		case NOT:
			addFunction(BOOL, BOOL);
			break;
		}
		
		if (fResult == null)
			return EMPTY;
		
		return fResult.toArray(new ICPPFunction[fResult.size()]);
	}



	// 13.6-3, 13.6-4, 13.6-5
	private void opIncOrDec() {
		IType[] types= getClassConversionTypes(FIRST);
		for (IType type : types) {
			type= SemanticUtil.getNestedType(type, TDEF);
			if (type instanceof ICPPReferenceType) {
				IType nested=  ((ICPPReferenceType) type).getType();
				CVQualifier cvq= SemanticUtil.getCVQualifier(nested);
				if (!cvq.isConst()) {
					nested= SemanticUtil.getNestedType(nested, TDEF | CVTYPE);
					boolean ok= false;
					if (isArithmetic(nested)) {
						// 13.6-3 and 1.3.6-4
						if (fOperator == OverloadableOperator.INCR || !isBoolean(type)) {
							ok= true;
						}
					} else if (isPointer(nested)) {
						// 13.6-5
						nested= ((IPointerType) nested).getType();
						if (!(SemanticUtil.getNestedType(nested, TDEF) instanceof IFunctionType)) {
							ok= true;
						}
					}
					if (ok) {
						if (fType2 != null) {
							addFunction(type, type, fType2);
						} else {
							addFunction(type, type);
						}
					}
				}
			} 
		}
	}
	
	// 13.6-6, 13.6-7
	private void opDeref() {
		IType[] types= getClassConversionTypes(FIRST);
		for (IType type : types) {
			type= SemanticUtil.getNestedType(type, TDEF);
			if (isPointer(type)) {
				IType nested=  SemanticUtil.getNestedType(((IPointerType) type).getType(), TDEF);
				if (nested instanceof ICPPFunctionType) {
					ICPPFunctionType ft= (ICPPFunctionType) nested;
					if (ft.isConst() || ft.isVolatile())
						return;
				}
				addFunction(new CPPReferenceType(nested, false), type);
			}
		}
	}

	// 13.6-8
	private void unaryPointer() {
		IType[] types= getClassConversionTypes(FIRST);
		for (IType type : types) {
			type= SemanticUtil.getNestedType(type, TDEF|REF);
			if (isPointer(type)) {
				addFunction(type, type);
			}
		}
	}

	// 13.6-9, 13.6-10
	private void unaryPromotedArithmetic(boolean includeFloatingPoint) {
		IType[] types= getClassConversionTypes(FIRST);
		for (IType type : types) {
			type= SemanticUtil.getNestedType(type, TDEF|REF|CVTYPE);
			if (isFloatingPoint(type)) {
				if (includeFloatingPoint) {
					addFunction(type, type);
				}
			} else {
				type= CPPArithmeticConversion.promoteCppType(type);
				if (type != null) {
					addFunction(type, type);
				}
			}
		}
	}
	
	// 13.6-11
	private void opArrowStar() {
		List<IPointerType> classPointers= null;
		List<ICPPPointerToMemberType> memberPointers= null;
		IType[] types= getClassConversionTypes(FIRST);
		for (IType type : types) {
			type= SemanticUtil.getNestedType(type, TDEF | REF);
			if (isPointer(type)) {
				final IPointerType ptrType = (IPointerType) type;
				if (SemanticUtil.getNestedType(ptrType.getType(), TDEF | CVTYPE) instanceof ICPPClassType) {
					if (classPointers == null) {
						classPointers= new ArrayList<IPointerType>();
					}
					classPointers.add(ptrType);
				}
			}
		}
		if (classPointers == null)
			return;
		
		types= getClassConversionTypes(SECOND);
		for (IType type : types) {
			type= SemanticUtil.getNestedType(type, TDEF | REF);
			if (type instanceof ICPPPointerToMemberType) {
				if (memberPointers == null) {
					memberPointers= new ArrayList<ICPPPointerToMemberType>();
				}
				memberPointers.add((ICPPPointerToMemberType) type);
			}
		}
		if (memberPointers == null)
			return;
		
		for (IPointerType clsPtr : classPointers) {
			IType cvClass= SemanticUtil.getNestedType(clsPtr.getType(), TDEF);
			CVQualifier cv1= SemanticUtil.getCVQualifier(cvClass);
			ICPPClassType c1= (ICPPClassType) SemanticUtil.getNestedType(cvClass, TDEF | CVTYPE);
			for (ICPPPointerToMemberType memPtr : memberPointers) {
				IType t2= SemanticUtil.getNestedType(memPtr.getMemberOfClass(), TDEF);
				if (t2 instanceof ICPPClassType) {
					ICPPClassType c2= (ICPPClassType) t2;
					if (SemanticUtil.calculateInheritanceDepth(c1, c2) >= 0) {
						IType cvt= SemanticUtil.getNestedType(memPtr.getType(), TDEF);
						IType rt= new CPPReferenceType(
								SemanticUtil.addQualifiers(cvt, cv1.isConst(), cv1.isVolatile()), false);
						addFunction(rt, clsPtr, memPtr);
					}
				}
			}
		}
	}

	// 13.6-12, 13.6-17
	private static enum ReturnType {CONVERT, USE_FIRST, USE_BOOL}
	private void binaryPromotedArithmetic(boolean fltPt, ReturnType rstrat) {
		List<IType> p1= null, p2= null;
		
		IType[] types1= getClassConversionTypes(FIRST);
		IType[] types2= getClassConversionTypes(SECOND);
		if (types1.length == 0 && types2.length == 0)
			return;
		
		for (IType t : types1) {
			p1 = addPromotedArithmetic(t, fltPt, p1);
		}
		for (IType t : types2) {
			p2 = addPromotedArithmetic(t, fltPt, p2);
		}
		p1= addPromotedArithmetic(fType1, fltPt, p1);
		p2= addPromotedArithmetic(fType2, fltPt, p2);
		if (p1 == null || p2 == null)
			return;
		
		for (IType t1 : p1) {
			for (IType t2 : p2) {
				IType rt= null;
				switch(rstrat) {
				case USE_BOOL:
					rt= BOOL;
					break;
				case USE_FIRST:
					rt= t1;
					break;
				case CONVERT:
					rt= CPPArithmeticConversion.convertCppOperandTypes(IASTBinaryExpression.op_plus, t1, t2);
					break;
				}
				addFunction(rt, t1, t2);
			}
		}
	}

	private List<IType> addPromotedArithmetic(IType t, boolean fltPt, List<IType> p1) {
		IType type= SemanticUtil.getNestedType(t, TDEF|REF|CVTYPE);
		if (isFloatingPoint(type)) {
			if (!fltPt) {
				type= null;
			}
		} else {
			type= CPPArithmeticConversion.promoteCppType(type);
		}
		if (type != null) {
			if (p1 == null) {
				p1= new ArrayList<IType>();
			}
			p1.add(type);
		}
		return p1;
	}


	// 13.6-13, 13.6.14
	private void pointerArithmetic(boolean useRef, boolean isDiff) {
		IType[] types= getClassConversionTypes(FIRST);
		if (types.length == 0 && !fIsClass[FIRST]) {
			types= new IType[] {fType1};
		}
		for (IType type : types) {
			type= SemanticUtil.getNestedType(type, TDEF|REF);
			if (isPointer(type)) {
				final IType ptrTarget = ((IPointerType) type).getType();
				final IType uqPtrTarget = SemanticUtil.getNestedType(ptrTarget, TDEF|CVTYPE);
				if (!(uqPtrTarget instanceof IFunctionType)) {
					final IType retType= useRef ? new CPPReferenceType(ptrTarget, false) : type;
					addFunction(retType, type, PTR_DIFF);
					if (isDiff) {
						addFunction(PTR_DIFF, type, type);
					}
				}
			}
		}
		
		types= getClassConversionTypes(SECOND);
		if (types.length == 0 && !fIsClass[SECOND]) {
			types= new IType[] {fType2};
		}
		for (IType type : types) {
			type= SemanticUtil.getNestedType(type, TDEF|REF);
			if (isPointer(type)) {
				final IType ptrTarget = ((IPointerType) type).getType();
				final IType uqPtrTarget = SemanticUtil.getNestedType(ptrTarget, TDEF|CVTYPE);
				if (!(uqPtrTarget instanceof IFunctionType)) {
					if (isDiff) {
						addFunction(PTR_DIFF, type, type);
					} else {
						final IType retType= useRef ? new CPPReferenceType(ptrTarget, false) : type;
						addFunction(retType, PTR_DIFF, type);
					}
				}
			}
		}
	}

	// 13.6-15, 13.6.16
	private void comparison(boolean ordered) {
		for (int i = FIRST; i <= SECOND; i++) {
			IType[] types= getClassConversionTypes(i);
			for (IType type : types) {
				type= SemanticUtil.getNestedType(type, TDEF|REF|CVTYPE);
				if (isPointer(type) || isEnumeration(type) || (!ordered && isPointerToMember(type))) {
					addFunction(BOOL, type, type);
				}
			}
		}
	}
	
	// 13.6-18, 13.6-29, 13.6-20, 13.6-22
	private static enum Assignment {WITHOUT_OPERATION, WITH_POINTER_OPERATION, WITH_OPERATION}
	private void arithmeticAssignement(boolean fltPt, Assignment assign) {
		IType[] types2= getClassConversionTypes(SECOND);
		if (types2.length == 0)
			return;
		
		IType refType= SemanticUtil.getNestedType(fType1, TDEF);
		if (refType instanceof ICPPReferenceType) {
			IType t= SemanticUtil.getNestedType(((ICPPReferenceType) refType).getType(), TDEF);
			if (!SemanticUtil.getCVQualifier(t).isConst()) {
				switch(assign) {
				case WITHOUT_OPERATION:
					if (isEnumeration(t) || isPointerToMember(t) || isPointer(t)) {
						addFunction(refType, refType, SemanticUtil.getNestedType(t, TDEF|ALLCVQ));
						return;
					}
					break;
				case WITH_POINTER_OPERATION:
					if (isPointer(t)) {
						addFunction(refType, refType, PTR_DIFF);
						return;
					}
					break;
				default:
					break;
				}
			}
			if (fltPt ? isArithmetic(t) : isIntegral(t)) {
				List<IType> p2= null;
				for (IType t2 : types2) {
					p2 = addPromotedArithmetic(t2, fltPt, p2);
				}
				if (p2 != null) {
					for (IType t2 : p2) {
						addFunction(refType, refType, t2);
					}
				}
			}
		} 
	}
	
	private void addFunction(IType returnType, IType p1) {
		addFunction(returnType, new IType[] {p1});
	}
		
	private void addFunction(IType returnType, IType p1, IType p2) {
		addFunction(returnType, new IType[] {p1, p2});
	}

	private void addFunction(IType returnType, IType[] parameterTypes) {
		ICPPParameter[] parameter = new ICPPParameter[parameterTypes.length];
		ICPPFunctionType functionType = new CPPFunctionType(returnType, parameterTypes);
		String sig= ASTTypeUtil.getType(functionType, true);
		if (fSignatures == null) {
			fSignatures= new HashSet<String>();
			if (fGlobalCandidates != null) {
				for (Object cand : fGlobalCandidates) {
					if (cand instanceof IFunction && !(cand instanceof ICPPMethod)) {
						try {
							fSignatures.add(ASTTypeUtil.getType(((IFunction)cand).getType(), true));
						} catch (DOMException e) {
						}
					}
				}
			}
		}
		if (fSignatures.add(sig)) {
			for (int i = 0; i < parameterTypes.length; i++) {
				IType t = parameterTypes[i];
				parameter[i]= new CPPBuiltinParameter(t);
			}
			if (fResult == null) {
				fResult= new ArrayList<ICPPFunction>();
			}
			fResult.add(new CPPImplicitFunction(fOperator.toCharArray(), fFileScope, functionType, parameter, false));
		}
	}
		
	private boolean isEnumeration(IType type) {
		return type instanceof IEnumeration;
	}
	
	private boolean isPointer(IType type) {
		return type instanceof IPointerType && !(type instanceof ICPPPointerToMemberType);
	}

	private boolean isPointerToMember(IType type) {
		return type instanceof ICPPPointerToMemberType;
	}

	private boolean isBoolean(IType type) {
		return type instanceof IBasicType && ((IBasicType) type).getKind() == Kind.eBoolean;
	}

	private boolean isFloatingPoint(IType type) {
		if (type instanceof IBasicType) {
			IBasicType.Kind kind= ((IBasicType) type).getKind();
			switch(kind) {
			case eDouble:
			case eFloat:
				return true;
			case eBoolean:
			case eChar:
			case eChar16:
			case eChar32:
			case eInt:
			case eWChar:
			case eUnspecified:
			case eVoid:
				return false;
			}
		}
		return false;
	}

	private boolean isArithmetic(IType type) {
		if (type instanceof IBasicType) {
			IBasicType.Kind kind= ((IBasicType) type).getKind();
			switch(kind) {
			case eBoolean:
			case eChar:
			case eChar16:
			case eChar32:
			case eDouble:
			case eFloat:
			case eInt:
			case eWChar:
				return true;
			case eUnspecified:
			case eVoid:
				return false;
			}
		}
		return false;
	}

	private boolean isIntegral(IType type) {
		if (type instanceof IBasicType) {
			IBasicType.Kind kind= ((IBasicType) type).getKind();
			switch(kind) {
			case eBoolean:
			case eChar:
			case eChar16:
			case eChar32:
			case eInt:
			case eWChar:
				return true;
			case eDouble:
			case eFloat:
			case eUnspecified:
			case eVoid:
				return false;
			}
		}
		return false;
	}

	private IType[] getClassConversionTypes(int idx) {
		IType[] result = fClassConversionTypes[idx];
		if (result == null) {
			result= IType.EMPTY_TYPE_ARRAY;
			IType type= idx == 0 ? fType1 : fType2;
			if (type != null) {
				type= SemanticUtil.getNestedType(type, TDEF | REF | CVTYPE);
				if (type instanceof ICPPClassType) {
					fIsClass[idx]= true;
					try {
						ICPPMethod[] ops = SemanticUtil.getConversionOperators((ICPPClassType) type);
						result= new IType[ops.length];
						int j= -1;
						for (ICPPMethod op : ops) {
							if (op.isExplicit())
								continue;
							final ICPPFunctionType functionType = op.getType();
							if (functionType != null) {
								IType retType= functionType.getReturnType();
								if (retType != null) {
									result[++j]= retType;
								}
							}
						}
						result= ArrayUtil.trimAt(IType.class, result, j);
					} catch (DOMException e) {
					}
				}
			}		
			fClassConversionTypes[idx]= result;
		}
		return result;
	}
}
