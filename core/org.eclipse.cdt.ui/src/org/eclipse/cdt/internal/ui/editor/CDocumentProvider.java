/*******************************************************************************
 * Copyright (c) 2002, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * Anton Leherbauer (Wind River Systems) - Fixed bug 141295
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.editor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceRuleFactory;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultLineTracker;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ILineTracker;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationModelEvent;
import org.eclipse.jface.text.source.IAnnotationAccessExtension;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelListener;
import org.eclipse.jface.text.source.IAnnotationModelListenerExtension;
import org.eclipse.jface.text.source.IAnnotationPresentation;
import org.eclipse.jface.text.source.ImageUtilities;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.eclipse.ui.texteditor.AnnotationPreference;
import org.eclipse.ui.texteditor.AnnotationPreferenceLookup;
import org.eclipse.ui.texteditor.MarkerAnnotation;
import org.eclipse.ui.texteditor.MarkerUtilities;
import org.eclipse.ui.texteditor.ResourceMarkerAnnotationModel;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IProblemRequestor;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.core.parser.IProblem;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;

import org.eclipse.cdt.internal.core.model.IBufferFactory;

import org.eclipse.cdt.internal.ui.CFileElementWorkingCopy;
import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.internal.ui.text.IProblemRequestorExtension;

/**
 * CDocumentProvider2
 */
public class CDocumentProvider extends TextFileDocumentProvider {
	/**
	 * Bundle of all required informations to allow working copy management.
	 */
	static protected class TranslationUnitInfo extends FileInfo {
		public IWorkingCopy fCopy;
	}

	/**
	 * Annotation representating an <code>IProblem</code>.
	 */
	static protected class ProblemAnnotation extends Annotation implements ICAnnotation, IAnnotationPresentation {

		private static final String SPELLING_ANNOTATION_TYPE= "org.eclipse.ui.workbench.texteditor.spelling"; //$NON-NLS-1$
		
		//XXX: To be fully correct these constants should be non-static
		/** 
		 * The layer in which task problem annotations are located.
		 */
		//private static final int TASK_LAYER;
		/** 
		 * The layer in which info problem annotations are located.
		 */
		private static final int INFO_LAYER;
		/** 
		 * The layer in which warning problem annotations representing are located.
		 */
		private static final int WARNING_LAYER;
		/** 
		 * The layer in which error problem annotations representing are located.
		 */
		private static final int ERROR_LAYER;
		
		static {
			AnnotationPreferenceLookup lookup= EditorsUI.getAnnotationPreferenceLookup();
			//TASK_LAYER= computeLayer("org.eclipse.ui.workbench.texteditor.task", lookup); //$NON-NLS-1$
			INFO_LAYER= computeLayer("org.eclipse.cdt.ui.info", lookup); //$NON-NLS-1$
			WARNING_LAYER= computeLayer("org.eclipse.cdt.ui.warning", lookup); //$NON-NLS-1$
			ERROR_LAYER= computeLayer("org.eclipse.cdt.ui.error", lookup); //$NON-NLS-1$
		}
		
		private static int computeLayer(String annotationType, AnnotationPreferenceLookup lookup) {
			Annotation annotation= new Annotation(annotationType, false, null);
			AnnotationPreference preference= lookup.getAnnotationPreference(annotation);
			if (preference != null)
				return preference.getPresentationLayer() + 1;
			return IAnnotationAccessExtension.DEFAULT_LAYER + 1;
		}
		
		private static Image fgQuickFixImage;
		private static Image fgQuickFixErrorImage;
		private static boolean fgQuickFixImagesInitialized= false;
		
		private ITranslationUnit fTranslationUnit;
		private List fOverlaids;
		private IProblem fProblem;
		private Image fImage;
		private boolean fQuickFixImagesInitialized= false;
		private int fLayer= IAnnotationAccessExtension.DEFAULT_LAYER;
		
		
		public ProblemAnnotation(IProblem problem, ITranslationUnit cu) {
			
			fProblem= problem;
			fTranslationUnit= cu;
			
			//if (SpellProblem.Spelling == fProblem.getID()) {
			//	setType(SPELLING_ANNOTATION_TYPE);
			//	fLayer= WARNING_LAYER;
			//if (IProblem.Task == fProblem.getID()) {
			//	setType(CMarkerAnnotation.TASK_ANNOTATION_TYPE);
			//	fLayer= TASK_LAYER;
			if (fProblem.isWarning()) {
				setType(CMarkerAnnotation.WARNING_ANNOTATION_TYPE);
				fLayer= WARNING_LAYER;
			} else if (fProblem.isError()) {
				setType(CMarkerAnnotation.ERROR_ANNOTATION_TYPE);
				fLayer= ERROR_LAYER;
			} else {
				setType(CMarkerAnnotation.INFO_ANNOTATION_TYPE);
				fLayer= INFO_LAYER;
			}
		}
		
		/*
		 * @see org.eclipse.jface.text.source.IAnnotationPresentation#getLayer()
		 */
		public int getLayer() {
			return fLayer;
		}
		
		private void initializeImages() {
			// http://bugs.eclipse.org/bugs/show_bug.cgi?id=18936
			if (!fQuickFixImagesInitialized) {
				if (isProblem() && indicateQuixFixableProblems()) { // no light bulb for tasks
					if (!fgQuickFixImagesInitialized) {
						fgQuickFixImage= CPluginImages.get(CPluginImages.IMG_OBJS_FIXABLE_PROBLEM);
						fgQuickFixErrorImage= CPluginImages.get(CPluginImages.IMG_OBJS_FIXABLE_ERROR);
						fgQuickFixImagesInitialized= true;
					}
					if (CMarkerAnnotation.ERROR_ANNOTATION_TYPE.equals(getType()))
						fImage= fgQuickFixErrorImage;
					else
						fImage= fgQuickFixImage;
				}
				fQuickFixImagesInitialized= true;
			}
		}
	
		private boolean indicateQuixFixableProblems() {
			return PreferenceConstants.getPreferenceStore().getBoolean(PreferenceConstants.EDITOR_CORRECTION_INDICATION);
		}

		/*
		 * @see org.eclipse.jface.text.source.IAnnotationPresentation#paint(org.eclipse.swt.graphics.GC, org.eclipse.swt.widgets.Canvas, org.eclipse.swt.graphics.Rectangle)
		 */
		public void paint(GC gc, Canvas canvas, Rectangle r) {
			initializeImages();
			if (fImage != null) {
				ImageUtilities.drawImage(fImage, gc, canvas, r, SWT.CENTER, SWT.TOP);
			}
		}
		
		/*
		 * @see ICAnnotation#getMessage()
		 */
		public String getText() {
			return fProblem.getMessage();
		}
		
		/*
		 * @see ICAnnotation#getArguments()
		 */
		public String[] getArguments() {
			return isProblem() ? new String[]{fProblem.getArguments()} : null;
		}
	
		/*
		 * @see ICAnnotation#getId()
		 */
		public int getId() {
			return fProblem.getID();
		}
	
		/*
		 * @see ICAnnotation#isProblem()
		 */
		public boolean isProblem() {
			String type= getType();
			return  CMarkerAnnotation.WARNING_ANNOTATION_TYPE.equals(type)  || 
						CMarkerAnnotation.ERROR_ANNOTATION_TYPE.equals(type) ||
						SPELLING_ANNOTATION_TYPE.equals(type);
		}
		
		/*
		 * @see ICAnnotation#hasOverlay()
		 */
		public boolean hasOverlay() {
			return false;
		}
		
		/*
		 * @see org.eclipse.cdt.internal.ui.javaeditor.IJavaAnnotation#getOverlay()
		 */
		public ICAnnotation getOverlay() {
			return null;
		}
		
		/*
		 * @see ICAnnotation#addOverlaid(ICAnnotation)
		 */
		public void addOverlaid(ICAnnotation annotation) {
			if (fOverlaids == null)
				fOverlaids= new ArrayList(1);
			fOverlaids.add(annotation);
		}
	
		/*
		 * @see ICAnnotation#removeOverlaid(ICAnnotation)
		 */
		public void removeOverlaid(ICAnnotation annotation) {
			if (fOverlaids != null) {
				fOverlaids.remove(annotation);
				if (fOverlaids.size() == 0)
					fOverlaids= null;
			}
		}
		
		/*
		 * @see ICAnnotation#getOverlaidIterator()
		 */
		public Iterator getOverlaidIterator() {
			if (fOverlaids != null)
				return fOverlaids.iterator();
			return null;
		}
				
		/*
		 * @see org.eclipse.cdt.internal.ui.editor.ICAnnotation#getTranslationUnit()
		 */
		public ITranslationUnit getTranslationUnit() {
			return fTranslationUnit;
		}
	}
		
	/**
	 * Internal structure for mapping positions to some value. 
	 * The reason for this specific structure is that positions can
	 * change over time. Thus a lookup is based on value and not
	 * on hash value.
	 */
	protected static class ReverseMap {
		
		static class Entry {
			Position fPosition;
			Object fValue;
		}
		
		private List fList= new ArrayList(2);
		private int fAnchor= 0;
		
		public ReverseMap() {
		}
		
		public Object get(Position position) {
			
			Entry entry;
			
			// behind anchor
			int length= fList.size();
			for (int i= fAnchor; i < length; i++) {
				entry= (Entry) fList.get(i);
				if (entry.fPosition.equals(position)) {
					fAnchor= i;
					return entry.fValue;
				}
			}
			
			// before anchor
			for (int i= 0; i < fAnchor; i++) {
				entry= (Entry) fList.get(i);
				if (entry.fPosition.equals(position)) {
					fAnchor= i;
					return entry.fValue;
				}
			}
			
			return null;
		}
		
		private int getIndex(Position position) {
			Entry entry;
			int length= fList.size();
			for (int i= 0; i < length; i++) {
				entry= (Entry) fList.get(i);
				if (entry.fPosition.equals(position))
					return i;
			}
			return -1;
		}
		
		public void put(Position position,  Object value) {
			int index= getIndex(position);
			if (index == -1) {
				Entry entry= new Entry();
				entry.fPosition= position;
				entry.fValue= value;
				fList.add(entry);
			} else {
				Entry entry= (Entry) fList.get(index);
				entry.fValue= value;
			}
		}
		
		public void remove(Position position) {
			int index= getIndex(position);
			if (index > -1)
				fList.remove(index);
		}
		
		public void clear() {
			fList.clear();
		}
	}

	/**
	 * Annotation model dealing with c marker annotations and temporary problems.
	 * Also acts as problem requestor for its translation unit. Initialiy inactive. Must explicitly be
	 * activated.
	 */
	protected static class TranslationUnitAnnotationModel extends ResourceMarkerAnnotationModel implements IProblemRequestor, IProblemRequestorExtension {
		
		private static class ProblemRequestorState {
			boolean fInsideReportingSequence= false;
			List fReportedProblems;
		}
		
		private ThreadLocal fProblemRequestorState= new ThreadLocal();
		private int fStateCount= 0;
		
		private ITranslationUnit fTranslationUnit;
		private List fGeneratedAnnotations;
		private IProgressMonitor fProgressMonitor;
		private boolean fIsActive= false;
		
		private ReverseMap fReverseMap= new ReverseMap();
		private List fPreviouslyOverlaid= null; 
		private List fCurrentlyOverlaid= new ArrayList();
		
		
		public TranslationUnitAnnotationModel(IResource resource) {
			super(resource);
		}
		
		public void setTranslationUnit(ITranslationUnit unit)  {
			fTranslationUnit= unit;
		}
		
		protected MarkerAnnotation createMarkerAnnotation(IMarker marker) {
			String markerType= MarkerUtilities.getMarkerType(marker);
			if (markerType != null && markerType.startsWith(CMarkerAnnotation.C_MARKER_TYPE_PREFIX)) {
				// TODO: Fix this we need the document
				return new CMarkerAnnotation(marker, null);
			}
			return super.createMarkerAnnotation(marker);
		}
		
		
		/* (non-Javadoc)
		 * @see org.eclipse.ui.texteditor.AbstractMarkerAnnotationModel#createPositionFromMarker(org.eclipse.core.resources.IMarker)
		 */
		protected Position createPositionFromMarker(IMarker marker) {
			int start= MarkerUtilities.getCharStart(marker);
			int end= MarkerUtilities.getCharEnd(marker);
			
			if (start > end) {
				end= start + end;
				start= end - start;
				end= end - start;
			}
			
			if (start == -1 && end == -1) {
				// marker line number is 1-based
				int line= MarkerUtilities.getLineNumber(marker);
				if (line > 0 && fDocument != null) {
					try {
						start= fDocument.getLineOffset(line - 1);
						String ld = fDocument.getLineDelimiter(line - 1);
						int lineDelimiterLegnth = ld != null ? ld.length(): 0;
						end= fDocument.getLineLength(line - 1) + start - lineDelimiterLegnth;
					} catch (BadLocationException x) {
					}
				}
			}
			
			if (start > -1 && end > -1)
				return new Position(start, end - start);
			
			return null;
		}
		/*
		 * @see org.eclipse.jface.text.source.AnnotationModel#createAnnotationModelEvent()
		 */
		protected AnnotationModelEvent createAnnotationModelEvent() {
			return new TranslationUnitAnnotationModelEvent(this, getResource());
		}
		
		protected Position createPositionFromProblem(IProblem problem) {
			int start= problem.getSourceStart();
			if (start < 0)
				return null;
			
			int length= problem.getSourceEnd() - problem.getSourceStart() + 1;
			if (length < 0)
				return null;
			return new Position(start, length);
		}
		
		/*
		 * @see IProblemRequestor#beginReporting()
		 */
		public void beginReporting() {
			ProblemRequestorState state= (ProblemRequestorState) fProblemRequestorState.get();
			if (state == null)
				internalBeginReporting(false);				
		}
		
		/*
		 * @see org.eclipse.jdt.internal.ui.text.java.IProblemRequestorExtension#beginReportingSequence()
		 */
		public void beginReportingSequence() {
			ProblemRequestorState state= (ProblemRequestorState) fProblemRequestorState.get();
			if (state == null)
				internalBeginReporting(true);
		}
		
		/**
		 * Sets up the infrastructure necessary for problem reporting.
		 * 
		 * @param insideReportingSequence <code>true</code> if this method
		 *            call is issued from inside a reporting sequence
		 */
		private void internalBeginReporting(boolean insideReportingSequence) {
			if (fTranslationUnit != null && fTranslationUnit.getCProject().isOnSourceRoot(fTranslationUnit.getResource())) {
				ProblemRequestorState state= new ProblemRequestorState();
				state.fInsideReportingSequence= insideReportingSequence;
				state.fReportedProblems= new ArrayList();
				synchronized (getLockObject()) {
					fProblemRequestorState.set(state);
					++fStateCount;
				}
			}
		}
		
		/*
		 * @see IProblemRequestor#acceptProblem(IProblem)
		 */
		public void acceptProblem(IProblem problem) {
			if (isActive()) {
				ProblemRequestorState state= (ProblemRequestorState) fProblemRequestorState.get();
				if (state != null)
					state.fReportedProblems.add(problem);
			}
		}
		
		/*
		 * @see IProblemRequestor#endReporting()
		 */
		public void endReporting() {
			ProblemRequestorState state= (ProblemRequestorState) fProblemRequestorState.get();
			if (state != null && !state.fInsideReportingSequence)
				internalEndReporting(state);
		}
		
		/*
		 * @see org.eclipse.jdt.internal.ui.text.java.IProblemRequestorExtension#endReportingSequence()
		 */
		public void endReportingSequence() {
			ProblemRequestorState state= (ProblemRequestorState) fProblemRequestorState.get();
			if (state != null && state.fInsideReportingSequence)
				internalEndReporting(state);
		}
		
		private void internalEndReporting(ProblemRequestorState state) {
			int stateCount= 0;
			synchronized(getLockObject()) {
				-- fStateCount;
				stateCount= fStateCount;
				fProblemRequestorState.set(null);
			}
			
			if (stateCount == 0 && isActive())
				reportProblems(state.fReportedProblems);
		}
		
		/**
		 * Signals the end of problem reporting.
		 */
		private void reportProblems(List reportedProblems) {
			if (fProgressMonitor != null && fProgressMonitor.isCanceled())
				return;
			
			boolean temporaryProblemsChanged= false;
			
			synchronized (getLockObject()) {
				
				boolean isCanceled= false;
				
				fPreviouslyOverlaid= fCurrentlyOverlaid;
				fCurrentlyOverlaid= new ArrayList();
				
				if (fGeneratedAnnotations.size() > 0) {
					temporaryProblemsChanged= true;	
					removeAnnotations(fGeneratedAnnotations, false, true);
					fGeneratedAnnotations.clear();
				}
				
				if (reportedProblems != null && reportedProblems.size() > 0) {
					
					Iterator e= reportedProblems.iterator();
					while (e.hasNext()) {
						
						if (fProgressMonitor != null && fProgressMonitor.isCanceled()) {
							isCanceled= true;
							break;
						}
						
						IProblem problem= (IProblem) e.next();
						Position position= createPositionFromProblem(problem);
						if (position != null) {
							
							try {
								ProblemAnnotation annotation= new ProblemAnnotation(problem, fTranslationUnit);
								overlayMarkers(position, annotation);								
								addAnnotation(annotation, position, false);
								fGeneratedAnnotations.add(annotation);
								
								temporaryProblemsChanged= true;
							} catch (BadLocationException x) {
								// ignore invalid position
							}
						}
					}
				}
				
				removeMarkerOverlays(isCanceled);
				fPreviouslyOverlaid= null;
			}
			
			if (temporaryProblemsChanged)
				fireModelChanged();
		}
		
		private void removeMarkerOverlays(boolean isCanceled) {
			if (isCanceled) {
				fCurrentlyOverlaid.addAll(fPreviouslyOverlaid);
			} else if (fPreviouslyOverlaid != null) {
				Iterator e= fPreviouslyOverlaid.iterator();
				while (e.hasNext()) {
					CMarkerAnnotation annotation= (CMarkerAnnotation) e.next();
					annotation.setOverlay(null);
				}
			}			
		}
		
		/**
		 * Overlays value with problem annotation.
		 * @param problemAnnotation
		 */
		private void setOverlay(Object value, ProblemAnnotation problemAnnotation) {
			if (value instanceof CMarkerAnnotation) {
				CMarkerAnnotation annotation= (CMarkerAnnotation) value;
				if (annotation.isProblem()) {
					annotation.setOverlay(problemAnnotation);
					fPreviouslyOverlaid.remove(annotation);
					fCurrentlyOverlaid.add(annotation);
				}
			} else {
			}
		}
		
		private void  overlayMarkers(Position position, ProblemAnnotation problemAnnotation) {
			Object value= getAnnotations(position);
			if (value instanceof List) {
				List list= (List) value;
				for (Iterator e = list.iterator(); e.hasNext();)
					setOverlay(e.next(), problemAnnotation);
			} else {
				setOverlay(value, problemAnnotation);
			}
		}
		
		/**
		 * Tells this annotation model to collect temporary problems from now on.
		 */
		private void startCollectingProblems() {
			fGeneratedAnnotations= new ArrayList();  
		}
		
		/**
		 * Tells this annotation model to no longer collect temporary problems.
		 */
		private void stopCollectingProblems() {
			if (fGeneratedAnnotations != null)
				removeAnnotations(fGeneratedAnnotations, true, true);
			fGeneratedAnnotations= null;
		}
		
		/*
		 * @see IProblemRequestor#isActive()
		 */
		public boolean isActive() {
			return fIsActive;
		}
		
		/*
		 * @see IProblemRequestorExtension#setProgressMonitor(IProgressMonitor)
		 */
		public void setProgressMonitor(IProgressMonitor monitor) {
			fProgressMonitor= monitor;
		}
		
		/*
		 * @see IProblemRequestorExtension#setIsActive(boolean)
		 */
		public void setIsActive(boolean isActive) {
			if (fIsActive != isActive) {
				fIsActive= isActive;
				if (fIsActive)
					startCollectingProblems();
				else
					stopCollectingProblems();
			}
		}
		
		private Object getAnnotations(Position position) {
			return fReverseMap.get(position);
		}
		
		/*
		 * @see AnnotationModel#addAnnotation(Annotation, Position, boolean)
		 */
		protected void addAnnotation(Annotation annotation, Position position, boolean fireModelChanged) throws BadLocationException {				
			super.addAnnotation(annotation, position, fireModelChanged);
			
			Object cached= fReverseMap.get(position);
			if (cached == null)
				fReverseMap.put(position, annotation);
			else if (cached instanceof List) {
				List list= (List) cached;
				list.add(annotation);
			} else if (cached instanceof Annotation) {
				List list= new ArrayList(2);
				list.add(cached);
				list.add(annotation);
				fReverseMap.put(position, list);
			}
		}
		
		/*
		 * @see AnnotationModel#removeAllAnnotations(boolean)
		 */
		protected void removeAllAnnotations(boolean fireModelChanged) {
			super.removeAllAnnotations(fireModelChanged);
			fReverseMap.clear();
		}
		
		/*
		 * @see AnnotationModel#removeAnnotation(Annotation, boolean)
		 */
		protected void removeAnnotation(Annotation annotation, boolean fireModelChanged) {
			Position position= getPosition(annotation);
			Object cached= fReverseMap.get(position);
			if (cached instanceof List) {
				List list= (List) cached;
				list.remove(annotation);
				if (list.size() == 1) {
					fReverseMap.put(position, list.get(0));
					list.clear();
				}
			} else if (cached instanceof Annotation) {
				fReverseMap.remove(position);
			}
			super.removeAnnotation(annotation, fireModelChanged);
		}
	}

	protected static class GlobalAnnotationModelListener implements IAnnotationModelListener, IAnnotationModelListenerExtension {
		
		private ArrayList fListenerList;
		
		public GlobalAnnotationModelListener() {
			fListenerList= new ArrayList();
		}
		
		/**
		 * @see IAnnotationModelListener#modelChanged(IAnnotationModel)
		 */
		public void modelChanged(IAnnotationModel model) {
			Object[] listeners= fListenerList.toArray();
			for (int i= 0; i < listeners.length; i++) {
				((IAnnotationModelListener) listeners[i]).modelChanged(model);
			}
		}

		/**
		 * @see IAnnotationModelListenerExtension#modelChanged(AnnotationModelEvent)
		 */
		public void modelChanged(AnnotationModelEvent event) {
			Object[] listeners= fListenerList.toArray();
			for (int i= 0; i < listeners.length; i++) {
				Object curr= listeners[i];
				if (curr instanceof IAnnotationModelListenerExtension) {
					((IAnnotationModelListenerExtension) curr).modelChanged(event);
				}
			}
		}
		
		public void addListener(IAnnotationModelListener listener) {
			fListenerList.add(listener);
		}
		
		public void removeListener(IAnnotationModelListener listener) {
			fListenerList.remove(listener);
		}			
	}		
	
	/** Preference key for temporary problems */
	private final static String HANDLE_TEMPORARY_PROBLEMS= PreferenceConstants.EDITOR_EVALUATE_TEMPORARY_PROBLEMS;

	/** Internal property changed listener */
	private IPropertyChangeListener fPropertyListener;
	/** Annotation model listener added to all created CU annotation models */
	private GlobalAnnotationModelListener fGlobalAnnotationModelListener;	


	/** The save policy used by this provider */
	//private ISavePolicy fSavePolicy;

	/**
	 *  
	 */
	public CDocumentProvider() {
		super();
		setParentDocumentProvider(new TextFileDocumentProvider(new CStorageDocumentProvider()));
		fGlobalAnnotationModelListener= new GlobalAnnotationModelListener();
		fPropertyListener= new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (HANDLE_TEMPORARY_PROBLEMS.equals(event.getProperty()))
					enableHandlingTemporaryProblems();
			}
		};
		CUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(fPropertyListener);
	}

	/**
	 * Creates a translation unit from the given file.
	 * 
	 * @param file
	 *            the file from which to create the translation unit
	 */
	protected ITranslationUnit createTranslationUnit(IFile file) {
		Object element = CoreModel.getDefault().create(file);
		if (element instanceof ITranslationUnit) {
			return (ITranslationUnit) element;
		}
		return null;
	}

	/*
	 * @see org.eclipse.ui.editors.text.TextFileDocumentProvider#createEmptyFileInfo()
	 */
	protected FileInfo createEmptyFileInfo() {
		return new TranslationUnitInfo();
	}

	/*
	 * @see org.eclipse.ui.editors.text.TextFileDocumentProvider#createAnnotationModel(org.eclipse.core.resources.IFile)
	 */
	protected IAnnotationModel createAnnotationModel(IFile file) {
		//return new CMarkerAnnotationModel(file);
		return new TranslationUnitAnnotationModel(file);
	}

	/*
	 * @see org.eclipse.ui.editors.text.TextFileDocumentProvider#createFileInfo(java.lang.Object)
	 */
	protected FileInfo createFileInfo(Object element) throws CoreException {
		ITranslationUnit original = null;
		if (element instanceof IFileEditorInput) {
			IFileEditorInput input = (IFileEditorInput)element;
			original = createTranslationUnit(input.getFile());
		} else if (element instanceof ITranslationUnitEditorInput) {
			ITranslationUnitEditorInput input = (ITranslationUnitEditorInput)element;
			original = input.getTranslationUnit();
		}

		if (original == null) {
			return null;
		}
		
		FileInfo info = super.createFileInfo(element);
		if (!(info instanceof TranslationUnitInfo))
			return null;
		TranslationUnitInfo tuInfo = (TranslationUnitInfo) info;
		setUpSynchronization(tuInfo);

		IProblemRequestor requestor= tuInfo.fModel instanceof IProblemRequestor ? (IProblemRequestor) tuInfo.fModel : null;
		IWorkingCopy copy = null;
		if (element instanceof IFileEditorInput) {
			IBufferFactory factory = CUIPlugin.getDefault().getBufferFactory();
			copy = original.getSharedWorkingCopy(getProgressMonitor(), factory, requestor);
		} else if (element instanceof ITranslationUnitEditorInput) {
			copy = new CFileElementWorkingCopy(original);
		}
		tuInfo.fCopy = copy;

		if (tuInfo.fModel instanceof TranslationUnitAnnotationModel) {
			TranslationUnitAnnotationModel model= (TranslationUnitAnnotationModel) tuInfo.fModel;
			model.setTranslationUnit(tuInfo.fCopy);
		}
		if (tuInfo.fModel != null)
			tuInfo.fModel.addAnnotationModelListener(fGlobalAnnotationModelListener);
		if (requestor instanceof IProblemRequestorExtension) {
			IProblemRequestorExtension extension= (IProblemRequestorExtension)requestor;
			extension.setIsActive(isHandlingTemporaryProblems());
		}
		return tuInfo;
	}

	/*
	 * @see org.eclipse.ui.editors.text.TextFileDocumentProvider#disposeFileInfo(java.lang.Object,
	 *      org.eclipse.ui.editors.text.TextFileDocumentProvider.FileInfo)
	 */
	protected void disposeFileInfo(Object element, FileInfo info) {
		if (info instanceof TranslationUnitInfo) {
			TranslationUnitInfo tuInfo = (TranslationUnitInfo) info;
			tuInfo.fCopy.destroy();
			if (tuInfo.fModel != null)
				tuInfo.fModel.removeAnnotationModelListener(fGlobalAnnotationModelListener);
		}
		super.disposeFileInfo(element, info);
	}

	protected void commitWorkingCopy(IProgressMonitor monitor, Object element, TranslationUnitInfo info, boolean overwrite)
			throws CoreException {

		synchronized (info.fCopy) {
			info.fCopy.reconcile();
		}

		IDocument document= info.fTextFileBuffer.getDocument();
		IResource resource= info.fCopy.getResource();
		
		//Assert.isTrue(resource instanceof IFile);
		if (resource instanceof IFile && !resource.exists()) {
			// underlying resource has been deleted, just recreate file, ignore the rest
			createFileFromDocument(monitor, (IFile) resource, document);
			return;
		}

		//if (fSavePolicy != null)
		//	fSavePolicy.preSave(info.fCopy);
		
		try {
			//info.fCopy.commit(overwrite, monitor);
			commitFileBuffer(monitor, info, overwrite);
		} catch (CoreException x) {
			// inform about the failure
			fireElementStateChangeFailed(element);
			throw x;
		} catch (RuntimeException x) {
			// inform about the failure
			fireElementStateChangeFailed(element);
			throw x;
		} finally {
		}

		// If here, the dirty state of the editor will change to "not dirty".
		// Thus, the state changing flag will be reset.
		// NOTE: this is done in commitFileBuffer() if we use info.fCopy.comit(...) reenable code
		//if (info.fModel instanceof AbstractMarkerAnnotationModel) {
		//	AbstractMarkerAnnotationModel model= (AbstractMarkerAnnotationModel) info.fModel;
		//	model.updateMarkers(document);
		//}

	}

	/*
	 * @see org.eclipse.ui.editors.text.TextFileDocumentProvider#createSaveOperation(java.lang.Object, org.eclipse.jface.text.IDocument, boolean)
	 */
	protected DocumentProviderOperation createSaveOperation(final Object element, final IDocument document, final boolean overwrite) throws CoreException {
		//add a newline to the end of the document (if it is not already present)
		//-----------------------------------------------------------------------
		//for people who do not want auto-modification of their files,
		//this flag will prevent addition of a newline unless the user
		//explicitly sets the preference thru Window -> Preferences -> C/C++ -> Editor 
		//  -> Appearance Tab -> Ensure newline end of file when saving
		if (PreferenceConstants.getPreferenceStore().getBoolean(
				PreferenceConstants.ENSURE_NEWLINE_AT_EOF)) {
			// even if the document is empty, there will be at least one line in
			// it (the 0th one)
			int lastLineIndex = document.getNumberOfLines() - 1;

			try {
				// we have to ensure that the length of the last line is 0.
				// this will also take care of empty files. empty files have
				// only one line in them and the length of this one and only 
				// line is 0. 
				// Thus we do not need to append an extra line separator to
				// empty files.
				int lastLineLength = document.getLineLength(lastLineIndex);
				if (lastLineLength != 0) {
					document.replace(document.getLength(), 0, TextUtilities
							.getDefaultLineDelimiter(document));
				}
			} catch (BadLocationException e) {
			}
		}		
		
		final FileInfo info= getFileInfo(element);
		if (info instanceof TranslationUnitInfo) {
			return new DocumentProviderOperation() {
				/*
				 * @see org.eclipse.ui.editors.text.TextFileDocumentProvider.DocumentProviderOperation#execute(org.eclipse.core.runtime.IProgressMonitor)
				 */
				protected void execute(IProgressMonitor monitor) throws CoreException {
					commitWorkingCopy(monitor, element, (TranslationUnitInfo) info, overwrite);
				}
				/*
				 * @see org.eclipse.ui.editors.text.TextFileDocumentProvider.DocumentProviderOperation#getSchedulingRule()
				 */
				public ISchedulingRule getSchedulingRule() {
					if (info.fElement instanceof IFileEditorInput) {
						IFile file= ((IFileEditorInput) info.fElement).getFile();
						IResourceRuleFactory ruleFactory= ResourcesPlugin.getWorkspace().getRuleFactory();
						if (file == null || !file.exists())
							return ruleFactory.createRule(file);
						return ruleFactory.modifyRule(file);
					}
					return null;
				}
			};
		}
		return null;
	}

	/**
	 * Returns the preference whether handling temporary problems is enabled.
	 */
	protected boolean isHandlingTemporaryProblems() {
		IPreferenceStore store= CUIPlugin.getDefault().getPreferenceStore();
		return store.getBoolean(HANDLE_TEMPORARY_PROBLEMS);
	} 
	
	/**
	 * Switches the state of problem acceptance according to the value in the preference store.
	 */
	protected void enableHandlingTemporaryProblems() {
		boolean enable= isHandlingTemporaryProblems();
		for (Iterator iter= getFileInfosIterator(); iter.hasNext();) {
			FileInfo info= (FileInfo) iter.next();
			if (info.fModel instanceof IProblemRequestorExtension) {
				IProblemRequestorExtension  extension= (IProblemRequestorExtension) info.fModel;
				extension.setIsActive(enable);
			}
		}
	}

	/*
	 * @see org.eclipse.jdt.internal.ui.javaeditor.ITranlationUnitDocumentProvider#addGlobalAnnotationModelListener(org.eclipse.jface.text.source.IAnnotationModelListener)
	 */
	public void addGlobalAnnotationModelListener(IAnnotationModelListener listener) {
		fGlobalAnnotationModelListener.addListener(listener);
	}

	/*
	 * @see org.eclipse.jdt.internal.ui.javaeditor.ITranslationUnitDocumentProvider#removeGlobalAnnotationModelListener(org.eclipse.jface.text.source.IAnnotationModelListener)
	 */
	public void removeGlobalAnnotationModelListener(IAnnotationModelListener listener) {
		fGlobalAnnotationModelListener.removeListener(listener);
	}


	/*
	 * @see org.eclipse.jdt.internal.ui.javaeditor.ICompilationUnitDocumentProvider#getWorkingCopy(java.lang.Object)
	 */
	public IWorkingCopy getWorkingCopy(Object element) {
		FileInfo fileInfo = getFileInfo(element);
		if (fileInfo instanceof TranslationUnitInfo) {
			TranslationUnitInfo info = (TranslationUnitInfo) fileInfo;
			return info.fCopy;
		}
		return null;
	}

	/*
	 * @see org.eclipse.jdt.internal.ui.javaeditor.ICompilationUnitDocumentProvider#shutdown()
	 */
	public void shutdown() {
		//CUIPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(fPropertyListener);
		Iterator e = getConnectedElementsIterator();
		while (e.hasNext())
			disconnect(e.next());
	}

	/**
	 * Returns the underlying resource for the given element.
	 * 
	 * @param element  the element
	 * @return the underlying resource of the given element
	 */
	public IResource getUnderlyingResource(Object element) {
		if (element instanceof IFileEditorInput) {
			IFileEditorInput input= (IFileEditorInput) element;
			return input.getFile();
		}
		return null;
	}

	public ILineTracker createLineTracker(Object element) {
		return new DefaultLineTracker();
	}
}
