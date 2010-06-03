/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.lrparser.c99.bindings;

import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;

@SuppressWarnings("restriction")
public class C99ProblemBinding extends ProblemBinding implements IProblemBinding {


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
