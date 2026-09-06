#!/bin/bash
# Connect the running dev client to the dedicated server (optionally through the proxy port).
PORT=${1:-25565}
clientdevbridge eval "
import net.minecraft.client.gui.screens.ConnectScreen
import net.minecraft.client.multiplayer.ServerData
import net.minecraft.client.multiplayer.resolver.ServerAddress
def data = new ServerData('metrics', '127.0.0.1:$PORT', ServerData.Type.OTHER)
ConnectScreen.startConnecting(mc.screen, mc, ServerAddress.parseString('127.0.0.1:$PORT'), data, false, null)
'connecting to $PORT'
" > /dev/null
for i in $(seq 1 40); do
  sleep 1
  if clientdevbridge status 2>/dev/null | grep -q "in world    true"; then echo "connected on $PORT"; exit 0; fi
done
echo "connect failed"; clientdevbridge status 2>&1 | grep screen; exit 1
