package org.eclipse.cdt.codan.internal.core.cfg;

import java.util.Iterator;

import org.eclipse.cdt.codan.provisional.core.model.cfg.IBasicBlock;
import org.eclipse.cdt.codan.provisional.core.model.cfg.IConnectorNode;
import org.eclipse.cdt.codan.provisional.core.model.cfg.IJumpNode;

/**
 * Plain node has one prev one jump
 *
 */
public class JumpNode extends AbstarctBasicBlock implements IJumpNode {
	final private IBasicBlock entry;
	private IBasicBlock jump;
	private boolean backward;

	public JumpNode(IBasicBlock entry, IBasicBlock jump, boolean backward) {
		super();
		this.entry = entry;
		this.jump = jump;
		this.backward = backward;
	}

	public Iterator<IBasicBlock> getIncomingIterator() {
		return new OneElementIterator<IBasicBlock>(entry);
	}

	public Iterator<IBasicBlock> getOutgoingIterator() {
		return new OneElementIterator<IBasicBlock>(jump);
	}

	public int getIncomingSize() {
		return 1;
	}

	public int getOutgoingSize() {
		return 1;
	}

	public IBasicBlock getIncoming() {
		return entry;
	}

	public IBasicBlock getOutgoing() {
		return jump;
	}

	public boolean isBackwardArc() {
		return backward;
	}

	public void setJump(IBasicBlock jump) {
		if (!(jump instanceof IConnectorNode))
			throw new IllegalArgumentException("Jump target must be a connection node"); //$NON-NLS-1$
		this.jump = jump;
	}

	public void setBackward(boolean backward) {
		this.backward = backward;
	}

}
