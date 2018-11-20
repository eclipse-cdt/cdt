/*
 * Copyright (c) 2013, 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.cdt.internal.qt.ui.assist;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTypeId;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.corext.template.c.CContextType;
import org.eclipse.cdt.internal.qt.core.QtKeywords;
import org.eclipse.cdt.internal.qt.core.index.IQProperty;
import org.eclipse.cdt.internal.qt.core.parser.QtParser;
import org.eclipse.cdt.internal.qt.ui.Activator;
import org.eclipse.cdt.internal.ui.text.CHeuristicScanner;
import org.eclipse.cdt.internal.ui.text.Symbols;
import org.eclipse.cdt.internal.ui.text.contentassist.CCompletionProposal;
import org.eclipse.cdt.ui.text.contentassist.ICEditorContentAssistInvocationContext;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContextType;

/**
 * A utility class for accessing parts of the Q_PROPERTY expansion that have already
 * been entered as well as the offset of various parts of the declaration.  This is
 * used for things like proposing only parameters that are not already used, offering
 * appropriate suggestions for a specific parameter, etc.
 */
@SuppressWarnings("restriction")
public class QPropertyExpansion {

	/** The full text of the expansion */
	private final String expansion;

	/** The offset of the first character in the attributes section.  This is usually the
	 *  start of READ. */
	private final int startOfAttrs;

	/** The offset of the cursor in the expansion. */
	private final int cursor;

	/** The parsed type of the property. */
	private final IType type;

	/** The parsed name of the property.  This is the last identifier before the first attribute. */
	private final String name;

	/** The identifier at which the cursor is currently pointing. */
	private final Identifier currIdentifier;

	/** The identifier before the one where the cursor is pointing.  This is needed to figure out what
	 *  values are valid for an attribute like READ, WRITE, etc. */
	private final Identifier prevIdentifier;

	// The type/name section ends right before the first attribute.
	private static final Pattern TYPENAME_REGEX;
	static {
		StringBuilder regexBuilder = new StringBuilder();
		regexBuilder.append("^(?:Q_PROPERTY\\s*\\()?\\s*(.*?)(\\s+)(?:");
		for (IQProperty.Attribute attr : IQProperty.Attribute.values()) {
			if (attr.ordinal() > 0)
				regexBuilder.append('|');
			regexBuilder.append("(?:");
			regexBuilder.append(attr.identifier);
			regexBuilder.append(")");
		}
		regexBuilder.append(").*$");
		TYPENAME_REGEX = Pattern.compile(regexBuilder.toString());
	}

	/**
	 * A small utility to store the important parts of an identifier.  This is just the starting
	 * offset and the text of the identifier.
	 */
	private static class Identifier {
		public final int start;
		public final String ident;

		public Identifier(int start, String ident) {
			this.start = start;
			this.ident = ident;
		}

		@Override
		public String toString() {
			return Integer.toString(start) + ':' + ident;
		}
	}

	public static QPropertyExpansion create(ICEditorContentAssistInvocationContext context) {

		// Extract the substring that likely contributes to this Q_PROPERTY declaration.  The declaration
		// could be in any state of being entered, so use the HeuristicScanner to guess about the
		// possible structure.  The fixed assumptions are that the content assistant was invoked within
		// the expansion parameter of Q_PROPERTY.  We try to guess at the end of the String, which is
		// either the closing paren (within 512 characters from the opening paren) or the current cursor
		// location.

		// The offset is always right after the opening paren, use it to get to a fixed point in the
		// declaration.
		int offset = context.getContextInformationOffset();
		if (offset < 0)
			return null;

		IDocument doc = context.getDocument();
		CHeuristicScanner scanner = new CHeuristicScanner(doc);

		// We should only need to backup the length of Q_PROPERTY, but allow extra to deal
		// with whitespace.
		int lowerBound = Math.max(0, offset - 64);

		// Allow up to 512 characters from the opening paren.
		int upperBound = Math.min(doc.getLength(), offset + 512);

		int openingParen = scanner.findOpeningPeer(offset, lowerBound, '(', ')');
		if (openingParen == CHeuristicScanner.NOT_FOUND)
			return null;

		int token = scanner.previousToken(scanner.getPosition() - 1, lowerBound);
		if (token != Symbols.TokenIDENT)
			return null;

		// Find the start of the previous identifier.  This scans backward, so it stops one
		// position before the identifier (unless the identifer is at the start of the content).
		int begin = scanner.getPosition();
		if (begin != 0)
			++begin;

		String identifier = null;
		try {
			identifier = doc.get(begin, openingParen - begin);
		} catch (BadLocationException e) {
			Activator.log(e);
		}

		if (!QtKeywords.Q_PROPERTY.equals(identifier))
			return null;

		// advance past the opening paren
		++openingParen;

		String expansion = null;
		int closingParen = scanner.findClosingPeer(openingParen, upperBound, '(', ')');

		// This expansion is not applicable if the assistant was invoked after the closing paren.
		if (closingParen != CHeuristicScanner.NOT_FOUND && context.getInvocationOffset() > scanner.getPosition())
			return null;

		try {
			if (closingParen != CHeuristicScanner.NOT_FOUND)
				expansion = doc.get(openingParen, closingParen - openingParen);
			else
				expansion = doc.get(openingParen, context.getInvocationOffset() - openingParen);
		} catch (BadLocationException e) {
			Activator.log(e);
		}

		if (expansion == null)
			return null;

		int cursor = context.getInvocationOffset();
		Identifier currIdentifier = identifier(doc, scanner, cursor, lowerBound, upperBound);
		if (currIdentifier == null)
			return null;
		Identifier prevIdentifier = identifier(doc, scanner, currIdentifier.start - 1, lowerBound, upperBound);

		// There are two significant regions in a Q_PROPERTY declaration.  The first is everything
		// between the opening paren and the first parameter.  This region specifies the type and the
		// name.  The other is the region that declares all the parameters.  There is an arbitrary
		// amount of whitespace between these regions.
		//
		// This function finds and returns the offset of the end of the region containing the type and
		// name.  Returns 0 if the type/name region cannot be found.
		IType type = null;
		String name = null;
		int endOfTypeName = 0;
		Matcher m = TYPENAME_REGEX.matcher(expansion);
		if (m.matches()) {
			endOfTypeName = openingParen + m.end(2);

			// parse the type/name part and then extract the type and name from the result
			ICPPASTTypeId typeId = QtParser.parseTypeId(m.group(1));
			type = CPPVisitor.createType(typeId);

			IASTDeclarator declarator = typeId.getAbstractDeclarator();
			if (declarator != null && declarator.getName() != null)
				name = declarator.getName().toString();
		}

		return new QPropertyExpansion(expansion, endOfTypeName, cursor, type, name, prevIdentifier, currIdentifier);
	}

	private QPropertyExpansion(String expansion, int startOfAttrs, int cursor, IType type, String name, Identifier prev,
			Identifier curr) {
		this.expansion = expansion;
		this.startOfAttrs = startOfAttrs;
		this.cursor = cursor;

		this.type = type;
		this.name = name;
		this.prevIdentifier = prev;
		this.currIdentifier = curr;
	}

	public String getCurrIdentifier() {
		return currIdentifier.ident;
	}

	public String getPrevIdentifier() {
		return prevIdentifier.ident;
	}

	public String getPrefix() {
		if (currIdentifier.ident == null)
			return null;

		if (cursor > currIdentifier.start + currIdentifier.ident.length())
			return null;

		return currIdentifier.ident.substring(0, cursor - currIdentifier.start);
	}

	private static class Attribute {
		public final IQProperty.Attribute attribute;
		public final int relevance;

		public Attribute(IQProperty.Attribute attribute) {
			this.attribute = attribute;

			// Give attribute proposals the same order as the Qt documentation.
			switch (attribute) {
			case READ:
				this.relevance = 11;
				break;
			case WRITE:
				this.relevance = 10;
				break;
			case RESET:
				this.relevance = 9;
				break;
			case NOTIFY:
				this.relevance = 8;
				break;
			case REVISION:
				this.relevance = 7;
				break;
			case DESIGNABLE:
				this.relevance = 6;
				break;
			case SCRIPTABLE:
				this.relevance = 5;
				break;
			case STORED:
				this.relevance = 4;
				break;
			case USER:
				this.relevance = 3;
				break;
			case CONSTANT:
				this.relevance = 2;
				break;
			case FINAL:
				this.relevance = 1;
				break;
			default:
				this.relevance = 0;
				break;
			}
		}

		public ICompletionProposal getProposal(String contextId, ICEditorContentAssistInvocationContext context) {

			// Attributes without values propose only their own identifier.
			if (!attribute.hasValue)
				return new CCompletionProposal(attribute.identifier, context.getInvocationOffset(), 0,
						Activator.getQtLogo(), attribute.identifier + " - Q_PROPERTY declaration parameter", relevance);

			// Otherwise create a template where the content depends on the type of the attribute's parameter.
			String display = attribute.identifier + ' ' + attribute.paramName;
			String replacement = attribute.identifier;
			if ("bool".equals(attribute.paramName))
				replacement += " ${true}";
			else if ("int".equals(attribute.paramName))
				replacement += " ${0}";
			else if (attribute.paramName != null)
				replacement += " ${" + attribute.paramName + '}';

			return templateProposal(contextId, context, display, replacement, relevance);
		}
	}

	private static ICompletionProposal templateProposal(String contextId,
			ICEditorContentAssistInvocationContext context, String display, String replacement, int relevance) {
		Template template = new Template(display, "Q_PROPERTY declaration parameter", contextId, replacement, true);

		TemplateContextType ctxType = new CContextType();
		ctxType.setId(contextId);

		QtProposalContext templateCtx = new QtProposalContext(context, ctxType);
		Region region = new Region(templateCtx.getCompletionOffset(), templateCtx.getCompletionLength());
		return new QtTemplateProposal(template, templateCtx, region, relevance);
	}

	public List<ICompletionProposal> getProposals(String contextId, ICEditorContentAssistInvocationContext context) {

		// Make no suggestions when the start of the current identifier is before the end of
		// the "type name" portion of the declaration.
		if (currIdentifier.start < startOfAttrs)
			return Collections.emptyList();

		// Propose nothing but READ as the first attribute.  If the previous identifier is before
		// the end of the typeName region, then we're currently at the first attribute.
		if (prevIdentifier.start < startOfAttrs)
			return Collections.singletonList(new Attribute(IQProperty.Attribute.READ).getProposal(contextId, context));

		// If the previous token is an Attribute name that has a parameter then suggest appropriate
		// values for that parameter.  Otherwise suggest the other Attribute names.

		String prefix = getPrefix();

		// There are two types of proposals.  If the previous identifier matches a known attribute name,
		// then we propose possible values for that attribute.  Otherwise we want to propose the identifiers
		// that don't already appear in the expansion.
		//
		// This is implemented by iterating over the list of known attributes.  If any of the attributes
		// matches the previous identifier, then we build and return a list of valid proposals for that
		// attribute.
		//
		// Otherwise, for each attribute we build a regular expression that checks to see if that token
		// appears within the expansion.  If it already appears, then the attribute is ignored.  Otherwise
		// it is added as an unspecified attribute.  If the loop completes, then we create a list of proposals
		// for from that unspecified list.

		List<Attribute> unspecifiedAttributes = new ArrayList<>();
		for (IQProperty.Attribute attr : IQProperty.Attribute.values()) {
			if (attr.hasValue && (prevIdentifier != null && attr.identifier.equals(prevIdentifier.ident))) {

				Collection<QPropertyAttributeProposal> attrProposals = QPropertyAttributeProposal.buildProposals(attr,
						context, type, name);
				if (attrProposals != null) {
					List<ICompletionProposal> proposals = new ArrayList<>();
					for (QPropertyAttributeProposal value : attrProposals)
						if (prefix == null || value.getIdentifier().startsWith(prefix))
							proposals.add(value.createProposal(prefix, context.getInvocationOffset()));
					return proposals;
				}

				return Collections.emptyList();
			}

			if (prefix != null) {
				if (attr.identifier.startsWith(prefix) && (!expansion.matches(".*\\s+" + attr.identifier + "\\s+.*")
						|| attr.identifier.equals(currIdentifier.ident)))
					unspecifiedAttributes.add(new Attribute(attr));
			} else if (!expansion.matches(".*\\s+" + attr.identifier + "\\s+.*"))
				unspecifiedAttributes.add(new Attribute(attr));
		}

		List<ICompletionProposal> proposals = new ArrayList<>();
		for (Attribute attr : unspecifiedAttributes) {
			ICompletionProposal proposal = attr.getProposal(contextId, context);
			if (proposal != null)
				proposals.add(proposal);
		}

		return proposals;
	}

	private static Identifier identifier(IDocument doc, CHeuristicScanner scanner, int cursor, int lower, int upper) {
		try {
			// If the cursor is in whitespace, then the current identifier is null.  Scan backward to find
			// the start of this whitespace.
			if (Character.isWhitespace(doc.getChar(cursor - 1))) {
				int prev = scanner.findNonWhitespaceBackward(cursor, lower);
				return new Identifier(Math.min(cursor, prev + 1), null);
			}

			int tok = scanner.previousToken(cursor, lower);
			if (tok != CHeuristicScanner.TokenIDENT)
				return null;
			int begin = scanner.getPosition() + 1;

			tok = scanner.nextToken(begin, upper);
			if (tok != CHeuristicScanner.TokenIDENT)
				return null;
			int end = scanner.getPosition();

			return new Identifier(begin, doc.get(begin, end - begin));
		} catch (BadLocationException e) {
			Activator.log(e);
		}
		return null;
	}

	@Override
	public String toString() {
		if (expansion == null)
			return super.toString();

		if (cursor >= expansion.length())
			return expansion + '|';
		if (cursor < 0)
			return "|" + expansion;

		return expansion.substring(0, cursor) + '|' + expansion.substring(cursor);
	}
}
