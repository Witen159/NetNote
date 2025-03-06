
Assuming that you have [Maven](https://maven.apache.org/install.html) installed, you can run the project out-of-the-box from your terminal via

	mvn javafx:run

If you receive errors about the `commons` artifact not being available, make sure to run `mvn clean install` before.

Running the template project from within your IDE (Eclipse/IntelliJ) requires setting up OpenJFX.

First download (and unzip!) an [OpenJFX SDK](https://openjfx.io).
Make sure that the download *matches your Java JDK version*.

Then create a *run configuration* and add the following *VM* commands:

	--module-path="/path/to/javafx-sdk/lib"
	--add-modules=javafx.controls,javafx.fxml,javafx.web

Adjust the module path to *your* local download location and make sure you adapt the path
to the `lib`(!) directory (not just the directory that you unzipped)...

*Tip:* Windows uses `\` to separate path elements.

*Tip:* Make sure not to forget the `/lib` at the end of the path

*Tip:* Double-check that the path is correct. If you receive abstract error messages, like `Module javafx.web not found`
or a segmentation fault, you are likely not pointing to the right folder
