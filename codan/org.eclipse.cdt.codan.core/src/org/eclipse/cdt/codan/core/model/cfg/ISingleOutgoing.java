package org.eclipse.cdt.codan.core.model.cfg;

/**
 * Node with one outgoing arc
 */
public interface ISingleOutgoing {
	/**
	 * @return outgoing node
	 */
	IBasicBlock getOutgoing();
}
