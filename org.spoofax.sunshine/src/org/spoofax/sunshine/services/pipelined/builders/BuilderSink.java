/**
 * 
 */
package org.spoofax.sunshine.services.pipelined.builders;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.sunshine.CompilerException;
import org.spoofax.sunshine.Environment;
import org.spoofax.sunshine.model.language.ALanguage;
import org.spoofax.sunshine.pipeline.ISinkOne;
import org.spoofax.sunshine.pipeline.diff.Diff;
import org.spoofax.sunshine.services.LanguageService;
import org.spoofax.sunshine.services.StrategoCallService;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class BuilderSink implements ISinkOne<BuilderInputTerm> {

	private static final Logger logger = LogManager.getLogger(BuilderSink.class.getName());

	private final String builderName;

	public BuilderSink(String builderName) {
		this.builderName = builderName;
		logger.trace("Created new builder for builder-name {}", builderName);
	}

	/**
	 * Call a builder on the file's source or analyzed AST. The builder is expected to have the
	 * following format:
	 * 
	 * <code>
	 * builder:
	 * 	(node, position, ast, path, project-path) -> (filename, result)
	 * 	
	 * </code>
	 * 
	 * NB: The current implementation calls the builder on the following input:
	 * 
	 * <code>
	 * 	(ast, [], ast, path, project-path)
	 * </code>
	 * 
	 * NB: The current implementation assumes that the result is a StrategoString and will not work
	 * with a term.
	 * 
	 * @param product
	 *            The {@link BuilderInputTerm} to run this builder on.
	 * 
	 * @throws CompilerException
	 */
	@Override
	public void sink(Diff<BuilderInputTerm> product) {
		logger.debug("Invoking builder {} on file {}", builderName, product.getPayload().getFile());
		File result = callBuilder(product.getPayload());
		logger.info("Builder {} called on file {} and produced file {}", builderName, product
				.getPayload().getFile(), result.getAbsolutePath());
	}

	private File callBuilder(BuilderInputTerm input) throws CompilerException {
		assert builderName != null && builderName.length() > 0;

		if (input == null) {
			throw new CompilerException("Builder " + builderName
					+ "failed. No input term available.");
		}
		final IStrategoTerm inputTuple = input.toStratego();
		ensureProperInput(inputTuple);

		final ALanguage lang = LanguageService.INSTANCE().getLanguageByExten(input.getFile());

		ensurePropertLanguage(lang);

		IStrategoTerm result = null;
		result = StrategoCallService.INSTANCE().callStratego(lang, builderName, inputTuple);

		ensureProperResult(result);

		final File resultFile = new File(Environment.INSTANCE().projectDir,
				((IStrategoString) result.getSubterm(0)).stringValue());
		final String resultContents = ((IStrategoString) result.getSubterm(1)).stringValue();
		// write the contents to the file
		try {
			FileUtils.writeStringToFile(resultFile, resultContents);
		} catch (IOException e) {
			throw new CompilerException("Builder " + builderName + "failed to save result", e);
		}

		return resultFile;
	}

	private void ensurePropertLanguage(final ALanguage lang) {
		assert lang != null;
	}

	private void ensureProperInput(final IStrategoTerm inputTuple) {
		assert inputTuple != null && inputTuple.getSubtermCount() == 5;
	}

	private void ensureProperResult(final IStrategoTerm result) {
		assert result != null : "StrategoCallService returned null. BUG!";
		assert result.getSubtermCount() == 2;
		assert result.getSubterm(0) instanceof IStrategoString;
		assert result.getSubterm(1) instanceof IStrategoString;
	}

}
