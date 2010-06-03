/*******************************************************************************
 * Copyright (c) 2007, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.disassembly;

import org.eclipse.swt.dnd.*;

/**
 * DisassemblyDropAdapter
 */
public class DisassemblyDropAdapter extends DropTargetAdapter {

	private DisassemblyPart fDisassembly;
	
	/**
	 * New DisassemblyDropAdapter.
	 */
	public DisassemblyDropAdapter(DisassemblyPart disassembly) {
		super();
		fDisassembly = disassembly;
	}

	/*
	 * @see org.eclipse.swt.dnd.DropTargetListener#drop(org.eclipse.swt.dnd.DropTargetEvent)
	 */
	@Override
	public void drop(final DropTargetEvent event) {
		TransferData dataType = event.currentDataType;
		if (isFileDataType(dataType)) {
			// event.data is an array of strings which represent the absolute file pathes
			assert event.data instanceof String[];
			String fileNames[] = (String[])event.data;
			dropFiles(fileNames);
		} else if (isTextDataType(dataType)) {
			// event.data is a string
			assert event.data instanceof String;
			String text = (String)event.data;
			if (text.indexOf('/') != -1 || text.indexOf('.') != -1) {
				dropFiles(new String[] { text });
			} else {
				dropText(text);
			}
		}
	}

	/*
	 * @see org.eclipse.swt.dnd.DropTargetListener#dragEnter(org.eclipse.swt.dnd.DropTargetEvent)
	 */
	@Override
	public void dragEnter(DropTargetEvent event) {
		event.detail = DND.DROP_COPY;
		event.feedback = DND.FEEDBACK_NONE;
	}

	/*
	 * @see org.eclipse.swt.dnd.DropTargetListener#dragOver(org.eclipse.swt.dnd.DropTargetEvent)
	 */
	@Override
	public void dragOver(DropTargetEvent event) {
		event.detail = DND.DROP_COPY;
		event.feedback = DND.FEEDBACK_NONE;
	}

	private static boolean isFileDataType(TransferData dataType) {
		return FileTransfer.getInstance().isSupportedType(dataType);
	}
	private static boolean isTextDataType(TransferData dataType) {
		return TextTransfer.getInstance().isSupportedType(dataType);
	}

	/*
	 * @see org.eclipse.swt.dnd.DropTargetListener#dropAccept(org.eclipse.swt.dnd.DropTargetEvent)
	 */
	@Override
	public void dropAccept(DropTargetEvent event) {
	}

	/*
	 * @see org.eclipse.swt.dnd.DropTargetListener#dragOperationChanged(org.eclipse.swt.dnd.DropTargetEvent)
	 */
	@Override
	public void dragOperationChanged(DropTargetEvent event) {
		event.detail = DND.DROP_COPY;
		event.feedback = DND.FEEDBACK_NONE;
	}

	/*
	 * @see org.eclipse.swt.dnd.DropTargetListener#dragLeave(org.eclipse.swt.dnd.DropTargetEvent)
	 */
	@Override
	public void dragLeave(DropTargetEvent event) {
	}

	/**
	 * Drop files.
	 * @param fileNames
	 */
	private void dropFiles(String[] fileNames) {
		// open all the files
		for (int i = 0; i < fileNames.length; i++) {
			// get disassembly for file
			fDisassembly.retrieveDisassembly(fileNames[i], 100, true);
		}
	}

	/**
	 * Drop text.
	 * @param text
	 */
	private void dropText(String text) {
		fDisassembly.gotoSymbol(text.trim());
	}

}
