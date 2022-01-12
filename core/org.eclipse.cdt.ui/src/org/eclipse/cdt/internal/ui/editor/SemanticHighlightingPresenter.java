/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
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
 *     Anton Leherbauer (Wind River Systems) - Adapted for CDT
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.editor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.internal.ui.editor.SemanticHighlightingManager.HighlightedPosition;
import org.eclipse.cdt.internal.ui.editor.SemanticHighlightingManager.HighlightingStyle;
import org.eclipse.cdt.internal.ui.text.CPresentationReconciler;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IPositionUpdater;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ISynchronizable;
import org.eclipse.jface.text.ITextInputListener;
import org.eclipse.jface.text.ITextPresentationListener;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.swt.custom.StyleRange;

/**
 * Semantic highlighting presenter - UI thread implementation.
 * Cloned from JDT.
 *
 * @since 4.0
 */
public class SemanticHighlightingPresenter implements ITextPresentationListener, ITextInputListener, IDocumentListener {
	/**
	 * Semantic highlighting position updater.
	 */
	private class HighlightingPositionUpdater implements IPositionUpdater {
		/** The position category. */
		private final String fCategory;

		/**
		 * Creates a new updater for the given <code>category</code>.
		 *
		 * @param category the new category.
		 */
		public HighlightingPositionUpdater(String category) {
			fCategory = category;
		}

		@Override
		public void update(DocumentEvent event) {
			int eventOffset = event.getOffset();
			int eventOldLength = event.getLength();
			int eventEnd = eventOffset + eventOldLength;

			try {
				Position[] positions = event.getDocument().getPositions(fCategory);

				for (int i = 0; i != positions.length; i++) {
					HighlightedPosition position = (HighlightedPosition) positions[i];

					// Also update deleted positions because they get deleted by the background
					// thread and removed/invalidated only in the UI runnable.
					//					if (position.isDeleted())
					//						continue;

					int offset = position.getOffset();
					int length = position.getLength();
					int end = offset + length;

					if (offset > eventEnd) {
						updateWithPrecedingEvent(position, event);
					} else if (end < eventOffset) {
						updateWithSucceedingEvent(position, event);
					} else if (offset <= eventOffset && end >= eventEnd) {
						updateWithIncludedEvent(position, event);
					} else if (offset <= eventOffset) {
						updateWithOverEndEvent(position, event);
					} else if (end >= eventEnd) {
						updateWithOverStartEvent(position, event);
					} else {
						updateWithIncludingEvent(position, event);
					}
				}
			} catch (BadPositionCategoryException e) {
				// ignore and return
			}
		}

		/**
		 * Updates the given position with the given event. The event precedes the position.
		 *
		 * @param position The position
		 * @param event The event
		 */
		private void updateWithPrecedingEvent(HighlightedPosition position, DocumentEvent event) {
			String newText = event.getText();
			int eventNewLength = newText != null ? newText.length() : 0;
			int deltaLength = eventNewLength - event.getLength();

			position.setOffset(position.getOffset() + deltaLength);
		}

		/**
		 * Updates the given position with the given event. The event succeeds the position.
		 *
		 * @param position The position
		 * @param event The event
		 */
		private void updateWithSucceedingEvent(HighlightedPosition position, DocumentEvent event) {
		}

		/**
		 * Updates the given position with the given event. The event is included by the position.
		 *
		 * @param position The position
		 * @param event The event
		 */
		private void updateWithIncludedEvent(HighlightedPosition position, DocumentEvent event) {
			int eventOffset = event.getOffset();
			String newText = event.getText();
			if (newText == null)
				newText = ""; //$NON-NLS-1$
			int eventNewLength = newText.length();

			int deltaLength = eventNewLength - event.getLength();

			int offset = position.getOffset();
			int length = position.getLength();
			int end = offset + length;

			int includedLength = 0;
			while (includedLength < eventNewLength && Character.isJavaIdentifierPart(newText.charAt(includedLength)))
				includedLength++;
			if (includedLength == eventNewLength) {
				position.setLength(length + deltaLength);
			} else {
				int newLeftLength = eventOffset - offset + includedLength;

				int excludedLength = eventNewLength;
				while (excludedLength > 0 && Character.isJavaIdentifierPart(newText.charAt(excludedLength - 1)))
					excludedLength--;
				int newRightOffset = eventOffset + excludedLength;
				int newRightLength = end + deltaLength - newRightOffset;

				if (newRightLength == 0) {
					position.setLength(newLeftLength);
				} else {
					if (newLeftLength == 0) {
						position.update(newRightOffset, newRightLength);
					} else {
						position.setLength(newLeftLength);
						addPositionFromUI(newRightOffset, newRightLength, position.getHighlighting());
					}
				}
			}
		}

		/**
		 * Updates the given position with the given event. The event overlaps with the end of
		 * the position.
		 *
		 * @param position The position
		 * @param event The event
		 */
		private void updateWithOverEndEvent(HighlightedPosition position, DocumentEvent event) {
			String newText = event.getText();
			if (newText == null)
				newText = ""; //$NON-NLS-1$
			int eventNewLength = newText.length();

			int includedLength = 0;
			while (includedLength < eventNewLength && Character.isJavaIdentifierPart(newText.charAt(includedLength)))
				includedLength++;
			position.setLength(event.getOffset() - position.getOffset() + includedLength);
		}

		/**
		 * Updates the given position with the given event. The event overlaps with the start of
		 * the position.
		 *
		 * @param position The position
		 * @param event The event
		 */
		private void updateWithOverStartEvent(HighlightedPosition position, DocumentEvent event) {
			int eventOffset = event.getOffset();
			int eventEnd = eventOffset + event.getLength();

			String newText = event.getText();
			if (newText == null)
				newText = ""; //$NON-NLS-1$
			int eventNewLength = newText.length();

			int excludedLength = eventNewLength;
			while (excludedLength > 0 && Character.isJavaIdentifierPart(newText.charAt(excludedLength - 1)))
				excludedLength--;
			int deleted = eventEnd - position.getOffset();
			int inserted = eventNewLength - excludedLength;
			position.update(eventOffset + excludedLength, position.getLength() - deleted + inserted);
		}

		/**
		 * Update the given position with the given event. The event includes the position.
		 *
		 * @param position The position
		 * @param event The event
		 */
		private void updateWithIncludingEvent(HighlightedPosition position, DocumentEvent event) {
			position.delete();
			position.update(event.getOffset(), 0);
		}
	}

	/** Position updater */
	private IPositionUpdater fPositionUpdater = new HighlightingPositionUpdater(getPositionCategory());

	/** The source viewer this semantic highlighting reconciler is installed on */
	private CSourceViewer fSourceViewer;
	/** The background presentation reconciler */
	private CPresentationReconciler fPresentationReconciler;

	/** UI's current highlighted positions - can contain <code>null</code> elements */
	private List<HighlightedPosition> fPositions = new ArrayList<>();
	/** UI position lock */
	private Object fPositionLock = new Object();

	/** <code>true</code> iff the current reconcile is canceled. */
	private boolean fIsCanceled = false;

	/**
	 * Creates and returns a new highlighted position with the given offset, length and highlighting.
	 * <p>
	 * NOTE: Also called from background thread.
	 * </p>
	 *
	 * @param offset The offset
	 * @param length The length
	 * @param style The highlighting
	 * @return The new highlighted position
	 */
	public HighlightedPosition createHighlightedPosition(int offset, int length, HighlightingStyle style) {
		// TODO: reuse deleted positions
		return new HighlightedPosition(offset, length, style, fPositionUpdater);
	}

	/**
	 * Adds all current positions to the given list.
	 * <p>
	 * NOTE: Called from background thread.
	 * </p>
	 *
	 * @param list The list
	 */
	public void addAllPositions(List<? super HighlightedPosition> list) {
		synchronized (fPositionLock) {
			list.addAll(fPositions);
		}
	}

	/**
	 * Create a text presentation in the background.
	 * <p>
	 * NOTE: Called from background thread.
	 * </p>
	 *
	 * @param addedPositions the added positions
	 * @param removedPositions the removed positions
	 * @return the text presentation or <code>null</code>, if reconciliation should be canceled
	 */
	public TextPresentation createPresentation(List<? extends Position> addedPositions,
			List<? extends Position> removedPositions) {
		CSourceViewer sourceViewer = fSourceViewer;
		CPresentationReconciler presentationReconciler = fPresentationReconciler;
		if (sourceViewer == null || presentationReconciler == null)
			return null;

		if (isCanceled())
			return null;

		IDocument document = sourceViewer.getDocument();
		if (document == null)
			return null;

		int minStart = Integer.MAX_VALUE;
		int maxEnd = Integer.MIN_VALUE;
		for (int i = 0, n = removedPositions.size(); i < n; i++) {
			Position position = removedPositions.get(i);
			int offset = position.getOffset();
			minStart = Math.min(minStart, offset);
			maxEnd = Math.max(maxEnd, offset + position.getLength());
		}
		for (int i = 0, n = addedPositions.size(); i < n; i++) {
			Position position = addedPositions.get(i);
			int offset = position.getOffset();
			minStart = Math.min(minStart, offset);
			maxEnd = Math.max(maxEnd, offset + position.getLength());
		}

		if (minStart < maxEnd)
			try {
				return presentationReconciler.createRepairDescription(new Region(minStart, maxEnd - minStart),
						document);
			} catch (RuntimeException e) {
				// Assume concurrent modification from UI thread
			}

		return null;
	}

	/**
	 * Create a runnable for updating the presentation.
	 * <p>
	 * NOTE: Called from background thread.
	 * </p>
	 * @param textPresentation the text presentation
	 * @param addedPositions the added positions
	 * @param removedPositions the removed positions
	 * @return the runnable or <code>null</code>, if reconciliation should be canceled
	 */
	public Runnable createUpdateRunnable(final TextPresentation textPresentation,
			List<HighlightedPosition> addedPositions, List<HighlightedPosition> removedPositions) {
		if (fSourceViewer == null || textPresentation == null)
			return null;

		// TODO: do clustering of positions and post multiple fast runnables
		final HighlightedPosition[] added = addedPositions.toArray(new HighlightedPosition[addedPositions.size()]);
		final HighlightedPosition[] removed = removedPositions
				.toArray(new HighlightedPosition[removedPositions.size()]);

		if (isCanceled())
			return null;

		Runnable runnable = () -> updatePresentation(textPresentation, added, removed);
		return runnable;
	}

	/**
	 * Invalidates the presentation of the positions based on the given added positions and
	 * the existing deleted positions. Also unregisters the deleted positions from the document
	 * and patches the positions of this presenter.
	 * <p>
	 * NOTE: Indirectly called from background thread by UI runnable.
	 * </p>
	 * @param textPresentation the text presentation or <code>null</code>, if the presentation
	 *     should computed in the UI thread
	 * @param addedPositions the added positions
	 * @param removedPositions the removed positions
	 */
	public void updatePresentation(TextPresentation textPresentation, HighlightedPosition[] addedPositions,
			HighlightedPosition[] removedPositions) {
		if (fSourceViewer == null)
			return;

		//		checkOrdering("added positions: ", Arrays.asList(addedPositions)); //$NON-NLS-1$
		//		checkOrdering("removed positions: ", Arrays.asList(removedPositions)); //$NON-NLS-1$
		//		checkOrdering("old positions: ", fPositions); //$NON-NLS-1$

		// TODO: double-check consistency with document.getPositions(...)
		// TODO: reuse removed positions
		if (isCanceled())
			return;

		IDocument document = fSourceViewer.getDocument();
		if (document == null)
			return;

		String positionCategory = getPositionCategory();

		List<HighlightedPosition> removedPositionsList = Arrays.asList(removedPositions);

		try {
			synchronized (fPositionLock) {
				List<HighlightedPosition> oldPositions = fPositions;
				int newSize = Math.max(fPositions.size() + addedPositions.length - removedPositions.length, 10);

				/*
				 * The following loop is a kind of merge sort: it merges two List<Position>, each
				 * sorted by position.offset, into one new list. The first of the two is the
				 * previous list of positions (oldPositions), from which any deleted positions get
				 * removed on the fly. The second of two is the list of added positions. The result
				 * is stored in newPositions.
				 */
				List<HighlightedPosition> newPositions = new ArrayList<>(newSize);
				HighlightedPosition position = null;
				HighlightedPosition addedPosition = null;
				for (int i = 0, j = 0, n = oldPositions.size(), m = addedPositions.length; i < n || position != null
						|| j < m || addedPosition != null;) {
					// loop variant: i + j < old(i + j)

					// a) find the next non-deleted Position from the old list
					while (position == null && i < n) {
						position = oldPositions.get(i++);
						if (position.isDeleted() || contain(removedPositionsList, position)) {
							document.removePosition(positionCategory, position);
							position = null;
						}
					}

					// b) find the next Position from the added list
					if (addedPosition == null && j < m) {
						addedPosition = addedPositions[j++];
						document.addPosition(positionCategory, addedPosition);
					}

					// c) merge: add the next of position/addedPosition with the lower offset
					if (position != null) {
						if (addedPosition != null)
							if (position.getOffset() <= addedPosition.getOffset()) {
								newPositions.add(position);
								position = null;
							} else {
								newPositions.add(addedPosition);
								addedPosition = null;
							}
						else {
							newPositions.add(position);
							position = null;
						}
					} else if (addedPosition != null) {
						newPositions.add(addedPosition);
						addedPosition = null;
					}
				}
				fPositions = newPositions;
			}
		} catch (BadPositionCategoryException e) {
			// Should not happen
			CUIPlugin.log(e);
		} catch (BadLocationException e) {
			// Should not happen
			CUIPlugin.log(e);
		}
		//		checkOrdering("new positions: ", fPositions); //$NON-NLS-1$

		if (textPresentation != null)
			fSourceViewer.changeTextPresentation(textPresentation, false);
		else
			fSourceViewer.invalidateTextPresentation();
	}

	//	private void checkOrdering(String s, List positions) {
	//		Position previous= null;
	//		for (int i= 0, n= positions.size(); i < n; i++) {
	//			Position current= (Position) positions.get(i);
	//			if (previous != null && previous.getOffset() + previous.getLength() > current.getOffset())
	//				return;
	//		}
	//	}

	/**
	 * Returns <code>true</code> iff the positions contain the position.
	 * @param positions the positions, must be ordered by offset but may overlap
	 * @param position the position
	 * @return <code>true</code> iff the positions contain the position
	 */
	private boolean contain(List<? extends Position> positions, Position position) {
		return indexOf(positions, position) != -1;
	}

	/**
	 * Returns index of the position in the positions, <code>-1</code> if not found.
	 * @param positions the positions, must be ordered by offset but may overlap
	 * @param position the position
	 * @return the index
	 */
	private int indexOf(List<? extends Position> positions, Position position) {
		int index = computeIndexAtOffset(positions, position.getOffset());
		int size = positions.size();
		while (index < size) {
			if (positions.get(index) == position)
				return index;
			index++;
		}
		return -1;
	}

	/**
	 * Inserts the given position in <code>fPositions</code>, s.t. the offsets remain in linear order.
	 *
	 * @param position The position for insertion
	 */
	private void insertPosition(HighlightedPosition position) {
		int i = computeIndexAfterOffset(fPositions, position.getOffset());
		fPositions.add(i, position);
	}

	/**
	 * Returns the index of the first position with an offset greater than the given offset.
	 *
	 * @param positions the positions, must be ordered by offset and must not overlap
	 * @param offset the offset
	 * @return the index of the last position with an offset greater than the given offset
	 */
	private int computeIndexAfterOffset(List<? extends Position> positions, int offset) {
		int i = -1;
		int j = positions.size();
		while (j - i > 1) {
			int k = (i + j) >> 1;
			Position position = positions.get(k);
			if (position.getOffset() > offset)
				j = k;
			else
				i = k;
		}
		return j;
	}

	/**
	 * Returns the index of the first position with an offset equal or greater than the given offset.
	 *
	 * @param positions the positions, must be ordered by offset and must not overlap
	 * @param offset the offset
	 * @return the index of the last position with an offset equal or greater than the given offset
	 */
	private int computeIndexAtOffset(List<? extends Position> positions, int offset) {
		int i = -1;
		int j = positions.size();
		while (j - i > 1) {
			int k = (i + j) >> 1;
			Position position = positions.get(k);
			if (position.getOffset() >= offset)
				j = k;
			else
				i = k;
		}
		return j;
	}

	@Override
	public void applyTextPresentation(TextPresentation textPresentation) {
		IRegion region = textPresentation.getExtent();
		int i = computeIndexAtOffset(fPositions, region.getOffset());
		int n = computeIndexAtOffset(fPositions, region.getOffset() + region.getLength());
		if (n - i > 2) {
			List<StyleRange> ranges = new ArrayList<>(n - i);
			for (; i < n; i++) {
				HighlightedPosition position = fPositions.get(i);
				if (!position.isDeleted())
					ranges.add(position.createStyleRange());
			}
			StyleRange[] array = new StyleRange[ranges.size()];
			array = ranges.toArray(array);
			textPresentation.replaceStyleRanges(array);
		} else {
			for (; i < n; i++) {
				HighlightedPosition position = fPositions.get(i);
				if (!position.isDeleted())
					textPresentation.replaceStyleRange(position.createStyleRange());
			}
		}
	}

	@Override
	public void inputDocumentAboutToBeChanged(IDocument oldInput, IDocument newInput) {
		setCanceled(true);
		releaseDocument(oldInput);
		resetState();
	}

	@Override
	public void inputDocumentChanged(IDocument oldInput, IDocument newInput) {
		manageDocument(newInput);
	}

	@Override
	public void documentAboutToBeChanged(DocumentEvent event) {
		setCanceled(true);
	}

	@Override
	public void documentChanged(DocumentEvent event) {
	}

	/**
	 * @return Returns <code>true</code> iff the current reconcile is canceled.
	 * <p>
	 * NOTE: Also called from background thread.
	 * </p>
	 */
	public boolean isCanceled() {
		IDocument document = fSourceViewer != null ? fSourceViewer.getDocument() : null;
		if (document == null)
			return fIsCanceled;

		synchronized (getLockObject(document)) {
			return fIsCanceled;
		}
	}

	/**
	 * Set whether or not the current reconcile is canceled.
	 *
	 * @param isCanceled <code>true</code> iff the current reconcile is canceled
	 */
	public void setCanceled(boolean isCanceled) {
		IDocument document = fSourceViewer != null ? fSourceViewer.getDocument() : null;
		if (document == null) {
			fIsCanceled = isCanceled;
			return;
		}

		synchronized (getLockObject(document)) {
			fIsCanceled = isCanceled;
		}
	}

	/**
	 * @param document the document
	 * @return the document's lock object
	 */
	private Object getLockObject(IDocument document) {
		if (document instanceof ISynchronizable) {
			Object lock = ((ISynchronizable) document).getLockObject();
			if (lock != null)
				return lock;
		}
		return document;
	}

	/**
	 * Install this presenter on the given source viewer and background presentation
	 * reconciler.
	 *
	 * @param sourceViewer the source viewer
	 * @param backgroundPresentationReconciler the background presentation reconciler,
	 * 	can be <code>null</code>, in that case {@link SemanticHighlightingPresenter#createPresentation(List, List)}
	 * 	should not be called
	 */
	public void install(CSourceViewer sourceViewer, CPresentationReconciler backgroundPresentationReconciler) {
		fSourceViewer = sourceViewer;
		fPresentationReconciler = backgroundPresentationReconciler;

		fSourceViewer.prependTextPresentationListener(this);
		fSourceViewer.addTextInputListener(this);
		manageDocument(fSourceViewer.getDocument());
	}

	/**
	 * Uninstalls this presenter.
	 */
	public void uninstall() {
		setCanceled(true);

		if (fSourceViewer != null) {
			fSourceViewer.removeTextPresentationListener(this);
			releaseDocument(fSourceViewer.getDocument());
			invalidateTextPresentation();
			resetState();

			fSourceViewer.removeTextInputListener(this);
			fSourceViewer = null;
		}
	}

	/**
	 * Invalidate text presentation of positions with the given highlighting.
	 *
	 * @param highlighting The highlighting
	 */
	public void highlightingStyleChanged(HighlightingStyle highlighting) {
		for (int i = 0, n = fPositions.size(); i < n; i++) {
			HighlightedPosition position = fPositions.get(i);
			if (position.getHighlighting() == highlighting)
				fSourceViewer.invalidateTextPresentation(position.getOffset(), position.getLength());
		}
	}

	/**
	 * Invalidate text presentation of all positions.
	 */
	private void invalidateTextPresentation() {
		if (fPositions.size() > 1000) {
			fSourceViewer.invalidateTextPresentation();
		} else {
			for (int i = 0, n = fPositions.size(); i < n; i++) {
				Position position = fPositions.get(i);
				fSourceViewer.invalidateTextPresentation(position.getOffset(), position.getLength());
			}
		}
	}

	/**
	 * Add a position with the given range and highlighting unconditionally, only from UI thread.
	 * The position will also be registered on the document. The text presentation is not invalidated.
	 *
	 * @param offset The range offset
	 * @param length The range length
	 * @param highlighting
	 */
	private void addPositionFromUI(int offset, int length, HighlightingStyle highlighting) {
		HighlightedPosition position = createHighlightedPosition(offset, length, highlighting);
		synchronized (fPositionLock) {
			insertPosition(position);
		}

		IDocument document = fSourceViewer.getDocument();
		if (document == null)
			return;
		String positionCategory = getPositionCategory();
		try {
			document.addPosition(positionCategory, position);
		} catch (BadLocationException e) {
			// Should not happen
			CUIPlugin.log(e);
		} catch (BadPositionCategoryException e) {
			// Should not happen
			CUIPlugin.log(e);
		}
	}

	/**
	 * Reset to initial state.
	 */
	private void resetState() {
		synchronized (fPositionLock) {
			fPositions.clear();
		}
	}

	/**
	 * Start managing the given document.
	 *
	 * @param document The document
	 */
	private void manageDocument(IDocument document) {
		if (document != null) {
			document.addPositionCategory(getPositionCategory());
			document.addPositionUpdater(fPositionUpdater);
			document.addDocumentListener(this);
		}
	}

	/**
	 * Stop managing the given document.
	 *
	 * @param document The document
	 */
	private void releaseDocument(IDocument document) {
		if (document != null) {
			document.removeDocumentListener(this);
			document.removePositionUpdater(fPositionUpdater);
			try {
				document.removePositionCategory(getPositionCategory());
			} catch (BadPositionCategoryException e) {
				// Should not happen
				CUIPlugin.log(e);
			}
		}
	}

	/**
	 * @return The semantic reconciler position's category.
	 */
	private String getPositionCategory() {
		return toString();
	}
}
