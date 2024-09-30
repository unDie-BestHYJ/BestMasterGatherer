package com.plugins.besthyj.bestmastergatherer.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.plugins.besthyj.bestmastergatherer.BestMasterGatherer;

public class GUIFileUtil {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static BestMasterGatherer plugin;

    public GUIFileUtil(BestMasterGatherer plugin) {
        this.plugin = plugin;
    }
}
