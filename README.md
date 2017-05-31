# IFA Server Documentation
[On-line Documentation](http://www-01.ibm.com/support/docview.wss?uid=swg22004133)

# ifa-client
Client REST tool for Intelligent Finding Analytics (IFA) Server

Simple REST wrapper which simplifies the workflow for submitting an assessment for processing.

Main operations:
1) IFA
2) Fix grouping
3) Delta analysis

Secondary operations:
1) Health check
2) Version
3) Usage statement

# Prerequisites

- Java 1.8
- Gradle 2.2.1+

# Building
Run `gradle` in the java directory to build the jar.
`cd java`
`gradle`

Produces a jar in build/libs.

# Usage
Run the jar to print out usage: `java -jar ifa-client.jar`

```sh
java -jar build/libs/ifa-client.jar 
Please enter an argument for the file or directory you wish to send to the IFA server

usage: java -jar ifa-client.jar [-c] [-g <Assessment File> | -i <Assessment
       File> | -n <Baseline Assessment> <New Assessment> | -r <Baseline Assessment>
       <New Assessment>] [-h <HOST>]    [-s] [-t <DIR>] [-v]
  -c,--heath-check                                              Performs a
                                                                health check of the host
  -g,--get-groups <Assessment File>                             Compare the
                                                                assessment(s) for -a with this baseline
  -h,--host <HOST>                                              Specify the
                                                                server host. Requires the protocol, host and port number to be specified - eg
                                                                http://server_1:9080 Default value: http://localhost:9080
  -i,--run-ifa <Assessment File>                                Apply IFA filtering to specified assessment.
  -n,--new-delta <Baseline Assessment> <New Assessment>         Delta new findings. Supply the baseline assessment.
  -r,--resolved-delta <Baseline Assessment> <New Assessment>    Delta resolved
                                                                findings. Supply the baseline assessment.
  -s,--accept-self-signed                                       Accept invalid
                                                                and self signed certificates.
  -t,--target-dir <DIR>                                         Specify the
                                                                target directory to place the IFA file. This option should be used to place the
                                                                results into a fresh directory. Files of the same name will be overwritten.
  -v,--version                                                  Prints the
                                                                version of the supplied host.
```

# IFA
To run IFA on an assessment use `java -jar ifa-client.jar -i <assessment file>`

Saves the new assessment using <Application name>_IFA.ozasmt

## Example:
```sh
java -jar ifa-client.jar -i webgoat.ozasmt 
Processing: webgoat.ozasmt
Job submitted. ID: 6b61564a-129c-40c7-a7f4-8e858b657eb6
Processing webgoat.ozasmt for IFA - Completed.:100%                                                                                                                                                     
Verifying returned payload
Completed processing Processing webgoat.ozasmt for IFA                                                                                                                                                  
Job completed. URL: http://localhost:9080/rest/ifa/v1/triaged-assessments/6b61564a-129c-40c7-a7f4-8e858b657eb6


Time taken to apply IFA: 00:06.619
Details for WebGoat-Legacy-archive_5_4:
	Total Findings: 136
	High: 42
	Medium: 8
	Low: 86
	Excluded: 1,460
IFA assessment path:./WebGoat-Legacy-archive_5_4_IFA.ozasmt

```
# Fix grouping
To run fix grouping on an assessment use `java -jar ifa-client.jar -g <assessment file>`

## Example:
```sh
java -jar ifa-client.jar -g WebGoat-Legacy-archive_5_4_IFA.ozasmt
Processing: WebGoat-Legacy-archive_5_4_IFA.ozasmt
Job submitted. ID: 3267bd00-d4cd-4921-b96a-89495b5a24d0
Processing WebGoat-Legacy-archive_5_4_IFA.ozasmt for solution groups - Completed.:100%                                                                                                                  
Verifying returned payload
Completed processing Processing WebGoat-Legacy-archive_5_4_IFA.ozasmt for solution groups                                                                                                               
Job completed. URL: http://localhost:9080/rest/ifa/v1/fix-group-assessments/3267bd00-d4cd-4921-b96a-89495b5a24d0

Fix Groups: 25
Time taken to determine solution groups: 00:01.482

```

# Delta analysis
1) New findings run `java -jar ifa-client.jar -n <baseline assessment file> <new assessment file>`
2) Resolved findings run `java -jar ifa-client.jar -r <baseline assessment file> <new assessment file>`

## Example
```sh
java -jar ifa-client.jar -r webgoat.ozasmt WebGoat-Legacy-archive_5_4_IFA.ozasmt
Processing: webgoat.ozasmt
Processing: WebGoat-Legacy-archive_5_4_IFA.ozasmt
Job submitted. ID: 6a852b90-f427-46c1-9e67-30d0f7af1058
Processing diff against webgoat.ozasmt - Completed.:100%                                                                                                                                                
Verifying returned payload
Completed processing Processing diff against webgoat.ozasmt                                                                                                                                             
Job completed. URL: http://localhost:9080/rest/ifa/v1/delta-assessments/6a852b90-f427-46c1-9e67-30d0f7af1058

Diff Results:
Original: WebGoat-Legacy-archive_5_4_IFA.ozasmt
Details for WebGoat-Legacy-archive_5_4:
	Total Findings: 136
	High: 42
	Medium: 8
	Low: 86
	Excluded: 1,460
Baseline: webgoat.ozasmt
Details for WebGoat-Legacy-archive_5_4:
	Total Findings: 1,410
	High: 156
	Medium: 149
	Low: 1,105
	Excluded: 0
Resolved: ./WebGoat-Legacy-archive_5_4_resolved.ozasmt
Details for WebGoat-Legacy-archive_5_4:
	Total Findings: 1,460
	High: 118
	Medium: 139
	Low: 1,203
	Excluded: 0
Time taken to determine diff: 00:02.051
```

# Using remote host
To use a remote host add the -h option `java -jar ifa-client.jar -h http://remote:9080 -i webgoat.ozasmt`
