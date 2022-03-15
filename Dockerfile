FROM amazoncorretto:11
COPY build/libs/Timetracker-1.0-all.jar /app/Timetracker-1.0-all.jar
ENV TIME_TRACKER_BASE_PATH /storage
ENV TIME_TRACKER_BACKUP_PATH backup
RUN mkdir /storage
CMD java -jar /app/Timetracker-1.0-all.jar