/*******************************************************************************
 * Copyright (c) 2006, 2015 Red Hat, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.autotools.ui.editors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;

public class AutoconfPartitionScanner extends RuleBasedPartitionScanner {

	public final static String AUTOCONF_MACRO = "autoconf_macro"; //$NON-NLS-1$
	public final static String AUTOCONF_COMMENT = "autoconf_comment"; //$NON-NLS-1$
	final static String[] AUTOCONF_PARTITION_TYPES = new String[] { AUTOCONF_MACRO, AUTOCONF_COMMENT };

	/**
	 * Creates the partitioner and sets up the appropriate rules.
	 */
	public AutoconfPartitionScanner() {
		super();

		List<IRule> rules = new ArrayList<>();
		Token macro = new Token(AUTOCONF_MACRO);
		Token comment = new Token(AUTOCONF_COMMENT);

		// Add rule for target bodies.
		rules.add(new AutoconfMacroPartitionRule(macro));

		rules.add(new EndOfLineRule("dnl", comment)); //$NON-NLS-1$
		rules.add(new SingleLineRule("\\#", null, Token.UNDEFINED));
		rules.add(new EndOfLineRule("#", comment, '\\')); //$NON-NLS-1$

		// We want to process identifiers that might have macro
		// names inside them.
		rules.add(new AutoconfIdentifierRule(Token.UNDEFINED));

		IPredicateRule[] result = new IPredicateRule[rules.size()];
		rules.toArray(result);
		setPredicateRules(result);
	}

}
