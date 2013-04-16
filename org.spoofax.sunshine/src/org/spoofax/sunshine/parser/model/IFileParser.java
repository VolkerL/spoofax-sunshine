/**
 * 
 */
package org.spoofax.sunshine.parser.model;

import java.io.File;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public interface IFileParser<T> {

    IStrategoParseOrAnalyzeResult parse();

    IParserConfig getConfig();

    File getFile();
}