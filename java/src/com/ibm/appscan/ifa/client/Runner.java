/**
 * ©  Copyright HCL Technologies Ltd. 2017.  All Rights Reserved.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */

package com.ibm.appscan.ifa.client;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.wink.json4j.JSONObject;

import com.ibm.appscan.ifa.client.action.ApplyIfa;
import com.ibm.appscan.ifa.client.action.DeltaNew;
import com.ibm.appscan.ifa.client.action.DeltaResolved;
import com.ibm.appscan.ifa.client.action.HealthCheck;
import com.ibm.appscan.ifa.client.action.IfaAction;
import com.ibm.appscan.ifa.client.action.SolutionGroup;
import com.ibm.appscan.ifa.client.action.Version;


public class Runner implements IProgressIfaListener{
	private Options m_options=new Options();
	private String[] m_args;
	private String m_current_title="";
	private String m_current_stage=Messages.getMessage("progress.initial");
	private static IfaAction s_action;
	public static void main(String[] args) throws IfaClientException {
		try {
			Runner r=new Runner(args);
			r.run();
		} catch (Exception e){
		}
	}
	public Runner(String[] args) {
		m_args=args;
		EventManagerIfa.addIfaProgressListener(this);
		OptionGroup g=new OptionGroup();
		g.setRequired(false);

		Option o=new Option("h","host",true, Messages.getMessage("option.single.host"));
		o.setArgName("HOST");
		m_options.addOption(o);
		m_options.addOption(new Option("v", "version",false, Messages.getMessage("option.version")));

		m_options.addOption(new Option("c", "heath-check",false, Messages.getMessage("option.health.check")));


		o=new Option("t", "target-dir",true, Messages.getMessage("option.target"));
		o.setArgName("DIR");
		m_options.addOption(o);
		o=new Option("f", "returned-file-name",true, Messages.getMessage("option.target.file.name"));
		o.setArgName("FILE_NAME");
		m_options.addOption(o);
		o=new Option("s", "accept-self-signed",false, Messages.getMessage("option.invalid.cert"));
		m_options.addOption(o);
		g=new OptionGroup();
		o=new Option("i", "run-ifa",true, Messages.getMessage("option.ifa"));
		o.setArgName(Messages.getMessage("option.arg.assmt"));
		g.addOption(o);
		o=new Option("g", "get-groups",true, Messages.getMessage("option.groups"));
		o.setArgName(Messages.getMessage("option.arg.assmt"));
		g.addOption(o); //TODO removed for work item 101142
		
		o=new Option("n",Messages.getMessage("option.delta.new"));
		o.setArgs(2);
		o.setLongOpt("new-delta");
		o.setValueSeparator(' ');
		o.setArgName(Messages.getMessage("option.delta.baseline"));
		g.addOption(o);
		
		o=new Option("r",Messages.getMessage("option.delta.resolved"));
		o.setArgs(2);
		o.setLongOpt("resolved-delta");
		o.setValueSeparator(' ');
		o.setArgName(Messages.getMessage("option.delta.baseline"));
		g.addOption(o);
		
		m_options.addOptionGroup(g);
		
		
		//o=new Option("d", "debug",false, Messages.getMessage("option.debug"));
		//m_options.addOption(o);

	}

	public void run() throws IfaClientException{

		CommandLineParser parser = new BasicParser();
		CommandLine line = null;
		if (m_args==null || m_args.length==0){
			System.out.println(Messages.getMessage("err.no.file.supplied"));
			help();
			return;
		}


		try {
			boolean ifa=false;
			boolean solution=false;
			boolean delta_new=false;
			boolean delta_resolved=false;
			boolean debug=false;
			boolean allow_self_signed=false;
			boolean version=false;
			boolean health=false;
			File target =null;
			File f =null;
			File baseline = null;
			String target_file_name=null;
			line= parser.parse(m_options, m_args);
			ArrayList<String>hosts=new ArrayList<String>();
			if (line.hasOption("f")){
				target_file_name= line.getOptionValue("f");
			} 
			if (line.hasOption("t")){
				target= new File(line.getOptionValue("t"));
				target.mkdirs();
			} 
			if (line.hasOption("i")){
				ifa=true;
				f=getFileFromOption(line,"i");
			}

			if (line.hasOption("g")){
				solution=true;
				f=getFileFromOption(line,"g");
			}
			if (line.hasOption("v")){
				version=true;
			}
			if (line.hasOption("c")){
				health=true;
			}
			if (line.hasOption("n")){
				delta_new=true;
				ArrayList<File>files=getFilesFromArg(line, "n");
				baseline=files.get(0);
				f=files.get(1);
			}
			if (line.hasOption("r")){
				delta_resolved=true;
				ArrayList<File>files=getFilesFromArg(line, "r");
				baseline=files.get(0);
				f=files.get(1);
			}
			if (line.hasOption("h")){
				hosts.add(line.getOptionValue("h"));
			} else {
				hosts.add("http://localhost:9080");
			}

			if (line.hasOption("d")){
				debug=true;
			}
			if (line.hasOption("s")){
				allow_self_signed=true;
			}

			if (version){
				s_action=new Version(allow_self_signed, hosts);
				System.out.println(Messages.getMessage("host.version", s_action.getHost(),((Version)s_action).getVersion()));
			}
			if (health){
				s_action=new HealthCheck(allow_self_signed, hosts);
				System.out.println(Messages.getMessage("host.health", s_action.getHost(),new JSONObject(((HealthCheck)s_action).getHealth()).toString(true)));
			}
			if (ifa){
				long start=System.currentTimeMillis();
				s_action=new ApplyIfa(f,target,target_file_name,debug,allow_self_signed,hosts);
				File ifa_file=s_action.run().get(0);
				System.out.println();
				
				printIFAAssessmentDetails(ifa_file,start);
				
				System.out.println(Messages.getMessage("ifa.assessment", ifa_file.getPath()));
			}
			if (solution){
				long start=System.currentTimeMillis();
				s_action=new SolutionGroup(f, target,debug,allow_self_signed, hosts);
				ArrayList<File> ret=s_action.run();
				printSolutionDetails(ret,start);
			}

			if (delta_new){
				long start=System.currentTimeMillis();
				s_action=new DeltaNew(f, target,target_file_name, baseline,debug, allow_self_signed,hosts);
				ArrayList<File> ret=s_action.run();
				printDiffResults("diff.details.new",f, baseline, ret.get(0),start);
			}
			if (delta_resolved){
				long start=System.currentTimeMillis();
				s_action=new DeltaResolved(f, target, target_file_name,baseline,debug, allow_self_signed,hosts);
				ArrayList<File> ret=s_action.run();
				printDiffResults("diff.details.resolved",f, baseline, ret.get(0),start);
			}

		} catch (ParseException e) {
			System.out.println();
			System.out.println(Messages.getMessage("err.parse.args", e.getLocalizedMessage()));
			help();
		} catch (NumberFormatException nfe){
			System.out.println();
			System.out.println(Messages.getMessage("err.num.format.ex",
					line.getOptionValue("p")));
			help();
		} catch (Exception e){
			System.out.println();
			System.out.println();
			//e.printStackTrace();
			System.out.println(Messages.getMessage("err.process.command",
					(s_action!=null)?s_action.getClass().getName():Messages.getMessage("action.null"),e.getLocalizedMessage()));
			help();
		}
	}

	private File getFileFromOption(CommandLine line, String option){
		File ret=new File(line.getOptionValue(option));
		System.out.println(Messages.getMessage("processing", 
				ret.getName()));
		if (!ret.exists()){

			System.out.println(Messages.getMessage("err.file.not.exist",ret.getAbsolutePath()));
			help();
		}
		return ret;
	}
	
	private ArrayList<File> getFilesFromArg(CommandLine line, String option){
		String[] names=line.getOptionValues(option);
		ArrayList<File>ret=new ArrayList<>();
		for (String name:names){
			File f=new File(name);
			System.out.println(Messages.getMessage("processing", 
					f.getName()));
			if (!f.exists()){

				System.out.println(Messages.getMessage("err.file.not.exist",f.getName()));
				help();
			}
			ret.add(f);
		}
		
		return ret;
	}
	
	private void printDiffResults(String head,File orig, File baseline, File result, long start) throws FileNotFoundException, XMLStreamException {
		System.out.println();
		System.out.println(Messages.getMessage("diff.details.head"));
		System.out.println(Messages.getMessage("diff.details.orig",orig.getPath()));
		printAssessmentDetails(orig);
		System.out.println(Messages.getMessage("diff.details.baseline",baseline.getPath()));
		printAssessmentDetails(baseline);
		System.out.println(Messages.getMessage(head,result.getPath()));
		printAssessmentDetails(result);
		SimpleDateFormat sdf = new SimpleDateFormat("mm:ss.SSS");
		System.out.println(Messages.getMessage("time.diff", sdf.format(System.currentTimeMillis()-start)));
	}
	private void printSolutionDetails(ArrayList<File> solution_dir, long start) {
		System.out.println();
		System.out.println(Messages.getMessage("solution.details.head", solution_dir.size()));
		SimpleDateFormat sdf = new SimpleDateFormat("mm:ss.SSS");
		System.out.println(Messages.getMessage("time.groups", sdf.format(System.currentTimeMillis()-start)));

		/*for (File f:solution_dir){
			Assessment a=new Assessment(f);
			System.out.println(Messages.getMessage("solution.details", a.getDetailsNode().getApplication(),
					f.getAbsolutePath()));
			printAssessmentDetails(a);
		}*/
	}
	private void printIFAAssessmentDetails(File f, long start) throws FileNotFoundException, XMLStreamException {
		System.out.println();
		SimpleDateFormat sdf = new SimpleDateFormat("mm:ss.SSS");
		System.out.println(Messages.getMessage("time", sdf.format(System.currentTimeMillis()-start)));
		printAssessmentDetails(f);
	}

	private static void printAssessmentDetails(File file) throws FileNotFoundException, XMLStreamException{
		AssessmentDetails ad=new AssessmentDetails(file);
		System.out.println(Messages.getMessage("details",
				ad.getApplicationName(),
				ad.getFindingCount(),
				ad.getHighFindingCount(),
				ad.getMedFindingCount(),
				ad.getLowFindingCount()));
		System.out.println(Messages.getMessage("details.excluded", 
				ad.getExcludedFindingCount()));
	}
	public void help() {
		System.out.println(getHelpText());
		System.exit(0);
	}

	public String getHelpText(){
		HelpFormatter formater = new HelpFormatter();
		StringWriter sw=new StringWriter();
		PrintWriter pw=new PrintWriter(sw);
		pw.println();
		formater.printHelp(pw, 80, "java -jar sdk-client.jar", "", m_options, 
				2, 4, "", true);
		String ret=sw.toString();
		return ret;
	}

	@Override
	public void handleProgress(int progress) {
		if (progress<=100){
			System.out.print("\r"+new String(new char[200]).replace('\0', ' '));
			if (m_current_stage.equals("Completed.")) {
				System.out.print("\r"+Messages.getMessage("progress",m_current_title,m_current_stage,100));
			} else {
				System.out.print("\r"+Messages.getMessage("progress",m_current_title,m_current_stage,progress));
			}
		}
	}

	@Override
	public void handleProgressTitle(String current) {
		m_current_title=current;
	}

	@Override
	public void handleProgressStage(String stage) {
		m_current_stage=stage;
	}
	@Override
	public void handleComplete(String completed_url) {
		System.out.print("\r"+new String(new char[200]).replace('\0', ' '));
		System.out.print("\r"+Messages.getMessage("progress.complete",m_current_stage));
		System.out.println();
		System.out.println(Messages.getMessage("job.completed.url", completed_url));
	}
}
