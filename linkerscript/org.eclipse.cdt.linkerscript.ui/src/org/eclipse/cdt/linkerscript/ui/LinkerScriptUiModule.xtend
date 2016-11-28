/*******************************************************************************
 * Copyright (c) 2016, 2017 Kichwa Coders Ltd (https://kichwacoders.com/) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.linkerscript.ui

import org.eclipse.xtend.lib.annotations.FinalFieldsConstructor
import org.eclipse.xtext.ui.editor.model.IResourceForEditorInputFactory
import org.eclipse.xtext.ui.editor.model.ResourceForIEditorInputFactory
import org.eclipse.xtext.ui.editor.model.edit.ITextEditComposer
import org.eclipse.xtext.ui.resource.IResourceSetProvider
import org.eclipse.xtext.ui.resource.SimpleResourceSetProvider
import org.eclipse.xtext.ui.shared.Access
import org.eclipse.xtext.ui.editor.model.edit.DefaultTextEditComposer
import org.eclipse.xtext.resource.SaveOptions

/**
 * Use this class to register components to be used within the Eclipse IDE.
 */
@FinalFieldsConstructor
class LinkerScriptUiModule extends AbstractLinkerScriptUiModule {
	/**
	 * Overridden to achieve JDT independence according to https://eclipse.org/Xtext/documentation/307_special_languages.html
	 */
	override Class<? extends IResourceForEditorInputFactory> bindIResourceForEditorInputFactory() {
		return ResourceForIEditorInputFactory
	}

	/**
	 * Overridden to achieve JDT independence according to https://eclipse.org/Xtext/documentation/307_special_languages.html
	 */
	override Class<? extends IResourceSetProvider> bindIResourceSetProvider() {
		return SimpleResourceSetProvider;
	}

	/**
	 * Overridden to achieve JDT independence according to https://eclipse.org/Xtext/documentation/307_special_languages.html
	 */
	override provideIAllContainersState() {
		return Access.getWorkspaceProjectsState();
	}

	/**
	 * On all model edits, format the whole file. This fixes
	 * a specific problem. See SerializerTest.serializerMergesMemoryLines()
	 */
	public static class AlwaysFormatTextEditComposer extends DefaultTextEditComposer {
		override protected getSaveOptions() {
			return SaveOptions.newBuilder().format().getOptions();
		}

	}

	def Class<? extends ITextEditComposer> bindITextEditComposer() {
		return AlwaysFormatTextEditComposer;
	}

}
