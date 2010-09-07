/*******************************************************************************
 * Copyright (c) 2002, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.editor;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultLineTracker;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.ILineTracker;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.AnnotationModelEvent;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelListener;
import org.eclipse.jface.text.source.IAnnotationModelListenerExtension;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.undo.DocumentUndoManagerRegistry;
import org.eclipse.text.undo.IDocumentUndoManager;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.editors.text.ForwardingDocumentProvider;
import org.eclipse.ui.editors.text.ILocationProvider;
import org.eclipse.ui.editors.text.ILocationProviderExtension;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.IMarkerUpdater;
import org.eclipse.ui.texteditor.MarkerAnnotation;
import org.eclipse.ui.texteditor.MarkerUtilities;
import org.eclipse.ui.texteditor.ResourceMarkerAnnotationModel;
import org.eclipse.ui.texteditor.spelling.SpellingAnnotation;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IProblemRequestor;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.core.parser.IPersistableProblem;
import org.eclipse.cdt.core.parser.IProblem;
import org.eclipse.cdt.ui.CDTUITools;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.cdt.ui.text.ICPartitions;

import org.eclipse.cdt.internal.core.model.TranslationUnit;

import org.eclipse.cdt.internal.ui.text.IProblemRequestorExtension;
import org.eclipse.cdt.internal.ui.text.spelling.CoreSpellingProblem;
import org.eclipse.cdt.internal.ui.util.EditorUtility;

/**
 * A document provider for C/C++ content.
 */
public class CDocumentProvider extends TextFileDocumentProvider {
	/**
	 * Bundle of all required informations to allow working copy management.
	 */
	static protected class TranslationUnitInfo extends FileInfo {
		public IWorkingCopy fCopy;
	}

	/**
	 * Annotation representing an <code>IProblem</code>.
	 */
	static protected class ProblemAnnotation extends Annotation implements ICAnnotation {
		private static final String INDEXER_ANNOTATION_TYPE= "org.eclipse.cdt.ui.indexmarker"; //$NON-NLS-1$
		
		private final ITranslationUnit fTranslationUnit;
		private final int fId;
		private final boolean fIsProblem;
		private final String[] fArguments;
		private final String fMarkerType;
		private List<ICAnnotation> fOverlaids;

		public ProblemAnnotation(IProblem problem, ITranslationUnit tu) {
			fTranslationUnit= tu;
			setText(problem.getMessage());
			fId= problem.getID();
			fIsProblem= problem.isError() || problem.isWarning();
			fArguments= isProblem() ? problem.getArguments() : null;
            setType(problem instanceof CoreSpellingProblem ?
            		SpellingAnnotation.TYPE : INDEXER_ANNOTATION_TYPE);
			if (problem instanceof IPersistableProblem)
				fMarkerType= ((IPersistableProblem) problem).getMarkerType();
			else
				fMarkerType= null;
		}
		
		/*
		 * @see ICAnnotation#getArguments()
		 */
		public String[] getArguments() {
			return fArguments;
		}
	
		/*
		 * @see ICAnnotation#getId()
		 */
		public int getId() {
			return fId;
		}
	
		/*
		 * @see ICAnnotation#isProblem()
		 */
		public boolean isProblem() {
			return fIsProblem;
		}
		
		/*
		 * @see ICAnnotation#hasOverlay()
		 */
		public boolean hasOverlay() {
			return false;
		}
		
		/*
		 * @see ICAnnotation#getOverlay()
		 */
		public ICAnnotation getOverlay() {
			return null;
		}
		
		/*
		 * @see ICAnnotation#addOverlaid(ICAnnotation)
		 */
		public void addOverlaid(ICAnnotation annotation) {
			if (fOverlaids == null)
				fOverlaids= new ArrayList<ICAnnotation>(1);
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
		public Iterator<ICAnnotation> getOverlaidIterator() {
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

		/*
		 * @see org.eclipsecjdt.internal.ui.editor.ICAnnotation#getMarkerType()
		 */
		public String getMarkerType() {
			return fMarkerType;
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
		
		private List<Entry> fList= new ArrayList<Entry>(2);
		private int fAnchor= 0;
		
		public ReverseMap() {
		}
		
		public Object get(Position position) {
			
			Entry entry;
			
			// behind anchor
			int length= fList.size();
			for (int i= fAnchor; i < length; i++) {
				entry= fList.get(i);
				if (entry.fPosition.equals(position)) {
					fAnchor= i;
					return entry.fValue;
				}
			}
			
			// before anchor
			for (int i= 0; i < fAnchor; i++) {
				entry= fList.get(i);
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
				entry= fList.get(i);
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
				Entry entry= fList.get(index);
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
			fAnchor= 0;
		}
	}

	/**
	 * A marker updater which removes problems markers with length 0.
	 */
	public static class ProblemMarkerUpdater implements IMarkerUpdater {

		/**
		 * Default constructor (executable extension).
		 */
		public ProblemMarkerUpdater() {
		}

		/*
		 * @see org.eclipse.ui.texteditor.IMarkerUpdater#getAttribute()
		 */
		public String[] getAttribute() {
			return null;
		}

		/*
		 * @see org.eclipse.ui.texteditor.IMarkerUpdater#getMarkerType()
		 */
		public String getMarkerType() {
			return ICModelMarker.C_MODEL_PROBLEM_MARKER;
		}

		/*
		 * @see org.eclipse.ui.texteditor.IMarkerUpdater#updateMarker(org.eclipse.core.resources.IMarker, org.eclipse.jface.text.IDocument, org.eclipse.jface.text.Position)
		 */
		public boolean updateMarker(IMarker marker, IDocument document, Position position) {
			if (position == null) {
				return true;
			}
			if (position.isDeleted() || position.getLength() == 0) {
				return false;
			}
			return true;
		}
	}

	/**
	 * Annotation model dealing with c marker annotations and temporary problems.
	 * Also acts as a problem requestor for its translation unit. Initially inactive. Must be explicitly
	 * activated.
	 */
	protected static class TranslationUnitAnnotationModel extends ResourceMarkerAnnotationModel implements IProblemRequestor, IProblemRequestorExtension {
		
		private static class ProblemRequestorState {
			boolean fInsideReportingSequence= false;
			List<IProblem> fReportedProblems;
		}
		
		private ThreadLocal<ProblemRequestorState> fProblemRequestorState= new ThreadLocal<ProblemRequestorState>();
		private int fStateCount= 0;
		
		private ITranslationUnit fTranslationUnit;
		private List<ProblemAnnotation> fGeneratedAnnotations;
		private IProgressMonitor fProgressMonitor;
		private boolean fIsActive= false;
		
		private ReverseMap fReverseMap= new ReverseMap();
		private List<CMarkerAnnotation> fPreviouslyOverlaid= null;
		private List<CMarkerAnnotation> fCurrentlyOverlaid= new ArrayList<CMarkerAnnotation>();
		
		
		public TranslationUnitAnnotationModel(IResource resource) {
			super(resource);
		}
		
		public void setTranslationUnit(ITranslationUnit unit)  {
			fTranslationUnit= unit;
		}
		
		@Override
		protected MarkerAnnotation createMarkerAnnotation(IMarker marker) {
			String markerType= MarkerUtilities.getMarkerType(marker);
			if (markerType != null && markerType.startsWith(CMarkerAnnotation.C_MARKER_TYPE_PREFIX)) {
				return new CMarkerAnnotation(marker);
			}
			return super.createMarkerAnnotation(marker);
		}
		
		
		/* (non-Javadoc)
		 * @see org.eclipse.ui.texteditor.AbstractMarkerAnnotationModel#createPositionFromMarker(org.eclipse.core.resources.IMarker)
		 */
		@Override
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
						IRegion lineRegion= fDocument.getLineInformation(line - 1);
						start= lineRegion.getOffset();
						end= start + lineRegion.getLength();
						if (marker.isSubtypeOf(ICModelMarker.C_MODEL_PROBLEM_MARKER)) {
							// strip leading whitespace
							while (start < end && Character.isWhitespace(fDocument.getChar(start))) {
								++start;
							}
						}
					} catch (BadLocationException x) {
					} catch (CoreException exc) {
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
		@Override
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
			ProblemRequestorState state= fProblemRequestorState.get();
			if (state == null)
				internalBeginReporting(false);
		}
		
		/*
		 * @see org.eclipse.cdt.internal.ui.text.java.IProblemRequestorExtension#beginReportingSequence()
		 */
		public void beginReportingSequence() {
			ProblemRequestorState state= fProblemRequestorState.get();
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
			if (fTranslationUnit != null) {
				ProblemRequestorState state= new ProblemRequestorState();
				state.fInsideReportingSequence= insideReportingSequence;
				state.fReportedProblems= new ArrayList<IProblem>();
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
				ProblemRequestorState state= fProblemRequestorState.get();
				if (state != null)
					state.fReportedProblems.add(problem);
			}
		}
		
		/*
		 * @see IProblemRequestor#endReporting()
		 */
		public void endReporting() {
			ProblemRequestorState state= fProblemRequestorState.get();
			if (state != null && !state.fInsideReportingSequence)
				internalEndReporting(state);
		}
		
		/*
		 * @see org.eclipse.cdt.internal.ui.text.java.IProblemRequestorExtension#endReportingSequence()
		 */
		public void endReportingSequence() {
			ProblemRequestorState state= fProblemRequestorState.get();
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
		private void reportProblems(List<IProblem> reportedProblems) {
			if (fProgressMonitor != null && fProgressMonitor.isCanceled())
				return;
			
			boolean temporaryProblemsChanged= false;
			
			synchronized (getLockObject()) {
				boolean isCanceled= false;
				
				fPreviouslyOverlaid= fCurrentlyOverlaid;
				fCurrentlyOverlaid= new ArrayList<CMarkerAnnotation>();
				
				if (fGeneratedAnnotations.size() > 0) {
					temporaryProblemsChanged= true;
					removeAnnotations(fGeneratedAnnotations, false, true);
					fGeneratedAnnotations.clear();
				}
				
				if (reportedProblems != null && reportedProblems.size() > 0) {
					Iterator<IProblem> e= reportedProblems.iterator();
					while (e.hasNext()) {
						
						if (fProgressMonitor != null && fProgressMonitor.isCanceled()) {
							isCanceled= true;
							break;
						}
						
						IProblem problem= e.next();
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
				Iterator<CMarkerAnnotation> e= fPreviouslyOverlaid.iterator();
				while (e.hasNext()) {
					CMarkerAnnotation annotation= e.next();
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
		
		private void overlayMarkers(Position position, ProblemAnnotation problemAnnotation) {
			Object value= getAnnotations(position);
			if (value instanceof List<?>) {
				List<?> list= (List<?>) value;
				for (Object element : list)
					setOverlay(element, problemAnnotation);
			} else {
				setOverlay(value, problemAnnotation);
			}
		}
		
		/**
		 * Tells this annotation model to collect temporary problems from now on.
		 */
		private void startCollectingProblems() {
			fGeneratedAnnotations= new ArrayList<ProblemAnnotation>();
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
			synchronized (getLockObject()) {
				return fReverseMap.get(position);
			}
		}
		
		/*
		 * @see AnnotationModel#addAnnotation(Annotation, Position, boolean)
		 */
		@Override
		@SuppressWarnings({ "unchecked" })
		protected void addAnnotation(Annotation annotation, Position position, boolean fireModelChanged) throws BadLocationException {
			super.addAnnotation(annotation, position, fireModelChanged);
			
			synchronized (getLockObject()) {
				Object cached= fReverseMap.get(position);
				if (cached == null) {
					fReverseMap.put(position, annotation);
				} else if (cached instanceof List) {
					List<Annotation> list= (List<Annotation>) cached;
					list.add(annotation);
				} else if (cached instanceof Annotation) {
					List<Object> list= new ArrayList<Object>(2);
					list.add(cached);
					list.add(annotation);
					fReverseMap.put(position, list);
				}
			}
		}
		
		/*
		 * @see AnnotationModel#removeAllAnnotations(boolean)
		 */
		@Override
		protected void removeAllAnnotations(boolean fireModelChanged) {
			super.removeAllAnnotations(fireModelChanged);
			synchronized (getLockObject()) {
				fReverseMap.clear();
			}
		}
		
		/*
		 * @see AnnotationModel#removeAnnotation(Annotation, boolean)
		 */
		@Override
		protected void removeAnnotation(Annotation annotation, boolean fireModelChanged) {
			Position position= getPosition(annotation);
			synchronized (getLockObject()) {
				Object cached= fReverseMap.get(position);
				if (cached instanceof List<?>) {
					List<?> list= (List<?>) cached;
					list.remove(annotation);
					if (list.size() == 1) {
						fReverseMap.put(position, list.get(0));
						list.clear();
					}
				} else if (cached instanceof Annotation) {
					fReverseMap.remove(position);
				}
			}
			super.removeAnnotation(annotation, fireModelChanged);
		}
	}

	protected static class GlobalAnnotationModelListener implements IAnnotationModelListener, IAnnotationModelListenerExtension {
		
		private ListenerList fListenerList;
		
		public GlobalAnnotationModelListener() {
			fListenerList= new ListenerList(ListenerList.IDENTITY);
		}
		
		/**
		 * @see IAnnotationModelListener#modelChanged(IAnnotationModel)
		 */
		public void modelChanged(IAnnotationModel model) {
			Object[] listeners= fListenerList.getListeners();
			for (Object listener : listeners) {
				((IAnnotationModelListener) listener).modelChanged(model);
			}
		}

		/**
		 * @see IAnnotationModelListenerExtension#modelChanged(AnnotationModelEvent)
		 */
		public void modelChanged(AnnotationModelEvent event) {
			Object[] listeners= fListenerList.getListeners();
			for (Object curr : listeners) {
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

	/**
	 * 
	 */
	public CDocumentProvider() {
		super();
		IDocumentProvider parentProvider= new ExternalSearchDocumentProvider();
		parentProvider= new ForwardingDocumentProvider(ICPartitions.C_PARTITIONING, new CDocumentSetupParticipant(), parentProvider);
		setParentDocumentProvider(parentProvider);
		fGlobalAnnotationModelListener= new GlobalAnnotationModelListener();
		fPropertyListener= new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (HANDLE_TEMPORARY_PROBLEMS.equals(event.getProperty()))
					enableHandlingTemporaryProblems();
			}
		};
		CUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(fPropertyListener);
	}

	/*
	 * @see org.eclipse.ui.editors.text.TextFileDocumentProvider#connect(java.lang.Object)
	 */
	@Override
	public void connect(Object element) throws CoreException {
		super.connect(element);
		IDocument document= getDocument(element);
		if (document instanceof IDocumentExtension3) {
			IDocumentExtension3 extension= (IDocumentExtension3) document;
			if (extension.getDocumentPartitioner(ICPartitions.C_PARTITIONING) == null)
				new CDocumentSetupParticipant().setup(document);
		}
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
		if (element == null) {
			// not in a source folder?
			ICProject cproject= CoreModel.getDefault().create(file.getProject());
			if (cproject != null) {
				String contentTypeId= CoreModel.getRegistedContentTypeId(file.getProject(), file.getName());
				if (contentTypeId != null) {
					return new TranslationUnit(cproject, file, contentTypeId);
				}
			}
		}
		return null;
	}

	/*
	 * @see org.eclipse.ui.editors.text.TextFileDocumentProvider#createEmptyFileInfo()
	 */
	@Override
	protected FileInfo createEmptyFileInfo() {
		return new TranslationUnitInfo();
	}

	/*
	 * @see org.eclipse.ui.editors.text.TextFileDocumentProvider#createAnnotationModel(org.eclipse.core.resources.IFile)
	 */
	@Override
	protected IAnnotationModel createAnnotationModel(IFile file) {
		return new TranslationUnitAnnotationModel(file);
	}

	/*
	 * @see org.eclipse.ui.editors.text.TextFileDocumentProvider#createFileInfo(java.lang.Object)
	 */
	@Override
	protected FileInfo createFileInfo(Object element) throws CoreException {
		ITranslationUnit original = null;
		if (element instanceof IFileEditorInput) {
			IFileEditorInput input = (IFileEditorInput)element;
			original = createTranslationUnit(input.getFile());
		} else if (element instanceof ITranslationUnitEditorInput) {
			ITranslationUnitEditorInput input = (ITranslationUnitEditorInput)element;
			original = input.getTranslationUnit();
		} else if (element instanceof IAdaptable) {
			IAdaptable adaptable= (IAdaptable)element;
			ILocationProvider locationProvider= (ILocationProvider)adaptable.getAdapter(ILocationProvider.class);
			if (locationProvider instanceof ILocationProviderExtension) {
				URI uri= ((ILocationProviderExtension)locationProvider).getURI(element);
				original= createTranslationUnit(uri);
			}
			if (original == null && locationProvider != null) {
				IPath location= locationProvider.getPath(element);
				original= createTranslationUnit(location);
			}
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
		tuInfo.fCopy = CDTUITools.getWorkingCopyManager().getSharedWorkingCopy(original, requestor, getProgressMonitor());

		if (tuInfo.fModel == null) {
			IPath location = original.getLocation();
			if (location != null) {
				IResource markerResource= original.getCProject().getProject();
				tuInfo.fModel= new ExternalSearchAnnotationModel(markerResource, location);
				IAnnotationModel fileBufferAnnotationModel= tuInfo.fTextFileBuffer.getAnnotationModel();
				if (fileBufferAnnotationModel != null) {
					((AnnotationModel)tuInfo.fModel).addAnnotationModel("fileBufferModel", fileBufferAnnotationModel); //$NON-NLS-1$
				}
				tuInfo.fCachedReadOnlyState= true;
			}
		}
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

	/**
	 * Try to synthesize an ITranslationUnit out of thin air.
	 * @param location  the file system location of the file in question
	 * @return a translation unit or <code>null</code>
	 */
	private ITranslationUnit createTranslationUnit(IPath location) {
		if (location == null) {
			return null;
		}
		IEditorInput input= EditorUtility.getEditorInputForLocation(location, null);
		if (input instanceof ITranslationUnitEditorInput) {
			return ((ITranslationUnitEditorInput)input).getTranslationUnit();
		}
		return null;
	}

	/**
	 * Try to synthesize an ITranslationUnit out of thin air.
	 * @param uri  the URU of the file in question
	 * @return a translation unit or <code>null</code>
	 */
	private ITranslationUnit createTranslationUnit(URI uri) {
		if (uri == null) {
			return null;
		}
		IEditorInput input= EditorUtility.getEditorInputForLocation(uri, null);
		if (input instanceof ITranslationUnitEditorInput) {
			return ((ITranslationUnitEditorInput)input).getTranslationUnit();
		}
		return null;
	}

	/*
	 * @see org.eclipse.ui.editors.text.TextFileDocumentProvider#disposeFileInfo(java.lang.Object,
	 *      org.eclipse.ui.editors.text.TextFileDocumentProvider.FileInfo)
	 */
	@Override
	protected void disposeFileInfo(Object element, FileInfo info) {
		if (info instanceof TranslationUnitInfo) {
			TranslationUnitInfo tuInfo = (TranslationUnitInfo) info;
			tuInfo.fCopy.destroy();
			if (tuInfo.fModel != null)
				tuInfo.fModel.removeAnnotationModelListener(fGlobalAnnotationModelListener);
		}
		super.disposeFileInfo(element, info);
	}

	/**
	 * Creates and returns a new sub-progress monitor for the
	 * given parent monitor.
	 *
	 * @param monitor the parent progress monitor
	 * @param ticks the number of work ticks allocated from the parent monitor
	 * @return the new sub-progress monitor
	 */
	private IProgressMonitor getSubProgressMonitor(IProgressMonitor monitor, int ticks) {
		if (monitor != null)
			return new SubProgressMonitor(monitor, ticks, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK);

		return new NullProgressMonitor();
	}

	protected void commitWorkingCopy(IProgressMonitor monitor, Object element, TranslationUnitInfo info,
			boolean overwrite) throws CoreException {
		if (monitor == null)
			monitor= new NullProgressMonitor();

		monitor.beginTask("", 100); //$NON-NLS-1$

		try {
			IDocument document= info.fTextFileBuffer.getDocument();
			IResource resource= info.fCopy.getResource();

			if (resource instanceof IFile && !resource.exists()) {
				// The underlying resource has been deleted, just recreate the file, ignore the rest
				createFileFromDocument(monitor, (IFile) resource, document);
				return;
			}

			try {
				CoreException saveActionException= null;
				try {
					performSaveActions(info.fTextFileBuffer, getSubProgressMonitor(monitor, 20));
				} catch (CoreException e) {
					saveActionException = e;
				}

				commitFileBuffer(monitor, info, overwrite);

				if (saveActionException != null) {
					throw saveActionException;
				}
			} catch (CoreException x) {
				// Inform about the failure
				fireElementStateChangeFailed(element);
				throw x;
			} catch (RuntimeException x) {
				// Inform about the failure
				fireElementStateChangeFailed(element);
				throw x;
			}
		} finally {
			monitor.done();
		}
	}

	/*
	 * @see org.eclipse.ui.editors.text.TextFileDocumentProvider#createSaveOperation(java.lang.Object, org.eclipse.jface.text.IDocument, boolean)
	 */
	@Override
	protected DocumentProviderOperation createSaveOperation(final Object element, final IDocument document,
			final boolean overwrite) throws CoreException {
		final FileInfo info= getFileInfo(element);
		if (info instanceof TranslationUnitInfo) {
			return new DocumentProviderOperation() {
				/*
				 * @see org.eclipse.ui.editors.text.TextFileDocumentProvider.DocumentProviderOperation#execute(org.eclipse.core.runtime.IProgressMonitor)
				 */
				@Override
				protected void execute(IProgressMonitor monitor) throws CoreException {
					commitWorkingCopy(monitor, element, (TranslationUnitInfo) info, overwrite);
				}
				/*
				 * @see org.eclipse.ui.editors.text.TextFileDocumentProvider.DocumentProviderOperation#getSchedulingRule()
				 */
				@Override
				public ISchedulingRule getSchedulingRule() {
					if (info.fElement instanceof IFileEditorInput) {
						IFile file= ((IFileEditorInput) info.fElement).getFile();
						return computeSchedulingRule(file);
					}
					return null;
				}
			};
		}
		return null;
	}

	/**
	 * Removes trailing whitespaces from changed lines and adds newline at the end of the file,
	 * if the last line of the file was changed.
	 * @throws BadLocationException 
	 */
	private void performSaveActions(ITextFileBuffer buffer, IProgressMonitor monitor) throws CoreException {
		if (shouldRemoveTrailingWhitespace() || shouldAddNewlineAtEof()) {
			IRegion[] changedRegions= needsChangedRegions() ?
					EditorUtility.calculateChangedLineRegions(buffer, getSubProgressMonitor(monitor, 20)) :
				    null;
			IDocument document = buffer.getDocument();
			TextEdit edit = createSaveActionEdit(document, changedRegions);
			if (edit != null) {
				try {
					IDocumentUndoManager manager= DocumentUndoManagerRegistry.getDocumentUndoManager(document);
					manager.beginCompoundChange();
					edit.apply(document);
					manager.endCompoundChange();
				} catch (MalformedTreeException e) {
					String message= e.getMessage();
					if (message == null)
						message= "MalformedTreeException"; //$NON-NLS-1$
					throw new CoreException(new Status(IStatus.ERROR, CUIPlugin.PLUGIN_ID, message, e));
				} catch (BadLocationException e) {
					String message= e.getMessage();
					if (message == null)
						message= "BadLocationException"; //$NON-NLS-1$
					throw new CoreException(new Status(IStatus.ERROR, CUIPlugin.PLUGIN_ID, message, e));
				}
			}
		}
	}

	private static boolean shouldAddNewlineAtEof() {
		return PreferenceConstants.getPreferenceStore().getBoolean(
				PreferenceConstants.ENSURE_NEWLINE_AT_EOF);
	}

	private static boolean shouldRemoveTrailingWhitespace() {
		return PreferenceConstants.getPreferenceStore().getBoolean(
				PreferenceConstants.REMOVE_TRAILING_WHITESPACE);
	}

	private static boolean isLimitedRemoveTrailingWhitespace() {
		return PreferenceConstants.getPreferenceStore().getBoolean(
				PreferenceConstants.REMOVE_TRAILING_WHITESPACE_LIMIT_TO_EDITED_LINES);
	}
	
	private static boolean needsChangedRegions() {
		return shouldRemoveTrailingWhitespace() && isLimitedRemoveTrailingWhitespace();
	}

	/**
	 * Creates a text edit for the save actions.
	 * @return a text edit, or <code>null</code> if the save actions leave the file intact.
	 */
	private TextEdit createSaveActionEdit(IDocument document, IRegion[] changedRegions) {
		TextEdit rootEdit = null;
		TextEdit lastWhitespaceEdit = null;
		try {
			if (shouldRemoveTrailingWhitespace()) {
				if (!isLimitedRemoveTrailingWhitespace()) {
					// Pretend that the whole document changed.
					changedRegions = new IRegion[] { new Region(0, document.getLength()) };
				}
				// Remove trailing whitespace from changed lines.
				for (IRegion region : changedRegions) {
					int firstLine = document.getLineOfOffset(region.getOffset());
					int lastLine = document.getLineOfOffset(region.getOffset() + region.getLength());
					for (int line = firstLine; line <= lastLine; line++) {
						IRegion lineRegion = document.getLineInformation(line);
						if (lineRegion.getLength() == 0) {
							continue;
						}
						int lineStart = lineRegion.getOffset();
						int lineEnd = lineStart + lineRegion.getLength();

						// Find the rightmost none-whitespace character
						int charPos = lineEnd - 1;
						while (charPos >= lineStart && Character.isWhitespace(document.getChar(charPos)))
							charPos--;

						charPos++;
						if (charPos < lineEnd) {
							// check partition - don't remove whitespace inside strings
							ITypedRegion partition = TextUtilities.getPartition(document, ICPartitions.C_PARTITIONING, charPos, false);
							if (!ICPartitions.C_STRING.equals(partition.getType())) {
								lastWhitespaceEdit= new DeleteEdit(charPos, lineEnd - charPos);
								if (rootEdit == null) {
									rootEdit = new MultiTextEdit();
								}
								rootEdit.addChild(lastWhitespaceEdit);
							}
						}
					}
				}
			}
			if (shouldAddNewlineAtEof()) {
				// Add newline at the end of the file.
				int endOffset = document.getLength();
				IRegion lastLineRegion = document.getLineInformationOfOffset(endOffset);
				// Insert newline at the end of the document if the last line is not empty and
				// will not become empty after removal of trailing whitespace.
				if (lastLineRegion.getLength() != 0 &&
						(lastWhitespaceEdit == null ||
						lastWhitespaceEdit.getOffset() != lastLineRegion.getOffset() ||
						lastWhitespaceEdit.getLength() != lastLineRegion.getLength())) {
					TextEdit edit = new InsertEdit(endOffset, TextUtilities.getDefaultLineDelimiter(document));
					if (rootEdit == null) {
						rootEdit = edit;
					} else {
						rootEdit.addChild(edit);
					}
				}
			}
		} catch (BadLocationException e) {
			CUIPlugin.log(e);
		}
		return rootEdit;
	}

//	private static boolean isWhitespaceRegion(IDocument document, IRegion region) throws BadLocationException {
//		int end = region.getOffset() + region.getLength();
//		for (int i = region.getOffset(); i < end; i++) {
//			if (!Character.isWhitespace(document.getChar(i))) {
//				return false;
//			}
//		}
//		return true;
//	}
	
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
		for (Iterator<?> iter= getFileInfosIterator(); iter.hasNext();) {
			FileInfo info= (FileInfo) iter.next();
			if (info.fModel instanceof IProblemRequestorExtension) {
				IProblemRequestorExtension  extension= (IProblemRequestorExtension) info.fModel;
				extension.setIsActive(enable);
			}
		}
	}

	public void addGlobalAnnotationModelListener(IAnnotationModelListener listener) {
		fGlobalAnnotationModelListener.addListener(listener);
	}

	public void removeGlobalAnnotationModelListener(IAnnotationModelListener listener) {
		fGlobalAnnotationModelListener.removeListener(listener);
	}

	public IWorkingCopy getWorkingCopy(Object element) {
		FileInfo fileInfo = getFileInfo(element);
		if (fileInfo instanceof TranslationUnitInfo) {
			TranslationUnitInfo info = (TranslationUnitInfo) fileInfo;
			return info.fCopy;
		}
		return null;
	}

	public void shutdown() {
//		CUIPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(fPropertyListener);
		Iterator<?> e = getConnectedElementsIterator();
		while (e.hasNext())
			disconnect(e.next());
	}

	public ILineTracker createLineTracker(Object element) {
		return new DefaultLineTracker();
	}
}
