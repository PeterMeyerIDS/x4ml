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

## Editing XML files (left pane, file name ends in ‘.xml’)
- If you type a start tag, the editor puts in the end tag as soon as you typed the closing bracket >.
- If you hit ENTER to start a new line, the indentation of the previous line is preserved. This is not always what you want, but most of the time it is convenient.
- XML files are easier to read when indentation is used to clearly show the structure. The action format XML can be used to automatically create a well-readable indentation. It’s not always perfect; if you do not like the outcome, just use the undo action (or the undo shortcut Ctrl/Command-z).

## Editing DTD files (right pane, file name ends in ‘.dtd’)
- Per default, x4ml uses the DTD file to validate (=check the structure of) the XML file currently open in the left pane and shows you the results in the lower half. If you want to validate all your XML files in your workspace at once, just use the action *show output for all XML files*. Note that this is not a permanent setting – as soon as you change something in either the DTD or the current XML file, you again get the validation results for the current XML only.
- Other than that, nothing special, just syntax highlighting.

## RelaxNG files, compact notation (right pane, file name ends in ‘.rnc’)
- Per default, x4ml uses the RelaxNG file to validate (=check the structure of) the XML file currently open in the left pane and shows you the results in the lower half. If you want to validate all your XML files in your workspace at once, just use the action *show output for all XML files*. Note that this is not a permanent setting – as soon as you change something in either the RelaxNG or the current XML file, you again get the validation results for the current XML only.
- Other than that, nothing special – alas, no syntax highlighting in the RelaxNG code.
- Inofficially, also the XML notation of RelaxNG is supported (file name ends in ‘.rng’).

## XPath/XQuery files (right pane, file name ends in ‘.xpath’)
- You can only type one XPath expression per file (but this might consist of several expressions separated by commas, which returns a sequence of results). The editor shows you the result of applying this expression to the XML on the left side. Your XPath expression is a question about the XML on the left hand side, and you’re shown the answer.
- If you like, you can use a comment, which looks (: like this :) in XPath, as a “storage” for, well, comments and other stuff, e.g. any number of XPath expressions you tried or want to try. You may put comments before or after your XPath expression.
- Per default, x4ml applies the XPath to the XML file currently open in the left pane and shows you the results in the lower half. If you want to do this for all of your XML files in your workspace at once such that the XPath expression is applied to each XML file in turn, just use the action *show output for all XML files*. Note that this is not a permanent setting – as soon as you change something in either the XPath file or the current XML file, you get the results for the current XML only again.
- You can apply your XPath expression to all your XML documents, not individually but taken together, using the action *evaluate XPath query on all XML docs* ("database mode"). Technically, this means that the query / returns a sequence such that for each XML document in the workspace, all children of the root node form one item in the sequence.
- In your XPath files, you may actually use not only XPath expressions, but also any expressions in XQuery (version 3.1), of which the XPath language is a subset.

## HTML files (right pane, file name ends in ‘.html’)
- The HTML editor has automatic end tag completion, like the XML editor; and it gives visual feedback if your HTML is not well-formed, i.e. if you made a syntax mistake.
- The output in the lower half of the right pane is the HTML as rendered in a browser.
- You can include XPath expressions into your HTML code. The expressions must be enclosed in double curly brackets (‘moustaches’), like this: {{/entry/headword}}. The trick is that x4ml executes that expression with respect to the XML file on the left and puts the result where the moustaches are. This way, your HTML document becomes a **template** that is filled with content from your XML file. (This is similar to an important technology called XSLT, but hopefully easier.)
- You can use the action *insert code to iterate HTML elements* to construct multiple HTML elements (or sequences of elements) from multiple XML elements. For example, you may have multiple <definition> elements in your XML, corresponding to different word senses. If you want, in your HTML, a `<p>` element (a paragraph) for each XML `<definition>` element, you just invoke the above action exactly at the place in your HTML where the `<p>`’s should go and something like the following is inserted:
```
    <!-- repeat for all $x in /your/xpath -->
           (insert HTML instead of this text; don't forget to use different variables $x, $y, ... if you nest repeated sections; don't forget to always use the matching variable name in the "end repeat" part)
    <!-- end repeat for all $x -->
```
Now you change this to something like the following, again using ‘moustaches’ to include XPath expressions – do not forget the `$` that is mandatory for the variable stuff:
```
	  <!-- repeat for all $x in //definition  -->
           <p>{{ $x/text() }}</p>
    <!-- end repeat for all $x -->
```
Your HTML output will now indeed contain, for each `<definition>` in the XML, a `<p>` element that contains the text of the `<definition>` element: For each result obtained from applying the XPath expression `//definition`, i.e. for each `<definition>` element in the XML document, the HTML stuff `<p>{{ $x/text() }}</p>` is inserted into the final HML document, where the variable `$x` stands for the currently treated XPath result, i.e., the current `<definition>` element. This will produce
```
   <p>some definition</p><p>other definition</p>
```
if your xml contains the elements `<definition>some definition</definition>` and `<definition>other definition</definition>`.
You may nest multiple “repeat-sections” inside each other. You must use different variables  for these nested sections.
- There’s a very similar action, insert code to iterate HTML elements, with separator. It repeats HTML stuff, but additionally, between the repeated stuff, some separator stuff (which could even be something like `<br>`) is inserted:
```
	  <!-- repeat for all $x in //definition separated by '; '  -->
           <i>{{ $x/text() }}</i>
    <!-- end repeat for all $x -->
```
This will produce
```
    <i>some definition</i>; <i>other definition</i>
```
if your xml contains the elements `<definition>some definition</definition>` and `<definition>other definition</definition>`.
- Per default, x4ml uses your HTML file with the XML file currently open in the left pane and shows you the results of that in the lower half. If you want to do this for all of your XML files in your workspace at once such that the HTML template is applied to each XML file separately, just use the action *show output for all XML files*. Note that this is not a permanent setting – as soon as you change something in either the XPath file or the current HTML file, you get the results for the current HTML only again. – This action is probably less interesting than the dictionary action discussed below.
- HTML is the language of browsers. If you want to see your HTML (with the moustaches and repeating stuff properly processed for the currently opened XML!) in a separate browser tab/window, you invoke the action *render HTML in browser*. A separate tab or window will open in your browser. You can leave this separate tab/window open; if you later change your HTML, the rendered browser content will update automagically. For different XML files different browser tabs are opened.
- You can check whether your HTML (with the moustaches and repeating stuff properly processed for the currently opened XML!) is valid, using an external validation service, by invoking the action *validate HTML*. This action requires an Internet connection.
- You can try to make your HTML prettier, with convenient indentations, through the action *format HTML*. This is a fascinating function, because it even works when your HTML is not well-formed, but it may change the content and the structure of your file considerably, so do check the result and be prepared to undo the formatting!
- You can use an HTML file with moustaches etc. to build your own mini-dictionary, where the HTML is applied to all the XML files in your workspace. Just invoke the *show as dictionary* action. In order to get a custom heading of your online dictionary, try the *set name of dictionary* action. x4ml memorizes the dictionary name for your workspace. Per default, the headword list on the left of your dictionary is filled with the names of your XML files (excluding the .xml suffix part). You can tell x4ml how to find the headword in your XMLs with the action *set XPath for dictionary headwords*. 
- If you want to include external files, e.g. multimedia stuff, into your HTML, that is possible. If you have an image called *funny.jpg* and you want to include it, create a folder named resources (no other name allowed!) directly inside your workspace folder and put the image there. You can do this using the workspace manager (which automatically creates the resources folder) – or manually using your computer’s file explorer if you are using x4ml in Desktop mode. Now you can use, for example, `<img src="resources/funny.jpg">` in your HTML file to include the image there.

