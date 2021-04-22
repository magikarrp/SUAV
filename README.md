# SUAV
Contributors: Tanner Braun, Zhitao Liu, Raymond Chu, Dennis Karpovitch, James Pelusi

UAV Flight Planning App

API's:
* [Mapbox](https://docs.mapbox.com/api/overview/)
* [AirMap](https://developers.airmap.com/docs)
* [OpenSky](https://opensky-network.org/apidoc/)

Libraries:
* [Firebase - Real Time Database](https://console.firebase.google.com/u/0/)


Compiling and Running the App:
    * SUAV should run as is in android studio
    * Please give it permission to access your location
    * In order to use the app, you must have an AirMap account
    * You can use our test account with the following information
        * Email: jamespel@bu.edu
        * PW: CS501airmap*
    
Basic Testing:
    * Pinning a Location
        * After logging into the app, select the "Drop Marker" button in the bottom right
        * Find a location that you would like to pin by scrolling along the map and hit "Select Location"
        * Press "Create Pin" and enter some details. If you make it public, it will be viewable by other accounts.
        * After submitting the pin details form, you should be able to view the pin on the map.
    * Check flight safety of an area
        * Follow the same steps as above, except instead of clicking "Create Pin" click "Check the Skies"
        * Here you will be presented with information about the weather conditions in the area
        * This page will also display the number of planes in the area you are checking out (Nothing will display if the OpenSky API is down and there will be a timeout error in the console log)
    * Creating and submitting a flight plan
        * After logging in, select "Plan Flight"
        * Scroll along the map to find a point of the area you would like to fly in
        * Use the "Drop Marker" button to place pins that create the outline of a shape
        * After placing at least 3 pins select "Confrim Boundaries" and a polygon of your flight plan should be drawn on the map
        * Select "Flight Details" and you will be brought to a form asking for more information about you flight plan
        * Input your maximum altitude you will be flying at and the date and time that you will be flying, then select "Create Plan"
        * You will then be brought to a screen that displays required sets of regulations you must follow in the area you are flying
        * Beneath each of them you will have a number of violations, and you can click on an item to learn more about specific rules you are violating
        * When you are satisfied with your plan, click the submit button and your plan will be submitted to AirMap. A toast will appear on the bottom of your screen when the approval message is received.
    * Creating a new event
        * Similar to creating a flight plan, follow the same steps as above until the "select 'Create Plan'" step.
        * Instead of selecting "Create Plan", select "Create Event" instead.
        * Here you can input the name and description of the event and then click "Create Event" again
        * You will be brought to the same flight briefing screen as above, and you can submit your flight plan for the event like before
        * Now if you go back to the home screen, you can click on the Events button and you should be able to see your Event in the list of events!
    