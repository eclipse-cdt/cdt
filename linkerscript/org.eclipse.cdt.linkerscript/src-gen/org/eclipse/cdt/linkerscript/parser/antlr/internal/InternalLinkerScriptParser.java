package org.eclipse.cdt.linkerscript.parser.antlr.internal;

import org.eclipse.xtext.*;
import org.eclipse.xtext.parser.*;
import org.eclipse.xtext.parser.impl.*;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.parser.antlr.AbstractInternalAntlrParser;
import org.eclipse.xtext.parser.antlr.XtextTokenStream;
import org.eclipse.xtext.parser.antlr.XtextTokenStream.HiddenTokens;
import org.eclipse.xtext.parser.antlr.AntlrDatatypeRuleToken;
import org.eclipse.cdt.linkerscript.services.LinkerScriptGrammarAccess;



import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
@SuppressWarnings("all")
public class InternalLinkerScriptParser extends AbstractInternalAntlrParser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "RULE_ID", "RULE_DEC", "RULE_HEX", "RULE_ML_COMMENT", "RULE_WS", "RULE_ANY_OTHER", "','", "';'", "'STARTUP'", "'('", "')'", "'ENTRY'", "'ASSERT'", "'TARGET'", "'SEARCH_DIR'", "'OUTPUT'", "'OUTPUT_FORMAT'", "'OUTPUT_ARCH'", "'FORCE_COMMON_ALLOCATION'", "'INHIBIT_COMMON_ALLOCATION'", "'INPUT'", "'GROUP'", "'MAP'", "'NOCROSSREFS'", "'NOCROSSREFS_TO'", "'EXTERN'", "'INCLUDE'", "'AS_NEEDED'", "'-l'", "'PHDRS'", "'{'", "'}'", "'SECTIONS'", "':'", "'AT'", "'SUBALIGN'", "'>'", "'='", "'ALIGN'", "'ALIGN_WITH_INPUT'", "'ONLY_IF_RO'", "'ONLY_IF_RW'", "'SPECIAL'", "'NOLOAD'", "'DSECT'", "'COPY'", "'INFO'", "'OVERLAY'", "'CREATE_OBJECT_SYMBOLS'", "'CONSTRUCTORS'", "'SORT_BY_NAME'", "'FILL'", "'BYTE'", "'SHORT'", "'LONG'", "'QUAD'", "'SQUAD'", "'HIDDEN'", "'PROVIDE'", "'PROVIDE_HIDDEN'", "'+='", "'-='", "'*='", "'/='", "'<'", "'>='", "'&='", "'|='", "'INPUT_SECTION_FLAGS'", "'&'", "'KEEP'", "'EXCLUDE_FILE'", "'SORT_BY_ALIGNMENT'", "'SORT_NONE'", "'SORT_BY_INIT_PRIORITY'", "'SORT'", "'MEMORY'", "'ORIGIN'", "'org'", "'o'", "'LENGTH'", "'len'", "'l'", "'!'", "'?'", "'||'", "'&&'", "'|'", "'=='", "'!='", "'+'", "'-'", "'*'", "'/'", "'%'", "'~'", "'++'", "'--'", "'SIZEOF'"
    };
    public static final int RULE_HEX=6;
    public static final int T__50=50;
    public static final int T__59=59;
    public static final int T__55=55;
    public static final int T__56=56;
    public static final int T__57=57;
    public static final int T__58=58;
    public static final int T__51=51;
    public static final int T__52=52;
    public static final int T__53=53;
    public static final int T__54=54;
    public static final int T__60=60;
    public static final int T__61=61;
    public static final int RULE_ID=4;
    public static final int RULE_DEC=5;
    public static final int T__66=66;
    public static final int RULE_ML_COMMENT=7;
    public static final int T__67=67;
    public static final int T__68=68;
    public static final int T__69=69;
    public static final int T__62=62;
    public static final int T__63=63;
    public static final int T__64=64;
    public static final int T__65=65;
    public static final int T__37=37;
    public static final int T__38=38;
    public static final int T__39=39;
    public static final int T__33=33;
    public static final int T__34=34;
    public static final int T__35=35;
    public static final int T__36=36;
    public static final int T__30=30;
    public static final int T__31=31;
    public static final int T__32=32;
    public static final int T__48=48;
    public static final int T__49=49;
    public static final int T__44=44;
    public static final int T__45=45;
    public static final int T__46=46;
    public static final int T__47=47;
    public static final int T__40=40;
    public static final int T__41=41;
    public static final int T__42=42;
    public static final int T__43=43;
    public static final int T__91=91;
    public static final int T__100=100;
    public static final int T__92=92;
    public static final int T__93=93;
    public static final int T__102=102;
    public static final int T__94=94;
    public static final int T__101=101;
    public static final int T__90=90;
    public static final int T__19=19;
    public static final int T__15=15;
    public static final int T__16=16;
    public static final int T__17=17;
    public static final int T__18=18;
    public static final int T__11=11;
    public static final int T__99=99;
    public static final int T__12=12;
    public static final int T__13=13;
    public static final int T__14=14;
    public static final int T__95=95;
    public static final int T__96=96;
    public static final int T__97=97;
    public static final int T__10=10;
    public static final int T__98=98;
    public static final int T__26=26;
    public static final int T__27=27;
    public static final int T__28=28;
    public static final int T__29=29;
    public static final int T__22=22;
    public static final int T__23=23;
    public static final int T__24=24;
    public static final int T__25=25;
    public static final int T__20=20;
    public static final int T__21=21;
    public static final int T__70=70;
    public static final int T__71=71;
    public static final int T__72=72;
    public static final int T__77=77;
    public static final int T__78=78;
    public static final int T__79=79;
    public static final int T__73=73;
    public static final int EOF=-1;
    public static final int T__74=74;
    public static final int T__75=75;
    public static final int T__76=76;
    public static final int T__80=80;
    public static final int T__81=81;
    public static final int T__82=82;
    public static final int T__83=83;
    public static final int RULE_WS=8;
    public static final int RULE_ANY_OTHER=9;
    public static final int T__88=88;
    public static final int T__89=89;
    public static final int T__84=84;
    public static final int T__85=85;
    public static final int T__86=86;
    public static final int T__87=87;

    // delegates
    // delegators


        public InternalLinkerScriptParser(TokenStream input) {
            this(input, new RecognizerSharedState());
        }
        public InternalLinkerScriptParser(TokenStream input, RecognizerSharedState state) {
            super(input, state);
             
        }
        

    public String[] getTokenNames() { return InternalLinkerScriptParser.tokenNames; }
    public String getGrammarFileName() { return "InternalLinkerScript.g"; }



     	private LinkerScriptGrammarAccess grammarAccess;

        public InternalLinkerScriptParser(TokenStream input, LinkerScriptGrammarAccess grammarAccess) {
            this(input);
            this.grammarAccess = grammarAccess;
            registerRules(grammarAccess.getGrammar());
        }

        @Override
        protected String getFirstRuleName() {
        	return "LinkerScript";
       	}

       	@Override
       	protected LinkerScriptGrammarAccess getGrammarAccess() {
       		return grammarAccess;
       	}




    // $ANTLR start "entryRuleLinkerScript"
    // InternalLinkerScript.g:64:1: entryRuleLinkerScript returns [EObject current=null] : iv_ruleLinkerScript= ruleLinkerScript EOF ;
    public final EObject entryRuleLinkerScript() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleLinkerScript = null;


        try {
            // InternalLinkerScript.g:64:53: (iv_ruleLinkerScript= ruleLinkerScript EOF )
            // InternalLinkerScript.g:65:2: iv_ruleLinkerScript= ruleLinkerScript EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getLinkerScriptRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleLinkerScript=ruleLinkerScript();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleLinkerScript; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleLinkerScript"


    // $ANTLR start "ruleLinkerScript"
    // InternalLinkerScript.g:71:1: ruleLinkerScript returns [EObject current=null] : ( (lv_statements_0_0= ruleLinkerScriptStatement ) )* ;
    public final EObject ruleLinkerScript() throws RecognitionException {
        EObject current = null;

        EObject lv_statements_0_0 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:77:2: ( ( (lv_statements_0_0= ruleLinkerScriptStatement ) )* )
            // InternalLinkerScript.g:78:2: ( (lv_statements_0_0= ruleLinkerScriptStatement ) )*
            {
            // InternalLinkerScript.g:78:2: ( (lv_statements_0_0= ruleLinkerScriptStatement ) )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( (LA1_0==RULE_ID||(LA1_0>=11 && LA1_0<=12)||(LA1_0>=15 && LA1_0<=30)||LA1_0==33||LA1_0==36||(LA1_0>=61 && LA1_0<=63)||LA1_0==80||(LA1_0>=82 && LA1_0<=83)||(LA1_0>=85 && LA1_0<=86)||LA1_0==96) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // InternalLinkerScript.g:79:3: (lv_statements_0_0= ruleLinkerScriptStatement )
            	    {
            	    // InternalLinkerScript.g:79:3: (lv_statements_0_0= ruleLinkerScriptStatement )
            	    // InternalLinkerScript.g:80:4: lv_statements_0_0= ruleLinkerScriptStatement
            	    {
            	    if ( state.backtracking==0 ) {

            	      				newCompositeNode(grammarAccess.getLinkerScriptAccess().getStatementsLinkerScriptStatementParserRuleCall_0());
            	      			
            	    }
            	    pushFollow(FOLLOW_3);
            	    lv_statements_0_0=ruleLinkerScriptStatement();

            	    state._fsp--;
            	    if (state.failed) return current;
            	    if ( state.backtracking==0 ) {

            	      				if (current==null) {
            	      					current = createModelElementForParent(grammarAccess.getLinkerScriptRule());
            	      				}
            	      				add(
            	      					current,
            	      					"statements",
            	      					lv_statements_0_0,
            	      					"org.eclipse.cdt.linkerscript.LinkerScript.LinkerScriptStatement");
            	      				afterParserOrEnumRuleCall();
            	      			
            	    }

            	    }


            	    }
            	    break;

            	default :
            	    break loop1;
                }
            } while (true);


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleLinkerScript"


    // $ANTLR start "entryRuleLinkerScriptStatement"
    // InternalLinkerScript.g:100:1: entryRuleLinkerScriptStatement returns [EObject current=null] : iv_ruleLinkerScriptStatement= ruleLinkerScriptStatement EOF ;
    public final EObject entryRuleLinkerScriptStatement() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleLinkerScriptStatement = null;


        try {
            // InternalLinkerScript.g:100:62: (iv_ruleLinkerScriptStatement= ruleLinkerScriptStatement EOF )
            // InternalLinkerScript.g:101:2: iv_ruleLinkerScriptStatement= ruleLinkerScriptStatement EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getLinkerScriptStatementRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleLinkerScriptStatement=ruleLinkerScriptStatement();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleLinkerScriptStatement; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleLinkerScriptStatement"


    // $ANTLR start "ruleLinkerScriptStatement"
    // InternalLinkerScript.g:107:1: ruleLinkerScriptStatement returns [EObject current=null] : (this_MemoryCommand_0= ruleMemoryCommand | this_SectionsCommand_1= ruleSectionsCommand | this_PhdrsCommand_2= rulePhdrsCommand | this_StatementCommand_3= ruleStatementCommand ) ;
    public final EObject ruleLinkerScriptStatement() throws RecognitionException {
        EObject current = null;

        EObject this_MemoryCommand_0 = null;

        EObject this_SectionsCommand_1 = null;

        EObject this_PhdrsCommand_2 = null;

        EObject this_StatementCommand_3 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:113:2: ( (this_MemoryCommand_0= ruleMemoryCommand | this_SectionsCommand_1= ruleSectionsCommand | this_PhdrsCommand_2= rulePhdrsCommand | this_StatementCommand_3= ruleStatementCommand ) )
            // InternalLinkerScript.g:114:2: (this_MemoryCommand_0= ruleMemoryCommand | this_SectionsCommand_1= ruleSectionsCommand | this_PhdrsCommand_2= rulePhdrsCommand | this_StatementCommand_3= ruleStatementCommand )
            {
            // InternalLinkerScript.g:114:2: (this_MemoryCommand_0= ruleMemoryCommand | this_SectionsCommand_1= ruleSectionsCommand | this_PhdrsCommand_2= rulePhdrsCommand | this_StatementCommand_3= ruleStatementCommand )
            int alt2=4;
            switch ( input.LA(1) ) {
            case 80:
                {
                int LA2_1 = input.LA(2);

                if ( ((LA2_1>=40 && LA2_1<=41)||(LA2_1>=64 && LA2_1<=68)||(LA2_1>=70 && LA2_1<=71)) ) {
                    alt2=4;
                }
                else if ( (LA2_1==34) ) {
                    alt2=1;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return current;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 2, 1, input);

                    throw nvae;
                }
                }
                break;
            case 36:
                {
                alt2=2;
                }
                break;
            case 33:
                {
                alt2=3;
                }
                break;
            case RULE_ID:
            case 11:
            case 12:
            case 15:
            case 16:
            case 17:
            case 18:
            case 19:
            case 20:
            case 21:
            case 22:
            case 23:
            case 24:
            case 25:
            case 26:
            case 27:
            case 28:
            case 29:
            case 30:
            case 61:
            case 62:
            case 63:
            case 82:
            case 83:
            case 85:
            case 86:
            case 96:
                {
                alt2=4;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 2, 0, input);

                throw nvae;
            }

            switch (alt2) {
                case 1 :
                    // InternalLinkerScript.g:115:3: this_MemoryCommand_0= ruleMemoryCommand
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getLinkerScriptStatementAccess().getMemoryCommandParserRuleCall_0());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_MemoryCommand_0=ruleMemoryCommand();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_MemoryCommand_0;
                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 2 :
                    // InternalLinkerScript.g:124:3: this_SectionsCommand_1= ruleSectionsCommand
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getLinkerScriptStatementAccess().getSectionsCommandParserRuleCall_1());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_SectionsCommand_1=ruleSectionsCommand();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_SectionsCommand_1;
                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 3 :
                    // InternalLinkerScript.g:133:3: this_PhdrsCommand_2= rulePhdrsCommand
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getLinkerScriptStatementAccess().getPhdrsCommandParserRuleCall_2());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_PhdrsCommand_2=rulePhdrsCommand();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_PhdrsCommand_2;
                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 4 :
                    // InternalLinkerScript.g:142:3: this_StatementCommand_3= ruleStatementCommand
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getLinkerScriptStatementAccess().getStatementCommandParserRuleCall_3());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_StatementCommand_3=ruleStatementCommand();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_StatementCommand_3;
                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleLinkerScriptStatement"


    // $ANTLR start "entryRuleStatementCommand"
    // InternalLinkerScript.g:154:1: entryRuleStatementCommand returns [EObject current=null] : iv_ruleStatementCommand= ruleStatementCommand EOF ;
    public final EObject entryRuleStatementCommand() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleStatementCommand = null;


        try {
            // InternalLinkerScript.g:154:57: (iv_ruleStatementCommand= ruleStatementCommand EOF )
            // InternalLinkerScript.g:155:2: iv_ruleStatementCommand= ruleStatementCommand EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getStatementCommandRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleStatementCommand=ruleStatementCommand();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleStatementCommand; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleStatementCommand"


    // $ANTLR start "ruleStatementCommand"
    // InternalLinkerScript.g:161:1: ruleStatementCommand returns [EObject current=null] : ( ( () ( (lv_assignment_1_0= ruleAssignmentRule ) ) (otherlv_2= ',' | otherlv_3= ';' ) ) | ( () otherlv_5= 'STARTUP' otherlv_6= '(' ( (lv_name_7_0= ruleWildID ) ) otherlv_8= ')' ) | ( () otherlv_10= 'ENTRY' otherlv_11= '(' ( (lv_name_12_0= ruleValidID ) ) otherlv_13= ')' ) | ( () otherlv_15= 'ASSERT' otherlv_16= '(' ( (lv_exp_17_0= ruleLExpression ) ) otherlv_18= ',' ( (lv_message_19_0= ruleValidID ) ) otherlv_20= ')' ) | ( () otherlv_22= 'TARGET' otherlv_23= '(' ( (lv_name_24_0= ruleValidID ) ) otherlv_25= ')' ) | ( () otherlv_27= 'SEARCH_DIR' otherlv_28= '(' ( (lv_name_29_0= ruleValidID ) ) otherlv_30= ')' ) | ( () otherlv_32= 'OUTPUT' otherlv_33= '(' ( (lv_name_34_0= ruleValidID ) ) otherlv_35= ')' ) | ( () otherlv_37= 'OUTPUT_FORMAT' otherlv_38= '(' ( (lv_name_39_0= ruleValidID ) ) (otherlv_40= ',' ( (lv_big_41_0= ruleValidID ) ) otherlv_42= ',' ( (lv_little_43_0= ruleValidID ) ) )? otherlv_44= ')' ) | ( () otherlv_46= 'OUTPUT_ARCH' otherlv_47= '(' ( (lv_name_48_0= ruleValidID ) ) otherlv_49= ')' ) | ( () otherlv_51= 'FORCE_COMMON_ALLOCATION' ) | ( () otherlv_53= 'INHIBIT_COMMON_ALLOCATION' ) | ( () otherlv_55= 'INPUT' otherlv_56= '(' ( (lv_list_57_0= ruleFileList ) ) otherlv_58= ')' ) | ( () otherlv_60= 'GROUP' otherlv_61= '(' ( (lv_files_62_0= ruleFileListName ) ) ( (otherlv_63= ',' )? ( (lv_files_64_0= ruleFileListName ) ) )* otherlv_65= ')' ) | ( () otherlv_67= 'MAP' otherlv_68= '(' ( (lv_name_69_0= ruleValidID ) ) otherlv_70= ')' ) | ( () otherlv_72= 'NOCROSSREFS' otherlv_73= '(' ( (lv_sections_74_0= ruleValidID ) )* otherlv_75= ')' ) | ( () otherlv_77= 'NOCROSSREFS_TO' otherlv_78= '(' ( (lv_sections_79_0= ruleValidID ) )* otherlv_80= ')' ) | ( () otherlv_82= 'EXTERN' otherlv_83= '(' ( (lv_sections_84_0= ruleValidID ) )* otherlv_85= ')' ) | ( () otherlv_87= 'INCLUDE' ( (lv_name_88_0= ruleValidID ) ) ) | ( () otherlv_90= ';' ) ) ;
    public final EObject ruleStatementCommand() throws RecognitionException {
        EObject current = null;

        Token otherlv_2=null;
        Token otherlv_3=null;
        Token otherlv_5=null;
        Token otherlv_6=null;
        Token otherlv_8=null;
        Token otherlv_10=null;
        Token otherlv_11=null;
        Token otherlv_13=null;
        Token otherlv_15=null;
        Token otherlv_16=null;
        Token otherlv_18=null;
        Token otherlv_20=null;
        Token otherlv_22=null;
        Token otherlv_23=null;
        Token otherlv_25=null;
        Token otherlv_27=null;
        Token otherlv_28=null;
        Token otherlv_30=null;
        Token otherlv_32=null;
        Token otherlv_33=null;
        Token otherlv_35=null;
        Token otherlv_37=null;
        Token otherlv_38=null;
        Token otherlv_40=null;
        Token otherlv_42=null;
        Token otherlv_44=null;
        Token otherlv_46=null;
        Token otherlv_47=null;
        Token otherlv_49=null;
        Token otherlv_51=null;
        Token otherlv_53=null;
        Token otherlv_55=null;
        Token otherlv_56=null;
        Token otherlv_58=null;
        Token otherlv_60=null;
        Token otherlv_61=null;
        Token otherlv_63=null;
        Token otherlv_65=null;
        Token otherlv_67=null;
        Token otherlv_68=null;
        Token otherlv_70=null;
        Token otherlv_72=null;
        Token otherlv_73=null;
        Token otherlv_75=null;
        Token otherlv_77=null;
        Token otherlv_78=null;
        Token otherlv_80=null;
        Token otherlv_82=null;
        Token otherlv_83=null;
        Token otherlv_85=null;
        Token otherlv_87=null;
        Token otherlv_90=null;
        EObject lv_assignment_1_0 = null;

        AntlrDatatypeRuleToken lv_name_7_0 = null;

        AntlrDatatypeRuleToken lv_name_12_0 = null;

        EObject lv_exp_17_0 = null;

        AntlrDatatypeRuleToken lv_message_19_0 = null;

        AntlrDatatypeRuleToken lv_name_24_0 = null;

        AntlrDatatypeRuleToken lv_name_29_0 = null;

        AntlrDatatypeRuleToken lv_name_34_0 = null;

        AntlrDatatypeRuleToken lv_name_39_0 = null;

        AntlrDatatypeRuleToken lv_big_41_0 = null;

        AntlrDatatypeRuleToken lv_little_43_0 = null;

        AntlrDatatypeRuleToken lv_name_48_0 = null;

        EObject lv_list_57_0 = null;

        EObject lv_files_62_0 = null;

        EObject lv_files_64_0 = null;

        AntlrDatatypeRuleToken lv_name_69_0 = null;

        AntlrDatatypeRuleToken lv_sections_74_0 = null;

        AntlrDatatypeRuleToken lv_sections_79_0 = null;

        AntlrDatatypeRuleToken lv_sections_84_0 = null;

        AntlrDatatypeRuleToken lv_name_88_0 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:167:2: ( ( ( () ( (lv_assignment_1_0= ruleAssignmentRule ) ) (otherlv_2= ',' | otherlv_3= ';' ) ) | ( () otherlv_5= 'STARTUP' otherlv_6= '(' ( (lv_name_7_0= ruleWildID ) ) otherlv_8= ')' ) | ( () otherlv_10= 'ENTRY' otherlv_11= '(' ( (lv_name_12_0= ruleValidID ) ) otherlv_13= ')' ) | ( () otherlv_15= 'ASSERT' otherlv_16= '(' ( (lv_exp_17_0= ruleLExpression ) ) otherlv_18= ',' ( (lv_message_19_0= ruleValidID ) ) otherlv_20= ')' ) | ( () otherlv_22= 'TARGET' otherlv_23= '(' ( (lv_name_24_0= ruleValidID ) ) otherlv_25= ')' ) | ( () otherlv_27= 'SEARCH_DIR' otherlv_28= '(' ( (lv_name_29_0= ruleValidID ) ) otherlv_30= ')' ) | ( () otherlv_32= 'OUTPUT' otherlv_33= '(' ( (lv_name_34_0= ruleValidID ) ) otherlv_35= ')' ) | ( () otherlv_37= 'OUTPUT_FORMAT' otherlv_38= '(' ( (lv_name_39_0= ruleValidID ) ) (otherlv_40= ',' ( (lv_big_41_0= ruleValidID ) ) otherlv_42= ',' ( (lv_little_43_0= ruleValidID ) ) )? otherlv_44= ')' ) | ( () otherlv_46= 'OUTPUT_ARCH' otherlv_47= '(' ( (lv_name_48_0= ruleValidID ) ) otherlv_49= ')' ) | ( () otherlv_51= 'FORCE_COMMON_ALLOCATION' ) | ( () otherlv_53= 'INHIBIT_COMMON_ALLOCATION' ) | ( () otherlv_55= 'INPUT' otherlv_56= '(' ( (lv_list_57_0= ruleFileList ) ) otherlv_58= ')' ) | ( () otherlv_60= 'GROUP' otherlv_61= '(' ( (lv_files_62_0= ruleFileListName ) ) ( (otherlv_63= ',' )? ( (lv_files_64_0= ruleFileListName ) ) )* otherlv_65= ')' ) | ( () otherlv_67= 'MAP' otherlv_68= '(' ( (lv_name_69_0= ruleValidID ) ) otherlv_70= ')' ) | ( () otherlv_72= 'NOCROSSREFS' otherlv_73= '(' ( (lv_sections_74_0= ruleValidID ) )* otherlv_75= ')' ) | ( () otherlv_77= 'NOCROSSREFS_TO' otherlv_78= '(' ( (lv_sections_79_0= ruleValidID ) )* otherlv_80= ')' ) | ( () otherlv_82= 'EXTERN' otherlv_83= '(' ( (lv_sections_84_0= ruleValidID ) )* otherlv_85= ')' ) | ( () otherlv_87= 'INCLUDE' ( (lv_name_88_0= ruleValidID ) ) ) | ( () otherlv_90= ';' ) ) )
            // InternalLinkerScript.g:168:2: ( ( () ( (lv_assignment_1_0= ruleAssignmentRule ) ) (otherlv_2= ',' | otherlv_3= ';' ) ) | ( () otherlv_5= 'STARTUP' otherlv_6= '(' ( (lv_name_7_0= ruleWildID ) ) otherlv_8= ')' ) | ( () otherlv_10= 'ENTRY' otherlv_11= '(' ( (lv_name_12_0= ruleValidID ) ) otherlv_13= ')' ) | ( () otherlv_15= 'ASSERT' otherlv_16= '(' ( (lv_exp_17_0= ruleLExpression ) ) otherlv_18= ',' ( (lv_message_19_0= ruleValidID ) ) otherlv_20= ')' ) | ( () otherlv_22= 'TARGET' otherlv_23= '(' ( (lv_name_24_0= ruleValidID ) ) otherlv_25= ')' ) | ( () otherlv_27= 'SEARCH_DIR' otherlv_28= '(' ( (lv_name_29_0= ruleValidID ) ) otherlv_30= ')' ) | ( () otherlv_32= 'OUTPUT' otherlv_33= '(' ( (lv_name_34_0= ruleValidID ) ) otherlv_35= ')' ) | ( () otherlv_37= 'OUTPUT_FORMAT' otherlv_38= '(' ( (lv_name_39_0= ruleValidID ) ) (otherlv_40= ',' ( (lv_big_41_0= ruleValidID ) ) otherlv_42= ',' ( (lv_little_43_0= ruleValidID ) ) )? otherlv_44= ')' ) | ( () otherlv_46= 'OUTPUT_ARCH' otherlv_47= '(' ( (lv_name_48_0= ruleValidID ) ) otherlv_49= ')' ) | ( () otherlv_51= 'FORCE_COMMON_ALLOCATION' ) | ( () otherlv_53= 'INHIBIT_COMMON_ALLOCATION' ) | ( () otherlv_55= 'INPUT' otherlv_56= '(' ( (lv_list_57_0= ruleFileList ) ) otherlv_58= ')' ) | ( () otherlv_60= 'GROUP' otherlv_61= '(' ( (lv_files_62_0= ruleFileListName ) ) ( (otherlv_63= ',' )? ( (lv_files_64_0= ruleFileListName ) ) )* otherlv_65= ')' ) | ( () otherlv_67= 'MAP' otherlv_68= '(' ( (lv_name_69_0= ruleValidID ) ) otherlv_70= ')' ) | ( () otherlv_72= 'NOCROSSREFS' otherlv_73= '(' ( (lv_sections_74_0= ruleValidID ) )* otherlv_75= ')' ) | ( () otherlv_77= 'NOCROSSREFS_TO' otherlv_78= '(' ( (lv_sections_79_0= ruleValidID ) )* otherlv_80= ')' ) | ( () otherlv_82= 'EXTERN' otherlv_83= '(' ( (lv_sections_84_0= ruleValidID ) )* otherlv_85= ')' ) | ( () otherlv_87= 'INCLUDE' ( (lv_name_88_0= ruleValidID ) ) ) | ( () otherlv_90= ';' ) )
            {
            // InternalLinkerScript.g:168:2: ( ( () ( (lv_assignment_1_0= ruleAssignmentRule ) ) (otherlv_2= ',' | otherlv_3= ';' ) ) | ( () otherlv_5= 'STARTUP' otherlv_6= '(' ( (lv_name_7_0= ruleWildID ) ) otherlv_8= ')' ) | ( () otherlv_10= 'ENTRY' otherlv_11= '(' ( (lv_name_12_0= ruleValidID ) ) otherlv_13= ')' ) | ( () otherlv_15= 'ASSERT' otherlv_16= '(' ( (lv_exp_17_0= ruleLExpression ) ) otherlv_18= ',' ( (lv_message_19_0= ruleValidID ) ) otherlv_20= ')' ) | ( () otherlv_22= 'TARGET' otherlv_23= '(' ( (lv_name_24_0= ruleValidID ) ) otherlv_25= ')' ) | ( () otherlv_27= 'SEARCH_DIR' otherlv_28= '(' ( (lv_name_29_0= ruleValidID ) ) otherlv_30= ')' ) | ( () otherlv_32= 'OUTPUT' otherlv_33= '(' ( (lv_name_34_0= ruleValidID ) ) otherlv_35= ')' ) | ( () otherlv_37= 'OUTPUT_FORMAT' otherlv_38= '(' ( (lv_name_39_0= ruleValidID ) ) (otherlv_40= ',' ( (lv_big_41_0= ruleValidID ) ) otherlv_42= ',' ( (lv_little_43_0= ruleValidID ) ) )? otherlv_44= ')' ) | ( () otherlv_46= 'OUTPUT_ARCH' otherlv_47= '(' ( (lv_name_48_0= ruleValidID ) ) otherlv_49= ')' ) | ( () otherlv_51= 'FORCE_COMMON_ALLOCATION' ) | ( () otherlv_53= 'INHIBIT_COMMON_ALLOCATION' ) | ( () otherlv_55= 'INPUT' otherlv_56= '(' ( (lv_list_57_0= ruleFileList ) ) otherlv_58= ')' ) | ( () otherlv_60= 'GROUP' otherlv_61= '(' ( (lv_files_62_0= ruleFileListName ) ) ( (otherlv_63= ',' )? ( (lv_files_64_0= ruleFileListName ) ) )* otherlv_65= ')' ) | ( () otherlv_67= 'MAP' otherlv_68= '(' ( (lv_name_69_0= ruleValidID ) ) otherlv_70= ')' ) | ( () otherlv_72= 'NOCROSSREFS' otherlv_73= '(' ( (lv_sections_74_0= ruleValidID ) )* otherlv_75= ')' ) | ( () otherlv_77= 'NOCROSSREFS_TO' otherlv_78= '(' ( (lv_sections_79_0= ruleValidID ) )* otherlv_80= ')' ) | ( () otherlv_82= 'EXTERN' otherlv_83= '(' ( (lv_sections_84_0= ruleValidID ) )* otherlv_85= ')' ) | ( () otherlv_87= 'INCLUDE' ( (lv_name_88_0= ruleValidID ) ) ) | ( () otherlv_90= ';' ) )
            int alt10=19;
            switch ( input.LA(1) ) {
            case RULE_ID:
            case 61:
            case 62:
            case 63:
            case 80:
            case 82:
            case 83:
            case 85:
            case 86:
            case 96:
                {
                alt10=1;
                }
                break;
            case 12:
                {
                alt10=2;
                }
                break;
            case 15:
                {
                alt10=3;
                }
                break;
            case 16:
                {
                alt10=4;
                }
                break;
            case 17:
                {
                alt10=5;
                }
                break;
            case 18:
                {
                alt10=6;
                }
                break;
            case 19:
                {
                alt10=7;
                }
                break;
            case 20:
                {
                alt10=8;
                }
                break;
            case 21:
                {
                alt10=9;
                }
                break;
            case 22:
                {
                alt10=10;
                }
                break;
            case 23:
                {
                alt10=11;
                }
                break;
            case 24:
                {
                alt10=12;
                }
                break;
            case 25:
                {
                alt10=13;
                }
                break;
            case 26:
                {
                alt10=14;
                }
                break;
            case 27:
                {
                alt10=15;
                }
                break;
            case 28:
                {
                alt10=16;
                }
                break;
            case 29:
                {
                alt10=17;
                }
                break;
            case 30:
                {
                alt10=18;
                }
                break;
            case 11:
                {
                alt10=19;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 10, 0, input);

                throw nvae;
            }

            switch (alt10) {
                case 1 :
                    // InternalLinkerScript.g:169:3: ( () ( (lv_assignment_1_0= ruleAssignmentRule ) ) (otherlv_2= ',' | otherlv_3= ';' ) )
                    {
                    // InternalLinkerScript.g:169:3: ( () ( (lv_assignment_1_0= ruleAssignmentRule ) ) (otherlv_2= ',' | otherlv_3= ';' ) )
                    // InternalLinkerScript.g:170:4: () ( (lv_assignment_1_0= ruleAssignmentRule ) ) (otherlv_2= ',' | otherlv_3= ';' )
                    {
                    // InternalLinkerScript.g:170:4: ()
                    // InternalLinkerScript.g:171:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getStatementCommandAccess().getStatementAssignmentAction_0_0(),
                      						current);
                      				
                    }

                    }

                    // InternalLinkerScript.g:177:4: ( (lv_assignment_1_0= ruleAssignmentRule ) )
                    // InternalLinkerScript.g:178:5: (lv_assignment_1_0= ruleAssignmentRule )
                    {
                    // InternalLinkerScript.g:178:5: (lv_assignment_1_0= ruleAssignmentRule )
                    // InternalLinkerScript.g:179:6: lv_assignment_1_0= ruleAssignmentRule
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getStatementCommandAccess().getAssignmentAssignmentRuleParserRuleCall_0_1_0());
                      					
                    }
                    pushFollow(FOLLOW_4);
                    lv_assignment_1_0=ruleAssignmentRule();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getStatementCommandRule());
                      						}
                      						set(
                      							current,
                      							"assignment",
                      							lv_assignment_1_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.AssignmentRule");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    // InternalLinkerScript.g:196:4: (otherlv_2= ',' | otherlv_3= ';' )
                    int alt3=2;
                    int LA3_0 = input.LA(1);

                    if ( (LA3_0==10) ) {
                        alt3=1;
                    }
                    else if ( (LA3_0==11) ) {
                        alt3=2;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return current;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 3, 0, input);

                        throw nvae;
                    }
                    switch (alt3) {
                        case 1 :
                            // InternalLinkerScript.g:197:5: otherlv_2= ','
                            {
                            otherlv_2=(Token)match(input,10,FOLLOW_2); if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              					newLeafNode(otherlv_2, grammarAccess.getStatementCommandAccess().getCommaKeyword_0_2_0());
                              				
                            }

                            }
                            break;
                        case 2 :
                            // InternalLinkerScript.g:202:5: otherlv_3= ';'
                            {
                            otherlv_3=(Token)match(input,11,FOLLOW_2); if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              					newLeafNode(otherlv_3, grammarAccess.getStatementCommandAccess().getSemicolonKeyword_0_2_1());
                              				
                            }

                            }
                            break;

                    }


                    }


                    }
                    break;
                case 2 :
                    // InternalLinkerScript.g:209:3: ( () otherlv_5= 'STARTUP' otherlv_6= '(' ( (lv_name_7_0= ruleWildID ) ) otherlv_8= ')' )
                    {
                    // InternalLinkerScript.g:209:3: ( () otherlv_5= 'STARTUP' otherlv_6= '(' ( (lv_name_7_0= ruleWildID ) ) otherlv_8= ')' )
                    // InternalLinkerScript.g:210:4: () otherlv_5= 'STARTUP' otherlv_6= '(' ( (lv_name_7_0= ruleWildID ) ) otherlv_8= ')'
                    {
                    // InternalLinkerScript.g:210:4: ()
                    // InternalLinkerScript.g:211:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getStatementCommandAccess().getStatementStartupAction_1_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_5=(Token)match(input,12,FOLLOW_5); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_5, grammarAccess.getStatementCommandAccess().getSTARTUPKeyword_1_1());
                      			
                    }
                    otherlv_6=(Token)match(input,13,FOLLOW_6); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_6, grammarAccess.getStatementCommandAccess().getLeftParenthesisKeyword_1_2());
                      			
                    }
                    // InternalLinkerScript.g:225:4: ( (lv_name_7_0= ruleWildID ) )
                    // InternalLinkerScript.g:226:5: (lv_name_7_0= ruleWildID )
                    {
                    // InternalLinkerScript.g:226:5: (lv_name_7_0= ruleWildID )
                    // InternalLinkerScript.g:227:6: lv_name_7_0= ruleWildID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getStatementCommandAccess().getNameWildIDParserRuleCall_1_3_0());
                      					
                    }
                    pushFollow(FOLLOW_7);
                    lv_name_7_0=ruleWildID();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getStatementCommandRule());
                      						}
                      						set(
                      							current,
                      							"name",
                      							lv_name_7_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.WildID");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    otherlv_8=(Token)match(input,14,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_8, grammarAccess.getStatementCommandAccess().getRightParenthesisKeyword_1_4());
                      			
                    }

                    }


                    }
                    break;
                case 3 :
                    // InternalLinkerScript.g:250:3: ( () otherlv_10= 'ENTRY' otherlv_11= '(' ( (lv_name_12_0= ruleValidID ) ) otherlv_13= ')' )
                    {
                    // InternalLinkerScript.g:250:3: ( () otherlv_10= 'ENTRY' otherlv_11= '(' ( (lv_name_12_0= ruleValidID ) ) otherlv_13= ')' )
                    // InternalLinkerScript.g:251:4: () otherlv_10= 'ENTRY' otherlv_11= '(' ( (lv_name_12_0= ruleValidID ) ) otherlv_13= ')'
                    {
                    // InternalLinkerScript.g:251:4: ()
                    // InternalLinkerScript.g:252:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getStatementCommandAccess().getStatementEntryAction_2_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_10=(Token)match(input,15,FOLLOW_5); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_10, grammarAccess.getStatementCommandAccess().getENTRYKeyword_2_1());
                      			
                    }
                    otherlv_11=(Token)match(input,13,FOLLOW_6); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_11, grammarAccess.getStatementCommandAccess().getLeftParenthesisKeyword_2_2());
                      			
                    }
                    // InternalLinkerScript.g:266:4: ( (lv_name_12_0= ruleValidID ) )
                    // InternalLinkerScript.g:267:5: (lv_name_12_0= ruleValidID )
                    {
                    // InternalLinkerScript.g:267:5: (lv_name_12_0= ruleValidID )
                    // InternalLinkerScript.g:268:6: lv_name_12_0= ruleValidID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getStatementCommandAccess().getNameValidIDParserRuleCall_2_3_0());
                      					
                    }
                    pushFollow(FOLLOW_7);
                    lv_name_12_0=ruleValidID();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getStatementCommandRule());
                      						}
                      						set(
                      							current,
                      							"name",
                      							lv_name_12_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.ValidID");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    otherlv_13=(Token)match(input,14,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_13, grammarAccess.getStatementCommandAccess().getRightParenthesisKeyword_2_4());
                      			
                    }

                    }


                    }
                    break;
                case 4 :
                    // InternalLinkerScript.g:291:3: ( () otherlv_15= 'ASSERT' otherlv_16= '(' ( (lv_exp_17_0= ruleLExpression ) ) otherlv_18= ',' ( (lv_message_19_0= ruleValidID ) ) otherlv_20= ')' )
                    {
                    // InternalLinkerScript.g:291:3: ( () otherlv_15= 'ASSERT' otherlv_16= '(' ( (lv_exp_17_0= ruleLExpression ) ) otherlv_18= ',' ( (lv_message_19_0= ruleValidID ) ) otherlv_20= ')' )
                    // InternalLinkerScript.g:292:4: () otherlv_15= 'ASSERT' otherlv_16= '(' ( (lv_exp_17_0= ruleLExpression ) ) otherlv_18= ',' ( (lv_message_19_0= ruleValidID ) ) otherlv_20= ')'
                    {
                    // InternalLinkerScript.g:292:4: ()
                    // InternalLinkerScript.g:293:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getStatementCommandAccess().getStatementAssertAction_3_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_15=(Token)match(input,16,FOLLOW_5); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_15, grammarAccess.getStatementCommandAccess().getASSERTKeyword_3_1());
                      			
                    }
                    otherlv_16=(Token)match(input,13,FOLLOW_8); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_16, grammarAccess.getStatementCommandAccess().getLeftParenthesisKeyword_3_2());
                      			
                    }
                    // InternalLinkerScript.g:307:4: ( (lv_exp_17_0= ruleLExpression ) )
                    // InternalLinkerScript.g:308:5: (lv_exp_17_0= ruleLExpression )
                    {
                    // InternalLinkerScript.g:308:5: (lv_exp_17_0= ruleLExpression )
                    // InternalLinkerScript.g:309:6: lv_exp_17_0= ruleLExpression
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getStatementCommandAccess().getExpLExpressionParserRuleCall_3_3_0());
                      					
                    }
                    pushFollow(FOLLOW_9);
                    lv_exp_17_0=ruleLExpression();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getStatementCommandRule());
                      						}
                      						set(
                      							current,
                      							"exp",
                      							lv_exp_17_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.LExpression");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    otherlv_18=(Token)match(input,10,FOLLOW_6); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_18, grammarAccess.getStatementCommandAccess().getCommaKeyword_3_4());
                      			
                    }
                    // InternalLinkerScript.g:330:4: ( (lv_message_19_0= ruleValidID ) )
                    // InternalLinkerScript.g:331:5: (lv_message_19_0= ruleValidID )
                    {
                    // InternalLinkerScript.g:331:5: (lv_message_19_0= ruleValidID )
                    // InternalLinkerScript.g:332:6: lv_message_19_0= ruleValidID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getStatementCommandAccess().getMessageValidIDParserRuleCall_3_5_0());
                      					
                    }
                    pushFollow(FOLLOW_7);
                    lv_message_19_0=ruleValidID();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getStatementCommandRule());
                      						}
                      						set(
                      							current,
                      							"message",
                      							lv_message_19_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.ValidID");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    otherlv_20=(Token)match(input,14,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_20, grammarAccess.getStatementCommandAccess().getRightParenthesisKeyword_3_6());
                      			
                    }

                    }


                    }
                    break;
                case 5 :
                    // InternalLinkerScript.g:355:3: ( () otherlv_22= 'TARGET' otherlv_23= '(' ( (lv_name_24_0= ruleValidID ) ) otherlv_25= ')' )
                    {
                    // InternalLinkerScript.g:355:3: ( () otherlv_22= 'TARGET' otherlv_23= '(' ( (lv_name_24_0= ruleValidID ) ) otherlv_25= ')' )
                    // InternalLinkerScript.g:356:4: () otherlv_22= 'TARGET' otherlv_23= '(' ( (lv_name_24_0= ruleValidID ) ) otherlv_25= ')'
                    {
                    // InternalLinkerScript.g:356:4: ()
                    // InternalLinkerScript.g:357:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getStatementCommandAccess().getStatementTargetAction_4_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_22=(Token)match(input,17,FOLLOW_5); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_22, grammarAccess.getStatementCommandAccess().getTARGETKeyword_4_1());
                      			
                    }
                    otherlv_23=(Token)match(input,13,FOLLOW_6); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_23, grammarAccess.getStatementCommandAccess().getLeftParenthesisKeyword_4_2());
                      			
                    }
                    // InternalLinkerScript.g:371:4: ( (lv_name_24_0= ruleValidID ) )
                    // InternalLinkerScript.g:372:5: (lv_name_24_0= ruleValidID )
                    {
                    // InternalLinkerScript.g:372:5: (lv_name_24_0= ruleValidID )
                    // InternalLinkerScript.g:373:6: lv_name_24_0= ruleValidID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getStatementCommandAccess().getNameValidIDParserRuleCall_4_3_0());
                      					
                    }
                    pushFollow(FOLLOW_7);
                    lv_name_24_0=ruleValidID();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getStatementCommandRule());
                      						}
                      						set(
                      							current,
                      							"name",
                      							lv_name_24_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.ValidID");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    otherlv_25=(Token)match(input,14,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_25, grammarAccess.getStatementCommandAccess().getRightParenthesisKeyword_4_4());
                      			
                    }

                    }


                    }
                    break;
                case 6 :
                    // InternalLinkerScript.g:396:3: ( () otherlv_27= 'SEARCH_DIR' otherlv_28= '(' ( (lv_name_29_0= ruleValidID ) ) otherlv_30= ')' )
                    {
                    // InternalLinkerScript.g:396:3: ( () otherlv_27= 'SEARCH_DIR' otherlv_28= '(' ( (lv_name_29_0= ruleValidID ) ) otherlv_30= ')' )
                    // InternalLinkerScript.g:397:4: () otherlv_27= 'SEARCH_DIR' otherlv_28= '(' ( (lv_name_29_0= ruleValidID ) ) otherlv_30= ')'
                    {
                    // InternalLinkerScript.g:397:4: ()
                    // InternalLinkerScript.g:398:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getStatementCommandAccess().getStatementSearchDirAction_5_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_27=(Token)match(input,18,FOLLOW_5); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_27, grammarAccess.getStatementCommandAccess().getSEARCH_DIRKeyword_5_1());
                      			
                    }
                    otherlv_28=(Token)match(input,13,FOLLOW_6); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_28, grammarAccess.getStatementCommandAccess().getLeftParenthesisKeyword_5_2());
                      			
                    }
                    // InternalLinkerScript.g:412:4: ( (lv_name_29_0= ruleValidID ) )
                    // InternalLinkerScript.g:413:5: (lv_name_29_0= ruleValidID )
                    {
                    // InternalLinkerScript.g:413:5: (lv_name_29_0= ruleValidID )
                    // InternalLinkerScript.g:414:6: lv_name_29_0= ruleValidID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getStatementCommandAccess().getNameValidIDParserRuleCall_5_3_0());
                      					
                    }
                    pushFollow(FOLLOW_7);
                    lv_name_29_0=ruleValidID();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getStatementCommandRule());
                      						}
                      						set(
                      							current,
                      							"name",
                      							lv_name_29_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.ValidID");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    otherlv_30=(Token)match(input,14,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_30, grammarAccess.getStatementCommandAccess().getRightParenthesisKeyword_5_4());
                      			
                    }

                    }


                    }
                    break;
                case 7 :
                    // InternalLinkerScript.g:437:3: ( () otherlv_32= 'OUTPUT' otherlv_33= '(' ( (lv_name_34_0= ruleValidID ) ) otherlv_35= ')' )
                    {
                    // InternalLinkerScript.g:437:3: ( () otherlv_32= 'OUTPUT' otherlv_33= '(' ( (lv_name_34_0= ruleValidID ) ) otherlv_35= ')' )
                    // InternalLinkerScript.g:438:4: () otherlv_32= 'OUTPUT' otherlv_33= '(' ( (lv_name_34_0= ruleValidID ) ) otherlv_35= ')'
                    {
                    // InternalLinkerScript.g:438:4: ()
                    // InternalLinkerScript.g:439:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getStatementCommandAccess().getStatementOutputAction_6_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_32=(Token)match(input,19,FOLLOW_5); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_32, grammarAccess.getStatementCommandAccess().getOUTPUTKeyword_6_1());
                      			
                    }
                    otherlv_33=(Token)match(input,13,FOLLOW_6); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_33, grammarAccess.getStatementCommandAccess().getLeftParenthesisKeyword_6_2());
                      			
                    }
                    // InternalLinkerScript.g:453:4: ( (lv_name_34_0= ruleValidID ) )
                    // InternalLinkerScript.g:454:5: (lv_name_34_0= ruleValidID )
                    {
                    // InternalLinkerScript.g:454:5: (lv_name_34_0= ruleValidID )
                    // InternalLinkerScript.g:455:6: lv_name_34_0= ruleValidID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getStatementCommandAccess().getNameValidIDParserRuleCall_6_3_0());
                      					
                    }
                    pushFollow(FOLLOW_7);
                    lv_name_34_0=ruleValidID();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getStatementCommandRule());
                      						}
                      						set(
                      							current,
                      							"name",
                      							lv_name_34_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.ValidID");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    otherlv_35=(Token)match(input,14,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_35, grammarAccess.getStatementCommandAccess().getRightParenthesisKeyword_6_4());
                      			
                    }

                    }


                    }
                    break;
                case 8 :
                    // InternalLinkerScript.g:478:3: ( () otherlv_37= 'OUTPUT_FORMAT' otherlv_38= '(' ( (lv_name_39_0= ruleValidID ) ) (otherlv_40= ',' ( (lv_big_41_0= ruleValidID ) ) otherlv_42= ',' ( (lv_little_43_0= ruleValidID ) ) )? otherlv_44= ')' )
                    {
                    // InternalLinkerScript.g:478:3: ( () otherlv_37= 'OUTPUT_FORMAT' otherlv_38= '(' ( (lv_name_39_0= ruleValidID ) ) (otherlv_40= ',' ( (lv_big_41_0= ruleValidID ) ) otherlv_42= ',' ( (lv_little_43_0= ruleValidID ) ) )? otherlv_44= ')' )
                    // InternalLinkerScript.g:479:4: () otherlv_37= 'OUTPUT_FORMAT' otherlv_38= '(' ( (lv_name_39_0= ruleValidID ) ) (otherlv_40= ',' ( (lv_big_41_0= ruleValidID ) ) otherlv_42= ',' ( (lv_little_43_0= ruleValidID ) ) )? otherlv_44= ')'
                    {
                    // InternalLinkerScript.g:479:4: ()
                    // InternalLinkerScript.g:480:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getStatementCommandAccess().getStatementOutputFormatAction_7_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_37=(Token)match(input,20,FOLLOW_5); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_37, grammarAccess.getStatementCommandAccess().getOUTPUT_FORMATKeyword_7_1());
                      			
                    }
                    otherlv_38=(Token)match(input,13,FOLLOW_6); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_38, grammarAccess.getStatementCommandAccess().getLeftParenthesisKeyword_7_2());
                      			
                    }
                    // InternalLinkerScript.g:494:4: ( (lv_name_39_0= ruleValidID ) )
                    // InternalLinkerScript.g:495:5: (lv_name_39_0= ruleValidID )
                    {
                    // InternalLinkerScript.g:495:5: (lv_name_39_0= ruleValidID )
                    // InternalLinkerScript.g:496:6: lv_name_39_0= ruleValidID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getStatementCommandAccess().getNameValidIDParserRuleCall_7_3_0());
                      					
                    }
                    pushFollow(FOLLOW_10);
                    lv_name_39_0=ruleValidID();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getStatementCommandRule());
                      						}
                      						set(
                      							current,
                      							"name",
                      							lv_name_39_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.ValidID");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    // InternalLinkerScript.g:513:4: (otherlv_40= ',' ( (lv_big_41_0= ruleValidID ) ) otherlv_42= ',' ( (lv_little_43_0= ruleValidID ) ) )?
                    int alt4=2;
                    int LA4_0 = input.LA(1);

                    if ( (LA4_0==10) ) {
                        alt4=1;
                    }
                    switch (alt4) {
                        case 1 :
                            // InternalLinkerScript.g:514:5: otherlv_40= ',' ( (lv_big_41_0= ruleValidID ) ) otherlv_42= ',' ( (lv_little_43_0= ruleValidID ) )
                            {
                            otherlv_40=(Token)match(input,10,FOLLOW_6); if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              					newLeafNode(otherlv_40, grammarAccess.getStatementCommandAccess().getCommaKeyword_7_4_0());
                              				
                            }
                            // InternalLinkerScript.g:518:5: ( (lv_big_41_0= ruleValidID ) )
                            // InternalLinkerScript.g:519:6: (lv_big_41_0= ruleValidID )
                            {
                            // InternalLinkerScript.g:519:6: (lv_big_41_0= ruleValidID )
                            // InternalLinkerScript.g:520:7: lv_big_41_0= ruleValidID
                            {
                            if ( state.backtracking==0 ) {

                              							newCompositeNode(grammarAccess.getStatementCommandAccess().getBigValidIDParserRuleCall_7_4_1_0());
                              						
                            }
                            pushFollow(FOLLOW_9);
                            lv_big_41_0=ruleValidID();

                            state._fsp--;
                            if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              							if (current==null) {
                              								current = createModelElementForParent(grammarAccess.getStatementCommandRule());
                              							}
                              							set(
                              								current,
                              								"big",
                              								lv_big_41_0,
                              								"org.eclipse.cdt.linkerscript.LinkerScript.ValidID");
                              							afterParserOrEnumRuleCall();
                              						
                            }

                            }


                            }

                            otherlv_42=(Token)match(input,10,FOLLOW_6); if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              					newLeafNode(otherlv_42, grammarAccess.getStatementCommandAccess().getCommaKeyword_7_4_2());
                              				
                            }
                            // InternalLinkerScript.g:541:5: ( (lv_little_43_0= ruleValidID ) )
                            // InternalLinkerScript.g:542:6: (lv_little_43_0= ruleValidID )
                            {
                            // InternalLinkerScript.g:542:6: (lv_little_43_0= ruleValidID )
                            // InternalLinkerScript.g:543:7: lv_little_43_0= ruleValidID
                            {
                            if ( state.backtracking==0 ) {

                              							newCompositeNode(grammarAccess.getStatementCommandAccess().getLittleValidIDParserRuleCall_7_4_3_0());
                              						
                            }
                            pushFollow(FOLLOW_7);
                            lv_little_43_0=ruleValidID();

                            state._fsp--;
                            if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              							if (current==null) {
                              								current = createModelElementForParent(grammarAccess.getStatementCommandRule());
                              							}
                              							set(
                              								current,
                              								"little",
                              								lv_little_43_0,
                              								"org.eclipse.cdt.linkerscript.LinkerScript.ValidID");
                              							afterParserOrEnumRuleCall();
                              						
                            }

                            }


                            }


                            }
                            break;

                    }

                    otherlv_44=(Token)match(input,14,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_44, grammarAccess.getStatementCommandAccess().getRightParenthesisKeyword_7_5());
                      			
                    }

                    }


                    }
                    break;
                case 9 :
                    // InternalLinkerScript.g:567:3: ( () otherlv_46= 'OUTPUT_ARCH' otherlv_47= '(' ( (lv_name_48_0= ruleValidID ) ) otherlv_49= ')' )
                    {
                    // InternalLinkerScript.g:567:3: ( () otherlv_46= 'OUTPUT_ARCH' otherlv_47= '(' ( (lv_name_48_0= ruleValidID ) ) otherlv_49= ')' )
                    // InternalLinkerScript.g:568:4: () otherlv_46= 'OUTPUT_ARCH' otherlv_47= '(' ( (lv_name_48_0= ruleValidID ) ) otherlv_49= ')'
                    {
                    // InternalLinkerScript.g:568:4: ()
                    // InternalLinkerScript.g:569:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getStatementCommandAccess().getStatementOutputArchAction_8_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_46=(Token)match(input,21,FOLLOW_5); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_46, grammarAccess.getStatementCommandAccess().getOUTPUT_ARCHKeyword_8_1());
                      			
                    }
                    otherlv_47=(Token)match(input,13,FOLLOW_6); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_47, grammarAccess.getStatementCommandAccess().getLeftParenthesisKeyword_8_2());
                      			
                    }
                    // InternalLinkerScript.g:583:4: ( (lv_name_48_0= ruleValidID ) )
                    // InternalLinkerScript.g:584:5: (lv_name_48_0= ruleValidID )
                    {
                    // InternalLinkerScript.g:584:5: (lv_name_48_0= ruleValidID )
                    // InternalLinkerScript.g:585:6: lv_name_48_0= ruleValidID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getStatementCommandAccess().getNameValidIDParserRuleCall_8_3_0());
                      					
                    }
                    pushFollow(FOLLOW_7);
                    lv_name_48_0=ruleValidID();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getStatementCommandRule());
                      						}
                      						set(
                      							current,
                      							"name",
                      							lv_name_48_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.ValidID");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    otherlv_49=(Token)match(input,14,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_49, grammarAccess.getStatementCommandAccess().getRightParenthesisKeyword_8_4());
                      			
                    }

                    }


                    }
                    break;
                case 10 :
                    // InternalLinkerScript.g:608:3: ( () otherlv_51= 'FORCE_COMMON_ALLOCATION' )
                    {
                    // InternalLinkerScript.g:608:3: ( () otherlv_51= 'FORCE_COMMON_ALLOCATION' )
                    // InternalLinkerScript.g:609:4: () otherlv_51= 'FORCE_COMMON_ALLOCATION'
                    {
                    // InternalLinkerScript.g:609:4: ()
                    // InternalLinkerScript.g:610:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getStatementCommandAccess().getStatementForceCommonAllocationAction_9_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_51=(Token)match(input,22,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_51, grammarAccess.getStatementCommandAccess().getFORCE_COMMON_ALLOCATIONKeyword_9_1());
                      			
                    }

                    }


                    }
                    break;
                case 11 :
                    // InternalLinkerScript.g:622:3: ( () otherlv_53= 'INHIBIT_COMMON_ALLOCATION' )
                    {
                    // InternalLinkerScript.g:622:3: ( () otherlv_53= 'INHIBIT_COMMON_ALLOCATION' )
                    // InternalLinkerScript.g:623:4: () otherlv_53= 'INHIBIT_COMMON_ALLOCATION'
                    {
                    // InternalLinkerScript.g:623:4: ()
                    // InternalLinkerScript.g:624:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getStatementCommandAccess().getStatementInhibitCommonAllocationAction_10_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_53=(Token)match(input,23,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_53, grammarAccess.getStatementCommandAccess().getINHIBIT_COMMON_ALLOCATIONKeyword_10_1());
                      			
                    }

                    }


                    }
                    break;
                case 12 :
                    // InternalLinkerScript.g:636:3: ( () otherlv_55= 'INPUT' otherlv_56= '(' ( (lv_list_57_0= ruleFileList ) ) otherlv_58= ')' )
                    {
                    // InternalLinkerScript.g:636:3: ( () otherlv_55= 'INPUT' otherlv_56= '(' ( (lv_list_57_0= ruleFileList ) ) otherlv_58= ')' )
                    // InternalLinkerScript.g:637:4: () otherlv_55= 'INPUT' otherlv_56= '(' ( (lv_list_57_0= ruleFileList ) ) otherlv_58= ')'
                    {
                    // InternalLinkerScript.g:637:4: ()
                    // InternalLinkerScript.g:638:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getStatementCommandAccess().getStatementInputAction_11_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_55=(Token)match(input,24,FOLLOW_5); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_55, grammarAccess.getStatementCommandAccess().getINPUTKeyword_11_1());
                      			
                    }
                    otherlv_56=(Token)match(input,13,FOLLOW_11); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_56, grammarAccess.getStatementCommandAccess().getLeftParenthesisKeyword_11_2());
                      			
                    }
                    // InternalLinkerScript.g:652:4: ( (lv_list_57_0= ruleFileList ) )
                    // InternalLinkerScript.g:653:5: (lv_list_57_0= ruleFileList )
                    {
                    // InternalLinkerScript.g:653:5: (lv_list_57_0= ruleFileList )
                    // InternalLinkerScript.g:654:6: lv_list_57_0= ruleFileList
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getStatementCommandAccess().getListFileListParserRuleCall_11_3_0());
                      					
                    }
                    pushFollow(FOLLOW_7);
                    lv_list_57_0=ruleFileList();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getStatementCommandRule());
                      						}
                      						set(
                      							current,
                      							"list",
                      							lv_list_57_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.FileList");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    otherlv_58=(Token)match(input,14,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_58, grammarAccess.getStatementCommandAccess().getRightParenthesisKeyword_11_4());
                      			
                    }

                    }


                    }
                    break;
                case 13 :
                    // InternalLinkerScript.g:677:3: ( () otherlv_60= 'GROUP' otherlv_61= '(' ( (lv_files_62_0= ruleFileListName ) ) ( (otherlv_63= ',' )? ( (lv_files_64_0= ruleFileListName ) ) )* otherlv_65= ')' )
                    {
                    // InternalLinkerScript.g:677:3: ( () otherlv_60= 'GROUP' otherlv_61= '(' ( (lv_files_62_0= ruleFileListName ) ) ( (otherlv_63= ',' )? ( (lv_files_64_0= ruleFileListName ) ) )* otherlv_65= ')' )
                    // InternalLinkerScript.g:678:4: () otherlv_60= 'GROUP' otherlv_61= '(' ( (lv_files_62_0= ruleFileListName ) ) ( (otherlv_63= ',' )? ( (lv_files_64_0= ruleFileListName ) ) )* otherlv_65= ')'
                    {
                    // InternalLinkerScript.g:678:4: ()
                    // InternalLinkerScript.g:679:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getStatementCommandAccess().getStatementGroupAction_12_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_60=(Token)match(input,25,FOLLOW_5); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_60, grammarAccess.getStatementCommandAccess().getGROUPKeyword_12_1());
                      			
                    }
                    otherlv_61=(Token)match(input,13,FOLLOW_12); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_61, grammarAccess.getStatementCommandAccess().getLeftParenthesisKeyword_12_2());
                      			
                    }
                    // InternalLinkerScript.g:693:4: ( (lv_files_62_0= ruleFileListName ) )
                    // InternalLinkerScript.g:694:5: (lv_files_62_0= ruleFileListName )
                    {
                    // InternalLinkerScript.g:694:5: (lv_files_62_0= ruleFileListName )
                    // InternalLinkerScript.g:695:6: lv_files_62_0= ruleFileListName
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getStatementCommandAccess().getFilesFileListNameParserRuleCall_12_3_0());
                      					
                    }
                    pushFollow(FOLLOW_13);
                    lv_files_62_0=ruleFileListName();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getStatementCommandRule());
                      						}
                      						add(
                      							current,
                      							"files",
                      							lv_files_62_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.FileListName");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    // InternalLinkerScript.g:712:4: ( (otherlv_63= ',' )? ( (lv_files_64_0= ruleFileListName ) ) )*
                    loop6:
                    do {
                        int alt6=2;
                        int LA6_0 = input.LA(1);

                        if ( (LA6_0==RULE_ID||LA6_0==10||LA6_0==32||LA6_0==80||(LA6_0>=82 && LA6_0<=83)||(LA6_0>=85 && LA6_0<=86)) ) {
                            alt6=1;
                        }


                        switch (alt6) {
                    	case 1 :
                    	    // InternalLinkerScript.g:713:5: (otherlv_63= ',' )? ( (lv_files_64_0= ruleFileListName ) )
                    	    {
                    	    // InternalLinkerScript.g:713:5: (otherlv_63= ',' )?
                    	    int alt5=2;
                    	    int LA5_0 = input.LA(1);

                    	    if ( (LA5_0==10) ) {
                    	        alt5=1;
                    	    }
                    	    switch (alt5) {
                    	        case 1 :
                    	            // InternalLinkerScript.g:714:6: otherlv_63= ','
                    	            {
                    	            otherlv_63=(Token)match(input,10,FOLLOW_12); if (state.failed) return current;
                    	            if ( state.backtracking==0 ) {

                    	              						newLeafNode(otherlv_63, grammarAccess.getStatementCommandAccess().getCommaKeyword_12_4_0());
                    	              					
                    	            }

                    	            }
                    	            break;

                    	    }

                    	    // InternalLinkerScript.g:719:5: ( (lv_files_64_0= ruleFileListName ) )
                    	    // InternalLinkerScript.g:720:6: (lv_files_64_0= ruleFileListName )
                    	    {
                    	    // InternalLinkerScript.g:720:6: (lv_files_64_0= ruleFileListName )
                    	    // InternalLinkerScript.g:721:7: lv_files_64_0= ruleFileListName
                    	    {
                    	    if ( state.backtracking==0 ) {

                    	      							newCompositeNode(grammarAccess.getStatementCommandAccess().getFilesFileListNameParserRuleCall_12_4_1_0());
                    	      						
                    	    }
                    	    pushFollow(FOLLOW_13);
                    	    lv_files_64_0=ruleFileListName();

                    	    state._fsp--;
                    	    if (state.failed) return current;
                    	    if ( state.backtracking==0 ) {

                    	      							if (current==null) {
                    	      								current = createModelElementForParent(grammarAccess.getStatementCommandRule());
                    	      							}
                    	      							add(
                    	      								current,
                    	      								"files",
                    	      								lv_files_64_0,
                    	      								"org.eclipse.cdt.linkerscript.LinkerScript.FileListName");
                    	      							afterParserOrEnumRuleCall();
                    	      						
                    	    }

                    	    }


                    	    }


                    	    }
                    	    break;

                    	default :
                    	    break loop6;
                        }
                    } while (true);

                    otherlv_65=(Token)match(input,14,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_65, grammarAccess.getStatementCommandAccess().getRightParenthesisKeyword_12_5());
                      			
                    }

                    }


                    }
                    break;
                case 14 :
                    // InternalLinkerScript.g:745:3: ( () otherlv_67= 'MAP' otherlv_68= '(' ( (lv_name_69_0= ruleValidID ) ) otherlv_70= ')' )
                    {
                    // InternalLinkerScript.g:745:3: ( () otherlv_67= 'MAP' otherlv_68= '(' ( (lv_name_69_0= ruleValidID ) ) otherlv_70= ')' )
                    // InternalLinkerScript.g:746:4: () otherlv_67= 'MAP' otherlv_68= '(' ( (lv_name_69_0= ruleValidID ) ) otherlv_70= ')'
                    {
                    // InternalLinkerScript.g:746:4: ()
                    // InternalLinkerScript.g:747:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getStatementCommandAccess().getStatementMapAction_13_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_67=(Token)match(input,26,FOLLOW_5); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_67, grammarAccess.getStatementCommandAccess().getMAPKeyword_13_1());
                      			
                    }
                    otherlv_68=(Token)match(input,13,FOLLOW_6); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_68, grammarAccess.getStatementCommandAccess().getLeftParenthesisKeyword_13_2());
                      			
                    }
                    // InternalLinkerScript.g:761:4: ( (lv_name_69_0= ruleValidID ) )
                    // InternalLinkerScript.g:762:5: (lv_name_69_0= ruleValidID )
                    {
                    // InternalLinkerScript.g:762:5: (lv_name_69_0= ruleValidID )
                    // InternalLinkerScript.g:763:6: lv_name_69_0= ruleValidID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getStatementCommandAccess().getNameValidIDParserRuleCall_13_3_0());
                      					
                    }
                    pushFollow(FOLLOW_7);
                    lv_name_69_0=ruleValidID();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getStatementCommandRule());
                      						}
                      						set(
                      							current,
                      							"name",
                      							lv_name_69_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.ValidID");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    otherlv_70=(Token)match(input,14,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_70, grammarAccess.getStatementCommandAccess().getRightParenthesisKeyword_13_4());
                      			
                    }

                    }


                    }
                    break;
                case 15 :
                    // InternalLinkerScript.g:786:3: ( () otherlv_72= 'NOCROSSREFS' otherlv_73= '(' ( (lv_sections_74_0= ruleValidID ) )* otherlv_75= ')' )
                    {
                    // InternalLinkerScript.g:786:3: ( () otherlv_72= 'NOCROSSREFS' otherlv_73= '(' ( (lv_sections_74_0= ruleValidID ) )* otherlv_75= ')' )
                    // InternalLinkerScript.g:787:4: () otherlv_72= 'NOCROSSREFS' otherlv_73= '(' ( (lv_sections_74_0= ruleValidID ) )* otherlv_75= ')'
                    {
                    // InternalLinkerScript.g:787:4: ()
                    // InternalLinkerScript.g:788:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getStatementCommandAccess().getStatementNoCrossRefsAction_14_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_72=(Token)match(input,27,FOLLOW_5); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_72, grammarAccess.getStatementCommandAccess().getNOCROSSREFSKeyword_14_1());
                      			
                    }
                    otherlv_73=(Token)match(input,13,FOLLOW_14); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_73, grammarAccess.getStatementCommandAccess().getLeftParenthesisKeyword_14_2());
                      			
                    }
                    // InternalLinkerScript.g:802:4: ( (lv_sections_74_0= ruleValidID ) )*
                    loop7:
                    do {
                        int alt7=2;
                        int LA7_0 = input.LA(1);

                        if ( (LA7_0==RULE_ID||LA7_0==80||(LA7_0>=82 && LA7_0<=83)||(LA7_0>=85 && LA7_0<=86)) ) {
                            alt7=1;
                        }


                        switch (alt7) {
                    	case 1 :
                    	    // InternalLinkerScript.g:803:5: (lv_sections_74_0= ruleValidID )
                    	    {
                    	    // InternalLinkerScript.g:803:5: (lv_sections_74_0= ruleValidID )
                    	    // InternalLinkerScript.g:804:6: lv_sections_74_0= ruleValidID
                    	    {
                    	    if ( state.backtracking==0 ) {

                    	      						newCompositeNode(grammarAccess.getStatementCommandAccess().getSectionsValidIDParserRuleCall_14_3_0());
                    	      					
                    	    }
                    	    pushFollow(FOLLOW_14);
                    	    lv_sections_74_0=ruleValidID();

                    	    state._fsp--;
                    	    if (state.failed) return current;
                    	    if ( state.backtracking==0 ) {

                    	      						if (current==null) {
                    	      							current = createModelElementForParent(grammarAccess.getStatementCommandRule());
                    	      						}
                    	      						add(
                    	      							current,
                    	      							"sections",
                    	      							lv_sections_74_0,
                    	      							"org.eclipse.cdt.linkerscript.LinkerScript.ValidID");
                    	      						afterParserOrEnumRuleCall();
                    	      					
                    	    }

                    	    }


                    	    }
                    	    break;

                    	default :
                    	    break loop7;
                        }
                    } while (true);

                    otherlv_75=(Token)match(input,14,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_75, grammarAccess.getStatementCommandAccess().getRightParenthesisKeyword_14_4());
                      			
                    }

                    }


                    }
                    break;
                case 16 :
                    // InternalLinkerScript.g:827:3: ( () otherlv_77= 'NOCROSSREFS_TO' otherlv_78= '(' ( (lv_sections_79_0= ruleValidID ) )* otherlv_80= ')' )
                    {
                    // InternalLinkerScript.g:827:3: ( () otherlv_77= 'NOCROSSREFS_TO' otherlv_78= '(' ( (lv_sections_79_0= ruleValidID ) )* otherlv_80= ')' )
                    // InternalLinkerScript.g:828:4: () otherlv_77= 'NOCROSSREFS_TO' otherlv_78= '(' ( (lv_sections_79_0= ruleValidID ) )* otherlv_80= ')'
                    {
                    // InternalLinkerScript.g:828:4: ()
                    // InternalLinkerScript.g:829:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getStatementCommandAccess().getStatementNoCrossRefsToAction_15_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_77=(Token)match(input,28,FOLLOW_5); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_77, grammarAccess.getStatementCommandAccess().getNOCROSSREFS_TOKeyword_15_1());
                      			
                    }
                    otherlv_78=(Token)match(input,13,FOLLOW_14); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_78, grammarAccess.getStatementCommandAccess().getLeftParenthesisKeyword_15_2());
                      			
                    }
                    // InternalLinkerScript.g:843:4: ( (lv_sections_79_0= ruleValidID ) )*
                    loop8:
                    do {
                        int alt8=2;
                        int LA8_0 = input.LA(1);

                        if ( (LA8_0==RULE_ID||LA8_0==80||(LA8_0>=82 && LA8_0<=83)||(LA8_0>=85 && LA8_0<=86)) ) {
                            alt8=1;
                        }


                        switch (alt8) {
                    	case 1 :
                    	    // InternalLinkerScript.g:844:5: (lv_sections_79_0= ruleValidID )
                    	    {
                    	    // InternalLinkerScript.g:844:5: (lv_sections_79_0= ruleValidID )
                    	    // InternalLinkerScript.g:845:6: lv_sections_79_0= ruleValidID
                    	    {
                    	    if ( state.backtracking==0 ) {

                    	      						newCompositeNode(grammarAccess.getStatementCommandAccess().getSectionsValidIDParserRuleCall_15_3_0());
                    	      					
                    	    }
                    	    pushFollow(FOLLOW_14);
                    	    lv_sections_79_0=ruleValidID();

                    	    state._fsp--;
                    	    if (state.failed) return current;
                    	    if ( state.backtracking==0 ) {

                    	      						if (current==null) {
                    	      							current = createModelElementForParent(grammarAccess.getStatementCommandRule());
                    	      						}
                    	      						add(
                    	      							current,
                    	      							"sections",
                    	      							lv_sections_79_0,
                    	      							"org.eclipse.cdt.linkerscript.LinkerScript.ValidID");
                    	      						afterParserOrEnumRuleCall();
                    	      					
                    	    }

                    	    }


                    	    }
                    	    break;

                    	default :
                    	    break loop8;
                        }
                    } while (true);

                    otherlv_80=(Token)match(input,14,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_80, grammarAccess.getStatementCommandAccess().getRightParenthesisKeyword_15_4());
                      			
                    }

                    }


                    }
                    break;
                case 17 :
                    // InternalLinkerScript.g:868:3: ( () otherlv_82= 'EXTERN' otherlv_83= '(' ( (lv_sections_84_0= ruleValidID ) )* otherlv_85= ')' )
                    {
                    // InternalLinkerScript.g:868:3: ( () otherlv_82= 'EXTERN' otherlv_83= '(' ( (lv_sections_84_0= ruleValidID ) )* otherlv_85= ')' )
                    // InternalLinkerScript.g:869:4: () otherlv_82= 'EXTERN' otherlv_83= '(' ( (lv_sections_84_0= ruleValidID ) )* otherlv_85= ')'
                    {
                    // InternalLinkerScript.g:869:4: ()
                    // InternalLinkerScript.g:870:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getStatementCommandAccess().getStatementExternAction_16_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_82=(Token)match(input,29,FOLLOW_5); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_82, grammarAccess.getStatementCommandAccess().getEXTERNKeyword_16_1());
                      			
                    }
                    otherlv_83=(Token)match(input,13,FOLLOW_14); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_83, grammarAccess.getStatementCommandAccess().getLeftParenthesisKeyword_16_2());
                      			
                    }
                    // InternalLinkerScript.g:884:4: ( (lv_sections_84_0= ruleValidID ) )*
                    loop9:
                    do {
                        int alt9=2;
                        int LA9_0 = input.LA(1);

                        if ( (LA9_0==RULE_ID||LA9_0==80||(LA9_0>=82 && LA9_0<=83)||(LA9_0>=85 && LA9_0<=86)) ) {
                            alt9=1;
                        }


                        switch (alt9) {
                    	case 1 :
                    	    // InternalLinkerScript.g:885:5: (lv_sections_84_0= ruleValidID )
                    	    {
                    	    // InternalLinkerScript.g:885:5: (lv_sections_84_0= ruleValidID )
                    	    // InternalLinkerScript.g:886:6: lv_sections_84_0= ruleValidID
                    	    {
                    	    if ( state.backtracking==0 ) {

                    	      						newCompositeNode(grammarAccess.getStatementCommandAccess().getSectionsValidIDParserRuleCall_16_3_0());
                    	      					
                    	    }
                    	    pushFollow(FOLLOW_14);
                    	    lv_sections_84_0=ruleValidID();

                    	    state._fsp--;
                    	    if (state.failed) return current;
                    	    if ( state.backtracking==0 ) {

                    	      						if (current==null) {
                    	      							current = createModelElementForParent(grammarAccess.getStatementCommandRule());
                    	      						}
                    	      						add(
                    	      							current,
                    	      							"sections",
                    	      							lv_sections_84_0,
                    	      							"org.eclipse.cdt.linkerscript.LinkerScript.ValidID");
                    	      						afterParserOrEnumRuleCall();
                    	      					
                    	    }

                    	    }


                    	    }
                    	    break;

                    	default :
                    	    break loop9;
                        }
                    } while (true);

                    otherlv_85=(Token)match(input,14,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_85, grammarAccess.getStatementCommandAccess().getRightParenthesisKeyword_16_4());
                      			
                    }

                    }


                    }
                    break;
                case 18 :
                    // InternalLinkerScript.g:909:3: ( () otherlv_87= 'INCLUDE' ( (lv_name_88_0= ruleValidID ) ) )
                    {
                    // InternalLinkerScript.g:909:3: ( () otherlv_87= 'INCLUDE' ( (lv_name_88_0= ruleValidID ) ) )
                    // InternalLinkerScript.g:910:4: () otherlv_87= 'INCLUDE' ( (lv_name_88_0= ruleValidID ) )
                    {
                    // InternalLinkerScript.g:910:4: ()
                    // InternalLinkerScript.g:911:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getStatementCommandAccess().getStatementIncludeAction_17_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_87=(Token)match(input,30,FOLLOW_6); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_87, grammarAccess.getStatementCommandAccess().getINCLUDEKeyword_17_1());
                      			
                    }
                    // InternalLinkerScript.g:921:4: ( (lv_name_88_0= ruleValidID ) )
                    // InternalLinkerScript.g:922:5: (lv_name_88_0= ruleValidID )
                    {
                    // InternalLinkerScript.g:922:5: (lv_name_88_0= ruleValidID )
                    // InternalLinkerScript.g:923:6: lv_name_88_0= ruleValidID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getStatementCommandAccess().getNameValidIDParserRuleCall_17_2_0());
                      					
                    }
                    pushFollow(FOLLOW_2);
                    lv_name_88_0=ruleValidID();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getStatementCommandRule());
                      						}
                      						set(
                      							current,
                      							"name",
                      							lv_name_88_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.ValidID");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }


                    }


                    }
                    break;
                case 19 :
                    // InternalLinkerScript.g:942:3: ( () otherlv_90= ';' )
                    {
                    // InternalLinkerScript.g:942:3: ( () otherlv_90= ';' )
                    // InternalLinkerScript.g:943:4: () otherlv_90= ';'
                    {
                    // InternalLinkerScript.g:943:4: ()
                    // InternalLinkerScript.g:944:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getStatementCommandAccess().getStatementNopAction_18_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_90=(Token)match(input,11,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_90, grammarAccess.getStatementCommandAccess().getSemicolonKeyword_18_1());
                      			
                    }

                    }


                    }
                    break;

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleStatementCommand"


    // $ANTLR start "entryRuleFileList"
    // InternalLinkerScript.g:959:1: entryRuleFileList returns [EObject current=null] : iv_ruleFileList= ruleFileList EOF ;
    public final EObject entryRuleFileList() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleFileList = null;


        try {
            // InternalLinkerScript.g:959:49: (iv_ruleFileList= ruleFileList EOF )
            // InternalLinkerScript.g:960:2: iv_ruleFileList= ruleFileList EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getFileListRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleFileList=ruleFileList();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleFileList; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleFileList"


    // $ANTLR start "ruleFileList"
    // InternalLinkerScript.g:966:1: ruleFileList returns [EObject current=null] : ( ( ( (lv_files_0_0= ruleFileListName ) ) ( (otherlv_1= ',' )? ( (lv_files_2_0= ruleFileListName ) ) )* ) | ( ( (lv_asNeeded_3_0= 'AS_NEEDED' ) ) otherlv_4= '(' ( (lv_list_5_0= ruleFileList ) ) otherlv_6= ')' ) ) ;
    public final EObject ruleFileList() throws RecognitionException {
        EObject current = null;

        Token otherlv_1=null;
        Token lv_asNeeded_3_0=null;
        Token otherlv_4=null;
        Token otherlv_6=null;
        EObject lv_files_0_0 = null;

        EObject lv_files_2_0 = null;

        EObject lv_list_5_0 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:972:2: ( ( ( ( (lv_files_0_0= ruleFileListName ) ) ( (otherlv_1= ',' )? ( (lv_files_2_0= ruleFileListName ) ) )* ) | ( ( (lv_asNeeded_3_0= 'AS_NEEDED' ) ) otherlv_4= '(' ( (lv_list_5_0= ruleFileList ) ) otherlv_6= ')' ) ) )
            // InternalLinkerScript.g:973:2: ( ( ( (lv_files_0_0= ruleFileListName ) ) ( (otherlv_1= ',' )? ( (lv_files_2_0= ruleFileListName ) ) )* ) | ( ( (lv_asNeeded_3_0= 'AS_NEEDED' ) ) otherlv_4= '(' ( (lv_list_5_0= ruleFileList ) ) otherlv_6= ')' ) )
            {
            // InternalLinkerScript.g:973:2: ( ( ( (lv_files_0_0= ruleFileListName ) ) ( (otherlv_1= ',' )? ( (lv_files_2_0= ruleFileListName ) ) )* ) | ( ( (lv_asNeeded_3_0= 'AS_NEEDED' ) ) otherlv_4= '(' ( (lv_list_5_0= ruleFileList ) ) otherlv_6= ')' ) )
            int alt13=2;
            int LA13_0 = input.LA(1);

            if ( (LA13_0==RULE_ID||LA13_0==32||LA13_0==80||(LA13_0>=82 && LA13_0<=83)||(LA13_0>=85 && LA13_0<=86)) ) {
                alt13=1;
            }
            else if ( (LA13_0==31) ) {
                alt13=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 13, 0, input);

                throw nvae;
            }
            switch (alt13) {
                case 1 :
                    // InternalLinkerScript.g:974:3: ( ( (lv_files_0_0= ruleFileListName ) ) ( (otherlv_1= ',' )? ( (lv_files_2_0= ruleFileListName ) ) )* )
                    {
                    // InternalLinkerScript.g:974:3: ( ( (lv_files_0_0= ruleFileListName ) ) ( (otherlv_1= ',' )? ( (lv_files_2_0= ruleFileListName ) ) )* )
                    // InternalLinkerScript.g:975:4: ( (lv_files_0_0= ruleFileListName ) ) ( (otherlv_1= ',' )? ( (lv_files_2_0= ruleFileListName ) ) )*
                    {
                    // InternalLinkerScript.g:975:4: ( (lv_files_0_0= ruleFileListName ) )
                    // InternalLinkerScript.g:976:5: (lv_files_0_0= ruleFileListName )
                    {
                    // InternalLinkerScript.g:976:5: (lv_files_0_0= ruleFileListName )
                    // InternalLinkerScript.g:977:6: lv_files_0_0= ruleFileListName
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getFileListAccess().getFilesFileListNameParserRuleCall_0_0_0());
                      					
                    }
                    pushFollow(FOLLOW_15);
                    lv_files_0_0=ruleFileListName();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getFileListRule());
                      						}
                      						add(
                      							current,
                      							"files",
                      							lv_files_0_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.FileListName");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    // InternalLinkerScript.g:994:4: ( (otherlv_1= ',' )? ( (lv_files_2_0= ruleFileListName ) ) )*
                    loop12:
                    do {
                        int alt12=2;
                        int LA12_0 = input.LA(1);

                        if ( (LA12_0==RULE_ID||LA12_0==10||LA12_0==32||LA12_0==80||(LA12_0>=82 && LA12_0<=83)||(LA12_0>=85 && LA12_0<=86)) ) {
                            alt12=1;
                        }


                        switch (alt12) {
                    	case 1 :
                    	    // InternalLinkerScript.g:995:5: (otherlv_1= ',' )? ( (lv_files_2_0= ruleFileListName ) )
                    	    {
                    	    // InternalLinkerScript.g:995:5: (otherlv_1= ',' )?
                    	    int alt11=2;
                    	    int LA11_0 = input.LA(1);

                    	    if ( (LA11_0==10) ) {
                    	        alt11=1;
                    	    }
                    	    switch (alt11) {
                    	        case 1 :
                    	            // InternalLinkerScript.g:996:6: otherlv_1= ','
                    	            {
                    	            otherlv_1=(Token)match(input,10,FOLLOW_12); if (state.failed) return current;
                    	            if ( state.backtracking==0 ) {

                    	              						newLeafNode(otherlv_1, grammarAccess.getFileListAccess().getCommaKeyword_0_1_0());
                    	              					
                    	            }

                    	            }
                    	            break;

                    	    }

                    	    // InternalLinkerScript.g:1001:5: ( (lv_files_2_0= ruleFileListName ) )
                    	    // InternalLinkerScript.g:1002:6: (lv_files_2_0= ruleFileListName )
                    	    {
                    	    // InternalLinkerScript.g:1002:6: (lv_files_2_0= ruleFileListName )
                    	    // InternalLinkerScript.g:1003:7: lv_files_2_0= ruleFileListName
                    	    {
                    	    if ( state.backtracking==0 ) {

                    	      							newCompositeNode(grammarAccess.getFileListAccess().getFilesFileListNameParserRuleCall_0_1_1_0());
                    	      						
                    	    }
                    	    pushFollow(FOLLOW_15);
                    	    lv_files_2_0=ruleFileListName();

                    	    state._fsp--;
                    	    if (state.failed) return current;
                    	    if ( state.backtracking==0 ) {

                    	      							if (current==null) {
                    	      								current = createModelElementForParent(grammarAccess.getFileListRule());
                    	      							}
                    	      							add(
                    	      								current,
                    	      								"files",
                    	      								lv_files_2_0,
                    	      								"org.eclipse.cdt.linkerscript.LinkerScript.FileListName");
                    	      							afterParserOrEnumRuleCall();
                    	      						
                    	    }

                    	    }


                    	    }


                    	    }
                    	    break;

                    	default :
                    	    break loop12;
                        }
                    } while (true);


                    }


                    }
                    break;
                case 2 :
                    // InternalLinkerScript.g:1023:3: ( ( (lv_asNeeded_3_0= 'AS_NEEDED' ) ) otherlv_4= '(' ( (lv_list_5_0= ruleFileList ) ) otherlv_6= ')' )
                    {
                    // InternalLinkerScript.g:1023:3: ( ( (lv_asNeeded_3_0= 'AS_NEEDED' ) ) otherlv_4= '(' ( (lv_list_5_0= ruleFileList ) ) otherlv_6= ')' )
                    // InternalLinkerScript.g:1024:4: ( (lv_asNeeded_3_0= 'AS_NEEDED' ) ) otherlv_4= '(' ( (lv_list_5_0= ruleFileList ) ) otherlv_6= ')'
                    {
                    // InternalLinkerScript.g:1024:4: ( (lv_asNeeded_3_0= 'AS_NEEDED' ) )
                    // InternalLinkerScript.g:1025:5: (lv_asNeeded_3_0= 'AS_NEEDED' )
                    {
                    // InternalLinkerScript.g:1025:5: (lv_asNeeded_3_0= 'AS_NEEDED' )
                    // InternalLinkerScript.g:1026:6: lv_asNeeded_3_0= 'AS_NEEDED'
                    {
                    lv_asNeeded_3_0=(Token)match(input,31,FOLLOW_5); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						newLeafNode(lv_asNeeded_3_0, grammarAccess.getFileListAccess().getAsNeededAS_NEEDEDKeyword_1_0_0());
                      					
                    }
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElement(grammarAccess.getFileListRule());
                      						}
                      						setWithLastConsumed(current, "asNeeded", true, "AS_NEEDED");
                      					
                    }

                    }


                    }

                    otherlv_4=(Token)match(input,13,FOLLOW_11); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_4, grammarAccess.getFileListAccess().getLeftParenthesisKeyword_1_1());
                      			
                    }
                    // InternalLinkerScript.g:1042:4: ( (lv_list_5_0= ruleFileList ) )
                    // InternalLinkerScript.g:1043:5: (lv_list_5_0= ruleFileList )
                    {
                    // InternalLinkerScript.g:1043:5: (lv_list_5_0= ruleFileList )
                    // InternalLinkerScript.g:1044:6: lv_list_5_0= ruleFileList
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getFileListAccess().getListFileListParserRuleCall_1_2_0());
                      					
                    }
                    pushFollow(FOLLOW_7);
                    lv_list_5_0=ruleFileList();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getFileListRule());
                      						}
                      						set(
                      							current,
                      							"list",
                      							lv_list_5_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.FileList");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    otherlv_6=(Token)match(input,14,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_6, grammarAccess.getFileListAccess().getRightParenthesisKeyword_1_3());
                      			
                    }

                    }


                    }
                    break;

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleFileList"


    // $ANTLR start "entryRuleFileListName"
    // InternalLinkerScript.g:1070:1: entryRuleFileListName returns [EObject current=null] : iv_ruleFileListName= ruleFileListName EOF ;
    public final EObject entryRuleFileListName() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleFileListName = null;



        	HiddenTokens myHiddenTokenState = ((XtextTokenStream)input).setHiddenTokens();

        try {
            // InternalLinkerScript.g:1072:2: (iv_ruleFileListName= ruleFileListName EOF )
            // InternalLinkerScript.g:1073:2: iv_ruleFileListName= ruleFileListName EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getFileListNameRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleFileListName=ruleFileListName();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleFileListName; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {

            	myHiddenTokenState.restore();

        }
        return current;
    }
    // $ANTLR end "entryRuleFileListName"


    // $ANTLR start "ruleFileListName"
    // InternalLinkerScript.g:1082:1: ruleFileListName returns [EObject current=null] : ( ( (lv_library_0_0= '-l' ) )? ( (lv_name_1_0= ruleValidID ) ) ) ;
    public final EObject ruleFileListName() throws RecognitionException {
        EObject current = null;

        Token lv_library_0_0=null;
        AntlrDatatypeRuleToken lv_name_1_0 = null;



        	enterRule();
        	HiddenTokens myHiddenTokenState = ((XtextTokenStream)input).setHiddenTokens();

        try {
            // InternalLinkerScript.g:1089:2: ( ( ( (lv_library_0_0= '-l' ) )? ( (lv_name_1_0= ruleValidID ) ) ) )
            // InternalLinkerScript.g:1090:2: ( ( (lv_library_0_0= '-l' ) )? ( (lv_name_1_0= ruleValidID ) ) )
            {
            // InternalLinkerScript.g:1090:2: ( ( (lv_library_0_0= '-l' ) )? ( (lv_name_1_0= ruleValidID ) ) )
            // InternalLinkerScript.g:1091:3: ( (lv_library_0_0= '-l' ) )? ( (lv_name_1_0= ruleValidID ) )
            {
            // InternalLinkerScript.g:1091:3: ( (lv_library_0_0= '-l' ) )?
            int alt14=2;
            int LA14_0 = input.LA(1);

            if ( (LA14_0==32) ) {
                alt14=1;
            }
            switch (alt14) {
                case 1 :
                    // InternalLinkerScript.g:1092:4: (lv_library_0_0= '-l' )
                    {
                    // InternalLinkerScript.g:1092:4: (lv_library_0_0= '-l' )
                    // InternalLinkerScript.g:1093:5: lv_library_0_0= '-l'
                    {
                    lv_library_0_0=(Token)match(input,32,FOLLOW_6); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      					newLeafNode(lv_library_0_0, grammarAccess.getFileListNameAccess().getLibraryLKeyword_0_0());
                      				
                    }
                    if ( state.backtracking==0 ) {

                      					if (current==null) {
                      						current = createModelElement(grammarAccess.getFileListNameRule());
                      					}
                      					setWithLastConsumed(current, "library", true, "-l");
                      				
                    }

                    }


                    }
                    break;

            }

            // InternalLinkerScript.g:1105:3: ( (lv_name_1_0= ruleValidID ) )
            // InternalLinkerScript.g:1106:4: (lv_name_1_0= ruleValidID )
            {
            // InternalLinkerScript.g:1106:4: (lv_name_1_0= ruleValidID )
            // InternalLinkerScript.g:1107:5: lv_name_1_0= ruleValidID
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getFileListNameAccess().getNameValidIDParserRuleCall_1_0());
              				
            }
            pushFollow(FOLLOW_2);
            lv_name_1_0=ruleValidID();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getFileListNameRule());
              					}
              					set(
              						current,
              						"name",
              						lv_name_1_0,
              						"org.eclipse.cdt.linkerscript.LinkerScript.ValidID");
              					afterParserOrEnumRuleCall();
              				
            }

            }


            }


            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {

            	myHiddenTokenState.restore();

        }
        return current;
    }
    // $ANTLR end "ruleFileListName"


    // $ANTLR start "entryRulePhdrsCommand"
    // InternalLinkerScript.g:1131:1: entryRulePhdrsCommand returns [EObject current=null] : iv_rulePhdrsCommand= rulePhdrsCommand EOF ;
    public final EObject entryRulePhdrsCommand() throws RecognitionException {
        EObject current = null;

        EObject iv_rulePhdrsCommand = null;


        try {
            // InternalLinkerScript.g:1131:53: (iv_rulePhdrsCommand= rulePhdrsCommand EOF )
            // InternalLinkerScript.g:1132:2: iv_rulePhdrsCommand= rulePhdrsCommand EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getPhdrsCommandRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_rulePhdrsCommand=rulePhdrsCommand();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_rulePhdrsCommand; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRulePhdrsCommand"


    // $ANTLR start "rulePhdrsCommand"
    // InternalLinkerScript.g:1138:1: rulePhdrsCommand returns [EObject current=null] : ( () otherlv_1= 'PHDRS' otherlv_2= '{' ( (lv_phdrs_3_0= rulePhdr ) )* otherlv_4= '}' ) ;
    public final EObject rulePhdrsCommand() throws RecognitionException {
        EObject current = null;

        Token otherlv_1=null;
        Token otherlv_2=null;
        Token otherlv_4=null;
        EObject lv_phdrs_3_0 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:1144:2: ( ( () otherlv_1= 'PHDRS' otherlv_2= '{' ( (lv_phdrs_3_0= rulePhdr ) )* otherlv_4= '}' ) )
            // InternalLinkerScript.g:1145:2: ( () otherlv_1= 'PHDRS' otherlv_2= '{' ( (lv_phdrs_3_0= rulePhdr ) )* otherlv_4= '}' )
            {
            // InternalLinkerScript.g:1145:2: ( () otherlv_1= 'PHDRS' otherlv_2= '{' ( (lv_phdrs_3_0= rulePhdr ) )* otherlv_4= '}' )
            // InternalLinkerScript.g:1146:3: () otherlv_1= 'PHDRS' otherlv_2= '{' ( (lv_phdrs_3_0= rulePhdr ) )* otherlv_4= '}'
            {
            // InternalLinkerScript.g:1146:3: ()
            // InternalLinkerScript.g:1147:4: 
            {
            if ( state.backtracking==0 ) {

              				current = forceCreateModelElement(
              					grammarAccess.getPhdrsCommandAccess().getPhdrsCommandAction_0(),
              					current);
              			
            }

            }

            otherlv_1=(Token)match(input,33,FOLLOW_16); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_1, grammarAccess.getPhdrsCommandAccess().getPHDRSKeyword_1());
              		
            }
            otherlv_2=(Token)match(input,34,FOLLOW_17); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_2, grammarAccess.getPhdrsCommandAccess().getLeftCurlyBracketKeyword_2());
              		
            }
            // InternalLinkerScript.g:1161:3: ( (lv_phdrs_3_0= rulePhdr ) )*
            loop15:
            do {
                int alt15=2;
                int LA15_0 = input.LA(1);

                if ( (LA15_0==RULE_ID||LA15_0==80||(LA15_0>=82 && LA15_0<=83)||(LA15_0>=85 && LA15_0<=86)) ) {
                    alt15=1;
                }


                switch (alt15) {
            	case 1 :
            	    // InternalLinkerScript.g:1162:4: (lv_phdrs_3_0= rulePhdr )
            	    {
            	    // InternalLinkerScript.g:1162:4: (lv_phdrs_3_0= rulePhdr )
            	    // InternalLinkerScript.g:1163:5: lv_phdrs_3_0= rulePhdr
            	    {
            	    if ( state.backtracking==0 ) {

            	      					newCompositeNode(grammarAccess.getPhdrsCommandAccess().getPhdrsPhdrParserRuleCall_3_0());
            	      				
            	    }
            	    pushFollow(FOLLOW_17);
            	    lv_phdrs_3_0=rulePhdr();

            	    state._fsp--;
            	    if (state.failed) return current;
            	    if ( state.backtracking==0 ) {

            	      					if (current==null) {
            	      						current = createModelElementForParent(grammarAccess.getPhdrsCommandRule());
            	      					}
            	      					add(
            	      						current,
            	      						"phdrs",
            	      						lv_phdrs_3_0,
            	      						"org.eclipse.cdt.linkerscript.LinkerScript.Phdr");
            	      					afterParserOrEnumRuleCall();
            	      				
            	    }

            	    }


            	    }
            	    break;

            	default :
            	    break loop15;
                }
            } while (true);

            otherlv_4=(Token)match(input,35,FOLLOW_2); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_4, grammarAccess.getPhdrsCommandAccess().getRightCurlyBracketKeyword_4());
              		
            }

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "rulePhdrsCommand"


    // $ANTLR start "entryRulePhdr"
    // InternalLinkerScript.g:1188:1: entryRulePhdr returns [EObject current=null] : iv_rulePhdr= rulePhdr EOF ;
    public final EObject entryRulePhdr() throws RecognitionException {
        EObject current = null;

        EObject iv_rulePhdr = null;


        try {
            // InternalLinkerScript.g:1188:45: (iv_rulePhdr= rulePhdr EOF )
            // InternalLinkerScript.g:1189:2: iv_rulePhdr= rulePhdr EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getPhdrRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_rulePhdr=rulePhdr();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_rulePhdr; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRulePhdr"


    // $ANTLR start "rulePhdr"
    // InternalLinkerScript.g:1195:1: rulePhdr returns [EObject current=null] : ( ( (lv_name_0_0= ruleValidID ) ) ( (lv_opts_1_0= ruleLExpression ) )* otherlv_2= ';' ) ;
    public final EObject rulePhdr() throws RecognitionException {
        EObject current = null;

        Token otherlv_2=null;
        AntlrDatatypeRuleToken lv_name_0_0 = null;

        EObject lv_opts_1_0 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:1201:2: ( ( ( (lv_name_0_0= ruleValidID ) ) ( (lv_opts_1_0= ruleLExpression ) )* otherlv_2= ';' ) )
            // InternalLinkerScript.g:1202:2: ( ( (lv_name_0_0= ruleValidID ) ) ( (lv_opts_1_0= ruleLExpression ) )* otherlv_2= ';' )
            {
            // InternalLinkerScript.g:1202:2: ( ( (lv_name_0_0= ruleValidID ) ) ( (lv_opts_1_0= ruleLExpression ) )* otherlv_2= ';' )
            // InternalLinkerScript.g:1203:3: ( (lv_name_0_0= ruleValidID ) ) ( (lv_opts_1_0= ruleLExpression ) )* otherlv_2= ';'
            {
            // InternalLinkerScript.g:1203:3: ( (lv_name_0_0= ruleValidID ) )
            // InternalLinkerScript.g:1204:4: (lv_name_0_0= ruleValidID )
            {
            // InternalLinkerScript.g:1204:4: (lv_name_0_0= ruleValidID )
            // InternalLinkerScript.g:1205:5: lv_name_0_0= ruleValidID
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getPhdrAccess().getNameValidIDParserRuleCall_0_0());
              				
            }
            pushFollow(FOLLOW_18);
            lv_name_0_0=ruleValidID();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getPhdrRule());
              					}
              					set(
              						current,
              						"name",
              						lv_name_0_0,
              						"org.eclipse.cdt.linkerscript.LinkerScript.ValidID");
              					afterParserOrEnumRuleCall();
              				
            }

            }


            }

            // InternalLinkerScript.g:1222:3: ( (lv_opts_1_0= ruleLExpression ) )*
            loop16:
            do {
                int alt16=2;
                int LA16_0 = input.LA(1);

                if ( ((LA16_0>=RULE_ID && LA16_0<=RULE_HEX)||LA16_0==13||LA16_0==38||LA16_0==42||LA16_0==80||(LA16_0>=82 && LA16_0<=87)||(LA16_0>=94 && LA16_0<=95)||LA16_0==99||LA16_0==102) ) {
                    alt16=1;
                }


                switch (alt16) {
            	case 1 :
            	    // InternalLinkerScript.g:1223:4: (lv_opts_1_0= ruleLExpression )
            	    {
            	    // InternalLinkerScript.g:1223:4: (lv_opts_1_0= ruleLExpression )
            	    // InternalLinkerScript.g:1224:5: lv_opts_1_0= ruleLExpression
            	    {
            	    if ( state.backtracking==0 ) {

            	      					newCompositeNode(grammarAccess.getPhdrAccess().getOptsLExpressionParserRuleCall_1_0());
            	      				
            	    }
            	    pushFollow(FOLLOW_18);
            	    lv_opts_1_0=ruleLExpression();

            	    state._fsp--;
            	    if (state.failed) return current;
            	    if ( state.backtracking==0 ) {

            	      					if (current==null) {
            	      						current = createModelElementForParent(grammarAccess.getPhdrRule());
            	      					}
            	      					add(
            	      						current,
            	      						"opts",
            	      						lv_opts_1_0,
            	      						"org.eclipse.cdt.linkerscript.LinkerScript.LExpression");
            	      					afterParserOrEnumRuleCall();
            	      				
            	    }

            	    }


            	    }
            	    break;

            	default :
            	    break loop16;
                }
            } while (true);

            otherlv_2=(Token)match(input,11,FOLLOW_2); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_2, grammarAccess.getPhdrAccess().getSemicolonKeyword_2());
              		
            }

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "rulePhdr"


    // $ANTLR start "entryRuleSectionsCommand"
    // InternalLinkerScript.g:1249:1: entryRuleSectionsCommand returns [EObject current=null] : iv_ruleSectionsCommand= ruleSectionsCommand EOF ;
    public final EObject entryRuleSectionsCommand() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleSectionsCommand = null;


        try {
            // InternalLinkerScript.g:1249:56: (iv_ruleSectionsCommand= ruleSectionsCommand EOF )
            // InternalLinkerScript.g:1250:2: iv_ruleSectionsCommand= ruleSectionsCommand EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getSectionsCommandRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleSectionsCommand=ruleSectionsCommand();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleSectionsCommand; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleSectionsCommand"


    // $ANTLR start "ruleSectionsCommand"
    // InternalLinkerScript.g:1256:1: ruleSectionsCommand returns [EObject current=null] : (otherlv_0= 'SECTIONS' otherlv_1= '{' () ( (lv_sectionCommands_3_0= ruleOutputSectionCommand ) )* otherlv_4= '}' ) ;
    public final EObject ruleSectionsCommand() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token otherlv_1=null;
        Token otherlv_4=null;
        EObject lv_sectionCommands_3_0 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:1262:2: ( (otherlv_0= 'SECTIONS' otherlv_1= '{' () ( (lv_sectionCommands_3_0= ruleOutputSectionCommand ) )* otherlv_4= '}' ) )
            // InternalLinkerScript.g:1263:2: (otherlv_0= 'SECTIONS' otherlv_1= '{' () ( (lv_sectionCommands_3_0= ruleOutputSectionCommand ) )* otherlv_4= '}' )
            {
            // InternalLinkerScript.g:1263:2: (otherlv_0= 'SECTIONS' otherlv_1= '{' () ( (lv_sectionCommands_3_0= ruleOutputSectionCommand ) )* otherlv_4= '}' )
            // InternalLinkerScript.g:1264:3: otherlv_0= 'SECTIONS' otherlv_1= '{' () ( (lv_sectionCommands_3_0= ruleOutputSectionCommand ) )* otherlv_4= '}'
            {
            otherlv_0=(Token)match(input,36,FOLLOW_16); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_0, grammarAccess.getSectionsCommandAccess().getSECTIONSKeyword_0());
              		
            }
            otherlv_1=(Token)match(input,34,FOLLOW_19); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_1, grammarAccess.getSectionsCommandAccess().getLeftCurlyBracketKeyword_1());
              		
            }
            // InternalLinkerScript.g:1272:3: ()
            // InternalLinkerScript.g:1273:4: 
            {
            if ( state.backtracking==0 ) {

              				current = forceCreateModelElement(
              					grammarAccess.getSectionsCommandAccess().getSectionsCommandAction_2(),
              					current);
              			
            }

            }

            // InternalLinkerScript.g:1279:3: ( (lv_sectionCommands_3_0= ruleOutputSectionCommand ) )*
            loop17:
            do {
                int alt17=2;
                int LA17_0 = input.LA(1);

                if ( (LA17_0==RULE_ID||LA17_0==11||(LA17_0>=15 && LA17_0<=16)||(LA17_0>=61 && LA17_0<=63)||LA17_0==80||(LA17_0>=82 && LA17_0<=83)||(LA17_0>=85 && LA17_0<=86)||LA17_0==96) ) {
                    alt17=1;
                }


                switch (alt17) {
            	case 1 :
            	    // InternalLinkerScript.g:1280:4: (lv_sectionCommands_3_0= ruleOutputSectionCommand )
            	    {
            	    // InternalLinkerScript.g:1280:4: (lv_sectionCommands_3_0= ruleOutputSectionCommand )
            	    // InternalLinkerScript.g:1281:5: lv_sectionCommands_3_0= ruleOutputSectionCommand
            	    {
            	    if ( state.backtracking==0 ) {

            	      					newCompositeNode(grammarAccess.getSectionsCommandAccess().getSectionCommandsOutputSectionCommandParserRuleCall_3_0());
            	      				
            	    }
            	    pushFollow(FOLLOW_19);
            	    lv_sectionCommands_3_0=ruleOutputSectionCommand();

            	    state._fsp--;
            	    if (state.failed) return current;
            	    if ( state.backtracking==0 ) {

            	      					if (current==null) {
            	      						current = createModelElementForParent(grammarAccess.getSectionsCommandRule());
            	      					}
            	      					add(
            	      						current,
            	      						"sectionCommands",
            	      						lv_sectionCommands_3_0,
            	      						"org.eclipse.cdt.linkerscript.LinkerScript.OutputSectionCommand");
            	      					afterParserOrEnumRuleCall();
            	      				
            	    }

            	    }


            	    }
            	    break;

            	default :
            	    break loop17;
                }
            } while (true);

            otherlv_4=(Token)match(input,35,FOLLOW_2); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_4, grammarAccess.getSectionsCommandAccess().getRightCurlyBracketKeyword_4());
              		
            }

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleSectionsCommand"


    // $ANTLR start "entryRuleOutputSectionCommand"
    // InternalLinkerScript.g:1306:1: entryRuleOutputSectionCommand returns [EObject current=null] : iv_ruleOutputSectionCommand= ruleOutputSectionCommand EOF ;
    public final EObject entryRuleOutputSectionCommand() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleOutputSectionCommand = null;


        try {
            // InternalLinkerScript.g:1306:61: (iv_ruleOutputSectionCommand= ruleOutputSectionCommand EOF )
            // InternalLinkerScript.g:1307:2: iv_ruleOutputSectionCommand= ruleOutputSectionCommand EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getOutputSectionCommandRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleOutputSectionCommand=ruleOutputSectionCommand();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleOutputSectionCommand; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleOutputSectionCommand"


    // $ANTLR start "ruleOutputSectionCommand"
    // InternalLinkerScript.g:1313:1: ruleOutputSectionCommand returns [EObject current=null] : (this_OutputSection_0= ruleOutputSection | this_StatementAnywhere_1= ruleStatementAnywhere ) ;
    public final EObject ruleOutputSectionCommand() throws RecognitionException {
        EObject current = null;

        EObject this_OutputSection_0 = null;

        EObject this_StatementAnywhere_1 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:1319:2: ( (this_OutputSection_0= ruleOutputSection | this_StatementAnywhere_1= ruleStatementAnywhere ) )
            // InternalLinkerScript.g:1320:2: (this_OutputSection_0= ruleOutputSection | this_StatementAnywhere_1= ruleStatementAnywhere )
            {
            // InternalLinkerScript.g:1320:2: (this_OutputSection_0= ruleOutputSection | this_StatementAnywhere_1= ruleStatementAnywhere )
            int alt18=2;
            switch ( input.LA(1) ) {
            case RULE_ID:
                {
                int LA18_1 = input.LA(2);

                if ( ((LA18_1>=40 && LA18_1<=41)||(LA18_1>=64 && LA18_1<=68)||(LA18_1>=70 && LA18_1<=71)) ) {
                    alt18=2;
                }
                else if ( ((LA18_1>=RULE_ID && LA18_1<=RULE_HEX)||LA18_1==13||(LA18_1>=37 && LA18_1<=38)||LA18_1==42||LA18_1==80||(LA18_1>=82 && LA18_1<=87)||(LA18_1>=94 && LA18_1<=95)||LA18_1==99||LA18_1==102) ) {
                    alt18=1;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return current;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 18, 1, input);

                    throw nvae;
                }
                }
                break;
            case 80:
                {
                int LA18_2 = input.LA(2);

                if ( ((LA18_2>=RULE_ID && LA18_2<=RULE_HEX)||LA18_2==13||(LA18_2>=37 && LA18_2<=38)||LA18_2==42||LA18_2==80||(LA18_2>=82 && LA18_2<=87)||(LA18_2>=94 && LA18_2<=95)||LA18_2==99||LA18_2==102) ) {
                    alt18=1;
                }
                else if ( ((LA18_2>=40 && LA18_2<=41)||(LA18_2>=64 && LA18_2<=68)||(LA18_2>=70 && LA18_2<=71)) ) {
                    alt18=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return current;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 18, 2, input);

                    throw nvae;
                }
                }
                break;
            case 83:
                {
                int LA18_3 = input.LA(2);

                if ( ((LA18_3>=40 && LA18_3<=41)||(LA18_3>=64 && LA18_3<=68)||(LA18_3>=70 && LA18_3<=71)) ) {
                    alt18=2;
                }
                else if ( ((LA18_3>=RULE_ID && LA18_3<=RULE_HEX)||LA18_3==13||(LA18_3>=37 && LA18_3<=38)||LA18_3==42||LA18_3==80||(LA18_3>=82 && LA18_3<=87)||(LA18_3>=94 && LA18_3<=95)||LA18_3==99||LA18_3==102) ) {
                    alt18=1;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return current;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 18, 3, input);

                    throw nvae;
                }
                }
                break;
            case 82:
                {
                int LA18_4 = input.LA(2);

                if ( ((LA18_4>=40 && LA18_4<=41)||(LA18_4>=64 && LA18_4<=68)||(LA18_4>=70 && LA18_4<=71)) ) {
                    alt18=2;
                }
                else if ( ((LA18_4>=RULE_ID && LA18_4<=RULE_HEX)||LA18_4==13||(LA18_4>=37 && LA18_4<=38)||LA18_4==42||LA18_4==80||(LA18_4>=82 && LA18_4<=87)||(LA18_4>=94 && LA18_4<=95)||LA18_4==99||LA18_4==102) ) {
                    alt18=1;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return current;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 18, 4, input);

                    throw nvae;
                }
                }
                break;
            case 86:
                {
                int LA18_5 = input.LA(2);

                if ( ((LA18_5>=RULE_ID && LA18_5<=RULE_HEX)||LA18_5==13||(LA18_5>=37 && LA18_5<=38)||LA18_5==42||LA18_5==80||(LA18_5>=82 && LA18_5<=87)||(LA18_5>=94 && LA18_5<=95)||LA18_5==99||LA18_5==102) ) {
                    alt18=1;
                }
                else if ( ((LA18_5>=40 && LA18_5<=41)||(LA18_5>=64 && LA18_5<=68)||(LA18_5>=70 && LA18_5<=71)) ) {
                    alt18=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return current;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 18, 5, input);

                    throw nvae;
                }
                }
                break;
            case 85:
                {
                int LA18_6 = input.LA(2);

                if ( ((LA18_6>=RULE_ID && LA18_6<=RULE_HEX)||LA18_6==13||(LA18_6>=37 && LA18_6<=38)||LA18_6==42||LA18_6==80||(LA18_6>=82 && LA18_6<=87)||(LA18_6>=94 && LA18_6<=95)||LA18_6==99||LA18_6==102) ) {
                    alt18=1;
                }
                else if ( ((LA18_6>=40 && LA18_6<=41)||(LA18_6>=64 && LA18_6<=68)||(LA18_6>=70 && LA18_6<=71)) ) {
                    alt18=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return current;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 18, 6, input);

                    throw nvae;
                }
                }
                break;
            case 11:
            case 15:
            case 16:
            case 61:
            case 62:
            case 63:
            case 96:
                {
                alt18=2;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 18, 0, input);

                throw nvae;
            }

            switch (alt18) {
                case 1 :
                    // InternalLinkerScript.g:1321:3: this_OutputSection_0= ruleOutputSection
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getOutputSectionCommandAccess().getOutputSectionParserRuleCall_0());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_OutputSection_0=ruleOutputSection();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_OutputSection_0;
                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 2 :
                    // InternalLinkerScript.g:1330:3: this_StatementAnywhere_1= ruleStatementAnywhere
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getOutputSectionCommandAccess().getStatementAnywhereParserRuleCall_1());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_StatementAnywhere_1=ruleStatementAnywhere();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_StatementAnywhere_1;
                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleOutputSectionCommand"


    // $ANTLR start "entryRuleOutputSection"
    // InternalLinkerScript.g:1342:1: entryRuleOutputSection returns [EObject current=null] : iv_ruleOutputSection= ruleOutputSection EOF ;
    public final EObject entryRuleOutputSection() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleOutputSection = null;


        try {
            // InternalLinkerScript.g:1342:54: (iv_ruleOutputSection= ruleOutputSection EOF )
            // InternalLinkerScript.g:1343:2: iv_ruleOutputSection= ruleOutputSection EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getOutputSectionRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleOutputSection=ruleOutputSection();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleOutputSection; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleOutputSection"


    // $ANTLR start "ruleOutputSection"
    // InternalLinkerScript.g:1349:1: ruleOutputSection returns [EObject current=null] : ( ( (lv_name_0_0= ruleValidID ) ) ( (lv_address_1_0= ruleLExpression ) )? (otherlv_2= '(' ( (lv_type_3_0= ruleOutputSectionType ) ) otherlv_4= ')' )? otherlv_5= ':' (otherlv_6= 'AT' otherlv_7= '(' ( (lv_at_8_0= ruleLExpression ) ) otherlv_9= ')' )? ( (lv_align_10_0= ruleOutputSectionAlign ) )? (otherlv_11= 'SUBALIGN' otherlv_12= '(' ( (lv_subAlign_13_0= ruleLExpression ) ) otherlv_14= ')' )? ( (lv_constraint_15_0= ruleOutputSectionConstraint ) )? otherlv_16= '{' ( (lv_statements_17_0= ruleStatement ) )* otherlv_18= '}' (otherlv_19= '>' ( (lv_memory_20_0= ruleValidID ) ) )? (otherlv_21= 'AT' otherlv_22= '>' ( (lv_atMemory_23_0= ruleValidID ) ) )? (otherlv_24= ':' ( (lv_phdrs_25_0= ruleValidID ) ) )* (otherlv_26= '=' ( (lv_fill_27_0= ruleLExpression ) ) )? (otherlv_28= ',' )? ) ;
    public final EObject ruleOutputSection() throws RecognitionException {
        EObject current = null;

        Token otherlv_2=null;
        Token otherlv_4=null;
        Token otherlv_5=null;
        Token otherlv_6=null;
        Token otherlv_7=null;
        Token otherlv_9=null;
        Token otherlv_11=null;
        Token otherlv_12=null;
        Token otherlv_14=null;
        Token otherlv_16=null;
        Token otherlv_18=null;
        Token otherlv_19=null;
        Token otherlv_21=null;
        Token otherlv_22=null;
        Token otherlv_24=null;
        Token otherlv_26=null;
        Token otherlv_28=null;
        AntlrDatatypeRuleToken lv_name_0_0 = null;

        EObject lv_address_1_0 = null;

        EObject lv_type_3_0 = null;

        EObject lv_at_8_0 = null;

        EObject lv_align_10_0 = null;

        EObject lv_subAlign_13_0 = null;

        EObject lv_constraint_15_0 = null;

        EObject lv_statements_17_0 = null;

        AntlrDatatypeRuleToken lv_memory_20_0 = null;

        AntlrDatatypeRuleToken lv_atMemory_23_0 = null;

        AntlrDatatypeRuleToken lv_phdrs_25_0 = null;

        EObject lv_fill_27_0 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:1355:2: ( ( ( (lv_name_0_0= ruleValidID ) ) ( (lv_address_1_0= ruleLExpression ) )? (otherlv_2= '(' ( (lv_type_3_0= ruleOutputSectionType ) ) otherlv_4= ')' )? otherlv_5= ':' (otherlv_6= 'AT' otherlv_7= '(' ( (lv_at_8_0= ruleLExpression ) ) otherlv_9= ')' )? ( (lv_align_10_0= ruleOutputSectionAlign ) )? (otherlv_11= 'SUBALIGN' otherlv_12= '(' ( (lv_subAlign_13_0= ruleLExpression ) ) otherlv_14= ')' )? ( (lv_constraint_15_0= ruleOutputSectionConstraint ) )? otherlv_16= '{' ( (lv_statements_17_0= ruleStatement ) )* otherlv_18= '}' (otherlv_19= '>' ( (lv_memory_20_0= ruleValidID ) ) )? (otherlv_21= 'AT' otherlv_22= '>' ( (lv_atMemory_23_0= ruleValidID ) ) )? (otherlv_24= ':' ( (lv_phdrs_25_0= ruleValidID ) ) )* (otherlv_26= '=' ( (lv_fill_27_0= ruleLExpression ) ) )? (otherlv_28= ',' )? ) )
            // InternalLinkerScript.g:1356:2: ( ( (lv_name_0_0= ruleValidID ) ) ( (lv_address_1_0= ruleLExpression ) )? (otherlv_2= '(' ( (lv_type_3_0= ruleOutputSectionType ) ) otherlv_4= ')' )? otherlv_5= ':' (otherlv_6= 'AT' otherlv_7= '(' ( (lv_at_8_0= ruleLExpression ) ) otherlv_9= ')' )? ( (lv_align_10_0= ruleOutputSectionAlign ) )? (otherlv_11= 'SUBALIGN' otherlv_12= '(' ( (lv_subAlign_13_0= ruleLExpression ) ) otherlv_14= ')' )? ( (lv_constraint_15_0= ruleOutputSectionConstraint ) )? otherlv_16= '{' ( (lv_statements_17_0= ruleStatement ) )* otherlv_18= '}' (otherlv_19= '>' ( (lv_memory_20_0= ruleValidID ) ) )? (otherlv_21= 'AT' otherlv_22= '>' ( (lv_atMemory_23_0= ruleValidID ) ) )? (otherlv_24= ':' ( (lv_phdrs_25_0= ruleValidID ) ) )* (otherlv_26= '=' ( (lv_fill_27_0= ruleLExpression ) ) )? (otherlv_28= ',' )? )
            {
            // InternalLinkerScript.g:1356:2: ( ( (lv_name_0_0= ruleValidID ) ) ( (lv_address_1_0= ruleLExpression ) )? (otherlv_2= '(' ( (lv_type_3_0= ruleOutputSectionType ) ) otherlv_4= ')' )? otherlv_5= ':' (otherlv_6= 'AT' otherlv_7= '(' ( (lv_at_8_0= ruleLExpression ) ) otherlv_9= ')' )? ( (lv_align_10_0= ruleOutputSectionAlign ) )? (otherlv_11= 'SUBALIGN' otherlv_12= '(' ( (lv_subAlign_13_0= ruleLExpression ) ) otherlv_14= ')' )? ( (lv_constraint_15_0= ruleOutputSectionConstraint ) )? otherlv_16= '{' ( (lv_statements_17_0= ruleStatement ) )* otherlv_18= '}' (otherlv_19= '>' ( (lv_memory_20_0= ruleValidID ) ) )? (otherlv_21= 'AT' otherlv_22= '>' ( (lv_atMemory_23_0= ruleValidID ) ) )? (otherlv_24= ':' ( (lv_phdrs_25_0= ruleValidID ) ) )* (otherlv_26= '=' ( (lv_fill_27_0= ruleLExpression ) ) )? (otherlv_28= ',' )? )
            // InternalLinkerScript.g:1357:3: ( (lv_name_0_0= ruleValidID ) ) ( (lv_address_1_0= ruleLExpression ) )? (otherlv_2= '(' ( (lv_type_3_0= ruleOutputSectionType ) ) otherlv_4= ')' )? otherlv_5= ':' (otherlv_6= 'AT' otherlv_7= '(' ( (lv_at_8_0= ruleLExpression ) ) otherlv_9= ')' )? ( (lv_align_10_0= ruleOutputSectionAlign ) )? (otherlv_11= 'SUBALIGN' otherlv_12= '(' ( (lv_subAlign_13_0= ruleLExpression ) ) otherlv_14= ')' )? ( (lv_constraint_15_0= ruleOutputSectionConstraint ) )? otherlv_16= '{' ( (lv_statements_17_0= ruleStatement ) )* otherlv_18= '}' (otherlv_19= '>' ( (lv_memory_20_0= ruleValidID ) ) )? (otherlv_21= 'AT' otherlv_22= '>' ( (lv_atMemory_23_0= ruleValidID ) ) )? (otherlv_24= ':' ( (lv_phdrs_25_0= ruleValidID ) ) )* (otherlv_26= '=' ( (lv_fill_27_0= ruleLExpression ) ) )? (otherlv_28= ',' )?
            {
            // InternalLinkerScript.g:1357:3: ( (lv_name_0_0= ruleValidID ) )
            // InternalLinkerScript.g:1358:4: (lv_name_0_0= ruleValidID )
            {
            // InternalLinkerScript.g:1358:4: (lv_name_0_0= ruleValidID )
            // InternalLinkerScript.g:1359:5: lv_name_0_0= ruleValidID
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getOutputSectionAccess().getNameValidIDParserRuleCall_0_0());
              				
            }
            pushFollow(FOLLOW_20);
            lv_name_0_0=ruleValidID();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getOutputSectionRule());
              					}
              					set(
              						current,
              						"name",
              						lv_name_0_0,
              						"org.eclipse.cdt.linkerscript.LinkerScript.ValidID");
              					afterParserOrEnumRuleCall();
              				
            }

            }


            }

            // InternalLinkerScript.g:1376:3: ( (lv_address_1_0= ruleLExpression ) )?
            int alt19=2;
            int LA19_0 = input.LA(1);

            if ( ((LA19_0>=RULE_ID && LA19_0<=RULE_HEX)||LA19_0==38||LA19_0==42||LA19_0==80||(LA19_0>=82 && LA19_0<=87)||(LA19_0>=94 && LA19_0<=95)||LA19_0==99||LA19_0==102) ) {
                alt19=1;
            }
            else if ( (LA19_0==13) ) {
                int LA19_2 = input.LA(2);

                if ( ((LA19_2>=RULE_ID && LA19_2<=RULE_HEX)||LA19_2==13||LA19_2==38||LA19_2==42||LA19_2==80||(LA19_2>=82 && LA19_2<=87)||(LA19_2>=94 && LA19_2<=95)||LA19_2==99||LA19_2==102) ) {
                    alt19=1;
                }
            }
            switch (alt19) {
                case 1 :
                    // InternalLinkerScript.g:1377:4: (lv_address_1_0= ruleLExpression )
                    {
                    // InternalLinkerScript.g:1377:4: (lv_address_1_0= ruleLExpression )
                    // InternalLinkerScript.g:1378:5: lv_address_1_0= ruleLExpression
                    {
                    if ( state.backtracking==0 ) {

                      					newCompositeNode(grammarAccess.getOutputSectionAccess().getAddressLExpressionParserRuleCall_1_0());
                      				
                    }
                    pushFollow(FOLLOW_21);
                    lv_address_1_0=ruleLExpression();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      					if (current==null) {
                      						current = createModelElementForParent(grammarAccess.getOutputSectionRule());
                      					}
                      					set(
                      						current,
                      						"address",
                      						lv_address_1_0,
                      						"org.eclipse.cdt.linkerscript.LinkerScript.LExpression");
                      					afterParserOrEnumRuleCall();
                      				
                    }

                    }


                    }
                    break;

            }

            // InternalLinkerScript.g:1395:3: (otherlv_2= '(' ( (lv_type_3_0= ruleOutputSectionType ) ) otherlv_4= ')' )?
            int alt20=2;
            int LA20_0 = input.LA(1);

            if ( (LA20_0==13) ) {
                alt20=1;
            }
            switch (alt20) {
                case 1 :
                    // InternalLinkerScript.g:1396:4: otherlv_2= '(' ( (lv_type_3_0= ruleOutputSectionType ) ) otherlv_4= ')'
                    {
                    otherlv_2=(Token)match(input,13,FOLLOW_22); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_2, grammarAccess.getOutputSectionAccess().getLeftParenthesisKeyword_2_0());
                      			
                    }
                    // InternalLinkerScript.g:1400:4: ( (lv_type_3_0= ruleOutputSectionType ) )
                    // InternalLinkerScript.g:1401:5: (lv_type_3_0= ruleOutputSectionType )
                    {
                    // InternalLinkerScript.g:1401:5: (lv_type_3_0= ruleOutputSectionType )
                    // InternalLinkerScript.g:1402:6: lv_type_3_0= ruleOutputSectionType
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getOutputSectionAccess().getTypeOutputSectionTypeParserRuleCall_2_1_0());
                      					
                    }
                    pushFollow(FOLLOW_7);
                    lv_type_3_0=ruleOutputSectionType();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getOutputSectionRule());
                      						}
                      						set(
                      							current,
                      							"type",
                      							lv_type_3_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.OutputSectionType");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    otherlv_4=(Token)match(input,14,FOLLOW_23); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_4, grammarAccess.getOutputSectionAccess().getRightParenthesisKeyword_2_2());
                      			
                    }

                    }
                    break;

            }

            otherlv_5=(Token)match(input,37,FOLLOW_24); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_5, grammarAccess.getOutputSectionAccess().getColonKeyword_3());
              		
            }
            // InternalLinkerScript.g:1428:3: (otherlv_6= 'AT' otherlv_7= '(' ( (lv_at_8_0= ruleLExpression ) ) otherlv_9= ')' )?
            int alt21=2;
            int LA21_0 = input.LA(1);

            if ( (LA21_0==38) ) {
                alt21=1;
            }
            switch (alt21) {
                case 1 :
                    // InternalLinkerScript.g:1429:4: otherlv_6= 'AT' otherlv_7= '(' ( (lv_at_8_0= ruleLExpression ) ) otherlv_9= ')'
                    {
                    otherlv_6=(Token)match(input,38,FOLLOW_5); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_6, grammarAccess.getOutputSectionAccess().getATKeyword_4_0());
                      			
                    }
                    otherlv_7=(Token)match(input,13,FOLLOW_8); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_7, grammarAccess.getOutputSectionAccess().getLeftParenthesisKeyword_4_1());
                      			
                    }
                    // InternalLinkerScript.g:1437:4: ( (lv_at_8_0= ruleLExpression ) )
                    // InternalLinkerScript.g:1438:5: (lv_at_8_0= ruleLExpression )
                    {
                    // InternalLinkerScript.g:1438:5: (lv_at_8_0= ruleLExpression )
                    // InternalLinkerScript.g:1439:6: lv_at_8_0= ruleLExpression
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getOutputSectionAccess().getAtLExpressionParserRuleCall_4_2_0());
                      					
                    }
                    pushFollow(FOLLOW_7);
                    lv_at_8_0=ruleLExpression();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getOutputSectionRule());
                      						}
                      						set(
                      							current,
                      							"at",
                      							lv_at_8_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.LExpression");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    otherlv_9=(Token)match(input,14,FOLLOW_25); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_9, grammarAccess.getOutputSectionAccess().getRightParenthesisKeyword_4_3());
                      			
                    }

                    }
                    break;

            }

            // InternalLinkerScript.g:1461:3: ( (lv_align_10_0= ruleOutputSectionAlign ) )?
            int alt22=2;
            int LA22_0 = input.LA(1);

            if ( ((LA22_0>=42 && LA22_0<=43)) ) {
                alt22=1;
            }
            switch (alt22) {
                case 1 :
                    // InternalLinkerScript.g:1462:4: (lv_align_10_0= ruleOutputSectionAlign )
                    {
                    // InternalLinkerScript.g:1462:4: (lv_align_10_0= ruleOutputSectionAlign )
                    // InternalLinkerScript.g:1463:5: lv_align_10_0= ruleOutputSectionAlign
                    {
                    if ( state.backtracking==0 ) {

                      					newCompositeNode(grammarAccess.getOutputSectionAccess().getAlignOutputSectionAlignParserRuleCall_5_0());
                      				
                    }
                    pushFollow(FOLLOW_26);
                    lv_align_10_0=ruleOutputSectionAlign();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      					if (current==null) {
                      						current = createModelElementForParent(grammarAccess.getOutputSectionRule());
                      					}
                      					set(
                      						current,
                      						"align",
                      						lv_align_10_0,
                      						"org.eclipse.cdt.linkerscript.LinkerScript.OutputSectionAlign");
                      					afterParserOrEnumRuleCall();
                      				
                    }

                    }


                    }
                    break;

            }

            // InternalLinkerScript.g:1480:3: (otherlv_11= 'SUBALIGN' otherlv_12= '(' ( (lv_subAlign_13_0= ruleLExpression ) ) otherlv_14= ')' )?
            int alt23=2;
            int LA23_0 = input.LA(1);

            if ( (LA23_0==39) ) {
                alt23=1;
            }
            switch (alt23) {
                case 1 :
                    // InternalLinkerScript.g:1481:4: otherlv_11= 'SUBALIGN' otherlv_12= '(' ( (lv_subAlign_13_0= ruleLExpression ) ) otherlv_14= ')'
                    {
                    otherlv_11=(Token)match(input,39,FOLLOW_5); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_11, grammarAccess.getOutputSectionAccess().getSUBALIGNKeyword_6_0());
                      			
                    }
                    otherlv_12=(Token)match(input,13,FOLLOW_8); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_12, grammarAccess.getOutputSectionAccess().getLeftParenthesisKeyword_6_1());
                      			
                    }
                    // InternalLinkerScript.g:1489:4: ( (lv_subAlign_13_0= ruleLExpression ) )
                    // InternalLinkerScript.g:1490:5: (lv_subAlign_13_0= ruleLExpression )
                    {
                    // InternalLinkerScript.g:1490:5: (lv_subAlign_13_0= ruleLExpression )
                    // InternalLinkerScript.g:1491:6: lv_subAlign_13_0= ruleLExpression
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getOutputSectionAccess().getSubAlignLExpressionParserRuleCall_6_2_0());
                      					
                    }
                    pushFollow(FOLLOW_7);
                    lv_subAlign_13_0=ruleLExpression();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getOutputSectionRule());
                      						}
                      						set(
                      							current,
                      							"subAlign",
                      							lv_subAlign_13_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.LExpression");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    otherlv_14=(Token)match(input,14,FOLLOW_27); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_14, grammarAccess.getOutputSectionAccess().getRightParenthesisKeyword_6_3());
                      			
                    }

                    }
                    break;

            }

            // InternalLinkerScript.g:1513:3: ( (lv_constraint_15_0= ruleOutputSectionConstraint ) )?
            int alt24=2;
            int LA24_0 = input.LA(1);

            if ( ((LA24_0>=44 && LA24_0<=46)) ) {
                alt24=1;
            }
            switch (alt24) {
                case 1 :
                    // InternalLinkerScript.g:1514:4: (lv_constraint_15_0= ruleOutputSectionConstraint )
                    {
                    // InternalLinkerScript.g:1514:4: (lv_constraint_15_0= ruleOutputSectionConstraint )
                    // InternalLinkerScript.g:1515:5: lv_constraint_15_0= ruleOutputSectionConstraint
                    {
                    if ( state.backtracking==0 ) {

                      					newCompositeNode(grammarAccess.getOutputSectionAccess().getConstraintOutputSectionConstraintParserRuleCall_7_0());
                      				
                    }
                    pushFollow(FOLLOW_16);
                    lv_constraint_15_0=ruleOutputSectionConstraint();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      					if (current==null) {
                      						current = createModelElementForParent(grammarAccess.getOutputSectionRule());
                      					}
                      					set(
                      						current,
                      						"constraint",
                      						lv_constraint_15_0,
                      						"org.eclipse.cdt.linkerscript.LinkerScript.OutputSectionConstraint");
                      					afterParserOrEnumRuleCall();
                      				
                    }

                    }


                    }
                    break;

            }

            otherlv_16=(Token)match(input,34,FOLLOW_28); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_16, grammarAccess.getOutputSectionAccess().getLeftCurlyBracketKeyword_8());
              		
            }
            // InternalLinkerScript.g:1536:3: ( (lv_statements_17_0= ruleStatement ) )*
            loop25:
            do {
                int alt25=2;
                int LA25_0 = input.LA(1);

                if ( (LA25_0==RULE_ID||LA25_0==11||LA25_0==16||LA25_0==30||(LA25_0>=52 && LA25_0<=63)||LA25_0==72||(LA25_0>=74 && LA25_0<=80)||(LA25_0>=82 && LA25_0<=83)||(LA25_0>=85 && LA25_0<=86)||LA25_0==96) ) {
                    alt25=1;
                }


                switch (alt25) {
            	case 1 :
            	    // InternalLinkerScript.g:1537:4: (lv_statements_17_0= ruleStatement )
            	    {
            	    // InternalLinkerScript.g:1537:4: (lv_statements_17_0= ruleStatement )
            	    // InternalLinkerScript.g:1538:5: lv_statements_17_0= ruleStatement
            	    {
            	    if ( state.backtracking==0 ) {

            	      					newCompositeNode(grammarAccess.getOutputSectionAccess().getStatementsStatementParserRuleCall_9_0());
            	      				
            	    }
            	    pushFollow(FOLLOW_28);
            	    lv_statements_17_0=ruleStatement();

            	    state._fsp--;
            	    if (state.failed) return current;
            	    if ( state.backtracking==0 ) {

            	      					if (current==null) {
            	      						current = createModelElementForParent(grammarAccess.getOutputSectionRule());
            	      					}
            	      					add(
            	      						current,
            	      						"statements",
            	      						lv_statements_17_0,
            	      						"org.eclipse.cdt.linkerscript.LinkerScript.Statement");
            	      					afterParserOrEnumRuleCall();
            	      				
            	    }

            	    }


            	    }
            	    break;

            	default :
            	    break loop25;
                }
            } while (true);

            otherlv_18=(Token)match(input,35,FOLLOW_29); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_18, grammarAccess.getOutputSectionAccess().getRightCurlyBracketKeyword_10());
              		
            }
            // InternalLinkerScript.g:1559:3: (otherlv_19= '>' ( (lv_memory_20_0= ruleValidID ) ) )?
            int alt26=2;
            int LA26_0 = input.LA(1);

            if ( (LA26_0==40) ) {
                alt26=1;
            }
            switch (alt26) {
                case 1 :
                    // InternalLinkerScript.g:1560:4: otherlv_19= '>' ( (lv_memory_20_0= ruleValidID ) )
                    {
                    otherlv_19=(Token)match(input,40,FOLLOW_6); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_19, grammarAccess.getOutputSectionAccess().getGreaterThanSignKeyword_11_0());
                      			
                    }
                    // InternalLinkerScript.g:1564:4: ( (lv_memory_20_0= ruleValidID ) )
                    // InternalLinkerScript.g:1565:5: (lv_memory_20_0= ruleValidID )
                    {
                    // InternalLinkerScript.g:1565:5: (lv_memory_20_0= ruleValidID )
                    // InternalLinkerScript.g:1566:6: lv_memory_20_0= ruleValidID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getOutputSectionAccess().getMemoryValidIDParserRuleCall_11_1_0());
                      					
                    }
                    pushFollow(FOLLOW_30);
                    lv_memory_20_0=ruleValidID();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getOutputSectionRule());
                      						}
                      						set(
                      							current,
                      							"memory",
                      							lv_memory_20_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.ValidID");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }


                    }
                    break;

            }

            // InternalLinkerScript.g:1584:3: (otherlv_21= 'AT' otherlv_22= '>' ( (lv_atMemory_23_0= ruleValidID ) ) )?
            int alt27=2;
            int LA27_0 = input.LA(1);

            if ( (LA27_0==38) ) {
                alt27=1;
            }
            switch (alt27) {
                case 1 :
                    // InternalLinkerScript.g:1585:4: otherlv_21= 'AT' otherlv_22= '>' ( (lv_atMemory_23_0= ruleValidID ) )
                    {
                    otherlv_21=(Token)match(input,38,FOLLOW_31); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_21, grammarAccess.getOutputSectionAccess().getATKeyword_12_0());
                      			
                    }
                    otherlv_22=(Token)match(input,40,FOLLOW_6); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_22, grammarAccess.getOutputSectionAccess().getGreaterThanSignKeyword_12_1());
                      			
                    }
                    // InternalLinkerScript.g:1593:4: ( (lv_atMemory_23_0= ruleValidID ) )
                    // InternalLinkerScript.g:1594:5: (lv_atMemory_23_0= ruleValidID )
                    {
                    // InternalLinkerScript.g:1594:5: (lv_atMemory_23_0= ruleValidID )
                    // InternalLinkerScript.g:1595:6: lv_atMemory_23_0= ruleValidID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getOutputSectionAccess().getAtMemoryValidIDParserRuleCall_12_2_0());
                      					
                    }
                    pushFollow(FOLLOW_32);
                    lv_atMemory_23_0=ruleValidID();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getOutputSectionRule());
                      						}
                      						set(
                      							current,
                      							"atMemory",
                      							lv_atMemory_23_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.ValidID");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }


                    }
                    break;

            }

            // InternalLinkerScript.g:1613:3: (otherlv_24= ':' ( (lv_phdrs_25_0= ruleValidID ) ) )*
            loop28:
            do {
                int alt28=2;
                int LA28_0 = input.LA(1);

                if ( (LA28_0==37) ) {
                    alt28=1;
                }


                switch (alt28) {
            	case 1 :
            	    // InternalLinkerScript.g:1614:4: otherlv_24= ':' ( (lv_phdrs_25_0= ruleValidID ) )
            	    {
            	    otherlv_24=(Token)match(input,37,FOLLOW_6); if (state.failed) return current;
            	    if ( state.backtracking==0 ) {

            	      				newLeafNode(otherlv_24, grammarAccess.getOutputSectionAccess().getColonKeyword_13_0());
            	      			
            	    }
            	    // InternalLinkerScript.g:1618:4: ( (lv_phdrs_25_0= ruleValidID ) )
            	    // InternalLinkerScript.g:1619:5: (lv_phdrs_25_0= ruleValidID )
            	    {
            	    // InternalLinkerScript.g:1619:5: (lv_phdrs_25_0= ruleValidID )
            	    // InternalLinkerScript.g:1620:6: lv_phdrs_25_0= ruleValidID
            	    {
            	    if ( state.backtracking==0 ) {

            	      						newCompositeNode(grammarAccess.getOutputSectionAccess().getPhdrsValidIDParserRuleCall_13_1_0());
            	      					
            	    }
            	    pushFollow(FOLLOW_32);
            	    lv_phdrs_25_0=ruleValidID();

            	    state._fsp--;
            	    if (state.failed) return current;
            	    if ( state.backtracking==0 ) {

            	      						if (current==null) {
            	      							current = createModelElementForParent(grammarAccess.getOutputSectionRule());
            	      						}
            	      						add(
            	      							current,
            	      							"phdrs",
            	      							lv_phdrs_25_0,
            	      							"org.eclipse.cdt.linkerscript.LinkerScript.ValidID");
            	      						afterParserOrEnumRuleCall();
            	      					
            	    }

            	    }


            	    }


            	    }
            	    break;

            	default :
            	    break loop28;
                }
            } while (true);

            // InternalLinkerScript.g:1638:3: (otherlv_26= '=' ( (lv_fill_27_0= ruleLExpression ) ) )?
            int alt29=2;
            int LA29_0 = input.LA(1);

            if ( (LA29_0==41) ) {
                alt29=1;
            }
            switch (alt29) {
                case 1 :
                    // InternalLinkerScript.g:1639:4: otherlv_26= '=' ( (lv_fill_27_0= ruleLExpression ) )
                    {
                    otherlv_26=(Token)match(input,41,FOLLOW_8); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_26, grammarAccess.getOutputSectionAccess().getEqualsSignKeyword_14_0());
                      			
                    }
                    // InternalLinkerScript.g:1643:4: ( (lv_fill_27_0= ruleLExpression ) )
                    // InternalLinkerScript.g:1644:5: (lv_fill_27_0= ruleLExpression )
                    {
                    // InternalLinkerScript.g:1644:5: (lv_fill_27_0= ruleLExpression )
                    // InternalLinkerScript.g:1645:6: lv_fill_27_0= ruleLExpression
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getOutputSectionAccess().getFillLExpressionParserRuleCall_14_1_0());
                      					
                    }
                    pushFollow(FOLLOW_33);
                    lv_fill_27_0=ruleLExpression();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getOutputSectionRule());
                      						}
                      						set(
                      							current,
                      							"fill",
                      							lv_fill_27_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.LExpression");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }


                    }
                    break;

            }

            // InternalLinkerScript.g:1663:3: (otherlv_28= ',' )?
            int alt30=2;
            int LA30_0 = input.LA(1);

            if ( (LA30_0==10) ) {
                alt30=1;
            }
            switch (alt30) {
                case 1 :
                    // InternalLinkerScript.g:1664:4: otherlv_28= ','
                    {
                    otherlv_28=(Token)match(input,10,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_28, grammarAccess.getOutputSectionAccess().getCommaKeyword_15());
                      			
                    }

                    }
                    break;

            }


            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleOutputSection"


    // $ANTLR start "entryRuleOutputSectionAlign"
    // InternalLinkerScript.g:1673:1: entryRuleOutputSectionAlign returns [EObject current=null] : iv_ruleOutputSectionAlign= ruleOutputSectionAlign EOF ;
    public final EObject entryRuleOutputSectionAlign() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleOutputSectionAlign = null;


        try {
            // InternalLinkerScript.g:1673:59: (iv_ruleOutputSectionAlign= ruleOutputSectionAlign EOF )
            // InternalLinkerScript.g:1674:2: iv_ruleOutputSectionAlign= ruleOutputSectionAlign EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getOutputSectionAlignRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleOutputSectionAlign=ruleOutputSectionAlign();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleOutputSectionAlign; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleOutputSectionAlign"


    // $ANTLR start "ruleOutputSectionAlign"
    // InternalLinkerScript.g:1680:1: ruleOutputSectionAlign returns [EObject current=null] : ( ( () otherlv_1= 'ALIGN' otherlv_2= '(' ( (lv_exp_3_0= ruleLExpression ) ) otherlv_4= ')' ) | ( () otherlv_6= 'ALIGN_WITH_INPUT' ) ) ;
    public final EObject ruleOutputSectionAlign() throws RecognitionException {
        EObject current = null;

        Token otherlv_1=null;
        Token otherlv_2=null;
        Token otherlv_4=null;
        Token otherlv_6=null;
        EObject lv_exp_3_0 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:1686:2: ( ( ( () otherlv_1= 'ALIGN' otherlv_2= '(' ( (lv_exp_3_0= ruleLExpression ) ) otherlv_4= ')' ) | ( () otherlv_6= 'ALIGN_WITH_INPUT' ) ) )
            // InternalLinkerScript.g:1687:2: ( ( () otherlv_1= 'ALIGN' otherlv_2= '(' ( (lv_exp_3_0= ruleLExpression ) ) otherlv_4= ')' ) | ( () otherlv_6= 'ALIGN_WITH_INPUT' ) )
            {
            // InternalLinkerScript.g:1687:2: ( ( () otherlv_1= 'ALIGN' otherlv_2= '(' ( (lv_exp_3_0= ruleLExpression ) ) otherlv_4= ')' ) | ( () otherlv_6= 'ALIGN_WITH_INPUT' ) )
            int alt31=2;
            int LA31_0 = input.LA(1);

            if ( (LA31_0==42) ) {
                alt31=1;
            }
            else if ( (LA31_0==43) ) {
                alt31=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 31, 0, input);

                throw nvae;
            }
            switch (alt31) {
                case 1 :
                    // InternalLinkerScript.g:1688:3: ( () otherlv_1= 'ALIGN' otherlv_2= '(' ( (lv_exp_3_0= ruleLExpression ) ) otherlv_4= ')' )
                    {
                    // InternalLinkerScript.g:1688:3: ( () otherlv_1= 'ALIGN' otherlv_2= '(' ( (lv_exp_3_0= ruleLExpression ) ) otherlv_4= ')' )
                    // InternalLinkerScript.g:1689:4: () otherlv_1= 'ALIGN' otherlv_2= '(' ( (lv_exp_3_0= ruleLExpression ) ) otherlv_4= ')'
                    {
                    // InternalLinkerScript.g:1689:4: ()
                    // InternalLinkerScript.g:1690:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getOutputSectionAlignAccess().getOutputSectionAlignExpressionAction_0_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_1=(Token)match(input,42,FOLLOW_5); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_1, grammarAccess.getOutputSectionAlignAccess().getALIGNKeyword_0_1());
                      			
                    }
                    otherlv_2=(Token)match(input,13,FOLLOW_8); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_2, grammarAccess.getOutputSectionAlignAccess().getLeftParenthesisKeyword_0_2());
                      			
                    }
                    // InternalLinkerScript.g:1704:4: ( (lv_exp_3_0= ruleLExpression ) )
                    // InternalLinkerScript.g:1705:5: (lv_exp_3_0= ruleLExpression )
                    {
                    // InternalLinkerScript.g:1705:5: (lv_exp_3_0= ruleLExpression )
                    // InternalLinkerScript.g:1706:6: lv_exp_3_0= ruleLExpression
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getOutputSectionAlignAccess().getExpLExpressionParserRuleCall_0_3_0());
                      					
                    }
                    pushFollow(FOLLOW_7);
                    lv_exp_3_0=ruleLExpression();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getOutputSectionAlignRule());
                      						}
                      						set(
                      							current,
                      							"exp",
                      							lv_exp_3_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.LExpression");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    otherlv_4=(Token)match(input,14,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_4, grammarAccess.getOutputSectionAlignAccess().getRightParenthesisKeyword_0_4());
                      			
                    }

                    }


                    }
                    break;
                case 2 :
                    // InternalLinkerScript.g:1729:3: ( () otherlv_6= 'ALIGN_WITH_INPUT' )
                    {
                    // InternalLinkerScript.g:1729:3: ( () otherlv_6= 'ALIGN_WITH_INPUT' )
                    // InternalLinkerScript.g:1730:4: () otherlv_6= 'ALIGN_WITH_INPUT'
                    {
                    // InternalLinkerScript.g:1730:4: ()
                    // InternalLinkerScript.g:1731:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getOutputSectionAlignAccess().getOutputSectionAlignWithInputAction_1_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_6=(Token)match(input,43,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_6, grammarAccess.getOutputSectionAlignAccess().getALIGN_WITH_INPUTKeyword_1_1());
                      			
                    }

                    }


                    }
                    break;

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleOutputSectionAlign"


    // $ANTLR start "entryRuleOutputSectionConstraint"
    // InternalLinkerScript.g:1746:1: entryRuleOutputSectionConstraint returns [EObject current=null] : iv_ruleOutputSectionConstraint= ruleOutputSectionConstraint EOF ;
    public final EObject entryRuleOutputSectionConstraint() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleOutputSectionConstraint = null;


        try {
            // InternalLinkerScript.g:1746:64: (iv_ruleOutputSectionConstraint= ruleOutputSectionConstraint EOF )
            // InternalLinkerScript.g:1747:2: iv_ruleOutputSectionConstraint= ruleOutputSectionConstraint EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getOutputSectionConstraintRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleOutputSectionConstraint=ruleOutputSectionConstraint();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleOutputSectionConstraint; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleOutputSectionConstraint"


    // $ANTLR start "ruleOutputSectionConstraint"
    // InternalLinkerScript.g:1753:1: ruleOutputSectionConstraint returns [EObject current=null] : ( ( () otherlv_1= 'ONLY_IF_RO' ) | ( () otherlv_3= 'ONLY_IF_RW' ) | ( () otherlv_5= 'SPECIAL' ) ) ;
    public final EObject ruleOutputSectionConstraint() throws RecognitionException {
        EObject current = null;

        Token otherlv_1=null;
        Token otherlv_3=null;
        Token otherlv_5=null;


        	enterRule();

        try {
            // InternalLinkerScript.g:1759:2: ( ( ( () otherlv_1= 'ONLY_IF_RO' ) | ( () otherlv_3= 'ONLY_IF_RW' ) | ( () otherlv_5= 'SPECIAL' ) ) )
            // InternalLinkerScript.g:1760:2: ( ( () otherlv_1= 'ONLY_IF_RO' ) | ( () otherlv_3= 'ONLY_IF_RW' ) | ( () otherlv_5= 'SPECIAL' ) )
            {
            // InternalLinkerScript.g:1760:2: ( ( () otherlv_1= 'ONLY_IF_RO' ) | ( () otherlv_3= 'ONLY_IF_RW' ) | ( () otherlv_5= 'SPECIAL' ) )
            int alt32=3;
            switch ( input.LA(1) ) {
            case 44:
                {
                alt32=1;
                }
                break;
            case 45:
                {
                alt32=2;
                }
                break;
            case 46:
                {
                alt32=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 32, 0, input);

                throw nvae;
            }

            switch (alt32) {
                case 1 :
                    // InternalLinkerScript.g:1761:3: ( () otherlv_1= 'ONLY_IF_RO' )
                    {
                    // InternalLinkerScript.g:1761:3: ( () otherlv_1= 'ONLY_IF_RO' )
                    // InternalLinkerScript.g:1762:4: () otherlv_1= 'ONLY_IF_RO'
                    {
                    // InternalLinkerScript.g:1762:4: ()
                    // InternalLinkerScript.g:1763:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getOutputSectionConstraintAccess().getOutputSectionConstraintOnlyIfROAction_0_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_1=(Token)match(input,44,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_1, grammarAccess.getOutputSectionConstraintAccess().getONLY_IF_ROKeyword_0_1());
                      			
                    }

                    }


                    }
                    break;
                case 2 :
                    // InternalLinkerScript.g:1775:3: ( () otherlv_3= 'ONLY_IF_RW' )
                    {
                    // InternalLinkerScript.g:1775:3: ( () otherlv_3= 'ONLY_IF_RW' )
                    // InternalLinkerScript.g:1776:4: () otherlv_3= 'ONLY_IF_RW'
                    {
                    // InternalLinkerScript.g:1776:4: ()
                    // InternalLinkerScript.g:1777:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getOutputSectionConstraintAccess().getOutputSectionConstraintOnlyIfRWAction_1_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_3=(Token)match(input,45,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_3, grammarAccess.getOutputSectionConstraintAccess().getONLY_IF_RWKeyword_1_1());
                      			
                    }

                    }


                    }
                    break;
                case 3 :
                    // InternalLinkerScript.g:1789:3: ( () otherlv_5= 'SPECIAL' )
                    {
                    // InternalLinkerScript.g:1789:3: ( () otherlv_5= 'SPECIAL' )
                    // InternalLinkerScript.g:1790:4: () otherlv_5= 'SPECIAL'
                    {
                    // InternalLinkerScript.g:1790:4: ()
                    // InternalLinkerScript.g:1791:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getOutputSectionConstraintAccess().getOutputSectionConstraintSpecialAction_2_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_5=(Token)match(input,46,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_5, grammarAccess.getOutputSectionConstraintAccess().getSPECIALKeyword_2_1());
                      			
                    }

                    }


                    }
                    break;

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleOutputSectionConstraint"


    // $ANTLR start "entryRuleOutputSectionType"
    // InternalLinkerScript.g:1806:1: entryRuleOutputSectionType returns [EObject current=null] : iv_ruleOutputSectionType= ruleOutputSectionType EOF ;
    public final EObject entryRuleOutputSectionType() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleOutputSectionType = null;


        try {
            // InternalLinkerScript.g:1806:58: (iv_ruleOutputSectionType= ruleOutputSectionType EOF )
            // InternalLinkerScript.g:1807:2: iv_ruleOutputSectionType= ruleOutputSectionType EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getOutputSectionTypeRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleOutputSectionType=ruleOutputSectionType();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleOutputSectionType; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleOutputSectionType"


    // $ANTLR start "ruleOutputSectionType"
    // InternalLinkerScript.g:1813:1: ruleOutputSectionType returns [EObject current=null] : ( ( () otherlv_1= 'NOLOAD' ) | ( () otherlv_3= 'DSECT' ) | ( () otherlv_5= 'COPY' ) | ( () otherlv_7= 'INFO' ) | ( () otherlv_9= 'OVERLAY' ) ) ;
    public final EObject ruleOutputSectionType() throws RecognitionException {
        EObject current = null;

        Token otherlv_1=null;
        Token otherlv_3=null;
        Token otherlv_5=null;
        Token otherlv_7=null;
        Token otherlv_9=null;


        	enterRule();

        try {
            // InternalLinkerScript.g:1819:2: ( ( ( () otherlv_1= 'NOLOAD' ) | ( () otherlv_3= 'DSECT' ) | ( () otherlv_5= 'COPY' ) | ( () otherlv_7= 'INFO' ) | ( () otherlv_9= 'OVERLAY' ) ) )
            // InternalLinkerScript.g:1820:2: ( ( () otherlv_1= 'NOLOAD' ) | ( () otherlv_3= 'DSECT' ) | ( () otherlv_5= 'COPY' ) | ( () otherlv_7= 'INFO' ) | ( () otherlv_9= 'OVERLAY' ) )
            {
            // InternalLinkerScript.g:1820:2: ( ( () otherlv_1= 'NOLOAD' ) | ( () otherlv_3= 'DSECT' ) | ( () otherlv_5= 'COPY' ) | ( () otherlv_7= 'INFO' ) | ( () otherlv_9= 'OVERLAY' ) )
            int alt33=5;
            switch ( input.LA(1) ) {
            case 47:
                {
                alt33=1;
                }
                break;
            case 48:
                {
                alt33=2;
                }
                break;
            case 49:
                {
                alt33=3;
                }
                break;
            case 50:
                {
                alt33=4;
                }
                break;
            case 51:
                {
                alt33=5;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 33, 0, input);

                throw nvae;
            }

            switch (alt33) {
                case 1 :
                    // InternalLinkerScript.g:1821:3: ( () otherlv_1= 'NOLOAD' )
                    {
                    // InternalLinkerScript.g:1821:3: ( () otherlv_1= 'NOLOAD' )
                    // InternalLinkerScript.g:1822:4: () otherlv_1= 'NOLOAD'
                    {
                    // InternalLinkerScript.g:1822:4: ()
                    // InternalLinkerScript.g:1823:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getOutputSectionTypeAccess().getOutputSectionTypeNoLoadAction_0_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_1=(Token)match(input,47,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_1, grammarAccess.getOutputSectionTypeAccess().getNOLOADKeyword_0_1());
                      			
                    }

                    }


                    }
                    break;
                case 2 :
                    // InternalLinkerScript.g:1835:3: ( () otherlv_3= 'DSECT' )
                    {
                    // InternalLinkerScript.g:1835:3: ( () otherlv_3= 'DSECT' )
                    // InternalLinkerScript.g:1836:4: () otherlv_3= 'DSECT'
                    {
                    // InternalLinkerScript.g:1836:4: ()
                    // InternalLinkerScript.g:1837:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getOutputSectionTypeAccess().getOutputSectionTypeDSectAction_1_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_3=(Token)match(input,48,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_3, grammarAccess.getOutputSectionTypeAccess().getDSECTKeyword_1_1());
                      			
                    }

                    }


                    }
                    break;
                case 3 :
                    // InternalLinkerScript.g:1849:3: ( () otherlv_5= 'COPY' )
                    {
                    // InternalLinkerScript.g:1849:3: ( () otherlv_5= 'COPY' )
                    // InternalLinkerScript.g:1850:4: () otherlv_5= 'COPY'
                    {
                    // InternalLinkerScript.g:1850:4: ()
                    // InternalLinkerScript.g:1851:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getOutputSectionTypeAccess().getOutputSectionTypeCopyAction_2_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_5=(Token)match(input,49,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_5, grammarAccess.getOutputSectionTypeAccess().getCOPYKeyword_2_1());
                      			
                    }

                    }


                    }
                    break;
                case 4 :
                    // InternalLinkerScript.g:1863:3: ( () otherlv_7= 'INFO' )
                    {
                    // InternalLinkerScript.g:1863:3: ( () otherlv_7= 'INFO' )
                    // InternalLinkerScript.g:1864:4: () otherlv_7= 'INFO'
                    {
                    // InternalLinkerScript.g:1864:4: ()
                    // InternalLinkerScript.g:1865:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getOutputSectionTypeAccess().getOutputSectionTypeInfoAction_3_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_7=(Token)match(input,50,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_7, grammarAccess.getOutputSectionTypeAccess().getINFOKeyword_3_1());
                      			
                    }

                    }


                    }
                    break;
                case 5 :
                    // InternalLinkerScript.g:1877:3: ( () otherlv_9= 'OVERLAY' )
                    {
                    // InternalLinkerScript.g:1877:3: ( () otherlv_9= 'OVERLAY' )
                    // InternalLinkerScript.g:1878:4: () otherlv_9= 'OVERLAY'
                    {
                    // InternalLinkerScript.g:1878:4: ()
                    // InternalLinkerScript.g:1879:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getOutputSectionTypeAccess().getOutputSectionTypeOverlayAction_4_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_9=(Token)match(input,51,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_9, grammarAccess.getOutputSectionTypeAccess().getOVERLAYKeyword_4_1());
                      			
                    }

                    }


                    }
                    break;

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleOutputSectionType"


    // $ANTLR start "entryRuleStatement"
    // InternalLinkerScript.g:1894:1: entryRuleStatement returns [EObject current=null] : iv_ruleStatement= ruleStatement EOF ;
    public final EObject entryRuleStatement() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleStatement = null;


        try {
            // InternalLinkerScript.g:1894:50: (iv_ruleStatement= ruleStatement EOF )
            // InternalLinkerScript.g:1895:2: iv_ruleStatement= ruleStatement EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getStatementRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleStatement=ruleStatement();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleStatement; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleStatement"


    // $ANTLR start "ruleStatement"
    // InternalLinkerScript.g:1901:1: ruleStatement returns [EObject current=null] : ( ( () ( (lv_assignment_1_0= ruleAssignmentRule ) ) (otherlv_2= ',' | otherlv_3= ';' ) ) | ( () otherlv_5= 'CREATE_OBJECT_SYMBOLS' ) | ( () otherlv_7= 'CONSTRUCTORS' ) | ( () otherlv_9= 'SORT_BY_NAME' otherlv_10= '(' otherlv_11= 'CONSTRUCTORS' otherlv_12= ')' ) | ( () ( (lv_size_14_0= ruleStatementDataSize ) ) otherlv_15= '(' ( (lv_data_16_0= ruleLExpression ) ) otherlv_17= ')' ) | ( () otherlv_19= 'FILL' otherlv_20= '(' ( (lv_fill_21_0= ruleLExpression ) ) otherlv_22= ')' ) | ( () otherlv_24= 'ASSERT' otherlv_25= '(' ( (lv_exp_26_0= ruleLExpression ) ) otherlv_27= ',' ( (lv_message_28_0= ruleValidID ) ) otherlv_29= ')' ) | ( () otherlv_31= 'INCLUDE' ( (lv_filename_32_0= ruleWildID ) ) ) | ( () ( (lv_spec_34_0= ruleInputSection ) ) ) | ( () otherlv_36= ';' ) ) ;
    public final EObject ruleStatement() throws RecognitionException {
        EObject current = null;

        Token otherlv_2=null;
        Token otherlv_3=null;
        Token otherlv_5=null;
        Token otherlv_7=null;
        Token otherlv_9=null;
        Token otherlv_10=null;
        Token otherlv_11=null;
        Token otherlv_12=null;
        Token otherlv_15=null;
        Token otherlv_17=null;
        Token otherlv_19=null;
        Token otherlv_20=null;
        Token otherlv_22=null;
        Token otherlv_24=null;
        Token otherlv_25=null;
        Token otherlv_27=null;
        Token otherlv_29=null;
        Token otherlv_31=null;
        Token otherlv_36=null;
        EObject lv_assignment_1_0 = null;

        AntlrDatatypeRuleToken lv_size_14_0 = null;

        EObject lv_data_16_0 = null;

        EObject lv_fill_21_0 = null;

        EObject lv_exp_26_0 = null;

        AntlrDatatypeRuleToken lv_message_28_0 = null;

        AntlrDatatypeRuleToken lv_filename_32_0 = null;

        EObject lv_spec_34_0 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:1907:2: ( ( ( () ( (lv_assignment_1_0= ruleAssignmentRule ) ) (otherlv_2= ',' | otherlv_3= ';' ) ) | ( () otherlv_5= 'CREATE_OBJECT_SYMBOLS' ) | ( () otherlv_7= 'CONSTRUCTORS' ) | ( () otherlv_9= 'SORT_BY_NAME' otherlv_10= '(' otherlv_11= 'CONSTRUCTORS' otherlv_12= ')' ) | ( () ( (lv_size_14_0= ruleStatementDataSize ) ) otherlv_15= '(' ( (lv_data_16_0= ruleLExpression ) ) otherlv_17= ')' ) | ( () otherlv_19= 'FILL' otherlv_20= '(' ( (lv_fill_21_0= ruleLExpression ) ) otherlv_22= ')' ) | ( () otherlv_24= 'ASSERT' otherlv_25= '(' ( (lv_exp_26_0= ruleLExpression ) ) otherlv_27= ',' ( (lv_message_28_0= ruleValidID ) ) otherlv_29= ')' ) | ( () otherlv_31= 'INCLUDE' ( (lv_filename_32_0= ruleWildID ) ) ) | ( () ( (lv_spec_34_0= ruleInputSection ) ) ) | ( () otherlv_36= ';' ) ) )
            // InternalLinkerScript.g:1908:2: ( ( () ( (lv_assignment_1_0= ruleAssignmentRule ) ) (otherlv_2= ',' | otherlv_3= ';' ) ) | ( () otherlv_5= 'CREATE_OBJECT_SYMBOLS' ) | ( () otherlv_7= 'CONSTRUCTORS' ) | ( () otherlv_9= 'SORT_BY_NAME' otherlv_10= '(' otherlv_11= 'CONSTRUCTORS' otherlv_12= ')' ) | ( () ( (lv_size_14_0= ruleStatementDataSize ) ) otherlv_15= '(' ( (lv_data_16_0= ruleLExpression ) ) otherlv_17= ')' ) | ( () otherlv_19= 'FILL' otherlv_20= '(' ( (lv_fill_21_0= ruleLExpression ) ) otherlv_22= ')' ) | ( () otherlv_24= 'ASSERT' otherlv_25= '(' ( (lv_exp_26_0= ruleLExpression ) ) otherlv_27= ',' ( (lv_message_28_0= ruleValidID ) ) otherlv_29= ')' ) | ( () otherlv_31= 'INCLUDE' ( (lv_filename_32_0= ruleWildID ) ) ) | ( () ( (lv_spec_34_0= ruleInputSection ) ) ) | ( () otherlv_36= ';' ) )
            {
            // InternalLinkerScript.g:1908:2: ( ( () ( (lv_assignment_1_0= ruleAssignmentRule ) ) (otherlv_2= ',' | otherlv_3= ';' ) ) | ( () otherlv_5= 'CREATE_OBJECT_SYMBOLS' ) | ( () otherlv_7= 'CONSTRUCTORS' ) | ( () otherlv_9= 'SORT_BY_NAME' otherlv_10= '(' otherlv_11= 'CONSTRUCTORS' otherlv_12= ')' ) | ( () ( (lv_size_14_0= ruleStatementDataSize ) ) otherlv_15= '(' ( (lv_data_16_0= ruleLExpression ) ) otherlv_17= ')' ) | ( () otherlv_19= 'FILL' otherlv_20= '(' ( (lv_fill_21_0= ruleLExpression ) ) otherlv_22= ')' ) | ( () otherlv_24= 'ASSERT' otherlv_25= '(' ( (lv_exp_26_0= ruleLExpression ) ) otherlv_27= ',' ( (lv_message_28_0= ruleValidID ) ) otherlv_29= ')' ) | ( () otherlv_31= 'INCLUDE' ( (lv_filename_32_0= ruleWildID ) ) ) | ( () ( (lv_spec_34_0= ruleInputSection ) ) ) | ( () otherlv_36= ';' ) )
            int alt35=10;
            alt35 = dfa35.predict(input);
            switch (alt35) {
                case 1 :
                    // InternalLinkerScript.g:1909:3: ( () ( (lv_assignment_1_0= ruleAssignmentRule ) ) (otherlv_2= ',' | otherlv_3= ';' ) )
                    {
                    // InternalLinkerScript.g:1909:3: ( () ( (lv_assignment_1_0= ruleAssignmentRule ) ) (otherlv_2= ',' | otherlv_3= ';' ) )
                    // InternalLinkerScript.g:1910:4: () ( (lv_assignment_1_0= ruleAssignmentRule ) ) (otherlv_2= ',' | otherlv_3= ';' )
                    {
                    // InternalLinkerScript.g:1910:4: ()
                    // InternalLinkerScript.g:1911:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getStatementAccess().getStatementAssignmentAction_0_0(),
                      						current);
                      				
                    }

                    }

                    // InternalLinkerScript.g:1917:4: ( (lv_assignment_1_0= ruleAssignmentRule ) )
                    // InternalLinkerScript.g:1918:5: (lv_assignment_1_0= ruleAssignmentRule )
                    {
                    // InternalLinkerScript.g:1918:5: (lv_assignment_1_0= ruleAssignmentRule )
                    // InternalLinkerScript.g:1919:6: lv_assignment_1_0= ruleAssignmentRule
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getStatementAccess().getAssignmentAssignmentRuleParserRuleCall_0_1_0());
                      					
                    }
                    pushFollow(FOLLOW_4);
                    lv_assignment_1_0=ruleAssignmentRule();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getStatementRule());
                      						}
                      						set(
                      							current,
                      							"assignment",
                      							lv_assignment_1_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.AssignmentRule");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    // InternalLinkerScript.g:1936:4: (otherlv_2= ',' | otherlv_3= ';' )
                    int alt34=2;
                    int LA34_0 = input.LA(1);

                    if ( (LA34_0==10) ) {
                        alt34=1;
                    }
                    else if ( (LA34_0==11) ) {
                        alt34=2;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return current;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 34, 0, input);

                        throw nvae;
                    }
                    switch (alt34) {
                        case 1 :
                            // InternalLinkerScript.g:1937:5: otherlv_2= ','
                            {
                            otherlv_2=(Token)match(input,10,FOLLOW_2); if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              					newLeafNode(otherlv_2, grammarAccess.getStatementAccess().getCommaKeyword_0_2_0());
                              				
                            }

                            }
                            break;
                        case 2 :
                            // InternalLinkerScript.g:1942:5: otherlv_3= ';'
                            {
                            otherlv_3=(Token)match(input,11,FOLLOW_2); if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              					newLeafNode(otherlv_3, grammarAccess.getStatementAccess().getSemicolonKeyword_0_2_1());
                              				
                            }

                            }
                            break;

                    }


                    }


                    }
                    break;
                case 2 :
                    // InternalLinkerScript.g:1949:3: ( () otherlv_5= 'CREATE_OBJECT_SYMBOLS' )
                    {
                    // InternalLinkerScript.g:1949:3: ( () otherlv_5= 'CREATE_OBJECT_SYMBOLS' )
                    // InternalLinkerScript.g:1950:4: () otherlv_5= 'CREATE_OBJECT_SYMBOLS'
                    {
                    // InternalLinkerScript.g:1950:4: ()
                    // InternalLinkerScript.g:1951:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getStatementAccess().getStatementCreateObjectSymbolsAction_1_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_5=(Token)match(input,52,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_5, grammarAccess.getStatementAccess().getCREATE_OBJECT_SYMBOLSKeyword_1_1());
                      			
                    }

                    }


                    }
                    break;
                case 3 :
                    // InternalLinkerScript.g:1963:3: ( () otherlv_7= 'CONSTRUCTORS' )
                    {
                    // InternalLinkerScript.g:1963:3: ( () otherlv_7= 'CONSTRUCTORS' )
                    // InternalLinkerScript.g:1964:4: () otherlv_7= 'CONSTRUCTORS'
                    {
                    // InternalLinkerScript.g:1964:4: ()
                    // InternalLinkerScript.g:1965:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getStatementAccess().getStatementConstructorsAction_2_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_7=(Token)match(input,53,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_7, grammarAccess.getStatementAccess().getCONSTRUCTORSKeyword_2_1());
                      			
                    }

                    }


                    }
                    break;
                case 4 :
                    // InternalLinkerScript.g:1977:3: ( () otherlv_9= 'SORT_BY_NAME' otherlv_10= '(' otherlv_11= 'CONSTRUCTORS' otherlv_12= ')' )
                    {
                    // InternalLinkerScript.g:1977:3: ( () otherlv_9= 'SORT_BY_NAME' otherlv_10= '(' otherlv_11= 'CONSTRUCTORS' otherlv_12= ')' )
                    // InternalLinkerScript.g:1978:4: () otherlv_9= 'SORT_BY_NAME' otherlv_10= '(' otherlv_11= 'CONSTRUCTORS' otherlv_12= ')'
                    {
                    // InternalLinkerScript.g:1978:4: ()
                    // InternalLinkerScript.g:1979:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getStatementAccess().getStatementConstructorsSortedAction_3_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_9=(Token)match(input,54,FOLLOW_5); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_9, grammarAccess.getStatementAccess().getSORT_BY_NAMEKeyword_3_1());
                      			
                    }
                    otherlv_10=(Token)match(input,13,FOLLOW_34); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_10, grammarAccess.getStatementAccess().getLeftParenthesisKeyword_3_2());
                      			
                    }
                    otherlv_11=(Token)match(input,53,FOLLOW_7); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_11, grammarAccess.getStatementAccess().getCONSTRUCTORSKeyword_3_3());
                      			
                    }
                    otherlv_12=(Token)match(input,14,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_12, grammarAccess.getStatementAccess().getRightParenthesisKeyword_3_4());
                      			
                    }

                    }


                    }
                    break;
                case 5 :
                    // InternalLinkerScript.g:2003:3: ( () ( (lv_size_14_0= ruleStatementDataSize ) ) otherlv_15= '(' ( (lv_data_16_0= ruleLExpression ) ) otherlv_17= ')' )
                    {
                    // InternalLinkerScript.g:2003:3: ( () ( (lv_size_14_0= ruleStatementDataSize ) ) otherlv_15= '(' ( (lv_data_16_0= ruleLExpression ) ) otherlv_17= ')' )
                    // InternalLinkerScript.g:2004:4: () ( (lv_size_14_0= ruleStatementDataSize ) ) otherlv_15= '(' ( (lv_data_16_0= ruleLExpression ) ) otherlv_17= ')'
                    {
                    // InternalLinkerScript.g:2004:4: ()
                    // InternalLinkerScript.g:2005:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getStatementAccess().getStatementDataAction_4_0(),
                      						current);
                      				
                    }

                    }

                    // InternalLinkerScript.g:2011:4: ( (lv_size_14_0= ruleStatementDataSize ) )
                    // InternalLinkerScript.g:2012:5: (lv_size_14_0= ruleStatementDataSize )
                    {
                    // InternalLinkerScript.g:2012:5: (lv_size_14_0= ruleStatementDataSize )
                    // InternalLinkerScript.g:2013:6: lv_size_14_0= ruleStatementDataSize
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getStatementAccess().getSizeStatementDataSizeParserRuleCall_4_1_0());
                      					
                    }
                    pushFollow(FOLLOW_5);
                    lv_size_14_0=ruleStatementDataSize();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getStatementRule());
                      						}
                      						set(
                      							current,
                      							"size",
                      							lv_size_14_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.StatementDataSize");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    otherlv_15=(Token)match(input,13,FOLLOW_8); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_15, grammarAccess.getStatementAccess().getLeftParenthesisKeyword_4_2());
                      			
                    }
                    // InternalLinkerScript.g:2034:4: ( (lv_data_16_0= ruleLExpression ) )
                    // InternalLinkerScript.g:2035:5: (lv_data_16_0= ruleLExpression )
                    {
                    // InternalLinkerScript.g:2035:5: (lv_data_16_0= ruleLExpression )
                    // InternalLinkerScript.g:2036:6: lv_data_16_0= ruleLExpression
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getStatementAccess().getDataLExpressionParserRuleCall_4_3_0());
                      					
                    }
                    pushFollow(FOLLOW_7);
                    lv_data_16_0=ruleLExpression();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getStatementRule());
                      						}
                      						set(
                      							current,
                      							"data",
                      							lv_data_16_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.LExpression");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    otherlv_17=(Token)match(input,14,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_17, grammarAccess.getStatementAccess().getRightParenthesisKeyword_4_4());
                      			
                    }

                    }


                    }
                    break;
                case 6 :
                    // InternalLinkerScript.g:2059:3: ( () otherlv_19= 'FILL' otherlv_20= '(' ( (lv_fill_21_0= ruleLExpression ) ) otherlv_22= ')' )
                    {
                    // InternalLinkerScript.g:2059:3: ( () otherlv_19= 'FILL' otherlv_20= '(' ( (lv_fill_21_0= ruleLExpression ) ) otherlv_22= ')' )
                    // InternalLinkerScript.g:2060:4: () otherlv_19= 'FILL' otherlv_20= '(' ( (lv_fill_21_0= ruleLExpression ) ) otherlv_22= ')'
                    {
                    // InternalLinkerScript.g:2060:4: ()
                    // InternalLinkerScript.g:2061:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getStatementAccess().getStatementFillAction_5_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_19=(Token)match(input,55,FOLLOW_5); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_19, grammarAccess.getStatementAccess().getFILLKeyword_5_1());
                      			
                    }
                    otherlv_20=(Token)match(input,13,FOLLOW_8); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_20, grammarAccess.getStatementAccess().getLeftParenthesisKeyword_5_2());
                      			
                    }
                    // InternalLinkerScript.g:2075:4: ( (lv_fill_21_0= ruleLExpression ) )
                    // InternalLinkerScript.g:2076:5: (lv_fill_21_0= ruleLExpression )
                    {
                    // InternalLinkerScript.g:2076:5: (lv_fill_21_0= ruleLExpression )
                    // InternalLinkerScript.g:2077:6: lv_fill_21_0= ruleLExpression
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getStatementAccess().getFillLExpressionParserRuleCall_5_3_0());
                      					
                    }
                    pushFollow(FOLLOW_7);
                    lv_fill_21_0=ruleLExpression();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getStatementRule());
                      						}
                      						set(
                      							current,
                      							"fill",
                      							lv_fill_21_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.LExpression");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    otherlv_22=(Token)match(input,14,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_22, grammarAccess.getStatementAccess().getRightParenthesisKeyword_5_4());
                      			
                    }

                    }


                    }
                    break;
                case 7 :
                    // InternalLinkerScript.g:2100:3: ( () otherlv_24= 'ASSERT' otherlv_25= '(' ( (lv_exp_26_0= ruleLExpression ) ) otherlv_27= ',' ( (lv_message_28_0= ruleValidID ) ) otherlv_29= ')' )
                    {
                    // InternalLinkerScript.g:2100:3: ( () otherlv_24= 'ASSERT' otherlv_25= '(' ( (lv_exp_26_0= ruleLExpression ) ) otherlv_27= ',' ( (lv_message_28_0= ruleValidID ) ) otherlv_29= ')' )
                    // InternalLinkerScript.g:2101:4: () otherlv_24= 'ASSERT' otherlv_25= '(' ( (lv_exp_26_0= ruleLExpression ) ) otherlv_27= ',' ( (lv_message_28_0= ruleValidID ) ) otherlv_29= ')'
                    {
                    // InternalLinkerScript.g:2101:4: ()
                    // InternalLinkerScript.g:2102:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getStatementAccess().getStatementAssertAction_6_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_24=(Token)match(input,16,FOLLOW_5); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_24, grammarAccess.getStatementAccess().getASSERTKeyword_6_1());
                      			
                    }
                    otherlv_25=(Token)match(input,13,FOLLOW_8); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_25, grammarAccess.getStatementAccess().getLeftParenthesisKeyword_6_2());
                      			
                    }
                    // InternalLinkerScript.g:2116:4: ( (lv_exp_26_0= ruleLExpression ) )
                    // InternalLinkerScript.g:2117:5: (lv_exp_26_0= ruleLExpression )
                    {
                    // InternalLinkerScript.g:2117:5: (lv_exp_26_0= ruleLExpression )
                    // InternalLinkerScript.g:2118:6: lv_exp_26_0= ruleLExpression
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getStatementAccess().getExpLExpressionParserRuleCall_6_3_0());
                      					
                    }
                    pushFollow(FOLLOW_9);
                    lv_exp_26_0=ruleLExpression();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getStatementRule());
                      						}
                      						set(
                      							current,
                      							"exp",
                      							lv_exp_26_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.LExpression");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    otherlv_27=(Token)match(input,10,FOLLOW_6); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_27, grammarAccess.getStatementAccess().getCommaKeyword_6_4());
                      			
                    }
                    // InternalLinkerScript.g:2139:4: ( (lv_message_28_0= ruleValidID ) )
                    // InternalLinkerScript.g:2140:5: (lv_message_28_0= ruleValidID )
                    {
                    // InternalLinkerScript.g:2140:5: (lv_message_28_0= ruleValidID )
                    // InternalLinkerScript.g:2141:6: lv_message_28_0= ruleValidID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getStatementAccess().getMessageValidIDParserRuleCall_6_5_0());
                      					
                    }
                    pushFollow(FOLLOW_7);
                    lv_message_28_0=ruleValidID();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getStatementRule());
                      						}
                      						set(
                      							current,
                      							"message",
                      							lv_message_28_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.ValidID");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    otherlv_29=(Token)match(input,14,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_29, grammarAccess.getStatementAccess().getRightParenthesisKeyword_6_6());
                      			
                    }

                    }


                    }
                    break;
                case 8 :
                    // InternalLinkerScript.g:2164:3: ( () otherlv_31= 'INCLUDE' ( (lv_filename_32_0= ruleWildID ) ) )
                    {
                    // InternalLinkerScript.g:2164:3: ( () otherlv_31= 'INCLUDE' ( (lv_filename_32_0= ruleWildID ) ) )
                    // InternalLinkerScript.g:2165:4: () otherlv_31= 'INCLUDE' ( (lv_filename_32_0= ruleWildID ) )
                    {
                    // InternalLinkerScript.g:2165:4: ()
                    // InternalLinkerScript.g:2166:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getStatementAccess().getStatementIncludeAction_7_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_31=(Token)match(input,30,FOLLOW_6); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_31, grammarAccess.getStatementAccess().getINCLUDEKeyword_7_1());
                      			
                    }
                    // InternalLinkerScript.g:2176:4: ( (lv_filename_32_0= ruleWildID ) )
                    // InternalLinkerScript.g:2177:5: (lv_filename_32_0= ruleWildID )
                    {
                    // InternalLinkerScript.g:2177:5: (lv_filename_32_0= ruleWildID )
                    // InternalLinkerScript.g:2178:6: lv_filename_32_0= ruleWildID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getStatementAccess().getFilenameWildIDParserRuleCall_7_2_0());
                      					
                    }
                    pushFollow(FOLLOW_2);
                    lv_filename_32_0=ruleWildID();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getStatementRule());
                      						}
                      						set(
                      							current,
                      							"filename",
                      							lv_filename_32_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.WildID");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }


                    }


                    }
                    break;
                case 9 :
                    // InternalLinkerScript.g:2197:3: ( () ( (lv_spec_34_0= ruleInputSection ) ) )
                    {
                    // InternalLinkerScript.g:2197:3: ( () ( (lv_spec_34_0= ruleInputSection ) ) )
                    // InternalLinkerScript.g:2198:4: () ( (lv_spec_34_0= ruleInputSection ) )
                    {
                    // InternalLinkerScript.g:2198:4: ()
                    // InternalLinkerScript.g:2199:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getStatementAccess().getStatementInputSectionAction_8_0(),
                      						current);
                      				
                    }

                    }

                    // InternalLinkerScript.g:2205:4: ( (lv_spec_34_0= ruleInputSection ) )
                    // InternalLinkerScript.g:2206:5: (lv_spec_34_0= ruleInputSection )
                    {
                    // InternalLinkerScript.g:2206:5: (lv_spec_34_0= ruleInputSection )
                    // InternalLinkerScript.g:2207:6: lv_spec_34_0= ruleInputSection
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getStatementAccess().getSpecInputSectionParserRuleCall_8_1_0());
                      					
                    }
                    pushFollow(FOLLOW_2);
                    lv_spec_34_0=ruleInputSection();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getStatementRule());
                      						}
                      						set(
                      							current,
                      							"spec",
                      							lv_spec_34_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.InputSection");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }


                    }


                    }
                    break;
                case 10 :
                    // InternalLinkerScript.g:2226:3: ( () otherlv_36= ';' )
                    {
                    // InternalLinkerScript.g:2226:3: ( () otherlv_36= ';' )
                    // InternalLinkerScript.g:2227:4: () otherlv_36= ';'
                    {
                    // InternalLinkerScript.g:2227:4: ()
                    // InternalLinkerScript.g:2228:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getStatementAccess().getStatementNopAction_9_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_36=(Token)match(input,11,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_36, grammarAccess.getStatementAccess().getSemicolonKeyword_9_1());
                      			
                    }

                    }


                    }
                    break;

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleStatement"


    // $ANTLR start "entryRuleStatementAnywhere"
    // InternalLinkerScript.g:2243:1: entryRuleStatementAnywhere returns [EObject current=null] : iv_ruleStatementAnywhere= ruleStatementAnywhere EOF ;
    public final EObject entryRuleStatementAnywhere() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleStatementAnywhere = null;


        try {
            // InternalLinkerScript.g:2243:58: (iv_ruleStatementAnywhere= ruleStatementAnywhere EOF )
            // InternalLinkerScript.g:2244:2: iv_ruleStatementAnywhere= ruleStatementAnywhere EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getStatementAnywhereRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleStatementAnywhere=ruleStatementAnywhere();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleStatementAnywhere; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleStatementAnywhere"


    // $ANTLR start "ruleStatementAnywhere"
    // InternalLinkerScript.g:2250:1: ruleStatementAnywhere returns [EObject current=null] : ( ( () ( (lv_assignment_1_0= ruleAssignmentRule ) ) (otherlv_2= ',' | otherlv_3= ';' ) ) | ( () otherlv_5= 'ENTRY' otherlv_6= '(' ( (lv_name_7_0= ruleValidID ) ) otherlv_8= ')' ) | ( () otherlv_10= 'ASSERT' otherlv_11= '(' ( (lv_exp_12_0= ruleLExpression ) ) otherlv_13= ',' ( (lv_message_14_0= ruleValidID ) ) otherlv_15= ')' ) | ( () otherlv_17= ';' ) ) ;
    public final EObject ruleStatementAnywhere() throws RecognitionException {
        EObject current = null;

        Token otherlv_2=null;
        Token otherlv_3=null;
        Token otherlv_5=null;
        Token otherlv_6=null;
        Token otherlv_8=null;
        Token otherlv_10=null;
        Token otherlv_11=null;
        Token otherlv_13=null;
        Token otherlv_15=null;
        Token otherlv_17=null;
        EObject lv_assignment_1_0 = null;

        AntlrDatatypeRuleToken lv_name_7_0 = null;

        EObject lv_exp_12_0 = null;

        AntlrDatatypeRuleToken lv_message_14_0 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:2256:2: ( ( ( () ( (lv_assignment_1_0= ruleAssignmentRule ) ) (otherlv_2= ',' | otherlv_3= ';' ) ) | ( () otherlv_5= 'ENTRY' otherlv_6= '(' ( (lv_name_7_0= ruleValidID ) ) otherlv_8= ')' ) | ( () otherlv_10= 'ASSERT' otherlv_11= '(' ( (lv_exp_12_0= ruleLExpression ) ) otherlv_13= ',' ( (lv_message_14_0= ruleValidID ) ) otherlv_15= ')' ) | ( () otherlv_17= ';' ) ) )
            // InternalLinkerScript.g:2257:2: ( ( () ( (lv_assignment_1_0= ruleAssignmentRule ) ) (otherlv_2= ',' | otherlv_3= ';' ) ) | ( () otherlv_5= 'ENTRY' otherlv_6= '(' ( (lv_name_7_0= ruleValidID ) ) otherlv_8= ')' ) | ( () otherlv_10= 'ASSERT' otherlv_11= '(' ( (lv_exp_12_0= ruleLExpression ) ) otherlv_13= ',' ( (lv_message_14_0= ruleValidID ) ) otherlv_15= ')' ) | ( () otherlv_17= ';' ) )
            {
            // InternalLinkerScript.g:2257:2: ( ( () ( (lv_assignment_1_0= ruleAssignmentRule ) ) (otherlv_2= ',' | otherlv_3= ';' ) ) | ( () otherlv_5= 'ENTRY' otherlv_6= '(' ( (lv_name_7_0= ruleValidID ) ) otherlv_8= ')' ) | ( () otherlv_10= 'ASSERT' otherlv_11= '(' ( (lv_exp_12_0= ruleLExpression ) ) otherlv_13= ',' ( (lv_message_14_0= ruleValidID ) ) otherlv_15= ')' ) | ( () otherlv_17= ';' ) )
            int alt37=4;
            switch ( input.LA(1) ) {
            case RULE_ID:
            case 61:
            case 62:
            case 63:
            case 80:
            case 82:
            case 83:
            case 85:
            case 86:
            case 96:
                {
                alt37=1;
                }
                break;
            case 15:
                {
                alt37=2;
                }
                break;
            case 16:
                {
                alt37=3;
                }
                break;
            case 11:
                {
                alt37=4;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 37, 0, input);

                throw nvae;
            }

            switch (alt37) {
                case 1 :
                    // InternalLinkerScript.g:2258:3: ( () ( (lv_assignment_1_0= ruleAssignmentRule ) ) (otherlv_2= ',' | otherlv_3= ';' ) )
                    {
                    // InternalLinkerScript.g:2258:3: ( () ( (lv_assignment_1_0= ruleAssignmentRule ) ) (otherlv_2= ',' | otherlv_3= ';' ) )
                    // InternalLinkerScript.g:2259:4: () ( (lv_assignment_1_0= ruleAssignmentRule ) ) (otherlv_2= ',' | otherlv_3= ';' )
                    {
                    // InternalLinkerScript.g:2259:4: ()
                    // InternalLinkerScript.g:2260:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getStatementAnywhereAccess().getStatementAssignmentAction_0_0(),
                      						current);
                      				
                    }

                    }

                    // InternalLinkerScript.g:2266:4: ( (lv_assignment_1_0= ruleAssignmentRule ) )
                    // InternalLinkerScript.g:2267:5: (lv_assignment_1_0= ruleAssignmentRule )
                    {
                    // InternalLinkerScript.g:2267:5: (lv_assignment_1_0= ruleAssignmentRule )
                    // InternalLinkerScript.g:2268:6: lv_assignment_1_0= ruleAssignmentRule
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getStatementAnywhereAccess().getAssignmentAssignmentRuleParserRuleCall_0_1_0());
                      					
                    }
                    pushFollow(FOLLOW_4);
                    lv_assignment_1_0=ruleAssignmentRule();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getStatementAnywhereRule());
                      						}
                      						set(
                      							current,
                      							"assignment",
                      							lv_assignment_1_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.AssignmentRule");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    // InternalLinkerScript.g:2285:4: (otherlv_2= ',' | otherlv_3= ';' )
                    int alt36=2;
                    int LA36_0 = input.LA(1);

                    if ( (LA36_0==10) ) {
                        alt36=1;
                    }
                    else if ( (LA36_0==11) ) {
                        alt36=2;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return current;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 36, 0, input);

                        throw nvae;
                    }
                    switch (alt36) {
                        case 1 :
                            // InternalLinkerScript.g:2286:5: otherlv_2= ','
                            {
                            otherlv_2=(Token)match(input,10,FOLLOW_2); if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              					newLeafNode(otherlv_2, grammarAccess.getStatementAnywhereAccess().getCommaKeyword_0_2_0());
                              				
                            }

                            }
                            break;
                        case 2 :
                            // InternalLinkerScript.g:2291:5: otherlv_3= ';'
                            {
                            otherlv_3=(Token)match(input,11,FOLLOW_2); if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              					newLeafNode(otherlv_3, grammarAccess.getStatementAnywhereAccess().getSemicolonKeyword_0_2_1());
                              				
                            }

                            }
                            break;

                    }


                    }


                    }
                    break;
                case 2 :
                    // InternalLinkerScript.g:2298:3: ( () otherlv_5= 'ENTRY' otherlv_6= '(' ( (lv_name_7_0= ruleValidID ) ) otherlv_8= ')' )
                    {
                    // InternalLinkerScript.g:2298:3: ( () otherlv_5= 'ENTRY' otherlv_6= '(' ( (lv_name_7_0= ruleValidID ) ) otherlv_8= ')' )
                    // InternalLinkerScript.g:2299:4: () otherlv_5= 'ENTRY' otherlv_6= '(' ( (lv_name_7_0= ruleValidID ) ) otherlv_8= ')'
                    {
                    // InternalLinkerScript.g:2299:4: ()
                    // InternalLinkerScript.g:2300:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getStatementAnywhereAccess().getStatementEntryAction_1_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_5=(Token)match(input,15,FOLLOW_5); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_5, grammarAccess.getStatementAnywhereAccess().getENTRYKeyword_1_1());
                      			
                    }
                    otherlv_6=(Token)match(input,13,FOLLOW_6); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_6, grammarAccess.getStatementAnywhereAccess().getLeftParenthesisKeyword_1_2());
                      			
                    }
                    // InternalLinkerScript.g:2314:4: ( (lv_name_7_0= ruleValidID ) )
                    // InternalLinkerScript.g:2315:5: (lv_name_7_0= ruleValidID )
                    {
                    // InternalLinkerScript.g:2315:5: (lv_name_7_0= ruleValidID )
                    // InternalLinkerScript.g:2316:6: lv_name_7_0= ruleValidID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getStatementAnywhereAccess().getNameValidIDParserRuleCall_1_3_0());
                      					
                    }
                    pushFollow(FOLLOW_7);
                    lv_name_7_0=ruleValidID();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getStatementAnywhereRule());
                      						}
                      						set(
                      							current,
                      							"name",
                      							lv_name_7_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.ValidID");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    otherlv_8=(Token)match(input,14,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_8, grammarAccess.getStatementAnywhereAccess().getRightParenthesisKeyword_1_4());
                      			
                    }

                    }


                    }
                    break;
                case 3 :
                    // InternalLinkerScript.g:2339:3: ( () otherlv_10= 'ASSERT' otherlv_11= '(' ( (lv_exp_12_0= ruleLExpression ) ) otherlv_13= ',' ( (lv_message_14_0= ruleValidID ) ) otherlv_15= ')' )
                    {
                    // InternalLinkerScript.g:2339:3: ( () otherlv_10= 'ASSERT' otherlv_11= '(' ( (lv_exp_12_0= ruleLExpression ) ) otherlv_13= ',' ( (lv_message_14_0= ruleValidID ) ) otherlv_15= ')' )
                    // InternalLinkerScript.g:2340:4: () otherlv_10= 'ASSERT' otherlv_11= '(' ( (lv_exp_12_0= ruleLExpression ) ) otherlv_13= ',' ( (lv_message_14_0= ruleValidID ) ) otherlv_15= ')'
                    {
                    // InternalLinkerScript.g:2340:4: ()
                    // InternalLinkerScript.g:2341:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getStatementAnywhereAccess().getStatementAssertAction_2_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_10=(Token)match(input,16,FOLLOW_5); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_10, grammarAccess.getStatementAnywhereAccess().getASSERTKeyword_2_1());
                      			
                    }
                    otherlv_11=(Token)match(input,13,FOLLOW_8); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_11, grammarAccess.getStatementAnywhereAccess().getLeftParenthesisKeyword_2_2());
                      			
                    }
                    // InternalLinkerScript.g:2355:4: ( (lv_exp_12_0= ruleLExpression ) )
                    // InternalLinkerScript.g:2356:5: (lv_exp_12_0= ruleLExpression )
                    {
                    // InternalLinkerScript.g:2356:5: (lv_exp_12_0= ruleLExpression )
                    // InternalLinkerScript.g:2357:6: lv_exp_12_0= ruleLExpression
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getStatementAnywhereAccess().getExpLExpressionParserRuleCall_2_3_0());
                      					
                    }
                    pushFollow(FOLLOW_9);
                    lv_exp_12_0=ruleLExpression();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getStatementAnywhereRule());
                      						}
                      						set(
                      							current,
                      							"exp",
                      							lv_exp_12_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.LExpression");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    otherlv_13=(Token)match(input,10,FOLLOW_6); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_13, grammarAccess.getStatementAnywhereAccess().getCommaKeyword_2_4());
                      			
                    }
                    // InternalLinkerScript.g:2378:4: ( (lv_message_14_0= ruleValidID ) )
                    // InternalLinkerScript.g:2379:5: (lv_message_14_0= ruleValidID )
                    {
                    // InternalLinkerScript.g:2379:5: (lv_message_14_0= ruleValidID )
                    // InternalLinkerScript.g:2380:6: lv_message_14_0= ruleValidID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getStatementAnywhereAccess().getMessageValidIDParserRuleCall_2_5_0());
                      					
                    }
                    pushFollow(FOLLOW_7);
                    lv_message_14_0=ruleValidID();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getStatementAnywhereRule());
                      						}
                      						set(
                      							current,
                      							"message",
                      							lv_message_14_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.ValidID");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    otherlv_15=(Token)match(input,14,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_15, grammarAccess.getStatementAnywhereAccess().getRightParenthesisKeyword_2_6());
                      			
                    }

                    }


                    }
                    break;
                case 4 :
                    // InternalLinkerScript.g:2403:3: ( () otherlv_17= ';' )
                    {
                    // InternalLinkerScript.g:2403:3: ( () otherlv_17= ';' )
                    // InternalLinkerScript.g:2404:4: () otherlv_17= ';'
                    {
                    // InternalLinkerScript.g:2404:4: ()
                    // InternalLinkerScript.g:2405:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getStatementAnywhereAccess().getStatementNopAction_3_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_17=(Token)match(input,11,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_17, grammarAccess.getStatementAnywhereAccess().getSemicolonKeyword_3_1());
                      			
                    }

                    }


                    }
                    break;

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleStatementAnywhere"


    // $ANTLR start "entryRuleStatementDataSize"
    // InternalLinkerScript.g:2420:1: entryRuleStatementDataSize returns [String current=null] : iv_ruleStatementDataSize= ruleStatementDataSize EOF ;
    public final String entryRuleStatementDataSize() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleStatementDataSize = null;


        try {
            // InternalLinkerScript.g:2420:57: (iv_ruleStatementDataSize= ruleStatementDataSize EOF )
            // InternalLinkerScript.g:2421:2: iv_ruleStatementDataSize= ruleStatementDataSize EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getStatementDataSizeRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleStatementDataSize=ruleStatementDataSize();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleStatementDataSize.getText(); 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleStatementDataSize"


    // $ANTLR start "ruleStatementDataSize"
    // InternalLinkerScript.g:2427:1: ruleStatementDataSize returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (kw= 'BYTE' | kw= 'SHORT' | kw= 'LONG' | kw= 'QUAD' | kw= 'SQUAD' ) ;
    public final AntlrDatatypeRuleToken ruleStatementDataSize() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;


        	enterRule();

        try {
            // InternalLinkerScript.g:2433:2: ( (kw= 'BYTE' | kw= 'SHORT' | kw= 'LONG' | kw= 'QUAD' | kw= 'SQUAD' ) )
            // InternalLinkerScript.g:2434:2: (kw= 'BYTE' | kw= 'SHORT' | kw= 'LONG' | kw= 'QUAD' | kw= 'SQUAD' )
            {
            // InternalLinkerScript.g:2434:2: (kw= 'BYTE' | kw= 'SHORT' | kw= 'LONG' | kw= 'QUAD' | kw= 'SQUAD' )
            int alt38=5;
            switch ( input.LA(1) ) {
            case 56:
                {
                alt38=1;
                }
                break;
            case 57:
                {
                alt38=2;
                }
                break;
            case 58:
                {
                alt38=3;
                }
                break;
            case 59:
                {
                alt38=4;
                }
                break;
            case 60:
                {
                alt38=5;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 38, 0, input);

                throw nvae;
            }

            switch (alt38) {
                case 1 :
                    // InternalLinkerScript.g:2435:3: kw= 'BYTE'
                    {
                    kw=(Token)match(input,56,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getStatementDataSizeAccess().getBYTEKeyword_0());
                      		
                    }

                    }
                    break;
                case 2 :
                    // InternalLinkerScript.g:2441:3: kw= 'SHORT'
                    {
                    kw=(Token)match(input,57,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getStatementDataSizeAccess().getSHORTKeyword_1());
                      		
                    }

                    }
                    break;
                case 3 :
                    // InternalLinkerScript.g:2447:3: kw= 'LONG'
                    {
                    kw=(Token)match(input,58,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getStatementDataSizeAccess().getLONGKeyword_2());
                      		
                    }

                    }
                    break;
                case 4 :
                    // InternalLinkerScript.g:2453:3: kw= 'QUAD'
                    {
                    kw=(Token)match(input,59,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getStatementDataSizeAccess().getQUADKeyword_3());
                      		
                    }

                    }
                    break;
                case 5 :
                    // InternalLinkerScript.g:2459:3: kw= 'SQUAD'
                    {
                    kw=(Token)match(input,60,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getStatementDataSizeAccess().getSQUADKeyword_4());
                      		
                    }

                    }
                    break;

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleStatementDataSize"


    // $ANTLR start "entryRuleAssignmentRule"
    // InternalLinkerScript.g:2468:1: entryRuleAssignmentRule returns [EObject current=null] : iv_ruleAssignmentRule= ruleAssignmentRule EOF ;
    public final EObject entryRuleAssignmentRule() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleAssignmentRule = null;


        try {
            // InternalLinkerScript.g:2468:55: (iv_ruleAssignmentRule= ruleAssignmentRule EOF )
            // InternalLinkerScript.g:2469:2: iv_ruleAssignmentRule= ruleAssignmentRule EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getAssignmentRuleRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleAssignmentRule=ruleAssignmentRule();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleAssignmentRule; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleAssignmentRule"


    // $ANTLR start "ruleAssignmentRule"
    // InternalLinkerScript.g:2475:1: ruleAssignmentRule returns [EObject current=null] : ( ( () ( (lv_name_1_0= ruleWildID ) ) ( (lv_feature_2_0= ruleOpAssign ) ) ( (lv_exp_3_0= ruleLExpression ) ) ) | ( () otherlv_5= 'HIDDEN' otherlv_6= '(' ( (lv_name_7_0= ruleWildID ) ) ( (lv_feature_8_0= '=' ) ) ( (lv_exp_9_0= ruleLExpression ) ) otherlv_10= ')' ) | ( () otherlv_12= 'PROVIDE' otherlv_13= '(' ( (lv_name_14_0= ruleWildID ) ) ( (lv_feature_15_0= '=' ) ) ( (lv_exp_16_0= ruleLExpression ) ) otherlv_17= ')' ) | ( () otherlv_19= 'PROVIDE_HIDDEN' otherlv_20= '(' ( (lv_name_21_0= ruleWildID ) ) ( (lv_feature_22_0= '=' ) ) ( (lv_exp_23_0= ruleLExpression ) ) otherlv_24= ')' ) ) ;
    public final EObject ruleAssignmentRule() throws RecognitionException {
        EObject current = null;

        Token otherlv_5=null;
        Token otherlv_6=null;
        Token lv_feature_8_0=null;
        Token otherlv_10=null;
        Token otherlv_12=null;
        Token otherlv_13=null;
        Token lv_feature_15_0=null;
        Token otherlv_17=null;
        Token otherlv_19=null;
        Token otherlv_20=null;
        Token lv_feature_22_0=null;
        Token otherlv_24=null;
        AntlrDatatypeRuleToken lv_name_1_0 = null;

        AntlrDatatypeRuleToken lv_feature_2_0 = null;

        EObject lv_exp_3_0 = null;

        AntlrDatatypeRuleToken lv_name_7_0 = null;

        EObject lv_exp_9_0 = null;

        AntlrDatatypeRuleToken lv_name_14_0 = null;

        EObject lv_exp_16_0 = null;

        AntlrDatatypeRuleToken lv_name_21_0 = null;

        EObject lv_exp_23_0 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:2481:2: ( ( ( () ( (lv_name_1_0= ruleWildID ) ) ( (lv_feature_2_0= ruleOpAssign ) ) ( (lv_exp_3_0= ruleLExpression ) ) ) | ( () otherlv_5= 'HIDDEN' otherlv_6= '(' ( (lv_name_7_0= ruleWildID ) ) ( (lv_feature_8_0= '=' ) ) ( (lv_exp_9_0= ruleLExpression ) ) otherlv_10= ')' ) | ( () otherlv_12= 'PROVIDE' otherlv_13= '(' ( (lv_name_14_0= ruleWildID ) ) ( (lv_feature_15_0= '=' ) ) ( (lv_exp_16_0= ruleLExpression ) ) otherlv_17= ')' ) | ( () otherlv_19= 'PROVIDE_HIDDEN' otherlv_20= '(' ( (lv_name_21_0= ruleWildID ) ) ( (lv_feature_22_0= '=' ) ) ( (lv_exp_23_0= ruleLExpression ) ) otherlv_24= ')' ) ) )
            // InternalLinkerScript.g:2482:2: ( ( () ( (lv_name_1_0= ruleWildID ) ) ( (lv_feature_2_0= ruleOpAssign ) ) ( (lv_exp_3_0= ruleLExpression ) ) ) | ( () otherlv_5= 'HIDDEN' otherlv_6= '(' ( (lv_name_7_0= ruleWildID ) ) ( (lv_feature_8_0= '=' ) ) ( (lv_exp_9_0= ruleLExpression ) ) otherlv_10= ')' ) | ( () otherlv_12= 'PROVIDE' otherlv_13= '(' ( (lv_name_14_0= ruleWildID ) ) ( (lv_feature_15_0= '=' ) ) ( (lv_exp_16_0= ruleLExpression ) ) otherlv_17= ')' ) | ( () otherlv_19= 'PROVIDE_HIDDEN' otherlv_20= '(' ( (lv_name_21_0= ruleWildID ) ) ( (lv_feature_22_0= '=' ) ) ( (lv_exp_23_0= ruleLExpression ) ) otherlv_24= ')' ) )
            {
            // InternalLinkerScript.g:2482:2: ( ( () ( (lv_name_1_0= ruleWildID ) ) ( (lv_feature_2_0= ruleOpAssign ) ) ( (lv_exp_3_0= ruleLExpression ) ) ) | ( () otherlv_5= 'HIDDEN' otherlv_6= '(' ( (lv_name_7_0= ruleWildID ) ) ( (lv_feature_8_0= '=' ) ) ( (lv_exp_9_0= ruleLExpression ) ) otherlv_10= ')' ) | ( () otherlv_12= 'PROVIDE' otherlv_13= '(' ( (lv_name_14_0= ruleWildID ) ) ( (lv_feature_15_0= '=' ) ) ( (lv_exp_16_0= ruleLExpression ) ) otherlv_17= ')' ) | ( () otherlv_19= 'PROVIDE_HIDDEN' otherlv_20= '(' ( (lv_name_21_0= ruleWildID ) ) ( (lv_feature_22_0= '=' ) ) ( (lv_exp_23_0= ruleLExpression ) ) otherlv_24= ')' ) )
            int alt39=4;
            switch ( input.LA(1) ) {
            case RULE_ID:
            case 80:
            case 82:
            case 83:
            case 85:
            case 86:
            case 96:
                {
                alt39=1;
                }
                break;
            case 61:
                {
                alt39=2;
                }
                break;
            case 62:
                {
                alt39=3;
                }
                break;
            case 63:
                {
                alt39=4;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 39, 0, input);

                throw nvae;
            }

            switch (alt39) {
                case 1 :
                    // InternalLinkerScript.g:2483:3: ( () ( (lv_name_1_0= ruleWildID ) ) ( (lv_feature_2_0= ruleOpAssign ) ) ( (lv_exp_3_0= ruleLExpression ) ) )
                    {
                    // InternalLinkerScript.g:2483:3: ( () ( (lv_name_1_0= ruleWildID ) ) ( (lv_feature_2_0= ruleOpAssign ) ) ( (lv_exp_3_0= ruleLExpression ) ) )
                    // InternalLinkerScript.g:2484:4: () ( (lv_name_1_0= ruleWildID ) ) ( (lv_feature_2_0= ruleOpAssign ) ) ( (lv_exp_3_0= ruleLExpression ) )
                    {
                    // InternalLinkerScript.g:2484:4: ()
                    // InternalLinkerScript.g:2485:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getAssignmentRuleAccess().getAssignmentAction_0_0(),
                      						current);
                      				
                    }

                    }

                    // InternalLinkerScript.g:2491:4: ( (lv_name_1_0= ruleWildID ) )
                    // InternalLinkerScript.g:2492:5: (lv_name_1_0= ruleWildID )
                    {
                    // InternalLinkerScript.g:2492:5: (lv_name_1_0= ruleWildID )
                    // InternalLinkerScript.g:2493:6: lv_name_1_0= ruleWildID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getAssignmentRuleAccess().getNameWildIDParserRuleCall_0_1_0());
                      					
                    }
                    pushFollow(FOLLOW_35);
                    lv_name_1_0=ruleWildID();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getAssignmentRuleRule());
                      						}
                      						set(
                      							current,
                      							"name",
                      							lv_name_1_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.WildID");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    // InternalLinkerScript.g:2510:4: ( (lv_feature_2_0= ruleOpAssign ) )
                    // InternalLinkerScript.g:2511:5: (lv_feature_2_0= ruleOpAssign )
                    {
                    // InternalLinkerScript.g:2511:5: (lv_feature_2_0= ruleOpAssign )
                    // InternalLinkerScript.g:2512:6: lv_feature_2_0= ruleOpAssign
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getAssignmentRuleAccess().getFeatureOpAssignParserRuleCall_0_2_0());
                      					
                    }
                    pushFollow(FOLLOW_8);
                    lv_feature_2_0=ruleOpAssign();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getAssignmentRuleRule());
                      						}
                      						set(
                      							current,
                      							"feature",
                      							lv_feature_2_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.OpAssign");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    // InternalLinkerScript.g:2529:4: ( (lv_exp_3_0= ruleLExpression ) )
                    // InternalLinkerScript.g:2530:5: (lv_exp_3_0= ruleLExpression )
                    {
                    // InternalLinkerScript.g:2530:5: (lv_exp_3_0= ruleLExpression )
                    // InternalLinkerScript.g:2531:6: lv_exp_3_0= ruleLExpression
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getAssignmentRuleAccess().getExpLExpressionParserRuleCall_0_3_0());
                      					
                    }
                    pushFollow(FOLLOW_2);
                    lv_exp_3_0=ruleLExpression();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getAssignmentRuleRule());
                      						}
                      						set(
                      							current,
                      							"exp",
                      							lv_exp_3_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.LExpression");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }


                    }


                    }
                    break;
                case 2 :
                    // InternalLinkerScript.g:2550:3: ( () otherlv_5= 'HIDDEN' otherlv_6= '(' ( (lv_name_7_0= ruleWildID ) ) ( (lv_feature_8_0= '=' ) ) ( (lv_exp_9_0= ruleLExpression ) ) otherlv_10= ')' )
                    {
                    // InternalLinkerScript.g:2550:3: ( () otherlv_5= 'HIDDEN' otherlv_6= '(' ( (lv_name_7_0= ruleWildID ) ) ( (lv_feature_8_0= '=' ) ) ( (lv_exp_9_0= ruleLExpression ) ) otherlv_10= ')' )
                    // InternalLinkerScript.g:2551:4: () otherlv_5= 'HIDDEN' otherlv_6= '(' ( (lv_name_7_0= ruleWildID ) ) ( (lv_feature_8_0= '=' ) ) ( (lv_exp_9_0= ruleLExpression ) ) otherlv_10= ')'
                    {
                    // InternalLinkerScript.g:2551:4: ()
                    // InternalLinkerScript.g:2552:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getAssignmentRuleAccess().getAssignmentHiddenAction_1_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_5=(Token)match(input,61,FOLLOW_5); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_5, grammarAccess.getAssignmentRuleAccess().getHIDDENKeyword_1_1());
                      			
                    }
                    otherlv_6=(Token)match(input,13,FOLLOW_6); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_6, grammarAccess.getAssignmentRuleAccess().getLeftParenthesisKeyword_1_2());
                      			
                    }
                    // InternalLinkerScript.g:2566:4: ( (lv_name_7_0= ruleWildID ) )
                    // InternalLinkerScript.g:2567:5: (lv_name_7_0= ruleWildID )
                    {
                    // InternalLinkerScript.g:2567:5: (lv_name_7_0= ruleWildID )
                    // InternalLinkerScript.g:2568:6: lv_name_7_0= ruleWildID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getAssignmentRuleAccess().getNameWildIDParserRuleCall_1_3_0());
                      					
                    }
                    pushFollow(FOLLOW_36);
                    lv_name_7_0=ruleWildID();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getAssignmentRuleRule());
                      						}
                      						set(
                      							current,
                      							"name",
                      							lv_name_7_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.WildID");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    // InternalLinkerScript.g:2585:4: ( (lv_feature_8_0= '=' ) )
                    // InternalLinkerScript.g:2586:5: (lv_feature_8_0= '=' )
                    {
                    // InternalLinkerScript.g:2586:5: (lv_feature_8_0= '=' )
                    // InternalLinkerScript.g:2587:6: lv_feature_8_0= '='
                    {
                    lv_feature_8_0=(Token)match(input,41,FOLLOW_8); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						newLeafNode(lv_feature_8_0, grammarAccess.getAssignmentRuleAccess().getFeatureEqualsSignKeyword_1_4_0());
                      					
                    }
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElement(grammarAccess.getAssignmentRuleRule());
                      						}
                      						setWithLastConsumed(current, "feature", lv_feature_8_0, "=");
                      					
                    }

                    }


                    }

                    // InternalLinkerScript.g:2599:4: ( (lv_exp_9_0= ruleLExpression ) )
                    // InternalLinkerScript.g:2600:5: (lv_exp_9_0= ruleLExpression )
                    {
                    // InternalLinkerScript.g:2600:5: (lv_exp_9_0= ruleLExpression )
                    // InternalLinkerScript.g:2601:6: lv_exp_9_0= ruleLExpression
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getAssignmentRuleAccess().getExpLExpressionParserRuleCall_1_5_0());
                      					
                    }
                    pushFollow(FOLLOW_7);
                    lv_exp_9_0=ruleLExpression();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getAssignmentRuleRule());
                      						}
                      						set(
                      							current,
                      							"exp",
                      							lv_exp_9_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.LExpression");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    otherlv_10=(Token)match(input,14,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_10, grammarAccess.getAssignmentRuleAccess().getRightParenthesisKeyword_1_6());
                      			
                    }

                    }


                    }
                    break;
                case 3 :
                    // InternalLinkerScript.g:2624:3: ( () otherlv_12= 'PROVIDE' otherlv_13= '(' ( (lv_name_14_0= ruleWildID ) ) ( (lv_feature_15_0= '=' ) ) ( (lv_exp_16_0= ruleLExpression ) ) otherlv_17= ')' )
                    {
                    // InternalLinkerScript.g:2624:3: ( () otherlv_12= 'PROVIDE' otherlv_13= '(' ( (lv_name_14_0= ruleWildID ) ) ( (lv_feature_15_0= '=' ) ) ( (lv_exp_16_0= ruleLExpression ) ) otherlv_17= ')' )
                    // InternalLinkerScript.g:2625:4: () otherlv_12= 'PROVIDE' otherlv_13= '(' ( (lv_name_14_0= ruleWildID ) ) ( (lv_feature_15_0= '=' ) ) ( (lv_exp_16_0= ruleLExpression ) ) otherlv_17= ')'
                    {
                    // InternalLinkerScript.g:2625:4: ()
                    // InternalLinkerScript.g:2626:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getAssignmentRuleAccess().getAssignmentProvideAction_2_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_12=(Token)match(input,62,FOLLOW_5); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_12, grammarAccess.getAssignmentRuleAccess().getPROVIDEKeyword_2_1());
                      			
                    }
                    otherlv_13=(Token)match(input,13,FOLLOW_6); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_13, grammarAccess.getAssignmentRuleAccess().getLeftParenthesisKeyword_2_2());
                      			
                    }
                    // InternalLinkerScript.g:2640:4: ( (lv_name_14_0= ruleWildID ) )
                    // InternalLinkerScript.g:2641:5: (lv_name_14_0= ruleWildID )
                    {
                    // InternalLinkerScript.g:2641:5: (lv_name_14_0= ruleWildID )
                    // InternalLinkerScript.g:2642:6: lv_name_14_0= ruleWildID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getAssignmentRuleAccess().getNameWildIDParserRuleCall_2_3_0());
                      					
                    }
                    pushFollow(FOLLOW_36);
                    lv_name_14_0=ruleWildID();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getAssignmentRuleRule());
                      						}
                      						set(
                      							current,
                      							"name",
                      							lv_name_14_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.WildID");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    // InternalLinkerScript.g:2659:4: ( (lv_feature_15_0= '=' ) )
                    // InternalLinkerScript.g:2660:5: (lv_feature_15_0= '=' )
                    {
                    // InternalLinkerScript.g:2660:5: (lv_feature_15_0= '=' )
                    // InternalLinkerScript.g:2661:6: lv_feature_15_0= '='
                    {
                    lv_feature_15_0=(Token)match(input,41,FOLLOW_8); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						newLeafNode(lv_feature_15_0, grammarAccess.getAssignmentRuleAccess().getFeatureEqualsSignKeyword_2_4_0());
                      					
                    }
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElement(grammarAccess.getAssignmentRuleRule());
                      						}
                      						setWithLastConsumed(current, "feature", lv_feature_15_0, "=");
                      					
                    }

                    }


                    }

                    // InternalLinkerScript.g:2673:4: ( (lv_exp_16_0= ruleLExpression ) )
                    // InternalLinkerScript.g:2674:5: (lv_exp_16_0= ruleLExpression )
                    {
                    // InternalLinkerScript.g:2674:5: (lv_exp_16_0= ruleLExpression )
                    // InternalLinkerScript.g:2675:6: lv_exp_16_0= ruleLExpression
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getAssignmentRuleAccess().getExpLExpressionParserRuleCall_2_5_0());
                      					
                    }
                    pushFollow(FOLLOW_7);
                    lv_exp_16_0=ruleLExpression();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getAssignmentRuleRule());
                      						}
                      						set(
                      							current,
                      							"exp",
                      							lv_exp_16_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.LExpression");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    otherlv_17=(Token)match(input,14,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_17, grammarAccess.getAssignmentRuleAccess().getRightParenthesisKeyword_2_6());
                      			
                    }

                    }


                    }
                    break;
                case 4 :
                    // InternalLinkerScript.g:2698:3: ( () otherlv_19= 'PROVIDE_HIDDEN' otherlv_20= '(' ( (lv_name_21_0= ruleWildID ) ) ( (lv_feature_22_0= '=' ) ) ( (lv_exp_23_0= ruleLExpression ) ) otherlv_24= ')' )
                    {
                    // InternalLinkerScript.g:2698:3: ( () otherlv_19= 'PROVIDE_HIDDEN' otherlv_20= '(' ( (lv_name_21_0= ruleWildID ) ) ( (lv_feature_22_0= '=' ) ) ( (lv_exp_23_0= ruleLExpression ) ) otherlv_24= ')' )
                    // InternalLinkerScript.g:2699:4: () otherlv_19= 'PROVIDE_HIDDEN' otherlv_20= '(' ( (lv_name_21_0= ruleWildID ) ) ( (lv_feature_22_0= '=' ) ) ( (lv_exp_23_0= ruleLExpression ) ) otherlv_24= ')'
                    {
                    // InternalLinkerScript.g:2699:4: ()
                    // InternalLinkerScript.g:2700:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getAssignmentRuleAccess().getAssignmentProvideHiddenAction_3_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_19=(Token)match(input,63,FOLLOW_5); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_19, grammarAccess.getAssignmentRuleAccess().getPROVIDE_HIDDENKeyword_3_1());
                      			
                    }
                    otherlv_20=(Token)match(input,13,FOLLOW_6); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_20, grammarAccess.getAssignmentRuleAccess().getLeftParenthesisKeyword_3_2());
                      			
                    }
                    // InternalLinkerScript.g:2714:4: ( (lv_name_21_0= ruleWildID ) )
                    // InternalLinkerScript.g:2715:5: (lv_name_21_0= ruleWildID )
                    {
                    // InternalLinkerScript.g:2715:5: (lv_name_21_0= ruleWildID )
                    // InternalLinkerScript.g:2716:6: lv_name_21_0= ruleWildID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getAssignmentRuleAccess().getNameWildIDParserRuleCall_3_3_0());
                      					
                    }
                    pushFollow(FOLLOW_36);
                    lv_name_21_0=ruleWildID();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getAssignmentRuleRule());
                      						}
                      						set(
                      							current,
                      							"name",
                      							lv_name_21_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.WildID");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    // InternalLinkerScript.g:2733:4: ( (lv_feature_22_0= '=' ) )
                    // InternalLinkerScript.g:2734:5: (lv_feature_22_0= '=' )
                    {
                    // InternalLinkerScript.g:2734:5: (lv_feature_22_0= '=' )
                    // InternalLinkerScript.g:2735:6: lv_feature_22_0= '='
                    {
                    lv_feature_22_0=(Token)match(input,41,FOLLOW_8); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						newLeafNode(lv_feature_22_0, grammarAccess.getAssignmentRuleAccess().getFeatureEqualsSignKeyword_3_4_0());
                      					
                    }
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElement(grammarAccess.getAssignmentRuleRule());
                      						}
                      						setWithLastConsumed(current, "feature", lv_feature_22_0, "=");
                      					
                    }

                    }


                    }

                    // InternalLinkerScript.g:2747:4: ( (lv_exp_23_0= ruleLExpression ) )
                    // InternalLinkerScript.g:2748:5: (lv_exp_23_0= ruleLExpression )
                    {
                    // InternalLinkerScript.g:2748:5: (lv_exp_23_0= ruleLExpression )
                    // InternalLinkerScript.g:2749:6: lv_exp_23_0= ruleLExpression
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getAssignmentRuleAccess().getExpLExpressionParserRuleCall_3_5_0());
                      					
                    }
                    pushFollow(FOLLOW_7);
                    lv_exp_23_0=ruleLExpression();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getAssignmentRuleRule());
                      						}
                      						set(
                      							current,
                      							"exp",
                      							lv_exp_23_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.LExpression");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    otherlv_24=(Token)match(input,14,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_24, grammarAccess.getAssignmentRuleAccess().getRightParenthesisKeyword_3_6());
                      			
                    }

                    }


                    }
                    break;

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleAssignmentRule"


    // $ANTLR start "entryRuleOpAssign"
    // InternalLinkerScript.g:2775:1: entryRuleOpAssign returns [String current=null] : iv_ruleOpAssign= ruleOpAssign EOF ;
    public final String entryRuleOpAssign() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleOpAssign = null;


        try {
            // InternalLinkerScript.g:2775:48: (iv_ruleOpAssign= ruleOpAssign EOF )
            // InternalLinkerScript.g:2776:2: iv_ruleOpAssign= ruleOpAssign EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getOpAssignRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleOpAssign=ruleOpAssign();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleOpAssign.getText(); 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleOpAssign"


    // $ANTLR start "ruleOpAssign"
    // InternalLinkerScript.g:2782:1: ruleOpAssign returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (kw= '=' | kw= '+=' | kw= '-=' | kw= '*=' | kw= '/=' | (kw= '<' kw= '<' kw= '=' ) | (kw= '>' kw= '>=' ) | kw= '&=' | kw= '|=' ) ;
    public final AntlrDatatypeRuleToken ruleOpAssign() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;


        	enterRule();

        try {
            // InternalLinkerScript.g:2788:2: ( (kw= '=' | kw= '+=' | kw= '-=' | kw= '*=' | kw= '/=' | (kw= '<' kw= '<' kw= '=' ) | (kw= '>' kw= '>=' ) | kw= '&=' | kw= '|=' ) )
            // InternalLinkerScript.g:2789:2: (kw= '=' | kw= '+=' | kw= '-=' | kw= '*=' | kw= '/=' | (kw= '<' kw= '<' kw= '=' ) | (kw= '>' kw= '>=' ) | kw= '&=' | kw= '|=' )
            {
            // InternalLinkerScript.g:2789:2: (kw= '=' | kw= '+=' | kw= '-=' | kw= '*=' | kw= '/=' | (kw= '<' kw= '<' kw= '=' ) | (kw= '>' kw= '>=' ) | kw= '&=' | kw= '|=' )
            int alt40=9;
            switch ( input.LA(1) ) {
            case 41:
                {
                alt40=1;
                }
                break;
            case 64:
                {
                alt40=2;
                }
                break;
            case 65:
                {
                alt40=3;
                }
                break;
            case 66:
                {
                alt40=4;
                }
                break;
            case 67:
                {
                alt40=5;
                }
                break;
            case 68:
                {
                alt40=6;
                }
                break;
            case 40:
                {
                alt40=7;
                }
                break;
            case 70:
                {
                alt40=8;
                }
                break;
            case 71:
                {
                alt40=9;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 40, 0, input);

                throw nvae;
            }

            switch (alt40) {
                case 1 :
                    // InternalLinkerScript.g:2790:3: kw= '='
                    {
                    kw=(Token)match(input,41,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getOpAssignAccess().getEqualsSignKeyword_0());
                      		
                    }

                    }
                    break;
                case 2 :
                    // InternalLinkerScript.g:2796:3: kw= '+='
                    {
                    kw=(Token)match(input,64,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getOpAssignAccess().getPlusSignEqualsSignKeyword_1());
                      		
                    }

                    }
                    break;
                case 3 :
                    // InternalLinkerScript.g:2802:3: kw= '-='
                    {
                    kw=(Token)match(input,65,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getOpAssignAccess().getHyphenMinusEqualsSignKeyword_2());
                      		
                    }

                    }
                    break;
                case 4 :
                    // InternalLinkerScript.g:2808:3: kw= '*='
                    {
                    kw=(Token)match(input,66,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getOpAssignAccess().getAsteriskEqualsSignKeyword_3());
                      		
                    }

                    }
                    break;
                case 5 :
                    // InternalLinkerScript.g:2814:3: kw= '/='
                    {
                    kw=(Token)match(input,67,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getOpAssignAccess().getSolidusEqualsSignKeyword_4());
                      		
                    }

                    }
                    break;
                case 6 :
                    // InternalLinkerScript.g:2820:3: (kw= '<' kw= '<' kw= '=' )
                    {
                    // InternalLinkerScript.g:2820:3: (kw= '<' kw= '<' kw= '=' )
                    // InternalLinkerScript.g:2821:4: kw= '<' kw= '<' kw= '='
                    {
                    kw=(Token)match(input,68,FOLLOW_37); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				current.merge(kw);
                      				newLeafNode(kw, grammarAccess.getOpAssignAccess().getLessThanSignKeyword_5_0());
                      			
                    }
                    kw=(Token)match(input,68,FOLLOW_36); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				current.merge(kw);
                      				newLeafNode(kw, grammarAccess.getOpAssignAccess().getLessThanSignKeyword_5_1());
                      			
                    }
                    kw=(Token)match(input,41,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				current.merge(kw);
                      				newLeafNode(kw, grammarAccess.getOpAssignAccess().getEqualsSignKeyword_5_2());
                      			
                    }

                    }


                    }
                    break;
                case 7 :
                    // InternalLinkerScript.g:2838:3: (kw= '>' kw= '>=' )
                    {
                    // InternalLinkerScript.g:2838:3: (kw= '>' kw= '>=' )
                    // InternalLinkerScript.g:2839:4: kw= '>' kw= '>='
                    {
                    kw=(Token)match(input,40,FOLLOW_38); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				current.merge(kw);
                      				newLeafNode(kw, grammarAccess.getOpAssignAccess().getGreaterThanSignKeyword_6_0());
                      			
                    }
                    kw=(Token)match(input,69,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				current.merge(kw);
                      				newLeafNode(kw, grammarAccess.getOpAssignAccess().getGreaterThanSignEqualsSignKeyword_6_1());
                      			
                    }

                    }


                    }
                    break;
                case 8 :
                    // InternalLinkerScript.g:2851:3: kw= '&='
                    {
                    kw=(Token)match(input,70,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getOpAssignAccess().getAmpersandEqualsSignKeyword_7());
                      		
                    }

                    }
                    break;
                case 9 :
                    // InternalLinkerScript.g:2857:3: kw= '|='
                    {
                    kw=(Token)match(input,71,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getOpAssignAccess().getVerticalLineEqualsSignKeyword_8());
                      		
                    }

                    }
                    break;

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleOpAssign"


    // $ANTLR start "entryRuleInputSection"
    // InternalLinkerScript.g:2866:1: entryRuleInputSection returns [EObject current=null] : iv_ruleInputSection= ruleInputSection EOF ;
    public final EObject entryRuleInputSection() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleInputSection = null;


        try {
            // InternalLinkerScript.g:2866:53: (iv_ruleInputSection= ruleInputSection EOF )
            // InternalLinkerScript.g:2867:2: iv_ruleInputSection= ruleInputSection EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getInputSectionRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleInputSection=ruleInputSection();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleInputSection; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleInputSection"


    // $ANTLR start "ruleInputSection"
    // InternalLinkerScript.g:2873:1: ruleInputSection returns [EObject current=null] : ( ( () (otherlv_1= 'INPUT_SECTION_FLAGS' otherlv_2= '(' ( (lv_flags_3_0= ruleWildID ) ) (otherlv_4= '&' ( (lv_flags_5_0= ruleWildID ) ) )* otherlv_6= ')' )? ( (lv_file_7_0= ruleWildID ) ) ) | ( () (otherlv_9= 'INPUT_SECTION_FLAGS' otherlv_10= '(' ( (lv_flags_11_0= ruleWildID ) ) (otherlv_12= '&' ( (lv_flags_13_0= ruleWildID ) ) )* otherlv_14= ')' )? ( (lv_wildFile_15_0= ruleWildcardRule ) ) otherlv_16= '(' ( (lv_sections_17_0= ruleWildcardRule ) ) ( (otherlv_18= ',' )? ( (lv_sections_19_0= ruleWildcardRule ) ) )* otherlv_20= ')' ) | ( () ( (lv_keep_22_0= 'KEEP' ) ) otherlv_23= '(' (otherlv_24= 'INPUT_SECTION_FLAGS' otherlv_25= '(' ( (lv_flags_26_0= ruleWildID ) ) (otherlv_27= '&' ( (lv_flags_28_0= ruleWildID ) ) )* otherlv_29= ')' )? ( (lv_file_30_0= ruleWildID ) ) otherlv_31= ')' ) | ( () ( (lv_keep_33_0= 'KEEP' ) ) otherlv_34= '(' (otherlv_35= 'INPUT_SECTION_FLAGS' otherlv_36= '(' ( (lv_flags_37_0= ruleWildID ) ) (otherlv_38= '&' ( (lv_flags_39_0= ruleWildID ) ) )* otherlv_40= ')' )? ( (lv_wildFile_41_0= ruleWildcardRule ) ) otherlv_42= '(' ( (lv_sections_43_0= ruleWildcardRule ) ) ( (otherlv_44= ',' )? ( (lv_sections_45_0= ruleWildcardRule ) ) )* otherlv_46= ')' otherlv_47= ')' ) ) ;
    public final EObject ruleInputSection() throws RecognitionException {
        EObject current = null;

        Token otherlv_1=null;
        Token otherlv_2=null;
        Token otherlv_4=null;
        Token otherlv_6=null;
        Token otherlv_9=null;
        Token otherlv_10=null;
        Token otherlv_12=null;
        Token otherlv_14=null;
        Token otherlv_16=null;
        Token otherlv_18=null;
        Token otherlv_20=null;
        Token lv_keep_22_0=null;
        Token otherlv_23=null;
        Token otherlv_24=null;
        Token otherlv_25=null;
        Token otherlv_27=null;
        Token otherlv_29=null;
        Token otherlv_31=null;
        Token lv_keep_33_0=null;
        Token otherlv_34=null;
        Token otherlv_35=null;
        Token otherlv_36=null;
        Token otherlv_38=null;
        Token otherlv_40=null;
        Token otherlv_42=null;
        Token otherlv_44=null;
        Token otherlv_46=null;
        Token otherlv_47=null;
        AntlrDatatypeRuleToken lv_flags_3_0 = null;

        AntlrDatatypeRuleToken lv_flags_5_0 = null;

        AntlrDatatypeRuleToken lv_file_7_0 = null;

        AntlrDatatypeRuleToken lv_flags_11_0 = null;

        AntlrDatatypeRuleToken lv_flags_13_0 = null;

        EObject lv_wildFile_15_0 = null;

        EObject lv_sections_17_0 = null;

        EObject lv_sections_19_0 = null;

        AntlrDatatypeRuleToken lv_flags_26_0 = null;

        AntlrDatatypeRuleToken lv_flags_28_0 = null;

        AntlrDatatypeRuleToken lv_file_30_0 = null;

        AntlrDatatypeRuleToken lv_flags_37_0 = null;

        AntlrDatatypeRuleToken lv_flags_39_0 = null;

        EObject lv_wildFile_41_0 = null;

        EObject lv_sections_43_0 = null;

        EObject lv_sections_45_0 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:2879:2: ( ( ( () (otherlv_1= 'INPUT_SECTION_FLAGS' otherlv_2= '(' ( (lv_flags_3_0= ruleWildID ) ) (otherlv_4= '&' ( (lv_flags_5_0= ruleWildID ) ) )* otherlv_6= ')' )? ( (lv_file_7_0= ruleWildID ) ) ) | ( () (otherlv_9= 'INPUT_SECTION_FLAGS' otherlv_10= '(' ( (lv_flags_11_0= ruleWildID ) ) (otherlv_12= '&' ( (lv_flags_13_0= ruleWildID ) ) )* otherlv_14= ')' )? ( (lv_wildFile_15_0= ruleWildcardRule ) ) otherlv_16= '(' ( (lv_sections_17_0= ruleWildcardRule ) ) ( (otherlv_18= ',' )? ( (lv_sections_19_0= ruleWildcardRule ) ) )* otherlv_20= ')' ) | ( () ( (lv_keep_22_0= 'KEEP' ) ) otherlv_23= '(' (otherlv_24= 'INPUT_SECTION_FLAGS' otherlv_25= '(' ( (lv_flags_26_0= ruleWildID ) ) (otherlv_27= '&' ( (lv_flags_28_0= ruleWildID ) ) )* otherlv_29= ')' )? ( (lv_file_30_0= ruleWildID ) ) otherlv_31= ')' ) | ( () ( (lv_keep_33_0= 'KEEP' ) ) otherlv_34= '(' (otherlv_35= 'INPUT_SECTION_FLAGS' otherlv_36= '(' ( (lv_flags_37_0= ruleWildID ) ) (otherlv_38= '&' ( (lv_flags_39_0= ruleWildID ) ) )* otherlv_40= ')' )? ( (lv_wildFile_41_0= ruleWildcardRule ) ) otherlv_42= '(' ( (lv_sections_43_0= ruleWildcardRule ) ) ( (otherlv_44= ',' )? ( (lv_sections_45_0= ruleWildcardRule ) ) )* otherlv_46= ')' otherlv_47= ')' ) ) )
            // InternalLinkerScript.g:2880:2: ( ( () (otherlv_1= 'INPUT_SECTION_FLAGS' otherlv_2= '(' ( (lv_flags_3_0= ruleWildID ) ) (otherlv_4= '&' ( (lv_flags_5_0= ruleWildID ) ) )* otherlv_6= ')' )? ( (lv_file_7_0= ruleWildID ) ) ) | ( () (otherlv_9= 'INPUT_SECTION_FLAGS' otherlv_10= '(' ( (lv_flags_11_0= ruleWildID ) ) (otherlv_12= '&' ( (lv_flags_13_0= ruleWildID ) ) )* otherlv_14= ')' )? ( (lv_wildFile_15_0= ruleWildcardRule ) ) otherlv_16= '(' ( (lv_sections_17_0= ruleWildcardRule ) ) ( (otherlv_18= ',' )? ( (lv_sections_19_0= ruleWildcardRule ) ) )* otherlv_20= ')' ) | ( () ( (lv_keep_22_0= 'KEEP' ) ) otherlv_23= '(' (otherlv_24= 'INPUT_SECTION_FLAGS' otherlv_25= '(' ( (lv_flags_26_0= ruleWildID ) ) (otherlv_27= '&' ( (lv_flags_28_0= ruleWildID ) ) )* otherlv_29= ')' )? ( (lv_file_30_0= ruleWildID ) ) otherlv_31= ')' ) | ( () ( (lv_keep_33_0= 'KEEP' ) ) otherlv_34= '(' (otherlv_35= 'INPUT_SECTION_FLAGS' otherlv_36= '(' ( (lv_flags_37_0= ruleWildID ) ) (otherlv_38= '&' ( (lv_flags_39_0= ruleWildID ) ) )* otherlv_40= ')' )? ( (lv_wildFile_41_0= ruleWildcardRule ) ) otherlv_42= '(' ( (lv_sections_43_0= ruleWildcardRule ) ) ( (otherlv_44= ',' )? ( (lv_sections_45_0= ruleWildcardRule ) ) )* otherlv_46= ')' otherlv_47= ')' ) )
            {
            // InternalLinkerScript.g:2880:2: ( ( () (otherlv_1= 'INPUT_SECTION_FLAGS' otherlv_2= '(' ( (lv_flags_3_0= ruleWildID ) ) (otherlv_4= '&' ( (lv_flags_5_0= ruleWildID ) ) )* otherlv_6= ')' )? ( (lv_file_7_0= ruleWildID ) ) ) | ( () (otherlv_9= 'INPUT_SECTION_FLAGS' otherlv_10= '(' ( (lv_flags_11_0= ruleWildID ) ) (otherlv_12= '&' ( (lv_flags_13_0= ruleWildID ) ) )* otherlv_14= ')' )? ( (lv_wildFile_15_0= ruleWildcardRule ) ) otherlv_16= '(' ( (lv_sections_17_0= ruleWildcardRule ) ) ( (otherlv_18= ',' )? ( (lv_sections_19_0= ruleWildcardRule ) ) )* otherlv_20= ')' ) | ( () ( (lv_keep_22_0= 'KEEP' ) ) otherlv_23= '(' (otherlv_24= 'INPUT_SECTION_FLAGS' otherlv_25= '(' ( (lv_flags_26_0= ruleWildID ) ) (otherlv_27= '&' ( (lv_flags_28_0= ruleWildID ) ) )* otherlv_29= ')' )? ( (lv_file_30_0= ruleWildID ) ) otherlv_31= ')' ) | ( () ( (lv_keep_33_0= 'KEEP' ) ) otherlv_34= '(' (otherlv_35= 'INPUT_SECTION_FLAGS' otherlv_36= '(' ( (lv_flags_37_0= ruleWildID ) ) (otherlv_38= '&' ( (lv_flags_39_0= ruleWildID ) ) )* otherlv_40= ')' )? ( (lv_wildFile_41_0= ruleWildcardRule ) ) otherlv_42= '(' ( (lv_sections_43_0= ruleWildcardRule ) ) ( (otherlv_44= ',' )? ( (lv_sections_45_0= ruleWildcardRule ) ) )* otherlv_46= ')' otherlv_47= ')' ) )
            int alt53=4;
            alt53 = dfa53.predict(input);
            switch (alt53) {
                case 1 :
                    // InternalLinkerScript.g:2881:3: ( () (otherlv_1= 'INPUT_SECTION_FLAGS' otherlv_2= '(' ( (lv_flags_3_0= ruleWildID ) ) (otherlv_4= '&' ( (lv_flags_5_0= ruleWildID ) ) )* otherlv_6= ')' )? ( (lv_file_7_0= ruleWildID ) ) )
                    {
                    // InternalLinkerScript.g:2881:3: ( () (otherlv_1= 'INPUT_SECTION_FLAGS' otherlv_2= '(' ( (lv_flags_3_0= ruleWildID ) ) (otherlv_4= '&' ( (lv_flags_5_0= ruleWildID ) ) )* otherlv_6= ')' )? ( (lv_file_7_0= ruleWildID ) ) )
                    // InternalLinkerScript.g:2882:4: () (otherlv_1= 'INPUT_SECTION_FLAGS' otherlv_2= '(' ( (lv_flags_3_0= ruleWildID ) ) (otherlv_4= '&' ( (lv_flags_5_0= ruleWildID ) ) )* otherlv_6= ')' )? ( (lv_file_7_0= ruleWildID ) )
                    {
                    // InternalLinkerScript.g:2882:4: ()
                    // InternalLinkerScript.g:2883:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getInputSectionAccess().getInputSectionFileAction_0_0(),
                      						current);
                      				
                    }

                    }

                    // InternalLinkerScript.g:2889:4: (otherlv_1= 'INPUT_SECTION_FLAGS' otherlv_2= '(' ( (lv_flags_3_0= ruleWildID ) ) (otherlv_4= '&' ( (lv_flags_5_0= ruleWildID ) ) )* otherlv_6= ')' )?
                    int alt42=2;
                    int LA42_0 = input.LA(1);

                    if ( (LA42_0==72) ) {
                        alt42=1;
                    }
                    switch (alt42) {
                        case 1 :
                            // InternalLinkerScript.g:2890:5: otherlv_1= 'INPUT_SECTION_FLAGS' otherlv_2= '(' ( (lv_flags_3_0= ruleWildID ) ) (otherlv_4= '&' ( (lv_flags_5_0= ruleWildID ) ) )* otherlv_6= ')'
                            {
                            otherlv_1=(Token)match(input,72,FOLLOW_5); if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              					newLeafNode(otherlv_1, grammarAccess.getInputSectionAccess().getINPUT_SECTION_FLAGSKeyword_0_1_0());
                              				
                            }
                            otherlv_2=(Token)match(input,13,FOLLOW_6); if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              					newLeafNode(otherlv_2, grammarAccess.getInputSectionAccess().getLeftParenthesisKeyword_0_1_1());
                              				
                            }
                            // InternalLinkerScript.g:2898:5: ( (lv_flags_3_0= ruleWildID ) )
                            // InternalLinkerScript.g:2899:6: (lv_flags_3_0= ruleWildID )
                            {
                            // InternalLinkerScript.g:2899:6: (lv_flags_3_0= ruleWildID )
                            // InternalLinkerScript.g:2900:7: lv_flags_3_0= ruleWildID
                            {
                            if ( state.backtracking==0 ) {

                              							newCompositeNode(grammarAccess.getInputSectionAccess().getFlagsWildIDParserRuleCall_0_1_2_0());
                              						
                            }
                            pushFollow(FOLLOW_39);
                            lv_flags_3_0=ruleWildID();

                            state._fsp--;
                            if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              							if (current==null) {
                              								current = createModelElementForParent(grammarAccess.getInputSectionRule());
                              							}
                              							add(
                              								current,
                              								"flags",
                              								lv_flags_3_0,
                              								"org.eclipse.cdt.linkerscript.LinkerScript.WildID");
                              							afterParserOrEnumRuleCall();
                              						
                            }

                            }


                            }

                            // InternalLinkerScript.g:2917:5: (otherlv_4= '&' ( (lv_flags_5_0= ruleWildID ) ) )*
                            loop41:
                            do {
                                int alt41=2;
                                int LA41_0 = input.LA(1);

                                if ( (LA41_0==73) ) {
                                    alt41=1;
                                }


                                switch (alt41) {
                            	case 1 :
                            	    // InternalLinkerScript.g:2918:6: otherlv_4= '&' ( (lv_flags_5_0= ruleWildID ) )
                            	    {
                            	    otherlv_4=(Token)match(input,73,FOLLOW_6); if (state.failed) return current;
                            	    if ( state.backtracking==0 ) {

                            	      						newLeafNode(otherlv_4, grammarAccess.getInputSectionAccess().getAmpersandKeyword_0_1_3_0());
                            	      					
                            	    }
                            	    // InternalLinkerScript.g:2922:6: ( (lv_flags_5_0= ruleWildID ) )
                            	    // InternalLinkerScript.g:2923:7: (lv_flags_5_0= ruleWildID )
                            	    {
                            	    // InternalLinkerScript.g:2923:7: (lv_flags_5_0= ruleWildID )
                            	    // InternalLinkerScript.g:2924:8: lv_flags_5_0= ruleWildID
                            	    {
                            	    if ( state.backtracking==0 ) {

                            	      								newCompositeNode(grammarAccess.getInputSectionAccess().getFlagsWildIDParserRuleCall_0_1_3_1_0());
                            	      							
                            	    }
                            	    pushFollow(FOLLOW_39);
                            	    lv_flags_5_0=ruleWildID();

                            	    state._fsp--;
                            	    if (state.failed) return current;
                            	    if ( state.backtracking==0 ) {

                            	      								if (current==null) {
                            	      									current = createModelElementForParent(grammarAccess.getInputSectionRule());
                            	      								}
                            	      								add(
                            	      									current,
                            	      									"flags",
                            	      									lv_flags_5_0,
                            	      									"org.eclipse.cdt.linkerscript.LinkerScript.WildID");
                            	      								afterParserOrEnumRuleCall();
                            	      							
                            	    }

                            	    }


                            	    }


                            	    }
                            	    break;

                            	default :
                            	    break loop41;
                                }
                            } while (true);

                            otherlv_6=(Token)match(input,14,FOLLOW_6); if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              					newLeafNode(otherlv_6, grammarAccess.getInputSectionAccess().getRightParenthesisKeyword_0_1_4());
                              				
                            }

                            }
                            break;

                    }

                    // InternalLinkerScript.g:2947:4: ( (lv_file_7_0= ruleWildID ) )
                    // InternalLinkerScript.g:2948:5: (lv_file_7_0= ruleWildID )
                    {
                    // InternalLinkerScript.g:2948:5: (lv_file_7_0= ruleWildID )
                    // InternalLinkerScript.g:2949:6: lv_file_7_0= ruleWildID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getInputSectionAccess().getFileWildIDParserRuleCall_0_2_0());
                      					
                    }
                    pushFollow(FOLLOW_2);
                    lv_file_7_0=ruleWildID();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getInputSectionRule());
                      						}
                      						set(
                      							current,
                      							"file",
                      							lv_file_7_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.WildID");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }


                    }


                    }
                    break;
                case 2 :
                    // InternalLinkerScript.g:2968:3: ( () (otherlv_9= 'INPUT_SECTION_FLAGS' otherlv_10= '(' ( (lv_flags_11_0= ruleWildID ) ) (otherlv_12= '&' ( (lv_flags_13_0= ruleWildID ) ) )* otherlv_14= ')' )? ( (lv_wildFile_15_0= ruleWildcardRule ) ) otherlv_16= '(' ( (lv_sections_17_0= ruleWildcardRule ) ) ( (otherlv_18= ',' )? ( (lv_sections_19_0= ruleWildcardRule ) ) )* otherlv_20= ')' )
                    {
                    // InternalLinkerScript.g:2968:3: ( () (otherlv_9= 'INPUT_SECTION_FLAGS' otherlv_10= '(' ( (lv_flags_11_0= ruleWildID ) ) (otherlv_12= '&' ( (lv_flags_13_0= ruleWildID ) ) )* otherlv_14= ')' )? ( (lv_wildFile_15_0= ruleWildcardRule ) ) otherlv_16= '(' ( (lv_sections_17_0= ruleWildcardRule ) ) ( (otherlv_18= ',' )? ( (lv_sections_19_0= ruleWildcardRule ) ) )* otherlv_20= ')' )
                    // InternalLinkerScript.g:2969:4: () (otherlv_9= 'INPUT_SECTION_FLAGS' otherlv_10= '(' ( (lv_flags_11_0= ruleWildID ) ) (otherlv_12= '&' ( (lv_flags_13_0= ruleWildID ) ) )* otherlv_14= ')' )? ( (lv_wildFile_15_0= ruleWildcardRule ) ) otherlv_16= '(' ( (lv_sections_17_0= ruleWildcardRule ) ) ( (otherlv_18= ',' )? ( (lv_sections_19_0= ruleWildcardRule ) ) )* otherlv_20= ')'
                    {
                    // InternalLinkerScript.g:2969:4: ()
                    // InternalLinkerScript.g:2970:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getInputSectionAccess().getInputSectionWildAction_1_0(),
                      						current);
                      				
                    }

                    }

                    // InternalLinkerScript.g:2976:4: (otherlv_9= 'INPUT_SECTION_FLAGS' otherlv_10= '(' ( (lv_flags_11_0= ruleWildID ) ) (otherlv_12= '&' ( (lv_flags_13_0= ruleWildID ) ) )* otherlv_14= ')' )?
                    int alt44=2;
                    int LA44_0 = input.LA(1);

                    if ( (LA44_0==72) ) {
                        alt44=1;
                    }
                    switch (alt44) {
                        case 1 :
                            // InternalLinkerScript.g:2977:5: otherlv_9= 'INPUT_SECTION_FLAGS' otherlv_10= '(' ( (lv_flags_11_0= ruleWildID ) ) (otherlv_12= '&' ( (lv_flags_13_0= ruleWildID ) ) )* otherlv_14= ')'
                            {
                            otherlv_9=(Token)match(input,72,FOLLOW_5); if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              					newLeafNode(otherlv_9, grammarAccess.getInputSectionAccess().getINPUT_SECTION_FLAGSKeyword_1_1_0());
                              				
                            }
                            otherlv_10=(Token)match(input,13,FOLLOW_6); if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              					newLeafNode(otherlv_10, grammarAccess.getInputSectionAccess().getLeftParenthesisKeyword_1_1_1());
                              				
                            }
                            // InternalLinkerScript.g:2985:5: ( (lv_flags_11_0= ruleWildID ) )
                            // InternalLinkerScript.g:2986:6: (lv_flags_11_0= ruleWildID )
                            {
                            // InternalLinkerScript.g:2986:6: (lv_flags_11_0= ruleWildID )
                            // InternalLinkerScript.g:2987:7: lv_flags_11_0= ruleWildID
                            {
                            if ( state.backtracking==0 ) {

                              							newCompositeNode(grammarAccess.getInputSectionAccess().getFlagsWildIDParserRuleCall_1_1_2_0());
                              						
                            }
                            pushFollow(FOLLOW_39);
                            lv_flags_11_0=ruleWildID();

                            state._fsp--;
                            if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              							if (current==null) {
                              								current = createModelElementForParent(grammarAccess.getInputSectionRule());
                              							}
                              							add(
                              								current,
                              								"flags",
                              								lv_flags_11_0,
                              								"org.eclipse.cdt.linkerscript.LinkerScript.WildID");
                              							afterParserOrEnumRuleCall();
                              						
                            }

                            }


                            }

                            // InternalLinkerScript.g:3004:5: (otherlv_12= '&' ( (lv_flags_13_0= ruleWildID ) ) )*
                            loop43:
                            do {
                                int alt43=2;
                                int LA43_0 = input.LA(1);

                                if ( (LA43_0==73) ) {
                                    alt43=1;
                                }


                                switch (alt43) {
                            	case 1 :
                            	    // InternalLinkerScript.g:3005:6: otherlv_12= '&' ( (lv_flags_13_0= ruleWildID ) )
                            	    {
                            	    otherlv_12=(Token)match(input,73,FOLLOW_6); if (state.failed) return current;
                            	    if ( state.backtracking==0 ) {

                            	      						newLeafNode(otherlv_12, grammarAccess.getInputSectionAccess().getAmpersandKeyword_1_1_3_0());
                            	      					
                            	    }
                            	    // InternalLinkerScript.g:3009:6: ( (lv_flags_13_0= ruleWildID ) )
                            	    // InternalLinkerScript.g:3010:7: (lv_flags_13_0= ruleWildID )
                            	    {
                            	    // InternalLinkerScript.g:3010:7: (lv_flags_13_0= ruleWildID )
                            	    // InternalLinkerScript.g:3011:8: lv_flags_13_0= ruleWildID
                            	    {
                            	    if ( state.backtracking==0 ) {

                            	      								newCompositeNode(grammarAccess.getInputSectionAccess().getFlagsWildIDParserRuleCall_1_1_3_1_0());
                            	      							
                            	    }
                            	    pushFollow(FOLLOW_39);
                            	    lv_flags_13_0=ruleWildID();

                            	    state._fsp--;
                            	    if (state.failed) return current;
                            	    if ( state.backtracking==0 ) {

                            	      								if (current==null) {
                            	      									current = createModelElementForParent(grammarAccess.getInputSectionRule());
                            	      								}
                            	      								add(
                            	      									current,
                            	      									"flags",
                            	      									lv_flags_13_0,
                            	      									"org.eclipse.cdt.linkerscript.LinkerScript.WildID");
                            	      								afterParserOrEnumRuleCall();
                            	      							
                            	    }

                            	    }


                            	    }


                            	    }
                            	    break;

                            	default :
                            	    break loop43;
                                }
                            } while (true);

                            otherlv_14=(Token)match(input,14,FOLLOW_40); if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              					newLeafNode(otherlv_14, grammarAccess.getInputSectionAccess().getRightParenthesisKeyword_1_1_4());
                              				
                            }

                            }
                            break;

                    }

                    // InternalLinkerScript.g:3034:4: ( (lv_wildFile_15_0= ruleWildcardRule ) )
                    // InternalLinkerScript.g:3035:5: (lv_wildFile_15_0= ruleWildcardRule )
                    {
                    // InternalLinkerScript.g:3035:5: (lv_wildFile_15_0= ruleWildcardRule )
                    // InternalLinkerScript.g:3036:6: lv_wildFile_15_0= ruleWildcardRule
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getInputSectionAccess().getWildFileWildcardRuleParserRuleCall_1_2_0());
                      					
                    }
                    pushFollow(FOLLOW_5);
                    lv_wildFile_15_0=ruleWildcardRule();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getInputSectionRule());
                      						}
                      						set(
                      							current,
                      							"wildFile",
                      							lv_wildFile_15_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.WildcardRule");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    otherlv_16=(Token)match(input,13,FOLLOW_40); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_16, grammarAccess.getInputSectionAccess().getLeftParenthesisKeyword_1_3());
                      			
                    }
                    // InternalLinkerScript.g:3057:4: ( (lv_sections_17_0= ruleWildcardRule ) )
                    // InternalLinkerScript.g:3058:5: (lv_sections_17_0= ruleWildcardRule )
                    {
                    // InternalLinkerScript.g:3058:5: (lv_sections_17_0= ruleWildcardRule )
                    // InternalLinkerScript.g:3059:6: lv_sections_17_0= ruleWildcardRule
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getInputSectionAccess().getSectionsWildcardRuleParserRuleCall_1_4_0());
                      					
                    }
                    pushFollow(FOLLOW_41);
                    lv_sections_17_0=ruleWildcardRule();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getInputSectionRule());
                      						}
                      						add(
                      							current,
                      							"sections",
                      							lv_sections_17_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.WildcardRule");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    // InternalLinkerScript.g:3076:4: ( (otherlv_18= ',' )? ( (lv_sections_19_0= ruleWildcardRule ) ) )*
                    loop46:
                    do {
                        int alt46=2;
                        int LA46_0 = input.LA(1);

                        if ( (LA46_0==RULE_ID||LA46_0==10||LA46_0==54||(LA46_0>=75 && LA46_0<=80)||(LA46_0>=82 && LA46_0<=83)||(LA46_0>=85 && LA46_0<=86)||LA46_0==96) ) {
                            alt46=1;
                        }


                        switch (alt46) {
                    	case 1 :
                    	    // InternalLinkerScript.g:3077:5: (otherlv_18= ',' )? ( (lv_sections_19_0= ruleWildcardRule ) )
                    	    {
                    	    // InternalLinkerScript.g:3077:5: (otherlv_18= ',' )?
                    	    int alt45=2;
                    	    int LA45_0 = input.LA(1);

                    	    if ( (LA45_0==10) ) {
                    	        alt45=1;
                    	    }
                    	    switch (alt45) {
                    	        case 1 :
                    	            // InternalLinkerScript.g:3078:6: otherlv_18= ','
                    	            {
                    	            otherlv_18=(Token)match(input,10,FOLLOW_40); if (state.failed) return current;
                    	            if ( state.backtracking==0 ) {

                    	              						newLeafNode(otherlv_18, grammarAccess.getInputSectionAccess().getCommaKeyword_1_5_0());
                    	              					
                    	            }

                    	            }
                    	            break;

                    	    }

                    	    // InternalLinkerScript.g:3083:5: ( (lv_sections_19_0= ruleWildcardRule ) )
                    	    // InternalLinkerScript.g:3084:6: (lv_sections_19_0= ruleWildcardRule )
                    	    {
                    	    // InternalLinkerScript.g:3084:6: (lv_sections_19_0= ruleWildcardRule )
                    	    // InternalLinkerScript.g:3085:7: lv_sections_19_0= ruleWildcardRule
                    	    {
                    	    if ( state.backtracking==0 ) {

                    	      							newCompositeNode(grammarAccess.getInputSectionAccess().getSectionsWildcardRuleParserRuleCall_1_5_1_0());
                    	      						
                    	    }
                    	    pushFollow(FOLLOW_41);
                    	    lv_sections_19_0=ruleWildcardRule();

                    	    state._fsp--;
                    	    if (state.failed) return current;
                    	    if ( state.backtracking==0 ) {

                    	      							if (current==null) {
                    	      								current = createModelElementForParent(grammarAccess.getInputSectionRule());
                    	      							}
                    	      							add(
                    	      								current,
                    	      								"sections",
                    	      								lv_sections_19_0,
                    	      								"org.eclipse.cdt.linkerscript.LinkerScript.WildcardRule");
                    	      							afterParserOrEnumRuleCall();
                    	      						
                    	    }

                    	    }


                    	    }


                    	    }
                    	    break;

                    	default :
                    	    break loop46;
                        }
                    } while (true);

                    otherlv_20=(Token)match(input,14,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_20, grammarAccess.getInputSectionAccess().getRightParenthesisKeyword_1_6());
                      			
                    }

                    }


                    }
                    break;
                case 3 :
                    // InternalLinkerScript.g:3109:3: ( () ( (lv_keep_22_0= 'KEEP' ) ) otherlv_23= '(' (otherlv_24= 'INPUT_SECTION_FLAGS' otherlv_25= '(' ( (lv_flags_26_0= ruleWildID ) ) (otherlv_27= '&' ( (lv_flags_28_0= ruleWildID ) ) )* otherlv_29= ')' )? ( (lv_file_30_0= ruleWildID ) ) otherlv_31= ')' )
                    {
                    // InternalLinkerScript.g:3109:3: ( () ( (lv_keep_22_0= 'KEEP' ) ) otherlv_23= '(' (otherlv_24= 'INPUT_SECTION_FLAGS' otherlv_25= '(' ( (lv_flags_26_0= ruleWildID ) ) (otherlv_27= '&' ( (lv_flags_28_0= ruleWildID ) ) )* otherlv_29= ')' )? ( (lv_file_30_0= ruleWildID ) ) otherlv_31= ')' )
                    // InternalLinkerScript.g:3110:4: () ( (lv_keep_22_0= 'KEEP' ) ) otherlv_23= '(' (otherlv_24= 'INPUT_SECTION_FLAGS' otherlv_25= '(' ( (lv_flags_26_0= ruleWildID ) ) (otherlv_27= '&' ( (lv_flags_28_0= ruleWildID ) ) )* otherlv_29= ')' )? ( (lv_file_30_0= ruleWildID ) ) otherlv_31= ')'
                    {
                    // InternalLinkerScript.g:3110:4: ()
                    // InternalLinkerScript.g:3111:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getInputSectionAccess().getInputSectionFileAction_2_0(),
                      						current);
                      				
                    }

                    }

                    // InternalLinkerScript.g:3117:4: ( (lv_keep_22_0= 'KEEP' ) )
                    // InternalLinkerScript.g:3118:5: (lv_keep_22_0= 'KEEP' )
                    {
                    // InternalLinkerScript.g:3118:5: (lv_keep_22_0= 'KEEP' )
                    // InternalLinkerScript.g:3119:6: lv_keep_22_0= 'KEEP'
                    {
                    lv_keep_22_0=(Token)match(input,74,FOLLOW_5); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						newLeafNode(lv_keep_22_0, grammarAccess.getInputSectionAccess().getKeepKEEPKeyword_2_1_0());
                      					
                    }
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElement(grammarAccess.getInputSectionRule());
                      						}
                      						setWithLastConsumed(current, "keep", true, "KEEP");
                      					
                    }

                    }


                    }

                    otherlv_23=(Token)match(input,13,FOLLOW_42); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_23, grammarAccess.getInputSectionAccess().getLeftParenthesisKeyword_2_2());
                      			
                    }
                    // InternalLinkerScript.g:3135:4: (otherlv_24= 'INPUT_SECTION_FLAGS' otherlv_25= '(' ( (lv_flags_26_0= ruleWildID ) ) (otherlv_27= '&' ( (lv_flags_28_0= ruleWildID ) ) )* otherlv_29= ')' )?
                    int alt48=2;
                    int LA48_0 = input.LA(1);

                    if ( (LA48_0==72) ) {
                        alt48=1;
                    }
                    switch (alt48) {
                        case 1 :
                            // InternalLinkerScript.g:3136:5: otherlv_24= 'INPUT_SECTION_FLAGS' otherlv_25= '(' ( (lv_flags_26_0= ruleWildID ) ) (otherlv_27= '&' ( (lv_flags_28_0= ruleWildID ) ) )* otherlv_29= ')'
                            {
                            otherlv_24=(Token)match(input,72,FOLLOW_5); if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              					newLeafNode(otherlv_24, grammarAccess.getInputSectionAccess().getINPUT_SECTION_FLAGSKeyword_2_3_0());
                              				
                            }
                            otherlv_25=(Token)match(input,13,FOLLOW_6); if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              					newLeafNode(otherlv_25, grammarAccess.getInputSectionAccess().getLeftParenthesisKeyword_2_3_1());
                              				
                            }
                            // InternalLinkerScript.g:3144:5: ( (lv_flags_26_0= ruleWildID ) )
                            // InternalLinkerScript.g:3145:6: (lv_flags_26_0= ruleWildID )
                            {
                            // InternalLinkerScript.g:3145:6: (lv_flags_26_0= ruleWildID )
                            // InternalLinkerScript.g:3146:7: lv_flags_26_0= ruleWildID
                            {
                            if ( state.backtracking==0 ) {

                              							newCompositeNode(grammarAccess.getInputSectionAccess().getFlagsWildIDParserRuleCall_2_3_2_0());
                              						
                            }
                            pushFollow(FOLLOW_39);
                            lv_flags_26_0=ruleWildID();

                            state._fsp--;
                            if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              							if (current==null) {
                              								current = createModelElementForParent(grammarAccess.getInputSectionRule());
                              							}
                              							add(
                              								current,
                              								"flags",
                              								lv_flags_26_0,
                              								"org.eclipse.cdt.linkerscript.LinkerScript.WildID");
                              							afterParserOrEnumRuleCall();
                              						
                            }

                            }


                            }

                            // InternalLinkerScript.g:3163:5: (otherlv_27= '&' ( (lv_flags_28_0= ruleWildID ) ) )*
                            loop47:
                            do {
                                int alt47=2;
                                int LA47_0 = input.LA(1);

                                if ( (LA47_0==73) ) {
                                    alt47=1;
                                }


                                switch (alt47) {
                            	case 1 :
                            	    // InternalLinkerScript.g:3164:6: otherlv_27= '&' ( (lv_flags_28_0= ruleWildID ) )
                            	    {
                            	    otherlv_27=(Token)match(input,73,FOLLOW_6); if (state.failed) return current;
                            	    if ( state.backtracking==0 ) {

                            	      						newLeafNode(otherlv_27, grammarAccess.getInputSectionAccess().getAmpersandKeyword_2_3_3_0());
                            	      					
                            	    }
                            	    // InternalLinkerScript.g:3168:6: ( (lv_flags_28_0= ruleWildID ) )
                            	    // InternalLinkerScript.g:3169:7: (lv_flags_28_0= ruleWildID )
                            	    {
                            	    // InternalLinkerScript.g:3169:7: (lv_flags_28_0= ruleWildID )
                            	    // InternalLinkerScript.g:3170:8: lv_flags_28_0= ruleWildID
                            	    {
                            	    if ( state.backtracking==0 ) {

                            	      								newCompositeNode(grammarAccess.getInputSectionAccess().getFlagsWildIDParserRuleCall_2_3_3_1_0());
                            	      							
                            	    }
                            	    pushFollow(FOLLOW_39);
                            	    lv_flags_28_0=ruleWildID();

                            	    state._fsp--;
                            	    if (state.failed) return current;
                            	    if ( state.backtracking==0 ) {

                            	      								if (current==null) {
                            	      									current = createModelElementForParent(grammarAccess.getInputSectionRule());
                            	      								}
                            	      								add(
                            	      									current,
                            	      									"flags",
                            	      									lv_flags_28_0,
                            	      									"org.eclipse.cdt.linkerscript.LinkerScript.WildID");
                            	      								afterParserOrEnumRuleCall();
                            	      							
                            	    }

                            	    }


                            	    }


                            	    }
                            	    break;

                            	default :
                            	    break loop47;
                                }
                            } while (true);

                            otherlv_29=(Token)match(input,14,FOLLOW_6); if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              					newLeafNode(otherlv_29, grammarAccess.getInputSectionAccess().getRightParenthesisKeyword_2_3_4());
                              				
                            }

                            }
                            break;

                    }

                    // InternalLinkerScript.g:3193:4: ( (lv_file_30_0= ruleWildID ) )
                    // InternalLinkerScript.g:3194:5: (lv_file_30_0= ruleWildID )
                    {
                    // InternalLinkerScript.g:3194:5: (lv_file_30_0= ruleWildID )
                    // InternalLinkerScript.g:3195:6: lv_file_30_0= ruleWildID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getInputSectionAccess().getFileWildIDParserRuleCall_2_4_0());
                      					
                    }
                    pushFollow(FOLLOW_7);
                    lv_file_30_0=ruleWildID();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getInputSectionRule());
                      						}
                      						set(
                      							current,
                      							"file",
                      							lv_file_30_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.WildID");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    otherlv_31=(Token)match(input,14,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_31, grammarAccess.getInputSectionAccess().getRightParenthesisKeyword_2_5());
                      			
                    }

                    }


                    }
                    break;
                case 4 :
                    // InternalLinkerScript.g:3218:3: ( () ( (lv_keep_33_0= 'KEEP' ) ) otherlv_34= '(' (otherlv_35= 'INPUT_SECTION_FLAGS' otherlv_36= '(' ( (lv_flags_37_0= ruleWildID ) ) (otherlv_38= '&' ( (lv_flags_39_0= ruleWildID ) ) )* otherlv_40= ')' )? ( (lv_wildFile_41_0= ruleWildcardRule ) ) otherlv_42= '(' ( (lv_sections_43_0= ruleWildcardRule ) ) ( (otherlv_44= ',' )? ( (lv_sections_45_0= ruleWildcardRule ) ) )* otherlv_46= ')' otherlv_47= ')' )
                    {
                    // InternalLinkerScript.g:3218:3: ( () ( (lv_keep_33_0= 'KEEP' ) ) otherlv_34= '(' (otherlv_35= 'INPUT_SECTION_FLAGS' otherlv_36= '(' ( (lv_flags_37_0= ruleWildID ) ) (otherlv_38= '&' ( (lv_flags_39_0= ruleWildID ) ) )* otherlv_40= ')' )? ( (lv_wildFile_41_0= ruleWildcardRule ) ) otherlv_42= '(' ( (lv_sections_43_0= ruleWildcardRule ) ) ( (otherlv_44= ',' )? ( (lv_sections_45_0= ruleWildcardRule ) ) )* otherlv_46= ')' otherlv_47= ')' )
                    // InternalLinkerScript.g:3219:4: () ( (lv_keep_33_0= 'KEEP' ) ) otherlv_34= '(' (otherlv_35= 'INPUT_SECTION_FLAGS' otherlv_36= '(' ( (lv_flags_37_0= ruleWildID ) ) (otherlv_38= '&' ( (lv_flags_39_0= ruleWildID ) ) )* otherlv_40= ')' )? ( (lv_wildFile_41_0= ruleWildcardRule ) ) otherlv_42= '(' ( (lv_sections_43_0= ruleWildcardRule ) ) ( (otherlv_44= ',' )? ( (lv_sections_45_0= ruleWildcardRule ) ) )* otherlv_46= ')' otherlv_47= ')'
                    {
                    // InternalLinkerScript.g:3219:4: ()
                    // InternalLinkerScript.g:3220:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getInputSectionAccess().getInputSectionWildAction_3_0(),
                      						current);
                      				
                    }

                    }

                    // InternalLinkerScript.g:3226:4: ( (lv_keep_33_0= 'KEEP' ) )
                    // InternalLinkerScript.g:3227:5: (lv_keep_33_0= 'KEEP' )
                    {
                    // InternalLinkerScript.g:3227:5: (lv_keep_33_0= 'KEEP' )
                    // InternalLinkerScript.g:3228:6: lv_keep_33_0= 'KEEP'
                    {
                    lv_keep_33_0=(Token)match(input,74,FOLLOW_5); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						newLeafNode(lv_keep_33_0, grammarAccess.getInputSectionAccess().getKeepKEEPKeyword_3_1_0());
                      					
                    }
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElement(grammarAccess.getInputSectionRule());
                      						}
                      						setWithLastConsumed(current, "keep", true, "KEEP");
                      					
                    }

                    }


                    }

                    otherlv_34=(Token)match(input,13,FOLLOW_40); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_34, grammarAccess.getInputSectionAccess().getLeftParenthesisKeyword_3_2());
                      			
                    }
                    // InternalLinkerScript.g:3244:4: (otherlv_35= 'INPUT_SECTION_FLAGS' otherlv_36= '(' ( (lv_flags_37_0= ruleWildID ) ) (otherlv_38= '&' ( (lv_flags_39_0= ruleWildID ) ) )* otherlv_40= ')' )?
                    int alt50=2;
                    int LA50_0 = input.LA(1);

                    if ( (LA50_0==72) ) {
                        alt50=1;
                    }
                    switch (alt50) {
                        case 1 :
                            // InternalLinkerScript.g:3245:5: otherlv_35= 'INPUT_SECTION_FLAGS' otherlv_36= '(' ( (lv_flags_37_0= ruleWildID ) ) (otherlv_38= '&' ( (lv_flags_39_0= ruleWildID ) ) )* otherlv_40= ')'
                            {
                            otherlv_35=(Token)match(input,72,FOLLOW_5); if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              					newLeafNode(otherlv_35, grammarAccess.getInputSectionAccess().getINPUT_SECTION_FLAGSKeyword_3_3_0());
                              				
                            }
                            otherlv_36=(Token)match(input,13,FOLLOW_6); if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              					newLeafNode(otherlv_36, grammarAccess.getInputSectionAccess().getLeftParenthesisKeyword_3_3_1());
                              				
                            }
                            // InternalLinkerScript.g:3253:5: ( (lv_flags_37_0= ruleWildID ) )
                            // InternalLinkerScript.g:3254:6: (lv_flags_37_0= ruleWildID )
                            {
                            // InternalLinkerScript.g:3254:6: (lv_flags_37_0= ruleWildID )
                            // InternalLinkerScript.g:3255:7: lv_flags_37_0= ruleWildID
                            {
                            if ( state.backtracking==0 ) {

                              							newCompositeNode(grammarAccess.getInputSectionAccess().getFlagsWildIDParserRuleCall_3_3_2_0());
                              						
                            }
                            pushFollow(FOLLOW_39);
                            lv_flags_37_0=ruleWildID();

                            state._fsp--;
                            if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              							if (current==null) {
                              								current = createModelElementForParent(grammarAccess.getInputSectionRule());
                              							}
                              							add(
                              								current,
                              								"flags",
                              								lv_flags_37_0,
                              								"org.eclipse.cdt.linkerscript.LinkerScript.WildID");
                              							afterParserOrEnumRuleCall();
                              						
                            }

                            }


                            }

                            // InternalLinkerScript.g:3272:5: (otherlv_38= '&' ( (lv_flags_39_0= ruleWildID ) ) )*
                            loop49:
                            do {
                                int alt49=2;
                                int LA49_0 = input.LA(1);

                                if ( (LA49_0==73) ) {
                                    alt49=1;
                                }


                                switch (alt49) {
                            	case 1 :
                            	    // InternalLinkerScript.g:3273:6: otherlv_38= '&' ( (lv_flags_39_0= ruleWildID ) )
                            	    {
                            	    otherlv_38=(Token)match(input,73,FOLLOW_6); if (state.failed) return current;
                            	    if ( state.backtracking==0 ) {

                            	      						newLeafNode(otherlv_38, grammarAccess.getInputSectionAccess().getAmpersandKeyword_3_3_3_0());
                            	      					
                            	    }
                            	    // InternalLinkerScript.g:3277:6: ( (lv_flags_39_0= ruleWildID ) )
                            	    // InternalLinkerScript.g:3278:7: (lv_flags_39_0= ruleWildID )
                            	    {
                            	    // InternalLinkerScript.g:3278:7: (lv_flags_39_0= ruleWildID )
                            	    // InternalLinkerScript.g:3279:8: lv_flags_39_0= ruleWildID
                            	    {
                            	    if ( state.backtracking==0 ) {

                            	      								newCompositeNode(grammarAccess.getInputSectionAccess().getFlagsWildIDParserRuleCall_3_3_3_1_0());
                            	      							
                            	    }
                            	    pushFollow(FOLLOW_39);
                            	    lv_flags_39_0=ruleWildID();

                            	    state._fsp--;
                            	    if (state.failed) return current;
                            	    if ( state.backtracking==0 ) {

                            	      								if (current==null) {
                            	      									current = createModelElementForParent(grammarAccess.getInputSectionRule());
                            	      								}
                            	      								add(
                            	      									current,
                            	      									"flags",
                            	      									lv_flags_39_0,
                            	      									"org.eclipse.cdt.linkerscript.LinkerScript.WildID");
                            	      								afterParserOrEnumRuleCall();
                            	      							
                            	    }

                            	    }


                            	    }


                            	    }
                            	    break;

                            	default :
                            	    break loop49;
                                }
                            } while (true);

                            otherlv_40=(Token)match(input,14,FOLLOW_40); if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              					newLeafNode(otherlv_40, grammarAccess.getInputSectionAccess().getRightParenthesisKeyword_3_3_4());
                              				
                            }

                            }
                            break;

                    }

                    // InternalLinkerScript.g:3302:4: ( (lv_wildFile_41_0= ruleWildcardRule ) )
                    // InternalLinkerScript.g:3303:5: (lv_wildFile_41_0= ruleWildcardRule )
                    {
                    // InternalLinkerScript.g:3303:5: (lv_wildFile_41_0= ruleWildcardRule )
                    // InternalLinkerScript.g:3304:6: lv_wildFile_41_0= ruleWildcardRule
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getInputSectionAccess().getWildFileWildcardRuleParserRuleCall_3_4_0());
                      					
                    }
                    pushFollow(FOLLOW_5);
                    lv_wildFile_41_0=ruleWildcardRule();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getInputSectionRule());
                      						}
                      						set(
                      							current,
                      							"wildFile",
                      							lv_wildFile_41_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.WildcardRule");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    otherlv_42=(Token)match(input,13,FOLLOW_40); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_42, grammarAccess.getInputSectionAccess().getLeftParenthesisKeyword_3_5());
                      			
                    }
                    // InternalLinkerScript.g:3325:4: ( (lv_sections_43_0= ruleWildcardRule ) )
                    // InternalLinkerScript.g:3326:5: (lv_sections_43_0= ruleWildcardRule )
                    {
                    // InternalLinkerScript.g:3326:5: (lv_sections_43_0= ruleWildcardRule )
                    // InternalLinkerScript.g:3327:6: lv_sections_43_0= ruleWildcardRule
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getInputSectionAccess().getSectionsWildcardRuleParserRuleCall_3_6_0());
                      					
                    }
                    pushFollow(FOLLOW_41);
                    lv_sections_43_0=ruleWildcardRule();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getInputSectionRule());
                      						}
                      						add(
                      							current,
                      							"sections",
                      							lv_sections_43_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.WildcardRule");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    // InternalLinkerScript.g:3344:4: ( (otherlv_44= ',' )? ( (lv_sections_45_0= ruleWildcardRule ) ) )*
                    loop52:
                    do {
                        int alt52=2;
                        int LA52_0 = input.LA(1);

                        if ( (LA52_0==RULE_ID||LA52_0==10||LA52_0==54||(LA52_0>=75 && LA52_0<=80)||(LA52_0>=82 && LA52_0<=83)||(LA52_0>=85 && LA52_0<=86)||LA52_0==96) ) {
                            alt52=1;
                        }


                        switch (alt52) {
                    	case 1 :
                    	    // InternalLinkerScript.g:3345:5: (otherlv_44= ',' )? ( (lv_sections_45_0= ruleWildcardRule ) )
                    	    {
                    	    // InternalLinkerScript.g:3345:5: (otherlv_44= ',' )?
                    	    int alt51=2;
                    	    int LA51_0 = input.LA(1);

                    	    if ( (LA51_0==10) ) {
                    	        alt51=1;
                    	    }
                    	    switch (alt51) {
                    	        case 1 :
                    	            // InternalLinkerScript.g:3346:6: otherlv_44= ','
                    	            {
                    	            otherlv_44=(Token)match(input,10,FOLLOW_40); if (state.failed) return current;
                    	            if ( state.backtracking==0 ) {

                    	              						newLeafNode(otherlv_44, grammarAccess.getInputSectionAccess().getCommaKeyword_3_7_0());
                    	              					
                    	            }

                    	            }
                    	            break;

                    	    }

                    	    // InternalLinkerScript.g:3351:5: ( (lv_sections_45_0= ruleWildcardRule ) )
                    	    // InternalLinkerScript.g:3352:6: (lv_sections_45_0= ruleWildcardRule )
                    	    {
                    	    // InternalLinkerScript.g:3352:6: (lv_sections_45_0= ruleWildcardRule )
                    	    // InternalLinkerScript.g:3353:7: lv_sections_45_0= ruleWildcardRule
                    	    {
                    	    if ( state.backtracking==0 ) {

                    	      							newCompositeNode(grammarAccess.getInputSectionAccess().getSectionsWildcardRuleParserRuleCall_3_7_1_0());
                    	      						
                    	    }
                    	    pushFollow(FOLLOW_41);
                    	    lv_sections_45_0=ruleWildcardRule();

                    	    state._fsp--;
                    	    if (state.failed) return current;
                    	    if ( state.backtracking==0 ) {

                    	      							if (current==null) {
                    	      								current = createModelElementForParent(grammarAccess.getInputSectionRule());
                    	      							}
                    	      							add(
                    	      								current,
                    	      								"sections",
                    	      								lv_sections_45_0,
                    	      								"org.eclipse.cdt.linkerscript.LinkerScript.WildcardRule");
                    	      							afterParserOrEnumRuleCall();
                    	      						
                    	    }

                    	    }


                    	    }


                    	    }
                    	    break;

                    	default :
                    	    break loop52;
                        }
                    } while (true);

                    otherlv_46=(Token)match(input,14,FOLLOW_7); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_46, grammarAccess.getInputSectionAccess().getRightParenthesisKeyword_3_8());
                      			
                    }
                    otherlv_47=(Token)match(input,14,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_47, grammarAccess.getInputSectionAccess().getRightParenthesisKeyword_3_9());
                      			
                    }

                    }


                    }
                    break;

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleInputSection"


    // $ANTLR start "entryRuleWildcardRule"
    // InternalLinkerScript.g:3384:1: entryRuleWildcardRule returns [EObject current=null] : iv_ruleWildcardRule= ruleWildcardRule EOF ;
    public final EObject entryRuleWildcardRule() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleWildcardRule = null;


        try {
            // InternalLinkerScript.g:3384:53: (iv_ruleWildcardRule= ruleWildcardRule EOF )
            // InternalLinkerScript.g:3385:2: iv_ruleWildcardRule= ruleWildcardRule EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getWildcardRuleRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleWildcardRule=ruleWildcardRule();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleWildcardRule; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleWildcardRule"


    // $ANTLR start "ruleWildcardRule"
    // InternalLinkerScript.g:3391:1: ruleWildcardRule returns [EObject current=null] : ( ( () ( (lv_name_1_0= ruleWildID ) ) ) | ( () otherlv_3= 'EXCLUDE_FILE' otherlv_4= '(' ( (lv_excludes_5_0= ruleWildID ) )+ otherlv_6= ')' ( (lv_name_7_0= ruleWildID ) ) ) | ( () ruleSORT_BY_NAME otherlv_10= '(' ( (lv_name_11_0= ruleWildID ) ) otherlv_12= ')' ) | ( () otherlv_14= 'SORT_BY_ALIGNMENT' otherlv_15= '(' ( (lv_name_16_0= ruleWildID ) ) otherlv_17= ')' ) | ( () otherlv_19= 'SORT_NONE' otherlv_20= '(' ( (lv_name_21_0= ruleWildID ) ) otherlv_22= ')' ) | ( () ruleSORT_BY_NAME otherlv_25= '(' otherlv_26= 'SORT_BY_ALIGNMENT' otherlv_27= '(' ( (lv_name_28_0= ruleWildID ) ) otherlv_29= ')' otherlv_30= ')' ) | ( () ruleSORT_BY_NAME otherlv_33= '(' ruleSORT_BY_NAME otherlv_35= '(' ( (lv_name_36_0= ruleWildID ) ) otherlv_37= ')' otherlv_38= ')' ) | ( () otherlv_40= 'SORT_BY_ALIGNMENT' otherlv_41= '(' ruleSORT_BY_NAME otherlv_43= '(' ( (lv_name_44_0= ruleWildID ) ) otherlv_45= ')' otherlv_46= ')' ) | ( () otherlv_48= 'SORT_BY_ALIGNMENT' otherlv_49= '(' otherlv_50= 'SORT_BY_ALIGNMENT' otherlv_51= '(' ( (lv_name_52_0= ruleWildID ) ) otherlv_53= ')' otherlv_54= ')' ) | ( () ruleSORT_BY_NAME otherlv_57= '(' otherlv_58= 'EXCLUDE_FILE' otherlv_59= '(' ( (lv_excludes_60_0= ruleWildID ) )+ otherlv_61= ')' ( (lv_name_62_0= ruleWildID ) ) otherlv_63= ')' ) | ( () otherlv_65= 'SORT_BY_INIT_PRIORITY' otherlv_66= '(' ( (lv_name_67_0= ruleWildID ) ) otherlv_68= ')' ) ) ;
    public final EObject ruleWildcardRule() throws RecognitionException {
        EObject current = null;

        Token otherlv_3=null;
        Token otherlv_4=null;
        Token otherlv_6=null;
        Token otherlv_10=null;
        Token otherlv_12=null;
        Token otherlv_14=null;
        Token otherlv_15=null;
        Token otherlv_17=null;
        Token otherlv_19=null;
        Token otherlv_20=null;
        Token otherlv_22=null;
        Token otherlv_25=null;
        Token otherlv_26=null;
        Token otherlv_27=null;
        Token otherlv_29=null;
        Token otherlv_30=null;
        Token otherlv_33=null;
        Token otherlv_35=null;
        Token otherlv_37=null;
        Token otherlv_38=null;
        Token otherlv_40=null;
        Token otherlv_41=null;
        Token otherlv_43=null;
        Token otherlv_45=null;
        Token otherlv_46=null;
        Token otherlv_48=null;
        Token otherlv_49=null;
        Token otherlv_50=null;
        Token otherlv_51=null;
        Token otherlv_53=null;
        Token otherlv_54=null;
        Token otherlv_57=null;
        Token otherlv_58=null;
        Token otherlv_59=null;
        Token otherlv_61=null;
        Token otherlv_63=null;
        Token otherlv_65=null;
        Token otherlv_66=null;
        Token otherlv_68=null;
        AntlrDatatypeRuleToken lv_name_1_0 = null;

        AntlrDatatypeRuleToken lv_excludes_5_0 = null;

        AntlrDatatypeRuleToken lv_name_7_0 = null;

        AntlrDatatypeRuleToken lv_name_11_0 = null;

        AntlrDatatypeRuleToken lv_name_16_0 = null;

        AntlrDatatypeRuleToken lv_name_21_0 = null;

        AntlrDatatypeRuleToken lv_name_28_0 = null;

        AntlrDatatypeRuleToken lv_name_36_0 = null;

        AntlrDatatypeRuleToken lv_name_44_0 = null;

        AntlrDatatypeRuleToken lv_name_52_0 = null;

        AntlrDatatypeRuleToken lv_excludes_60_0 = null;

        AntlrDatatypeRuleToken lv_name_62_0 = null;

        AntlrDatatypeRuleToken lv_name_67_0 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:3397:2: ( ( ( () ( (lv_name_1_0= ruleWildID ) ) ) | ( () otherlv_3= 'EXCLUDE_FILE' otherlv_4= '(' ( (lv_excludes_5_0= ruleWildID ) )+ otherlv_6= ')' ( (lv_name_7_0= ruleWildID ) ) ) | ( () ruleSORT_BY_NAME otherlv_10= '(' ( (lv_name_11_0= ruleWildID ) ) otherlv_12= ')' ) | ( () otherlv_14= 'SORT_BY_ALIGNMENT' otherlv_15= '(' ( (lv_name_16_0= ruleWildID ) ) otherlv_17= ')' ) | ( () otherlv_19= 'SORT_NONE' otherlv_20= '(' ( (lv_name_21_0= ruleWildID ) ) otherlv_22= ')' ) | ( () ruleSORT_BY_NAME otherlv_25= '(' otherlv_26= 'SORT_BY_ALIGNMENT' otherlv_27= '(' ( (lv_name_28_0= ruleWildID ) ) otherlv_29= ')' otherlv_30= ')' ) | ( () ruleSORT_BY_NAME otherlv_33= '(' ruleSORT_BY_NAME otherlv_35= '(' ( (lv_name_36_0= ruleWildID ) ) otherlv_37= ')' otherlv_38= ')' ) | ( () otherlv_40= 'SORT_BY_ALIGNMENT' otherlv_41= '(' ruleSORT_BY_NAME otherlv_43= '(' ( (lv_name_44_0= ruleWildID ) ) otherlv_45= ')' otherlv_46= ')' ) | ( () otherlv_48= 'SORT_BY_ALIGNMENT' otherlv_49= '(' otherlv_50= 'SORT_BY_ALIGNMENT' otherlv_51= '(' ( (lv_name_52_0= ruleWildID ) ) otherlv_53= ')' otherlv_54= ')' ) | ( () ruleSORT_BY_NAME otherlv_57= '(' otherlv_58= 'EXCLUDE_FILE' otherlv_59= '(' ( (lv_excludes_60_0= ruleWildID ) )+ otherlv_61= ')' ( (lv_name_62_0= ruleWildID ) ) otherlv_63= ')' ) | ( () otherlv_65= 'SORT_BY_INIT_PRIORITY' otherlv_66= '(' ( (lv_name_67_0= ruleWildID ) ) otherlv_68= ')' ) ) )
            // InternalLinkerScript.g:3398:2: ( ( () ( (lv_name_1_0= ruleWildID ) ) ) | ( () otherlv_3= 'EXCLUDE_FILE' otherlv_4= '(' ( (lv_excludes_5_0= ruleWildID ) )+ otherlv_6= ')' ( (lv_name_7_0= ruleWildID ) ) ) | ( () ruleSORT_BY_NAME otherlv_10= '(' ( (lv_name_11_0= ruleWildID ) ) otherlv_12= ')' ) | ( () otherlv_14= 'SORT_BY_ALIGNMENT' otherlv_15= '(' ( (lv_name_16_0= ruleWildID ) ) otherlv_17= ')' ) | ( () otherlv_19= 'SORT_NONE' otherlv_20= '(' ( (lv_name_21_0= ruleWildID ) ) otherlv_22= ')' ) | ( () ruleSORT_BY_NAME otherlv_25= '(' otherlv_26= 'SORT_BY_ALIGNMENT' otherlv_27= '(' ( (lv_name_28_0= ruleWildID ) ) otherlv_29= ')' otherlv_30= ')' ) | ( () ruleSORT_BY_NAME otherlv_33= '(' ruleSORT_BY_NAME otherlv_35= '(' ( (lv_name_36_0= ruleWildID ) ) otherlv_37= ')' otherlv_38= ')' ) | ( () otherlv_40= 'SORT_BY_ALIGNMENT' otherlv_41= '(' ruleSORT_BY_NAME otherlv_43= '(' ( (lv_name_44_0= ruleWildID ) ) otherlv_45= ')' otherlv_46= ')' ) | ( () otherlv_48= 'SORT_BY_ALIGNMENT' otherlv_49= '(' otherlv_50= 'SORT_BY_ALIGNMENT' otherlv_51= '(' ( (lv_name_52_0= ruleWildID ) ) otherlv_53= ')' otherlv_54= ')' ) | ( () ruleSORT_BY_NAME otherlv_57= '(' otherlv_58= 'EXCLUDE_FILE' otherlv_59= '(' ( (lv_excludes_60_0= ruleWildID ) )+ otherlv_61= ')' ( (lv_name_62_0= ruleWildID ) ) otherlv_63= ')' ) | ( () otherlv_65= 'SORT_BY_INIT_PRIORITY' otherlv_66= '(' ( (lv_name_67_0= ruleWildID ) ) otherlv_68= ')' ) )
            {
            // InternalLinkerScript.g:3398:2: ( ( () ( (lv_name_1_0= ruleWildID ) ) ) | ( () otherlv_3= 'EXCLUDE_FILE' otherlv_4= '(' ( (lv_excludes_5_0= ruleWildID ) )+ otherlv_6= ')' ( (lv_name_7_0= ruleWildID ) ) ) | ( () ruleSORT_BY_NAME otherlv_10= '(' ( (lv_name_11_0= ruleWildID ) ) otherlv_12= ')' ) | ( () otherlv_14= 'SORT_BY_ALIGNMENT' otherlv_15= '(' ( (lv_name_16_0= ruleWildID ) ) otherlv_17= ')' ) | ( () otherlv_19= 'SORT_NONE' otherlv_20= '(' ( (lv_name_21_0= ruleWildID ) ) otherlv_22= ')' ) | ( () ruleSORT_BY_NAME otherlv_25= '(' otherlv_26= 'SORT_BY_ALIGNMENT' otherlv_27= '(' ( (lv_name_28_0= ruleWildID ) ) otherlv_29= ')' otherlv_30= ')' ) | ( () ruleSORT_BY_NAME otherlv_33= '(' ruleSORT_BY_NAME otherlv_35= '(' ( (lv_name_36_0= ruleWildID ) ) otherlv_37= ')' otherlv_38= ')' ) | ( () otherlv_40= 'SORT_BY_ALIGNMENT' otherlv_41= '(' ruleSORT_BY_NAME otherlv_43= '(' ( (lv_name_44_0= ruleWildID ) ) otherlv_45= ')' otherlv_46= ')' ) | ( () otherlv_48= 'SORT_BY_ALIGNMENT' otherlv_49= '(' otherlv_50= 'SORT_BY_ALIGNMENT' otherlv_51= '(' ( (lv_name_52_0= ruleWildID ) ) otherlv_53= ')' otherlv_54= ')' ) | ( () ruleSORT_BY_NAME otherlv_57= '(' otherlv_58= 'EXCLUDE_FILE' otherlv_59= '(' ( (lv_excludes_60_0= ruleWildID ) )+ otherlv_61= ')' ( (lv_name_62_0= ruleWildID ) ) otherlv_63= ')' ) | ( () otherlv_65= 'SORT_BY_INIT_PRIORITY' otherlv_66= '(' ( (lv_name_67_0= ruleWildID ) ) otherlv_68= ')' ) )
            int alt56=11;
            alt56 = dfa56.predict(input);
            switch (alt56) {
                case 1 :
                    // InternalLinkerScript.g:3399:3: ( () ( (lv_name_1_0= ruleWildID ) ) )
                    {
                    // InternalLinkerScript.g:3399:3: ( () ( (lv_name_1_0= ruleWildID ) ) )
                    // InternalLinkerScript.g:3400:4: () ( (lv_name_1_0= ruleWildID ) )
                    {
                    // InternalLinkerScript.g:3400:4: ()
                    // InternalLinkerScript.g:3401:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getWildcardRuleAccess().getWildcardSortNoneAction_0_0(),
                      						current);
                      				
                    }

                    }

                    // InternalLinkerScript.g:3407:4: ( (lv_name_1_0= ruleWildID ) )
                    // InternalLinkerScript.g:3408:5: (lv_name_1_0= ruleWildID )
                    {
                    // InternalLinkerScript.g:3408:5: (lv_name_1_0= ruleWildID )
                    // InternalLinkerScript.g:3409:6: lv_name_1_0= ruleWildID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getWildcardRuleAccess().getNameWildIDParserRuleCall_0_1_0());
                      					
                    }
                    pushFollow(FOLLOW_2);
                    lv_name_1_0=ruleWildID();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getWildcardRuleRule());
                      						}
                      						set(
                      							current,
                      							"name",
                      							lv_name_1_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.WildID");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }


                    }


                    }
                    break;
                case 2 :
                    // InternalLinkerScript.g:3428:3: ( () otherlv_3= 'EXCLUDE_FILE' otherlv_4= '(' ( (lv_excludes_5_0= ruleWildID ) )+ otherlv_6= ')' ( (lv_name_7_0= ruleWildID ) ) )
                    {
                    // InternalLinkerScript.g:3428:3: ( () otherlv_3= 'EXCLUDE_FILE' otherlv_4= '(' ( (lv_excludes_5_0= ruleWildID ) )+ otherlv_6= ')' ( (lv_name_7_0= ruleWildID ) ) )
                    // InternalLinkerScript.g:3429:4: () otherlv_3= 'EXCLUDE_FILE' otherlv_4= '(' ( (lv_excludes_5_0= ruleWildID ) )+ otherlv_6= ')' ( (lv_name_7_0= ruleWildID ) )
                    {
                    // InternalLinkerScript.g:3429:4: ()
                    // InternalLinkerScript.g:3430:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getWildcardRuleAccess().getWildcardSortNoneAction_1_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_3=(Token)match(input,75,FOLLOW_5); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_3, grammarAccess.getWildcardRuleAccess().getEXCLUDE_FILEKeyword_1_1());
                      			
                    }
                    otherlv_4=(Token)match(input,13,FOLLOW_6); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_4, grammarAccess.getWildcardRuleAccess().getLeftParenthesisKeyword_1_2());
                      			
                    }
                    // InternalLinkerScript.g:3444:4: ( (lv_excludes_5_0= ruleWildID ) )+
                    int cnt54=0;
                    loop54:
                    do {
                        int alt54=2;
                        int LA54_0 = input.LA(1);

                        if ( (LA54_0==RULE_ID||LA54_0==80||(LA54_0>=82 && LA54_0<=83)||(LA54_0>=85 && LA54_0<=86)||LA54_0==96) ) {
                            alt54=1;
                        }


                        switch (alt54) {
                    	case 1 :
                    	    // InternalLinkerScript.g:3445:5: (lv_excludes_5_0= ruleWildID )
                    	    {
                    	    // InternalLinkerScript.g:3445:5: (lv_excludes_5_0= ruleWildID )
                    	    // InternalLinkerScript.g:3446:6: lv_excludes_5_0= ruleWildID
                    	    {
                    	    if ( state.backtracking==0 ) {

                    	      						newCompositeNode(grammarAccess.getWildcardRuleAccess().getExcludesWildIDParserRuleCall_1_3_0());
                    	      					
                    	    }
                    	    pushFollow(FOLLOW_14);
                    	    lv_excludes_5_0=ruleWildID();

                    	    state._fsp--;
                    	    if (state.failed) return current;
                    	    if ( state.backtracking==0 ) {

                    	      						if (current==null) {
                    	      							current = createModelElementForParent(grammarAccess.getWildcardRuleRule());
                    	      						}
                    	      						add(
                    	      							current,
                    	      							"excludes",
                    	      							lv_excludes_5_0,
                    	      							"org.eclipse.cdt.linkerscript.LinkerScript.WildID");
                    	      						afterParserOrEnumRuleCall();
                    	      					
                    	    }

                    	    }


                    	    }
                    	    break;

                    	default :
                    	    if ( cnt54 >= 1 ) break loop54;
                    	    if (state.backtracking>0) {state.failed=true; return current;}
                                EarlyExitException eee =
                                    new EarlyExitException(54, input);
                                throw eee;
                        }
                        cnt54++;
                    } while (true);

                    otherlv_6=(Token)match(input,14,FOLLOW_6); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_6, grammarAccess.getWildcardRuleAccess().getRightParenthesisKeyword_1_4());
                      			
                    }
                    // InternalLinkerScript.g:3467:4: ( (lv_name_7_0= ruleWildID ) )
                    // InternalLinkerScript.g:3468:5: (lv_name_7_0= ruleWildID )
                    {
                    // InternalLinkerScript.g:3468:5: (lv_name_7_0= ruleWildID )
                    // InternalLinkerScript.g:3469:6: lv_name_7_0= ruleWildID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getWildcardRuleAccess().getNameWildIDParserRuleCall_1_5_0());
                      					
                    }
                    pushFollow(FOLLOW_2);
                    lv_name_7_0=ruleWildID();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getWildcardRuleRule());
                      						}
                      						set(
                      							current,
                      							"name",
                      							lv_name_7_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.WildID");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }


                    }


                    }
                    break;
                case 3 :
                    // InternalLinkerScript.g:3488:3: ( () ruleSORT_BY_NAME otherlv_10= '(' ( (lv_name_11_0= ruleWildID ) ) otherlv_12= ')' )
                    {
                    // InternalLinkerScript.g:3488:3: ( () ruleSORT_BY_NAME otherlv_10= '(' ( (lv_name_11_0= ruleWildID ) ) otherlv_12= ')' )
                    // InternalLinkerScript.g:3489:4: () ruleSORT_BY_NAME otherlv_10= '(' ( (lv_name_11_0= ruleWildID ) ) otherlv_12= ')'
                    {
                    // InternalLinkerScript.g:3489:4: ()
                    // InternalLinkerScript.g:3490:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getWildcardRuleAccess().getWildcardSortNameAction_2_0(),
                      						current);
                      				
                    }

                    }

                    if ( state.backtracking==0 ) {

                      				newCompositeNode(grammarAccess.getWildcardRuleAccess().getSORT_BY_NAMEParserRuleCall_2_1());
                      			
                    }
                    pushFollow(FOLLOW_5);
                    ruleSORT_BY_NAME();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				afterParserOrEnumRuleCall();
                      			
                    }
                    otherlv_10=(Token)match(input,13,FOLLOW_6); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_10, grammarAccess.getWildcardRuleAccess().getLeftParenthesisKeyword_2_2());
                      			
                    }
                    // InternalLinkerScript.g:3507:4: ( (lv_name_11_0= ruleWildID ) )
                    // InternalLinkerScript.g:3508:5: (lv_name_11_0= ruleWildID )
                    {
                    // InternalLinkerScript.g:3508:5: (lv_name_11_0= ruleWildID )
                    // InternalLinkerScript.g:3509:6: lv_name_11_0= ruleWildID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getWildcardRuleAccess().getNameWildIDParserRuleCall_2_3_0());
                      					
                    }
                    pushFollow(FOLLOW_7);
                    lv_name_11_0=ruleWildID();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getWildcardRuleRule());
                      						}
                      						set(
                      							current,
                      							"name",
                      							lv_name_11_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.WildID");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    otherlv_12=(Token)match(input,14,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_12, grammarAccess.getWildcardRuleAccess().getRightParenthesisKeyword_2_4());
                      			
                    }

                    }


                    }
                    break;
                case 4 :
                    // InternalLinkerScript.g:3532:3: ( () otherlv_14= 'SORT_BY_ALIGNMENT' otherlv_15= '(' ( (lv_name_16_0= ruleWildID ) ) otherlv_17= ')' )
                    {
                    // InternalLinkerScript.g:3532:3: ( () otherlv_14= 'SORT_BY_ALIGNMENT' otherlv_15= '(' ( (lv_name_16_0= ruleWildID ) ) otherlv_17= ')' )
                    // InternalLinkerScript.g:3533:4: () otherlv_14= 'SORT_BY_ALIGNMENT' otherlv_15= '(' ( (lv_name_16_0= ruleWildID ) ) otherlv_17= ')'
                    {
                    // InternalLinkerScript.g:3533:4: ()
                    // InternalLinkerScript.g:3534:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getWildcardRuleAccess().getWildcardSortAlignAction_3_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_14=(Token)match(input,76,FOLLOW_5); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_14, grammarAccess.getWildcardRuleAccess().getSORT_BY_ALIGNMENTKeyword_3_1());
                      			
                    }
                    otherlv_15=(Token)match(input,13,FOLLOW_6); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_15, grammarAccess.getWildcardRuleAccess().getLeftParenthesisKeyword_3_2());
                      			
                    }
                    // InternalLinkerScript.g:3548:4: ( (lv_name_16_0= ruleWildID ) )
                    // InternalLinkerScript.g:3549:5: (lv_name_16_0= ruleWildID )
                    {
                    // InternalLinkerScript.g:3549:5: (lv_name_16_0= ruleWildID )
                    // InternalLinkerScript.g:3550:6: lv_name_16_0= ruleWildID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getWildcardRuleAccess().getNameWildIDParserRuleCall_3_3_0());
                      					
                    }
                    pushFollow(FOLLOW_7);
                    lv_name_16_0=ruleWildID();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getWildcardRuleRule());
                      						}
                      						set(
                      							current,
                      							"name",
                      							lv_name_16_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.WildID");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    otherlv_17=(Token)match(input,14,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_17, grammarAccess.getWildcardRuleAccess().getRightParenthesisKeyword_3_4());
                      			
                    }

                    }


                    }
                    break;
                case 5 :
                    // InternalLinkerScript.g:3573:3: ( () otherlv_19= 'SORT_NONE' otherlv_20= '(' ( (lv_name_21_0= ruleWildID ) ) otherlv_22= ')' )
                    {
                    // InternalLinkerScript.g:3573:3: ( () otherlv_19= 'SORT_NONE' otherlv_20= '(' ( (lv_name_21_0= ruleWildID ) ) otherlv_22= ')' )
                    // InternalLinkerScript.g:3574:4: () otherlv_19= 'SORT_NONE' otherlv_20= '(' ( (lv_name_21_0= ruleWildID ) ) otherlv_22= ')'
                    {
                    // InternalLinkerScript.g:3574:4: ()
                    // InternalLinkerScript.g:3575:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getWildcardRuleAccess().getWildcardSortNoneAction_4_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_19=(Token)match(input,77,FOLLOW_5); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_19, grammarAccess.getWildcardRuleAccess().getSORT_NONEKeyword_4_1());
                      			
                    }
                    otherlv_20=(Token)match(input,13,FOLLOW_6); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_20, grammarAccess.getWildcardRuleAccess().getLeftParenthesisKeyword_4_2());
                      			
                    }
                    // InternalLinkerScript.g:3589:4: ( (lv_name_21_0= ruleWildID ) )
                    // InternalLinkerScript.g:3590:5: (lv_name_21_0= ruleWildID )
                    {
                    // InternalLinkerScript.g:3590:5: (lv_name_21_0= ruleWildID )
                    // InternalLinkerScript.g:3591:6: lv_name_21_0= ruleWildID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getWildcardRuleAccess().getNameWildIDParserRuleCall_4_3_0());
                      					
                    }
                    pushFollow(FOLLOW_7);
                    lv_name_21_0=ruleWildID();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getWildcardRuleRule());
                      						}
                      						set(
                      							current,
                      							"name",
                      							lv_name_21_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.WildID");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    otherlv_22=(Token)match(input,14,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_22, grammarAccess.getWildcardRuleAccess().getRightParenthesisKeyword_4_4());
                      			
                    }

                    }


                    }
                    break;
                case 6 :
                    // InternalLinkerScript.g:3614:3: ( () ruleSORT_BY_NAME otherlv_25= '(' otherlv_26= 'SORT_BY_ALIGNMENT' otherlv_27= '(' ( (lv_name_28_0= ruleWildID ) ) otherlv_29= ')' otherlv_30= ')' )
                    {
                    // InternalLinkerScript.g:3614:3: ( () ruleSORT_BY_NAME otherlv_25= '(' otherlv_26= 'SORT_BY_ALIGNMENT' otherlv_27= '(' ( (lv_name_28_0= ruleWildID ) ) otherlv_29= ')' otherlv_30= ')' )
                    // InternalLinkerScript.g:3615:4: () ruleSORT_BY_NAME otherlv_25= '(' otherlv_26= 'SORT_BY_ALIGNMENT' otherlv_27= '(' ( (lv_name_28_0= ruleWildID ) ) otherlv_29= ')' otherlv_30= ')'
                    {
                    // InternalLinkerScript.g:3615:4: ()
                    // InternalLinkerScript.g:3616:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getWildcardRuleAccess().getWildcardSortNameAlignAction_5_0(),
                      						current);
                      				
                    }

                    }

                    if ( state.backtracking==0 ) {

                      				newCompositeNode(grammarAccess.getWildcardRuleAccess().getSORT_BY_NAMEParserRuleCall_5_1());
                      			
                    }
                    pushFollow(FOLLOW_5);
                    ruleSORT_BY_NAME();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				afterParserOrEnumRuleCall();
                      			
                    }
                    otherlv_25=(Token)match(input,13,FOLLOW_43); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_25, grammarAccess.getWildcardRuleAccess().getLeftParenthesisKeyword_5_2());
                      			
                    }
                    otherlv_26=(Token)match(input,76,FOLLOW_5); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_26, grammarAccess.getWildcardRuleAccess().getSORT_BY_ALIGNMENTKeyword_5_3());
                      			
                    }
                    otherlv_27=(Token)match(input,13,FOLLOW_6); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_27, grammarAccess.getWildcardRuleAccess().getLeftParenthesisKeyword_5_4());
                      			
                    }
                    // InternalLinkerScript.g:3641:4: ( (lv_name_28_0= ruleWildID ) )
                    // InternalLinkerScript.g:3642:5: (lv_name_28_0= ruleWildID )
                    {
                    // InternalLinkerScript.g:3642:5: (lv_name_28_0= ruleWildID )
                    // InternalLinkerScript.g:3643:6: lv_name_28_0= ruleWildID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getWildcardRuleAccess().getNameWildIDParserRuleCall_5_5_0());
                      					
                    }
                    pushFollow(FOLLOW_7);
                    lv_name_28_0=ruleWildID();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getWildcardRuleRule());
                      						}
                      						set(
                      							current,
                      							"name",
                      							lv_name_28_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.WildID");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    otherlv_29=(Token)match(input,14,FOLLOW_7); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_29, grammarAccess.getWildcardRuleAccess().getRightParenthesisKeyword_5_6());
                      			
                    }
                    otherlv_30=(Token)match(input,14,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_30, grammarAccess.getWildcardRuleAccess().getRightParenthesisKeyword_5_7());
                      			
                    }

                    }


                    }
                    break;
                case 7 :
                    // InternalLinkerScript.g:3670:3: ( () ruleSORT_BY_NAME otherlv_33= '(' ruleSORT_BY_NAME otherlv_35= '(' ( (lv_name_36_0= ruleWildID ) ) otherlv_37= ')' otherlv_38= ')' )
                    {
                    // InternalLinkerScript.g:3670:3: ( () ruleSORT_BY_NAME otherlv_33= '(' ruleSORT_BY_NAME otherlv_35= '(' ( (lv_name_36_0= ruleWildID ) ) otherlv_37= ')' otherlv_38= ')' )
                    // InternalLinkerScript.g:3671:4: () ruleSORT_BY_NAME otherlv_33= '(' ruleSORT_BY_NAME otherlv_35= '(' ( (lv_name_36_0= ruleWildID ) ) otherlv_37= ')' otherlv_38= ')'
                    {
                    // InternalLinkerScript.g:3671:4: ()
                    // InternalLinkerScript.g:3672:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getWildcardRuleAccess().getWildcardSortNameAction_6_0(),
                      						current);
                      				
                    }

                    }

                    if ( state.backtracking==0 ) {

                      				newCompositeNode(grammarAccess.getWildcardRuleAccess().getSORT_BY_NAMEParserRuleCall_6_1());
                      			
                    }
                    pushFollow(FOLLOW_5);
                    ruleSORT_BY_NAME();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				afterParserOrEnumRuleCall();
                      			
                    }
                    otherlv_33=(Token)match(input,13,FOLLOW_44); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_33, grammarAccess.getWildcardRuleAccess().getLeftParenthesisKeyword_6_2());
                      			
                    }
                    if ( state.backtracking==0 ) {

                      				newCompositeNode(grammarAccess.getWildcardRuleAccess().getSORT_BY_NAMEParserRuleCall_6_3());
                      			
                    }
                    pushFollow(FOLLOW_5);
                    ruleSORT_BY_NAME();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				afterParserOrEnumRuleCall();
                      			
                    }
                    otherlv_35=(Token)match(input,13,FOLLOW_6); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_35, grammarAccess.getWildcardRuleAccess().getLeftParenthesisKeyword_6_4());
                      			
                    }
                    // InternalLinkerScript.g:3700:4: ( (lv_name_36_0= ruleWildID ) )
                    // InternalLinkerScript.g:3701:5: (lv_name_36_0= ruleWildID )
                    {
                    // InternalLinkerScript.g:3701:5: (lv_name_36_0= ruleWildID )
                    // InternalLinkerScript.g:3702:6: lv_name_36_0= ruleWildID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getWildcardRuleAccess().getNameWildIDParserRuleCall_6_5_0());
                      					
                    }
                    pushFollow(FOLLOW_7);
                    lv_name_36_0=ruleWildID();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getWildcardRuleRule());
                      						}
                      						set(
                      							current,
                      							"name",
                      							lv_name_36_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.WildID");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    otherlv_37=(Token)match(input,14,FOLLOW_7); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_37, grammarAccess.getWildcardRuleAccess().getRightParenthesisKeyword_6_6());
                      			
                    }
                    otherlv_38=(Token)match(input,14,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_38, grammarAccess.getWildcardRuleAccess().getRightParenthesisKeyword_6_7());
                      			
                    }

                    }


                    }
                    break;
                case 8 :
                    // InternalLinkerScript.g:3729:3: ( () otherlv_40= 'SORT_BY_ALIGNMENT' otherlv_41= '(' ruleSORT_BY_NAME otherlv_43= '(' ( (lv_name_44_0= ruleWildID ) ) otherlv_45= ')' otherlv_46= ')' )
                    {
                    // InternalLinkerScript.g:3729:3: ( () otherlv_40= 'SORT_BY_ALIGNMENT' otherlv_41= '(' ruleSORT_BY_NAME otherlv_43= '(' ( (lv_name_44_0= ruleWildID ) ) otherlv_45= ')' otherlv_46= ')' )
                    // InternalLinkerScript.g:3730:4: () otherlv_40= 'SORT_BY_ALIGNMENT' otherlv_41= '(' ruleSORT_BY_NAME otherlv_43= '(' ( (lv_name_44_0= ruleWildID ) ) otherlv_45= ')' otherlv_46= ')'
                    {
                    // InternalLinkerScript.g:3730:4: ()
                    // InternalLinkerScript.g:3731:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getWildcardRuleAccess().getWildcardSortAlignNameAction_7_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_40=(Token)match(input,76,FOLLOW_5); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_40, grammarAccess.getWildcardRuleAccess().getSORT_BY_ALIGNMENTKeyword_7_1());
                      			
                    }
                    otherlv_41=(Token)match(input,13,FOLLOW_44); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_41, grammarAccess.getWildcardRuleAccess().getLeftParenthesisKeyword_7_2());
                      			
                    }
                    if ( state.backtracking==0 ) {

                      				newCompositeNode(grammarAccess.getWildcardRuleAccess().getSORT_BY_NAMEParserRuleCall_7_3());
                      			
                    }
                    pushFollow(FOLLOW_5);
                    ruleSORT_BY_NAME();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				afterParserOrEnumRuleCall();
                      			
                    }
                    otherlv_43=(Token)match(input,13,FOLLOW_6); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_43, grammarAccess.getWildcardRuleAccess().getLeftParenthesisKeyword_7_4());
                      			
                    }
                    // InternalLinkerScript.g:3756:4: ( (lv_name_44_0= ruleWildID ) )
                    // InternalLinkerScript.g:3757:5: (lv_name_44_0= ruleWildID )
                    {
                    // InternalLinkerScript.g:3757:5: (lv_name_44_0= ruleWildID )
                    // InternalLinkerScript.g:3758:6: lv_name_44_0= ruleWildID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getWildcardRuleAccess().getNameWildIDParserRuleCall_7_5_0());
                      					
                    }
                    pushFollow(FOLLOW_7);
                    lv_name_44_0=ruleWildID();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getWildcardRuleRule());
                      						}
                      						set(
                      							current,
                      							"name",
                      							lv_name_44_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.WildID");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    otherlv_45=(Token)match(input,14,FOLLOW_7); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_45, grammarAccess.getWildcardRuleAccess().getRightParenthesisKeyword_7_6());
                      			
                    }
                    otherlv_46=(Token)match(input,14,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_46, grammarAccess.getWildcardRuleAccess().getRightParenthesisKeyword_7_7());
                      			
                    }

                    }


                    }
                    break;
                case 9 :
                    // InternalLinkerScript.g:3785:3: ( () otherlv_48= 'SORT_BY_ALIGNMENT' otherlv_49= '(' otherlv_50= 'SORT_BY_ALIGNMENT' otherlv_51= '(' ( (lv_name_52_0= ruleWildID ) ) otherlv_53= ')' otherlv_54= ')' )
                    {
                    // InternalLinkerScript.g:3785:3: ( () otherlv_48= 'SORT_BY_ALIGNMENT' otherlv_49= '(' otherlv_50= 'SORT_BY_ALIGNMENT' otherlv_51= '(' ( (lv_name_52_0= ruleWildID ) ) otherlv_53= ')' otherlv_54= ')' )
                    // InternalLinkerScript.g:3786:4: () otherlv_48= 'SORT_BY_ALIGNMENT' otherlv_49= '(' otherlv_50= 'SORT_BY_ALIGNMENT' otherlv_51= '(' ( (lv_name_52_0= ruleWildID ) ) otherlv_53= ')' otherlv_54= ')'
                    {
                    // InternalLinkerScript.g:3786:4: ()
                    // InternalLinkerScript.g:3787:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getWildcardRuleAccess().getWildcardSortAlignAction_8_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_48=(Token)match(input,76,FOLLOW_5); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_48, grammarAccess.getWildcardRuleAccess().getSORT_BY_ALIGNMENTKeyword_8_1());
                      			
                    }
                    otherlv_49=(Token)match(input,13,FOLLOW_43); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_49, grammarAccess.getWildcardRuleAccess().getLeftParenthesisKeyword_8_2());
                      			
                    }
                    otherlv_50=(Token)match(input,76,FOLLOW_5); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_50, grammarAccess.getWildcardRuleAccess().getSORT_BY_ALIGNMENTKeyword_8_3());
                      			
                    }
                    otherlv_51=(Token)match(input,13,FOLLOW_6); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_51, grammarAccess.getWildcardRuleAccess().getLeftParenthesisKeyword_8_4());
                      			
                    }
                    // InternalLinkerScript.g:3809:4: ( (lv_name_52_0= ruleWildID ) )
                    // InternalLinkerScript.g:3810:5: (lv_name_52_0= ruleWildID )
                    {
                    // InternalLinkerScript.g:3810:5: (lv_name_52_0= ruleWildID )
                    // InternalLinkerScript.g:3811:6: lv_name_52_0= ruleWildID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getWildcardRuleAccess().getNameWildIDParserRuleCall_8_5_0());
                      					
                    }
                    pushFollow(FOLLOW_7);
                    lv_name_52_0=ruleWildID();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getWildcardRuleRule());
                      						}
                      						set(
                      							current,
                      							"name",
                      							lv_name_52_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.WildID");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    otherlv_53=(Token)match(input,14,FOLLOW_7); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_53, grammarAccess.getWildcardRuleAccess().getRightParenthesisKeyword_8_6());
                      			
                    }
                    otherlv_54=(Token)match(input,14,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_54, grammarAccess.getWildcardRuleAccess().getRightParenthesisKeyword_8_7());
                      			
                    }

                    }


                    }
                    break;
                case 10 :
                    // InternalLinkerScript.g:3838:3: ( () ruleSORT_BY_NAME otherlv_57= '(' otherlv_58= 'EXCLUDE_FILE' otherlv_59= '(' ( (lv_excludes_60_0= ruleWildID ) )+ otherlv_61= ')' ( (lv_name_62_0= ruleWildID ) ) otherlv_63= ')' )
                    {
                    // InternalLinkerScript.g:3838:3: ( () ruleSORT_BY_NAME otherlv_57= '(' otherlv_58= 'EXCLUDE_FILE' otherlv_59= '(' ( (lv_excludes_60_0= ruleWildID ) )+ otherlv_61= ')' ( (lv_name_62_0= ruleWildID ) ) otherlv_63= ')' )
                    // InternalLinkerScript.g:3839:4: () ruleSORT_BY_NAME otherlv_57= '(' otherlv_58= 'EXCLUDE_FILE' otherlv_59= '(' ( (lv_excludes_60_0= ruleWildID ) )+ otherlv_61= ')' ( (lv_name_62_0= ruleWildID ) ) otherlv_63= ')'
                    {
                    // InternalLinkerScript.g:3839:4: ()
                    // InternalLinkerScript.g:3840:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getWildcardRuleAccess().getWildcardSortNameAction_9_0(),
                      						current);
                      				
                    }

                    }

                    if ( state.backtracking==0 ) {

                      				newCompositeNode(grammarAccess.getWildcardRuleAccess().getSORT_BY_NAMEParserRuleCall_9_1());
                      			
                    }
                    pushFollow(FOLLOW_5);
                    ruleSORT_BY_NAME();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				afterParserOrEnumRuleCall();
                      			
                    }
                    otherlv_57=(Token)match(input,13,FOLLOW_45); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_57, grammarAccess.getWildcardRuleAccess().getLeftParenthesisKeyword_9_2());
                      			
                    }
                    otherlv_58=(Token)match(input,75,FOLLOW_5); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_58, grammarAccess.getWildcardRuleAccess().getEXCLUDE_FILEKeyword_9_3());
                      			
                    }
                    otherlv_59=(Token)match(input,13,FOLLOW_6); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_59, grammarAccess.getWildcardRuleAccess().getLeftParenthesisKeyword_9_4());
                      			
                    }
                    // InternalLinkerScript.g:3865:4: ( (lv_excludes_60_0= ruleWildID ) )+
                    int cnt55=0;
                    loop55:
                    do {
                        int alt55=2;
                        int LA55_0 = input.LA(1);

                        if ( (LA55_0==RULE_ID||LA55_0==80||(LA55_0>=82 && LA55_0<=83)||(LA55_0>=85 && LA55_0<=86)||LA55_0==96) ) {
                            alt55=1;
                        }


                        switch (alt55) {
                    	case 1 :
                    	    // InternalLinkerScript.g:3866:5: (lv_excludes_60_0= ruleWildID )
                    	    {
                    	    // InternalLinkerScript.g:3866:5: (lv_excludes_60_0= ruleWildID )
                    	    // InternalLinkerScript.g:3867:6: lv_excludes_60_0= ruleWildID
                    	    {
                    	    if ( state.backtracking==0 ) {

                    	      						newCompositeNode(grammarAccess.getWildcardRuleAccess().getExcludesWildIDParserRuleCall_9_5_0());
                    	      					
                    	    }
                    	    pushFollow(FOLLOW_14);
                    	    lv_excludes_60_0=ruleWildID();

                    	    state._fsp--;
                    	    if (state.failed) return current;
                    	    if ( state.backtracking==0 ) {

                    	      						if (current==null) {
                    	      							current = createModelElementForParent(grammarAccess.getWildcardRuleRule());
                    	      						}
                    	      						add(
                    	      							current,
                    	      							"excludes",
                    	      							lv_excludes_60_0,
                    	      							"org.eclipse.cdt.linkerscript.LinkerScript.WildID");
                    	      						afterParserOrEnumRuleCall();
                    	      					
                    	    }

                    	    }


                    	    }
                    	    break;

                    	default :
                    	    if ( cnt55 >= 1 ) break loop55;
                    	    if (state.backtracking>0) {state.failed=true; return current;}
                                EarlyExitException eee =
                                    new EarlyExitException(55, input);
                                throw eee;
                        }
                        cnt55++;
                    } while (true);

                    otherlv_61=(Token)match(input,14,FOLLOW_6); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_61, grammarAccess.getWildcardRuleAccess().getRightParenthesisKeyword_9_6());
                      			
                    }
                    // InternalLinkerScript.g:3888:4: ( (lv_name_62_0= ruleWildID ) )
                    // InternalLinkerScript.g:3889:5: (lv_name_62_0= ruleWildID )
                    {
                    // InternalLinkerScript.g:3889:5: (lv_name_62_0= ruleWildID )
                    // InternalLinkerScript.g:3890:6: lv_name_62_0= ruleWildID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getWildcardRuleAccess().getNameWildIDParserRuleCall_9_7_0());
                      					
                    }
                    pushFollow(FOLLOW_7);
                    lv_name_62_0=ruleWildID();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getWildcardRuleRule());
                      						}
                      						set(
                      							current,
                      							"name",
                      							lv_name_62_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.WildID");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    otherlv_63=(Token)match(input,14,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_63, grammarAccess.getWildcardRuleAccess().getRightParenthesisKeyword_9_8());
                      			
                    }

                    }


                    }
                    break;
                case 11 :
                    // InternalLinkerScript.g:3913:3: ( () otherlv_65= 'SORT_BY_INIT_PRIORITY' otherlv_66= '(' ( (lv_name_67_0= ruleWildID ) ) otherlv_68= ')' )
                    {
                    // InternalLinkerScript.g:3913:3: ( () otherlv_65= 'SORT_BY_INIT_PRIORITY' otherlv_66= '(' ( (lv_name_67_0= ruleWildID ) ) otherlv_68= ')' )
                    // InternalLinkerScript.g:3914:4: () otherlv_65= 'SORT_BY_INIT_PRIORITY' otherlv_66= '(' ( (lv_name_67_0= ruleWildID ) ) otherlv_68= ')'
                    {
                    // InternalLinkerScript.g:3914:4: ()
                    // InternalLinkerScript.g:3915:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getWildcardRuleAccess().getWildcardSortInitPriorityAction_10_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_65=(Token)match(input,78,FOLLOW_5); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_65, grammarAccess.getWildcardRuleAccess().getSORT_BY_INIT_PRIORITYKeyword_10_1());
                      			
                    }
                    otherlv_66=(Token)match(input,13,FOLLOW_6); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_66, grammarAccess.getWildcardRuleAccess().getLeftParenthesisKeyword_10_2());
                      			
                    }
                    // InternalLinkerScript.g:3929:4: ( (lv_name_67_0= ruleWildID ) )
                    // InternalLinkerScript.g:3930:5: (lv_name_67_0= ruleWildID )
                    {
                    // InternalLinkerScript.g:3930:5: (lv_name_67_0= ruleWildID )
                    // InternalLinkerScript.g:3931:6: lv_name_67_0= ruleWildID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getWildcardRuleAccess().getNameWildIDParserRuleCall_10_3_0());
                      					
                    }
                    pushFollow(FOLLOW_7);
                    lv_name_67_0=ruleWildID();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getWildcardRuleRule());
                      						}
                      						set(
                      							current,
                      							"name",
                      							lv_name_67_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.WildID");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    otherlv_68=(Token)match(input,14,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_68, grammarAccess.getWildcardRuleAccess().getRightParenthesisKeyword_10_4());
                      			
                    }

                    }


                    }
                    break;

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleWildcardRule"


    // $ANTLR start "entryRuleSORT_BY_NAME"
    // InternalLinkerScript.g:3957:1: entryRuleSORT_BY_NAME returns [String current=null] : iv_ruleSORT_BY_NAME= ruleSORT_BY_NAME EOF ;
    public final String entryRuleSORT_BY_NAME() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleSORT_BY_NAME = null;


        try {
            // InternalLinkerScript.g:3957:52: (iv_ruleSORT_BY_NAME= ruleSORT_BY_NAME EOF )
            // InternalLinkerScript.g:3958:2: iv_ruleSORT_BY_NAME= ruleSORT_BY_NAME EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getSORT_BY_NAMERule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleSORT_BY_NAME=ruleSORT_BY_NAME();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleSORT_BY_NAME.getText(); 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleSORT_BY_NAME"


    // $ANTLR start "ruleSORT_BY_NAME"
    // InternalLinkerScript.g:3964:1: ruleSORT_BY_NAME returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (kw= 'SORT' | kw= 'SORT_BY_NAME' ) ;
    public final AntlrDatatypeRuleToken ruleSORT_BY_NAME() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;


        	enterRule();

        try {
            // InternalLinkerScript.g:3970:2: ( (kw= 'SORT' | kw= 'SORT_BY_NAME' ) )
            // InternalLinkerScript.g:3971:2: (kw= 'SORT' | kw= 'SORT_BY_NAME' )
            {
            // InternalLinkerScript.g:3971:2: (kw= 'SORT' | kw= 'SORT_BY_NAME' )
            int alt57=2;
            int LA57_0 = input.LA(1);

            if ( (LA57_0==79) ) {
                alt57=1;
            }
            else if ( (LA57_0==54) ) {
                alt57=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 57, 0, input);

                throw nvae;
            }
            switch (alt57) {
                case 1 :
                    // InternalLinkerScript.g:3972:3: kw= 'SORT'
                    {
                    kw=(Token)match(input,79,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getSORT_BY_NAMEAccess().getSORTKeyword_0());
                      		
                    }

                    }
                    break;
                case 2 :
                    // InternalLinkerScript.g:3978:3: kw= 'SORT_BY_NAME'
                    {
                    kw=(Token)match(input,54,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getSORT_BY_NAMEAccess().getSORT_BY_NAMEKeyword_1());
                      		
                    }

                    }
                    break;

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleSORT_BY_NAME"


    // $ANTLR start "entryRuleMemoryCommand"
    // InternalLinkerScript.g:3987:1: entryRuleMemoryCommand returns [EObject current=null] : iv_ruleMemoryCommand= ruleMemoryCommand EOF ;
    public final EObject entryRuleMemoryCommand() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleMemoryCommand = null;


        try {
            // InternalLinkerScript.g:3987:54: (iv_ruleMemoryCommand= ruleMemoryCommand EOF )
            // InternalLinkerScript.g:3988:2: iv_ruleMemoryCommand= ruleMemoryCommand EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getMemoryCommandRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleMemoryCommand=ruleMemoryCommand();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleMemoryCommand; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleMemoryCommand"


    // $ANTLR start "ruleMemoryCommand"
    // InternalLinkerScript.g:3994:1: ruleMemoryCommand returns [EObject current=null] : (otherlv_0= 'MEMORY' otherlv_1= '{' () ( (lv_memories_3_0= ruleMemory ) )* otherlv_4= '}' ) ;
    public final EObject ruleMemoryCommand() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token otherlv_1=null;
        Token otherlv_4=null;
        EObject lv_memories_3_0 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:4000:2: ( (otherlv_0= 'MEMORY' otherlv_1= '{' () ( (lv_memories_3_0= ruleMemory ) )* otherlv_4= '}' ) )
            // InternalLinkerScript.g:4001:2: (otherlv_0= 'MEMORY' otherlv_1= '{' () ( (lv_memories_3_0= ruleMemory ) )* otherlv_4= '}' )
            {
            // InternalLinkerScript.g:4001:2: (otherlv_0= 'MEMORY' otherlv_1= '{' () ( (lv_memories_3_0= ruleMemory ) )* otherlv_4= '}' )
            // InternalLinkerScript.g:4002:3: otherlv_0= 'MEMORY' otherlv_1= '{' () ( (lv_memories_3_0= ruleMemory ) )* otherlv_4= '}'
            {
            otherlv_0=(Token)match(input,80,FOLLOW_16); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_0, grammarAccess.getMemoryCommandAccess().getMEMORYKeyword_0());
              		
            }
            otherlv_1=(Token)match(input,34,FOLLOW_17); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_1, grammarAccess.getMemoryCommandAccess().getLeftCurlyBracketKeyword_1());
              		
            }
            // InternalLinkerScript.g:4010:3: ()
            // InternalLinkerScript.g:4011:4: 
            {
            if ( state.backtracking==0 ) {

              				current = forceCreateModelElement(
              					grammarAccess.getMemoryCommandAccess().getMemoryCommandAction_2(),
              					current);
              			
            }

            }

            // InternalLinkerScript.g:4017:3: ( (lv_memories_3_0= ruleMemory ) )*
            loop58:
            do {
                int alt58=2;
                int LA58_0 = input.LA(1);

                if ( (LA58_0==RULE_ID||LA58_0==80||(LA58_0>=82 && LA58_0<=83)||(LA58_0>=85 && LA58_0<=86)) ) {
                    alt58=1;
                }


                switch (alt58) {
            	case 1 :
            	    // InternalLinkerScript.g:4018:4: (lv_memories_3_0= ruleMemory )
            	    {
            	    // InternalLinkerScript.g:4018:4: (lv_memories_3_0= ruleMemory )
            	    // InternalLinkerScript.g:4019:5: lv_memories_3_0= ruleMemory
            	    {
            	    if ( state.backtracking==0 ) {

            	      					newCompositeNode(grammarAccess.getMemoryCommandAccess().getMemoriesMemoryParserRuleCall_3_0());
            	      				
            	    }
            	    pushFollow(FOLLOW_17);
            	    lv_memories_3_0=ruleMemory();

            	    state._fsp--;
            	    if (state.failed) return current;
            	    if ( state.backtracking==0 ) {

            	      					if (current==null) {
            	      						current = createModelElementForParent(grammarAccess.getMemoryCommandRule());
            	      					}
            	      					add(
            	      						current,
            	      						"memories",
            	      						lv_memories_3_0,
            	      						"org.eclipse.cdt.linkerscript.LinkerScript.Memory");
            	      					afterParserOrEnumRuleCall();
            	      				
            	    }

            	    }


            	    }
            	    break;

            	default :
            	    break loop58;
                }
            } while (true);

            otherlv_4=(Token)match(input,35,FOLLOW_2); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_4, grammarAccess.getMemoryCommandAccess().getRightCurlyBracketKeyword_4());
              		
            }

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleMemoryCommand"


    // $ANTLR start "entryRuleMemory"
    // InternalLinkerScript.g:4044:1: entryRuleMemory returns [EObject current=null] : iv_ruleMemory= ruleMemory EOF ;
    public final EObject entryRuleMemory() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleMemory = null;


        try {
            // InternalLinkerScript.g:4044:47: (iv_ruleMemory= ruleMemory EOF )
            // InternalLinkerScript.g:4045:2: iv_ruleMemory= ruleMemory EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getMemoryRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleMemory=ruleMemory();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleMemory; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleMemory"


    // $ANTLR start "ruleMemory"
    // InternalLinkerScript.g:4051:1: ruleMemory returns [EObject current=null] : ( ( (lv_name_0_0= ruleMemoryName ) ) ( (lv_attr_1_0= ruleMemoryAttribute ) )? otherlv_2= ':' (otherlv_3= 'ORIGIN' | otherlv_4= 'org' | otherlv_5= 'o' ) otherlv_6= '=' ( (lv_origin_7_0= ruleLExpression ) ) otherlv_8= ',' (otherlv_9= 'LENGTH' | otherlv_10= 'len' | otherlv_11= 'l' ) otherlv_12= '=' ( (lv_length_13_0= ruleLExpression ) ) ) ;
    public final EObject ruleMemory() throws RecognitionException {
        EObject current = null;

        Token otherlv_2=null;
        Token otherlv_3=null;
        Token otherlv_4=null;
        Token otherlv_5=null;
        Token otherlv_6=null;
        Token otherlv_8=null;
        Token otherlv_9=null;
        Token otherlv_10=null;
        Token otherlv_11=null;
        Token otherlv_12=null;
        AntlrDatatypeRuleToken lv_name_0_0 = null;

        AntlrDatatypeRuleToken lv_attr_1_0 = null;

        EObject lv_origin_7_0 = null;

        EObject lv_length_13_0 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:4057:2: ( ( ( (lv_name_0_0= ruleMemoryName ) ) ( (lv_attr_1_0= ruleMemoryAttribute ) )? otherlv_2= ':' (otherlv_3= 'ORIGIN' | otherlv_4= 'org' | otherlv_5= 'o' ) otherlv_6= '=' ( (lv_origin_7_0= ruleLExpression ) ) otherlv_8= ',' (otherlv_9= 'LENGTH' | otherlv_10= 'len' | otherlv_11= 'l' ) otherlv_12= '=' ( (lv_length_13_0= ruleLExpression ) ) ) )
            // InternalLinkerScript.g:4058:2: ( ( (lv_name_0_0= ruleMemoryName ) ) ( (lv_attr_1_0= ruleMemoryAttribute ) )? otherlv_2= ':' (otherlv_3= 'ORIGIN' | otherlv_4= 'org' | otherlv_5= 'o' ) otherlv_6= '=' ( (lv_origin_7_0= ruleLExpression ) ) otherlv_8= ',' (otherlv_9= 'LENGTH' | otherlv_10= 'len' | otherlv_11= 'l' ) otherlv_12= '=' ( (lv_length_13_0= ruleLExpression ) ) )
            {
            // InternalLinkerScript.g:4058:2: ( ( (lv_name_0_0= ruleMemoryName ) ) ( (lv_attr_1_0= ruleMemoryAttribute ) )? otherlv_2= ':' (otherlv_3= 'ORIGIN' | otherlv_4= 'org' | otherlv_5= 'o' ) otherlv_6= '=' ( (lv_origin_7_0= ruleLExpression ) ) otherlv_8= ',' (otherlv_9= 'LENGTH' | otherlv_10= 'len' | otherlv_11= 'l' ) otherlv_12= '=' ( (lv_length_13_0= ruleLExpression ) ) )
            // InternalLinkerScript.g:4059:3: ( (lv_name_0_0= ruleMemoryName ) ) ( (lv_attr_1_0= ruleMemoryAttribute ) )? otherlv_2= ':' (otherlv_3= 'ORIGIN' | otherlv_4= 'org' | otherlv_5= 'o' ) otherlv_6= '=' ( (lv_origin_7_0= ruleLExpression ) ) otherlv_8= ',' (otherlv_9= 'LENGTH' | otherlv_10= 'len' | otherlv_11= 'l' ) otherlv_12= '=' ( (lv_length_13_0= ruleLExpression ) )
            {
            // InternalLinkerScript.g:4059:3: ( (lv_name_0_0= ruleMemoryName ) )
            // InternalLinkerScript.g:4060:4: (lv_name_0_0= ruleMemoryName )
            {
            // InternalLinkerScript.g:4060:4: (lv_name_0_0= ruleMemoryName )
            // InternalLinkerScript.g:4061:5: lv_name_0_0= ruleMemoryName
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getMemoryAccess().getNameMemoryNameParserRuleCall_0_0());
              				
            }
            pushFollow(FOLLOW_21);
            lv_name_0_0=ruleMemoryName();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getMemoryRule());
              					}
              					set(
              						current,
              						"name",
              						lv_name_0_0,
              						"org.eclipse.cdt.linkerscript.LinkerScript.MemoryName");
              					afterParserOrEnumRuleCall();
              				
            }

            }


            }

            // InternalLinkerScript.g:4078:3: ( (lv_attr_1_0= ruleMemoryAttribute ) )?
            int alt59=2;
            int LA59_0 = input.LA(1);

            if ( (LA59_0==13) ) {
                alt59=1;
            }
            switch (alt59) {
                case 1 :
                    // InternalLinkerScript.g:4079:4: (lv_attr_1_0= ruleMemoryAttribute )
                    {
                    // InternalLinkerScript.g:4079:4: (lv_attr_1_0= ruleMemoryAttribute )
                    // InternalLinkerScript.g:4080:5: lv_attr_1_0= ruleMemoryAttribute
                    {
                    if ( state.backtracking==0 ) {

                      					newCompositeNode(grammarAccess.getMemoryAccess().getAttrMemoryAttributeParserRuleCall_1_0());
                      				
                    }
                    pushFollow(FOLLOW_23);
                    lv_attr_1_0=ruleMemoryAttribute();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      					if (current==null) {
                      						current = createModelElementForParent(grammarAccess.getMemoryRule());
                      					}
                      					set(
                      						current,
                      						"attr",
                      						lv_attr_1_0,
                      						"org.eclipse.cdt.linkerscript.LinkerScript.MemoryAttribute");
                      					afterParserOrEnumRuleCall();
                      				
                    }

                    }


                    }
                    break;

            }

            otherlv_2=(Token)match(input,37,FOLLOW_46); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_2, grammarAccess.getMemoryAccess().getColonKeyword_2());
              		
            }
            // InternalLinkerScript.g:4101:3: (otherlv_3= 'ORIGIN' | otherlv_4= 'org' | otherlv_5= 'o' )
            int alt60=3;
            switch ( input.LA(1) ) {
            case 81:
                {
                alt60=1;
                }
                break;
            case 82:
                {
                alt60=2;
                }
                break;
            case 83:
                {
                alt60=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 60, 0, input);

                throw nvae;
            }

            switch (alt60) {
                case 1 :
                    // InternalLinkerScript.g:4102:4: otherlv_3= 'ORIGIN'
                    {
                    otherlv_3=(Token)match(input,81,FOLLOW_36); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_3, grammarAccess.getMemoryAccess().getORIGINKeyword_3_0());
                      			
                    }

                    }
                    break;
                case 2 :
                    // InternalLinkerScript.g:4107:4: otherlv_4= 'org'
                    {
                    otherlv_4=(Token)match(input,82,FOLLOW_36); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_4, grammarAccess.getMemoryAccess().getOrgKeyword_3_1());
                      			
                    }

                    }
                    break;
                case 3 :
                    // InternalLinkerScript.g:4112:4: otherlv_5= 'o'
                    {
                    otherlv_5=(Token)match(input,83,FOLLOW_36); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_5, grammarAccess.getMemoryAccess().getOKeyword_3_2());
                      			
                    }

                    }
                    break;

            }

            otherlv_6=(Token)match(input,41,FOLLOW_8); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_6, grammarAccess.getMemoryAccess().getEqualsSignKeyword_4());
              		
            }
            // InternalLinkerScript.g:4121:3: ( (lv_origin_7_0= ruleLExpression ) )
            // InternalLinkerScript.g:4122:4: (lv_origin_7_0= ruleLExpression )
            {
            // InternalLinkerScript.g:4122:4: (lv_origin_7_0= ruleLExpression )
            // InternalLinkerScript.g:4123:5: lv_origin_7_0= ruleLExpression
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getMemoryAccess().getOriginLExpressionParserRuleCall_5_0());
              				
            }
            pushFollow(FOLLOW_9);
            lv_origin_7_0=ruleLExpression();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getMemoryRule());
              					}
              					set(
              						current,
              						"origin",
              						lv_origin_7_0,
              						"org.eclipse.cdt.linkerscript.LinkerScript.LExpression");
              					afterParserOrEnumRuleCall();
              				
            }

            }


            }

            otherlv_8=(Token)match(input,10,FOLLOW_47); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_8, grammarAccess.getMemoryAccess().getCommaKeyword_6());
              		
            }
            // InternalLinkerScript.g:4144:3: (otherlv_9= 'LENGTH' | otherlv_10= 'len' | otherlv_11= 'l' )
            int alt61=3;
            switch ( input.LA(1) ) {
            case 84:
                {
                alt61=1;
                }
                break;
            case 85:
                {
                alt61=2;
                }
                break;
            case 86:
                {
                alt61=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 61, 0, input);

                throw nvae;
            }

            switch (alt61) {
                case 1 :
                    // InternalLinkerScript.g:4145:4: otherlv_9= 'LENGTH'
                    {
                    otherlv_9=(Token)match(input,84,FOLLOW_36); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_9, grammarAccess.getMemoryAccess().getLENGTHKeyword_7_0());
                      			
                    }

                    }
                    break;
                case 2 :
                    // InternalLinkerScript.g:4150:4: otherlv_10= 'len'
                    {
                    otherlv_10=(Token)match(input,85,FOLLOW_36); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_10, grammarAccess.getMemoryAccess().getLenKeyword_7_1());
                      			
                    }

                    }
                    break;
                case 3 :
                    // InternalLinkerScript.g:4155:4: otherlv_11= 'l'
                    {
                    otherlv_11=(Token)match(input,86,FOLLOW_36); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_11, grammarAccess.getMemoryAccess().getLKeyword_7_2());
                      			
                    }

                    }
                    break;

            }

            otherlv_12=(Token)match(input,41,FOLLOW_8); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_12, grammarAccess.getMemoryAccess().getEqualsSignKeyword_8());
              		
            }
            // InternalLinkerScript.g:4164:3: ( (lv_length_13_0= ruleLExpression ) )
            // InternalLinkerScript.g:4165:4: (lv_length_13_0= ruleLExpression )
            {
            // InternalLinkerScript.g:4165:4: (lv_length_13_0= ruleLExpression )
            // InternalLinkerScript.g:4166:5: lv_length_13_0= ruleLExpression
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getMemoryAccess().getLengthLExpressionParserRuleCall_9_0());
              				
            }
            pushFollow(FOLLOW_2);
            lv_length_13_0=ruleLExpression();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getMemoryRule());
              					}
              					set(
              						current,
              						"length",
              						lv_length_13_0,
              						"org.eclipse.cdt.linkerscript.LinkerScript.LExpression");
              					afterParserOrEnumRuleCall();
              				
            }

            }


            }


            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleMemory"


    // $ANTLR start "entryRuleMemoryName"
    // InternalLinkerScript.g:4187:1: entryRuleMemoryName returns [String current=null] : iv_ruleMemoryName= ruleMemoryName EOF ;
    public final String entryRuleMemoryName() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleMemoryName = null;


        try {
            // InternalLinkerScript.g:4187:50: (iv_ruleMemoryName= ruleMemoryName EOF )
            // InternalLinkerScript.g:4188:2: iv_ruleMemoryName= ruleMemoryName EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getMemoryNameRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleMemoryName=ruleMemoryName();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleMemoryName.getText(); 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleMemoryName"


    // $ANTLR start "ruleMemoryName"
    // InternalLinkerScript.g:4194:1: ruleMemoryName returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : this_ValidID_0= ruleValidID ;
    public final AntlrDatatypeRuleToken ruleMemoryName() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        AntlrDatatypeRuleToken this_ValidID_0 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:4200:2: (this_ValidID_0= ruleValidID )
            // InternalLinkerScript.g:4201:2: this_ValidID_0= ruleValidID
            {
            if ( state.backtracking==0 ) {

              		newCompositeNode(grammarAccess.getMemoryNameAccess().getValidIDParserRuleCall());
              	
            }
            pushFollow(FOLLOW_2);
            this_ValidID_0=ruleValidID();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              		current.merge(this_ValidID_0);
              	
            }
            if ( state.backtracking==0 ) {

              		afterParserOrEnumRuleCall();
              	
            }

            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleMemoryName"


    // $ANTLR start "entryRuleMemoryAttribute"
    // InternalLinkerScript.g:4214:1: entryRuleMemoryAttribute returns [String current=null] : iv_ruleMemoryAttribute= ruleMemoryAttribute EOF ;
    public final String entryRuleMemoryAttribute() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleMemoryAttribute = null;


        try {
            // InternalLinkerScript.g:4214:55: (iv_ruleMemoryAttribute= ruleMemoryAttribute EOF )
            // InternalLinkerScript.g:4215:2: iv_ruleMemoryAttribute= ruleMemoryAttribute EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getMemoryAttributeRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleMemoryAttribute=ruleMemoryAttribute();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleMemoryAttribute.getText(); 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleMemoryAttribute"


    // $ANTLR start "ruleMemoryAttribute"
    // InternalLinkerScript.g:4221:1: ruleMemoryAttribute returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (kw= '(' ( (kw= '!' )? this_WildID_2= ruleWildID )+ kw= ')' ) ;
    public final AntlrDatatypeRuleToken ruleMemoryAttribute() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;
        AntlrDatatypeRuleToken this_WildID_2 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:4227:2: ( (kw= '(' ( (kw= '!' )? this_WildID_2= ruleWildID )+ kw= ')' ) )
            // InternalLinkerScript.g:4228:2: (kw= '(' ( (kw= '!' )? this_WildID_2= ruleWildID )+ kw= ')' )
            {
            // InternalLinkerScript.g:4228:2: (kw= '(' ( (kw= '!' )? this_WildID_2= ruleWildID )+ kw= ')' )
            // InternalLinkerScript.g:4229:3: kw= '(' ( (kw= '!' )? this_WildID_2= ruleWildID )+ kw= ')'
            {
            kw=(Token)match(input,13,FOLLOW_48); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current.merge(kw);
              			newLeafNode(kw, grammarAccess.getMemoryAttributeAccess().getLeftParenthesisKeyword_0());
              		
            }
            // InternalLinkerScript.g:4234:3: ( (kw= '!' )? this_WildID_2= ruleWildID )+
            int cnt63=0;
            loop63:
            do {
                int alt63=2;
                int LA63_0 = input.LA(1);

                if ( (LA63_0==RULE_ID||LA63_0==80||(LA63_0>=82 && LA63_0<=83)||(LA63_0>=85 && LA63_0<=87)||LA63_0==96) ) {
                    alt63=1;
                }


                switch (alt63) {
            	case 1 :
            	    // InternalLinkerScript.g:4235:4: (kw= '!' )? this_WildID_2= ruleWildID
            	    {
            	    // InternalLinkerScript.g:4235:4: (kw= '!' )?
            	    int alt62=2;
            	    int LA62_0 = input.LA(1);

            	    if ( (LA62_0==87) ) {
            	        alt62=1;
            	    }
            	    switch (alt62) {
            	        case 1 :
            	            // InternalLinkerScript.g:4236:5: kw= '!'
            	            {
            	            kw=(Token)match(input,87,FOLLOW_6); if (state.failed) return current;
            	            if ( state.backtracking==0 ) {

            	              					current.merge(kw);
            	              					newLeafNode(kw, grammarAccess.getMemoryAttributeAccess().getExclamationMarkKeyword_1_0());
            	              				
            	            }

            	            }
            	            break;

            	    }

            	    if ( state.backtracking==0 ) {

            	      				newCompositeNode(grammarAccess.getMemoryAttributeAccess().getWildIDParserRuleCall_1_1());
            	      			
            	    }
            	    pushFollow(FOLLOW_49);
            	    this_WildID_2=ruleWildID();

            	    state._fsp--;
            	    if (state.failed) return current;
            	    if ( state.backtracking==0 ) {

            	      				current.merge(this_WildID_2);
            	      			
            	    }
            	    if ( state.backtracking==0 ) {

            	      				afterParserOrEnumRuleCall();
            	      			
            	    }

            	    }
            	    break;

            	default :
            	    if ( cnt63 >= 1 ) break loop63;
            	    if (state.backtracking>0) {state.failed=true; return current;}
                        EarlyExitException eee =
                            new EarlyExitException(63, input);
                        throw eee;
                }
                cnt63++;
            } while (true);

            kw=(Token)match(input,14,FOLLOW_2); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current.merge(kw);
              			newLeafNode(kw, grammarAccess.getMemoryAttributeAccess().getRightParenthesisKeyword_2());
              		
            }

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleMemoryAttribute"


    // $ANTLR start "entryRuleLExpression"
    // InternalLinkerScript.g:4262:1: entryRuleLExpression returns [EObject current=null] : iv_ruleLExpression= ruleLExpression EOF ;
    public final EObject entryRuleLExpression() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleLExpression = null;


        try {
            // InternalLinkerScript.g:4262:52: (iv_ruleLExpression= ruleLExpression EOF )
            // InternalLinkerScript.g:4263:2: iv_ruleLExpression= ruleLExpression EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getLExpressionRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleLExpression=ruleLExpression();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleLExpression; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleLExpression"


    // $ANTLR start "ruleLExpression"
    // InternalLinkerScript.g:4269:1: ruleLExpression returns [EObject current=null] : this_LTernary_0= ruleLTernary ;
    public final EObject ruleLExpression() throws RecognitionException {
        EObject current = null;

        EObject this_LTernary_0 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:4275:2: (this_LTernary_0= ruleLTernary )
            // InternalLinkerScript.g:4276:2: this_LTernary_0= ruleLTernary
            {
            if ( state.backtracking==0 ) {

              		newCompositeNode(grammarAccess.getLExpressionAccess().getLTernaryParserRuleCall());
              	
            }
            pushFollow(FOLLOW_2);
            this_LTernary_0=ruleLTernary();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              		current = this_LTernary_0;
              		afterParserOrEnumRuleCall();
              	
            }

            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleLExpression"


    // $ANTLR start "entryRuleLTernary"
    // InternalLinkerScript.g:4287:1: entryRuleLTernary returns [EObject current=null] : iv_ruleLTernary= ruleLTernary EOF ;
    public final EObject entryRuleLTernary() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleLTernary = null;


        try {
            // InternalLinkerScript.g:4287:49: (iv_ruleLTernary= ruleLTernary EOF )
            // InternalLinkerScript.g:4288:2: iv_ruleLTernary= ruleLTernary EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getLTernaryRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleLTernary=ruleLTernary();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleLTernary; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleLTernary"


    // $ANTLR start "ruleLTernary"
    // InternalLinkerScript.g:4294:1: ruleLTernary returns [EObject current=null] : (this_LOrExpression_0= ruleLOrExpression ( ( ( ( () '?' ( ( ruleLOrExpression ) ) ':' ) )=> ( () otherlv_2= '?' ( (lv_ifPart_3_0= ruleLOrExpression ) ) otherlv_4= ':' ) ) ( (lv_thenPart_5_0= ruleLOrExpression ) ) )? ) ;
    public final EObject ruleLTernary() throws RecognitionException {
        EObject current = null;

        Token otherlv_2=null;
        Token otherlv_4=null;
        EObject this_LOrExpression_0 = null;

        EObject lv_ifPart_3_0 = null;

        EObject lv_thenPart_5_0 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:4300:2: ( (this_LOrExpression_0= ruleLOrExpression ( ( ( ( () '?' ( ( ruleLOrExpression ) ) ':' ) )=> ( () otherlv_2= '?' ( (lv_ifPart_3_0= ruleLOrExpression ) ) otherlv_4= ':' ) ) ( (lv_thenPart_5_0= ruleLOrExpression ) ) )? ) )
            // InternalLinkerScript.g:4301:2: (this_LOrExpression_0= ruleLOrExpression ( ( ( ( () '?' ( ( ruleLOrExpression ) ) ':' ) )=> ( () otherlv_2= '?' ( (lv_ifPart_3_0= ruleLOrExpression ) ) otherlv_4= ':' ) ) ( (lv_thenPart_5_0= ruleLOrExpression ) ) )? )
            {
            // InternalLinkerScript.g:4301:2: (this_LOrExpression_0= ruleLOrExpression ( ( ( ( () '?' ( ( ruleLOrExpression ) ) ':' ) )=> ( () otherlv_2= '?' ( (lv_ifPart_3_0= ruleLOrExpression ) ) otherlv_4= ':' ) ) ( (lv_thenPart_5_0= ruleLOrExpression ) ) )? )
            // InternalLinkerScript.g:4302:3: this_LOrExpression_0= ruleLOrExpression ( ( ( ( () '?' ( ( ruleLOrExpression ) ) ':' ) )=> ( () otherlv_2= '?' ( (lv_ifPart_3_0= ruleLOrExpression ) ) otherlv_4= ':' ) ) ( (lv_thenPart_5_0= ruleLOrExpression ) ) )?
            {
            if ( state.backtracking==0 ) {

              			newCompositeNode(grammarAccess.getLTernaryAccess().getLOrExpressionParserRuleCall_0());
              		
            }
            pushFollow(FOLLOW_50);
            this_LOrExpression_0=ruleLOrExpression();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current = this_LOrExpression_0;
              			afterParserOrEnumRuleCall();
              		
            }
            // InternalLinkerScript.g:4310:3: ( ( ( ( () '?' ( ( ruleLOrExpression ) ) ':' ) )=> ( () otherlv_2= '?' ( (lv_ifPart_3_0= ruleLOrExpression ) ) otherlv_4= ':' ) ) ( (lv_thenPart_5_0= ruleLOrExpression ) ) )?
            int alt64=2;
            int LA64_0 = input.LA(1);

            if ( (LA64_0==88) && (synpred1_InternalLinkerScript())) {
                alt64=1;
            }
            switch (alt64) {
                case 1 :
                    // InternalLinkerScript.g:4311:4: ( ( ( () '?' ( ( ruleLOrExpression ) ) ':' ) )=> ( () otherlv_2= '?' ( (lv_ifPart_3_0= ruleLOrExpression ) ) otherlv_4= ':' ) ) ( (lv_thenPart_5_0= ruleLOrExpression ) )
                    {
                    // InternalLinkerScript.g:4311:4: ( ( ( () '?' ( ( ruleLOrExpression ) ) ':' ) )=> ( () otherlv_2= '?' ( (lv_ifPart_3_0= ruleLOrExpression ) ) otherlv_4= ':' ) )
                    // InternalLinkerScript.g:4312:5: ( ( () '?' ( ( ruleLOrExpression ) ) ':' ) )=> ( () otherlv_2= '?' ( (lv_ifPart_3_0= ruleLOrExpression ) ) otherlv_4= ':' )
                    {
                    // InternalLinkerScript.g:4324:5: ( () otherlv_2= '?' ( (lv_ifPart_3_0= ruleLOrExpression ) ) otherlv_4= ':' )
                    // InternalLinkerScript.g:4325:6: () otherlv_2= '?' ( (lv_ifPart_3_0= ruleLOrExpression ) ) otherlv_4= ':'
                    {
                    // InternalLinkerScript.g:4325:6: ()
                    // InternalLinkerScript.g:4326:7: 
                    {
                    if ( state.backtracking==0 ) {

                      							current = forceCreateModelElementAndSet(
                      								grammarAccess.getLTernaryAccess().getLTernaryOperationConditionAction_1_0_0_0(),
                      								current);
                      						
                    }

                    }

                    otherlv_2=(Token)match(input,88,FOLLOW_8); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						newLeafNode(otherlv_2, grammarAccess.getLTernaryAccess().getQuestionMarkKeyword_1_0_0_1());
                      					
                    }
                    // InternalLinkerScript.g:4336:6: ( (lv_ifPart_3_0= ruleLOrExpression ) )
                    // InternalLinkerScript.g:4337:7: (lv_ifPart_3_0= ruleLOrExpression )
                    {
                    // InternalLinkerScript.g:4337:7: (lv_ifPart_3_0= ruleLOrExpression )
                    // InternalLinkerScript.g:4338:8: lv_ifPart_3_0= ruleLOrExpression
                    {
                    if ( state.backtracking==0 ) {

                      								newCompositeNode(grammarAccess.getLTernaryAccess().getIfPartLOrExpressionParserRuleCall_1_0_0_2_0());
                      							
                    }
                    pushFollow(FOLLOW_23);
                    lv_ifPart_3_0=ruleLOrExpression();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      								if (current==null) {
                      									current = createModelElementForParent(grammarAccess.getLTernaryRule());
                      								}
                      								set(
                      									current,
                      									"ifPart",
                      									lv_ifPart_3_0,
                      									"org.eclipse.cdt.linkerscript.LinkerScript.LOrExpression");
                      								afterParserOrEnumRuleCall();
                      							
                    }

                    }


                    }

                    otherlv_4=(Token)match(input,37,FOLLOW_8); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						newLeafNode(otherlv_4, grammarAccess.getLTernaryAccess().getColonKeyword_1_0_0_3());
                      					
                    }

                    }


                    }

                    // InternalLinkerScript.g:4361:4: ( (lv_thenPart_5_0= ruleLOrExpression ) )
                    // InternalLinkerScript.g:4362:5: (lv_thenPart_5_0= ruleLOrExpression )
                    {
                    // InternalLinkerScript.g:4362:5: (lv_thenPart_5_0= ruleLOrExpression )
                    // InternalLinkerScript.g:4363:6: lv_thenPart_5_0= ruleLOrExpression
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getLTernaryAccess().getThenPartLOrExpressionParserRuleCall_1_1_0());
                      					
                    }
                    pushFollow(FOLLOW_2);
                    lv_thenPart_5_0=ruleLOrExpression();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getLTernaryRule());
                      						}
                      						set(
                      							current,
                      							"thenPart",
                      							lv_thenPart_5_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.LOrExpression");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }


                    }
                    break;

            }


            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleLTernary"


    // $ANTLR start "entryRuleLOrExpression"
    // InternalLinkerScript.g:4385:1: entryRuleLOrExpression returns [EObject current=null] : iv_ruleLOrExpression= ruleLOrExpression EOF ;
    public final EObject entryRuleLOrExpression() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleLOrExpression = null;


        try {
            // InternalLinkerScript.g:4385:54: (iv_ruleLOrExpression= ruleLOrExpression EOF )
            // InternalLinkerScript.g:4386:2: iv_ruleLOrExpression= ruleLOrExpression EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getLOrExpressionRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleLOrExpression=ruleLOrExpression();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleLOrExpression; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleLOrExpression"


    // $ANTLR start "ruleLOrExpression"
    // InternalLinkerScript.g:4392:1: ruleLOrExpression returns [EObject current=null] : (this_LAndExpression_0= ruleLAndExpression ( ( ( ( () ( ( ruleOpOr ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpOr ) ) ) ) ( (lv_rightOperand_3_0= ruleLAndExpression ) ) )* ) ;
    public final EObject ruleLOrExpression() throws RecognitionException {
        EObject current = null;

        EObject this_LAndExpression_0 = null;

        AntlrDatatypeRuleToken lv_feature_2_0 = null;

        EObject lv_rightOperand_3_0 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:4398:2: ( (this_LAndExpression_0= ruleLAndExpression ( ( ( ( () ( ( ruleOpOr ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpOr ) ) ) ) ( (lv_rightOperand_3_0= ruleLAndExpression ) ) )* ) )
            // InternalLinkerScript.g:4399:2: (this_LAndExpression_0= ruleLAndExpression ( ( ( ( () ( ( ruleOpOr ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpOr ) ) ) ) ( (lv_rightOperand_3_0= ruleLAndExpression ) ) )* )
            {
            // InternalLinkerScript.g:4399:2: (this_LAndExpression_0= ruleLAndExpression ( ( ( ( () ( ( ruleOpOr ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpOr ) ) ) ) ( (lv_rightOperand_3_0= ruleLAndExpression ) ) )* )
            // InternalLinkerScript.g:4400:3: this_LAndExpression_0= ruleLAndExpression ( ( ( ( () ( ( ruleOpOr ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpOr ) ) ) ) ( (lv_rightOperand_3_0= ruleLAndExpression ) ) )*
            {
            if ( state.backtracking==0 ) {

              			newCompositeNode(grammarAccess.getLOrExpressionAccess().getLAndExpressionParserRuleCall_0());
              		
            }
            pushFollow(FOLLOW_51);
            this_LAndExpression_0=ruleLAndExpression();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current = this_LAndExpression_0;
              			afterParserOrEnumRuleCall();
              		
            }
            // InternalLinkerScript.g:4408:3: ( ( ( ( () ( ( ruleOpOr ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpOr ) ) ) ) ( (lv_rightOperand_3_0= ruleLAndExpression ) ) )*
            loop65:
            do {
                int alt65=2;
                int LA65_0 = input.LA(1);

                if ( (LA65_0==89) && (synpred2_InternalLinkerScript())) {
                    alt65=1;
                }


                switch (alt65) {
            	case 1 :
            	    // InternalLinkerScript.g:4409:4: ( ( ( () ( ( ruleOpOr ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpOr ) ) ) ) ( (lv_rightOperand_3_0= ruleLAndExpression ) )
            	    {
            	    // InternalLinkerScript.g:4409:4: ( ( ( () ( ( ruleOpOr ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpOr ) ) ) )
            	    // InternalLinkerScript.g:4410:5: ( ( () ( ( ruleOpOr ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpOr ) ) )
            	    {
            	    // InternalLinkerScript.g:4420:5: ( () ( (lv_feature_2_0= ruleOpOr ) ) )
            	    // InternalLinkerScript.g:4421:6: () ( (lv_feature_2_0= ruleOpOr ) )
            	    {
            	    // InternalLinkerScript.g:4421:6: ()
            	    // InternalLinkerScript.g:4422:7: 
            	    {
            	    if ( state.backtracking==0 ) {

            	      							current = forceCreateModelElementAndSet(
            	      								grammarAccess.getLOrExpressionAccess().getLBinaryOperationLeftOperandAction_1_0_0_0(),
            	      								current);
            	      						
            	    }

            	    }

            	    // InternalLinkerScript.g:4428:6: ( (lv_feature_2_0= ruleOpOr ) )
            	    // InternalLinkerScript.g:4429:7: (lv_feature_2_0= ruleOpOr )
            	    {
            	    // InternalLinkerScript.g:4429:7: (lv_feature_2_0= ruleOpOr )
            	    // InternalLinkerScript.g:4430:8: lv_feature_2_0= ruleOpOr
            	    {
            	    if ( state.backtracking==0 ) {

            	      								newCompositeNode(grammarAccess.getLOrExpressionAccess().getFeatureOpOrParserRuleCall_1_0_0_1_0());
            	      							
            	    }
            	    pushFollow(FOLLOW_8);
            	    lv_feature_2_0=ruleOpOr();

            	    state._fsp--;
            	    if (state.failed) return current;
            	    if ( state.backtracking==0 ) {

            	      								if (current==null) {
            	      									current = createModelElementForParent(grammarAccess.getLOrExpressionRule());
            	      								}
            	      								set(
            	      									current,
            	      									"feature",
            	      									lv_feature_2_0,
            	      									"org.eclipse.cdt.linkerscript.LinkerScript.OpOr");
            	      								afterParserOrEnumRuleCall();
            	      							
            	    }

            	    }


            	    }


            	    }


            	    }

            	    // InternalLinkerScript.g:4449:4: ( (lv_rightOperand_3_0= ruleLAndExpression ) )
            	    // InternalLinkerScript.g:4450:5: (lv_rightOperand_3_0= ruleLAndExpression )
            	    {
            	    // InternalLinkerScript.g:4450:5: (lv_rightOperand_3_0= ruleLAndExpression )
            	    // InternalLinkerScript.g:4451:6: lv_rightOperand_3_0= ruleLAndExpression
            	    {
            	    if ( state.backtracking==0 ) {

            	      						newCompositeNode(grammarAccess.getLOrExpressionAccess().getRightOperandLAndExpressionParserRuleCall_1_1_0());
            	      					
            	    }
            	    pushFollow(FOLLOW_51);
            	    lv_rightOperand_3_0=ruleLAndExpression();

            	    state._fsp--;
            	    if (state.failed) return current;
            	    if ( state.backtracking==0 ) {

            	      						if (current==null) {
            	      							current = createModelElementForParent(grammarAccess.getLOrExpressionRule());
            	      						}
            	      						set(
            	      							current,
            	      							"rightOperand",
            	      							lv_rightOperand_3_0,
            	      							"org.eclipse.cdt.linkerscript.LinkerScript.LAndExpression");
            	      						afterParserOrEnumRuleCall();
            	      					
            	    }

            	    }


            	    }


            	    }
            	    break;

            	default :
            	    break loop65;
                }
            } while (true);


            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleLOrExpression"


    // $ANTLR start "entryRuleOpOr"
    // InternalLinkerScript.g:4473:1: entryRuleOpOr returns [String current=null] : iv_ruleOpOr= ruleOpOr EOF ;
    public final String entryRuleOpOr() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleOpOr = null;


        try {
            // InternalLinkerScript.g:4473:44: (iv_ruleOpOr= ruleOpOr EOF )
            // InternalLinkerScript.g:4474:2: iv_ruleOpOr= ruleOpOr EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getOpOrRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleOpOr=ruleOpOr();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleOpOr.getText(); 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleOpOr"


    // $ANTLR start "ruleOpOr"
    // InternalLinkerScript.g:4480:1: ruleOpOr returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : kw= '||' ;
    public final AntlrDatatypeRuleToken ruleOpOr() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;


        	enterRule();

        try {
            // InternalLinkerScript.g:4486:2: (kw= '||' )
            // InternalLinkerScript.g:4487:2: kw= '||'
            {
            kw=(Token)match(input,89,FOLLOW_2); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              		current.merge(kw);
              		newLeafNode(kw, grammarAccess.getOpOrAccess().getVerticalLineVerticalLineKeyword());
              	
            }

            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleOpOr"


    // $ANTLR start "entryRuleLAndExpression"
    // InternalLinkerScript.g:4495:1: entryRuleLAndExpression returns [EObject current=null] : iv_ruleLAndExpression= ruleLAndExpression EOF ;
    public final EObject entryRuleLAndExpression() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleLAndExpression = null;


        try {
            // InternalLinkerScript.g:4495:55: (iv_ruleLAndExpression= ruleLAndExpression EOF )
            // InternalLinkerScript.g:4496:2: iv_ruleLAndExpression= ruleLAndExpression EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getLAndExpressionRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleLAndExpression=ruleLAndExpression();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleLAndExpression; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleLAndExpression"


    // $ANTLR start "ruleLAndExpression"
    // InternalLinkerScript.g:4502:1: ruleLAndExpression returns [EObject current=null] : (this_LBitwiseOrExpression_0= ruleLBitwiseOrExpression ( ( ( ( () ( ( ruleOpAnd ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpAnd ) ) ) ) ( (lv_rightOperand_3_0= ruleLBitwiseOrExpression ) ) )* ) ;
    public final EObject ruleLAndExpression() throws RecognitionException {
        EObject current = null;

        EObject this_LBitwiseOrExpression_0 = null;

        AntlrDatatypeRuleToken lv_feature_2_0 = null;

        EObject lv_rightOperand_3_0 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:4508:2: ( (this_LBitwiseOrExpression_0= ruleLBitwiseOrExpression ( ( ( ( () ( ( ruleOpAnd ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpAnd ) ) ) ) ( (lv_rightOperand_3_0= ruleLBitwiseOrExpression ) ) )* ) )
            // InternalLinkerScript.g:4509:2: (this_LBitwiseOrExpression_0= ruleLBitwiseOrExpression ( ( ( ( () ( ( ruleOpAnd ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpAnd ) ) ) ) ( (lv_rightOperand_3_0= ruleLBitwiseOrExpression ) ) )* )
            {
            // InternalLinkerScript.g:4509:2: (this_LBitwiseOrExpression_0= ruleLBitwiseOrExpression ( ( ( ( () ( ( ruleOpAnd ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpAnd ) ) ) ) ( (lv_rightOperand_3_0= ruleLBitwiseOrExpression ) ) )* )
            // InternalLinkerScript.g:4510:3: this_LBitwiseOrExpression_0= ruleLBitwiseOrExpression ( ( ( ( () ( ( ruleOpAnd ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpAnd ) ) ) ) ( (lv_rightOperand_3_0= ruleLBitwiseOrExpression ) ) )*
            {
            if ( state.backtracking==0 ) {

              			newCompositeNode(grammarAccess.getLAndExpressionAccess().getLBitwiseOrExpressionParserRuleCall_0());
              		
            }
            pushFollow(FOLLOW_52);
            this_LBitwiseOrExpression_0=ruleLBitwiseOrExpression();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current = this_LBitwiseOrExpression_0;
              			afterParserOrEnumRuleCall();
              		
            }
            // InternalLinkerScript.g:4518:3: ( ( ( ( () ( ( ruleOpAnd ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpAnd ) ) ) ) ( (lv_rightOperand_3_0= ruleLBitwiseOrExpression ) ) )*
            loop66:
            do {
                int alt66=2;
                int LA66_0 = input.LA(1);

                if ( (LA66_0==90) && (synpred3_InternalLinkerScript())) {
                    alt66=1;
                }


                switch (alt66) {
            	case 1 :
            	    // InternalLinkerScript.g:4519:4: ( ( ( () ( ( ruleOpAnd ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpAnd ) ) ) ) ( (lv_rightOperand_3_0= ruleLBitwiseOrExpression ) )
            	    {
            	    // InternalLinkerScript.g:4519:4: ( ( ( () ( ( ruleOpAnd ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpAnd ) ) ) )
            	    // InternalLinkerScript.g:4520:5: ( ( () ( ( ruleOpAnd ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpAnd ) ) )
            	    {
            	    // InternalLinkerScript.g:4530:5: ( () ( (lv_feature_2_0= ruleOpAnd ) ) )
            	    // InternalLinkerScript.g:4531:6: () ( (lv_feature_2_0= ruleOpAnd ) )
            	    {
            	    // InternalLinkerScript.g:4531:6: ()
            	    // InternalLinkerScript.g:4532:7: 
            	    {
            	    if ( state.backtracking==0 ) {

            	      							current = forceCreateModelElementAndSet(
            	      								grammarAccess.getLAndExpressionAccess().getLBinaryOperationLeftOperandAction_1_0_0_0(),
            	      								current);
            	      						
            	    }

            	    }

            	    // InternalLinkerScript.g:4538:6: ( (lv_feature_2_0= ruleOpAnd ) )
            	    // InternalLinkerScript.g:4539:7: (lv_feature_2_0= ruleOpAnd )
            	    {
            	    // InternalLinkerScript.g:4539:7: (lv_feature_2_0= ruleOpAnd )
            	    // InternalLinkerScript.g:4540:8: lv_feature_2_0= ruleOpAnd
            	    {
            	    if ( state.backtracking==0 ) {

            	      								newCompositeNode(grammarAccess.getLAndExpressionAccess().getFeatureOpAndParserRuleCall_1_0_0_1_0());
            	      							
            	    }
            	    pushFollow(FOLLOW_8);
            	    lv_feature_2_0=ruleOpAnd();

            	    state._fsp--;
            	    if (state.failed) return current;
            	    if ( state.backtracking==0 ) {

            	      								if (current==null) {
            	      									current = createModelElementForParent(grammarAccess.getLAndExpressionRule());
            	      								}
            	      								set(
            	      									current,
            	      									"feature",
            	      									lv_feature_2_0,
            	      									"org.eclipse.cdt.linkerscript.LinkerScript.OpAnd");
            	      								afterParserOrEnumRuleCall();
            	      							
            	    }

            	    }


            	    }


            	    }


            	    }

            	    // InternalLinkerScript.g:4559:4: ( (lv_rightOperand_3_0= ruleLBitwiseOrExpression ) )
            	    // InternalLinkerScript.g:4560:5: (lv_rightOperand_3_0= ruleLBitwiseOrExpression )
            	    {
            	    // InternalLinkerScript.g:4560:5: (lv_rightOperand_3_0= ruleLBitwiseOrExpression )
            	    // InternalLinkerScript.g:4561:6: lv_rightOperand_3_0= ruleLBitwiseOrExpression
            	    {
            	    if ( state.backtracking==0 ) {

            	      						newCompositeNode(grammarAccess.getLAndExpressionAccess().getRightOperandLBitwiseOrExpressionParserRuleCall_1_1_0());
            	      					
            	    }
            	    pushFollow(FOLLOW_52);
            	    lv_rightOperand_3_0=ruleLBitwiseOrExpression();

            	    state._fsp--;
            	    if (state.failed) return current;
            	    if ( state.backtracking==0 ) {

            	      						if (current==null) {
            	      							current = createModelElementForParent(grammarAccess.getLAndExpressionRule());
            	      						}
            	      						set(
            	      							current,
            	      							"rightOperand",
            	      							lv_rightOperand_3_0,
            	      							"org.eclipse.cdt.linkerscript.LinkerScript.LBitwiseOrExpression");
            	      						afterParserOrEnumRuleCall();
            	      					
            	    }

            	    }


            	    }


            	    }
            	    break;

            	default :
            	    break loop66;
                }
            } while (true);


            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleLAndExpression"


    // $ANTLR start "entryRuleOpAnd"
    // InternalLinkerScript.g:4583:1: entryRuleOpAnd returns [String current=null] : iv_ruleOpAnd= ruleOpAnd EOF ;
    public final String entryRuleOpAnd() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleOpAnd = null;


        try {
            // InternalLinkerScript.g:4583:45: (iv_ruleOpAnd= ruleOpAnd EOF )
            // InternalLinkerScript.g:4584:2: iv_ruleOpAnd= ruleOpAnd EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getOpAndRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleOpAnd=ruleOpAnd();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleOpAnd.getText(); 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleOpAnd"


    // $ANTLR start "ruleOpAnd"
    // InternalLinkerScript.g:4590:1: ruleOpAnd returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : kw= '&&' ;
    public final AntlrDatatypeRuleToken ruleOpAnd() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;


        	enterRule();

        try {
            // InternalLinkerScript.g:4596:2: (kw= '&&' )
            // InternalLinkerScript.g:4597:2: kw= '&&'
            {
            kw=(Token)match(input,90,FOLLOW_2); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              		current.merge(kw);
              		newLeafNode(kw, grammarAccess.getOpAndAccess().getAmpersandAmpersandKeyword());
              	
            }

            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleOpAnd"


    // $ANTLR start "entryRuleLBitwiseOrExpression"
    // InternalLinkerScript.g:4605:1: entryRuleLBitwiseOrExpression returns [EObject current=null] : iv_ruleLBitwiseOrExpression= ruleLBitwiseOrExpression EOF ;
    public final EObject entryRuleLBitwiseOrExpression() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleLBitwiseOrExpression = null;


        try {
            // InternalLinkerScript.g:4605:61: (iv_ruleLBitwiseOrExpression= ruleLBitwiseOrExpression EOF )
            // InternalLinkerScript.g:4606:2: iv_ruleLBitwiseOrExpression= ruleLBitwiseOrExpression EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getLBitwiseOrExpressionRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleLBitwiseOrExpression=ruleLBitwiseOrExpression();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleLBitwiseOrExpression; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleLBitwiseOrExpression"


    // $ANTLR start "ruleLBitwiseOrExpression"
    // InternalLinkerScript.g:4612:1: ruleLBitwiseOrExpression returns [EObject current=null] : (this_LBitwiseAndExpression_0= ruleLBitwiseAndExpression ( ( ( ( () ( ( ruleOpBitwiseOr ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpBitwiseOr ) ) ) ) ( (lv_rightOperand_3_0= ruleLBitwiseAndExpression ) ) )* ) ;
    public final EObject ruleLBitwiseOrExpression() throws RecognitionException {
        EObject current = null;

        EObject this_LBitwiseAndExpression_0 = null;

        AntlrDatatypeRuleToken lv_feature_2_0 = null;

        EObject lv_rightOperand_3_0 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:4618:2: ( (this_LBitwiseAndExpression_0= ruleLBitwiseAndExpression ( ( ( ( () ( ( ruleOpBitwiseOr ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpBitwiseOr ) ) ) ) ( (lv_rightOperand_3_0= ruleLBitwiseAndExpression ) ) )* ) )
            // InternalLinkerScript.g:4619:2: (this_LBitwiseAndExpression_0= ruleLBitwiseAndExpression ( ( ( ( () ( ( ruleOpBitwiseOr ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpBitwiseOr ) ) ) ) ( (lv_rightOperand_3_0= ruleLBitwiseAndExpression ) ) )* )
            {
            // InternalLinkerScript.g:4619:2: (this_LBitwiseAndExpression_0= ruleLBitwiseAndExpression ( ( ( ( () ( ( ruleOpBitwiseOr ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpBitwiseOr ) ) ) ) ( (lv_rightOperand_3_0= ruleLBitwiseAndExpression ) ) )* )
            // InternalLinkerScript.g:4620:3: this_LBitwiseAndExpression_0= ruleLBitwiseAndExpression ( ( ( ( () ( ( ruleOpBitwiseOr ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpBitwiseOr ) ) ) ) ( (lv_rightOperand_3_0= ruleLBitwiseAndExpression ) ) )*
            {
            if ( state.backtracking==0 ) {

              			newCompositeNode(grammarAccess.getLBitwiseOrExpressionAccess().getLBitwiseAndExpressionParserRuleCall_0());
              		
            }
            pushFollow(FOLLOW_53);
            this_LBitwiseAndExpression_0=ruleLBitwiseAndExpression();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current = this_LBitwiseAndExpression_0;
              			afterParserOrEnumRuleCall();
              		
            }
            // InternalLinkerScript.g:4628:3: ( ( ( ( () ( ( ruleOpBitwiseOr ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpBitwiseOr ) ) ) ) ( (lv_rightOperand_3_0= ruleLBitwiseAndExpression ) ) )*
            loop67:
            do {
                int alt67=2;
                int LA67_0 = input.LA(1);

                if ( (LA67_0==91) && (synpred4_InternalLinkerScript())) {
                    alt67=1;
                }


                switch (alt67) {
            	case 1 :
            	    // InternalLinkerScript.g:4629:4: ( ( ( () ( ( ruleOpBitwiseOr ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpBitwiseOr ) ) ) ) ( (lv_rightOperand_3_0= ruleLBitwiseAndExpression ) )
            	    {
            	    // InternalLinkerScript.g:4629:4: ( ( ( () ( ( ruleOpBitwiseOr ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpBitwiseOr ) ) ) )
            	    // InternalLinkerScript.g:4630:5: ( ( () ( ( ruleOpBitwiseOr ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpBitwiseOr ) ) )
            	    {
            	    // InternalLinkerScript.g:4640:5: ( () ( (lv_feature_2_0= ruleOpBitwiseOr ) ) )
            	    // InternalLinkerScript.g:4641:6: () ( (lv_feature_2_0= ruleOpBitwiseOr ) )
            	    {
            	    // InternalLinkerScript.g:4641:6: ()
            	    // InternalLinkerScript.g:4642:7: 
            	    {
            	    if ( state.backtracking==0 ) {

            	      							current = forceCreateModelElementAndSet(
            	      								grammarAccess.getLBitwiseOrExpressionAccess().getLBinaryOperationLeftOperandAction_1_0_0_0(),
            	      								current);
            	      						
            	    }

            	    }

            	    // InternalLinkerScript.g:4648:6: ( (lv_feature_2_0= ruleOpBitwiseOr ) )
            	    // InternalLinkerScript.g:4649:7: (lv_feature_2_0= ruleOpBitwiseOr )
            	    {
            	    // InternalLinkerScript.g:4649:7: (lv_feature_2_0= ruleOpBitwiseOr )
            	    // InternalLinkerScript.g:4650:8: lv_feature_2_0= ruleOpBitwiseOr
            	    {
            	    if ( state.backtracking==0 ) {

            	      								newCompositeNode(grammarAccess.getLBitwiseOrExpressionAccess().getFeatureOpBitwiseOrParserRuleCall_1_0_0_1_0());
            	      							
            	    }
            	    pushFollow(FOLLOW_8);
            	    lv_feature_2_0=ruleOpBitwiseOr();

            	    state._fsp--;
            	    if (state.failed) return current;
            	    if ( state.backtracking==0 ) {

            	      								if (current==null) {
            	      									current = createModelElementForParent(grammarAccess.getLBitwiseOrExpressionRule());
            	      								}
            	      								set(
            	      									current,
            	      									"feature",
            	      									lv_feature_2_0,
            	      									"org.eclipse.cdt.linkerscript.LinkerScript.OpBitwiseOr");
            	      								afterParserOrEnumRuleCall();
            	      							
            	    }

            	    }


            	    }


            	    }


            	    }

            	    // InternalLinkerScript.g:4669:4: ( (lv_rightOperand_3_0= ruleLBitwiseAndExpression ) )
            	    // InternalLinkerScript.g:4670:5: (lv_rightOperand_3_0= ruleLBitwiseAndExpression )
            	    {
            	    // InternalLinkerScript.g:4670:5: (lv_rightOperand_3_0= ruleLBitwiseAndExpression )
            	    // InternalLinkerScript.g:4671:6: lv_rightOperand_3_0= ruleLBitwiseAndExpression
            	    {
            	    if ( state.backtracking==0 ) {

            	      						newCompositeNode(grammarAccess.getLBitwiseOrExpressionAccess().getRightOperandLBitwiseAndExpressionParserRuleCall_1_1_0());
            	      					
            	    }
            	    pushFollow(FOLLOW_53);
            	    lv_rightOperand_3_0=ruleLBitwiseAndExpression();

            	    state._fsp--;
            	    if (state.failed) return current;
            	    if ( state.backtracking==0 ) {

            	      						if (current==null) {
            	      							current = createModelElementForParent(grammarAccess.getLBitwiseOrExpressionRule());
            	      						}
            	      						set(
            	      							current,
            	      							"rightOperand",
            	      							lv_rightOperand_3_0,
            	      							"org.eclipse.cdt.linkerscript.LinkerScript.LBitwiseAndExpression");
            	      						afterParserOrEnumRuleCall();
            	      					
            	    }

            	    }


            	    }


            	    }
            	    break;

            	default :
            	    break loop67;
                }
            } while (true);


            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleLBitwiseOrExpression"


    // $ANTLR start "entryRuleOpBitwiseOr"
    // InternalLinkerScript.g:4693:1: entryRuleOpBitwiseOr returns [String current=null] : iv_ruleOpBitwiseOr= ruleOpBitwiseOr EOF ;
    public final String entryRuleOpBitwiseOr() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleOpBitwiseOr = null;


        try {
            // InternalLinkerScript.g:4693:51: (iv_ruleOpBitwiseOr= ruleOpBitwiseOr EOF )
            // InternalLinkerScript.g:4694:2: iv_ruleOpBitwiseOr= ruleOpBitwiseOr EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getOpBitwiseOrRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleOpBitwiseOr=ruleOpBitwiseOr();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleOpBitwiseOr.getText(); 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleOpBitwiseOr"


    // $ANTLR start "ruleOpBitwiseOr"
    // InternalLinkerScript.g:4700:1: ruleOpBitwiseOr returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : kw= '|' ;
    public final AntlrDatatypeRuleToken ruleOpBitwiseOr() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;


        	enterRule();

        try {
            // InternalLinkerScript.g:4706:2: (kw= '|' )
            // InternalLinkerScript.g:4707:2: kw= '|'
            {
            kw=(Token)match(input,91,FOLLOW_2); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              		current.merge(kw);
              		newLeafNode(kw, grammarAccess.getOpBitwiseOrAccess().getVerticalLineKeyword());
              	
            }

            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleOpBitwiseOr"


    // $ANTLR start "entryRuleLBitwiseAndExpression"
    // InternalLinkerScript.g:4715:1: entryRuleLBitwiseAndExpression returns [EObject current=null] : iv_ruleLBitwiseAndExpression= ruleLBitwiseAndExpression EOF ;
    public final EObject entryRuleLBitwiseAndExpression() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleLBitwiseAndExpression = null;


        try {
            // InternalLinkerScript.g:4715:62: (iv_ruleLBitwiseAndExpression= ruleLBitwiseAndExpression EOF )
            // InternalLinkerScript.g:4716:2: iv_ruleLBitwiseAndExpression= ruleLBitwiseAndExpression EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getLBitwiseAndExpressionRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleLBitwiseAndExpression=ruleLBitwiseAndExpression();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleLBitwiseAndExpression; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleLBitwiseAndExpression"


    // $ANTLR start "ruleLBitwiseAndExpression"
    // InternalLinkerScript.g:4722:1: ruleLBitwiseAndExpression returns [EObject current=null] : (this_LEqualityExpression_0= ruleLEqualityExpression ( ( ( ( () ( ( ruleOpBitwiseAnd ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpBitwiseAnd ) ) ) ) ( (lv_rightOperand_3_0= ruleLEqualityExpression ) ) )* ) ;
    public final EObject ruleLBitwiseAndExpression() throws RecognitionException {
        EObject current = null;

        EObject this_LEqualityExpression_0 = null;

        AntlrDatatypeRuleToken lv_feature_2_0 = null;

        EObject lv_rightOperand_3_0 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:4728:2: ( (this_LEqualityExpression_0= ruleLEqualityExpression ( ( ( ( () ( ( ruleOpBitwiseAnd ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpBitwiseAnd ) ) ) ) ( (lv_rightOperand_3_0= ruleLEqualityExpression ) ) )* ) )
            // InternalLinkerScript.g:4729:2: (this_LEqualityExpression_0= ruleLEqualityExpression ( ( ( ( () ( ( ruleOpBitwiseAnd ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpBitwiseAnd ) ) ) ) ( (lv_rightOperand_3_0= ruleLEqualityExpression ) ) )* )
            {
            // InternalLinkerScript.g:4729:2: (this_LEqualityExpression_0= ruleLEqualityExpression ( ( ( ( () ( ( ruleOpBitwiseAnd ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpBitwiseAnd ) ) ) ) ( (lv_rightOperand_3_0= ruleLEqualityExpression ) ) )* )
            // InternalLinkerScript.g:4730:3: this_LEqualityExpression_0= ruleLEqualityExpression ( ( ( ( () ( ( ruleOpBitwiseAnd ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpBitwiseAnd ) ) ) ) ( (lv_rightOperand_3_0= ruleLEqualityExpression ) ) )*
            {
            if ( state.backtracking==0 ) {

              			newCompositeNode(grammarAccess.getLBitwiseAndExpressionAccess().getLEqualityExpressionParserRuleCall_0());
              		
            }
            pushFollow(FOLLOW_54);
            this_LEqualityExpression_0=ruleLEqualityExpression();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current = this_LEqualityExpression_0;
              			afterParserOrEnumRuleCall();
              		
            }
            // InternalLinkerScript.g:4738:3: ( ( ( ( () ( ( ruleOpBitwiseAnd ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpBitwiseAnd ) ) ) ) ( (lv_rightOperand_3_0= ruleLEqualityExpression ) ) )*
            loop68:
            do {
                int alt68=2;
                int LA68_0 = input.LA(1);

                if ( (LA68_0==73) && (synpred5_InternalLinkerScript())) {
                    alt68=1;
                }


                switch (alt68) {
            	case 1 :
            	    // InternalLinkerScript.g:4739:4: ( ( ( () ( ( ruleOpBitwiseAnd ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpBitwiseAnd ) ) ) ) ( (lv_rightOperand_3_0= ruleLEqualityExpression ) )
            	    {
            	    // InternalLinkerScript.g:4739:4: ( ( ( () ( ( ruleOpBitwiseAnd ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpBitwiseAnd ) ) ) )
            	    // InternalLinkerScript.g:4740:5: ( ( () ( ( ruleOpBitwiseAnd ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpBitwiseAnd ) ) )
            	    {
            	    // InternalLinkerScript.g:4750:5: ( () ( (lv_feature_2_0= ruleOpBitwiseAnd ) ) )
            	    // InternalLinkerScript.g:4751:6: () ( (lv_feature_2_0= ruleOpBitwiseAnd ) )
            	    {
            	    // InternalLinkerScript.g:4751:6: ()
            	    // InternalLinkerScript.g:4752:7: 
            	    {
            	    if ( state.backtracking==0 ) {

            	      							current = forceCreateModelElementAndSet(
            	      								grammarAccess.getLBitwiseAndExpressionAccess().getLBinaryOperationLeftOperandAction_1_0_0_0(),
            	      								current);
            	      						
            	    }

            	    }

            	    // InternalLinkerScript.g:4758:6: ( (lv_feature_2_0= ruleOpBitwiseAnd ) )
            	    // InternalLinkerScript.g:4759:7: (lv_feature_2_0= ruleOpBitwiseAnd )
            	    {
            	    // InternalLinkerScript.g:4759:7: (lv_feature_2_0= ruleOpBitwiseAnd )
            	    // InternalLinkerScript.g:4760:8: lv_feature_2_0= ruleOpBitwiseAnd
            	    {
            	    if ( state.backtracking==0 ) {

            	      								newCompositeNode(grammarAccess.getLBitwiseAndExpressionAccess().getFeatureOpBitwiseAndParserRuleCall_1_0_0_1_0());
            	      							
            	    }
            	    pushFollow(FOLLOW_8);
            	    lv_feature_2_0=ruleOpBitwiseAnd();

            	    state._fsp--;
            	    if (state.failed) return current;
            	    if ( state.backtracking==0 ) {

            	      								if (current==null) {
            	      									current = createModelElementForParent(grammarAccess.getLBitwiseAndExpressionRule());
            	      								}
            	      								set(
            	      									current,
            	      									"feature",
            	      									lv_feature_2_0,
            	      									"org.eclipse.cdt.linkerscript.LinkerScript.OpBitwiseAnd");
            	      								afterParserOrEnumRuleCall();
            	      							
            	    }

            	    }


            	    }


            	    }


            	    }

            	    // InternalLinkerScript.g:4779:4: ( (lv_rightOperand_3_0= ruleLEqualityExpression ) )
            	    // InternalLinkerScript.g:4780:5: (lv_rightOperand_3_0= ruleLEqualityExpression )
            	    {
            	    // InternalLinkerScript.g:4780:5: (lv_rightOperand_3_0= ruleLEqualityExpression )
            	    // InternalLinkerScript.g:4781:6: lv_rightOperand_3_0= ruleLEqualityExpression
            	    {
            	    if ( state.backtracking==0 ) {

            	      						newCompositeNode(grammarAccess.getLBitwiseAndExpressionAccess().getRightOperandLEqualityExpressionParserRuleCall_1_1_0());
            	      					
            	    }
            	    pushFollow(FOLLOW_54);
            	    lv_rightOperand_3_0=ruleLEqualityExpression();

            	    state._fsp--;
            	    if (state.failed) return current;
            	    if ( state.backtracking==0 ) {

            	      						if (current==null) {
            	      							current = createModelElementForParent(grammarAccess.getLBitwiseAndExpressionRule());
            	      						}
            	      						set(
            	      							current,
            	      							"rightOperand",
            	      							lv_rightOperand_3_0,
            	      							"org.eclipse.cdt.linkerscript.LinkerScript.LEqualityExpression");
            	      						afterParserOrEnumRuleCall();
            	      					
            	    }

            	    }


            	    }


            	    }
            	    break;

            	default :
            	    break loop68;
                }
            } while (true);


            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleLBitwiseAndExpression"


    // $ANTLR start "entryRuleOpBitwiseAnd"
    // InternalLinkerScript.g:4803:1: entryRuleOpBitwiseAnd returns [String current=null] : iv_ruleOpBitwiseAnd= ruleOpBitwiseAnd EOF ;
    public final String entryRuleOpBitwiseAnd() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleOpBitwiseAnd = null;


        try {
            // InternalLinkerScript.g:4803:52: (iv_ruleOpBitwiseAnd= ruleOpBitwiseAnd EOF )
            // InternalLinkerScript.g:4804:2: iv_ruleOpBitwiseAnd= ruleOpBitwiseAnd EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getOpBitwiseAndRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleOpBitwiseAnd=ruleOpBitwiseAnd();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleOpBitwiseAnd.getText(); 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleOpBitwiseAnd"


    // $ANTLR start "ruleOpBitwiseAnd"
    // InternalLinkerScript.g:4810:1: ruleOpBitwiseAnd returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : kw= '&' ;
    public final AntlrDatatypeRuleToken ruleOpBitwiseAnd() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;


        	enterRule();

        try {
            // InternalLinkerScript.g:4816:2: (kw= '&' )
            // InternalLinkerScript.g:4817:2: kw= '&'
            {
            kw=(Token)match(input,73,FOLLOW_2); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              		current.merge(kw);
              		newLeafNode(kw, grammarAccess.getOpBitwiseAndAccess().getAmpersandKeyword());
              	
            }

            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleOpBitwiseAnd"


    // $ANTLR start "entryRuleLEqualityExpression"
    // InternalLinkerScript.g:4825:1: entryRuleLEqualityExpression returns [EObject current=null] : iv_ruleLEqualityExpression= ruleLEqualityExpression EOF ;
    public final EObject entryRuleLEqualityExpression() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleLEqualityExpression = null;


        try {
            // InternalLinkerScript.g:4825:60: (iv_ruleLEqualityExpression= ruleLEqualityExpression EOF )
            // InternalLinkerScript.g:4826:2: iv_ruleLEqualityExpression= ruleLEqualityExpression EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getLEqualityExpressionRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleLEqualityExpression=ruleLEqualityExpression();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleLEqualityExpression; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleLEqualityExpression"


    // $ANTLR start "ruleLEqualityExpression"
    // InternalLinkerScript.g:4832:1: ruleLEqualityExpression returns [EObject current=null] : (this_LRelationalExpression_0= ruleLRelationalExpression ( ( ( ( () ( ( ruleOpEquality ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpEquality ) ) ) ) ( (lv_rightOperand_3_0= ruleLRelationalExpression ) ) )* ) ;
    public final EObject ruleLEqualityExpression() throws RecognitionException {
        EObject current = null;

        EObject this_LRelationalExpression_0 = null;

        AntlrDatatypeRuleToken lv_feature_2_0 = null;

        EObject lv_rightOperand_3_0 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:4838:2: ( (this_LRelationalExpression_0= ruleLRelationalExpression ( ( ( ( () ( ( ruleOpEquality ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpEquality ) ) ) ) ( (lv_rightOperand_3_0= ruleLRelationalExpression ) ) )* ) )
            // InternalLinkerScript.g:4839:2: (this_LRelationalExpression_0= ruleLRelationalExpression ( ( ( ( () ( ( ruleOpEquality ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpEquality ) ) ) ) ( (lv_rightOperand_3_0= ruleLRelationalExpression ) ) )* )
            {
            // InternalLinkerScript.g:4839:2: (this_LRelationalExpression_0= ruleLRelationalExpression ( ( ( ( () ( ( ruleOpEquality ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpEquality ) ) ) ) ( (lv_rightOperand_3_0= ruleLRelationalExpression ) ) )* )
            // InternalLinkerScript.g:4840:3: this_LRelationalExpression_0= ruleLRelationalExpression ( ( ( ( () ( ( ruleOpEquality ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpEquality ) ) ) ) ( (lv_rightOperand_3_0= ruleLRelationalExpression ) ) )*
            {
            if ( state.backtracking==0 ) {

              			newCompositeNode(grammarAccess.getLEqualityExpressionAccess().getLRelationalExpressionParserRuleCall_0());
              		
            }
            pushFollow(FOLLOW_55);
            this_LRelationalExpression_0=ruleLRelationalExpression();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current = this_LRelationalExpression_0;
              			afterParserOrEnumRuleCall();
              		
            }
            // InternalLinkerScript.g:4848:3: ( ( ( ( () ( ( ruleOpEquality ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpEquality ) ) ) ) ( (lv_rightOperand_3_0= ruleLRelationalExpression ) ) )*
            loop69:
            do {
                int alt69=2;
                int LA69_0 = input.LA(1);

                if ( (LA69_0==92) && (synpred6_InternalLinkerScript())) {
                    alt69=1;
                }
                else if ( (LA69_0==93) && (synpred6_InternalLinkerScript())) {
                    alt69=1;
                }


                switch (alt69) {
            	case 1 :
            	    // InternalLinkerScript.g:4849:4: ( ( ( () ( ( ruleOpEquality ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpEquality ) ) ) ) ( (lv_rightOperand_3_0= ruleLRelationalExpression ) )
            	    {
            	    // InternalLinkerScript.g:4849:4: ( ( ( () ( ( ruleOpEquality ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpEquality ) ) ) )
            	    // InternalLinkerScript.g:4850:5: ( ( () ( ( ruleOpEquality ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpEquality ) ) )
            	    {
            	    // InternalLinkerScript.g:4860:5: ( () ( (lv_feature_2_0= ruleOpEquality ) ) )
            	    // InternalLinkerScript.g:4861:6: () ( (lv_feature_2_0= ruleOpEquality ) )
            	    {
            	    // InternalLinkerScript.g:4861:6: ()
            	    // InternalLinkerScript.g:4862:7: 
            	    {
            	    if ( state.backtracking==0 ) {

            	      							current = forceCreateModelElementAndSet(
            	      								grammarAccess.getLEqualityExpressionAccess().getLBinaryOperationLeftOperandAction_1_0_0_0(),
            	      								current);
            	      						
            	    }

            	    }

            	    // InternalLinkerScript.g:4868:6: ( (lv_feature_2_0= ruleOpEquality ) )
            	    // InternalLinkerScript.g:4869:7: (lv_feature_2_0= ruleOpEquality )
            	    {
            	    // InternalLinkerScript.g:4869:7: (lv_feature_2_0= ruleOpEquality )
            	    // InternalLinkerScript.g:4870:8: lv_feature_2_0= ruleOpEquality
            	    {
            	    if ( state.backtracking==0 ) {

            	      								newCompositeNode(grammarAccess.getLEqualityExpressionAccess().getFeatureOpEqualityParserRuleCall_1_0_0_1_0());
            	      							
            	    }
            	    pushFollow(FOLLOW_8);
            	    lv_feature_2_0=ruleOpEquality();

            	    state._fsp--;
            	    if (state.failed) return current;
            	    if ( state.backtracking==0 ) {

            	      								if (current==null) {
            	      									current = createModelElementForParent(grammarAccess.getLEqualityExpressionRule());
            	      								}
            	      								set(
            	      									current,
            	      									"feature",
            	      									lv_feature_2_0,
            	      									"org.eclipse.cdt.linkerscript.LinkerScript.OpEquality");
            	      								afterParserOrEnumRuleCall();
            	      							
            	    }

            	    }


            	    }


            	    }


            	    }

            	    // InternalLinkerScript.g:4889:4: ( (lv_rightOperand_3_0= ruleLRelationalExpression ) )
            	    // InternalLinkerScript.g:4890:5: (lv_rightOperand_3_0= ruleLRelationalExpression )
            	    {
            	    // InternalLinkerScript.g:4890:5: (lv_rightOperand_3_0= ruleLRelationalExpression )
            	    // InternalLinkerScript.g:4891:6: lv_rightOperand_3_0= ruleLRelationalExpression
            	    {
            	    if ( state.backtracking==0 ) {

            	      						newCompositeNode(grammarAccess.getLEqualityExpressionAccess().getRightOperandLRelationalExpressionParserRuleCall_1_1_0());
            	      					
            	    }
            	    pushFollow(FOLLOW_55);
            	    lv_rightOperand_3_0=ruleLRelationalExpression();

            	    state._fsp--;
            	    if (state.failed) return current;
            	    if ( state.backtracking==0 ) {

            	      						if (current==null) {
            	      							current = createModelElementForParent(grammarAccess.getLEqualityExpressionRule());
            	      						}
            	      						set(
            	      							current,
            	      							"rightOperand",
            	      							lv_rightOperand_3_0,
            	      							"org.eclipse.cdt.linkerscript.LinkerScript.LRelationalExpression");
            	      						afterParserOrEnumRuleCall();
            	      					
            	    }

            	    }


            	    }


            	    }
            	    break;

            	default :
            	    break loop69;
                }
            } while (true);


            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleLEqualityExpression"


    // $ANTLR start "entryRuleOpEquality"
    // InternalLinkerScript.g:4913:1: entryRuleOpEquality returns [String current=null] : iv_ruleOpEquality= ruleOpEquality EOF ;
    public final String entryRuleOpEquality() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleOpEquality = null;


        try {
            // InternalLinkerScript.g:4913:50: (iv_ruleOpEquality= ruleOpEquality EOF )
            // InternalLinkerScript.g:4914:2: iv_ruleOpEquality= ruleOpEquality EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getOpEqualityRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleOpEquality=ruleOpEquality();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleOpEquality.getText(); 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleOpEquality"


    // $ANTLR start "ruleOpEquality"
    // InternalLinkerScript.g:4920:1: ruleOpEquality returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (kw= '==' | kw= '!=' ) ;
    public final AntlrDatatypeRuleToken ruleOpEquality() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;


        	enterRule();

        try {
            // InternalLinkerScript.g:4926:2: ( (kw= '==' | kw= '!=' ) )
            // InternalLinkerScript.g:4927:2: (kw= '==' | kw= '!=' )
            {
            // InternalLinkerScript.g:4927:2: (kw= '==' | kw= '!=' )
            int alt70=2;
            int LA70_0 = input.LA(1);

            if ( (LA70_0==92) ) {
                alt70=1;
            }
            else if ( (LA70_0==93) ) {
                alt70=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 70, 0, input);

                throw nvae;
            }
            switch (alt70) {
                case 1 :
                    // InternalLinkerScript.g:4928:3: kw= '=='
                    {
                    kw=(Token)match(input,92,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getOpEqualityAccess().getEqualsSignEqualsSignKeyword_0());
                      		
                    }

                    }
                    break;
                case 2 :
                    // InternalLinkerScript.g:4934:3: kw= '!='
                    {
                    kw=(Token)match(input,93,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getOpEqualityAccess().getExclamationMarkEqualsSignKeyword_1());
                      		
                    }

                    }
                    break;

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleOpEquality"


    // $ANTLR start "entryRuleLRelationalExpression"
    // InternalLinkerScript.g:4943:1: entryRuleLRelationalExpression returns [EObject current=null] : iv_ruleLRelationalExpression= ruleLRelationalExpression EOF ;
    public final EObject entryRuleLRelationalExpression() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleLRelationalExpression = null;


        try {
            // InternalLinkerScript.g:4943:62: (iv_ruleLRelationalExpression= ruleLRelationalExpression EOF )
            // InternalLinkerScript.g:4944:2: iv_ruleLRelationalExpression= ruleLRelationalExpression EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getLRelationalExpressionRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleLRelationalExpression=ruleLRelationalExpression();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleLRelationalExpression; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleLRelationalExpression"


    // $ANTLR start "ruleLRelationalExpression"
    // InternalLinkerScript.g:4950:1: ruleLRelationalExpression returns [EObject current=null] : (this_LOtherOperatorExpression_0= ruleLOtherOperatorExpression ( ( ( ( () ( ( ruleOpCompare ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpCompare ) ) ) ) ( (lv_rightOperand_3_0= ruleLOtherOperatorExpression ) ) )* ) ;
    public final EObject ruleLRelationalExpression() throws RecognitionException {
        EObject current = null;

        EObject this_LOtherOperatorExpression_0 = null;

        AntlrDatatypeRuleToken lv_feature_2_0 = null;

        EObject lv_rightOperand_3_0 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:4956:2: ( (this_LOtherOperatorExpression_0= ruleLOtherOperatorExpression ( ( ( ( () ( ( ruleOpCompare ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpCompare ) ) ) ) ( (lv_rightOperand_3_0= ruleLOtherOperatorExpression ) ) )* ) )
            // InternalLinkerScript.g:4957:2: (this_LOtherOperatorExpression_0= ruleLOtherOperatorExpression ( ( ( ( () ( ( ruleOpCompare ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpCompare ) ) ) ) ( (lv_rightOperand_3_0= ruleLOtherOperatorExpression ) ) )* )
            {
            // InternalLinkerScript.g:4957:2: (this_LOtherOperatorExpression_0= ruleLOtherOperatorExpression ( ( ( ( () ( ( ruleOpCompare ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpCompare ) ) ) ) ( (lv_rightOperand_3_0= ruleLOtherOperatorExpression ) ) )* )
            // InternalLinkerScript.g:4958:3: this_LOtherOperatorExpression_0= ruleLOtherOperatorExpression ( ( ( ( () ( ( ruleOpCompare ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpCompare ) ) ) ) ( (lv_rightOperand_3_0= ruleLOtherOperatorExpression ) ) )*
            {
            if ( state.backtracking==0 ) {

              			newCompositeNode(grammarAccess.getLRelationalExpressionAccess().getLOtherOperatorExpressionParserRuleCall_0());
              		
            }
            pushFollow(FOLLOW_56);
            this_LOtherOperatorExpression_0=ruleLOtherOperatorExpression();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current = this_LOtherOperatorExpression_0;
              			afterParserOrEnumRuleCall();
              		
            }
            // InternalLinkerScript.g:4966:3: ( ( ( ( () ( ( ruleOpCompare ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpCompare ) ) ) ) ( (lv_rightOperand_3_0= ruleLOtherOperatorExpression ) ) )*
            loop71:
            do {
                int alt71=2;
                int LA71_0 = input.LA(1);

                if ( (LA71_0==69) && (synpred7_InternalLinkerScript())) {
                    alt71=1;
                }
                else if ( (LA71_0==68) && (synpred7_InternalLinkerScript())) {
                    alt71=1;
                }
                else if ( (LA71_0==40) && (synpred7_InternalLinkerScript())) {
                    alt71=1;
                }


                switch (alt71) {
            	case 1 :
            	    // InternalLinkerScript.g:4967:4: ( ( ( () ( ( ruleOpCompare ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpCompare ) ) ) ) ( (lv_rightOperand_3_0= ruleLOtherOperatorExpression ) )
            	    {
            	    // InternalLinkerScript.g:4967:4: ( ( ( () ( ( ruleOpCompare ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpCompare ) ) ) )
            	    // InternalLinkerScript.g:4968:5: ( ( () ( ( ruleOpCompare ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpCompare ) ) )
            	    {
            	    // InternalLinkerScript.g:4978:5: ( () ( (lv_feature_2_0= ruleOpCompare ) ) )
            	    // InternalLinkerScript.g:4979:6: () ( (lv_feature_2_0= ruleOpCompare ) )
            	    {
            	    // InternalLinkerScript.g:4979:6: ()
            	    // InternalLinkerScript.g:4980:7: 
            	    {
            	    if ( state.backtracking==0 ) {

            	      							current = forceCreateModelElementAndSet(
            	      								grammarAccess.getLRelationalExpressionAccess().getLBinaryOperationLeftOperandAction_1_0_0_0(),
            	      								current);
            	      						
            	    }

            	    }

            	    // InternalLinkerScript.g:4986:6: ( (lv_feature_2_0= ruleOpCompare ) )
            	    // InternalLinkerScript.g:4987:7: (lv_feature_2_0= ruleOpCompare )
            	    {
            	    // InternalLinkerScript.g:4987:7: (lv_feature_2_0= ruleOpCompare )
            	    // InternalLinkerScript.g:4988:8: lv_feature_2_0= ruleOpCompare
            	    {
            	    if ( state.backtracking==0 ) {

            	      								newCompositeNode(grammarAccess.getLRelationalExpressionAccess().getFeatureOpCompareParserRuleCall_1_0_0_1_0());
            	      							
            	    }
            	    pushFollow(FOLLOW_8);
            	    lv_feature_2_0=ruleOpCompare();

            	    state._fsp--;
            	    if (state.failed) return current;
            	    if ( state.backtracking==0 ) {

            	      								if (current==null) {
            	      									current = createModelElementForParent(grammarAccess.getLRelationalExpressionRule());
            	      								}
            	      								set(
            	      									current,
            	      									"feature",
            	      									lv_feature_2_0,
            	      									"org.eclipse.cdt.linkerscript.LinkerScript.OpCompare");
            	      								afterParserOrEnumRuleCall();
            	      							
            	    }

            	    }


            	    }


            	    }


            	    }

            	    // InternalLinkerScript.g:5007:4: ( (lv_rightOperand_3_0= ruleLOtherOperatorExpression ) )
            	    // InternalLinkerScript.g:5008:5: (lv_rightOperand_3_0= ruleLOtherOperatorExpression )
            	    {
            	    // InternalLinkerScript.g:5008:5: (lv_rightOperand_3_0= ruleLOtherOperatorExpression )
            	    // InternalLinkerScript.g:5009:6: lv_rightOperand_3_0= ruleLOtherOperatorExpression
            	    {
            	    if ( state.backtracking==0 ) {

            	      						newCompositeNode(grammarAccess.getLRelationalExpressionAccess().getRightOperandLOtherOperatorExpressionParserRuleCall_1_1_0());
            	      					
            	    }
            	    pushFollow(FOLLOW_56);
            	    lv_rightOperand_3_0=ruleLOtherOperatorExpression();

            	    state._fsp--;
            	    if (state.failed) return current;
            	    if ( state.backtracking==0 ) {

            	      						if (current==null) {
            	      							current = createModelElementForParent(grammarAccess.getLRelationalExpressionRule());
            	      						}
            	      						set(
            	      							current,
            	      							"rightOperand",
            	      							lv_rightOperand_3_0,
            	      							"org.eclipse.cdt.linkerscript.LinkerScript.LOtherOperatorExpression");
            	      						afterParserOrEnumRuleCall();
            	      					
            	    }

            	    }


            	    }


            	    }
            	    break;

            	default :
            	    break loop71;
                }
            } while (true);


            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleLRelationalExpression"


    // $ANTLR start "entryRuleOpCompare"
    // InternalLinkerScript.g:5031:1: entryRuleOpCompare returns [String current=null] : iv_ruleOpCompare= ruleOpCompare EOF ;
    public final String entryRuleOpCompare() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleOpCompare = null;


        try {
            // InternalLinkerScript.g:5031:49: (iv_ruleOpCompare= ruleOpCompare EOF )
            // InternalLinkerScript.g:5032:2: iv_ruleOpCompare= ruleOpCompare EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getOpCompareRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleOpCompare=ruleOpCompare();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleOpCompare.getText(); 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleOpCompare"


    // $ANTLR start "ruleOpCompare"
    // InternalLinkerScript.g:5038:1: ruleOpCompare returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (kw= '>=' | (kw= '<' kw= '=' ) | kw= '>' | kw= '<' ) ;
    public final AntlrDatatypeRuleToken ruleOpCompare() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;


        	enterRule();

        try {
            // InternalLinkerScript.g:5044:2: ( (kw= '>=' | (kw= '<' kw= '=' ) | kw= '>' | kw= '<' ) )
            // InternalLinkerScript.g:5045:2: (kw= '>=' | (kw= '<' kw= '=' ) | kw= '>' | kw= '<' )
            {
            // InternalLinkerScript.g:5045:2: (kw= '>=' | (kw= '<' kw= '=' ) | kw= '>' | kw= '<' )
            int alt72=4;
            switch ( input.LA(1) ) {
            case 69:
                {
                alt72=1;
                }
                break;
            case 68:
                {
                int LA72_2 = input.LA(2);

                if ( (LA72_2==EOF||(LA72_2>=RULE_ID && LA72_2<=RULE_HEX)||LA72_2==13||LA72_2==38||LA72_2==42||LA72_2==80||(LA72_2>=82 && LA72_2<=87)||(LA72_2>=94 && LA72_2<=95)||LA72_2==99||LA72_2==102) ) {
                    alt72=4;
                }
                else if ( (LA72_2==41) ) {
                    alt72=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return current;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 72, 2, input);

                    throw nvae;
                }
                }
                break;
            case 40:
                {
                alt72=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 72, 0, input);

                throw nvae;
            }

            switch (alt72) {
                case 1 :
                    // InternalLinkerScript.g:5046:3: kw= '>='
                    {
                    kw=(Token)match(input,69,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getOpCompareAccess().getGreaterThanSignEqualsSignKeyword_0());
                      		
                    }

                    }
                    break;
                case 2 :
                    // InternalLinkerScript.g:5052:3: (kw= '<' kw= '=' )
                    {
                    // InternalLinkerScript.g:5052:3: (kw= '<' kw= '=' )
                    // InternalLinkerScript.g:5053:4: kw= '<' kw= '='
                    {
                    kw=(Token)match(input,68,FOLLOW_36); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				current.merge(kw);
                      				newLeafNode(kw, grammarAccess.getOpCompareAccess().getLessThanSignKeyword_1_0());
                      			
                    }
                    kw=(Token)match(input,41,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				current.merge(kw);
                      				newLeafNode(kw, grammarAccess.getOpCompareAccess().getEqualsSignKeyword_1_1());
                      			
                    }

                    }


                    }
                    break;
                case 3 :
                    // InternalLinkerScript.g:5065:3: kw= '>'
                    {
                    kw=(Token)match(input,40,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getOpCompareAccess().getGreaterThanSignKeyword_2());
                      		
                    }

                    }
                    break;
                case 4 :
                    // InternalLinkerScript.g:5071:3: kw= '<'
                    {
                    kw=(Token)match(input,68,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getOpCompareAccess().getLessThanSignKeyword_3());
                      		
                    }

                    }
                    break;

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleOpCompare"


    // $ANTLR start "entryRuleLOtherOperatorExpression"
    // InternalLinkerScript.g:5080:1: entryRuleLOtherOperatorExpression returns [EObject current=null] : iv_ruleLOtherOperatorExpression= ruleLOtherOperatorExpression EOF ;
    public final EObject entryRuleLOtherOperatorExpression() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleLOtherOperatorExpression = null;


        try {
            // InternalLinkerScript.g:5080:65: (iv_ruleLOtherOperatorExpression= ruleLOtherOperatorExpression EOF )
            // InternalLinkerScript.g:5081:2: iv_ruleLOtherOperatorExpression= ruleLOtherOperatorExpression EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getLOtherOperatorExpressionRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleLOtherOperatorExpression=ruleLOtherOperatorExpression();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleLOtherOperatorExpression; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleLOtherOperatorExpression"


    // $ANTLR start "ruleLOtherOperatorExpression"
    // InternalLinkerScript.g:5087:1: ruleLOtherOperatorExpression returns [EObject current=null] : (this_LAdditiveExpression_0= ruleLAdditiveExpression ( ( ( ( () ( ( ruleOpOther ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpOther ) ) ) ) ( (lv_rightOperand_3_0= ruleLAdditiveExpression ) ) )* ) ;
    public final EObject ruleLOtherOperatorExpression() throws RecognitionException {
        EObject current = null;

        EObject this_LAdditiveExpression_0 = null;

        AntlrDatatypeRuleToken lv_feature_2_0 = null;

        EObject lv_rightOperand_3_0 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:5093:2: ( (this_LAdditiveExpression_0= ruleLAdditiveExpression ( ( ( ( () ( ( ruleOpOther ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpOther ) ) ) ) ( (lv_rightOperand_3_0= ruleLAdditiveExpression ) ) )* ) )
            // InternalLinkerScript.g:5094:2: (this_LAdditiveExpression_0= ruleLAdditiveExpression ( ( ( ( () ( ( ruleOpOther ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpOther ) ) ) ) ( (lv_rightOperand_3_0= ruleLAdditiveExpression ) ) )* )
            {
            // InternalLinkerScript.g:5094:2: (this_LAdditiveExpression_0= ruleLAdditiveExpression ( ( ( ( () ( ( ruleOpOther ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpOther ) ) ) ) ( (lv_rightOperand_3_0= ruleLAdditiveExpression ) ) )* )
            // InternalLinkerScript.g:5095:3: this_LAdditiveExpression_0= ruleLAdditiveExpression ( ( ( ( () ( ( ruleOpOther ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpOther ) ) ) ) ( (lv_rightOperand_3_0= ruleLAdditiveExpression ) ) )*
            {
            if ( state.backtracking==0 ) {

              			newCompositeNode(grammarAccess.getLOtherOperatorExpressionAccess().getLAdditiveExpressionParserRuleCall_0());
              		
            }
            pushFollow(FOLLOW_57);
            this_LAdditiveExpression_0=ruleLAdditiveExpression();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current = this_LAdditiveExpression_0;
              			afterParserOrEnumRuleCall();
              		
            }
            // InternalLinkerScript.g:5103:3: ( ( ( ( () ( ( ruleOpOther ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpOther ) ) ) ) ( (lv_rightOperand_3_0= ruleLAdditiveExpression ) ) )*
            loop73:
            do {
                int alt73=2;
                int LA73_0 = input.LA(1);

                if ( (LA73_0==68) ) {
                    int LA73_2 = input.LA(2);

                    if ( (LA73_2==68) && (synpred8_InternalLinkerScript())) {
                        alt73=1;
                    }


                }
                else if ( (LA73_0==40) ) {
                    int LA73_3 = input.LA(2);

                    if ( (LA73_3==40) && (synpred8_InternalLinkerScript())) {
                        alt73=1;
                    }


                }


                switch (alt73) {
            	case 1 :
            	    // InternalLinkerScript.g:5104:4: ( ( ( () ( ( ruleOpOther ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpOther ) ) ) ) ( (lv_rightOperand_3_0= ruleLAdditiveExpression ) )
            	    {
            	    // InternalLinkerScript.g:5104:4: ( ( ( () ( ( ruleOpOther ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpOther ) ) ) )
            	    // InternalLinkerScript.g:5105:5: ( ( () ( ( ruleOpOther ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpOther ) ) )
            	    {
            	    // InternalLinkerScript.g:5115:5: ( () ( (lv_feature_2_0= ruleOpOther ) ) )
            	    // InternalLinkerScript.g:5116:6: () ( (lv_feature_2_0= ruleOpOther ) )
            	    {
            	    // InternalLinkerScript.g:5116:6: ()
            	    // InternalLinkerScript.g:5117:7: 
            	    {
            	    if ( state.backtracking==0 ) {

            	      							current = forceCreateModelElementAndSet(
            	      								grammarAccess.getLOtherOperatorExpressionAccess().getLBinaryOperationLeftOperandAction_1_0_0_0(),
            	      								current);
            	      						
            	    }

            	    }

            	    // InternalLinkerScript.g:5123:6: ( (lv_feature_2_0= ruleOpOther ) )
            	    // InternalLinkerScript.g:5124:7: (lv_feature_2_0= ruleOpOther )
            	    {
            	    // InternalLinkerScript.g:5124:7: (lv_feature_2_0= ruleOpOther )
            	    // InternalLinkerScript.g:5125:8: lv_feature_2_0= ruleOpOther
            	    {
            	    if ( state.backtracking==0 ) {

            	      								newCompositeNode(grammarAccess.getLOtherOperatorExpressionAccess().getFeatureOpOtherParserRuleCall_1_0_0_1_0());
            	      							
            	    }
            	    pushFollow(FOLLOW_8);
            	    lv_feature_2_0=ruleOpOther();

            	    state._fsp--;
            	    if (state.failed) return current;
            	    if ( state.backtracking==0 ) {

            	      								if (current==null) {
            	      									current = createModelElementForParent(grammarAccess.getLOtherOperatorExpressionRule());
            	      								}
            	      								set(
            	      									current,
            	      									"feature",
            	      									lv_feature_2_0,
            	      									"org.eclipse.cdt.linkerscript.LinkerScript.OpOther");
            	      								afterParserOrEnumRuleCall();
            	      							
            	    }

            	    }


            	    }


            	    }


            	    }

            	    // InternalLinkerScript.g:5144:4: ( (lv_rightOperand_3_0= ruleLAdditiveExpression ) )
            	    // InternalLinkerScript.g:5145:5: (lv_rightOperand_3_0= ruleLAdditiveExpression )
            	    {
            	    // InternalLinkerScript.g:5145:5: (lv_rightOperand_3_0= ruleLAdditiveExpression )
            	    // InternalLinkerScript.g:5146:6: lv_rightOperand_3_0= ruleLAdditiveExpression
            	    {
            	    if ( state.backtracking==0 ) {

            	      						newCompositeNode(grammarAccess.getLOtherOperatorExpressionAccess().getRightOperandLAdditiveExpressionParserRuleCall_1_1_0());
            	      					
            	    }
            	    pushFollow(FOLLOW_57);
            	    lv_rightOperand_3_0=ruleLAdditiveExpression();

            	    state._fsp--;
            	    if (state.failed) return current;
            	    if ( state.backtracking==0 ) {

            	      						if (current==null) {
            	      							current = createModelElementForParent(grammarAccess.getLOtherOperatorExpressionRule());
            	      						}
            	      						set(
            	      							current,
            	      							"rightOperand",
            	      							lv_rightOperand_3_0,
            	      							"org.eclipse.cdt.linkerscript.LinkerScript.LAdditiveExpression");
            	      						afterParserOrEnumRuleCall();
            	      					
            	    }

            	    }


            	    }


            	    }
            	    break;

            	default :
            	    break loop73;
                }
            } while (true);


            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleLOtherOperatorExpression"


    // $ANTLR start "entryRuleOpOther"
    // InternalLinkerScript.g:5168:1: entryRuleOpOther returns [String current=null] : iv_ruleOpOther= ruleOpOther EOF ;
    public final String entryRuleOpOther() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleOpOther = null;


        try {
            // InternalLinkerScript.g:5168:47: (iv_ruleOpOther= ruleOpOther EOF )
            // InternalLinkerScript.g:5169:2: iv_ruleOpOther= ruleOpOther EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getOpOtherRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleOpOther=ruleOpOther();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleOpOther.getText(); 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleOpOther"


    // $ANTLR start "ruleOpOther"
    // InternalLinkerScript.g:5175:1: ruleOpOther returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : ( (kw= '>' ( ( '>' )=>kw= '>' ) ) | (kw= '<' ( ( '<' )=>kw= '<' ) ) ) ;
    public final AntlrDatatypeRuleToken ruleOpOther() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;


        	enterRule();

        try {
            // InternalLinkerScript.g:5181:2: ( ( (kw= '>' ( ( '>' )=>kw= '>' ) ) | (kw= '<' ( ( '<' )=>kw= '<' ) ) ) )
            // InternalLinkerScript.g:5182:2: ( (kw= '>' ( ( '>' )=>kw= '>' ) ) | (kw= '<' ( ( '<' )=>kw= '<' ) ) )
            {
            // InternalLinkerScript.g:5182:2: ( (kw= '>' ( ( '>' )=>kw= '>' ) ) | (kw= '<' ( ( '<' )=>kw= '<' ) ) )
            int alt74=2;
            int LA74_0 = input.LA(1);

            if ( (LA74_0==40) ) {
                alt74=1;
            }
            else if ( (LA74_0==68) ) {
                alt74=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 74, 0, input);

                throw nvae;
            }
            switch (alt74) {
                case 1 :
                    // InternalLinkerScript.g:5183:3: (kw= '>' ( ( '>' )=>kw= '>' ) )
                    {
                    // InternalLinkerScript.g:5183:3: (kw= '>' ( ( '>' )=>kw= '>' ) )
                    // InternalLinkerScript.g:5184:4: kw= '>' ( ( '>' )=>kw= '>' )
                    {
                    kw=(Token)match(input,40,FOLLOW_31); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				current.merge(kw);
                      				newLeafNode(kw, grammarAccess.getOpOtherAccess().getGreaterThanSignKeyword_0_0());
                      			
                    }
                    // InternalLinkerScript.g:5189:4: ( ( '>' )=>kw= '>' )
                    // InternalLinkerScript.g:5190:5: ( '>' )=>kw= '>'
                    {
                    kw=(Token)match(input,40,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      					current.merge(kw);
                      					newLeafNode(kw, grammarAccess.getOpOtherAccess().getGreaterThanSignKeyword_0_1());
                      				
                    }

                    }


                    }


                    }
                    break;
                case 2 :
                    // InternalLinkerScript.g:5199:3: (kw= '<' ( ( '<' )=>kw= '<' ) )
                    {
                    // InternalLinkerScript.g:5199:3: (kw= '<' ( ( '<' )=>kw= '<' ) )
                    // InternalLinkerScript.g:5200:4: kw= '<' ( ( '<' )=>kw= '<' )
                    {
                    kw=(Token)match(input,68,FOLLOW_37); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				current.merge(kw);
                      				newLeafNode(kw, grammarAccess.getOpOtherAccess().getLessThanSignKeyword_1_0());
                      			
                    }
                    // InternalLinkerScript.g:5205:4: ( ( '<' )=>kw= '<' )
                    // InternalLinkerScript.g:5206:5: ( '<' )=>kw= '<'
                    {
                    kw=(Token)match(input,68,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      					current.merge(kw);
                      					newLeafNode(kw, grammarAccess.getOpOtherAccess().getLessThanSignKeyword_1_1());
                      				
                    }

                    }


                    }


                    }
                    break;

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleOpOther"


    // $ANTLR start "entryRuleLAdditiveExpression"
    // InternalLinkerScript.g:5218:1: entryRuleLAdditiveExpression returns [EObject current=null] : iv_ruleLAdditiveExpression= ruleLAdditiveExpression EOF ;
    public final EObject entryRuleLAdditiveExpression() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleLAdditiveExpression = null;


        try {
            // InternalLinkerScript.g:5218:60: (iv_ruleLAdditiveExpression= ruleLAdditiveExpression EOF )
            // InternalLinkerScript.g:5219:2: iv_ruleLAdditiveExpression= ruleLAdditiveExpression EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getLAdditiveExpressionRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleLAdditiveExpression=ruleLAdditiveExpression();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleLAdditiveExpression; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleLAdditiveExpression"


    // $ANTLR start "ruleLAdditiveExpression"
    // InternalLinkerScript.g:5225:1: ruleLAdditiveExpression returns [EObject current=null] : (this_LMultiplicativeExpression_0= ruleLMultiplicativeExpression ( ( ( ( () ( ( ruleOpAdd ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpAdd ) ) ) ) ( (lv_rightOperand_3_0= ruleLMultiplicativeExpression ) ) )* ) ;
    public final EObject ruleLAdditiveExpression() throws RecognitionException {
        EObject current = null;

        EObject this_LMultiplicativeExpression_0 = null;

        AntlrDatatypeRuleToken lv_feature_2_0 = null;

        EObject lv_rightOperand_3_0 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:5231:2: ( (this_LMultiplicativeExpression_0= ruleLMultiplicativeExpression ( ( ( ( () ( ( ruleOpAdd ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpAdd ) ) ) ) ( (lv_rightOperand_3_0= ruleLMultiplicativeExpression ) ) )* ) )
            // InternalLinkerScript.g:5232:2: (this_LMultiplicativeExpression_0= ruleLMultiplicativeExpression ( ( ( ( () ( ( ruleOpAdd ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpAdd ) ) ) ) ( (lv_rightOperand_3_0= ruleLMultiplicativeExpression ) ) )* )
            {
            // InternalLinkerScript.g:5232:2: (this_LMultiplicativeExpression_0= ruleLMultiplicativeExpression ( ( ( ( () ( ( ruleOpAdd ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpAdd ) ) ) ) ( (lv_rightOperand_3_0= ruleLMultiplicativeExpression ) ) )* )
            // InternalLinkerScript.g:5233:3: this_LMultiplicativeExpression_0= ruleLMultiplicativeExpression ( ( ( ( () ( ( ruleOpAdd ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpAdd ) ) ) ) ( (lv_rightOperand_3_0= ruleLMultiplicativeExpression ) ) )*
            {
            if ( state.backtracking==0 ) {

              			newCompositeNode(grammarAccess.getLAdditiveExpressionAccess().getLMultiplicativeExpressionParserRuleCall_0());
              		
            }
            pushFollow(FOLLOW_58);
            this_LMultiplicativeExpression_0=ruleLMultiplicativeExpression();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current = this_LMultiplicativeExpression_0;
              			afterParserOrEnumRuleCall();
              		
            }
            // InternalLinkerScript.g:5241:3: ( ( ( ( () ( ( ruleOpAdd ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpAdd ) ) ) ) ( (lv_rightOperand_3_0= ruleLMultiplicativeExpression ) ) )*
            loop75:
            do {
                int alt75=2;
                alt75 = dfa75.predict(input);
                switch (alt75) {
            	case 1 :
            	    // InternalLinkerScript.g:5242:4: ( ( ( () ( ( ruleOpAdd ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpAdd ) ) ) ) ( (lv_rightOperand_3_0= ruleLMultiplicativeExpression ) )
            	    {
            	    // InternalLinkerScript.g:5242:4: ( ( ( () ( ( ruleOpAdd ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpAdd ) ) ) )
            	    // InternalLinkerScript.g:5243:5: ( ( () ( ( ruleOpAdd ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpAdd ) ) )
            	    {
            	    // InternalLinkerScript.g:5253:5: ( () ( (lv_feature_2_0= ruleOpAdd ) ) )
            	    // InternalLinkerScript.g:5254:6: () ( (lv_feature_2_0= ruleOpAdd ) )
            	    {
            	    // InternalLinkerScript.g:5254:6: ()
            	    // InternalLinkerScript.g:5255:7: 
            	    {
            	    if ( state.backtracking==0 ) {

            	      							current = forceCreateModelElementAndSet(
            	      								grammarAccess.getLAdditiveExpressionAccess().getLBinaryOperationLeftOperandAction_1_0_0_0(),
            	      								current);
            	      						
            	    }

            	    }

            	    // InternalLinkerScript.g:5261:6: ( (lv_feature_2_0= ruleOpAdd ) )
            	    // InternalLinkerScript.g:5262:7: (lv_feature_2_0= ruleOpAdd )
            	    {
            	    // InternalLinkerScript.g:5262:7: (lv_feature_2_0= ruleOpAdd )
            	    // InternalLinkerScript.g:5263:8: lv_feature_2_0= ruleOpAdd
            	    {
            	    if ( state.backtracking==0 ) {

            	      								newCompositeNode(grammarAccess.getLAdditiveExpressionAccess().getFeatureOpAddParserRuleCall_1_0_0_1_0());
            	      							
            	    }
            	    pushFollow(FOLLOW_8);
            	    lv_feature_2_0=ruleOpAdd();

            	    state._fsp--;
            	    if (state.failed) return current;
            	    if ( state.backtracking==0 ) {

            	      								if (current==null) {
            	      									current = createModelElementForParent(grammarAccess.getLAdditiveExpressionRule());
            	      								}
            	      								set(
            	      									current,
            	      									"feature",
            	      									lv_feature_2_0,
            	      									"org.eclipse.cdt.linkerscript.LinkerScript.OpAdd");
            	      								afterParserOrEnumRuleCall();
            	      							
            	    }

            	    }


            	    }


            	    }


            	    }

            	    // InternalLinkerScript.g:5282:4: ( (lv_rightOperand_3_0= ruleLMultiplicativeExpression ) )
            	    // InternalLinkerScript.g:5283:5: (lv_rightOperand_3_0= ruleLMultiplicativeExpression )
            	    {
            	    // InternalLinkerScript.g:5283:5: (lv_rightOperand_3_0= ruleLMultiplicativeExpression )
            	    // InternalLinkerScript.g:5284:6: lv_rightOperand_3_0= ruleLMultiplicativeExpression
            	    {
            	    if ( state.backtracking==0 ) {

            	      						newCompositeNode(grammarAccess.getLAdditiveExpressionAccess().getRightOperandLMultiplicativeExpressionParserRuleCall_1_1_0());
            	      					
            	    }
            	    pushFollow(FOLLOW_58);
            	    lv_rightOperand_3_0=ruleLMultiplicativeExpression();

            	    state._fsp--;
            	    if (state.failed) return current;
            	    if ( state.backtracking==0 ) {

            	      						if (current==null) {
            	      							current = createModelElementForParent(grammarAccess.getLAdditiveExpressionRule());
            	      						}
            	      						set(
            	      							current,
            	      							"rightOperand",
            	      							lv_rightOperand_3_0,
            	      							"org.eclipse.cdt.linkerscript.LinkerScript.LMultiplicativeExpression");
            	      						afterParserOrEnumRuleCall();
            	      					
            	    }

            	    }


            	    }


            	    }
            	    break;

            	default :
            	    break loop75;
                }
            } while (true);


            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleLAdditiveExpression"


    // $ANTLR start "entryRuleOpAdd"
    // InternalLinkerScript.g:5306:1: entryRuleOpAdd returns [String current=null] : iv_ruleOpAdd= ruleOpAdd EOF ;
    public final String entryRuleOpAdd() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleOpAdd = null;


        try {
            // InternalLinkerScript.g:5306:45: (iv_ruleOpAdd= ruleOpAdd EOF )
            // InternalLinkerScript.g:5307:2: iv_ruleOpAdd= ruleOpAdd EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getOpAddRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleOpAdd=ruleOpAdd();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleOpAdd.getText(); 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleOpAdd"


    // $ANTLR start "ruleOpAdd"
    // InternalLinkerScript.g:5313:1: ruleOpAdd returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (kw= '+' | kw= '-' ) ;
    public final AntlrDatatypeRuleToken ruleOpAdd() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;


        	enterRule();

        try {
            // InternalLinkerScript.g:5319:2: ( (kw= '+' | kw= '-' ) )
            // InternalLinkerScript.g:5320:2: (kw= '+' | kw= '-' )
            {
            // InternalLinkerScript.g:5320:2: (kw= '+' | kw= '-' )
            int alt76=2;
            int LA76_0 = input.LA(1);

            if ( (LA76_0==94) ) {
                alt76=1;
            }
            else if ( (LA76_0==95) ) {
                alt76=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 76, 0, input);

                throw nvae;
            }
            switch (alt76) {
                case 1 :
                    // InternalLinkerScript.g:5321:3: kw= '+'
                    {
                    kw=(Token)match(input,94,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getOpAddAccess().getPlusSignKeyword_0());
                      		
                    }

                    }
                    break;
                case 2 :
                    // InternalLinkerScript.g:5327:3: kw= '-'
                    {
                    kw=(Token)match(input,95,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getOpAddAccess().getHyphenMinusKeyword_1());
                      		
                    }

                    }
                    break;

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleOpAdd"


    // $ANTLR start "entryRuleLMultiplicativeExpression"
    // InternalLinkerScript.g:5336:1: entryRuleLMultiplicativeExpression returns [EObject current=null] : iv_ruleLMultiplicativeExpression= ruleLMultiplicativeExpression EOF ;
    public final EObject entryRuleLMultiplicativeExpression() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleLMultiplicativeExpression = null;


        try {
            // InternalLinkerScript.g:5336:66: (iv_ruleLMultiplicativeExpression= ruleLMultiplicativeExpression EOF )
            // InternalLinkerScript.g:5337:2: iv_ruleLMultiplicativeExpression= ruleLMultiplicativeExpression EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getLMultiplicativeExpressionRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleLMultiplicativeExpression=ruleLMultiplicativeExpression();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleLMultiplicativeExpression; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleLMultiplicativeExpression"


    // $ANTLR start "ruleLMultiplicativeExpression"
    // InternalLinkerScript.g:5343:1: ruleLMultiplicativeExpression returns [EObject current=null] : (this_LUnaryOperation_0= ruleLUnaryOperation ( ( ( ( () ( ( ruleOpMulti ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpMulti ) ) ) ) ( (lv_rightOperand_3_0= ruleLUnaryOperation ) ) )* ) ;
    public final EObject ruleLMultiplicativeExpression() throws RecognitionException {
        EObject current = null;

        EObject this_LUnaryOperation_0 = null;

        AntlrDatatypeRuleToken lv_feature_2_0 = null;

        EObject lv_rightOperand_3_0 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:5349:2: ( (this_LUnaryOperation_0= ruleLUnaryOperation ( ( ( ( () ( ( ruleOpMulti ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpMulti ) ) ) ) ( (lv_rightOperand_3_0= ruleLUnaryOperation ) ) )* ) )
            // InternalLinkerScript.g:5350:2: (this_LUnaryOperation_0= ruleLUnaryOperation ( ( ( ( () ( ( ruleOpMulti ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpMulti ) ) ) ) ( (lv_rightOperand_3_0= ruleLUnaryOperation ) ) )* )
            {
            // InternalLinkerScript.g:5350:2: (this_LUnaryOperation_0= ruleLUnaryOperation ( ( ( ( () ( ( ruleOpMulti ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpMulti ) ) ) ) ( (lv_rightOperand_3_0= ruleLUnaryOperation ) ) )* )
            // InternalLinkerScript.g:5351:3: this_LUnaryOperation_0= ruleLUnaryOperation ( ( ( ( () ( ( ruleOpMulti ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpMulti ) ) ) ) ( (lv_rightOperand_3_0= ruleLUnaryOperation ) ) )*
            {
            if ( state.backtracking==0 ) {

              			newCompositeNode(grammarAccess.getLMultiplicativeExpressionAccess().getLUnaryOperationParserRuleCall_0());
              		
            }
            pushFollow(FOLLOW_59);
            this_LUnaryOperation_0=ruleLUnaryOperation();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current = this_LUnaryOperation_0;
              			afterParserOrEnumRuleCall();
              		
            }
            // InternalLinkerScript.g:5359:3: ( ( ( ( () ( ( ruleOpMulti ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpMulti ) ) ) ) ( (lv_rightOperand_3_0= ruleLUnaryOperation ) ) )*
            loop77:
            do {
                int alt77=2;
                alt77 = dfa77.predict(input);
                switch (alt77) {
            	case 1 :
            	    // InternalLinkerScript.g:5360:4: ( ( ( () ( ( ruleOpMulti ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpMulti ) ) ) ) ( (lv_rightOperand_3_0= ruleLUnaryOperation ) )
            	    {
            	    // InternalLinkerScript.g:5360:4: ( ( ( () ( ( ruleOpMulti ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpMulti ) ) ) )
            	    // InternalLinkerScript.g:5361:5: ( ( () ( ( ruleOpMulti ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpMulti ) ) )
            	    {
            	    // InternalLinkerScript.g:5371:5: ( () ( (lv_feature_2_0= ruleOpMulti ) ) )
            	    // InternalLinkerScript.g:5372:6: () ( (lv_feature_2_0= ruleOpMulti ) )
            	    {
            	    // InternalLinkerScript.g:5372:6: ()
            	    // InternalLinkerScript.g:5373:7: 
            	    {
            	    if ( state.backtracking==0 ) {

            	      							current = forceCreateModelElementAndSet(
            	      								grammarAccess.getLMultiplicativeExpressionAccess().getLBinaryOperationLeftOperandAction_1_0_0_0(),
            	      								current);
            	      						
            	    }

            	    }

            	    // InternalLinkerScript.g:5379:6: ( (lv_feature_2_0= ruleOpMulti ) )
            	    // InternalLinkerScript.g:5380:7: (lv_feature_2_0= ruleOpMulti )
            	    {
            	    // InternalLinkerScript.g:5380:7: (lv_feature_2_0= ruleOpMulti )
            	    // InternalLinkerScript.g:5381:8: lv_feature_2_0= ruleOpMulti
            	    {
            	    if ( state.backtracking==0 ) {

            	      								newCompositeNode(grammarAccess.getLMultiplicativeExpressionAccess().getFeatureOpMultiParserRuleCall_1_0_0_1_0());
            	      							
            	    }
            	    pushFollow(FOLLOW_8);
            	    lv_feature_2_0=ruleOpMulti();

            	    state._fsp--;
            	    if (state.failed) return current;
            	    if ( state.backtracking==0 ) {

            	      								if (current==null) {
            	      									current = createModelElementForParent(grammarAccess.getLMultiplicativeExpressionRule());
            	      								}
            	      								set(
            	      									current,
            	      									"feature",
            	      									lv_feature_2_0,
            	      									"org.eclipse.cdt.linkerscript.LinkerScript.OpMulti");
            	      								afterParserOrEnumRuleCall();
            	      							
            	    }

            	    }


            	    }


            	    }


            	    }

            	    // InternalLinkerScript.g:5400:4: ( (lv_rightOperand_3_0= ruleLUnaryOperation ) )
            	    // InternalLinkerScript.g:5401:5: (lv_rightOperand_3_0= ruleLUnaryOperation )
            	    {
            	    // InternalLinkerScript.g:5401:5: (lv_rightOperand_3_0= ruleLUnaryOperation )
            	    // InternalLinkerScript.g:5402:6: lv_rightOperand_3_0= ruleLUnaryOperation
            	    {
            	    if ( state.backtracking==0 ) {

            	      						newCompositeNode(grammarAccess.getLMultiplicativeExpressionAccess().getRightOperandLUnaryOperationParserRuleCall_1_1_0());
            	      					
            	    }
            	    pushFollow(FOLLOW_59);
            	    lv_rightOperand_3_0=ruleLUnaryOperation();

            	    state._fsp--;
            	    if (state.failed) return current;
            	    if ( state.backtracking==0 ) {

            	      						if (current==null) {
            	      							current = createModelElementForParent(grammarAccess.getLMultiplicativeExpressionRule());
            	      						}
            	      						set(
            	      							current,
            	      							"rightOperand",
            	      							lv_rightOperand_3_0,
            	      							"org.eclipse.cdt.linkerscript.LinkerScript.LUnaryOperation");
            	      						afterParserOrEnumRuleCall();
            	      					
            	    }

            	    }


            	    }


            	    }
            	    break;

            	default :
            	    break loop77;
                }
            } while (true);


            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleLMultiplicativeExpression"


    // $ANTLR start "entryRuleOpMulti"
    // InternalLinkerScript.g:5424:1: entryRuleOpMulti returns [String current=null] : iv_ruleOpMulti= ruleOpMulti EOF ;
    public final String entryRuleOpMulti() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleOpMulti = null;


        try {
            // InternalLinkerScript.g:5424:47: (iv_ruleOpMulti= ruleOpMulti EOF )
            // InternalLinkerScript.g:5425:2: iv_ruleOpMulti= ruleOpMulti EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getOpMultiRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleOpMulti=ruleOpMulti();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleOpMulti.getText(); 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleOpMulti"


    // $ANTLR start "ruleOpMulti"
    // InternalLinkerScript.g:5431:1: ruleOpMulti returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (kw= '*' | kw= '/' | kw= '%' ) ;
    public final AntlrDatatypeRuleToken ruleOpMulti() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;


        	enterRule();

        try {
            // InternalLinkerScript.g:5437:2: ( (kw= '*' | kw= '/' | kw= '%' ) )
            // InternalLinkerScript.g:5438:2: (kw= '*' | kw= '/' | kw= '%' )
            {
            // InternalLinkerScript.g:5438:2: (kw= '*' | kw= '/' | kw= '%' )
            int alt78=3;
            switch ( input.LA(1) ) {
            case 96:
                {
                alt78=1;
                }
                break;
            case 97:
                {
                alt78=2;
                }
                break;
            case 98:
                {
                alt78=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 78, 0, input);

                throw nvae;
            }

            switch (alt78) {
                case 1 :
                    // InternalLinkerScript.g:5439:3: kw= '*'
                    {
                    kw=(Token)match(input,96,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getOpMultiAccess().getAsteriskKeyword_0());
                      		
                    }

                    }
                    break;
                case 2 :
                    // InternalLinkerScript.g:5445:3: kw= '/'
                    {
                    kw=(Token)match(input,97,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getOpMultiAccess().getSolidusKeyword_1());
                      		
                    }

                    }
                    break;
                case 3 :
                    // InternalLinkerScript.g:5451:3: kw= '%'
                    {
                    kw=(Token)match(input,98,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getOpMultiAccess().getPercentSignKeyword_2());
                      		
                    }

                    }
                    break;

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleOpMulti"


    // $ANTLR start "entryRuleLUnaryOperation"
    // InternalLinkerScript.g:5460:1: entryRuleLUnaryOperation returns [EObject current=null] : iv_ruleLUnaryOperation= ruleLUnaryOperation EOF ;
    public final EObject entryRuleLUnaryOperation() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleLUnaryOperation = null;


        try {
            // InternalLinkerScript.g:5460:56: (iv_ruleLUnaryOperation= ruleLUnaryOperation EOF )
            // InternalLinkerScript.g:5461:2: iv_ruleLUnaryOperation= ruleLUnaryOperation EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getLUnaryOperationRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleLUnaryOperation=ruleLUnaryOperation();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleLUnaryOperation; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleLUnaryOperation"


    // $ANTLR start "ruleLUnaryOperation"
    // InternalLinkerScript.g:5467:1: ruleLUnaryOperation returns [EObject current=null] : ( ( () ( (lv_feature_1_0= ruleOpUnary ) ) ( (lv_operand_2_0= ruleLUnaryOperation ) ) ) | this_LPostfixOperation_3= ruleLPostfixOperation ) ;
    public final EObject ruleLUnaryOperation() throws RecognitionException {
        EObject current = null;

        AntlrDatatypeRuleToken lv_feature_1_0 = null;

        EObject lv_operand_2_0 = null;

        EObject this_LPostfixOperation_3 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:5473:2: ( ( ( () ( (lv_feature_1_0= ruleOpUnary ) ) ( (lv_operand_2_0= ruleLUnaryOperation ) ) ) | this_LPostfixOperation_3= ruleLPostfixOperation ) )
            // InternalLinkerScript.g:5474:2: ( ( () ( (lv_feature_1_0= ruleOpUnary ) ) ( (lv_operand_2_0= ruleLUnaryOperation ) ) ) | this_LPostfixOperation_3= ruleLPostfixOperation )
            {
            // InternalLinkerScript.g:5474:2: ( ( () ( (lv_feature_1_0= ruleOpUnary ) ) ( (lv_operand_2_0= ruleLUnaryOperation ) ) ) | this_LPostfixOperation_3= ruleLPostfixOperation )
            int alt79=2;
            int LA79_0 = input.LA(1);

            if ( (LA79_0==87||(LA79_0>=94 && LA79_0<=95)||LA79_0==99) ) {
                alt79=1;
            }
            else if ( ((LA79_0>=RULE_ID && LA79_0<=RULE_HEX)||LA79_0==13||LA79_0==38||LA79_0==42||LA79_0==80||(LA79_0>=82 && LA79_0<=86)||LA79_0==102) ) {
                alt79=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 79, 0, input);

                throw nvae;
            }
            switch (alt79) {
                case 1 :
                    // InternalLinkerScript.g:5475:3: ( () ( (lv_feature_1_0= ruleOpUnary ) ) ( (lv_operand_2_0= ruleLUnaryOperation ) ) )
                    {
                    // InternalLinkerScript.g:5475:3: ( () ( (lv_feature_1_0= ruleOpUnary ) ) ( (lv_operand_2_0= ruleLUnaryOperation ) ) )
                    // InternalLinkerScript.g:5476:4: () ( (lv_feature_1_0= ruleOpUnary ) ) ( (lv_operand_2_0= ruleLUnaryOperation ) )
                    {
                    // InternalLinkerScript.g:5476:4: ()
                    // InternalLinkerScript.g:5477:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getLUnaryOperationAccess().getLUnaryOperationAction_0_0(),
                      						current);
                      				
                    }

                    }

                    // InternalLinkerScript.g:5483:4: ( (lv_feature_1_0= ruleOpUnary ) )
                    // InternalLinkerScript.g:5484:5: (lv_feature_1_0= ruleOpUnary )
                    {
                    // InternalLinkerScript.g:5484:5: (lv_feature_1_0= ruleOpUnary )
                    // InternalLinkerScript.g:5485:6: lv_feature_1_0= ruleOpUnary
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getLUnaryOperationAccess().getFeatureOpUnaryParserRuleCall_0_1_0());
                      					
                    }
                    pushFollow(FOLLOW_8);
                    lv_feature_1_0=ruleOpUnary();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getLUnaryOperationRule());
                      						}
                      						set(
                      							current,
                      							"feature",
                      							lv_feature_1_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.OpUnary");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    // InternalLinkerScript.g:5502:4: ( (lv_operand_2_0= ruleLUnaryOperation ) )
                    // InternalLinkerScript.g:5503:5: (lv_operand_2_0= ruleLUnaryOperation )
                    {
                    // InternalLinkerScript.g:5503:5: (lv_operand_2_0= ruleLUnaryOperation )
                    // InternalLinkerScript.g:5504:6: lv_operand_2_0= ruleLUnaryOperation
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getLUnaryOperationAccess().getOperandLUnaryOperationParserRuleCall_0_2_0());
                      					
                    }
                    pushFollow(FOLLOW_2);
                    lv_operand_2_0=ruleLUnaryOperation();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getLUnaryOperationRule());
                      						}
                      						set(
                      							current,
                      							"operand",
                      							lv_operand_2_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.LUnaryOperation");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }


                    }


                    }
                    break;
                case 2 :
                    // InternalLinkerScript.g:5523:3: this_LPostfixOperation_3= ruleLPostfixOperation
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getLUnaryOperationAccess().getLPostfixOperationParserRuleCall_1());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_LPostfixOperation_3=ruleLPostfixOperation();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_LPostfixOperation_3;
                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleLUnaryOperation"


    // $ANTLR start "entryRuleOpUnary"
    // InternalLinkerScript.g:5535:1: entryRuleOpUnary returns [String current=null] : iv_ruleOpUnary= ruleOpUnary EOF ;
    public final String entryRuleOpUnary() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleOpUnary = null;


        try {
            // InternalLinkerScript.g:5535:47: (iv_ruleOpUnary= ruleOpUnary EOF )
            // InternalLinkerScript.g:5536:2: iv_ruleOpUnary= ruleOpUnary EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getOpUnaryRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleOpUnary=ruleOpUnary();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleOpUnary.getText(); 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleOpUnary"


    // $ANTLR start "ruleOpUnary"
    // InternalLinkerScript.g:5542:1: ruleOpUnary returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (kw= '!' | kw= '-' | kw= '+' | kw= '~' ) ;
    public final AntlrDatatypeRuleToken ruleOpUnary() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;


        	enterRule();

        try {
            // InternalLinkerScript.g:5548:2: ( (kw= '!' | kw= '-' | kw= '+' | kw= '~' ) )
            // InternalLinkerScript.g:5549:2: (kw= '!' | kw= '-' | kw= '+' | kw= '~' )
            {
            // InternalLinkerScript.g:5549:2: (kw= '!' | kw= '-' | kw= '+' | kw= '~' )
            int alt80=4;
            switch ( input.LA(1) ) {
            case 87:
                {
                alt80=1;
                }
                break;
            case 95:
                {
                alt80=2;
                }
                break;
            case 94:
                {
                alt80=3;
                }
                break;
            case 99:
                {
                alt80=4;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 80, 0, input);

                throw nvae;
            }

            switch (alt80) {
                case 1 :
                    // InternalLinkerScript.g:5550:3: kw= '!'
                    {
                    kw=(Token)match(input,87,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getOpUnaryAccess().getExclamationMarkKeyword_0());
                      		
                    }

                    }
                    break;
                case 2 :
                    // InternalLinkerScript.g:5556:3: kw= '-'
                    {
                    kw=(Token)match(input,95,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getOpUnaryAccess().getHyphenMinusKeyword_1());
                      		
                    }

                    }
                    break;
                case 3 :
                    // InternalLinkerScript.g:5562:3: kw= '+'
                    {
                    kw=(Token)match(input,94,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getOpUnaryAccess().getPlusSignKeyword_2());
                      		
                    }

                    }
                    break;
                case 4 :
                    // InternalLinkerScript.g:5568:3: kw= '~'
                    {
                    kw=(Token)match(input,99,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getOpUnaryAccess().getTildeKeyword_3());
                      		
                    }

                    }
                    break;

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleOpUnary"


    // $ANTLR start "entryRuleLPostfixOperation"
    // InternalLinkerScript.g:5577:1: entryRuleLPostfixOperation returns [EObject current=null] : iv_ruleLPostfixOperation= ruleLPostfixOperation EOF ;
    public final EObject entryRuleLPostfixOperation() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleLPostfixOperation = null;


        try {
            // InternalLinkerScript.g:5577:58: (iv_ruleLPostfixOperation= ruleLPostfixOperation EOF )
            // InternalLinkerScript.g:5578:2: iv_ruleLPostfixOperation= ruleLPostfixOperation EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getLPostfixOperationRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleLPostfixOperation=ruleLPostfixOperation();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleLPostfixOperation; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleLPostfixOperation"


    // $ANTLR start "ruleLPostfixOperation"
    // InternalLinkerScript.g:5584:1: ruleLPostfixOperation returns [EObject current=null] : (this_LPrimaryExpression_0= ruleLPrimaryExpression ( ( ( () ( ( ruleOpPostfix ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpPostfix ) ) ) )? ) ;
    public final EObject ruleLPostfixOperation() throws RecognitionException {
        EObject current = null;

        EObject this_LPrimaryExpression_0 = null;

        AntlrDatatypeRuleToken lv_feature_2_0 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:5590:2: ( (this_LPrimaryExpression_0= ruleLPrimaryExpression ( ( ( () ( ( ruleOpPostfix ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpPostfix ) ) ) )? ) )
            // InternalLinkerScript.g:5591:2: (this_LPrimaryExpression_0= ruleLPrimaryExpression ( ( ( () ( ( ruleOpPostfix ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpPostfix ) ) ) )? )
            {
            // InternalLinkerScript.g:5591:2: (this_LPrimaryExpression_0= ruleLPrimaryExpression ( ( ( () ( ( ruleOpPostfix ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpPostfix ) ) ) )? )
            // InternalLinkerScript.g:5592:3: this_LPrimaryExpression_0= ruleLPrimaryExpression ( ( ( () ( ( ruleOpPostfix ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpPostfix ) ) ) )?
            {
            if ( state.backtracking==0 ) {

              			newCompositeNode(grammarAccess.getLPostfixOperationAccess().getLPrimaryExpressionParserRuleCall_0());
              		
            }
            pushFollow(FOLLOW_60);
            this_LPrimaryExpression_0=ruleLPrimaryExpression();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current = this_LPrimaryExpression_0;
              			afterParserOrEnumRuleCall();
              		
            }
            // InternalLinkerScript.g:5600:3: ( ( ( () ( ( ruleOpPostfix ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpPostfix ) ) ) )?
            int alt81=2;
            int LA81_0 = input.LA(1);

            if ( (LA81_0==100) && (synpred13_InternalLinkerScript())) {
                alt81=1;
            }
            else if ( (LA81_0==101) && (synpred13_InternalLinkerScript())) {
                alt81=1;
            }
            switch (alt81) {
                case 1 :
                    // InternalLinkerScript.g:5601:4: ( ( () ( ( ruleOpPostfix ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpPostfix ) ) )
                    {
                    // InternalLinkerScript.g:5611:4: ( () ( (lv_feature_2_0= ruleOpPostfix ) ) )
                    // InternalLinkerScript.g:5612:5: () ( (lv_feature_2_0= ruleOpPostfix ) )
                    {
                    // InternalLinkerScript.g:5612:5: ()
                    // InternalLinkerScript.g:5613:6: 
                    {
                    if ( state.backtracking==0 ) {

                      						current = forceCreateModelElementAndSet(
                      							grammarAccess.getLPostfixOperationAccess().getLPostfixOperationOperandAction_1_0_0(),
                      							current);
                      					
                    }

                    }

                    // InternalLinkerScript.g:5619:5: ( (lv_feature_2_0= ruleOpPostfix ) )
                    // InternalLinkerScript.g:5620:6: (lv_feature_2_0= ruleOpPostfix )
                    {
                    // InternalLinkerScript.g:5620:6: (lv_feature_2_0= ruleOpPostfix )
                    // InternalLinkerScript.g:5621:7: lv_feature_2_0= ruleOpPostfix
                    {
                    if ( state.backtracking==0 ) {

                      							newCompositeNode(grammarAccess.getLPostfixOperationAccess().getFeatureOpPostfixParserRuleCall_1_0_1_0());
                      						
                    }
                    pushFollow(FOLLOW_2);
                    lv_feature_2_0=ruleOpPostfix();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      							if (current==null) {
                      								current = createModelElementForParent(grammarAccess.getLPostfixOperationRule());
                      							}
                      							set(
                      								current,
                      								"feature",
                      								lv_feature_2_0,
                      								"org.eclipse.cdt.linkerscript.LinkerScript.OpPostfix");
                      							afterParserOrEnumRuleCall();
                      						
                    }

                    }


                    }


                    }


                    }
                    break;

            }


            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleLPostfixOperation"


    // $ANTLR start "entryRuleOpPostfix"
    // InternalLinkerScript.g:5644:1: entryRuleOpPostfix returns [String current=null] : iv_ruleOpPostfix= ruleOpPostfix EOF ;
    public final String entryRuleOpPostfix() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleOpPostfix = null;


        try {
            // InternalLinkerScript.g:5644:49: (iv_ruleOpPostfix= ruleOpPostfix EOF )
            // InternalLinkerScript.g:5645:2: iv_ruleOpPostfix= ruleOpPostfix EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getOpPostfixRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleOpPostfix=ruleOpPostfix();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleOpPostfix.getText(); 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleOpPostfix"


    // $ANTLR start "ruleOpPostfix"
    // InternalLinkerScript.g:5651:1: ruleOpPostfix returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (kw= '++' | kw= '--' ) ;
    public final AntlrDatatypeRuleToken ruleOpPostfix() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;


        	enterRule();

        try {
            // InternalLinkerScript.g:5657:2: ( (kw= '++' | kw= '--' ) )
            // InternalLinkerScript.g:5658:2: (kw= '++' | kw= '--' )
            {
            // InternalLinkerScript.g:5658:2: (kw= '++' | kw= '--' )
            int alt82=2;
            int LA82_0 = input.LA(1);

            if ( (LA82_0==100) ) {
                alt82=1;
            }
            else if ( (LA82_0==101) ) {
                alt82=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 82, 0, input);

                throw nvae;
            }
            switch (alt82) {
                case 1 :
                    // InternalLinkerScript.g:5659:3: kw= '++'
                    {
                    kw=(Token)match(input,100,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getOpPostfixAccess().getPlusSignPlusSignKeyword_0());
                      		
                    }

                    }
                    break;
                case 2 :
                    // InternalLinkerScript.g:5665:3: kw= '--'
                    {
                    kw=(Token)match(input,101,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getOpPostfixAccess().getHyphenMinusHyphenMinusKeyword_1());
                      		
                    }

                    }
                    break;

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleOpPostfix"


    // $ANTLR start "entryRuleLPrimaryExpression"
    // InternalLinkerScript.g:5674:1: entryRuleLPrimaryExpression returns [EObject current=null] : iv_ruleLPrimaryExpression= ruleLPrimaryExpression EOF ;
    public final EObject entryRuleLPrimaryExpression() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleLPrimaryExpression = null;


        try {
            // InternalLinkerScript.g:5674:59: (iv_ruleLPrimaryExpression= ruleLPrimaryExpression EOF )
            // InternalLinkerScript.g:5675:2: iv_ruleLPrimaryExpression= ruleLPrimaryExpression EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getLPrimaryExpressionRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleLPrimaryExpression=ruleLPrimaryExpression();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleLPrimaryExpression; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleLPrimaryExpression"


    // $ANTLR start "ruleLPrimaryExpression"
    // InternalLinkerScript.g:5681:1: ruleLPrimaryExpression returns [EObject current=null] : (this_LengthCall_0= ruleLengthCall | this_AlignCall_1= ruleAlignCall | this_SizeofCall_2= ruleSizeofCall | this_AtCall_3= ruleAtCall | this_LNumberLiteral_4= ruleLNumberLiteral | this_LParenthesizedExpression_5= ruleLParenthesizedExpression | this_LVariable_6= ruleLVariable ) ;
    public final EObject ruleLPrimaryExpression() throws RecognitionException {
        EObject current = null;

        EObject this_LengthCall_0 = null;

        EObject this_AlignCall_1 = null;

        EObject this_SizeofCall_2 = null;

        EObject this_AtCall_3 = null;

        EObject this_LNumberLiteral_4 = null;

        EObject this_LParenthesizedExpression_5 = null;

        EObject this_LVariable_6 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:5687:2: ( (this_LengthCall_0= ruleLengthCall | this_AlignCall_1= ruleAlignCall | this_SizeofCall_2= ruleSizeofCall | this_AtCall_3= ruleAtCall | this_LNumberLiteral_4= ruleLNumberLiteral | this_LParenthesizedExpression_5= ruleLParenthesizedExpression | this_LVariable_6= ruleLVariable ) )
            // InternalLinkerScript.g:5688:2: (this_LengthCall_0= ruleLengthCall | this_AlignCall_1= ruleAlignCall | this_SizeofCall_2= ruleSizeofCall | this_AtCall_3= ruleAtCall | this_LNumberLiteral_4= ruleLNumberLiteral | this_LParenthesizedExpression_5= ruleLParenthesizedExpression | this_LVariable_6= ruleLVariable )
            {
            // InternalLinkerScript.g:5688:2: (this_LengthCall_0= ruleLengthCall | this_AlignCall_1= ruleAlignCall | this_SizeofCall_2= ruleSizeofCall | this_AtCall_3= ruleAtCall | this_LNumberLiteral_4= ruleLNumberLiteral | this_LParenthesizedExpression_5= ruleLParenthesizedExpression | this_LVariable_6= ruleLVariable )
            int alt83=7;
            switch ( input.LA(1) ) {
            case 84:
                {
                alt83=1;
                }
                break;
            case 42:
                {
                alt83=2;
                }
                break;
            case 102:
                {
                alt83=3;
                }
                break;
            case 38:
                {
                alt83=4;
                }
                break;
            case RULE_DEC:
            case RULE_HEX:
                {
                alt83=5;
                }
                break;
            case 13:
                {
                alt83=6;
                }
                break;
            case RULE_ID:
            case 80:
            case 82:
            case 83:
            case 85:
            case 86:
                {
                alt83=7;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 83, 0, input);

                throw nvae;
            }

            switch (alt83) {
                case 1 :
                    // InternalLinkerScript.g:5689:3: this_LengthCall_0= ruleLengthCall
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getLPrimaryExpressionAccess().getLengthCallParserRuleCall_0());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_LengthCall_0=ruleLengthCall();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_LengthCall_0;
                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 2 :
                    // InternalLinkerScript.g:5698:3: this_AlignCall_1= ruleAlignCall
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getLPrimaryExpressionAccess().getAlignCallParserRuleCall_1());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_AlignCall_1=ruleAlignCall();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_AlignCall_1;
                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 3 :
                    // InternalLinkerScript.g:5707:3: this_SizeofCall_2= ruleSizeofCall
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getLPrimaryExpressionAccess().getSizeofCallParserRuleCall_2());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_SizeofCall_2=ruleSizeofCall();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_SizeofCall_2;
                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 4 :
                    // InternalLinkerScript.g:5716:3: this_AtCall_3= ruleAtCall
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getLPrimaryExpressionAccess().getAtCallParserRuleCall_3());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_AtCall_3=ruleAtCall();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_AtCall_3;
                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 5 :
                    // InternalLinkerScript.g:5725:3: this_LNumberLiteral_4= ruleLNumberLiteral
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getLPrimaryExpressionAccess().getLNumberLiteralParserRuleCall_4());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_LNumberLiteral_4=ruleLNumberLiteral();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_LNumberLiteral_4;
                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 6 :
                    // InternalLinkerScript.g:5734:3: this_LParenthesizedExpression_5= ruleLParenthesizedExpression
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getLPrimaryExpressionAccess().getLParenthesizedExpressionParserRuleCall_5());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_LParenthesizedExpression_5=ruleLParenthesizedExpression();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_LParenthesizedExpression_5;
                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 7 :
                    // InternalLinkerScript.g:5743:3: this_LVariable_6= ruleLVariable
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getLPrimaryExpressionAccess().getLVariableParserRuleCall_6());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_LVariable_6=ruleLVariable();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_LVariable_6;
                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleLPrimaryExpression"


    // $ANTLR start "entryRuleLVariable"
    // InternalLinkerScript.g:5755:1: entryRuleLVariable returns [EObject current=null] : iv_ruleLVariable= ruleLVariable EOF ;
    public final EObject entryRuleLVariable() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleLVariable = null;


        try {
            // InternalLinkerScript.g:5755:50: (iv_ruleLVariable= ruleLVariable EOF )
            // InternalLinkerScript.g:5756:2: iv_ruleLVariable= ruleLVariable EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getLVariableRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleLVariable=ruleLVariable();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleLVariable; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleLVariable"


    // $ANTLR start "ruleLVariable"
    // InternalLinkerScript.g:5762:1: ruleLVariable returns [EObject current=null] : ( () ( (lv_feature_1_0= ruleValidID ) ) ) ;
    public final EObject ruleLVariable() throws RecognitionException {
        EObject current = null;

        AntlrDatatypeRuleToken lv_feature_1_0 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:5768:2: ( ( () ( (lv_feature_1_0= ruleValidID ) ) ) )
            // InternalLinkerScript.g:5769:2: ( () ( (lv_feature_1_0= ruleValidID ) ) )
            {
            // InternalLinkerScript.g:5769:2: ( () ( (lv_feature_1_0= ruleValidID ) ) )
            // InternalLinkerScript.g:5770:3: () ( (lv_feature_1_0= ruleValidID ) )
            {
            // InternalLinkerScript.g:5770:3: ()
            // InternalLinkerScript.g:5771:4: 
            {
            if ( state.backtracking==0 ) {

              				current = forceCreateModelElement(
              					grammarAccess.getLVariableAccess().getLVariableAction_0(),
              					current);
              			
            }

            }

            // InternalLinkerScript.g:5777:3: ( (lv_feature_1_0= ruleValidID ) )
            // InternalLinkerScript.g:5778:4: (lv_feature_1_0= ruleValidID )
            {
            // InternalLinkerScript.g:5778:4: (lv_feature_1_0= ruleValidID )
            // InternalLinkerScript.g:5779:5: lv_feature_1_0= ruleValidID
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getLVariableAccess().getFeatureValidIDParserRuleCall_1_0());
              				
            }
            pushFollow(FOLLOW_2);
            lv_feature_1_0=ruleValidID();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getLVariableRule());
              					}
              					set(
              						current,
              						"feature",
              						lv_feature_1_0,
              						"org.eclipse.cdt.linkerscript.LinkerScript.ValidID");
              					afterParserOrEnumRuleCall();
              				
            }

            }


            }


            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleLVariable"


    // $ANTLR start "entryRuleLParenthesizedExpression"
    // InternalLinkerScript.g:5800:1: entryRuleLParenthesizedExpression returns [EObject current=null] : iv_ruleLParenthesizedExpression= ruleLParenthesizedExpression EOF ;
    public final EObject entryRuleLParenthesizedExpression() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleLParenthesizedExpression = null;


        try {
            // InternalLinkerScript.g:5800:65: (iv_ruleLParenthesizedExpression= ruleLParenthesizedExpression EOF )
            // InternalLinkerScript.g:5801:2: iv_ruleLParenthesizedExpression= ruleLParenthesizedExpression EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getLParenthesizedExpressionRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleLParenthesizedExpression=ruleLParenthesizedExpression();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleLParenthesizedExpression; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleLParenthesizedExpression"


    // $ANTLR start "ruleLParenthesizedExpression"
    // InternalLinkerScript.g:5807:1: ruleLParenthesizedExpression returns [EObject current=null] : (otherlv_0= '(' this_LExpression_1= ruleLExpression otherlv_2= ')' ) ;
    public final EObject ruleLParenthesizedExpression() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token otherlv_2=null;
        EObject this_LExpression_1 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:5813:2: ( (otherlv_0= '(' this_LExpression_1= ruleLExpression otherlv_2= ')' ) )
            // InternalLinkerScript.g:5814:2: (otherlv_0= '(' this_LExpression_1= ruleLExpression otherlv_2= ')' )
            {
            // InternalLinkerScript.g:5814:2: (otherlv_0= '(' this_LExpression_1= ruleLExpression otherlv_2= ')' )
            // InternalLinkerScript.g:5815:3: otherlv_0= '(' this_LExpression_1= ruleLExpression otherlv_2= ')'
            {
            otherlv_0=(Token)match(input,13,FOLLOW_8); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_0, grammarAccess.getLParenthesizedExpressionAccess().getLeftParenthesisKeyword_0());
              		
            }
            if ( state.backtracking==0 ) {

              			newCompositeNode(grammarAccess.getLParenthesizedExpressionAccess().getLExpressionParserRuleCall_1());
              		
            }
            pushFollow(FOLLOW_7);
            this_LExpression_1=ruleLExpression();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current = this_LExpression_1;
              			afterParserOrEnumRuleCall();
              		
            }
            otherlv_2=(Token)match(input,14,FOLLOW_2); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_2, grammarAccess.getLParenthesizedExpressionAccess().getRightParenthesisKeyword_2());
              		
            }

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleLParenthesizedExpression"


    // $ANTLR start "entryRuleLengthCall"
    // InternalLinkerScript.g:5835:1: entryRuleLengthCall returns [EObject current=null] : iv_ruleLengthCall= ruleLengthCall EOF ;
    public final EObject entryRuleLengthCall() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleLengthCall = null;


        try {
            // InternalLinkerScript.g:5835:51: (iv_ruleLengthCall= ruleLengthCall EOF )
            // InternalLinkerScript.g:5836:2: iv_ruleLengthCall= ruleLengthCall EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getLengthCallRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleLengthCall=ruleLengthCall();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleLengthCall; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleLengthCall"


    // $ANTLR start "ruleLengthCall"
    // InternalLinkerScript.g:5842:1: ruleLengthCall returns [EObject current=null] : ( () otherlv_1= 'LENGTH' otherlv_2= '(' ( (lv_memory_3_0= ruleValidID ) ) otherlv_4= ')' ) ;
    public final EObject ruleLengthCall() throws RecognitionException {
        EObject current = null;

        Token otherlv_1=null;
        Token otherlv_2=null;
        Token otherlv_4=null;
        AntlrDatatypeRuleToken lv_memory_3_0 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:5848:2: ( ( () otherlv_1= 'LENGTH' otherlv_2= '(' ( (lv_memory_3_0= ruleValidID ) ) otherlv_4= ')' ) )
            // InternalLinkerScript.g:5849:2: ( () otherlv_1= 'LENGTH' otherlv_2= '(' ( (lv_memory_3_0= ruleValidID ) ) otherlv_4= ')' )
            {
            // InternalLinkerScript.g:5849:2: ( () otherlv_1= 'LENGTH' otherlv_2= '(' ( (lv_memory_3_0= ruleValidID ) ) otherlv_4= ')' )
            // InternalLinkerScript.g:5850:3: () otherlv_1= 'LENGTH' otherlv_2= '(' ( (lv_memory_3_0= ruleValidID ) ) otherlv_4= ')'
            {
            // InternalLinkerScript.g:5850:3: ()
            // InternalLinkerScript.g:5851:4: 
            {
            if ( state.backtracking==0 ) {

              				current = forceCreateModelElement(
              					grammarAccess.getLengthCallAccess().getLengthCallAction_0(),
              					current);
              			
            }

            }

            otherlv_1=(Token)match(input,84,FOLLOW_5); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_1, grammarAccess.getLengthCallAccess().getLENGTHKeyword_1());
              		
            }
            otherlv_2=(Token)match(input,13,FOLLOW_6); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_2, grammarAccess.getLengthCallAccess().getLeftParenthesisKeyword_2());
              		
            }
            // InternalLinkerScript.g:5865:3: ( (lv_memory_3_0= ruleValidID ) )
            // InternalLinkerScript.g:5866:4: (lv_memory_3_0= ruleValidID )
            {
            // InternalLinkerScript.g:5866:4: (lv_memory_3_0= ruleValidID )
            // InternalLinkerScript.g:5867:5: lv_memory_3_0= ruleValidID
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getLengthCallAccess().getMemoryValidIDParserRuleCall_3_0());
              				
            }
            pushFollow(FOLLOW_7);
            lv_memory_3_0=ruleValidID();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getLengthCallRule());
              					}
              					set(
              						current,
              						"memory",
              						lv_memory_3_0,
              						"org.eclipse.cdt.linkerscript.LinkerScript.ValidID");
              					afterParserOrEnumRuleCall();
              				
            }

            }


            }

            otherlv_4=(Token)match(input,14,FOLLOW_2); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_4, grammarAccess.getLengthCallAccess().getRightParenthesisKeyword_4());
              		
            }

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleLengthCall"


    // $ANTLR start "entryRuleAlignCall"
    // InternalLinkerScript.g:5892:1: entryRuleAlignCall returns [EObject current=null] : iv_ruleAlignCall= ruleAlignCall EOF ;
    public final EObject entryRuleAlignCall() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleAlignCall = null;


        try {
            // InternalLinkerScript.g:5892:50: (iv_ruleAlignCall= ruleAlignCall EOF )
            // InternalLinkerScript.g:5893:2: iv_ruleAlignCall= ruleAlignCall EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getAlignCallRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleAlignCall=ruleAlignCall();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleAlignCall; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleAlignCall"


    // $ANTLR start "ruleAlignCall"
    // InternalLinkerScript.g:5899:1: ruleAlignCall returns [EObject current=null] : ( () otherlv_1= 'ALIGN' otherlv_2= '(' ( (lv_expOrAlign_3_0= ruleLExpression ) ) (otherlv_4= ',' ( (lv_align_5_0= ruleLExpression ) ) )? otherlv_6= ')' ) ;
    public final EObject ruleAlignCall() throws RecognitionException {
        EObject current = null;

        Token otherlv_1=null;
        Token otherlv_2=null;
        Token otherlv_4=null;
        Token otherlv_6=null;
        EObject lv_expOrAlign_3_0 = null;

        EObject lv_align_5_0 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:5905:2: ( ( () otherlv_1= 'ALIGN' otherlv_2= '(' ( (lv_expOrAlign_3_0= ruleLExpression ) ) (otherlv_4= ',' ( (lv_align_5_0= ruleLExpression ) ) )? otherlv_6= ')' ) )
            // InternalLinkerScript.g:5906:2: ( () otherlv_1= 'ALIGN' otherlv_2= '(' ( (lv_expOrAlign_3_0= ruleLExpression ) ) (otherlv_4= ',' ( (lv_align_5_0= ruleLExpression ) ) )? otherlv_6= ')' )
            {
            // InternalLinkerScript.g:5906:2: ( () otherlv_1= 'ALIGN' otherlv_2= '(' ( (lv_expOrAlign_3_0= ruleLExpression ) ) (otherlv_4= ',' ( (lv_align_5_0= ruleLExpression ) ) )? otherlv_6= ')' )
            // InternalLinkerScript.g:5907:3: () otherlv_1= 'ALIGN' otherlv_2= '(' ( (lv_expOrAlign_3_0= ruleLExpression ) ) (otherlv_4= ',' ( (lv_align_5_0= ruleLExpression ) ) )? otherlv_6= ')'
            {
            // InternalLinkerScript.g:5907:3: ()
            // InternalLinkerScript.g:5908:4: 
            {
            if ( state.backtracking==0 ) {

              				current = forceCreateModelElement(
              					grammarAccess.getAlignCallAccess().getAlignCallAction_0(),
              					current);
              			
            }

            }

            otherlv_1=(Token)match(input,42,FOLLOW_5); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_1, grammarAccess.getAlignCallAccess().getALIGNKeyword_1());
              		
            }
            otherlv_2=(Token)match(input,13,FOLLOW_8); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_2, grammarAccess.getAlignCallAccess().getLeftParenthesisKeyword_2());
              		
            }
            // InternalLinkerScript.g:5922:3: ( (lv_expOrAlign_3_0= ruleLExpression ) )
            // InternalLinkerScript.g:5923:4: (lv_expOrAlign_3_0= ruleLExpression )
            {
            // InternalLinkerScript.g:5923:4: (lv_expOrAlign_3_0= ruleLExpression )
            // InternalLinkerScript.g:5924:5: lv_expOrAlign_3_0= ruleLExpression
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getAlignCallAccess().getExpOrAlignLExpressionParserRuleCall_3_0());
              				
            }
            pushFollow(FOLLOW_10);
            lv_expOrAlign_3_0=ruleLExpression();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getAlignCallRule());
              					}
              					set(
              						current,
              						"expOrAlign",
              						lv_expOrAlign_3_0,
              						"org.eclipse.cdt.linkerscript.LinkerScript.LExpression");
              					afterParserOrEnumRuleCall();
              				
            }

            }


            }

            // InternalLinkerScript.g:5941:3: (otherlv_4= ',' ( (lv_align_5_0= ruleLExpression ) ) )?
            int alt84=2;
            int LA84_0 = input.LA(1);

            if ( (LA84_0==10) ) {
                alt84=1;
            }
            switch (alt84) {
                case 1 :
                    // InternalLinkerScript.g:5942:4: otherlv_4= ',' ( (lv_align_5_0= ruleLExpression ) )
                    {
                    otherlv_4=(Token)match(input,10,FOLLOW_8); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_4, grammarAccess.getAlignCallAccess().getCommaKeyword_4_0());
                      			
                    }
                    // InternalLinkerScript.g:5946:4: ( (lv_align_5_0= ruleLExpression ) )
                    // InternalLinkerScript.g:5947:5: (lv_align_5_0= ruleLExpression )
                    {
                    // InternalLinkerScript.g:5947:5: (lv_align_5_0= ruleLExpression )
                    // InternalLinkerScript.g:5948:6: lv_align_5_0= ruleLExpression
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getAlignCallAccess().getAlignLExpressionParserRuleCall_4_1_0());
                      					
                    }
                    pushFollow(FOLLOW_7);
                    lv_align_5_0=ruleLExpression();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getAlignCallRule());
                      						}
                      						set(
                      							current,
                      							"align",
                      							lv_align_5_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.LExpression");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }


                    }
                    break;

            }

            otherlv_6=(Token)match(input,14,FOLLOW_2); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_6, grammarAccess.getAlignCallAccess().getRightParenthesisKeyword_5());
              		
            }

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleAlignCall"


    // $ANTLR start "entryRuleSizeofCall"
    // InternalLinkerScript.g:5974:1: entryRuleSizeofCall returns [EObject current=null] : iv_ruleSizeofCall= ruleSizeofCall EOF ;
    public final EObject entryRuleSizeofCall() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleSizeofCall = null;


        try {
            // InternalLinkerScript.g:5974:51: (iv_ruleSizeofCall= ruleSizeofCall EOF )
            // InternalLinkerScript.g:5975:2: iv_ruleSizeofCall= ruleSizeofCall EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getSizeofCallRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleSizeofCall=ruleSizeofCall();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleSizeofCall; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleSizeofCall"


    // $ANTLR start "ruleSizeofCall"
    // InternalLinkerScript.g:5981:1: ruleSizeofCall returns [EObject current=null] : ( () otherlv_1= 'SIZEOF' otherlv_2= '(' ( (lv_name_3_0= ruleValidID ) ) otherlv_4= ')' ) ;
    public final EObject ruleSizeofCall() throws RecognitionException {
        EObject current = null;

        Token otherlv_1=null;
        Token otherlv_2=null;
        Token otherlv_4=null;
        AntlrDatatypeRuleToken lv_name_3_0 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:5987:2: ( ( () otherlv_1= 'SIZEOF' otherlv_2= '(' ( (lv_name_3_0= ruleValidID ) ) otherlv_4= ')' ) )
            // InternalLinkerScript.g:5988:2: ( () otherlv_1= 'SIZEOF' otherlv_2= '(' ( (lv_name_3_0= ruleValidID ) ) otherlv_4= ')' )
            {
            // InternalLinkerScript.g:5988:2: ( () otherlv_1= 'SIZEOF' otherlv_2= '(' ( (lv_name_3_0= ruleValidID ) ) otherlv_4= ')' )
            // InternalLinkerScript.g:5989:3: () otherlv_1= 'SIZEOF' otherlv_2= '(' ( (lv_name_3_0= ruleValidID ) ) otherlv_4= ')'
            {
            // InternalLinkerScript.g:5989:3: ()
            // InternalLinkerScript.g:5990:4: 
            {
            if ( state.backtracking==0 ) {

              				current = forceCreateModelElement(
              					grammarAccess.getSizeofCallAccess().getSizeofCallAction_0(),
              					current);
              			
            }

            }

            otherlv_1=(Token)match(input,102,FOLLOW_5); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_1, grammarAccess.getSizeofCallAccess().getSIZEOFKeyword_1());
              		
            }
            otherlv_2=(Token)match(input,13,FOLLOW_6); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_2, grammarAccess.getSizeofCallAccess().getLeftParenthesisKeyword_2());
              		
            }
            // InternalLinkerScript.g:6004:3: ( (lv_name_3_0= ruleValidID ) )
            // InternalLinkerScript.g:6005:4: (lv_name_3_0= ruleValidID )
            {
            // InternalLinkerScript.g:6005:4: (lv_name_3_0= ruleValidID )
            // InternalLinkerScript.g:6006:5: lv_name_3_0= ruleValidID
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getSizeofCallAccess().getNameValidIDParserRuleCall_3_0());
              				
            }
            pushFollow(FOLLOW_7);
            lv_name_3_0=ruleValidID();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getSizeofCallRule());
              					}
              					set(
              						current,
              						"name",
              						lv_name_3_0,
              						"org.eclipse.cdt.linkerscript.LinkerScript.ValidID");
              					afterParserOrEnumRuleCall();
              				
            }

            }


            }

            otherlv_4=(Token)match(input,14,FOLLOW_2); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_4, grammarAccess.getSizeofCallAccess().getRightParenthesisKeyword_4());
              		
            }

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleSizeofCall"


    // $ANTLR start "entryRuleAtCall"
    // InternalLinkerScript.g:6031:1: entryRuleAtCall returns [EObject current=null] : iv_ruleAtCall= ruleAtCall EOF ;
    public final EObject entryRuleAtCall() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleAtCall = null;


        try {
            // InternalLinkerScript.g:6031:47: (iv_ruleAtCall= ruleAtCall EOF )
            // InternalLinkerScript.g:6032:2: iv_ruleAtCall= ruleAtCall EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getAtCallRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleAtCall=ruleAtCall();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleAtCall; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleAtCall"


    // $ANTLR start "ruleAtCall"
    // InternalLinkerScript.g:6038:1: ruleAtCall returns [EObject current=null] : ( () otherlv_1= 'AT' otherlv_2= '(' ( (lv_exp_3_0= ruleLExpression ) ) otherlv_4= ')' ) ;
    public final EObject ruleAtCall() throws RecognitionException {
        EObject current = null;

        Token otherlv_1=null;
        Token otherlv_2=null;
        Token otherlv_4=null;
        EObject lv_exp_3_0 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:6044:2: ( ( () otherlv_1= 'AT' otherlv_2= '(' ( (lv_exp_3_0= ruleLExpression ) ) otherlv_4= ')' ) )
            // InternalLinkerScript.g:6045:2: ( () otherlv_1= 'AT' otherlv_2= '(' ( (lv_exp_3_0= ruleLExpression ) ) otherlv_4= ')' )
            {
            // InternalLinkerScript.g:6045:2: ( () otherlv_1= 'AT' otherlv_2= '(' ( (lv_exp_3_0= ruleLExpression ) ) otherlv_4= ')' )
            // InternalLinkerScript.g:6046:3: () otherlv_1= 'AT' otherlv_2= '(' ( (lv_exp_3_0= ruleLExpression ) ) otherlv_4= ')'
            {
            // InternalLinkerScript.g:6046:3: ()
            // InternalLinkerScript.g:6047:4: 
            {
            if ( state.backtracking==0 ) {

              				current = forceCreateModelElement(
              					grammarAccess.getAtCallAccess().getAtCallAction_0(),
              					current);
              			
            }

            }

            otherlv_1=(Token)match(input,38,FOLLOW_5); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_1, grammarAccess.getAtCallAccess().getATKeyword_1());
              		
            }
            otherlv_2=(Token)match(input,13,FOLLOW_8); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_2, grammarAccess.getAtCallAccess().getLeftParenthesisKeyword_2());
              		
            }
            // InternalLinkerScript.g:6061:3: ( (lv_exp_3_0= ruleLExpression ) )
            // InternalLinkerScript.g:6062:4: (lv_exp_3_0= ruleLExpression )
            {
            // InternalLinkerScript.g:6062:4: (lv_exp_3_0= ruleLExpression )
            // InternalLinkerScript.g:6063:5: lv_exp_3_0= ruleLExpression
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getAtCallAccess().getExpLExpressionParserRuleCall_3_0());
              				
            }
            pushFollow(FOLLOW_7);
            lv_exp_3_0=ruleLExpression();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getAtCallRule());
              					}
              					set(
              						current,
              						"exp",
              						lv_exp_3_0,
              						"org.eclipse.cdt.linkerscript.LinkerScript.LExpression");
              					afterParserOrEnumRuleCall();
              				
            }

            }


            }

            otherlv_4=(Token)match(input,14,FOLLOW_2); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_4, grammarAccess.getAtCallAccess().getRightParenthesisKeyword_4());
              		
            }

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleAtCall"


    // $ANTLR start "entryRuleLNumberLiteral"
    // InternalLinkerScript.g:6088:1: entryRuleLNumberLiteral returns [EObject current=null] : iv_ruleLNumberLiteral= ruleLNumberLiteral EOF ;
    public final EObject entryRuleLNumberLiteral() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleLNumberLiteral = null;


        try {
            // InternalLinkerScript.g:6088:55: (iv_ruleLNumberLiteral= ruleLNumberLiteral EOF )
            // InternalLinkerScript.g:6089:2: iv_ruleLNumberLiteral= ruleLNumberLiteral EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getLNumberLiteralRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleLNumberLiteral=ruleLNumberLiteral();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleLNumberLiteral; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleLNumberLiteral"


    // $ANTLR start "ruleLNumberLiteral"
    // InternalLinkerScript.g:6095:1: ruleLNumberLiteral returns [EObject current=null] : ( () ( (lv_value_1_0= ruleNumber ) ) ) ;
    public final EObject ruleLNumberLiteral() throws RecognitionException {
        EObject current = null;

        AntlrDatatypeRuleToken lv_value_1_0 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:6101:2: ( ( () ( (lv_value_1_0= ruleNumber ) ) ) )
            // InternalLinkerScript.g:6102:2: ( () ( (lv_value_1_0= ruleNumber ) ) )
            {
            // InternalLinkerScript.g:6102:2: ( () ( (lv_value_1_0= ruleNumber ) ) )
            // InternalLinkerScript.g:6103:3: () ( (lv_value_1_0= ruleNumber ) )
            {
            // InternalLinkerScript.g:6103:3: ()
            // InternalLinkerScript.g:6104:4: 
            {
            if ( state.backtracking==0 ) {

              				current = forceCreateModelElement(
              					grammarAccess.getLNumberLiteralAccess().getLNumberLiteralAction_0(),
              					current);
              			
            }

            }

            // InternalLinkerScript.g:6110:3: ( (lv_value_1_0= ruleNumber ) )
            // InternalLinkerScript.g:6111:4: (lv_value_1_0= ruleNumber )
            {
            // InternalLinkerScript.g:6111:4: (lv_value_1_0= ruleNumber )
            // InternalLinkerScript.g:6112:5: lv_value_1_0= ruleNumber
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getLNumberLiteralAccess().getValueNumberParserRuleCall_1_0());
              				
            }
            pushFollow(FOLLOW_2);
            lv_value_1_0=ruleNumber();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getLNumberLiteralRule());
              					}
              					set(
              						current,
              						"value",
              						lv_value_1_0,
              						"org.eclipse.cdt.linkerscript.LinkerScript.Number");
              					afterParserOrEnumRuleCall();
              				
            }

            }


            }


            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleLNumberLiteral"


    // $ANTLR start "entryRuleValidID"
    // InternalLinkerScript.g:6133:1: entryRuleValidID returns [String current=null] : iv_ruleValidID= ruleValidID EOF ;
    public final String entryRuleValidID() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleValidID = null;



        	HiddenTokens myHiddenTokenState = ((XtextTokenStream)input).setHiddenTokens();

        try {
            // InternalLinkerScript.g:6135:2: (iv_ruleValidID= ruleValidID EOF )
            // InternalLinkerScript.g:6136:2: iv_ruleValidID= ruleValidID EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getValidIDRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleValidID=ruleValidID();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleValidID.getText(); 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {

            	myHiddenTokenState.restore();

        }
        return current;
    }
    // $ANTLR end "entryRuleValidID"


    // $ANTLR start "ruleValidID"
    // InternalLinkerScript.g:6145:1: ruleValidID returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (this_ID_0= RULE_ID | kw= 'MEMORY' | kw= 'o' | kw= 'org' | kw= 'l' | kw= 'len' ) ;
    public final AntlrDatatypeRuleToken ruleValidID() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token this_ID_0=null;
        Token kw=null;


        	enterRule();
        	HiddenTokens myHiddenTokenState = ((XtextTokenStream)input).setHiddenTokens();

        try {
            // InternalLinkerScript.g:6152:2: ( (this_ID_0= RULE_ID | kw= 'MEMORY' | kw= 'o' | kw= 'org' | kw= 'l' | kw= 'len' ) )
            // InternalLinkerScript.g:6153:2: (this_ID_0= RULE_ID | kw= 'MEMORY' | kw= 'o' | kw= 'org' | kw= 'l' | kw= 'len' )
            {
            // InternalLinkerScript.g:6153:2: (this_ID_0= RULE_ID | kw= 'MEMORY' | kw= 'o' | kw= 'org' | kw= 'l' | kw= 'len' )
            int alt85=6;
            switch ( input.LA(1) ) {
            case RULE_ID:
                {
                alt85=1;
                }
                break;
            case 80:
                {
                alt85=2;
                }
                break;
            case 83:
                {
                alt85=3;
                }
                break;
            case 82:
                {
                alt85=4;
                }
                break;
            case 86:
                {
                alt85=5;
                }
                break;
            case 85:
                {
                alt85=6;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 85, 0, input);

                throw nvae;
            }

            switch (alt85) {
                case 1 :
                    // InternalLinkerScript.g:6154:3: this_ID_0= RULE_ID
                    {
                    this_ID_0=(Token)match(input,RULE_ID,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(this_ID_0);
                      		
                    }
                    if ( state.backtracking==0 ) {

                      			newLeafNode(this_ID_0, grammarAccess.getValidIDAccess().getIDTerminalRuleCall_0());
                      		
                    }

                    }
                    break;
                case 2 :
                    // InternalLinkerScript.g:6162:3: kw= 'MEMORY'
                    {
                    kw=(Token)match(input,80,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getValidIDAccess().getMEMORYKeyword_1());
                      		
                    }

                    }
                    break;
                case 3 :
                    // InternalLinkerScript.g:6168:3: kw= 'o'
                    {
                    kw=(Token)match(input,83,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getValidIDAccess().getOKeyword_2());
                      		
                    }

                    }
                    break;
                case 4 :
                    // InternalLinkerScript.g:6174:3: kw= 'org'
                    {
                    kw=(Token)match(input,82,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getValidIDAccess().getOrgKeyword_3());
                      		
                    }

                    }
                    break;
                case 5 :
                    // InternalLinkerScript.g:6180:3: kw= 'l'
                    {
                    kw=(Token)match(input,86,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getValidIDAccess().getLKeyword_4());
                      		
                    }

                    }
                    break;
                case 6 :
                    // InternalLinkerScript.g:6186:3: kw= 'len'
                    {
                    kw=(Token)match(input,85,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getValidIDAccess().getLenKeyword_5());
                      		
                    }

                    }
                    break;

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {

            	myHiddenTokenState.restore();

        }
        return current;
    }
    // $ANTLR end "ruleValidID"


    // $ANTLR start "entryRuleWildID"
    // InternalLinkerScript.g:6198:1: entryRuleWildID returns [String current=null] : iv_ruleWildID= ruleWildID EOF ;
    public final String entryRuleWildID() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleWildID = null;



        	HiddenTokens myHiddenTokenState = ((XtextTokenStream)input).setHiddenTokens();

        try {
            // InternalLinkerScript.g:6200:2: (iv_ruleWildID= ruleWildID EOF )
            // InternalLinkerScript.g:6201:2: iv_ruleWildID= ruleWildID EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getWildIDRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleWildID=ruleWildID();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleWildID.getText(); 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {

            	myHiddenTokenState.restore();

        }
        return current;
    }
    // $ANTLR end "entryRuleWildID"


    // $ANTLR start "ruleWildID"
    // InternalLinkerScript.g:6210:1: ruleWildID returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (kw= '*' | this_ValidID_1= ruleValidID ) ;
    public final AntlrDatatypeRuleToken ruleWildID() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;
        AntlrDatatypeRuleToken this_ValidID_1 = null;



        	enterRule();
        	HiddenTokens myHiddenTokenState = ((XtextTokenStream)input).setHiddenTokens();

        try {
            // InternalLinkerScript.g:6217:2: ( (kw= '*' | this_ValidID_1= ruleValidID ) )
            // InternalLinkerScript.g:6218:2: (kw= '*' | this_ValidID_1= ruleValidID )
            {
            // InternalLinkerScript.g:6218:2: (kw= '*' | this_ValidID_1= ruleValidID )
            int alt86=2;
            int LA86_0 = input.LA(1);

            if ( (LA86_0==96) ) {
                alt86=1;
            }
            else if ( (LA86_0==RULE_ID||LA86_0==80||(LA86_0>=82 && LA86_0<=83)||(LA86_0>=85 && LA86_0<=86)) ) {
                alt86=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 86, 0, input);

                throw nvae;
            }
            switch (alt86) {
                case 1 :
                    // InternalLinkerScript.g:6219:3: kw= '*'
                    {
                    kw=(Token)match(input,96,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getWildIDAccess().getAsteriskKeyword_0());
                      		
                    }

                    }
                    break;
                case 2 :
                    // InternalLinkerScript.g:6225:3: this_ValidID_1= ruleValidID
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getWildIDAccess().getValidIDParserRuleCall_1());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_ValidID_1=ruleValidID();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(this_ValidID_1);
                      		
                    }
                    if ( state.backtracking==0 ) {

                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {

            	myHiddenTokenState.restore();

        }
        return current;
    }
    // $ANTLR end "ruleWildID"


    // $ANTLR start "entryRuleValidFunc"
    // InternalLinkerScript.g:6242:1: entryRuleValidFunc returns [String current=null] : iv_ruleValidFunc= ruleValidFunc EOF ;
    public final String entryRuleValidFunc() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleValidFunc = null;


        try {
            // InternalLinkerScript.g:6242:49: (iv_ruleValidFunc= ruleValidFunc EOF )
            // InternalLinkerScript.g:6243:2: iv_ruleValidFunc= ruleValidFunc EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getValidFuncRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleValidFunc=ruleValidFunc();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleValidFunc.getText(); 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleValidFunc"


    // $ANTLR start "ruleValidFunc"
    // InternalLinkerScript.g:6249:1: ruleValidFunc returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (kw= 'LENGTH' | kw= 'ALIGN' ) ;
    public final AntlrDatatypeRuleToken ruleValidFunc() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;


        	enterRule();

        try {
            // InternalLinkerScript.g:6255:2: ( (kw= 'LENGTH' | kw= 'ALIGN' ) )
            // InternalLinkerScript.g:6256:2: (kw= 'LENGTH' | kw= 'ALIGN' )
            {
            // InternalLinkerScript.g:6256:2: (kw= 'LENGTH' | kw= 'ALIGN' )
            int alt87=2;
            int LA87_0 = input.LA(1);

            if ( (LA87_0==84) ) {
                alt87=1;
            }
            else if ( (LA87_0==42) ) {
                alt87=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 87, 0, input);

                throw nvae;
            }
            switch (alt87) {
                case 1 :
                    // InternalLinkerScript.g:6257:3: kw= 'LENGTH'
                    {
                    kw=(Token)match(input,84,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getValidFuncAccess().getLENGTHKeyword_0());
                      		
                    }

                    }
                    break;
                case 2 :
                    // InternalLinkerScript.g:6263:3: kw= 'ALIGN'
                    {
                    kw=(Token)match(input,42,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getValidFuncAccess().getALIGNKeyword_1());
                      		
                    }

                    }
                    break;

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleValidFunc"


    // $ANTLR start "entryRuleNumber"
    // InternalLinkerScript.g:6272:1: entryRuleNumber returns [String current=null] : iv_ruleNumber= ruleNumber EOF ;
    public final String entryRuleNumber() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleNumber = null;



        	HiddenTokens myHiddenTokenState = ((XtextTokenStream)input).setHiddenTokens();

        try {
            // InternalLinkerScript.g:6274:2: (iv_ruleNumber= ruleNumber EOF )
            // InternalLinkerScript.g:6275:2: iv_ruleNumber= ruleNumber EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getNumberRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleNumber=ruleNumber();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleNumber.getText(); 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {

            	myHiddenTokenState.restore();

        }
        return current;
    }
    // $ANTLR end "entryRuleNumber"


    // $ANTLR start "ruleNumber"
    // InternalLinkerScript.g:6284:1: ruleNumber returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (this_DEC_0= RULE_DEC | this_HEX_1= RULE_HEX ) ;
    public final AntlrDatatypeRuleToken ruleNumber() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token this_DEC_0=null;
        Token this_HEX_1=null;


        	enterRule();
        	HiddenTokens myHiddenTokenState = ((XtextTokenStream)input).setHiddenTokens();

        try {
            // InternalLinkerScript.g:6291:2: ( (this_DEC_0= RULE_DEC | this_HEX_1= RULE_HEX ) )
            // InternalLinkerScript.g:6292:2: (this_DEC_0= RULE_DEC | this_HEX_1= RULE_HEX )
            {
            // InternalLinkerScript.g:6292:2: (this_DEC_0= RULE_DEC | this_HEX_1= RULE_HEX )
            int alt88=2;
            int LA88_0 = input.LA(1);

            if ( (LA88_0==RULE_DEC) ) {
                alt88=1;
            }
            else if ( (LA88_0==RULE_HEX) ) {
                alt88=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 88, 0, input);

                throw nvae;
            }
            switch (alt88) {
                case 1 :
                    // InternalLinkerScript.g:6293:3: this_DEC_0= RULE_DEC
                    {
                    this_DEC_0=(Token)match(input,RULE_DEC,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(this_DEC_0);
                      		
                    }
                    if ( state.backtracking==0 ) {

                      			newLeafNode(this_DEC_0, grammarAccess.getNumberAccess().getDECTerminalRuleCall_0());
                      		
                    }

                    }
                    break;
                case 2 :
                    // InternalLinkerScript.g:6301:3: this_HEX_1= RULE_HEX
                    {
                    this_HEX_1=(Token)match(input,RULE_HEX,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(this_HEX_1);
                      		
                    }
                    if ( state.backtracking==0 ) {

                      			newLeafNode(this_HEX_1, grammarAccess.getNumberAccess().getHEXTerminalRuleCall_1());
                      		
                    }

                    }
                    break;

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {

            	myHiddenTokenState.restore();

        }
        return current;
    }
    // $ANTLR end "ruleNumber"

    // $ANTLR start synpred1_InternalLinkerScript
    public final void synpred1_InternalLinkerScript_fragment() throws RecognitionException {   
        // InternalLinkerScript.g:4312:5: ( ( () '?' ( ( ruleLOrExpression ) ) ':' ) )
        // InternalLinkerScript.g:4312:6: ( () '?' ( ( ruleLOrExpression ) ) ':' )
        {
        // InternalLinkerScript.g:4312:6: ( () '?' ( ( ruleLOrExpression ) ) ':' )
        // InternalLinkerScript.g:4313:6: () '?' ( ( ruleLOrExpression ) ) ':'
        {
        // InternalLinkerScript.g:4313:6: ()
        // InternalLinkerScript.g:4314:6: 
        {
        }

        match(input,88,FOLLOW_8); if (state.failed) return ;
        // InternalLinkerScript.g:4316:6: ( ( ruleLOrExpression ) )
        // InternalLinkerScript.g:4317:7: ( ruleLOrExpression )
        {
        // InternalLinkerScript.g:4317:7: ( ruleLOrExpression )
        // InternalLinkerScript.g:4318:8: ruleLOrExpression
        {
        pushFollow(FOLLOW_23);
        ruleLOrExpression();

        state._fsp--;
        if (state.failed) return ;

        }


        }

        match(input,37,FOLLOW_2); if (state.failed) return ;

        }


        }
    }
    // $ANTLR end synpred1_InternalLinkerScript

    // $ANTLR start synpred2_InternalLinkerScript
    public final void synpred2_InternalLinkerScript_fragment() throws RecognitionException {   
        // InternalLinkerScript.g:4410:5: ( ( () ( ( ruleOpOr ) ) ) )
        // InternalLinkerScript.g:4410:6: ( () ( ( ruleOpOr ) ) )
        {
        // InternalLinkerScript.g:4410:6: ( () ( ( ruleOpOr ) ) )
        // InternalLinkerScript.g:4411:6: () ( ( ruleOpOr ) )
        {
        // InternalLinkerScript.g:4411:6: ()
        // InternalLinkerScript.g:4412:6: 
        {
        }

        // InternalLinkerScript.g:4413:6: ( ( ruleOpOr ) )
        // InternalLinkerScript.g:4414:7: ( ruleOpOr )
        {
        // InternalLinkerScript.g:4414:7: ( ruleOpOr )
        // InternalLinkerScript.g:4415:8: ruleOpOr
        {
        pushFollow(FOLLOW_2);
        ruleOpOr();

        state._fsp--;
        if (state.failed) return ;

        }


        }


        }


        }
    }
    // $ANTLR end synpred2_InternalLinkerScript

    // $ANTLR start synpred3_InternalLinkerScript
    public final void synpred3_InternalLinkerScript_fragment() throws RecognitionException {   
        // InternalLinkerScript.g:4520:5: ( ( () ( ( ruleOpAnd ) ) ) )
        // InternalLinkerScript.g:4520:6: ( () ( ( ruleOpAnd ) ) )
        {
        // InternalLinkerScript.g:4520:6: ( () ( ( ruleOpAnd ) ) )
        // InternalLinkerScript.g:4521:6: () ( ( ruleOpAnd ) )
        {
        // InternalLinkerScript.g:4521:6: ()
        // InternalLinkerScript.g:4522:6: 
        {
        }

        // InternalLinkerScript.g:4523:6: ( ( ruleOpAnd ) )
        // InternalLinkerScript.g:4524:7: ( ruleOpAnd )
        {
        // InternalLinkerScript.g:4524:7: ( ruleOpAnd )
        // InternalLinkerScript.g:4525:8: ruleOpAnd
        {
        pushFollow(FOLLOW_2);
        ruleOpAnd();

        state._fsp--;
        if (state.failed) return ;

        }


        }


        }


        }
    }
    // $ANTLR end synpred3_InternalLinkerScript

    // $ANTLR start synpred4_InternalLinkerScript
    public final void synpred4_InternalLinkerScript_fragment() throws RecognitionException {   
        // InternalLinkerScript.g:4630:5: ( ( () ( ( ruleOpBitwiseOr ) ) ) )
        // InternalLinkerScript.g:4630:6: ( () ( ( ruleOpBitwiseOr ) ) )
        {
        // InternalLinkerScript.g:4630:6: ( () ( ( ruleOpBitwiseOr ) ) )
        // InternalLinkerScript.g:4631:6: () ( ( ruleOpBitwiseOr ) )
        {
        // InternalLinkerScript.g:4631:6: ()
        // InternalLinkerScript.g:4632:6: 
        {
        }

        // InternalLinkerScript.g:4633:6: ( ( ruleOpBitwiseOr ) )
        // InternalLinkerScript.g:4634:7: ( ruleOpBitwiseOr )
        {
        // InternalLinkerScript.g:4634:7: ( ruleOpBitwiseOr )
        // InternalLinkerScript.g:4635:8: ruleOpBitwiseOr
        {
        pushFollow(FOLLOW_2);
        ruleOpBitwiseOr();

        state._fsp--;
        if (state.failed) return ;

        }


        }


        }


        }
    }
    // $ANTLR end synpred4_InternalLinkerScript

    // $ANTLR start synpred5_InternalLinkerScript
    public final void synpred5_InternalLinkerScript_fragment() throws RecognitionException {   
        // InternalLinkerScript.g:4740:5: ( ( () ( ( ruleOpBitwiseAnd ) ) ) )
        // InternalLinkerScript.g:4740:6: ( () ( ( ruleOpBitwiseAnd ) ) )
        {
        // InternalLinkerScript.g:4740:6: ( () ( ( ruleOpBitwiseAnd ) ) )
        // InternalLinkerScript.g:4741:6: () ( ( ruleOpBitwiseAnd ) )
        {
        // InternalLinkerScript.g:4741:6: ()
        // InternalLinkerScript.g:4742:6: 
        {
        }

        // InternalLinkerScript.g:4743:6: ( ( ruleOpBitwiseAnd ) )
        // InternalLinkerScript.g:4744:7: ( ruleOpBitwiseAnd )
        {
        // InternalLinkerScript.g:4744:7: ( ruleOpBitwiseAnd )
        // InternalLinkerScript.g:4745:8: ruleOpBitwiseAnd
        {
        pushFollow(FOLLOW_2);
        ruleOpBitwiseAnd();

        state._fsp--;
        if (state.failed) return ;

        }


        }


        }


        }
    }
    // $ANTLR end synpred5_InternalLinkerScript

    // $ANTLR start synpred6_InternalLinkerScript
    public final void synpred6_InternalLinkerScript_fragment() throws RecognitionException {   
        // InternalLinkerScript.g:4850:5: ( ( () ( ( ruleOpEquality ) ) ) )
        // InternalLinkerScript.g:4850:6: ( () ( ( ruleOpEquality ) ) )
        {
        // InternalLinkerScript.g:4850:6: ( () ( ( ruleOpEquality ) ) )
        // InternalLinkerScript.g:4851:6: () ( ( ruleOpEquality ) )
        {
        // InternalLinkerScript.g:4851:6: ()
        // InternalLinkerScript.g:4852:6: 
        {
        }

        // InternalLinkerScript.g:4853:6: ( ( ruleOpEquality ) )
        // InternalLinkerScript.g:4854:7: ( ruleOpEquality )
        {
        // InternalLinkerScript.g:4854:7: ( ruleOpEquality )
        // InternalLinkerScript.g:4855:8: ruleOpEquality
        {
        pushFollow(FOLLOW_2);
        ruleOpEquality();

        state._fsp--;
        if (state.failed) return ;

        }


        }


        }


        }
    }
    // $ANTLR end synpred6_InternalLinkerScript

    // $ANTLR start synpred7_InternalLinkerScript
    public final void synpred7_InternalLinkerScript_fragment() throws RecognitionException {   
        // InternalLinkerScript.g:4968:5: ( ( () ( ( ruleOpCompare ) ) ) )
        // InternalLinkerScript.g:4968:6: ( () ( ( ruleOpCompare ) ) )
        {
        // InternalLinkerScript.g:4968:6: ( () ( ( ruleOpCompare ) ) )
        // InternalLinkerScript.g:4969:6: () ( ( ruleOpCompare ) )
        {
        // InternalLinkerScript.g:4969:6: ()
        // InternalLinkerScript.g:4970:6: 
        {
        }

        // InternalLinkerScript.g:4971:6: ( ( ruleOpCompare ) )
        // InternalLinkerScript.g:4972:7: ( ruleOpCompare )
        {
        // InternalLinkerScript.g:4972:7: ( ruleOpCompare )
        // InternalLinkerScript.g:4973:8: ruleOpCompare
        {
        pushFollow(FOLLOW_2);
        ruleOpCompare();

        state._fsp--;
        if (state.failed) return ;

        }


        }


        }


        }
    }
    // $ANTLR end synpred7_InternalLinkerScript

    // $ANTLR start synpred8_InternalLinkerScript
    public final void synpred8_InternalLinkerScript_fragment() throws RecognitionException {   
        // InternalLinkerScript.g:5105:5: ( ( () ( ( ruleOpOther ) ) ) )
        // InternalLinkerScript.g:5105:6: ( () ( ( ruleOpOther ) ) )
        {
        // InternalLinkerScript.g:5105:6: ( () ( ( ruleOpOther ) ) )
        // InternalLinkerScript.g:5106:6: () ( ( ruleOpOther ) )
        {
        // InternalLinkerScript.g:5106:6: ()
        // InternalLinkerScript.g:5107:6: 
        {
        }

        // InternalLinkerScript.g:5108:6: ( ( ruleOpOther ) )
        // InternalLinkerScript.g:5109:7: ( ruleOpOther )
        {
        // InternalLinkerScript.g:5109:7: ( ruleOpOther )
        // InternalLinkerScript.g:5110:8: ruleOpOther
        {
        pushFollow(FOLLOW_2);
        ruleOpOther();

        state._fsp--;
        if (state.failed) return ;

        }


        }


        }


        }
    }
    // $ANTLR end synpred8_InternalLinkerScript

    // $ANTLR start synpred11_InternalLinkerScript
    public final void synpred11_InternalLinkerScript_fragment() throws RecognitionException {   
        // InternalLinkerScript.g:5243:5: ( ( () ( ( ruleOpAdd ) ) ) )
        // InternalLinkerScript.g:5243:6: ( () ( ( ruleOpAdd ) ) )
        {
        // InternalLinkerScript.g:5243:6: ( () ( ( ruleOpAdd ) ) )
        // InternalLinkerScript.g:5244:6: () ( ( ruleOpAdd ) )
        {
        // InternalLinkerScript.g:5244:6: ()
        // InternalLinkerScript.g:5245:6: 
        {
        }

        // InternalLinkerScript.g:5246:6: ( ( ruleOpAdd ) )
        // InternalLinkerScript.g:5247:7: ( ruleOpAdd )
        {
        // InternalLinkerScript.g:5247:7: ( ruleOpAdd )
        // InternalLinkerScript.g:5248:8: ruleOpAdd
        {
        pushFollow(FOLLOW_2);
        ruleOpAdd();

        state._fsp--;
        if (state.failed) return ;

        }


        }


        }


        }
    }
    // $ANTLR end synpred11_InternalLinkerScript

    // $ANTLR start synpred12_InternalLinkerScript
    public final void synpred12_InternalLinkerScript_fragment() throws RecognitionException {   
        // InternalLinkerScript.g:5361:5: ( ( () ( ( ruleOpMulti ) ) ) )
        // InternalLinkerScript.g:5361:6: ( () ( ( ruleOpMulti ) ) )
        {
        // InternalLinkerScript.g:5361:6: ( () ( ( ruleOpMulti ) ) )
        // InternalLinkerScript.g:5362:6: () ( ( ruleOpMulti ) )
        {
        // InternalLinkerScript.g:5362:6: ()
        // InternalLinkerScript.g:5363:6: 
        {
        }

        // InternalLinkerScript.g:5364:6: ( ( ruleOpMulti ) )
        // InternalLinkerScript.g:5365:7: ( ruleOpMulti )
        {
        // InternalLinkerScript.g:5365:7: ( ruleOpMulti )
        // InternalLinkerScript.g:5366:8: ruleOpMulti
        {
        pushFollow(FOLLOW_2);
        ruleOpMulti();

        state._fsp--;
        if (state.failed) return ;

        }


        }


        }


        }
    }
    // $ANTLR end synpred12_InternalLinkerScript

    // $ANTLR start synpred13_InternalLinkerScript
    public final void synpred13_InternalLinkerScript_fragment() throws RecognitionException {   
        // InternalLinkerScript.g:5601:4: ( ( () ( ( ruleOpPostfix ) ) ) )
        // InternalLinkerScript.g:5601:5: ( () ( ( ruleOpPostfix ) ) )
        {
        // InternalLinkerScript.g:5601:5: ( () ( ( ruleOpPostfix ) ) )
        // InternalLinkerScript.g:5602:5: () ( ( ruleOpPostfix ) )
        {
        // InternalLinkerScript.g:5602:5: ()
        // InternalLinkerScript.g:5603:5: 
        {
        }

        // InternalLinkerScript.g:5604:5: ( ( ruleOpPostfix ) )
        // InternalLinkerScript.g:5605:6: ( ruleOpPostfix )
        {
        // InternalLinkerScript.g:5605:6: ( ruleOpPostfix )
        // InternalLinkerScript.g:5606:7: ruleOpPostfix
        {
        pushFollow(FOLLOW_2);
        ruleOpPostfix();

        state._fsp--;
        if (state.failed) return ;

        }


        }


        }


        }
    }
    // $ANTLR end synpred13_InternalLinkerScript

    // Delegated rules

    public final boolean synpred12_InternalLinkerScript() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred12_InternalLinkerScript_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred6_InternalLinkerScript() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred6_InternalLinkerScript_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred13_InternalLinkerScript() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred13_InternalLinkerScript_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred3_InternalLinkerScript() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred3_InternalLinkerScript_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred2_InternalLinkerScript() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred2_InternalLinkerScript_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred4_InternalLinkerScript() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred4_InternalLinkerScript_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred7_InternalLinkerScript() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred7_InternalLinkerScript_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred5_InternalLinkerScript() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred5_InternalLinkerScript_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred1_InternalLinkerScript() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred1_InternalLinkerScript_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred8_InternalLinkerScript() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred8_InternalLinkerScript_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred11_InternalLinkerScript() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred11_InternalLinkerScript_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }


    protected DFA35 dfa35 = new DFA35(this);
    protected DFA53 dfa53 = new DFA53(this);
    protected DFA56 dfa56 = new DFA56(this);
    protected DFA75 dfa75 = new DFA75(this);
    protected DFA77 dfa77 = new DFA77(this);
    static final String dfa_1s = "\24\uffff";
    static final String dfa_2s = "\1\uffff\7\20\14\uffff";
    static final String dfa_3s = "\10\4\3\uffff\1\15\6\uffff\1\4\1\uffff";
    static final String dfa_4s = "\10\140\3\uffff\1\15\6\uffff\1\140\1\uffff";
    static final String dfa_5s = "\10\uffff\1\1\1\2\1\3\1\uffff\1\5\1\6\1\7\1\10\1\11\1\12\1\uffff\1\4";
    static final String dfa_6s = "\24\uffff}>";
    static final String[] dfa_7s = {
            "\1\2\6\uffff\1\21\4\uffff\1\16\15\uffff\1\17\25\uffff\1\11\1\12\1\13\1\15\5\14\3\10\10\uffff\1\20\1\uffff\6\20\1\3\1\uffff\1\5\1\4\1\uffff\1\7\1\6\11\uffff\1\1",
            "\1\20\6\uffff\1\20\1\uffff\1\20\2\uffff\1\20\15\uffff\1\20\4\uffff\1\20\4\uffff\2\10\12\uffff\14\20\5\10\1\uffff\2\10\1\20\1\uffff\7\20\1\uffff\2\20\1\uffff\2\20\11\uffff\1\20",
            "\1\20\6\uffff\1\20\1\uffff\1\20\2\uffff\1\20\15\uffff\1\20\4\uffff\1\20\4\uffff\2\10\12\uffff\14\20\5\10\1\uffff\2\10\1\20\1\uffff\7\20\1\uffff\2\20\1\uffff\2\20\11\uffff\1\20",
            "\1\20\6\uffff\1\20\1\uffff\1\20\2\uffff\1\20\15\uffff\1\20\4\uffff\1\20\4\uffff\2\10\12\uffff\14\20\5\10\1\uffff\2\10\1\20\1\uffff\7\20\1\uffff\2\20\1\uffff\2\20\11\uffff\1\20",
            "\1\20\6\uffff\1\20\1\uffff\1\20\2\uffff\1\20\15\uffff\1\20\4\uffff\1\20\4\uffff\2\10\12\uffff\14\20\5\10\1\uffff\2\10\1\20\1\uffff\7\20\1\uffff\2\20\1\uffff\2\20\11\uffff\1\20",
            "\1\20\6\uffff\1\20\1\uffff\1\20\2\uffff\1\20\15\uffff\1\20\4\uffff\1\20\4\uffff\2\10\12\uffff\14\20\5\10\1\uffff\2\10\1\20\1\uffff\7\20\1\uffff\2\20\1\uffff\2\20\11\uffff\1\20",
            "\1\20\6\uffff\1\20\1\uffff\1\20\2\uffff\1\20\15\uffff\1\20\4\uffff\1\20\4\uffff\2\10\12\uffff\14\20\5\10\1\uffff\2\10\1\20\1\uffff\7\20\1\uffff\2\20\1\uffff\2\20\11\uffff\1\20",
            "\1\20\6\uffff\1\20\1\uffff\1\20\2\uffff\1\20\15\uffff\1\20\4\uffff\1\20\4\uffff\2\10\12\uffff\14\20\5\10\1\uffff\2\10\1\20\1\uffff\7\20\1\uffff\2\20\1\uffff\2\20\11\uffff\1\20",
            "",
            "",
            "",
            "\1\22",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\20\60\uffff\1\23\1\20\24\uffff\2\20\2\uffff\2\20\1\uffff\2\20\1\uffff\2\20\11\uffff\1\20",
            ""
    };

    static final short[] dfa_1 = DFA.unpackEncodedString(dfa_1s);
    static final short[] dfa_2 = DFA.unpackEncodedString(dfa_2s);
    static final char[] dfa_3 = DFA.unpackEncodedStringToUnsignedChars(dfa_3s);
    static final char[] dfa_4 = DFA.unpackEncodedStringToUnsignedChars(dfa_4s);
    static final short[] dfa_5 = DFA.unpackEncodedString(dfa_5s);
    static final short[] dfa_6 = DFA.unpackEncodedString(dfa_6s);
    static final short[][] dfa_7 = unpackEncodedStringArray(dfa_7s);

    class DFA35 extends DFA {

        public DFA35(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 35;
            this.eot = dfa_1;
            this.eof = dfa_2;
            this.min = dfa_3;
            this.max = dfa_4;
            this.accept = dfa_5;
            this.special = dfa_6;
            this.transition = dfa_7;
        }
        public String getDescription() {
            return "1908:2: ( ( () ( (lv_assignment_1_0= ruleAssignmentRule ) ) (otherlv_2= ',' | otherlv_3= ';' ) ) | ( () otherlv_5= 'CREATE_OBJECT_SYMBOLS' ) | ( () otherlv_7= 'CONSTRUCTORS' ) | ( () otherlv_9= 'SORT_BY_NAME' otherlv_10= '(' otherlv_11= 'CONSTRUCTORS' otherlv_12= ')' ) | ( () ( (lv_size_14_0= ruleStatementDataSize ) ) otherlv_15= '(' ( (lv_data_16_0= ruleLExpression ) ) otherlv_17= ')' ) | ( () otherlv_19= 'FILL' otherlv_20= '(' ( (lv_fill_21_0= ruleLExpression ) ) otherlv_22= ')' ) | ( () otherlv_24= 'ASSERT' otherlv_25= '(' ( (lv_exp_26_0= ruleLExpression ) ) otherlv_27= ',' ( (lv_message_28_0= ruleValidID ) ) otherlv_29= ')' ) | ( () otherlv_31= 'INCLUDE' ( (lv_filename_32_0= ruleWildID ) ) ) | ( () ( (lv_spec_34_0= ruleInputSection ) ) ) | ( () otherlv_36= ';' ) )";
        }
    }
    static final String dfa_8s = "\71\uffff";
    static final String dfa_9s = "\2\uffff\7\14\60\uffff";
    static final String dfa_10s = "\1\4\1\15\7\4\1\uffff\1\15\1\4\1\uffff\1\4\7\16\10\15\1\uffff\3\4\1\uffff\16\16\2\4\7\16";
    static final String dfa_11s = "\1\140\1\15\7\140\1\uffff\1\15\1\140\1\uffff\1\140\7\111\1\15\7\16\1\uffff\3\140\1\uffff\16\111\2\140\7\111";
    static final String dfa_12s = "\11\uffff\1\2\2\uffff\1\1\20\uffff\1\4\3\uffff\1\3\27\uffff";
    static final String dfa_13s = "\71\uffff}>";
    static final String[] dfa_14s = {
            "\1\3\61\uffff\1\11\21\uffff\1\1\1\uffff\1\12\5\11\1\4\1\uffff\1\6\1\5\1\uffff\1\10\1\7\11\uffff\1\2",
            "\1\13",
            "\1\14\6\uffff\1\14\1\uffff\1\11\2\uffff\1\14\15\uffff\1\14\4\uffff\1\14\20\uffff\14\14\10\uffff\1\14\1\uffff\7\14\1\uffff\2\14\1\uffff\2\14\11\uffff\1\14",
            "\1\14\6\uffff\1\14\1\uffff\1\11\2\uffff\1\14\15\uffff\1\14\4\uffff\1\14\20\uffff\14\14\10\uffff\1\14\1\uffff\7\14\1\uffff\2\14\1\uffff\2\14\11\uffff\1\14",
            "\1\14\6\uffff\1\14\1\uffff\1\11\2\uffff\1\14\15\uffff\1\14\4\uffff\1\14\20\uffff\14\14\10\uffff\1\14\1\uffff\7\14\1\uffff\2\14\1\uffff\2\14\11\uffff\1\14",
            "\1\14\6\uffff\1\14\1\uffff\1\11\2\uffff\1\14\15\uffff\1\14\4\uffff\1\14\20\uffff\14\14\10\uffff\1\14\1\uffff\7\14\1\uffff\2\14\1\uffff\2\14\11\uffff\1\14",
            "\1\14\6\uffff\1\14\1\uffff\1\11\2\uffff\1\14\15\uffff\1\14\4\uffff\1\14\20\uffff\14\14\10\uffff\1\14\1\uffff\7\14\1\uffff\2\14\1\uffff\2\14\11\uffff\1\14",
            "\1\14\6\uffff\1\14\1\uffff\1\11\2\uffff\1\14\15\uffff\1\14\4\uffff\1\14\20\uffff\14\14\10\uffff\1\14\1\uffff\7\14\1\uffff\2\14\1\uffff\2\14\11\uffff\1\14",
            "\1\14\6\uffff\1\14\1\uffff\1\11\2\uffff\1\14\15\uffff\1\14\4\uffff\1\14\20\uffff\14\14\10\uffff\1\14\1\uffff\7\14\1\uffff\2\14\1\uffff\2\14\11\uffff\1\14",
            "",
            "\1\15",
            "\1\17\113\uffff\1\20\1\uffff\1\22\1\21\1\uffff\1\24\1\23\11\uffff\1\16",
            "",
            "\1\27\61\uffff\1\35\21\uffff\1\25\2\uffff\5\35\1\30\1\uffff\1\32\1\31\1\uffff\1\34\1\33\11\uffff\1\26",
            "\1\37\72\uffff\1\36",
            "\1\37\72\uffff\1\36",
            "\1\37\72\uffff\1\36",
            "\1\37\72\uffff\1\36",
            "\1\37\72\uffff\1\36",
            "\1\37\72\uffff\1\36",
            "\1\37\72\uffff\1\36",
            "\1\40",
            "\1\35\1\41",
            "\1\35\1\41",
            "\1\35\1\41",
            "\1\35\1\41",
            "\1\35\1\41",
            "\1\35\1\41",
            "\1\35\1\41",
            "",
            "\1\43\113\uffff\1\44\1\uffff\1\46\1\45\1\uffff\1\50\1\47\11\uffff\1\42",
            "\1\3\61\uffff\1\11\24\uffff\5\11\1\4\1\uffff\1\6\1\5\1\uffff\1\10\1\7\11\uffff\1\2",
            "\1\52\113\uffff\1\53\1\uffff\1\55\1\54\1\uffff\1\57\1\56\11\uffff\1\51",
            "",
            "\1\37\72\uffff\1\36",
            "\1\37\72\uffff\1\36",
            "\1\37\72\uffff\1\36",
            "\1\37\72\uffff\1\36",
            "\1\37\72\uffff\1\36",
            "\1\37\72\uffff\1\36",
            "\1\37\72\uffff\1\36",
            "\1\61\72\uffff\1\60",
            "\1\61\72\uffff\1\60",
            "\1\61\72\uffff\1\60",
            "\1\61\72\uffff\1\60",
            "\1\61\72\uffff\1\60",
            "\1\61\72\uffff\1\60",
            "\1\61\72\uffff\1\60",
            "\1\63\113\uffff\1\64\1\uffff\1\66\1\65\1\uffff\1\70\1\67\11\uffff\1\62",
            "\1\27\61\uffff\1\35\24\uffff\5\35\1\30\1\uffff\1\32\1\31\1\uffff\1\34\1\33\11\uffff\1\26",
            "\1\61\72\uffff\1\60",
            "\1\61\72\uffff\1\60",
            "\1\61\72\uffff\1\60",
            "\1\61\72\uffff\1\60",
            "\1\61\72\uffff\1\60",
            "\1\61\72\uffff\1\60",
            "\1\61\72\uffff\1\60"
    };

    static final short[] dfa_8 = DFA.unpackEncodedString(dfa_8s);
    static final short[] dfa_9 = DFA.unpackEncodedString(dfa_9s);
    static final char[] dfa_10 = DFA.unpackEncodedStringToUnsignedChars(dfa_10s);
    static final char[] dfa_11 = DFA.unpackEncodedStringToUnsignedChars(dfa_11s);
    static final short[] dfa_12 = DFA.unpackEncodedString(dfa_12s);
    static final short[] dfa_13 = DFA.unpackEncodedString(dfa_13s);
    static final short[][] dfa_14 = unpackEncodedStringArray(dfa_14s);

    class DFA53 extends DFA {

        public DFA53(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 53;
            this.eot = dfa_8;
            this.eof = dfa_9;
            this.min = dfa_10;
            this.max = dfa_11;
            this.accept = dfa_12;
            this.special = dfa_13;
            this.transition = dfa_14;
        }
        public String getDescription() {
            return "2880:2: ( ( () (otherlv_1= 'INPUT_SECTION_FLAGS' otherlv_2= '(' ( (lv_flags_3_0= ruleWildID ) ) (otherlv_4= '&' ( (lv_flags_5_0= ruleWildID ) ) )* otherlv_6= ')' )? ( (lv_file_7_0= ruleWildID ) ) ) | ( () (otherlv_9= 'INPUT_SECTION_FLAGS' otherlv_10= '(' ( (lv_flags_11_0= ruleWildID ) ) (otherlv_12= '&' ( (lv_flags_13_0= ruleWildID ) ) )* otherlv_14= ')' )? ( (lv_wildFile_15_0= ruleWildcardRule ) ) otherlv_16= '(' ( (lv_sections_17_0= ruleWildcardRule ) ) ( (otherlv_18= ',' )? ( (lv_sections_19_0= ruleWildcardRule ) ) )* otherlv_20= ')' ) | ( () ( (lv_keep_22_0= 'KEEP' ) ) otherlv_23= '(' (otherlv_24= 'INPUT_SECTION_FLAGS' otherlv_25= '(' ( (lv_flags_26_0= ruleWildID ) ) (otherlv_27= '&' ( (lv_flags_28_0= ruleWildID ) ) )* otherlv_29= ')' )? ( (lv_file_30_0= ruleWildID ) ) otherlv_31= ')' ) | ( () ( (lv_keep_33_0= 'KEEP' ) ) otherlv_34= '(' (otherlv_35= 'INPUT_SECTION_FLAGS' otherlv_36= '(' ( (lv_flags_37_0= ruleWildID ) ) (otherlv_38= '&' ( (lv_flags_39_0= ruleWildID ) ) )* otherlv_40= ')' )? ( (lv_wildFile_41_0= ruleWildcardRule ) ) otherlv_42= '(' ( (lv_sections_43_0= ruleWildcardRule ) ) ( (otherlv_44= ',' )? ( (lv_sections_45_0= ruleWildcardRule ) ) )* otherlv_46= ')' otherlv_47= ')' ) )";
        }
    }
    static final String dfa_15s = "\21\uffff";
    static final String dfa_16s = "\1\4\2\uffff\3\15\2\uffff\2\4\7\uffff";
    static final String dfa_17s = "\1\140\2\uffff\3\15\2\uffff\2\140\7\uffff";
    static final String dfa_18s = "\1\uffff\1\1\1\2\3\uffff\1\5\1\13\2\uffff\1\7\1\3\1\6\1\12\1\4\1\11\1\10";
    static final String dfa_19s = "\21\uffff}>";
    static final String[] dfa_20s = {
            "\1\1\61\uffff\1\4\24\uffff\1\2\1\5\1\6\1\7\1\3\1\1\1\uffff\2\1\1\uffff\2\1\11\uffff\1\1",
            "",
            "",
            "\1\10",
            "\1\10",
            "\1\11",
            "",
            "",
            "\1\13\61\uffff\1\12\24\uffff\1\15\1\14\2\uffff\1\12\1\13\1\uffff\2\13\1\uffff\2\13\11\uffff\1\13",
            "\1\16\61\uffff\1\20\25\uffff\1\17\2\uffff\1\20\1\16\1\uffff\2\16\1\uffff\2\16\11\uffff\1\16",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] dfa_15 = DFA.unpackEncodedString(dfa_15s);
    static final char[] dfa_16 = DFA.unpackEncodedStringToUnsignedChars(dfa_16s);
    static final char[] dfa_17 = DFA.unpackEncodedStringToUnsignedChars(dfa_17s);
    static final short[] dfa_18 = DFA.unpackEncodedString(dfa_18s);
    static final short[] dfa_19 = DFA.unpackEncodedString(dfa_19s);
    static final short[][] dfa_20 = unpackEncodedStringArray(dfa_20s);

    class DFA56 extends DFA {

        public DFA56(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 56;
            this.eot = dfa_15;
            this.eof = dfa_15;
            this.min = dfa_16;
            this.max = dfa_17;
            this.accept = dfa_18;
            this.special = dfa_19;
            this.transition = dfa_20;
        }
        public String getDescription() {
            return "3398:2: ( ( () ( (lv_name_1_0= ruleWildID ) ) ) | ( () otherlv_3= 'EXCLUDE_FILE' otherlv_4= '(' ( (lv_excludes_5_0= ruleWildID ) )+ otherlv_6= ')' ( (lv_name_7_0= ruleWildID ) ) ) | ( () ruleSORT_BY_NAME otherlv_10= '(' ( (lv_name_11_0= ruleWildID ) ) otherlv_12= ')' ) | ( () otherlv_14= 'SORT_BY_ALIGNMENT' otherlv_15= '(' ( (lv_name_16_0= ruleWildID ) ) otherlv_17= ')' ) | ( () otherlv_19= 'SORT_NONE' otherlv_20= '(' ( (lv_name_21_0= ruleWildID ) ) otherlv_22= ')' ) | ( () ruleSORT_BY_NAME otherlv_25= '(' otherlv_26= 'SORT_BY_ALIGNMENT' otherlv_27= '(' ( (lv_name_28_0= ruleWildID ) ) otherlv_29= ')' otherlv_30= ')' ) | ( () ruleSORT_BY_NAME otherlv_33= '(' ruleSORT_BY_NAME otherlv_35= '(' ( (lv_name_36_0= ruleWildID ) ) otherlv_37= ')' otherlv_38= ')' ) | ( () otherlv_40= 'SORT_BY_ALIGNMENT' otherlv_41= '(' ruleSORT_BY_NAME otherlv_43= '(' ( (lv_name_44_0= ruleWildID ) ) otherlv_45= ')' otherlv_46= ')' ) | ( () otherlv_48= 'SORT_BY_ALIGNMENT' otherlv_49= '(' otherlv_50= 'SORT_BY_ALIGNMENT' otherlv_51= '(' ( (lv_name_52_0= ruleWildID ) ) otherlv_53= ')' otherlv_54= ')' ) | ( () ruleSORT_BY_NAME otherlv_57= '(' otherlv_58= 'EXCLUDE_FILE' otherlv_59= '(' ( (lv_excludes_60_0= ruleWildID ) )+ otherlv_61= ')' ( (lv_name_62_0= ruleWildID ) ) otherlv_63= ')' ) | ( () otherlv_65= 'SORT_BY_INIT_PRIORITY' otherlv_66= '(' ( (lv_name_67_0= ruleWildID ) ) otherlv_68= ')' ) )";
        }
    }
    static final String dfa_21s = "\51\uffff";
    static final String dfa_22s = "\1\1\50\uffff";
    static final String dfa_23s = "\1\4\15\uffff\2\0\31\uffff";
    static final String dfa_24s = "\1\146\15\uffff\2\0\31\uffff";
    static final String dfa_25s = "\1\uffff\1\2\46\uffff\1\1";
    static final String dfa_26s = "\16\uffff\1\0\1\1\31\uffff}>";
    static final String[] dfa_27s = {
            "\3\1\3\uffff\2\1\1\uffff\4\1\22\uffff\1\1\1\uffff\2\1\1\uffff\1\1\1\uffff\1\1\22\uffff\3\1\4\uffff\2\1\3\uffff\1\1\6\uffff\1\1\1\uffff\14\1\1\17\1\16\1\1\2\uffff\1\1\2\uffff\1\1",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\uffff",
            "\1\uffff",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] dfa_21 = DFA.unpackEncodedString(dfa_21s);
    static final short[] dfa_22 = DFA.unpackEncodedString(dfa_22s);
    static final char[] dfa_23 = DFA.unpackEncodedStringToUnsignedChars(dfa_23s);
    static final char[] dfa_24 = DFA.unpackEncodedStringToUnsignedChars(dfa_24s);
    static final short[] dfa_25 = DFA.unpackEncodedString(dfa_25s);
    static final short[] dfa_26 = DFA.unpackEncodedString(dfa_26s);
    static final short[][] dfa_27 = unpackEncodedStringArray(dfa_27s);

    class DFA75 extends DFA {

        public DFA75(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 75;
            this.eot = dfa_21;
            this.eof = dfa_22;
            this.min = dfa_23;
            this.max = dfa_24;
            this.accept = dfa_25;
            this.special = dfa_26;
            this.transition = dfa_27;
        }
        public String getDescription() {
            return "()* loopback of 5241:3: ( ( ( ( () ( ( ruleOpAdd ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpAdd ) ) ) ) ( (lv_rightOperand_3_0= ruleLMultiplicativeExpression ) ) )*";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA75_14 = input.LA(1);

                         
                        int index75_14 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred11_InternalLinkerScript()) ) {s = 40;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index75_14);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA75_15 = input.LA(1);

                         
                        int index75_15 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred11_InternalLinkerScript()) ) {s = 40;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index75_15);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 75, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String dfa_28s = "\26\uffff";
    static final String dfa_29s = "\1\1\25\uffff";
    static final String dfa_30s = "\1\4\1\uffff\1\4\23\uffff";
    static final String dfa_31s = "\1\146\1\uffff\1\146\23\uffff";
    static final String dfa_32s = "\1\uffff\1\2\1\uffff\23\1";
    static final String dfa_33s = "\1\1\1\uffff\1\0\23\uffff}>";
    static final String[] dfa_34s = {
            "\3\1\3\uffff\2\1\1\uffff\4\1\22\uffff\1\1\1\uffff\2\1\1\uffff\1\1\1\uffff\1\1\22\uffff\3\1\4\uffff\2\1\3\uffff\1\1\6\uffff\1\1\1\uffff\16\1\1\2\1\3\1\4\1\1\2\uffff\1\1",
            "",
            "\1\20\1\15\1\16\6\uffff\1\17\30\uffff\1\14\1\uffff\2\1\1\12\25\uffff\5\1\1\uffff\2\1\10\uffff\1\21\1\uffff\1\23\1\22\1\11\1\25\1\24\1\5\6\uffff\1\7\1\6\3\uffff\1\10\2\uffff\1\13",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] dfa_28 = DFA.unpackEncodedString(dfa_28s);
    static final short[] dfa_29 = DFA.unpackEncodedString(dfa_29s);
    static final char[] dfa_30 = DFA.unpackEncodedStringToUnsignedChars(dfa_30s);
    static final char[] dfa_31 = DFA.unpackEncodedStringToUnsignedChars(dfa_31s);
    static final short[] dfa_32 = DFA.unpackEncodedString(dfa_32s);
    static final short[] dfa_33 = DFA.unpackEncodedString(dfa_33s);
    static final short[][] dfa_34 = unpackEncodedStringArray(dfa_34s);

    class DFA77 extends DFA {

        public DFA77(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 77;
            this.eot = dfa_28;
            this.eof = dfa_29;
            this.min = dfa_30;
            this.max = dfa_31;
            this.accept = dfa_32;
            this.special = dfa_33;
            this.transition = dfa_34;
        }
        public String getDescription() {
            return "()* loopback of 5359:3: ( ( ( ( () ( ( ruleOpMulti ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpMulti ) ) ) ) ( (lv_rightOperand_3_0= ruleLUnaryOperation ) ) )*";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA77_2 = input.LA(1);

                         
                        int index77_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA77_2==87) && (synpred12_InternalLinkerScript())) {s = 5;}

                        else if ( (LA77_2==95) && (synpred12_InternalLinkerScript())) {s = 6;}

                        else if ( (LA77_2==94) && (synpred12_InternalLinkerScript())) {s = 7;}

                        else if ( (LA77_2==99) && (synpred12_InternalLinkerScript())) {s = 8;}

                        else if ( (LA77_2==84) && (synpred12_InternalLinkerScript())) {s = 9;}

                        else if ( (LA77_2==42) && (synpred12_InternalLinkerScript())) {s = 10;}

                        else if ( (LA77_2==102) && (synpred12_InternalLinkerScript())) {s = 11;}

                        else if ( (LA77_2==38) && (synpred12_InternalLinkerScript())) {s = 12;}

                        else if ( (LA77_2==RULE_DEC) && (synpred12_InternalLinkerScript())) {s = 13;}

                        else if ( (LA77_2==RULE_HEX) && (synpred12_InternalLinkerScript())) {s = 14;}

                        else if ( (LA77_2==13) && (synpred12_InternalLinkerScript())) {s = 15;}

                        else if ( (LA77_2==RULE_ID) && (synpred12_InternalLinkerScript())) {s = 16;}

                        else if ( (LA77_2==80) && (synpred12_InternalLinkerScript())) {s = 17;}

                        else if ( (LA77_2==83) && (synpred12_InternalLinkerScript())) {s = 18;}

                        else if ( (LA77_2==82) && (synpred12_InternalLinkerScript())) {s = 19;}

                        else if ( (LA77_2==86) && (synpred12_InternalLinkerScript())) {s = 20;}

                        else if ( (LA77_2==85) && (synpred12_InternalLinkerScript())) {s = 21;}

                        else if ( ((LA77_2>=40 && LA77_2<=41)||(LA77_2>=64 && LA77_2<=68)||(LA77_2>=70 && LA77_2<=71)) ) {s = 1;}

                         
                        input.seek(index77_2);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA77_0 = input.LA(1);

                         
                        int index77_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA77_0==EOF||(LA77_0>=RULE_ID && LA77_0<=RULE_HEX)||(LA77_0>=10 && LA77_0<=11)||(LA77_0>=13 && LA77_0<=16)||LA77_0==35||(LA77_0>=37 && LA77_0<=38)||LA77_0==40||LA77_0==42||(LA77_0>=61 && LA77_0<=63)||(LA77_0>=68 && LA77_0<=69)||LA77_0==73||LA77_0==80||(LA77_0>=82 && LA77_0<=95)||LA77_0==99||LA77_0==102) ) {s = 1;}

                        else if ( (LA77_0==96) ) {s = 2;}

                        else if ( (LA77_0==97) && (synpred12_InternalLinkerScript())) {s = 3;}

                        else if ( (LA77_0==98) && (synpred12_InternalLinkerScript())) {s = 4;}

                         
                        input.seek(index77_0);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 77, _s, input);
            error(nvae);
            throw nvae;
        }
    }
 

    public static final BitSet FOLLOW_1 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_2 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_3 = new BitSet(new long[]{0xE00000127FFF9812L,0x00000001006D0000L});
    public static final BitSet FOLLOW_4 = new BitSet(new long[]{0x0000000000000C00L});
    public static final BitSet FOLLOW_5 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_6 = new BitSet(new long[]{0x0000000000000010L,0x00000001006D0000L});
    public static final BitSet FOLLOW_7 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_8 = new BitSet(new long[]{0x0000044000002070L,0x00000049C0FD0000L});
    public static final BitSet FOLLOW_9 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_10 = new BitSet(new long[]{0x0000000000004400L});
    public static final BitSet FOLLOW_11 = new BitSet(new long[]{0x0000000180000010L,0x00000001006D0000L});
    public static final BitSet FOLLOW_12 = new BitSet(new long[]{0x0000000100000010L,0x00000001006D0000L});
    public static final BitSet FOLLOW_13 = new BitSet(new long[]{0x0000000100004410L,0x00000001006D0000L});
    public static final BitSet FOLLOW_14 = new BitSet(new long[]{0x0000000000004010L,0x00000001006D0000L});
    public static final BitSet FOLLOW_15 = new BitSet(new long[]{0x0000000100000412L,0x00000001006D0000L});
    public static final BitSet FOLLOW_16 = new BitSet(new long[]{0x0000000400000000L});
    public static final BitSet FOLLOW_17 = new BitSet(new long[]{0x0000000800000010L,0x00000001006D0000L});
    public static final BitSet FOLLOW_18 = new BitSet(new long[]{0x0000044000002870L,0x00000049C0FD0000L});
    public static final BitSet FOLLOW_19 = new BitSet(new long[]{0xE000000800018810L,0x00000001006D0000L});
    public static final BitSet FOLLOW_20 = new BitSet(new long[]{0x0000046000002070L,0x00000049C0FD0000L});
    public static final BitSet FOLLOW_21 = new BitSet(new long[]{0x0000002000002000L});
    public static final BitSet FOLLOW_22 = new BitSet(new long[]{0x000F800000000000L});
    public static final BitSet FOLLOW_23 = new BitSet(new long[]{0x0000002000000000L});
    public static final BitSet FOLLOW_24 = new BitSet(new long[]{0x00007CC400000000L});
    public static final BitSet FOLLOW_25 = new BitSet(new long[]{0x00007C8400000000L});
    public static final BitSet FOLLOW_26 = new BitSet(new long[]{0x0000708400000000L});
    public static final BitSet FOLLOW_27 = new BitSet(new long[]{0x0000700400000000L});
    public static final BitSet FOLLOW_28 = new BitSet(new long[]{0xFFF0000840010810L,0x00000001006DFD00L});
    public static final BitSet FOLLOW_29 = new BitSet(new long[]{0x0000036000000402L});
    public static final BitSet FOLLOW_30 = new BitSet(new long[]{0x0000026000000402L});
    public static final BitSet FOLLOW_31 = new BitSet(new long[]{0x0000010000000000L});
    public static final BitSet FOLLOW_32 = new BitSet(new long[]{0x0000022000000402L});
    public static final BitSet FOLLOW_33 = new BitSet(new long[]{0x0000000000000402L});
    public static final BitSet FOLLOW_34 = new BitSet(new long[]{0x0020000000000000L});
    public static final BitSet FOLLOW_35 = new BitSet(new long[]{0x0000030000000000L,0x00000000000000DFL});
    public static final BitSet FOLLOW_36 = new BitSet(new long[]{0x0000020000000000L});
    public static final BitSet FOLLOW_37 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_38 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000020L});
    public static final BitSet FOLLOW_39 = new BitSet(new long[]{0x0000000000004000L,0x0000000000000200L});
    public static final BitSet FOLLOW_40 = new BitSet(new long[]{0x0040000000000010L,0x00000001006DF900L});
    public static final BitSet FOLLOW_41 = new BitSet(new long[]{0x0040000000004410L,0x00000001006DF900L});
    public static final BitSet FOLLOW_42 = new BitSet(new long[]{0x0000000000000010L,0x00000001006D0100L});
    public static final BitSet FOLLOW_43 = new BitSet(new long[]{0x0000000000000000L,0x0000000000001000L});
    public static final BitSet FOLLOW_44 = new BitSet(new long[]{0x0040000000000000L,0x0000000000008000L});
    public static final BitSet FOLLOW_45 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000800L});
    public static final BitSet FOLLOW_46 = new BitSet(new long[]{0x0000000000000000L,0x00000000000E0000L});
    public static final BitSet FOLLOW_47 = new BitSet(new long[]{0x0000000000000000L,0x0000000000700000L});
    public static final BitSet FOLLOW_48 = new BitSet(new long[]{0x0000000000000010L,0x0000000100ED0000L});
    public static final BitSet FOLLOW_49 = new BitSet(new long[]{0x0000000000004010L,0x0000000100ED0000L});
    public static final BitSet FOLLOW_50 = new BitSet(new long[]{0x0000000000000002L,0x0000000001000000L});
    public static final BitSet FOLLOW_51 = new BitSet(new long[]{0x0000000000000002L,0x0000000002000000L});
    public static final BitSet FOLLOW_52 = new BitSet(new long[]{0x0000000000000002L,0x0000000004000000L});
    public static final BitSet FOLLOW_53 = new BitSet(new long[]{0x0000000000000002L,0x0000000008000000L});
    public static final BitSet FOLLOW_54 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000200L});
    public static final BitSet FOLLOW_55 = new BitSet(new long[]{0x0000000000000002L,0x0000000030000000L});
    public static final BitSet FOLLOW_56 = new BitSet(new long[]{0x0000010000000002L,0x0000000000000030L});
    public static final BitSet FOLLOW_57 = new BitSet(new long[]{0x0000010000000002L,0x0000000000000010L});
    public static final BitSet FOLLOW_58 = new BitSet(new long[]{0x0000000000000002L,0x00000000C0000000L});
    public static final BitSet FOLLOW_59 = new BitSet(new long[]{0x0000000000000002L,0x0000000700000000L});
    public static final BitSet FOLLOW_60 = new BitSet(new long[]{0x0000000000000002L,0x0000003000000000L});

}