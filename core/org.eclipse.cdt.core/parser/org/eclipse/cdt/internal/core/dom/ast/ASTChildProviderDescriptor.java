/*
 * Copyright (c) 2013 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Eidsness - Initial implementation
 */

package org.eclipse.cdt.internal.core.dom.ast;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IASTChildProvider;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionConverter;
import org.eclipse.core.expressions.ExpressionTagNames;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;

/**
 * Internal container for extensions of org.eclipse.cdt.core.astChildProvider. The implementation of the
 * provider is instantiated only after checking the enablement expression (if present) for the specified
 * node, which avoids activating the contributing plugin until it is actually needed.
 */
public class ASTChildProviderDescriptor {

	private final IConfigurationElement element;
	private final Expression enablementExpression;
	private Boolean fStatus = null;

	private String id;
	private IASTChildProvider astChildProvider;

	private static final String ATTR_CLASS = "class"; //$NON-NLS-1$
	private static final String VAR_LANGUAGEID = "languageId"; //$NON-NLS-1$
	private static final String VAR_PARENT = "parent"; //$NON-NLS-1$
	private static final String VAR_PROJECTNATURES = "projectNatures"; //$NON-NLS-1$
	private static final String VAR_NODE = "node"; //$NON-NLS-1$

	/**
	 * An empty implementation of the searchParticipant used as a placeholder in descriptors that are unable
	 * to load the contributed class.
	 */
	private static final IASTChildProvider NullProvider = new IASTChildProvider() {
		@Override
		public Collection<IASTNode> getChildren(IASTExpression expr) {
			return null;
		}
	};

	public ASTChildProviderDescriptor(IConfigurationElement element) {
		this.element = element;

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
				CCorePlugin.log("Error in enablement expression of " + id, e); //$NON-NLS-1$
			}
			break;
		default:
			CCorePlugin.log("Too many enablement expressions for " + id); //$NON-NLS-1$
			fStatus = Boolean.FALSE;
			break;
		}
		enablementExpression = expr;
	}

	private boolean matches(ITranslationUnit tu, IASTNode node) {
		// if the enablement expression is missing or structurally invalid, then return immediately
		if (fStatus != null)
			return fStatus.booleanValue();

		// if there is no tu, then the enablement expression cannot be evaluated, assume that all taggers
		// are needed
		if (tu == null)
			return true;

		if (enablementExpression != null)
			try {
				IProject project = null;
				ICProject cProject = tu.getCProject();
				if (cProject != null)
					project = cProject.getProject();

				EvaluationContext evalContext = new EvaluationContext(null, project);

				evalContext.addVariable(VAR_NODE, node);

				// if the project is not accessible, then only providers that don't care about it will
				// get a chance to run
				if (project == null)
					evalContext.addVariable(VAR_PROJECTNATURES, Collections.emptyList());
				else {
					String[] natures = project.getDescription().getNatureIds();
					evalContext.addVariable(VAR_PROJECTNATURES, Arrays.asList(natures));
				}

				ILanguage language = tu.getLanguage();
				evalContext.addVariable(VAR_LANGUAGEID, language != null ? language.getId() : new String());

				IASTNode parent = node.getParent();
				evalContext.addVariable(VAR_PARENT, parent != null ? parent : new Object());

				return enablementExpression.evaluate(evalContext) == EvaluationResult.TRUE;
			} catch (CoreException e) {
				CCorePlugin.log("Error while evaluating enablement expression for " + id, e); //$NON-NLS-1$
			}

		fStatus = Boolean.FALSE;
		return false;
	}

	private IASTChildProvider getASTChildProvider() {
		if (astChildProvider == null)
			synchronized (this) {
				if (astChildProvider == null) {
					try {
						astChildProvider = (IASTChildProvider) element.createExecutableExtension(ATTR_CLASS);
					} catch (CoreException e) {
						String id = element.getDeclaringExtension().getNamespaceIdentifier() + '.'
								  + element.getDeclaringExtension().getSimpleIdentifier();
						CCorePlugin.log("Error in class attribute of " + id, e); //$NON-NLS-1$

						// mark the participant with an empty implementation to prevent future load attempts
						astChildProvider = NullProvider;
					}
				}
			}

		return astChildProvider;
	}

	// Activates the plugin if needed.
	public IASTChildProvider getASTChildProviderFor(IASTNode node) {
		// If there isn't an ast with an AST-TU accessible, then there is no way to defer processing,
		// just return the participant and let it try to sort things out. E.g., this happens for built-in
		// things.
		if (node == null)
			return getASTChildProvider();
		IASTTranslationUnit astTU = node.getTranslationUnit();
		if (astTU == null)
			return getASTChildProvider();

		// Otherwise evaluate the enablement expression for this TU
		return matches(astTU.getOriginatingTranslationUnit(), node) ? getASTChildProvider() : null;
	}
}
