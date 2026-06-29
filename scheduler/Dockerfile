FROM eclipse-temurin:21-jre-alpine

# Set up working directory
WORKDIR /app

# Copy the execution script and grant execute permissions
COPY run_billing.sh /app/run_billing.sh
RUN chmod +x /app/run_billing.sh

# Copy the crontab file to the alpine cron directory
COPY crontab.txt /etc/crontabs/root

# Create the log file so we can tail it
RUN touch /var/log/cron.log

# Command to run the cron daemon in the foreground (-f) with log level 8 (-d 8)
CMD ["crond", "-f", "-d", "8"]
