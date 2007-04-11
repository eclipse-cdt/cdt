/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Michael Scharf (Wind River) - initial implementation
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.control;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;


/**
 * Manages the Command History for the command line input
 * of the terminal control.
 *
 */
public class CommandInputFieldWithHistory implements ICommandInputField {
	final List fHistory=new ArrayList();
	private int fHistoryPos=0;
	private final int fMaxSize;
	private boolean fInHistory=false;
	private Text fInputField;
	public CommandInputFieldWithHistory(int maxHistorySize) {
		fMaxSize=maxHistorySize;
	}
	/**
	 * Add a line to the history. 
	 * @param line
	 */
	protected void pushLine(String line) {
		// if we used the history. therefore we added the current as 0th item
		if(fInHistory)
			fHistory.remove(0);
		fInHistory=false;
		fHistoryPos=0;
		// anything to remember?
		if(line==null || line.trim().length()==0)
			return;
		fHistory.add(0,line);
		// ignore if the same as last
		if(fHistory.size()>1 && line.equals(fHistory.get(1)))
			fHistory.remove(0);
		// limit the history size.
		if(fHistory.size()>=fMaxSize)
			fHistory.remove(fHistory.size()-1);
	}
	/**
	 * Sets the history
	 * @param history or null
	 */
	public void setHistory(String history) {
		fHistory.clear();
		if(history==null)
			return;
		fHistory.addAll(Arrays.asList(history.split("\n"))); //$NON-NLS-1$
	}
	/**
	 * @return the current content of the history buffer and new line separated list
	 */
	public String getHistory() {
		StringBuffer buff=new StringBuffer();
		boolean sep=false;
		for (Iterator iterator = fHistory.iterator(); iterator.hasNext();) {
			String line=(String) iterator.next();
			if(line.length()>0) {
				if(sep)
					buff.append("\n"); //$NON-NLS-1$
				else
					sep=true;
				buff.append(line);
			}
		}
		return buff.toString();
	}
	/**
	 * @param currLine
	 * @param count (+1 or -1) for forward and backward movement. -1 goes back
	 * @return the new string to be displayed in the command line or null,
	 * if the limit is reached.
	 */
	public String move(String currLine, int count) {
		if(!fInHistory) {
			fInHistory=true;
			fHistory.add(0,currLine);
		} else {
			fHistory.set(fHistoryPos,currLine);
		}
		if(fHistoryPos+count>=fHistory.size())
			return null;
		if(fHistoryPos+count<0)
			return null;
		fHistoryPos+=count;
		return (String) fHistory.get(fHistoryPos);
	}

	/**
	 * Exit the history movements and go to position 0;
	 * @return the string to be shown in the command line
	 */
	protected String escape() {
		if(!fInHistory)
			return null;
		fHistoryPos=0;
		return (String) fHistory.get(fHistoryPos);
	}
	public void createControl(Composite parent,final ITerminalViewControl terminal) {
		fInputField=new Text(parent, SWT.SINGLE|SWT.BORDER);
		fInputField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		fInputField.setFont(terminal.getCtlText().getFont());
		fInputField.addKeyListener(new KeyListener(){
			public void keyPressed(KeyEvent e) {
				if(e.keyCode=='\n' || e.keyCode=='\r') {
					e.doit=false;
					String line=fInputField.getText();
					if(!terminal.pasteString(line+"\n")) //$NON-NLS-1$
						return;
					pushLine(line);
					setCommand("");//$NON-NLS-1$
				} else if(e.keyCode==SWT.ARROW_UP || e.keyCode==SWT.PAGE_UP) {
					e.doit=false;
					setCommand(move(fInputField.getText(),1));
				} else if(e.keyCode==SWT.ARROW_DOWN || e.keyCode==SWT.PAGE_DOWN) {
					e.doit=false;
					setCommand(move(fInputField.getText(),-1));
				} else if(e.keyCode==SWT.ESC) {
					e.doit=false;
					setCommand(escape());
				}
			}
			private void setCommand(String line) {
				if(line==null)
					return;
				fInputField.setText(line);
				fInputField.setSelection(fInputField.getCharCount());
			}
			public void keyReleased(KeyEvent e) {
			}
		});
	}
	public void setFont(Font font) {
		fInputField.setFont(font);
		fInputField.getParent().layout(true);
	}
	public void dispose() {
		fInputField.dispose();
		fInputField=null;
		
	}
}