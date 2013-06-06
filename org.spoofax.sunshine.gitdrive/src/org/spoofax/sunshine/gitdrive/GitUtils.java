/**
 * 
 */
package org.spoofax.sunshine.gitdrive;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.spoofax.sunshine.CompilerException;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public final class GitUtils {

	public static List<RevCommit> getCommits(Git git, RevFilter filter, boolean ascOrder) {
		final Repository repo = git.getRepository();
		final RevWalk rw = new RevWalk(repo);

		try {
			rw.markStart(rw.parseCommit(repo.resolve(Constants.HEAD)));
		} catch (Exception ex) {
			throw new RuntimeException("Failed to initialize revwalker", ex);
		}
		if (filter != null)
			rw.setRevFilter(filter);
		Iterator<RevCommit> iter = rw.iterator();
		List<RevCommit> commits = new ArrayList<RevCommit>();
		while (iter.hasNext()) {
			commits.add(iter.next());
		}
		if (ascOrder)
			Collections.reverse(commits);
		rw.release();
		rw.dispose();
		return commits;
	}

	public static void cleanVeryHard(Git git) {
		Process proc;
		try {
			proc = Runtime.getRuntime().exec("git clean -f -f -d", new String[0],
					git.getRepository().getDirectory().getParentFile());
			proc.waitFor();
			if (proc.exitValue() != 0) {
				throw new CompilerException("Could not clean git repository");
			}
		} catch (Exception e) {
			throw new CompilerException("Could not clean git repository", e);
		}
	}

	public static void stepRevision(Git git, RevCommit from, RevCommit to) {
		try {
			git.checkout().setName(to.getName()).setCreateBranch(true).setStartPoint(to).call();
			updateSubmodule(git);
			cleanVeryHard(git);
			if (from != null) {
				deleteBranch(git, from.getName());
			}
		} catch (Exception gitex) {
			throw new CompilerException(
					"Failed to pull submodule for project. Currently at revision " + to.getId(),
					gitex);
		}
	}

	public static void checkoutBranch(Git git, String branchname) {
		try {
			git.checkout().setName(branchname).call();
		} catch (GitAPIException gitex) {
			throw new CompilerException("Failed to checkout out branch " + branchname, gitex);
		}
	}

	public static void deleteBranch(Git git, String branchname) {
		try {
			git.branchDelete().setBranchNames(branchname).setForce(true).call();
		} catch (GitAPIException gitex) {
			throw new CompilerException("Failed to delete branch " + branchname, gitex);
		}
	}

	public static void updateSubmodule(Git git) {
		try {
			git.submoduleUpdate().call();
		} catch (GitAPIException gitex) {
			throw new CompilerException("Failed to update submodules", gitex);
		}
	}

}
