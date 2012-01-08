/**********************************************************************
 * Copyright (c) 2004, 2008 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *     Intel Corporation - Initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 **********************************************************************/
package org.eclipse.cdt.internal.ui.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.help.HelpSystem;
import org.eclipse.help.IContext;
import org.eclipse.help.IHelpResource;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;

import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.ICHelpResourceDescriptor;
import org.eclipse.cdt.ui.text.ICHelpInvocationContext;

import org.eclipse.cdt.internal.ui.CHelpProviderManager;
import org.eclipse.cdt.internal.ui.text.CWordFinder;

/**
 * 
 * @since 2.1
 */
public class CHelpDisplayContext implements IContext {
	
	private IHelpResource[] fHelpResources;
	private String fText;
	
	public static void displayHelp(String contextId, ITextEditor editor) throws CoreException {
		String selected = getSelectedString(editor);
		IContext context= HelpSystem.getContext(contextId);
		if (context != null) {
			if (selected != null && selected.length() > 0) {
				context= new CHelpDisplayContext(context, editor, selected);
			}
			PlatformUI.getWorkbench().getHelpSystem().displayHelp(context);
		}
	}
	
	private static String getSelectedString(ITextEditor editor){
		String expression = null;
		try{
			ITextSelection selection = (ITextSelection)editor.getSite().getSelectionProvider().getSelection();
			IDocument document = editor.getDocumentProvider().getDocument(editor.getEditorInput());
			IRegion region = CWordFinder.findWord(document, selection.getOffset());
			expression = document.get(region.getOffset(), region.getLength());
		}
		catch(Exception e){
		}
		return expression;
	}

	public CHelpDisplayContext(IContext context, final ITextEditor editor , String selected) throws CoreException {

		List<IHelpResource> helpResources= new ArrayList<IHelpResource>();
		
		ICHelpInvocationContext invocationContext = new ICHelpInvocationContext() {

			@Override
			public IProject getProject() {
				ITranslationUnit unit = getTranslationUnit();
				if (unit != null) {
					return unit.getCProject().getProject();
				}
				return null;
			}

			@Override
			public ITranslationUnit getTranslationUnit() {
				IEditorInput editorInput= editor.getEditorInput();
				return CUIPlugin.getDefault().getWorkingCopyManager().getWorkingCopy(editorInput);
			}	
		};

		if (context != null) {
			IHelpResource[] resources= context.getRelatedTopics();
			if (resources != null){
				helpResources.addAll(Arrays.asList(resources));
			}
		}

		ICHelpResourceDescriptor providerResources[] = CHelpProviderManager.getDefault().getHelpResources(invocationContext,selected);
		if(providerResources != null){
			for(int i = 0; i < providerResources.length; i++){
				helpResources.addAll(Arrays.asList(providerResources[i].getHelpResources()));
			}
		}

		fHelpResources= helpResources.toArray(new IHelpResource[helpResources.size()]);
		if (fText == null || fText.length() == 0) {
			if (context != null) {
				fText= context.getText();
			}
		}
		if (fText != null && fText.length() == 0) {
			fText= null; 
		}
	}

	@Override
	public IHelpResource[] getRelatedTopics() {
		return fHelpResources;
	}

	@Override
	public String getText() {
		return fText;
	}
}

