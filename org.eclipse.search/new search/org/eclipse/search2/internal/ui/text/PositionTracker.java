/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search2.internal.ui.text;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.IFileBuffer;
import org.eclipse.core.filebuffers.IFileBufferListener;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Position;
import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.ISearchResultChangedListener;
import org.eclipse.search.ui.ISearchResultListener;
import org.eclipse.search.ui.SearchResultEvent;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.search.ui.text.IStructureProvider;
import org.eclipse.search.ui.text.ITextSearchResult;
import org.eclipse.search.ui.text.Match;


/**
 * @author Thomas M�der
 *
 */
public class PositionTracker implements ISearchResultListener, ISearchResultChangedListener, IFileBufferListener {

	private Map fMatchesToPositions= new HashMap();
	private Map fMatchesToSearchResults= new HashMap();
	private Map fFileBuffersToMatches= new HashMap();
	
	private interface IFileBufferMatchOperation {
		void run(ITextFileBuffer buffer, Match match);
	}
	
	public PositionTracker() {
		NewSearchUI.getSearchManager().addSearchResultListener(this);
		FileBuffers.getTextFileBufferManager().addFileBufferListener(this);
	}
	
	// tracking search results --------------------------------------------------------------
	public void searchResultAdded(org.eclipse.search.ui.ISearchResult result) {
		if (result instanceof ITextSearchResult) {
			result.addListener(this);
		}
	}
	
	public void searchResultRemoved(ISearchResult result) {
		if (result instanceof ITextSearchResult) {
			untrackAll((ITextSearchResult)result);
			result.removeListener(this);
		}
	}

	// tracking matches ---------------------------------------------------------------------
	public void searchResultsChanged(SearchResultEvent e) {
		if (e instanceof MatchEvent) {
			MatchEvent evt= (MatchEvent)e;
			ITextFileBuffer fb= getTrackedFileBuffer((ITextSearchResult)evt.getSearch(), evt.getMatch().getElement());
			if (fb != null) {
				if (evt.getKind() == MatchEvent.ADDED) {
					trackPosition((ITextSearchResult) e.getSearch(), fb, evt.getMatch());
				} else if (evt.getKind() == MatchEvent.REMOVED) {
					untrackPosition(fb, evt.getMatch());
				}
			}
		} else if (e instanceof RemoveAllEvent) {
			RemoveAllEvent evt= (RemoveAllEvent)e;
			ISearchResult result= evt.getSearch();
			untrackAll((ITextSearchResult)result);
		}
	}

	private void untrackAll(ITextSearchResult result) {
		Set matchSet= new HashSet(fMatchesToPositions.keySet());
		for (Iterator matches= matchSet.iterator(); matches.hasNext();) {
			Match match= (Match) matches.next();
			ITextSearchResult matchContainer= (ITextSearchResult) fMatchesToSearchResults.get(match);
			if (result.equals(matchContainer)) {
				ITextFileBuffer fb= getTrackedFileBuffer(result, match.getElement());
				if (fb != null) {
					untrackPosition(fb, match);
				}
			}
		}
	}

	private void untrackPosition(ITextFileBuffer fb, Match match) {
		Position position= (Position) fMatchesToPositions.get(match);
		if (position != null) {
			removeFileBufferMapping(fb, match);
			fMatchesToSearchResults.remove(match);
			fMatchesToPositions.remove(match);
			fb.getDocument().removePosition(position);
		}
	}

	private void trackPosition(ITextSearchResult result, ITextFileBuffer fb, Match match) {
		Position position= new Position(match.getOffset(), match.getLength());
		try {
			fb.getDocument().addPosition(position);
			fMatchesToSearchResults.put(match, result);
			fMatchesToPositions.put(match, position);
			addFileBufferMapping(fb, match);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	private void addFileBufferMapping(ITextFileBuffer fb, Match match) {
		Set matches= (Set) fFileBuffersToMatches.get(fb);
		if (matches == null) {
			matches= new HashSet();
			fFileBuffersToMatches.put(fb, matches);
		}
		matches.add(match);
	}

	private void removeFileBufferMapping(ITextFileBuffer fb, Match match) {
		Set matches= (Set) fFileBuffersToMatches.get(fb);
		if (matches != null) {
			matches.remove(match);
			if (matches.size() == 0)
				fFileBuffersToMatches.remove(fb);
		}
	}
	
	private ITextFileBuffer getTrackedFileBuffer(ITextSearchResult result, Object element) {
		IFile file= result.getStructureProvider().getFile(element);
		if (file == null)
			return null;
		return FileBuffers.getTextFileBufferManager().getTextFileBuffer(file.getLocation());
	}
	
	public Position getCurrentPosition(Match position) {
		return (Position)fMatchesToPositions.get(position);
	}
	
	public void dispose() {
		NewSearchUI.getSearchManager().removeSearchResultListener(this);
		FileBuffers.getTextFileBufferManager().removeFileBufferListener(this);
	}

	// IFileBufferListener implementation ---------------------------------------------------------------------
	/* (non-Javadoc)
	 * @see org.eclipse.core.filebuffers.IFileBufferListener#bufferCreated(org.eclipse.core.filebuffers.IFileBuffer)
	 */
	public void bufferCreated(IFileBuffer buffer) {
		final int[] trackCount= new int[1];
		if (!(buffer instanceof ITextFileBuffer))
			return;
		IWorkspace ws= ResourcesPlugin.getWorkspace();
		IFile file= ws.getRoot().getFileForLocation(buffer.getLocation());
		if (file == null)
			file= ws.getRoot().getFile(buffer.getLocation());
		ISearchResult[] results= NewSearchUI.getSearchManager().getSearchResults();
		for (int i= 0; i < results.length; i++) {
			if (results[i] instanceof ITextSearchResult) {
				IStructureProvider structureProvider= ((ITextSearchResult)results[i]).getStructureProvider();
				Match[] matches= structureProvider.findContainedMatches((ITextSearchResult) results[i], file);
				for (int j= 0; j < matches.length; j++) {
					trackCount[0]++;
					trackPosition((ITextSearchResult)results[i], (ITextFileBuffer) buffer, matches[j]);
				}
			}
		}
	}
		
	private void doForExistingMatchesIn(IFileBuffer buffer, IFileBufferMatchOperation operation) {
		if (!(buffer instanceof ITextFileBuffer))
			return;
		Set matches= (Set) fFileBuffersToMatches.get(buffer);
		if (matches != null) {
			Set matchSet= new HashSet(matches);
			for (Iterator matchIterator= matchSet.iterator(); matchIterator.hasNext();) {
				Match element= (Match) matchIterator.next();
				operation.run((ITextFileBuffer) buffer, element);
			}
		}
	}
	

	/* (non-Javadoc)
	 * @see org.eclipse.core.filebuffers.IFileBufferListener#bufferDisposed(org.eclipse.core.filebuffers.IFileBuffer)
	 */
	public void bufferDisposed(IFileBuffer buffer) {
		final int[] trackCount= new int[1];
		doForExistingMatchesIn(buffer, new IFileBufferMatchOperation() {
			/* (non-Javadoc)
			 * @see org.eclipse.search.internal.model.PositionTracker.FileBufferMatchRunnable#run(org.eclipse.core.filebuffers.ITextFileBuffer, org.eclipse.search.ui.model.text.Match)
			 */
			public void run(ITextFileBuffer textBuffer, Match match) {
				trackCount[0]++;
				untrackPosition(textBuffer, match);
			}
		});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.filebuffers.IFileBufferListener#bufferContentAboutToBeReplaced(org.eclipse.core.filebuffers.IFileBuffer)
	 */
	public void bufferContentAboutToBeReplaced(IFileBuffer buffer) {
		// not interesting for us.
	}

	public void bufferContentReplaced(IFileBuffer buffer) {
		final int[] trackCount= new int[1];
		doForExistingMatchesIn(buffer, new IFileBufferMatchOperation() {
			public void run(ITextFileBuffer textBuffer, Match match) {
				trackCount[0]++;
				untrackPosition(textBuffer, match);
				ITextSearchResult result= (ITextSearchResult) fMatchesToSearchResults.get(match);
				trackPosition(result, textBuffer, match);
			}
		});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.filebuffers.IFileBufferListener#stateChanging(org.eclipse.core.filebuffers.IFileBuffer)
	 */
	public void stateChanging(IFileBuffer buffer) {
		// not interesting for us
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.filebuffers.IFileBufferListener#dirtyStateChanged(org.eclipse.core.filebuffers.IFileBuffer, boolean)
	 */
	public void dirtyStateChanged(IFileBuffer buffer, boolean isDirty) {
		if (isDirty)
			return;
		final int[] trackCount= new int[1];
		doForExistingMatchesIn(buffer, new IFileBufferMatchOperation() {
			/* (non-Javadoc)
			 * @see org.eclipse.search.internal.model.PositionTracker.FileBufferMatchRunnable#run(org.eclipse.core.filebuffers.ITextFileBuffer, org.eclipse.search.ui.model.text.Match)
			 */
			public void run(ITextFileBuffer textBuffer, Match match) {
				trackCount[0]++;
				Position pos= (Position) fMatchesToPositions.get(match);
				if (pos != null) {
					if (pos.isDeleted()) {
						ITextSearchResult result= (ITextSearchResult) fMatchesToSearchResults.get(match);
						result.removeMatch(match);
					} else {
						match.setOffset(pos.getOffset());
						match.setLength(pos.getLength());
					}
				}
			}
		});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.filebuffers.IFileBufferListener#stateValidationChanged(org.eclipse.core.filebuffers.IFileBuffer, boolean)
	 */
	public void stateValidationChanged(IFileBuffer buffer, boolean isStateValidated) {
		// not interesting for us.
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.filebuffers.IFileBufferListener#underlyingFileMoved(org.eclipse.core.filebuffers.IFileBuffer, org.eclipse.core.runtime.IPath)
	 */
	public void underlyingFileMoved(IFileBuffer buffer, IPath path) {
		// not interesting for us.
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.filebuffers.IFileBufferListener#underlyingFileDeleted(org.eclipse.core.filebuffers.IFileBuffer)
	 */
	public void underlyingFileDeleted(IFileBuffer buffer) {
		// not interesting for us.
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.filebuffers.IFileBufferListener#stateChangeFailed(org.eclipse.core.filebuffers.IFileBuffer)
	 */
	public void stateChangeFailed(IFileBuffer buffer) {
		// not interesting for us.
	}

}
