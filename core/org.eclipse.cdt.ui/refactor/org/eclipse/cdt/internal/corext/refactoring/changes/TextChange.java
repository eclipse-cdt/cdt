/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.corext.refactoring.changes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.internal.corext.Assert;
import org.eclipse.cdt.internal.corext.textmanipulation.GroupDescription;
import org.eclipse.cdt.internal.corext.textmanipulation.TextBuffer;
import org.eclipse.cdt.internal.corext.textmanipulation.TextRegion;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.IRegion;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.TextEditCopier;

public abstract class TextChange extends AbstractTextChange {

	public static class EditChange {
		private boolean fIsActive;
		private TextChange fTextChange;
		private GroupDescription fDescription;
		
		/* package */ EditChange(GroupDescription description, TextChange change) {
			fTextChange= change;
			fIsActive= true;
			fDescription= description;
		}
		public String getName() {
			return fDescription.getName();
		}
		public void setActive(boolean active) {
			fIsActive= active;
		}
		public boolean isActive() {
			return fIsActive;
		}
		public TextChange getTextChange() {
			return fTextChange;
		}
		public IRegion getTextRange() {
			return fDescription.getTextRange();
		}
		public boolean isEmpty() {
			return fDescription.hasTextEdits();
		}
		/* package */ GroupDescription getGroupDescription() {
			return fDescription;
		}
		public boolean coveredBy(IRegion sourceRegion) {
			int sLength= sourceRegion.getLength();
			if (sLength == 0)
				return false;
			int sOffset= sourceRegion.getOffset();
			int sEnd= sOffset + sLength - 1;
			TextEdit[] edits= fDescription.getTextEdits();
			for (int i= 0; i < edits.length; i++) {
				TextEdit edit= edits[i];
				if (edit.isDeleted())
					return false;
				int rOffset= edit.getOffset();
				int rLength= edit.getLength();
				int rEnd= rOffset + rLength - 1;
			    if (rLength == 0) {
					if (!(sOffset < rOffset && rOffset <= sEnd))
						return false;
				} else {
					if (!(sOffset <= rOffset && rEnd <= sEnd))
						return false;
				}
			}
			return true;
		}
	}
	
	private List fTextEditChanges;
	private TextEditCopier fCopier;
	private TextEdit fEdit;
	private boolean fKeepExecutedTextEdits;
	private boolean fAutoMode;
	private String fTextType;

	/**
	 * Creates a new <code>TextChange</code> with the given name.
	 * 
	 * @param name the change's name mainly used to render the change in the UI.
	 */
	protected TextChange(String name) {
		super(name, ORIGINAL_CHANGE);
		fTextEditChanges= new ArrayList(5);
		fTextType= "txt"; //$NON-NLS-1$
	}
	
	/**
	 * Sets the text type. Text types are defined by the extension
	 * point >>TODO<<. 
	 * 
	 * @param type the text type. If <code>null</code> is passed the
	 *  text type is resetted to the default text type "text".
	 */
	protected void setTextType(String type) {
		if (type == null)
			fTextType= "txt"; //$NON-NLS-1$
		fTextType= type;
	}
	
	/**
	 * Returns the text change's text type.
	 * 
	 * @return the text change's text type
	 */
	public String getTextType() {
		return fTextType;
	}
	
	/**
	 * Adds a text edit object to this text buffer change.
	 *
	 * @param name the name of the given text edit. The name is used to render this 
	 * 	change in the UI.
	 * @param edit the text edit to add
	 */
	public void addTextEdit(String name, TextEdit edit) {
		addTextEdit(name, new TextEdit[] {edit});
	}
	
	/**
	 * Adds an array of  text edit objects to this text buffer change.
	 *
	 * @param name the name of the given text edit. The name is used to render this 
	 * 	change in the UI.
	 * @param edite the array of text edits to add
	 */
	public void addTextEdit(String name, TextEdit[] edits) {
		Assert.isNotNull(name);
		Assert.isNotNull(edits);
		GroupDescription description= new GroupDescription(name, edits);
		fTextEditChanges.add(new EditChange(description, this));
		if (fEdit == null) {
			fEdit= new MultiTextEdit();
			fAutoMode= true;
		} else {
			Assert.isTrue(fAutoMode, "Can only add edits when in auto organizing mode"); //$NON-NLS-1$
		}
		for (int i= 0; i < edits.length; i++) {
			insert(fEdit, edits[i]);
		}
	}
	
	/**
	 * Sets the root text edit.
	 * 
	 * @param edit the root text edit
	 */
	public void setEdit(TextEdit edit) {
		Assert.isTrue(fEdit == null, "Root edit can only be set once"); //$NON-NLS-1$
		Assert.isTrue(edit != null);
		fEdit= edit;
		fTextEditChanges=  new ArrayList(5);
		fAutoMode= false;
	}
	
	/**
	 * Gets the root text edit.
	 * 
	 * @return Returns the root text edit
	 */
	public TextEdit getEdit() {
		return fEdit;
	}	
	
	/**
	 * Adds a group description.
	 * 
	 * @param description the group description to be added
	 */
	public void addGroupDescription(GroupDescription description) {
		Assert.isTrue(fEdit != null, "Can only add a description if a root edit exists"); //$NON-NLS-1$
		Assert.isTrue(!fAutoMode, "Group descriptions are only supported if root edit has been set by setEdit"); //$NON-NLS-1$
		Assert.isTrue(description != null);
		fTextEditChanges.add(new EditChange(description, this));
	}
	
	/**
	 * Adds a set of group descriptions.
	 * 
	 * @param descriptios the group descriptions to be added
	 */
	public void addGroupDescriptions(GroupDescription[] descriptions) {
		for (int i= 0; i < descriptions.length; i++) {
			addGroupDescription(descriptions[i]);
		}
	}
	
	/**
	 * Returns the group descriptions that have been added to this change
	 * @return GroupDescription[]
	 */	
	public GroupDescription[] getGroupDescriptions() {
		GroupDescription[] res= new GroupDescription[fTextEditChanges.size()];
		for (int i= 0; i < res.length; i++) {
			EditChange elem= (EditChange) fTextEditChanges.get(i);
			res[i]= elem.getGroupDescription();
		}
		return res;	
	}
	
	/**
	 * Returns the group description with the given name or <code>null</code> if no such
	 * GroupDescription exists.
	 */	
	public GroupDescription getGroupDescription(String name) {
		for (int i= 0; i < fTextEditChanges.size(); i++) {
			EditChange elem= (EditChange) fTextEditChanges.get(i);
			GroupDescription description= elem.getGroupDescription();
			if (name.equals(description.getName())) {
				return description;
			}
		}
		return null;	
	}	
	
	
	/**
	 * Returns the text edit changes managed by this text change.
	 * 
	 * @return the text edit changes
	 */
	public EditChange[] getTextEditChanges() {
		return (EditChange[])fTextEditChanges.toArray(new EditChange[fTextEditChanges.size()]);
	}

	/**
	 * Returns the text this change is working on.
	 * 
	 * @return the original text.
	 * @exception CModelException if text cannot be accessed
	 */
	public String getCurrentContent() throws CModelException {
		TextBuffer buffer= null;
		try {
			buffer= acquireTextBuffer();
			return buffer.getContent();
		} catch (CModelException e){
			throw e;
		} catch (CoreException e) {
			throw new CModelException(e);
		} finally {
			if (buffer != null)
				releaseTextBuffer(buffer);
		}
	}
	
	/**
	 * Returns a preview of the change without actually modifying the underlying text.
	 * 
	 * @return the change's preview
	 * @exception CModelException if the preview could not be created
	 */
	public String getPreviewContent() throws CModelException {
		return getPreviewTextBuffer().getContent();
	}
	
	/**
	 * Note: API is under construction
	 */
	public TextBuffer getPreviewTextBuffer() throws CModelException {
		try {
			LocalTextEditProcessor editor= new LocalTextEditProcessor(createTextBuffer());
			addTextEdits(editor);
			editor.performEdits(new NullProgressMonitor());
			return editor.getTextBuffer();
		} catch (CModelException e){
			throw e;
		} catch (CoreException e) {
			throw new CModelException(e);
		}
	}
	
	/**
	 * Returns the current content to be modified by the given text edit change. The
	 * text edit change must have been added to this <code>TextChange<code>
	 * by calling <code>addTextEdit()</code>
	 * 
	 * @param change the text edit change for which the content is requested
	 * @param surroundingLines the number of surrounding lines added at top and bottom
	 * 	of the content
	 * @return the current content to be modified by the given text edit change
	 */
	public String getCurrentContent(EditChange change, int surroundingLines) throws CoreException {
		return getContent(change, surroundingLines, false);
	}
	
	/**
	 * Returns the preview of the given text edit change. The text edit change must 
	 * have been added to this <code>TextChange<code> by calling <code>addTextEdit()
	 * </code>
	 * 
	 * @param change the text edit change for which the preview is requested
	 * @param surroundingLines the number of surrounding lines added at top and bottom
	 * 	of the preview
	 * @return the preview of the given text edit change
	 */
	public String getPreviewContent(EditChange change, int surroundingLines) throws CoreException {
		return getContent(change, surroundingLines, true);
	}

	/**
	 * Returns the current content denoted by the given <code>range</code>.
	 * 
	 * @param range the range describing the content to be returned
	 * @return the current content denoted by the given <code>range</code>
	 */	
	public String getCurrentContent(IRegion range) throws CoreException {
		TextBuffer buffer= null;
		try {
			buffer= acquireTextBuffer();
			int offset= buffer.getLineInformationOfOffset(range.getOffset()).getOffset();
			int length= range.getLength() + range.getOffset() - offset;
			return buffer.getContent(offset, length);
		} finally {
			if (buffer != null)
				releaseTextBuffer(buffer);
		}
	}

	/**
	 * Returns a preview denoted by the given <code>range</code>. First the changes
	 * passed in argument <code>changes</code> are applied and then a string denoted
	 * by <code>range</code> is extracted from the result.
	 * 
	 * @param changes the changes to apply
	 * @param range the range denoting the resulting string.
	 * @return the computed preview 
	 */	
	public String getPreviewContent(EditChange[] changes, IRegion range) throws CoreException {
		TextBuffer buffer= createTextBuffer();
		LocalTextEditProcessor editor= new LocalTextEditProcessor(buffer);
		addTextEdits(editor, changes);
		int oldLength= buffer.getLength();
		editor.performEdits(new NullProgressMonitor());
		int delta= buffer.getLength() - oldLength;
		int offset= buffer.getLineInformationOfOffset(range.getOffset()).getOffset();
		int length= range.getLength() + range.getOffset() - offset + delta;
		if (length > 0) {
			return buffer.getContent(offset, length);
		} else {
			// range got removed
			return ""; //$NON-NLS-1$
		}
	}

	/**
	 * Controls whether the text change should keep executed edits. If set to <code>true</code>
	 * a call to <code>getExecutedTextEdit(TextEdit original)</code> will return the executed edit
	 * associated with the original edit.
	 * 
	 * @param keep if <code>true</code> executed edits are kept
	 */
	public void setKeepExecutedTextEdits(boolean keep) {
		fKeepExecutedTextEdits= keep;
		if (!fKeepExecutedTextEdits)
			fCopier= null;
	}
	
	/**
	 * Returns the edit that got copied and executed instead of the orignial.
	 * 
	 * @return the executed edit
	 */
	private TextEdit getExecutedTextEdit(TextEdit original) {
		if (!fKeepExecutedTextEdits || fCopier == null)
			return null;
		return fCopier.getCopy(original);
	}
	
	/**
	 * Returns the text range of the given text edit. If the change doesn't 
	 * manage the given text edit or if <code>setTrackPositionChanges</code> 
	 * is set to <code>false</code> <code>null</code> is returned.
	 * 
	 * <p>
	 * Note: API is under construction
	 * </P>
	 */
	public IRegion getNewTextRange(TextEdit edit) {
		Assert.isNotNull(edit);
		TextEdit result= getExecutedTextEdit(edit);
		if (result == null)
			return null;
		return result.getRegion();
	}
	
	/**
	 * Returns the new text range for given text edits. If <code>setTrackPositionChanges</code> 
	 * is set to <code>false</code> <code>null</code> is returned.
	 * 
	 * <p>
	 * Note: API is under construction
	 * </P>
	 */
	public IRegion getNewTextRange(TextEdit[] edits) {
		Assert.isTrue(edits != null && edits.length > 0);
		if (!fKeepExecutedTextEdits || fCopier == null)
			return null;		
		
		TextEdit[] copies= new TextEdit[edits.length];
		for (int i= 0; i < edits.length; i++) {
			TextEdit copy= fCopier.getCopy(edits[i]);
			if (copy == null)
				return null;
			copies[i]= copy;
		}
		return TextEdit.getCoverage(copies);
	}	
	
	/**
	 * Note: API is under construction
	 */
	public IRegion getNewTextRange(EditChange editChange) {
		return getNewTextRange(editChange.getGroupDescription().getTextEdits());
	}
	
	/* (Non-Javadoc)
	 * Method declared in IChange.
	 */
	public void setActive(boolean active) {
		super.setActive(active);
		for (Iterator iter= fTextEditChanges.iterator(); iter.hasNext();) {
			EditChange element= (EditChange) iter.next();
			element.setActive(active);
		}
	}
	
	/* non Java-doc
	 * @see AbstractTextChange#addTextEdits
	 */
	protected void addTextEdits(LocalTextEditProcessor editor) throws CoreException {
		if (fEdit == null)
			return;
		List excludes= new ArrayList(0);
		for (Iterator iter= fTextEditChanges.iterator(); iter.hasNext(); ) {
			EditChange edit= (EditChange)iter.next();
			if (!edit.isActive()) {
				excludes.addAll(Arrays.asList(edit.getGroupDescription().getTextEdits()));
			}
		}
		fCopier= new TextEditCopier(fEdit);
		TextEdit copiedEdit= fCopier.perform();
		if (copiedEdit != null) {
			editor.add(copiedEdit);
			editor.setExcludes(mapEdits(
				(TextEdit[])excludes.toArray(new TextEdit[excludes.size()]),
				fCopier));	
		}
		if (!fKeepExecutedTextEdits)
			fCopier= null;
	}
	
	protected void addTextEdits(LocalTextEditProcessor editor, EditChange[] changes) throws CoreException {
		if (fEdit == null)
			return;
		List includes= new ArrayList(0);
		for (int c= 0; c < changes.length; c++) {
			EditChange change= changes[c];
			Assert.isTrue(change.getTextChange() == this);
			if (change.isActive()) {
				includes.addAll(Arrays.asList(change.getGroupDescription().getTextEdits()));
			}
		}
		fCopier= new TextEditCopier(fEdit);
		TextEdit copiedEdit= fCopier.perform();
		if (copiedEdit != null) {
			editor.add(copiedEdit);
			editor.setIncludes(mapEdits(
				(TextEdit[])includes.toArray(new TextEdit[includes.size()]),
				fCopier));
		}		
		if (!fKeepExecutedTextEdits)
			fCopier= null;
	}
	
	private TextEdit[] mapEdits(TextEdit[] edits, TextEditCopier copier) {
		if (edits == null)
			return null;
		for (int i= 0; i < edits.length; i++) {
			edits[i]= copier.getCopy(edits[i]);
		}
		return edits;
	}
	
	private String getContent(EditChange change, int surroundingLines, boolean preview) throws CoreException {
		Assert.isTrue(change.getTextChange() == this);
		TextBuffer buffer= createTextBuffer();
		IRegion range= null;
		if (preview) {
			LocalTextEditProcessor editor= new LocalTextEditProcessor(buffer);
			boolean keepEdits= fKeepExecutedTextEdits;
			setKeepExecutedTextEdits(true);
			addTextEdits(editor, new EditChange[] { change });
			editor.performEdits(new NullProgressMonitor());
			range= getNewTextRange(change);
			setKeepExecutedTextEdits(keepEdits);
		} else {
			range= change.getTextRange();
		}
		int startLine= Math.max(buffer.getLineOfOffset(range.getOffset()) - surroundingLines, 0);
		int endLine= Math.min(
			buffer.getLineOfOffset(range.getOffset() + range.getLength() - 1) + surroundingLines,
			buffer.getNumberOfLines() - 1);
		int offset= buffer.getLineInformation(startLine).getOffset();
		TextRegion region= buffer.getLineInformation(endLine);
		int length = region.getOffset() + region.getLength() - offset;
		return buffer.getContent(offset, length);
	}

	private static void insert(TextEdit parent, TextEdit edit) {
		if (!parent.hasChildren()) {
			parent.addChild(edit);
			return;
		}
		TextEdit[] children= parent.getChildren();
		// First dive down to find the right parent.
		for (int i= 0; i < children.length; i++) {
			TextEdit child= children[i];
			if (covers(child, edit)) {
				insert(child, edit);
				return;
			}
		}
		// We have the right parent. Now check if some of the children have to
		// be moved under the new edit since it is covering it.
		for (int i= children.length - 1; i >= 0; i--) {
			TextEdit child= children[i];
			if (covers(edit, child)) {
				parent.removeChild(i);
				edit.addChild(child);
			}
		}
		parent.addChild(edit);
	}
	
	private static boolean covers(TextEdit thisEdit, TextEdit otherEdit) {
		if (thisEdit.getLength() == 0)	// an insertion point can't cover anything
			return false;
		
		int thisOffset= thisEdit.getOffset();
		int thisEnd= thisEdit.getExclusiveEnd();	
		if (otherEdit.getLength() == 0) {
			int otherOffset= otherEdit.getOffset();
			return thisOffset < otherOffset && otherOffset < thisEnd;
		} else {
			int otherOffset= otherEdit.getOffset();
			int otherEnd= otherEdit.getExclusiveEnd();
			return thisOffset <= otherOffset && otherEnd <= thisEnd;
		}
	}		
}

