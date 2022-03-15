# Timetracker

Timetracker was built to support tracking time I spend on projects.

It allows me to:
- manage a list of projects with a category assignment
- track time spent on a project on any given day
  - by specifying the exact time spent (supports several blocks for the same project)
  - by giving start/end times and the duration of a break
- display a weekly overall aggregate displaying the total time per day and week, and how
  the days break up into project portions.
  
## Technical setup
The timetracker is built as a Kotlin multiplatform build with a Ktor REST-like API and
a React-based frontend. The app is run in a Docker container and stores its data in Json
files.

## Build
- Run gradle shadowJar to build a fat jar with dependencies
- Run a Docker build on top of that to create a Docker image.
- Start a container with it
  - Forward port 8080 to access the app
  - Mount a volume or folder to /storage to store the data outside the container
