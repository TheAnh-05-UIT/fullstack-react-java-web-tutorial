#!/bin/sh
set -e

# Change ownership of /tmp volume to the non-root appuser (10001)
# This solves the Fargate empty volume root ownership issue when readonlyRootFilesystem=true
chown -R appuser:appgroup /tmp

# Drop privileges and execute the main process as appuser
exec su-exec appuser "$@"
