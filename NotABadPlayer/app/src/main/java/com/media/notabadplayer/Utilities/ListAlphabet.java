package com.media.notabadplayer.Utilities;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ListAlphabet {
    public static final int ALPHABET_CAPACITY = 24;

    private final @NonNull ArrayList<Character> _alphabet = new ArrayList<>();

    public boolean isEmpty()
    {
        return _alphabet.isEmpty();
    }

    public @NonNull List<Character> getCharacters()
    {
        return Collections.unmodifiableList(_alphabet);
    }

    public void clear()
    {
        _alphabet.clear();
    }

    public void updateAlphabet(@NonNull ArrayList<String> titles)
    {
        ArrayList<String> result = new ArrayList<>();

        for (int e = 0; e < titles.size(); e++)
        {
            if (result.size() >= ALPHABET_CAPACITY)
            {
                break;
            }

            String title = titles.get(e);

            if (title.length() > 0)
            {
                char firstChar = transformToCheckedCharacter(title.charAt(0));

                String resultString = String.format("%c", firstChar);

                if (!result.contains(resultString))
                {
                    result.add(resultString);
                }
            }
        }

        Collections.sort(result);

        // Replace current values with result
        _alphabet.clear();

        for (String str : result) {
            _alphabet.add(str.charAt(0));
        }
    }

    public static String englishAlphabet()
    {
        return "abcdefghijklmnopqrstuvwxyz";
    }

    public static char transformToCheckedCharacter(char uncheckedChar)
    {
        uncheckedChar = Character.toLowerCase(uncheckedChar);

        if (englishAlphabet().indexOf(uncheckedChar) != -1) {
            return Character.toUpperCase(uncheckedChar);
        }

        // Use '0' to represent all digits
        if (Character.isDigit(uncheckedChar)) {
            return '0';
        }

        // Use hashtag to represent all other symbols
        return '#';
    }

    public static int compareStrings(@NonNull String first, @NonNull String second)
    {
        if (first.isEmpty() || second.isEmpty()) {
            return 0;
        }

        Character a = ListAlphabet.transformToCheckedCharacter(first.charAt(0));
        Character b = ListAlphabet.transformToCheckedCharacter(second.charAt(0));

        return a.compareTo(b);
    }
}
