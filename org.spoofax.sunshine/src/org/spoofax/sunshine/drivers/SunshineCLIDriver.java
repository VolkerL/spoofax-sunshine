/**
 * 
 */
package org.spoofax.sunshine.drivers;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import org.spoofax.sunshine.Environment;
import org.spoofax.sunshine.framework.language.AdHocJarBasedLanguage;
import org.spoofax.sunshine.framework.messages.IMessage;
import org.spoofax.sunshine.framework.services.BuilderService;
import org.spoofax.sunshine.framework.services.FileMonitoringService;
import org.spoofax.sunshine.framework.services.LanguageService;
import org.spoofax.sunshine.framework.services.MessageService;
import org.spoofax.sunshine.framework.services.ParseService;
import org.spoofax.sunshine.framework.services.QueableAnalysisService;

/**
 * @author Vlad Vergu
 * 
 */
public class SunshineCLIDriver {

	private final static String DBG_WARM = "--warmup";
	private final static String LANG_JAR = "--lang-jar";
	private final static String LANG_TBL = "--lang-tbl";
	private final static String LANG_SSYMB = "--start-symbol";
	private final static String PROJ_DIR = "--proj-dir";
	private final static String PARSE_ONLY = "--pao";
	private final static String EXT_ENS = "--extens";
	private final static String LANG_NAME = "--lang-name";
	private final static String MOD_DAEMON = "--daemon";
	private final static String CALL_BUILDER = "--builder";
	private final static String BUILD_ON = "--build-on";

	private static final String observer_fun = "editor-analyze";

	private final List<String> extens = new LinkedList<String>();
	private final List<String> language_jars = new LinkedList<String>();;
	private String language_tbl;
	private String language_startsymb;
	private String langname;
	private String project_dir;
	private String builderName;
	private String buildOnTarget;
	private boolean parse_only;

	private boolean daemon;
	private int warmups;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		final SunshineCLIDriver front = new SunshineCLIDriver();
		front.parseArgs(args);
		front.initialize();
		front.warmup();
		front.work();
		System.exit(0);
	}

	private void warmup() {
		System.out.println("Warming up " + warmups + " rounds.");
		long begin = 0;
		long end = 0;
		for (int i = warmups; i > 0; i--) {
			begin = System.currentTimeMillis();
			final Collection<File> files = FileMonitoringService.INSTANCE().getChangesNoPersist();
			if (parse_only) {
				parse(files);
			} else {
				analyze(files);
			}
			MessageService.INSTANCE().clearMessages();
			end = System.currentTimeMillis();
			System.out.println("Round " + (warmups - i + 1) + " done in " + (end - begin) + " ms");
		}
		new File(Environment.INSTANCE().projectDir, ".cache/index.idx").delete();
		MessageService.INSTANCE().clearMessages();
		System.out.println("Warm up completed. Last duration: " + (end - begin) + " ms");
	}

	private void work() {
		Scanner sc = new Scanner(System.in);
		do {
			long begin = 0, end = 0;
			final Collection<File> files = FileMonitoringService.INSTANCE().getChanges();
			if (builderName == null) {
				System.out.println("Processing " + files.size() + " changed files:");
				for (File file : files) {
					System.out.println("\t " + file.getPath());
				}
			}
			begin = System.currentTimeMillis();
			if (parse_only) {
				parse(files);
			} else if (builderName != null) {
				build(buildOnTarget, builderName);
			} else {
				analyze(files);
			}
			end = System.currentTimeMillis();
			final Collection<IMessage> msgs = MessageService.INSTANCE().getMessages();
			for (IMessage msg : msgs) {
				System.out.println(msg);
			}
			System.out.println("Completed in " + (end - begin) + " ms. " + msgs.size() + " messages produced.");
			MessageService.INSTANCE().clearMessages();
		} while (daemon && sc.nextLine() != null);
	}

	private static void parse(Collection<File> files) {
		for (File f : files) {
			ParseService.INSTANCE().parse(f);
		}
	}

	private static void build(String target, String builderName) {
		BuilderService.INSTANCE().callBuilder(new File(target), builderName, true);
	}

	private static void analyze(Collection<File> files) {
		QueableAnalysisService.INSTANCE().enqueueAnalysis(files);
		QueableAnalysisService.INSTANCE().analyzeQueue();
	}

	private void initialize() {
		Environment.INSTANCE().setProjectDir(new File(project_dir));
		final Collection<File> jars = new LinkedList<File>();
		for (String fn : language_jars) {
			jars.add(new File(fn));
		}
		final AdHocJarBasedLanguage lang = new AdHocJarBasedLanguage(langname, extens.toArray(new String[0]),
				language_startsymb, new File(language_tbl), observer_fun, jars.toArray(new File[0]));
		LanguageService.INSTANCE().registerLanguage(lang);
	}

	private void parseArgs(String[] args) throws IllegalArgumentException {
		boolean lang_jar_next = false;
		boolean lang_tbl_next = false;
		boolean proj_dir_next = false;
		boolean extens_next = false;
		boolean lang_name_next = false;
		boolean lang_startsymb_next = false;
		boolean dbg_warmups_next = false;
		boolean call_builder_next = false;
		boolean build_on_next = false;

		for (String a : args) {
			if (a.equals(LANG_JAR)) {
				lang_jar_next = true;
				lang_tbl_next = false;
				proj_dir_next = false;
				extens_next = false;
				lang_name_next = false;
				lang_startsymb_next = false;
				dbg_warmups_next = false;
				call_builder_next = false;
				build_on_next = false;
			} else if (a.equals(LANG_TBL)) {
				lang_jar_next = false;
				lang_tbl_next = true;
				proj_dir_next = false;
				extens_next = false;
				lang_name_next = false;
				lang_startsymb_next = false;
				dbg_warmups_next = false;
				call_builder_next = false;
				build_on_next = false;
			} else if (a.equals(LANG_NAME)) {
				lang_jar_next = false;
				lang_tbl_next = false;
				proj_dir_next = false;
				extens_next = false;
				lang_name_next = true;
				lang_startsymb_next = false;
				dbg_warmups_next = false;
				call_builder_next = false;
				build_on_next = false;
			} else if (a.equals(PROJ_DIR)) {
				lang_jar_next = false;
				lang_tbl_next = false;
				proj_dir_next = true;
				extens_next = false;
				lang_name_next = false;
				lang_startsymb_next = false;
				dbg_warmups_next = false;
				call_builder_next = false;
				build_on_next = false;
			} else if (a.equals(EXT_ENS)) {
				lang_jar_next = false;
				lang_tbl_next = false;
				proj_dir_next = false;
				extens_next = true;
				lang_name_next = false;
				lang_startsymb_next = false;
				dbg_warmups_next = false;
				call_builder_next = false;
				build_on_next = false;
			} else if (a.equals(LANG_SSYMB)) {
				lang_jar_next = false;
				lang_tbl_next = false;
				proj_dir_next = false;
				extens_next = false;
				lang_name_next = false;
				lang_startsymb_next = true;
				dbg_warmups_next = false;
				call_builder_next = false;
				build_on_next = false;
			} else if (a.equals(DBG_WARM)) {
				lang_jar_next = false;
				lang_tbl_next = false;
				proj_dir_next = false;
				extens_next = false;
				lang_name_next = false;
				lang_startsymb_next = false;
				dbg_warmups_next = true;
				call_builder_next = false;
				build_on_next = false;
			} else if (a.equals(PARSE_ONLY)) {
				lang_jar_next = false;
				lang_tbl_next = false;
				proj_dir_next = false;
				extens_next = false;
				lang_name_next = false;
				this.parse_only = true;
				lang_startsymb_next = false;
				dbg_warmups_next = false;
				call_builder_next = false;
				build_on_next = false;
			} else if (a.equals(MOD_DAEMON)) {
				lang_jar_next = false;
				lang_tbl_next = false;
				proj_dir_next = false;
				extens_next = false;
				lang_name_next = false;
				this.daemon = true;
				lang_startsymb_next = false;
				dbg_warmups_next = false;
				call_builder_next = false;
				build_on_next = false;
			} else if (a.equals(CALL_BUILDER)) {
				lang_jar_next = false;
				lang_tbl_next = false;
				proj_dir_next = false;
				extens_next = false;
				lang_name_next = false;
				lang_startsymb_next = false;
				dbg_warmups_next = false;
				call_builder_next = true;
				build_on_next = false;
			} else if (a.equals(BUILD_ON)) {
				lang_jar_next = false;
				lang_tbl_next = false;
				proj_dir_next = false;
				extens_next = false;
				lang_name_next = false;
				lang_startsymb_next = false;
				dbg_warmups_next = false;
				call_builder_next = false;
				build_on_next = true;
			} else {
				if (lang_jar_next) {
					language_jars.add(a);
				} else if (lang_tbl_next) {
					language_tbl = a;
				} else if (proj_dir_next) {
					project_dir = a;
				} else if (extens_next) {
					extens.add(a);
				} else if (lang_name_next) {
					langname = a;
				} else if (lang_startsymb_next) {
					language_startsymb = a;
				} else if (dbg_warmups_next) {
					warmups = Integer.parseInt(a);
				} else if (call_builder_next) {
					builderName = a;
				} else if (build_on_next) {
					buildOnTarget = a;
				}
			}
		}
		if (language_jars.isEmpty()) {
			throw new IllegalArgumentException("Missing --lang-jar argument");
		} else if (language_tbl == null) {
			throw new IllegalArgumentException("Missing --lang-tbl argument");
		} else if (project_dir == null) {
			throw new IllegalArgumentException("Missing --proj-dir argument");
		} else if (extens.isEmpty()) {
			throw new IllegalArgumentException("Missing --extens argument");
		} else if (langname == null) {
			throw new IllegalArgumentException("Missing --lang-name argument");
		} else if (language_startsymb == null) {
			throw new IllegalArgumentException("Missing --start-symbol argument");
		} else if (builderName != null && buildOnTarget == null) {
			throw new IllegalArgumentException("Missing --build-on argument");
		} else if (builderName == null && buildOnTarget != null) {
			throw new IllegalArgumentException("Missing --builder argument");
		} else if (builderName != null && warmups > 0) {
			throw new IllegalArgumentException("Cannot warm up when calling a builder");
		} else if (parse_only && builderName != null) {
			throw new IllegalArgumentException("Parse only is incompatible with running a builder");
		}

		System.out.println("Parameters:");
		System.out.println("\t JARS: " + this.language_jars);
		System.out.println("\t TBL: " + this.language_tbl);
		System.out.println("\t PROJ: " + this.project_dir);
		System.out.println("\t DAEMON: " + this.daemon);
		System.out.println("\t WARMUPS: " + this.warmups);
		System.out.println("\t BUILDER: " + this.builderName);
		System.out.println("\t BUILDON: " + this.buildOnTarget);
		System.out.println("---------------------------------");
	}
}