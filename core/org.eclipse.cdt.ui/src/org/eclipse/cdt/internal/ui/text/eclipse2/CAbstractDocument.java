package org.eclipse.cdt.internal.ui.text.eclipse2;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DefaultPositionUpdater;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.IDocumentPartitioningListener;
import org.eclipse.jface.text.ILineTracker;
import org.eclipse.jface.text.IPositionUpdater;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextStore;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TypedRegion;
import org.eclipse.jface.util.Assert;


/**
 * Abstract implementation of <code>IDocument</code>. 
 * Implements the complete contract of <code>IDocument</code>.
 * An <code>AbstractDocument</code> supports the following implementation plug-ins:
 * <ul>
 * <li> a text store for storing and managing the document's content
 * <li> a line tracker to map character positions to line numbers and vice versa
 * </ul>
 * This class must be subclassed. Subclasses must configure which implementation 
 * plug-ins the document should use. Subclasses are not intended to overwrite 
 * existing methods.
 *
 * @see IDocument
 * @see ITextStore
 * @see ILineTracker
 */
public abstract class CAbstractDocument implements IDocument, IDocumentExtension {
		
	/** The document's text store */
	private ITextStore   fStore;
	/** The document's line tracker */
	private ILineTracker fTracker;
	/** The document's partitioner */
	private IDocumentPartitioner fDocumentPartitioner;
	/** The document's partitioner casted to <code>IDocumentPartitionerExtension</code>. */
	private IDocumentPartitionerExtension fDocumentPartitionerExtension;
	/** The registered document listeners */
	private List fDocumentListeners;
	/** The registered prenotified document listeners */
	private List fPrenotifiedDocumentListeners;
	/** The registered document partitioning listeners */
	private List fDocumentPartitioningListeners;
	/** All positions managed by the document */
	private Map fPositions;
	/** All registered document position updaters */
	private List fPositionUpdaters;
	
	/** The list of post notification changes */
	private List fPostNotificationChanges;
	/** The reentrance count for post notification changes. */
	private int fReentranceCount= 0;
	/** Indicates whether post notification change processing has been stopped. */
	private int fStoppedCount= 0;
	
	/**
	 * The default constructor does not perform any configuration
	 * but leaves it to the clients who must first initialize the
	 * implementation plug-ins and then call <code>completeInitialization</code>.
	 * Results in the construction of an empty document.
	 */
	protected CAbstractDocument() {
	}
	
	
	//--- accessor to fields -------------------------------
	
	/**
	 * Returns the document's text store. Assumes that the
	 * document has been initialized with a text store.
	 *
	 * @return the document's text store
	 */
	protected ITextStore getStore() {
		Assert.isNotNull(fStore);
		return fStore;
	}
	
	/**
	 * Returns the document's line tracker. Assumes that the
	 * document has been initialized with a line tracker.
	 *
	 * @return the document's line tracker
	 */
	protected ILineTracker getTracker() {
		Assert.isNotNull(fTracker);
		return fTracker;
	}	
	
	/**
	 * Returns the document's document listeners.
	 *
	 * @return the document's document listeners
	 */
	protected List getDocumentListeners() {
		return fDocumentListeners;
	}
	
	/** 
	 * Returns the document's partitioning listeners .
	 *
	 * @return the document's partitioning listeners
	 */
	protected List getDocumentPartitioningListeners() {
		return fDocumentPartitioningListeners;
	}
	
	/**
	 * Returns all positions managed by the document grouped by category.
	 *
	 * @return the document's positions
       */
	protected Map getDocumentManagedPositions() {
		return fPositions;
	}
	
	/*
	 * @see IDocument#getDocumentPartitioner
	 */
	public IDocumentPartitioner getDocumentPartitioner() {
		return fDocumentPartitioner;
	}
	
	
	
	//--- implementation configuration interface ------------
		
	/**
	 * Sets the document's text store.
	 * Must be called inside the constructor.
	 *
	 * @param store the document's text store
	 */
	protected void setTextStore(ITextStore store) {
		fStore= store;
	}
	
	/**
	 * Sets the document's line tracker. 
	 * Must be called inside the constructor.
	 *
	 * @param tracker the document's line tracker
	 */
	protected void setLineTracker(ILineTracker tracker) {
		fTracker= tracker;
	}
		
	/*
	 * @see IDocument#setDocumentPartitioner
	 */
	public void setDocumentPartitioner(IDocumentPartitioner partitioner) {
		fDocumentPartitioner= partitioner;
		if (fDocumentPartitioner instanceof IDocumentPartitionerExtension)
			fDocumentPartitionerExtension= (IDocumentPartitionerExtension) fDocumentPartitioner;
			
		fireDocumentPartitioningChanged(new Region(0, getLength()));
	}
			
	/**
	 * Initializes document listeners, positions, and position updaters.
	 * Must be called inside the constructor after the implementation plug-ins
	 * have been set.
	 */
	protected void completeInitialization() {
		
		fPositions= new HashMap();
		fPositionUpdaters= new ArrayList();
		fDocumentListeners= new ArrayList();
		fPrenotifiedDocumentListeners= new ArrayList();
		fDocumentPartitioningListeners= new ArrayList();
		
		addPositionCategory(DEFAULT_CATEGORY);
		addPositionUpdater(new DefaultPositionUpdater(DEFAULT_CATEGORY));		
	}
	
		
	//-------------------------------------------------------
	
	/*
	 * @see IDocument#addDocumentListener
	 */
	public void addDocumentListener(IDocumentListener listener) {
		Assert.isNotNull(listener);
		if (! fDocumentListeners.contains(listener))
			fDocumentListeners.add(listener);
	}
	
	/*
	 * @see IDocument#removeDocumentListener
	 */
	public void removeDocumentListener(IDocumentListener listener) {
		Assert.isNotNull(listener);
		fDocumentListeners.remove(listener);
	}
	
	/*
	 * @see IDocument#addPrenotifiedDocumentListener(IDocumentListener) 
	 */
	public void addPrenotifiedDocumentListener(IDocumentListener listener) {
		Assert.isNotNull(listener);
		if (! fPrenotifiedDocumentListeners.contains(listener))
			fPrenotifiedDocumentListeners.add(listener);
	}
	
	/*
	 * @see IDocument#removePrenotifiedDocumentListener(IDocumentListener)
	 */
	public void removePrenotifiedDocumentListener(IDocumentListener listener) {
		Assert.isNotNull(listener);
		fPrenotifiedDocumentListeners.remove(listener);
	}
	
	/*
	 * @see IDocument#addDocumentPartitioningListener
	 */
	public void addDocumentPartitioningListener(IDocumentPartitioningListener listener) {
		Assert.isNotNull(listener);
		if (! fDocumentPartitioningListeners.contains(listener))
			fDocumentPartitioningListeners.add(listener);
	}
	
	/*
	 * @see IDocument#removeDocumentPartitioningListener
	 */
	public void removeDocumentPartitioningListener(IDocumentPartitioningListener listener) {
		Assert.isNotNull(listener);
		fDocumentPartitioningListeners.remove(listener);
	}
	
	/*
	 * @see IDocument#addPosition
	 */
	public void addPosition(String category, Position position) throws BadLocationException, BadPositionCategoryException  {
		
		if ((0 > position.offset) || (0 > position.length) || (position.offset + position.length > getLength()))
			throw new BadLocationException();
			
		if (category == null)
			throw new BadPositionCategoryException();
			
		List list= (List) fPositions.get(category);
		if (list == null)
			throw new BadPositionCategoryException();
		
		list.add(computeIndexInPositionList(list, position.offset), position);
	}
	
	/*
	 * @see IDocument#addPosition
	 */
	public void addPosition(Position position) throws BadLocationException {
		try {
			addPosition(DEFAULT_CATEGORY, position);
		} catch (BadPositionCategoryException e) {
		}
	}
	
	/*
	 * @see IDocument#addPositionCategory
	 */
	public void addPositionCategory(String category) {
		
		if (category == null)
			return;
			
		if (!containsPositionCategory(category))
			fPositions.put(category, new ArrayList());
	}
	
	/*
	 * @see IDocument#addPositionUpdater
	 */
	public void addPositionUpdater(IPositionUpdater updater) {
		insertPositionUpdater(updater, fPositionUpdaters.size());
	}
	
	/*
	 * @see IDocument#containsPosition
	 */
	public boolean containsPosition(String category, int offset, int length) {
		
		if (category == null)
			return false;
			
		List list= (List) fPositions.get(category);
		if (list == null)
			return false;
		
		int size= list.size();
		if (size == 0)
			return false;
		
		int index= computeIndexInPositionList(list, offset);
		if (index < size) {
			Position p= (Position) list.get(index);
			while (p != null && p.offset == offset) {
				if (p.length == length)
					return true;
				++ index;
				p= (index < size) ? (Position) list.get(index) : null;
			}
		}
		
		return false;
	}
	
	/*
	 * @see IDocument#containsPositionCategory
	 */
	public boolean containsPositionCategory(String category) {
		if (category != null)
			return fPositions.containsKey(category);
		return false;
	}
	
	
	/**
	 * Computes the index in the list of positions at which a position with the given
	 * offset would be inserted. The position is supposed to become the first in this list
	 * of all positions with the same offset.
	 *
	 * @param positions the list in which the index is computed
	 * @param offset the offset for which the index is computed
	 * @return the computed index
	 *
	 * @see IDocument#computeIndexInCategory(String, int)
	 */
	protected int computeIndexInPositionList(List positions, int offset) {
		
		if (positions.size() == 0)
			return 0;

		int left= 0;
		int right= positions.size() -1;
		int mid= 0;
		Position p= null;

		while (left < right) {
			
			mid= (left + right) / 2;
						
			p= (Position) positions.get(mid);
			if (offset < p.getOffset()) {
				if (left == mid)
					right= left;
				else
					right= mid -1;
			} else if (offset > p.getOffset()) {
				if (right == mid)
					left= right;
				else
					left= mid  +1;
			} else if (offset == p.getOffset()) {
				left= right= mid;
			}

		}

		int pos= left;
		p= (Position) positions.get(pos);
		if (offset > p.getOffset()) {
			// append to the end
			pos++;
		} else {
			// entry will became the first of all entries with the same offset
			do {
				--pos;
				if (pos < 0)
					break;
				p= (Position) positions.get(pos);
			} while (offset == p.getOffset());
			++pos;
		}
			
		Assert.isTrue(0 <= pos && pos <= positions.size());

		return pos;
	}

		
	/*
	 * @see IDocument#computeIndexInCategory
	 */
	public int computeIndexInCategory(String category, int offset) throws BadLocationException, BadPositionCategoryException {
		
		if (0 > offset || offset > getLength())
			throw new BadLocationException();
			
		List c= (List) fPositions.get(category);
		if (c == null)
			throw new BadPositionCategoryException();
		
		return computeIndexInPositionList(c, offset);
	}
		
	/**
	 * Fires the document partitioning changed notification to all registered 
	 * document partitioning listeners. Uses a robust iterator.
	 * @deprecated use <code>fireDocumentPartitioningChanged(IRegion)</code> instead
	 */
	protected void fireDocumentPartitioningChanged() {
		
		if (fDocumentPartitioningListeners != null && fDocumentPartitioningListeners.size() > 0) {
			
			List list= new ArrayList(fDocumentPartitioningListeners);
			Iterator e= list.iterator();
			while (e.hasNext()) {
				IDocumentPartitioningListener l= (IDocumentPartitioningListener) e.next();
				l.documentPartitioningChanged(this);
			}
		}
	}
	
	/**
	 * Fires the document partitioning changed notification to all registered 
	 * document partitioning listeners. Uses a robust iterator.
	 * 
	 * @param region the region in which partitioning has changed
	 */
	protected void fireDocumentPartitioningChanged(IRegion region) {
		
		if (fDocumentPartitioningListeners != null && fDocumentPartitioningListeners.size() > 0) {
			
			List list= new ArrayList(fDocumentPartitioningListeners);
			Iterator e= list.iterator();
			while (e.hasNext()) {
				IDocumentPartitioningListener l= (IDocumentPartitioningListener) e.next();
				if (l instanceof IDocumentPartitioningListenerExtension)
					((IDocumentPartitioningListenerExtension) l).documentPartitioningChanged(this, region);
				else
					l.documentPartitioningChanged(this);
			}
		}
	}
	
	/**
	 * Fires the given document event to all registers document listeners informing them
	 * about the forthcoming document manipulation. Uses a robust iterator.
	 *
	 * @param event the event to be sent out
	 */
	protected void fireDocumentAboutToBeChanged(DocumentEvent event) {
		
		// IDocumentExtension
		if (fReentranceCount == 0)
			flushPostNotificationChanges();
		
		if (fDocumentPartitioner != null)
			fDocumentPartitioner.documentAboutToBeChanged(event);
			
		if (fPrenotifiedDocumentListeners.size() > 0) {
			
			List list= new ArrayList(fPrenotifiedDocumentListeners);
			Iterator e= list.iterator();
			while (e.hasNext()) {
				IDocumentListener l= (IDocumentListener) e.next();
				l.documentAboutToBeChanged(event);
			}
		}
				
		if (fDocumentListeners.size() > 0) {
			
			List list= new ArrayList(fDocumentListeners);
			Iterator e= list.iterator();
			while (e.hasNext()) {
				IDocumentListener l= (IDocumentListener) e.next();
				l.documentAboutToBeChanged(event);
			}
		}
	}
	
	/**
	 * Updates document partitioning and document positions according to the 
	 * specification given by the document event.
	 *
	 * @param event the document event describing the change to which structures must be adapted
	 */
	protected void updateDocumentStructures(DocumentEvent event) {
		boolean partitioningChanged= false;
		IRegion changedRegion= null;
		
		if (fDocumentPartitioner != null) {
			if (fDocumentPartitionerExtension != null) {
				changedRegion= fDocumentPartitionerExtension.documentChanged2(event);
				partitioningChanged= (changedRegion != null);
			} else
				partitioningChanged= fDocumentPartitioner.documentChanged(event);
		}
			
		if (fPositions.size() > 0)
			updatePositions(event);
			
		if (partitioningChanged)
			fireDocumentPartitioningChanged(changedRegion);
	}
		
	/**
	 * Updates the internal document structures and informs all document listeners.
	 * Uses a robust iterator.
	 *
	 * @param event the document event to be sent out
	 */
	protected void fireDocumentChanged(DocumentEvent event) {
		updateDocumentStructures(event);
		
		if (fPrenotifiedDocumentListeners.size() > 0) {
			
			List list= new ArrayList(fPrenotifiedDocumentListeners);
			Iterator e= list.iterator();
			while (e.hasNext()) {
				IDocumentListener l= (IDocumentListener) e.next();
				l.documentChanged(event);
			}
		}
		
		if (fDocumentListeners.size() > 0) {
			
			List list= new ArrayList(fDocumentListeners);
			Iterator e= list.iterator();
			while (e.hasNext()) {
				IDocumentListener l= (IDocumentListener) e.next();
				l.documentChanged(event);
			}
		}
		
		// IDocumentExtension
		++ fReentranceCount;
		try {
			if (fReentranceCount == 1)
				executePostNotificationChanges();
		} finally {
			-- fReentranceCount;
		}
	}
	
	/*
	 * @see IDocument#getChar
	 */
	public char getChar(int pos) throws BadLocationException {
		if ((0 > pos) || (pos >= getLength()))
			throw new BadLocationException();
		return getStore().get(pos);
	}
	
	/*
	 * @see IDocument#getContentType
	 */
	public String getContentType(int offset) throws BadLocationException {
		if ((0 > offset) || (offset > getLength()))
			throw new BadLocationException();
			
		if (fDocumentPartitioner == null)
			return DEFAULT_CONTENT_TYPE;
			
		return fDocumentPartitioner.getContentType(offset);
	}
	
	/*
	 * @see IDocument#getLegalContentTypes
	 */
	public String[] getLegalContentTypes() {
		if (fDocumentPartitioner == null)
			return new String[] { DEFAULT_CONTENT_TYPE };
		return fDocumentPartitioner.getLegalContentTypes();
	}
		
	/*
	 * @see IDocument#getLength
	 */
	public int getLength() {
		return getStore().getLength();
	}
	
	/*
	 * @see IDocument#getLineDelimiter
	 */
	public String getLineDelimiter(int line) throws BadLocationException {
		return getTracker().getLineDelimiter(line);
	}
	
	/*
	 * @see IDocument#getLegalLineDelimiters
	 */
	public String[] getLegalLineDelimiters() {
		return getTracker().getLegalLineDelimiters();
	}
	
	/*
	 * @see IDocument#getLineLength
	 */
	public int getLineLength(int line) throws BadLocationException {
		return getTracker().getLineLength(line);
	}
	
	/*
	 * @see IDocument#getLineOfOffset
	 */
	public int getLineOfOffset(int pos) throws BadLocationException {
		return getTracker().getLineNumberOfOffset(pos);
	}
	
	/*
	 * @see IDocument#getLineOffset
	 */
	public int getLineOffset(int line) throws BadLocationException {
		return getTracker().getLineOffset(line);
	}
	
	/*
	 * @see IDocument#getLineInformation
	 */
	public IRegion getLineInformation(int line) throws BadLocationException {
		return getTracker().getLineInformation(line);
	}
	
	/*
	 * @see IDocument#getLineInformationOfOffset
	 */
	public IRegion getLineInformationOfOffset(int offset) throws BadLocationException {
		return getTracker().getLineInformationOfOffset(offset);
	}
	
	/*
	 * @see IDocument#getNumberOfLines
	 */
	public int getNumberOfLines() {
		return getTracker().getNumberOfLines();
	}
	
	/*
	 * @see IDocument#getNumberOfLines(int, int)
	 */
	public int getNumberOfLines(int offset, int length) throws BadLocationException {
		return getTracker().getNumberOfLines(offset, length);
	}
	
	/*
	 * @see IDocument#computeNumberOfLines(String)
	 */
	public int computeNumberOfLines(String text) {
		return getTracker().computeNumberOfLines(text);
	}
	
	/*
	 * @see IDocument#getPartition
	 */
	public ITypedRegion getPartition(int offset) throws BadLocationException {
		if ((0 > offset) || (offset > getLength()))
			throw new BadLocationException();
		
		if (fDocumentPartitioner == null)
			return new TypedRegion(0, getLength(), DEFAULT_CONTENT_TYPE);
			
		return fDocumentPartitioner.getPartition(offset);
	}
	
	/*
	 * @see IDocument#computePartitioning
	 */
	public ITypedRegion[] computePartitioning(int offset, int length) throws BadLocationException {
		if ((0 > offset) || (0 > length) || (offset + length > getLength()))
			throw new BadLocationException();
		
		if (fDocumentPartitioner == null)
			return new TypedRegion[] { new TypedRegion(offset, length, DEFAULT_CONTENT_TYPE) };
			
		return fDocumentPartitioner.computePartitioning(offset, length);
	}
	
	/*
	 * @see IDocument#getPositions
	 */
	public Position[] getPositions(String category) throws BadPositionCategoryException {
		
		if (category == null)
			throw new BadPositionCategoryException();
			
		List c= (List) fPositions.get(category);
		if (c == null)
			throw new BadPositionCategoryException();
		
		Position[] positions= new Position[c.size()];
		c.toArray(positions);
		return positions;
	}
	
	/*
	 * @see IDocument#getPositionCategories
	 */
	public String[] getPositionCategories() {
		String[] categories= new String[fPositions.size()];
		Iterator keys= fPositions.keySet().iterator();
		for (int i= 0; i < categories.length; i++)
			categories[i]= (String) keys.next();
		return categories;
	}
	
	/*
	 * @see IDocument#getPositionUpdaters
	 */
	public IPositionUpdater[] getPositionUpdaters() {
		IPositionUpdater[] updaters= new IPositionUpdater[fPositionUpdaters.size()];
		fPositionUpdaters.toArray(updaters);
		return updaters;
	}
		
	/*
	 * @see IDocument#get
	 */
	public String get() {
		return getStore().get(0, getLength());
	}
	
	/*
	 * @see IDocument#get
	 */
	public String get(int pos, int length) throws BadLocationException {
		int myLength= getLength();
		if ((0 > pos) || (0 > length) || (pos + length > myLength))
			throw new BadLocationException();
		return getStore().get(pos, length);
	}
		
	/*
	 * @see IDocument#insertPositionUpdater
	 */
	public void insertPositionUpdater(IPositionUpdater updater, int index) {

		for (int i= fPositionUpdaters.size() - 1; i >= 0; i--) {
			if (fPositionUpdaters.get(i) == updater)
				return;
		} 
		
		if (index == fPositionUpdaters.size())
			fPositionUpdaters.add(updater);
		else
			fPositionUpdaters.add(index, updater);
	}
 		
	/*
	 * @see IDocument#removePosition
	 */
	public void removePosition(String category, Position position) throws BadPositionCategoryException {
		
		if (position == null)
			return;

		if (category == null)
			throw new BadPositionCategoryException();
			
		List c= (List) fPositions.get(category);
		if (c == null)
			throw new BadPositionCategoryException();
			
		c.remove(position);
	}
	
	/*
	 * @see IDocument#removePosition
	 */
	public void removePosition(Position position) {
		try {
			removePosition(DEFAULT_CATEGORY, position);
		} catch (BadPositionCategoryException e) {
		}
	}
		
	/*
	 * @see IDocument#removePositionCategory
	 */
	public void removePositionCategory(String category) throws BadPositionCategoryException {

		if (category == null)
			return;
		
		if ( !containsPositionCategory(category))
			throw new BadPositionCategoryException();

		fPositions.remove(category);
	}
		
	/*
	 * @see IDocument#removePositionUpdater
	 */
	public void removePositionUpdater(IPositionUpdater updater) {
		for (int i= fPositionUpdaters.size() - 1; i >= 0; i--) {
			if (fPositionUpdaters.get(i) == updater) {
				fPositionUpdaters.remove(i);
				return;
			}
		} 
	}
	
	/*
	 * @see IDocument#replace
	 */
	public void replace(int pos, int length, String text) throws BadLocationException {
		if ((0 > pos) || (0 > length) || (pos + length > getLength()))
			throw new BadLocationException();
			
		DocumentEvent e= new DocumentEvent(this, pos, length, text);
		fireDocumentAboutToBeChanged(e);
				
		getStore().replace(pos, length, text);
		getTracker().replace(pos, length, text);
		 			
		fireDocumentChanged(e);
	}
		
	/*
	 * @see IDocument#set
	 */
	public void set(String text) {
		int length= getStore().getLength();
		DocumentEvent e= new DocumentEvent(this, 0, length, text);
		fireDocumentAboutToBeChanged(e);
		
		getStore().set(text);
		getTracker().set(text);
		
		fireDocumentChanged(e);
	}
		
	/**
	 * Updates all positions of all categories to the change
	 * described by the document event. All registered document
	 * updaters are called in the sequence they have been arranged.
	 * Uses a robust iterator.
	 *
	 * @param event the document event describing the change to which to adapt the positions
	 */
	protected void updatePositions(DocumentEvent event) {
		List list= new ArrayList(fPositionUpdaters);
		Iterator e= list.iterator();
		while (e.hasNext()) {
			IPositionUpdater u= (IPositionUpdater) e.next();
			u.update(event);
		}
	}
	
	/*
	 * @see IDocument#search
	 */
	public int search(int startPosition, String findString, boolean forwardSearch, boolean caseSensitive, boolean wholeWord) throws BadLocationException {
		
		if (findString == null || findString.length() == 0)
			return -1;
		
    		ITextStore store= getStore();
		
		if (startPosition < -1 || startPosition > store.getLength())
			throw new BadLocationException();
		
		if (!caseSensitive)
			findString= findString.toLowerCase();
			
		char[] fs= new char[findString.length()];
		findString.getChars(0, fs.length, fs, 0);		
		

		if (forwardSearch) {
			if (startPosition == -1)
				startPosition= 0;
			int end= getLength();
			while (startPosition < end) {				
				int pos= indexOf(store, fs, startPosition, caseSensitive);
				if (!wholeWord || pos == -1 || isWholeWord(store, pos, pos + fs.length)) {
					return pos;
				}
				startPosition= pos + 1;
			}		
		} else {
			if (startPosition == -1)
				startPosition= getLength();
			while (startPosition >= 0) {				
				int pos= lastIndexOf(store, fs, startPosition, caseSensitive);
				if (!wholeWord || pos == -1 || isWholeWord(store, pos, pos + fs.length)) {
					return pos;
				}
				startPosition= pos - 1;
			}				
		}
		return -1;
	}
	
	/*
	 * Returns the first index greater than <code>fromIndex</code> at which <code>str</code>
	 * can be found in the <code>store</code>.
	 */
	static private int indexOf(ITextStore store, char[] str, int fromIndex, boolean caseSensitive) {
		int count= store.getLength();
	    	
		if (fromIndex >= count)
		    return -1;
		
	    	if (fromIndex < 0)
	    	    fromIndex= 0;
	    	
	    	int strLen= str.length;
		if (strLen == 0)	// empty string always matches
			return fromIndex;
	
		char first= str[0];
		int i= fromIndex;
		int max= count - strLen;
	
	  restart:
		while (true) {
			
			// Look for first character
			if (caseSensitive) {
				while (i <= max && store.get(i) != first)
					i++;
			} else {
				while (i <= max && Character.toLowerCase(store.get(i)) != first)
					i++;
			}
		    
			if (i > max)
				return -1;
	
			// Found first character
			int j= i + 1;
			int end= j + strLen - 1;
			int k= 1;
			if (caseSensitive) {
				while (j < end) {
					if (store.get(j++) != str[k++]) {
						i++;
						continue restart;
					}
				}
			} else {
				while (j < end) {
					if (Character.toLowerCase(store.get(j++)) != str[k++]) {
						i++;
						continue restart;
					}
				}
			}
		    
			return i;	// Found
		}
	}
	
	/*
	 * Returns the first index smaller than <code>fromIndex</code> at which <code>str</code>
	 * can be found in the <code>store</code>.
	 */
	static private int lastIndexOf(ITextStore store, char[] str, int fromIndex, boolean caseSensitive) {
    	
		if (fromIndex < 0)
		    return -1;
		
   		int count= store.getLength();
   		int strLen= str.length;
		int rightIndex= count - strLen;
		
		if (fromIndex > rightIndex)
		    fromIndex= rightIndex;
		
		if (strLen == 0)		// empty string always matches
		    return fromIndex;
	
		int strLastIndex= strLen - 1;
		char strLastChar= str[strLastIndex];
		int min= strLen - 1;
		int i= min + fromIndex;
	
	  restart:
		while (true) {
	
		    // Look for the last character
		    if (caseSensitive) {
				while (i >= min && store.get(i) != strLastChar)
					i--;
		    } else {
				while (i >= min && Character.toLowerCase(store.get(i)) != strLastChar)
					i--;
		    }
		    		    
		    if (i < min)
				return -1;
	
		    // Found last character
		    int j= i - 1;
		    int start= j - (strLen - 1);
		    int k= strLastIndex - 1;
	
		    if (caseSensitive) {
			    while (j > start) {
			        if (store.get(j--) != str[k--]) {
				    	i--;
				    	continue restart;
					}
			    }
		    } else {
			    while (j > start) {
			        if (Character.toLowerCase(store.get(j--)) != str[k--]) {
				    	i--;
				    	continue restart;
					}
			    }
		    }
	
		    return start + 1;    /* Found whole string. */
		}
	}
	
	/*
	 * Tests if the substring is a whole word.
	 */	
	private static boolean isWholeWord(ITextStore store, int from, int to) {
		    	
		if (from > 0) {
			char ch= store.get(from-1);
			if (Character.isLetterOrDigit(ch) || ch == '_') {
				return false;
			}
		}
		if (to < store.getLength()) {
			char ch= store.get(to);
			if (Character.isLetterOrDigit(ch) || ch == '_' ) {
				return false;
			}
		}
		return true;
	}
	
	
	// ---------- implementation of IDocumentExtension --------------
	
	static private class RegisteredReplace {
		IDocumentListener fOwner;
		IDocumentExtension.IReplace fReplace;
		
		RegisteredReplace(IDocumentListener owner, IDocumentExtension.IReplace replace) {
			fOwner= owner;
			fReplace= replace;
		}
	};
	
	/**
	 * Flushs all registered post notification changes.
	 */
	private void flushPostNotificationChanges() {
		if (fPostNotificationChanges != null)
			fPostNotificationChanges.clear();
	}
	
	/**
	 * Executes all registered post notification changes. The process is
	 * repeated until no new post notification changes are added.
	 */
	private void executePostNotificationChanges() {
		
		if (fStoppedCount > 0)
			return;
			
		while (fPostNotificationChanges != null) {
			List changes= fPostNotificationChanges;
			fPostNotificationChanges= null;
			
			Iterator e= changes.iterator();
			while (e.hasNext()) {
				RegisteredReplace replace = (RegisteredReplace) e.next();
				replace.fReplace.perform(this, replace.fOwner);
			}
		}
	}
	
	/*
	 * @see IDocumentExtension#registerPostNotificationReplace(IDocumentListener, IReplace)
	 */
	public void registerPostNotificationReplace(IDocumentListener owner, IDocumentExtension.IReplace replace) {
		if (fPostNotificationChanges == null)
			fPostNotificationChanges= new ArrayList(1);
		fPostNotificationChanges.add(new RegisteredReplace(owner, replace));
	}
	
	/*
	 * @see IDocumentExtension#stopPostNotificationProcessing()
	 */
	public void stopPostNotificationProcessing() {
		++ fStoppedCount;
	}
	
	/*
	 * @see IDocumentExtension#resumePostNotificationProcessing()
	 */
	public void resumePostNotificationProcessing() {
		-- fStoppedCount;
		if (fStoppedCount == 0 && fReentranceCount == 0)
			executePostNotificationChanges();
	}
}


