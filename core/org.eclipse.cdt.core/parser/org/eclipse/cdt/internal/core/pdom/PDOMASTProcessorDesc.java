/*******************************************************************************
 * Copyright (c) 2013 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom;

import java.util.Arrays;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IPDOMASTProcessor;
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
 * Internal container for extensions of org.eclipse.cdt.core.PDOMASTProcessor.  The implementation
 * of the processor is instantiated only after checking the enablement expression (if present) for
 * the given AST translation unit. This avoids activating the contributing plugin until it is
 * actually needed.
 */
public class PDOMASTProcessorDesc {
	private static final String Attr_Class = "class"; //$NON-NLS-1$

	private final IConfigurationElement element;
	private final Expression enablementExpression;
	private Boolean fStatus = null;

	private String id;
	private IPDOMASTProcessor processor;

	private static final String VAR_PROJECTNATURES = "projectNatures"; //$NON-NLS-1$
	private static final String VAR_LANGUAGEID = "languageId"; //$NON-NLS-1$

	/**
	 * An empty implementation of the processor used as a placeholder in descriptors that are unable
	 * to load the contributed class.
	 */
	private static final IPDOMASTProcessor NULL_PROCESSOR = new IPDOMASTProcessor.Abstract() {
	};

	public PDOMASTProcessorDesc(IConfigurationElement element) {
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

	private boolean matches(ITranslationUnit tu) {
		// If the enablement expression is missing or structurally invalid, then return immediately
		if (fStatus != null)
			return fStatus.booleanValue();

		// If there is no tu, then the enablement expression cannot be evaluated, assume that all
		// taggers are needed
		if (tu == null)
			return true;

		if (enablementExpression != null)
			try {
				IProject project = null;
				ICProject cProject = tu.getCProject();
				if (cProject != null)
					project = cProject.getProject();

				EvaluationContext evalContext = new EvaluationContext(null, project);

				// if the project is not accessible, then only taggers that don't care about it will
				// get a chance to run
				if (project != null) {
					String[] natures = project.getDescription().getNatureIds();
					evalContext.addVariable(VAR_PROJECTNATURES, Arrays.asList(natures));
				}

				ILanguage language = tu.getLanguage();
				if (language != null)
					evalContext.addVariable(VAR_LANGUAGEID, language.getId());

				return enablementExpression.evaluate(evalContext) == EvaluationResult.TRUE;
			} catch (CoreException e) {
				CCorePlugin.log("Error while evaluating enablement expression for " + id, e); //$NON-NLS-1$
			}

		fStatus = Boolean.FALSE;
		return false;
	}

	private IPDOMASTProcessor getProcessor() {
		if (processor == null)
			synchronized (this) {
				if (processor == null) {
					try {
						processor = (IPDOMASTProcessor) element.createExecutableExtension(Attr_Class);
					} catch (CoreException e) {
						String id = element.getDeclaringExtension().getNamespaceIdentifier() + '.'
								+ element.getDeclaringExtension().getSimpleIdentifier();
						CCorePlugin.log("Error in class attribute of " + id, e); //$NON-NLS-1$

						// mark the tagger with an empty implementation to prevent future load attempts
						processor = NULL_PROCESSOR;
					}
				}
			}

		return processor;
	}

	// Activates the plugin if needed.
	public IPDOMASTProcessor getProcessorFor(IASTTranslationUnit ast) {
		// If there isn't an ast with an AST-TU accessible, then there is no way to defer processing,
		// just return the processor and let it try to sort things out. E.g., this happens for built-in
		// things.
		if (ast == null)
			return getProcessor();

		// Otherwise evaluate the enablement expression for this TU
		return matches(ast.getOriginatingTranslationUnit()) ? getProcessor() : null;
	}
}
