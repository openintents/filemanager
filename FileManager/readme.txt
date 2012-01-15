 ****************************************************************************
 * Copyright (C) 2008-2011 OpenIntents.org                                  *
 *                                                                          *
 * Licensed under the Apache License, Version 2.0 (the "License");          *
 * you may not use this file except in compliance with the License.         *
 * You may obtain a copy of the License at                                  *
 *                                                                          *
 *      http://www.apache.org/licenses/LICENSE-2.0                          *
 *                                                                          *
 * Unless required by applicable law or agreed to in writing, software      *
 * distributed under the License is distributed on an "AS IS" BASIS,        *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. *
 * See the License for the specific language governing permissions and      *
 * limitations under the License.                                           *
 ****************************************************************************


OI File Manager is an open file manager that 
seamlessly cooperates with other applications.

To obtain the current release, visit
  http://www.openintents.org


---------------------------------------------------------
release: 1.2
date: ?
- Limit icon size in file list (issue 319, patch by John Doe)
- Hide optional commands in context menu (issue 329, patch by John Doe).
- "Save as" integration for Google Mail (Google Code-in task by Matěj Konečný)
- Option to hide hidden files (Google Code-In task by Matěj Konečný)
- Filter by file type (issue 166, Google Code-in task by Aviral Dasgupta)
- Show file details through context menu (Google Code-in task by Aviral Dasgupta)
- Option to sort files (Google Code-In task by Matěj Konečný)
- Keep list position after delete (Google Code-in task by Chickenbellyfinn)
- Show correct toast when deleting file (issue 365, Google Code-in task by Chickenbellyfinn)
- Apk icon support (Google Code-in task by Philip Hayes)
- Bookmarks of folder locations (Google Code-In task by Matěj Konečný)
- Details dialog shows size of folder contents (Google Code-In task by Philip Hayes)
- Fixed bug with details dialog on API 7 and earlier (Google Code-In task by Philip Hayes)
- Fixed bug with More option (issue 330, Google Code-In task by Matěj Konečný)
- Select/Deselect all in multi-select (Google Code-In task by Philip Hayes)
- remember previous directory when attaching files (Google Code-in task by Matěj Konečný)
- Better visibility for multi-select selection (issue 460, Google Code-in task by Philip Hayes)
- support for ZIP compression (patch by Evgeniy Berlog)
- warning dialog for file extension changes (issue 397, patch by Evgeniy Berlog)
- Refresh menu item (Google Code-in task by Aviral Dasgupta)
- Lazy loading of thumbnails (issue 271, Google Code-in task by Philip Hayes)
- Use custom icons for certain file types (issue 333, Google Code-in task by Matěj Konečný)
- Accept return key for entering a path (issue 461, Google Code-in task by Matěj Konečný)

---------------------------------------------------------
release: 1.1.6
date: 2011-06-02
- fix bug that prevented sending attachments through menu "Send" (patch by Alex)

---------------------------------------------------------
release: 1.1.5
date: 2011-05-28
- new menu item for multiselect: copy, move or delete multiple files at once (patch by John Doe).
- case insensitive sort order (issue 334, patch by Vishrut Patel).
- handle projections in the provider (issue 324, patch by Dominik Pretzsch).
- drop "mimetype" from provider path.
- delete files and folders in background (issue 294, patch by Damienix).

---------------------------------------------------------
release: 1.1.4
date: 2011-02-05
- new application icon for Android 2.0 or higher.
- exclude/include directories from media scan (activate this feature in advanced settings).
- allow app installation on external storage (requires Android 2.2 or higher)
- fix browser file upload (issue 288)
- support Android 2.3.
- translations into various languages.
- bug fixes (issue 308, 318).

---------------------------------------------------------
release: 1.1.3
date: 2010-05-29
- backward compatibility with Android 1.5.

---------------------------------------------------------
release: 1.1.2
date: 2010-05-29
- fix thumbnail size on high-density devices.
- translations: Occitan (post 1500), Polish, Russian

---------------------------------------------------------
release: 1.1.1
date: 2009-12-26
- recursive delete
- translations: Dutch, Faroese, Korean, Lao, Romanian

---------------------------------------------------------
release: 1.1.0
date: 2009-10-30
- display file size.
- show thumbnails for images.
- copy files.
- handle GET_CONTENT action.
- added support for all WebKit extensions.
- added support for following extensions:
  .amr, .3gp
- added support for upper case or mixed case letter
  extensions (like .png and .PNG)
- fix for send files via MMS.
- support for OI About.
- encode file URIs properly
- translations: Chinese, French, German, Japanese, Spanish

---------------------------------------------------------
release: 1.0.0
date: 2008-12-10

- First public release on Android SDK 1.0.

Features: 
- Show list of files.
- Icons for home (root) directory and SD card.
- Directory structure displayed through clickable
  buttons.
- Alternatively, the current path can be displayed
  in an input field.
- Supports PICK_FILE and PICK_DIRECTORY intents.
- Support for many file endings and mime types.
- "Back" key works for directories clicked in the
  list.
- Create directory, rename, delete files.
- Move files.

