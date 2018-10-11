# DatePickerForceSpinner
Fix an issue that Android DatePicker spinner mode is ignored on some devices/OS version.

## Features
This sample project can force ALL DatePicker dialog inside the Activity to use the old spinner mode,
even for those picker that is not created by your own code (such as the DatePicker dialog from a
WebView's <input type="date"/> element), on (almost) all Android version / skins.

## [Issue 222208](https://issuetracker.google.com/issues/37119315) - TimePickerMode spinner does not work in Nougat
This sample project also fix this issue. It should work on any Android version starts from 5.0
(which initially introduced the problematic material design DatePicker).

## Acknowledgement
Based on the work of:
- https://gist.github.com/lognaturel/232395ee1079ff9e4b1b8e7096c3afaf
- https://stackoverflow.com/a/27952810
