/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.lrparser.c99;

import org.eclipse.cdt.core.dom.lrparser.action.c99.C99BuildASTParserAction;
import org.eclipse.cdt.internal.core.dom.lrparser.c99.action.deprecated.C99TypedefTrackerParserAction;

class C99ParserAction {

	public C99BuildASTParserAction builder;
	public C99TypedefTrackerParserAction resolver;
}
