// Generated from Neon.g4 by ANTLR 4.0

package com.ncc.neon.language.parse;

import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class NeonLexer extends Lexer {
	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__12=1, T__11=2, T__10=3, T__9=4, T__8=5, T__7=6, T__6=7, T__5=8, T__4=9, 
		T__3=10, T__2=11, T__1=12, T__0=13, ALL_FIELDS=14, AND=15, OR=16, GT=17, 
		GTE=18, LT=19, LTE=20, EQ=21, NE=22, USE=23, SELECT=24, FROM=25, WHERE=26, 
		GROUP=27, LIMIT=28, SORT=29, SORT_DIRECTION=30, STRING=31, WHITESPACE=32;
	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] tokenNames = {
		"<INVALID>",
		"'max'", "'avg'", "'last'", "'sum'", "')'", "','", "'min'", "'push'", 
		"'('", "'addToSet'", "'count'", "';'", "'first'", "'*'", "AND", "OR", 
		"'>'", "'>='", "'<'", "'<='", "'='", "'!='", "USE", "SELECT", "FROM", 
		"WHERE", "GROUP", "LIMIT", "SORT", "SORT_DIRECTION", "STRING", "WHITESPACE"
	};
	public static final String[] ruleNames = {
		"T__12", "T__11", "T__10", "T__9", "T__8", "T__7", "T__6", "T__5", "T__4", 
		"T__3", "T__2", "T__1", "T__0", "ALL_FIELDS", "AND", "OR", "GT", "GTE", 
		"LT", "LTE", "EQ", "NE", "USE", "SELECT", "FROM", "WHERE", "GROUP", "LIMIT", 
		"SORT", "SORT_DIRECTION", "STRING", "CHAR", "WHITESPACE"
	};


	public NeonLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "Neon.g4"; }

	@Override
	public String[] getTokenNames() { return tokenNames; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	@Override
	public void action(RuleContext _localctx, int ruleIndex, int actionIndex) {
		switch (ruleIndex) {
		case 32: WHITESPACE_action((RuleContext)_localctx, actionIndex); break;
		}
	}
	private void WHITESPACE_action(RuleContext _localctx, int actionIndex) {
		switch (actionIndex) {
		case 0: skip();  break;
		}
	}

	public static final String _serializedATN =
		"\2\4\"\u0107\b\1\4\2\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t"+
		"\b\4\t\t\t\4\n\t\n\4\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20"+
		"\t\20\4\21\t\21\4\22\t\22\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27"+
		"\t\27\4\30\t\30\4\31\t\31\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36"+
		"\t\36\4\37\t\37\4 \t \4!\t!\4\"\t\"\3\2\3\2\3\2\3\2\3\3\3\3\3\3\3\3\3"+
		"\4\3\4\3\4\3\4\3\4\3\5\3\5\3\5\3\5\3\6\3\6\3\7\3\7\3\b\3\b\3\b\3\b\3\t"+
		"\3\t\3\t\3\t\3\t\3\n\3\n\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13"+
		"\3\f\3\f\3\f\3\f\3\f\3\f\3\r\3\r\3\16\3\16\3\16\3\16\3\16\3\16\3\17\3"+
		"\17\3\20\3\20\3\20\3\20\3\20\3\20\5\20\u0085\n\20\3\21\3\21\3\21\3\21"+
		"\5\21\u008b\n\21\3\22\3\22\3\23\3\23\3\23\3\24\3\24\3\25\3\25\3\25\3\26"+
		"\3\26\3\27\3\27\3\27\3\30\3\30\3\30\3\30\3\30\3\30\5\30\u00a2\n\30\3\31"+
		"\3\31\3\31\3\31\3\31\3\31\3\31\3\31\3\31\3\31\3\31\3\31\5\31\u00b0\n\31"+
		"\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\5\32\u00ba\n\32\3\33\3\33\3\33"+
		"\3\33\3\33\3\33\3\33\3\33\3\33\3\33\5\33\u00c6\n\33\3\34\3\34\3\34\3\34"+
		"\3\34\3\34\3\34\3\34\3\34\3\34\5\34\u00d2\n\34\3\35\3\35\3\35\3\35\3\35"+
		"\3\35\3\35\3\35\3\35\3\35\5\35\u00de\n\35\3\36\3\36\3\36\3\36\3\36\3\36"+
		"\3\36\3\36\5\36\u00e8\n\36\3\37\3\37\3\37\3\37\3\37\3\37\3\37\3\37\3\37"+
		"\3\37\3\37\3\37\3\37\3\37\5\37\u00f8\n\37\3 \6 \u00fb\n \r \16 \u00fc"+
		"\3!\3!\3\"\6\"\u0102\n\"\r\"\16\"\u0103\3\"\3\"\2#\3\3\1\5\4\1\7\5\1\t"+
		"\6\1\13\7\1\r\b\1\17\t\1\21\n\1\23\13\1\25\f\1\27\r\1\31\16\1\33\17\1"+
		"\35\20\1\37\21\1!\22\1#\23\1%\24\1\'\25\1)\26\1+\27\1-\30\1/\31\1\61\32"+
		"\1\63\33\1\65\34\1\67\35\19\36\1;\37\1= \1?!\1A\2\1C\"\2\3\2\4\7/\60\62"+
		";C\\aac|\5\13\f\17\17\"\"\u0113\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2"+
		"\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2"+
		"\2\2\2\25\3\2\2\2\2\27\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2\2\2\35\3\2\2\2"+
		"\2\37\3\2\2\2\2!\3\2\2\2\2#\3\2\2\2\2%\3\2\2\2\2\'\3\2\2\2\2)\3\2\2\2"+
		"\2+\3\2\2\2\2-\3\2\2\2\2/\3\2\2\2\2\61\3\2\2\2\2\63\3\2\2\2\2\65\3\2\2"+
		"\2\2\67\3\2\2\2\29\3\2\2\2\2;\3\2\2\2\2=\3\2\2\2\2?\3\2\2\2\2C\3\2\2\2"+
		"\3E\3\2\2\2\5I\3\2\2\2\7M\3\2\2\2\tR\3\2\2\2\13V\3\2\2\2\rX\3\2\2\2\17"+
		"Z\3\2\2\2\21^\3\2\2\2\23c\3\2\2\2\25e\3\2\2\2\27n\3\2\2\2\31t\3\2\2\2"+
		"\33v\3\2\2\2\35|\3\2\2\2\37\u0084\3\2\2\2!\u008a\3\2\2\2#\u008c\3\2\2"+
		"\2%\u008e\3\2\2\2\'\u0091\3\2\2\2)\u0093\3\2\2\2+\u0096\3\2\2\2-\u0098"+
		"\3\2\2\2/\u00a1\3\2\2\2\61\u00af\3\2\2\2\63\u00b9\3\2\2\2\65\u00c5\3\2"+
		"\2\2\67\u00d1\3\2\2\29\u00dd\3\2\2\2;\u00e7\3\2\2\2=\u00f7\3\2\2\2?\u00fa"+
		"\3\2\2\2A\u00fe\3\2\2\2C\u0101\3\2\2\2EF\7o\2\2FG\7c\2\2GH\7z\2\2H\4\3"+
		"\2\2\2IJ\7c\2\2JK\7x\2\2KL\7i\2\2L\6\3\2\2\2MN\7n\2\2NO\7c\2\2OP\7u\2"+
		"\2PQ\7v\2\2Q\b\3\2\2\2RS\7u\2\2ST\7w\2\2TU\7o\2\2U\n\3\2\2\2VW\7+\2\2"+
		"W\f\3\2\2\2XY\7.\2\2Y\16\3\2\2\2Z[\7o\2\2[\\\7k\2\2\\]\7p\2\2]\20\3\2"+
		"\2\2^_\7r\2\2_`\7w\2\2`a\7u\2\2ab\7j\2\2b\22\3\2\2\2cd\7*\2\2d\24\3\2"+
		"\2\2ef\7c\2\2fg\7f\2\2gh\7f\2\2hi\7V\2\2ij\7q\2\2jk\7U\2\2kl\7g\2\2lm"+
		"\7v\2\2m\26\3\2\2\2no\7e\2\2op\7q\2\2pq\7w\2\2qr\7p\2\2rs\7v\2\2s\30\3"+
		"\2\2\2tu\7=\2\2u\32\3\2\2\2vw\7h\2\2wx\7k\2\2xy\7t\2\2yz\7u\2\2z{\7v\2"+
		"\2{\34\3\2\2\2|}\7,\2\2}\36\3\2\2\2~\177\7C\2\2\177\u0080\7P\2\2\u0080"+
		"\u0085\7F\2\2\u0081\u0082\7c\2\2\u0082\u0083\7p\2\2\u0083\u0085\7f\2\2"+
		"\u0084~\3\2\2\2\u0084\u0081\3\2\2\2\u0085 \3\2\2\2\u0086\u0087\7Q\2\2"+
		"\u0087\u008b\7T\2\2\u0088\u0089\7q\2\2\u0089\u008b\7t\2\2\u008a\u0086"+
		"\3\2\2\2\u008a\u0088\3\2\2\2\u008b\"\3\2\2\2\u008c\u008d\7@\2\2\u008d"+
		"$\3\2\2\2\u008e\u008f\7@\2\2\u008f\u0090\7?\2\2\u0090&\3\2\2\2\u0091\u0092"+
		"\7>\2\2\u0092(\3\2\2\2\u0093\u0094\7>\2\2\u0094\u0095\7?\2\2\u0095*\3"+
		"\2\2\2\u0096\u0097\7?\2\2\u0097,\3\2\2\2\u0098\u0099\7#\2\2\u0099\u009a"+
		"\7?\2\2\u009a.\3\2\2\2\u009b\u009c\7W\2\2\u009c\u009d\7U\2\2\u009d\u00a2"+
		"\7G\2\2\u009e\u009f\7w\2\2\u009f\u00a0\7u\2\2\u00a0\u00a2\7g\2\2\u00a1"+
		"\u009b\3\2\2\2\u00a1\u009e\3\2\2\2\u00a2\60\3\2\2\2\u00a3\u00a4\7U\2\2"+
		"\u00a4\u00a5\7G\2\2\u00a5\u00a6\7N\2\2\u00a6\u00a7\7G\2\2\u00a7\u00a8"+
		"\7E\2\2\u00a8\u00b0\7V\2\2\u00a9\u00aa\7u\2\2\u00aa\u00ab\7g\2\2\u00ab"+
		"\u00ac\7n\2\2\u00ac\u00ad\7g\2\2\u00ad\u00ae\7e\2\2\u00ae\u00b0\7v\2\2"+
		"\u00af\u00a3\3\2\2\2\u00af\u00a9\3\2\2\2\u00b0\62\3\2\2\2\u00b1\u00b2"+
		"\7H\2\2\u00b2\u00b3\7T\2\2\u00b3\u00b4\7Q\2\2\u00b4\u00ba\7O\2\2\u00b5"+
		"\u00b6\7h\2\2\u00b6\u00b7\7t\2\2\u00b7\u00b8\7q\2\2\u00b8\u00ba\7o\2\2"+
		"\u00b9\u00b1\3\2\2\2\u00b9\u00b5\3\2\2\2\u00ba\64\3\2\2\2\u00bb\u00bc"+
		"\7Y\2\2\u00bc\u00bd\7J\2\2\u00bd\u00be\7G\2\2\u00be\u00bf\7T\2\2\u00bf"+
		"\u00c6\7G\2\2\u00c0\u00c1\7y\2\2\u00c1\u00c2\7j\2\2\u00c2\u00c3\7g\2\2"+
		"\u00c3\u00c4\7t\2\2\u00c4\u00c6\7g\2\2\u00c5\u00bb\3\2\2\2\u00c5\u00c0"+
		"\3\2\2\2\u00c6\66\3\2\2\2\u00c7\u00c8\7I\2\2\u00c8\u00c9\7T\2\2\u00c9"+
		"\u00ca\7Q\2\2\u00ca\u00cb\7W\2\2\u00cb\u00d2\7R\2\2\u00cc\u00cd\7i\2\2"+
		"\u00cd\u00ce\7t\2\2\u00ce\u00cf\7q\2\2\u00cf\u00d0\7w\2\2\u00d0\u00d2"+
		"\7r\2\2\u00d1\u00c7\3\2\2\2\u00d1\u00cc\3\2\2\2\u00d28\3\2\2\2\u00d3\u00d4"+
		"\7N\2\2\u00d4\u00d5\7K\2\2\u00d5\u00d6\7O\2\2\u00d6\u00d7\7K\2\2\u00d7"+
		"\u00de\7V\2\2\u00d8\u00d9\7n\2\2\u00d9\u00da\7k\2\2\u00da\u00db\7o\2\2"+
		"\u00db\u00dc\7k\2\2\u00dc\u00de\7v\2\2\u00dd\u00d3\3\2\2\2\u00dd\u00d8"+
		"\3\2\2\2\u00de:\3\2\2\2\u00df\u00e0\7U\2\2\u00e0\u00e1\7Q\2\2\u00e1\u00e2"+
		"\7T\2\2\u00e2\u00e8\7V\2\2\u00e3\u00e4\7u\2\2\u00e4\u00e5\7q\2\2\u00e5"+
		"\u00e6\7t\2\2\u00e6\u00e8\7v\2\2\u00e7\u00df\3\2\2\2\u00e7\u00e3\3\2\2"+
		"\2\u00e8<\3\2\2\2\u00e9\u00ea\7C\2\2\u00ea\u00eb\7U\2\2\u00eb\u00f8\7"+
		"E\2\2\u00ec\u00ed\7c\2\2\u00ed\u00ee\7u\2\2\u00ee\u00f8\7e\2\2\u00ef\u00f0"+
		"\7F\2\2\u00f0\u00f1\7G\2\2\u00f1\u00f2\7U\2\2\u00f2\u00f8\7E\2\2\u00f3"+
		"\u00f4\7f\2\2\u00f4\u00f5\7g\2\2\u00f5\u00f6\7u\2\2\u00f6\u00f8\7e\2\2"+
		"\u00f7\u00e9\3\2\2\2\u00f7\u00ec\3\2\2\2\u00f7\u00ef\3\2\2\2\u00f7\u00f3"+
		"\3\2\2\2\u00f8>\3\2\2\2\u00f9\u00fb\5A!\2\u00fa\u00f9\3\2\2\2\u00fb\u00fc"+
		"\3\2\2\2\u00fc\u00fa\3\2\2\2\u00fc\u00fd\3\2\2\2\u00fd@\3\2\2\2\u00fe"+
		"\u00ff\t\2\2\2\u00ffB\3\2\2\2\u0100\u0102\t\3\2\2\u0101\u0100\3\2\2\2"+
		"\u0102\u0103\3\2\2\2\u0103\u0101\3\2\2\2\u0103\u0104\3\2\2\2\u0104\u0105"+
		"\3\2\2\2\u0105\u0106\b\"\2\2\u0106D\3\2\2\2\17\2\u0084\u008a\u00a1\u00af"+
		"\u00b9\u00c5\u00d1\u00dd\u00e7\u00f7\u00fc\u0103";
	public static final ATN _ATN =
		ATNSimulator.deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
	}
}