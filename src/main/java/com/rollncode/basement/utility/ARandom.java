package com.rollncode.basement.utility;

import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Random;

/**
 * @author Tregub Artem tregub.artem@gmail.com
 * @since 24/01/17
 */
public final class ARandom {

    private static final Random RANDOM = new Random("http://rollncode.com/".hashCode());
    private static final char[] ALPHABET = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};

    @Nullable
    public static String getImageUrl() {
        switch (nextInt(13)) {
            case 0:
                return "http://www.proandroid.net/scr/3531/avatar-ukraintsa-big-7.png";

            case 1:
                return "http://cs6.pikabu.ru/images/big_size_comm/2014-04_3/13974696056179.jpg";

            case 2:
                return "http://devme.ir/wp-content/uploads/2015/09/def5.png";

            case 3:
                return "http://www.lovemarks.com/wp-content/uploads/profile-avatars/default-avatar-business-bear.png";

            case 4:
                return "https://pp.vk.me/c614823/v614823780/ba01/dF42a9Tcrr4.jpg";

            case 5:
                return "http://www.lovemarks.com/wp-content/uploads/profile-avatars/default-avatar-ponsy-deer.png";

            case 6:
                return "http://www.lovemarks.com/wp-content/uploads/profile-avatars/default-avatar-braindead-zombie.png";

            case 7:
                return "http://trikky.ru/wp-content/blogs.dir/1/files/2016/12/Avatar_1481048766783-500x500.png";

            case 8:
                return "http://www.lovemarks.com/wp-content/uploads/profile-avatars/default-avatar-knives-ninja.png";

            case 9:
                return "http://www.lovemarks.com/wp-content/uploads/profile-avatars/default-avatar-asian-girl.png";

            case 10:
                return "https://yt3.ggpht.com/-xaqyed2bmQg/AAAAAAAAAAI/AAAAAAAAAAA/Ez9_bAzCc6k/s900-c-k-no-mo-rj-c0xffffff/photo.jpg";

            case 11:
                return "http://www.lovemarks.com/wp-content/uploads/profile-avatars/default-avatar-crazy-robot.png";

            case 12:
                return "http://i.imgur.com/zqRnN9M.jpg";

            default:
                return getImageUrl();
        }
    }

    @NonNull
    private static StringBuilder generateStringInternal(int length) {
        final StringBuilder sb = new StringBuilder(length);
        while (sb.length() != length) {
            sb.append(ALPHABET[nextInt(ALPHABET.length)]);
        }
        return sb;
    }

    @NonNull
    public static String generateString(int length) {
        return generateStringInternal(length).toString();
    }

    @NonNull
    public static String generateName() {
        final int length = nextInt(10) + 5;
        final StringBuilder sb = generateStringInternal(length);
        sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));

        return sb.toString();
    }

    @NonNull
    public static String generateText(int wordsCount) {
        final StringBuilder sb = new StringBuilder(wordsCount * 16);

        for (int i = 0; i < wordsCount; i++) {
            sb.append(generateStringInternal(nextInt(15) + 1)).append(' ');
        }
        sb.deleteCharAt(sb.length() - 1);

        return sb.toString();
    }

    @SuppressWarnings("unused")
    @NonNull
    public static String generatePhoneNumber() {
        final StringBuilder sb = new StringBuilder(13).append('+');
        for (int i = 0; i < 12; i++) {
            sb.append(nextInt(10));
        }
        return sb.toString();
    }

    public static int nextInt(int i) {
        return RANDOM.nextInt(i);
    }

    public static int random(@NonNull int... values) {
        return values[nextInt(values.length)];
    }

    @NonNull
    public static String getSiteUrl() {
        switch (nextInt(5)) {
            case 0:
                return "https://www.google.ru/";

            case 1:
                return "https://lichess.org/";

            case 2:
                return "http://www.epochconverter.com/";

            case 3:
                return "http://jsbeautifier.org/";

            case 4:
                return "http://rollncode.com/";

            default:
                return getSiteUrl();
        }
    }

    @ColorInt
    public static int getColor() {
        switch (nextInt(9)) {
            case 0:
                return Color.YELLOW;

            case 1:
                return Color.RED;

            case 2:
                return Color.GREEN;

            case 3:
                return Color.BLUE;

            case 4:
                return Color.MAGENTA;

            case 5:
                return Color.CYAN;

            case 6:
                return Color.DKGRAY;

            case 7:
                return Color.LTGRAY;

            case 8:
                return Color.GRAY;

            default:
                return getColor();
        }
    }
}