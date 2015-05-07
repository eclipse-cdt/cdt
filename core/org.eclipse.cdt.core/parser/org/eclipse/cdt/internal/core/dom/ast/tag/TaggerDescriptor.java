/*******************************************************************************
 * Copyright (c) 2013 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Eidsness - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.ast.tag;

import java.util.Arrays;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.tag.IBindingTagger;
import org.eclipse.cdt.core.dom.ast.tag.ITag;
import org.eclipse.cdt.core.dom.ast.tag.ITagWriter;
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
 * Internal container for extensions of org.eclipse.cdt.core.tagger. The implementation of
 * the tagger is instantiated only after checking the enablement expression (if present) for
 * the specified binding. This avoids activating the contributing plugin until it is actually
 * needed.
 */
public class TaggerDescriptor {
	private static final String Attr_LocalId = "local-id"; //$NON-NLS-1$
	private static final String Attr_Class = "class"; //$NON-NLS-1$

	private final IConfigurationElement element;
	private final Expression enablementExpression;
	private Boolean fStatus = null;

	private String id;
	private IBindingTagger tagger;

	private static final String VAR_PROJECTNATURES = "projectNatures"; //$NON-NLS-1$
	private static final String VAR_LANGUAGEID = "languageId"; //$NON-NLS-1$

	/**
	 * An empty implementation of the tagger used as a placeholder in descriptors that are unable
	 * to load the contributed class.
	 */
	private static final IBindingTagger NULL_TAGGER = new IBindingTagger() {
		@Override
		public ITag process(ITagWriter tagWriter, IBinding binding, IASTName ast) {
			return null;
		}
	};

	public TaggerDescriptor(IConfigurationElement element) {
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

	public String getId() {
		if (id != null)
			return id;

		String globalId = element.getContributor().getName();
		String localId = element.getAttribute(Attr_LocalId);

		// there must be a valid local id
		if (localId == null) {
			String extId = element.getDeclaringExtension().getSimpleIdentifier();
			CCorePlugin.log("Invalid extension " + globalId + '.' + extId //$NON-NLS-1$
					+ " must provide tagger's local-id"); //$NON-NLS-1$
			return null;
		}

		// the extension should not include the plugin id, but return immediately if it does
		if (localId.startsWith(globalId) && localId.length() > globalId.length())
			return localId;

		// make sure the local id has real content
		if (localId.isEmpty()) {
			String extId = element.getDeclaringExtension().getSimpleIdentifier();
			CCorePlugin.log("Invalid extension " + globalId + '.' + extId //$NON-NLS-1$
					+ " must provide value for tagger's local-id"); //$NON-NLS-1$
			return null;
		}

		// otherwise prepend with the globalId, and ensure a dot between them
		if (localId.charAt(0) == '.')
			return globalId + localId;
		return globalId + '.' + localId;
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

	private IBindingTagger getTagger() {
		if (tagger == null)
			synchronized (this) {
				if (tagger == null) {
					try {
						tagger = (IBindingTagger) element.createExecutableExtension(Attr_Class);
					} catch (CoreException e) {
						String id = element.getDeclaringExtension().getNamespaceIdentifier() + '.'
								+ element.getDeclaringExtension().getSimpleIdentifier();
						CCorePlugin.log("Error in class attribute of " + id, e); //$NON-NLS-1$

						// mark the tagger with an empty implementation to prevent future load attempts
						tagger = NULL_TAGGER;
					}
				}
			}

		return tagger;
	}

	// Activates the plugin if needed.
	public IBindingTagger getBindingTaggerFor(IBinding binding, IASTName ast) {
		// If there isn't an ast with an AST-TU accessible, then there is no way to defer processing,
		// just return the tagger and let it try to sort things out. E.g., this happens for built-in
		// things.
		if (ast == null)
			return getTagger();
		IASTTranslationUnit astTU = ast.getTranslationUnit();
		if (astTU == null)
			return getTagger();

		// Otherwise evaluate the enablement expression for this TU
		return matches(astTU.getOriginatingTranslationUnit()) ? getTagger() : null;
	}
}
