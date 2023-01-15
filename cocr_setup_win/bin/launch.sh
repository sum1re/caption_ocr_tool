#!/bin/sh

GC_LOG="$GC_LOG"

DIRNAME=$(dirname "$0")
PROGNAME=$(basename "$0")
GREP="grep"

. "$DIRNAME/common.sh"

# Use the maximum available, or set MAX_FD != -1 to use that
MAX_FD="maximum"

# tell linux glibc how many memory pools can be created that are used by malloc
MALLOC_ARENA_MAX="${MALLOC_ARENA_MAX:-1}"
export MALLOC_ARENA_MAX

# OS specific support (must be 'true' or 'false').
cygwin=false;
darwin=false;
linux=false;
solaris=false;
freebsd=false;
other=false
case "`uname`" in
    Darwin*)
        darwin=true
        ;;
    FreeBSD)
        freebsd=true
        ;;
    Linux)
        linux=true
        ;;
    *)
        other=true
        ;;
esac

# Setup COCR_HOME
RESOLVED_COCR_HOME=`cd "$DIRNAME/.." >/dev/null; pwd`
if [ "x$COCR_HOME" = "x" ]; then
    # get the full path (without any relative bits)
    COCR_HOME=$RESOLVED_COCR_HOME
else
 SANITIZED_COCR_HOME=`cd "$COCR_HOME"; pwd`
 if [ "$RESOLVED_COCR_HOME" != "$SANITIZED_COCR_HOME" ]; then
   echo ""
   echo "   WARNING:  COCR_HOME may be pointing to a different installation - unpredictable results may occur."
   echo ""
   echo "             COCR_HOME: $COCR_HOME"
   echo ""
   sleep 2s
 fi
fi
export COCR_HOME

# Read an optional running configuration file
if [ "x$RUN_CONF" = "x" ]; then
    RUN_CONF="$DIRNAME/launch.conf"
fi
if [ -r "$RUN_CONF" ]; then
    . "$RUN_CONF"
fi

# Setup the JVM
JAVA_HOME=$COCR_HOME/jre
if [ "x$JAVA" = "x" ]; then
    JAVA="$JAVA_HOME/bin/java"
fi

if $linux; then
    # process the launch options
    for var in $JAVA_OPTS
    do
       # Remove quotes
       p=`echo $var | tr -d "'"`
       case $p in
         -Dcocr.base.dir=*)
              COCR_BASE_DIR=`readlink -m ${p#*=}`
              ;;
         -Dcocr.log.dir=*)
              COCR_LOG_DIR=`readlink -m ${p#*=}`
              ;;
       esac
    done
fi

# No readlink -m on BSD
if $darwin || $freebsd || $other ; then
    # process the launch options
    for var in $JAVA_OPTS
    do
       # Remove quotes
       p=`echo $var | tr -d "'"`
       case $p in
         -Dcocr.base.dir=*)
              COCR_BASE_DIR=`cd ${p#*=} ; pwd -P`
              ;;
         -Dcocr.log.dir=*)
              if [ -d "${p#*=}" ]; then
                COCR_LOG_DIR=`cd ${p#*=} ; pwd -P`
             else
                #since the specified directory doesn't exist we don't validate it
                COCR_LOG_DIR=${p#*=}
             fi
             ;;
       esac
    done
fi

# determine the default base dir, if not set
if [ "x$COCR_BASE_DIR" = "x" ]; then
   COCR_BASE_DIR="$COCR_HOME/app"
fi
# determine the default log dir, if not set
if [ "x$COCR_LOG_DIR" = "x" ]; then
   COCR_LOG_DIR="$COCR_BASE_DIR/logs"
fi


if [ "$PRESERVE_JAVA_OPTS" != "true" ]; then
    PREPEND_JAVA_OPTS="-server $PREPEND_JAVA_OPTS"

    # Set flag if JVM is modular
    setModularJdk

    if [ "$GC_LOG" = "true" ]; then
        # Enable rotating GC logs if the JVM supports it and GC logs are not already enabled
        mkdir -p $COCR_LOG_DIR
        NO_GC_LOG_ROTATE=`echo $JAVA_OPTS | $GREP "\-Xlog\:\?gc"`
        if [ "x$NO_GC_LOG_ROTATE" = "x" ]; then
            # backup prior gc logs
            mv -f "$COCR_LOG_DIR/gc.log" "$COCR_LOG_DIR/backupgc.log" >/dev/null 2>&1
            mv -f "$COCR_LOG_DIR/gc.log.0" "$COCR_LOG_DIR/backupgc.log.0" >/dev/null 2>&1
            mv -f "$COCR_LOG_DIR/gc.log.1" "$COCR_LOG_DIR/backupgc.log.1" >/dev/null 2>&1
            mv -f "$COCR_LOG_DIR/gc.log.2" "$COCR_LOG_DIR/backupgc.log.2" >/dev/null 2>&1
            mv -f "$COCR_LOG_DIR/gc.log.3" "$COCR_LOG_DIR/backupgc.log.3" >/dev/null 2>&1
            mv -f "$COCR_LOG_DIR/gc.log.4" "$COCR_LOG_DIR/backupgc.log.4" >/dev/null 2>&1
            mv -f "$COCR_LOG_DIR"/gc.log.*.current "$COCR_LOG_DIR/backupgc.log.current" >/dev/null 2>&1

            if [ "$MODULAR_JDK" = "true" ]; then
                TMP_PARAM="-Xlog:gc*:file=\"$COCR_LOG_DIR/gc.log\":time,uptimemillis:filecount=5,filesize=3M"
            else
                TMP_PARAM="-verbose:gc -Xloggc:\"$COCR_LOG_DIR/gc.log\" -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=5 -XX:GCLogFileSize=3M -XX:-TraceClassUnloading"
            fi

            eval "$JAVA" -server $TMP_PARAM -version >/dev/null 2>&1 && PREPEND_JAVA_OPTS="$PREPEND_JAVA_OPTS $TMP_PARAM"
            # Remove the gc.log file from the -version check
            rm -f "$COCR_LOG_DIR/gc.log" >/dev/null 2>&1
        fi
    fi

    # Set default modular JVM options
    setDefaultModularJvmOptions $JAVA_OPTS
    JAVA_OPTS="$JAVA_OPTS $DEFAULT_MODULAR_JVM_OPTIONS"

    JAVA_OPTS="$PREPEND_JAVA_OPTS $JAVA_OPTS"
fi

# Display our environment
echo "========================================================================="
echo ""
echo "  COCR Bootstrap Environment"
echo ""
echo "  COCR_HOME: $COCR_HOME"
echo ""
echo "  JAVA: $JAVA"
echo ""
echo "  JAVA_OPTS: $JAVA_OPTS"
echo ""
echo "========================================================================="
echo ""

while true; do
   if [ "x$LAUNCH_COCR_IN_BACKGROUND" = "x" ]; then
      # Execute the JVM in the foreground
      eval \"$JAVA\" -D\"[Launch]\" $JAVA_OPTS \
	     -Dcocr.home.dir=\""$COCR_HOME"\" \
         -Dcocr.base.dir=\""$COCR_BASE_DIR"\" \
		 -Dcocr.log.dir=\""$COCR_LOG_DIR"\" \
         -jar \""$COCR_HOME"/cocr.jar\"
      COCR_STATUS=$?
   else
      # Execute the JVM in the background
      eval \"$JAVA\" -D\"[Launch]\" $JAVA_OPTS \
	     -Dcocr.home.dir=\""$COCR_HOME"\" \
         -Dcocr.base.dir=\""$COCR_BASE_DIR"\" \
		 -Dcocr.log.dir=\""$COCR_LOG_DIR"\" \
         -jar \""$COCR_HOME"/cocr.jar\" "&"
      COCR_PID=$!
      # Trap common signals and relay them to the cocr process
      trap "kill -HUP  $COCR_PID" HUP
      trap "kill -TERM $COCR_PID" INT
      trap "kill -QUIT $COCR_PID" QUIT
      trap "kill -PIPE $COCR_PID" PIPE
      trap "kill -TERM $COCR_PID" TERM
      if [ "x$COCR_PIDFILE" != "x" ]; then
        echo $COCR_PID > $COCR_PIDFILE
      fi
      # Wait until the background process exits
      WAIT_STATUS=128
      while [ "$WAIT_STATUS" -ge 128 ]; do
         wait $COCR_PID 2>/dev/null
         WAIT_STATUS=$?
         if [ "$WAIT_STATUS" -gt 128 ]; then
            SIGNAL=`expr $WAIT_STATUS - 128`
            SIGNAL_NAME=`kill -l $SIGNAL`
            echo "*** COCRAS process ($COCR_PID) received $SIGNAL_NAME signal ***" >&2
         fi
      done
      if [ "$WAIT_STATUS" -lt 127 ]; then
         COCR_STATUS=$WAIT_STATUS
      else
         COCR_STATUS=0
      fi
      if [ "$COCR_STATUS" -ne 10 ]; then
            # Wait for a complete shudown
            wait $COCR_PID 2>/dev/null
      fi
      if [ "x$COCR_PIDFILE" != "x" ]; then
            grep "$COCR_PID" $COCR_PIDFILE && rm $COCR_PIDFILE
      fi
   fi
   if [ "$COCR_STATUS" -eq 10 ]; then
      echo "Restarting application..."
   else
      exit $COCR_STATUS
   fi
done
