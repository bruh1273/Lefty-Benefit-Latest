# Lefty's Benefit. https://discord.gg/lefty
---
- <!> DISCLAIMER <!> NOT MY ORIGINAL MOD. ORIGINAL MOD CREDIT GOES TO Ui Utils by Coderx_Gamer (https://github.com/Coderx-Gamer/ui-utils) and konoashi#9999 on discord
- Shoutouts to Crosby.dev on Discord for some great improvements.
- Here's how you use the mods, Fabric API is required for this mod to work.
---

- "Soft Close" closes your current gui without sending a CloseHandledScreen Packet to the server, a TON of small dupes use this, because the gui cannot keep track of the correct one you're in.

- "De-sync" closes your current menu server-side and keeps it open client-side, this is totally useless in practice.

- "Send packets true/false" tells the client whether it should send any ClickSlot and ButtonClick Packets. (This means when send packets is active, nothing that you do in a GUI will ever be sent to the server.)

- "Delay packets true/false" when turned on it will store all ClickSlot and ButtonClick Packets into the memory, and will not send them until you turn it back on. (This is effectively "blink" for clickslots, and it is used a TON for dupes. See here : https://www.youtube.com/watch?v=L_-jZ_dJi_w )(You will also notice that it will say in chat "You have sent 'x' delayed packets." this is simply to give you a better understanding of what is happening.)

- "Save UI" saves your current gui to the memory, and can be restored be pressing 'V'. This is configurable in the Minecraft Keybinds options. The gui will only restore a real gui if you haven't closed it, or opened a new one. (It's main function is to allow you to do things, like place blocks down while still being in the GUI.)

- "Leave & Send Packets" only works when Delay packets is turned on. What it does, is immediately send all delayed packets, and disconnect you from the server .(can create potential race conditions on non-vanilla servers).

- "Sync Id" Is used by the game to keep track of which gui your player is currently in.

- "Revision" is something the server uses to keep track of what / how many times you've clicked on something in a gui.

- "GUI Chat" will let you send any chat message, or command while still existing in the gui. (I suggest copy pasting the command beforehand cause pressing E will close your gui rather then type it.)

- "Get Name" will automatically print the UI's name in chat, and copy it to your clipboard.
