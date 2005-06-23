/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.internal.ui.text.makefile;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.Token;

public class MakefilePartitionScanner extends RuleBasedPartitionScanner {
	// Partition types
	public final static String MAKEFILE_COMMENT_PARTITION = "makefile_comment"; //$NON-NLS-1$
	public final static String MAKEFILE_MACRO_ASSIGNEMENT_PARTITION = "makefile_macro_assignement"; //$NON-NLS-1$
	public final static String MAKEFILE_INCLUDE_BLOCK_PARTITION = "makefile_include_block"; //$NON-NLS-1$
	public final static String MAKEFILE_IF_BLOCK_PARTITION = "makefile_if_block"; //$NON-NLS-1$
	public final static String MAKEFILE_DEF_BLOCK_PARTITION = "makefile_def_block"; //$NON-NLS-1$
	public final static String MAKEFILE_OTHER_PARTITION = "makefile_other"; //$NON-NLS-1$

	public final static String[] MAKE_PARTITIONS =
		new String[] {
			MAKEFILE_COMMENT_PARTITION,
			MAKEFILE_MACRO_ASSIGNEMENT_PARTITION,
			MAKEFILE_INCLUDE_BLOCK_PARTITION,
			MAKEFILE_IF_BLOCK_PARTITION,
			MAKEFILE_DEF_BLOCK_PARTITION,
			MAKEFILE_OTHER_PARTITION,
	};

	/** The predefined delimiters of this tracker */
	private char[][] fModDelimiters = { { '\r', '\n' }, { '\r' }, { '\n' } };

	/**
	 * Constructor for MakefilePartitionScanner
	 */
	public MakefilePartitionScanner() {
		super();

		IToken tComment = new Token(MAKEFILE_COMMENT_PARTITION);
		IToken tMacro = new Token(MAKEFILE_MACRO_ASSIGNEMENT_PARTITION);
		IToken tInclude = new Token(MAKEFILE_INCLUDE_BLOCK_PARTITION);
		IToken tIf = new Token(MAKEFILE_IF_BLOCK_PARTITION);
		IToken tDef = new Token(MAKEFILE_DEF_BLOCK_PARTITION);
		IToken tOther = new Token(MAKEFILE_OTHER_PARTITION);

		List rules = new ArrayList();

		// Add rule for single line comments.

		rules.add(new EndOfLineRule("#", tComment, '\\', true)); //$NON-NLS-1$

		rules.add(new EndOfLineRule("include", tInclude)); //$NON-NLS-1$

		rules.add(new EndOfLineRule("export", tDef)); //$NON-NLS-1$
		rules.add(new EndOfLineRule("unexport", tDef)); //$NON-NLS-1$
		rules.add(new EndOfLineRule("vpath", tDef)); //$NON-NLS-1$
		rules.add(new EndOfLineRule("override", tDef)); //$NON-NLS-1$
		rules.add(new MultiLineRule("define", "endef", tDef)); //$NON-NLS-1$ //$NON-NLS-2$
		rules.add(new MultiLineRule("override define", "endef", tDef)); //$NON-NLS-1$ //$NON-NLS-2$

		// Add rules for multi-line comments and javadoc.
		rules.add(new MultiLineRule("ifdef", "endif", tIf)); //$NON-NLS-1$ //$NON-NLS-2$
		rules.add(new MultiLineRule("ifndef", "endif", tIf)); //$NON-NLS-1$ //$NON-NLS-2$
		rules.add(new MultiLineRule("ifeq", "endif", tIf)); //$NON-NLS-1$ //$NON-NLS-2$
		rules.add(new MultiLineRule("ifnneq", "endif", tIf)); //$NON-NLS-1$ //$NON-NLS-2$

		// Last rule must be supplied with default token!
		rules.add(new MacroDefinitionRule(tMacro, tOther)); //$NON-NLS-1$

		IPredicateRule[] result = new IPredicateRule[rules.size()];
		rules.toArray(result);
		setPredicateRules(result);

	}

	/*
	 * @see ICharacterScanner#getLegalLineDelimiters
	 */
	public char[][] getLegalLineDelimiters() {
		return fModDelimiters;
	}

}
