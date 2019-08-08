/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.lrparser.c99.bindings;

import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;

@SuppressWarnings("restriction")
public class C99ProblemBinding extends ProblemBinding {

	public C99ProblemBinding(int messageId) {
		super(null, messageId);
	}

	public C99ProblemBinding(int messageId, String arg) {
		super(null, messageId, arg.toCharArray());
	}

	public static C99ProblemBinding badType() {
		return new C99ProblemBinding(IProblemBinding.SEMANTIC_INVALID_TYPE);
	}

}
