/*******************************************************************************
 * Copyright (c) 2016, 2017 Kichwa Coders Ltd (https://kichwacoders.com/) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.linkerscript.ui.labeling

import com.google.inject.Inject
import org.eclipse.cdt.linkerscript.linkerScript.Memory
import org.eclipse.cdt.linkerscript.linkerScript.MemoryCommand
import org.eclipse.cdt.linkerscript.linkerScript.SectionsCommand
import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider
import org.eclipse.ui.ISharedImages
import org.eclipse.ui.PlatformUI
import org.eclipse.xtext.ui.label.DefaultEObjectLabelProvider
import org.eclipse.cdt.linkerscript.linkerScript.OutputSection

/**
 * Provides labels for EObjects.
 *
 * See https://www.eclipse.org/Xtext/documentation/304_ide_concepts.html#label-provider
 */
class LinkerScriptLabelProvider extends DefaultEObjectLabelProvider {

	@Inject
	new(AdapterFactoryLabelProvider delegate) {
		super(delegate);
	}

	def text(SectionsCommand secCmd) {
		'SECTIONS'
	}

	def text(MemoryCommand memCmd) {
		'MEMORY'
	}

	def image(Memory memory) {
		PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
	}

	def image(OutputSection outSec) {
		PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
	}

}
