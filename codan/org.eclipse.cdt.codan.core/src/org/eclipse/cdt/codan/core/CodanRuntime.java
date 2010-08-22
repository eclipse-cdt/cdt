/*******************************************************************************
 * Copyright (c) 2009, 2010 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core;

import org.eclipse.cdt.codan.core.model.ICheckersRegistry;
import org.eclipse.cdt.codan.core.model.ICodanBuilder;
import org.eclipse.cdt.codan.core.model.IProblemLocationFactory;
import org.eclipse.cdt.codan.core.model.IProblemReporter;
import org.eclipse.cdt.codan.internal.core.CheckersRegistry;
import org.eclipse.cdt.codan.internal.core.CodanBuilder;
import org.eclipse.cdt.codan.internal.core.model.CodanMarkerProblemReporter;
import org.eclipse.cdt.codan.internal.core.model.ProblemLocationFactory;

/**
 * Runtime singleton class to get access to Codan framework parts
 * 
 * Clients may extend this class to override default framework parts.
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part
 * of a work in progress. There is no guarantee that this API will work or that
 * it will remain the same.
 * </p>
 */
public class CodanRuntime {
	private static CodanRuntime instance = new CodanRuntime();
	private IProblemReporter problemReporter = new CodanMarkerProblemReporter();
	private ICodanBuilder builder = new CodanBuilder();
	private CheckersRegistry checkers = CheckersRegistry.getInstance();
	private IProblemLocationFactory locFactory = new ProblemLocationFactory();

	/**
	 * CodanRuntime - only can be called by subclasses to override default
	 * constructor
	 */
	protected CodanRuntime() {
		// nothing here
	}

	/**
	 * Get runtime problem reporter. Default reported generated problem markers.
	 * 
	 * @return
	 */
	public IProblemReporter getProblemReporter() {
		return problemReporter;
	}

	/**
	 * Set different problem reporter.
	 * 
	 * @param reporter
	 */
	public void setProblemReporter(IProblemReporter reporter) {
		problemReporter = reporter;
	}

	/**
	 * Get instance of of Codan Runtime
	 * 
	 * @return
	 */
	public static CodanRuntime getInstance() {
		return instance;
	}

	/**
	 * Get builder. Builder can used to run code analysis on given resource
	 * using API.
	 * 
	 * @return
	 */
	public ICodanBuilder getBuilder() {
		return builder;
	}

	/**
	 * Get checkers registry.
	 * 
	 * @deprecated (misspelled) use getCheckersRegistry
	 * @return
	 */
	@Deprecated
	public ICheckersRegistry getChechersRegistry() {
		return checkers;
	}

	/**
	 * Get checkers registry.
	 * 
	 * @return
	 * @since 2.0
	 */
	public ICheckersRegistry getCheckersRegistry() {
		return checkers;
	}

	/**
	 * Get problem location factory.
	 * 
	 * @return
	 */
	public IProblemLocationFactory getProblemLocationFactory() {
		return locFactory;
	}

	/**
	 * Set another problem location factory - only need if default is not
	 * sufficient, i.e IProblemLocation is implemented differently
	 * 
	 * @param factory
	 */
	public void setProblemLocationFactory(IProblemLocationFactory factory) {
		locFactory = factory;
	}
}
