package org.vmstudio.essentials.core.server;

import lombok.Getter;


public class EssentialsServerSettings {

    @Getter
    private static boolean betterBow;

    static {
        resetToDefaults();
    }

    public static void resetToDefaults(){
        betterBow = true;
    }


    public static void joinedDedicatedServer(){
        betterBow = false;
    }

    static void setBetterBow(boolean value){
        betterBow = value;
    }
}
