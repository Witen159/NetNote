# CSEP Template Project

This repository contains the template for the CSE project. Please extend this README.md with sufficient instructions
that will illustrate for your TA and the course staff how they can run your project.

To run the template project from the command line, you either need to
have [Maven](https://maven.apache.org/install.html) installed on your local system (`mvn`) or you need to use the Maven
wrapper (`mvnw`). You can then execute

	mvn clean install

to package and install the artifacts for the three subprojects. Afterwards, you can run ...

	cd server
	mvn spring-boot:run

to start the server or ...

	cd client
	mvn javafx:run

to run the client. Please note that the server needs to be running, before you can start the client.

Once this is working, you can try importing the project into your favorite IDE.

# Key Features

- Create, edit and delete notes
- Create, edit and delete collections
- Create, edit and delete tags
- Supports live updates of the rendered view for note contents
- Automatic saving of notes, collections and tags
- Search notes by title, content and tags
- Supports markdown rendering of text
- Supports 3 different languages
- Supports uploading of images

# Backlog

## Multi-Collection

Notes can be organized into a collection through the choiceBox to the right of the textField for the title of a note.
Users may change the collection of a note by choosing another collection in that choiceBox.

The other choiceBox at the top right corner filters the notes by the selected collection. When "All" is chosen, all
notes are shown regardless of their collection. When a specific collection is chosen, only its respective notes will be
shown.

When creating notes, if a collection is selected, the notes will be added to the specified collection. If no collection
is selected (i.e. "All" is selected), the notes will be added to a default collection which is created automatically if
a default does not exist. Users are able to change a collection's title and select which one they want to make default.

## Embedded Files

Users are able to open the Image Options button, where they could add or delete small images, and cycle through them in
the list. Other than that, this part doesn't have any more functionality, as images cannot be added to notes.

## Interconnected Content

Users are able to create tags in notes, which are recognized by a hashtag ('#'). The tags are saved only when the user
exits the note. There is a choiceBox with which the user can choose the tags to filter notes accordingly. Users may
choose multiple tags, and the selected tags are shown in the label "Selected Tags". The "Clear Tags" button clears the
selection of tags, so notes are no longer filtered by tags.

WARNING: Selecting or cycling tags gives warnings.

## Automated Change Synchronization

The app saves note content automatically every 3 seconds, and the changes appear automatically on all other clients,
using a websocket connection. Changes to note titles are immediately synchronized with the other clients,
as well as note additions and deletions.

WARNING: Do not close the app within 3 seconds after modifying it, as it will only close the window and not the
background process in Intelij.

## Live Language Switch

The app supports 3 languages: English, Dutch, Romanian; implemented via Internationalization. Languages can be switched
via the choiceBox (top left corner) which displays the language names, and country flags, or cycled via the respective
keyboard shortcut. Language is automatically saved upon app closure, and remains the same upon restarting(if ran through
Intelij).

## User Preferences

The app is able to save the language and the collection that has been last chosen by the user. This feature ensures a
good user experience by allowing users to resume their work exactly where they left off, without needing to manually
reselect their preferred language or collection.

# Usability

## Color Contrast

There is a single color scheme for the app, as the TA said this is good enough.

## Keyboard Shortcuts

### MainOverview

- ESC: Cycle between input fields(search bar, note title,note content)
- CTRL+N: Add a new note
- CTRL+D: Delete selected note
- CTRL+R: Refresh overview
- CTRL+Q: Check the syntax of the selected note
- CTRL+I: Open the image options screen
- CTRL+T: Used to cycle between existing tags(each time it's pressed, it cycles to the next tag, to actually select the
  tag, you need to be idle for two seconds, with the visual selector on the tag)
- CTRL+SHIFT+T: Used to clear tags
- ALT+DOWN-ARROW: Used to select the next note
- ALT+UP-ARROW: Used to select the previous note
- ALT+RIGHT-ARROW: Used to select the next collection
- ALT+LEFT-ARROW: Used to select the previous collection
- CTRL+P: Cycle through the selected note's collection(works the same as the tags, did this so that collections/tags
  don't get selected instantly and move their location in the wrong place)
- ENTER: Saves the note's title when the input focus is on the title field
- CTRL+L: Cycle through the languages
- CTRL+E: Opens the editCollection window
- CTRL+W: Close the window and stop the program
- CTRL+Z: Undo
- CTRL+Y: Redo

### EditCollectionOverview

- ESC: Focus the title text area
- CTRL+N: Create a new collection
- CTRL+D: Delete selected collection
- ALT+DOWN-ARROW: Select the next collection
- ALT+UP-ARROW: Select the previous collection
- CTRL+S: Save the updates of the selected collection
- CTRL+M: Make the selected collection default
- CTRL+W: Close the window
- CTRL+Z: Undo
- CTRL+Y: Redo

### ImageOptionsCtrl

- CTRL+N: Add a new image
- CTRL+D: Delete selected image
- ALT+DOWN-ARROW: Cycle to the next file
- ALT+UP-ARROW: Cycle to the previous file
- CTRL+W: Close the window

#### Side-notes

_*client/src/test/client.scene/KeyboardShortcutsTest.java*_
uses TestFX. Here ChatGpt was used, trying to fix the piepline before we had the documentation on how to run these types
of tests.

## Multi-modal Visualization

The app contains multiple icons with obvious purposes, flags and names for the languages, etc.

## Logical Navigation

There is the main scene, from which you can access all other scenes, each having a clear and logical layout, with easy
usabilty, both by mouse and keyborad.

## Keyboard Navigation

As explained in the Keyboard Shortcuts part, every feature of the app should be usable without mouse focus.

Side note: When trying to use both mouse and keyboard, there is one issue. If you select the notes in the main overview
with the mouse, the input focus change will not work, since mouse selection provides a different type of focus than the
keyboard one. In this case, when selecting a note by mouse and the blue outline appears around the list view, press the
right arrow to lose this weird focus on it. After that, it should run normally again.

## Undo & Redo Actions

Users may reverse addition, deletion and updates of notes and collections by using the Keyboard Shortcut Ctrl+Z. The UI
will be updated to show the changes. Actions maybe also re-done by Ctrl + Y. However, changes are not saved to the
server.

## Error messages

In cases of empty or duplicate titles, unreachable server, and others, pop-ups of warning type appear to inform the user
of the error. Users should be well-acknowledged of the error, so it is necessary for users to close the pop-ups
manually.

## Informative feedback

When creating or deleting notes and collections, and in some other cases, pop-ups appear again to inform users of
results or effects of their actions. These pop-ups may be manually closed or close automatically after a couple of
seconds.

The status label in the Edit Collections scene will also update users about the result of their actions.

## Confirmation for Key Actions

Deletion of notes and collections create a pop-up that requests user confirmation to delete them.

# Additional Information

### Buttons

The "+" and "-" buttons in the NetNote overview and Edit Collections overview add and delete notes and collections
respectively. The "↻" button refreshes the UI and updates it from the server.

### Save Button

The "Save" button next to the textField showing the title of a note was implemented to save changes made to the title
only. Other changes such as changes to note content and moving it to another collection is done automatically, every 3
seconds.

Similarly, the "Save" button in the Edit Collections overview saves changes made the to title only. Other changes are
saved to the server automatically.

### Markdown Syntax Errors

The '!' button shows the possible errors that may occur during rendering of the markdown view, so that users are
informed of typos specific to markdown formatting.

### Server Unavailable

When the server is unavailable, pop-ups will be shown for any action users try to make on the application, stopping them
from making any changes. This is because the application does not support saving changes without access to the server.

### Filters

When using all 3 filters (i.e. filter by collection, filter by tags and search by title or content), only notes that
satisfy all 3 filters will be shown.