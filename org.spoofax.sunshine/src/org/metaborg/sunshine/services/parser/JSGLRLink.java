/**
 * 
 */
package org.metaborg.sunshine.services.parser;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.metaborg.sunshine.pipeline.connectors.ALinkOneToOne;
import org.metaborg.sunshine.pipeline.diff.Diff;
import org.metaborg.sunshine.pipeline.diff.DiffKind;
import org.metaborg.sunshine.services.analyzer.AnalysisResult;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class JSGLRLink extends ALinkOneToOne<File, AnalysisResult> {

	private static final Logger logger = LogManager.getLogger(JSGLRLink.class.getName());

	@Override
	public Diff<AnalysisResult> sinkWork(Diff<File> input) {
		if (input.getDiffKind() == DiffKind.DELETION) {
			logger.debug("File {} has been deleted therefore has no AST", input.getPayload()
					.getName());
			return null;
		}
		AnalysisResult parseResult = ParserService.INSTANCE().parseFile(input.getPayload());
		logger.trace("Parsing of file {} produced AST {} and {} messages", input.getPayload(),
				parseResult.ast(), parseResult.messages().size());
		return new Diff<AnalysisResult>(parseResult, input.getDiffKind());
	}

}
