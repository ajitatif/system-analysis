# Gitlog CLI Wizard for microservice coupling analysis

This is a Spring Shell wizard to ease up using the [gitlog-converter](../gitlog-converter/README.md). The usage is interactive and quite simple:

1. Change current directory using series of `cd` command until you reach the directory where you want to work
2. Add a git repo using `add <repo_dir>` command. Add as many as you like
3. Run `extract` command with date of the first commit, and the filename to create

A typical rundown goes like this:
```bash
cd code/
add service-a
add service-b
add service-c
extract 2022-01-01 /tmp/analysis.csv
```

this will run the `git log` command and then use the `gitlog-converter` for all 
the `services` picked, along with their directory name. The output will contain 
the header line in the CSV.  

The tab completion works somewhat.

## TODO

* [ ] Make a tab completion that actually works as intended
* [ ] Add some unit tests damn it!
* [ ] Turn on/off the blaming mode (adding the author of the commit message to the CSV)