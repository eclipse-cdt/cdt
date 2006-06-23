/*******************************************************************************
 * Copyright (c) 2002, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.text.c.hover;

import java.util.Iterator;

import org.eclipse.cdt.internal.ui.editor.CAnnotationIterator;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.text.HTMLPrinter;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.AnnotationPreference;
import org.eclipse.ui.texteditor.DefaultMarkerAnnotationAccess;
import org.eclipse.ui.texteditor.IDocumentProvider;

/**
 * AbstractAnnotationHover
 * Abstract super class for annotation hovers.
 */
public class AbstractAnnotationHover extends AbstractCEditorTextHover {

	private IPreferenceStore fStore= CUIPlugin.getDefault().getCombinedPreferenceStore();
	private DefaultMarkerAnnotationAccess fAnnotationAccess= new DefaultMarkerAnnotationAccess();
	private boolean fAllAnnotations;
	
	
	public AbstractAnnotationHover(boolean allAnnotations) {
		fAllAnnotations= allAnnotations;
	}
	
	/*
	 * Formats a message as HTML text.
	 */
	private String formatMessage(String message) {
		StringBuffer buffer= new StringBuffer();
		HTMLPrinter.addPageProlog(buffer);
		HTMLPrinter.addParagraph(buffer, HTMLPrinter.convertToHTMLContent(message));
		HTMLPrinter.addPageEpilog(buffer);
		return buffer.toString();
	}
	
	/*
	 * @see ITextHover#getHoverInfo(ITextViewer, IRegion)
	 */
	public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
		
		if (getEditor() == null)
			return null;
		
		IDocumentProvider provider= CUIPlugin.getDefault().getDocumentProvider();
		IAnnotationModel model= provider.getAnnotationModel(getEditor().getEditorInput());
		
		if (model != null) {
			Iterator e= new CAnnotationIterator(model, true, fAllAnnotations);
			int layer= -1;
			String message= null;
			while (e.hasNext()) {
				Annotation a= (Annotation) e.next();

				AnnotationPreference preference= getAnnotationPreference(a);
				if (preference == null || !(preference.getTextPreferenceKey() != null && fStore.getBoolean(preference.getTextPreferenceKey()) || (preference.getHighlightPreferenceKey() != null && fStore.getBoolean(preference.getHighlightPreferenceKey()))))
					continue;

				Position p= model.getPosition(a);
				
				int l= fAnnotationAccess.getLayer(a);
				
				if (l > layer && p != null && p.overlapsWith(hoverRegion.getOffset(), hoverRegion.getLength())) {
					String msg= a.getText();
					if (msg != null && msg.trim().length() > 0) {
						message= msg;
						layer= l;
					}
				}
			}
			if (layer > -1)
				return formatMessage(message);
		}
		
		return null;
	}
	
	/*
	 * @see IJavaEditorTextHover#setEditor(IEditorPart)
	 */
	public void setEditor(IEditorPart editor) {
		if (editor instanceof CEditor)
			super.setEditor(editor);
		else
			super.setEditor(null);
	}

	/**
	 * Returns the annotation preference for the given annotation.
	 *
	 * @param annotation the annotation
	 * @return the annotation preference or <code>null</code> if none
	 */	
	private AnnotationPreference getAnnotationPreference(Annotation annotation) {
		
		if (annotation.isMarkedDeleted())
			return null;
		return EditorsUI.getAnnotationPreferenceLookup().getAnnotationPreference(annotation);
	}

}
