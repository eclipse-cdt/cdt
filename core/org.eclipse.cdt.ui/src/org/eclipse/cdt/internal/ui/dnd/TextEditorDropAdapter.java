/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.dnd;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.Position;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.StyledTextContent;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Caret;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.MarkerTransfer;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ITextEditorDropTargetListener;
import org.eclipse.ui.texteditor.ITextEditorExtension;

import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.CUIMessages;
import org.eclipse.cdt.internal.ui.util.EditorUtility;
import org.eclipse.cdt.internal.ui.util.ExceptionHandler;

/**
 * A drop adapter which supports dragging a non-workspace file from some
 * external tool (e.g. explorer) into the editor area. The adaptor also supports
 * text and marker transfer.
 */
public class TextEditorDropAdapter extends DropTargetAdapter implements
		ITextEditorDropTargetListener {

	/**
	 * Adapter factory for text editor drop target listeners. Can be registered
	 * to add text, file and marker drop support for all
	 * <code>ITextEditor</code>s.
	 * 
	 * @see ITextEditorDropTargetListener
	 */
	public static class Factory implements IAdapterFactory {
		private static final Class[] CLASSES= { ITextEditorDropTargetListener.class };

		/*
		 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object,
		 *      java.lang.Class)
		 */
		public Object getAdapter(Object adaptableObject, Class adapterType) {
			if (adaptableObject instanceof ITextEditor) {
				return TextEditorDropAdapter.create((ITextEditor) adaptableObject);
			}
			return null;
		}

		/*
		 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
		 */
		public Class[] getAdapterList() {
			return CLASSES;
		}

	}

	/** Saved original caret during drag operation */
	private Caret fOldCaret;
	/** Caret used during drag operation to indicate insert point */
	private Caret fCaret;
	/** The text viewer target */
	private ITextViewer fViewer;
	/** The editor containing the viewer (can be null) */
	private ITextEditor fEditor;
	/** Direction of current autoscroll SWT.UP, SWT.DOWN or SWT.NULL */
	private int fAutoScrollDirection= SWT.NULL;

	/**
	 * Create an EditorDropAdapter for the given text viewer and (optional)
	 * editor.
	 * 
	 * @param viewer
	 *            the text viewer, may not be <code>null</code>
	 * @param editor
	 *            the text editor, may be <code>null</code>
	 */
	public TextEditorDropAdapter(ITextViewer viewer, ITextEditor editor) {
		super();
		fViewer= viewer;
		fEditor= editor;
	}

	/*
	 * @see org.eclipse.swt.dnd.DropTargetListener#drop(org.eclipse.swt.dnd.DropTargetEvent)
	 */
	public void drop(final DropTargetEvent event) {
		try {
			TransferData dataType= event.currentDataType;
			if (isFileDataType(dataType)) {
				// event.data is an array of strings which represent the
				// absolute file pathes
				assert event.data instanceof String[];
				dropFiles((String[]) event.data);
			} else if (isMarkerDataType(dataType)) {
				assert event.data instanceof IMarker[];
				dropMarkers((IMarker[]) event.data);
			} else if (isTextDataType(dataType)) {
				// event.data is a string
				assert event.data instanceof String;
				assert isDocumentEditable();
				autoScroll(SWT.NULL);
				int offset= getOffsetAtLocation(event.x, event.y, true);
				dropText((String) event.data, offset);
			}
		} catch (CoreException exc) {
			ExceptionHandler.handle(exc, 
					CUIMessages.getString("TextEditorDropAdapter.error.title"),
					CUIMessages.getString("TextEditorDropAdapter.error.message"));
		}
	}

	/*
	 * @see org.eclipse.swt.dnd.DropTargetListener#dragEnter(org.eclipse.swt.dnd.DropTargetEvent)
	 */
	public void dragEnter(DropTargetEvent event) {
		TransferData dataType= event.currentDataType;
		if (isFileDataType(dataType)) {
			// make sure the file is never moved; always do a copy
			event.detail= DND.DROP_COPY;
			event.feedback= DND.FEEDBACK_NONE;
		} else if (isMarkerDataType(dataType)) {
			event.detail= DND.DROP_COPY;
			event.feedback= DND.FEEDBACK_NONE;
		} else if (isTextDataType(dataType)) {
			if (isDocumentEditable()) {
				if (event.detail == DND.DROP_DEFAULT) {
					event.detail= getAcceptableOperation(event.operations);
				}
				StyledText textWidget= fViewer.getTextWidget();
				textWidget.setFocus();
				fOldCaret= textWidget.getCaret();
				fCaret= new Caret(textWidget, SWT.NONE);
				fCaret.setSize(fOldCaret.getSize());
				int offset= getOffsetAtLocation(event.x, event.y, true);
				if (offset >= 0) {
					fCaret.setLocation(textWidget.getLocationAtOffset(offset));
				}
			} else {
				event.detail= DND.DROP_NONE;
			}
			event.feedback= DND.FEEDBACK_SCROLL;
		}
	}

	/*
	 * @see org.eclipse.swt.dnd.DropTargetListener#dragOver(org.eclipse.swt.dnd.DropTargetEvent)
	 */
	public void dragOver(DropTargetEvent event) {
		TransferData dataType= event.currentDataType;
		if (isFileDataType(dataType)) {
			// make sure the file is never moved; always do a copy
			event.detail= DND.DROP_COPY;
			event.feedback= DND.FEEDBACK_NONE;
		} else if (isMarkerDataType(dataType)) {
			event.detail= DND.DROP_COPY;
			event.feedback= DND.FEEDBACK_NONE;
		} else if (isTextDataType(dataType)) {
			if (isDocumentEditable()) {
				if ((event.operations & event.detail) == 0) {
					event.detail= getAcceptableOperation(event.operations);
				}
				int offset= getOffsetAtLocation(event.x, event.y, true);
				StyledText textWidget= fViewer.getTextWidget();
				if (offset >= 0) {
					fCaret.setLocation(textWidget.getLocationAtOffset(offset));
				} else {
					event.detail= DND.DROP_NONE;
				}
				// scroll feedback has issues (bugs 149576, 139485): we do it
				// ourselves
				Point location= textWidget.toControl(event.x, event.y);
				Rectangle viewPort= textWidget.getClientArea();
				if (location.y < textWidget.getLineHeight()) {
					autoScroll(SWT.UP);
				} else if (location.y > viewPort.height
						- textWidget.getLineHeight()) {
					autoScroll(SWT.DOWN);
				} else {
					autoScroll(SWT.NULL);
				}
			} else {
				event.detail= DND.DROP_NONE;
			}
			event.feedback= DND.FEEDBACK_SCROLL;
		}
	}

	private static boolean isFileDataType(TransferData dataType) {
		return FileTransfer.getInstance().isSupportedType(dataType);
	}

	private static boolean isTextDataType(TransferData dataType) {
		return TextTransfer.getInstance().isSupportedType(dataType);
	}

	private static boolean isMarkerDataType(TransferData dataType) {
		return MarkerTransfer.getInstance().isSupportedType(dataType);
	}

	/*
	 * @see org.eclipse.swt.dnd.DropTargetListener#dropAccept(org.eclipse.swt.dnd.DropTargetEvent)
	 */
	public void dropAccept(DropTargetEvent event) {
		TransferData dataType= event.currentDataType;
		if (isTextDataType(dataType)) {
			// check if the offset is inside the drag source selection
			IDocument doc= fViewer.getDocument();
			if (doc.containsPositionCategory(TextViewerDragAdapter.DRAG_SELECTION_CATEGORY)) {
				int widgetOffset= getOffsetAtLocation(event.x, event.y, true);
				if (widgetOffset != -1) {
					int documentOffset= getDocumentOffset(widgetOffset);
					try {
						Position[] dragSource= doc.getPositions(TextViewerDragAdapter.DRAG_SELECTION_CATEGORY);
						if (dragSource.length == 0
								|| event.detail == DND.DROP_MOVE
								&& dragSource[0].includes(documentOffset)) {
							// do not drop-move on the drag source
							event.detail = DND.DROP_NONE;
						}
					} catch (BadPositionCategoryException e) {
						event.detail= DND.DROP_NONE;
					}
				}
			}
		}
	}

	/*
	 * @see org.eclipse.swt.dnd.DropTargetListener#dragOperationChanged(org.eclipse.swt.dnd.DropTargetEvent)
	 */
	public void dragOperationChanged(DropTargetEvent event) {
		TransferData dataType= event.currentDataType;
		if (isFileDataType(dataType)) {
			// make sure the file is never moved; always do a copy
			event.detail= DND.DROP_COPY;
			event.feedback= DND.FEEDBACK_NONE;
		} else if (isMarkerDataType(dataType)) {
			event.detail= DND.DROP_COPY;
			event.feedback= DND.FEEDBACK_NONE;
		} else if (isTextDataType(dataType)) {
			if (isDocumentEditable()) {
				if (event.detail == DND.DROP_DEFAULT) {
					event.detail= getAcceptableOperation(event.operations);
				}
			} else {
				event.detail= DND.DROP_NONE;
			}
			event.feedback= DND.FEEDBACK_SCROLL;
		}
	}

	/**
	 * Get preferred operation out of allowed set.
	 * 
	 * @param operations
	 *            a bitset of allowed operations
	 * @return operation the preferred operation
	 */
	private int getAcceptableOperation(int operations) {
		if ((operations & DND.DROP_MOVE) != 0 && isDocumentEditable()) {
			return DND.DROP_MOVE;
		}
		if ((operations & DND.DROP_COPY) != 0) {
			return DND.DROP_COPY;
		}
		return DND.DROP_NONE;
	}

	/*
	 * @see org.eclipse.swt.dnd.DropTargetListener#dragLeave(org.eclipse.swt.dnd.DropTargetEvent)
	 */
	public void dragLeave(DropTargetEvent event) {
		autoScroll(SWT.NULL);
		if (fOldCaret != null) {
			StyledText textWidget= fViewer.getTextWidget();
			textWidget.setCaret(fOldCaret);
			fOldCaret= null;
		}
		if (fCaret != null) {
			fCaret.dispose();
			fCaret= null;
		}
	}

	/**
	 * Drop text data at offset.
	 * 
	 * @param text
	 * @param offset
	 * @throws CoreException 
	 */
	private void dropText(String text, int offset) throws CoreException {
		IDocument d= fViewer.getDocument();
		try {
			int docOffset= getDocumentOffset(offset);
			d.replace(docOffset, 0, text);
			fViewer.setSelectedRange(docOffset, text.length());
		} catch (BadLocationException e) {
			// should not happen
			throw new CoreException(
					new Status(IStatus.ERROR, CUIPlugin.getPluginId(), 0,
							e.getLocalizedMessage(), e));
		}
	}

	/**
	 * Drop (external) files.
	 * 
	 * @param fileNames
	 */
	private void dropFiles(String[] fileNames) throws CoreException {
		for (int i= 0; i < fileNames.length; i++) {
			Path path= new Path(fileNames[i]);
			java.io.File file= path.toFile();
			if (!file.isFile()) {
				throw new CoreException(new Status(IStatus.ERROR, CUIPlugin
						.getPluginId(), 0, CUIMessages.getFormattedString(
						"TextEditorDropAdapter.noFile", fileNames[i]),
						null));
			}
			if (file.canRead()) {
				IEditorInput editorInput= EditorUtility.getEditorInputForLocation(path);
				IEditorDescriptor desc= IDE.getEditorDescriptor(file.getName());
				String editorId= desc.getId();
				IDE.openEditor(getPage(), editorInput, editorId);
			} else {
				throw new CoreException(new Status(IStatus.ERROR, CUIPlugin
						.getPluginId(), 0, CUIMessages.getFormattedString(
						"TextEditorDropAdapter.unreadableFile", fileNames[i]),
						null));
			}
		}
	}

	/**
	 * Drop markers (open editor and navigate to marker).
	 * 
	 * @param markers
	 * @throws PartInitException
	 */
	private void dropMarkers(IMarker[] markers) throws PartInitException {
		for (int i= 0; i < markers.length; i++) {
			IMarker marker= markers[i];
			IDE.openEditor(getPage(), marker);
		}
	}

	private IWorkbenchPage getPage() {
		if (fEditor != null) {
			return ((IWorkbenchPart) fEditor).getSite().getPage();
		}
		return CUIPlugin.getActivePage();
	}

	/**
	 * Convert mouse screen coordinates to a <code>StyledText</code> offset.
	 * 
	 * @param x
	 * @param y
	 * @param absolute
	 *            if <code>true</code>, coordinates are expected to be
	 *            absolute screen coordinates
	 * @return text offset
	 * 
	 * @see StyledText#getOffsetAtLocation() \
	 */
	private int getOffsetAtLocation(int x, int y, boolean absolute) {
		StyledText textWidget= fViewer.getTextWidget();
		StyledTextContent content= textWidget.getContent();
		Point location;
		if (absolute) {
			location= textWidget.toControl(x, y);
		} else {
			location= new Point(x, y);
		}
		int line= (textWidget.getTopPixel() + location.y)
				/ textWidget.getLineHeight();
		if (line >= content.getLineCount()) {
			return content.getCharCount();
		}
		int lineOffset= content.getOffsetAtLine(line);
		String lineText= content.getLine(line);
		Point endOfLine= textWidget.getLocationAtOffset(lineOffset
				+ lineText.length());
		if (location.x >= endOfLine.x) {
			return lineOffset + lineText.length();
		}
		try {
			return textWidget.getOffsetAtLocation(location);
		} catch (IllegalArgumentException iae) {
			// we are expecting this
			return -1;
		}
	}

	/**
	 * Convert a widget offset to the corresponding document offset.
	 * 
	 * @param widgetOffset
	 * @return document offset
	 */
	private int getDocumentOffset(int widgetOffset) {
		if (fViewer instanceof ITextViewerExtension5) {
			ITextViewerExtension5 extension= (ITextViewerExtension5) fViewer;
			return extension.widgetOffset2ModelOffset(widgetOffset);
		} else {
			IRegion visible= fViewer.getVisibleRegion();
			if (widgetOffset > visible.getLength()) {
				return -1;
			}
			return widgetOffset + visible.getOffset();
		}
	}

	/**
	 * Scrolls the viewer into the given direction.
	 * 
	 * @param direction
	 *            the scroll direction
	 */
	private void autoScroll(int direction) {

		if (fAutoScrollDirection == direction)
			return;

		final int TIMER_INTERVAL= 20;
		final Display display= fViewer.getTextWidget().getDisplay();
		Runnable timer= null;
		switch (direction) {
		case SWT.UP:
			timer= new Runnable() {
				public void run() {
					if (fAutoScrollDirection == SWT.UP) {
						int top= getInclusiveTopIndex();
						if (top > 0) {
							fViewer.setTopIndex(top - 1);
							display.timerExec(TIMER_INTERVAL, this);
						}
					}
				}
			};
			break;
		case SWT.DOWN:
			timer= new Runnable() {
				public void run() {
					if (fAutoScrollDirection == SWT.DOWN) {
						int top= getInclusiveTopIndex();
						fViewer.setTopIndex(top + 1);
						display.timerExec(TIMER_INTERVAL, this);
					}
				}
			};
			break;
		}

		fAutoScrollDirection= direction;
		if (timer != null) {
			display.timerExec(TIMER_INTERVAL, timer);
		}
	}

	/**
	 * Returns the viewer's first visible line, even if only partially visible.
	 * 
	 * @return the viewer's first visible line
	 */
	private int getInclusiveTopIndex() {
		StyledText textWidget= fViewer.getTextWidget();
		if (textWidget != null && !textWidget.isDisposed()) {
			int top= fViewer.getTopIndex();
			if ((textWidget.getTopPixel() % textWidget.getLineHeight()) != 0)
				--top;
			return top;
		}
		return -1;
	}

	/**
	 * @return true if the document may be changed by the drag.
	 */
	private boolean isDocumentEditable() {
		if (fEditor instanceof ITextEditorExtension) {
			return !((ITextEditorExtension) fEditor).isEditorInputReadOnly();
		}
		return fViewer.isEditable();
	}

	/*
	 * @see org.eclipse.ui.texteditor.ITextEditorDropTargetListener#getTransfers()
	 */
	public Transfer[] getTransfers() {
		return new Transfer[] { TextTransfer.getInstance(),
				FileTransfer.getInstance(), MarkerTransfer.getInstance() };
	}

	/**
	 * Factory method to create a drop target listener for the given text
	 * editor.
	 * 
	 * @param textEditor
	 * @return a drop target listener or <code>null</code>
	 */
	protected static ITextEditorDropTargetListener create(ITextEditor textEditor) {
		ITextViewer textViewer= (ITextViewer) textEditor
				.getAdapter(ITextViewer.class);
		if (textViewer == null) {
			// this is a little trick to get the viewer from a text editor
			ITextOperationTarget target= (ITextOperationTarget) textEditor
					.getAdapter(ITextOperationTarget.class);
			if (target instanceof ITextViewer) {
				textViewer= (ITextViewer) target;
			}
		}
		if (textViewer == null) {
			return null;
		}
		return new TextEditorDropAdapter(textViewer, textEditor);
	}
}
