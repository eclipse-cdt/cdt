/*******************************************************************************
 * Copyright (c) 2010 Meisam Fathi and others 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Meisam Fathi  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.checkers.fs;

import java.util.Iterator;

import org.eclipse.cdt.codan.core.cxx.model.AbstractIndexAstChecker;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IArrayType;
import org.eclipse.cdt.core.dom.ast.IType;

/**
 * This checker detects format string vulnerabilities in the source code of
 * C/C++ applications.
 * <p>
 * e.g:
 * <p>
 * <code>
 * int f() { <br>
 * char inputstr[5]; <br>
 * scanf("%s", inputstr); // detects vulnerability here <br>
 * return 0; <br>
 * }
 * </code>
 * <p>
 * e.g:
 * <p>
 * <code>
 * int f(void) { <br>
 * char inputstr[5]; <br>
 * int inputval; <br>
 * int i = 5; <br>
 * scanf("%d %9s", inputval, inputstr); // detects vulnerability here <br>
 * printf("%d" ,i); <br>
 * return 0; <br>
 * } <br>
 * </code> <br>
 * <p>
 * e.g:
 * <p>
 * <code>
 * int main(void) { <br>
 * char inputstr[5]; <br>
 * int inputval; <br>
 * int i = 5; <br>
 * scanf("%4s %i", inputstr, inputval); // no vulnerability here <br>
 * printf("%d" ,i); <br>
 * return 0; <br>
 * } <br>
 * </code>
 * 
 * @version 0.3 July 29, 2010
 * @author Meisam Fathi
 * 
 */
public class ScanfFormatStringSecurityChecker extends AbstractIndexAstChecker {
	private static final String ER_ID = "org.eclipse.cdt.codan.internal.checkers.ScanfFormatStringSecurityProblem"; //$NON-NLS-1$
	private final static VulnerableFunction[] VULNERABLE_FUNCTIONS = {//
	// list of all format string vulnerable functions
			new VulnerableFunction("scanf", 0), //$NON-NLS-1$
			new VulnerableFunction("fscanf", 1), //$NON-NLS-1$
			new VulnerableFunction("fwscanf", 1), //$NON-NLS-1$
			new VulnerableFunction("wscanf", 0), //$NON-NLS-1$
			new VulnerableFunction("swscanf", 1), //$NON-NLS-1$
			new VulnerableFunction("sscanf", 1) //$NON-NLS-1$
	};

	@Override
	public void processAst(IASTTranslationUnit ast) {
		ast.accept(new FormatStringVisitor());
	}

	private static final class VulnerableFunction {
		private final String name;
		private final int formatStringArgumentIndex;

		private VulnerableFunction(String name, int formatStringArgumentIndex) {
			this.name = name;
			this.formatStringArgumentIndex = formatStringArgumentIndex;
		}

		/**
		 * @return the name
		 */
		public String getName() {
			return name;
		}

		/**
		 * @return the formatStringArgumentIndex
		 */
		public int getFormatStringArgumentIndex() {
			return formatStringArgumentIndex;
		}
	}

	private class FormatStringVisitor extends ASTVisitor {
		private FormatStringVisitor() {
			shouldVisitExpressions = true;
		}

		@Override
		public int visit(IASTExpression expression) {
			if (expression instanceof IASTFunctionCallExpression) {
				IASTFunctionCallExpression callExpression = (IASTFunctionCallExpression) expression;
				VulnerableFunction vulnerableFunction = getVulnerableFunctionForExpression(callExpression);
				if (vulnerableFunction == null) {
					return PROCESS_CONTINUE;
				}
				IASTInitializerClause[] arguments = callExpression.getArguments();
				int stringArgumentIndex = vulnerableFunction.getFormatStringArgumentIndex();
				detectFaulyArguments(callExpression, arguments, stringArgumentIndex);
			}
			return PROCESS_CONTINUE;
		}

		private VulnerableFunction getVulnerableFunctionForExpression(IASTFunctionCallExpression callExpression) {
			String rawSignature = callExpression.getFunctionNameExpression().getRawSignature();
			for (int i = 0; i < VULNERABLE_FUNCTIONS.length; i++) {
				if (VULNERABLE_FUNCTIONS[i].getName().equals(rawSignature)) {
					return VULNERABLE_FUNCTIONS[i];
				}
			}
			return null;
		}

		private void detectFaulyArguments(IASTFunctionCallExpression callExpression, IASTInitializerClause[] arguments,
				int formatStringArgumentIndex) {
			final IASTInitializerClause formatArgument = arguments[formatStringArgumentIndex];
			final String formatArgumentValue = formatArgument.getRawSignature();
			final CFormatStringParser formatStringParser = new CFormatStringParser(formatArgumentValue);
			if (!formatStringParser.isVulnerable()) {
				return;
			}
			// match arguments;
			final Iterator<VulnerableFormatStringArgument> vulnerableArgumentsIterator = formatStringParser
					.getVulnerableArgumentsIterator();
			while (vulnerableArgumentsIterator.hasNext()) {
				final VulnerableFormatStringArgument currentArgument = vulnerableArgumentsIterator.next();
				final int argumentIndex = currentArgument.getArgumentIndex();
				final int argumentSize = currentArgument.getArgumentSize();
				if (argumentSize == CFormatStringParser.ARGUMENT_SIZE_NOT_SPECIFIED) {
					reportProblem(ER_ID, callExpression, callExpression.getRawSignature());
				}
				// else there some size is specified, so it should be less than
				// or equal
				// the size of the string variable.
				int suspectArgumentIndex = 1 + formatStringArgumentIndex + argumentIndex;
				IASTInitializerClause suspectArgument = arguments[suspectArgumentIndex];
				if (suspectArgument instanceof IASTIdExpression) {
					final IASTIdExpression idExpression = (IASTIdExpression) suspectArgument;
					IType expressionType = idExpression.getExpressionType();
					if (expressionType instanceof IArrayType) {
						IArrayType arrayExpressionType = (IArrayType) expressionType;
						long arraySize = arrayExpressionType.getSize().numericalValue().longValue();
						if (argumentSize > arraySize) {
							reportProblem(ER_ID, idExpression, idExpression.getRawSignature());
						}
					}
				}
			}
		}
	}
}
