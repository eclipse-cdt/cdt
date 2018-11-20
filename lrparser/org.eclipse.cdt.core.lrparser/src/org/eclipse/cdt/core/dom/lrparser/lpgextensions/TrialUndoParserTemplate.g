--------------------------------------------------------------------------------
-- Copyright (c) 2006, 2007 IBM Corporation and others.
-- This program and the accompanying materials
-- are made available under the terms of the Eclipse Public License 2.0
-- which accompanies this distribution, and is available at
-- https://www.eclipse.org/legal/epl-2.0/
--
-- SPDX-License-Identifier: EPL-2.0
--
-- Contributors:
--     IBM Corporation - initial API and implementation
--------------------------------------------------------------------------------
 
--
-- In a parser using this template, the following macro may be redefined:
--
--     $additional_interfaces
--     $ast_class
--
-- B E G I N N I N G   O F   T E M P L A T E   TrialUndoParserTemplate
--
%Options programming_language=java,margin=8,backtrack
%Options table,error_maps,scopes
%options prefix=TK_,
%options action=("*.java", "/.", "./")
%options headers=("*.java", "/:", ":/")
%options ParseTable=lpg.lpgjavaruntime.ParseTable

--
-- This template requires that the name of the EOF token be set
-- to EOF_TOKEN to be consistent with LexerTemplateD and LexerTemplateE
--
$EOF
	EOF_TOKEN
$End

$ERROR
	ERROR_TOKEN
$End

$Define
		$DefaultAllocation
		/:
			RULE_ACTIONS[$rule_number] = new Action$rule_number$();:/

		$NoAllocation
		/:
			RULE_ACTIONS[$rule_number] = EMPTY_ACTION:/

		$NullAllocation
		/:
			RULE_ACTIONS[$rule_number] = new NullAction();:/

		$BadAllocation
		/:
			RULE_ACTIONS[$rule_number] = new BadAction();:/

		$Header
		/.
		//
		// Rule $rule_number:  $rule_text
		//./

		$DefaultAction
		/.$DefaultAllocation $Header
			static final class Action$rule_number extends Action./

		--
		-- This macro is used to initialize the ruleAction array
		-- to the null_action function.
		--
		$NullAction
		/. $NullAllocation $Header
			//
			// final class NullAction extends Action
			//
		./

		--
		-- This macro is used to initialize the ruleAction array
		-- to the no_action function.
		--
		$NoAction
		/. $NoAllocation $Header
			//
			// final class NullAction extends Action
			//
		./

		--
		-- This macro is used to initialize the ruleAction array
		-- to the bad_action function.
		--
		$BadAction
		/. $BadAllocation $Header
			//
			// final class NullAction extends Action
			//
		./

		--
		-- This is the header for a ruleAction class
		--
		$BeginAction
		/.$DefaultAllocation $Header
		static final class Action$rule_number extends DeclaredAction<$action_class, $data_class> {
			./

		$EndAction
		/.
		}./

		$BeginTrial
		/.
			public boolean doTrial(ITrialUndoActionProvider<$data_class> provider, $action_class action) {./
		
		$EndTrial
		/.
			return hasUndo;
			}./
		
		$BeginUndo
		/.
			public Action$rule_number() { hasUndo = true; };
			public void doUndo(ITrialUndoActionProvider<$data_class> provider, $action_class action) {./
		
		$EndUndo
		/.
			}./
		
		$BeginFinal
		/.
			public void doFinal(ITrialUndoActionProvider<$data_class> provider, $action_class action) {./
		
		$EndFinal
		/.
			}./
		
		$BeginJava
		/.$BeginAction
					$symbol_declarations./

		$EndJava /.$EndAction./

		$SplitActions /../

		--
		-- Macros that may be needed in a parser using this template
		--
		$additional_interfaces /../
		$ast_class /.$ast_type./
		$action_class /.Object./
		
		$Trial /.$BeginTrial./
		$Undo  /.$BeginUndo./
		$Final /.$BeginFinal./
		$Action /.$BeginAction./
		
$End

$Globals
		/.
import lpg.lpgjavaruntime.*;
import org.eclipse.cdt.core.dom.lrparser.lpgextensions.ITrialUndoActionProvider;
import org.eclipse.cdt.core.dom.lrparser.lpgextensions.AbstractTrialUndoActionProvider;
import org.eclipse.cdt.core.dom.lrparser.lpgextensions.TrialUndoParser;
./
$End

$Headers
	/.
public class $action_type extends AbstractTrialUndoActionProvider<$action_class, $data_class> implements IParserActionTokenProvider, IParser $additional_interfaces {
		private static ParseTable prs = new $prs_type();
		protected static final Action<$action_class, $data_class>[] RULE_ACTIONS;
	
		{
		    ruleAction = RULE_ACTIONS;
		}
		    
		public $action_type(LexStream lexStream) {
			super(lexStream);
	
			try {
				super.remapTerminalSymbols(orderedTerminalSymbols(), $prs_type.EOFT_SYMBOL);
			} catch (NullExportedSymbolsException e) {
			} catch (NullTerminalSymbolsException e) {
			} catch (UnimplementedTerminalsException e) {
				java.util.ArrayList unimplemented_symbols = e.getSymbols();
				System.out.println("The Lexer will not scan the following token(s):");
				for (int i = 0; i < unimplemented_symbols.size(); i++) {
					Integer id = (Integer) unimplemented_symbols.get(i);
					System.out.println("    " + $sym_type.orderedTerminalSymbols[id.intValue()]);
				}
				System.out.println();
			} catch (UndefinedEofSymbolException e) {
				throw new Error(new UndefinedEofSymbolException("The Lexer does not implement the Eof symbol " + $sym_type.orderedTerminalSymbols[$prs_type.EOFT_SYMBOL]));
			}
		}
	
		
	    public $action_type() {  // constructor
	        //	this(new $lexer_class());
	    }
	
		public String[] orderedTerminalSymbols() {
			return $sym_type.orderedTerminalSymbols;
		}
	
		public String getTokenKindName(int kind) {
			return $sym_type.orderedTerminalSymbols[kind];
		}
	
		public int getEOFTokenKind() {
			return $prs_type.EOFT_SYMBOL;
		}
		
		public PrsStream getParseStream() {
			return (PrsStream) this;
		}
	
		//
		// Report error message for given error_token.
		//
		public final void reportErrorTokenMessage(int error_token, String msg) {
			int firsttok = super.getFirstRealToken(error_token), lasttok = super.getLastRealToken(error_token);
			String location = super.getFileName() + ':' +
				(firsttok > lasttok
					? (super.getEndLine(lasttok) + ":" + super.getEndColumn(lasttok))
					: (super.getLine(error_token) + ":" +
					   super.getColumn(error_token) + ":" +
					   super.getEndLine(error_token) + ":" +
					   super.getEndColumn(error_token))) + ": ";
			super.reportError((firsttok > lasttok ? ParseErrorCodes.INSERTION_CODE : ParseErrorCodes.SUBSTITUTION_CODE), location, msg);
		}
	
		public $ast_class parser() {
			return parser(null, 0);
		}
	
		public $ast_class parser(Monitor monitor) {
			return parser(monitor, 0);
		}
	
		public $ast_class parser(int error_repair_count) {
			return parser(null, error_repair_count);
		}
	
		public $ast_class parser(Monitor monitor, int error_repair_count) {
			try {
				btParser = new TrialUndoParser((TokenStream) this, prs, (ITrialUndoActionProvider<$data_class>) this);
			} catch (NotBacktrackParseTableException e) {
				throw new Error(new NotBacktrackParseTableException("Regenerate $prs_type.java with -BACKTRACK option"));
			} catch (BadParseSymFileException e) {
				throw new Error(new BadParseSymFileException("Bad Parser Symbol File -- $sym_type.java"));
			}
	
			try {
				Object result = (Object) btParser.parse(error_repair_count);
				btParser.commit();
				return result;
			} catch (BadParseException e) {
				reset(e.error_token); // point to error token
	
				//DiagnoseParser diagnoseParser = new DiagnoseParser((TokenStream) this, prs);
				//diagnoseParser.diagnose(e.error_token);
			}
	
			return null;
		}
	
	./

	/:

		//
		// Initialize ruleAction array.
		//
		static {
			RULE_ACTIONS = new Action[$NUM_RULES + 1];
			RULE_ACTIONS[0] = null;
	:/
$End

$Trailers
	/.
	}
	./

	/:


			//
			// Make sure that all elements of ruleAction are properly initialized
			//
			for (int i = 0; i < RULE_ACTIONS.length; i++) {
				if (RULE_ACTIONS[i] == null) {
					RULE_ACTIONS[i] = emptyAction();
				}
			}
		}
	:/
$End
--
-- E N D   O F   T E M P L A T E
--
