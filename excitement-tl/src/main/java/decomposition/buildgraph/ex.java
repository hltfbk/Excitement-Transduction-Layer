package decomposition.buildgraph;

import java.io.BufferedWriter;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import decomposition.buildgraph.Document.Markables.FOCUS;
import decomposition.buildgraph.Document.Markables.FRAGMENT;
import decomposition.buildgraph.Document.Markables.MODIFIER;
import decomposition.buildgraph.Document.Relations.BEFORE;
import decomposition.buildgraph.Document.Relations.ENTAILS;
import decomposition.buildgraph.Document.Relations.EQUIVALENT;
import decomposition.buildgraph.Document.Token;

public class ex {
	
	static String fileName="";
	static Document gfile;
	static List<String> modifiers = new ArrayList<String>();//This is a list of modifier ids related to the fragement. updated each time while reading the fragement
	/**
	 * @param args
	 * @throws JAXBException 
	 * @throws IOException 
	 */
	public static String run(String fileName) throws JAXBException, IOException {
		File file = new File(fileName);
 	    String filepath = file.getParentFile().getAbsolutePath() ;
		String filePathMy = filepath+"/my/";
		//System.out.println(filepath);
		ClassLoader cl = ObjectFactory.class.getClassLoader(); //added by KATHRIN
		JAXBContext jc = JAXBContext.newInstance("decomposition.buildgraph", cl); //added by KATHRIN
	    Unmarshaller unmarshaller = jc.createUnmarshaller();
	  // Document myFile = (Document) unmarshaller.unmarshal(new File(fileName));
	   Document myFile = (Document) unmarshaller.unmarshal(new InputStreamReader(new FileInputStream(fileName), "UTF-8"));

	   gfile=myFile;
	   String fname = myFile.getDocName();
	   String fname1 = fname;
		String[] cc1 =  Pattern.compile("\\.").split(fname);
		fname = cc1[0];
		
	   ListIterator<FRAGMENT> lFragment = myFile.getMarkables().get(0).getFRAGMENT().listIterator();
	   //get Fragements
		while(lFragment.hasNext()){
			FRAGMENT tempFrag = lFragment.next();
			String fid = tempFrag.getId();
			List<String> maptable = getmaptable(tempFrag);
			String outID = "\""+fname1+"_"+fid+"\"";
			List<String> tempF = getF(tempFrag.getTokenAnchor());
			String outF = repareF(tempFrag,tempF);
			String outFC = getFC(tempFrag.getTokenAnchor(),maptable);
			String BP = getBP(tempFrag.getTokenAnchor(),maptable);
			String MODL = getMODL(tempFrag.getTokenAnchor(),maptable);
			List<String> mapmodl = getmapmodl(MODL);
			String MODC = getMODCMap(mapmodl, maptable);
			MODC = getTransfer(MODC);
			//String MODC = getMODC(MODL, maptable);
			//String MODR = getMODR(MODL, maptable);
			String MODR = getMODRMap(mapmodl, maptable);
			MODR = getTransfer(MODR);
			String output=outID+" "+outF+" "+outFC+" "+BP+" "+MODL+" "+MODC+" "+MODR;
			
			String outpath=filepath+"/my/"+outID.replaceAll("\"", "")+".txt";
			new File(filePathMy).mkdirs();
			//System.out.println(outpath);
			new File(outpath).createNewFile();
			//BufferedWriter writer = new BufferedWriter(new FileWriter(outpath));
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outpath), "UTF-8"));
          
			writer.write(output);
			writer.close();
			
			modifiers.clear();
		}//get Fragements
		return filePathMy;
	}
	
	public static String getTransfer(String temp) {
		List<String> ftemp = new ArrayList<String>();
		temp = temp.replaceAll("\"","");
		String[] cc =  Pattern.compile(";").split(temp);
		List<String> templ = Arrays.asList(cc);
		for(int i=0;i<templ.size();i++){
			String ss="";
			for(int q = templ.get(i).length()-1;q>=0;q--){
				ss += templ.get(i).charAt(q);
			}
			ftemp.add(ss);
		}
		temp = "\"";
		for(int i =0;i<ftemp.size();i++)
			temp += ftemp.get(i)+";";
		temp+="\"";
		temp = temp.replaceAll(";\"", "\"");
		
		
		return temp;
	}






	public static String getMODCMap(List<String> mapmodl, List<String> maptable) {
		List<String> temp = new ArrayList<String>();

		String modtemp="\"";
		ListIterator<BEFORE> rb = gfile.getRelations().get(0).getBEFORE().listIterator();
		while(rb.hasNext()){
			BEFORE bt = rb.next();
			String ss="";
			String sid ="";
			String tt="";
			String tid ="";
			String atm="";
			ListIterator<Source> aa = bt.getSource().listIterator();
			while(aa.hasNext())
				atm = aa.next().getId();
			MODIFIER mod = mymodifier(atm);
			if(mod != null){
				List<String> ll = getModIndex(mod,maptable,mapmodl);
				//System.out.println("$$$$Source$$$$$");
				
				for(int i=0;i<ll.size();i++)
					ss+=ll.get(i)+" ";
				ss+="\"";
				ss=ss.replaceAll(" \"", "");
				 sid = getMindex(ss,mapmodl);
				//	System.out.println(sid+" "+ss);
				//System.out.println("###########");
				// modtemp += ll;
			}
			String atm2="";
			//ListIterator<Source> aa2 = bt.getSource().listIterator();
			ListIterator<Target> aa2 = bt.getTarget().listIterator();
			while(aa2.hasNext())
				atm2 = aa2.next().getId();
			MODIFIER mod2 = mymodifier(atm2);

			if(mod2!=null){
				List<String> ll2 = getModIndex(mod2,maptable,mapmodl);
				//System.out.println("$$$$target$$$$$");
				
				for(int i=0;i<ll2.size();i++)
					tt+=ll2.get(i)+" ";
				tt+="\"";
				tt=tt.replaceAll(" \"", "");
				 tid = getMindex(tt,mapmodl);
					//System.out.println(tid+" "+tt);
				// modtemp += ll2;
			}
			
			if(tid !="" &&tid!= null && sid!=""&&sid!=null){
				temp.add(sid+" "+tid);
			}
			//System.out.println("===========");
			
			
		}
		//System.out.println("===========");
		//System.out.println("===========");
		//System.out.println(modtemp);
		for(int i =0;i<temp.size();i++)
			modtemp += temp.get(i)+";";
		modtemp+="\"";
		modtemp = modtemp.replaceAll(";\"", "\"");
		
		//System.out.println(modtemp);
		
		return modtemp;
	}
	

	public static String getMODRMap(List<String> mapmodl, List<String> maptable) {
		// TODO Auto-generated method stub
		List<String> temp = new ArrayList<String>();

		String modtemp="\"";
		ListIterator<ENTAILS> rb = gfile.getRelations().get(0).getENTAILS().listIterator();
		while(rb.hasNext()){
			ENTAILS bt = rb.next();
			String ss="";
			String sid ="";
			String tt="";
			String tid ="";
			String atm="";
			ListIterator<Source> aa = bt.getSource().listIterator();
			while(aa.hasNext())
				atm = aa.next().getId();
			MODIFIER mod = mymodifier(atm);
			if(mod != null){
				List<String> ll = getModIndex(mod,maptable,mapmodl);
				//System.out.println("$$$$Source$$$$$");
				
				for(int i=0;i<ll.size();i++)
					ss+=ll.get(i)+" ";
				ss+="\"";
				ss=ss.replaceAll(" \"", "");
				 sid = getMindex(ss,mapmodl);
				//	System.out.println(sid+" "+ss);
				//System.out.println("###########");
				// modtemp += ll;
			}
			String atm2="";
			ListIterator<Source> aa2 = bt.getSource().listIterator();
			while(aa2.hasNext())
				atm2 = aa2.next().getId();
			MODIFIER mod2 = mymodifier(atm2);

			if(mod2!=null){
				List<String> ll2 = getModIndex(mod2,maptable,mapmodl);
				//System.out.println("$$$$target$$$$$");
				
				for(int i=0;i<ll2.size();i++)
					tt+=ll2.get(i)+" ";
				tt+="\"";
				tt=tt.replaceAll(" \"", "");
				 tid = getMindex(tt,mapmodl);
					//System.out.println(tid+" "+tt);
				// modtemp += ll2;
			}
			
			if(tid !="" &&tid!= null && sid!=""&&sid!=null){
				temp.add(sid+" "+tid);
			}
			//System.out.println("===========");
			
			
		}
		//System.out.println("===========");
		//System.out.println("===========");
		//System.out.println(modtemp);
		for(int i =0;i<temp.size();i++)
			modtemp += temp.get(i)+";";
		modtemp+="\"";
		modtemp = modtemp.replaceAll(";\"", "\"");
		
		//System.out.println("modr "+modtemp);
		
		return modtemp;
	}
	private static MODIFIER mymodifier(String id) {
		ListIterator<MODIFIER> ml = gfile.getMarkables().get(0).getMODIFIER().listIterator();
		while(ml.hasNext()){
			MODIFIER tm = ml.next();
			if(tm.getId().equals(id)){
				return tm;
			}
		}
		return null;
	}

	
	private static String getMindex(String ss, List<String> mapmodl) {
		for(int i=0;i<mapmodl.size();i++)
			if(mapmodl.get(i).equals(ss))
				return ""+i;
		return null;
	}

	private static List<String> getModIndex(MODIFIER mod, List<String> maptable, List<String> mapmodl) {
		List<String> temp = new ArrayList<String>();
		ListIterator<TokenAnchor> modl = mod.getTokenAnchor().listIterator();
		while(modl.hasNext()){
			TokenAnchor modtm = modl.next();
			String a ="";
			for(int i =0;i<maptable.size();i++){
				
				//System.out.println(modtm.getId()+" "+maptable.get(i)+" "+i);
				if(modtm.getId().equals(maptable.get(i))){
					a+=i;
				}
			}
			if(a!=""){
			temp.add(a);
			}
			
		}
				
		return temp;
	}
	
	public static List<String> getmapmodl(String mODL) {
		// TODO Auto-generated method stub
		//List<String> temp = new ArrayList<String>();

		mODL = mODL.replaceAll("\"", "");
		String[] cc =  Pattern.compile(";").split(mODL);
		List<String> temp = Arrays.asList(cc);
		return temp;
	}

	public static List<String> getmaptable(FRAGMENT tempFrag) {
		// TODO Auto-generated method stub
		List<String> temp = new ArrayList<String>();
 
		ListIterator<TokenAnchor> aa = tempFrag.getTokenAnchor().listIterator();
		while(aa.hasNext()){
			TokenAnchor te = aa.next();
			temp.add(te.getId());
			}
		return temp;
	}
	private static String getMODR(String mODL, List<String> tempF) {
		// TODO Auto-generated method stub
		String MODR="";
		MODR ="\"";
		mODL = mODL.replaceAll("\"", "");
		String[] cc =  Pattern.compile(";").split(mODL);
		List<String> listc = Arrays.asList(cc);
		

        Set<String> set = new HashSet<String>(modifiers);
        String[] result = new String[set.size()];
        set.toArray(result);
        if(result.length>1){
        for(int p =0;p<result.length;p++){
        	if(isaEntailsR(result[p])){
        	ListIterator<MODIFIER> ml = gfile.getMarkables().get(0).getMODIFIER().listIterator();
        	while(ml.hasNext()){
        		MODIFIER mltemp = ml.next();
        		if(mltemp.getId().equals(result[p])){
        			ListIterator<TokenAnchor> tl = mltemp.getTokenAnchor().listIterator();
        			String temp="";
        			while(tl.hasNext()){
        				TokenAnchor tltemp = tl.next();
        				//temp+=getIndex(getTokenString(tltemp.getId()),tempF)+" ";
        				temp+=getIndexmap(tltemp.getId(), tempF)+" ";

        				
        			}
        			temp+="\"";
        			temp=temp.replaceAll(" \"", "");
        			//System.out.println(temp);
        			for(int e=0;e<listc.size();e++){
        				if(listc.get(e).equals(temp)){
        					MODR+=e+" ";
        				}
        			}
        		}
        	}
        	
			System.out.println(result[p]);
        	}
        }
        }else
        	MODR = "\"";
        
        MODR+="\"";
        MODR = MODR.replaceAll(" \"", "\"");
		return MODR;
	}
	private static boolean isaEntailsR(String id) {
		ListIterator<ENTAILS> rl = gfile.getRelations().get(0).getENTAILS().listIterator();
		while(rl.hasNext()){
			ENTAILS rltemp = rl.next();
			if(rltemp.getSource().get(0).getId().equals(id)||rltemp.getTarget().get(0).getId().equals(id)){
				return true;
			}
		}
		return false;
	}
	private static String getMODC(String mODL, List<String> tempF) {
		String MODC="";
		MODC ="\"";
		mODL = mODL.replaceAll("\"", "");
		String[] cc =  Pattern.compile(";").split(mODL);
		List<String> listc = Arrays.asList(cc);
		
        Set<String> set = new HashSet<String>(modifiers);
        String[] result = new String[set.size()];
        set.toArray(result);
        if(result.length>1){
        for(int p =0;p<result.length;p++){
        	BEFORE beforeR = isaBeforeR(result[p]);
        	if(beforeR!= null){
        	ListIterator<MODIFIER> ml = gfile.getMarkables().get(0).getMODIFIER().listIterator();
        	while(ml.hasNext()){
        		MODIFIER mltemp = ml.next();
        		if(mltemp.getId().equals(beforeR.getSource().get(0).getId())){
        			ListIterator<TokenAnchor> tl = mltemp.getTokenAnchor().listIterator();
        			String temp="";
        			while(tl.hasNext()){
        				TokenAnchor tltemp = tl.next();
        				//temp+=getIndex(getTokenString(tltemp.getId()),tempF)+" ";
        				temp+=getIndexmap(tltemp.getId(), tempF)+" ";

        				
        			}
        			temp+="\"";
        			temp=temp.replaceAll(" \"", "");
        			//System.out.println(temp);
        			for(int e=0;e<listc.size();e++){
        				if(listc.get(e).equals(temp)){
        					MODC+=e+" ";
        				}
        			}
        			
        		}
        		
        		if(mltemp.getId().equals(beforeR.getTarget().get(0).getId())){
        			ListIterator<TokenAnchor> tl = mltemp.getTokenAnchor().listIterator();
        			String temp="";
        			while(tl.hasNext()){
        				TokenAnchor tltemp = tl.next();
        				//temp+=getIndex(getTokenString(tltemp.getId()),tempF)+" ";
        				temp+=getIndexmap(tltemp.getId(), tempF)+" ";

        				
        			}
        			temp+="\"";
        			temp=temp.replaceAll(" \"", "");
        			//System.out.println(temp);
        			for(int e=0;e<listc.size();e++){
        				if(listc.get(e).equals(temp)){
        					MODC+=e+" ";
        				}
        			}
        			
        		}
        		
        	}
        	MODC+=";";
			//System.out.println(result[p]);
        	}
        }
        }else
        	MODC = "\"";
        
        MODC+="\"";
        MODC = MODC.replaceAll(" \"", "\"");
		return MODC;
	}
	private static BEFORE isaBeforeR(String id) {
		// TODO Auto-generated method stub
		ListIterator<BEFORE> rl = gfile.getRelations().get(0).getBEFORE().listIterator();
		while(rl.hasNext()){
			BEFORE rltemp = rl.next();
			if(rltemp.getSource().get(0).getId().equals(id)||rltemp.getTarget().get(0).getId().equals(id)){
				return rltemp;
			}
		}
		return null;
	}
	public static String getMODL(List<TokenAnchor> fanchor, List<String> tempF) {
		// TODO Auto-generated method stub
		String MODL="";
		MODL = "\"";
		ListIterator<TokenAnchor> ff = fanchor.listIterator();
		while(ff.hasNext()){
			TokenAnchor tempff = ff.next();
			if(isaModifier(tempff.getId())){
				MODIFIER m = getModifiers(tempff.getId());
				MODL+=getAnchors(m,tempF);
			}
		}
		
		MODL += "\"";
		MODL = MODL.replace(";\"", "\"");
		
		MODL= MODL.replaceAll("\"", "");
		String[] cc =  Pattern.compile(";").split(MODL);
		MODL="\"";
		List<String> listc = Arrays.asList(cc);
        Set<String> set = new HashSet<String>(listc);
        String[] result = new String[set.size()];
        set.toArray(result);
        for (String s : result) {
        	MODL+=s+";";
            //System.out.print(s + ", ");
        }
        MODL+="\"";
        MODL= MODL.replaceAll(";\"", "\"");
		
		
		
		return MODL;
	}
	private static String getAnchors(MODIFIER m, List<String> tempF) {
		// TODO Auto-generated method stub
		String anames="";
		ListIterator<TokenAnchor> la = m.getTokenAnchor().listIterator();
		while(la.hasNext()){
			TokenAnchor lat = la.next();
			//anames+= getIndex(getTokenString(lat.getId()),tempF)+" ";
			anames+= getIndexmap(lat.getId(), tempF)+" ";

			
		}
		anames+=";";
		anames= anames.replaceAll(" ;", ";");
		return anames;
	}
	private static MODIFIER getModifiers(String id) {
		// TODO Auto-generated method stub
		ListIterator<MODIFIER> ml = gfile.getMarkables().get(0).getMODIFIER().listIterator();
		while(ml.hasNext()){
			MODIFIER tempml = ml.next();
		ListIterator<TokenAnchor> lmlanchors = tempml.getTokenAnchor().listIterator();
		while(lmlanchors.hasNext()){
			TokenAnchor tlmlanchors = lmlanchors.next();
			if(tlmlanchors.getId().equals(id)){
				modifiers.add(tempml.getId());
				return tempml;
			}
		}
		}
		return null;
		
	}
	public static String getBP(List<TokenAnchor> tokenAnchor,
			List<String> tempF) {
		// TODO Auto-generated method stub
		String BP="";
		BP ="\"";
		ListIterator<TokenAnchor> tl = tokenAnchor.listIterator();
		while(tl.hasNext()){
			TokenAnchor temptl = tl.next();
			if(!isaModifier(temptl.getId())){
				//BP+= getIndex(getTokenString(temptl.getId()), tempF)+" ";
				BP+= getIndexmap(temptl.getId(), tempF)+" ";
			}
		}
		BP+="\"";
		BP = BP.replaceAll(" \"", "\"");
		return BP;
	}
	private static String getIndexmap(String id, List<String> tempF) {
		// TODO Auto-generated method stub
		for(int i =0; i < tempF.size();i++){
		//System.out.println(i+"  "+tempF.get(i));
			if(tempF.get(i).equals(id)){
				return ""+i;
			}
		}
		return null;
	}
	private static boolean isaModifier(String id) {
		// TODO Auto-generated method stub
		ListIterator<MODIFIER> lm = gfile.getMarkables().get(0).getMODIFIER().listIterator();
		while(lm.hasNext()){
			MODIFIER templm = lm.next();
			ListIterator<TokenAnchor> mal = templm.getTokenAnchor().listIterator();
			while(mal.hasNext()){
				TokenAnchor tmal = mal.next();
				if(tmal.getId().equals(id))
					return true;
			}
		}
		return false;
	}
	public static String getFC(List<TokenAnchor> ftokenAnchor, List<String> tempF) {
		// TODO Auto-generated method stub
		String tFC="\"";
		Boolean found = false;
		ListIterator<TokenAnchor> ft = ftokenAnchor.listIterator();
		while(ft.hasNext()){
			TokenAnchor tempft = ft.next();
			System.out.println(tempft.getId()+" "+isaFocus(tempft.getId()));
		if(isaFocus(tempft.getId())){
			String fname = getTokenString(tempft.getId());
			//String index = getIndex(fname,tempF);
			String index =  getIndexmap(tempft.getId(), tempF);
			found = true;
			tFC +=index+" "+fname+";";
			//return tFC;
		}
		}
		tFC +="\"";
		tFC = tFC.replaceAll(";\"", "\"");
		if(!found)
			tFC="\"-1\"";
			
		return tFC;
	}
	private static String getIndex(String fname, List<String> tempF) {
		// TODO Auto-generated method stub
		for(int i =0;i<tempF.size();i++){
			if(tempF.get(i).equals(fname)){
				return ""+i;
			}
		}
		return "-2";
	}
	private static boolean isaFocus(String id) {
		// TODO Auto-generated method stub
		ListIterator<FOCUS> foucs = gfile.getMarkables().get(0).getFOCUS().listIterator();
		while(foucs.hasNext()){
			FOCUS tempf = foucs.next();
			ListIterator<TokenAnchor> tl = tempf.getTokenAnchor().listIterator();
			while(tl.hasNext()){
				TokenAnchor tem = tl.next();
			if(tem.getId().equals(id))
				return true;
			}
		}
		return false;
	}
	public static String repareF(FRAGMENT tempFrag, List<String> tempF) {
		// TODO Auto-generated method stub
		String outF;
		if(tempF == null)
			outF="\"\"";
		else{
			outF="\"";
			for(int i=0;i<tempF.size();i++)
				outF+=tempF.get(i)+" ";
			outF+="\"";
			outF= outF.replaceAll(" \"", "\"");
		}
		return outF;
	}
	public static List<String> getF(List<TokenAnchor> fragAnchorl) {
		// TODO Auto-generated method stub
		List<String> temp = new ArrayList<String>();

		ListIterator<TokenAnchor> al = fragAnchorl.listIterator();
		int i = 0;
		while(al.hasNext()){
			TokenAnchor tempal = al.next();
			//System.out.println(getTokenString(tempal.getId(),tokenl));
			temp.add(getTokenString(tempal.getId()));
			
			i++;
		}
		
		
		
		return temp;
	}
	
	public static String getTokenString(String id){
		List<Token> tokenl = gfile.getToken();
		String tname = "";
		ListIterator<Token> tl = tokenl.listIterator();
		while(tl.hasNext()){
			Token temptl = tl.next();
			if(temptl.getId().equals(id))
				return temptl.value;
		}
		
		
		return tname;
	}
	

}
