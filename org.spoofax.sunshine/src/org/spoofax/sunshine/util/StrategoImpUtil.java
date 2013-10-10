package org.spoofax.sunshine.util;

import static org.spoofax.interpreter.terms.IStrategoTerm.APPL;
import static org.spoofax.terms.attachments.ParentAttachment.getParent;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.imploder.TermTreeFactory;
import org.spoofax.sunshine.Environment;

/**
 * Some utility functions copied from org.strategoxt.imp.runtime package. They are here to allow us
 * to remove the IMP dependencies.
 * 
 * @author Volker Lanting
 * 
 */
public class StrategoImpUtil {

	// As sunshine uses Loggers instead of Environment for logging
	private static final Logger LOG = LogManager.getLogger(StrategoImpUtil.class);

	// Copied from org.strategoxt.imp.runtime.dynamicloading.TermReader
	public static IStrategoAppl findTerm(IStrategoTerm term, String constructor) {
		if (term.getTermType() == IStrategoTerm.APPL && cons(term).equals(constructor))
			return (IStrategoAppl) term;

		IStrategoTerm[] subterms = term.getAllSubterms();
		for (int i = subterms.length - 1; i >= 0; i--) {
			IStrategoAppl result = findTerm(subterms[i], constructor);
			if (result != null)
				return result;
		}

		return null;
	}

	// Copied from org.strategoxt.imp.runtime.dynamicloading.TermReader
	// NOTE: private until required by an external class
	private static String cons(IStrategoTerm t) {
		if (t == null || t.getTermType() != APPL)
			return null;
		return ((IStrategoAppl) t).getConstructor().getName();
	}

	/**
	 * Find the common ancestor of two AST nodes, creating a SubListAstNode if they are in the same
	 * list ancestor.
	 * 
	 * Copied from org.strategoxt.imp.runtime.stratego.StrategoTermPath
	 */
	public static IStrategoTerm findCommonAncestor(IStrategoTerm node1, IStrategoTerm node2) {
		if (node1 == null)
			return node2;
		if (node2 == null)
			return node1;

		List<IStrategoTerm> node1Ancestors = new ArrayList<IStrategoTerm>();
		for (IStrategoTerm n = node1; n != null; n = getParent(n))
			node1Ancestors.add(n);

		for (IStrategoTerm n = node2, n2Child = node2; n != null; n2Child = n, n = getParent(n)) {
			int node1Index = node1Ancestors.indexOf(n);
			if (node1Index != -1 && node1Ancestors.get(node1Index) == n) // common ancestor w/
																			// reference equality
				return tryCreateListCommonAncestor(n, node1Ancestors, n2Child);
		}

		LOG.warn(
				"Could not find common ancestor for nodes: " + node1 + "," + node2);
		assert false : "Could not find common ancestor for nodes: " + node1 + "," + node2;
		return getRoot(node1);
	}

	// Copied from org.strategoxt.imp.runtime.stratego.StrategoTermPath
	private static IStrategoTerm tryCreateListCommonAncestor(IStrategoTerm commonAncestor,
			List<IStrategoTerm> ancestors1List, IStrategoTerm child2) {
		if (commonAncestor != child2 && commonAncestor.isList()) {
			int i = ancestors1List.indexOf(commonAncestor);
			if (i == 0)
				return commonAncestor;
			IStrategoTerm child1 = ancestors1List.get(i - 1);
			return new TermTreeFactory(Environment.INSTANCE().termFactory).createSublist(
					(IStrategoList) commonAncestor, child1, child2);
		} else {
			return commonAncestor;
		}
	}

	// Copied from org.strategoxt.imp.runtime.stratego.StrategoTermPath
	private static IStrategoTerm getRoot(IStrategoTerm selection) {
		IStrategoTerm result = selection;
		while (getParent(result) != null)
			result = getParent(result);
		return result;
	}
	

}
