package eu.excitementproject.tl.laputils;

/**
 * Representation of STTS tag set
 * 
 * @author Aleksandra Gabryszak
 *
 */

public enum POSTag_DE {

	ADJA("attributives Adjektiv: [das] große [Haus]"),
	ADJD("adverbiales oder prädikatives Adjektiv: [er fährt] schnell, [er ist] schnell"), 
	ADV("Adverb: schon, bald, doch"),
	APPR("Präposition; Zirkumposition links: in [der Stadt], ohne [mich]"),
	APPRART("Präposition mit Artikel: im [Haus], zur [Sache]"),
	APPO("Postposition:	[ihm] zufolge, [der Sache] wegen"),
	APZR("Zirkumposition rechts: [von jetzt] an"),
	ART("bestimmter oder unbestimmter Artikel: der, die, das, ein, eine"),
	CARD("Kardinalzahl: zwei [Männer], [im Jahre] 1994"),
	FM("Fremdsprachliches Material: [Er hat das mit ``] A big fish ['' übersetzt]"),
	ITJ("Interjektion:	mhm, ach, tja"),
	KOUI("unterordnende Konjunktion mit 'zu' und Infinitiv:	um [zu leben], anstatt [zu fragen]"),
	KOUS("unterordnende Konjunktion mit Satz: weil, dass, damit, wenn, ob"),
	KON("nebenordnende Konjunktion:	und, oder, aber"),
	KOKOM("Vergleichskonjunktion: als, wie"),			 	 	 
	NN("normales Nomen: Tisch, Herr, [das] Reisen"),
	NE("Eigennamen:	Hans, Hamburg, HSV"),
	PDS("substituierendes Demonstrativpronomen:	dieser, jener"),
	PDAT("attribuierendes Demonstrativpronomen:	jener [Mensch]"),
	PIS("substituierendes Indefinitpronomen: keiner, viele, man, niemand"),
	PIAT("attribuierendes Indefinitpronomen ohne Determiner: kein [Mensch], irgendein [Glas]"),
	PIDAT("attribuierendes Indefinitpronomen mit Determiner:	[ein] wenig [Wasser], [die] beiden [Brüder]"),
	PPER("irreflexives Personalpronomen: ich, er, ihm, mich, dir"),
	PPOSS("substituierendes Possessivpronomen: meins, deiner"),
	PPOSAT("attribuierendes Possessivpronomen: mein [Buch], deine [Mutter]"),
	PRELS("substituierendes Relativpronomen: [der Hund ,] der"),
	PRELAT("attribuierendes Relativpronomen: [der Mann ,] dessen [Hund]"),
	PRF("reflexives Personalpronomen: sich, einander, dich, mir"),
	PWS("substituierendes Interrogativpronomen:	wer, was"),
	PWAT("attribuierendes Interrogativpronomen:	welche[Farbe], wessen [Hut]"),
	PWAV("adverbiales Interrogativ- oder Relativpronomen: warum, wo, wann, worüber, wobei"),
	PAV("Pronominaladverb: dafür, dabei, deswegen, trotzdem"),
	PTKZU("'zu' vor Infinitiv: zu [gehen]"),
	PTKNEG("Negationspartikel:	nicht"),
	PTKVZ("abgetrennter Verbzusatz:	[er kommt] an, [er fährt] rad"),
	PTKANT("Antwortpartikel	ja, nein, danke, bitte"),
	PTKA("Partikel bei Adjektiv oder Adverb: am [schönsten], zu [schnell]"),
	TRUNC("Kompositions-Erstglied: An- [und Abreise]"),
	VVFIN("finites Verb, voll: [du] gehst, [wir] kommen [an]"),
	VVIMP("Imperativ, voll:	komm [!]"),
	VVINF("Infinitiv, voll:	gehen, ankommen"),
	VVIZU("Infinitiv mit 'zu', voll: anzukommen, loszulassen"),
	VVPP("Partizip Perfekt, voll: gegangen, angekommen"),
	VAFIN("finites Verb, aux: [du] bist, [wir] werden"),
	VAIMP("Imperativ, aux: sei [ruhig !]"),
	VAINF("Infinitiv, aux:	werden, sein"),
	VAPP("Partizip Perfekt, aux: gewesen"),
	VMFIN("finites Verb, modal:	dürfen"),
	VMINF("Infinitiv, modal	wollen"),
	VMPP("Partizip Perfekt, modal: gekonnt, [er hat gehen] können"),
	XY("Nichtwort, Sonderzeichen enthaltend: 3:7, H2O, D2XW3"),
	SENTENCE_ENDING_PUNCTUATION("Satzbeendende Interpunktion	. ? ! ; :"),
	COMMA("Komma	,"),
	OTHERS("Sonderzeichen: satzintern '-' [ ] ( )");
	
	
	private POSTag_DE(String description){}
	
	/**
	 * map POS value of STTS tag set to POSTag_DE
	 * @param posValue
	 * @return
	 */
	public static POSTag_DE mapToPOStag_DE(String posValue){
		
		if(posValue.equals("ADJA")) 
			return POSTag_DE.ADJA;
		if(posValue.equals("ADJD")) 
			return POSTag_DE.ADJD;
		if(posValue.equals("ADV"))
			return POSTag_DE.ADV;
		if(posValue.equals("APPR"))
			return POSTag_DE.APPR;
		if(posValue.equals("APPRART"))
			return POSTag_DE.APPRART;
		if(posValue.equals("APPO"))
			return POSTag_DE.APPO;
		if(posValue.equals("APZR"))
			return POSTag_DE.APZR;
		if(posValue.equals("ART"))
			return POSTag_DE.ART;
		if(posValue.equals("CARD"))
			return POSTag_DE.CARD;
		if(posValue.equals("FM"))
			return POSTag_DE.FM;
		if(posValue.equals("ITJ"))
			return POSTag_DE.ITJ;
		if(posValue.equals("KOUI"))
			return POSTag_DE.KOUI;
		if(posValue.equals("KOUS"))
			return POSTag_DE.KOUS;
		if(posValue.equals("KON"))
			return POSTag_DE.KON;
		if(posValue.equals("KOKOM"))
			return POSTag_DE.KOKOM; 
		if(posValue.equals("NN"))
			return POSTag_DE.NN;
		if(posValue.equals("NE"))
			return POSTag_DE.NE;
		if(posValue.equals("PDS"))
			return POSTag_DE.PDS;
		if(posValue.equals("PDAT"))
			return POSTag_DE.PDAT;
		if(posValue.equals("PIS"))
			return POSTag_DE.PIS;
		if(posValue.equals("PIAT"))
			return POSTag_DE.PIAT;
		if(posValue.equals("PIDAT"))
			return POSTag_DE.PIDAT;
		if(posValue.equals("PPER"))
			return POSTag_DE.PPER;
		if(posValue.equals("PPOSS"))
			return POSTag_DE.PPOSS;
		if(posValue.equals("PPOSAT"))
			return POSTag_DE.PPOSAT;
		if(posValue.equals("PRELS"))
			return POSTag_DE.PRELS;
		if(posValue.equals("PRELAT"))
			return POSTag_DE.PRELAT; 
		if(posValue.equals("PRF"))
			return POSTag_DE.PRF;
		if(posValue.equals("PWS"))
			return POSTag_DE.PWS;
		if(posValue.equals("PWAT"))
			return POSTag_DE.PWAT;
		if(posValue.equals("PWAV"))
			return POSTag_DE.PWAV;
		if(posValue.equals("PAV"))
			return POSTag_DE.PAV;
		if(posValue.equals("PTKZU"))
			return POSTag_DE.PTKZU;
		if(posValue.equals("PTKNEG"))
			return POSTag_DE.PTKNEG;
		if(posValue.equals("PTKVZ"))
			return POSTag_DE.PTKVZ;
		if(posValue.equals("PTKANT"))
			return POSTag_DE.PTKANT;
		if(posValue.equals("PTKA"))
			return POSTag_DE.PTKA;
		if(posValue.equals("TRUNC"))
			return POSTag_DE.TRUNC;
		if(posValue.equals("VVFIN"))
			return POSTag_DE.VVFIN;
		if(posValue.equals("VVIMP"))
			return POSTag_DE.VVIMP;
		if(posValue.equals("VVINF"))
			return POSTag_DE.VVINF;
		if(posValue.equals("VVIZU"))
			return POSTag_DE.VVIZU;
		if(posValue.equals("VVPP"))
			return POSTag_DE.VVPP;
		if(posValue.equals("VAFIN"))
			return POSTag_DE.VAFIN;
		if(posValue.equals("VAIMP"))
			return POSTag_DE.VAIMP;
		if(posValue.equals("VAINF"))
			return POSTag_DE.VAINF;
		if(posValue.equals("VAPP"))
			return POSTag_DE.VAPP;
		if(posValue.equals("VMFIN"))
			return POSTag_DE.VMFIN;
		if(posValue.equals("VMINF"))
			return POSTag_DE.VMINF;
		if(posValue.equals("VMPP"))
			return POSTag_DE.VMPP;
		if(posValue.equals("XY"))
			return POSTag_DE.XY;
		if(posValue.equals("$."))
			return POSTag_DE.SENTENCE_ENDING_PUNCTUATION;
		if(posValue.equals("$,"))
			return POSTag_DE.COMMA;
		if(posValue.equals("$(")) 
			return POSTag_DE.OTHERS;
		return null;
	}

}
