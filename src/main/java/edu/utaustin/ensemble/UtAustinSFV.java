package edu.utaustin.ensemble;

public class UtAustinSFV {

	public static void main(String[] args) throws Exception {
		Integer hop_num = 2;
//		String inputDir1 = "src/main/resources/2013";
		String inputDir1 = "src/main/resources/2014_CS/";
//		String inputDir1 = "C:\\Users\\Alex\\eclipse_workspace\\ESFE_Alex\\src\\main\\resources\\2014_CS";
		
//		String inputDir2 = "src/main/resources/2014";
		String inputDir2 = "src/main/resources/2015_CS/";
//		String inputDir2 = "C:\\Users\\Alex\\eclipse_workspace\\ESFE_Alex\\src\\main\\resources\\2015_CS";
		
//		Integer nsys = 10;
		Integer nsys = 10;
		
//		String year1= "2013";
		String year1= "2014";
		
		String year2 = "2014";
		
//		String key1 = "src/main/resources/keys/key_file_2013";
		String key1 = "src/main/resources/keys/cskey_2014";
//		String key1 = "C:\\Users\\Alex\\eclipse_workspace\\ESFE_Alex\\src\\main\\resources\\keys\\cskey_2014";
		
//		String key2 = "src/main/resources/keys/key_file_2014";
		String key2 = "src/main/resources/keys/cskey_2014";
//		String key2 = "C:\\Users\\Alex\\eclipse_workspace\\ESFE_Alex\\src\\main\\resources\\keys\\cskey_2014";
		
//		String query1="src/main/resources/q_2013";
		String query1="src/main/resources/query.xml";
//		String query1="C:\\Users\\Alex\\eclipse_workspace\\ESFE_Alex\\src\\main\\resources\\query.xml";
		
		String query2="src/main/resources/q_2014";
		
//		String out_file1 = "src/main/resources/2013_out";
		String out_file1 = "src/main/resources/2014_CS_out";
//		String out_file1 = "C:\\Users\\Alex\\eclipse_workspace\\ESFE_Alex\\src\\main\\resources\\2014_CS_out";
		
//		String out_file2 = "src/main/resources/2014_out";
		String out_file2 = "src/main/resources/2015_CS_out";
//		String out_file2 = "C:\\Users\\Alex\\eclipse_workspace\\ESFE_Alex\\src\\main\\resources\\2015_CS_out";
		
//		String feature_file1 = "src/main/resources/2013_"+nsys+".arff";
		String feature_file1 = "src/main/resources/2014_CS_"+nsys+".arff";
//		String feature_file1 = "C:\\Users\\Alex\\eclipse_workspace\\ESFE_Alex\\src\\main\\resources\\2014_CS_"+nsys+".arff";
		
//		String feature_file2 = "src/main/resources/2014_"+nsys+".arff";
		String feature_file2 = "src/main/resources/2015_CS_"+nsys+".arff";
//		String feature_file2 = "C:\\Users\\Alex\\eclipse_workspace\\ESFE_Alex\\src\\main\\resources\\2015_CS_"+nsys+".arff";
		
		String prob_file ="src/main/resources/prob";
		
//		String outFile=new String("src/main/resources/"+year2+"_final");
		String outFile=new String("src/main/resources/2015_final");
//		String outFile=new String("C:\\Users\\Alex\\eclipse_workspace\\ESFE_Alex\\src\\main\\resources\\2015_final");
		
		String aliasFlag = "false";
		
		if(hop_num == 2){
			inputDir1 = "src/main/resources/2014_CS_hop2/";
//			inputDir1 = "C:\\Users\\Alex\\eclipse_workspace\\ESFE_Alex\\src\\main\\resources\\2014_CS_hop2";
			inputDir2 = "src/main/resources/2015_CS_hop2/";
//			inputDir2 = "C:\\Users\\Alex\\eclipse_workspace\\ESFE_Alex\\src\\main\\resources\\2015_CS_hop2";
			nsys = 10;
			year1= "2014";
			year2 = "2014";
//			key1 = "C:\\Users\\Alex\\eclipse_workspace\\ESFE_Alex\\src\\main\\resources\\keys\\cskey_2014";
//			key2 = "C:\\Users\\Alex\\eclipse_workspace\\ESFE_Alex\\src\\main\\resources\\keys\\cskey_2014";
//			query1="C:\\Users\\Alex\\eclipse_workspace\\ESFE_Alex\\src\\main\\resources\\query.xml";
//			query2="src/main/resources/q_2014";
			out_file1 = "src/main/resources/2014_CS_out_hop2";
//			out_file1 = "C:\\Users\\Alex\\eclipse_workspace\\ESFE_Alex\\src\\main\\resources\\2014_CS_out_hop2";
			out_file2 = "src/main/resources/2015_CS_out_hop2";
//			out_file2 = "C:\\Users\\Alex\\eclipse_workspace\\ESFE_Alex\\src\\main\\resources\\2015_CS_out_hop2";
			feature_file1 = "src/main/resources/2014_CS_"+nsys+"_hop2.arff";
//			feature_file1 = "C:\\Users\\Alex\\eclipse_workspace\\ESFE_Alex\\src\\main\\resources\\2014_CS_"+nsys+"_hop2.arff";
			feature_file2 = "src/main/resources/2015_CS_"+nsys+"_hop2.arff";
//			feature_file2 = "C:\\Users\\Alex\\eclipse_workspace\\ESFE_Alex\\src\\main\\resources\\2015_CS_"+nsys+"_hop2.arff";
//			prob_file ="C:\\Users\\Alex\\eclipse_workspace\\ESFE_Alex\\src\\main\\resources\\prob_hop2";
			outFile=new String("src/main/resources/2015_final_hop2");
//			outFile=new String("C:\\Users\\Alex\\eclipse_workspace\\ESFE_Alex\\src\\main\\resources\\2015_final_hop2");
			aliasFlag = "false";
			
		}
		
		FeatureExtractor fe1 = new FeatureExtractor(nsys);
		fe1.getFiles(inputDir1);
		for(int sys=0; sys<nsys;sys++){
			fe1.getFeatures(year1, nsys, key1, fe1.REOutputs[sys],query1);
		}
		for(int i=0;i<nsys;i++){
			String[] nargs=new String[3];
			nargs[0]=fe1.REOutputs[i];
			nargs[1]=key1;
//			nargs[2]= new String("anydoc");
			nargs[2]= new String("trace");
			fe1.scorers_2014[i].run(nargs);	
		}
		fe1.getSlotsAndConfidences(year1);
		fe1.writeOutput(nsys,year1,feature_file1,out_file1);
		
		FeatureExtractor fe2 = new FeatureExtractor(nsys);
		fe2.getFiles(inputDir2);
		for(int sys=0; sys<nsys;sys++){
			fe2.getFeatures(year2, nsys, key2, fe2.REOutputs[sys],query2);
		}
		for(int i=0;i<nsys;i++){
			String[] nargs=new String[3];
			nargs[0]=fe2.REOutputs[i];
			nargs[1]=key2;
//			nargs[2]= new String("anydoc");
			nargs[2]= new String("trace");
			fe2.scorers_2014[i].run(nargs);	
		}
		fe2.getSlotsAndConfidences(year2);
		fe2.writeOutput(nsys,year2,feature_file2,out_file2);
		
		SVMClassifier sc = new SVMClassifier();
		sc.classify(feature_file1,feature_file2);
		
		PostProcessor pp = new PostProcessor(aliasFlag,"","");
		pp.populateSingleValuedSlots();
		pp.populateSlotFills();
		pp.processClassifierOutput(out_file2,year2,prob_file,sc.prediction);
		pp.writeOutputFile(outFile);
		
		String[] nargs=new String[3];
		nargs[0] = outFile;
		nargs[1] = key2;
		nargs[2] = new String("anydoc");
		SFScorer sf = new SFScorer();
		sf.run(nargs);
	}
}
