package org.eclipse.cdt.internal.corext.textmanipulation;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.util.Assert;


/**
 * A helper class to arrange <code>TextEdit</code>s into a tree to optimize their
 * execution.
 */
/* package */ abstract class TextEditNode {

	/* package */ TextEditNode fParent;
	/* package */ List fChildren;
	/* package */ TextEdit fEdit;
	
	/* package */ static class DefaultNode extends TextEditNode {
		public DefaultNode(TextEdit edit) {
			super(edit);
		}
	}
	
	/* package */ static class RootNode extends TextEditNode {
		private int fUndoIndex;
		public RootNode(int length) {
			super(new NopTextEdit(new TextRange(0, length)));
			fEdit.isSynthetic= true;
		}
		public boolean covers(TextEditNode node) {
			return true;
		}
		public UndoMemento performDo(TextBuffer buffer, IProgressMonitor pm) throws CoreException {
			DoRangeUpdater updater= new DoRangeUpdater();
			UndoMemento undo= new UndoMemento(TextBufferEditor.UNDO);
			try {
				buffer.registerUpdater(updater);
				performDo(buffer, updater, undo, pm);
			} finally {
				buffer.unregisterUpdater(updater);
				updater.setActiveNode(null);
			}
			return undo;
		}
		public UndoMemento performUndo(TextBuffer buffer, IProgressMonitor pm) throws CoreException {
			UndoRangeUpdater updater= new UndoRangeUpdater(this);
			UndoMemento undo= new UndoMemento(TextBufferEditor.REDO);
			try {
				buffer.registerUpdater(updater);
				performUndo(buffer, updater, undo, pm);
			} finally {
				buffer.unregisterUpdater(updater);
				updater.setActiveNode(null);
			}
			return undo;
		}
		
		protected void setUndoIndex(int index) {
			fUndoIndex= index;
		}
		
		protected int getUndoIndex() {
			return fUndoIndex;
		}
	}
	
	/* package */ abstract static class AbstractMoveNode extends TextEditNode {
		private int state;		
		
		private int fTargetIndex;
		private int fSourceIndex;
		
		private List fAffectedChildren;
		
		public AbstractMoveNode(TextEdit edit) {
			super(edit);
			reset();
		}
		protected abstract TextRange getSourceRange();
		protected abstract TextRange getTargetRange();
		protected abstract boolean isUpMove();
		protected boolean isDownMove() {
			return !isUpMove();
		}
		public boolean isMove() {
			return true;
		}
		protected void checkRange(DocumentEvent event) {
			TextRange range= getChildRange();
			int eventOffset= event.getOffset();
			int eventLength= event.getLength();
			int eventEnd = eventOffset + eventLength - 1;
			// "Edit changes text that lies outside its defined range"
			Assert.isTrue(range.fOffset <= eventOffset && eventEnd <= range.getInclusiveEnd());
		}
		protected boolean activeNodeChanged(int delta) {
			TextRange targetRange= getTargetRange();
			TextRange sourceRange= getSourceRange();
			switch (state) {
				case 0: // the move delete
					init();
					Assert.isTrue(Math.abs(delta) == sourceRange.fLength);
					if (isUpMove()) {
						updateOffset(fAffectedChildren, delta);
						targetRange.fOffset+= delta;
					}
					sourceRange.fLength= 0;
					state= 1;
					break;
				case 1:
					TextEditNode target= (TextEditNode)fParent.fChildren.get(fTargetIndex);
					TextEditNode source= (TextEditNode)fParent.fChildren.get(fSourceIndex);
					updateOffset(source.fChildren, targetRange.fOffset - sourceRange.fOffset);
					target.fChildren= source.fChildren;
					if (target.fChildren != null) {
						for (Iterator iter= target.fChildren.iterator(); iter.hasNext();) {
							((TextEditNode)iter.next()).fParent= target;
						}
					}
					source.fChildren= null;				
					if (isDownMove()) {
						updateOffset(fAffectedChildren, delta);
						sourceRange.fOffset+= delta;
					}
					targetRange.fLength= delta;
					reset();
					break;
			}
			return true;
		}
		private static void updateOffset(List nodes, int delta) {
			if (nodes == null)
				return;
			for (int i= nodes.size() - 1; i >= 0; i--) {
				TextEditNode node= (TextEditNode)nodes.get(i);
				TextRange range= node.getTextRange();
				range.fOffset+= delta;
				updateOffset(node.fChildren, delta);
			}
		}
		private void init() {
			TextRange source= getSourceRange();
			TextRange target= getTargetRange();
			List children= fParent.fChildren;
			for (int i= children.size() - 1; i >= 0; i--) {
				TextEditNode child= (TextEditNode)children.get(i);
				TextRange range= child.fEdit.getTextRange();
				if (range == source)
					fSourceIndex= i;
				else if (range == target)
					fTargetIndex= i;
			}
			int start= Math.min(fTargetIndex, fSourceIndex);
			int end= Math.max(fTargetIndex, fSourceIndex);
			fAffectedChildren= new ArrayList(3);
			for (int i= start + 1; i < end; i++) {
				fAffectedChildren.add(children.get(i));
			}
		}
		private void reset() {
			state= 0;
			fSourceIndex= -1;
			fTargetIndex= -1;
		}
	}
	
	/* package */ static class MoveNode extends AbstractMoveNode {
		public MoveNode(TextEdit edit) {
			super(edit);
		}
		protected TextRange getChildRange() {
			return ((MoveTextEdit)fEdit).getChildRange();
		}
		protected TextRange getSourceRange() {
			return ((MoveTextEdit)fEdit).getSourceRange();
		}
		protected TextRange getTargetRange() {
			return ((MoveTextEdit)fEdit).getTargetRange();
		}
		protected boolean isUpMove() {
			return ((MoveTextEdit)fEdit).isUpMove();
		}
		public boolean isMovePartner(TextEditNode other) {
			if (!(other instanceof TargetMarkNode))
				return false;
			return fEdit == ((MoveTextEdit.TargetMark)other.fEdit).getMoveTextEdit();
		}
		public boolean covers(TextEditNode node) {
			if (node instanceof TargetMarkNode) {
				MoveTextEdit.TargetMark edit= (MoveTextEdit.TargetMark)node.fEdit;
				if (edit.getMoveTextEdit() == fEdit)
					return false;
			}
			return getParentRange().covers(node.getChildRange());
		}
	}
	
	/* package */ static class TargetMarkNode extends AbstractMoveNode {
		public TargetMarkNode(TextEdit edit) {
			super(edit);
		}
		protected TextRange getChildRange() {
			return ((MoveTextEdit.TargetMark)fEdit).getMoveTextEdit().getChildRange();
		}
		protected TextRange getSourceRange() {
			return ((MoveTextEdit.TargetMark)fEdit).getMoveTextEdit().getSourceRange();
		}
		protected TextRange getTargetRange() {
			return ((MoveTextEdit.TargetMark)fEdit).getMoveTextEdit().getTargetRange();
		}
		protected boolean isUpMove() {
			return ((MoveTextEdit.TargetMark)fEdit).getMoveTextEdit().isUpMove();
		}
		public boolean isMovePartner(TextEditNode other) {
			return ((MoveTextEdit.TargetMark)fEdit).getMoveTextEdit() == other.fEdit;
		}
	}

	//---- Range updating ---------------------------------------------------------------------------

	private static abstract class RangeUpdater implements IDocumentListener {
		protected TextEditNode fActiveNode;
		public void documentAboutToBeChanged(DocumentEvent event) {
		}
		public void setActiveNode(TextEditNode node) {
			fActiveNode= node;
		}
		public void updateParents(int delta) {
			TextEditNode node= fActiveNode.fParent;
			while (node != null) {
				node.childNodeChanged(delta);
				node= node.fParent;
			}
		}
		public static int getDelta(DocumentEvent event) {
			return (event.getText() == null ? 0 : event.getText().length()) - event.getLength();
		}
	}
	private static class DoRangeUpdater extends RangeUpdater {
		private List fProcessedNodes= new ArrayList(10);
		public void setActiveNode(TextEditNode node) {
			if (fActiveNode != null)
				fProcessedNodes.add(fActiveNode);
			super.setActiveNode(node);
		}
		public void documentChanged(DocumentEvent event) {
			fActiveNode.checkRange(event);
			int delta= getDelta(event);
			if (!fActiveNode.activeNodeChanged(delta)) {
				for (Iterator iter= fProcessedNodes.iterator(); iter.hasNext();) {
					((TextEditNode)iter.next()).previousNodeChanged(delta);
				}
			}
			updateParents(delta);
		}
	}
	private static class UndoRangeUpdater  extends RangeUpdater {
		private RootNode fRootNode;
		public UndoRangeUpdater(RootNode root) {
			fRootNode= root;
		}
		public void setActiveNode(TextEditNode node) {
			super.setActiveNode(node);
		}
		public void documentChanged(DocumentEvent event) {
			fActiveNode.checkRange(event);
			int delta= getDelta(event);
			if (!fActiveNode.activeNodeChanged(delta)) {
				int start= fRootNode.getUndoIndex() + 1;
				List children= fRootNode.fChildren;
				int size= children != null ? children.size() : 0;
				for (int i= start; i < size; i++) {
					updateUndo((TextEditNode)children.get(i), delta);
				}
			}
			updateParents(delta);
		}
		private void updateUndo(TextEditNode node, int delta) {
			node.previousNodeChanged(delta);
			List children= node.fChildren;
			int size= children != null ? children.size() : 0;
			for (int i= 0; i < size; i++) {
				updateUndo((TextEditNode)children.get(i), delta);
			}
		}
	}
	
	//---- Creating instances ---------------------------------------------------------------------------
	
	static TextEditNode create(TextEdit edit) {
		if (edit instanceof MoveTextEdit)
			return new MoveNode(edit);
		if (edit instanceof MoveTextEdit.TargetMark)
			return new TargetMarkNode(edit);
		return new DefaultNode(edit);
	}
	
	static RootNode createRoot(int length) {
		return new RootNode(length);
	}
	
	protected TextEditNode(TextEdit edit) {
		fEdit= edit;
	}
	
	//---- Adding children ---------------------------------------------------------------------------
	
	protected void add(TextEditNode node) {
		if (fChildren == null) {
			fChildren= new ArrayList(1);
			node.fParent= this;
			fChildren.add(node);
			return;
		}
		// Optimize using binary search
		for (Iterator iter= fChildren.iterator(); iter.hasNext();) {
			TextEditNode child= (TextEditNode)iter.next();
			if (child.covers(node)) {
				child.add(node);
				return;
			}
		}
		for (int i= 0; i < fChildren.size(); ) {
			TextEditNode child= (TextEditNode)fChildren.get(i);
			if (node.covers(child)) {
				fChildren.remove(i);
				node.add(child);
			} else {
				i++;
			}
		}
		node.fParent= this;
		fChildren.add(node);
	}
	
	public boolean covers(TextEditNode node) {
		return false; 
	}
	
	//---- Accessing --------------------------------------------------------------------------------------
	
	protected RootNode getRoot() {
		TextEditNode candidate= this;
		while(candidate.fParent != null)
			candidate= candidate.fParent;
		return (RootNode)candidate;
	}
	
	//---- Query interface --------------------------------------------------------------------------------
	
	protected boolean isSynthetic() {
		return fEdit.isSynthetic;
	}
	
	public boolean isMove() {
		return false;
	}
	
	//---- Accessing Ranges ------------------------------------------------------------------------------
	
	protected void checkRange(DocumentEvent event) {
		TextRange range= getTextRange();
		int eventOffset= event.getOffset();
		int eventLength= event.getLength();
		int eventEnd = eventOffset + eventLength - 1;
		// "Edit changes text that lies outside its defined range"
		Assert.isTrue(range.fOffset <= eventOffset && eventEnd <= range.getInclusiveEnd());
	}
	
	protected TextRange getTextRange() {
		return fEdit.getTextRange();
	}
	
	protected TextRange getChildRange() {
		return getTextRange();
	}
	
	protected TextRange getParentRange() {
		return getTextRange();
	}
		
	public boolean validate(int bufferLength) {
		if (fChildren == null)
			return true;
		// Only Moves and Nops can be parents	
		if (!(fEdit instanceof MoveTextEdit || fEdit instanceof NopTextEdit))
			return false;
		TextRange lastRange= null;
		for (Iterator iter= fChildren.iterator(); iter.hasNext(); ) {
			TextEditNode node= (TextEditNode)iter.next();
			if (!node.validate(bufferLength))
				return false;
			TextRange range= node.fEdit.getTextRange();
			if (!range.isValid() || range.fOffset + range.fLength > bufferLength)
				return false;
			if (lastRange != null && !(range.isInsertionPointAt(lastRange.fOffset) || range.liesBehind(lastRange)))
				return false;
			lastRange= range;
		}
		return true;
	}

	//---- Updating ----------------------------------------------------------------------------------------
	
	protected boolean activeNodeChanged(int delta) {
		TextRange range= getTextRange();
		range.fLength+= delta;
		// we didn't adjust any processed nodes.
		return false;
	}
	
	protected void previousNodeChanged(int delta) {
		TextRange range= getTextRange();
		range.fOffset+= delta;
	}
	
	protected void childNodeChanged(int delta) {
		getTextRange().fLength+= delta;
	}

	//---- Do it ---------------------------------------------------------------------------------------------
	
	protected void performDo(TextBuffer buffer, RangeUpdater updater, UndoMemento undo, IProgressMonitor pm) throws CoreException {
		int size= fChildren != null ? fChildren.size() : 0;
		for (int i= size - 1; i >= 0; i--) {
			TextEditNode child= (TextEditNode)fChildren.get(i);
			child.performDo(buffer, updater, undo, pm);
		}
		updater.setActiveNode(this);
		if (isSynthetic())
			fEdit.perform(buffer);
		else
			undo.add(fEdit.perform(buffer));
		pm.worked(1);
	}
	
	public void performedDo()  {
		int size= fChildren != null ? fChildren.size() : 0;
		for (int i= size - 1; i >= 0; i--) {
			TextEditNode child= (TextEditNode)fChildren.get(i);
			child.performedDo();
		}
		fEdit.performed();
	}
	
	//---- Undo it -------------------------------------------------------------------------------------------
	
	protected void performUndo(TextBuffer buffer, RangeUpdater updater, UndoMemento undo, IProgressMonitor pm) throws CoreException {
		int size= fChildren != null ? fChildren.size() : 0;
		for (int i= 0; i < size; i++) {
			setUndoIndex(i);
			TextEditNode child= (TextEditNode)fChildren.get(i);
			child.performUndo(buffer, updater, undo, pm);
		}
		updater.setActiveNode(this);
		if (isSynthetic())
			fEdit.perform(buffer);
		else
			undo.add(fEdit.perform(buffer));
		pm.worked(1);
	}
	
	protected void setUndoIndex(int index) {
	}
	
	public void performedUndo()  {
		int size= fChildren != null ? fChildren.size() : 0;
		for (int i= 0; i < size; i++) {
			TextEditNode child= (TextEditNode)fChildren.get(i);
			child.performedUndo();
		}
		fEdit.performed();
	}
	
//	protected void createUndoList(List list) {
//		int size= fChildren != null ? fChildren.size() : 0;
//		for (int i= 0; i < size; i++) {
//			TextEditNode child= (TextEditNode)fChildren.get(i);
//			child.createUndoList(list);
//		}
//		list.add(this);
//	}	
}

