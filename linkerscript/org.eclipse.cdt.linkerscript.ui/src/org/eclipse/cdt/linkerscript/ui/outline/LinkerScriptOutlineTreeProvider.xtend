/*******************************************************************************
 * Copyright (c) 2016, 2017 Kichwa Coders Ltd (https://kichwacoders.com/) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.linkerscript.ui.outline

import org.eclipse.cdt.linkerscript.linkerScript.LinkerScript
import org.eclipse.cdt.linkerscript.linkerScript.Memory
import org.eclipse.cdt.linkerscript.linkerScript.MemoryCommand
import org.eclipse.cdt.linkerscript.linkerScript.OutputSection
import org.eclipse.cdt.linkerscript.linkerScript.SectionsCommand
import org.eclipse.xtext.ui.editor.outline.impl.DefaultOutlineTreeProvider
import org.eclipse.xtext.ui.editor.outline.impl.DocumentRootNode

/**
 * Customization of the default outline structure.
 *
 * See https://www.eclipse.org/Xtext/documentation/304_ide_concepts.html#outline
 */
class LinkerScriptOutlineTreeProvider extends DefaultOutlineTreeProvider {
	def void _createChildren(DocumentRootNode parentNode, LinkerScript domainModel) {
		domainModel.statements.filter[element|element instanceof SectionsCommand || element instanceof MemoryCommand].
			forEach[element|createNode(parentNode, element)]
	}

	def boolean _isLeaf(Memory memory) {
		return true;
	}

	def boolean _isLeaf(OutputSection outputSection) {
		return true;
	}
}
