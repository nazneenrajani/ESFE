package edu.utaustin.ensemble;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.*;

/**
 * This class has implementation of feature extraction for Ensembling ESF systems. 
 * The features include ESF output, relation type, provenance features (Jaccard and document score).
 */

public class FeatureExtractor {
	int numSystems;
	String[] REOutputs;
	static Set<String>[] extractions;
	static Map<String,String>[] outputs;
	static Map<String,List<Double>> fextractions_confs;
	static Map<String,Integer> fextractions_target;
	static Map<String,Map<String,Integer>> doc_count;
	static Map<String,Map<String,Double>> prov_count;
	static Map<String,Map<String,List<Integer>>> off_count;
	static Map<String,Double> sys_count;
//	static Map<String,Integer> query_lines;
	static Map<String,String> query_lines;
	static Map<String,String> fextractions_output;
	Scorer2013[] scorers_2013;
	Scorer2014[] scorers_2014;
	static List<String> singleValuedSlots;
	static List<String> listValuedSlots;

	public FeatureExtractor(int nsys){
		numSystems = nsys;
		REOutputs = new String[numSystems];
		fextractions_confs = new HashMap<String,List<Double>>();
		scorers_2013 = new Scorer2013[numSystems];
		for(int i=0;i<numSystems;i++)
			scorers_2013[i] = new Scorer2013();
		scorers_2014 = new Scorer2014[numSystems];
		for(int i=0;i<numSystems;i++)
			scorers_2014[i] = new Scorer2014();
		doc_count = new HashMap<String,Map<String,Integer>>();
		off_count = new HashMap<String,Map<String,List<Integer>>>();
		prov_count = new HashMap<String,Map<String,Double>>();
		fextractions_target = new HashMap<String,Integer>();
		sys_count = new HashMap<String,Double>();
		fextractions_output = new HashMap<String,String>();
//		query_lines = new HashMap<String,Integer>();
		query_lines = new HashMap<String,String>();

		singleValuedSlots = Arrays.asList(
				"per:date_of_birth",
				"per:age",
				"per:country_of_birth",
				"per:stateorprovince_of_birth",
				"per:city_of_birth",
				"per:date_of_death",
				"per:country_of_death",
				"per:stateorprovince_of_death",
				"per:city_of_death",
				"per:cause_of_death",
				"per:religion",
				"org:number_of_employees_members",
				"org:date_founded",
				"org:date_dissolved",
				"org:country_of_headquarters",
				"org:stateorprovince_of_headquarters",
				"org:city_of_headquarters",
				"org:website");

		listValuedSlots = Arrays.asList(
				"per:alternate_names",
				"per:origin",
				"per:countries_of_residence",
				"per:statesorprovinces_of_residence",
				"per:cities_of_residence",
				"per:schools_attended",
				"per:title",
				"per:employee_or_member_of",
				"per:spouse",
				"per:children",
				"per:parents",
				"per:siblings",
				"per:other_family",
				"per:charges",
				"org:alternate_names",
				"org:political_religious_affiliation",
				"org:top_members_employees",
				"org:members",
				"org:member_of",
				"org:subsidiaries",
				"org:parents",
				"org:founded_by",
				"org:shareholders",
				"per:awards_won",
				"per:charities_supported",
				"per:diseases",
				"org:products",
				"per:pos-from",
				"per:neg-from",
				"per:pos-towards",
				"per:neg-towards",
				"org:pos-from",
				"org:neg-from",
				"org:pos-towards",
				"org:neg-towards",
				"gpe:pos-from",
				"gpe:neg-from",
				"gpe:pos-towards",
				"gpe:neg-towards",
				
				"org:employees_or_members",
				"gpe:employees_or_members",
				
				"org:students",
				"gpe:births_in_city",
				"gpe:births_in_stateorprovince",
				"gpe:births_in_country",
				"gpe:residents_of_city",
				"gpe:residents_of_stateorprovince",
				"gpe:residents_of_country",
				"gpe:deaths_in_city",
				"gpe:deaths_in_stateorprovince",
				"gpe:deaths_in_country",
				
				"per:holds_shares_in",
				"org:holds_shares_in",
				"gpe:holds_shares_in",
				
				"per:organizations_founded",
				"org:organizations_founded",
				"gpe:organizations_founded",
				
				"gpe:member_of",
				
				"per:top_member_employee_of",
				"gpe:headquarters_in_city",
				"gpe:headquarters_in_stateorprovince",
				"gpe:headquarters_in_country");
	}

	public void getFiles(String path){
		System.out.println(path);
		File folder = new File(path);
		System.out.println(folder.exists());
		System.out.println(folder.getAbsolutePath());
		File folder2 = new File("src/main/resources/");
		System.out.println(folder2.exists());
		File[] listOfFiles = folder.listFiles();
		int k=0;
//		import java.nio.file.Files;
//		System.out.println(Files.exists(path));
		System.out.println(listOfFiles.length);
		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				REOutputs[k] = path+"/"+listOfFiles[i].getName();
				System.out.println(REOutputs[k]);
				k++;
			}
		}
	}
	
	public Integer getNumAliasesUW(String key){
		String name = key.split("~")[2];
//		String ent_type = key.split("~")[1].split(":")[0];
//		Integer num_aliases = 0;
//		if(ent_type != "per"){
//			return 1;
//		}
		String fmls = "^([A-Za-z.-]+) ([A-Za-z.-]+) ([A-Za-z-]+) ([jJSs][Rr].{0,1})$";
		Pattern fmlsr = Pattern.compile(fmls);
		Matcher fmlsm = fmlsr.matcher(name);
		if (fmlsm.find( )) {
			return 3;
	    }
		String fml = "^([A-Za-z.-]+) ([A-Za-z.-]+) ([A-Za-z-]+)$";
		Pattern fmlr = Pattern.compile(fml);
		Matcher fmlm = fmlr.matcher(name);
		if (fmlm.find( )) {
			return 2;
	    }
		String fls = "^([A-Za-z.-]+) ([A-Za-z-]+) ([jJSs][Rr].{0,1})$";
		Pattern flsr = Pattern.compile(fls);
		Matcher flsm = flsr.matcher(name);
		if (flsm.find( )) {
			return 2;
	    }
		String fl = "^([A-Za-z.-]+) ([A-Za-z-]+)$";
		Pattern flr = Pattern.compile(fl);
		Matcher flm = flr.matcher(name);
		if (flm.find( )) {
			return 1;
	    }
		return 1;
	}

//	public Integer getQueryAnswerDocOverlap(String key, String prov){
//		String name = key.split("~")[2];
//		return 1;
//	}
	
	public void buildQueryLines(String file_name){
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = null;
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
		Document document = null;
		try {
			document = builder.parse(new File(file_name));
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
		Element root = document.getDocumentElement();
		NodeList nList = document.getElementsByTagName("query");
		for (int temp = 0; temp < nList.getLength(); temp++)
		{
		 Node node = nList.item(temp);
		 System.out.println("");    //Just a separator
		 if (node.getNodeType() == Node.ELEMENT_NODE)
		 {
		    //Print each employee's detail
		    Element eElement = (Element) node;
//		    System.out.println("Employee id : "    + eElement.getAttribute("id"));
//		    System.out.println("First Name : "  + eElement.getElementsByTagName("firstName").item(0).getTextContent());
//		    System.out.println("Last Name : "   + eElement.getElementsByTagName("lastName").item(0).getTextContent());
//		    System.out.println("Location : "    + eElement.getElementsByTagName("location").item(0).getTextContent());
//		    System.out.println("Employee id : "    + eElement.getAttribute("id"));
//		    System.out.println("First Name : "  + eElement.getElementsByTagName("docid").item(0).getTextContent());
		    query_lines.put(eElement.getAttribute("id"), eElement.getElementsByTagName("docid").item(0).getTextContent());
		 }
		}
//		System.exit(1);
	}
	
	public boolean findInDoc(String doc_id, Integer start, Integer end, String value){
		String base_name = "D:\\ProjectData\\data\\";
		String file_name;
		String doc_style;
		if(doc_id.startsWith("AFP") || doc_id.startsWith("APW") || doc_id.startsWith("CNA") || doc_id.startsWith("LTW") || doc_id.startsWith("NYT") || doc_id.startsWith("WPB") || doc_id.startsWith("XIN")){
			file_name = base_name + "newswire\\" + doc_id.substring(0,14);
			doc_style = "newswire";
		}
		else if(doc_id.startsWith("eng")){
			file_name = base_name + "web\\" + doc_id.substring(0,14);
			doc_style = "web";
//			System.out.println(doc_id);
//			System.exit(1);
		}
		else if(doc_id.startsWith("bolt")){
			file_name = base_name + "discussion_forums\\" + doc_id.substring(0,15);
			doc_style = "discussion_forums";
//			System.out.println(file_name);
//			System.out.println(doc_id);
//			System.exit(1);
		}
		else{
			file_name = null;
			doc_style = null;
			System.out.println("bad id");
			System.out.println(doc_id);
			System.exit(1);
		}
		
		BufferedReader featureReader = null;
		try {
			featureReader = new BufferedReader (new FileReader(file_name));
		} catch (FileNotFoundException e) {
			System.exit(1);
		}
		String line;
		String data = "";
		boolean found_flag = false;
		try {
			while ((line = featureReader.readLine()) != null) {
				if(line.startsWith("</DOC>") || line.startsWith("</doc>")){
					found_flag = false;
				}
				if(found_flag){
//					if(line.equals("")){
//						System.out.println("yay");
//						System.exit(1);
//					}
					data += line + " ";
				}
				if(doc_style == "newswire" && line.startsWith("<DOC id=\"" + doc_id)){
					found_flag = true;
					data += line + " ";
				}
				if(doc_style == "web" && line.startsWith("<DOCID> " + doc_id)){
					found_flag = true;
//					System.out.println(line);
//					System.exit(1);
					data += "<DOC> " + line + " ";
//					data += "<DOC> <DOCID> ";
				}
				if(doc_style == "discussion_forums" && line.startsWith("<doc id=\"" + doc_id)){
					found_flag = true;
//					System.out.println(line);
//					System.exit(1);
					data += line + " ";
//					data += "<DOC> <DOCID> ";
				}
			}
//			data = data.replaceAll("\n", " ");
//			data = data.replaceAll("<[^>]*>", "");
//			data = data.replacePattern("<P>", "");
//			if(doc_style.equals("discussion_forums")){
//				System.out.println(value);
//				System.out.println(data.substring(start,end+1));
//				System.exit(1);
//			}
			if(data.substring(start,end+1).contains(value)){
				return true;
			}
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
		
		return true;
	}
	
	public void getSlotsAndConfidences(String year){
		Map<String,Double> mp1=null,mp2=null;
		Map<String,Integer> t1=null,t2=null;
		Map<String,String> mpOut1=null,mpOut2=null,provenance=null,prov2=null,prov=null,provenance2=null;

		for(int i=0;i<numSystems;i++){
			if(year.equals("2013")){
				mp1=scorers_2013[i].mpConfidence;				
				t1=scorers_2013[i].mpTarget;
				mpOut1=scorers_2013[i].mpOutput;
				provenance = scorers_2013[i].offset_rel;
				prov = scorers_2013[i].rel_prov;
			}
			else if(year.equals("2014")){
				mp1=scorers_2014[i].mpConfidence;				
				t1=scorers_2014[i].mpTarget;
				mpOut1=scorers_2014[i].mpOutput;
				provenance = scorers_2014[i].offset_rel;
				prov = scorers_2014[i].rel_prov;
//				System.out.println(provenance);
//				System.exit(1);
			}
			for(String mp1key : mp1.keySet()){
				if(fextractions_confs.containsKey(mp1key)){
					continue;
				}
				else{
					ArrayList<Double> confs = new ArrayList<Double>();
					System.out.println(mp1key);
					System.out.println(getNumAliasesUW(mp1key));
//					System.exit(1);
					confs.add(mp1.get(mp1key));
					int start_main = 0,end_main=0;
					double jaccard = 0.0;
					int ans_length=mp1key.split("~")[2].split(" ").length;
//					System.out.println(mp1key);
//					System.out.println(mp1key.split("~")[2]);
//					System.exit(1);
//					int query_docs = query_lines.get(mp1key.split("~")[0]);
					if(provenance.containsKey(mp1key)&&prov_count.containsKey(mp1key)){
						String offset_main = provenance.get(mp1key);
						String[] tmp_main = offset_main.split("-");
						start_main = Integer.parseInt(tmp_main[0]);
						end_main = Integer.parseInt(tmp_main[1]);
					}
					fextractions_target.put(mp1key, t1.get(mp1key));
					fextractions_output.put(mp1key, mpOut1.get(mp1key));
					int overlap_count =0;
					for(int j=0;j<numSystems;j++){
						if(i==j){
							continue;
						}
						if(year.equals("2013")){
							mp2=scorers_2013[j].mpConfidence;				
							t2=scorers_2013[j].mpTarget;
							mpOut2=scorers_2013[j].mpOutput;
							provenance2=scorers_2013[j].offset_rel;
							prov2 = scorers_2013[i].rel_prov;
						}
						else if(year.equals("2014")){
							mp2=scorers_2014[j].mpConfidence;				
							t2=scorers_2014[j].mpTarget;
//							System.out.println(t2.get(mp1key));
							mpOut2=scorers_2014[j].mpOutput;
							provenance2=scorers_2014[j].offset_rel;
							prov2 = scorers_2014[i].rel_prov;
						}

						if(mp2.containsKey(mp1key)){
//						if(mp2.containsKey(mp1key)&&t2.containsKey(mp1key)){
							confs.add(mp2.get(mp1key));
//							System.out.println(mp1key);
//							System.out.println(t2.get(mp1key));
							int target = t2.get(mp1key);
							String out = mpOut2.get(mp1key);
							if(target==1){
								if(fextractions_target.containsKey(mp1key)){
									if(fextractions_target.get(mp1key)==0){
										fextractions_target.remove(mp1key);
										fextractions_output.remove(mp1key);
									}									
								}
								fextractions_target.put(mp1key, target);
								fextractions_output.put(mp1key, out);
							}
						}
						else{
							confs.add(0.0);
						}
						if(provenance.containsKey(mp1key)&&prov_count.containsKey(mp1key)){
							if(mp2.containsKey(mp1key)&&prov2.get(mp1key).equals(prov.get(mp1key))&&prov_count.get(mp1key).containsKey(prov.get(mp1key))){
								String offset = provenance2.get(mp1key);
								int start=0,end=0;
								if(!offset.equals("")){
									String[] tmp = offset.split("-");
									start = Integer.parseInt(tmp[0]);
									end = Integer.parseInt(tmp[1]);	
								}
								if((start_main>=start && end_main<=end)||(start>=start_main && end<=end_main)||(end_main>=start&&start_main<=end)){
									int intersection = 0;
									int union = 0;
									if(start_main>=start && end_main<=end){
										intersection = end_main-start_main;
										union = end-start;
									}
									else if(start>=start_main && end<=end_main){
										intersection = end-start;
										union = end_main-start_main;
									}
									else{
										intersection = end_main-start;	
										union = Math.max(end,end_main)-Math.min(start,start_main);
									}
									jaccard += (double)intersection/(double)union;
								}
							}
						}
					}
					if(prov_count.containsKey(mp1key)){
						if(prov_count.get(mp1key).containsKey(provenance.get(mp1key))){
							confs.add(prov_count.get(mp1key).get(provenance.get(mp1key)));
							confs.add((double)doc_count.get(mp1key).get(provenance.get(mp1key)));
						}
						else{
							confs.add(0.0);
							confs.add(0.0);
						}
					}
					else{
						confs.add(0.0);
						confs.add(0.0);
					}
					double score = 0;
					if(provenance.containsKey(mp1key)&&prov_count.containsKey(mp1key)){
						if(prov_count.get(mp1key).containsKey(prov.get(mp1key))){
							score = overlap_count*sys_count.get(mp1key)*prov_count.get(mp1key).get(prov.get(mp1key));
							confs.add((double)overlap_count);
						}
						else {
							confs.add(0.0);
						}
					}
					else {
						confs.add(0.0);
					}
					if(overlap_count==0)
						confs.add(0.0);
					else
						confs.add(jaccard/overlap_count);
					
					//feature:UWaliases
					confs.add((double)getNumAliasesUW(mp1key));
					
					//feature:ans_length
					confs.add((double)ans_length);
					
					//feature:num_letters
					confs.add((double)mp1key.split("~")[2].length());
					
//					System.out.println(mp1key);
//					System.out.println(query_lines.get(mp1key.split("~")[0]));
//					System.out.println(provenance.get(mp1key));
//					System.out.println(prov.get(mp1key));
//					System.exit(1);
					
					//feature:query_doc_overlap
					if(query_lines.get(mp1key.split("~")[0]).equals(prov.get(mp1key))){
						confs.add(1.0);
//						System.out.println("yay");
//						System.exit(1);
					}
					else{
						confs.add(0.0);
					}
//					Integer.parseInt("1234");
					Integer start = Integer.parseInt(provenance.get(mp1key).split("-")[0]);
					Integer end = Integer.parseInt(provenance.get(mp1key).split("-")[1]);
//					System.out.println(provenance.get(mp1key));
//					System.out.println(start);
//					System.out.println(end);
//					System.exit(1);
//					String end = provenance.get(mp1key).split("-")[1];
//					System.out.println(findInDoc(prov.get(mp1key), start, end, mp1key.split("~")[2]));
					
					//feature:value_in_provenance
					if(findInDoc(prov.get(mp1key), start, end, mp1key.split("~")[2])){
						confs.add(1.0);
					}
					else {
						confs.add(0.0);
					}
					
					//confs.add((double)query_docs);
					fextractions_confs.put(mp1key, confs);
				}
			}			
		}
	}


	public void writeOutput(int num, String year, String feature_file, String out_file) throws IOException{
		ArrayList<String> al= new ArrayList<String>();
		al.addAll(singleValuedSlots);
		al.addAll(listValuedSlots);
		BufferedWriter bw = new BufferedWriter(new FileWriter(out_file));
		BufferedWriter bfeatures = new BufferedWriter(new FileWriter(feature_file));
		bfeatures.write("@relation jaccard_"+year+"\n");
		bfeatures.write("\n");
		for(int i=0;i<numSystems;i++){
			int tmp=i+1;
			bfeatures.write("@attribute conf_"+tmp+" numeric\n");
		}
		bfeatures.write("@attribute prov numeric\n");
		bfeatures.write("@attribute num_prov numeric\n");
		bfeatures.write("@attribute jaccard numeric\n");
		
		//feature:UWaliases
		bfeatures.write("@attribute UWaliases numeric\n");
		
		//feature:ans_length
		bfeatures.write("@attribute answer_length numeric\n");
		
		//feature:num_letters
		bfeatures.write("@attribute num_letters numeric\n");
		
		//feature:query_doc_overlap
		bfeatures.write("@attribute query_doc_overlap numeric\n");
		
		//feature:value_in_provenance
		bfeatures.write("@attribute value_in_provenance numeric\n");
		
		bfeatures.write("@attribute num_offset numeric\n");
//		bfeatures.write("@attribute rel {per:cities_of_residence,per:employee_or_member_of,per:age,org:date_founded,per:schools_attended,org:alternate_names,org:top_members_employees,org:member_of,org:shareholders,org:parents,org:subsidiaries,per:siblings,per:spouse,per:title,per:countries_of_residence,per:country_of_birth,per:alternate_names,per:parents,org:founded_by,org:country_of_headquarters,org:city_of_headquarters,org:date_dissolved,per:statesorprovinces_of_residence,per:stateorprovince_of_death,per:city_of_death,per:city_of_birth,per:stateorprovince_of_birth,org:members,per:children,per:cause_of_death,org:stateorprovince_of_headquarters,per:charges,org:website,per:religion,per:country_of_death,per:other_family,per:date_of_death,org:number_of_employees_members,per:date_of_birth,per:origin,org:political_religious_affiliation}\n");
		bfeatures.write("@attribute rel {per:cities_of_residence,per:employee_or_member_of,per:age,org:date_founded,per:schools_attended,org:alternate_names,org:top_members_employees,org:member_of,org:shareholders,org:parents,org:subsidiaries,per:siblings,per:spouse,per:title,per:countries_of_residence,per:country_of_birth,per:alternate_names,per:parents,org:founded_by,org:country_of_headquarters,org:city_of_headquarters,org:date_dissolved,per:statesorprovinces_of_residence,per:stateorprovince_of_death,per:city_of_death,per:city_of_birth,per:stateorprovince_of_birth,org:members,per:children,per:cause_of_death,org:stateorprovince_of_headquarters,per:charges,org:website,per:religion,per:country_of_death,per:other_family,per:date_of_death,org:number_of_employees_members,per:date_of_birth,per:origin,org:political_religious_affiliation,org:employees_or_members,gpe:employees_or_members,org:students,gpe:births_in_city,gpe:births_in_stateorprovince,gpe:births_in_country,gpe:residents_of_city,gpe:residents_of_stateorprovince,gpe:residents_of_country,gpe:deaths_in_city,gpe:deaths_in_stateorprovince,gpe:deaths_in_country,per:holds_shares_in,org:holds_shares_in,gpe:holds_shares_in,per:organizations_founded,org:organizations_founded,gpe:organizations_founded,gpe:member_of,per:top_member_employee_of,gpe:headquarters_in_city,gpe:headquarters_in_stateorprovince,gpe:headquarters_in_country}\n");
		bfeatures.write("@attribute target {w,c}\n");
		bfeatures.write("\n");
		bfeatures.write("@data\n");
		for(String key : fextractions_confs.keySet()){
//			System.out.println(key);
//			System.exit(1);
			ArrayList<Double> confs = (ArrayList<Double>) fextractions_confs.get(key);
			//if(confs.get(confs.size()-1)<1)
				//continue;
			String conf_str = "";
			for(int i =0;i<confs.size();i++){
				conf_str += confs.get(i)+",";
			}
//			System.out.println(conf_str);
			//conf_str += confs.get(confs.size()-2);
			conf_str = conf_str.trim();

			String[] parts = key.split("~");
			String out_str = fextractions_output.get(key);

			bw.write(out_str+"\n");
//			System.out.println(fextractions_target.get(key));
//			if(fextractions_target.get(key) != null) {
			if(fextractions_target.get(key)==1)
				bfeatures.write(conf_str+parts[1]+","+"w"+"\n");
			else if(fextractions_target.get(key)==0)
				bfeatures.write(conf_str+parts[1]+","+"w"+"\n");
			else
				bfeatures.write(conf_str+parts[1]+","+"c"+"\n");
//			}
		}

		bw.close();
		bfeatures.close();
	}

	public void getFeatures(String year, int nsys, String key, String file, String query) throws IOException {
		BufferedReader featureReader = null;
		try {
			featureReader = new BufferedReader (new FileReader(file));
		} catch (FileNotFoundException e) {
			System.exit (1);
		}
		String line;
		if(year.equals("2014")){
			while ((line = featureReader.readLine()) != null) {
				String[] prov = line.split("\t");
				if(!(prov.length>4))
					continue;
				String uniq = prov[0]+"~"+prov[1]+"~"+prov[4].trim();
				String[] provenance = prov[5].split(":");
				String doc_id = "";
				int start,end=0;
				if(!prov[5].trim().contains(",")){
					String[] provenance1 = prov[5].trim().split(":");
					doc_id = provenance1[0];
					String[] offset = provenance1[1].split("-");
					start = Integer.parseInt(offset[0]);
					end = Integer.parseInt(offset[1]);
				}
				else{
					String[] tmp = prov[5].trim().split(",");
					String[] provenance1 = tmp[0].split(":");
					doc_id = provenance1[0];
					String[] offset = provenance1[1].split("-");
					start = Integer.parseInt(offset[0]);
					end = Integer.parseInt(offset[1]);
				}
				if(!off_count.containsKey(uniq)){
					Map<String,List<Integer>> tmp = new HashMap<String,List<Integer>>();
					List<Integer> offsetList = new ArrayList<Integer>();
					offsetList.add(start);
					offsetList.add(end);
					tmp.put(doc_id,offsetList);
					off_count.put(uniq, tmp);
					Map<String,Integer> tmp1 = new HashMap<String,Integer>();
					tmp1.put(provenance[0],1);
					doc_count.put(uniq, tmp1);
				}
				else if(off_count.get(uniq).containsKey(doc_id)){
					Map<String,List<Integer>> tmp = off_count.get(uniq);
					List<Integer> offsetList = tmp.get(doc_id);
					offsetList.add(start);
					offsetList.add(end);
					tmp.put(doc_id,offsetList);
					off_count.put(uniq,tmp);
					Map<String,Integer> tmp1 = doc_count.get(uniq);
					tmp1.put(provenance[0],tmp1.get(provenance[0])+1);
					doc_count.put(uniq,tmp1);
				}
				else{
					Map<String,List<Integer>> tmp = off_count.get(uniq);
					List<Integer> offsetList = new ArrayList<Integer>();
					offsetList.add(start);
					offsetList.add(end);
					tmp.put(doc_id,offsetList);
					off_count.put(uniq, tmp);
					Map<String,Integer> tmp1 = doc_count.get(uniq);
					tmp1.put(provenance[0],1);
					doc_count.put(uniq, tmp1);
				}
			}
		}
		else{
			while ((line = featureReader.readLine()) != null) {
				String[] prov = line.split("\t");
				if(!(prov.length>7))
					continue;
				String uniq = prov[0]+"~"+prov[1]+"~"+prov[4].trim();
				String doc_id = prov[3];
				int start=0,end=0;
				if(!prov[5].trim().contains(",")&&!prov[5].equals("")){
					String[] offset = prov[5].trim().split("-");
					start = Integer.parseInt(offset[0]);
					end = Integer.parseInt(offset[1]);
				}
				else if(!prov[5].equals("")){
					String[] tmp = prov[5].trim().split(",");
					String[] offset = tmp[0].split("-");
					start = Integer.parseInt(offset[0]);
					end = Integer.parseInt(offset[1]);
				}
				if(!off_count.containsKey(uniq)){
					Map<String,List<Integer>> tmp = new HashMap<String,List<Integer>>();
					List<Integer> offsetList = new ArrayList<Integer>();
					offsetList.add(start);
					offsetList.add(end);
					tmp.put(doc_id,offsetList);
					off_count.put(uniq, tmp);
					Map<String,Integer> tmp1 = new HashMap<String,Integer>();
					tmp1.put(prov[3],1);
					doc_count.put(uniq, tmp1);
				}
				else if(off_count.get(uniq).containsKey(doc_id)){
					Map<String,List<Integer>> tmp = off_count.get(uniq);
					List<Integer> offsetList = tmp.get(doc_id);
					offsetList.add(start);
					offsetList.add(end);
					tmp.put(doc_id,offsetList);
					off_count.put(uniq,tmp);
					Map<String,Integer> tmp1 = doc_count.get(uniq);
					tmp1.put(prov[3],tmp1.get(prov[3])+1);
					doc_count.put(uniq,tmp1);
				}
				else{
					Map<String,List<Integer>> tmp = off_count.get(uniq);
					List<Integer> offsetList = new ArrayList<Integer>();
					offsetList.add(start);
					offsetList.add(end);
					tmp.put(doc_id,offsetList);
					off_count.put(uniq, tmp);
					Map<String,Integer> tmp1 = doc_count.get(uniq);
					tmp1.put(prov[3],1);
					doc_count.put(uniq, tmp1);
				}
			}
		}
		featureReader.close();
		for(String key_name: off_count.keySet()){
			Map<String,Integer> tmp1 = doc_count.get(key_name);
			int total_doc=0;
			for(String doc: tmp1.keySet()){
				int count = tmp1.get(doc);
				total_doc = total_doc+count;
			}
			Map<String,Double> temp = new HashMap<String,Double>();
			for(String doc: tmp1.keySet()){
				temp.put(doc,(double)tmp1.get(doc)/(double)total_doc);
			}
			prov_count.put(key_name, temp);
			sys_count.put(key_name, (double)total_doc/10.0);
		}
		buildQueryLines(query);
//		featureReader = new BufferedReader(new FileReader(query));
//		String q="";
//		if(year.equals("2013"))
//			q="SF13_ENG_";
//		else
//			q="SF14_ENG_";
//		int count=0;
//		while((line = featureReader.readLine()) != null){
//			count++;
//			if(count<10)
//				query_lines.put(q+"00"+count,Integer.parseInt(line.trim()));
//			else if(count<100)
//				query_lines.put(q+"0"+count,Integer.parseInt(line.trim()));
//			else
//				query_lines.put(q+count,Integer.parseInt(line.trim()));
//		}
	}
}