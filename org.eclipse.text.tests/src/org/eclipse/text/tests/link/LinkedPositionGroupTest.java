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

import junit.framework.TestCase;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.link.LinkedEnvironment;
import org.eclipse.jface.text.link.LinkedPosition;
import org.eclipse.jface.text.link.LinkedPositionGroup;


public class LinkedPositionGroupTest extends TestCase {

	public void testIsEmtpy() {
		LinkedPositionGroup group= new LinkedPositionGroup();
		assertTrue(group.isEmtpy());
	}

	public void testIsNotEmtpy() throws BadLocationException {
		LinkedPositionGroup group= new LinkedPositionGroup();
		group.addPosition(new LinkedPosition(new Document(), 0, 0));
		assertFalse(group.isEmtpy());
	}

	public void testGetPositions() throws BadLocationException {
		LinkedPositionGroup group= new LinkedPositionGroup();
		group.addPosition(new LinkedPosition(new Document(), 0, 0));
		group.addPosition(new LinkedPosition(new Document(), 0, 0));
		assertEquals(2, group.getPositions().length);
	}

	public void testAddPosition() throws BadLocationException {
		LinkedPositionGroup group= new LinkedPositionGroup();
		LinkedPosition p= new LinkedPosition(new Document(), 0, 0);
		group.addPosition(p);
		assertSame(p, group.getPositions()[0]);
	}

	public void testAddIllegalState() throws BadLocationException {
		LinkedPositionGroup group= new LinkedPositionGroup();
		LinkedEnvironment env= new LinkedEnvironment();
		env.addGroup(group);
		
		LinkedPosition p= new LinkedPosition(new Document(), 0, 0);
		try {
			group.addPosition(p);
		} catch (IllegalStateException e) {
			return;
		}
		
		assertFalse(true);
	}
	
	public void testAddBadLocation() throws BadLocationException {
		LinkedPositionGroup group= new LinkedPositionGroup();
		IDocument doc= new Document(GARTEN);
		group.addPosition(new LinkedPosition(doc, 1, 9));
		try {
			group.addPosition(new LinkedPosition(doc, 3, 9));
		} catch (BadLocationException e) {
			return;
		}
		
		assertFalse(true);
	}
	
	public void testAddEqualContent() {
		LinkedPositionGroup group= new LinkedPositionGroup();
		IDocument doc= new Document(GARTEN);
		try {
			group.addPosition(new LinkedPosition(doc, 1, 9));
			group.addPosition(new LinkedPosition(doc, 68, 9));
		} catch (BadLocationException e) {
			assertFalse(true);
		}
	}
	
	public void testAddNotEqualContent() {
		LinkedPositionGroup group= new LinkedPositionGroup();
		IDocument doc= new Document(GARTEN);
		try {
			group.addPosition(new LinkedPosition(doc, 1, 9));
			group.addPosition(new LinkedPosition(doc, 68, 10));
		} catch (BadLocationException e) {
			return;
		}
		assertFalse(true);
	}
	
	private static final String GARTEN= 
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
}
