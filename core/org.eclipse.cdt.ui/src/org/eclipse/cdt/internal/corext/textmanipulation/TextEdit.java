package org.eclipse.cdt.internal.corext.textmanipulation;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.core.runtime.CoreException;

/**
 * A text edit describes an elementary text manipulation operation. Text edits
 * are executed by adding them to a <code>TextBufferEditor</code> and then
 * calling <code>perform</code> on the <code>TextBufferEditor</code>.
 * <p>
 * After a <code>TextEdit</code> has been added to a <code>TextBufferEditor</code>
 * the method <code>connect</code> is sent to the text edit. A <code>TextEdit</code>
 * is allowed to do some adjustments of the text range it is going to manipulate while inside
 * the hook <code>connect</code>.
 * 
 * @see TextBufferEditor
 */
public abstract class TextEdit {
	
	// index that determines the insertion order into a text buffer
	/* package */ int index;
	/* package */ boolean isSynthetic;
	
	/**
	 * Connects this text edit to the given <code>TextBufferEditor</code>. A text edit 
	 * must not keep a reference to the passed text buffer editor. It is guaranteed that 
	 * the buffer passed to <code>perform<code> is equal to the buffer managed by
	 * the given text buffer editor. But they don't have to be identical.
	 * <p>
	 * Note that this method <b>should only be called</b> by a <code>
	 * TextBufferEditor</code>.
	 *<p>
	 * This default implementation does nothing. Subclasses may override
	 * if needed.
	 *  
	 * @param editor the text buffer editor this text edit has been added to
	 */
	public void connect(TextBufferEditor editor) throws CoreException {
		// does nothing
	}
	
	/**
	 * Returns the <code>TextRange</code> that this text edit is going to
	 * manipulate. If this method is called before the <code>TextEdit</code>
	 * has been added to a <code>TextBufferEditor</code> it may return <code>
	 * null</code> or <code>TextRange.UNDEFINED</code> to indicate this situation.
	 * 
	 * @return the <code>TextRange</code>s this <code>TextEdit is going
	 * 	to manipulate
	 */
	public abstract TextRange getTextRange();
	
	/**
	 * Performs the text edit. Note that this method <b>should only be called</b> 
	 * by a <code>TextBufferEditor</code>. 
	 * 
	 * @param buffer the actual buffer to manipulate
	 * @return a text edit that can undo this text edit
	 */
	public abstract TextEdit perform(TextBuffer buffer) throws CoreException;
	
	/**
	 * This method gets called after all <code>TextEdit</code>s added to a text buffer
	 * editor are executed. Implementors of this method can do some clean-up or can 
	 * release allocated resources that are now longer needed.
	 * <p>
	 * This default implementation does nothing.
	 */
	public void performed() {
		// do nothing
	}
		
	/**
     * Creates and returns a copy of this object. The copy method should
     * be implemented in a way so that the copy can be added to a different 
     * <code>TextBufferEditor</code> without causing any harm to the object 
     * from which the copy has been created.
     * 
     * @return a copy of this object.
     */
	public abstract TextEdit copy() throws CoreException;	
	
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
	
	/** @deprecated reimplement getModifiedElement */
	public final Object getModifiedLanguageElement() {
		return null;
	}	
}

