package org.eclipse.cdt.internal.corext.textmanipulation;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.internal.utils.Assert;
import org.eclipse.core.runtime.CoreException;

public class MultiTextEdit {

	private List fChildren;

	/**
	 * Creates a new composite text edit.
	 */
	public MultiTextEdit() {
		fChildren= new ArrayList(3);
	}

	protected MultiTextEdit(List children) throws CoreException {
		fChildren= new ArrayList(children.size());
		for (Iterator iter= children.iterator(); iter.hasNext();) {
			fChildren.add(((TextEdit)iter.next()).copy());
		}
	}
	
	protected List getChildren() {
		return fChildren;
	}
	
	/**
	 * Adds all <code>TextEdits</code> managed by the given multt text edit.
	 * 
	 * @param edit the multi text edit to be added.
	 */
	public void add(MultiTextEdit edit) {
		Assert.isNotNull(edit);
		fChildren.add(edit);
	}
	
	/**
	 * Adds a text edit.
	 * 
	 * @param edit the text edit to be added
	 */
	public void add(TextEdit edit) {
		Assert.isNotNull(edit);
		fChildren.add(edit);
	}
	
	/**
	 * Returns the children managed by this text edit collection.
	 * 
	 * @return the children of this composite text edit
	 */
	public Iterator iterator() {
		return fChildren.iterator();
	}

	/**
	 * Connects this text edit to the given <code>TextBufferEditor</code>. 
	 * Note that this method <b>should only be called</b> by a <code>
	 * TextBufferEditor</code>.
	 *<p>
	 * This default implementation does nothing. Subclasses may override
	 * if needed.
	 *  
	 * @param editor the text buffer editor this text edit has been added to
	 */
	public void connect(TextBufferEditor editor) throws CoreException {
		for (Iterator iter= fChildren.iterator(); iter.hasNext();) {
			Object element= iter.next();
			if (element instanceof TextEdit)
				editor.add((TextEdit)element);
			else
				editor.add((MultiTextEdit)element);
		}
	}
	
	/**
     * Creates and returns a copy of this text edit collection. The copy method should
     * be implemented in a way so that the copy can be added to a different  <code>
     * TextBuffer</code> without causing any harm to the object from which the copy 
     * has been created.
     * 
     * @return a copy of this object.
     */
	public MultiTextEdit copy() throws CoreException {
		return new MultiTextEdit(fChildren);
	}
	
	/**
	 * Returns the <code>TextRange</code> that this text edit is going to
	 * manipulate. If this method is called before the <code>MultiTextEdit</code>
	 * has been added to a <code>TextBufferEditor</code> it may return <code>
	 * null</code> to indicate this situation.
	 * 
	 * @return the <code>TextRange</code>s this <code>TextEdit is going
	 * 	to manipulate
	 */
	public TextRange getTextRange() {
		int size= fChildren.size();
		if (size == 0)
			return new TextRange(0,0);
		TextRange range= ((TextEdit)fChildren.get(0)).getTextRange();
		int start= range.getOffset();
		int end= range.getInclusiveEnd();
		for (int i= 1; i < size; i++) {
			range= ((TextEdit)fChildren.get(i)).getTextRange();
			start= Math.min(start, range.getOffset());
			end= Math.max(end, range.getInclusiveEnd());
		}
		return new TextRange(start, end - start + 1);
	}
	
	/**
	 * Returns the element modified by this text edit. The method
	 * may return <code>null</code> if the modification isn't related to a
	 * element or if the content of the modified text buffer doesn't
	 * follow any syntax.
	 * <p>
	 * This default implementation returns <code>null</code>
	 * 
	 * @return the element modified by this text edit
	 */
	public Object getModifiedElement() {
		return null;
	}	
}

