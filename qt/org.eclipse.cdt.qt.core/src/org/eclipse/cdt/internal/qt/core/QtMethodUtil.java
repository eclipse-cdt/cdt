/*
 * Copyright (c) 2013 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.cdt.internal.qt.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.DatatypeConverter;

import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTPointer;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTReferenceOperator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTypeId;
import org.eclipse.cdt.internal.core.dom.parser.ASTAmbiguousNode;
import org.eclipse.cdt.internal.qt.core.parser.QtParser;

/**
 * A collection of utility functions for dealing with Qt methods.  A Qt method is a normal
 * C++ method that has been annotated with empty macro expansions.
 */
@SuppressWarnings("restriction")
public class QtMethodUtil {

	/**
	 * The Qt implementation uses specific rules for generating a signature that is used
	 * to map between invokable function declarations and their use.  This function has
	 * be implemented by comparing the output of moc to the various test cases in the
	 * qt test suite.
	 */
	public static String getQtNormalizedMethodSignature(String signature) {

		ICPPASTFunctionDeclarator function = QtParser.parseQtMethodReference(signature);
		if (function == null)
			return null;

		// NOTE: This implementation (both here and in methods that are invoked) used call #getRawSignature
		//       to get the original tokens.  This has been changed to use #toString instead.  They seem to
		//       provide the same value, so this should be OK.  The problem with #getRawSignature is that it
		//       looks for the characters in the file (using offset and length).  There isn't a file backing
		//       the StringScanner, so the result is the empty String.  If we find cases where #toString
		//       returns the wrong value, then this can be changed back to #getRawSignature.  Implement the
		//       AST and LocationResolver to work with ASTNode#getRawSignatureChars:
		// protected char[] getRawSignatureChars() {
		//     final IASTFileLocation floc= getFileLocation();
		//     final IASTTranslationUnit ast = getTranslationUnit();
		//     if (floc != null && ast != null) {
		//  	   ILocationResolver lr= (ILocationResolver) ast.getAdapter(ILocationResolver.class);
		//  	   if (lr != null) {
		//  		   return lr.getUnpreprocessedSignature(getFileLocation());
		//  	   }
		//     }

		StringBuilder result = new StringBuilder();

		// raw sig tries to find the file
		String fnName = function.getName().getLastName().toString();
		result.append(stripWS(fnName));
		result.append('(');

		boolean first = true;
		for (ICPPASTParameterDeclaration param : function.getParameters()) {
			if (first)
				first = false;
			else
				result.append(',');

			IASTDeclSpecifier spec = param.getDeclSpecifier();
			ICPPASTDeclarator declarator = param.getDeclarator();

			// The parameters are encoded so that we can rely on , being used to separate
			// parameters.  All other commas (e.g., to separate template arguments within
			// the parameter type) will be encoded.
			StringBuilder paramSig = new StringBuilder();
			append(paramSig, spec, declarator, true);

			result.append(stripWS(paramSig.toString()));
		}

		result.append(')');

		// Whitespace around operators is not needed, remove it to normalize the signature.
		return result.toString();
	}

	public static Collection<String> getDecodedQtMethodSignatures(String qtEncSignatures) {
		if (qtEncSignatures == null)
			return null;

		StringBuilder signature = new StringBuilder();
		int i = qtEncSignatures.indexOf('(');
		String name = qtEncSignatures.substring(0, i);

		signature.append(name);
		signature.append('(');

		boolean first = true;
		List<String> signatures = new ArrayList<>();
		qtEncSignatures = qtEncSignatures.substring(i + 1);
		Pattern p = Pattern.compile("^([a-zA-Z0-9+/=]*)(@?).*$");
		while (!qtEncSignatures.isEmpty()) {
			Matcher m = p.matcher(qtEncSignatures);
			if (!m.matches())
				break;

			int next = m.end(2) + 1;
			qtEncSignatures = qtEncSignatures.substring(next);

			String param = new String(DatatypeConverter.parseBase64Binary(m.group(1)));

			// If this parameter has a default value, then add a signature for the method up
			// to this point.
			if (!m.group(2).isEmpty())
				signatures.add(signature.toString() + ')');

			if (first)
				first = false;
			else
				signature.append(',');
			signature.append(param);
		}

		signature.append(')');
		signatures.add(signature.toString());
		return signatures;
	}

	/**
	 * The Qt implementation has specific rules for generating a signature that is used
	 * to map between invokable function declarations and their use.  This function has
	 * been implemented by comparing the output of moc to the various test cases in the
	 * Qt test suite.
	 */
	public static String getEncodedQtMethodSignatures(ICPPASTFunctionDeclarator function) {
		StringBuilder result = new StringBuilder();

		String fnName = function.getName().getLastName().toString();
		result.append(stripWS(fnName));
		result.append('(');

		boolean first = true;
		for (ICPPASTParameterDeclaration param : function.getParameters()) {
			if (first)
				first = false;
			else
				result.append(',');

			IASTDeclSpecifier spec = param.getDeclSpecifier();
			ICPPASTDeclarator declarator = param.getDeclarator();

			// The parameters are encoded so that we can rely on , being used to separate
			// parameters.  All other commas (e.g., to separate template arguments within
			// the parameter type) will be encoded.
			StringBuilder paramSig = new StringBuilder();
			append(paramSig, spec, declarator, true);

			String paramStr = stripWS(paramSig.toString());
			result.append(DatatypeConverter.printBase64Binary(paramStr.getBytes()));

			// A special character is used as a suffix on parameters that have a default value.
			// A previous version of this implementation used '=' within the Base64 encoded
			// payload.  Now that the initializer flag is outside of the payload, '=' is a bad
			// choice because it is also a valid Base64 encoded character.
			// Like all the other parts of this encoder, the @ must match the value that is used
			// in the decoder.
			if (declarator.getInitializer() != null)
				result.append('@');
		}

		result.append(')');

		// Whitespace around operators is not needed, remove it to normalize the signature.
		return result.toString();
	}

	private static String stripWS(String str) {
		return str.trim().replaceAll("\\s+", " ").replaceAll(" ([\\*&,()<>]+)", "$1").replaceAll("([\\*&,()<>]+) ",
				"$1");
	}

	private static String asString(IASTPointerOperator ptr) {
		if (ptr instanceof ICPPASTReferenceOperator)
			return "&";
		if (ptr instanceof IASTPointer) {
			StringBuilder str = new StringBuilder();
			IASTPointer astPtr = (IASTPointer) ptr;
			str.append('*');
			if (astPtr.isConst())
				str.append(" const");
			if (astPtr.isVolatile())
				str.append(" volatile");
			return str.toString();
		}

		return ptr.toString();
	}

	private static void append(StringBuilder result, IASTDeclSpecifier spec, IASTDeclarator declarator,
			boolean pruneConst) {
		IASTPointerOperator[] ptrs = declarator.getPointerOperators();
		if (ptrs == null)
			ptrs = new IASTPointerOperator[0];

		if (!(spec instanceof ICPPASTDeclSpecifier)) {
			result.append(spec.toString());
			return;
		}

		ICPPASTDeclSpecifier cppSpec = (ICPPASTDeclSpecifier) spec;

		// Qt considers the type const if it is marked as const, or if it is a reference
		// and the previous pointer is const.  I.e., we need this:
		// const T&         -> T
		// const T* const & -> T*
		boolean isConst = cppSpec.isConst();
		boolean stripLastPtrConst = pruneConst && !isConst
				&& (ptrs.length >= 2 && ptrs[ptrs.length - 1] instanceof ICPPASTReferenceOperator
						&& ptrs[ptrs.length - 2] instanceof IASTPointer
						&& ((IASTPointer) ptrs[ptrs.length - 2]).isConst());

		if (isConst || stripLastPtrConst) {
			if (!pruneConst)
				result.append("const ");
			else {
				// Qt signature generation converts const value and const reference types
				// into simple value types.  E.g.,
				//     const T   => T
				//     const T & => T
				// From observation, they also convert const pointer to const to const
				// pointers although I think that is a bug, because simple pointer to
				// const are not converted to simple pointers.  E.g.,
				//     const T *       => const T *
				//     const T * const =>       T * const
				if (ptrs.length > 0) {
					IASTPointerOperator lastPtr = ptrs[ptrs.length - 1];
					if (lastPtr instanceof ICPPASTReferenceOperator)
						ptrs = Arrays.copyOf(ptrs, ptrs.length - 1);
					else if (!(lastPtr instanceof IASTPointer) || !((IASTPointer) lastPtr).isConst())
						result.append("const ");
				}
			}
		}

		// Qt does no special handling for volatile.  This is likely an oversight.
		if (cppSpec.isVolatile())
			result.append("volatile ");

		IASTNode[] children = cppSpec.getChildren();
		if (children == null || children.length <= 0) {
			// We use the raw signature to get the text that was used to reference the
			// type (without following typedefs, etc.), and then strip out all const
			// which has already been handled.
			String raw = cppSpec.toString();
			raw = raw.replaceAll("const\\s", "");
			raw = raw.replaceAll("\\sconst", "");
			result.append(raw);
		} else {
			for (IASTNode child : children) {
				result.append(' ');
				if (child instanceof ICPPASTTemplateId) {
					ICPPASTTemplateId templId = (ICPPASTTemplateId) child;
					result.append(templId.getTemplateName());
					result.append('<');
					for (IASTNode templArg : templId.getTemplateArguments()) {
						append(result, templArg);
					}
					result.append('>');
				} else
					result.append(child.toString());
			}
		}

		// exclude param name, use '=' to indicate an initial value
		for (int i = 0; i < ptrs.length; ++i) {
			if (!stripLastPtrConst || i < ptrs.length - 1)
				result.append(asString(ptrs[i]));
			else
				result.append(asString(ptrs[i]).replaceAll("const", ""));
		}
	}

	private static void append(StringBuilder result, IASTNode node) {

		// JI476551: When the code is parsed without full context, e.g., when parsing a Qt method ref, an
		//           ambiguous node could be created.  Since we only need the original text, we can use
		//           any of the nodes that triggered the ambiguity.  Arbitrarily choose the first one.
		if (node instanceof ASTAmbiguousNode) {
			IASTNode[] nodes = ((ASTAmbiguousNode) node).getNodes();
			if (nodes != null && nodes.length > 0) {
				append(result, nodes[0]);
				return;
			}
		}

		if (node instanceof ICPPASTTypeId) {
			ICPPASTTypeId typeId = (ICPPASTTypeId) node;
			IASTDeclSpecifier spec = typeId.getDeclSpecifier();
			IASTDeclarator declarator = typeId.getAbstractDeclarator();
			append(result, spec, declarator, false);
			return;
		}

		if (!(node instanceof ICPPASTTemplateId)) {
			result.append(node.toString());
			return;
		}

		ICPPASTTemplateId templId = (ICPPASTTemplateId) node;
		result.append(templId.getTemplateName());
		result.append('<');
		boolean first = true;
		for (IASTNode child : templId.getTemplateArguments()) {
			if (first)
				first = false;
			else
				result.append(", ");
			append(result, child);
		}
		result.append('>');
	}
}
