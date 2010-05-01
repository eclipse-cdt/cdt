package org.eclipse.cdt.codan.core.model.cfg;

/**
 * Has one incoming, one outgoing connection. Usually expression statement or
 * declaration.
 */
public interface IPlainNode extends IBasicBlock, ISingleOutgoing,
		ISingleIncoming {
}
