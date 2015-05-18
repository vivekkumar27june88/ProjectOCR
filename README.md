# ProjectOCR
A mobile app where you take the picture of a credit or a debit card and it does an OCR to retrieve and re-create and visualize the card in data. You may use any existing third party OCR engine but not any existing card OCR engine/ API.

The app-flow would be as follows:
a. App opens a camera view
b. Auto-snaps an image if it identifies a card like pattern in focus and in view (optional: bonus points if you implement this). Alternatively tap to snap a picture of a card within a wire-frame
c. Run the OCR engine and show a progress indicator meanwhile. Extract the following information:

 i. Card network (Visa/ MC/ Rupay/ Amex etc)
 ii. Bank name (optional: bonus points if you implement this)
 iii. Card number
 iv. Card holder's name 
 v. Card Expiry date
 vi. Card - prominent background color
 
d. Display a drawn card with the background color as extracted from above
e. On top of this display the other extracted fields to look like a real card
