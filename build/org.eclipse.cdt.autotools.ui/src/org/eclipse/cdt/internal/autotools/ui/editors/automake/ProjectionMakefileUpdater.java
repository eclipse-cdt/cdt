/*******************************************************************************
 * Copyright (c) 2002, 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * Red Hat Inc. - convert to use with Automake editor
 *******************************************************************************/

package org.eclipse.cdt.internal.autotools.ui.editors.automake;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.autotools.core.AutotoolsPlugin;
import org.eclipse.cdt.make.core.makefile.IDirective;
import org.eclipse.cdt.make.core.makefile.IMacroDefinition;
import org.eclipse.cdt.make.core.makefile.IParent;
import org.eclipse.cdt.make.core.makefile.IRule;
import org.eclipse.cdt.make.core.makefile.gnu.IConditional;
import org.eclipse.cdt.make.ui.IWorkingCopyManager;
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
public class ProjectionMakefileUpdater implements IProjectionListener {

	private static class MakefileProjectionAnnotation extends ProjectionAnnotation {

		private IDirective fDirective;
		private boolean fIsComment;

		public MakefileProjectionAnnotation(IDirective element, boolean isCollapsed, boolean isComment) {
			super(isCollapsed);
			fDirective = element;
			fIsComment = isComment;
		}

		public IDirective getElement() {
			return fDirective;
		}

		public void setElement(IDirective element) {
			fDirective = element;
		}

		public boolean isComment() {
			return fIsComment;
		}
	}

	public void install(MakefileEditor editor, ProjectionViewer viewer) {
		fEditor = editor;
		fViewer = viewer;
		fViewer.addProjectionListener(this);
	}

	public void uninstall() {
		if (isInstalled()) {
			projectionDisabled();
			fViewer.removeProjectionListener(this);
			fViewer = null;
			fEditor = null;
		}
	}

	protected boolean isInstalled() {
		return fEditor != null;
	}

	private class ReconcilerParticipant implements IReconcilingParticipant {

		@Override
		public void reconciled() {
			processReconcile();
		}
	}

	private IDocument fCachedDocument;
	private MakefileEditor fEditor;
	private IDirective fInput;
	private ProjectionViewer fViewer;
	private IReconcilingParticipant fParticipant;

	private boolean fAllowCollapsing = false;
	@SuppressWarnings("unused")
	private boolean fCollapseMacroDef = false;
	@SuppressWarnings("unused")
	private boolean fCollapseRule = false;
	@SuppressWarnings("unused")
	private boolean fCollapseConditional = false;

	@Override
	public void projectionEnabled() {
		// http://home.ott.oti.com/teams/wswb/anon/out/vms/index.html
		// projectionEnabled messages are not always paired with projectionDisabled
		// i.e. multiple enabled messages may be sent out.
		// we have to make sure that we disable first when getting an enable
		// message.
		projectionDisabled();

		initialize();
		fParticipant = new ReconcilerParticipant();
		fEditor.addReconcilingParticipant(fParticipant);
	}

	@Override
	public void projectionDisabled() {
		fCachedDocument = null;
		if (fParticipant != null) {
			fEditor.addReconcilingParticipant(fParticipant);
			fParticipant = null;
		}
	}

	public void initialize() {

		if (!isInstalled())
			return;

		initializePreferences();

		try {

			IDocumentProvider provider = fEditor.getDocumentProvider();
			fCachedDocument = provider.getDocument(fEditor.getEditorInput());
			fAllowCollapsing = true;

			IWorkingCopyManager manager = AutomakeEditorFactory.getDefault().getWorkingCopyManager();
			fInput = manager.getWorkingCopy(fEditor.getEditorInput());

			if (fInput != null) {
				ProjectionAnnotationModel model = fEditor.getAdapter(ProjectionAnnotationModel.class);
				if (model != null) {
					Map<MakefileProjectionAnnotation, Position> additions = computeAdditions((IParent) fInput);
					model.removeAllAnnotations();
					model.replaceAnnotations(null, additions);
				}
			}

		} finally {
			fCachedDocument = null;
			fAllowCollapsing = false;
		}
	}

	private void initializePreferences() {
		//FIXME: what to do with Makefile editor preferences
		IPreferenceStore store = AutotoolsPlugin.getDefault().getPreferenceStore();
		fCollapseMacroDef = store.getBoolean(MakefileEditorPreferenceConstants.EDITOR_FOLDING_MACRODEF);
		fCollapseRule = store.getBoolean(MakefileEditorPreferenceConstants.EDITOR_FOLDING_RULE);
		fCollapseConditional = store.getBoolean(MakefileEditorPreferenceConstants.EDITOR_FOLDING_CONDITIONAL);
	}

	private Map<MakefileProjectionAnnotation, Position> computeAdditions(IParent parent) {
		Map<MakefileProjectionAnnotation, Position> map = new HashMap<>();
		computeAdditions(parent.getDirectives(), map);
		return map;
	}

	private void computeAdditions(IDirective[] elements, Map<MakefileProjectionAnnotation, Position> map) {
		for (int i = 0; i < elements.length; i++) {
			IDirective element = elements[i];

			computeAdditions(element, map);

			if (element instanceof IParent) {
				IParent parent = (IParent) element;
				computeAdditions(parent.getDirectives(), map);
			}
		}
	}

	private void computeAdditions(IDirective element, Map<MakefileProjectionAnnotation, Position> map) {

		boolean createProjection = false;

		if (element instanceof IConditional) {
			createProjection = true;
		} else if (element instanceof IMacroDefinition) {
			createProjection = true;
		} else if (element instanceof IRule) {
			createProjection = true;
		}

		if (createProjection) {
			Position position = createProjectionPosition(element);
			if (position != null) {
				map.put(new MakefileProjectionAnnotation(element, fAllowCollapsing, true), position);
			}
		}
	}

	private Position createProjectionPosition(IDirective element) {

		if (fCachedDocument == null)
			return null;

		try {
			int startLine = element.getStartLine() - 1;
			int endLine = element.getEndLine() - 1;
			if (startLine != endLine) {
				int offset = fCachedDocument.getLineOffset(startLine);
				int endOffset = fCachedDocument.getLineOffset(endLine + 1);
				return new Position(offset, endOffset - offset);
			}

		} catch (BadLocationException x) {
		}

		return null;
	}

	public void processReconcile() {
		if (!isInstalled())
			return;

		ProjectionAnnotationModel model = fEditor.getAdapter(ProjectionAnnotationModel.class);
		if (model == null)
			return;

		try {
			IDocumentProvider provider = fEditor.getDocumentProvider();
			fCachedDocument = provider.getDocument(fEditor.getEditorInput());
			fAllowCollapsing = false;

			Map<MakefileProjectionAnnotation, Position> additions = new HashMap<>();
			List<MakefileProjectionAnnotation> deletions = new ArrayList<>();
			List<MakefileProjectionAnnotation> updates = new ArrayList<>();

			Map<MakefileProjectionAnnotation, Position> updated = computeAdditions((IParent) fInput);
			Map<IDirective, List<MakefileProjectionAnnotation>> previous = createAnnotationMap(model);

			Iterator<MakefileProjectionAnnotation> e = updated.keySet().iterator();
			while (e.hasNext()) {
				MakefileProjectionAnnotation annotation = e.next();
				IDirective element = annotation.getElement();
				Position position = updated.get(annotation);

				List<MakefileProjectionAnnotation> annotations = previous.get(element);
				if (annotations == null) {
					additions.put(annotation, position);
				} else {
					Iterator<MakefileProjectionAnnotation> x = annotations.iterator();
					while (x.hasNext()) {
						MakefileProjectionAnnotation a = x.next();
						if (annotation.isComment() == a.isComment()) {
							Position p = model.getPosition(a);
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

			Iterator<List<MakefileProjectionAnnotation>> e2 = previous.values().iterator();
			while (e.hasNext()) {
				List<MakefileProjectionAnnotation> list = e2.next();
				int size = list.size();
				for (int i = 0; i < size; i++)
					deletions.add(list.get(i));
			}

			match(model, deletions, additions, updates);

			Annotation[] removals = new Annotation[deletions.size()];
			deletions.toArray(removals);
			Annotation[] changes = new Annotation[updates.size()];
			updates.toArray(changes);
			model.modifyAnnotations(removals, additions, changes);

		} finally {
			fCachedDocument = null;
			fAllowCollapsing = true;
		}
	}

	private void match(ProjectionAnnotationModel model, List<MakefileProjectionAnnotation> deletions,
			Map<MakefileProjectionAnnotation, Position> additions, List<MakefileProjectionAnnotation> changes) {
		if (deletions.isEmpty() || (additions.isEmpty() && changes.isEmpty()))
			return;

		List<MakefileProjectionAnnotation> newDeletions = new ArrayList<>();
		List<MakefileProjectionAnnotation> newChanges = new ArrayList<>();

		Iterator<MakefileProjectionAnnotation> deletionIterator = deletions.iterator();
		outer: while (deletionIterator.hasNext()) {
			MakefileProjectionAnnotation deleted = deletionIterator.next();
			Position deletedPosition = model.getPosition(deleted);
			if (deletedPosition == null)
				continue;

			Iterator<MakefileProjectionAnnotation> changesIterator = changes.iterator();
			while (changesIterator.hasNext()) {
				MakefileProjectionAnnotation changed = changesIterator.next();
				if (deleted.isComment() == changed.isComment()) {
					Position changedPosition = model.getPosition(changed);
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

			Iterator<MakefileProjectionAnnotation> additionsIterator = additions.keySet().iterator();
			while (additionsIterator.hasNext()) {
				MakefileProjectionAnnotation added = additionsIterator.next();
				if (deleted.isComment() == added.isComment()) {
					Position addedPosition = additions.get(added);

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

	private Map<IDirective, List<MakefileProjectionAnnotation>> createAnnotationMap(IAnnotationModel model) {
		Map<IDirective, List<MakefileProjectionAnnotation>> map = new HashMap<>();
		Iterator<Annotation> e = model.getAnnotationIterator();
		while (e.hasNext()) {
			Annotation annotation = e.next();
			if (annotation instanceof MakefileProjectionAnnotation) {
				MakefileProjectionAnnotation directive = (MakefileProjectionAnnotation) annotation;
				List<MakefileProjectionAnnotation> list = map.get(directive.getElement());
				if (list == null) {
					list = new ArrayList<>(2);
					map.put(directive.getElement(), list);
				}
				list.add(directive);
			}
		}
		return map;
	}

}
