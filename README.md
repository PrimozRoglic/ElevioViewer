# ElevioVeiwer

This is a command line GUI, used to display Elev.io content.

![missing](./images/sample.png "Sample")

## Requirements

Make sure you have Java 1.8, scala and sbt installed on your machine.

## Usage

To get started, create a configuration file in the root directory of the project, here I will simply call it *config*. The format is very simple, in the first line put the desired domain, in the second your api key and in the third your jwt token. Here is a sample *config* (note this are not real credentials):
```
api.elevio-staging.com
558ed34563456f470367dd568421439d
eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwczovL2FwcC5lbGV2aW8tc3RhZ2luZy5jb20iLCJzdWIiOiI1ZDFkODBjNTlmZjE5IiwiZXhwIjoadfjHUOFODk3LCJpYXQiOjE1NjIyNDI4OTcsImp0aSI6InFJNSDZTN0cWM5Y2cycGQ0bjllc25pN2WEDHSoMWJxIiwKICAidXNlck5hbWUiIDogImphbmtyZW1zZXJAZ21haWwuY29tIiwKICAidXNlcklkIiA6IDEzMDUzLAogICJzY29wZSIgOiBbICJyZWFkOmNvbnRleHR1YWwiLCAid3JpdGU6Y29udGV4dHVhbCIsICJyZWFkOmNhcmQiLCAid3JpdGU6Y2FyZCIsICJhcHByb3ZlOmFydGljbGUiLCAicmVhZDphcnRpY2xlIiwgIndyaXRlOmFydGljbGUiIF0KfQ.n1djeu-VRdMIzNG6lDCuvjxBiFPm6qnJnCpSCjc_N8g
```
Next, from the root directory of the project, simply run:
```
sbt "run config"
```
where *config* should be the path to your config file (just the name of the file if you created it in the root directory of the project).  
This should open a command line GUI as in the picture above. To navigate between elements use the arrow keys. To display a selected article in either the Search panel of the Article List panel, press Enter. To trigger the next, prev and GO actions, navigate to them and press Enter. To search, enter your search query into the input field and trigger the GO action.

## Libraries

This project was built using the [akka-http](https://doc.akka.io/docs/akka-http/current/index.html) library for making HTTP requests, and the [Scurses](https://github.com/Tenchi2xh/Scurses) library for assembling the GUI. 

## Make this project great again (or for the first time more accurately)

This project is still in a very rough state. See the GitHub issues page for more details.
