/*******************************************************************************
 * Copyright (c) 2009, 2010 Red Hat Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.ui.editors.automake;

import org.eclipse.cdt.autotools.ui.AutotoolsUIPlugin;
import org.eclipse.core.runtime.AssertionFailedException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.quickassist.IQuickFixableAnnotation;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.ui.IEditorInput;


public class AutomakeErrorHandler {
	public static final String AUTOMAKE_ERROR_MARKER_ID = AutotoolsUIPlugin.PLUGIN_ID
	+ ".parsefileerror";
	
	private IDocument document;
	private AnnotationModel fAnnotationModel;

	public static final String CDT_ANNOTATION_INFO = "org.eclipse.cdt.ui.info";
	public static final String CDT_ANNOTATION_WARNING = "org.eclipse.cdt.ui.warning";
	public static final String CDT_ANNOTATION_ERROR = "org.eclipse.cdt.ui.error";
	
	// TODO: no quickfixes yet implemented, but maybe in the future
	private static class AutomakeAnnotation extends Annotation implements IQuickFixableAnnotation {
		public AutomakeAnnotation(String annotationType, boolean persist, String message) {
			super(annotationType, persist, message);
		}

		public void setQuickFixable(boolean state) {
			// do nothing
		}

		public boolean isQuickFixableStateSet() {
			return true;
		}

		public boolean isQuickFixable() throws AssertionFailedException {
			return false;
		}

	}
	
	public AutomakeErrorHandler(IEditorInput input) {
		this.document = AutomakeEditorFactory.getDefault().getAutomakefileDocumentProvider().getDocument(input);
		this.fAnnotationModel = (AnnotationModel)AutomakeEditorFactory.getDefault().getAutomakefileDocumentProvider().getAnnotationModel(input);
	}

	
	public void update(IMakefile makefile) {
		removeExistingMarkers();

		// Recursively process all the directives in the Makefile
		checkChildren(makefile);
	}
	
	private void checkChildren(IParent parent) {
		IDirective[] directives = parent.getDirectives();
		for (int i = 0; i < directives.length; i++) {
			IDirective directive = directives[i];
			if (directive instanceof IParent) {
				checkChildren((IParent)directive);
			} else if (directive instanceof BadDirective) {
				int lineNumber = directive.getStartLine();
				Integer charStart = getCharOffset(lineNumber - 1, 0);
				Integer charEnd = Integer.valueOf(getCharOffset(directive.getEndLine() - 1, -1).intValue());
				
				String annotationType = CDT_ANNOTATION_ERROR;
				Annotation annotation = new AutomakeAnnotation(annotationType, true, "Bad directive"); //$NON-NLS-1$
				Position p = new Position(charStart.intValue(),charEnd.intValue() - charStart.intValue());
				fAnnotationModel.addAnnotation(annotation, p);
			}
		}
		return;
	}
	
	public void removeExistingMarkers()
	{
		fAnnotationModel.removeAllAnnotations();
	}
	
	
	private Integer getCharOffset(int lineNumber, int columnNumber)
	{
		try
		{
			if (columnNumber >= 0)
				return Integer.valueOf(document.getLineOffset(lineNumber) + columnNumber);
			return Integer.valueOf(document.getLineOffset(lineNumber) + document.getLineLength(lineNumber));
		}
		catch (BadLocationException e)
		{
			e.printStackTrace();
			return null;
		}
	}
}
