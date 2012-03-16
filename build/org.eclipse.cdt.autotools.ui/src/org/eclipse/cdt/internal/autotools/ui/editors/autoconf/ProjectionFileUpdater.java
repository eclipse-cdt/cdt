/*******************************************************************************
 * Copyright (c) 2002, 2006, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * Red Hat Inc. - convert to use with Autoconf Editor
 * Ed Swartz (NOKIA) - refactoring
 *******************************************************************************/

package org.eclipse.cdt.internal.autotools.ui.editors.autoconf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.autotools.core.AutotoolsPlugin;
import org.eclipse.cdt.autotools.ui.editors.AutoconfEditor;
import org.eclipse.cdt.autotools.ui.editors.parser.AutoconfCaseElement;
import org.eclipse.cdt.autotools.ui.editors.parser.AutoconfElement;
import org.eclipse.cdt.autotools.ui.editors.parser.AutoconfElifElement;
import org.eclipse.cdt.autotools.ui.editors.parser.AutoconfElseElement;
import org.eclipse.cdt.autotools.ui.editors.parser.AutoconfForElement;
import org.eclipse.cdt.autotools.ui.editors.parser.AutoconfIfElement;
import org.eclipse.cdt.autotools.ui.editors.parser.AutoconfMacroArgumentElement;
import org.eclipse.cdt.autotools.ui.editors.parser.AutoconfMacroElement;
import org.eclipse.cdt.autotools.ui.editors.parser.AutoconfRootElement;
import org.eclipse.cdt.autotools.ui.editors.parser.AutoconfSelectElement;
import org.eclipse.cdt.autotools.ui.editors.parser.AutoconfUntilElement;
import org.eclipse.cdt.autotools.ui.editors.parser.AutoconfWhileElement;
import org.eclipse.cdt.internal.autotools.ui.editors.automake.IReconcilingParticipant;
import org.eclipse.cdt.internal.autotools.ui.preferences.AutotoolsEditorPreferenceConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.projection.IProjectionListener;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.ui.texteditor.IDocumentProvider;


/**
 * ProjectionMakefileUpdater
 */
public class ProjectionFileUpdater implements IProjectionListener {

	private static class AutoconfProjectionAnnotation extends ProjectionAnnotation {
		
		private AutoconfElement fElement;
		private boolean fIsComment;
		
		public AutoconfProjectionAnnotation(AutoconfElement element, boolean isCollapsed, boolean isComment) {
			super(isCollapsed);
			fElement = element;
			fIsComment = isComment;
		}
		
		public AutoconfElement getElement() {
			return fElement;
		}
		
		public void setElement(AutoconfElement element) {
			fElement = element;
		}
		
		public boolean isComment() {
			return fIsComment;
		}
		
	}
	

	public void install(AutoconfEditor editor, ProjectionViewer viewer) {
		fEditor= editor;
		fViewer= viewer;
		fViewer.addProjectionListener(this);
	}
	
	public void uninstall() {
		if (isInstalled()) {
			projectionDisabled();
			fViewer.removeProjectionListener(this);
			fViewer= null;
			fEditor= null;
		}
	}
	
	protected boolean isInstalled() {
		return fEditor != null;
	}

	private class ReconcilerParticipant implements IReconcilingParticipant {

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.internal.autotools.ui.editors.automake.IReconcilingParticipant#reconciled()
		 */
		public void reconciled() {
			processReconcile();
		}
	}

	private IDocument fCachedDocument;
	private AutoconfEditor fEditor;
	private ProjectionViewer fViewer;
	private IReconcilingParticipant fParticipant;

	private boolean fAllowCollapsing = false;
	private boolean fCollapseMacroDef = false;
	private boolean fCollapseCase = false;
	private boolean fCollapseConditional = false;
	private boolean fCollapseLoop = false;

	/*
	 * @see org.eclipse.jface.text.source.projection.IProjectionListener#projectionEnabled()
	 */
	public void projectionEnabled() {
		// http://home.ott.oti.com/teams/wswb/anon/out/vms/index.html
		// projectionEnabled messages are not always paired with projectionDisabled
		// i.e. multiple enabled messages may be sent out.
		// we have to make sure that we disable first when getting an enable
		// message.
		projectionDisabled();
		
		initialize();			
		fParticipant= new ReconcilerParticipant();
		fEditor.addReconcilingParticipant(fParticipant);
	}
	
	/*
	 * @see org.eclipse.jface.text.source.projection.IProjectionListener#projectionDisabled()
	 */
	public void projectionDisabled() {
		fCachedDocument= null;
		if (fParticipant != null) {
			fEditor.addReconcilingParticipant(fParticipant);
			fParticipant= null;
		}
	}
		
	public void initialize() {
		
		if (!isInstalled())
			return;
		
		initializePreferences();
		
		try {
			
			IDocumentProvider provider= fEditor.getDocumentProvider();
			fCachedDocument= provider.getDocument(fEditor.getEditorInput());
			fAllowCollapsing= true;
			
//			IWorkingCopyManager manager= AutomakeEditorFactory.getDefault().getWorkingCopyManager();
			AutoconfElement fInput= fEditor.getRootElement();
			
			if (fInput != null) {
				ProjectionAnnotationModel model= (ProjectionAnnotationModel) fEditor.getAdapter(ProjectionAnnotationModel.class);
				if (model != null) {
					Map<AutoconfProjectionAnnotation, Position> additions= computeAdditions(fInput);
					model.removeAllAnnotations();
					model.replaceAnnotations(null, additions);
				}
			}
			
		} finally {
			fCachedDocument= null;
			fAllowCollapsing= false;
		}
	}

	private void initializePreferences() {
		//FIXME: what to do with Makefile editor preferences
		IPreferenceStore store = AutotoolsPlugin.getDefault().getPreferenceStore();
		fCollapseMacroDef = store.getBoolean(AutotoolsEditorPreferenceConstants.EDITOR_FOLDING_MACRODEF);
		fCollapseCase = store.getBoolean(AutotoolsEditorPreferenceConstants.EDITOR_FOLDING_CASE);
		fCollapseConditional = store.getBoolean(AutotoolsEditorPreferenceConstants.EDITOR_FOLDING_CONDITIONAL);
		fCollapseLoop = store.getBoolean(AutotoolsEditorPreferenceConstants.EDITOR_FOLDING_LOOP);
	}

	private Map<AutoconfProjectionAnnotation, Position> computeAdditions(AutoconfElement root) {
		Map<AutoconfProjectionAnnotation, Position> map= new HashMap<AutoconfProjectionAnnotation, Position>();
		if (root instanceof AutoconfRootElement)
			computeAdditions(root.getChildren(), map);
		return map;
	}

	private void computeAdditions(Object[] elements, Map<AutoconfProjectionAnnotation, Position> map) {
		for (int i= 0; i < elements.length; i++) {
			AutoconfElement element= (AutoconfElement)elements[i];
			
			computeAdditions(element, map);
			
			if (element.hasChildren()) {				
				computeAdditions(element.getChildren(), map);
			}
		}
	}

	private void computeAdditions(AutoconfElement element, Map<AutoconfProjectionAnnotation, Position> map) {
		
		boolean createProjection= false;
		
		@SuppressWarnings("unused")
		boolean collapse= false;
			
		if (element instanceof AutoconfIfElement ||
				element instanceof AutoconfElseElement ||
				element instanceof AutoconfElifElement) {
			collapse= fAllowCollapsing && fCollapseConditional;
			createProjection= true;
		} else if (element instanceof AutoconfMacroElement) {
			collapse= fAllowCollapsing && fCollapseMacroDef;
			createProjection= true;
		} else if (element instanceof AutoconfMacroArgumentElement) {
			collapse= fAllowCollapsing && fCollapseMacroDef;
			createProjection= true;
		} else if (element instanceof AutoconfCaseElement) {
			collapse= fAllowCollapsing && fCollapseCase;
			createProjection= true;
		} else if (element instanceof AutoconfForElement ||
				element instanceof AutoconfWhileElement ||
				element instanceof AutoconfUntilElement ||
				element instanceof AutoconfSelectElement) {
			collapse = fAllowCollapsing && fCollapseLoop;
			createProjection = true;
		}
	
		if (createProjection) {
			Position position= createProjectionPosition(element);
			if (position != null) {
				map.put(new AutoconfProjectionAnnotation(element, fAllowCollapsing, true), position);
			}
		}
	}

	private Position createProjectionPosition(AutoconfElement element) {
		if (fCachedDocument == null)
			return null;
		int offset = 0;
		try {
			int startLine = 0;
			int endLine = 0;
			startLine = fCachedDocument.getLineOfOffset(element.getStartOffset());
			endLine = fCachedDocument.getLineOfOffset(element.getEndOffset());

			if (startLine != endLine) {
				offset= fCachedDocument.getLineOffset(startLine);
				int endOffset = fCachedDocument.getLineOffset(endLine) + fCachedDocument.getLineLength(endLine);
				return new Position(offset, endOffset - offset);
			}
		} catch (BadLocationException x) {
			// We should only get here if we try and read the line past EOF
			return new Position(offset, fCachedDocument.getLength() - 1);
		}
		return null;
	}

	public void processReconcile() {
		if (!isInstalled())
			return;
		
		ProjectionAnnotationModel model= (ProjectionAnnotationModel) fEditor.getAdapter(ProjectionAnnotationModel.class);
		if (model == null)
			return;
		
		try {
			IDocumentProvider provider= fEditor.getDocumentProvider();
			fCachedDocument= provider.getDocument(fEditor.getEditorInput());
			fAllowCollapsing= false;
			
			Map<AutoconfProjectionAnnotation, Position> additions= new HashMap<AutoconfProjectionAnnotation, Position>();
			List<AutoconfProjectionAnnotation> deletions= new ArrayList<AutoconfProjectionAnnotation>();
			List<AutoconfProjectionAnnotation> updates = new ArrayList<AutoconfProjectionAnnotation>();
			
			Map<AutoconfProjectionAnnotation, Position> updated= computeAdditions(fEditor.getRootElement());
			
			Map<AutoconfElement, List<AutoconfProjectionAnnotation>> previous= createAnnotationMap(model);
			
			
			Iterator<AutoconfProjectionAnnotation> e= updated.keySet().iterator();
			while (e.hasNext()) {
				AutoconfProjectionAnnotation annotation= (AutoconfProjectionAnnotation) e.next();
				AutoconfElement element= annotation.getElement();
				Position position= (Position) updated.get(annotation);
				
				List<AutoconfProjectionAnnotation> annotations= previous.get(element);
				if (annotations == null) {
					additions.put(annotation, position);
				} else {
					Iterator<AutoconfProjectionAnnotation> x= annotations.iterator();
					while (x.hasNext()) {
						AutoconfProjectionAnnotation a= (AutoconfProjectionAnnotation) x.next();
						if (annotation.isComment() == a.isComment()) {
							Position p= model.getPosition(a);
							if (p != null && !position.equals(p)) {
								p.setOffset(position.getOffset());
								p.setLength(position.getLength());
								updates.add(a);
							}
							x.remove();
							break;
						}
					}
										
					if (annotations.isEmpty())
						previous.remove(element);
				}
			}
			
			Iterator<List<AutoconfProjectionAnnotation>> e2 = previous.values().iterator();
			while (e2.hasNext()) {
				List<AutoconfProjectionAnnotation> list= e2.next();
				int size= list.size();
				for (int i= 0; i < size; i++)
					deletions.add(list.get(i));
			}

			match(model, deletions, additions, updates);

			Annotation[] removals= new Annotation[deletions.size()];
			deletions.toArray(removals);
			Annotation[] changes= new Annotation[updates.size()];
			updates.toArray(changes);
			model.modifyAnnotations(removals, additions, changes);
			
		} finally {
			fCachedDocument= null;
			fAllowCollapsing= true;
		}
	}

	private void match(ProjectionAnnotationModel model, List<AutoconfProjectionAnnotation> deletions, Map<AutoconfProjectionAnnotation, Position> additions, List<AutoconfProjectionAnnotation> changes) {
		if (deletions.isEmpty() || (additions.isEmpty() && changes.isEmpty()))
			return;
		
		List<AutoconfProjectionAnnotation> newDeletions= new ArrayList<AutoconfProjectionAnnotation>();
		List<AutoconfProjectionAnnotation> newChanges= new ArrayList<AutoconfProjectionAnnotation>();
		
		Iterator<AutoconfProjectionAnnotation> deletionIterator= deletions.iterator();
		outer: while (deletionIterator.hasNext()) {
			AutoconfProjectionAnnotation deleted= (AutoconfProjectionAnnotation) deletionIterator.next();
			Position deletedPosition= model.getPosition(deleted);
			if (deletedPosition == null)
				continue;
			
			Iterator<AutoconfProjectionAnnotation> changesIterator= changes.iterator();
			while (changesIterator.hasNext()) {
				AutoconfProjectionAnnotation changed= (AutoconfProjectionAnnotation) changesIterator.next();
				if (deleted.isComment() == changed.isComment()) {
					Position changedPosition= model.getPosition(changed);
					if (changedPosition == null)
						continue;
					
					if (deletedPosition.getOffset() == changedPosition.getOffset()) {
						
						deletedPosition.setLength(changedPosition.getLength());
						deleted.setElement(changed.getElement());
						
						deletionIterator.remove();
						newChanges.add(deleted);
						
						changesIterator.remove();
						newDeletions.add(changed);
						
						continue outer;
					}
				}
			}
			
			Iterator<AutoconfProjectionAnnotation> additionsIterator= additions.keySet().iterator();
			while (additionsIterator.hasNext()) {
				AutoconfProjectionAnnotation added= (AutoconfProjectionAnnotation) additionsIterator.next();
				if (deleted.isComment() == added.isComment()) {
					Position addedPosition= (Position) additions.get(added);
					
					if (deletedPosition.getOffset() == addedPosition.getOffset()) {
						
						deletedPosition.setLength(addedPosition.getLength());
						deleted.setElement(added.getElement());
						
						deletionIterator.remove();
						newChanges.add(deleted);
						
						additionsIterator.remove();
						
						break;
					}
				}
			}
		}
		
		deletions.addAll(newDeletions);
		changes.addAll(newChanges);
	}

	private Map<AutoconfElement, List<AutoconfProjectionAnnotation>> createAnnotationMap(IAnnotationModel model) {
		Map<AutoconfElement, List<AutoconfProjectionAnnotation>> map= new HashMap<AutoconfElement, List<AutoconfProjectionAnnotation>>();
		@SuppressWarnings("unchecked")
		Iterator e= model.getAnnotationIterator();
		while (e.hasNext()) {
			Object annotation= e.next();
			if (annotation instanceof AutoconfProjectionAnnotation) {
				AutoconfProjectionAnnotation directive= (AutoconfProjectionAnnotation) annotation;
				List<AutoconfProjectionAnnotation> list= map.get(directive.getElement());
				if (list == null) {
					list= new ArrayList<AutoconfProjectionAnnotation>(2);
					map.put(directive.getElement(), list);
				}
				list.add(directive);
			}
		}
		return map;
	}

}
