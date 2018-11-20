/*
 * Copyright (c) 2013, 2014 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.cdt.internal.ui.editor;

import java.util.Arrays;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.text.ISemanticHighlighter;
import org.eclipse.cdt.ui.text.ISemanticToken;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionConverter;
import org.eclipse.core.expressions.ExpressionTagNames;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.resource.DataFormatException;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.swt.graphics.RGB;

public class ContributedSemanticHighlighting extends SemanticHighlightingWithOwnPreference {

	/**
	 * The configuration element needs to be cached until the class is instantiated. Instantiation is deferred
	 * to avoid loading the contributing plugin when the highlighter is not actually needed.
	 */
	private IConfigurationElement configurationElement;
	private Boolean fStatus = null;
	private ISemanticHighlighter semanticHighlighter;

	private final Expression enablementExpression;

	private final int priority;
	private final String id;
	private final String preferenceKey;
	private final String displayName;

	private final RGB defaultTextColor;
	private final boolean defaultBold;
	private final boolean defaultItalic;
	private final boolean defaultStrikethrough;
	private final boolean defaultUnderline;
	private final boolean defaultEnabled;

	private static final String Attr_Class = "class"; //$NON-NLS-1$
	private static final String Attr_Priority = "priority"; //$NON-NLS-1$

	private static final String Attr_PrefKey = "preferenceKey"; //$NON-NLS-1$
	private static final String Attr_DisplayName = "displayName"; //$NON-NLS-1$
	private static final String Attr_DefaultTextColor = "defaultTextColor"; //$NON-NLS-1$
	private static final String Attr_DefaultBold = "defaultBold"; //$NON-NLS-1$

	private static final String Attr_DefaultItalic = "defaultItalic"; //$NON-NLS-1$
	private static final String Attr_DefaultStrikethrough = "defaultStrikethrough"; //$NON-NLS-1$
	private static final String Attr_DefaultUnderline = "defaultUnderline"; //$NON-NLS-1$
	private static final String Attr_DefaultEnabled = "defaultEnabled"; //$NON-NLS-1$

	private static final String Var_projectNature = "projectNatures"; //$NON-NLS-1$
	private static final String Var_languageId = "languageId"; //$NON-NLS-1$
	private static final int Default_Priority = 1000;

	public ContributedSemanticHighlighting(IConfigurationElement element) {
		configurationElement = element;

		// required
		id = element.getDeclaringExtension().getNamespaceIdentifier() + '.'
				+ element.getDeclaringExtension().getSimpleIdentifier();

		int pri = Default_Priority;
		String priStr = element.getAttribute(Attr_Priority);
		if (priStr != null)
			try {
				pri = Integer.parseInt(priStr);
			} catch (NumberFormatException e) {
				CUIPlugin.log("Error in priority attribute of " + id + " was " + priStr, e); //$NON-NLS-1$ //$NON-NLS-2$
			}
		priority = pri;

		Expression expr = null;
		IConfigurationElement[] children = element.getChildren(ExpressionTagNames.ENABLEMENT);
		switch (children.length) {
		case 0:
			fStatus = Boolean.TRUE;
			break;
		case 1:
			try {
				ExpressionConverter parser = ExpressionConverter.getDefault();
				expr = parser.perform(children[0]);
			} catch (CoreException e) {
				CUIPlugin.log("Error in enablement expression of " + id, e); //$NON-NLS-1$
			}
			break;
		default:
			CUIPlugin.logError("Too many enablement expressions for " + id); //$NON-NLS-1$
			fStatus = Boolean.FALSE;
			break;
		}
		enablementExpression = expr;

		preferenceKey = element.getAttribute(Attr_PrefKey);
		displayName = element.getAttribute(Attr_DisplayName);

		// optional
		defaultTextColor = getRGBAttribute(element, id, Attr_DefaultTextColor);
		defaultBold = Boolean.parseBoolean(element.getAttribute(Attr_DefaultBold));
		defaultItalic = Boolean.parseBoolean(element.getAttribute(Attr_DefaultItalic));
		defaultStrikethrough = Boolean.parseBoolean(element.getAttribute(Attr_DefaultStrikethrough));
		defaultUnderline = Boolean.parseBoolean(element.getAttribute(Attr_DefaultUnderline));
		defaultEnabled = Boolean.parseBoolean(element.getAttribute(Attr_DefaultEnabled));
	}

	public String getId() {
		return id;
	}

	public int getPriority() {
		return priority;
	}

	private static RGB getRGBAttribute(IConfigurationElement element, String extensionId, String key) {
		String val = element.getAttribute(key);
		if (val != null)
			try {
				return StringConverter.asRGB(val);
			} catch (DataFormatException e) {
				CUIPlugin.log("Error in " + Attr_DefaultTextColor + " attribute of " + extensionId + ' ' + val //$NON-NLS-1$//$NON-NLS-2$
						+ " is not a RGB value", e); //$NON-NLS-1$
			}

		// black by default
		return new RGB(0, 0, 0);
	}

	private ISemanticHighlighter createSemanticHighlighter() {

		// only one try at creating the class
		if (configurationElement == null)
			return null;

		IConfigurationElement element = configurationElement;
		configurationElement = null;

		try {
			return (ISemanticHighlighter) element.createExecutableExtension(Attr_Class);
		} catch (CoreException e) {
			CUIPlugin.log("Error in class attribute of " + id, e); //$NON-NLS-1$
		}

		return null;
	}

	@Override
	public String getPreferenceKey() {
		return preferenceKey;
	}

	@Override
	public RGB getDefaultDefaultTextColor() {
		return defaultTextColor;
	}

	@Override
	public boolean isBoldByDefault() {
		return defaultBold;
	}

	@Override
	public boolean isItalicByDefault() {
		return defaultItalic;
	}

	@Override
	public boolean isStrikethroughByDefault() {
		return defaultStrikethrough;
	}

	@Override
	public boolean isUnderlineByDefault() {
		return defaultUnderline;
	}

	@Override
	public boolean isEnabledByDefault() {
		return defaultEnabled;
	}

	@Override
	public String getDisplayName() {
		return displayName;
	}

	@Override
	public boolean requiresImplicitNames() {
		return false;
	}

	private boolean matches(ITranslationUnit tu) {

		// if the enablement expression is missing or structurally invalid, then return immediately
		if (fStatus != null)
			return fStatus.booleanValue();

		if (enablementExpression != null)
			try {
				EvaluationContext evalContext = new EvaluationContext(null, tu);

				ICProject cProject = tu.getCProject();
				String[] natures = cProject.getProject().getDescription().getNatureIds();
				evalContext.addVariable(Var_projectNature, Arrays.asList(natures));

				ILanguage language = tu.getLanguage();
				if (language != null)
					evalContext.addVariable(Var_languageId, language.getId());

				return enablementExpression.evaluate(evalContext) == EvaluationResult.TRUE;
			} catch (CoreException e) {
				CUIPlugin.log("Error while evaluating enablement expression for " + id, e); //$NON-NLS-1$
			}

		fStatus = Boolean.FALSE;
		return false;
	}

	/**
	 * Return the contributed ISemanticHighlighter if the receiver should be applied to the specified TU and
	 * null otherwise.
	 */
	private ISemanticHighlighter getSemanticHighlighter(ITranslationUnit tu) {
		if (!matches(tu))
			return null;

		if (semanticHighlighter == null)
			synchronized (this) {
				if (semanticHighlighter == null) {
					semanticHighlighter = createSemanticHighlighter();
				}
			}

		return semanticHighlighter;
	}

	@Override
	public boolean consumes(ISemanticToken token) {
		if (token == null)
			return false;

		IASTTranslationUnit astTU = token.getRoot();
		if (astTU == null)
			return false;

		ITranslationUnit tu = astTU.getOriginatingTranslationUnit();
		if (tu == null)
			return false;

		ISemanticHighlighter highlighter = getSemanticHighlighter(tu);
		if (highlighter == null)
			return false;

		return highlighter.consumes(token);
	}
}
