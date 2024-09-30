package com.plugins.besthyj.bestmastergatherer.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.plugins.besthyj.bestmastergatherer.BestMasterGatherer;

public class GUIFileUtil {
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private BestMasterGatherer plugin;

    public GUIFileUtil(BestMasterGatherer plugin) {
        this.plugin = plugin;
    }
}
