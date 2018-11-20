/*******************************************************************************
 * Copyright (c) 2008, 2015 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.rewrite.astwriter;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplatedTypeTemplateParameter;
import org.eclipse.cdt.core.parser.Keywords;
import org.eclipse.cdt.internal.core.dom.rewrite.commenthandler.NodeCommentMap;

/**
 * Generates source code of template parameter nodes. The actual string operations are delegated
 * to the <code>Scribe</code> class.
 *
 * @see Scribe
 * @see ICPPASTTemplateParameter
 * @author Emanuel Graf IFS
 */
public class TemplateParameterWriter extends NodeWriter {
	private static final String GREATER_THAN = ">"; //$NON-NLS-1$
	private static final String TEMPLATE_LESS_THAN = "template <"; //$NON-NLS-1$

	/**
	 * @param scribe
	 * @param visitor
	 */
	public TemplateParameterWriter(Scribe scribe, ASTWriterVisitor visitor, NodeCommentMap commentMap) {
		super(scribe, visitor, commentMap);
	}

	protected void writeTemplateParameter(ICPPASTTemplateParameter parameter) {
		if (parameter instanceof ICPPASTParameterDeclaration) {
			((IASTParameterDeclaration) ((ICPPASTParameterDeclaration) parameter)).accept(visitor);
		} else if (parameter instanceof ICPPASTSimpleTypeTemplateParameter) {
			ICPPASTSimpleTypeTemplateParameter simple = (ICPPASTSimpleTypeTemplateParameter) parameter;
			writeTemplateParameter(simple.getParameterType() == ICPPASTSimpleTypeTemplateParameter.st_class,
					simple.isParameterPack(), simple.getName(), simple.getDefaultType());
		} else if (parameter instanceof ICPPASTTemplatedTypeTemplateParameter) {
			writeTemplatedTypeTemplateParameter((ICPPASTTemplatedTypeTemplateParameter) parameter);
		}
	}

	private void writeTemplatedTypeTemplateParameter(ICPPASTTemplatedTypeTemplateParameter templated) {
		scribe.print(TEMPLATE_LESS_THAN);
		ICPPASTTemplateParameter[] params = templated.getTemplateParameters();
		writeNodeList(params);
		scribe.print(GREATER_THAN);
		scribe.printSpace();

		writeTemplateParameter(templated.getParameterType() == ICPPASTTemplatedTypeTemplateParameter.tt_class,
				templated.isParameterPack(), templated.getName(), templated.getDefaultValue());
	}

	private void writeTemplateParameter(boolean usesClass, boolean isVariadic, IASTName name,
			IASTNode defaultArgument) {
		scribe.print(usesClass ? Keywords.CLASS : Keywords.TYPENAME);
		if (isVariadic) {
			scribe.print(VAR_ARGS);
		}
		scribe.printSpace();
		visitNodeIfNotNull(name);

		if (defaultArgument != null) {
			scribe.print(EQUALS);
			defaultArgument.accept(visitor);
		}
	}

}
