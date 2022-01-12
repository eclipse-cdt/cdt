/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
 *     Markus Schorn (Wind River Systems)
 *     Ed Swartz (Nokia)
 *     Mike Kucera (IBM)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.search.actions;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.ui.actions.OpenActionUtil;
import org.eclipse.cdt.internal.ui.editor.CEditorMessages;
import org.eclipse.cdt.internal.ui.text.CWordFinder;
import org.eclipse.cdt.internal.ui.viewsupport.CElementLabels;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.ICModelBasedEditor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;

/**
 * Navigates to the definition of a name, or to the declaration if invoked on the definition.
 */
public class OpenDeclarationsAction extends SelectionParseAction {
	public static boolean sDisallowAmbiguousInput = false;

	ITextSelection fTextSelection;

	/**
	 * Creates a new action with the given editor
	 */
	public OpenDeclarationsAction(ICModelBasedEditor editor) {
		super(editor);
		setText(CEditorMessages.OpenDeclarations_label);
		setToolTipText(CEditorMessages.OpenDeclarations_tooltip);
		setDescription(CEditorMessages.OpenDeclarations_description);
	}

	@Override
	public void run() {
		OpenDeclarationsJob job = createJob(sDefaultDisambiguator);
		if (job != null)
			job.schedule();
	}

	/**
	 * For the purpose of regression testing.
	 */
	public void runSync() throws CoreException {
		OpenDeclarationsJob job = createJob(sDefaultDisambiguator);
		if (job != null)
			job.performNavigation(new NullProgressMonitor());
	}

	public void runSync(ITargetDisambiguator targetDisambiguator) throws CoreException {
		OpenDeclarationsJob job = createJob(targetDisambiguator);
		if (job != null)
			job.performNavigation(new NullProgressMonitor());
	}

	private OpenDeclarationsJob createJob(ITargetDisambiguator targetDisambiguator) {
		String text = computeSelectedWord();
		OpenDeclarationsJob job = null;
		ICElement elem = fEditor.getTranslationUnit();
		if (elem instanceof ITranslationUnit && fTextSelection != null) {
			job = new OpenDeclarationsJob(this, (ITranslationUnit) elem, fTextSelection, text, targetDisambiguator);
		}
		return job;
	}

	private String computeSelectedWord() {
		fTextSelection = getSelectedStringFromEditor();
		String text = null;
		if (fTextSelection != null) {
			if (fTextSelection.getLength() > 0) {
				text = fTextSelection.getText();
			} else {
				IDocument document = fEditor.getDocumentProvider().getDocument(fEditor.getEditorInput());
				IRegion reg = CWordFinder.findWord(document, fTextSelection.getOffset());
				if (reg != null && reg.getLength() > 0) {
					try {
						text = document.get(reg.getOffset(), reg.getLength());
					} catch (BadLocationException e) {
						CUIPlugin.log(e);
					}
				}
			}
		}
		return text;
	}

	/**
	 * Used to diambiguate between multiple candidate targets for this action.
	 */
	public static interface ITargetDisambiguator {
		ICElement disambiguateTargets(ICElement[] targets, SelectionParseAction action);
	}

	/**
	 * Disambiguates by showing the user a dialog to choose.
	 */
	private static class DialogTargetDisambiguator implements ITargetDisambiguator {
		@Override
		public ICElement disambiguateTargets(ICElement[] targets, SelectionParseAction action) {
			return OpenActionUtil.selectCElement(targets, action.getSite().getShell(),
					CEditorMessages.OpenDeclarationsAction_dialog_title,
					CEditorMessages.OpenDeclarationsAction_selectMessage, CElementLabels.ALL_DEFAULT
							| CElementLabels.ALL_FULLY_QUALIFIED | CElementLabels.MF_POST_FILE_QUALIFIED,
					0);
		}
	}

	private static final ITargetDisambiguator sDefaultDisambiguator = new DialogTargetDisambiguator();
}
