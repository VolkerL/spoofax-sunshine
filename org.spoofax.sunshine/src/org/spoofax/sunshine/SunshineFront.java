/**
 * 
 */
package org.spoofax.sunshine;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.sunshine.framework.language.AdHocJarBasedLanguage;
import org.spoofax.sunshine.framework.messages.IMessage;
import org.spoofax.sunshine.framework.services.AnalysisService;
import org.spoofax.sunshine.framework.services.LanguageService;
import org.spoofax.sunshine.framework.services.MessageService;

/**
 * @author Vlad Vergu
 * 
 */
public class SunshineFront {

	private final static String LANG_JAR = "--lang-jar";
	private final static String LANG_TBL = "--lang-tbl";
	private final static String TRG_FILE = "--targets";

	private String language_jar;
	private String language_tbl;
	private final List<String> file_targets = new LinkedList<String>();

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		final SunshineFront front = new SunshineFront();
		front.parseArgs(args);
		front.initializeLanguage();
		front.analyzeFiles();
	}

	private void analyzeFiles() {
		for (String trg : file_targets) {
			analyzeFile(trg);
		}
	}

	private void analyzeFile(String filename) {
		final IStrategoTerm ast = AnalysisService.INSTANCE().analyze(new File(filename));
		if (ast == null) {
			System.out.println("No AST produced");
		} else {
			System.out.println(ast);
		}
		Collection<IMessage> msgs = MessageService.INSTANCE().getMessages();
		for (IMessage msg : msgs) {
			System.err.println(msg);
		}
	}

	private void initializeLanguage() {
		final AdHocJarBasedLanguage lang = new AdHocJarBasedLanguage("Entity", new String[] { ".ent" }, "Start", new File(language_tbl), "editor-analyze", new File(language_jar));
		LanguageService.INSTANCE().registerLanguage(lang);
	}

	private void parseArgs(String[] args) throws IllegalArgumentException {
		boolean lang_jar_next = false, lang_tbl_next = false, trg_file_next = false;

		String lang_jar = null;
		String lang_tbl = null;
		List<String> targets = new LinkedList<String>();

		for (String a : args) {
			if (a.equals(LANG_JAR)) {
				lang_jar_next = true;
				lang_tbl_next = false;
				trg_file_next = false;
			} else if (a.equals(LANG_TBL)) {
				lang_jar_next = false;
				lang_tbl_next = true;
				trg_file_next = false;
			} else if (a.equals(TRG_FILE)) {
				lang_jar_next = false;
				lang_tbl_next = false;
				trg_file_next = true;
			} else {
				if (lang_jar_next) {
					lang_jar = a;
				} else if (lang_tbl_next) {
					lang_tbl = a;
				} else if (trg_file_next) {
					targets.add(a);
				}
			}
		}
		if (lang_jar == null) {
			throw new IllegalArgumentException("Missing --lang-jar argument");
		} else if (lang_tbl == null) {
			throw new IllegalArgumentException("Missing --lang-tbl argument");
		} else if (targets.size() == 0) {
			throw new IllegalArgumentException("Missing target files");
		}

		this.language_jar = lang_jar;
		this.language_tbl = lang_tbl;
		this.file_targets.addAll(targets);

	}

	/*
	 * General:
	 * 
	 * PretenderFront [LANGUAGE-OPTS] [FILE_TO_RUN_AGAINST]
	 * 
	 * Language argument: --lang-jar [foobar.jar] --lang-tbl [foobar.tbl] ... --lang-esv
	 * [foobar.main.packed.esv]
	 */

}