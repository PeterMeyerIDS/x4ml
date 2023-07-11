# x4ml: A Teaching Tool for XML-based lexicographical data modeling and (HTML) presentation

**x4ml** is "**X**ML **4 M**asterful **L**exicographers". This is the first public release of x4ml, in honor of Gerd Hentschel on the occasion of his 70th birthday. This release accompanies the Festschrift publication
> Peter Meyer (2023): 'Einsatz von EDV und Mikrocomputer in Lehrveranstaltungen zur digitalen Lexikografie'. In: Hauke Bartels, Thomas Menzel, Jan Patrick Zeller (eds.): *Einheit(en) in der Vielfalt von Slavistik und Osteuropakunde. Prvdentia Regnorvm Fvndamentvm.* Lausanne-Berlin-Bruxelles-Chennai-New York-Oxford: Peter Lang.

## Welcome to x4ml ##
x4ml is a didactic software tool aimed at IT novices who want to learn about XML/HTML-related technologies. It has been designed with aspiring lexicographers in mind, but may be useful for other purposes, too. With x4ml, you can
- create and edit XML documents
- write DTD or RelaxNG schemas to validate your XML documents
- evaluate XPath/XQuery expressions on your XML documents
- create HTML pages from your XML documents using a simple templating system

Special features of x4ml:
- two execution modes: run it locally on your computer or deploy it to a server
- minimalist deployment: one binary file is all you need (well, except a Java Runtime)
- it just works: no complex manual 'connecting' your XML to a schema or stylesheet
- everything in one place: do everything in one single non-intrusive user interface
- instantaneous feedback: after any change, all views, validation messages, HTML renderings, … are updated automagically
- simple file management: organize multiple projects in workspaces (aka folders) – no need to manually save your data
- create a mini-dictionary from XML documents and an HTML template

## Installing x4ml ##
x4ml requires a Java Runtime Environment (JRE), version 11 or higher. It has been tested with the free and open-source OpenJDK distributions Eclipse Temurin and Azul Zulu but should work with any Java distribution.
### desktop mode (local installation) 
Just grab the latest binary, save it to a folder where you have read/write access, and double-click. On the command line, enter
```java -jar /path/to/x4ml.jar```
The main application window should appear – see below. Its main purpose is to launch the browser-based user interface.
