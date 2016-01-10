package com.renegade.rc;

import android.os.Build;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class RC {
    protected static final String SERVER_IP = "et.therenegadeclan.org";
    protected static final int SERVER_PORT = 27960;
	protected static final String API_URL = "http://www.therenegadeclan.org/api/";
	protected static final String API_KEY = "0d267a49448f1a907aa90da78b008fa5";
	protected static final String API_SECRET = "56b05115c416bdd410acfa7deb3965d967c2d127";

	public static String generateAPI(String api) {
		return API_URL + api + "?key=" + API_KEY + "&secret=" + API_SECRET;
	}

    /*
    static class JSONFunctions {
        public static ArrayList<String> toArrayList(JSONArray json) {
            ArrayList<String> arr = new ArrayList<String>();
            try {
                for (int i = 0; i < json.length(); i++) {
                    arr.add( json.getString(i) );
                }
                return arr;

            } catch (JSONException e) {
                Log.e("JSONFunctions", "ToArrayList() Exception: " + e.toString());
            }
            return arr;
        }
    }
    */

    public static HashMap<String, Integer> etColors = new HashMap<String, Integer>();

    public static void initColors() {
        etColors.put("0", 0xFF000000);
        etColors.put("1", 0xFFFF0000);
        etColors.put("2", 0xFF00FF00);
        etColors.put("3", 0xFFFFFF00);
        etColors.put("4", 0xFF0000FF);
        etColors.put("5", 0xFF00FFFF);
        etColors.put("6", 0xFFFF00FF);
        etColors.put("7", 0xFF000000); // 0xFFFFFFFF
        etColors.put("8", 0xFFFF7F00);
        etColors.put("9", 0xFF7F7F7F);
        etColors.put("a", 0xFFFF9919);
        etColors.put("b", 0xFF007F7F);
        etColors.put("c", 0xFF7F007F);
        etColors.put("d", 0xFF007FFF);
        etColors.put("e", 0xFF7F00FF);
        etColors.put("f", 0xFF3399CC);
        etColors.put("g", 0xFFCCFFCC);
        etColors.put("h", 0xFF006633);
        etColors.put("i", 0xFFFF0033);
        etColors.put("j", 0xFF9A1717);
        etColors.put("k", 0xFF993300);
        etColors.put("l", 0xFFB1852D);
        etColors.put("m", 0xFF999933);
        etColors.put("n", 0xFFFFFFBF);
        etColors.put("o", 0xFFFFFF7F);
        etColors.put("p", 0xFF000000);
        etColors.put("q", 0xFFFF0000);
        etColors.put("r", 0xFF00FF00);
        etColors.put("s", 0xFFFFFF00);
        etColors.put("t", 0xFF0000FF);
        etColors.put("u", 0xFF00FFFF);
        etColors.put("v", 0xFFFF00FF);
        etColors.put("w", 0xFF000000); // 0xFFFFFFFF
        etColors.put("x", 0xFFFF7F00);
        etColors.put("y", 0xFF7F7F7F);
        etColors.put("z", 0xFFBFBFBF);
        etColors.put("!", 0xFFF99132);
        etColors.put("@", 0xFF7F3F00);
        etColors.put("#", 0xFF6C196B);
        etColors.put("$", 0xFF7F007F);
        etColors.put("%", 0xFF007FFF);
        etColors.put("&", 0xFF7F00FF);
        etColors.put("*", 0xFFBFBFBF);
        etColors.put("(", 0xFF006633);
        etColors.put(")", 0xFFFF0033);
        etColors.put("-", 0xFF999933);
        etColors.put("_", 0xFF660000);
        etColors.put("+", 0xFF993300);
        etColors.put("=", 0xFF999933);
        etColors.put("[", 0xFFFFFFCC);
        etColors.put("{", 0xFFFFFFCC);
        etColors.put("]", 0xFF999900);
        etColors.put("}", 0xFF999900);
        etColors.put("\\", 0xFF006600);
        etColors.put("|", 0xFF006600);
        etColors.put(";", 0xFFFFFFCC);
        etColors.put(":", 0xFFFFFFCC);
        etColors.put("'", 0xFFFFFFCC);
        etColors.put("\"", 0xFFFF9919);
        etColors.put(",", 0xFFCC9933);
        etColors.put("<", 0xFF064E06);
        etColors.put(".", 0x00000000); // 0xFFFFFFFF
        etColors.put(">", 0xFF04055D);
        etColors.put("?", 0xFF660303);
        etColors.put("/", 0xFFFFFF7F);
         etColors.put("", 0xFF007F7F); // euro character
//         etColors.put("^", 0xFFFFFFFF);
    }

    public static String strip(String text) {
        String code, output = "";
        if(text.contains("^")) {

            if(etColors.size() == 0)
                initColors();

            for (int i = 0; i < text.length(); i++) {
                if (text.substring(i, i + 1).equals("^")) {
                    code = text.substring(i + 1, i + 2).toLowerCase();
                    if (etColors.containsKey(code)) {
                        for (int j = i + 2; j < text.length(); j++) {
                            if (text.substring(j, j + 1).equals("^")) {
                                i = j - 1;
                                break;
                            }
                            output += text.substring(j, j + 1);
                        }
                    }
                }
            }
        } else {
            output = text;
        }

        return output;
    }

    /**
     * Use ET color codes to color a TextView
     * @param tv TextView to color
     */
    public static void color(TextView tv) {
        String text = tv.getText().toString();
        if(!text.contains("^"))
            return;

        if(etColors.size() == 0)
            initColors();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            SpannableStringBuilder ssb = new SpannableStringBuilder();
            for (int i = 0; i < text.length(); i++) {
                if (text.substring(i, i + 1).equals("^")) {
                    String code = text.substring(i + 1, i + 2).toLowerCase();
                    if (etColors.containsKey(code)) {
                        String output = "";
                        for (int j = i + 2; j < text.length(); j++) {
                            if (text.substring(j, j + 1).equals("^")) {
                                i = j - 1;
                                break;
                            }
                            output += text.substring(j, j + 1);
                        }
                        ssb.append(output, new ForegroundColorSpan(etColors.get(code)), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                    }
                }
            }
            tv.setText(ssb);
        } else {
            colorKitKat(tv);
        }
    }

    public static void colorKitKat(TextView tv) {
        int startPos, endPos, colorNum = 0;
        String text = tv.getText().toString(), et = text, colorKey;
        HashMap<String, Integer> etColors = new HashMap<>();
        etColors.put("^0", 0xFF000000);
        etColors.put("^1", 0xFFFF0000);
        etColors.put("^2", 0xFF00FF00);
        etColors.put("^3", 0xFFFFFF00);
        etColors.put("^4", 0xFF0000FF);
        etColors.put("^5", 0xFF00FFFF);
        etColors.put("^6", 0xFFFF00FF);
        etColors.put("^7", 0xFF000000); // 0xFFFFFFFF
        etColors.put("^8", 0xFFFF7F00);
        etColors.put("^9", 0xFF7F7F7F);
        etColors.put("^a", 0xFFFF9919);
        etColors.put("^b", 0xFF007F7F);
        etColors.put("^c", 0xFF7F007F);
        etColors.put("^d", 0xFF007FFF);
        etColors.put("^e", 0xFF7F00FF);
        etColors.put("^f", 0xFF3399CC);
        etColors.put("^g", 0xFFCCFFCC);
        etColors.put("^h", 0xFF006633);
        etColors.put("^i", 0xFFFF0033);
        etColors.put("^j", 0xFF9A1717);
        etColors.put("^k", 0xFF993300);
        etColors.put("^l", 0xFFB1852D);
        etColors.put("^m", 0xFF999933);
        etColors.put("^n", 0xFFFFFFBF);
        etColors.put("^o", 0xFFFFFF7F);
        etColors.put("^p", 0xFF000000);
        etColors.put("^q", 0xFFFF0000);
        etColors.put("^r", 0xFF00FF00);
        etColors.put("^s", 0xFFFFFF00);
        etColors.put("^t", 0xFF0000FF);
        etColors.put("^u", 0xFF00FFFF);
        etColors.put("^v", 0xFFFF00FF);
        etColors.put("^w", 0xFF000000); // 0xFFFFFFFF
        etColors.put("^x", 0xFFFF7F00);
        etColors.put("^y", 0xFF7F7F7F);
        etColors.put("^z", 0xFFBFBFBF);
        etColors.put("^A", 0xFFFF9919);
        etColors.put("^B", 0xFF007F7F);
        etColors.put("^C", 0xFF7F007F);
        etColors.put("^D", 0xFF007FFF);
        etColors.put("^E", 0xFF7F00FF);
        etColors.put("^F", 0xFF3399CC);
        etColors.put("^G", 0xFFCCFFCC);
        etColors.put("^H", 0xFF006633);
        etColors.put("^I", 0xFFFF0033);
        etColors.put("^J", 0xFF9A1717);
        etColors.put("^K", 0xFF993300);
        etColors.put("^L", 0xFFB1852D);
        etColors.put("^M", 0xFF999933);
        etColors.put("^N", 0xFFFFFFBF);
        etColors.put("^O", 0xFFFFFF7F);
        etColors.put("^P", 0xFF000000);
        etColors.put("^Q", 0xFFFF0000);
        etColors.put("^R", 0xFF00FF00);
        etColors.put("^S", 0xFFFFFF00);
        etColors.put("^T", 0xFF0000FF);
        etColors.put("^U", 0xFF00FFFF);
        etColors.put("^V", 0xFFFF00FF);
        etColors.put("^W", 0xFF000000); // 0xFFFFFFFF
        etColors.put("^X", 0xFFFF7F00);
        etColors.put("^Y", 0xFF7F7F7F);
        etColors.put("^Z", 0xFFBFBFBF);
        etColors.put("^!", 0xFFF99132);
        etColors.put("^@", 0xFF7F3F00);
        etColors.put("^#", 0xFF6C196B);
        etColors.put("^$", 0xFF7F007F);
        etColors.put("^%", 0xFF007FFF);
        etColors.put("^&", 0xFF7F00FF);
        etColors.put("^*", 0xFFBFBFBF);
        etColors.put("^(", 0xFF006633);
        etColors.put("^)", 0xFFFF0033);
        etColors.put("^-", 0xFF999933);
        etColors.put("^_", 0xFF660000);
        etColors.put("^+", 0xFF993300);
        etColors.put("^=", 0xFF999933);
        etColors.put("^[", 0xFFFFFFCC);
        etColors.put("^{", 0xFFFFFFCC);
        etColors.put("^]", 0xFF999900);
        etColors.put("^}", 0xFF999900);
        etColors.put("^\\", 0xFF006600);
        etColors.put("^|", 0xFF006600);
        etColors.put("^;", 0xFFFFFFCC);
        etColors.put("^:", 0xFFFFFFCC);
        etColors.put("^'", 0xFFFFFFCC);
        etColors.put("^\"", 0xFFFF9919);
        etColors.put("^,", 0xFFCC9933);
        etColors.put("^<", 0xFF064E06);
        etColors.put("^.", 0x00000000); // 0xFFFFFFFF
        etColors.put("^>", 0xFF04055D);
        etColors.put("^?", 0xFF660303);
        etColors.put("^/", 0xFFFFFF7F);
        // etColors.put("^", 0xFF007F7F); // euro character
        // etColors.put("^^", "");
        for (String key : etColors.keySet()) {
            et = et.replace(key, "");
        }
        tv.setText(et, TextView.BufferType.SPANNABLE);
        Spannable s = (Spannable) tv.getText();
        for (int i = 0; i < text.length() - 1; i++) {
            if (etColors.containsKey(text.substring(i, i + 2))) {
                colorKey = text.substring(i, i + 2);
                startPos = i - (colorNum++ * 2);
                for (int j = i + 2; j < text.length(); j++) {
                    if (j == text.length() - 1) {
                        s.setSpan(new ForegroundColorSpan(etColors.get(colorKey)), startPos, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        break;
                    }
                    if (etColors.containsKey(text.substring(j, j + 2))) {
                        endPos = j - (colorNum * 2);
                        s.setSpan(new ForegroundColorSpan(etColors.get(colorKey)), startPos, endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        break;
                    }
                }
            }
        }
    }

}

class Maps {

    private String bsp;
    private String map;
    private int length;
    private String mapDesc;

    public Maps(String bsp, String map, int length, String mapDesc) {
        this.bsp = bsp;
        this.map = map;
        this.length = length;
        this.mapDesc = mapDesc;
    }

    public String getBsp() {
        return bsp;
    }

    public String getMap() {
        return map;
    }

    public int getLength() {
        return length;
    }

    public String getMapDesc() {
        return mapDesc;
    }
}

class Bans {

    private String player, reason, banner;
    private String created, expires;

    public Bans(String player, String created, String reason, String banner, String expires) {
        this.player = player;
        this.created = created;
        this.reason = reason;
        this.banner = banner;
        this.expires = expires;
    }

    public String getPlayer() {
        return player;
    }

    public String getCreated() {
        return created;
    }

    public String getReason() {
        return reason;
    }

    public String getBanner() {
        return banner;
    }

    public String getExpires() {
        return expires;
    }
}

class Warnings {

    private String name, warning, created, warner;

    public Warnings(String name, String warning, String created, String warner) {
        this.name = name;
        this.warning = warning;
        this.created = created;
        this.warner = warner;
    }

    public String getName() {
        return name;
    }

    public String getWarning() {
        return warning;
    }

    public String getCreated() {
        return created;
    }

    public String getWarner() {
        return warner;
    }
}

class Teams {

    public static int clients = 0;

    public static void clear() {
        Axis.clear();
        Allies.clear();
        Spectators.clear();
        Connecting.clear();
        clients = 0;
    }

    static class Axis {
        public static int players = 0;
        private static ArrayList<Player> playerList = new ArrayList<Player>();

        public static void add(String name, String xp, String ping) {
            playerList.add( new Player(name, xp, ping) );
            players++;
            clients++;
        }

        public static Player get(int i) {
            return playerList.get(i);
        }

        public static void clear() {
            playerList.clear();
            players = 0;
        }
    }

    static class Allies {
        public static int players = 0;
        private static ArrayList<Player> playerList = new ArrayList<Player>();

        public static void add(String name, String xp, String ping) {
            playerList.add( new Player(name, xp, ping) );
            players++;
            clients++;
        }

        public static Player get(int i) {
            return playerList.get(i);
        }

        public static void clear() {
            playerList.clear();
            players = 0;
        }
    }

    static class Spectators {
        public static int players = 0;
        private static ArrayList<Player> playerList = new ArrayList<Player>();

        public static void add(String name, String xp, String ping) {
            playerList.add( new Player(name, xp, ping) );
            players++;
            clients++;
        }

        public static Player get(int i) {
            return playerList.get(i);
        }

        public static void clear() {
            playerList.clear();
            players = 0;
        }
    }

    static class Connecting {
        public static int players = 0;
        private static ArrayList<Player> playerList = new ArrayList<Player>();

        public static void add(String name, String xp, String ping) {
            playerList.add( new Player(name, xp, ping) );
            players++;
            clients++;
        }

        public static Player get(int i) {
            return playerList.get(i);
        }

        public static void clear() {
            playerList.clear();
            players = 0;
        }
    }

}

class Player {
    protected String name = "", xp = "", ping = "";

    public Player(String name, String xp, String ping) {
        this.name = name;
        this.xp = xp;
        this.ping = ping;
    }
}

class Settings {
    private static HashMap<String, String> hash = new HashMap<String, String>();

    public static void set(String key, String value) {
        hash.put(key, value);
    }

    public static String get(String key) {
        return hash.get(key);
    }

    public static void clear() {
        hash.clear();
    }
}

class Info {
    private static HashMap<String, String> hash = new HashMap<String, String>();

    public static void set(String key, String value) {
        hash.put(key, value);
    }

    public static String get(String key) {
        return hash.get(key);
    }

    public static void clear() {
        hash.clear();
    }
}