/*******************************************************************************
 * Copyright (c) 2009 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.core.cfg;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.codan.provisional.core.model.cfg.IBasicBlock;
import org.eclipse.cdt.codan.provisional.core.model.cfg.IConnectorNode;
import org.eclipse.cdt.codan.provisional.core.model.cfg.IDecisionArc;
import org.eclipse.cdt.codan.provisional.core.model.cfg.IDecisionNode;

/**
 * @see {@link IDecisionNode}
 */
public class DecisionNode extends AbstractSingleIncomingNode implements
		IDecisionNode {
	private List<IDecisionArc> next = new ArrayList<IDecisionArc>(2);
	private IConnectorNode conn;

	/**
	 * @param prev
	 */
	public DecisionNode() {
		super();
	}

	public void setDecisionArcs(Collection<IDecisionArc> next) {
		this.next = Collections.unmodifiableList(new ArrayList<IDecisionArc>(
				next));
	}

	@Override
	public void addOutgoing(IBasicBlock node) {
		DecisionArc arc = new DecisionArc(this, getDecisionArcSize(), node);
		next.add(arc);
	}

	public void addOutgoing(IDecisionArc arc) {
		next.add(arc);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.cdt.codan.provisional.core.model.cfg.IDecisionNode#
	 * getDecisionArcs()
	 */
	public Iterator<IDecisionArc> getDecisionArcs() {
		return next.iterator();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.cdt.codan.provisional.core.model.cfg.IDecisionNode#
	 * getDecisionArcSize()
	 */
	public int getDecisionArcSize() {
		return next.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.cdt.codan.provisional.core.model.cfg.IBasicBlock#
	 * getOutgoingIterator()
	 */
	public Iterator<IBasicBlock> getOutgoingIterator() {
		return new Iterator<IBasicBlock>() {
			private Iterator<IDecisionArc> it;
			{
				it = next.iterator();
			}

			public boolean hasNext() {
				return it.hasNext();
			}

			public IBasicBlock next() {
				IDecisionArc arc = it.next();
				return arc.getOutgoing();
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.cdt.codan.provisional.core.model.cfg.IBasicBlock#getOutgoingSize
	 * ()
	 */
	public int getOutgoingSize() {
		return next.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.cdt.codan.provisional.core.model.cfg.IDecisionNode#
	 * getConnectionNode()
	 */
	public IConnectorNode getConnectionNode() {
		return conn;
	}

	public void setConnectorNode(IConnectorNode conn) {
		this.conn = conn;
	}
}
