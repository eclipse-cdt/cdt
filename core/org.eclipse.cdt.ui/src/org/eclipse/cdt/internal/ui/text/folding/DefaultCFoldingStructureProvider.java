/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.text.folding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ElementChangedEvent;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.IElementChangedListener;
import org.eclipse.cdt.core.model.IParent;
import org.eclipse.cdt.core.model.ISourceRange;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.IWorkingCopyManager;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.cdt.ui.text.folding.ICFoldingStructureProvider;
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
import org.eclipse.ui.texteditor.ITextEditor;

/**
 */
public class DefaultCFoldingStructureProvider implements IProjectionListener, ICFoldingStructureProvider {
	
	private static class CProjectionAnnotation extends ProjectionAnnotation {
		
		private ICElement fCElement;
		private boolean fIsComment;
		
		public CProjectionAnnotation(ICElement element, boolean isCollapsed, boolean isComment) {
			super(isCollapsed);
			fCElement= element;
			fIsComment= isComment;
		}
		
		public ICElement getElement() {
			return fCElement;
		}
		
		public void setElement(ICElement element) {
			fCElement= element;
		}
		
		public boolean isComment() {
			return fIsComment;
		}
		
		public void setIsComment(boolean isComment) {
			fIsComment= isComment;
		}
	}
	
	private class ElementChangedListener implements IElementChangedListener {
		
		/*
		 * @see org.eclipse.jdt.core.IElementChangedListener#elementChanged(org.eclipse.jdt.core.ElementChangedEvent)
		 */
		public void elementChanged(ElementChangedEvent e) {
			ICElementDelta delta= findElement(fInput, e.getDelta());
			if (delta != null)
				processDelta(delta);
		}
		
		private ICElementDelta findElement(ICElement target, ICElementDelta delta) {
			
			if (delta == null || target == null)
				return null;
			
			ICElement element= delta.getElement();
			
			if (element.getElementType() > ICElement.C_UNIT)
				return null;
			
			if (target.equals(element))
				return delta;				
			
			ICElementDelta[] children= delta.getAffectedChildren();
			if (children == null || children.length == 0)
				return null;
				
			for (int i= 0; i < children.length; i++) {
				ICElementDelta d= findElement(target, children[i]);
				if (d != null)
					return d;
			}
			
			return null;
		}		
	}
	
	
	private IDocument fCachedDocument;
	
	private ITextEditor fEditor;
	private ProjectionViewer fViewer;
	protected ICElement fInput;
	private IElementChangedListener fElementListener;
	
	private boolean fAllowCollapsing= false;
	private boolean fCollapseMacros= false;
	private boolean fCollapseFunctions= true;
	private boolean fCollapseStructures= true;
	private boolean fCollapseMethods= false;
	
	public DefaultCFoldingStructureProvider() {
	}
	
	public void install(ITextEditor editor, ProjectionViewer viewer) {
		if (editor instanceof CEditor) {
			fEditor= editor;
			fViewer= viewer;
			fViewer.addProjectionListener(this);
		}
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
		
		if (fEditor instanceof CEditor) {
			initialize();
			fElementListener= new ElementChangedListener();
			CoreModel.getDefault().addElementChangedListener(fElementListener);
		}
	}
	
	/*
	 * @see org.eclipse.jface.text.source.projection.IProjectionListener#projectionDisabled()
	 */
	public void projectionDisabled() {
		fCachedDocument= null;
		if (fElementListener != null) {
			CoreModel.getDefault().removeElementChangedListener(fElementListener);
			fElementListener= null;
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
			
			if (fEditor instanceof CEditor) {
				IWorkingCopyManager manager= CUIPlugin.getDefault().getWorkingCopyManager();
				fInput= manager.getWorkingCopy(fEditor.getEditorInput());
			}

			if (fInput != null) {
				ProjectionAnnotationModel model= (ProjectionAnnotationModel) fEditor.getAdapter(ProjectionAnnotationModel.class);
				if (model != null) {
					
					if (fInput instanceof IWorkingCopy) {
						IWorkingCopy unit= (IWorkingCopy) fInput;
						synchronized (unit) {
							try {
								unit.reconcile();
							} catch (CModelException x) {
							}
						}
					}

					Map additions= computeAdditions((IParent) fInput);
					model.removeAllAnnotations();
					model.replaceAnnotations(null, additions);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			fCachedDocument= null;
			fAllowCollapsing= false;
		}
	}

	private void initializePreferences() {
		IPreferenceStore store= CUIPlugin.getDefault().getPreferenceStore();
		fCollapseFunctions= store.getBoolean(PreferenceConstants.EDITOR_FOLDING_FUNCTIONS);
		fCollapseStructures= store.getBoolean(PreferenceConstants.EDITOR_FOLDING_STRUCTURES);
		fCollapseMacros= store.getBoolean(PreferenceConstants.EDITOR_FOLDING_MACROS);
		fCollapseMethods= store.getBoolean(PreferenceConstants.EDITOR_FOLDING_METHODS);
	}

	private Map computeAdditions(IParent parent) {
		Map map= new HashMap();
		try {
			computeAdditions(parent.getChildren(), map);
		} catch (CModelException x) {
		}
		return map;
	}

	private void computeAdditions(ICElement[] elements, Map map) throws CModelException {
		for (int i= 0; i < elements.length; i++) {
			ICElement element= elements[i];
			
			computeAdditions(element, map);
			
			if (element instanceof IParent) {
				IParent parent= (IParent) element;
				computeAdditions(parent.getChildren(), map);
			}
		}
	}

	private void computeAdditions(ICElement element, Map map) {
		
		boolean createProjection= false;
		
		boolean collapse= false;
		switch (element.getElementType()) {
			
			case ICElement.C_STRUCT:
			case ICElement.C_CLASS:
				collapse= fAllowCollapsing && fCollapseStructures;
				createProjection= true;
				break;
			case ICElement.C_MACRO:
				collapse= fAllowCollapsing && fCollapseMacros;
				createProjection= true;
				break;
			case ICElement.C_FUNCTION:
				collapse= fAllowCollapsing && fCollapseFunctions;
				createProjection= true;
				break;
			case ICElement.C_METHOD:
				collapse= fAllowCollapsing && fCollapseMethods;
				createProjection= true;
				break;
		}
		
		if (createProjection) {
			Position position= createProjectionPosition(element);
			if (position != null) {
				map.put(new CProjectionAnnotation(element, collapse, false), position);
			}
		}
	}

	private Position createProjectionPosition(ICElement element) {
		
		if (fCachedDocument == null)
			return null;
		
		try {
			if (element instanceof ISourceReference) {
				ISourceReference reference= (ISourceReference) element;
				ISourceRange range= reference.getSourceRange();
				
				int start= fCachedDocument.getLineOfOffset(range.getStartPos());
				int end= fCachedDocument.getLineOfOffset(range.getStartPos() + range.getLength());
				if (start < end) {
					int offset= fCachedDocument.getLineOffset(start);
					int endOffset= fCachedDocument.getLineOffset(end + 1);
					return new Position(offset, endOffset - offset);
				}
			}
		} catch (BadLocationException x) {
		} catch (CModelException e) {
		}
		return null;
	}
		
	protected void processDelta(ICElementDelta delta) {
		
		if (!isInstalled())
			return;
		
		ProjectionAnnotationModel model= (ProjectionAnnotationModel) fEditor.getAdapter(ProjectionAnnotationModel.class);
		if (model == null)
			return;
		
		try {
			
			IDocumentProvider provider= fEditor.getDocumentProvider();
			fCachedDocument= provider.getDocument(fEditor.getEditorInput());
			fAllowCollapsing= false;
			
			Map additions= new HashMap();
			List deletions= new ArrayList();
			List updates= new ArrayList();
			
			Map updated= computeAdditions((IParent) fInput);
			Map previous= createAnnotationMap(model);
			
			
			Iterator e= updated.keySet().iterator();
			while (e.hasNext()) {
				CProjectionAnnotation annotation= (CProjectionAnnotation) e.next();
				ICElement element= annotation.getElement();
				Position position= (Position) updated.get(annotation);
				
				List annotations= (List) previous.get(element);
				if (annotations == null) {
					
					additions.put(annotation, position);
					
				} else {
					
					Iterator x= annotations.iterator();
					while (x.hasNext()) {
						CProjectionAnnotation a= (CProjectionAnnotation) x.next();
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
			
			e= previous.values().iterator();
			while (e.hasNext()) {
				List list= (List) e.next();
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
	
	private void match(ProjectionAnnotationModel model, List deletions, Map additions, List changes) {
		if (deletions.isEmpty() || (additions.isEmpty() && changes.isEmpty()))
			return;
		
		List newDeletions= new ArrayList();
		List newChanges= new ArrayList();
		
		Iterator deletionIterator= deletions.iterator();
		outer: while (deletionIterator.hasNext()) {
			CProjectionAnnotation deleted= (CProjectionAnnotation) deletionIterator.next();
			Position deletedPosition= model.getPosition(deleted);
			if (deletedPosition == null)
				continue;
			
			Iterator changesIterator= changes.iterator();
			while (changesIterator.hasNext()) {
				CProjectionAnnotation changed= (CProjectionAnnotation) changesIterator.next();
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
			
			Iterator additionsIterator= additions.keySet().iterator();
			while (additionsIterator.hasNext()) {
				CProjectionAnnotation added= (CProjectionAnnotation) additionsIterator.next();
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

	private Map createAnnotationMap(IAnnotationModel model) {
		Map map= new HashMap();
		Iterator e= model.getAnnotationIterator();
		while (e.hasNext()) {
			Object annotation= e.next();
			if (annotation instanceof CProjectionAnnotation) {
				CProjectionAnnotation c= (CProjectionAnnotation) annotation;
				List list= (List) map.get(c.getElement());
				if (list == null) {
					list= new ArrayList(2);
					map.put(c.getElement(), list);
				}
				list.add(c);
			}
		}
		return map;
	}

}
