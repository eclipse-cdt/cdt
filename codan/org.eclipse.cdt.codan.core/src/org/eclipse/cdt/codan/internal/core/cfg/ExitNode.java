package org.eclipse.cdt.codan.internal.core.cfg;

import java.util.Collections;
import java.util.Iterator;
import org.eclipse.cdt.codan.provisional.core.model.cfg.IBasicBlock;
import org.eclipse.cdt.codan.provisional.core.model.cfg.IExitNode;
import org.eclipse.cdt.codan.provisional.core.model.cfg.IStartNode;

/**
 * Plain node has one prev one jump
 * 
 */
public class ExitNode extends AbstractBasicBlock implements IExitNode {
	private IBasicBlock prev;
	private IStartNode start;

	public ExitNode(IBasicBlock prev, IStartNode start) {
		super();
		this.prev = prev;
		this.start = start;
	}

	public Iterator<IBasicBlock> getIncomingIterator() {
		return new OneElementIterator<IBasicBlock>(prev);
	}

	@SuppressWarnings("unchecked")
	public Iterator<IBasicBlock> getOutgoingIterator() {
		return Collections.EMPTY_LIST.iterator();
	}

	public int getIncomingSize() {
		return 1;
	}

	public int getOutgoingSize() {
		return 0;
	}

	public IBasicBlock getIncoming() {
		return prev;
	}

	public IStartNode getStartNode() {
		return start;
	}

	public void setIncoming(IBasicBlock prev) {
		this.prev = prev;
	}

	public void setStartNode(IStartNode start) {
		this.start = start;
	}

	@Override
	public void addOutgoing(IBasicBlock node) {
		throw new UnsupportedOperationException();
	}
}
