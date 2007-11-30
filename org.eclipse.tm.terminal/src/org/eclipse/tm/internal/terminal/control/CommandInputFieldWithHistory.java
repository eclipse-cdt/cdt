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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.fieldassist.ContentAssistCommandAdapter;


/**
 * Manages the Command History for the command line input
 * of the terminal control.
 * <li>
 * <ul>Navigate with ARROW_UP,ARROW_DOWN,PAGE_UP,PAGE_DOWN
 * <ul>ESC to cancel history editing
 * <ul>History can be edited (by moving up and edit) but changes are 
 * not persistent (like in bash). 
 * <ul>If the same command is entered multiple times in a row, 
 * only one entry is kept in the history. 
 * </li>
 *
 */
public class CommandInputFieldWithHistory implements ICommandInputField {
	private class FieldAssist implements IContentProposalProvider {

		public IContentProposal[] getProposals(String contents, int position) {
			String prefix=contents.substring(0, position);
			List result=new ArrayList();
			// show an entry only once
			Set seen=new HashSet();
			for (Iterator iterator = fHistory.iterator(); iterator.hasNext();) {
				String history = (String) iterator.next();
				if(history.startsWith(prefix) && !seen.contains(history)) {
					// the content is the rest of the history item
					String content=history.substring(prefix.length());
					result.add(new Proposal(content,history));
					// don't add this proposal again
					seen.add(history);
				}
			}
			return (IContentProposal[]) result.toArray(new IContentProposal[result.size()]);
		}
		
	}
	private static class Proposal implements IContentProposal {
		
		private final String fContent;
		private final String fLabel;
		Proposal(String content, String label) {
			fContent= content;
			fLabel= label;
		}
		public String getContent() {
			return fContent;
		}
		
		public String getLabel() {
			return fLabel;
		}
		
		public String getDescription() {
			return null;
		}
		
		public int getCursorPosition() {
			return fContent.length();
		}
	}

	final List fHistory=new ArrayList();
	/**
	 * Keeps a modifiable history while in history editing mode
	 */
	List fEditedHistory;
	/**
	 * The current position in the edit history
	 */
	private int fEditHistoryPos=0;
	/**
	 * The limit of the history.
	 */
	private final int fMaxSize;
	/**
	 * The input text field.
	 */
	private Text fInputField;
	public CommandInputFieldWithHistory(int maxHistorySize) {
		fMaxSize=maxHistorySize;
	}
	/**
	 * Add a line to the history. 
	 * @param line
	 */
	protected void pushLine(String line) {
		endHistoryMode();
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
		endHistoryMode();
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
		if(!inHistoryMode()) {
			fEditedHistory=new ArrayList(fHistory.size()+1);
			fEditedHistory.add(currLine);
			fEditedHistory.addAll(fHistory);
			fEditHistoryPos=0;
		}
		fEditedHistory.set(fEditHistoryPos,currLine);
		if(fEditHistoryPos+count>=fEditedHistory.size())
			return null;
		if(fEditHistoryPos+count<0)
			return null;
		fEditHistoryPos+=count;
		return (String) fEditedHistory.get(fEditHistoryPos);
	}
	private boolean inHistoryMode() {
		return fEditedHistory!=null;
	}

	/**
	 * Exit the history movements and go to position 0;
	 * @return the string to be shown in the command line
	 */
	protected String escape() {
		if(!inHistoryMode())
			return null;
		String line= (String) fEditedHistory.get(0);
		endHistoryMode();
		return line;
	}
	/**
	 * End history editing
	 */
	private void endHistoryMode() {
		fEditedHistory=null;
		fEditHistoryPos=0;
	}
	public void createControl(Composite parent,final ITerminalViewControl terminal) {
		fInputField=new Text(parent, SWT.SINGLE|SWT.BORDER);
		GridData data=new GridData(SWT.FILL, SWT.FILL, true, false);
		boolean installDecoration=true;
		if(installDecoration) {
			// The ContentAssistCommandAdapter says: "The client is responsible for
			// ensuring that adequate space is reserved for the decoration."
			// TODO: what is the "adequate space"???
			data.horizontalIndent=6;
		}
		fInputField.setLayoutData(data);
		fInputField.setFont(terminal.getFont());
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
		// register the field assist
		new ContentAssistCommandAdapter(
				fInputField,
				new TextContentAdapter(),
				new FieldAssist(), 
				null,
				null,
				installDecoration);
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