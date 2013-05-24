/**
 * 
 */
package org.spoofax.sunshine.model.language;

import java.io.File;
import java.util.Collection;

import org.spoofax.sunshine.parser.model.IParseTableProvider;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public abstract class ALanguage {

	protected final String name;
	protected final LanguageNature nature;

	protected ALanguage(String name, LanguageNature nature) {
		assert name != null && name.length() > 0;
		assert nature != null;
		this.name = name;
		this.nature = nature;
	}

	public String getName() {
		return this.name;
	}

	public LanguageNature getNature() {
		return nature;
	}

	public abstract Collection<String> getFileExtensions();

	public abstract String getStartSymbol();

	public abstract IParseTableProvider getParseTableProvider();

	public abstract File[] getCompilerFiles();

	public abstract String getAnalysisFunction();

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ALanguage) {
			ALanguage ol = (ALanguage) obj;
			return getName().equals(ol.getName());
		}
		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		return getName().hashCode() + 42;
	}

	@Override
	public String toString() {
		String s = "";
		s += "Name: " + name + "\n";
		s += "Nature: " + nature + "\n";
		s += "Extensions: " + getFileExtensions() + "\n";
		s += "Parsetable: " + getParseTableProvider() + "\n";
		s += "Code files: " + getCompilerFiles() + "\n";
		s += "Observer: " + getAnalysisFunction() + "\n";
		return s;
	}

}
