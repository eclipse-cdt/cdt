/*******************************************************************************
 * Copyright (c) 2008, 2010 Symbian Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Ferguson (Symbian) - Initial implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.ui.text.doctools.doxygen;

import java.util.LinkedHashSet;

import org.eclipse.cdt.core.dom.ast.ExpansionOverlapsBoundaryException;
import org.eclipse.cdt.core.dom.ast.IASTArrayDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStandardFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLinkageSpecification;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTypeId;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;
import org.eclipse.cdt.internal.core.model.ASTStringUtil;
import org.eclipse.cdt.ui.text.doctools.DefaultMultilineCommentAutoEditStrategy;
import org.eclipse.cdt.ui.text.doctools.IDocCustomizer;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.TextUtilities;

/**
 * {@link IAutoEditStrategy} for adding Doxygen tags for comments.
 *
 * @since 5.0
 * @noextend This class is not intended to be subclassed by clients.
 */
public class DoxygenMultilineAutoEditStrategy extends DefaultMultilineCommentAutoEditStrategy
		implements IDocCustomizer {
	private static final String SINGLELINE_COMMENT_PRECEDING = "//!< "; //$NON-NLS-1$
	private static final String CLASS = "class "; //$NON-NLS-1$
	private static final String ENUM = "enum "; //$NON-NLS-1$
	private static final String THROW = "throw "; //$NON-NLS-1$
	private static final String STRUCT = "struct "; //$NON-NLS-1$
	private static final String UNION = "union "; //$NON-NLS-1$
	private static final String BRIEF = "brief "; //$NON-NLS-1$
	private static final String PARAM = "param "; //$NON-NLS-1$
	private static final String TPARAM = "tparam "; //$NON-NLS-1$
	private static final String RETURN = "return"; //$NON-NLS-1$
	private static final String PREFIX_JAVADOC = "@"; //$NON-NLS-1$
	private static final String PREFIX_NO_JAVADOC = "\\"; //$NON-NLS-1$

	protected boolean documentPureVirtuals = true;
	protected boolean documentDeclarations = true;

	private boolean javadocStyle;
	private String fLineDelimiter;

	/**
	 * Default constructor, Javadoc style is used for tags
	 */
	public DoxygenMultilineAutoEditStrategy() {
		this(true);
	}

	/**
	 * Constructor to define doxygen style.
	 * @param javadoc True to create tags starting with @, false to create tags
	 * starting with \
	 * @since 6.7
	 */
	public DoxygenMultilineAutoEditStrategy(boolean javadoc) {
		javadocStyle = javadoc;
	}

	private String getPrefix() {
		return javadocStyle ? PREFIX_JAVADOC : PREFIX_NO_JAVADOC;
	}

	/**
	 * @since 6.6
	 */
	@Override
	protected boolean isEnabled() {
		return true;
	}

	/**
	 * @param decl the function declarator to document
	 * @param ds the function specifier to document
	 * @param templateParams Template parameters for the function
	 * @return content describing the specified function
	 * @since 6.7
	 */
	protected StringBuilder documentComposite(IASTCompositeTypeSpecifier decl,
			ICPPASTTemplateParameter[] templateParams) {
		StringBuilder result = new StringBuilder();
		switch (decl.getKey()) {
		case ICPPASTCompositeTypeSpecifier.k_class:
			result.append(getPrefix()).append(CLASS).append(decl.getName().getSimpleID()).append(getLineDelimiter());
			break;
		case IASTCompositeTypeSpecifier.k_struct:
			result.append(getPrefix()).append(STRUCT).append(decl.getName().getSimpleID()).append(getLineDelimiter());
			break;
		case IASTCompositeTypeSpecifier.k_union:
			result.append(getPrefix()).append(UNION).append(decl.getName().getSimpleID()).append(getLineDelimiter());
			break;
		}
		result.append(getPrefix()).append(BRIEF).append(getLineDelimiter())
				.append(documentTemplateParameters(templateParams));
		return result;
	}

	/**
	 * Document a function/method
	 * @param decl the function declarator to document
	 * @param ds the function specifier to document
	 * @param templateParams Template parameters for the function
	 * @return content describing the specified function
	 * @since 6.7
	 */
	protected StringBuilder documentFunction(IASTFunctionDeclarator decl, IASTDeclSpecifier ds,
			ICPPASTTemplateParameter[] templateParams) {
		StringBuilder result = new StringBuilder();

		result.append(documentTemplateParameters(templateParams));
		result.append(documentFunctionParameters(getParameterDecls(decl)));
		if (decl instanceof ICPPASTFunctionDeclarator) {
			ICPPASTFunctionDeclarator cppDecl = (ICPPASTFunctionDeclarator) decl;
			result.append(documentExceptions(cppDecl.getExceptionSpecification(), cppDecl.getNoexceptExpression()));
		}

		boolean hasReturn = true;
		if (ds instanceof IASTSimpleDeclSpecifier) {
			IASTSimpleDeclSpecifier sds = (IASTSimpleDeclSpecifier) ds;
			if (sds.getType() == IASTSimpleDeclSpecifier.t_void
					|| sds.getType() == IASTSimpleDeclSpecifier.t_unspecified) {
				hasReturn = false;
			}
		}
		if (hasReturn) {
			result.append(documentFunctionReturn());
		}

		return result;
	}

	/**
	 * @deprecated This method is deprecated
	 * @param decl the function declarator to document
	 * @param ds the function specifier to document
	 * @return content describing the specified function
	 */
	@Deprecated
	protected StringBuilder documentFunction(IASTFunctionDeclarator decl, IASTDeclSpecifier ds) {
		return documentFunction(decl, ds, null);
	}

	/**
	 * Document template parameters
	 * @param templateParams The list of template parameters
	 * @return The built string
	 * @since 6.7
	 */
	protected StringBuilder documentTemplateParameters(ICPPASTTemplateParameter[] templateParams) {
		StringBuilder result = new StringBuilder();
		if (templateParams == null || templateParams.length == 0)
			return result;
		for (ICPPASTTemplateParameter t : templateParams) {
			IASTName name = CPPTemplates.getTemplateParameterName(t);
			result.append(getPrefix()).append(TPARAM).append(new String(name.getSimpleID())).append(getLineDelimiter());
		}
		return result;
	}

	/**
	 * Returns the comment content to add to the documentation comment.
	 * @param decls The parameter declarations to describe
	 * @return a buffer containing the comment content to generate to describe the parameters of
	 * the specified {@link IASTParameterDeclaration} objects.
	 */
	protected StringBuilder documentFunctionParameters(IASTParameterDeclaration[] decls) {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < decls.length; i++) {
			if (!isVoidParameter(decls[i])) {
				result.append(getPrefix()).append(PARAM).append(getParameterName(decls[i])).append(getLineDelimiter());
			}
		}
		return result;
	}

	/**
	 * Get the default line delimiter for the currently customized document
	 * which should be used for new lines.
	 *
	 * @return the default line delimiter
	 */
	private String getLineDelimiter() {
		return fLineDelimiter;
	}

	/**
	 * @param decl
	 * @return the name of the parameter
	 */
	String getParameterName(IASTParameterDeclaration decl) {
		IASTDeclarator dtor = decl.getDeclarator();
		for (int i = 0; i < 8 && dtor.getName().getRawSignature().length() == 0
				&& dtor.getNestedDeclarator() != null; i++) {
			dtor = dtor.getNestedDeclarator();
		}
		return dtor.getName().getRawSignature();
	}

	/**
	 * @param decl
	 * @return true if the specified parameter declaration is of void type
	 */
	boolean isVoidParameter(IASTParameterDeclaration decl) {
		if (decl.getDeclSpecifier() instanceof IASTSimpleDeclSpecifier) {
			if (((IASTSimpleDeclSpecifier) decl.getDeclSpecifier()).getType() == IASTSimpleDeclSpecifier.t_void) {
				IASTDeclarator dtor = decl.getDeclarator();
				if (dtor.getPointerOperators().length == 0) {
					if (!(dtor instanceof IASTFunctionDeclarator) && !(dtor instanceof IASTArrayDeclarator)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * @return the comment content to describe the return
	 */
	protected StringBuilder documentFunctionReturn() {
		return new StringBuilder(getPrefix()).append(RETURN).append(getLineDelimiter());
	}

	/**
	 * @param decl the function declarator to analyze
	 * @return the parameter declarations for the specified function definition
	 */
	protected IASTParameterDeclaration[] getParameterDecls(IASTFunctionDeclarator decl) {
		IASTParameterDeclaration[] result;
		if (decl instanceof IASTStandardFunctionDeclarator) {
			IASTStandardFunctionDeclarator standardFunctionDecl = (IASTStandardFunctionDeclarator) decl;
			result = standardFunctionDecl.getParameters();
		} else /*if (def instanceof ICASTKnRFunctionDeclarator) {
				ICASTKnRFunctionDeclarator knrDeclarator= (ICASTKnRFunctionDeclarator)decl;
				result= knrDeclarator.getParameterDeclarations();
				} else */ {
			result = new IASTParameterDeclaration[0];
		}
		return result;
	}

	/*
	 * @see org.eclipse.cdt.ui.text.doctools.DefaultMultilineCommentAutoEditStrategy#customizeAfterNewLineForDeclaration(org.eclipse.jface.text.IDocument, org.eclipse.cdt.core.dom.ast.IASTDeclaration, org.eclipse.jface.text.ITypedRegion)
	 */
	/**
	 * @since 6.7
	 */
	@Override
	public StringBuilder customizeAfterNewLineForDeclaration(IDocument doc, IASTDeclaration dec, ITypedRegion partition,
			CustomizeOptions options) {
		fLineDelimiter = TextUtilities.getDefaultLineDelimiter(doc);

		IASTDeclaration declToDocument = dec;

		if (declToDocument instanceof ICPPASTLinkageSpecification) {
			ICPPASTLinkageSpecification linkageSpecification = (ICPPASTLinkageSpecification) declToDocument;
			IASTDeclaration[] declarations = linkageSpecification.getDeclarations();

			if (declarations.length == 1) {

				boolean isCurlyExtern = false;
				IToken token = null;

				try {
					token = declarations[0].getTrailingSyntax();
				} catch (UnsupportedOperationException e) {
					return new StringBuilder();
				} catch (ExpansionOverlapsBoundaryException e) {
					return new StringBuilder();
				}

				if (token != null && token.getType() == IToken.tRBRACE) {
					isCurlyExtern = true;
				}

				if (!isCurlyExtern) {
					declToDocument = declarations[0];
				}
			}
		}

		ICPPASTTemplateParameter[] params = null;
		if (declToDocument instanceof ICPPASTTemplateDeclaration) {
			params = ((ICPPASTTemplateDeclaration) declToDocument).getTemplateParameters();
		}

		while (declToDocument instanceof ICPPASTTemplateDeclaration) /* if? */
			declToDocument = ((ICPPASTTemplateDeclaration) declToDocument).getDeclaration();

		if (declToDocument instanceof IASTFunctionDefinition) {
			IASTFunctionDefinition fd = (IASTFunctionDefinition) declToDocument;
			return documentFunction(fd.getDeclarator(), fd.getDeclSpecifier(), params);
		}

		if (declToDocument instanceof IASTSimpleDeclaration) {
			IASTSimpleDeclaration sdec = (IASTSimpleDeclaration) declToDocument;

			if (sdec.getDeclSpecifier() instanceof IASTCompositeTypeSpecifier) {
				if (options != null)
					options.keepFirstLine = false;
				return documentComposite((IASTCompositeTypeSpecifier) sdec.getDeclSpecifier(), params);
			} else {
				IASTDeclarator[] dcs = sdec.getDeclarators();
				if (dcs.length == 1 && dcs[0] instanceof IASTFunctionDeclarator) {
					IASTFunctionDeclarator fdecl = (IASTFunctionDeclarator) dcs[0];
					boolean shouldDocument = documentDeclarations;
					if (documentPureVirtuals && dcs[0] instanceof ICPPASTFunctionDeclarator) {
						ICPPASTFunctionDeclarator cppfdecl = (ICPPASTFunctionDeclarator) dcs[0];
						shouldDocument = shouldDocument || cppfdecl.isPureVirtual();
					}

					if (shouldDocument) {
						return documentFunction(fdecl, sdec.getDeclSpecifier(), params);
					}
				}
			}
		}

		StringBuilder builder = new StringBuilder();
		if (dec instanceof IASTSimpleDeclaration
				&& ((IASTSimpleDeclaration) dec).getDeclSpecifier() instanceof IASTEnumerationSpecifier) {
			if (options != null)
				options.keepFirstLine = false;
			builder = documentEnum((IASTEnumerationSpecifier) ((IASTSimpleDeclaration) dec).getDeclSpecifier());
		}

		try {
			alterDoc(doc, declToDocument);
		} catch (BadLocationException ble) {
			/*ignore*/
		}

		return builder;
	}

	/**
	 * Document enums
	 * @param dec Enumeration specifier
	 * @return The built buffer
	 * @since 6.7
	 */
	protected StringBuilder documentEnum(IASTEnumerationSpecifier dec) {
		StringBuilder result = new StringBuilder();
		result.append(getPrefix()).append(ENUM).append(new String(dec.getName().getSimpleID()))
				.append(getLineDelimiter()).append(getPrefix()).append(BRIEF).append(getLineDelimiter());
		return result;
	}

	/**
	 * Document function exceptions
	 * @param exceptions A list of exceptions or NO_EXCEPTION_SPECIFICATION if no exceptions are present
	 * or EMPTY_TYPEID_ARRAY if no exceptions will be thrown.
	 * @param noexcept Noexcept expression, null if no present, NOEXCEPT_DEFAULT if noexcept has been used
	 * @return The built string
	 * @since 6.7
	 */
	protected StringBuilder documentExceptions(IASTTypeId[] exceptions, ICPPASTExpression noexcept) {
		StringBuilder result = new StringBuilder();
		if (exceptions == ICPPASTFunctionDeclarator.NO_EXCEPTION_SPECIFICATION
				|| exceptions == IASTTypeId.EMPTY_TYPEID_ARRAY) {
			if (noexcept != null && noexcept != ICPPASTFunctionDeclarator.NOEXCEPT_DEFAULT
					&& !isNoexceptTrue(noexcept)) {
				result.append(getPrefix()).append(THROW).append(getLineDelimiter());
			}
		} else {
			for (int i = 0; i < exceptions.length; i++) {
				result.append(getPrefix()).append(THROW);
				if (exceptions[i] instanceof ICPPASTTypeId && ((ICPPASTTypeId) exceptions[i]).isPackExpansion()) {
					result.append(getLineDelimiter());
					continue;
				}
				result.append(ASTStringUtil.getSignatureString(exceptions[i].getAbstractDeclarator()))
						.append(getLineDelimiter());
			}
		}
		return result;
	}

	private boolean isNoexceptTrue(ICPPASTExpression expr) {
		if (expr instanceof IASTLiteralExpression) {
			return ((IASTLiteralExpression) expr).getKind() == IASTLiteralExpression.lk_true;
		}
		return false;
	}

	/*
	 * Add post-declaration comments to enumerators, after initializing a doc-comment on an enumeration
	 */
	private void alterDoc(IDocument doc, IASTDeclaration dec) throws BadLocationException {
		if (dec instanceof IASTSimpleDeclaration
				&& ((IASTSimpleDeclaration) dec).getDeclSpecifier() instanceof IASTEnumerationSpecifier) {
			IASTEnumerationSpecifier spc = (IASTEnumerationSpecifier) ((IASTSimpleDeclaration) dec).getDeclSpecifier();
			IASTEnumerator[] enms = spc.getEnumerators();

			class Entry {
				final int offset, length;
				StringBuilder comment;

				Entry(int offset, int length, String comment) {
					this.offset = offset;
					this.length = length;
					this.comment = new StringBuilder(comment);
				}

				@Override
				public int hashCode() {
					return offset;
				}

				@Override
				public boolean equals(Object obj) {
					if (obj instanceof Entry) {
						Entry other = (Entry) obj;
						return offset == other.offset;
					}
					return false;
				}
			}

			boolean noCollisions = true;
			LinkedHashSet<Entry> entries = new LinkedHashSet<>();
			for (IASTEnumerator enumerator : enms) {
				IASTNodeLocation loc = enumerator.getName().getFileLocation();
				if (loc != null) {
					int nodeOffset = loc.getNodeOffset() + loc.getNodeLength();
					String cmt = SINGLELINE_COMMENT_PRECEDING + enumerator.getName();
					IRegion line = doc.getLineInformationOfOffset(nodeOffset);
					if (!doc.get(line.getOffset(), line.getLength()).contains("//")) { //$NON-NLS-1$
						noCollisions &= entries.add(new Entry(line.getOffset(), line.getLength(), cmt));
					}
				}
			}

			/*
			 * Only auto-insert comments if each enumerator is declared on a unique line
			 */
			if (noCollisions) {
				int max = Integer.MIN_VALUE;
				for (Entry e : entries) {
					if (e.length > max)
						max = e.length;
				}

				int addedLength = 0;
				for (Entry e : entries) {
					// pad with whitespace
					int toAdd = max - e.length;
					for (int j = 0; j < toAdd; j++) {
						e.comment.insert(0, " "); //$NON-NLS-1$
					}
					doc.replace(e.offset + e.length + addedLength, 0, e.comment.toString());
					addedLength += e.comment.length();
				}
			}
		}
	}
}
