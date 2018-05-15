package com.nders.motif;

/**
 * Created by nders on 2/4/2018.
 */

public  class Constants {

    /*
    *   Data
     */
    public static final String DATABASE =  "motifdata";
    public static final int MAX_NODE_COUNT = 519;
    public static final int GRAPH_COUNT = 23;
    public static final double NO_EDGE_SCORE = -1.0;
    public static double EDGE_THRESHOLD = 4.0;
    public static int DEGREE_THRESHOLD = 148;


    public enum DATASIZE {SIZE_5, SIZE_10, SIZE_16};
    public static final String[] DATA16 =
            {
                    "(85, 318, 344, 239, 132, 134, 174, 471, 447, 274, 452, 112, 200, 418, 306, 80)",
                    "(293, 21, 432, 442, 71, 142, 26, 314, 176, 287, 111, 16, 234, 15, 294, 309)",
                    "(38, 136, 103, 155, 131, 151, 370, 194, 356, 2, 501, 39, 81, 223, 95, 110)",
                    "(44, 230, 513, 369, 215, 53, 283, 509, 218, 265, 371, 282, 139, 462, 76, 439)",
                    "(63, 32, 396, 252, 467, 238, 487, 380, 504, 116, 511, 259, 393, 254, 322, 130)",
                    "(251, 46, 3, 403, 300, 150, 122, 124, 184, 185, 298, 281, 285, 321, 141, 145)",
                    "(253, 289, 381, 387, 288, 175, 188, 257, 220, 231, 492, 485, 421, 325, 499,123)",
                    "(435,343,226,219,423,385,117,56,443,327,503,437,453,232,156,493)",
                    "(58, 279, 431, 236, 12, 24, 241, 263, 480, 429, 237, 500, 203, 149, 97, 446)",
                    "(422, 436, 77, 301, 6, 29, 350, 388, 140, 404, 214, 420, 317, 57, 296, 119)",
                    "(165, 61, 515, 152, 227, 78, 384, 392, 216, 172, 379, 337, 225, 303, 311, 348)",
                    "(376, 13, 458, 320, 341, 468, 31, 269, 489, 248, 50, 153, 105, 60, 86, 424)",
                    "(115, 270, 374, 315, 135, 275, 394, 434, 1, 190, 42, 101, 378, 196, 477, 357)",
                    "(48, 495, 70, 186, 464, 302, 242, 482, 407, 323, 470, 118, 84, 94, 365, 305)",
                    "(490, 334, 158, 486, 466, 143, 129, 398, 262, 173, 366, 212, 473, 373, 92, 107)",
                    "(472, 474, 144, 79, 138, 23, 179, 397, 445, 518, 514, 235, 204, 349, 258, 427)",
                    "(161, 324, 405, 199, 64, 411, 402, 148, 202, 406, 510, 273, 91, 508, 450, 502)",
                    "(183, 198, 120, 444, 290, 246, 272, 166, 9, 67, 169, 8, 345, 7, 189, 171)",
                    "(36, 310, 201, 507, 297, 426, 391, 256, 277, 355, 335, 211, 93, 496, 367, 109)",
                    "(19, 170, 146, 51, 328, 154, 22, 352, 326, 99, 247, 494, 217, 195, 268, 401)",
                    "(498, 478, 197, 408, 244, 280, 65, 62, 121, 55, 37, 430, 331, 33, 304, 206)",
                    "(461, 100, 168, 463, 205, 162, 207, 419, 245, 128, 147, 108, 347, 291, 66, 25)",
                    "(360, 409, 395, 455, 191, 339, 414, 74, 292, 454, 389, 4, 338, 382, 113, 319)",
                    "(358, 178, 295, 229, 375, 210, 104, 126, 250, 483, 361, 449, 181, 441, 59, 342)",
                    "(383, 284, 451, 278, 368, 177, 96, 428, 506, 41, 243, 222, 481, 364, 20, 98)",
                    "(264, 83, 224, 286, 266, 114, 10, 332, 271, 516, 440, 469, 209, 73, 157, 133)",
                    "(72, 457, 88, 82, 90, 362, 475, 425, 484, 182, 54, 316, 180, 460, 448, 14)",
                    "(49, 488, 255, 27, 307, 159, 221, 34, 193, 43, 137, 213, 459, 299, 233, 456)",
                    "(433, 476, 512, 75, 5, 127, 240, 160, 208, 354, 346, 497, 11, 40, 68, 106)",
                    "(336, 363, 164, 102, 390, 249, 167, 35, 89, 0, 47, 417, 261, 45, 28, 192)",
                    "(18, 479, 187, 410, 491, 359, 267, 87, 312, 260, 329, 69, 308, 412, 505, 313)",
                    "(353, 438, 228, 125, 386, 415, 30, 413, 465, 340, 276, 377, 351, 52, 416, 163)"
            };

    public static final String[] DATA10 =
            {
                    "(242, 355, 42, 499, 384, 430, 87, 56, 380, 439)",
                    "(481, 63, 311, 213, 184, 299, 108, 496, 2, 99)",
                    "(179, 65, 402, 1, 22, 446, 14, 3, 272, 53)",
                    "(256, 153, 317, 161, 518, 249, 257, 490, 441, 470)",
                    "(431, 342, 77, 240, 220, 411, 137, 340, 62, 319)",
                    "(376, 399, 135, 394, 92, 197, 422, 200, 278, 245)",
                    "(405, 234, 168, 433, 70, 76, 350, 64, 335, 79)",
                    "(93, 121, 44, 46, 202, 368, 327, 73, 41, 471)",
                    "(277, 223, 49, 298, 440, 88, 50, 492, 336, 348)",
                    "(109, 26, 164, 436, 365, 280, 308, 432, 205, 233)",
                    "(347, 383, 407, 516, 183, 241, 466, 182, 51, 412)",
                    "(151, 68, 218, 497, 382, 461, 107, 315, 484, 91)",
                    "(469, 498, 29, 251, 81, 250, 352, 361, 224, 417)",
                    "(291, 21, 385, 325, 265, 188, 369, 287, 160, 511)",
                    "(366, 180, 129, 101, 284, 231, 462, 11, 374, 71)",
                    "(305, 337, 517, 156, 173, 326, 16, 194, 419, 134)",
                    "(89, 332, 104, 415, 97, 459, 112, 320, 410, 331)",
                    "(36, 58, 206, 465, 118, 209, 243, 215, 393, 203)",
                    "(321, 54, 237, 136, 400, 398, 128, 144, 152, 193)",
                    "(396, 338, 189, 166, 294, 502, 285, 460, 123, 452)",
                    "(437, 420, 371, 199, 289, 125, 178, 95, 445, 211)",
                    "(0, 214, 292, 13, 225, 283, 515, 438, 413, 165)",
                    "(474, 7, 443, 477, 450, 96, 232, 262, 103, 248)",
                    "(40, 397, 377, 185, 83, 373, 131, 116, 55, 491)",
                    "(300, 476, 488, 372, 260, 269, 8, 67, 418, 148)",
                    "(82, 115, 186, 345, 275, 506, 483, 339, 192, 357)",
                    "(94, 69, 236, 259, 293, 172, 273, 318, 207, 504)",
                    "(500, 390, 212, 270, 171, 235, 509, 467, 258, 458)",
                    "(281, 351, 453, 169, 119, 472, 238, 406, 78, 176)",
                    "(313, 448, 346, 447, 170, 143, 424, 268, 47, 296)",
                    "(267, 349, 167, 266, 227, 314, 457, 322, 244, 157)",
                    "(18, 451, 142, 208, 124, 230, 463, 19, 252, 177)"

            };

    public static final String[] DATA5 =
            {       "(85, 318, 344, 239, 132)",
                    "(134, 174, 471, 447, 274)",
                    "(452, 112, 200, 418, 306)",
                    "(80, 293, 21, 432, 442)",
                    "(71, 142, 26, 314, 176)",
                    "(287, 111, 16, 234, 15)",
                    "(294, 309, 38, 136, 103)",
                    "(155, 131, 151, 370, 194)",
                    "(356, 2, 501, 39, 81)",
                    "(223, 95, 110, 44, 230)",
                    "(513, 369, 215, 53, 283)",
                    "(509, 218, 265, 371, 282)",
                    "(139, 462, 76, 439, 63)",
                    "(32, 396, 252, 467, 238)",
                    "(487, 380, 504, 116, 511)",
                    "(259, 393, 254, 322, 130)",
                    "(251, 46, 3, 403, 300)",
                    "(150, 122, 124, 184, 185)",
                    "(298, 281, 285, 321, 141)",
                    "(145, 253, 289, 381, 387)",
                    "(288, 175, 188, 257, 220)",
                    "(231, 492, 485, 421, 325)",
                    "(499, 123, 435, 343, 226)",
                    "(219, 423, 385, 117, 56)",
                    "(443, 327, 503, 437, 453)",
                    "(232, 156, 493, 58, 279)",
                    "(431, 236, 12, 24, 241)",
                    "(263, 480, 429, 237, 500)",
                    "(203, 149, 97, 446, 422)",
                    "(436, 77, 301, 6, 29)",
                    "(350, 388, 140, 404, 214)",
                    "(420, 317, 57, 296, 119)"
            };


    public static final double[]  MAX10 = {430};


    /*
    *   Shared Preferences Keys
     */
    public static final String KEY_SOUND_ENABLED = "sound";
    public static final String KEY_MUSIC_ENABLED = "music";
    public static final String KEY_SCREEN_WIDTH = "screen_width";
    public static final String KEY_SCREEN_HEIGHT = "screen_height";
    public static String KEY_MAP_RECT_BOTTOM = "scroll_offset";
    public static final String KEY_HIGHEST_LEVEL = "highest_level";
    public static final String KEY_VIBRATION_ENABLED = "vibration_enabled";
    public static final String KEY_MUSIC_LEVEL = "music_level";
    public static final String KEY_SOUND_LEVEL = "sound_level";
    public static final String KEY_GAME_DIFFICULTY = "game_difficulty";
    public static final String KEY_GAME_COMPLETE = "game_complete";


    /*
    *  Home Menu && AboutAndOptionsActivity
     */
    public static final String KEY_ABOUT_OR_OPTIONS = "about_or_options";
    public static final int VALUE_OPTIONS = 0;
    public static final int VALUE_ABOUT = 1;


    /*
    *   GameLevel
     */

    public static final String KEY_GAME_ID = "game_ID";
    public static final String KEY_GAME_SCORE = "game_score";
    public static final int MAX_LEVEL = 23;

}
