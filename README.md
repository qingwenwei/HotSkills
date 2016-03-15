#<center>COMP4601Project</center>

###Updated 2016-3-15
This project was written __April 2014__ as part of my school project for the course of COMP4601 supervised by Prof.Tony White. Some of the codes were pretty immature looking back from now. But it was personally a milestone I had made in my programming career path.


##Introduction
Our project is a text summarization system which collects job ad web pages on some of the major job search websites and data collected from the Internet would be used as source for analyzing the employers' preference for certain computer science skills. The aim of our project is to help computer science graduates set up goals for learning new techniques, or simply to feed people's curiosity.

##Requires
+ Jsoup 1.7.3
+ MongoDB 2.11.3
+ Jersey 1.17
+ OpenCloud
+ Chart4j 1.3

##How it works
###MongoDB
In order to store documents, set up a database in MongoDB called __JobPostDB__ and a collection called __JobPost__.

###Add A New Job Category 
In SeedConfig class under COMP4601ProjectJobAdCollector project, simply add a new seed with a job title as tag to the urlSet return by getSeeds method.

Example:

Add new job category into database

	public class SeedConfig{
		public static ArrayList<String> titleFilter = new ArrayList<String>();
		
		public static UrlSet getSeeds(){
		
			//This is the collection of root URLs
			UrlSet urlSet = new UrlSet();			
			
			//The URL is the search result of the job
			Url seed = new Url("http://www.workopolis.com/jobsearch/web-programmer-jobs");
			
			//Every job post URL in workoplis.com contains "jobsearch/job"
			//We are not interested in other links
			seed.addMatcher("jobsearch/job");
			
			//This is the new job category
			seed.setTag("Web Developer");			
			
			urlSet.addUrl(seed);
		}
	}
	
When JobAdCollector runs, it will call getSeeds method in SeedConfig class to obtain the urlSet as the URL roots.

To show the category in the homepage of the web service

	public class PageGenerator {
		public String generateIndexPage(){
		
			ArrayList<String> jobRanks = new ArrayList<String>();
			
			jobRanks.add(...);
			jobRanks.add(...);
			
			jobRanks.add("Web Developer"); // New category

			. . .
		}
	}

##How To Use it
1. Run the class main on server in the COMP4601ProjectWebService under edu.carleton.COMP4601.web package
2. Directing a browser to http://localhost:8080/comp4601/rest/ep would display the homepage of the top 10 job skills rankings for the job categories.
3. To see more job skills preferred by employers with respect to a particular job, click on the title of the job and a skill keyword word cloud would be displayed.
4. Under the skill word cloud, there is a job difficulty ranking based on the number of skill keyword occurrence.
5. Click on a job post link would direct you to the actual job post in the job search website.
6. To search keyword in the job post documents in the database, 
use rest/ep/__job__/__keyword__, for example: rest/ep/web_developer/toronto.
7. In the search result page, the pie chart and the top 10 skill list will reflect the search result based on the two given criteria above.

##Design Pattern
The whole project is consist of three parts:

+ WebCrawler
+ COMP4601ProjectJobAdCollector
+ COMP4601ProjectWebService

###WebCrawler
is a simple self-customized web crawler program which is consist of two main classes under edu.carleton.webcrawler.main:

+ UrlCollector
+ WebPageCollector

####UrlCollector
This class is to collect URLs with specified patterns in a given web page. The UrlCollectorOnFinishListener is called when the URL collection process is finished and a UrlSet is returned. UrlSet is simply a URL string container. setNumOfThreads method is used to set the number of thread involved in the process of collecting web URLs.

####WebPageCollector
This class is to collect web page content based on the given UrlSet. It uses Jsoup to parse level 1 to 4 of HTML titles and the paragraphs from the web page. The WebPageCollectorOnFinishListner is called when it finishes collecting web pages.

##COMP4601ProjectJobAdCollector
Some of the important classes:

+ JobAdCollector
+ SeedConfig
+ TextAnalyzer
+ MongoDBManager

JobAdCollector is the implementation of the actual process of job ads collection. It calls the getSeed method in SeedConfig to get the seed URLs with specified matchers(patterns that the URL might contain). The WebPageCollectorOnVisitListener is called each time when a new page is visited and a Page object is returned. Page's content will be tokenized and the program will analyze each token using TextAnalyzer to check if a certain job skill keyword exits in the page content. If a skill keyword or its other forms is found, program will accumulate it to jobPost. After checking through the whole page content, the page is added to MongoDB using MongoDBManager.

#Statistical Methods

###Skill Keyword Accumulative Method
Once a skill keyword occurs in the page content, the skill keyword will be put to skillTags in JobPostDocument. The number of this skill required by this job will be count as 1. The same skill keyword will not be count twice.

###Job Post Search Accuracy
The job search engines provided by the job search websites does not always provide us the correct result. Sometimes, when you search for __"web developer"__, it gives you web developer jobs __AND graphic designer jobs__ which is another job category. So in order to get more precise statistics, we have to limit the HTML page title to some keywords that are associated with __"web developer"__, such as "web designer","web programmer" etc.

###Job Difficulty Ranking
The ranking is based on the number of skill keyword occurrence in the job ad. Because we think if there are many job skills are mentioned in a job ad, it would likely be a difficult job.

##Future Works
###High Frequency Skill Set
Some skills have the higher chance appear in the job post. For example: HTML and CSS. We are interested in what the most frequent skill sets are, so that we can see the relationship amongst skills.

###Job Title Variation Dictionary
We need a dictionary for dealing with the job name varieties. For example, A job post is found with title "Junior Web Programmer in Ottawa" in its HTML title would be considered as a Web Developer job.

###External Job Skill and Skill Name Variation Dictionary
We need to have a external dictionary for the skills and their corresponding name varieties. Because having an external configuration is easier for users to insert and modify the target skill keywords they are interested in.