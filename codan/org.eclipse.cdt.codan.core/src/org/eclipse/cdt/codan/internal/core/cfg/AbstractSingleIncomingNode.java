package org.eclipse.cdt.codan.internal.core.cfg;

import java.util.Iterator;
import org.eclipse.cdt.codan.provisional.core.model.cfg.IBasicBlock;
import org.eclipse.cdt.codan.provisional.core.model.cfg.ISingleIncoming;

/**
 * Abstrat node with one incoming arc
 * 
 */
public abstract class AbstractSingleIncomingNode extends AbstractBasicBlock
		implements ISingleIncoming {
	final IBasicBlock prev;

	public AbstractSingleIncomingNode(IBasicBlock prev) {
		super();
		this.prev = prev;
	}

	public Iterator<IBasicBlock> getIncomingIterator() {
		return new OneElementIterator<IBasicBlock>(prev);
	}

	public int getIncomingSize() {
		return 1;
	}

	public IBasicBlock getIncoming() {
		return prev;
	}
}
