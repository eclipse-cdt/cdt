package org.eclipse.cdt.codan.provisional.core.model.cfg;

import java.util.Iterator;

/**
 * 
 * Interface for decision node. This node represent condition node in the graph,
 * it has one incoming arc and many outgoing, each of them has a value of
 * condition associated with it.
 */
public interface IDecisionNode extends IBasicBlock, ISingleIncoming {
	Iterator<IDecisionArc> getDecisionArcs();

	int getDecisionArcSize();

	IConnectorNode getConnectionNode();
}
