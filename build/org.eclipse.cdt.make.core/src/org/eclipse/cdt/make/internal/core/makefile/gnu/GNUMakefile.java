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
package org.eclipse.cdt.make.internal.core.makefile.gnu;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.StringTokenizer;

import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.core.makefile.IDirective;
import org.eclipse.cdt.make.core.makefile.IMakefile;
import org.eclipse.cdt.make.core.makefile.gnu.IGNUMakefile;
import org.eclipse.cdt.make.internal.core.makefile.AbstractMakefile;
import org.eclipse.cdt.make.internal.core.makefile.BadDirective;
import org.eclipse.cdt.make.internal.core.makefile.Command;
import org.eclipse.cdt.make.internal.core.makefile.Comment;
import org.eclipse.cdt.make.internal.core.makefile.DefaultRule;
import org.eclipse.cdt.make.internal.core.makefile.Directive;
import org.eclipse.cdt.make.internal.core.makefile.EmptyLine;
import org.eclipse.cdt.make.internal.core.makefile.IgnoreRule;
import org.eclipse.cdt.make.internal.core.makefile.InferenceRule;
import org.eclipse.cdt.make.internal.core.makefile.MacroDefinition;
import org.eclipse.cdt.make.internal.core.makefile.MakeFileConstants;
import org.eclipse.cdt.make.internal.core.makefile.MakefileReader;
import org.eclipse.cdt.make.internal.core.makefile.PosixRule;
import org.eclipse.cdt.make.internal.core.makefile.PreciousRule;
import org.eclipse.cdt.make.internal.core.makefile.Rule;
import org.eclipse.cdt.make.internal.core.makefile.SccsGetRule;
import org.eclipse.cdt.make.internal.core.makefile.SilentRule;
import org.eclipse.cdt.make.internal.core.makefile.SpecialRule;
import org.eclipse.cdt.make.internal.core.makefile.SuffixesRule;
import org.eclipse.cdt.make.internal.core.makefile.Target;
import org.eclipse.cdt.make.internal.core.makefile.TargetRule;
import org.eclipse.cdt.make.internal.core.makefile.Util;
import org.eclipse.cdt.make.internal.core.makefile.posix.PosixMakefileUtil;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;

/**
 * Makefile : ( statement ) *
 * statement :   rule | macro_definition | comments | empty
 * rule :  inference_rule | target_rule
 * inference_rule : target ':' <nl> ( <tab> command <nl> ) +
 * target_rule : target [ ( target ) * ] ':' [ ( prerequisite ) * ] [ ';' command ] <nl> 
                 [ ( command ) * ]
 * macro_definition : string '=' (string)* 
 * comments : ('#' (string) <nl>) *
 * empty : <nl>
 * command : <tab> prefix_command string <nl>
 * target : string
 * prefix_command : '-' | '@' | '+'
 * internal_macro :  "$<" | "$*" | "$@" | "$?" | "$%" 
 */

public class GNUMakefile extends AbstractMakefile implements IGNUMakefile {

	public static String PATH_SEPARATOR = System.getProperty("path.separator", ":"); //$NON-NLS-1$ //$NON-NLS-2$
	public static String FILE_SEPARATOR = System.getProperty("file.separator", "/"); //$NON-NLS-1$ //$NON-NLS-2$

	String[] includeDirectories = new String[0];
	IDirective[] builtins = null;

	public GNUMakefile() {
		super(null);
	}

	public void parse(String name) throws IOException {
		FileReader stream = new FileReader(name);
		parse(name, stream);
		if (stream != null) {
			try {
				stream.close();
			} catch (IOException e) {
			}
		}
	}

	public void parse(String name, Reader reader) throws IOException {
		parse(name, new MakefileReader(reader));
	}

	protected void parse(String name, MakefileReader reader) throws IOException {
		String line;
		Rule[] rules = null;
		Stack conditions = new Stack();
		Stack defines = new Stack();
		int startLine = 0;
		int endLine = 0;

		// Clear any old directives.
		clearDirectives();

		setFilename(name);
	
		while ((line = reader.readLine()) != null) {
			startLine = endLine + 1;
			endLine = reader.getLineNumber();

			// Check if we enter in "define"
			if (GNUMakefileUtil.isEndef(line)) {
				// We should have a "define" for a "endef".
				if (!defines.empty()) {
					VariableDefinition def = (VariableDefinition) defines.pop();
					def.setEndLine(endLine);
				}
				Endef endef = new Endef(this);
				endef.setLines(startLine, endLine);
				addDirective(conditions, endef);
				continue;
			} else if (GNUMakefileUtil.isDefine(line)) {
				VariableDefinition def = parseVariableDefinition(line);
				def.setLines(startLine, endLine);
				addDirective(conditions, def);
				defines.push(def);
				continue;
			} else if (GNUMakefileUtil.isOverrideDefine(line)) {
				VariableDefinition oDef = parseVariableDefinition(line);
				oDef.setLines(startLine, endLine);
				addDirective(conditions, oDef);
				defines.push(oDef);
				continue;
			}

			// We still in a define.
			if (!defines.empty()) {
				VariableDefinition def = (VariableDefinition) defines.peek();
				StringBuffer sb = def.getValue();
				if (sb.length() > 0) {
					sb.append('\n');
				}
				sb.append(line);
				continue;
			}

			// 1- Try command first, since we can not strip '#' in command line
			if (PosixMakefileUtil.isCommand(line)) {
				Command cmd = new Command(this, line);
				cmd.setLines(startLine, endLine);
				if (!conditions.empty()) {
					addDirective(conditions, cmd);
					continue;
				} else if (rules != null) {
					// The command is added to the rules
					for (int i = 0; i < rules.length; i++) {
						rules[i].addDirective(cmd);
						rules[i].setEndLine(endLine);
					}
					continue;
				}
				// If we have no rules/condition for the command,
				// give the other directives a chance by falling through
			}

			// 2- Strip away any comments.
			int pound = Util.indexOfComment(line);
			if (pound != -1) {
				Comment cmt = new Comment(this, line.substring(pound + 1));
				cmt.setLines(startLine, endLine);
				if (rules != null) {
					// The comment is added to the rules.
					for (int i = 0; i < rules.length; i++) {
						rules[i].addDirective(cmt);
						rules[i].setEndLine(endLine);
					}
				} else {
					addDirective(conditions, cmt);
				}
				line = line.substring(0, pound);
				// If all we have left are spaces continue
				if (Util.isEmptyLine(line)) {
					continue;
				}
				// The rest of the line maybe a valid directives.
				// keep on trying by falling through.
			}

			// 3- Empty lines ?
			if (Util.isEmptyLine(line)) {
				Directive empty = new EmptyLine(this);
				empty.setLines(startLine, endLine);
				if (rules != null) {
					// The EmptyLine is added to the rules.
					for (int i = 0; i < rules.length; i++) {
						rules[i].addDirective(empty);
						rules[i].setEndLine(endLine);
					}
				} else {
					addDirective(conditions, empty);
				}
				continue;
			}

			// 4- reset the rules to null
			// The first non empty line that does not begin with a <TAB> or '#'
			// shall begin a new entry.
			rules = null;

			if (GNUMakefileUtil.isElse(line)) {
				Conditional elseDirective = parseConditional(line);
				elseDirective.setLines(startLine, endLine);
				// Are we missing a if condition ?
				if (!conditions.empty()) {
					Conditional cond = (Conditional) conditions.pop();
					cond.setEndLine(endLine - 1);
				}
				addDirective(conditions, elseDirective);
				conditions.push(elseDirective);
				continue;
			} else if (GNUMakefileUtil.isEndif(line)) {
				Endif endif = new Endif(this);
				endif.setLines(startLine, endLine);
				// Are we missing a if/else condition ?
				if (!conditions.empty()) {
					Conditional cond = (Conditional) conditions.pop();
					cond.setEndLine(endLine);
				}
				addDirective(conditions, endif);
				continue;
			}

			// 5- Check for the conditionnals.
			Directive directive = processConditions(line);
			if (directive != null) {
				directive.setLines(startLine, endLine);
				addDirective(conditions, directive);
				conditions.push(directive);
				continue;
			}

			// 6- Check for other special gnu directives.
			directive = processGNUDirectives(line);
			if (directive != null) {
				directive.setLines(startLine, endLine);
				addDirective(conditions, directive);
				continue;
			}

			// 7- Check for GNU special rules.
			SpecialRule special = processSpecialRules(line);
			if (special != null) {
				rules = new Rule[] { special };
				special.setLines(startLine, endLine);
				addDirective(conditions, special);
				continue;
			}

			// - Check for inference rule.
			if (PosixMakefileUtil.isInferenceRule(line)) {
				InferenceRule irule = parseInferenceRule(line);
				irule.setLines(startLine, endLine);
				addDirective(conditions, irule);
				rules = new Rule[] { irule };
				continue;
			}

			// - Variable Definiton ?
			if (GNUMakefileUtil.isVariableDefinition(line)) {
				VariableDefinition vd = parseVariableDefinition(line);
				vd.setLines(startLine, endLine);
				addDirective(conditions, vd);
				if (!vd.isTargetSpecific()) {
					continue;					
				}
			}

			// - GNU Static Target rule ?
			if (GNUMakefileUtil.isStaticTargetRule(line)) {
				StaticTargetRule[] srules = parseStaticTargetRule(line);
				for (int i = 0; i < srules.length; i++) {
					srules[i].setLines(startLine, endLine);
					addDirective(conditions, srules[i]);
				}
				rules = srules;
				continue;
			}

			// - Target Rule ?
			if (GNUMakefileUtil.isGNUTargetRule(line)) {
				TargetRule[] trules = parseGNUTargetRules(line);
				for (int i = 0; i < trules.length; i++) {
					trules[i].setLines(startLine, endLine);
					addDirective(conditions, trules[i]);
				}
				rules = trules;
				continue;
			}

			// XXX ?? Should not be here.
			BadDirective stmt = new BadDirective(this, line);
			stmt.setLines(startLine, endLine);
			addDirective(conditions, stmt);

		}
		setLines(1, endLine);
		// TEST please remove.
		//GNUMakefileValidator validator = new GNUMakefileValidator();
		//validator.validateDirectives(null, getDirectives());
	}

	private void addDirective(Stack conditions, Directive directive) {
		if (conditions.empty()) {
			addDirective(directive);
		} else {
			Conditional cond = (Conditional) conditions.peek();
			cond.addDirective(directive);
			cond.setEndLine(directive.getEndLine());
		}
	}

	protected Conditional processConditions(String line) {
		Conditional stmt = null;
		if (GNUMakefileUtil.isIfdef(line)) {
			stmt = parseConditional(line);
		} else if (GNUMakefileUtil.isIfndef(line)) {
			stmt = parseConditional(line);
		} else if (GNUMakefileUtil.isIfeq(line)) {
			stmt = parseConditional(line);
		} else if (GNUMakefileUtil.isIfneq(line)) {
			stmt = parseConditional(line);
		}
		return stmt;
	}

	protected Directive processGNUDirectives(String line) {
		Directive stmt = null;
		if (GNUMakefileUtil.isUnExport(line)) {
			stmt = parseUnExport(line);
		} else if (GNUMakefileUtil.isVPath(line)) {
			stmt = parseVPath(line);
		} else if (GNUMakefileUtil.isInclude(line)) {
			stmt = parseInclude(line);
		}
		return stmt;
	}

	protected SpecialRule processSpecialRules(String line) {
		SpecialRule stmt = null;
		if (PosixMakefileUtil.isIgnoreRule(line)) {
			stmt = parseSpecialRule(line);
		} else if (PosixMakefileUtil.isPosixRule(line)) {
			stmt = parseSpecialRule(line);
		} else if (PosixMakefileUtil.isPreciousRule(line)) {
			stmt = parseSpecialRule(line);
		} else if (PosixMakefileUtil.isSilentRule(line)) {
			stmt = parseSpecialRule(line);
		} else if (PosixMakefileUtil.isSuffixesRule(line)) {
			stmt = parseSpecialRule(line);
		} else if (PosixMakefileUtil.isDefaultRule(line)) {
			stmt = parseSpecialRule(line);
		} else if (PosixMakefileUtil.isSccsGetRule(line)) {
			stmt = parseSpecialRule(line);
		} else if (GNUMakefileUtil.isPhonyRule(line)) {
			stmt = parseSpecialRule(line);
		} else if (GNUMakefileUtil.isIntermediateRule(line)) {
			stmt = parseSpecialRule(line);
		} else if (GNUMakefileUtil.isSecondaryRule(line)) {
			stmt = parseSpecialRule(line);
		} else if (GNUMakefileUtil.isDeleteOnErrorRule(line)) {
			stmt = parseSpecialRule(line);
		} else if (GNUMakefileUtil.isLowResolutionTimeRule(line)) {
			stmt = parseSpecialRule(line);
		} else if (GNUMakefileUtil.isExportAllVariablesRule(line)) {
			stmt = parseSpecialRule(line);
		} else if (GNUMakefileUtil.isNotParallelRule(line)) {
			stmt = parseSpecialRule(line);
		}
		return stmt;
	}

	/**
	 * @param line
	 * @return
	 */
	protected SpecialRule parseSpecialRule(String line) {
		line = line.trim();
		String keyword =  null;
		String[] reqs = null;
		SpecialRule special = null;
		int index = Util.indexOf(line, ':');
		if (index != -1) {
			keyword = line.substring(0, index).trim();
			String req = line.substring(index + 1);
			reqs = PosixMakefileUtil.findPrerequisites(req);
		} else {
			keyword = line;
			reqs = new String[0];
		}
		if (keyword.equals(MakeFileConstants.RULE_IGNORE)) {
			special = new IgnoreRule(this, reqs);
		} else if (keyword.equals(MakeFileConstants.RULE_POSIX)) {
			special = new PosixRule(this);
		} else if (keyword.equals(MakeFileConstants.RULE_PRECIOUS)) {
			special = new PreciousRule(this, reqs);
		} else if (keyword.equals(MakeFileConstants.RULE_SILENT)) {
			special = new SilentRule(this, reqs);
		} else if (keyword.equals(MakeFileConstants.RULE_SUFFIXES)) {
			special = new SuffixesRule(this, reqs);
		} else if (keyword.equals(MakeFileConstants.RULE_DEFAULT)) {
			special = new DefaultRule(this, new Command[0]);
		} else if (keyword.equals(MakeFileConstants.RULE_SCCS_GET)) {
			special = new SccsGetRule(this, new Command[0]);
		} else if (keyword.equals(GNUMakefileConstants.RULE_PHONY)) {
			special = new PhonyRule(this, reqs);
		} else if (keyword.equals(GNUMakefileConstants.RULE_INTERMEDIATE)) {
			special = new IntermediateRule(this, reqs);
		} else if (keyword.equals(GNUMakefileConstants.RULE_SECONDARY)) {
			special = new SecondaryRule(this, reqs);
		} else if (keyword.equals(GNUMakefileConstants.RULE_DELETE_ON_ERROR)) {
			special = new DeleteOnErrorRule(this, reqs);
		} else if (keyword.equals(GNUMakefileConstants.RULE_LOW_RESOLUTION_TIME)) {
			special = new LowResolutionTimeRule(this, reqs);
		} else if (keyword.equals(GNUMakefileConstants.RULE_EXPORT_ALL_VARIABLES)) {
			special = new ExportAllVariablesRule(this, reqs);
		} else if (keyword.equals(GNUMakefileConstants.RULE_NOT_PARALLEL)) {
			special = new NotParallelRule(this, reqs);
		}
		return special;
	}

	/**
	 *
	 * ifdef CONDITIONAL
	 * ifeq CONDITIONAL
	 * ifneq CONDITIONAL
	 * else
	 *
	 * @param line
	 * @return
	 */
	protected Conditional parseConditional(String line) {
		Conditional condition = null;
		line = line.trim();
		String keyword = null;
		// Move pass the keyword
		for (int i = 0; i < line.length(); i++) {
			if (Util.isSpace(line.charAt(i))) {
				keyword = line.substring(0, i);
				line = line.substring(i).trim();
				break;
			}
		}
		if (keyword == null) {
			keyword = line;
		}
		if (keyword.equals(GNUMakefileConstants.CONDITIONAL_IFDEF)) {
			condition = new Ifdef(this, line);
		} else if (keyword.equals(GNUMakefileConstants.CONDITIONAL_IFNDEF)) {
			condition = new Ifndef(this, line);
		} else if (keyword.equals(GNUMakefileConstants.CONDITIONAL_IFEQ)) {
			condition = new Ifeq(this, line);
		} else if (keyword.equals(GNUMakefileConstants.CONDITIONAL_IFNEQ)) {
			condition = new Ifneq(this, line);
		} else if (keyword.equals(GNUMakefileConstants.CONDITIONAL_ELSE)) {
			condition = new Else(this);
		}
		return condition;
	}

	/**
	 *  Format of the include directive:
	 *      include filename1 filename2 ...
	 */
	protected Include parseInclude(String line) {
		String[] filenames;
		StringTokenizer st = new StringTokenizer(line);
		int count = st.countTokens();
		if (count > 0) {
			filenames = new String[count - 1];
			for (int i = 0; i < count; i++) {
				if (i == 0) {
					st.nextToken();
					// ignore the "include" keyword.
					continue;
				}
				filenames[i - 1] = st.nextToken();
			}
		} else {
			filenames = new String[0];
		}
		return new Include(this, filenames, getIncludeDirectories());
	}

	/**
	   * There are three forms of the "vpath" directive:
	   *      "vpath PATTERN DIRECTORIES"
	   * Specify the search path DIRECTORIES for file names that match PATTERN.
	   *
	   * The search path, DIRECTORIES, is a list of directories to be
	   * searched, separated by colons (semi-colons on MS-DOS and
	   * MS-Windows) or blanks, just like the search path used in the `VPATH' variable.
	   *
	   *      "vpath PATTERN"
	   * Clear out the search path associated with PATTERN.
	   *
	   *      "vpath"
	   * Clear all search paths previously specified with `vpath' directives.
	   */
	protected VPath parseVPath(String line) {
		String pattern = null;
		String[] directories;
		StringTokenizer st = new StringTokenizer(line);
		int count = st.countTokens();
		List dirs = new ArrayList(count);
		if (count > 0) {
			for (int i = 0; i < count; i++) {
				if (count == 0) {
					// ignore the "vpath" directive
					st.nextToken();
				} else if (count == 1) {
					pattern = st.nextToken();
				} else if (count == 3) {
					String delim = " \t\n\r\f" + GNUMakefile.PATH_SEPARATOR; //$NON-NLS-1$
					dirs.add(st.nextToken(delim));
				} else {
					dirs.add(st.nextToken());
				}
			}
		}
		directories = (String[]) dirs.toArray(new String[0]);
		if (pattern == null) {
			pattern = new String();
		}
		return new VPath(this, pattern, directories);
	}

	/**
	 * @param line
	 * @return
	 */
	protected UnExport parseUnExport(String line) {
		// Pass over "unexport"
		for (int i = 0; i < line.length(); i++) {
			if (Util.isSpace(line.charAt(i))) {
				line = line.substring(i).trim();
				break;
			}
		}
		return new UnExport(this, line);
	}

	protected GNUTargetRule[] parseGNUTargetRules(String line) {
		String[] targetNames;
		String[] normalReqs;
		String[] orderReqs;
		String cmd = null;
		boolean doubleColon = false;
		int index = Util.indexOf(line, ':');
		if (index != -1) {
			// Break the targets
			String target = line.substring(0, index);
			targetNames = PosixMakefileUtil.findTargets(target.trim());

			// Some TargetRule have "::" for separator
			String req = line.substring(index + 1);
			doubleColon = req.startsWith(":"); //$NON-NLS-1$
			if (doubleColon) {
				// move pass the second ':'
				req = req.substring(1);
			}

			// Check for command
			int semicolon = Util.indexOf(req, ';');
			if (semicolon != -1) {
				cmd = req.substring(semicolon + 1);
				req = req.substring(0, semicolon);
			}

			// Check for Normal and order prerequisites
			String normalReq = null;
			String orderReq = null;
			int pipe = Util.indexOf(req, '|');
			if (pipe != -1) {
				normalReq = req.substring(0, pipe);
				orderReq = req.substring(pipe + 1);
			} else {
				normalReq = req;
				orderReq = ""; //$NON-NLS-1$
			}

			normalReqs = PosixMakefileUtil.findPrerequisites(normalReq.trim());
			orderReqs = PosixMakefileUtil.findPrerequisites(orderReq.trim());
		} else {
			targetNames = PosixMakefileUtil.findTargets(line);
			normalReqs = new String[0];
			orderReqs = new String[0];
		}

		GNUTargetRule[] rules = new GNUTargetRule[targetNames.length];
		for (int i = 0; i < targetNames.length; i++) {
			rules[i] = new GNUTargetRule(this, new Target(targetNames[i]), doubleColon, normalReqs, orderReqs, new Command[0]);
			if (cmd != null) {
				rules[i].addDirective(new Command(this, cmd));
			}
		}
		return rules;
	}

	protected VariableDefinition parseVariableDefinition(String line) {
		line = line.trim();
		VariableDefinition vd;

		// the default type.
		int type = VariableDefinition.TYPE_RECURSIVE_EXPAND;
		boolean isDefine = false;
		boolean isOverride = false;
		boolean isTargetVariable = false;
		boolean isExport = false;
		String targetName = ""; //$NON-NLS-1$

		String name;
		StringBuffer value = new StringBuffer();

		// Check for Target: Variable-assignment
		isTargetVariable = GNUMakefileUtil.isTargetVariable(line);
		if (isTargetVariable) {
			// move to the first ':'
			int colon = Util.indexOf(line, ':');
			if (colon != -1) {
				targetName = line.substring(0, colon).trim();
				line = line.substring(colon + 1).trim();
			} else {
				targetName = ""; //$NON-NLS-1$
			}
		}

		// Check for Override condition.
		if (GNUMakefileUtil.isOverride(line)) {
			isOverride = true;
			// Move pass the keyword override.
			for (int i = 0; i < line.length(); i++) {
				if (Util.isSpace(line.charAt(i))) {
					line = line.substring(i).trim();
					break;
				}
			}
		}

		// Check for "define"
		if (GNUMakefileUtil.isDefine(line)) {
			isDefine = true;
			// Move pass the keyword define.
			for (int i = 0; i < line.length(); i++) {
				if (Util.isSpace(line.charAt(i))) {
					line = line.substring(i).trim();
					break;
				}
			}
		}

		// Check for Override condition.
		if (GNUMakefileUtil.isExport(line)) {
			isExport = true;
			// Move pass the keyword export.
			for (int i = 0; i < line.length(); i++) {
				if (Util.isSpace(line.charAt(i))) {
					line = line.substring(i).trim();
					break;
				}
			}
		}

		// Check for Target-variable

		int index = line.indexOf('=');
		if (index != -1) {
			int separator = index;
			// Check for "+=",  ":=", "?="
			if (index > 0) {
				type = line.charAt(index - 1);
				if (type == VariableDefinition.TYPE_SIMPLE_EXPAND
					|| type == VariableDefinition.TYPE_APPEND
					|| type == VariableDefinition.TYPE_CONDITIONAL) {
					separator = index - 1;
				} else {
					type = VariableDefinition.TYPE_RECURSIVE_EXPAND;
				}
			}
			name = line.substring(0, separator).trim();
			value.append(line.substring(index + 1).trim());
		} else {
			name = line;
		}

		if (isTargetVariable) {
			vd = new TargetVariable(this, targetName, name, value, isOverride, type);
		} else if (isOverride && isDefine) {
			vd = new OverrideDefine(this, name, value);
		} else if (isDefine) {
			vd = new DefineVariable(this, name, value);
		} else if (isOverride) {
			vd = new OverrideVariable(this, name, value, type);
		} else if (isExport) {
			vd = new ExportVariable(this, name, value, type);
		} else {
			vd = new VariableDefinition(this, name, value, type);
		}
		return vd;
	}

	protected StaticTargetRule[] parseStaticTargetRule(String line) {
		// first colon: the Targets
		String targetPattern;
		String[] prereqPatterns;
		String[] targets;
		int colon = Util.indexOf(line, ':');
		if (colon > 1) {
			String targetLine = line.substring(0, colon).trim();
			targets = PosixMakefileUtil.findTargets(targetLine);
			// second colon: Target-Pattern
			line = line.substring(colon + 1);
			colon = Util.indexOf(line, ':');
			if (colon != -1) {
				targetPattern = line.substring(0, colon).trim();
				line = line.substring(colon + 1);
				StringTokenizer st = new StringTokenizer(line);
				int count = st.countTokens();
				prereqPatterns = new String[count];
				for (int i = 0; i < count; i++) {
					prereqPatterns[i] = st.nextToken();
				}
			} else {
				targetPattern = ""; //$NON-NLS-1$
				prereqPatterns = new String[0];
			}
		} else {
			targets = new String[0];
			targetPattern = ""; //$NON-NLS-1$
			prereqPatterns = new String[0];
		}

		StaticTargetRule[] staticRules = new StaticTargetRule[targets.length];
		for (int i = 0; i < targets.length; i++) {
			staticRules[i] = new StaticTargetRule(this, new Target(targets[i]), targetPattern, prereqPatterns, new Command[0]);
		}
		return staticRules;
	}

	/**
	 * @param line
	 * @return
	 */
	protected InferenceRule parseInferenceRule(String line) {
		String tgt;
		int index = Util.indexOf(line, ':');
		if (index != -1) {
			tgt = line.substring(0, index);
		} else {
			tgt = line;
		}
		return new InferenceRule(this, new Target(tgt));
	}

	public IDirective[] getDirectives(boolean expand) {
		if (!expand) {
			return getDirectives();
		}
		IDirective[] dirs = getDirectives();
		ArrayList list = new ArrayList(Arrays.asList(dirs));
		for (int i = 0; i < dirs.length; ++i) {
			if (dirs[i] instanceof Include) {
				Include include = (Include)dirs[i];
				IDirective[] includedMakefiles = include.getDirectives();
				for (int j = 0; j < includedMakefiles.length; ++j) {
					IMakefile includedMakefile = (IMakefile)includedMakefiles[j];
					list.addAll(Arrays.asList(includedMakefile.getDirectives()));
				}
			}
		}
		return (IDirective[]) list.toArray(new IDirective[list.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.make.internal.core.makefile.AbstractMakefile#getBuiltins()
	 */
	public IDirective[] getBuiltins() {
		if (builtins == null) {
			String location =  "builtin" + File.separator + "gnu.mk"; //$NON-NLS-1$ //$NON-NLS-2$
			try {
				InputStream stream = MakeCorePlugin.getDefault().openStream(new Path(location));
				GNUMakefile gnu = new GNUMakefile();
				URL url = Platform.find(MakeCorePlugin.getDefault().getBundle(), new Path(location));
				url = Platform.resolve(url);
				location = url.getFile();
				gnu.parse(location, new InputStreamReader(stream));
				builtins = gnu.getDirectives();
				for (int i = 0; i < builtins.length; i++) {
					if (builtins[i] instanceof MacroDefinition) {
						((MacroDefinition)builtins[i]).setFromDefault(true);
					}
				}
			} catch (Exception e) {
				//e.printStackTrace();
			}
			if (builtins == null) {
				builtins = new IDirective[0];
			}
		}
		return builtins;
	}

	public void setIncludeDirectories(String[] dirs) {
		includeDirectories = dirs;
	}

	public String[] getIncludeDirectories() {
		return includeDirectories;
	}

	public static void main(String[] args) {
		try {
			String filename = "Makefile"; //$NON-NLS-1$
			if (args.length == 1) {
				filename = args[0];
			}
			GNUMakefile makefile = new GNUMakefile();
			makefile.parse(filename);
			IDirective[] directive = makefile.getDirectives();
			for (int i = 0; i < directive.length; i++) {
				//System.out.println("Rule[" + i +"]");
				System.out.print(directive[i]);
			}
		} catch (IOException e) {
			System.out.println(e);
		}
	}

}
