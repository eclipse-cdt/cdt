/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.text.c.hover;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

import org.eclipse.cdt.core.dom.rewrite.MacroExpansionExplorer;

/**
 * An input object to the {@link CMacroExpansionExplorationControl}.
 * 
 * @since 5.0
 */
class CMacroExpansionInput {
	MacroExpansionExplorer fExplorer;
	IDocument fDocument;
	IRegion fRegion;
}
