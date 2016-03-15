package edu.carleton.COMP4601.web;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.mcavallo.opencloud.Cloud;
import org.mcavallo.opencloud.Tag;

import com.googlecode.charts4j.Color;
import com.googlecode.charts4j.GCharts;
import com.googlecode.charts4j.PieChart;
import com.googlecode.charts4j.Slice;

import edu.carleton.COMP4601.constants.JobTitles;
import edu.carleton.COMP4601.models.JobPostDocument;
import edu.carleton.COMP4601.models.JobPostDocumentCollection;
import edu.carleton.COMP4601.models.Pair;
import edu.carleton.COMP4601.mongodb.MongoDBManager;


public class PageGenerator {
	public String generateIndexPage() throws UnknownHostException{
		String html = "";
		html = html + "<html>\n";
		html = html + "<title>Employer's Preference</title>\n";
		html = html + "<body><h1><center>Employer's Preference</center></h1></body>\n";
		
		/*
		 * Setup the job list
		 */
		ArrayList<String> jobRanks = new ArrayList<String>();
		jobRanks.add(JobTitles.WEB_DEVELOPER);
		jobRanks.add(JobTitles.SYSTEM_ANALYST);
		jobRanks.add(JobTitles.MOBILE_DEVELOPER);
//		jobRanks.add(JobTitles.DATABASE_ADMINISTRATOR);
		
		/*
		 * Generate one table for a job
		 */
		for(String job:jobRanks){
			JobPostDocumentCollection coll = MongoDBManager.getInstance().getCollectionByJobTitle(job);
			ArrayList<Pair<String,Integer>> skillList = rankJobSkills(coll); 
			ArrayList<Pair<String,Integer>> percentages = calculatePercentages(skillList);
			
			String chartUrl = "";
			List<Slice> list = new ArrayList<Slice>();
			for(Pair<String,Integer> pair:percentages)	
				list.add(Slice.newSlice(pair.getSecond(), Color.newColor(randomColor()), pair.getFirst(), pair.getFirst()));
			
			PieChart chart = GCharts.newPieChart(list);
			chart.setTitle(job);
	        chart.setSize(700, 400);
	        chartUrl = chart.toURLString();
	        
	        
	        html = html + "<br><br><table width='1000' border='0' align='center'>\n";
			html = html + "<tr>\n";
			html = html + "<td width='300'><h2><a href=\"ep/"+job.toLowerCase().replace(" ", "_")+"\">"+job+"</a></h2></td>\n";
			html = html + "<td width='700'></td>\n";
			html = html + "</tr>\n";
			html = html + "<tr>\n";
			html = html + "<td width='300'></td>\n";
			html = html + "<td width='700' rowspan='10'><img border='0' src='"+chartUrl+"' align='left'></td>\n";
			html = html + "</tr>\n";
			
			/*
			 * One table with three job skill lists
			 */
			
			int rankNum = 0;
			if(skillList.size()>=10)
				rankNum = 10;
			else
				rankNum = skillList.size()-1;
			
			for(int i=0; i<rankNum;i++){
				html = html + "<tr>\n";
				
					if(skillList.size()!=0 && skillList.get(i)!=null)
						html = html + "<td>\n" + (i+1) + ". " + skillList.get(i).getFirst() 
									+ "    (" + skillList.get(i).getSecond()+")</td>\n";
					else
						html = html + "<td>No Data Found</td>\n"; 
				
				html = html + "</tr>\n";
			}
			html = html + "</table>";
		}
		
		html = html + "</body></html>\n";
		return html;
	}
	
	public String generateCloud(String job) throws UnknownHostException{
		ArrayList<Pair<String,Integer>> skillList = new ArrayList<Pair<String,Integer>>();
		String theJob = null;
		JobPostDocumentCollection coll = new JobPostDocumentCollection();
		coll.setJobPostDocuments(new ArrayList<JobPostDocument>());
		
		if(job.equalsIgnoreCase("mobile_developer"))
			theJob = JobTitles.MOBILE_DEVELOPER; 
		
		else if(job.equalsIgnoreCase("web_developer"))
			theJob = JobTitles.WEB_DEVELOPER; 
		
		else if(job.equalsIgnoreCase("system_analyst"))
			theJob = JobTitles.SYSTEM_ANALYST; 
		
		if(theJob!=null){
			coll = MongoDBManager.getInstance().getCollectionByJobTitle(theJob);
			skillList = rankJobSkills(coll); 
		}
		
		Cloud cloud = new Cloud();  // create cloud 
		cloud.setMaxWeight(38.0);   // max font size
		for(Pair<String,Integer> pair:skillList){
			Tag tag = new Tag(pair.getFirst(),Math.sqrt(Math.sqrt(pair.getSecond())));
			tag.setLink(job+"/"+pair.getFirst());
			cloud.addTag(tag);
		}
		
		
		String html = "<html>\n<head>";
		html = html + "<title>"+ theJob +" Skill Cloud</title>\n";
		html = html + "<body><h1><center>"+theJob+"'s Skill Cloud</center></h1></body>\n";
		html = html + "<style type=\"text/css\">\n";
		
		/*
		 * CSS
		 */
		html = html + "body {font-family: Arial, Helvetica, Sans-serif;}a {text-decoration: none;}"
				+ "a:link {color: #0063DC;}a:visited {color: #1057ae;}a:hover {color: #FFFFFF;background: #0063DC;}"
				+ "a:active { color: #FFFFFF;background: #0259C4;}"
				+ ".tagcloud {text-align: justify;padding: 15px;border: 1px solid #eeeeee;background-color: #f5f5f5;}";
		
		html = html + "\n</style>\n</head>\n<body>";
		html = html + "<div class='tagcloud' style='margin: auto; width: 80%;'>\n";
		for (Tag t : cloud.tags()) { 
			html = html + "<a href=\""+t.getLink()+"\" style=\"font-size:"+ t.getWeight() +"px\">";
			html = html + t.getName() +"</a>\n";
		}
		html = html + "</div>\n</body>\n";
		
		html = html + "<br><br><table width='1000' border='0' align='center'>\n";
		html = html + "<tr>\n";
		html = html + "<td width='1000'><h2><center>"+theJob+" jobs' Difficulty Ranking</center></h2></td>\n";
		html = html + "</tr>\n";
		html = html + "<tr>\n";
		html = html + "<td width='1000'></td>\n";
		html = html + "</tr>\n";
		html = html + jobDifficultyIndex(coll);
		html = html + "</html>";
		
		return html;
	}
	
	
	public String searchJobWithKeyword(String job, String keyword) throws UnknownHostException{
		String theJob = null;
		
		if(job.equalsIgnoreCase("mobile_developer"))
			theJob = JobTitles.MOBILE_DEVELOPER; 
		
		else if(job.equalsIgnoreCase("web_developer"))
			theJob = JobTitles.WEB_DEVELOPER; 
		
		else if(job.equalsIgnoreCase("system_analyst"))
			theJob = JobTitles.SYSTEM_ANALYST; 
		
		if(theJob==null)
			return "";
		
		JobPostDocumentCollection collectionByJobTitle = MongoDBManager.getInstance().getCollectionByJobTitle(theJob);
		
		JobPostDocumentCollection coll = new JobPostDocumentCollection();
		coll.setJobPostDocuments(new ArrayList<JobPostDocument>());
		
		for(JobPostDocument doc:collectionByJobTitle.getJobPostDocuments()){
			if(doc.getTitle().toLowerCase().contains(keyword.toLowerCase())
					&& doc.getTextContent().toLowerCase().contains(keyword.toLowerCase()))
				coll.getJobPostDocuments().add(doc);
		}
		
		ArrayList<Pair<String,Integer>> skillList = rankJobSkills(coll); 
		ArrayList<Pair<String,Integer>> percentages = calculatePercentages(skillList);
		
		String chartUrl = "";
		List<Slice> list = new ArrayList<Slice>();
		for(Pair<String,Integer> pair:percentages)	
			list.add(Slice.newSlice(pair.getSecond(), Color.newColor(randomColor()), pair.getFirst(), pair.getFirst()));
		
		PieChart chart = GCharts.newPieChart(list);
		chart.setTitle(job);
        chart.setSize(700, 400);
        chartUrl = chart.toURLString();
		
		String html = "";
		html = html + "<html>\n";
		html = html + "<title>" + theJob + " Related To:" + keyword + "</title>\n";
		html = html + "<body><h1><center>" + theJob + " Skills Related To: " + keyword + "</center></h1></body>\n";

		
		html = html + "<br><br><table width='1000' border='0' align='center'>\n";
		html = html + "<tr>\n";
		html = html + "<td width='300'><h2>"+theJob+"</h2></td>\n";
		html = html + "<td width='700'></td>\n";
		html = html + "</tr>\n";
		html = html + "<tr>\n";
		html = html + "<td width='300'></td>\n";
		html = html + "<td width='700' rowspan='10'><img border='0' src='"+chartUrl+"' align='left'></td>\n";
		html = html + "</tr>\n";
		
		
		/*
		 * Generate the HTML for each row of the table
		 */
		int rankNum = 0;
		if(skillList.size()>=10)
			rankNum = 10;
		else
			rankNum = skillList.size()-1;
		
		for(int i=0; i<rankNum;i++){
			html = html + "<tr>\n";
			
				if(skillList.size()!=0 && skillList.get(i)!=null)
					html = html + "<td>\n" + (i+1) + ". " + skillList.get(i).getFirst() 
								+ "    (" + skillList.get(i).getSecond()+")</td>\n";
				else
					html = html + "<td>No Data Found</td>\n"; 
			
			html = html + "</tr>\n";
		}
		
		html = html + "</table>\n</body>\n";
		
		
		html = html + "<br><br><table width='1000' border='0' align='center'>\n";
		html = html + "<tr>\n";
		html = html + "<td width='1000'><h2><center>"+theJob+" jobs' Difficulty Ranking</center></h2></td>\n";
		html = html + "</tr>\n";
		html = html + "<tr>\n";
		html = html + "<td width='1000'></td>\n";
		html = html + "</tr>\n";
		html = html + jobDifficultyIndex(coll);
		html = html + "</html>";
		
		
		
		return html;
	}
	
	/*
	 * Returns a ArrayList of Pairs, the firstElement of a pair is the name of the skill
	 * The secondElement of a pair is the number of occurrence of skill tags
	 */
	public ArrayList<Pair<String,Integer>> rankJobSkills(JobPostDocumentCollection coll) throws UnknownHostException{		
		HashMap<String, Integer> stats = new HashMap<String, Integer>();
		ValueComparator bvc =  new ValueComparator(stats);
		TreeMap<String,Integer> sorted_map = new TreeMap<String,Integer>(bvc);
		
		for(JobPostDocument doc:coll.getJobPostDocuments()){
			ArrayList<String> skillTags = doc.getSkillTags();
			for(String str:skillTags)
				if(stats.containsKey(str)){
					stats.put(str, (Integer)stats.get(str)+1);
				}else{
					stats.put(str,1);
				}
		}
		
		sorted_map.putAll(stats);

		
		ArrayList<Pair<String,Integer>> sortedSkillList = new ArrayList<Pair<String,Integer>>();
		for(Map.Entry<String,Integer> entry:sorted_map.entrySet())
			sortedSkillList.add(new Pair<String, Integer>(entry.getKey(),entry.getValue()));
		
		return sortedSkillList;
	}
	
	
	/*
	 * The comparator used with rankJobSkills method
	 */
	class ValueComparator implements Comparator<String> {

	    Map<String, Integer> base;
	    public ValueComparator(Map<String, Integer> base) {
	        this.base = base;
	    }

	    // Note: this comparator imposes orderings that are inconsistent with equals.    
	    public int compare(String a, String b) {
	        if (base.get(a) >= base.get(b)) {
	            return -1;
	        } else {
	            return 1;
	        } // returning 0 would merge keys
	    }
	}
	
	/*
	 * The comparator used with jobDifficultyIndex method
	 */
	class DocValueComparator implements Comparator<JobPostDocument> {

	    Map<JobPostDocument, Integer> base;
	    public DocValueComparator (Map<JobPostDocument, Integer> base) {
	        this.base = base;
	    }

	    // Note: this comparator imposes orderings that are inconsistent with equals.    
	    public int compare(JobPostDocument a, JobPostDocument b) {
	        if (base.get(a) >= base.get(b)) {
	            return -1;
	        } else {
	            return 1;
	        } // returning 0 would merge keys
	    }
	}
	
	public ArrayList<Pair<String,Integer>> calculatePercentages(ArrayList<Pair<String,Integer>> skills){
		ArrayList<Pair<String,Integer>> percentages = new  ArrayList<Pair<String,Integer>>();
		int total = 0;
		for(Pair<String,Integer> pair:skills)
			total = total + pair.getSecond();
		
		for(Pair<String,Integer> pair:skills){
			double percent = (double)pair.getSecond()/total*100;
			percentages.add(new Pair<String, Integer>(pair.getFirst(),(int)percent));
		}
		
		return percentages;
	}
	
	public static String randomColor(){
		String code = ""+(int)(Math.random()*256);
		code = code+code+code;
		int i = Integer.parseInt(code);
		return Integer.toHexString( 0x1000000 | i).substring(1).toUpperCase();
	}
	
	public String jobDifficultyIndex(JobPostDocumentCollection jobCollection) throws UnknownHostException{
//		JobPostDocumentCollection jobCollection;
//		if(theJob!=null)
//			jobCollection = MongoDBManager.getInstance().getCollectionByJobTitle(theJob);
//		else
//			jobCollection = MongoDBManager.getInstance().getCollectionByContent(content);
		
		HashMap<JobPostDocument, Integer> stats = new HashMap<JobPostDocument, Integer>();
		DocValueComparator bvc =  new DocValueComparator(stats);
		TreeMap<JobPostDocument,Integer> sorted_map = new TreeMap<JobPostDocument, Integer>(bvc);
		

		
		for(JobPostDocument doc:jobCollection.getJobPostDocuments())
			stats.put(doc, doc.getSkillTags().size());
		
		
		sorted_map.putAll(stats);
		
		ArrayList<Pair<JobPostDocument,Integer>> sortedList = new ArrayList<Pair<JobPostDocument,Integer>>();
		for(Map.Entry<JobPostDocument,Integer> entry:sorted_map.entrySet())
			sortedList.add(new Pair<JobPostDocument, Integer>(entry.getKey(),entry.getValue()));
		
		String html = "";
		
		for(Pair<JobPostDocument,Integer> pair:sortedList){
			System.out.println(pair.getSecond() + " = " + pair.getFirst().getUrl());
			html = html + "<tr>\n";
			html = html + "<td>\n(" + pair.getSecond() +")"
							+ "    "+"<a href=\"" + pair.getFirst().getUrl() + "\">" + pair.getFirst().getTitle() + "</a>"
							+"</td>\n";
			html = html + "</tr>\n";
		}


		html = html + "</table>\n</body>\n</html>\n";
		
		return html;
	}
	
	
	public static void main(String args[]) throws UnknownHostException{
		PageGenerator pg = new PageGenerator();	
		
//		JobPostDocumentCollection coll = MongoDBManager.getInstance().getCollectionByJobTitle(JobTitles.WEB_DEVELOPER);
//		System.out.println(coll.getJobPostDocuments().size());
//		for(JobPostDocument doc:coll.getJobPostDocuments()){
//			System.out.println(doc.getTitle());
//		}
	}
}
