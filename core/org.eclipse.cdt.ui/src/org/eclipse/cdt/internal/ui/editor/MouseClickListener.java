/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
/*
 * Created on Mar 5, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.eclipse.cdt.internal.ui.editor;

import java.util.Iterator;
import java.util.Set;

import org.eclipse.cdt.core.ICLogConstants;
import org.eclipse.cdt.core.parser.KeywordSetKey;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.internal.core.parser.token.KeywordSets;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewerExtension2;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;


class MouseClickListener
implements
MouseListener,
KeyListener,
MouseMoveListener,
FocusListener,
PaintListener,
IPropertyChangeListener{
	
	private final CEditor fEditor;

	private ISourceViewer fViewer;

	private IPreferenceStore fPrefStore;

	private IAction fOpenDeclAction;

	/**
	 * @param editor
	 * @param viewer
	 * @param store
	 */
	public MouseClickListener(CEditor editor, ISourceViewer viewer, IPreferenceStore store, IAction openDeclAction) {
		this.fEditor = editor;
		this.fViewer = viewer;
		this.fPrefStore = store;
		this.fOpenDeclAction = openDeclAction;
		this.fgKeywords = KeywordSets.getKeywords(KeywordSetKey.ALL,ParserLanguage.CPP);
	}

	/** The session is active. */
	private boolean fActive;

	/** The currently active style range. */
	private IRegion fActiveRegion;
	/** The currently active style range as position. */
	private Position fRememberedPosition;
	/** The hand cursor. */
	private Cursor fCursor;
	/** The link color. */
	private Color fColor;
	/** The key modifier mask. */
	private int fKeyModifierMask;
	/** The key modifier mask. */
	private boolean fIncludeMode;
	
	//TODO: Replace Keywords
	//Temp. Keywords: Once the selection parser is complete, we can use
	//it to determine if a word can be underlined
	
	private  Set fgKeywords;
	
	public void deactivate() {
		deactivate(false);
	}

	public void deactivate(boolean redrawAll) {
		if (!fActive)
			return;

		repairRepresentation(redrawAll);			
		fActive= false;
		fIncludeMode = false;
	}
	
	private void repairRepresentation(boolean redrawAll) {			

		if (fActiveRegion == null)
			return;
		
		ISourceViewer viewer= fViewer;
		if (viewer != null) {
			resetCursor(viewer);

			int offset= fActiveRegion.getOffset();
			int length= fActiveRegion.getLength();

			// remove style
			if (!redrawAll && viewer instanceof ITextViewerExtension2)
				((ITextViewerExtension2) viewer).invalidateTextPresentation(offset, length);
			else
				viewer.invalidateTextPresentation();

			// remove underline				
			if (viewer instanceof ITextViewerExtension5) {
				ITextViewerExtension5 extension= (ITextViewerExtension5) viewer;
				offset= extension.modelOffset2WidgetOffset(offset);
			} else {
				offset -= viewer.getVisibleRegion().getOffset();
			}
			
			StyledText text= viewer.getTextWidget();
			try {
				text.redrawRange(offset, length, true);
			} catch (IllegalArgumentException x) {
				org.eclipse.cdt.internal.core.model.Util.log(x, "Error in CEditor.MouseClickListener.repairRepresentation", ICLogConstants.CDT); //$NON-NLS-1$
			}
		}
		
		fActiveRegion= null;
	}
	
	private void activateCursor(ISourceViewer viewer) {
		StyledText text= viewer.getTextWidget();
		if (text == null || text.isDisposed())
			return;
		Display display= text.getDisplay();
		if (fCursor == null)
			fCursor= new Cursor(display, SWT.CURSOR_HAND);
		text.setCursor(fCursor);
	}
	
	private void resetCursor(ISourceViewer viewer) {
		StyledText text= viewer.getTextWidget();
		if (text != null && !text.isDisposed())
			text.setCursor(null);
		
		if (fCursor != null) {
			fCursor.dispose();
			fCursor= null;
		}
	}
	
	public void install() {

		ISourceViewer sourceViewer= fViewer;
		if (sourceViewer == null)
			return;
		
		StyledText text= sourceViewer.getTextWidget();			
		if (text == null || text.isDisposed())
			return;
		
		updateColor(sourceViewer);
	
		text.addKeyListener(this);
		text.addMouseListener(this);
		text.addMouseMoveListener(this);
		text.addFocusListener(this);
		text.addPaintListener(this);
		
		updateKeyModifierMask();
		
		IPreferenceStore preferenceStore= fPrefStore;
		preferenceStore.addPropertyChangeListener(this);			
	}
	
	public void uninstall() {

		if (fColor != null) {
			fColor.dispose();
			fColor= null;
		}
		
		if (fCursor != null) {
			fCursor.dispose();
			fCursor= null;
		}
		
		IPreferenceStore preferenceStore= fPrefStore;
		if (preferenceStore != null)
			preferenceStore.removePropertyChangeListener(this);

		ISourceViewer sourceViewer= fViewer;
		if (sourceViewer == null)
			return;

		StyledText text= sourceViewer.getTextWidget();
		if (text == null || text.isDisposed())
			return;
						
		text.removeKeyListener(this);
		text.removeMouseListener(this);
		text.removeMouseMoveListener(this);
		text.removeFocusListener(this);
		text.removePaintListener(this);
	}
	private void updateKeyModifierMask() {
		//Add code here to allow for specification of hyperlink trigger key
		fKeyModifierMask=262144;
	}
	
	private void updateColor(ISourceViewer viewer) {
		if (fColor != null)
			fColor.dispose();
		
		StyledText text= viewer.getTextWidget();
		if (text == null || text.isDisposed())
			return;

		Display display= text.getDisplay();
		fColor= createColor(fPrefStore, CEditor.LINKED_POSITION_COLOR, display);
	}
	
	/**
	 * Creates a color from the information stored in the given preference store.
	 * Returns <code>null</code> if there is no such information available.
	 */
	private Color createColor(IPreferenceStore store, String key, Display display) {
		
		RGB rgb= null;		
		
		if (store.contains(key)) {
			
			if (store.isDefault(key))
				rgb= PreferenceConverter.getDefaultColor(store, key);
			else
				rgb= PreferenceConverter.getColor(store, key);
			
			if (rgb != null)
				return new Color(display, rgb);
		}
		
		return null;
	}	
	
	public void mouseDoubleClick(MouseEvent e) {}

	public void mouseDown(MouseEvent event) {
		if (!fActive)
			return;
		
		if (event.stateMask != fKeyModifierMask) {
			deactivate();
			return;	
		}
		
		if (event.button != 1) {
			deactivate();
			return;	
		}			
	}

	public void mouseUp(MouseEvent e) {
		if (!fActive)
			return;
		
		if (e.button != 1) {
			deactivate();
			return;
		}
		
		boolean wasActive= fCursor != null;
		boolean wasInclude = fIncludeMode;
		
		deactivate();

		if (wasActive) {
			if (wasInclude){
			/*	IAction action= getAction("OpenInclude");  //$NON-NLS-1$
				if (action != null){
					action.run();
				}*/
			}
			else {
			if (fOpenDeclAction != null)
				fOpenDeclAction.run();
			}
		}
	}

	public void keyPressed(KeyEvent event) {
		if (fActive) {
			deactivate();
			return;	
		}

		if (event.keyCode != fKeyModifierMask) {
			deactivate();
			return;
		}
		
		fActive= true;
	}

	public void keyReleased(KeyEvent event) {
		if (!fActive)
			return;

		deactivate();
	}

	public void mouseMove(MouseEvent event) {
		if (event.widget instanceof Control && !((Control) event.widget).isFocusControl()) {
			deactivate();
			return;
		}
		
		if (!fActive) {
			if (event.stateMask != fKeyModifierMask)
				return;
			// modifier was already pressed
			fActive= true;
		}
		
		ISourceViewer viewer= fViewer;
		if (viewer == null) {
			deactivate();
			return;
		}
		
		StyledText text= viewer.getTextWidget();
		if (text == null || text.isDisposed()) {
			deactivate();
			return;
		}
		
		if ((event.stateMask & SWT.BUTTON1) != 0 && text.getSelectionCount() != 0) {
			deactivate();
			return;
		}
		
		IRegion region= getCurrentTextRegion(viewer);
		if (region == null || region.getLength() == 0) {
			repairRepresentation();
			return;
		}
		
		highlightRegion(viewer, region);	
		activateCursor(viewer);				
	}
	
	IRegion getCurrentTextRegion(ISourceViewer viewer) {
		int offset= getCurrentTextOffset(viewer);				
		if (offset == -1)
			return null;

		//Need some code in here to determine if the selected input should
		//be selected - the JDT does this by doing a code complete on the input -
		//if there are any elements presented it selects the word
		
		return selectWord(viewer.getDocument(), offset);	
	}
	//TODO: Modify this to work with qualified name
	private IRegion selectWord(IDocument document, int anchor) {
		
		try {		
			int offset= anchor;
			char c;
			
			while (offset >= 0) {
				c= document.getChar(offset);
				if (!Character.isJavaIdentifierPart(c))
					break;
				--offset;
			}
			
			int start= offset;
			
			offset= anchor;
			int length= document.getLength();
			
			while (offset < length) {
				c= document.getChar(offset);
				if (!Character.isJavaIdentifierPart(c))
					break;
				++offset;
			}
			
			int end= offset;
			//Allow for new lines
			if (start == end)
				return new Region(start, 0);
			else{
				String selWord = null;
				String slas = document.get(start,1);
				if (slas.equals("\n") || //$NON-NLS-1$
					slas.equals("\t") || //$NON-NLS-1$
				   slas.equals(" "))	 //$NON-NLS-1$
				 {
					
					selWord =document.get(start+1, end - start - 1);
				}
				else{
					selWord =document.get(start, end - start);  	
				}
				//Check for keyword
				if (isKeyWord(selWord))
					return null;
				//Avoid selecting literals, includes etc.
				char charX = selWord.charAt(0);
				if (charX == '"' ||
					charX == '.' ||
					charX == '<' ||
					charX == '>')
					return null;
				
				if (selWord.equals("#include")) //$NON-NLS-1$
				{
					//get start of next identifier
					
				 
				  int end2 = end;
	
				  while (!Character.isJavaIdentifierPart(document.getChar(end2))){
				    	++end2;
				    	
				  }
				  
				  while (end2 < length){
				  	c = document.getChar(end2);
				  	
				  	if (!Character.isJavaIdentifierPart(c) &&
				  		 c != '.')
				  		break;
				  	++end2;
				  }
				  
				  int finalEnd = end2;
				  selWord =document.get(start, finalEnd - start);
				  end = finalEnd + 1;
				  start--;
				  fIncludeMode = true;
				}
				
				  
				
				return new Region(start + 1, end - start - 1);
			}
			
		} catch (BadLocationException x) {
			return null;
		}
	}
	
	private boolean isKeyWord(String selWord) {
		Iterator i = fgKeywords.iterator();
		
		while (i.hasNext()){
			 String tempWord = (String) i.next();
			 if (selWord.equals(tempWord))
			 	return true;
		}
		
		return false;
	}

	private int getCurrentTextOffset(ISourceViewer viewer) {

		try {					
			StyledText text= viewer.getTextWidget();			
			if (text == null || text.isDisposed())
				return -1;

			Display display= text.getDisplay();				
			Point absolutePosition= display.getCursorLocation();
			Point relativePosition= text.toControl(absolutePosition);
			
			int widgetOffset= text.getOffsetAtLocation(relativePosition);
			if (viewer instanceof ITextViewerExtension5) {
				ITextViewerExtension5 extension= (ITextViewerExtension5) viewer;
				return extension.widgetOffset2ModelOffset(widgetOffset);
			} else {
				return widgetOffset + viewer.getVisibleRegion().getOffset();
			}

		} catch (IllegalArgumentException e) {
			return -1;
		}			
	}
	
	private void highlightRegion(ISourceViewer viewer, IRegion region) {

		if (region.equals(fActiveRegion))
			return;

		repairRepresentation();

		StyledText text= viewer.getTextWidget();
		if (text == null || text.isDisposed())
			return;

		// highlight region
		int offset= 0;
		int length= 0;
		
		if (viewer instanceof ITextViewerExtension5) {
			ITextViewerExtension5 extension= (ITextViewerExtension5) viewer;
			IRegion widgetRange= extension.modelRange2WidgetRange(region);
			if (widgetRange == null)
				return;
			
			offset= widgetRange.getOffset();
			length= widgetRange.getLength();
			
		} else {
			offset= region.getOffset() - viewer.getVisibleRegion().getOffset();
			length= region.getLength();
		}
		
		StyleRange oldStyleRange= text.getStyleRangeAtOffset(offset);
		Color foregroundColor= fColor;
		Color backgroundColor= oldStyleRange == null ? text.getBackground() : oldStyleRange.background;
		StyleRange styleRange= new StyleRange(offset, length, foregroundColor, backgroundColor);
		text.setStyleRange(styleRange);

		// underline
		text.redrawRange(offset, length, true);

		fActiveRegion= region;
	}
	
	
	private void repairRepresentation() {			
		repairRepresentation(false);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.FocusListener#focusGained(java.awt.event.FocusEvent)
	 */
	public void focusGained(FocusEvent arg0) {
	}

	/* (non-Javadoc)
	 * @see java.awt.event.FocusListener#focusLost(java.awt.event.FocusEvent)
	 */
	public void focusLost(FocusEvent arg0) {
		deactivate();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.PaintListener#paintControl(org.eclipse.swt.events.PaintEvent)
	 */
	public void paintControl(PaintEvent event) {
		if (fActiveRegion == null)
			return;
		
		ISourceViewer viewer= fViewer;
		if (viewer == null)
			return;
		
		StyledText text= viewer.getTextWidget();
		if (text == null || text.isDisposed())
			return;
		
		
		int offset= 0;
		int length= 0;

		if (viewer instanceof ITextViewerExtension5) {
			
			ITextViewerExtension5 extension= (ITextViewerExtension5) viewer;
			IRegion widgetRange= extension.modelRange2WidgetRange(new Region(offset, length));
			if (widgetRange == null)
				return;
			
			offset= widgetRange.getOffset();
			length= widgetRange.getLength();
			
		} else {
			
			IRegion region= viewer.getVisibleRegion();			
			if (!includes(region, fActiveRegion))
				return;		    

			offset= fActiveRegion.getOffset() - region.getOffset();
			length= fActiveRegion.getLength();
		}
		
		// support for bidi
		Point minLocation= getMinimumLocation(text, offset, length);
		Point maxLocation= getMaximumLocation(text, offset, length);
		
		int x1= minLocation.x;
		int x2= minLocation.x + maxLocation.x - minLocation.x - 1;
		int y= minLocation.y + text.getLineHeight() - 1;
		
		GC gc= event.gc;
		if (fColor != null && !fColor.isDisposed())
			gc.setForeground(fColor);
		gc.drawLine(x1, y, x2, y);
	
	}

	private boolean includes(IRegion region, IRegion position) {
		return
		position.getOffset() >= region.getOffset() &&
		position.getOffset() + position.getLength() <= region.getOffset() + region.getLength();
	}

	private Point getMinimumLocation(StyledText text, int offset, int length) {
		Point minLocation= new Point(Integer.MAX_VALUE, Integer.MAX_VALUE);
		
		for (int i= 0; i <= length; i++) {
			Point location= text.getLocationAtOffset(offset + i);
			
			if (location.x < minLocation.x)
				minLocation.x= location.x;			
			if (location.y < minLocation.y)
				minLocation.y= location.y;			
		}	
		
		return minLocation;
	}
	
	private Point getMaximumLocation(StyledText text, int offset, int length) {
		Point maxLocation= new Point(Integer.MIN_VALUE, Integer.MIN_VALUE);
		
		for (int i= 0; i <= length; i++) {
			Point location= text.getLocationAtOffset(offset + i);
			
			if (location.x > maxLocation.x)
				maxLocation.x= location.x;			
			if (location.y > maxLocation.y)
				maxLocation.y= location.y;			
		}	
		
		return maxLocation;
	}
	
	public void propertyChange(PropertyChangeEvent event) {
		if (event.getProperty().equals(CEditor.LINKED_POSITION_COLOR)) {
			ISourceViewer viewer= fViewer;
			if (viewer != null)	
				updateColor(viewer);
		}
	}

	
}
