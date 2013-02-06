package decomposition.buildgraph;
import java.util.Hashtable;
import java.lang.Object;
import java.io.*;

public class buildCieg {
	protected static String indexF;
	protected static String wdF;
	protected static String[] Fl;
	protected static String idFC;
	protected static String wdFC;
	protected static String BP;
	protected static String[] idModL;
	protected static String[] wdModL;
	protected static String[] ModC;
	protected static String[] ModR;
	protected static Hashtable Nodes = new Hashtable();
	protected static Hashtable Dparents = new Hashtable();
	protected static Hashtable Ddaughters = new Hashtable();
	protected static Hashtable Aparents = new Hashtable();
	protected static Hashtable Adaughters = new Hashtable();
	protected static String root = "";
    protected static String top = "";
    protected static Hashtable levelStates = new Hashtable();
    protected static Hashtable state2node = new Hashtable();
    protected static Hashtable Nlbl = new Hashtable();
    protected static Hashtable Nmod = new Hashtable();
    protected static int numberN = 0;
	protected static String path = "";
	
	public static String run(String dirName)
	{
		File dir = new File(dirName);
		String outputFile = "";
		String[] children = dir.list();
    	for (int i=0; i<children.length; i++) {
    		try{
    		if (children[i].indexOf(".txt") == -1)
    		{
    			continue;
    		}
    		System.out.println("Processing "+children[i]);
			//path = args[0];
			
			//KATHRIN
			path = dirName;
			
        	String filename = path+children[i];
        	
        	System.out.println("filename: " + filename);
			
        	FileInputStream fstream = new FileInputStream(filename);
  			DataInputStream in = new DataInputStream(fstream);
  			BufferedReader br = new BufferedReader(new InputStreamReader(in,"UTF-8"));
  			
        	String strLine = br.readLine();
        	
        	//KATHRIN
        	System.out.println("strLine: " + strLine);
        	
        	String[] argsL= strLine.split("\" \"");
        	argsL[0] = argsL[0].replace("\"","");
        	argsL[argsL.length-1] = argsL[argsL.length-1].replace("\"","");
        	 Nodes = new Hashtable();
        	 Dparents = new Hashtable();
        	 Ddaughters = new Hashtable();
        	 Aparents = new Hashtable();
        	 Adaughters = new Hashtable();
        	 root = "";
        	 top = "";
        	 levelStates = new Hashtable();
        	 state2node = new Hashtable();
        	 Nlbl = new Hashtable();
        	 Nmod = new Hashtable();
        	 numberN = 0;
        	outputFile = mainL(argsL, strLine); 
        	in.close();
        	} catch (Exception e){}
        }	
    	return outputFile;
    }
    
    public static String mainL(String[] args, String StrLine) {
    	String outputFile = "";
    	System.out.println(StrLine);
        indexF = args[0];
        wdF = args[1];
        Fl = wdF.split(" ");
        
        String[] tmp = args[2].split(" ");
        idFC = tmp[0];
        if (idFC.equals("-1"))
        {
        	wdFC = "NOFOCUS";
        }
        else
        {
        	wdFC=(String) Fl[Integer.parseInt(idFC)];
        	String[] tmpp = args[2].split(";");
        	for (int ipp = 1; ipp < tmpp.length; ipp++)
        	{
        		System.out.println(ipp + " are "+tmpp[ipp]);
        		String[] iitpp = tmpp[ipp].split(" ");
        		wdFC= wdFC+" "+Fl[Integer.parseInt(iitpp[0])];
        		System.out.println(wdFC);
        	}
        }
        BP = args[3];
        tmp = args[4].split(";");
        idModL = tmp;
        tmp = args[4].split(";");
        wdModL = tmp;
        ModC = args[5].split(";");
        ModR = args[6].split(";");
        
        if (args[4].equals(""))
        {
          String content = "";
		  content = 	"<document>\n"+"<input>\n"+"<id id=\""+indexF+"\"></id>\n"+"<fragment F=\""+wdF+"\"></fragment>\n"+
						"<focus FCindex=\""+idFC+"\" FC=\""+wdFC+"\"></focus>\n"+"<basePredicate BP=\""+BP+"\"></basePredicate>\n"+
					"<modl modList=\"" + "\"></modl>\n<modc before=\""+args[5]+"\"></modc>\n";
		  content = 	content + "<modr ModR=\""+args[6]+"\"></modr>\n";
	      content = content +"<numberNodes nr=\""+1+"\"></numberNodes>\n";
		  content = content + "</input>\n";	
		  content = content + "<node id=\""+indexF+"_0\">\n";
		  content = content + "<label lbl=\""+wdF+"\"></label>\n";
		  content = content + "<modifiers mod=\"\"></modifiers>\n";
		  content = content + "<parents direct=\"\" closure=\"\"></parents>\n";	
		  content = content + "<daughters direct=\"\" closure=\"\"></daughters>\n";
		  content = content + "</node>\n</document>";
		  //System.out.println(content);
		  outputFile = printGraf(content);
		  System.out.println("ENDDDDDDDDDDD!\n\n");
		}
		else
		{
			for (int i = 0; i <  idModL.length; i++)
	        {
	        	tmp = idModL[i].split(" ");
	        	wdModL[i] = Fl[Integer.parseInt(tmp[0])];
	        	for (int j = 1; j < tmp.length ; j++)
	        	{
	        		wdModL[i] = wdModL[i]+" "+Fl[Integer.parseInt(tmp[j])];
	        	}
	        }
			String Rcc = checkforCycle();
	        System.out.println(Rcc);
	        if (Rcc.indexOf("<cycle") !=-1)
	        {
	        	outputFile = printGraf(Rcc);	
	        }
			else
			{
				generateGraf();
				outputFile = printGraf("ok");
			}
	        System.out.println("ENDDDDDDDDDDD!\n\n");
        }
        return outputFile;
    }
    
    public static void generateGraf()
    {
        String activeState = "";
        String daughterState = "";
        String topState = "";
        int activeLevel = 0;
        int nextLevel= 0;
        int localID = 0;
        String activeNode = "";
        String daughterNode = "";
        
        int card = idModL.length - 1;
        for (int i=0; i <= card; i++)
        {
        	 activeState=activeState+"<"+i+"_1>";
        	 root = root+"<"+i+"_1>";
        	 top = top +"<"+i+"_0>";
        }
        activeNode= indexF+"_"+Integer.toString(localID++);
        System.out.println("Node="+activeNode+"   "+activeState);
        Nodes.put(activeNode, activeState);
        state2node.put(activeState, activeNode);
        levelStates.put(activeLevel, activeNode);
        activeLevel = 0;
        
        while (activeLevel <= card)
        {
        	nextLevel = activeLevel  + 1;
        	String states = (String) levelStates.get(activeLevel);
        	System.out.println("level "+ activeLevel+" states " + states);
        	String[] statesL = states.split(";");
        	for (int i = 0; i < statesL.length;i++)
        	{
        		activeState = (String) Nodes.get(statesL[i]);
        		activeNode = (String) statesL[i];
        		//System.out.println(statesL[i]+" activeState "+ activeState);
 				for (int j = 0; j <= card; j++)
 				{
 					String curI = "<"+j+ "_1>";
 					String curS = "<"+j+"_0>";
 					if (activeState.indexOf(curI) == -1)
 					{
 						continue;
 					}
 					daughterState = new String(activeState);
 					daughterState=daughterState.replace(curI,curS);
 					//System.out.print("daughter = "+daughterState);
 					if (!checkC(daughterState))
 					{
 						continue;
 					}
 					if (state2node.containsKey(daughterState))
 					{
 						daughterNode = (String) state2node.get(daughterState);
 						//System.out.print(" already seen" + daughterNode);
 					}
 					else
 					{
 						daughterNode = indexF+"_"+Integer.toString(localID++);
 						Nodes.put(daughterNode, daughterState);
 						state2node.put(daughterState,daughterNode);
 						//System.out.print(" new Node" + daughterNode);
 						if (levelStates.containsKey(nextLevel))
 						{ 
 							levelStates.put(nextLevel, (String) levelStates.get(nextLevel)+";"+daughterNode);
 						}
 						else
 						{
 							levelStates.put(nextLevel, daughterNode);
 						}
 					}
 					//System.out.println(" next level" + nextLevel + " " + (String)levelStates.get(nextLevel));
 					
 					String parent = activeNode;
 					if (Dparents.containsKey(daughterNode))
 					{
 						parent = parent + ";"+(String) Dparents.get(daughterNode);
 					}
 					Dparents.put(daughterNode, parent);
 					System.out.print("Node="+daughterNode+"   "+daughterState+ " parents="+parent);
 					String daughter = daughterNode;
 					if (Ddaughters.containsKey(activeNode))
 					{
 						daughter= daughterNode + ";" + Ddaughters.get(activeNode);
 					}
 					Ddaughters.put(activeNode, daughter);
 					System.out.println("  Node="+activeNode+"   "+activeState+" daughters="+daughter);
 				}
 			}  
 			activeLevel++;
 		}
 		// here I treat the implication;
 		if (!ModR[0].equals(""))
 		{
 			resolveModEntailment(activeLevel);
 		}
 		System.out.println("\nImplications  CHECKED!!\n");
 		addAllInfo(activeLevel, localID);    	
    }
    
    public static boolean checkC(String daughterNode)
    {
    	//System.out.print("Condition on: "+daughterNode);
    	boolean ret = true;
    	for (int i=0; i < ModC.length; i++)
    	{
    		 if (ModC[i].equals(""))
    		 {
    		 	continue;
    		 }
    		 String bf[] = ModC[i].split(" ");
    		 //System.out.print("if poz "+ bf[1] + " is 1 then poz " + bf[0] + " must be 1");
    		 String c1 = "<"+bf[1]+"_1>";
    		 String c2 = "<"+bf[0]+"_0>";
    		 //System.out.print("  "+dacorpus preuspostional patternsughterNode.indexOf(c1)+"   "+daughterNode.indexOf(c2)); 
    		 if (daughterNode.indexOf(c1) != -1 && daughterNode.indexOf(c2) != -1)
    		 {
    		 		ret = false;
    		 		//System.out.print("    BREACH!");
    		 		break;
    		 }
    		 //System.out.println("");
    	}
    	//System.out.println("");
    	return ret;   
    }
    
 	
 	public static void addAllInfo(int maxL, int maxN)
 	{
 		String rootID = (String) state2node.get(root);
 		Aparents.put(rootID, "");
 		Dparents.put(rootID, "");
 		Nlbl.put(rootID, wdF);
 		// original, String ss = wdModL[0];
		// adding pozition info;
		String ss = wdModL[0]+"poz="+idModL[0];
 		for (int i=1; i < wdModL.length; i++)
 		{
 			//original, ss = ss+";"+wdModL[i];
			// adding pozition info;
			ss = ss +";"+wdModL[i]+"poz="+idModL[i];
 		}
 		Nmod.put(rootID, ss);
 		System.out.println("rootID="+rootID);
 		System.out.println("L="+(String) Nlbl.get(rootID));
 		System.out.println("M="+(String) Nmod.get(rootID));
 		
 		String activeNode = "";
 		for (int l=1; l <= maxL; l++)
 		{
 			String states = (String) levelStates.get(l);
        	System.out.println("\n\nlevel "+ l +" states " + states);
        	String[] statesL = states.split(";");
        	boolean fl_allOk = false;
        	while (! fl_allOk)
        	{
        		fl_allOk = true;
        		for (int i = 0; i < statesL.length;i++)
        		{
        			activeNode = (String) statesL[i];
        			String pc = (String) Dparents.get(activeNode);
        			String[] pl= pc.split(";");
        			String mask = (String) Nodes.get(activeNode);
        			System.out.println("activeNode="+activeNode+"  mask="+mask);
        			System.out.println("parents="+pc);
        			boolean fl_ALLP = true;
        			for (int ip = 0; ip < pl.length;ip++)
        			{
        				System.out.println("Checking AP for parent "+pl[ip]);
        				if (!Aparents.containsKey(pl[ip]))
        				{
        					System.out.println("NO AParents for " + pl[ip]);
        					fl_ALLP = false;
        					fl_allOk = false;
        					break;
        				}
        				else
        				{
        						System.out.println(pl[ip]+ " has parents "+(String) Aparents.get(pl[ip]));
        				}
        			}
        			if (!fl_ALLP)
        			{
        				continue;
        			}
        			String M = "";
        			String Lmask = "";
        			for (int j = 0; j < idModL.length; j++)
        			{
        				String c = "<"+j+"_1>";
        				if (mask.indexOf(c) != -1)
        				{
        					// original, M =  M + wdModL[j] + ";";
						// adding pozition info;
						M = M + wdModL[j] +"poz="+idModL[j]+";";
						//
        				}
        				else
        				{
        					String[] idml = idModL[j].split(" ");
        					for (int k=0; k < idml.length; k++)
        					{
        						Lmask = Lmask+"<"+idml[k]+">";
        					}
        				}
        			}
        			if (M.equals(""))
        			{
        				//System.out.println("It should be the top of the graph - no modifiers");
        			}
        			else
        			{
        				M = M.substring(0, M.lastIndexOf(";"));
        			}
        			System.out.println("M="+M);
        			Nmod.put(activeNode, M);
        			String L = "";
        			for (int k=0; k < Fl.length;k++)
        			{
        				String c1 = "<"+k+">";
        				if (Lmask.indexOf(c1) == -1)
        				{
        					L = L +Fl[k]+" ";
        				}
        			}
        			L = L.substring(0, L.lastIndexOf(" "));
        			System.out.println("L="+L);
        			Nlbl.put(activeNode,new String(L));
        		 
        			String parents = (String) Dparents.get(activeNode);
        			System.out.println("Dparents="+parents);
        			String[] parentsL = parents.split(";");
        			String AP =  "";
        			for (int j=0; j < parentsL.length; j++)
        			{
        				String P = parentsL[j];
        				AP = addExclusive(P, AP);
        				System.out.println("After adding the parent P="+P+"   "+"AP="+AP);
        				String APP = (String) Aparents.get(P);
        				System.out.println("I am going to add the Aparent of P="+P+"   "+"AP="+APP);
        				AP = addExclusive(AP, APP);
        				//System.out.println("After adding the Aparent="+APP+" of P="+P+"   "+"AP="+AP);
        			}
        			Aparents.put(activeNode, (String)AP);
        			System.out.println("Finally the Aparent of activeNode="+activeNode+" is "+"AP="+AP+"\n");
        		}
        	}
 		}
 		//System.exit(0);
 		numberN = maxN;
 		for (int i=1; i< maxN; i++)
 		{
 			activeNode= indexF+"_"+Integer.toString(i);
 			String AP = (String) Aparents.get(activeNode);
 			System.out.println("\nactiveNode="+activeNode+" state="+(String) Nodes.get(activeNode)+" AP="+AP);
 			String[] APl = AP.split(";");
 			for (int j=0; j < APl.length; j++)
 			{
 				String P = APl[j];
 				String DP = "";
 				if (Adaughters.containsKey(P))
 				{
 					DP = (String) Adaughters.get(P);
 				}
 				else
 				{
 					DP = "";
 				}
 				DP = addExclusive(activeNode, DP);
 				Adaughters.put(P, DP);
 				System.out.println("Finally the Adaughters of P="+P+" is "+"DP="+DP);
 			}
 		}
 	}
 		
 	public static String addExclusive(String s1, String s2)
 	{
 		if (s1.equals(""))
 		{
 			return s2;
 		}
 		if (s2.equals(""))
 		{
 			return s1;
 		}
 		s1 = s1 + ";";
 		String s = s1.concat(s2);
 		String[] sL =  s.split(";");
 		Hashtable v = new Hashtable();
 		v.put (sL[0], 1);
 		s = sL[0];
 		for (int i=1; i < sL.length; i++)
 		{
 			if (v.containsKey(sL[i]))
 			{
 				continue;
 			}
 			s = s+";"+sL[i];
 			v.put(sL[i],1);
 		}
 		return s;
 	}
 	
 	public static String printGraf(String in)
 	{
		String nf = path + indexF+".xml.graph";
 		try {
			//File file = new File(indexF+".xml");
			//if (!file.exists()) {
				//file.createNewFile();
			//}
	
			FileWriter fw = new FileWriter(nf);
		//	BufferedWriter bw = new BufferedWriter(fw);
		
			new File(nf).createNewFile();
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(nf),"UTF-8"));	
			
			String content = "";
			content = 	"<document>\n"+"<input>\n"+"<id id=\""+indexF+"\"></id>\n"+"<fragment F=\""+wdF+"\"></fragment>\n"+
						"<focus FCindex=\""+idFC+"\" FC=\""+wdFC+"\"></focus>\n"+"<basePredicate BP=\""+BP+"\"></basePredicate>\n"+
						"<modl modList=\"";
			//System.out.println("In graf wdMod="+wdModL.length+"  modr="+ ModR.length + "  modc"+ModC.length);
			for (int i=0; i < wdModL.length -1;i++)
			{
				content = content + wdModL[i]+";";
			}
			content = 	content + wdModL[wdModL.length-1]+"\"></modl>\n<modc before=\"";
			for (int i=0; i < ModC.length -1;i++)
			{
				content = content + ModC[i]+";";
			}
			content = 	content + ModC[ModC.length-1]+"\"></modc>\n";
			content = 	content +"<modr entailment=\"";
			for (int i=0; i < ModR.length -1;i++)
			{
				content = content + ModR[i]+";";
			}
			content = 	content + ModR[ModR.length-1]+"\"></modr>\n";
			//if (numberN == 0)
			//{
				//content = content + "<numberNodes nr=\"1\"></numberNodes>\n";
			//}
			//else
			//{
				content = content +"<numberNodes nr=\""+numberN+"\"></numberNodes>\n";
			//}
			content = content + "</input>\n";
			//System.out.println(content);
			if (in.indexOf("<node") == -1)
			{
				
				bw.write(content);
			}
			if (in.indexOf("<cycle") != -1 || in.indexOf("<node") != -1)
			{
				bw.write(in);
				bw.close();
				return nf;
			}
			content = "";
			for (int i=0; i < numberN;i++)
			{
				String activeNode = indexF+"_"+Integer.toString(i);
				content = content+"<node id=\""+activeNode+"\">\n";
				content = content+"<label lbl=\""+(String) Nlbl.get(activeNode)+"\"></label>\n";
				//System.out.println("in file active node=" +activeNode+ " lbl="+(String) Nlbl.get(activeNode)+"****");
				content = content+"<modifiers mod=\""+(String) Nmod.get(activeNode)+"\"></modifiers>\n";
				
				content = content+"<parents direct=\""+(String) Dparents.get(activeNode)+"\" closure=\""+(String) Aparents.get(activeNode)+"\"></parents>\n";
				content = content+"<daughters direct=\""+(String) Ddaughters.get(activeNode)+"\" closure=\""+(String) Adaughters.get(activeNode)+"\"></daughters>\n";
				content = content+"</node>\n";
			}
			
			bw.write(content);
			content = "";
			for (int i=1; i < numberN; i++)
			{
				String activeNode = indexF+"_"+Integer.toString(i);
				String parents = (String) Dparents.get(activeNode);
				String[] pl = parents.split(";");
				for (int j=0; j < pl.length; j++)
				{
					content = content + "<edge source=\""+pl[j]+"\" target=\""+activeNode+"\" type=\"direct\"></edge>\n";
				}
				parents = (String) Aparents.get(activeNode);
				pl = parents.split(";");
				for (int j=0; j < pl.length; j++)
				{
					content = content + "<edge source=\""+pl[j]+"\" target=\""+activeNode+"\" type=\"clousure\"></edge>\n";
				}
			}
			content = content+"</document>\n";
			bw.write(content);
			bw.close();
			//System.exit(0);
 
		} catch (IOException e) {
			e.printStackTrace();
		}
 		return nf;
 	}
 	
 	public static String checkforCycle()
 	{
 		Hashtable ep = new Hashtable();
 		String Rcc = "No Cycle";
 		for (int i=0; i < ModR.length; i++)
 		{
 			if (ModR[i].equals(""))
 			{
 				continue;
 			}
 			//System.out.println(ModR[i]+"lkjkjk");
 		 	String[] sp=ModR[i].split(" ");
 		 	String pm = "<"+sp[1]+">";
 		 	String pp = "";
 		 	//System.out.println(sp[0]+" ent "+sp[1]);
 			if (ep.containsKey(sp[0]))
 			{
 				pp = (String) ep.get(sp[0]);
 				if (pp.indexOf(pm) != -1)
 				{
 					pp = pp.replaceAll("><",";");
 					pp = pp.replaceAll("<","");
 					pp = pp.replaceAll(">","");
 					String[] up = pp.split(";"); 
 					Rcc = "<cycle chain=\"";
 					for (int j=0; j < up.length; j++)
 					{
 						Rcc= Rcc + wdModL[Integer.parseInt(up[j])]+ " ENTAILS ";
 					}
 					Rcc = Rcc +wdModL[Integer.parseInt(sp[0])]+" ENTAILS "+wdModL[Integer.parseInt(sp[1])]+"\"></cycle>\n</document>";
 					break;
 				}
 				else
 				{
 					ep.put(sp[1], pp+"<"+sp[0]+">");
 				}
 			}
 			else
 			{
 				ep.put(sp[1], "<"+sp[0]+">");
 			}
 		}
 		//System.out.println(Rcc);
 		return Rcc;
 	}
 	
 	public static void resolveModEntailment(int maxL)
 	{
 		String[] st = new String[ModR.length];
 		String[] dr = new String[ModR.length];
 	
 		for (int i=0; i < ModR.length; i++)
 		{
 			//System.out.println(ModR[i]);
 		 	String[] sp=ModR[i].split(" ");
 		 	st[i] = (String) sp[0];
 		 	dr[i] = (String) sp[1];
		}
			//System.out.println("k="+k+" key="+keys[k]+" value="+(String) ep.get(keys[k]));
		
		for (int l=0; l <= maxL; l++)
        {
            String states = (String) levelStates.get(l);
        	System.out.println("\n\ncheck implication on level "+ l +" states " + states);
        	String[] statesL = states.split(";");
        	for (int s=0; s < statesL.length;s++)
        	{
        		String activeNode = (String) statesL[s];
        		String mask = (String) Nodes.get(activeNode);
        		//System.out.println("activeNode="+activeNode+" mask="+mask);
            	for (int r=0; r < st.length; r++)
            	{
            	 	String rc_st = "<"+st[r]+"_1>";
            	 	String rc_dr = "<"+dr[r]+"_1>";
            	 	if (mask.indexOf(rc_st) == -1 || mask.indexOf(rc_dr) != -1)
            	 	{
            	 		continue;
            	 	}
            	 	System.out.println("\nThis active sister,"+activeNode+", "+mask +", is affected by the implication "+r+" which is exactly  st="+st[r]+" dr="+(String)dr[r]);
            	 	//accept only the states which are sisters and have dr[r] 1;
            	 	String is = "";
            	 	for (int i = 0; i < idModL.length;i++)
            	 	{
            	 		if (i == Integer.parseInt(st[r]))
            	 		{
            	 			is = is+"<"+Integer.toString(i)+"_0>";
            	 			continue;
            	 		}
            	 		if (i == Integer.parseInt(dr[r]))
            	 		{
            	 			is = is + "<"+Integer.toString(i)+"_1>";
            	 			continue;
            	 		}
           				String isi = "<"+Integer.toString(i)+"_1>";
           				if (mask.indexOf(isi) == -1)
            	 		{
            	 			is = is + "<"+Integer.toString(i)+"_0>";
            	 		}
            	 		else
            	 		{
            	 			is = is +"<"+Integer.toString(i)+"_1>";
            	 		}
            	 	}
            	 	String isNode = (String) state2node.get(is);
            	 	System.out.println("This implied sister,"+isNode+", "+is +", is affected by the implication "+r+" which is exactly  st="+st[r]+" dr="+(String)dr[r]);
            	 	String parents = (String) Dparents.get(isNode);
            	 	System.out.println("parentsI "+isNode+"="+parents);
            	 	parents = addExclusive(activeNode, parents);
            	 	System.out.println("parentsD "+ isNode+"="+parents);
            	 	Dparents.put(isNode, parents);
            	 	String daughters = (String) Ddaughters.get(activeNode);
            	 	System.out.println("daughtersI "+activeNode+"="+daughters);
            	 	daughters = addExclusive(isNode,daughters);
            	 	System.out.println("daughtersD "+activeNode+"="+daughters);
            	 	Ddaughters.put(activeNode, daughters);
            	}
            }
           }
          }

}
