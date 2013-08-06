/*******************************************************************************
 * Copyright (c) 2008, 2012 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 * 	   Institute for Software - initial API and implementation
 *     Sergey Prigogin (Google)
 *     Thomas Corbat (IFS)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.rewrite.astwriter;

import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTTypedefNameSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.parser.GCCKeywords;
import org.eclipse.cdt.core.parser.Keywords;
import org.eclipse.cdt.internal.core.dom.rewrite.commenthandler.NodeCommentMap;

/**
 * Generates source code of declaration specifier nodes. The actual string operations are delegated
 * to the <code>Scribe</code> class.
 * 
 * @see Scribe
 * @see IASTDeclSpecifier
 * @author Emanuel Graf IFS
 */
public class DeclSpecWriter extends NodeWriter {
	
	public DeclSpecWriter(Scribe scribe, ASTWriterVisitor visitor, NodeCommentMap commentMap) {
		super(scribe, visitor, commentMap);
	}

	protected void writeDelcSpec(IASTDeclSpecifier declSpec) {
		// Write general DelcSpec Keywords
		writeDeclSpec(declSpec);
		if (declSpec instanceof ICPPASTDeclSpecifier) {
			writeCPPDeclSpec((ICPPASTDeclSpecifier) declSpec);
		} else if (declSpec instanceof ICASTDeclSpecifier) {
			writeCDeclSpec((ICASTDeclSpecifier) declSpec);
		}
	}

	private String getCPPSimpleDecSpecifier(ICPPASTSimpleDeclSpecifier simpDeclSpec) {
		return getASTSimpleDecSpecifier(simpDeclSpec.getType(), true);
	}
	
	private String getCSimpleDecSpecifier(ICASTSimpleDeclSpecifier simpDeclSpec) {
		return getASTSimpleDecSpecifier(simpDeclSpec.getType(), false);
	}

	private String getASTSimpleDecSpecifier(int type, boolean isCpp) {
		switch (type) {
		case IASTSimpleDeclSpecifier.t_unspecified:
			return ""; //$NON-NLS-1$
		case IASTSimpleDeclSpecifier.t_void:
			return Keywords.VOID;
		case IASTSimpleDeclSpecifier.t_char:
			return Keywords.CHAR;
		case IASTSimpleDeclSpecifier.t_int:
			return Keywords.INT;
		case IASTSimpleDeclSpecifier.t_int128:
			return GCCKeywords.__INT128;

		case IASTSimpleDeclSpecifier.t_float:
			return Keywords.FLOAT;
		case IASTSimpleDeclSpecifier.t_double:
			return Keywords.DOUBLE;
		case IASTSimpleDeclSpecifier.t_float128:
			return GCCKeywords.__FLOAT128;
			
		case IASTSimpleDeclSpecifier.t_bool:
			return isCpp ? Keywords.BOOL : Keywords._BOOL;
			
		case IASTSimpleDeclSpecifier.t_wchar_t:
			if (isCpp)
				return Keywords.WCHAR_T;
			break;
		case IASTSimpleDeclSpecifier.t_char16_t:
			if (isCpp)
				return Keywords.CHAR16_T;
			break;
		case IASTSimpleDeclSpecifier.t_char32_t:
			if (isCpp)
				return Keywords.CHAR32_T;
			break;
		case IASTSimpleDeclSpecifier.t_auto:
			if (isCpp)
				return Keywords.AUTO;
			break;
		case IASTSimpleDeclSpecifier.t_typeof:
			if (isCpp)
				return Keywords.TYPEOF;
			break;
		case IASTSimpleDeclSpecifier.t_decltype:
			if (isCpp)
				return Keywords.DECLTYPE;
			break;
		}

		throw new IllegalArgumentException("Unknown specifier type: " + type); //$NON-NLS-1$
	}

	private void writeCDeclSpec(ICASTDeclSpecifier cDeclSpec) {
		if (cDeclSpec.isRestrict()) {
			scribe.printStringSpace(Keywords.RESTRICT);
		}
		
		if (cDeclSpec instanceof ICASTCompositeTypeSpecifier) {
			writeCompositeTypeSpecifier((ICASTCompositeTypeSpecifier) cDeclSpec);
		} else if (cDeclSpec instanceof ICASTEnumerationSpecifier) {
			writeEnumSpec((ICASTEnumerationSpecifier) cDeclSpec);
		} else if (cDeclSpec instanceof ICASTElaboratedTypeSpecifier) {
			writeElaboratedTypeSec((ICASTElaboratedTypeSpecifier) cDeclSpec);
		} else if (cDeclSpec instanceof ICASTSimpleDeclSpecifier) {
			writeCSimpleDeclSpec((ICASTSimpleDeclSpecifier) cDeclSpec);
		} else if (cDeclSpec instanceof ICASTTypedefNameSpecifier) {
			writeNamedTypeSpecifier((ICASTTypedefNameSpecifier) cDeclSpec);
		}
	}

	private void writeNamedTypeSpecifier(ICPPASTNamedTypeSpecifier namedSpc) {
		if (namedSpc.isTypename()) {
			scribe.printStringSpace(Keywords.TYPENAME);
		}
		namedSpc.getName().accept(visitor);
	}

	private void writeNamedTypeSpecifier(IASTNamedTypeSpecifier namedSpc) {
		namedSpc.getName().accept(visitor);
	}

	private void writeElaboratedTypeSec(IASTElaboratedTypeSpecifier elabType) {
		scribe.printStringSpace(getElabTypeString(elabType.getKind()));
		elabType.getName().accept(visitor);
	}

	private String getElabTypeString(int kind) {
		switch (kind) {
		case IASTElaboratedTypeSpecifier.k_enum:
			return Keywords.ENUM;
		case IASTElaboratedTypeSpecifier.k_struct:
			return Keywords.STRUCT;
		case IASTElaboratedTypeSpecifier.k_union:
			return Keywords.UNION;
		case ICPPASTElaboratedTypeSpecifier.k_class:
			return Keywords.CLASS;
			
		default:
			throw new IllegalArgumentException("Unknown elaborated type: " + kind); //$NON-NLS-1$
		}
	}

	private void writeCPPDeclSpec(ICPPASTDeclSpecifier cppDelcSpec) {
		if (cppDelcSpec.isVirtual()) {
			scribe.printStringSpace(Keywords.VIRTUAL);
		}
		if (cppDelcSpec.isConstexpr()) {
			scribe.printStringSpace(Keywords.CONSTEXPR);
		}
		if (cppDelcSpec.isExplicit()) {
			scribe.printStringSpace(Keywords.EXPLICIT);
		}
		if (cppDelcSpec.isFriend()) {
			scribe.printStringSpace(Keywords.FRIEND);
		}
		if (cppDelcSpec.isThreadLocal()) {
			scribe.printStringSpace(Keywords.THREAD_LOCAL);
		}
		if (cppDelcSpec.getStorageClass() == IASTDeclSpecifier.sc_mutable) {
			scribe.printStringSpace(Keywords.MUTABLE);
		}
		
		if (cppDelcSpec instanceof ICPPASTCompositeTypeSpecifier) {
			writeCompositeTypeSpecifier((ICPPASTCompositeTypeSpecifier) cppDelcSpec);
		} else if (cppDelcSpec instanceof IASTEnumerationSpecifier) {
			writeEnumSpec((IASTEnumerationSpecifier) cppDelcSpec);
		} else if (cppDelcSpec instanceof ICPPASTElaboratedTypeSpecifier) {
			writeElaboratedTypeSec((ICPPASTElaboratedTypeSpecifier) cppDelcSpec);
		} else if (cppDelcSpec instanceof ICPPASTSimpleDeclSpecifier) {
			writeCPPSimpleDeclSpec((ICPPASTSimpleDeclSpecifier) cppDelcSpec);
		} else if (cppDelcSpec instanceof ICPPASTNamedTypeSpecifier) {
			writeNamedTypeSpecifier((ICPPASTNamedTypeSpecifier) cppDelcSpec);
		}
	}

	private void writeEnumSpec(IASTEnumerationSpecifier enumSpec) {
		scribe.printStringSpace(Keywords.ENUM);
		enumSpec.getName().accept(visitor);
		scribe.print('{');
		scribe.printSpace();
		IASTEnumerator[] enums = enumSpec.getEnumerators();
		for (int i = 0; i < enums.length; ++i) {
			writeEnumerator(enums[i]);
			if (i + 1 < enums.length) {
				scribe.print(NodeWriter.COMMA_SPACE);
			}
		}
		scribe.print('}');
	}

	private void writeEnumerator(IASTEnumerator enumerator) {
		enumerator.getName().accept(visitor);
		
		IASTExpression value = enumerator.getValue();
		if (value != null) {
			scribe.print(EQUALS);
			value.accept(visitor);
		}		
	}

	private void writeCompositeTypeSpecifier(IASTCompositeTypeSpecifier compDeclSpec) {
		boolean hasTrailingComments = hasTrailingComments(compDeclSpec.getName());
		scribe.printStringSpace(getCPPCompositeTypeString(compDeclSpec.getKey()));
		compDeclSpec.getName().accept(visitor);
		if (compDeclSpec instanceof ICPPASTCompositeTypeSpecifier) {
			ICPPASTCompositeTypeSpecifier cppComp = (ICPPASTCompositeTypeSpecifier) compDeclSpec;
			if (cppComp.isFinal()) {
				scribe.printSpace();
				scribe.print(Keywords.cFINAL);
			}
			ICPPASTBaseSpecifier[] baseSpecifiers = cppComp.getBaseSpecifiers();
			if (baseSpecifiers.length > 0) {
				scribe.print(SPACE_COLON_SPACE);
				for (int i = 0; i < baseSpecifiers.length; ++i) {
					writeBaseSpecifiers(baseSpecifiers[i]);
					if (i + 1 < baseSpecifiers.length) {
						scribe.print(COMMA_SPACE);
					}
				}
				hasTrailingComments = hasTrailingComments(baseSpecifiers[baseSpecifiers.length-1].getName());
			}
		}
		if (!hasTrailingComments) {
			scribe.newLine();
		}
		scribe.print('{');
		scribe.newLine();
		scribe.incrementIndentationLevel();
		visitor.setSuppressLeadingBlankLine(true);
		IASTDeclaration[] decls = getMembers(compDeclSpec);
		
		if (decls.length > 0) {
			for (IASTDeclaration declaration : decls) {
				declaration.accept(visitor);
			}
		}

		if (hasFreestandingComments(compDeclSpec)) {
			writeFreestandingComments(compDeclSpec);			
		}
		scribe.decrementIndentationLevel();
		scribe.print('}');

		if (hasTrailingComments(compDeclSpec)) {
			writeTrailingComments(compDeclSpec);			
		}
	}

	protected IASTDeclaration[] getMembers(IASTCompositeTypeSpecifier compDeclSpec) {
		return compDeclSpec.getMembers();
	}

	public void writeBaseSpecifiers(ICPPASTBaseSpecifier specifier) {
		switch (specifier.getVisibility()) {
		case ICPPASTBaseSpecifier.v_public:
			scribe.printStringSpace(Keywords.PUBLIC);
			break;
		case ICPPASTBaseSpecifier.v_protected:
			scribe.printStringSpace(Keywords.PROTECTED);
			break;
		case ICPPASTBaseSpecifier.v_private:
			scribe.printStringSpace(Keywords.PRIVATE);
			break;
		}
		
		if (specifier.isVirtual()) {
			scribe.printStringSpace(Keywords.VIRTUAL);
		}
		specifier.getName().accept(visitor);
	}

	private String getCPPCompositeTypeString(int key) {
		if (key <= IASTCompositeTypeSpecifier.k_last) {
			return getCompositeTypeString(key);
		}
		switch (key) {
		case ICPPASTCompositeTypeSpecifier.k_class:
			return Keywords.CLASS;
		default:
			throw new IllegalArgumentException("Unknown type specifier: " + key); //$NON-NLS-1$
		}
	}

	private String getCompositeTypeString(int key) {
		switch (key) {
		case IASTCompositeTypeSpecifier.k_struct:
			return Keywords.STRUCT;
		case IASTCompositeTypeSpecifier.k_union:
			return Keywords.UNION;
		default:
			throw new IllegalArgumentException("Unknown type specifier: " + key); //$NON-NLS-1$
		}
	}

	private void writeDeclSpec(IASTDeclSpecifier declSpec) {
		if (declSpec.isInline()) {
			scribe.printStringSpace(Keywords.INLINE);
		}
		switch (declSpec.getStorageClass()) {
		case IASTDeclSpecifier.sc_typedef:
			scribe.printStringSpace(Keywords.TYPEDEF);
			break;
		case IASTDeclSpecifier.sc_extern:
			scribe.printStringSpace(Keywords.EXTERN);
			break;
		case IASTDeclSpecifier.sc_static:
			scribe.printStringSpace(Keywords.STATIC);
			break;
		case IASTDeclSpecifier.sc_auto:
			scribe.printStringSpace(Keywords.AUTO);
			break;
		case IASTDeclSpecifier.sc_register:
			scribe.printStringSpace(Keywords.REGISTER);
			break;
		}
		if (declSpec.isConst()) {
			scribe.printStringSpace(Keywords.CONST);
		}
		if (declSpec.isVolatile()) {
			scribe.printStringSpace(Keywords.VOLATILE);
		}
	}

	private void writeCPPSimpleDeclSpec(ICPPASTSimpleDeclSpecifier simpDeclSpec) {
		printQualifiers(simpDeclSpec);
		scribe.print(getCPPSimpleDecSpecifier(simpDeclSpec));
		if (simpDeclSpec.getType() == IASTSimpleDeclSpecifier.t_typeof) {
			scribe.printSpace();
			visitNodeIfNotNull(simpDeclSpec.getDeclTypeExpression());
		} else if (simpDeclSpec.getType() == IASTSimpleDeclSpecifier.t_decltype) {
			scribe.print('(');
			visitNodeIfNotNull(simpDeclSpec.getDeclTypeExpression());
			scribe.print(')');
		}
	}
	
	private void printQualifiers(IASTSimpleDeclSpecifier simpDeclSpec) {
		if (simpDeclSpec.isSigned()) {
			scribe.printStringSpace(Keywords.SIGNED);
		} else if (simpDeclSpec.isUnsigned()) {
			scribe.printStringSpace(Keywords.UNSIGNED);
		}
		
		if (simpDeclSpec.isShort()) {
			scribe.printStringSpace(Keywords.SHORT);
		} else if (simpDeclSpec.isLong()) {
			scribe.printStringSpace(Keywords.LONG);
		} else if (simpDeclSpec.isLongLong()) {			
			scribe.printStringSpace(Keywords.LONG);
			scribe.printStringSpace(Keywords.LONG);
		}
		if (simpDeclSpec instanceof ICASTSimpleDeclSpecifier) {
			ICASTSimpleDeclSpecifier cSimpDeclSpec = (ICASTSimpleDeclSpecifier) simpDeclSpec;
			if (cSimpDeclSpec.isComplex()) {
				scribe.printStringSpace(Keywords._COMPLEX);
			}
		}
	}

	private void writeCSimpleDeclSpec(ICASTSimpleDeclSpecifier simpDeclSpec) {
		printQualifiers(simpDeclSpec);
		scribe.print(getCSimpleDecSpecifier(simpDeclSpec));
	}
}
