package org.eclipse.cdt.internal.corext.textmanipulation;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.util.Assert;

import org.eclipse.cdt.internal.corext.textmanipulation.TextEditNode.RootNode;
import org.eclipse.cdt.internal.ui.CStatusConstants;
import org.eclipse.cdt.ui.CUIPlugin;
/**
 * A <code>TextBufferEditor</code> manages a set of <code>TextEdit</code>s and applies
 * them as a whole to a <code>TextBuffer</code>. Added <code>TextEdit</code>s must 
 * not overlap. The only exception from this rule are insertion point. There can be more than
 * one insert point at the same text position. Clients should use the method <code>
 * canPerformEdits</code> to validate if all added text edits follow these rules.
 * <p>
 * Clients can attach more than one <code>TextBufferEditor</code> to a single <code>
 * TextBuffer</code>. If so <code>canPerformEdits</code> validates all text edits from
 * all text buffer editors working on the same text buffer.
 */
public class TextBufferEditor {
		
	private TextBuffer fBuffer;
	private List fEdits;
	private RootNode fRootNode;
	private int fNumberOfNodes;
	private int fConnectCount;
	private int fMode;

	/* package */ static final int UNDEFINED= 	0;
	/* package */ static final int REDO=				1;
	/* package */ static final int UNDO=			2;

	/**
	 * Creates a new <code>TextBufferEditor</code> for the given 
	 * <code>TextBuffer</code>.
	 * 
	 * @param the text buffer this editor is working on.
	 */
	public TextBufferEditor(TextBuffer buffer) {
		fBuffer= buffer;
		Assert.isNotNull(fBuffer);
		fEdits= new ArrayList();
	}
	
	/**
	 * Returns the text buffer this editor is working on.
	 * 
	 * @return the text buffer this editor is working on
	 */
	public TextBuffer getTextBuffer() {
		return fBuffer;
	}
	
	/**
	 * Adds a <code>TextEdit</code> to this text editor. Adding a <code>TextEdit</code>
	 * to a <code>TextBufferEditor</code> transfers ownership of the edit to the editor. So
	 * after a edit has been added to a editor the creator of that edit <b>must</b> not continue
	 * modifing it.
	 * 
	 * @param edit the text edit to be added
	 * @exception CoreException if the text edit can not be added
	 * 	to this text buffer editor
	 */
	public void add(TextEdit edit) throws CoreException {
		Assert.isTrue(fMode == UNDEFINED || fMode == REDO);
		internalAdd(edit);
		fMode= REDO;
	}
		
	/**
	 * Adds a <code>MultiTextEdit</code> to this text editor. Adding a <code>MultiTextEdit</code>
	 * to a <code>TextBufferEditor</code> transfers ownership of the edit to the editor. So
	 * after a edit has been added to a editor the creator of that edit <b>must</b> not continue
	 * modifing it.
	 * 
	 * @param edit the multi text edit to be added
	 * @exception CoreException if the multi text edit can not be added
	 * 	to this text buffer editor
	 */
	public void add(MultiTextEdit edit) throws CoreException {
		Assert.isTrue(fMode == UNDEFINED || fMode == REDO);
		edit.connect(this);
		fMode= REDO;
	}

	/**
	 * Adds a <code>UndoMemento</code> to this text editor. Adding a <code>UndoMemento</code>
	 * to a <code>TextBufferEditor</code> transfers ownership of the memento to the editor. So
	 * after a memento has been added to a editor the creator of that memento <b>must</b> not continue
	 * modifing it.
	 * 
	 * @param undo the undo memento to be added
	 * @exception CoreException if the undo memento can not be added
	 * 	to this text buffer editor
	 */
	public void add(UndoMemento undo) throws CoreException {
		Assert.isTrue(fMode == UNDEFINED);
		List list= undo.fEdits;
		// Add them reverse since we are adding undos.
		for (int i= list.size() - 1; i >= 0; i--) {
			internalAdd((TextEdit)list.get(i));			
		}
		fMode= undo.fMode;
	}
	
	/**
	 * Checks if the <code>TextEdit</code> added to this text editor can be executed.
	 * 
	 * @return <code>true</code> if the edits can be executed. Return  <code>false
	 * 	</code>otherwise. One major reason why text edits cannot be executed
	 * 	is a wrong offset or length value of a <code>TextEdit</code>.
	 */
	public boolean canPerformEdits() {
		if (fRootNode != null)
			return true;
		fRootNode= buildTree();
		if (fRootNode == null)
			return false;
		if (fRootNode.validate(fBuffer.getLength()))
			return true;
			
		fRootNode= null;
		return false;
	}
	
	/**
	 * Clears the text buffer editor.
	 */
	public void clear() {
		fRootNode= null;
		fMode= UNDEFINED;
		fEdits.clear();
	}
	
	/**
	 * Executes the text edits added to this text buffer editor and clears all added
	 * text edits.
	 * 
	 * @param pm a progress monitor to report progress
	 * @return an object representing the undo of the executed <code>TextEdit</code>s
	 * @exception CoreException if the edits cannot be executed
	 */
	public UndoMemento performEdits(IProgressMonitor pm) throws CoreException {
		if (pm == null)
			pm= new NullProgressMonitor();
			
		int size= fEdits.size();
		if (size == 0)
			return new UndoMemento(fMode == UNDO ? REDO : UNDO);
			
		if (fRootNode == null) {
			fRootNode= buildTree();
			if (fRootNode == null || !fRootNode.validate(fBuffer.getLength())) {
				IStatus s= new Status(IStatus.ERROR, CUIPlugin.PLUGIN_ID, CStatusConstants.INTERNAL_ERROR, 
				"RootNode empty", null);
				throw new CoreException(s);
			}
		}
		try {
			pm.beginTask("", fNumberOfNodes + 10); //$NON-NLS-1$
			UndoMemento undo= null;
			if (fMode == REDO) {
				undo= fRootNode.performDo(fBuffer, pm);
				fRootNode.performedDo();
			} else {
				undo= fRootNode.performUndo(fBuffer, pm);
				fRootNode.performedUndo();
			}
			pm.worked(10);
			return undo;
		} finally {
			pm.done();
			clear();
		}
	}
	
	//---- Helper methods ------------------------------------------------------------
	
	private RootNode buildTree() {
		TextEditNode[] nodes= new TextEditNode[fEdits.size()];
		for (int i= fEdits.size() - 1; i >= 0; i--) {
			nodes[i]= TextEditNode.create((TextEdit)fEdits.get(i));
		}
		fNumberOfNodes= nodes.length;
		Arrays.sort(nodes, new TextEditNodeComparator());
		RootNode root= new RootNode(fBuffer.getLength());
		for (int i= 0; i < nodes.length; i++) {
			root.add(nodes[i]);
		}
		return root;
	}
	
	private void internalAdd(TextEdit edit) throws CoreException {
		edit.index= fEdits.size();
		edit.isSynthetic= fConnectCount > 0;
		try {
			fConnectCount++;
			edit.connect(this);
		} finally {
			fConnectCount--;
		}
		fEdits.add(edit);
	}	
}

