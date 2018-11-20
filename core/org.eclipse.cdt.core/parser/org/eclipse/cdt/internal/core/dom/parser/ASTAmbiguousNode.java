/*******************************************************************************
 * Copyright (c) 2004, 2014 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPExecution;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalFixed;
import org.eclipse.cdt.internal.core.parser.ParserException;

/**
 * Base implementation for all ambiguous nodes.
 */
public abstract class ASTAmbiguousNode extends ASTNode {

	public static class NameCollector extends ASTVisitor {
		private IASTName[] names = new IASTName[2];
		private int namesPos = -1;

		public NameCollector() {
			shouldVisitNames = true;
		}

		@Override
		public int visit(IASTName name) {
			if (name != null) {
				namesPos++;
				names = ArrayUtil.append(IASTName.class, names, name);
			}
			return PROCESS_CONTINUE;
		}

		public IASTName[] getNames() {
			names = ArrayUtil.trimAt(IASTName.class, names, namesPos);
			return names;
		}
	}

	private IASTNode fResolution;

	/**
	 * Return the alternative nodes for this ambiguity.
	 */
	public abstract IASTNode[] getNodes();

	@Override
	public final boolean accept(ASTVisitor visitor) {
		if (visitor.shouldVisitAmbiguousNodes && visitor.visit(this) == ASTVisitor.PROCESS_ABORT)
			return false;

		// Alternatives are not visited on purpose.
		return true;
	}

	protected void beforeResolution() {
	}

	protected void beforeAlternative(IASTNode alternative) {
	}

	protected void afterResolution(ASTVisitor resolver, IASTNode best) {
	}

	public IASTNode resolveAmbiguity(ASTVisitor resolver) {
		return fResolution = doResolveAmbiguity(resolver);
	}

	protected IASTNode doResolveAmbiguity(ASTVisitor resolver) {
		beforeResolution();
		final IASTAmbiguityParent owner = (IASTAmbiguityParent) getParent();
		IASTNode nodeToReplace = this;

		final IASTNode[] alternatives = getNodes();
		IASTNode bestAlternative = null;

		int minIssues = Integer.MAX_VALUE;
		for (IASTNode alternative : alternatives) {
			// Setup the ast to use the alternative
			owner.replace(nodeToReplace, alternative);

			beforeAlternative(alternative);

			// Handle nested ambiguities
			alternative = resolveNestedAmbiguities(alternative, resolver);
			nodeToReplace = alternative;

			// Find nested names
			final NameCollector nameCollector = new NameCollector();
			alternative.accept(nameCollector);
			final IASTName[] names = nameCollector.getNames();

			// Resolve names and count issues
			int issues = 0;
			for (IASTName name : names) {
				try {
					// Avoid resolution of parameters (can always be resolved),
					// it can triggers resolution of declaration it belongs to,
					// while the declarator is still ambiguous. Could be solved by introducing an
					// intermediate binding for parameters, similar to template parameters.
					if (name.getPropertyInParent() == IASTDeclarator.DECLARATOR_NAME) {
						IASTNode parent = name.getParent();
						if (parent instanceof IASTDeclarator) {
							parent = ASTQueries.findOutermostDeclarator((IASTDeclarator) parent);
							if (parent.getPropertyInParent() == IASTParameterDeclaration.DECLARATOR)
								continue;
						}
					}
					IBinding b = name.resolvePreBinding();
					if (b instanceof IProblemBinding) {
						issues++;
					}
				} catch (Exception t) {
					issues++;
				}
				if (issues == minIssues) {
					break;
				}
			}
			if (issues < minIssues) {
				minIssues = issues;
				bestAlternative = alternative;
				if (issues == 0) {
					break;
				}
			}
		}

		// Switch back to the best alternative, if necessary.
		if (nodeToReplace != bestAlternative) {
			owner.replace(nodeToReplace, bestAlternative);
		}
		afterResolution(resolver, bestAlternative);
		return bestAlternative;
	}

	protected IASTNode resolveNestedAmbiguities(IASTNode alternative, ASTVisitor resolver) {
		alternative.accept(resolver);
		if (alternative instanceof ASTAmbiguousNode)
			return ((ASTAmbiguousNode) alternative).fResolution;
		return alternative;
	}

	public final IType getExpressionType() {
		logAmbiguousNodeError();
		return ProblemType.UNKNOWN_FOR_EXPRESSION;
	}

	public final ValueCategory getValueCategory() {
		logAmbiguousNodeError();
		return ValueCategory.PRVALUE;
	}

	public final boolean isLValue() {
		logAmbiguousNodeError();
		return false;
	}

	public final ICPPEvaluation getEvaluation() {
		logAmbiguousNodeError();
		return EvalFixed.INCOMPLETE;
	}

	public final ICPPExecution getExecution() {
		logAmbiguousNodeError();
		return null;
	}

	private void logAmbiguousNodeError() {
		CCorePlugin.log(new ParserException("Encountered an ambiguous node \"" + //$NON-NLS-1$
				getRawSignature() + "\" at " + getFileLocation().getFileName() + //$NON-NLS-1$
				", line " + getFileLocation().getStartingLineNumber() + //$NON-NLS-1$
				" while parsing " + getTranslationUnit().getContainingFilename())); //$NON-NLS-1$
	}
}
