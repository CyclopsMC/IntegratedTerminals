# Same as env.sh, plus a JFR recording on every JVM it starts.
# The pid placeholder keeps the gradle launcher and the game server from colliding.
export JAVA_HOME=/tmp/claude-0/-home-user-IntegratedTerminals/2efbb763-3878-5cba-8ea3-c90bdfeacf0e/scratchpad/jdks/jdk-25.0.4.1+1
export PATH=$JAVA_HOME/bin:$PATH
export JAVA_TOOL_OPTIONS="$JAVA_TOOL_OPTIONS -Dintegratedterminals.debugTerminalOpenMetrics=true -Dintegratedterminals.debugTerminalOpenMetricsDir=/home/user/IntegratedTerminals/run/metrics -XX:StartFlightRecording=name=open,settings=profile,filename=/tmp/claude-0/-home-user-IntegratedTerminals/2efbb763-3878-5cba-8ea3-c90bdfeacf0e/scratchpad/jfr/rec_%p.jfr,dumponexit=true,maxsize=512m"
