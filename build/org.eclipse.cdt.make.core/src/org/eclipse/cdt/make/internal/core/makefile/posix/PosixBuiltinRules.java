/**********************************************************************
 * Copyright (c) 2002,2003 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.make.internal.core.makefile.posix;

import org.eclipse.cdt.make.internal.core.makefile.Command;
import org.eclipse.cdt.make.internal.core.makefile.InferenceRule;

public class PosixBuiltinRules {
	InferenceRule[] rules = new InferenceRule[] {
		// Special Targets
		new InferenceRule(".SCCS_GET", new Command[] { new Command("sccs $(SCCSFLAGS) get $(SCCSGETFLAGS) $@")}),
			new InferenceRule(".SUFFIXES", new Command[] { new Command(" .o .c .y .l .a .sh .f .c~ .y~ .l~ .sh~ .f~")}),
		//  Single Suffix Rules
		new InferenceRule(".c", new Command[] { new Command("$(CC) $(CFLAGS) $(LDFLAGS) -o $@ $<")}),
			new InferenceRule(".f", new Command[] { new Command("$(FC) $(FFLAGS) $(LDFLAGS) -o $@ $<")}),
			new InferenceRule(".sh", new Command[] { new Command("cp $< $@"), new Command("chmod a+x $@")}),
			new InferenceRule(
				".c~",
				new Command[] {
					new Command("$(GET) $(GFLAGS) -p $< > $*.c"),
					new Command("$(CC) $(CFLAGS) $(LDFLAGS) -o $@ $*.c")}),
			new InferenceRule(
				".f~",
				new Command[] {
					new Command("$(GET) $(GFLAGS) -p $< > $*.f"),
					new Command("$(FC) $(FFLAGS) $(LDFLAGS) -o $@ $*.f")}),
			new InferenceRule(
				".sh~",
				new Command[] {
					new Command("$(GET) $(GFLAGS) -p $< > $*.sh"),
					new Command("cp $*.sh $@"),
					new Command("chmod a+x $@")}),

		// DOUBLE SUFFIX RULES

		new InferenceRule(".c.o", new Command[] { new Command("$(CC) $(CFLAGS) -c $<")}),
			new InferenceRule(".f.o", new Command[] { new Command("$(FC) $(FFLAGS) -c $<")}),
			new InferenceRule(
				".y.o",
				new Command[] {
					new Command("$(YACC) $(YFLAGS) $<"),
					new Command("$(CC) $(CFLAGS) -c y.tab.c"),
					new Command("rm -f y.tab.c"),
					new Command("mv y.tab.o $@")}),
			new InferenceRule(
				".l.o",
				new Command[] {
					new Command("$(LEX) $(LFLAGS) $<"),
					new Command("$(CC) $(CFLAGS) -c lex.yy.c"),
					new Command("rm -f lex.yy.c"),
					new Command("mv lex.yy.o $@"),
					}),
			new InferenceRule(".y.c", new Command[] { new Command("$(YACC) $(YFLAGS) $<"), new Command("mv y.tab.c $@")}),
			new InferenceRule(".l.c", new Command[] { new Command("$(LEX) $(LFLAGS) $<"), new Command("mv lex.yy.c $@")}),
			new InferenceRule(
				".c~.o",
				new Command[] { new Command("$(GET) $(GFLAGS) -p $< > $*.c"), new Command("$(CC) $(CFLAGS) -c $*.c")}),
			new InferenceRule(
				".f~.o",
				new Command[] { new Command("$(GET) $(GFLAGS) -p $< > $*.f"), new Command("$(FC) $(FFLAGS) -c $*.f")}),
			new InferenceRule(
				".y~.o",
				new Command[] {
					new Command("$(GET) $(GFLAGS) -p $< > $*.y"),
					new Command("$(YACC) $(YFLAGS) $*.y"),
					new Command("$(CC) $(CFLAGS) -c y.tab.c"),
					new Command("rm -f y.tab.c"),
					new Command("mv y.tab.o $@")}),
			new InferenceRule(
				".l~.o",
				new Command[] {
					new Command("$(GET) $(GFLAGS) -p $< > $*.l"),
					new Command("$(LEX) $(LFLAGS) $*.l"),
					new Command("$(CC) $(CFLAGS) -c lex.yy.c"),
					new Command("rm -f lex.yy.c"),
					new Command("mv lex.yy.o $@")}),
			new InferenceRule(
				".y~.c",
				new Command[] {
					new Command("$(GET) $(GFLAGS) -p $< > $*.y"),
					new Command("$(YACC) $(YFLAGS) $*.y"),
					new Command("mv y.tab.c $@")}),
			new InferenceRule(
				".l~.c",
				new Command[] {
					new Command("$(GET) $(GFLAGS) -p $< > $*.l"),
					new Command("$(LEX) $(LFLAGS) $*.l"),
					new Command("mv lex.yy.c $@")}),
			new InferenceRule(
				".c.a",
				new Command[] {
					new Command("$(CC) -c $(CFLAGS) $<"),
					new Command("$(AR) $(ARFLAGS) $@ $*.o"),
					new Command("rm -f $*.o")}),
			new InferenceRule(
				".f.a",
				new Command[] {
					new Command("$(FC) -c $(FFLAGS) $<"),
					new Command("$(AR) $(ARFLAGS) $@ $*.o"),
					new Command("rm -f $*.o")})
		};

	InferenceRule getInferenceRule(String name) {
		for (int i = 0; i < rules.length; i++) {
			if (name.equals(rules[i].getTarget())) {
				return rules[i];
			}
		}
		return null;
	}

	InferenceRule[] getInferenceRules() {
		return rules;
	}
}
