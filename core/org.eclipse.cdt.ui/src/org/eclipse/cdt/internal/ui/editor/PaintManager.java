package org.eclipse.cdt.internal.ui.editor;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.widgets.Control;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DefaultPositionUpdater;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IPositionUpdater;
import org.eclipse.jface.text.ITextInputListener;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;


public final class PaintManager implements KeyListener, MouseListener, ISelectionChangedListener, ITextListener, ITextInputListener {		
					
	static class PositionManager implements IPositionManager {
		
		private IDocument fDocument;
		private IPositionUpdater fPositionUpdater;
		private String fCategory;
		
		public PositionManager() {
			fCategory= getClass().getName() + hashCode();
			fPositionUpdater= new DefaultPositionUpdater(fCategory);
		}
		
		public void install(IDocument document) {
			fDocument= document;
			fDocument.addPositionCategory(fCategory);
			fDocument.addPositionUpdater(fPositionUpdater);
		}
		
		public void dispose() {
			uninstall(fDocument);
		}
		
		public void uninstall(IDocument document) {
			if (document == fDocument && document != null) {
				try {
					fDocument.removePositionUpdater(fPositionUpdater);
					fDocument.removePositionCategory(fCategory);			
				} catch (BadPositionCategoryException x) {
					// should not happen
				}
				fDocument= null;
			}
		}
		
		/*
		 * @see IPositionManager#addManagedPosition(Position)
		 */
		public void addManagedPosition(Position position) {
			try {
				fDocument.addPosition(fCategory, position);
			} catch (BadPositionCategoryException x) {
				// should not happen
			} catch (BadLocationException x) {
				// should not happen
			}
		}
		
		/*
		 * @see IPositionManager#removeManagedPosition(Position)
		 */
		public void removeManagedPosition(Position position) {
			try {
				fDocument.removePosition(fCategory, position);
			} catch (BadPositionCategoryException x) {
				// should not happen
			}
		}
	};
	
	
	private List fPainters= new ArrayList(2);
	private PositionManager fManager;
	private ISourceViewer fSourceViewer;
	private boolean fTextChanged= false;
	private boolean fAutoRepeat= false;
	
	
	public PaintManager(ISourceViewer sourceViewer) {
		fSourceViewer= sourceViewer;
	}
	
	public void addPainter(IPainter painter) {
		if (!fPainters.contains(painter)) {
			fPainters.add(painter);
			if (fPainters.size() == 1)
				install();
			painter.setPositionManager(fManager);
			painter.paint(IPainter.INTERNAL);
		}
	}
	
	public void removePainter(IPainter painter) {
		if (fPainters.remove(painter))
			painter.setPositionManager(null);
		if (fPainters.size() == 0)
			dispose();
	}
	
	private void install() {
		
		fManager= new PositionManager();
		fManager.install(fSourceViewer.getDocument());
		
		fSourceViewer.addTextInputListener(this);
		
		ISelectionProvider provider= fSourceViewer.getSelectionProvider();
		provider.addSelectionChangedListener(this);
		
		fSourceViewer.addTextListener(this);
		
		StyledText text= fSourceViewer.getTextWidget();
		text.addKeyListener(this);
		text.addMouseListener(this);
	}
	
	public void dispose() {
		
		if (fManager != null) {
			fManager.dispose();
			fManager= null;
		}
		
		for (Iterator e = fPainters.iterator(); e.hasNext();)
			((IPainter) e.next()).dispose();	
		fPainters.clear();
		
		fSourceViewer.removeTextInputListener(this);
		
		ISelectionProvider provider= fSourceViewer.getSelectionProvider();
		if (provider != null)
			provider.removeSelectionChangedListener(this);
		
		fSourceViewer.removeTextListener(this);
		
		StyledText text= fSourceViewer.getTextWidget();
		if (text != null && !text.isDisposed()) {
			text.removeKeyListener(this);
			text.removeMouseListener(this);
		}
	}
	
	private void paint(int reason) {
		for (Iterator e = fPainters.iterator(); e.hasNext();)
			((IPainter) e.next()).paint(reason);
	}
	
	/*
	 * @see KeyListener#keyPressed(KeyEvent)
	 */
	public void keyPressed(KeyEvent e) {
		// This leaves artifacts when scrolling
		//if (fAutoRepeat)
			paint(IPainter.KEY_STROKE);
		
		fTextChanged= false;
		fAutoRepeat= true;
	}

	/*
	 * @see KeyListener#keyReleased(KeyEvent)
	 */
	public void keyReleased(KeyEvent e) {
		fAutoRepeat= false;
		if (!fTextChanged)
			paint(IPainter.KEY_STROKE);
	}

	/*
	 * @see MouseListener#mouseDoubleClick(MouseEvent)
	 */
	public void mouseDoubleClick(MouseEvent e) {
	}
	
	/*
	 * @see MouseListener#mouseDown(MouseEvent)
	 */
	public void mouseDown(MouseEvent e) {
	}
	
	/*
	 * @see MouseListener#mouseUp(MouseEvent)
	 */
	public void mouseUp(MouseEvent e) {
		paint(IPainter.MOUSE_BUTTON);
	}
	
	/*
	 * @see ISelectionChangedListener#selectionChanged(SelectionChangedEvent)
	 */
	public void selectionChanged(SelectionChangedEvent event) {
		paint(IPainter.SELECTION);
	}
	
	/*
	 * @see ITextListener#textChanged(TextEvent)
	 */
	public void textChanged(TextEvent event) {
		fTextChanged= true;
		Control control= fSourceViewer.getTextWidget();
		if (control != null) {
			control.getDisplay().asyncExec(new Runnable() {
				public void run() {
					if (fTextChanged && fSourceViewer != null) 
						paint(IPainter.TEXT_CHANGE);
				}
			});
		}
	}
	
	/*
	 * @see ITextInputListener#inputDocumentAboutToBeChanged(IDocument, IDocument)
	 */
	public void inputDocumentAboutToBeChanged(IDocument oldInput, IDocument newInput) {
		if (oldInput != null) {
			for (Iterator e = fPainters.iterator(); e.hasNext();)
				((IPainter) e.next()).deactivate(false);				
			fManager.uninstall(oldInput);
		}
	}
	
	/*
	 * @see ITextInputListener#inputDocumentChanged(IDocument, IDocument)
	 */
	public void inputDocumentChanged(IDocument oldInput, IDocument newInput) {
		if (newInput != null) {
			fManager.install(newInput);
			paint(IPainter.TEXT_CHANGE);
		}
	}
}



