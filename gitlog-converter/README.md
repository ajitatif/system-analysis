# GitLog Converter

This tool extracts the JIRA ticket numbers from git logs. The inspiration for this tool came from the book 
_Your Code as a Crime Scene_ from [Adam Thornhill](https://github.com/adamtornhill) and his tool _Code Maat_.  

As wonderful as the tool is, it focuses on one repository only, which is not the case for most of the microservice 
architectures. I wanted to analyse the _Temporal Coupling_ of our microservices and thought it would be cool to do 
it using Git logs, just as Thornhill did.  

Git logs are awesome and can reveal many things about your SDLC processes (as Thornhill proves in the book many times), 
yet you can only get logs for repositories one by one. This is why we will need to use the tool multiple times for each 
of our repositories.

## Usage

The tool needs git logs to work on. We only need the summary, (probably) the author and the service in a macro level, so 
the git command to get the log is:

```shell
git log --pretty=format:"%s|%al" --date=short --after=YYYY-MM-DD > /path/to/logfile
```

This will create the logs that we need - `%s` is the summary, which is the commit message, 
`%al` is the local part of the email address of the author. The emails have some prefixes but the tool takes care of it.  

Next up, using the GitLog Converter itself. Assuming you have built a JAR file with name `gitlog-converter.jar`:

```shell
java -jar gitlog-converter.jar gitlog-file service-name > gitlogs.csv
```
(remember to use `>>` for the following services to append the results into the same CSV file)

This will result in lines like:

```text
ABC-487;coder-one;service-name
Merge pull request #12 from org/feature_branch;coder-one;service-name
Merge branch 'master' of https://github.com/org/service into feature_branch;coder-two;service-name
ABC-447;coder-two;service-name
CDE-12;coder-three;service-name
```
So, the success of the tool tightly depends on the consistency of the practices of the team, and feature branches are 
most helpful. But you should be OK as long as you don't have dozens of microservices to check.

Now, repeat the `git log` and `java -jar` commands until you're done with all the microservices

## Enriching Data

### Jira Data Association

The CSV file now contains the ticket key, the author, and the service that was changed for the ticket. 
Then we need to figure out which of our services had to change for each one of the features or projects our teams 
undertook. To figure this out, we also need the ticket information from JIRA. For me, the best way of doing this was 
importing the CSV data into Google Sheets, where I added JIRA Integration. You can find it under _Extensions_ menu, 
look for _Jira Clouds for Sheets_, create a new Sheet within the worksheet and work your `JQL` magic there. The fields 
I use are `Key`, `Parent`, and `Summary` so that I can group the tickets (and commits) under their respective epics.

### Enriching Result Set

So we have the ticket key, the author and the service in our imported CSV sheet. Let's create a header row to the top 
and name the three columns _Ticket_, _Author_, and _Service_. I went ahead and created two more 
columns at the start of the sheet: _Epic_ and _Epic Name_. The okayest formula for _Epic_ would be:

```text
=VLOOKUP(C2,'JIRA Tickets'!$A$2:$B$3107, 2, false)
```
where `JIRA Tickets` is the sheet with Jira ticket information, and the range being the whole table. 
This looks up the Jira ticket key from the respective sheet, and extracts the parent ticket

For _Epic Name_, it's similar, but getting the summary of the epic this time:

```text
=VLOOKUP(A2,'JIRA Tickets'!$A$1:$D$3000, 3, false)
```

## Interpretation

This is solely up to you actually, but the first things I wanted to check were most changed services, 
epics vs the services changed, and temporal coupling among services.  

The first two were as easy as a pivot table, but temporal coupling is a bit trouble for me - I don't 
know Google Sheets enough to magically put it together, but still got good insights from _epics vs. services changed_ 
pivot.

So I think that's about it for now from this tool. I'll try to add a few more features for better Jira ticket key 
extraction when I have time.

## Contribution

You want to contribute? That's great! There are a few things I don't want to have in this tool though, and I 
hope you appreciate:

- Since this is just a simple tool which deals with some regex, I want it to be as lightweight as possible, so no dependencies unless absolutely required
- This tool is meant to be free, so I want the tool to remain in GPL licences

I think that's all for now. Thanks.
