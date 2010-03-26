package org.eclipse.cdt.codan.internal.core.cfg;

import java.util.Iterator;

import org.eclipse.cdt.codan.provisional.core.model.cfg.IBasicBlock;
import org.eclipse.cdt.codan.provisional.core.model.cfg.IPlainNode;

/**
 * Plain node has one prev one jump
 *
 */
public class PlainNode extends AbstarctBasicBlock implements IPlainNode {
	final IBasicBlock prev;
	IBasicBlock next;

	public PlainNode(IBasicBlock entry, IBasicBlock exit) {
		super();
		this.prev = entry;
		this.next = exit;
	}

	public Iterator<IBasicBlock> getIncomingIterator() {
		return new OneElementIterator<IBasicBlock>(prev);
	}

	public Iterator<IBasicBlock> getOutgoingIterator() {
		return new OneElementIterator<IBasicBlock>(next);
	}

	public int getIncomingSize() {
		return 1;
	}

	public int getOutgoingSize() {
		return 1;
	}

	public IBasicBlock getIncoming() {
		return prev;
	}

	public IBasicBlock getOutgoing() {
		return next;
	}

	public void setOutgoing(IBasicBlock exit) {
		if (this.next != null) throw new IllegalArgumentException("Cannot modify already exiting connector"); //$NON-NLS-1$
		this.next = exit;
	}
}
