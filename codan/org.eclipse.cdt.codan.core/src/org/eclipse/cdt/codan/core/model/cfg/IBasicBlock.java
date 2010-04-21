package org.eclipse.cdt.codan.core.model.cfg;

import java.util.Iterator;

/**
 * 
 * Control flow graph basic block node - superinterface of all nodes. Each node
 * has iterator and size over incoming and outgoing arc
 * <p/>
 * The following are speciazed versions of the nodes:
 * <li>{@link IStartNode} - start node of the graph
 * <li>{@link I}
 */
public interface IBasicBlock {
	Iterator<IBasicBlock> getIncomingIterator();

	Iterator<IBasicBlock> getOutgoingIterator();

	int getIncomingSize();

	int getOutgoingSize();
}
