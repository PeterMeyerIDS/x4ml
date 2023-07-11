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

## Installing and starting x4ml
x4ml requires a Java Runtime Environment (JRE), version 11 or higher. It has been tested with the free and open-source OpenJDK distributions Eclipse Temurin and Azul Zulu but should work with any Java distribution.
### desktop mode (local single-user installation) 
Just grab the latest binary, save it to a folder where you have read/write access, and double-click. On the command line, enter
```
java -jar /path/to/x4ml.jar
```
The main application window (called the *Launcher*) should appear. You must select a *base directory* where all your workspaces will be created. The Launcher's main purpose is to launch the browser-based user interface.
### server mode (remote installation for multi-user mode of operation)
Server mode is fully operational but still rather rudimentary for the time being. It has not been tested in the wild, so your mileage may vary. There's no user data encryption, users cannot change their password... On the server, invoke
```
java -jar x4ml.jar 1234 /path/to/base/directory your-admin-user-name your-admin-password
```
The base directory contains the workspaces of all users. The web application is available at `http://127.0.0.1:1234` and presents users with a login screen. If you log in with your-admin-user-name and your-admin-password, you can create other users on the page `http://127.0.0.1:1234/users`. Just follow the instructions.

## Understanding the Launcher app in desktop mode
Here are some basic ideas of how to work with the Launcher and the x4ml application:
- x4ml needs the Launcher to run. If you close the Launcher, your x4ml browser windows won’t react any more. (But no data is lost.)
- The Launcher memorizes the selected base directory, so you won’t have to select it again each time you start the program. But you can change it any time.
- With Launcher, you can only work with one base directory at a time. Changing the base directory in Launcher means that any browser window with the previous base directory becomes inactive (but no data is lost). This is intentional and should help you to keep sane. You use different workspaces to keep different sets of documents apart.
- Yes, you could start the Launcher app multiple times and use different base directories for each instance. Simply don’t do that. You have workspaces for this – see below!
- Whenever you change something in a file opened through x4ml, your changes are saved immediately. There is no “save” button. This also means you can terminate the Launcher program or close an x4ml browser window any time without losing data.
- Whenever you click on the “open a new x4ml application window in browser” button in Launcher, a new x4ml browser window opens, always with the default workspace ‘playground’. If another x4ml browser window with the same workspace ‘playground’ was already open before, it becomes inactive and denies any further services – you may simply close it. No data is lost! This means that there is never more than one active browser window/tab for a given workspace.
- If there are folders inside the directory belonging to a workspace, they are ignored (with the exception of the resources subdirectory that only becomes relevant when we come to HTML). In other words, you cannot open or create files in subfolders of your workspace directory.

## Understanding workspaces
Your work with x4ml is organized in workspaces.
- A workspace is just a folder/directory – actually simply a subfolder inside the base directory – that contains files that “belong together”. Think of a workspace as an individual project of yours: You may want to just play around with some files – so you put them in the pre-defined default workspace ‘playground’. You may want to keep the files for a do-it-yourself-dictionary as a separate project – so you use another workspace you could simply call ‘my-nano-dictionary’. Different workspaces do not interfere with each other.
- You can have any number of workspaces open in parallel, in different tabs.
- In order to create, delete, manage workspaces, just click on the *current workspace* info in the upper right corner. The different options are, hopefully, self-explanatory. The *manage workspaces* menu item leads you to a rudimentary **workspace manager**, which basically allows you to delete files in your workspaces – something that you can do manually in your computer’s file explorer as well if you are in desktop mode. Clicking on a file name usually gives you a preview of its contents. Each workspace has a special resources folder that you can use for HTML stuff (see below). Using the green “+” button, you can put files in this folder. In desktop mode you can also simply put a file into a workspace folder manually.
- You may create any number of new files in the current workspace by clicking on the “select or create …” dropdowns at the top of the page and then selecting the CREATE NEW FILE entry at the end of the dropdown menu.
- If you want to delete a file in your workspace, you can either do that in x4ml’s workspace manager or (in desktop mode) use the file system program of your operating system (Windows Explorer, Apple Finder, Linux Konqueror, …) to do that.
- All files that you create with x4ml can of course also be edited with any other program. But I do not recommend that – at least you should definitely not do it while you’re working with x4ml on the exact same file.

## Basic principles of working with the x4ml browser user interface
- The x4ml application window in the browser consists of two panes, left and right. **The left pane is for working with XML files. The right pane is for working with all other kinds of files that can “do something with” the currently opened XML file on the left** (such as describe/validate its structure or extract certain information from it). 
- Each pane has an upper and a lower half. The upper half is an editor that you use to inspect and change the content of a file you opened. The lower half just displays messages or some output that relates to your file.
- The editor panes understand standard keyboard shortcuts, mostly do syntax highlighting, and, depending on the file type, the editors have some additional features, e.g., showing you errors, automatically completing your code etc.
- You can drag the blue round knobs to change the size of the two panes and their upper and lower halves.
- When there is an open file in a pane, you may click on the “action” button in that same pane to perform certain operations on the file. We will learn about the possible actions for different types of files as we go along.
- Whenever you change something in a file opened through x4ml, your changes are saved immediately; all panes are updated immediately.
- Watch out for blocked popups! Some workspace operations open a new tab or window in the browser. Some browsers (notably Chrome) block this, but issue a warning. You should then allow “popups” for x4ml and retry. I have not been able to solve this problem satisfactorily until now. x4ml checks for suppressed popups, however, and issues warnings where necessary.
