crawljax-functional-testing-suite
=================================

An automated framework for crawling websites using Crawljax. To enable distributed crawling, a sql server is required.

Compile
=================================
- install Crawljax v4.0.0 using Maven
- To compile crawljax-functional-testing-suite use:
- mvn clean compile assembly:single
- The binary will be located in target/crawljax-functional-testing-suite-{version}

Usage
=================================
The application allows the following arguments:
* -d, --distributor   Runs the commandline interface of the distributor.
* -f, --flush             Flushes the website-file to the server. Nothing is crawled.
* -l, --local             Do not use server-functionality. Read the website-file and crawl all.
* -w, --worker        Setup computer as slave/worker, polling the db continuously.

