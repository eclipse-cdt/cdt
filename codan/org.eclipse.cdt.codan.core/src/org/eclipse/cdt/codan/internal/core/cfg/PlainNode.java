package org.eclipse.cdt.codan.internal.core.cfg;

import java.util.Iterator;

import org.eclipse.cdt.codan.core.model.cfg.IBasicBlock;
import org.eclipse.cdt.codan.core.model.cfg.IPlainNode;

/**
 * Plain node has one incoming arc and one outgoing arc
 * 
 */
public class PlainNode extends AbstractSingleIncomingNode implements IPlainNode {
	protected IBasicBlock next;

	protected PlainNode() {
		super();
	}

	public Iterator<IBasicBlock> getOutgoingIterator() {
		return new OneElementIterator<IBasicBlock>(next);
	}

	public int getOutgoingSize() {
		return 1;
	}

	public IBasicBlock getOutgoing() {
		return next;
	}

	public void setOutgoing(IBasicBlock exit) {
		this.next = exit;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.cdt.codan.internal.core.cfg.AbstractBasicBlock#addOutgoing
	 * (org.eclipse.cdt.codan.core.model.cfg.IBasicBlock)
	 */
	@Override
	public void addOutgoing(IBasicBlock node) {
		setOutgoing(node);
	}
}
