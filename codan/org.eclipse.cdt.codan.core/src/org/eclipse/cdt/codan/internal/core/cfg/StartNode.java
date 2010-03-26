package org.eclipse.cdt.codan.internal.core.cfg;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.codan.provisional.core.model.cfg.IBasicBlock;
import org.eclipse.cdt.codan.provisional.core.model.cfg.IStartNode;

/**
 * Start node has no prev, one jump and it is connect to function exits
 *
 */
public class StartNode extends AbstarctBasicBlock implements IStartNode {
	private IBasicBlock next;
	private List<IBasicBlock> exitNodes;

	public StartNode(IBasicBlock next, Collection<IBasicBlock> exitNodes) {
		super();
		this.next = next;
		if (exitNodes != null) this.exitNodes = Collections.unmodifiableList(new ArrayList<IBasicBlock>(exitNodes));
		else this.exitNodes = null; // incomplete node
	}

	@SuppressWarnings("unchecked")
	public Iterator<IBasicBlock> getIncomingIterator() {
		return Collections.EMPTY_LIST.iterator();
	}

	public Iterator<IBasicBlock> getOutgoingIterator() {
		return new OneElementIterator<IBasicBlock>(next);
	}

	public int getIncomingSize() {
		return 0;
	}

	public int getOutgoingSize() {
		return 1;
	}

	public IBasicBlock getOutgoing() {
		return next;
	}

	public Iterator<IBasicBlock> getExitNodeIterator() {
		return exitNodes.iterator();
	}

	public int getExitNodeSize() {
		return exitNodes.size();
	}

	public void setOutgoing(IBasicBlock next) {
		if (this.next != null) throw new IllegalArgumentException("Cannot modify already exiting connector"); //$NON-NLS-1$
		this.next = next;
	}

	public void setExitNodes(Collection<IBasicBlock> exitNodes) {
		if (this.exitNodes != null) throw new IllegalArgumentException("Cannot modify already exiting connector"); //$NON-NLS-1$
		this.exitNodes = Collections.unmodifiableList(new ArrayList<IBasicBlock>(exitNodes));
	}
}
