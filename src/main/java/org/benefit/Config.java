package org.benefit;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import static org.benefit.LayoutMode.TOP_LEFT;

// Don't reference this class for json troubleshooting, it has zero robustness, and only accomplishes what absolutely has to be done.
public class Config {
    private final String FILENAME = "config/Benefit.json";
    private int theMode = TOP_LEFT.getId();
    private boolean overlayValue = true;
    private boolean copyJson = false;

    public Config() {
        try {
            FileReader reader = new FileReader(FILENAME);
            JsonElement rootElement = JsonParser.parseReader(reader);
            if (!rootElement.isJsonObject()) throw new JsonParseException("Invalid Json Element Provided!");
            JsonObject asObject = rootElement.getAsJsonObject();
            theMode = asObject.get("Layout").getAsInt();
            overlayValue = asObject.get("Slot Overlay").getAsBoolean();
            copyJson = asObject.get("Json Name").getAsBoolean();
        } catch (Exception e) {
            File file = new File(FILENAME);
            if (!file.exists()) createConfig();
        }
    }

    private void createConfig() {
        try (FileWriter fileWriter = new FileWriter(FILENAME)) {
            fileWriter.write("{\"Layout\": 1, \"Slot Overlay\": true, \"Json Name\": false}");
        } catch (IOException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    public void save() {
        try (FileWriter fileWriter = new FileWriter(FILENAME)) {
            fileWriter.write(String.format("{\"Layout\": %s, \"Slot Overlay\": %s, \"Json Name\": %s}", theMode, overlayValue, copyJson));
        } catch (IOException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    public LayoutMode getLayoutMode() {
        return LayoutMode.byId(this.theMode);
    }

    public void setLayout(LayoutMode mode) {
        this.theMode = mode.getId();
    }

    public boolean getOverlayValue() {
        return this.overlayValue;
    }

    public void setOverlayValue(boolean overlayValue) {
        this.overlayValue = overlayValue;
    }

    public boolean shouldCopyJson() {
        return this.copyJson;
    }

    public void setCopyJson(boolean copyJson) {
        this.copyJson = copyJson;
    }

}