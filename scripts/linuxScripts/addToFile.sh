#!/bin/bash
mvn --quiet exec:java -Dexec.mainClass="win.servername.TicketCreator.Main" -Dexec.args="Add"
