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
package org.eclipse.text.tests.link;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.link.ILinkedListener;
import org.eclipse.jface.text.link.LinkedEnvironment;
import org.eclipse.jface.text.link.LinkedPosition;
import org.eclipse.jface.text.link.LinkedPositionGroup;


public class LinkedEnvironmentTest extends TestCase {
	
	private List fPositions= new LinkedList();

	private List fDocumentMap= new ArrayList();

	public void testUpdate() throws BadLocationException {
		IDocument doc1= new Document(GARTEN1);
		
		// set up linked mode
		LinkedPositionGroup group1= new LinkedPositionGroup();
		createLinkedPositions(group1, doc1, "MARGARETE");
		LinkedEnvironment env= new LinkedEnvironment();
		env.addGroup(group1);
		env.forceInstall();
		
		// edit the document
		doc1.replace(1, 9, "GRETCHEN");
		
		assertEquals(group1, "GRETCHEN");
		assertUnchanged(group1);
	}
	
	public void testUpdateTwoGroups() throws BadLocationException {
		IDocument doc1= new Document(GARTEN1);
		
		// set up linked mode
		LinkedPositionGroup group1= new LinkedPositionGroup();
		createLinkedPositions(group1, doc1, "MARGARETE");
		
		LinkedPositionGroup group2= new LinkedPositionGroup();
		createLinkedPositions(group2, doc1, "FAUST");
		
		LinkedEnvironment env= new LinkedEnvironment();
		env.addGroup(group1);
		env.addGroup(group2);
		
		env.forceInstall();
		
		
		// edit the document
		doc1.replace(7, 3, "INE");
		
		assertEquals(group1, "MARGARINE");
		assertEquals(group2, "FAUST");
		assertUnchanged(group1, group2);
	}

	public void testUpdateMultipleGroups() throws BadLocationException {
		IDocument doc1= new Document(GARTEN1);
		
		// set up linked mode
		LinkedPositionGroup group1= new LinkedPositionGroup();
		createLinkedPositions(group1, doc1, "MARGARETE");
		
		LinkedPositionGroup group2= new LinkedPositionGroup();
		createLinkedPositions(group2, doc1, "FAUST");
		
		LinkedEnvironment env= new LinkedEnvironment();
		env.addGroup(group1);
		env.addGroup(group2);
		
		env.forceInstall();
		
		
		// edit the document
		doc1.replace(7, 3, "INE");
		doc1.replace(42, 1, "");
		doc1.replace(44, 2, "GE");
		
		assertEquals(group1, "MARGARINE");
		assertEquals(group2, "AUGE");
		assertUnchanged(group1, group2);
	}
	
	public void testUpdateMultiDocument() throws BadLocationException {
		IDocument doc1= new Document(GARTEN1);
		IDocument doc2= new Document(GARTEN2);
		
		// set up linked mode
		LinkedPositionGroup group1= new LinkedPositionGroup();
		createLinkedPositions(group1, doc1, "MARGARETE");
		createLinkedPositions(group1, doc2, "MARGARETE");
		
		LinkedPositionGroup group2= new LinkedPositionGroup();
		createLinkedPositions(group2, doc1, "FAUST");
		createLinkedPositions(group2, doc2, "FAUST");
		
		LinkedEnvironment env= new LinkedEnvironment();
		env.addGroup(group1);
		env.addGroup(group2);
		
		env.forceInstall();
		
		
		// edit the document
		doc1.replace(7, 3, "INE");
		doc1.replace(42, 1, "");
		doc1.replace(44, 2, "GE");
		
		assertEquals(group1, "MARGARINE");
		assertEquals(group2, "AUGE");
		assertUnchanged(group1, group2);
	
	}

	public void testAddCompatibleGroups() throws BadLocationException {
		IDocument doc1= new Document(GARTEN1);
		
		// set up linked mode
		LinkedPositionGroup group1= new LinkedPositionGroup();
		createLinkedPositions(group1, doc1, "MARGARETE");
		
		LinkedPositionGroup group2= new LinkedPositionGroup();
		createLinkedPositions(group2, doc1, "FAUST");
		
		LinkedEnvironment env= new LinkedEnvironment();
		try {
			env.addGroup(group1);
			env.addGroup(group2);
		} catch (BadLocationException e) {
			assertFalse(true);
		}
		assertUnchanged(group1, group2);
	
	}

	public void testAddIncompatibleGroups() throws BadLocationException {
		IDocument doc1= new Document(GARTEN1);
		
		// set up linked mode
		LinkedPositionGroup group1= new LinkedPositionGroup();
		createLinkedPositions(group1, doc1, "MARGARETE");
		
		LinkedPositionGroup group2= new LinkedPositionGroup();
		createLinkedPositions(group2, doc1, "MARGA");
		
		LinkedEnvironment env= new LinkedEnvironment();
		try {
			env.addGroup(group1);
			env.addGroup(group2);
		} catch (BadLocationException e) {
			return;
		}
		assertFalse(true);		
	}
	
	public void testAddNullGroup() throws BadLocationException {
		LinkedEnvironment env= new LinkedEnvironment();
		try {
			env.addGroup(null);
		} catch (IllegalArgumentException e) {
			return;
		}
		
		assertFalse(true);
	}
	
	public void testAddGroupWhenSealed() throws BadLocationException {
		IDocument doc1= new Document(GARTEN1);
		
		// set up linked mode
		LinkedPositionGroup group1= new LinkedPositionGroup();
		createLinkedPositions(group1, doc1, "MARGARETE");
		LinkedEnvironment env= new LinkedEnvironment();
		env.addGroup(group1);
		env.forceInstall();

		LinkedPositionGroup group2= new LinkedPositionGroup();
		createLinkedPositions(group2, doc1, "FAUST");
		try {
			env.addGroup(group2);
		} catch (IllegalStateException e) {
			return;
		}
		
		assertFalse(true);
	}

	public void testDoubleInstall() throws BadLocationException {
		IDocument doc1= new Document(GARTEN1);
		
		// set up linked mode
		LinkedPositionGroup group1= new LinkedPositionGroup();
		createLinkedPositions(group1, doc1, "MARGARETE");
		LinkedEnvironment env= new LinkedEnvironment();
		env.addGroup(group1);
		
		env.forceInstall();
		
		try {
			env.forceInstall();
		} catch (IllegalStateException e) {
			return;
		}
		
		assertFalse(true);
	}
	
	public void testEmptyInstall() throws BadLocationException {
		LinkedEnvironment env= new LinkedEnvironment();
		
		try {
			env.forceInstall();
		} catch (IllegalStateException e) {
			return;
		}
		
		assertFalse(true);
	}
	
	public void testNestedUpdate() throws BadLocationException {
		IDocument doc1= new Document(GARTEN1);
		
		// set up linked mode
		LinkedPositionGroup group1= new LinkedPositionGroup();
		createLinkedPositions(group1, doc1, "MARGARETE");
		
		LinkedPositionGroup group2= new LinkedPositionGroup();
		createLinkedPositions(group2, doc1, "FAUST");
		
		LinkedEnvironment env= new LinkedEnvironment();
		env.addGroup(group1);
		env.addGroup(group2);
		
		env.forceInstall();
		
		// second level
		
		LinkedPositionGroup group1_2= new LinkedPositionGroup();
		group1_2.createPosition(doc1, 7, 3);
		
		
		LinkedEnvironment childEnv= new LinkedEnvironment();
		childEnv.addGroup(group1_2);
		childEnv.forceInstall();
		
		assertTrue(childEnv.isNested());
		assertFalse(env.isNested());
		
		
		// edit the document
		doc1.replace(7, 3, "INE");
		
		assertEquals(group1_2, "INE");
		assertEquals(group1, "MARGARINE");
		assertEquals(group2, "FAUST");		
		assertUnchanged(group1, group2);
	}
	
	public void testNestedForceInstall() throws BadLocationException {
		IDocument doc1= new Document(GARTEN1);
		
		// set up linked mode
		LinkedPositionGroup group1= new LinkedPositionGroup();
		createLinkedPositions(group1, doc1, "MARGARETE");
		
		LinkedPositionGroup group2= new LinkedPositionGroup();
		createLinkedPositions(group2, doc1, "FAUST");
		
		LinkedEnvironment env= new LinkedEnvironment();
		env.addGroup(group1);
		env.addGroup(group2);
		
		final boolean[] isExit= { false } ;
		env.addLinkedListener(new LinkedAdapter() {
			public void left(LinkedEnvironment environment, int flags) {
				isExit[0]= true;
			}
		});
		
		env.forceInstall();
		
		
		// second level
		
		LinkedPositionGroup group1_2= new LinkedPositionGroup();
		
		group1_2.createPosition(doc1, 12, 3);
		
		LinkedEnvironment childEnv= new LinkedEnvironment();
		childEnv.addGroup(group1_2);
		childEnv.forceInstall();
		
		assertFalse(childEnv.isNested());
		assertTrue(isExit[0]);
		
		
		// edit the document
		doc1.replace(12, 3, "INE");
		
		assertEquals(group1_2, "INE");
	}
	
	public void testNestedTryInstall() throws BadLocationException {
		IDocument doc1= new Document(GARTEN1);
		
		// set up linked mode
		LinkedPositionGroup group1= new LinkedPositionGroup();
		createLinkedPositions(group1, doc1, "MARGARETE");
		
		LinkedPositionGroup group2= new LinkedPositionGroup();
		createLinkedPositions(group2, doc1, "FAUST");
		
		LinkedEnvironment env= new LinkedEnvironment();
		env.addGroup(group1);
		env.addGroup(group2);
		env.forceInstall();
		
		
		// second level
		
		LinkedPositionGroup group1_2= new LinkedPositionGroup();
		group1_2.createPosition(doc1, 12, 3);
		
		LinkedEnvironment childEnv= new LinkedEnvironment();
		childEnv.addGroup(group1_2);
		
		final boolean[] isExit= { false } ;
		env.addLinkedListener(new LinkedAdapter() {
			public void left(LinkedEnvironment environment, int flags) {
				isExit[0]= true;
			}
		});
		
		assertFalse(childEnv.tryInstall());
		assertFalse(childEnv.isNested());
		
		
		// edit the document
		doc1.replace(7, 3, "INE");
		
		assertEquals(group1, "MARGARINE");
		assertUnchanged(group1, group2);
	}
	
	private void assertEquals(LinkedPositionGroup group, String expected) throws BadLocationException {
		LinkedPosition[] positions= group.getPositions();
		for (int i= 0; i < positions.length; i++) {
			LinkedPosition pos= positions[i];
			assertEquals(expected, pos.getContent());
		}
	}
	
	private void assertUnchanged(LinkedPositionGroup actual1) throws BadLocationException {
		assertUnchanged(actual1, new LinkedPositionGroup());
	}
	
	private void assertUnchanged(LinkedPositionGroup actual1, LinkedPositionGroup actual2) throws BadLocationException {
		LinkedPosition[] exp= (LinkedPosition[]) fPositions.toArray(new LinkedPosition[0]);
		LinkedPosition[] act1= actual1.getPositions();
		LinkedPosition[] act2= actual2.getPositions();
		LinkedPosition[] act= new LinkedPosition[act1.length + act2.length];
		System.arraycopy(act1, 0, act, 0, act1.length);
		System.arraycopy(act2, 0, act, act1.length, act2.length);
		Arrays.sort(act, new PositionComparator());
		Arrays.sort(exp, new PositionComparator());
		
		assertEquals(exp.length, act.length);
		
		LinkedPosition e_prev= null, a_prev= null;
		for (int i= 0; i <= exp.length; i++) {
			LinkedPosition e_next= i == exp.length ? null : exp[i];
			LinkedPosition a_next= i == exp.length ? null : act[i];
			
			IDocument e_doc= e_prev != null ? e_prev.getDocument() : e_next.getDocument();
			if (e_next != null && e_next.getDocument() != e_doc) {
				// split at document boundaries
				assertEquals(getContentBetweenPositions(e_prev, null), getContentBetweenPositions(a_prev, null));
				assertEquals(getContentBetweenPositions(null, e_next), getContentBetweenPositions(null, a_next));
			} else {
				assertEquals(getContentBetweenPositions(e_prev, e_next), getContentBetweenPositions(a_prev, a_next));
			}
			
			e_prev= e_next;
			a_prev= a_next;
		}
	}
	
	private String getContentBetweenPositions(LinkedPosition p1, LinkedPosition p2) throws BadLocationException {
		if (p1 == null && p2 == null)
			return null;
		if (p1 == null)
			p1= new LinkedPosition(p2.getDocument(), 0, 0);
		
		if (p2 == null)
			p2= new LinkedPosition(p1.getDocument(), p1.getDocument().getLength(), 0);
		
		IDocument document= p1.getDocument();
		
		int offset= p1.getOffset() + p1.getLength();
		int length= p2.getOffset() - offset;
		
		return document.get(offset, length);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	protected void setUp() throws Exception {
		fPositions.clear();
		fDocumentMap.clear();
	}
	
	/**
	 * Returns a test group on a copy of the document
	 */
	private void createLinkedPositions(LinkedPositionGroup group, IDocument doc, String substring) throws BadLocationException {
		String text= doc.get();
		
		IDocument original= getOriginal(doc);
		if (original == null) {
			original= new Document(text);
			putOriginal(doc, original);
		}
			
		
		for (int offset= text.indexOf(substring); offset != -1; offset= text.indexOf(substring, offset + 1)) {
			group.createPosition(doc, offset, substring.length());
			fPositions.add(new LinkedPosition(original, offset, substring.length()));
		}
		
	}

	private void putOriginal(IDocument doc, IDocument original) {
		fDocumentMap.add(new IDocument[] { doc, original });
	}

	private IDocument getOriginal(IDocument doc) {
		for (Iterator it = fDocumentMap.iterator(); it.hasNext(); ) {
			IDocument[] docs = (IDocument[]) it.next();
			if (docs[0] == doc)
				return docs[1];
		}
		return null;
	}

	private static final String GARTEN1= 
		"	MARGARETE:\n" + 
		"	Versprich mir, Heinrich!\n" + 
		"	 \n" + 
		"	FAUST:\n" + 
		"	Was ich kann!\n" + 
		"	 \n" + 
		"	MARGARETE:\n" + 
		"	Nun sag, wie hast du\'s mit der Religion?\n" + 
		"	Du bist ein herzlich guter Mann,\n" + 
		"	Allein ich glaub, du h�ltst nicht viel davon.\n" + 
		"	 \n" + 
		"	FAUST:\n" + 
		"	La� das, mein Kind! Du f�hlst, ich bin dir gut;\n" + 
		"	F�r meine Lieben lie�\' ich Leib und Blut,\n" + 
		"	Will niemand sein Gef�hl und seine Kirche rauben.\n" + 
		"	 \n" + 
		"	MARGARETE:\n" + 
		"	Das ist nicht recht, man mu� dran glauben.\n" + 
		"	 \n" + 
		"	FAUST:\n" + 
		"	Mu� man?\n" + 
		"	 \n" + 
		"	MARGARETE:\n" + 
		"	Ach! wenn ich etwas auf dich konnte! Du ehrst auch nicht die heil\'gen Sakramente.\n" + 
		"	 \n" + 
		"	FAUST:\n" + 
		"	Ich ehre sie.\n" + 
		"	 \n" + 
		"	MARGARETE:\n" + 
		"	Doch ohne Verlangen. Zur Messe, zur Beichte bist du lange nicht gegangen.\n" + 
		"	Glaubst du an Gott?\n" + 
		"	 \n" + 
		"	FAUST:\n" + 
		"	Mein Liebchen, wer darf sagen: Ich glaub an Gott?\n" + 
		"	Magst Priester oder Weise fragen,\n" + 
		"	Und ihre Antwort scheint nur Spott\n" + 
		"	�ber den Frager zu sein.\n" + 
		"	 \n" + 
		"	MARGARETE:\n" + 
		"	So glaubst du nicht?\n" + 
		"	 \n" + 
		"	FAUST:\n" + 
		"	Mi�h�r mich nicht, du holdes Angesicht!\n" + 
		"	Wer darf ihn nennen?\n" + 
		"	Und wer bekennen:\n" + 
		"	�Ich glaub ihn!�?\n" + 
		"	Wer empfinden,\n" + 
		"	Und sich unterwinden\n" + 
		"	Zu sagen: �Ich glaub ihn nicht!�?\n" + 
		"	Der Allumfasser,\n" + 
		"	Der Allerhalter,\n" + 
		"	Fa�t und erh�lt er nicht\n" + 
		"	Dich, mich, sich selbst?\n" + 
		"	W�lbt sich der Himmel nicht da droben?\n" + 
		"	Liegt die Erde nicht hier unten fest?\n" + 
		"	Und steigen freundlich blickend\n" + 
		"	Ewige Sterne nicht herauf?\n" + 
		"	Schau ich nicht Aug in Auge dir,\n" + 
		"	Und dr�ngt nicht alles\n" + 
		"	Nach Haupt und Herzen dir,\n" + 
		"	Und webt in ewigem Geheimnis\n" + 
		"	Unsichtbar sichtbar neben dir?\n" + 
		"	Erf�ll davon dein Herz, so gro� es ist,\n" + 
		"	Und wenn du ganz in dem Gef�hle selig bist,\n" + 
		"	Nenn es dann, wie du willst,\n" + 
		"	Nenn\'s Gl�ck! Herz! Liebe! Gott\n" + 
		"	Ich habe keinen Namen\n" + 
		"	Daf�r! Gef�hl ist alles;\n" + 
		"	Name ist Schall und Rauch,\n" + 
		"	Umnebelnd Himmelsglut.\n";
	
	private static final String GARTEN2=
		"	MARGARETE:\n" + 
		"	Das ist alles recht sch�n und gut;\n" + 
		"	Ungef�hr sagt das der Pfarrer auch,\n" + 
		"	Nur mit ein bi�chen andern Worten.\n" + 
		"	 \n" + 
		"	FAUST:\n" + 
		"	Es sagen\'s allerorten\n" + 
		"	Alle Herzen unter dem himmlischen Tage,\n" + 
		"	Jedes in seiner Sprache;\n" + 
		"	Warum nicht ich in der meinen?\n" + 
		"	 \n" + 
		"	MARGARETE:\n" + 
		"	Wenn man\'s so h�rt, m�cht\'s leidlich scheinen,\n" + 
		"	Steht aber doch immer schief darum;\n" + 
		"	Denn du hast kein Christentum.\n" + 
		"	 \n" + 
		"	FAUST:\n" + 
		"	Liebs Kind!\n" + 
		"	 \n" + 
		"	MARGARETE:\n" + 
		"	Es tut mir lange schon weh, Da� ich dich in der Gesellschaft seh.\n" + 
		"	 \n" + 
		"	FAUST:\n" + 
		"	Wieso?\n" + 
		"	 \n" + 
		"	MARGARETE:\n" + 
		"	Der Mensch, den du da bei dir hast, Ist mir in tiefer innrer Seele verha�t;\n" + 
		"	Es hat mir in meinem Leben\n" + 
		"	So nichts einen Stich ins Herz gegeben\n" + 
		"	Als des Menschen widrig Gesicht.\n" + 
		"	 \n" + 
		"	FAUST:\n" + 
		"	Liebe Puppe, f�rcht ihn nicht!\n" + 
		"	 \n" + 
		"	MARGARETE:\n" + 
		"	Seine Gegenwart bewegt mir das Blut.\n" + 
		"	Ich bin sonst allen Menschen gut;\n" + 
		"	Aber wie ich mich sehne, dich zu schauen,\n" + 
		"	Hab ich vor dem Menschen ein heimlich Grauen,\n" + 
		"	Und halt ihn f�r einen Schelm dazu!\n" + 
		"	Gott verzeih mir\'s, wenn ich ihm unrecht tu!\n" + 
		"	 \n" + 
		"	FAUST:\n" + 
		"	Es mu� auch solche K�uze geben.\n" + 
		"	 \n" + 
		"	MARGARETE:\n" + 
		"	Wollte nicht mit seinesgleichen leben!\n" + 
		"	Kommt er einmal zur T�r herein,\n" + 
		"	Sieht er immer so sp�ttisch drein\n" + 
		"	Und halb ergrimmt;\n" + 
		"	Man sieht, da� er an nichts keinen Anteil nimmt;\n" + 
		"	Es steht ihm an der Stirn geschrieben,\n" + 
		"	Da� er nicht mag eine Seele lieben.\n" + 
		"	Mir wird\'s so wohl in deinem Arm,\n" + 
		"	So frei, so hingegeben warm,\n" + 
		"	Und seine Gegenwart schn�rt mir das Innre zu.\n" + 
		"	 \n" + 
		"	FAUST:\n" + 
		"	Du ahnungsvoller Engel du!\n" + 
		"	 \n" + 
		"	MARGARETE:\n" + 
		"	Das �bermannt mich so sehr,\n" + 
		"	Da�, wo er nur mag zu uns treten,\n" + 
		"	Mein ich sogar, ich liebte dich nicht mehr.\n" + 
		"	Auch, wenn er da ist, k�nnt ich nimmer beten,\n" + 
		"	Und das fri�t mir ins Herz hinein;\n" + 
		"	Dir, Heinrich, mu� es auch so sein.\n" + 
		"	 \n" + 
		"	FAUST:\n" + 
		"	Du hast nun die Antipathie!\n" + 
		"	 \n" + 
		"	MARGARETE:\n" + 
		"	Ich mu� nun fort.\n" + 
		"	 \n" + 
		"	FAUST:\n" + 
		"	Ach kann ich nie Ein St�ndchen ruhig dir am Busen h�ngen\n" + 
		"	Und Brust an Brust und Seel in Seele dr�ngen?\n" + 
		"	 \n" + 
		"	MARGARETE:\n" + 
		"	Ach wenn ich nur alleine schlief!\n" + 
		"	Ich lie� dir gern heut nacht den Riegel offen;\n" + 
		"	Doch meine Mutter schl�ft nicht tief,\n" + 
		"	Und w�rden wir von ihr betroffen,\n" + 
		"	Ich w�r gleich auf der Stelle tot!\n" + 
		"	 \n" + 
		"	FAUST:\n" + 
		"	Du Engel, das hat keine Not.\n" + 
		"	Hier ist ein Fl�schchen!\n" + 
		"	Drei Tropfen nur In ihren Trank umh�llen\n" + 
		"	Mit tiefem Schlaf gef�llig die Natur.\n" + 
		"	 \n" + 
		"	MARGARETE:\n" + 
		"	Was tu ich nicht um deinetwillen?\n" + 
		"	Es wird ihr hoffentlich nicht schaden!\n" + 
		"	 \n" + 
		"	FAUST:\n" + 
		"	W�rd ich sonst, Liebchen, dir es raten?\n" + 
		"	 \n" + 
		"	MARGARETE:\n" + 
		"	Seh ich dich, bester Mann, nur an,\n" + 
		"	Wei� nicht, was mich nach deinem Willen treibt,\n" + 
		"	Ich habe schon so viel f�r dich getan,\n" + 
		"	Da� mir zu tun fast nichts mehr �brigbleibt.";

	private class LinkedAdapter implements ILinkedListener {
		public void left(LinkedEnvironment environment, int flags) {}
		public void suspend(LinkedEnvironment environment) {}
		public void resume(LinkedEnvironment environment, int flags) {}
	}

	public class PositionComparator implements Comparator {

		public int compare(Object o1, Object o2) {
			LinkedPosition p1= (LinkedPosition) o1;
			LinkedPosition p2= (LinkedPosition) o2;
			
			IDocument d1= p1.getDocument();
			IDocument d2= p2.getDocument();
			
			if (d1 == d2)
				// sort by offset inside the same document
				return p1.getOffset() - p2.getOffset();
			else
				return getIndex(d1) - getIndex(d2);
		}

		private int getIndex(IDocument doc) {
			int i= 0;
			for (Iterator it= fDocumentMap.iterator(); it.hasNext(); i++) {
				IDocument[] docs= (IDocument[]) it.next();
				if (docs[0] == doc || docs[1] == doc)
					return i;
			}
			return -1;
		}
	}
	
}
