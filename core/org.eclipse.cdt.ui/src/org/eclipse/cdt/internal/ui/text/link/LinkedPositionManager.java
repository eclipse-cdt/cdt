package org.eclipse.cdt.internal.ui.text.link;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.cdt.ui.CUIPlugin;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IPositionUpdater;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TypedPosition;
import org.eclipse.jface.util.Assert;


/**
 * This class manages linked positions in a document. Positions are linked
 * by type names. If positions have the same type name, they are considered
 * as <em>linked</em>.
 * 
 * The manager remains active on a document until any of the following actions
 * occurs:
 * 
 * <ul>
 *   <li>A document change is performed which would invalidate any of the
 *       above constraints.</li>
 * 
 *   <li>The method <code>uninstall()</code> is called.</li>
 * 
 *   <li>Another instance of <code>LinkedPositionManager</code> tries to
 *       gain control of the same document.
 * </ul>
 */
public class LinkedPositionManager implements IDocumentListener, IPositionUpdater {
	
	private static class PositionComparator implements Comparator {
		/*
		 * @see Comparator#compare(Object, Object)
		 */
		public int compare(Object object0, Object object1) {
			Position position0= (Position) object0;
			Position position1= (Position) object1;
			
			return position0.getOffset() - position1.getOffset();
		}
	}
	
	private class Replace implements IDocumentExtension.IReplace {
		
		private Position fReplacePosition;
		private int fReplaceDeltaOffset;
		private int fReplaceLength;
		private String fReplaceText;
		
		public Replace(Position position, int deltaOffset, int length, String text) {
			fReplacePosition= position;
			fReplaceDeltaOffset= deltaOffset;
			fReplaceLength= length;
			fReplaceText= text;
		}
				
		public void perform(IDocument document, IDocumentListener owner) {
			document.removeDocumentListener(owner);
			try {
				document.replace(fReplacePosition.getOffset() + fReplaceDeltaOffset, fReplaceLength, fReplaceText);
			} catch (BadLocationException e) {
				CUIPlugin.getDefault().log(e);
				// TBD
			}
			document.addDocumentListener(owner);
		}
	}

	private static final String LINKED_POSITION= "LinkedPositionManager.linked.position"; //$NON-NLS-1$
	private static final Comparator fgPositionComparator= new PositionComparator();
	private static final Map fgActiveManagers= new HashMap();
		
	private IDocument fDocument;
	
	private LinkedPositionListener fListener;

	/**
	 * Creates a <code>LinkedPositionManager</code> for a <code>IDocument</code>.
	 * 
	 * @param document the document to use with linked positions.
	 */
	public LinkedPositionManager(IDocument document) {
		Assert.isNotNull(document);
		
		fDocument= document;		
		install();
	}

	/**
	 * Sets a listener to notify changes of current linked position.
	 */
	public void setLinkedPositionListener(LinkedPositionListener listener) {
		fListener= listener;	
	}
	
	/**
	 * Adds a linked position to the manager.
	 * There are the following constraints for linked positions:
	 * 
	 * <ul>
	 *   <li>Any two positions have spacing of at least one character.
	 *       This implies that two positions must not overlap.</li>
	 *
	 *   <li>The string at any position must not contain line delimiters.</li>
	 * </ul>
	 * 
	 * @param offset the offset of the position.
	 * @param length the length of the position.
	 */
	public void addPosition(int offset, int length) throws BadLocationException {
		Position[] positions= getPositions(fDocument);

		if (positions != null) {
			for (int i = 0; i < positions.length; i++)
				if (collides(positions[i], offset, length))
					throw new BadLocationException(LinkedPositionMessages.getString(("LinkedPositionManager.error.position.collision"))); //$NON-NLS-1$
		}
		
		String type= fDocument.get(offset, length);		

		if (containsLineDelimiters(type))
			throw new BadLocationException(LinkedPositionMessages.getString(("LinkedPositionManager.error.contains.line.delimiters"))); //$NON-NLS-1$

		try {
			fDocument.addPosition(LINKED_POSITION, new TypedPosition(offset, length, type));
		} catch (BadPositionCategoryException e) {
			CUIPlugin.getDefault().log(e);
			Assert.isTrue(false);
		}
	}

	/**
	 * Tests if a manager is already active for a document.
	 */
	public static boolean hasActiveManager(IDocument document) {
		return fgActiveManagers.get(document) != null;
	}

	private void install() {
		LinkedPositionManager manager= (LinkedPositionManager) fgActiveManagers.get(fDocument);
		if (manager != null)
			manager.leave(true);		

		fgActiveManagers.put(fDocument, this);
		
		fDocument.addPositionCategory(LINKED_POSITION);
		fDocument.addPositionUpdater(this);		
		fDocument.addDocumentListener(this);
	}	
	
	/**
	 * Leaves the linked mode. If unsuccessful, the linked positions
	 * are restored to the values at the time they were added.
	 */
	public void uninstall(boolean success) {			
		fDocument.removeDocumentListener(this);

		try {
			Position[] positions= getPositions(fDocument);	
			if ((!success) && (positions != null)) {
				// restore
				for (int i= 0; i != positions.length; i++) {
					TypedPosition position= (TypedPosition) positions[i];				
					fDocument.replace(position.getOffset(), position.getLength(), position.getType());
				}
			}		
			
			fDocument.removePositionCategory(LINKED_POSITION);

		} catch (BadLocationException e) {
			CUIPlugin.getDefault().log(e);
			Assert.isTrue(false);

		} catch (BadPositionCategoryException e) {
			CUIPlugin.getDefault().log(e);
			Assert.isTrue(false);

		} finally {
			fDocument.removePositionUpdater(this);		
			fgActiveManagers.remove(fDocument);		
		}
	}

	/**
	 * Returns the first linked position.
	 * 
	 * @return returns <code>null</code> if no linked position exist.
	 */
	public Position getFirstPosition() {
		return getNextPosition(-1);
	}

	/**
	 * Returns the next linked position with an offset greater than <code>offset</code>.
	 * If another position with the same type and offset lower than <code>offset</code>
	 * exists, the position is skipped.
	 * 
	 * @return returns <code>null</code> if no linked position exist.
	 */
	public Position getNextPosition(int offset) {
		Position[] positions= getPositions(fDocument);
		return findNextPosition(positions, offset);
	}

	private static Position findNextPosition(Position[] positions, int offset) {
		// skip already visited types
		for (int i= 0; i != positions.length; i++) {			
			if (positions[i].getOffset() > offset) {
				String type= ((TypedPosition) positions[i]).getType();
				int j;
				for (j = 0; j != i; j++)
					if (((TypedPosition) positions[j]).getType().equals(type))
						break;

				if (j == i)
					return positions[i];				
			}
		}

		return null;
	}
	
	/**
	 * Returns the position with the greatest offset smaller than <code>offset</code>.
	 *
	 * @return returns <code>null</code> if no linked position exist.
	 */
	public Position getPreviousPosition(int offset) {
		Position[] positions= getPositions(fDocument);		
		if (positions == null)
			return null;

		Position lastPosition= null;
		Position position= getFirstPosition();
		
		while ((position != null) && (position.getOffset() < offset)) {
			lastPosition= position;
			position= findNextPosition(positions, position.getOffset());
		}		
		
		return lastPosition;
	}

	private static Position[] getPositions(IDocument document) {
		try {
			Position[] positions= document.getPositions(LINKED_POSITION);
			Arrays.sort(positions, fgPositionComparator);
			return positions;

		} catch (BadPositionCategoryException e) {
			CUIPlugin.getDefault().log(e);
			Assert.isTrue(false);
		}
		
		return null;
	}	

	public static boolean includes(Position position, int offset, int length) {
		return
			(offset >= position.getOffset()) &&
			(offset + length <= position.getOffset() + position.getLength());
	}

	public static boolean excludes(Position position, int offset, int length) {
		return
			(offset + length <= position.getOffset()) ||
			(position.getOffset() + position.getLength() <= offset);
	}

	/*
	 * Collides if spacing if positions intersect each other or are adjacent.
	 */
	private static boolean collides(Position position, int offset, int length) {
		return
			(offset <= position.getOffset() + position.getLength()) &&
			(position.getOffset() <= offset + length);	
	}
	
	private void leave(boolean success) {
		uninstall(success);

		if (fListener != null)
			fListener.exit(success);		
	}

	/*
	 * @see IDocumentListener#documentAboutToBeChanged(DocumentEvent)
	 */
	public void documentAboutToBeChanged(DocumentEvent event) {
		IDocument document= event.getDocument();

		Position[] positions= getPositions(document);
		Position position= findCurrentEditablePosition(positions, event.getOffset());

		// modification outside editable position
		if (position == null) {
			position= findCurrentPosition(positions, event.getOffset());

			// modification outside any position			
			if (position == null) {
				// check for destruction of constraints (spacing of at least 1)
				if ((event.getText().length() == 0) &&
					(findCurrentPosition(positions, event.getOffset()) != null) &&
					(findCurrentPosition(positions, event.getOffset() + event.getLength()) != null))
				{
					leave(true);
				}
				
			// modification intersects non-editable position
			} else {
				leave(true);
			}

		// modification intersects editable position
		} else {
			// modificaction inside editable position
			if (includes(position, event.getOffset(), event.getLength())) {
				if (containsLineDelimiters(event.getText()))
					leave(true);

			// modificaction exceeds editable position
			} else {
				leave(true);
			}
		}
	}

	/*
	 * @see IDocumentListener#documentChanged(DocumentEvent)
	 */
	public void documentChanged(DocumentEvent event) {		
		IDocument document= event.getDocument();

		Position[] positions= getPositions(document);
		TypedPosition currentPosition= (TypedPosition) findCurrentEditablePosition(positions, event.getOffset());

		// ignore document changes (assume it won't invalidate constraints)
		if (currentPosition == null)
			return;
		
		int deltaOffset= event.getOffset() - currentPosition.getOffset();		

		if (fListener != null)
			fListener.setCurrentPosition(currentPosition, deltaOffset + event.getText().length());
		
		for (int i= 0; i != positions.length; i++) {
			TypedPosition p= (TypedPosition) positions[i];			
			
			if (p.getType().equals(currentPosition.getType()) && !p.equals(currentPosition)) {
				Replace replace= new Replace(p, deltaOffset, event.getLength(), event.getText());
				((IDocumentExtension) document).registerPostNotificationReplace(this, replace);
			}
		}
	}
	
	/*
	 * @see IPositionUpdater#update(DocumentEvent)
	 */
	public void update(DocumentEvent event) {
		int deltaLength= event.getText().length() - event.getLength();

		Position[] positions= getPositions(event.getDocument());
		TypedPosition currentPosition= (TypedPosition) findCurrentPosition(positions, event.getOffset());

		// document change outside positions
		if (currentPosition == null) {
			
			for (int i= 0; i != positions.length; i++) {
				TypedPosition position= (TypedPosition) positions[i];
				int offset= position.getOffset();
				
				if (offset >= event.getOffset())
					position.setOffset(offset + deltaLength);
			}
			
		// document change within a position
		} else {
			int length= currentPosition.getLength();
	
			for (int i= 0; i != positions.length; i++) {
				TypedPosition position= (TypedPosition) positions[i];
				int offset= position.getOffset();
				
				if (position.equals(currentPosition)) {
					position.setLength(length + deltaLength);					
				} else if (offset > currentPosition.getOffset()) {
					position.setOffset(offset + deltaLength);
				}
			}		
		}
	}

	private static Position findCurrentPosition(Position[] positions, int offset) {
		for (int i= 0; i != positions.length; i++)
			if (includes(positions[i], offset, 0))
				return positions[i];
		
		return null;			
	}

	private static Position findCurrentEditablePosition(Position[] positions, int offset) {
		Position position= positions[0];

		while ((position != null) && !includes(position, offset, 0))
			position= findNextPosition(positions, position.getOffset());

		return position;
	}

	private boolean containsLineDelimiters(String string) {
		String[] delimiters= fDocument.getLegalLineDelimiters();

		for (int i= 0; i != delimiters.length; i++)
			if (string.indexOf(delimiters[i]) != -1)
				return true;

		return false;
	}
	
	/**
	 * Test if ok to modify through UI.
	 */
	public boolean anyPositionIncludes(int offset, int length) {
		Position[] positions= getPositions(fDocument);

		Position position= findCurrentEditablePosition(positions, offset);
		if (position == null)
			return false;
		
		return includes(position, offset, length);
	}
	
}
