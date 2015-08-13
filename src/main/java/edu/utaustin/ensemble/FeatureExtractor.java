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
	static Map<String,Integer> query_lines;
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
		query_lines = new HashMap<String,Integer>();

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
			}
			for(String mp1key : mp1.keySet()){
				if(fextractions_confs.containsKey(mp1key)){
					continue;
				}
				else{
					ArrayList<Double> confs = new ArrayList<Double>();
					confs.add(mp1.get(mp1key));
					int start_main = 0,end_main=0;
					double jaccard = 0.0;
					int ans_length=mp1key.split("~")[2].split(" ").length;
					System.out.println(mp1key.split("~")[0]);
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
					//confs.add((double)ans_length);
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
		bfeatures.write("@attribute num_offset numeric\n");
//		bfeatures.write("@attribute rel {per:cities_of_residence,per:employee_or_member_of,per:age,org:date_founded,per:schools_attended,org:alternate_names,org:top_members_employees,org:member_of,org:shareholders,org:parents,org:subsidiaries,per:siblings,per:spouse,per:title,per:countries_of_residence,per:country_of_birth,per:alternate_names,per:parents,org:founded_by,org:country_of_headquarters,org:city_of_headquarters,org:date_dissolved,per:statesorprovinces_of_residence,per:stateorprovince_of_death,per:city_of_death,per:city_of_birth,per:stateorprovince_of_birth,org:members,per:children,per:cause_of_death,org:stateorprovince_of_headquarters,per:charges,org:website,per:religion,per:country_of_death,per:other_family,per:date_of_death,org:number_of_employees_members,per:date_of_birth,per:origin,org:political_religious_affiliation}\n");
		bfeatures.write("@attribute rel {per:cities_of_residence,per:employee_or_member_of,per:age,org:date_founded,per:schools_attended,org:alternate_names,org:top_members_employees,org:member_of,org:shareholders,org:parents,org:subsidiaries,per:siblings,per:spouse,per:title,per:countries_of_residence,per:country_of_birth,per:alternate_names,per:parents,org:founded_by,org:country_of_headquarters,org:city_of_headquarters,org:date_dissolved,per:statesorprovinces_of_residence,per:stateorprovince_of_death,per:city_of_death,per:city_of_birth,per:stateorprovince_of_birth,org:members,per:children,per:cause_of_death,org:stateorprovince_of_headquarters,per:charges,org:website,per:religion,per:country_of_death,per:other_family,per:date_of_death,org:number_of_employees_members,per:date_of_birth,per:origin,org:political_religious_affiliation,org:employees_or_members,gpe:employees_or_members,org:students,gpe:births_in_city,gpe:births_in_stateorprovince,gpe:births_in_country,gpe:residents_of_city,gpe:residents_of_stateorprovince,gpe:residents_of_country,gpe:deaths_in_city,gpe:deaths_in_stateorprovince,gpe:deaths_in_country,per:holds_shares_in,org:holds_shares_in,gpe:holds_shares_in,per:organizations_founded,org:organizations_founded,gpe:organizations_founded,gpe:member_of,per:top_member_employee_of,gpe:headquarters_in_city,gpe:headquarters_in_stateorprovince,gpe:headquarters_in_country}\n");
		bfeatures.write("@attribute target {w,c}\n");
		bfeatures.write("\n");
		bfeatures.write("@data\n");
		for(String key : fextractions_confs.keySet()){
			ArrayList<Double> confs = (ArrayList<Double>) fextractions_confs.get(key);
			//if(confs.get(confs.size()-1)<1)
				//continue;
			String conf_str = "";
			for(int i =0;i<confs.size();i++){
				conf_str += confs.get(i)+",";
			}
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