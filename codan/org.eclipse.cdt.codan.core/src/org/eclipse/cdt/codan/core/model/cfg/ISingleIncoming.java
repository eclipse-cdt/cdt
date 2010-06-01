package org.eclipse.cdt.codan.core.model.cfg;

/**
 * Node with one incoming arc
 */
public interface ISingleIncoming {
	/**
	 * @return single incoming node
	 */
	IBasicBlock getIncoming();
}
