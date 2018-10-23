#!/bin/sh
> config.properties
echo "bot_token = ${BOT_TOKEN}" >> config.properties
echo "asana_personal_token = ${ASANA_PERSONAL_TOKEN}" >> config.properties
echo "asana_team_id = ${ASANA_TEAM_ID}" >> config.properties
gradle run