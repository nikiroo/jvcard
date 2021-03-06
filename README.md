# jVCard
Small TUI (text mode) VCard manager (also supports abook files)

## Current version

- it can open VCF and ABOOK files
- it can create a Swing terminal or use a real terminal (try "--help") using Lanterna 3 (which can be found here on GitHub, too)
- it will list all the contacts of the file you select
- it will show more detailed informations about a selected contact, including an ASCII art representation of their photo if any
- it can create/delete a contact
- it can be used to edit your data (currently in RAW format, field by field)
- it can save back to file
- English and French versions available (will look for the host language, can be forced with "--lang en")
- remote synchronisation server (still in testing, but should work)

## TODO

- [ ] lot of other things
- [ ] correct UI for new contact/new data/edit data-types
- [ ] update the screenshots
- [ ] support escape as cancel in new X, new Y..
- [ ] allow adding a new addressbook (and ask where to save it, offer current dir OR last input dir as default)
- 	make sure it works OK for "jvcard.jar /dir/to/directory-full-of-vcf-files"
- 	maybe a mode with 1 contact/file ?
- 	and take the name from the contact to show in the list of "files" ?
- 	add action "open as addressbook" ?
- [x] find out why "R" is not displayed as a possible action (edit RAW) when viewing a contact)
- 	it was missing in resources-en
- [ ] add: modal dialogue -> "sub menu" (change the action keys) ? (maybe a "second action mode" and a copy of the MainContent)
- [x] change --save-dir so that it saves ALL the translation files (open + scan dir?)

## Screenshots

### Navigation

![Opening a directory](http://nikiroo.be/jvcard/screenshots/open-dir.png)
![Opening a VCF/aBook card](http://nikiroo.be/jvcard/screenshots/open-vcf.png)

### Contact screen

![Contact screen](http://nikiroo.be/jvcard/screenshots/open-contact.png)

### Image modes

![Double Dithering](http://nikiroo.be/jvcard/screenshots/image-dd.png)
![ASCII](http://nikiroo.be/jvcard/screenshots/image-ascii.png)
![Dithering](http://nikiroo.be/jvcard/screenshots/image-dithering.png)
![Double Resolution](http://nikiroo.be/jvcard/screenshots/image-double.png)

### Edit contact screen (Work In Progress)

![Edit contact WIP](http://nikiroo.be/jvcard/screenshots/edit-contact.png)
