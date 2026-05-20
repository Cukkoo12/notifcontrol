package com.cukkoo.notifcontrol.modmenu;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/**
 * Loaded only when ModMenu is present (Fabric entrypoint mechanism).
 */
public class NotifControlModMenuEntry implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return NotifControlScreen::new;
    }
}
